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

package com.cloud.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.api.ApiConstants;
import com.cloud.api.IdentityService;
import com.cloud.api.PlugService;
import com.cloud.api.commands.AddNetworkDeviceCmd;
import com.cloud.api.commands.DeleteNetworkDeviceCmd;
import com.cloud.api.commands.ListNetworkDeviceCmd;
import com.cloud.baremetal.ExternalDhcpManager;
import com.cloud.baremetal.PxeServerManager;
import com.cloud.baremetal.PxeServerProfile;
import com.cloud.baremetal.PxeServerManager.PxeServerType;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.Host.Type;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostDetailsDao;
import com.cloud.network.dao.ExternalFirewallDeviceDao;
import com.cloud.network.dao.ExternalLoadBalancerDeviceDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.InlineLoadBalancerNicMapDao;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkExternalFirewallDao;
import com.cloud.network.dao.NetworkExternalLoadBalancerDao;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkServiceProviderDao;
import com.cloud.network.dao.VpnUserDao;
import com.cloud.network.element.F5ExternalLoadBalancerElementService;
import com.cloud.network.element.JuniperSRXFirewallElementService;
import com.cloud.network.element.NetscalerLoadBalancerElementService;
import com.cloud.network.resource.F5BigIpResource;
import com.cloud.network.resource.JuniperSrxResource;
import com.cloud.network.resource.NetscalerResource;
import com.cloud.network.rules.dao.PortForwardingRulesDao;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.resource.ServerResource;
import com.cloud.server.ManagementServer;
import com.cloud.server.api.response.NetworkDeviceResponse;
import com.cloud.server.api.response.NwDeviceDhcpResponse;
import com.cloud.server.api.response.PxePingResponse;
import com.cloud.user.AccountManager;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserStatisticsDao;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicDao;

@Local(value = {ExternalNetworkDeviceManager.class})
public class ExternalNetworkDeviceManagerImpl implements ExternalNetworkDeviceManager {

    @Inject ExternalDhcpManager _dhcpMgr;
    @Inject PxeServerManager _pxeMgr;
    @Inject AgentManager _agentMgr;
    @Inject NetworkManager _networkMgr;
    @Inject HostDao _hostDao;
    @Inject DataCenterDao _dcDao;
    @Inject AccountDao _accountDao;
    @Inject DomainRouterDao _routerDao;
    @Inject IPAddressDao _ipAddressDao;
    @Inject VlanDao _vlanDao;
    @Inject UserStatisticsDao _userStatsDao;
    @Inject NetworkDao _networkDao;
    @Inject PortForwardingRulesDao _portForwardingRulesDao;
    @Inject LoadBalancerDao _loadBalancerDao;
    @Inject ConfigurationDao _configDao;
    @Inject NetworkOfferingDao _networkOfferingDao;
    @Inject NicDao _nicDao;
    @Inject VpnUserDao _vpnUsersDao;
    @Inject InlineLoadBalancerNicMapDao _inlineLoadBalancerNicMapDao;
    @Inject AccountManager _accountMgr;
    @Inject PhysicalNetworkDao _physicalNetworkDao;
    @Inject PhysicalNetworkServiceProviderDao _physicalNetworkServiceProviderDao;
    @Inject ExternalLoadBalancerDeviceDao _externalLoadBalancerDeviceDao;
    @Inject ExternalFirewallDeviceDao _externalFirewallDeviceDao;
    @Inject NetworkExternalLoadBalancerDao _networkExternalLBDao;
    @Inject NetworkExternalFirewallDao _networkExternalFirewallDao;

    @PlugService NetscalerLoadBalancerElementService _netsclarLbService;
    @PlugService F5ExternalLoadBalancerElementService _f5LbElementService;
    @PlugService JuniperSRXFirewallElementService _srxElementService;

    ScheduledExecutorService _executor;
    int _externalNetworkStatsInterval;
    private final static IdentityService _identityService = (IdentityService)ComponentLocator.getLocator(ManagementServer.Name).getManager(IdentityService.class); 
    
    private static final org.apache.log4j.Logger s_logger = Logger.getLogger(ExternalNetworkDeviceManagerImpl.class);
    protected String _name;
    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
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
    
