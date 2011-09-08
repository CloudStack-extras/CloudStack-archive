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

package com.cloud.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.AgentManager.OnError;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.GetFileStatsCommand;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.agent.api.HostStatsEntry;
import com.cloud.agent.api.VmStatsEntry;
import com.cloud.agent.manager.Commands;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.host.Host;
import com.cloud.host.HostStats;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.storage.StorageManager;
import com.cloud.storage.StoragePoolHostVO;
import com.cloud.storage.StoragePoolVO;
import com.cloud.storage.StorageStats;
import com.cloud.storage.VolumeStats;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.StoragePoolDao;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.vm.UserVmManager;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VmStats;
import com.cloud.vm.dao.UserVmDao;

/**
 * Provides real time stats for various agent resources up to x seconds
 * 
 * @author Will Chan
 *
 */
public class StatsCollector {
	public static final Logger s_logger = Logger.getLogger(StatsCollector.class.getName());

	private static StatsCollector s_instance = null;

	private ScheduledExecutorService _executor = null;
	private final AgentManager _agentMgr;
	private final UserVmManager _userVmMgr;
	private final HostDao _hostDao;
	private final UserVmDao _userVmDao;
	private final VolumeDao _volsDao;
	private final CapacityDao _capacityDao;
	private final StoragePoolDao _storagePoolDao;
	private final StorageManager _storageManager;
    private final StoragePoolHostDao _storagePoolHostDao;

	private ConcurrentHashMap<Long, HostStats> _hostStats = new ConcurrentHashMap<Long, HostStats>();
	private final ConcurrentHashMap<Long, VmStats> _VmStats = new ConcurrentHashMap<Long, VmStats>();
	private ConcurrentHashMap<Long, VolumeStats> _volumeStats = new ConcurrentHashMap<Long, VolumeStats>();
	private ConcurrentHashMap<Long, StorageStats> _storageStats = new ConcurrentHashMap<Long, StorageStats>();
	private ConcurrentHashMap<Long, StorageStats> _storagePoolStats = new ConcurrentHashMap<Long, StorageStats>();
	
	long hostStatsInterval = -1L;
	long hostAndVmStatsInterval = -1L;
	long storageStatsInterval = -1L;
	long volumeStatsInterval = -1L;

	//private final GlobalLock m_capacityCheckLock = GlobalLock.getInternLock("capacity.check");

    public static StatsCollector getInstance() {
        return s_instance;
    }
	public static StatsCollector getInstance(Map<String, String> configs) {
	    if (s_instance == null) {
	        s_instance = new StatsCollector(configs);
	    }
        return s_instance;
    }

	private StatsCollector(Map<String, String> configs) {
		ComponentLocator locator = ComponentLocator.getLocator(ManagementServer.Name);
		_agentMgr = locator.getManager(AgentManager.class);
		_userVmMgr = locator.getManager(UserVmManager.class);
		_hostDao = locator.getDao(HostDao.class);
		_userVmDao = locator.getDao(UserVmDao.class);
		_volsDao = locator.getDao(VolumeDao.class);
		_capacityDao = locator.getDao(CapacityDao.class);
		_storagePoolDao = locator.getDao(StoragePoolDao.class);
		_storageManager = locator.getManager(StorageManager.class);
        _storagePoolHostDao  = locator.getDao(StoragePoolHostDao.class);

		_executor = Executors.newScheduledThreadPool(3, new NamedThreadFactory("StatsCollector"));

		 hostStatsInterval = NumbersUtil.parseLong(configs.get("host.stats.interval"), 60000L);
		 hostAndVmStatsInterval = NumbersUtil.parseLong(configs.get("vm.stats.interval"), 60000L);
		 storageStatsInterval = NumbersUtil.parseLong(configs.get("storage.stats.interval"), 60000L);
		 volumeStatsInterval = NumbersUtil.parseLong(configs.get("volume.stats.interval"), -1L);

		 if (hostStatsInterval > 0) {
		     _executor.scheduleWithFixedDelay(new HostCollector(), 15000L, hostStatsInterval, TimeUnit.MILLISECONDS);
		 }
		 
		 if (hostAndVmStatsInterval > 0) {
		     _executor.scheduleWithFixedDelay(new VmStatsCollector(), 15000L, hostAndVmStatsInterval, TimeUnit.MILLISECONDS);
		 }
		 
		 if (storageStatsInterval > 0) {
		     _executor.scheduleWithFixedDelay(new StorageCollector(), 15000L, storageStatsInterval, TimeUnit.MILLISECONDS);
		 }
		
		// -1 means we don't even start this thread to pick up any data.
		if (volumeStatsInterval > 0) {
			_executor.scheduleWithFixedDelay(new VolumeCollector(), 15000L, volumeStatsInterval, TimeUnit.MILLISECONDS);
		} else {
			s_logger.info("Disabling volume stats collector");
		}
	}

