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

package com.cloud.hypervisor.kvm.resource;

import java.util.HashMap;
import java.util.Map;

import javax.naming.ConfigurationException;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.agent.api.StartupRoutingCommand.VmState;
import com.cloud.host.Host.Type;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.resource.ServerResource;
import com.cloud.resource.ServerResourceBase;
import com.cloud.vm.VirtualMachine.State;

public class KvmDummyResourceBase extends ServerResourceBase implements ServerResource {
	private String _zoneId;
	private String _podId;
	private String _clusterId;
	private String _guid;
	private String _agentIp;
	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StartupCommand[] initialize() {
		StartupRoutingCommand cmd = new StartupRoutingCommand(0, 0, 0, 0, null, Hypervisor.HypervisorType.KVM, new HashMap<String, String>(), new HashMap<String, VmState>());
		cmd.setDataCenter(_zoneId);
		cmd.setPod(_podId);
		cmd.setCluster(_clusterId);
		cmd.setGuid(_guid);
		cmd.setName(_agentIp);
		cmd.setPrivateIpAddress(_agentIp);
		cmd.setStorageIpAddress(_agentIp);
		cmd.setVersion(KvmDummyResourceBase.class.getPackage().getImplementationVersion());
		return new StartupCommand[] { cmd };
	}

	@Override
	public PingCommand getCurrentStatus(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Answer executeRequest(Command cmd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getDefaultScriptsDir() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
		_zoneId = (String)params.get("zone");
		_podId = (String)params.get("pod");
		_clusterId = (String)params.get("cluster");
		_guid = (String)params.get("guid");
		_agentIp = (String)params.get("agentIp");
		return true;
	}
}
