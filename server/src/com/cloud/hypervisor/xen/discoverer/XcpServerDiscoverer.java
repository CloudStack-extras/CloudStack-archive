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
package com.cloud.hypervisor.xen.discoverer;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.ejb.Local;
import javax.naming.ConfigurationException;
import javax.persistence.EntityExistsException;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;

import com.cloud.agent.AgentManager;
import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.PoolEjectCommand;
import com.cloud.agent.api.SetupAnswer;
import com.cloud.agent.api.SetupCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.alert.AlertManager;
import com.cloud.configuration.Config;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.ConnectionException;
import com.cloud.exception.DiscoveredWithErrorException;
import com.cloud.exception.DiscoveryException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.host.HostEnvironment;
import com.cloud.host.HostInfo;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.xen.resource.CitrixResourceBase;
import com.cloud.hypervisor.xen.resource.XcpServerResource;
import com.cloud.hypervisor.xen.resource.XenServer56FP1Resource;
import com.cloud.hypervisor.xen.resource.XenServer56Resource;
import com.cloud.hypervisor.xen.resource.XenServer56SP2Resource;
import com.cloud.hypervisor.xen.resource.XenServer600Resource;
import com.cloud.hypervisor.xen.resource.XenServerConnectionPool;
import com.cloud.resource.Discoverer;
import com.cloud.resource.DiscovererBase;
import com.cloud.resource.ResourceManager;
import com.cloud.resource.ResourceStateAdapter;
import com.cloud.resource.ServerResource;
import com.cloud.resource.UnableDeleteHostException;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateHostDao;
import com.cloud.user.Account;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.SearchCriteria2;
import com.cloud.utils.db.SearchCriteriaService;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.HypervisorVersionChangedException;
import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Pool;
import com.xensource.xenapi.Session;
import com.xensource.xenapi.Types.SessionAuthenticationFailed;
import com.xensource.xenapi.Types.XenAPIException;

@Local(value=Discoverer.class)
public class XcpServerDiscoverer extends DiscovererBase implements Discoverer, Listener, ResourceStateAdapter {
    private static final Logger s_logger = Logger.getLogger(XcpServerDiscoverer.class);
    protected String _publicNic;
    protected String _privateNic;
    protected String _storageNic1;
    protected String _storageNic2;
    protected int _wait;
    protected XenServerConnectionPool _connPool;
    protected boolean _checkHvm;
    protected String _guestNic;
    protected boolean _setupMultipath;
    protected String _instance;

    @Inject protected AlertManager _alertMgr;
    @Inject protected AgentManager _agentMgr;
    @Inject VMTemplateDao _tmpltDao;
    @Inject VMTemplateHostDao _vmTemplateHostDao;
    @Inject ResourceManager _resourceMgr;
    @Inject HostPodDao _podDao;
    @Inject DataCenterDao _dcDao;
    
    protected XcpServerDiscoverer() {
    }
    
    void setClusterGuid(ClusterVO cluster, String guid) {
        cluster.setGuid(guid);
        try {
            _clusterDao.update(cluster.getId(), cluster);
        } catch (EntityExistsException e) {
            SearchCriteriaService<ClusterVO, ClusterVO> sc = SearchCriteria2.create(ClusterVO.class);
            sc.addAnd(sc.getEntity().getGuid(), Op.EQ, guid);
            List<ClusterVO> clusters = sc.list();
            ClusterVO clu = clusters.get(0);
            List<HostVO> clusterHosts = _resourceMgr.listAllHostsInCluster(clu.getId());
            if (clusterHosts == null || clusterHosts.size() == 0) {
                clu.setGuid(null);
                _clusterDao.update(clu.getId(), clu);
                _clusterDao.update(cluster.getId(), cluster);
                return;
            }
            throw e;
        }
    }

