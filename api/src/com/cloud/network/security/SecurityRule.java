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
package com.cloud.network.security;

import com.cloud.async.AsyncInstanceCreateStatus;

/**
 * @author ahuang
 * 
 */
public interface SecurityRule {

    public static class SecurityRuleType {
        public static final SecurityRuleType IngressRule = new SecurityRuleType("ingress", "I");
        public static final SecurityRuleType EgressRule = new SecurityRuleType("egress", "E");
        
        public SecurityRuleType(String rule, String dbType) {
            this._strType = rule;
            this._dbType = dbType;
        }
        
        public String getStrType(){
            return _strType;
        }
        
        public String getDbType(){
            return _dbType;
        }
        private String _strType;
        private String _dbType;
    }
    long getId();

    long getSecurityGroupId();

    int getStartPort();

    int getEndPort();
    
    String getType();
    
    SecurityRuleType getRuleType();
    
    String getProtocol();

    AsyncInstanceCreateStatus getCreateStatus();

    Long getAllowedNetworkId();

    String getAllowedSourceIpCidr();

}
