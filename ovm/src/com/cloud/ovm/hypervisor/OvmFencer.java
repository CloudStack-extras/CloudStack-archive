package com.cloud.ovm.hypervisor;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.FenceAnswer;
import com.cloud.agent.api.FenceCommand;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.ha.FenceBuilder;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.utils.component.Inject;
import com.cloud.vm.VMInstanceVO;

@Local(value=FenceBuilder.class)
public class OvmFencer implements FenceBuilder {
	private static final Logger s_logger = Logger.getLogger(OvmFencer.class);
	String _name;
	@Inject HostDao _hostDao;
	@Inject AgentManager _agentMgr;
	
	@Override
	public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
		_name = name;
		return true;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return _name;
	}

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public OvmFencer() {
		super();
	}

	@Override
	public Boolean fenceOff(VMInstanceVO vm, HostVO host) {
		if (host.getHypervisorType() != HypervisorType.Ovm) {
			s_logger.debug("Don't know how to fence non Ovm hosts " + host.getHypervisorType());
			return null;
		}
		
		List<HostVO> hosts = _hostDao.listByCluster(host.getClusterId());
		FenceCommand fence = new FenceCommand(vm, host);
		
		for (HostVO h : hosts) {
			if (h.getHypervisorType() != HypervisorType.Ovm) {
				continue;
			}
			
			if( h.getStatus() != Status.Up ) {
				continue;
			}
			
			if( h.getId() == host.getId() ) {
				continue;
			}
			
			FenceAnswer answer;
			try {
				answer = (FenceAnswer)_agentMgr.send(h.getId(), fence);
			} catch (AgentUnavailableException e) {
				if (s_logger.isDebugEnabled()) {
					s_logger.debug("Moving on to the next host because " + h.toString() + " is unavailable");
				}
				continue;
			} catch (OperationTimedoutException e) {
				if (s_logger.isDebugEnabled()) {
					s_logger.debug("Moving on to the next host because " + h.toString() + " is unavailable");
				}
				continue;
			}
			
			if (answer != null && answer.getResult()) {
				return true;
			}
		}
		
		if (s_logger.isDebugEnabled()) {
			s_logger.debug("Unable to fence off " + vm.toString() + " on " + host.toString());
		}
		
		return false;
	}

}
