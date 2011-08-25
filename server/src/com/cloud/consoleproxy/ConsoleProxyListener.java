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
package com.cloud.consoleproxy;

import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.ConsoleAccessAuthenticationCommand;
import com.cloud.agent.api.ConsoleProxyLoadReportCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupProxyCommand;
import com.cloud.host.HostVO;
import com.cloud.host.Status;

public class ConsoleProxyListener implements Listener {
    AgentHook _proxyMgr = null;

    public ConsoleProxyListener(AgentHook proxyMgr) {
        _proxyMgr = proxyMgr;
    }

    @Override
    public boolean isRecurring() {
        return true;
    }

    @Override
    public boolean processAnswers(long agentId, long seq, Answer[] answers) {
        return true;
    }

    @Override
    public boolean processCommands(long agentId, long seq, Command[] commands) {
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(long agentId, AgentControlCommand cmd) {
        if (cmd instanceof ConsoleProxyLoadReportCommand) {
            _proxyMgr.onLoadReport((ConsoleProxyLoadReportCommand) cmd);

            // return dummy answer
            return new AgentControlAnswer(cmd);
        } else if (cmd instanceof ConsoleAccessAuthenticationCommand) {
            return _proxyMgr.onConsoleAccessAuthentication((ConsoleAccessAuthenticationCommand) cmd);
        }
        return null;
    }

    @Override
    public void processConnect(HostVO host, StartupCommand cmd, boolean forRebalance) {
        _proxyMgr.onAgentConnect(host, cmd);
    	
    	if (cmd instanceof StartupProxyCommand) {
    		_proxyMgr.startAgentHttpHandlerInVM((StartupProxyCommand)cmd);
    	}
    }

    @Override
    public boolean processDisconnect(long agentId, Status state) {
        _proxyMgr.onAgentDisconnect(agentId, state);
        return true;
    }

    @Override
    public boolean processTimeout(long agentId, long seq) {
        return true;
    }

    @Override
    public int getTimeout() {
        return -1;
    }
    
}
