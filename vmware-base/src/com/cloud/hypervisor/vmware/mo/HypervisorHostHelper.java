/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.hypervisor.vmware.mo;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.cloud.hypervisor.vmware.util.VmwareHelper;
import com.cloud.utils.ActionDelegate;
import com.cloud.utils.Pair;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.net.NetUtils;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.HostNetworkTrafficShapingPolicy;
import com.vmware.vim25.HostVirtualSwitch;
import com.vmware.vim25.HttpNfcLeaseDeviceUrl;
import com.vmware.vim25.HttpNfcLeaseInfo;
import com.vmware.vim25.HttpNfcLeaseState;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.OvfCreateImportSpecParams;
import com.vmware.vim25.OvfCreateImportSpecResult;
import com.vmware.vim25.OvfFileItem;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineFileInfo;
import com.vmware.vim25.VirtualMachineVideoCard;
import com.vmware.vim25.VirtualSCSISharing;

public class HypervisorHostHelper {
    private static final Logger s_logger = Logger.getLogger(HypervisorHostHelper.class);
    private static final int DEFAULT_LOCK_TIMEOUT_SECONDS = 600;
    
    // make vmware-base loosely coupled with cloud-specific stuff, duplicate VLAN.UNTAGGED constant here
    private static final String UNTAGGED_VLAN_NAME = "untagged";
	
	public static VirtualMachineMO findVmFromObjectContent(VmwareContext context, 
		ObjectContent[] ocs, String name) {
		
		if(ocs != null && ocs.length > 0) {
			for(ObjectContent oc : ocs) {
				DynamicProperty prop = oc.getPropSet(0);
				assert(prop != null);
				if(prop.getVal().toString().equals(name))
					return new VirtualMachineMO(context, oc.getObj());
			}
		}
		return null;
	}
	
	public static DatastoreMO getHyperHostDatastoreMO(VmwareHypervisorHost hyperHost, String datastoreName) throws Exception {
		ObjectContent[] ocs = hyperHost.getDatastorePropertiesOnHyperHost(new String[] { "name"} );
		if(ocs != null && ocs.length > 0) {
    		for(ObjectContent oc : ocs) {
		        DynamicProperty[] objProps = oc.getPropSet();
		        if(objProps != null) {
		        	for(DynamicProperty objProp : objProps) {
		        		if(objProp.getVal().toString().equals(datastoreName))
		        			return new DatastoreMO(hyperHost.getContext(), oc.getObj());
		        	}
		        }
    		}
		}
		return null;
	}
	
	public static String getPublicNetworkNamePrefix(String vlanId) {
		if (UNTAGGED_VLAN_NAME.equalsIgnoreCase(vlanId)) {
			return "cloud.public.untagged";
		} else {
			return "cloud.public." + vlanId;
		}
	}
	
