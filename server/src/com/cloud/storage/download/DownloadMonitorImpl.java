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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.Listener;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.storage.DeleteTemplateCommand;
import com.cloud.agent.api.storage.DownloadCommand;
import com.cloud.agent.api.storage.DownloadProgressCommand;
import com.cloud.agent.api.storage.ListTemplateAnswer;
import com.cloud.agent.api.storage.ListTemplateCommand;
import com.cloud.agent.api.storage.DownloadProgressCommand.RequestType;
import com.cloud.alert.AlertManager;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventVO;
import com.cloud.event.dao.UsageEventDao;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;
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
import com.cloud.storage.secondary.SecondaryStorageVmManager;
import com.cloud.storage.template.TemplateConstants;
import com.cloud.storage.template.TemplateInfo;
import com.cloud.user.Account;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.vm.SecondaryStorageVm;
import com.cloud.vm.SecondaryStorageVmVO;
import com.cloud.vm.UserVmManager;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.dao.SecondaryStorageVmDao;



/**
 * @author chiradeep
 *
 */
@Local(value={DownloadMonitor.class})
public class DownloadMonitorImpl implements  DownloadMonitor {
    static final Logger s_logger = Logger.getLogger(DownloadMonitorImpl.class);
	
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
    private final DataCenterDao _dcDao = null;
    @Inject
    VMTemplateDao _templateDao =  null;
    @Inject
	private AgentManager _agentMgr;
    @Inject SecondaryStorageVmManager _secMgr;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    UserVmManager _vmMgr;

    
    @Inject 
    private UsageEventDao _usageEventDao;
    
    @Inject
    private ClusterDao _clusterDao;
    @Inject
    private HostDao _hostDao;

	private String _name;
	private Boolean _sslCopy = new Boolean(false);
	private String _copyAuthPasswd;
    protected SearchBuilder<VMTemplateHostVO> ReadyTemplateStatesSearch;

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
        
