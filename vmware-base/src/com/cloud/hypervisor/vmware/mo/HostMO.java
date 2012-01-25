/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.hypervisor.vmware.mo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.cloud.hypervisor.vmware.util.VmwareHelper;
import com.cloud.utils.Pair;
import com.google.gson.Gson;
import com.vmware.apputils.vim25.ServiceUtil;
import com.vmware.vim25.AboutInfo;
import com.vmware.vim25.ClusterDasConfigInfo;
import com.vmware.vim25.ComputeResourceSummary;
import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.HostConfigManager;
import com.vmware.vim25.HostConnectInfo;
import com.vmware.vim25.HostHardwareSummary;
import com.vmware.vim25.HostHyperThreadScheduleInfo;
import com.vmware.vim25.HostIpRouteEntry;
import com.vmware.vim25.HostListSummaryQuickStats;
import com.vmware.vim25.HostNetworkInfo;
import com.vmware.vim25.HostNetworkPolicy;
import com.vmware.vim25.HostNetworkTrafficShapingPolicy;
import com.vmware.vim25.HostPortGroup;
import com.vmware.vim25.HostPortGroupSpec;
import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostSystemConnectionState;
import com.vmware.vim25.HostVirtualNic;
import com.vmware.vim25.HostVirtualSwitch;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.OptionValue;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualNicManagerNetConfig;

public class HostMO extends BaseMO implements VmwareHypervisorHost {
    private static final Logger s_logger = Logger.getLogger(HostMO.class);
    Map<String, VirtualMachineMO> _vmCache = new HashMap<String, VirtualMachineMO>();
	
	public HostMO (VmwareContext context, ManagedObjectReference morHost) {
		super(context, morHost);
	}
	
	public HostMO (VmwareContext context, String morType, String morValue) {
		super(context, morType, morValue);
	}
	
	public HostHardwareSummary getHostHardwareSummary() throws Exception {
		HostConnectInfo hostInfo = _context.getService().queryHostConnectionInfo(_mor);
		HostHardwareSummary hardwareSummary = hostInfo.getHost().getHardware();
		return hardwareSummary;
	}
	
	public HostConfigManager getHostConfigManager() throws Exception {
		return (HostConfigManager)_context.getServiceUtil().getDynamicProperty(_mor, "configManager");
	}
	
	public VirtualNicManagerNetConfig[] getHostVirtualNicManagerNetConfig() throws Exception {
		VirtualNicManagerNetConfig[] netConfigs = (VirtualNicManagerNetConfig[])_context.getServiceUtil().getDynamicProperty(_mor, 
			"config.virtualNicManagerInfo.netConfig");
		return netConfigs; 
	}
	
	public HostIpRouteEntry[] getHostIpRouteEntries() throws Exception {
		HostIpRouteEntry[] entries = (HostIpRouteEntry[])_context.getServiceUtil().getDynamicProperty(_mor, 
			"config.network.routeTableInfo.ipRoute");
		return entries; 
	}
	
	public HostListSummaryQuickStats getHostQuickStats() throws Exception {
		return (HostListSummaryQuickStats)_context.getServiceUtil().getDynamicProperty(_mor, "summary.quickStats");
	}
	
	public HostHyperThreadScheduleInfo getHostHyperThreadInfo() throws Exception {
		return (HostHyperThreadScheduleInfo)_context.getServiceUtil().getDynamicProperty(_mor, "config.hyperThread");
	}
	
	public HostNetworkInfo getHostNetworkInfo() throws Exception {
		return (HostNetworkInfo)_context.getServiceUtil().getDynamicProperty(_mor, "config.network");
	}
	
	public HostPortGroupSpec getHostPortGroupSpec(String portGroupName) throws Exception {
		
		HostNetworkInfo hostNetInfo = getHostNetworkInfo();
		
		HostPortGroup[] portGroups = hostNetInfo.getPortgroup();
		if(portGroups != null) {
			for(HostPortGroup portGroup : portGroups) {
				HostPortGroupSpec spec = portGroup.getSpec();
				if(spec.getName().equals(portGroupName))
					return spec;
			}
		}
		
		return null;
	}
	
	@Override
	public String getHyperHostName() throws Exception {
		return getName();
	}
	
