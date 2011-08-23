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
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.DataCenter;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.dao.HostDao;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.GuestIpType;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkManager;
import com.cloud.network.PublicIpAddress;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.VpnUser;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.lb.LoadBalancingRulesManager;
import com.cloud.network.router.VirtualNetworkApplianceManager;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.router.VirtualRouter.Role;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.RulesManager;
import com.cloud.network.rules.StaticNat;
import com.cloud.network.vpn.RemoteAccessVpnElement;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.org.Cluster;
import com.cloud.uservm.UserVm;
import com.cloud.utils.component.Inject;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.UserVmManager;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.UserVmDao;


@Local(value=NetworkElement.class)
public class VirtualRouterElement extends DhcpElement implements NetworkElement, RemoteAccessVpnElement {
    private static final Logger s_logger = Logger.getLogger(VirtualRouterElement.class);
    
    private static final Map<Service, Map<Capability, String>> capabilities = setCapabilities();
    
    @Inject NetworkDao _networksDao;
    @Inject NetworkManager _networkMgr;
    @Inject LoadBalancingRulesManager _lbMgr;
    @Inject NetworkOfferingDao _networkOfferingDao;
    @Inject VirtualNetworkApplianceManager _routerMgr;
    @Inject ConfigurationManager _configMgr;
    @Inject RulesManager _rulesMgr;
    @Inject UserVmManager _userVmMgr;
    @Inject UserVmDao _userVmDao;
    @Inject DomainRouterDao _routerDao;
    @Inject LoadBalancerDao _lbDao;
    @Inject HostDao _hostDao;
    @Inject ConfigurationDao _configDao;
    
    private boolean canHandle(GuestIpType ipType, DataCenter dc) {
        String provider = dc.getGatewayProvider();
        boolean result = (provider != null && ipType == GuestIpType.Virtual && provider.equals(Provider.VirtualRouter.getName()));
        if (!result) {
            s_logger.trace("Virtual router element only takes care of guest ip type " + GuestIpType.Virtual + " for provider " + Provider.VirtualRouter.getName());
        }
        return result;
    }

    @Override
    public boolean implement(Network guestConfig, NetworkOffering offering, DeployDestination dest, ReservationContext context) throws ResourceUnavailableException, ConcurrentOperationException, InsufficientCapacityException {
        boolean isRedundant = _configDao.getValue("network.redundantrouter").equals("true");
        if (!canHandle(guestConfig.getGuestType(), dest.getDataCenter())) {
            return false;
        }
        
        Map<VirtualMachineProfile.Param, Object> params = new HashMap<VirtualMachineProfile.Param, Object>(1);
        params.put(VirtualMachineProfile.Param.RestartNetwork, true);

        _routerMgr.deployVirtualRouter(guestConfig, dest, _accountMgr.getAccount(guestConfig.getAccountId()), params, isRedundant);
        
        return true;
    }
    
    
    @Override
    public boolean prepare(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest, ReservationContext context) throws ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException {
        if (canHandle(network.getGuestType(), dest.getDataCenter())) {
            boolean isRedundant = _configDao.getValue("network.redundantrouter").equals("true");
            if (vm.getType() != VirtualMachine.Type.User) {
                return false;
            }
            
            @SuppressWarnings("unchecked")
            VirtualMachineProfile<UserVm> uservm = (VirtualMachineProfile<UserVm>)vm;
            List<DomainRouterVO> routers = _routerMgr.deployVirtualRouter(network, dest, _accountMgr.getAccount(network.getAccountId()), uservm.getParameters(), isRedundant);
            if ((routers == null) || (routers.size() == 0)) {
                throw new ResourceUnavailableException("Can't find at least one running router!", this.getClass(), 0);
            }
            List<VirtualRouter> rets = _routerMgr.addVirtualMachineIntoNetwork(network, nic, uservm, dest, context, routers);                                                                                                                      
            return (rets != null) && (!rets.isEmpty());
        } else {
            return false;
        }
    }
    
