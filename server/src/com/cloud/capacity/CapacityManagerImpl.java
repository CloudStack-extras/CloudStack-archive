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
package com.cloud.capacity;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.exception.ConnectionException;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.offering.ServiceOffering;
import com.cloud.resource.ResourceListener;
import com.cloud.resource.ResourceManager;
import com.cloud.resource.ServerResource;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.StorageManager;
import com.cloud.storage.StoragePoolVO;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateStoragePoolVO;
import com.cloud.storage.VMTemplateSwiftVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplatePoolDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.swift.SwiftManager;
import com.cloud.utils.DateUtil;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.component.Inject;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.fsm.StateListener;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.Event;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.dao.VMInstanceDao;

@Local(value = CapacityManager.class)
public class CapacityManagerImpl implements CapacityManager, StateListener<State, VirtualMachine.Event, VirtualMachine>, Listener, ResourceListener {
    private static final Logger s_logger = Logger.getLogger(CapacityManagerImpl.class);
    String _name;
    @Inject
    CapacityDao _capacityDao;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    ServiceOfferingDao _offeringsDao;
    @Inject
    HostDao _hostDao;
    @Inject
    VMInstanceDao _vmDao;
    @Inject 
    VolumeDao _volumeDao;
    @Inject 
    VMTemplatePoolDao _templatePoolDao;
    @Inject
    AgentManager _agentManager;
    @Inject
    ResourceManager _resourceMgr;
    @Inject
    StorageManager _storageMgr;
    @Inject
    SwiftManager _swiftMgr;    

    private int _vmCapacityReleaseInterval;
    private ScheduledExecutorService _executor;
    private boolean _stopped;
    long _extraBytesPerVolume = 0;
    private float _storageOverProvisioningFactor = 1.0f;
    private float _cpuOverProvisioningFactor = 1.0f;

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _name = name;
        _vmCapacityReleaseInterval = NumbersUtil.parseInt(_configDao.getValue(Config.CapacitySkipcountingHours.key()), 3600);
        _storageOverProvisioningFactor = NumbersUtil.parseFloat(_configDao.getValue(Config.StorageOverprovisioningFactor.key()), 1.0f);
        _cpuOverProvisioningFactor = NumbersUtil.parseFloat(_configDao.getValue(Config.CPUOverprovisioningFactor.key()), 1.0f);

