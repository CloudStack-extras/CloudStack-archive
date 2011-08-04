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

package com.cloud.hypervisor.vmware;

import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.alert.AlertManager;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.exception.DiscoveredWithErrorException;
import com.cloud.exception.DiscoveryException;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.vmware.manager.VmwareManager;
import com.cloud.hypervisor.vmware.mo.ClusterMO;
import com.cloud.hypervisor.vmware.mo.HostMO;
import com.cloud.hypervisor.vmware.resource.VmwareContextFactory;
import com.cloud.hypervisor.vmware.resource.VmwareResource;
import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.cloud.resource.Discoverer;
import com.cloud.resource.DiscovererBase;
import com.cloud.resource.ServerResource;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.user.Account;
import com.cloud.utils.UriUtils;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.vmware.vim25.ClusterDasConfigInfo;
import com.vmware.vim25.ManagedObjectReference;

@Local(value=Discoverer.class)
public class VmwareServerDiscoverer extends DiscovererBase implements Discoverer {
	private static final Logger s_logger = Logger.getLogger(VmwareServerDiscoverer.class);
	
	@Inject ClusterDao _clusterDao;
	@Inject VmwareManager _vmwareMgr;
    @Inject AlertManager _alertMgr;
    @Inject VMTemplateDao _tmpltDao;
    @Inject ClusterDetailsDao _clusterDetailsDao;
    @Inject HostDao _hostDao;
    
    @Override
    public Map<? extends ServerResource, Map<String, String>> find(long dcId, Long podId, Long clusterId, URI url, 
    	String username, String password) throws DiscoveryException {
    	
    	if(s_logger.isInfoEnabled())
    		s_logger.info("Discover host. dc: " + dcId + ", pod: " + podId + ", cluster: " + clusterId + ", uri host: " + url.getHost());
    	
    	if(podId == null) {
        	if(s_logger.isInfoEnabled())
        		s_logger.info("No pod is assigned, assuming that it is not for vmware and skip it to next discoverer"); 
    		return null;
    	}
    	
        ClusterVO cluster = _clusterDao.findById(clusterId);
        if(cluster == null || cluster.getHypervisorType() != HypervisorType.VMware) {
        	if(s_logger.isInfoEnabled())
        		s_logger.info("invalid cluster id or cluster is not for VMware hypervisors"); 
    		return null;
        }
        
        List<HostVO> hosts = _hostDao.listByCluster(clusterId);
        if(hosts.size() >= _vmwareMgr.getMaxHostsPerCluster()) {
        	String msg = "VMware cluster " + cluster.getName() + " is too big to add new host now. (current configured cluster size: " + _vmwareMgr.getMaxHostsPerCluster() + ")";
        	s_logger.error(msg);
        	throw new DiscoveredWithErrorException(msg);
        }
    	
		VmwareContext context = null;
		try {
			context = VmwareContextFactory.create(url.getHost(), username, password);
			List<ManagedObjectReference> morHosts = _vmwareMgr.addHostToPodCluster(context, dcId, podId, clusterId,
				URLDecoder.decode(url.getPath()));
			if(morHosts == null) {
				s_logger.error("Unable to find host or cluster based on url: " + URLDecoder.decode(url.getPath()));
				return null;
			}
			
			ManagedObjectReference morCluster = null;
			Map<String, String> clusterDetails = _clusterDetailsDao.findDetails(clusterId);
			if(clusterDetails.get("url") != null) {
				URI uriFromCluster = new URI(UriUtils.encodeURIComponent(clusterDetails.get("url")));
				morCluster = context.getHostMorByPath(URLDecoder.decode(uriFromCluster.getPath()));
				
				if(morCluster == null || !morCluster.getType().equalsIgnoreCase("ClusterComputeResource")) {
					s_logger.warn("Cluster url does not point to a valid vSphere cluster, url: " + clusterDetails.get("url"));
					return null;
				} else {
					ClusterMO clusterMo = new ClusterMO(context, morCluster);
					ClusterDasConfigInfo dasConfig = clusterMo.getDasConfig();
					if(dasConfig != null && dasConfig.getEnabled() != null && dasConfig.getEnabled().booleanValue()) {
						clusterDetails.put("NativeHA", "true");
						_clusterDetailsDao.persist(clusterId, clusterDetails);
					}
				}
			}
			
			if(!validateDiscoveredHosts(context, morCluster, morHosts)) {
				if(morCluster == null)
					s_logger.warn("The discovered host is not standalone host, can not be added to a standalone cluster");
				else
					s_logger.warn("The discovered host does not belong to the cluster");
				return null;
			}
			
            Map<VmwareResource, Map<String, String>> resources = new HashMap<VmwareResource, Map<String, String>>();
			for(ManagedObjectReference morHost : morHosts) {
	            Map<String, String> details = new HashMap<String, String>();
	            Map<String, Object> params = new HashMap<String, Object>();
	            
	            HostMO hostMo = new HostMO(context, morHost);
	            details.put("url", hostMo.getHostName());
	            details.put("username", username);
	            details.put("password", password);
	            String guid = morHost.getType() + ":" + morHost.get_value() + "@"+ url.getHost();
	            details.put("guid", guid);
	            
	            params.put("url", hostMo.getHostName());
	            params.put("username", username);
	            params.put("password", password);
	            params.put("zone", Long.toString(dcId));
	            params.put("pod", Long.toString(podId));
	            params.put("cluster", Long.toString(clusterId));
	            params.put("guid", guid);
	            
	            VmwareResource resource = new VmwareResource(); 
	            try {
	                resource.configure("VMware", params);
	            } catch (ConfigurationException e) {
	                _alertMgr.sendAlert(AlertManager.ALERT_TYPE_HOST, dcId, podId, "Unable to add " + url.getHost(), "Error is " + e.getMessage());
	                s_logger.warn("Unable to instantiate " + url.getHost(), e);
	            }
	            resource.start();
	            
	            resources.put(resource, details);
			}
            
            // place a place holder guid derived from cluster ID
            cluster.setGuid(UUID.nameUUIDFromBytes(String.valueOf(clusterId).getBytes()).toString());
            _clusterDao.update(clusterId, cluster);
            
            return resources;
		} catch (DiscoveredWithErrorException e) {
			throw e;
		} catch (Exception e) {
			s_logger.warn("Unable to connect to Vmware vSphere server. service address: " + url.getHost());
			return null;
		} finally {
			if(context != null)
				context.close();
		}
    }
    
