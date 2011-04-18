/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
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

package com.cloud.agent.resource.computing;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.AttachIsoCommand;
import com.cloud.agent.api.AttachVolumeCommand;
import com.cloud.agent.api.CheckHealthAnswer;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.agent.api.CheckStateAnswer;
import com.cloud.agent.api.CheckStateCommand;
import com.cloud.agent.api.CheckVirtualMachineAnswer;
import com.cloud.agent.api.CheckVirtualMachineCommand;
import com.cloud.agent.api.CleanupNetworkRulesCmd;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.GetHostStatsAnswer;
import com.cloud.agent.api.GetHostStatsCommand;
import com.cloud.agent.api.GetStorageStatsAnswer;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.agent.api.GetVmStatsAnswer;
import com.cloud.agent.api.GetVmStatsCommand;
import com.cloud.agent.api.ModifySshKeysCommand;
import com.cloud.agent.api.ModifyStoragePoolAnswer;
import com.cloud.agent.api.ModifyStoragePoolCommand;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingRoutingCommand;
import com.cloud.agent.api.PingTestCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.RebootAnswer;
import com.cloud.agent.api.RebootCommand;
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
import com.cloud.agent.api.routing.VmDataCommand;
import com.cloud.agent.api.storage.CreateAnswer;
import com.cloud.agent.api.storage.CreateCommand;
import com.cloud.agent.api.storage.DestroyCommand;
import com.cloud.agent.api.storage.PrimaryStorageDownloadAnswer;
import com.cloud.agent.api.storage.PrimaryStorageDownloadCommand;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.agent.dhcp.DhcpSnooper;
import com.cloud.agent.dhcp.FakeDhcpSnooper;
import com.cloud.agent.mockvm.MockVm;
import com.cloud.agent.mockvm.MockVmMgr;
import com.cloud.agent.mockvm.VmMgr;
import com.cloud.host.Host.Type;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Networks.RouterPrivateIpStrategy;
import com.cloud.network.Networks.TrafficType;
import com.cloud.resource.ServerResource;
import com.cloud.resource.ServerResourceBase;
import com.cloud.storage.Storage;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.Volume;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.template.TemplateInfo;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;
import com.cloud.vm.VirtualMachine.State;

/**
 * Pretends to be a computing resource
 * 
 */
@Local(value={ServerResource.class})
public class FakeComputingResource extends ServerResourceBase implements ServerResource {
    private static final Logger s_logger = Logger.getLogger(FakeComputingResource.class);
    private Map<String, Object> _params;
    private VmMgr _vmManager = new MockVmMgr();
    protected HashMap<String, State> _vms = new HashMap<String, State>(20);
    protected DhcpSnooper _dhcpSnooper = new FakeDhcpSnooper();


    @Override
    public Type getType() {
        return Type.Routing;
    }

    @Override
    public StartupCommand[] initialize() {
        Map<String, State> changes = null;

 
        final List<Object> info = getHostInfo();

        final StartupRoutingCommand cmd = new StartupRoutingCommand((Integer)info.get(0), (Long)info.get(1), (Long)info.get(2), (Long)info.get(4), (String)info.get(3), HypervisorType.KVM, RouterPrivateIpStrategy.HostLocal, changes);
        fillNetworkInformation(cmd);
        cmd.getHostDetails().putAll(getVersionStrings());
        cmd.setCluster(getConfiguredProperty("cluster", "1"));
        StoragePoolInfo pi = initializeLocalStorage();
        StartupStorageCommand sscmd = new StartupStorageCommand();
        sscmd.setPoolInfo(pi);
        sscmd.setGuid(pi.getUuid());
        sscmd.setDataCenter((String)_params.get("zone"));
        sscmd.setResourceType(Storage.StorageResourceType.STORAGE_POOL);

        return new StartupCommand[]{cmd, sscmd};
       
    }

