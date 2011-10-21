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
import com.cloud.network.rules.LbStickinessMethod;
import com.cloud.network.rules.LbStickinessMethod.StickinessMethodType;
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

        _routerMgr.deployVirtualRouter(guestConfig, dest, _accountMgr.getAccount(guestConfig.getAccountId()), params, offering.getRedundantRouter());

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
    public boolean restart(Network network, ReservationContext context, boolean cleanup) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException{
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
            if (cleanup) {
                /* FIXME it's not completely safe to ignore these failure, but we would try to push on now */
                if (router.getState() != State.Stopped && _routerMgr.stopRouter(router.getId(), false) == null) {
                    s_logger.warn("Failed to stop virtual router element " + router + " as a part of network " + network + " restart");
                }
                if (_routerMgr.destroyRouter(router.getId()) == null) {
                    s_logger.warn("Failed to destroy virtual router element " + router + " as a part of network " + network + " restart");
                }
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
        
        LbStickinessMethod method;
        List <LbStickinessMethod> methodList = new ArrayList<LbStickinessMethod>(1);
        method = new LbStickinessMethod(StickinessMethodType.LBCookieBased,"This is loadbalancer cookie based sticky method, it can be used only for http",true); 
        method.addParam("name", true,  "cookie name passed in http header by the LB to the client");
        method.addParam("mode", false,  "valid value : insert ,rewrite/prefix . default value: insert,  In the insert mode cookie will be created by the LB . in other mode cookie will be created by the server and LB modifies it.");     
        method.addParam("nocache", false, "This option is recommended in conjunction with the insert mode when there is a cache between the client and HAProxy, as it ensures that a cacheable response will be tagged non-cacheable if  a cookie needs to be inserted. This is important because if all persistence cookies are added on a cacheable home page for instance, then all customers will then fetch the page from an outer cache and will all share the same persistence cookie, leading to one server receiving much more traffic than others. See also the insert and postonly options. "); 
        method.addParam("indirect", false, "When this option is specified, no cookie will be emitted to a client which already has a valid one for the server which has processed the request. If the server sets such a cookie itself, it will be removed, unless the -preserve- option is also set. In insert mode, this will additionally remove cookies from the requests transmitted to the server, making the persistence mechanism totally transparent from an application point of view.");    
        method.addParam("postonly",false,"This option ensures that cookie insertion will only be performed on responses to POST requests. It is an alternative to the nocache option, because POST responses are not cacheable, so this ensures that the persistence cookie will never get cached.Since most sites do not need any sort of persistence before the first POST which generally is a login request, this is a very efficient method to optimize caching without risking to find a persistence cookie in the cache. See also the insert and nocache options.");
        method.addParam("preserve",false,"  This option may only be used with insert and/or indirect. It allows the server to emit the persistence cookie itself. In this case, if a cookie is found in the response, haproxy will leave it untouched. This is useful in order to end persistence after a logout request for instance. For this, the server just has to emit a cookie with an invalid value (eg: empty) or with a date in the past. By combining this mechanism with the disable-on-404 check option, it is possible to perform a completely gracefulshutdown because users will definitely leave the server afterthey logout."); 
        method.addParam("maxlife",false,"This option allows inserted cookies to be ignored after some life time, whether they're in use or not. It only works with insert mode cookies. When a cookie is first sent to the client, the date this cookie was emitted is sent too. Upon further presentations of this cookie, if the date is older than the delay indicated by the parameter (in seconds), it will be ignored. If the cookie in the request has no date, it is accepted and a date will be set. Cookies that have a date in the future further than 24 hours are ignored. Doing so lets admins fix timezone issues without risking kicking users off the site. Contrary to maxidle, this value is not refreshed, only the first visit date counts. Both maxidle and maxlife may be used at the time. This is particularly useful to prevent users who never close their browsers from remaining for too long on the same server (eg: after a farm size change). This is stronger than the maxidle method in that it forces aredispatch after some absolute delay.");
        method.addParam("maxidle",false,"This option allows inserted cookies to be ignored after some idle time. It only works with insert-mode cookies. When a cookie is sent to the client, the date this cookie was emitted is sent too. Upon further presentations of this cookie, if the date is older than the delay indicated by the parameter (in seconds), it will be ignored. Otherwise, it will be refreshed if needed when the response is sent to the client. This is particularly useful to prevent users who never close their browsers from remaining for too long on the same server (eg: after a farm size change). When this option is set and a cookie has no date, it is always accepted, but gets refreshed in the response. This maintains the ability for admins to access their sites. Cookies that have a date in the future further than 24 hours are ignored. Doing so lets admins fix timezone issues without risking kicking users off the site.");
        method.addParam("domain",false,"This option allows to specify the domain at which a cookie is inserted. It requires exactly one parameter: a valid domain name. If the domain begins with a dot, the browser is allowed to use it for any host ending with that name. It is also possible to specify several domain names by invoking this option multiple times. Some browsers might have small limits on the number of domains, so be careful when doing that. For the record, sending 10 domains to MSIE 6 or Firefox 2 works as expected.");
        methodList.add(method);
        
        method = new LbStickinessMethod(StickinessMethodType.AppCookieBased,"This is app session based sticky method,Define session stickiness on an existing application cookie. it can be used only for a specific http traffic",true);
        method.addParam("name", true,  "this is the name of the cookie used by the application and which LB will have to learn for each new session");
        method.addParam("length", true,  "this is the max number of characters that will be memorized and checked in each cookie value");  
        method.addParam("holdtime", true, "this is the time after which the cookie will be removed from memory if unused. If no unit is specified, this time is in milliseconds.");
        method.addParam("request-learn", false, "If this option is specified, then haproxy will be able to learn the cookie found in the request in case the server does not specify any in response. This is typically what happens with PHPSESSID cookies, or when haproxy's session expires before the application's session and the correct server is selected. It is recommended to specify this option to improve reliability");
        method.addParam("prefix", false,"When this option is specified, haproxy will match on the cookie prefix (or URL parameter prefix). The appsession value is the data following this prefix. Example : appsession ASPSESSIONID len 64 timeout 3h prefix  This will match the cookie ASPSESSIONIDXXXX=XXXXX, the appsession value will be XXXX=XXXXX.");
        method.addParam("mode", false,"This option allows to change the URL parser mode. 2 modes are currently supported : - path-parameters : The parser looks for the appsession in the path parameters part (each parameter is separated by a semi-colon), which is convenient for JSESSIONID for example.This is the default mode if the option is not set. - query-string : In this mode, the parser will look for the appsession in the query string.");
        methodList.add(method);
        
        method = new LbStickinessMethod(StickinessMethodType.SourceBased,"This is source based sticky method, it can be used for any type of protocol.",false);
        method.addParam("tablesize", false, "size of table to store source ip address.");
        method.addParam("expire", false, "entry in source ip table will expire after expire duration.");
        methodList.add(method);
        
        Gson gson = new Gson();
        String s = gson.toJson(methodList);
        s_logger.info("Before Converting to : " + s);
        lbCapabilities.put(Capability.SupportedStickinessMethods,s);
       
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
        
        Map<Capability, String> gatewayCapabilities = new HashMap<Capability, String>();
        gatewayCapabilities.put(Capability.Redundancy, "true");
        capabilities.put(Service.Gateway, gatewayCapabilities);
        
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
            result = result && (_routerMgr.destroyRouter(router.getId()) != null);
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
