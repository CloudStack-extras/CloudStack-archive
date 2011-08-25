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

package com.cloud.dc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;

@Local(value=ClusterDetailsDao.class)
public class ClusterDetailsDaoImpl extends GenericDaoBase<ClusterDetailsVO, Long> implements ClusterDetailsDao {
    protected final SearchBuilder<ClusterDetailsVO> ClusterSearch;
    protected final SearchBuilder<ClusterDetailsVO> DetailSearch;
    
    protected ClusterDetailsDaoImpl() {
        ClusterSearch = createSearchBuilder();
        ClusterSearch.and("clusterId", ClusterSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        ClusterSearch.done();
        
        DetailSearch = createSearchBuilder();
        DetailSearch.and("clusterId", DetailSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        DetailSearch.and("name", DetailSearch.entity().getName(), SearchCriteria.Op.EQ);
        DetailSearch.done();
    }

    @Override
    public ClusterDetailsVO findDetail(long clusterId, String name) {
        SearchCriteria<ClusterDetailsVO> sc = DetailSearch.create();
        sc.setParameters("clusterId", clusterId);
        sc.setParameters("name", name);
        
        return findOneIncludingRemovedBy(sc);
    }
    

    @Override
    public Map<String, String> findDetails(long clusterId) {
        SearchCriteria<ClusterDetailsVO> sc = ClusterSearch.create();
        sc.setParameters("clusterId", clusterId);
        
        List<ClusterDetailsVO> results = search(sc, null);
        Map<String, String> details = new HashMap<String, String>(results.size());
        for (ClusterDetailsVO result : results) {
            details.put(result.getName(), result.getValue());
        }
        return details;
    }
    
    @Override
    public void deleteDetails(long clusterId) {
        SearchCriteria sc = ClusterSearch.create();
        sc.setParameters("clusterId", clusterId);
        
        List<ClusterDetailsVO> results = search(sc, null);
        for (ClusterDetailsVO result : results) {
        	remove(result.getId());
        }
    }

    @Override
    public void persist(long clusterId, Map<String, String> details) {
        Transaction txn = Transaction.currentTxn();
        txn.start();
        SearchCriteria<ClusterDetailsVO> sc = ClusterSearch.create();
        sc.setParameters("clusterId", clusterId);
        expunge(sc);
        
        for (Map.Entry<String, String> detail : details.entrySet()) {
            ClusterDetailsVO vo = new ClusterDetailsVO(clusterId, detail.getKey(), detail.getValue());
            persist(vo);
        }
        txn.commit();
    }

    @Override
    public void persist(long clusterId, String name, String value) {
        Transaction txn = Transaction.currentTxn();
        txn.start();
        SearchCriteria<ClusterDetailsVO> sc = DetailSearch.create();
        sc.setParameters("clusterId", clusterId);
        sc.setParameters("name", name);
        expunge(sc);
        
        ClusterDetailsVO vo = new ClusterDetailsVO(clusterId, name, value);
        persist(vo);
        txn.commit();
    }
    
}
