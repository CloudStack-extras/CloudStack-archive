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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.dc.Vlan.VlanType;
import com.cloud.dc.VlanVO;
import com.cloud.dc.dao.VlanDaoImpl;
import com.cloud.network.IPAddressVO;
import com.cloud.network.IpAddress.State;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.net.Ip;

@Local(value = { IPAddressDao.class })
@DB
public class IPAddressDaoImpl extends GenericDaoBase<IPAddressVO, Long> implements IPAddressDao {
    private static final Logger s_logger = Logger.getLogger(IPAddressDaoImpl.class);

    protected final SearchBuilder<IPAddressVO> AllFieldsSearch;
    protected final SearchBuilder<IPAddressVO> VlanDbIdSearchUnallocated;
    protected final GenericSearchBuilder<IPAddressVO, Integer> AllIpCount;
    protected final GenericSearchBuilder<IPAddressVO, Integer> AllocatedIpCount;
    protected final GenericSearchBuilder<IPAddressVO, Integer> AllIpCountForDashboard;    
    protected final GenericSearchBuilder<IPAddressVO, Long> AllocatedIpCountForAccount;    
    protected final VlanDaoImpl _vlanDao = ComponentLocator.inject(VlanDaoImpl.class);
    protected GenericSearchBuilder<IPAddressVO, Long> CountFreePublicIps;
    
    
    // make it public for JUnit test
    public IPAddressDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("dataCenterId", AllFieldsSearch.entity().getDataCenterId(), Op.EQ);
        AllFieldsSearch.and("ipAddress", AllFieldsSearch.entity().getAddress(), Op.EQ);
        AllFieldsSearch.and("vlan", AllFieldsSearch.entity().getVlanId(), Op.EQ);
        AllFieldsSearch.and("accountId", AllFieldsSearch.entity().getAllocatedToAccountId(), Op.EQ);
        AllFieldsSearch.and("sourceNat", AllFieldsSearch.entity().isSourceNat(), Op.EQ);
        AllFieldsSearch.and("network", AllFieldsSearch.entity().getAssociatedWithNetworkId(), Op.EQ);
        AllFieldsSearch.and("associatedWithVmId", AllFieldsSearch.entity().getAssociatedWithVmId(), Op.EQ);
        AllFieldsSearch.and("oneToOneNat", AllFieldsSearch.entity().isOneToOneNat(), Op.EQ);
        AllFieldsSearch.and("sourcenetwork", AllFieldsSearch.entity().getSourceNetworkId(), Op.EQ);
        AllFieldsSearch.and("physicalNetworkId", AllFieldsSearch.entity().getPhysicalNetworkId(), Op.EQ);
        AllFieldsSearch.done();

        VlanDbIdSearchUnallocated = createSearchBuilder();
        VlanDbIdSearchUnallocated.and("allocated", VlanDbIdSearchUnallocated.entity().getAllocatedTime(), Op.NULL);
        VlanDbIdSearchUnallocated.and("vlanDbId", VlanDbIdSearchUnallocated.entity().getVlanId(), Op.EQ);
        VlanDbIdSearchUnallocated.done();

        AllIpCount = createSearchBuilder(Integer.class);
        AllIpCount.select(null, Func.COUNT, AllIpCount.entity().getAddress());
        AllIpCount.and("dc", AllIpCount.entity().getDataCenterId(), Op.EQ);
        AllIpCount.and("vlan", AllIpCount.entity().getVlanId(), Op.EQ);
        AllIpCount.done();

        AllocatedIpCount = createSearchBuilder(Integer.class);
        AllocatedIpCount.select(null, Func.COUNT, AllocatedIpCount.entity().getAddress());
        AllocatedIpCount.and("dc", AllocatedIpCount.entity().getDataCenterId(), Op.EQ);
        AllocatedIpCount.and("vlan", AllocatedIpCount.entity().getVlanId(), Op.EQ);
        AllocatedIpCount.and("allocated", AllocatedIpCount.entity().getAllocatedTime(), Op.NNULL);
        AllocatedIpCount.done();              
        
