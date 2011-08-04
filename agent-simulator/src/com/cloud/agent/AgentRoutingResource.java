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

package com.cloud.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.AttachIsoCommand;
import com.cloud.agent.api.AttachVolumeCommand;
import com.cloud.agent.api.BackupSnapshotCommand;
import com.cloud.agent.api.CheckHealthAnswer;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.agent.api.CheckStateAnswer;
import com.cloud.agent.api.CheckStateCommand;
import com.cloud.agent.api.CheckVirtualMachineAnswer;
import com.cloud.agent.api.CheckVirtualMachineCommand;
import com.cloud.agent.api.CleanupNetworkRulesCmd;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.CreateStoragePoolCommand;
import com.cloud.agent.api.CreateVolumeFromSnapshotCommand;
import com.cloud.agent.api.DeleteSnapshotBackupCommand;
import com.cloud.agent.api.DeleteStoragePoolCommand;
import com.cloud.agent.api.GetHostStatsAnswer;
import com.cloud.agent.api.GetHostStatsCommand;
import com.cloud.agent.api.GetStorageStatsAnswer;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.agent.api.GetVmStatsAnswer;
import com.cloud.agent.api.GetVmStatsCommand;
import com.cloud.agent.api.GetVncPortCommand;
import com.cloud.agent.api.HostStatsEntry;
import com.cloud.agent.api.ManageSnapshotCommand;
import com.cloud.agent.api.MigrateAnswer;
import com.cloud.agent.api.MigrateCommand;
import com.cloud.agent.api.ModifyStoragePoolAnswer;
import com.cloud.agent.api.ModifyStoragePoolCommand;
import com.cloud.agent.api.NetworkRulesSystemVmCommand;
import com.cloud.agent.api.NetworkUsageAnswer;
import com.cloud.agent.api.NetworkUsageCommand;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingRoutingCommand;
import com.cloud.agent.api.PingTestCommand;
import com.cloud.agent.api.PrepareForMigrationAnswer;
import com.cloud.agent.api.PrepareForMigrationCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.RebootAnswer;
import com.cloud.agent.api.RebootCommand;
import com.cloud.agent.api.RebootRouterCommand;
import com.cloud.agent.api.SecurityIngressRuleAnswer;
import com.cloud.agent.api.SecurityIngressRulesCmd;
import com.cloud.agent.api.StartAnswer;
import com.cloud.agent.api.StartCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.agent.api.StartupStorageCommand;
import com.cloud.agent.api.StopAnswer;
import com.cloud.agent.api.StopCommand;
import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.agent.api.VmStatsEntry;
import com.cloud.agent.api.check.CheckSshAnswer;
import com.cloud.agent.api.check.CheckSshCommand;
import com.cloud.agent.api.proxy.CheckConsoleProxyLoadCommand;
import com.cloud.agent.api.proxy.ConsoleProxyLoadAnswer;
import com.cloud.agent.api.proxy.WatchConsoleProxyLoadCommand;
import com.cloud.agent.api.routing.DhcpEntryCommand;
import com.cloud.agent.api.routing.IpAssocCommand;
import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.routing.SavePasswordCommand;
import com.cloud.agent.api.routing.SetPortForwardingRulesCommand;
import com.cloud.agent.api.routing.SetStaticNatRulesCommand;
import com.cloud.agent.api.routing.VmDataCommand;
import com.cloud.agent.api.storage.CreateAnswer;
import com.cloud.agent.api.storage.CreateCommand;
import com.cloud.agent.api.storage.CreatePrivateTemplateCommand;
import com.cloud.agent.api.storage.DestroyAnswer;
import com.cloud.agent.api.storage.DestroyCommand;
import com.cloud.agent.api.storage.PrimaryStorageDownloadAnswer;
import com.cloud.agent.api.storage.PrimaryStorageDownloadCommand;
import com.cloud.agent.api.storage.UpgradeDiskCommand;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.agent.manager.SimulatorManager;
import com.cloud.agent.manager.SimulatorManager.AgentType;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Networks.RouterPrivateIpStrategy;
import com.cloud.storage.Volume;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.Storage.StorageResourceType;
import com.cloud.storage.template.TemplateInfo;
import com.cloud.utils.script.Script;
import com.cloud.vm.VirtualMachine.State;

public class AgentRoutingResource extends AgentStorageResource {
    private static final Logger s_logger = Logger.getLogger(AgentRoutingResource.class);

    private static final int DEFAULT_DOM0_MEM_MB = 128;
    private final Random rand = new Random();

    protected Map<String, State> _vms = new HashMap<String, State>(20);
    protected String _mountParent;

    public AgentRoutingResource(long instanceId, AgentType agentType, SimulatorManager simMgr) {
        super(instanceId, agentType, simMgr);
    }

