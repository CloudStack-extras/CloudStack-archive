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

package com.cloud.cluster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.AgentManager.OnError;
import com.cloud.agent.Listener;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.ChangeAgentCommand;
import com.cloud.agent.api.Command;
import com.cloud.agent.manager.Commands;
import com.cloud.cluster.agentlb.dao.HostTransferMapDao;
import com.cloud.cluster.dao.ManagementServerHostDao;
import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.host.HostVO;
import com.cloud.host.Status.Event;
import com.cloud.host.dao.HostDao;
import com.cloud.serializer.GsonHelper;
import com.cloud.utils.DateUtil;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Profiler;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.component.Adapters;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.ConnectionConcierge;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.events.SubscriptionMgr;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.ExceptionUtil;
import com.cloud.utils.mgmt.JmxUtil;
import com.cloud.utils.net.NetUtils;
import com.google.gson.Gson;

@Local(value = { ClusterManager.class })
public class ClusterManagerImpl implements ClusterManager {
    private static final Logger s_logger = Logger.getLogger(ClusterManagerImpl.class);


    private static final int EXECUTOR_SHUTDOWN_TIMEOUT = 1000; // 1 second


    private final List<ClusterManagerListener> _listeners = new ArrayList<ClusterManagerListener>();
    private final Map<Long, ManagementServerHostVO> _activePeers = new HashMap<Long, ManagementServerHostVO>();
    private int _heartbeatInterval = ClusterManager.DEFAULT_HEARTBEAT_INTERVAL;
    private int _heartbeatThreshold = ClusterManager.DEFAULT_HEARTBEAT_THRESHOLD;

    private final Map<String, ClusterService> _clusterPeers;
    private final Map<String, Listener> _asyncCalls;
    private final Gson _gson;

    @Inject
    private AgentManager _agentMgr;
    @Inject
    private ClusteredAgentRebalanceService _rebalanceService;

