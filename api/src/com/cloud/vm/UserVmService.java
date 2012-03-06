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
package com.cloud.vm;

import java.util.List;
import java.util.Map;

import javax.naming.InsufficientResourcesException;

import com.cloud.api.commands.AssignVMCmd;
import com.cloud.api.commands.AttachVolumeCmd;
import com.cloud.api.commands.CreateTemplateCmd;
import com.cloud.api.commands.CreateVMGroupCmd;
import com.cloud.api.commands.DeleteVMGroupCmd;
import com.cloud.api.commands.DeployVMCmd;
import com.cloud.api.commands.DestroyVMCmd;
import com.cloud.api.commands.DetachVolumeCmd;
import com.cloud.api.commands.ListVMsCmd;
import com.cloud.api.commands.RebootVMCmd;
import com.cloud.api.commands.RecoverVMCmd;
import com.cloud.api.commands.ResetVMPasswordCmd;
import com.cloud.api.commands.RestoreVMCmd;
import com.cloud.api.commands.StartVMCmd;
import com.cloud.api.commands.UpdateVMCmd;
import com.cloud.api.commands.UpgradeVMCmd;
import com.cloud.dc.DataCenter;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ManagementServerException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.exception.VirtualMachineMigrationException;
import com.cloud.host.Host;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.offering.ServiceOffering;
import com.cloud.storage.StoragePool;
import com.cloud.storage.Volume;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.utils.exception.ExecutionException;

public interface UserVmService {
    /**
     * Destroys one virtual machine
     * 
     * @param userId
     *            the id of the user performing the action
     * @param vmId
     *            the id of the virtual machine.
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    UserVm destroyVm(DestroyVMCmd cmd) throws ResourceUnavailableException, ConcurrentOperationException;

    /**
     * Destroys one virtual machine
     * 
     * @param userId
     *            the id of the user performing the action
     * @param vmId
     *            the id of the virtual machine.
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    UserVm destroyVm(long vmId) throws ResourceUnavailableException, ConcurrentOperationException;

    /**
     * Resets the password of a virtual machine.
     * 
     * @param cmd
     *            - the command specifying vmId, password
     * @return the VM if reset worked successfully, null otherwise
     */
    UserVm resetVMPassword(ResetVMPasswordCmd cmd, String password) throws ResourceUnavailableException, InsufficientCapacityException;

    /**
     * Attaches the specified volume to the specified VM
     * 
     * @param cmd
     *            - the command specifying volumeId and vmId
     * @return the Volume object if attach worked successfully.
     */
    Volume attachVolumeToVM(AttachVolumeCmd cmd);

    /**
     * Detaches the specified volume from the VM it is currently attached to.
     * 
     * @param cmd
     *            - the command specifying volumeId
     * @return the Volume object if detach worked successfully.
     */
    Volume detachVolumeFromVM(DetachVolumeCmd cmmd);

    UserVm startVirtualMachine(StartVMCmd cmd) throws StorageUnavailableException, ExecutionException, ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException,
            ResourceAllocationException;