	@Override
	public ClusterDasConfigInfo getDasConfig() throws Exception {
		ManagedObjectReference morParent = getParentMor();
		if(morParent.getType().equals("ClusterComputeResource")) {
			ClusterMO clusterMo = new ClusterMO(_context, morParent);
			return clusterMo.getDasConfig();
		}
		
		return null;
	}
	
	@Override
	public String getHyperHostDefaultGateway() throws Exception {
		HostIpRouteEntry[] entries = getHostIpRouteEntries();
		for(HostIpRouteEntry entry : entries) {
			if(entry.getNetwork().equalsIgnoreCase("0.0.0.0"))
				return entry.getGateway();
		}
		
		throw new Exception("Could not find host default gateway, host is not properly configured?");
	}
	
	public HostDatastoreSystemMO getHostDatastoreSystemMO() throws Exception {
		return new HostDatastoreSystemMO(_context,
			(ManagedObjectReference)_context.getServiceUtil().getDynamicProperty(
				_mor, "configManager.datastoreSystem")
		);
	}
	
	public HostDatastoreBrowserMO getHostDatastoreBrowserMO() throws Exception {
		return new HostDatastoreBrowserMO(_context,
				(ManagedObjectReference)_context.getServiceUtil().getDynamicProperty(
					_mor, "datastoreBrowser")
			);
	}

	private DatastoreMO getHostDatastoreMO(String datastoreName) throws Exception {
		ObjectContent[] ocs = getDatastorePropertiesOnHyperHost(new String[] { "name"} );
		if(ocs != null && ocs.length > 0) {
    		for(ObjectContent oc : ocs) {
		        DynamicProperty[] objProps = oc.getPropSet();
		        if(objProps != null) {
		        	for(DynamicProperty objProp : objProps) {
		        		if(objProp.getVal().toString().equals(datastoreName))
		        			return new DatastoreMO(_context, oc.getObj());
		        	}
		        }
    		}
		}
		return null;
	}
	
	public HostNetworkSystemMO getHostNetworkSystemMO() throws Exception {
		HostConfigManager configMgr = getHostConfigManager();
		return new HostNetworkSystemMO(_context, configMgr.getNetworkSystem());
	}
	
	public HostFirewallSystemMO getHostFirewallSystemMO() throws Exception {
		HostConfigManager configMgr = getHostConfigManager();
		ManagedObjectReference morFirewall = configMgr.getFirewallSystem();
		
		// only ESX hosts have firewall manager
		if(morFirewall != null)
			return new HostFirewallSystemMO(_context, morFirewall);
		return null;
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
		ManagedObjectReference morComputerResource = (ManagedObjectReference)serviceUtil.getDynamicProperty(_mor, "parent");
		return (ManagedObjectReference)serviceUtil.getDynamicProperty(morComputerResource, "resourcePool"); 
	}

	@Override
	public ManagedObjectReference getHyperHostCluster() throws Exception {
		ServiceUtil serviceUtil = _context.getServiceUtil();
		ManagedObjectReference morParent = (ManagedObjectReference)serviceUtil.getDynamicProperty(_mor, "parent");
		
		if(morParent.getType().equalsIgnoreCase("ClusterComputeResource")) {
			return morParent;
		}
		
		assert(false);
		throw new Exception("Standalone host is not supported");
	}
	
	public ManagedObjectReference[] getHostLocalDatastore() throws Exception {
		ServiceUtil serviceUtil = _context.getServiceUtil();
		ManagedObjectReference[] datastores = (ManagedObjectReference[])serviceUtil.getDynamicProperty(
			_mor, "datastore");
		List<ManagedObjectReference> l = new ArrayList<ManagedObjectReference>();
		if(datastores != null) {
			for(ManagedObjectReference mor : datastores) {
				DatastoreSummary summary = (DatastoreSummary)serviceUtil.getDynamicProperty(mor, "summary");
				if(summary.getType().equalsIgnoreCase("VMFS") && !summary.getMultipleHostAccess())
					l.add(mor);
			}
		}
		return l.toArray(new ManagedObjectReference[1]);
	}
	
	public HostVirtualSwitch getHostVirtualSwitchByName(String name) throws Exception {
		HostVirtualSwitch[] switches = (HostVirtualSwitch[])_context.getServiceUtil().getDynamicProperty(
			_mor, "config.network.vswitch");
		
		if(switches != null) {
			for(HostVirtualSwitch vswitch : switches) {
				if(vswitch.getName().equals(name))
					return vswitch;
			}
		}
		return null;
	}
	
