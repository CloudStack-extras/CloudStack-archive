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
package com.cloud.host;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.db.GenericDao;

@Entity
@Table(name="host")
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@DiscriminatorColumn(name="type", discriminatorType=DiscriminatorType.STRING, length=32)
public class HostVO implements Host {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
	private long id;

    @Column(name="disconnected")
    @Temporal(value=TemporalType.TIMESTAMP)
    private Date disconnectedOn;
    
    @Column(name="name", nullable=false)
	private String name = null;
    
    /**
     * Note: There is no setter for status because it has to be set in the dao code.
     */
    @Column(name="status", nullable=false)
    private Status status = null;

    @Column(name="type", updatable = true, nullable=false)
    @Enumerated(value=EnumType.STRING)
    private Type type;
    
    @Column(name="private_ip_address", nullable=false)
	private String privateIpAddress;
    
    @Column(name="private_mac_address", nullable=false)
    private String privateMacAddress;
    
    @Column(name="private_netmask", nullable=false)
    private String privateNetmask;
    
    @Column(name="public_netmask")
    private String publicNetmask;
    
    @Column(name="public_ip_address")
    private String publicIpAddress;
    
    @Column(name="public_mac_address")
    private String publicMacAddress;
    
    @Column(name="storage_ip_address")
    private String storageIpAddress;

    @Column(name="cluster_id")
    private Long clusterId;
    
    @Column(name="storage_netmask")
    private String storageNetmask;
    
    @Column(name="storage_mac_address")
    private String storageMacAddress;
    
    @Column(name="storage_ip_address_2")
    private String storageIpAddressDeux;
    
    @Column(name="storage_netmask_2")
    private String storageNetmaskDeux;
    
    @Column(name="storage_mac_address_2")
    private String storageMacAddressDeux;
    
    @Column(name="hypervisor_type", updatable = true, nullable=false)
    @Enumerated(value=EnumType.STRING)
    private HypervisorType hypervisorType;
    
    @Column(name="proxy_port")
    private Integer proxyPort;
    
    @Column(name="resource")
    private String resource;
    
    @Column(name="fs_type")
    private StoragePoolType fsType;
    
    @Column(name="available")
    private boolean available = true;
    
    @Column(name="setup")
    private boolean setup = false;
    
    @Column(name="allocation_state", nullable=false)
    @Enumerated(value=EnumType.STRING)
    private HostAllocationState hostAllocationState;


    // This is a delayed load value.  If the value is null,
    // then this field has not been loaded yet.
    // Call host dao to load it.
    @Transient
    Map<String, String> details;
    
    // This is a delayed load value.  If the value is null,
    // then this field has not been loaded yet.
    // Call host dao to load it.
    @Transient
    List<String> hostTags;

    @Override
    public String getStorageIpAddressDeux() {
		return storageIpAddressDeux;
	}

	public void setStorageIpAddressDeux(String deuxStorageIpAddress) {
		this.storageIpAddressDeux = deuxStorageIpAddress;
	}

	public String getStorageNetmaskDeux() {
		return storageNetmaskDeux;
	}

	@Override
    public Long getClusterId() {
	    return clusterId;
	}
	
	public void setClusterId(Long clusterId) {
	    this.clusterId = clusterId;
	}
	
	public void setStorageNetmaskDeux(String deuxStorageNetmask) {
		this.storageNetmaskDeux = deuxStorageNetmask;
	}

	public String getStorageMacAddressDeux() {
		return storageMacAddressDeux;
	}

	public void setStorageMacAddressDeux(String duexStorageMacAddress) {
		this.storageMacAddressDeux = duexStorageMacAddress;
	}

	public String getPrivateMacAddress() {
        return privateMacAddress;
    }

