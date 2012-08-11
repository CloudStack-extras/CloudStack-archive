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

import org.apache.log4j.Logger;

import com.cloud.stack.models.CloudStackServiceOffering;


public class CloudStackSvcOfferingDao extends BaseDao {
	public static final Logger logger = Logger.getLogger(CloudStackSvcOfferingDao.class);
	private Connection conn = null;
	
	public CloudStackSvcOfferingDao() {
	}


/*	public CloudStackServiceOffering getSvcOfferingByName( String name ){
		return queryEntity("from CloudStackServiceOffering where name=?", new Object[] {name});
	}

    public CloudStackServiceOffering getSvcOfferingById( String id ){
        return queryEntity("from CloudStackServiceOffering where id=?", new Object[] {id});
    }
*/    
    public CloudStackServiceOffering getSvcOfferingById( String id ) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
    	
    	openConnection();
    	CloudStackServiceOffering offering = null;
		try { 
			PreparedStatement pstmt = conn.prepareStatement ( "select id, name, domain_id from disk_offering where name=?");
			pstmt.setString(1, id);
			ResultSet rst = pstmt.executeQuery();
			if (rst.next()) {
				offering = new CloudStackServiceOffering();
				offering.setId(rst.getString("id"));
				offering.setName(rst.getString("name"));
				offering.setDomainId( rst.getString("domain_id"));
				offering.setDomainId( rst.getString("domain_id"));
				return offering;
			}
		}catch (Exception e) {
			
		}
		finally {
			closeConnection();
		}
		
		return offering;
	}
    
public List<CloudStackServiceOffering> getSvcOfferingByName( String name ) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
    	
    	openConnection();
    	List<CloudStackServiceOffering> offerings = new ArrayList<CloudStackServiceOffering>();
    	CloudStackServiceOffering offering = null;
		try { 
			PreparedStatement pstmt = conn.prepareStatement ( "select id, name, domain_id, removed from disk_offering where id=?");
			pstmt.setString(1, name);
			ResultSet rst = pstmt.executeQuery();
			if (rst.next()) {
				offering = new CloudStackServiceOffering();
				offering.setId(rst.getString("id"));
				offering.setName(rst.getString("name"));
				offering.setDomainId( rst.getString("domain_id"));
				offering.setRemoved(rst.getString("removed"));
				offerings.add(offering);
			}
		}catch (Exception e) {
			
		}
		finally {
			closeConnection();
		}
		
		return offerings;
	}
    
	private void openConnection() 
	        throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException 
	    {
	        if (null == conn) {
	            Class.forName( "com.mysql.jdbc.Driver" ).newInstance();
	            conn = DriverManager.getConnection( "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + cloud_dbName, dbUser, dbPassword );
	        }
	    }

	private void closeConnection() throws SQLException {
		if (null != conn)
			conn.close();
		conn = null;
	}

}

/* This should be Easy to finish of.. */