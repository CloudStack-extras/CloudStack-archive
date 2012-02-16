/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.cloud.user;

import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.cloud.acl.ControlledEntity;
import com.cloud.acl.SecurityChecker;
import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ApiDBUtils;
import com.cloud.api.commands.DeleteUserCmd;
import com.cloud.api.commands.ListAccountsCmd;
import com.cloud.api.commands.ListUsersCmd;
import com.cloud.api.commands.RegisterCmd;
import com.cloud.api.commands.UpdateAccountCmd;
import com.cloud.api.commands.UpdateUserCmd;
import com.cloud.configuration.Config;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.configuration.ResourceLimit;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.configuration.dao.ResourceCountDao;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.domain.Domain;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.event.EventUtils;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.CloudAuthenticationException;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IPAddressVO;
import com.cloud.network.IpAddress;
import com.cloud.network.NetworkManager;
import com.cloud.network.NetworkVO;
import com.cloud.network.RemoteAccessVpnVO;
import com.cloud.network.VpnUserVO;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.RemoteAccessVpnDao;
import com.cloud.network.dao.VpnUserDao;
import com.cloud.network.security.SecurityGroupManager;
import com.cloud.network.security.dao.SecurityGroupDao;
import com.cloud.network.vpn.RemoteAccessVpnService;
import com.cloud.projects.Project;
import com.cloud.projects.Project.ListProjectResourcesCriteria;
import com.cloud.projects.ProjectInvitationVO;
import com.cloud.projects.ProjectManager;
import com.cloud.projects.ProjectVO;
import com.cloud.projects.dao.ProjectAccountDao;
import com.cloud.projects.dao.ProjectDao;
import com.cloud.server.auth.UserAuthenticator;
import com.cloud.storage.StorageManager;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.Volume;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.snapshot.SnapshotManager;
import com.cloud.template.TemplateManager;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account.State;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserAccountDao;
import com.cloud.user.dao.UserDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.Ternary;
import com.cloud.utils.component.Adapters;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.component.Manager;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.InstanceGroupVO;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.ReservationContextImpl;
import com.cloud.vm.UserVmManager;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.dao.InstanceGroupDao;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;

@Local(value = { AccountManager.class, AccountService.class })
public class AccountManagerImpl implements AccountManager, AccountService, Manager {
    public static final Logger s_logger = Logger.getLogger(AccountManagerImpl.class);

    private String _name;
    @Inject
    private AccountDao _accountDao;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    private ResourceCountDao _resourceCountDao;
    @Inject
    private UserDao _userDao;
    @Inject
    private InstanceGroupDao _vmGroupDao;
    @Inject
    private UserAccountDao _userAccountDao;
    @Inject
    private VolumeDao _volumeDao;
    @Inject
    private UserVmDao _userVmDao;
    @Inject
    private VMTemplateDao _templateDao;
    @Inject
    private NetworkDao _networkDao;
    @Inject
    private SecurityGroupDao _securityGroupDao;
    @Inject
    private VMInstanceDao _vmDao;
    @Inject
    protected SnapshotDao _snapshotDao;
    @Inject
    protected VMTemplateDao _vmTemplateDao;
    @Inject
    private SecurityGroupManager _networkGroupMgr;
    @Inject
    private NetworkManager _networkMgr;
    @Inject
    private SnapshotManager _snapMgr;
    @Inject
    private UserVmManager _vmMgr;
    @Inject
    private StorageManager _storageMgr;
    @Inject
    private TemplateManager _tmpltMgr;
    @Inject
    private ConfigurationManager _configMgr;
    @Inject
    private VirtualMachineManager _itMgr;
    @Inject
    private RemoteAccessVpnDao _remoteAccessVpnDao;
    @Inject
    private RemoteAccessVpnService _remoteAccessVpnMgr;
    @Inject
    private VpnUserDao _vpnUser;
    @Inject
    private DataCenterDao _dcDao;
    @Inject
    private DomainManager _domainMgr;
    @Inject
    private ProjectManager _projectMgr;
    @Inject
    private ProjectDao _projectDao;
    @Inject
    private AccountDetailsDao _accountDetailsDao;
    @Inject
    private DomainDao _domainDao;
    @Inject
    private ProjectAccountDao _projectAccountDao;
    @Inject
    private IPAddressDao _ipAddressDao;

    private Adapters<UserAuthenticator> _userAuthenticators;

