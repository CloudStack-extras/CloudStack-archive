package com.cloud.updates.interfaces;

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
