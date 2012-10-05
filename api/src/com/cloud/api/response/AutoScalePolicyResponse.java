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
package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.utils.IdentityProxy;
import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AutoScalePolicyResponse extends BaseResponse implements ControlledEntityResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the autoscale policy ID")
    private IdentityProxy id = new IdentityProxy("autoscale_policies");

    @SerializedName(ApiConstants.ACTION)
    @Param(description = "the action to be executed if all the conditions evaluate to true for the specified duration.")
    private String action;

    @SerializedName(ApiConstants.DURATION)
    @Param(description = "the duration for which the conditions have to be true before action is taken")
    private Integer duration;

    @SerializedName(ApiConstants.QUIETTIME)
    @Param(description = "the cool down period for which the policy should not be evaluated after the action has been taken")
    private Integer quietTime;

    @SerializedName("conditions")
    @Param(description = "the list of IDs of the conditions that are being evaluated on every interval")
    private List<ConditionResponse> conditions;

    @SerializedName(ApiConstants.ACCOUNT) @Param(description="the account owning the autoscale policy")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID) @Param(description="the project id autoscale policy")
    private IdentityProxy projectId = new IdentityProxy("projects");

    @SerializedName(ApiConstants.PROJECT) @Param(description="the project name of the autoscale policy")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID) @Param(description="the domain ID of the autoscale policy")
    private IdentityProxy domainId = new IdentityProxy("domain");

    @SerializedName(ApiConstants.DOMAIN) @Param(description="the domain name of the autoscale policy")
    private String domainName;

    public void setId(Long id) {
        this.id.setValue(id);
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public void setQuietTime(Integer quietTime) {
        this.quietTime = quietTime;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setConditions(List<ConditionResponse> conditions) {
        this.conditions = conditions;
    }

    @Override
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Override
    public void setDomainId(Long domainId) {
        this.domainId.setValue(domainId);
    }

    @Override
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public void setProjectId(Long projectId) {
        this.projectId.setValue(projectId);
    }

    @Override
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