    public AgentRoutingResource() {
        setType(Host.Type.Routing);
    }

    @Override
    public Answer executeRequest(Command cmd) {

        try {
            if (cmd instanceof StartCommand) {
                return execute((StartCommand) cmd);
            } else if (cmd instanceof SetPortForwardingRulesCommand) {
                return execute((SetPortForwardingRulesCommand) cmd);
            } else if (cmd instanceof SetStaticNatRulesCommand) {
                return execute((SetStaticNatRulesCommand) cmd);
            } else if (cmd instanceof StopCommand) {
                return execute((StopCommand) cmd);
            } else if (cmd instanceof VmDataCommand) {
                return execute((VmDataCommand) cmd);
            } else if (cmd instanceof GetVmStatsCommand) {
                return execute((GetVmStatsCommand) cmd);
            } else if (cmd instanceof GetStorageStatsCommand) {
            	return execute((GetStorageStatsCommand) cmd);
            }  else if (cmd instanceof RebootRouterCommand) {
                return execute((RebootRouterCommand) cmd);
            } else if (cmd instanceof RebootCommand) {
                return execute((RebootCommand) cmd);
            } else if (cmd instanceof GetHostStatsCommand) {
                return execute((GetHostStatsCommand) cmd);
            } else if (cmd instanceof PingTestCommand) {
                return execute((PingTestCommand) cmd);
            } else if (cmd instanceof PrepareForMigrationCommand) {
                return execute((PrepareForMigrationCommand) cmd);
            } else if (cmd instanceof MigrateCommand) {
                return execute((MigrateCommand) cmd);
            } else if (cmd instanceof CheckVirtualMachineCommand) {
                return execute((CheckVirtualMachineCommand) cmd);
            } else if (cmd instanceof AttachVolumeCommand) {
                return execute((AttachVolumeCommand) cmd);
            } else if (cmd instanceof AttachIsoCommand) {
                return execute((AttachIsoCommand) cmd);
            } else if (cmd instanceof PrimaryStorageDownloadCommand) {
                return execute((PrimaryStorageDownloadCommand) cmd);
            } else if (cmd instanceof CreateStoragePoolCommand) {
                return execute((CreateStoragePoolCommand) cmd);
            } else if (cmd instanceof ModifyStoragePoolCommand) {
    			return execute((ModifyStoragePoolCommand) cmd);
    		} else if (cmd instanceof DeleteStoragePoolCommand) {
    			return execute((DeleteStoragePoolCommand) cmd);
    		} else if (cmd instanceof GetVncPortCommand) {
                return execute((GetVncPortCommand) cmd);
            } else if (cmd instanceof LoadBalancerConfigCommand) {
                return execute((LoadBalancerConfigCommand) cmd);
            } else if (cmd instanceof IpAssocCommand) {
                return execute((IpAssocCommand) cmd);
            } else if (cmd instanceof CheckConsoleProxyLoadCommand) {
                return execute((CheckConsoleProxyLoadCommand) cmd);
            } else if (cmd instanceof WatchConsoleProxyLoadCommand) {
                return execute((WatchConsoleProxyLoadCommand) cmd);
            } else if (cmd instanceof NetworkUsageCommand) {
                return execute((NetworkUsageCommand) cmd);
            } else if (cmd instanceof SavePasswordCommand) {
                return execute((SavePasswordCommand) cmd);
            } else if (cmd instanceof RebootRouterCommand) {
                return execute((RebootRouterCommand) cmd);
            } else if (cmd instanceof CheckSshCommand) {
                return execute((CheckSshCommand) cmd);
            } else if (cmd instanceof DhcpEntryCommand) {
                return execute((DhcpEntryCommand) cmd);
            } else if (cmd instanceof CreateCommand) {
                return execute((CreateCommand) cmd);
            } else if (cmd instanceof DestroyCommand) {
                return execute((DestroyCommand) cmd);
            } else if (cmd instanceof PrimaryStorageDownloadCommand) {
                return execute((PrimaryStorageDownloadCommand) cmd);
            } else if (cmd instanceof CheckStateCommand) {
                return execute((CheckStateCommand) cmd);
            } else if (cmd instanceof CheckHealthCommand) {
                return execute((CheckHealthCommand) cmd);
            } else if (cmd instanceof UpgradeDiskCommand) {
                return execute((UpgradeDiskCommand) cmd);
            } else if (cmd instanceof CreatePrivateTemplateCommand) {
                return execute((CreatePrivateTemplateCommand) cmd);
            } else if (cmd instanceof ManageSnapshotCommand) {
                return execute((ManageSnapshotCommand) cmd);
            } else if (cmd instanceof BackupSnapshotCommand) {
                return execute((BackupSnapshotCommand) cmd);
            } else if (cmd instanceof DeleteSnapshotBackupCommand) {
                return execute((DeleteSnapshotBackupCommand) cmd);
            } else if (cmd instanceof CreateVolumeFromSnapshotCommand) {
                return execute((CreateVolumeFromSnapshotCommand) cmd);
            } else if (cmd instanceof ReadyCommand) {
                return execute((ReadyCommand) cmd);
            } else if (cmd instanceof CleanupNetworkRulesCmd) {
                return execute((CleanupNetworkRulesCmd) cmd);
            } else if (cmd instanceof SecurityIngressRulesCmd) {
            	return execute((SecurityIngressRulesCmd) cmd);
            } else if (cmd instanceof NetworkRulesSystemVmCommand) {
            	return execute((NetworkRulesSystemVmCommand) cmd);
            }
             else {
                return Answer.createUnsupportedCommandAnswer(cmd);
            }
        } catch (IllegalArgumentException e) {
            return new Answer(cmd, false, e.getMessage());
        }
    }