	public static synchronized Pair<ManagedObjectReference, String> preparePublicNetwork(String vSwitchName,
			HostMO hostMo, String vlanId, Integer networkRateMbps, Integer networkRateMulticastMbps, long timeOutMs, boolean syncPeerHosts) throws Exception {
		
		HostVirtualSwitch vSwitch = hostMo.getHostVirtualSwitchByName(vSwitchName);
		if (vSwitch == null) {
			String msg = "Unable to find vSwitch configured for public network";
			s_logger.error(msg);
			throw new Exception(msg);
		}

		boolean createGCTag = false;
		String networkName;
		Integer vid = null;
		if(vlanId != null && !UNTAGGED_VLAN_NAME.equalsIgnoreCase(vlanId)) {
			createGCTag = true;
			vid = Integer.parseInt(vlanId);
		}
		
		networkName = composeCloudNetworkName("cloud.public", vlanId, networkRateMbps, vSwitchName);
		
		HostNetworkTrafficShapingPolicy shapingPolicy = null;
		if(networkRateMbps != null && networkRateMbps.intValue() > 0) {
			shapingPolicy = new HostNetworkTrafficShapingPolicy();
			shapingPolicy.setEnabled(true);
			shapingPolicy.setAverageBandwidth((long)networkRateMbps.intValue()*1024L*1024L);
		
			// 
			// TODO : people may have different opinion on how to set the following
			//
			
			// give 50% premium to peek
			shapingPolicy.setPeakBandwidth((long)(shapingPolicy.getAverageBandwidth()*1.5));

			// allow 5 seconds of burst transfer
			shapingPolicy.setBurstSize(5*shapingPolicy.getAverageBandwidth()/8);
		}
		
		if (!hostMo.hasPortGroup(vSwitch, networkName)) {
			hostMo.createPortGroup(vSwitch, networkName, vid, shapingPolicy);
		} else {
			hostMo.updatePortGroup(vSwitch, networkName, vid, shapingPolicy);
		}

		ManagedObjectReference morNetwork = waitForNetworkReady(hostMo, networkName, timeOutMs);
		if (morNetwork == null) {
			String msg = "Failed to create public network on vSwitch " + vSwitchName;
			s_logger.error(msg);
			throw new Exception(msg);
		}
		
		if(createGCTag) {
			NetworkMO networkMo = new NetworkMO(hostMo.getContext(), morNetwork);
			networkMo.setCustomFieldValue(CustomFieldConstants.CLOUD_GC, "true");
		}
		
		if(syncPeerHosts) {
			ManagedObjectReference morParent = hostMo.getParentMor();
			if(morParent != null && morParent.getType().equals("ClusterComputeResource")) {

				// to be conservative, lock cluster
				GlobalLock lock = GlobalLock.getInternLock("ClusterLock." + morParent.get_value());
				try {
					if(lock.lock(DEFAULT_LOCK_TIMEOUT_SECONDS)) {
						try {
							ManagedObjectReference[] hosts = (ManagedObjectReference[])hostMo.getContext().getServiceUtil().getDynamicProperty(morParent, "host");
							if(hosts != null) {
								for(ManagedObjectReference otherHost: hosts) {
									if(!otherHost.get_value().equals(hostMo.getMor().get_value())) {
										HostMO otherHostMo = new HostMO(hostMo.getContext(), otherHost);
										try {
											if(s_logger.isDebugEnabled())
												s_logger.debug("prepare public network on other host, vlan: " + vlanId + ", host: " + otherHostMo.getHostName());
											preparePublicNetwork(vSwitchName, otherHostMo, vlanId, networkRateMbps, networkRateMulticastMbps, timeOutMs, false);
										} catch(Exception e) {
											s_logger.warn("Unable to prepare public network on other host, vlan: " + vlanId + ", host: " + otherHostMo.getHostName());
										}
									}
								}
							}
						} finally {
							lock.unlock();
						}
					} else {
						s_logger.warn("Unable to lock cluster to prepare public network, vlan: " + vlanId);
					}
				} finally {
					lock.releaseRef();
				}
			}
		}

		s_logger.info("Network " + networkName + " is ready on vSwitch " + vSwitchName);
		return new Pair<ManagedObjectReference, String>(morNetwork, networkName);
	}
	
	public static Pair<ManagedObjectReference, String> preparePrivateNetwork(String vSwitchName, 
		HostMO hostMo, Integer vlanId, long timeOutMs) throws Exception {
		
		HostVirtualSwitch vSwitch = hostMo.getHostVirtualSwitchByName(vSwitchName);
		if (vSwitch == null) {
			String msg = "Unable to find vSwitch configured for private network";
			s_logger.error(msg);
			throw new Exception(msg);
		}
		
		String networkName;
		networkName = composeCloudNetworkName("cloud.private", vlanId == null ? null : String.valueOf(vlanId), null, vSwitchName);
				
		if (!hostMo.hasPortGroup(vSwitch, networkName)) {
			hostMo.createPortGroup(vSwitch, networkName, vlanId, null);
		}

		ManagedObjectReference morNetwork = waitForNetworkReady(hostMo, networkName, timeOutMs);
		if (morNetwork == null) {
			String msg = "Failed to create private network";
			s_logger.error(msg);
			throw new Exception(msg);
		}

		s_logger.info("Network " + networkName + " is ready on vSwitch " + vSwitchName);
		return new Pair<ManagedObjectReference, String>(morNetwork, networkName);
	}
	
	public static String composeCloudNetworkName(String prefix, String vlanId, Integer networkRateMbps, String vSwitchName) {
		StringBuffer sb = new StringBuffer(prefix);
		if(vlanId == null || UNTAGGED_VLAN_NAME.equalsIgnoreCase(vlanId))
			sb.append(".untagged");
		else
			sb.append(".").append(vlanId);
		
		if(networkRateMbps != null && networkRateMbps.intValue() > 0)
			sb.append(".").append(String.valueOf(networkRateMbps));
		else
			sb.append(".0");
		sb.append(".").append(VersioningContants.PORTGROUP_NAMING_VERSION);
		sb.append("-").append(vSwitchName);
		
		return sb.toString();
	}
	
