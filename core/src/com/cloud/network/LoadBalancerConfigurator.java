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
package com.cloud.network;

import java.util.List;

import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.to.PortForwardingRuleTO;


/**
 * @author chiradeep
 *
 */
public interface LoadBalancerConfigurator {
	public final static int ADD = 0;
	public final static int REMOVE = 1;
	public final static int STATS = 2;
	
	public String [] generateConfiguration(List<PortForwardingRuleTO> fwRules);
	
	public String [] generateConfiguration(LoadBalancerConfigCommand lbCmd) throws Exception ;
	public String [][] generateFwRules(LoadBalancerConfigCommand lbCmd);
}
