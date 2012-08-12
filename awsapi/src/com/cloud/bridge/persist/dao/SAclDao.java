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

import com.cloud.bridge.model.SAcl;
import com.cloud.bridge.service.core.s3.S3AccessControlList;
import com.cloud.bridge.service.core.s3.S3Grant;

/**
 * @author Kelven Yang
 */
public class SAclDao extends BaseDao {
	
	private Connection conn= null;
	public SAclDao() {
	}
	
	public List<SAcl> listGrants(String target, long targetId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		PreparedStatement statement = null;
		List<SAcl> acls = new ArrayList<SAcl>();
		SAcl acl = null;
		openConnection();	
		try {            
		    statement = conn.prepareStatement ( "SELECT * from acl where Target=? and TargetId=? order by grantOrder asc" );
		    statement.setString( 1, target);
		    statement.setLong( 2, targetId);
		    ResultSet rs = statement.executeQuery();
		    while(rs.next()) {
		    	acl = new SAcl();
		    	acl.setId(rs.getLong("ID"));
		    	acl.setTarget(target);
		    	acl.setTargetId(targetId);
		    	acl.setGranteeType(rs.getInt("GranteeType"));
		    	acl.setGranteeCanonicalId(rs.getString("GranteeCanonicalID"));
		    	acl.setPermission(rs.getInt("Permission"));
		    	acl.setGrantOrder(rs.getInt("GrantOrder"));
		    	acl.setCreateTime(rs.getTimestamp("CreateTime"));
		    	acl.setLastModifiedTime (rs.getTimestamp("LastModifiedTime"));
		    	acls.add(acl);
		    }
		   
		    
		}finally  {
			statement.close();
			closeConnection();
		}
	    return acls;
	}	
	public List<SAcl> listGrants(String target, long targetId, String userCanonicalId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		PreparedStatement statement = null;
		List<SAcl> acls = new ArrayList<SAcl>();
		SAcl acl = null;
		openConnection();	
		try {            
		    statement = conn.prepareStatement ( "SELECT * from acl where Target=? and TargetId=? order by grantOrder asc" );
		    statement.setString( 1, target);
		    statement.setLong( 2, targetId);
		    ResultSet rs = statement.executeQuery();
		    while(rs.next()) {
		    	acl = new SAcl();
		    	acl.setId(rs.getLong("ID"));
		    	acl.setTarget(target);
		    	acl.setTargetId(targetId);
		    	acl.setGranteeType(rs.getInt("GranteeType"));
		    	acl.setGranteeCanonicalId(rs.getString("GranteeCanonicalID"));
		    	acl.setPermission(rs.getInt("Permission"));
		    	acl.setGrantOrder(rs.getInt("GrantOrder"));
		    	acl.setCreateTime(rs.getTimestamp("CreateTime"));
		    	acl.setLastModifiedTime (rs.getTimestamp("LastModifiedTime"));
		    	acls.add(acl);
		    }
		    
		    }finally  {
				statement.close();
				closeConnection();
			}
		    return acls;

		
	}

	public void save(String target, long targetId, S3AccessControlList acl) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		// -> the target's ACLs are being redefined
		executeUpdate(target, targetId);
		
		if(acl != null) {
			S3Grant[] grants = acl.getGrants();
			if(grants != null && grants.length > 0) {
				int grantOrder = 1;
				for(S3Grant grant : grants) {
					save(target, targetId, grant, grantOrder++);
				}
			}
		}
	}
	
	private void executeUpdate(String target, long targetId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		PreparedStatement statement = null;
		openConnection();
		try {            
		    statement = conn.prepareStatement ( "DELETE from acl where target=? and targetId=?" );
		    statement.setString( 1, target);
		    statement.setLong( 2, targetId);
		    statement.executeUpdate();
		}finally {
			closeConnection();
		}
		
		
	}

	public SAcl save(String target, long targetId, S3Grant grant, int grantOrder) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		PreparedStatement pstmt = null;
		SAcl aclEntry = new SAcl();
		aclEntry.setTarget(target);
		aclEntry.setTargetId(targetId);
		aclEntry.setGrantOrder(grantOrder);
		
		int grantee = grant.getGrantee();
		aclEntry.setGranteeType(grantee);
		aclEntry.setPermission(grant.getPermission());
		aclEntry.setGranteeCanonicalId(grant.getCanonicalUserID());

		Date tod = new Date();
        java.sql.Timestamp dateTime = new Timestamp( tod.getTime());
		
		aclEntry.setCreateTime(dateTime);
		aclEntry.setLastModifiedTime(dateTime);
		
		openConnection();
		
		try {            
			    pstmt = conn.prepareStatement ( "INSERT into acl (Target, TargetID, GranteeType, GranteeCanonicalID, Permission,GrantOrder,CreateTime, LastModifiedTime) VALUES(?,?,?,?,?,?,?,?)" );
			    pstmt.setString( 1, target);
			    pstmt.setLong( 2, targetId);
			    pstmt.setInt(3, aclEntry.getGranteeType());
			    pstmt.setString(4, aclEntry.getGranteeCanonicalId());
			    pstmt.setInt(5, aclEntry.getPermission());
			    pstmt.setInt(6, aclEntry.getGrantOrder());
			    pstmt.setTimestamp(7, dateTime);
			    pstmt.setTimestamp(8, dateTime);
			    pstmt.executeUpdate();
			    pstmt.close();

			    pstmt = conn.prepareStatement ( "SELECT ID from acl where target=? and targetId=?" );
			    pstmt.setString( 1, target);
			    pstmt.setLong( 2, targetId);
			    ResultSet rs = pstmt.executeQuery(); 
			    if (rs.next()) {
			    	aclEntry.setId(rs.getLong("ID"));
			    }
			    pstmt.close();
			    
		} finally {
			closeConnection();
		}
		
		return aclEntry;
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

	public void delete(SAcl oneTag) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		executeUpdate(oneTag.getTarget(), oneTag.getTargetId());
		
	}
	
}
