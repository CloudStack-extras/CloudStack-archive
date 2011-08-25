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

package com.cloud.storage.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.utils.DateUtil;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;

@Local(value={VMTemplateHostDao.class})
public class VMTemplateHostDaoImpl extends GenericDaoBase<VMTemplateHostVO, Long> implements VMTemplateHostDao {
	public static final Logger s_logger = Logger.getLogger(VMTemplateHostDaoImpl.class.getName());
    @Inject
    HostDao   _hostDao;
	protected final SearchBuilder<VMTemplateHostVO> HostSearch;
	protected final SearchBuilder<VMTemplateHostVO> TemplateSearch;
	protected final SearchBuilder<VMTemplateHostVO> HostTemplateSearch;
	protected final SearchBuilder<VMTemplateHostVO> HostTemplateStateSearch;
	protected final SearchBuilder<VMTemplateHostVO> HostDestroyedSearch;
	protected final SearchBuilder<VMTemplateHostVO> TemplateStatusSearch;
	protected final SearchBuilder<VMTemplateHostVO> TemplateStatesSearch;
	protected SearchBuilder<VMTemplateHostVO> ZONE_TEMPLATE_SEARCH;
	protected SearchBuilder<VMTemplateHostVO> LOCAL_SECONDARY_STORAGE_SEARCH;

	
	protected static final String UPDATE_TEMPLATE_HOST_REF =
		"UPDATE template_host_ref SET download_state = ?, download_pct= ?, last_updated = ? "
	+   ", error_str = ?, local_path = ?, job_id = ? "
	+   "WHERE host_id = ? and type_id = ?";
			
	protected static final String DOWNLOADS_STATE_DC=
		"SELECT t.id, t.host_id, t.template_id, t.created, t.last_updated, t.job_id, " 
	+   "t.download_pct, t.size, t.physical_size, t.download_state, t.error_str, t.local_path, "
	+   "t.install_path, t.url, t.destroyed, t.is_copy FROM template_host_ref t, host h " 
	+   "where t.host_id = h.id and h.data_center_id=? "
	+	" and t.template_id=? and t.download_state = ?" ;
	
	protected static final String DOWNLOADS_STATE_DC_POD=
		"SELECT * FROM template_host_ref t, host h where t.host_id = h.id and h.data_center_id=? and h.pod_id=? "
	+	" and t.template_id=? and t.download_state=?" ;
	
	protected static final String DOWNLOADS_STATE=
		"SELECT * FROM template_host_ref t "
	+	" where t.template_id=? and t.download_state=?";
	