        ReadyTemplateStatesSearch = _vmTemplateHostDao.createSearchBuilder();
        ReadyTemplateStatesSearch.and("download_state", ReadyTemplateStatesSearch.entity().getDownloadState(), SearchCriteria.Op.EQ);
        ReadyTemplateStatesSearch.and("destroyed", ReadyTemplateStatesSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        ReadyTemplateStatesSearch.and("host_id", ReadyTemplateStatesSearch.entity().getHostId(), SearchCriteria.Op.EQ);

        SearchBuilder<VMTemplateVO> TemplatesWithNoChecksumSearch = _templateDao.createSearchBuilder();
        TemplatesWithNoChecksumSearch.and("checksum", TemplatesWithNoChecksumSearch.entity().getChecksum(), SearchCriteria.Op.NULL);

        ReadyTemplateStatesSearch.join("vm_template", TemplatesWithNoChecksumSearch, TemplatesWithNoChecksumSearch.entity().getId(),
                ReadyTemplateStatesSearch.entity().getTemplateId(), JoinBuilder.JoinType.INNER);
        TemplatesWithNoChecksumSearch.done();
        ReadyTemplateStatesSearch.done();
               
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
	
	public boolean isTemplateUpdateable(Long templateId, Long hostId) {
		List<VMTemplateHostVO> downloadsInProgress =
			_vmTemplateHostDao.listByTemplateHostStatus(templateId.longValue(), hostId.longValue(), VMTemplateHostVO.Status.DOWNLOAD_IN_PROGRESS, VMTemplateHostVO.Status.DOWNLOADED);
		return (downloadsInProgress.size() == 0);
	}
	
	@Override
    public boolean copyTemplate(VMTemplateVO template, HostVO sourceServer, HostVO destServer) throws StorageUnavailableException{

		boolean downloadJobExists = false;
        VMTemplateHostVO destTmpltHost = null;
        VMTemplateHostVO srcTmpltHost = null;

        srcTmpltHost = _vmTemplateHostDao.findByHostTemplate(sourceServer.getId(), template.getId());
        if (srcTmpltHost == null) {
        	throw new InvalidParameterValueException("Template " + template.getName() + " not associated with " + sourceServer.getName());
        }
        if( !_secMgr.generateSetupCommand(sourceServer.getId()) ){
            return false;
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
            destTmpltHost.setPhysicalSize(srcTmpltHost.getPhysicalSize());
            _vmTemplateHostDao.persist(destTmpltHost);
        } else if ((destTmpltHost.getJobId() != null) && (destTmpltHost.getJobId().length() > 2)) {
            downloadJobExists = true;
        }

        Long maxTemplateSizeInBytes = getMaxTemplateSizeInBytes();
        
		if(destTmpltHost != null) {
		    start();
		    
			DownloadCommand dcmd =  
              new DownloadCommand(destServer.getStorageUrl(), url, template, TemplateConstants.DEFAULT_HTTP_AUTH_USER, _copyAuthPasswd, maxTemplateSizeInBytes);
			if (downloadJobExists) {
				dcmd = new DownloadProgressCommand(dcmd, destTmpltHost.getJobId(), RequestType.GET_OR_RESTART);
	 		} 
            HostVO ssAhost = _agentMgr.getSSAgent(destServer);
            if( ssAhost == null ) {
                 s_logger.warn("There is no secondary storage VM for secondary storage host " + destServer.getName());
                 return false;
            }
            DownloadListener dl = new DownloadListener(ssAhost, destServer, template, _timer, _vmTemplateHostDao, destTmpltHost.getId(), this, dcmd);
            if (downloadJobExists) {
                dl.setCurrState(destTmpltHost.getDownloadState());
            }
			DownloadListener old = null;
			synchronized (_listenerMap) {
			    old = _listenerMap.put(destTmpltHost, dl);
			}
			if( old != null ) {
			    old.abandon();
			}
			
	        long result = send(ssAhost.getId(), dcmd, dl);
			if (result == -1) {
				s_logger.warn("Unable to start /resume COPY of template " + template.getUniqueName() + " to " + destServer.getName());
				dl.setDisconnected();
				dl.scheduleStatusCheck(RequestType.GET_OR_RESTART);
			} else {
			    return true;
			}
		}
		return false;
	}
	
	private String generateCopyUrl(String ipAddress, String dir, String path){
		String hostname = ipAddress;
		String scheme = "http";
		if (_sslCopy) {
			hostname = ipAddress.replace(".", "-");
			hostname = hostname + ".realhostip.com";
			scheme = "https";
		}
		return scheme + "://" + hostname + "/copy/SecStorage/" + dir + "/" + path; 
	}
	
	private String generateCopyUrl(HostVO sourceServer, VMTemplateHostVO srcTmpltHost) {
		List<SecondaryStorageVmVO> ssVms = _secStorageVmDao.getSecStorageVmListInStates(SecondaryStorageVm.Role.templateProcessor, sourceServer.getDataCenterId(), State.Running);
		if (ssVms.size() > 0) {
			SecondaryStorageVmVO ssVm = ssVms.get(0);
			if (ssVm.getPublicIpAddress() == null) {
				s_logger.warn("A running secondary storage vm has a null public ip?");
				return null;
			}
			return generateCopyUrl(ssVm.getPublicIpAddress(), sourceServer.getParent(), srcTmpltHost.getInstallPath());
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
        String secUrl = sserver.getStorageUrl();
		if(vmTemplateHost != null) {
		    start();
			DownloadCommand dcmd =
             new DownloadCommand(secUrl, template, maxTemplateSizeInBytes);
	        if (downloadJobExists) {
	            dcmd = new DownloadProgressCommand(dcmd, vmTemplateHost.getJobId(), RequestType.GET_OR_RESTART);
	        }
			if (vmTemplateHost.isCopy()) {
				dcmd.setCreds(TemplateConstants.DEFAULT_HTTP_AUTH_USER, _copyAuthPasswd);
			}
			HostVO ssAhost = _agentMgr.getSSAgent(sserver);
			if( ssAhost == null ) {
	             s_logger.warn("There is no secondary storage VM for secondary storage host " + sserver.getName());
	             return;
			}
			DownloadListener dl = new DownloadListener(ssAhost, sserver, template, _timer, _vmTemplateHostDao, vmTemplateHost.getId(), this, dcmd);
			if (downloadJobExists) {
				dl.setCurrState(vmTemplateHost.getDownloadState());
	 		}
            DownloadListener old = null;
            synchronized (_listenerMap) {
                old = _listenerMap.put(vmTemplateHost, dl);
            }
            if( old != null ) {
                old.abandon();
            }

			long result = send(ssAhost.getId(), dcmd, dl);
			if (result == -1) {
				s_logger.warn("Unable to start /resume download of template " + template.getUniqueName() + " to " + sserver.getName());
				dl.setDisconnected();
				dl.scheduleStatusCheck(RequestType.GET_OR_RESTART);
			}
		}
	}



	@Override
	public boolean downloadTemplateToStorage(VMTemplateVO template, Long zoneId) {
        List<DataCenterVO> dcs = new ArrayList<DataCenterVO>();       
        if (zoneId == null) {
            dcs.addAll(_dcDao.listAll());
        } else {
            dcs.add(_dcDao.findById(zoneId));
        }
        long templateId = template.getId();
        for ( DataCenterVO dc : dcs ) {
    	    List<HostVO> ssHosts = _hostDao.listAllSecondaryStorageHosts(dc.getId());
    	    for ( HostVO ssHost : ssHosts ) {
        		if (isTemplateUpdateable(templateId, ssHost.getId())) {
       				initiateTemplateDownload(templateId, ssHost);
       				break;
        		}
    	    }
	    }
	    return true;
	}

	private void initiateTemplateDownload(Long templateId, HostVO ssHost) {
		VMTemplateVO template = _templateDao.findById(templateId);
		if (template != null && (template.getUrl() != null)) {
			//find all storage hosts and tell them to initiate download
    		downloadTemplateToStorage(template, ssHost);
		}
		
	}

	@DB
	public void handleDownloadEvent(HostVO host, VMTemplateVO template, Status dnldStatus) {
		if ((dnldStatus == VMTemplateStorageResourceAssoc.Status.DOWNLOADED) || (dnldStatus==Status.ABANDONED)){
			VMTemplateHostVO vmTemplateHost = new VMTemplateHostVO(host.getId(), template.getId());
	        synchronized (_listenerMap) {
	            _listenerMap.remove(vmTemplateHost);
	        }
		}
		
		VMTemplateHostVO vmTemplateHost = _vmTemplateHostDao.findByHostTemplate(host.getId(), template.getId());
		
		Transaction txn = Transaction.currentTxn();
        txn.start();
		
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

        if (dnldStatus == Status.DOWNLOADED) {
            long size = -1;
            if(vmTemplateHost!=null){
                size = vmTemplateHost.getPhysicalSize();
            }
            else{
                s_logger.warn("Failed to get size for template" + template.getName());
            }
            String eventType = EventTypes.EVENT_TEMPLATE_CREATE;
            if((template.getFormat()).equals(ImageFormat.ISO)){
                eventType = EventTypes.EVENT_ISO_CREATE;
            }
            if(template.getAccountId() != Account.ACCOUNT_ID_SYSTEM){
                UsageEventVO usageEvent = new UsageEventVO(eventType, template.getAccountId(), host.getDataCenterId(), template.getId(), template.getName(), null, template.getSourceTemplateId() , size);
                _usageEventDao.persist(usageEvent);
            }
        }
        txn.commit();
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
	    /*Download all the templates in zone with the same hypervisortype*/
        for ( HostVO ssHost : ssHosts) {
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
    	        VMTemplateHostVO tmpltHost = _vmTemplateHostDao.findByHostTemplate(ssHost.getId(), template.getId());
    	        if (tmpltHost == null || tmpltHost.getDownloadState() != Status.DOWNLOADED) {
    	            downloadTemplateToStorage(template, ssHost);
    	        }
    	    }
        }
	}
    
    @Override	
	public void addSystemVMTemplatesToHost(HostVO host, Map<String, TemplateInfo> templateInfos){
	    if ( templateInfos == null ) {
	        return;
	    }
	    Long hostId = host.getId();
	    List<VMTemplateVO> rtngTmplts = _templateDao.listAllSystemVMTemplates();
	    for ( VMTemplateVO tmplt : rtngTmplts ) {
	        TemplateInfo tmpltInfo = templateInfos.get(tmplt.getUniqueName());
	        if ( tmpltInfo == null ) {
	            continue;
	        }
	        VMTemplateHostVO tmpltHost = _vmTemplateHostDao.findByHostTemplate(hostId, tmplt.getId());
	        if ( tmpltHost == null ) {
                tmpltHost = new VMTemplateHostVO(hostId, tmplt.getId(), new Date(), 100, Status.DOWNLOADED, null, null, null, tmpltInfo.getInstallPath(), tmplt.getUrl());
	            tmpltHost.setSize(tmpltInfo.getSize());
	            tmpltHost.setPhysicalSize(tmpltInfo.getPhysicalSize());
	            _vmTemplateHostDao.persist(tmpltHost);
	        }
	    }
	}
	
    @Override
    public void handleTemplateSync(long dcId) {
        List<HostVO> ssHosts = _hostDao.listSecondaryStorageHosts(dcId);
        for ( HostVO ssHost : ssHosts ) {
            handleTemplateSync(ssHost);
        }
    }
    
    private Map<String, TemplateInfo> listTemplate(HostVO ssHost) {
        ListTemplateCommand cmd = new ListTemplateCommand(ssHost.getStorageUrl());
        Answer answer = _agentMgr.sendToSecStorage(ssHost, cmd);
        if (answer != null && answer.getResult()) {
            ListTemplateAnswer tanswer = (ListTemplateAnswer)answer;
            return tanswer.getTemplateInfo();
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("can not list template for secondary storage host " + ssHost.getId());
            }
        } 
        
        return null;
    }
    
	
	@Override
    public void handleTemplateSync(HostVO ssHost) {
        if (ssHost == null) {
            s_logger.warn("Huh? ssHost is null");
            return;
        }
        long sserverId = ssHost.getId();
        long zoneId = ssHost.getDataCenterId();
        if (!(ssHost.getType() == Host.Type.SecondaryStorage || ssHost.getType() == Host.Type.LocalSecondaryStorage)) {
            s_logger.warn("Huh? Agent id " + sserverId + " is not secondary storage host");
            return;
        }
        Map<String, TemplateInfo> templateInfos = listTemplate(ssHost);
        if (templateInfos == null) {
            return;
        }

        Set<VMTemplateVO> toBeDownloaded = new HashSet<VMTemplateVO>();
        List<VMTemplateVO> allTemplates = _templateDao.listAllInZone(zoneId);
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

        for (VMTemplateVO tmplt : allTemplates) {
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
                    if (tmpltInfo.isCorrupted()) {
                        tmpltHost.setDownloadState(Status.DOWNLOAD_ERROR);
                        String msg = "Template " + tmplt.getName() + ":" + tmplt.getId() + " is corrupted on secondary storage " + tmpltHost.getId();
                        tmpltHost.setErrorString(msg);
                        s_logger.info("msg");
                        if (tmplt.getUrl() == null) {
                            msg = "Private Template (" + tmplt + ") with install path " + tmpltInfo.getInstallPath() + "is corrupted, please check in secondary storage: " + tmpltHost.getHostId();
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
                    if (tmpltZoneVO == null) {
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
            	//if the template is already downloaded into one of secondary storage in the zone, don't duplicate right now.
            	if (tmplt.getTemplateType() != TemplateType.BUILTIN) {
            		List<VMTemplateHostVO> readyTmplts = _vmTemplateHostDao.listByZoneTemplate(zoneId, tmplt.getId(), true);
            		if (readyTmplts.size() > 0) {
            			toBeDownloaded.remove(tmplt);
            			continue;
            		}
            	}
                s_logger.info("Template Sync did not find " + uniqueName + " on the server " + sserverId + ", will request download shortly");
                VMTemplateHostVO templtHost = new VMTemplateHostVO(sserverId, tmplt.getId(), new Date(), 0, Status.NOT_DOWNLOADED, null, null, null, null, tmplt.getUrl());
                _vmTemplateHostDao.persist(templtHost);
                VMTemplateZoneVO tmpltZoneVO = _vmTemplateZoneDao.findByZoneTemplate(zoneId, tmplt.getId());
                if (tmpltZoneVO == null) {
                    tmpltZoneVO = new VMTemplateZoneVO(zoneId, tmplt.getId(), new Date());
                    _vmTemplateZoneDao.persist(tmpltZoneVO);
                } else {
                    tmpltZoneVO.setLastUpdated(new Date());
                    _vmTemplateZoneDao.update(tmpltZoneVO.getId(), tmpltZoneVO);
                }
            }

        }

        if (toBeDownloaded.size() > 0) {
            /* Only download templates whose hypervirsor type is in the zone */
            List<HypervisorType> availHypers = _clusterDao.getAvailableHypervisorInZone(zoneId);
            if (availHypers.isEmpty()) {
                /*
                 * This is for cloudzone, local secondary storage resource
                 * started before cluster created
                 */
                availHypers.add(HypervisorType.KVM);
            }
            /* Baremetal need not to download any template */
            availHypers.remove(HypervisorType.BareMetal);
            availHypers.add(HypervisorType.None); // bug 9809: resume ISO
                                                  // download.
            for (VMTemplateVO tmplt : toBeDownloaded) {
                if (tmplt.getUrl() == null) { // If url is null we can't
                                              // initiate the download
                    continue;
                }
                // if this is private template, and there is no record for this
                // template in this sHost, skip
                if (!tmplt.isPublicTemplate() && !tmplt.isFeatured()) {
                    VMTemplateHostVO tmpltHost = _vmTemplateHostDao.findByHostTemplate(sserverId, tmplt.getId());
                    if (tmpltHost == null) {
                        continue;
                    }
                }
                if (availHypers.contains(tmplt.getHypervisorType())) {
                    s_logger.debug("Template " + tmplt.getName() + " needs to be downloaded to " + ssHost.getName());
                    downloadTemplateToStorage(tmplt, ssHost);
                }
            }
        }

        for (String uniqueName : templateInfos.keySet()) {
            TemplateInfo tInfo = templateInfos.get(uniqueName);
            DeleteTemplateCommand dtCommand = new DeleteTemplateCommand(ssHost.getStorageUrl(), tInfo.getInstallPath());
            long result = _agentMgr.sendToSecStorage(ssHost, dtCommand, null);
            if (result == -1) {
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
			    DownloadListener dl = null;
		        synchronized (_listenerMap) {
				    dl = _listenerMap.remove(vmthvo);
		        }
				if (dl != null) {
					dl.abandon();
					s_logger.info("Stopping download of template " + templateId + " to storage server " + vmthvo.getHostId());
				}
			}
		}
	}
	
	private void checksumSync(long hostId){
        SearchCriteria<VMTemplateHostVO> sc = ReadyTemplateStatesSearch.create();
        sc.setParameters("download_state", com.cloud.storage.VMTemplateStorageResourceAssoc.Status.DOWNLOADED);
        sc.setParameters("host_id", hostId);

        List<VMTemplateHostVO> templateHostRefList = _vmTemplateHostDao.search(sc, null);
        s_logger.debug("Found " +templateHostRefList.size()+ " templates with no checksum. Will ask for computation");
        for(VMTemplateHostVO templateHostRef : templateHostRefList){
            s_logger.debug("Getting checksum for template - " + templateHostRef.getTemplateId());
            String checksum = _vmMgr.getChecksum(hostId, templateHostRef.getInstallPath());
            VMTemplateVO template = _templateDao.findById(templateHostRef.getTemplateId());
            s_logger.debug("Setting checksum " +checksum+ " for template - " + template.getName());
            template.setChecksum(checksum);
            _templateDao.update(template.getId(), template);
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
	
