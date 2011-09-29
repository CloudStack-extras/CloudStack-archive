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
package com.cloud.network.lb;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


import com.cloud.api.commands.CreateLBStickyPolicyCmd;
import com.cloud.api.commands.CreateLoadBalancerRuleCmd;
import com.cloud.api.commands.ListLBStickyPoliciesCmd;
import com.cloud.api.commands.ListLBStickyMethodsCmd;

import com.cloud.api.commands.ListLoadBalancerRuleInstancesCmd;
import com.cloud.api.commands.ListLoadBalancerRulesCmd;
import com.cloud.api.commands.UpdateLoadBalancerRuleCmd;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.lb.LoadBalancingRule.StickyPolicy;
import com.cloud.network.rules.LBStickyPolicy;
import com.cloud.network.rules.LoadBalancer;


import com.cloud.uservm.UserVm;


public interface LoadBalancingRulesService {
    /**
     * Create a load balancer rule from the given ipAddress/port to the given private port
     * @param openFirewall TODO
     * @param cmd the command specifying the ip address, public port, protocol, private port, and algorithm
     * @return the newly created LoadBalancerVO if successful, null otherwise
     * @throws InsufficientAddressCapacityException 
     */
    LoadBalancer createLoadBalancerRule(CreateLoadBalancerRuleCmd lb, boolean openFirewall) throws NetworkRuleConflictException, InsufficientAddressCapacityException;

    LoadBalancer updateLoadBalancerRule(UpdateLoadBalancerRuleCmd cmd);
    
    boolean deleteLoadBalancerRule(long lbRuleId, boolean apply);
    /**
     * Create a stickiness policy to a load balancer.
     */
    public LBStickyPolicy createLBStickyPolicy(CreateLBStickyPolicyCmd cmd) throws NetworkRuleConflictException;
    boolean deleteLBStickyPolicy(long stickyPolicyId);
    /**
     * Assign a virtual machine, or list of virtual machines, to a load balancer.
     */
    boolean assignToLoadBalancer(long lbRuleId, List<Long> vmIds);

    boolean removeFromLoadBalancer(long lbRuleId, List<Long> vmIds);
    
    boolean applyLoadBalancerConfig(long lbRuleId) throws ResourceUnavailableException;
    /**
     * List instances that have either been applied to a load balancer or are eligible to be assigned to a load balancer.
     * @param cmd
     * @return list of vm instances that have been or can be applied to a load balancer
     */
    List<? extends UserVm> listLoadBalancerInstances(ListLoadBalancerRuleInstancesCmd cmd);

    /**
     * List load balancer rules based on the given criteria
     * @param cmd the command that specifies the criteria to use for listing load balancers.  Load balancers can be listed
     *            by id, name, public ip, and vm instance id
     * @return list of load balancers that match the criteria
     */
    List<? extends LoadBalancer> searchForLoadBalancers(ListLoadBalancerRulesCmd cmd);
    List<? extends LBStickyPolicy> searchForLBStickyPolicies(ListLBStickyPoliciesCmd cmd);
    List< LBStickyRule > getLBStickyMethods(ListLBStickyMethodsCmd cmd);   
    List<StickyPolicy> getStickypolicies(long lbId);
    
    List<LoadBalancingRule> listByNetworkId(long networkId);
    
    LoadBalancer findById(long LoadBalancer);

}
