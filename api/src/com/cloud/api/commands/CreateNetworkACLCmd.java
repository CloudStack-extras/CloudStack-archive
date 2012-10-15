// Copyright 2012 Citrix Systems, Inc. Licensed under the
package com.cloud.api.commands;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.converters.basic.StringBufferConverter;
import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.NetworkACLResponse;
import com.cloud.async.AsyncJob;
import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.vpc.Vpc;
import com.cloud.user.Account;
import com.cloud.user.UserContext;
import com.cloud.utils.net.NetUtils;

@Implementation(description = "Creates a ACL rule the given network (the network has to belong to VPC)", 
responseObject = NetworkACLResponse.class)
public class CreateNetworkACLCmd extends BaseAsyncCreateCmd implements FirewallRule {
    public static final Logger s_logger = Logger.getLogger(CreateNetworkACLCmd.class.getName());

    private static final String s_name = "createnetworkaclresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.PROTOCOL, type = CommandType.STRING, required = true, description = 
            "the protocol for the ACL rule. Valid values are TCP/UDP/ICMP.")
    private String protocol;

    @Parameter(name = ApiConstants.START_PORT, type = CommandType.INTEGER, description = "the starting port of ACL")
    private Integer publicStartPort;

    @Parameter(name = ApiConstants.END_PORT, type = CommandType.INTEGER, description = "the ending port of ACL")
    private Integer publicEndPort;

    @Parameter(name = ApiConstants.CIDR_LIST, type = CommandType.LIST, collectionType = CommandType.STRING, 
            description = "the cidr list to allow traffic from/to")
    private List<String> cidrlist;

    @Parameter(name = ApiConstants.ICMP_TYPE, type = CommandType.INTEGER, description = "type of the icmp message being sent")
    private Integer icmpType;

    @Parameter(name = ApiConstants.ICMP_CODE, type = CommandType.INTEGER, description = "error code for this icmp message")
    private Integer icmpCode;

    @IdentityMapper(entityTableName="networks")
    @Parameter(name=ApiConstants.NETWORK_ID, type=CommandType.LONG, required=true,
    description="The network of the vm the ACL will be created for")
    private Long networkId;

    @Parameter(name=ApiConstants.TRAFFIC_TYPE, type=CommandType.STRING, description="the traffic type for the ACL," +
            "can be Ingress or Egress, defaulted to Ingress if not specified")
    private String trafficType;

    private final String  egress = "Egress";
    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEntityTable() {
        return "firewall_rules";
    }

    public Long getIpAddressId() {
        return null;
    }

    @Override
    public String getProtocol() {
        return protocol.trim();
    }

    @Override
    public List<String> getSourceCidrList() {
        if (cidrlist != null) {
            return cidrlist;
        } else {
            List<String> oneCidrList = new ArrayList<String>();
            oneCidrList.add(NetUtils.ALL_CIDRS);
            return oneCidrList;
        }
    }

    public Long getVpcId() {
        Network network = _networkService.getNetwork(getNetworkId());
        if (network == null) {
            throw new InvalidParameterValueException("Invalid networkId is given", null);
        }

        Long vpcId = network.getVpcId();
        return vpcId;
    }

    @Override
    public FirewallRule.TrafficType getTrafficType() {
        if (trafficType == null) {
            return FirewallRule.TrafficType.Ingress;
        }
        else if (egress.equalsIgnoreCase(trafficType)){
            return TrafficType.Egress;
        }
        for (FirewallRule.TrafficType type : FirewallRule.TrafficType.values()) {
            if (type.toString().equalsIgnoreCase(trafficType)) {
                return type;
            }
        }
        throw new InvalidParameterValueException("Invalid traffic type " + trafficType, null);
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    public void setSourceCidrList(List<String> cidrs){
        cidrlist = cidrs;
    }

    @Override
    public void execute() throws ResourceUnavailableException {
        UserContext callerContext = UserContext.current();
        boolean success = false;
        FirewallRule rule = _networkACLService.getNetworkACL(getEntityId());
        try {
            UserContext.current().setEventDetails("Rule Id: " + getEntityId());
            success = _networkACLService.applyNetworkACLs(rule.getNetworkId(), callerContext.getCaller());

            // State is different after the rule is applied, so get new object here
            NetworkACLResponse aclResponse = new NetworkACLResponse(); 
            if (rule != null) {
                aclResponse = _responseGenerator.createNetworkACLResponse(rule);
                setResponseObject(aclResponse);
            }
            aclResponse.setResponseName(getCommandName());
        } finally {
            if (!success || rule == null) {
                _networkACLService.revokeNetworkACL(getEntityId(), true);
                throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to create network ACL");
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
    public Long getSourceIpAddressId() {
        return null;
    }

    @Override
    public Integer getSourcePortStart() {
        if (publicStartPort != null) {
            return publicStartPort.intValue();
        }
        return null;
    }

    @Override
    public Integer getSourcePortEnd() {
        if (publicEndPort == null) {
            if (publicStartPort != null) {
                return publicStartPort.intValue();
            }
        } else {
            return publicEndPort.intValue();
        }

        return null;
    }

    @Override
    public Purpose getPurpose() {
        return Purpose.Firewall;
    }

    @Override
    public State getState() {
        throw new UnsupportedOperationException("Should never call me to find the state");
    }

    @Override
    public long getNetworkId() {
        return networkId;
    }

    @Override
    public long getEntityOwnerId() {
        if (getVpcId() != null ) {
           Vpc vpc = _vpcService.getVpc(getVpcId());
           if (vpc == null) {
               throw new InvalidParameterValueException("Invalid vpcId is given", null);
           }
           Account account = _accountService.getAccount(vpc.getAccountId());
           return account.getId();
        }
        return  _networkService.getNetwork(networkId).getAccountId();
    }

    @Override
    public long getDomainId() {
        if (getVpcId() != null) {
           Vpc vpc = _vpcService.getVpc(getVpcId());
           return vpc.getDomainId();
        }
        return _networkService.getNetwork(networkId).getDomainId();
    }

    @Override
    public void create() {
        if (getSourceCidrList() != null) {
            for (String cidr: getSourceCidrList()){
                if (!NetUtils.isValidCIDR(cidr)){
                    throw new ServerApiException(BaseCmd.PARAM_ERROR, "Source cidrs formatting error " + cidr); 
                }
            }
        }
        if (getVpcId() == null ){
            if (getTrafficType() == TrafficType.Ingress ){
               throw new    ServerApiException(BaseCmd.PARAM_ERROR, "Networkacl ingress rules are not supported for non vpc networks.");
            }
        }
        try {
            FirewallRule result = _networkACLService.createNetworkACL(this);
            setEntityId(result.getId());
        } catch (NetworkRuleConflictException ex) {
            s_logger.info("Network rule conflict: " + ex.getMessage());
            s_logger.trace("Network Rule Conflict: ", ex);
            throw new ServerApiException(BaseCmd.NETWORK_RULE_CONFLICT_ERROR, ex.getMessage());
        }
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_FIREWALL_OPEN;
    }

    @Override
    public String getEventDescription() {
        Network network = _networkService.getNetwork(networkId);
        return ("Createing Network ACL for Netowrk: " + network + " for protocol:" + this.getProtocol());
    }

    @Override
    public long getAccountId() {
        if (getVpcId() != null){
        Vpc vpc = _vpcService.getVpc(getVpcId());
        return vpc.getAccountId();
        }
        return  _networkService.getNetwork(networkId).getAccountId();
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        return getNetworkId();
    }

    @Override
    public Integer getIcmpCode() {
        if (icmpCode != null) {
            return icmpCode;
        } else if (protocol.equalsIgnoreCase(NetUtils.ICMP_PROTO)) {
            return -1;
        }
        return null;
    }

    @Override
    public Integer getIcmpType() {
        if (icmpType != null) {
            return icmpType;
        } else if (protocol.equalsIgnoreCase(NetUtils.ICMP_PROTO)) {
            return -1;

        }
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
