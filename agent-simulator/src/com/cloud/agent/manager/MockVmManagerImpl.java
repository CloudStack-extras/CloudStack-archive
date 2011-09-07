/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.agent.manager;


import java.util.HashMap;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckVirtualMachineAnswer;
import com.cloud.agent.api.CheckVirtualMachineCommand;
import com.cloud.agent.api.CleanupNetworkRulesCmd;
import com.cloud.agent.api.GetVmStatsAnswer;
import com.cloud.agent.api.GetVmStatsCommand;
import com.cloud.agent.api.GetVncPortAnswer;
import com.cloud.agent.api.GetVncPortCommand;
import com.cloud.agent.api.MigrateAnswer;
import com.cloud.agent.api.MigrateCommand;
import com.cloud.agent.api.NetworkUsageAnswer;
import com.cloud.agent.api.NetworkUsageCommand;
import com.cloud.agent.api.RebootCommand;
import com.cloud.agent.api.SecurityIngressRuleAnswer;
import com.cloud.agent.api.SecurityIngressRulesCmd;
import com.cloud.agent.api.StartAnswer;
import com.cloud.agent.api.StartCommand;
import com.cloud.agent.api.StopAnswer;
import com.cloud.agent.api.StopCommand;
import com.cloud.agent.api.VmStatsEntry;
import com.cloud.agent.api.check.CheckSshAnswer;
import com.cloud.agent.api.check.CheckSshCommand;
import com.cloud.agent.api.proxy.CheckConsoleProxyLoadCommand;
import com.cloud.agent.api.proxy.WatchConsoleProxyLoadCommand;
import com.cloud.agent.api.routing.DhcpEntryCommand;
import com.cloud.agent.api.routing.IpAssocCommand;
import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.routing.SavePasswordCommand;
import com.cloud.agent.api.routing.SetPortForwardingRulesAnswer;
import com.cloud.agent.api.routing.SetPortForwardingRulesCommand;
import com.cloud.agent.api.routing.SetStaticNatRulesAnswer;
import com.cloud.agent.api.routing.SetStaticNatRulesCommand;
import com.cloud.agent.api.routing.VmDataCommand;
import com.cloud.agent.api.storage.DestroyCommand;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.VirtualMachineTO;

import com.cloud.network.Networks.TrafficType;

import com.cloud.simulator.MockHost;
import com.cloud.simulator.MockHostVO;
import com.cloud.simulator.MockSecurityRulesVO;
import com.cloud.simulator.MockVMVO;
import com.cloud.simulator.MockVm;
import com.cloud.simulator.dao.MockHostDao;
import com.cloud.simulator.dao.MockSecurityRulesDao;
import com.cloud.simulator.dao.MockVMDao;
import com.cloud.utils.Pair;
import com.cloud.utils.component.Inject;

import com.cloud.vm.VirtualMachine.State;

@Local(value = { MockVmManager.class })
public class MockVmManagerImpl implements MockVmManager {
    private static final Logger s_logger = Logger.getLogger(MockVmManagerImpl.class);

	@Inject MockVMDao _mockVmDao = null;
	@Inject MockAgentManager _mockAgentMgr = null;
	@Inject MockHostDao _mockHostDao = null;
	@Inject MockSecurityRulesDao _mockSecurityDao = null;
	
	public MockVmManagerImpl() {
	}
	
	@Override
    public boolean configure(String name, Map<String, Object> params)
            throws ConfigurationException {
	    
	    return true;
	}
	
