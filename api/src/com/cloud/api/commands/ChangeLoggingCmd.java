// Copyright 2012 Citrix Systems, Inc. Licensed under the
// Apache License, Version 2.0 (the "License"); you may not use this
// file except in compliance with the License.  Citrix Systems, Inc.
// reserves all rights not expressly granted by the License.
// You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 
package com.cloud.api.commands;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.PlugService;
import com.cloud.api.BaseCmd.CommandType;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.element.LoggingExampleElementService;
import com.cloud.user.Account;



/**
 * @author edison
 *
 */
@Implementation(description="Change the logging level for the Logging Example", responseObject = SuccessResponse.class)
public class ChangeLoggingCmd extends BaseCmd {
    public static final Logger s_logger = Logger.getLogger(ChangeLoggingCmd.class.getName());    
    private static final String s_name = "changeloggingresponse"; 	
    
    @Parameter(name=ApiConstants.DETAILS, type=CommandType.STRING, required = true, description="Logging level: Can be only trace or debug")
    private String level;
    
    @PlugService LoggingExampleElementService _loggingExampleElementService;
    
    public ChangeLoggingCmd() {
    	
    }

	@Override
	public void execute() throws ResourceUnavailableException,
			InsufficientCapacityException, ServerApiException,
			ConcurrentOperationException, ResourceAllocationException,
			NetworkRuleConflictException {
		_loggingExampleElementService.changeLoggingLevel(level);
		SuccessResponse response = new SuccessResponse(getCommandName());
		this.setResponseObject(response);
	}

	@Override
	public String getCommandName() {
		return s_name;
	}

	@Override
	public long getEntityOwnerId() {
		return Account.ACCOUNT_ID_SYSTEM;
	}
    
}
