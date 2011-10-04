/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.cloud.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import com.cloud.network.rules.StickinessPolicy;



@Entity
@Table(name=("load_balancer_stickiness_policies"))
@PrimaryKeyJoinColumn(name="load_balancer_id", referencedColumnName = "id")
public class LBStickinessPolicyVO  implements StickinessPolicy{
	
	
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private long id;

    @Column(name="load_balancer_id")
    private long loadBalancerId;

    @Column(name="name")
    private String name;
    
    @Column(name="description")
    private String description;
    
    
    @Column(name="method_name")
    private String methodName;

    @Column(name="params")
    private String paramsInDb;
    
    @Column(name="revoke")
    private boolean revoke = false;
    

    
    protected LBStickinessPolicyVO()
    {
    }

    public LBStickinessPolicyVO(long loadBalancerId, String name, String methodName, Map paramList) {	
        this.loadBalancerId = loadBalancerId;
        this.name = name;
        this.methodName = methodName;
        StringBuilder sb = new StringBuilder();

        if (paramList != null){
			Collection userGroupCollection = paramList.values();
			Iterator iter = userGroupCollection.iterator();
			HashMap paramKVpair = (HashMap) iter.next();

			String paramName = (String) paramKVpair.get("name");
			String paramValue = (String) paramKVpair.get("value");
			sb.append(paramName + "," + paramValue);

			while (iter.hasNext()) {
				paramKVpair = (HashMap) iter.next();
				paramName = (String) paramKVpair.get("name");
				paramValue = (String) paramKVpair.get("value");
				sb.append("," + paramName + "," + paramValue);
			}
			paramsInDb = sb.toString();
		}else
        {
        	paramsInDb = "";
        }
    }

    public long getId() {
        return id;
    }

    public long getLoadBalancerId() {
        return loadBalancerId;
    }

    public String getName() {
        return name;
    }
    
	  public String getDescription()
	  {
		  return description;
	  }
    
    public String getMethodName() {
        return methodName;
    }
    
    public String getDBParams() {
        return paramsInDb;
    }

    public Map getParams() {
        return null;
    }
    
    public boolean isRevoke() {
        return revoke;
    }

    public void setRevoke(boolean revoke) {
        this.revoke = revoke;
    }
}
