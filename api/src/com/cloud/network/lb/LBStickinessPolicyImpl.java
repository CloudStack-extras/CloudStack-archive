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

package com.cloud.network.lb;

import java.util.HashMap;
import java.util.Map;
import com.cloud.network.rules.LBStickinessPolicy;



public class LBStickinessPolicyImpl implements LBStickinessPolicy{

	private long id;

	private long loadBalancerId;

	private String name;

	private String description;

	private String methodName;

	private String paramsInDb;

	private boolean revoke = false;

	private Map<String,String> paramList;


	public LBStickinessPolicyImpl(String methodName, String paramlist, String description) {
		String[] temp;
		StringBuilder sb = new StringBuilder();
		this.methodName = methodName;
		this.description = description;
		temp = paramlist.split(",");
		this.paramList = new HashMap<String, String>();
		for (int i = 0; i < (temp.length-1); i=i+2) {
			this.paramList.put(temp[i], temp[i+1]);
			sb = sb.append(temp[i]).append(",").append(temp[i+1]).append(",");
		}
		paramsInDb =  sb.toString();
		this.id =  0;
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

	public String getDescription() {
		return description;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getDBParams() {
		return paramsInDb;
	}

	public boolean isRevoke() {
		return revoke;
	}

	public void setRevoke(boolean revoke) {
		this.revoke = revoke;
	}

	public Map<String,String> getParams() {
		return paramList;
	}

	public void setParams(Map<String,String> param) {
		this.paramList = param;
	}
}