	public HostVirtualSwitch[] getHostVirtualSwitch() throws Exception {
		return (HostVirtualSwitch[])_context.getServiceUtil().getDynamicProperty(_mor, "config.network.vswitch");
	}
	
	public AboutInfo getHostAboutInfo() throws Exception {
		return (AboutInfo)_context.getServiceUtil().getDynamicProperty(_mor, "config.product");
	}
	
	public VmwareHostType getHostType() throws Exception {
		AboutInfo aboutInfo = getHostAboutInfo();
		if("VMware ESXi".equals(aboutInfo.getName()))
			return VmwareHostType.ESXi;
		else if("VMware ESX".equals(aboutInfo.getName()))
			return VmwareHostType.ESX;
		
		throw new Exception("Unrecognized VMware host type " + aboutInfo.getName());
	}
	
	// default virtual switch is which management network residents on
	public HostVirtualSwitch getHostDefaultVirtualSwitch() throws Exception {
		String managementPortGroup = getPortGroupNameByNicType(HostVirtualNicType.management);
		if(managementPortGroup != null)
			return getPortGroupVirtualSwitch(managementPortGroup);
		
		return null;
	}
	
	public HostVirtualSwitch getPortGroupVirtualSwitch(String portGroupName) throws Exception {
		String vSwitchName = getPortGroupVirtualSwitchName(portGroupName);
		if(vSwitchName != null)
			return getVirtualSwitchByName(vSwitchName);
		
		return null;
	}
	
	public HostVirtualSwitch getVirtualSwitchByName(String vSwitchName) throws Exception {
		
		HostVirtualSwitch[] vSwitchs = getHostVirtualSwitch();
		if(vSwitchs != null) {
			for(HostVirtualSwitch vSwitch: vSwitchs) {
				if(vSwitch.getName().equals(vSwitchName))
					return vSwitch;
			}
		}
		
		return null;
	}
	
	public String getPortGroupVirtualSwitchName(String portGroupName) throws Exception {
		HostNetworkInfo hostNetInfo = getHostNetworkInfo();
		HostPortGroup[] portGroups = hostNetInfo.getPortgroup();
		if(portGroups != null) {
			for(HostPortGroup portGroup : portGroups) {
				HostPortGroupSpec spec = portGroup.getSpec();
				if(spec.getName().equals(portGroupName))
					return spec.getVswitchName();
			}
		}
		
		return null;
	}
	
	public HostPortGroupSpec getPortGroupSpec(String portGroupName) throws Exception {
		HostNetworkInfo hostNetInfo = getHostNetworkInfo();
		HostPortGroup[] portGroups = hostNetInfo.getPortgroup();
		if(portGroups != null) {
			for(HostPortGroup portGroup : portGroups) {
				HostPortGroupSpec spec = portGroup.getSpec();
				if(spec.getName().equals(portGroupName))
					return spec;
			}
		}
		
		return null;
	}
	
	public String getPortGroupNameByNicType(HostVirtualNicType nicType) throws Exception {
		assert(nicType != null);
		
		VirtualNicManagerNetConfig[] netConfigs = (VirtualNicManagerNetConfig[])_context.getServiceUtil().getDynamicProperty(_mor, 
			"config.virtualNicManagerInfo.netConfig");
		
		if(netConfigs != null) {
			for(VirtualNicManagerNetConfig netConfig : netConfigs) {
				if(netConfig.getNicType().equals(nicType.toString())) {
					HostVirtualNic[] nics = netConfig.getCandidateVnic();
					if(nics != null) {
						for(HostVirtualNic nic : nics) {
							return nic.getPortgroup();
						}
					}
				}
			}
		}
		
		if(nicType == HostVirtualNicType.management) {
			// ESX management network is configured in service console
			HostNetworkInfo netInfo = getHostNetworkInfo();
			assert(netInfo != null);
			HostVirtualNic[] nics = netInfo.getConsoleVnic();
			if(nics != null) {
				for(HostVirtualNic nic : nics) {
					return nic.getPortgroup();
				}
			}
		}
		
		return null;
	}
	
	public boolean hasPortGroup(HostVirtualSwitch vSwitch, String portGroupName) throws Exception {
		ManagedObjectReference morNetwork = getNetworkMor(portGroupName);
		if(morNetwork != null)
			return true;
		return false;
	}
	