	class HostCollector implements Runnable {
		@Override
        public void run() {
			try {
				s_logger.debug("HostStatsCollector is running...");
				
				SearchCriteria<HostVO> sc = _hostDao.createSearchCriteria();
				sc.addAnd("status", SearchCriteria.Op.EQ, Status.Up.toString());
				sc.addAnd("type", SearchCriteria.Op.NEQ, Host.Type.Storage.toString());
				sc.addAnd("type", SearchCriteria.Op.NEQ, Host.Type.ConsoleProxy.toString());
				sc.addAnd("type", SearchCriteria.Op.NEQ, Host.Type.SecondaryStorage.toString());
				sc.addAnd("type", SearchCriteria.Op.NEQ, Host.Type.LocalSecondaryStorage.toString());
				sc.addAnd("type", SearchCriteria.Op.NEQ, Host.Type.TrafficMonitor.toString());
		        sc.addAnd("type", SearchCriteria.Op.NEQ, Host.Type.SecondaryStorageVM.toString());
		        sc.addAnd("type", SearchCriteria.Op.NEQ, Host.Type.ExternalFirewall.toString());
		        sc.addAnd("type", SearchCriteria.Op.NEQ, Host.Type.ExternalLoadBalancer.toString());
				ConcurrentHashMap<Long, HostStats> hostStats = new ConcurrentHashMap<Long, HostStats>();
				List<HostVO> hosts = _hostDao.search(sc, null);
				for (HostVO host : hosts)
				{
				    HostStatsEntry stats = (HostStatsEntry) _agentMgr.getHostStatistics(host.getId());
				    if (stats != null)
				    {
                        hostStats.put(host.getId(), stats);
				    }
				    else
				    {
				        s_logger.warn("Received invalid host stats for host: " + host.getId());
				    }
				}
				_hostStats = hostStats;
			}
			catch (Throwable t)
			{
				s_logger.error("Error trying to retrieve host stats", t);
			}
		}
	}
	
