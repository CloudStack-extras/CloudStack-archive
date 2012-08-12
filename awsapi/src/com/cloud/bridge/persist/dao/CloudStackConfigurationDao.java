/*
 * Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloud.bridge.persist.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;


public class CloudStackConfigurationDao extends BaseDao {
	public static final Logger logger = Logger.getLogger(CloudStackConfigurationDao.class);
	private Connection conn = null;
	
	public CloudStackConfigurationDao() {
		
	}

	public String getConfigValue( String configName ) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		openConnection();
		try { 
			PreparedStatement pstmt = conn.prepareStatement ( "SELECT value from configuration where name=?" );
			pstmt.setString(1, configName);
			
			ResultSet rst = pstmt.executeQuery();
			if (rst.next()) {
				logger.info("found the config value for " + configName +" :: " + rst.getString("value"));
				return rst.getString("value");
             }

			} finally {
			closeConnection();
		}
		return null;
	}

	
	private void openConnection() 
        throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException 
    {
        if (null == conn) {
            Class.forName( "com.mysql.jdbc.Driver" ).newInstance();
            conn = DriverManager.getConnection( "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + cloud_dbName, dbUser, dbPassword );
        }
    }

    private void closeConnection() throws SQLException {
        if (null != conn) conn.close();
        conn = null;
    }

}
