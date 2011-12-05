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
package com.cloud.network;

import java.net.URI;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Identity;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.Mode;
import com.cloud.network.Networks.TrafficType;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.net.NetUtils;

/**
 * NetworkConfigurationVO contains information about a specific network.
 *
 */
@Entity
@Table(name="networks")
public class NetworkVO implements Network, Identity {
    @Id
    @TableGenerator(name="networks_sq", table="sequence", pkColumnName="name", valueColumnName="value", pkColumnValue="networks_seq", allocationSize=1)
    @Column(name="id")
    long id;

    @Column(name="mode")
    @Enumerated(value=EnumType.STRING)
    Mode mode;

    @Column(name="broadcast_domain_type")
    @Enumerated(value=EnumType.STRING)
    BroadcastDomainType broadcastDomainType;

    @Column(name="traffic_type")
    @Enumerated(value=EnumType.STRING)
    TrafficType trafficType;

    @Column(name="name")
    String name;

    @Column(name="display_text")
    String displayText;;

    @Column(name="broadcast_uri")
    URI broadcastUri;

    @Column(name="gateway")
    String gateway;

    @Column(name="cidr")
    String cidr;

    @Column(name="network_offering_id")
    long networkOfferingId;

    @Column(name="physical_network_id")
    Long physicalNetworkId;
    
    @Column(name="data_center_id")
    long dataCenterId;

    @Column(name="related")
    long related;

    @Column(name="guru_name")
    String guruName;

    @Column(name="state")
    @Enumerated(value=EnumType.STRING)
    State state;

    @Column(name="dns1")
    String dns1;

    @Column(name="domain_id")
    long domainId;

    @Column(name="account_id")
    long accountId;

    @Column(name="set_fields")
    long setFields;

    @TableGenerator(name="mac_address_seq", table="op_networks", pkColumnName="id", valueColumnName="mac_address_seq", allocationSize=1)
    @Transient
    long macAddress = 1;

    @Column(name="guru_data", length=1024)
    String guruData;

    @Column(name="dns2")
    String dns2;

    @Column(name="network_domain")
    String networkDomain;

    @Column(name=GenericDao.REMOVED_COLUMN)
    Date removed;

    @Column(name=GenericDao.CREATED_COLUMN)
    Date created;

    @Column(name="reservation_id")
    String reservationId;
    
    @Column(name="uuid")
    String uuid;
    
    @Column(name="guest_type")
    @Enumerated(value=EnumType.STRING)
    Network.GuestType guestType;
    
    @Column(name="acl_type")
    @Enumerated(value=EnumType.STRING)
    ControlledEntity.ACLType aclType;

    public NetworkVO() {
    	this.uuid = UUID.randomUUID().toString();
    }

    /**
     * Constructor to be used for the adapters because it only initializes what's needed.
     * @param trafficType
     * @param mode
     * @param broadcastDomainType
     * @param networkOfferingId
     * @param state TODO
     * @param dataCenterId
     * @param physicalNetworkId TODO
     */
    public NetworkVO(TrafficType trafficType, Mode mode, BroadcastDomainType broadcastDomainType, long networkOfferingId, State state, long dataCenterId, Long physicalNetworkId) {
        this.trafficType = trafficType;
        this.mode = mode;
        this.broadcastDomainType = broadcastDomainType;
        this.networkOfferingId = networkOfferingId;
        this.dataCenterId = dataCenterId;
        this.physicalNetworkId = physicalNetworkId;
        if (state == null) {
            state = State.Allocated;
        } else {
            this.state = state;
        }
        this.id = -1;
        this.guestType = guestType;
    	this.uuid = UUID.randomUUID().toString();
    }

    public NetworkVO(long id, Network that, long offeringId, String guruName, long domainId, long accountId, long related, String name, String displayText, String networkDomain, GuestType guestType, long dcId, Long physicalNetworkId, ACLType aclType) {
        this(id, that.getTrafficType(), that.getMode(), that.getBroadcastDomainType(), offeringId, domainId, accountId, related, name, displayText, networkDomain,guestType, dcId, physicalNetworkId, aclType);
        this.gateway = that.getGateway();
        this.cidr = that.getCidr();
        this.broadcastUri = that.getBroadcastUri();
        this.broadcastDomainType = that.getBroadcastDomainType();
        this.guruName = guruName;
        this.state = that.getState();
        if (state == null) {
            state = State.Allocated;
        }
    	this.uuid = UUID.randomUUID().toString();
    }