    private final ScheduledExecutorService _executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("AccountChecker"));

    UserVO _systemUser;
    AccountVO _systemAccount;
    @Inject(adapter = SecurityChecker.class)
    Adapters<SecurityChecker> _securityCheckers;
    int _cleanupInterval;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;

        _systemAccount = _accountDao.findById(AccountVO.ACCOUNT_ID_SYSTEM);
        if (_systemAccount == null) {
            throw new ConfigurationException("Unable to find the system account using " + Account.ACCOUNT_ID_SYSTEM);
        }

        _systemUser = _userDao.findById(UserVO.UID_SYSTEM);
        if (_systemUser == null) {
            throw new ConfigurationException("Unable to find the system user using " + User.UID_SYSTEM);
        }

        ComponentLocator locator = ComponentLocator.getCurrentLocator();
        ConfigurationDao configDao = locator.getDao(ConfigurationDao.class);
        Map<String, String> configs = configDao.getConfiguration(params);

        String value = configs.get(Config.AccountCleanupInterval.key());
        _cleanupInterval = NumbersUtil.parseInt(value, 60 * 60 * 24); // 1 hour.

        _userAuthenticators = locator.getAdapters(UserAuthenticator.class);
        if (_userAuthenticators == null || !_userAuthenticators.isSet()) {
            s_logger.error("Unable to find an user authenticator.");
        }

        return true;
    }

    @Override
    public UserVO getSystemUser() {
        return _systemUser;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean start() {
        _executor.scheduleAtFixedRate(new AccountCleanupTask(), _cleanupInterval, _cleanupInterval, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    public AccountVO getSystemAccount() {
        if (_systemAccount == null) {
            _systemAccount = _accountDao.findById(Account.ACCOUNT_ID_SYSTEM);
        }
        return _systemAccount;
    }

    @Override
    public boolean isAdmin(short accountType) {
        return ((accountType == Account.ACCOUNT_TYPE_ADMIN) || (accountType == Account.ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN) || (accountType == Account.ACCOUNT_TYPE_DOMAIN_ADMIN) || (accountType == Account.ACCOUNT_TYPE_READ_ONLY_ADMIN));
    }

    @Override
    public boolean isRootAdmin(short accountType) {
        return (accountType == Account.ACCOUNT_TYPE_ADMIN);
    }

    public boolean isResourceDomainAdmin(short accountType) {
        return (accountType == Account.ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN);
    }

    @Override
    public void checkAccess(Account caller, Domain domain) throws PermissionDeniedException {
        for (SecurityChecker checker : _securityCheckers) {
            if (checker.checkAccess(caller, domain)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Access granted to " + caller + " to " + domain + " by " + checker.getName());
                }
                return;
            }
        }

        assert false : "How can all of the security checkers pass on checking this caller?";
        throw new PermissionDeniedException("There's no way to confirm " + caller + " has access to " + domain);
    }

    @Override
    public void checkAccess(Account caller, AccessType accessType, boolean sameOwner, ControlledEntity... entities) {

        if (caller.getId() == Account.ACCOUNT_ID_SYSTEM || isRootAdmin(caller.getType())) {
            // no need to make permission checks if the system/root admin makes the call
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("No need to make permission check for System/RootAdmin account, returning true");
            }
            return;
        }

        HashMap<Long, List<ControlledEntity>> domains = new HashMap<Long, List<ControlledEntity>>();
        Long ownerId = null;
        ControlledEntity prevEntity = null;

        for (ControlledEntity entity : entities) {
            long domainId = entity.getDomainId();
            if (entity.getAccountId() != -1 && domainId == -1) { // If account exists domainId should too so calculate
// it. This condition might be hit for templates or entities which miss domainId in their tables
                Account account = ApiDBUtils.findAccountById(entity.getAccountId());
                domainId = account != null ? account.getDomainId() : -1;
            }
            if (entity.getAccountId() != -1 && domainId != -1 && !(entity instanceof VirtualMachineTemplate) && !(accessType != null && accessType == AccessType.UseNetwork)) {
                List<ControlledEntity> toBeChecked = domains.get(entity.getDomainId());
                // for templates, we don't have to do cross domains check
                if (toBeChecked == null) {
                    toBeChecked = new ArrayList<ControlledEntity>();
                    domains.put(domainId, toBeChecked);
                }
                toBeChecked.add(entity);
            }
            boolean granted = false;
            for (SecurityChecker checker : _securityCheckers) {
                if (checker.checkAccess(caller, entity, accessType)) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Access to " + entity + " granted to " + caller + " by " + checker.getName());
                    }
                    granted = true;
                    break;
                }
            }

            if (sameOwner) {
                if (ownerId == null) {
                    ownerId = entity.getAccountId();
                } else if (ownerId.longValue() != entity.getAccountId()) {
                    throw new PermissionDeniedException("Entity " + entity + " and entity " + prevEntity + " belong to different accounts");
                }
                prevEntity = entity;
            }

            if (!granted) {
                assert false : "How can all of the security checkers pass on checking this check: " + entity;
                throw new PermissionDeniedException("There's no way to confirm " + caller + " has access to " + entity);
            }
        }

        for (Map.Entry<Long, List<ControlledEntity>> domain : domains.entrySet()) {
            for (SecurityChecker checker : _securityCheckers) {
                Domain d = _domainMgr.getDomain(domain.getKey());
                if (d == null || d.getRemoved() != null) {
                    throw new PermissionDeniedException("Domain is not found.", caller, domain.getValue());
                }
                try {
                    checker.checkAccess(caller, d);
                } catch (PermissionDeniedException e) {
                    e.addDetails(caller, domain.getValue());
                    throw e;
                }
            }
        }

        // check that resources belong to the same account

    }

    @Override
    public Long checkAccessAndSpecifyAuthority(Account caller, Long zoneId) {
        // We just care for resource domain admin for now. He should be permitted to see only his zone.
        if (isResourceDomainAdmin(caller.getType())) {
            if (zoneId == null)
                return getZoneIdForAccount(caller);
            else if (zoneId.compareTo(getZoneIdForAccount(caller)) != 0)
                throw new PermissionDeniedException("Caller " + caller + "is not allowed to access the zone " + zoneId);
            else
                return zoneId;
        }

        else
            return zoneId;
    }

    private Long getZoneIdForAccount(Account account) {

        // Currently just for resource domain admin
        List<DataCenterVO> dcList = _dcDao.findZonesByDomainId(account.getDomainId());
        if (dcList != null && dcList.size() != 0)
            return dcList.get(0).getId();
        else
            throw new CloudRuntimeException("Failed to find any private zone for Resource domain admin.");

    }

    private boolean doSetUserStatus(long userId, State state) {
        UserVO userForUpdate = _userDao.createForUpdate();
        userForUpdate.setState(state);
        return _userDao.update(Long.valueOf(userId), userForUpdate);
    }

    @Override
    public boolean enableAccount(long accountId) {
        boolean success = false;
        AccountVO acctForUpdate = _accountDao.createForUpdate();
        acctForUpdate.setState(State.enabled);
        acctForUpdate.setNeedsCleanup(false);
        success = _accountDao.update(Long.valueOf(accountId), acctForUpdate);
        return success;
    }

    @Override
    public boolean lockAccount(long accountId) {
        boolean success = false;
        Account account = _accountDao.findById(accountId);
        if (account != null) {
            if (account.getState().equals(State.locked)) {
                return true; // already locked, no-op
            } else if (account.getState().equals(State.enabled)) {
                AccountVO acctForUpdate = _accountDao.createForUpdate();
                acctForUpdate.setState(State.locked);
                success = _accountDao.update(Long.valueOf(accountId), acctForUpdate);
            } else {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("Attempting to lock a non-enabled account, current state is " + account.getState() + " (accountId: " + accountId + "), locking failed.");
                }
            }
        } else {
            s_logger.warn("Failed to lock account " + accountId + ", account not found.");
        }
        return success;
    }

    @Override
    public boolean deleteAccount(AccountVO account, long callerUserId, Account caller) {
        long accountId = account.getId();
        
        //delete the account record
        if (!_accountDao.remove(accountId)) {
            s_logger.error("Unable to delete account " + accountId);
            return false;
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Removed account " + accountId);
        }

        return cleanupAccount(account, callerUserId, caller);
    }

    @Override
    public boolean cleanupAccount(AccountVO account, long callerUserId, Account caller) {
        long accountId = account.getId();
        boolean accountCleanupNeeded = false;
        
        try {
            //cleanup the users from the account
            List<UserVO> users = _userDao.listByAccount(accountId);
            for (UserVO user : users) {
                if (!_userDao.remove(user.getId())) {
                    s_logger.error("Unable to delete user: " + user + " as a part of account " + account + " cleanup");
                    accountCleanupNeeded = true;
                }
            }
            
            //delete the account from project accounts
            _projectAccountDao.removeAccountFromProjects(accountId);
            
            // delete all vm groups belonging to accont
            List<InstanceGroupVO> groups = _vmGroupDao.listByAccountId(accountId);
            for (InstanceGroupVO group : groups) {
                if (!_vmMgr.deleteVmGroup(group.getId())) {
                    s_logger.error("Unable to delete group: " + group.getId());
                    accountCleanupNeeded = true;
                }
            }

            // Delete the snapshots dir for the account. Have to do this before destroying the VMs.
            boolean success = _snapMgr.deleteSnapshotDirsForAccount(accountId);
            if (success) {
                s_logger.debug("Successfully deleted snapshots directories for all volumes under account " + accountId + " across all zones");
            }

            // clean up templates
            List<VMTemplateVO> userTemplates = _templateDao.listByAccountId(accountId);
            boolean allTemplatesDeleted = true;
            for (VMTemplateVO template : userTemplates) {
                if (template.getRemoved() == null) {
                    try {
                        allTemplatesDeleted = _tmpltMgr.delete(callerUserId, template.getId(), null);
                    } catch (Exception e) {
                        s_logger.warn("Failed to delete template while removing account: " + template.getName() + " due to: ", e);
                        allTemplatesDeleted = false;
                    }
                }
            }

            if (!allTemplatesDeleted) {
                s_logger.warn("Failed to delete templates while removing account id=" + accountId);
                accountCleanupNeeded = true;
            }

            // Destroy the account's VMs
            List<UserVmVO> vms = _userVmDao.listByAccountId(accountId);
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Expunging # of vms (accountId=" + accountId + "): " + vms.size());
            }

            // no need to catch exception at this place as expunging vm should pass in order to perform further cleanup
            for (UserVmVO vm : vms) {
                if (!_vmMgr.expunge(vm, callerUserId, caller)) {
                    s_logger.error("Unable to expunge vm: " + vm.getId());
                    accountCleanupNeeded = true;
                }
            }

            // Mark the account's volumes as destroyed
            List<VolumeVO> volumes = _volumeDao.findDetachedByAccount(accountId);
            for (VolumeVO volume : volumes) {
                if (!volume.getState().equals(Volume.State.Destroy)) {
                    try {
                        _storageMgr.destroyVolume(volume);
                    } catch (Exception ex) {
                        s_logger.warn("Failed to cleanup volumes as a part of account id=" + accountId + " cleanup due to Exception: ", ex);
                        accountCleanupNeeded = true;
                    }
                }
            }

            // delete remote access vpns and associated users
            List<RemoteAccessVpnVO> remoteAccessVpns = _remoteAccessVpnDao.findByAccount(accountId);
            List<VpnUserVO> vpnUsers = _vpnUser.listByAccount(accountId);

            for (VpnUserVO vpnUser : vpnUsers) {
                _remoteAccessVpnMgr.removeVpnUser(accountId, vpnUser.getUsername());
            }

            try {
                for (RemoteAccessVpnVO vpn : remoteAccessVpns) {
                    _remoteAccessVpnMgr.destroyRemoteAccessVpn(vpn.getServerAddressId());
                }
            } catch (ResourceUnavailableException ex) {
                s_logger.warn("Failed to cleanup remote access vpn resources as a part of account id=" + accountId + " cleanup due to Exception: ", ex);
                accountCleanupNeeded = true;
            }

            // Cleanup security groups
            int numRemoved = _securityGroupDao.removeByAccountId(accountId);
            s_logger.info("deleteAccount: Deleted " + numRemoved + " network groups for account " + accountId);

            // Delete all the networks
            boolean networksDeleted = true;
            s_logger.debug("Deleting networks for account " + account.getId());
            List<NetworkVO> networks = _networkDao.listByOwner(accountId);
            if (networks != null) {
                for (NetworkVO network : networks) {

                    ReservationContext context = new ReservationContextImpl(null, null, getActiveUser(callerUserId), account);

                    if (!_networkMgr.destroyNetwork(network.getId(), context)) {
                        s_logger.warn("Unable to destroy network " + network + " as a part of account id=" + accountId + " cleanup.");
                        accountCleanupNeeded = true;
                        networksDeleted = false;
                    } else {
                        s_logger.debug("Network " + network.getId() + " successfully deleted as a part of account id=" + accountId + " cleanup.");
                    }
                }
            }

            // release ip addresses belonging to the account
            List<? extends IpAddress> ipsToRelease = _ipAddressDao.listByAccount(accountId);
            for (IpAddress ip : ipsToRelease) {
                s_logger.debug("Releasing ip " + ip + " as a part of account id=" + accountId + " cleanup");
                if (!_networkMgr.releasePublicIpAddress(ip.getId(), callerUserId, caller)) {
                    s_logger.warn("Failed to release ip address " + ip + " as a part of account id=" + accountId + " clenaup");
                    accountCleanupNeeded = true;
                }
            }

            // delete account specific Virtual vlans (belong to system Public Network) - only when networks are cleaned
            // up
            // successfully
            if (networksDeleted) {
                if (!_configMgr.deleteAccountSpecificVirtualRanges(accountId)) {
                    accountCleanupNeeded = true;
                } else {
                    s_logger.debug("Account specific Virtual IP ranges " + " are successfully deleted as a part of account id=" + accountId + " cleanup.");
                }
            }

            return true;
        } catch (Exception ex) {
            s_logger.warn("Failed to cleanup account " + account + " due to ", ex);
            accountCleanupNeeded = true;
            return true;
        } finally {
            s_logger.info("Cleanup for account " + account.getId() + (accountCleanupNeeded ? " is needed." : " is not needed."));
            if (accountCleanupNeeded) {
                _accountDao.markForCleanup(accountId);
            }
        }
    }

    @Override
    public boolean disableAccount(long accountId) throws ConcurrentOperationException, ResourceUnavailableException {
        boolean success = false;
        if (accountId <= 2) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("disableAccount -- invalid account id: " + accountId);
            }
            return false;
        }

        AccountVO account = _accountDao.findById(accountId);
        if ((account == null) || (account.getState().equals(State.disabled) && !account.getNeedsCleanup())) {
            success = true;
        } else {
            AccountVO acctForUpdate = _accountDao.createForUpdate();
            acctForUpdate.setState(State.disabled);
            success = _accountDao.update(Long.valueOf(accountId), acctForUpdate);

            if (success) {
                if (!doDisableAccount(accountId)) {
                    s_logger.warn("Failed to disable account " + account + " resources as a part of disableAccount call, marking the account for cleanup");
                    _accountDao.markForCleanup(accountId);
                }
            }
        }
        return success;
    }

    private boolean doDisableAccount(long accountId) throws ConcurrentOperationException, ResourceUnavailableException {
        List<VMInstanceVO> vms = _vmDao.listByAccountId(accountId);
        boolean success = true;
        for (VMInstanceVO vm : vms) {
            try {
                try {
                    success = (success && _itMgr.advanceStop(vm, false, getSystemUser(), getSystemAccount()));
                } catch (OperationTimedoutException ote) {
                    s_logger.warn("Operation for stopping vm timed out, unable to stop vm " + vm.getHostName(), ote);
                    success = false;
                }
            } catch (AgentUnavailableException aue) {
                s_logger.warn("Agent running on host " + vm.getHostId() + " is unavailable, unable to stop vm " + vm.getHostName(), aue);
                success = false;
            }
        }

        return success;
    }

    // ///////////////////////////////////////////////////
    // ////////////// API commands /////////////////////
    // ///////////////////////////////////////////////////

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_ACCOUNT_CREATE, eventDescription = "creating Account")
    public UserAccount createUserAccount(String userName, String password, String firstName, String lastName, String email, String timezone, String accountName, short accountType, Long domainId, String networkDomain,
            Map<String, String> details) {

        if (accountName == null) {
            accountName = userName;
        }
        if (domainId == null) {
            domainId = DomainVO.ROOT_DOMAIN;
        }

        // Validate domain
        Domain domain = _domainMgr.getDomain(domainId);
        if (domain == null) {
            throw new InvalidParameterValueException("The domain " + domainId + " does not exist; unable to create account");
        }

        // Check permissions
        checkAccess(UserContext.current().getCaller(), domain);

        if (!_userAccountDao.validateUsernameInDomain(userName, domainId)) {
            throw new InvalidParameterValueException("The user " + userName + " already exists in domain " + domainId);
        }

        if (networkDomain != null) {
            if (!NetUtils.verifyDomainName(networkDomain)) {
                throw new InvalidParameterValueException(
                        "Invalid network domain. Total length shouldn't exceed 190 chars. Each domain label must be between 1 and 63 characters long, can contain ASCII letters 'a' through 'z', the digits '0' through '9', "
                                + "and the hyphen ('-'); can't start or end with \"-\"");
            }
        }

        Transaction txn = Transaction.currentTxn();
        txn.start();

        // create account
        Account account = createAccount(accountName, accountType, domainId, networkDomain, details);
        long accountId = account.getId();

        // create the first user for the account
        UserVO user = createUser(accountId, userName, password, firstName, lastName, email, timezone);

        if (accountType == Account.ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN) {
            // set registration token
            byte[] bytes = (domainId + accountName + userName + System.currentTimeMillis()).getBytes();
            String registrationToken = UUID.nameUUIDFromBytes(bytes).toString();
            user.setRegistrationToken(registrationToken);
        }

        txn.commit();
        return _userAccountDao.findById(user.getId());
    }

    @Override
    public UserVO createUser(String userName, String password, String firstName, String lastName, String email, String timeZone, String accountName, Long domainId) {

        // default domain to ROOT if not specified
        if (domainId == null) {
            domainId = Domain.ROOT_DOMAIN;
        }

        Domain domain = _domainMgr.getDomain(domainId);
        if (domain == null) {
            throw new CloudRuntimeException("The domain " + domainId + " does not exist; unable to create user");
        } else if (domain.getState().equals(Domain.State.Inactive)) {
            throw new CloudRuntimeException("The user cannot be created as domain " + domain.getName() + " is being deleted");
        }

        checkAccess(UserContext.current().getCaller(), domain);

        Account account = _accountDao.findEnabledAccount(accountName, domainId);
        if (account == null || account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain id=" + domainId + " to create user");
        }

        if (!_userAccountDao.validateUsernameInDomain(userName, domainId)) {
            throw new CloudRuntimeException("The user " + userName + " already exists in domain " + domainId);
        }

        UserVO user = createUser(account.getId(), userName, password, firstName, lastName, email, timeZone);

        return user;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_USER_UPDATE, eventDescription = "updating User")
    public UserAccount updateUser(UpdateUserCmd cmd) {
        Long id = cmd.getId();
        String apiKey = cmd.getApiKey();
        String firstName = cmd.getFirstname();
        String email = cmd.getEmail();
        String lastName = cmd.getLastname();
        String password = cmd.getPassword();
        String secretKey = cmd.getSecretKey();
        String timeZone = cmd.getTimezone();
        String userName = cmd.getUsername();

        // Input validation
        UserVO user = _userDao.getUser(id);

        if (user == null) {
            throw new InvalidParameterValueException("unable to find user by id");
        }

        if ((apiKey == null && secretKey != null) || (apiKey != null && secretKey == null)) {
            throw new InvalidParameterValueException("Please provide an userApiKey/userSecretKey pair");
        }

        // If the account is an admin type, return an error. We do not allow this
        Account account = _accountDao.findById(user.getAccountId());

        // don't allow updating project account
        if (account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("unable to find user by id");
        }

        if (account != null && (account.getId() == Account.ACCOUNT_ID_SYSTEM)) {
            throw new PermissionDeniedException("user id : " + id + " is system account, update is not allowed");
        }

        checkAccess(UserContext.current().getCaller(), null, true, account);

        if (firstName != null) {
            user.setFirstname(firstName);
        }
        if (lastName != null) {
            user.setLastname(lastName);
        }
        if (userName != null) {
            // don't allow to have same user names in the same domain
            List<UserVO> duplicatedUsers = _userDao.findUsersLike(userName);
            for (UserVO duplicatedUser : duplicatedUsers) {
                if (duplicatedUser.getId() != user.getId()) {
                    Account duplicatedUserAccount = _accountDao.findById(duplicatedUser.getAccountId());
                    if (duplicatedUserAccount.getDomainId() == account.getDomainId()) {
                        throw new InvalidParameterValueException("User with name " + userName + " already exists in domain " + duplicatedUserAccount.getDomainId());
                    }
                }
            }

            user.setUsername(userName);
        }
        if (password != null) {
            user.setPassword(password);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (timeZone != null) {
            user.setTimezone(timeZone);
        }
        if (apiKey != null) {
            user.setApiKey(apiKey);
        }
        if (secretKey != null) {
            user.setSecretKey(secretKey);
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("updating user with id: " + id);
        }
        try {
            // check if the apiKey and secretKey are globally unique
            if (apiKey != null && secretKey != null) {
                Pair<User, Account> apiKeyOwner = _accountDao.findUserAccountByApiKey(apiKey);

                if (apiKeyOwner != null) {
                    User usr = apiKeyOwner.first();
                    if (usr.getId() != id) {
                        throw new InvalidParameterValueException("The api key:" + apiKey + " exists in the system for user id:" + id + " ,please provide a unique key");
                    } else {
                        // allow the updation to take place
                    }
                }
            }

            _userDao.update(id, user);
        } catch (Throwable th) {
            s_logger.error("error updating user", th);
            throw new CloudRuntimeException("Unable to update user " + id);
        }
        return _userAccountDao.findById(id);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_USER_DISABLE, eventDescription = "disabling User", async = true)
    public UserAccount disableUser(long userId) {
        Account caller = UserContext.current().getCaller();

        // Check if user exists in the system
        User user = _userDao.findById(userId);
        if (user == null || user.getRemoved() != null) {
            throw new InvalidParameterValueException("Unable to find active user by id " + userId);
        }

        Account account = _accountDao.findById(user.getAccountId());

        // don't allow disabling user belonging to project's account
        if (account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Unable to find active user by id " + userId);
        }

        // If the user is a System user, return an error
        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new InvalidParameterValueException("User id : " + userId + " is a system user, disabling is not allowed");
        }

        checkAccess(caller, null, true, account);

        boolean success = doSetUserStatus(userId, State.disabled);
        if (success) {
            // user successfully disabled
            return _userAccountDao.findById(userId);
        } else {
            throw new CloudRuntimeException("Unable to disable user " + userId);
        }
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_USER_ENABLE, eventDescription = "enabling User")
    public UserAccount enableUser(long userId) {

        Account caller = UserContext.current().getCaller();

        // Check if user exists in the system
        User user = _userDao.findById(userId);
        if (user == null || user.getRemoved() != null) {
            throw new InvalidParameterValueException("Unable to find active user by id " + userId);
        }

        Account account = _accountDao.findById(user.getAccountId());

        if (account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Unable to find active user by id " + userId);
        }

        // If the user is a System user, return an error
        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new InvalidParameterValueException("User id : " + userId + " is a system user, enabling is not allowed");
        }

        checkAccess(caller, null, true, account);

        Transaction txn = Transaction.currentTxn();
        txn.start();

        boolean success = doSetUserStatus(userId, State.enabled);

        // make sure the account is enabled too
        success = success && enableAccount(user.getAccountId());

        txn.commit();

        if (success) {
            return _userAccountDao.findById(userId);
        } else {
            throw new CloudRuntimeException("Unable to enable user " + userId);
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_USER_LOCK, eventDescription = "locking User")
    public UserAccount lockUser(long userId) {
        Account caller = UserContext.current().getCaller();

        // Check if user with id exists in the system
        User user = _userDao.findById(userId);
        if (user == null || user.getRemoved() != null) {
            throw new InvalidParameterValueException("Unable to find user by id");
        }

        Account account = _accountDao.findById(user.getAccountId());

        // don't allow to lock user of the account of type Project
        if (account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Unable to find user by id");
        }

        // If the user is a System user, return an error. We do not allow this
        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new PermissionDeniedException("user id : " + userId + " is a system user, locking is not allowed");
        }

        checkAccess(caller, null, true, account);

        // make sure the account is enabled too
        // if the user is either locked already or disabled already, don't change state...only lock currently enabled