    private boolean validateDiscoveredHosts(VmwareContext context, ManagedObjectReference morCluster, List<ManagedObjectReference> morHosts) throws Exception {
    	if(morCluster == null) {
    		for(ManagedObjectReference morHost : morHosts) {
    			ManagedObjectReference morParent = (ManagedObjectReference)context.getServiceUtil().getDynamicProperty(morHost, "parent");
    			if(morParent.getType().equalsIgnoreCase("ClusterComputeResource"))
    				return false;
    		}
    	} else {
    		for(ManagedObjectReference morHost : morHosts) {
    			ManagedObjectReference morParent = (ManagedObjectReference)context.getServiceUtil().getDynamicProperty(morHost, "parent");
    			if(!morParent.getType().equalsIgnoreCase("ClusterComputeResource"))
    				return false;
    			
    			if(!morParent.get_value().equals(morCluster.get_value()))
    				return false;
    		}
    	}
    	
    	return true;
    }
    
    @Override
    public void postDiscovery(List<HostVO> hosts, long msId) {
        // do nothing
    }
    
    @Override
	public boolean matchHypervisor(String hypervisor) {
    	if(hypervisor == null)
    		return true;
    	
    	return Hypervisor.HypervisorType.VMware.toString().equalsIgnoreCase(hypervisor);
    }
    
    @Override
	public Hypervisor.HypervisorType getHypervisorType() {
    	return Hypervisor.HypervisorType.VMware;
    }
    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        if(s_logger.isInfoEnabled())
        	s_logger.info("Configure VmwareServerDiscoverer, discover name: " + name);
        
        super.configure(name, params);
        
        ComponentLocator locator = ComponentLocator.getCurrentLocator();
        ConfigurationDao configDao = locator.getDao(ConfigurationDao.class);
        if (configDao == null) {
            throw new ConfigurationException("Unable to get the configuration dao.");
        }
        
        createVmwareToolsIso();

		if(s_logger.isInfoEnabled())
			s_logger.info("VmwareServerDiscoverer has been successfully configured");
        return true;
    }
    
    private void createVmwareToolsIso() {
        String isoName = "vmware-tools.iso";
        VMTemplateVO tmplt = _tmpltDao.findByTemplateName(isoName);
        Long id;
        if (tmplt == null) {
            id = _tmpltDao.getNextInSequence(Long.class, "id");
            VMTemplateVO template = new VMTemplateVO(id, isoName, isoName, ImageFormat.ISO, true, true,
                    TemplateType.PERHOST, null, null, true, 64,
                    Account.ACCOUNT_ID_SYSTEM, null, "VMware Tools Installer ISO", false, 1, false, HypervisorType.VMware);
            _tmpltDao.persist(template);
        } else {
            id = tmplt.getId();
            tmplt.setTemplateType(TemplateType.PERHOST);
            tmplt.setUrl(null);
            _tmpltDao.update(id, tmplt);
        }
    }
}

