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

package com.cloud.event.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.event.UsageEventVO;
import com.cloud.exception.UsageServerException;
import com.cloud.utils.DateUtil;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;

@Local(value={UsageEventDao.class})
public class UsageEventDaoImpl extends GenericDaoBase<UsageEventVO, Long> implements UsageEventDao {
    public static final Logger s_logger = Logger.getLogger(UsageEventDaoImpl.class.getName());

    private final SearchBuilder<UsageEventVO> latestEventsSearch;
    private static final String COPY_EVENTS = "INSERT INTO cloud_usage.usage_event (id, type, account_id, created, zone_id, resource_id, resource_name, offering_id, template_id, size, resource_type) " +
    		"SELECT id, type, account_id, created, zone_id, resource_id, resource_name, offering_id, template_id, size, resource_type FROM cloud.usage_event vmevt WHERE vmevt.id > ? and vmevt.id <= ? ";
    private static final String COPY_ALL_EVENTS = "INSERT INTO cloud_usage.usage_event (id, type, account_id, created, zone_id, resource_id, resource_name, offering_id, template_id, size, resource_type) " +
    		"SELECT id, type, account_id, created, zone_id, resource_id, resource_name, offering_id, template_id, size, resource_type FROM cloud.usage_event vmevt WHERE vmevt.id <= ?";
    private static final String MAX_EVENT = "select max(id) from cloud.usage_event where created <= ?";


    public UsageEventDaoImpl () {
        latestEventsSearch = createSearchBuilder();
        latestEventsSearch.and("processed", latestEventsSearch.entity().isProcessed(), SearchCriteria.Op.EQ);
        latestEventsSearch.and("enddate", latestEventsSearch.entity().getCreateDate(), SearchCriteria.Op.LTEQ);
        latestEventsSearch.done();
    }

    @Override
    public List<UsageEventVO> listLatestEvents(Date endDate) {
        Filter filter = new Filter(UsageEventVO.class, "createDate", Boolean.TRUE, null, null);
        SearchCriteria<UsageEventVO> sc = latestEventsSearch.create();
        sc.setParameters("processed", false);
        sc.setParameters("enddate", endDate);
        return listBy(sc, filter);
    }

    @Override
    public List<UsageEventVO> getLatestEvent() {
        Filter filter = new Filter(UsageEventVO.class, "id", Boolean.FALSE, Long.valueOf(0), Long.valueOf(1));
        return listAll(filter);
    }
    
    @Override
    @DB
    public synchronized List<UsageEventVO> getRecentEvents(Date endDate) throws UsageServerException {
        long recentEventId = getMostRecentEventId();
        long maxEventId = getMaxEventId(endDate);
        Transaction txn = Transaction.open(Transaction.USAGE_DB);
        String sql = COPY_EVENTS;
        if (recentEventId == 0) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("no recent event date, copying all events");
            }
            sql = COPY_ALL_EVENTS;
        }

        PreparedStatement pstmt = null;
        try {
            txn.start();
            pstmt = txn.prepareAutoCloseStatement(sql);
            int i = 1;
            if (recentEventId != 0) {
                pstmt.setLong(i++, recentEventId);
            }
            pstmt.setLong(i++, maxEventId);
            pstmt.executeUpdate();
            txn.commit();
            return findRecentEvents(endDate);
        } catch (Exception ex) {
            txn.rollback();
            s_logger.error("error copying events from cloud db to usage db", ex);
            throw new UsageServerException(ex.getMessage());
        } finally {
            txn.close();
        }
    }

    @DB
    private long getMostRecentEventId() throws UsageServerException {
        Transaction txn = Transaction.open(Transaction.USAGE_DB);
        try {
            List<UsageEventVO> latestEvents = getLatestEvent();

            if(latestEvents !=null && latestEvents.size() == 1){
                UsageEventVO latestEvent = latestEvents.get(0);
                if(latestEvent != null){
                    return latestEvent.getId();
                }
            }
            return 0;
        } catch (Exception ex) {
            s_logger.error("error getting most recent event id", ex);
            throw new UsageServerException(ex.getMessage());
        } finally {
            txn.close();
        }
    }

    private List<UsageEventVO> findRecentEvents(Date endDate) throws UsageServerException {
        Transaction txn = Transaction.open(Transaction.USAGE_DB);
        try {
            return listLatestEvents(endDate);
        } catch (Exception ex) {
            s_logger.error("error getting most recent event date", ex);
            throw new UsageServerException(ex.getMessage());
        } finally {
            txn.close();
        }
    }
    
    private long getMaxEventId(Date endDate) throws UsageServerException {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        try {
            String sql = MAX_EVENT;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setString(1, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Long.valueOf(rs.getLong(1));
            }
            return 0;
        } catch (Exception ex) {
            s_logger.error("error getting max event id", ex);
            throw new UsageServerException(ex.getMessage());
        } finally {
            txn.close();
        }
    }
}
