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
import java.util.ArrayList;
import java.util.List;

import com.cloud.bridge.model.SMeta;
import com.cloud.bridge.service.core.s3.S3MetaDataEntry;

/**
 * @author Kelven Yang, John Zucker
 */
public class SMetaDao extends BaseDao {
	private Connection conn = null;
	public SMetaDao() {
	}
	
	public List<SMeta> getByTarget(String target, long targetId) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		List<SMeta> metaList = new ArrayList<SMeta>();
		SMeta meta = null;
		PreparedStatement pstmt = null;
		try {
			openConnection();
			pstmt = conn.prepareStatement( "SELECT * from meta where Target=? and TargetID=?");
			pstmt.setString(1, target);
			pstmt.setLong(2, targetId);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				meta = new SMeta();
				meta.setId(rs.getLong("ID"));
				meta.setTarget(rs.getString("Target"));
				meta.setTargetId(rs.getLong("TargetID"));
				meta.setName(rs.getString("Name"));
				meta.setValue(rs.getString("Value"));
				metaList.add(meta);
			}
		}finally {
			pstmt.close();
			closeConnection();
		}
		return metaList;
	}

	public SMeta save(String target, long targetId, S3MetaDataEntry entry) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		SMeta meta = new SMeta();
		meta.setTarget(target);
		meta.setTargetId(targetId);
		meta.setName(entry.getName());
		meta.setValue(entry.getValue());
		PreparedStatement pstmt = null;
		openConnection();
		try {            
			
			pstmt = conn.prepareStatement ( "INSERT into meta (Target, TargetID, Name, Value) VALUES(?,?,?,?)" );
		    pstmt.setString( 1, target);
		    pstmt.setLong( 2, targetId);
		    pstmt.setString(3, meta.getName());
		    pstmt.setString(4, meta.getValue());
		    pstmt.executeUpdate();
		    pstmt.close();
		    // Get the id of the created acl and set it in acl object
		    pstmt = conn.prepareStatement ( "SELECT ID from meta where Target=? and TargetID=?" );
		    pstmt.setString( 1, target);
		    pstmt.setLong( 2, targetId);
		    ResultSet rs = pstmt.executeQuery(); 
		    if (rs.next()) {
		    	meta.setId(rs.getLong("ID"));
		    }
		} finally {
			pstmt.close();
			closeConnection();
		}
		return meta;
	}
	
	public void save(String target, long targetId, S3MetaDataEntry[] entries) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		executeUpdate(target, targetId);

		if(entries != null) {
			for(S3MetaDataEntry entry : entries)
				save(target, targetId, entry);
		}
	}
	
	private void executeUpdate(String target, long targetId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		PreparedStatement pstmt = null;
		try {
			openConnection();
			pstmt = conn.prepareStatement("DELETE from meta where target=? and targetId=?");
			pstmt.setString(1, target);
			pstmt.setLong(2, targetId);
			pstmt.executeUpdate();
		} finally {
			pstmt.close();
			closeConnection();
		}
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

	public void delete(SMeta oneTag) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		executeUpdate(oneTag.getTarget(), oneTag.getTargetId());
	}
	
}
