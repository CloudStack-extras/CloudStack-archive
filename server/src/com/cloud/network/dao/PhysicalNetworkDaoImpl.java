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
package com.cloud.network.dao;

import java.util.List;

import javax.ejb.Local;

import com.cloud.network.Networks.TrafficType;
import com.cloud.network.PhysicalNetworkVO;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;

@Local(value=PhysicalNetworkDao.class) @DB(txn=false)
public class PhysicalNetworkDaoImpl extends GenericDaoBase<PhysicalNetworkVO, Long> implements PhysicalNetworkDao {
    final SearchBuilder<PhysicalNetworkVO> ZoneSearch;
    
    protected final PhysicalNetworkTrafficTypeDaoImpl _trafficTypeDao = ComponentLocator.inject(PhysicalNetworkTrafficTypeDaoImpl.class);
    
    protected PhysicalNetworkDaoImpl() {
        super();
        ZoneSearch = createSearchBuilder();
        ZoneSearch.and("dataCenterId", ZoneSearch.entity().getDataCenterId(), Op.EQ);
        ZoneSearch.done();
    }

    @Override
    public List<PhysicalNetworkVO> listByZone(long zoneId) {
        SearchCriteria<PhysicalNetworkVO> sc = ZoneSearch.create();
        sc.setParameters("dataCenterId", zoneId);
        return search(sc, null);
    }

    @Override
    public List<PhysicalNetworkVO> listByZoneIncludingRemoved(long zoneId) {
        SearchCriteria<PhysicalNetworkVO> sc = ZoneSearch.create();
        sc.setParameters("dataCenterId", zoneId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<PhysicalNetworkVO> listByZoneAndTrafficType(long dataCenterId, TrafficType trafficType) {
        
        SearchBuilder<PhysicalNetworkTrafficTypeVO> trafficTypeSearch = _trafficTypeDao.createSearchBuilder();
        PhysicalNetworkTrafficTypeVO trafficTypeEntity = trafficTypeSearch.entity();
        trafficTypeSearch.and("trafficType", trafficTypeSearch.entity().getTrafficType(), SearchCriteria.Op.EQ);

        SearchBuilder<PhysicalNetworkVO> pnSearch = createSearchBuilder();
        pnSearch.and("dataCenterId", pnSearch.entity().getDataCenterId(), Op.EQ);
        pnSearch.join("trafficTypeSearch", trafficTypeSearch, pnSearch.entity().getId(), trafficTypeEntity.getPhysicalNetworkId(), JoinBuilder.JoinType.INNER);

        SearchCriteria<PhysicalNetworkVO> sc = pnSearch.create();
        sc.setJoinParameters("trafficTypeSearch", "trafficType", trafficType);
        sc.setParameters("dataCenterId", dataCenterId);

        return listBy(sc);  
    }
}
