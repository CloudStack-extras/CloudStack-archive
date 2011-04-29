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
package com.cloud.storage.listener;

import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.exception.ConnectionException;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.StorageManagerImpl;
import com.cloud.storage.StoragePoolVO;
import com.cloud.storage.dao.StoragePoolDao;

public class StoragePoolMonitor implements Listener {
    private static final Logger s_logger = Logger.getLogger(StoragePoolMonitor.class);
	private final StorageManagerImpl _storageManager;
	private final StoragePoolDao _poolDao;
	
    public StoragePoolMonitor(StorageManagerImpl mgr, StoragePoolDao poolDao) {
    	this._storageManager = mgr;
    	this._poolDao = poolDao;
    }
    
    
    @Override
    public boolean isRecurring() {
        return false;
    }
    
    @Override
    public synchronized boolean processAnswers(long agentId, long seq, Answer[] resp) {
        return true;
    }
    
    @Override
    public synchronized boolean processDisconnect(long agentId, Status state) {
        return true;
    }
    
    @Override
    public void processConnect(HostVO host, StartupCommand cmd) throws ConnectionException {
    	if (cmd instanceof StartupRoutingCommand) {
    		StartupRoutingCommand scCmd = (StartupRoutingCommand)cmd;
    		if (scCmd.getHypervisorType() == HypervisorType.XenServer || scCmd.getHypervisorType() ==  HypervisorType.KVM ||
				scCmd.getHypervisorType() == HypervisorType.VMware) {
    			List<StoragePoolVO> pools = _poolDao.listBy(host.getDataCenterId(), host.getPodId(), host.getClusterId());
    			for (StoragePoolVO pool : pools) {
    			    if (!pool.getPoolType().isShared()) {
    			        continue;
    			    }
    				Long hostId = host.getId();
    				s_logger.debug("Host " + hostId + " connected, sending down storage pool information ...");
    				try {
    				    _storageManager.connectHostToSharedPool(hostId, pool);
    					_storageManager.createCapacityEntry(pool);
    				} catch (Exception e) {
    				    throw new ConnectionException(true, "Unable to connect to pool " + pool, e);
    				}
    			}
    		}
    	}
    }
    

    @Override
    public boolean processCommands(long agentId, long seq, Command[] req) {
        return false;
    }
   
    @Override
    public AgentControlAnswer processControlCommand(long agentId, AgentControlCommand cmd) {
    	return null;
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
