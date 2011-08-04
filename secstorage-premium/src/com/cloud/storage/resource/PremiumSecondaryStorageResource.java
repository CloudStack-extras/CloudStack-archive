/**
 * *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
*
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

package com.cloud.storage.resource;

import java.util.HashMap;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.hypervisor.Hypervisor;

public class PremiumSecondaryStorageResource extends NfsSecondaryStorageResource {

    private static final Logger s_logger = Logger.getLogger(PremiumSecondaryStorageResource.class);

    private Map<Hypervisor.HypervisorType, SecondaryStorageResourceHandler> _handlers = new HashMap<Hypervisor.HypervisorType, SecondaryStorageResourceHandler>();
    
    private Map<String, String> _activeOutgoingAddresses = new HashMap<String, String>();
	
    @Override
    public Answer executeRequest(Command cmd) {
    	String hypervisor = cmd.getContextParam("hypervisor");
    	if(hypervisor != null) {
    		Hypervisor.HypervisorType hypervisorType = Hypervisor.HypervisorType.getType(hypervisor);
    		if(hypervisorType == null) {
    			s_logger.error("Unsupported hypervisor type in command context, hypervisor: " + hypervisor);
    			return defaultAction(cmd);
    		}
    		
    		SecondaryStorageResourceHandler handler = getHandler(hypervisorType);
    		if(handler == null) {
    			s_logger.error("No handler can be found for hypervisor type in command context, hypervisor: " + hypervisor);
    			return defaultAction(cmd);
    		}
    		
    		return handler.executeRequest(cmd);
    	}

        return defaultAction(cmd);
    }
    
    public Answer defaultAction(Command cmd) {
    	return super.executeRequest(cmd);
    }
    
    public void ensureOutgoingRuleForAddress(String address) {
    	if(address == null || address.isEmpty() || address.startsWith("0.0.0.0")) {
    		if(s_logger.isInfoEnabled())
    			s_logger.info("Drop invalid dynamic route/firewall entry " + address);
    		return;
    	}
    	
    	boolean needToSetRule = false;
    	synchronized(_activeOutgoingAddresses) {
    		if(!_activeOutgoingAddresses.containsKey(address)) {
    			_activeOutgoingAddresses.put(address, address);
    			needToSetRule = true;
    		}
    	}
    	
    	if(needToSetRule) {
    		if(s_logger.isInfoEnabled())
    			s_logger.info("Add dynamic route/firewall entry for " + address);
    		allowOutgoingOnPrivate(address);
    	}
    }
    
    private void registerHandler(Hypervisor.HypervisorType hypervisorType, SecondaryStorageResourceHandler handler) {
    	_handlers.put(hypervisorType, handler);
    }
    
    private SecondaryStorageResourceHandler getHandler(Hypervisor.HypervisorType hypervisorType) {
    	return _handlers.get(hypervisorType);
    }
    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
    	super.configure(name, params);

    	if(_inSystemVM) {
    		VmwareSecondaryStorageContextFactory.initFactoryEnvironment();
    	}
    	
    	registerHandler(Hypervisor.HypervisorType.VMware, new VmwareSecondaryStorageResourceHandler(this));
    	return true;
    }
}
