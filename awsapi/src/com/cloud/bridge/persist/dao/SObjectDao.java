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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cloud.bridge.model.SBucket;
import com.cloud.bridge.model.SObject;

/**
 * @author Kelven Yang
 */
public class SObjectDao extends BaseDao {
	
	private Connection conn= null;
	public SObjectDao() {
	}

	public SObject getByNameKey(SBucket bucket, String nameKey) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
	    PreparedStatement statement = null;
	    SObject sobject = null;
        openConnection();	
        try {            
            statement = conn.prepareStatement ( "SELECT * from sobject where SBucketID=? and nameKey=?" );
            statement.setLong( 1, bucket.getId());
            statement.setString( 2, nameKey);
            
            ResultSet rs = statement.executeQuery();
            
	        if (rs.next()) {
	        	SObjectItemDao itemDao = new SObjectItemDao();
	        	sobject = new SObject();
	        	sobject.setId(rs.getLong("ID"));
	        	sobject.setBucket(bucket);
	        	sobject.setNameKey(nameKey);
	        	sobject.setItems(itemDao.getByObjectId(sobject.getId()));
	        	sobject.setOwnerCanonicalId(rs.getString("OwnerCanonicalID"));
	        	sobject.setNextSequence(rs.getInt("NextSequence"));
	        	sobject.setDeletionMark(rs.getString("DeletionMark"));
	        	sobject.setCreateTime(rs.getTimestamp("CreateTime"));
	        }
            	
            return sobject;
        } finally {
        	statement.close();
            closeConnection();
        }
	}
	
	public List<SObject> listBucketObjects(SBucket bucket, String prefix,
			String marker, int maxKeys) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		
		List<SObject> items = new ArrayList<SObject>();
		PreparedStatement statement = null;
	    SObject sobject = null;
	    SObjectItemDao itemDao = new SObjectItemDao();
        openConnection();        
        try {            
            statement = conn.prepareStatement ( "SELECT * from sobject where SBucketID=? and DeletionMark is null" );
            statement.setLong( 1, bucket.getId());
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
            	sobject = new SObject();
	        	sobject.setId(rs.getLong("ID"));
	        	sobject.setBucket(bucket);
	        	sobject.setItems(itemDao.getByObjectId(sobject.getId()));
	        	sobject.setNameKey(rs.getString("NameKey"));
	        	sobject.setOwnerCanonicalId(rs.getString("OwnerCanonicalID"));
	        	sobject.setNextSequence(rs.getInt("NextSequence"));
	        	sobject.setDeletionMark(rs.getString("DeletionMark"));
	        	sobject.setCreateTime(rs.getTimestamp("CreateTime"));
	        	items.add(sobject);
            }
            
        }finally {
        	statement.close();
        	closeConnection();
        }
		return items;
	}
	
	public List<SObject> listAllBucketObjects(SBucket bucket, String prefix,
			String marker, int maxKeys) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		
		List<SObject> items = new ArrayList<SObject>();
		SObjectItemDao itemDao = new SObjectItemDao();
		PreparedStatement statement = null;
	    SObject sobject = null;
        openConnection();	
        try {            
        	statement = conn.prepareStatement ( "SELECT * from sobject where SBucketID=?" );
            statement.setLong( 1, bucket.getId());
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
            	sobject = new SObject();
	        	sobject.setId(rs.getLong("ID"));
	        	sobject.setBucket(bucket);
	        	sobject.setItems(itemDao.getByObjectId(sobject.getId()));
	        	sobject.setNameKey(rs.getString("NameKey"));
	        	sobject.setOwnerCanonicalId(rs.getString("OwnerCanonicalID"));
	        	sobject.setNextSequence(rs.getInt("NextSequence"));
	        	sobject.setDeletionMark(rs.getString("DeletionMark"));
	        	sobject.setCreateTime(rs.getTimestamp("CreateTime"));
	        	items.add(sobject);
            }
            
        }finally {
        	statement.close();
        	closeConnection();
        }
		return items;
	}
	
	
	private void openConnection() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		if (null == conn) {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection( "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + awsapi_dbName, dbUser, dbPassword );
		}
	}

	private void closeConnection() throws SQLException {
		if (null != conn)
			conn.close();
		conn = null;
	}

	public void delete(SObject sobject) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
	    PreparedStatement statement = null;
        openConnection();	
        try {            
            statement = conn.prepareStatement ( "DELETE from sobject where ID=?" );
            statement.setLong( 1, sobject.getId());
            statement.executeUpdate();
        }finally {
        	statement.close();
        	closeConnection();
        }
		
	}

	public void update(SObject sobject, String string) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		PreparedStatement statement = null;
        openConnection();	
        try {            
            statement = conn.prepareStatement ( "UPDATE sobject set DeletionMark=? where ID=?" );
            statement.setString(1, sobject.getDeletionMark());
            statement.setLong(2, sobject.getId());
            statement.executeUpdate();
        }finally {
        	statement.close();
        	closeConnection();
        }
		
	}

	public void updateVerionSeq(SObject object) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		PreparedStatement statement = null;
        openConnection();	
        try {            
            statement = conn.prepareStatement ( "UPDATE sobject set NextSequence=? where ID=?" );
            statement.setInt(1, object.getNextSequence());
            statement.setLong(2, object.getId());
            statement.executeUpdate();
        }finally {
        	statement.close();
        	closeConnection();
        }
		
	}

	public long save(SObject object) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		PreparedStatement pstmt = null;
        openConnection();
        long id = 0;
        java.sql.Timestamp dateTime = new Timestamp( new Date().getTime());

        try {            
        	pstmt = conn.prepareStatement ( "INSERT into sobject (SBucketID, NameKey, OwnerCanonicalID, NextSequence, DeletionMark, CreateTime) VALUES (?,?,?,?,?,?)");
        	pstmt.setLong(1, object.getBucket().getId());
        	pstmt.setString(2, object.getNameKey());
        	pstmt.setString(3, object.getOwnerCanonicalId());
        	pstmt.setInt(4, object.getNextSequence());
        	pstmt.setString(5, object.getDeletionMark());
        	pstmt.setTimestamp(6, dateTime);
        	pstmt.executeUpdate();
        	pstmt.close();
        	pstmt = conn.prepareStatement ( "SELECT ID from sobject where SBucketID=? and  NameKey=?");
        	pstmt.setLong(1, object.getBucket().getId());
        	pstmt.setString(2, object.getNameKey());
        	ResultSet rs = pstmt.executeQuery();
        	if (rs.next()) {
        		id = rs.getLong("ID");
        		object.setId(id);
        	}
        	return id;
        } finally {
        	closeConnection();
        }
	}
	
}
