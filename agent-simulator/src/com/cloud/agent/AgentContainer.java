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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentSimulator.AgentType;
import com.cloud.agent.api.StartupCommand;
import com.cloud.resource.ServerResource;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.net.MacAddress;
import com.cloud.utils.net.NetUtils;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;

@Deprecated
public class AgentContainer extends Agent {
    private static final Logger s_logger = Logger.getLogger(AgentContainer.class);

    private final int instanceId;
    private final AgentType agentType;

    private final VmMgr vmMgr;
    private final StorageMgr storageMgr;
	private byte prefix = 0;
	
	public AgentContainer(IAgentShell shell, int instanceId, AgentType agentType) throws ConfigurationException {
		super(shell); //Defines the NioClient that talks to Management Server
		if(agentType == AgentType.Routing) {
			_resource = new AgentRoutingResource();			
		} else {
			_resource = new AgentStorageResource();
		}
		
		final Map<String, Object> params = PropertiesUtil.toMap(_shell.getProperties());
		_resource.configure(_resource.getClass().getSimpleName(), params);
		
		this.instanceId = instanceId;
		this.agentType = agentType;
		
		vmMgr = new MockVmMgr();
		storageMgr = new MockStorageMgr();
		
		InetAddress inetAddress = NetUtils.getLocalInetAddress();
		if(inetAddress != null) {
            byte[] ipBytes = inetAddress.getAddress();
            prefix = ipBytes[3];		// retrieve last byte of the IP address
		}
	}
	
	public AgentType getAgentType() {
		return agentType;
	}
	
	public int getInstanceId() {
		return instanceId;
	}
	
	public String composeHostName(String hostName) {
		return hostName + "-" + String.valueOf(AgentSimulator.getInstance().getRunId()) + "-" +
			String.valueOf(instanceId);
	}
	
	public String getHostIqn() {
		return "iqn:" + AgentSimulator.getInstance().getRunId() + "." +  getInstanceId();
	}
	
	public MacAddress getHostMacAddress() {
		long address = 0;
		
		address = (prefix & 0xff);
		address <<= 40;
		address |= ((long)(short)AgentSimulator.getInstance().getRunId()) << 32;
		address |= instanceId;
		return new MacAddress(address);
	}
	
	public String getHostPrivateIp() {
		int id = instanceId;
		id |= AgentSimulator.getInstance().getRunId() << 16;
		
		return "10." + String.valueOf((id >> 16) & 0xff) + "." +
			String.valueOf((id >> 8) & 0xff) + "." +
			String.valueOf(id & 0xff);
	}
	
	public MacAddress getHostStorageMacAddress() {
		long address = 0;
		
		address = (prefix & 0xff);
		address <<= 40;
		address |= ((long)(short)AgentSimulator.getInstance().getRunId()) << 32;
		address |= (instanceId | (1L << 31)) & 0xffffffff;
		return new MacAddress(address);
	}
	
	public MacAddress getHostStorageMacAddress2() {
		long address = 0;
		
		address = (prefix & 0xff);
		address <<= 40;
		address |= ((long)(short)AgentSimulator.getInstance().getRunId()) << 32;
		address |= (instanceId | (3L << 30)) & 0xffffffff;
		return new MacAddress(address);
	}
	
	public String getHostStoragePrivateIp() {
		int id = instanceId;
		id |= AgentSimulator.getInstance().getRunId() << 16;
		id |= 1 << 15;
		
		return "10." + String.valueOf((id >> 16) & 0xff) + "." +
			String.valueOf((id >> 8) & 0xff) + "." +
			String.valueOf(id & 0xff);
	}
	
	public String getHostStoragePrivateIp2() {
		int id = instanceId;
		id |= AgentSimulator.getInstance().getRunId() << 16;
		id |= 3 << 14;
		
		return "10." + String.valueOf((id >> 16) & 0xff) + "." +
			String.valueOf((id >> 8) & 0xff) + "." +
			String.valueOf((id) & 0xff);
	}
	
	public VmMgr getVmMgr() {
		return vmMgr;
	}
	
	public StorageMgr getStorageMgr() {
		return storageMgr;
	}

    protected void setupStartupCommand(StartupCommand startup) {
        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            s_logger.warn("unknown host? ", e);
            //ignore
            return;
        }
    	
        final Script command = new Script("hostname", 500, s_logger);
        final OutputInterpreter.OneLineParser parser = new OutputInterpreter.OneLineParser();
        final String result = command.execute(parser);
        final String hostname = result == null ? parser.getLine() : addr.toString();
    	
        startup.setId(getId());
        startup.setName(hostname);
        startup.setDataCenter(getZone());
        startup.setPod(getPod());
        startup.setGuid(getHostMacAddress().toString(":") + "-" + _resource.getClass().getSimpleName());
        startup.setVersion(getVersion());
        
        startup.setIqn(getHostIqn());
        startup.setPrivateIpAddress(getHostPrivateIp());
        startup.setPrivateMacAddress(getHostMacAddress().toString(":"));
        startup.setPrivateNetmask("255.0.0.0");
        
        startup.setStorageIpAddress(getHostStoragePrivateIp());
        startup.setStorageMacAddress(getHostStorageMacAddress().toString(":"));
        startup.setStorageNetmask("255.0.0.0");
        
        startup.setStorageIpAddressDeux(getHostStoragePrivateIp2());
        startup.setStorageMacAddressDeux(getHostStorageMacAddress2().toString(":"));
        startup.setStorageNetmaskDeux("255.0.0.0");
        startup.setAgentTag("vmops-simulator");
    }
}
