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
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.BaseCmd.CommandType;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.LBStickyRuleResponse;
import com.cloud.api.response.LBStickyResponse;
import com.cloud.network.rules.LBStickyPolicy;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.network.lb.LBStickyRule;
import com.cloud.user.Account;
import com.cloud.utils.component.Inject;


@Implementation(description = "Lists load balancer rules.", responseObject = LBStickyResponse.class)
public class ListLBStickyMethodsCmd extends BaseListCmd {
    public static final Logger s_logger = Logger.getLogger(ListLBStickyMethodsCmd.class.getName());

    private static final String s_name = "listLBStickyMethodsresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name=ApiConstants.LBID, type=CommandType.LONG, required=true, description="the ID of the load balancer rule")
    private Long lbRuleId;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getLbRuleId() {
        return lbRuleId;
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

        List<LBStickyRule> stickymethods = _lbService.getLBStickyMethods(this);
        LoadBalancer lb = _lbService.findById(getLbRuleId());
        
    	LBStickyResponse spResponse = _responseGenerator.createLBStickyMethodResponse(stickymethods,lb);
    	List<LBStickyResponse> spResponses = new ArrayList<LBStickyResponse>();
    	ListResponse<LBStickyResponse> response = new ListResponse<LBStickyResponse>();
    	spResponses.add(spResponse);   
        response.setResponses(spResponses);
    	spResponse.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

}
