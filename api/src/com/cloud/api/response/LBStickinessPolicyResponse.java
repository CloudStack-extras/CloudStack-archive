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
package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.network.rules.StickinessPolicy;
import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class LBStickinessPolicyResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the LB stickiness policy rule ID")
    private Long id;

    @SerializedName("lbRuleId")
    @Param(description = "the LB rule ID")
    private Long lbRuleId;
    
    @SerializedName("name")
    @Param(description = "the name of the stickiness rule")
    private String name;
    
    @SerializedName("methodName")
    @Param(description = "the method name name of the stickiness rule")
    private String methodName;

    @SerializedName("description")
    @Param(description = "the description of the load balancer")
    private String description;;

    @SerializedName("account")
    @Param(description = "the account of the load balancer rule")
    private String accountName;

    @SerializedName("domainid")
    @Param(description = "the domain ID of the load balancer rule")
    private Long domainId;

    @SerializedName("domain")
    @Param(description = "the domain of the load balancer rule")
    private String domainName;

    @SerializedName("state")
    @Param(description = "the state of the rule")
    private String state;

    //FIXME : the format of display looks different from the input . input are of the form parame[0].name .... where as response is different , this is especially for the CreateLBstickinessPolicy
    @SerializedName("params")
    @Param(description = "the params of the rule")
    private Map<String,String>  params;
    
    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the id of the zone the rule belongs to")
    private Long zoneId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setlbRuleId(Long lbRuleId) {
        this.lbRuleId = lbRuleId;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    
	public LBStickinessPolicyResponse(StickinessPolicy stickinesspolicy) {
		this.name = stickinesspolicy.getName();
		String dbparams = stickinesspolicy.getDBParams();
		this.methodName = stickinesspolicy.getMethodName();
		this.description = stickinesspolicy.getDescription();
		if (stickinesspolicy.isRevoke())
		{
			this.setState("Revoked");
		}
		if (stickinesspolicy.getId() != 0)
		      this.id = stickinesspolicy.getId();

		String[] temp;
		temp = dbparams.split("[,]");
		Map<String, String> paramList = new HashMap<String, String>();
		for (int i = 0; i < (temp.length - 1); i = i + 2) {
			paramList.put(temp[i], temp[i + 1]);
		}
		this.params = paramList;
		setObjectName("stickinesspolicy");
	}

}