	@Override
    public Type getType() {
        return Host.Type.Routing;
    }

    @Override
    public PingCommand getCurrentStatus(long id) {
        HashMap<String, State> newStates = sync();
        return new PingRoutingCommand(Host.Type.Routing, id, newStates);
    }

    @Override
    public StartupCommand[] initialize() {
        synchronized (_vms) {
            _vms.clear();
        }
        Map<String, State> changes = sync();
        List<Object> info = getHostInfo();

        StartupRoutingCommand cmd = new StartupRoutingCommand((Integer) info.get(0), (Long) info.get(1), (Long) info.get(2), (Long) info.get(4), (String) info.get(3), HypervisorType.Simulator,
                changes);

        Map<String, String> hostDetails = new HashMap<String, String>();
        hostDetails.put(RouterPrivateIpStrategy.class.getCanonicalName(), RouterPrivateIpStrategy.DcGlobal.toString());

        cmd.setHostDetails(hostDetails);
        cmd.setAgentTag("agent-simulator");
        cmd.setPrivateIpAddress(getHostPrivateIp());
        cmd.setPrivateNetmask("255.255.0.0");
        cmd.setPrivateMacAddress(getHostMacAddress().toString());
        cmd.setStorageIpAddress(getHostStoragePrivateIp());
        cmd.setStorageNetmask("255.255.0.0");
        cmd.setStorageMacAddress(getHostStorageMacAddress().toString());
        cmd.setStorageIpAddressDeux(getHostStoragePrivateIp2());
        cmd.setStorageNetmaskDeux("255.255.0.0");
        cmd.setStorageMacAddressDeux(getHostStorageMacAddress2().toString());

        cmd.setName(getName());
        assert getGuid() != null : "How is guid null?";
        cmd.setGuid(getGuid());
        cmd.setVersion(getVersion());
        cmd.setAgentTag("agent-simulator");
        cmd.setDataCenter(Long.toString(getZone()));
        cmd.setIqn(getHostIqn());
        cmd.setPod(getPod());
        cmd.setCluster(getCluster());

        StartupStorageCommand ssCmd = initializeLocalSR();
        getSimulatorManager().saveResourceState(null, this);
        
        int randomConnectTime = getSimulatorManager().randomizeWaitDelay(
        		getSimulatorManager().getDelayStart(),
        		getSimulatorManager().getDelayEnd());
        if (getName() != null && s_logger.isDebugEnabled()) {
        	s_logger.info(getName() + " initializing in " + randomConnectTime
        			+ "s");
        }			
        return new StartupCommand[] { cmd, ssCmd };
    }

    private StartupStorageCommand initializeLocalSR() {
        StorageMgr storageMgr = getStorageMgr();

        Map<String, TemplateInfo> tInfo = new HashMap<String, TemplateInfo>();
        populateTemplateStartupInfo(tInfo);

        StoragePoolInfo poolInfo = new StoragePoolInfo(getGuid(), getHostPrivateIp(), "/host/path", "/local/path", com.cloud.storage.Storage.StoragePoolType.LVM, storageMgr.getTotalSize(),
                storageMgr.getTotalSize() - storageMgr.getUsedSize());

        StartupStorageCommand cmd = new StartupStorageCommand(_parent + File.pathSeparator + getGuid(), com.cloud.storage.Storage.StoragePoolType.LVM, getStorageMgr().getTotalSize(), tInfo);

        cmd.setPoolInfo(poolInfo);
        cmd.setGuid(getGuid());
        cmd.setResourceType(StorageResourceType.STORAGE_POOL);
        return cmd;
    }
    
