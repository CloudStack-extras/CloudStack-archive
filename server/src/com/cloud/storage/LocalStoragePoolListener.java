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
package com.cloud.storage;

import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupStorageCommand;
import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.capacity.Capacity;
import com.cloud.capacity.CapacityVO;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.exception.ConnectionException;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.storage.dao.StoragePoolDao;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;

public class LocalStoragePoolListener implements Listener {
    private final static Logger s_logger = Logger.getLogger(LocalStoragePoolListener.class);
    @Inject StoragePoolDao _storagePoolDao;
    @Inject StoragePoolHostDao _storagePoolHostDao;
    @Inject CapacityDao _capacityDao;
    @Inject StorageManager _storageMgr;

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public boolean isRecurring() {
        return false;
    }

    @Override
    public boolean processAnswers(long agentId, long seq, Answer[] answers) {
        return false;
    }

    @Override
    public boolean processCommands(long agentId, long seq, Command[] commands) {
        return false;
    }
    
    @Override
    @DB
    public void processConnect(HostVO host, StartupCommand cmd, boolean forRebalance) throws ConnectionException {
        if (!(cmd instanceof StartupStorageCommand)) {
            return;
        }
        
        StartupStorageCommand ssCmd = (StartupStorageCommand)cmd;
        
        if (ssCmd.getResourceType() != Storage.StorageResourceType.STORAGE_POOL) {
            return;
        }
        
        StoragePoolInfo pInfo = ssCmd.getPoolInfo();
        if (pInfo == null) {
            return;
        }
        
        try {
            StoragePoolVO pool = _storagePoolDao.findPoolByHostPath(host.getDataCenterId(), host.getPodId(), pInfo.getHost(), pInfo.getHostPath(), pInfo.getUuid());
            if (pool == null) {
                long poolId = _storagePoolDao.getNextInSequence(Long.class, "id");
                String name = cmd.getName() == null ? (host.getName() + " Local Storage") : cmd.getName();
                Transaction txn = Transaction.currentTxn();
                txn.start();
                pool = new StoragePoolVO(poolId, name, pInfo.getUuid(), pInfo.getPoolType(), host.getDataCenterId(),
                                         host.getPodId(), pInfo.getAvailableBytes(), pInfo.getCapacityBytes(), pInfo.getHost(), 0,
                                         pInfo.getHostPath());
                pool.setClusterId(host.getClusterId());
                _storagePoolDao.persist(pool, pInfo.getDetails());
                StoragePoolHostVO poolHost = new StoragePoolHostVO(pool.getId(), host.getId(), pInfo.getLocalPath());
                _storagePoolHostDao.persist(poolHost);
                _storageMgr.createCapacityEntry(pool, Capacity.CAPACITY_TYPE_LOCAL_STORAGE, pool.getCapacityBytes() - pool.getAvailableBytes());
                
                txn.commit();
            } else {
                Transaction txn = Transaction.currentTxn();
                txn.start();
                pool.setAvailableBytes(pInfo.getAvailableBytes());
                pool.setCapacityBytes(pInfo.getCapacityBytes());
                _storagePoolDao.update(pool.getId(), pool);
                if (pInfo.getDetails() != null) {
                    _storagePoolDao.updateDetails(pool.getId(), pInfo.getDetails());
                }
                StoragePoolHostVO poolHost = _storagePoolHostDao.findByPoolHost(pool.getId(), host.getId());
                if (poolHost == null) {
                    poolHost = new StoragePoolHostVO(pool.getId(), host.getId(), pInfo.getLocalPath());
                    _storagePoolHostDao.persist(poolHost);
                }
                
                _storageMgr.createCapacityEntry(pool, Capacity.CAPACITY_TYPE_LOCAL_STORAGE, pool.getCapacityBytes() - pool.getAvailableBytes());
                
                txn.commit();
            }
        } catch (Exception e) {
            s_logger.warn("Unable to setup the local storage pool for " + host, e);
            throw new ConnectionException(true, "Unable to setup the local storage pool for " + host, e);
        }
    }
    
   

    @Override
    public AgentControlAnswer processControlCommand(long agentId, AgentControlCommand cmd) {
        return null;
    }

    @Override
    public boolean processDisconnect(long agentId, Status state) {
        return false;
    }

    @Override
    public boolean processTimeout(long agentId, long seq) {
        return false;
    }
}
