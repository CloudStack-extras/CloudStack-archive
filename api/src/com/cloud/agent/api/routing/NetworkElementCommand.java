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
package com.cloud.agent.api.routing;

import java.util.HashMap;

import com.cloud.agent.api.Command;

public abstract class NetworkElementCommand extends Command {
    HashMap<String, String> accessDetails = new HashMap<String, String>(0);
    
    public static final String ACCOUNT_ID = "account.id";
    public static final String GUEST_NETWORK_CIDR = "guest.network.cidr";
    public static final String GUEST_NETWORK_GATEWAY = "guest.network.gateway";
    public static final String GUEST_VLAN_TAG = "guest.vlan.tag";
    public static final String ROUTER_NAME = "router.name";
    public static final String ROUTER_IP = "router.ip";
    public static final String ROUTER_GUEST_IP = "router.guest.ip";
    public static final String ZONE_NETWORK_TYPE = "zone.network.type";
       
    protected NetworkElementCommand() {
        super();
    }
    
    public void setAccessDetail(String name, String value) {
        accessDetails.put(name, value);
    }
    
    public String getAccessDetail(String name) {
        return accessDetails.get(name);
    }
    
    @Override
    public boolean executeInSequence() {
        return false;
    }

}
