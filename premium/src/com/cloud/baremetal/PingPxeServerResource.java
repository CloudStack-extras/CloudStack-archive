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

package com.cloud.baremetal;

import java.util.HashMap;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingRoutingCommand;
import com.cloud.agent.api.baremetal.PreparePxeServerAnswer;
import com.cloud.agent.api.baremetal.PreparePxeServerCommand;
import com.cloud.agent.api.baremetal.prepareCreateTemplateCommand;
import com.cloud.utils.script.Script;
import com.cloud.utils.ssh.SSHCmdHelper;
import com.cloud.vm.VirtualMachine.State;
import com.trilead.ssh2.SCPClient;

public class PingPxeServerResource extends PxeServerResourceBase {
	private static final Logger s_logger = Logger.getLogger(PingPxeServerResource.class);
	String _storageServer;
	String _pingDir;
	String _share;
	String _dir;
	String _tftpDir;
	String _cifsUserName;
	String _cifsPassword;
	
	@Override
	public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
		super.configure(name, params);
		
		_storageServer = (String)params.get("storageServer");
		_pingDir = (String)params.get("pingDir");
		_tftpDir = (String)params.get("tftpDir");
		_cifsUserName = (String)params.get("cifsUserName");
		_cifsPassword = (String)params.get("cifsPassword");
		
		if (_storageServer == null) {
			throw new ConfigurationException("No stroage server specified");
		}
		
		if (_tftpDir == null) {
			throw new ConfigurationException("No tftp directory specified");
		}
		
		if (_pingDir == null) {
			throw new ConfigurationException("No PING directory specified");
		}
		
		if (_cifsUserName == null || _cifsUserName.equalsIgnoreCase("")) {
			_cifsUserName = "xxx";
		}
		
		if (_cifsPassword == null || _cifsPassword.equalsIgnoreCase("")) {
			_cifsPassword = "xxx";
		}
		
		String pingDirs[]= _pingDir.split("/");
		if (pingDirs.length != 2) {
			throw new ConfigurationException("PING dir should have format like myshare/direcotry, eg: windows/64bit");
		}
		_share = pingDirs[0];
		_dir = pingDirs[1];
		
		com.trilead.ssh2.Connection sshConnection = new com.trilead.ssh2.Connection(_ip, 22);
		