    private Map<String, String> getVersionStrings() {
        Map<String, String> result = new HashMap<String, String>();
        String hostOs = (String) _params.get("Host.OS");
        String hostOsVer = (String) _params.get("Host.OS.Version");
        String hostOsKernVer = (String) _params.get("Host.OS.Kernel.Version");
        result.put("Host.OS", hostOs==null?"Fedora":hostOs); 
        result.put("Host.OS.Version", hostOsVer==null?"14":hostOsVer); 
        result.put("Host.OS.Kernel.Version", hostOsKernVer==null?"2.6.35.6-45.fc14.x86_64":hostOsKernVer);
        return result;
    }

    protected void fillNetworkInformation(final StartupCommand cmd) {

        cmd.setPrivateIpAddress((String)_params.get("private.ip.address"));
        cmd.setPrivateMacAddress((String)_params.get("private.mac.address"));
        cmd.setPrivateNetmask((String)_params.get("private.ip.netmask"));
        
        cmd.setStorageIpAddress((String)_params.get("private.ip.address"));
        cmd.setStorageMacAddress((String)_params.get("private.mac.address"));
        cmd.setStorageNetmask((String)_params.get("private.ip.netmask"));
        cmd.setGatewayIpAddress((String)_params.get("gateway.ip.address"));

    }
    
    protected  StoragePoolInfo initializeLocalStorage() {
        String hostIp = (String)_params.get("private.ip.address");
        String localStoragePath = (String)_params.get("local.storage.path");
        String lh = hostIp + localStoragePath;
        String uuid = UUID.nameUUIDFromBytes(lh.getBytes()).toString();
        
        String capacity = (String)_params.get("local.storage.capacity");
        String available = (String)_params.get("local.storage.avail");

        return new StoragePoolInfo(uuid, hostIp, localStoragePath, 
                                   localStoragePath, StoragePoolType.Filesystem, 
                                   Long.parseLong(capacity), Long.parseLong(available));

    }
    
    @Override
    public PingCommand getCurrentStatus(long id) {
        final HashMap<String, State> newStates = new HashMap<String, State>();
        _dhcpSnooper.syncIpAddr();
        return new PingRoutingCommand(com.cloud.host.Host.Type.Routing, id, newStates);
    }

    @Override
    public Answer executeRequest(Command cmd) {
        try {
            if (cmd instanceof ReadyCommand) {
                return execute((ReadyCommand)cmd);
            }else if (cmd instanceof ModifySshKeysCommand) {
                    return execute((ModifySshKeysCommand)cmd);//TODO: remove
            } else if (cmd instanceof GetHostStatsCommand) {
                        return execute((GetHostStatsCommand)cmd);
            } else if (cmd instanceof PrimaryStorageDownloadCommand) {
                return execute((PrimaryStorageDownloadCommand) cmd);
                                
            } else if (cmd instanceof StopCommand) {
                return execute((StopCommand)cmd);
            } else if (cmd instanceof GetVmStatsCommand) {
                return execute((GetVmStatsCommand)cmd);
            } else if (cmd instanceof RebootCommand) {
                return execute((RebootCommand)cmd);
            }  else if (cmd instanceof CheckStateCommand) {
                return executeRequest(cmd);
            } else if (cmd instanceof CheckHealthCommand) {
                return execute((CheckHealthCommand)cmd);
            } else if (cmd instanceof PingTestCommand) {
                return execute((PingTestCommand)cmd);
            } else if (cmd instanceof CheckVirtualMachineCommand) {
                return execute((CheckVirtualMachineCommand)cmd);
            } else if (cmd instanceof ReadyCommand) {
                return execute((ReadyCommand)cmd);
            } else if (cmd instanceof StopCommand) {
                return execute((StopCommand)cmd);
            } else if (cmd instanceof CreateCommand) {
                return execute((CreateCommand) cmd);
            } else if (cmd instanceof DestroyCommand) {
                return execute((DestroyCommand) cmd);
            } else if (cmd instanceof PrimaryStorageDownloadCommand) {
                return execute((PrimaryStorageDownloadCommand) cmd);
            } else if (cmd instanceof GetStorageStatsCommand) {
                return execute((GetStorageStatsCommand) cmd);
            }  else if (cmd instanceof ModifyStoragePoolCommand) {
                return execute((ModifyStoragePoolCommand) cmd);
            } else if (cmd instanceof SecurityIngressRulesCmd) {
                return execute((SecurityIngressRulesCmd) cmd);
            }  else if (cmd instanceof StartCommand ) {
                return execute((StartCommand) cmd);
            } else if (cmd instanceof CleanupNetworkRulesCmd) {
               return execute((CleanupNetworkRulesCmd)cmd);
            } else {
                s_logger.warn("Unsupported command ");
                return Answer.createUnsupportedCommandAnswer(cmd);
            }
        } catch (final IllegalArgumentException e) {
            return new Answer(cmd, false, e.getMessage());
        }
    }

