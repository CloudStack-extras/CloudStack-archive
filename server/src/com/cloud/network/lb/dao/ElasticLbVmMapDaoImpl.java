/**
 *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved.
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

package com.cloud.network.lb.dao;

import java.util.List;

import javax.ejb.Local;

import com.cloud.network.ElasticLbVmMapVO;
import com.cloud.network.LoadBalancerVO;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.LoadBalancerDaoImpl;
import com.cloud.network.router.VirtualRouter.Role;
import com.cloud.network.router.VirtualRouter.Role;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.JoinBuilder.JoinType;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.DomainRouterDaoImpl;

@Local(value={ElasticLbVmMapDao.class})
public class ElasticLbVmMapDaoImpl extends GenericDaoBase<ElasticLbVmMapVO, Long> implements ElasticLbVmMapDao {
    protected final DomainRouterDao _routerDao = ComponentLocator.inject(DomainRouterDaoImpl.class);
    protected final LoadBalancerDao _loadbalancerDao = ComponentLocator.inject(LoadBalancerDaoImpl.class);

    
    protected final SearchBuilder<ElasticLbVmMapVO> AllFieldsSearch;
    protected final SearchBuilder<ElasticLbVmMapVO> UnusedVmSearch;
    protected final SearchBuilder<ElasticLbVmMapVO> LoadBalancersForElbVmSearch;


    protected final SearchBuilder<DomainRouterVO> ElbVmSearch;
    
    protected final SearchBuilder<LoadBalancerVO> LoadBalancerSearch;
   
    protected ElasticLbVmMapDaoImpl() {
        AllFieldsSearch  = createSearchBuilder();
        AllFieldsSearch.and("ipId", AllFieldsSearch.entity().getIpAddressId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("lbId", AllFieldsSearch.entity().getLbId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("elbVmId", AllFieldsSearch.entity().getElbVmId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();
   
        ElbVmSearch = _routerDao.createSearchBuilder();
        ElbVmSearch.and("role", ElbVmSearch.entity().getRole(), SearchCriteria.Op.EQ);
        UnusedVmSearch  = createSearchBuilder();
        UnusedVmSearch.and("elbVmId", UnusedVmSearch.entity().getElbVmId(), SearchCriteria.Op.NULL);
        ElbVmSearch.join("UnusedVmSearch", UnusedVmSearch, ElbVmSearch.entity().getId(), UnusedVmSearch.entity().getElbVmId(), JoinType.LEFTOUTER);
        ElbVmSearch.done();
        UnusedVmSearch.done();    
        
        LoadBalancerSearch = _loadbalancerDao.createSearchBuilder();
        LoadBalancersForElbVmSearch = createSearchBuilder();
        LoadBalancersForElbVmSearch.and("elbVmId", LoadBalancersForElbVmSearch.entity().getElbVmId(), SearchCriteria.Op.EQ);
        LoadBalancerSearch.join("LoadBalancersForElbVm", LoadBalancersForElbVmSearch, LoadBalancerSearch.entity().getId(), LoadBalancersForElbVmSearch.entity().getLbId(), JoinType.INNER);
        LoadBalancersForElbVmSearch.done();
        LoadBalancerSearch.done();

    }

    @Override
    public ElasticLbVmMapVO findOneByLbIdAndElbVmId(long lbId, long elbVmId) {
        SearchCriteria<ElasticLbVmMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("lbId", lbId);
        sc.setParameters("elbVmId", elbVmId);
        return findOneBy(sc);
    }

    @Override
    public List<ElasticLbVmMapVO> listByLbId(long lbId) {
        SearchCriteria<ElasticLbVmMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("lbId", lbId);
        return listBy(sc);
    }

    @Override
    public List<ElasticLbVmMapVO> listByElbVmId(long elbVmId) {
        SearchCriteria<ElasticLbVmMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("elbVmId", elbVmId);
        return listBy(sc);
    }

    @Override
    public int deleteLB(long lbId) {
    	SearchCriteria<ElasticLbVmMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("lbId", lbId);
        return super.expunge(sc);
    }

    @Override
    public ElasticLbVmMapVO findOneByIpIdAndElbVmId(long ipId, long elbVmId) {
        SearchCriteria<ElasticLbVmMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("ipId", ipId);
        sc.setParameters("elbVmId", elbVmId);
        return findOneBy(sc);
    }

    @Override
    public ElasticLbVmMapVO findOneByIp(long ipId) {
        SearchCriteria<ElasticLbVmMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("ipId", ipId);
        return findOneBy(sc);
    }

    public List<DomainRouterVO> listUnusedElbVms() {
        SearchCriteria<DomainRouterVO> sc = ElbVmSearch.create();
        sc.setParameters("role", Role.LB);
        return _routerDao.search(sc, null);
    }
    
    @Override
    public List<LoadBalancerVO> listLbsForElbVm(long elbVmId) {
        SearchCriteria<LoadBalancerVO> sc = LoadBalancerSearch.create();
        sc.setJoinParameters("LoadBalancersForElbVm", "elbVmId", elbVmId);
        return _loadbalancerDao.search(sc, null);
    }
	
}
