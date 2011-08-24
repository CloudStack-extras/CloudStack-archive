package com.cloud.simulator.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import com.cloud.simulator.MockHostVO;
import com.cloud.simulator.MockVMVO;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

@Local(value={MockVMDao.class})
public class MockVMDaoImpl extends GenericDaoBase<MockVMVO, Long> implements MockVMDao {
    protected SearchBuilder<MockVMVO> GuidSearch;  
    protected SearchBuilder<MockVMVO> vmNameSearch;
    protected SearchBuilder<MockVMVO> vmhostSearch;
    @Inject MockHostDao _mockHostDao;
    @Override
    public List<MockVMVO> findByHostId(long hostId) {
        return new ArrayList<MockVMVO>();
    }

    @Override
    public MockVMVO findByVmName(String vmName) {
        SearchCriteria<MockVMVO> sc = vmNameSearch.create();
        sc.setParameters("name", vmName);
        return findOneBy(sc);
    }

    @Override
    public List<MockVMVO> findByHostGuid(String guid) {
        SearchCriteria<MockVMVO> sc = GuidSearch.create();
        sc.setJoinParameters("host", "guid", guid);
        return listBy(sc);
    }
    
    @Override
    public MockVMVO findByVmNameAndHost(String vmName, String hostGuid) {
        SearchCriteria<MockVMVO> sc = vmhostSearch.create();
        sc.setJoinParameters("host", "guid", hostGuid);
        sc.setParameters("name", vmName);
        return findOneBy(sc);
    }
    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        SearchBuilder<MockHostVO> host = _mockHostDao.createSearchBuilder();
        host.and("guid", host.entity().getGuid(), SearchCriteria.Op.EQ);
        
        GuidSearch = createSearchBuilder();
        GuidSearch.join("host", host, host.entity().getId(), GuidSearch.entity().getHostId(), JoinBuilder.JoinType.INNER);
        GuidSearch.done();
        
        vmNameSearch = createSearchBuilder();
        vmNameSearch.and("name", vmNameSearch.entity().getName(), SearchCriteria.Op.EQ);
        vmNameSearch.done();
        
        SearchBuilder<MockHostVO> newhost = _mockHostDao.createSearchBuilder();
        newhost.and("guid", newhost.entity().getGuid(), SearchCriteria.Op.EQ);
        vmhostSearch = createSearchBuilder();
        vmhostSearch.and("name", vmhostSearch.entity().getName(), SearchCriteria.Op.EQ);
        vmhostSearch.join("host", newhost, newhost.entity().getId(), vmhostSearch.entity().getHostId(), JoinBuilder.JoinType.INNER);
        vmhostSearch.done();
        
        return true;
    }
}
