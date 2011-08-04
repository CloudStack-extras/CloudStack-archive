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
package com.cloud.resource;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.AgentResourceBase;
import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.manager.SimulatorManager;
import com.cloud.agent.manager.SimulatorManagerImpl;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.exception.ConnectionException;
import com.cloud.exception.DiscoveryException;
import com.cloud.resource.Discoverer;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.VMTemplateZoneVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateHostDao;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.storage.VMTemplateStorageResourceAssoc;
import com.cloud.utils.component.Inject;

/**
 * @author prasanna
 * 
 */
@Local(value = Discoverer.class)
public class SimulatorDiscoverer extends DiscovererBase implements Discoverer, Listener {
	private static final Logger s_logger = Logger
			.getLogger(SimulatorDiscoverer.class);
	
	@Inject HostDao _hostDao;
	@Inject VMTemplateDao _vmTemplateDao;
    @Inject VMTemplateHostDao _vmTemplateHostDao;
    @Inject VMTemplateZoneDao _vmTemplateZoneDao;
    @Inject ClusterDao _clusterDao;
    @Inject AgentManager _agentMgr = null;
    @Inject SimulatorManager _simMgr = null;
    
	/**
	 * Finds ServerResources of an in-process simulator
	 * 
	 * @see com.cloud.resource.Discoverer#find(long, java.lang.Long,
	 *      java.lang.Long, java.net.URI, java.lang.String, java.lang.String)
	 */
	@Override
	public Map<? extends ServerResource, Map<String, String>> find(long dcId,
			Long podId, Long clusterId, URI uri, String username,
			String password) throws DiscoveryException {
		Map<AgentResourceBase, Map<String, String>> resources;

		try {
			if (uri.getScheme().equals("http")) {
				if (!uri.getAuthority().contains("sim")) {
					String msg = "uri is not of simulator type so we're not taking care of the discovery for this: "
							+ uri;
					if(s_logger.isDebugEnabled()) {
						s_logger.debug(msg);
					}
					return null;
				}
			} else {
				String msg = "uriString is not http so we're not taking care of the discovery for this: "
						+ uri;
				if(s_logger.isDebugEnabled()) {
					s_logger.debug(msg);
				}
				return null;
			}

			String cluster = null;
			if (clusterId == null) {
				String msg = "must specify cluster Id when adding host";
				if(s_logger.isDebugEnabled()) {
					s_logger.debug(msg);
				}
				throw new RuntimeException(msg);
			} else {
				ClusterVO clu = _clusterDao.findById(clusterId);
				if (clu == null
						|| (clu.getHypervisorType() != HypervisorType.Simulator)) {
					if (s_logger.isInfoEnabled())
						s_logger.info("invalid cluster id or cluster is not for Simulator hypervisors");
					return null;
				}					
				cluster = Long.toString(clusterId);
				if(clu.getGuid() == null) {
					clu.setGuid(UUID.randomUUID().toString());
				}
				_clusterDao.update(clusterId, clu);
			}

			String pod;
			if (podId == null) {
				String msg = "must specify pod Id when adding host";
				if(s_logger.isDebugEnabled()) {
					s_logger.debug(msg);
				}
				throw new RuntimeException(msg);
			} else {
				pod = Long.toString(podId);
			}

			Map<String, String> details = new HashMap<String, String>();
			Map<String, Object> params = new HashMap<String, Object>();
			details.put("username", username);
			params.put("username", username);
			details.put("password", password);
			params.put("password", password);
			params.put("zone", Long.toString(dcId));
			params.put("pod", pod);
			params.put("cluster", cluster);

			resources = createAgentResources(params);
			return resources;
		} catch (Exception ex) {
			s_logger.error("Exception when discovering simulator hosts: "
					+ ex.getMessage());
		}
		return null;
	}

	private Map<AgentResourceBase, Map<String, String>> createAgentResources(
			Map<String, Object> params) {
		try {
			s_logger.info("Creating Simulator Resources");
			_simMgr.start();
			return _simMgr.createServerResources(params);
		} catch (Exception ex) {
			s_logger.warn("Caught exception at agent resource creation: "
					+ ex.getMessage(), ex);
		}
		return null;
	}

	@Override
	public void postDiscovery(List<HostVO> hosts, long msId) {

		for (HostVO h : hosts) {
			associateTemplatesToZone(h.getId(), h.getDataCenterId());
		}
	}    

    private void associateTemplatesToZone(long hostId, long dcId){
    	VMTemplateZoneVO tmpltZone;

    	List<VMTemplateVO> allTemplates = _vmTemplateDao.listAll();
    	for (VMTemplateVO vt: allTemplates){
    		if (vt.isCrossZones()) {
    			tmpltZone = _vmTemplateZoneDao.findByZoneTemplate(dcId, vt.getId());
    			if (tmpltZone == null) {
    				VMTemplateZoneVO vmTemplateZone = new VMTemplateZoneVO(dcId, vt.getId(), new Date());
    				_vmTemplateZoneDao.persist(vmTemplateZone);
    			}
    		}
    	}
    }
    
	@Override
	public HypervisorType getHypervisorType() {
		return HypervisorType.Simulator;
	}

	@Override
	public boolean matchHypervisor(String hypervisor) {
		return hypervisor.equalsIgnoreCase(HypervisorType.Simulator.toString());
	}
	
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        _agentMgr.registerForHostEvents(this, true, false, false);       
        return true;
    }

    @Override
    public boolean processAnswers(long agentId, long seq, Answer[] answers) {
        return false;
    }

    @Override
    public boolean processCommands(long agentId, long seq, Command[] commands) {
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(long agentId, AgentControlCommand cmd) {
        return null;
    }

    @Override
    public void processConnect(HostVO host, StartupCommand cmd, boolean forRebalance) throws ConnectionException {
    	if(forRebalance)
    		return;
        if ( Host.Type.SecondaryStorage == host.getType() ) {
            List<VMTemplateVO> tmplts = _vmTemplateDao.listAll();
            for( VMTemplateVO tmplt : tmplts ) {
                VMTemplateHostVO vmTemplateHost = _vmTemplateHostDao.findByHostTemplate(host.getId(), tmplt.getId());
                if (vmTemplateHost == null) {
                    vmTemplateHost = new VMTemplateHostVO(host.getId(), tmplt.getId(), new Date(), 100,
                            VMTemplateStorageResourceAssoc.Status.DOWNLOADED, null, null, null, null, tmplt.getUrl());
                    _vmTemplateHostDao.persist(vmTemplateHost);
                } else {
                    vmTemplateHost.setDownloadState(VMTemplateStorageResourceAssoc.Status.DOWNLOADED);
                    vmTemplateHost.setDownloadPercent(100);
                    _vmTemplateHostDao.update(vmTemplateHost.getId(), vmTemplateHost);
                }
            }
        }
    }

    @Override
    public boolean processDisconnect(long agentId, Status state) {
        return false;
    }

    @Override
    public boolean isRecurring() {
        return false;
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public boolean processTimeout(long agentId, long seq) {
        return false;
    }
    
}