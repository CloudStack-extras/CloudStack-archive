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
import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;

import com.cloud.bridge.model.MHost;

/**
 * @author Kelven Yang
 */
public class MHostDao extends BaseDao {
	public static final Logger s_logger = Logger.getLogger(MHostDao.class.getName());
	private Connection conn = null;
	
	public MHostDao() {

	}
	
/*	public MHost getByHostKey(String hostKey) {
		return queryEntity("from MHost where hostKey=?", new Object[] {hostKey});
	}
*/	
	public MHost getByHostKey(String hostKey) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		MHost mhost = null;
        openConnection();
        
		try {
			
			PreparedStatement pstmt = conn.prepareStatement( "SELECT * from mhost where mhostkey=?");
			pstmt.setString(1, hostKey);
			logger.info("SQL ::" + pstmt.toString() );
			ResultSet rst = pstmt.executeQuery();

			if (rst.next()) {
				mhost = new MHost();
				mhost.setId(rst.getLong("ID"));
				mhost.setHostKey(rst.getString("MHostKey"));
				mhost.setHost(rst.getString("Host"));
				mhost.setVersion(rst.getString("Version"));
				mhost.setLastHeartbeatTime(rst.getDate("LastHeartbeatTime"));
				
				return mhost;
				/*
				 * ID | bigint(20) | NO | PRI | NULL | auto_increment | |
				 * MHostKey | varchar(128) | NO | MUL | NULL | | | Host |
				 * varchar(128) | YES | UNI | NULL | | | Version | varchar(64) |
				 * YES | | NULL | | | LastHeartbeatTime
				 */
			}

/*		} catch (Exception ex) {
			s_logger.error("Error getting Object ID", ex);*/
		} finally {
			closeConnection();
		}
		return mhost;
	}
	
	
	public MHost getByHostId(Long hostId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		MHost mhost = null;	
        openConnection();
        
		try {
			
			PreparedStatement pstmt = conn.prepareStatement( "select * from mhost where id=?");
			pstmt.setLong(1, hostId);
			ResultSet rst = pstmt.executeQuery();

			if (rst.next()) {
				mhost = new MHost();
				mhost.setId(hostId);
				mhost.setHostKey(rst.getString("MHostKey"));
				mhost.setHost(rst.getString("Host"));
				mhost.setVersion(rst.getString("Version"));
				mhost.setLastHeartbeatTime(rst.getDate("LastHeartbeatTime"));
				return mhost;
				
				/*
				 * ID | bigint(20) | NO | PRI | NULL | auto_increment | |
				 * MHostKey | varchar(128) | NO | MUL | NULL | | | Host |
				 * varchar(128) | YES | UNI | NULL | | | Version | varchar(64) |
				 * YES | | NULL | | | LastHeartbeatTime
				 */
				
			}

		} catch (Exception ex) {
			s_logger.error("Error getting Object ID", ex);
		} finally {
			closeConnection();
		}
		return mhost;
	}
	
	
/*	public void update(MHost mhost, String key) {
		openConnection();        
		try {
			
			PreparedStatement pstmt = conn.prepareStatement( "update mhost from mhost where id=?");
			pstmt.setLong(1, hostId);
			ResultSet rst = pstmt.executeQuery();

			if (rst.next()) {
			}
		
	}
*/		
	private void openConnection() 
	        throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException 
	    {
	        if (null == conn) {
	            Class.forName( "com.mysql.jdbc.Driver" ).newInstance();
	            conn = DriverManager.getConnection( "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + awsapi_dbName, dbUser, dbPassword );
	        }
	    }
	
	private void closeConnection() throws SQLException {
		if (null != conn)
			conn.close();
		conn = null;
	}

/*	public void update(MHost mhost) {
		// TODO Auto-generated method stub
		
	}
*/
	public MHost get(long managementHostId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		return getByHostId(managementHostId);
		
	}

	public void update(MHost mhost, Date lastHeartbeatTime) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		openConnection();
		PreparedStatement pstmt = null;
		try {
			//UPDATE multipart_parts SET MD5=?, StoredSize=?, CreateTime=? WHERE UploadId=? AND partNumber=?"
			pstmt = conn.prepareStatement( "UPDATE mhost SET LastHeartbeatTime=? where ID=?");
			pstmt.setDate(1, (java.sql.Date) lastHeartbeatTime);
			pstmt.setLong(2, mhost.getId());
			pstmt.executeUpdate();
			pstmt.close();
		}catch (Exception e) {
			
		}finally {
			closeConnection();
			
		}
		
	}

	public void update(MHost mhost, String host) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		openConnection();
		PreparedStatement pstmt = null;
		try {

			pstmt = conn.prepareStatement( "UPDATE mhost SET Host=? where ID=?");
			pstmt.setString(1, host);
			pstmt.setLong(2, mhost.getId());
			pstmt.executeUpdate();
			pstmt.close();
		}finally {
			closeConnection();
		}
		
	}

	public long save(MHost mhost) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		long id = -1;
		openConnection();
		PreparedStatement pstmt = null;
		try {
			//INSERT INTO multipart_uploads (AccessKey, BucketName, NameKey, x_amz_acl, CreateTime) VALUES (?,?,?,?,?)
			Date tod = new Date();
	        java.sql.Timestamp dateTime = new Timestamp( tod.getTime());

			pstmt = conn.prepareStatement( "INSERT into mhost (MHostKey, Host, Version, LastHeartbeatTime) VALUES(?,?,?,?)" );
			pstmt.setString(1, mhost.getHostKey());
			pstmt.setString(2, mhost.getHost());
			pstmt.setString(3, mhost.getVersion());
			pstmt.setTimestamp(4, dateTime);
			pstmt.executeUpdate();
	/*			-------------------+--------------+------+-----+---------+----------------+
	| ID                | bigint(20)   | NO   | PRI | NULL    | auto_increment |
	| MHostKey          | varchar(128) | NO   | MUL | NULL    |                |
	| Host              | varchar(128) | YES  | UNI | NULL    |                |
	| Version           | varchar(64)  | YES  |     | NULL    |                |
	| LastHeartbeatTime | datetime     | YES  | MUL | NULL    |                |
	+-------------------+--------------+------+-----+---------+----------------+
	  
	*/		// INSERT into mhost (MHostKey, Host, Version, LastHeartbeatTime) VALUES('08:00:27:82:4A:5B','http://localhost:7080/awsapi',null,** NOT SPECIFIED **
			
			pstmt.close();
			
			pstmt = conn.prepareStatement( "SELECT ID from mhost where MHostKey =? and Host=?"); //INSERT into mhost (MHostKey, Host, Version, LastHeartbeatTime) VALUES(?,?,?,?)" );
			pstmt.setString(1, mhost.getHostKey());
			pstmt.setString(2, mhost.getHost());
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				id = rs.getLong("ID");
			}
		}catch (Exception e) {
			
		}finally {
			closeConnection();
		}
		return id;
		
	}

/*	public void save(MHost mhost) {
		// TODO Auto-generated method stub
		
	}
*/	
}