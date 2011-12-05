/**
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
package com.cloud.baremetal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupExternalDhcpCommand;
import com.cloud.agent.api.routing.DhcpEntryCommand;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Network;
import com.cloud.resource.ResourceManager;
import com.cloud.resource.ResourceStateAdapter;
import com.cloud.resource.ServerResource;
import com.cloud.resource.UnableDeleteHostException;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;

@Local(value = {ExternalDhcpManager.class})
public class ExternalDhcpManagerImpl implements ExternalDhcpManager, ResourceStateAdapter {
	private static final org.apache.log4j.Logger s_logger = Logger.getLogger(ExternalDhcpManagerImpl.class);
	protected String _name;
	@Inject DataCenterDao _dcDao;
	@Inject HostDao _hostDao;
	@Inject AgentManager _agentMgr;
	@Inject HostPodDao _podDao;
	@Inject UserVmDao _userVmDao;
	@Inject ResourceManager _resourceMgr;
	@Inject NicDao _nicDao;
	
	@Override
	public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
		_resourceMgr.registerResourceStateAdapter(this.getClass().getSimpleName(), this);
		return true;
	}

	@Override
	public boolean start() {
		return true;
	}

	@Override
	public boolean stop() {
		_resourceMgr.unregisterResourceStateAdapter(this.getClass().getSimpleName());
		return true;
	}

	@Override
	public String getName() {
		return _name;
	}

	protected String getDhcpServerGuid(String zoneId, String name, String ip) {
		return zoneId + "-" + name + "-" + ip;
	}
	
	
	@Override @DB
	public Host addDhcpServer(Long zoneId, Long podId, String type, String url, String username, String password) {	
		DataCenterVO zone = _dcDao.findById(zoneId);
		if (zone == null) {
			throw new InvalidParameterValueException("Could not find zone with ID: " + zoneId);
		} 
		
		HostPodVO pod = _podDao.findById(podId);
		if (pod == null) {
			throw new InvalidParameterValueException("Could not find pod with ID: " + podId);
		} 
		
		List<HostVO> dhcps = _resourceMgr.listAllUpAndEnabledHosts(Host.Type.ExternalDhcp, null, podId, zoneId);
		if (dhcps.size() != 0) {
			throw new InvalidParameterValueException("Already had a DHCP server in Pod: " + podId + " zone: " + zoneId);
		}
		
		
		String ipAddress = url;
		String guid = getDhcpServerGuid(Long.toString(zoneId) + "-" + Long.toString(podId), "ExternalDhcp", ipAddress);
		Map params = new HashMap<String, String>();
		params.put("type", type);
		params.put("zone", Long.toString(zoneId));
		params.put("pod", podId.toString());
		params.put("ip", ipAddress);
		params.put("username", username);
		params.put("password", password);
		params.put("guid", guid);
		params.put("pod", Long.toString(podId));
		params.put("gateway", pod.getGateway());
		String dns = zone.getDns1();
		if (dns == null) {
			dns = zone.getDns2();
		}
		params.put("dns", dns);
		
		ServerResource resource = null;
		try {
			if (type.equalsIgnoreCase(DhcpServerType.Dnsmasq.getName())) {
				resource = new DnsmasqResource();
				resource.configure("Dnsmasq resource", params);
			} else if (type.equalsIgnoreCase(DhcpServerType.Dhcpd.getName())) {
				resource = new DhcpdResource();
				resource.configure("Dhcpd resource", params);
			} else {
				throw new CloudRuntimeException("Unsupport DHCP server " + type);
			}
		} catch (Exception e) {
			s_logger.debug(e);
			throw new CloudRuntimeException(e.getMessage());
		}
		
		Host dhcpServer = _resourceMgr.addHost(zoneId, resource, Host.Type.ExternalDhcp, params);
		if (dhcpServer == null) {
			throw new CloudRuntimeException("Cannot add external Dhcp server as a host");
		}
		
		Transaction txn = Transaction.currentTxn();
        txn.start();
        pod.setExternalDhcp(true);
        _podDao.update(pod.getId(), pod);
        txn.commit();
		return dhcpServer;
	}
	
	@Override
	public DhcpServerResponse getApiResponse(Host dhcpServer) {
		DhcpServerResponse response = new DhcpServerResponse();
		response.setId(dhcpServer.getId());
		return response;
	}

	private void prepareBareMetalDhcpEntry(NicProfile nic, DhcpEntryCommand cmd) {
		Long vmId = nic.getVmId();
		UserVmVO vm = _userVmDao.findById(vmId);
		if (vm == null || vm.getHypervisorType() != HypervisorType.BareMetal) {
			s_logger.debug("VM " + vmId + " is not baremetal machine, skip preparing baremetal DHCP entry");
			return;
		}
		
		List<HostVO> servers = _resourceMgr.listAllUpAndEnabledHosts(Host.Type.PxeServer, null, vm.getPodIdToDeployIn(), vm.getDataCenterIdToDeployIn());
		if (servers.size() != 1) {
			throw new CloudRuntimeException("Wrong number of PXE server found in zone " + vm.getDataCenterIdToDeployIn()
					+ " Pod " + vm.getPodIdToDeployIn() + ", number is " + servers.size());
		}
		HostVO pxeServer = servers.get(0);
		cmd.setNextServer(pxeServer.getPrivateIpAddress());
		s_logger.debug("Set next-server to " + pxeServer.getPrivateIpAddress() + " for VM " + vm.getId());
	}
	
	@Override
	public boolean addVirtualMachineIntoNetwork(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> profile, DeployDestination dest,
			ReservationContext context) throws ResourceUnavailableException {
		Long zoneId = profile.getVirtualMachine().getDataCenterIdToDeployIn();
		Long podId = profile.getVirtualMachine().getPodIdToDeployIn();
		List<HostVO> hosts = _resourceMgr.listAllUpAndEnabledHosts(Type.ExternalDhcp, null, podId, zoneId);
		if (hosts.size() == 0) {
			throw new CloudRuntimeException("No external Dhcp found in zone " + zoneId + " pod " + podId);
		}
		
		if (hosts.size() > 1) {
			throw new CloudRuntimeException("Something wrong, more than 1 external Dhcp found in zone " + zoneId + " pod " + podId);
		}
		
		HostVO h = hosts.get(0);
		String dns = nic.getDns1();
		if (dns == null) {
			dns = nic.getDns2();
		}
		DhcpEntryCommand dhcpCommand = new DhcpEntryCommand(nic.getMacAddress(), nic.getIp4Address(), profile.getVirtualMachine().getHostName(), dns, nic.getGateway());
		String errMsg = String.format("Set dhcp entry on external DHCP %1$s failed(ip=%2$s, mac=%3$s, vmname=%4$s)",
				h.getPrivateIpAddress(), nic.getIp4Address(), nic.getMacAddress(), profile.getVirtualMachine().getHostName());
		//prepareBareMetalDhcpEntry(nic, dhcpCommand);
		try {
			Answer ans = _agentMgr.send(h.getId(), dhcpCommand);
			if (ans.getResult()) {
				s_logger.debug(String.format("Set dhcp entry on external DHCP %1$s successfully(ip=%2$s, mac=%3$s, vmname=%4$s)",
						h.getPrivateIpAddress(), nic.getIp4Address(), nic.getMacAddress(), profile.getVirtualMachine().getHostName()));
				return true;
			} else {
				s_logger.debug(errMsg + " " + ans.getDetails());
				throw new ResourceUnavailableException(errMsg, DataCenter.class, zoneId);
			}
		} catch (Exception e) {
			s_logger.debug(errMsg, e);
			throw new ResourceUnavailableException(errMsg + e.getMessage(), DataCenter.class, zoneId);
		}
	}

	@Override
    public HostVO createHostVOForConnectedAgent(HostVO host, StartupCommand[] cmd) {
	    // TODO Auto-generated method stub
	    return null;
    }

	@Override
    public HostVO createHostVOForDirectConnectAgent(HostVO host, StartupCommand[] startup, ServerResource resource, Map<String, String> details,
            List<String> hostTags) {
        if (!(startup[0] instanceof StartupExternalDhcpCommand)) {
            return null;
        }
        
        host.setType(Host.Type.ExternalDhcp);
        return host;
    }

	@Override
    public DeleteHostAnswer deleteHost(HostVO host, boolean isForced, boolean isForceDeleteStorage) throws UnableDeleteHostException {
	    // TODO Auto-generated method stub
	    return null;
    }
}
