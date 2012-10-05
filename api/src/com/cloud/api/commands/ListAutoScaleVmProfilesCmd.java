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
import com.cloud.api.response.AutoScaleVmProfileResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.network.as.AutoScaleVmProfile;

@Implementation(description = "Lists autoscale vm profiles.", responseObject = AutoScaleVmProfileResponse.class)
public class ListAutoScaleVmProfilesCmd extends BaseListProjectAndAccountResourcesCmd {
    public static final Logger s_logger = Logger.getLogger(ListAutoScaleVmProfilesCmd.class.getName());

    private static final String s_name = "listautoscalevmprofilesresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @IdentityMapper(entityTableName="autoscale_vmprofiles")
    @Parameter(name = ApiConstants.ID, type = CommandType.LONG, description = "the ID of the autoscale vm profile")
    private Long id;

    @IdentityMapper(entityTableName="vm_template")
    @Parameter(name=ApiConstants.TEMPLATE_ID, type=CommandType.LONG, description="the templateid of the autoscale vm profile")
    private Long templateId;

    @Parameter(name=ApiConstants.OTHER_DEPLOY_PARAMS, type=CommandType.STRING, description="the otherdeployparameters of the autoscale vm profile")
    private String otherDeployParams;
    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public String getOtherDeployParams() {
        return otherDeployParams;
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
        List<? extends AutoScaleVmProfile> autoScaleProfiles = _autoScaleService.listAutoScaleVmProfiles(this);
        ListResponse<AutoScaleVmProfileResponse> response = new ListResponse<AutoScaleVmProfileResponse>();
        List<AutoScaleVmProfileResponse> responses = new ArrayList<AutoScaleVmProfileResponse>();
        if (autoScaleProfiles != null) {
            for (AutoScaleVmProfile autoScaleVmProfile : autoScaleProfiles) {
                AutoScaleVmProfileResponse autoScaleVmProfileResponse = _responseGenerator.createAutoScaleVmProfileResponse(autoScaleVmProfile);
                autoScaleVmProfileResponse.setObjectName("autoscalevmprofile");
                responses.add(autoScaleVmProfileResponse);
            }
        }
        response.setResponses(responses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

}
