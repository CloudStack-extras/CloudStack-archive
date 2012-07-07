//       Licensed to the Apache Software Foundation (ASF) under one
//       or more contributor license agreements.  See the NOTICE file
//       distributed with this work for additional information
//       regarding copyright ownership.  The ASF licenses this file
//       to you under the Apache License, Version 2.0 (the
//       "License"); you may not use this file except in compliance
//       with the License.  You may obtain a copy of the License at
//
//         http://www.apache.org/licenses/LICENSE-2.0
//
//       Unless required by applicable law or agreed to in writing,
//       software distributed under the License is distributed on an
//       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//       KIND, either express or implied.  See the License for the
//       specific language governing permissions and limitations
//       under the License.

package com.cloud.api.commands;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.AutoScaleVmProfileResponse;
import com.cloud.async.AsyncJob;
import com.cloud.event.EventTypes;

@Implementation(description = "Updates an existing autoscale vm profile.", responseObject = AutoScaleVmProfileResponse.class)
public class UpdateAutoScaleVmProfileCmd extends BaseAsyncCmd {
    public static final Logger s_logger = Logger.getLogger(UpdateAutoScaleVmProfileCmd.class.getName());

    private static final String s_name = "updateautoscalevmprofileresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @IdentityMapper(entityTableName = "autoscale_vmgroups")
    @Parameter(name = ApiConstants.ID, type = CommandType.LONG, required = true, description = "the ID of the autoscale group")
    private Long id;

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() throws ServerApiException {

    }

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_AUTOSCALEVMPROFILE_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "Updating AutoScale Vm Profile";
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
// Long projectId = null;
// String sProjectId = getOtherDeployParam("projectId");
//
// if (sProjectId != null)
// try {
// projectId = Long.parseLong(sProjectId);
// } catch (Exception ex) {
// throw new InvalidParameterValueException("projectid specified is invalid");
// }
//
// Long accountId = finalyzeAccountId(accountName, domainId, projectId, true);
// if (accountId == null) {
// return UserContext.current().getCaller().getId();
// }
//
// return accountId;
        return 0;
    }

    @Override
    public AsyncJob.Type getInstanceType() {
        return AsyncJob.Type.AutoScaleVmProfile;
    }
}
