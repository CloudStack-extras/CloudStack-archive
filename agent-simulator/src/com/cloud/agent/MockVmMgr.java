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

package com.cloud.agent;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.cloud.agent.api.to.NicTO;
import com.cloud.network.Networks.TrafficType;
import com.cloud.vm.VirtualMachine.State;

public class MockVmMgr implements VmMgr {
    private static final Logger s_logger = Logger.getLogger(MockVmMgr.class);
    public static final int DEFAULT_HOST_MEM_SIZE_MB = 8000; // 8G, unit of
	// Mbytes
	public static final int DEFAULT_HOST_CPU_CORES = 4; // 2 dual core CPUs (2 x
	// 2)
	public static final int DEFAULT_HOST_SPEED_MHZ = 8000; // 1 GHz CPUs
  
	private Map<String, MockVm> vms = new HashMap<String, MockVm>();
	private long vncPortMap = 0;
	
	public MockVmMgr() {
	}

	@Override
	public Set<String> getCurrentVMs() {
		HashSet<String> vmNameSet = new HashSet<String>();
		synchronized(this) {
			for(String vmName : vms.keySet())
				vmNameSet.add(vmName);
		}
		return vmNameSet;
	}
	
	@Override
	public Collection<MockVm> getMockVMs() {
		return vms.values();
	}
	
	@Override
    public String startVM(String vmName, NicTO[] nics,
        	int cpuCount, int cpuUtilization, long ramSize,
        	String localPath, String vncPassword, long hostFreeMemory) {
		 
		if(s_logger.isInfoEnabled()) {
			StringBuffer sb = new StringBuffer();
			sb.append("Starting VM: " + vmName);
			for(NicTO nic : nics) {								
				sb.append(", gateway " + nic.getGateway()+", DNS1: " +nic.getDns1()+ ", DNS2: " + nic.getDns2());
				if(nic.getType() == TrafficType.Management) {
					sb.append(", privateIP: " + nic.getIp() + ", privateMac: " + nic.getIp() + ", privateNetMask: " + nic.getNetmask());
				} else if(nic.getType() == TrafficType.Guest) {
					sb.append(", guestIP: " + nic.getIp() + ", guestMac: " + nic.getIp() + ", guestNetMask: " + nic.getNetmask());
				} else if (nic.getType() == TrafficType.Public) {
					sb.append(", publicIP: " + nic.getIp() + ", publicMac: " + nic.getIp() + ", publicNetMask: " + nic.getNetmask());				
				}
				sb.append(", cpu count: " + cpuCount + ", cpuUtilization: " + cpuUtilization + ", ram : " + ramSize);
				sb.append(", localPath: " + localPath);
			}
			s_logger.info(sb.toString());
		}
		
		synchronized(this) {
			MockVm vm = vms.get(vmName);
			if(vm == null) {
				if(ramSize > hostFreeMemory)
					return "Out of memory";
					
				int vncPort = allocVncPort();
				if(vncPort < 0)
					return "Unable to allocate VNC port";
				
				vm = new MockVm(vmName, State.Running, ramSize, cpuCount, cpuUtilization,
					vncPort);
				vms.put(vmName, vm);
			} else {
				if(vm.getState() == State.Stopped) {
					vm.setState(State.Running);
				}
			}
		}
		
		return null;
	}
	
	@Override
	public String stopVM(String vmName, boolean force) {
		if(s_logger.isInfoEnabled())
			s_logger.info("Stop VM. name: " + vmName);
			
		synchronized(this) {
			MockVm vm = vms.get(vmName);
			if(vm != null) {
				vm.setState(State.Stopped);
				freeVncPort(vm.getVncPort());
			}
		}
		
		return null;
	}
	
	@Override
	public String rebootVM(String vmName) {
		if(s_logger.isInfoEnabled())
			s_logger.info("Reboot VM. name: " + vmName);
		
		synchronized(this) {
			MockVm vm = vms.get(vmName);
			if(vm != null)
				vm.setState(State.Running);
		}
		return null;
	}
	
	@Override
    public boolean migrate(String vmName, String params) {
		if(s_logger.isInfoEnabled())
			s_logger.info("Migrate VM. name: " + vmName);
		
		synchronized(this) {
			MockVm vm = vms.get(vmName);
			if(vm != null) {
				vm.setState(State.Stopped);
				freeVncPort(vm.getVncPort());
				
				vms.remove(vmName);
				
				return true;
			}
		}		
		return false;
	}
	
    public MockVm getVm(String vmName) {
		synchronized(this) {
			MockVm vm = vms.get(vmName);
			return vm;
		}
    }
	
	@Override
    public State checkVmState(String vmName) {
		
		synchronized(this) {
			MockVm vm = vms.get(vmName);
			if(vm != null)
				return vm.getState();
		}
		return State.Unknown;
	}
	
	@Override
    public Map<String, State> getVmStates() {
		Map<String, State> states = new HashMap<String, State>();
	
		synchronized(this) {
			for(MockVm vm : vms.values()) {
				states.put(vm.getName(), vm.getState());
			}
		}
		
		return states;
    }
    
	@Override
    public void cleanupVM(String vmName, String local) {
		synchronized(this) {
			MockVm vm = vms.get(vmName);
			if(vm != null) {
				freeVncPort(vm.getVncPort());
			}
			vms.remove(vmName);
		}
    }	

	@Override
	public String cleanupVnet(String vnetId) {
		return null;
	}
	
	@Override
    public Integer getVncPort(String name) {
		synchronized(this) {
			MockVm vm = vms.get(name);
			if(vm != null)
				return vm.getVncPort();
		}
		
    	return new Integer(-1);
    }
	
	public int allocVncPort() {
		for(int i = 0; i < 64; i++) {
			if( ((1L << i) & vncPortMap) == 0 ) {
				vncPortMap |= (1L << i);
				return i;
			}
		}
		return -1;
	}
	
	public void freeVncPort(int port) {
		vncPortMap &= ~(1L << port);
	}	
	
	@Override
	public int getHostMemSizeInMB() {
		return DEFAULT_HOST_MEM_SIZE_MB;
	}

	@Override
	public int getHostCpuCores() {
		return DEFAULT_HOST_CPU_CORES;
	}

	@Override
	public int getHostCpuSpeedInMHz() {
		return DEFAULT_HOST_SPEED_MHZ;
	}
}
