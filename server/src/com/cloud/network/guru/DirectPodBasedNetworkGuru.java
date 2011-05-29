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

package com.cloud.network.guru;

import java.net.URI;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.Pod;
import com.cloud.dc.Vlan;
import com.cloud.dc.Vlan.VlanType;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapcityException;
import com.cloud.network.Network;
import com.cloud.network.NetworkManager;
import com.cloud.network.Networks.AddressFormat;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.addr.PublicIp;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.user.UserContext;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.vm.Nic.ReservationStrategy;
import com.cloud.vm.VirtualMachine.Type;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

@Local(value = NetworkGuru.class)
public class DirectPodBasedNetworkGuru extends DirectNetworkGuru {
    private static final Logger s_logger = Logger.getLogger(DirectPodBasedNetworkGuru.class);

    @Inject
    DataCenterDao _dcDao;
    @Inject
    VlanDao _vlanDao;
    @Inject
    NetworkManager _networkMgr;
    @Inject
    IPAddressDao _ipAddressDao;
    @Inject
    NetworkOfferingDao _networkOfferingDao;
    @Inject
    ConfigurationDao _configDao;
    
    boolean _elasticIpEnabled;
    String _elasticIpGuestCidrs;

    @Override
    protected boolean canHandle(NetworkOffering offering, DataCenter dc) {
        // this guru handles system Direct pod based network
        if (dc.getNetworkType() == NetworkType.Basic && offering.getTrafficType() == TrafficType.Guest && offering.isSystemOnly()) {
            return true;
        } else {
            s_logger.trace("We only take care of Guest Direct Pod based networks");
            return false;
        }
    }

    @Override
    public NicProfile allocate(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm) throws InsufficientVirtualNetworkCapcityException,
            InsufficientAddressCapacityException, ConcurrentOperationException {

        DataCenter dc = _dcDao.findById(network.getDataCenterId());
        NetworkOffering offering = _networkOfferingDao.findByIdIncludingRemoved(network.getNetworkOfferingId());

        if (!canHandle(offering, dc)) {
            return null;
        }

        if (nic == null) {
            nic = new NicProfile(ReservationStrategy.Start, null, null, null, null);
        } else if (nic.getIp4Address() == null) {
            nic.setStrategy(ReservationStrategy.Start);
        } else {
            nic.setStrategy(ReservationStrategy.Create);
        }

        return nic;
    }

    @Override
    public void reserve(NicProfile nic, Network network, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest, ReservationContext context)
            throws InsufficientVirtualNetworkCapcityException, InsufficientAddressCapacityException, ConcurrentOperationException {
        if (nic.getIp4Address() == null) {
            getIp(nic, dest.getPod(), vm, network);
            nic.setStrategy(ReservationStrategy.Create);
        }
    }
    
    @DB
    public void getIp(NicProfile nic, Pod pod, VirtualMachineProfile<? extends VirtualMachine> vm, Network network) throws InsufficientVirtualNetworkCapcityException,
            InsufficientAddressCapacityException, ConcurrentOperationException {
        DataCenter dc = _dcDao.findById(pod.getDataCenterId());
        Transaction txn = Transaction.currentTxn();
        txn.start();
        if (nic.getIp4Address() == null) {
            PublicIp ip = _networkMgr.assignPublicIpAddress(dc.getId(), pod.getId(), vm.getOwner(), VlanType.DirectAttached, network.getId());
            nic.setIp4Address(ip.getAddress().toString());
            nic.setFormat(AddressFormat.Ip4);
            nic.setGateway(ip.getGateway());
            nic.setNetmask(ip.getNetmask());
            if (ip.getVlanTag() != null && ip.getVlanTag().equalsIgnoreCase(Vlan.UNTAGGED)) {
                nic.setIsolationUri(URI.create("ec2://" + Vlan.UNTAGGED));
                nic.setBroadcastUri(URI.create("vlan://" + Vlan.UNTAGGED));
                nic.setBroadcastType(BroadcastDomainType.Native);
            }
            nic.setReservationId(String.valueOf(ip.getVlanTag()));
            nic.setMacAddress(ip.getMacAddress());
        }
        nic.setDns1(dc.getDns1());
        nic.setDns2(dc.getDns2());
        if (_elasticIpEnabled && vm.getType()==Type.User) {
            if (nic.getElasticIpAddressId() == null) {
                PublicIp ip = _networkMgr.assignElasticPublicIpAddress(dc.getId(), vm.getId(),  vm.getOwner());
                nic.setElasticIpAddressId(ip.getId());
            }
        }
        txn.commit();
        
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        final Map<String, String> configs = _configDao.getConfiguration(params);
        _elasticIpEnabled = Boolean.parseBoolean(configs.get(Config.ElasticIpVmEnabled.key()));
        if (_elasticIpEnabled) {
            _elasticIpGuestCidrs = configs.get(Config.ElasticIpGuestCidrs.key());
            s_logger.info("Elastic ip feature is enabled, guest cidrs are " + _elasticIpGuestCidrs);
        }
        return true;
    }

    @Override
    public boolean release(NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, String reservationId) {
        boolean result = true;
        if (_elasticIpEnabled) {
            if (nic.getElasticIpAddressId() != null) {
                result = _networkMgr.releasePublicIpAddress(nic.getElasticIpAddressId(), 
                        UserContext.current().getCallerUserId(), vm.getOwner());
            }
        }
        return result && super.release(nic, vm, reservationId);
    }

}
