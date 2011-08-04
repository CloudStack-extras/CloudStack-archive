/**
 * *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
*
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.PatternSyntaxException;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentResourceBase;
import com.cloud.agent.AgentRoutingResource;
import com.cloud.agent.AgentStorageResource;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.component.Inject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Local(value = { SimulatorManager.class })
public class SimulatorManagerImpl implements SimulatorManager {
	private static final Logger s_logger = Logger
			.getLogger(SimulatorManagerImpl.class);

	private static final long STARTUP_DELAY = 600000;
	private static final long SCAN_INTERVAL = 600000;

	private static SimulatorManagerImpl _instance;
	private Map<AgentResourceBase, Map<String, String>> _resources = new ConcurrentHashMap<AgentResourceBase, Map<String, String>>();
	private final Random rand = new Random();

	private final Properties properties = new Properties();
	private String agentTypeSequence;
	private int agentCount;
	private int _rollingIndex = 0;

	@Inject	HostPodDao _podDao = null;	
	@Inject	HostDao _hostDao = null;    

	private final Timer _timer = new Timer("AgentSimulatorReport Task");
    private final Timer _propertyTimer = new Timer("PropertyFileScanner Task");

	private SecureRandom random;
	private int _runid;
	
	private File[] pool;

	private boolean _useDistribution=false;
	private List<Pair<Long, Long>> _distribution = new ArrayList<Pair<Long,Long>>();

	/**
	 * This no-args constructor is only for the gson serializer. Not to be used
	 * otherwise
	 */
	protected SimulatorManagerImpl() {
		
	}

	@Override
	public Map<AgentResourceBase, Map<String, String>> createServerResources(
			Map<String, Object> params) {
		properties.putAll(params);
		Map<String, String> args = new HashMap<String, String>();
		Map<AgentResourceBase, Map<String,String>> newResources = new HashMap<AgentResourceBase, Map<String,String>>();
		String name;
		AgentResourceBase agentResource;
		// make first ServerResource as SecondaryStorage (single one per zone)
		if( getZone() == null) {
			return null;
		}
	    long zoneId = getZone().longValue();
		
		if (_hostDao.findSecondaryStorageHost(zoneId) == null) {
			agentResource = new AgentStorageResource(1,
					AgentType.Storage, this);
			if (agentResource != null) {
				try {
					agentResource.start();
					name = "SimulatedAgent." + agentResource.getGuid();
					args.put(name, name);
					agentResource.configure(name, PropertiesUtil
							.toMap(properties));
					newResources.put(agentResource, args);
					_resources.put(agentResource, args);
				} catch (ConfigurationException e) {
					s_logger.error("error while configuring server resource"
							+ e.getMessage());
				}
			}
		}

		synchronized (this) {
			for (int i = 0; i < getWorkers(); i++) {
				long agentId = getNextAgentId();
				agentResource = new AgentRoutingResource(agentId,
						AgentType.Routing, this);
				if (agentResource != null) {
					try {
						agentResource.start();
						name = "SimulatedAgent." + agentResource.getGuid();
						agentResource.configure(name,
								PropertiesUtil.toMap(properties));
						args.put(name, name);
						newResources.put(agentResource, args);
						_resources.put(agentResource, args);
					} catch (ConfigurationException e) {
						s_logger
								.error("error while configuring server resource"
										+ e.getMessage());
					}
				}
			}
		}
		return newResources;
	}

	

	@Override
	public Properties getProperties() {
		return properties;
	}

	public class FileAgentStateFilter implements FilenameFilter {
		String _ext;

		public FileAgentStateFilter() {
			_ext = ".json";
		}

		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(_ext);
		}
	}
	
	@Override
	public boolean configure(String name, Map<String, Object> params)
			throws ConfigurationException {
		loadProperties();
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
			_runid = Math.abs(random.nextInt()) % 113;
			_instance = this;
			getDelayDistribution();
		} catch (NoSuchAlgorithmException e) {
			s_logger.error("SHAIPRNG is not a valid algorithm");
		}
		_timer.schedule(new AgentSimulatorReportTask(), STARTUP_DELAY,
				SCAN_INTERVAL);
		if (isPropertyScannerEnabled())
			_propertyTimer.schedule(new PropertyFileScanTask(), 300000,
					getPropertyScanInterval() * 1000);

		FileAgentStateFilter filter = new FileAgentStateFilter();
		File dir = new File(getAgentPath());
		if (!dir.exists()) {
			s_logger.warn("The directory " + getAgentPath()
					+ " containing agent states does not exist, creating one");
			dir.mkdir();
			s_logger.info(getAgentPath() + " created");
		}
		dir.setReadable(true);
		dir.setWritable(true);
		dir.setExecutable(true);
		pool = dir.listFiles(filter);
		if (pool.length == 0) {
			s_logger.info("The directory " + getAgentPath()
					+ " containing agent states is empty. Assuming fresh run");

		}
		return true;
	}
	
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean start() {
		s_logger.info("Simulator Manager started");
		return true;
	}

	@Override
	public boolean stop() {
		try {
			saveAllResourceStates(getAgentPath());
		} catch (FileNotFoundException e) {
			s_logger.info("Failed to stop simulator because of "
					+ e.getStackTrace());
		} catch (IOException e) {
			s_logger.info("Failed to stop simulator because of "
					+ e.getStackTrace());
		}
		s_logger.info("Simulator Manager stopped successfully");
		return true;
	}

	@Override
	public synchronized boolean saveResourceState(String path,
			AgentResourceBase resource) {
		File dir;
		if (path == null) {
			path = getAgentPath();
		}
		dir = new File(path);
		if (!dir.exists()) {
			s_logger.info("Creating new agent directory path: "
					+ dir.getAbsolutePath());
			dir.mkdir();
		}
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues()
				.serializeNulls().create();
		Writer writer;
		try {
			String filePrefix = getFilePrefix(resource);
			s_logger.info("Saving resource with guid: "
					+ resource.getGuid() + " to path: "
					+ dir.getAbsolutePath());
			File agentFile = new File(dir.getAbsolutePath(), filePrefix + "."
					+ resource.getGuid() + ".json");
			//Set permissions -rwx-rwx-rwx
			agentFile.setReadable(true, false);
			agentFile.setWritable(true, false);
			agentFile.setExecutable(true, false);
			writer = new OutputStreamWriter(new FileOutputStream(agentFile));
			gson.toJson(resource, writer);
			writer.close();
		} catch (FileNotFoundException e) {
			s_logger.error("FileNotFoundException " + e.getMessage());
		} catch (IOException e) {
			s_logger.error("IOException: " + e.getMessage());
		}
		return true;
	}

	private String getFilePrefix(AgentResourceBase resource) {
		if (resource.getType() == Host.Type.Routing)
			return "AgentRouting";
		else
			return "AgentStorage";
	}

	public synchronized boolean saveAllResourceStates(String path)
			throws FileNotFoundException, IOException {
		for (Entry<AgentResourceBase, Map<String, String>> res : _resources
				.entrySet()) {
			AgentResourceBase resource = res.getKey();
			saveResourceState(path, resource);
		}
		return true;
	}

	private class AgentSimulatorReportTask extends TimerTask {
		@Override
		public void run() {
			try {
				if (getZone() == null) {
					return;
				}
				List<HostVO> rtngHosts = _hostDao.listBy(Host.Type.Routing,
						getZone());
				int size = 0;
				if (rtngHosts != null) {
					size = rtngHosts.size();
				}
				s_logger.info("Simulator running with: " + size
						+ " hosts in zone: " + getZone());

			} catch (Throwable e) {
				s_logger.error("Unexpected exception " + e.getMessage(), e);
			}
		}
	}

	@Override
	public void loadProperties() {
		File file = PropertiesUtil.findConfigFile("simulator.properties");
		if (file != null) {
			try {
				properties.load(new FileInputStream(file));
			} catch (FileNotFoundException ex) {
				s_logger
						.warn("simulator.properties file was not found. using defaults");
			} catch (IOException ex) {
			}
		} else {
			s_logger
					.warn("Could not find simulator.properties for loading simulator args");
		}
	}

	@Override
	public synchronized long getNextAgentId() {
		return random.nextLong();
	}

	@Override
	public AgentType getAgentType(int iteration) {
		switch (getSequence().charAt(iteration % getSequence().length())) {
		case 'r':
			return AgentType.Routing;

		case 's':
			return AgentType.Storage;

		default:
			s_logger
					.error("Invalid agent type character, default to routing agent");
			break;
		}
		return AgentType.Routing;
	}

	@Override
	public String getAgentPath() {
		return properties.getProperty("agent.save.path");
	}

	@Override
	public int getPort() {
		return NumbersUtil.parseInt(properties.getProperty("port"), 8250);
	}

	@Override
	public int getWorkers() {
		agentCount = NumbersUtil.parseInt(properties.getProperty("workers"), 3);
		return agentCount;
	}

	@Override
	public String getSequence() {
		agentTypeSequence = properties.getProperty("sequence", "rs");
		return agentTypeSequence;
	}
	
	@Override
	public List<Pair<Long, Long>> getDelayDistribution() {
		String strDistribution = properties.getProperty("delay.distribution", null);
		if(strDistribution == null) {
			return null;
		}
		_useDistribution = true;
		s_logger.info("Found property delay.distribution. Using it for operation delays");
		_distribution.clear();
		try {
			strDistribution = strDistribution.trim().replaceAll("[{()} ]", "");
			String[] delayPairs = strDistribution.split(";");
			for(String str : delayPairs) {
				Long one = Long.parseLong(str.split(",")[0]);
				Long two = Long.parseLong(str.split(",")[1]);
				Pair<Long, Long> pair = new Pair<Long, Long>(one, two);
				_distribution.add(pair);
			}
			if(s_logger.isDebugEnabled()) {
				s_logger.debug("Delay Distribution: " + _distribution);
			}
		} catch(PatternSyntaxException e) {
			s_logger.error("delay.distribution property is not in the right format in simulator.properties");
		} catch(IndexOutOfBoundsException e) {
			s_logger.error("delay.distribution property is not in the right format in simulator.properties");
		}
		return _distribution;
	}

	@Override
	public Long getZone() {
		return Long.parseLong(properties.getProperty("zone"));
	}
	
	@Override
	public int getRunId() {
		_runid = NumbersUtil.parseInt(properties.getProperty("run"), Math.abs(random.nextInt()) % 113);
		return _runid;
	}

	@Override
	public String getPodCidrPrefix() {
		try {
			Long podId = NumbersUtil.parseLong(properties.getProperty("pod"), 1);
			HashMap<Long, List<Object>> podMap = _podDao
					.getCurrentPodCidrSubnets(getZone(), 0);
			List<Object> cidrPair = podMap.get(podId);
			String cidrAddress = (String) cidrPair.get(0);
			String prefix = cidrAddress.split("\\.")[0] + "."
					+ cidrAddress.split("\\.")[1] + "."
					+ cidrAddress.split("\\.")[2];
			return prefix;
		} catch (PatternSyntaxException e) {
			s_logger.error("Exception while splitting pod cidr");
			return null;
		} catch(IndexOutOfBoundsException e) {
			s_logger.error("Invalid pod cidr. Please check");
			return null;
		}
	}

	
	/**
	 * Lazy loads the resource from the nfs share containing saved agent state
	 * @param agentname of the resource, of the form SimulatedAgent.<guid>
     * @param params to start the resource
	 * @return boolean true/false
	 */
	@Override
	public synchronized boolean checkPoolForResource(String agentname,
			Map<String, Object> params) {
		Map<String, String> args = new HashMap<String, String>();
		Gson gson = new GsonBuilder().create();
		
		try {
			Reader reader;
			File agent = new File(getAgentPath(), "AgentRouting."
					+ agentname.split("\\.")[1] + ".json");
			reader = new InputStreamReader(new FileInputStream(agent));
			args.put(agentname, agentname);

			AgentRoutingResource res = gson.fromJson(reader,
					AgentRoutingResource.class);
			reader.close();
			synchronized (_resources) {
				_resources.put(res, args);
			}
			s_logger.info("successfully reconnected agent " + agentname
					+ " from the pool");
			return true;
		} catch (PatternSyntaxException pse) {
			s_logger.error("Error while splitting agent name: " + agentname);
			return false;
		} catch (FileNotFoundException fnfe) {
			s_logger.warn("Simulated Agent Lost/Agent state file not found for: " + agentname);
			return false;
		} catch (IOException ioe) {
			s_logger.error("IOException when reloading agent " +agentname+ " from pool");
			return false;
		} finally {
		}
	}

	@Override
	public synchronized AgentResourceBase getResourceByName(String name) {
		for (Entry<AgentResourceBase, Map<String, String>> res : _resources
				.entrySet()) {
			if (res.getKey().getName().equalsIgnoreCase(name)) {
				return res.getKey();
			}
		}
		return null;
	}

	/**
	 * Randomizes the delay between two values start and end. If a delay
	 * distribution is specified, the distribution overrides the
	 * latency.[start|end].range properties
	 */
	@Override
	public int randomizeWaitDelay(int start, int end) {
		synchronized (_distribution) {
			if(_useDistribution) {
				try {
					if(_distribution != null && _distribution.size() != 0) {
						start = _distribution.get(_rollingIndex).first().intValue();
						end = _distribution.get(_rollingIndex).second().intValue();
						if(s_logger.isDebugEnabled()) {
							s_logger.debug("Applying delay b/w: " +_distribution.get(_rollingIndex)+ " from distribution: " + _distribution);
						}
						_rollingIndex = ( ++ _rollingIndex ) % _distribution.size();				
					}
				} catch(IndexOutOfBoundsException e) {
					s_logger.error("Something is wrong!" + e.getStackTrace());
				}
			}			
		}
		int randomDiff = 0;
		if (start != end)
			randomDiff = rand.nextInt(Math.abs(start - end));
		try {
			if (start + randomDiff <= end) {
				Thread.sleep((start + randomDiff) * 1000);
			} else {
				Thread.sleep(start * 1000);
			}
		} catch (InterruptedException e) {
			s_logger.error("Thread Sleep was interrupted!" + e.getStackTrace());
		}
		return start+randomDiff;
	}
	
	private boolean isPropertyScannerEnabled() {
		int isEnabled = NumbersUtil.parseInt(properties.getProperty("property.scan.enabled"), 0);
		if (isEnabled > 0)
			return true;
		return false;
	}

	private class PropertyFileScanTask extends TimerTask {
		@Override
		public void run() {
			try {
				s_logger.info("Attempting to reload properties");
				loadProperties();
				getDelayDistribution();
			} catch (Throwable e) {
				s_logger.error("Unexpected exception " + e.getMessage(), e);
			}
		}
	}

	private int getPropertyScanInterval() {
		return NumbersUtil.parseInt(properties
				.getProperty("property.scan.interval"), 0);
	}
	
	@Override
	public int getDelayEnd() {
		return NumbersUtil.parseInt(properties.getProperty("latency.end.range"), 0);
	}

	@Override
	public int getDelayStart() {
		return NumbersUtil.parseInt(properties.getProperty("latency.start.range"), 0);
	}
}