		s_logger.debug(String.format("Trying to connect to PING PXE server(IP=%1$s, username=%2$s, password=%3$s", _ip, _username, _password));
		try {
			sshConnection.connect(null, 60000, 60000);
			if (!sshConnection.authenticateWithPassword(_username, _password)) {
				s_logger.debug("SSH Failed to authenticate");
				throw new ConfigurationException(String.format("Cannot connect to PING PXE server(IP=%1$s, username=%2$s, password=%3$s", _ip, _username,
						_password));
			}
			
			String cmd = String.format("[ -f /%1$s/pxelinux.0 ] && [ -f /%2$s/kernel ] && [ -f /%3$s/initrd.gz ] ", _tftpDir, _tftpDir, _tftpDir);
			if (!SSHCmdHelper.sshExecuteCmd(sshConnection, cmd)) {
				throw new ConfigurationException("Miss files in TFTP directory at " + _tftpDir + " check if pxelinux.0, kernel initrd.gz are here");
			}
			
			SCPClient scp = new SCPClient(sshConnection);	
			String prepareScript = "scripts/network/ping/prepare_tftp_bootfile.py";
			String prepareScriptPath = Script.findScript("", prepareScript);
			if (prepareScriptPath == null) {
				throw new ConfigurationException("Can not find prepare_tftp_bootfile.py at " + prepareScriptPath);
			}
			scp.put(prepareScriptPath, "/usr/bin/", "0755");
			
			return true;
		} catch (Exception e) {
			throw new ConfigurationException(e.getMessage());
		} finally {
			if (sshConnection != null) {
				sshConnection.close();
			}
		}
	}
	
	@Override
	public PingCommand getCurrentStatus(long id) {
		com.trilead.ssh2.Connection sshConnection = SSHCmdHelper.acquireAuthorizedConnection(_ip, _username, _password);
		if (sshConnection == null) {
			return null;
		} else {
			SSHCmdHelper.releaseSshConnection(sshConnection);
			return new PingRoutingCommand(getType(), id, new HashMap<String, State>());
		}
	}
	
	protected PreparePxeServerAnswer execute(PreparePxeServerCommand cmd) {
		com.trilead.ssh2.Connection sshConnection = new com.trilead.ssh2.Connection(_ip, 22);
		try {
			sshConnection.connect(null, 60000, 60000);
			if (!sshConnection.authenticateWithPassword(_username, _password)) {
				s_logger.debug("SSH Failed to authenticate");
				throw new ConfigurationException(String.format("Cannot connect to PING PXE server(IP=%1$s, username=%2$s, password=%3$s", _ip, _username,
						_password));
			}
			
			String script = String.format("python /usr/bin/prepare_tftp_bootfile.py restore %1$s %2$s %3$s %4$s %5$s %6$s %7$s %8$s %9$s %10$s %11$s",
					_tftpDir, cmd.getMac(), _storageServer, _share, _dir, cmd.getTemplate(), _cifsUserName, _cifsPassword, cmd.getIp(), cmd.getNetMask(), cmd.getGateWay());
			s_logger.debug("Prepare Ping PXE server successfully");
			if (!SSHCmdHelper.sshExecuteCmd(sshConnection, script)) {
				return new PreparePxeServerAnswer(cmd, "prepare PING at " + _ip + " failed, command:" + script);
			}	
			
			return new PreparePxeServerAnswer(cmd);
		} catch (Exception e){
			s_logger.debug("Prepare PING pxe server failed", e);
			return new PreparePxeServerAnswer(cmd, e.getMessage());
		} finally {
			if (sshConnection != null) {
				sshConnection.close();
			}
		}
	}
	
	protected Answer execute(prepareCreateTemplateCommand cmd) {
       com.trilead.ssh2.Connection sshConnection = new com.trilead.ssh2.Connection(_ip, 22);
        try {
            sshConnection.connect(null, 60000, 60000);
            if (!sshConnection.authenticateWithPassword(_username, _password)) {
                s_logger.debug("SSH Failed to authenticate");
                throw new ConfigurationException(String.format("Cannot connect to PING PXE server(IP=%1$s, username=%2$s, password=%3$s", _ip, _username,
                        _password));
            }
            
            String script = String.format("python /usr/bin/prepare_tftp_bootfile.py backup %1$s %2$s %3$s %4$s %5$s %6$s %7$s %8$s %9$s %10$s %11$s",
                    _tftpDir, cmd.getMac(), _storageServer, _share, _dir, cmd.getTemplate(), _cifsUserName, _cifsPassword, cmd.getIp(), cmd.getNetMask(), cmd.getGateWay());
            s_logger.debug("Prepare for creating template successfully");
            if (!SSHCmdHelper.sshExecuteCmd(sshConnection, script)) {
                return new Answer(cmd, false, "prepare for creating template failed, command:" + script);
            }
            
            return new Answer(cmd, true, "Success");
        }  catch (Exception e){
            s_logger.debug("Prepare for creating baremetal template failed", e);
            return new Answer(cmd, false, e.getMessage());
        } finally {
            if (sshConnection != null) {
                sshConnection.close();
            }
        }
	}
	
	@Override
	public Answer executeRequest(Command cmd) {
		if (cmd instanceof PreparePxeServerCommand) {
			return execute((PreparePxeServerCommand) cmd);
		} else if (cmd instanceof prepareCreateTemplateCommand) {
		    return execute((prepareCreateTemplateCommand)cmd);
		} else {
			return super.executeRequest(cmd);
		}
	}
}