	public void createPortGroup(HostVirtualSwitch vSwitch, String portGroupName, Integer vlanId, HostNetworkTrafficShapingPolicy shapingPolicy) throws Exception {
		assert(portGroupName != null);
		HostNetworkSystemMO hostNetMo = getHostNetworkSystemMO();
		assert(hostNetMo != null);
		
		HostPortGroupSpec spec = new HostPortGroupSpec();
		
		spec.setName(portGroupName);
		if(vlanId != null)
			spec.setVlanId(vlanId.intValue());
		HostNetworkPolicy policy = new HostNetworkPolicy();
		policy.setShapingPolicy(shapingPolicy);
		spec.setPolicy(policy);
		spec.setVswitchName(vSwitch.getName());
		hostNetMo.addPortGroup(spec);
	}
	
	public void updatePortGroup(HostVirtualSwitch vSwitch, String portGroupName, Integer vlanId, HostNetworkTrafficShapingPolicy shapingPolicy) throws Exception {
		assert(portGroupName != null);
		HostNetworkSystemMO hostNetMo = getHostNetworkSystemMO();
		assert(hostNetMo != null);
		
		HostPortGroupSpec spec = new HostPortGroupSpec();
		
		spec.setName(portGroupName);
		if(vlanId != null)
			spec.setVlanId(vlanId.intValue());
		HostNetworkPolicy policy = new HostNetworkPolicy();
		policy.setShapingPolicy(shapingPolicy);
		spec.setPolicy(policy);
		spec.setVswitchName(vSwitch.getName());
		hostNetMo.updatePortGroup(portGroupName, spec);
	}
	
	public void deletePortGroup(String portGroupName) throws Exception {
		assert(portGroupName != null);
		HostNetworkSystemMO hostNetMo = getHostNetworkSystemMO();
		assert(hostNetMo != null);
		hostNetMo.removePortGroup(portGroupName); 
	}
	
	public ManagedObjectReference getNetworkMor(String portGroupName) throws Exception {
		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("Network");
		pSpec.setPathSet(new String[] {"summary.name"});
		
	    TraversalSpec host2NetworkTraversal = new TraversalSpec();
	    host2NetworkTraversal.setType("HostSystem");
	    host2NetworkTraversal.setPath("network");
	    host2NetworkTraversal.setName("host2NetworkTraversal");

	    ObjectSpec oSpec = new ObjectSpec();
	    oSpec.setObj(_mor);
	    oSpec.setSkip(Boolean.TRUE);
	    oSpec.setSelectSet(new SelectionSpec[] { host2NetworkTraversal });

	    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
	    pfSpec.setPropSet(new PropertySpec[] { pSpec });
	    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
	    
	    ObjectContent[] ocs = _context.getService().retrieveProperties(
	    	_context.getServiceContent().getPropertyCollector(), 
	    	new PropertyFilterSpec[] { pfSpec });
	    
	    if(ocs != null) {
	    	for(ObjectContent oc : ocs) {
	    		DynamicProperty[] props = oc.getPropSet();
	    		if(props != null) {
	    			for(DynamicProperty prop : props) {
	    				if(prop.getVal().equals(portGroupName))
	    					return oc.getObj();
	    			}
	    		}
	    	}
	    }
	    return null;
	}
	
	public ManagedObjectReference[] getVmMorsOnNetwork(String portGroupName) throws Exception {
		ManagedObjectReference morNetwork = getNetworkMor(portGroupName);
		if(morNetwork != null)
			return (ManagedObjectReference[])_context.getServiceUtil().getDynamicProperty(morNetwork, "vm");
		return null;
	}
	
	public String getHostName() throws Exception {
		return (String)_context.getServiceUtil().getDynamicProperty(_mor, "name");
	}

    @Override
    public synchronized VirtualMachineMO findVmOnHyperHost(String name) throws Exception {
    	if(s_logger.isDebugEnabled())
    		s_logger.debug("find VM " + name + " on host");
    	
        VirtualMachineMO vmMo = _vmCache.get(name);
        if(vmMo != null) {
        	if(s_logger.isDebugEnabled())
        		s_logger.debug("VM " + name + " found in host cache");
            return vmMo;
        }
        
        loadVmCache();
        return _vmCache.get(name);
    }
    