    @Override
    public Map<? extends ServerResource, Map<String, String>> find(long dcId, Long podId, Long clusterId, URI url, String username, String password, List<String> hostTags) throws DiscoveryException {
        Map<CitrixResourceBase, Map<String, String>> resources = new HashMap<CitrixResourceBase, Map<String, String>>();
        Connection conn = null;
        if (!url.getScheme().equals("http")) {
            String msg = "urlString is not http so we're not taking care of the discovery for this: " + url;
            s_logger.debug(msg);
            return null;
        }
        if (clusterId == null) {
            String msg = "must specify cluster Id when add host";
            s_logger.debug(msg);
            throw new RuntimeException(msg);
        } 
        
		if (podId == null) {
			String msg = "must specify pod Id when add host";
			s_logger.debug(msg);
			throw new RuntimeException(msg);
		}
		
        ClusterVO cluster = _clusterDao.findById(clusterId);
        if(cluster == null || cluster.getHypervisorType() != HypervisorType.XenServer) {
        	if(s_logger.isInfoEnabled())
                s_logger.info("invalid cluster id or cluster is not for XenServer hypervisors");
    		return null;
        }
		
        try {
            List<HostVO> eHosts = _resourceMgr.listAllHostsInCluster(clusterId);
            if( eHosts.size() > 0 ) {
            	HostVO eHost = eHosts.get(0);
            	_hostDao.loadDetails(eHost);
            }         
            String hostname = url.getHost();
            InetAddress ia = InetAddress.getByName(hostname);
            String hostIp = ia.getHostAddress(); 
            Queue<String> pass=new LinkedList<String>();
            pass.add(password);
            String masterIp = _connPool.getMasterIp(hostIp, username, pass);          
            conn = _connPool.masterConnect(masterIp, username, pass);
            if (conn == null) {
                String msg = "Unable to get a connection to " + url;
                s_logger.debug(msg);
                throw new DiscoveryException(msg);
            }
           
            Set<Pool> pools = Pool.getAll(conn);
            Pool pool = pools.iterator().next();
            Pool.Record pr = pool.getRecord(conn);           
            String poolUuid = pr.uuid;
            Map<Host, Host.Record> hosts = Host.getAllRecords(conn);

            /*set cluster hypervisor type to xenserver*/
            ClusterVO clu = _clusterDao.findById(clusterId);
            if ( clu.getGuid()== null ) {
                setClusterGuid(clu, poolUuid);
            } else {
                List<HostVO> clusterHosts = _resourceMgr.listAllHostsInCluster(clusterId);
                if( clusterHosts != null && clusterHosts.size() > 0) {
                    if (!clu.getGuid().equals(poolUuid)) {
                        if (hosts.size() == 1) {
                            if (!addHostsToPool(conn, hostIp, clusterId)) {
                                String msg = "Unable to add host(" + hostIp + ") to cluster " + clusterId;
                                s_logger.warn(msg);
                                throw new DiscoveryException(msg);
                            }
                        } else {
                            String msg = "Host (" + hostIp + ") is already in pool(" + poolUuid + "), can to join pool(" + clu.getGuid() + ")";
                            s_logger.warn(msg);
                            throw new DiscoveryException(msg);
                        }
                    }
                } else {
                    setClusterGuid(clu, poolUuid);
                }
            }
            // can not use this conn after this point, because this host may join a pool, this conn is retired
            if (conn != null) {
                try{
                    Session.logout(conn);
                } catch (Exception e ) {
                }
                conn.dispose();
                conn = null;
            }
            
            poolUuid = clu.getGuid();
            _clusterDao.update(clusterId, clu);
            
                    
            if (_checkHvm) {
                for (Map.Entry<Host, Host.Record> entry : hosts.entrySet()) {
                    Host.Record record = entry.getValue();
                    
                    boolean support_hvm = false;
                    for ( String capability : record.capabilities ) {
                        if(capability.contains("hvm")) {
                           support_hvm = true;
                           break;
                        }
                    } 
                    if( !support_hvm ) {
                        String msg = "Unable to add host " + record.address + " because it doesn't support hvm";
                        _alertMgr.sendAlert(AlertManager.ALERT_TYPE_HOST, dcId, podId, msg, msg);
                        s_logger.debug(msg);
                        throw new RuntimeException(msg);
                    }
                }
            }

            for (Map.Entry<Host, Host.Record> entry : hosts.entrySet()) {
                Host.Record record = entry.getValue();
                String hostAddr = record.address;
                
                String prodVersion = record.softwareVersion.get("product_version");
                String xenVersion = record.softwareVersion.get("xen");
                String hostOS = record.softwareVersion.get("product_brand");
                String hostOSVer = prodVersion;
                String hostKernelVer = record.softwareVersion.get("linux");

                if (_resourceMgr.findHostByGuid(record.uuid) != null) {
                    s_logger.debug("Skipping " + record.address + " because " + record.uuid + " is already in the database.");
                    continue;
                }                

                CitrixResourceBase resource = createServerResource(dcId, podId, record);
                s_logger.info("Found host " + record.hostname + " ip=" + record.address + " product version=" + prodVersion);
                            
                Map<String, String> details = new HashMap<String, String>();
                Map<String, Object> params = new HashMap<String, Object>();
                details.put("url", hostAddr);
                details.put("username", username);
                params.put("username", username);
                details.put("password", password);
                params.put("password", password);
                params.put("zone", Long.toString(dcId));
                params.put("guid", record.uuid);
                params.put("pod", podId.toString());
                params.put("cluster", clusterId.toString());
                params.put("pool", poolUuid);
                params.put("ipaddress", record.address);
                
                details.put(HostInfo.HOST_OS, hostOS);
                details.put(HostInfo.HOST_OS_VERSION, hostOSVer);
                details.put(HostInfo.HOST_OS_KERNEL_VERSION, hostKernelVer);
                details.put(HostInfo.HYPERVISOR_VERSION, xenVersion);
                
                String privateNetworkLabel = _networkMgr.getDefaultManagementTrafficLabel(dcId, HypervisorType.XenServer);
                String storageNetworkLabel = _networkMgr.getDefaultStorageTrafficLabel(dcId, HypervisorType.XenServer);
                
                if (!params.containsKey("private.network.device") && privateNetworkLabel != null) {
                    params.put("private.network.device", privateNetworkLabel);
                    details.put("private.network.device", privateNetworkLabel);
                }
                
                if (!params.containsKey("storage.network.device1") && storageNetworkLabel != null) {
                    params.put("storage.network.device1", storageNetworkLabel);
                    details.put("storage.network.device1", storageNetworkLabel);
                }

                
                params.put("wait", Integer.toString(_wait));
                details.put("wait", Integer.toString(_wait));
                params.put("migratewait", _configDao.getValue(Config.MigrateWait.toString()));
                params.put(Config.InstanceName.toString().toLowerCase(), _instance);
                details.put(Config.InstanceName.toString().toLowerCase(), _instance);
                try {
                    resource.configure("Xen Server", params);
                } catch (ConfigurationException e) {
                    _alertMgr.sendAlert(AlertManager.ALERT_TYPE_HOST, dcId, podId, "Unable to add " + record.address, "Error is " + e.getMessage());
                    s_logger.warn("Unable to instantiate " + record.address, e);
                    continue;
                }
                resource.start();
                resources.put(resource, details);
            }                        
        } catch (SessionAuthenticationFailed e) {       
            throw new DiscoveredWithErrorException("Authetication error");
        } catch (XenAPIException e) {
            s_logger.warn("XenAPI exception", e);
            return null;
        } catch (XmlRpcException e) {
            s_logger.warn("Xml Rpc Exception", e);
            return null;
        } catch (UnknownHostException e) {
            s_logger.warn("Unable to resolve the host name", e);
            return null;
        } catch (Exception e) {
        	s_logger.debug("other exceptions: " + e.toString(), e);
        	return null;
        }
        return resources;
    }
    
