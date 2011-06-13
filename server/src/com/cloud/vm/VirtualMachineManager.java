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

import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.exception.ManagementServerException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.VirtualMachineMigrationException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.NetworkVO;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.user.Account;
import com.cloud.user.User;
import com.cloud.utils.Pair;
import com.cloud.utils.component.Manager;
import com.cloud.utils.fsm.NoTransitionException;

/**
 * Manages allocating resources to vms.
 */
public interface VirtualMachineManager extends Manager {
    
    <T extends VMInstanceVO> T allocate(T vm,
            VMTemplateVO template,
            ServiceOfferingVO serviceOffering,
            Pair<? extends DiskOfferingVO, Long> rootDiskOffering,
            List<Pair<DiskOfferingVO, Long>> dataDiskOfferings,
            List<Pair<NetworkVO, NicProfile>> networks,
            Map<VirtualMachineProfile.Param, Object> params,
            DeploymentPlan plan,
            HypervisorType hyperType,
            Account owner) throws InsufficientCapacityException;
    
    <T extends VMInstanceVO> T allocate(T vm,
            VMTemplateVO template,
            ServiceOfferingVO serviceOffering,
            Long rootSize,
            Pair<DiskOfferingVO, Long> dataDiskOffering,
            List<Pair<NetworkVO, NicProfile>> networks,
            DeploymentPlan plan,
            HypervisorType hyperType,
            Account owner) throws InsufficientCapacityException;
    
    <T extends VMInstanceVO> T allocate(T vm,
            VMTemplateVO template,
            ServiceOfferingVO serviceOffering,
            List<Pair<NetworkVO, NicProfile>> networkProfiles,
            DeploymentPlan plan,
            HypervisorType hyperType,
            Account owner) throws InsufficientCapacityException;
    
    <T extends VMInstanceVO> T start(T vm, Map<VirtualMachineProfile.Param, Object> params, User caller, Account account) throws InsufficientCapacityException, ResourceUnavailableException;
    
    <T extends VMInstanceVO> boolean stop(T vm, User caller, Account account) throws ResourceUnavailableException;
    
    <T extends VMInstanceVO> boolean expunge(T vm, User caller, Account account) throws ResourceUnavailableException;
    
    <T extends VMInstanceVO> void registerGuru(VirtualMachine.Type type, VirtualMachineGuru<T> guru);

	boolean stateTransitTo(VMInstanceVO vm, VirtualMachine.Event e, Long hostId) throws NoTransitionException;
	
    <T extends VMInstanceVO> T advanceStart(T vm, Map<VirtualMachineProfile.Param, Object> params, User caller, Account account) throws InsufficientCapacityException, ResourceUnavailableException, ConcurrentOperationException, OperationTimedoutException;
    
	<T extends VMInstanceVO> boolean advanceStop(T vm, boolean forced, User caller, Account account) throws ResourceUnavailableException, OperationTimedoutException, ConcurrentOperationException;
	
	<T extends VMInstanceVO> boolean advanceExpunge(T vm, User caller, Account account) throws ResourceUnavailableException, OperationTimedoutException, ConcurrentOperationException;
	
	<T extends VMInstanceVO> boolean remove(T vm, User caller, Account account);
	
	<T extends VMInstanceVO> boolean destroy(T vm, User caller, Account account) throws AgentUnavailableException, OperationTimedoutException, ConcurrentOperationException;
	
	boolean migrateAway(VirtualMachine.Type type, long vmid, long hostId) throws InsufficientServerCapacityException, VirtualMachineMigrationException;
	
	<T extends VMInstanceVO> T migrate(T vm, long srcHostId, DeployDestination dest) throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException, VirtualMachineMigrationException;
	
	<T extends VMInstanceVO> T reboot(T vm, Map<VirtualMachineProfile.Param, Object> params, User caller, Account account) throws InsufficientCapacityException, ResourceUnavailableException;
	
	<T extends VMInstanceVO> T advanceReboot(T vm, Map<VirtualMachineProfile.Param, Object> params, User caller, Account account) throws InsufficientCapacityException, ResourceUnavailableException, ConcurrentOperationException, OperationTimedoutException;
	
	VMInstanceVO findById(VirtualMachine.Type type, long vmId);
}
