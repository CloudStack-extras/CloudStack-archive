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

package com.cloud.hypervisor.vmware.mo;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.cloud.utils.Pair;
import com.vmware.apputils.vim25.ServiceUtil;
import com.vmware.vim25.ArrayOfHostIpRouteEntry;
import com.vmware.vim25.ClusterComputeResourceSummary;
import com.vmware.vim25.ClusterConfigInfoEx;
import com.vmware.vim25.ClusterDasConfigInfo;
import com.vmware.vim25.ClusterHostRecommendation;
import com.vmware.vim25.ComputeResourceSummary;
import com.vmware.vim25.CustomFieldStringValue;
import com.vmware.vim25.DatastoreInfo;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.HostHardwareSummary;
import com.vmware.vim25.HostIpRouteEntry;
import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostSystemConnectionState;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NasDatastoreInfo;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.OptionValue;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VirtualMachineConfigSpec;

//
// TODO : ClusterMO was designed to be able to work as a special host before, therefore it implements VmwareHypervisorHost
// interface. This has changed as ClusterMO no longer works as a special host anymore. Need to refactor accordingly
//
public class ClusterMO extends BaseMO implements VmwareHypervisorHost {
    private static final Logger s_logger = Logger.getLogger(ClusterMO.class);
	
	public ClusterMO(VmwareContext context, ManagedObjectReference morCluster) {
		super(context, morCluster);
	}
	
	public ClusterMO(VmwareContext context, String morType, String morValue) {
		super(context, morType, morValue);
	}
	
	@Override
	public String getHyperHostName() throws Exception {
		return getName();
	}

	@Override
	public ClusterDasConfigInfo getDasConfig() throws Exception {
		// Note getDynamicProperty() with "configurationEx.dasConfig" does not work here because of that dasConfig is a property in subclass
		ClusterConfigInfoEx configInfo = (ClusterConfigInfoEx)_context.getServiceUtil().getDynamicProperty(_mor, "configurationEx");
		return configInfo.getDasConfig();
	}
	
	@Override
	public ManagedObjectReference getHyperHostDatacenter() throws Exception {
		Pair<DatacenterMO, String> dcPair = DatacenterMO.getOwnerDatacenter(getContext(), getMor());
		assert(dcPair != null);
		return dcPair.first().getMor();
	}
	
	@Override
	public ManagedObjectReference getHyperHostOwnerResourcePool() throws Exception {
		ServiceUtil serviceUtil = _context.getServiceUtil();
		return (ManagedObjectReference)serviceUtil.getDynamicProperty(getMor(), "resourcePool"); 
	}
	
	@Override
	public ManagedObjectReference getHyperHostCluster() throws Exception {
		return _mor;
	}
	
	@Override
	public VirtualMachineMO findVmOnHyperHost(String name) throws Exception {
		ObjectContent[] ocs = getVmPropertiesOnHyperHost(new String[] { "name" });
		return HypervisorHostHelper.findVmFromObjectContent(_context, ocs, name);
	}
	
