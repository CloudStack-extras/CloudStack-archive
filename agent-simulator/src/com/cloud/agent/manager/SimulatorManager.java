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

package com.cloud.agent.manager;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.cloud.agent.AgentResourceBase;
import com.cloud.utils.Pair;
import com.cloud.utils.component.Manager;

public interface SimulatorManager extends Manager {
	public static final String Name = "simulator manager";
	
	public enum AgentType {
		Computing(0), // not used anymore
		Routing(1), 
		Storage(2);

		int value;

		AgentType(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}
	
	Map<AgentResourceBase, Map<String, String>> createServerResources(
			Map<String, Object> params);

	boolean checkPoolForResource(String name, Map<String, Object> params);
	
	AgentResourceBase getResourceByName(String name);

	String getPodCidrPrefix();
	
	Properties getProperties();

	long getNextAgentId();

	AgentType getAgentType(int iteration);

	String getAgentPath();

	int getPort();

	int getWorkers();

	String getSequence();

	Long getZone();

	int getRunId();

    boolean saveResourceState(String path,
			AgentResourceBase agentResourceBase);

	int randomizeWaitDelay(int start,int end);
	
	void loadProperties();
	
	int getDelayStart();

	int getDelayEnd();

	List<Pair<Long, Long>> getDelayDistribution();


}