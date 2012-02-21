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
package com.cloud.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cloud.acl.ControlledEntity.ACLType;
import com.cloud.dc.DataCenter;
import com.cloud.dc.Vlan;
import com.cloud.dc.Vlan.VlanType;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapcityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.addr.PublicIp;
import com.cloud.network.element.RemoteAccessVPNServiceProvider;
import com.cloud.network.element.UserDataServiceProvider;
import com.cloud.network.guru.NetworkGuru;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.StaticNat;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.user.Account;
import com.cloud.user.User;
import com.cloud.utils.Pair;
import com.cloud.vm.Nic;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

/**
 * NetworkManager manages the network for the different end users.
 * 
 */
public interface NetworkManager extends NetworkService {
    /**
     * Assigns a new public ip address.
     * 
     * @param dcId
     * @param podId
     *            TODO
     * @param owner
     * @param type
     * @param networkId
     * @param requestedIp
     *            TODO
     * @param allocatedBy
     *            TODO
     * @return
     * @throws InsufficientAddressCapacityException
     */

    PublicIp assignPublicIpAddress(long dcId, Long podId, Account owner, VlanType type, Long networkId, String requestedIp, boolean isSystem) throws InsufficientAddressCapacityException;

    /**
     * assigns a source nat ip address to an account within a network.
     * 
     * @param owner
     * @param network
     * @param callerId
     * @return
     * @throws ConcurrentOperationException
     * @throws InsufficientAddressCapacityException
     */
    PublicIp assignSourceNatIpAddress(Account owner, Network network, long callerId) throws ConcurrentOperationException, InsufficientAddressCapacityException;

    /**
     * Do all of the work of releasing public ip addresses. Note that if this method fails, there can be side effects.
     * 
     * @param userId
     * @param caller
     *            TODO
     * @param ipAddress
     * @return true if it did; false if it didn't
     */
    public boolean releasePublicIpAddress(long id, long userId, Account caller);

    /**
     * Lists IP addresses that belong to VirtualNetwork VLANs
     * 
     * @param accountId
     *            - account that the IP address should belong to
     * @param dcId
     *            - zone that the IP address should belong to
     * @param sourceNat
     *            - (optional) true if the IP address should be a source NAT address
     * @param associatedNetworkId
     *            TODO
     * @return - list of IP addresses
     */
    List<IPAddressVO> listPublicIpAddressesInVirtualNetwork(long accountId, long dcId, Boolean sourceNat, Long associatedNetworkId);

    List<NetworkVO> setupNetwork(Account owner, NetworkOfferingVO offering, DeploymentPlan plan, String name, String displayText, boolean isDefault)
            throws ConcurrentOperationException;

    List<NetworkVO> setupNetwork(Account owner, NetworkOfferingVO offering, Network predefined, DeploymentPlan plan, String name, String displayText, boolean errorIfAlreadySetup, Long domainId,
            ACLType aclType, Boolean subdomainAccess) throws ConcurrentOperationException;

    List<NetworkOfferingVO> getSystemAccountNetworkOfferings(String... offeringNames);

    void allocate(VirtualMachineProfile<? extends VMInstanceVO> vm, List<Pair<NetworkVO, NicProfile>> networks) throws InsufficientCapacityException, ConcurrentOperationException;

    void prepare(VirtualMachineProfile<? extends VMInstanceVO> profile, DeployDestination dest, ReservationContext context) throws InsufficientCapacityException, ConcurrentOperationException,
            ResourceUnavailableException;

    void release(VirtualMachineProfile<? extends VMInstanceVO> vmProfile, boolean forced);

    void cleanupNics(VirtualMachineProfile<? extends VMInstanceVO> vm);

    void expungeNics(VirtualMachineProfile<? extends VMInstanceVO> vm);

    List<? extends Nic> getNics(long vmId);

    List<NicProfile> getNicProfiles(VirtualMachine vm);

    String getNextAvailableMacAddressInNetwork(long networkConfigurationId) throws InsufficientAddressCapacityException;

    boolean applyRules(List<? extends FirewallRule> rules, boolean continueOnError) throws ResourceUnavailableException;

    public boolean validateRule(FirewallRule rule);

    List<? extends RemoteAccessVPNServiceProvider> getRemoteAccessVpnElements();

    PublicIpAddress getPublicIpAddress(long ipAddressId);

    List<? extends Vlan> listPodVlans(long podId);

