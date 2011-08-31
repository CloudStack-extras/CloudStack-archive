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

package com.cloud.network;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name=("inline_load_balancer_nic_map"))
public class InlineLoadBalancerNicMapVO {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private long id;

    @Column(name="load_balancer_id")
    private long loadBalancerId;
    
    @Column(name="public_ip_address")
    private String publicIpAddress;

    @Column(name="nic_id")
    private long nicId;

    public InlineLoadBalancerNicMapVO() { }

    public InlineLoadBalancerNicMapVO(long loadBalancerId, String publicIpAddress, long nicId) {
        this.loadBalancerId = loadBalancerId;
        this.publicIpAddress = publicIpAddress;
        this.nicId = nicId;
    }

    public long getId() {
        return id;
    }

    public long getLoadBalancerId() {
        return loadBalancerId;
    }
    
    public String getPublicIpAddress() {
    	return publicIpAddress;
    }

    public long getNicId() {
    	return nicId;
    }
}