    public String startVM(String vmName, NicTO[] nics,
        	int cpuHz, long ramSize,
        	String bootArgs, String hostGuid) {

        MockHost host = _mockHostDao.findByGuid(hostGuid);
        if (host == null) {
            return "can't find host";
        }
        
        MockVm vm = _mockVmDao.findByVmName(vmName);
        if(vm == null) {
            int vncPort = 0;
            if(vncPort < 0)
                return "Unable to allocate VNC port";
            vm = new MockVMVO();
            vm.setCpu(cpuHz);
            vm.setMemory(ramSize);
            vm.setState(State.Running);
            vm.setName(vmName);
            vm.setVncPort(vncPort);
            vm.setHostId(host.getId());
            vm = _mockVmDao.persist((MockVMVO)vm);
        } else {
            if(vm.getState() == State.Stopped) {
                vm.setState(State.Running);
                _mockVmDao.update(vm.getId(), (MockVMVO)vm);
            }
        }

        if (vm.getState() == State.Running && vmName.startsWith("s-")) {
            String prvIp = null;
            String prvMac = null;
            String prvNetMask = null;

            for (NicTO nic : nics) {
                if (nic.getType() == TrafficType.Management) {
                    prvIp = nic.getIp();
                    prvMac = nic.getMac();
                    prvNetMask = nic.getNetmask();
                }
            }
            long dcId = 0;
            long podId = 0;
            String name = null;
            String vmType = null;
            String url = null;
            String[] args = bootArgs.trim().split(" ");
            for (String arg : args) {
                String[] params = arg.split("=");
                if (params.length < 1) {
                    continue;
                }

                if (params[0].equalsIgnoreCase("zone")) {
                    dcId = Long.parseLong(params[1]);
                } else if (params[0].equalsIgnoreCase("name")) {
                    name = params[1];
                } else if (params[0].equalsIgnoreCase("type")) {
                    vmType = params[1];
                } else if (params[0].equalsIgnoreCase("url")) {
                    url = params[1];
                } else if (params[0].equalsIgnoreCase("pod")) {
                    podId = Long.parseLong(params[1]);
                }
            }

            _mockAgentMgr.handleSystemVMStart(vm.getId(), prvIp, prvMac, prvNetMask, dcId, podId, name, vmType, url);
        }

        return null;
    }

	public boolean rebootVM(String vmName) {
	    MockVm vm = _mockVmDao.findByVmName(vmName);
	    if(vm != null) {
	        vm.setState(State.Running);
	        _mockVmDao.update(vm.getId(), (MockVMVO)vm);
	    }
	    return true;
	}
	
	@Override
    public boolean migrate(String vmName, String params) {
		MockVm vm = _mockVmDao.findByVmName(vmName);
		if(vm != null) {
		    vm.setState(State.Stopped);
		    _mockVmDao.remove(vm.getId());
		    return true;
		}

		return false;
	}
	
	@Override
    public Map<String, State> getVmStates(String hostGuid) {
		Map<String, State> states = new HashMap<String, State>();
		List<MockVMVO> vms = _mockVmDao.findByHostGuid(hostGuid);
		if (vms.isEmpty()) {
		    return states;
		}

		for(MockVm vm : vms) {
		    states.put(vm.getName(), vm.getState());
		}

		return states;
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
        return this.getClass().getSimpleName();
    }	
    
    @Override
    public Answer getVmStats(GetVmStatsCommand cmd) {
        HashMap<String, VmStatsEntry> vmStatsNameMap = new HashMap<String, VmStatsEntry>();
        List<String> vmNames = cmd.getVmNames();
        for (String vmName : vmNames) {
            VmStatsEntry entry = new VmStatsEntry(0, 0, 0, 0, "vm");
            entry.setNetworkReadKBs(32768); // default values 256 KBps
            entry.setNetworkWriteKBs(16384);
            entry.setCPUUtilization(10);
            entry.setNumCPUs(1);
            vmStatsNameMap.put(vmName, entry);
        }
        return new GetVmStatsAnswer(cmd, vmStatsNameMap);
    }

    @Override
    public CheckVirtualMachineAnswer checkVmState(CheckVirtualMachineCommand cmd, String hostGuid) {
        MockVMVO vm = _mockVmDao.findByVmNameAndHost(cmd.getVmName(), hostGuid);
        if (vm == null) {
            return new CheckVirtualMachineAnswer(cmd, "can't find vm:" + cmd.getVmName());
        }
        
        return new CheckVirtualMachineAnswer(cmd, vm.getState(), vm.getVncPort());
    }

