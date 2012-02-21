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

package com.cloud.network.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.api.commands.AddExternalFirewallCmd;
import com.cloud.api.commands.AddSrxFirewallCmd;
import com.cloud.api.commands.ConfigureSrxFirewallCmd;
import com.cloud.api.commands.DeleteExternalFirewallCmd;
import com.cloud.api.commands.DeleteSrxFirewallCmd;
import com.cloud.api.commands.ListExternalFirewallsCmd;
import com.cloud.api.commands.ListSrxFirewallNetworksCmd;
import com.cloud.api.commands.ListSrxFirewallsCmd;
import com.cloud.api.response.SrxFirewallResponse;
import com.cloud.configuration.Config;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientNetworkCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostDetailsDao;
import com.cloud.network.ExternalFirewallDeviceManagerImpl;
import com.cloud.network.ExternalFirewallDeviceVO;
import com.cloud.network.ExternalFirewallDeviceVO.FirewallDeviceState;
import com.cloud.network.ExternalNetworkDeviceManager.NetworkDevice;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkExternalFirewallVO;
import com.cloud.network.NetworkManager;
import com.cloud.network.NetworkVO;
import com.cloud.network.PhysicalNetworkServiceProvider;
import com.cloud.network.PhysicalNetworkVO;
import com.cloud.network.PublicIpAddress;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.VpnUser;
import com.cloud.network.dao.ExternalFirewallDeviceDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkExternalFirewallDao;
import com.cloud.network.dao.NetworkServiceMapDao;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.resource.JuniperSrxResource;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.PortForwardingRule;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.resource.ServerResource;
import com.cloud.server.api.response.ExternalFirewallResponse;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.Inject;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

