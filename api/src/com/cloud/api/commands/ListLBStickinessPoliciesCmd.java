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

package com.cloud.api.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.BaseCmd.CommandType;
import com.cloud.api.response.LBStickinessResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.LBStickinessPolicyResponse;
import com.cloud.api.response.SecurityGroupResponse;
import com.cloud.network.rules.StickinessPolicy;
import com.cloud.network.rules.LoadBalancer;


@Implementation(description = "Lists LBStickiness policies.", responseObject = LBStickinessResponse.class)
public class ListLBStickinessPoliciesCmd extends BaseListCmd {
    public static final Logger s_logger = Logger
            .getLogger(ListLBStickinessPoliciesCmd.class.getName());

    private static final String s_name = "listLBStickinessPoliciesresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.LBID, type = CommandType.LONG, required = true, description = "the ID of the load balancer rule")
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

        List<? extends StickinessPolicy> stickinesspolicies = _lbService
                .searchForLBStickinessPolicies(this);

        LoadBalancer lb = _lbService.findById(getLbRuleId());

        LBStickinessResponse spResponse = _responseGenerator
                .createLBStickinessPolicyResponse(stickinesspolicies, lb);
        List<LBStickinessResponse> spResponses = new ArrayList<LBStickinessResponse>();
        ListResponse<LBStickinessResponse> response = new ListResponse<LBStickinessResponse>();
        spResponses.add(spResponse);

        response.setResponses(spResponses);
        spResponse.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

}
