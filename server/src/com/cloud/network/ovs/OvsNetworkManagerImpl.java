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

package com.cloud.network.ovs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.manager.Commands;
import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.ovs.dao.GreTunnelDao;
import com.cloud.network.ovs.dao.GreTunnelVO;
import com.cloud.network.ovs.dao.OvsWorkDao;
import com.cloud.network.ovs.dao.OvsWorkVO;
import com.cloud.network.ovs.dao.OvsWorkVO.Step;
import com.cloud.network.ovs.dao.VlanMappingDao;
import com.cloud.network.ovs.dao.VlanMappingDirtyDao;
import com.cloud.network.ovs.dao.VlanMappingVO;
import com.cloud.network.ovs.dao.VmFlowLogDao;
import com.cloud.network.ovs.dao.VmFlowLogVO;
import com.cloud.server.ManagementServer;
import com.cloud.user.AccountVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.fsm.StateListener;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.Event;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;

@Local(value={OvsNetworkManager.class})
public class OvsNetworkManagerImpl implements OvsNetworkManager {
	private static final Logger s_logger = Logger.getLogger(OvsNetworkManagerImpl.class);
	@Inject ConfigurationDao _configDao;
	@Inject VlanMappingDao _vlanMappingDao;
	@Inject UserVmDao _userVmDao;
	@Inject HostDao _hostDao;
	@Inject AgentManager _agentMgr;
	@Inject NicDao _nicDao;
	@Inject NetworkDao _networkDao;
	@Inject VlanMappingDirtyDao _vlanMappingDirtyDao;
	@Inject DomainRouterDao _routerDao;
	@Inject OvsWorkDao _workDao;
	@Inject VmFlowLogDao _flowLogDao;
	@Inject UserVmDao _userVMDao;
	@Inject VMInstanceDao _instanceDao;
	@Inject AccountDao _accountDao;
	@Inject GreTunnelDao _tunnelDao;
	String _name;
	boolean _isEnabled;
	ScheduledExecutorService _executorPool;
    ScheduledExecutorService _cleanupExecutor;
    OvsListener _ovsListener;
    VmStateListener _stateListener;

	private long _serverId;
	private final long _timeBetweenCleanups = 30; //seconds
	
	public class VmStateListener implements StateListener<State, VirtualMachine.Event, VirtualMachine> {
	    OvsNetworkManager _mgr;
	    public VmStateListener(OvsNetworkManager mgr) {
	        _mgr = mgr;
	    }
	    
        @Override
        public boolean postStateTransitionEvent(State oldState, Event event, State newState, VirtualMachine vm, boolean status, Long oldHostId) {
            if (!_isEnabled || !status || (vm.getType() != VirtualMachine.Type.User && vm.getType() != VirtualMachine.Type.DomainRouter)) {
                return false;
            }

            if (VirtualMachine.State.isVmStarted(oldState, event, newState)) {
                _mgr.handleVmStateTransition((VMInstanceVO)vm, State.Running);
            } else if (VirtualMachine.State.isVmMigrated(oldState, event, newState)) {
            }
            return true;
        }

        @Override
        public boolean preStateTransitionEvent(State oldState, Event event, State newState, VirtualMachine vm, boolean status, Long id) {
            if (!_isEnabled || !status || (vm.getType() != VirtualMachine.Type.User && vm.getType() != VirtualMachine.Type.DomainRouter)) {
                return false;
            }
            
            if (VirtualMachine.State.isVmStopped(oldState, event, newState)) {
                _mgr.handleVmStateTransition((VMInstanceVO)vm, State.Stopped);
            }
            
            return true;
        }
	    
	}
	
	public  class WorkerThread implements Runnable {
		@Override
		public void run() {
			work();
		}
		
		WorkerThread() {
			
		}
	}
	
	public  class CleanupThread implements Runnable {
		@Override
		public void run() {
			cleanupFinishedWork();
			cleanupUnfinishedWork();
		}

		CleanupThread() {
			
		}
	}
	
