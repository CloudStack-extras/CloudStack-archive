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
package com.cloud.network.element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.configuration.ConfigurationManager;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IPAddressVO;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.GuestIpType;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkManager;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.PublicIpAddress;
import com.cloud.network.IpAddress;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.router.VirtualNetworkApplianceManager;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.router.VirtualRouter.Role;
import com.cloud.network.rules.FirewallRule;
import com.cloud.offering.NetworkOffering;
import com.cloud.uservm.UserVm;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.component.Inject;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.NicProfile;
import com.cloud.vm.NicVO;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.UserVmManager;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;


@Local(value=NetworkElement.class)
public class ElasticIpElement extends AdapterBase implements NetworkElement{
    private static final Logger s_logger = Logger.getLogger(ElasticIpElement.class);
    
    private static final Map<Service, Map<Capability, String>> capabilities = setCapabilities();
    
    @Inject NetworkDao _networkConfigDao;
    @Inject NetworkManager _networkMgr;
    @Inject VirtualNetworkApplianceManager _routerMgr;
    @Inject UserVmManager _userVmMgr;
    @Inject UserVmDao _userVmDao;
    @Inject DomainRouterDao _routerDao;
    @Inject ConfigurationManager _configMgr;
    @Inject HostPodDao _podDao;
    @Inject NicDao _nicDao;
    @Inject IPAddressDao _ipAddressDao;
     
    private boolean canHandle(GuestIpType ipType, DeployDestination dest, TrafficType trafficType) {
        DataCenter dc = dest.getDataCenter();
        if (dc.getNetworkType() == NetworkType.Basic) {
            return (ipType == GuestIpType.Direct && trafficType == TrafficType.Guest);
        } 
        return false;
    }

    @Override
    public boolean implement(Network network, NetworkOffering offering, DeployDestination dest, ReservationContext context) throws ResourceUnavailableException, ConcurrentOperationException, InsufficientCapacityException {
        s_logger.debug("In ElasticIpElement.implement");
        if (!canHandle(network.getGuestType(), dest, offering.getTrafficType())) {
            s_logger.debug("ElasticIpElement.implement: cannot handle guest " + network.getGuestType() + ", traffic " + offering.getTrafficType());
            return false;
        }
        
        Map<VirtualMachineProfile.Param, Object> params = new HashMap<VirtualMachineProfile.Param, Object>(1);
        params.put(VirtualMachineProfile.Param.RestartNetwork, true);
        s_logger.debug("Asking router manager to deploy elastic ip vm if necessary");
        VirtualRouter eipVm = _routerMgr.deployElasticIpVm(network, dest, context.getAccount(), params);
        s_logger.debug("Elastic ip vm = " + eipVm);
        return true;
    }

    @Override
    public boolean prepare(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest, ReservationContext context) throws ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException {
        if (canHandle(network.getGuestType(), dest, network.getTrafficType())) {
            
            if (vm.getType() != VirtualMachine.Type.User) {
                return false;
            }
            
            @SuppressWarnings("unchecked")
            VirtualMachineProfile<UserVm> uservm = (VirtualMachineProfile<UserVm>)vm;
            Map<VirtualMachineProfile.Param, Object> params = new HashMap<VirtualMachineProfile.Param, Object>(1);
            params.put(VirtualMachineProfile.Param.RestartNetwork, true);
            s_logger.debug("Asking router manager to deploy elastic ip vm if necessary");
            //What happens really is that the DhcpElement gets the prepare request first and calls 
            //addVirtualMachineIntoNetwork on the VirtualNetworkApplianceManager. That routine needs to know the 
            //elastic ip vm's guest ip so that the dhcp server can tell the user vm its default route goes through
            //the elastic ip vm. 
            VirtualRouter eipVm = _routerMgr.deployElasticIpVm(network, dest, context.getAccount(), params);
            s_logger.debug("Elastic ip vm = " + eipVm);
            if (eipVm != null) {
                nic.setElasticIpVmId(eipVm.getId());
            }
            return true;
        } else {
            s_logger.debug("ElasticIpElement.prepare: cannot handle guest " + network.getGuestType() + ", traffic " + network.getTrafficType());
            return false;
        }
    }

    @Override
    public boolean release(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, ReservationContext context)
                throws ConcurrentOperationException, ResourceUnavailableException {
        if (vm.getType() != VirtualMachine.Type.User) {
            return true;
        }
        //We get here when the user vm is stopping or stopped. We need to release the association of public -> guest
        if (nic.getElasticIpVmId() == null) {
            s_logger.info("Hmmm.. we got to releasing a nic, but there isn't a elastic ip vm to found");
            return true;
        }
        DomainRouterVO elasticIpVm = _routerDao.findById(nic.getElasticIpVmId());
        IPAddressVO publicIp = _ipAddressDao.findByAssociatedVmId(vm.getId());
        if (publicIp == null) {
            s_logger.info("Hmmm.. we got to releasing a nic, but there isn't a public ip to found");
            return true;
        }
        _routerMgr.associateElasticIp(elasticIpVm, publicIp.getId(), nic.getIp4Address(), false, null);
        return true;
    }
    
    @Override
    public boolean shutdown(Network network, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        DomainRouterVO router = _routerDao.findByNetwork(network.getId());
        if (router == null) {
            return true;
        }
        return (_routerMgr.stop(router, false, context.getCaller(), context.getAccount()) != null);
    }
    