        if (_cpuOverProvisioningFactor < 1.0f) {
            _cpuOverProvisioningFactor = 1.0f;
        }
        _executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("HostCapacity-Checker"));
        VirtualMachine.State.getStateMachine().registerListener(this);
        _agentManager.registerForHostEvents(new StorageCapacityListener(_capacityDao, _storageOverProvisioningFactor), true, false, false);
        _agentManager.registerForHostEvents(new ComputeCapacityListener(_capacityDao, this, _cpuOverProvisioningFactor), true, false, false);

        return true;
    }

    @Override
    public boolean start() {
    	_resourceMgr.registerResourceEvent(ResourceListener.EVENT_PREPARE_MAINTENANCE_AFTER, this);
    	_resourceMgr.registerResourceEvent(ResourceListener.EVENT_CANCEL_MAINTENANCE_AFTER, this);
        return true;
    }

    @Override
    public boolean stop() {
        _executor.shutdownNow();
        _stopped = true;
        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @DB
    @Override
    public boolean releaseVmCapacity(VirtualMachine vm, boolean moveFromReserved, boolean moveToReservered, Long hostId) {
        ServiceOfferingVO svo = _offeringsDao.findById(vm.getServiceOfferingId());
        CapacityVO capacityCpu = _capacityDao.findByHostIdType(hostId, CapacityVO.CAPACITY_TYPE_CPU);
        CapacityVO capacityMemory = _capacityDao.findByHostIdType(hostId, CapacityVO.CAPACITY_TYPE_MEMORY);

        if (capacityCpu == null || capacityMemory == null || svo == null) {
            return false;
        }

        Transaction txn = Transaction.currentTxn();
        try {
            txn.start();

            int vmCPU = svo.getCpu() * svo.getSpeed();
            long vmMem = svo.getRamSize() * 1024L * 1024L;

            capacityCpu = _capacityDao.lockRow(capacityCpu.getId(), true);
            capacityMemory = _capacityDao.lockRow(capacityMemory.getId(), true);

            long usedCpu = capacityCpu.getUsedCapacity();
            long usedMem = capacityMemory.getUsedCapacity();
            long reservedCpu = capacityCpu.getReservedCapacity();
            long reservedMem = capacityMemory.getReservedCapacity();
            long actualTotalCpu = capacityCpu.getTotalCapacity();
            String opFactor = _configDao.getValue(Config.CPUOverprovisioningFactor.key());
            float cpuOverprovisioningFactor = NumbersUtil.parseFloat(opFactor, 1);
            long totalCpu = (long) (actualTotalCpu * cpuOverprovisioningFactor);
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Hosts's actual total CPU: " + actualTotalCpu + " and CPU after applying overprovisioning: " + totalCpu);
            }
            long totalMem = capacityMemory.getTotalCapacity();

            if (!moveFromReserved) {
                /* move resource from used */
                if (usedCpu >= vmCPU) {
                    capacityCpu.setUsedCapacity(usedCpu - vmCPU);
                }
                if (usedMem >= vmMem) {
                    capacityMemory.setUsedCapacity(usedMem - vmMem);
                }

                if (moveToReservered) {
                    if (reservedCpu + vmCPU <= totalCpu) {
                        capacityCpu.setReservedCapacity(reservedCpu + vmCPU);
                    }
                    if (reservedMem + vmMem <= totalMem) {
                        capacityMemory.setReservedCapacity(reservedMem + vmMem);
                    }
                }
            } else {
                if (reservedCpu >= vmCPU) {
                    capacityCpu.setReservedCapacity(reservedCpu - vmCPU);
                }
                if (reservedMem >= vmMem) {
                    capacityMemory.setReservedCapacity(reservedMem - vmMem);
                }
            }

            s_logger.debug("release cpu from host: " + hostId + ", old used: " + usedCpu + ",reserved: " + reservedCpu + ", actual total: "
                    + actualTotalCpu + ", total with overprovisioning: " + totalCpu + "; new used: " + capacityCpu.getUsedCapacity() + ",reserved:"
                    + capacityCpu.getReservedCapacity() + "; movedfromreserved: " + moveFromReserved + ",moveToReservered" + moveToReservered);

            s_logger.debug("release mem from host: " + hostId + ", old used: " + usedMem + ",reserved: " + reservedMem + ", total: " + totalMem
                    + "; new used: " + capacityMemory.getUsedCapacity() + ",reserved:" + capacityMemory.getReservedCapacity()
                    + "; movedfromreserved: " + moveFromReserved + ",moveToReservered" + moveToReservered);

            _capacityDao.update(capacityCpu.getId(), capacityCpu);
            _capacityDao.update(capacityMemory.getId(), capacityMemory);
            txn.commit();
            return true;
        } catch (Exception e) {
            s_logger.debug("Failed to transit vm's state, due to " + e.getMessage());
            txn.rollback();
            return false;
        }
    }

    @DB
    @Override
    public void allocateVmCapacity(VirtualMachine vm, boolean fromLastHost) {

        long hostId = vm.getHostId();

        ServiceOfferingVO svo = _offeringsDao.findById(vm.getServiceOfferingId());

        CapacityVO capacityCpu = _capacityDao.findByHostIdType(hostId, CapacityVO.CAPACITY_TYPE_CPU);
        CapacityVO capacityMem = _capacityDao.findByHostIdType(hostId, CapacityVO.CAPACITY_TYPE_MEMORY);

        if (capacityCpu == null || capacityMem == null || svo == null) {
            return;
        }

        int cpu = svo.getCpu() * svo.getSpeed();
        long ram = svo.getRamSize() * 1024L * 1024L;

        String opFactor = _configDao.getValue(Config.CPUOverprovisioningFactor.key());
        float cpuOverprovisioningFactor = NumbersUtil.parseFloat(opFactor, 1);

        Transaction txn = Transaction.currentTxn();

        try {
            txn.start();
            capacityCpu = _capacityDao.lockRow(capacityCpu.getId(), true);
            capacityMem = _capacityDao.lockRow(capacityMem.getId(), true);

            long usedCpu = capacityCpu.getUsedCapacity();
            long usedMem = capacityMem.getUsedCapacity();
            long reservedCpu = capacityCpu.getReservedCapacity();
            long reservedMem = capacityMem.getReservedCapacity();
            long actualTotalCpu = capacityCpu.getTotalCapacity();
            long totalCpu = (long) (actualTotalCpu * cpuOverprovisioningFactor);
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Hosts's actual total CPU: " + actualTotalCpu + " and CPU after applying overprovisioning: " + totalCpu);
            }
            long totalMem = capacityMem.getTotalCapacity();

            long freeCpu = totalCpu - (reservedCpu + usedCpu);
            long freeMem = totalMem - (reservedMem + usedMem);

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("We are allocating VM, increasing the used capacity of this host:" + hostId);
                s_logger.debug("Current Used CPU: " + usedCpu + " , Free CPU:" + freeCpu + " ,Requested CPU: " + cpu);
                s_logger.debug("Current Used RAM: " + usedMem + " , Free RAM:" + freeMem + " ,Requested RAM: " + ram);
            }
            capacityCpu.setUsedCapacity(usedCpu + cpu);
            capacityMem.setUsedCapacity(usedMem + ram);

            if (fromLastHost) {
                /* alloc from reserved */
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("We are allocating VM to the last host again, so adjusting the reserved capacity if it is not less than required");
                    s_logger.debug("Reserved CPU: " + reservedCpu + " , Requested CPU: " + cpu);
                    s_logger.debug("Reserved RAM: " + reservedMem + " , Requested RAM: " + ram);
                }
                if (reservedCpu >= cpu && reservedMem >= ram) {
                    capacityCpu.setReservedCapacity(reservedCpu - cpu);
                    capacityMem.setReservedCapacity(reservedMem - ram);
                }
            } else {
                /* alloc from free resource */
                if (!((reservedCpu + usedCpu + cpu <= totalCpu) && (reservedMem + usedMem + ram <= totalMem))) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Host doesnt seem to have enough free capacity, but increasing the used capacity anyways, since the VM is already starting on this host ");
                    }
                }
            }

            s_logger.debug("CPU STATS after allocation: for host: " + hostId + ", old used: " + usedCpu + ", old reserved: " + reservedCpu
                    + ", actual total: " + actualTotalCpu + ", total with overprovisioning: " + totalCpu + "; new used:"
                    + capacityCpu.getUsedCapacity() + ", reserved:" + capacityCpu.getReservedCapacity() + "; requested cpu:" + cpu
                    + ",alloc_from_last:" + fromLastHost);

            s_logger.debug("RAM STATS after allocation: for host: " + hostId + ", old used: " + usedMem + ", old reserved: " + reservedMem
                    + ", total: " + totalMem + "; new used: " + capacityMem.getUsedCapacity() + ", reserved: " + capacityMem.getReservedCapacity()
                    + "; requested mem: " + ram + ",alloc_from_last:" + fromLastHost);

            _capacityDao.update(capacityCpu.getId(), capacityCpu);
            _capacityDao.update(capacityMem.getId(), capacityMem);
            txn.commit();
        } catch (Exception e) {
            txn.rollback();
            return;
        }
    }

    @Override
    public boolean checkIfHostHasCapacity(long hostId, Integer cpu, long ram, boolean checkFromReservedCapacity, float cpuOverprovisioningFactor, boolean considerReservedCapacity) {
        boolean hasCapacity = false;

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Checking if host: " + hostId + " has enough capacity for requested CPU: " + cpu + " and requested RAM: " + ram
                    + " , cpuOverprovisioningFactor: " + cpuOverprovisioningFactor);
        }

        CapacityVO capacityCpu = _capacityDao.findByHostIdType(hostId, CapacityVO.CAPACITY_TYPE_CPU);
        CapacityVO capacityMem = _capacityDao.findByHostIdType(hostId, CapacityVO.CAPACITY_TYPE_MEMORY);

        if (capacityCpu == null || capacityMem == null) {
            if (capacityCpu == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Cannot checkIfHostHasCapacity, Capacity entry for CPU not found in Db, for hostId: " + hostId);
                }
            }
            if (capacityMem == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Cannot checkIfHostHasCapacity, Capacity entry for RAM not found in Db, for hostId: " + hostId);
                }
            }

            return false;
        }

        long usedCpu = capacityCpu.getUsedCapacity();
        long usedMem = capacityMem.getUsedCapacity();
        long reservedCpu = capacityCpu.getReservedCapacity();
        long reservedMem = capacityMem.getReservedCapacity();
        long actualTotalCpu = capacityCpu.getTotalCapacity();
        long totalCpu = (long) (actualTotalCpu * cpuOverprovisioningFactor);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Hosts's actual total CPU: " + actualTotalCpu + " and CPU after applying overprovisioning: " + totalCpu);
        }

        long totalMem = capacityMem.getTotalCapacity();

        String failureReason = "";
        if (checkFromReservedCapacity) {
            long freeCpu = reservedCpu;
            long freeMem = reservedMem;

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("We need to allocate to the last host again, so checking if there is enough reserved capacity");
                s_logger.debug("Reserved CPU: " + freeCpu + " , Requested CPU: " + cpu);
                s_logger.debug("Reserved RAM: " + freeMem + " , Requested RAM: " + ram);
            }
            /* alloc from reserved */
            if (reservedCpu >= cpu) {
                if (reservedMem >= ram) {
                    hasCapacity = true;
                } else {
                    failureReason = "Host does not have enough reserved RAM available";
                }
            } else {
                failureReason = "Host does not have enough reserved CPU available";
            }
        } else {
            
            long reservedCpuValueToUse = reservedCpu;
            long reservedMemValueToUse = reservedMem;
            
            if(!considerReservedCapacity){
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("considerReservedCapacity is" + considerReservedCapacity + " , not considering reserved capacity for calculating free capacity");
                }
                reservedCpuValueToUse = 0;
                reservedMemValueToUse = 0;
            }
            long freeCpu = totalCpu - (reservedCpuValueToUse + usedCpu);
            long freeMem = totalMem - (reservedMemValueToUse + usedMem);

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Free CPU: " + freeCpu + " , Requested CPU: " + cpu);
                s_logger.debug("Free RAM: " + freeMem + " , Requested RAM: " + ram);
            }
            /* alloc from free resource */
            if ((reservedCpuValueToUse + usedCpu + cpu <= totalCpu)) {
                if ((reservedMemValueToUse + usedMem + ram <= totalMem)) {
                    hasCapacity = true;
                } else {
                    failureReason = "Host does not have enough RAM available";
                }
            } else {
                failureReason = "Host does not have enough CPU available";
            }
        }

        if (hasCapacity) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Host has enough CPU and RAM available");
            }

            s_logger.debug("STATS: Can alloc CPU from host: " + hostId + ", used: " + usedCpu + ", reserved: " + reservedCpu + ", actual total: "
                    + actualTotalCpu + ", total with overprovisioning: " + totalCpu + "; requested cpu:" + cpu + ",alloc_from_last_host?:"
                    + checkFromReservedCapacity + " ,considerReservedCapacity?: "+considerReservedCapacity);

            s_logger.debug("STATS: Can alloc MEM from host: " + hostId + ", used: " + usedMem + ", reserved: " + reservedMem + ", total: " + totalMem
                    + "; requested mem: " + ram + ",alloc_from_last_host?:" + checkFromReservedCapacity+ " ,considerReservedCapacity?: "+considerReservedCapacity);
        } else {

            if (checkFromReservedCapacity) {
                s_logger.debug("STATS: Failed to alloc resource from host: " + hostId + " reservedCpu: " + reservedCpu + ", requested cpu: " + cpu
                        + ", reservedMem: " + reservedMem + ", requested mem: " + ram);
            } else {
                s_logger.debug("STATS: Failed to alloc resource from host: " + hostId + " reservedCpu: " + reservedCpu + ", used cpu: " + usedCpu
                        + ", requested cpu: " + cpu + ", actual total cpu: " + actualTotalCpu + ", total cpu with overprovisioning: " + totalCpu
                        + ", reservedMem: " + reservedMem + ", used Mem: " + usedMem + ", requested mem: " + ram + ", total Mem:" + totalMem+ " ,considerReservedCapacity?: "+considerReservedCapacity);
            }

            if (s_logger.isDebugEnabled()) {
                s_logger.debug(failureReason + ", cannot allocate to this host.");
            }
        }

        return hasCapacity;

    }
    
    @Override
    public long getAllocatedPoolCapacity(StoragePoolVO pool, VMTemplateVO templateForVmCreation){
      
        // Get size for all the volumes
        Pair<Long, Long> sizes = _volumeDao.getCountAndTotalByPool(pool.getId());
        long totalAllocatedSize = sizes.second() + sizes.first() * _extraBytesPerVolume;

        // Iterate through all templates on this storage pool
        boolean tmpinstalled = false;
        List<VMTemplateStoragePoolVO> templatePoolVOs;
        templatePoolVOs = _templatePoolDao.listByPoolId(pool.getId());
        
        for (VMTemplateStoragePoolVO templatePoolVO : templatePoolVOs) {            
            if ((templateForVmCreation != null) && !tmpinstalled && (templatePoolVO.getTemplateId() == templateForVmCreation.getId())) {
                tmpinstalled = true;
            }
            long templateSize = templatePoolVO.getTemplateSize();
            totalAllocatedSize += templateSize + _extraBytesPerVolume;
        }
        
        // Add the size for the templateForVmCreation if its not already present
        if ((templateForVmCreation != null) && !tmpinstalled) {
            // If the template that was passed into this allocator is not installed in the storage pool,
            // add 3 * (template size on secondary storage) to the running total
            VMTemplateHostVO templateHostVO = _storageMgr.findVmTemplateHost(templateForVmCreation.getId(), pool);

            if (templateHostVO == null) {
                VMTemplateSwiftVO templateSwiftVO = _swiftMgr.findByTmpltId(templateForVmCreation.getId());
                if (templateSwiftVO != null) {                                    
                    long templateSize = templateSwiftVO.getPhysicalSize();
                    if (templateSize == 0) {
                        templateSize = templateSwiftVO.getSize();
                    }
                    totalAllocatedSize += (templateSize + _extraBytesPerVolume);
                }
            } else {
                long templateSize = templateHostVO.getPhysicalSize();
                if ( templateSize == 0 ){
                    templateSize = templateHostVO.getSize();
                }
                totalAllocatedSize +=  (templateSize + _extraBytesPerVolume);
            }
        }
        
        return totalAllocatedSize;
    }
    
    
    @DB
    @Override
	public void updateCapacityForHost(HostVO host){
    	// prep the service offerings
        List<ServiceOfferingVO> offerings = _offeringsDao.listAllIncludingRemoved();
        Map<Long, ServiceOfferingVO> offeringsMap = new HashMap<Long, ServiceOfferingVO>();
        for (ServiceOfferingVO offering : offerings) {
            offeringsMap.put(offering.getId(), offering);
        }
        
        long usedCpu = 0;
        long usedMemory = 0;
        long reservedMemory = 0;
        long reservedCpu = 0;

        List<VMInstanceVO> vms = _vmDao.listUpByHostId(host.getId());
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Found " + vms.size() + " VMs on host " + host.getId());
        }

        for (VMInstanceVO vm : vms) {
            ServiceOffering so = offeringsMap.get(vm.getServiceOfferingId());
            usedMemory += so.getRamSize() * 1024L * 1024L;
            usedCpu += so.getCpu() * so.getSpeed();
        }

        List<VMInstanceVO> vmsByLastHostId = _vmDao.listByLastHostId(host.getId());
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Found " + vmsByLastHostId.size() + " VM, not running on host " + host.getId());
        }
        for (VMInstanceVO vm : vmsByLastHostId) {
            long secondsSinceLastUpdate = (DateUtil.currentGMTTime().getTime() - vm.getUpdateTime().getTime()) / 1000;
            if (secondsSinceLastUpdate < _vmCapacityReleaseInterval) {
                ServiceOffering so = offeringsMap.get(vm.getServiceOfferingId());
                reservedMemory += so.getRamSize() * 1024L * 1024L;
                reservedCpu += so.getCpu() * so.getSpeed();
            }
        }

        CapacityVO cpuCap = _capacityDao.findByHostIdType(host.getId(), CapacityVO.CAPACITY_TYPE_CPU);
        CapacityVO memCap = _capacityDao.findByHostIdType(host.getId(), CapacityVO.CAPACITY_TYPE_MEMORY);

        if (cpuCap != null && memCap != null){
        	if (cpuCap.getUsedCapacity() == usedCpu && cpuCap.getReservedCapacity() == reservedCpu) {
        		s_logger.debug("No need to calibrate cpu capacity, host:" + host.getId() + " usedCpu: " + cpuCap.getUsedCapacity()
        				+ " reservedCpu: " + cpuCap.getReservedCapacity());
        	} else if (cpuCap.getReservedCapacity() != reservedCpu) {
        		s_logger.debug("Calibrate reserved cpu for host: " + host.getId() + " old reservedCpu:" + cpuCap.getReservedCapacity()
        				+ " new reservedCpu:" + reservedCpu);
        		cpuCap.setReservedCapacity(reservedCpu);
        	} else if (cpuCap.getUsedCapacity() != usedCpu) {
        		s_logger.debug("Calibrate used cpu for host: " + host.getId() + " old usedCpu:" + cpuCap.getUsedCapacity() + " new usedCpu:"
        				+ usedCpu);
        		cpuCap.setUsedCapacity(usedCpu);
        	}
	
	        if (memCap.getUsedCapacity() == usedMemory && memCap.getReservedCapacity() == reservedMemory) {
	            s_logger.debug("No need to calibrate memory capacity, host:" + host.getId() + " usedMem: " + memCap.getUsedCapacity()
	                    + " reservedMem: " + memCap.getReservedCapacity());
	        } else if (memCap.getReservedCapacity() != reservedMemory) {
	            s_logger.debug("Calibrate reserved memory for host: " + host.getId() + " old reservedMem:" + memCap.getReservedCapacity()
	                    + " new reservedMem:" + reservedMemory);
	            memCap.setReservedCapacity(reservedMemory);
	        } else if (memCap.getUsedCapacity() != usedMemory) {
	            /*
	             * Didn't calibrate for used memory, because VMs can be in state(starting/migrating) that I don't know on which host they are
	             * allocated
	             */
	            s_logger.debug("Calibrate used memory for host: " + host.getId() + " old usedMem: " + memCap.getUsedCapacity()
	                    + " new usedMem: " + usedMemory);
	            memCap.setUsedCapacity(usedMemory);
	        }
	
	        try {
	            _capacityDao.update(cpuCap.getId(), cpuCap);
	            _capacityDao.update(memCap.getId(), memCap);
	        } catch (Exception e) {
	        	s_logger.error("Caught exception while updating cpu/memory capacity for the host " +host.getId(), e);
	        }
        }else {
        	Transaction txn = Transaction.currentTxn();
        	txn.start();
        	CapacityVO capacity = new CapacityVO(host.getId(),
                    host.getDataCenterId(), host.getPodId(), host.getClusterId(), usedMemory,
                    host.getTotalMemory(),
                    CapacityVO.CAPACITY_TYPE_MEMORY);
            capacity.setReservedCapacity(reservedMemory);
            _capacityDao.persist(capacity);
        	
            capacity = new CapacityVO(
                    host.getId(),
                    host.getDataCenterId(),
                    host.getPodId(), 
                    host.getClusterId(),
                    usedCpu,
                    (long)(host.getCpus().longValue() * host.getSpeed().longValue()),
                    CapacityVO.CAPACITY_TYPE_CPU);
            capacity.setReservedCapacity(reservedCpu);
            _capacityDao.persist(capacity);
            txn.commit();
            
        }
        
    }
    
    @Override
    public boolean preStateTransitionEvent(State oldState, Event event, State newState, VirtualMachine vm, boolean transitionStatus, Object opaque) {
        return true;
    }

    @Override
    public boolean postStateTransitionEvent(State oldState, Event event, State newState, VirtualMachine vm, boolean status, Object opaque) {
        if (!status) {
            return false;
        }
        @SuppressWarnings("unchecked")
		Pair<Long, Long> hosts = (Pair<Long, Long>)opaque;
        Long oldHostId = hosts.first();

        s_logger.debug("VM state transitted from :" + oldState + " to " + newState + " with event: " + event + "vm's original host id: "
                + vm.getLastHostId() + " new host id: " + vm.getHostId() + " host id before state transition: " + oldHostId);

        if (oldState == State.Starting) {
            if(newState != State.Running) {
            	releaseVmCapacity(vm, false, false, oldHostId);
            }
        } else if (oldState == State.Running) {
            if (event == Event.AgentReportStopped) {
                releaseVmCapacity(vm, false, true, oldHostId);
            } else if(event == Event.AgentReportMigrated) {
                releaseVmCapacity(vm, false, false, oldHostId);
            }
        } else if (oldState == State.Migrating) {
            if (event == Event.AgentReportStopped) {
                /* Release capacity from original host */
                releaseVmCapacity(vm, false, false, vm.getLastHostId());
                releaseVmCapacity(vm, false, false, oldHostId);
            } else if (event == Event.OperationFailed) {
                /* Release from dest host */
                releaseVmCapacity(vm, false, false, oldHostId);
            } else if (event == Event.OperationSucceeded) {
                releaseVmCapacity(vm, false, false, vm.getLastHostId());
            }
        } else if (oldState == State.Stopping) {
            if (event == Event.OperationSucceeded) {
                releaseVmCapacity(vm, false, true, oldHostId);
            } else if (event == Event.AgentReportStopped) {
                releaseVmCapacity(vm, false, false, oldHostId);
            } else if(event == Event.AgentReportMigrated) {
                releaseVmCapacity(vm, false, false, oldHostId);
            }
        } else if (oldState == State.Stopped) {
            if (event == Event.DestroyRequested || event == Event.ExpungeOperation) {
                releaseVmCapacity(vm, true, false, vm.getLastHostId());
            } else if(event == Event.AgentReportMigrated) {
                releaseVmCapacity(vm, false, false, oldHostId);
            }
        }
        
        if ((newState == State.Starting || newState == State.Migrating || event == Event.AgentReportMigrated) && vm.getHostId() != null) {
            boolean fromLastHost = false;
            if (vm.getLastHostId() == vm.getHostId()) {
                s_logger.debug("VM starting again on the last host it was stopped on");
                fromLastHost = true;
            }
            allocateVmCapacity(vm, fromLastHost);
        }

        return true;
    }

    // TODO: Get rid of this case once we've determined that the capacity listeners above have all the changes
    // create capacity entries if none exist for this server
    private void createCapacityEntry(StartupCommand startup, HostVO server) {
        SearchCriteria<CapacityVO> capacitySC = _capacityDao.createSearchCriteria();
        capacitySC.addAnd("hostOrPoolId", SearchCriteria.Op.EQ, server.getId());
        capacitySC.addAnd("dataCenterId", SearchCriteria.Op.EQ, server.getDataCenterId());
        capacitySC.addAnd("podId", SearchCriteria.Op.EQ, server.getPodId());

        if (startup instanceof StartupRoutingCommand) {
            SearchCriteria<CapacityVO> capacityCPU = _capacityDao.createSearchCriteria();
            capacityCPU.addAnd("hostOrPoolId", SearchCriteria.Op.EQ, server.getId());
            capacityCPU.addAnd("dataCenterId", SearchCriteria.Op.EQ, server.getDataCenterId());
            capacityCPU.addAnd("podId", SearchCriteria.Op.EQ, server.getPodId());
            capacityCPU.addAnd("capacityType", SearchCriteria.Op.EQ, CapacityVO.CAPACITY_TYPE_CPU);
            List<CapacityVO> capacityVOCpus = _capacityDao.search(capacitySC, null);

            if (capacityVOCpus != null && !capacityVOCpus.isEmpty()) {
                CapacityVO CapacityVOCpu = capacityVOCpus.get(0);
                long newTotalCpu = (long) (server.getCpus().longValue() * server.getSpeed().longValue() * _cpuOverProvisioningFactor);
                if ((CapacityVOCpu.getTotalCapacity() <= newTotalCpu)
                        || ((CapacityVOCpu.getUsedCapacity() + CapacityVOCpu.getReservedCapacity()) <= newTotalCpu)) {
                    CapacityVOCpu.setTotalCapacity(newTotalCpu);
                } else if ((CapacityVOCpu.getUsedCapacity() + CapacityVOCpu.getReservedCapacity() > newTotalCpu)
                        && (CapacityVOCpu.getUsedCapacity() < newTotalCpu)) {
                    CapacityVOCpu.setReservedCapacity(0);
                    CapacityVOCpu.setTotalCapacity(newTotalCpu);
                } else {
                    s_logger.debug("What? new cpu is :" + newTotalCpu + ", old one is " + CapacityVOCpu.getUsedCapacity() + ","
                            + CapacityVOCpu.getReservedCapacity() + "," + CapacityVOCpu.getTotalCapacity());
                }
                _capacityDao.update(CapacityVOCpu.getId(), CapacityVOCpu);
            } else {
                CapacityVO capacity = new CapacityVO(server.getId(), server.getDataCenterId(), server.getPodId(), server.getClusterId(), 0L,
                        (long) (server.getCpus().longValue() * server.getSpeed().longValue() * _cpuOverProvisioningFactor),
                        CapacityVO.CAPACITY_TYPE_CPU);
                _capacityDao.persist(capacity);
            }

            SearchCriteria<CapacityVO> capacityMem = _capacityDao.createSearchCriteria();
            capacityMem.addAnd("hostOrPoolId", SearchCriteria.Op.EQ, server.getId());
            capacityMem.addAnd("dataCenterId", SearchCriteria.Op.EQ, server.getDataCenterId());
            capacityMem.addAnd("podId", SearchCriteria.Op.EQ, server.getPodId());
            capacityMem.addAnd("capacityType", SearchCriteria.Op.EQ, CapacityVO.CAPACITY_TYPE_MEMORY);
            List<CapacityVO> capacityVOMems = _capacityDao.search(capacityMem, null);

            if (capacityVOMems != null && !capacityVOMems.isEmpty()) {
                CapacityVO CapacityVOMem = capacityVOMems.get(0);
                long newTotalMem = server.getTotalMemory();
                if (CapacityVOMem.getTotalCapacity() <= newTotalMem
                        || (CapacityVOMem.getUsedCapacity() + CapacityVOMem.getReservedCapacity() <= newTotalMem)) {
                    CapacityVOMem.setTotalCapacity(newTotalMem);
                } else if (CapacityVOMem.getUsedCapacity() + CapacityVOMem.getReservedCapacity() > newTotalMem
                        && CapacityVOMem.getUsedCapacity() < newTotalMem) {
                    CapacityVOMem.setReservedCapacity(0);
                    CapacityVOMem.setTotalCapacity(newTotalMem);
                } else {
                    s_logger.debug("What? new cpu is :" + newTotalMem + ", old one is " + CapacityVOMem.getUsedCapacity() + ","
                            + CapacityVOMem.getReservedCapacity() + "," + CapacityVOMem.getTotalCapacity());
                }
                _capacityDao.update(CapacityVOMem.getId(), CapacityVOMem);
            } else {
                CapacityVO capacity = new CapacityVO(server.getId(), server.getDataCenterId(), server.getPodId(), server.getClusterId(), 0L,
                        server.getTotalMemory(), CapacityVO.CAPACITY_TYPE_MEMORY);
                _capacityDao.persist(capacity);
            }
        }

    }

    @Override
    public boolean processAnswers(long agentId, long seq, Answer[] answers) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean processCommands(long agentId, long seq, Command[] commands) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(long agentId, AgentControlCommand cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void processConnect(HostVO host, StartupCommand cmd, boolean forRebalance) throws ConnectionException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean processDisconnect(long agentId, Status state) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRecurring() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean processTimeout(long agentId, long seq) {
        // TODO Auto-generated method stub
        return false;
    }

	@Override
	public void processCancelMaintenaceEventAfter(Long hostId) {
		updateCapacityForHost(_hostDao.findById(hostId));		
	}

	@Override
	public void processCancelMaintenaceEventBefore(Long hostId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processDeletHostEventAfter(HostVO host) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processDeleteHostEventBefore(HostVO host) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processDiscoverEventAfter(
			Map<? extends ServerResource, Map<String, String>> resources) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processDiscoverEventBefore(Long dcid, Long podId,
			Long clusterId, URI uri, String username, String password,
			List<String> hostTags) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processPrepareMaintenaceEventAfter(Long hostId) {		
		_capacityDao.removeBy(Capacity.CAPACITY_TYPE_MEMORY, null, null, null, hostId);
		_capacityDao.removeBy(Capacity.CAPACITY_TYPE_CPU, null, null, null, hostId);
	}

	@Override
	public void processPrepareMaintenaceEventBefore(Long hostId) {
		// TODO Auto-generated method stub
		
	}

}
