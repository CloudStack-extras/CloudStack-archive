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
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.CounterResponse;
import com.cloud.event.EventTypes;
import com.cloud.network.as.Counter;
import com.cloud.user.Account;

/**
 * @author Deepak Garg
 */


@Implementation(description="Adds metric counter", responseObject = CounterResponse.class)
public class AddCounterCmd extends BaseAsyncCmd {
    public static final Logger s_logger = Logger.getLogger(AddCounterCmd.class.getName());
    private static final String s_name = "counterresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.NAME, type=CommandType.STRING, required = true, description="Name of the counter.")
    private String name;

    @Parameter(name=ApiConstants.SOURCE, type=CommandType.STRING, required = true, description="Source of the counter.")
    private String source;

    @Parameter(name=ApiConstants.VALUE, type=CommandType.LONG, required = false, description="Value in case of snmp or other specific counters.")
    private int value;

	///////////////////////////////////////////////////
	/////////////////// Accessors ///////////////////////
	/////////////////////////////////////////////////////

	public String getName() {
		return name;
	}

	public String getSource() {
		return source;
	}

	public int getValue() {
		return value;
	}


    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
	public void execute() {
    	Counter ctr = null;
    	ctr = _lbService.addCounter(this);

    	if(ctr != null){
        	CounterResponse response = _responseGenerator.createCounterResponse(ctr);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
    	} else {
    		throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to create Counter with name " + getName());
    	}

    }

	@Override
	public String getEventType() {
		return EventTypes.EVENT_COUNTER_CREATE;
	}

	@Override
	public String getEventDescription() {
		return "creating a new Counter";
	}

	@Override
	public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
	}
}
