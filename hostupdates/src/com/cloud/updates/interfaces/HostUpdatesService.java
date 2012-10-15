package com.cloud.updates.interfaces;

import java.util.List;

import com.cloud.api.commands.ListHostUpdatesCmd;
import com.cloud.api.commands.ListHostsWithPendingUpdatesCmd;
import com.cloud.api.response.HostUpdatesResponse;
import com.cloud.api.response.HostsWithPendingUpdatesResponse;
import com.cloud.utils.component.PluggableService;

public interface HostUpdatesService extends PluggableService {
    /**
     * Searches for Host updates
     * @param hostupdatesCommand
     * @return List of Host updates
     */
	
    List<? extends HostUpdatesRef> searchForHostUpdates(ListHostUpdatesCmd cmd);
    List<Long> searchForHosts(ListHostsWithPendingUpdatesCmd cmd);
    
    HostUpdatesResponse createHostUpdatesResponse(HostUpdatesRef update);
    HostsWithPendingUpdatesResponse createHostsWithPendingUpdatesResponse(Long update);
}
