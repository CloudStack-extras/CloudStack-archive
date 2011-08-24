/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.server.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.response.BaseResponse;
import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

public class ExternalFirewallResponse extends BaseResponse {

    @SerializedName(ApiConstants.ID) @Param(description="the ID of the external firewall")
    private Long id;
    
    @SerializedName(ApiConstants.ZONE_ID) @Param(description="the zone ID of the external firewall")
    private Long zoneId;
    
    @SerializedName(ApiConstants.IP_ADDRESS) @Param(description="the management IP address of the external firewall")
    private String ipAddress;
    
    @SerializedName(ApiConstants.USERNAME) @Param(description="the username that's used to log in to the external firewall")
    private String username;
    
    @SerializedName(ApiConstants.PUBLIC_INTERFACE) @Param(description="the public interface of the external firewall")
    private String publicInterface;
    
    @SerializedName(ApiConstants.USAGE_INTERFACE) @Param(description="the usage interface of the external firewall")
    private String usageInterface;
    
    @SerializedName(ApiConstants.PRIVATE_INTERFACE) @Param(description="the private interface of the external firewall")
    private String privateInterface;
    
    @SerializedName(ApiConstants.PUBLIC_ZONE) @Param(description="the public security zone of the external firewall")
    private String publicZone;
    
    @SerializedName(ApiConstants.PRIVATE_ZONE) @Param(description="the private security zone of the external firewall")
    private String privateZone;
    
    @SerializedName(ApiConstants.NUM_RETRIES) @Param(description="the number of times to retry requests to the external firewall")
    private String numRetries;
    
    @SerializedName(ApiConstants.TIMEOUT) @Param(description="the timeout (in seconds) for requests to the external firewall")
    private String timeout;
    
    public Long getId() {
    	return id;
    }
    
    public void setId(Long id) {
    	this.id = id;
    }
    
    public Long getZoneId() {
    	return zoneId;
    }
    
    public void setZoneId(Long zoneId) {
    	this.zoneId = zoneId;
    }
    
    public String getIpAddress() {
    	return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
    	this.ipAddress = ipAddress;
    }
    
    public String getUsername() {
    	return username;
    }
    
    public void setUsername(String username) {
    	this.username = username;
    }
    
    public String getPublicInterface() {
    	return publicInterface;
    }
    
    public void setPublicInterface(String publicInterface) {
    	this.publicInterface = publicInterface;
    }
    
    public String getUsageInterface() {
    	return usageInterface;
    }
    
    public void setUsageInterface(String usageInterface) {
    	this.usageInterface = usageInterface;
    }
    
    public String getPrivateInterface() {
    	return privateInterface;
    }
    
    public void setPrivateInterface(String privateInterface) {
    	this.privateInterface = privateInterface;
    }
    
    public String getPublicZone() {
    	return publicZone;
    }
    
    public void setPublicZone(String publicZone) {
    	this.publicZone = publicZone;
    }
    
    public String getPrivateZone() {
    	return privateZone;
    }
    
    public void setPrivateZone(String privateZone) {
    	this.privateZone = privateZone;
    }
    
    public String getNumRetries() {
    	return numRetries;
    }
    
    public void setNumRetries(String numRetries) {
    	this.numRetries = numRetries;
    }
    
    public String getTimeout() {
    	return timeout;
    }
    
    public void setTimeout(String timeout) {
    	this.timeout = timeout;
    }
}
