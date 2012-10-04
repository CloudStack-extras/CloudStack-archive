package com.cloud.host.updates.dao;

import java.util.List;

import javax.ejb.Local;

import com.cloud.host.updates.HostUpdatesRefVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

@Local(value = { HostUpdatesRefDao.class })
public class HostUpdatesRefDaoImpl extends GenericDaoBase<HostUpdatesRefVO, Long> implements HostUpdatesRefDao {
    protected final SearchBuilder<HostUpdatesRefVO> HostSearch;

    protected HostUpdatesRefDaoImpl() {
        HostSearch = createSearchBuilder();
        HostSearch.and("hostId", HostSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        HostSearch.done();
    }

    @Override
    public List<HostUpdatesRefVO> searchByHostId(Long hostId, Boolean isApplied) {
        SearchCriteria<HostUpdatesRefVO> sc = createSearchCriteria();

        sc.addAnd("hostId", SearchCriteria.Op.EQ, String.valueOf(hostId));
        if(isApplied != null) {
            sc.addAnd("isApplied", SearchCriteria.Op.EQ, Boolean.valueOf(isApplied));
        }
        return listBy(sc);
    }

    @Override
    public void deletePatchRef(long hostId) {
        SearchCriteria<HostUpdatesRefVO> sc = HostSearch.create();
        sc.setParameters("hostId", hostId);

        List<HostUpdatesRefVO> results = search(sc, null);
        for (HostUpdatesRefVO result : results) {
            remove(result.getId());
        }
    }

    @Override
    public HostUpdatesRefVO findUpdate(Long hostId, Long patchId) {
        SearchCriteria<HostUpdatesRefVO> sc = createSearchCriteria();

        sc.addAnd("hostId", SearchCriteria.Op.EQ, Long.valueOf(hostId));
        sc.addAnd("patchId", SearchCriteria.Op.EQ, String.valueOf(patchId));

        List<HostUpdatesRefVO> updates = listBy(sc);
        if ((updates != null) && !updates.isEmpty()) {
            return updates.get(0);
        }
        return null;
    }
}

