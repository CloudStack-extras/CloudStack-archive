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
package com.cloud.api.response;

import java.util.Date;

import com.cloud.api.ApiConstants;
import com.cloud.api.IdentityProxy;
import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class IPAddressResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName(ApiConstants.ID) @Param(description="public IP address id")
    private IdentityProxy id = new IdentityProxy("user_ip_address");
    
    @SerializedName(ApiConstants.IP_ADDRESS) @Param(description="public IP address")
    private String ipAddress;

    @SerializedName("allocated") @Param(description="date the public IP address was acquired")
    private Date allocated;

    @SerializedName(ApiConstants.ZONE_ID) @Param(description="the ID of the zone the public IP address belongs to")
    private IdentityProxy zoneId = new IdentityProxy("data_center");

    @SerializedName(ApiConstants.ZONE_NAME) @Param(description="the name of the zone the public IP address belongs to")
    private String zoneName;

    @SerializedName("issourcenat") @Param(description="true if the IP address is a source nat address, false otherwise")
    private Boolean sourceNat;

    @SerializedName(ApiConstants.ACCOUNT) @Param(description="the account the public IP address is associated with")
    private String accountName;
    
    @SerializedName(ApiConstants.PROJECT_ID) @Param(description="the project id of the ipaddress")
    private IdentityProxy projectId = new IdentityProxy("projects");
    
    @SerializedName(ApiConstants.PROJECT) @Param(description="the project name of the address")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID) @Param(description="the domain ID the public IP address is associated with")
    private IdentityProxy domainId = new IdentityProxy("domain");

    @SerializedName(ApiConstants.DOMAIN) @Param(description="the domain the public IP address is associated with")
    private String domainName;

    @SerializedName(ApiConstants.FOR_VIRTUAL_NETWORK) @Param(description="the virtual network for the IP address")
    private Boolean forVirtualNetwork;

    @SerializedName(ApiConstants.VLAN_ID) @Param(description="the ID of the VLAN associated with the IP address")
    private IdentityProxy vlanId = new IdentityProxy("vlan");

    @SerializedName("vlanname") @Param(description="the VLAN associated with the IP address")
    private String vlanName;

    @SerializedName("isstaticnat") @Param(description="true if this ip is for static nat, false otherwise")
    private Boolean staticNat;
    
    @SerializedName(ApiConstants.IS_SYSTEM) @Param(description="true if this ip is system ip (was allocated as a part of deployVm or createLbRule)")
    private Boolean isSystem;
    
    @SerializedName(ApiConstants.VIRTUAL_MACHINE_ID) @Param(description="virutal machine id the ip address is assigned to (not null only for static nat Ip)")
    private IdentityProxy virtualMachineId = new IdentityProxy("vm_instance");
    
    @SerializedName("virtualmachinename") @Param(description="virutal machine name the ip address is assigned to (not null only for static nat Ip)")
    private String virtualMachineName;
    
    @SerializedName("virtualmachinedisplayname") @Param(description="virutal machine display name the ip address is assigned to (not null only for static nat Ip)")
    private String virtualMachineDisplayName;
    
    @SerializedName(ApiConstants.ASSOCIATED_NETWORK_ID) @Param(description="the ID of the Network associated with the IP address")
    private IdentityProxy associatedNetworkId = new IdentityProxy("networks");
    
    @SerializedName(ApiConstants.NETWORK_ID) @Param(description="the ID of the Network where ip belongs to")
    private IdentityProxy networkId = new IdentityProxy("networks");
    
    @SerializedName(ApiConstants.STATE) @Param(description="State of the ip address. Can be: Allocatin, Allocated and Releasing")
    private String state;
    
    @SerializedName(ApiConstants.PHYSICAL_NETWORK_ID) @Param(description="the physical network this belongs to")
    private IdentityProxy physicalNetworkId = new IdentityProxy("physical_network");
    
    @SerializedName(ApiConstants.PURPOSE) @Param(description="purpose of the IP address. In Acton this value is not null for Ips with isSystem=true, and can have either StaticNat or LB value")
    private String purpose;

/*    
    @SerializedName(ApiConstants.JOB_ID) @Param(description="shows the current pending asynchronous job ID. This tag is not returned if no current pending jobs are acting on the volume")
    private IdentityProxy jobId = new IdentityProxy("async_job");
*/    

/*    
    @SerializedName(ApiConstants.JOB_STATUS) @Param(description="shows the current pending asynchronous job status")
    private Integer jobStatus;
*/    

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setAllocated(Date allocated) {
        this.allocated = allocated;
    }
    
    public void setZoneId(Long zoneId) {
        this.zoneId.setValue(zoneId);
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public void setSourceNat(Boolean sourceNat) {
        this.sourceNat = sourceNat;
    }

    @Override
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Override
    public void setDomainId(Long domainId) {
        this.domainId.setValue(domainId);
    }
    
    @Override
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public void setForVirtualNetwork(Boolean forVirtualNetwork) {
        this.forVirtualNetwork = forVirtualNetwork;
    }

    public void setVlanId(Long vlanId) {
        this.vlanId.setValue(vlanId);
    }

    public void setVlanName(String vlanName) {
        this.vlanName = vlanName;
    }

	public void setStaticNat(Boolean staticNat) {
		this.staticNat = staticNat;
	}

    public void setAssociatedNetworkId(Long networkId) {
        this.associatedNetworkId.setValue(networkId);
    }

    public void setNetworkId(Long networkId) {
        this.networkId.setValue(networkId);
    }

    public void setVirtualMachineId(Long virtualMachineId) {
        this.virtualMachineId.setValue(virtualMachineId);
    }

    public void setVirtualMachineName(String virtualMachineName) {
        this.virtualMachineName = virtualMachineName;
    }

    public void setVirtualMachineDisplayName(String virtualMachineDisplayName) {
        this.virtualMachineDisplayName = virtualMachineDisplayName;
    }

    public Long getId() {
        return id.getValue();
    }

    public void setId(Long id) {
        this.id.setValue(id);
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public Long getObjectId() {
        return getId();
    }

    @Override
    public void setProjectId(Long projectId) {
        this.projectId.setValue(projectId);
    }

    @Override
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public void setPhysicalNetworkId(long physicalNetworkId) {
        this.physicalNetworkId.setValue(physicalNetworkId);
    }

	public void setIsSystem(Boolean isSystem) {
		this.isSystem = isSystem;
	}

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
