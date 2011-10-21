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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cloud.network.lb.LoadBalancingRule.LbDestination;
import com.cloud.network.lb.LoadBalancingRule.LbStickinessPolicy;


public class LoadBalancerTO {
    String srcIp;
    int srcPort;
    String protocol;
    String algorithm;    
    boolean revoked;
    boolean alreadyAdded;
    DestinationTO[] destinations;
    private StickinessPolicyTO[] stickinessPolicies;
   
    public LoadBalancerTO (String srcIp, int srcPort, String protocol, String algorithm, boolean revoked, boolean alreadyAdded, List<LbDestination> destinations) {
        this.srcIp = srcIp;
        this.srcPort = srcPort;
        this.protocol = protocol;
        this.algorithm = algorithm;     
        this.revoked = revoked;
        this.alreadyAdded = alreadyAdded;
        this.destinations = new DestinationTO[destinations.size()];
        this.stickinessPolicies = null;
        int i = 0;
        for (LbDestination destination : destinations) {
            this.destinations[i++] = new DestinationTO(destination.getIpAddress(), destination.getDestinationPortStart(), destination.isRevoked(), false);
        }
    }
    
    public LoadBalancerTO (String srcIp, int srcPort, String protocol, String algorithm, boolean revoked, boolean alreadyAdded, List<LbDestination> arg_destinations,List<LbStickinessPolicy> stickinessPolicies) {
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
        this.stickinessPolicies = null;
        if (stickinessPolicies != null && stickinessPolicies.size()>0)
        {
    	    this.stickinessPolicies = new StickinessPolicyTO[stickinessPolicies.size()];
            i = 0;
            for (LbStickinessPolicy stickinesspolicy : stickinessPolicies) {
        	    if (!stickinesspolicy.isRevoked())
                    this.stickinessPolicies[i++] = new StickinessPolicyTO(stickinesspolicy.getMethodName(), stickinesspolicy.getDbParams());
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
    
    public StickinessPolicyTO[] getStickinessPolicies() {
        return stickinessPolicies;
    }
    
    public DestinationTO[] getDestinations() {
        return destinations;
    }
    
    public static class StickinessPolicyTO {
        private String _methodName;
        private String _paramsDB;
        private Map<String, String> _paramsList;

        public String getMethodName() {
            return _methodName;
        }

        public Map<String, String> getParams() {
            return _paramsList;
        }

        public String getParamsDB() {
            return _paramsDB;
        }

        public StickinessPolicyTO(String methodName, String paramsDB) {
            this._methodName = methodName;
            this._paramsDB = paramsDB;
            String[] temp;
            temp = paramsDB.split("[,]");
            Map<String, String> paramList = new HashMap<String, String>();
            for (int i = 0; i < (temp.length - 1); i = i + 2) {
                paramList.put(temp[i], temp[i + 1]);
            }
            this._paramsList = paramList;
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