	@Override
	public VirtualMachineMO findVmOnPeerHyperHost(String name) throws Exception {
		ObjectContent[] ocs = getVmPropertiesOnHyperHost(new String[] { "name" });
		if(ocs != null && ocs.length > 0) {
			for(ObjectContent oc : ocs) {
				DynamicProperty[] props = oc.getPropSet();
				if(props != null) {
					for(DynamicProperty prop : props) {
						if(prop.getVal().toString().equals(name))
							return new VirtualMachineMO(_context, oc.getObj());
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public ObjectContent[] getVmPropertiesOnHyperHost(String[] propertyPaths) throws Exception {
		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("VirtualMachine");
		pSpec.setPathSet(propertyPaths);

	    TraversalSpec host2VmFolderTraversal = new TraversalSpec();
	    host2VmFolderTraversal.setType("HostSystem");
	    host2VmFolderTraversal.setPath("vm");
	    host2VmFolderTraversal.setName("host2VmFolderTraversal");
		
	    TraversalSpec cluster2HostFolderTraversal = new TraversalSpec();
	    cluster2HostFolderTraversal.setType("ClusterComputeResource");
	    cluster2HostFolderTraversal.setPath("host");
	    cluster2HostFolderTraversal.setName("cluster2HostFolderTraversal");
	    cluster2HostFolderTraversal.setSelectSet(new SelectionSpec[] { host2VmFolderTraversal });
	    
	    ObjectSpec oSpec = new ObjectSpec();
	    oSpec.setObj(getMor());
	    oSpec.setSkip(Boolean.TRUE);
	    oSpec.setSelectSet(new SelectionSpec[] { cluster2HostFolderTraversal });
	    
	    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
	    pfSpec.setPropSet(new PropertySpec[] { pSpec });
	    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
	    
	    return _context.getService().retrieveProperties(
	    	_context.getServiceContent().getPropertyCollector(), 
	    	new PropertyFilterSpec[] { pfSpec }); 
	}

	@Override
	public ObjectContent[] getDatastorePropertiesOnHyperHost(String[] propertyPaths) throws Exception {
		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("Datastore");
		pSpec.setPathSet(propertyPaths);
		
	    TraversalSpec cluster2DatastoreTraversal = new TraversalSpec();
	    cluster2DatastoreTraversal.setType("ClusterComputeResource");
	    cluster2DatastoreTraversal.setPath("datastore");
	    cluster2DatastoreTraversal.setName("cluster2DatastoreTraversal");

	    ObjectSpec oSpec = new ObjectSpec();
	    oSpec.setObj(_mor);
	    oSpec.setSkip(Boolean.TRUE);
	    oSpec.setSelectSet(new SelectionSpec[] { cluster2DatastoreTraversal });

	    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
	    pfSpec.setPropSet(new PropertySpec[] { pSpec });
	    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
	    
	    return _context.getService().retrieveProperties(
	    	_context.getServiceContent().getPropertyCollector(), 
	    	new PropertyFilterSpec[] { pfSpec }); 
	}
	
	private ObjectContent[] getHostPropertiesOnCluster(String[] propertyPaths) throws Exception {
		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("HostSystem");
		pSpec.setPathSet(propertyPaths);
		
	    TraversalSpec cluster2HostTraversal = new TraversalSpec();
	    cluster2HostTraversal.setType("ClusterComputeResource");
	    cluster2HostTraversal.setPath("host");
	    cluster2HostTraversal.setName("cluster2HostTraversal");

	    ObjectSpec oSpec = new ObjectSpec();
	    oSpec.setObj(_mor);
	    oSpec.setSkip(Boolean.TRUE);
	    oSpec.setSelectSet(new SelectionSpec[] { cluster2HostTraversal });

	    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
	    pfSpec.setPropSet(new PropertySpec[] { pSpec });
	    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
	    
	    return _context.getService().retrieveProperties(
	    	_context.getServiceContent().getPropertyCollector(), 
	    	new PropertyFilterSpec[] { pfSpec }); 
	}

	@Override
	public boolean createVm(VirtualMachineConfigSpec vmSpec) throws Exception {
		assert(vmSpec != null);
		DatacenterMO dcMo = new DatacenterMO(_context, getHyperHostDatacenter());
        ManagedObjectReference morPool = getHyperHostOwnerResourcePool();
		
	    ManagedObjectReference morTask = _context.getService().createVM_Task(
	    	dcMo.getVmFolder(), vmSpec, morPool, null);
		String result = _context.getServiceUtil().waitForTask(morTask);
		
		if(result.equals("sucess")) {
			_context.waitForTaskProgressDone(morTask);
			return true;
		} else {
        	s_logger.error("VMware createVM_Task failed due to " + TaskMO.getTaskFailureInfo(_context, morTask));
		}
		
		return false;
	}
	
	@Override
	public void importVmFromOVF(String ovfFilePath, String vmName, DatastoreMO dsMo, String diskOption) throws Exception {
		ManagedObjectReference morRp = getHyperHostOwnerResourcePool();
		assert(morRp != null);
		
		HypervisorHostHelper.importVmFromOVF(this, ovfFilePath, vmName, dsMo, diskOption, morRp, null);
	}

	@Override
	public boolean createBlankVm(String vmName, int cpuCount, int cpuSpeedMHz, int cpuReservedMHz, boolean limitCpuUse, int memoryMB, 
		String guestOsIdentifier, ManagedObjectReference morDs, boolean snapshotDirToParent) throws Exception {
		
		return HypervisorHostHelper.createBlankVm(this, vmName, cpuCount, cpuSpeedMHz, cpuReservedMHz, limitCpuUse,
			memoryMB, guestOsIdentifier, morDs, snapshotDirToParent);
	}

	@Override
	public ManagedObjectReference mountDatastore(boolean vmfsDatastore, String poolHostAddress, 
		int poolHostPort, String poolPath, String poolUuid) throws Exception {
		
		ManagedObjectReference morDs = null;
		ManagedObjectReference morDsFirst = null;
		ManagedObjectReference[] hosts = (ManagedObjectReference[])_context.getServiceUtil().getDynamicProperty(_mor, "host");
		if(hosts != null && hosts.length > 0) {
			for(ManagedObjectReference morHost : hosts) {
				HostMO hostMo = new HostMO(_context, morHost);
				morDs = hostMo.mountDatastore(vmfsDatastore, poolHostAddress, poolHostPort, poolPath, poolUuid);
				if(morDsFirst == null)
					morDsFirst = morDs;
				
				// assume datastore is in scope of datacenter
				assert(morDsFirst.get_value().equals(morDs.get_value()));
			}
		}
		
		if(morDs == null) {
			String msg = "Failed to mount datastore in all hosts within the cluster";
			s_logger.error(msg);
			throw new Exception(msg);
		}
		
		return morDs;
	}
	
	@Override
	public void unmountDatastore(String poolUuid) throws Exception {
		ManagedObjectReference[] hosts = (ManagedObjectReference[])_context.getServiceUtil().getDynamicProperty(_mor, "host");
		if(hosts != null && hosts.length > 0) {
			for(ManagedObjectReference morHost : hosts) {
				HostMO hostMo = new HostMO(_context, morHost);
				hostMo.unmountDatastore(poolUuid);
			}
		}
	}
	
	@Override
	public ManagedObjectReference findDatastore(String poolUuid) throws Exception {
		CustomFieldsManagerMO cfmMo = new CustomFieldsManagerMO(_context, 
				_context.getServiceContent().getCustomFieldsManager());
		int key = cfmMo.getCustomFieldKey("Datastore", CustomFieldConstants.CLOUD_UUID);
		assert(key != 0);

		ObjectContent[] ocs = getDatastorePropertiesOnHyperHost(new String[] {"name", String.format("value[%d]", key)});
		if(ocs != null) {
			for(ObjectContent oc : ocs) {
				if(oc.getPropSet(0).getVal().equals(poolUuid))
					return oc.getObj();
				
				if(oc.getPropSet().length > 1) {
					DynamicProperty prop = oc.getPropSet(1);
					if(prop != null && prop.getVal() != null) {
						if(prop.getVal() instanceof CustomFieldStringValue) {
							String val = ((CustomFieldStringValue)prop.getVal()).getValue();
							if(val.equalsIgnoreCase(poolUuid))
								return oc.getObj();
						}
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public ManagedObjectReference findDatastoreByExportPath(String exportPath) throws Exception {
		ObjectContent[] ocs = getDatastorePropertiesOnHyperHost(new String[] {"info"});
		if(ocs != null && ocs.length > 0) {
			for(ObjectContent oc : ocs) {
				DatastoreInfo dsInfo = (DatastoreInfo)oc.getPropSet(0).getVal();
				if(dsInfo != null && dsInfo instanceof NasDatastoreInfo) {
					NasDatastoreInfo info = (NasDatastoreInfo)dsInfo;
					if(info != null) {
						String vmwareUrl = info.getUrl();
						if(vmwareUrl.charAt(vmwareUrl.length() - 1) == '/')
							vmwareUrl = vmwareUrl.substring(0, vmwareUrl.length() - 1);
						
						URI uri = new URI(vmwareUrl);
						if(uri.getPath().equals("/" + exportPath))
							return oc.getObj();
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	public ManagedObjectReference findMigrationTarget(VirtualMachineMO vmMo) throws Exception {
		ClusterHostRecommendation[] candidates = recommendHostsForVm(vmMo);
		if(candidates != null && candidates.length > 0)
			return candidates[0].getHost();
		
		return null;
	}
	
	@Override
	public boolean isHyperHostConnected() throws Exception {
		ObjectContent[] ocs = getHostPropertiesOnCluster(new String[] {"runtime"});
		if(ocs != null && ocs.length > 0) {
			for(ObjectContent oc : ocs) {
				HostRuntimeInfo runtimeInfo = (HostRuntimeInfo)oc.getPropSet(0).getVal();
				// as long as we have one host connected, we assume the cluster is up 
				if(runtimeInfo.getConnectionState() == HostSystemConnectionState.connected)
					return true;
			}
		}
		return false;
	}
	
	@Override
	public String getHyperHostDefaultGateway() throws Exception {
		ObjectContent[] ocs = getHostPropertiesOnCluster(new String[] {"config.network.routeTableInfo.ipRoute"});
		if(ocs != null && ocs.length > 0) {
			for(ObjectContent oc : ocs) {
				ArrayOfHostIpRouteEntry entries = (ArrayOfHostIpRouteEntry)oc.getPropSet(0).getVal();
				if(entries != null) {
					for(HostIpRouteEntry entry : entries.getHostIpRouteEntry()) {
						if(entry.getNetwork().equalsIgnoreCase("0.0.0.0"))
							return entry.getGateway();
					}
				}
			}
		}
		
		throw new Exception("Could not find host default gateway, host is not properly configured?");
	}
	
	@Override
	public VmwareHypervisorHostResourceSummary getHyperHostResourceSummary() throws Exception {
		VmwareHypervisorHostResourceSummary summary = new VmwareHypervisorHostResourceSummary();
	
		ComputeResourceSummary vmwareSummary = (ComputeResourceSummary)_context.getServiceUtil().getDynamicProperty(
			_mor, "summary");
		
		// TODO, need to use traversal to optimize retrieve of 
		int cpuNumInCpuThreads = 1;
		ManagedObjectReference[] hosts = (ManagedObjectReference[])_context.getServiceUtil().getDynamicProperty(_mor, "host");
		if(hosts != null && hosts.length > 0) {
			for(ManagedObjectReference morHost : hosts) {
				HostMO hostMo = new HostMO(_context, morHost);
				HostHardwareSummary hardwareSummary = hostMo.getHostHardwareSummary();
				
				if(hardwareSummary.getNumCpuCores()*hardwareSummary.getNumCpuThreads() > cpuNumInCpuThreads)
					cpuNumInCpuThreads = hardwareSummary.getNumCpuCores()*hardwareSummary.getNumCpuThreads();
			}
		}
		summary.setCpuCount(cpuNumInCpuThreads);
		summary.setCpuSpeed(vmwareSummary.getTotalCpu());
		summary.setMemoryBytes(vmwareSummary.getTotalMemory());
		return summary;
	}
	
	@Override
	public VmwareHypervisorHostNetworkSummary getHyperHostNetworkSummary(String esxServiceConsolePort) throws Exception {
		ManagedObjectReference[] hosts = (ManagedObjectReference[])_context.getServiceUtil().getDynamicProperty(_mor, "host");
		if(hosts != null && hosts.length > 0) {
			return new HostMO(_context, hosts[0]).getHyperHostNetworkSummary(esxServiceConsolePort);
		}
		return null;
	}
	
	@Override
	public ComputeResourceSummary getHyperHostHardwareSummary() throws Exception {
		ClusterComputeResourceSummary hardwareSummary = (ClusterComputeResourceSummary)
			_context.getServiceUtil().getDynamicProperty(_mor, "summary");

		return hardwareSummary;
	}

	public ClusterHostRecommendation[] recommendHostsForVm(VirtualMachineMO vmMo) throws Exception {
		return _context.getService().recommendHostsForVm(_mor, vmMo.getMor(), 
			getHyperHostOwnerResourcePool());
	}
	
	public List<Pair<ManagedObjectReference, String>> getClusterHosts() throws Exception {
		List<Pair<ManagedObjectReference, String>> hosts = new ArrayList<Pair<ManagedObjectReference, String>>();
		
		ObjectContent[] ocs = getHostPropertiesOnCluster(new String[] {"name"});
		if(ocs != null) {
			for(ObjectContent oc : ocs) {
				ManagedObjectReference morHost = oc.getObj();
				String name = (String)oc.getPropSet(0).getVal();
				
				hosts.add(new Pair<ManagedObjectReference, String>(morHost, name));
			}
		}
		return hosts;
	}
	
	public HashMap<String, Integer> getVmVncPortsOnCluster() throws Exception {
    	ObjectContent[] ocs = getVmPropertiesOnHyperHost(
        		new String[] { "name", "config.extraConfig[\"RemoteDisplay.vnc.port\"]" }
        	);
		
        HashMap<String, Integer> portInfo = new HashMap<String, Integer>();
    	if(ocs != null && ocs.length > 0) {
    		for(ObjectContent oc : ocs) {
		        DynamicProperty[] objProps = oc.getPropSet();
		        if(objProps != null) {
		        	String name = null;
		        	String value = null;
		        	for(DynamicProperty objProp : objProps) {
		        		if(objProp.getName().equals("name")) {
		        			name = (String)objProp.getVal();
		        		} else {
		        			OptionValue optValue = (OptionValue)objProp.getVal();
		        			value = (String)optValue.getValue();
		        		} 
		        	}
		        	
		        	if(name != null && value != null) {
		        		portInfo.put(name, Integer.parseInt(value));
		        	}
		        }
    		}
    	}
    	
    	return portInfo;
	}
}

