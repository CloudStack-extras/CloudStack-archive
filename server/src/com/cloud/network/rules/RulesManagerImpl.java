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
package com.cloud.network.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.api.commands.ListPortForwardingRulesCmd;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.domain.dao.DomainDao;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventVO;
import com.cloud.event.dao.EventDao;
import com.cloud.event.dao.UsageEventDao;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IPAddressVO;
import com.cloud.network.IpAddress;
import com.cloud.network.Network;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkManager;
import com.cloud.network.dao.FirewallRulesCidrsDao;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.rules.FirewallRule.FirewallRuleType;
import com.cloud.network.rules.FirewallRule.Purpose;
import com.cloud.network.rules.dao.PortForwardingRulesDao;
import com.cloud.offering.NetworkOffering;
import com.cloud.projects.Project.ListProjectResourcesCriteria;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.DomainManager;
import com.cloud.user.UserContext;
import com.cloud.uservm.UserVm;
import com.cloud.utils.Ternary;
import com.cloud.utils.component.Inject;
import com.cloud.utils.component.Manager;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.Ip;
import com.cloud.vm.Nic;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;

@Local(value = { RulesManager.class, RulesService.class })
public class RulesManagerImpl implements RulesManager, RulesService, Manager {
    private static final Logger s_logger = Logger.getLogger(RulesManagerImpl.class);
    String _name;

    @Inject
    PortForwardingRulesDao _portForwardingDao;
    @Inject
    FirewallRulesCidrsDao _firewallCidrsDao;
    @Inject
    FirewallRulesDao _firewallDao;
    @Inject
    IPAddressDao _ipAddressDao;
    @Inject
    UserVmDao _vmDao;
    @Inject
    AccountManager _accountMgr;
    @Inject
    NetworkManager _networkMgr;
    @Inject
    EventDao _eventDao;
    @Inject
    UsageEventDao _usageEventDao;
    @Inject
    DomainDao _domainDao;
    @Inject
    FirewallManager _firewallMgr;
    @Inject
    DomainManager _domainMgr;
    @Inject
    ConfigurationManager _configMgr;
    @Inject
    NicDao _nicDao;

    @Override
    public void checkIpAndUserVm(IpAddress ipAddress, UserVm userVm, Account caller) {
        if (ipAddress == null || ipAddress.getAllocatedTime() == null || ipAddress.getAllocatedToAccountId() == null) {
            throw new InvalidParameterValueException("Unable to create ip forwarding rule on address " + ipAddress + ", invalid IP address specified.");
        }

        if (userVm == null) {
            return;
        }

        if (userVm.getState() == VirtualMachine.State.Destroyed || userVm.getState() == VirtualMachine.State.Expunging) {
            throw new InvalidParameterValueException("Invalid user vm: " + userVm.getId());
        }

        _accountMgr.checkAccess(caller, null, true, ipAddress, userVm);

        // validate that IP address and userVM belong to the same account
        if (ipAddress.getAllocatedToAccountId().longValue() != userVm.getAccountId()) {
            throw new InvalidParameterValueException("Unable to create ip forwarding rule, IP address " + ipAddress + " owner is not the same as owner of virtual machine " + userVm.toString());
        }

        // validate that userVM is in the same availability zone as the IP address
        if (ipAddress.getDataCenterId() != userVm.getDataCenterIdToDeployIn()) {
            throw new InvalidParameterValueException("Unable to create ip forwarding rule, IP address " + ipAddress + " is not in the same availability zone as virtual machine " + userVm.toString());
        }

    }

    @Override
    public void checkRuleAndUserVm(FirewallRule rule, UserVm userVm, Account caller) {
        if (userVm == null || rule == null) {
            return;
        }

        _accountMgr.checkAccess(caller, null, true, rule, userVm);

        if (userVm.getState() == VirtualMachine.State.Destroyed || userVm.getState() == VirtualMachine.State.Expunging) {
            throw new InvalidParameterValueException("Invalid user vm: " + userVm.getId());
        }

        if (rule.getAccountId() != userVm.getAccountId()) {
            throw new InvalidParameterValueException("New rule " + rule + " and vm id=" + userVm.getId() + " belong to different accounts");
        }
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_NET_RULE_ADD, eventDescription = "creating forwarding rule", create = true)
    public PortForwardingRule createPortForwardingRule(PortForwardingRule rule, Long vmId, boolean openFirewall) throws NetworkRuleConflictException {
        UserContext ctx = UserContext.current();
        Account caller = ctx.getCaller();

        Long ipAddrId = rule.getSourceIpAddressId();

        IPAddressVO ipAddress = _ipAddressDao.findById(ipAddrId);

        // Validate ip address
        if (ipAddress == null) {
            throw new InvalidParameterValueException("Unable to create port forwarding rule; ip id=" + ipAddrId + " doesn't exist in the system");
        } else if (ipAddress.isOneToOneNat()) {
            throw new InvalidParameterValueException("Unable to create port forwarding rule; ip id=" + ipAddrId + " has static nat enabled");
        }

        _firewallMgr.validateFirewallRule(caller, ipAddress, rule.getSourcePortStart(), rule.getSourcePortEnd(), rule.getProtocol(), Purpose.PortForwarding, FirewallRuleType.User);

        Long networkId = ipAddress.getAssociatedWithNetworkId();
        Long accountId = ipAddress.getAllocatedToAccountId();
        Long domainId = ipAddress.getAllocatedInDomainId();

        // start port can't be bigger than end port
        if (rule.getDestinationPortStart() > rule.getDestinationPortEnd()) {
            throw new InvalidParameterValueException("Start port can't be bigger than end port");
        }

        // check that the port ranges are of equal size
        if ((rule.getDestinationPortEnd() - rule.getDestinationPortStart()) != (rule.getSourcePortEnd() - rule.getSourcePortStart())) {
            throw new InvalidParameterValueException("Source port and destination port ranges should be of equal sizes.");
        }

        // validate user VM exists
        UserVm vm = _vmDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException("Unable to create port forwarding rule on address " + ipAddress + ", invalid virtual machine id specified (" + vmId + ").");
        } else {
            checkRuleAndUserVm(rule, vm, caller);
        }

