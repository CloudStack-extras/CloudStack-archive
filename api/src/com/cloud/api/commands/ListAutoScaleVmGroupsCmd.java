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
import com.cloud.api.BaseListProjectAndAccountResourcesCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.response.AutoScaleVmGroupResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.as.AutoScaleVmGroup;

@Implementation(description = "Lists autoscale vm groups.", responseObject = AutoScaleVmGroupResponse.class)
public class ListAutoScaleVmGroupsCmd extends BaseListProjectAndAccountResourcesCmd {
    public static final Logger s_logger = Logger.getLogger(ListAutoScaleVmGroupsCmd.class.getName());

    private static final String s_name = "listautoscalevmgroupsresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @IdentityMapper(entityTableName="autoscale_vmgroups")
    @Parameter(name = ApiConstants.ID, type = CommandType.LONG, description = "the ID of the autoscale vm group")
    private Long id;

    @IdentityMapper(entityTableName="firewall_rules")
    @Parameter(name = ApiConstants.LBID, type = CommandType.LONG, description = "the ID of the loadbalancer")
    private Long loadBalancerId;

    @IdentityMapper(entityTableName="autoscale_vmprofiles")
    @Parameter(name = ApiConstants.VMPROFILE_ID, type = CommandType.LONG, description = "the ID of the profile")
    private Long profileId;

    @IdentityMapper(entityTableName="autoscale_policies")
    @Parameter(name = ApiConstants.POLICY_ID, type = CommandType.LONG, description = "the ID of the policy")
    private Long policyId;

    @IdentityMapper(entityTableName="data_center")
    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.LONG, description = "the availability zone ID")
    private Long zoneId;
    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public Long getLoadBalancerId() {
        return loadBalancerId;
    }


    public Long getProfileId() {
        return profileId;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public Long getZoneId() {
        return zoneId;
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
        if(id != null && (loadBalancerId != null || profileId != null || policyId != null))
            throw new InvalidParameterValueException("When id is specified other parameters need not be specified");

        List<? extends AutoScaleVmGroup> autoScaleGroups = _autoScaleService.listAutoScaleVmGroups(this);
        ListResponse<AutoScaleVmGroupResponse> response = new ListResponse<AutoScaleVmGroupResponse>();
        List<AutoScaleVmGroupResponse> responses = new ArrayList<AutoScaleVmGroupResponse>();
        if (autoScaleGroups != null) {
            for (AutoScaleVmGroup autoScaleVmGroup : autoScaleGroups) {
                AutoScaleVmGroupResponse autoScaleVmGroupResponse = _responseGenerator.createAutoScaleVmGroupResponse(autoScaleVmGroup);
                autoScaleVmGroupResponse.setObjectName("autoscalevmgroup");
                responses.add(autoScaleVmGroupResponse);
            }
        }
        response.setResponses(responses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }
}
