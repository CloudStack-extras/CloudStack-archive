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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.ejb.Local;
import javax.naming.ConfigurationException;
import javax.persistence.EntityExistsException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.manager.Commands;
import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.network.ovs.dao.OvsTunnelAccountDao;
import com.cloud.network.ovs.dao.OvsTunnelAccountVO;
import com.cloud.network.ovs.dao.OvsTunnelDao;
import com.cloud.utils.Pair;
import com.cloud.utils.component.Inject;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.UserVmDao;

@Local(value={OvsTunnelManager.class})
public class OvsTunnelManagerImpl implements OvsTunnelManager {
	public static final Logger s_logger = Logger.getLogger(OvsTunnelManagerImpl.class.getName());
	
	String _name;
	boolean _isEnabled;
	ScheduledExecutorService _executorPool;
    ScheduledExecutorService _cleanupExecutor;
    OvsTunnelListener _listener;
    
	@Inject ConfigurationDao _configDao;
	@Inject OvsTunnelDao _tunnelDao;
	@Inject HostDao _hostDao;
	@Inject UserVmDao _userVmDao;
	@Inject DomainRouterDao _routerDao;
	@Inject OvsTunnelAccountDao _tunnelAccountDao;
	@Inject AgentManager _agentMgr;
	
	@Override
	public boolean configure(String name, Map<String, Object> params)
			throws ConfigurationException {
		_name = name;
		_isEnabled = Boolean.parseBoolean(_configDao.getValue(Config.OvsTunnelNetwork.key()));
		
		if (_isEnabled) {
			_executorPool = Executors.newScheduledThreadPool(10, new NamedThreadFactory("OVS"));
			_cleanupExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("OVS-Cleanup"));
			_listener = new OvsTunnelListener(_tunnelDao, _hostDao);
			_agentMgr.registerForHostEvents(_listener, true, true, true);
		}
		
