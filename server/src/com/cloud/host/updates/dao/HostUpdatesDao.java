package com.cloud.host.updates.dao;

import com.cloud.host.updates.HostUpdatesVO;
import com.cloud.utils.db.GenericDao;

public interface HostUpdatesDao extends GenericDao<HostUpdatesVO, Long> {
    HostUpdatesVO searchForUpdates(Long id, Long hostId);
    HostUpdatesVO findByUUID(String uuid);
}