	class VmStatsCollector implements Runnable {
		@Override
        public void run() {
			try {
				s_logger.debug("VmStatsCollector is running...");
				
				SearchCriteria<HostVO> sc = _hostDao.createSearchCriteria();
				sc.addAnd("status", SearchCriteria.Op.EQ, Status.Up.toString());
				sc.addAnd("type", SearchCriteria.Op.NEQ, Host.Type.Storage.toString());
				sc.addAnd("type", SearchCriteria.Op.NEQ, Host.Type.ConsoleProxy.toString());
				sc.addAnd("type", SearchCriteria.Op.NEQ, Host.Type.SecondaryStorage.toString());
                sc.addAnd("type", SearchCriteria.Op.NEQ, Host.Type.LocalSecondaryStorage.toString());
                sc.addAnd("type", SearchCriteria.Op.NEQ, Host.Type.TrafficMonitor.toString());
                sc.addAnd("type", SearchCriteria.Op.NEQ, Host.Type.SecondaryStorageVM.toString());
				List<HostVO> hosts = _hostDao.search(sc, null);
				
				for (HostVO host : hosts) {
					List<UserVmVO> vms = _userVmDao.listRunningByHostId(host.getId());
					List<Long> vmIds = new ArrayList<Long>();
					
					for (UserVmVO vm : vms) {
						vmIds.add(vm.getId());
					}
					
					try
					{
							HashMap<Long, VmStatsEntry> vmStatsById = _userVmMgr.getVirtualMachineStatistics(host.getId(), host.getName(), vmIds);
							
							if(vmStatsById != null)
							{
								VmStatsEntry statsInMemory = null;
								
								Set<Long> vmIdSet = vmStatsById.keySet();
								for(Long vmId : vmIdSet)
								{
									VmStatsEntry statsForCurrentIteration = vmStatsById.get(vmId);
									statsInMemory = (VmStatsEntry) _VmStats.get(vmId);
									
									if(statsInMemory == null)
									{
										//no stats exist for this vm, directly persist
										_VmStats.put(vmId, statsForCurrentIteration);
									}
									else
									{
										//update each field
										statsInMemory.setCPUUtilization(statsForCurrentIteration.getCPUUtilization());
										statsInMemory.setNumCPUs(statsForCurrentIteration.getNumCPUs());
										statsInMemory.setNetworkReadKBs(statsInMemory.getNetworkReadKBs() + statsForCurrentIteration.getNetworkReadKBs());
										statsInMemory.setNetworkWriteKBs(statsInMemory.getNetworkWriteKBs() + statsForCurrentIteration.getNetworkWriteKBs());
										
										_VmStats.put(vmId, statsInMemory);
									}
								}
							}
							
					} catch (Exception e) {
						s_logger.debug("Failed to get VM stats for host with ID: " + host.getId());
						continue;
					}
				}
				
			} catch (Throwable t) {
				s_logger.error("Error trying to retrieve VM stats", t);
			}
		}
	}

	public VmStats getVmStats(long id) {
		return _VmStats.get(id);
	}

	class StorageCollector implements Runnable {
		@Override
        public void run() {
			try {
	            if (s_logger.isDebugEnabled()) {
	            	s_logger.debug("StorageCollector is running...");
	            }
				
                List<HostVO> hosts = _hostDao.listSecondaryStorageHosts();
                ConcurrentHashMap<Long, StorageStats> storageStats = new ConcurrentHashMap<Long, StorageStats>();
                for (HostVO host : hosts) {
                    if ( host.getStorageUrl() == null ) {
                        continue;
                    }
                    GetStorageStatsCommand command = new GetStorageStatsCommand(host.getStorageUrl());
                    HostVO ssAhost = _agentMgr.getSSAgent(host);
                    if (ssAhost == null) {
                        s_logger.debug("There is no secondary storage VM for secondary storage host " + host.getName());
                        continue;
                    }
                    long hostId = host.getId();
                    Answer answer = _agentMgr.easySend(ssAhost.getId(), command);
                    if (answer != null && answer.getResult()) {
                        storageStats.put(hostId, (StorageStats)answer);
                        s_logger.trace("HostId: "+hostId+ " Used: " + ((StorageStats)answer).getByteUsed() + " Total Available: " + ((StorageStats)answer).getCapacityBytes());
                        //Seems like we have dynamically updated the sec. storage as prev. size and the current do not match
                        if (_storageStats.get(hostId)!=null &&
                        		_storageStats.get(hostId).getCapacityBytes() != ((StorageStats)answer).getCapacityBytes()){
	                       	host.setTotalSize(((StorageStats)answer).getCapacityBytes());
	                       	_hostDao.update(hostId, host);
	                    }
                    }
                }
                _storageStats = storageStats;
				ConcurrentHashMap<Long, StorageStats> storagePoolStats = new ConcurrentHashMap<Long, StorageStats>();

				List<StoragePoolVO> storagePools = _storagePoolDao.listAll();
				for (StoragePoolVO pool: storagePools) {
					GetStorageStatsCommand command = new GetStorageStatsCommand(pool.getUuid(), pool.getPoolType(), pool.getPath());
					long poolId = pool.getId();
					try {
    					Answer answer = _storageManager.sendToPool(pool, command);
    					if (answer != null && answer.getResult()) {
    						storagePoolStats.put(pool.getId(), (StorageStats)answer);
    
    						// Seems like we have dynamically updated the pool size since the prev. size and the current do not match
    						if (_storagePoolStats.get(poolId)!= null &&
    								_storagePoolStats.get(poolId).getCapacityBytes() != ((StorageStats)answer).getCapacityBytes()){
    		                    pool.setCapacityBytes(((StorageStats)answer).getCapacityBytes());
    		                    _storagePoolDao.update(pool.getId(), pool);
    	                    }
    					}
                    } catch (StorageUnavailableException e) {
                        s_logger.info("Unable to reach " + pool);
                    } catch (Exception e) {
                        s_logger.warn("Unable to get stats for " + pool);
                    }
				}
                _storagePoolStats = storagePoolStats;
			} catch (Throwable t) {
				s_logger.error("Error trying to retrieve storage stats", t);
			}
		}
	}

