/**
 * Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
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
package com.cloud.alert.snmp.dao;

import java.util.List;

import javax.ejb.Local;

import com.cloud.alert.snmp.SnmpManagersVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchCriteria;

/**
 * @author Anshul Gangwar
 *
 */
@Local(value = { SnmpManagersDao.class })
public class SnmpManagersDaoImpl extends GenericDaoBase<SnmpManagersVO, Long> implements SnmpManagersDao {

    @Override
    public List<SnmpManagersVO> getSnmpManagers(short type, boolean enabled) {

        SearchCriteria<SnmpManagersVO> sc = createSearchCriteria();
        sc.addAnd("type", SearchCriteria.Op.EQ, Short.valueOf(type));
        sc.addAnd("enabled", SearchCriteria.Op.EQ, Boolean.valueOf(enabled));

        List<SnmpManagersVO> snmpManagers = listBy(sc);

        if(!snmpManagers.isEmpty()){
            return snmpManagers;
        }

        return null;
    }

    @Override
    public List<SnmpManagersVO> getSnmpManagers(boolean enabled) {

        SearchCriteria<SnmpManagersVO> sc = createSearchCriteria();
        sc.addAnd("enabled", SearchCriteria.Op.EQ, Boolean.valueOf(enabled));

        List<SnmpManagersVO> snmpManagers = listBy(sc);

        if(!snmpManagers.isEmpty()){
            return snmpManagers;
        }

        return null;
    }

}
