package com.cloud.simulator;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table(name="mockhost")

public class MockHostVO implements MockHost {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private long id;
    
    @Column(name="name", nullable=false)
    private String name = null;
    
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
    
    @Column(name="storage_mac_address")
    private String storageMacAddress;
    
    @Column(name="storage_netmask")
    private String storageNetMask;
    
    @Column(name="guid")
    private String guid;
    
    @Column(name="version")
    private String version;
    
    @Column(name="data_center_id", nullable=false)
    private long dataCenterId;
    
    @Column(name="pod_id")
    private Long podId;
    
    @Column(name="cluster_id")
    private Long clusterId;
    
    @Column(name="speed")
    private long cpuSpeed;
    
    @Column(name="cpus")
    private long cpuCount;
    
    @Column(name="ram")
    private long memorySize;
    
    @Column(name="capabilities")
    private String capabilities;
    
    @Column(name="vm_id")
    private long vmId;
    
    @Column(name="resource")
    private String resource;
    
   
    
    public MockHostVO() {
        
    }
    
    
    public long getVmId() {
        return vmId;
    }
    
    public void setVmId(long vmId) {
        this.vmId = vmId;
    }
    
    public String getResource() {
        return this.resource;
    }
    
    public void setResource(String resource) {
        this.resource = resource;
    }
    
    public long getCpuSpeed() {
        return this.cpuSpeed;
    }
    
    public void setCpuSpeed(long cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }
    
    public long getCpuCount() {
        return this.cpuCount;
    }
    
    public void setCpuCount(long cpuCount) {
        this.cpuCount = cpuCount;
    }
    
    public long getMemorySize() {
        return this.memorySize;
    }
    
    public void setMemorySize(long memorySize) {
        this.memorySize = memorySize;
    }
    
    public String getCapabilities() {
        return this.capabilities;
    }
    
    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }
    
    public long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getGuid() {
        return this.guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Long getDataCenterId() {
        return this.dataCenterId;
    }
    
    public void setDataCenterId(Long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }
    
    public Long getPodId() {
        return this.podId;
    }
    
    public void setPodId(long podId) {
        this.podId = podId;
    }
    
    public Long getClusterId() {
        return this.clusterId;
    }
    
    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }
     
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }
    
    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }
    
    public String getPrivateNetMask() {
        return this.privateNetmask;
    }
    
    public void setPrivateNetMask(String privateNetmask) {
        this.privateNetmask = privateNetmask;
    }
    
    public String getPrivateMacAddress() {
        return this.privateMacAddress;
    }
    
    public void setPrivateMacAddress(String privateMacAddress) {
        this.privateMacAddress = privateMacAddress;
    }
    
    public String getPublicIpAddress() {
        return this.publicIpAddress;
    }
    
    public void setPublicIpAddress(String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }
    
    public String getPublicNetMask() {
        return this.publicNetmask;
    }
    
    public void setPublicNetMask(String publicNetMask) {
        this.publicNetmask = publicNetMask;
    }
    
    public String getPublicMacAddress() {
        return this.publicMacAddress;
    }
    
    public void setPublicMacAddress(String publicMacAddress) {
        this.publicMacAddress = publicMacAddress;
    }
    
    public String getStorageIpAddress() {
        return this.storageIpAddress;
    }
    
    public void setStorageIpAddress(String storageIpAddress) {
        this.storageIpAddress = storageIpAddress;
    }
    
    public String getStorageNetMask() {
        return this.storageNetMask;
    }
    
    public void setStorageNetMask(String storageNetMask) {
        this.storageNetMask = storageNetMask;
    }
    
    public String getStorageMacAddress() {
        return this.storageMacAddress;
    }
    
    public void setStorageMacAddress(String storageMacAddress) {
        this.storageMacAddress = storageMacAddress;
    }
}
