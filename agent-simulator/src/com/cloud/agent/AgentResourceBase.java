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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.manager.SimulatorManager;
import com.cloud.agent.manager.SimulatorManager.AgentType;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.resource.ServerResource;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.net.MacAddress;
import com.cloud.utils.net.NetUtils;

public class AgentResourceBase implements ServerResource {
	private static final Logger s_logger = Logger
			.getLogger(AgentResourceBase.class);

	private MockVmMgr _vmMgr = new MockVmMgr();
	private MockStorageMgr _storageMgr = new MockStorageMgr();
	protected String _name;
	private List<String> _warnings = new LinkedList<String>();
	private List<String> _errors = new LinkedList<String>();
	private byte _prefix = 0;
	protected transient MetricsCollector _collector;	
	private transient IAgentControl _agentControl;

	protected long _instanceId;
	private long _dcId;
	private String _host;
	private String _pod;
	private String _cluster;
	private String _version;
	private String _guid;
	
	private String _hostPrivateIp;
	private long _macAddress;

	private String _hostStoragePrivateIp;
	private long _storageMacAddress;
	
	private String _hostStoragePrivateIpDeux;
	private long _storageMacAddressDeux;
	private Type _type;
	private String _cidrPrefix;
	private transient ComponentLocator _locator = null;
	protected transient SimulatorManager _simMgr;
	

	public AgentResourceBase(long instanceId, AgentType agentType, SimulatorManager simMgr) {
		_instanceId = instanceId;		
		_guid = UUID.randomUUID().toString(); //in case of reconnect set explicitly
		_name = "SimulatedAgent."+_guid;
		
		assert _guid != null : "Why give out a null guid?";
		if(s_logger.isDebugEnabled()) {
			s_logger.info("New Routing host instantiated with guid:" + _guid);
		}
		_collector = new MetricsCollector(getVmMgr().getCurrentVMs());
		_simMgr = simMgr;
		_cidrPrefix = simMgr.getPodCidrPrefix();
		
		if (agentType == AgentType.Routing) {
			_type = Host.Type.Routing;
		} else {
			_type = Host.Type.Storage;
		}
		
		InetAddress inetAddress = NetUtils.getLocalInetAddress();
		if (inetAddress != null) {
			byte[] ipBytes = inetAddress.getAddress();
			_prefix = ipBytes[3]; //retrieve last byte of the IP address
		}
	}

	public AgentResourceBase() {
		if(s_logger.isDebugEnabled()) {
			s_logger.debug("Deserializing simulated agent on reconnect");
		}
		_collector = new MetricsCollector(getVmMgr().getCurrentVMs());
		_locator = ComponentLocator.getLocator("management-server");
		_simMgr = _locator.getManager(SimulatorManager.class);
	}

