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
package com.cloud.vm;

import java.net.URI;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.cloud.network.Networks.AddressFormat;
import com.cloud.network.Networks.Mode;
import com.cloud.utils.db.GenericDao;

@Entity
@Table(name = "nics")
public class NicVO implements Nic {
    protected NicVO() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "instance_id")
    Long instanceId;

    @Column(name = "ip4_address")
    String ip4Address;

    @Column(name = "ip6_address")
    String ip6Address;

    @Column(name = "netmask")
    String netmask;

    @Column(name = "isolation_uri")
    URI isolationUri;

    @Column(name = "ip_type")
    AddressFormat addressFormat;

    @Column(name = "broadcast_uri")
    URI broadcastUri;

    @Column(name = "gateway")
    String gateway;

    @Column(name = "mac_address")
    String macAddress;

    @Column(name = "mode")
    @Enumerated(value = EnumType.STRING)
    Mode mode;

    @Column(name = "network_id")
    long networkId;

    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    State state;

    @Column(name = "reserver_name")
    String reserver;

    @Column(name = "reservation_id")
    String reservationId;

    @Column(name = "device_id")
    int deviceId;

    @Column(name = "update_time")
    Date updateTime;

    @Column(name = "default_nic")
    boolean defaultNic;

    @Column(name = "strategy")
    @Enumerated(value = EnumType.STRING)
    ReservationStrategy reservationStrategy;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "vm_type")
    VirtualMachine.Type vmType;

    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;

    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    
    @Column(name = "elastic_ip_vm_id")
    Long elasticIpVmId;

    public NicVO(String reserver, Long instanceId, long configurationId, VirtualMachine.Type vmType) {
        this.reserver = reserver;
        this.instanceId = instanceId;
        this.networkId = configurationId;
        this.state = State.Allocated;
        this.vmType = vmType;
    }

    @Override
    public String getIp4Address() {
        return ip4Address;
    }

    public void setIp4Address(String address) {
        ip4Address = address;
    }

    @Override
    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public boolean isDefaultNic() {
        return defaultNic;
    }

    public void setDefaultNic(boolean defaultNic) {
        this.defaultNic = defaultNic;
    }

    public String getIp6Address() {
        return ip6Address;
    }

    public void setIp6Address(String ip6Address) {
        this.ip6Address = ip6Address;
    }

    @Override
    public String getNetmask() {
        return netmask;
    }

    @Override
    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    @Override
    public AddressFormat getAddressFormat() {
        return addressFormat;
    }

    public void setAddressFormat(AddressFormat format) {
        this.addressFormat = format;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    @Override
    public URI getIsolationUri() {
        return isolationUri;
    }

    public void setIsolationUri(URI isolationUri) {
        this.isolationUri = isolationUri;
    }

    @Override
    public URI getBroadcastUri() {
        return broadcastUri;
    }

    public void setBroadcastUri(URI broadcastUri) {
        this.broadcastUri = broadcastUri;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public void setNetworkId(long networkId) {
        this.networkId = networkId;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getInstanceId() {
        return instanceId;
    }

    @Override
    public long getNetworkId() {
        return networkId;
    }

    @Override
    public int getDeviceId() {
        return deviceId;
    }

    @Override
    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String id) {
        this.reservationId = id;
    }

    public void setReservationStrategy(ReservationStrategy strategy) {
        this.reservationStrategy = strategy;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public String getReserver() {
        return reserver;
    }

    public void setReserver(String reserver) {
        this.reserver = reserver;
    }

    @Override
    public ReservationStrategy getReservationStrategy() {
        return reservationStrategy;
    }

    @Override
    public Date getUpdateTime() {
        return updateTime;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(Date removed) {
        this.removed = removed;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return new StringBuilder("Nic[").append(id).append("-").append(instanceId).append("-").append(reservationId).append("-").append(ip4Address).append("]").toString();
    }

    @Override
    public VirtualMachine.Type getVmType() {
        return vmType;
    }


    @Override
    public Long getElasticIpVmId() {
        return elasticIpVmId;
    }

    public void setElasticIpVmId(Long elasticIpVmId) {
        this.elasticIpVmId = elasticIpVmId;
    }
}
