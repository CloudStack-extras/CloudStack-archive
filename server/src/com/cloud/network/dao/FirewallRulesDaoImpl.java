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

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.network.IPAddressVO;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.Purpose;
import com.cloud.network.rules.FirewallRule.State;
import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.Transaction;

@Local(value=FirewallRulesDao.class) @DB(txn=false)
public class FirewallRulesDaoImpl extends GenericDaoBase<FirewallRuleVO, Long> implements FirewallRulesDao {
    private static final Logger s_logger = Logger.getLogger(FirewallRulesDaoImpl.class);
    
    protected final SearchBuilder<FirewallRuleVO> AllFieldsSearch;
    protected final SearchBuilder<FirewallRuleVO> NotRevokedSearch;
    protected final SearchBuilder<FirewallRuleVO> ReleaseSearch;
    protected SearchBuilder<FirewallRuleVO> VmSearch;
    
    protected final FirewallRulesCidrsDaoImpl _firewallRulesCidrsDao = ComponentLocator.inject(FirewallRulesCidrsDaoImpl.class);
    
    protected FirewallRulesDaoImpl() {
        super();
        
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("ipId", AllFieldsSearch.entity().getSourceIpAddressId(), Op.EQ);
        AllFieldsSearch.and("protocol", AllFieldsSearch.entity().getProtocol(), Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.and("purpose", AllFieldsSearch.entity().getPurpose(), Op.EQ);
        AllFieldsSearch.and("account", AllFieldsSearch.entity().getAccountId(), Op.EQ);
        AllFieldsSearch.and("domain", AllFieldsSearch.entity().getDomainId(), Op.EQ);
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("networkId", AllFieldsSearch.entity().getNetworkId(), Op.EQ);
        AllFieldsSearch.and("related", AllFieldsSearch.entity().getRelated(), Op.EQ);
        AllFieldsSearch.done();
        
        NotRevokedSearch = createSearchBuilder();
        NotRevokedSearch.and("ipId", NotRevokedSearch.entity().getSourceIpAddressId(), Op.EQ);
        NotRevokedSearch.and("state", NotRevokedSearch.entity().getState(), Op.NEQ);
        NotRevokedSearch.and("purpose", NotRevokedSearch.entity().getPurpose(), Op.EQ);
        NotRevokedSearch.and("protocol", NotRevokedSearch.entity().getProtocol(), Op.EQ);
        NotRevokedSearch.and("sourcePortStart", NotRevokedSearch.entity().getSourcePortStart(), Op.EQ);
        NotRevokedSearch.and("sourcePortEnd", NotRevokedSearch.entity().getSourcePortEnd(), Op.EQ);
        NotRevokedSearch.and("networkId", NotRevokedSearch.entity().getNetworkId(), Op.EQ);
        NotRevokedSearch.done();
        
        ReleaseSearch = createSearchBuilder();
        ReleaseSearch.and("protocol", ReleaseSearch.entity().getProtocol(), Op.EQ);
        ReleaseSearch.and("ipId", ReleaseSearch.entity().getSourceIpAddressId(), Op.EQ);
        ReleaseSearch.and("purpose", ReleaseSearch.entity().getPurpose(), Op.EQ);
        ReleaseSearch.and("ports", ReleaseSearch.entity().getSourcePortStart(), Op.IN);
        ReleaseSearch.done();
        
    }
    @Override
    public boolean releasePorts(long ipId, String protocol, FirewallRule.Purpose purpose, int[] ports) {
        SearchCriteria<FirewallRuleVO> sc = ReleaseSearch.create();
        sc.setParameters("protocol", protocol);
        sc.setParameters("ipId", ipId);
        sc.setParameters("purpose", purpose);
        sc.setParameters("ports", ports);
        
        int results = remove(sc);
        return results == ports.length;
    }
    
    @Override
    public List<FirewallRuleVO> listByIpAndPurpose(long ipId, FirewallRule.Purpose purpose) {
        SearchCriteria<FirewallRuleVO> sc = AllFieldsSearch.create();
        sc.setParameters("ipId", ipId);
        sc.setParameters("purpose", purpose);
        
        return listBy(sc);
    }

    @Override
    public List<FirewallRuleVO> listByIpAndPurposeAndNotRevoked(long ipId, FirewallRule.Purpose purpose) {
        SearchCriteria<FirewallRuleVO> sc = NotRevokedSearch.create();
        sc.setParameters("ipId", ipId);
        sc.setParameters("state", State.Revoke);
        
        if (purpose != null) {
            sc.setParameters("purpose", purpose);
        }
        
        return listBy(sc);
    }
    
    @Override
    public List<FirewallRuleVO> listByNetworkAndPurposeAndNotRevoked(long networkId, FirewallRule.Purpose purpose) {
        SearchCriteria<FirewallRuleVO> sc = NotRevokedSearch.create();
        sc.setParameters("networkId", networkId);
        sc.setParameters("state", State.Revoke);
        
        if (purpose != null) {
            sc.setParameters("purpose", purpose);
        }
        
        return listBy(sc);
    }
    
    @Override
    public List<FirewallRuleVO> listByNetworkAndPurpose(long networkId, FirewallRule.Purpose purpose) {
        SearchCriteria<FirewallRuleVO> sc = AllFieldsSearch.create();
        sc.setParameters("purpose", purpose);
        sc.setParameters("networkId", networkId);
        
        return listBy(sc);
    }
    
    @Override
    public boolean setStateToAdd(FirewallRuleVO rule) {
        SearchCriteria<FirewallRuleVO> sc = AllFieldsSearch.create();
        sc.setParameters("id", rule.getId());
        sc.setParameters("state", State.Staged);
        
        rule.setState(State.Add);
        
        return update(rule, sc) > 0;
    }
    
    @Override
    public boolean revoke(FirewallRuleVO rule) {
        rule.setState(State.Revoke);
        return update(rule.getId(), rule);
    }
    
    @Override
    public List<FirewallRuleVO> listStaticNatByVmId(long vmId) {   
        IPAddressDao _ipDao = ComponentLocator.getLocator("management-server").getDao(IPAddressDao.class);
        
        if (VmSearch == null) {         
            SearchBuilder<IPAddressVO> IpSearch = _ipDao.createSearchBuilder();
            IpSearch.and("associatedWithVmId", IpSearch.entity().getAssociatedWithVmId(), SearchCriteria.Op.EQ);
            IpSearch.and("oneToOneNat", IpSearch.entity().isOneToOneNat(), SearchCriteria.Op.NNULL);
            
            VmSearch = createSearchBuilder();
            VmSearch.and("purpose", VmSearch.entity().getPurpose(), Op.EQ);
            VmSearch.join("ipSearch", IpSearch, VmSearch.entity().getSourceIpAddressId(), IpSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            VmSearch.done();
        }      
          
        SearchCriteria<FirewallRuleVO> sc = VmSearch.create();
        sc.setParameters("purpose", Purpose.StaticNat);
        sc.setJoinParameters("ipSearch", "associatedWithVmId", vmId);
        
        return listBy(sc);
    }
    
    @Override @DB
    public FirewallRuleVO persist(FirewallRuleVO firewallRule) {        
        Transaction txn = Transaction.currentTxn();
        txn.start();
        
        FirewallRuleVO dbfirewallRule = super.persist(firewallRule);
        saveSourceCidrs(firewallRule);
        
        txn.commit();
        return dbfirewallRule;
    }
    
    
    public void saveSourceCidrs(FirewallRuleVO firewallRule) {
        List<String> cidrlist = firewallRule.getSourceCidrList();
        if (cidrlist == null) {
            return;
        }
        _firewallRulesCidrsDao.persist(firewallRule.getId(), cidrlist);
    }
    

    @Override
    public List<FirewallRuleVO> listByIpPurposeAndProtocolAndNotRevoked(long ipAddressId, Integer startPort, Integer endPort, String protocol, FirewallRule.Purpose purpose) {
        SearchCriteria<FirewallRuleVO> sc = NotRevokedSearch.create();
        sc.setParameters("ipId", ipAddressId);
        sc.setParameters("state", State.Revoke);
        
        if (purpose != null) {
            sc.setParameters("purpose", purpose);
        }
        
        if (protocol != null) {
            sc.setParameters("protocol", protocol);
        }
        
        sc.setParameters("sourcePortStart", startPort);
       
        sc.setParameters("sourcePortEnd", endPort);
      
        return listBy(sc);
    }
    
    @Override
    public FirewallRuleVO findByRelatedId(long ruleId) {
        SearchCriteria<FirewallRuleVO> sc = AllFieldsSearch.create();
        sc.setParameters("related", ruleId);
        sc.setParameters("purpose", Purpose.Firewall);
        
        return findOneBy(sc);
    }
}
