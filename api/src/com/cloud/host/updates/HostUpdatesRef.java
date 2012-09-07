package com.cloud.host.updates;

/**
 * @author sanjay
 *
 */
public interface HostUpdatesRef {

    long getId();
    long getHostId();
    long getPatchId();
    boolean getIsApplied();
    String getLabel();
    String getDescription();
    String getURL();
    String getAfterApplyGuidance();
    String getTimestamp();
}
