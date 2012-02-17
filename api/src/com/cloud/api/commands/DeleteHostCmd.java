/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.cloud.api.commands;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.user.Account;

@Implementation(description = "Deletes a host.", responseObject = SuccessResponse.class)
public class DeleteHostCmd extends BaseCmd {
    public static final Logger s_logger = Logger.getLogger(DeleteHostCmd.class.getName());

    private static final String s_name = "deletehostresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @IdentityMapper(entityTableName="host")
    @Parameter(name = ApiConstants.ID, type = CommandType.LONG, required = true, description = "the host ID")
    private Long id;

    @Parameter(name = ApiConstants.FORCED, type = CommandType.BOOLEAN, description = "Force delete the host. All HA enabled vms running on the host will be put to HA; HA disabled ones will be stopped")
    private Boolean forced;
    
    @Parameter(name = ApiConstants.FORCED_DESTROY_LOCAL_STORAGE, type = CommandType.BOOLEAN, description = "Force destroy local storage on this host. All VMs created on this local storage will be destroyed")
    private Boolean forceDestroyLocalStorage;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public boolean isForced() {
        return (forced != null) ? forced : false;
    }
    
    public boolean isForceDestoryLocalStorage() {
        return (forceDestroyLocalStorage != null) ? forceDestroyLocalStorage : true;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public void execute() {
        boolean result = _resourceService.deleteHost(getId(), isForced(), isForceDestoryLocalStorage());
        if (result) {
            SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to delete host");
        }
    }
}