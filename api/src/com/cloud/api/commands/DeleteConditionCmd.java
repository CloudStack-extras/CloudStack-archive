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
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.async.AsyncJob;
import com.cloud.event.EventTypes;
import com.cloud.user.UserContext;

@Implementation(description = "Removes a condition", responseObject = SuccessResponse.class)
public class DeleteConditionCmd extends BaseAsyncCmd {
    public static final Logger s_logger = Logger.getLogger(DeleteConditionCmd.class.getName());
    private static final String s_name = "deleteconditionresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @IdentityMapper(entityTableName = "conditions")
    @Parameter(name = ApiConstants.ID, type = CommandType.LONG, required = true, description = "the ID of the condition.")
    private Long id;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "the account of the condition. " +
            "Must be used with the domainId parameter.")
    private String accountName;

    @IdentityMapper(entityTableName = "domain")
    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.LONG, description = "the domain ID of the account.")
    private Long domainId;

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() {
        boolean result = _lbService.deleteCondition(getId());
        if (result) {
            SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to delete condition.");
        }
    }

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Long getId() {
        return id;
    }

    @Override
    public AsyncJob.Type getInstanceType() {
        return AsyncJob.Type.Condition;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        Long accountId = finalyzeAccountId(accountName, domainId, null, true);
        if (accountId == null) {
            return UserContext.current().getCaller().getId();
        }

        return accountId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_CONDITION_DELETE;
    }

    @Override
    public String getEventDescription() {
        return "Deleting a condition.";
    }
}