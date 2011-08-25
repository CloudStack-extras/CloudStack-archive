package com.cloud.resource;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.cloud.host.HostVO;

public interface ResourceListener {
    static final Integer EVENT_DISCOVER_BEFORE = 0x1;
    static final Integer EVENT_DISCOVER_AFTER = 0x1 << 1;
    static final Integer EVENT_DELETE_HOST_BEFORE = 0x1 << 2;
    static final Integer EVENT_DELETE_HOST_AFTER = 0x1 << 3;
    static final Integer EVENT_CANCEL_MAINTENANCE_BEFORE = 0x1 << 4;
    static final Integer EVENT_CANCEL_MAINTENANCE_AFTER = 0x1 << 5;
    static final Integer EVENT_PREPARE_MAINTENANCE_BEFORE = 0x1 << 6;
    static final Integer EVENT_PREPARE_MAINTENANCE_AFTER = 0x1 << 7;
    static final Integer EVENT_ALL = (EVENT_DISCOVER_BEFORE | EVENT_DISCOVER_AFTER | EVENT_DELETE_HOST_BEFORE | EVENT_DELETE_HOST_AFTER
            | EVENT_CANCEL_MAINTENANCE_BEFORE | EVENT_CANCEL_MAINTENANCE_AFTER | EVENT_PREPARE_MAINTENANCE_BEFORE | EVENT_PREPARE_MAINTENANCE_AFTER);
    
    /**
     * 
     * @param dcid
     * @param podId
     * @param clusterId
     * @param uri
     * @param username
     * @param password
     * @param hostTags
     * 
     * Called before Discover.find()
     */
    void processDiscoverEventBefore(Long dcid, Long podId, Long clusterId, URI uri, String username, String password, List<String> hostTags);

    /**
     * 
     * @param resources
     * 
     * Called after Discover.find()
     */
    void processDiscoverEventAfter(Map<? extends ServerResource, Map<String, String>> resources);

    /**
     * 
     * @param host
     * 
     * Called before host delete
     */
    void processDeleteHostEventBefore(HostVO host);

    /**
     * 
     * @param host
     * 
     * Called after host delete. NOTE param host includes stale data which has been removed from database
     */
    void processDeletHostEventAfter(HostVO host);

    /**
     * 
     * @param hostId
     * 
     * Called before AgentManager.cancelMaintenance
     */
    void processCancelMaintenaceEventBefore(Long hostId);

    /**
     * 
     * @param hostId
     * 
     * Called after AgentManager.cancelMaintenance
     */
    void processCancelMaintenaceEventAfter(Long hostId);

    /**
     * 
     * @param hostId
     * 
     * Called before AgentManager.main
     */
    void processPrepareMaintenaceEventBefore(Long hostId);

    /**
     * 
     * @param hostId
     * 
     * Called after AgentManager.main
     */
    void processPrepareMaintenaceEventAfter(Long hostId);
}
