package com.cloud.updates.dao;

import com.cloud.updates.HostUpdatesVO;
import com.cloud.utils.db.GenericDao;

public interface HostUpdatesDao extends GenericDao<HostUpdatesVO, Long> {
    /**
     * search for available updates for a host
     * @param id of patch
     * @param hostId
     * @return list of patches
     */
    HostUpdatesVO searchForUpdates(Long id, Long hostId);
    /**
     * search for a patch
     * @param uuid of patch
     * @param 
     * @return patch
     */
    HostUpdatesVO findByUUID(String uuid);
}
