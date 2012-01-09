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
package com.cloud.agent.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ejb.Local;

import com.cloud.agent.IAgentControl;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.agent.api.StartupStorageCommand;
import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.agent.api.StartupRoutingCommand.VmState;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Networks.RouterPrivateIpStrategy;
import com.cloud.resource.ServerResource;
import com.cloud.storage.Storage;
import com.cloud.storage.Storage.StoragePoolType;

@Local(value={ServerResource.class})
public class DummyResource implements ServerResource {
    String _name;
    Host.Type _type;
    boolean _negative;
    IAgentControl _agentControl;
    private Map<String, Object> _params;

    @Override
    public void disconnected() {
    }
    
    @Override
    public Answer executeRequest(Command cmd) {
        System.out.println("Received Command: " + cmd.toString());
        Answer answer = new Answer(cmd, !_negative, "response");
        System.out.println("Replying with: " + answer.toString());
        return answer;
    }

    @Override
    public PingCommand getCurrentStatus(long id) {
        return new PingCommand(_type, id);
    }

    @Override
    public Type getType() {
        return _type;
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
    
    protected void fillNetworkInformation(final StartupCommand cmd) {

        cmd.setPrivateIpAddress((String)getConfiguredProperty("private.ip.address", "127.0.0.1"));
        cmd.setPrivateMacAddress((String)getConfiguredProperty("private.mac.address", "8A:D2:54:3F:7C:C3"));
        cmd.setPrivateNetmask((String)getConfiguredProperty("private.ip.netmask", "255.255.255.0"));
        
        cmd.setStorageIpAddress((String)getConfiguredProperty("private.ip.address", "127.0.0.1"));
        cmd.setStorageMacAddress((String)getConfiguredProperty("private.mac.address", "8A:D2:54:3F:7C:C3"));
        cmd.setStorageNetmask((String)getConfiguredProperty("private.ip.netmask", "255.255.255.0"));
        cmd.setGatewayIpAddress((String)getConfiguredProperty("gateway.ip.address", "127.0.0.1"));

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
    
    protected  StoragePoolInfo initializeLocalStorage() {
        String hostIp = (String)getConfiguredProperty("private.ip.address", "127.0.0.1");
        String localStoragePath = (String)getConfiguredProperty("local.storage.path", "/mnt");
        String lh = hostIp + localStoragePath;
        String uuid = UUID.nameUUIDFromBytes(lh.getBytes()).toString();
        
        String capacity = (String)getConfiguredProperty("local.storage.capacity", "1000000000");
        String available = (String)getConfiguredProperty("local.storage.avail", "10000000");

        return new StoragePoolInfo(uuid, hostIp, localStoragePath, 
                                   localStoragePath, StoragePoolType.Filesystem, 
                                   Long.parseLong(capacity), Long.parseLong(available));

    }
    
    @Override
    public StartupCommand[] initialize() {
    	   Map<String, VmState> changes = null;

    	   
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

    @Override
    public boolean configure(String name, Map<String, Object> params) {
        _name = name;
        
        String value = (String)params.get("type");
        _type = Host.Type.valueOf(value);
        
        value = (String)params.get("negative.reply");
        _negative = Boolean.parseBoolean(value);
        setParams(params);
        return true;
    }
    
    public void setParams(Map<String, Object> _params) {
        this._params = _params;
    }

    @Override
    public String getName() {
        return _name;
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
    public IAgentControl getAgentControl() {
    	return _agentControl;
    }
    
    @Override
    public void setAgentControl(IAgentControl agentControl) {
    	_agentControl = agentControl;
    }
}
