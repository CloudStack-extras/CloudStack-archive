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
import com.cloud.api.response.UserVmResponse;
import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.uservm.UserVm;

@Implementation(description="Detaches any ISO file (if any) currently attached to a virtual machine.", responseObject=UserVmResponse.class)
public class DetachIsoCmd extends BaseAsyncCmd {
    public static final Logger s_logger = Logger.getLogger(DetachIsoCmd.class.getName());

    private static final String s_name = "detachisoresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.VIRTUAL_MACHINE_ID, type=CommandType.LONG, required=true, description="The ID of the virtual machine")
    private Long virtualMachineId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

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
        UserVm vm = _entityMgr.findById(UserVm.class, getVirtualMachineId());
        if (vm != null) {
            return vm.getAccountId();
        } else {
            throw new InvalidParameterValueException("Unable to find vm by id " + getVirtualMachineId());
        }  
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ISO_DETACH;
    }

    @Override
    public String getEventDescription() {
        return  "detaching ISO from vm: " + getVirtualMachineId();
    }
	
    @Override
    public void execute(){
        boolean result = _templateService.detachIso(this);
        if (result) {
            UserVm userVm = _entityMgr.findById(UserVm.class, virtualMachineId);         
            UserVmResponse response = _responseGenerator.createUserVmResponse("virtualmachine", userVm).get(0);            
            response.setResponseName(DeployVMCmd.getResultObjectName());           
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to detach iso");
        }
    }
}
