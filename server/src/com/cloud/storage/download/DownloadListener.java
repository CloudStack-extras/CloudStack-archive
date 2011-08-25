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
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.agent.api.StartupSecondaryStorageCommand;
import com.cloud.agent.api.StartupStorageCommand;
import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.agent.api.storage.DownloadCommand;
import com.cloud.agent.api.storage.DownloadProgressCommand;
import com.cloud.agent.api.storage.DownloadProgressCommand.RequestType;
import com.cloud.exception.ConnectionException;
import com.cloud.host.HostVO;
import com.cloud.storage.Storage;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateHostDao;
import com.cloud.storage.download.DownloadState.DownloadEvent;
import com.cloud.utils.exception.CloudRuntimeException;

/**
 * Monitor progress of template download to a single storage server
 * @author chiradeep
 *
 */
public class DownloadListener implements Listener {


	private static final class StatusTask extends TimerTask {
		private final DownloadListener dl;
		private final RequestType reqType;
		
		public StatusTask( DownloadListener dl,  RequestType req) {
			this.reqType = req;
			this.dl = dl;
		}

		@Override
		public void run() {
		  dl.sendCommand(reqType);

		}
	}
	
	private static final class TimeoutTask extends TimerTask {
		private final DownloadListener dl;
		
		public TimeoutTask( DownloadListener dl) {
			this.dl = dl;
		}

		@Override
		public void run() {
		  dl.checkProgress();
		}
	}


	public static final Logger s_logger = Logger.getLogger(DownloadListener.class.getName());
	public static final int SMALL_DELAY = 100;
	public static final long STATUS_POLL_INTERVAL = 30000L;
	
	public static final String DOWNLOADED=Status.DOWNLOADED.toString();
	public static final String NOT_DOWNLOADED=Status.NOT_DOWNLOADED.toString();
	public static final String DOWNLOAD_ERROR=Status.DOWNLOAD_ERROR.toString();
	public static final String DOWNLOAD_IN_PROGRESS=Status.DOWNLOAD_IN_PROGRESS.toString();
	public static final String DOWNLOAD_ABANDONED=Status.ABANDONED.toString();


	private HostVO sserver;
	private HostVO ssAgent;
	private VMTemplateVO template;
	
	private boolean downloadActive = true;

	private VMTemplateHostDao vmTemplateHostDao;

	private final DownloadMonitorImpl downloadMonitor;
	
	private DownloadState currState;
	
	private DownloadCommand cmd;

	private Timer timer;

	private StatusTask statusTask;
	private TimeoutTask timeoutTask;
	private Date lastUpdated = new Date();
	private String jobId;
	
	private final Map<String,  DownloadState> stateMap = new HashMap<String, DownloadState>();
	private Long templateHostId;
	
	public DownloadListener(HostVO ssAgent, HostVO host, VMTemplateVO template, Timer _timer, VMTemplateHostDao dao, Long templHostId, DownloadMonitorImpl downloadMonitor, DownloadCommand cmd) {
	    this.ssAgent = ssAgent;
        this.sserver = host;
		this.template = template;
		this.vmTemplateHostDao = dao;
		this.downloadMonitor = downloadMonitor;
		this.cmd = cmd;
		this.templateHostId = templHostId;
		initStateMachine();
		this.currState=getState(Status.NOT_DOWNLOADED.toString());
		this.timer = _timer;
		this.timeoutTask = new TimeoutTask(this);
		this.timer.schedule(timeoutTask, 3*STATUS_POLL_INTERVAL);
		updateDatabase(Status.NOT_DOWNLOADED, "");
	}
	
	public void setCurrState(VMTemplateHostVO.Status currState) {
		this.currState = getState(currState.toString());
	}

	private void initStateMachine() {
		stateMap.put(Status.NOT_DOWNLOADED.toString(), new NotDownloadedState(this));
		stateMap.put(Status.DOWNLOADED.toString(), new DownloadCompleteState(this));
		stateMap.put(Status.DOWNLOAD_ERROR.toString(), new DownloadErrorState(this));
		stateMap.put(Status.DOWNLOAD_IN_PROGRESS.toString(), new DownloadInProgressState(this));
		stateMap.put(Status.ABANDONED.toString(), new DownloadAbandonedState(this));
	}
	
	private DownloadState getState(String stateName) {
		return stateMap.get(stateName);
	}

	public void sendCommand(RequestType reqType) {
		if (getJobId() != null) {
			if (s_logger.isTraceEnabled()) {
				log("Sending progress command ", Level.TRACE);
			}
			long sent = downloadMonitor.send(ssAgent.getId(), new DownloadProgressCommand(getCommand(), getJobId(), reqType), this);
			if (sent == -1) {
				setDisconnected();
			}
		}

	}

	public void checkProgress() {
		transition(DownloadEvent.TIMEOUT_CHECK, null);
	}

	public void setDisconnected() {
		transition(DownloadEvent.DISCONNECT, null);
	}