    private Answer execute(CleanupNetworkRulesCmd cmd) {
        return new Answer(cmd);
    }

    private Answer execute(SecurityIngressRulesCmd cmd) {
        s_logger.info("Programmed network rules for vm " + cmd.getVmName() + " guestIp=" + cmd.getGuestIp() + ", numrules=" + cmd.getRuleSet().length);
        return new SecurityIngressRuleAnswer(cmd);
    }

    private Answer execute(ModifyStoragePoolCommand cmd) {
        long capacity = getConfiguredProperty("local.storage.capacity", 10000000000L);
        long used = 10000000L;
        long available = capacity - used;
        if (cmd.getAdd()) {

            ModifyStoragePoolAnswer answer = new ModifyStoragePoolAnswer(cmd,
                    capacity, used, new HashMap<String, TemplateInfo>());

            if (s_logger.isInfoEnabled())
                s_logger
                        .info("Sending ModifyStoragePoolCommand answer with capacity: "
                                + capacity
                                + ", used: "
                                + used
                                + ", available: " + available);
            return answer;
        } else {
            if (s_logger.isInfoEnabled())
                s_logger
                        .info("ModifyNetfsStoragePoolCmd is not add command, cmd: "
                                + cmd.toString());
            return new Answer(cmd);
        }
    }

    private Answer execute(GetStorageStatsCommand cmd) {
        return new GetStorageStatsAnswer(cmd, getConfiguredProperty("local.storage.capacity", 100000000000L), 0L);
    }

    protected synchronized ReadyAnswer execute(ReadyCommand cmd) {
        return new ReadyAnswer(cmd);
    }
    
    private Answer execute(PrimaryStorageDownloadCommand cmd) {
        return new PrimaryStorageDownloadAnswer(cmd.getLocalPath(), 16000000L);
    }

    private Answer execute(ModifySshKeysCommand cmd) {
        return new Answer(cmd, true, null);
    }

    @Override
    protected String getDefaultScriptsDir() {
        return null;
    }
    
    
    protected String getConfiguredProperty(String key, String defaultValue) {
        String val = (String)_params.get(key);
        return val==null?defaultValue:val;
    }
    
    protected Long getConfiguredProperty(String key, Long defaultValue) {
        String val = (String)_params.get(key);
        
        if (val != null) {
            Long result = Long.parseLong(val);
            return result;
        }
        return defaultValue;
    }
    
