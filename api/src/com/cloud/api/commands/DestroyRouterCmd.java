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
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DomainRouterResponse;
import com.cloud.async.AsyncJob;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.router.VirtualRouter;
import com.cloud.user.Account;
import com.cloud.user.UserContext;

@Implementation(description = "Destroys a router.", responseObject = DomainRouterResponse.class)
public class DestroyRouterCmd extends BaseAsyncCmd {
    public static final Logger s_logger = Logger.getLogger(DestroyRouterCmd.class.getName());
    private static final String s_name = "destroyrouterresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.LONG, required = true, description = "the ID of the router")
    private Long id;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
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
        VirtualRouter router = _entityMgr.findById(VirtualRouter.class, getId());
        if (router != null) {
            return router.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ROUTER_DESTROY;
    }

    @Override
    public String getEventDescription() {
        return "destroying router: " + getId();
    }

    @Override
    public AsyncJob.Type getInstanceType() {
        return AsyncJob.Type.DomainRouter;
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public void execute() throws ConcurrentOperationException, ResourceUnavailableException {
        UserContext.current().setEventDetails("Router Id: "+getId());
        VirtualRouter result = _routerService.destroyRouter(getId());
        if (result != null) {
            DomainRouterResponse response = _responseGenerator.createDomainRouterResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to destroy router");
        }
    }
}
