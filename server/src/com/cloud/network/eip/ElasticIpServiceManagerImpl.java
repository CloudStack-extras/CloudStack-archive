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
package com.cloud.network.eip;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
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
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.RebootCommand;
import com.cloud.agent.api.StopAnswer;
import com.cloud.agent.api.check.CheckSshAnswer;
import com.cloud.agent.api.check.CheckSshCommand;
import com.cloud.agent.manager.Commands;
import com.cloud.cluster.ClusterManager;
import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.info.RunningHostCountInfo;
import com.cloud.info.RunningHostInfoAgregator;
import com.cloud.info.RunningHostInfoAgregator.ZoneHostInfo;
import com.cloud.network.NetworkManager;
import com.cloud.network.NetworkVO;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.router.VirtualRouter.Role;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateHostDao;
import com.cloud.user.Account;
import com.cloud.user.AccountService;
import com.cloud.user.User;
import com.cloud.utils.DateUtil;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.component.Adapters;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachine.Type;
import com.cloud.vm.VirtualMachineGuru;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.VirtualMachineName;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.DomainRouterDao;


@Local(value = { ElasticIpServiceManager.class })
public class ElasticIpServiceManagerImpl implements ElasticIpServiceManager, VirtualMachineGuru<DomainRouterVO> {
    private static final Logger s_logger = Logger.getLogger(ElasticIpServiceManagerImpl.class);

    private static final int DEFAULT_CAPACITY_SCAN_INTERVAL = 30000; // 30
                                                                     // seconds
    private static final int EXECUTOR_SHUTDOWN_TIMEOUT = 1000; // 1 second

    private static final int ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_COOPERATION = 3; // 3
                                                                              // seconds
    private static final int ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_SYNC = 180; // 3
                                                                         // minutes

    private static final int STARTUP_DELAY = 60000; // 60 seconds

    private String _name;
    @Inject(adapter = ElasticIpVmAllocator.class)
    private Adapters<ElasticIpVmAllocator> _eipVmAllocators;

    @Inject
    private DomainRouterDao _elasticIpVmDao;
    @Inject
    private DataCenterDao _dcDao;
    @Inject
    private VMTemplateDao _templateDao;
    @Inject
    private HostDao _hostDao;
    @Inject
    private StoragePoolHostDao _storagePoolHostDao;

    @Inject
    private VMTemplateHostDao _vmTemplateHostDao;

    @Inject
    private AgentManager _agentMgr;
    @Inject
    private NetworkManager _networkMgr;

    @Inject
    private ClusterManager _clusterMgr;

    private ServiceOfferingVO _serviceOffering;

    @Inject
    private ConfigurationDao _configDao;
    @Inject
    private ServiceOfferingDao _offeringDao;
    @Inject
    private AccountService _accountMgr;
    @Inject
    private VirtualMachineManager _itMgr;

    private final ScheduledExecutorService _capacityScanScheduler = Executors.newScheduledThreadPool(1, new NamedThreadFactory("EIP-Scan"));

    private long _capacityScanInterval = DEFAULT_CAPACITY_SCAN_INTERVAL;

    private int _elasticIpVmRamSize;
    private int _elasticIpVmCpuMHz;

    private String _instance;
    private boolean _useLocalStorage;
    private final GlobalLock _capacityScanLock = GlobalLock.getInternLock(getCapacityScanLockName());
    private final GlobalLock _allocLock = GlobalLock.getInternLock(getAllocLockName());

    @Override
    public DomainRouterVO startElasticIpVm(long elasticIpVmId) {
        try {
            DomainRouterVO elasticIpVm = _elasticIpVmDao.findById(elasticIpVmId);
            Account systemAcct = _accountMgr.getSystemAccount();
            User systemUser = _accountMgr.getSystemUser();
            return _itMgr.start(elasticIpVm, null, systemUser, systemAcct);
        } catch (StorageUnavailableException e) {
            s_logger.warn("Exception while trying to start elastic ip  vm", e);
            return null;
        } catch (InsufficientCapacityException e) {
            s_logger.warn("Exception while trying to start elastic ip  vm", e);
            return null;
        } catch (ResourceUnavailableException e) {
            s_logger.warn("Exception while trying to start elastic ip  vm", e);
            return null;
        } catch (Exception e) {
            s_logger.warn("Exception while trying to start elastic ip  vm", e);
            return null;
        }
    }