    private void loadVmCache() throws Exception {
    	if(s_logger.isDebugEnabled())
    		s_logger.debug("load VM cache on host");
    	
        _vmCache.clear();
        
        ObjectContent[] ocs = getVmPropertiesOnHyperHost(new String[] { "name" });
        if(ocs != null && ocs.length > 0) {
            for(ObjectContent oc : ocs) {
                String vmName = oc.getPropSet()[0].getVal().toString();
                
                if(s_logger.isDebugEnabled())
                	s_logger.debug("put " + vmName + " into host cache");
                
                _vmCache.put(vmName, new VirtualMachineMO(_context, oc.getObj()));
            }
        }
    }
	
	@Override
	public VirtualMachineMO findVmOnPeerHyperHost(String name) throws Exception {
		ManagedObjectReference morParent = getParentMor();
		
		if(morParent.getType().equals("ClusterComputeResource")) {
			ClusterMO clusterMo = new ClusterMO(_context, morParent);
			return clusterMo.findVmOnHyperHost(name);
		} else {
			// we don't support standalone host, all hosts have to be managed by 
			// a cluster within vCenter
			assert(false);
			return null;
		}
	}

	@Override
	public boolean createVm(VirtualMachineConfigSpec vmSpec) throws Exception {
		assert(vmSpec != null);
		DatacenterMO dcMo = new DatacenterMO(_context, getHyperHostDatacenter());
        ManagedObjectReference morPool = getHyperHostOwnerResourcePool();
		
	    ManagedObjectReference morTask = _context.getService().createVM_Task(
	    	dcMo.getVmFolder(), vmSpec, morPool, _mor);
		String result = _context.getServiceUtil().waitForTask(morTask);
		
		if(result.equals("sucess")) {
			_context.waitForTaskProgressDone(morTask);
			return true;
		} else {
        	s_logger.error("VMware createVM_Task failed due to " + TaskMO.getTaskFailureInfo(_context, morTask));
		}
		
		return false;
	}
	
	public HashMap<String, Integer> getVmVncPortsOnHost() throws Exception {
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
	
	public ObjectContent[] getVmPropertiesOnHyperHost(String[] propertyPaths) throws Exception {
		if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - retrieveProperties() for VM properties. target MOR: " + _mor.get_value() + ", properties: " + new Gson().toJson(propertyPaths));
		
		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("VirtualMachine");
		pSpec.setPathSet(propertyPaths);
		
	    TraversalSpec host2VmTraversal = new TraversalSpec();
	    host2VmTraversal.setType("HostSystem");
	    host2VmTraversal.setPath("vm");
	    host2VmTraversal.setName("host2VmTraversal");

	    ObjectSpec oSpec = new ObjectSpec();
	    oSpec.setObj(_mor);
	    oSpec.setSkip(Boolean.TRUE);
	    oSpec.setSelectSet(new SelectionSpec[] { host2VmTraversal });

	    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
	    pfSpec.setPropSet(new PropertySpec[] { pSpec });
	    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
	    
	    ObjectContent[] properties = _context.getService().retrieveProperties(
	    	_context.getServiceContent().getPropertyCollector(), 
	    	new PropertyFilterSpec[] { pfSpec });

		if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - retrieveProperties() done");
	    return properties;
	}

	@Override
	public ObjectContent[] getDatastorePropertiesOnHyperHost(String[] propertyPaths) throws Exception {
		if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - retrieveProperties() on Datastore properties. target MOR: " + _mor.get_value() + ", properties: " + new Gson().toJson(propertyPaths));

		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("Datastore");
		pSpec.setPathSet(propertyPaths);
		
	    TraversalSpec host2DatastoreTraversal = new TraversalSpec();
	    host2DatastoreTraversal.setType("HostSystem");
	    host2DatastoreTraversal.setPath("datastore");
	    host2DatastoreTraversal.setName("host2DatastoreTraversal");

	    ObjectSpec oSpec = new ObjectSpec();
	    oSpec.setObj(_mor);
	    oSpec.setSkip(Boolean.TRUE);
	    oSpec.setSelectSet(new SelectionSpec[] { host2DatastoreTraversal });

	    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
	    pfSpec.setPropSet(new PropertySpec[] { pSpec });
	    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
	    
	    ObjectContent[] properties = _context.getService().retrieveProperties(
	    	_context.getServiceContent().getPropertyCollector(), 
	    	new PropertyFilterSpec[] { pfSpec });
	    
	    if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - retrieveProperties() done");
	    return properties;
	}
	
	public List<Pair<ManagedObjectReference, String>> getDatastoreMountsOnHost() throws Exception {
		List<Pair<ManagedObjectReference, String>> mounts = new ArrayList<Pair<ManagedObjectReference, String>>();
		
		ObjectContent[] ocs = getDatastorePropertiesOnHyperHost(new String[] {
			String.format("host[\"%s\"].mountInfo.path", _mor.get_value()) });
		if(ocs != null) {
			for(ObjectContent oc : ocs) {
				Pair<ManagedObjectReference, String> mount = new Pair<ManagedObjectReference, String>(
					oc.getObj(), oc.getPropSet(0).getVal().toString());
				mounts.add(mount);
			}
		}
		return mounts;
	}
	
	public List<Pair<ManagedObjectReference, String>> getLocalDatastoreOnHost() throws Exception {
		List<Pair<ManagedObjectReference, String>> dsList = new ArrayList<Pair<ManagedObjectReference, String>>();
		
		ObjectContent[] ocs = getDatastorePropertiesOnHyperHost(new String[] { "name", "summary" });
		if(ocs != null) {
			for(ObjectContent oc : ocs) {
				DatastoreSummary dsSummary = (DatastoreSummary)VmwareHelper.getPropValue(oc, "summary");
				if(dsSummary.getMultipleHostAccess() == false && dsSummary.isAccessible() && dsSummary.getType().equalsIgnoreCase("vmfs")) {
					ManagedObjectReference morDs = oc.getObj();
					String name = (String)VmwareHelper.getPropValue(oc, "name");
					
					dsList.add(new Pair<ManagedObjectReference, String>(morDs, name));
				}
			}
		}
		return dsList;
	}
	
	public void importVmFromOVF(String ovfFilePath, String vmName, String datastoreName, String diskOption) throws Exception {
	    if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - importVmFromOVF(). target MOR: " + _mor.get_value() + ", ovfFilePath: " + ovfFilePath + ", vmName: " + vmName 
				+ ",datastoreName: " + datastoreName + ", diskOption: " + diskOption);
	
		DatastoreMO dsMo = getHostDatastoreMO(datastoreName);
		if(dsMo == null)
			throw new Exception("Invalid datastore name: " + datastoreName);
		
		importVmFromOVF(ovfFilePath, vmName, dsMo, diskOption);
		
	    if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - importVmFromOVF() done");
	}
	