    protected Answer execute(CreateStoragePoolCommand cmd) {
    	return new Answer(cmd, true, "success");	
    }  
    
    protected Answer execute(ModifyStoragePoolCommand cmd) {
        if (cmd.getAdd()) {

            long capacity = getStorageMgr().getTotalSize();
            long used = getStorageMgr().getUsedSize();
            long available = capacity - used;
            Map<String, TemplateInfo> tInfo = new HashMap<String, TemplateInfo>();
            populateTemplateStartupInfo(tInfo);

            ModifyStoragePoolAnswer answer = new ModifyStoragePoolAnswer(cmd, capacity, available, tInfo);

            if (s_logger.isInfoEnabled()) {
                s_logger.info("Sending ModifyNetfsStoragePoolCommand answer with capacity: " + capacity + ", used: " + used + ", available: " + available);
            }
            return answer;
        } else {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("ModifyNetfsStoragePoolCmd is not add command, cmd: " + cmd.toString());
            }
            return new Answer(cmd);
        }
    }

    public Answer execute(DeleteStoragePoolCommand cmd) {
        return new Answer(cmd);
    }

	private Answer execute(SecurityIngressRulesCmd cmd) {
		s_logger.info("Programmed network rules for vm " + cmd.getVmName()
				+ " guestIp=" + cmd.getGuestIp() + ", numrules="
				+ cmd.getRuleSet().length);
		return new SecurityIngressRuleAnswer(cmd);
	}
	
	private Answer execute(NetworkRulesSystemVmCommand cmd) {
		return new Answer(cmd, true, "");
	}
	
	protected synchronized Answer execute(StartCommand cmd)
			throws IllegalArgumentException {
		VmMgr vmMgr = getVmMgr();

		VirtualMachineTO vmSpec = cmd.getVirtualMachine();
		String vmName = vmSpec.getName();

		String result = null;
		String local = _mountParent + vmName;

		State state = State.Stopped;
		synchronized (_vms) {
			_vms.put(vmName, State.Starting);
		}

		try {
			result = vmMgr.startVM(cmd.getVirtualMachine().getName(), vmSpec.getNics(), vmSpec.getCpus(), vmSpec.getSpeed(), vmSpec.getMaxRam(),
					local, vmSpec.getVncPassword(), getHostFreeMemory());
			if (result != null) {
				vmMgr.cleanupVM(vmName, local);
				return new StartAnswer(cmd, result);
			}

			_collector.addVM(cmd.getVirtualMachine().getName());
			_collector.submitMetricsJobs();

			state = State.Running;
		} finally {
			synchronized (_vms) {
				_vms.put(vmName, state);
				getSimulatorManager().saveResourceState(null, this);
			}
		}
		int delay = getSimulatorManager().randomizeWaitDelay(
				getSimulatorManager().getDelayStart(),
				getSimulatorManager().getDelayEnd());
		if(s_logger.isDebugEnabled()) {
			s_logger.debug("VM "+cmd.getVirtualMachine().getName()+" starting in " + delay + "s");			
		}
		return new StartAnswer(cmd);
	}

	public CheckSshAnswer execute(CheckSshCommand cmd) {
		String vmName = cmd.getName();
		String privateIp = cmd.getIp();
		int cmdPort = cmd.getPort();
		
		if (s_logger.isDebugEnabled()) {
			s_logger.debug("Ping command port, " + privateIp + ":" + cmdPort);
		}
		
		if (s_logger.isDebugEnabled()) {
			s_logger.debug("Ping command port succeeded for vm " + vmName);
		}
		
		return new CheckSshAnswer(cmd);
	}
	
	protected synchronized StopAnswer execute(StopCommand cmd) {
		VmMgr vmMgr = getVmMgr();

		StopAnswer answer = null;
		String vmName = cmd.getVmName();

		Integer port = vmMgr.getVncPort(vmName);
		Long bytesReceived = null;
		Long bytesSent = null;

		Map<String, MockVmMetrics> map = _collector.getMetricsMap();
		MockVmMetrics metrics = map.get(cmd.getVmName());
		if (metrics != null) {
			Map<String, Long> mapRx = metrics.getNetRxTotalBytes();
			bytesReceived = mapRx.get("eth0");
			Map<String, Long> mapTx = metrics.getNetTxTotalBytes();
			bytesSent = mapTx.get("eth0");
		}

		State state = null;
		synchronized (_vms) {
			state = _vms.get(vmName);
			_vms.put(vmName, State.Stopping);
		}
		try {
			String result = vmMgr.stopVM(vmName, false);
			if (result != null) {
				s_logger.info("Trying destroy on " + vmName);
				if (result == Script.ERR_TIMEOUT) {
					result = vmMgr.stopVM(vmName, true);
				}

				s_logger.warn("Couldn't stop " + vmName);

				if (result != null) {
					return new StopAnswer(cmd, result);
				}
			}

			answer = new StopAnswer(cmd, null, port, bytesSent, bytesReceived);
			String local = _mountParent + vmName;
			result = unmountImage(local, vmName);
			
			if (result != null) {
				answer = new StopAnswer(cmd, result, port, bytesSent,
						bytesReceived);
			}

			String result2 = vmMgr.cleanupVnet(cmd.getVnet());
			if (result2 != null) {
				result = result2 + (result != null ? ("\n" + result) : "");
				answer = new StopAnswer(cmd, result, port, bytesSent,
						bytesReceived);
			}

			_collector.removeVM(vmName);
			state = State.Stopped;
		} finally {
			synchronized (_vms) {
				_vms.put(vmName, state);
				getSimulatorManager()
						.saveResourceState(null, this);
			}
		}
		int delay = getSimulatorManager().randomizeWaitDelay(
				getSimulatorManager().getDelayStart(),
				getSimulatorManager().getDelayEnd());
		if(s_logger.isDebugEnabled()) {
			s_logger.debug("VM " + cmd.getVmName() + " stopping in " + delay + "s");			
		}
		return answer;
	}

	protected Answer execute(CleanupNetworkRulesCmd cmd) {
		s_logger.info("Cleaned up rules for " + getVmMgr().getCurrentVMs() + " vms on host "
				+ getHostPrivateIp());
		return new Answer(cmd, true, getVmMgr().getCurrentVMs().toString());
	}
	
    protected Answer execute(final VmDataCommand cmd) {
        return new Answer(cmd);
    }
    
	protected Answer execute(PrimaryStorageDownloadCommand cmd) {
		int delay = getSimulatorManager().randomizeWaitDelay(
				getSimulatorManager().getDelayStart(),
				getSimulatorManager().getDelayEnd());
		if (s_logger.isInfoEnabled()) {
			s_logger.info("Downloading template "
					+ cmd.getSecondaryStorageUrl() + " to pool "
					+ cmd.getPoolId() + " in " + delay + "s");
		}
		String installPath = cmd.getUrl().replace(
				cmd.getSecondaryStorageUrl() + File.separator, "");
		PrimaryStorageDownloadAnswer answer = new PrimaryStorageDownloadAnswer(
				installPath, 200 * 1024 * 1024L);
		return answer;
	}

    protected Answer execute(RebootRouterCommand cmd) {
		return execute((RebootCommand)cmd);
    }
    
    protected Answer execute(RebootCommand cmd) {
        VmMgr vmMgr = getVmMgr();
        vmMgr.rebootVM(cmd.getVmName());
        int delay = getSimulatorManager().randomizeWaitDelay(
        		getSimulatorManager().getDelayStart(),
        		getSimulatorManager().getDelayEnd());
        if(s_logger.isDebugEnabled()) {
        	s_logger.debug("VM "+cmd.getVmName()+" rebooting in " + delay + "s");			
        }
        return new RebootAnswer(cmd, "success", 0L, 0L);
    }

    protected GetVmStatsAnswer execute(GetVmStatsCommand cmd) {
        List<String> vmNames = cmd.getVmNames();
        HashMap<String, VmStatsEntry> vmStatsNameMap = new HashMap<String, VmStatsEntry>();
        if (vmNames.size() == 0) {
            return new GetVmStatsAnswer(cmd, vmStatsNameMap);
        }
        waitDelayMillis(vmNames.size()); //millisecond delay, more the vms running on the host, more the delay
        for (String vmName : vmNames) {
			MockVm vm = getVmMgr().getVm(vmName);
			VmStatsEntry entry = new VmStatsEntry(0, 0, 0, 0, "vm");
			entry.setNetworkReadKBs(32768); // default values 256 KBps
			entry.setNetworkWriteKBs(16384);
			entry.setCPUUtilization(vm.getUtilization());
			entry.setNumCPUs(vm.getCpuCount());
			vmStatsNameMap.put(vmName, entry);
        }
        return new GetVmStatsAnswer(cmd, vmStatsNameMap);
    }

	protected GetStorageStatsAnswer execute(GetStorageStatsCommand cmd) {
		long size = getStorageMgr().getUsedSize();
		return size != -1 ? new GetStorageStatsAnswer(cmd, getStorageMgr()
				.getTotalSize(), size) : new GetStorageStatsAnswer(cmd,
				"Unable to get storage stats");
	}

    protected Answer execute(GetHostStatsCommand cmd) {
        HostStatsEntry hostStats = new HostStatsEntry();
        hostStats.setTotalMemoryKBs(getHostTotalMemory());
        hostStats.setFreeMemoryKBs(getHostFreeMemory());
        hostStats.setNetworkReadKBs(32768);
        hostStats.setNetworkWriteKBs(16384);
        hostStats.setCpuUtilization(getHostCpuUtilization());
        hostStats.setEntityType("simulator-host");
        hostStats.setHostId(cmd.getHostId());

        return new GetHostStatsAnswer(cmd, hostStats);
    }

    @Override
    protected CheckStateAnswer execute(CheckStateCommand cmd) {
        State state = getVmMgr().checkVmState(cmd.getVmName());
        return new CheckStateAnswer(cmd, state);
    }

    @Override
    protected CheckHealthAnswer execute(CheckHealthCommand cmd) {
        return new CheckHealthAnswer(cmd, true);
    }

    protected Answer execute(PingTestCommand cmd) {
        if (s_logger.isInfoEnabled())
            s_logger.info("Excuting PingTestCommand. agent instance id : " + getInstanceId());

        String result = null;
        String computingHostIp = cmd.getComputingHostIp();

        if (computingHostIp != null) {
            if (s_logger.isInfoEnabled())
                s_logger.info("Ping host : " + computingHostIp + ", agent instance id : " + getInstanceId());
            result = doPingTest(computingHostIp);
        } else {
            if (s_logger.isInfoEnabled())
                s_logger.info("Ping user router. router IP: " + cmd.getRouterIp() + ", router private IP: " + cmd.getPrivateIp() + ", agent instance id : " + getInstanceId());

            result = doPingTest(cmd.getRouterIp(), cmd.getPrivateIp());
        }

        if (result != null) {
            return new Answer(cmd, false, result);
        }
        return new Answer(cmd);
    }

    private Answer execute(SetStaticNatRulesCommand cmd) {
        return new Answer(cmd);
    }

    private Answer execute(SetPortForwardingRulesCommand cmd) {
        return new Answer(cmd);
    }

    private String doPingTest(String computingHostIp) {
        return null;
    }

    private String doPingTest(String domRIp, String vmIp) {
        return null;
    }

    protected PrepareForMigrationAnswer execute(final PrepareForMigrationCommand cmd) {
        if (s_logger.isDebugEnabled())
            s_logger.debug("Handle PrepareForMigrationCommand, host: " + getHostPrivateIp() + ", vm: " + cmd.getVirtualMachine().getName());

        return new PrepareForMigrationAnswer(cmd);
    }

    private Answer execute(NetworkUsageCommand cmd) {
    	waitDelay(3); //few seconds for ssh-login and xapi call
        
    	long bytesReceived = 0;
    	long bytesSent = 0;
    	if(s_logger.isDebugEnabled()) {
    		s_logger.debug("Collecting metrics for router: " + cmd.getDomRName() + " with private ip: " + cmd.getPrivateIP());
    	}
    	Map<String, MockVmMetrics> map = _collector.getMetricsMap();
    	
    	for(String vmName : getVmMgr().getCurrentVMs()) {
    		if(vmName.equalsIgnoreCase(cmd.getDomRName())) {
                MockVmMetrics metrics = map.get(vmName);
                if(metrics != null) {
                	bytesReceived = metrics.getNetRxTotalBytes("eth0");
                	bytesSent = metrics.getNetTxTotalBytes("eth0");                	
                }    			
    		}
    	}
        return new NetworkUsageAnswer(cmd, "Router: " + cmd.getDomRName() + " Sent/Received: " + bytesSent + "/" + bytesReceived + " bytes", bytesSent, bytesReceived);
    }


    protected MigrateAnswer execute(final MigrateCommand cmd) {
        VmMgr vmMgr = getVmMgr();

        // SimulatorMigrateVmCmd simulatorCmd = new SimulatorMigrateVmCmd(
        // simulator.getTestCase());

        if (s_logger.isDebugEnabled())
            s_logger.debug("Handle MigrationCommand, host: " + getHostPrivateIp() + ", vm: " + cmd.getVmName() + ", destIP: " + cmd.getDestinationIp());

        MockVm vm = vmMgr.getVm(cmd.getVmName());
        if (vm != null) {
            if (vmMgr.migrate(cmd.getVmName(), cmd.getDestinationIp())) {
                // simulatorCmd.setVmName(cmd.getVmName());
                // simulatorCmd.setDestIp(cmd.getDestinationIp());
                // simulatorCmd.setCpuCount(vm.getCpuCount());
                // simulatorCmd.setRamSize(vm.getRamSize());
                // simulatorCmd.setUtilization(vm.getUtilization());
                // AgentSimulator.getInstance().castSimulatorCmd(simulatorCmd);
                return new MigrateAnswer(cmd, true, null, null);
            }
        }

        return new MigrateAnswer(cmd, false, "VM " + cmd.getVmName() + " is no longer running", null);
    }

    protected CheckVirtualMachineAnswer execute(final CheckVirtualMachineCommand cmd) {
        VmMgr vmMgr = getVmMgr();
        final String vmName = cmd.getVmName();

        final State state = vmMgr.checkVmState(vmName);
        Integer vncPort = null;
        if (state == State.Running) {
            vncPort = vmMgr.getVncPort(vmName);
            synchronized (_vms) {
                _vms.put(vmName, State.Running);
            }
        }
        return new CheckVirtualMachineAnswer(cmd, state, vncPort);
    }

    @Override
    protected synchronized ReadyAnswer execute(ReadyCommand cmd) {
        return new ReadyAnswer(cmd);
    }

    protected Answer execute(final AttachVolumeCommand cmd) {
    	waitDelay(3);
        return new Answer(cmd);
    }

    protected Answer execute(final AttachIsoCommand cmd) {
    	waitDelay(3);
        return new Answer(cmd);
    }

	protected Answer execute(CreateCommand cmd) {
		try {
			VolumeTO vol = null;
			StorageFilerTO pool = cmd.getPool();
			if (cmd.getDiskCharacteristics().getType() == Volume.Type.ROOT) {
				getStorageMgr().create(cmd.getTemplateUrl(), "", pool.getPath(),
						8 * 1024 * 1024 * 1024L);
				vol = new VolumeTO(cmd.getVolumeId(), cmd
						.getDiskCharacteristics().getType(),
						pool.getType(), pool.getUuid(), "dummy", pool.getPath(),
						"/dummy/path", 8 * 1024 * 1024 * 1024L, null);
			} else {
				getStorageMgr().create(cmd.getTemplateUrl(), "", pool.getPath(),
						cmd.getDiskCharacteristics().getSize());
				vol = new VolumeTO(cmd.getVolumeId(), cmd
						.getDiskCharacteristics().getType(),
						pool.getType(), pool.getUuid(), "dummy", pool.getPath(),
						"/dummy/path", cmd.getDiskCharacteristics().getSize(),
						null);
			}
			return new CreateAnswer(cmd, vol);
		} catch (Throwable th) {
			return new CreateAnswer(cmd, new Exception(
					"Unexpected exception when creating volume"));
		}
	}

    @Override
    protected Answer execute(DestroyCommand cmd) {
    	getVmMgr().cleanupVM(cmd.getVmName(), null);
    	return new DestroyAnswer(cmd, true, "success");
    }

    public synchronized String mountImage(String storageHosts[], String dest, String vmName, List<VolumeVO> volumes, boolean mirroredVols) {
        if (!mirroredVols) {
            return mountImage(storageHosts[0], dest, vmName, volumes);
        } else {
            return mountMirroredImage(storageHosts, dest, vmName, volumes);
        }
    }

    public synchronized String mountImage(String host, String dest, String vmName, List<VolumeVO> volumes) {
        return null;
    }

    protected String mountMirroredImage(String hosts[], String dest, String vmName, List<VolumeVO> volumes) {
        return null;
    }

    protected synchronized String unmountImage(String path, String vmName) {
        if (s_logger.isInfoEnabled())
            s_logger.info("unmountMirroredImage for vm : " + vmName + ", path :" + path);
        return null;
    }

    protected List<Object> getHostInfo() {
        ArrayList<Object> info = new ArrayList<Object>();
        long speed = getHostCpuSpeed();
        long cpus = getHostCpuCount();
        long ram = getHostTotalMemory();
        long dom0Ram = getHostDom0Memory();

        // make sure we add hvm support into caps, host allocator will check it
        StringBuilder caps = new StringBuilder();
        caps.append("hvm");

        info.add((int) cpus);
        info.add(speed);
        info.add(ram);
        info.add(caps.toString());
        info.add(dom0Ram);

        return info;
    }

    protected HashMap<String, State> sync() {
        Map<String, State> newStates;
        Map<String, State> oldStates = null;

        HashMap<String, State> changes = new HashMap<String, State>();

        synchronized (_vms) {
            newStates = getVmMgr().getVmStates();
            oldStates = new HashMap<String, State>(_vms.size());
            oldStates.putAll(_vms);

            for (Map.Entry<String, State> entry : newStates.entrySet()) {
                String vm = entry.getKey();

                State newState = entry.getValue();
                State oldState = oldStates.remove(vm);

                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("VM " + vm + ": has state " + newState + " and we have state " + (oldState != null ? oldState.toString() : "null"));
                }

                if (oldState == null) {
                    _vms.put(vm, newState);
                    changes.put(vm, newState);
                } else if (oldState == State.Starting) {
                    if (newState == State.Running) {
                        _vms.put(vm, newState);
                    } else if (newState == State.Stopped) {
                        s_logger.debug("Ignoring vm " + vm + " because of a lag in starting the vm.");
                    }
                } else if (oldState == State.Stopping) {
                    if (newState == State.Stopped) {
                        _vms.put(vm, newState);
                    } else if (newState == State.Running) {
                        s_logger.debug("Ignoring vm " + vm + " because of a lag in stopping the vm. ");
                    }
                } else if (oldState != newState) {
                    _vms.put(vm, newState);
                    changes.put(vm, newState);
                }
            }

            for (Map.Entry<String, State> entry : oldStates.entrySet()) {
                String vm = entry.getKey();
                State oldState = entry.getValue();

                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("VM " + vm + " is now missing from simulator agent so reporting stopped");
                }

                if (oldState == State.Stopping) {
                    s_logger.debug("Ignoring VM " + vm + " in transition state stopping.");
                    _vms.remove(vm);
                } else if (oldState == State.Starting) {
                    s_logger.debug("Ignoring VM " + vm + " in transition state starting.");
                } else if (oldState == State.Stopped) {
                    _vms.remove(vm);
                } else {
                    changes.put(entry.getKey(), State.Stopped);
                }
            }
        }

        return changes;
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        if (!super.configure(name, params)) {
            s_logger.warn("Base class was unable to configure");
            return false;
        }

        _mountParent = (String) params.get("mount.parent");
        if (_mountParent == null) {
            _mountParent = "/images";
        }

        if (_mountParent.charAt(_mountParent.length() - 1) != File.separatorChar) {
            _mountParent += File.separatorChar;
        }

        s_logger.info("Parent directory for image mounts is: " + _mountParent);
        return true;
    }

    protected Answer execute(LoadBalancerConfigCommand cmd) {
        return new Answer(cmd, true, null);
    }

    protected Answer execute(IpAssocCommand cmd) {
        return new Answer(cmd);
    }

    protected Answer execute(CheckConsoleProxyLoadCommand cmd) {
        return executeProxyLoadScan(cmd, cmd.getProxyVmId(), cmd.getProxyVmName(), cmd.getProxyManagementIp(), cmd.getProxyCmdPort());
    }

    protected Answer execute(WatchConsoleProxyLoadCommand cmd) {
        return executeProxyLoadScan(cmd, cmd.getProxyVmId(), cmd.getProxyVmName(), cmd.getProxyManagementIp(), cmd.getProxyCmdPort());
    }

    private Answer executeProxyLoadScan(Command cmd, long proxyVmId, String proxyVmName, String proxyManagementIp, int cmdPort) {
        return new ConsoleProxyLoadAnswer(cmd, proxyVmId, proxyVmName, true, null);
    }

    protected Answer execute(SavePasswordCommand cmd) {
        return new Answer(cmd);
    }


    protected Answer execute(DhcpEntryCommand cmd) {
		waitDelay(2);
        return new Answer(cmd, true, null);
    }

    public double getHostCpuUtilization() {
        return rand.nextInt(20); // This is just a %age value. Should not matter for simulator
    }

    public int getHostCpuCount() {
        return getVmMgr().getHostCpuCores();
    }

    public long getHostCpuSpeed() {
        return getVmMgr().getHostCpuSpeedInMHz();
    }

    public long getHostTotalMemory() { // total memory in kilobytes
        return getVmMgr().getHostMemSizeInMB() * 1024 * 1024L;
    }

    public long getHostFreeMemory() { // free memory in kilobytes
        long memSize = getHostTotalMemory();
        memSize -= getHostDom0Memory();

        synchronized (this) {
            for (MockVm vm : getVmMgr().getMockVMs()) {
                if (vm.getState() != State.Stopped)
                    memSize -= vm.getRamSize();
            }
        }
        return memSize;
    }

    public double getHostAverageLoad() {
        return (getHostTotalMemory() - getHostFreeMemory()) / getHostTotalMemory();
    }

    public long getHostDom0Memory() { // memory size in kilobytes
        return DEFAULT_DOM0_MEM_MB * 1024 * 1024L;
    }

    private void waitDelay(int latencyInSeconds) {
    	try {
    		Thread.sleep(latencyInSeconds * 1000);
    	} catch (InterruptedException e) {
    		// Don't do anything
    	}
    }
    
    private void waitDelayMillis(int latencyInMilliSeconds) {
    	try {
    		Thread.sleep(latencyInMilliSeconds);
    	} catch (InterruptedException e) {
    		// Don't do anything
    	}
    }
}
