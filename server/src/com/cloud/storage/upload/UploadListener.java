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

package com.cloud.storage.upload;


import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupStorageCommand;
import com.cloud.agent.api.storage.UploadAnswer;
import com.cloud.agent.api.storage.UploadCommand;
import com.cloud.agent.api.storage.UploadProgressCommand;
import com.cloud.agent.api.storage.UploadProgressCommand.RequestType;
import com.cloud.api.commands.ExtractIsoCmd;
import com.cloud.api.commands.ExtractTemplateCmd;
import com.cloud.api.commands.ExtractVolumeCmd;
import com.cloud.api.response.ExtractResponse;
import com.cloud.async.AsyncJobManager;
import com.cloud.async.AsyncJobResult;
import com.cloud.async.executor.ExtractJobResultObject;
import com.cloud.event.EventVO;
import com.cloud.host.HostVO;
import com.cloud.storage.Storage;
import com.cloud.storage.Upload.Status;
import com.cloud.storage.Upload.Type;
import com.cloud.storage.UploadVO;
import com.cloud.storage.dao.UploadDao;
import com.cloud.storage.upload.UploadState.UploadEvent;
import com.cloud.utils.exception.CloudRuntimeException;

public class UploadListener implements Listener {
	

	private static final class StatusTask extends TimerTask {
		private final UploadListener ul;
		private final RequestType reqType;
		
		public StatusTask( UploadListener ul,  RequestType req) {
			this.reqType = req;
			this.ul = ul;
		}

		@Override
		public void run() {
		  ul.sendCommand(reqType);

		}
	}
	
	private static final class TimeoutTask extends TimerTask {
		private final UploadListener ul;
		
		public TimeoutTask( UploadListener ul) {
			this.ul = ul;
		}

		@Override
		public void run() {
		  ul.checkProgress();
		}
	}

	public static final Logger s_logger = Logger.getLogger(UploadListener.class.getName());
	public static final int SMALL_DELAY = 100;
	public static final long STATUS_POLL_INTERVAL = 10000L;
	
	public static final String UPLOADED=Status.UPLOADED.toString();
	public static final String NOT_UPLOADED=Status.NOT_UPLOADED.toString();
	public static final String UPLOAD_ERROR=Status.UPLOAD_ERROR.toString();
	public static final String UPLOAD_IN_PROGRESS=Status.UPLOAD_IN_PROGRESS.toString();
	public static final String UPLOAD_ABANDONED=Status.ABANDONED.toString();
	public static final Map<String,String> responseNameMap; 
	static{
	    Map<String, String>tempMap = new HashMap<String, String>();
        tempMap.put(Type.ISO.toString(), ExtractIsoCmd.getStaticName());
        tempMap.put(Type.TEMPLATE.toString(), ExtractTemplateCmd.getStaticName());
        tempMap.put(Type.VOLUME.toString(), ExtractVolumeCmd.getStaticName());
        tempMap.put("DEFAULT","extractresponse");
        responseNameMap = Collections.unmodifiableMap(tempMap);
	}


	private HostVO sserver;	
	
	private boolean uploadActive = true;
	
	private UploadDao uploadDao;
	
	private final UploadMonitorImpl uploadMonitor;
	
	private UploadState currState;
	
	private UploadCommand cmd;

	private Timer timer;

	private StatusTask statusTask;
	private TimeoutTask timeoutTask;
	private Date lastUpdated = new Date();
	private String jobId;
	private Long accountId;
	private String typeName;
	private Type type;
	private long asyncJobId;
	private long eventId;
	private AsyncJobManager asyncMgr;
	private ExtractResponse resultObj;
	
	public AsyncJobManager getAsyncMgr() {
		return asyncMgr;
	}

	public void setAsyncMgr(AsyncJobManager asyncMgr) {
		this.asyncMgr = asyncMgr;
	}

	public long getAsyncJobId() {
		return asyncJobId;
	}

	public void setAsyncJobId(long asyncJobId) {
		this.asyncJobId = asyncJobId;
	}