	@Override
	public void importVmFromOVF(String ovfFilePath, String vmName, DatastoreMO dsMo, String diskOption) throws Exception {

		ManagedObjectReference morRp = getHyperHostOwnerResourcePool();
		assert(morRp != null);
		
		HypervisorHostHelper.importVmFromOVF(this, ovfFilePath, vmName, dsMo, diskOption, morRp, _mor);
	}
	
	@Override
	public boolean createBlankVm(String vmName, int cpuCount, int cpuSpeedMHz, int cpuReservedMHz, boolean limitCpuUse, int memoryMB, int memoryReserveMB,
		String guestOsIdentifier, ManagedObjectReference morDs, boolean snapshotDirToParent) throws Exception {

		if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - createBlankVm(). target MOR: " + _mor.get_value() + ", vmName: " + vmName + ", cpuCount: " + cpuCount
				+ ", cpuSpeedMhz: " + cpuSpeedMHz + ", cpuReservedMHz: " + cpuReservedMHz + ", limitCpu: " + limitCpuUse + ", memoryMB: " + memoryMB 
				+ ", guestOS: " + guestOsIdentifier + ", datastore: " + morDs.get_value() + ", snapshotDirToParent: " + snapshotDirToParent);
		
		boolean result = HypervisorHostHelper.createBlankVm(this, vmName, cpuCount, cpuSpeedMHz, cpuReservedMHz, limitCpuUse,
			memoryMB, memoryReserveMB, guestOsIdentifier, morDs, snapshotDirToParent);

