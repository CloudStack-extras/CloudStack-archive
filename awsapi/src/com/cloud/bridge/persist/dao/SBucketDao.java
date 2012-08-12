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
import java.util.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.cloud.bridge.model.SBucket;

/**
 * @author Kelven Yang
 */
public class SBucketDao extends BaseDao {

	private Connection conn = null;
	
	public SBucketDao() { 
	}

	public SBucket getByName(String bucketName) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException  {
		
		PreparedStatement statement = null;
        openConnection();
        try {
        	statement = conn.prepareStatement( "SELECT * from sbucket where name=?");
            statement.setString( 1, bucketName );
            ResultSet rs= statement.executeQuery();
            return convertToObject(rs);
        } finally {
            statement.close();
            closeConnection();
        }
		
	}
	
	private SBucket convertToObject(ResultSet rs) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		SBucket sbucket = null;
		SHostDao shostDao = null;
		if (rs.next()) {
			sbucket = new SBucket();
			shostDao = new SHostDao();
			sbucket.setId(rs.getLong("ID"));
			sbucket.setName(rs.getString("Name"));
			sbucket.setShost(shostDao.getById(rs.getLong("SHostID")));
			sbucket.setOwnerCanonicalId(rs.getString("ownerCanonicalId"));
			sbucket.setVersioningStatus(rs.getInt("VersioningStatus"));
			sbucket.setCreateTime(rs.getTimestamp("CreateTime"));
		}
		return sbucket;
		
	}

	public List<SBucket> listBuckets(String canonicalId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		PreparedStatement statement = null;
		List<SBucket> buckets = new ArrayList<SBucket>();
		SBucket sbucket = null;
        openConnection();
        try {
        	statement = conn.prepareStatement( "SELECT * from sbucket where ownerCanonicalId=? order by createTime asc");
            statement.setString( 1, canonicalId );
            ResultSet rs= statement.executeQuery();

            while (rs.next()) {
	        	sbucket = new SBucket();
				sbucket.setId(rs.getLong("ID"));
				sbucket.setName(rs.getString("Name"));
				sbucket.setOwnerCanonicalId(rs.getString("ownerCanonicalId"));
				sbucket.setVersioningStatus(rs.getInt("VersioningStatus"));
				sbucket.setCreateTime(rs.getTime("CreateTime"));
				buckets.add(sbucket);
            }
            
        }catch(Exception e){
        	
        }finally {
        	statement.close();
        	closeConnection();
        }
		
        return buckets;
		
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
		if (null != conn)
			conn.close();
		conn = null;
	}
	
	
	public void update(SBucket sbucket ) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		PreparedStatement statement = null;
        openConnection();
        try {
        	statement = conn.prepareStatement( "UPDATE sbucket SET VersioningStatus=? where ID=?" );
            statement.setInt( 1, sbucket.getVersioningStatus());
            statement.setLong( 2, sbucket.getId());
            statement.executeUpdate();
            statement.close();
        } finally {
        	statement.close();
            closeConnection();
        }
	}

	public long save(SBucket sbucket) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		PreparedStatement pstmt = null;
        openConnection();
		Date tod = new Date();
        java.sql.Timestamp dateTime = new Timestamp( tod.getTime());
        long id=-1;
        try {
        	pstmt = conn.prepareStatement( "INSERT into sbucket (Name, OwnerCanonicalID, SHostID, CreateTime, VersioningStatus) VALUES (?,?,?,?,?)" );
        	long shostid = sbucket.getShost().getId();
        	pstmt.setString(1, sbucket.getName());
        	pstmt.setString(2, sbucket.getOwnerCanonicalId());
        	pstmt.setLong(3, shostid);
        	pstmt.setTimestamp(4, dateTime);
            pstmt.setInt(5, sbucket.getVersioningStatus());
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = conn.prepareStatement( "SELECT ID from sbucket where Name=? and OwnerCanonicalID=? and SHostID=?");
            pstmt.setString(1, sbucket.getName());
            pstmt.setString(2, sbucket.getOwnerCanonicalId());
            pstmt.setLong(3, shostid);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
            	id = rs.getLong("ID");
            }
            return id;
        } finally {
        	pstmt.close();
            closeConnection();
        }
	}

	public void delete(SBucket sbucket) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		PreparedStatement pst = null;
        openConnection();
        
        try {
        	pst = conn.prepareStatement( "DELETE from sbucket where ID=?" );
            pst.setLong(1, sbucket.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeConnection();
        }
		
	}


}
