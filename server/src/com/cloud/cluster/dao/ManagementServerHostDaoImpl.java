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

package com.cloud.cluster.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.cluster.ClusterInvalidSessionException;
import com.cloud.cluster.ManagementServerHost;
import com.cloud.cluster.ManagementServerHost.State;
import com.cloud.cluster.ManagementServerHostVO;
import com.cloud.utils.DateUtil;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;

@Local(value={ManagementServerHostDao.class})
public class ManagementServerHostDaoImpl extends GenericDaoBase<ManagementServerHostVO, Long> implements ManagementServerHostDao {
    private static final Logger s_logger = Logger.getLogger(ManagementServerHostDaoImpl.class);
    
    private final SearchBuilder<ManagementServerHostVO> MsIdSearch;
    private final SearchBuilder<ManagementServerHostVO> ActiveSearch;
    private final SearchBuilder<ManagementServerHostVO> InactiveSearch;
    private final SearchBuilder<ManagementServerHostVO> StateSearch;

	@Override
    public void invalidateRunSession(long id, long runid) {
	    Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement("update mshost set runid=0, state='Down' where id=? and runid=?");
            pstmt.setLong(1, id);
            pstmt.setLong(2, runid);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
        	throw new CloudRuntimeException("DB exception on " + pstmt.toString(), e);
        }
	}
	
	@Override
    public ManagementServerHostVO findByMsid(long msid) {
        SearchCriteria<ManagementServerHostVO> sc = MsIdSearch.create();
        sc.setParameters("msid", msid);
		
		List<ManagementServerHostVO> l = listIncludingRemovedBy(sc);
		if(l != null && l.size() > 0) {
            return l.get(0);
        }
		 
		return null;
	}
	
	@Override
    @DB
	public void update(long id, long runid, String name, String version, String serviceIP, int servicePort, Date lastUpdate) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        try {
            txn.start();
            
            pstmt = txn.prepareAutoCloseStatement("update mshost set name=?, version=?, service_ip=?, service_port=?, last_update=?, removed=null, alert_count=0, runid=?, state=? where id=?");
            pstmt.setString(1, name);
            pstmt.setString(2, version);
            pstmt.setString(3, serviceIP);
            pstmt.setInt(4, servicePort);
            pstmt.setString(5, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), lastUpdate));
            pstmt.setLong(6, runid);
            pstmt.setString(7, ManagementServerHost.State.Up.toString());
            pstmt.setLong(8, id);
            
            pstmt.executeUpdate();
            txn.commit();
        } catch(Exception e) {
            s_logger.warn("Unexpected exception, ", e);
        }
	}
	
	@Override
    @DB
    public boolean remove(Long id) {
        Transaction txn = Transaction.currentTxn();
    
        try {
        	txn.start();
        	
        	ManagementServerHostVO msHost = findById(id);
        	msHost.setState(ManagementServerHost.State.Down);
        	super.remove(id);
        	
        	txn.commit();
        	return true;
        } catch(Exception e) {
            s_logger.warn("Unexpected exception, ", e);
        }
        
        return false;
    }

	@Override
    @DB
	public void update(long id, long runid, Date lastUpdate) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        try {
            txn.start();
            
            pstmt = txn.prepareAutoCloseStatement("update mshost set last_update=?, removed=null, alert_count=0 where id=? and runid=?");
            pstmt.setString(1, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), lastUpdate));
            pstmt.setLong(2, id);
            pstmt.setLong(3, runid);
            
            int count = pstmt.executeUpdate();
            txn.commit();

            if(count < 1) {
                throw new CloudRuntimeException("Invalid cluster session detected", new ClusterInvalidSessionException("runid " + runid + " is no longer valid"));
            }
        } catch(Exception e) {
            s_logger.warn("Unexpected exception, ", e);
        }
	}
	
	@Override
    public List<ManagementServerHostVO> getActiveList(Date cutTime) {
	    SearchCriteria<ManagementServerHostVO> sc = ActiveSearch.create();
	    sc.setParameters("lastUpdateTime", cutTime);
	    
	    return listIncludingRemovedBy(sc);
	}

	@Override
    public List<ManagementServerHostVO> getInactiveList(Date cutTime) {
	    SearchCriteria<ManagementServerHostVO> sc = InactiveSearch.create();
	    sc.setParameters("lastUpdateTime", cutTime);
	    
	    return listIncludingRemovedBy(sc);
	}
	
	@Override
    @DB
	public int increaseAlertCount(long id) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        int changedRows = 0;
        try {
            txn.start();
            
            pstmt = txn.prepareAutoCloseStatement("update mshost set alert_count=alert_count+1 where id=? and alert_count=0");
            pstmt.setLong(1, id);
            
            changedRows = pstmt.executeUpdate();
            txn.commit();
        } catch(Exception e) {
            s_logger.warn("Unexpected exception, ", e);
            txn.rollback();
        }
        
        return changedRows;
	}
	
	protected ManagementServerHostDaoImpl() {
		MsIdSearch = createSearchBuilder();
		MsIdSearch.and("msid",  MsIdSearch.entity().getMsid(), SearchCriteria.Op.EQ);
		MsIdSearch.done();
		
	    ActiveSearch = createSearchBuilder();
	    ActiveSearch.and("lastUpdateTime", ActiveSearch.entity().getLastUpdateTime(),  SearchCriteria.Op.GT);
	    ActiveSearch.and("removed", ActiveSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
	    ActiveSearch.done();

	    InactiveSearch = createSearchBuilder();
	    InactiveSearch.and("lastUpdateTime", InactiveSearch.entity().getLastUpdateTime(),  SearchCriteria.Op.LTEQ);
	    InactiveSearch.and("removed", InactiveSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
	    InactiveSearch.done();
	    
	    StateSearch = createSearchBuilder();
	    StateSearch.and("state", StateSearch.entity().getState(), SearchCriteria.Op.IN);
	    StateSearch.done();
	}
	
	
	@Override
    public void update(long id, long runId, State state, Date lastUpdate) {
	    Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement("update mshost set state=?, last_update=? where id=? and runid=?");
            pstmt.setString(1, state.toString());
            pstmt.setString(2, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), lastUpdate));
            pstmt.setLong(3, id);
            pstmt.setLong(4, runId);
            
            int count = pstmt.executeUpdate();
            
            if(count < 1) {
                throw new CloudRuntimeException("Invalid cluster session detected", new ClusterInvalidSessionException("runid " + runId + " is no longer valid"));
            }
        } catch (SQLException e) {
            throw new CloudRuntimeException("DB exception on " + pstmt.toString(), e);
        }
    }
	
	@Override
	public List<ManagementServerHostVO> listBy(ManagementServerHost.State...states) {
	    SearchCriteria<ManagementServerHostVO> sc = StateSearch.create();

        sc.setParameters("state", (Object[]) states);
        
        return listBy(sc);
	}
	
	@Override
	public List<Long> listOrphanMsids() {
		List<Long> orphanList = new ArrayList<Long>();
		
	    Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement(
            	"select t.mgmt_server_id from (select mgmt_server_id, count(*) as count from host group by mgmt_server_id) as t WHERE t.count > 0 AND t.mgmt_server_id NOT IN (select msid from mshost)");

            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
            	orphanList.add(rs.getLong(1));
            }
        } catch (SQLException e) {
            throw new CloudRuntimeException("DB exception on " + pstmt.toString(), e);
        }
        
        return orphanList;
	}
}
