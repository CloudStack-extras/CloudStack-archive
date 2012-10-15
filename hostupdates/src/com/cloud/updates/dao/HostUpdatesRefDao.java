package com.cloud.updates.dao;

import java.util.List;

import com.cloud.updates.HostUpdatesRefVO;
import com.cloud.utils.db.GenericDao;

public interface HostUpdatesRefDao extends GenericDao<HostUpdatesRefVO, Long>{
    /**
     * search for patches
     * @param hostId
     * @param if patch is applied or not
     * @return list of patch ref
     */
    List<HostUpdatesRefVO> searchByHostId(Long hostId, Boolean isApplied);
    /**
     * search for a patch
     * @param patchId
     * @param hostId
     * @return a patch ref
     */
    HostUpdatesRefVO findUpdate(Long hostId, Long patchId);
    /**
     * delete the patch ref entries based on the hostId
     * @param hostId
     * @return
     */
    List<Long> listHosts();
    void deletePatchRef(long hostId);
}