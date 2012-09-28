package com.cloud.maint;

import com.cloud.utils.component.Adapter;

public interface HostUpdatesMonitor extends Adapter {

    /**
     * Update the database with the updates available for hosts.
     * 
     * @return none
     */
    public void updateHosts();
}