        _networkMgr.checkIpForService(ipAddress, Service.PortForwarding);

        // Verify that vm has nic in the network
        Ip dstIp = rule.getDestinationIpAddress();
        Nic guestNic = _networkMgr.getNicInNetwork(vmId, networkId);
        if (guestNic == null || guestNic.getIp4Address() == null) {
            throw new InvalidParameterValueException("Vm doesn't belong to network associated with ipAddress");
        } else {
            dstIp = new Ip(guestNic.getIp4Address());
        }

        Transaction txn = Transaction.currentTxn();
        txn.start();

        PortForwardingRuleVO newRule = new PortForwardingRuleVO(rule.getXid(), rule.getSourceIpAddressId(), rule.getSourcePortStart(), rule.getSourcePortEnd(), dstIp, rule.getDestinationPortStart(),
                rule.getDestinationPortEnd(), rule.getProtocol().toLowerCase(), networkId, accountId, domainId, vmId);
        newRule = _portForwardingDao.persist(newRule);

        // create firewallRule for 0.0.0.0/0 cidr
        if (openFirewall) {
            _firewallMgr.createRuleForAllCidrs(ipAddrId, caller, rule.getSourcePortStart(), rule.getSourcePortEnd(), rule.getProtocol(), null, null, newRule.getId());
        }

