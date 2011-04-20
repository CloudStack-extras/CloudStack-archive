package com.cloud.storage.upload;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.Listener;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.storage.CreateEntityDownloadURLCommand;
import com.cloud.agent.api.storage.DeleteEntityDownloadURLCommand;
import com.cloud.agent.api.storage.UploadCommand;
import com.cloud.agent.api.storage.UploadProgressCommand.RequestType;
import com.cloud.api.ApiDBUtils;
import com.cloud.async.AsyncJobManager;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Upload;
import com.cloud.storage.Upload.Mode;
import com.cloud.storage.Upload.Status;
import com.cloud.storage.Upload.Type;
import com.cloud.storage.UploadVO;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.UploadDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateHostDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.Inject;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.SecondaryStorageVmVO;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.dao.SecondaryStorageVmDao;

/**
 * @author nitin
 * Monitors the progress of upload.
 */
@Local(value={UploadMonitor.class})
public class UploadMonitorImpl implements UploadMonitor {

	static final Logger s_logger = Logger.getLogger(UploadMonitorImpl.class);
	
    @Inject 
    VMTemplateHostDao _vmTemplateHostDao;
    @Inject 
    UploadDao _uploadDao;
    @Inject
    SecondaryStorageVmDao _secStorageVmDao;

    
    @Inject
    HostDao _serverDao = null;    
    @Inject
    VMTemplateDao _templateDao =  null;
    @Inject
	private AgentManager _agentMgr;
    @Inject
    ConfigurationDao _configDao;

	private String _name;
	private Boolean _sslCopy = new Boolean(false);
    private ScheduledExecutorService _executor = null;

	Timer _timer;
	int _cleanupInterval;

	final Map<UploadVO, UploadListener> _listenerMap = new ConcurrentHashMap<UploadVO, UploadListener>();

	
	@Override
	public void cancelAllUploads(Long templateId) {
		// TODO

	}	
	
	@Override
	public boolean isTypeUploadInProgress(Long typeId, Type type) {
		List<UploadVO> uploadsInProgress =
			_uploadDao.listByTypeUploadStatus(typeId, type, UploadVO.Status.UPLOAD_IN_PROGRESS);
		
		if(uploadsInProgress.size() > 0) {
            return true;
        } else if (type == Type.VOLUME && _uploadDao.listByTypeUploadStatus(typeId, type, UploadVO.Status.COPY_IN_PROGRESS).size() > 0){
		    return true;
		}
		return false;
		
	}
	
	@Override
	public UploadVO createNewUploadEntry(Long hostId, Long typeId, UploadVO.Status  uploadState,
	                                        Type  type, String uploadUrl, Upload.Mode mode){
	       
        UploadVO uploadObj = new UploadVO(hostId, typeId, new Date(), 
                                          uploadState, type, uploadUrl, mode);
        _uploadDao.persist(uploadObj);
        
        return uploadObj;
	    
	}
	
	@Override
	public void extractVolume(UploadVO uploadVolumeObj, HostVO sserver, VolumeVO volume, String url, Long dataCenterId, String installPath, long eventId, long asyncJobId, AsyncJobManager asyncMgr){				
						
		uploadVolumeObj.setUploadState(Upload.Status.NOT_UPLOADED);
		_uploadDao.update(uploadVolumeObj.getId(), uploadVolumeObj);
				
	    start();		
		UploadCommand ucmd = new UploadCommand(url, volume.getId(), volume.getSize(), installPath, Type.VOLUME);
		UploadListener ul = new UploadListener(sserver, _timer, _uploadDao, uploadVolumeObj, this, ucmd, volume.getAccountId(), volume.getName(), Type.VOLUME, eventId, asyncJobId, asyncMgr);
		_listenerMap.put(uploadVolumeObj, ul);

		long result = send(sserver.getId(), ucmd, ul);	
		if (result == -1) {
			s_logger.warn("Unable to start upload of volume " + volume.getName() + " from " + sserver.getName() + " to " +url);
			ul.setDisconnected();
			ul.scheduleStatusCheck(RequestType.GET_OR_RESTART);
		}				
		
	}

