package com.cloud.maint;

import com.cloud.utils.component.Adapter;

public interface HostUpdatesMonitor extends Adapter{

    public void updateHosts(long serverId);
}
