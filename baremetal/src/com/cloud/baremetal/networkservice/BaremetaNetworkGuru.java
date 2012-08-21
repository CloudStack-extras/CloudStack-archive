package com.cloud.baremetal.networkservice;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.dc.Pod;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapcityException;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Network;
import com.cloud.network.Networks.AddressFormat;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.guru.PodBasedNetworkGuru;
import com.cloud.utils.Pair;
import com.cloud.utils.component.Inject;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

public class BaremetaNetworkGuru extends PodBasedNetworkGuru {
    private static final Logger s_logger = Logger.getLogger(BaremetaNetworkGuru.class);
    @Inject
    private HostDao _hostDao; 
    
    @Override
    public void reserve(NicProfile nic, Network config, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest, ReservationContext context) throws InsufficientVirtualNetworkCapcityException,
            InsufficientAddressCapacityException {
        if (dest.getHost().getHypervisorType() != HypervisorType.BareMetal) {
            super.reserve(nic, config, vm, dest, context);
            return;
        }
        
        HostVO host = _hostDao.findById(dest.getHost().getId());
        String intentIp = host.getDetail(ApiConstants.IP_ADDRESS);
        if (intentIp == null) {
            super.reserve(nic, config, vm, dest, context);
            return;
        }
        
        Pod pod = dest.getPod();
        Pair<String, Long> ip = _dcDao.allocatePrivateIpAddress(dest.getDataCenter().getId(), dest.getPod().getId(), nic.getId(), context.getReservationId(), intentIp);
        if (ip == null) {
            throw new InsufficientAddressCapacityException("Unable to get a management ip address", Pod.class, pod.getId());
        }
        
        nic.setIp4Address(ip.first());
        nic.setMacAddress(NetUtils.long2Mac(NetUtils.createSequenceBasedMacAddress(ip.second())));
        nic.setGateway(pod.getGateway());
        nic.setFormat(AddressFormat.Ip4);
        String netmask = NetUtils.getCidrNetmask(pod.getCidrSize());
        nic.setNetmask(netmask);
        nic.setBroadcastType(BroadcastDomainType.Native);
        nic.setBroadcastUri(null);
        nic.setIsolationUri(null);
        
        s_logger.debug("Allocated a nic " + nic + " for " + vm);
    }
}
