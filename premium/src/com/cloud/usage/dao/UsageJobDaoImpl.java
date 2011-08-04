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

package com.cloud.usage.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.exception.UsageServerException;
import com.cloud.usage.UsageJobVO;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;

@Local(value={UsageJobDao.class})
public class UsageJobDaoImpl extends GenericDaoBase<UsageJobVO, Long> implements UsageJobDao {
    private static final Logger s_logger = Logger.getLogger(UsageJobDaoImpl.class.getName());

    private static final String GET_LAST_JOB_SUCCESS_DATE_MILLIS = "SELECT end_millis FROM cloud_usage.usage_job WHERE end_millis > 0 ORDER BY end_millis DESC LIMIT 1";

    @Override
    public long getLastJobSuccessDateMillis() {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        String sql = GET_LAST_JOB_SUCCESS_DATE_MILLIS;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception ex) {
            s_logger.error("error getting last usage job success date", ex);
        } finally {
            txn.close();
        }
        return 0L;
    }

    @Override
    public void updateJobSuccess(Long jobId, long startMillis, long endMillis, long execTime, boolean success) throws UsageServerException {
        Transaction txn = Transaction.open(Transaction.USAGE_DB);
        try {
            txn.start();

            UsageJobVO job = lockRow(jobId, Boolean.TRUE);
            UsageJobVO jobForUpdate = createForUpdate();
            jobForUpdate.setStartMillis(startMillis);
            jobForUpdate.setEndMillis(endMillis);
            jobForUpdate.setExecTime(execTime);
            jobForUpdate.setStartDate(new Date(startMillis));
            jobForUpdate.setEndDate(new Date(endMillis));
            jobForUpdate.setSuccess(success);
            update(job.getId(), jobForUpdate);

            txn.commit();
        } catch (Exception ex) {
            txn.rollback();
            s_logger.error("error updating job success date", ex);
            throw new UsageServerException(ex.getMessage());
        } finally {
            txn.close();
        }
    }

    @Override
    public Long checkHeartbeat(String hostname, int pid, int aggregationDuration) {
        UsageJobVO job = getNextRecurringJob();
        if (job == null) {
            return null;
        }

        if (job.getHost().equals(hostname) && (job.getPid() != null) && (job.getPid().intValue() == pid)) {
            return job.getId();
        }

        Date lastHeartbeat = job.getHeartbeat();
        if (lastHeartbeat == null) {
            return null;
        }

        long sinceLastHeartbeat = System.currentTimeMillis() - lastHeartbeat.getTime();

        // TODO:  Make this check a little smarter..but in the mean time we want the mgmt
        //        server to monitor the usage server, we need to make sure other usage
        //        servers take over as the usage job owner more aggressively.  For now
        //        this is hardcoded to 5 minutes.
        if (sinceLastHeartbeat > (5 * 60 * 1000)) {
            return job.getId();
        }
        return null;
    }

    @Override
    public UsageJobVO isOwner(String hostname, int pid) {
        Transaction txn = Transaction.open(Transaction.USAGE_DB);
        try {
            if ((hostname == null) || (pid <= 0)) {
                return null;
            }

            UsageJobVO job = getLastJob();
            if (job == null) {
                return null;
            }

            if (hostname.equals(job.getHost()) && (job.getPid() != null) && (pid == job.getPid().intValue())) {
                return job;
            }
        } finally {
            txn.close();
        }
        return null;
    }

    @Override
    public void createNewJob(String hostname, int pid, int jobType) {
        UsageJobVO newJob = new UsageJobVO();
        newJob.setHost(hostname);
        newJob.setPid(pid);
        newJob.setHeartbeat(new Date());
        newJob.setJobType(jobType);
        persist(newJob);
    }

    @Override
    public UsageJobVO getLastJob() {
        Filter filter = new Filter(UsageJobVO.class, "id", false, Long.valueOf(0), Long.valueOf(1));
        SearchCriteria<UsageJobVO> sc = createSearchCriteria();
        sc.addAnd("endMillis", SearchCriteria.Op.EQ, Long.valueOf(0));
        List<UsageJobVO> jobs = search(sc, filter);

        if ((jobs == null) || jobs.isEmpty()) {
            return null;
        }
        return jobs.get(0);
    }

    private UsageJobVO getNextRecurringJob() {
        Filter filter = new Filter(UsageJobVO.class, "id", false, Long.valueOf(0), Long.valueOf(1));
        SearchCriteria<UsageJobVO> sc = createSearchCriteria();
        sc.addAnd("endMillis", SearchCriteria.Op.EQ, Long.valueOf(0));
        sc.addAnd("jobType", SearchCriteria.Op.EQ, Integer.valueOf(UsageJobVO.JOB_TYPE_RECURRING));
        List<UsageJobVO> jobs = search(sc, filter);

        if ((jobs == null) || jobs.isEmpty()) {
            return null;
        }
        return jobs.get(0);
    }

    @Override
    public UsageJobVO getNextImmediateJob() {
        Filter filter = new Filter(UsageJobVO.class, "id", false, Long.valueOf(0), Long.valueOf(1));
        SearchCriteria<UsageJobVO> sc = createSearchCriteria();
        sc.addAnd("endMillis", SearchCriteria.Op.EQ, Long.valueOf(0));
        sc.addAnd("jobType", SearchCriteria.Op.EQ, Integer.valueOf(UsageJobVO.JOB_TYPE_SINGLE));
        sc.addAnd("scheduled", SearchCriteria.Op.EQ, Integer.valueOf(0));
        List<UsageJobVO> jobs = search(sc, filter);

        if ((jobs == null) || jobs.isEmpty()) {
            return null;
        }
        return jobs.get(0);
    }

    @Override
    public Date getLastHeartbeat() {
        Filter filter = new Filter(UsageJobVO.class, "heartbeat", false, Long.valueOf(0), Long.valueOf(1));
        SearchCriteria<UsageJobVO> sc = createSearchCriteria();
        List<UsageJobVO> jobs = search(sc, filter);

        if ((jobs == null) || jobs.isEmpty()) {
            return null;
        }
        return jobs.get(0).getHeartbeat();
    }
}