		if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - createBlankVm() done");
		return result;
	}

	@Override
	public ManagedObjectReference mountDatastore(boolean vmfsDatastore, String poolHostAddress, 
		int poolHostPort, String poolPath, String poolUuid) throws Exception {
		
	    if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - mountDatastore(). target MOR: " + _mor.get_value() + ", vmfs: " + vmfsDatastore + ", poolHost: " + poolHostAddress
				+ ", poolHostPort: " + poolHostPort + ", poolPath: " + poolPath + ", poolUuid: " + poolUuid);
		
    	HostDatastoreSystemMO hostDatastoreSystemMo = getHostDatastoreSystemMO();
        ManagedObjectReference morDatastore = hostDatastoreSystemMo.findDatastore(poolUuid);
        if(morDatastore == null) {
        	if(!vmfsDatastore) {
	        	morDatastore = hostDatastoreSystemMo.createNfsDatastore(
	        		poolHostAddress,
	        		poolHostPort, 
	        		poolPath, 
	        		poolUuid);
	        	if(morDatastore == null) {
	        		String msg = "Unable to create NFS datastore. host: " + poolHostAddress + ", port: " 
	    			+ poolHostPort + ", path: " + poolPath + ", uuid: " + poolUuid;
		    		s_logger.error(msg);
		    		
					if(s_logger.isTraceEnabled())
						s_logger.trace("vCenter API trace - mountDatastore() done(failed)");
		    		throw new Exception(msg);
	        	}
        	} else {
        		morDatastore = _context.getDatastoreMorByPath(poolPath);
	        	if(morDatastore == null) {
	        		String msg = "Unable to create VMFS datastore. host: " + poolHostAddress + ", port: " 
	    			+ poolHostPort + ", path: " + poolPath + ", uuid: " + poolUuid;
		    		s_logger.error(msg);
		    		
					if(s_logger.isTraceEnabled())
						s_logger.trace("vCenter API trace - mountDatastore() done(failed)");
		    		throw new Exception(msg);
	        	}
	        	
        		DatastoreMO dsMo = new DatastoreMO(_context, morDatastore);
        		dsMo.setCustomFieldValue(CustomFieldConstants.CLOUD_UUID, poolUuid);
        	}
        }
        
		if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - mountDatastore() done(successfully)");
        
    	return morDatastore;
	}
	
	@Override
	public void unmountDatastore(String poolUuid) throws Exception {
		
	    if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - unmountDatastore(). target MOR: " + _mor.get_value() + ", poolUuid: " + poolUuid);
		
    	HostDatastoreSystemMO hostDatastoreSystemMo = getHostDatastoreSystemMO();
    	if(!hostDatastoreSystemMo.deleteDatastore(poolUuid)) {
    		String msg = "Unable to unmount datastore. uuid: " + poolUuid;
    		s_logger.error(msg);

    		if(s_logger.isTraceEnabled())
    			s_logger.trace("vCenter API trace - unmountDatastore() done(failed)");
    		throw new Exception(msg);
    	}
    	
		if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - unmountDatastore() done");
	}
	
	@Override
	public ManagedObjectReference findDatastore(String poolUuid) throws Exception {
    	HostDatastoreSystemMO hostDsMo = getHostDatastoreSystemMO();
		return hostDsMo.findDatastore(poolUuid);
	}
	
	@Override
	public ManagedObjectReference findDatastoreByExportPath(String exportPath) throws Exception {
		HostDatastoreSystemMO datastoreSystemMo = getHostDatastoreSystemMO();
		return datastoreSystemMo.findDatastoreByExportPath(exportPath);
	}
	
	@Override
	public ManagedObjectReference findMigrationTarget(VirtualMachineMO vmMo) throws Exception {
		return _mor;
	}
	
	@Override
	public VmwareHypervisorHostResourceSummary getHyperHostResourceSummary() throws Exception {
	    if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - getHyperHostResourceSummary(). target MOR: " + _mor.get_value());
		
		VmwareHypervisorHostResourceSummary summary = new VmwareHypervisorHostResourceSummary();
		
		HostConnectInfo hostInfo = _context.getService().queryHostConnectionInfo(_mor);
		HostHardwareSummary hardwareSummary = hostInfo.getHost().getHardware();
		
		// TODO: not sure how hyper-thread is counted in VMware resource pool
		summary.setCpuCount(hardwareSummary.getNumCpuCores()*hardwareSummary.getNumCpuPkgs());
		summary.setMemoryBytes(hardwareSummary.getMemorySize());
		summary.setCpuSpeed(hardwareSummary.getCpuMhz());

	    if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - getHyperHostResourceSummary() done");
		return summary;
	}
	
	@Override
	public VmwareHypervisorHostNetworkSummary getHyperHostNetworkSummary(String managementPortGroup) throws Exception {
	    if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - getHyperHostNetworkSummary(). target MOR: " + _mor.get_value() + ", mgmtPortgroup: " + managementPortGroup);
		
		VmwareHypervisorHostNetworkSummary summary = new VmwareHypervisorHostNetworkSummary();
		
		if(this.getHostType() == VmwareHostType.ESXi) {
			VirtualNicManagerNetConfig[] netConfigs = (VirtualNicManagerNetConfig[])_context.getServiceUtil().getDynamicProperty(_mor, 
				"config.virtualNicManagerInfo.netConfig");
			assert(netConfigs != null);
	
			for(int i = 0; i < netConfigs.length; i++) {
				if(netConfigs[i].getNicType().equals("management")) {
					for(HostVirtualNic nic : netConfigs[i].getCandidateVnic()) {
						if(nic.getPortgroup().equals(managementPortGroup)) {
							summary.setHostIp(nic.getSpec().getIp().getIpAddress());
							summary.setHostNetmask(nic.getSpec().getIp().getSubnetMask());
							summary.setHostMacAddress(nic.getSpec().getMac());
							
						    if(s_logger.isTraceEnabled())
								s_logger.trace("vCenter API trace - getHyperHostNetworkSummary() done(successfully)");
							return summary;
						}
					}
				}
			}
		} else {
			// try with ESX path
			HostVirtualNic[] hostVNics = (HostVirtualNic[])_context.getServiceUtil().getDynamicProperty(_mor, 
				"config.network.consoleVnic");
			
			if(hostVNics != null) {
				for(HostVirtualNic vnic : hostVNics) {
					if(vnic.getPortgroup().equals(managementPortGroup)) {
						summary.setHostIp(vnic.getSpec().getIp().getIpAddress());
						summary.setHostNetmask(vnic.getSpec().getIp().getSubnetMask());
						summary.setHostMacAddress(vnic.getSpec().getMac());
						
					    if(s_logger.isTraceEnabled())
							s_logger.trace("vCenter API trace - getHyperHostNetworkSummary() done(successfully)");
						return summary;
					}
				}
			}
		}

	    if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - getHyperHostNetworkSummary() done(failed)");
		throw new Exception("Uanble to find management port group " + managementPortGroup);
	}
	
	@Override
	public ComputeResourceSummary getHyperHostHardwareSummary() throws Exception {
	    if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - getHyperHostHardwareSummary(). target MOR: " + _mor.get_value());

		//
		// This is to adopt the model when using Cluster as a big host while ComputeResourceSummary is used
		// directly from VMware resource pool
		//
		// When we break cluster hosts into individual hosts used in our resource allocator, 
		// we will have to populate ComputeResourceSummary by ourselves here
		//
		HostHardwareSummary hardwareSummary = getHostHardwareSummary();
		
		ComputeResourceSummary resourceSummary = new ComputeResourceSummary();
		
		// TODO: not sure how hyper-threading is counted in VMware
		short totalCores = (short)(hardwareSummary.getNumCpuCores()*hardwareSummary.getNumCpuPkgs());
		resourceSummary.setNumCpuCores(totalCores);
		
		// Note: memory here is in Byte unit
		resourceSummary.setTotalMemory(hardwareSummary.getMemorySize());
		
		// Total CPU is based on socket x core x Mhz
		int totalCpu = hardwareSummary.getCpuMhz() * totalCores;
		resourceSummary.setTotalCpu(totalCpu);

		HostListSummaryQuickStats stats = getHostQuickStats();
		if(stats.getOverallCpuUsage() == null || stats.getOverallMemoryUsage() == null)
			throw new Exception("Unable to get valid overal CPU/Memory usage data, host may be disconnected");
		
		resourceSummary.setEffectiveCpu(totalCpu - stats.getOverallCpuUsage());
		
		// Note effective memory is in MB unit
		resourceSummary.setEffectiveMemory(hardwareSummary.getMemorySize()/(1024*1024) - stats.getOverallMemoryUsage());
		
	    if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - getHyperHostHardwareSummary() done");
		return resourceSummary;
	}
	
	@Override
	public boolean isHyperHostConnected() throws Exception {
    	HostRuntimeInfo runtimeInfo = (HostRuntimeInfo)_context.getServiceUtil().getDynamicProperty(_mor, "runtime");
    	return runtimeInfo.getConnectionState() == HostSystemConnectionState.connected;
	}
}
