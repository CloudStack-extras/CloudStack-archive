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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.HostCreator;
import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.GetHostStatsAnswer;
import com.cloud.agent.api.GetHostStatsCommand;
import com.cloud.agent.api.MaintainCommand;
import com.cloud.agent.api.PingAnswer;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingRoutingCommand;
import com.cloud.agent.api.PoolEjectCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.ShutdownCommand;
import com.cloud.agent.api.StartupAnswer;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupExternalDhcpCommand;
import com.cloud.agent.api.StartupExternalFirewallCommand;
import com.cloud.agent.api.StartupExternalLoadBalancerCommand;
import com.cloud.agent.api.StartupProxyCommand;
import com.cloud.agent.api.StartupPxeServerCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.agent.api.StartupStorageCommand;
import com.cloud.agent.api.UnsupportedAnswer;
import com.cloud.agent.manager.allocator.HostAllocator;
import com.cloud.agent.manager.allocator.PodAllocator;
import com.cloud.agent.transport.Request;
import com.cloud.agent.transport.Response;
import com.cloud.alert.AlertManager;
import com.cloud.api.commands.AddClusterCmd;
import com.cloud.api.commands.AddHostCmd;
import com.cloud.api.commands.AddSecondaryStorageCmd;
import com.cloud.api.commands.CancelMaintenanceCmd;
import com.cloud.api.commands.DeleteClusterCmd;
import com.cloud.api.commands.DeleteHostCmd;
import com.cloud.api.commands.PrepareForMaintenanceCmd;
import com.cloud.api.commands.ReconnectHostCmd;
import com.cloud.api.commands.UpdateHostCmd;
import com.cloud.capacity.Capacity;
import com.cloud.capacity.CapacityVO;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.ClusterDetailsVO;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenterIpAddressVO;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.PodCluster;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.DataCenterIpAddressDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlanner;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.event.dao.EventDao;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.ConnectionException;
import com.cloud.exception.DiscoveryException;
import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.UnsupportedVersionException;
import com.cloud.ha.HighAvailabilityManager;
import com.cloud.ha.HighAvailabilityManager.WorkType;
import com.cloud.host.DetailVO;
import com.cloud.host.Host;
import com.cloud.host.Host.HostAllocationState;
import com.cloud.host.Host.Type;
import com.cloud.host.HostStats;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.Status.Event;
import com.cloud.host.dao.DetailsDao;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostTagsDao;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.kvm.resource.KvmDummyResourceBase;
import com.cloud.maid.StackMaid;
import com.cloud.maint.UpgradeManager;
import com.cloud.network.IPAddressVO;
import com.cloud.network.NetworkManager;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.offering.ServiceOffering;
import com.cloud.org.Cluster;
import com.cloud.org.Grouping;
import com.cloud.resource.Discoverer;
import com.cloud.resource.ResourceService;
import com.cloud.resource.ServerResource;
import com.cloud.server.ManagementServer;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.storage.GuestOSCategoryVO;
import com.cloud.storage.Storage;
import com.cloud.storage.StorageManager;
import com.cloud.storage.StoragePoolVO;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.GuestOSCategoryDao;
import com.cloud.storage.dao.StoragePoolDao;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateHostDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.resource.DummySecondaryStorageResource;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.UserContext;
import com.cloud.user.dao.UserStatisticsDao;
import com.cloud.uservm.UserVm;
import com.cloud.utils.ActionDelegate;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.StringUtils;
import com.cloud.utils.UriUtils;
import com.cloud.utils.component.Adapters;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.component.Manager;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.Ip;
import com.cloud.utils.net.MacAddress;
import com.cloud.utils.net.NetUtils;
import com.cloud.utils.nio.HandlerFactory;
import com.cloud.utils.nio.Link;
import com.cloud.utils.nio.NioServer;
import com.cloud.utils.nio.Task;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachineProfileImpl;
import com.cloud.vm.dao.VMInstanceDao;

/**
 * Implementation of the Agent Manager. This class controls the connection to
 * the agents.
 * 
 * @config {@table || Param Name | Description | Values | Default || || port |
 *         port to listen on for agent connection. | Integer | 8250 || ||
 *         workers | # of worker threads | Integer | 5 || || router.template.id
 *         | default id for template | Integer | 1 || || router.ram.size |
 *         default ram for router vm in mb | Integer | 128 || ||
 *         router.ip.address | ip address for the router | ip | 10.1.1.1 || ||
 *         wait | Time to wait for control commands to return | seconds | 1800
 *         || || domain | domain for domain routers| String | foo.com || ||
 *         alert.wait | time to wait before alerting on a disconnected agent |
 *         seconds | 1800 || || update.wait | time to wait before alerting on a
 *         updating agent | seconds | 600 || || ping.interval | ping interval in
 *         seconds | seconds | 60 || || instance.name | Name of the deployment
 *         String | required || || start.retry | Number of times to retry start
 *         | Number | 2 || || ping.timeout | multiplier to ping.interval before
 *         announcing an agent has timed out | float | 2.0x || ||
 *         router.stats.interval | interval to report router statistics |
 *         seconds | 300s || * }
 **/
