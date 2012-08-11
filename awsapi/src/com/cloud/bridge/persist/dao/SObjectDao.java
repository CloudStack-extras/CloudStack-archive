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
		//super(SObject.class);
	}

	public SObject getByNameKey(SBucket bucket, String nameKey) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		/*return queryEntity("from SObject where bucket=? and nameKey=?", 
				new Object[] { new EntityParam(bucket), nameKey });*/
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
	        /*
	         *  | ID               | bigint(20)   | NO   | PRI | NULL    | auto_increment |
				| SBucketID        | bigint(20)   | NO   | MUL | NULL    |                |
				| NameKey          | varchar(255) | NO   |     | NULL    |                |
				| OwnerCanonicalID | varchar(150) | NO   | MUL | NULL    |                |
				| NextSequence     | int(11)      | NO   |     | 1       |                |
				| DeletionMark     | varchar(150) | YES  |     | NULL    |                |
				| CreateTime       | datetime     | YES  | MUL | NULL    |                |
				+------------------+--------------+------+-----+---------+----------------+
	         */
	        
            statement.close();	
            return sobject;
        } finally {
            closeConnection();
        }
	}
	
	public List<SObject> listBucketObjects(SBucket bucket, String prefix, String marker, int maxKeys) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		StringBuffer sb = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		List<SObject> items = new ArrayList<SObject>();
		/*
		 *  select sobject0_.ID as ID5_0_, items1_.ID as ID6_1_, sobject0_.NameKey as NameKey5_0_, sobject0_.OwnerCanonicalId as OwnerCan3_5_0_, sobject0_.NextSequence as NextSequ4_5_0_, sobject0_.DeletionMark as Deletion5_5_0_, sobject0_.CreateTime as CreateTime5_0_, sobject0_.SBucketID as SBucketID5_0_, items1_.Version as Version6_1_, MD5 as MD3_6_1_, items1_.StoredPath as StoredPath6_1_, items1_.StoredSize as StoredSize6_1_, items1_.CreateTime as CreateTime6_1_, items1_.LastModifiedTime as LastModi7_6_1_, items1_.LastAccessTime as LastAcce8_6_1_, items1_.SObjectID as SObjectID6_1_, items1_.SObjectID as SObjectID5_0__, items1_.ID as ID0__ from sobject sobject0_ left outer join sobject_item items1_ on sobject0_.ID=items1_.SObjectID where (deletionMark is null) and sobject0_.SBucketID=1

		 */
		
		/*sb.append("from sobject o left join fetch o.items where deletionMark is null and o.bucket=?");
		params.add(new EntityParam(bucket));
		
		if(prefix!= null && !prefix.isEmpty()) {
			sb.append(" and o.nameKey like ?");
			params.add(new String(prefix + "%"));
		}
		
		if(marker != null && !marker.isEmpty()) {
			sb.append(" and o.nameKey > ?");
			params.add(marker);
		}*/
		
		PreparedStatement statement = null;
	    SObject sobject = null;
	    SObjectItemDao itemDao = new SObjectItemDao();
        openConnection();
        //sb = new StringBuffer("from sobject sobject0_ left outer join sobject_item items1_ on sobject0_.ID=items1_.SObjectID where (deletionMark is null) and sobject0_.SBucketID=?");
        //sb.append("sobject0_.ID as ID5_0_, items1_.ID as ID6_1_, sobject0_.NameKey as NameKey5_0_, sobject0_.OwnerCanonicalId as OwnerCan3_5_0_, sobject0_.NextSequence as NextSequ4_5_0_, sobject0_.DeletionMark as Deletion5_5_0_, sobject0_.CreateTime as CreateTime5_0_, sobject0_.SBucketID as SBucketID5_0_, items1_.Version as Version6_1_, MD5 as MD3_6_1_, items1_.StoredPath as StoredPath6_1_, items1_.StoredSize as StoredSize6_1_, items1_.CreateTime as CreateTime6_1_, items1_.LastModifiedTime as LastModi7_6_1_, items1_.LastAccessTime as LastAcce8_6_1_, items1_.SObjectID as SObjectID6_1_, items1_.SObjectID as SObjectID5_0__, items1_.ID as ID0__ from sobject sobject0_ left outer join sobject_item items1_ on sobject0_.ID=items1_.SObjectID where (deletionMark is null) and sobject0_.SBucketID=?");
        
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
        	closeConnection();
        }
		return items;
		// return queryEntities(sb.toString(), 0, maxKeys, params.toArray());
	}
	
	public List<SObject> listAllBucketObjects(SBucket bucket, String prefix, String marker, int maxKeys) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		StringBuffer sb = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		List<SObject> items = new ArrayList<SObject>();
/*		sb.append("from sobject o left join fetch o.items where o.bucket=?");
		params.add(new EntityParam(bucket));
		
		if(prefix != null && !prefix.isEmpty()) {
			sb.append(" and o.nameKey like ?");
			params.add(new String(prefix + "%"));
		}
		
		if(marker != null && !marker.isEmpty()) {
			sb.append(" and o.nameKey > ?");
			params.add(marker);
		}
*/		
		SObjectItemDao itemDao = new SObjectItemDao();
		sb.append("from sobject sobject0_ left outer join sobject_item items1_ on sobject0_.ID=items1_.SObjectID where (deletionMark is null) and sobject0_.SBucketID=?");
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
        	closeConnection();
        }
		return items;
		
		//return queryEntities(sb.toString(), 0, maxKeys, params.toArray());
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
		//Date tod = new Date();
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
        /*
		 * +------------------+--------------+------+-----+---------+----------------+
| Field            | Type         | Null | Key | Default | Extra          |
+------------------+--------------+------+-----+---------+----------------+
| ID               | bigint(20)   | NO   | PRI | NULL    | auto_increment |
| SBucketID        | bigint(20)   | NO   | MUL | NULL    |                |
| NameKey          | varchar(255) | NO   |     | NULL    |                |
| OwnerCanonicalID | varchar(150) | NO   | MUL | NULL    |                |
| NextSequence     | int(11)      | NO   |     | 1       |                |
| DeletionMark     | varchar(150) | YES  |     | NULL    |                |
| CreateTime       | datetime     | YES  | MUL | NULL    |                |
+------------------+--------------+------+-----+---------+----------------+
		 */
		
	}

	/*public void update(SObjectItem item) {
		// TODO Auto-generated method stub
		

		PreparedStatement statement = null;
        openConnection();	
        try {            
            statement = conn.prepareStatement ( "UPDATE sobject set DeletionMark=?" );
            statement.setString(1, sobject.getDeletionMark());
            statement.executeUpdate();
        }finally {
        	statement.close();
        	closeConnection();
        }
		
	}
*/

	
}
