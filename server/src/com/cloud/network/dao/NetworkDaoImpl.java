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
package com.cloud.network.dao;

import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ejb.Local;
import javax.persistence.TableGenerator;

import com.cloud.network.Network;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkAccountDaoImpl;
import com.cloud.network.NetworkAccountVO;
import com.cloud.network.NetworkDomainVO;
import com.cloud.network.NetworkServiceMapVO;
import com.cloud.network.NetworkVO;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.Mode;
import com.cloud.network.Networks.TrafficType;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.JoinBuilder.JoinType;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.SequenceFetcher;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.net.NetUtils;

@Local(value = NetworkDao.class)
@DB(txn = false)
public class NetworkDaoImpl extends GenericDaoBase<NetworkVO, Long> implements NetworkDao {
    final SearchBuilder<NetworkVO> AllFieldsSearch;
    final SearchBuilder<NetworkVO> AccountSearch;
    final SearchBuilder<NetworkVO> RelatedConfigSearch;
    final SearchBuilder<NetworkVO> AccountNetworkSearch;
    final SearchBuilder<NetworkVO> ZoneBroadcastUriSearch;
    final SearchBuilder<NetworkVO> ZoneSecurityGroupSearch;
    final GenericSearchBuilder<NetworkVO, Long> CountByOfferingId;
    final SearchBuilder<NetworkVO> PhysicalNetworkSearch;
    final SearchBuilder<NetworkVO> SecurityGroupSearch;
    private final GenericSearchBuilder<NetworkVO, Integer> NetworksCount;


    NetworkAccountDaoImpl _accountsDao = ComponentLocator.inject(NetworkAccountDaoImpl.class);
    NetworkDomainDaoImpl _domainsDao = ComponentLocator.inject(NetworkDomainDaoImpl.class);
    NetworkOpDaoImpl _opDao = ComponentLocator.inject(NetworkOpDaoImpl.class);
    NetworkServiceMapDaoImpl _ntwkSvcMap = ComponentLocator.inject(NetworkServiceMapDaoImpl.class);


    final TableGenerator _tgMacAddress;
    Random _rand = new Random(System.currentTimeMillis());
    long _prefix = 0x2;

