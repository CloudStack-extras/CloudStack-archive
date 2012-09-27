package com.cloud.host.updates.dao;

import java.util.List;

import com.cloud.host.updates.HostUpdatesRefVO;
import com.cloud.utils.db.GenericDao;

public interface HostUpdatesRefDao extends GenericDao<HostUpdatesRefVO, Long>{
    List<HostUpdatesRefVO> searchByHostId(Long hostId, Boolean isApplied);
    HostUpdatesRefVO searchByPatchId(Long patchId);
    HostUpdatesRefVO findUpdate(Long hostId, Long patchId);
    void deletePatchRef(long hostId);
}
