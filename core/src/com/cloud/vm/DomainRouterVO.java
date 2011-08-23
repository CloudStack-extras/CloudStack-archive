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
package com.cloud.vm;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.router.VirtualRouter;

/**
 * VirtualMachineRouterVO implements all the fields stored for a domain router.
 */
@Entity
@Table(name="domain_router")
@PrimaryKeyJoinColumn(name="id")
@DiscriminatorValue(value="DomainRouter")
public class DomainRouterVO extends VMInstanceVO implements VirtualRouter {
    @Column(name="public_ip_address")
    private String publicIpAddress;
    
    @Column(name="public_mac_address")
    private String publicMacAddress;
    
    @Column(name="public_netmask")
    private String publicNetmask;
    
    @Column(name="guest_ip_address")
    private String guestIpAddress;
    
    @Column(name="network_id")
    long networkId;

    @Column(name="is_redundant_router")
    boolean isRedundantRouter;
    
    @Column(name="priority")
    int priority;
    
    @Column(name="redundant_state")
    @Enumerated(EnumType.STRING)
    private RedundantState redundantState;
    
    @Column(name="role")
    @Enumerated(EnumType.STRING)
    private Role role = Role.DHCP_FIREWALL_LB_PASSWD_USERDATA;
    
    public DomainRouterVO(long id,
            long serviceOfferingId,
            String name,
            long templateId,
            HypervisorType hypervisorType,
            long guestOSId,
            long domainId,
            long accountId,
            long networkId,
            boolean isRedundantRouter,
            int priority,
            RedundantState redundantState,
            boolean haEnabled) {
        super(id, serviceOfferingId, name, name, Type.DomainRouter, templateId, hypervisorType, guestOSId, domainId, accountId, haEnabled);
        this.networkId = networkId;
        this.isRedundantRouter = isRedundantRouter;
        this.priority = priority;
        this.redundantState = redundantState;
    }
    
    public DomainRouterVO(long id,
            long serviceOfferingId,
            String name,
            long templateId,
            HypervisorType hypervisorType,
            long guestOSId,
            long domainId,
            long accountId,
            long networkId,
            boolean isRedundantRouter,
            int priority,
            RedundantState redundantState,
            boolean haEnabled,
            VirtualMachine.Type vmType) {
        super(id, serviceOfferingId, name, name, vmType, templateId, hypervisorType, guestOSId, domainId, accountId, haEnabled);
        this.networkId = networkId;
        this.isRedundantRouter = isRedundantRouter;
        this.priority = priority;
        this.redundantState = redundantState;
    }

    public void setPublicIpAddress(String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public void setPublicMacAddress(String publicMacAddress) {
        this.publicMacAddress = publicMacAddress;
    }

    public void setPublicNetmask(String publicNetmask) {
        this.publicNetmask = publicNetmask;
    }

    public long getNetworkId() {
        return networkId;
    }
    
    public void setGuestIpAddress(String routerIpAddress) {
        this.guestIpAddress = routerIpAddress;
    }

    @Override
    public long getDataCenterIdToDeployIn() {
        return dataCenterIdToDeployIn;
    }
    
    public String getPublicNetmask() {
        return publicNetmask;
    }
    
    public String getPublicMacAddress() {
        return publicMacAddress;
    }
    
    @Override
    public String getGuestIpAddress() {
        return guestIpAddress;
    }
    
    protected DomainRouterVO() {
        super();
    }
    
    public String getPublicIpAddress() {
        return publicIpAddress;
    }
    
	@Override
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	@Override
	public boolean getIsRedundantRouter() {
	    return this.isRedundantRouter;
 	}

	public void setIsRedundantRouter(boolean isRedundantRouter) {
	    this.isRedundantRouter = isRedundantRouter;
	}

	@Override
	public long getServiceOfferingId() {
	    return serviceOfferingId;
	}

	public int getPriority() {
	    return this.priority;
	}
	
	public void setPriority(int priority) {
	    this.priority = priority;
	}

	@Override
	public RedundantState getRedundantState() {
	    return this.redundantState;
	}
	
	public void setRedundantState(RedundantState redundantState) {
	    this.redundantState = redundantState;
	}
	
    public void setServiceOfferingId(long serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }
}