    @Override
    public boolean destroy(Network config) throws ConcurrentOperationException, ResourceUnavailableException{
        DomainRouterVO router = _routerDao.findByNetwork(config.getId());
        if (router == null) {
            return true;
        }
        return _routerMgr.destroyRouter(router.getId());
    }

    @Override
    public boolean applyRules(Network network, List<? extends FirewallRule> rules) throws ResourceUnavailableException {
        return false;
    }
    
    private DomainRouterVO findElasticIpVmForUserVm(long networkId, UserVmVO userVm) {
       //FIXME: do something more sophisticated here.
       return _routerDao.findByNetworkAndPodAndRole(networkId, userVm.getPodId(), Role.FIREWALL);
    }

    @Override
    public boolean applyIps(Network network, List<? extends PublicIpAddress> ipAddressList) throws ResourceUnavailableException {
        if (network.getGuestType() != GuestIpType.Direct)
            return false;
        boolean result = true;
        //this is a little it convoluted since it can get here in several different contexts
        //context #1: associate public ip with a vm. In this case we need to release the old public ip
        //            if the old ip was system owned then it needs to get unassigned (return to the free pool). 
        //            if the vm is not running, then disallow it.
        //context #2: disassociate public ip with a vm. In this case a new public ip has been allocated for the vm and is
        //            passed in the ipAddressList. We have to disassociate the old public ip address. If the old ip was
        //            system owned then it needs to get unassigned (returned to the free pool)
        //context #3: release an elastic ip. this is like context #2.
        //context #4: restart network or restart elastic ip vm. In this case there is no old ip to disassociate
        for (PublicIpAddress publicIp: ipAddressList){
            Long vmId = publicIp.getAssociatedWithVmId();
            if (vmId == null) {
                continue;
            }
             UserVmVO vm = _userVmDao.findById(vmId);
             NicVO nic = _nicDao.findByInstanceIdAndNetworkId(network.getId(), vmId);
             if (vm.getState() != VirtualMachine.State.Running) {
                 throw new ResourceUnavailableException("Instance (vm) is not in a state that supports this operation", UserVm.class, vmId);
             }
             if (nic.getIp4Address() == null) {
                 throw new ResourceUnavailableException("Instance (vm) does not have a guest ip address", UserVm.class, vmId);
             }
             DomainRouterVO elasticIpVm = findElasticIpVmForUserVm(network.getId(), vm);
             List<IPAddressVO> assocIps = _ipAddressDao.findAllByAssociatedVmId(publicIp.getAssociatedWithVmId());
             Long oldId = null;
             for (IPAddressVO ip: assocIps) {
                 if (ip.getId()  != publicIp.getId()) {
                     oldId = ip.getId();
                     break;
                 }
             }
             boolean release = (publicIp.getState() == IpAddress.State.Releasing || publicIp.getState() == IpAddress.State.Free);
             result = result && _routerMgr.associateElasticIp(elasticIpVm, publicIp.getId(), nic.getIp4Address(), !release, oldId);
             s_logger.debug(release?"Disa":"A" + "ssociate elastic ip : " + publicIp.getAddress() + " to " + nic.getIp4Address() + "result=" + result);
        }
        return result;
    }
    
    
    @Override
    public Provider getProvider() {
        return Provider.ElasticIpVm;
    }
    
    @Override
    public Map<Service, Map<Capability, String>> getCapabilities() {
        return capabilities;
    }
    
    private static Map<Service, Map<Capability, String>> setCapabilities() {
        Map<Service, Map<Capability, String>> capabilities = new HashMap<Service, Map<Capability, String>>();
        
        Map<Capability, String> firewallCapabilities = new HashMap<Capability, String>();
        firewallCapabilities.put(Capability.PortForwarding, "false");
        firewallCapabilities.put(Capability.TrafficStatistics, "per public ip");
        firewallCapabilities.put(Capability.StaticNat, "true");
        firewallCapabilities.put(Capability.PortFiltering, "none");
        
        capabilities.put(Service.Firewall, firewallCapabilities);   
        return capabilities;
    }
    
    @Override
    public boolean restart(Network network, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException{
        DataCenter dc = _configMgr.getZone(network.getDataCenterId());
        NetworkOffering offering = _configMgr.getNetworkOffering(network.getNetworkOfferingId());
        DeployDestination dest = new DeployDestination(dc, null, null, null);
        DomainRouterVO router = _routerDao.findByNetwork(network.getId());
        if (router == null) {
            s_logger.trace("Can't find elastic ip vm element in network " + network.getId());
            return true;
        }
        
        VirtualRouter result = null;
        if (canHandle(network.getGuestType(), dest, offering.getTrafficType())) {
            if (router.getState() == State.Stopped) {
                result = _routerMgr.startRouter(router.getId(), false);
            } else {
                result = _routerMgr.rebootRouter(router.getId(), false);
            }
            if (result == null) {
                s_logger.warn("Failed to restart elastic ip vm element " + router + " as a part of netowrk " + network + " restart");
                return false;
            } else {
                return true;
            }
        } else {
            s_logger.trace("Elastic ip vm element doesn't handle network restart for the network " + network);
            return true;
        }
    }

}