    protected NetworkDaoImpl() {
        super();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("trafficType", AllFieldsSearch.entity().getTrafficType(), Op.EQ);
        AllFieldsSearch.and("cidr", AllFieldsSearch.entity().getCidr(), Op.EQ);
        AllFieldsSearch.and("broadcastType", AllFieldsSearch.entity().getBroadcastDomainType(), Op.EQ);
        AllFieldsSearch.and("offering", AllFieldsSearch.entity().getNetworkOfferingId(), Op.EQ);
        AllFieldsSearch.and("datacenter", AllFieldsSearch.entity().getDataCenterId(), Op.EQ);
        AllFieldsSearch.and("account", AllFieldsSearch.entity().getAccountId(), Op.EQ);
        AllFieldsSearch.and("related", AllFieldsSearch.entity().getRelated(), Op.EQ);
        AllFieldsSearch.and("guestType", AllFieldsSearch.entity().getGuestType(), Op.EQ);
        AllFieldsSearch.and("physicalNetwork", AllFieldsSearch.entity().getPhysicalNetworkId(), Op.EQ);
        AllFieldsSearch.done();

        AccountSearch = createSearchBuilder();
        AccountSearch.and("offering", AccountSearch.entity().getNetworkOfferingId(), Op.EQ);
        SearchBuilder<NetworkAccountVO> join = _accountsDao.createSearchBuilder();
        join.and("account", join.entity().getAccountId(), Op.EQ);
        AccountSearch.join("accounts", join, AccountSearch.entity().getId(), join.entity().getNetworkId(), JoinBuilder.JoinType.INNER);
        AccountSearch.and("datacenter", AccountSearch.entity().getDataCenterId(), Op.EQ);
        AccountSearch.and("cidr", AccountSearch.entity().getCidr(), Op.EQ);
        AccountSearch.done();

        RelatedConfigSearch = createSearchBuilder();
        RelatedConfigSearch.and("offering", RelatedConfigSearch.entity().getNetworkOfferingId(), Op.EQ);
        RelatedConfigSearch.and("datacenter", RelatedConfigSearch.entity().getDataCenterId(), Op.EQ);
        SearchBuilder<NetworkAccountVO> join2 = _accountsDao.createSearchBuilder();
        join2.and("account", join2.entity().getAccountId(), Op.EQ);
        RelatedConfigSearch.join("account", join2, join2.entity().getNetworkId(), RelatedConfigSearch.entity().getId(), JoinType.INNER);
        RelatedConfigSearch.done();

        AccountNetworkSearch = createSearchBuilder();
        AccountNetworkSearch.and("networkId", AccountNetworkSearch.entity().getId(), Op.EQ);
        SearchBuilder<NetworkAccountVO> mapJoin = _accountsDao.createSearchBuilder();
        mapJoin.and("accountId", mapJoin.entity().getAccountId(), Op.EQ);
        AccountNetworkSearch.join("networkSearch", mapJoin, AccountNetworkSearch.entity().getId(), mapJoin.entity().getNetworkId(), JoinBuilder.JoinType.INNER);
        AccountNetworkSearch.done();

        ZoneBroadcastUriSearch = createSearchBuilder();
        ZoneBroadcastUriSearch.and("dataCenterId", ZoneBroadcastUriSearch.entity().getDataCenterId(), Op.EQ);
        ZoneBroadcastUriSearch.and("broadcastUri", ZoneBroadcastUriSearch.entity().getBroadcastUri(), Op.EQ);
        ZoneBroadcastUriSearch.done();

        ZoneSecurityGroupSearch = createSearchBuilder();
        ZoneSecurityGroupSearch.and("dataCenterId", ZoneSecurityGroupSearch.entity().getDataCenterId(), Op.EQ);
        SearchBuilder<NetworkServiceMapVO> join1 = _ntwkSvcMap.createSearchBuilder();
        join1.and("service", join1.entity().getService(), Op.EQ);
        ZoneSecurityGroupSearch.join("services", join1, ZoneSecurityGroupSearch.entity().getId(), join1.entity().getNetworkId(), JoinBuilder.JoinType.INNER);
        ZoneSecurityGroupSearch.done();

        CountByOfferingId = createSearchBuilder(Long.class);
        CountByOfferingId.select(null, Func.COUNT, CountByOfferingId.entity().getId());
        CountByOfferingId.and("offeringId", CountByOfferingId.entity().getNetworkOfferingId(), Op.EQ);
        CountByOfferingId.and("removed", CountByOfferingId.entity().getRemoved(), Op.NULL);
        CountByOfferingId.done();

        PhysicalNetworkSearch = createSearchBuilder();
        PhysicalNetworkSearch.and("physicalNetworkId", PhysicalNetworkSearch.entity().getPhysicalNetworkId(), Op.EQ);
        PhysicalNetworkSearch.done();

        SecurityGroupSearch = createSearchBuilder();
        SearchBuilder<NetworkServiceMapVO> join3 = _ntwkSvcMap.createSearchBuilder();
        join3.and("service", join3.entity().getService(), Op.EQ);
        SecurityGroupSearch.join("services", join3, SecurityGroupSearch.entity().getId(), join3.entity().getNetworkId(), JoinBuilder.JoinType.INNER);
        SecurityGroupSearch.done();
        
        NetworksCount = createSearchBuilder(Integer.class);
        NetworksCount.select(null, Func.COUNT, NetworksCount.entity().getId());
        NetworksCount.and("networkOfferingId", NetworksCount.entity().getNetworkOfferingId(), SearchCriteria.Op.EQ);
        NetworksCount.done();

        _tgMacAddress = _tgs.get("macAddress");

    }

    @Override
    public List<NetworkVO> listBy(long accountId, long dataCenterId, Network.GuestType type) {
        SearchCriteria<NetworkVO> sc = AllFieldsSearch.create();
        sc.setParameters("datacenter", dataCenterId);
        sc.setParameters("account", accountId);
        if (type != null) {
            sc.setParameters("guestType", type);
        }
        return listBy(sc, null);
    }

    public List<NetworkVO> findBy(TrafficType trafficType, Mode mode, BroadcastDomainType broadcastType, long networkOfferingId, long dataCenterId) {
        SearchCriteria<NetworkVO> sc = AllFieldsSearch.create();
        sc.setParameters("trafficType", trafficType);
        sc.setParameters("broadcastType", broadcastType);
        sc.setParameters("offering", networkOfferingId);
        sc.setParameters("datacenter", dataCenterId);

        return search(sc, null);
    }

    @Override
    public List<NetworkVO> listBy(long accountId, long offeringId, long dataCenterId) {
        SearchCriteria<NetworkVO> sc = AccountSearch.create();
        sc.setParameters("offering", offeringId);
        sc.setJoinParameters("accounts", "account", accountId);
        sc.setParameters("datacenter", dataCenterId);

        return listBy(sc);
    }

