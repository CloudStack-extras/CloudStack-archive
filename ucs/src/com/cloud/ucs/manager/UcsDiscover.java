package com.cloud.ucs.manager;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.api.ApiConstants;
import com.cloud.exception.DiscoveryException;
import com.cloud.host.HostVO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.Discoverer;
import com.cloud.resource.DiscovererBase;
import com.cloud.resource.ResourceManager;
import com.cloud.resource.ResourceStateAdapter;
import com.cloud.resource.ServerResource;
import com.cloud.resource.UnableDeleteHostException;
import com.cloud.utils.component.Inject;
@Local(value = Discoverer.class)
public class UcsDiscover extends DiscovererBase implements ResourceStateAdapter{
    @Inject
    private ResourceManager resourceMgr;
    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        resourceMgr.registerResourceStateAdapter("UcsDiscover", this);
        return true;
    }
    
    @Override
    public Map<? extends ServerResource, Map<String, String>> find(long dcId, Long podId, Long clusterId, URI uri, String username, String password,
            List<String> hostTags) throws DiscoveryException {
        try {
            Map<UcsResourceBase, Map<String, String>> ret = new HashMap<UcsResourceBase, Map<String, String>>();
            UcsResourceBase resource = new UcsResourceBase();
            Map params = new HashMap();
            params.put(ApiConstants.ZONE_ID, String.valueOf(dcId));
            params.put(ApiConstants.POD_ID, String.valueOf(podId));
            params.put(ApiConstants.CLUSTER_ID, String.valueOf(clusterId));
            params.put(ApiConstants.UUID, UUID.randomUUID().toString());
            params.put(ApiConstants.PRIVATE_IP, "0.0.0.0");
            params.put(ApiConstants.PASSWORD, password);
            resource.configure("UcsResource", params);
            resource.start();
            
            Map<String, String> details = new HashMap<String, String>(params.size());
            details.putAll(params);
            ret.put(resource, details);
            return ret;
        } catch (Exception e) {
            throw new DiscoveryException(e.getMessage(), e);
        }
    }

    @Override
    public void postDiscovery(List<HostVO> hosts, long msId) throws DiscoveryException {
    }

    @Override
    public boolean matchHypervisor(String hypervisor) {
        return HypervisorType.ManagedHost.toString().equals(hypervisor);
    }

    @Override
    public HypervisorType getHypervisorType() {
        return HypervisorType.ManagedHost;
    }

    @Override
    public HostVO createHostVOForConnectedAgent(HostVO host, StartupCommand[] cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HostVO createHostVOForDirectConnectAgent(HostVO host, StartupCommand[] startup, ServerResource resource, Map<String, String> details,
            List<String> hostTags) {
        StartupCommand firstCmd = startup[0];
        if (!(firstCmd instanceof StartupRoutingCommand)) {
            return null;
        }

        StartupRoutingCommand ssCmd = ((StartupRoutingCommand) firstCmd);
        if (ssCmd.getHypervisorType() != HypervisorType.ManagedHost) {
            return null;
        }
        
        return resourceMgr.fillRoutingHostVO(host, ssCmd, HypervisorType.ManagedHost, details, hostTags);
    }

    @Override
    public DeleteHostAnswer deleteHost(HostVO host, boolean isForced, boolean isForceDeleteStorage) throws UnableDeleteHostException {
        return new DeleteHostAnswer(true);
    }
}
