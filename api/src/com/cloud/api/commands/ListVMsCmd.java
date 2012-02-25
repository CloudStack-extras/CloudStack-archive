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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.ApiConstants.VMDetails;
import com.cloud.api.BaseListProjectAndAccountResourcesCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.async.AsyncJob;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.uservm.UserVm;

@Implementation(description="List the virtual machines owned by the account.", responseObject=UserVmResponse.class)
public class ListVMsCmd extends BaseListProjectAndAccountResourcesCmd {
    public static final Logger s_logger = Logger.getLogger(ListVMsCmd.class.getName());

    private static final String s_name = "listvirtualmachinesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @IdentityMapper(entityTableName="instance_group")
    @Parameter(name=ApiConstants.GROUP_ID, type=CommandType.LONG, description="the group ID")
    private Long groupId;

    @IdentityMapper(entityTableName="host")
    @Parameter(name=ApiConstants.HOST_ID, type=CommandType.LONG, description="the host ID")
    private Long hostId;

    @IdentityMapper(entityTableName="vm_instance")
    @Parameter(name=ApiConstants.ID, type=CommandType.LONG, description="the ID of the virtual machine")
    private Long id;

    @Parameter(name=ApiConstants.NAME, type=CommandType.STRING, description="name of the virtual machine")
    private String instanceName;

    @IdentityMapper(entityTableName="host_pod_ref")
    @Parameter(name=ApiConstants.POD_ID, type=CommandType.LONG, description="the pod ID")
    private Long podId;

    @Parameter(name=ApiConstants.STATE, type=CommandType.STRING, description="state of the virtual machine")
    private String state;

    @IdentityMapper(entityTableName="data_center")
    @Parameter(name=ApiConstants.ZONE_ID, type=CommandType.LONG, description="the availability zone ID")
    private Long zoneId;
    
    @Parameter(name=ApiConstants.FOR_VIRTUAL_NETWORK, type=CommandType.BOOLEAN, description="list by network type; true if need to list vms using Virtual Network, false otherwise")
    private Boolean forVirtualNetwork;
    
    @IdentityMapper(entityTableName="networks")
    @Parameter(name=ApiConstants.NETWORK_ID, type=CommandType.LONG, description="list by network id")
    private Long networkId;

    @Parameter(name=ApiConstants.HYPERVISOR, type=CommandType.STRING, description="the target hypervisor for the template")
    private String hypervisor;
    
    @IdentityMapper(entityTableName="storage_pool")
    @Parameter(name=ApiConstants.STORAGE_ID, type=CommandType.LONG, description="the storage ID where vm's volumes belong to")
    private Long storageId;

    @Parameter(name=ApiConstants.DETAILS, type=CommandType.LIST, collectionType=CommandType.STRING, description="comma separated list of host details requested, " +
    		"value can be a list of [all, group, nics, stats, secgrp, tmpl, servoff, iso, volume, min]. If no parameter is passed in, the details will be defaulted to all" )
    private List<String> viewDetails; 
   

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getGroupId() {
        return groupId;
    }

    public Long getHostId() {
        return hostId;
    }

    public Long getId() {
        return id;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public Long getPodId() {
        return podId;
    }

    public String getState() {
        return state;
    }

    public Long getZoneId() {
        return zoneId;
    }
    
    public Boolean getForVirtualNetwork() {
        return forVirtualNetwork;
    }

    public void setForVirtualNetwork(Boolean forVirtualNetwork) {
        this.forVirtualNetwork = forVirtualNetwork;
    }
    
    public Long getNetworkId() {
        return networkId;
    }
        
    public String getHypervisor() {
		return hypervisor;
	}
    
    public Long getStorageId() {
        return storageId;
    }
    
    public EnumSet<VMDetails> getDetails() throws InvalidParameterValueException {
        EnumSet<VMDetails> dv;
        if (viewDetails==null || viewDetails.size() <=0){
            dv = EnumSet.of(VMDetails.all);
        }
        else {
            try {
                ArrayList<VMDetails> dc = new ArrayList<VMDetails>();
                for (String detail: viewDetails){
                    dc.add(VMDetails.valueOf(detail));
                }
                dv = EnumSet.copyOf(dc);
            }
            catch (IllegalArgumentException e){
                throw new InvalidParameterValueException("The details parameter contains a non permitted value. The allowed values are " + EnumSet.allOf(VMDetails.class));
            }
        }
        return dv;
    }
    
    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
	public String getCommandName() {
        return s_name;
    }
    
    public AsyncJob.Type getInstanceType() {
    	return AsyncJob.Type.VirtualMachine;
    }

	@Override
    public void execute(){
        List<? extends UserVm> result = _userVmService.searchForUserVMs(this);
        ListResponse<UserVmResponse> response = new ListResponse<UserVmResponse>();
        EnumSet<VMDetails> details = getDetails();
        List<UserVmResponse> vmResponses;
        if (details.contains(VMDetails.all)){ // for all use optimized version
        	 vmResponses = _responseGenerator.createUserVmResponse("virtualmachine", result.toArray(new UserVm[result.size()]));
        }
        else {
        	 vmResponses = _responseGenerator.createUserVmResponse("virtualmachine", getDetails(), result.toArray(new UserVm[result.size()]));
        }
        response.setResponses(vmResponses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }
    
}