    @Override
    public boolean restart(Network network, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException{
        DataCenter dc = _configMgr.getZone(network.getDataCenterId());
        if (!canHandle(network.getGuestType(), dc)) {
            s_logger.trace("Virtual router element doesn't handle network restart for the network " + network);
            return false;
        }

        DeployDestination dest = new DeployDestination(dc, null, null, null);

        NetworkOffering networkOffering = _networkOfferingDao.findById(network.getNetworkOfferingId());
        
        // We need to re-implement the network since the redundancy capability may changed
        List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.DHCP_FIREWALL_LB_PASSWD_USERDATA);
        if (routers == null || routers.isEmpty()) {
            s_logger.trace("Can't find virtual router element in network " + network.getId());
            return true;
        }

        /* Get the host_id in order to find the cluster */
        Long host_id = new Long(0);
        for (DomainRouterVO router : routers) {
            if (host_id == null || host_id == 0) {
                host_id = (router.getHostId() != null ? router.getHostId() : router.getLastHostId());
            }
            /* FIXME it's not completely safe to ignore these failure, but we would try to push on now */
            if (router.getState() != State.Stopped || _routerMgr.stopRouter(router.getId(), false) == null) {
                s_logger.warn("Failed to stop virtual router element " + router + " as a part of network " + network + " restart");
            }
            if (!_routerMgr.destroyRouter(router.getId())) {
                s_logger.warn("Failed to destroy virtual router element " + router + " as a part of network " + network + " restart");
            }
        }
        if (host_id == null || host_id == 0) {
            throw new ResourceUnavailableException("Fail to locate virtual router element in network " + network.getId(), this.getClass(), 0);
        }
        
        /* The cluster here is only used to determine hypervisor type, not the real deployment */
        Cluster cluster = _configMgr.getCluster(_hostDao.findById(host_id).getClusterId());
        dest = new DeployDestination(dc, null, cluster, null);
        return implement(network, networkOffering, dest, context);
    }

    @Override
    public boolean applyRules(Network config, List<? extends FirewallRule> rules) throws ResourceUnavailableException {
    
        DataCenter dc = _configMgr.getZone(config.getDataCenterId());
        if (canHandle(config.getGuestType(),dc)) {
            List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(config.getId(), Role.DHCP_FIREWALL_LB_PASSWD_USERDATA);
            if (routers == null || routers.isEmpty()) {
                s_logger.debug("Virtual router elemnt doesn't need to apply firewall rules on the backend; virtual router doesn't exist in the network " + config.getId());
                return true;
            }
            
            return _routerMgr.applyFirewallRules(config, rules, routers);
        } else {
            return true;
        }
    }
    
    
    @Override
    public String[] applyVpnUsers(RemoteAccessVpn vpn, List<? extends VpnUser> users) throws ResourceUnavailableException{
        Network network = _networksDao.findById(vpn.getNetworkId());
        DataCenter dc = _configMgr.getZone(network.getDataCenterId());
        
        List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.DHCP_FIREWALL_LB_PASSWD_USERDATA);
        if (routers == null || routers.isEmpty()) {
            s_logger.debug("Virtual router elemnt doesn't need to apply vpn users on the backend; virtual router doesn't exist in the network " + network.getId());
            return null;
        }
        