@Local(value = NetworkElement.class)
public class JuniperSRXExternalFirewallElement extends ExternalFirewallDeviceManagerImpl implements SourceNatServiceProvider, FirewallServiceProvider,
        PortForwardingServiceProvider, RemoteAccessVPNServiceProvider, IpDeployer, JuniperSRXFirewallElementService {

    private static final Logger s_logger = Logger.getLogger(JuniperSRXExternalFirewallElement.class);

    private static final Map<Service, Map<Capability, String>> capabilities = setCapabilities();

    @Inject
    NetworkManager _networkManager;
    @Inject
    HostDao _hostDao;
    @Inject
    ConfigurationManager _configMgr;
    @Inject
    NetworkOfferingDao _networkOfferingDao;
    @Inject
    NetworkDao _networksDao;
    @Inject
    DataCenterDao _dcDao;
    @Inject
    PhysicalNetworkDao _physicalNetworkDao;
    @Inject
    ExternalFirewallDeviceDao _fwDevicesDao;
    @Inject
    NetworkExternalFirewallDao _networkFirewallDao;
    @Inject
    NetworkDao _networkDao;
    @Inject
    NetworkServiceMapDao _ntwkSrvcDao;
    @Inject
    HostDetailsDao _hostDetailDao;
    @Inject
    ConfigurationDao _configDao;

    private boolean canHandle(Network network, Service service) {
        DataCenter zone = _configMgr.getZone(network.getDataCenterId());
        if ((zone.getNetworkType() == NetworkType.Advanced && network.getGuestType() != Network.GuestType.Isolated) || (zone.getNetworkType() == NetworkType.Basic && network.getGuestType() != Network.GuestType.Shared)) {
            s_logger.trace("Element " + getProvider().getName() + "is not handling network type = " + network.getGuestType());
            return false;
        }

        if (service == null) {
            if (!_networkManager.isProviderForNetwork(getProvider(), network.getId())) {
                s_logger.trace("Element " + getProvider().getName() + " is not a provider for the network " + network);
                return false;
            }
        } else {
            if (!_networkManager.isProviderSupportServiceInNetwork(network.getId(), service, getProvider())) {
                s_logger.trace("Element " + getProvider().getName() + " doesn't support service " + service.getName() + " in the network " + network);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean implement(Network network, NetworkOffering offering, DeployDestination dest, ReservationContext context) throws ResourceUnavailableException, ConcurrentOperationException,
            InsufficientNetworkCapacityException {
        DataCenter zone = _configMgr.getZone(network.getDataCenterId());

        // don't have to implement network is Basic zone
        if (zone.getNetworkType() == NetworkType.Basic) {
            s_logger.debug("Not handling network implement in zone of type " + NetworkType.Basic);
            return false;
        }

        if (!canHandle(network, null)) {
            return false;
        }

        try {
            return manageGuestNetworkWithExternalFirewall(true, network);
        } catch (InsufficientCapacityException capacityException) {
            // TODO: handle out of capacity exception in more gracefule manner when multiple providers are present for
            // the network
            s_logger.error("Fail to implement the JuniperSRX for network " + network, capacityException);
            return false;
        }
    }

    @Override
    public boolean prepare(Network config, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest, ReservationContext context) throws ConcurrentOperationException,
            InsufficientNetworkCapacityException, ResourceUnavailableException {
        return true;
    }

    @Override
    public boolean release(Network config, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, ReservationContext context) {
        return true;
    }

    @Override
    public boolean shutdown(Network network, ReservationContext context, boolean cleanup) throws ResourceUnavailableException, ConcurrentOperationException {
        DataCenter zone = _configMgr.getZone(network.getDataCenterId());

        // don't have to implement network is Basic zone
        if (zone.getNetworkType() == NetworkType.Basic) {
            s_logger.debug("Not handling network shutdown in zone of type " + NetworkType.Basic);
            return false;
        }

        if (!canHandle(network, null)) {
            return false;
        }
        try {
            return manageGuestNetworkWithExternalFirewall(false, network);
        } catch (InsufficientCapacityException capacityException) {
            // TODO: handle out of capacity exception
            return false;
        }
    }

    @Override
    public boolean destroy(Network config) {
        return true;
    }

    @Override
    public boolean applyFWRules(Network config, List<? extends FirewallRule> rules) throws ResourceUnavailableException {
        if (!canHandle(config, Service.Firewall)) {
            return false;
        }

        return applyFirewallRules(config, rules);
    }

    @Override
    public boolean startVpn(Network config, RemoteAccessVpn vpn) throws ResourceUnavailableException {
        if (!canHandle(config, Service.Vpn)) {
            return false;
        }

        return manageRemoteAccessVpn(true, config, vpn);

    }

    @Override
    public boolean stopVpn(Network config, RemoteAccessVpn vpn) throws ResourceUnavailableException {
        if (!canHandle(config, Service.Vpn)) {
            return false;
        }

        return manageRemoteAccessVpn(false, config, vpn);
    }

    @Override
    public String[] applyVpnUsers(RemoteAccessVpn vpn, List<? extends VpnUser> users) throws ResourceUnavailableException {
        Network config = _networksDao.findById(vpn.getNetworkId());

        if (!canHandle(config, Service.Vpn)) {
            return null;
        }

        boolean result = manageRemoteAccessVpnUsers(config, vpn, users);
        String[] results = new String[users.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = String.valueOf(result);
        }

        return results;
    }

    @Override
    public Provider getProvider() {
        return Provider.JuniperSRX;
    }

    @Override
    public Map<Service, Map<Capability, String>> getCapabilities() {
        return capabilities;
    }

    private static Map<Service, Map<Capability, String>> setCapabilities() {
        Map<Service, Map<Capability, String>> capabilities = new HashMap<Service, Map<Capability, String>>();

        // Set capabilities for Firewall service
        Map<Capability, String> firewallCapabilities = new HashMap<Capability, String>();
        firewallCapabilities.put(Capability.SupportedProtocols, "tcp,udp");
        firewallCapabilities.put(Capability.MultipleIps, "true");
        firewallCapabilities.put(Capability.TrafficStatistics, "per public ip");
        capabilities.put(Service.Firewall, firewallCapabilities);

        // Disabling VPN for Juniper in Acton as it 1) Was never tested 2) probably just doesn't work
// // Set VPN capabilities
// Map<Capability, String> vpnCapabilities = new HashMap<Capability, String>();
// vpnCapabilities.put(Capability.SupportedVpnTypes, "ipsec");
// capabilities.put(Service.Vpn, vpnCapabilities);

        capabilities.put(Service.Gateway, null);

        Map<Capability, String> sourceNatCapabilities = new HashMap<Capability, String>();
        // Specifies that this element supports either one source NAT rule per account, or no source NAT rules at all;
        // in the latter case a shared interface NAT rule will be used
        sourceNatCapabilities.put(Capability.SupportedSourceNatTypes, "peraccount, perzone");
        capabilities.put(Service.SourceNat, sourceNatCapabilities);

        // Specifies that port forwarding rules are supported by this element
        capabilities.put(Service.PortForwarding, null);

        // Specifies that static NAT rules are supported by this element
        capabilities.put(Service.StaticNat, null);

        return capabilities;
    }

    @Override
    public boolean applyPFRules(Network network, List<PortForwardingRule> rules) throws ResourceUnavailableException {
        if (!canHandle(network, Service.PortForwarding)) {
            return false;
        }

        return applyFirewallRules(network, rules);
    }

    @Override
    public boolean isReady(PhysicalNetworkServiceProvider provider) {

        List<ExternalFirewallDeviceVO> fwDevices = _fwDevicesDao.listByPhysicalNetworkAndProvider(provider.getPhysicalNetworkId(), Provider.JuniperSRX.getName());
        // true if at-least one SRX device is added in to physical network and is in configured (in enabled state) state
        if (fwDevices != null && !fwDevices.isEmpty()) {
            for (ExternalFirewallDeviceVO fwDevice : fwDevices) {
                if (fwDevice.getDeviceState() == FirewallDeviceState.Enabled) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean shutdownProviderInstances(PhysicalNetworkServiceProvider provider, ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean canEnableIndividualServices() {
        return false;
    }

    @Override
    @Deprecated
    // should use more generic addNetworkDevice command to add firewall
    public Host addExternalFirewall(AddExternalFirewallCmd cmd) {
        Long zoneId = cmd.getZoneId();
        DataCenterVO zone = null;
        PhysicalNetworkVO pNetwork = null;
        HostVO fwHost = null;

        zone = _dcDao.findById(zoneId);
        if (zone == null) {
            throw new InvalidParameterValueException("Could not find zone with ID: " + zoneId);
        }

        List<PhysicalNetworkVO> physicalNetworks = _physicalNetworkDao.listByZone(zoneId);
        if ((physicalNetworks == null) || (physicalNetworks.size() > 1)) {
            throw new InvalidParameterValueException("There are no physical networks or multiple physical networks configured in zone with ID: "
                    + zoneId + " to add this device.");
        }
        pNetwork = physicalNetworks.get(0);

        String deviceType = NetworkDevice.JuniperSRXFirewall.getName();
        ExternalFirewallDeviceVO fwDeviceVO = addExternalFirewall(pNetwork.getId(), cmd.getUrl(), cmd.getUsername(), cmd.getPassword(), deviceType, (ServerResource) new JuniperSrxResource());
        if (fwDeviceVO != null) {
            fwHost = _hostDao.findById(fwDeviceVO.getHostId());
        }

        return fwHost;
    }

    @Override
    public boolean deleteExternalFirewall(DeleteExternalFirewallCmd cmd) {
        return deleteExternalFirewall(cmd.getId());
    }

    @Override
    @Deprecated
    // should use more generic listNetworkDevice command
    public List<Host> listExternalFirewalls(ListExternalFirewallsCmd cmd) {
        List<Host> firewallHosts = new ArrayList<Host>();
        Long zoneId = cmd.getZoneId();
        DataCenterVO zone = null;
        PhysicalNetworkVO pNetwork = null;

        if (zoneId != null) {
            zone = _dcDao.findById(zoneId);
            if (zone == null) {
                throw new InvalidParameterValueException("Could not find zone with ID: " + zoneId);
            }

            List<PhysicalNetworkVO> physicalNetworks = _physicalNetworkDao.listByZone(zoneId);
            if ((physicalNetworks == null) || (physicalNetworks.size() > 1)) {
                throw new InvalidParameterValueException("There are no physical networks or multiple physical networks configured in zone with ID: "
                        + zoneId + " to add this device.");
            }
            pNetwork = physicalNetworks.get(0);
        }

        firewallHosts.addAll(listExternalFirewalls(pNetwork.getId(), NetworkDevice.JuniperSRXFirewall.getName()));
        return firewallHosts;
    }

    public ExternalFirewallResponse createExternalFirewallResponse(Host externalFirewall) {
        return super.createExternalFirewallResponse(externalFirewall);
    }

    @Override
    public String getPropertiesFile() {
        return "junipersrx_commands.properties";
    }

    @Override
    public ExternalFirewallDeviceVO addSrxFirewall(AddSrxFirewallCmd cmd) {
        String deviceName = cmd.getDeviceType();
        if (!deviceName.equalsIgnoreCase(NetworkDevice.JuniperSRXFirewall.getName())) {
            throw new InvalidParameterValueException("Invalid SRX firewall device type");
        }
        return addExternalFirewall(cmd.getPhysicalNetworkId(), cmd.getUrl(), cmd.getUsername(), cmd.getPassword(), deviceName,
                (ServerResource) new JuniperSrxResource());
    }

    @Override
    public boolean deleteSrxFirewall(DeleteSrxFirewallCmd cmd) {
        Long fwDeviceId = cmd.getFirewallDeviceId();

        ExternalFirewallDeviceVO fwDeviceVO = _fwDevicesDao.findById(fwDeviceId);
        if (fwDeviceVO == null || !fwDeviceVO.getDeviceName().equalsIgnoreCase(NetworkDevice.JuniperSRXFirewall.getName())) {
            throw new InvalidParameterValueException("No SRX firewall device found with ID: " + fwDeviceId);
        }
        return deleteExternalFirewall(fwDeviceVO.getHostId());
    }

    @Override
    public ExternalFirewallDeviceVO configureSrxFirewall(ConfigureSrxFirewallCmd cmd) {
        Long fwDeviceId = cmd.getFirewallDeviceId();
        Long deviceCapacity = cmd.getFirewallCapacity();

        ExternalFirewallDeviceVO fwDeviceVO = _fwDevicesDao.findById(fwDeviceId);
        if (fwDeviceVO == null || !fwDeviceVO.getDeviceName().equalsIgnoreCase(NetworkDevice.JuniperSRXFirewall.getName())) {
            throw new InvalidParameterValueException("No SRX firewall device found with ID: " + fwDeviceId);
        }

        if (deviceCapacity != null) {
            // check if any networks are using this SRX device
            List<NetworkExternalFirewallVO> networks = _networkFirewallDao.listByFirewallDeviceId(fwDeviceId);
            if ((networks != null) && !networks.isEmpty()) {
                if (deviceCapacity < networks.size()) {
                    throw new CloudRuntimeException("There are more number of networks already using this SRX firewall device than configured capacity");
                }
            }
            if (deviceCapacity != null) {
                fwDeviceVO.setCapacity(deviceCapacity);
            }
        }

        fwDeviceVO.setDeviceState(FirewallDeviceState.Enabled);
        _fwDevicesDao.update(fwDeviceId, fwDeviceVO);
        return fwDeviceVO;
    }

    @Override
    public List<ExternalFirewallDeviceVO> listSrxFirewalls(ListSrxFirewallsCmd cmd) {
        Long physcialNetworkId = cmd.getPhysicalNetworkId();
        Long fwDeviceId = cmd.getFirewallDeviceId();
        PhysicalNetworkVO pNetwork = null;
        List<ExternalFirewallDeviceVO> fwDevices = new ArrayList<ExternalFirewallDeviceVO>();

        if (physcialNetworkId == null && fwDeviceId == null) {
            throw new InvalidParameterValueException("Either physical network Id or load balancer device Id must be specified");
        }

        if (fwDeviceId != null) {
            ExternalFirewallDeviceVO fwDeviceVo = _fwDevicesDao.findById(fwDeviceId);
            if (fwDeviceVo == null || !fwDeviceVo.getDeviceName().equalsIgnoreCase(NetworkDevice.JuniperSRXFirewall.getName())) {
                throw new InvalidParameterValueException("Could not find SRX firewall device with ID: " + fwDeviceId);
            }
            fwDevices.add(fwDeviceVo);
        }

        if (physcialNetworkId != null) {
            pNetwork = _physicalNetworkDao.findById(physcialNetworkId);
            if (pNetwork == null) {
                throw new InvalidParameterValueException("Could not find phyical network with ID: " + physcialNetworkId);
            }
            fwDevices = _fwDevicesDao.listByPhysicalNetworkAndProvider(physcialNetworkId, Provider.JuniperSRX.getName());
        }

        return fwDevices;
    }

    @Override
    public List<? extends Network> listNetworks(ListSrxFirewallNetworksCmd cmd) {
        Long fwDeviceId = cmd.getFirewallDeviceId();
        List<NetworkVO> networks = new ArrayList<NetworkVO>();

        ExternalFirewallDeviceVO fwDeviceVo = _fwDevicesDao.findById(fwDeviceId);
        if (fwDeviceVo == null || !fwDeviceVo.getDeviceName().equalsIgnoreCase(NetworkDevice.JuniperSRXFirewall.getName())) {
            throw new InvalidParameterValueException("Could not find SRX firewall device with ID " + fwDeviceId);
        }

        List<NetworkExternalFirewallVO> networkFirewallMaps = _networkFirewallDao.listByFirewallDeviceId(fwDeviceId);
        if (networkFirewallMaps != null && !networkFirewallMaps.isEmpty()) {
            for (NetworkExternalFirewallVO networkFirewallMap : networkFirewallMaps) {
                NetworkVO network = _networkDao.findById(networkFirewallMap.getNetworkId());
                networks.add(network);
            }
        }

        return networks;
    }

    @Override
    public SrxFirewallResponse createSrxFirewallResponse(ExternalFirewallDeviceVO fwDeviceVO) {
        SrxFirewallResponse response = new SrxFirewallResponse();
        Map<String, String> fwDetails = _hostDetailDao.findDetails(fwDeviceVO.getHostId());
        Host fwHost = _hostDao.findById(fwDeviceVO.getHostId());

        response.setId(fwDeviceVO.getId());
        response.setPhysicalNetworkId(fwDeviceVO.getPhysicalNetworkId());
        response.setDeviceName(fwDeviceVO.getDeviceName());
        if (fwDeviceVO.getCapacity() == 0) {
            long defaultFwCapacity = NumbersUtil.parseLong(_configDao.getValue(Config.DefaultExternalFirewallCapacity.key()), 50);
            response.setDeviceCapacity(defaultFwCapacity);
        } else {
            response.setDeviceCapacity(fwDeviceVO.getCapacity());
        }
        response.setProvider(fwDeviceVO.getProviderName());
        response.setDeviceState(fwDeviceVO.getDeviceState().name());
        response.setIpAddress(fwHost.getPrivateIpAddress());
        response.setPublicInterface(fwDetails.get("publicInterface"));
        response.setUsageInterface(fwDetails.get("usageInterface"));
        response.setPrivateInterface(fwDetails.get("privateInterface"));
        response.setPublicZone(fwDetails.get("publicZone"));
        response.setPrivateZone(fwDetails.get("privateZone"));
        response.setNumRetries(fwDetails.get("numRetries"));
        response.setTimeout(fwDetails.get("timeout"));
        response.setObjectName("srxfirewall");
        return response;
    }

    @Override
    public boolean verifyServicesCombination(List<String> services) {
        return true;
    }

    @Override
    public IpDeployer getIpDeployer(Network network) {
        return this;
    }

    @Override
    public boolean applyIps(Network network, List<? extends PublicIpAddress> ipAddress, Set<Service> service) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }
}