    private final ScheduledExecutorService _heartbeatScheduler = Executors.newScheduledThreadPool(1, new NamedThreadFactory("Cluster-Heartbeat"));
    private final ExecutorService _notificationExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("Cluster-Notification"));
    private final List<ClusterManagerMessage> _notificationMsgs = new ArrayList<ClusterManagerMessage>();
    private ConnectionConcierge _heartbeatConnection = null;

    private final ExecutorService _executor;

    private ClusterServiceAdapter _currentServiceAdapter;

    private ManagementServerHostDao _mshostDao;
    private HostDao _hostDao;
    private HostTransferMapDao _hostTransferDao;

    //
    // pay attention to _mshostId and _msid
    // _mshostId is the primary key of management host table
    // _msid is the unique persistent identifier that peer name is based upon
    //
    private Long _mshostId = null;
    protected long _msId = ManagementServerNode.getManagementServerId();
    protected long _runId = System.currentTimeMillis();

    private boolean _peerScanInited = false;

    private String _name;
    private String _clusterNodeIP = "127.0.0.1";
    private boolean _agentLBEnabled = false;
    private double _connectedAgentsThreshold = 0.7;
    private static boolean _agentLbHappened = false;
    
    
    public ClusterManagerImpl() {
        _clusterPeers = new HashMap<String, ClusterService>();
        _asyncCalls = new HashMap<String, Listener>();

        _gson = GsonHelper.getGson();

        // executor to perform remote-calls in another thread context, to avoid potential
        // recursive remote calls between nodes
        //
        _executor = Executors.newCachedThreadPool(new NamedThreadFactory("Cluster-Worker"));
    }

    @Override
    public Answer[] sendToAgent(Long hostId, Command[] cmds, boolean stopOnError) throws AgentUnavailableException, OperationTimedoutException {
        Commands commands = new Commands(stopOnError ? OnError.Stop : OnError.Continue);
        for (Command cmd : cmds) {
            commands.addCommand(cmd);
        }
        return _agentMgr.send(hostId, commands);
    }

    @Override
    public long sendToAgent(Long hostId, Command[] cmds, boolean stopOnError, Listener listener) throws AgentUnavailableException {
        Commands commands = new Commands(stopOnError ? OnError.Stop : OnError.Continue);
        for (Command cmd : cmds) {
            commands.addCommand(cmd);
        }
        return _agentMgr.send(hostId, commands, listener);
    }

    @Override
    public boolean executeAgentUserRequest(long agentId, Event event) throws AgentUnavailableException {
        return _agentMgr.executeUserRequest(agentId, event);
    }

    @Override
    public Boolean propagateAgentEvent(long agentId, Event event) throws AgentUnavailableException {
        final String msPeer = getPeerName(agentId);
        if (msPeer == null) {
            return null;
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Propagating agent change request event:" + event.toString() + " to agent:" + agentId);
        }
        Command[] cmds = new Command[1];
        cmds[0] = new ChangeAgentCommand(agentId, event);

        Answer[] answers = execute(msPeer, agentId, cmds, true);
        if (answers == null) {
            throw new AgentUnavailableException(agentId);
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Result for agent change is " + answers[0].getResult());
        }

        return answers[0].getResult();
    }

    /**
     * called by DatabaseUpgradeChecker to see if there are other peers running.
     * 
     * @param notVersion
     *            If version is passed in, the peers CANNOT be running at this version. If version is null, return true if any
     *            peer is running regardless of version.
     * @return true if there are peers running and false if not.
     */
    public static final boolean arePeersRunning(String notVersion) {
        return false; // TODO: Leaving this for Kelven to take care of.
    }

    @Override
    public void broadcast(long agentId, Command[] cmds) {
        Date cutTime = DateUtil.currentGMTTime();

        List<ManagementServerHostVO> peers = _mshostDao.getActiveList(new Date(cutTime.getTime() - _heartbeatThreshold));
        for (ManagementServerHostVO peer : peers) {
            String peerName = Long.toString(peer.getMsid());
            if (getSelfPeerName().equals(peerName)) {
                continue; // Skip myself.
            }
            try {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Forwarding " + cmds[0].toString() + " to " + peer.getMsid());
                }
                Answer[] answers = execute(peerName, agentId, cmds, true);
            } catch (Exception e) {
                s_logger.warn("Caught exception while talkign to " + peer.getMsid());
            }
        }
    }

    @Override
    public Answer[] execute(String strPeer, long agentId, Command [] cmds, boolean stopOnError) {
        ClusterService peerService =  null;

        if(s_logger.isDebugEnabled()) {
            s_logger.debug(getSelfPeerName() + " -> " + strPeer + "." + agentId + " " +
                    _gson.toJson(cmds, Command[].class));
        }

        for(int i = 0; i < 2; i++) {
            try {
                peerService = getPeerService(strPeer);
            } catch (RemoteException e) {
                s_logger.error("Unable to get cluster service on peer : " + strPeer);
            }

            if(peerService != null) {
                try {
                    if(s_logger.isDebugEnabled()) {
                        s_logger.debug("Send " + getSelfPeerName() + " -> " + strPeer + "." + agentId + " to remote");
                    }

                    long startTick = System.currentTimeMillis();
                    String strResult = peerService.execute(getSelfPeerName(), agentId, _gson.toJson(cmds, Command[].class), stopOnError);
                    if(s_logger.isDebugEnabled()) {
                        s_logger.debug("Completed " + getSelfPeerName() + " -> " + strPeer + "." + agentId + "in " +
                                (System.currentTimeMillis() - startTick) + " ms, result: " + strResult);
                    }

                    if(strResult != null) {
                        try {
                            return _gson.fromJson(strResult, Answer[].class);
                        } catch(Throwable e) {
                            s_logger.error("Exception on parsing gson package from remote call to " + strPeer);
                        }
                    }
                    return null;
                } catch (RemoteException e) {
                    invalidatePeerService(strPeer);
                    if(s_logger.isInfoEnabled()) {
                        s_logger.info("Exception on remote execution, peer: " + strPeer + ", iteration: "
                                + i + ", exception message :" + e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    @Override
    public long executeAsync(String strPeer, long agentId, Command[] cmds, boolean stopOnError, Listener listener) {
        ClusterService peerService =  null;

        if(s_logger.isDebugEnabled()) {
            s_logger.debug("Async " + getSelfPeerName() + " -> " + strPeer + "." + agentId + " " +
                    _gson.toJson(cmds, Command[].class));
        }

        for(int i = 0; i < 2; i++) {
            try {
                peerService = getPeerService(strPeer);
            } catch (RemoteException e) {
                s_logger.error("Unable to get cluster service on peer : " + strPeer);
            }
            if(peerService != null) {
                try {
                    long seq = 0;
                    synchronized(String.valueOf(agentId).intern()) {
                        if(s_logger.isDebugEnabled()) {
                            s_logger.debug("Send Async " + getSelfPeerName() + " -> " + strPeer + "." + agentId + " to remote");
                        }

                        long startTick = System.currentTimeMillis();
                        seq = peerService.executeAsync(getSelfPeerName(), agentId, _gson.toJson(cmds, Command[].class), stopOnError);
                        if(seq > 0) {
                            if(s_logger.isDebugEnabled()) {
                                s_logger.debug("Completed Async " + getSelfPeerName() + " -> " + strPeer + "." + agentId
                                        + " in " + (System.currentTimeMillis() - startTick) + " ms"
                                        + ", register local listener " + strPeer + "/" + seq);
                            }

                            registerAsyncCall(strPeer, seq, listener);
                        } else {
                            s_logger.warn("Completed Async " + getSelfPeerName() + " -> " + strPeer + "." + agentId
                                    + " in " + (System.currentTimeMillis() - startTick) + " ms, return indicates failure, seq: " + seq);
                        }
                    }
                    return seq;
                } catch (RemoteException e) {
                    invalidatePeerService(strPeer);

                    if(s_logger.isInfoEnabled()) {
                        s_logger.info("Exception on remote execution -> " + strPeer + ", iteration : " + i);
                    }
                }
            }
        }

        return 0L;
    }

    @Override
    public boolean onAsyncResult(String executingPeer, long agentId, long seq, Answer[] answers) {
        if(s_logger.isDebugEnabled()) {
            s_logger.debug("Process Async-call result from remote peer " + executingPeer + ", {" +
                    agentId + "-" + seq + "} answers: " + (answers != null ? _gson.toJson(answers, Answer[].class): "null"));
        }

        Listener listener = null;
        synchronized(String.valueOf(agentId).intern()) {
            // need to synchronize it with executeAsync() to make sure listener have been registered
            // before this callback reaches back
            listener = getAsyncCallListener(executingPeer, seq);
        }

        if(listener != null) {
            long startTick = System.currentTimeMillis();

            if(s_logger.isDebugEnabled()) {
                s_logger.debug("Processing answer {" + agentId + "-" + seq + "} from remote peer " + executingPeer);
            }

            listener.processAnswers(agentId, seq, answers);

            if(s_logger.isDebugEnabled()) {
                s_logger.debug("Answer {" + agentId + "-" + seq + "} is processed in " +
                        (System.currentTimeMillis() - startTick) + " ms");
            }

            if(!listener.isRecurring()) {
                if(s_logger.isDebugEnabled()) {
                    s_logger.debug("Listener is not recurring after async-result callback {" +
                            agentId + "-" + seq + "}, unregister it");
                }

                unregisterAsyncCall(executingPeer, seq);
            } else {
                if(s_logger.isDebugEnabled()) {
                    s_logger.debug("Listener is recurring after async-result callback {" + agentId
                            +"-" + seq + "}, will keep it");
                }
                return true;
            }
        } else {
            if(s_logger.isInfoEnabled()) {
                s_logger.info("Async-call Listener has not been registered yet for {" + agentId
                        +"-" + seq + "}");
            }
        }
        return false;
    }

    @Override
    public boolean forwardAnswer(String targetPeer, long agentId, long seq, Answer[] answers) {
        if(s_logger.isDebugEnabled()) {
            s_logger.debug("Forward -> " + targetPeer + " Async-call answer {" + agentId + "-" + seq +
                    "} " + (answers != null? _gson.toJson(answers, Answer[].class):""));
        }

        final String targetPeerF = targetPeer;
        final Answer[] answersF = answers;
        final long agentIdF = agentId;
        final long seqF = seq;

        ClusterService peerService = null;

        for(int i = 0; i < 2; i++) {
            try {
                peerService = getPeerService(targetPeerF);
            } catch (RemoteException e) {
                s_logger.error("cluster service for peer " + targetPeerF + " no longer exists");
            }

            if(peerService != null) {
                try {
                    boolean result = false;

                    long startTick = System.currentTimeMillis();
                    if(s_logger.isDebugEnabled()) {
                        s_logger.debug("Start forwarding Async-call answer {" + agentId + "-" + seq + "} to remote");
                    }

                    result = peerService.onAsyncResult(getSelfPeerName(), agentIdF, seqF, _gson.toJson(answersF, Answer[].class));

                    if(s_logger.isDebugEnabled()) {
                        s_logger.debug("Completed forwarding Async-call answer {" + agentId + "-" + seq + "} in " +
                                (System.currentTimeMillis() - startTick) + " ms, return result: " + result);
                    }

                    return result;
                } catch (RemoteException e) {
                    s_logger.warn("Exception in performing remote call, ", e);
                    invalidatePeerService(targetPeerF);
                }
            } else {
                s_logger.warn("Remote peer " + targetPeer + " no longer exists to process answer {" + agentId + "-"
                        + seq + "}");
            }
        }

        return false;
    }

    @Override
    public String getPeerName(long agentHostId) {

        HostVO host = _hostDao.findById(agentHostId);
        if(host != null && host.getManagementServerId() != null) {
            if(getSelfPeerName().equals(Long.toString(host.getManagementServerId()))) {
                return null;
            }

            return Long.toString(host.getManagementServerId());
        }
        return null;
    }

    @Override
    public ManagementServerHostVO getPeer(String mgmtServerId) {
        return _mshostDao.findByMsid(Long.valueOf(mgmtServerId));
    }

    @Override
    public String getSelfPeerName() {
        return Long.toString(_msId);
    }

    @Override
    public String getSelfNodeIP() {
        return _clusterNodeIP;
    }

    @Override
    public void registerListener(ClusterManagerListener listener) {
        // Note : we don't check duplicates
        synchronized (_listeners) {
    		s_logger.info("register cluster listener " + listener.getClass());
    		
        	_listeners.add(listener);
        }
    }

    @Override
    public void unregisterListener(ClusterManagerListener listener) {
        synchronized(_listeners) {
    		s_logger.info("unregister cluster listener " + listener.getClass());
        	
        	_listeners.remove(listener);
        }
    }

    public void notifyNodeJoined(List<ManagementServerHostVO> nodeList) {
        if(s_logger.isDebugEnabled()) {
            s_logger.debug("Notify management server node join to listeners.");

            for(ManagementServerHostVO mshost : nodeList) {
                s_logger.debug("Joining node, IP: " + mshost.getServiceIP() + ", msid: " + mshost.getMsid());
            }
        }

        synchronized(_listeners) {
            for(ClusterManagerListener listener : _listeners) {
                listener.onManagementNodeJoined(nodeList, _mshostId);
            }
        }

        SubscriptionMgr.getInstance().notifySubscribers(ClusterManager.ALERT_SUBJECT, this,
                new ClusterNodeJoinEventArgs(_mshostId, nodeList));
    }

    public void notifyNodeLeft(List<ManagementServerHostVO> nodeList) {
        if(s_logger.isDebugEnabled()) {
            s_logger.debug("Notify management server node left to listeners.");

            for(ManagementServerHostVO mshost : nodeList) {
                s_logger.debug("Leaving node, IP: " + mshost.getServiceIP() + ", msid: " + mshost.getMsid());
            }
        }

        synchronized(_listeners) {
            for(ClusterManagerListener listener : _listeners) {
                listener.onManagementNodeLeft(nodeList, _mshostId);
            }
        }

        SubscriptionMgr.getInstance().notifySubscribers(ClusterManager.ALERT_SUBJECT, this,
                new ClusterNodeLeftEventArgs(_mshostId, nodeList));
    }

    public void notifyNodeIsolated() {
        if(s_logger.isDebugEnabled())
            s_logger.debug("Notify management server node isolation to listeners");

        synchronized(_listeners) {
            for(ClusterManagerListener listener : _listeners) {
                listener.onManagementNodeIsolated();
            }
        }
    }

    public ClusterService getPeerService(String strPeer) throws RemoteException {
        synchronized(_clusterPeers) {
            if(_clusterPeers.containsKey(strPeer)) {
                return _clusterPeers.get(strPeer);
            }
        }

        ClusterService service = _currentServiceAdapter.getPeerService(strPeer);

        if(service != null) {
            synchronized(_clusterPeers) {
                // re-check the peer map again to deal with the
                // race conditions
                if(!_clusterPeers.containsKey(strPeer)) {
                    _clusterPeers.put(strPeer, service);
                }
            }
        }

        return service;
    }

    public void invalidatePeerService(String strPeer) {
        synchronized(_clusterPeers) {
            if(_clusterPeers.containsKey(strPeer)) {
                _clusterPeers.remove(strPeer);
            }
        }
    }

    private void registerAsyncCall(String strPeer, long seq, Listener listener) {
        String key = strPeer + "/" + seq;

        synchronized(_asyncCalls) {
            if(!_asyncCalls.containsKey(key)) {
                _asyncCalls.put(key, listener);
            }
        }
    }

    private Listener getAsyncCallListener(String strPeer, long seq) {
        String key = strPeer + "/" + seq;

        synchronized(_asyncCalls) {
            if(_asyncCalls.containsKey(key)) {
                return _asyncCalls.get(key);
            }
        }

        return null;
    }

    private void unregisterAsyncCall(String strPeer, long seq) {
        String key = strPeer + "/" + seq;

        synchronized(_asyncCalls) {
            if(_asyncCalls.containsKey(key)) {
                _asyncCalls.remove(key);
            }
        }
    }

    private Runnable getHeartbeatTask() {
        return new Runnable() {
            @Override
            public void run() {
                Transaction txn = Transaction.open("ClusterHeartBeat");
                try {
                    Profiler profiler = new Profiler();
                    Profiler profilerHeartbeatUpdate = new Profiler();
                    Profiler profilerPeerScan = new Profiler();
                    Profiler profilerAgentLB = new Profiler();
                    
                    try {
                        profiler.start();
                        
                        profilerHeartbeatUpdate.start();
                        txn.transitToUserManagedConnection(getHeartbeatConnection());
                        if(s_logger.isTraceEnabled()) {
                            s_logger.trace("Cluster manager heartbeat update, id:" + _mshostId);
                        }
    
                        _mshostDao.update(_mshostId, getCurrentRunId(), DateUtil.currentGMTTime());
                        profilerHeartbeatUpdate.stop();
    
                        profilerPeerScan.start();
                        if (s_logger.isTraceEnabled()) {
                            s_logger.trace("Cluster manager peer-scan, id:" + _mshostId);
                        }
    
                        if (!_peerScanInited) {
                            _peerScanInited = true;
                            initPeerScan();
                        }
    
                        peerScan();
                        profilerPeerScan.stop();
                        
                        profilerAgentLB.start();
                        //initiate agent lb task will be scheduled and executed only once, and only when number of agents loaded exceeds _connectedAgentsThreshold
                        if (_agentLBEnabled && !_agentLbHappened) {
                            List<HostVO> allManagedRoutingAgents = _hostDao.listManagedRoutingAgents();
                            List<HostVO> allAgents = _hostDao.listAllRoutingAgents();
                            double allHostsCount = allAgents.size();
                            double managedHostsCount = allManagedRoutingAgents.size();
                            if (allHostsCount > 0.0) {
                                double load = managedHostsCount/allHostsCount;
                                if (load >= _connectedAgentsThreshold) {
                                    s_logger.debug("Scheduling agent rebalancing task as the average agent load " + load + " is more than the threshold " + _connectedAgentsThreshold);
                                    _rebalanceService.scheduleRebalanceAgents();
                                    _agentLbHappened = true;
                                } else {
                                    s_logger.trace("Not scheduling agent rebalancing task as the averages load " + load + " is less than the threshold " + _connectedAgentsThreshold);
                                }
                            } 
                        }
                        profilerAgentLB.stop();
                    } finally {
                        profiler.stop();
                        
                        if(profiler.getDuration() >= _heartbeatInterval) {
                            s_logger.warn("Management server heartbeat takes too long to finish. profiler: " + profiler.toString() + 
                                ", profilerHeartbeatUpdate: " + profilerHeartbeatUpdate.toString() +
                                ", profilerPeerScan: " + profilerPeerScan.toString() +
                                ", profilerAgentLB: " + profilerAgentLB.toString());
                        }
                    }
                    
                } catch(CloudRuntimeException e) {
                    s_logger.error("Runtime DB exception ", e.getCause());

                    if(e.getCause() instanceof ClusterInvalidSessionException) {
                        s_logger.error("Invalid cluster session found");
                        queueNotification(new ClusterManagerMessage(ClusterManagerMessage.MessageType.nodeIsolated));
                    }

                    if(isRootCauseConnectionRelated(e.getCause())) {
                        s_logger.error("DB communication problem detected");
                        queueNotification(new ClusterManagerMessage(ClusterManagerMessage.MessageType.nodeIsolated));
                    }

                    invalidHeartbeatConnection();
                } catch (Throwable e) {
                    if(isRootCauseConnectionRelated(e.getCause())) {
                        s_logger.error("DB communication problem detected");
                        queueNotification(new ClusterManagerMessage(ClusterManagerMessage.MessageType.nodeIsolated));
                    }

                    s_logger.error("Problem with the cluster heartbeat!", e);
                } finally {
                    txn.close("ClusterHeartBeat");
                }
            }
        };
    }

    private boolean isRootCauseConnectionRelated(Throwable e) {
        while (e != null) {
            if (e instanceof com.mysql.jdbc.CommunicationsException || e instanceof com.mysql.jdbc.exceptions.jdbc4.CommunicationsException) {
                return true;
            }

            e = e.getCause();
        }

        return false;
    }

    private Connection getHeartbeatConnection() throws SQLException {
        if(_heartbeatConnection == null) {
            Connection conn = Transaction.getStandaloneConnectionWithException();
            _heartbeatConnection = new ConnectionConcierge("ClusterManagerHeartBeat", conn, false);
        }

        return _heartbeatConnection.conn();
    }

    private void invalidHeartbeatConnection() {
        if(_heartbeatConnection != null) {
            Connection conn = Transaction.getStandaloneConnection();
            if (conn != null) {
                _heartbeatConnection.reset(Transaction.getStandaloneConnection());
            }
        }
    }

    private Runnable getNotificationTask() {
        return new Runnable() {
            @Override
            public void run() {
                while(true) {
                    synchronized(_notificationMsgs) {
                        try {
                            _notificationMsgs.wait(1000);
                        } catch (InterruptedException e) {
                        }
                    }

                    ClusterManagerMessage msg = null;
                    while((msg = getNextNotificationMessage()) != null) {
                        try {
                            switch(msg.getMessageType()) {
                            case nodeAdded:
                                if(msg.getNodes() != null && msg.getNodes().size() > 0) {
                                    Profiler profiler = new Profiler();
                                    profiler.start();

                                    notifyNodeJoined(msg.getNodes());

                                    profiler.stop();
                                    if(profiler.getDuration() > 1000) {
                                        if(s_logger.isDebugEnabled()) {
                                            s_logger.debug("Notifying management server join event took " + profiler.getDuration() + " ms");
                                        }
                                    } else {
                                        s_logger.warn("Notifying management server join event took " + profiler.getDuration() + " ms");
                                    }
                                }
                                break;

                            case nodeRemoved:
                                if(msg.getNodes() != null && msg.getNodes().size() > 0) {
                                    Profiler profiler = new Profiler();
                                    profiler.start();

                                    notifyNodeLeft(msg.getNodes());

                                    profiler.stop();
                                    if(profiler.getDuration() > 1000) {
                                        if(s_logger.isDebugEnabled()) {
                                            s_logger.debug("Notifying management server leave event took " + profiler.getDuration() + " ms");
                                        }
                                    } else {
                                        s_logger.warn("Notifying management server leave event took " + profiler.getDuration() + " ms");
                                    }
                                }
                                break;

                            case nodeIsolated:
                                notifyNodeIsolated();
                                break;

                            default :
                                assert(false);
                                break;
                            }

                        } catch (Throwable e) {
                            s_logger.warn("Unexpected exception during cluster notification. ", e);
                        }
                    }

                    try { Thread.sleep(1000); } catch (InterruptedException e) {}
                }
            }
        };
    }

    private void queueNotification(ClusterManagerMessage msg) {
        synchronized(this._notificationMsgs) {
            this._notificationMsgs.add(msg);
            this._notificationMsgs.notifyAll();
        }
    }

    private ClusterManagerMessage getNextNotificationMessage() {
        synchronized(this._notificationMsgs) {
            if(this._notificationMsgs.size() > 0) {
                return this._notificationMsgs.remove(0);
            }
        }

        return null;
    }

    private void initPeerScan() {
        // upon startup, for all inactive management server nodes that we see at startup time, we will send notification also to help upper layer perform
        // missed cleanup
        Date cutTime = DateUtil.currentGMTTime();
        List<ManagementServerHostVO> inactiveList = _mshostDao.getInactiveList(new Date(cutTime.getTime() - _heartbeatThreshold));
       
        // We don't have foreign key constraints to enforce the mgmt_server_id integrity in host table, when user manually 
        // remove records from mshost table, this will leave orphan mgmt_serve_id reference in host table.
        List<Long> orphanList = _mshostDao.listOrphanMsids();
        if(orphanList.size() > 0) {
	        for(Long orphanMsid : orphanList) {
	        	// construct fake ManagementServerHostVO based on orphan MSID
	        	s_logger.info("Add orphan management server msid found in host table to initial clustering notification, orphan msid: " + orphanMsid);
	        	inactiveList.add(new ManagementServerHostVO(orphanMsid, 0, "orphan", 0, new Date()));
	        }
        } else {
        	s_logger.info("We are good, no orphan management server msid in host table is found");
        }
        
        if(inactiveList.size() > 0) {
        	if(s_logger.isInfoEnabled()) {
        		s_logger.info("Found " + inactiveList.size() + " inactive management server node based on timestamp");
        		for(ManagementServerHostVO host : inactiveList)
        			s_logger.info("management server node msid: " + host.getMsid() + ", name: " + host.getName() + ", service ip: " + host.getServiceIP() + ", version: " + host.getVersion());
        	}
        	
            this.queueNotification(new ClusterManagerMessage(ClusterManagerMessage.MessageType.nodeRemoved, inactiveList));
        } else {
        	s_logger.info("No inactive management server node found");
        }
    }

    private void peerScan() {
        Date cutTime = DateUtil.currentGMTTime();

        List<ManagementServerHostVO> currentList = _mshostDao.getActiveList(new Date(cutTime.getTime() - _heartbeatThreshold));

        List<ManagementServerHostVO> removedNodeList = new ArrayList<ManagementServerHostVO>();
        List<ManagementServerHostVO> invalidatedNodeList = new ArrayList<ManagementServerHostVO>();

        if(_mshostId != null) {
            // only if we have already attached to cluster, will we start to check leaving nodes
            for(Map.Entry<Long, ManagementServerHostVO>  entry : _activePeers.entrySet()) {

                ManagementServerHostVO current = getInListById(entry.getKey(), currentList);
                if(current == null) {
                    if(entry.getKey().longValue() != _mshostId.longValue()) {
                        if(s_logger.isDebugEnabled()) {
                            s_logger.debug("Detected management node left, id:" + entry.getKey() + ", nodeIP:" + entry.getValue().getServiceIP());
                        }
                        removedNodeList.add(entry.getValue());
                    }
                } else {
                    if(current.getRunid() == 0) {
                        if(entry.getKey().longValue() != _mshostId.longValue()) {
                            if(s_logger.isDebugEnabled()) {
                                s_logger.debug("Detected management node left because of invalidated session, id:" + entry.getKey() + ", nodeIP:" + entry.getValue().getServiceIP());
                            }
                            invalidatedNodeList.add(entry.getValue());
                        }
                    } else {
                        if(entry.getValue().getRunid() != current.getRunid()) {
                            if(s_logger.isDebugEnabled()) {
                                s_logger.debug("Detected management node left and rejoined quickly, id:" + entry.getKey() + ", nodeIP:" + entry.getValue().getServiceIP());
                            }

                            entry.getValue().setRunid(current.getRunid());
                        }
                    }
                }
            }
        }

        // process invalidated node list
        if(invalidatedNodeList.size() > 0) {
            for(ManagementServerHostVO mshost : invalidatedNodeList) {
                _activePeers.remove(mshost.getId());
                try {
                    JmxUtil.unregisterMBean("ClusterManager", "Node " + mshost.getId());
                } catch(Exception e) {
                    s_logger.warn("Unable to deregiester cluster node from JMX monitoring due to exception " + e.toString());
                }
            }

            this.queueNotification(new ClusterManagerMessage(ClusterManagerMessage.MessageType.nodeRemoved, invalidatedNodeList));
        }

        // process removed node list
        Iterator<ManagementServerHostVO> it = removedNodeList.iterator();
        while(it.hasNext()) {
            ManagementServerHostVO mshost = it.next();
            if(!pingManagementNode(mshost)) {
                s_logger.warn("Management node " + mshost.getId() + " is detected inactive by timestamp and also not pingable");
                _activePeers.remove(mshost.getId());
                try {
                    JmxUtil.unregisterMBean("ClusterManager", "Node " + mshost.getId());
                } catch(Exception e) {
                    s_logger.warn("Unable to deregiester cluster node from JMX monitoring due to exception " + e.toString());
                }
            } else {
                s_logger.info("Management node " + mshost.getId() + " is detected inactive by timestamp but is pingable");
                it.remove();
            }
        }

        if(removedNodeList.size() > 0) {
            this.queueNotification(new ClusterManagerMessage(ClusterManagerMessage.MessageType.nodeRemoved, removedNodeList));
        }

        List<ManagementServerHostVO> newNodeList = new ArrayList<ManagementServerHostVO>();
        for(ManagementServerHostVO mshost : currentList) {
            if(!_activePeers.containsKey(mshost.getId())) {
                _activePeers.put(mshost.getId(), mshost);

                if(s_logger.isDebugEnabled()) {
                    s_logger.debug("Detected management node joined, id:" + mshost.getId() + ", nodeIP:" + mshost.getServiceIP());
                }
                newNodeList.add(mshost);

                try {
                    JmxUtil.registerMBean("ClusterManager", "Node " + mshost.getId(), new ClusterManagerMBeanImpl(this, mshost));
                } catch(Exception e) {
                    s_logger.warn("Unable to regiester cluster node into JMX monitoring due to exception " + ExceptionUtil.toString(e));
                }
            }
        }

        if(newNodeList.size() > 0) {
            this.queueNotification(new ClusterManagerMessage(ClusterManagerMessage.MessageType.nodeAdded, newNodeList));
        }
    }

    private static ManagementServerHostVO getInListById(Long id, List<ManagementServerHostVO> l) {
        for(ManagementServerHostVO mshost : l) {
            if(mshost.getId() == id) {
                return mshost;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override @DB
    public boolean start() {
        if(s_logger.isInfoEnabled()) {
            s_logger.info("Starting cluster manager, msid : " + _msId);
        }

        Transaction txn = Transaction.currentTxn();
        try {
            txn.start();

            final Class<?> c = this.getClass();
            String version = c.getPackage().getImplementationVersion();

            ManagementServerHostVO mshost = _mshostDao.findByMsid(_msId);
            if (mshost == null) {
                mshost = new ManagementServerHostVO();
                mshost.setMsid(_msId);
                mshost.setRunid(this.getCurrentRunId());
                mshost.setName(NetUtils.getHostName());
                mshost.setVersion(version);
                mshost.setServiceIP(_clusterNodeIP);
                mshost.setServicePort(_currentServiceAdapter.getServicePort());
                mshost.setLastUpdateTime(DateUtil.currentGMTTime());
                mshost.setRemoved(null);
                mshost.setAlertCount(0);
                mshost.setState(ManagementServerHost.State.Up);
                _mshostDao.persist(mshost);

                if (s_logger.isInfoEnabled()) {
                    s_logger.info("New instance of management server msid " + _msId + " is being started");
                }
            } else {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("Management server " + _msId + " is being started");
                }

                _mshostDao.update(mshost.getId(), getCurrentRunId(), NetUtils.getHostName(), version, _clusterNodeIP, _currentServiceAdapter.getServicePort(), DateUtil.currentGMTTime());
            }

            txn.commit();

            _mshostId = mshost.getId();
            if (s_logger.isInfoEnabled()) {
                s_logger.info("Management server (host id : " + _mshostId + ") is being started at " + _clusterNodeIP + ":" + _currentServiceAdapter.getServicePort());
            }

            // use seperate thread for heartbeat updates
            _heartbeatScheduler.scheduleAtFixedRate(getHeartbeatTask(), _heartbeatInterval, _heartbeatInterval, TimeUnit.MILLISECONDS);
            _notificationExecutor.submit(getNotificationTask());

        } catch (Throwable e) {
            s_logger.error("Unexpected exception : ", e);
            txn.rollback();

            throw new CloudRuntimeException("Unable to initialize cluster info into database");
        }

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Cluster manager was started successfully");
        }

        return true;
    }

    @Override @DB
    public boolean stop() {
        if(_mshostId != null) {
            ManagementServerHostVO mshost = _mshostDao.findByMsid(_msId);
            mshost.setState(ManagementServerHost.State.Down);
            _mshostDao.update(_mshostId, mshost);
        }

        _heartbeatScheduler.shutdownNow();
        _executor.shutdownNow();

        try {
            _heartbeatScheduler.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
            _executor.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }

        if(s_logger.isInfoEnabled()) {
            s_logger.info("Cluster manager is stopped");
        }

        return true;
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        if(s_logger.isInfoEnabled()) {
            s_logger.info("Start configuring cluster manager : " + name);
        }
        _name = name;

        ComponentLocator locator = ComponentLocator.getCurrentLocator();
        _agentMgr = locator.getManager(AgentManager.class);
        if (_agentMgr == null) {
            throw new ConfigurationException("Unable to get " + AgentManager.class.getName());
        }

        _mshostDao = locator.getDao(ManagementServerHostDao.class);
        if (_mshostDao == null) {
            throw new ConfigurationException("Unable to get " + ManagementServerHostDao.class.getName());
        }

        _hostDao = locator.getDao(HostDao.class);
        if (_hostDao == null) {
            throw new ConfigurationException("Unable to get " + HostDao.class.getName());
        }

        _hostTransferDao = locator.getDao(HostTransferMapDao.class);
        if (_hostTransferDao == null) {
            throw new ConfigurationException("Unable to get agent transfer map dao");
        }

        ConfigurationDao configDao = locator.getDao(ConfigurationDao.class);
        if (configDao == null) {
            throw new ConfigurationException("Unable to get the configuration dao.");
        }

        Map<String, String> configs = configDao.getConfiguration("management-server", params);

        String value = configs.get("cluster.heartbeat.interval");
        if (value != null) {
            _heartbeatInterval = NumbersUtil.parseInt(value, ClusterManager.DEFAULT_HEARTBEAT_INTERVAL);
        }

        value = configs.get("cluster.heartbeat.threshold");
        if (value != null) {
            _heartbeatThreshold = NumbersUtil.parseInt(value, ClusterManager.DEFAULT_HEARTBEAT_THRESHOLD);
        }

        File dbPropsFile = PropertiesUtil.findConfigFile("db.properties");
        Properties dbProps = new Properties();
        try {
            dbProps.load(new FileInputStream(dbPropsFile));
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Unable to find db.properties");
        } catch (IOException e) {
            throw new ConfigurationException("Unable to load db.properties content");
        }
        _clusterNodeIP = dbProps.getProperty("cluster.node.IP");
        if (_clusterNodeIP == null) {
            _clusterNodeIP = "127.0.0.1";
        }
        _clusterNodeIP = _clusterNodeIP.trim();

        if(s_logger.isInfoEnabled()) {
            s_logger.info("Cluster node IP : " + _clusterNodeIP);
        }

        if(!NetUtils.isLocalAddress(_clusterNodeIP)) {
            throw new ConfigurationException("cluster node IP should be valid local address where the server is running, please check your configuration");
        }

        Adapters<ClusterServiceAdapter> adapters = locator.getAdapters(ClusterServiceAdapter.class);
        if (adapters == null || !adapters.isSet()) {
            throw new ConfigurationException("Unable to get cluster service adapters");
        }
        Enumeration<ClusterServiceAdapter> it = adapters.enumeration();
        if(it.hasMoreElements()) {
            _currentServiceAdapter = it.nextElement();
        }

        if(_currentServiceAdapter == null) {
            throw new ConfigurationException("Unable to set current cluster service adapter");
        }


        _agentLBEnabled = Boolean.valueOf(configDao.getValue(Config.AgentLbEnable.key()));
        
        String connectedAgentsThreshold = configs.get("agent.load.threshold");
        
        if (connectedAgentsThreshold != null) {
            _connectedAgentsThreshold = Double.parseDouble(connectedAgentsThreshold);
        }

        this.registerListener(new LockMasterListener(_msId));

        checkConflicts();

        if(s_logger.isInfoEnabled()) {
            s_logger.info("Cluster manager is configured.");
        }
        return true;
    }

    @Override
    public long getManagementNodeId() {
        return _msId;
    }

    @Override
    public long getCurrentRunId() {
        return _runId;
    }

    @Override
    public boolean isManagementNodeAlive(long msid) {
        ManagementServerHostVO mshost = _mshostDao.findByMsid(msid);
        if(mshost != null) {
            if(mshost.getLastUpdateTime().getTime() >=  DateUtil.currentGMTTime().getTime() - _heartbeatThreshold) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean pingManagementNode(long msid) {
        ManagementServerHostVO mshost = _mshostDao.findByMsid(msid);
        if(mshost == null) {
            return false;
        }

        return pingManagementNode(mshost);
    }

    private boolean pingManagementNode(ManagementServerHostVO mshost) {

        String targetIp = mshost.getServiceIP();
        if("127.0.0.1".equals(targetIp) || "0.0.0.0".equals(targetIp)) {
            s_logger.info("ping management node cluster service can not be performed on self");
            return false;
        }

        String targetPeer = String.valueOf(mshost.getMsid());
        ClusterService peerService = null;
        for(int i = 0; i < 2; i++) {
            try {
                peerService = getPeerService(targetPeer);
            } catch (RemoteException e) {
                s_logger.error("cluster service for peer " + targetPeer + " no longer exists");
            }

            if(peerService != null) {
                try {
                    return peerService.ping(getSelfPeerName());
                } catch (RemoteException e) {
                    s_logger.warn("Exception in performing remote call, ", e);
                    invalidatePeerService(targetPeer);
                }
            } else {
                s_logger.warn("Remote peer " + mshost.getMsid() + " no longer exists");
            }
        }

        return false;
    }


    @Override
    public int getHeartbeatThreshold() {
        return this._heartbeatThreshold;
    }

    public int getHeartbeatInterval() {
        return this._heartbeatInterval;
    }

    public void setHeartbeatThreshold(int threshold) {
        _heartbeatThreshold = threshold;
    }

    private void checkConflicts() throws ConfigurationException {
        Date cutTime = DateUtil.currentGMTTime();
        List<ManagementServerHostVO> peers = _mshostDao.getActiveList(new Date(cutTime.getTime() - _heartbeatThreshold));
        for(ManagementServerHostVO peer : peers) {
            String peerIP = peer.getServiceIP().trim();
            if(_clusterNodeIP.equals(peerIP)) {
                if("127.0.0.1".equals(_clusterNodeIP)) {
                    if(pingManagementNode(peer.getMsid())) {
                        String msg = "Detected another management node with localhost IP is already running, please check your cluster configuration";
                        s_logger.error(msg);
                        throw new ConfigurationException(msg);
                    } else {
                        String msg = "Detected another management node with localhost IP is considered as running in DB, however it is not pingable, we will continue cluster initialization with this management server node";
                        s_logger.info(msg);
                    }
                } else {
                    if(pingManagementNode(peer.getMsid())) {
                        String msg = "Detected that another management node with the same IP " + peer.getServiceIP() + " is already running, please check your cluster configuration";
                        s_logger.error(msg);
                        throw new ConfigurationException(msg);
                    } else {
                        String msg = "Detected that another management node with the same IP " + peer.getServiceIP()
                                + " is considered as running in DB, however it is not pingable, we will continue cluster initialization with this management server node";
                        s_logger.info(msg);
                    }
                }
            }
        }
    }

    @Override
    public boolean rebalanceAgent(long agentId, Event event, long currentOwnerId, long futureOwnerId) throws AgentUnavailableException, OperationTimedoutException {
        return _rebalanceService.executeRebalanceRequest(agentId, currentOwnerId, futureOwnerId, event);
    }

    @Override
    public  boolean isAgentRebalanceEnabled() {
        return _agentLBEnabled;
    }
}