    UserVm rebootVirtualMachine(RebootVMCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException;

    UserVm updateVirtualMachine(UpdateVMCmd cmd);

    UserVm recoverVirtualMachine(RecoverVMCmd cmd) throws ResourceAllocationException;

    /**
     * Create a template database record in preparation for creating a private template.
     * 
     * @param cmd
     *            the command object that defines the name, display text, snapshot/volume, bits, public/private, etc.
     *            for the
     *            private template
     * @param templateOwner
     *            TODO
     * @return the vm template object if successful, null otherwise
     * @throws ResourceAllocationException
     */
    VirtualMachineTemplate createPrivateTemplateRecord(CreateTemplateCmd cmd, Account templateOwner) throws ResourceAllocationException;

    /**
     * Creates a private template from a snapshot of a VM
     * 
     * @param cmd
     *            - the command specifying snapshotId, name, description
     * @return a template if successfully created, null otherwise
     */
    VirtualMachineTemplate createPrivateTemplate(CreateTemplateCmd cmd);

    /**
     * Creates a Basic Zone User VM in the database and returns the VM to the caller.
     * 
     * @param zone
     *            - availability zone for the virtual machine
     * @param serviceOffering
     *            - the service offering for the virtual machine
     * @param template
     *            - the template for the virtual machine
     * @param securityGroupIdList
     *            - comma separated list of security groups id that going to be applied to the virtual machine
     * @param hostName
     *            - host name for the virtual machine
     * @param displayName
     *            - an optional user generated name for the virtual machine
     * @param diskOfferingId
     *            - the ID of the disk offering for the virtual machine. If the template is of ISO format, the
     *            diskOfferingId is
     *            for the root disk volume. Otherwise this parameter is used to indicate the offering for the data disk
     *            volume.
     *            If the templateId parameter passed is from a Template object, the diskOfferingId refers to a DATA Disk
     *            Volume
     *            created. If the templateId parameter passed is from an ISO object, the diskOfferingId refers to a ROOT
     *            Disk
     *            Volume created
     * @param diskSize
     *            - the arbitrary size for the DATADISK volume. Mutually exclusive with diskOfferingId
     * @param group
     *            - an optional group for the virtual machine
     * @param hypervisor
     *            - the hypervisor on which to deploy the virtual machine
     * @param userData
     *            - an optional binary data that can be sent to the virtual machine upon a successful deployment. This
     *            binary
     *            data must be base64 encoded before adding it to the request. Currently only HTTP GET is supported.
     *            Using HTTP
     *            GET (via querystring), you can send up to 2KB of data after base64 encoding
     * @param sshKeyPair
     *            - name of the ssh key pair used to login to the virtual machine
     * @param requestedIps
     *            TODO
     * @param defaultIp
     *            TODO
     * @param accountName
     *            - an optional account for the virtual machine. Must be used with domainId
     * @param domainId
     *            - an optional domainId for the virtual machine. If the account parameter is used, domainId must also
     *            be used
     * @return UserVm object if successful.
     * 
     * @throws InsufficientCapacityException
     *             if there is insufficient capacity to deploy the VM.
     * @throws ConcurrentOperationException
     *             if there are multiple users working on the same VM or in the same environment.
     * @throws ResourceUnavailableException
     *             if the resources required to deploy the VM is not currently available.
     * @throws InsufficientResourcesException
     */
    UserVm createBasicSecurityGroupVirtualMachine(DataCenter zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, List<Long> securityGroupIdList, Account owner, String hostName,
            String displayName, Long diskOfferingId, Long diskSize, String group, HypervisorType hypervisor, String userData, String sshKeyPair, Map<Long, String> requestedIps, String defaultIp, String keyboard)
            throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException, StorageUnavailableException, ResourceAllocationException;

    /**
     * Creates a User VM in Advanced Zone (Security Group feature is enabled) in the database and returns the VM to the
     * caller.
     * 
     * @param zone
     *            - availability zone for the virtual machine
     * @param serviceOffering
     *            - the service offering for the virtual machine
     * @param template
     *            - the template for the virtual machine
     * @param networkIdList
     *            - list of network ids used by virtual machine
     * @param securityGroupIdList
     *            - comma separated list of security groups id that going to be applied to the virtual machine
     * @param hostName
     *            - host name for the virtual machine
     * @param displayName
     *            - an optional user generated name for the virtual machine
     * @param diskOfferingId
     *            - the ID of the disk offering for the virtual machine. If the template is of ISO format, the
     *            diskOfferingId is
     *            for the root disk volume. Otherwise this parameter is used to indicate the offering for the data disk
     *            volume.
     *            If the templateId parameter passed is from a Template object, the diskOfferingId refers to a DATA Disk
     *            Volume
     *            created. If the templateId parameter passed is from an ISO object, the diskOfferingId refers to a ROOT
     *            Disk
     *            Volume created
     * @param diskSize
     *            - the arbitrary size for the DATADISK volume. Mutually exclusive with diskOfferingId
     * @param group
     *            - an optional group for the virtual machine
     * @param hypervisor
     *            - the hypervisor on which to deploy the virtual machine
     * @param userData
     *            - an optional binary data that can be sent to the virtual machine upon a successful deployment. This
     *            binary
     *            data must be base64 encoded before adding it to the request. Currently only HTTP GET is supported.
     *            Using HTTP
     *            GET (via querystring), you can send up to 2KB of data after base64 encoding
     * @param sshKeyPair
     *            - name of the ssh key pair used to login to the virtual machine
     * @param requestedIps
     *            TODO
     * @param defaultIp
     *            TODO
     * @param accountName
     *            - an optional account for the virtual machine. Must be used with domainId
     * @param domainId
     *            - an optional domainId for the virtual machine. If the account parameter is used, domainId must also
     *            be used
     * @return UserVm object if successful.
     * 
     * @throws InsufficientCapacityException
     *             if there is insufficient capacity to deploy the VM.
     * @throws ConcurrentOperationException
     *             if there are multiple users working on the same VM or in the same environment.
     * @throws ResourceUnavailableException
     *             if the resources required to deploy the VM is not currently available.
     * @throws InsufficientResourcesException
     */
    UserVm createAdvancedSecurityGroupVirtualMachine(DataCenter zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, List<Long> networkIdList, List<Long> securityGroupIdList,
            Account owner, String hostName, String displayName, Long diskOfferingId, Long diskSize, String group, HypervisorType hypervisor, String userData, String sshKeyPair, Map<Long, String> requestedIps,
            String defaultIp, String keyboard)
            throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException, StorageUnavailableException, ResourceAllocationException;

    /**
     * Creates a User VM in Advanced Zone (Security Group feature is disabled) in the database and returns the VM to the
     * caller.
     * 
     * @param zone
     *            - availability zone for the virtual machine
     * @param serviceOffering
     *            - the service offering for the virtual machine
     * @param template
     *            - the template for the virtual machine
     * @param networkIdList
     *            - list of network ids used by virtual machine
     * @param hostName
     *            - host name for the virtual machine
     * @param displayName
     *            - an optional user generated name for the virtual machine
     * @param diskOfferingId
     *            - the ID of the disk offering for the virtual machine. If the template is of ISO format, the
     *            diskOfferingId is
     *            for the root disk volume. Otherwise this parameter is used to indicate the offering for the data disk
     *            volume.
     *            If the templateId parameter passed is from a Template object, the diskOfferingId refers to a DATA Disk
     *            Volume
     *            created. If the templateId parameter passed is from an ISO object, the diskOfferingId refers to a ROOT
     *            Disk
     *            Volume created
     * @param diskSize
     *            - the arbitrary size for the DATADISK volume. Mutually exclusive with diskOfferingId
     * @param group
     *            - an optional group for the virtual machine
     * @param hypervisor
     *            - the hypervisor on which to deploy the virtual machine
     * @param userData
     *            - an optional binary data that can be sent to the virtual machine upon a successful deployment. This
     *            binary
     *            data must be base64 encoded before adding it to the request. Currently only HTTP GET is supported.
     *            Using HTTP
     *            GET (via querystring), you can send up to 2KB of data after base64 encoding
     * @param sshKeyPair
     *            - name of the ssh key pair used to login to the virtual machine
     * @param requestedIps
     *            TODO
     * @param defaultIp
     *            TODO
     * @param accountName
     *            - an optional account for the virtual machine. Must be used with domainId
     * @param domainId
     *            - an optional domainId for the virtual machine. If the account parameter is used, domainId must also
     *            be used
     * @return UserVm object if successful.
     * 
     * @throws InsufficientCapacityException
     *             if there is insufficient capacity to deploy the VM.
     * @throws ConcurrentOperationException
     *             if there are multiple users working on the same VM or in the same environment.
     * @throws ResourceUnavailableException
     *             if the resources required to deploy the VM is not currently available.
     * @throws InsufficientResourcesException
     */
    UserVm createAdvancedVirtualMachine(DataCenter zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, List<Long> networkIdList, Account owner, String hostName,
            String displayName, Long diskOfferingId, Long diskSize, String group, HypervisorType hypervisor, String userData, String sshKeyPair, Map<Long, String> requestedIps, String defaultIp, String keyboard)
            throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException, StorageUnavailableException, ResourceAllocationException;

    /**
     * Starts the virtual machine created from createVirtualMachine.
     * 
     * @param cmd
     *            Command to deploy.
     * @return UserVm object if successful.
     * @throws InsufficientCapacityException
     *             if there is insufficient capacity to deploy the VM.
     * @throws ConcurrentOperationException
     *             if there are multiple users working on the same VM.
     * @throws ResourceUnavailableException
     *             if the resources required the deploy the VM is not currently available.
     */
    UserVm startVirtualMachine(DeployVMCmd cmd) throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException;

    /**
     * Creates a vm group.
     * 
     * @param name
     *            - name of the group
     * @param accountId
     *            - accountId
     */
    InstanceGroup createVmGroup(CreateVMGroupCmd cmd);

    boolean deleteVmGroup(DeleteVMGroupCmd cmd);

    /**
     * upgrade the service offering of the virtual machine
     * 
     * @param cmd
     *            - the command specifying vmId and new serviceOfferingId
     * @return the vm
     */
    UserVm upgradeVirtualMachine(UpgradeVMCmd cmd);

    UserVm stopVirtualMachine(long vmId, boolean forced) throws ConcurrentOperationException;

    UserVm startVirtualMachine(long vmId, Long hostId) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException;

    void deletePrivateTemplateRecord(Long templateId);

    /**
     * Obtains a list of virtual machines by the specified search criteria. Can search by: "userId", "name", "state",
     * "dataCenterId", "podId", "hostId"
     * 
     * @param cmd
     *            the API command that wraps the search criteria
     * @return List of UserVMs.
     */
    List<? extends UserVm> searchForUserVMs(ListVMsCmd cmd);

    HypervisorType getHypervisorTypeOfUserVM(long vmid);

    UserVm createVirtualMachine(DeployVMCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException, ConcurrentOperationException, StorageUnavailableException,
            ResourceAllocationException;

    UserVm getUserVm(long vmId);

    /**
     * Migrate the given VM to the destination host provided. The API returns the migrated VM if migration succeeds.
     * Only Root
     * Admin can migrate a VM.
     * 
     * @param destinationStorage
     *            TODO
     * @param Long
     *            vmId
     *            vmId of The VM to migrate
     * @param Host
     *            destinationHost to migrate the VM
     * 
     * @return VirtualMachine migrated VM
     * @throws ManagementServerException
     *             in case we get error finding the VM or host or access errors or other internal errors.
     * @throws ConcurrentOperationException
     *             if there are multiple users working on the same VM.
     * @throws ResourceUnavailableException
     *             if the destination host to migrate the VM is not currently available.
     * @throws VirtualMachineMigrationException
     *             if the VM to be migrated is not in Running state
     */
    VirtualMachine migrateVirtualMachine(Long vmId, Host destinationHost) throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException, VirtualMachineMigrationException;

    UserVm moveVMToUser(AssignVMCmd moveUserVMCmd) throws ResourceAllocationException, ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException;

    VirtualMachine vmStorageMigration(Long vmId, StoragePool destPool);

    UserVm restoreVM(RestoreVMCmd cmd);
}
