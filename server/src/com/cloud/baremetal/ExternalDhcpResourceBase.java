/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.baremetal;

import java.util.HashMap;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.IAgentControl;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingRoutingCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupExternalDhcpCommand;
import com.cloud.agent.api.StartupPxeServerCommand;
import com.cloud.host.Host.Type;
import com.cloud.resource.ServerResource;
import com.cloud.utils.script.Script;
import com.cloud.utils.ssh.SSHCmdHelper;
import com.cloud.vm.VirtualMachine.State;
import com.trilead.ssh2.SCPClient;

public class ExternalDhcpResourceBase implements ServerResource {
	private static final Logger s_logger = Logger.getLogger(ExternalDhcpResourceBase.class);
	String _name;
	String _guid;
	String _username;
	String _password;
	String _ip;
	String _zoneId;
	String _podId;
	String _gateway;
	String _dns;
	
	@Override
	public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
		_name = name;
		_guid = (String)params.get("guid");
		_ip = (String)params.get("ip");
		_username = (String)params.get("username");
		_password = (String)params.get("password");
		_zoneId = (String)params.get("zone");
		_podId = (String)params.get("pod");
		_gateway = (String)params.get("gateway");
		_dns = (String)params.get("dns");
		
		if (_guid == null) {
			throw new ConfigurationException("No Guid specified");
		}
		
		if (_zoneId == null) {
			throw new ConfigurationException("No Zone specified");
		}
		
		if (_podId == null) {
			throw new ConfigurationException("No Pod specified");
		}
		
		if (_ip == null) {
			throw new ConfigurationException("No IP specified");
		}
		
		if (_username == null) {
			throw new ConfigurationException("No username specified");
		}
		
		if (_password == null) {
			throw new ConfigurationException("No password specified");
		}
		
		if (_gateway == null) {
			throw new ConfigurationException("No gateway specified");
		}
		
		if (_dns == null) {
			throw new ConfigurationException("No dns specified");
		}
		
		return true;
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
	public Type getType() {
		return Type.ExternalDhcp;
	}

	@Override
	public StartupCommand[] initialize() {
		StartupExternalDhcpCommand cmd = new StartupExternalDhcpCommand();
		cmd.setName(_name);
		cmd.setDataCenter(_zoneId);
		cmd.setPod(_podId);
		cmd.setPrivateIpAddress(_ip);
		cmd.setStorageIpAddress("");
		cmd.setVersion("");
		cmd.setGuid(_guid);
		return new StartupCommand[]{cmd};
	}

	@Override
	public PingCommand getCurrentStatus(long id) {
		//TODO: check server
		return new PingRoutingCommand(getType(), id, new HashMap<String, State>());
	}

	protected ReadyAnswer execute(ReadyCommand cmd) {
		s_logger.debug("External DHCP resource " + _name + " is ready");
		return new ReadyAnswer(cmd);
	}
	
	@Override
	public Answer executeRequest(Command cmd) {
		if (cmd instanceof ReadyCommand) {
			return execute((ReadyCommand) cmd);
		} else {
			return Answer.createUnsupportedCommandAnswer(cmd);
		}
	}

	@Override
	public void disconnected() {
	}

	@Override
	public IAgentControl getAgentControl() {
		return null;
	}

	@Override
	public void setAgentControl(IAgentControl agentControl) {
	}

}
