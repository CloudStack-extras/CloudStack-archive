package com.cloud.host.updates;

/**
 * @author sanjay
 *
 */
public interface PatchHostRef {

    long getId();
    long getHostId();
    long getPatchId();
    boolean getUpdateApplied();
    String getLabel();
    String getDescription();
    String getURL();
    String getAfterApplyGuidance();
    String getTimestamp();
}
