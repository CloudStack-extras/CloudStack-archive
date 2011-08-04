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

public class VmwareHypervisorHostNetworkSummary {
	private String hostIp;
	private String hostNetmask;
	private String hostMacAddress;

	public VmwareHypervisorHostNetworkSummary() {
	}
	
	public String getHostIp() {
		return hostIp;
	}
	
	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}
	
	public String getHostNetmask() {
		return hostNetmask;
	}
	
	public void setHostNetmask(String hostNetmask) {
		this.hostNetmask = hostNetmask;
	}
	
	public String getHostMacAddress() {
		return hostMacAddress;
	}
	
	public void setHostMacAddress(String hostMacAddress) {
		this.hostMacAddress = hostMacAddress;
	}
}