    public DomainRouterVO startNew(long dataCenterId) {

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Assign elastic ip  vm from a newly started instance for request from data center : " + dataCenterId);
        }

        Map<String, Object> context = createElasticIpVmInstance(dataCenterId);

        long ElasticIpVmId = (Long) context.get("ElasticIpVmId");
        if (ElasticIpVmId == 0) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Creating elastic ip  vm instance failed, data center id : " + dataCenterId);
            }

            return null;
        }

        DomainRouterVO ElasticIpVm = _elasticIpVmDao.findById(ElasticIpVmId);
       
        if (ElasticIpVm != null) {
//            SubscriptionMgr.getInstance().notifySubscribers(ALERT_SUBJECT, this,
//                    new ElasticIpVmAlertEventArgs(ElasticIpVmAlertEventArgs.SSVM_CREATED, dataCenterId, ElasticIpVmId, ElasticIpVm, null));
            return ElasticIpVm;
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Unable to allocate elastic ip  vm storage, remove the elastic ip  vm record from DB, elastic ip  vm id: " + ElasticIpVmId);
            }

//            SubscriptionMgr.getInstance().notifySubscribers(ALERT_SUBJECT, this,
//                    new ElasticIpVmAlertEventArgs(ElasticIpVmAlertEventArgs.SSVM_CREATE_FAILURE, dataCenterId, ElasticIpVmId, null, "Unable to allocate storage"));
        }
        return null;
    }

    protected Map<String, Object> createElasticIpVmInstance(long dataCenterId) {
        HostVO secHost = _hostDao.findSecondaryStorageHost(dataCenterId);
        if (secHost == null) {
            String msg = "No secondary storage  available in zone " + dataCenterId + ", cannot create elastic ip  vm";
            s_logger.warn(msg);
            throw new CloudRuntimeException(msg);
        }

        long id = _elasticIpVmDao.getNextInSequence(Long.class, "id");
        String name = VirtualMachineName.getSystemVmName(id, _instance, "r").intern();
        Account systemAcct = _accountMgr.getSystemAccount();

        DataCenterDeployment plan = new DataCenterDeployment(dataCenterId);
        DataCenter dc = _dcDao.findById(plan.getDataCenterId());

        List<NetworkOfferingVO> defaultOffering = _networkMgr.getSystemAccountNetworkOfferings(NetworkOfferingVO.SystemPublicNetwork);

        if (dc.getNetworkType() == NetworkType.Basic || dc.isSecurityGroupEnabled()) {
            defaultOffering = _networkMgr.getSystemAccountNetworkOfferings(NetworkOfferingVO.SystemGuestNetwork);
        }

        List<NetworkOfferingVO> offerings = _networkMgr.getSystemAccountNetworkOfferings(NetworkOfferingVO.SystemControlNetwork, NetworkOfferingVO.SystemManagementNetwork);
        List<Pair<NetworkVO, NicProfile>> networks = new ArrayList<Pair<NetworkVO, NicProfile>>(offerings.size() + 1);
        NicProfile defaultNic = new NicProfile();
        defaultNic.setDefaultNic(true);
        defaultNic.setDeviceId(2);
        try {
            networks.add(new Pair<NetworkVO, NicProfile>(_networkMgr.setupNetwork(systemAcct, defaultOffering.get(0), plan, null, null, false, false).get(0), defaultNic));
            for (NetworkOfferingVO offering : offerings) {
                networks.add(new Pair<NetworkVO, NicProfile>(_networkMgr.setupNetwork(systemAcct, offering, plan, null, null, false, false).get(0), null));
            }
        } catch (ConcurrentOperationException e) {
            s_logger.info("Unable to setup due to concurrent operation. " + e);
            return new HashMap<String, Object>();
        }

        VMTemplateVO template = _templateDao.findSystemVMTemplate(dataCenterId);
        if (template == null) {
            s_logger.debug("Can't find a template to start");
            throw new CloudRuntimeException("Insufficient capacity exception");
        }

        DomainRouterVO elasticIpVm = new DomainRouterVO(id, _serviceOffering.getId(), name, template.getId(), template.getHypervisorType(), Type.ElasticIpVm, template.getGuestOSId(), dataCenterId,
                systemAcct.getDomainId(), systemAcct.getId(), _serviceOffering.getOfferHA());
        try {
            elasticIpVm = _itMgr.allocate(elasticIpVm, template, _serviceOffering, networks, plan, null, systemAcct);
        } catch (InsufficientCapacityException e) {
            s_logger.warn("InsufficientCapacity", e);
            throw new CloudRuntimeException("Insufficient capacity exception", e);
        }

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("ElasticIpVmId", elasticIpVm.getId());
        return context;
    }

    private ElasticIpVmAllocator getCurrentAllocator() {

        // for now, only one adapter is supported
        Enumeration<ElasticIpVmAllocator> it = _eipVmAllocators.enumeration();
        if (it.hasMoreElements()) {
            return it.nextElement();
        }

        return null;
    }

    protected String connect(String ipAddress, int port) {
        return null;
    }

    private Runnable getCapacityScanTask() {
        return new Runnable() {

            @Override
            public void run() {
                Transaction txn = Transaction.open(Transaction.CLOUD_DB);
                try {
                    reallyRun();
                } catch (Throwable e) {
                    s_logger.warn("Unexpected exception " + e.getMessage(), e);
                } finally {
                    txn.close();
                }
            }

            private void reallyRun() {
                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("Begin elastic ip  vm capacity scan");
                }

                Map<Long, ZoneHostInfo> zoneHostInfoMap = getZoneHostInfo();
                if (isServiceReady(zoneHostInfoMap)) {
                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("Elastic Ip VM Service is ready, check to see if we need to allocate standby capacity");
                    }

                    if (!_capacityScanLock.lock(ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_COOPERATION)) {
                        if (s_logger.isTraceEnabled()) {
                            s_logger.trace("Elastic Ip VM Capacity scan lock is used by others, skip and wait for my turn");
                        }
                        return;
                    }

                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("*** Begining elastic ip  vm capacity scan... ***");
                    }

                    try {
                        // checkPendingElasticIpVms();

                        List<DataCenterVO> datacenters = _dcDao.listAllIncludingRemoved();

                        for (DataCenterVO dc : datacenters) {
                            if (isZoneReady(zoneHostInfoMap, dc.getId())) {
                                List<DomainRouterVO> alreadyRunning = _elasticIpVmDao.listInStates(dc.getId(), Role.FIREWALL, State.Running, State.Migrating, State.Starting);
                                List<DomainRouterVO> stopped = _elasticIpVmDao.listInStates(dc.getId(), Role.FIREWALL, State.Stopped, State.Stopping);
                                if (alreadyRunning.size() == 0) {
                                    if (stopped.size() == 0) {
                                        s_logger.info("No elastic ip  vms found in datacenter id=" + dc.getId() + ", starting a new one");
                                        allocCapacity(dc.getId());
                                    } else {
                                        s_logger.warn("Stopped elastic ip  vms found in datacenter id=" + dc.getId() + ", not restarting them automatically");
                                    }

                                }
                            } else {
                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Zone " + dc.getId() + " is not ready to alloc elastic ip  vm");
                                }
                            }
                        }

                        if (s_logger.isTraceEnabled()) {
                            s_logger.trace("*** Stop elastic ip  vm capacity scan ***");
                        }
                    } finally {
                        _capacityScanLock.unlock();
                    }

                } else {
                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("Elastic Ip vm service is not ready for capacity preallocation, wait for next time");
                    }
                }

                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("End of elastic ip  vm capacity scan");
                }
            }
        };
    }

    public DomainRouterVO assignElasticIpVmFromRunningPool(long dataCenterId) {

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Assign  elastic ip  vm from running pool for request from data center : " + dataCenterId);
        }

        ElasticIpVmAllocator allocator = getCurrentAllocator();
        assert (allocator != null);
        List<DomainRouterVO> runningList = _elasticIpVmDao.listInStates(dataCenterId, Role.FIREWALL, State.Running);
        if (runningList != null && runningList.size() > 0) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Running elastic ip  vm pool size : " + runningList.size());
                for (DomainRouterVO ElasticIpVm : runningList) {
                    s_logger.trace("Running ElasticIpVm instance : " + ElasticIpVm.getHostName());
                }
            }

            Map<Long, Integer> loadInfo = new HashMap<Long, Integer>();

            return allocator.allocElasticIpVm(runningList, loadInfo, dataCenterId);
        } else {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Empty running ElasticIpVm pool for now in data center : " + dataCenterId);
            }
        }
        return null;
    }

    public DomainRouterVO assignElasticIpVmFromStoppedPool(long dataCenterId) {
        List<DomainRouterVO> l = _elasticIpVmDao.listInStates(dataCenterId, Role.FIREWALL, State.Starting, State.Stopped, State.Migrating);
        if (l != null && l.size() > 0) {
            return l.get(0);
        }

        return null;
    }

    private void allocCapacity(long dataCenterId) {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Allocate elastic ip  vm standby capacity for data center : " + dataCenterId);
        }

        boolean elasticIpVmFromStoppedPool = false;
        DomainRouterVO elasticIpVm = assignElasticIpVmFromStoppedPool(dataCenterId);
        if (elasticIpVm == null) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("No stopped elastic ip  vm is available, need to allocate a new elastic ip  vm");
            }

            if (_allocLock.lock(ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_SYNC)) {
                try {
                    elasticIpVm = startNew(dataCenterId);
                } finally {
                    _allocLock.unlock();
                }
            } else {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("Unable to acquire synchronization lock to allocate ElasticIpVm resource for standby capacity, wait for next scan");
                }
                return;
            }
        } else {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("Found a stopped elastic ip  vm, bring it up to running pool. ElasticIpVm vm id : " + elasticIpVm.getId());
            }
            elasticIpVmFromStoppedPool = true;
        }

        if (elasticIpVm != null) {
            long elasticIpVmId = elasticIpVm.getId();
            GlobalLock elasticIpVmLock = GlobalLock.getInternLock(getElasticIpVmLockName(elasticIpVmId));
            try {
                if (elasticIpVmLock.lock(ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_SYNC)) {
                    try {
                        elasticIpVm = startElasticIpVm(elasticIpVmId);
                    } finally {
                        elasticIpVmLock.unlock();
                    }
                } else {
                    if (s_logger.isInfoEnabled()) {
                        s_logger.info("Unable to acquire synchronization lock to start ElasticIpVm for standby capacity, ElasticIpVm vm id : " + elasticIpVm.getId());
                    }
                    return;
                }
            } finally {
                elasticIpVmLock.releaseRef();
            }

            if (elasticIpVm == null) {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("Unable to start elastic ip  vm for standby capacity, ElasticIpVm vm Id : " + elasticIpVmId + ", will recycle it and start a new one");
                }

                if (elasticIpVmFromStoppedPool) {
                    destroyElasticIpVm(elasticIpVmId);
                }
            } else {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("Elastic Ip vm " + elasticIpVm.getHostName() + " is started");
                }
            }
        }
    }

    public boolean isServiceReady(Map<Long, ZoneHostInfo> zoneHostInfoMap) {
        for (ZoneHostInfo zoneHostInfo : zoneHostInfoMap.values()) {
            if ((zoneHostInfo.getFlags() & RunningHostInfoAgregator.ZoneHostInfo.ALL_HOST_MASK) != 0) {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("Zone " + zoneHostInfo.getDcId() + " is ready to launch");
                }
                return true;
            }
        }

        return false;
    }

    public boolean isZoneReady(Map<Long, ZoneHostInfo> zoneHostInfoMap, long dataCenterId) {
        ZoneHostInfo zoneHostInfo = zoneHostInfoMap.get(dataCenterId);
        if (zoneHostInfo != null && (zoneHostInfo.getFlags() & RunningHostInfoAgregator.ZoneHostInfo.ROUTING_HOST_MASK) != 0) {
            VMTemplateVO template = _templateDao.findSystemVMTemplate(dataCenterId);
            HostVO secHost = _hostDao.findSecondaryStorageHost(dataCenterId);
            if (secHost == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("No secondary storage available in zone " + dataCenterId + ", wait until it is ready to launch elastic ip  vm");
                }
                return false;
            }

            boolean templateReady = false;
            if (template != null) {
                VMTemplateHostVO templateHostRef = _vmTemplateHostDao.findByHostTemplate(secHost.getId(), template.getId());
                templateReady = (templateHostRef != null) && (templateHostRef.getDownloadState() == Status.DOWNLOADED);
            }

            if (templateReady) {

                List<Pair<Long, Integer>> l = _storagePoolHostDao.getDatacenterStoragePoolHostInfo(dataCenterId, !_useLocalStorage);
                if (l != null && l.size() > 0 && l.get(0).second().intValue() > 0) {

                    return true;
                } else {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Primary storage is not ready, wait until it is ready to launch elastic ip  vm");
                    }
                }
            } else {
                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("Zone host is ready, but elastic ip  vm template is not ready");
                }
            }
        }
        return false;
    }

    private synchronized Map<Long, ZoneHostInfo> getZoneHostInfo() {
        Date cutTime = DateUtil.currentGMTTime();
        List<RunningHostCountInfo> l = _hostDao.getRunningHostCounts(new Date(cutTime.getTime() - _clusterMgr.getHeartbeatThreshold()));

        RunningHostInfoAgregator aggregator = new RunningHostInfoAgregator();
        if (l.size() > 0) {
            for (RunningHostCountInfo countInfo : l) {
                aggregator.aggregate(countInfo);
            }
        }

        return aggregator.getZoneHostInfoMap();
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean start() {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Start elastic ip  vm manager");
        }

        return true;
    }

    @Override
    public boolean stop() {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Stop elastic ip  vm manager");
        }
        _capacityScanScheduler.shutdownNow();

        try {
            _capacityScanScheduler.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }

        _capacityScanLock.releaseRef();
        _allocLock.releaseRef();
        return true;
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Start configuring elastic ip  vm manager : " + name);
        }

        _name = name;

        ComponentLocator locator = ComponentLocator.getCurrentLocator();
        ConfigurationDao configDao = locator.getDao(ConfigurationDao.class);
        if (configDao == null) {
            throw new ConfigurationException("Unable to get the configuration dao.");
        }

        Map<String, String> configs = configDao.getConfiguration("management-server", params);

        _elasticIpVmRamSize = NumbersUtil.parseInt(configs.get("elasticip.vm.ram.size"), DEFAULT_EIP_VM_RAMSIZE);
        _elasticIpVmCpuMHz = NumbersUtil.parseInt(configs.get("elasticip.vm.cpu.mhz"), DEFAULT_EIP_VM_CPUMHZ);
        String useServiceVM = configDao.getValue("elasticip.vm");
        boolean _useServiceVM = false; //FIXME
        if ("true".equalsIgnoreCase(useServiceVM)) {
            _useServiceVM = true;
        }   

        String value = configs.get("secstorage.capacityscan.interval"); //TODO
        _capacityScanInterval = NumbersUtil.parseLong(value, DEFAULT_CAPACITY_SCAN_INTERVAL);

        _instance = configs.get("instance.name");
        if (_instance == null) {
            _instance = "DEFAULT";
        }


       _itMgr.registerGuru(VirtualMachine.Type.ElasticIpVm, this);

        _useLocalStorage = Boolean.parseBoolean(configs.get(Config.SystemVMUseLocalStorage.key()));
        _serviceOffering = new ServiceOfferingVO("System Offering For Elastic IP VM", 1, _elasticIpVmRamSize, _elasticIpVmCpuMHz, 0, 0, true, null, _useLocalStorage, true, null, true);
        _serviceOffering.setUniqueName("Cloud.com-ElasticIp");
        _serviceOffering = _offeringDao.persistSystemServiceOffering(_serviceOffering);

        if (_useServiceVM) {
            _capacityScanScheduler.scheduleAtFixedRate(getCapacityScanTask(), STARTUP_DELAY, _capacityScanInterval, TimeUnit.MILLISECONDS);
        }
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Elastic ip vm Manager is configured.");
        }
        return true;
    }

    protected ElasticIpServiceManagerImpl() {
    }

    @Override
    public Long convertToId(String vmName) {
        if (!VirtualMachineName.isValidSystemVmName(vmName, _instance, "r")) {
            return null;
        }
        return VirtualMachineName.getSystemVmId(vmName);
    }

    @Override
    public boolean stopElasticIpVm(long elasticIpVmId) {
        DomainRouterVO elasticIpVm = _elasticIpVmDao.findById(elasticIpVmId);
        if (elasticIpVm == null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Stopping elastic ip  vm failed: elastic ip  vm " + elasticIpVmId + " no longer exists");
            }
            return false;
        }
        try {
            if (elasticIpVm.getHostId() != null) {
                GlobalLock elasticIpVmLock = GlobalLock.getInternLock(getElasticIpVmLockName(elasticIpVm.getId()));
                try {
                    if (elasticIpVmLock.lock(ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_SYNC)) {
                        try {
                            boolean result = _itMgr.stop(elasticIpVm, _accountMgr.getSystemUser(), _accountMgr.getSystemAccount());
                            if (result) {
                            }

                            return result;
                        } finally {
                            elasticIpVmLock.unlock();
                        }
                    } else {
                        String msg = "Unable to acquire elastic ip  vm lock : " + elasticIpVm.toString();
                        s_logger.debug(msg);
                        return false;
                    }
                } finally {
                    elasticIpVmLock.releaseRef();
                }
            }

            // vm was already stopped, return true
            return true;
        } catch (ResourceUnavailableException e) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Stopping elastic ip  vm " + elasticIpVm.getHostName() + " faled : exception " + e.toString());
            }
            return false;
        }
    }

    @Override
    public boolean rebootElasticIpVm(long ElasticIpVmId) {
       
        final DomainRouterVO elasticIpVm = _elasticIpVmDao.findById(ElasticIpVmId);

        if (elasticIpVm == null || elasticIpVm.getState() == State.Destroyed) {
            return false;
        }

        if (elasticIpVm.getState() == State.Running && elasticIpVm.getHostId() != null) {
            final RebootCommand cmd = new RebootCommand(elasticIpVm.getInstanceName());
            final Answer answer = _agentMgr.easySend(elasticIpVm.getHostId(), cmd);

            if (answer != null && answer.getResult()) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Successfully reboot elastic ip  vm " + elasticIpVm.getHostName());
                }

              /*  SubscriptionMgr.getInstance().notifySubscribers(ALERT_SUBJECT, this,
                        new ElasticIpVmAlertEventArgs(ElasticIpVmAlertEventArgs.SSVM_REBOOTED, elasticIpVm.getDataCenterId(), elasticIpVm.getId(), elasticIpVm, null));
*/
                return true;
            } else {
                String msg = "Rebooting Elastic Ip VM failed - " + elasticIpVm.getHostName();
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug(msg);
                }
                return false;
            }
        } else {
            return startElasticIpVm(ElasticIpVmId) != null;
        }
    }

    @Override
    public boolean destroyElasticIpVm(long vmId) {
        DomainRouterVO eipVm = _elasticIpVmDao.findById(vmId);

        try {
            return _itMgr.expunge(eipVm, _accountMgr.getSystemUser(), _accountMgr.getSystemAccount());
        } catch (ResourceUnavailableException e) {
            s_logger.warn("Unable to expunge " + eipVm, e);
            return false;
        }
    }


    private String getCapacityScanLockName() {
        return "ElasticIpVm.capacity.scan";
    }

    private String getAllocLockName() {
        return "ElasticIpVm.alloc";
    }

    private String getElasticIpVmLockName(long id) {
        return "ElasticIpVm." + id;
    }

    @Override
    public DomainRouterVO findByName(String name) {
        if (!VirtualMachineName.isValidRouterName(name, null)) {
            return null;
        }
        return findById(VirtualMachineName.getSystemVmId(name));
    }

    @Override
    public DomainRouterVO findById(long id) {
        return _elasticIpVmDao.findById(id);
    }

    @Override
    public DomainRouterVO persist(DomainRouterVO vm) {
        return _elasticIpVmDao.persist(vm);
    }

    @Override
    public boolean finalizeVirtualMachineProfile(VirtualMachineProfile<DomainRouterVO> profile, DeployDestination dest, ReservationContext context) {

        StringBuilder buf = profile.getBootArgsBuilder();
        buf.append(" template=domP type=elasticip");
        buf.append(" name=").append(profile.getHostName());
        NicProfile controlNic = null;
        String defaultDns1 = null;
        String defaultDns2 = null;

        for (NicProfile nic : profile.getNics()) {
            int deviceId = nic.getDeviceId();
            buf.append(" eth").append(deviceId).append("ip=").append(nic.getIp4Address());
            buf.append(" eth").append(deviceId).append("mask=").append(nic.getNetmask());
            if (nic.getTrafficType() == TrafficType.Public) {
                buf.append(" gateway=").append(nic.getGateway());
                defaultDns1 = nic.getDns1();
                defaultDns2 = nic.getDns2();
            }
            if (nic.getTrafficType() == TrafficType.Management) {
                buf.append(" localgw=").append(dest.getPod().getGateway());
            } else if (nic.getTrafficType() == TrafficType.Control) {
                //TODO: VMWare: need to account for route back to management server
                controlNic = nic;
            }
            if (nic.getTrafficType() == TrafficType.Guest) {
                buf.append(" guestgw=").append(nic.getGateway());
            }
        }

        buf.append(" dns1=").append(defaultDns1);
        if (defaultDns2 != null) {
            buf.append(" dns2=").append(defaultDns2);
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Boot Args for " + profile + ": " + buf.toString());
        }

        if (controlNic == null) {
            throw new CloudRuntimeException("Didn't start a control port");
        }

        return true;
    }

    @Override
    public boolean finalizeDeployment(Commands cmds, VirtualMachineProfile<DomainRouterVO> profile, DeployDestination dest, ReservationContext context) {

        finalizeCommandsOnStart(cmds, profile);

        DomainRouterVO elasticIpVm = profile.getVirtualMachine();
        List<NicProfile> nics = profile.getNics();
        for (NicProfile nic : nics) {
            if (nic.getTrafficType() == TrafficType.Public) {
                elasticIpVm.setPublicIpAddress(nic.getIp4Address());
                elasticIpVm.setPublicNetmask(nic.getNetmask());
                elasticIpVm.setPublicMacAddress(nic.getMacAddress());
            } else if (nic.getTrafficType() == TrafficType.Management) {
                elasticIpVm.setPrivateIpAddress(nic.getIp4Address());
                elasticIpVm.setPrivateMacAddress(nic.getMacAddress());
            }
        }
        _elasticIpVmDao.update(elasticIpVm.getId(), elasticIpVm);
        return true;
    }

    @Override
    public boolean finalizeCommandsOnStart(Commands cmds, VirtualMachineProfile<DomainRouterVO> profile) {

        NicProfile managementNic = null;
        NicProfile controlNic = null;
        for (NicProfile nic : profile.getNics()) {
            if (nic.getTrafficType() == TrafficType.Management) {
                managementNic = nic;
            } else if (nic.getTrafficType() == TrafficType.Control && nic.getIp4Address() != null) {
                controlNic = nic;
            }
        }

        if (controlNic == null) {
            if (managementNic == null) {
                s_logger.error("Management network doesn't exist for the elastic ip vm " + profile.getVirtualMachine());
                return false;
            }
            controlNic = managementNic;
        }

        CheckSshCommand check = new CheckSshCommand(profile.getInstanceName(), controlNic.getIp4Address(), 3922, 5, 20);
        cmds.addCommand("checkSsh", check);

        return true;
    }

    @Override
    public boolean finalizeStart(VirtualMachineProfile<DomainRouterVO> profile, long hostId, Commands cmds, ReservationContext context) {
        CheckSshAnswer answer = (CheckSshAnswer) cmds.getAnswer("checkSsh");
        if (!answer.getResult()) {
            s_logger.warn("Unable to ssh to the VM: " + answer.getDetails());
            return false;
        }

        return true;
    }

    @Override
    public void finalizeStop(VirtualMachineProfile<DomainRouterVO> profile, StopAnswer answer) {
    }

    @Override
    public void finalizeExpunge(DomainRouterVO vm) {
    }
}
