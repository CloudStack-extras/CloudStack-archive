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
 * @author-aj
 */
package com.cloud.api.commands.netapp;

import java.rmi.ServerException;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceInUseException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.netapp.NetappManager;
import com.cloud.server.ManagementService;
import com.cloud.server.api.response.netapp.DeleteLUNCmdResponse;
import com.cloud.utils.component.ComponentLocator;

@Implementation(description="Destroy a LUN", responseObject = DeleteLUNCmdResponse.class)
public class DestroyLunCmd extends BaseCmd {
    
	public static final Logger s_logger = Logger.getLogger(DestroyLunCmd.class.getName());
    private static final String s_name = "destroylunresponse";

    @Parameter(name=ApiConstants.PATH, type=CommandType.STRING, required = true, description="LUN path.")
	private String path;

    @Override
    public void execute() throws ResourceUnavailableException,
    InsufficientCapacityException, ServerApiException,
    ConcurrentOperationException, ResourceAllocationException {
    	ComponentLocator locator = ComponentLocator.getLocator(ManagementService.Name);
    	NetappManager netappMgr = locator.getManager(NetappManager.class);
    	try {
			netappMgr.destroyLunOnFiler(path);
    		DeleteLUNCmdResponse response = new DeleteLUNCmdResponse();
    		response.setResponseName(getCommandName());
    		this.setResponseObject(response);
    	} catch (InvalidParameterValueException e) {
    		throw new ServerApiException(BaseCmd.PARAM_ERROR, e.toString());
    	} catch (ServerException e) {
    		throw new ServerApiException(BaseCmd.RESOURCE_IN_USE_ERROR, e.toString());
    	}
    }

    @Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return s_name;
	}

	@Override
	public long getEntityOwnerId() {
		// TODO Auto-generated method stub
		return 0;
	}
    
}
