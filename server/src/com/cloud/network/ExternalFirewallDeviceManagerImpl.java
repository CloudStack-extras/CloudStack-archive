/**
 * *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
*
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupExternalFirewallCommand;
import com.cloud.agent.api.StartupExternalLoadBalancerCommand;
import com.cloud.agent.api.routing.IpAssocCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.RemoteAccessVpnCfgCommand;
import com.cloud.agent.api.routing.SetPortForwardingRulesCommand;
import com.cloud.agent.api.routing.SetStaticNatRulesCommand;
import com.cloud.agent.api.routing.VpnUsersCfgCommand;
import com.cloud.agent.api.to.IpAddressTO;
import com.cloud.agent.api.to.PortForwardingRuleTO;
import com.cloud.agent.api.to.StaticNatRuleTO;
import com.cloud.api.ApiConstants;
import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.Vlan;
import com.cloud.dc.VlanVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientNetworkCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.DetailVO;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostDetailsDao;
import com.cloud.network.ExternalFirewallDeviceVO.FirewallDeviceState;
import com.cloud.network.ExternalNetworkDeviceManager.NetworkDevice;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Service;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.dao.ExternalFirewallDeviceDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.InlineLoadBalancerNicMapDao;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkExternalFirewallDao;
import com.cloud.network.dao.NetworkServiceMapDao;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkServiceProviderDao;
import com.cloud.network.dao.PhysicalNetworkServiceProviderVO;
import com.cloud.network.dao.VpnUserDao;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.Purpose;
import com.cloud.network.rules.PortForwardingRule;
import com.cloud.network.rules.StaticNatRule;
import com.cloud.network.rules.dao.PortForwardingRulesDao;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.resource.ResourceManager;
import com.cloud.resource.ResourceStateAdapter;
import com.cloud.resource.ServerResource;
import com.cloud.resource.UnableDeleteHostException;
import com.cloud.server.api.response.ExternalFirewallResponse;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserStatisticsDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import com.cloud.utils.net.UrlUtil;
import com.cloud.vm.Nic.ReservationStrategy;
import com.cloud.vm.Nic.State;
import com.cloud.vm.NicVO;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicDao;

public abstract class ExternalFirewallDeviceManagerImpl extends AdapterBase implements ExternalFirewallDeviceManager, ResourceStateAdapter {

    @Inject HostDao _hostDao;
    @Inject NetworkServiceMapDao _ntwkSrvcProviderDao;
    @Inject DataCenterDao _dcDao;
    @Inject NetworkManager _networkMgr;
    @Inject InlineLoadBalancerNicMapDao _inlineLoadBalancerNicMapDao;
    @Inject NicDao _nicDao;
    @Inject AgentManager _agentMgr;
    @Inject ResourceManager _resourceMgr;
    @Inject IPAddressDao _ipAddressDao;
    @Inject VlanDao _vlanDao;
    @Inject NetworkOfferingDao _networkOfferingDao;
    @Inject AccountDao _accountDao;
    @Inject PhysicalNetworkDao _physicalNetworkDao;
    @Inject PhysicalNetworkServiceProviderDao _physicalNetworkServiceProviderDao;
    @Inject AccountManager _accountMgr;
    @Inject UserStatisticsDao _userStatsDao;
    @Inject NetworkDao _networkDao;
    @Inject DomainRouterDao _routerDao;
    @Inject LoadBalancerDao _loadBalancerDao;
    @Inject PortForwardingRulesDao _portForwardingRulesDao;
    @Inject ConfigurationDao _configDao;
    @Inject ExternalFirewallDeviceDao _externalFirewallDeviceDao;
    @Inject NetworkExternalFirewallDao _networkExternalFirewallDao;
    @Inject VpnUserDao _vpnUsersDao;
    @Inject HostDetailsDao _hostDetailDao;

    private static final org.apache.log4j.Logger s_logger = Logger.getLogger(ExternalFirewallDeviceManagerImpl.class);
    private long _defaultFwCapacity;

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        _resourceMgr.registerResourceStateAdapter(this.getClass().getSimpleName(), this);
        _defaultFwCapacity = NumbersUtil.parseLong(_configDao.getValue(Config.DefaultExternalFirewallCapacity.key()), 50);
        return true;
    }

    @Override
    @DB
    public ExternalFirewallDeviceVO addExternalFirewall(long physicalNetworkId, String url, String username, String password, String deviceName, ServerResource resource) {
        String guid;
        PhysicalNetworkVO pNetwork=null;
        NetworkDevice ntwkDevice = NetworkDevice.getNetworkDevice(deviceName);
        long zoneId;

        if ((ntwkDevice == null) || (url == null) || (username == null) || (resource == null) || (password == null) ) {
            throw new InvalidParameterValueException("Atleast one of the required parameters (url, username, password," +
                    " server resource, zone id/physical network id) is not specified or a valid parameter.");
        }

        pNetwork = _physicalNetworkDao.findById(physicalNetworkId);
        if (pNetwork == null) {
            throw new InvalidParameterValueException("Could not find phyical network with ID: " + physicalNetworkId);
        }
        zoneId = pNetwork.getDataCenterId();

        PhysicalNetworkServiceProviderVO ntwkSvcProvider = _physicalNetworkServiceProviderDao.findByServiceProvider(pNetwork.getId(), ntwkDevice.getNetworkServiceProvder());
        if (ntwkSvcProvider == null ) {
            throw new CloudRuntimeException("Network Service Provider: " + ntwkDevice.getNetworkServiceProvder() + 
                    " is not enabled in the physical network: " + physicalNetworkId + "to add this device" );
        } else if (ntwkSvcProvider.getState() == PhysicalNetworkServiceProvider.State.Shutdown) {
            throw new CloudRuntimeException("Network Service Provider: " + ntwkSvcProvider.getProviderName() + 
                    " is not added or in shutdown state in the physical network: " + physicalNetworkId + "to add this device" );
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (Exception e) {
            s_logger.debug(e);
            throw new InvalidParameterValueException(e.getMessage());
        }

        String ipAddress = uri.getHost();
        Map hostDetails = new HashMap<String, String>();
        guid = getExternalNetworkResourceGuid(pNetwork.getId(), deviceName, ipAddress);
        hostDetails.put("name", guid);
        hostDetails.put("guid", guid);
        hostDetails.put("zoneId", String.valueOf(pNetwork.getDataCenterId()));
        hostDetails.put("ip", ipAddress);
        hostDetails.put("physicalNetworkId", String.valueOf(pNetwork.getId()));
        hostDetails.put("username", username);
        hostDetails.put("password", password);
        hostDetails.put("deviceName", deviceName);
        Map<String, String> configParams = new HashMap<String, String>();
        UrlUtil.parseQueryParameters(uri.getQuery(), false, configParams);
        hostDetails.putAll(configParams);

        // let the server resource to do parameters validation
        try {
            resource.configure(guid, hostDetails);
        } catch (ConfigurationException e) {
            throw new CloudRuntimeException(e.getMessage());
        }

        Host externalFirewall = _resourceMgr.addHost(zoneId, resource, Host.Type.ExternalFirewall, hostDetails);
        if (externalFirewall != null) {
            Transaction txn = Transaction.currentTxn();
            txn.start();

            boolean dedicatedUse = (configParams.get(ApiConstants.FIREWALL_DEVICE_DEDICATED) != null) ? Boolean.parseBoolean(configParams.get(ApiConstants.FIREWALL_DEVICE_DEDICATED)) : false;
            long capacity =  NumbersUtil.parseLong((String)configParams.get(ApiConstants.FIREWALL_DEVICE_CAPACITY), 0);
            if (capacity == 0) {
                capacity = _defaultFwCapacity;
            }

            ExternalFirewallDeviceVO fwDevice = new ExternalFirewallDeviceVO(externalFirewall.getId(), pNetwork.getId(), ntwkSvcProvider.getProviderName(), 
                    deviceName, capacity, dedicatedUse);

            _externalFirewallDeviceDao.persist(fwDevice);

            DetailVO hostDetail = new DetailVO(externalFirewall.getId(), ApiConstants.FIREWALL_DEVICE_ID, String.valueOf(fwDevice.getId()));
            _hostDetailDao.persist(hostDetail);

            txn.commit();
            return fwDevice;
        } else {
            return null;
        }
    }

    @Override
    public boolean deleteExternalFirewall(Long hostId) {
        HostVO externalFirewall = _hostDao.findById(hostId);
        if (externalFirewall == null) {
            throw new InvalidParameterValueException("Could not find an external firewall with ID: " + hostId);
        }

        try {
            if (_resourceMgr.maintain(hostId) && _resourceMgr.deleteHost(hostId, false, false)) {
                return true;
            } else {
                return false;
            }
        } catch (AgentUnavailableException e) {
            s_logger.debug(e);
            return false;
        }
    }

    @Override
    public List<Host> listExternalFirewalls(long physicalNetworkId, String deviceName) {
        List<Host> firewallHosts = new ArrayList<Host>();
        NetworkDevice fwNetworkDevice = NetworkDevice.getNetworkDevice(deviceName);
        PhysicalNetworkVO pNetwork=null;

        pNetwork = _physicalNetworkDao.findById(physicalNetworkId);
        if (pNetwork == null) {
            throw new InvalidParameterValueException("Could not find phyical network with ID: " + physicalNetworkId);
        }

        if ((pNetwork == null) || (fwNetworkDevice == null)) {
            throw new InvalidParameterValueException("Atleast one of ther required parameter physical networkId, device name is missing or invalid.");
        }

        PhysicalNetworkServiceProviderVO ntwkSvcProvider = _physicalNetworkServiceProviderDao.findByServiceProvider(pNetwork.getId(), fwNetworkDevice.getNetworkServiceProvder());
        if (ntwkSvcProvider == null) {
            return null; 
        }

        List<ExternalFirewallDeviceVO> fwDevices = _externalFirewallDeviceDao.listByPhysicalNetworkAndProvider(physicalNetworkId, ntwkSvcProvider.getProviderName());
        for (ExternalFirewallDeviceVO fwDevice : fwDevices) {
            firewallHosts.add(_hostDao.findById(fwDevice.getHostId()));
        }
        return firewallHosts;
    }
 
    public ExternalFirewallDeviceVO getExternalFirewallForNetwork(Network network) {
        NetworkExternalFirewallVO fwDeviceForNetwork = _networkExternalFirewallDao.findByNetworkId(network.getId());
        if (fwDeviceForNetwork != null) {
            long fwDeviceId = fwDeviceForNetwork.getExternalFirewallDeviceId();
            ExternalFirewallDeviceVO fwDevice = _externalFirewallDeviceDao.findById(fwDeviceId);
            assert(fwDevice != null);
            return fwDevice;
        }
        return null;
    }

    public void setExternalFirewallForNetwork(Network network, long externalFWDeviceID) {
        NetworkExternalFirewallVO fwDeviceForNetwork = new NetworkExternalFirewallVO(network.getId(), externalFWDeviceID);
        _networkExternalFirewallDao.persist(fwDeviceForNetwork);
    }

    @Override
    public ExternalFirewallDeviceVO findSuitableFirewallForNetwork(Network network) throws InsufficientCapacityException {
        long physicalNetworkId = network.getPhysicalNetworkId();
        List<ExternalFirewallDeviceVO> fwDevices = _externalFirewallDeviceDao.listByPhysicalNetwork(physicalNetworkId);

        // loop through the firewall device in the physical network and pick the first-fit 
        for (ExternalFirewallDeviceVO fwDevice: fwDevices) {
            // max number of guest networks that can be mapped to this device
            long fullCapacity = fwDevice.getCapacity();
            if (fullCapacity == 0) {
                fullCapacity = _defaultFwCapacity; // if capacity not configured then use the default
            }

            // get the list of guest networks that are mapped to this load balancer
            List<NetworkExternalFirewallVO> mappedNetworks = _networkExternalFirewallDao.listByFirewallDeviceId(fwDevice.getId()); 

            long usedCapacity = (mappedNetworks == null) ? 0 : mappedNetworks.size();
            if ((fullCapacity - usedCapacity) > 0) {
                return fwDevice;
            }
        }
        throw new InsufficientNetworkCapacityException("Unable to find a firewall provider with sufficient capcity " +
                " to implement the network", DataCenter.class, network.getDataCenterId());
    }

    public String getExternalNetworkResourceGuid(long physicalNetworkId, String deviceName, String ip) {
        return physicalNetworkId + "-" + deviceName + "-" + ip;
    }

    public ExternalFirewallResponse createExternalFirewallResponse(Host externalFirewall) {
        Map<String, String> fwDetails = _hostDetailDao.findDetails(externalFirewall.getId());
        ExternalFirewallResponse response = new ExternalFirewallResponse();
        response.setId(externalFirewall.getId());
        response.setIpAddress(externalFirewall.getPrivateIpAddress());
        response.setUsername(fwDetails.get("username"));
        response.setPublicInterface(fwDetails.get("publicInterface"));
        response.setUsageInterface(fwDetails.get("usageInterface"));
        response.setPrivateInterface(fwDetails.get("privateInterface"));
        response.setPublicZone(fwDetails.get("publicZone"));
        response.setPrivateZone(fwDetails.get("privateZone"));
        response.setNumRetries(fwDetails.get("numRetries"));
        response.setTimeout(fwDetails.get("timeout"));
        return response;
    }

    @Override
    public boolean manageGuestNetworkWithExternalFirewall(boolean add, Network network) throws ResourceUnavailableException, InsufficientCapacityException {
        if (network.getTrafficType() != TrafficType.Guest) {
            s_logger.trace("External firewall can only be used for add/remove guest networks.");
            return false;
        }

        long zoneId = network.getDataCenterId();
        DataCenterVO zone = _dcDao.findById(zoneId);
        HostVO externalFirewall = null;

        if (add) {
            GlobalLock deviceMapLock =  GlobalLock.getInternLock("NetworkFirewallDeviceMap");
            try {
                if (deviceMapLock.lock(120)) {
                    try {
                        ExternalFirewallDeviceVO device  = findSuitableFirewallForNetwork(network); 
                        long externalFirewallId = device.getId();

                        NetworkExternalFirewallVO networkFW = new NetworkExternalFirewallVO(network.getId(), externalFirewallId);
                        _networkExternalFirewallDao.persist(networkFW);

                        externalFirewall = _hostDao.findById(device.getHostId());
                    } finally {
                        deviceMapLock.unlock();
                    }
                }
            } finally {
                deviceMapLock.releaseRef();
            }
        } else {
            ExternalFirewallDeviceVO fwDeviceVO = getExternalFirewallForNetwork(network);
            if (fwDeviceVO == null) {
                s_logger.warn("Network shutdown requested on external firewall element, which did not implement the network." +
                              " Either network implement failed half way through or already network shutdown is completed.");
                return true;
            }
            externalFirewall = _hostDao.findById(fwDeviceVO.getHostId());
        }

        Account account = _accountDao.findByIdIncludingRemoved(network.getAccountId());
        
        NetworkOffering offering = _networkOfferingDao.findById(network.getNetworkOfferingId());      
        boolean sharedSourceNat = offering.getSharedSourceNat();
        
        IPAddressVO sourceNatIp = null;
        if (!sharedSourceNat) {
            // Get the source NAT IP address for this network          
            List<IPAddressVO> sourceNatIps = _networkMgr.listPublicIpAddressesInVirtualNetwork(network.getAccountId(), zoneId, true, null);

            if (sourceNatIps.size() != 1) {
                String errorMsg = "External firewall was unable to find the source NAT IP address for account " + account.getAccountName();
                s_logger.error(errorMsg);
                return true;
            } else {
                sourceNatIp = sourceNatIps.get(0);
            }
        }

        // Send a command to the external firewall to implement or shutdown the guest network
        long guestVlanTag = Long.parseLong(network.getBroadcastUri().getHost());
        String guestVlanGateway = network.getGateway();
        String guestVlanCidr = network.getCidr();
        String sourceNatIpAddress = null;
        String publicVlanTag = null;

        if (sourceNatIp != null) {
            sourceNatIpAddress = sourceNatIp.getAddress().addr();
            VlanVO publicVlan = _vlanDao.findById(sourceNatIp.getVlanId());
            publicVlanTag = publicVlan.getVlanTag();
        }

        // Get network rate
        Integer networkRate = _networkMgr.getNetworkRate(network.getId(), null);

        IpAddressTO ip = new IpAddressTO(account.getAccountId(), sourceNatIpAddress, add, false, !sharedSourceNat, publicVlanTag, null, null, null, null, networkRate, false);
        IpAddressTO[] ips = new IpAddressTO[1];
        ips[0] = ip;
        IpAssocCommand cmd = new IpAssocCommand(ips);
        cmd.setAccessDetail(NetworkElementCommand.GUEST_NETWORK_GATEWAY, guestVlanGateway);
        cmd.setAccessDetail(NetworkElementCommand.GUEST_NETWORK_CIDR, guestVlanCidr);
        cmd.setAccessDetail(NetworkElementCommand.GUEST_VLAN_TAG, String.valueOf(guestVlanTag));
        Answer answer = _agentMgr.easySend(externalFirewall.getId(), cmd);

        if (answer == null || !answer.getResult()) {
            String action = add ? "implement" : "shutdown";
            String answerDetails = (answer != null) ? answer.getDetails() : "answer was null";
            String msg = "External firewall was unable to " + action + " the guest network on the external firewall in zone " + zone.getName() + " due to " + answerDetails;
            s_logger.error(msg);
            throw new ResourceUnavailableException(msg, DataCenter.class, zoneId);
        }

        List<String> reservedIpAddressesForGuestNetwork = _nicDao.listIpAddressInNetwork(network.getId());
        if (add && (!reservedIpAddressesForGuestNetwork.contains(network.getGateway()))) {
            // Insert a new NIC for this guest network to reserve the gateway address
            savePlaceholderNic(network,  network.getGateway());
        }
        
        // Delete any mappings used for inline external load balancers in this network
        List<NicVO> nicsInNetwork = _nicDao.listByNetworkId(network.getId());
        for (NicVO nic : nicsInNetwork) {
            InlineLoadBalancerNicMapVO mapping = _inlineLoadBalancerNicMapDao.findByNicId(nic.getId());
            
            if (mapping != null) {
                _nicDao.expunge(mapping.getNicId());
                _inlineLoadBalancerNicMapDao.expunge(mapping.getId());
            }
        }
        
        String action = add ? "implemented" : "shut down";
        s_logger.debug("External firewall has " + action + " the guest network for account " + account.getAccountName() + "(id = " + account.getAccountId() + ") with VLAN tag " + guestVlanTag);

        return true;
    }

    @Override
    public boolean applyFirewallRules(Network network, List<? extends FirewallRule> rules) throws ResourceUnavailableException {
        // Find the external firewall in this zone
        long zoneId = network.getDataCenterId();
        DataCenterVO zone = _dcDao.findById(zoneId);
        ExternalFirewallDeviceVO fwDeviceVO = getExternalFirewallForNetwork(network);
        HostVO externalFirewall = _hostDao.findById(fwDeviceVO.getHostId());

        assert(externalFirewall != null);

        if (network.getState() == Network.State.Allocated) {
            s_logger.debug("External firewall was asked to apply firewall rules for network with ID " + network.getId() + "; this network is not implemented. Skipping backend commands.");
            return true;
        }

        List<StaticNatRuleTO> staticNatRules = new ArrayList<StaticNatRuleTO>();
        List<PortForwardingRuleTO> portForwardingRules = new ArrayList<PortForwardingRuleTO>();

        for (FirewallRule rule : rules) {
            IpAddress sourceIp = _networkMgr.getIp(rule.getSourceIpAddressId());
            Vlan vlan = _vlanDao.findById(sourceIp.getVlanId());

            if (rule.getPurpose() == Purpose.StaticNat) {
                StaticNatRule staticNatRule = (StaticNatRule) rule;
                StaticNatRuleTO ruleTO = new StaticNatRuleTO(staticNatRule, vlan.getVlanTag(), sourceIp.getAddress().addr(), staticNatRule.getDestIpAddress());
                staticNatRules.add(ruleTO);
            } else if (rule.getPurpose() == Purpose.PortForwarding) {
                PortForwardingRuleTO ruleTO = new PortForwardingRuleTO((PortForwardingRule) rule, vlan.getVlanTag(), sourceIp.getAddress().addr());
                portForwardingRules.add(ruleTO);
            }
        }

        // Apply static nat rules
        applyStaticNatRules(staticNatRules, zone, externalFirewall.getId());

        // apply port forwarding rules
        applyPortForwardingRules(portForwardingRules, zone, externalFirewall.getId());

        return true;
    }

    protected void applyStaticNatRules(List<StaticNatRuleTO> staticNatRules, DataCenter zone, long externalFirewallId) throws ResourceUnavailableException {
        if (!staticNatRules.isEmpty()) {
            SetStaticNatRulesCommand cmd = new SetStaticNatRulesCommand(staticNatRules);
            Answer answer = _agentMgr.easySend(externalFirewallId, cmd);
            if (answer == null || !answer.getResult()) {
                String details = (answer != null) ? answer.getDetails() : "details unavailable";
                String msg = "External firewall was unable to apply static nat rules to the SRX appliance in zone " + zone.getName() + " due to: " + details + ".";
                s_logger.error(msg);
                throw new ResourceUnavailableException(msg, DataCenter.class, zone.getId());
            }
        }
    }

    protected void applyPortForwardingRules(List<PortForwardingRuleTO> portForwardingRules, DataCenter zone, long externalFirewallId) throws ResourceUnavailableException {
        if (!portForwardingRules.isEmpty()) {
            SetPortForwardingRulesCommand cmd = new SetPortForwardingRulesCommand(portForwardingRules);
            Answer answer = _agentMgr.easySend(externalFirewallId, cmd);
            if (answer == null || !answer.getResult()) {
                String details = (answer != null) ? answer.getDetails() : "details unavailable";
                String msg = "External firewall was unable to apply port forwarding rules to the SRX appliance in zone " + zone.getName() + " due to: " + details + ".";
                s_logger.error(msg);
                throw new ResourceUnavailableException(msg, DataCenter.class, zone.getId());
            }
        }
    }

    public boolean applyIps(Network network, List<? extends PublicIpAddress> ipAddresses) throws ResourceUnavailableException {
        return true;
    }

    public boolean manageRemoteAccessVpn(boolean create, Network network, RemoteAccessVpn vpn) throws ResourceUnavailableException {
        ExternalFirewallDeviceVO fwDeviceVO = getExternalFirewallForNetwork(network);
        HostVO externalFirewall = _hostDao.findById(fwDeviceVO.getHostId());

        if (externalFirewall == null) {
            return false;
        }
        
        // Create/delete VPN
        IpAddress ip = _networkMgr.getIp(vpn.getServerAddressId());
        
        // Mask the IP range with the network's VLAN tag
        String[] ipRange = vpn.getIpRange().split("-");
        DataCenterVO zone = _dcDao.findById(network.getDataCenterId());
        int vlanTag = Integer.parseInt(network.getBroadcastUri().getHost());
        int offset = getVlanOffset(network.getPhysicalNetworkId(), vlanTag);
        int cidrSize = getGloballyConfiguredCidrSize();
        
        for (int i = 0; i < 2; i++) {
            ipRange[i] = NetUtils.long2Ip((NetUtils.ip2Long(ipRange[i]) & 0xff000000) | (offset << (32 - cidrSize)));
        }
        
        String maskedIpRange = ipRange[0] + "-" + ipRange[1];
        
        RemoteAccessVpnCfgCommand createVpnCmd = new RemoteAccessVpnCfgCommand(create, ip.getAddress().addr(), vpn.getLocalIp(), maskedIpRange, vpn.getIpsecPresharedKey());
        createVpnCmd.setAccessDetail(NetworkElementCommand.ACCOUNT_ID, String.valueOf(network.getAccountId()));
        createVpnCmd.setAccessDetail(NetworkElementCommand.GUEST_NETWORK_CIDR, network.getCidr()); 
        Answer answer = _agentMgr.easySend(externalFirewall.getId(), createVpnCmd);
        if (answer == null || !answer.getResult()) {
             String details = (answer != null) ? answer.getDetails() : "details unavailable";
             String msg = "External firewall was unable to create a remote access VPN in zone " + zone.getName() + " due to: " + details + ".";
             s_logger.error(msg);
             throw new ResourceUnavailableException(msg, DataCenter.class, zone.getId());
        }
        
        // Add/delete users
        List<VpnUserVO> vpnUsers = _vpnUsersDao.listByAccount(vpn.getAccountId());
        return manageRemoteAccessVpnUsers(network, vpn, vpnUsers);
    }  
    
    public boolean manageRemoteAccessVpnUsers(Network network, RemoteAccessVpn vpn, List<? extends VpnUser> vpnUsers) throws ResourceUnavailableException {
        ExternalFirewallDeviceVO fwDeviceVO = getExternalFirewallForNetwork(network);
        HostVO externalFirewall = _hostDao.findById(fwDeviceVO.getHostId());

        if (externalFirewall == null) {
            return false;
        }
        
        List<VpnUser> addUsers = new ArrayList<VpnUser>();
        List<VpnUser> removeUsers = new ArrayList<VpnUser>();
        for (VpnUser user : vpnUsers) {
            if (user.getState() == VpnUser.State.Add ||
                user.getState() == VpnUser.State.Active) {
                addUsers.add(user);
            } else if (user.getState() == VpnUser.State.Revoke) {
                removeUsers.add(user);
            }
        }
        
        VpnUsersCfgCommand addUsersCmd = new VpnUsersCfgCommand(addUsers, removeUsers);
        addUsersCmd.setAccessDetail(NetworkElementCommand.ACCOUNT_ID, String.valueOf(network.getAccountId()));
        addUsersCmd.setAccessDetail(NetworkElementCommand.GUEST_NETWORK_CIDR, network.getCidr());        
        
        Answer answer = _agentMgr.easySend(externalFirewall.getId(), addUsersCmd);
        if (answer == null || !answer.getResult()) {
             String details = (answer != null) ? answer.getDetails() : "details unavailable";
             DataCenterVO zone = _dcDao.findById(network.getDataCenterId());
             String msg = "External firewall was unable to add remote access users in zone " + zone.getName() + " due to: " + details + ".";
             s_logger.error(msg);
             throw new ResourceUnavailableException(msg, DataCenter.class, zone.getId());
        }
        
        return true;
    }

    public int getVlanOffset(long physicalNetworkId, int vlanTag) {
        PhysicalNetworkVO pNetwork = _physicalNetworkDao.findById(physicalNetworkId);
        if (pNetwork == null) {
            throw new CloudRuntimeException("Could not find the physical Network " + physicalNetworkId + ".");
        }

        if (pNetwork.getVnet() == null) {
            throw new CloudRuntimeException("Could not find vlan range for physical Network " + physicalNetworkId + ".");
        }
        String vlanRange[] = pNetwork.getVnet().split("-");
        int lowestVlanTag = Integer.valueOf(vlanRange[0]);
        return vlanTag - lowestVlanTag;
    }
    
    private NicVO savePlaceholderNic(Network network, String ipAddress) {
        NicVO nic = new NicVO(null, null, network.getId(), null);
        nic.setIp4Address(ipAddress);
        nic.setReservationStrategy(ReservationStrategy.PlaceHolder);
        nic.setState(State.Reserved);
        return _nicDao.persist(nic);
    }
    
    public int getGloballyConfiguredCidrSize() {
        try {
            String globalVlanBits = _configDao.getValue(Config.GuestVlanBits.key());
            return 8 + Integer.parseInt(globalVlanBits);
        } catch (Exception e) {
            throw new CloudRuntimeException("Failed to read the globally configured VLAN bits size.");
        }
    }

    @Override
    public HostVO createHostVOForConnectedAgent(HostVO host, StartupCommand[] cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HostVO createHostVOForDirectConnectAgent(HostVO host, StartupCommand[] startup, ServerResource resource,
            Map<String, String> details, List<String> hostTags) {
        if (!(startup[0] instanceof StartupExternalFirewallCommand)) {
            return null;
        }
        host.setType(Host.Type.ExternalFirewall);
        return host;
    }

    @Override
    public DeleteHostAnswer deleteHost(HostVO host, boolean isForced, boolean isForceDeleteStorage) throws UnableDeleteHostException {
        // TODO Auto-generated method stub
        return null;
    }
}
