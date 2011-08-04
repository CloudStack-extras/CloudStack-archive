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
import java.util.Map;
import java.util.Set;

import com.cloud.agent.api.to.NicTO;
import com.cloud.vm.VirtualMachine.State;

public interface VmMgr {
	public Set<String> getCurrentVMs();
	
    public String stopVM(String vmName, boolean force);
	public String rebootVM(String vmName);
    public void cleanupVM(String vmName, String local);
    
    public boolean migrate(String vmName, String params);
    
    public MockVm getVm(String vmName);
    
    public State checkVmState(String vmName);
    public Map<String, State> getVmStates();
    public Integer getVncPort(String name);
    
	public String cleanupVnet(String vnetId);

	Collection<MockVm> getMockVMs();

	String startVM(String vmName, NicTO[] nics, int cpuCount,
			int cpuUtilization, long ramSize, String localPath,
			String vncPassword, long hostFreeMemory);

	int getHostMemSizeInMB();

	int getHostCpuCores();

	int getHostCpuSpeedInMHz();
}