	@Override
	public boolean configure(String name, Map<String, Object> params)
			throws ConfigurationException {
		_name = name;
		_isEnabled = Boolean.parseBoolean(_configDao.getValue(Config.OvsNetwork.key()));
		
		if (_isEnabled) {
			_serverId = ((ManagementServer)ComponentLocator.getComponent(ManagementServer.Name)).getId();
			_executorPool = Executors.newScheduledThreadPool(10, new NamedThreadFactory("OVS"));
			_cleanupExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("OVS-Cleanup"));
			_ovsListener = new OvsListener(this, _workDao, _tunnelDao, _vlanMappingDao, _hostDao);
			_agentMgr.registerForHostEvents(_ovsListener, true, true, true);
			_stateListener = new VmStateListener(this);
			VirtualMachine.State.getStateMachine().registerListener(_stateListener);
		}
		
		return true;
	}

	@Override
	public boolean start() {
		if (_isEnabled) {
			_cleanupExecutor.scheduleAtFixedRate(new CleanupThread(), _timeBetweenCleanups, _timeBetweenCleanups, TimeUnit.SECONDS);
		}
		return true;
	}

	@Override
	public boolean stop() {
		return true;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public boolean isOvsNetworkEnabled() {
		return _isEnabled;
	}

	public void cleanupFinishedWork() {
		Date before = new Date(System.currentTimeMillis() - 24*3600*1000l);
		int numDeleted = _workDao.deleteFinishedWork(before);
		if (numDeleted > 0) {
			s_logger.info("Ovs cleanup deleted " + numDeleted + " finished work items older than " + before.toString());
		}
		
	}
	

	private void cleanupUnfinishedWork() {
		Date before = new Date(System.currentTimeMillis() - 30*1000l);
		List<OvsWorkVO> unfinished = _workDao.findUnfinishedWork(before);
		if (unfinished.size() > 0) {
			s_logger.info("Ovscleanup found " + unfinished.size() + " unfinished work items older than " + before.toString());
			Set<Long> affectedVms = new HashSet<Long>();
			for (OvsWorkVO work: unfinished) {
				affectedVms.add(work.getInstanceId());
			}
			
			s_logger.info("Ovs cleanup re-schedule unfinished work");
			scheduleFlowUpdateToHosts(affectedVms, false, null);
		} else {
			s_logger.debug("Ovs cleanup found no unfinished work items older than " + before.toString());
		}
	}
	
	//TODO: think about lock, how new VM start when we change rows
	@DB
	public void work() {
	    if (s_logger.isTraceEnabled()) {
	        s_logger.trace("Checking the database");
	    }
		final OvsWorkVO work = _workDao.take(_serverId);
		if (work == null) {
			return;
		}
		Long userVmId = work.getInstanceId();
		VirtualMachine vm = null;
		Long seqnum = null;
		Long vmId = work.getInstanceId();
		s_logger.info("Ovs working on " + work.toString());
		final Transaction txn = Transaction.currentTxn();
		txn.start();
		try {
			vm = _userVMDao.acquireInLockTable(vmId);
			if (vm == null) {
				vm = _routerDao.acquireInLockTable(vmId);
				if (vm == null) {
					s_logger.warn("Ovs unable to acquire lock on vm id=" + userVmId);
					return ;
				}		
			}
			
			Long agentId = null;
			VmFlowLogVO log = _flowLogDao.findByVmId(userVmId);
			if (log == null) {
				s_logger.warn("Ovs cannot find log record for vm id=" + userVmId);
				return;
			}
			seqnum = log.getLogsequence();

			if (vm != null && vm.getState() == State.Running) {
				agentId = vm.getHostId();
				if (agentId != null ) {
					String vlans = getVlanInPortMapping(vm.getAccountId(), vm.getHostId());
					String tag = Long.toString(_vlanMappingDao.findByAccountIdAndHostId(
							vm.getAccountId(), vm.getHostId()).getVlan());

					Commands cmds = new Commands(new OvsSetTagAndFlowCommand(
							vm.getHostName(), tag, vlans, seqnum.toString(),
							vm.getId()));

					try {
						_agentMgr.send(agentId, cmds, _ovsListener);
					} catch (AgentUnavailableException e) {
						s_logger.debug("Unable to send updates for vm: "
								+ userVmId + "(agentid=" + agentId + ")");
						_workDao.updateStep(work.getInstanceId(), seqnum,
								Step.Done);
					}
				}
			}
		} finally {
			if (vm != null) {
				if (vm.getType() == VirtualMachine.Type.User) {
					_userVMDao.releaseFromLockTable(vmId);
				} else if (vm.getType() == VirtualMachine.Type.DomainRouter) {
					_routerDao.releaseFromLockTable(vmId);
				} else {
					assert 1 == 0 : "Should not be here";
				}
				
				_workDao.updateStep(work.getId(),  Step.Done);
			}
			txn.commit();
		}

	
	}
	
	@DB
	protected long askVlanId(long accountId, long hostId) throws OvsVlanExhaustedException {
		assert _isEnabled : "Who call me ??? while OvsNetwokr is not enabled!!!";
		final Transaction txn = Transaction.currentTxn();
		txn.start();
		
		VlanMappingVO currVlan = _vlanMappingDao.lockByAccountIdAndHostId(accountId, hostId);
		long vlan = 0;
		
		if (currVlan != null) {
			vlan = currVlan.getVlan();
			currVlan.ref();
			_vlanMappingDao.update(currVlan.getId(), currVlan);
			s_logger.debug("Already has an Vlan " + vlan + " on host " + hostId
					+ " for account " + accountId + ", use it, reference count is " + currVlan.getRef());
			txn.commit();
			return vlan;
		}
		
		List<VlanMappingVO>mappings = _vlanMappingDao.listByHostId(hostId);
		assert mappings.size() > 0: "where is my data!? it should be added when host connected!";
	
		VlanMappingVO target = null;
		for (VlanMappingVO vo : mappings) {
			if (vo.getAccountId() == 0) {
				target = _vlanMappingDao.lockRow(vo.getId(), true);
				if (target == null || target.getAccountId() != 0) {
					s_logger.debug("Someone took vlan mapping host = "
							+ vo.getHostId() + " vlan = " + vo.getVlan());
					continue;
				} else {
					break;
				}
			}
		}
		
		if (target == null) {
			throw new OvsVlanExhaustedException("vlan exhausted on host " + hostId);
		}
		
		target.setAccountId(accountId);
		target.ref();
		_vlanMappingDao.update(target.getId(), target);
		_vlanMappingDirtyDao.markDirty(accountId);
		String s = String.format("allocate a new vlan %1$s(account:%2$s, hostId:%3$s), mark dirty",
						vlan, accountId, hostId);
		s_logger.debug("OVSDIRTY:" + s);
		txn.commit();
		return target.getVlan();
	}

	private void handleCreateTunnelAnswer(Answer[] answers) throws GreTunnelException {
		OvsCreateGreTunnelAnswer r = (OvsCreateGreTunnelAnswer) answers[0];
		String s = String.format(
				"(hostIP:%1$s, remoteIP:%2$s, bridge:%3$s, greKey:%4$s)",
				r.getHostIp(), r.getRemoteIp(), r.getBridge(), r.getKey());

		if (!r.getResult()) {
			s_logger.warn("Create GRE tunnel failed due to " + r.getDetails()
					+ s);
		} else {
			GreTunnelVO tunnel = _tunnelDao.getByFromAndTo(r.getFrom(), r.getTo());
			if (tunnel == null) {
				throw new GreTunnelException("No record matches from = "
						+ r.getFrom() + " to = " + r.getTo());
			} else {
				tunnel.setInPort(r.getPort());
				_tunnelDao.update(tunnel.getId(), tunnel);
				s_logger.info("Create GRE tunnel success" + s + " from "
						+ r.getFrom() + " to " + r.getTo() + " inport="
						+ r.getPort());
			}
		}
		
	}

	@DB
	protected void CheckAndCreateTunnel(VirtualMachine instance,
			DeployDestination dest) throws GreTunnelException {
		if (!_isEnabled) {
			return;
		}
		
		if (instance.getType() != VirtualMachine.Type.User
				&& instance.getType() != VirtualMachine.Type.DomainRouter) {
			return;
		}
		
		final Transaction txn = Transaction.currentTxn();
		long hostId = dest.getHost().getId();
		long accountId = instance.getAccountId();
		List<UserVmVO>vms = _userVmDao.listByAccountId(accountId);
		/* FIXME: Redundant virtual router doesn't support OVS */
		DomainRouterVO router = _routerDao.findBy(accountId, instance.getDataCenterId()).get(0);
		List<VMInstanceVO>ins = new ArrayList<VMInstanceVO>();
		ins.addAll(vms);
		ins.add(router);
		List<Long>toHostIds = new ArrayList<Long>();
		List<Long>fromHostIds = new ArrayList<Long>();
		
		for (VMInstanceVO v : ins) {
			Long rh = v.getHostId();
			if (rh == null || rh.longValue() == hostId) {
				continue;
			}
			
			txn.start();
			GreTunnelVO tunnel = _tunnelDao.lockByFromAndTo(hostId,
					rh.longValue());
			txn.commit();
			if (tunnel == null) {
				throw new GreTunnelException(String.format(
						"No entity(from=%1$s, to=%2$s) of failed to lock",
						hostId, rh.longValue()));
			}

			if (tunnel.getInPort() == 0 && !toHostIds.contains(rh)) {
				toHostIds.add(rh);
			}
			
			txn.start();
			tunnel = _tunnelDao.lockByFromAndTo(rh.longValue(), hostId);
			txn.commit();
			if (tunnel == null) {
				throw new GreTunnelException(String.format(
						"No entity(from=%1$s, to=%2$s) of failed to lock",
						rh.longValue(), hostId));
			}

			if (tunnel.getInPort() == 0 && !fromHostIds.contains(rh)) {
				fromHostIds.add(rh);
			}
		}
		
		try {
			String myIp = dest.getHost().getPrivateIpAddress();
			for (Long i : toHostIds) {
				HostVO rHost = _hostDao.findById(i.longValue());
				Commands cmds = new Commands(
						new OvsCreateGreTunnelCommand(
								rHost.getPrivateIpAddress(), "1", hostId,
								i.longValue()));
				s_logger.debug("Ask host " + hostId + " to create gre tunnel to " + i.longValue());
				Answer[] answers = _agentMgr.send(hostId, cmds);
				handleCreateTunnelAnswer(answers);
			}
			
			for (Long i : fromHostIds) {
				Commands cmd2s = new Commands(new OvsCreateGreTunnelCommand(myIp, "1", i.longValue(), hostId));
				s_logger.debug("Ask host " + i.longValue() + " to create gre tunnel to " + hostId);
				Answer[] answers = _agentMgr.send(i.longValue(), cmd2s);
				handleCreateTunnelAnswer(answers);
			}
		} catch (Exception e) {
		    s_logger.warn("Ovs vlan remap network creates tunnel failed", e);
		}	
	}
	
	@DB
	protected String getVlanInPortMapping(long accountId, long from) {
		List<GreTunnelVO> tunnels = _tunnelDao.getByFrom(from);
		if (tunnels.size() == 0) {
			return "[]";
		} else {
			List<String> maps = new ArrayList<String>();
			for (GreTunnelVO t : tunnels) {
				VlanMappingVO m = _vlanMappingDao.findByAccountIdAndHostId(accountId, t.getTo());
				if (m == null) {
					s_logger.debug("Host " + t.getTo() + " has no VM for account " + accountId + ", skip it");
					continue;
				}
				String s = String.format("%1$s:%2$s", m.getVlan(), t.getInPort());
				maps.add(s);
			}
			
			return maps.toString();
		}
	}
	
	private String cmdPair(String key, String value) {
	    return String.format("%1$s;%2$s", key, value);
	}
	
	@Override
	public String applyDefaultFlow(VirtualMachine instance, DeployDestination dest) {
		if (!_isEnabled) {
			return null;
		}
		
		VirtualMachine.Type vmType = instance.getType();
		if (vmType != VirtualMachine.Type.User
				&& vmType != VirtualMachine.Type.DomainRouter) {
			return null;
		}

		try {
			long hostId = instance.getHostId();
			long accountId = instance.getAccountId();
			String tag = Long.toString(askVlanId(accountId, hostId));
			CheckAndUpdateDhcpFlow(instance);
			String vlans = getVlanInPortMapping(accountId, hostId);
			VmFlowLogVO log = _flowLogDao.findOrNewByVmId(instance.getId(),
					instance.getHostName());
			StringBuffer command = new StringBuffer();
			command.append("vlan");
			command.append("/");
			command.append(cmdPair("vmName", instance.getHostName()));
			command.append("/");
			command.append(cmdPair("tag", tag));
			command.append("/");
			vlans = vlans.replace("[", "@");
			vlans = vlans.replace("]", "#");
			command.append(cmdPair("vlans", vlans));
			command.append("/");
			command.append(cmdPair("seqno", Long.toString(log.getLogsequence())));
			command.append("/");
			command.append(cmdPair("vmId", Long.toString(instance.getId())));
			return command.toString();
		} catch (OvsVlanExhaustedException e) {
			s_logger.warn("vlan exhaused on host " + instance.getHostId(), e);
			return null;
		}
	}
	
	//FIXME: if router has record in database but not start, this will hang 10 secs due to host
	//plugin cannot found vif for router.
	protected void CheckAndUpdateDhcpFlow(VirtualMachine instance) {
		if (!_isEnabled) {
			return;
		}
		
		if (instance.getType() == VirtualMachine.Type.DomainRouter) {
			return;
		}
		
		long accountId = instance.getAccountId();
		/* FIXME: Redundant virtual router doesn't support OVS */
		DomainRouterVO router = _routerDao.findBy(accountId, instance.getDataCenterId()).get(0);
		if (router == null) {
			return;
		}
		
		if (!_vlanMappingDirtyDao.isDirty(accountId)) {
			return;
		}
		
		try {
			long hostId = router.getHostId();
			String tag = Long.toString(_vlanMappingDao.findByAccountIdAndHostId(accountId, hostId).getVlan());
			VmFlowLogVO log = _flowLogDao.findOrNewByVmId(instance.getId(), instance.getHostName());
			String vlans = getVlanInPortMapping(accountId, hostId);
			s_logger.debug("ask router " + router.getHostName() + " on host "
					+ hostId + " update vlan map to " + vlans);
			Commands cmds = new Commands(new OvsSetTagAndFlowCommand(
					router.getHostName(), tag, vlans, Long.toString(log.getLogsequence()), instance.getId()));
			_agentMgr.send(router.getHostId(), cmds, _ovsListener);
		} catch (Exception e) {
			s_logger.warn("apply flow to router failed", e);
		}
	}
	
	@DB
	@Override
	public void scheduleFlowUpdateToHosts(Set<Long> affectedVms, boolean updateSeqno, Long delayMs) {
	    if (!_isEnabled) {
	        return;
	    }
	    
		if (affectedVms == null) {
			return;
		}
		
		if (delayMs == null) {
            delayMs = new Long(100l);
        }
		
		for (Long vmId: affectedVms) {
			Transaction txn = Transaction.currentTxn();
			txn.start();
			VmFlowLogVO log = null;
			OvsWorkVO work = null;
			VirtualMachine vm = null;
			try {
				vm = _userVMDao.acquireInLockTable(vmId);
				if (vm == null) {
					vm = _routerDao.acquireInLockTable(vmId);
					if (vm == null) {
						s_logger.warn("Ovs failed to acquire lock on vm id " + vmId);
						continue;
					}
				}
				log = _flowLogDao.findOrNewByVmId(vmId, vm.getHostName());
		
				if (log != null && updateSeqno){
					log.incrLogsequence();
					_flowLogDao.update(log.getId(), log);
				}
				
				work = _workDao.findByVmIdStep(vmId, Step.Scheduled);
				if (work == null) {
					work = new OvsWorkVO(vmId,  null, null, OvsWorkVO.Step.Scheduled, null);
					work = _workDao.persist(work);
				}
				
				work.setLogsequenceNumber(log.getLogsequence());
				 _workDao.update(work.getId(), work);	
			} finally {
				if (vm != null) {
					if (vm.getType() == VirtualMachine.Type.User) {
						_userVMDao.releaseFromLockTable(vmId);
					} else if (vm.getType() == VirtualMachine.Type.DomainRouter) {
						_routerDao.releaseFromLockTable(vmId);
					} else {
						assert 1 == 0 : "Should not be here";
					}
				}
			}
			txn.commit();

			_executorPool.schedule(new WorkerThread(), delayMs, TimeUnit.MILLISECONDS);

		}
	}
	
	protected Set<Long> getAffectedVms(VMInstanceVO instance, boolean tellRouter) {
		long accountId = instance.getAccountId();
		if (!_vlanMappingDirtyDao.isDirty(accountId)) {
			s_logger.debug("OVSAFFECTED: no VM affected by " + instance.getHostName());
			return null;
		}
		
		Set<Long> affectedVms = new HashSet<Long>();
		List<UserVmVO> vms = _userVmDao.listByAccountId(accountId);
		for (UserVmVO vm : vms) {
			affectedVms.add(new Long(vm.getId()));
		}
		
		if (tellRouter && instance.getType() != VirtualMachine.Type.DomainRouter) {
		    /* FIXME: Redundant virtual router doesn't support OVS */
			DomainRouterVO router = _routerDao.findBy(accountId, instance.getDataCenterId()).get(0);
			if (router != null) {
				affectedVms.add(new Long(router.getId()));
			}
		}
		return affectedVms;
	}
	
	protected void handleVmStateChange(VMInstanceVO instance, boolean tellRouter) {
		Set<Long> affectedVms = getAffectedVms(instance, tellRouter);
		scheduleFlowUpdateToHosts(affectedVms, true, null);
		_vlanMappingDirtyDao.clean(instance.getAccountId());
		s_logger.debug("OVSDIRTY:Clean dirty for account " + instance.getAccountId());
	}
	
	@DB
	protected void checkAndRemove(VMInstanceVO instance) {
		long accountId = instance.getAccountId();
		long hostId = instance.getHostId();
		
		final Transaction txn = Transaction.currentTxn();
		txn.start();
		VlanMappingVO vo = _vlanMappingDao.lockByAccountIdAndHostId(accountId, hostId);
		assert vo!=null: "Why there is no record for account " + accountId + " host " + hostId;
		if (vo.unref() == 0) {
			vo.setAccountId(0);
			_vlanMappingDirtyDao.markDirty(accountId);
			String s = String.format("%1$s is the last VM(host:%2$s, accountId:%3$s), remove vlan",
							instance.getHostName(), hostId, accountId);
			s_logger.debug("OVSDIRTY:" + s);
		} else {
			s_logger.debug(instance.getHostName()
					+ " reduces reference count of (account,host) = ("
					+ accountId + "," + hostId + ") to " + vo.getRef());
		}
		_vlanMappingDao.update(vo.getId(), vo);
		_flowLogDao.deleteByVmId(instance.getId());
		txn.commit();
		
		try {
			Commands cmds = new Commands(new OvsDeleteFlowCommand(instance.getHostName()));
			_agentMgr.send(hostId, cmds, _ovsListener);
		} catch (Exception e) {
		    s_logger.warn("remove flow failed", e);
		}
	}
	
	@Override
	public void handleVmStateTransition(VMInstanceVO instance, State vmState) {
		if (!_isEnabled) {
			return;
		}
		
		switch (vmState) {
		case Destroyed:
		case Error:
		case Migrating:
		case Expunging:
		case Starting:
		case Unknown:
			return;
		case Running:
			handleVmStateChange(instance, false);
			break;
		case Stopping:
		case Stopped:
			checkAndRemove(instance);
			handleVmStateChange(instance, true);
			break;
		}
		
	}

	@Override
	public void VmCheckAndCreateTunnel(VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest) {
		try {
            CheckAndCreateTunnel(vm.getVirtualMachine(), dest);
        } catch (Exception e) {
           s_logger.warn("create gre tunnel failed", e);
        }	
	}

	@Override
	public void fullSync(List<Pair<String, Long>> states) {
		if (!_isEnabled) {
			return;
		}
		
		//TODO:debug code, remove in future
		List<AccountVO> accounts = _accountDao.listAll();
		for (AccountVO acnt : accounts) {
			if (_vlanMappingDirtyDao.isDirty(acnt.getId())) {
				s_logger.warn("Vlan mapping for account "
						+ acnt.getAccountName() + " id " + acnt.getId()
						+ " is dirty");
			}
		}
		
		if (states.size() ==0) {
			s_logger.info("Nothing to do, Ovs fullsync is happy");
			return;
		}
		
		Set<Long>vmIds = new HashSet<Long>();
		for (Pair<String, Long>state : states) {
			if (state.second() == -1) {
				s_logger.warn("Ovs fullsync get wrong seqno for " + state.first());
				continue;
			}
			VmFlowLogVO log = _flowLogDao.findByName(state.first());
			if (log.getLogsequence() != state.second()) {
				s_logger.debug("Ovs fullsync detected unmatch seq number for " + state.first() + ", run sync");
				VMInstanceVO vo = _instanceDao.findById(log.getInstanceId());
				if (vo == null) {
					s_logger.warn("Ovs can't find " + state.first() + " in vm_instance!");
					continue;
				}
				
				if (vo.getType() != VirtualMachine.Type.User && vo.getType() != VirtualMachine.Type.DomainRouter) {
					s_logger.warn("Ovs fullsync: why we sync a " + vo.getType().toString() + " VM???");
					continue;
				}
				vmIds.add(new Long(vo.getId()));
			}
		}
		
		if (vmIds.size() > 0) {
			scheduleFlowUpdateToHosts(vmIds, false, null);
		}
	}

}
