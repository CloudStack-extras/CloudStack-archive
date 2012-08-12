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
import java.util.HashSet;
import java.util.Set;

import com.cloud.bridge.model.SObjectItem;

/**
 * @author Kelven Yang
 */
public class SObjectItemDao extends BaseDao {
	
	private Connection conn= null;
	
	public SObjectItemDao() {
	}
	
	public SObjectItem getByObjectIdNullVersion(long id) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		PreparedStatement pstmt = null;
		SObjectItem item = null;
		openConnection();
		try {
			pstmt = conn.prepareStatement( "SELECT * from sobject_item where SObjectID=? and Version is null");
			pstmt.setLong(1, id);
			ResultSet rst = pstmt.executeQuery();
			
			if(rst.next()) {
				item = new SObjectItem();
				item.setId(rst.getLong("ID"));
				item.setVersion(rst.getString("Version"));
				item.setMd5(rst.getString("MD5"));
				item.setStoredPath(rst.getString("StoredPath"));
				item.setStoredSize(rst.getLong("StoredSize"));
				item.setCreateTime(rst.getTimestamp("CreateTime"));
				item.setLastModifiedTime(rst.getTimestamp("LastModifiedTime"));
				item.setLastAccessTime(rst.getTimestamp("LastAccessTime"));
			}
		}finally {
			pstmt.close();
			closeConnection();
		}
		return item;
	}
	
	public Set<SObjectItem> getByObjectId(long id) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		PreparedStatement pstmt = null;
		SObjectItem item;
		Set<SObjectItem> items = new HashSet<SObjectItem>();
		openConnection();
		try {
			pstmt = conn.prepareStatement( "SELECT * from sobject_item where SObjectID=?");
			pstmt.setLong(1, id);
			ResultSet rst = pstmt.executeQuery();
			while(rst.next()) {
				item = new SObjectItem();
				item.setId(rst.getLong("ID"));
				item.setVersion(rst.getString("Version"));
				item.setMd5(rst.getString("MD5"));
				item.setStoredPath(rst.getString("StoredPath"));
				item.setStoredSize(rst.getLong("StoredSize"));
				item.setCreateTime(rst.getTimestamp("CreateTime"));
				item.setLastModifiedTime(rst.getTimestamp("LastModifiedTime"));
				item.setLastAccessTime(rst.getTimestamp("LastAccessTime"));
				items.add(item);
			}
		}finally {
			pstmt.close();
			closeConnection();
		}
		return items;
	}
	
	
	
	
	
	private void openConnection() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		if (null == conn) {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection( "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + awsapi_dbName, dbUser, dbPassword );		}
	}

	private void closeConnection() throws SQLException {
		if (null != conn)
			conn.close();
		conn = null;
	}

	public SObjectItem get(Long id) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		PreparedStatement pstmt = null;
		SObjectItem item = null;
		openConnection();
		try {
			pstmt = conn.prepareStatement( "SELECT * from sobject_item where ID=?");
			pstmt.setLong(1, id);
			ResultSet rst = pstmt.executeQuery();
			
			if(rst.next()) {
				item = new SObjectItem();
				item.setId(rst.getLong("ID"));
				item.setVersion(rst.getString("Version"));
				item.setMd5(rst.getString("MD5"));
				item.setStoredPath(rst.getString("StoredPath"));
				item.setStoredSize(rst.getLong("StoredSize"));
				item.setCreateTime(rst.getTimestamp("CreateTime"));
				item.setLastModifiedTime(rst.getTimestamp("LastModifiedTime"));
				item.setLastAccessTime(rst.getTimestamp("LastAccessTime"));
			}
		}finally {
			pstmt.close();
			closeConnection();
		}
		return item;
		
	}

	public void delete(SObjectItem item) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		PreparedStatement pstmt = null;
		openConnection();
		try {
			pstmt = conn.prepareStatement( "DELETE from sobject_item where ID=?");
			pstmt.setLong(1, item.getId());
			pstmt.executeUpdate();
		}finally {
			closeConnection();
		}
		
	}

	public void update(SObjectItem item, String[] keys) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		StringBuffer query =  new StringBuffer("UPDATE sobject_item set ");
		for(String key:keys) {
			if (key.equalsIgnoreCase("checksum")) {
				query.append(" MD5=\""+item.getMd5()+"\"");
			}
			if (key.equalsIgnoreCase("storedsize")) {
				query.append(" , storedsize="+item.getStoredSize());
			}
			
			if (key.equalsIgnoreCase("storedpath")) {
				query.append(" storedpath=\""+item.getStoredPath()+"\"");
			}
		}
		
		query.append(" where ID=?");
		
		PreparedStatement pstmt = null;
		openConnection();
		try {
			pstmt = conn.prepareStatement(query.toString());
			pstmt.setLong(1, item.getId());
			pstmt.executeUpdate();
		}finally {
			pstmt.close();
			closeConnection();
		}
	}
	
	public SObjectItem save(SObjectItem item) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		PreparedStatement pstmt = null;
		openConnection();
		try {
			pstmt = conn.prepareStatement( "INSERT into sobject_item (SObjectID, Version, MD5, StoredPath, StoredSize, CreateTime, LastModifiedTime, LastAccessTime) VALUES (?,?,?,?,?,?,?,?)");
			
			pstmt.setLong(1, item.getTheObject().getId() );
			pstmt.setString(2, item.getVersion());
			pstmt.setString(3, item.getMd5());
			pstmt.setString(4, item.getStoredPath());
			pstmt.setLong(5, item.getStoredSize());
			pstmt.setTimestamp(6, new Timestamp(item.getCreateTime().getTime()));
			pstmt.setTimestamp(7, new Timestamp(item.getLastModifiedTime().getTime()));
			pstmt.setTimestamp(8, new Timestamp(item.getLastAccessTime().getTime()));
			pstmt.executeUpdate();
			
			pstmt.close();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT ID from sobject_item where SObjectID=? and "); // Version is null")
			if (item.getVersion() == null) 
				sb.append("Version is null");
			else 
				sb.append("Version=\""+item.getVersion()+"\"");
	
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setLong(1, item.getTheObject().getId() );
			ResultSet rs = pstmt.executeQuery();
			if (rs.next() ){
				item.setId(rs.getLong("ID"));
			}
			return item;
			
			}finally {
				pstmt.close();
				closeConnection();
			}
	}
}