    protected List<Object> getHostInfo() {
        final ArrayList<Object> info = new ArrayList<Object>();
        long speed = getConfiguredProperty("cpuspeed", 4000L) ;
        long cpus = getConfiguredProperty("cpus", 4L);
        long ram = getConfiguredProperty("memory", 16000L*1024L*1024L);
        long dom0ram = Math.min(ram/10, 768*1024*1024L);


        String cap = getConfiguredProperty("capabilities", "hvm");
        info.add((int)cpus);
        info.add(speed);
        info.add(ram);
        info.add(cap);
        info.add(dom0ram);        
        return info;
        
    }
    private Map<String, Object> getSimulatorProperties() throws ConfigurationException {
        final File file = PropertiesUtil.findConfigFile("simulator.properties");
        if (file == null) {
            throw new ConfigurationException("Unable to find simulator.properties.");
        }

        s_logger.info("simulator.properties found at " + file.getAbsolutePath());
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(file));

            
            final Map<String, Object> params = PropertiesUtil.toMap(properties);

           
            return params;
        } catch (final FileNotFoundException ex) {
            throw new CloudRuntimeException("Cannot find the file: " + file.getAbsolutePath(), ex);
        } catch (final IOException ex) {
            throw new CloudRuntimeException("IOException in reading " + file.getAbsolutePath(), ex);
        }
    }
       

    
    @Override
    public boolean configure(String name, Map<String, Object> params)
            throws ConfigurationException {
        Map<String, Object> simProps = getSimulatorProperties();
        params.putAll(simProps);
        setParams(params);
        _vmManager.configure(params);
        _dhcpSnooper.configure(name, params);
        return true;
    }

    public void setParams(Map<String, Object> _params) {
        this._params = _params;
    }

    public Map<String, Object> getParams() {
        return _params;
    }

    
    protected synchronized StartAnswer execute(StartCommand cmd) {
        VmMgr vmMgr = getVmManager();

        VirtualMachineTO vmSpec = cmd.getVirtualMachine();
        String vmName = vmSpec.getName();
        State state = State.Stopped;

        try {
            if (!_vms.containsKey(vmName)) {
                synchronized (_vms) {
                    _vms.put(vmName, State.Starting);
                }

                MockVm vm = vmMgr.createVmFromSpec(vmSpec);
                vmMgr.createVbd(vmSpec, vmName, vm);
                vmMgr.createVif(vmSpec, vmName, vm);
           
                state = State.Running;
                for (NicTO nic: cmd.getVirtualMachine().getNics()) {
                    if (nic.getType() == TrafficType.Guest) {
                        InetAddress addr = _dhcpSnooper.getIPAddr(nic.getMac(), vmName);
                        nic.setIp(addr.getHostAddress());
                    }
                }
                return new StartAnswer(cmd);
            } else {
                String msg = "There is already a VM having the same name "
                        + vmName;
                s_logger.warn(msg);
                return new StartAnswer(cmd, msg);
            }
        } catch (Exception ex) {

        } finally {
            synchronized (_vms) {
                _vms.put(vmName, state);
            }
        }
        return new StartAnswer(cmd);
    }

    protected synchronized StopAnswer execute(StopCommand cmd) {
        VmMgr vmMgr = getVmManager();

        StopAnswer answer = null;
        String vmName = cmd.getVmName();

        Integer port = vmMgr.getVncPort(vmName);
        Long bytesReceived = null;
        Long bytesSent = null;

       

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


            String result2 = vmMgr.cleanupVnet(cmd.getVnet());
            if (result2 != null) {
                result = result2 + (result != null ? ("\n" + result) : "");
                answer = new StopAnswer(cmd, result, port, bytesSent,
                        bytesReceived);
            }
          
            _dhcpSnooper.cleanup(vmName, null);

            return answer;
        } finally {
            if (answer == null || !answer.getResult()) {
                synchronized (_vms) {
                    _vms.put(vmName, state);
                }
            }
        }
    }

    protected Answer execute(final VmDataCommand cmd) {
        return new Answer(cmd);
    }
    
    protected Answer execute(RebootCommand cmd) {
        VmMgr vmMgr = getVmManager();
        vmMgr.rebootVM(cmd.getVmName());
        return new RebootAnswer(cmd, "success", 0L, 0L);
    }

    private Answer execute(PingTestCommand cmd) {
        return new Answer(cmd);
    }
    
    protected GetVmStatsAnswer execute(GetVmStatsCommand cmd) {
        return null;
    }
    
    private VmMgr getVmManager() {
        return _vmManager;
    }
    
    protected Answer execute(GetHostStatsCommand cmd) {
        VmMgr vmMgr =  getVmManager();
        return new GetHostStatsAnswer(cmd, vmMgr.getHostCpuUtilization(), vmMgr
                .getHostFreeMemory(), vmMgr.getHostTotalMemory(), 0, 0,
                "SimulatedHost");
    }

    protected CheckStateAnswer execute(CheckStateCommand cmd) {
        State state = getVmManager().checkVmState(cmd.getVmName());
        return new CheckStateAnswer(cmd, state);
    }

    protected CheckHealthAnswer execute(CheckHealthCommand cmd) {
        return new CheckHealthAnswer(cmd, true);
    }

    
    protected CheckVirtualMachineAnswer execute(
            final CheckVirtualMachineCommand cmd) {
        VmMgr vmMgr = getVmManager();
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


    protected Answer execute(final AttachVolumeCommand cmd) {
        return new Answer(cmd);
    }

    protected Answer execute(final AttachIsoCommand cmd) {
        return new Answer(cmd);
    }

    protected CreateAnswer execute(final CreateCommand cmd) {
        try {

            VolumeTO vol = new VolumeTO(cmd.getVolumeId(),
                    Volume.Type.ROOT,
                    Storage.StorageResourceType.STORAGE_POOL,
                    com.cloud.storage.Storage.StoragePoolType.LVM, cmd
                            .getPool().getUuid(), "dummy", "/mountpoint",
                    "dummyPath", 1000L, null);
            return new CreateAnswer(cmd, vol);
        } catch (Throwable th) {
            return new CreateAnswer(cmd, new Exception("Unexpected exception"));
        }
    }
    


    protected HashMap<String, State> sync() {
        Map<String, State> newStates;
        Map<String, State> oldStates = null;

        HashMap<String, State> changes = new HashMap<String, State>();

        synchronized (_vms) {
            newStates = getVmManager().getVmStates();
            oldStates = new HashMap<String, State>(_vms.size());
            oldStates.putAll(_vms);

            for (Map.Entry<String, State> entry : newStates.entrySet()) {
                String vm = entry.getKey();

                State newState = entry.getValue();
                State oldState = oldStates.remove(vm);

                if (s_logger.isTraceEnabled()) {
                    s_logger
                            .trace("VM "
                                    + vm
                                    + ": xen has state "
                                    + newState
                                    + " and we have state "
                                    + (oldState != null ? oldState.toString()
                                            : "null"));
                }

                if (oldState == null) {
                    _vms.put(vm, newState);
                    changes.put(vm, newState);
                } else if (oldState == State.Starting) {
                    if (newState == State.Running) {
                        _vms.put(vm, newState);
                    } else if (newState == State.Stopped) {
                        s_logger.debug("Ignoring vm " + vm
                                + " because of a lag in starting the vm.");
                    }
                } else if (oldState == State.Stopping) {
                    if (newState == State.Stopped) {
                        _vms.put(vm, newState);
                    } else if (newState == State.Running) {
                        s_logger.debug("Ignoring vm " + vm
                                + " because of a lag in stopping the vm. ");
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
                    s_logger.trace("VM " + vm
                            + " is now missing from xen so reporting stopped");
                }

                if (oldState == State.Stopping) {
                    s_logger.debug("Ignoring VM " + vm
                            + " in transition state stopping.");
                    _vms.remove(vm);
                } else if (oldState == State.Starting) {
                    s_logger.debug("Ignoring VM " + vm
                            + " in transition state starting.");
                } else if (oldState == State.Stopped) {
                    _vms.remove(vm);
                } else {
                    changes.put(entry.getKey(), State.Stopped);
                }
            }
        }

        return changes;
    }
    
    protected Answer execute(DestroyCommand cmd) {
        return new Answer(cmd, true, null);
    }
}
