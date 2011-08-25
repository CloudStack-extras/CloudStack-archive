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
package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Log4jLevel;


public class SecurityIngressRulesCmd extends Command {
    public static class IpPortAndProto {
        String proto;
        int startPort;
        int endPort;
        @LogLevel(Log4jLevel.Trace)
        String [] allowedCidrs;

        public IpPortAndProto() { }

        public IpPortAndProto(String proto, int startPort, int endPort,
                String[] allowedCidrs) {
            super();
            this.proto = proto;
            this.startPort = startPort;
            this.endPort = endPort;
            this.allowedCidrs = allowedCidrs;
        }

        public String[] getAllowedCidrs() {
            return allowedCidrs;
        }

        public void setAllowedCidrs(String[] allowedCidrs) {
            this.allowedCidrs = allowedCidrs;
        }

        public String getProto() {
            return proto;
        }

        public int getStartPort() {
            return startPort;
        }

        public int getEndPort() {
            return endPort;
        }

    }


    String guestIp;
    String vmName;
    String guestMac;
    String signature;
    Long seqNum;
    Long vmId;
    IpPortAndProto [] ruleSet;

    public SecurityIngressRulesCmd() {
        super();
    }


    public SecurityIngressRulesCmd(String guestIp, String guestMac, String vmName, Long vmId, String signature, Long seqNum, IpPortAndProto[] ruleSet) {
        super();
        this.guestIp = guestIp;
        this.vmName = vmName;
        this.ruleSet = ruleSet;
        this.guestMac = guestMac;
        this.signature = signature;
        this.seqNum = seqNum;
        this.vmId  = vmId;
    }


    @Override
    public boolean executeInSequence() {
        return true;
    }


    public IpPortAndProto[] getRuleSet() {
        return ruleSet;
    }


    public void setRuleSet(IpPortAndProto[] ruleSet) {
        this.ruleSet = ruleSet;
    }


    public String getGuestIp() {
        return guestIp;
    }


    public String getVmName() {
        return vmName;
    }

    public String stringifyRules() {
        StringBuilder ruleBuilder = new StringBuilder();
        for (SecurityIngressRulesCmd.IpPortAndProto ipPandP: getRuleSet()) {
            ruleBuilder.append(ipPandP.getProto()).append(":").append(ipPandP.getStartPort()).append(":").append(ipPandP.getEndPort()).append(":");
            for (String cidr: ipPandP.getAllowedCidrs()) {
                ruleBuilder.append(cidr).append(",");
            }
            ruleBuilder.append("NEXT");
            ruleBuilder.append(" ");
        }
        return ruleBuilder.toString();
    }

    public String getSignature() {
        return signature;
    }


    public String getGuestMac() {
        return guestMac;
    }


    public Long getSeqNum() {
        return seqNum;
    }


    public Long getVmId() {
        return vmId;
    }

}