	@Override
	public Long extractTemplate( VMTemplateVO template, String url,
			VMTemplateHostVO vmTemplateHost,Long dataCenterId, long eventId, long asyncJobId, AsyncJobManager asyncMgr){

		Type type = (template.getFormat() == ImageFormat.ISO) ? Type.ISO : Type.TEMPLATE ;
				
		List<HostVO> storageServers = _serverDao.listByTypeDataCenter(Host.Type.SecondaryStorage, dataCenterId);
		HostVO sserver = storageServers.get(0);			
		
		UploadVO uploadTemplateObj = new UploadVO(sserver.getId(), template.getId(), new Date(), 
													Upload.Status.NOT_UPLOADED, type, url, Mode.FTP_UPLOAD);
		_uploadDao.persist(uploadTemplateObj);        		               
        		
		if(vmTemplateHost != null) {
		    start();
			UploadCommand ucmd = new UploadCommand(template, url, vmTemplateHost.getInstallPath(), vmTemplateHost.getSize());	
			UploadListener ul = new UploadListener(sserver, _timer, _uploadDao, uploadTemplateObj, this, ucmd, template.getAccountId(), template.getName(), type, eventId, asyncJobId, asyncMgr);			
			_listenerMap.put(uploadTemplateObj, ul);

			long result = send(sserver.getId(), ucmd, ul);	
			if (result == -1) {
				s_logger.warn("Unable to start upload of " + template.getUniqueName() + " from " + sserver.getName() + " to " +url);
				ul.setDisconnected();
				ul.scheduleStatusCheck(RequestType.GET_OR_RESTART);
			}
			return uploadTemplateObj.getId();
		}		
		return null;		
	}	
	
	@Override
	public UploadVO createEntityDownloadURL(VMTemplateVO template, VMTemplateHostVO vmTemplateHost, Long dataCenterId, long eventId) {
	    
	    String errorString = "";
	    boolean success = false;
	    List<HostVO> storageServers = _serverDao.listByTypeDataCenter(Host.Type.SecondaryStorage, dataCenterId);
	    if(storageServers == null ) {
            throw new CloudRuntimeException("No Storage Server found at the datacenter - " +dataCenterId);
        }
	    
	    Type type = (template.getFormat() == ImageFormat.ISO) ? Type.ISO : Type.TEMPLATE ;
	    
	    //Check if ssvm is up
	    HostVO sserver = storageServers.get(0);
	    if(sserver.getStatus() != com.cloud.host.Status.Up){
	    	throw new CloudRuntimeException("Couldnt create extract link - Secondary Storage Vm is not up");
	    }
	    
	    //Check if it already exists.
	    List<UploadVO> extractURLList = _uploadDao.listByTypeUploadStatus(template.getId(), type, UploadVO.Status.DOWNLOAD_URL_CREATED);	    
	    if (extractURLList.size() > 0) {
            return extractURLList.get(0);
        }
	    
	    // It doesn't exist so create a DB entry.	    
	    UploadVO uploadTemplateObj = new UploadVO(sserver.getId(), template.getId(), new Date(), 
	                                                Status.DOWNLOAD_URL_NOT_CREATED, 0, type, Mode.HTTP_DOWNLOAD); 
	    uploadTemplateObj.setInstallPath(vmTemplateHost.getInstallPath());	                                                
	    _uploadDao.persist(uploadTemplateObj);
	    try{
    	    // Create Symlink at ssvm
	    	String uuid = UUID.randomUUID().toString() + ".vhd";
    	    CreateEntityDownloadURLCommand cmd = new CreateEntityDownloadURLCommand(vmTemplateHost.getInstallPath(), uuid);
    	    long result = send(sserver.getId(), cmd, null);
    	    if (result == -1){
    	        errorString = "Unable to create a link for " +type+ " id:"+template.getId();
                s_logger.error(errorString);
                throw new CloudRuntimeException(errorString);
    	    }
    	    
    	    //Construct actual URL locally now that the symlink exists at SSVM
    	    List<SecondaryStorageVmVO> ssVms = _secStorageVmDao.getSecStorageVmListInStates(dataCenterId, State.Running);
            if (ssVms.size() > 0) {
                SecondaryStorageVmVO ssVm = ssVms.get(0);
                if (ssVm.getPublicIpAddress() == null) {
                    errorString = "A running secondary storage vm has a null public ip?";
                    s_logger.error(errorString);
                    throw new CloudRuntimeException(errorString);
                }
                String extractURL = generateCopyUrl(ssVm.getPublicIpAddress(), uuid);
                UploadVO vo = _uploadDao.createForUpdate();
                vo.setLastUpdated(new Date());
                vo.setUploadUrl(extractURL);
                vo.setUploadState(Status.DOWNLOAD_URL_CREATED);
                _uploadDao.update(uploadTemplateObj.getId(), vo);
                success = true;
                return _uploadDao.findById(uploadTemplateObj.getId(), true);
            }
            errorString = "Couldnt find a running SSVM in the zone" + dataCenterId+ ". Couldnt create the extraction URL.";
            throw new CloudRuntimeException(errorString);
	    }finally{
           if(!success){
                UploadVO uploadJob = _uploadDao.createForUpdate(uploadTemplateObj.getId());
                uploadJob.setLastUpdated(new Date());
                uploadJob.setErrorString(errorString);
                uploadJob.setUploadState(Status.ERROR);
                _uploadDao.update(uploadTemplateObj.getId(), uploadJob);
            }
	    }
	    
	}
	
