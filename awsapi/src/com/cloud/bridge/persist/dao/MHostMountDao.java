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

import com.cloud.bridge.model.MHostMount;

/**
 * @author Kelven Yang
 */
public class MHostMountDao extends BaseDao {
	private Connection conn = null;
	
	public MHostMountDao() {
	}
	
	/*public MHostMount getHostMount(long mHostId, long sHostId) {
		return queryEntity("from MHostMount where mhost=? and shost=?", new Object[] { mHostId, sHostId } );
	}*/
	
	
	public MHostMount getHostMount(long mHostId, long sHostId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
	
		PreparedStatement statement = null;
		MHostMount mhostmount = null;
		
        openConnection();	
        try {
		    statement = conn.prepareStatement( "select * from mhost_mount where mhostid=? and shostid=?" );
		    statement.setLong(1, mHostId);
	        statement.setLong(2, sHostId);
	        ResultSet rst = statement.executeQuery();
	        if (rst.next()) {
	        	mhostmount = new MHostMount();
	        	MHostDao mhostdao = new MHostDao();
	        	SHostDao shostdao = new SHostDao();
	        	mhostmount.setId(rst.getLong("ID"));
	        	mhostmount.setMhost(mhostdao.getByHostId(rst.getLong("MHostID")));
	        	mhostmount.setShost(shostdao.getById(rst.getLong("SHostID")));
	        	mhostmount.setLastMountTime(rst.getTimestamp("LastMountTime"));
	        	mhostmount.setMountPath(rst.getString("MountPath"));
	        	return mhostmount;
	        }
	        
/*        
	+---------------+--------------+------+-----+---------+----------------+
	| Field         | Type         | Null | Key | Default | Extra          |
	+---------------+--------------+------+-----+---------+----------------+
	| ID            | bigint(20)   | NO   | PRI | NULL    | auto_increment |
	| MHostID       | bigint(20)   | NO   | MUL | NULL    |                |
	| SHostID       | bigint(20)   | NO   | MUL | NULL    |                |
	| MountPath     | varchar(256) | YES  |     | NULL    |                |
	| LastMountTime | datetime     | YES  | MUL | NULL    |                |
	+---------------+--------------+------+-----+---------+----------------+

*/          
        } finally {
            closeConnection();
        }
		return mhostmount;
	}
	
	private void openConnection() 
	        throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException 
	    {
	        if (null == conn) {
	            Class.forName( "com.mysql.jdbc.Driver" ).newInstance();
	            conn = DriverManager.getConnection( "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + awsapi_dbName, dbUser, dbPassword );
	        }
	    }

	    private void closeConnection() throws SQLException {
	        if (null != conn) conn.close();
	        conn = null;
	    }

	
}
