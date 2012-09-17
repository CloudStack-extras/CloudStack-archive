package com.cloud.host.updates;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Join table for updates hosts and patches
 * @author sanjay
 *
 */
@Entity
@Table(name="patch_host_ref")
public class PatchHostVO implements PatchHostRef{
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
    public boolean getUpdateApplied() {
        return isApplied;
    }

    public void setUpdateApplied(boolean value) {
        isApplied = value;
    }

    @Override
    public String getAfterApplyGuidance() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTimestamp() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getURL() {
        // TODO Auto-generated method stub
        return null;
    }

}
