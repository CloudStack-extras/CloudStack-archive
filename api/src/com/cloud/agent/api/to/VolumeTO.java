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
package com.cloud.agent.api.to;

import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StoragePool;
import com.cloud.storage.Volume;

public class VolumeTO {
    protected VolumeTO() {
    }

    private long id;
    private String name;
    private String mountPoint;
    private String path;
    private long size;
    private Volume.Type type;
    private StoragePoolType storagePoolType;
    private String storagePoolUuid;
    private long deviceId;
    private String chainInfo;
    private String guestOsType;

    public VolumeTO(long id, Volume.Type type, StoragePoolType poolType, String poolUuid, String name, String mountPoint, String path, long size, String chainInfo) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.size = size;
        this.type = type;
        this.storagePoolType = poolType;
        this.storagePoolUuid = poolUuid;
        this.mountPoint = mountPoint;
        this.chainInfo = chainInfo;
    }

    public VolumeTO(long id, Volume.Type type, StoragePoolType poolType, String poolUuid, String name, String mountPoint, String path, long size, String chainInfo, String guestOsType) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.size = size;
        this.type = type;
        this.storagePoolType = poolType;
        this.storagePoolUuid = poolUuid;
        this.mountPoint = mountPoint;
        this.chainInfo = chainInfo;
        this.guestOsType = guestOsType;
    }

    public VolumeTO(Volume volume, StoragePool pool) {
        this.id = volume.getId();
        this.name = volume.getName();
        this.path = volume.getPath();
        this.size = volume.getSize();
        this.type = volume.getVolumeType();
        this.storagePoolType = pool.getPoolType();
        this.storagePoolUuid = pool.getUuid();
        this.mountPoint = volume.getFolder();
        this.chainInfo = volume.getChainInfo();
        if (volume.getDeviceId() != null)
            this.deviceId = volume.getDeviceId();
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long id) {
        this.deviceId = id;
    }

    public long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public Volume.Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public StoragePoolType getPoolType() {
        return storagePoolType;
    }

    public String getPoolUuid() {
        return storagePoolUuid;
    }

    public String getChainInfo() {
        return chainInfo;
    }

    public String getOsType() {
        return guestOsType;
    }

    @Override
    public String toString() {
        return new StringBuilder("Vol[").append(id).append("|").append(type).append("|").append(path).append("|").append(size).append("]").toString();
    }
}
