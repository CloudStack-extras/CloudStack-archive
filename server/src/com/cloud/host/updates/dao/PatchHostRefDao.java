package com.cloud.host.updates.dao;

import java.util.List;

import com.cloud.host.updates.PatchHostVO;
import com.cloud.utils.db.GenericDao;

public interface PatchHostRefDao extends GenericDao<PatchHostVO, Long>{
    List<PatchHostVO> searchByHostId(Long hostId, Boolean isApplied);
    PatchHostVO searchByPatchId(Long patchId);
    PatchHostVO findUpdate(Long hostId, Long patchId);
    void deletePatchRef(long hostId);
}
