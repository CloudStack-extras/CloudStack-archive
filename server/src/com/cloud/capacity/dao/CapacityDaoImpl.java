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

package com.cloud.capacity.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.capacity.CapacityVO;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;

@Local(value = { CapacityDao.class })
public class CapacityDaoImpl extends GenericDaoBase<CapacityVO, Long> implements CapacityDao {
    private static final Logger s_logger = Logger.getLogger(CapacityDaoImpl.class);

    private static final String ADD_ALLOCATED_SQL = "UPDATE `cloud`.`op_host_capacity` SET used_capacity = used_capacity + ? WHERE host_id = ? AND capacity_type = ?";
    private static final String SUBTRACT_ALLOCATED_SQL = "UPDATE `cloud`.`op_host_capacity` SET used_capacity = used_capacity - ? WHERE host_id = ? AND capacity_type = ?";

    private static final String LIST_CLUSTERSINZONE_BY_HOST_CAPACITIES_PART1 = "SELECT DISTINCT capacity.cluster_id  FROM `cloud`.`op_host_capacity` capacity INNER JOIN `cloud`.`cluster` cluster on (cluster.id = capacity.cluster_id AND cluster.removed is NULL) WHERE ";
    private static final String LIST_CLUSTERSINZONE_BY_HOST_CAPACITIES_PART2 = " AND capacity_type = ? AND ((total_capacity * ?) - used_capacity + reserved_capacity) >= ? " +
    		"AND cluster_id IN (SELECT distinct cluster_id  FROM `cloud`.`op_host_capacity` WHERE ";
    private static final String LIST_CLUSTERSINZONE_BY_HOST_CAPACITIES_PART3 = " AND capacity_type = ? AND ((total_capacity * ?) - used_capacity + reserved_capacity) >= ?) " +
    		"ORDER BY ((total_capacity * ?) - used_capacity + reserved_capacity) DESC";
    
    private SearchBuilder<CapacityVO> _hostIdTypeSearch;
	private SearchBuilder<CapacityVO> _hostOrPoolIdSearch;
    protected GenericSearchBuilder<CapacityVO, SummedCapacity> SummedCapactySearch;
	private SearchBuilder<CapacityVO> _allFieldsSearch;
	
	private static final String LIST_HOSTS_IN_CLUSTER_WITH_ENOUGH_CAPACITY = "SELECT a.host_id FROM (host JOIN op_host_capacity a ON host.id = a.host_id AND host.cluster_id = ? AND host.type = ? " +
			"AND (a.total_capacity * ? - a.used_capacity) >= ? and a.capacity_type = 1) " +
			"JOIN op_host_capacity b ON a.host_id = b.host_id AND b.total_capacity - b.used_capacity >= ? AND b.capacity_type = 0";
    
    public CapacityDaoImpl() {
    	_hostIdTypeSearch = createSearchBuilder();
    	_hostIdTypeSearch.and("hostId", _hostIdTypeSearch.entity().getHostOrPoolId(), SearchCriteria.Op.EQ);
    	_hostIdTypeSearch.and("type", _hostIdTypeSearch.entity().getCapacityType(), SearchCriteria.Op.EQ);
    	_hostIdTypeSearch.done();
    	
    	_hostOrPoolIdSearch = createSearchBuilder();
    	_hostOrPoolIdSearch.and("hostId", _hostOrPoolIdSearch.entity().getHostOrPoolId(), SearchCriteria.Op.EQ);
    	_hostOrPoolIdSearch.done();
    	
    	_allFieldsSearch = createSearchBuilder();
    	_allFieldsSearch.and("id", _allFieldsSearch.entity().getId(), SearchCriteria.Op.EQ);
    	_allFieldsSearch.and("hostId", _allFieldsSearch.entity().getHostOrPoolId(), SearchCriteria.Op.EQ);
    	_allFieldsSearch.and("zoneId", _allFieldsSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
    	_allFieldsSearch.and("podId", _allFieldsSearch.entity().getPodId(), SearchCriteria.Op.EQ);
    	_allFieldsSearch.and("clusterId", _allFieldsSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
    	_allFieldsSearch.and("capacityType", _allFieldsSearch.entity().getCapacityType(), SearchCriteria.Op.EQ);
    	
    	_allFieldsSearch.done();
    }
    
    @Override
    public  List<SummedCapacity> findCapacityByType(short capacityType, Long zoneId, Long podId, Long clusterId, Long startIndex, Long pageSize){
    	
    	SummedCapactySearch = createSearchBuilder(SummedCapacity.class);
        SummedCapactySearch.select("sumUsed", Func.SUM, SummedCapactySearch.entity().getUsedCapacity());
        SummedCapactySearch.select("sumTotal", Func.SUM, SummedCapactySearch.entity().getTotalCapacity());
        SummedCapactySearch.select("clusterId", Func.NATIVE, SummedCapactySearch.entity().getClusterId());
        SummedCapactySearch.select("podId", Func.NATIVE, SummedCapactySearch.entity().getPodId());
        
        SummedCapactySearch.and("dcId", SummedCapactySearch.entity().getDataCenterId(), Op.EQ);
        SummedCapactySearch.and("capacityType", SummedCapactySearch.entity().getCapacityType(), Op.EQ);
        SummedCapactySearch.groupBy(SummedCapactySearch.entity().getClusterId());
        
        if (podId != null){
        	SummedCapactySearch.and("podId", SummedCapactySearch.entity().getPodId(), Op.EQ);
        }
        if (clusterId != null){
        	SummedCapactySearch.and("clusterId", SummedCapactySearch.entity().getClusterId(), Op.EQ);
        }
        SummedCapactySearch.done();
        
        
        SearchCriteria<SummedCapacity> sc = SummedCapactySearch.create();
        sc.setParameters("dcId", zoneId);
        sc.setParameters("capacityType", capacityType);
        if (podId != null){
        	sc.setParameters("podId", podId);
        }
        if (clusterId != null){
        	sc.setParameters("clusterId", clusterId);
        }
        
        Filter filter = new Filter(CapacityVO.class, null, true, startIndex, pageSize);
        List<SummedCapacity> results = customSearchIncludingRemoved(sc, filter);
        return results;        
    	
    }
    
    public void updateAllocated(Long hostId, long allocatedAmount, short capacityType, boolean add) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        try {
            txn.start();
            String sql = null;
            if (add) {
                sql = ADD_ALLOCATED_SQL;
            } else {
                sql = SUBTRACT_ALLOCATED_SQL;
            }
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, allocatedAmount);
            pstmt.setLong(2, hostId);
            pstmt.setShort(3, capacityType);
            pstmt.executeUpdate(); // TODO:  Make sure exactly 1 row was updated?
            txn.commit();
        } catch (Exception e) {
            txn.rollback();
            s_logger.warn("Exception updating capacity for host: " + hostId, e);
        }
    }

    
    @Override
    public CapacityVO findByHostIdType(Long hostId, short capacityType) {
    	SearchCriteria<CapacityVO> sc = _hostIdTypeSearch.create();
    	sc.setParameters("hostId", hostId);
    	sc.setParameters("type", capacityType);
    	return findOneBy(sc);
    }  
    
