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

package com.cloud.network;

import java.util.List;

import com.cloud.api.commands.AddExternalFirewallCmd;
import com.cloud.api.commands.AddExternalLoadBalancerCmd;
import com.cloud.api.commands.DeleteExternalFirewallCmd;
import com.cloud.api.commands.DeleteExternalLoadBalancerCmd;
import com.cloud.api.commands.ListExternalFirewallsCmd;
import com.cloud.api.commands.ListExternalLoadBalancersCmd;
import com.cloud.dc.DataCenter;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.network.rules.FirewallRule;
import com.cloud.offering.NetworkOffering;
import com.cloud.server.api.response.ExternalFirewallResponse;
import com.cloud.server.api.response.ExternalLoadBalancerResponse;
import com.cloud.utils.component.Manager;

public interface ExternalNetworkManager extends Manager {
	
	public static class ExternalNetworkDeviceType {
		private String _name;
		
		public static final ExternalNetworkDeviceType F5BigIP = new ExternalNetworkDeviceType("F5BigIP");
		public static final ExternalNetworkDeviceType JuniperSRX = new ExternalNetworkDeviceType("JuniperSRX");
		
		public ExternalNetworkDeviceType(String name) {
			_name = name;
		}
		
		public String getName() {
			return _name;
		}
	}
	
	// External Firewall methods

	public Host addExternalFirewall(AddExternalFirewallCmd cmd);
	
	public boolean deleteExternalFirewall(DeleteExternalFirewallCmd cmd);
	
	public List<HostVO> listExternalFirewalls(ListExternalFirewallsCmd cmd);
	
	public ExternalFirewallResponse createExternalFirewallResponse(Host externalFirewall);
		
	public boolean manageGuestNetworkWithExternalFirewall(boolean add, Network network, NetworkOffering offering) throws ResourceUnavailableException;
	
	public boolean applyFirewallRules(Network network, List<? extends FirewallRule> rules) throws ResourceUnavailableException;

	public boolean applyIps(Network network, List<? extends PublicIpAddress> ipAddresses) throws ResourceUnavailableException;

	public boolean manageRemoteAccessVpn(boolean create, Network network, RemoteAccessVpn vpn) throws ResourceUnavailableException;
	
	public boolean manageRemoteAccessVpnUsers(Network network, RemoteAccessVpn vpn, List<? extends VpnUser> users) throws ResourceUnavailableException;

	// External Load balancer methods	
	
	public Host addExternalLoadBalancer(AddExternalLoadBalancerCmd cmd);

	public boolean deleteExternalLoadBalancer(DeleteExternalLoadBalancerCmd cmd);
	
	public List<HostVO> listExternalLoadBalancers(ListExternalLoadBalancersCmd cmd);
	
	public ExternalLoadBalancerResponse createExternalLoadBalancerResponse(Host externalLoadBalancer);
	
	public boolean manageGuestNetworkWithExternalLoadBalancer(boolean add, Network guestConfig) throws ResourceUnavailableException;
	
	public boolean applyLoadBalancerRules(Network network, List<? extends FirewallRule> rules) throws ResourceUnavailableException;
	
	// General methods
	
	public int getVlanOffset(DataCenter zone, int vlanTag);
	
	public int getGloballyConfiguredCidrSize();
}
