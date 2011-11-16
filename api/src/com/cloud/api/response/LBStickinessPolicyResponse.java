/**
 * Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
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

import com.cloud.network.rules.StickinessPolicy;
import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;
import java.util.Map;
import java.util.HashMap;

public class LBStickinessPolicyResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the LB Stickiness policy ID")
    private Long id;
    
    @SerializedName("name")
    @Param(description = "the name of the Stickiness policy")
    private String name;

    @SerializedName("methodname")
    @Param(description = "the method name of the Stickiness policy")
    private String methodName;

    @SerializedName("description")
    @Param(description = "the description of the Stickiness policy")
    private String description;;

    @SerializedName("state")
    @Param(description = "the state of the policy")
    private String state;

    // FIXME : if prams with the same name exists more then once then value are concatinated with ":" as delimitor .
    // Reason: Map does not support duplicate keys, need to look for the alernate data structure
    // Example: <params>{indirect=null, name=testcookie, nocache=null, domain=www.yahoo.com:www.google.com, postonly=null}</params>
    // in the above there are two domains with values www.yahoo.com and www.google.com
    @SerializedName("params")
    @Param(description = "the params of the policy")
    private Map<String, String> params;
   // private List<Pair<String, String>> params;
    
    public Long getId() {
        return id;
    }
    
    public Map<String, String> getParams() {
        return params;
    }


    public void setId(Long id) {
        this.id = id;
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

    public String getMethodName() {
        return methodName;
    }
    
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public LBStickinessPolicyResponse(StickinessPolicy stickinesspolicy) {
        this.name = stickinesspolicy.getName();
        String paramsInDB = stickinesspolicy.getParamsInDB();
        this.methodName = stickinesspolicy.getMethodName();
        this.description = stickinesspolicy.getDescription();
        if (stickinesspolicy.isRevoke()) {
            this.setState("Revoked");
        }
        if (stickinesspolicy.getId() != 0)
            this.id = stickinesspolicy.getId();

        /* Get the param and values from the database(dbparams) and fill the response object 
         * Format :  param1,value1,param2,value2,param3,value3 
         *  Example for App cookie method:  "name,cookapp,length,12,holdtime,3h" . Here 3 parameters name,length and holdtime with corresponsing values.
         * */
        String[] temp;
        temp = paramsInDB.split("[,]");
        Map<String, String> paramList =  new HashMap<String, String>();
        for (int i = 0; i < (temp.length - 1); i = i + 2) {
            StringBuilder sb = new StringBuilder();
            sb.append(temp[i+1]);
            if (paramList.get(temp[i]) != null)
            {
                sb.append(":").append(paramList.get(temp[i]));
            }
                
            paramList.put(temp[i],sb.toString());
        }
        this.params = paramList;
        setObjectName("stickinesspolicy");
    }

}
