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
import com.cloud.agent.api.Command;
import com.cloud.agent.api.GetHostStatsAnswer;
import com.cloud.agent.api.GetHostStatsCommand;
import com.cloud.agent.api.ModifySshKeysCommand;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingRoutingCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.agent.api.StartupStorageCommand;
import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.agent.api.storage.PrimaryStorageDownloadCommand;
import com.cloud.host.Host.Type;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Networks.RouterPrivateIpStrategy;
import com.cloud.resource.ServerResource;
import com.cloud.resource.ServerResourceBase;
import com.cloud.storage.Storage;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.VirtualMachine.State;

/**
 * Pretends to be a computing resource
 * 
 */
@Local(value={ServerResource.class})
public class FakeComputingResource extends ServerResourceBase implements ServerResource {
    private static final Logger s_logger = Logger.getLogger(FakeComputingResource.class);
    private Map<String, Object> _params;
    

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
                                
            /*} else if (cmd instanceof StopCommand) {
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
               return execute((CleanupNetworkRulesCmd)cmd);*/
            } else {
                s_logger.warn("Unsupported command ");
                return Answer.createUnsupportedCommandAnswer(cmd);
            }
        } catch (final IllegalArgumentException e) {
            return new Answer(cmd, false, e.getMessage());
        }
    }

    private Answer execute(PrimaryStorageDownloadCommand cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    private Answer execute(ModifySshKeysCommand cmd) {
        return new Answer(cmd, true, null);
    }

    @Override
    protected String getDefaultScriptsDir() {
        return null;
    }
    
    private Answer execute(ReadyCommand cmd) {
        return new ReadyAnswer(cmd);
    }
    
    protected Answer execute(GetHostStatsCommand cmd) {
       
        return new GetHostStatsAnswer(cmd, 0.0d, 8000, 16000, 0, 0,
                "SimulatedHost");
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
        long ram = getConfiguredProperty("memory", 16000L);
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
        return true;
    }

    public void setParams(Map<String, Object> _params) {
        this._params = _params;
    }

    public Map<String, Object> getParams() {
        return _params;
    }

}
