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

public class AutoScaleVmGroupResponse extends BaseResponse implements ControlledEntityResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the autoscale vm group ID")
    private IdentityProxy id = new IdentityProxy("autoscale_vmgroups");

    @SerializedName(ApiConstants.LBID)
    @Param(description = "the load balancer rule ID")
    private IdentityProxy loadBalancerId = new IdentityProxy("firewall_rules");

    @SerializedName(ApiConstants.VMPROFILE_ID)
    @Param(description = "the autoscale profile that contains information about the vms in the vm group.")
    private IdentityProxy profileId = new IdentityProxy("autoscale_vmprofiles");

    @SerializedName(ApiConstants.MIN_MEMBERS)
    @Param(description = "the minimum number of members in the vmgroup, the number of instances in the vm group will be equal to or more than this number.")
    private int minMembers;

    @SerializedName(ApiConstants.MAX_MEMBERS)
    @Param(description = "the maximum number of members in the vmgroup, The number of instances in the vm group will be equal to or less than this number.")
    private int maxMembers;

    @SerializedName(ApiConstants.INTERVAL)
    @Param(description = "the frequency at which the conditions have to be evaluated")
    private int interval;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the current state of the AutoScale Vm Group")
    private String state;

    @SerializedName(ApiConstants.SCALEUP_POLICIES)
    @Param(description = "list of scaleup autoscale policies")
    private List<AutoScalePolicyResponse> scaleUpPolicies;

    @SerializedName(ApiConstants.SCALEDOWN_POLICIES)
    @Param(description = "list of scaledown autoscale policies")
    private List<AutoScalePolicyResponse> scaleDownPolicies;

    @SerializedName(ApiConstants.ACCOUNT) @Param(description="the account owning the instance group")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID) @Param(description="the project id vm profile")
    private IdentityProxy projectId = new IdentityProxy("projects");

    @SerializedName(ApiConstants.PROJECT) @Param(description="the project name of the vm profile")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID) @Param(description="the domain ID of the vm profile")
    private IdentityProxy domainId = new IdentityProxy("domain");

    @SerializedName(ApiConstants.DOMAIN) @Param(description="the domain name of the vm profile")
    private String domainName;

    public AutoScaleVmGroupResponse() {

    }

    public void setId(Long id) {
        this.id.setValue(id);
    }

    public void setLoadBalancerId(Long loadBalancerId) {
        this.loadBalancerId.setValue(loadBalancerId);
    }

    public void setProfileId(Long profileId) {
        this.profileId.setValue(profileId);
    }

    public void setMinMembers(int minMembers) {
        this.minMembers = minMembers;
    }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setScaleUpPolicies(List<AutoScalePolicyResponse> scaleUpPolicies) {
        this.scaleUpPolicies = scaleUpPolicies;
    }

    public void setScaleDownPolicies(List<AutoScalePolicyResponse> scaleDownPolicies) {
        this.scaleDownPolicies = scaleDownPolicies;
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