    @Override
    public Host addNetworkDevice(AddNetworkDeviceCmd cmd) {
        Map paramList = cmd.getParamList();
        if (paramList == null) {
            throw new CloudRuntimeException("Parameter list is null");
        }
    
        Collection paramsCollection = paramList.values();
        HashMap params = (HashMap) (paramsCollection.toArray())[0];
        if (cmd.getDeviceType().equalsIgnoreCase(NetworkDevice.ExternalDhcp.getName())) {
            //Long zoneId = _identityService.getIdentityId("data_center", (String) params.get(ApiConstants.ZONE_ID));
            //Long podId = _identityService.getIdentityId("host_pod_ref", (String)params.get(ApiConstants.POD_ID));
        	Long zoneId = Long.valueOf((String) params.get(ApiConstants.ZONE_ID));
        	Long podId = Long.valueOf((String)params.get(ApiConstants.POD_ID));
            String type = (String) params.get(ApiConstants.DHCP_SERVER_TYPE);
            String url = (String) params.get(ApiConstants.URL);
            String username = (String) params.get(ApiConstants.USERNAME);
            String password = (String) params.get(ApiConstants.PASSWORD);

            return _dhcpMgr.addDhcpServer(zoneId, podId, type, url, username, password);
        } else if (cmd.getDeviceType().equalsIgnoreCase(NetworkDevice.PxeServer.getName())) {
            Long zoneId = Long.parseLong((String) params.get(ApiConstants.ZONE_ID));
            Long podId = Long.parseLong((String)params.get(ApiConstants.POD_ID));
            //Long zoneId = _identityService.getIdentityId("data_center", (String) params.get(ApiConstants.ZONE_ID));
            //Long podId = _identityService.getIdentityId("host_pod_ref", (String)params.get(ApiConstants.POD_ID));
            String type = (String) params.get(ApiConstants.PXE_SERVER_TYPE);
            String url = (String) params.get(ApiConstants.URL);
            String username = (String) params.get(ApiConstants.USERNAME);
            String password = (String) params.get(ApiConstants.PASSWORD);
            String pingStorageServerIp = (String) params.get(ApiConstants.PING_STORAGE_SERVER_IP);
            String pingDir = (String) params.get(ApiConstants.PING_DIR);
            String tftpDir = (String) params.get(ApiConstants.TFTP_DIR);
            String pingCifsUsername = (String) params.get(ApiConstants.PING_CIFS_USERNAME);
            String pingCifsPassword = (String) params.get(ApiConstants.PING_CIFS_PASSWORD);
            PxeServerProfile profile = new PxeServerProfile(zoneId, podId, url, username, password, type, pingStorageServerIp, pingDir, tftpDir,
                    pingCifsUsername, pingCifsPassword);
            return _pxeMgr.addPxeServer(profile);
        } else if (cmd.getDeviceType().equalsIgnoreCase(NetworkDevice.JuniperSRXFirewall.getName())) {
            Long physicalNetworkId = (params.get(ApiConstants.PHYSICAL_NETWORK_ID)==null)?Long.parseLong((String)params.get(ApiConstants.PHYSICAL_NETWORK_ID)):null;
            String url = (String) params.get(ApiConstants.URL);
            String username = (String) params.get(ApiConstants.USERNAME);
            String password = (String) params.get(ApiConstants.PASSWORD);
            ExternalFirewallDeviceManager fwDeviceManager = (ExternalFirewallDeviceManager) _srxElementService;
            ExternalFirewallDeviceVO fwDeviceVO = fwDeviceManager.addExternalFirewall(physicalNetworkId, url, username, password, NetworkDevice.JuniperSRXFirewall.getName(),new JuniperSrxResource());
            if (fwDeviceVO != null) {
                return _hostDao.findById(fwDeviceVO.getHostId());
            } else {
                throw new CloudRuntimeException("Failed to add SRX firewall device due to internal error");
            }
        } else if (cmd.getDeviceType().equalsIgnoreCase(NetworkDevice.NetscalerMPXLoadBalancer.getName()) ||
                cmd.getDeviceType().equalsIgnoreCase(NetworkDevice.NetscalerVPXLoadBalancer.getName()) ||
                cmd.getDeviceType().equalsIgnoreCase(NetworkDevice.NetscalerSDXLoadBalancer.getName())) {
            Long physicalNetworkId = (params.get(ApiConstants.PHYSICAL_NETWORK_ID)==null)?Long.parseLong((String)params.get(ApiConstants.PHYSICAL_NETWORK_ID)):null;
            String url = (String) params.get(ApiConstants.URL);
            String username = (String) params.get(ApiConstants.USERNAME);
            String password = (String) params.get(ApiConstants.PASSWORD);
            ExternalLoadBalancerDeviceManager lbDeviceMgr = (ExternalLoadBalancerDeviceManager) _netsclarLbService;
            ExternalLoadBalancerDeviceVO lbDeviceVO = lbDeviceMgr.addExternalLoadBalancer(physicalNetworkId, 
                    url, username, password, cmd.getDeviceType(), (ServerResource) new NetscalerResource());
            if (lbDeviceVO != null) {
                return _hostDao.findById(lbDeviceVO.getHostId());
            } else {
                throw new CloudRuntimeException("Failed to add Netscaler load balancer device due to internal error");
            }
        } else if (cmd.getDeviceType().equalsIgnoreCase(NetworkDevice.F5BigIpLoadBalancer.getName())) {
            Long physicalNetworkId = (params.get(ApiConstants.PHYSICAL_NETWORK_ID)==null)?Long.parseLong((String)params.get(ApiConstants.PHYSICAL_NETWORK_ID)):null;
            String url = (String) params.get(ApiConstants.URL);
            String username = (String) params.get(ApiConstants.USERNAME);
            String password = (String) params.get(ApiConstants.PASSWORD);
            ExternalLoadBalancerDeviceManager lbDeviceMgr = (ExternalLoadBalancerDeviceManager) _f5LbElementService;
            ExternalLoadBalancerDeviceVO lbDeviceVO =  lbDeviceMgr.addExternalLoadBalancer(physicalNetworkId, url, username, password,
                    cmd.getDeviceType(), (ServerResource) new F5BigIpResource());
            if (lbDeviceVO != null) {
                return _hostDao.findById(lbDeviceVO.getHostId());
            } else {
                throw new CloudRuntimeException("Failed to add Netscaler load balancer device due to internal error");
            }
        } else {
            throw new CloudRuntimeException("Unsupported network device type:" + cmd.getDeviceType());
        }
    }