    public void setPrivateMacAddress(String privateMacAddress) {
        this.privateMacAddress = privateMacAddress;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getPrivateNetmask() {
        return privateNetmask;
    }

    public void setPrivateNetmask(String privateNetmask) {
        this.privateNetmask = privateNetmask;
    }

    public String getPublicNetmask() {
        return publicNetmask;
    }

    public void setPublicNetmask(String publicNetmask) {
        this.publicNetmask = publicNetmask;
    }

    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public String getPublicMacAddress() {
        return publicMacAddress;
    }

    public void setPublicMacAddress(String publicMacAddress) {
        this.publicMacAddress = publicMacAddress;
    }

    @Override
    public String getStorageIpAddress() {
        return storageIpAddress;
    }

    public void setStorageIpAddress(String storageIpAddress) {
        this.storageIpAddress = storageIpAddress;
    }

    public String getStorageNetmask() {
        return storageNetmask;
    }

    public void setStorageNetmask(String storageNetmask) {
        this.storageNetmask = storageNetmask;
    }

    public String getStorageMacAddress() {
        return storageMacAddress;
    }
    
    public boolean isSetup() {
        return setup;
    }
    
    public void setSetup(boolean setup) {
        this.setup = setup;
    }

    public void setStorageMacAddress(String storageMacAddress) {
        this.storageMacAddress = storageMacAddress;
    }
    
    public String getResource() {
        return resource;
    }
    
    public void setResource(String resource) {
        this.resource = resource;
    }
    
    public Map<String, String> getDetails() {
        return details;
    }
    
    public String getDetail(String name) {
        assert (details != null) : "Did you forget to load the details?";
        
        return details != null ? details.get(name) : null;
    }
    
    public void setDetail(String name, String value) {
        assert (details != null) : "Did you forget to load the details?";
        
        details.put(name, value);
    }
    
    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    public List<String> getHostTags() {
        return hostTags;
    }
   
    public void setHostTags(List<String> hostTags) {
        this.hostTags = hostTags;
    }

    @Column(name="data_center_id", nullable=false)
    private long dataCenterId;
    
    @Column(name="pod_id")
	private Long podId;
    
    @Column(name="cpus")
    private Integer cpus;
    
    @Column(name="url")
    private String storageUrl;

    @Column(name="speed")
    private Long speed;
    
    @Column(name="ram")
    private long totalMemory;
    
    @Column(name="parent", nullable=false)
    private String parent;
    
    @Column(name="guid", updatable=true, nullable=false)
    private String guid;

	@Column(name="capabilities")
    private String caps;
    
    @Column(name="total_size")
    private Long totalSize;
    
    @Column(name="last_ping")
    private long lastPinged;
    
    @Column(name="mgmt_server_id")
    private Long managementServerId;
    
    @Column(name="dom0_memory")
    private long dom0MinMemory;
    
    @Column(name="version")
    private String version;
    
    @Column(name=GenericDao.CREATED_COLUMN)
    private Date created;
    
    @Column(name=GenericDao.REMOVED_COLUMN)
    private Date removed;
    
    public HostVO(String guid) {
        this.guid = guid;
        this.status = Status.Up;
        this.totalMemory = 0;
        this.dom0MinMemory = 0;
        this.hostAllocationState = Host.HostAllocationState.Enabled;
    }
    
    protected HostVO() {
    }
    
	public HostVO(long id,
	              String name,
	              Type type,
	              String privateIpAddress,
	              String privateNetmask,
	              String privateMacAddress,
	              String publicIpAddress,
	              String publicNetmask,
	              String publicMacAddress,
	              String storageIpAddress,
	              String storageNetmask,
	              String storageMacAddress,
	              String deuxStorageIpAddress,
	              String duxStorageNetmask,
	              String deuxStorageMacAddress,
	              String guid,
	              Status status,
	              String version,
	              String iqn,
	              Date disconnectedOn,
	              long dcId,
	              Long podId,
	              long serverId,
	              long ping,
	              String parent,
	              long totalSize,
	              StoragePoolType fsType) {
	    this(id, name, type, privateIpAddress, privateNetmask, privateMacAddress, publicIpAddress, publicNetmask, publicMacAddress, storageIpAddress, storageNetmask, storageMacAddress, guid, status, version, iqn, disconnectedOn, dcId, podId, serverId, ping, null, null, null, 0, null);
	    this.parent = parent;
	    this.totalSize = totalSize;
	    this.fsType = fsType;
	    this.hostAllocationState = Host.HostAllocationState.Enabled;
	}
	
    public HostVO(long id,
                  String name,
                  Type type,
                  String privateIpAddress,
                  String privateNetmask,
                  String privateMacAddress,
                  String publicIpAddress,
                  String publicNetmask,
                  String publicMacAddress,
                  String storageIpAddress,
                  String storageNetmask,
                  String storageMacAddress,
                  String guid,
                  Status status,
                  String version,
                  String url,
                  Date disconnectedOn,
                  long dcId,
                  Long podId,
                  long serverId,
                  long ping,
                  Integer cpus,
                  Long speed,
                  Long totalMemory,
                  long dom0MinMemory,
                  String caps) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.type = type;
        this.privateIpAddress = privateIpAddress;
        this.privateNetmask = privateNetmask;
        this.privateMacAddress = privateMacAddress;
        this.publicIpAddress = publicIpAddress;
        this.publicNetmask = publicNetmask;
        this.publicMacAddress = publicMacAddress;
        this.storageIpAddress = storageIpAddress;
        this.storageNetmask = storageNetmask;
        this.storageMacAddress = storageMacAddress;
        this.dataCenterId = dcId;
        this.podId = podId;
        this.cpus = cpus;
        this.version = version;
        this.speed = speed;
        this.totalMemory = totalMemory != null ? totalMemory : 0;
        this.guid = guid;
        this.parent = null;
        this.totalSize = null;
        this.fsType = null;
        this.managementServerId = serverId;
        this.lastPinged = ping;
        this.caps = caps;
        this.disconnectedOn = disconnectedOn;
        this.dom0MinMemory = dom0MinMemory;
        this.storageUrl = url;
        this.hostAllocationState = Host.HostAllocationState.Enabled;
    }
    
