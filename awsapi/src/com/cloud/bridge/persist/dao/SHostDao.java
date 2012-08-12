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

import com.cloud.bridge.model.SHost;

/**
 * @author Kelven Yang
 */
public class SHostDao extends BaseDao {
	private Connection conn = null;
	
	public SHostDao() {
	}
	
	public SHost getById(Long hostId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		SHost shost = null;
		openConnection();
		PreparedStatement pstmt = null;
		try { 
			pstmt = conn.prepareStatement ( "select * from shost where id=?");
			pstmt.setLong(1, hostId);
			
			ResultSet rst = pstmt.executeQuery();
			if (rst.next()) {
				shost = new SHost();
				MHostDao mhostdao = new MHostDao();
				shost.setId(rst.getLong("ID"));
				shost.setHost(rst.getString("Host"));
				shost.setHostType(rst.getInt("HostType"));
				shost.setExportRoot(rst.getString("ExportRoot"));
				shost.setMhost(mhostdao.getByHostId(rst.getLong("MHostID")));
				shost.setUserOnHost(rst.getString("UserOnHost"));
				shost.setUserPassword(rst.getString("UserPassword"));
				return shost;
			}
		} finally {
			pstmt.close();
			closeConnection();
		}
		
		return shost;
		
	}
	public SHost getLocalStorageHost(long mhostId, String storageRoot) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
	
		openConnection();
		SHost shost = null;
		PreparedStatement pstmt=null;
		try { 
			pstmt = conn.prepareStatement ( "SELECT * from shost where MHostID=? and exportRoot=?");
			pstmt.setLong(1, mhostId);
			pstmt.setString(2, storageRoot);
			ResultSet rst = pstmt.executeQuery();
			if (rst.next()) {
				shost = new SHost();
				MHostDao mhostdao = new MHostDao();
				shost.setId(rst.getLong("ID"));
				shost.setHost(rst.getString("Host"));
				shost.setHostType(rst.getInt("HostType"));
				shost.setExportRoot(rst.getString("ExportRoot"));
				shost.setMhost(mhostdao.getByHostId(rst.getLong("MHostID")));
				shost.setUserOnHost(rst.getString("UserOnHost"));
				shost.setUserPassword(rst.getString("UserPassword"));
				pstmt.close();
				return shost;
			}
			
		} catch(Exception e){
			
		}finally {
			pstmt.close();
			closeConnection();
		}
		
		return shost;
	}
	
	private void openConnection() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		if (null == conn) {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":"
					+ dbPort + "/" + awsapi_dbName, dbUser, dbPassword);
		}
	}

	private void closeConnection() throws SQLException {
		if (null != conn)
			conn.close();
		conn = null;
	}

	public void save(SHost shost) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		openConnection();
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement( "INSERT into shost (Host, HostType, ExportRoot, MHostID) VALUES(?,?,?,?)" );
			
			pstmt.setString(1, shost.getHost());
			pstmt.setInt(2, shost.getHostType());
			pstmt.setString(3, shost.getExportRoot());
			pstmt.setLong(4, shost.getMhost().getId());
			pstmt.executeUpdate();
			
		} finally {
			pstmt.close();
			closeConnection();
		}
		
	}
}
