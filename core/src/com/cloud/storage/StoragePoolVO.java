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
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.utils.db.GenericDao;

/**
 * @author chiradeep
 *
 */
@Entity
@Table(name="storage_pool")
public class StoragePoolVO implements StoragePool {
    @Id
    @TableGenerator(name="storage_pool_sq", table="sequence", pkColumnName="name", valueColumnName="value", pkColumnValue="storage_pool_seq", allocationSize=1)
    @Column(name="id", updatable=false, nullable = false)
	private long id;
    
    @Column(name="name", updatable=false, nullable=false, length=255)
	private String name = null;

    @Column(name="uuid", length=255)
	private String uuid = null;
    
    @Column(name="pool_type", updatable=false, nullable=false, length=32)
    @Enumerated(value=EnumType.STRING)
    private StoragePoolType poolType;
    
    @Column(name=GenericDao.CREATED_COLUMN)
    Date created;
    
    @Column(name=GenericDao.REMOVED_COLUMN)
    private Date removed;
    
    @Column(name="update_time", updatable=true)
    @Temporal(value=TemporalType.TIMESTAMP)
    private Date updateTime;
    
    @Column(name="data_center_id", updatable=true, nullable=false)
    private long dataCenterId;
    
    @Column(name="pod_id", updatable=true)
    private Long podId;
    
    @Column(name="available_bytes", updatable=true, nullable=true)
    private long availableBytes;
    
    @Column(name="capacity_bytes", updatable=true, nullable=true)
    private long capacityBytes;

    @Column(name="status",  updatable=true, nullable=false)
    @Enumerated(value=EnumType.STRING)
    private StoragePoolStatus status;
    
	@Override
    public long getId() {
		return id;
	}
	
	@Override
	public StoragePoolStatus getStatus() {
		return status;
	}

	public StoragePoolVO() {
		// TODO Auto-generated constructor stub
	}

	@Override
    public String getName() {
		return name;
	}

	@Override
    public String getUuid() {
		return uuid;
	}

	@Override
    public StoragePoolType getPoolType() {
		return poolType;
	}

	@Override
    public Date getCreated() {
		return created;
	}

	public Date getRemoved() {
		return removed;
	}

	@Override
    public Date getUpdateTime() {
		return updateTime;
	}

	@Override
    public long getDataCenterId() {
		return dataCenterId;
	}

	@Override
    public long getAvailableBytes() {
		return availableBytes;
	}

	@Override
    public long getCapacityBytes() {
		return capacityBytes;
	}

	public void setAvailableBytes(long available) {
		availableBytes = available;
	}
	
	public void setCapacityBytes(long capacity) {
		capacityBytes = capacity;
	}
	
    @Column(name="host_address")
    private String hostAddress;
    
    @Column(name="path")
    private String path;
    
    @Column(name="port")
    private int port;

    @Column(name="cluster_id")
    private Long clusterId;
    
    
    @Override
    public Long getClusterId() {
        return clusterId;
    }
    
    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }
    
    @Override
    public String getHostAddress() {
        return hostAddress;
    }
    
    @Override
    public String getPath() {
        return path;
    }
    
    public StoragePoolVO(long poolId, String name, String uuid, StoragePoolType type,
            long dataCenterId, Long podId, long availableBytes, long capacityBytes, String hostAddress, int port, String hostPath) {
        this.name  = name;
        this.id = poolId;
        this.uuid = uuid;
        this.poolType = type;
        this.dataCenterId = dataCenterId;
        this.availableBytes = availableBytes;
        this.capacityBytes = capacityBytes;
        this.hostAddress = hostAddress;
        this.path = hostPath;
        this.port = port;
        this.podId = podId;
        this.setStatus(StoragePoolStatus.Up);
    }
    
    public StoragePoolVO(StoragePoolVO that) {
        this(that.id, that.name, that.uuid, that.poolType, that.dataCenterId, that.podId, that.availableBytes, that.capacityBytes, that.hostAddress, that.port, that.path);
    }
    
    public StoragePoolVO(StoragePoolType type, String hostAddress, int port, String path) {
        this.poolType = type;
        this.hostAddress = hostAddress;
        this.port = port;
        this.path = path;
        this.setStatus(StoragePoolStatus.Up);
    }
    
    public void setStatus(StoragePoolStatus status)
    {
    	this.status = status;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public void setDataCenterId(long dcId) {
        this.dataCenterId = dcId;
    }
    
    public void setPodId(Long podId) {
        this.podId = podId;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public void setPath(String path) {
    	this.path = path;
    }
    
    @Override
    public int getPort() {
        return port;
    }
    
    @Override
    public boolean isShared() {
    	return poolType.isShared();
    }
    
    @Override
    public boolean isLocal() {
    	return !poolType.isShared();
    }
    
    @Transient
    public String toUri() {
        /*
        URI uri = new URI();
        try {
            if (type == StoragePoolType.Filesystem) {
                uri.setScheme("file");
            } else if (type == StoragePoolType.NetworkFilesystem) {
                uri.setScheme("nfs");
            } else if (type == StoragePoolType.IscsiLUN) {
            }
        } catch (MalformedURIException e) {
            throw new VmopsRuntimeException("Unable to form the uri " + id);
        }
        return uri.toString();
        */
        return null;
    }

	@Override
    public Long getPodId() {
		return podId;
	}
	
	public void setName(String name) {
	    this.name = name;
	}
	
	public boolean isInMaintenance() {
	    return status == StoragePoolStatus.PrepareForMaintenance || status == StoragePoolStatus.Maintenance || status == StoragePoolStatus.ErrorInMaintenance || removed != null;
	}
	
	@Override
    public boolean equals(Object obj) {
	    if (!(obj instanceof StoragePoolVO) || obj == null) {
	        return false;
	    }
	    StoragePoolVO that = (StoragePoolVO)obj;
	    return this.id == that.id;
	}
	
	@Override
	public int hashCode() {
	    return new Long(id).hashCode();
	}
	
    @Override
    public String toString() {
        return new StringBuilder("Pool[").append(id).append("|").append(poolType).append("]").toString();
    }
}