	public VMTemplateHostDaoImpl () {
		HostSearch = createSearchBuilder();
		HostSearch.and("host_id", HostSearch.entity().getHostId(), SearchCriteria.Op.EQ);
		HostSearch.done();
		
		TemplateSearch = createSearchBuilder();
		TemplateSearch.and("template_id", TemplateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
		TemplateSearch.and("destroyed", TemplateSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
		TemplateSearch.done();
		
		HostTemplateSearch = createSearchBuilder();
		HostTemplateSearch.and("host_id", HostTemplateSearch.entity().getHostId(), SearchCriteria.Op.EQ);
		HostTemplateSearch.and("template_id", HostTemplateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
		HostTemplateSearch.done();
		
		HostDestroyedSearch = createSearchBuilder();
		HostDestroyedSearch.and("host_id", HostDestroyedSearch.entity().getHostId(), SearchCriteria.Op.EQ);
		HostDestroyedSearch.and("destroyed", HostDestroyedSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
		HostDestroyedSearch.done();		
		
		TemplateStatusSearch = createSearchBuilder();
		TemplateStatusSearch.and("template_id", TemplateStatusSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
		TemplateStatusSearch.and("download_state", TemplateStatusSearch.entity().getDownloadState(), SearchCriteria.Op.EQ);
		TemplateStatusSearch.done();

        TemplateStatesSearch = createSearchBuilder();
        TemplateStatesSearch.and("template_id", TemplateStatesSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        TemplateStatesSearch.and("states", TemplateStatesSearch.entity().getDownloadState(), SearchCriteria.Op.IN);
        TemplateStatesSearch.done();

        HostTemplateStateSearch = createSearchBuilder();
        HostTemplateStateSearch.and("template_id", HostTemplateStateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        HostTemplateStateSearch.and("host_id", HostTemplateStateSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        HostTemplateStateSearch.and("states", HostTemplateStateSearch.entity().getDownloadState(), SearchCriteria.Op.IN);
        HostTemplateStateSearch.done();

    }
	
	@Override
	public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
	    boolean result = super.configure(name, params);
	    ZONE_TEMPLATE_SEARCH = createSearchBuilder();
	    ZONE_TEMPLATE_SEARCH.and("template_id", ZONE_TEMPLATE_SEARCH.entity().getTemplateId(), SearchCriteria.Op.EQ);
	    ZONE_TEMPLATE_SEARCH.and("state", ZONE_TEMPLATE_SEARCH.entity().getDownloadState(), SearchCriteria.Op.EQ);
	    SearchBuilder<HostVO> hostSearch = _hostDao.createSearchBuilder();
	    hostSearch.and("zone_id", hostSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
	    ZONE_TEMPLATE_SEARCH.join("tmplHost", hostSearch, hostSearch.entity().getId(), ZONE_TEMPLATE_SEARCH.entity().getHostId(), JoinBuilder.JoinType.INNER);
	    ZONE_TEMPLATE_SEARCH.done();
	    
	    LOCAL_SECONDARY_STORAGE_SEARCH = createSearchBuilder();
	    LOCAL_SECONDARY_STORAGE_SEARCH.and("template_id", LOCAL_SECONDARY_STORAGE_SEARCH.entity().getTemplateId(), SearchCriteria.Op.EQ);
	    LOCAL_SECONDARY_STORAGE_SEARCH.and("state", LOCAL_SECONDARY_STORAGE_SEARCH.entity().getDownloadState(), SearchCriteria.Op.EQ);
	    SearchBuilder<HostVO> localSecondaryHost = _hostDao.createSearchBuilder();
	    localSecondaryHost.and("private_ip_address", localSecondaryHost.entity().getPrivateIpAddress(), SearchCriteria.Op.EQ);
	    localSecondaryHost.and("state", localSecondaryHost.entity().getStatus(), SearchCriteria.Op.EQ);
	    localSecondaryHost.and("data_center_id", localSecondaryHost.entity().getDataCenterId(), SearchCriteria.Op.EQ);
	    localSecondaryHost.and("type", localSecondaryHost.entity().getType(), SearchCriteria.Op.EQ);
	    LOCAL_SECONDARY_STORAGE_SEARCH.join("host", localSecondaryHost, localSecondaryHost.entity().getId(), LOCAL_SECONDARY_STORAGE_SEARCH.entity().getHostId(), JoinBuilder.JoinType.INNER);
	    LOCAL_SECONDARY_STORAGE_SEARCH.done();
	    
	    return result;
	}
	@Override
    public void update(VMTemplateHostVO instance) {
        Transaction txn = Transaction.currentTxn();
		PreparedStatement pstmt = null;
		try {
			Date now = new Date();
			String sql = UPDATE_TEMPLATE_HOST_REF;
			pstmt = txn.prepareAutoCloseStatement(sql);
			pstmt.setString(1, instance.getDownloadState().toString());
			pstmt.setInt(2, instance.getDownloadPercent());
			pstmt.setString(3, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), now));
			pstmt.setString(4, instance.getErrorString());
			pstmt.setString(5, instance.getLocalDownloadPath());
			pstmt.setString(6, instance.getJobId());
			pstmt.setLong(7, instance.getHostId());
			pstmt.setLong(8, instance.getTemplateId());
			pstmt.executeUpdate();
		} catch (Exception e) {
			s_logger.warn("Exception: ", e);
		}
	}	

	@Override
	public List<VMTemplateHostVO> listByHostId(long id) {
	    SearchCriteria<VMTemplateHostVO> sc = HostSearch.create();
	    sc.setParameters("host_id", id);
	    return listIncludingRemovedBy(sc);
	}

	@Override
	public List<VMTemplateHostVO> listByTemplateId(long templateId) {
	    SearchCriteria<VMTemplateHostVO> sc = TemplateSearch.create();
	    sc.setParameters("template_id", templateId);
	    sc.setParameters("destroyed", false);
	    return listIncludingRemovedBy(sc);
	}
	
	
	@Override
	public List<VMTemplateHostVO> listByOnlyTemplateId(long templateId) {
	    SearchCriteria<VMTemplateHostVO> sc = TemplateSearch.create();
	    sc.setParameters("template_id", templateId);	    
	    return listIncludingRemovedBy(sc);
	}

	@Override
	public VMTemplateHostVO findByHostTemplate(long hostId, long templateId) {
		SearchCriteria<VMTemplateHostVO> sc = HostTemplateSearch.create();
	    sc.setParameters("host_id", hostId);
	    sc.setParameters("template_id", templateId);
	    return findOneIncludingRemovedBy(sc);
	}
	
	@Override
	public List<VMTemplateHostVO> listByTemplateStatus(long templateId, VMTemplateHostVO.Status downloadState) {
		SearchCriteria<VMTemplateHostVO> sc = TemplateStatusSearch.create();
		sc.setParameters("template_id", templateId);
		sc.setParameters("download_state", downloadState.toString());
		return listIncludingRemovedBy(sc);
	}
	
	@Override
	public List<VMTemplateHostVO> listByTemplateStatus(long templateId, long datacenterId, VMTemplateHostVO.Status downloadState) {
        Transaction txn = Transaction.currentTxn();
		PreparedStatement pstmt = null;
		List<VMTemplateHostVO> result = new ArrayList<VMTemplateHostVO>();
		try {
			String sql = DOWNLOADS_STATE_DC;
			pstmt = txn.prepareAutoCloseStatement(sql);
			pstmt.setLong(1, datacenterId);
			pstmt.setLong(2, templateId);
			pstmt.setString(3, downloadState.toString());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
                result.add(toEntityBean(rs, false));
            }
		} catch (Exception e) {
			s_logger.warn("Exception: ", e);
		}
		return result;
	}

    @Override
    public List<VMTemplateHostVO> listByTemplateHostStatus(long templateId, long hostId, VMTemplateHostVO.Status... states) {       
        SearchCriteria<VMTemplateHostVO> sc = HostTemplateStateSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("host_id", hostId);
        sc.setParameters("states", (Object[])states);
        return search(sc, null);
    }
	   
	@Override
	public List<VMTemplateHostVO> listByTemplateStatus(long templateId, long datacenterId, long podId, VMTemplateHostVO.Status downloadState) {
        Transaction txn = Transaction.currentTxn();
		PreparedStatement pstmt = null;
		List<VMTemplateHostVO> result = new ArrayList<VMTemplateHostVO>();
		ResultSet rs = null;
		try {
			String sql = DOWNLOADS_STATE_DC_POD;
			pstmt = txn.prepareStatement(sql);
			
			pstmt.setLong(1, datacenterId);
			pstmt.setLong(2, podId);
			pstmt.setLong(3, templateId);
			pstmt.setString(4, downloadState.toString());
			rs = pstmt.executeQuery();
			while (rs.next()) {
                // result.add(toEntityBean(rs, false)); TODO: this is buggy in GenericDaoBase for hand constructed queries
				long id = rs.getLong(1); //ID column
				result.add(findById(id));
            }
		} catch (Exception e) {
			s_logger.warn("Exception: ", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException e) {
			}
		}
		return result;

	}

	@Override
	public boolean templateAvailable(long templateId, long hostId) {
		VMTemplateHostVO tmpltHost = findByHostTemplate(hostId, templateId);
		if (tmpltHost == null)
		  return false;
		
		return tmpltHost.getDownloadState()==Status.DOWNLOADED;
	}

	@Override
	public List<VMTemplateHostVO> listByTemplateStates(long templateId, VMTemplateHostVO.Status... states) {
    	SearchCriteria<VMTemplateHostVO> sc = TemplateStatesSearch.create();
    	sc.setParameters("states", (Object[])states);
		sc.setParameters("template_id", templateId);

	  	return search(sc, null);
	}

	@Override
	public List<VMTemplateHostVO> listByHostTemplate(long hostId, long templateId) {
		SearchCriteria<VMTemplateHostVO> sc = HostTemplateSearch.create();
	    sc.setParameters("host_id", hostId);
	    sc.setParameters("template_id", templateId);
	    return listIncludingRemovedBy(sc);
	}

    @Override
    public List<VMTemplateHostVO> listByZoneTemplate(long dcId, long templateId, boolean readyOnly) {
        SearchCriteria<VMTemplateHostVO> sc = ZONE_TEMPLATE_SEARCH.create();
        sc.setParameters("template_id", templateId);
        sc.setJoinParameters("tmplHost", "zone_id", dcId);
        if (readyOnly) {
            sc.setParameters("state", VMTemplateHostVO.Status.DOWNLOADED);
        } 
        return listBy(sc);
    }
	
	@Override
	public List<VMTemplateHostVO> listDestroyed(long hostId) {
		SearchCriteria<VMTemplateHostVO> sc = HostDestroyedSearch.create();
		sc.setParameters("host_id", hostId);
		sc.setParameters("destroyed", true);
		return listIncludingRemovedBy(sc);
	}

	@Override
	public VMTemplateHostVO findByHostTemplate(long hostId, long templateId, boolean lock) {
		SearchCriteria<VMTemplateHostVO> sc = HostTemplateSearch.create();
	    sc.setParameters("host_id", hostId);
	    sc.setParameters("template_id", templateId);
	    if (!lock)
	    	return findOneIncludingRemovedBy(sc);
	    else
	    	return lockOneRandomRow(sc, true);
	}
	
	//Based on computing node host id, and template id, find out the corresponding template_host_ref, assuming local secondary storage and computing node is in the same zone, and private ip
	@Override 
	public VMTemplateHostVO findLocalSecondaryStorageByHostTemplate(long hostId, long templateId) {
	    HostVO computingHost = _hostDao.findById(hostId);
	    SearchCriteria<VMTemplateHostVO> sc = LOCAL_SECONDARY_STORAGE_SEARCH.create();
	    sc.setJoinParameters("host", "private_ip_address", computingHost.getPrivateIpAddress());
	    sc.setJoinParameters("host", "state", com.cloud.host.Status.Up);
	    sc.setJoinParameters("host", "data_center_id", computingHost.getDataCenterId());
	    sc.setJoinParameters("host", "type", Host.Type.LocalSecondaryStorage);
	    sc.setParameters("template_id", templateId);
	    sc.setParameters("state", VMTemplateHostVO.Status.DOWNLOADED);
	    return findOneBy(sc);
	}

    @Override
    public void deleteByHost(Long hostId) {
        List<VMTemplateHostVO> tmpltHosts = listByHostId(hostId);
        for (VMTemplateHostVO tmpltHost : tmpltHosts ) {
            remove(tmpltHost.getId());
        }
    }

}
