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

package com.cloud.api.commands;

import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.FirewallRuleResponse;
import com.cloud.async.AsyncJob;
import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IpAddress;
import com.cloud.network.rules.PortForwardingRule;
import com.cloud.user.Account;
import com.cloud.user.UserContext;
import com.cloud.utils.net.Ip;

@Implementation(description = "Creates a port forwarding rule", responseObject = FirewallRuleResponse.class)
public class CreatePortForwardingRuleCmd extends BaseAsyncCreateCmd implements PortForwardingRule {
    public static final Logger s_logger = Logger.getLogger(CreatePortForwardingRuleCmd.class.getName());

    private static final String s_name = "createportforwardingruleresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @IdentityMapper(entityTableName = "user_ip_address")
    @Parameter(name = ApiConstants.IP_ADDRESS_ID, type = CommandType.LONG, required = true, description = "the IP address id of the port forwarding rule")
    private Long ipAddressId;

    @Parameter(name = ApiConstants.PRIVATE_START_PORT, type = CommandType.INTEGER, required = true, description = "the starting port of port forwarding rule's private port range")
    private Integer privateStartPort;

    @Parameter(name = ApiConstants.PROTOCOL, type = CommandType.STRING, required = true, description = "the protocol for the port fowarding rule. Valid values are TCP or UDP.")
    private String protocol;

    @Parameter(name = ApiConstants.PUBLIC_START_PORT, type = CommandType.INTEGER, required = true, description = "the starting port of port forwarding rule's public port range")
    private Integer publicStartPort;

    @IdentityMapper(entityTableName = "vm_instance")
    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID, type = CommandType.LONG, required = true, description = "the ID of the virtual machine for the port forwarding rule")
    private Long virtualMachineId;

    @Parameter(name = ApiConstants.CIDR_LIST, type = CommandType.LIST, collectionType = CommandType.STRING, description = "the cidr list to forward traffic from")
    private List<String> cidrlist;

    @Parameter(name = ApiConstants.OPEN_FIREWALL, type = CommandType.BOOLEAN, description = "if true, firewall rule for source/end pubic port is automatically created; if false - firewall rule has to be created explicitely. Has value true by default")
    private Boolean openFirewall;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public String getEntityTable() {
        return "firewall_rules";
    }

    public Long getIpAddressId() {
        return ipAddressId;
    }

    @Override
    public String getProtocol() {
        return protocol.trim();
    }

    @Override
    public long getVirtualMachineId() {
        return virtualMachineId;
    }

    public List<String> getSourceCidrList() {
        if (cidrlist != null) {
            throw new InvalidParameterValueException("Parameter cidrList is deprecated; if you need to open firewall rule for the specific cidr, please refer to createFirewallRule command");
        }
        return null;
    }

    public Boolean getOpenFirewall() {
        if (openFirewall != null) {
            return openFirewall;
        } else {
            return true;
        }
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public void execute() throws ResourceUnavailableException {
        UserContext callerContext = UserContext.current();
        boolean success = true;
        PortForwardingRule rule = null;
        try {
            UserContext.current().setEventDetails("Rule Id: " + getEntityId());

            if (getOpenFirewall()) {
                success = success && _firewallService.applyFirewallRules(ipAddressId, callerContext.getCaller());
            }

            success = success && _rulesService.applyPortForwardingRules(ipAddressId, callerContext.getCaller());

            // State is different after the rule is applied, so get new object here
            rule = _entityMgr.findById(PortForwardingRule.class, getEntityId());
            FirewallRuleResponse fwResponse = new FirewallRuleResponse();
            if (rule != null) {
                fwResponse = _responseGenerator.createPortForwardingRuleResponse(rule);
                setResponseObject(fwResponse);
            }
            fwResponse.setResponseName(getCommandName());
        } finally {
            if (!success || rule == null) {

                if (getOpenFirewall()) {
                    _firewallService.revokeRelatedFirewallRule(getEntityId(), true);
                }

                _rulesService.revokePortForwardingRule(getEntityId(), true);

                throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to apply port forwarding rule");
            }
        }
    }

    @Override
    public long getId() {
        throw new UnsupportedOperationException("database id can only provided by VO objects");
    }

    @Override
    public String getXid() {
        // FIXME: We should allow for end user to specify Xid.
        return null;
    }

    @Override
    public long getSourceIpAddressId() {
        return ipAddressId;
    }

    @Override
    public Integer getSourcePortStart() {
        return publicStartPort.intValue();
    }

    @Override
    public Integer getSourcePortEnd() {
        return publicStartPort.intValue();
    }

    @Override
    public Purpose getPurpose() {
        return Purpose.PortForwarding;
    }

    @Override
    public State getState() {
        throw new UnsupportedOperationException("Should never call me to find the state");
    }

    @Override
    public long getNetworkId() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public long getEntityOwnerId() {
        Account account = UserContext.current().getCaller();

        if (account != null) {
            return account.getId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are
// tracked
    }

    @Override
    public long getDomainId() {
        IpAddress ip = _networkService.getIp(ipAddressId);
        return ip.getDomainId();
    }

    @Override
    public Ip getDestinationIpAddress() {
        return null;
    }

    @Override
    public void setDestinationIpAddress(Ip destinationIpAddress) {
        return;
    }

    @Override
    public int getDestinationPortStart() {
        return privateStartPort.intValue();
    }

    @Override
    public int getDestinationPortEnd() {
        return privateStartPort.intValue();
    }

    @Override
    public void create() {

        // cidr list parameter is deprecated
        if (cidrlist != null) {
            throw new InvalidParameterValueException("Parameter cidrList is deprecated; if you need to open firewall rule for the specific cidr, please refer to createFirewallRule command");
        }

        try {
            PortForwardingRule result = _rulesService.createPortForwardingRule(this, virtualMachineId, getOpenFirewall());
            setEntityId(result.getId());
        } catch (NetworkRuleConflictException ex) {
            s_logger.info("Network rule conflict: ", ex);
            s_logger.trace("Network Rule Conflict: ", ex);
            throw new ServerApiException(BaseCmd.NETWORK_RULE_CONFLICT_ERROR, ex.getMessage());
        }
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NET_RULE_ADD;
    }

    @Override
    public String getEventDescription() {
        IpAddress ip = _networkService.getIp(ipAddressId);
        return ("Applying port forwarding  rule for Ip: " + ip.getAddress() + " with virtual machine:" + virtualMachineId);
    }

    @Override
    public long getAccountId() {
        IpAddress ip = _networkService.getIp(ipAddressId);
        return ip.getAccountId();
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        return getIp().getAssociatedWithNetworkId();
    }

    private IpAddress getIp() {
        IpAddress ip = _networkService.getIp(ipAddressId);
        if (ip == null) {
            throw new InvalidParameterValueException("Unable to find ip address by id " + ipAddressId);
        }
        return ip;
    }

    @Override
    public Integer getIcmpCode() {
        return null;
    }

    @Override
    public Integer getIcmpType() {
        return null;
    }

    @Override
    public Long getRelated() {
        return null;
    }

    @Override
    public FirewallRuleType getType() {
        return FirewallRuleType.User;
    }
    
    @Override
    public AsyncJob.Type getInstanceType() {
        return AsyncJob.Type.FirewallRule;
    }

}
