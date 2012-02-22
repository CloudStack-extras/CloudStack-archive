package com.cloud.network;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.ExternalNetworkResourceUsageAnswer;
import com.cloud.agent.api.ExternalNetworkResourceUsageCommand;
import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.host.DetailVO;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostDetailsDao;
import com.cloud.network.dao.ExternalFirewallDeviceDao;
import com.cloud.network.dao.ExternalLoadBalancerDeviceDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.InlineLoadBalancerNicMapDao;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkExternalFirewallDao;
import com.cloud.network.dao.NetworkExternalLoadBalancerDao;
import com.cloud.network.dao.NetworkServiceMapDao;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkServiceProviderDao;
import com.cloud.network.rules.dao.PortForwardingRulesDao;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.resource.ResourceManager;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.UserStatisticsVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserStatisticsDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.Inject;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.ExecutionException;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.NicVO;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicDao;

@Local(value = { ExternalLoadBalancerUsageManager.class })
public class ExternalLoadBalancerUsageManagerImpl implements ExternalLoadBalancerUsageManager {

    String _name;
    @Inject
    NetworkExternalLoadBalancerDao _networkExternalLBDao;
    @Inject
    ExternalLoadBalancerDeviceDao _externalLoadBalancerDeviceDao;
    @Inject
    HostDao _hostDao;
    @Inject
    DataCenterDao _dcDao;
    @Inject
    NetworkManager _networkMgr;
    @Inject
    InlineLoadBalancerNicMapDao _inlineLoadBalancerNicMapDao;
    @Inject
    NicDao _nicDao;
    @Inject
    AgentManager _agentMgr;
    @Inject
    ResourceManager _resourceMgr;
    @Inject
    IPAddressDao _ipAddressDao;
    @Inject
    VlanDao _vlanDao;
    @Inject
    NetworkOfferingDao _networkOfferingDao;
    @Inject
    AccountDao _accountDao;
    @Inject
    PhysicalNetworkDao _physicalNetworkDao;
    @Inject
    PhysicalNetworkServiceProviderDao _physicalNetworkServiceProviderDao;
    @Inject
    AccountManager _accountMgr;
    @Inject
    UserStatisticsDao _userStatsDao;
    @Inject
    NetworkDao _networkDao;
    @Inject
    DomainRouterDao _routerDao;
    @Inject
    LoadBalancerDao _loadBalancerDao;
    @Inject
    PortForwardingRulesDao _portForwardingRulesDao;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    HostDetailsDao _hostDetailDao;
    @Inject
    NetworkExternalLoadBalancerDao _networkLBDao;
    @Inject
    NetworkServiceMapDao _ntwkSrvcProviderDao;
    @Inject
    NetworkExternalFirewallDao _networkExternalFirewallDao;
    @Inject
    ExternalFirewallDeviceDao _externalFirewallDeviceDao;
    @Inject
    protected HostPodDao _podDao = null;
    
