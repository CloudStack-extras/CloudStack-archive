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
package com.cloud.storage.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Volume;
import com.cloud.storage.Volume.Type;
import com.cloud.storage.VolumeVO;
import com.cloud.utils.Pair;
import com.cloud.utils.db.Attribute;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.utils.exception.CloudRuntimeException;

@Local(value=VolumeDao.class) @DB(txn=false)
public class VolumeDaoImpl extends GenericDaoBase<VolumeVO, Long> implements VolumeDao {
    private static final Logger s_logger = Logger.getLogger(VolumeDaoImpl.class);
    protected final SearchBuilder<VolumeVO> DetachedAccountIdSearch;
    protected final SearchBuilder<VolumeVO> TemplateZoneSearch;
    protected final GenericSearchBuilder<VolumeVO, SumCount> TotalSizeByPoolSearch;
    protected final GenericSearchBuilder<VolumeVO, Long> ActiveTemplateSearch;
    protected final SearchBuilder<VolumeVO> InstanceStatesSearch;
    protected final SearchBuilder<VolumeVO> AllFieldsSearch;
    
    protected final Attribute _stateAttr;
    
    protected static final String SELECT_VM_SQL = "SELECT DISTINCT instance_id from volumes v where v.host_id = ? and v.mirror_state = ?";
    protected static final String SELECT_HYPERTYPE_FROM_VOLUME = "SELECT c.hypervisor_type from volumes v, storage_pool s, cluster c where v.pool_id = s.id and s.cluster_id = c.id and v.id = ?";

    @Override
    public List<VolumeVO> findDetachedByAccount(long accountId) {
    	SearchCriteria<VolumeVO> sc = DetachedAccountIdSearch.create();
    	sc.setParameters("accountId", accountId);
    	sc.setParameters("destroyed", Volume.State.Destroy);
    	return listBy(sc);
    }
    
    @Override
    public List<VolumeVO> findByAccount(long accountId) {
        SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("notDestroyed", Volume.State.Destroy);
        return listBy(sc);
    }
    
    @Override
    public List<VolumeVO> findByInstance(long id) {
        SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", id);
	    return listBy(sc);
	}
   
    @Override
    public List<VolumeVO> findByInstanceAndDeviceId(long instanceId, long deviceId){
    	SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
    	sc.setParameters("instanceId", instanceId);
    	sc.setParameters("deviceId", deviceId);
    	return listBy(sc);
    }
    
    @Override
    public List<VolumeVO> findByPoolId(long poolId) {
        SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("poolId", poolId);
        sc.setParameters("notDestroyed", Volume.State.Destroy);
        sc.setParameters("vType", Volume.Type.ROOT.toString());
	    return listBy(sc);
	}
    
    @Override 
    public List<VolumeVO> findCreatedByInstance(long id) {
        SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", id);
        sc.setParameters("state", Volume.State.Ready);
        return listBy(sc);
    }
    
    @Override
    public List<VolumeVO> findUsableVolumesForInstance(long instanceId) {
        SearchCriteria<VolumeVO> sc = InstanceStatesSearch.create();
        sc.setParameters("instance", instanceId);
        sc.setParameters("states", Volume.State.Creating, Volume.State.Ready, Volume.State.Allocated);
        
        return listBy(sc);
    }
    
	@Override
	public List<VolumeVO> findByInstanceAndType(long id, Type vType) {
        SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", id);
        sc.setParameters("vType", vType.toString());
	    return listBy(sc);
	}
	
	@Override
	public List<VolumeVO> findByInstanceIdDestroyed(long vmId) {
		SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
		sc.setParameters("instanceId", vmId);
		sc.setParameters("destroyed", Volume.State.Destroy);
		return listBy(sc);
	}
	
	@Override
	public List<VolumeVO> findReadyRootVolumesByInstance(long instanceId) {
		SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
		sc.setParameters("instanceId", instanceId);
		sc.setParameters("state", Volume.State.Ready);
		sc.setParameters("vType", Volume.Type.ROOT);		
		return listBy(sc);
	}
	