    @Override
    public List<Long> orderClustersInZoneOrPodByHostCapacities(long id, int requiredCpu, long requiredRam, short capacityTypeForOrdering, boolean isZone, float cpuOverprovisioningFactor){
    	Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        List<Long> result = new ArrayList<Long>();

        StringBuilder sql = new StringBuilder(LIST_CLUSTERSINZONE_BY_HOST_CAPACITIES_PART1);
        
        if(isZone){
        	sql.append("capacity.data_center_id = ?");
        }else{
        	sql.append("capacity.pod_id = ?");
        }
        sql.append(LIST_CLUSTERSINZONE_BY_HOST_CAPACITIES_PART2);
        if(isZone){
        	sql.append("capacity.data_center_id = ?");
        }else{
        	sql.append("capacity.pod_id = ?");
        }
        sql.append(LIST_CLUSTERSINZONE_BY_HOST_CAPACITIES_PART3);

        try {
            pstmt = txn.prepareAutoCloseStatement(sql.toString());
            pstmt.setLong(1, id);
            if(capacityTypeForOrdering == CapacityVO.CAPACITY_TYPE_CPU){
            	pstmt.setShort(2, CapacityVO.CAPACITY_TYPE_CPU);
            	pstmt.setFloat(3, cpuOverprovisioningFactor);
            	pstmt.setLong(4, requiredCpu);
            	pstmt.setLong(5, id);
            	pstmt.setShort(6, CapacityVO.CAPACITY_TYPE_MEMORY);
            	pstmt.setFloat(7, 1);
            	pstmt.setLong(8, requiredRam);
            	pstmt.setFloat(9, cpuOverprovisioningFactor);
            }else{
            	pstmt.setShort(2, CapacityVO.CAPACITY_TYPE_MEMORY);
            	pstmt.setFloat(3, 1);
            	pstmt.setLong(4, requiredRam);
            	pstmt.setLong(5, id);
            	pstmt.setShort(6, CapacityVO.CAPACITY_TYPE_CPU);
            	pstmt.setFloat(7, cpuOverprovisioningFactor);
            	pstmt.setLong(8, requiredCpu);
            	pstmt.setFloat(9, 1);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
            return result;
        } catch (SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + sql, e);
        } catch (Throwable e) {
            throw new CloudRuntimeException("Caught: " + sql, e);
        }
    }
    
    
    @Override
    public List<Long> listHostsWithEnoughCapacity(int requiredCpu, long requiredRam, Long clusterId, String hostType, float cpuOverprovisioningFactor){
    	Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        List<Long> result = new ArrayList<Long>();

        StringBuilder sql = new StringBuilder(LIST_HOSTS_IN_CLUSTER_WITH_ENOUGH_CAPACITY);
        try {
            pstmt = txn.prepareAutoCloseStatement(sql.toString());
            pstmt.setLong(1, clusterId);
        	pstmt.setString(2, hostType);
        	pstmt.setFloat(3, cpuOverprovisioningFactor);
        	pstmt.setLong(4, requiredCpu);
        	pstmt.setLong(5, requiredRam);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
            return result;
        } catch (SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + sql, e);
        } catch (Throwable e) {
            throw new CloudRuntimeException("Caught: " + sql, e);
        }
    }
    
	public static class SummedCapacity {
	    public long sumUsed;
	    public long sumTotal;
	    public long clusterId;
	    public long podId;
	    public SummedCapacity() {
	    }
	}
    @Override
    public boolean removeBy(Short capacityType, Long zoneId, Long podId, Long clusterId) {
        SearchCriteria<CapacityVO> sc = _allFieldsSearch.create();
        
        if (capacityType != null) {
            sc.setParameters("capacityType", capacityType);
        }
        
        if (zoneId != null) {
            sc.setParameters("zoneId", zoneId);
        }
        
        if (podId != null) {
            sc.setParameters("podId", podId);
        }
        
        if (clusterId != null) {
            sc.setParameters("clusterId", clusterId);
        }
        
        return remove(sc) > 0;
    }
}