    public void setPodId(Long podId) {
        
        this.podId = podId;
    }
    
    public void setDataCenterId(long dcId) {
        this.dataCenterId = dcId;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public void setStorageUrl(String url) {
        this.storageUrl = url;
    }
    
    public void setDisconnectedOn(Date disconnectedOn) {
        this.disconnectedOn = disconnectedOn;
    }
    
    public String getStorageUrl() {
        return storageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrivateIpAddress(String ipAddress) {
        this.privateIpAddress = ipAddress;
    }

    public void setCpus(Integer cpus) {
        this.cpus = cpus;
    }

    public void setSpeed(Long speed) {
        this.speed = speed;
    }

    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setCaps(String caps) {
        this.caps = caps;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public void setLastPinged(long lastPinged) {
        this.lastPinged = lastPinged;
    }

    public void setManagementServerId(Long managementServerId) {
        this.managementServerId = managementServerId;
    }

    @Override
    public long getLastPinged() {
        return lastPinged;
    }
    
    @Override
    public String getParent() {
        return parent;
    }
    
    @Override
    public long getTotalSize() {
        return totalSize;
    }
    
    @Override
    public String getCapabilities() {
        return caps;
    }
    
    @Override
    public Date getCreated() {
        return created;
    }
    
    @Override
    public Date getRemoved() {
        return removed;
    }
    
    @Override
    public String getVersion() {
        return version;
    }

    public void setType(Type type) {
        this.type = type;
    }
    
	@Override
    public long getId() {
		return id;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Status getStatus() {
		return status;
	}
	
	@Override
	public long getDataCenterId() {
		return dataCenterId;
	}
	
	@Override
	public Long getPodId() {
		return podId;
	}
    
    @Override
    public Long getManagementServerId() {
        return managementServerId;
    }
    
    @Override
    public Date getDisconnectedOn() {
        return disconnectedOn;
    }
    
    @Override
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }
    
    @Override
    public String getGuid() {
        return guid;
    }
    
    public void setGuid(String guid) {
		this.guid = guid;
	}
    
    @Override
    public Integer getCpus() {
        return cpus;
    }
    
    @Override
    public Long getSpeed() {
        return speed;
    }
    
    @Override
    public Long getTotalMemory() {
        return totalMemory;
    }

    @Override
    public Integer getProxyPort() {
    	return proxyPort;
    }
    
    public void setProxyPort(Integer port) {
    	proxyPort = port;
    }
    
    public StoragePoolType getFsType() {
        return fsType;
    }
    
    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(id);
    }
    
    @Override
	public boolean equals(Object obj) {
    	if (obj instanceof HostVO) {
    		return ((HostVO)obj).getId() == this.getId();
    	} else {
    		return false;
    	}
    }

    @Override
    public String toString() {
    	return new StringBuilder(type.toString()).append("-").append(id).append("-").append(name).toString();
    }

	public void setHypervisorType(HypervisorType hypervisorType) {
		this.hypervisorType = hypervisorType;
	}

	@Override
	public HypervisorType getHypervisorType() {
		return hypervisorType;
	}
	
	@Override
	public HostAllocationState getHostAllocationState() {
    	return hostAllocationState;
    }
    
    public void setHostAllocationState(HostAllocationState hostAllocationState) {
		this.hostAllocationState = hostAllocationState;
    }	
}
