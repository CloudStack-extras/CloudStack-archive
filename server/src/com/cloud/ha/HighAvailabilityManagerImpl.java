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
package com.cloud.ha;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import com.cloud.agent.AgentManager;
import com.cloud.alert.AlertManager;
import com.cloud.cluster.ClusterManagerListener;
import com.cloud.cluster.ManagementServerHostVO;
import com.cloud.cluster.StackMaid;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.VirtualMachineMigrationException;
import com.cloud.ha.dao.HighAvailabilityDao;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.server.ManagementServer;
import com.cloud.storage.StorageManager;
import com.cloud.storage.dao.GuestOSCategoryDao;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.user.AccountManager;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.Adapters;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.VMInstanceDao;

/**
 * HighAvailabilityManagerImpl coordinates the HA process. VMs are registered with the HA Manager for HA. The request is stored
 * within a database backed work queue. HAManager has a number of workers that pick up these work items to perform HA on the
 * VMs.
 * 
 * The HA process goes as follows: 1. Check with the list of Investigators to determine that the VM is no longer running. If a
 * Investigator finds the VM is still alive, the HA process is stopped and the state of the VM reverts back to its previous
 * state. If a Investigator finds the VM is dead, then HA process is started on the VM, skipping step 2. 2. If the list of
 * Investigators can not determine if the VM is dead or alive. The list of FenceBuilders is invoked to fence off the VM so that
 * it won't do any damage to the storage and network. 3. The VM is marked as stopped. 4. The VM is started again via the normal
 * process of starting VMs. Note that once the VM is marked as stopped, the user may have started the VM himself. 5. VMs that
 * have re-started more than the configured number of times are marked as in Error state and the user is not allowed to restart
 * the VM.
 * 
 * @config {@table || Param Name | Description | Values | Default || || workers | number of worker threads to spin off to do the
 *         processing | int | 1 || || time.to.sleep | Time to sleep if no work items are found | seconds | 60 || || max.retries
 *         | number of times to retry start | int | 5 || || time.between.failure | Time elapsed between failures before we
 *         consider it as another retry | seconds | 3600 || || time.between.cleanup | Time to wait before the cleanup thread
 *         runs | seconds | 86400 || || force.ha | Force HA to happen even if the VM says no | boolean | false || ||
 *         ha.retry.wait | time to wait before retrying the work item | seconds | 120 || || stop.retry.wait | time to wait
 *         before retrying the stop | seconds | 120 || * }
 **/
@Local(value = { HighAvailabilityManager.class })
public class HighAvailabilityManagerImpl implements HighAvailabilityManager, ClusterManagerListener {
    protected static final Logger s_logger = Logger.getLogger(HighAvailabilityManagerImpl.class);
    String _name;
    WorkerThread[] _workers;
    boolean _stopped;
    long _timeToSleep;
    @Inject
    HighAvailabilityDao _haDao;
    @Inject
    VMInstanceDao _instanceDao;
    @Inject
    HostDao _hostDao;
    @Inject
    DataCenterDao _dcDao;
    @Inject
    HostPodDao _podDao;
    @Inject
    ClusterDetailsDao _clusterDetailsDao;
    long _serverId;
    @Inject(adapter = Investigator.class)
    Adapters<Investigator> _investigators;
    @Inject(adapter = FenceBuilder.class)
    Adapters<FenceBuilder> _fenceBuilders;
    @Inject
    AgentManager _agentMgr;
    @Inject
    AlertManager _alertMgr;
    @Inject
    StorageManager _storageMgr;
    @Inject
    GuestOSDao _guestOSDao;
    @Inject
    GuestOSCategoryDao _guestOSCategoryDao;
    @Inject
    VirtualMachineManager _itMgr;
    @Inject
    AccountManager _accountMgr;

    String _instance;
    ScheduledExecutorService _executor;
    int _stopRetryInterval;
    int _investigateRetryInterval;
    int _migrateRetryInterval;
    int _restartRetryInterval;

    int _maxRetries;
    long _timeBetweenFailures;
    long _timeBetweenCleanups;
    boolean _forceHA;

    protected HighAvailabilityManagerImpl() {
    }

