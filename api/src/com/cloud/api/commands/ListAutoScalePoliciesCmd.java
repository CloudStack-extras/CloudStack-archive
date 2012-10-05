// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.api.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListAccountResourcesCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.response.AutoScalePolicyResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.network.as.AutoScalePolicy;

@Implementation(description = "Lists autoscale policies.", responseObject = AutoScalePolicyResponse.class)
public class ListAutoScalePoliciesCmd extends BaseListAccountResourcesCmd {
    public static final Logger s_logger = Logger.getLogger(ListAutoScalePoliciesCmd.class.getName());

    private static final String s_name = "listautoscalepoliciesresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @IdentityMapper(entityTableName = "autoscale_policies")
    @Parameter(name = ApiConstants.ID, type = CommandType.LONG, description = "the ID of the autoscale policy")
    private Long id;

    @IdentityMapper(entityTableName = "conditions")
    @Parameter(name = ApiConstants.CONDITION_ID, type = CommandType.LONG, description = "the ID of the condition of the policy")
    private Long conditionId;

    @Parameter(name = ApiConstants.ACTION, type = CommandType.STRING, required = true, description = "the action to be executed if all the conditions evaluate to true for the specified duration.")
    private String action;

    @IdentityMapper(entityTableName="autoscale_vmgroups")
    @Parameter(name = ApiConstants.VMGROUP_ID, type = CommandType.LONG, description = "the ID of the autoscale vm group")
    private Long vmGroupId;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public Long getConditionId() {
        return conditionId;
    }

    public String getAction() {
        return action;
    }

    public Long getVmGroupId() {
        return vmGroupId;
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
        List<? extends AutoScalePolicy> autoScalePolicies = _autoScaleService.listAutoScalePolicies(this);
        ListResponse<AutoScalePolicyResponse> response = new ListResponse<AutoScalePolicyResponse>();
        List<AutoScalePolicyResponse> responses = new ArrayList<AutoScalePolicyResponse>();
        if (autoScalePolicies != null) {
            for (AutoScalePolicy autoScalePolicy : autoScalePolicies) {
                AutoScalePolicyResponse autoScalePolicyResponse = _responseGenerator.createAutoScalePolicyResponse(autoScalePolicy);
                autoScalePolicyResponse.setObjectName("autoscalepolicy");
                responses.add(autoScalePolicyResponse);
            }
        }
        response.setResponses(responses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

}
