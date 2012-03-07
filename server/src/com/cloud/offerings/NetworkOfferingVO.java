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
package com.cloud.offerings;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.cloud.api.Identity;
import com.cloud.network.Network;
import com.cloud.network.Networks.TrafficType;
import com.cloud.offering.NetworkOffering;
import com.cloud.utils.db.GenericDao;

@Entity
@Table(name = "network_offerings")
public class NetworkOfferingVO implements NetworkOffering, Identity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "name")
    String name;

    @Column(name = "unique_name")
    private String uniqueName;

    @Column(name = "display_text")
    String displayText;

    @Column(name = "nw_rate")
    Integer rateMbps;

    @Column(name = "mc_rate")
    Integer multicastRateMbps;

    @Column(name = "traffic_type")
    @Enumerated(value = EnumType.STRING)
    TrafficType trafficType;

    @Column(name = "specify_vlan")
    boolean specifyVlan;

    @Column(name = "system_only")
    boolean systemOnly;

    @Column(name = "service_offering_id")
    Long serviceOfferingId;

    @Column(name = "tags", length = 4096)
    String tags;

    @Column(name = "default")
    boolean isDefault;

    @Column(name = "availability")
    @Enumerated(value = EnumType.STRING)
    Availability availability;

    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    State state = State.Disabled;

    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;

    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;

    @Column(name = "guest_type")
    @Enumerated(value = EnumType.STRING)
    Network.GuestType guestType;

    @Column(name = "dedicated_lb_service")
    boolean dedicatedLB;

    @Column(name = "shared_source_nat_service")
    boolean sharedSourceNat;

    @Column(name = "specify_ip_ranges")
    boolean specifyIpRanges = false;

    @Column(name = "sort_key")
    int sortKey;

    @Column(name = "uuid")
    String uuid;

    @Column(name = "redundant_router_service")
    boolean redundantRouter;

    @Column(name = "conserve_mode")
    boolean conserveMode;

    @Column(name = "elastic_ip_service")
    boolean elasticIp;

    @Column(name = "elastic_lb_service")
    boolean elasticLb;

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public TrafficType getTrafficType() {
        return trafficType;
    }

    @Override
    public Integer getMulticastRateMbps() {
        return multicastRateMbps;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getRateMbps() {
        return rateMbps;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public boolean isSystemOnly() {
        return systemOnly;
    }

    public Date getRemoved() {
        return removed;
    }

    @Override
    public String getTags() {
        return tags;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public void setRateMbps(Integer rateMbps) {
        this.rateMbps = rateMbps;
    }

    public void setMulticastRateMbps(Integer multicastRateMbps) {
        this.multicastRateMbps = multicastRateMbps;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean getSpecifyVlan() {
        return specifyVlan;
    }

    @Override
    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    @Override
    public String getUniqueName() {
        return uniqueName;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Network.GuestType getGuestType() {
        return guestType;
    }

    public void setServiceOfferingId(Long serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }

    @Override
    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }

    @Override
    public boolean getDedicatedLB() {
        return dedicatedLB;
    }

    public void setDedicatedLB(boolean dedicatedLB) {
        this.dedicatedLB = dedicatedLB;
    }

    @Override
    public boolean getSharedSourceNat() {
        return sharedSourceNat;
    }

    public void setSharedSourceNat(boolean sharedSourceNat) {
        this.sharedSourceNat = sharedSourceNat;
    }

    @Override
    public boolean getRedundantRouter() {
        return redundantRouter;
    }

    public void setRedundantRouter(boolean redundantRouter) {
        this.redundantRouter = redundantRouter;
    }

    public NetworkOfferingVO(String name, String displayText, TrafficType trafficType, boolean systemOnly, boolean specifyVlan, Integer rateMbps, Integer multicastRateMbps, boolean isDefault,
            Availability availability, String tags, Network.GuestType guestType, boolean conserveMode, boolean specifyIpRanges) {
        this.name = name;
        this.displayText = displayText;
        this.rateMbps = rateMbps;
        this.multicastRateMbps = multicastRateMbps;
        this.trafficType = trafficType;
        this.systemOnly = systemOnly;
        this.specifyVlan = specifyVlan;
        this.isDefault = isDefault;
        this.availability = availability;
        this.uniqueName = name;
        this.uuid = UUID.randomUUID().toString();
        this.tags = tags;
        this.guestType = guestType;
        this.conserveMode = conserveMode;
        this.dedicatedLB = true;
        this.sharedSourceNat = false;
        this.redundantRouter = false;
        this.elasticIp = false;
        this.elasticLb = false;
        this.specifyIpRanges = specifyIpRanges;
    }

    public NetworkOfferingVO(String name, String displayText, TrafficType trafficType, boolean systemOnly, boolean specifyVlan, Integer rateMbps, Integer multicastRateMbps, boolean isDefault,
            Availability availability, String tags, Network.GuestType guestType, boolean conserveMode, boolean dedicatedLb, boolean sharedSourceNat, boolean redundantRouter, boolean elasticIp, boolean elasticLb,
            boolean specifyIpRanges) {
        this(name, displayText, trafficType, systemOnly, specifyVlan, rateMbps, multicastRateMbps, isDefault, availability, tags, guestType, conserveMode, specifyIpRanges);
        this.dedicatedLB = dedicatedLb;
        this.sharedSourceNat = sharedSourceNat;
        this.redundantRouter = redundantRouter;
        this.elasticIp = elasticIp;
        this.elasticLb = elasticLb;
    }

    public NetworkOfferingVO() {
    }

    /**
     * Network Offering for all system vms.
     * 
     * @param name
     * @param trafficType
     * @param specifyIpRanges
     *            TODO
     */
    public NetworkOfferingVO(String name, TrafficType trafficType, boolean specifyIpRanges) {
        this(name, "System Offering for " + name, trafficType, true, false, 0, 0, true, Availability.Required, null, null, true, specifyIpRanges);
        this.state = State.Enabled;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("[Network Offering [");
        return buf.append(id).append("-").append(trafficType).append("-").append(name).append("]").toString();
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setSortKey(int key) {
        sortKey = key;
    }

    public int getSortKey() {
        return sortKey;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    @Override
    public boolean isConserveMode() {
        return conserveMode;
    }

    @Override
    public boolean getElasticIp() {
        return elasticIp;
    }

    @Override
    public boolean getElasticLb() {
        return elasticLb;
    }

    @Override
    public boolean getSpecifyIpRanges() {
        return specifyIpRanges;
    }

}
