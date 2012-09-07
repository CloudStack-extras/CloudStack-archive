package com.cloud.host.updates.dao;

import java.util.List;

import javax.ejb.Local;

import com.cloud.host.updates.HostUpdatesVO;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchCriteria;


@Local(value = { HostUpdatesDao.class })
public class HostUpdatesDaoImpl extends GenericDaoBase<HostUpdatesVO, Long> implements HostUpdatesDao {
    @Override
    public HostUpdatesVO searchForUpdates(Long id,Long hostId) {
        Filter searchFilter = new Filter(HostUpdatesVO.class, "timestamp", Boolean.FALSE, Long.valueOf(0), Long.valueOf(1));
        SearchCriteria<HostUpdatesVO> sc = createSearchCriteria();
        
        if(id != null)
        sc.addAnd("id", SearchCriteria.Op.EQ, String.valueOf(id));
        
        if(hostId != null)
        sc.addAnd("hostIP", SearchCriteria.Op.EQ, Long.valueOf(hostId));
        
        List<HostUpdatesVO> updates = listBy(sc, searchFilter);
        if ((updates != null) && !updates.isEmpty()) {
            return updates.get(0);
        }
        return null;
    }   
    
    @Override
    public HostUpdatesVO findByUUID(String uuid){
    	SearchCriteria<HostUpdatesVO> sc = createSearchCriteria();

        sc.addAnd("uuid", SearchCriteria.Op.EQ, String.valueOf(uuid));
        
        List<HostUpdatesVO> updates = listBy(sc);
        if ((updates != null) && !updates.isEmpty()) {
            return updates.get(0);
        }
        return null;
    }
}
