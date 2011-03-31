/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
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
package com.cloud.dc.dao;

import java.util.Map;

import com.cloud.dc.DcDetailVO;
import com.cloud.utils.db.GenericDao;

public interface DcDetailsDao extends GenericDao<DcDetailVO, Long> {
    Map<String, String> findDetails(long dcId);
    
    void persist(long dcId, Map<String, String> details);
    
    DcDetailVO findDetail(long dcId, String name);

	void deleteDetails(long dcId);
}