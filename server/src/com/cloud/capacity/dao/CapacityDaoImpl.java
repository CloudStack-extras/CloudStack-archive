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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.capacity.Capacity;
import com.cloud.capacity.CapacityVO;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.storage.Storage;
import com.cloud.storage.StoragePoolVO;
import com.cloud.storage.dao.StoragePoolDaoImpl;
import com.cloud.utils.DateUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.utils.db.JoinBuilder.JoinType;
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
    private static final String LIST_CLUSTERSINZONE_BY_HOST_CAPACITIES_PART3 = " AND capacity_type = ? AND ((total_capacity * ?) - used_capacity + reserved_capacity) >= ?) ";
    
    private final SearchBuilder<CapacityVO> _hostIdTypeSearch;
	private final SearchBuilder<CapacityVO> _hostOrPoolIdSearch;
    protected GenericSearchBuilder<CapacityVO, SummedCapacity> SummedCapacitySearch;
	private SearchBuilder<CapacityVO> _allFieldsSearch;
    protected final StoragePoolDaoImpl _storagePoolDao = ComponentLocator.inject(StoragePoolDaoImpl.class);

	
    private static final String LIST_HOSTS_IN_CLUSTER_WITH_ENOUGH_CAPACITY = "SELECT a.host_id FROM (host JOIN op_host_capacity a ON host.id = a.host_id AND host.cluster_id = ? AND host.type = ? " +
			"AND (a.total_capacity * ? - a.used_capacity) >= ? and a.capacity_type = 1) " +
			"JOIN op_host_capacity b ON a.host_id = b.host_id AND b.total_capacity - b.used_capacity >= ? AND b.capacity_type = 0";
	
    private static final String ORDER_CLUSTERS_BY_AGGREGATE_CAPACITY_PART1 = "SELECT cluster_id, SUM(used_capacity+reserved_capacity)/SUM(total_capacity * ?) FROM `cloud`.`op_host_capacity` WHERE " ;
    private static final String ORDER_CLUSTERS_BY_AGGREGATE_CAPACITY_PART2 = " AND capacity_type = ? GROUP BY cluster_id ORDER BY SUM(used_capacity+reserved_capacity)/SUM(total_capacity * ?) ASC";
	
    private static final String LIST_PODSINZONE_BY_HOST_CAPACITIES = "SELECT DISTINCT capacity.pod_id  FROM `cloud`.`op_host_capacity` capacity INNER JOIN `cloud`.`host_pod_ref` pod " +
    		                                                         " ON (pod.id = capacity.pod_id AND pod.removed is NULL) WHERE " +
                                                                     " capacity.data_center_id = ? AND capacity_type = ? AND ((total_capacity * ?) - used_capacity + reserved_capacity) >= ? " +
                                                                     " AND pod_id IN (SELECT distinct pod_id  FROM `cloud`.`op_host_capacity` WHERE " +
                                                                     " capacity.data_center_id = ? AND capacity_type = ? AND ((total_capacity * ?) - used_capacity + reserved_capacity) >= ?) ";

    private static final String ORDER_PODS_BY_AGGREGATE_CAPACITY = "SELECT pod_id, SUM(used_capacity+reserved_capacity)/SUM(total_capacity * ?) FROM `cloud`.`op_host_capacity` WHERE data_center_id = ? " +
                                                                   " AND capacity_type = ? GROUP BY pod_id ORDER BY SUM(used_capacity+reserved_capacity)/SUM(total_capacity * ?) ASC";
    
    private static final String LIST_CAPACITY_BY_RESOURCE_STATE = "SELECT capacity.data_center_id, sum(capacity.used_capacity), sum(capacity.reserved_quantity), sum(capacity.total_capacity), capacity_capacity_type "+
                                                                  "FROM `cloud`.`op_host_capacity` capacity INNER JOIN `cloud`.`data_center` dc ON (dc.id = capacity.data_center_id AND dc.removed is NULL)"+
                                                                  "FROM `cloud`.`op_host_capacity` capacity INNER JOIN `cloud`.`host_pod_ref` pod ON (pod.id = capacity.pod_id AND pod.removed is NULL)"+
                                                                  "FROM `cloud`.`op_host_capacity` capacity INNER JOIN `cloud`.`cluster` cluster ON (cluster.id = capacity.cluster_id AND cluster.removed is NULL)"+
                                                                  "FROM `cloud`.`op_host_capacity` capacity INNER JOIN `cloud`.`host` host ON (host.id = capacity.host_id AND host.removed is NULL)"+
                                                                  "WHERE dc.allocation_state = ? AND pod.allocation_state = ? AND cluster.allocation_state = ? AND host.resource_state = ? AND capacity_type not in (3,4) ";
    
    private static final String LIST_CAPACITY_GROUP_BY_ZONE_TYPE_PART1 = "SELECT (sum(capacity.used_capacity) + sum(capacity.reserved_capacity)), (case capacity_type when 1 then (sum(total_capacity) * (select value from `cloud`.`configuration` where name like 'cpu.overprovisioning.factor')) else sum(total_capacity) end), " +
                                                                         "((sum(capacity.used_capacity) + sum(capacity.reserved_capacity)) / (case capacity_type when 1 then (sum(total_capacity) * (select value from `cloud`.`configuration` where name like 'cpu.overprovisioning.factor')) else sum(total_capacity) end)) percent,"+
                                                                         " capacity.capacity_type, capacity.data_center_id "+
                                                                         "FROM `cloud`.`op_host_capacity` capacity "+
                                                                         "WHERE  total_capacity > 0 AND data_center_id is not null AND capacity_state='Enabled'";
    private static final String LIST_CAPACITY_GROUP_BY_ZONE_TYPE_PART2 = " GROUP BY data_center_id, capacity_type order by percent desc limit ";
    private static final String LIST_CAPACITY_GROUP_BY_POD_TYPE_PART1 = "SELECT (sum(capacity.used_capacity) + sum(capacity.reserved_capacity)), (case capacity_type when 1 then (sum(total_capacity) * (select value from `cloud`.`configuration` where name like 'cpu.overprovisioning.factor')) else sum(total_capacity) end), " +
                                                                        "((sum(capacity.used_capacity) + sum(capacity.reserved_capacity)) / (case capacity_type when 1 then (sum(total_capacity) * (select value from `cloud`.`configuration` where name like 'cpu.overprovisioning.factor')) else sum(total_capacity) end)) percent,"+
                                                                        " capacity.capacity_type, capacity.data_center_id, pod_id "+
                                                                        "FROM `cloud`.`op_host_capacity` capacity "+
                                                                        "WHERE  total_capacity > 0 AND pod_id is not null AND capacity_state='Enabled'";
    private static final String LIST_CAPACITY_GROUP_BY_POD_TYPE_PART2 = " GROUP BY pod_id, capacity_type order by percent desc limit ";
    
    private static final String LIST_CAPACITY_GROUP_BY_CLUSTER_TYPE_PART1 = "SELECT (sum(capacity.used_capacity) + sum(capacity.reserved_capacity)), (case capacity_type when 1 then (sum(total_capacity) * (select value from `cloud`.`configuration` where name like 'cpu.overprovisioning.factor')) else sum(total_capacity) end), " +
                                                                            "((sum(capacity.used_capacity) + sum(capacity.reserved_capacity)) / (case capacity_type when 1 then (sum(total_capacity) * (select value from `cloud`.`configuration` where name like 'cpu.overprovisioning.factor')) else sum(total_capacity) end)) percent,"+
                                                                            "capacity.capacity_type, capacity.data_center_id, pod_id, cluster_id "+
                                                                            "FROM `cloud`.`op_host_capacity` capacity "+
                                                                            "WHERE  total_capacity > 0 AND cluster_id is not null AND capacity_state='Enabled'";
    private static final String LIST_CAPACITY_GROUP_BY_CLUSTER_TYPE_PART2 = " GROUP BY cluster_id, capacity_type order by percent desc limit ";
    private static final String UPDATE_CAPACITY_STATE = "UPDATE `cloud`.`op_host_capacity` SET capacity_state = ? WHERE ";
    
    
    
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
    	_allFieldsSearch.and("capacityState", _allFieldsSearch.entity().getCapacityState(), SearchCriteria.Op.EQ);
    	
    	_allFieldsSearch.done();
    }
    
    @Override
    public  List<SummedCapacity> findCapacityBy(Integer capacityType, Long zoneId, Long podId, Long clusterId, String resource_state){
        
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        List<SummedCapacity> result = new ArrayList<SummedCapacity>();

        StringBuilder sql = new StringBuilder(LIST_CAPACITY_BY_RESOURCE_STATE);           
        List<Long> resourceIdList = new ArrayList<Long>();
        
        if (zoneId != null){
            sql.append(" AND capacity.data_center_id = ?");
            resourceIdList.add(zoneId);
        }
        if (podId != null){
            sql.append(" AND capacity.pod_id = ?");
            resourceIdList.add(podId);
        }
        if (clusterId != null){
            sql.append(" AND capacity.cluster_id = ?");
            resourceIdList.add(clusterId);
        }
        if (capacityType != null){
            sql.append(" AND capacity.capacity_type = ?");
            resourceIdList.add(capacityType.longValue());
        }   

        try {
            pstmt = txn.prepareAutoCloseStatement(sql.toString());
            pstmt.setString(1, resource_state);
            pstmt.setString(2, resource_state);
            pstmt.setString(3, resource_state);
            pstmt.setString(4, resource_state);
            for (int i = 0; i < resourceIdList.size(); i++){                
                pstmt.setLong( 5+i, resourceIdList.get(i));
            }            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                SummedCapacity summedCapacity = new SummedCapacity(rs.getLong(2), rs.getLong(3), rs.getLong(4), (short)rs.getLong(5), null, null, rs.getLong(1));
                result.add(summedCapacity);
            }
            return result;
        } catch (SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + sql, e);
        } catch (Throwable e) {
            throw new CloudRuntimeException("Caught: " + sql, e);
        }        
    }
    
    @Override
    public  List<SummedCapacity> listCapacitiesGroupedByLevelAndType(Integer capacityType, Long zoneId, Long podId, Long clusterId, int level, Long limit){
        
        StringBuilder finalQuery = new StringBuilder(); 
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        List<SummedCapacity> result = new ArrayList<SummedCapacity>();
        
        switch(level){
            case 1: // List all the capacities grouped by zone, capacity Type
                finalQuery.append(LIST_CAPACITY_GROUP_BY_ZONE_TYPE_PART1);
                break;
                
            case 2: // List all the capacities grouped by pod, capacity Type
                finalQuery.append(LIST_CAPACITY_GROUP_BY_POD_TYPE_PART1);
                break;
                
            case 3: // List all the capacities grouped by cluster, capacity Type
                finalQuery.append(LIST_CAPACITY_GROUP_BY_CLUSTER_TYPE_PART1);
                break;
        }
        
        if (zoneId != null){
            finalQuery.append(" AND data_center_id="+zoneId);
        }
        if (podId != null){
            finalQuery.append(" AND pod_id="+podId);
        }
        if (clusterId != null){
            finalQuery.append(" AND cluster_id="+clusterId);
        }
        if (capacityType != null){
            finalQuery.append(" AND capacity_type="+capacityType);   
        }                
        
        switch(level){
        case 1: // List all the capacities grouped by zone, capacity Type
            finalQuery.append(LIST_CAPACITY_GROUP_BY_ZONE_TYPE_PART2);
            break;
            
        case 2: // List all the capacities grouped by pod, capacity Type
            finalQuery.append(LIST_CAPACITY_GROUP_BY_POD_TYPE_PART2);
            break;
            
        case 3: // List all the capacities grouped by cluster, capacity Type
            finalQuery.append(LIST_CAPACITY_GROUP_BY_CLUSTER_TYPE_PART2);
            break;
        }
        
        finalQuery.append(limit.toString());
        
        try {
            pstmt = txn.prepareAutoCloseStatement(finalQuery.toString());        
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {                
                SummedCapacity summedCapacity = new SummedCapacity( rs.getLong(1), rs.getLong(2), rs.getFloat(3),
                                                                    (short)rs.getLong(4), rs.getLong(5),
                                                                    level != 1 ? rs.getLong(6): null,
                                                                    level == 3 ? rs.getLong(7): null);
                                                                   
                result.add(summedCapacity);
            }
            return result;
        } catch (SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + finalQuery, e);
        } catch (Throwable e) {
            throw new CloudRuntimeException("Caught: " + finalQuery, e);
        }                     
        
    }
    
    @Override
    public  List<SummedCapacity> findCapacityBy(Integer capacityType, Long zoneId, Long podId, Long clusterId){
    	
    	SummedCapacitySearch = createSearchBuilder(SummedCapacity.class);
    	SummedCapacitySearch.select("dcId", Func.NATIVE, SummedCapacitySearch.entity().getDataCenterId());
        SummedCapacitySearch.select("sumUsed", Func.SUM, SummedCapacitySearch.entity().getUsedCapacity());
        SummedCapacitySearch.select("sumReserved", Func.SUM, SummedCapacitySearch.entity().getReservedCapacity());
        SummedCapacitySearch.select("sumTotal", Func.SUM, SummedCapacitySearch.entity().getTotalCapacity());
        SummedCapacitySearch.select("capacityType", Func.NATIVE, SummedCapacitySearch.entity().getCapacityType());        
        
        if (zoneId==null && podId==null && clusterId==null){ // List all the capacities grouped by zone, capacity Type
            SummedCapacitySearch.groupBy(SummedCapacitySearch.entity().getDataCenterId(), SummedCapacitySearch.entity().getCapacityType());            
        }else {
            SummedCapacitySearch.groupBy(SummedCapacitySearch.entity().getCapacityType());
        }
        
        if (zoneId != null){
        	SummedCapacitySearch.and("dcId", SummedCapacitySearch.entity().getDataCenterId(), Op.EQ);
        }
        if (podId != null){
        	SummedCapacitySearch.and("podId", SummedCapacitySearch.entity().getPodId(), Op.EQ);
        }
        if (clusterId != null){
        	SummedCapacitySearch.and("clusterId", SummedCapacitySearch.entity().getClusterId(), Op.EQ);
        }
        if (capacityType != null){
        	SummedCapacitySearch.and("capacityType", SummedCapacitySearch.entity().getCapacityType(), Op.EQ);	
        }        

        SummedCapacitySearch.done();
        
        
        SearchCriteria<SummedCapacity> sc = SummedCapacitySearch.create();
        if (zoneId != null){
        	sc.setParameters("dcId", zoneId);
        }
        if (podId != null){
        	sc.setParameters("podId", podId);
        }
        if (clusterId != null){
        	sc.setParameters("clusterId", clusterId);
        }
        if (capacityType != null){
        	sc.setParameters("capacityType", capacityType);
        }
        
        Filter filter = new Filter(CapacityVO.class, null, true, null, null);
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
    public List<Long> listClustersInZoneOrPodByHostCapacities(long id, int requiredCpu, long requiredRam, short capacityTypeForOrdering, boolean isZone, float cpuOverprovisioningFactor){
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
        	pstmt.setShort(2, CapacityVO.CAPACITY_TYPE_CPU);
        	pstmt.setFloat(3, cpuOverprovisioningFactor);
        	pstmt.setLong(4, requiredCpu);
        	pstmt.setLong(5, id);
        	pstmt.setShort(6, CapacityVO.CAPACITY_TYPE_MEMORY);
        	pstmt.setFloat(7, 1);
        	pstmt.setLong(8, requiredRam);

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
	    public long sumReserved;
	    public long sumTotal;
	    public Float percentUsed;
	    public short capacityType;
	    public Long clusterId;
	    public Long podId;
	    public Long dcId;
	    public SummedCapacity() {
	    }
		public SummedCapacity(long sumUsed, long sumReserved, long sumTotal,
				short capacityType, Long clusterId, Long podId) {
			super();
			this.sumUsed = sumUsed;
			this.sumReserved = sumReserved;
			this.sumTotal = sumTotal;
			this.capacityType = capacityType;
			this.clusterId = clusterId;
			this.podId = podId;
		}
		public SummedCapacity(long sumUsed, long sumReserved, long sumTotal,
                short capacityType, Long clusterId, Long podId, Long zoneId) {
		    this(sumUsed, sumReserved, sumTotal, capacityType, clusterId, podId);
	        this.dcId = zoneId;
		}
		
		public SummedCapacity(long sumUsed, long sumTotal, float percentUsed, short capacityType, Long zoneId, Long podId, Long clusterId) {
		    super();
		    this.sumUsed = sumUsed;
		    this.sumTotal = sumTotal;
		    this.percentUsed = percentUsed;
		    this.capacityType = capacityType;
            this.clusterId = clusterId;
            this.podId = podId;
            this.dcId = zoneId;
        }
		
		public Short getCapacityType() {				
			return capacityType;
		}
		public Long getUsedCapacity() {
			return sumUsed;
		}
		public long getReservedCapacity() {
			return sumReserved;
		}
		public Long getTotalCapacity() {
			return sumTotal;
		}
		public Long getDataCenterId() {
            return dcId;
        }
		public Long getClusterId() {
            return clusterId;
        }
        public Long getPodId() {
            return podId;
        }
        public Float getPercentUsed() {
            return percentUsed;
        }
	}
	public List<SummedCapacity> findByClusterPodZone(Long zoneId, Long podId, Long clusterId){

    	SummedCapacitySearch = createSearchBuilder(SummedCapacity.class);
        SummedCapacitySearch.select("sumUsed", Func.SUM, SummedCapacitySearch.entity().getUsedCapacity());
        SummedCapacitySearch.select("sumTotal", Func.SUM, SummedCapacitySearch.entity().getTotalCapacity());   
        SummedCapacitySearch.select("capacityType", Func.NATIVE, SummedCapacitySearch.entity().getCapacityType());                                
        SummedCapacitySearch.groupBy(SummedCapacitySearch.entity().getCapacityType());
        
        if(zoneId != null){
        	SummedCapacitySearch.and("zoneId", SummedCapacitySearch.entity().getDataCenterId(), Op.EQ);
        }
        if (podId != null){
        	SummedCapacitySearch.and("podId", SummedCapacitySearch.entity().getPodId(), Op.EQ);
        }
        if (clusterId != null){
        	SummedCapacitySearch.and("clusterId", SummedCapacitySearch.entity().getClusterId(), Op.EQ);
        }
        SummedCapacitySearch.done();
        
        
        SearchCriteria<SummedCapacity> sc = SummedCapacitySearch.create();
        if (zoneId != null){
        	sc.setParameters("zoneId", zoneId);
        }
        if (podId != null){
        	sc.setParameters("podId", podId);
        }
        if (clusterId != null){
        	sc.setParameters("clusterId", clusterId);
        }
                
        return customSearchIncludingRemoved(sc, null);         
	}
	
	@Override
	public List<SummedCapacity> findNonSharedStorageForClusterPodZone(Long zoneId, Long podId, Long clusterId){

    	SummedCapacitySearch = createSearchBuilder(SummedCapacity.class);
        SummedCapacitySearch.select("sumUsed", Func.SUM, SummedCapacitySearch.entity().getUsedCapacity());
        SummedCapacitySearch.select("sumTotal", Func.SUM, SummedCapacitySearch.entity().getTotalCapacity());   
        SummedCapacitySearch.select("capacityType", Func.NATIVE, SummedCapacitySearch.entity().getCapacityType());
        SummedCapacitySearch.and("capacityType", SummedCapacitySearch.entity().getCapacityType(), Op.EQ);
    	
    	SearchBuilder<StoragePoolVO>  nonSharedStorage = _storagePoolDao.createSearchBuilder();
    	nonSharedStorage.and("poolTypes", nonSharedStorage.entity().getPoolType(), SearchCriteria.Op.IN);
    	SummedCapacitySearch.join("nonSharedStorage", nonSharedStorage, nonSharedStorage.entity().getId(), SummedCapacitySearch.entity().getHostOrPoolId(), JoinType.INNER);
    	nonSharedStorage.done();        
    	
        if(zoneId != null){
        	SummedCapacitySearch.and("zoneId", SummedCapacitySearch.entity().getDataCenterId(), Op.EQ);
        }
        if (podId != null){
        	SummedCapacitySearch.and("podId", SummedCapacitySearch.entity().getPodId(), Op.EQ);
        }
        if (clusterId != null){
        	SummedCapacitySearch.and("clusterId", SummedCapacitySearch.entity().getClusterId(), Op.EQ);
        }
        SummedCapacitySearch.done();
        
        
        SearchCriteria<SummedCapacity> sc = SummedCapacitySearch.create();
        sc.setJoinParameters("nonSharedStorage", "poolTypes", Storage.getNonSharedStoragePoolTypes().toArray());
        sc.setParameters("capacityType", Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED);
        if (zoneId != null){
        	sc.setParameters("zoneId", zoneId);
        }
        if (podId != null){
        	sc.setParameters("podId", podId);
        }
        if (clusterId != null){
        	sc.setParameters("clusterId", clusterId);
        }
                
        return customSearchIncludingRemoved(sc, null);         
	}
	
    @Override
    public boolean removeBy(Short capacityType, Long zoneId, Long podId, Long clusterId, Long hostId) {
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
        
        if (hostId != null) {
            sc.setParameters("hostId", hostId);
        }
        
        return remove(sc) > 0;
    }
    
    @Override
    public Pair<List<Long>, Map<Long, Double>> orderClustersByAggregateCapacity(long id, short capacityTypeForOrdering, boolean isZone, float cpuOverprovisioningFactor){
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        List<Long> result = new ArrayList<Long>();
        Map<Long, Double> clusterCapacityMap = new HashMap<Long, Double>();

        StringBuilder sql = new StringBuilder(ORDER_CLUSTERS_BY_AGGREGATE_CAPACITY_PART1);
        
        if(isZone){
            sql.append("data_center_id = ?");
        }else{
            sql.append("pod_id = ?");
        }
        sql.append(ORDER_CLUSTERS_BY_AGGREGATE_CAPACITY_PART2);
        try {
            pstmt = txn.prepareAutoCloseStatement(sql.toString());
            if(capacityTypeForOrdering == CapacityVO.CAPACITY_TYPE_CPU){
                pstmt.setFloat(1, cpuOverprovisioningFactor);
                pstmt.setFloat(4, cpuOverprovisioningFactor);
            }else{
                pstmt.setFloat(1, 1);
                pstmt.setFloat(4, 1);
            }
            pstmt.setLong(2, id);
            pstmt.setShort(3, capacityTypeForOrdering);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Long clusterId = rs.getLong(1);
                result.add(clusterId);
                clusterCapacityMap.put(clusterId, rs.getDouble(2));
            }
            return new Pair<List<Long>, Map<Long, Double>>(result, clusterCapacityMap);
        } catch (SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + sql, e);
        } catch (Throwable e) {
            throw new CloudRuntimeException("Caught: " + sql, e);
        }
    }

    @Override
    public List<Long> listPodsByHostCapacities(long zoneId, int requiredCpu, long requiredRam, short capacityType, float cpuOverprovisioningFactor) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        List<Long> result = new ArrayList<Long>();

        StringBuilder sql = new StringBuilder(LIST_PODSINZONE_BY_HOST_CAPACITIES);

        try {
            pstmt = txn.prepareAutoCloseStatement(sql.toString());
            pstmt.setLong(1, zoneId);
            pstmt.setShort(2, CapacityVO.CAPACITY_TYPE_CPU);
            pstmt.setFloat(3, cpuOverprovisioningFactor);
            pstmt.setLong(4, requiredCpu);
            pstmt.setLong(5, zoneId);
            pstmt.setShort(6, CapacityVO.CAPACITY_TYPE_MEMORY);
            pstmt.setFloat(7, 1);
            pstmt.setLong(8, requiredRam);

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
    public Pair<List<Long>, Map<Long, Double>> orderPodsByAggregateCapacity(long zoneId, short capacityTypeForOrdering, float cpuOverprovisioningFactor) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        List<Long> result = new ArrayList<Long>();
        Map<Long, Double> podCapacityMap = new HashMap<Long, Double>();
        
        StringBuilder sql = new StringBuilder(ORDER_PODS_BY_AGGREGATE_CAPACITY);
        try {
            pstmt = txn.prepareAutoCloseStatement(sql.toString());
            pstmt.setLong(2, zoneId);
            pstmt.setShort(3, capacityTypeForOrdering);
            
            if(capacityTypeForOrdering == CapacityVO.CAPACITY_TYPE_CPU){
                pstmt.setFloat(1, cpuOverprovisioningFactor);
                pstmt.setFloat(4, cpuOverprovisioningFactor);
            }else{
                pstmt.setFloat(1, 1);
                pstmt.setFloat(4, 1);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Long podId = rs.getLong(1);
                result.add(podId);
                podCapacityMap.put(podId, rs.getDouble(2));
            }
            return new Pair<List<Long>, Map<Long, Double>>(result, podCapacityMap);
        } catch (SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + sql, e);
        } catch (Throwable e) {
            throw new CloudRuntimeException("Caught: " + sql, e);
        }
    }
    
    @Override
    public void updateCapacityState(Long dcId, Long podId, Long clusterId, Long hostId, String capacityState) {
        Transaction txn = Transaction.currentTxn();
        StringBuilder sql = new StringBuilder(UPDATE_CAPACITY_STATE); 
        List<Long> resourceIdList = new ArrayList<Long>();
        
        if (dcId != null){
            sql.append(" data_center_id = ?");
            resourceIdList.add(dcId);
        }
        if (podId != null){
            sql.append(" pod_id = ?");
            resourceIdList.add(podId);
        }
        if (clusterId != null){
            sql.append(" cluster_id = ?");
            resourceIdList.add(clusterId);
        }
        if (hostId != null){
            sql.append(" host_id = ?");
            resourceIdList.add(hostId);
        }
        
        PreparedStatement pstmt = null;
        try {       
            pstmt = txn.prepareAutoCloseStatement(sql.toString());
            pstmt.setString(1, capacityState);
            for (int i = 0; i < resourceIdList.size(); i++){                
                pstmt.setLong( 2+i, resourceIdList.get(i));
            }            
            pstmt.executeUpdate();
        } catch (Exception e) {
            s_logger.warn("Error updating CapacityVO", e);
        }
    }
}
