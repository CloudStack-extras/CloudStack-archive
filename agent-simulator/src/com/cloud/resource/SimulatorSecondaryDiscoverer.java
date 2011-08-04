package com.cloud.resource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentStorageResource;
import com.cloud.storage.secondary.SecondaryStorageDiscoverer;
import com.cloud.utils.net.NfsUtils;

/**
 * SimulatorSecondaryDiscoverer is used to discover secondary
 * storage servers for the simulator and make sure everything it can do is
 * correct.
 */
@Local(value=Discoverer.class)
public class SimulatorSecondaryDiscoverer extends SecondaryStorageDiscoverer implements
		Discoverer {
	private static final Logger s_logger = Logger.getLogger(SimulatorSecondaryDiscoverer.class);
	
	@Override
	public Map<? extends ServerResource, Map<String, String>> find(long dcId,
			Long podId, Long clusterId, URI uri, String username,
			String password) {

		if (uri.getAuthority().contains("sim")) {
			return createSimulatorAgentStorageResource(dcId, podId, uri);
		} else if (uri.getScheme().equalsIgnoreCase("nfs") || uri.getScheme().equalsIgnoreCase("iso")) {
            return createNfsSecondaryStorageResource(dcId, podId, uri);
        }  else if (uri.getScheme().equalsIgnoreCase("file")) {
			return createLocalSecondaryStorageResource(dcId, podId, uri);
		} else if (uri.getScheme().equalsIgnoreCase("dummy")) {
			return createDummySecondaryStorageResource(dcId, podId, uri);
		} else {
			return null;
		}
	}

	protected Map<? extends ServerResource, Map<String, String>> createSimulatorAgentStorageResource(
			long dcId, Long podId, URI uri) {

		String mountStr = NfsUtils.uri2Mount(uri);

		Map<AgentStorageResource, Map<String, String>> srs = new HashMap<AgentStorageResource, Map<String, String>>();

		AgentStorageResource storage;
		if (_configDao.isPremium()) {
			Class<?> impl;
			String name = "com.cloud.agent.AgentStorageResource";
			try {
				impl = Class.forName(name);
				final Constructor<?> constructor = impl
						.getDeclaredConstructor();
				constructor.setAccessible(true);
				storage = (AgentStorageResource) constructor
						.newInstance();
			} catch (final ClassNotFoundException e) {
				s_logger
						.error("Unable to load "+name+" due to ClassNotFoundException");
				return null;
			} catch (final SecurityException e) {
				s_logger
						.error("Unable to load "+ name + " due to SecurityException");
				return null;
			} catch (final NoSuchMethodException e) {
				s_logger
						.error("Unable to load "+name+" due to NoSuchMethodException");
				return null;
			} catch (final IllegalArgumentException e) {
				s_logger
						.error("Unable to load "+name+" due to IllegalArgumentException");
				return null;
			} catch (final InstantiationException e) {
				s_logger
						.error("Unable to load "+name+" due to InstantiationException");
				return null;
			} catch (final IllegalAccessException e) {
				s_logger
						.error("Unable to load "+name+" due to IllegalAccessException");
				return null;
			} catch (final InvocationTargetException e) {
				s_logger
						.error("Unable to load "+name+" due to InvocationTargetException");
				return null;
			}
		} else {
			storage = new AgentStorageResource();
		}

		Map<String, String> details = new HashMap<String, String>();
		details.put("mount.path", mountStr);
		details.put("orig.url", uri.toString());
		details.put("mount.parent", "dummy");

		Map<String, Object> params = new HashMap<String, Object>();
		params.putAll(details);
		params.put("zone", Long.toString(dcId));
		if (podId != null) {
			params.put("pod", podId.toString());
		}
		params.put("guid", uri.toString());
		params.put("secondary.storage.vm", "false");
		params.put("max.template.iso.size", _configDao
				.getValue("max.template.iso.size"));

		try {
			storage.configure("Storage", params);
		} catch (ConfigurationException e) {
			s_logger.warn("Unable to configure the storage ", e);
			return null;
		}
		srs.put(storage, details);

		return srs;
	}
}
