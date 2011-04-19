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

import java.util.ArrayList;
import java.util.List;

public class VmDataCommand extends NetworkElementCommand {
    
	String vmIpAddress;
	String vmName;
	List<String[]> vmData;
	
    protected VmDataCommand() {    	
    }
    
    @Override
    public boolean executeInSequence() {
        return true;
    }
    
    public VmDataCommand(String vmIpAddress) {
    	this(vmIpAddress, null);
    }
    
    public String getVmName() {
        return vmName;
    }

    public VmDataCommand(String vmIpAddress, String vmName) {
        this.vmName = vmName;
        this.vmIpAddress = vmIpAddress;
        this.vmData = new ArrayList<String[]>();
    }
    
	
	public String getVmIpAddress() {
		return vmIpAddress;
	}
	
	public List<String[]> getVmData() {
		return vmData;
	}
	
	public void addVmData(String folder, String file, String contents) {
		vmData.add(new String[]{folder, file, contents});
	}
	
}