		return true;
	}

	protected int getGreKey(long from, long to, long account) {
		OvsTunnelAccountVO ta = null;
		int key;
		
		try {
			key = _tunnelDao.askKey(from, to);
			ta = new OvsTunnelAccountVO(from, to, key, account);
			OvsTunnelAccountVO lock = _tunnelAccountDao.acquireInLockTable(Long.valueOf(1));
			if (lock == null) {
			    s_logger.warn("Cannot lock table ovs_tunnel_account");
			    return -1;
			}
			_tunnelAccountDao.persist(ta);
			_tunnelAccountDao.releaseFromLockTable(lock.getId());
		} catch (EntityExistsException e) {
			ta = _tunnelAccountDao.getByFromToAccount(from, to, account);
			if (ta == null) {
				key = -1;
			} else {
				key = ta.getKey();
			}
		}
		
		return key;
	}

	private void handleCreateTunnelAnswer(Answer[] answers){
		OvsCreateTunnelAnswer r = (OvsCreateTunnelAnswer) answers[0];
		String s = String.format(
				"(hostIP:%1$s, remoteIP:%2$s, bridge:%3$s, greKey:%4$s, portName:%5$s)",
				r.getFromIp(), r.getToIp(), r.getBridge(), r.getKey(), r.getInPortName());
		Long from = r.getFrom();
		Long to = r.getTo();
		long account = r.getAccount();
		OvsTunnelAccountVO ta = _tunnelAccountDao.getByFromToAccount(from, to, account);
		if (ta == null) {
            throw new CloudRuntimeException(String.format("Unable find tunnelAccount record(from=%1$s, to=%2$s, account=%3$s", from, to, account));
		}
		
		if (!r.getResult()) {
		    ta.setState("FAILED");
			s_logger.warn("Create GRE tunnel failed due to " + r.getDetails() + s);
		} else {
		    ta.setState("SUCCESS");
		    ta.setPortName(r.getInPortName());
		    s_logger.warn("Create GRE tunnel " + r.getDetails() + s);
		}
		_tunnelAccountDao.update(ta.getId(), ta);
	}
	
	@DB
    protected void CheckAndCreateTunnel(VirtualMachine instance, DeployDestination dest) {
		if (!_isEnabled) {
			return;
		}
		
		if (instance.getType() != VirtualMachine.Type.User
				&& instance.getType() != VirtualMachine.Type.DomainRouter) {
			return;
		}
		
		long hostId = dest.getHost().getId();
		long accountId = instance.getAccountId();
		List<UserVmVO>vms = _userVmDao.listByAccountId(accountId);
		/* FIXME: Redundant virtual router doesn't support OVM */
		DomainRouterVO router = _routerDao.findBy(accountId, instance.getDataCenterId()).get(0);
		List<VMInstanceVO>ins = new ArrayList<VMInstanceVO>();
		if (vms != null) {
			ins.addAll(vms);
		}
		if (router != null) {
			ins.add(router);
		}
		List<Pair<Long, Integer>>toHosts = new ArrayList<Pair<Long, Integer>>();
		List<Pair<Long, Integer>>fromHosts = new ArrayList<Pair<Long, Integer>>();
		int key;
		
        for (VMInstanceVO v : ins) {
            Long rh = v.getHostId();
            if (rh == null || rh.longValue() == hostId) {
                continue;
            }

            OvsTunnelAccountVO ta = _tunnelAccountDao.getByFromToAccount(hostId, rh.longValue(), accountId);
            if (ta == null) {
                key = getGreKey(hostId, rh.longValue(), accountId);
                if (key == -1) {
                    s_logger.warn(String.format("Cannot get GRE key for from=%1$s to=%2$s accountId=%3$s, tunnel create failed", hostId, rh.longValue(), accountId));
                    continue;
                }

                Pair<Long, Integer> p = new Pair<Long, Integer>(rh, Integer.valueOf(key));
                if (!toHosts.contains(p)) {
                    toHosts.add(p);
                } 
            }

            ta = _tunnelAccountDao.getByFromToAccount(rh.longValue(), hostId, accountId);
            if (ta == null) {
                key = getGreKey(rh.longValue(), hostId, accountId);
                if (key == -1) {
                    s_logger.warn(String.format("Cannot get GRE key for from=%1$s to=%2$s accountId=%3$s, tunnel create failed", rh.longValue(), hostId, accountId));
                    continue;
                }

                Pair<Long, Integer> p = new Pair<Long, Integer>(rh, Integer.valueOf(key));
                if (!fromHosts.contains(p)) {
                    fromHosts.add(p);
                }
            }
        }
		
		try {
			String myIp = dest.getHost().getPrivateIpAddress();
			for (Pair<Long, Integer> i : toHosts) {
				HostVO rHost = _hostDao.findById(i.first());
				Commands cmds = new Commands(
						new OvsCreateTunnelCommand(rHost.getPrivateIpAddress(), i.second().toString(), Long.valueOf(hostId), i.first(), accountId, myIp));
				s_logger.debug("Ask host " + hostId + " to create gre tunnel to " + i.first());
				Answer[] answers = _agentMgr.send(hostId, cmds);
				handleCreateTunnelAnswer(answers);
			}
			
			for (Pair<Long, Integer> i : fromHosts) {
			    HostVO rHost = _hostDao.findById(i.first());
				Commands cmd2s = new Commands(
				        new OvsCreateTunnelCommand(myIp, i.second().toString(), i.first(), Long.valueOf(hostId), accountId, rHost.getPrivateIpAddress()));
				s_logger.debug("Ask host " + i.first() + " to create gre tunnel to " + hostId);
				Answer[] answers = _agentMgr.send(i.first(), cmd2s);
				handleCreateTunnelAnswer(answers);
			}
		} catch (Exception e) {
		    s_logger.debug("Ovs Tunnel network created tunnel failed", e);
		}	
	}
	
	@Override
	public boolean start() {
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
	public boolean isOvsTunnelEnabled() {
		return _isEnabled;
	}

    @Override
    public void VmCheckAndCreateTunnel(VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest) {
        CheckAndCreateTunnel(vm.getVirtualMachine(), dest);    
    }

    private void handleDestroyTunnelAnswer(Answer ans, long from, long to, long account) {
        String toStr = (to == 0 ? "all peers" : Long.toString(to));
        
        if (ans.getResult()) {
            OvsTunnelAccountVO lock = _tunnelAccountDao.acquireInLockTable(Long.valueOf(1));
            if (lock == null) {
                s_logger.warn(String.format("failed to lock ovs_tunnel_account, remove record of tunnel(from=%1$s, to=%2$s account=%3$s) failed", from, to, account));
                return;
            }

            if (to == 0) {
                _tunnelAccountDao.removeByFromAccount(from, account);
            } else {
                _tunnelAccountDao.removeByFromToAccount(from, to, account);
            }
            _tunnelAccountDao.releaseFromLockTable(lock.getId());
            
            s_logger.debug(String.format("Destroy tunnel(account:%1$s, from:%2$s, to:%3$s) successful", account, from, toStr)); 
        } else {
            s_logger.debug(String.format("Destroy tunnel(account:%1$s, from:%2$s, to:%3$s) failed", account, from, toStr));
        }
    }
    
    @Override
    public void CheckAndDestroyTunnel(VirtualMachine vm) {
        if (!_isEnabled) {
            return;
        }
        
        List<UserVmVO> userVms = _userVmDao.listByAccountIdAndHostId(vm.getAccountId(), vm.getHostId());
        if (vm.getType() == VirtualMachine.Type.User) {
            if (userVms.size() > 1) {
                return;
            }
            
            DomainRouterVO router = _routerDao.findBy(vm.getAccountId(), vm.getDataCenterId()).get(0);
            if (router.getHostId() == vm.getHostId()) {
                return;
            }
        } else if (vm.getType() == VirtualMachine.Type.DomainRouter && userVms.size() != 0) {
                return;
        }
        
        try {
            /* Now we are last one on host, destroy all tunnels of my account */
            Command cmd = new OvsDestroyTunnelCommand(vm.getAccountId(), "[]");
            Answer ans = _agentMgr.send(vm.getHostId(), cmd);
            handleDestroyTunnelAnswer(ans, vm.getHostId(), 0, vm.getAccountId());
            
            /* Then ask hosts have peer tunnel with me to destroy them */
            List<OvsTunnelAccountVO> peers = _tunnelAccountDao.listByToAccount(vm.getHostId(), vm.getAccountId());
            for (OvsTunnelAccountVO p : peers) {
                cmd = new OvsDestroyTunnelCommand(p.getAccount(), p.getPortName());
                ans = _agentMgr.send(p.getFrom(), cmd);
                handleDestroyTunnelAnswer(ans, p.getFrom(), p.getTo(), p.getAccount());
            }
        } catch (Exception e) {
            s_logger.warn(String.format("Destroy tunnel(account:%1$s, hostId:%2$s) failed", vm.getAccountId(), vm.getHostId()), e);
        }
        
    }

}