        try {
            _firewallMgr.detectRulesConflict(newRule, ipAddress);
            if (!_firewallDao.setStateToAdd(newRule)) {
                throw new CloudRuntimeException("Unable to update the state to add for " + newRule);
            }
            UserContext.current().setEventDetails("Rule Id: " + newRule.getId());
            UsageEventVO usageEvent = new UsageEventVO(EventTypes.EVENT_NET_RULE_ADD, newRule.getAccountId(), ipAddress.getDataCenterId(), newRule.getId(), null);
            _usageEventDao.persist(usageEvent);
            txn.commit();
            return newRule;
        } catch (Exception e) {

            if (newRule != null) {

                txn.start();
                // no need to apply the rule as it wasn't programmed on the backend yet
                _firewallMgr.revokeRelatedFirewallRule(newRule.getId(), false);
                _portForwardingDao.remove(newRule.getId());

                txn.commit();
            }

            if (e instanceof NetworkRuleConflictException) {
                throw (NetworkRuleConflictException) e;
            }
            throw new CloudRuntimeException("Unable to add rule for the ip id=" + ipAddrId, e);
        }
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_NET_RULE_ADD, eventDescription = "creating static nat rule", create = true)
    public StaticNatRule createStaticNatRule(StaticNatRule rule, boolean openFirewall) throws NetworkRuleConflictException {
        Account caller = UserContext.current().getCaller();

        Long ipAddrId = rule.getSourceIpAddressId();

        IPAddressVO ipAddress = _ipAddressDao.findById(ipAddrId);

        // Validate ip address
        if (ipAddress == null) {
            throw new InvalidParameterValueException("Unable to create static nat rule; ip id=" + ipAddrId + " doesn't exist in the system");
        } else if (ipAddress.isSourceNat() || !ipAddress.isOneToOneNat() || ipAddress.getAssociatedWithVmId() == null) {
            throw new NetworkRuleConflictException("Can't do static nat on ip address: " + ipAddress.getAddress());
        }

        _firewallMgr.validateFirewallRule(caller, ipAddress, rule.getSourcePortStart(), rule.getSourcePortEnd(), rule.getProtocol(), Purpose.StaticNat, FirewallRuleType.User);

        Long networkId = ipAddress.getAssociatedWithNetworkId();
        Long accountId = ipAddress.getAllocatedToAccountId();
        Long domainId = ipAddress.getAllocatedInDomainId();

        _networkMgr.checkIpForService(ipAddress, Service.StaticNat);

        Network network = _networkMgr.getNetwork(networkId);
        NetworkOffering off = _configMgr.getNetworkOffering(network.getNetworkOfferingId());
        if (off.getElasticIp()) {
            throw new InvalidParameterValueException("Can't create ip forwarding rules for the network where elasticIP service is enabled");
        }

        String dstIp = _networkMgr.getIpInNetwork(ipAddress.getAssociatedWithVmId(), networkId);

        Transaction txn = Transaction.currentTxn();
        txn.start();

        FirewallRuleVO newRule = new FirewallRuleVO(rule.getXid(), rule.getSourceIpAddressId(), rule.getSourcePortStart(), rule.getSourcePortEnd(), rule.getProtocol().toLowerCase(),
                networkId, accountId, domainId, rule.getPurpose(), null, null, null, null);

        newRule = _firewallDao.persist(newRule);

        // create firewallRule for 0.0.0.0/0 cidr
        if (openFirewall) {
            _firewallMgr.createRuleForAllCidrs(ipAddrId, caller, rule.getSourcePortStart(), rule.getSourcePortEnd(), rule.getProtocol(), null, null, newRule.getId());
        }

        try {
            _firewallMgr.detectRulesConflict(newRule, ipAddress);
            if (!_firewallDao.setStateToAdd(newRule)) {
                throw new CloudRuntimeException("Unable to update the state to add for " + newRule);
            }
            UserContext.current().setEventDetails("Rule Id: " + newRule.getId());
            UsageEventVO usageEvent = new UsageEventVO(EventTypes.EVENT_NET_RULE_ADD, newRule.getAccountId(), 0, newRule.getId(), null);
            _usageEventDao.persist(usageEvent);

            txn.commit();
            StaticNatRule staticNatRule = new StaticNatRuleImpl(newRule, dstIp);

            return staticNatRule;
        } catch (Exception e) {

            if (newRule != null) {
                txn.start();
                // no need to apply the rule as it wasn't programmed on the backend yet
                _firewallMgr.revokeRelatedFirewallRule(newRule.getId(), false);
                _portForwardingDao.remove(newRule.getId());
                txn.commit();
            }

            if (e instanceof NetworkRuleConflictException) {
                throw (NetworkRuleConflictException) e;
            }
            throw new CloudRuntimeException("Unable to add static nat rule for the ip id=" + newRule.getSourceIpAddressId(), e);
        }
    }

    @Override
    public boolean enableStaticNat(long ipId, long vmId) throws NetworkRuleConflictException, ResourceUnavailableException {
        UserContext ctx = UserContext.current();
        Account caller = ctx.getCaller();

        // Verify input parameters
        UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException("Can't enable static nat for the address id=" + ipId + ", invalid virtual machine id specified (" + vmId + ").");
        }

        IPAddressVO ipAddress = _ipAddressDao.findById(ipId);
        if (ipAddress == null) {
            throw new InvalidParameterValueException("Unable to find ip address by id " + ipId);
        }

        // Check permissions
        checkIpAndUserVm(ipAddress, vm, caller);

        // Verify that the ip is associated with the network and static nat service is supported for the network
        Long networkId = ipAddress.getAssociatedWithNetworkId();
        if (networkId == null) {
            throw new InvalidParameterValueException("Unable to enable static nat for the ipAddress id=" + ipId + " as ip is not associated with any network");
        }

        // Check that vm has a nic in the network
        Nic guestNic = _networkMgr.getNicInNetwork(vmId, networkId);
        if (guestNic == null) {
            throw new InvalidParameterValueException("Vm doesn't belong to the network " + networkId);
        }

        Network network = _networkMgr.getNetwork(networkId);
        if (!_networkMgr.areServicesSupportedInNetwork(network.getId(), Service.StaticNat)) {
            throw new InvalidParameterValueException("Unable to create static nat rule; StaticNat service is not supported in network id=" + networkId);
        }

        // Verify ip address parameter
        isIpReadyForStaticNat(vmId, ipAddress, caller, ctx.getCallerUserId());

        _networkMgr.checkIpForService(ipAddress, Service.StaticNat);

        ipAddress.setOneToOneNat(true);
        ipAddress.setAssociatedWithVmId(vmId);

        if (_ipAddressDao.update(ipAddress.getId(), ipAddress)) {
            // enable static nat on the backend
            s_logger.trace("Enabling static nat for ip address " + ipAddress + " and vm id=" + vmId + " on the backend");
            if (applyStaticNatForIp(ipId, false, caller, false)) {
                return true;
            } else {
                ipAddress.setOneToOneNat(false);
                ipAddress.setAssociatedWithVmId(null);
                _ipAddressDao.update(ipAddress.getId(), ipAddress);
                s_logger.warn("Failed to enable static nat rule for ip address " + ipId + " on the backend");
                return false;
            }
        } else {
            s_logger.warn("Failed to update ip address " + ipAddress + " in the DB as a part of enableStaticNat");
            return false;
        }
    }

    protected void isIpReadyForStaticNat(long vmId, IPAddressVO ipAddress, Account caller, long callerUserId) throws NetworkRuleConflictException, ResourceUnavailableException {
        if (ipAddress.isSourceNat()) {
            throw new InvalidParameterValueException("Can't enable static, ip address " + ipAddress + " is a sourceNat ip address");
        }

        if (!ipAddress.isOneToOneNat()) { // Dont allow to enable static nat if PF/LB rules exist for the IP
            List<FirewallRuleVO> portForwardingRules = _firewallDao.listByIpAndPurposeAndNotRevoked(ipAddress.getId(), Purpose.PortForwarding);
            if (portForwardingRules != null && !portForwardingRules.isEmpty()) {
                throw new NetworkRuleConflictException("Failed to enable static nat for the ip address " + ipAddress + " as it already has PortForwarding rules assigned");
            }

            List<FirewallRuleVO> loadBalancingRules = _firewallDao.listByIpAndPurposeAndNotRevoked(ipAddress.getId(), Purpose.LoadBalancing);
            if (loadBalancingRules != null && !loadBalancingRules.isEmpty()) {
                throw new NetworkRuleConflictException("Failed to enable static nat for the ip address " + ipAddress + " as it already has LoadBalancing rules assigned");
            }
        } else if (ipAddress.getAssociatedWithVmId() != null && ipAddress.getAssociatedWithVmId().longValue() != vmId) {
            throw new NetworkRuleConflictException("Failed to enable static for the ip address " + ipAddress + " and vm id=" + vmId + " as it's already assigned to antoher vm");
        }

        IPAddressVO oldIP = _ipAddressDao.findByAssociatedVmId(vmId);

        if (oldIP != null) {
            // If elasticIP functionality is supported in the network, we always have to disable static nat on the old
// ip in order to re-enable it on the new one
            Long networkId = oldIP.getAssociatedWithNetworkId();
            boolean reassignStaticNat = false;
            if (networkId != null) {
                Network guestNetwork = _networkMgr.getNetwork(networkId);
                NetworkOffering offering = _configMgr.getNetworkOffering(guestNetwork.getNetworkOfferingId());
                if (offering.getElasticIp()) {
                    reassignStaticNat = true;
                }
            }

            // If there is public ip address already associated with the vm, throw an exception
            if (!reassignStaticNat) {
                throw new InvalidParameterValueException("Failed to enable static nat for the ip address id=" + ipAddress.getId() + " as vm id=" + vmId + " is already associated with ip id=" + oldIP.getId());
            }
            // unassign old static nat rule
            s_logger.debug("Disassociating static nat for ip " + oldIP);
            if (!disableStaticNat(oldIP.getId(), caller, callerUserId, true)) {
                throw new CloudRuntimeException("Failed to disable old static nat rule for vm id=" + vmId + " and ip " + oldIP);
            }
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_NET_RULE_DELETE, eventDescription = "revoking forwarding rule", async = true)
    public boolean revokePortForwardingRule(long ruleId, boolean apply) {
        UserContext ctx = UserContext.current();
        Account caller = ctx.getCaller();

        PortForwardingRuleVO rule = _portForwardingDao.findById(ruleId);
        if (rule == null) {
            throw new InvalidParameterValueException("Unable to find " + ruleId);
        }

        _accountMgr.checkAccess(caller, null, true, rule);

        if (!revokePortForwardingRuleInternal(ruleId, caller, ctx.getCallerUserId(), apply)) {
            throw new CloudRuntimeException("Failed to delete port forwarding rule");
        }
        return true;
    }

    private boolean revokePortForwardingRuleInternal(long ruleId, Account caller, long userId, boolean apply) {
        PortForwardingRuleVO rule = _portForwardingDao.findById(ruleId);

        _firewallMgr.revokeRule(rule, caller, userId, true);

        boolean success = false;

        if (apply) {
            success = applyPortForwardingRules(rule.getSourceIpAddressId(), true, caller);
        } else {
            success = true;
        }

        return success;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_NET_RULE_DELETE, eventDescription = "revoking forwarding rule", async = true)
    public boolean revokeStaticNatRule(long ruleId, boolean apply) {
        UserContext ctx = UserContext.current();
        Account caller = ctx.getCaller();

        FirewallRuleVO rule = _firewallDao.findById(ruleId);
        if (rule == null) {
            throw new InvalidParameterValueException("Unable to find " + ruleId);
        }

        _accountMgr.checkAccess(caller, null, true, rule);

        if (!revokeStaticNatRuleInternal(ruleId, caller, ctx.getCallerUserId(), apply)) {
            throw new CloudRuntimeException("Failed to revoke forwarding rule");
        }
        return true;
    }

    private boolean revokeStaticNatRuleInternal(long ruleId, Account caller, long userId, boolean apply) {
        FirewallRuleVO rule = _firewallDao.findById(ruleId);

        _firewallMgr.revokeRule(rule, caller, userId, true);

        boolean success = false;

        if (apply) {
            success = applyStaticNatRulesForIp(rule.getSourceIpAddressId(), true, caller, true);
        } else {
            success = true;
        }

        return success;
    }

    @Override
    public boolean revokePortForwardingRulesForVm(long vmId) {
        boolean success = true;
        UserVmVO vm = _vmDao.findByIdIncludingRemoved(vmId);
        if (vm == null) {
            return false;
        }

        List<PortForwardingRuleVO> rules = _portForwardingDao.listByVm(vmId);
        Set<Long> ipsToReprogram = new HashSet<Long>();

        if (rules == null || rules.isEmpty()) {
            s_logger.debug("No port forwarding rules are found for vm id=" + vmId);
            return true;
        }

        for (PortForwardingRuleVO rule : rules) {
            // Mark port forwarding rule as Revoked, but don't revoke it yet (apply=false)
            revokePortForwardingRuleInternal(rule.getId(), _accountMgr.getSystemAccount(), Account.ACCOUNT_ID_SYSTEM, false);
            ipsToReprogram.add(rule.getSourceIpAddressId());
        }

        // apply rules for all ip addresses
        for (Long ipId : ipsToReprogram) {
            s_logger.debug("Applying port forwarding rules for ip address id=" + ipId + " as a part of vm expunge");
            if (!applyPortForwardingRules(ipId, true, _accountMgr.getSystemAccount())) {
                s_logger.warn("Failed to apply port forwarding rules for ip id=" + ipId);
                success = false;
            }
        }

        return success;
    }

    @Override
    public boolean revokeStaticNatRulesForVm(long vmId) {
        boolean success = true;

        UserVmVO vm = _vmDao.findByIdIncludingRemoved(vmId);
        if (vm == null) {
            return false;
        }

        List<FirewallRuleVO> rules = _firewallDao.listStaticNatByVmId(vm.getId());
        Set<Long> ipsToReprogram = new HashSet<Long>();

        if (rules == null || rules.isEmpty()) {
            s_logger.debug("No static nat rules are found for vm id=" + vmId);
            return true;
        }

        for (FirewallRuleVO rule : rules) {
            // mark static nat as Revoked, but don't revoke it yet (apply = false)
            revokeStaticNatRuleInternal(rule.getId(), _accountMgr.getSystemAccount(), Account.ACCOUNT_ID_SYSTEM, false);
            ipsToReprogram.add(rule.getSourceIpAddressId());
        }

        // apply rules for all ip addresses
        for (Long ipId : ipsToReprogram) {
            s_logger.debug("Applying static nat rules for ip address id=" + ipId + " as a part of vm expunge");
            if (!applyStaticNatRulesForIp(ipId, true, _accountMgr.getSystemAccount(), true)) {
                success = false;
                s_logger.warn("Failed to apply static nat rules for ip id=" + ipId);
            }
        }

        return success;
    }

    @Override
    public List<? extends PortForwardingRule> listPortForwardingRulesForApplication(long ipId) {
        return _portForwardingDao.listForApplication(ipId);
    }

    @Override
    public List<? extends PortForwardingRule> listPortForwardingRules(ListPortForwardingRulesCmd cmd) {
        Long ipId = cmd.getIpAddressId();
        Long id = cmd.getId();

        Account caller = UserContext.current().getCaller();
        List<Long> permittedAccounts = new ArrayList<Long>();

        if (ipId != null) {
            IPAddressVO ipAddressVO = _ipAddressDao.findById(ipId);
            if (ipAddressVO == null || !ipAddressVO.readyToUse()) {
                throw new InvalidParameterValueException("Ip address id=" + ipId + " not ready for port forwarding rules yet");
            }
            _accountMgr.checkAccess(caller, null, true, ipAddressVO);
        }

        Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<Long, Boolean, ListProjectResourcesCriteria>(cmd.getDomainId(), cmd.isRecursive(), null);
        _accountMgr.buildACLSearchParameters(caller, id, cmd.getAccountName(), cmd.getProjectId(), permittedAccounts, domainIdRecursiveListProject, cmd.listAll(), false);
        Long domainId = domainIdRecursiveListProject.first();
        Boolean isRecursive = domainIdRecursiveListProject.second();
        ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();

        Filter filter = new Filter(PortForwardingRuleVO.class, "id", false, cmd.getStartIndex(), cmd.getPageSizeVal());
        SearchBuilder<PortForwardingRuleVO> sb = _portForwardingDao.createSearchBuilder();
        _accountMgr.buildACLSearchBuilder(sb, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);

        sb.and("id", sb.entity().getId(), Op.EQ);
        sb.and("ip", sb.entity().getSourceIpAddressId(), Op.EQ);
        sb.and("purpose", sb.entity().getPurpose(), Op.EQ);

        SearchCriteria<PortForwardingRuleVO> sc = sb.create();
        _accountMgr.buildACLSearchCriteria(sc, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (ipId != null) {
            sc.setParameters("ip", ipId);
        }

        sc.setParameters("purpose", Purpose.PortForwarding);

        return _portForwardingDao.search(sc, filter);
    }

    @Override
    public List<String> getSourceCidrs(long ruleId) {
        return _firewallCidrsDao.getSourceCidrs(ruleId);
    }

    @Override
    public boolean applyPortForwardingRules(long ipId, boolean continueOnError, Account caller) {
        List<PortForwardingRuleVO> rules = _portForwardingDao.listForApplication(ipId);

        if (rules.size() == 0) {
            s_logger.debug("There are no port forwarding rules to apply for ip id=" + ipId);
            return true;
        }

        if (caller != null) {
            _accountMgr.checkAccess(caller, null, true, rules.toArray(new PortForwardingRuleVO[rules.size()]));
        }

        try {
            if (!_firewallMgr.applyRules(rules, continueOnError, true)) {
                return false;
            }
        } catch (ResourceUnavailableException ex) {
            s_logger.warn("Failed to apply port forwarding rules for ip due to ", ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean applyStaticNatRulesForIp(long sourceIpId, boolean continueOnError, Account caller, boolean forRevoke) {
        List<? extends FirewallRule> rules = _firewallDao.listByIpAndPurpose(sourceIpId, Purpose.StaticNat);
        List<StaticNatRule> staticNatRules = new ArrayList<StaticNatRule>();

        if (rules.size() == 0) {
            s_logger.debug("There are no static nat rules to apply for ip id=" + sourceIpId);
            return true;
        }

        for (FirewallRule rule : rules) {
            staticNatRules.add(buildStaticNatRule(rule, forRevoke));
        }

        if (caller != null) {
            _accountMgr.checkAccess(caller, null, true, staticNatRules.toArray(new StaticNatRule[staticNatRules.size()]));
        }

        try {
            if (!_firewallMgr.applyRules(staticNatRules, continueOnError, true)) {
                return false;
            }
        } catch (ResourceUnavailableException ex) {
            s_logger.warn("Failed to apply static nat rules for ip due to ", ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean applyPortForwardingRulesForNetwork(long networkId, boolean continueOnError, Account caller) {
        List<PortForwardingRuleVO> rules = listByNetworkId(networkId);
        if (rules.size() == 0) {
            s_logger.debug("There are no port forwarding rules to apply for network id=" + networkId);
            return true;
        }

        if (caller != null) {
            _accountMgr.checkAccess(caller, null, true, rules.toArray(new PortForwardingRuleVO[rules.size()]));
        }

        try {
            if (!_firewallMgr.applyRules(rules, continueOnError, true)) {
                return false;
            }
        } catch (ResourceUnavailableException ex) {
            s_logger.warn("Failed to apply port forwarding rules for network due to ", ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean applyStaticNatRulesForNetwork(long networkId, boolean continueOnError, Account caller) {
        List<FirewallRuleVO> rules = _firewallDao.listByNetworkAndPurpose(networkId, Purpose.StaticNat);
        List<StaticNatRule> staticNatRules = new ArrayList<StaticNatRule>();

        if (rules.size() == 0) {
            s_logger.debug("There are no static nat rules to apply for network id=" + networkId);
            return true;
        }

        if (caller != null) {
            _accountMgr.checkAccess(caller, null, true, rules.toArray(new FirewallRule[rules.size()]));
        }

        for (FirewallRuleVO rule : rules) {
            staticNatRules.add(buildStaticNatRule(rule, false));
        }

        try {
            if (!_firewallMgr.applyRules(staticNatRules, continueOnError, true)) {
                return false;
            }
        } catch (ResourceUnavailableException ex) {
            s_logger.warn("Failed to apply static nat rules for network due to ", ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean applyStaticNatsForNetwork(long networkId, boolean continueOnError, Account caller) {
        List<IPAddressVO> ips = _ipAddressDao.listStaticNatPublicIps(networkId);
        if (ips.isEmpty()) {
            s_logger.debug("There are no static nat to apply for network id=" + networkId);
            return true;
        }

        if (caller != null) {
            _accountMgr.checkAccess(caller, null, true, ips.toArray(new IPAddressVO[ips.size()]));
        }

        List<StaticNat> staticNats = new ArrayList<StaticNat>();
        for (IPAddressVO ip : ips) {
            // Get nic IP4 address
            String dstIp = _networkMgr.getIpInNetwork(ip.getAssociatedWithVmId(), networkId);
            StaticNatImpl staticNat = new StaticNatImpl(ip.getAllocatedToAccountId(), ip.getAllocatedInDomainId(), networkId, ip.getId(), dstIp, false);
            staticNats.add(staticNat);
        }

        try {
            if (!_networkMgr.applyStaticNats(staticNats, continueOnError)) {
                return false;
            }
        } catch (ResourceUnavailableException ex) {
            s_logger.warn("Failed to create static nat for network due to ", ex);
            return false;
        }

        return true;
    }

    @Override
    public List<? extends FirewallRule> searchStaticNatRules(Long ipId, Long id, Long vmId, Long start, Long size, String accountName, Long domainId, Long projectId, boolean isRecursive, boolean listAll) {
        Account caller = UserContext.current().getCaller();
        List<Long> permittedAccounts = new ArrayList<Long>();

        if (ipId != null) {
            IPAddressVO ipAddressVO = _ipAddressDao.findById(ipId);
            if (ipAddressVO == null || !ipAddressVO.readyToUse()) {
                throw new InvalidParameterValueException("Ip address id=" + ipId + " not ready for port forwarding rules yet");
            }
            _accountMgr.checkAccess(caller, null, true, ipAddressVO);
        }

        Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<Long, Boolean, ListProjectResourcesCriteria>(domainId, isRecursive, null);
        _accountMgr.buildACLSearchParameters(caller, id, accountName, projectId, permittedAccounts, domainIdRecursiveListProject, listAll, false);
        domainId = domainIdRecursiveListProject.first();
        isRecursive = domainIdRecursiveListProject.second();
        ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();

        Filter filter = new Filter(PortForwardingRuleVO.class, "id", false, start, size);
        SearchBuilder<FirewallRuleVO> sb = _firewallDao.createSearchBuilder();
        _accountMgr.buildACLSearchBuilder(sb, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);

        sb.and("ip", sb.entity().getSourceIpAddressId(), Op.EQ);
        sb.and("purpose", sb.entity().getPurpose(), Op.EQ);
        sb.and("id", sb.entity().getId(), Op.EQ);

        if (vmId != null) {
            SearchBuilder<IPAddressVO> ipSearch = _ipAddressDao.createSearchBuilder();
            ipSearch.and("associatedWithVmId", ipSearch.entity().getAssociatedWithVmId(), Op.EQ);
            sb.join("ipSearch", ipSearch, sb.entity().getSourceIpAddressId(), ipSearch.entity().getId(), JoinBuilder.JoinType.INNER);
        }

        SearchCriteria<FirewallRuleVO> sc = sb.create();
        _accountMgr.buildACLSearchCriteria(sc, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);
        sc.setParameters("purpose", Purpose.StaticNat);

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (ipId != null) {
            sc.setParameters("ip", ipId);
        }

        if (vmId != null) {
            sc.setJoinParameters("ipSearch", "associatedWithVmId", vmId);
        }

        return _firewallDao.search(sc, filter);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_NET_RULE_ADD, eventDescription = "applying port forwarding rule", async = true)
    public boolean applyPortForwardingRules(long ipId, Account caller) throws ResourceUnavailableException {
        if (!applyPortForwardingRules(ipId, false, caller)) {
            throw new CloudRuntimeException("Failed to apply port forwarding rule");
        }
        return true;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_NET_RULE_ADD, eventDescription = "applying static nat rule", async = true)
    public boolean applyStaticNatRules(long ipId, Account caller) throws ResourceUnavailableException {
        if (!applyStaticNatRulesForIp(ipId, false, caller, false)) {
            throw new CloudRuntimeException("Failed to apply static nat rule");
        }
        return true;
    }

    @Override
    public boolean revokeAllPFAndStaticNatRulesForIp(long ipId, long userId, Account caller) throws ResourceUnavailableException {
        List<FirewallRule> rules = new ArrayList<FirewallRule>();

        List<PortForwardingRuleVO> pfRules = _portForwardingDao.listByIpAndNotRevoked(ipId);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Releasing " + pfRules.size() + " port forwarding rules for ip id=" + ipId);
        }

        for (PortForwardingRuleVO rule : pfRules) {
            // Mark all PF rules as Revoke, but don't revoke them yet
            revokePortForwardingRuleInternal(rule.getId(), caller, userId, false);
        }

        List<FirewallRuleVO> staticNatRules = _firewallDao.listByIpAndPurposeAndNotRevoked(ipId, Purpose.StaticNat);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Releasing " + staticNatRules.size() + " static nat rules for ip id=" + ipId);
        }

        for (FirewallRuleVO rule : staticNatRules) {
            // Mark all static nat rules as Revoke, but don't revoke them yet
            revokeStaticNatRuleInternal(rule.getId(), caller, userId, false);
        }

        boolean success = true;

        // revoke all port forwarding rules
        success = success && applyPortForwardingRules(ipId, true, caller);

        // revoke all all static nat rules
        success = success && applyStaticNatRulesForIp(ipId, true, caller, true);

        // revoke static nat for the ip address
        success = success && applyStaticNatForIp(ipId, false, caller, true);

        // Now we check again in case more rules have been inserted.
        rules.addAll(_portForwardingDao.listByIpAndNotRevoked(ipId));
        rules.addAll(_firewallDao.listByIpAndPurposeAndNotRevoked(ipId, Purpose.StaticNat));

        if (s_logger.isDebugEnabled() && success) {
            s_logger.debug("Successfully released rules for ip id=" + ipId + " and # of rules now = " + rules.size());
        }

        return (rules.size() == 0 && success);
    }

    @Override
    public boolean revokeAllPFStaticNatRulesForNetwork(long networkId, long userId, Account caller) throws ResourceUnavailableException {
        List<FirewallRule> rules = new ArrayList<FirewallRule>();

        List<PortForwardingRuleVO> pfRules = _portForwardingDao.listByNetwork(networkId);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Releasing " + pfRules.size() + " port forwarding rules for network id=" + networkId);
        }

        List<FirewallRuleVO> staticNatRules = _firewallDao.listByNetworkAndPurpose(networkId, Purpose.StaticNat);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Releasing " + staticNatRules.size() + " static nat rules for network id=" + networkId);
        }

        // Mark all pf rules (Active and non-Active) to be revoked, but don't revoke it yet - pass apply=false
        for (PortForwardingRuleVO rule : pfRules) {
            revokePortForwardingRuleInternal(rule.getId(), caller, userId, false);
        }

        // Mark all static nat rules (Active and non-Active) to be revoked, but don't revoke it yet - pass apply=false
        for (FirewallRuleVO rule : staticNatRules) {
            revokeStaticNatRuleInternal(rule.getId(), caller, userId, false);
        }

        boolean success = true;
        // revoke all PF rules for the network
        success = success && applyPortForwardingRulesForNetwork(networkId, true, caller);

        // revoke all all static nat rules for the network
        success = success && applyStaticNatRulesForNetwork(networkId, true, caller);

        // Now we check again in case more rules have been inserted.
        rules.addAll(_portForwardingDao.listByNetworkAndNotRevoked(networkId));
        rules.addAll(_firewallDao.listByNetworkAndPurposeAndNotRevoked(networkId, Purpose.StaticNat));

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Successfully released rules for network id=" + networkId + " and # of rules now = " + rules.size());
        }

        return success && rules.size() == 0;
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _name = name;
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public List<? extends FirewallRule> listFirewallRulesByIp(long ipId) {
        return null;
    }

    @Override
    public boolean releasePorts(long ipId, String protocol, FirewallRule.Purpose purpose, int... ports) {
        return _firewallDao.releasePorts(ipId, protocol, purpose, ports);
    }

    @Override
    @DB
    public FirewallRuleVO[] reservePorts(IpAddress ip, String protocol, FirewallRule.Purpose purpose, boolean openFirewall, Account caller, int... ports) throws NetworkRuleConflictException {
        FirewallRuleVO[] rules = new FirewallRuleVO[ports.length];

        Transaction txn = Transaction.currentTxn();
        txn.start();
        for (int i = 0; i < ports.length; i++) {

            rules[i] = new FirewallRuleVO(null, ip.getId(), ports[i], protocol, ip.getAssociatedWithNetworkId(), ip.getAllocatedToAccountId(), ip.getAllocatedInDomainId(), purpose, null, null, null, null);
            rules[i] = _firewallDao.persist(rules[i]);

            if (openFirewall) {
                _firewallMgr.createRuleForAllCidrs(ip.getId(), caller, ports[i], ports[i], protocol, null, null, rules[i].getId());
            }
        }
        txn.commit();

        boolean success = false;
        try {
            for (FirewallRuleVO newRule : rules) {
                _firewallMgr.detectRulesConflict(newRule, ip);
            }
            success = true;
            return rules;
        } finally {
            if (!success) {
                txn.start();

                for (FirewallRuleVO newRule : rules) {
                    _portForwardingDao.remove(newRule.getId());
                }
                txn.commit();
            }
        }
    }

    @Override
    public List<? extends PortForwardingRule> gatherPortForwardingRulesForApplication(List<? extends IpAddress> addrs) {
        List<PortForwardingRuleVO> allRules = new ArrayList<PortForwardingRuleVO>();

        for (IpAddress addr : addrs) {
            if (!addr.readyToUse()) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Skipping " + addr + " because it is not ready for propation yet.");
                }
                continue;
            }
            allRules.addAll(_portForwardingDao.listForApplication(addr.getId()));
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Found " + allRules.size() + " rules to apply for the addresses.");
        }

        return allRules;
    }

    @Override
    public List<PortForwardingRuleVO> listByNetworkId(long networkId) {
        return _portForwardingDao.listByNetwork(networkId);
    }

    @Override
    public boolean disableStaticNat(long ipId) throws ResourceUnavailableException, NetworkRuleConflictException, InsufficientAddressCapacityException {
        UserContext ctx = UserContext.current();
        Account caller = ctx.getCaller();
        IPAddressVO ipAddress = _ipAddressDao.findById(ipId);
        checkIpAndUserVm(ipAddress, null, caller);

        if (ipAddress.getSystem()) {
            throw new InvalidParameterValueException("Can't disable static nat for system IP address " + ipAddress);
        }

        Long vmId = ipAddress.getAssociatedWithVmId();
        if (vmId == null) {
            throw new InvalidParameterValueException("IP address " + ipAddress + " is not associated with any vm Id");
        }

        // if network has elastic IP functionality supported, we first have to disable static nat on old ip in order to
// re-enable it on the new one
        // enable static nat takes care of that
        Network guestNetwork = _networkMgr.getNetwork(ipAddress.getAssociatedWithNetworkId());
        NetworkOffering offering = _configMgr.getNetworkOffering(guestNetwork.getNetworkOfferingId());
        if (offering.getElasticIp()) {
            getSystemIpAndEnableStaticNatForVm(_vmDao.findById(vmId), true);
            return true;
        } else {
            return disableStaticNat(ipId, caller, ctx.getCallerUserId(), false);
        }
    }

    @Override
    @DB
    public boolean disableStaticNat(long ipId, Account caller, long callerUserId, boolean releaseIpIfElastic) throws ResourceUnavailableException {
        boolean success = true;

        IPAddressVO ipAddress = _ipAddressDao.findById(ipId);
        checkIpAndUserVm(ipAddress, null, caller);

        if (!ipAddress.isOneToOneNat()) {
            throw new InvalidParameterValueException("One to one nat is not enabled for the ip id=" + ipId);
        }

        // Revoke all firewall rules for the ip
        try {
            s_logger.debug("Revoking all " + Purpose.Firewall + "rules as a part of disabling static nat for public IP id=" + ipId);
            if (!_firewallMgr.revokeFirewallRulesForIp(ipId, callerUserId, caller)) {
                s_logger.warn("Unable to revoke all the firewall rules for ip id=" + ipId + " as a part of disable statis nat");
                success = false;
            }
        } catch (ResourceUnavailableException e) {
            s_logger.warn("Unable to revoke all firewall rules for ip id=" + ipId + " as a part of ip release", e);
            success = false;
        }

        if (!revokeAllPFAndStaticNatRulesForIp(ipId, callerUserId, caller)) {
            s_logger.warn("Unable to revoke all static nat rules for ip " + ipAddress);
            success = false;
        }

        if (success) {
            boolean isIpSystem = ipAddress.getSystem();

            ipAddress.setOneToOneNat(false);
            ipAddress.setAssociatedWithVmId(null);
            if (isIpSystem && !releaseIpIfElastic) {
                ipAddress.setSystem(false);
            }
            _ipAddressDao.update(ipAddress.getId(), ipAddress);

            if (isIpSystem && releaseIpIfElastic && !_networkMgr.handleSystemIpRelease(ipAddress)) {
                s_logger.warn("Failed to release system ip address " + ipAddress);
                success = false;
            }

            return true;
        } else {
            s_logger.warn("Failed to disable one to one nat for the ip address id" + ipId);
            return false;
        }
    }

    @Override
    public PortForwardingRule getPortForwardigRule(long ruleId) {
        return _portForwardingDao.findById(ruleId);
    }

    @Override
    public FirewallRule getFirewallRule(long ruleId) {
        return _firewallDao.findById(ruleId);
    }

    @Override
    public StaticNatRule buildStaticNatRule(FirewallRule rule, boolean forRevoke) {
        IpAddress ip = _ipAddressDao.findById(rule.getSourceIpAddressId());
        FirewallRuleVO ruleVO = _firewallDao.findById(rule.getId());

        if (ip == null || !ip.isOneToOneNat() || ip.getAssociatedWithVmId() == null) {
            throw new InvalidParameterValueException("Source ip address of the rule id=" + rule.getId() + " is not static nat enabled");
        }
        
        String dstIp;
        if (forRevoke) {
            dstIp = _networkMgr.getIpInNetworkIncludingRemoved(ip.getAssociatedWithVmId(), rule.getNetworkId());
        } else {
            dstIp = _networkMgr.getIpInNetwork(ip.getAssociatedWithVmId(), rule.getNetworkId());
        }

        return new StaticNatRuleImpl(ruleVO, dstIp);
    }

    @Override
    public boolean applyStaticNatForIp(long sourceIpId, boolean continueOnError, Account caller, boolean forRevoke) {

        List<StaticNat> staticNats = new ArrayList<StaticNat>();
        IpAddress sourceIp = _ipAddressDao.findById(sourceIpId);

        if (!sourceIp.isOneToOneNat()) {
            s_logger.debug("Source ip id=" + sourceIpId + " is not one to one nat");
            return true;
        }

        Long networkId = sourceIp.getAssociatedWithNetworkId();
        if (networkId == null) {
            throw new CloudRuntimeException("Ip address is not associated with any network");
        }

        UserVmVO vm = _vmDao.findById(sourceIp.getAssociatedWithVmId());
        Network network = _networkMgr.getNetwork(networkId);
        if (network == null) {
            throw new CloudRuntimeException("Unable to find ip address to map to in vm id=" + vm.getId());
        }

        if (caller != null) {
            _accountMgr.checkAccess(caller, null, true, sourceIp);
        }

        // create new static nat rule
        // Get nic IP4 address

        String dstIp;
        if (forRevoke) {
            dstIp = _networkMgr.getIpInNetworkIncludingRemoved(sourceIp.getAssociatedWithVmId(), networkId);
        } else {
            dstIp = _networkMgr.getIpInNetwork(sourceIp.getAssociatedWithVmId(), networkId);
        }

        StaticNatImpl staticNat = new StaticNatImpl(sourceIp.getAllocatedToAccountId(), sourceIp.getAllocatedInDomainId(), networkId, sourceIpId, dstIp, forRevoke);
        staticNats.add(staticNat);

        try {
            if (!_networkMgr.applyStaticNats(staticNats, continueOnError)) {
                return false;
            }
        } catch (ResourceUnavailableException ex) {
            s_logger.warn("Failed to create static nat rule due to ", ex);
            return false;
        }

        return true;
    }

    @Override
    public void getSystemIpAndEnableStaticNatForVm(UserVm vm, boolean getNewIp) throws InsufficientAddressCapacityException {
        boolean success = true;

        // enable static nat if eIp capability is supported
        List<? extends Nic> nics = _nicDao.listByVmId(vm.getId());
        for (Nic nic : nics) {
            Network guestNetwork = _networkMgr.getNetwork(nic.getNetworkId());
            NetworkOffering offering = _configMgr.getNetworkOffering(guestNetwork.getNetworkOfferingId());
            if (offering.getElasticIp()) {

                // check if there is already static nat enabled
                if (_ipAddressDao.findByAssociatedVmId(vm.getId()) != null && !getNewIp) {
                    s_logger.debug("Vm " + vm + " already has ip associated with it in guest network " + guestNetwork);
                    continue;
                }

                s_logger.debug("Allocating system ip and enabling static nat for it for the vm " + vm + " in guest network " + guestNetwork);
                IpAddress ip = _networkMgr.assignSystemIp(guestNetwork.getId(), _accountMgr.getAccount(vm.getAccountId()), false, true);
                if (ip == null) {
                    throw new CloudRuntimeException("Failed to allocate system ip for vm " + vm + " in guest network " + guestNetwork);
                }

                s_logger.debug("Allocated system ip " + ip + ", now enabling static nat on it for vm " + vm);

                try {
                    success = enableStaticNat(ip.getId(), vm.getId());
                } catch (NetworkRuleConflictException ex) {
                    s_logger.warn("Failed to enable static nat as a part of enabling elasticIp and staticNat for vm " + vm + " in guest network " + guestNetwork + " due to exception ", ex);
                    success = false;
                } catch (ResourceUnavailableException ex) {
                    s_logger.warn("Failed to enable static nat as a part of enabling elasticIp and staticNat for vm " + vm + " in guest network " + guestNetwork + " due to exception ", ex);
                    success = false;
                }

                if (!success) {
                    s_logger.warn("Failed to enable static nat on system ip " + ip + " for the vm " + vm + ", releasing the ip...");
                    _networkMgr.handleSystemIpRelease(ip);
                    throw new CloudRuntimeException("Failed to enable static nat on system ip for the vm " + vm);
                } else {
                    s_logger.warn("Succesfully enabled static nat on system ip " + ip + " for the vm " + vm);
                }
            }
        }
    }

}
