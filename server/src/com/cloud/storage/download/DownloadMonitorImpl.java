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
package com.cloud.storage.download;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.Listener;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.storage.DeleteTemplateCommand;
import com.cloud.agent.api.storage.DownloadCommand;
import com.cloud.agent.api.storage.DownloadProgressCommand;
import com.cloud.agent.api.storage.DownloadProgressCommand.RequestType;
import com.cloud.alert.AlertManager;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventVO;
import com.cloud.event.dao.EventDao;
import com.cloud.event.dao.UsageEventDao;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.StoragePoolHostVO;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateStoragePoolVO;
import com.cloud.storage.VMTemplateStorageResourceAssoc;
import com.cloud.storage.VMTemplateZoneVO;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateHostDao;
import com.cloud.storage.dao.VMTemplatePoolDao;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.storage.template.TemplateConstants;
import com.cloud.storage.template.TemplateInfo;
import com.cloud.user.Account;
import com.cloud.utils.component.Inject;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.SecondaryStorageVmVO;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.dao.SecondaryStorageVmDao;



/**
 * @author chiradeep
 *
 */
@Local(value={DownloadMonitor.class})
public class DownloadMonitorImpl implements  DownloadMonitor {
    static final Logger s_logger = Logger.getLogger(DownloadMonitorImpl.class);

	private static final String DEFAULT_HTTP_COPY_PORT = "80";
    @Inject 
    VMTemplateHostDao _vmTemplateHostDao;
    @Inject 
    VMTemplateZoneDao _vmTemplateZoneDao;
    @Inject
	VMTemplatePoolDao _vmTemplatePoolDao;
    @Inject
    StoragePoolHostDao _poolHostDao;
    @Inject
    SecondaryStorageVmDao _secStorageVmDao;
    @Inject
    AlertManager _alertMgr;
    
    @Inject
    HostDao _serverDao = null;
    @Inject
    private final DataCenterDao _dcDao = null;
    @Inject
    VMTemplateDao _templateDao =  null;
    @Inject
	private AgentManager _agentMgr;
    @Inject
    ConfigurationDao _configDao;
    
    @Inject 
    private UsageEventDao _usageEventDao;
    
    @Inject
    private ClusterDao _clusterDao;
    @Inject
    private HostDao _hostDao;

	private String _name;
	private Boolean _sslCopy = new Boolean(false);
	private String _copyAuthPasswd;


	Timer _timer;

	final Map<VMTemplateHostVO, DownloadListener> _listenerMap = new ConcurrentHashMap<VMTemplateHostVO, DownloadListener>();



	public long send(Long hostId, Command cmd, Listener listener) {
		return _agentMgr.gatherStats(hostId, cmd, listener);
	}

