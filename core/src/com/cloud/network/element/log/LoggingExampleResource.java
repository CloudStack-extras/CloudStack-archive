// Copyright 2012 Citrix Systems, Inc. Licensed under the
// Apache License, Version 2.0 (the "License"); you may not use this
// file except in compliance with the License.  Citrix Systems, Inc.
// reserves all rights not expressly granted by the License.
// You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 
package com.cloud.network.element.log;

import java.util.Map;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.ChangeLogLevelAnswer;
import com.cloud.agent.api.LogNetworkEventCommand;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupExternalFirewallCommand;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.hypervisor.xen.resource.CitrixResourceBase;
import com.cloud.resource.ServerResource;
import com.cloud.resource.ServerResourceBase;

/**
 * @author edison
 *
 */
public class LoggingExampleResource extends ServerResourceBase implements
		ServerResource {
	private final static Logger s_logger = Logger.getLogger(LoggingExampleResource.class);
	String _guid;
	long _zoneId;

	/* (non-Javadoc)
	 * @see com.cloud.resource.ServerResource#getType()
	 */
	@Override
	public Type getType() {
		return Host.Type.ExternalFirewall;
	}
	
    public boolean configure(String name, Map<String, Object> params) {
    	_guid = (String)params.get("guid");
    	_zoneId = Long.parseLong((String)params.get("zone"));
    	return true;
    }

	/* (non-Javadoc)
	 * @see com.cloud.resource.ServerResource#initialize()
	 */
	@Override
	public StartupCommand[] initialize() {
		StartupCommand[] cmds = new StartupCommand[1];
		StartupExternalFirewallCommand cmd = new StartupExternalFirewallCommand();
		cmd.setGuid(_guid);
		cmd.setDataCenter(Long.toString(_zoneId));
		cmd.setName(_guid);
		cmd.setPrivateIpAddress("192.168.10.2");
		cmd.setVersion(LoggingExampleResource.class.getPackage().getImplementationVersion());
		cmds[0] = cmd;
		return cmds;
	}

	/* (non-Javadoc)
	 * @see com.cloud.resource.ServerResource#getCurrentStatus(long)
	 */
	@Override
	public PingCommand getCurrentStatus(long id) {
		return new PingCommand(Host.Type.ExternalFirewall, id);
	}

	/* (non-Javadoc)
	 * @see com.cloud.resource.ServerResource#executeRequest(com.cloud.agent.api.Command)
	 */
	@Override
	public Answer executeRequest(Command cmd) {
		if (cmd.getClass() == LogNetworkEventCommand.class) {
			return execute((LogNetworkEventCommand)cmd);
		} if (cmd.getClass() == ReadyCommand.class) {
			return new ReadyAnswer((ReadyCommand)cmd);
		} else {
            return Answer.createUnsupportedCommandAnswer(cmd);
		}
	}

	protected ChangeLogLevelAnswer execute(LogNetworkEventCommand cmd) {
		if (cmd.getLevel().compareToIgnoreCase("trace") == 0) {
			s_logger.trace(cmd.getMsg());
		} else if (cmd.getLevel().compareToIgnoreCase("debug") == 0) {
			s_logger.debug(cmd.getMsg());
		} else {
			assert false : "Should never get here.  Who's not checking the values in the management server.  Shame!";
		}
		return new ChangeLogLevelAnswer(cmd, true, null);
	}
	
	/* (non-Javadoc)
	 * @see com.cloud.resource.ServerResourceBase#getDefaultScriptsDir()
	 */
	@Override
	protected String getDefaultScriptsDir() {
		// TODO Auto-generated method stub
		return null;
	}

}
