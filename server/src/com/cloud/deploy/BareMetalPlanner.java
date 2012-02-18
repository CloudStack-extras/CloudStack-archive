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

package com.cloud.deploy;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.capacity.CapacityManager;
import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenter;
import com.cloud.dc.Pod;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.offering.ServiceOffering;
import com.cloud.org.Cluster;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.Inject;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

@Local(value=DeploymentPlanner.class)
public class BareMetalPlanner implements DeploymentPlanner {
	private static final Logger s_logger = Logger.getLogger(BareMetalPlanner.class);
	@Inject protected DataCenterDao _dcDao;
	@Inject protected HostPodDao _podDao;
	@Inject protected ClusterDao _clusterDao;
	@Inject protected HostDao _hostDao;
	@Inject protected ConfigurationDao _configDao;
	@Inject protected CapacityManager _capacityMgr;
	@Inject protected ResourceManager _resourceMgr;
	String _name;
	
	@Override
	public DeployDestination plan(VirtualMachineProfile<? extends VirtualMachine> vmProfile, DeploymentPlan plan, ExcludeList avoid) throws InsufficientServerCapacityException {
		VirtualMachine vm = vmProfile.getVirtualMachine();
		ServiceOffering offering = vmProfile.getServiceOffering();	
		String hostTag = null;
		
        String opFactor = _configDao.getValue(Config.CPUOverprovisioningFactor.key());
        float cpuOverprovisioningFactor = NumbersUtil.parseFloat(opFactor, 1);

		
		if (vm.getLastHostId() != null) {
			HostVO h = _hostDao.findById(vm.getLastHostId());
			DataCenter dc = _dcDao.findById(h.getDataCenterId());
			Pod pod = _podDao.findById(h.getPodId());
			Cluster c =  _clusterDao.findById(h.getClusterId());
			s_logger.debug("Start baremetal vm " + vm.getId() + " on last stayed host " + h.getId());
			return new DeployDestination(dc, pod, c, h);
		}
		
		if (offering.getHostTag() != null) {
			String[] tags = offering.getHostTag().split(",");
			if (tags.length > 0) {
				hostTag = tags[0];
			}
		}
		
		List<ClusterVO> clusters = _clusterDao.listByDcHyType(vm.getDataCenterIdToDeployIn(), HypervisorType.BareMetal.toString());
		int cpu_requested;
		long ram_requested;
		HostVO target = null;
		List<HostVO> hosts;
		for (ClusterVO cluster : clusters) {
			hosts = _resourceMgr.listAllUpAndEnabledHosts(Host.Type.Routing, cluster.getId(), cluster.getPodId(), cluster.getDataCenterId());
			if (hostTag != null) {
				for (HostVO h : hosts) {
					_hostDao.loadDetails(h);
					if (h.getDetail("hostTag") != null && h.getDetail("hostTag").equalsIgnoreCase(hostTag)) {
						target = h;
						break;
					}
				}
			}
		}

		if (target == null) {
			s_logger.warn("Cannot find host with tag " + hostTag + " use capacity from service offering");
			cpu_requested = offering.getCpu() * offering.getSpeed();
			ram_requested = offering.getRamSize() * 1024 * 1024;
		} else {
			cpu_requested = target.getCpus() * target.getSpeed().intValue();
			ram_requested = target.getTotalMemory();
		}
		
		for (ClusterVO cluster : clusters) {
			hosts = _resourceMgr.listAllUpAndEnabledHosts(Host.Type.Routing, cluster.getId(), cluster.getPodId(), cluster.getDataCenterId());
			for (HostVO h : hosts) {
				if (_capacityMgr.checkIfHostHasCapacity(h.getId(), cpu_requested, ram_requested, false, cpuOverprovisioningFactor, true)) {
					s_logger.debug("Find host " + h.getId() + " has enough capacity");
					DataCenter dc = _dcDao.findById(h.getDataCenterId());
					Pod pod = _podDao.findById(h.getPodId());
					return new DeployDestination(dc, pod, cluster, h);
				}
			}
		}

		s_logger.warn(String.format("Cannot find enough capacity(requested cpu=%1$s memory=%2$s)", cpu_requested, ram_requested));
		return null;
	}

	@Override
	public boolean canHandle(VirtualMachineProfile<? extends VirtualMachine> vm, DeploymentPlan plan, ExcludeList avoid) {
		return vm.getHypervisorType() == HypervisorType.BareMetal;
	}

	@Override
	public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
		_name = name;
		return true;
	}

	@Override
	public String getName() {
		return _name;
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
	public boolean check(VirtualMachineProfile<? extends VirtualMachine> vm, DeploymentPlan plan, DeployDestination dest, ExcludeList exclude) {
		// TODO Auto-generated method stub
		return false;
	}
}
