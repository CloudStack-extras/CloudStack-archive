package com.cloud.simulator.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import javax.ejb.Local;

import com.cloud.simulator.MockConfigurationVO;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;

@Local(value={MockConfigurationDao.class})
public class MockConfigurationDaoImpl extends GenericDaoBase<MockConfigurationVO, Long> implements MockConfigurationDao {
    private SearchBuilder<MockConfigurationVO> _searchByDcIdName;
    private SearchBuilder<MockConfigurationVO> _searchByDcIDPodIdName;
    private SearchBuilder<MockConfigurationVO> _searchByDcIDPodIdClusterIdName;
    private SearchBuilder<MockConfigurationVO> _searchByDcIDPodIdClusterIdHostIdName;
    private SearchBuilder<MockConfigurationVO> _searchByGlobalName;
    																					
    
    public MockConfigurationDaoImpl() {
        _searchByGlobalName = createSearchBuilder();
        _searchByGlobalName.and("dcId", _searchByGlobalName.entity().getDataCenterId(), SearchCriteria.Op.NULL);
        _searchByGlobalName.and("podId", _searchByGlobalName.entity().getPodId(), SearchCriteria.Op.NULL);
        _searchByGlobalName.and("clusterId", _searchByGlobalName.entity().getClusterId(), SearchCriteria.Op.NULL);
        _searchByGlobalName.and("hostId", _searchByGlobalName.entity().getHostId(), SearchCriteria.Op.NULL);
        _searchByGlobalName.and("name", _searchByGlobalName.entity().getName(), SearchCriteria.Op.EQ);
        _searchByGlobalName.done();
        
        _searchByDcIdName = createSearchBuilder();
        _searchByDcIdName.and("dcId", _searchByDcIdName.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        _searchByDcIdName.and("podId", _searchByDcIdName.entity().getPodId(), SearchCriteria.Op.NULL);
        _searchByDcIdName.and("clusterId", _searchByDcIdName.entity().getClusterId(), SearchCriteria.Op.NULL);
        _searchByDcIdName.and("hostId", _searchByDcIdName.entity().getHostId(), SearchCriteria.Op.NULL);
        _searchByDcIdName.and("name", _searchByDcIdName.entity().getName(), SearchCriteria.Op.EQ);
        _searchByDcIdName.done();
        
        _searchByDcIDPodIdName = createSearchBuilder();
        _searchByDcIDPodIdName.and("dcId", _searchByDcIDPodIdName.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        _searchByDcIDPodIdName.and("podId", _searchByDcIDPodIdName.entity().getPodId(), SearchCriteria.Op.EQ);
        _searchByDcIDPodIdName.and("clusterId", _searchByDcIDPodIdName.entity().getClusterId(), SearchCriteria.Op.NULL);
        _searchByDcIDPodIdName.and("hostId", _searchByDcIDPodIdName.entity().getHostId(), SearchCriteria.Op.NULL);
        _searchByDcIDPodIdName.and("name", _searchByDcIDPodIdName.entity().getName(), SearchCriteria.Op.EQ);
        _searchByDcIDPodIdName.done();
        
        _searchByDcIDPodIdClusterIdName = createSearchBuilder();
        _searchByDcIDPodIdClusterIdName.and("dcId", _searchByDcIDPodIdClusterIdName.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        _searchByDcIDPodIdClusterIdName.and("podId", _searchByDcIDPodIdClusterIdName.entity().getPodId(), SearchCriteria.Op.EQ);
        _searchByDcIDPodIdClusterIdName.and("clusterId", _searchByDcIDPodIdClusterIdName.entity().getClusterId(), SearchCriteria.Op.EQ);
        _searchByDcIDPodIdClusterIdName.and("hostId", _searchByDcIDPodIdClusterIdName.entity().getHostId(), SearchCriteria.Op.NULL);
        _searchByDcIDPodIdClusterIdName.and("name", _searchByDcIDPodIdClusterIdName.entity().getName(), SearchCriteria.Op.EQ);
        _searchByDcIDPodIdClusterIdName.done();
        
        _searchByDcIDPodIdClusterIdHostIdName = createSearchBuilder();
        _searchByDcIDPodIdClusterIdHostIdName.and("dcId", _searchByDcIDPodIdClusterIdHostIdName.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        _searchByDcIDPodIdClusterIdHostIdName.and("podId", _searchByDcIDPodIdClusterIdHostIdName.entity().getPodId(), SearchCriteria.Op.EQ);
        _searchByDcIDPodIdClusterIdHostIdName.and("clusterId", _searchByDcIDPodIdClusterIdHostIdName.entity().getClusterId(), SearchCriteria.Op.EQ);
        _searchByDcIDPodIdClusterIdHostIdName.and("hostId", _searchByDcIDPodIdClusterIdHostIdName.entity().getHostId(), SearchCriteria.Op.EQ);
        _searchByDcIDPodIdClusterIdHostIdName.and("name", _searchByDcIDPodIdClusterIdHostIdName.entity().getName(), SearchCriteria.Op.EQ);
        _searchByDcIDPodIdClusterIdHostIdName.done();
    }
    @Override
    public MockConfigurationVO findByCommand(Long dcId, Long podId, Long clusterId, Long hostId, String name) {
        
        if (dcId == null) {
            SearchCriteria<MockConfigurationVO> sc = _searchByGlobalName.create();
            sc.setParameters("name", name);
            return findOneBy(sc);
        } else if (podId == null) {
            SearchCriteria<MockConfigurationVO> sc = _searchByDcIdName.create();
            sc.setParameters("name", name);
            sc.setParameters("dcId", dcId);
            return findOneBy(sc);
        } else if (clusterId == null) {
            SearchCriteria<MockConfigurationVO> sc = _searchByDcIDPodIdName.create();
            sc.setParameters("name", name);
            sc.setParameters("dcId", dcId);
            sc.setParameters("podId", podId);
            return findOneBy(sc);
        } else if (hostId == null) {
            SearchCriteria<MockConfigurationVO> sc = _searchByDcIDPodIdClusterIdName.create();
            sc.setParameters("name", name);
            sc.setParameters("dcId", dcId);
            sc.setParameters("podId", podId);
            sc.setParameters("clusterId", clusterId);
            return findOneBy(sc);
        } else {
            SearchCriteria<MockConfigurationVO> sc = _searchByDcIDPodIdClusterIdHostIdName.create();
            sc.setParameters("name", name);
            sc.setParameters("dcId", dcId);
            sc.setParameters("podId", podId);
            sc.setParameters("clusterId", clusterId);
            sc.setParameters("hostId", hostId);
            return findOneBy(sc);
        }
    }
    
    @Override
    public MockConfigurationVO findByNameBottomUP(Long dcId, Long podId, Long clusterId, Long hostId, String name) {
    	 Transaction txn = Transaction.currentTxn();
    	StringBuilder search = new StringBuilder();
    	Formatter formatter = new Formatter(search);
    	formatter.format("select * from mockconfiguration where (name='%s') and ((data_center_id = %d and pod_id = %d and cluster_id = %d and host_id = %d)", name, dcId, podId, clusterId, hostId);
    	formatter.format(" or (data_center_id = %d and pod_id = %d and cluster_id = %d and host_id is null)", dcId, podId, clusterId);
    	formatter.format(" or (data_center_id = %d and pod_id = %d and cluster_id is null and host_id is null)", dcId, podId);
    	formatter.format(" or (data_center_id = %d and pod_id is null and cluster_id is null and host_id is null)", dcId);
    	formatter.format(" or (data_center_id is null and pod_id is null and cluster_id is null and host_id is null)) LIMIT 1");
    	
    	PreparedStatement pstmt = null;
		try {
			String sql = search.toString();
			pstmt = txn.prepareAutoCloseStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
                return toEntityBean(rs, false);
            }
		} catch (Exception e) {
			
		}
		return null;
    } 

}
