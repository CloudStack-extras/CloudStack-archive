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
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.IPAddressResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.event.EventTypes;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.network.IpAddress;
import com.cloud.user.Account;
import com.cloud.user.UserContext;
import com.cloud.uservm.UserVm;

@Implementation(description="Enables static nat for given ip address", responseObject=SuccessResponse.class)
public class EnableStaticNatCmd extends BaseAsyncCreateCmd{
    public static final Logger s_logger = Logger.getLogger(EnableStaticNatCmd.class.getName());

    private static final String s_name = "enablestaticnatresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.IP_ADDRESS_ID, type=CommandType.LONG, required=true, description="the public IP address id for which static nat feature is being enabled")
    private Long ipAddressId;

    @Parameter(name=ApiConstants.VIRTUAL_MACHINE_ID, type=CommandType.LONG, required=true, description="the ID of the virtual machine for enabling static nat feature")
    private Long virtualMachineId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getIpAddressId() {
        return ipAddressId;
    }

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }
    
    @Override
    public long getEntityOwnerId() {
        UserVm userVm = _entityMgr.findById(UserVm.class, getVirtualMachineId());
        if (userVm != null) {
            return userVm.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    @Override
    public void execute(){ 
        UserContext.current().setEventDetails("Ip Id: "+getEntityId());
        IpAddress result = _networkService.associateElasticIP(this);
        if (result != null) {
            IPAddressResponse ipResponse = _responseGenerator.createIPAddressResponse(result);
            ipResponse.setResponseName(getCommandName());
            this.setResponseObject(ipResponse);
        } else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to associate ip address");
        }
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ENABLE_STATIC_NAT;
    }

    @Override
    public String getEventDescription() {
        return "Enable static nat or elastic ip service";
    }

    @Override
    public void create() throws ResourceAllocationException {
        setEntityId(getIpAddressId());
        try {
            boolean result = _rulesService.enableOneToOneNat(ipAddressId, virtualMachineId);
            if (!result) {
                throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to configure static nat / elasticip");
            }
        } catch (NetworkRuleConflictException ex) {
            s_logger.info("Network rule conflict: " + ex.getMessage());
            s_logger.trace("Network Rule Conflict: ", ex);
            throw new ServerApiException(BaseCmd.NETWORK_RULE_CONFLICT_ERROR, ex.getMessage());
        }
        
    }

}