    @Override
    public NetworkDeviceResponse getApiResponse(Host device) {
        NetworkDeviceResponse response;
        HostVO host = (HostVO)device;
        _hostDao.loadDetails(host);
        if (host.getType() == Host.Type.ExternalDhcp) {
            NwDeviceDhcpResponse r = new NwDeviceDhcpResponse();
            r.setZoneId(host.getDataCenterId());
            r.setPodId(host.getPodId());
            r.setUrl(host.getPrivateIpAddress());
            r.setType(host.getDetail("type"));
            response = r;
        } else if (host.getType() == Host.Type.PxeServer) {
            String pxeType = host.getDetail("type");
            if (pxeType.equalsIgnoreCase(PxeServerType.PING.getName())) {
                PxePingResponse r = new PxePingResponse();
                r.setZoneId(host.getDataCenterId());
                r.setPodId(host.getPodId());
                r.setUrl(host.getPrivateIpAddress());
                r.setType(pxeType);
                r.setStorageServerIp(host.getDetail("storageServer"));
                r.setPingDir(host.getDetail("pingDir"));
                r.setTftpDir(host.getDetail("tftpDir"));
                response = r;
            } else {
                throw new CloudRuntimeException("Unsupported PXE server type:" + pxeType);
            }
        } else if (host.getType() == Host.Type.ExternalLoadBalancer) {
            ExternalLoadBalancerDeviceManager lbDeviceMgr = (ExternalLoadBalancerDeviceManager) _f5LbElementService;
            response = _f5LbElementService.createExternalLoadBalancerResponse(host);
        } else if (host.getType() == Host.Type.ExternalFirewall) {
            response = _srxElementService.createExternalFirewallResponse(host);
        } else {
            throw new CloudRuntimeException("Unsupported network device type:" + host.getType());
        }
        
        response.setId(device.getId());
        return response;
    }

    private List<Host> listNetworkDevice(Long zoneId, Long physicalNetworkId, Long podId, Host.Type type) {
//        List<Host> res = new ArrayList<Host>();
//        if (podId != null) {
//            List<HostVO> devs = _hostDao.listBy(type, null, podId, zoneId);
//            if (devs.size() == 1) {
//                res.add(devs.get(0));
//            } else {
//                s_logger.debug("List " + type + ": " + devs.size() + " found");
//            }
//        } else {
//            List<HostVO> devs = _hostDao.listBy(type, zoneId);
//            res.addAll(devs);
 //       }
        
 //       return res;
        return null;
    }
    
