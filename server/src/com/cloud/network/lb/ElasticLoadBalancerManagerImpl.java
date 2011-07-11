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
package com.cloud.network.lb;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.AgentManager.OnError;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.to.LoadBalancerTO;
import com.cloud.agent.manager.Commands;
import com.cloud.api.ApiDispatcher;
import com.cloud.api.ApiGsonHelper;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.ServerApiException;
import com.cloud.api.commands.DeployVMCmd;
import com.cloud.api.commands.ListLoadBalancerRuleInstancesCmd;
import com.cloud.api.commands.ListLoadBalancerRulesCmd;
import com.cloud.api.commands.UpdateLoadBalancerRuleCmd;
import com.cloud.async.AsyncJobManager;
import com.cloud.async.AsyncJobVO;
import com.cloud.dc.DataCenter;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.domain.Domain;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.event.EventUtils;
import com.cloud.event.UsageEventVO;
import com.cloud.event.dao.EventDao;
import com.cloud.event.dao.UsageEventDao;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IPAddressVO;
import com.cloud.network.LoadBalancerVMMapVO;
import com.cloud.network.LoadBalancerVO;
import com.cloud.network.Network;
import com.cloud.network.NetworkVO;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkManager;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.LoadBalancerVMMapDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.lb.LoadBalancingRule.LbDestination;
import com.cloud.network.router.VirtualNetworkApplianceManager;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.Purpose;
import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.network.rules.PortForwardingRule;
import com.cloud.network.rules.RulesManager;
import com.cloud.network.rules.StaticNatRule;
import com.cloud.org.Cluster;
import com.cloud.server.ManagementServer;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountService;
import com.cloud.user.User;
import com.cloud.user.UserContext;
import com.cloud.user.dao.AccountDao;
import com.cloud.uservm.UserVm;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.component.Manager;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.Nic;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.ReservationContextImpl;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;

