/**
 * Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.api.commands.ListDomainChildrenCmd;
import com.cloud.api.commands.ListDomainsCmd;
import com.cloud.configuration.ResourceLimit;
import com.cloud.configuration.dao.ResourceCountDao;
import com.cloud.domain.Domain;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.projects.ProjectManager;
import com.cloud.projects.ProjectVO;
import com.cloud.projects.dao.ProjectDao;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.component.Inject;
import com.cloud.utils.component.Manager;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;

@Local(value = { DomainManager.class, DomainService.class })
public class DomainManagerImpl implements DomainManager, DomainService, Manager {
    public static final Logger s_logger = Logger.getLogger(DomainManagerImpl.class);

    private String _name;
    @Inject
    private DomainDao _domainDao;
    @Inject
    private AccountManager _accountMgr;
    @Inject
    private ResourceCountDao _resourceCountDao;
    @Inject
    private AccountDao _accountDao;
    @Inject
    private DiskOfferingDao _diskOfferingDao;
    @Inject
    private ServiceOfferingDao _offeringsDao;
    @Inject
    private ProjectDao _projectDao;
    @Inject
    private ProjectManager _projectMgr;

    @Override
    public Domain getDomain(long domainId) {
        return _domainDao.findById(domainId);
    }

    @Override
    public String getName() {
        return _name;
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
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;

        return true;
    }

    @Override
    public Set<Long> getDomainChildrenIds(String parentDomainPath) {
        Set<Long> childDomains = new HashSet<Long>();
        SearchCriteria<DomainVO> sc = _domainDao.createSearchCriteria();
        sc.addAnd("path", SearchCriteria.Op.LIKE, parentDomainPath + "%");

        List<DomainVO> domains = _domainDao.search(sc, null);

        for (DomainVO domain : domains) {
            childDomains.add(domain.getId());
        }

        return childDomains;
    }

    @Override
    public boolean isChildDomain(Long parentId, Long childId) {
        return _domainDao.isChildDomain(parentId, childId);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_DOMAIN_CREATE, eventDescription = "creating Domain")
    public Domain createDomain(String name, Long parentId, String networkDomain) {
        Account caller = UserContext.current().getCaller();

        if (parentId == null) {
            parentId = Long.valueOf(DomainVO.ROOT_DOMAIN);
        }

        DomainVO parentDomain = _domainDao.findById(parentId);
        if (parentDomain == null) {
            throw new InvalidParameterValueException("Unable to create domain " + name + ", parent domain " + parentId + " not found.");
        }

        if (parentDomain.getState().equals(Domain.State.Inactive)) {
            throw new CloudRuntimeException("The domain cannot be created as the parent domain " + parentDomain.getName() + " is being deleted");
        }

        _accountMgr.checkAccess(caller, parentDomain);

        return createDomain(name, parentId, caller.getId(), networkDomain);

    }

    @Override
    @DB
    public Domain createDomain(String name, Long parentId, Long ownerId, String networkDomain) {
        // Verify network domain
        if (networkDomain != null) {
            if (!NetUtils.verifyDomainName(networkDomain)) {
                throw new InvalidParameterValueException(
                        "Invalid network domain. Total length shouldn't exceed 190 chars. Each domain label must be between 1 and 63 characters long, can contain ASCII letters 'a' through 'z', the digits '0' through '9', "
                                + "and the hyphen ('-'); can't start or end with \"-\"");
            }
        }

        SearchCriteria<DomainVO> sc = _domainDao.createSearchCriteria();
        sc.addAnd("name", SearchCriteria.Op.EQ, name);
        sc.addAnd("parent", SearchCriteria.Op.EQ, parentId);
        List<DomainVO> domains = _domainDao.search(sc, null);

        if (!domains.isEmpty()) {
            throw new InvalidParameterValueException("Domain with name " + name + " already exists for the parent id=" + parentId);
        }

        Transaction txn = Transaction.currentTxn();
        txn.start();

        DomainVO domain = _domainDao.create(new DomainVO(name, ownerId, parentId, networkDomain));
        _resourceCountDao.createResourceCounts(domain.getId(), ResourceLimit.ResourceOwnerType.Domain);

        txn.commit();

        return domain;
    }

    @Override
    public DomainVO findDomainByPath(String domainPath) {
        return _domainDao.findDomainByPath(domainPath);
    }

    @Override
    public Set<Long> getDomainParentIds(long domainId) {
        return _domainDao.getDomainParentIds(domainId);
    }

    @Override
    public boolean removeDomain(long domainId) {
        return _domainDao.remove(domainId);
    }

    @Override
    public List<? extends Domain> findInactiveDomains() {
        return _domainDao.findInactiveDomains();
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_DOMAIN_DELETE, eventDescription = "deleting Domain", async = true)
    public boolean deleteDomain(long domainId, Boolean cleanup) {
        Account caller = UserContext.current().getCaller();

        DomainVO domain = _domainDao.findById(domainId);

        if (domain == null) {
            throw new InvalidParameterValueException("Failed to delete domain " + domainId + ", domain not found");
        } else if (domainId == DomainVO.ROOT_DOMAIN) {
            throw new PermissionDeniedException("Can't delete ROOT domain");
        }

        _accountMgr.checkAccess(caller, domain);

        return deleteDomain(domain, cleanup);
    }

    @Override
    public boolean deleteDomain(DomainVO domain, Boolean cleanup) {
        // mark domain as inactive
        s_logger.debug("Marking domain id=" + domain.getId() + " as " + Domain.State.Inactive + " before actually deleting it");
        domain.setState(Domain.State.Inactive);
        _domainDao.update(domain.getId(), domain);
        boolean rollBackState = false;

        try {
            long ownerId = domain.getAccountId();
            if ((cleanup != null) && cleanup.booleanValue()) {
                if (!cleanupDomain(domain.getId(), ownerId)) {
                    s_logger.error("Failed to clean up domain resources and sub domains, delete failed on domain " + domain.getName() + " (id: " + domain.getId() + ").");
                    return false;
                }
            } else {
                List<AccountVO> accountsForCleanup = _accountDao.findCleanupsForRemovedAccounts(domain.getId());
                if (accountsForCleanup.isEmpty()) {
                    if (!_domainDao.remove(domain.getId())) {
                        s_logger.error("Delete failed on domain " + domain.getName() + " (id: " + domain.getId()
                                + "); please make sure all users and sub domains have been removed from the domain before deleting");
                        rollBackState = true;
                        return false;
                    }
                } else {
                    s_logger.warn("Can't delete the domain yet because it has " + accountsForCleanup.size() + "accounts that need a cleanup");
                    rollBackState = true;
                    return false;
                }
            }

            cleanupDomainOfferings(domain.getId());
            return true;
        } catch (Exception ex) {
            s_logger.error("Exception deleting domain with id " + domain.getId(), ex);
            return false;
        } finally {
            //when success is false
            if (rollBackState) {
                s_logger.debug("Changing domain id=" + domain.getId() + " state back to " + Domain.State.Active + " because it can't be removed due to resources referencing to it");
                domain.setState(Domain.State.Active);
                _domainDao.update(domain.getId(), domain);
            }
        }
    }

    private void cleanupDomainOfferings(Long domainId) {
        // delete the service and disk offerings associated with this domain
        List<DiskOfferingVO> diskOfferingsForThisDomain = _diskOfferingDao.listByDomainId(domainId);
        for (DiskOfferingVO diskOffering : diskOfferingsForThisDomain) {
            _diskOfferingDao.remove(diskOffering.getId());
        }

        List<ServiceOfferingVO> serviceOfferingsForThisDomain = _offeringsDao.findServiceOfferingByDomainId(domainId);
        for (ServiceOfferingVO serviceOffering : serviceOfferingsForThisDomain) {
            _offeringsDao.remove(serviceOffering.getId());
        }
    }

    private boolean cleanupDomain(Long domainId, Long ownerId) throws ConcurrentOperationException, ResourceUnavailableException {
        s_logger.debug("Cleaning up domain id=" + domainId);
        boolean success = true;
        {
            DomainVO domainHandle = _domainDao.findById(domainId);
            domainHandle.setState(Domain.State.Inactive);
            _domainDao.update(domainId, domainHandle);

            SearchCriteria<DomainVO> sc = _domainDao.createSearchCriteria();
            sc.addAnd("parent", SearchCriteria.Op.EQ, domainId);
            List<DomainVO> domains = _domainDao.search(sc, null);

            SearchCriteria<DomainVO> sc1 = _domainDao.createSearchCriteria();
            sc1.addAnd("path", SearchCriteria.Op.LIKE, "%" + domainHandle.getPath() + "%");
            List<DomainVO> domainsToBeInactivated = _domainDao.search(sc1, null);

            // update all subdomains to inactive so no accounts/users can be created
            for (DomainVO domain : domainsToBeInactivated) {
                domain.setState(Domain.State.Inactive);
                _domainDao.update(domain.getId(), domain);
            }

            // cleanup sub-domains first
            for (DomainVO domain : domains) {
                success = (success && cleanupDomain(domain.getId(), domain.getAccountId()));
                if (!success) {
                    s_logger.warn("Failed to cleanup domain id=" + domain.getId());
                }
            }
        }

        // delete users which will also delete accounts and release resources for those accounts
        SearchCriteria<AccountVO> sc = _accountDao.createSearchCriteria();
        sc.addAnd("domainId", SearchCriteria.Op.EQ, domainId);
        List<AccountVO> accounts = _accountDao.search(sc, null);
        for (AccountVO account : accounts) {
            if (account.getType() != Account.ACCOUNT_TYPE_PROJECT) {
                s_logger.debug("Deleting account " + account + " as a part of domain id=" + domainId + " cleanup");
                success = (success && _accountMgr.deleteAccount(account, UserContext.current().getCallerUserId(), UserContext.current().getCaller()));
                if (!success) {
                    s_logger.warn("Failed to cleanup account id=" + account.getId() + " as a part of domain cleanup");
                }
            } else {
                ProjectVO project = _projectDao.findByProjectAccountId(account.getId());
                s_logger.debug("Deleting project " + project + " as a part of domain id=" + domainId + " cleanup");
                success = (success && _projectMgr.deleteProject(UserContext.current().getCaller(), UserContext.current().getCallerUserId(), project));
                if (!success) {
                    s_logger.warn("Failed to cleanup project " + project + " as a part of domain cleanup");
                }
            }
        }

        // don't remove the domain if there are accounts required cleanup
        boolean deleteDomainSuccess = true;
        List<AccountVO> accountsForCleanup = _accountDao.findCleanupsForRemovedAccounts(domainId);
        if (accountsForCleanup.isEmpty()) {
            deleteDomainSuccess = _domainDao.remove(domainId);
        } else {
            s_logger.debug("Can't delete the domain yet because it has " + accountsForCleanup.size() + "accounts that need a cleanup");
        }

        return success && deleteDomainSuccess;
    }

    @Override
    public List<DomainVO> searchForDomains(ListDomainsCmd cmd) {
        Account caller = UserContext.current().getCaller();
        Long domainId = cmd.getId();
        boolean listAll = cmd.listAll();
        boolean isRecursive = false;

        if (domainId != null) {
            Domain domain = getDomain(domainId);
            if (domain == null) {
                throw new InvalidParameterValueException("Domain id=" + domainId + " doesn't exist");
            }
            _accountMgr.checkAccess(caller, domain);
        } else {
            domainId = caller.getDomainId();
            if (listAll) {
                isRecursive = true;
            }
        }

        Filter searchFilter = new Filter(DomainVO.class, "id", true, cmd.getStartIndex(), cmd.getPageSizeVal());
        String domainName = cmd.getDomainName();
        Integer level = cmd.getLevel();
        Object keyword = cmd.getKeyword();

        SearchBuilder<DomainVO> sb = _domainDao.createSearchBuilder();
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("name", sb.entity().getName(), SearchCriteria.Op.LIKE);
        sb.and("level", sb.entity().getLevel(), SearchCriteria.Op.EQ);
        sb.and("path", sb.entity().getPath(), SearchCriteria.Op.LIKE);
        sb.and("state", sb.entity().getState(), SearchCriteria.Op.EQ);

        SearchCriteria<DomainVO> sc = sb.create();

        if (keyword != null) {
            SearchCriteria<DomainVO> ssc = _domainDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (domainName != null) {
            sc.setParameters("name", "%" + domainName + "%");
        }

        if (level != null) {
            sc.setParameters("level", level);
        }

        if (domainId != null) {
            if (isRecursive) {
                sc.setParameters("path", getDomain(domainId).getPath() + "%");
            } else {
                sc.setParameters("id", domainId);
            }
        }

        // return only Active domains to the API
        sc.setParameters("state", Domain.State.Active);

        return _domainDao.search(sc, searchFilter);
    }

    @Override
    public List<DomainVO> searchForDomainChildren(ListDomainChildrenCmd cmd) throws PermissionDeniedException {
        Long domainId = cmd.getId();
        String domainName = cmd.getDomainName();
        Boolean isRecursive = cmd.isRecursive();
        Object keyword = cmd.getKeyword();
        boolean listAll = cmd.listAll();
        String path = null;

        Account caller = UserContext.current().getCaller();
        if (domainId != null) {
            _accountMgr.checkAccess(caller, getDomain(domainId));
        } else {
            domainId = caller.getDomainId();
        }

        DomainVO domain = _domainDao.findById(domainId);
        if (domain != null && isRecursive && !listAll) {
            path = domain.getPath();
            domainId = null;
        }

        Filter searchFilter = new Filter(DomainVO.class, "id", true, cmd.getStartIndex(), cmd.getPageSizeVal());
        List<DomainVO> domainList = searchForDomainChildren(searchFilter, domainId, domainName, keyword, path, true);

        return domainList;
    }

    private List<DomainVO> searchForDomainChildren(Filter searchFilter, Long domainId, String domainName, Object keyword, String path, boolean listActiveOnly) {
        SearchCriteria<DomainVO> sc = _domainDao.createSearchCriteria();

        if (keyword != null) {
            SearchCriteria<DomainVO> ssc = _domainDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");

            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (domainId != null) {
            sc.addAnd("parent", SearchCriteria.Op.EQ, domainId);
        }

        if (domainName != null) {
            sc.addAnd("name", SearchCriteria.Op.LIKE, "%" + domainName + "%");
        }

        if (path != null) {
            sc.addAnd("path", SearchCriteria.Op.NEQ, path);
            sc.addAnd("path", SearchCriteria.Op.LIKE, path + "%");
        }

        if (listActiveOnly) {
            sc.addAnd("state", SearchCriteria.Op.EQ, Domain.State.Active);
        }

        return _domainDao.search(sc, searchFilter);
    }

}