    String getPoolUuid(Connection conn) throws XenAPIException, XmlRpcException {
        Map<Pool, Pool.Record> pools = Pool.getAllRecords(conn);
        assert pools.size() == 1 : "Pools size is " + pools.size();
        return pools.values().iterator().next().uuid;
    }
    
    protected void addSamePool(Connection conn, Map<CitrixResourceBase, Map<String, String>> resources) throws XenAPIException, XmlRpcException {
        Map<Pool, Pool.Record> hps = Pool.getAllRecords(conn);
        assert (hps.size() == 1) : "How can it be more than one but it's actually " + hps.size();
        
        // This is the pool.
        String poolUuid = hps.values().iterator().next().uuid;
        
        for (Map<String, String> details : resources.values()) {
            details.put("pool", poolUuid);
        }
    }
    
    protected boolean addHostsToPool(Connection conn, String hostIp, Long clusterId) throws XenAPIException, XmlRpcException, DiscoveryException {
        
        List<HostVO> hosts;
        hosts = _resourceMgr.listAllHostsInCluster(clusterId);

        String masterIp = null;
        String username = null;
        String password = null;
        Queue<String> pass=new LinkedList<String>();
        for (HostVO host : hosts) {
            _hostDao.loadDetails(host);
            username = host.getDetail("username");
            password = host.getDetail("password");
            pass.add(password);
            String address = host.getPrivateIpAddress();
            Connection hostConn = _connPool.slaveConnect(address, username, pass);
            if (hostConn == null) {
                continue;
            }
            try {
                Set<Pool> pools = Pool.getAll(hostConn);
                Pool pool = pools.iterator().next();
                masterIp = pool.getMaster(hostConn).getAddress(hostConn);
                break;

            } catch (Exception e ) {
                s_logger.warn("Can not get master ip address from host " + address);
            } finally {
                try{
                    Session.localLogout(hostConn);
                } catch (Exception e ) {
                }
                hostConn.dispose();
                hostConn = null;
            }
        }
        
        if (masterIp == null) {
            s_logger.warn("Unable to reach the pool master of the existing cluster");
            throw new CloudRuntimeException("Unable to reach the pool master of the existing cluster");
        }
        
        if( !_connPool.joinPool(conn, hostIp, masterIp, username, pass) ){
            s_logger.warn("Unable to join the pool");
            throw new DiscoveryException("Unable to join the pool");
        }   
        return true;
    }
    