    /**
     * Constructor for the actual DAO object.
     * @param trafficType
     * @param mode
     * @param broadcastDomainType
     * @param networkOfferingId
     * @param domainId
     * @param accountId
     * @param name
     * @param displayText
     * @param networkDomain
     * @param guestType TODO
     * @param aclType TODO
     * @param isShared TODO
     * @param isShared
     * @param dataCenterId
     */
    public NetworkVO(long id, TrafficType trafficType, Mode mode, BroadcastDomainType broadcastDomainType, long networkOfferingId, long domainId, long accountId, long related, String name, String displayText, String networkDomain, GuestType guestType, long dcId, Long physicalNetworkId, ACLType aclType) {
        this(trafficType, mode, broadcastDomainType, networkOfferingId, State.Allocated, dcId, physicalNetworkId);
        this.domainId = domainId;
        this.accountId = accountId;
        this.related = related;
        this.id = id;
        this.name = name;
        this.displayText = displayText;
        this.aclType = aclType;
        this.networkDomain = networkDomain;
    	this.uuid = UUID.randomUUID().toString();
        this.guestType = guestType;
    }

    @Override
    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public long getRelated() {
        return related;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getNetworkOfferingId() {
        return networkOfferingId;
    }

    public void setNetworkOfferingId(long networkOfferingId) {
        this.networkOfferingId = networkOfferingId;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public BroadcastDomainType getBroadcastDomainType() {
        return broadcastDomainType;
    }

    public String getGuruData() {
        return guruData;
    }

    public void setGuruData(String guruData) {
        this.guruData = guruData;
    }

    public String getGuruName() {
        return guruName;
    }

    public void setGuruName(String guruName) {
        this.guruName = guruName;
    }

    public void setBroadcastDomainType(BroadcastDomainType broadcastDomainType) {
        this.broadcastDomainType = broadcastDomainType;
    }

    @Override
    public String getNetworkDomain() {
        return networkDomain;
    }

    public void setNetworkDomain(String networkDomain) {
        this.networkDomain = networkDomain;
    }

    @Override
    public TrafficType getTrafficType() {
        return trafficType;
    }

    public void setTrafficType(TrafficType trafficType) {
        this.trafficType = trafficType;
    }

    @Override
    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    @Override
    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    @Override
    public URI getBroadcastUri() {
        return broadcastUri;
    }

    public void setBroadcastUri(URI broadcastUri) {
        this.broadcastUri = broadcastUri;
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(id);
    }

    @Override
    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }
    
    @Override
    public void setPhysicalNetworkId(Long physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }

    public String getDns1() {
        return dns1;
    }

    public void setDns1(String dns) {
        this.dns1 = dns;
    }

    public String getDns2() {
        return dns2;
    }

    public void setDns2(String dns) {
        this.dns2 = dns;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
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
    public Network.GuestType getGuestType() {
        return guestType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NetworkVO)) {
            return false;
        }
        NetworkVO that = (NetworkVO)obj;
        if (this.trafficType != that.trafficType) {
            return false;
        }

        if ((this.cidr == null && that.cidr != null) || (this.cidr != null && that.cidr == null)) {
            return false;
        }

        if (this.cidr == null && that.cidr == null) {
            return true;
        }

        return NetUtils.isNetworkAWithinNetworkB(this.cidr, that.cidr);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("Ntwk[");
        buf.append(id).append("|").append(trafficType.toString()).append("|").append(networkOfferingId).append("]");
        return buf.toString();
    }


    public String getUuid() {
    	return this.uuid;
    }
    
    public void setUuid(String uuid) {
    	this.uuid = uuid;
    }

	public ControlledEntity.ACLType getAclType() {
		return aclType;
	}

}