	public long getEventId() {
		return eventId;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	private final Map<String,  UploadState> stateMap = new HashMap<String, UploadState>();
	private Long uploadId;	
	
	public UploadListener(HostVO host, Timer _timer, UploadDao uploadDao,
			UploadVO uploadObj, UploadMonitorImpl uploadMonitor, UploadCommand cmd,
			Long accountId, String typeName, Type type, long eventId, long asyncJobId, AsyncJobManager asyncMgr) {
		this.sserver = host;				
		this.uploadDao = uploadDao;
		this.uploadMonitor = uploadMonitor;
		this.cmd = cmd;
		this.uploadId = uploadObj.getId();
		this.accountId = accountId;
		this.typeName = typeName;
		this.type = type;
		initStateMachine();
		this.currState = getState(Status.NOT_UPLOADED.toString());
		this.timer = _timer;
		this.timeoutTask = new TimeoutTask(this);
		this.timer.schedule(timeoutTask, 3*STATUS_POLL_INTERVAL);
		this.eventId = eventId;
		this.asyncJobId = asyncJobId;
		this.asyncMgr = asyncMgr;
		this.resultObj = new ExtractResponse(uploadObj.getTypeId(), typeName, accountId, Status.NOT_UPLOADED.toString(), uploadId);
		resultObj.setResponseName(responseNameMap.get(type.toString()));
		updateDatabase(Status.NOT_UPLOADED, cmd.getUrl(),"");
	}
	
	public UploadListener(UploadMonitorImpl monitor) {
	    uploadMonitor = monitor;
	}	
	
	public void checkProgress() {
		transition(UploadEvent.TIMEOUT_CHECK, null);
	}

	@Override
	public int getTimeout() {
		return -1;
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

	public void setCommand(UploadCommand _cmd) {
		this.cmd = _cmd;
	}
	
	public void setJobId(String _jobId) {
		this.jobId = _jobId;
	}
	
	public String getJobId() {
		return jobId;
	}
	
	@Override
	public boolean processAnswers(long agentId, long seq, Answer[] answers) {
		boolean processed = false;
    	if(answers != null & answers.length > 0) {
    		if(answers[0] instanceof UploadAnswer) {
    			final UploadAnswer answer = (UploadAnswer)answers[0];
    			if (getJobId() == null) {
    				setJobId(answer.getJobId());
    			} else if (!getJobId().equalsIgnoreCase(answer.getJobId())){
    				return false;//TODO
    			}
    			transition(UploadEvent.UPLOAD_ANSWER, answer);
    			processed = true;
    		}
    	}
        return processed;
	}
	

	@Override
	public boolean processCommands(long agentId, long seq, Command[] commands) {
		return false;
	}

	@Override
	public void processConnect(HostVO agent, StartupCommand cmd, boolean forRebalance) {		
		if (!(cmd instanceof StartupStorageCommand)) {
	        return;
	    }
	   
	    long agentId = agent.getId();
	    
	    StartupStorageCommand storage = (StartupStorageCommand)cmd;
	    if (storage.getResourceType() == Storage.StorageResourceType.STORAGE_HOST ||
	    storage.getResourceType() == Storage.StorageResourceType.SECONDARY_STORAGE )
	    {
	    	uploadMonitor.handleUploadSync(agentId);
	    }
	}

	@Override
	public AgentControlAnswer processControlCommand(long agentId,
			AgentControlCommand cmd) {
		return null;
	}
	
	public void setUploadInactive(Status reason) {
		uploadActive=false;
		uploadMonitor.handleUploadEvent(sserver, accountId, typeName, type, uploadId, reason, eventId);
	}
	
	public void logUploadStart() {
		//uploadMonitor.logEvent(accountId, event, "Storage server " + sserver.getName() + " started upload of " +type.toString() + " " + typeName, EventVO.LEVEL_INFO, eventId);
	}
	
	public void cancelTimeoutTask() {
		if (timeoutTask != null) timeoutTask.cancel();
	}
	
	public void cancelStatusTask() {
		if (statusTask != null) statusTask.cancel();
	}

	@Override
	public boolean processDisconnect(long agentId, com.cloud.host.Status state) {	
		setDisconnected();
		return true;
	}

	@Override
	public boolean processTimeout(long agentId, long seq) {		
		return true;
	}
	
	private void initStateMachine() {
		stateMap.put(Status.NOT_UPLOADED.toString(), new NotUploadedState(this));
		stateMap.put(Status.UPLOADED.toString(), new UploadCompleteState(this));
		stateMap.put(Status.UPLOAD_ERROR.toString(), new UploadErrorState(this));
		stateMap.put(Status.UPLOAD_IN_PROGRESS.toString(), new UploadInProgressState(this));
		stateMap.put(Status.ABANDONED.toString(), new UploadAbandonedState(this));
	}
	
	private UploadState getState(String stateName) {
		return stateMap.get(stateName);
	}

	private synchronized void transition(UploadEvent event, Object evtObj) {
	    if (currState == null) {
	        return;
	    }
		String prevName = currState.getName();
		String nextState = currState.handleEvent(event, evtObj);
		if (nextState != null) {
			currState = getState(nextState);
			if (currState != null) {
				currState.onEntry(prevName, event, evtObj);
			} else {
				throw new CloudRuntimeException("Invalid next state: currState="+prevName+", evt="+event + ", next=" + nextState);
			}
		} else {
			throw new CloudRuntimeException("Unhandled event transition: currState="+prevName+", evt="+event);
		}
	}
	
	public Date getLastUpdated() {
		return lastUpdated;
	}
	
	public void setLastUpdated() {
		lastUpdated  = new Date();
	}
	
	public void log(String message, Level level) {
		s_logger.log(level, message + ", " + type.toString() + " = " + typeName + " at host " + sserver.getName());
	}

	public void setDisconnected() {
		transition(UploadEvent.DISCONNECT, null);
	}
	
	public void scheduleStatusCheck(com.cloud.agent.api.storage.UploadProgressCommand.RequestType getStatus) {
		if (statusTask != null) statusTask.cancel();

		statusTask = new StatusTask(this, getStatus);
		timer.schedule(statusTask, STATUS_POLL_INTERVAL);
	}

	public void scheduleTimeoutTask(long delay) {
		if (timeoutTask != null) timeoutTask.cancel();

		timeoutTask = new TimeoutTask(this);
		timer.schedule(timeoutTask, delay);
		if (s_logger.isDebugEnabled()) {
			log("Scheduling timeout at " + delay + " ms", Level.DEBUG);
		}
	}
	
	public void updateDatabase(Status state, String uploadErrorString) {
		resultObj.setResultString(uploadErrorString);
		resultObj.setState(state.toString());
		asyncMgr.updateAsyncJobAttachment(asyncJobId, type.toString(), 1L);
		asyncMgr.updateAsyncJobStatus(asyncJobId, AsyncJobResult.STATUS_IN_PROGRESS, resultObj);
		
		UploadVO vo = uploadDao.createForUpdate();
		vo.setUploadState(state);
		vo.setLastUpdated(new Date());
		vo.setErrorString(uploadErrorString);
		uploadDao.update(getUploadId(), vo);
	}
	
	public void updateDatabase(Status state, String uploadUrl,String uploadErrorString) {
		resultObj.setResultString(uploadErrorString);
		resultObj.setState(state.toString());
		asyncMgr.updateAsyncJobAttachment(asyncJobId, type.toString(), 1L);
		asyncMgr.updateAsyncJobStatus(asyncJobId, AsyncJobResult.STATUS_IN_PROGRESS, resultObj);
		
		
		UploadVO vo = uploadDao.createForUpdate();
		vo.setUploadState(state);
		vo.setLastUpdated(new Date());
		vo.setUploadUrl(uploadUrl);
		vo.setJobId(null);
		vo.setUploadPercent(0);
		vo.setErrorString(uploadErrorString);
		
		uploadDao.update(getUploadId(), vo);
	}
	
	private Long getUploadId() {
		return uploadId;
	}

	public synchronized void updateDatabase(UploadAnswer answer) {		
		
	    if(answer.getErrorString().startsWith("553")){
	        answer.setErrorString(answer.getErrorString().concat("Please check if the file name already exists."));
	    }
		resultObj.setResultString(answer.getErrorString());
		resultObj.setState(answer.getUploadStatus().toString());
		resultObj.setUploadPercent(answer.getUploadPct());
		
		if (answer.getUploadStatus() == Status.UPLOAD_IN_PROGRESS){
			asyncMgr.updateAsyncJobAttachment(asyncJobId, type.toString(), 1L);
			asyncMgr.updateAsyncJobStatus(asyncJobId, AsyncJobResult.STATUS_IN_PROGRESS, resultObj);
		}else if(answer.getUploadStatus() == Status.UPLOADED){
		    resultObj.setResultString("Success");
			asyncMgr.completeAsyncJob(asyncJobId, AsyncJobResult.STATUS_SUCCEEDED, 1, resultObj);
		}else{
			asyncMgr.completeAsyncJob(asyncJobId, AsyncJobResult.STATUS_FAILED, 2, resultObj);
		}
        UploadVO updateBuilder = uploadDao.createForUpdate();
		updateBuilder.setUploadPercent(answer.getUploadPct());
		updateBuilder.setUploadState(answer.getUploadStatus());
		updateBuilder.setLastUpdated(new Date());
		updateBuilder.setErrorString(answer.getErrorString());
		updateBuilder.setJobId(answer.getJobId());
		
		uploadDao.update(getUploadId(), updateBuilder);
	}

	public void sendCommand(RequestType reqType) {
		if (getJobId() != null) {
			if (s_logger.isTraceEnabled()) {
				log("Sending progress command ", Level.TRACE);
			}
			long sent = uploadMonitor.send(sserver.getId(), new UploadProgressCommand(getCommand(), getJobId(), reqType), this);
			if (sent == -1) {
				setDisconnected();
			}
		}
		
	}
	
	private UploadCommand getCommand() {
		return cmd;
	}

	public void logDisconnect() {
		s_logger.warn("Unable to monitor upload progress of " + typeName + " at host " + sserver.getName());
	}
	
	public void scheduleImmediateStatusCheck(RequestType request) {
		if (statusTask != null) statusTask.cancel();
		statusTask = new StatusTask(this, request);
		timer.schedule(statusTask, SMALL_DELAY);
	}

	public void setCurrState(Status uploadState) {
		this.currState = getState(currState.toString());		
	}
}