    protected CitrixResourceBase createServerResource(long dcId, Long podId, Host.Record record) {
        String prodBrand = record.softwareVersion.get("product_brand").trim();
        String prodVersion = record.softwareVersion.get("product_version").trim();
        
        if(prodBrand.equals("XCP") && (prodVersion.equals("1.0.0") ||  prodVersion.equals("1.1.0") || prodVersion.equals("5.6.100"))) 
        	return new XcpServerResource();

        if(prodBrand.equals("XenServer") && prodVersion.equals("5.6.0")) 
        	return new XenServer56Resource();
        
        if(prodBrand.equals("XenServer") && prodVersion.startsWith("6.0")) 
            return new XenServer600Resource();
        
        if(prodBrand.equals("XenServer") && prodVersion.equals("5.6.100"))  {
            String prodVersionTextShort = record.softwareVersion.get("product_version_text_short").trim();
            if("5.6 SP2".equals(prodVersionTextShort)) {
                return new XenServer56SP2Resource();
            } else if("5.6 FP1".equals(prodVersionTextShort)) {
                return new XenServer56FP1Resource();
            }
        }
        
        String msg = "Only support XCP 1.0.0, XCP 1.1.0, XenServer 5.6,  XenServer 5.6 FP1, XenServer 5.6 SP2, Xenserver 6.0 but this one is " + prodBrand + " " + prodVersion;
        _alertMgr.sendAlert(AlertManager.ALERT_TYPE_HOST, dcId, podId, msg, msg);
        s_logger.debug(msg);
        throw new RuntimeException(msg);
    }
    
    protected void serverConfig() {      
        String value = _params.get(Config.XenSetupMultipath.key());
        _setupMultipath = Boolean.parseBoolean(value);
    }
    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        serverConfig();
        
        _publicNic = _params.get(Config.XenPublicNetwork.key());
        _privateNic = _params.get(Config.XenPrivateNetwork.key());
        
        _storageNic1 = _params.get(Config.XenStorageNetwork1.key());
        _storageNic2 = _params.get(Config.XenStorageNetwork2.key());
        
        _guestNic = _params.get(Config.XenGuestNetwork.key());
               
        String value = _params.get(Config.XapiWait.toString());
        _wait = NumbersUtil.parseInt(value, Integer.parseInt(Config.XapiWait.getDefaultValue()));
        
        _instance = _params.get(Config.InstanceName.key());
        
        value = _params.get(Config.XenSetupMultipath.key());
        Boolean.parseBoolean(value);

        value = _params.get("xen.check.hvm");
        _checkHvm = value == null ? true : Boolean.parseBoolean(value);
        
        _connPool = XenServerConnectionPool.getInstance();
        
        _agentMgr.registerForHostEvents(this, true, false, true);
        
