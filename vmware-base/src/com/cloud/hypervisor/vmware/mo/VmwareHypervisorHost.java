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

import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.vmware.vim25.ClusterDasConfigInfo;
import com.vmware.vim25.ComputeResourceSummary;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.VirtualMachineConfigSpec;

/**
 * Interface to consolidate ESX(i) hosts and HA/FT clusters into a common interface used by Cloud.com Hypervisor resources  
 */
public interface VmwareHypervisorHost {
	VmwareContext getContext();
	ManagedObjectReference getMor();
	
	String getHyperHostName() throws Exception;
	
	ClusterDasConfigInfo getDasConfig() throws Exception;
	
	ManagedObjectReference getHyperHostDatacenter() throws Exception;
	ManagedObjectReference getHyperHostOwnerResourcePool() throws Exception;
	ManagedObjectReference getHyperHostCluster() throws Exception;
	
	boolean isHyperHostConnected() throws Exception;
	String getHyperHostDefaultGateway() throws Exception;
	
	VirtualMachineMO findVmOnHyperHost(String name) throws Exception;
	VirtualMachineMO findVmOnPeerHyperHost(String name) throws Exception;
	
	boolean createVm(VirtualMachineConfigSpec vmSpec) throws Exception;
	boolean createBlankVm(String vmName, int cpuCount, int cpuSpeedMHz, int cpuReservedMHz, boolean limitCpuUse, int memoryMB, 
		String guestOsIdentifier, ManagedObjectReference morDs, boolean snapshotDirToParent) throws Exception;
	void importVmFromOVF(String ovfFilePath, String vmName, DatastoreMO dsMo, String diskOption) throws Exception;

	ObjectContent[] getVmPropertiesOnHyperHost(String[] propertyPaths) throws Exception;
	ObjectContent[] getDatastorePropertiesOnHyperHost(String[] propertyPaths) throws Exception;
	
	ManagedObjectReference mountDatastore(boolean vmfsDatastore, String poolHostAddress, 
		int poolHostPort, String poolPath, String poolUuid) throws Exception;
	void unmountDatastore(String poolUuid) throws Exception;
	
	ManagedObjectReference findDatastore(String poolUuid) throws Exception;
	
	@Deprecated
	ManagedObjectReference findDatastoreByExportPath(String exportPath) throws Exception;
	
	ManagedObjectReference findMigrationTarget(VirtualMachineMO vmMo) throws Exception;
	
	VmwareHypervisorHostResourceSummary getHyperHostResourceSummary() throws Exception;
	VmwareHypervisorHostNetworkSummary getHyperHostNetworkSummary(String esxServiceConsolePort) throws Exception;
	ComputeResourceSummary getHyperHostHardwareSummary() throws Exception;
}
