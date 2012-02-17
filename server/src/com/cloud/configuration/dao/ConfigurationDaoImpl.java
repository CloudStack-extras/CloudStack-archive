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

package com.cloud.configuration.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.configuration.ConfigurationVO;
import com.cloud.utils.crypt.DBEncryptionUtil;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;

@Local(value={ConfigurationDao.class})
public class ConfigurationDaoImpl extends GenericDaoBase<ConfigurationVO, String> implements ConfigurationDao {
    private static final Logger s_logger = Logger.getLogger(ConfigurationDaoImpl.class);
    private Map<String, String> _configs = null;
    private boolean _premium;

    final SearchBuilder<ConfigurationVO> InstanceSearch;
    final SearchBuilder<ConfigurationVO> NameSearch;
    
    public static final String UPDATE_CONFIGURATION_SQL = "UPDATE configuration SET value = ? WHERE name = ?";

    public ConfigurationDaoImpl () {
        InstanceSearch = createSearchBuilder();
        InstanceSearch.and("instance", InstanceSearch.entity().getInstance(), SearchCriteria.Op.EQ);
        
        NameSearch = createSearchBuilder();
        NameSearch.and("name", NameSearch.entity().getName(), SearchCriteria.Op.EQ);
    }

    @Override
    public boolean isPremium() {
        return _premium;
    }

    @Override
    public Map<String, String> getConfiguration(String instance, Map<String, ? extends Object> params) {
        if (_configs == null) {
            _configs = new HashMap<String, String>();

            SearchCriteria<ConfigurationVO> sc = InstanceSearch.create();
            sc.setParameters("instance", "DEFAULT");

            List<ConfigurationVO> configurations = listIncludingRemovedBy(sc);

            for (ConfigurationVO config : configurations) {
            	if (config.getValue() != null)
            		_configs.put(config.getName(), config.getValue());
            }

            if(!"DEFAULT".equals(instance)){
            	//Default instance params are already added, need not add again 
            	sc = InstanceSearch.create();
            	sc.setParameters("instance", instance);

            	configurations = listIncludingRemovedBy(sc);

            	for (ConfigurationVO config : configurations) {
            		if (config.getValue() != null)
            			_configs.put(config.getName(), config.getValue());
            	}
            }

        }

        mergeConfigs(_configs, params);
        return _configs;
    }

    @Override
    public Map<String, String> getConfiguration(Map<String, ? extends Object> params) {
        return getConfiguration("DEFAULT", params);
    }
    
    @Override
    public Map<String, String> getConfiguration() {
        return getConfiguration("DEFAULT", new HashMap<String, Object>());
    }
    
    protected void mergeConfigs(Map<String, String> dbParams, Map<String, ? extends Object> xmlParams) {
        for (Map.Entry<String, ? extends Object> param : xmlParams.entrySet()) {
            dbParams.put(param.getKey(), (String)param.getValue());
        }
    }

    @Override
	public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
    	super.configure(name, params);

    	Object premium = params.get("premium");
        _premium = (premium != null) && ((String) premium).equals("true");

        return true;
    }

    //Use update method with category instead
    @Override @Deprecated
    public boolean update(String name, String value) {
    	Transaction txn = Transaction.currentTxn();
		try {
			PreparedStatement stmt = txn.prepareStatement(UPDATE_CONFIGURATION_SQL);
			stmt.setString(1, value);
			stmt.setString(2, name);
			stmt.executeUpdate();
			return true;
		} catch (Exception e) {
			s_logger.warn("Unable to update Configuration Value", e);
		}
		return false;
    }

    @Override
    public boolean update(String name, String category, String value) {
    	Transaction txn = Transaction.currentTxn();
		try {
			value = ("Hidden".equals(category) || "Secure".equals(category)) ? DBEncryptionUtil.encrypt(value) : value;
			PreparedStatement stmt = txn.prepareStatement(UPDATE_CONFIGURATION_SQL);
			stmt.setString(1, value);
			stmt.setString(2, name);
			stmt.executeUpdate();
			return true;
		} catch (Exception e) {
			s_logger.warn("Unable to update Configuration Value", e);
		}
		return false;
    }
    
    @Override
    public String getValue(String name) {
    	ConfigurationVO config =  findByName(name);
        return (config == null) ? null : config.getValue();
    }
    
    @Override
    @DB
    public String getValueAndInitIfNotExist(String name, String category, String initValue) {
    	Transaction txn = Transaction.currentTxn();
    	PreparedStatement stmt = null;
    	PreparedStatement stmtInsert = null;
    	String returnValue = initValue;
		try {
			txn.start();
			stmt = txn.prepareAutoCloseStatement("SELECT value FROM configuration WHERE name=?");
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if(rs != null && rs.next()) {
				returnValue =  rs.getString(1);
				if(returnValue != null) {
					txn.commit();
					if("Hidden".equals(category) || "Secure".equals(category)){
						return DBEncryptionUtil.decrypt(returnValue);
					} else {
						return returnValue;
					}
				} else {
					// restore init value
					returnValue = initValue;
				}
			}
			stmt.close();

			if("Hidden".equals(category) || "Secure".equals(category)){
				initValue = DBEncryptionUtil.encrypt(initValue);
			}
			stmtInsert = txn.prepareAutoCloseStatement(
				"INSERT INTO configuration(instance, name, value, description) VALUES('DEFAULT', ?, ?, '') ON DUPLICATE KEY UPDATE value=?");
			stmtInsert.setString(1, name);
			stmtInsert.setString(2, initValue);
			stmtInsert.setString(3, initValue);
			if(stmtInsert.executeUpdate() < 1) {
				throw new CloudRuntimeException("Unable to init configuration variable: " + name); 
			}
			txn.commit();
			return returnValue;
		} catch (Exception e) {
			s_logger.warn("Unable to update Configuration Value", e);
			throw new CloudRuntimeException("Unable to init configuration variable: " + name); 
		}
    }
    
    @Override
    public ConfigurationVO findByName(String name) {
        SearchCriteria<ConfigurationVO> sc = NameSearch.create();
        sc.setParameters("name", name);
        return findOneIncludingRemovedBy(sc);
    }
    
}
