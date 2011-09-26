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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.response.LBStickyResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.LBStickyRuleResponse;
import com.cloud.api.response.SecurityGroupResponse;
import com.cloud.network.rules.LBStickyPolicy;
import com.cloud.network.rules.LoadBalancer;


@Implementation(description = "Lists load balancer rules.", responseObject = LBStickyResponse.class)
public class ListLBStickyPoliciesCmd extends BaseListCmd {
    public static final Logger s_logger = Logger.getLogger(ListLBStickyPoliciesCmd.class.getName());

    private static final String s_name = "listLBStickyPoliciesresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.LONG, description = "the ID of the StickyPolicy rule")
    private Long id;



    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }



    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public void execute() {

        List<? extends LBStickyPolicy> stickypolicies = _lbService.searchForLBStickyPolicies(this);

        LoadBalancer lb = _lbService.findById(getId());
        
    	LBStickyResponse spResponse = _responseGenerator.createLBStickyPolicyResponse(stickypolicies,lb);
    	List<LBStickyResponse> spResponses = new ArrayList<LBStickyResponse>();
    	ListResponse<LBStickyResponse> response = new ListResponse<LBStickyResponse>();
    	spResponses.add(spResponse);

        response.setResponses(spResponses);
    	spResponse.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

}
