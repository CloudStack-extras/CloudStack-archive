/**
 *  Copyright (C) 2011 Cloud.com.  All rights reserved.
 *
 * This software is licensed under the GNU General Public License v3 or later. 
 *
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later
version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.cloud.hypervisor;

import java.text.ParseException;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.HostCreator;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.agent.manager.authn.AgentAuthnException;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.exception.ConnectionException;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.utils.component.Inject;

/**
 * Creates a host record and supporting records such as pod and ip address
 *
 */
@Local(value=HostCreator.class)
public class SelfConfiguringHostCreator implements HostCreator {
    private static final Logger s_logger = Logger.getLogger(SelfConfiguringHostCreator.class);
    @Inject ClusterDao _clusterDao = null;
    @Inject ConfigurationDao _configDao = null;
    @Inject DataCenterDao _zoneDao = null;
    @Inject HostDao _hostDao = null;
    @Inject HostPodDao _podDao = null;

    @Inject AgentManager _agentManager = null;
   

    @Override
    public boolean configure(String name, Map<String, Object> params)
            throws ConfigurationException {
        _agentManager.registerForInitialConnects(this, false);
        return true;
    }

    
    @Override
    public String getName() {
        return getClass().getName();
    }

 
    @Override
    public boolean start() {
       return true;
    }


    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public boolean processInitialConnect(StartupCommand[] cmd)
            throws ConnectionException {
        StartupCommand startup = cmd[0];
        if (startup instanceof StartupRoutingCommand) {
            StartupRoutingCommand ssCmd = ((StartupRoutingCommand) startup);
            Type type = Host.Type.Routing;
            final Map<String, String> hostDetails = ssCmd.getHostDetails();
            HostVO server = _hostDao.findByGuid(startup.getGuid());
            if (server == null) {
                server = _hostDao.findByGuid(startup.getGuidWithoutResource());
            }
            if (server != null && server.getRemoved() == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Found the host " + server.getId() + " by guid: "
                            + startup.getGuid());
                }
                
            } else {
                server = new HostVO(startup.getGuid());
            }
            server.setDetails(hostDetails);
            
            try {
                updateHost(server, startup, type);
            } catch (AgentAuthnException e) {
                throw new ConnectionException(true, "Failed to authorize host, invalid configuration", e);
            }
            server = _hostDao.persist(server);
            return true;
        }
        return false;
    }
    
    protected void updateHost(final HostVO host, final StartupCommand startup,
            final Host.Type type) throws AgentAuthnException {
        s_logger.debug("updateHost() called");

        String dataCenter = startup.getDataCenter();
        String pod = startup.getPod(); //FIXME: always deduce this and ignore if passed in
        String cluster = startup.getCluster(); //FIXME: always figure this out, or ignore

        
        DataCenterVO zone = _zoneDao.findByName(dataCenter); //TODO: use secure zone token
        if (zone == null) {
            try {
                long zoneId = Long.parseLong(dataCenter);
                zone = _zoneDao.findById(zoneId);
                zone.getDetails();
            } catch (NumberFormatException nfe) {
                throw new AgentAuthnException("Could not find zone for agent in datacenter with token " + dataCenter);
            }
        }
        
        HostPodVO p = _podDao.findByName(pod, zone.getId()); //TODO: DAO needs lookup by private cidr and zone

        if (p == null) {//TODO: autocreate pod and cluster
            try {
                long podId = Long.parseLong(pod);
                p = _podDao.findById(podId);
                if (pod == null) {
                    throw new AgentAuthnException("Could not find pod for agent in datacenter with token " + dataCenter);
                }
            } catch (NumberFormatException nfe) {
                throw new AgentAuthnException("Could not find pod for agent in datacenter with token " + dataCenter);
            }
        } 
        
        Long clusterId = null;
        if (cluster != null) {
            try {
                clusterId = Long.parseLong(cluster);
                if (_clusterDao.findById(clusterId) == null) {
                    throw new AgentAuthnException("Could not find cluster for agent in datacenter with token " + dataCenter);
                }
            } catch (NumberFormatException nfe) {
                throw new AgentAuthnException("Could not find cluster for agent in datacenter with token " + dataCenter);
            }
        }
        

        host.setDataCenterId(zone.getId());
        host.setPodId(p.getId());
        host.setClusterId(clusterId);
        host.setPrivateIpAddress(startup.getPrivateIpAddress());
        host.setPrivateNetmask(startup.getPrivateNetmask());
        host.setPrivateMacAddress(startup.getPrivateMacAddress());
        host.setPublicIpAddress(startup.getPublicIpAddress());
        host.setPublicMacAddress(startup.getPublicMacAddress());
        host.setPublicNetmask(startup.getPublicNetmask());
        host.setStorageIpAddress(startup.getStorageIpAddress());
        host.setStorageMacAddress(startup.getStorageMacAddress());
        host.setStorageNetmask(startup.getStorageNetmask());
        host.setVersion(startup.getVersion());
        host.setName(startup.getName());
        host.setType(type);
        host.setStorageUrl(startup.getIqn());
        host.setLastPinged(System.currentTimeMillis() >> 10);
        final StartupRoutingCommand scc = (StartupRoutingCommand) startup;
        host.setCaps(scc.getCapabilities());
        host.setCpus(scc.getCpus());
        host.setTotalMemory(scc.getMemory());
        host.setSpeed(scc.getSpeed());
        HypervisorType hyType = scc.getHypervisorType();
        host.setHypervisorType(hyType);

    }

}