	@Override
	public boolean configure(String name, Map<String, Object> params) {
		_name = name;
        final Map<String, String> configs = _configDao.getConfiguration("ManagementServer", params);
        _sslCopy = Boolean.parseBoolean(configs.get("secstorage.encrypt.copy"));
        
        String cert = configs.get("secstorage.ssl.cert.domain");
        if (!"realhostip.com".equalsIgnoreCase(cert)) {
        	s_logger.warn("Only realhostip.com ssl cert is supported, ignoring self-signed and other certs");
        }
        
        _copyAuthPasswd = configs.get("secstorage.copy.password");
        
        _agentMgr.registerForHostEvents(new DownloadListener(this), true, false, false);
		return true;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public boolean start() {
		_timer = new Timer();
		return true;
	}

	@Override
	public boolean stop() {
		return true;
	}
	
	
	public boolean isTemplateUpdateable(Long templateId) {
		List<VMTemplateHostVO> downloadsInProgress =
			_vmTemplateHostDao.listByTemplateStatus(templateId, VMTemplateHostVO.Status.DOWNLOAD_IN_PROGRESS);
		return (downloadsInProgress.size() == 0);
	}
	
	
	public boolean isTemplateUpdateable(Long templateId, Long datacenterId) {
		List<VMTemplateHostVO> downloadsInProgress =
			_vmTemplateHostDao.listByTemplateStatus(templateId, datacenterId, VMTemplateHostVO.Status.DOWNLOAD_IN_PROGRESS);
		return (downloadsInProgress.size() == 0);
	}
	
	@Override
    public void copyTemplate(VMTemplateVO template, HostVO sourceServer, HostVO destServer) throws InvalidParameterValueException, StorageUnavailableException{

		boolean downloadJobExists = false;
        VMTemplateHostVO destTmpltHost = null;
        VMTemplateHostVO srcTmpltHost = null;

        srcTmpltHost = _vmTemplateHostDao.findByHostTemplate(sourceServer.getId(), template.getId());
        if (srcTmpltHost == null) {
        	throw new InvalidParameterValueException("Template " + template.getName() + " not associated with " + sourceServer.getName());
        }
        String url = generateCopyUrl(sourceServer, srcTmpltHost);
	    if (url == null) {
			s_logger.warn("Unable to start/resume copy of template " + template.getUniqueName() + " to " + destServer.getName() + ", no secondary storage vm in running state in source zone");
			throw new StorageUnavailableException("No secondary VM in running state in zone " + sourceServer.getDataCenterId(), srcTmpltHost.getPoolId());
	    }
        destTmpltHost = _vmTemplateHostDao.findByHostTemplate(destServer.getId(), template.getId());
        if (destTmpltHost == null) {
            destTmpltHost = new VMTemplateHostVO(destServer.getId(), template.getId(), new Date(), 0, VMTemplateStorageResourceAssoc.Status.NOT_DOWNLOADED, null, null, "jobid0000", null, url);
            destTmpltHost.setCopy(true);
            _vmTemplateHostDao.persist(destTmpltHost);
        } else if ((destTmpltHost.getJobId() != null) && (destTmpltHost.getJobId().length() > 2)) {
            downloadJobExists = true;
        }

        Long maxTemplateSizeInBytes = getMaxTemplateSizeInBytes();
        
		if(destTmpltHost != null) {
		    start();
		    
			DownloadCommand dcmd = new DownloadCommand(url, template.getUniqueName(), template.getFormat(), template.isRequiresHvm(), template.getAccountId(), template.getId(), template.getDisplayText(), template.getChecksum(), TemplateConstants.DEFAULT_HTTP_AUTH_USER, _copyAuthPasswd, maxTemplateSizeInBytes);
			DownloadListener dl = downloadJobExists?_listenerMap.get(destTmpltHost):null;
			if (dl == null) {
				dl = new DownloadListener(destServer, template, _timer, _vmTemplateHostDao, destTmpltHost.getId(), this, dcmd);
			}
			if (downloadJobExists) {
				dcmd = new DownloadProgressCommand(dcmd, destTmpltHost.getJobId(), RequestType.GET_OR_RESTART);
				dl.setCurrState(destTmpltHost.getDownloadState());
	 		}

			_listenerMap.put(destTmpltHost, dl);

			long result = send(destServer.getId(), dcmd, dl);
			if (result == -1) {
				s_logger.warn("Unable to start /resume COPY of template " + template.getUniqueName() + " to " + destServer.getName());
				dl.setDisconnected();
				dl.scheduleStatusCheck(RequestType.GET_OR_RESTART);
			}
		}
	}
	
	private String generateCopyUrl(String ipAddress, String path){
		String hostname = ipAddress;
		String scheme = "http";
		if (_sslCopy) {
			hostname = ipAddress.replace(".", "-");
			hostname = hostname + ".realhostip.com";
			scheme = "https";
		}
		return scheme + "://" + hostname + "/copy/" + path; 
	}
	
	private String generateCopyUrl(HostVO sourceServer, VMTemplateHostVO srcTmpltHost) {
		List<SecondaryStorageVmVO> ssVms = _secStorageVmDao.getSecStorageVmListInStates(sourceServer.getDataCenterId(), State.Running);
		if (ssVms.size() > 0) {
			SecondaryStorageVmVO ssVm = ssVms.get(0);
			if (ssVm.getPublicIpAddress() == null) {
				s_logger.warn("A running secondary storage vm has a null public ip?");
				return null;
			}
			return generateCopyUrl(ssVm.getPublicIpAddress(), srcTmpltHost.getInstallPath());
		}
		
		VMTemplateVO tmplt = _templateDao.findById(srcTmpltHost.getTemplateId());
		HypervisorType hyperType = tmplt.getHypervisorType();
		/*No secondary storage vm yet*/
		if (hyperType != null && hyperType == HypervisorType.KVM) {
			return "file://" + sourceServer.getParent() + "/" + srcTmpltHost.getInstallPath();
		}
		return null;
	}

	private void downloadTemplateToStorage(VMTemplateVO template, HostVO sserver) {
		boolean downloadJobExists = false;
        VMTemplateHostVO vmTemplateHost = null;

        vmTemplateHost = _vmTemplateHostDao.findByHostTemplate(sserver.getId(), template.getId());
        if (vmTemplateHost == null) {
            vmTemplateHost = new VMTemplateHostVO(sserver.getId(), template.getId(), new Date(), 0, VMTemplateStorageResourceAssoc.Status.NOT_DOWNLOADED, null, null, "jobid0000", null, template.getUrl());
            _vmTemplateHostDao.persist(vmTemplateHost);
        } else if ((vmTemplateHost.getJobId() != null) && (vmTemplateHost.getJobId().length() > 2)) {
            downloadJobExists = true;
        }
                
        Long maxTemplateSizeInBytes = getMaxTemplateSizeInBytes();
        
		if(vmTemplateHost != null) {
		    start();
			DownloadCommand dcmd = new DownloadCommand(template, maxTemplateSizeInBytes);
			dcmd.setUrl(vmTemplateHost.getDownloadUrl());
			if (vmTemplateHost.isCopy()) {
				dcmd.setCreds(TemplateConstants.DEFAULT_HTTP_AUTH_USER, _copyAuthPasswd);
			}
			DownloadListener dl = new DownloadListener(sserver, template, _timer, _vmTemplateHostDao, vmTemplateHost.getId(), this, dcmd);
			if (downloadJobExists) {
				dcmd = new DownloadProgressCommand(dcmd, vmTemplateHost.getJobId(), RequestType.GET_OR_RESTART);
				dl.setCurrState(vmTemplateHost.getDownloadState());
	 		}

			_listenerMap.put(vmTemplateHost, dl);

			long result = send(sserver.getId(), dcmd, dl);
			if (result == -1) {
				s_logger.warn("Unable to start /resume download of template " + template.getUniqueName() + " to " + sserver.getName());
				dl.setDisconnected();
				dl.scheduleStatusCheck(RequestType.GET_OR_RESTART);
			}
		}
	}



	@Override
	public boolean downloadTemplateToStorage(Long templateId, Long zoneId) {
		if (isTemplateUpdateable(templateId)) {
			List<DataCenterVO> dcs = new ArrayList<DataCenterVO>();
			
			if (zoneId == null) {
				dcs.addAll(_dcDao.listAllIncludingRemoved());
			} else {
				dcs.add(_dcDao.findById(zoneId));
			}

			for (DataCenterVO dc: dcs) {
				initiateTemplateDownload(templateId, dc.getId());
			}
			return true;
		} else {
			return false;
		}
	}

	private void initiateTemplateDownload(Long templateId, Long dataCenterId) {
		VMTemplateVO template = _templateDao.findById(templateId);
		if (template != null && (template.getUrl() != null)) {
			//find all storage hosts and tell them to initiate download
			List<HostVO> storageServers = _serverDao.listByTypeDataCenter(Host.Type.SecondaryStorage, dataCenterId);
			for (HostVO sserver: storageServers) {
				downloadTemplateToStorage(template, sserver);
			}
		}
		
	}

	public void handleDownloadEvent(HostVO host, VMTemplateVO template, Status dnldStatus) {
		if ((dnldStatus == VMTemplateStorageResourceAssoc.Status.DOWNLOADED) || (dnldStatus==Status.ABANDONED)){
			VMTemplateHostVO vmTemplateHost = new VMTemplateHostVO(host.getId(), template.getId());
			DownloadListener oldListener = _listenerMap.get(vmTemplateHost);
			if (oldListener != null) {
				_listenerMap.remove(vmTemplateHost);
			}
		}
		
		VMTemplateHostVO vmTemplateHost = _vmTemplateHostDao.findByHostTemplate(host.getId(), template.getId());
		
        if (dnldStatus == Status.DOWNLOADED) {
            long size = -1;
            if(vmTemplateHost!=null){
            	size = vmTemplateHost.getSize();
            }
            else{
            	s_logger.warn("Failed to get size for template" + template.getName());
            }
			String eventType = EventTypes.EVENT_TEMPLATE_CREATE;
            if((template.getFormat()).equals(ImageFormat.ISO)){
                eventType = EventTypes.EVENT_ISO_CREATE;
            }
            if(template.getAccountId() != Account.ACCOUNT_ID_SYSTEM){
                UsageEventVO usageEvent = new UsageEventVO(eventType, template.getAccountId(), host.getDataCenterId(), template.getId(), template.getName(), null, null , size);
                _usageEventDao.persist(usageEvent);
            }
        } 
        
		if (vmTemplateHost != null) {
			Long poolId = vmTemplateHost.getPoolId();
			if (poolId != null) {
				VMTemplateStoragePoolVO vmTemplatePool = _vmTemplatePoolDao.findByPoolTemplate(poolId, template.getId());
				StoragePoolHostVO poolHost = _poolHostDao.findByPoolHost(poolId, host.getId());
				if (vmTemplatePool != null && poolHost != null) {
					vmTemplatePool.setDownloadPercent(vmTemplateHost.getDownloadPercent());
					vmTemplatePool.setDownloadState(vmTemplateHost.getDownloadState());
					vmTemplatePool.setErrorString(vmTemplateHost.getErrorString());
					String localPath = poolHost.getLocalPath();
					String installPath = vmTemplateHost.getInstallPath();
					if (installPath != null) {
						if (!installPath.startsWith("/")) {
							installPath = "/" + installPath;
						}
						if (!(localPath == null) && !installPath.startsWith(localPath)) {
							localPath = localPath.replaceAll("/\\p{Alnum}+/*$", ""); //remove instance if necessary
						}
						if (!(localPath == null) && installPath.startsWith(localPath)) {
							installPath = installPath.substring(localPath.length());
						}
					}
					vmTemplatePool.setInstallPath(installPath);
					vmTemplatePool.setLastUpdated(vmTemplateHost.getLastUpdated());
					vmTemplatePool.setJobId(vmTemplateHost.getJobId());
					vmTemplatePool.setLocalDownloadPath(vmTemplateHost.getLocalDownloadPath());
					_vmTemplatePoolDao.update(vmTemplatePool.getId(),vmTemplatePool);
				}
			}
		}

	}
	
	@Override
    public void handleSysTemplateDownload(HostVO host) {
	    List<HypervisorType> hypers = _hostDao.getAvailHypervisorInZone(host.getId(), host.getDataCenterId());
	    HypervisorType hostHyper = host.getHypervisorType();
	    if (hypers.contains(hostHyper)) {
	        return;
	    }

	    Set<VMTemplateVO> toBeDownloaded = new HashSet<VMTemplateVO>();
	    List<HostVO> ssHosts = _hostDao.listBy(Host.Type.SecondaryStorage, host.getDataCenterId());
	    if (ssHosts == null || ssHosts.isEmpty()) {
	        return;
	    }
	    HostVO sshost = ssHosts.get(0);
	    /*Download all the templates in zone with the same hypervisortype*/

	    List<VMTemplateVO> rtngTmplts = _templateDao.listAllSystemVMTemplates();
	    List<VMTemplateVO> defaultBuiltin = _templateDao.listDefaultBuiltinTemplates();


	    for (VMTemplateVO rtngTmplt : rtngTmplts) {
	        if (rtngTmplt.getHypervisorType() == hostHyper) {
	            toBeDownloaded.add(rtngTmplt);
	        }
	    }

	    for (VMTemplateVO builtinTmplt : defaultBuiltin) {
	        if (builtinTmplt.getHypervisorType() == hostHyper) {
	            toBeDownloaded.add(builtinTmplt);
	        }
	    }

	    for (VMTemplateVO template: toBeDownloaded) {
	        VMTemplateHostVO tmpltHost = _vmTemplateHostDao.findByHostTemplate(sshost.getId(), template.getId());
	        if (tmpltHost == null || tmpltHost.getDownloadState() != Status.DOWNLOADED) {
	            downloadTemplateToStorage(template, sshost);
	        }
	    }
	}
	
	@Override
	public void handleTemplateSync(long sserverId, Map<String, TemplateInfo> templateInfos) {
		HostVO storageHost = _serverDao.findById(sserverId);
		if (storageHost == null) {
			s_logger.warn("Huh? Agent id " + sserverId + " does not correspond to a row in hosts table?");
			return;
		}		
		long zoneId = storageHost.getDataCenterId();

		Set<VMTemplateVO> toBeDownloaded = new HashSet<VMTemplateVO>();
		List<VMTemplateVO> allTemplates = _templateDao.listAllInZone(storageHost.getDataCenterId());
		List<VMTemplateVO> rtngTmplts = _templateDao.listAllSystemVMTemplates();
		List<VMTemplateVO> defaultBuiltin = _templateDao.listDefaultBuiltinTemplates();
		
		if (rtngTmplts != null) {
		    for (VMTemplateVO rtngTmplt : rtngTmplts) {
		        if (!allTemplates.contains(rtngTmplt)) {
		            allTemplates.add(rtngTmplt);
		        }
		    }
		}

		if (defaultBuiltin != null) {
		    for (VMTemplateVO builtinTmplt : defaultBuiltin) {
		        if (!allTemplates.contains(builtinTmplt)) {
		            allTemplates.add(builtinTmplt);
		        }
		    }
		}

        toBeDownloaded.addAll(allTemplates);
        
		for (VMTemplateVO tmplt: allTemplates) {
			String uniqueName = tmplt.getUniqueName();
			VMTemplateHostVO tmpltHost = _vmTemplateHostDao.findByHostTemplate(sserverId, tmplt.getId());
			if (templateInfos.containsKey(uniqueName)) {
			    TemplateInfo tmpltInfo = templateInfos.remove(uniqueName);
				toBeDownloaded.remove(tmplt);
				if (tmpltHost != null) {
					s_logger.info("Template Sync found " + uniqueName + " already in the template host table");
                    if (tmpltHost.getDownloadState() != Status.DOWNLOADED) {
                    	tmpltHost.setErrorString("");
                    }
                    if( tmpltInfo.isCorrupted() ) {
                        tmpltHost.setDownloadState(Status.DOWNLOAD_ERROR);
                        tmpltHost.setErrorString("This template is corrupted");
                        toBeDownloaded.add(tmplt);
                        s_logger.info("Template (" + tmplt +") is corrupted");
                        if( tmplt.getUrl() == null) {
                            String msg = "Private Template (" + tmplt +") with install path "  + tmpltInfo.getInstallPath() + 
                            "is corrupted, please check in secondary storage: " + tmpltHost.getHostId();
                            s_logger.warn(msg);
                        } else {
                            toBeDownloaded.add(tmplt);
                        }

                    } else {
                        tmpltHost.setDownloadPercent(100);
                        tmpltHost.setDownloadState(Status.DOWNLOADED);
                        tmpltHost.setInstallPath(tmpltInfo.getInstallPath());
                        tmpltHost.setSize(tmpltInfo.getSize());
                        tmpltHost.setPhysicalSize(tmpltInfo.getPhysicalSize());
                        tmpltHost.setLastUpdated(new Date());
                    }
					_vmTemplateHostDao.update(tmpltHost.getId(), tmpltHost);
				} else {
				    tmpltHost = new VMTemplateHostVO(sserverId, tmplt.getId(), new Date(), 100, Status.DOWNLOADED, null, null, null, tmpltInfo.getInstallPath(), tmplt.getUrl());
					tmpltHost.setSize(tmpltInfo.getSize());
                    tmpltHost.setPhysicalSize(tmpltInfo.getPhysicalSize());
					_vmTemplateHostDao.persist(tmpltHost);
					VMTemplateZoneVO tmpltZoneVO = _vmTemplateZoneDao.findByZoneTemplate(zoneId, tmplt.getId());
					if (tmpltZoneVO == null){
					    tmpltZoneVO = new VMTemplateZoneVO(zoneId, tmplt.getId(), new Date());
					    _vmTemplateZoneDao.persist(tmpltZoneVO);
					} else {
					    tmpltZoneVO.setLastUpdated(new Date());
					    _vmTemplateZoneDao.update(tmpltZoneVO.getId(), tmpltZoneVO);
					}

				}

				continue;
			}
			if (tmpltHost != null && tmpltHost.getDownloadState() != Status.DOWNLOADED) {
				s_logger.info("Template Sync did not find " + uniqueName + " ready on server " + sserverId + ", will request download to start/resume shortly");

			} else if (tmpltHost == null) {
				s_logger.info("Template Sync did not find " + uniqueName + " on the server " + sserverId + ", will request download shortly");
				VMTemplateHostVO templtHost = new VMTemplateHostVO(sserverId, tmplt.getId(), new Date(), 0, Status.NOT_DOWNLOADED, null, null, null, null, tmplt.getUrl());
				_vmTemplateHostDao.persist(templtHost);
				VMTemplateZoneVO tmpltZoneVO = _vmTemplateZoneDao.findByZoneTemplate(zoneId, tmplt.getId());
				if (tmpltZoneVO == null){
				    tmpltZoneVO = new VMTemplateZoneVO(zoneId, tmplt.getId(), new Date());
				    _vmTemplateZoneDao.persist(tmpltZoneVO);
				} else {
				    tmpltZoneVO.setLastUpdated(new Date());
				    _vmTemplateZoneDao.update(tmpltZoneVO.getId(), tmpltZoneVO);
				}
			}

		}
		
		if (toBeDownloaded.size() > 0) {
			HostVO sserver = _serverDao.findById(sserverId);
			if (sserver == null) {
				throw new CloudRuntimeException("Unable to find host from id");
			}
			/*Only download templates whose hypervirsor type is in the zone*/
			List<HypervisorType> availHypers = _clusterDao.getAvailableHypervisorInZone(sserver.getDataCenterId());
			for (VMTemplateVO tmplt: toBeDownloaded) {
				
				if (tmplt.getUrl() == null){ // If url is null we cant initiate the download so mark it as an error.
					VMTemplateHostVO tmpltHost = _vmTemplateHostDao.findByHostTemplate(sserver.getId(), tmplt.getId());
					if(tmpltHost != null){
						tmpltHost.setDownloadState(Status.DOWNLOAD_ERROR);
						tmpltHost.setDownloadPercent(0);
						tmpltHost.setErrorString("Cannot initiate the download as url is null.");
						_vmTemplateHostDao.update(tmpltHost.getId(), tmpltHost);
					}
					continue;
				}
				
			    if (availHypers.contains(tmplt.getHypervisorType())) {
			        s_logger.debug("Template " + tmplt.getName() + " needs to be downloaded to " + sserver.getName());
			        downloadTemplateToStorage(tmplt, sserver);
			    }
			}
		}
		
		for (String uniqueName: templateInfos.keySet()) {
			TemplateInfo tInfo = templateInfos.get(uniqueName);
			DeleteTemplateCommand dtCommand = new DeleteTemplateCommand(tInfo.getInstallPath());
			long result = send(sserverId, dtCommand, null);
			if (result == -1 ){
				String description = "Failed to delete " + tInfo.getTemplateName() + " on secondary storage " + sserverId + " which isn't in the database";
				s_logger.error(description);
				return;
			}
			String description = "Deleted template " + tInfo.getTemplateName() + " on secondary storage " + sserverId + " since it isn't in the database, result=" + result;
			s_logger.info(description);
		}

	}

	@Override
	public void cancelAllDownloads(Long templateId) {
		List<VMTemplateHostVO> downloadsInProgress =
			_vmTemplateHostDao.listByTemplateStates(templateId, VMTemplateHostVO.Status.DOWNLOAD_IN_PROGRESS, VMTemplateHostVO.Status.NOT_DOWNLOADED);
		if (downloadsInProgress.size() > 0){
			for (VMTemplateHostVO vmthvo: downloadsInProgress) {
				DownloadListener dl = _listenerMap.get(vmthvo);
				if (dl != null) {
					dl.abandon();
					s_logger.info("Stopping download of template " + templateId + " to storage server " + vmthvo.getHostId());
				}
			}
		}
	}
	
	private Long getMaxTemplateSizeInBytes() {
		try {
			return Long.parseLong(_configDao.getValue("max.template.iso.size")) * 1024L * 1024L * 1024L;
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
}
	
