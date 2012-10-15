package com.cloud.updates.manager;

import com.cloud.utils.component.Manager;

public interface HostUpdatesManager extends Manager {
    /**
     * Update the database with the updates available for hosts.
     * 
     * @return none
     */
    public void updateHosts();
}