        AllIpCountForDashboard = createSearchBuilder(Integer.class);
        AllIpCountForDashboard.select(null, Func.COUNT, AllIpCountForDashboard.entity().getAddress());
        AllIpCountForDashboard.and("dc", AllIpCountForDashboard.entity().getDataCenterId(), Op.EQ);        
        AllIpCountForDashboard.and("state", AllIpCountForDashboard.entity().getState(), SearchCriteria.Op.NEQ);                

        SearchBuilder<VlanVO> virtaulNetworkVlan = _vlanDao.createSearchBuilder();
        virtaulNetworkVlan.and("vlanType", virtaulNetworkVlan.entity().getVlanType(), SearchCriteria.Op.EQ);

        AllIpCountForDashboard.join("vlan", virtaulNetworkVlan, virtaulNetworkVlan.entity().getId(),
        		AllIpCountForDashboard.entity().getVlanId(), JoinBuilder.JoinType.INNER);
        virtaulNetworkVlan.done();
        AllIpCountForDashboard.done();

        AllocatedIpCountForAccount = createSearchBuilder(Long.class);
        AllocatedIpCountForAccount.select(null, Func.COUNT, AllocatedIpCountForAccount.entity().getAddress());
        AllocatedIpCountForAccount.and("account", AllocatedIpCountForAccount.entity().getAllocatedToAccountId(), Op.EQ);
        AllocatedIpCountForAccount.and("allocated", AllocatedIpCountForAccount.entity().getAllocatedTime(), Op.NNULL);
        AllocatedIpCountForAccount.and("network", AllocatedIpCountForAccount.entity().getAssociatedWithNetworkId(), Op.NNULL);        
        AllocatedIpCountForAccount.done();
        
