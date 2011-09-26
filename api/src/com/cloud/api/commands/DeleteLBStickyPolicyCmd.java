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
package com.cloud.api.commands;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.rules.LBStickyPolicy;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.user.Account;
import com.cloud.user.UserContext;

@Implementation(description="Deletes a LB sticky rule.", responseObject=SuccessResponse.class)
public class DeleteLBStickyPolicyCmd extends BaseAsyncCmd {
    public static final Logger s_logger = Logger.getLogger(DeleteLBStickyPolicyCmd.class.getName());
    private static final String s_name = "deleteLBstickyrruleresponse";
    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.ID, type=CommandType.LONG, required=true, description="the ID of the LB sticky rule")
    private Long id;


    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
    	LBStickyPolicy policy = _entityMgr.findById(LBStickyPolicy.class, getId());
    	
		if (policy != null) {
			LoadBalancer lb = _entityMgr.findById(LoadBalancer.class,
					policy.getLoadBalancerId());
			if (lb != null) {
				return lb.getAccountId();
			}
		}

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_LB_STICKYPOLICY_DELETE;
    }

    @Override
    public String getEventDescription() {
        return  "deleting load balancer sticky policy: " + getId();
    }
	
    @Override
    public void execute(){
        UserContext.current().setEventDetails("Load balancer sticky policy Id: "+getId());
        boolean result = _lbService.deleteLBStickyPolicy(getId());
        
        if (result) {
            SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to delete load balancer sticky policy");
        }
    }
    
    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
    	LBStickyPolicy policy = _entityMgr.findById(LBStickyPolicy.class, getId());
    	if (policy == null) {
    		throw new InvalidParameterValueException("Unable to find LB sticky rule: " + id);
    	}
    	LoadBalancer lb = _lbService.findById(policy.getLoadBalancerId());
    	if(lb == null){
    		throw new InvalidParameterValueException("Unable to find load balancer rule for sticky rule: " + id);
    	}
        return lb.getNetworkId();
    }
}