    @Override
    public Status investigate(final long hostId) {
        final HostVO host = _hostDao.findById(hostId);
        if (host == null) {
            return null;
        }

        final Enumeration<Investigator> en = _investigators.enumeration();
        Status hostState = null;
        Investigator investigator = null;
        while (en.hasMoreElements()) {
            investigator = en.nextElement();
            hostState = investigator.isAgentAlive(host);
            if (hostState != null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug(investigator.getName() + " was able to determine host " + hostId + " is in " + hostState.toString());
                }
                return hostState;
            }
            if (s_logger.isDebugEnabled()) {
                s_logger.debug(investigator.getName() + " unable to determine the state of the host.  Moving on.");
            }
        }

        return null;
    }

    @Override
    public void scheduleRestartForVmsOnHost(final HostVO host, boolean investigate) {

        if (host.getType() != Host.Type.Routing) {
            return;
        }
        
        if(host.getHypervisorType() == HypervisorType.VMware) {
        	// still need to disable HA attempts when host is down because in VMware, any HA attempt that tries
        	// to operate on VMs previously running at this host will fail
        	s_logger.info("Skip host-level HA for host " + host.getId() + " as it is a VMware host");
        	return;
        }
        
        s_logger.warn("Scheduling restart for VMs on host " + host.getId());

        final List<VMInstanceVO> vms = _instanceDao.listByHostId(host.getId());
        final DataCenterVO dcVO = _dcDao.findById(host.getDataCenterId());

        // send an email alert that the host is down
        StringBuilder sb = null;
        if ((vms != null) && !vms.isEmpty()) {
            sb = new StringBuilder();
            sb.append("  Starting HA on the following VMs: ");
            // collect list of vm names for the alert email
            VMInstanceVO vm = vms.get(0);
            if (vm.isHaEnabled()) {
                sb.append(" " + vm);
            }
            for (int i = 1; i < vms.size(); i++) {
                vm = vms.get(i);
                if (vm.isHaEnabled()) {
                    sb.append(" " + vm.getHostName());
                }
            }
        }

        // send an email alert that the host is down, include VMs
        HostPodVO podVO = _podDao.findById(host.getPodId());
        String hostDesc = "name: " + host.getName() + " (id:" + host.getId() + "), availability zone: " + dcVO.getName() + ", pod: " + podVO.getName();

        _alertMgr.sendAlert(AlertManager.ALERT_TYPE_HOST, host.getDataCenterId(), host.getPodId(), "Host is down, " + hostDesc, "Host [" + hostDesc + "] is down."
                + ((sb != null) ? sb.toString() : ""));

        for (final VMInstanceVO vm : vms) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Notifying HA Mgr of to restart vm " + vm.getId() + "-" + vm.getHostName());
            }
            scheduleRestart(vm, investigate);
        }
    }

    @Override
    public void scheduleStop(VMInstanceVO vm, long hostId, WorkType type) {
        assert (type == WorkType.CheckStop || type == WorkType.ForceStop || type == WorkType.Stop);

        if (_haDao.hasBeenScheduled(vm.getId(), type)) {
            s_logger.info("There's already a job scheduled to stop " + vm);
            return;
        }

        HaWorkVO work = new HaWorkVO(vm.getId(), vm.getType(), type, Step.Scheduled, hostId, vm.getState(), 0, vm.getUpdated());
        _haDao.persist(work);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Scheduled " + work);
        }
        wakeupWorkers();
    }

    protected void wakeupWorkers() {
        for (WorkerThread worker : _workers) {
            worker.wakup();
        }
    }

    @Override
    public boolean scheduleMigration(final VMInstanceVO vm) {
        if (vm.getHostId() != null) {
            final HaWorkVO work = new HaWorkVO(vm.getId(), vm.getType(), WorkType.Migration, Step.Scheduled, vm.getHostId(), vm.getState(), 0, vm.getUpdated());
            _haDao.persist(work);
            wakeupWorkers();
        }
        return true;
    }

    @Override
    public void scheduleRestart(VMInstanceVO vm, boolean investigate) {
    	Long hostId = vm.getHostId();
    	if (hostId == null) {
    	    try {
    	        s_logger.debug("Found a vm that is scheduled to be restarted but has no host id: " + vm);
                _itMgr.advanceStop(vm, true, _accountMgr.getSystemUser(), _accountMgr.getSystemAccount());
            } catch (ResourceUnavailableException e) {
                assert false : "How do we hit this when force is true?";
                throw new CloudRuntimeException("Caught exception even though it should be handled.", e);
            } catch (OperationTimedoutException e) {
                assert false : "How do we hit this when force is true?";
                throw new CloudRuntimeException("Caught exception even though it should be handled.", e);
            } catch (ConcurrentOperationException e) {
                assert false : "How do we hit this when force is true?";
                throw new CloudRuntimeException("Caught exception even though it should be handled.", e);
            }            
    	    return;
    	}
    	
        if (!investigate) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("VM does not require investigation so I'm marking it as Stopped: " + vm.toString());
            }

            short alertType = AlertManager.ALERT_TYPE_USERVM;
            if (VirtualMachine.Type.DomainRouter.equals(vm.getType())) {
                alertType = AlertManager.ALERT_TYPE_DOMAIN_ROUTER;
            } else if (VirtualMachine.Type.ConsoleProxy.equals(vm.getType())) {
                alertType = AlertManager.ALERT_TYPE_CONSOLE_PROXY;
            }

            if (!(_forceHA || vm.isHaEnabled())) {
                String hostDesc = "id:" + vm.getHostId() + ", availability zone id:" + vm.getDataCenterIdToDeployIn() + ", pod id:" + vm.getPodIdToDeployIn();
                _alertMgr.sendAlert(alertType, vm.getDataCenterIdToDeployIn(), vm.getPodIdToDeployIn(), "VM (name: " + vm.getHostName() + ", id: " + vm.getId() + ") stopped unexpectedly on host " + hostDesc,
                        "Virtual Machine " + vm.getHostName() + " (id: " + vm.getId() + ") running on host [" + vm.getHostId() + "] stopped unexpectedly.");

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("VM is not HA enabled so we're done.");
                }
            }

            try {
                _itMgr.advanceStop(vm, true, _accountMgr.getSystemUser(), _accountMgr.getSystemAccount());
            } catch (ResourceUnavailableException e) {
                assert false : "How do we hit this when force is true?";
                throw new CloudRuntimeException("Caught exception even though it should be handled.", e);
            } catch (OperationTimedoutException e) {
                assert false : "How do we hit this when force is true?";
                throw new CloudRuntimeException("Caught exception even though it should be handled.", e);
            } catch (ConcurrentOperationException e) {
                assert false : "How do we hit this when force is true?";
                throw new CloudRuntimeException("Caught exception even though it should be handled.", e);
            }        
        }

        List<HaWorkVO> items = _haDao.findPreviousHA(vm.getId());
        int maxRetries = 0;
        for (HaWorkVO item : items) {
            if (maxRetries < item.getTimesTried() && !item.canScheduleNew(_timeBetweenFailures)) {
                maxRetries = item.getTimesTried();
                break;
            }
        }

        HaWorkVO work = new HaWorkVO(vm.getId(), vm.getType(), WorkType.HA, investigate ? Step.Investigating : Step.Scheduled, hostId, vm.getState(), maxRetries + 1, vm.getUpdated());
        _haDao.persist(work);

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Schedule vm for HA:  " + vm);
        }

        wakeupWorkers();

    }

    protected Long restart(HaWorkVO work) {
        List<HaWorkVO> items = _haDao.listFutureHaWorkForVm(work.getInstanceId(), work.getId());
        if (items.size() > 0) {
            StringBuilder str = new StringBuilder("Cancelling this work item because newer ones have been scheduled.  Work Ids = [");
            for (HaWorkVO item : items) {
                str.append(item.getId()).append(", ");
            }
            str.delete(str.length() - 2, str.length()).append("]");
            s_logger.info(str.toString());
            return null;
        }
        
        items = _haDao.listRunningHaWorkForVm(work.getInstanceId());
        if (items.size() > 0) {
            StringBuilder str = new StringBuilder("Waiting because there's HA work being executed on an item currently.  Work Ids =[");
            for (HaWorkVO item : items) {
                str.append(item.getId()).append(", ");
            }
            str.delete(str.length() - 2, str.length()).append("]");
            s_logger.info(str.toString());
            return (System.currentTimeMillis() >> 10) + _investigateRetryInterval;
        }
        
        long vmId = work.getInstanceId();

        VMInstanceVO vm = _itMgr.findById(work.getType(), work.getInstanceId());
        if (vm == null) {
            s_logger.info("Unable to find vm: " + vmId);
            return null;
        }

        s_logger.info("HA on " + vm);
        if (vm.getState() != work.getPreviousState() || vm.getUpdated() != work.getUpdateTime()) {
            s_logger.info("VM " + vm + " has been changed.  Current State = " + vm.getState() + " Previous State = " + work.getPreviousState() + " last updated = " + vm.getUpdated()
                    + " previous updated = " + work.getUpdateTime());
            return null;
        }

        short alertType = AlertManager.ALERT_TYPE_USERVM;
        if (VirtualMachine.Type.DomainRouter.equals(vm.getType())) {
            alertType = AlertManager.ALERT_TYPE_DOMAIN_ROUTER;
        } else if (VirtualMachine.Type.ConsoleProxy.equals(vm.getType())) {
            alertType = AlertManager.ALERT_TYPE_CONSOLE_PROXY;
        }

        HostVO host = _hostDao.findById(work.getHostId());
        boolean isHostRemoved = false;
        if (host == null) {
            host = _hostDao.findByIdIncludingRemoved(work.getHostId());
            if (host != null) {
                s_logger.debug("VM " + vm.toString() + " is now no longer on host " + work.getHostId() + " as the host is removed");
                isHostRemoved = true;
            }
        }

        DataCenterVO dcVO = _dcDao.findById(host.getDataCenterId());
        HostPodVO podVO = _podDao.findById(host.getPodId());
        String hostDesc = "name: " + host.getName() + "(id:" + host.getId() + "), availability zone: " + dcVO.getName() + ", pod: " + podVO.getName();

        Boolean alive = null;
        if (work.getStep() == Step.Investigating) {
            if (!isHostRemoved) {
                if (vm.getHostId() == null || vm.getHostId() != work.getHostId()) {
                    s_logger.info("VM " + vm.toString() + " is now no longer on host " + work.getHostId());
                    return null;
                }

                Enumeration<Investigator> en = _investigators.enumeration();
                Investigator investigator = null;
                while (en.hasMoreElements()) {
                    investigator = en.nextElement();
                    alive = investigator.isVmAlive(vm, host);
                    s_logger.info(investigator.getName() + " found " + vm + "to be alive? " + alive);
                    if (alive != null) {
                        break;
                    }
                }
                boolean fenced = false;
                if (alive == null) {
                    s_logger.debug("Fencing off VM that we don't know the state of");
                    Enumeration<FenceBuilder> enfb = _fenceBuilders.enumeration();
                    while (enfb.hasMoreElements()) {
                        FenceBuilder fb = enfb.nextElement();
                        Boolean result = fb.fenceOff(vm, host);
                        s_logger.info("Fencer " + fb.getName() + " returned " + result);
                        if (result != null && result) {
                            fenced = true;
                            break;
                        }
                    }
                } else if (!alive) {
                    fenced = true;
                } else {
                    s_logger.debug("VM " + vm.getHostName() + " is found to be alive by " + investigator.getName());
                    if (host.getStatus() == Status.Up) {
                        s_logger.info(vm + " is alive and host is up. No need to restart it.");
                        return null;
                    } else {
                        s_logger.debug("Rescheduling because the host is not up but the vm is alive");
                        return (System.currentTimeMillis() >> 10) + _investigateRetryInterval;
                    }
                }

                if (!fenced) {
                    s_logger.debug("We were unable to fence off the VM " + vm);
                    _alertMgr.sendAlert(alertType, vm.getDataCenterIdToDeployIn(), vm.getPodIdToDeployIn(), "Unable to restart " + vm.getHostName() + " which was running on host " + hostDesc,
                            "Insufficient capacity to restart VM, name: " + vm.getHostName() + ", id: " + vmId + " which was running on host " + hostDesc);
                    return (System.currentTimeMillis() >> 10) + _restartRetryInterval;
                }

                try {
                    _itMgr.advanceStop(vm, true, _accountMgr.getSystemUser(), _accountMgr.getSystemAccount());
                } catch (ResourceUnavailableException e) {
                    assert false : "How do we hit this when force is true?";
                    throw new CloudRuntimeException("Caught exception even though it should be handled.", e);
                } catch (OperationTimedoutException e) {
                    assert false : "How do we hit this when force is true?";
                    throw new CloudRuntimeException("Caught exception even though it should be handled.", e);
                } catch (ConcurrentOperationException e) {
                    assert false : "How do we hit this when force is true?";
                    throw new CloudRuntimeException("Caught exception even though it should be handled.", e);
                }

                work.setStep(Step.Scheduled);
                _haDao.update(work.getId(), work);
            } else {
                s_logger.debug("How come that HA step is Investigating and the host is removed? Calling forced Stop on Vm anyways");
                try {
                    _itMgr.advanceStop(vm, true, _accountMgr.getSystemUser(), _accountMgr.getSystemAccount());
                } catch (ResourceUnavailableException e) {
                    assert false : "How do we hit this when force is true?";
                    throw new CloudRuntimeException("Caught exception even though it should be handled.", e);
                } catch (OperationTimedoutException e) {
                    assert false : "How do we hit this when force is true?";
                    throw new CloudRuntimeException("Caught exception even though it should be handled.", e);
                } catch (ConcurrentOperationException e) {
                    assert false : "How do we hit this when force is true?";
                    throw new CloudRuntimeException("Caught exception even though it should be handled.", e);
                }
            }
        }

        vm = _itMgr.findById(vm.getType(), vm.getId());

        if (!_forceHA && !vm.isHaEnabled()) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("VM is not HA enabled so we're done.");
            }
            return null; // VM doesn't require HA
        }

        if (!_storageMgr.canVmRestartOnAnotherServer(vm.getId())) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("VM can not restart on another server.");
            }
            return null;
        }

        if (work.getTimesTried() > _maxRetries) {
            s_logger.warn("Retried to max times so deleting: " + vmId);
            return null;
        }

        try {
            VMInstanceVO started = _itMgr.advanceStart(vm, new HashMap<VirtualMachineProfile.Param, Object>(), _accountMgr.getSystemUser(), _accountMgr.getSystemAccount());
            if (started != null) {
                s_logger.info("VM is now restarted: " + vmId + " on " + started.getHostId());
                return null;
            }

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Rescheduling VM " + vm.toString() + " to try again in " + _restartRetryInterval);
            }
        } catch (final InsufficientCapacityException e) {
            s_logger.warn("Unable to restart " + vm.toString() + " due to " + e.getMessage());
            _alertMgr.sendAlert(alertType, vm.getDataCenterIdToDeployIn(), vm.getPodIdToDeployIn(), "Unable to restart " + vm.getHostName() + " which was running on host " + hostDesc,
                    "Insufficient capacity to restart VM, name: " + vm.getHostName() + ", id: " + vmId + " which was running on host " + hostDesc);
        } catch (final ResourceUnavailableException e) {
            s_logger.warn("Unable to restart " + vm.toString() + " due to " + e.getMessage());
            _alertMgr.sendAlert(alertType, vm.getDataCenterIdToDeployIn(), vm.getPodIdToDeployIn(), "Unable to restart " + vm.getHostName() + " which was running on host " + hostDesc,
                    "The Storage is unavailable for trying to restart VM, name: " + vm.getHostName() + ", id: " + vmId + " which was running on host " + hostDesc);
        } catch (ConcurrentOperationException e) {
            s_logger.warn("Unable to restart " + vm.toString() + " due to " + e.getMessage());
            _alertMgr.sendAlert(alertType, vm.getDataCenterIdToDeployIn(), vm.getPodIdToDeployIn(), "Unable to restart " + vm.getHostName() + " which was running on host " + hostDesc,
                    "The Storage is unavailable for trying to restart VM, name: " + vm.getHostName() + ", id: " + vmId + " which was running on host " + hostDesc);
        } catch (OperationTimedoutException e) {
            s_logger.warn("Unable to restart " + vm.toString() + " due to " + e.getMessage());
            _alertMgr.sendAlert(alertType, vm.getDataCenterIdToDeployIn(), vm.getPodIdToDeployIn(), "Unable to restart " + vm.getHostName() + " which was running on host " + hostDesc,
                    "The Storage is unavailable for trying to restart VM, name: " + vm.getHostName() + ", id: " + vmId + " which was running on host " + hostDesc);
        }
        vm = _itMgr.findById(vm.getType(), vm.getId());
        work.setUpdateTime(vm.getUpdated());
        work.setPreviousState(vm.getState());
        return (System.currentTimeMillis() >> 10) + _restartRetryInterval;
    }

    public Long migrate(final HaWorkVO work) {
        long vmId = work.getInstanceId();

        long srcHostId = work.getHostId();
        try {
            work.setStep(Step.Migrating);
            _haDao.update(work.getId(), work);

            if (!_itMgr.migrateAway(work.getType(), vmId, srcHostId)) {
                s_logger.warn("Unable to migrate vm from " + srcHostId);
                _agentMgr.maintenanceFailed(srcHostId);
            }
            return null;
        } catch (InsufficientServerCapacityException e) {
            s_logger.warn("Insufficient capacity for migrating a VM.");
            _agentMgr.maintenanceFailed(srcHostId);
            return (System.currentTimeMillis() >> 10) + _migrateRetryInterval;
        } catch (VirtualMachineMigrationException e) {
            s_logger.warn("Looks like VM is still starting, we need to retry migrating the VM later.");
            _agentMgr.maintenanceFailed(srcHostId);
            return (System.currentTimeMillis() >> 10) + _migrateRetryInterval;
        }
    }

    @Override
    public void scheduleDestroy(VMInstanceVO vm, long hostId) {
        final HaWorkVO work = new HaWorkVO(vm.getId(), vm.getType(), WorkType.Destroy, Step.Scheduled, hostId, vm.getState(), 0, vm.getUpdated());
        _haDao.persist(work);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Scheduled " + work.toString());
        }
        wakeupWorkers();
    }

    @Override
    public void cancelDestroy(VMInstanceVO vm, Long hostId) {
        _haDao.delete(vm.getId(), WorkType.Destroy);
    }

    protected Long destroyVM(HaWorkVO work) {
        final VMInstanceVO vm = _itMgr.findById(work.getType(), work.getInstanceId());
        s_logger.info("Destroying " + vm.toString());
        try {
            if (vm.getState() != State.Destroyed) {
                s_logger.info("VM is no longer in Destroyed state " + vm.toString());
                return null;
            }

            if (vm.getHostId() != null) {
                if (_itMgr.destroy(vm, _accountMgr.getSystemUser(), _accountMgr.getSystemAccount())) {
                    s_logger.info("Successfully destroy " + vm);
                    return null;
                }
                s_logger.debug("Stop for " + vm + " was unsuccessful.");
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug(vm + " has already been stopped");
                }
                return null;
            }
        } catch (final AgentUnavailableException e) {
            s_logger.debug("Agnet is not available" + e.getMessage());
        } catch (OperationTimedoutException e) {
            s_logger.debug("operation timed out: " + e.getMessage());
        } catch (ConcurrentOperationException e) {
            s_logger.debug("concurrent operation: " + e.getMessage());
        }

        work.setTimesTried(work.getTimesTried() + 1);
        return (System.currentTimeMillis() >> 10) + _stopRetryInterval;
    }

    protected Long stopVM(final HaWorkVO work) throws ConcurrentOperationException {
        VMInstanceVO vm = _itMgr.findById(work.getType(), work.getInstanceId());
        if (vm == null) {
            s_logger.info("No longer can find VM " + work.getInstanceId() + ". Throwing away " + work);
            work.setStep(Step.Done);
            return null;
        }
        s_logger.info("Stopping " + vm);
        try {
            if (work.getWorkType() == WorkType.Stop) {
                if (_itMgr.advanceStop(vm, false, _accountMgr.getSystemUser(), _accountMgr.getSystemAccount())) {
                    s_logger.info("Successfully stopped " + vm);
                    return null;
                }
            } else if (work.getWorkType() == WorkType.CheckStop) {
                if ((vm.getState() != work.getPreviousState()) || vm.getUpdated() != work.getUpdateTime() || vm.getHostId() == null || vm.getHostId().longValue() != work.getHostId()) {
                    s_logger.info(vm + " is different now.  Scheduled Host: " + work.getHostId() + " Current Host: " + (vm.getHostId() != null ? vm.getHostId() : "none") + " State: " + vm.getState());
                    return null;
                }
                if (_itMgr.advanceStop(vm, false, _accountMgr.getSystemUser(), _accountMgr.getSystemAccount())) {
                    s_logger.info("Stop for " + vm + " was successful");
                    return null;
                }
            } else if (work.getWorkType() == WorkType.ForceStop) {
                if ((vm.getState() != work.getPreviousState()) || vm.getUpdated() != work.getUpdateTime() || vm.getHostId() == null || vm.getHostId().longValue() != work.getHostId()) {
                    s_logger.info(vm + " is different now.  Scheduled Host: " + work.getHostId() + " Current Host: " + (vm.getHostId() != null ? vm.getHostId() : "none") + " State: " + vm.getState());
                    return null;
                }
                if (_itMgr.advanceStop(vm, true, _accountMgr.getSystemUser(), _accountMgr.getSystemAccount())) {
                    s_logger.info("Stop for " + vm + " was successful");
                    return null;
                }
            } else {
                assert false : "Who decided there's other steps but didn't modify the guy who does the work?";
            }
        } catch (final ResourceUnavailableException e) {
            s_logger.debug("Agnet is not available" + e.getMessage());
        } catch (OperationTimedoutException e) {
            s_logger.debug("operation timed out: " + e.getMessage());
        }

        work.setTimesTried(work.getTimesTried() + 1);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Stop was unsuccessful.  Rescheduling");
        }
        return (System.currentTimeMillis() >> 10) + _stopRetryInterval;
    }

    @Override
    public void cancelScheduledMigrations(final HostVO host) {
        WorkType type = host.getType() == HostVO.Type.Storage ? WorkType.Stop : WorkType.Migration;

        _haDao.deleteMigrationWorkItems(host.getId(), type, _serverId);
    }

    @Override
    public List<VMInstanceVO> findTakenMigrationWork() {
        List<HaWorkVO> works = _haDao.findTakenWorkItems(WorkType.Migration);
        List<VMInstanceVO> vms = new ArrayList<VMInstanceVO>(works.size());
        for (HaWorkVO work : works) {
            vms.add(_instanceDao.findById(work.getInstanceId()));
        }
        return vms;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> xmlParams) throws ConfigurationException {
        _name = name;
        ComponentLocator locator = ComponentLocator.getLocator(ManagementServer.Name);

        _serverId = ((ManagementServer) ComponentLocator.getComponent(ManagementServer.Name)).getId();

        _investigators = locator.getAdapters(Investigator.class);
        _fenceBuilders = locator.getAdapters(FenceBuilder.class);

        Map<String, String> params = new HashMap<String, String>();
        final ConfigurationDao configDao = locator.getDao(ConfigurationDao.class);
        if (configDao != null) {
            params = configDao.getConfiguration(Long.toHexString(_serverId), xmlParams);
        }

        String value = params.get("workers");
        final int count = NumbersUtil.parseInt(value, 1);
        _workers = new WorkerThread[count];
        for (int i = 0; i < _workers.length; i++) {
            _workers[i] = new WorkerThread("HA-Worker-" + i);
        }

        value = params.get("force.ha");
        _forceHA = Boolean.parseBoolean(value);

        value = params.get("time.to.sleep");
        _timeToSleep = NumbersUtil.parseInt(value, 60) * 1000;

        value = params.get("max.retries");
        _maxRetries = NumbersUtil.parseInt(value, 5);

        value = params.get("time.between.failures");
        _timeBetweenFailures = NumbersUtil.parseLong(value, 3600) * 1000;

        value = params.get("time.between.cleanup");
        _timeBetweenCleanups = NumbersUtil.parseLong(value, 3600 * 24);

        value = params.get("stop.retry.interval");
        _stopRetryInterval = NumbersUtil.parseInt(value, 10 * 60);

        value = params.get("restart.retry.interval");
        _restartRetryInterval = NumbersUtil.parseInt(value, 10 * 60);

        value = params.get("investigate.retry.interval");
        _investigateRetryInterval = NumbersUtil.parseInt(value, 1 * 60);

        value = params.get("migrate.retry.interval");
        _migrateRetryInterval = NumbersUtil.parseInt(value, 2 * 60);

        _instance = params.get("instance");
        if (_instance == null) {
            _instance = "VMOPS";
        }

        _haDao.releaseWorkItems(_serverId);

        _stopped = true;

        _executor = Executors.newScheduledThreadPool(count, new NamedThreadFactory("HA"));

        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean start() {
        _stopped = false;

        for (final WorkerThread thread : _workers) {
            thread.start();
        }

        _executor.scheduleAtFixedRate(new CleanupTask(), _timeBetweenCleanups, _timeBetweenCleanups, TimeUnit.SECONDS);

        return true;
    }

    @Override
    public boolean stop() {
        _stopped = true;

        wakeupWorkers();

        _executor.shutdown();

        return true;
    }

    protected class CleanupTask implements Runnable {
        @Override
        public void run() {
            s_logger.info("HA Cleanup Thread Running");

            try {
                _haDao.cleanup(System.currentTimeMillis() - _timeBetweenFailures);
            } catch (Exception e) {
                s_logger.warn("Error while cleaning up", e);
            } finally {
                StackMaid.current().exitCleanup();
            }
        }
    }

    protected class WorkerThread extends Thread {
        public WorkerThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            s_logger.info("Starting work");
            while (!_stopped) {
                HaWorkVO work = null;
                try {
                    s_logger.trace("Checking the database");
                    work = _haDao.take(_serverId);
                    if (work == null) {
                        try {
                            synchronized (this) {
                                wait(_timeToSleep);
                            }
                            continue;
                        } catch (final InterruptedException e) {
                            s_logger.info("Interrupted");
                            continue;
                        }
                    }

                    NDC.push("work-" + work.getId());
                    s_logger.info("Processing " + work);

                    try {
                        final WorkType wt = work.getWorkType();
                        Long nextTime = null;
                        if (wt == WorkType.Migration) {
                            nextTime = migrate(work);
                        } else if (wt == WorkType.HA) {
                            nextTime = restart(work);
                        } else if (wt == WorkType.Stop || wt == WorkType.CheckStop || wt == WorkType.ForceStop) {
                            nextTime = stopVM(work);
                        } else if (wt == WorkType.Destroy) {
                            nextTime = destroyVM(work);
                        } else {
                            assert false : "How did we get here with " + wt.toString();
                            continue;
                        }

                        if (nextTime == null) {
                            s_logger.info("Completed " + work);
                            work.setStep(Step.Done);
                        } else {
                            s_logger.info("Rescheduling " + work + " to try again at " + new Date(nextTime << 10));
                            work.setTimeToTry(nextTime);
                            work.setServerId(null);
                            work.setDateTaken(null);
                        }
                    } catch (Exception e) {
                        s_logger.error("Terminating " + work, e);
                        work.setStep(Step.Error);
                    }
                    _haDao.update(work.getId(), work);
                } catch (final Throwable th) {
                    s_logger.error("Caught this throwable, ", th);
                } finally {
                    StackMaid.current().exitCleanup();
                    if (work != null) {
                        NDC.pop();
                    }
                }
            }
            s_logger.info("Time to go home!");
        }

        public synchronized void wakup() {
            notifyAll();
        }
    }

    @Override
    public void onManagementNodeJoined(List<ManagementServerHostVO> nodeList, long selfNodeId) {
    }

    @Override
    public void onManagementNodeLeft(List<ManagementServerHostVO> nodeList, long selfNodeId) {
        for (ManagementServerHostVO node : nodeList) {
            _haDao.releaseWorkItems(node.getMsid());
        }
    }

    @Override
    public void onManagementNodeIsolated() {
    }
}
