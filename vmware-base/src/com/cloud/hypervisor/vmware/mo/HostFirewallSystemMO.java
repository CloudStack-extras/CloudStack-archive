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
import com.vmware.vim25.ManagedObjectReference;

public class HostFirewallSystemMO extends BaseMO {
    private static final Logger s_logger = Logger.getLogger(HostFirewallSystemMO.class);

	public HostFirewallSystemMO(VmwareContext context, ManagedObjectReference morFirewallSystem) {
		super(context, morFirewallSystem);
	}
	
	public HostFirewallSystemMO(VmwareContext context, String morType, String morValue) {
		super(context, morType, morValue);
	}
	
	public void enableRuleset(String rulesetName) throws Exception {
		_context.getService().enableRuleset(_mor, rulesetName);
	}
	
	public void disableRuleset(String rulesetName) throws Exception {
		_context.getService().disableRuleset(_mor, rulesetName);
	}
	
	public void refreshFirewall() throws Exception {
		_context.getService().refreshFirewall(_mor);
	}
}
