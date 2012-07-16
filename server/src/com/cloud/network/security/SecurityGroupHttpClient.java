package com.cloud.network.security;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.cloud.agent.api.SecurityGroupRuleAnswer;
import com.cloud.agent.api.SecurityGroupRulesCmd;
import com.cloud.agent.api.SecurityGroupRulesCmd.IpPortAndProto;
import com.cloud.network.security.schema.SecurityGroupRule;
import com.cloud.network.security.schema.SecurityGroupVmRuleSet;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

public class SecurityGroupHttpClient {
    private static final String BODY_NAME = "ruleset";
    private static final String APP_NAME = "securitygroup";
    private JAXBContext context;
    private int port;
    
    private enum OpConstant {
        setRules,
        removeRules,
    }

    public SecurityGroupHttpClient() {
        try {
            context = JAXBContext.newInstance("com.cloud.network.security.schema");
            port = 9988;
        } catch (Exception e) {
            throw new CloudRuntimeException("Unable to create JAXBContext for security group", e);
        }
    }

    private List<SecurityGroupRule> generateRules(IpPortAndProto[] ipps) {
        List<SecurityGroupRule> rules = new ArrayList<SecurityGroupRule>(ipps.length);
        for (SecurityGroupRulesCmd.IpPortAndProto ipp : ipps) {
            SecurityGroupRule r = new SecurityGroupRule();
            r.setProtocol(ipp.getProto());
            r.setStartPort(ipp.getStartPort());
            r.setEndPort(ipp.getEndPort());
            for (String cidr : ipp.getAllowedCidrs()) {
                String[] toks = cidr.split("/");
                long ipnum = NetUtils.ip2Long(toks[0]);
                r.getIp().add(Long.toHexString(ipnum) + "/" + toks[1]);
            }
            rules.add(r);
        }
        return rules;
    }

    public SecurityGroupRuleAnswer call(String agentIp, SecurityGroupRulesCmd cmd) {
        HttpClient httpClient = new HttpClient();
        try {
            SecurityGroupVmRuleSet rset = new SecurityGroupVmRuleSet();
            rset.getEgressRules().addAll(generateRules(cmd.getEgressRuleSet()));
            rset.getIngressRules().addAll(generateRules(cmd.getIngressRuleSet()));
            rset.setVmName(cmd.getVmName());
            rset.setVmIp(cmd.getGuestIp());
            rset.setVmMac(cmd.getGuestMac());
            rset.setVmId(cmd.getVmId());
            rset.setSignature(cmd.getSignature());
            rset.setSequenceNumber(cmd.getSeqNum());
            Marshaller marshaller = context.createMarshaller();
            StringWriter writer = new StringWriter();
            marshaller.marshal(rset, writer);
            String xmlContents = writer.toString();
            
            PostMethod post = new PostMethod(String.format("http://%s:%s/%s/%s/", agentIp, getPort(), APP_NAME, OpConstant.setRules));
            post.setParameter(BODY_NAME, xmlContents);
            if (httpClient.executeMethod(post) != 200) {
                return new SecurityGroupRuleAnswer(cmd, false, post.getResponseBodyAsString());
            } else {
                return new SecurityGroupRuleAnswer(cmd);
            }
        } catch (Exception e) {
            return new SecurityGroupRuleAnswer(cmd, false, e.getMessage());
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
