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

import com.cloud.stack.models.CloudStackAccount;

public class CloudStackAccountDao extends BaseDao {
    public static final Logger logger = Logger.getLogger(CloudStackAccountDao.class);
	private Connection conn = null;
	
    public CloudStackAccountDao() {
    }

    public CloudStackAccount getdefaultZoneId( String id ) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
    	CloudStackAccount account = null;
		openConnection();
		try {
			PreparedStatement pstmt = conn.prepareStatement( "SELECT id, account_name, default_zone_id from account where uuid=?" );
			pstmt.setLong(1,Long.parseLong(id));
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				account = new CloudStackAccount();
				account.setId( rs.getString("id")) ;
				account.setName(rs.getString("account_name"));
				account.setdefaultZoneId(rs.getString("default_zone_id"));
			}
			pstmt.close();
		} finally {
			closeConnection();
		}
    	
		return account;
    }
    
	private void openConnection() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		if (null == conn) {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":"
					+ dbPort + "/" + cloud_dbName, dbUser, dbPassword);
		}
	}

	private void closeConnection() throws SQLException {
		if (null != conn)
			conn.close();
		conn = null;
	}
    
    
}

