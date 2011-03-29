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
package com.cloud.agent.api;

import com.cloud.vm.VirtualMachine;

public class StopCommand extends RebootCommand {
    String vnet;
    private boolean isProxy=false;
    private String urlPort=null;
    private String publicConsoleProxyIpAddress=null;
    private String privateRouterIpAddress=null;
    private String privateMacAddress;
    
    protected StopCommand() {
    }
    
    public StopCommand(VirtualMachine vm, boolean isProxy, String urlPort, String publicConsoleProxyIpAddress) {
    	super(vm);
    	this.isProxy = isProxy;
    	this.urlPort = urlPort;
    	this.publicConsoleProxyIpAddress = publicConsoleProxyIpAddress;
    	this.privateMacAddress = vm.getPrivateMacAddress();
    }
    
    public StopCommand(VirtualMachine vm, String vnet) {
        super(vm);
        this.vnet = vnet;
    }
    
    public StopCommand(VirtualMachine vm, String vmName, String vnet) {
        super(vmName);
        this.vnet = vnet;
    }
    
    public StopCommand(VirtualMachine vm, String vmName, String vnet, String privateRouterIpAddress) {
        super(vmName);
        this.vnet = vnet;
        this.privateRouterIpAddress = privateRouterIpAddress;
    }
    
    public StopCommand(String vmName) {
        super(vmName);
    }
    
    public String getVnet() {
        return vnet;
    }
    
    @Override
    public boolean executeInSequence() {
        return true;
    }

	public boolean isProxy() {
		return this.isProxy;
	}
	
	public String getURLPort() {
		return this.urlPort;
	}
	
	public String getPublicConsoleProxyIpAddress() {
		return this.publicConsoleProxyIpAddress;
	}

    public String getPrivateRouterIpAddress() {
        return privateRouterIpAddress;
    }
    
    public String getPrivateMacAddress() {
        return privateMacAddress;
    }
}