	@Override
	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}
	
	@Override
	public boolean configure(String name, Map<String, Object> params)
			throws ConfigurationException {
		if(!checkPoolForResource(name, params)) {
			if(s_logger.isDebugEnabled()) {
				s_logger.debug("Configuring new simulated host w/ name: " + name + " and guid: " + getGuid());
			}
			_name = name;
			try {
				setZone(Long.parseLong((String) params.get("zone")));
				setHost((String) params.get("url"));
				setPod((String) params.get("pod"));
				setCluster((String)params.get("cluster"));
				setVersion("2.2.8");
			} catch (NumberFormatException e) {
				throw new ConfigurationException("Parameter configuration error");
			}        
			return true;			
		} else {
			reconnect(name, params);
			return true; 
		}
	}
	

	private void reconnect(String name, Map<String, Object> params) {
		AgentResourceBase oldResource = getSimulatorManager().getResourceByName(name);
		
		assert oldResource.getGuid() != null : "Old resource: " + name + " without proper guid!Do not reconnect";
		setGuid(oldResource.getGuid());
		if(s_logger.isDebugEnabled()) {
			s_logger.debug("Reconfiguring existing simulated host w/ name: " + name + " and guid: " + getGuid());
		}
		setHostMacaddress(oldResource.getHostMacAddress().toLong());
		setHostPrivateIp(oldResource.getHostPrivateIp());
		setHostStorageMacAddress(oldResource.getHostStorageMacAddress().toLong());
		setHostStorageMacAddress2(oldResource.getHostStorageMacAddress2().toLong());
		setHostStoragePrivateIp(oldResource.getHostStoragePrivateIp());
		setHostStoragePrivateIp2(oldResource.getHostStoragePrivateIp2());

		setHost(oldResource.getHost());
		setCluster(oldResource.getCluster());
		setZone(oldResource.getZone());
		setPod(oldResource.getPod());
		setAgentControl(oldResource.getAgentControl());
		setName(name);
		setVersion("2.2.8");
		
		_prefix = oldResource._prefix;
		_instanceId = oldResource._instanceId;		
		_warnings = oldResource._warnings;
		_errors = oldResource._errors;
		_cidrPrefix = oldResource._cidrPrefix;
		
		setVmMgr(oldResource.getVmMgr());
		setStorageMgr(oldResource.getStorageMgr());		
	}

	private boolean checkPoolForResource(String name, Map<String, Object> params) {
		return getSimulatorManager().checkPoolForResource(name,
				params);
	}

	@Override
	public void disconnected() {
	}

	protected void recordWarning(String msg, Throwable th) {
		String str = getLogStr(msg, th);
		synchronized (_warnings) {
			_warnings.add(str);
		}
	}

	protected void recordWarning(String msg) {
		recordWarning(msg, null);
	}

	protected List<String> getWarnings() {
		synchronized (this) {
			List<String> results = _warnings;
			_warnings = new ArrayList<String>();
			return results;
		}
	}

	protected List<String> getErrors() {
		synchronized (this) {
			List<String> result = _errors;
			_errors = new ArrayList<String>();
			return result;
		}
	}

	protected void recordError(String msg, Throwable th) {
		String str = getLogStr(msg, th);
		synchronized (_errors) {
			_errors.add(str);
		}
	}

	protected void recordError(String msg) {
		recordError(msg, null);
	}

	protected Answer createErrorAnswer(Command cmd, String msg, Throwable th) {
		StringWriter writer = new StringWriter();
		if (msg != null) {
			writer.append(msg);
		}
		writer.append("===>Stack<===");
		th.printStackTrace(new PrintWriter(writer));
		return new Answer(cmd, false, writer.toString());
	}

	protected String createErrorDetail(String msg, Throwable th) {
		StringWriter writer = new StringWriter();
		if (msg != null) {
			writer.append(msg);
		}
		writer.append("===>Stack<===");
		th.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	protected String getLogStr(String msg, Throwable th) {
		StringWriter writer = new StringWriter();
		writer.append(new Date().toString()).append(": ").append(msg);
		if (th != null) {
			writer.append("\n  Exception: ");
			th.printStackTrace(new PrintWriter(writer));
		}
		return writer.toString();
	}
	

	protected String getVersion() {
		return _version;
	}

	private void setVersion(String version) {
		_version = version;		
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

	public String composeHostName(String hostName) {
		return hostName + "-"
				+ String.valueOf(getSimulatorManager().getRunId())
				+ "-" + String.valueOf(_instanceId);
	}

	public String getHostIqn() {
		return "iqn:" + getSimulatorManager().getRunId() + "."
				+ getInstanceId();
	}

	public long getInstanceId() {
		return _instanceId;
	}

	public MacAddress getHostMacAddress() {
		if (_macAddress == 0) {
			long address = 0;

			address = (_prefix & 0xff);
			address <<= 40;
			address |= ((long) (short) getSimulatorManager()
					.getRunId()) << 32;
			address |= _instanceId;
			_macAddress = address;
		}
		return new MacAddress(_macAddress);
	}
	
	public void setHostMacaddress(long macAddress) {
		_macAddress = macAddress;
	}

	public String getHostPrivateIp() {

		if (_hostPrivateIp == null) {
			long id = _instanceId;
			id |= getSimulatorManager().getRunId() << 16;
			_hostPrivateIp = _cidrPrefix + "." + String.valueOf(id & 0xff);
		}
		return _hostPrivateIp;
	}
	
	public void setHostPrivateIp(String hostPrivateIp) {
		_hostPrivateIp = hostPrivateIp;
	}

	public MacAddress getHostStorageMacAddress() {
		if (_storageMacAddress == 0) {
			long address = 0;

			address = (_prefix & 0xff);
			address <<= 40;
			address |= ((long) (short) getSimulatorManager()
					.getRunId()) << 32;
			address |= (_instanceId | (1L << 31)) & 0xffffffff;
			_storageMacAddress = address;
		}
		return new MacAddress(_storageMacAddress);
	}
	
	public void setHostStorageMacAddress(long storageMacAddress) {
		_storageMacAddress = storageMacAddress;
	}

	public MacAddress getHostStorageMacAddress2() {
		if(_storageMacAddressDeux == 0) {
			
			long address = 0;
			
			address = (_prefix & 0xff);
			address <<= 40;
			address |= ((long) (short) getSimulatorManager().getRunId()) << 32;
			address |= (_instanceId | (3L << 30)) & 0xffffffff;
			_storageMacAddressDeux = address;
		}
		return new MacAddress(_storageMacAddressDeux);
	}

	public void setHostStorageMacAddress2(long storageMacAddressDeux) {
		_storageMacAddressDeux = storageMacAddressDeux;
	}
	
	public String getHostStoragePrivateIp() {
		if(_hostStoragePrivateIp == null) {
			long id = _instanceId;
			id |= getSimulatorManager().getRunId() << 16;
			id |= 1 << 15;
			_hostStoragePrivateIp = _cidrPrefix  + "." + String.valueOf(id & 0xff);
		}
		
		return _hostStoragePrivateIp;
	}
	
	public void setHostStoragePrivateIp(String hostStoragePrivateIp) {
		_hostStoragePrivateIp = hostStoragePrivateIp;
	}

	public String getHostStoragePrivateIp2() {
		if(_hostStoragePrivateIpDeux == null) {
			long id = _instanceId;
			id |= getSimulatorManager().getRunId() << 16;
			id |= 3 << 14;
			_hostStoragePrivateIp = _cidrPrefix  + "." + String.valueOf(id & 0xff);
		}
		return _hostStoragePrivateIpDeux;
	}
	
	public void setHostStoragePrivateIp2(String hostStoragePrivateIpDeux) {
		_hostStoragePrivateIpDeux = hostStoragePrivateIpDeux;
	}

	protected String findScript(String script) {
		s_logger.debug("Looking for " + script + " in the classpath");
		URL url = ClassLoader.getSystemResource(script);
		File file = null;
		if (url == null) {
			file = new File("./" + script);
			s_logger.debug("Looking for " + script + " in "
					+ file.getAbsolutePath());
			if (!file.exists()) {
				return null;
			}
		} else {
			file = new File(url.getFile());
		}
		return file.getAbsolutePath();
	}
	
	public String getGuid() {
		return _guid;
	}
	
	public void setGuid(String guid) {
		_guid = guid;
	}

	public MockVmMgr getVmMgr() {
		return _vmMgr;
	}
	
	public void setVmMgr(MockVmMgr vmMgr) {
		_vmMgr = vmMgr;
	}

	public MockStorageMgr getStorageMgr() {
		return _storageMgr;
	}
	
	public void setStorageMgr(MockStorageMgr storageMgr) {
		_storageMgr = storageMgr;
	}
	
	public void setZone(long _dcId) {
		this._dcId = _dcId;
	}

	public long getZone() {
		return _dcId;
	}

	public void setHost(String _host) {
		this._host = _host;
	}

	public String getHost() {
		return _host;
	}

	public void setPod(String _pod) {
		this._pod = _pod;
	}

	public String getPod() {
		return _pod;
	}

	public void setCluster(String _cluster) {
		this._cluster = _cluster;
	}

	public String getCluster() {
		return _cluster;
	}

	@Override
	public Answer executeRequest(Command cmd) {
		return null;
	}

	@Override
	public PingCommand getCurrentStatus(long id) {
		return null;
	}

	@Override
	public Type getType() {
		return _type;
	}

	public void setType(Host.Type _type) {
		this._type = _type;
	}
	
	@Override
	public StartupCommand[] initialize() {
		return null;
	}
	
	public SimulatorManager getSimulatorManager() {
		return _simMgr;
	}	
	
	public void setSimulatorManager(SimulatorManager simMgr) {
		_simMgr = simMgr;	
	}
}