	@Override
	public List<VolumeVO> findByAccountAndPod(long accountId, long podId) {
		SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("pod", podId);
        sc.setParameters("state", Volume.State.Ready);
        
        return listIncludingRemovedBy(sc);
	}
	
	@Override
	public List<VolumeVO> findByTemplateAndZone(long templateId, long zoneId) {
		SearchCriteria<VolumeVO> sc = TemplateZoneSearch.create();
		sc.setParameters("template", templateId);
		sc.setParameters("zone", zoneId);
		
		return listIncludingRemovedBy(sc);
	}

	@Override
	public boolean isAnyVolumeActivelyUsingTemplateOnPool(long templateId, long poolId) {
	    SearchCriteria<Long> sc = ActiveTemplateSearch.create();
	    sc.setParameters("template", templateId);
	    sc.setParameters("pool", poolId);
	    
	    List<Long> results = customSearchIncludingRemoved(sc, null);
	    assert results.size() > 0 : "How can this return a size of " + results.size();
	    
	    return results.get(0) > 0;
	}
	
    @Override
    public void deleteVolumesByInstance(long instanceId) {
        SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", instanceId);
        expunge(sc);
    }
    
    @Override
    public void attachVolume(long volumeId, long vmId, long deviceId) {
    	VolumeVO volume = createForUpdate(volumeId);
    	volume.setInstanceId(vmId);
    	volume.setDeviceId(deviceId);
    	volume.setUpdated(new Date());
    	volume.setAttached(new Date());
    	update(volumeId, volume);
    }
    
    @Override
    public void detachVolume(long volumeId) {
    	VolumeVO volume = createForUpdate(volumeId);
    	volume.setInstanceId(null);
        volume.setDeviceId(null);
    	volume.setUpdated(new Date());
    	volume.setAttached(null);
    	update(volumeId, volume);
    }
    
    @Override
    public boolean update(VolumeVO vol, Volume.Event event) throws ConcurrentOperationException {
        Volume.State oldState = vol.getState();
        Volume.State newState = oldState.getNextState(event);
        
        assert newState != null : "Event "+  event + " cannot happen from " + oldState; 
        
        UpdateBuilder builder = getUpdateBuilder(vol);
        builder.set(vol, _stateAttr, newState);
        
        SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("id", vol.getId());
        sc.setParameters("state", oldState);
        
        int rows = update(builder, sc, null);
        if (rows != 1) {
            VolumeVO dbVol = findById(vol.getId()); 
            throw new ConcurrentOperationException("Unable to update " + vol + ": Old State=" + oldState + "; New State = " + newState + "; DB State=" + dbVol.getState());
        }
        return rows == 1;
    }
    
    @Override
    @DB
	public HypervisorType getHypervisorType(long volumeId) {
		/*lookup from cluster of pool*/
    	 Transaction txn = Transaction.currentTxn();
         PreparedStatement pstmt = null;

         try {
             String sql = SELECT_HYPERTYPE_FROM_VOLUME;
             pstmt = txn.prepareAutoCloseStatement(sql);
             pstmt.setLong(1, volumeId);
             ResultSet rs = pstmt.executeQuery();
             if (rs.next()) {
                return HypervisorType.getType(rs.getString(1));
            }
             return HypervisorType.None;
         } catch (SQLException e) {
             throw new CloudRuntimeException("DB Exception on: " + SELECT_HYPERTYPE_FROM_VOLUME, e);
         } catch (Throwable e) {
             throw new CloudRuntimeException("Caught: " + SELECT_HYPERTYPE_FROM_VOLUME, e);
         }
	}
    
    @Override
    public ImageFormat getImageFormat(Long volumeId) {
        HypervisorType type = getHypervisorType(volumeId);
        if ( type.equals(HypervisorType.KVM)) {
            return ImageFormat.QCOW2;
        } else if ( type.equals(HypervisorType.XenServer)) {
            return ImageFormat.VHD;
        } else if ( type.equals(HypervisorType.VMware)) {
            return ImageFormat.OVA;
        } else {
            s_logger.warn("Do not support hypervisor " + type.toString());
            return null;
        }
    }
    
