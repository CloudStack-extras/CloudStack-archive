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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.api.commands.ConfigureVirtualRouterElementCmd;
import com.cloud.api.commands.ListVirtualRouterElementsCmd;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.dao.HostDao;
import com.cloud.network.HAProxy;
import com.cloud.network.LoadBalancerValidator;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkManager;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.HAProxyConfigurator;
import com.cloud.network.LoadBalancerConfigurator;
import com.cloud.network.PhysicalNetworkServiceProvider;
import com.cloud.network.PublicIpAddress;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.VirtualRouterProvider;
import com.cloud.network.VirtualRouterProvider.VirtualRouterProviderType;
import com.cloud.network.VpnUser;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.VirtualRouterProviderDao;
import com.cloud.network.lb.LoadBalancingRule;
import com.cloud.network.lb.LoadBalancingRulesManager;
import com.cloud.network.router.VirtualNetworkApplianceManager;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.rules.LbStickinessMethod;
import com.cloud.network.rules.LbStickinessMethod.StickinessMethodType;
import com.cloud.network.router.VirtualRouter.Role;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.PortForwardingRule;
import com.cloud.network.rules.RulesManager;
import com.cloud.network.rules.StaticNat;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.user.AccountManager;
import com.cloud.uservm.UserVm;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.SearchCriteria2;
import com.cloud.utils.db.SearchCriteriaService;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.UserVmManager;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.UserVmDao;
import com.google.gson.Gson;
import com.cloud.network.HAProxy.HAProxyValidator;
import com.cloud.network.LoadBalancerValidator;


@Local(value=NetworkElement.class)
public class VirtualRouterElement extends AdapterBase implements VirtualRouterElementService, DhcpServiceProvider, UserDataServiceProvider, SourceNatServiceProvider, StaticNatServiceProvider, FirewallServiceProvider, LoadBalancingServiceProvider, PortForwardingServiceProvider, RemoteAccessVPNServiceProvider {
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
    @Inject AccountManager _accountMgr;
    @Inject ConfigurationDao _configDao;
    @Inject VirtualRouterProviderDao _vrProviderDao;
    
    protected boolean canHandle(Network network, Service service) {
        if (!_networkMgr.isProviderEnabledInPhysicalNetwork(_networkMgr.getPhysicalNetworkId(network), "VirtualRouter")) {
            return false;
        }
        
        if (service == null) {
        	if (!_networkMgr.isProviderForNetwork(getProvider(), network.getId())) {
        		s_logger.trace("Element " + getProvider().getName() + " is not a provider for the network " + network);
        		return false;
        	}
        } else {
        	if (!_networkMgr.isProviderSupportServiceInNetwork(network.getId(), service, getProvider())) {
        		s_logger.trace("Element " + getProvider().getName() + " doesn't support service " + service.getName() + " in the network " + network);
                return false;
            }
        }
        
        return true;
    }

    @Override
    public boolean implement(Network network, NetworkOffering offering, DeployDestination dest, ReservationContext context) throws ResourceUnavailableException, ConcurrentOperationException, InsufficientCapacityException {
        if (offering.isSystemOnly()) {
            return false;
        }

        Map<VirtualMachineProfile.Param, Object> params = new HashMap<VirtualMachineProfile.Param, Object>(1);
        params.put(VirtualMachineProfile.Param.ReProgramNetwork, true);

        _routerMgr.deployVirtualRouter(network, dest, _accountMgr.getAccount(network.getAccountId()), params, offering.getRedundantRouter());

        return true;
    }
    
    
    @Override
    public boolean prepare(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest, ReservationContext context) throws ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException {
        if (!canHandle(network, null)) {
        	return false;
        }
    	
    	NetworkOfferingVO offering = _networkOfferingDao.findById(network.getNetworkOfferingId());
        if (offering.isSystemOnly()) {
            return false;
        }
        if (!_networkMgr.isProviderEnabledInPhysicalNetwork(_networkMgr.getPhysicalNetworkId(network), "VirtualRouter")) {
            return false;
        }
        
        if (vm.getType() != VirtualMachine.Type.User) {
            return false;
        }

        @SuppressWarnings("unchecked")
        VirtualMachineProfile<UserVm> uservm = (VirtualMachineProfile<UserVm>)vm;
        List<DomainRouterVO> routers = _routerMgr.deployVirtualRouter(network, dest, _accountMgr.getAccount(network.getAccountId()), uservm.getParameters(), offering.getRedundantRouter());
        if ((routers == null) || (routers.size() == 0)) {
            throw new ResourceUnavailableException("Can't find at least one running router!", this.getClass(), 0);
        }
        return true;
    }