@Local(value = { ElasticLoadBalancerManager.class })
public class ElasticLoadBalancerManagerImpl implements
        ElasticLoadBalancerManager, Manager {
    private static final Logger s_logger = Logger
            .getLogger(ElasticLoadBalancerManagerImpl.class);
    ComponentLocator locator = ComponentLocator
            .getLocator(ManagementServer.Name);

    @Inject
    IPAddressDao _ipAddressDao;
    @Inject
    AgentManager _agentMgr;
    @Inject
    NetworkManager _networkMgr;
    @Inject
    LoadBalancerDao _loadBalancerDao = null;
    @Inject
    LoadBalancingRulesManager _lbMgr;
    @Inject
    VirtualNetworkApplianceManager _routerMgr;
    @Inject
    DomainRouterDao _routerDao = null;
    @Inject
    protected HostPodDao _podDao = null;
    @Inject
    protected ClusterDao _clusterDao;
    @Inject
    DataCenterDao _dcDao = null;
    @Inject
    protected NetworkDao _networkDao;
    public AccountService _accountService = locator
            .getManager(AccountService.class);
    private final UserVmDao _userVmDao = locator.getDao(UserVmDao.class);

    String _name;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public long deployLoadBalancerVM() {  /* ELB_TODO :need to remove hardcoded network id,account id,pod id , cluster id */
        @SuppressWarnings("unchecked")
        DataCenter dc = _dcDao.findById(new Long(1));
        HostPodVO pod = _podDao.findById(new Long(1));
        Cluster clusterVO = _clusterDao.findById(new Long(1));

        NetworkVO network = _networkDao.findById(new Long(204));
        Map<VirtualMachineProfile.Param, Object> params = new HashMap<VirtualMachineProfile.Param, Object>(
                1);
        params.put(VirtualMachineProfile.Param.RestartNetwork, true);
        Account owner = _accountService.getActiveAccount("system", new Long(1));
        DeployDestination dest = new DeployDestination(dc, pod, clusterVO, null);

        s_logger.debug("Asking router manager to deploy elastic ip vm if necessary");
        // What happens really is that the DhcpElement gets the prepare request
        // first and calls
        // addVirtualMachineIntoNetwork on the VirtualNetworkApplianceManager.
        // That routine needs to know the
        // elastic ip vm's guest ip so that the dhcp server can tell the user vm
        // its default route goes through
        // the elastic ip vm.
        try {
            VirtualRouter elbVm = _routerMgr.deployELBVm(network, dest, owner,
                    params);

            s_logger.debug("ELB ip vm = " + elbVm);
            if (elbVm == null) {
                throw new InvalidParameterValueException("No VM with id '"
                        + elbVm + "' found.");
            }
            DomainRouterVO elbRouterVm = _routerDao.findById(elbVm.getId());
            String PublicIp = elbRouterVm.getGuestIpAddress();
            IPAddressVO ipvo = _ipAddressDao.findBySourceNetworkAndIp(204,PublicIp);
            ipvo.setAssociatedWithNetworkId(new Long(204)); 
            _ipAddressDao.update(ipvo.getId(), ipvo);
            return ipvo.getId();
        } catch (Throwable t) {
            String errorMsg = "Error while Deoplying Loadbalancer VM:  " + t;
            s_logger.warn(errorMsg);
            return 0;
        }

    }
    
    private boolean sendCommandsToRouter(final DomainRouterVO router,
            Commands cmds) throws AgentUnavailableException {
        Answer[] answers = null;
        try {
            answers = _agentMgr.send(router.getHostId(), cmds);
        } catch (OperationTimedoutException e) {
            s_logger.warn("Timed Out", e);
            throw new AgentUnavailableException(
                    "Unable to send commands to virtual router ",
                    router.getHostId(), e);
        }

        if (answers == null) {
            return false;
        }

        if (answers.length != cmds.size()) {
            return false;
        }

        // FIXME: Have to return state for individual command in the future
        if (answers.length > 0) {
            Answer ans = answers[0];
            return ans.getResult();
        }
        return true;
    }

    private void createApplyLoadBalancingRulesCommands(
            List<LoadBalancingRule> rules, DomainRouterVO router, Commands cmds) {

        String elbIp = "";

        LoadBalancerTO[] lbs = new LoadBalancerTO[rules.size()];
        int i = 0;
        for (LoadBalancingRule rule : rules) {
            boolean revoked = (rule.getState()
                    .equals(FirewallRule.State.Revoke));
            String protocol = rule.getProtocol();
            String algorithm = rule.getAlgorithm();

            elbIp = _networkMgr.getIp(rule.getSourceIpAddressId()).getAddress()
                    .addr();
            int srcPort = rule.getSourcePortStart();
            List<LbDestination> destinations = rule.getDestinations();
            LoadBalancerTO lb = new LoadBalancerTO(elbIp, srcPort, protocol,
                    algorithm, revoked, false, destinations);
            lbs[i++] = lb;
        }

        LoadBalancerConfigCommand cmd = new LoadBalancerConfigCommand(lbs);
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP,
                router.getPrivateIpAddress());
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME,
                router.getInstanceName());
        cmds.addCommand(cmd);

    }

    protected boolean applyLBRules(DomainRouterVO router,
            List<LoadBalancingRule> rules) throws ResourceUnavailableException {
        Commands cmds = new Commands(OnError.Continue);
        createApplyLoadBalancingRulesCommands(rules, router, cmds);
        // Send commands to router
        return sendCommandsToRouter(router, cmds);
    }

    public boolean applyFirewallRules(Network network,
            List<? extends FirewallRule> rules)
            throws ResourceUnavailableException {
        DomainRouterVO router = _routerDao.findByNetwork(network.getId());/* ELB_TODO :may have multiple LB's , need to get the correct LB based on network id and account id */
                                                                          
        if (router == null) {
            s_logger.warn("Unable to apply lb rules, virtual router doesn't exist in the network "
                    + network.getId());
            throw new ResourceUnavailableException("Unable to apply lb rules",
                    DataCenter.class, network.getDataCenterId());
        }

        if (router.getState() == State.Running) {
            if (rules != null && !rules.isEmpty()) {
                if (rules.get(0).getPurpose() == Purpose.LoadBalancing) {
                    // for elastic load balancer we have to resend all lb rules
                    // belonging to same sourceIpAddressId for the network
                    List<LoadBalancerVO> lbs = _loadBalancerDao
                            .listByNetworkId(network.getId());
                    List<LoadBalancingRule> lbRules = new ArrayList<LoadBalancingRule>();
                    for (LoadBalancerVO lb : lbs) {
                        if (lb.getSourceIpAddressId() == rules.get(0)
                                .getSourceIpAddressId()) {
                            List<LbDestination> dstList = _lbMgr
                                    .getExistingDestinations(lb.getId());
                            LoadBalancingRule loadBalancing = new LoadBalancingRule(
                                    lb, dstList);
                            lbRules.add(loadBalancing);
                        }
                    }

                    return applyLBRules(router, lbRules);
                } else {
                    s_logger.warn("Unable to apply rules of purpose: "
                            + rules.get(0).getPurpose());
                    return false;
                }
            } else {
                return true;
            }
        } else if (router.getState() == State.Stopped
                || router.getState() == State.Stopping) {
            s_logger.debug("Router is in "
                    + router.getState()
                    + ", so not sending apply firewall rules commands to the backend");
            return true;
        } else {
            s_logger.warn("Unable to apply firewall rules, virtual router is not in the right state "
                    + router.getState());
            throw new ResourceUnavailableException(
                    "Unable to apply firewall rules, virtual router is not in the right state",
                    VirtualRouter.class, router.getId());
        }
    }

    @Override
    public boolean configure(String name, Map<String, Object> params)
            throws ConfigurationException {
        _name = name;
        return true;
    }

    @Override
    public boolean start() {
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

}
