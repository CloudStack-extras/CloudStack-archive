package com.cloud.network.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.cloud.agent.api.SecurityGroupRuleAnswer;
import com.cloud.agent.api.SecurityGroupRulesCmd;
import com.cloud.agent.api.SecurityGroupRulesCmd.IpPortAndProto;
import com.cloud.network.security.schema.SecurityGroupRule;
import com.cloud.network.security.schema.SecurityGroupVmRuleSet;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

public class SecurityGroupHttpClient {
    private static final Logger logger = Logger.getLogger(SecurityGroupHttpClient.class);
	private static final String ARG_NAME = "args";
	private static final String COMMAND = "command";
	private static final String APP_NAME = "securitygroup";
	private JAXBContext context;
	private int port;

	private enum OpConstant {
		setRules, echo,
	}

	public SecurityGroupHttpClient() {
		try {
			context = JAXBContext
					.newInstance("com.cloud.network.security.schema");
			port = 9988;
		} catch (Exception e) {
			throw new CloudRuntimeException(
					"Unable to create JAXBContext for security group", e);
		}
	}

	private List<SecurityGroupRule> generateRules(IpPortAndProto[] ipps) {
		List<SecurityGroupRule> rules = new ArrayList<SecurityGroupRule>(
				ipps.length);
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

    public boolean echo(String agentIp, int timeout, int interval) {
        HttpClient httpClient = new HttpClient();
        boolean ret = false;
        int count = 1;
        while (true) {
            try {
                Thread.sleep(interval);
                count++;
            } catch (InterruptedException e1) {
                logger.warn("", e1);
                break;
            }
            
            try {
                PostMethod post = new PostMethod(String.format("http://%s:%s/%s/", agentIp, getPort(), APP_NAME));
                post.setParameter(COMMAND, OpConstant.echo.toString());
                if (httpClient.executeMethod(post) != 200) {
                    logger.debug(String.format("echoing baremetal security group agent on %s got error: %s", agentIp, post.getResponseBodyAsString()));
                } else {
                    ret = true;
                }
                break;
            } catch (Exception e) {
                if (count*interval >= timeout) {
                    break;
                }
            }
        }
        return ret;
    }
	
	public SecurityGroupRuleAnswer call(String agentIp,
			SecurityGroupRulesCmd cmd) {
		HttpClient httpClient = new HttpClient();
		try {
			SecurityGroupVmRuleSet rset = new SecurityGroupVmRuleSet();
			rset.getEgressRules().addAll(generateRules(cmd.getEgressRuleSet()));
			rset.getIngressRules().addAll(
					generateRules(cmd.getIngressRuleSet()));
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

			/*
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// Note : not using GZipOutputStream since that is for files
			// GZipOutputStream gives a different header, although the
			// compression is the same
			DeflaterOutputStream dzip = new DeflaterOutputStream(out);
			dzip.write(xmlContents.getBytes());
			dzip.close();
			xmlContents = Base64.encodeBase64URLSafeString(out.toByteArray());
			*/

			PostMethod post = new PostMethod(String.format(
					"http://%s:%s/%s/", agentIp, getPort(), APP_NAME));
			post.setParameter(COMMAND, OpConstant.setRules.toString());
			post.setParameter(ARG_NAME, xmlContents);
			if (httpClient.executeMethod(post) != 200) {
				return new SecurityGroupRuleAnswer(cmd, false,
						post.getResponseBodyAsString());
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
