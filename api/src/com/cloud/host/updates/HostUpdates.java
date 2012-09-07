package com.cloud.host.updates;


public interface HostUpdates {
	
    long getId();
	long getHostId();
	String getLabel();
	String getDescription();
	String getURL();
	String getAfterApplyGuidance();
	String getTimestamp();
}
