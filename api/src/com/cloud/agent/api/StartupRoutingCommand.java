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

import java.util.HashMap;
import java.util.Map;

import com.cloud.host.Host;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Networks.RouterPrivateIpStrategy;
import com.cloud.vm.VirtualMachine.State;

public class StartupRoutingCommand extends StartupCommand {
    public static class VmState {
        State state;
        String host;
        public VmState() {
        }
        public VmState(State state, String host) {
            this.state = state;
            this.host = host;
        }
        public State getState() {
            return state;
        }
        public String getHost() {
            return host;
        }
    }
    int cpus;
    long speed;
    long memory;
    long dom0MinMemory;
    boolean poolSync;
    Map<String, VmState> vms;
    String caps;
    String pool;
    HypervisorType hypervisorType;
    Map<String, String> hostDetails; //stuff like host os, cpu capabilities

    public StartupRoutingCommand() {
        super(Host.Type.Routing);
        hostDetails = new HashMap<String, String>();
        getHostDetails().put(RouterPrivateIpStrategy.class.getCanonicalName(), RouterPrivateIpStrategy.DcGlobal.toString());

    }
    
    public StartupRoutingCommand(int cpus,
                                   long speed,
                                   long memory,
                                   long dom0MinMemory,
                                   String caps,
                                   HypervisorType hypervisorType,
                                   RouterPrivateIpStrategy privIpStrategy,
                                   Map<String, VmState> vms) {
        this(cpus, speed, memory, dom0MinMemory, caps, hypervisorType, vms);
        getHostDetails().put(RouterPrivateIpStrategy.class.getCanonicalName(), privIpStrategy.toString());
    }
    
    public StartupRoutingCommand(int cpus,
            long speed,
            long memory,
            long dom0MinMemory,
            String caps,
            HypervisorType hypervisorType,
            RouterPrivateIpStrategy privIpStrategy) {
this(cpus, speed, memory, dom0MinMemory, caps, hypervisorType, new HashMap<String,String>(), new HashMap<String, VmState>());
getHostDetails().put(RouterPrivateIpStrategy.class.getCanonicalName(), privIpStrategy.toString());
}
    
    public StartupRoutingCommand(int cpus,
    		long speed,
    		long memory,
    		long dom0MinMemory,
    		final String caps,
    		final HypervisorType hypervisorType,   		
    		final Map<String, String> hostDetails,
    		Map<String, VmState> vms) {
    	super(Host.Type.Routing);
    	this.cpus = cpus;
    	this.speed = speed;
    	this.memory = memory;
    	this.dom0MinMemory = dom0MinMemory;
    	this.vms = vms;
    	this.hypervisorType = hypervisorType;
    	this.hostDetails = hostDetails;
    	this.caps = caps;
    	this.poolSync = false;
    }
    
    public StartupRoutingCommand(int cpus2, long speed2, long memory2,
			long dom0MinMemory2, String caps2, HypervisorType hypervisorType2,
			Map<String, VmState> vms2) {
		this(cpus2, speed2, memory2, dom0MinMemory2, caps2, hypervisorType2, new HashMap<String,String>(), vms2);
	}
    
	public void setChanges(Map<String, VmState> vms) {
        this.vms = vms;
    }
	
	public void setStateChanges(Map<String, State> vms) {
	    for( String vm_name : vms.keySet() ) {
	        if( this.vms == null ) {
	            this.vms = new HashMap<String, VmState>();
	        }
	        this.vms.put(vm_name, new VmState(vms.get(vm_name), null));
	    }
	}

    public int getCpus() {
        return cpus;
    }
    
    public String getCapabilities() {
        return caps;
    }

    public long getSpeed() {
        return speed;
    }

    public long getMemory() {
        return memory;
    }

    public long getDom0MinMemory() {
        return dom0MinMemory;
    }

    public Map<String, VmState> getVmStates() {
        return vms;
    }
    
    public void setSpeed(long speed) {
        this.speed = speed;
    }
    
    public void setCpus(int cpus) {
        this.cpus = cpus;
    }
    
    public void setMemory(long memory) {
        this.memory = memory;
    }
    
    public void setDom0MinMemory(long dom0MinMemory) {
        this.dom0MinMemory = dom0MinMemory;
    }
    
    public void setCaps(String caps) {
        this.caps = caps;
    }
    
    public String getPool() {
    	return pool;
    }
    
    public void setPool(String pool) {
    	this.pool = pool;
    }

	public boolean isPoolSync() {
        return poolSync;
    }

    public void setPoolSync(boolean poolSync) {
        this.poolSync = poolSync;
    }

    public HypervisorType getHypervisorType() {
		return hypervisorType;
	}

	public void setHypervisorType(HypervisorType hypervisorType) {
		this.hypervisorType = hypervisorType;
	}

	public Map<String, String> getHostDetails() {
		return hostDetails;
	}

	public void setHostDetails(Map<String, String> hostDetails) {
		this.hostDetails = hostDetails;
	}
}