@Local(value = { AgentManager.class, ResourceService.class })
public class AgentManagerImpl implements AgentManager, HandlerFactory,
		ResourceService, Manager {
	private static final Logger s_logger = Logger
			.getLogger(AgentManagerImpl.class);

	protected ConcurrentHashMap<Long, AgentAttache> _agents = new ConcurrentHashMap<Long, AgentAttache>(
			10007);
	protected List<Pair<Integer, Listener>> _hostMonitors = new ArrayList<Pair<Integer, Listener>>(
			17);
	protected List<Pair<Integer, Listener>> _cmdMonitors = new ArrayList<Pair<Integer, Listener>>(
			17);
	protected List<Pair<Integer, HostCreator>> _creationMonitors = new ArrayList<Pair<Integer, HostCreator>>(
	            17);
	protected int _monitorId = 0;

	protected NioServer _connection;
	@Inject
	protected HostDao _hostDao = null;
	@Inject
	protected UserStatisticsDao _userStatsDao = null;
	@Inject
	protected DataCenterDao _dcDao = null;
	@Inject
	protected VlanDao _vlanDao = null;
	@Inject
	protected DataCenterIpAddressDao _privateIPAddressDao = null;
	@Inject
	protected IPAddressDao _publicIPAddressDao = null;
	@Inject
	protected HostPodDao _podDao = null;
	protected Adapters<HostAllocator> _hostAllocators = null;
	protected Adapters<PodAllocator> _podAllocators = null;
	@Inject
	protected EventDao _eventDao = null;
	@Inject
	protected VMInstanceDao _vmDao = null;
	@Inject
	protected VolumeDao _volDao = null;
	@Inject
	protected CapacityDao _capacityDao = null;
	@Inject
	protected ConfigurationDao _configDao = null;
	@Inject
	protected StoragePoolDao _storagePoolDao = null;
	@Inject
	protected StoragePoolHostDao _storagePoolHostDao = null;
	@Inject
	protected GuestOSCategoryDao _guestOSCategoryDao = null;
	@Inject
	protected DetailsDao _hostDetailsDao = null;
	@Inject
	protected ClusterDao _clusterDao = null;
	@Inject
	protected ClusterDetailsDao _clusterDetailsDao = null;
    @Inject 
    protected HostTagsDao _hostTagsDao = null;
    
	@Inject(adapter = DeploymentPlanner.class)
	private Adapters<DeploymentPlanner> _planners;

	protected Adapters<Discoverer> _discoverers = null;
	protected int _port;

	@Inject
	protected HighAvailabilityManager _haMgr = null;
	@Inject
	protected AlertManager _alertMgr = null;

	@Inject
	protected NetworkManager _networkMgr = null;

	@Inject
	protected UpgradeManager _upgradeMgr = null;

	@Inject
	protected StorageManager _storageMgr = null;
	
    @Inject
    protected AccountManager _accountMgr = null;

	protected int _retry = 2;

	protected String _name;
	protected String _instance;

	protected int _wait;
	protected int _updateWait;
	protected int _alertWait;
	protected long _nodeId = -1;
	protected int _overProvisioningFactor = 1;
	protected float _cpuOverProvisioningFactor = 1;

	protected Random _rand = new Random(System.currentTimeMillis());

	protected int _pingInterval;
	protected long _pingTimeout;
	protected AgentMonitor _monitor = null;

	protected ExecutorService _executor;

	@Inject
	protected VMTemplateDao _tmpltDao;
	@Inject
	protected VMTemplateHostDao _vmTemplateHostDao;

	@Override
	public boolean configure(final String name, final Map<String, Object> params)
			throws ConfigurationException {
		_name = name;

		Request.initBuilder();

		final ComponentLocator locator = ComponentLocator.getCurrentLocator();
		ConfigurationDao configDao = locator.getDao(ConfigurationDao.class);
		if (configDao == null) {
			throw new ConfigurationException(
					"Unable to get the configuration dao.");
		}

		final Map<String, String> configs = configDao.getConfiguration(
				"AgentManager", params);
		_port = NumbersUtil.parseInt(configs.get("port"), 8250);
		final int workers = NumbersUtil.parseInt(configs.get("workers"), 5);

		String value = configs.get("ping.interval");
		_pingInterval = NumbersUtil.parseInt(value, 60);

		value = configs.get("wait");
		_wait = NumbersUtil.parseInt(value, 1800) * 1000;

		value = configs.get("alert.wait");
		_alertWait = NumbersUtil.parseInt(value, 1800);

		value = configs.get("update.wait");
		_updateWait = NumbersUtil.parseInt(value, 600);

		value = configs.get("ping.timeout");
		final float multiplier = value != null ? Float.parseFloat(value) : 2.5f;
		_pingTimeout = (long) (multiplier * _pingInterval);

		s_logger.info("Ping Timeout is " + _pingTimeout);

		_instance = configs.get("instance.name");
		if (_instance == null) {
			_instance = "DEFAULT";
		}

		_hostAllocators = locator.getAdapters(HostAllocator.class);
		if (_hostAllocators == null || !_hostAllocators.isSet()) {
			throw new ConfigurationException(
					"Unable to find an host allocator.");
		}

		_podAllocators = locator.getAdapters(PodAllocator.class);
		if (_podAllocators == null || !_podAllocators.isSet()) {
			throw new ConfigurationException("Unable to find an pod allocator.");
		}

		_discoverers = locator.getAdapters(Discoverer.class);

		if (_nodeId == -1) {
			// FIXME: We really should not do this like this. It should be done
			// at config time and is stored as a config variable.
			_nodeId = MacAddress.getMacAddress().toLong();
		}

		_hostDao.markHostsAsDisconnected(_nodeId, Status.Up, Status.Connecting,
				Status.Updating, Status.Disconnected, Status.Down);

		_monitor = new AgentMonitor(_nodeId, _hostDao, _volDao, _vmDao, _dcDao,
				_podDao, this, _alertMgr, _pingTimeout);
		registerForHostEvents(_monitor, true, true, false);

		_executor = new ThreadPoolExecutor(10, 100, 60l, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(
						"AgentTaskPool"));

		String overProvisioningFactorStr = configs
				.get("storage.overprovisioning.factor");
		_overProvisioningFactor = NumbersUtil.parseInt(
				overProvisioningFactorStr, 1);

		String cpuOverProvisioningFactorStr = configs
				.get("cpu.overprovisioning.factor");
		_cpuOverProvisioningFactor = NumbersUtil.parseFloat(
				cpuOverProvisioningFactorStr, 1);
		if (_cpuOverProvisioningFactor < 1) {
			_cpuOverProvisioningFactor = 1;
		}

		_connection = new NioServer("AgentManager", _port, workers + 10, this);

		s_logger.info("Listening on " + _port + " with " + workers + " workers");
		return true;
	}

	@Override
	public boolean isHostNativeHAEnabled(long hostId) {
        HostVO host = _hostDao.findById(hostId);
        if (host.getClusterId() != null) {
            ClusterDetailsVO detail = _clusterDetailsDao.findDetail(host.getClusterId(), "NativeHA");
            return detail == null ? false : Boolean.parseBoolean(detail.getValue());
        }
        return false;
    }
    
	@Override
	public Task create(Task.Type type, Link link, byte[] data) {
		return new AgentHandler(type, link, data);
	}

	@Override
	public int registerForHostEvents(final Listener listener,
			boolean connections, boolean commands, boolean priority) {
		synchronized (_hostMonitors) {
			_monitorId++;
			if (connections) {
				if (priority) {
					_hostMonitors.add(0, new Pair<Integer, Listener>(
							_monitorId, listener));
				} else {
					_hostMonitors.add(new Pair<Integer, Listener>(_monitorId,
							listener));
				}
			}
			if (commands) {
				if (priority) {
					_cmdMonitors.add(0, new Pair<Integer, Listener>(_monitorId,
							listener));
				} else {
					_cmdMonitors.add(new Pair<Integer, Listener>(_monitorId,
							listener));
				}
			}
			if (s_logger.isDebugEnabled()) {
				s_logger.debug("Registering listener "
						+ listener.getClass().getSimpleName() + " with id "
						+ _monitorId);
			}
			return _monitorId;
		}
	}
	
	@Override
	public int registerForInitialConnects(final HostCreator creator,boolean priority) {
	    synchronized (_hostMonitors) {
	        _monitorId++;

	        if (priority) {
	            _creationMonitors.add(0, new Pair<Integer, HostCreator>(
	                    _monitorId, creator));
	        } else {
	            _creationMonitors.add(0, new Pair<Integer, HostCreator>(
	                    _monitorId, creator));
	        }
	    }

	    return _monitorId;
	}



	@Override
	public void unregisterForHostEvents(final int id) {
		s_logger.debug("Deregistering " + id);
		_hostMonitors.remove(id);
	}

	private AgentControlAnswer handleControlCommand(AgentAttache attache,
			final AgentControlCommand cmd) {
		AgentControlAnswer answer = null;

		for (Pair<Integer, Listener> listener : _cmdMonitors) {
			answer = listener.second().processControlCommand(attache.getId(),
					cmd);

			if (answer != null) {
				return answer;
			}
		}

		s_logger.warn("No handling of agent control command: " + cmd.toString()
				+ " sent from " + attache.getId());
		return new AgentControlAnswer(cmd);
	}

	public void handleCommands(AgentAttache attache, final long sequence,
			final Command[] cmds) {
		for (Pair<Integer, Listener> listener : _cmdMonitors) {
			boolean processed = listener.second().processCommands(
					attache.getId(), sequence, cmds);
			if (s_logger.isTraceEnabled()) {
				s_logger.trace("SeqA " + attache.getId() + "-" + sequence
						+ ": " + (processed ? "processed" : "not processed")
						+ " by " + listener.getClass());
			}
		}
	}

	public AgentAttache findAttache(long hostId) {
		return _agents.get(hostId);
	}

	@Override
	public Set<Long> getConnectedHosts() {
		// make the returning set be safe for concurrent iteration
		final HashSet<Long> result = new HashSet<Long>();

		synchronized (_agents) {
			final Set<Long> s = _agents.keySet();
			for (final Long id : s) {
				result.add(id);
			}
		}
		return result;
	}

	@Override
	public Host findHost(final Host.Type type, final DataCenterVO dc,
			final HostPodVO pod, final StoragePoolVO sp,
			final ServiceOfferingVO offering, final VMTemplateVO template,
			VMInstanceVO vm, Host currentHost, final Set<Host> avoid) {
		VirtualMachineProfileImpl<VMInstanceVO> vmProfile = new VirtualMachineProfileImpl<VMInstanceVO>(
				vm, template, offering, null, null);
		DeployDestination dest = null;
		DataCenterDeployment plan = new DataCenterDeployment(dc.getId(),
				pod.getId(), sp.getClusterId(), null, null);
		ExcludeList avoids = new ExcludeList();
		for (Host h : avoid) {
			avoids.addHost(h.getId());
		}

		for (DeploymentPlanner planner : _planners) {
			try {
				dest = planner.plan(vmProfile, plan, avoids);
				if (dest != null) {
					return dest.getHost();
				}
			} catch (InsufficientServerCapacityException e) {

			} 
		}

		s_logger.warn("findHost() could not find a non-null host.");
		return null;
	}

	@Override
	public List<PodCluster> listByDataCenter(long dcId) {
		List<HostPodVO> pods = _podDao.listByDataCenterId(dcId);
		ArrayList<PodCluster> pcs = new ArrayList<PodCluster>();
		for (HostPodVO pod : pods) {
			List<ClusterVO> clusters = _clusterDao.listByPodId(pod.getId());
			if (clusters.size() == 0) {
				pcs.add(new PodCluster(pod, null));
			} else {
				for (ClusterVO cluster : clusters) {
					pcs.add(new PodCluster(pod, cluster));
				}
			}
		}
		return pcs;
	}

	@Override
	public List<PodCluster> listByPod(long podId) {
		ArrayList<PodCluster> pcs = new ArrayList<PodCluster>();
		HostPodVO pod = _podDao.findById(podId);
		if (pod == null) {
			return pcs;
		}
		List<ClusterVO> clusters = _clusterDao.listByPodId(pod.getId());
		if (clusters.size() == 0) {
			pcs.add(new PodCluster(pod, null));
		} else {
			for (ClusterVO cluster : clusters) {
				pcs.add(new PodCluster(pod, cluster));
			}
		}
		return pcs;
	}

	protected AgentAttache handleDirectConnect(ServerResource resource,
			StartupCommand[] startup, Map<String, String> details, boolean old, List<String> hostTags, String allocationState)
			throws ConnectionException {
		if (startup == null) {
			return null;
		}
		HostVO server = createHost(startup, resource, details, old, hostTags, allocationState);
		if (server == null) {
			return null;
		}

		long id = server.getId();

		AgentAttache attache = createAttache(id, server, resource);

		attache = notifyMonitorsOfConnection(attache, startup);

		return attache;
	}

	@Override
	public List<? extends Cluster> discoverCluster(AddClusterCmd cmd)
			throws IllegalArgumentException, DiscoveryException,
			InvalidParameterValueException {
		Long dcId = cmd.getZoneId();
		Long podId = cmd.getPodId();
		String clusterName = cmd.getClusterName();
		String url = cmd.getUrl();
		String username = cmd.getUsername();
		String password = cmd.getPassword();
		
		URI uri = null;

		// Check if the zone exists in the system
		DataCenterVO zone = _dcDao.findById(dcId);
		if (zone == null) {
			throw new InvalidParameterValueException("Can't find zone by id "
					+ dcId);
		}
		
		Account account = UserContext.current().getCaller();
		if(Grouping.AllocationState.Disabled == zone.getAllocationState() && !_accountMgr.isRootAdmin(account.getType())){
			throw new PermissionDeniedException("Cannot perform this operation, Zone is currently disabled: "+ dcId );
		}
		
		// Check if the pod exists in the system
		if (podId != null) {
			if (_podDao.findById(podId) == null) {
				throw new InvalidParameterValueException(
						"Can't find pod by id " + podId);
			}
			// check if pod belongs to the zone
			HostPodVO pod = _podDao.findById(podId);
			if (!Long.valueOf(pod.getDataCenterId()).equals(dcId)) {
				throw new InvalidParameterValueException("Pod " + podId
						+ " doesn't belong to the zone " + dcId);
			}
		}

		// Verify cluster information and create a new cluster if needed
		if (clusterName == null || clusterName.isEmpty()) {
			throw new InvalidParameterValueException(
					"Please specify cluster name");
		}

		if (cmd.getHypervisor() == null || cmd.getHypervisor().isEmpty()) {
			throw new InvalidParameterValueException(
					"Please specify a hypervisor");
		}

		Hypervisor.HypervisorType hypervisorType = Hypervisor.HypervisorType.getType(cmd.getHypervisor());
		if (hypervisorType == null) {
			s_logger.error("Unable to resolve " + cmd.getHypervisor() + " to a valid supported hypervisor type");
			throw new InvalidParameterValueException("Unable to resolve " + cmd.getHypervisor() + " to a supported ");
		}

		Cluster.ClusterType clusterType = null;
		if (cmd.getClusterType() != null && !cmd.getClusterType().isEmpty()) {
			clusterType = Cluster.ClusterType.valueOf(cmd.getClusterType());
		}
		if (clusterType == null) {
			clusterType = Cluster.ClusterType.CloudManaged;
		}
		
		Grouping.AllocationState allocationState = null;
		if (cmd.getAllocationState() != null && !cmd.getAllocationState().isEmpty()) {
			try{
				allocationState = Grouping.AllocationState.valueOf(cmd.getAllocationState());
			}catch(IllegalArgumentException ex){
				throw new InvalidParameterValueException("Unable to resolve Allocation State '" + cmd.getAllocationState() + "' to a supported state");
			}
		}
		if (allocationState == null) {
			allocationState = Grouping.AllocationState.Enabled;
		}
		
		Discoverer discoverer = getMatchingDiscover(hypervisorType);
		if (discoverer == null) {
			
			throw new InvalidParameterValueException("Could not find corresponding resource manager for " + cmd.getHypervisor());
		}

		List<ClusterVO> result = new ArrayList<ClusterVO>();

		long clusterId = 0;
		ClusterVO cluster = new ClusterVO(dcId, podId, clusterName);
		cluster.setHypervisorType(cmd.getHypervisor());

		cluster.setClusterType(clusterType);
		cluster.setAllocationState(allocationState);
		try {
			cluster = _clusterDao.persist(cluster);
		} catch (Exception e) {
			// no longer tolerate exception during the cluster creation phase
			throw new CloudRuntimeException("Unable to create cluster "
					+ clusterName + " in pod " + podId + " and data center "
					+ dcId, e);
		}
		clusterId = cluster.getId();
		result.add(cluster);

		if (clusterType == Cluster.ClusterType.CloudManaged) {
			return result;
		}

		// save cluster details for later cluster/host cross-checking
		Map<String, String> details = new HashMap<String, String>();
		details.put("url", url);
		details.put("username", username);
		details.put("password", password);
		_clusterDetailsDao.persist(cluster.getId(), details);

		boolean success = false;
		try {
			try {
				uri = new URI(UriUtils.encodeURIComponent(url));
				if (uri.getScheme() == null) {
					throw new InvalidParameterValueException(
							"uri.scheme is null " + url + ", add http:// as a prefix");
				} else if (uri.getScheme().equalsIgnoreCase("http")) {
					if (uri.getHost() == null
							|| uri.getHost().equalsIgnoreCase("")
							|| uri.getPath() == null
							|| uri.getPath().equalsIgnoreCase("")) {
						throw new InvalidParameterValueException(
								"Your host and/or path is wrong.  Make sure it's of the format http://hostname/path");
					}
				}
			} catch (URISyntaxException e) {
				throw new InvalidParameterValueException(url
						+ " is not a valid uri");
			}

			List<HostVO> hosts = new ArrayList<HostVO>();
			Map<? extends ServerResource, Map<String, String>> resources = null;

			try {
				resources = discoverer.find(dcId, podId, clusterId, uri, username, password);
			} catch (Exception e) {
				s_logger.info("Exception in external cluster discovery process with discoverer: "
						+ discoverer.getName());
			}
			if (resources != null) {
				for (Map.Entry<? extends ServerResource, Map<String, String>> entry : resources.entrySet()) {
					ServerResource resource = entry.getKey();
					AgentAttache attache = simulateStart(resource, entry.getValue(), true, null, null);
					if (attache != null) {
						hosts.add(_hostDao.findById(attache.getId()));
					}
					discoverer.postDiscovery(hosts, _nodeId);
				}
				s_logger.info("External cluster has been successfully discovered by " + discoverer.getName());
				success = true;
				return result;
			}

			s_logger.warn("Unable to find the server resources at " + url);
			throw new DiscoveryException("Unable to add the external cluster");
		} catch(Throwable e) {
			s_logger.error("Unexpected exception ", e);
			throw new DiscoveryException("Unable to add the external cluster due to unhandled exception");
		} finally {
			if (!success) {
				_clusterDetailsDao.deleteDetails(clusterId);
				_clusterDao.remove(clusterId);
			}
		}
	}

	private Discoverer getMatchingDiscover(
			Hypervisor.HypervisorType hypervisorType) {
		Enumeration<Discoverer> en = _discoverers.enumeration();
		while (en.hasMoreElements()) {
			Discoverer discoverer = en.nextElement();
			if (discoverer.getHypervisorType() == hypervisorType) {
				return discoverer;
			}
		}
		return null;
	}

	@Override
	public List<? extends Host> discoverHosts(AddHostCmd cmd)
			throws IllegalArgumentException, DiscoveryException,
			InvalidParameterValueException {
		Long dcId = cmd.getZoneId();
		Long podId = cmd.getPodId();
		Long clusterId = cmd.getClusterId();
		String clusterName = cmd.getClusterName();
		String url = cmd.getUrl();
		String username = cmd.getUsername();
		String password = cmd.getPassword();
		Long memCapacity = cmd.getMemCapacity();
		Long cpuCapacity = cmd.getCpuCapacity();
		Long cpuNum = cmd.getCpuNum();
		String mac = cmd.getMac();
		String hostTag = cmd.getHostTag();
		Map<String, String>bareMetalParams = new HashMap<String, String>();
		
		// this is for standalone option
		if (clusterName == null && clusterId == null) {
			clusterName = "Standalone-" + url;
		}
		
		if (cmd.getHypervisor().equalsIgnoreCase(Hypervisor.HypervisorType.BareMetal.toString())) {
			if (memCapacity == null) {
				memCapacity = Long.valueOf(0);
			}
			if (cpuCapacity == null) {
				cpuCapacity = Long.valueOf(0);
			}
			if (cpuNum == null) {
				cpuNum = Long.valueOf(0);
			}
			if (mac == null) {
				mac = "unknown";
			}
			
			bareMetalParams.put("cpuNum", cpuNum.toString());
			bareMetalParams.put("cpuCapacity", cpuCapacity.toString());
			bareMetalParams.put("memCapacity", memCapacity.toString());
			bareMetalParams.put("mac", mac);
			if (hostTag != null) {
				bareMetalParams.put("hostTag", hostTag);
			}
		}
		
		List<String> hostTags = cmd.getHostTags();
		String allocationState = cmd.getAllocationState();
		if (allocationState == null) {
			allocationState = Host.HostAllocationState.Enabled.toString();
		}
		
		return discoverHostsFull(dcId, podId, clusterId, clusterName, url,
				username, password, cmd.getHypervisor(), hostTags, bareMetalParams, allocationState);
	}

	@Override
	public List<? extends Host> discoverHosts(AddSecondaryStorageCmd cmd)
			throws IllegalArgumentException, DiscoveryException,
			InvalidParameterValueException {
		Long dcId = cmd.getZoneId();
		String url = cmd.getUrl();
		return discoverHosts(dcId, null, null, null, url, null, null,
				"SecondaryStorage", null);
	}

	@Override
	public List<HostVO> discoverHosts(Long dcId, Long podId, Long clusterId,
			String clusterName, String url, String username, String password,
			String hypervisorType, List<String> hostTags) throws IllegalArgumentException,
			DiscoveryException, InvalidParameterValueException {
		return discoverHostsFull(dcId, podId, clusterId, clusterName, url, username, password, hypervisorType, hostTags, null, null);
	}
	
	
	private List<HostVO> discoverHostsFull(Long dcId, Long podId, Long clusterId,
			String clusterName, String url, String username, String password,
			String hypervisorType, List<String>hostTags, Map<String, String>params, String allocationState) throws IllegalArgumentException,
			DiscoveryException, InvalidParameterValueException {
		URI uri = null;

		// Check if the zone exists in the system
		DataCenterVO zone = _dcDao.findById(dcId);
		if (zone == null) {
			throw new InvalidParameterValueException("Can't find zone by id "
					+ dcId);
		}
		
		Account account = UserContext.current().getCaller();
		if(Grouping.AllocationState.Disabled == zone.getAllocationState() && !_accountMgr.isRootAdmin(account.getType())){
			throw new PermissionDeniedException("Cannot perform this operation, Zone is currently disabled: "+ dcId );
		}

		// Check if the pod exists in the system
		if (podId != null) {
			if (_podDao.findById(podId) == null) {
				throw new InvalidParameterValueException(
						"Can't find pod by id " + podId);
			}
			// check if pod belongs to the zone
			HostPodVO pod = _podDao.findById(podId);
			if (!Long.valueOf(pod.getDataCenterId()).equals(dcId)) {
				throw new InvalidParameterValueException("Pod " + podId
						+ " doesn't belong to the zone " + dcId);
			}
		}

		// Deny to add a secondary storage multiple times for the same zone
		if ((username == null)
				&& (_hostDao.findSecondaryStorageHost(dcId) != null)) {
			throw new InvalidParameterValueException(
					"A secondary storage host already exists in the specified zone");
		}

		// Verify cluster information and create a new cluster if needed
		if (clusterName != null && clusterId != null) {
			throw new InvalidParameterValueException(
					"Can't specify cluster by both id and name");
		}

		if (hypervisorType == null || hypervisorType.isEmpty()) {
			throw new InvalidParameterValueException(
					"Need to specify Hypervisor Type");
		}

		if ((clusterName != null || clusterId != null) && podId == null) {
			throw new InvalidParameterValueException(
					"Can't specify cluster without specifying the pod");
		}

		if (clusterId != null) {
			if (_clusterDao.findById(clusterId) == null) {
				throw new InvalidParameterValueException(
						"Can't find cluster by id " + clusterId);
			}
		}

		if (clusterName != null) {
			ClusterVO cluster = new ClusterVO(dcId, podId, clusterName);
			cluster.setHypervisorType(hypervisorType);
			try {
				cluster = _clusterDao.persist(cluster);
			} catch (Exception e) {
				cluster = _clusterDao.findBy(clusterName, podId);
				if (cluster == null) {
					throw new CloudRuntimeException("Unable to create cluster "
							+ clusterName + " in pod " + podId
							+ " and data center " + dcId, e);
				}
			}
			clusterId = cluster.getId();
		}

		try {
			uri = new URI(UriUtils.encodeURIComponent(url));
			if (uri.getScheme() == null) {
				throw new InvalidParameterValueException("uri.scheme is null "
						+ url + ", add nfs:// as a prefix");
			} else if (uri.getScheme().equalsIgnoreCase("nfs")) {
				if (uri.getHost() == null || uri.getHost().equalsIgnoreCase("")
						|| uri.getPath() == null
						|| uri.getPath().equalsIgnoreCase("")) {
					throw new InvalidParameterValueException(
							"Your host and/or path is wrong.  Make sure it's of the format nfs://hostname/path");
				}
			}
		} catch (URISyntaxException e) {
			throw new InvalidParameterValueException(url
					+ " is not a valid uri");
		}

		List<HostVO> hosts = new ArrayList<HostVO>();
		s_logger.info("Trying to add a new host at " + url + " in data center "
				+ dcId);
		Enumeration<Discoverer> en = _discoverers.enumeration();
		boolean isHypervisorTypeSupported = false;
		while (en.hasMoreElements()) {
			Discoverer discoverer = en.nextElement();
			if (params != null) {
				discoverer.putParam(params);
			}
			
			if (!discoverer.matchHypervisor(hypervisorType)) {
				continue;
			}
			isHypervisorTypeSupported = true;
			Map<? extends ServerResource, Map<String, String>> resources = null;

			try {
				resources = discoverer.find(dcId, podId, clusterId, uri,
						username, password);
			} catch (Exception e) {
				s_logger.info("Exception in host discovery process with discoverer: "
						+ discoverer.getName()
						+ ", skip to another discoverer if there is any");
			}
			if (resources != null) {
				for (Map.Entry<? extends ServerResource, Map<String, String>> entry : resources
						.entrySet()) {
					ServerResource resource = entry.getKey();
					/*
					 * For KVM, if we go to here, that means kvm agent is
					 * already connected to mgt svr.
					 */
					if (resource instanceof KvmDummyResourceBase) {
						Map<String, String> details = entry.getValue();
						String guid = details.get("guid");
						List<HostVO> kvmHosts = _hostDao.listBy(
								Host.Type.Routing, clusterId, podId, dcId);
						for (HostVO host : kvmHosts) {
							if (host.getGuid().equalsIgnoreCase(guid)) {
								hosts.add(host);
								return hosts;
							}
						}
						return null;
					}
					AgentAttache attache = simulateStart(resource,
							entry.getValue(), true, hostTags, allocationState);
					if (attache != null) {
						hosts.add(_hostDao.findById(attache.getId()));
					}
					discoverer.postDiscovery(hosts, _nodeId);

				}
				s_logger.info("server resources successfully discovered by "
						+ discoverer.getName());
				return hosts;
			}
		}
		if (!isHypervisorTypeSupported) {
			String msg = "Do not support HypervisorType " + hypervisorType
					+ " for " + url;
			s_logger.warn(msg);
			throw new DiscoveryException(msg);
		}
		s_logger.warn("Unable to find the server resources at " + url);
		throw new DiscoveryException("Unable to add the host");
	}

	@Override
	@DB
	public boolean deleteCluster(DeleteClusterCmd cmd)
			throws InvalidParameterValueException {
		Transaction txn = Transaction.currentTxn();
		try {
			txn.start();
			ClusterVO cluster = _clusterDao.lockRow(cmd.getId(), true);
			if (cluster == null) {
				if (s_logger.isDebugEnabled()) {
					s_logger.debug("Cluster: " + cmd.getId()
							+ " does not even exist.  Delete call is ignored.");
				}
				txn.rollback();
				return true;
			}

			List<HostVO> hosts = _hostDao.listByCluster(cmd.getId());
			if (hosts.size() > 0) {
				if (s_logger.isDebugEnabled()) {
					s_logger.debug("Cluster: " + cmd.getId()
							+ " still has hosts");
				}
				txn.rollback();
				return false;
			}

			_clusterDao.remove(cmd.getId());

			txn.commit();
			return true;
		} catch (Throwable t) {
			s_logger.error("Unable to delete cluster: " + cmd.getId(), t);
			txn.rollback();
			return false;
		}
	}
	
	@Override
	@DB
	public Cluster updateCluster(Cluster clusterToUpdate, String clusterType, String hypervisor, String allocationState)
	throws InvalidParameterValueException {
		
		ClusterVO cluster = (ClusterVO)clusterToUpdate;
		// Verify cluster information and update the cluster if needed
		boolean doUpdate = false;

		if (hypervisor != null && !hypervisor.isEmpty()) {
			Hypervisor.HypervisorType hypervisorType = Hypervisor.HypervisorType.getType(hypervisor);
			if (hypervisorType == null) {
				s_logger.error("Unable to resolve " + hypervisor + " to a valid supported hypervisor type");
				throw new InvalidParameterValueException("Unable to resolve " + hypervisor + " to a supported type");
			}else{
				cluster.setHypervisorType(hypervisor);
				doUpdate = true;
			}
		}


		Cluster.ClusterType newClusterType = null;
		if (clusterType != null && !clusterType.isEmpty()) {
			try{
				newClusterType = Cluster.ClusterType.valueOf(clusterType);
			}catch(IllegalArgumentException ex){
				throw new InvalidParameterValueException("Unable to resolve " + clusterType + " to a supported type");
			}
			if (newClusterType == null) {
				s_logger.error("Unable to resolve " + clusterType + " to a valid supported cluster type");
				throw new InvalidParameterValueException("Unable to resolve " + clusterType + " to a supported type");
			}else{
				cluster.setClusterType(newClusterType);
				doUpdate = true;
			}			
		}
		
		Grouping.AllocationState newAllocationState = null;
		if (allocationState != null && !allocationState.isEmpty()) {
			try{
				newAllocationState = Grouping.AllocationState.valueOf(allocationState);
			}catch(IllegalArgumentException ex){
				throw new InvalidParameterValueException("Unable to resolve Allocation State '" + allocationState + "' to a supported state");
			}
			if (newAllocationState == null) {
				s_logger.error("Unable to resolve " + allocationState + " to a valid supported allocation State");
				throw new InvalidParameterValueException("Unable to resolve " + allocationState + " to a supported state");
			}else{
				cluster.setAllocationState(newAllocationState);
				doUpdate = true;
			}
		}
		if(doUpdate){
			Transaction txn = Transaction.currentTxn();
			try {
				txn.start();			
				_clusterDao.update(cluster.getId(), cluster);
				txn.commit();
			} catch(Exception e) {
				s_logger.error("Unable to update cluster due to " + e.getMessage(), e);
				throw new CloudRuntimeException("Failed to update cluster. Please contact Cloud Support.");
			}
		}
		return cluster;
	}

	public Cluster getCluster(Long clusterId){
		return _clusterDao.findById(clusterId);
	}
	
	@Override
    public Answer sendTo(Long dcId, HypervisorType type, Command cmd) {
        List<ClusterVO> clusters = _clusterDao.listByDcHyType(dcId, type.toString());
        int retry = 0;
        for( ClusterVO cluster : clusters ) {
            List<HostVO> hosts = _hostDao.listBy(Host.Type.Routing, cluster.getId(), null, dcId);
            for ( HostVO host : hosts ) {
                retry++;
                if ( retry > _retry )  {
                    return null;
                }
                Answer answer = null;
                try {
                    answer = easySend( host.getId(), cmd);
                } catch (Exception e ) {
                }
                if ( answer != null ) {
                    return answer;
                }
            }
        }      
        return null;
    }
	
	@Override
	@DB
	public boolean deleteHost(long hostId) {
	    
	    //Check if there are vms running/starting/stopping on this host
	    List<VMInstanceVO> vms = _vmDao.listByHostId(hostId);
	    
	    if (!vms.isEmpty()) {
	        throw new CloudRuntimeException("Unable to delete the host as there are vms in " + vms.get(0).getState() + " state using this host");
	    } 
	    
		Transaction txn = Transaction.currentTxn();
		try {
			HostVO host = _hostDao.findById(hostId);
			if (host == null) {
				if (s_logger.isDebugEnabled()) {
					s_logger.debug("Host: " + hostId
							+ " does not even exist.  Delete call is ignored.");
				}
				return true;
			}
			if (host.getType() == Type.SecondaryStorage) {
				return deleteSecondaryStorageHost(host);
			}
			if (s_logger.isDebugEnabled()) {
				s_logger.debug("Delete Host: " + hostId + " Guid:"
						+ host.getGuid());
			}

			if (host.getType() == Type.Routing) {
			    if (host.getHypervisorType() == HypervisorType.XenServer) {
			        if (host.getClusterId() != null) {
			            List<HostVO> hosts = _hostDao.listBy(Type.Routing,
			                    host.getClusterId(), host.getPodId(),
			                    host.getDataCenterId());
			            hosts.add(host);
			            boolean success = true;
			            for (HostVO thost : hosts) {
			                long thostId = thost.getId();
			                PoolEjectCommand eject = new PoolEjectCommand(
			                        host.getGuid());
			                Answer answer = easySend(thostId, eject);
			                if (answer != null && answer.getResult()) {
			                    s_logger.debug("Eject Host: " + hostId + " from "
			                            + thostId + " Succeed");
			                    success = true;
			                    break;
			                } else {
			                    success = false;
			                    s_logger.debug("Eject Host: "
			                            + hostId
			                            + " from "
			                            + thostId
			                            + " failed due to "
			                            + (answer != null ? answer.getDetails()
			                                    : "no answer"));
			                }
			            }
			            if (!success) {
			                String msg = "Unable to eject host "
			                    + host.getGuid()
			                    + " due to there is no host up in this cluster, please execute xe pool-eject host-uuid="
			                    + host.getGuid() + "in this host "
			                    + host.getPrivateIpAddress();
			                s_logger.info(msg);
			                _alertMgr.sendAlert(AlertManager.ALERT_TYPE_HOST,
			                        host.getDataCenterId(), host.getPodId(),
			                        "Unable to eject host " + host.getGuid(), msg);
			            }
			        }
			    } else if (host.getHypervisorType() == HypervisorType.KVM) {
			        try {
			            ShutdownCommand cmd = new ShutdownCommand(ShutdownCommand.DeleteHost, null);
			            send(host.getId(), cmd);
			        } catch (AgentUnavailableException e) {
			            s_logger.debug("Sending ShutdownCommand failed: " + e.toString());
			        } catch (OperationTimedoutException e) {
			            s_logger.debug("Sending ShutdownCommand failed: " + e.toString());
			        }
			    }
			}
			txn.start();

			_dcDao.releasePrivateIpAddress(host.getPrivateIpAddress(),
					host.getDataCenterId(), null);
			AgentAttache attache = findAttache(hostId);
			if (attache != null) {
				handleDisconnect(attache, Status.Event.Remove, false);
			}
			// delete host details
			_hostDetailsDao.deleteDetails(hostId);

			host.setGuid(null);
			Long clusterId = host.getClusterId();
			host.setClusterId(null);
			_hostDao.update(host.getId(), host);

			_hostDao.remove(hostId);
			if (clusterId != null) {
				List<HostVO> hosts = _hostDao.listByCluster(clusterId);
				if (hosts.size() == 0) {
					ClusterVO cluster = _clusterDao.findById(clusterId);
					cluster.setGuid(null);
					_clusterDao.update(clusterId, cluster);
				}

			}

			// delete the associated primary storage from db
			ComponentLocator locator = ComponentLocator
					.getLocator(ManagementServer.Name);
			_storagePoolHostDao = locator.getDao(StoragePoolHostDao.class);
			if (_storagePoolHostDao == null) {
				throw new ConfigurationException(
						"Unable to get storage pool host dao: "
								+ StoragePoolHostDao.class);
			}
			// 1. Get the pool_ids from the host ref table
			ArrayList<Long> pool_ids = _storagePoolHostDao.getPoolIds(hostId);

			// 2.Delete the associated entries in host ref table
			_storagePoolHostDao.deletePrimaryRecordsForHost(hostId);

			// 3.For pool ids you got, delete entries in pool table where
			// type='FileSystem' || 'LVM'
			for (Long poolId : pool_ids) {
				StoragePoolVO storagePool = _storagePoolDao.findById(poolId);
				if (storagePool.isLocal()) {
					storagePool.setUuid(null);
					storagePool.setClusterId(null);
					_storagePoolDao.update(poolId, storagePool);
					_storagePoolDao.remove(poolId);
				}
			}
			txn.commit();
			return true;
		} catch (Throwable t) {
			s_logger.error("Unable to delete host: " + hostId, t);
			return false;
		}
	}

	@Override
	public boolean deleteHost(DeleteHostCmd cmd)
			throws InvalidParameterValueException {
		Long id = cmd.getId();

		// Verify that host exists
		HostVO host = _hostDao.findById(id);
		if (host == null) {
			throw new InvalidParameterValueException("Host with id "
					+ id.toString() + " doesn't exist");
		}

		return deleteHost(id);
	}

	@DB
	protected boolean deleteSecondaryStorageHost(HostVO secStorageHost) {
		long zoneId = secStorageHost.getDataCenterId();
		long hostId = secStorageHost.getId();
		Transaction txn = Transaction.currentTxn();
		try {
			
			List<VMInstanceVO> allVmsInZone = _vmDao.listByZoneId(zoneId);
			if (!allVmsInZone.isEmpty()) {
				s_logger.warn("Cannot delete secondary storage host when there are  "
						+ allVmsInZone.size() + " vms in zone " + zoneId);
				return false;
			}
			txn.start();

			if (!_hostDao.updateStatus(secStorageHost,
					Event.MaintenanceRequested, _nodeId)) {
				if (s_logger.isDebugEnabled()) {
					s_logger.debug("Unable to take host " + hostId
							+ " into maintenance mode.  Delete call is ignored");
				}
				return false;
			}
			if (!_hostDao.updateStatus(secStorageHost,
					Event.PreparationComplete, _nodeId)) {
				if (s_logger.isDebugEnabled()) {
					s_logger.debug("Unable to take host " + hostId
							+ " into maintenance mode.  Delete call is ignored");
				}
				return false;
			}

			AgentAttache attache = findAttache(hostId);
			if (attache != null) {
				handleDisconnect(attache, Status.Event.Remove, false);
			}
			// now delete the host
			secStorageHost.setGuid(null);
			_hostDao.update(secStorageHost.getId(), secStorageHost);
			_hostDao.remove(secStorageHost.getId());

			// delete the templates associated with this host
			SearchCriteria<VMTemplateHostVO> templateHostSC = _vmTemplateHostDao
					.createSearchCriteria();
			templateHostSC.addAnd("hostId", SearchCriteria.Op.EQ,
					secStorageHost.getId());
			_vmTemplateHostDao.remove(templateHostSC);
						
			//delete the op_host_capacity entry
			SearchCriteria<CapacityVO> secStorageCapacitySC = _capacityDao.createSearchCriteria();			
			secStorageCapacitySC.addAnd("hostOrPoolId", SearchCriteria.Op.EQ,
					secStorageHost.getId());
			secStorageCapacitySC.addAnd("capacityType", SearchCriteria.Op.EQ,
					Capacity.CAPACITY_TYPE_SECONDARY_STORAGE);	
			_capacityDao.remove(secStorageCapacitySC);

	
			/* Disconnected agent needs special handling here */
			secStorageHost.setGuid(null);
			
			txn.commit();
			return true;
		} catch (Throwable t) {
			s_logger.error("Unable to delete sec storage host: "
					+ secStorageHost.getId(), t);
			return false;
		}
	}

	@Override
	public boolean isVirtualMachineUpgradable(final UserVm vm,
			final ServiceOffering offering) {
		Enumeration<HostAllocator> en = _hostAllocators.enumeration();
		boolean isMachineUpgradable = true;
		while (isMachineUpgradable && en.hasMoreElements()) {
			final HostAllocator allocator = en.nextElement();
			isMachineUpgradable = allocator.isVirtualMachineUpgradable(vm,
					offering);
		}

		return isMachineUpgradable;
	}

	protected int getPingInterval() {
		return _pingInterval;
	}

	@Override
	public Answer send(Long hostId, Command cmd, int timeout)
			throws AgentUnavailableException, OperationTimedoutException {
		Commands cmds = new Commands(OnError.Revert);
		cmds.addCommand(cmd);
		send(hostId, cmds, timeout);
		Answer[] answers = cmds.getAnswers();
		if (answers != null && !(answers[0] instanceof UnsupportedAnswer)) {
			return answers[0];
		}

		if (answers != null && (answers[0] instanceof UnsupportedAnswer)) {
			s_logger.warn("Unsupported Command: " + answers[0].getDetails());
			return answers[0];
		}

		return null;
	}
	
	@DB
	protected boolean noDbTxn() {
	    Transaction txn = Transaction.currentTxn();
	    return !txn.dbTxnStarted();
	}

	@Override
	public Answer[] send(Long hostId, Commands commands, int timeout)
			throws AgentUnavailableException, OperationTimedoutException {
		assert hostId != null : "Who's not checking the agent id before sending?  ... (finger wagging)";
		if (hostId == null) {
			throw new AgentUnavailableException(-1);
		}
		
		//assert noDbTxn() : "I know, I know.  Why are we so strict as to not allow txn across an agent call?  ...  Why are we so cruel ... Why are we such a dictator .... Too bad... Sorry...but NO AGENT COMMANDS WRAPPED WITHIN DB TRANSACTIONS!"; 

		Command[] cmds = commands.toCommands();

		assert cmds.length > 0 : "Ask yourself this about a hundred times.  Why am I  sending zero length commands?";

		if (cmds.length == 0) {
			commands.setAnswers(new Answer[0]);
		}

		final AgentAttache agent = getAttache(hostId);
		if (agent == null || agent.isClosed()) {
			throw new AgentUnavailableException("agent not logged into this management server", hostId);
		}

		long seq = _hostDao.getNextSequence(hostId);
		Request req = new Request(seq, hostId, _nodeId, cmds,
				commands.stopOnError(), true, commands.revertOnError());
		Answer[] answers = agent.send(req, timeout);
		commands.setAnswers(answers);
		return answers;
	}

	protected Status investigate(AgentAttache agent) {
		Long hostId = agent.getId();
		if (s_logger.isDebugEnabled()) {
			s_logger.debug("checking if agent (" + hostId + ") is alive");
		}

		try {
			long seq = _hostDao.getNextSequence(hostId);
			Request req = new Request(seq, hostId, _nodeId,
					new CheckHealthCommand(), true);
			Answer[] answers = agent.send(req, 50 * 1000);
			if (answers != null && answers[0] != null) {
				Status status = answers[0].getResult() ? Status.Up
						: Status.Down;
				if (s_logger.isDebugEnabled()) {
					s_logger.debug("agent ("
							+ hostId
							+ ") responded to checkHeathCommand, reporting that agent is "
							+ status);
				}
				return status;
			}
		} catch (AgentUnavailableException e) {
			s_logger.debug("Agent is unavailable so we move on.");
		} catch (OperationTimedoutException e) {
			s_logger.debug("Timed Out " + e.getMessage());
		}

		return _haMgr.investigate(hostId);
	}

	protected AgentAttache getAttache(final Long hostId)
			throws AgentUnavailableException {
		assert (hostId != null) : "Who didn't check their id value?";
		if (hostId == null) {
			return null;
		}
		AgentAttache agent = findAttache(hostId);
		if (agent == null) {
			s_logger.debug("Unable to find agent for " + hostId);
			throw new AgentUnavailableException("Unable to find agent ", hostId);
		}

		return agent;
	}

	@Override
	public long send(Long hostId, Commands commands, Listener listener)
			throws AgentUnavailableException {
		final AgentAttache agent = getAttache(hostId);
		if (agent.isClosed()) {
			return -1;
		}

		Command[] cmds = commands.toCommands();

		assert cmds.length > 0 : "Why are you sending zero length commands?";
		if (cmds.length == 0) {
			return -1;
		}
		long seq = _hostDao.getNextSequence(hostId);
		Request req = new Request(seq, hostId, _nodeId, cmds,
				commands.stopOnError(), true, commands.revertOnError());
		agent.send(req, listener);
		return seq;
	}

	@Override
	public long gatherStats(final Long hostId, final Command cmd,
			final Listener listener) {
		try {
			return send(hostId, new Commands(cmd), listener);
		} catch (final AgentUnavailableException e) {
			return -1;
		}
	}

	public void removeAgent(AgentAttache attache, Status nextState) {
		if (attache == null) {
			return;
		}
		long hostId = attache.getId();
		if (s_logger.isDebugEnabled()) {
			s_logger.debug("Remove Agent : " + hostId);
		}
		AgentAttache removed = null;
		boolean conflict = false;
		synchronized (_agents) {
			removed = _agents.remove(hostId);
			if (removed != null && removed != attache) {
				conflict = true;
				_agents.put(hostId, removed);
				removed = attache;
			}
		}
		if (conflict) {
			s_logger.debug("Agent for host " + hostId
					+ " is created when it is being disconnected");
		}
		if (removed != null) {
			removed.disconnect(nextState);
		}
	}

	@Override
	public void disconnect(final long hostId, final Status.Event event,
			final boolean investigate) {
		AgentAttache attache = findAttache(hostId);

		if (attache != null) {
			disconnect(attache, event, investigate);
		} else {
			HostVO host = _hostDao.findById(hostId);
			if (host != null && host.getRemoved() == null) {
				if (event != null && event.equals(Event.Remove)) {
					host.setGuid(null);
					host.setClusterId(null);
				}
				_hostDao.updateStatus(host, event, _nodeId);
			}
		}
	}

	public void disconnect(AgentAttache attache, final Status.Event event,
			final boolean investigate) {
		_executor.submit(new DisconnectTask(attache, event, investigate));
	}

	protected boolean handleDisconnect(AgentAttache attache,
			Status.Event event, boolean investigate) {
		if (attache == null) {
			return true;
		}

		long hostId = attache.getId();

		s_logger.info("Host " + hostId + " is disconnecting with event "
				+ event.toString());

		HostVO host = _hostDao.findById(hostId);
		if (host == null) {
			s_logger.warn("Can't find host with " + hostId);
			removeAgent(attache, Status.Removed);
			return true;

		}
		final Status currentState = host.getStatus();
		if (currentState == Status.Down || currentState == Status.Alert
				|| currentState == Status.Removed
				|| currentState == Status.PrepareForMaintenance) {
			if (s_logger.isDebugEnabled()) {
				s_logger.debug("Host " + hostId + " is already "
						+ currentState.toString());
			}
			if (currentState != Status.PrepareForMaintenance) {
				removeAgent(attache, currentState);
			}
			return true;
		}
		Status nextState = currentState.getNextStatus(event);
		if (nextState == null) {
			if (!(attache instanceof DirectAgentAttache)) {
				return false;
			}

			s_logger.debug("There is no transition from state "
					+ currentState.toString() + " and event "
					+ event.toString());
			assert false : "How did we get here.  Look at the FSM";
			return false;
		}

		if (s_logger.isDebugEnabled()) {
			s_logger.debug("The next state is " + nextState.toString()
					+ ", current state is " + currentState);
		}

		// Now we go and correctly diagnose what the actual situation is
		if (nextState == Status.Alert && investigate) {
			s_logger.info("Investigating why host " + hostId
					+ " has disconnected with event " + event.toString());

			final Status determinedState = investigate(attache);
			s_logger.info("The state determined is "
					+ (determinedState != null ? determinedState.toString()
							: "undeterminable"));

			if (determinedState == null || determinedState == Status.Down) {
				s_logger.error("Host is down: " + host.getId() + "-"
						+ host.getName() + ".  Starting HA on the VMs");

				event = Event.HostDown;
			} else if (determinedState == Status.Up) {
				// we effectively pinged from the server here.
				s_logger.info("Agent is determined to be up and running");
				_hostDao.updateStatus(host, Event.Ping, _nodeId);
				return false;
			} else if (determinedState == Status.Disconnected) {
				s_logger.warn("Agent is disconnected but the host is still up: "
						+ host.getId() + "-" + host.getName());
				if (currentState == Status.Disconnected) {
					if (((System.currentTimeMillis() >> 10) - host
							.getLastPinged()) > _alertWait) {
						s_logger.warn("Host "
								+ host.getId()
								+ " has been disconnected pass the time it should be disconnected.");
						event = Event.WaitedTooLong;
					} else {
						s_logger.debug("Host has been determined to be disconnected but it hasn't passed the wait time yet.");
						return false;
					}
				} else if (currentState == Status.Updating) {
					if (((System.currentTimeMillis() >> 10) - host
							.getLastPinged()) > _updateWait) {
						s_logger.warn("Host " + host.getId()
								+ " has been updating for too long");

						event = Event.WaitedTooLong;
					} else {
						s_logger.debug("Host has been determined to be disconnected but it hasn't passed the wait time yet.");
						return false;
					}
				} else if (currentState == Status.Up) {
					DataCenterVO dcVO = _dcDao.findById(host.getDataCenterId());
					HostPodVO podVO = _podDao.findById(host.getPodId());
					String hostDesc = "name: " + host.getName() + " (id:"
							+ host.getId() + "), availability zone: "
							+ dcVO.getName() + ", pod: " + podVO.getName();
					if((host.getType() != Host.Type.SecondaryStorage) && (host.getType() != Host.Type.ConsoleProxy)){
					    _alertMgr.sendAlert(AlertManager.ALERT_TYPE_HOST,
					            host.getDataCenterId(), host.getPodId(),
					            "Host disconnected, " + hostDesc,
					            "If the agent for host [" + hostDesc
					            + "] is not restarted within " + _alertWait
					            + " seconds, HA will begin on the VMs");
					}
					event = Event.AgentDisconnected;
				}
			} else {
				// if we end up here we are in alert state, send an alert
				DataCenterVO dcVO = _dcDao.findById(host.getDataCenterId());
				HostPodVO podVO = _podDao.findById(host.getPodId());
				String hostDesc = "name: " + host.getName() + " (id:"
						+ host.getId() + "), availability zone: "
						+ dcVO.getName() + ", pod: " + podVO.getName();
				_alertMgr.sendAlert(AlertManager.ALERT_TYPE_HOST,
						host.getDataCenterId(), host.getPodId(),
						"Host in ALERT state, " + hostDesc,
						"In availability zone " + host.getDataCenterId()
								+ ", host is in alert state: " + host.getId()
								+ "-" + host.getName());
			}
		}

		if (s_logger.isDebugEnabled()) {
			s_logger.debug("Deregistering link for " + hostId + " with state "
					+ nextState);
		}

		_hostDao.disconnect(host, event, _nodeId);

		removeAgent(attache, nextState);

		host = _hostDao.findById(host.getId());
		if (host.getStatus() == Status.Alert || host.getStatus() == Status.Down) {
			_haMgr.scheduleRestartForVmsOnHost(host);
		}

		for (Pair<Integer, Listener> monitor : _hostMonitors) {
			if (s_logger.isDebugEnabled()) {
				s_logger.debug("Sending Disconnect to listener: "
						+ monitor.second().getClass().getName());
			}
			monitor.second().processDisconnect(hostId, nextState);
		}

		return true;
	}

	protected AgentAttache notifyMonitorsOfConnection(AgentAttache attache,
			final StartupCommand[] cmd) throws ConnectionException {
		long hostId = attache.getId();
		HostVO host = _hostDao.findById(hostId);
		for (Pair<Integer, Listener> monitor : _hostMonitors) {
			if (s_logger.isDebugEnabled()) {
				s_logger.debug("Sending Connect to listener: "
						+ monitor.second().getClass().getSimpleName());
			}
			for (int i = 0; i < cmd.length; i++) {
				try {
					monitor.second().processConnect(host, cmd[i]);
				} catch (ConnectionException e) {
					if (e.isSetupError()) {
						s_logger.warn("Monitor "
								+ monitor.second().getClass().getSimpleName()
								+ " says there is an error in the connect process for "
								+ hostId + " due to " + e.getMessage());
						handleDisconnect(attache, Event.AgentDisconnected,
								false);
						throw e;
					} else {
						s_logger.info("Monitor "
								+ monitor.second().getClass().getSimpleName()
								+ " says not to continue the connect process for "
								+ hostId + " due to " + e.getMessage());
						handleDisconnect(attache, Event.ShutdownRequested,
								false);
						return attache;
					}
				}
			}
		}

		Long dcId = host.getDataCenterId();
		ReadyCommand ready = new ReadyCommand(dcId);
		Answer answer = easySend(hostId, ready);
		if (answer == null || !answer.getResult()) {
			// this is tricky part for secondary storage
			// make it as disconnected, wait for secondary storage VM to be up
			// return the attache instead of null, even it is disconnectede
			handleDisconnect(attache, Event.AgentDisconnected, false);
		}

		_hostDao.updateStatus(host, Event.Ready, _nodeId);
		attache.ready();
		return attache;
	}

	protected boolean notifyCreatorsOfConnection(StartupCommand[] cmd) throws ConnectionException {
	    for (Pair<Integer, HostCreator> monitor : _creationMonitors) {
	        if (s_logger.isDebugEnabled()) {
	            s_logger.debug("Sending Connect to creator: "
	                    + monitor.second().getClass().getSimpleName());
	        }
	        boolean handled =  monitor.second().processInitialConnect(cmd);
	        if (handled) {
	            break;
	        }
	    }

	    return false;
	}
	
	@Override
	public boolean start() {
		startDirectlyConnectedHosts();
		if (_monitor != null) {
			_monitor.start();
		}
		if (_connection != null) {
			_connection.start();
		}

		return true;
	}

	public void startDirectlyConnectedHosts() {
		List<HostVO> hosts = _hostDao.findDirectlyConnectedHosts();
		for (HostVO host : hosts) {
			loadDirectlyConnectedHost(host);
		}
	}

	@SuppressWarnings("rawtypes")
	protected void loadDirectlyConnectedHost(HostVO host) {
		String resourceName = host.getResource();
		ServerResource resource = null;
		try {
			Class<?> clazz = Class.forName(resourceName);
			Constructor constructor = clazz.getConstructor();
			resource = (ServerResource) constructor.newInstance();
		} catch (ClassNotFoundException e) {
			s_logger.warn("Unable to find class " + host.getResource(), e);
			return;
		} catch (InstantiationException e) {
			s_logger.warn("Unablet to instantiate class " + host.getResource(),
					e);
			return;
		} catch (IllegalAccessException e) {
			s_logger.warn("Illegal access " + host.getResource(), e);
			return;
		} catch (SecurityException e) {
			s_logger.warn("Security error on " + host.getResource(), e);
			return;
		} catch (NoSuchMethodException e) {
			s_logger.warn(
					"NoSuchMethodException error on " + host.getResource(), e);
			return;
		} catch (IllegalArgumentException e) {
			s_logger.warn(
					"IllegalArgumentException error on " + host.getResource(),
					e);
			return;
		} catch (InvocationTargetException e) {
			s_logger.warn(
					"InvocationTargetException error on " + host.getResource(),
					e);
			return;
		}

		_hostDao.loadDetails(host);

		HashMap<String, Object> params = new HashMap<String, Object>(host.getDetails().size() + 5);
		params.putAll(host.getDetails());

		params.put("guid", host.getGuid());
		params.put("zone", Long.toString(host.getDataCenterId()));
		if (host.getPodId() != null) {
			params.put("pod", Long.toString(host.getPodId()));
		}
		if (host.getClusterId() != null) {
			params.put("cluster", Long.toString(host.getClusterId()));
			String guid = null;
			ClusterVO cluster = _clusterDao.findById(host.getClusterId());
			if (cluster.getGuid() == null) {
				guid = host.getDetail("pool");
			} else {
				guid = cluster.getGuid();
			}
			if (guid == null || guid.isEmpty()) {
				throw new CloudRuntimeException(
						"Can not find guid for cluster " + cluster.getId() + " name " + cluster.getName());
			}
			params.put("pool", guid);
		}

		params.put("ipaddress", host.getPrivateIpAddress());
		params.put("secondary.storage.vm", "false");
		params.put("max.template.iso.size",
				_configDao.getValue("max.template.iso.size"));

		try {
			resource.configure(host.getName(), params);
		} catch (ConfigurationException e) {
			s_logger.warn("Unable to configure resource due to ", e);
			return;
		}

		if (!resource.start()) {
			s_logger.warn("Unable to start the resource");
			return;
		}
		host.setLastPinged(System.currentTimeMillis() >> 10);
		host.setManagementServerId(_nodeId);
		_hostDao.update(host.getId(), host);
		_executor.execute(new SimulateStartTask(host.getId(), resource, host
				.getDetails(), null));
	}

	protected AgentAttache simulateStart(ServerResource resource,
			Map<String, String> details, boolean old, List<String> hostTags, String allocationState)
			throws IllegalArgumentException {
		StartupCommand[] cmds = resource.initialize();
		if (cmds == null) {
			return null;
		}

		AgentAttache attache = null;
		if (s_logger.isDebugEnabled()) {
		    new Request(0l, -1l, -1l, cmds, true, false, true).log(-1, "Startup request from directly connected host: ");
//			s_logger.debug("Startup request from directly connected host: "
//					+ new Request(0l, -1l, -1l, cmds, true, false, true)
//							.toString());
		}
		try {
			attache = handleDirectConnect(resource, cmds, details, old, hostTags, allocationState);
		} catch (IllegalArgumentException ex) {
			s_logger.warn("Unable to connect due to ", ex);
			throw ex;
		} catch (Exception e) {
			s_logger.warn("Unable to connect due to ", e);
		}

		if (attache == null) {
			resource.disconnected();
			return null;
		}
		if (attache.isReady()) {
			StartupAnswer[] answers = new StartupAnswer[cmds.length];
			for (int i = 0; i < answers.length; i++) {
				answers[i] = new StartupAnswer(cmds[i], attache.getId(),
						_pingInterval);
			}

			attache.process(answers);
		}
		return attache;
	}

	@Override
	public boolean stop() {
		if (_monitor != null) {
			_monitor.signalStop();
		}
		if (_connection != null) {
			_connection.stop();
		}

		s_logger.info("Disconnecting agents: " + _agents.size());
		synchronized (_agents) {
			for (final AgentAttache agent : _agents.values()) {
				final HostVO host = _hostDao.findById(agent.getId());
				if (host == null) {
					if (s_logger.isDebugEnabled()) {
						s_logger.debug("Cant not find host " + agent.getId());
					}
				} else {
					_hostDao.updateStatus(host, Event.ManagementServerDown,
							_nodeId);
				}
			}
		}
		return true;
	}

	@Override
	public Pair<HostPodVO, Long> findPod(final VirtualMachineTemplate template,
			ServiceOfferingVO offering, final DataCenterVO dc,
			final long accountId, Set<Long> avoids) {
		final Enumeration en = _podAllocators.enumeration();
		while (en.hasMoreElements()) {
			final PodAllocator allocator = (PodAllocator) en.nextElement();
			final Pair<HostPodVO, Long> pod = allocator.allocateTo(template,
					offering, dc, accountId, avoids);
			if (pod != null) {
				return pod;
			}
		}
		return null;
	}

	@Override
	public HostStats getHostStatistics(long hostId) {
		Answer answer = easySend(hostId, new GetHostStatsCommand(_hostDao
				.findById(hostId).getGuid(), _hostDao.findById(hostId)
				.getName(), hostId));

		if (answer != null && (answer instanceof UnsupportedAnswer)) {
			return null;
		}

		if (answer == null || !answer.getResult()) {
			String msg = "Unable to obtain host " + hostId + " statistics. ";
			s_logger.warn(msg);
			return null;
		} else {

			// now construct the result object
			if (answer instanceof GetHostStatsAnswer) {
				return ((GetHostStatsAnswer) answer).getHostStats();
			}
		}
		return null;
	}

	@Override
	public Long getGuestOSCategoryId(long hostId) {
		HostVO host = _hostDao.findById(hostId);
		if (host == null) {
			return null;
		} else {
			_hostDao.loadDetails(host);
			DetailVO detail = _hostDetailsDao.findDetail(hostId,
					"guest.os.category.id");
			if (detail == null) {
				return null;
			} else {
				return Long.parseLong(detail.getValue());
			}
		}
	}

	@Override
    public String getHostTags(long hostId){
		List<String> hostTags = _hostTagsDao.gethostTags(hostId);
		if (hostTags == null) {
			return null;
		} else {
			return StringUtils.listToCsvTags(hostTags);
		}
    }
	
	@Override
	public String getName() {
		return _name;
	}

	protected class DisconnectTask implements Runnable {
		AgentAttache _attache;
		Status.Event _event;
		boolean _investigate;

		DisconnectTask(final AgentAttache attache, final Status.Event event,
				final boolean investigate) {
			_attache = attache;
			_event = event;
			_investigate = investigate;
		}

		@Override
		public void run() {
			try {
				handleDisconnect(_attache, _event, _investigate);
			} catch (final Exception e) {
				s_logger.error("Exception caught while handling disconnect: ",
						e);
			} finally {
				StackMaid.current().exitCleanup();
			}
		}
	}

	@Override
	public Answer easySend(final Long hostId, final Command cmd) {
		return easySend(hostId, cmd, _wait);
	}

	@Override
	public Answer easySend(final Long hostId, final Command cmd, int timeout) {
		try {
			Host h = _hostDao.findById(hostId);
			if (h == null || h.getRemoved() != null) {
				s_logger.debug("Host with id " + hostId.toString()
						+ " doesn't exist");
				return null;
			}
			Status status = h.getStatus();
			if (!status.equals(Status.Up) && !status.equals(Status.Connecting)) {
				return null;
			}
			final Answer answer = send(hostId, cmd, timeout);
			if (answer == null) {
				s_logger.warn("send returns null answer");
				return null;
			}

			if (!answer.getResult()) {
				s_logger.warn("Unable to execute command: " + cmd.toString()
						+ " due to " + answer.getDetails());
				return null;
			}

			if (s_logger.isDebugEnabled() && answer.getDetails() != null) {
				s_logger.debug("Details from executing "
						+ cmd.getClass().toString() + ": "
						+ answer.getDetails());
			}

			return answer;

		} catch (final AgentUnavailableException e) {
			s_logger.warn(e.getMessage());
			return null;
		} catch (final OperationTimedoutException e) {
			s_logger.warn("Operation timed out: " + e.getMessage());
			return null;
		} catch (final Exception e) {
			s_logger.warn("Exception while sending", e);
			return null;
		}
	}

	@Override
	public Answer send(final Long hostId, final Command cmd)
			throws AgentUnavailableException, OperationTimedoutException {
		return send(hostId, cmd, _wait);
	}

	@Override
	public Answer[] send(final Long hostId, Commands cmds)
			throws AgentUnavailableException, OperationTimedoutException {
		return send(hostId, cmds, _wait);
	}

	@Override
	public Host reconnectHost(ReconnectHostCmd cmd)
			throws AgentUnavailableException {
		Long hostId = cmd.getId();

		HostVO host = _hostDao.findById(hostId);
		if (host == null) {
			throw new InvalidParameterValueException("Host with id "
					+ hostId.toString() + " doesn't exist");
		}

		boolean result = reconnect(hostId);
		if (result) {
			return host;
		}
		throw new CloudRuntimeException("Failed to reconnect host with id " + hostId.toString()
						+ ", internal error.");
	}

	@Override
	public boolean reconnect(final long hostId)
			throws AgentUnavailableException {
		HostVO host;

		host = _hostDao.findById(hostId);
		if (host == null || host.getRemoved() != null) {
			s_logger.warn("Unable to find host " + hostId);
			return false;
		}

		if (host.getStatus() != Status.Up && host.getStatus() != Status.Alert) {
			s_logger.info("Unable to disconnect host because it is not in the correct state: host="
					+ hostId + "; Status=" + host.getStatus());
			return false;
		}

		AgentAttache attache = findAttache(hostId);
		if (attache == null) {
			s_logger.info("Unable to disconnect host because it is not connected to this server: "
					+ hostId);
			return false;
		}

		disconnect(attache, Event.ShutdownRequested, false);
		return true;
	}

	@Override
	public boolean cancelMaintenance(final long hostId) {

		HostVO host;
		host = _hostDao.findById(hostId);
		if (host == null || host.getRemoved() != null) {
			s_logger.warn("Unable to find host " + hostId);
			return true;
		}

		if (host.getStatus() != Status.PrepareForMaintenance
				&& host.getStatus() != Status.Maintenance
				&& host.getStatus() != Status.ErrorInMaintenance) {
			return true;
		}

		_haMgr.cancelScheduledMigrations(host);
		List<VMInstanceVO> vms = _haMgr.findTakenMigrationWork();
		for (VMInstanceVO vm : vms) {
			if (vm.getHostId() != null && vm.getHostId() == hostId) {
				s_logger.info("Unable to cancel migration because the vm is being migrated: "
						+ vm.toString());
				return false;
			}
		}
		disconnect(hostId, Event.ResetRequested, false);
		return true;
	}

	@Override
	public Host cancelMaintenance(CancelMaintenanceCmd cmd)
			throws InvalidParameterValueException {
		Long hostId = cmd.getId();

		// verify input parameters
		HostVO host = _hostDao.findById(hostId);
		if (host == null || host.getRemoved() != null) {
			throw new InvalidParameterValueException("Host with id "
					+ hostId.toString() + " doesn't exist");
		}

		boolean success = cancelMaintenance(hostId);
		if (!success) {
			throw new CloudRuntimeException(
					"Internal error cancelling maintenance.");
		}
		return host;
	}

	@Override
	public boolean executeUserRequest(long hostId, Event event)
			throws AgentUnavailableException {
		if (event == Event.MaintenanceRequested) {
			return maintain(hostId);
		} else if (event == Event.ResetRequested) {
			return cancelMaintenance(hostId);
		} else if (event == Event.Remove) {
			return deleteHost(hostId);
		} else if (event == Event.AgentDisconnected) {
			if (s_logger.isDebugEnabled()) {
				s_logger.debug("Received agent disconnect event for host "
						+ hostId);
			}
			AgentAttache attache = null;
			attache = findAttache(hostId);
			if (attache != null) {
				handleDisconnect(attache, Event.AgentDisconnected, false);
			}
			return true;
		} else if (event == Event.ShutdownRequested) {
			return reconnect(hostId);
		}
		return false;
	}

	@Override
	public boolean maintain(final long hostId) throws AgentUnavailableException {
		HostVO host = _hostDao.findById(hostId);
		Status state;

		Answer answer = easySend(hostId, new MaintainCommand());
		if (answer == null || !answer.getResult()) {
			s_logger.warn("Unable to put host in maintainance mode: " + hostId);
			return false;
		}

		// Let's put this guy in maintenance state
		do {
			host = _hostDao.findById(hostId);
			if (host == null) {
				s_logger.debug("Unable to find host " + hostId);
				return false;
			}
			state = host.getStatus();
			if (state == Status.Disconnected || state == Status.Updating) {
				s_logger.debug("Unable to put host " + hostId
						+ " in matinenance mode because it is currently in "
						+ state.toString());
				throw new AgentUnavailableException(
						"Agent is in "
								+ state.toString()
								+ " state.  Please wait for it to become Alert state try again.",
						hostId);
			}
		} while (!_hostDao.updateStatus(host, Event.MaintenanceRequested,
				_nodeId));

		AgentAttache attache = findAttache(hostId);
		if (attache != null) {
			attache.setMaintenanceMode(true);
		}

		if (attache != null) {
			// Now cancel all of the commands except for the active one.
			attache.cancelAllCommands(Status.PrepareForMaintenance, false);
		}

		final Host.Type type = host.getType();

		if (type == Host.Type.Routing) {
		    
			final List<VMInstanceVO> vms = _vmDao.listByHostId(hostId);
			if (vms.size() == 0) {
				return true;
			}
			
			List<HostVO> hosts = _hostDao.listBy(host.getClusterId(), host.getPodId(), host.getDataCenterId());	

			for (final VMInstanceVO vm : vms) {
		        if( hosts == null || hosts.size() <= 1) {
		            // for the last host in this cluster, stop all the VMs
	                _haMgr.scheduleStop(vm, hostId, WorkType.ForceStop);
		        } else {
		            _haMgr.scheduleMigration(vm);
		        }
			}
		}

		return true;
	}

	@Override
	public Host maintain(PrepareForMaintenanceCmd cmd)
			throws InvalidParameterValueException {
		Long hostId = cmd.getId();
		HostVO host = _hostDao.findById(hostId);

		if (host == null) {
			s_logger.debug("Unable to find host " + hostId);
			throw new InvalidParameterValueException("Unable to find host with ID: " + hostId + ". Please specify a valid host ID.");
		}
		
		if (_hostDao.countBy(host.getClusterId(), Status.PrepareForMaintenance, Status.ErrorInMaintenance) > 0) {
		    throw new InvalidParameterValueException("There are other servers in PrepareForMaintenance OR ErrorInMaintenance STATUS in cluster " + host.getClusterId());
		}

		if (_storageMgr.isLocalStorageActiveOnHost(host)) {
			throw new InvalidParameterValueException(
					"There are active VMs using the host's local storage pool. Please stop all VMs on this host that use local storage.");
		}

		try {
			if (maintain(hostId)) {
				return _hostDao.findById(hostId);
			} else {
				throw new CloudRuntimeException(
						"Unable to prepare for maintenance host " + hostId);
			}
		} catch (AgentUnavailableException e) {
			throw new CloudRuntimeException(
					"Unable to prepare for maintenance host " + hostId);
		}
	}

	public boolean checkCIDR(Host.Type type, HostPodVO pod,
			String serverPrivateIP, String serverPrivateNetmask) {
		if (serverPrivateIP == null) {
			return true;
		}
		// Get the CIDR address and CIDR size
		String cidrAddress = pod.getCidrAddress();
		long cidrSize = pod.getCidrSize();

		// If the server's private IP address is not in the same subnet as the
		// pod's CIDR, return false
		String cidrSubnet = NetUtils.getCidrSubNet(cidrAddress, cidrSize);
		String serverSubnet = NetUtils.getSubNet(serverPrivateIP,
				serverPrivateNetmask);
		if (!cidrSubnet.equals(serverSubnet)) {
			return false;
		}

		// If the server's private netmask is less inclusive than the pod's CIDR
		// netmask, return false
		String cidrNetmask = NetUtils
				.getCidrSubNet("255.255.255.255", cidrSize);
		long cidrNetmaskNumeric = NetUtils.ip2Long(cidrNetmask);
		long serverNetmaskNumeric = NetUtils.ip2Long(serverPrivateNetmask);
		if (serverNetmaskNumeric > cidrNetmaskNumeric) {
			return false;
		}
		return true;
	}

	protected void checkCIDR(Host.Type type, HostPodVO pod, DataCenterVO dc,
			String serverPrivateIP, String serverPrivateNetmask)
			throws IllegalArgumentException {
		// Skip this check for Storage Agents and Console Proxies
		if (type == Host.Type.Storage || type == Host.Type.ConsoleProxy) {
			return;
		}

		if (serverPrivateIP == null) {
			return;
		}
		// Get the CIDR address and CIDR size
		String cidrAddress = pod.getCidrAddress();
		long cidrSize = pod.getCidrSize();

		// If the server's private IP address is not in the same subnet as the
		// pod's CIDR, return false
		String cidrSubnet = NetUtils.getCidrSubNet(cidrAddress, cidrSize);
		String serverSubnet = NetUtils.getSubNet(serverPrivateIP,
				serverPrivateNetmask);
		if (!cidrSubnet.equals(serverSubnet)) {
			s_logger.warn("The private ip address of the server ("
					+ serverPrivateIP
					+ ") is not compatible with the CIDR of pod: "
					+ pod.getName() + " and zone: " + dc.getName());
			throw new IllegalArgumentException(
					"The private ip address of the server (" + serverPrivateIP
							+ ") is not compatible with the CIDR of pod: "
							+ pod.getName() + " and zone: " + dc.getName());
		}

		// If the server's private netmask is less inclusive than the pod's CIDR
		// netmask, return false
		String cidrNetmask = NetUtils
				.getCidrSubNet("255.255.255.255", cidrSize);
		long cidrNetmaskNumeric = NetUtils.ip2Long(cidrNetmask);
		long serverNetmaskNumeric = NetUtils.ip2Long(serverPrivateNetmask);
		if (serverNetmaskNumeric > cidrNetmaskNumeric) {
			throw new IllegalArgumentException(
					"The private ip address of the server (" + serverPrivateIP
							+ ") is not compatible with the CIDR of pod: "
							+ pod.getName() + " and zone: " + dc.getName());
		}

	}

	public void checkIPConflicts(Host.Type type, HostPodVO pod,
			DataCenterVO dc, String serverPrivateIP,
			String serverPrivateNetmask, String serverPublicIP,
			String serverPublicNetmask) {
		// If the server's private IP is the same as is public IP, this host has
		// a host-only private network. Don't check for conflicts with the
		// private IP address table.
		if (serverPrivateIP != serverPublicIP) {
			if (!_privateIPAddressDao.mark(dc.getId(), pod.getId(),
					serverPrivateIP)) {
				// If the server's private IP address is already in the
				// database, return false
				List<DataCenterIpAddressVO> existingPrivateIPs = _privateIPAddressDao
						.listByPodIdDcIdIpAddress(pod.getId(), dc.getId(),
								serverPrivateIP);

				assert existingPrivateIPs.size() <= 1 : " How can we get more than one ip address with "
						+ serverPrivateIP;
				if (existingPrivateIPs.size() > 1) {
					throw new IllegalArgumentException(
							"The private ip address of the server ("
									+ serverPrivateIP
									+ ") is already in use in pod: "
									+ pod.getName() + " and zone: "
									+ dc.getName());
				}
				if (existingPrivateIPs.size() == 1) {
					DataCenterIpAddressVO vo = existingPrivateIPs.get(0);
					if (vo.getInstanceId() != null) {
						throw new IllegalArgumentException(
								"The private ip address of the server ("
										+ serverPrivateIP
										+ ") is already in use in pod: "
										+ pod.getName() + " and zone: "
										+ dc.getName());
					}
				}
			}
		}

		if (serverPublicIP != null
				&& !_publicIPAddressDao
						.mark(dc.getId(), new Ip(serverPublicIP))) {
			// If the server's public IP address is already in the database,
			// return false
			List<IPAddressVO> existingPublicIPs = _publicIPAddressDao
					.listByDcIdIpAddress(dc.getId(), serverPublicIP);
			if (existingPublicIPs.size() > 0) {
				throw new IllegalArgumentException(
						"The public ip address of the server ("
								+ serverPublicIP
								+ ") is already in use in zone: "
								+ dc.getName());
			}
		}
	}

	@Override
	public Host addHost(long zoneId, ServerResource resource, Type hostType,
			Map<String, String> hostDetails) {
		// Check if the zone exists in the system
		if (_dcDao.findById(zoneId) == null) {
			throw new InvalidParameterValueException("Can't find zone with id "
					+ zoneId);
		}

		Map<String, String> details = hostDetails;
		String guid = details.get("guid");
		List<HostVO> currentHosts = _hostDao.listBy(hostType, zoneId);
		for (HostVO currentHost : currentHosts) {
			if (currentHost.getGuid().equals(guid)) {
				return currentHost;
			}
		}

		AgentAttache attache = simulateStart(resource, hostDetails, true, null, null);
		return _hostDao.findById(attache.getId());
	}

	public HostVO createHost(final StartupCommand startup,
			ServerResource resource, Map<String, String> details,
			boolean directFirst, List<String> hostTags, String allocationState) throws IllegalArgumentException {
		Host.Type type = null;

		if (startup instanceof StartupStorageCommand) {

			StartupStorageCommand ssCmd = ((StartupStorageCommand) startup);
			if (ssCmd.getResourceType() == Storage.StorageResourceType.SECONDARY_STORAGE) {
				type = Host.Type.SecondaryStorage;
				if (resource != null
						&& resource instanceof DummySecondaryStorageResource) {
					resource = null;
				}
			} else {
				type = Host.Type.Storage;
			}
			final Map<String, String> hostDetails = ssCmd.getHostDetails();
			if (hostDetails != null) {
				if (details != null) {
					details.putAll(hostDetails);
				} else {
					details = hostDetails;
				}
			}
		} else if (startup instanceof StartupRoutingCommand) {
			StartupRoutingCommand ssCmd = ((StartupRoutingCommand) startup);
			type = Host.Type.Routing;
			final Map<String, String> hostDetails = ssCmd.getHostDetails();
			if (hostDetails != null) {
				if (details != null) {
					details.putAll(hostDetails);
				} else {
					details = hostDetails;
				}
			}
		} else if (startup instanceof StartupProxyCommand) {
			type = Host.Type.ConsoleProxy;
		} else if (startup instanceof StartupRoutingCommand) {
			type = Host.Type.Routing;
		} else if (startup instanceof StartupExternalFirewallCommand) {
			type = Host.Type.ExternalFirewall;
		} else if (startup instanceof StartupExternalLoadBalancerCommand) {
			type = Host.Type.ExternalLoadBalancer;
		} else if (startup instanceof StartupPxeServerCommand) {
			type = Host.Type.PxeServer;
		} else if (startup instanceof StartupExternalDhcpCommand) {
			type = Host.Type.ExternalDhcp;
		}else {
			assert false : "Did someone add a new Startup command?";
		}

		Long id = null;
		HostVO server = _hostDao.findByGuid(startup.getGuid());
		if (server == null) {
			server = _hostDao.findByGuid(startup.getGuidWithoutResource());
		}
		if (server != null && server.getRemoved() == null) {
			id = server.getId();
			if (s_logger.isDebugEnabled()) {
				s_logger.debug("Found the host " + id + " by guid: "
						+ startup.getGuid());
			}
			if (directFirst) {
				s_logger.debug("Old host reconnected as new");
				return null;
			}
		} else {
			server = new HostVO(startup.getGuid());
		}

		server.setDetails(details);
		server.setHostTags(hostTags);
		
		if(allocationState != null){
			try{
				HostAllocationState hostAllocationState = Host.HostAllocationState.valueOf(allocationState);
				if(hostAllocationState != null){
					server.setHostAllocationState(hostAllocationState);
				}
			}catch(IllegalArgumentException ex){
				s_logger.error("Unable to resolve " + allocationState + " to a valid supported host allocation State, defaulting to 'Enabled'");
				server.setHostAllocationState(Host.HostAllocationState.Enabled);
			}
		}else{
			server.setHostAllocationState(Host.HostAllocationState.Enabled);
		}
		
		updateHost(server, startup, type, _nodeId);
		if (resource != null) {
			server.setResource(resource.getClass().getName());
		}
		if (id == null) {
			/*
			 * // ignore integrity check for agent-simulator
			 * if(!"0.0.0.0".equals(startup.getPrivateIpAddress()) &&
			 * !"0.0.0.0".equals(startup.getStorageIpAddress())) { if
			 * (_hostDao.findByPrivateIpAddressInDataCenter
			 * (server.getDataCenterId(), startup.getPrivateIpAddress()) !=
			 * null) { throw newIllegalArgumentException(
			 * "The private ip address is already in used: " +
			 * startup.getPrivateIpAddress()); }
			 * 
			 * if
			 * (_hostDao.findByPrivateIpAddressInDataCenter(server.getDataCenterId
			 * (), startup.getStorageIpAddress()) != null) { throw new
			 * IllegalArgumentException
			 * ("The private ip address is already in used: " +
			 * startup.getStorageIpAddress()); } }
			 */

			if (startup instanceof StartupProxyCommand) {
				server.setProxyPort(((StartupProxyCommand) startup)
						.getProxyPort());
			}

			server = _hostDao.persist(server);
			id = server.getId();

			s_logger.info("New " + server.getType().toString()
					+ " host connected w/ guid " + startup.getGuid()
					+ " and id is " + id);
		} else {
			if (!_hostDao.connect(server, _nodeId)) {
				throw new CloudRuntimeException(
						"Agent cannot connect because the current state is "
								+ server.getStatus().toString());
			}
			s_logger.info("Old " + server.getType().toString()
					+ " host reconnected w/ id =" + id);
		}
		createCapacityEntry(startup, server);

		return server;
	}

	public HostVO createHost(final StartupCommand[] startup,
			ServerResource resource, Map<String, String> details,
			boolean directFirst, List<String> hostTags, String allocationState) throws IllegalArgumentException {
		StartupCommand firstCmd = startup[0];
		HostVO result = createHost(firstCmd, resource, details, directFirst, hostTags, allocationState);
		if (result == null) {
			return null;
		}
		return result;
	}

	public AgentAttache handleConnect(final Link link,
			final StartupCommand[] startup) throws IllegalArgumentException,
			ConnectionException {
	    boolean created = false;
	    HostVO server = _hostDao.findByGuid(startup[0].getGuid());
	    if (server == null) {
	        created = notifyCreatorsOfConnection(startup);
	        if (!created) {
	            server = createHost(startup, null, null, false, null, null);
	        }
	    }
	     
		if (server == null) {
			return null;
		}
		long id = server.getId();

		AgentAttache attache = createAttache(id, server, link);

		attache = notifyMonitorsOfConnection(attache, startup);

		return attache;
	}

	public AgentAttache findAgent(long hostId) {
		synchronized (_agents) {
			return _agents.get(hostId);
		}
	}

	protected AgentAttache createAttache(long id, HostVO server, Link link) {
		s_logger.debug("create ConnectedAgentAttache for " + id);
		final AgentAttache attache = new ConnectedAgentAttache(id, link,
				server.getStatus() == Status.Maintenance
						|| server.getStatus() == Status.ErrorInMaintenance
						|| server.getStatus() == Status.PrepareForMaintenance);
		link.attach(attache);
		AgentAttache old = null;
		synchronized (_agents) {
			old = _agents.get(id);
			_agents.put(id, attache);
		}
		if (old != null) {
			old.disconnect(Status.Removed);
		}
		return attache;
	}

	protected AgentAttache createAttache(long id, HostVO server,
			ServerResource resource) {
		if (resource instanceof DummySecondaryStorageResource
				|| resource instanceof KvmDummyResourceBase) {
			return new DummyAttache(id, false);
		}
		s_logger.debug("create DirectAgentAttache for " + id);
		final DirectAgentAttache attache = new DirectAgentAttache(id, resource,
				server.getStatus() == Status.Maintenance
						|| server.getStatus() == Status.ErrorInMaintenance
						|| server.getStatus() == Status.PrepareForMaintenance,
				this);
		AgentAttache old = null;
		synchronized (_agents) {
			old = _agents.get(id);
			_agents.put(id, attache);
		}
		if (old != null) {
			old.disconnect(Status.Removed);
		}
		return attache;
	}

	@Override
	public boolean maintenanceFailed(long hostId) {
		HostVO host = _hostDao.findById(hostId);
		if (host == null) {
			if (s_logger.isDebugEnabled()) {
				s_logger.debug("Cant not find host " + hostId);
			}
			return false;
		} else {
			return _hostDao.updateStatus(host, Event.UnableToMigrate, _nodeId);
		}
	}

	@Override
	public Host updateHost(UpdateHostCmd cmd)
			throws InvalidParameterValueException {
		Long hostId = cmd.getId();
		Long guestOSCategoryId = cmd.getOsCategoryId();

		if (guestOSCategoryId != null) {

			// Verify that the host exists
			HostVO host = _hostDao.findById(hostId);
			if (host == null) {
				throw new InvalidParameterValueException("Host with id "
						+ hostId + " doesn't exist");
			}

			// Verify that the guest OS Category exists
			if (guestOSCategoryId > 0) {
				if (_guestOSCategoryDao.findById(guestOSCategoryId) == null) {
					throw new InvalidParameterValueException(
							"Please specify a valid guest OS category.");
				}
			}

			GuestOSCategoryVO guestOSCategory = _guestOSCategoryDao
					.findById(guestOSCategoryId);
			Map<String, String> hostDetails = _hostDetailsDao
					.findDetails(hostId);

			if (guestOSCategory != null) {
				// Save a new entry for guest.os.category.id
				hostDetails.put("guest.os.category.id",
						String.valueOf(guestOSCategory.getId()));
			} else {
				// Delete any existing entry for guest.os.category.id
				hostDetails.remove("guest.os.category.id");
			}
			_hostDetailsDao.persist(hostId, hostDetails);
		}
		
		String allocationState = cmd.getAllocationState();
		if(allocationState != null){
			// Verify that the host exists
			HostVO host = _hostDao.findById(hostId);
			if (host == null) {
				throw new InvalidParameterValueException("Host with id "
						+ hostId + " doesn't exist");
			}

			try{
				HostAllocationState newAllocationState = Host.HostAllocationState.valueOf(allocationState);
				if (newAllocationState == null) {
					s_logger.error("Unable to resolve " + allocationState + " to a valid supported allocation State");
					throw new InvalidParameterValueException("Unable to resolve " + allocationState + " to a supported state");
				}else{
					host.setHostAllocationState(newAllocationState);
				}
			}catch(IllegalArgumentException ex){
				s_logger.error("Unable to resolve " + allocationState + " to a valid supported allocation State");
				throw new InvalidParameterValueException("Unable to resolve " + allocationState + " to a supported state");
			}
		
			_hostDao.update(hostId, host);
		}

		HostVO updatedHost = _hostDao.findById(hostId);
		return updatedHost;
	}

	protected void updateHost(final HostVO host, final StartupCommand startup,
			final Host.Type type, final long msId)
			throws IllegalArgumentException {
		s_logger.debug("updateHost() called");

		String dataCenter = startup.getDataCenter();
		String pod = startup.getPod();
		String cluster = startup.getCluster();

		if (pod != null && dataCenter != null
				&& pod.equalsIgnoreCase("default")
				&& dataCenter.equalsIgnoreCase("default")) {
			List<HostPodVO> pods = _podDao.listAllIncludingRemoved();
			for (HostPodVO hpv : pods) {
				if (checkCIDR(type, hpv, startup.getPrivateIpAddress(),
						startup.getPrivateNetmask())) {
					pod = hpv.getName();
					dataCenter = _dcDao.findById(hpv.getDataCenterId())
							.getName();
					break;
				}
			}
		}
		long dcId = -1;
		DataCenterVO dc = _dcDao.findByName(dataCenter);
		if (dc == null) {
			try {
				dcId = Long.parseLong(dataCenter);
				dc = _dcDao.findById(dcId);
			} catch (final NumberFormatException e) {
			}
		}
		if (dc == null) {
			throw new IllegalArgumentException("Host "
					+ startup.getPrivateIpAddress()
					+ " sent incorrect data center: " + dataCenter);
		}
		dcId = dc.getId();

		HostPodVO p = _podDao.findByName(pod, dcId);
		if (p == null) {
			try {
				final long podId = Long.parseLong(pod);
				p = _podDao.findById(podId);
			} catch (final NumberFormatException e) {
			}
		}
		Long podId = null;
		if (p == null) {
			if (type != Host.Type.SecondaryStorage
					&& type != Host.Type.ExternalFirewall
					&& type != Host.Type.ExternalLoadBalancer) {

				/*
				 * s_logger.info("Unable to find the pod so we are creating one."
				 * ); p = createPod(pod, dcId, startup.getPrivateIpAddress(),
				 * NetUtils.getCidrSize(startup.getPrivateNetmask())); podId =
				 * p.getId();
				 */
				s_logger.error("Host " + startup.getPrivateIpAddress()
						+ " sent incorrect pod: " + pod + " in " + dataCenter);
				throw new IllegalArgumentException("Host "
						+ startup.getPrivateIpAddress()
						+ " sent incorrect pod: " + pod + " in " + dataCenter);
			}
		} else {
			podId = p.getId();
		}

		Long clusterId = null;
		if (cluster != null) {
			try {
				clusterId = Long.valueOf(cluster);
			} catch (NumberFormatException e) {
				ClusterVO c = _clusterDao.findBy(cluster, podId);
				if (c == null) {
					c = new ClusterVO(dcId, podId, cluster);
					c = _clusterDao.persist(c);
				}
				clusterId = c.getId();
			}
		}

		if (type == Host.Type.Routing) {
			StartupRoutingCommand scc = (StartupRoutingCommand) startup;

			HypervisorType hypervisorType = scc.getHypervisorType();
			boolean doCidrCheck = true;

			ClusterVO clusterVO = _clusterDao.findById(clusterId);
			if (clusterVO.getHypervisorType() != scc.getHypervisorType()) {
				throw new IllegalArgumentException(
						"Can't add host whose hypervisor type is: "
								+ scc.getHypervisorType() + " into cluster: "
								+ clusterId + " whose hypervisor type is: "
								+ clusterVO.getHypervisorType());
			}

			/*
			 * KVM:Enforcement that all the hosts in the cluster have the same
			 * os type, for migration
			 */
			if (scc.getHypervisorType() == HypervisorType.KVM) {
				List<HostVO> hostsInCluster = _hostDao.listByCluster(clusterId);
				if (!hostsInCluster.isEmpty()) {
					HostVO oneHost = hostsInCluster.get(0);
					_hostDao.loadDetails(oneHost);
					String hostOsInCluster = oneHost.getDetail("Host.OS");
					String hostOs = scc.getHostDetails().get("Host.OS");
					if (!hostOsInCluster.equalsIgnoreCase(hostOs)) {
						throw new IllegalArgumentException("Can't add host: "
								+ startup.getPrivateIpAddress()
								+ " with hostOS: " + hostOs
								+ " into a cluster," + "in which there are "
								+ hostOsInCluster + " hosts added");
					}
				}
			}

			// If this command is from the agent simulator, don't do the CIDR
			// check
			if (scc.getAgentTag() != null
					&& startup.getAgentTag()
							.equalsIgnoreCase("vmops-simulator")) {
				doCidrCheck = false;
			}

			// If this command is from a KVM agent, or from an agent that has a
			// null hypervisor type, don't do the CIDR check
			if (hypervisorType == null || hypervisorType == HypervisorType.KVM
					|| hypervisorType == HypervisorType.VMware || hypervisorType == HypervisorType.BareMetal) {
				doCidrCheck = false;
			}

			if (doCidrCheck) {
				s_logger.info("Host: " + host.getName()
						+ " connected with hypervisor type: " + hypervisorType
						+ ". Checking CIDR...");
			} else {
				s_logger.info("Host: " + host.getName()
						+ " connected with hypervisor type: " + hypervisorType
						+ ". Skipping CIDR check...");
			}

			if (doCidrCheck) {
				checkCIDR(type, p, dc, scc.getPrivateIpAddress(),
						scc.getPrivateNetmask());
			}

			// Check if the private/public IPs of the server are already in the
			// private/public IP address tables
			checkIPConflicts(type, p, dc, scc.getPrivateIpAddress(),
					scc.getPublicIpAddress(), scc.getPublicIpAddress(),
					scc.getPublicNetmask());
		}

		host.setDataCenterId(dc.getId());
		host.setPodId(podId);
		host.setClusterId(clusterId);
		host.setPrivateIpAddress(startup.getPrivateIpAddress());
		host.setPrivateNetmask(startup.getPrivateNetmask());
		host.setPrivateMacAddress(startup.getPrivateMacAddress());
		host.setPublicIpAddress(startup.getPublicIpAddress());
		host.setPublicMacAddress(startup.getPublicMacAddress());
		host.setPublicNetmask(startup.getPublicNetmask());
		host.setStorageIpAddress(startup.getStorageIpAddress());
		host.setStorageMacAddress(startup.getStorageMacAddress());
		host.setStorageNetmask(startup.getStorageNetmask());
		host.setVersion(startup.getVersion());
		host.setName(startup.getName());
		host.setType(type);
		host.setManagementServerId(msId);
		host.setStorageUrl(startup.getIqn());
		host.setLastPinged(System.currentTimeMillis() >> 10);
		if (startup instanceof StartupRoutingCommand) {
			final StartupRoutingCommand scc = (StartupRoutingCommand) startup;
			host.setCaps(scc.getCapabilities());
			host.setCpus(scc.getCpus());
			host.setTotalMemory(scc.getMemory());
			host.setSpeed(scc.getSpeed());
			HypervisorType hyType = scc.getHypervisorType();
			host.setHypervisorType(hyType);

		} else if (startup instanceof StartupStorageCommand) {
			final StartupStorageCommand ssc = (StartupStorageCommand) startup;
			host.setParent(ssc.getParent());
			host.setTotalSize(ssc.getTotalSize());
			host.setHypervisorType(HypervisorType.None);
			if (ssc.getNfsShare() != null) {
				host.setStorageUrl(ssc.getNfsShare());
			}
		}
		if (startup.getStorageIpAddressDeux() != null) {
			host.setStorageIpAddressDeux(startup.getStorageIpAddressDeux());
			host.setStorageMacAddressDeux(startup.getStorageMacAddressDeux());
			host.setStorageNetmaskDeux(startup.getStorageNetmaskDeux());
		}

	}
	
	public Host getHost(long hostId){
		return _hostDao.findById(hostId);
	}

	// create capacity entries if none exist for this server
	private void createCapacityEntry(final StartupCommand startup, HostVO server) {
		SearchCriteria<CapacityVO> capacitySC = _capacityDao
				.createSearchCriteria();
		capacitySC.addAnd("hostOrPoolId", SearchCriteria.Op.EQ, server.getId());
		capacitySC.addAnd("dataCenterId", SearchCriteria.Op.EQ,
				server.getDataCenterId());
		capacitySC.addAnd("podId", SearchCriteria.Op.EQ, server.getPodId());
		List<CapacityVO> capacities = _capacityDao.search(capacitySC, null);

		// remove old entries, we'll recalculate them anyway
		if (startup instanceof StartupStorageCommand) {
			if ((capacities != null) && !capacities.isEmpty()) {
				for (CapacityVO capacity : capacities) {
					_capacityDao.remove(capacity.getId());
				}
			}
		}

		if (startup instanceof StartupStorageCommand) {
			StartupStorageCommand ssCmd = (StartupStorageCommand) startup;
			if (ssCmd.getResourceType() == Storage.StorageResourceType.STORAGE_HOST) {
				CapacityVO capacity = new CapacityVO(server.getId(),
						server.getDataCenterId(), server.getPodId(), server.getClusterId(), 0L,
						server.getTotalSize() * _overProvisioningFactor,
						CapacityVO.CAPACITY_TYPE_STORAGE_ALLOCATED);
				_capacityDao.persist(capacity);
			}
		} else if (startup instanceof StartupRoutingCommand) {
			SearchCriteria<CapacityVO> capacityCPU = _capacityDao
					.createSearchCriteria();
			capacityCPU.addAnd("hostOrPoolId", SearchCriteria.Op.EQ,
					server.getId());
			capacityCPU.addAnd("dataCenterId", SearchCriteria.Op.EQ,
					server.getDataCenterId());
			capacityCPU
					.addAnd("podId", SearchCriteria.Op.EQ, server.getPodId());
			capacityCPU.addAnd("capacityType", SearchCriteria.Op.EQ,
					CapacityVO.CAPACITY_TYPE_CPU);
			List<CapacityVO> capacityVOCpus = _capacityDao.search(capacitySC,
					null);

			if (capacityVOCpus != null && !capacityVOCpus.isEmpty()) {
				CapacityVO CapacityVOCpu = capacityVOCpus.get(0);
				long newTotalCpu = (long) (server.getCpus().longValue()
						* server.getSpeed().longValue() * _cpuOverProvisioningFactor);
				if ((CapacityVOCpu.getTotalCapacity() <= newTotalCpu)
						|| ((CapacityVOCpu.getUsedCapacity() + CapacityVOCpu
								.getReservedCapacity()) <= newTotalCpu)) {
					CapacityVOCpu.setTotalCapacity(newTotalCpu);
				} else if ((CapacityVOCpu.getUsedCapacity()
						+ CapacityVOCpu.getReservedCapacity() > newTotalCpu)
						&& (CapacityVOCpu.getUsedCapacity() < newTotalCpu)) {
					CapacityVOCpu.setReservedCapacity(0);
					CapacityVOCpu.setTotalCapacity(newTotalCpu);
				} else {
					s_logger.debug("What? new cpu is :" + newTotalCpu
							+ ", old one is " + CapacityVOCpu.getUsedCapacity()
							+ "," + CapacityVOCpu.getReservedCapacity() + ","
							+ CapacityVOCpu.getTotalCapacity());
				}
				_capacityDao.update(CapacityVOCpu.getId(), CapacityVOCpu);
			} else {
				CapacityVO capacity = new CapacityVO(
						server.getId(),
						server.getDataCenterId(),
						server.getPodId(), 
						server.getClusterId(),
						0L,
						(long) (server.getCpus().longValue()
								* server.getSpeed().longValue() * _cpuOverProvisioningFactor),
						CapacityVO.CAPACITY_TYPE_CPU);
				_capacityDao.persist(capacity);
			}

			SearchCriteria<CapacityVO> capacityMem = _capacityDao
					.createSearchCriteria();
			capacityMem.addAnd("hostOrPoolId", SearchCriteria.Op.EQ,
					server.getId());
			capacityMem.addAnd("dataCenterId", SearchCriteria.Op.EQ,
					server.getDataCenterId());
			capacityMem
					.addAnd("podId", SearchCriteria.Op.EQ, server.getPodId());
			capacityMem.addAnd("capacityType", SearchCriteria.Op.EQ,
					CapacityVO.CAPACITY_TYPE_MEMORY);
			List<CapacityVO> capacityVOMems = _capacityDao.search(capacityMem,
					null);

			if (capacityVOMems != null && !capacityVOMems.isEmpty()) {
				CapacityVO CapacityVOMem = capacityVOMems.get(0);
				long newTotalMem = server.getTotalMemory();
				if (CapacityVOMem.getTotalCapacity() <= newTotalMem
						|| (CapacityVOMem.getUsedCapacity()
								+ CapacityVOMem.getReservedCapacity() <= newTotalMem)) {
					CapacityVOMem.setTotalCapacity(newTotalMem);
				} else if (CapacityVOMem.getUsedCapacity()
						+ CapacityVOMem.getReservedCapacity() > newTotalMem
						&& CapacityVOMem.getUsedCapacity() < newTotalMem) {
					CapacityVOMem.setReservedCapacity(0);
					CapacityVOMem.setTotalCapacity(newTotalMem);
				} else {
					s_logger.debug("What? new cpu is :" + newTotalMem
							+ ", old one is " + CapacityVOMem.getUsedCapacity()
							+ "," + CapacityVOMem.getReservedCapacity() + ","
							+ CapacityVOMem.getTotalCapacity());
				}
				_capacityDao.update(CapacityVOMem.getId(), CapacityVOMem);
			} else {
				CapacityVO capacity = new CapacityVO(server.getId(),
						server.getDataCenterId(), server.getPodId(), server.getClusterId(), 0L,
						server.getTotalMemory(),
						CapacityVO.CAPACITY_TYPE_MEMORY);
				_capacityDao.persist(capacity);
			}
		}

	}

	// protected void upgradeAgent(final Link link, final byte[] request, final
	// String reason) {
	//
	// if (reason == UnsupportedVersionException.IncompatibleVersion) {
	// final UpgradeResponse response = new UpgradeResponse(request,
	// _upgradeMgr.getAgentUrl());
	// try {
	// s_logger.info("Asking for the agent to update due to incompatible version: "
	// + response.toString());
	// link.send(response.toBytes());
	// } catch (final ClosedChannelException e) {
	// s_logger.warn("Unable to send response due to connection closed: " +
	// response.toString());
	// }
	// return;
	// }
	//
	// assert (reason == UnsupportedVersionException.UnknownVersion) :
	// "Unknown reason: " + reason;
	// final UpgradeResponse response = new UpgradeResponse(request,
	// _upgradeMgr.getAgentUrl());
	// try {
	// s_logger.info("Asking for the agent to update due to unknown version: " +
	// response.toString());
	// link.send(response.toBytes());
	// } catch (final ClosedChannelException e) {
	// s_logger.warn("Unable to send response due to connection closed: " +
	// response.toString());
	// }
	// }

	protected class SimulateStartTask implements Runnable {
		ServerResource resource;
		Map<String, String> details;
		long id;
		ActionDelegate<Long> actionDelegate;

		public SimulateStartTask(long id, ServerResource resource,
				Map<String, String> details, ActionDelegate<Long> actionDelegate) {
			this.id = id;
			this.resource = resource;
			this.details = details;
			this.actionDelegate = actionDelegate;
		}

		@Override
		public void run() {
            AgentAttache at = null;
			try {
				if (s_logger.isDebugEnabled()) {
					s_logger.debug("Simulating start for resource "
							+ resource.getName() + " id " + id);
				}
				simulateStart(resource, details, false, null, null);
			} catch (Exception e) {
				s_logger.warn("Unable to simulate start on resource " + id
						+ " name " + resource.getName(), e);
			} finally {
				if (actionDelegate != null) {
					actionDelegate.action(new Long(id));
				}
				if ( at == null ) {
				    HostVO host = _hostDao.findById(id);
				    host.setManagementServerId(null);
				    _hostDao.update(id, host);
				}
				StackMaid.current().exitCleanup();
			}
		}
	}

	public class AgentHandler extends Task {
		public AgentHandler(Task.Type type, Link link, byte[] data) {
			super(type, link, data);
		}

		protected void processRequest(final Link link, final Request request) {
			AgentAttache attache = (AgentAttache) link.attachment();
			final Command[] cmds = request.getCommands();
			Command cmd = cmds[0];
			boolean logD = true;

			Response response = null;
			if (attache == null) {
				s_logger.debug("Processing sequence " + request.getSequence()
						+ ": Processing " + request.toString());
				if (!(cmd instanceof StartupCommand)) {
					s_logger.warn("Throwing away a request because it came through as the first command on a connect: "
							+ request.toString());
					return;
				}
				StartupCommand startup = (StartupCommand) cmd;
				// if ((_upgradeMgr.registerForUpgrade(-1, startup.getVersion())
				// == UpgradeManager.State.RequiresUpdate) &&
				// (_upgradeMgr.getAgentUrl() != null)) {
				// final UpgradeCommand upgrade = new
				// UpgradeCommand(_upgradeMgr.getAgentUrl());
				// final Request req = new Request(1, -1, -1, new Command[] {
				// upgrade }, true, true);
				// s_logger.info("Agent requires upgrade: " + req.toString());
				// try {
				// link.send(req.toBytes());
				// } catch (ClosedChannelException e) {
				// s_logger.warn("Unable to tell agent it should update.");
				// }
				// return;
				// }
				try {
					StartupCommand[] startups = new StartupCommand[cmds.length];
					for (int i = 0; i < cmds.length; i++) {
						startups[i] = (StartupCommand) cmds[i];
					}
					attache = handleConnect(link, startups);
				} catch (final IllegalArgumentException e) {
					_alertMgr.sendAlert(
							AlertManager.ALERT_TYPE_HOST,
							0,
							new Long(0),
							"Agent from " + startup.getPrivateIpAddress()
									+ " is unable to connect due to "
									+ e.getMessage(),
							"Agent from " + startup.getPrivateIpAddress()
									+ " is unable to connect with "
									+ request.toString() + " because of "
									+ e.getMessage());
					s_logger.warn("Unable to create attache for agent: "
							+ request.toString(), e);
					response = new Response(request, new StartupAnswer(
							(StartupCommand) cmd, e.getMessage()), _nodeId, -1);
				} catch (ConnectionException e) {
					_alertMgr.sendAlert(
							AlertManager.ALERT_TYPE_HOST,
							0,
							new Long(0),
							"Agent from " + startup.getPrivateIpAddress()
									+ " is unable to connect due to "
									+ e.getMessage(),
							"Agent from " + startup.getPrivateIpAddress()
									+ " is unable to connect with "
									+ request.toString() + " because of "
									+ e.getMessage());
					s_logger.warn("Unable to create attache for agent: "
							+ request.toString(), e);
					response = new Response(request, new StartupAnswer(
							(StartupCommand) cmd, e.getMessage()), _nodeId, -1);
				} catch (final CloudRuntimeException e) {
					_alertMgr.sendAlert(
							AlertManager.ALERT_TYPE_HOST,
							0,
							new Long(0),
							"Agent from " + startup.getPrivateIpAddress()
									+ " is unable to connect due to "
									+ e.getMessage(),
							"Agent from " + startup.getPrivateIpAddress()
									+ " is unable to connect with "
									+ request.toString() + " because of "
									+ e.getMessage());
					s_logger.warn("Unable to create attache for agent: "
							+ request.toString(), e);
				}
				if (attache == null) {
					if (response == null) {
						s_logger.warn("Unable to create attache for agent: "
								+ request.toString());
						response = new Response(request, new StartupAnswer(
								(StartupCommand) request.getCommand(),
								"Unable to register this agent"), _nodeId, -1);
					}
					try {
						link.send(response.toBytes(), true);
					} catch (final ClosedChannelException e) {
						s_logger.warn("Response was not sent: "
								+ response.toString());
					}
					return;
				}
			}

			final long hostId = attache.getId();

			if (s_logger.isDebugEnabled()) {
				if (cmd instanceof PingRoutingCommand) {
					final PingRoutingCommand ping = (PingRoutingCommand) cmd;
					if (ping.getNewStates().size() > 0) {
						s_logger.debug("SeqA " + hostId + "-"
								+ request.getSequence() + ": Processing "
								+ request.toString());
					} else {
						logD = false;
						s_logger.debug("Ping from " + hostId);
						s_logger.trace("SeqA " + hostId + "-"
								+ request.getSequence() + ": Processing "
								+ request.toString());
					}
				} else if (cmd instanceof PingCommand) {
					logD = false;
					s_logger.debug("Ping from " + hostId);
					s_logger.trace("SeqA " + attache.getId() + "-"
							+ request.getSequence() + ": Processing "
							+ request.toString());
				} else {
					s_logger.debug("SeqA " + attache.getId() + "-"
							+ request.getSequence() + ": Processing "
							+ request.toString());
				}
			}

			final Answer[] answers = new Answer[cmds.length];
			for (int i = 0; i < cmds.length; i++) {
				cmd = cmds[i];
				Answer answer = null;
				try {
					if (cmd instanceof StartupRoutingCommand) {
						final StartupRoutingCommand startup = (StartupRoutingCommand) cmd;
						answer = new StartupAnswer(startup, attache.getId(),
								getPingInterval());
					} else if (cmd instanceof StartupProxyCommand) {
						final StartupProxyCommand startup = (StartupProxyCommand) cmd;
						answer = new StartupAnswer(startup, attache.getId(),
								getPingInterval());
					} else if (cmd instanceof StartupStorageCommand) {
						final StartupStorageCommand startup = (StartupStorageCommand) cmd;
						answer = new StartupAnswer(startup, attache.getId(),
								getPingInterval());
					} else if (cmd instanceof ShutdownCommand) {
						final ShutdownCommand shutdown = (ShutdownCommand) cmd;
						final String reason = shutdown.getReason();
						s_logger.info("Host "
								+ attache.getId()
								+ " has informed us that it is shutting down with reason "
								+ reason + " and detail "
								+ shutdown.getDetail());
						if (reason.equals(ShutdownCommand.Update)) {
							disconnect(attache, Event.UpdateNeeded, false);
						} else if (reason.equals(ShutdownCommand.Requested)) {
							disconnect(attache, Event.ShutdownRequested, false);
						}
						return;
					} else if (cmd instanceof AgentControlCommand) {
						answer = handleControlCommand(attache,
								(AgentControlCommand) cmd);
					} else {
						handleCommands(attache, request.getSequence(),
								new Command[] { cmd });
						if (cmd instanceof PingCommand) {
							long cmdHostId = ((PingCommand) cmd).getHostId();

							// if the router is sending a ping, verify the
							// gateway was pingable
							if (cmd instanceof PingRoutingCommand) {
								boolean gatewayAccessible = ((PingRoutingCommand) cmd)
										.isGatewayAccessible();
								HostVO host = _hostDao.findById(Long
										.valueOf(cmdHostId));
								if (!gatewayAccessible) {
									// alert that host lost connection to
									// gateway (cannot ping the default route)
									DataCenterVO dcVO = _dcDao.findById(host
											.getDataCenterId());
									HostPodVO podVO = _podDao.findById(host
											.getPodId());
									String hostDesc = "name: " + host.getName()
											+ " (id:" + host.getId()
											+ "), availability zone: "
											+ dcVO.getName() + ", pod: "
											+ podVO.getName();

									_alertMgr
											.sendAlert(
													AlertManager.ALERT_TYPE_ROUTING,
													host.getDataCenterId(),
													host.getPodId(),
													"Host lost connection to gateway, "
															+ hostDesc,
													"Host ["
															+ hostDesc
															+ "] lost connection to gateway (default route) and is possibly having network connection issues.");
								} else {
									_alertMgr.clearAlert(
											AlertManager.ALERT_TYPE_ROUTING,
											host.getDataCenterId(),
											host.getPodId());
								}
							}
							answer = new PingAnswer((PingCommand) cmd);
						} else if (cmd instanceof ReadyAnswer) {
							HostVO host = _hostDao.findById(attache.getId());
							if (host == null) {
								if (s_logger.isDebugEnabled()) {
									s_logger.debug("Cant not find host "
											+ attache.getId());
								}
							}
							answer = new Answer(cmd);
						} else {
							answer = new Answer(cmd);
						}
					}
				} catch (final Throwable th) {
					s_logger.warn("Caught: ", th);
					answer = new Answer(cmd, false, th.getMessage());
				}
				answers[i] = answer;
			}

			response = new Response(request, answers, _nodeId, attache.getId());
			if (s_logger.isDebugEnabled()) {
				if (logD) {
					s_logger.debug("SeqA " + attache.getId() + "-"
							+ response.getSequence() + ": Sending "
							+ response.toString());
				} else {
					s_logger.trace("SeqA " + attache.getId() + "-"
							+ response.getSequence() + ": Sending "
							+ response.toString());
				}
			}
			try {
				link.send(response.toBytes());
			} catch (final ClosedChannelException e) {
				s_logger.warn("Unable to send response because connection is closed: "
						+ response.toString());
			}
		}

		protected void processResponse(final Link link, final Response response) {
			final AgentAttache attache = (AgentAttache) link.attachment();
			if (attache == null) {
				s_logger.warn("Unable to process: " + response.toString());
			}

			if (!attache.processAnswers(response.getSequence(), response)) {
				s_logger.info("Host " + attache.getId() + " - Seq "
						+ response.getSequence()
						+ ": Response is not processed: " + response.toString());
			}
		}

		@Override
		protected void doTask(final Task task) throws Exception {
			Transaction txn = Transaction.open(Transaction.CLOUD_DB);
			try {
				final Type type = task.getType();
				if (type == Task.Type.DATA) {
					final byte[] data = task.getData();
					try {
						final Request event = Request.parse(data);
						if (event instanceof Response) {
							processResponse(task.getLink(), (Response) event);
						} else {
							processRequest(task.getLink(), event);
						}
					} catch (final UnsupportedVersionException e) {
						s_logger.warn(e.getMessage());
						// upgradeAgent(task.getLink(), data, e.getReason());
					}
				} else if (type == Task.Type.CONNECT) {
				} else if (type == Task.Type.DISCONNECT) {
					final Link link = task.getLink();
					final AgentAttache attache = (AgentAttache) link
							.attachment();
					if (attache != null) {
						disconnect(attache, Event.AgentDisconnected, true);
					} else {
						s_logger.info("Connection from " + link.getIpAddress()
								+ " closed but no cleanup was done.");
						link.close();
						link.terminated();
					}
				}
			} finally {
				StackMaid.current().exitCleanup();
				txn.close();
			}
		}
	}

	protected AgentManagerImpl() {
	}
}
