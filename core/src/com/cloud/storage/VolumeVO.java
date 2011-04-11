/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.storage;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.db.GenericDao;

@Entity
@Table(name = "volumes")
public class VolumeVO implements Volume {
    @Id
    @TableGenerator(name = "volume_sq", table = "sequence", pkColumnName = "name", valueColumnName = "value", pkColumnValue = "volume_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id")
    long id;

    @Column(name = "name")
    String name;

    @Column(name = "pool_id")
    Long poolId;

    @Column(name = "account_id")
    long accountId;

    @Column(name = "domain_id")
    long domainId;

    @Column(name = "instance_id")
    Long instanceId = null;

    @Column(name = "device_id")
    Long deviceId = null;

    @Column(name = "size")
    long size;

    @Column(name = "folder")
    String folder;

    @Column(name = "path")
    String path;

    @Column(name = "pod_id")
    Long podId;

    @Column(name = "created")
    Date created;

    @Column(name = "attached")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date attached;

    @Column(name = "data_center_id")
    long dataCenterId;

    @Column(name = "host_ip")
    String hostip;

    @Column(name = "disk_offering_id")
    long diskOfferingId;

    @Column(name = "template_id")
    Long templateId;

    @Column(name = "first_snapshot_backup_uuid")
    String firstSnapshotBackupUuid;

    @Column(name = "volume_type")
    @Enumerated(EnumType.STRING)
    Type volumeType = Volume.Type.UNKNOWN;

    @Column(name = "pool_type")
    @Enumerated(EnumType.STRING)
    StoragePoolType poolType;

    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;

    @Column(name = "updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date updated;

    @Column(name = "recreatable")
    boolean recreatable;

    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    private State state;

    @Column(name = "chain_info")
    String chainInfo;

    // Real Constructor
    public VolumeVO(Type type, String name, long dcId, long domainId, long accountId, long diskOfferingId, long size) {
        this.volumeType = type;
        this.name = name;
        this.dataCenterId = dcId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.size = size;
        this.diskOfferingId = diskOfferingId;
        this.state = State.Allocated;
    }

    public VolumeVO(String name, long dcId, long podId, long accountId, long domainId, Long instanceId, String folder, String path, long size, Volume.Type vType) {
        this.name = name;
        this.accountId = accountId;
        this.domainId = domainId;
        this.instanceId = instanceId;
        this.folder = folder;
        this.path = path;
        this.size = size;
        this.podId = podId;
        this.dataCenterId = dcId;
        this.volumeType = vType;
        this.recreatable = false;
    }

    // Copy Constructor
    public VolumeVO(Volume that) {
        this(that.getName(), that.getDataCenterId(), that.getPodId(), that.getAccountId(), that.getDomainId(), that.getInstanceId(), that.getFolder(), that.getPath(), that.getSize(), that
                .getVolumeType());
        this.recreatable = that.isRecreatable();
        this.state = that.getState();
        this.size = that.getSize();
        this.diskOfferingId = that.getDiskOfferingId();
        this.poolId = that.getPoolId();
        this.attached = that.getAttached();
        this.chainInfo = that.getChainInfo();
        this.templateId = that.getTemplateId();
        this.deviceId = that.getDeviceId();
    }

    @Override
    public boolean isRecreatable() {
        return recreatable;
    }

    public void setRecreatable(boolean recreatable) {
        this.recreatable = recreatable;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Long getPodId() {
        return podId;
    }

    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setPoolType(StoragePoolType poolType) {
        this.poolType = poolType;
    }

    public StoragePoolType getPoolType() {
        return poolType;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public String getFolder() {
        return folder;
    }

    @Override
    public String getPath() {
        return path;
    }

    protected VolumeVO() {
    }

    @Override
    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public Long getInstanceId() {
        return instanceId;
    }

    @Override
    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public Type getVolumeType() {
        return volumeType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setDomainId(long domainId) {
        this.domainId = domainId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHostIp() {
        return hostip;
    }

    public void setHostIp(String hostip) {
        this.hostip = hostip;
    }

    public void setPodId(Long podId) {
        this.podId = podId;
    }

    public void setDataCenterId(long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public void setVolumeType(Type type) {
        volumeType = type;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(Date removed) {
        this.removed = removed;
    }

    @Override
    public long getDiskOfferingId() {
        return diskOfferingId;
    }

    public void setDiskOfferingId(long diskOfferingId) {
        this.diskOfferingId = diskOfferingId;
    }

    @Override
    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getFirstSnapshotBackupUuid() {
        return firstSnapshotBackupUuid;
    }

    public void setFirstSnapshotBackupUuid(String firstSnapshotBackupUuid) {
        this.firstSnapshotBackupUuid = firstSnapshotBackupUuid;
    }

    @Override
    public Long getPoolId() {
        return poolId;
    }

    public void setPoolId(Long poolId) {
        this.poolId = poolId;
    }

    public Date getUpdated() {
        return updated;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return new StringBuilder("Vol[").append(id).append("|vm=").append(instanceId).append("|").append(volumeType).append("]").toString();
    }

    @Override
    public Date getAttached() {
        return this.attached;
    }

    public void setAttached(Date attached) {
        this.attached = attached;
    }

    @Override
    public String getChainInfo() {
        return this.chainInfo;
    }

    public void setChainInfo(String chainInfo) {
        this.chainInfo = chainInfo;
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VolumeVO) {
            return id == ((VolumeVO) obj).id;
        } else {
            return false;
        }
    }
}
