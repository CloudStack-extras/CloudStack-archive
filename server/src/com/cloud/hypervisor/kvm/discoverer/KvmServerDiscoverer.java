package com.cloud.hypervisor.kvm.discoverer;

import java.net.InetAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.exception.DiscoveryException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.kvm.resource.KvmDummyResourceBase;
import com.cloud.resource.Discoverer;
import com.cloud.resource.DiscovererBase;
import com.cloud.resource.ServerResource;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.script.Script;
import com.cloud.utils.ssh.SSHCmdHelper;
import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;

@Local(value=Discoverer.class)
public class KvmServerDiscoverer extends DiscovererBase implements Discoverer,
		Listener {
	 private static final Logger s_logger = Logger.getLogger(KvmServerDiscoverer.class);
	 private String _setupAgentPath;
	 private ConfigurationDao _configDao;
	 private String _hostIp;
	 private int _waitTime = 5; /*wait for 5 minutes*/
	 private String _kvmPrivateNic;
	 private String _kvmPublicNic;
	 @Inject HostDao _hostDao = null;
	 @Inject ClusterDao _clusterDao;
	 
	@Override
	public boolean processAnswers(long agentId, long seq, Answer[] answers) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean processCommands(long agentId, long seq, Command[] commands) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AgentControlAnswer processControlCommand(long agentId,
			AgentControlCommand cmd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processConnect(HostVO host, StartupCommand cmd) {
	}

	@Override
	public boolean processDisconnect(long agentId, Status state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean processTimeout(long agentId, long seq) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<? extends ServerResource, Map<String, String>> find(long dcId,
			Long podId, Long clusterId, URI uri, String username,
			String password) throws DiscoveryException {
		
        ClusterVO cluster = _clusterDao.findById(clusterId);
        if(cluster == null || cluster.getHypervisorType() != HypervisorType.KVM) {
        	if(s_logger.isInfoEnabled())
        		s_logger.info("invalid cluster id or cluster is not for KVM hypervisors"); 
    		return null;
        }
		
		 Map<KvmDummyResourceBase, Map<String, String>> resources = new HashMap<KvmDummyResourceBase, Map<String, String>>();
		 Map<String, String> details = new HashMap<String, String>();
		if (!uri.getScheme().equals("http")) {
            String msg = "urlString is not http so we're not taking care of the discovery for this: " + uri;
            s_logger.debug(msg);
            return null;
		}
		com.trilead.ssh2.Connection sshConnection = null;
		String agentIp = null;
		try {
			
			String hostname = uri.getHost();
			InetAddress ia = InetAddress.getByName(hostname);
			agentIp = ia.getHostAddress();
			String guid = UUID.nameUUIDFromBytes(agentIp.getBytes()).toString();
			String guidWithTail = guid + "-LibvirtComputingResource";/*tail added by agent.java*/
			if (_hostDao.findByGuid(guidWithTail) != null) {
				s_logger.debug("Skipping " + agentIp + " because " + guidWithTail + " is already in the database.");
				return null;
			}       
			
			sshConnection = new com.trilead.ssh2.Connection(agentIp, 22);

			sshConnection.connect(null, 60000, 60000);
			if (!sshConnection.authenticateWithPassword(username, password)) {
				s_logger.debug("Failed to authenticate");
				return null;
			}
			
			if (!SSHCmdHelper.sshExecuteCmd(sshConnection, "lsmod|grep kvm >& /dev/null", 3)) {
				s_logger.debug("It's not a KVM enabled machine");
				return null;
			}
			
			String parameters = " --host=" + _hostIp + " --zone=" + dcId + " --pod=" + podId + " --cluster=" + clusterId + " --guid=" + guid + " -a";
			
			if (_kvmPublicNic != null) {
				parameters += " -P " + _kvmPublicNic;
			}
			
			if (_kvmPrivateNic != null) {
				parameters += " -N " + _kvmPrivateNic;
			}
		
			SSHCmdHelper.sshExecuteCmd(sshConnection, "cloud-setup-agent " + parameters + " >& /dev/null", 3);
			
			KvmDummyResourceBase kvmResource = new KvmDummyResourceBase();
			Map<String, Object> params = new HashMap<String, Object>();
						
			params.put("zone", Long.toString(dcId));
			params.put("pod", Long.toString(podId));
			params.put("cluster",  Long.toString(clusterId));
			params.put("guid", guid); 
			params.put("agentIp", agentIp);
			kvmResource.configure("kvm agent", params);
			resources.put(kvmResource, details);
			
			HostVO connectedHost = waitForHostConnect(dcId, podId, clusterId, guidWithTail);
			if (connectedHost == null)
				return null;
			
			details.put("guid", guidWithTail);
			return resources;
		} catch (Exception e) {
			String msg = " can't setup agent, due to " + e.toString() + " - " + e.getMessage();
			s_logger.warn(msg);
		} finally {
			if (sshConnection != null)
				sshConnection.close();
		}
		
		return null;
	}

	private HostVO waitForHostConnect(long dcId, long podId, long clusterId, String guid) {
		for (int i = 0; i < _waitTime *2; i++) {
			List<HostVO> hosts = _hostDao.listBy(Host.Type.Routing, clusterId, podId, dcId);
			for (HostVO host : hosts) {
				if (host.getGuid().equalsIgnoreCase(guid)) {
					return host;
				}
			}
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				s_logger.debug("Failed to sleep: " + e.toString());
			}
		}
		s_logger.debug("Timeout, to wait for the host connecting to mgt svr, assuming it is failed");
		return null;
	}
	
	@Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
		ComponentLocator locator = ComponentLocator.getCurrentLocator();
        _configDao = locator.getDao(ConfigurationDao.class);
		_setupAgentPath = Script.findScript(getPatchPath(), "setup_agent.sh");
		_kvmPrivateNic = _configDao.getValue(Config.KvmPrivateNetwork.key());
		_kvmPublicNic = _configDao.getValue(Config.KvmPublicNetwork.key());
		
		if (_setupAgentPath == null) {
			throw new ConfigurationException("Can't find setup_agent.sh");
		}
		_hostIp = _configDao.getValue("host");
		if (_hostIp == null) {
			throw new ConfigurationException("Can't get host IP");
		}
		return true;
	}
	
	protected String getPatchPath() {
        return "scripts/vm/hypervisor/kvm/";
    }

	@Override
	public void postDiscovery(List<HostVO> hosts, long msId)
			throws DiscoveryException {
		// TODO Auto-generated method stub
	}
	
	public Hypervisor.HypervisorType getHypervisorType() {
		return Hypervisor.HypervisorType.KVM;
	}
	
    @Override
	public boolean matchHypervisor(String hypervisor) {
    	// for backwards compatibility, if not supplied, always let to try it
    	if(hypervisor == null)
    		return true;
    	
    	return Hypervisor.HypervisorType.KVM.toString().equalsIgnoreCase(hypervisor);
    }
}
