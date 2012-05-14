// Copyright 2012 Citrix Systems, Inc. Licensed under the
// Apache License, Version 2.0 (the "License"); you may not use this
// file except in compliance with the License.  Citrix Systems, Inc.
// reserves all rights not expressly granted by the License.
// You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 
package com.cloud.network.element.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.LogNetworkEventCommand;
import com.cloud.dc.DataCenter;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.dao.HostDao;
import com.cloud.network.Network;
import com.cloud.network.PhysicalNetworkServiceProvider;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.element.LoggingExampleElementService;
import com.cloud.network.element.NetworkElement;
import com.cloud.offering.NetworkOffering;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.component.Inject;
import com.cloud.utils.component.PluggableService;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.host.Host;

@Local(value=NetworkElement.class)
public class LoggingExampleElement implements NetworkElement, LoggingExampleElementService {

	private static final Logger s_logger = Logger.getLogger(LoggingExampleElement.class);
	private boolean _useTrace = false;
	@Inject
	HostDao _hostDao;
	@Inject
	ResourceManager _resourceMgr;
	@Inject
	AgentManager _agentMgr;
	
	public LoggingExampleElement() {
	}
	
	public boolean addInstance(long zoneId, String url) {
		Map<String, Object> details = new HashMap<String, Object>();
		details.put("guid", "url");
		details.put("zone", Long.toString(zoneId));
		LoggingExampleResource resource = new LoggingExampleResource();
		resource.configure("LoggingResource", details);
		Host host = _resourceMgr.addHost(zoneId, resource, Host.Type.ExternalFirewall, new HashMap<String,String>());
		return host != null;
	}
	
	/* (non-Javadoc)
	 * @see com.cloud.utils.component.Adapter#configure(java.lang.String, java.util.Map)
	 */
	@Override
	public boolean configure(String name, Map<String, Object> params)
			throws ConfigurationException {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see com.cloud.utils.component.Adapter#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "LoggingExampleElement";
	}

	/* (non-Javadoc)
	 * @see com.cloud.utils.component.Adapter#start()
	 */
	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see com.cloud.utils.component.Adapter#stop()
	 */
	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see com.cloud.utils.component.PluggableService#getPropertiesFile()
	 */
	@Override
	public String getPropertiesFile() {
		return "logging-example.properties";
	}

	/* (non-Javadoc)
	 * @see com.cloud.network.element.NetworkElement#getCapabilities()
	 */
	@Override
	public Map<Service, Map<Capability, String>> getCapabilities() {
		return new HashMap<Service, Map<Capability, String>>();
	}
	
	protected static Provider _provider = new Provider("LoggingExampleProvider", false);

	/* (non-Javadoc)
	 * @see com.cloud.network.element.NetworkElement#getProvider()
	 */
	@Override
	public Provider getProvider() {
		// TODO Auto-generated method stub
		return _provider;
	}

	/* (non-Javadoc)
	 * @see com.cloud.network.element.NetworkElement#implement(com.cloud.network.Network, com.cloud.offering.NetworkOffering, com.cloud.deploy.DeployDestination, com.cloud.vm.ReservationContext)
	 */
	@Override
	public boolean implement(Network network, NetworkOffering offering,
			DeployDestination dest, ReservationContext context)
			throws ConcurrentOperationException, ResourceUnavailableException,
			InsufficientCapacityException {
		if (_useTrace) {
			s_logger.trace("Network has just been implemented: " + network.getName() + " Broadcast uri=" + network.getBroadcastUri());
		} else {
			s_logger.debug("Network has just been implemented: " + network.getName() + " Broadcast uri=" + network.getBroadcastUri());
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see com.cloud.network.element.NetworkElement#prepare(com.cloud.network.Network, com.cloud.vm.NicProfile, com.cloud.vm.VirtualMachineProfile, com.cloud.deploy.DeployDestination, com.cloud.vm.ReservationContext)
	 */
	@Override
	public boolean prepare(Network network, NicProfile nic,
			VirtualMachineProfile<? extends VirtualMachine> vm,
			DeployDestination dest, ReservationContext context)
			throws ConcurrentOperationException, ResourceUnavailableException,
			InsufficientCapacityException {
		if (_useTrace) {
			s_logger.trace("VM " + vm.getInstanceName() + " is being provisioned into network " + network.getName() + " nic has ip address " + nic.getIp4Address());
		} else {
			s_logger.debug("VM " + vm.getInstanceName() + " is being provisioned into network " + network.getName() + " nic has ip address " + nic.getIp4Address());
		}
		DataCenter dc = dest.getDataCenter();
		Host host = _hostDao.findByTypeNameAndZoneId(dc.getId(), "url", Host.Type.ExternalFirewall);
		LogNetworkEventCommand cmd = new LogNetworkEventCommand(_useTrace ? "trace" : "debug", "Network has just been implemented: " + network.getName() + " Broadcast uri=" + network.getBroadcastUri());
		_agentMgr.easySend(host.getId(), cmd);
		return false;
	}

	/* (non-Javadoc)
	 * @see com.cloud.network.element.NetworkElement#release(com.cloud.network.Network, com.cloud.vm.NicProfile, com.cloud.vm.VirtualMachineProfile, com.cloud.vm.ReservationContext)
	 */
	@Override
	public boolean release(Network network, NicProfile nic,
			VirtualMachineProfile<? extends VirtualMachine> vm,
			ReservationContext context) throws ConcurrentOperationException,
			ResourceUnavailableException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.cloud.network.element.NetworkElement#shutdown(com.cloud.network.Network, com.cloud.vm.ReservationContext, boolean)
	 */
	@Override
	public boolean shutdown(Network network, ReservationContext context,
			boolean cleanup) throws ConcurrentOperationException,
			ResourceUnavailableException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.cloud.network.element.NetworkElement#destroy(com.cloud.network.Network)
	 */
	@Override
	public boolean destroy(Network network)
			throws ConcurrentOperationException, ResourceUnavailableException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.cloud.network.element.NetworkElement#isReady(com.cloud.network.PhysicalNetworkServiceProvider)
	 */
	@Override
	public boolean isReady(PhysicalNetworkServiceProvider provider) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.cloud.network.element.NetworkElement#shutdownProviderInstances(com.cloud.network.PhysicalNetworkServiceProvider, com.cloud.vm.ReservationContext)
	 */
	@Override
	public boolean shutdownProviderInstances(
			PhysicalNetworkServiceProvider provider, ReservationContext context)
			throws ConcurrentOperationException, ResourceUnavailableException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.cloud.network.element.NetworkElement#canEnableIndividualServices()
	 */
	@Override
	public boolean canEnableIndividualServices() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.cloud.network.element.NetworkElement#verifyServicesCombination(java.util.List)
	 */
	@Override
	public boolean verifyServicesCombination(List<String> services) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void changeLoggingLevel(String level) {
		if (level.compareToIgnoreCase("debug") == 0) {
			_useTrace = false;
		} else if (level.compareToIgnoreCase("trace") == 0){
			_useTrace = true;
		} else {
			throw new InvalidParameterValueException("The logging level value must be trace or debug");
			
		}
		// TODO Auto-generated method stub
		
	}

}
