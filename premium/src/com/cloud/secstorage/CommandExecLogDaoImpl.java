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

package com.cloud.secstorage;

import java.util.Date;

import javax.ejb.Local;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

@Local(value={CommandExecLogDao.class})
public class CommandExecLogDaoImpl extends GenericDaoBase<CommandExecLogVO, Long> implements CommandExecLogDao {

    protected final SearchBuilder<CommandExecLogVO> ExpungeSearch;
	
	public CommandExecLogDaoImpl() {
		ExpungeSearch = createSearchBuilder();
		ExpungeSearch.and("created", ExpungeSearch.entity().getCreated(), Op.LT);
		ExpungeSearch.done();
	}
	
	@Override
	public void expungeExpiredRecords(Date cutTime) {
		SearchCriteria<CommandExecLogVO> sc = ExpungeSearch.create();
		sc.setParameters("created", cutTime);
		expunge(sc);
	}
}