	@Override
    public void createVolumeDownloadURL(Long entityId, String path, Type type, Long dataCenterId, Long uploadId) {
        
	    String errorString = "";
	    boolean success = false;
	    try{
            List<HostVO> storageServers = _serverDao.listByTypeDataCenter(Host.Type.SecondaryStorage, dataCenterId);
            if(storageServers == null ){
                errorString = "No Storage Server found at the datacenter - " +dataCenterId;
                throw new CloudRuntimeException(errorString);   
            }                    
            
            // Update DB for state = DOWNLOAD_URL_NOT_CREATED.        
            UploadVO uploadJob = _uploadDao.createForUpdate(uploadId);
            uploadJob.setUploadState(Status.DOWNLOAD_URL_NOT_CREATED);
            uploadJob.setLastUpdated(new Date());
            _uploadDao.update(uploadJob.getId(), uploadJob);
            
            List<SecondaryStorageVmVO> ssVms = _secStorageVmDao.getSecStorageVmListInStates(dataCenterId, State.Running);
            if (ssVms.size() == 0){
            	errorString = "Couldnt find a running SSVM in the zone" + dataCenterId+ ". Couldnt create the extraction URL.";
                throw new CloudRuntimeException(errorString);
            }
            // Create Symlink at ssvm
            String uuid = UUID.randomUUID().toString() + ".vhd";
            CreateEntityDownloadURLCommand cmd = new CreateEntityDownloadURLCommand(path, uuid);
            long result = send(ApiDBUtils.findUploadById(uploadId).getHostId(), cmd, null);
            if (result == -1){
                errorString = "Unable to create a link for " +type+ " id:"+entityId;
                s_logger.warn(errorString);
                throw new CloudRuntimeException(errorString);
            }
            
            //Construct actual URL locally now that the symlink exists at SSVM            
            SecondaryStorageVmVO ssVm = ssVms.get(0);
            if (ssVm.getPublicIpAddress() == null) {
                errorString = "A running secondary storage vm has a null public ip?";
                s_logger.warn(errorString);
                throw new CloudRuntimeException(errorString);
            }
            String extractURL = generateCopyUrl(ssVm.getPublicIpAddress(), uuid);
            UploadVO vo = _uploadDao.createForUpdate();
            vo.setLastUpdated(new Date());
            vo.setUploadUrl(extractURL);
            vo.setUploadState(Status.DOWNLOAD_URL_CREATED);
            _uploadDao.update(uploadId, vo);
            success = true;
            return;
                        
	    }finally{
	        if(!success){
	            UploadVO uploadJob = _uploadDao.createForUpdate(uploadId);
	            uploadJob.setLastUpdated(new Date());
	            uploadJob.setErrorString(errorString);
	            uploadJob.setUploadState(Status.ERROR);
	            _uploadDao.update(uploadId, uploadJob);
	        }
	    }
    }
	
	   private String generateCopyUrl(String ipAddress, String uuid){
	        String hostname = ipAddress;
	        String scheme = "http";
	        if (_sslCopy) {
	            hostname = ipAddress.replace(".", "-");
	            hostname = hostname + ".realhostip.com";
	            scheme = "https";
	        }
	        return scheme + "://" + hostname + "/userdata/" + uuid; 
	    }
	


	public long send(Long hostId, Command cmd, Listener listener) {
		return _agentMgr.gatherStats(hostId, cmd, listener);
	}

