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

import java.util.Date;
import java.util.List;

import com.cloud.cluster.ManagementServerHost;
import com.cloud.cluster.ManagementServerHost.State;
import com.cloud.cluster.ManagementServerHostVO;
import com.cloud.utils.db.GenericDao;

public interface ManagementServerHostDao extends GenericDao<ManagementServerHostVO, Long> {
    @Override
    boolean remove(Long id);

	ManagementServerHostVO findByMsid(long msid);
	int increaseAlertCount(long id);
	
	void update(long id, long runid, String name, String version, String serviceIP, int servicePort, Date lastUpdate);
	void update(long id, long runid, Date lastUpdate);
	List<ManagementServerHostVO> getActiveList(Date cutTime);
	List<ManagementServerHostVO> getInactiveList(Date cutTime);

	void invalidateRunSession(long id, long runid);
	
	void update(long id, long runId, State state, Date lastUpdate);
	
	List<ManagementServerHostVO> listBy(ManagementServerHost.State...states);
	public List<Long> listOrphanMsids();
}