    ScheduledExecutorService _executor;
    private int _externalNetworkStatsInterval;
    private static final org.apache.log4j.Logger s_logger = Logger.getLogger(ExternalLoadBalancerUsageManagerImpl.class);
    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _externalNetworkStatsInterval = NumbersUtil.parseInt(_configDao.getValue(Config.ExternalNetworkStatsInterval.key()), 300);
        if (_externalNetworkStatsInterval > 0) {
            _executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("ExternalNetworkMonitor"));
        }
        return true;

    }

    @Override
    public boolean start() {
        if (_externalNetworkStatsInterval > 0) {
            _executor.scheduleAtFixedRate(new ExternalLoadBalancerDeviceNetworkUsageTask(), _externalNetworkStatsInterval, _externalNetworkStatsInterval, TimeUnit.SECONDS);
        }
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    private ExternalLoadBalancerDeviceVO getExternalLoadBalancerForNetwork(Network network) {
        NetworkExternalLoadBalancerVO lbDeviceForNetwork = _networkExternalLBDao.findByNetworkId(network.getId());
        if (lbDeviceForNetwork != null) {
            long lbDeviceId = lbDeviceForNetwork.getExternalLBDeviceId();
            ExternalLoadBalancerDeviceVO lbDeviceVo = _externalLoadBalancerDeviceDao.findById(lbDeviceId);
            assert (lbDeviceVo != null);
            return lbDeviceVo;
        }
        return null;
    }
    
    private boolean externalLoadBalancerIsInline(HostVO externalLoadBalancer) {
        DetailVO detail = _hostDetailDao.findDetail(externalLoadBalancer.getId(), "inline");
        return (detail != null && detail.getValue().equals("true"));
    }
    
    @Override
    public void updateExternalLoadBalancerNetworkUsageStats(long loadBalancerRuleId){
        
        LoadBalancerVO lb = _loadBalancerDao.findById(loadBalancerRuleId);
        if(lb == null){
            if(s_logger.isDebugEnabled()){
                s_logger.debug("Cannot update usage stats, LB rule is not found");
            }
            return;
        }
        long networkId = lb.getNetworkId();
        Network network = _networkDao.findById(networkId);
        if(network == null){
            if(s_logger.isDebugEnabled()){
                s_logger.debug("Cannot update usage stats, Network is not found");
            }
            return;
        }
        ExternalLoadBalancerDeviceVO lbDeviceVO = getExternalLoadBalancerForNetwork(network); 
        if (lbDeviceVO == null) {
            if(s_logger.isDebugEnabled()){
                s_logger.debug("Cannot update usage stats,  No external LB device found");
            }
            return;
        }

        // Get network stats from the external load balancer
        ExternalNetworkResourceUsageAnswer lbAnswer = null;
        HostVO externalLoadBalancer = _hostDao.findById(lbDeviceVO.getHostId());
        if (externalLoadBalancer != null) {
            ExternalNetworkResourceUsageCommand cmd = new ExternalNetworkResourceUsageCommand();
            lbAnswer = (ExternalNetworkResourceUsageAnswer) _agentMgr.easySend(externalLoadBalancer.getId(), cmd);
            if (lbAnswer == null || !lbAnswer.getResult()) {
                String details = (lbAnswer != null) ? lbAnswer.getDetails() : "details unavailable";
                String msg = "Unable to get external load balancer stats for network" + networkId + " due to: " + details + ".";
                s_logger.error(msg);
                return;
            }
        }

        long accountId = lb.getAccountId();
        AccountVO account = _accountDao.findById(accountId);
        if (account == null) {
            s_logger.debug("Skipping stats update for external LB for account with ID " + accountId);
            return;
        }

        String publicIp = _networkMgr.getIp(lb.getSourceIpAddressId()).getAddress().addr();
        DataCenterVO zone = _dcDao.findById(network.getDataCenterId());
        String statsEntryIdentifier = "account " + account.getAccountName() + ", zone " + zone.getName() + ", network ID " + networkId + ", host ID " + externalLoadBalancer.getName();            
        
        long newCurrentBytesSent = 0;
        long newCurrentBytesReceived = 0;
        
        if (publicIp != null) {
            long[] bytesSentAndReceived = null;
            statsEntryIdentifier += ", public IP: " + publicIp;
            
            if (externalLoadBalancer.getType().equals(Host.Type.ExternalLoadBalancer) && externalLoadBalancerIsInline(externalLoadBalancer)) {
                // Look up stats for the guest IP address that's mapped to the public IP address
                InlineLoadBalancerNicMapVO mapping = _inlineLoadBalancerNicMapDao.findByPublicIpAddress(publicIp);
                
                if (mapping != null) {
                    NicVO nic = _nicDao.findById(mapping.getNicId());
                    String loadBalancingIpAddress = nic.getIp4Address();
                    bytesSentAndReceived = lbAnswer.ipBytes.get(loadBalancingIpAddress);
                    
                    if (bytesSentAndReceived != null) {
                        bytesSentAndReceived[0] = 0;
                    }
                }
            } else {
                bytesSentAndReceived = lbAnswer.ipBytes.get(publicIp);
            }
            
            if (bytesSentAndReceived == null) {
                s_logger.debug("Didn't get an external network usage answer for public IP " + publicIp);
            } else {
                newCurrentBytesSent += bytesSentAndReceived[0];
                newCurrentBytesReceived += bytesSentAndReceived[1];
            }
            
            UserStatisticsVO userStats;
            final Transaction txn = Transaction.currentTxn();
            try {
                txn.start();            
                userStats = _userStatsDao.lock(accountId, zone.getId(), networkId, publicIp, externalLoadBalancer.getId(), externalLoadBalancer.getType().toString());
            
                if(userStats != null){
                    long oldNetBytesSent = userStats.getNetBytesSent();
                    long oldNetBytesReceived = userStats.getNetBytesReceived();
                    long oldCurrentBytesSent = userStats.getCurrentBytesSent();
                    long oldCurrentBytesReceived = userStats.getCurrentBytesReceived();
                    String warning = "Received an external network stats byte count that was less than the stored value. Zone ID: " + userStats.getDataCenterId() + ", account ID: " + userStats.getAccountId() + ".";
                                
                    userStats.setCurrentBytesSent(newCurrentBytesSent);
                    if (oldCurrentBytesSent > newCurrentBytesSent) {
                        s_logger.warn(warning + "Stored bytes sent: " + oldCurrentBytesSent + ", new bytes sent: " + newCurrentBytesSent + ".");            
                        userStats.setNetBytesSent(oldNetBytesSent + oldCurrentBytesSent);
                    } 
                    
                    userStats.setCurrentBytesReceived(newCurrentBytesReceived);
                    if (oldCurrentBytesReceived > newCurrentBytesReceived) {
                        s_logger.warn(warning + "Stored bytes received: " + oldCurrentBytesReceived + ", new bytes received: " + newCurrentBytesReceived + ".");                        
                        userStats.setNetBytesReceived(oldNetBytesReceived + oldCurrentBytesReceived);
                    } 
                            
                    if (_userStatsDao.update(userStats.getId(), userStats)) {
                        s_logger.debug("Successfully updated stats for " + statsEntryIdentifier);
                    } else {
                        s_logger.debug("Failed to update stats for " + statsEntryIdentifier);
                    }
                }else {
                    s_logger.warn("Unable to find user stats entry for " + statsEntryIdentifier);
                }
                
                txn.commit();
            }catch (final Exception e) {
                txn.rollback();
                throw new CloudRuntimeException("Problem getting stats after reboot/stop ", e);
            }
        }
    }

    protected class ExternalLoadBalancerDeviceNetworkUsageTask implements Runnable {

        public ExternalLoadBalancerDeviceNetworkUsageTask() {

        }

        @Override
        public void run() {
            GlobalLock scanLock = GlobalLock.getInternLock("ExternalLoadBalancerUsageManagerImpl");
            try {
                if (scanLock.lock(20)) {
                    try {
                        runExternalLoadBalancerNetworkUsageTask();
                    } finally {
                        scanLock.unlock();
                    }
                }
            } catch (Exception e) {
                s_logger.warn("Problems while getting external load balancer device usage", e);
            } finally {
                scanLock.releaseRef();
            }
        }

        private void runExternalLoadBalancerNetworkUsageTask() {
            s_logger.debug("External load balancer devices stats collector is running...");

            for (DataCenterVO zone : _dcDao.listAll()) {
                List<DomainRouterVO> domainRoutersInZone = _routerDao.listByDataCenter(zone.getId());
                if (domainRoutersInZone == null) {
                    continue;
                }
                Map<Long, ExternalNetworkResourceUsageAnswer> lbDeviceUsageAnswerMap = new HashMap<Long, ExternalNetworkResourceUsageAnswer>();
                List<Long> accountsProcessed = new ArrayList<Long>();

                for (DomainRouterVO domainRouter : domainRoutersInZone) {
                    long accountId = domainRouter.getAccountId();

                    if (accountsProcessed.contains(new Long(accountId))) {
                        if (s_logger.isTraceEnabled()) {
                            s_logger.trace("Networks for Account " + accountId + " are already processed for external network usage, so skipping usage check.");
                        }
                        continue;
                    }

                    long zoneId = zone.getId();

                    List<NetworkVO> networksForAccount = _networkDao.listBy(accountId, zoneId, Network.GuestType.Isolated);
                    if (networksForAccount == null) {
                        continue;
                    }

                    for (NetworkVO network : networksForAccount) {
                        if (!_networkMgr.networkIsConfiguredForExternalNetworking(zoneId, network.getId())) {
                            s_logger.debug("Network " + network.getId() + " is not configured for external networking, so skipping usage check.");
                            continue;
                        }

                        ExternalLoadBalancerDeviceVO lbDeviceVO = getExternalLoadBalancerForNetwork(network);
                        if (lbDeviceVO == null) {
                            continue;
                        }

                        // Get network stats from the external load balancer
                        ExternalNetworkResourceUsageAnswer lbAnswer = null;
                        HostVO externalLoadBalancer = _hostDao.findById(lbDeviceVO.getHostId());
                        if (externalLoadBalancer != null) {
                            Long lbDeviceId = new Long(externalLoadBalancer.getId());
                            if (!lbDeviceUsageAnswerMap.containsKey(lbDeviceId)) {
                                ExternalNetworkResourceUsageCommand cmd = new ExternalNetworkResourceUsageCommand();
                                lbAnswer = (ExternalNetworkResourceUsageAnswer) _agentMgr.easySend(externalLoadBalancer.getId(), cmd);
                                if (lbAnswer == null || !lbAnswer.getResult()) {
                                    String details = (lbAnswer != null) ? lbAnswer.getDetails() : "details unavailable";
                                    String msg = "Unable to get external load balancer stats for " + zone.getName() + " due to: " + details + ".";
                                    s_logger.error(msg);
                                    continue;
                                }
                                lbDeviceUsageAnswerMap.put(lbDeviceId, lbAnswer);
                            } else {
                                if (s_logger.isTraceEnabled()) {
                                    s_logger.trace("Reusing usage Answer for device id " + lbDeviceId + "for Network " + network.getId());
                                }
                                lbAnswer = lbDeviceUsageAnswerMap.get(lbDeviceId);
                            }
                        }

                        AccountVO account = _accountDao.findById(accountId);
                        if (account == null) {
                            s_logger.debug("Skipping stats update for account with ID " + accountId);
                            continue;
                        }

                        if (!manageStatsEntries(true, accountId, zoneId, network, externalLoadBalancer, lbAnswer)) {
                            continue;
                        }

                        manageStatsEntries(false, accountId, zoneId, network, externalLoadBalancer, lbAnswer);
                    }

                    accountsProcessed.add(new Long(accountId));
                }
            }
        }

        private boolean updateBytes(UserStatisticsVO userStats, long newCurrentBytesSent, long newCurrentBytesReceived) {
            long oldNetBytesSent = userStats.getNetBytesSent();
            long oldNetBytesReceived = userStats.getNetBytesReceived();
            long oldCurrentBytesSent = userStats.getCurrentBytesSent();
            long oldCurrentBytesReceived = userStats.getCurrentBytesReceived();
            String warning = "Received an external network stats byte count that was less than the stored value. Zone ID: " + userStats.getDataCenterId() + ", account ID: " + userStats.getAccountId() + ".";

            userStats.setCurrentBytesSent(newCurrentBytesSent);
            if (oldCurrentBytesSent > newCurrentBytesSent) {
                s_logger.warn(warning + "Stored bytes sent: " + oldCurrentBytesSent + ", new bytes sent: " + newCurrentBytesSent + ".");
                userStats.setNetBytesSent(oldNetBytesSent + oldCurrentBytesSent);
            }

            userStats.setCurrentBytesReceived(newCurrentBytesReceived);
            if (oldCurrentBytesReceived > newCurrentBytesReceived) {
                s_logger.warn(warning + "Stored bytes received: " + oldCurrentBytesReceived + ", new bytes received: " + newCurrentBytesReceived + ".");
                userStats.setNetBytesReceived(oldNetBytesReceived + oldCurrentBytesReceived);
            }

            return _userStatsDao.update(userStats.getId(), userStats);
        }

        // Creates a new stats entry for the specified parameters, if one doesn't already exist.
        private boolean createStatsEntry(long accountId, long zoneId, long networkId, String publicIp, long hostId) {
            HostVO host = _hostDao.findById(hostId);
            UserStatisticsVO userStats = _userStatsDao.findBy(accountId, zoneId, networkId, publicIp, hostId, host.getType().toString());
            if (userStats == null) {
                return (_userStatsDao.persist(new UserStatisticsVO(accountId, zoneId, publicIp, hostId, host.getType().toString(), networkId)) != null);
            } else {
                return true;
            }
        }

        // Updates an existing stats entry with new data from the specified usage answer.
        private boolean updateStatsEntry(long accountId, long zoneId, long networkId, String publicIp, long hostId, ExternalNetworkResourceUsageAnswer answer) {
            AccountVO account = _accountDao.findById(accountId);
            DataCenterVO zone = _dcDao.findById(zoneId);
            NetworkVO network = _networkDao.findById(networkId);
            HostVO host = _hostDao.findById(hostId);
            String statsEntryIdentifier = "account " + account.getAccountName() + ", zone " + zone.getName() + ", network ID " + networkId + ", host ID " + host.getName();

            long newCurrentBytesSent = 0;
            long newCurrentBytesReceived = 0;

            if (publicIp != null) {
                long[] bytesSentAndReceived = null;
                statsEntryIdentifier += ", public IP: " + publicIp;

                if (host.getType().equals(Host.Type.ExternalLoadBalancer) && externalLoadBalancerIsInline(host)) {
                    // Look up stats for the guest IP address that's mapped to the public IP address
                    InlineLoadBalancerNicMapVO mapping = _inlineLoadBalancerNicMapDao.findByPublicIpAddress(publicIp);

                    if (mapping != null) {
                        NicVO nic = _nicDao.findById(mapping.getNicId());
                        String loadBalancingIpAddress = nic.getIp4Address();
                        bytesSentAndReceived = answer.ipBytes.get(loadBalancingIpAddress);

                        if (bytesSentAndReceived != null) {
                            bytesSentAndReceived[0] = 0;
                        }
                    }
                } else {
                    bytesSentAndReceived = answer.ipBytes.get(publicIp);
                }

                if (bytesSentAndReceived == null) {
                    s_logger.debug("Didn't get an external network usage answer for public IP " + publicIp);
                } else {
                    newCurrentBytesSent += bytesSentAndReceived[0];
                    newCurrentBytesReceived += bytesSentAndReceived[1];
                }
            } else {
                URI broadcastURI = network.getBroadcastUri();
                if (broadcastURI == null) {
                    s_logger.debug("Not updating stats for guest network with ID " + network.getId() + " because the network is not implemented.");
                    return true;
                } else {
                    long vlanTag = Integer.parseInt(broadcastURI.getHost());
                    long[] bytesSentAndReceived = answer.guestVlanBytes.get(String.valueOf(vlanTag));

                    if (bytesSentAndReceived == null) {
                        s_logger.warn("Didn't get an external network usage answer for guest VLAN " + vlanTag);
                    } else {
                        newCurrentBytesSent += bytesSentAndReceived[0];
                        newCurrentBytesReceived += bytesSentAndReceived[1];
                    }
                }
            }

            UserStatisticsVO userStats;
            try {
                userStats = _userStatsDao.lock(accountId, zoneId, networkId, publicIp, hostId, host.getType().toString());
            } catch (Exception e) {
                s_logger.warn("Unable to find user stats entry for " + statsEntryIdentifier);
                return false;
            }

            if (updateBytes(userStats, newCurrentBytesSent, newCurrentBytesReceived)) {
                s_logger.debug("Successfully updated stats for " + statsEntryIdentifier);
                return true;
            } else {
                s_logger.debug("Failed to update stats for " + statsEntryIdentifier);
                return false;
            }
        }

        private boolean createOrUpdateStatsEntry(boolean create, long accountId, long zoneId, long networkId, String publicIp, long hostId, ExternalNetworkResourceUsageAnswer answer) {
            if (create) {
                return createStatsEntry(accountId, zoneId, networkId, publicIp, hostId);
            } else {
                return updateStatsEntry(accountId, zoneId, networkId, publicIp, hostId, answer);
            }
        }

        /*
         * Creates/updates all necessary stats entries for an account and zone.
         * Stats entries are created for source NAT IP addresses, static NAT rules, port forwarding rules, and load
         * balancing rules
         */
        private boolean manageStatsEntries(boolean create, long accountId, long zoneId, Network network,
                HostVO externalLoadBalancer, ExternalNetworkResourceUsageAnswer lbAnswer) {
            String accountErrorMsg = "Failed to update external network stats entry. Details: account ID = " + accountId;
            Transaction txn = Transaction.open(Transaction.CLOUD_DB);
            try {
                txn.start();
                String networkErrorMsg = accountErrorMsg + ", network ID = " + network.getId();

                // If an external load balancer is added, manage one entry for each load balancing rule in this network
                if (externalLoadBalancer != null && lbAnswer != null) {
                    List<LoadBalancerVO> loadBalancers = _loadBalancerDao.listByNetworkId(network.getId());
                    for (LoadBalancerVO loadBalancer : loadBalancers) {
                        String publicIp = _networkMgr.getIp(loadBalancer.getSourceIpAddressId()).getAddress().addr();
                        if (!createOrUpdateStatsEntry(create, accountId, zoneId, network.getId(), publicIp, externalLoadBalancer.getId(), lbAnswer)) {
                            throw new ExecutionException(networkErrorMsg + ", load balancing rule public IP = " + publicIp);
                        }
                    }
                }
                return txn.commit();
            } catch (Exception e) {
                s_logger.warn("Exception: ", e);
                txn.rollback();
                return false;
            } finally {
                txn.close();
            }
        }
    }
}
