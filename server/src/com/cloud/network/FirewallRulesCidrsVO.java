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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name=("firewall_rules_cidrs"))
public class FirewallRulesCidrsVO {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="firewall_rule_id")
    private long firewallRuleId;

    @Column(name="source_cidr")
    private String sourceCidrList;

    public FirewallRulesCidrsVO() { }

    public FirewallRulesCidrsVO(long firewallRuleId, String sourceCidrList) {
        this.firewallRuleId = firewallRuleId;
        this.sourceCidrList = sourceCidrList;
    }

    public Long getId() {
        return id;
    }

    public long getFirewallRuleId() {
        return firewallRuleId;
    }

    public String getCidr() {
        return sourceCidrList;
    }

    public String getSourceCidrList() {
        return sourceCidrList;
    }
    
}
