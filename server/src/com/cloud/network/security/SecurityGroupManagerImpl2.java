/**
 *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved.
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
package com.cloud.network.security;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import com.cloud.agent.api.SecurityIngressRulesCmd;
import com.cloud.agent.manager.Commands;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.network.security.SecurityGroupWork.Step;
import com.cloud.uservm.UserVm;
import com.cloud.utils.Profiler;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.mgmt.JmxUtil;
import com.cloud.vm.VirtualMachine.State;

/**
 * Same as the base class -- except it uses the abstracted security group work queue
 *
 */
@Local(value={ SecurityGroupManager.class, SecurityGroupService.class })
public class SecurityGroupManagerImpl2 extends SecurityGroupManagerImpl{
    SecurityGroupWorkQueue _workQueue = new LocalSecurityGroupWorkQueue();
    SecurityManagerMBeanImpl _mBean;
    
    WorkerThread[] _workers;

    
    protected class WorkerThread extends Thread {
        public WorkerThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (true) {
                try{
                    work(); 
                } catch (final Throwable th) {
                    s_logger.error("SG Work: Caught this throwable, ", th);
                } 
            }
        }

    }
    
    @Override
    protected void createThreadPools() {
        _workers = new WorkerThread[_numWorkerThreads];
        for (int i = 0; i < _workers.length; i++) {
            _workers[i] = new WorkerThread("SecGrp-Worker-" + i);
        }
    }

    @Override
    //@DB
    public void scheduleRulesetUpdateToHosts(List<Long> affectedVms, boolean updateSeqno, Long delayMs) {
        if (affectedVms.size() == 0) {
            return;
        }
        Set<Long> workItems = new TreeSet<Long>();
        workItems.addAll(affectedVms);
        
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Security Group Mgr v2: scheduling ruleset updates for " + affectedVms.size() + " vms " + " (unique=" + workItems.size() + "), current queue size=" + _workQueue.size());
        }

        Profiler p = new Profiler();
        p.start();
        int updated = 0;
        if (updateSeqno) {
            updated = _rulesetLogDao.createOrUpdate(workItems);
            if (updated < workItems.size()) {
                throw new CloudRuntimeException("Failed to create ruleset log entries");
            }
        }
        int newJobs = _workQueue.submitWorkForVms(workItems);
        _mBean.logScheduledDetails(workItems);
        p.stop();
        if (s_logger.isTraceEnabled()){
            s_logger.trace("Security Group Mgr v2: done scheduling ruleset updates for " + workItems.size() + " vms: num new jobs=" + 
                           newJobs + " num rows insert or updated=" + updated + " time taken=" + p.getDuration());
        }
    }

   
    

    @Override
    public boolean start() {
        for (final WorkerThread thread : _workers) {
            thread.start();
        }
        return true;
    }

    @Override
    public void work() {
        s_logger.trace("Checking the work queue");
        List<SecurityGroupWork> workItems;
        try {
            workItems = _workQueue.getWork(1);
            for (SecurityGroupWork work: workItems) {
                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("Processing " + work.getInstanceId());
                }

                try {
                    VmRulesetLogVO rulesetLog = _rulesetLogDao.findByVmId(work.getInstanceId());
                    if (rulesetLog == null) {
                        s_logger.warn("Could not find ruleset log for vm " + work.getInstanceId());
                        continue;
                    }
                    work.setLogsequenceNumber(rulesetLog.getLogsequence());
                    sendRulesetUpdates(work);
                    _mBean.logUpdateDetails(work.getInstanceId(), work.getLogsequenceNumber());
                }catch (Exception e) {
                    s_logger.error("Problem during SG work " + work, e);
                    work.setStep(Step.Error);
                }
            }
        } catch (InterruptedException e1) {
           s_logger.warn("SG work: caught InterruptException", e1);
        }


    }
    
    protected void sendRulesetUpdates(SecurityGroupWork work){
        Long userVmId = work.getInstanceId();
        UserVm vm = _userVMDao.findById(userVmId);

        if (vm != null && vm.getState() == State.Running) {
            if (s_logger.isTraceEnabled()) { 
                s_logger.trace("SecurityGroupManager v2: found vm, " + userVmId + " state=" + vm.getState());
            }
            Map<PortAndProto, Set<String>> rules = generateRulesForVM(userVmId);
            Long agentId = vm.getHostId();
            if (agentId != null) {
                SecurityIngressRulesCmd cmd = generateRulesetCmd(vm.getInstanceName(), vm.getPrivateIpAddress(), 
                        vm.getPrivateMacAddress(), vm.getId(), null, 
                        work.getLogsequenceNumber(), rules);
                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("SecurityGroupManager v2: sending ruleset update for vm " + vm.getInstanceName() + 
                                   ": num rules=" + cmd.getRuleSet().length + " num cidrs=" + cmd.getTotalNumCidrs() + " sig=" + cmd.getSignature());
                }
                Commands cmds = new Commands(cmd);
                try {
                    _agentMgr.send(agentId, cmds, _answerListener);
                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("SecurityGroupManager v2: sent ruleset updates for " + vm.getInstanceName() + " curr queue size=" + _workQueue.size());
                    }
                } catch (AgentUnavailableException e) {
                    s_logger.debug("Unable to send updates for vm: " + userVmId + "(agentid=" + agentId + ")");
                }
            }
        } else {
            if (s_logger.isTraceEnabled()) {
                if (vm != null)
                    s_logger.trace("No rules sent to vm " + vm + "state=" + vm.getState());
                else
                    s_logger.trace("Could not find vm: No rules sent to vm " + userVmId );
            }
        }
    }

    @Override
    public void cleanupFinishedWork() {
        //TODO: over time clean up op_vm_ruleset_log table for destroyed vms
    }
    
    /* 
     * Same as the superclass, except that we use the  ip address(es) returned from the join
     * made with the nics table when retrieving the SecurityGroupVmMapVO. If a vm has a single
     * nic then that nic is the default and then this query is correct. If the vm has multiple nics
     * then we get all ips, including the default nic ip. This is also probably the correct behavior.
     */
    @Override
    protected Map<PortAndProto, Set<String>> generateRulesForVM(Long userVmId) {

        Map<PortAndProto, Set<String>> allowed = new TreeMap<PortAndProto, Set<String>>();

        List<SecurityGroupVMMapVO> groupsForVm = _securityGroupVMMapDao.listByInstanceId(userVmId);
        for (SecurityGroupVMMapVO mapVO : groupsForVm) {
            List<IngressRuleVO> rules = _ingressRuleDao.listBySecurityGroupId(mapVO.getSecurityGroupId());
            for (IngressRuleVO rule : rules) {
                PortAndProto portAndProto = new PortAndProto(rule.getProtocol(), rule.getStartPort(), rule.getEndPort());
                Set<String> cidrs = allowed.get(portAndProto);
                if (cidrs == null) {
                    cidrs = new TreeSet<String>(new CidrComparator());
                }
                if (rule.getAllowedNetworkId() != null) {
                    List<SecurityGroupVMMapVO> allowedInstances = _securityGroupVMMapDao.listBySecurityGroup(rule.getAllowedNetworkId(), State.Running);
                    for (SecurityGroupVMMapVO ngmapVO : allowedInstances) {
                        //here, we differ from the superclass: instead of creating N more queries to the
                        //nics table, we use what's already there in the VO since the listBySecurityGroup already
                        //did a join with the nics table
                        String cidr = ngmapVO.getGuestIpAddress() + "/32";
                        cidrs.add(cidr);
                    }
                } else if (rule.getAllowedSourceIpCidr() != null) {
                    cidrs.add(rule.getAllowedSourceIpCidr());
                }
                if (cidrs.size() > 0) {
                    allowed.put(portAndProto, cidrs);
                }
            }
        }

        return allowed;
    }

 
    public int getQueueSize() {
        return _workQueue.size();
    }

   

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _mBean = new SecurityManagerMBeanImpl(this);
        try {
            JmxUtil.registerMBean("SecurityGroupManager", "SecurityGroupManagerImpl2", _mBean);
        } catch (Exception e){
            s_logger.error("Failed to register MBean", e);
        }
        return super.configure(name, params);
    }

}
