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
package com.cloud.agent.api.to;

import java.util.List;

import com.cloud.network.lb.LoadBalancingRule.LbDestination;
import com.cloud.network.lb.LoadBalancingRule.StickyPolicy;


public class LoadBalancerTO {
    String srcIp;
    int srcPort;
    String protocol;
    String algorithm;    
    boolean revoked;
    boolean alreadyAdded;
    DestinationTO[] destinations;
    StickyPolicyTO[] stickyPolicies;
   
    public LoadBalancerTO (String srcIp, int srcPort, String protocol, String algorithm, boolean revoked, boolean alreadyAdded, List<LbDestination> destinations) {
        this.srcIp = srcIp;
        this.srcPort = srcPort;
        this.protocol = protocol;
        this.algorithm = algorithm;     
        this.revoked = revoked;
        this.alreadyAdded = alreadyAdded;
        this.destinations = new DestinationTO[destinations.size()];
        int i = 0;
        for (LbDestination destination : destinations) {
            this.destinations[i++] = new DestinationTO(destination.getIpAddress(), destination.getDestinationPortStart(), destination.isRevoked(), false);
        }
    }
    
    public LoadBalancerTO (String srcIp, int srcPort, String protocol, String algorithm, boolean revoked, boolean alreadyAdded, List<LbDestination> arg_destinations,List<StickyPolicy> stickyPolicies) {
        //LoadBalancerTO(srcIp,srcPort,protocol,algorithm,revoked,alreadyAdded,destination); FIXME : to remove some of below assigments
        this.srcIp = srcIp;
        this.srcPort = srcPort;
        this.protocol = protocol;
        this.algorithm = algorithm;     
        this.revoked = revoked;
        this.alreadyAdded = alreadyAdded;
        this.destinations = new DestinationTO[arg_destinations.size()];
        int i = 0;
        for (LbDestination destination : arg_destinations) {
            this.destinations[i++] = new DestinationTO(destination.getIpAddress(), destination.getDestinationPortStart(), destination.isRevoked(), false);
        }
        
        if (stickyPolicies != null && stickyPolicies.size()>0)
        {
    	    this.stickyPolicies = new StickyPolicyTO[stickyPolicies.size()];
            i = 0;
            for (StickyPolicy stickypolicy : stickyPolicies) {
        	    if (!stickypolicy.isRevoked())
                    this.stickyPolicies[i++] = new StickyPolicyTO(stickypolicy.getMethodName(), stickypolicy.getParams());
            }
        }

    }
    
    
    protected LoadBalancerTO() {
    }
    
    public String getSrcIp() {
        return srcIp;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getProtocol() {
        return protocol;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public boolean isAlreadyAdded() {
        return alreadyAdded;
    }
    
    public StickyPolicyTO[] getStickyPolacies() {
        return stickyPolicies;
    }
    
    public DestinationTO[] getDestinations() {
        return destinations;
    }
    
    public static class StickyPolicyTO {
    	String methodName;
    	String params;
    	public String getMethodName()
    	{
    		return methodName;
    	}
    	public String getParams()
    	{
    		return params;
    	}
    	public StickyPolicyTO(String methodName,String params )
    	{
    		this.methodName = methodName;
    		this.params = params;
    	}
    }
    
    public static class DestinationTO {
        String destIp;
        int destPort;
        boolean revoked;
        boolean alreadyAdded;
        public DestinationTO(String destIp, int destPort, boolean revoked, boolean alreadyAdded) {
            this.destIp = destIp;
            this.destPort = destPort;
            this.revoked = revoked;
            this.alreadyAdded = alreadyAdded;
        }
        
        protected DestinationTO() {
        }
        
        public String getDestIp() {
            return destIp;
        }
        
        public int getDestPort() {
            return destPort;
        }
        
        public boolean isRevoked() {
            return revoked;
        }

        public boolean isAlreadyAdded() {
            return alreadyAdded;
        }
    }

}