	@Override
	public boolean configure(String name, Map<String, Object> params)
			throws ConfigurationException {
		_name = name;
        final Map<String, String> configs = _configDao.getConfiguration("ManagementServer", params);
        _sslCopy = Boolean.parseBoolean(configs.get("secstorage.encrypt.copy"));
        
        String cert = configs.get("secstorage.secure.copy.cert");
        if ("realhostip.com".equalsIgnoreCase(cert)) {
        	s_logger.warn("Only realhostip.com ssl cert is supported, ignoring self-signed and other certs");
        }        
        
        _agentMgr.registerForHostEvents(new UploadListener(this), true, false, false);
        String cleanupInterval = configs.get("extract.url.cleanup.interval");
        _cleanupInterval = NumbersUtil.parseInt(cleanupInterval, 14400);
        
        
        String workers = (String)params.get("expunge.workers");
        int wrks = NumbersUtil.parseInt(workers, 1);
        _executor = Executors.newScheduledThreadPool(wrks, new NamedThreadFactory("UploadMonitor-Scavenger"));
		return true;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public boolean start() {	    
	    _executor.scheduleWithFixedDelay(new StorageGarbageCollector(), _cleanupInterval, _cleanupInterval, TimeUnit.SECONDS);
		_timer = new Timer();
		return true;
	}

	@Override
	public boolean stop() {		
		return true;
	}
	
	public void handleUploadEvent(HostVO host, Long accountId, String typeName, Type type, Long uploadId, com.cloud.storage.Upload.Status reason, long eventId) {
		
		if ((reason == Upload.Status.UPLOADED) || (reason==Upload.Status.ABANDONED)){
			UploadVO uploadObj = new UploadVO(uploadId);
			UploadListener oldListener = _listenerMap.get(uploadObj);
			if (oldListener != null) {
				_listenerMap.remove(uploadObj);
			}
		}

	}
	
	@Override
	public void handleUploadSync(long sserverId) {
	    
	    HostVO storageHost = _serverDao.findById(sserverId);
        if (storageHost == null) {
            s_logger.warn("Huh? Agent id " + sserverId + " does not correspond to a row in hosts table?");
            return;
        }
        s_logger.debug("Handling upload sserverId " +sserverId);
        List<UploadVO> uploadsInProgress = new ArrayList<UploadVO>();
        uploadsInProgress.addAll(_uploadDao.listByHostAndUploadStatus(sserverId, UploadVO.Status.UPLOAD_IN_PROGRESS));
        uploadsInProgress.addAll(_uploadDao.listByHostAndUploadStatus(sserverId, UploadVO.Status.COPY_IN_PROGRESS));
        if (uploadsInProgress.size() > 0){
            for (UploadVO uploadJob : uploadsInProgress){
                uploadJob.setUploadState(UploadVO.Status.UPLOAD_ERROR);
                uploadJob.setErrorString("Could not complete the upload.");
                uploadJob.setLastUpdated(new Date());
                _uploadDao.update(uploadJob.getId(), uploadJob);
            }
            
        }
	        

	}				

    protected class StorageGarbageCollector implements Runnable {

        public StorageGarbageCollector() {
        }

        @Override
        public void run() {
            try {
                s_logger.info("Extract Monitor Garbage Collection Thread is running.");

                GlobalLock scanLock = GlobalLock.getInternLock("uploadmonitor.storageGC");
                try {
                    if (scanLock.lock(3)) {
                        try {
                            cleanupStorage();
                        } finally {
                            scanLock.unlock();
                        }
                    }
                } finally {
                    scanLock.releaseRef();
                }

            } catch (Exception e) {
                s_logger.error("Caught the following Exception", e);
            }
        }
    }
    
    
    private long getTimeDiff(Date date){
        Calendar currentDateCalendar = Calendar.getInstance();
        Calendar givenDateCalendar = Calendar.getInstance();
        givenDateCalendar.setTime(date);
        
        return (currentDateCalendar.getTimeInMillis() - givenDateCalendar.getTimeInMillis() )/1000;  
    }
    
    public void cleanupStorage() {

        final int EXTRACT_URL_LIFE_LIMIT_IN_SECONDS = 14400;//FIX ME make it configurable.
        List<UploadVO> extractJobs= _uploadDao.listByModeAndStatus(Mode.HTTP_DOWNLOAD, Status.DOWNLOAD_URL_CREATED);
        
        for (UploadVO extractJob : extractJobs){
            if( getTimeDiff(extractJob.getLastUpdated()) > EXTRACT_URL_LIFE_LIMIT_IN_SECONDS ){                           
                String path = extractJob.getInstallPath();
                s_logger.debug("Sending deletion of extract URL "+extractJob.getUploadUrl());
                // Would delete the symlink for the Type and if Type == VOLUME then also the volume
                DeleteEntityDownloadURLCommand cmd = new DeleteEntityDownloadURLCommand(path, extractJob.getType(),extractJob.getUploadUrl());            
                long result = send(extractJob.getHostId(), cmd, null);
                if (result == -1){
                    s_logger.warn("Unable to delete the link for " +extractJob.getType()+ " id=" +extractJob.getTypeId()+ " url="+extractJob.getUploadUrl());
                }else{
                    _uploadDao.remove(extractJob.getId());
                }
            }
        }
                
    }
	
}