    @Override
    public List<NetworkVO> listBy(long accountId, long dataCenterId, String cidr) {
        SearchCriteria<NetworkVO> sc = AccountSearch.create();
        sc.setJoinParameters("accounts", "account", accountId);
        sc.setParameters("datacenter", dataCenterId);
        sc.setParameters("cidr", cidr);

        return listBy(sc);
    }

    @Override
    @DB
    public NetworkVO persist(NetworkVO network, boolean gc, Map<String, String> serviceProviderMap) {
        Transaction txn = Transaction.currentTxn();
        txn.start();

        // 1) create network
        NetworkVO newNetwork = super.persist(network);
        // 2) add account to the network
        addAccountToNetwork(network.getId(), network.getAccountId(), true);
        // 3) add network to gc monitor table
        NetworkOpVO op = new NetworkOpVO(network.getId(), gc);
        _opDao.persist(op);
        // 4) add services/providers for the network
        persistNetworkServiceProviders(newNetwork.getId(), serviceProviderMap);

        txn.commit();
        return newNetwork;
    }

    @Override
    @DB
    public boolean update(Long networkId, NetworkVO network, Map<String, String> serviceProviderMap) {
        Transaction txn = Transaction.currentTxn();
        txn.start();

        super.update(networkId, network);
        if (serviceProviderMap != null) {
            _ntwkSvcMap.deleteByNetworkId(networkId);
            persistNetworkServiceProviders(networkId, serviceProviderMap);
        }

        txn.commit();
        return true;
    }

    @Override
    @DB
    public void persistNetworkServiceProviders(long networkId, Map<String, String> serviceProviderMap) {
        Transaction txn = Transaction.currentTxn();
        txn.start();
        for (String service : serviceProviderMap.keySet()) {
            NetworkServiceMapVO serviceMap = new NetworkServiceMapVO(networkId, Service.getService(service), Provider.getProvider(serviceProviderMap.get(service)));
            _ntwkSvcMap.persist(serviceMap);
        }
        txn.commit();
    }

    protected void addAccountToNetwork(long networkId, long accountId, boolean isOwner) {
        NetworkAccountVO account = new NetworkAccountVO(networkId, accountId, isOwner);
        _accountsDao.persist(account);
    }

    @Override
    public SearchBuilder<NetworkAccountVO> createSearchBuilderForAccount() {
        return _accountsDao.createSearchBuilder();
    }

    @Override
    public List<NetworkVO> getNetworksForOffering(long offeringId, long dataCenterId, long accountId) {
        SearchCriteria<NetworkVO> sc = RelatedConfigSearch.create();
        sc.setParameters("offering", offeringId);
        sc.setParameters("dc", dataCenterId);
        sc.setJoinParameters("account", "account", accountId);
        return search(sc, null);
    }

    @Override
    public String getNextAvailableMacAddress(long networkConfigId) {
        SequenceFetcher fetch = SequenceFetcher.getInstance();

        long seq = fetch.getNextSequence(Long.class, _tgMacAddress, networkConfigId);
        seq = seq | _prefix << 40 | ((_rand.nextInt(Short.MAX_VALUE) << 16) & 0x00000000ffff0000l);
        return NetUtils.long2Mac(seq);
    }

    @Override
    public List<NetworkVO> listBy(long accountId, long networkId) {
        SearchCriteria<NetworkVO> sc = AccountNetworkSearch.create();
        sc.setParameters("networkId", networkId);
        sc.setJoinParameters("networkSearch", "accountId", accountId);
        return listBy(sc);
    }

    @Override
    public List<NetworkVO> listBy(long zoneId, String broadcastUri) {
        SearchCriteria<NetworkVO> sc = ZoneBroadcastUriSearch.create();
        sc.setParameters("dataCenterId", zoneId);
        sc.setParameters("broadcastUri", broadcastUri);
        return search(sc, null);
    }

    @Override
    public List<NetworkVO> listByZone(long zoneId) {
        SearchCriteria<NetworkVO> sc = ZoneBroadcastUriSearch.create();
        sc.setParameters("dataCenterId", zoneId);
        return search(sc, null);
    }

    @Override
    public List<NetworkVO> listByZoneSecurityGroup(Long zoneId) {
        SearchCriteria<NetworkVO> sc = ZoneSecurityGroupSearch.create();
        if (zoneId != null) {
            sc.setParameters("dataCenterId", zoneId);
        }
        sc.setJoinParameters("services", "service", Service.SecurityGroup.getName());
        return search(sc, null);
    }

    @Override
    public void changeActiveNicsBy(long networkId, int count) {
        _opDao.changeActiveNicsBy(networkId, count);
    }

