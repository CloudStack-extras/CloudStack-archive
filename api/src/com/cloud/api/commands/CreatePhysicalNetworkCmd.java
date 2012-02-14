/**
 *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved.
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

import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.PhysicalNetworkResponse;
import com.cloud.async.AsyncJob;
import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.network.PhysicalNetwork;
import com.cloud.user.Account;
import com.cloud.user.UserContext;

@Implementation(description="Creates a physical network", responseObject=PhysicalNetworkResponse.class, since="3.0.0")
public class CreatePhysicalNetworkCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = Logger.getLogger(CreatePhysicalNetworkCmd.class.getName());

    private static final String s_name = "createphysicalnetworkresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    
    @IdentityMapper(entityTableName="data_center")
    @Parameter(name=ApiConstants.ZONE_ID, type=CommandType.LONG, required=true, description="the Zone ID for the physical network")
    private Long zoneId;

    @Parameter(name=ApiConstants.VLAN, type=CommandType.STRING, description="the VLAN for the physical network")
    private String vlan;

    @Parameter(name=ApiConstants.NETWORK_SPEED, type=CommandType.STRING, description="the speed for the physical network[1G/10G]")
    private String speed;

    @IdentityMapper(entityTableName="domain")
    @Parameter(name=ApiConstants.DOMAIN_ID, type=CommandType.LONG, description="domain ID of the account owning a physical network")
    private Long domainId;
    
    @Parameter(name=ApiConstants.BROADCAST_DOMAIN_RANGE, type=CommandType.STRING, description="the broadcast domain range for the physical network[Pod or Zone]. In Acton release it can be Zone only in Advance zone, and Pod in Basic")
    private String broadcastDomainRange;
    
    @Parameter(name=ApiConstants.TAGS, type=CommandType.LIST, collectionType=CommandType.STRING, description="Tag the physical network")
    private List<String> tags;
    
    @Parameter(name=ApiConstants.ISOLATION_METHODS, type=CommandType.LIST, collectionType=CommandType.STRING, description="the isolation method for the physical network[VLAN/L3/GRE]")
    private List<String> isolationMethods;
    
    @Parameter(name=ApiConstants.NAME, type=CommandType.STRING, required=true, description="the name of the physical network")
    private String networkName;
    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    
    public List<String> getTags() {
        return tags;
    }

    @Override
    public String getEntityTable() {
        return "physical_network";
    }

    public Long getZoneId() {
        return zoneId;
    }

    public String getVlan() {
        return vlan;
    }

    public Long getDomainId() {
        return domainId;
    }

    public String getBroadcastDomainRange() {
        return broadcastDomainRange;
    }

    public List<String> getIsolationMethods() {
        return isolationMethods;
    }
    
    public String getNetworkSpeed() {
        return speed;
    }
    
    public String getNetworkName() {
		return networkName;
	}
    
	@Override
    public String getCommandName() {
        return s_name;
    }
    
    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
    
    @Override
    public String getEventType() {
        return EventTypes.EVENT_PHYSICAL_NETWORK_CREATE;
    }

    @Override
    public String getCreateEventType() {
        return EventTypes.EVENT_PHYSICAL_NETWORK_CREATE;
    }

    @Override
    public String getCreateEventDescription() {
        return "creating Physical Network";
    }

    @Override
    public String getEventDescription() {
        return  "creating Physical Network. Id: "+getEntityId();
    }
    
    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute(){
        UserContext.current().setEventDetails("Physical Network Id: "+getEntityId());
        PhysicalNetwork result = _networkService.getCreatedPhysicalNetwork(getEntityId());
        if (result != null) {
            PhysicalNetworkResponse response = _responseGenerator.createPhysicalNetworkResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        }else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to create physical network");
        }
    }

    @Override
    public void create() throws ResourceAllocationException {
        PhysicalNetwork result = _networkService.createPhysicalNetwork(getZoneId(),getVlan(),getNetworkSpeed(), getIsolationMethods(),getBroadcastDomainRange(),getDomainId(), getTags(), getNetworkName());
        if (result != null) {
            setEntityId(result.getId());
        } else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to create physical network entity");
        }
    }
    
    @Override
    public AsyncJob.Type getInstanceType() {
        return AsyncJob.Type.PhysicalNetwork;
    }
}