	protected VolumeDaoImpl() {
	    AllFieldsSearch = createSearchBuilder();
	    AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.and("accountId", AllFieldsSearch.entity().getAccountId(), Op.EQ);
        AllFieldsSearch.and("pod", AllFieldsSearch.entity().getPodId(), Op.EQ);
        AllFieldsSearch.and("instanceId", AllFieldsSearch.entity().getInstanceId(), Op.EQ);
        AllFieldsSearch.and("deviceId", AllFieldsSearch.entity().getDeviceId(), Op.EQ);
        AllFieldsSearch.and("poolId", AllFieldsSearch.entity().getPoolId(), Op.EQ);
        AllFieldsSearch.and("vType", AllFieldsSearch.entity().getVolumeType(), Op.EQ);
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("destroyed", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.and("notDestroyed", AllFieldsSearch.entity().getState(), Op.NEQ);
        AllFieldsSearch.done();
        
        DetachedAccountIdSearch = createSearchBuilder();
        DetachedAccountIdSearch.and("accountId", DetachedAccountIdSearch.entity().getAccountId(), Op.EQ);
        DetachedAccountIdSearch.and("destroyed", DetachedAccountIdSearch.entity().getState(), Op.NEQ);
        DetachedAccountIdSearch.and("instanceId", DetachedAccountIdSearch.entity().getInstanceId(), Op.NULL);
        DetachedAccountIdSearch.done();
        
        TemplateZoneSearch = createSearchBuilder();
        TemplateZoneSearch.and("template", TemplateZoneSearch.entity().getTemplateId(), Op.EQ);
        TemplateZoneSearch.and("zone", TemplateZoneSearch.entity().getDataCenterId(), Op.EQ);
        TemplateZoneSearch.done();
        
        TotalSizeByPoolSearch = createSearchBuilder(SumCount.class);
        TotalSizeByPoolSearch.select("sum", Func.SUM, TotalSizeByPoolSearch.entity().getSize());
        TotalSizeByPoolSearch.select("count", Func.COUNT, (Object[])null);
        TotalSizeByPoolSearch.and("poolId", TotalSizeByPoolSearch.entity().getPoolId(), Op.EQ);
        TotalSizeByPoolSearch.and("removed", TotalSizeByPoolSearch.entity().getRemoved(), Op.NULL);
        TotalSizeByPoolSearch.done();
      
        ActiveTemplateSearch = createSearchBuilder(Long.class);
        ActiveTemplateSearch.and("pool", ActiveTemplateSearch.entity().getPoolId(), Op.EQ);
        ActiveTemplateSearch.and("template", ActiveTemplateSearch.entity().getTemplateId(), Op.EQ);
        ActiveTemplateSearch.and("removed", ActiveTemplateSearch.entity().getRemoved(), Op.NULL);
        ActiveTemplateSearch.select(null, Func.COUNT, null);
        ActiveTemplateSearch.done();
        
        InstanceStatesSearch = createSearchBuilder();
        InstanceStatesSearch.and("instance", InstanceStatesSearch.entity().getInstanceId(), Op.EQ);
        InstanceStatesSearch.and("states", InstanceStatesSearch.entity().getState(), Op.IN);
        InstanceStatesSearch.done();

        _stateAttr = _allAttributes.get("state");
        assert _stateAttr != null : "Couldn't get the state attribute";
	}

	@Override @DB(txn=false)
	public Pair<Long, Long> getCountAndTotalByPool(long poolId) {
        SearchCriteria<SumCount> sc = TotalSizeByPoolSearch.create();
        sc.setParameters("poolId", poolId);
        List<SumCount> results = customSearchIncludingRemoved(sc, null);
        SumCount sumCount = results.get(0);
        return new Pair<Long, Long>(sumCount.count, sumCount.sum);
	}
	
	public static class SumCount {
	    public long sum;
	    public long count;
	    public SumCount() {
	    }
	}

    @Override
    public List<VolumeVO> listVolumesToBeDestroyed() {
        SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("state", Volume.State.Destroy);
        
        return listBy(sc);
    }
}
