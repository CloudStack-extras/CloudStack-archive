package com.cloud.host.updates;

import java.util.List;

import com.cloud.api.commands.ListHostUpdatesCmd;
import com.cloud.host.updates.HostUpdatesRef;

public interface HostUpdatesService {
    /**
     * Searches for Host updates
     * @param hostupdatesCommand
     * @return List of Host updates
     */
    List<? extends HostUpdatesRef> searchForHostUpdates(ListHostUpdatesCmd cmd);
}
