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
package com.cloud.storage.snapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.api.ApiDispatcher;
import com.cloud.api.commands.CreateSnapshotCmd;
import com.cloud.async.AsyncJobManager;
import com.cloud.async.AsyncJobResult;
import com.cloud.async.AsyncJobVO;
import com.cloud.async.dao.AsyncJobDao;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.event.EventTypes;
import com.cloud.event.EventUtils;
import com.cloud.serializer.GsonHelper;
import com.cloud.storage.Snapshot;
import com.cloud.storage.SnapshotPolicyVO;
import com.cloud.storage.SnapshotScheduleVO;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.dao.SnapshotPolicyDao;
import com.cloud.storage.dao.SnapshotScheduleDao;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.user.Account;
import com.cloud.user.User;
import com.cloud.utils.DateUtil;
import com.cloud.utils.DateUtil.IntervalType;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.concurrency.TestClock;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.SearchCriteria;

/**
 *
 */
@Local(value={SnapshotScheduler.class})
public class SnapshotSchedulerImpl implements SnapshotScheduler {
    private static final Logger s_logger = Logger.getLogger(SnapshotSchedulerImpl.class);
    
    private String _name = null;
    @Inject protected AsyncJobDao             _asyncJobDao;
    @Inject protected SnapshotDao             _snapshotDao;
    @Inject protected SnapshotScheduleDao     _snapshotScheduleDao;
    @Inject protected SnapshotPolicyDao       _snapshotPolicyDao;
    @Inject protected AsyncJobManager         _asyncMgr;
    @Inject protected SnapshotManager         _snapshotManager;
    @Inject protected StoragePoolHostDao      _poolHostDao;
    @Inject protected VolumeDao               _volsDao;
    
    private static final int ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_COOPERATION = 5;    // 5 seconds
    private int        _snapshotPollInterval;
    private Timer      _testClockTimer;
    private Date       _currentTimestamp;
    private TestClock  _testTimerTask;
    