    @Override
    public List<Host> listNetworkDevice(ListNetworkDeviceCmd cmd) {
        Map paramList = cmd.getParamList();
        if (paramList == null) {
            throw new CloudRuntimeException("Parameter list is null");
        }
        
        List<Host> res;
        Collection paramsCollection = paramList.values();
        HashMap params = (HashMap) (paramsCollection.toArray())[0];
        if (NetworkDevice.ExternalDhcp.getName().equalsIgnoreCase(cmd.getDeviceType())) {
            Long zoneId = Long.parseLong((String) params.get(ApiConstants.ZONE_ID));
            Long podId = Long.parseLong((String)params.get(ApiConstants.POD_ID));
            res = listNetworkDevice(zoneId, null, podId, Host.Type.ExternalDhcp);
        } else if (NetworkDevice.PxeServer.getName().equalsIgnoreCase(cmd.getDeviceType())) {
            Long zoneId = Long.parseLong((String) params.get(ApiConstants.ZONE_ID));
            Long podId = Long.parseLong((String)params.get(ApiConstants.POD_ID));
            res = listNetworkDevice(zoneId, null, podId, Host.Type.PxeServer);
        } else if (cmd.getDeviceType().equalsIgnoreCase(NetworkDevice.NetscalerMPXLoadBalancer.getName()) ||
                cmd.getDeviceType().equalsIgnoreCase(NetworkDevice.NetscalerVPXLoadBalancer.getName()) ||
                cmd.getDeviceType().equalsIgnoreCase(NetworkDevice.NetscalerSDXLoadBalancer.getName()) ||
                cmd.getDeviceType().equalsIgnoreCase(NetworkDevice.F5BigIpLoadBalancer.getName())) {
            Long zoneId = Long.parseLong((String) params.get(ApiConstants.ZONE_ID));
            Long physicalNetworkId = (params.get(ApiConstants.PHYSICAL_NETWORK_ID)==null)?Long.parseLong((String)params.get(ApiConstants.PHYSICAL_NETWORK_ID)):null;
            ExternalLoadBalancerDeviceManager lbDeviceMgr = (ExternalLoadBalancerDeviceManager) _f5LbElementService;
            return lbDeviceMgr.listExternalLoadBalancers(physicalNetworkId, cmd.getDeviceType());
        } else if (NetworkDevice.JuniperSRXFirewall.getName().equalsIgnoreCase(cmd.getDeviceType())) {
            Long zoneId = Long.parseLong((String) params.get(ApiConstants.ZONE_ID));
            Long physicalNetworkId = (params.get(ApiConstants.PHYSICAL_NETWORK_ID)==null)?Long.parseLong((String)params.get(ApiConstants.PHYSICAL_NETWORK_ID)):null;
            ExternalFirewallDeviceManager fwDeviceManager = (ExternalFirewallDeviceManager) _srxElementService;
            return fwDeviceManager.listExternalFirewalls(physicalNetworkId, NetworkDevice.JuniperSRXFirewall.getName());
        } else if (cmd.getDeviceType() == null){
            Long zoneId = Long.parseLong((String) params.get(ApiConstants.ZONE_ID));
            Long podId = Long.parseLong((String)params.get(ApiConstants.POD_ID));
            Long physicalNetworkId = (params.get(ApiConstants.PHYSICAL_NETWORK_ID)==null)?Long.parseLong((String)params.get(ApiConstants.PHYSICAL_NETWORK_ID)):null;            
            List<Host> res1 = listNetworkDevice(zoneId, physicalNetworkId, podId, Host.Type.PxeServer);
            List<Host> res2 = listNetworkDevice(zoneId, physicalNetworkId, podId, Host.Type.ExternalDhcp);
            List<Host> res3 = listNetworkDevice(zoneId, physicalNetworkId, podId, Host.Type.ExternalLoadBalancer);
            List<Host> res4 = listNetworkDevice(zoneId, physicalNetworkId, podId, Host.Type.ExternalFirewall);
            List<Host> deviceAll = new ArrayList<Host>();
            deviceAll.addAll(res1);
            deviceAll.addAll(res2);
            deviceAll.addAll(res3);
            deviceAll.addAll(res4);
            res = deviceAll;
        } else {
            throw new CloudRuntimeException("Unknown network device type:" + cmd.getDeviceType());
        }
        
        return res;
    }

    @Override
    public boolean deleteNetworkDevice(DeleteNetworkDeviceCmd cmd) {
       HostVO device = _hostDao.findById(cmd.getId());
       if (device.getType() == Type.ExternalLoadBalancer) {
           ExternalLoadBalancerDeviceManager lbDeviceMgr = (ExternalLoadBalancerDeviceManager) _f5LbElementService;
           return lbDeviceMgr.deleteExternalLoadBalancer(cmd.getId());
       } else if (device.getType() == Type.ExternalLoadBalancer) {
           ExternalFirewallDeviceManager fwDeviceManager = (ExternalFirewallDeviceManager) _srxElementService;
           return fwDeviceManager.deleteExternalFirewall(cmd.getId());
       }
       return true;
    }
}