	public static Pair<ManagedObjectReference, String> prepareGuestNetwork(String vSwitchName,
			HostMO hostMo, String vlanId, Integer networkRateMbps, Integer networkRateMulticastMbps, 
			long timeOutMs, boolean syncPeerHosts) throws Exception {

		HostVirtualSwitch vSwitch;
		vSwitch = hostMo.getHostVirtualSwitchByName(vSwitchName);

		if (vSwitch == null) {
			String msg = "Unable to find the default virtual switch";
			s_logger.error(msg);
			throw new Exception(msg);
		}

		boolean createGCTag = false;
		String networkName;
		Integer vid = null;
		
		if(vlanId != null && !UNTAGGED_VLAN_NAME.equalsIgnoreCase(vlanId)) {
			createGCTag = true;
			vid = Integer.parseInt(vlanId);
		}
		
		networkName = composeCloudNetworkName("cloud.guest", vlanId, networkRateMbps, vSwitchName);

		HostNetworkTrafficShapingPolicy shapingPolicy = null;
		if(networkRateMbps != null && networkRateMbps.intValue() > 0) {
			shapingPolicy = new HostNetworkTrafficShapingPolicy();
			shapingPolicy.setEnabled(true);
			shapingPolicy.setAverageBandwidth((long)networkRateMbps.intValue()*1024L*1024L);
		
			// 
			// TODO : people may have different opinion on how to set the following
			//
			
			// give 50% premium to peek
			shapingPolicy.setPeakBandwidth((long)(shapingPolicy.getAverageBandwidth()*1.5));

			// allow 5 seconds of burst transfer
			shapingPolicy.setBurstSize(5*shapingPolicy.getAverageBandwidth()/8);
		}

		if (!hostMo.hasPortGroup(vSwitch, networkName)) {
			hostMo.createPortGroup(vSwitch, networkName, vid, shapingPolicy);
		} else {
			hostMo.updatePortGroup(vSwitch, networkName, vid, shapingPolicy);
		}

		ManagedObjectReference morNetwork = waitForNetworkReady(hostMo, networkName, timeOutMs);
		if (morNetwork == null) {
			String msg = "Failed to create guest network " + networkName;
			s_logger.error(msg);
			throw new Exception(msg);
		}
		
		if(createGCTag) {
			NetworkMO networkMo = new NetworkMO(hostMo.getContext(), morNetwork);
			networkMo.setCustomFieldValue(CustomFieldConstants.CLOUD_GC, "true");
		}
		
		if(syncPeerHosts) {
			ManagedObjectReference morParent = hostMo.getParentMor();
			if(morParent != null && morParent.getType().equals("ClusterComputeResource")) {
				// to be conservative, lock cluster
				GlobalLock lock = GlobalLock.getInternLock("ClusterLock." + morParent.get_value());
				try {
					if(lock.lock(DEFAULT_LOCK_TIMEOUT_SECONDS)) {
						try {
							ManagedObjectReference[] hosts = (ManagedObjectReference[])hostMo.getContext().getServiceUtil().getDynamicProperty(morParent, "host");
							if(hosts != null) {
								for(ManagedObjectReference otherHost: hosts) {
									if(!otherHost.get_value().equals(hostMo.getMor().get_value())) {
										HostMO otherHostMo = new HostMO(hostMo.getContext(), otherHost);
										try {
											if(s_logger.isDebugEnabled())
												s_logger.debug("Prepare guest network on other host, vlan: " + vlanId + ", host: " + otherHostMo.getHostName());
											prepareGuestNetwork(vSwitchName, otherHostMo, vlanId, networkRateMbps, networkRateMulticastMbps, timeOutMs, false);
										} catch(Exception e) {
											s_logger.warn("Unable to prepare guest network on other host, vlan: " + vlanId + ", host: " + otherHostMo.getHostName());
										}
									}
								}
							}
						} finally {
							lock.unlock();
						}
					} else {
						s_logger.warn("Unable to lock cluster to prepare guest network, vlan: " + vlanId);
					}
				} finally {
					lock.releaseRef();
				}
			}
		}

		s_logger.info("Network " + networkName + " is ready on vSwitch " + vSwitchName);
		return new Pair<ManagedObjectReference, String>(morNetwork, networkName);
	}
	
	public static ManagedObjectReference waitForNetworkReady(HostMO hostMo,
			String networkName, long timeOutMs) throws Exception {

		ManagedObjectReference morNetwork = null;

		// if portGroup is just created, getNetwork may fail to retrieve it, we
		// need to retry
		long startTick = System.currentTimeMillis();
		while (System.currentTimeMillis() - startTick <= timeOutMs) {
			morNetwork = hostMo.getNetworkMor(networkName);
			if (morNetwork != null) {
				break;
			}

			s_logger.info("Waiting for network " + networkName + " to be ready");
			Thread.sleep(1000);
		}

		return morNetwork;
	}
	