    private Date getNextScheduledTime(long policyId, Date currentTimestamp) {
        SnapshotPolicyVO policy = _snapshotPolicyDao.findById(policyId);
        Date nextTimestamp = null;
        if (policy != null) {
            short intervalType = policy.getInterval();
            IntervalType type = DateUtil.getIntervalType(intervalType);
            String schedule = policy.getSchedule();
            String timezone = policy.getTimezone();
            nextTimestamp = DateUtil.getNextRunTime(type, schedule, timezone, currentTimestamp);
            String currentTime = DateUtil.displayDateInTimezone(DateUtil.GMT_TIMEZONE, currentTimestamp);
            String nextScheduledTime = DateUtil.displayDateInTimezone(DateUtil.GMT_TIMEZONE, nextTimestamp);
            s_logger.debug("Current time is " + currentTime + ". NextScheduledTime of policyId " + policyId + " is " + nextScheduledTime);
        }
        return nextTimestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void poll(Date currentTimestamp) {
        // We don't maintain the time. The timer task does.
        _currentTimestamp = currentTimestamp;
        
        GlobalLock scanLock = GlobalLock.getInternLock("snapshot.poll");
        try {
            if(scanLock.lock(ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_COOPERATION)) {
                try {
                    checkStatusOfCurrentlyExecutingSnapshots();
                } finally {
                    scanLock.unlock();
                }
            }
        } finally {
            scanLock.releaseRef();
        }
        
        scanLock = GlobalLock.getInternLock("snapshot.poll");
        try {
            if(scanLock.lock(ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_COOPERATION)) {
                try {
                    scheduleSnapshots();
                } finally {
                    scanLock.unlock();
                }
            }
        } finally {
            scanLock.releaseRef();
        } 
    }
    
    private void checkStatusOfCurrentlyExecutingSnapshots() {
        SearchCriteria<SnapshotScheduleVO> sc = _snapshotScheduleDao.createSearchCriteria();
        sc.addAnd("asyncJobId", SearchCriteria.Op.NNULL);
        List<SnapshotScheduleVO> snapshotSchedules = _snapshotScheduleDao.search(sc, null);
        for (SnapshotScheduleVO snapshotSchedule : snapshotSchedules) {
            Long asyncJobId = snapshotSchedule.getAsyncJobId();
            AsyncJobVO asyncJob = _asyncJobDao.findById(asyncJobId);
            switch (asyncJob.getStatus()) {
            case AsyncJobResult.STATUS_SUCCEEDED:
                // The snapshot has been successfully backed up.
                // The snapshot state has also been cleaned up.
                // We can schedule the next job for this snapshot.
                // Remove the existing entry in the snapshot_schedule table.
                scheduleNextSnapshotJob(snapshotSchedule);
                break;
            case AsyncJobResult.STATUS_FAILED:
                // Check the snapshot status.
                Long snapshotId = snapshotSchedule.getSnapshotId();
                if (snapshotId == null) {
                    // createSnapshotAsync exited, successfully or unsuccessfully,
                    // even before creating a snapshot record
                    // No cleanup needs to be done.
                    // Schedule the next snapshot.
                    scheduleNextSnapshotJob(snapshotSchedule);
                }
                else {
                    SnapshotVO snapshot = _snapshotDao.findById(snapshotId);
                    if (snapshot == null || snapshot.getRemoved() != null) {
                        // This snapshot has been deleted successfully from the primary storage
                        // Again no cleanup needs to be done.
                        // Schedule the next snapshot.
                        // There's very little probability that the code reaches this point.
                        // The snapshotId is a foreign key for the snapshot_schedule table
                        // set to ON DELETE CASCADE. So if the snapshot entry is deleted, the snapshot_schedule entry will be too.
                        // But what if it has only been marked as removed?
                        scheduleNextSnapshotJob(snapshotSchedule);
                    }
                    else {
                        // The management server executing this snapshot job appears to have crashed
                        // while creating the snapshot on primary storage/or backing it up.
                        // We have no idea whether the snapshot was successfully taken on the primary or not.
                        // Schedule the next snapshot job.
                        // The ValidatePreviousSnapshotCommand will take appropriate action on this snapshot
                        // If the snapshot was taken successfully on primary, it will retry backing it up.
                        // and cleanup the previous snapshot
                        // Set the userId to that of system.
                        //_snapshotManager.validateSnapshot(1L, snapshot);
                        // In all cases, schedule the next snapshot job 
                        scheduleNextSnapshotJob(snapshotSchedule);
                    }
                }
 
                break;
            case AsyncJobResult.STATUS_IN_PROGRESS:
                // There is no way of knowing from here whether 
                // 1) Another management server is processing this snapshot job
                // 2) The management server has crashed and this snapshot is lying 
                // around in an inconsistent state.
                // Hopefully, this can be resolved at the backend when the current snapshot gets executed.
                // But if it remains in this state, the current snapshot will not get executed. 
                // And it will remain in stasis.
                break;
            }
        }
    }

    @DB
    protected void scheduleSnapshots() {
        String displayTime = DateUtil.displayDateInTimezone(DateUtil.GMT_TIMEZONE, _currentTimestamp);
        s_logger.debug("Snapshot scheduler.poll is being called at " + displayTime);
        
        List<SnapshotScheduleVO> snapshotsToBeExecuted = _snapshotScheduleDao.getSchedulesToExecute(_currentTimestamp);
        s_logger.debug("Got " + snapshotsToBeExecuted.size() + " snapshots to be executed at " + displayTime);

        // This is done for recurring snapshots, which are executed by the system automatically
        // Hence set user id to that of system
        long userId = 1;

        for (SnapshotScheduleVO snapshotToBeExecuted : snapshotsToBeExecuted) {
            long policyId = snapshotToBeExecuted.getPolicyId();
            long volumeId = snapshotToBeExecuted.getVolumeId();
            VolumeVO volume = _volsDao.findById(volumeId);
            if ( volume.getPoolId() == null) {
                // this volume is not attached
                continue;
            }
            if ( _snapshotPolicyDao.findById(policyId) == null ) {
                _snapshotScheduleDao.remove(snapshotToBeExecuted.getId());
            }
            if (s_logger.isDebugEnabled()) {
                Date scheduledTimestamp = snapshotToBeExecuted.getScheduledTimestamp();
                displayTime = DateUtil.displayDateInTimezone(DateUtil.GMT_TIMEZONE, scheduledTimestamp);
                s_logger.debug("Scheduling 1 snapshot for volume " + volumeId + " for schedule id: "
                        + snapshotToBeExecuted.getId() + " at " + displayTime);
            }
            long snapshotScheId = snapshotToBeExecuted.getId();
            SnapshotScheduleVO tmpSnapshotScheduleVO = null;
            try {
                tmpSnapshotScheduleVO = _snapshotScheduleDao.acquireInLockTable(snapshotScheId);

                Long eventId = EventUtils.saveScheduledEvent(User.UID_SYSTEM, Account.ACCOUNT_ID_SYSTEM,
                        EventTypes.EVENT_SNAPSHOT_CREATE, "creating snapshot for volume Id:"+volumeId,0);

                Map<String, String> params = new HashMap<String, String>();
                params.put("volumeid", ""+volumeId);
                params.put("policyid", ""+policyId);
                params.put("ctxUserId", "1");
                params.put("ctxAccountId", "1");
                params.put("ctxStartEventId", String.valueOf(eventId));
                
                CreateSnapshotCmd cmd = new CreateSnapshotCmd();
                ApiDispatcher.getInstance().dispatchCreateCmd(cmd, params);
                params.put("id", ""+cmd.getEntityId());
                params.put("ctxStartEventId", "1");
                
                AsyncJobVO job = new AsyncJobVO();
                job.setUserId(userId);
                // Just have SYSTEM own the job for now.  Users won't be able to see this job, but
                // it's an internal job so probably not a huge deal.
                job.setAccountId(1L);
                job.setCmd(CreateSnapshotCmd.class.getName());
                job.setInstanceId(cmd.getEntityId());
                job.setCmdInfo(GsonHelper.getBuilder().create().toJson(params));

                long jobId = _asyncMgr.submitAsyncJob(job);

                tmpSnapshotScheduleVO.setAsyncJobId(jobId);
                _snapshotScheduleDao.update(snapshotScheId, tmpSnapshotScheduleVO);
            } finally {
                if (tmpSnapshotScheduleVO != null) {
                    _snapshotScheduleDao.releaseFromLockTable(snapshotScheId);
                }
            }
        }
    }

    private Date scheduleNextSnapshotJob(SnapshotScheduleVO snapshotSchedule) {
        if ( snapshotSchedule == null ) {
            return null;
        }
        Long policyId = snapshotSchedule.getPolicyId();
        if (policyId.longValue() == Snapshot.MANUAL_POLICY_ID) {
            // Don't need to schedule the next job for this.
            return null;
        }
        SnapshotPolicyVO snapshotPolicy = _snapshotPolicyDao.findById(policyId);
        if ( snapshotPolicy == null ) {
            _snapshotScheduleDao.expunge(snapshotSchedule.getId());
        }
        return scheduleNextSnapshotJob(snapshotPolicy);
    }
    
    @Override @DB
    public Date scheduleNextSnapshotJob(SnapshotPolicyVO policy) {
        if ( policy == null) {
            return null;
        }
        long policyId = policy.getId();
        if ( policyId == Snapshot.MANUAL_POLICY_ID ) {
            return null;
        }
        Date nextSnapshotTimestamp = getNextScheduledTime(policyId, _currentTimestamp);
        SnapshotScheduleVO spstSchedVO = _snapshotScheduleDao.findOneByVolumePolicy(policy.getVolumeId(), policy.getId());
        if ( spstSchedVO == null ) {
            spstSchedVO = new SnapshotScheduleVO(policy.getVolumeId(), policyId, nextSnapshotTimestamp);
            _snapshotScheduleDao.persist(spstSchedVO);
        } else {
            try{
                spstSchedVO = _snapshotScheduleDao.acquireInLockTable(spstSchedVO.getId());
                spstSchedVO.setPolicyId(policyId);
                spstSchedVO.setScheduledTimestamp(nextSnapshotTimestamp);
                spstSchedVO.setAsyncJobId(null);
                spstSchedVO.setSnapshotId(null);
                _snapshotScheduleDao.update(spstSchedVO.getId(), spstSchedVO);
            } finally {
                if(spstSchedVO != null ) {
                    _snapshotScheduleDao.releaseFromLockTable(spstSchedVO.getId());
                }
            }
        }
        return nextSnapshotTimestamp;
    }
    
 
    
    @Override @DB
    public boolean removeSchedule(Long volumeId, Long policyId) {
        // We can only remove schedules which are in the future. Not which are already executed in the past.
        SnapshotScheduleVO schedule = _snapshotScheduleDao.getCurrentSchedule(volumeId, policyId, false);
        boolean success = true;
        if (schedule != null) {
            success = _snapshotScheduleDao.remove(schedule.getId());
        }
        if(!success){
            s_logger.debug("Error while deleting Snapshot schedule with Id: "+schedule.getId());
        }
        return success;
    }


    @Override
    public boolean configure(String name, Map<String, Object> params)
    throws ConfigurationException {
        _name = name;

        ComponentLocator locator = ComponentLocator.getCurrentLocator();
        
        ConfigurationDao configDao = locator.getDao(ConfigurationDao.class);
        if (configDao == null) {
            s_logger.error("Unable to get the configuration dao. " + ConfigurationDao.class.getName());
            return false;
        }
        _snapshotPollInterval = NumbersUtil.parseInt(configDao.getValue("snapshot.poll.interval"), 300);
        boolean snapshotsRecurringTest = Boolean.parseBoolean(configDao.getValue("snapshot.recurring.test"));
        if (snapshotsRecurringTest) {
            // look for some test values in the configuration table so that snapshots can be taken more frequently (QA test code)
            int minutesPerHour = NumbersUtil.parseInt(configDao.getValue("snapshot.test.minutes.per.hour"), 60);
            int hoursPerDay = NumbersUtil.parseInt(configDao.getValue("snapshot.test.hours.per.day"), 24);
            int daysPerWeek = NumbersUtil.parseInt(configDao.getValue("snapshot.test.days.per.week"), 7);
            int daysPerMonth = NumbersUtil.parseInt(configDao.getValue("snapshot.test.days.per.month"), 30);
            int weeksPerMonth = NumbersUtil.parseInt(configDao.getValue("snapshot.test.weeks.per.month"), 4);
            int monthsPerYear = NumbersUtil.parseInt(configDao.getValue("snapshot.test.months.per.year"), 12);
    
            _testTimerTask = new TestClock(this, minutesPerHour, hoursPerDay, daysPerWeek, daysPerMonth, weeksPerMonth, monthsPerYear);
        }
        _currentTimestamp = new Date();
        s_logger.info("Snapshot Scheduler is configured.");
       
        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override @DB
    public boolean start() {
        // reschedule all policies after management restart
        List<SnapshotPolicyVO> policyInstances = _snapshotPolicyDao.listAll();
        for( SnapshotPolicyVO policyInstance : policyInstances) {
            if( policyInstance.getId() != Snapshot.MANUAL_POLICY_ID ) {
                scheduleNextSnapshotJob(policyInstance);
            }
        }
        if (_testTimerTask != null) {
            _testClockTimer = new Timer("TestClock");
            // Run the test clock every 60s. Because every tick is counted as 1 minute.
            // Else it becomes too confusing.
            _testClockTimer.schedule(_testTimerTask, 100*1000L, 60*1000L);
        }
        else {
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    Date currentTimestamp = new Date();
                    poll(currentTimestamp);
                }
            };
            _testClockTimer = new Timer("SnapshotPollTask");
            _testClockTimer.schedule(timerTask, _snapshotPollInterval*1000L, _snapshotPollInterval*1000L);
        }
        
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }
}