        CountFreePublicIps = createSearchBuilder(Long.class);
        CountFreePublicIps.select(null, Func.COUNT, null);
        CountFreePublicIps.and("state", CountFreePublicIps.entity().getState(), SearchCriteria.Op.EQ);
        SearchBuilder<VlanVO> join = _vlanDao.createSearchBuilder();
        join.and("vlanType", join.entity().getVlanType(), Op.EQ);
        CountFreePublicIps.join("vlans", join, CountFreePublicIps.entity().getVlanId(), join.entity().getId(), JoinBuilder.JoinType.INNER);
        CountFreePublicIps.done();
    }

    @Override
    public boolean mark(long dcId, Ip ip) {
        SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("dataCenterId", dcId);
        sc.setParameters("ipAddress", ip);

        IPAddressVO vo = createForUpdate();
        vo.setAllocatedTime(new Date());
        vo.setState(State.Allocated);

        return update(vo, sc) >= 1;
    }

    @Override
    public void unassignIpAddress(long ipAddressId) {
        IPAddressVO address = createForUpdate();
        address.setAllocatedToAccountId(null);
        address.setAllocatedInDomainId(null);
        address.setAllocatedTime(null);
        address.setSourceNat(false);
        address.setOneToOneNat(false);
        address.setAssociatedWithVmId(null);
        address.setState(State.Free);
        address.setAssociatedWithNetworkId(null);
        address.setElastic(false);
        update(ipAddressId, address);
    }

    @Override
    public List<IPAddressVO> listByAccount(long accountId) {
        SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc);
    }
    
    @Override
    public List<IPAddressVO> listByVlanId(long vlanId) {
        SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("vlan", vlanId);
        return listBy(sc);
    }
    
    @Override
    public IPAddressVO findByIpAndSourceNetworkId(long networkId, String ipAddress) {
        SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("sourcenetwork", networkId);
        sc.setParameters("ipAddress", ipAddress);
        return findOneBy(sc);
    }

    @Override
    public IPAddressVO findByIpAndDcId(long dcId, String ipAddress) {
        SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("dataCenterId", dcId);
        sc.setParameters("ipAddress", ipAddress);
        return findOneBy(sc);
    }

    @Override
    public List<IPAddressVO> listByDcId(long dcId) {
        SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("dataCenterId", dcId);
        return listBy(sc);
    }
    
    @Override
    public List<IPAddressVO> listByDcIdIpAddress(long dcId, String ipAddress) {
        SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("dataCenterId", dcId);
        sc.setParameters("ipAddress", ipAddress);
        return listBy(sc);
    }
    
    @Override
    public List<IPAddressVO> listByAssociatedNetwork(long networkId, Boolean isSourceNat) {
        SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        
        if (isSourceNat != null) {
            sc.setParameters("sourceNat", isSourceNat);
        }
        
        return listBy(sc);
    }
    
    @Override 
    public List<IPAddressVO> listStaticNatPublicIps(long networkId) {
        SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("oneToOneNat", true);
        return listBy(sc);        
    }
    
    @Override
    public IPAddressVO findByAssociatedVmId(long vmId) {
        SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("associatedWithVmId", vmId);
        
        return findOneBy(sc);
    }

    @Override
    public int countIPs(long dcId, long vlanId, boolean onlyCountAllocated) {
        SearchCriteria<Integer> sc = onlyCountAllocated ? AllocatedIpCount.create() : AllIpCount.create();
        sc.setParameters("dc", dcId);
        sc.setParameters("vlan", vlanId);

        return customSearch(sc, null).get(0);
    }

    @Override
    public int countIPsForNetwork(long dcId, boolean onlyCountAllocated, VlanType vlanType) {
        SearchCriteria<Integer> sc = AllIpCountForDashboard.create();
        sc.setParameters("dc", dcId);
        if (onlyCountAllocated){
        	sc.setParameters("state", State.Free);
        }
        sc.setJoinParameters("vlan", "vlanType", vlanType.toString());
        return customSearch(sc, null).get(0);
    }

    
    @Override
    @DB
    public int countIPs(long dcId, Long accountId, String vlanId, String vlanGateway, String vlanNetmask) {
        Transaction txn = Transaction.currentTxn();
        int ipCount = 0;
        try {
            String sql = "SELECT count(*) FROM user_ip_address u INNER JOIN vlan v on (u.vlan_db_id = v.id AND v.data_center_id = ? AND v.vlan_id = ? AND v.vlan_gateway = ? AND v.vlan_netmask = ? AND u.account_id = ?)";

            PreparedStatement pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, dcId);
            pstmt.setString(2, vlanId);
            pstmt.setString(3, vlanGateway);
            pstmt.setString(4, vlanNetmask);
            pstmt.setLong(5, accountId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                ipCount = rs.getInt(1);
            }
        } catch (Exception e) {
            s_logger.warn("Exception counting IP addresses", e);
        }

        return ipCount;
    }

    @Override @DB
    public IPAddressVO markAsUnavailable(long ipAddressId) {
        SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("id", ipAddressId);
        
        IPAddressVO ip = createForUpdate();
        ip.setState(State.Releasing);
        if (update(ip, sc) != 1) {
            return null;
        }
        
        return findOneBy(sc);
    }

    @Override
    public long countAllocatedIPsForAccount(long accountId) {
    	SearchCriteria<Long> sc = AllocatedIpCountForAccount.create();
        sc.setParameters("account", accountId);
        return customSearch(sc, null).get(0);
    }
    
    @Override
    public List<IPAddressVO> listByPhysicalNetworkId(long physicalNetworkId) {
        SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);
        return listBy(sc);
    }
    
    @Override
    public long countFreeIPs() {
    	SearchCriteria<Long> sc = CountFreePublicIps.create();
    	sc.setParameters("state", State.Free);
    	sc.setJoinParameters("vlans", "vlanType", VlanType.VirtualNetwork);
        return customSearch(sc, null).get(0);       
    }
}