    @Override
    public boolean applyFWRules(Network config, List<? extends FirewallRule> rules) throws ResourceUnavailableException {
        if (canHandle(config, Service.Firewall)) {
            List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(config.getId(), Role.VIRTUAL_ROUTER);
            if (routers == null || routers.isEmpty()) {
                s_logger.debug("Virtual router elemnt doesn't need to apply firewall rules on the backend; virtual router doesn't exist in the network " + config.getId());
                return true;
            }
            
            if(!_routerMgr.applyFirewallRules(config, rules, routers)){
                throw new CloudRuntimeException("Failed to apply firewall rules in network " + config.getId());
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    
    @Override
    public boolean validateLBRule(Network network, LoadBalancingRule rule) {
        if (canHandle(network, Service.Lb)) {
            List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
            if (routers == null || routers.isEmpty()) {
                return true;
            }
            LoadBalancerValidator validator = new HAProxyValidator();
            return validator.validateLBRule(rule);
        }
        return true;
    }
    
    @Override
    public boolean applyLBRules(Network network, List<LoadBalancingRule> rules) throws ResourceUnavailableException {
        if (canHandle(network, Service.Lb)) {
            List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
            if (routers == null || routers.isEmpty()) {
                s_logger.debug("Virtual router elemnt doesn't need to apply firewall rules on the backend; virtual router doesn't exist in the network " + network.getId());
                return true;
            }
            
            if(!_routerMgr.applyFirewallRules(network, rules, routers)){
                throw new CloudRuntimeException("Failed to apply firewall rules in network " + network.getId());
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    
    
    @Override
    public String[] applyVpnUsers(RemoteAccessVpn vpn, List<? extends VpnUser> users) throws ResourceUnavailableException{
        Network network = _networksDao.findById(vpn.getNetworkId());
        
        if (canHandle(network, Service.Vpn)) {
        	List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
            if (routers == null || routers.isEmpty()) {
                s_logger.debug("Virtual router elemnt doesn't need to apply vpn users on the backend; virtual router doesn't exist in the network " + network.getId());
                return null;
            }
            return _routerMgr.applyVpnUsers(network, users, routers);
        } else {
            s_logger.debug("Element " + this.getName() + " doesn't handle applyVpnUsers command");
            return null;
        }
    }
    
    @Override
    public boolean startVpn(Network network, RemoteAccessVpn vpn) throws ResourceUnavailableException {
        if (canHandle(network, Service.Vpn)) {
        	 List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
             if (routers == null || routers.isEmpty()) {
                 s_logger.debug("Virtual router elemnt doesn't need stop vpn on the backend; virtual router doesn't exist in the network " + network.getId());
                 return true;
             }
            return _routerMgr.startRemoteAccessVpn(network, vpn, routers);
        } else {
            s_logger.debug("Element " + this.getName() + " doesn't handle createVpn command");
            return false;
        }
    }
    
    @Override
    public boolean stopVpn(Network network, RemoteAccessVpn vpn) throws ResourceUnavailableException {
        if (canHandle(network, Service.Vpn)) {
        	List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
            if (routers == null || routers.isEmpty()) {
                s_logger.debug("Virtual router elemnt doesn't need stop vpn on the backend; virtual router doesn't exist in the network " + network.getId());
                return true;
            }
            return _routerMgr.deleteRemoteAccessVpn(network, vpn, routers);
        } else {
            s_logger.debug("Element " + this.getName() + " doesn't handle removeVpn command");
            return false;
        }
    }

    @Override
    public boolean applyIps(Network network, List<? extends PublicIpAddress> ipAddress) throws ResourceUnavailableException {
        if (canHandle(network, Service.Firewall)) {
            List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
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
        lbCapabilities.put(Capability.SupportedLBIsolation, "dedicated");
        lbCapabilities.put(Capability.SupportedProtocols, "tcp, udp");
        
        lbCapabilities.put(Capability.SupportedStickinessMethods,HAProxy.getStickinessCapability());
       
        capabilities.put(Service.Lb, lbCapabilities);
        
        //Set capabilities for Firewall service
        Map<Capability, String> firewallCapabilities = new HashMap<Capability, String>();
        firewallCapabilities.put(Capability.TrafficStatistics, "per public ip");
        firewallCapabilities.put(Capability.SupportedProtocols, "tcp,udp,icmp");
        firewallCapabilities.put(Capability.MultipleIps, "true");
        
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
        
        Map<Capability, String> sourceNatCapabilities = new HashMap<Capability, String>();
        sourceNatCapabilities.put(Capability.SupportedSourceNatTypes, "per account");
        sourceNatCapabilities.put(Capability.RedundantRouter, "true");
        capabilities.put(Service.SourceNat, sourceNatCapabilities);
        
        capabilities.put(Service.StaticNat, null);
        capabilities.put(Service.PortForwarding, null);
        
        return capabilities;
    }
    
    @Override
    public boolean applyStaticNats(Network config, List<? extends StaticNat> rules) throws ResourceUnavailableException {
        if (canHandle(config, Service.StaticNat)) {
            List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(config.getId(), Role.VIRTUAL_ROUTER);
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
    public boolean shutdown(Network network, ReservationContext context, boolean cleanup) throws ConcurrentOperationException, ResourceUnavailableException {
        List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
        if (routers == null || routers.isEmpty()) {
            return true;
        }
        boolean result = true;
        for (DomainRouterVO router : routers) {
            result = result && _routerMgr.stop(router, false, context.getCaller(), context.getAccount()) != null;
            if (cleanup) {
                if (!result) {
                    s_logger.warn("Failed to stop virtual router element " + router + ", but would try to process clean up anyway.");
                }
                result = (_routerMgr.destroyRouter(router.getId()) != null);
                if (!result) {
                    s_logger.warn("Failed to clean up virtual router element " + router);
                }
            }
        }
        return result;
    }
    
    @Override
    public boolean destroy(Network config) throws ConcurrentOperationException, ResourceUnavailableException{
        List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(config.getId(), Role.VIRTUAL_ROUTER);
        if (routers == null || routers.isEmpty()) {
            return true;
        }
        boolean result = true;
        for (DomainRouterVO router : routers) {
            result = result && (_routerMgr.destroyRouter(router.getId()) != null);
        }
        return result;
    }
    
    @Override
    public boolean savePassword(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm) throws ResourceUnavailableException{
    	if (!canHandle(network, null)) {
    		return false;
    	}
        List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
        if (routers == null || routers.isEmpty()) {
            s_logger.debug("Can't find virtual router element in network " + network.getId());
            return true;
        }
        
        @SuppressWarnings("unchecked")
        VirtualMachineProfile<UserVm> uservm = (VirtualMachineProfile<UserVm>)vm;
 
        return _routerMgr.savePasswordToRouter(network, nic, uservm, routers);
    }

    @Override
    public String getPropertiesFile() {
        return "virtualrouter_commands.properties";
    }
    
    @Override
    public VirtualRouterProvider configure(ConfigureVirtualRouterElementCmd cmd) {
        VirtualRouterProviderVO element = _vrProviderDao.findById(cmd.getId());
        if (element == null) {
            s_logger.debug("Can't find element with network service provider id " + cmd.getId());
            return null;
        }
        
        element.setEnabled(cmd.getEnabled());
        _vrProviderDao.persist(element);
        
        return element;
    }
    
    @Override
    public VirtualRouterProvider addElement(Long nspId) {
        VirtualRouterProviderVO element = _vrProviderDao.findByNspIdAndType(nspId, VirtualRouterProviderType.VirtualRouter);
        if (element != null) {
            s_logger.debug("There is already a virtual router element with service provider id " + nspId);
            return null;
        }
        element = new VirtualRouterProviderVO(nspId, VirtualRouterProviderType.VirtualRouter);
        _vrProviderDao.persist(element);
        return element;
    }

    @Override
    public boolean applyPFRules(Network network, List<PortForwardingRule> rules) throws ResourceUnavailableException {
        if (canHandle(network, Service.PortForwarding)) {
            List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
            if (routers == null || routers.isEmpty()) {
                s_logger.debug("Virtual router elemnt doesn't need to apply firewall rules on the backend; virtual router doesn't exist in the network " + network.getId());
                return true;
            }
            
            if(!_routerMgr.applyFirewallRules(network, rules, routers)){
                throw new CloudRuntimeException("Failed to apply firewall rules in network " + network.getId());
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    
    @Override
    public boolean isReady(PhysicalNetworkServiceProvider provider) {
        VirtualRouterProviderVO element = _vrProviderDao.findByNspIdAndType(provider.getId(), VirtualRouterProviderType.VirtualRouter);
        if (element == null) {
            return false;
        }
        return element.isEnabled();
    }

    @Override
    public boolean shutdownProviderInstances(PhysicalNetworkServiceProvider provider, ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException {
        VirtualRouterProviderVO element = _vrProviderDao.findByNspIdAndType(provider.getId(), VirtualRouterProviderType.VirtualRouter);
        if (element == null) {
            return true;
        }
        //Find domain routers
        long elementId = element.getId();
        List<DomainRouterVO> routers = _routerDao.listByElementId(elementId);
        boolean result = true;
        for (DomainRouterVO router : routers) {
            result = result && (_routerMgr.destroyRouter(router.getId()) != null);
        }
        return result;
    }
    
    @Override
    public boolean canEnableIndividualServices() {
        return true;
    }    

    public Long getIdByNspId(Long nspId) {
        VirtualRouterProviderVO vr = _vrProviderDao.findByNspIdAndType(nspId, VirtualRouterProviderType.VirtualRouter);
        return vr.getId();
    }

    @Override
    public VirtualRouterProvider getCreatedElement(long id) {
        return _vrProviderDao.findById(id);
    }

    @Override
    public boolean release(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException {
        return true;
    }

    @Override
    public boolean addDhcpEntry(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest, ReservationContext context)
            throws ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException {
        if (canHandle(network, Service.Dhcp)) {
            if (vm.getType() != VirtualMachine.Type.User) {
                return false;
            }

            @SuppressWarnings("unchecked")
            VirtualMachineProfile<UserVm> uservm = (VirtualMachineProfile<UserVm>)vm;

            boolean publicNetwork = false;
            if (_networkMgr.isProviderSupportServiceInNetwork(network.getId(), Service.SourceNat, getProvider())) {
                publicNetwork = true;
            }
            boolean isPodBased = (dest.getDataCenter().getNetworkType() == NetworkType.Basic || _networkMgr.isSecurityGroupSupportedInNetwork(network)) &&
                                network.getTrafficType() == TrafficType.Guest;
            
            List<DomainRouterVO> routers;
            
            if (publicNetwork) {
                routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
            } else {
                Long podId = dest.getPod().getId();
                if (isPodBased) {
                    routers = _routerDao.listByNetworkAndPodAndRole(network.getId(), podId, Role.VIRTUAL_ROUTER);
                } else {
                    routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
                }
            }
        
            //for Basic zone, add all Running routers - we have to send Dhcp/vmData/password info to them when network.dns.basiczone.updates is set to "all"
            Long podId = dest.getPod().getId();
            if (isPodBased && _routerMgr.getDnsBasicZoneUpdate().equalsIgnoreCase("all")) {
                List<DomainRouterVO> allRunningRoutersOutsideThePod = _routerDao.findByNetworkOutsideThePod(network.getId(), podId, State.Running, Role.VIRTUAL_ROUTER);
                routers.addAll(allRunningRoutersOutsideThePod);
            }

            if ((routers == null) || (routers.size() == 0)) {
                throw new ResourceUnavailableException("Can't find at least one router!", this.getClass(), 0);
            }
            
            List<VirtualRouter> rets = _routerMgr.applyDhcpEntry(network, nic, uservm, dest, context, routers);
            return (rets != null) && (!rets.isEmpty());
        }
        return false;
    }

    @Override
    public boolean addPasswordAndUserdata(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest, ReservationContext context)
            throws ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException {
        if (canHandle(network, Service.UserData)) {
            if (vm.getType() != VirtualMachine.Type.User) {
                return false;
            }

            @SuppressWarnings("unchecked")
            VirtualMachineProfile<UserVm> uservm = (VirtualMachineProfile<UserVm>)vm;

            boolean publicNetwork = false;
            if (_networkMgr.isProviderSupportServiceInNetwork(network.getId(), Service.SourceNat, getProvider())) {
                publicNetwork = true;
            }
            boolean isPodBased = (dest.getDataCenter().getNetworkType() == NetworkType.Basic || _networkMgr.isSecurityGroupSupportedInNetwork(network)) &&
                                network.getTrafficType() == TrafficType.Guest;
            
            List<DomainRouterVO> routers;
            
            if (publicNetwork) {
                routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
            } else {
                Long podId = dest.getPod().getId();
                if (isPodBased) {
                    routers = _routerDao.listByNetworkAndPodAndRole(network.getId(), podId, Role.VIRTUAL_ROUTER);
                } else {
                    routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
                }
            }
        
            //for Basic zone, add all Running routers - we have to send Dhcp/vmData/password info to them when network.dns.basiczone.updates is set to "all"
            Long podId = dest.getPod().getId();
            if (isPodBased && _routerMgr.getDnsBasicZoneUpdate().equalsIgnoreCase("all")) {
                List<DomainRouterVO> allRunningRoutersOutsideThePod = _routerDao.findByNetworkOutsideThePod(network.getId(), podId, State.Running, Role.VIRTUAL_ROUTER);
                routers.addAll(allRunningRoutersOutsideThePod);
            }

            if ((routers == null) || (routers.size() == 0)) {
                throw new ResourceUnavailableException("Can't find at least one router!", this.getClass(), 0);
            }
            
            List<VirtualRouter> rets = _routerMgr.applyUserData(network, nic, uservm, dest, context, routers);
            return (rets != null) && (!rets.isEmpty());
        }
        return false;
    }

    @Override
    public List<? extends VirtualRouterProvider> searchForVirtualRouterElement(ListVirtualRouterElementsCmd cmd) {
        Long id = cmd.getId();
        Long nspId = cmd.getNspId();
        Boolean enabled = cmd.getEnabled();
        
        SearchCriteriaService<VirtualRouterProviderVO, VirtualRouterProviderVO> sc = SearchCriteria2.create(VirtualRouterProviderVO.class);
        if (id != null) {
            sc.addAnd(sc.getEntity().getId(), Op.EQ, id);
        }
        if (nspId != null) {
            sc.addAnd(sc.getEntity().getNspId(), Op.EQ, nspId);
        }
        if (enabled != null) {
            sc.addAnd(sc.getEntity().isEnabled(), Op.EQ, enabled);
        }
        return sc.list();
    }

    @Override
    public boolean verifyServicesCombination(List<String> services) {
        if (!services.contains("SourceNat")) {
            if (services.contains("StaticNat") || services.contains("Firewall") || services.contains("Lb") || services.contains("PortForwarding") ||
                    services.contains("Vpn")) {
                s_logger.warn("Virtual router can't enable services " + services + " without source NAT service");
                return false;
            }
        }
        return true;
    }
}