    Pair<NetworkGuru, NetworkVO> implementNetwork(long networkId, DeployDestination dest, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException,
            InsufficientCapacityException;

    List<NetworkVO> listNetworksUsedByVm(long vmId, boolean isSystem);

    <T extends VMInstanceVO> void prepareNicForMigration(VirtualMachineProfile<T> vm, DeployDestination dest);

    boolean shutdownNetwork(long networkId, ReservationContext context, boolean cleanupElements);

    boolean destroyNetwork(long networkId, ReservationContext context);

    Network createGuestNetwork(long networkOfferingId, String name, String displayText, String gateway, String cidr, String vlanId, String networkDomain, Account owner, boolean isSecurityGroupEnabled, Long domainId,
            PhysicalNetwork physicalNetwork, long zoneId, ACLType aclType, Boolean subdomainAccess) throws ConcurrentOperationException, InsufficientCapacityException;

    /**
     * @throws InsufficientCapacityException
     *             Associates an ip address list to an account. The list of ip addresses are all addresses associated
     *             with the
     *             given vlan id.
     * @param userId
     * @param accountId
     * @param zoneId
     * @param vlanId
     * @throws InsufficientAddressCapacityException
     * @throws
     */
    boolean associateIpAddressListToAccount(long userId, long accountId, long zoneId, Long vlanId, Network networkToAssociateWith) throws InsufficientCapacityException, ConcurrentOperationException,
            ResourceUnavailableException;

    Nic getNicInNetwork(long vmId, long networkId);

    List<? extends Nic> getNicsForTraffic(long vmId, TrafficType type);

    Network getDefaultNetworkForVm(long vmId);

    Nic getDefaultNic(long vmId);

    List<? extends UserDataServiceProvider> getPasswordResetElements();

    boolean networkIsConfiguredForExternalNetworking(long zoneId, long networkId);

    Map<Capability, String> getNetworkServiceCapabilities(long networkId, Service service);

    boolean applyIpAssociations(Network network, boolean continueOnError) throws ResourceUnavailableException;

    boolean areServicesSupportedByNetworkOffering(long networkOfferingId, Service... services);

    NetworkVO getNetworkWithSecurityGroupEnabled(Long zoneId);

    boolean startNetwork(long networkId, DeployDestination dest, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException;

    String getIpOfNetworkElementInVirtualNetwork(long accountId, long dataCenterId);

    List<NetworkVO> listNetworksForAccount(long accountId, long zoneId, Network.GuestType type);

    IPAddressVO markIpAsUnavailable(long addrId);

    public String acquireGuestIpAddress(Network network, String requestedIp);

    String getGlobalGuestDomainSuffix();

    String getStartIpAddress(long networkId);

    boolean applyStaticNats(List<? extends StaticNat> staticNats, boolean continueOnError) throws ResourceUnavailableException;

    String getIpInNetwork(long vmId, long networkId);

    String getIpInNetworkIncludingRemoved(long vmId, long networkId);

    Long getPodIdForVlan(long vlanDbId);

    List<Long> listNetworkOfferingsForUpgrade(long networkId);

    PhysicalNetwork translateZoneIdToPhysicalNetwork(long zoneId);

    boolean isSecurityGroupSupportedInNetwork(Network network);

    boolean isProviderSupportServiceInNetwork(long networkId, Service service, Provider provider);

    boolean isProviderEnabledInPhysicalNetwork(long physicalNetowrkId, String providerName);

    String getNetworkTag(HypervisorType hType, Network network);

    List<Service> getElementServices(Provider provider);

    boolean canElementEnableIndividualServices(Provider provider);

    PhysicalNetworkServiceProvider addDefaultVirtualRouterToPhysicalNetwork(long physicalNetworkId);

    boolean areServicesSupportedInNetwork(long networkId, Service... services);

    boolean isNetworkSystem(Network network);

    boolean reallocate(VirtualMachineProfile<? extends VMInstanceVO> vm,
            DataCenterDeployment dest) throws InsufficientCapacityException, ConcurrentOperationException;

    Map<Capability, String> getNetworkOfferingServiceCapabilities(NetworkOffering offering, Service service);

    Long getPhysicalNetworkId(Network network);

    boolean getAllowSubdomainAccessGlobal();

    boolean isProviderForNetwork(Provider provider, long networkId);

    boolean isProviderForNetworkOffering(Provider provider, long networkOfferingId);

    void canProviderSupportServices(Map<Provider, Set<Service>> providersMap);

    PhysicalNetworkServiceProvider addDefaultSecurityGroupProviderToPhysicalNetwork(
            long physicalNetworkId);

    List<PhysicalNetworkSetupInfo> getPhysicalNetworkInfo(long dcId,
            HypervisorType hypervisorType);

    boolean canAddDefaultSecurityGroup();

    List<Service> listNetworkOfferingServices(long networkOfferingId);

    boolean areServicesEnabledInZone(long zoneId, NetworkOffering offering, List<Service> services);

    public Map<PublicIp, Set<Service>> getIpToServices(List<PublicIp> publicIps, boolean rulesRevoked, boolean includingFirewall);

    public Map<Provider, ArrayList<PublicIp>> getProviderToIpList(Network network, Map<PublicIp, Set<Service>> ipToServices);

    public boolean checkIpForService(IPAddressVO ip, Service service);

    void checkVirtualNetworkCidrOverlap(Long zoneId, String cidr);

    void checkCapabilityForProvider(Set<Provider> providers, Service service,
            Capability cap, String capValue);

    Provider getDefaultUniqueProviderForService(String serviceName);

    IpAddress assignSystemIp(long networkId, Account owner,
            boolean forElasticLb, boolean forElasticIp)
            throws InsufficientAddressCapacityException;

    boolean handleSystemIpRelease(IpAddress ip);

    void checkNetworkPermissions(Account owner, Network network);

    void allocateDirectIp(NicProfile nic, DataCenter dc,
            VirtualMachineProfile<? extends VirtualMachine> vm,
            Network network, String requestedIp)
            throws InsufficientVirtualNetworkCapcityException,
            InsufficientAddressCapacityException;

    String getDefaultManagementTrafficLabel(long zoneId, HypervisorType hypervisorType);

    String getDefaultStorageTrafficLabel(long zoneId, HypervisorType hypervisorType);
}
