/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
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
package com.cloud.network.eip;

import com.cloud.utils.component.Manager;
import com.cloud.vm.DomainRouterVO;

public interface ElasticIpServiceManager extends Manager {

	public static final int DEFAULT_EIP_VM_RAMSIZE = 256;			// 256M
	public static final int DEFAULT_EIP_VM_CPUMHZ = 500;				// 500 MHz
	
	public static final String ALERT_SUBJECT = "ElasticIpVm-alert";
		
	public DomainRouterVO startElasticIpVm(long eipVmId);
	public boolean stopElasticIpVm(long eipVmId);
	public boolean rebootElasticIpVm(long eipVmId);
	public boolean destroyElasticIpVm(long eipVmId);
	
}