        if (canHandle(network.getGuestType(),dc)) {
            return _routerMgr.applyVpnUsers(network, users, routers);
        } else {
            s_logger.debug("Element " + this.getName() + " doesn't handle applyVpnUsers command");
            return null;
        }
    }
    
    @Override
    public boolean startVpn(Network network, RemoteAccessVpn vpn) throws ResourceUnavailableException {
        DataCenter dc = _configMgr.getZone(network.getDataCenterId());
        
        List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.DHCP_FIREWALL_LB_PASSWD_USERDATA);
        if (routers == null || routers.isEmpty()) {
            s_logger.debug("Virtual router elemnt doesn't need stop vpn on the backend; virtual router doesn't exist in the network " + network.getId());
            return true;
        }
        
        if (canHandle(network.getGuestType(),dc)) {
            return _routerMgr.startRemoteAccessVpn(network, vpn, routers);
        } else {
            s_logger.debug("Element " + this.getName() + " doesn't handle createVpn command");
            return false;
        }
    }
    
    @Override
    public boolean stopVpn(Network network, RemoteAccessVpn vpn) throws ResourceUnavailableException {
        DataCenter dc = _configMgr.getZone(network.getDataCenterId());
        
        List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.DHCP_FIREWALL_LB_PASSWD_USERDATA);
        if (routers == null || routers.isEmpty()) {
            s_logger.debug("Virtual router elemnt doesn't need stop vpn on the backend; virtual router doesn't exist in the network " + network.getId());
            return true;
        }
        
        if (canHandle(network.getGuestType(),dc)) {
            return _routerMgr.deleteRemoteAccessVpn(network, vpn, routers);
        } else {
            s_logger.debug("Element " + this.getName() + " doesn't handle removeVpn command");
            return false;
        }
    }

    @Override
    public boolean applyIps(Network network, List<? extends PublicIpAddress> ipAddress) throws ResourceUnavailableException {
        DataCenter dc = _configMgr.getZone(network.getDataCenterId());
        if (canHandle(network.getGuestType(),dc)) {
            
            List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.DHCP_FIREWALL_LB_PASSWD_USERDATA);
            if (routers == null || routers.isEmpty()) {
                s_logger.debug("Virtual router elemnt doesn't need to associate ip addresses on the backend; virtual router doesn't exist in the network " + network.getId());
                return true;
            }
            
            return _routerMgr.associateIP(network, ipAddress, routers);
        } else {
            return false;
        }
    }

    @Override
    public Provider getProvider() {
        return Provider.VirtualRouter;
    }
    
    @Override
    public Map<Service, Map<Capability, String>> getCapabilities() {
        return capabilities;
    }
    
    private static Map<Service, Map<Capability, String>> setCapabilities() {
        Map<Service, Map<Capability, String>> capabilities = new HashMap<Service, Map<Capability, String>>();
        
        //Set capabilities for LB service
        Map<Capability, String> lbCapabilities = new HashMap<Capability, String>();
        lbCapabilities.put(Capability.SupportedLBAlgorithms, "roundrobin,leastconn,source");
        lbCapabilities.put(Capability.SupportedProtocols, "tcp, udp");
        
        capabilities.put(Service.Lb, lbCapabilities);
        
        //Set capabilities for Firewall service
        Map<Capability, String> firewallCapabilities = new HashMap<Capability, String>();
        firewallCapabilities.put(Capability.PortForwarding, "true");
        firewallCapabilities.put(Capability.TrafficStatistics, "per public ip");
        firewallCapabilities.put(Capability.StaticNat, "true");
        firewallCapabilities.put(Capability.SupportedProtocols, "tcp,udp,icmp");
        firewallCapabilities.put(Capability.MultipleIps, "true");
        firewallCapabilities.put(Capability.SupportedSourceNatTypes, "per account");
        
        capabilities.put(Service.Firewall, firewallCapabilities);
        
        //Set capabilities for vpn
        Map<Capability, String> vpnCapabilities = new HashMap<Capability, String>();
        vpnCapabilities.put(Capability.SupportedVpnTypes, "pptp,l2tp,ipsec");
        capabilities.put(Service.Vpn, vpnCapabilities);
        
        Map<Capability, String> dnsCapabilities = new HashMap<Capability, String>();
        dnsCapabilities.put(Capability.AllowDnsSuffixModification, "true");
        capabilities.put(Service.Dns, dnsCapabilities);
        
        capabilities.put(Service.UserData, null);
        capabilities.put(Service.Dhcp, null);
        capabilities.put(Service.Gateway, null);
        
        return capabilities;
    }
    
    @Override
    public boolean applyStaticNats(Network config, List<? extends StaticNat> rules) throws ResourceUnavailableException {
        DataCenter dc = _configMgr.getZone(config.getDataCenterId());
        if (canHandle(config.getGuestType(),dc)) {
            List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(config.getId(), Role.DHCP_FIREWALL_LB_PASSWD_USERDATA);
            if (routers == null || routers.isEmpty()) {
                s_logger.debug("Virtual router elemnt doesn't need to apply static nat on the backend; virtual router doesn't exist in the network " + config.getId());
                return true;
            }
            
            return _routerMgr.applyStaticNats(config, rules, routers);
        } else {
            return true;
        }
    }
    
    @Override
    public boolean shutdown(Network network, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.DHCP_FIREWALL_LB_PASSWD_USERDATA);
        if (routers == null || routers.isEmpty()) {
            return true;
        }
        boolean result = true;
        for (DomainRouterVO router : routers) {
            result = result && _routerMgr.stop(router, false, context.getCaller(), context.getAccount()) != null;
        }
        return result;
    }
    
    @Override
    public boolean destroy(Network config) throws ConcurrentOperationException, ResourceUnavailableException{
        List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(config.getId(), Role.DHCP_FIREWALL_LB_PASSWD_USERDATA);
        if (routers == null || routers.isEmpty()) {
            return true;
        }
        boolean result = true;
        for (DomainRouterVO router : routers) {
            result = result && _routerMgr.destroyRouter(router.getId());
        }
        return result;
    }
    
    @Override
    public boolean savePassword(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm) throws ResourceUnavailableException{

        List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.DHCP_FIREWALL_LB_PASSWD_USERDATA);
        if (routers == null || routers.isEmpty()) {
            s_logger.trace("Can't find dhcp element in network " + network.getId());
            return true;
        }
        
        @SuppressWarnings("unchecked")
        VirtualMachineProfile<UserVm> uservm = (VirtualMachineProfile<UserVm>)vm;
 
        return _routerMgr.savePasswordToRouter(network, nic, uservm, routers);
    }
}
