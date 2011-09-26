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

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.BaseCmd.CommandType;
import com.cloud.api.response.LBStickyRuleResponse;
import com.cloud.api.response.LoadBalancerResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.event.EventTypes;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.rules.LBStickyPolicy;
import com.cloud.api.response.LBStickyResponse;
import com.cloud.network.IpAddress;
import com.cloud.network.lb.LoadBalancingRule;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.user.Account;
import com.cloud.user.UserContext;
import com.cloud.utils.StringUtils;
import com.cloud.utils.net.NetUtils;

@Implementation(description="Creates a Load Balancer sticky policy ", responseObject=LBStickyResponse.class)
public class CreateLBStickyPolicyCmd extends BaseAsyncCreateCmd  {
    public static final Logger s_logger = Logger.getLogger(CreateLBStickyPolicyCmd.class.getName());

    private static final String s_name = "createLBStickyPolicy";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////


    @Parameter(name=ApiConstants.LBID, type=CommandType.LONG, required=true, description="the ID of the load balancer rule")
    private Long lbRuleId;
    
    @Parameter(name=ApiConstants.DESCRIPTION, type=CommandType.STRING, description="the description of the LB Stickiness policy")
    private String description;

    @Parameter(name=ApiConstants.NAME, type=CommandType.STRING, required=true, description="name of the LB Stickiness policy")
    private String LBStickinessPolicyName;

    @Parameter(name=ApiConstants.METHOD_NAME, type=CommandType.STRING, required=true, description="name of the LB Stickiness policy")
    private String StickinessMethodName;
     
    @Parameter(name = ApiConstants.PARAM_LIST, type = CommandType.MAP, description = "param list. Example: param[0].name=cookiename&param0].value=LBCookie ")
    private Map paramList;
 

    
    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getLbRuleId() {
        return lbRuleId;
    }

    public String getDescription() {
        return description;
    }

    public String getLBStickyPolicyName() {
        return LBStickinessPolicyName;
    }

    public String getStickyMethodName() {
        return StickinessMethodName;
    }
    
    public Map getparamList() {
        return paramList;
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
        LoadBalancer lb = _entityMgr.findById(LoadBalancer.class, getLbRuleId());
        if (lb == null) {
            return Account.ACCOUNT_ID_SYSTEM; // bad id given, parent this command to SYSTEM so ERROR events are tracked
        }
        return lb.getAccountId();
    }
    
    @Override
    public void execute() throws ResourceAllocationException, ResourceUnavailableException {            	
        UserContext callerContext = UserContext.current();
        boolean success = true;
        LBStickyPolicy policy = null;
        try {
            UserContext.current().setEventDetails("Rule Id: " + getEntityId());
                   
            _lbService.applyLoadBalancerConfig(getLbRuleId()); //FIXME: failing when the host is down 
          //  success = success && _lbService.applyLoadBalancerConfig(getLbRuleId());
            
            // State might be different after the rule is applied, so get new object here
            policy = _entityMgr.findById(LBStickyPolicy.class, getEntityId());
            LoadBalancer lb = _lbService.findById(policy.getLoadBalancerId());
            LBStickyResponse spResponse =_responseGenerator.createLBStickyPolicyResponse(policy,lb);
            setResponseObject(spResponse);
            spResponse.setResponseName(getCommandName());
        } finally {
            if (!success || policy == null) {
                // no need to apply the rule on the backend as it exists in the db only
                _lbService.deleteLBStickyPolicy(getEntityId());
       
                throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to create sticky policy rule");
            }
        }
    }
     
    @Override
    public void create() {	
    	 try {
    		 LBStickyPolicy result = _lbService.createLBStickyPolicy(this);
             this.setEntityId(result.getId());
         } catch (NetworkRuleConflictException e) {
             s_logger.warn("Exception: ", e);
             throw new ServerApiException(BaseCmd.NETWORK_RULE_CONFLICT_ERROR, e.getMessage());
         } 
    }
 
    @Override
    public String getEventType() {
        return EventTypes.EVENT_LB_STICKYPOLICY_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "creating a Load Balancer stickiness policy: " + getLBStickyPolicyName() ;
    }

    public String getXid() {
        /*FIXME*/
        return null;
    }


}

