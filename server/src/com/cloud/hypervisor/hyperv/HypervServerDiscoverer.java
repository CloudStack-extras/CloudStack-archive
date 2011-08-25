/**
 * *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
*
 *
 * This software is licensed under the GNU General Public License v3 or later.
 *
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.cloud.hypervisor.hyperv;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupVMMAgentCommand;
import com.cloud.agent.transport.Request;
import com.cloud.alert.AlertManager;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.exception.DiscoveryException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.hyperv.resource.HypervDummyResourceBase;
import com.cloud.resource.Discoverer;
import com.cloud.resource.DiscovererBase;
import com.cloud.resource.ServerResource;
import com.cloud.utils.component.Inject;
import com.cloud.utils.nio.HandlerFactory;
import com.cloud.utils.nio.Link;
import com.cloud.utils.nio.NioClient;
import com.cloud.utils.nio.Task;
import com.cloud.utils.nio.Task.Type;

@Local(value=Discoverer.class)
public class HypervServerDiscoverer extends DiscovererBase implements Discoverer, HandlerFactory{
    private static final Logger s_logger = Logger.getLogger(HypervServerDiscoverer.class);
    private int _waitTime = 1;

    @Inject ClusterDao _clusterDao;
    @Inject AlertManager _alertMgr;
    @Inject ClusterDetailsDao _clusterDetailsDao;
    @Inject HostDao _hostDao = null;
    Link _link;

    @SuppressWarnings("static-access")
    @Override
    public Map<? extends ServerResource, Map<String, String>> find(long dcId, Long podId, Long clusterId, URI url,
            String username, String password, List<String> hostTags) throws DiscoveryException {

        if(s_logger.isInfoEnabled()) {
            s_logger.info("Discover host. dc: " + dcId + ", pod: " + podId + ", cluster: " + clusterId + ", uri host: " + url.getHost());
        }

        if(podId == null) {
            if(s_logger.isInfoEnabled()) {
                s_logger.info("No pod is assigned, skipping the discovery in Hyperv discoverer");
            }
            return null;
        }

        if (!url.getScheme().equals("http")) {
            String msg = "urlString is not http so HypervServerDiscoverer taking care of the discovery for this: " + url;
            s_logger.debug(msg);
            return null;
        }

        ClusterVO cluster = _clusterDao.findById(clusterId);
        if(cluster == null || cluster.getHypervisorType() != HypervisorType.Hyperv) {
            if(s_logger.isInfoEnabled()) {
                s_logger.info("invalid cluster id or cluster is not for Hyperv hypervisors");
            }
            return null;
        }
        String clusterName = cluster.getName();

        try {
        	
    		String hostname = url.getHost();
    		InetAddress ia = InetAddress.getByName(hostname);
    		String agentIp = ia.getHostAddress();
    		String guid = UUID.nameUUIDFromBytes(agentIp.getBytes()).toString();
    		String guidWithTail = guid + "-HypervResource";/*tail added by agent.java*/
    		if (_hostDao.findByGuid(guidWithTail) != null) {
    			s_logger.debug("Skipping " + agentIp + " because " + guidWithTail + " is already in the database.");
    			return null;
    		}
        	
	        // bootstrap SCVMM agent to connect back to management server
	        NioClient _connection = new NioClient("HypervAgentClient", url.getHost(), 9000, 1, this);  
	        _connection.start();

	        StartupVMMAgentCommand cmd = new StartupVMMAgentCommand(
	        		dcId,
	        		podId,
	        		clusterName,
	        		guid,
	        		InetAddress.getLocalHost().getHostAddress(),
	        		"8250",
	        		HypervServerDiscoverer.class.getPackage().getImplementationVersion());
	        
	        // send bootstrap command to agent running on SCVMM host	
        	s_logger.info("sending bootstrap request to SCVMM agent on host "+ url.getHost());
	        Request request = new Request(0, 0, cmd, false);

	        // :FIXME without sleep link.send failing why??????
	        Thread.currentThread().sleep(5000);
        	_link.send(request.toBytes());

	        //wait for SCVMM agent to connect back
			HostVO connectedHost = waitForHostConnect(dcId, podId, clusterId, guidWithTail);
			if (connectedHost == null)
			{
				s_logger.info("SCVMM agent did not connect back after sending bootstrap request"); 
				return null;
			}

	        //disconnect
			s_logger.info("SCVMM agent connected back after sending bootstrap request"); 
	        _connection.stop();        
	
	        Map<HypervDummyResourceBase, Map<String, String>> resources = new HashMap<HypervDummyResourceBase, Map<String, String>>();
	        Map<String, String> details = new HashMap<String, String>();
	        Map<String, Object> params = new HashMap<String, Object>();
	        HypervDummyResourceBase resource = new HypervDummyResourceBase(); 
	
	        details.put("url", url.getHost());
	        details.put("username", username);
	        details.put("password", password);
			resources.put(resource, details);
	
	        params.put("zone", Long.toString(dcId));
	        params.put("pod", Long.toString(podId));
	        params.put("cluster", Long.toString(clusterId));

            resource.configure("Hyperv", params);
            return resources;
        } catch (ConfigurationException e) {
            _alertMgr.sendAlert(AlertManager.ALERT_TYPE_HOST, dcId, podId, "Unable to add " + url.getHost(), "Error is " + e.getMessage());
            s_logger.warn("Unable to instantiate " + url.getHost(), e);
        } catch (UnknownHostException e) {
            _alertMgr.sendAlert(AlertManager.ALERT_TYPE_HOST, dcId, podId, "Unable to add " + url.getHost(), "Error is " + e.getMessage());
            s_logger.warn("Unable to instantiate " + url.getHost(), e);
        } catch (Exception e) {
            s_logger.info("exception " + e.toString());
        }
        return null;
    }

    @Override
    public void postDiscovery(List<HostVO> hosts, long msId) {
        // do nothing
    }

    @Override
    public boolean matchHypervisor(String hypervisor) {
        if(hypervisor == null) {
            return true;
        }

        return Hypervisor.HypervisorType.VMware.toString().equalsIgnoreCase(hypervisor);
    }

    @Override
    public Hypervisor.HypervisorType getHypervisorType() {
        return Hypervisor.HypervisorType.Hyperv;
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        return true;
    }

    @Override
    public Task create(Type type, Link link, byte[] data) {
        _link = link;
        return new BootStrapTakHandler(type, link, data);
    }

    private HostVO waitForHostConnect(long dcId, long podId, long clusterId, String guid) {
        for (int i = 0; i < _waitTime *2; i++) {
            List<HostVO> hosts = _hostDao.listBy(Host.Type.Routing, clusterId, podId, dcId);
            for (HostVO host : hosts) {
                if (host.getGuid().equalsIgnoreCase(guid)) {
                    return host;
                }
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                s_logger.debug("Failed to sleep: " + e.toString());
            }
        }
        s_logger.debug("Timeout, to wait for the host connecting to mgt svr, assuming it is failed");
        return null;
    }

    // class to handle the bootstrap command from the management server
    public class BootStrapTakHandler extends Task {

        public BootStrapTakHandler(Task.Type type, Link link, byte[] data) {
            super(type, link, data);
            s_logger.info("created new BootStrapTakHandler");
        }

        protected void processRequest(final Link link, final Request request) {
            final Command[] cmds = request.getCommands();
            Command cmd = cmds[0];
        }

        @Override
        protected void doTask(Task task) throws Exception {
            final Type type = task.getType();
            s_logger.info("recieved task of type "+type.toString() +" in BootStrapTakHandler");
        }
    }
}


