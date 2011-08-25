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
package com.cloud.agent.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.alert.AlertManager;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.Status.Event;
import com.cloud.host.dao.HostDao;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.ConnectionConcierge;
import com.cloud.utils.db.DB;
import com.cloud.utils.time.InaccurateClock;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.VMInstanceDao;

public class AgentMonitor extends Thread implements Listener {
    private static Logger s_logger = Logger.getLogger(AgentMonitor.class);
    private long _pingTimeout;
    private HostDao _hostDao;
    private boolean _stop;
    private AgentManagerImpl _agentMgr;
    private VMInstanceDao _vmDao;
    private DataCenterDao _dcDao = null;
    private HostPodDao _podDao = null;
    private AlertManager _alertMgr;
    private long _msId;
    private ConnectionConcierge _concierge;
    @Inject
    ClusterDao _clusterDao;
    // private ConnectionConcierge _concierge;
    private Map<Long, Long> _pingMap;

    protected AgentMonitor() {
    }

    public AgentMonitor(long msId, HostDao hostDao, VMInstanceDao vmDao, DataCenterDao dcDao, HostPodDao podDao, AgentManagerImpl agentMgr, AlertManager alertMgr, long pingTimeout) {
        super("AgentMonitor");
        _msId = msId;
        _pingTimeout = pingTimeout;
        _hostDao = hostDao;
        _agentMgr = agentMgr;
        _stop = false;
        _vmDao = vmDao;
        _dcDao = dcDao;
        _podDao = podDao;
        _alertMgr = alertMgr;
        _pingMap = new ConcurrentHashMap<Long, Long>(10007);
        // try {
        // Connection conn = Transaction.getStandaloneConnectionWithException();
        // conn.setAutoCommit(true);
        // conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        // _concierge = new ConnectionConcierge("AgentMonitor", conn, true);
        // } catch (SQLException e) {
        // throw new CloudRuntimeException("Unable to get a db connection", e);
        // }

    }

    /**
     * Check if the agent is behind on ping
     * 
     * @param agentId
     *            agent or host id.
     * @return null if the agent is not kept here. true if behind; false if not.
     */
    public Boolean isAgentBehindOnPing(long agentId) {
        Long pingTime = _pingMap.get(agentId);
        if (pingTime == null) {
            return null;
        }
        return pingTime < (InaccurateClock.getTimeInSeconds() - _pingTimeout);
    }

    public Long getAgentPingTime(long agentId) {
        return _pingMap.get(agentId);
    }

    public void pingBy(long agentId) {
        Long previousTime = _pingMap.put(agentId, InaccurateClock.getTimeInSeconds());
        assert (previousTime != null) : "How does agent not have a previous time? " + agentId;
    }

    // TODO : use host machine time is not safe in clustering environment
    @Override
    public void run() {
        s_logger.info("Agent Monitor is started.");

        while (!_stop) {
            try {
                // check every 60 seconds
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                s_logger.info("Who woke me from my slumber?");
            }

            try {

                List<Long> behindAgents = findAgentsBehindOnPing();
                for (Long agentId : behindAgents) {
                    _agentMgr.disconnect(agentId, Event.PingTimeout, true);
                }

                List<HostVO> hosts = _hostDao.listByStatus(Status.PrepareForMaintenance, Status.ErrorInMaintenance);
                for (HostVO host : hosts) {
                    long hostId = host.getId();
                    DataCenterVO dcVO = _dcDao.findById(host.getDataCenterId());
                    HostPodVO podVO = _podDao.findById(host.getPodId());
                    String hostDesc = "name: " + host.getName() + " (id:" + hostId + "), availability zone: " + dcVO.getName() + ", pod: " + podVO.getName();

                    if (host.getType() != Host.Type.Storage) {
                        List<VMInstanceVO> vos = _vmDao.listByHostId(hostId);
                        List<VMInstanceVO> vosMigrating = _vmDao.listVmsMigratingFromHost(hostId);
                        if (vos.isEmpty() && vosMigrating.isEmpty()) {
                            _alertMgr.sendAlert(AlertManager.ALERT_TYPE_HOST, host.getDataCenterId(), host.getPodId(), "Migration Complete for host " + hostDesc, "Host [" + hostDesc + "] is ready for maintenance");
                            _hostDao.updateStatus(host, Event.PreparationComplete, _msId);
                        }
                    }
                }
            } catch (Throwable th) {
                s_logger.error("Caught the following exception: ", th);
            }
        }

        s_logger.info("Agent Monitor is leaving the building!");
    }

    public void signalStop() {
        _stop = true;
        interrupt();
    }

    @Override
    public boolean isRecurring() {
        return true;
    }

    @Override
    public boolean processAnswers(long agentId, long seq, Answer[] answers) {
        return false;
    }

    @Override @DB
    public boolean processCommands(long agentId, long seq, Command[] commands) {
        boolean processed = false;
        for (Command cmd : commands) {
            if (cmd instanceof PingCommand) {
                pingBy(agentId);
            }
        }
        return processed;
    }

    protected List<Long> findAgentsBehindOnPing() {
        List<Long> agentsBehind = new ArrayList<Long>();
        long cutoffTime = InaccurateClock.getTimeInSeconds() - _pingTimeout;
        for (Map.Entry<Long, Long> entry : _pingMap.entrySet()) {
            if (entry.getValue() < cutoffTime) {
                agentsBehind.add(entry.getKey());
            }
        }

        if (agentsBehind.size() > 0) {
            s_logger.info("Found the following agents behind on ping: " + agentsBehind);
        }

        return agentsBehind;
    }

    /**
     * @deprecated We're using the in-memory
     */
    @Deprecated
    protected List<HostVO> findHostsBehindOnPing() {
        long time = (System.currentTimeMillis() >> 10) - _pingTimeout;
        List<HostVO> hosts = _hostDao.findLostHosts(time);
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Found " + hosts.size() + " hosts behind on ping. pingTimeout : " + _pingTimeout +
                    ", mark time : " + time);
        }

        for (HostVO host : hosts) {
            if (host.getType().equals(Host.Type.ExternalFirewall) ||
                    host.getType().equals(Host.Type.ExternalLoadBalancer) ||
                    host.getType().equals(Host.Type.TrafficMonitor) ||
                    host.getType().equals(Host.Type.SecondaryStorage)) {
                continue;
            }

            if (host.getManagementServerId() == null || host.getManagementServerId() == _msId) {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("Asking agent mgr to investgate why host " + host.getId() +
                            " is behind on ping. last ping time: " + host.getLastPinged());
                }
                _agentMgr.disconnect(host.getId(), Event.PingTimeout, true);
            }
        }

        return hosts;
    }

    @Override
    public AgentControlAnswer processControlCommand(long agentId, AgentControlCommand cmd) {
        return null;
    }

    @Override
    public void processConnect(HostVO host, StartupCommand cmd, boolean forRebalance) {
        if (host.getType().equals(Host.Type.ExternalFirewall) ||
                host.getType().equals(Host.Type.ExternalLoadBalancer) ||
                host.getType().equals(Host.Type.TrafficMonitor) ||
                host.getType().equals(Host.Type.SecondaryStorage)) {
            return;
        }

        // NOTE: We don't use pingBy here because we're initiating.
        _pingMap.put(host.getId(), InaccurateClock.getTimeInSeconds());
    }

    @Override
    public boolean processDisconnect(long agentId, Status state) {
        _pingMap.remove(agentId);
        return true;
    }

    @Override
    public boolean processTimeout(long agentId, long seq) {
        return true;
    }

    @Override
    public int getTimeout() {
        return -1;
    }

}
