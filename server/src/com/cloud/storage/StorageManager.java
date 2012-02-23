/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
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
package com.cloud.storage;

import java.util.List;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.manager.Commands;
import com.cloud.capacity.CapacityVO;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientStorageCapacityException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.storage.Volume.Event;
import com.cloud.storage.Volume.Type;
import com.cloud.user.Account;
import com.cloud.utils.Pair;
import com.cloud.utils.component.Manager;
import com.cloud.utils.fsm.NoTransitionException;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

public interface StorageManager extends StorageService, Manager {
    boolean canVmRestartOnAnotherServer(long vmId);

    /** Returns the absolute path of the specified ISO
     * @param templateId - the ID of the template that represents the ISO
     * @param datacenterId
     * @return absolute ISO path
     */
	public Pair<String, String> getAbsoluteIsoPath(long templateId, long dataCenterId);
	
	/**
	 * Returns the URL of the secondary storage host
	 * @param zoneId
	 * @return URL
	 */
	public String getSecondaryStorageURL(long zoneId);
	
	/**
	 * Returns a comma separated list of tags for the specified storage pool
	 * @param poolId
	 * @return comma separated list of tags
	 */
	public String getStoragePoolTags(long poolId);
	
	/**
	 * Returns the secondary storage host
	 * @param zoneId
	 * @return secondary storage host
	 */
	public HostVO getSecondaryStorageHost(long zoneId);
	
	/**
	 * Returns the secondary storage host
	 * @param zoneId
	 * @return secondary storage host
	 */
    public VMTemplateHostVO findVmTemplateHost(long templateId, StoragePool pool);

	/**
	 * Moves a volume from its current storage pool to a storage pool with enough capacity in the specified zone, pod, or cluster
	 * @param volume
	 * @param destPoolDcId
	 * @param destPoolPodId
	 * @param destPoolClusterId
	 * @return VolumeVO
	 * @throws ConcurrentOperationException 
	 */
	VolumeVO moveVolume(VolumeVO volume, long destPoolDcId, Long destPoolPodId, Long destPoolClusterId, HypervisorType dataDiskHyperType) throws ConcurrentOperationException;

	/**
	 * Create a volume based on the given criteria
	 * @param volume
	 * @param vm
	 * @param template
	 * @param dc
	 * @param pod
	 * @param clusterId
	 * @param offering
	 * @param diskOffering
	 * @param avoids
	 * @param size
	 * @param hyperType
	 * @return volume VO if success, null otherwise
	 */
	VolumeVO createVolume(VolumeVO volume, VMInstanceVO vm, VMTemplateVO template, DataCenterVO dc, HostPodVO pod, Long clusterId,
            ServiceOfferingVO offering, DiskOfferingVO diskOffering, List<StoragePoolVO> avoids, long size, HypervisorType hyperType);

	/**
	 * Marks the specified volume as destroyed in the management server database. The expunge thread will delete the volume from its storage pool.
	 * @param volume
	 * @return 
	 */
	boolean destroyVolume(VolumeVO volume) throws ConcurrentOperationException;
	
	/** Create capacity entries in the op capacity table
	 * @param storagePool
	 */
	public void createCapacityEntry(StoragePoolVO storagePool);

	/**
	 * Checks that the volume is stored on a shared storage pool
	 * @param volume
	 * @return true if the volume is on a shared storage pool, false otherwise
	 */
	boolean volumeOnSharedStoragePool(VolumeVO volume);

	Answer sendToPool(long poolId, Command cmd) throws StorageUnavailableException;
	Answer sendToPool(StoragePool pool, Command cmd) throws StorageUnavailableException;
	Answer[] sendToPool(long poolId, Commands cmd) throws StorageUnavailableException;
    Answer[] sendToPool(StoragePool pool, Commands cmds) throws StorageUnavailableException;
	Pair<Long, Answer[]> sendToPool(StoragePool pool, long[] hostIdsToTryFirst, List<Long> hostIdsToAvoid, Commands cmds) throws StorageUnavailableException;
	Pair<Long, Answer> sendToPool(StoragePool pool, long[] hostIdsToTryFirst, List<Long> hostIdsToAvoid, Command cmd) throws StorageUnavailableException;
	
	/**
	 * Checks that one of the following is true:
	 * 1. The volume is not attached to any VM
	 * 2. The volume is attached to a VM that is running on a host with the KVM hypervisor, and the VM is stopped
	 * 3. The volume is attached to a VM that is running on a host with the XenServer hypervisor (the VM can be stopped or running)
	 * @return true if one of the above conditions is true
	 */
	boolean volumeInactive(VolumeVO volume);
	
	String getVmNameOnVolume(VolumeVO volume);
	
	/**
	 * Checks if a host has running VMs that are using its local storage pool.
	 * @return true if local storage is active on the host
	 */
	boolean isLocalStorageActiveOnHost(Host host);
	
    /**
	 * Cleans up storage pools by removing unused templates.
	 * @param recurring - true if this cleanup is part of a recurring garbage collection thread
	 */
	void cleanupStorage(boolean recurring);
	
    String getPrimaryStorageNameLabel(VolumeVO volume);

    /**
     * Allocates one volume.
     * @param <T>
     * @param type
     * @param offering
     * @param name
     * @param size
     * @param template
     * @param vm
     * @param account
     * @return VolumeVO a persisted volume.
     */
    <T extends VMInstanceVO> DiskProfile allocateRawVolume(Type type, String name, DiskOfferingVO offering, Long size, T vm, Account owner);
    <T extends VMInstanceVO> DiskProfile allocateTemplatedVolume(Type type, String name, DiskOfferingVO offering, VMTemplateVO template, T vm, Account owner);
    
	void createCapacityEntry(StoragePoolVO storagePool, short capacityType, long allocated);

    
    void prepare(VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest) throws StorageUnavailableException, InsufficientStorageCapacityException, ConcurrentOperationException;

	void release(VirtualMachineProfile<? extends VMInstanceVO> profile);

	void cleanupVolumes(long vmId) throws ConcurrentOperationException;
	
	void prepareForMigration(VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest);

	Answer sendToPool(StoragePool pool, long[] hostIdsToTryFirst, Command cmd) throws StorageUnavailableException;

	CapacityVO getSecondaryStorageUsedStats(Long hostId, Long zoneId);

	CapacityVO getStoragePoolUsedStats(Long poolId, Long clusterId, Long podId, Long zoneId);

    boolean createStoragePool(long hostId, StoragePoolVO pool);

    boolean delPoolFromHost(long hostId);

    HostVO getSecondaryStorageHost(long zoneId, long tmpltId);

    List<HostVO> getSecondaryStorageHosts(long zoneId);

    List<StoragePoolVO> ListByDataCenterHypervisor(long datacenterId, HypervisorType type);


    List<VMInstanceVO> listByStoragePool(long storagePoolId);

    StoragePoolVO findLocalStorageOnHost(long hostId);

    VMTemplateHostVO getTemplateHostRef(long zoneId, long tmpltId, boolean readyOnly);

	boolean StorageMigration(
			VirtualMachineProfile<? extends VirtualMachine> vm,
			StoragePool destPool) throws ConcurrentOperationException;

	boolean stateTransitTo(Volume vol, Event event)
			throws NoTransitionException;
	
	VolumeVO allocateDuplicateVolume(VolumeVO oldVol, Long templateId);

	Host updateSecondaryStorage(long secStorageId, String newUrl);

	List<Long> getUpHostsInPool(long poolId);

    void cleanupSecondaryStorage(boolean recurring);
}
