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

package com.cloud.hypervisor.vmware.mo;

import org.apache.log4j.Logger;

import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.vmware.vim25.HostPortGroupSpec;
import com.vmware.vim25.HostVirtualSwitchSpec;
import com.vmware.vim25.ManagedObjectReference;

public class HostNetworkSystemMO extends BaseMO {
    private static final Logger s_logger = Logger.getLogger(HostNetworkSystemMO.class);
    
	public HostNetworkSystemMO(VmwareContext context, ManagedObjectReference morNetworkSystem) {
		super(context, morNetworkSystem);
	}
	
	public HostNetworkSystemMO(VmwareContext context, String morType, String morValue) {
		super(context, morType, morValue);
	}
	
	public void addPortGroup(HostPortGroupSpec spec) throws Exception {
		_context.getService().addPortGroup(_mor, spec);
	}
	
	public void updatePortGroup(String portGroupName, HostPortGroupSpec spec) throws Exception {
		_context.getService().updatePortGroup(_mor, portGroupName, spec);
	}
	
	public void removePortGroup(String portGroupName) throws Exception {
		_context.getService().removePortGroup(_mor, portGroupName);
	}
	
	public void addVirtualSwitch(String vSwitchName, HostVirtualSwitchSpec spec) throws Exception {
		_context.getService().addVirtualSwitch(_mor, vSwitchName, spec);
	}
	
	public void updateVirtualSwitch(String vSwitchName, HostVirtualSwitchSpec spec) throws Exception {
		_context.getService().updateVirtualSwitch(_mor, vSwitchName, spec);
	}
	
	public void removeVirtualSwitch(String vSwitchName) throws Exception {
		_context.getService().removeVirtualSwitch(_mor, vSwitchName);
	}
	
	public void refresh() throws Exception {
		_context.getService().refreshNetworkSystem(_mor);
	}
}