	public StorageStats getStorageStats(long id) {
		return _storageStats.get(id);
	}
	
	public HostStats getHostStats(long hostId){
		return _hostStats.get(hostId);
	}
	
	public StorageStats getStoragePoolStats(long id) {
		return _storagePoolStats.get(id);
	}

	class VolumeCollector implements Runnable {
		@Override
        public void run() {
			try {
				List<VolumeVO> volumes = _volsDao.listAll();
				Map<Long, List<VolumeCommand>> commandsByPool = new HashMap<Long, List<VolumeCommand>>();
				
				for (VolumeVO volume : volumes) {
					List<VolumeCommand> commands = commandsByPool.get(volume.getPoolId());
					if (commands == null) {
						commands = new ArrayList<VolumeCommand>();
						commandsByPool.put(volume.getPoolId(), commands);
					}
					VolumeCommand vCommand = new VolumeCommand();
					vCommand.volumeId = volume.getId();
					vCommand.command = new GetFileStatsCommand(volume);
					commands.add(vCommand);
				}
				ConcurrentHashMap<Long, VolumeStats> volumeStats = new ConcurrentHashMap<Long, VolumeStats>();
				for (Iterator<Long> iter = commandsByPool.keySet().iterator(); iter.hasNext();) {
					Long poolId = iter.next();
					if(poolId != null) {
						List<VolumeCommand> commandsList = commandsByPool.get(poolId);
						
						long[] volumeIdArray = new long[commandsList.size()];
						Commands commands = new Commands(OnError.Continue);
						for (int i = 0; i < commandsList.size(); i++) {
							VolumeCommand vCommand = commandsList.get(i);
							volumeIdArray[i] = vCommand.volumeId;
							commands.addCommand(vCommand.command);
						}
						
			            List<StoragePoolHostVO> poolhosts = _storagePoolHostDao.listByPoolId(poolId);
			            for(StoragePoolHostVO poolhost : poolhosts) {
	    					Answer[] answers = _agentMgr.send(poolhost.getHostId(), commands);
	    					if (answers != null) {
	    					    long totalBytes = 0L;
	    						for (int i = 0; i < answers.length; i++) {
	    							if (answers[i].getResult()) {
	    							    VolumeStats vStats = (VolumeStats)answers[i];
	    								volumeStats.put(volumeIdArray[i], vStats);
	    								totalBytes += vStats.getBytesUsed();
	    							}
	    						}
	    						break;
	                        }
			            }
					}
				}

				// We replace the existing volumeStats so that it does not grow with no bounds
				_volumeStats = volumeStats;
			} catch (AgentUnavailableException e) {
			    s_logger.debug(e.getMessage());
			} catch (Throwable t) {
				s_logger.error("Error trying to retrieve volume stats", t);
			}
		}
	}

	private class VolumeCommand {
		public long volumeId;
		public GetFileStatsCommand command;
	}
	
	public VolumeStats[] getVolumeStats(long[] ids) {
		VolumeStats[] stats = new VolumeStats[ids.length];
		if (volumeStatsInterval > 0) {
			for (int i = 0; i < ids.length; i++) {
				stats[i] = _volumeStats.get(ids[i]);
			}
		}
		return stats;
	}
}