	public static boolean createBlankVm(VmwareHypervisorHost host, String vmName, 
		int cpuCount, int cpuSpeedMHz, int cpuReservedMHz, boolean limitCpuUse, int memoryMB, String guestOsIdentifier, 
		ManagedObjectReference morDs, boolean snapshotDirToParent) throws Exception {
		
		if(s_logger.isInfoEnabled())
			s_logger.info("Create blank VM. cpuCount: " + cpuCount + ", cpuSpeed(MHz): " + cpuSpeedMHz + ", mem(Mb): " + memoryMB);
		
		// VM config basics
		VirtualMachineConfigSpec vmConfig = new VirtualMachineConfigSpec();
		vmConfig.setName(vmName);
		VmwareHelper.setBasicVmConfig(vmConfig, cpuCount, cpuSpeedMHz, cpuReservedMHz, memoryMB, guestOsIdentifier, limitCpuUse);

		// Scsi controller
		VirtualLsiLogicController scsiController = new VirtualLsiLogicController();
		scsiController.setSharedBus(VirtualSCSISharing.noSharing);
		scsiController.setBusNumber(0);
		scsiController.setKey(1);
		VirtualDeviceConfigSpec scsiControllerSpec = new VirtualDeviceConfigSpec();
		scsiControllerSpec.setDevice(scsiController);
		scsiControllerSpec.setOperation(VirtualDeviceConfigSpecOperation.add);

		VirtualMachineFileInfo fileInfo = new VirtualMachineFileInfo();
		DatastoreMO dsMo = new DatastoreMO(host.getContext(), morDs);
		fileInfo.setVmPathName(String.format("[%s]", dsMo.getName()));
		vmConfig.setFiles(fileInfo);
		
		VirtualMachineVideoCard videoCard = new VirtualMachineVideoCard();
		videoCard.setControllerKey(100);
		videoCard.setUseAutoDetect(true);
		
		VirtualDeviceConfigSpec videoDeviceSpec = new VirtualDeviceConfigSpec();
		videoDeviceSpec.setDevice(videoCard);
		videoDeviceSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
		
		vmConfig.setDeviceChange(new VirtualDeviceConfigSpec[] { scsiControllerSpec, videoDeviceSpec });
		if(host.createVm(vmConfig)) {
			VirtualMachineMO vmMo = host.findVmOnHyperHost(vmName);
			assert(vmMo != null);
			
			int ideControllerKey = -1;
			while(ideControllerKey < 0) {
				ideControllerKey = vmMo.tryGetIDEDeviceControllerKey();
				if(ideControllerKey >= 0)
					break;
				
				s_logger.info("Waiting for IDE controller be ready in VM: " + vmName);
				Thread.sleep(1000);
			}
			
			if(snapshotDirToParent) {
				String snapshotDir = String.format("/vmfs/volumes/%s/", dsMo.getName());
				
				s_logger.info("Switch snapshot working directory to " + snapshotDir + " for " + vmName);
				vmMo.setSnapshotDirectory(snapshotDir);
				
				// Don't have a good way to test if the VM is really ready for use through normal API after configuration file manipulation,
				// delay 3 seconds
				Thread.sleep(3000);
			}
			
			s_logger.info("Blank VM: " + vmName + " is ready for use");
			return true;
		}
		return false;
	}
	
	public static String resolveHostNameInUrl(DatacenterMO dcMo, String url) {
		
		s_logger.info("Resolving host name in url through vCenter, url: " + url);
		
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			s_logger.warn("URISyntaxException on url " + url);
			return url;
		}
		
		String host = uri.getHost();
		if(NetUtils.isValidIp(host)) {
			s_logger.info("host name in url is already in IP address, url: " + url);
			return url;
		}
		
		try {
			ManagedObjectReference morHost = dcMo.findHost(host);
			if(morHost != null) {
				HostMO hostMo = new HostMO(dcMo.getContext(), morHost);
				String managementPortGroupName;
				if(hostMo.getHostType() == VmwareHostType.ESXi)
					managementPortGroupName = (String)dcMo.getContext().getStockObject("manageportgroup");
				else
					managementPortGroupName = (String)dcMo.getContext().getStockObject("serviceconsole");
				
				VmwareHypervisorHostNetworkSummary summary = hostMo.getHyperHostNetworkSummary(managementPortGroupName);
				if(summary == null) {
					s_logger.warn("Unable to resolve host name in url through vSphere, url: " + url);
					return url;
				}
				
				String hostIp = summary.getHostIp();
				
				try {
					URI resolvedUri = new URI(uri.getScheme(), uri.getUserInfo(), hostIp, uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
					
					s_logger.info("url " + url + " is resolved to " + resolvedUri.toString() + " through vCenter");
					return resolvedUri.toString();
				} catch (URISyntaxException e) {
					assert(false);
					return url;
				}
			}
		} catch(Exception e) {
			s_logger.warn("Unexpected exception ", e);
		}
		
		return url;
	}
	
