package com.cloud.host.updates;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Join table for updates hosts and patches
 */
@Entity
@Table(name="host_updates_ref")
public class HostUpdatesRefVO implements HostUpdatesRef {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;

    @Column(name="host_id")
    private long hostId;

    @Column(name="patch_id")
    private long patchId;

    @Column(name="update_applied")
    private boolean isApplied;

    @Override
    public long getHostId() {
        return hostId;
    }

    public void setHostId(long hostId) {
        this.hostId = hostId;
    }

    @Override
    public long getPatchId() {
        return patchId;
    }

    public void setPatchId(long patchId) {
        this.patchId = patchId;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean getIsApplied() {
        return isApplied;
    }

    public void setIsApplied(boolean value) {
        isApplied = value;
    }

    @Override
    public String getAfterApplyGuidance() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public String getTimestamp() {
        return null;
    }

    @Override
    public String getURL() {
        return null;
    }
}