    @Override
    public Answer startVM(StartCommand cmd, String hostGuid) {
        VirtualMachineTO vm = cmd.getVirtualMachine();
        String result = startVM(vm.getName(), vm.getNics(), vm.getCpus()* vm.getSpeed(), vm.getMaxRam(), vm.getBootArgs(), hostGuid);
        if (result != null) {
            return new StartAnswer(cmd, result);
        } else {
            return new StartAnswer(cmd);
        }
    }

    @Override
    public CheckSshAnswer checkSshCommand(CheckSshCommand cmd) {
        return new CheckSshAnswer(cmd);
    }

    @Override
    public Answer SetStaticNatRules(SetStaticNatRulesCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public Answer SetPortForwardingRules(SetPortForwardingRulesCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public NetworkUsageAnswer getNetworkUsage(NetworkUsageCommand cmd) {
        return new NetworkUsageAnswer(cmd, null, 100L, 100L);
    }

    @Override
    public MigrateAnswer Migrate(MigrateCommand cmd, String hostGuid) {
        String vmName = cmd.getVmName();
        String destGuid = cmd.getHostGuid();
        MockVMVO vm = _mockVmDao.findByVmNameAndHost(vmName, hostGuid);
        if (vm == null) {
            return new MigrateAnswer(cmd, false, "can;t find vm:" + vmName + " on host:" + hostGuid, null);
        }
        
        MockHost destHost = _mockHostDao.findByGuid(destGuid);
        if (destHost == null) {
            return new MigrateAnswer(cmd, false, "can;t find host:" + hostGuid, null);
        }
        vm.setHostId(destHost.getId());
        _mockVmDao.update(vm.getId(), vm);
        return new MigrateAnswer(cmd, true,null, 0);
    }

    @Override
    public Answer IpAssoc(IpAssocCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public Answer LoadBalancerConfig(LoadBalancerConfigCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public Answer AddDhcpEntry(DhcpEntryCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public Answer setVmData(VmDataCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public Answer CleanupNetworkRules(CleanupNetworkRulesCmd cmd, String hostGuid) {
        List<MockSecurityRulesVO> rules = _mockSecurityDao.findByHost(hostGuid);
        for (MockSecurityRulesVO rule : rules) {
            MockVMVO vm = _mockVmDao.findByVmNameAndHost(rule.getVmName(), hostGuid);
            if (vm == null) {
                _mockSecurityDao.remove(rule.getId());
            }
        }
        return new Answer(cmd);
    }

    @Override
    public Answer stopVM(StopCommand cmd) {
        String vmName = cmd.getVmName();
        MockVm vm = _mockVmDao.findByVmName(vmName);
        if(vm != null) {
            vm.setState(State.Stopped);
            _mockVmDao.update(vm.getId(), (MockVMVO)vm);
        }

        if (vmName.startsWith("s-")) {
            _mockAgentMgr.handleSystemVMStop(vm.getId());
        }

        return new StopAnswer(cmd, null, new Integer(0), new Long(100), new Long(200));
    }

    @Override
    public Answer rebootVM(RebootCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public Answer getVncPort(GetVncPortCommand cmd) {
          return new GetVncPortAnswer(cmd, 0);
    }

    @Override
    public Answer CheckConsoleProxyLoad(CheckConsoleProxyLoadCommand cmd) {
        return Answer.createUnsupportedCommandAnswer(cmd);
    }

    @Override
    public Answer WatchConsoleProxyLoad(WatchConsoleProxyLoadCommand cmd) {
        return Answer.createUnsupportedCommandAnswer(cmd);
    }

    @Override
    public SecurityIngressRuleAnswer AddSecurityIngressRules(SecurityIngressRulesCmd cmd) {
        MockVMVO vm = _mockVmDao.findByVmName(cmd.getVmName());
        if (vm == null) {
            return new SecurityIngressRuleAnswer(cmd, false, "cant' find the vm: " + cmd.getVmName());
        }
        boolean update = logSecurityGroupAction(cmd);
         MockSecurityRulesVO rules = _mockSecurityDao.findByVmId(cmd.getVmId());
        if (rules == null) {
            rules = new MockSecurityRulesVO();
            rules.setRuleSet(cmd.stringifyRules());
            rules.setSeqNum(cmd.getSeqNum());
            rules.setSignature(cmd.getSignature());
            rules.setVmId(vm.getId());
            rules.setHostId(vm.getHostId());

            _mockSecurityDao.persist(rules);
        } else if (update){
            rules.setSeqNum(cmd.getSeqNum());
            rules.setSignature(cmd.getSignature());
            rules.setRuleSet(cmd.stringifyRules());
            rules.setVmId(cmd.getVmId());
            rules.setHostId(vm.getHostId());
            _mockSecurityDao.update(rules.getId(), rules);
        }
        
        return new SecurityIngressRuleAnswer(cmd);
    }
    
    private boolean logSecurityGroupAction(SecurityIngressRulesCmd cmd) {
        String action = ", do nothing";
        String reason = ", reason=";
        MockSecurityRulesVO rule = _mockSecurityDao.findByVmId(cmd.getVmId());
        Long currSeqnum = rule == null? null: rule.getSeqNum();
        String currSig = rule == null? null: rule.getSignature();
        boolean updateSeqnoAndSig = false;
        if (currSeqnum != null) {
            if (cmd.getSeqNum() > currSeqnum) {
                s_logger.info("New seqno received: " + cmd.getSeqNum() + " curr=" + currSeqnum);
                updateSeqnoAndSig = true;
                if (!cmd.getSignature().equals(currSig)) {
                    s_logger.info("New seqno received: " + cmd.getSeqNum() + " curr=" + currSeqnum 
                            + " new signature received:" + cmd.getSignature()  + " curr=" + currSig + ", updated iptables");
                    action = ", updated iptables";
                    reason = reason + "seqno_increased_sig_changed";
                } else {
                    s_logger.info("New seqno received: " + cmd.getSeqNum() + " curr=" + currSeqnum 
                            + " no change in signature:" + cmd.getSignature() +  ", do nothing"); 
                    reason = reason + "seqno_increased_sig_same";
                }
            } else if (cmd.getSeqNum() < currSeqnum) {
                s_logger.info("Older seqno received: " + cmd.getSeqNum() + " curr=" + currSeqnum + ", do nothing");
                reason = reason + "seqno_decreased";
            } else {
                if (!cmd.getSignature().equals(currSig)) {
                    s_logger.info("Identical seqno received: " + cmd.getSeqNum()   
                            + " new signature received:" + cmd.getSignature()  + " curr=" + currSig + ", updated iptables");
                    action = ", updated iptables";
                    reason = reason + "seqno_same_sig_changed";
                    updateSeqnoAndSig = true;
                } else {
                    s_logger.info("Identical seqno received: " + cmd.getSeqNum() + " curr=" + currSeqnum 
                            + " no change in signature:" + cmd.getSignature() +  ", do nothing"); 
                    reason = reason + "seqno_same_sig_same";
                }
            }
        } else {
            s_logger.info("New seqno received: " + cmd.getSeqNum() + " old=null");
            updateSeqnoAndSig = true;
            action = ", updated iptables";
            reason = ", seqno_new";
        }
        s_logger.info("Programmed network rules for vm " + cmd.getVmName() + " seqno=" + cmd.getSeqNum() 
                + " signature=" + cmd.getSignature() 
                + " guestIp=" + cmd.getGuestIp() + ", numrules="
                + cmd.getRuleSet().length + " total cidrs=" + cmd.getTotalNumCidrs() + action + reason);
        return updateSeqnoAndSig;
    }

    @Override
    public Answer SavePassword(SavePasswordCommand cmd) {
        return new Answer(cmd); 
    }
    
    @Override
    public HashMap<String, Pair<Long, Long>> syncNetworkGroups(String hostGuid) {
        HashMap<String, Pair<Long, Long>> maps = new HashMap<String, Pair<Long, Long>>();
        List<MockSecurityRulesVO> rules = _mockSecurityDao.findByHost(hostGuid);
        for (MockSecurityRulesVO rule : rules) {
            maps.put(rule.getVmName(), new Pair<Long, Long>(rule.getVmId(), rule.getSeqNum()));
        }
        return maps;
    }
	
}