// users
        boolean success = true;
        if (user.getState().equals(State.locked)) {
            // already locked...no-op
            return _userAccountDao.findById(userId);
        } else if (user.getState().equals(State.enabled)) {
            success = doSetUserStatus(user.getId(), State.locked);

            boolean lockAccount = true;
            List<UserVO> allUsersByAccount = _userDao.listByAccount(user.getAccountId());
            for (UserVO oneUser : allUsersByAccount) {
                if (oneUser.getState().equals(State.enabled)) {
                    lockAccount = false;
                    break;
                }
            }

            if (lockAccount) {
                success = (success && lockAccount(user.getAccountId()));
            }
        } else {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("Attempting to lock a non-enabled user, current state is " + user.getState() + " (userId: " + user.getId() + "), locking failed.");
            }
            success = false;
        }

        if (success) {
            return _userAccountDao.findById(userId);
        } else {
            throw new CloudRuntimeException("Unable to lock user " + userId);
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_ACCOUNT_DELETE, eventDescription = "deleting account", async = true)
    // This method deletes the account
    public boolean deleteUserAccount(long accountId) {

        UserContext ctx = UserContext.current();
        long callerUserId = ctx.getCallerUserId();
        Account caller = ctx.getCaller();

        // If the user is a System user, return an error. We do not allow this
        AccountVO account = _accountDao.findById(accountId);

        if (account.getRemoved() != null) {
            s_logger.info("The account:" + account.getAccountName() + " is already removed");
            return true;
        }

        // don't allow removing Project account
        if (account == null || account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("The specified account does not exist in the system");
        }

        checkAccess(caller, null, true, account);

        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new PermissionDeniedException("Account id : " + accountId + " is a system account, delete is not allowed");
        }

        // Account that manages project(s) can't be removed
        List<Long> managedProjectIds = _projectAccountDao.listAdministratedProjectIds(accountId);
        if (!managedProjectIds.isEmpty()) {
            StringBuilder projectIds = new StringBuilder();
            for (Long projectId : managedProjectIds) {
                projectIds.append(projectId + ", ");
            }

            throw new InvalidParameterValueException("The account id=" + accountId + " manages project(s) with ids " + projectIds + "and can't be removed");
        }

        return deleteAccount(account, callerUserId, caller);
    }

    @Override
    public AccountVO enableAccount(String accountName, Long domainId, Long accountId) {

        // Check if account exists
        Account account = null;
        if (accountId != null) {
            account = _accountDao.findById(accountId);
        } else {
            account = _accountDao.findActiveAccount(accountName, domainId);
        }

        if (account == null || account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Unable to find account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }

        // Don't allow to modify system account
        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new InvalidParameterValueException("Can not modify system account");
        }

        // Check if user performing the action is allowed to modify this account
        Account caller = UserContext.current().getCaller();
        checkAccess(caller, null, true, account);

        boolean success = enableAccount(account.getId());
        if (success) {
            return _accountDao.findById(account.getId());
        } else {
            throw new CloudRuntimeException("Unable to enable account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_ACCOUNT_DISABLE, eventDescription = "locking account", async = true)
    public AccountVO lockAccount(String accountName, Long domainId, Long accountId) {
        Account caller = UserContext.current().getCaller();

        Account account = null;
        if (accountId != null) {
            account = _accountDao.findById(accountId);
        } else {
            account = _accountDao.findActiveAccount(accountName, domainId);
        }

        if (account == null || account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Unable to find active account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }

        checkAccess(caller, null, true, account);

        // don't allow modify system account
        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new InvalidParameterValueException("can not lock system account");
        }

        if (lockAccount(account.getId())) {
            return _accountDao.findById(account.getId());
        } else {
            throw new CloudRuntimeException("Unable to lock account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_ACCOUNT_DISABLE, eventDescription = "disabling account", async = true)
    public AccountVO disableAccount(String accountName, Long domainId, Long accountId) throws ConcurrentOperationException, ResourceUnavailableException {
        Account caller = UserContext.current().getCaller();

        Account account = null;
        if (accountId != null) {
            account = _accountDao.findById(accountId);
        } else {
            account = _accountDao.findActiveAccount(accountName, domainId);
        }

        if (account == null || account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Unable to find account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }

        checkAccess(caller, null, true, account);

        if (disableAccount(account.getId())) {
            return _accountDao.findById(account.getId());
        } else {
            throw new CloudRuntimeException("Unable to update account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }
    }

    @Override
    @DB
    public AccountVO updateAccount(UpdateAccountCmd cmd) {
        Long accountId = cmd.getId();
        Long domainId = cmd.getDomainId();
        String accountName = cmd.getAccountName();
        String newAccountName = cmd.getNewName();
        String networkDomain = cmd.getNetworkDomain();
        Map<String, String> details = cmd.getDetails();

        boolean success = false;
        Account account = null;
        if (accountId != null) {
            account = _accountDao.findById(accountId);
        } else {
            account = _accountDao.findEnabledAccount(accountName, domainId);
        }

        // Check if account exists
        if (account == null || account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            s_logger.error("Unable to find account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
            throw new InvalidParameterValueException("Unable to find account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }

        // Don't allow to modify system account
        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new InvalidParameterValueException("Can not modify system account");
        }

        // Check if user performing the action is allowed to modify this account
        checkAccess(UserContext.current().getCaller(), _domainMgr.getDomain(account.getDomainId()));

        // check if the given account name is unique in this domain for updating
        Account duplicateAcccount = _accountDao.findActiveAccount(newAccountName, domainId);
        if (duplicateAcccount != null && duplicateAcccount.getId() != account.getId()) {// allow
                                                                                        // same
                                                                                        // account
                                                                                        // to
                                                                                        // update
                                                                                        // itself
            throw new InvalidParameterValueException("There already exists an account with the name:" + newAccountName + " in the domain:" + domainId + " with existing account id:"
                    + duplicateAcccount.getId());
        }

        if (networkDomain != null && !networkDomain.isEmpty()) {
            if (!NetUtils.verifyDomainName(networkDomain)) {
                throw new InvalidParameterValueException(
                        "Invalid network domain. Total length shouldn't exceed 190 chars. Each domain label must be between 1 and 63 characters long, can contain ASCII letters 'a' through 'z', the digits '0' through '9', "
                                + "and the hyphen ('-'); can't start or end with \"-\"");
            }
        }

        AccountVO acctForUpdate = _accountDao.findById(account.getId());
        acctForUpdate.setAccountName(newAccountName);

        if (networkDomain != null) {
            if (networkDomain.isEmpty()) {
                acctForUpdate.setNetworkDomain(null);
            } else {
                acctForUpdate.setNetworkDomain(networkDomain);
            }
        }

        Transaction txn = Transaction.currentTxn();
        txn.start();

        success = _accountDao.update(account.getId(), acctForUpdate);

        if (details != null && success) {
            _accountDetailsDao.update(account.getId(), details);
        }

        txn.commit();

        if (success) {
            return _accountDao.findById(account.getId());
        } else {
            throw new CloudRuntimeException("Unable to update account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_USER_DELETE, eventDescription = "deleting User")
    public boolean deleteUser(DeleteUserCmd deleteUserCmd) {
        long id = deleteUserCmd.getId();

        UserVO user = _userDao.findById(id);

        if (user == null) {
            throw new InvalidParameterValueException("The specified user doesn't exist in the system");
        }

        Account account = _accountDao.findById(user.getAccountId());

        // don't allow to delete the user from the account of type Project
        if (account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("The specified user doesn't exist in the system");
        }

        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new InvalidParameterValueException("Account id : " + user.getAccountId() + " is a system account, delete for user associated with this account is not allowed");
        }

        checkAccess(UserContext.current().getCaller(), null, true, account);
        return _userDao.remove(id);
    }

    public class ResourceCountCalculateTask implements Runnable {
        @Override
        public void run() {

        }
    }

    protected class AccountCleanupTask implements Runnable {
        @Override
        public void run() {
            try {
                GlobalLock lock = GlobalLock.getInternLock("AccountCleanup");
                if (lock == null) {
                    s_logger.debug("Couldn't get the global lock");
                    return;
                }

                if (!lock.lock(30)) {
                    s_logger.debug("Couldn't lock the db");
                    return;
                }

                Transaction txn = null;
                try {
                    txn = Transaction.open(Transaction.CLOUD_DB);

                    // Cleanup removed accounts
                    List<AccountVO> removedAccounts = _accountDao.findCleanupsForRemovedAccounts(null);
                    s_logger.info("Found " + removedAccounts.size() + " removed accounts to cleanup");
                    for (AccountVO account : removedAccounts) {
                        s_logger.debug("Cleaning up " + account.getId());
                        try {
                            if (cleanupAccount(account, getSystemUser().getId(), getSystemAccount())) {
                                account.setNeedsCleanup(false);
                                _accountDao.update(account.getId(), account);
                            }
                        } catch (Exception e) {
                            s_logger.error("Skipping due to error on account " + account.getId(), e);
                        }
                    }

                    // cleanup disabled accounts
                    List<AccountVO> disabledAccounts = _accountDao.findCleanupsForDisabledAccounts();
                    s_logger.info("Found " + disabledAccounts.size() + " disabled accounts to cleanup");
                    for (AccountVO account : disabledAccounts) {
                        s_logger.debug("Disabling account " + account.getId());
                        try {
                            if (disableAccount(account.getId())) {
                                account.setNeedsCleanup(false);
                                _accountDao.update(account.getId(), account);
                            }
                        } catch (Exception e) {
                            s_logger.error("Skipping due to error on account " + account.getId(), e);
                        }
                    }

                    // cleanup inactive domains
                    List<? extends Domain> inactiveDomains = _domainMgr.findInactiveDomains();
                    s_logger.info("Found " + inactiveDomains.size() + " inactive domains to cleanup");
                    for (Domain inactiveDomain : inactiveDomains) {
                        long domainId = inactiveDomain.getId();
                        try {
                            List<AccountVO> accountsForCleanupInDomain = _accountDao.findCleanupsForRemovedAccounts(domainId);
                            if (accountsForCleanupInDomain.isEmpty()) {
                                s_logger.debug("Removing inactive domain id=" + domainId);
                                _domainMgr.removeDomain(domainId);
                            } else {
                                s_logger.debug("Can't remove inactive domain id=" + domainId + " as it has accounts that need cleanup");
                            }
                        } catch (Exception e) {
                            s_logger.error("Skipping due to error on domain " + domainId, e);
                        }
                    }

                    // cleanup inactive projects
                    List<ProjectVO> inactiveProjects = _projectDao.listByState(Project.State.Disabled);
                    s_logger.info("Found " + inactiveProjects.size() + " disabled projects to cleanup");
                    for (ProjectVO project : inactiveProjects) {
                        try {
                            Account projectAccount = getAccount(project.getProjectAccountId());
                            if (projectAccount == null) {
                                s_logger.debug("Removing inactive project id=" + project.getId());
                                _projectMgr.deleteProject(UserContext.current().getCaller(), UserContext.current().getCallerUserId(), project);
                            } else {
                                s_logger.debug("Can't remove disabled project " + project + " as it has non removed account id=" + project.getId());
                            }
                        } catch (Exception e) {
                            s_logger.error("Skipping due to error on project " + project, e);
                        }
                    }

                } catch (Exception e) {
                    s_logger.error("Exception ", e);
                } finally {
                    if (txn != null) {
                        txn.close();
                    }

                    lock.unlock();
                }
            } catch (Exception e) {
                s_logger.error("Exception ", e);
            }
        }
    }

    @Override
    public Account finalizeOwner(Account caller, String accountName, Long domainId, Long projectId) {
        // don't default the owner to the system account
        if (caller.getId() == Account.ACCOUNT_ID_SYSTEM && ((accountName == null || domainId == null) && projectId == null)) {
            throw new InvalidParameterValueException("Account and domainId are needed for resource creation");
        }

        // projectId and account/domainId can't be specified together
        if ((accountName != null && domainId != null) && projectId != null) {
            throw new InvalidParameterValueException("ProjectId and account/domainId can't be specified together");
        }

        if (projectId != null) {
            Project project = _projectMgr.getProject(projectId);
            if (project == null) {
                throw new InvalidParameterValueException("Unable to find project by id=" + projectId);
            }

            if (!_projectMgr.canAccessProjectAccount(caller, project.getProjectAccountId())) {
                throw new PermissionDeniedException("Account " + caller + " is unauthorised to use project id=" + projectId);
            }

            return getAccount(project.getProjectAccountId());
        }

        if (isAdmin(caller.getType()) && accountName != null && domainId != null) {
            Domain domain = _domainMgr.getDomain(domainId);
            if (domain == null) {
                throw new InvalidParameterValueException("Unable to find the domain by id=" + domainId);
            }

            Account owner = _accountDao.findActiveAccount(accountName, domainId);
            if (owner == null) {
                throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain " + domainId);
            }
            checkAccess(caller, domain);

            return owner;
        } else if (!isAdmin(caller.getType()) && accountName != null && domainId != null) {
            if (!accountName.equals(caller.getAccountName()) || domainId.longValue() != caller.getDomainId()) {
                throw new PermissionDeniedException("Can't create/list resources for account " + accountName + " in domain " + domainId + ", permission denied");
            } else {
                return caller;
            }
        } else {
            if ((accountName == null && domainId != null) || (accountName != null && domainId == null)) {
                throw new InvalidParameterValueException("AccountName and domainId must be specified together");
            }
            // regular user can't create/list resources for other people
            return caller;
        }
    }

    @Override
    public Account getActiveAccountByName(String accountName, Long domainId) {
        if (accountName == null || domainId == null) {
            throw new InvalidParameterValueException("Both accountName and domainId are required for finding active account in the system");
        } else {
            return _accountDao.findActiveAccount(accountName, domainId);
        }
    }

    @Override
    public Account getActiveAccountById(Long accountId) {
        if (accountId == null) {
            throw new InvalidParameterValueException("AccountId is required by account search");
        } else {
            return _accountDao.findById(accountId);
        }
    }

    @Override
    public Account getAccount(Long accountId) {
        if (accountId == null) {
            throw new InvalidParameterValueException("AccountId is required by account search");
        } else {
            return _accountDao.findByIdIncludingRemoved(accountId);
        }
    }

    @Override
    public User getActiveUser(long userId) {
        return _userDao.findById(userId);
    }

    @Override
    public User getUserIncludingRemoved(long userId) {
        return _userDao.findByIdIncludingRemoved(userId);
    }

    @Override
    public Pair<List<Long>, Long> finalizeAccountDomainForList(Account caller, String accountName, Long domainId, Long projectId) {
        List<Long> permittedAccounts = new ArrayList<Long>();

        if (isAdmin(caller.getType())) {
            if (domainId == null && accountName != null) {
                throw new InvalidParameterValueException("accountName and domainId might be specified together");
            } else if (domainId != null) {
                Domain domain = _domainMgr.getDomain(domainId);
                if (domain == null) {
                    throw new InvalidParameterValueException("Unable to find the domain by id=" + domainId);
                }

                checkAccess(caller, domain);

                if (accountName != null) {
                    Account owner = getActiveAccountByName(accountName, domainId);
                    if (owner == null) {
                        throw new InvalidParameterValueException("Unable to find account with name " + accountName + " in domain id=" + domainId);
                    }

                    permittedAccounts.add(owner.getId());
                }
            }
        } else if (accountName != null && domainId != null) {
            if (!accountName.equals(caller.getAccountName()) || domainId.longValue() != caller.getDomainId()) {
                throw new PermissionDeniedException("Can't list port forwarding rules for account " + accountName + " in domain " + domainId + ", permission denied");
            }
            permittedAccounts.add(getActiveAccountByName(accountName, domainId).getId());
        } else {
            permittedAccounts.add(caller.getAccountId());
        }

        if (domainId == null && caller.getType() == Account.ACCOUNT_TYPE_DOMAIN_ADMIN) {
            domainId = caller.getDomainId();
        }

        // set project information
        if (projectId != null) {
            if (projectId == -1) {
                permittedAccounts.addAll(_projectMgr.listPermittedProjectAccounts(caller.getId()));
            } else {
                permittedAccounts.clear();
                Project project = _projectMgr.getProject(projectId);
                if (project == null) {
                    throw new InvalidParameterValueException("Unable to find project by id " + projectId);
                }
                if (!_projectMgr.canAccessProjectAccount(caller, project.getProjectAccountId())) {
                    throw new InvalidParameterValueException("Account " + caller + " can't access project id=" + projectId);
                }
                permittedAccounts.add(project.getProjectAccountId());
            }
        }

        return new Pair<List<Long>, Long>(permittedAccounts, domainId);
    }

    @Override
    public User getActiveUserByRegistrationToken(String registrationToken) {
        return _userDao.findUserByRegistrationToken(registrationToken);
    }

    @Override
    public void markUserRegistered(long userId) {
        UserVO userForUpdate = _userDao.createForUpdate();
        userForUpdate.setRegistered(true);
        _userDao.update(Long.valueOf(userId), userForUpdate);
    }

    @Override
    @DB
    public Account createAccount(String accountName, short accountType, Long domainId, String networkDomain, Map details) {
        // Validate domain
        Domain domain = _domainMgr.getDomain(domainId);
        if (domain == null) {
            throw new InvalidParameterValueException("The domain " + domainId + " does not exist; unable to create account");
        }

        if (domain.getState().equals(Domain.State.Inactive)) {
            throw new CloudRuntimeException("The account cannot be created as domain " + domain.getName() + " is being deleted");
        }

        if ((domainId != DomainVO.ROOT_DOMAIN) && (accountType == Account.ACCOUNT_TYPE_ADMIN)) {
            throw new InvalidParameterValueException("Invalid account type " + accountType + " given for an account in domain " + domainId + "; unable to create user.");
        }

        // Validate account/user/domain settings
        if (_accountDao.findActiveAccount(accountName, domainId) != null) {
            throw new InvalidParameterValueException("The specified account: " + accountName + " already exists");
        }

        if (networkDomain != null) {
            if (!NetUtils.verifyDomainName(networkDomain)) {
                throw new InvalidParameterValueException(
                        "Invalid network domain. Total length shouldn't exceed 190 chars. Each domain label must be between 1 and 63 characters long, can contain ASCII letters 'a' through 'z', the digits '0' through '9', "
                                + "and the hyphen ('-'); can't start or end with \"-\"");
            }
        }

        // Verify account type
        if ((accountType < Account.ACCOUNT_TYPE_NORMAL) || (accountType > Account.ACCOUNT_TYPE_PROJECT)) {
            throw new InvalidParameterValueException("Invalid account type " + accountType + " given; unable to create user");
        }

        if (accountType == Account.ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN) {
            List<DataCenterVO> dc = _dcDao.findZonesByDomainId(domainId);
            if (dc.isEmpty()) {
                throw new InvalidParameterValueException("The account cannot be created as domain " + domain.getName() + " is not associated with any private Zone");
            }
        }

        // Create the account
        Transaction txn = Transaction.currentTxn();
        txn.start();

        Account account = _accountDao.persist(new AccountVO(accountName, domainId, networkDomain, accountType));

        if (account == null) {
            throw new CloudRuntimeException("Failed to create account name " + accountName + " in domain id=" + domainId);
        }

        Long accountId = account.getId();

        if (details != null) {
            _accountDetailsDao.persist(accountId, details);
        }

        // Create resource count records for the account
        _resourceCountDao.createResourceCounts(accountId, ResourceLimit.ResourceOwnerType.Account);

        // Create default security group
        _networkGroupMgr.createDefaultSecurityGroup(accountId);

        txn.commit();

        return account;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_USER_CREATE, eventDescription = "creating User")
    public UserVO createUser(long accountId, String userName, String password, String firstName, String lastName, String email, String timezone) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating user: " + userName + ", accountId: " + accountId + " timezone:" + timezone);
        }
        UserVO user = _userDao.persist(new UserVO(accountId, userName, password, firstName, lastName, email, timezone));

        return user;
    }

    @Override
    public void logoutUser(Long userId) {
        UserAccount userAcct = _userAccountDao.findById(userId);
        if (userAcct != null) {
            EventUtils.saveEvent(userId, userAcct.getAccountId(), userAcct.getDomainId(), EventTypes.EVENT_USER_LOGOUT, "user has logged out");
        } // else log some kind of error event? This likely means the user doesn't exist, or has been deleted...
    }

    @Override
    public UserAccount getUserAccount(String username, Long domainId) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Retrieiving user: " + username + " in domain " + domainId);
        }

        UserAccount userAccount = _userAccountDao.getUserAccount(username, domainId);
        if (userAccount == null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Unable to find user with name " + username + " in domain " + domainId);
            }
            return null;
        }

        return userAccount;
    }

    @Override
    public UserAccount authenticateUser(String username, String password, Long domainId, Map<String, Object[]> requestParameters) {
        UserAccount user = null;
        if (password != null) {
            user = getUserAccount(username, password, domainId, requestParameters);
        } else {
            String key = _configDao.getValue("security.singlesignon.key");
            if (key == null) {
                // the SSO key is gone, don't authenticate
                return null;
            }

            String singleSignOnTolerance = _configDao.getValue("security.singlesignon.tolerance.millis");
            if (singleSignOnTolerance == null) {
                // the SSO tolerance is gone (how much time before/after system time we'll allow the login request to be
// valid),
                // don't authenticate
                return null;
            }

            long tolerance = Long.parseLong(singleSignOnTolerance);
            String signature = null;
            long timestamp = 0L;
            String unsignedRequest = null;

            // - build a request string with sorted params, make sure it's all lowercase
            // - sign the request, verify the signature is the same
            List<String> parameterNames = new ArrayList<String>();

            for (Object paramNameObj : requestParameters.keySet()) {
                parameterNames.add((String) paramNameObj); // put the name in a list that we'll sort later
            }

            Collections.sort(parameterNames);

            try {
                for (String paramName : parameterNames) {
                    // parameters come as name/value pairs in the form String/String[]
                    String paramValue = ((String[]) requestParameters.get(paramName))[0];

                    if ("signature".equalsIgnoreCase(paramName)) {
                        signature = paramValue;
                    } else {
                        if ("timestamp".equalsIgnoreCase(paramName)) {
                            String timestampStr = paramValue;
                            try {
                                // If the timestamp is in a valid range according to our tolerance, verify the request
                                // signature, otherwise return null to indicate authentication failure
                                timestamp = Long.parseLong(timestampStr);
                                long currentTime = System.currentTimeMillis();
                                if (Math.abs(currentTime - timestamp) > tolerance) {
                                    if (s_logger.isDebugEnabled()) {
                                        s_logger.debug("Expired timestamp passed in to login, current time = " + currentTime + ", timestamp = " + timestamp);
                                    }
                                    return null;
                                }
                            } catch (NumberFormatException nfe) {
                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Invalid timestamp passed in to login: " + timestampStr);
                                }
                                return null;
                            }
                        }

                        if (unsignedRequest == null) {
                            unsignedRequest = paramName + "=" + URLEncoder.encode(paramValue, "UTF-8").replaceAll("\\+", "%20");
                        } else {
                            unsignedRequest = unsignedRequest + "&" + paramName + "=" + URLEncoder.encode(paramValue, "UTF-8").replaceAll("\\+", "%20");
                        }
                    }
                }

                if ((signature == null) || (timestamp == 0L)) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Missing parameters in login request, signature = " + signature + ", timestamp = " + timestamp);
                    }
                    return null;
                }

                unsignedRequest = unsignedRequest.toLowerCase();

                Mac mac = Mac.getInstance("HmacSHA1");
                SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA1");
                mac.init(keySpec);
                mac.update(unsignedRequest.getBytes());
                byte[] encryptedBytes = mac.doFinal();
                String computedSignature = new String(Base64.encodeBase64(encryptedBytes));
                boolean equalSig = signature.equals(computedSignature);
                if (!equalSig) {
                    s_logger.info("User signature: " + signature + " is not equaled to computed signature: " + computedSignature);
                } else {
                    user = _userAccountDao.getUserAccount(username, domainId);
                }
            } catch (Exception ex) {
                s_logger.error("Exception authenticating user", ex);
                return null;
            }
        }

        if (user != null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("User: " + username + " in domain " + domainId + " has successfully logged in");
            }
            EventUtils.saveEvent(user.getId(), user.getAccountId(), user.getDomainId(), EventTypes.EVENT_USER_LOGIN, "user has logged in");
            return user;
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("User: " + username + " in domain " + domainId + " has failed to log in");
            }
            return null;
        }
    }

    private UserAccount getUserAccount(String username, String password, Long domainId, Map<String, Object[]> requestParameters) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Attempting to log in user: " + username + " in domain " + domainId);
        }

        boolean authenticated = false;
        for (Enumeration<UserAuthenticator> en = _userAuthenticators.enumeration(); en.hasMoreElements();) {
            UserAuthenticator authenticator = en.nextElement();
            if (authenticator.authenticate(username, password, domainId, requestParameters)) {
                authenticated = true;
                break;
            }
        }

        if (authenticated) {
            UserAccount userAccount = _userAccountDao.getUserAccount(username, domainId);
            if (userAccount == null) {
                s_logger.warn("Unable to find an authenticated user with username " + username + " in domain " + domainId);
                return null;
            }

            Domain domain = _domainMgr.getDomain(domainId);
            String domainName = null;
            if (domain != null) {
                domainName = domain.getName();
            }

            if (!userAccount.getState().equalsIgnoreCase(Account.State.enabled.toString()) || !userAccount.getAccountState().equalsIgnoreCase(Account.State.enabled.toString())) {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("User " + username + " in domain " + domainName + " is disabled/locked (or account is disabled/locked)");
                }
                throw new CloudAuthenticationException("User " + username + " in domain " + domainName + " is disabled/locked (or account is disabled/locked)");
                // return null;
            }
            return userAccount;
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Unable to authenticate user with username " + username + " in domain " + domainId);
            }
            return null;
        }
    }

    @Override
    public Pair<User, Account> findUserByApiKey(String apiKey) {
        return _accountDao.findUserAccountByApiKey(apiKey);
    }

    @Override
    public String[] createApiKeyAndSecretKey(RegisterCmd cmd) {
        Long userId = cmd.getId();

        if (getUserIncludingRemoved(userId) == null) {
            throw new InvalidParameterValueException("unable to find user for id : " + userId);
        }

        // generate both an api key and a secret key, update the user table with the keys, return the keys to the user
        String[] keys = new String[2];
        keys[0] = createUserApiKey(userId);
        keys[1] = createUserSecretKey(userId);

        return keys;
    }

    private String createUserApiKey(long userId) {
        try {
            UserVO updatedUser = _userDao.createForUpdate();

            String encodedKey = null;
            Pair<User, Account> userAcct = null;
            int retryLimit = 10;
            do {
                // FIXME: what algorithm should we use for API keys?
                KeyGenerator generator = KeyGenerator.getInstance("HmacSHA1");
                SecretKey key = generator.generateKey();
                encodedKey = Base64.encodeBase64URLSafeString(key.getEncoded());
                userAcct = _accountDao.findUserAccountByApiKey(encodedKey);
                retryLimit--;
            } while ((userAcct != null) && (retryLimit >= 0));

            if (userAcct != null) {
                return null;
            }
            updatedUser.setApiKey(encodedKey);
            _userDao.update(userId, updatedUser);
            return encodedKey;
        } catch (NoSuchAlgorithmException ex) {
            s_logger.error("error generating secret key for user id=" + userId, ex);
        }
        return null;
    }

    private String createUserSecretKey(long userId) {
        try {
            UserVO updatedUser = _userDao.createForUpdate();
            String encodedKey = null;
            int retryLimit = 10;
            UserVO userBySecretKey = null;
            do {
                KeyGenerator generator = KeyGenerator.getInstance("HmacSHA1");
                SecretKey key = generator.generateKey();
                encodedKey = Base64.encodeBase64URLSafeString(key.getEncoded());
                userBySecretKey = _userDao.findUserBySecretKey(encodedKey);
                retryLimit--;
            } while ((userBySecretKey != null) && (retryLimit >= 0));

            if (userBySecretKey != null) {
                return null;
            }

            updatedUser.setSecretKey(encodedKey);
            _userDao.update(userId, updatedUser);
            return encodedKey;
        } catch (NoSuchAlgorithmException ex) {
            s_logger.error("error generating secret key for user id=" + userId, ex);
        }
        return null;
    }

    @Override
    public List<AccountVO> searchForAccounts(ListAccountsCmd cmd) {
        Account caller = UserContext.current().getCaller();
        Long domainId = cmd.getDomainId();
        Long accountId = cmd.getId();
        String accountName = cmd.getSearchName();
        boolean isRecursive = cmd.isRecursive();
        boolean listAll = cmd.listAll();
        Boolean listForDomain = false;

        if (accountId != null) {
            Account account = _accountDao.findById(accountId);
            if (account == null || account.getId() == Account.ACCOUNT_ID_SYSTEM) {
                throw new InvalidParameterValueException("Unable to find account by id " + accountId);
            }

            checkAccess(caller, null, true, account);
        }

        if (domainId != null) {
            Domain domain = _domainMgr.getDomain(domainId);
            if (domain == null) {
                throw new InvalidParameterValueException("Domain id=" + domainId + " doesn't exist");
            }

            checkAccess(caller, domain);

            if (accountName != null) {
                Account account = _accountDao.findActiveAccount(accountName, domainId);
                if (account == null || account.getId() == Account.ACCOUNT_ID_SYSTEM) {
                    throw new InvalidParameterValueException("Unable to find account by name " + accountName + " in domain " + domainId);
                }
                checkAccess(caller, null, true, account);
            }
        }

        if (accountId == null) {
            if (isAdmin(caller.getType()) && listAll && domainId == null) {
                listForDomain = true;
                isRecursive = true;
                if (domainId == null) {
                    domainId = caller.getDomainId();
                }
            } else if (domainId != null) {
                listForDomain = true;
            } else {
                accountId = caller.getAccountId();
            }
        }

        Filter searchFilter = new Filter(AccountVO.class, "id", true, cmd.getStartIndex(), cmd.getPageSizeVal());

        Object type = cmd.getAccountType();
        Object state = cmd.getState();
        Object isCleanupRequired = cmd.isCleanupRequired();
        Object keyword = cmd.getKeyword();

        SearchBuilder<AccountVO> sb = _accountDao.createSearchBuilder();
        sb.and("accountName", sb.entity().getAccountName(), SearchCriteria.Op.EQ);
        sb.and("domainId", sb.entity().getDomainId(), SearchCriteria.Op.EQ);
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("type", sb.entity().getType(), SearchCriteria.Op.EQ);
        sb.and("state", sb.entity().getState(), SearchCriteria.Op.EQ);
        sb.and("needsCleanup", sb.entity().getNeedsCleanup(), SearchCriteria.Op.EQ);
        sb.and("typeNEQ", sb.entity().getType(), SearchCriteria.Op.NEQ);
        sb.and("idNEQ", sb.entity().getId(), SearchCriteria.Op.NEQ);

        if (listForDomain && isRecursive) {
            SearchBuilder<DomainVO> domainSearch = _domainDao.createSearchBuilder();
            domainSearch.and("path", domainSearch.entity().getPath(), SearchCriteria.Op.LIKE);
            sb.join("domainSearch", domainSearch, sb.entity().getDomainId(), domainSearch.entity().getId(), JoinBuilder.JoinType.INNER);
        }

        SearchCriteria<AccountVO> sc = sb.create();

        sc.setParameters("idNEQ", Account.ACCOUNT_ID_SYSTEM);

        if (keyword != null) {
            SearchCriteria<AccountVO> ssc = _accountDao.createSearchCriteria();
            ssc.addOr("accountName", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("state", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("accountName", SearchCriteria.Op.SC, ssc);
        }

        if (type != null) {
            sc.setParameters("type", type);
        }

        if (state != null) {
            sc.setParameters("state", state);
        }

        if (isCleanupRequired != null) {
            sc.setParameters("needsCleanup", isCleanupRequired);
        }

        if (accountName != null) {
            sc.setParameters("accountName", accountName);
        }

        // don't return account of type project to the end user
        sc.setParameters("typeNEQ", 5);

        if (accountId != null) {
            sc.setParameters("id", accountId);
        }

        if (listForDomain) {
            DomainVO domain = _domainDao.findById(domainId);
            if (isRecursive) {
                sc.setJoinParameters("domainSearch", "path", domain.getPath() + "%");
            } else {
                sc.setParameters("domainId", domainId);
            }
        }

        return _accountDao.search(sc, searchFilter);
    }

    @Override
    public List<UserAccountVO> searchForUsers(ListUsersCmd cmd) throws PermissionDeniedException {
        Account caller = UserContext.current().getCaller();

        Long domainId = cmd.getDomainId();
        if (domainId != null) {
            Domain domain = _domainDao.findById(domainId);
            if (domain == null) {
                throw new InvalidParameterValueException("Unable to find domain by id=" + domainId);
            }

            checkAccess(caller, domain);
        } else {
            // default domainId to the caller's domain
            domainId = caller.getDomainId();
        }

        Filter searchFilter = new Filter(UserAccountVO.class, "id", true, cmd.getStartIndex(), cmd.getPageSizeVal());

        Long id = cmd.getId();
        Object username = cmd.getUsername();
        Object type = cmd.getAccountType();
        Object accountName = cmd.getAccountName();
        Object state = cmd.getState();
        Object keyword = cmd.getKeyword();

        SearchBuilder<UserAccountVO> sb = _userAccountDao.createSearchBuilder();
        sb.and("username", sb.entity().getUsername(), SearchCriteria.Op.LIKE);
        if (id != null && id == 1) {
            // system user should NOT be searchable
            List<UserAccountVO> emptyList = new ArrayList<UserAccountVO>();
            return emptyList;
        } else if (id != null) {
            sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        } else {
            // this condition is used to exclude system user from the search results
            sb.and("id", sb.entity().getId(), SearchCriteria.Op.NEQ);
        }

        sb.and("type", sb.entity().getType(), SearchCriteria.Op.EQ);
        sb.and("domainId", sb.entity().getDomainId(), SearchCriteria.Op.EQ);
        sb.and("accountName", sb.entity().getAccountName(), SearchCriteria.Op.EQ);
        sb.and("state", sb.entity().getState(), SearchCriteria.Op.EQ);

        if ((accountName == null) && (domainId != null)) {
            SearchBuilder<DomainVO> domainSearch = _domainDao.createSearchBuilder();
            domainSearch.and("path", domainSearch.entity().getPath(), SearchCriteria.Op.LIKE);
            sb.join("domainSearch", domainSearch, sb.entity().getDomainId(), domainSearch.entity().getId(), JoinBuilder.JoinType.INNER);
        }

        SearchCriteria<UserAccountVO> sc = sb.create();
        if (keyword != null) {
            SearchCriteria<UserAccountVO> ssc = _userAccountDao.createSearchCriteria();
            ssc.addOr("username", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("firstname", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("lastname", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("email", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("state", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("accountName", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("type", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("accountState", SearchCriteria.Op.LIKE, "%" + keyword + "%");

            sc.addAnd("username", SearchCriteria.Op.SC, ssc);
        }

        if (username != null) {
            sc.setParameters("username", username);
        }

        if (id != null) {
            sc.setParameters("id", id);
        } else {
            // Don't return system user, search builder with NEQ
            sc.setParameters("id", 1);
        }

        if (type != null) {
            sc.setParameters("type", type);
        }

        if (accountName != null) {
            sc.setParameters("accountName", accountName);
            if (domainId != null) {
                sc.setParameters("domainId", domainId);
            }
        } else if (domainId != null) {
            DomainVO domainVO = _domainDao.findById(domainId);
            sc.setJoinParameters("domainSearch", "path", domainVO.getPath() + "%");
        }

        if (state != null) {
            sc.setParameters("state", state);
        }

        return _userAccountDao.search(sc, searchFilter);
    }

    @Override
    public void buildACLSearchBuilder(SearchBuilder<? extends ControlledEntity> sb,
            Long domainId, boolean isRecursive, List<Long> permittedAccounts, ListProjectResourcesCriteria listProjectResourcesCriteria) {

        if (sb.entity() instanceof IPAddressVO) {
            sb.and("accountIdIN", ((IPAddressVO) sb.entity()).getAllocatedToAccountId(), SearchCriteria.Op.IN);
            sb.and("domainId", ((IPAddressVO) sb.entity()).getAllocatedInDomainId(), SearchCriteria.Op.EQ);
        } else if (sb.entity() instanceof ProjectInvitationVO) {
            sb.and("accountIdIN", ((ProjectInvitationVO) sb.entity()).getForAccountId(), SearchCriteria.Op.IN);
            sb.and("domainId", ((ProjectInvitationVO) sb.entity()).getInDomainId(), SearchCriteria.Op.EQ);
        } else {
            sb.and("accountIdIN", sb.entity().getAccountId(), SearchCriteria.Op.IN);
            sb.and("domainId", sb.entity().getDomainId(), SearchCriteria.Op.EQ);
        }

        if (((permittedAccounts.isEmpty()) && (domainId != null) && isRecursive)) {
            // if accountId isn't specified, we can do a domain match for the admin case if isRecursive is true
            SearchBuilder<DomainVO> domainSearch = _domainDao.createSearchBuilder();
            domainSearch.and("path", domainSearch.entity().getPath(), SearchCriteria.Op.LIKE);

            if (sb.entity() instanceof IPAddressVO) {
                sb.join("domainSearch", domainSearch, ((IPAddressVO) sb.entity()).getAllocatedInDomainId(), domainSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            } else if (sb.entity() instanceof ProjectInvitationVO) {
                sb.join("domainSearch", domainSearch, ((ProjectInvitationVO) sb.entity()).getInDomainId(), domainSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            } else {
                sb.join("domainSearch", domainSearch, sb.entity().getDomainId(), domainSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            }

        }
        if (listProjectResourcesCriteria != null) {
            SearchBuilder<AccountVO> accountSearch = _accountDao.createSearchBuilder();
            if (listProjectResourcesCriteria == Project.ListProjectResourcesCriteria.ListProjectResourcesOnly) {
                accountSearch.and("type", accountSearch.entity().getType(), SearchCriteria.Op.EQ);
            } else if (listProjectResourcesCriteria == Project.ListProjectResourcesCriteria.SkipProjectResources) {
                accountSearch.and("type", accountSearch.entity().getType(), SearchCriteria.Op.NEQ);
            }

            if (sb.entity() instanceof IPAddressVO) {
                sb.join("accountSearch", accountSearch, ((IPAddressVO) sb.entity()).getAllocatedToAccountId(), accountSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            } else if (sb.entity() instanceof ProjectInvitationVO) {
                sb.join("accountSearch", accountSearch, ((ProjectInvitationVO) sb.entity()).getForAccountId(), accountSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            } else {
                sb.join("accountSearch", accountSearch, sb.entity().getAccountId(), accountSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            }
        }
    }

    @Override
    public void buildACLSearchCriteria(SearchCriteria<? extends ControlledEntity> sc,
            Long domainId, boolean isRecursive, List<Long> permittedAccounts, ListProjectResourcesCriteria listProjectResourcesCriteria) {

        if (listProjectResourcesCriteria != null) {
            sc.setJoinParameters("accountSearch", "type", Account.ACCOUNT_TYPE_PROJECT);
        }

        if (!permittedAccounts.isEmpty()) {
            sc.setParameters("accountIdIN", permittedAccounts.toArray());
        } else if (domainId != null) {
            DomainVO domain = _domainDao.findById(domainId);
            if (isRecursive) {
                sc.setJoinParameters("domainSearch", "path", domain.getPath() + "%");
            } else {
                sc.setParameters("domainId", domainId);
            }
        }
    }

    @Override
    public void buildACLSearchParameters(Account caller, Long id, String accountName, Long projectId, List<Long> permittedAccounts, Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject,
            boolean listAll, boolean forProjectInvitation) {
        Long domainId = domainIdRecursiveListProject.first();

        if (domainId != null) {
            Domain domain = _domainDao.findById(domainId);
            if (domain == null) {
                throw new InvalidParameterValueException("Unable to find domain by id " + domainId);
            }
            // check permissions
            checkAccess(caller, domain);
        }

        if (accountName != null) {
            if (projectId != null) {
                throw new InvalidParameterValueException("Account and projectId can't be specified together");
            }

            Account userAccount = null;
            if (domainId != null) {
                userAccount = _accountDao.findActiveAccount(accountName, domainId);
            } else {
                userAccount = _accountDao.findActiveAccount(accountName, caller.getDomainId());
            }

            if (userAccount != null) {
                permittedAccounts.add(userAccount.getId());
            } else {
                throw new InvalidParameterValueException("could not find account " + accountName + " in domain " + domainId);
            }
        }

        // set project information
        if (projectId != null) {
            if (!forProjectInvitation) {
                if (projectId == -1) {
                    if (caller.getType() == Account.ACCOUNT_TYPE_NORMAL) {
                        permittedAccounts.addAll(_projectMgr.listPermittedProjectAccounts(caller.getId()));
                    } else {
                        domainIdRecursiveListProject.third(Project.ListProjectResourcesCriteria.ListProjectResourcesOnly);
                    }
                } else {
                    Project project = _projectMgr.getProject(projectId);
                    if (project == null) {
                        throw new InvalidParameterValueException("Unable to find project by id " + projectId);
                    }
                    if (!_projectMgr.canAccessProjectAccount(caller, project.getProjectAccountId())) {
                        throw new PermissionDeniedException("Account " + caller + " can't access project id=" + projectId);
                    }
                    permittedAccounts.add(project.getProjectAccountId());
                }
            }
        } else {
            if (id == null) {
                domainIdRecursiveListProject.third(Project.ListProjectResourcesCriteria.SkipProjectResources);
            }
            if (permittedAccounts.isEmpty() && domainId == null) {
                if (caller.getType() == Account.ACCOUNT_TYPE_NORMAL) {
                    permittedAccounts.add(caller.getId());
                } else if (!listAll) {
                    if (id == null) {
                        permittedAccounts.add(caller.getId());
                    } else if (caller.getType() != Account.ACCOUNT_TYPE_ADMIN) {
                        domainIdRecursiveListProject.first(caller.getDomainId());
                        domainIdRecursiveListProject.second(true);
                    }
                } else if (domainId == null) {
                    if (caller.getType() == Account.ACCOUNT_TYPE_DOMAIN_ADMIN) {
                        domainIdRecursiveListProject.first(caller.getDomainId());
                        domainIdRecursiveListProject.second(true);
                    }
                }
            }

        }
    }
}