    @Override
    public int getActiveNicsIn(long networkId) {
        return _opDao.getActiveNics(networkId);
    }

    @Override
    public List<Long> findNetworksToGarbageCollect() {
        return _opDao.getNetworksToGarbageCollect();
    }

    @Override
    public void clearCheckForGc(long networkId) {
        _opDao.clearCheckForGc(networkId);
    }
    
    @Override
    public void setCheckForGc(long networkId) {
        _opDao.setCheckForGc(networkId);
    }

    @Override
    public List<NetworkVO> listByOwner(long ownerId) {
        SearchCriteria<NetworkVO> sc = AllFieldsSearch.create();
        sc.setParameters("account", ownerId);
        return listBy(sc);
    }

    @Override
    public void addDomainToNetwork(long networkId, long domainId, Boolean subdomainAccess) {
        addDomainToNetworknetwork(networkId, domainId, subdomainAccess);
    }

    protected void addDomainToNetworknetwork(long networkId, long domainId, Boolean subdomainAccess) {
        NetworkDomainVO domain = new NetworkDomainVO(networkId, domainId, subdomainAccess);
        _domainsDao.persist(domain);
    }

    @Override
    public Long getNetworkCountByOfferingId(long offeringId) {
        SearchCriteria<Long> sc = CountByOfferingId.create();
        sc.setParameters("offeringId", offeringId);
        List<Long> results = customSearch(sc, null);
        return results.get(0);
    }

    @Override
    public List<NetworkVO> listSecurityGroupEnabledNetworks() {
        SearchCriteria<NetworkVO> sc = SecurityGroupSearch.create();
        sc.setJoinParameters("services", "service", Service.SecurityGroup.getName());
        return listBy(sc);
    }

    @Override
    public List<NetworkVO> listByPhysicalNetwork(long physicalNetworkId) {
        SearchCriteria<NetworkVO> sc = PhysicalNetworkSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);
        return listBy(sc);
    }

    @Override
    public List<NetworkVO> listByPhysicalNetworkTrafficType(long physicalNetworkId, TrafficType trafficType) {
        SearchCriteria<NetworkVO> sc = AllFieldsSearch.create();
        sc.setParameters("trafficType", trafficType);
        sc.setParameters("physicalNetworkId", physicalNetworkId);
        return listBy(sc);
    }

    @Override
    public List<NetworkVO> listByPhysicalNetworkAndProvider(long physicalNetworkId, String providerName) {
        SearchBuilder<NetworkServiceMapVO> svcProviderMapSearch = _ntwkSvcMap.createSearchBuilder();
        NetworkServiceMapVO svcProviderEntry = svcProviderMapSearch.entity();
        svcProviderMapSearch.and("Provider", svcProviderMapSearch.entity().getProvider(), SearchCriteria.Op.EQ);

        SearchBuilder<NetworkVO> networksSearch = createSearchBuilder();
        networksSearch.and("physicalNetworkId", networksSearch.entity().getPhysicalNetworkId(), Op.EQ);
        networksSearch.join("svcProviderMapSearch", svcProviderMapSearch, networksSearch.entity().getId(), svcProviderEntry.getNetworkId(), JoinBuilder.JoinType.INNER);

        SearchCriteria<NetworkVO> sc = networksSearch.create();
        sc.setJoinParameters("svcProviderMapSearch", "Provider", providerName);
        sc.setParameters("physicalNetworkId", physicalNetworkId);

        return listBy(sc);
    }

    @Override
    public List<NetworkVO> listBy(long accountId, long dataCenterId, Network.GuestType type, TrafficType trafficType) {
        SearchCriteria<NetworkVO> sc = AllFieldsSearch.create();
        sc.setParameters("datacenter", dataCenterId);
        sc.setParameters("account", accountId);
        sc.setParameters("guestType", type);
        sc.setParameters("trafficType", trafficType);

        return listBy(sc, null);
    }

    @Override
    public List<NetworkVO> listByZoneAndTrafficType(long zoneId, TrafficType trafficType) {
        SearchCriteria<NetworkVO> sc = AllFieldsSearch.create();
        sc.setParameters("datacenter", zoneId);
        sc.setParameters("trafficType", trafficType);

        return listBy(sc, null);
    }
    
    @Override
    public int getNetworkCountByNetworkOffId(long networkOfferingId) {
        SearchCriteria<Integer> sc = NetworksCount.create();
        sc.setParameters("networkOfferingId", networkOfferingId);
        List<Integer> count = customSearch(sc, null);
        return count.get(0);
    }

}