	public static void importVmFromOVF(VmwareHypervisorHost host, String ovfFilePath, String vmName, DatastoreMO dsMo, String diskOption, 
		ManagedObjectReference morRp, ManagedObjectReference morHost) throws Exception {
		
		assert(morRp != null);
		
		OvfCreateImportSpecParams importSpecParams = new OvfCreateImportSpecParams();  
		importSpecParams.setHostSystem(morHost);  
		importSpecParams.setLocale("US");  
		importSpecParams.setEntityName(vmName);  
		importSpecParams.setDeploymentOption("");
		importSpecParams.setDiskProvisioning(diskOption); // diskOption: thin, thick, etc
		importSpecParams.setPropertyMapping(null);
		
		String ovfDescriptor = HttpNfcLeaseMO.readOvfContent(ovfFilePath);
		VmwareContext context = host.getContext();
		OvfCreateImportSpecResult ovfImportResult = context.getService().createImportSpec(
			context.getServiceContent().getOvfManager(), ovfDescriptor, morRp, 
			dsMo.getMor(), importSpecParams);
		
		if(ovfImportResult == null) {
			String msg = "createImportSpec() failed. ovfFilePath: " + ovfFilePath + ", vmName: " 
				+ vmName + ", diskOption: " + diskOption;
			s_logger.error(msg);
			throw new Exception(msg);
		}
		
		DatacenterMO dcMo = new DatacenterMO(context, host.getHyperHostDatacenter());
		ManagedObjectReference morLease = context.getService().importVApp(morRp, 
			ovfImportResult.getImportSpec(), dcMo.getVmFolder(), morHost);
		if(morLease == null) {
			String msg = "importVApp() failed. ovfFilePath: " + ovfFilePath + ", vmName: " 
				+ vmName + ", diskOption: " + diskOption;
			s_logger.error(msg);
			throw new Exception(msg);
		}
		final HttpNfcLeaseMO leaseMo = new HttpNfcLeaseMO(context, morLease);
		HttpNfcLeaseState state = leaseMo.waitState(
			new HttpNfcLeaseState[] { HttpNfcLeaseState.ready, HttpNfcLeaseState.error });
		try {
			if(state == HttpNfcLeaseState.ready) {
				final long totalBytes = HttpNfcLeaseMO.calcTotalBytes(ovfImportResult);
				File ovfFile = new File(ovfFilePath); 
				
				HttpNfcLeaseInfo httpNfcLeaseInfo = leaseMo.getLeaseInfo();
		        HttpNfcLeaseDeviceUrl[] deviceUrls = httpNfcLeaseInfo.getDeviceUrl();  
		        long bytesAlreadyWritten = 0;
		        
		        final HttpNfcLeaseMO.ProgressReporter progressReporter = leaseMo.createProgressReporter();
		        try {
			        for (HttpNfcLeaseDeviceUrl deviceUrl : deviceUrls) {
			        	String deviceKey = deviceUrl.getImportKey();  
			        	for (OvfFileItem ovfFileItem : ovfImportResult.getFileItem()) {
			        		if (deviceKey.equals(ovfFileItem.getDeviceId())) {  
			        			String absoluteFile = ovfFile.getParent() + File.separator + ovfFileItem.getPath();
			        			String urlToPost = deviceUrl.getUrl();
			        			urlToPost = resolveHostNameInUrl(dcMo, urlToPost);
			        			
		        			  	context.uploadVmdkFile(ovfFileItem.isCreate() ? "PUT" : "POST", urlToPost, absoluteFile, 
		    			  			bytesAlreadyWritten, new ActionDelegate<Long> () {
									public void action(Long param) {
										progressReporter.reportProgress((int)(param * 100 / totalBytes));
									}
		    			  		});  
		        			  	
		        			  	bytesAlreadyWritten += ovfFileItem.getSize();
		        			 }  
			        	 }  
			        }
		        } finally {
		        	progressReporter.close();
		        }
		        leaseMo.updateLeaseProgress(100);
			}
		} finally {
			leaseMo.completeLease();
		}
	}
}