        createXsToolsISO();
        _resourceMgr.registerResourceStateAdapter(this.getClass().getSimpleName(), this);
        return true;
    }
    
    @Override
	public boolean matchHypervisor(String hypervisor) {
    	if(hypervisor == null)
    		return true;
    	return Hypervisor.HypervisorType.XenServer.toString().equalsIgnoreCase(hypervisor);
    }
    
    @Override
	public Hypervisor.HypervisorType getHypervisorType() {
    	return Hypervisor.HypervisorType.XenServer;
    }
    
    @Override
    public void postDiscovery(List<HostVO> hosts, long msId)  throws DiscoveryException{
        //do nothing
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public boolean isRecurring() {
        return false;
    }

    @Override
    public boolean processAnswers(long agentId, long seq, Answer[] answers) {
        return false;
    }

    @Override
    public boolean processCommands(long agentId, long seq, Command[] commands) {
        return false;
    }
  
    private void createXsToolsISO() {
        String isoName = "xs-tools.iso";
        VMTemplateVO tmplt = _tmpltDao.findByTemplateName(isoName);
        Long id;
        if (tmplt == null) {
            id = _tmpltDao.getNextInSequence(Long.class, "id");
            VMTemplateVO template = new VMTemplateVO(id, isoName, isoName, ImageFormat.ISO, true, true,
                    TemplateType.PERHOST, null, null, true, 64,
                    Account.ACCOUNT_ID_SYSTEM, null, "xen-pv-drv-iso", false, 1, false, HypervisorType.XenServer);
            _tmpltDao.persist(template);
        } else {
            id = tmplt.getId();
            tmplt.setTemplateType(TemplateType.PERHOST);
            tmplt.setUrl(null);
            _tmpltDao.update(id, tmplt);
        }
    }

    @Override
    public void processConnect(HostVO agent, StartupCommand cmd, boolean forRebalance) throws ConnectionException {
        if (!(cmd instanceof StartupRoutingCommand )) {
            return;
        }       
        long agentId = agent.getId();
        
        StartupRoutingCommand startup = (StartupRoutingCommand)cmd;
        if (startup.getHypervisorType() != HypervisorType.XenServer) {
            s_logger.debug("Not XenServer so moving on.");
            return;
        }
        
        HostVO host = _hostDao.findById(agentId);
        
        ClusterVO cluster = _clusterDao.findById(host.getClusterId());
        if ( cluster.getGuid() == null) {
            cluster.setGuid(startup.getPool());
            _clusterDao.update(cluster.getId(), cluster);
        } else if (! cluster.getGuid().equals(startup.getPool()) ) {
            String msg = "pool uuid for cluster " + cluster.getId() + " changed from " + cluster.getGuid() + " to " + cmd.getPod();
            s_logger.warn(msg);
            throw new CloudRuntimeException(msg);
        }
        String resource = null;
        Map<String, String> details = startup.getHostDetails();
        String prodBrand = details.get("product_brand").trim();
        String prodVersion = details.get("product_version").trim();
        
        if(prodBrand.equals("XCP") && (prodVersion.equals("1.0.0") || prodVersion.equals("1.1.0") || prodVersion.equals("5.6.100"))) {
            resource = XcpServerResource.class.getName();
        } else if(prodBrand.equals("XenServer") && prodVersion.equals("5.6.0")) {
            resource = XenServer56Resource.class.getName();
        } else if(prodBrand.equals("XenServer") && prodVersion.startsWith("6.0")) {
            resource = XenServer600Resource.class.getName();
        } else if(prodBrand.equals("XenServer") && prodVersion.equals("5.6.100"))  {
            String prodVersionTextShort = details.get("product_version_text_short").trim();
            if("5.6 SP2".equals(prodVersionTextShort)) {
                resource = XenServer56SP2Resource.class.getName();
            } else if("5.6 FP1".equals(prodVersionTextShort)) {
                resource = XenServer56FP1Resource.class.getName();
            }
        }
        if( resource == null ){
            String msg = "Only support XCP 1.0.0, XCP 1.1.0, XenServer 5.6, 5.6 FP1, 5.6 SP2 and Xenserver 6.0 but this one is " + prodBrand + " " + prodVersion;
            s_logger.debug(msg);
            throw new RuntimeException(msg);
        }
        if (! resource.equals(host.getResource()) ) {
            String msg = "host " + host.getPrivateIpAddress() + " changed from " + host.getResource() + " to " + resource;
            s_logger.debug(msg);
            host.setResource(resource);
            host.setSetup(false);
            _hostDao.update(agentId, host);
            throw new HypervisorVersionChangedException(msg);
        }
        
        
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Setting up host " + agentId);
        }
        HostEnvironment env = new HostEnvironment();
        
        SetupCommand setup = new SetupCommand(env);
        if (_setupMultipath) {
            setup.setMultipathOn();
        }
        if (!host.isSetup()) {
            setup.setNeedSetup(true);
        }
        
        try {
            SetupAnswer answer = (SetupAnswer)_agentMgr.send(agentId, setup);
            if (answer != null && answer.getResult()) {
                host.setSetup(true);
                host.setLastPinged((System.currentTimeMillis()>>10) - 5 * 60 );
                _hostDao.update(host.getId(), host);
                if ( answer.needReconnect() ) {
                    throw new ConnectionException(false, "Reinitialize agent after setup.");
                }
                return;
            } else {
                s_logger.warn("Unable to setup agent " + agentId + " due to " + ((answer != null)?answer.getDetails():"return null"));
            }
        } catch (AgentUnavailableException e) {
            s_logger.warn("Unable to setup agent " + agentId + " because it became unavailable.", e);
        } catch (OperationTimedoutException e) {
            s_logger.warn("Unable to setup agent " + agentId + " because it timed out", e);
        }
        throw new ConnectionException(true, "Reinitialize agent after setup.");
    }

    @Override
    public AgentControlAnswer processControlCommand(long agentId, AgentControlCommand cmd) {
        return null;
    }

    @Override
    public boolean processDisconnect(long agentId, Status state) {
        return false;
    }

    @Override
    public boolean processTimeout(long agentId, long seq) {
        return false;
    }

	@Override
    public HostVO createHostVOForConnectedAgent(HostVO host, StartupCommand[] cmd) {
	    // TODO Auto-generated method stub
	    return null;
    }

	@Override
    public HostVO createHostVOForDirectConnectAgent(HostVO host, StartupCommand[] startup, ServerResource resource, Map<String, String> details,
            List<String> hostTags) {
		StartupCommand firstCmd = startup[0];
		if (!(firstCmd instanceof StartupRoutingCommand)) {
			return null;
		}

		StartupRoutingCommand ssCmd = ((StartupRoutingCommand) firstCmd);
		if (ssCmd.getHypervisorType() != HypervisorType.XenServer) {
			return null;
		}

		HostPodVO pod = _podDao.findById(host.getPodId());
		DataCenterVO dc = _dcDao.findById(host.getDataCenterId());
		s_logger.info("Host: " + host.getName() + " connected with hypervisor type: " + HypervisorType.XenServer + ". Checking CIDR...");
		_resourceMgr.checkCIDR(pod, dc, ssCmd.getPrivateIpAddress(), ssCmd.getPrivateNetmask());
		return _resourceMgr.fillRoutingHostVO(host, ssCmd, HypervisorType.XenServer, details, hostTags);
    }

	@Override
    public DeleteHostAnswer deleteHost(HostVO host, boolean isForced, boolean isForceDeleteStorage) throws UnableDeleteHostException {
		if (host.getType() != com.cloud.host.Host.Type.Routing || host.getHypervisorType() != HypervisorType.XenServer) {
			return null;
		}
		
		_resourceMgr.deleteRoutingHost(host, isForced, isForceDeleteStorage);
		if (host.getClusterId() != null) {
			List<HostVO> hosts = _resourceMgr.listAllUpAndEnabledHosts(com.cloud.host.Host.Type.Routing, host.getClusterId(), host.getPodId(), host.getDataCenterId());
			boolean success = true;
			for (HostVO thost : hosts) {
				if (thost.getId() == host.getId()) {
					continue;
				}
				
				long thostId = thost.getId();
				PoolEjectCommand eject = new PoolEjectCommand(host.getGuid());
				Answer answer = _agentMgr.easySend(thostId, eject);
				if (answer != null && answer.getResult()) {
					s_logger.debug("Eject Host: " + host.getId() + " from " + thostId + " Succeed");
					success = true;
					break;
				} else {
					success = false;
					s_logger.warn("Eject Host: " + host.getId() + " from " + thostId + " failed due to " + (answer != null ? answer.getDetails() : "no answer"));
				}
			}
			if (!success) {
				String msg = "Unable to eject host " + host.getGuid() + " due to there is no host up in this cluster, please execute xe pool-eject host-uuid="
				        + host.getGuid() + "in this host " + host.getPrivateIpAddress();
				s_logger.warn(msg);
				_alertMgr.sendAlert(AlertManager.ALERT_TYPE_HOST, host.getDataCenterId(), host.getPodId(), "Unable to eject host " + host.getGuid(), msg);
			}
		}
		
		return new DeleteHostAnswer(true);
    }
	
    @Override
    public boolean stop() {
    	_resourceMgr.unregisterResourceStateAdapter(this.getClass().getSimpleName());
        return super.stop();
    }
}
