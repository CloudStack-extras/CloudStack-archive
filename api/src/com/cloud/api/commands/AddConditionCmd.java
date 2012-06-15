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
import com.cloud.api.response.ConditionResponse;
import com.cloud.event.EventTypes;
import com.cloud.network.as.Condition;
import com.cloud.user.Account;

/**
 * @author Deepak Garg
 */


@Implementation(description="Adds condition using a counter", responseObject = ConditionResponse.class)
public class AddConditionCmd extends BaseAsyncCmd {
    public static final Logger s_logger = Logger.getLogger(AddConditionCmd.class.getName());
    private static final String s_name = "conditionresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.NAME, type=CommandType.STRING, required = true, description="Name of the condition.")
    private String name;

    @IdentityMapper(entityTableName="counter")
    @Parameter(name=ApiConstants.COUNTER_ID, type=CommandType.LONG, required = true, description="ID of the Counter.")
    private long counterId;

    @Parameter(name=ApiConstants.RELATIONAL_OPERATOR, type=CommandType.STRING, required = true, description="Relational Operator to be used with threshold.")
    private String relationalOperator;

    @Parameter(name=ApiConstants.THRESHOLD, type=CommandType.LONG, required = true, description="Threshold value.")
    private Long threshold;

    @Parameter(name=ApiConstants.AGGR_FUNCTION, type=CommandType.STRING, required = false, description="Aggregate Function.")
    private String aggrFunction;

    @Parameter(name=ApiConstants.AGGR_OPERATOR, type=CommandType.STRING, required = false, description="Aggregate Operator.")
    private String aggrOperator;

    @Parameter(name=ApiConstants.AGGR_VALUE, type=CommandType.LONG, required = false, description="Aggregate Value.")
    private Long aggrValue;


    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

	@Override
	public void execute() {
		Condition cndn = null;
		cndn = _lbService.addCondition(this);

		if(cndn != null){
			ConditionResponse response = _responseGenerator.createConditionResponse(cndn);
			response.setResponseName(getCommandName());
			this.setResponseObject(response);
		} else {
			throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to create Condition with name " + getName());
    	}
	}

	///////////////////////////////////////////////////
	/////////////////// Accessors ///////////////////////
	/////////////////////////////////////////////////////

	@Override
	public String getCommandName() {
		return s_name;
	}

	public String getName() {
		return name;
	}

	public Long getCounterId() {
		return counterId;
	}

	public String getRelationalOperator() {
		return relationalOperator;
	}

	public Long getThreshold() {
		return threshold;
	}

	public String getAggrFunction() {
		return aggrFunction;
	}

	public String getAggrOperator() {
		return aggrOperator;
	}

	public Long getAggrValue() {
		return aggrValue;
	}

	@Override
	public String getEventDescription() {
		return "creating a new Condition";
	}

	@Override
	public String getEventType() {
		return EventTypes.EVENT_CONDITION_CREATE;
	}

	@Override
	public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
	}
}