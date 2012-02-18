package com.cloud.network.guru;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.dc.Pod;
import com.cloud.dc.StorageNetworkIpAddressVO;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapcityException;
import com.cloud.network.Network;
import com.cloud.network.NetworkProfile;
import com.cloud.network.NetworkVO;
import com.cloud.network.Networks.AddressFormat;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.Mode;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.StorageNetworkManager;
import com.cloud.offering.NetworkOffering;
import com.cloud.user.Account;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.component.Inject;
import com.cloud.utils.net.Ip4Address;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.Nic.ReservationStrategy;

@Local(value = NetworkGuru.class)
public class StorageNetworkGuru extends PodBasedNetworkGuru implements NetworkGuru {
	private static final Logger s_logger = Logger.getLogger(StorageNetworkGuru.class);
	@Inject StorageNetworkManager _sNwMgr;
	
	protected StorageNetworkGuru() {
		super();
	}
	
    private static final TrafficType[] _trafficTypes = {TrafficType.Storage};
    
    @Override
    public boolean isMyTrafficType(TrafficType type) {
    	for (TrafficType t : _trafficTypes) {
    		if (t == type) {
    			return true;
    		}
    	}
    	return false;
    }

    @Override
    public TrafficType[] getSupportedTrafficType() {
    	return _trafficTypes;
    }
    
	protected boolean canHandle(NetworkOffering offering) {
		if (isMyTrafficType(offering.getTrafficType()) && offering.isSystemOnly()) {
			return true;
		} else {
			s_logger.trace("It's not storage network offering, skip it.");
			return false;
		}
	}
	
	@Override
	public Network design(NetworkOffering offering, DeploymentPlan plan, Network userSpecified, Account owner) {
		if (!canHandle(offering)) {
			return null;
		}
				
		NetworkVO config = new NetworkVO(offering.getTrafficType(), Mode.Static, BroadcastDomainType.Native, offering.getId(), Network.State.Setup,
		        plan.getDataCenterId(), plan.getPhysicalNetworkId());
		return config;
	}

	@Override
	public Network implement(Network network, NetworkOffering offering, DeployDestination destination, ReservationContext context)
	        throws InsufficientVirtualNetworkCapcityException {
		assert network.getTrafficType() == TrafficType.Storage : "Why are you sending this configuration to me " + network;
		if (!_sNwMgr.isStorageIpRangeAvailable()) {
			return super.implement(network, offering, destination, context);
		}
		return network;
	}

	@Override
	public NicProfile allocate(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm)
	        throws InsufficientVirtualNetworkCapcityException, InsufficientAddressCapacityException {
		assert network.getTrafficType() == TrafficType.Storage : "Well, I can't take care of this config now can I? " + network; 
		if (!_sNwMgr.isStorageIpRangeAvailable()) {
			return super.allocate(network, nic, vm);
		}
		
		return new NicProfile(ReservationStrategy.Start, null, null, null, null);
	}

	@Override
	public void reserve(NicProfile nic, Network network, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest, ReservationContext context)
	        throws InsufficientVirtualNetworkCapcityException, InsufficientAddressCapacityException {
		if (!_sNwMgr.isStorageIpRangeAvailable()) {
			super.reserve(nic, network, vm, dest, context);
			return;
		}
		
		Pod pod = dest.getPod();
		Integer vlan = null;
		
		StorageNetworkIpAddressVO ip = _sNwMgr.acquireIpAddress(pod.getId());
		if (ip == null) {
			throw new InsufficientAddressCapacityException("Unable to get a storage network ip address", Pod.class, pod.getId());
		}
	
		vlan = ip.getVlan();	
		nic.setIp4Address(ip.getIpAddress());
		nic.setMacAddress(NetUtils.long2Mac(NetUtils.createSequenceBasedMacAddress(ip.getMac())));
		nic.setFormat(AddressFormat.Ip4);
		nic.setNetmask(ip.getNetmask());
		nic.setBroadcastType(BroadcastDomainType.Storage);
		nic.setGateway(ip.getGateway());
		if (vlan != null) {
			nic.setBroadcastUri(BroadcastDomainType.Storage.toUri(vlan));
		} else {
			nic.setBroadcastUri(null);
		}
        nic.setIsolationUri(null);
        s_logger.debug("Allocated a storage nic " + nic + " for " + vm);
	}

	@Override
	public boolean release(NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, String reservationId) {
		if (!_sNwMgr.isStorageIpRangeAvailable()) {
			return super.release(nic, vm, reservationId);
		}
		
		_sNwMgr.releaseIpAddress(nic.getIp4Address());
		s_logger.debug("Release an storage ip " + nic.getIp4Address());
		nic.deallocate();
		return true;
	}

	@Override
	public void deallocate(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateNicProfile(NicProfile profile, Network network) {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown(NetworkProfile network, NetworkOffering offering) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean trash(Network network, NetworkOffering offering, Account owner) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateNetworkProfile(NetworkProfile networkProfile) {
		// TODO Auto-generated method stub

	}

}
