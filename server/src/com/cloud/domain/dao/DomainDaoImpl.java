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

package com.cloud.domain.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.domain.Domain;
import com.cloud.domain.DomainVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;

@Local(value={DomainDao.class})
public class DomainDaoImpl extends GenericDaoBase<DomainVO, Long> implements DomainDao {
    private static final Logger s_logger = Logger.getLogger(DomainDaoImpl.class);
    
	protected SearchBuilder<DomainVO> DomainNameLikeSearch;
	protected SearchBuilder<DomainVO> ParentDomainNameLikeSearch;
	protected SearchBuilder<DomainVO> DomainPairSearch;
	protected SearchBuilder<DomainVO> ImmediateChildDomainSearch;
	protected SearchBuilder<DomainVO> FindAllChildrenSearch;
	protected SearchBuilder<DomainVO> AllFieldsSearch;
	
	public DomainDaoImpl () {
		DomainNameLikeSearch = createSearchBuilder();
		DomainNameLikeSearch.and("name", DomainNameLikeSearch.entity().getName(), SearchCriteria.Op.LIKE);
		DomainNameLikeSearch.done();
		
		ParentDomainNameLikeSearch = createSearchBuilder();
		ParentDomainNameLikeSearch.and("name", ParentDomainNameLikeSearch.entity().getName(), SearchCriteria.Op.LIKE);
		ParentDomainNameLikeSearch.and("parent", ParentDomainNameLikeSearch.entity().getName(), SearchCriteria.Op.EQ);
		ParentDomainNameLikeSearch.done();

		DomainPairSearch = createSearchBuilder();
		DomainPairSearch.and("id", DomainPairSearch.entity().getId(), SearchCriteria.Op.IN);
		DomainPairSearch.done();

		ImmediateChildDomainSearch = createSearchBuilder();
		ImmediateChildDomainSearch.and("parent", ImmediateChildDomainSearch.entity().getParent(), SearchCriteria.Op.EQ);
		ImmediateChildDomainSearch.done();
		
		FindAllChildrenSearch = createSearchBuilder();
		FindAllChildrenSearch.and("path", FindAllChildrenSearch.entity().getPath(), SearchCriteria.Op.LIKE);
		FindAllChildrenSearch.and("id", FindAllChildrenSearch.entity().getId(), SearchCriteria.Op.NEQ);
		FindAllChildrenSearch.done();
		
		AllFieldsSearch = createSearchBuilder();
		AllFieldsSearch.and("name", AllFieldsSearch.entity().getName(), SearchCriteria.Op.EQ);
		AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), SearchCriteria.Op.EQ);
		AllFieldsSearch.and("owner", AllFieldsSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
		AllFieldsSearch.and("path", AllFieldsSearch.entity().getPath(), SearchCriteria.Op.EQ);
		AllFieldsSearch.and("parent", AllFieldsSearch.entity().getParent(), SearchCriteria.Op.EQ);
		AllFieldsSearch.done();

	}
	
    
    private static String allocPath(DomainVO parentDomain, String name) {
        String parentPath = parentDomain.getPath();
        return parentPath + name + "/";
    }

    @Override
    public synchronized DomainVO create(DomainVO domain) {
        // make sure domain name is valid
        String domainName = domain.getName();
        if (domainName != null) {
            if (domainName.contains("/")) {
                throw new IllegalArgumentException("Domain name contains one or more invalid characters.  Please enter a name without '/' characters.");
            }
        } else {
            throw new IllegalArgumentException("Domain name is null.  Please specify a valid domain name.");
        }

    	long parent = DomainVO.ROOT_DOMAIN;
    	if(domain.getParent() != null && domain.getParent().longValue() >= DomainVO.ROOT_DOMAIN) {
    		parent = domain.getParent().longValue();
    	}
    	
    	DomainVO parentDomain = findById(parent);
    	if(parentDomain == null) {
            s_logger.error("Unable to load parent domain: " + parent);
    		return null;
    	}
    	
        Transaction txn = Transaction.currentTxn();
    	try {
    		txn.start();

    		parentDomain = this.lockRow(parent, true);
            if(parentDomain == null) {
                s_logger.error("Unable to lock parent domain: " + parent);
                return null;
            }
    		
            domain.setPath(allocPath(parentDomain, domain.getName()));
            domain.setLevel(parentDomain.getLevel() + 1);
            
            parentDomain.setNextChildSeq(parentDomain.getNextChildSeq() + 1); // FIXME:  remove sequence number?
            parentDomain.setChildCount(parentDomain.getChildCount() + 1);
            persist(domain);
            update(parentDomain.getId(), parentDomain);
            
    		txn.commit();
    		return domain;
    	} catch(Exception e) {
    		s_logger.error("Unable to create domain due to " + e.getMessage(), e);
    		txn.rollback();
    		return null;
    	} 
    }

    @Override
    @DB
    public boolean remove(Long id) {
        // check for any active users / domains assigned to the given domain id and don't remove the domain if there are any
    	if (id != null && id.longValue() == DomainVO.ROOT_DOMAIN) {
    		s_logger.error("Can not remove domain " + id + " as it is ROOT domain");
    		return false;
    	}
    	
        DomainVO domain = findById(id);
        if(domain == null) {
        	s_logger.error("Unable to remove domain as domain " + id + " no longer exists");
        	return false;
        }
        
        if(domain.getParent() == null) {
        	s_logger.error("Invalid domain " + id + ", orphan?");
        	return false;
        }
    	
        String sql = "SELECT * from account where domain_id = " + id + " and removed is null";
        String sql1 = "SELECT * from domain where parent = " + id + " and removed is null";

        boolean success = false;
        Transaction txn = Transaction.currentTxn();
        try {
        	txn.start();
            DomainVO parentDomain = super.lockRow(domain.getParent(), true);
            if(parentDomain == null) {
                s_logger.error("Unable to load parent domain: " + domain.getParent());
                return false;
            }
        	
            PreparedStatement stmt = txn.prepareAutoCloseStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return false;
            }
            stmt = txn.prepareAutoCloseStatement(sql1);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return false;
            }
            
        	parentDomain.setChildCount(parentDomain.getChildCount() - 1);
        	update(parentDomain.getId(), parentDomain);
            success = super.remove(id);
            txn.commit();
        } catch (SQLException ex) {
            success = false;
            s_logger.error("error removing domain: " + id, ex);
            txn.rollback();
        } 
        return success;
    }

    @Override
    public DomainVO findDomainByPath(String domainPath) {
        SearchCriteria<DomainVO> sc = createSearchCriteria();
        sc.addAnd("path", SearchCriteria.Op.EQ, domainPath);
        return findOneBy(sc);
    }
    
    @Override
    public DomainVO findImmediateChildForParent(Long parentId){
        SearchCriteria<DomainVO> sc = ImmediateChildDomainSearch.create();
    	sc.setParameters("parent", parentId);
    	return (listBy(sc).size() > 0 ? listBy(sc).get(0) : null);//may need to revisit for multiple children case
    }

    @Override
    public List<DomainVO> findImmediateChildrenForParent(Long parentId){
        SearchCriteria<DomainVO> sc = ImmediateChildDomainSearch.create();
    	sc.setParameters("parent", parentId);
    	return listBy(sc);
    }

    @Override
    public List<DomainVO> findAllChildren(String path, Long parentId){
    	SearchCriteria<DomainVO> sc = FindAllChildrenSearch.create();
    	sc.setParameters("path", "%"+path+"%");
    	sc.setParameters("id", parentId);
    	return listBy(sc);
    }
    
    @Override
    public boolean isChildDomain(Long parentId, Long childId) {
        if ((parentId == null) || (childId == null)) {
            return false;
        }

        if (parentId.equals(childId)) {
            return true;
        }

        boolean result = false;
        SearchCriteria<DomainVO> sc = DomainPairSearch.create();
        sc.setParameters("id", parentId, childId);

        List<DomainVO> domainPair = listBy(sc);

        if ((domainPair != null) && (domainPair.size() == 2)) {
            DomainVO d1 = domainPair.get(0);
            DomainVO d2 = domainPair.get(1);

            if (d1.getId() == parentId) {
                result = d2.getPath().startsWith(d1.getPath());
            } else {
                result = d1.getPath().startsWith(d2.getPath());
            }
        }
        return result;
    }
    
    @Override
    public List<DomainVO> findInactiveDomains() {
        SearchCriteria<DomainVO> sc = AllFieldsSearch.create();
        sc.setParameters("state", Domain.State.Inactive);
        return listBy(sc);
    }
    
    @Override
    public Set<Long> getDomainParentIds(long domainId) {
        Set<Long> parentDomains = new HashSet<Long>();
        Domain domain = findById(domainId);
        parentDomains.add(domain.getId());
        
        while (domain.getParent() != null) {
            domain = findById(domain.getParent());
            parentDomains.add(domain.getId());
        }
        
        return parentDomains;
    }
}
