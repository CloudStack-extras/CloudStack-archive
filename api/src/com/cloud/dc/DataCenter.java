/**
 * 
 */
package com.cloud.dc;

import java.util.Map;

import com.cloud.org.Grouping;

/**
 *
 */
public interface DataCenter extends Grouping {
    public enum NetworkType {
        Basic,
        Advanced,
    }
    long getId();
    String getDns1();
    String getDns2();
    String getGuestNetworkCidr();
    String getName();
    Long getDomainId();
    String getDescription();
    String getDomain();
    String getVnet();
    
    NetworkType getNetworkType();
    String getInternalDns1();
    String getInternalDns2();
    String getDnsProvider();
    String getGatewayProvider();
    String getFirewallProvider();
    String getDhcpProvider();
    String getLoadBalancerProvider();
    String getUserDataProvider();
    String getVpnProvider();
    boolean isSecurityGroupEnabled();
    Map<String, String> getDetails();
    void setDetails(Map<String, String> details);
    AllocationState getAllocationState();
    String getZoneToken();

}
