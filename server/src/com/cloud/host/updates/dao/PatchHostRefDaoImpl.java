package com.cloud.host.updates.dao;

import java.util.List;

import javax.ejb.Local;

import com.cloud.host.updates.PatchHostVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

@Local(value = { PatchHostRefDao.class })
public class PatchHostRefDaoImpl extends GenericDaoBase<PatchHostVO, Long> implements PatchHostRefDao {
    protected final SearchBuilder<PatchHostVO> HostSearch;

    protected PatchHostRefDaoImpl() {
        HostSearch = createSearchBuilder();
        HostSearch.and("hostId", HostSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        HostSearch.done();
    }

    @Override
    public List<PatchHostVO> searchByHostId(Long hostId, Boolean isApplied) {
        SearchCriteria<PatchHostVO> sc = createSearchCriteria();

        sc.addAnd("hostId", SearchCriteria.Op.EQ, String.valueOf(hostId));

        if(isApplied != null) {
            sc.addAnd("isApplied", SearchCriteria.Op.EQ, Boolean.valueOf(isApplied));
        }
        return listBy(sc);
    }    

    @Override
    public PatchHostVO searchByPatchId(Long patchId) {
        SearchCriteria<PatchHostVO> sc = createSearchCriteria();

        if(patchId != null) {
            sc.addAnd("patchId", SearchCriteria.Op.EQ, String.valueOf(patchId));
        }
        List<PatchHostVO> updates = listBy(sc);

        if ((updates != null) && !updates.isEmpty()) {
            return updates.get(0);
        }
        return null;
    }

    @Override
    public void deletePatchRef(long hostId) {
        SearchCriteria<PatchHostVO> sc = HostSearch.create();
        sc.setParameters("hostId", hostId);

        List<PatchHostVO> results = search(sc, null);
        for (PatchHostVO result : results) {
            remove(result.getId());
        }
    }

    @Override
    public PatchHostVO findUpdate(Long hostId, Long patchId) {
        SearchCriteria<PatchHostVO> sc = createSearchCriteria();

        sc.addAnd("hostId", SearchCriteria.Op.EQ, Long.valueOf(hostId));
        sc.addAnd("patchId", SearchCriteria.Op.EQ, String.valueOf(patchId));

        List<PatchHostVO> updates = listBy(sc);
        if ((updates != null) && !updates.isEmpty()) {
            return updates.get(0);
        }
        return null;
    }
}