	public void logDisconnect() {
		s_logger.warn("Unable to monitor download progress of " + template.getName() + " at host " + sserver.getName());
	}

	public synchronized void updateDatabase(Status state, String errorString) {
	    VMTemplateHostVO vo = vmTemplateHostDao.createForUpdate();
		vo.setDownloadState(state);
		vo.setLastUpdated(new Date());
		vo.setErrorString(errorString);
		vmTemplateHostDao.update(getTemplateHostId(), vo);
	}
	
	public void log(String message, Level level) {
		s_logger.log(level, message + ", template=" + template.getName() + " at host " + sserver.getName());
	}

	private Long getTemplateHostId() {
		if (templateHostId == null){
			VMTemplateHostVO templHost = vmTemplateHostDao.findByHostTemplate(sserver.getId(), template.getId());
			templateHostId = templHost.getId();
		}
		return templateHostId;
	}

	public DownloadListener(DownloadMonitorImpl monitor) {
	    downloadMonitor = monitor;
	}
	
	@Override
	public boolean isRecurring() {
		return false;
	}

	
	@Override
	public boolean processAnswers(long agentId, long seq, Answer[] answers) {
		boolean processed = false;
    	if(answers != null & answers.length > 0) {
    		if(answers[0] instanceof DownloadAnswer) {
    			final DownloadAnswer answer = (DownloadAnswer)answers[0];
    			if (getJobId() == null) {
    				setJobId(answer.getJobId());
    			} else if (!getJobId().equalsIgnoreCase(answer.getJobId())){
    				return false;//TODO
    			}
    			transition(DownloadEvent.DOWNLOAD_ANSWER, answer);
    			processed = true;
    		}
    	}
        return processed;
	}
	
	private synchronized void transition(DownloadEvent event, Object evtObj) {
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

	public synchronized void updateDatabase(DownloadAnswer answer) {
        VMTemplateHostVO updateBuilder = vmTemplateHostDao.createForUpdate();
		updateBuilder.setDownloadPercent(answer.getDownloadPct());
		updateBuilder.setDownloadState(answer.getDownloadStatus());
		updateBuilder.setLastUpdated(new Date());
		updateBuilder.setErrorString(answer.getErrorString());
		updateBuilder.setJobId(answer.getJobId());
		updateBuilder.setLocalDownloadPath(answer.getDownloadPath());
		updateBuilder.setInstallPath(answer.getInstallPath());
		updateBuilder.setSize(answer.getTemplateSize());
		updateBuilder.setPhysicalSize(answer.getTemplatePhySicalSize());
		
		vmTemplateHostDao.update(getTemplateHostId(), updateBuilder);
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
	public boolean processDisconnect(long agentId, com.cloud.host.Status state) {
		setDisconnected();
		return true;
	}
	
	@Override
	public void processConnect(HostVO agent, StartupCommand cmd, boolean forRebalance) throws ConnectionException {
	    if (cmd instanceof StartupRoutingCommand) {
	        downloadMonitor.handleSysTemplateDownload(agent);
	    } else if ( cmd instanceof StartupStorageCommand) {
	        StartupStorageCommand storage = (StartupStorageCommand)cmd;
            if( storage.getResourceType() == Storage.StorageResourceType.SECONDARY_STORAGE ||  
                    storage.getResourceType() == Storage.StorageResourceType.LOCAL_SECONDARY_STORAGE  ) {
                downloadMonitor.addSystemVMTemplatesToHost(agent, storage.getTemplateInfo());
                downloadMonitor.handleTemplateSync(agent);
            }
	    } else if ( cmd instanceof StartupSecondaryStorageCommand ) {        
	        downloadMonitor.handleTemplateSync(agent.getDataCenterId());
	    }
	}

	public void setCommand(DownloadCommand _cmd) {
		this.cmd = _cmd;
	}

	public DownloadCommand getCommand() {
		return cmd;
	}

	
	public void abandon() {
		transition(DownloadEvent.ABANDON_DOWNLOAD, null);
	}

	public void setJobId(String _jobId) {
		this.jobId = _jobId;
	}

	public String getJobId() {
		return jobId;
	}

	public void scheduleStatusCheck(RequestType request) {
		if (statusTask != null) statusTask.cancel();

		statusTask = new StatusTask(this, request);
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
	
	public void scheduleImmediateStatusCheck(RequestType request) {
		if (statusTask != null) statusTask.cancel();
		statusTask = new StatusTask(this, request);
		timer.schedule(statusTask, SMALL_DELAY);
	}

	public boolean isDownloadActive() {
		return downloadActive;
	}

	public void cancelStatusTask() {
		if (statusTask != null) statusTask.cancel();
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}
	
	public void setLastUpdated() {
		lastUpdated  = new Date();
	}

	public void setDownloadInactive(Status reason) {
		downloadActive=false;
		downloadMonitor.handleDownloadEvent(sserver, template, reason);
	}

	public void cancelTimeoutTask() {
		if (timeoutTask != null) timeoutTask.cancel();
	}

	public void logDownloadStart() {
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
