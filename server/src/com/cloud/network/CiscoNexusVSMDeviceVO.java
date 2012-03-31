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

package com.cloud.network;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * CiscoNexusVSMDeviceVO contains information on external Cisco Nexus 1000v VSM devices added into a deployment.
 * This should be probably made as a more generic class so that we can handle multiple versions of Nexus VSMs
 * in future.
 */

@Entity
@Table(name="external_virtual_switch_management_devices")
public class CiscoNexusVSMDeviceVO {
	
	// We need to know what properties a VSM has. Put them here.
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name="uuid")
    private String uuid;

    @Column(name = "host_id")
    private long hostId;

    @Column(name = "management_vlan")
    private int managementVlan;

    @Column(name = "data_vlan")
    private int dataVlan;
    
    @Column(name = "packet_vlan")
    private int packetVlan;
    
    @Column(name="is_managed")
    private boolean isManagedDevice;

    @Column(name="is_inline")
    private boolean isInlineMode;

    @Column(name = "parent_host_id")
    private long parentHostId;
    
    // We probably should have a field to put the VSM in a maintenance state for upgrades, or to
    // not manage it for a while, etc. See ExternalLoadBalancerDeviceVO.java'sLBDeviceAllocationState.
    

    public CiscoNexusVSMDeviceVO(long hostId, int mgmtVlan, int packetVlan, int dataVlan, long capacity, boolean inline) {
    	
    	// Set all the VSM's properties here.
    	
        this.managementVlan = mgmtVlan;
        this.dataVlan = dataVlan;
        this.packetVlan = packetVlan;
        this.hostId = hostId;
        this.isInlineMode = inline;
        this.isManagedDevice = false;
        this.uuid = UUID.randomUUID().toString();
    }

    public CiscoNexusVSMDeviceVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    // Put all the get/set methods for the VSM properties here.
    
    public long getId() {
        return id;
    }

    public long getHostId() {
        return hostId;
    }

    public long getParentHostId() {
        return parentHostId;
    }

    public void setParentHostId(long parentHostId) {
        this.parentHostId = parentHostId;
    }
 
    public void setPacketVlan(int packetVlan) {
    	this.packetVlan = packetVlan;
    }
    
    public int getPacketVlan() {
    	return this.packetVlan;
    }
    
    public void setManagementVlan(int managementVlan) {
    	this.managementVlan = managementVlan;
    }
    
    public int getManagementVlan() {
    	return this.managementVlan;
    }
    
    public void setDataVlan(int dataVlan) {
    	this.dataVlan = dataVlan;
    }
    
    public int getDataVlan() {
    	return this.dataVlan;
    }
    
    public boolean getIsManagedDevice() {
        return isManagedDevice;
    }

    public void setIsManagedDevice(boolean managed) {
        this.isManagedDevice = managed;
    }

    public boolean getIsInLineMode () {
        return isInlineMode;
    }

    public void  setIsInlineMode(boolean inline) {
        this.isInlineMode = inline;
    }
    
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
