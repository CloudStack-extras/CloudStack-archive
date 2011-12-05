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

package com.cloud.baremetal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.StopAnswer;
import com.cloud.agent.api.baremetal.IpmISetBootDevCommand;
import com.cloud.agent.api.baremetal.IpmiBootorResetCommand;
import com.cloud.agent.manager.Commands;
import com.cloud.api.commands.AttachVolumeCmd;
import com.cloud.api.commands.CreateTemplateCmd;
import com.cloud.api.commands.DeployVMCmd;
import com.cloud.api.commands.DetachVolumeCmd;
import com.cloud.api.commands.UpgradeVMCmd;
import com.cloud.baremetal.PxeServerManager.PxeServerType;
import com.cloud.configuration.Resource.ResourceType;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeployDestination;
import com.cloud.domain.DomainVO;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventVO;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Network;
import com.cloud.network.NetworkVO;
import com.cloud.network.Networks.TrafficType;
import com.cloud.org.Grouping;
import com.cloud.resource.ResourceManager;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.storage.Storage;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.Volume;
import com.cloud.template.TemplateAdapter;
import com.cloud.template.TemplateAdapter.TemplateAdapterType;
import com.cloud.template.TemplateProfile;
import com.cloud.user.Account;
import com.cloud.user.AccountVO;
import com.cloud.user.SSHKeyPair;
import com.cloud.user.User;
import com.cloud.user.UserContext;
import com.cloud.uservm.UserVm;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.component.Adapters;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.component.Manager;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.fsm.StateListener;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.BareMetalVmService;
import com.cloud.vm.NicProfile;
import com.cloud.vm.NicVO;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.UserVmManagerImpl;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.Event;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachine.Type;
import com.cloud.vm.VirtualMachineName;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.VirtualMachineProfile.Param;

@Local(value={BareMetalVmManager.class, BareMetalVmService.class})
public class BareMetalVmManagerImpl extends UserVmManagerImpl implements BareMetalVmManager, BareMetalVmService, Manager,
		StateListener<State, VirtualMachine.Event, VirtualMachine> {
	private static final Logger s_logger = Logger.getLogger(BareMetalVmManagerImpl.class); 
	private ConfigurationDao _configDao;
	@Inject PxeServerManager _pxeMgr;
	@Inject ResourceManager _resourceMgr;
	
    @Inject (adapter=TemplateAdapter.class)
    protected Adapters<TemplateAdapter> _adapters;

	@Override
	public boolean attachISOToVM(long vmId, long isoId, boolean attach) {
		s_logger.warn("attachISOToVM is not supported by Bare Metal, just fake a true");
		return true;
	}
	
	@Override
	public Volume attachVolumeToVM(AttachVolumeCmd command) {
		s_logger.warn("attachVolumeToVM is not supported by Bare Metal, return null");
		return null;
	}
	
	@Override
	public Volume detachVolumeFromVM(DetachVolumeCmd cmd) {
		s_logger.warn("detachVolumeFromVM is not supported by Bare Metal, return null");
		return null;
	}
	
	@Override
	public UserVm upgradeVirtualMachine(UpgradeVMCmd cmd) {
		s_logger.warn("upgradeVirtualMachine is not supported by Bare Metal, return null");
		return null;
	}
	
	@Override
    public VMTemplateVO createPrivateTemplateRecord(CreateTemplateCmd cmd) throws ResourceAllocationException {
		/*Baremetal creates record after host rebooting for imaging, in createPrivateTemplate*/
		return null;
	}
	
	@Override @DB
    public VMTemplateVO createPrivateTemplate(CreateTemplateCmd cmd) throws CloudRuntimeException {
	    Long vmId = cmd.getVmId();
	    if (vmId == null) {
	        throw new InvalidParameterValueException("VM ID is null");
	    }
	    
	    UserVmVO vm = _vmDao.findById(vmId);
	    if (vm == null) {
	        throw new InvalidParameterValueException("Cannot find VM for ID " + vmId);
	    }
	    
	    Long hostId = (vm.getHostId() == null ? vm.getLastHostId() : vm.getHostId());
        HostVO host = _hostDao.findById(hostId);
        if (host == null) {
            throw new InvalidParameterValueException("Cannot find host with id " + hostId);
        }

        List<HostVO> pxes = _resourceMgr.listAllUpAndEnabledHosts(Host.Type.PxeServer, null, host.getPodId(), host.getDataCenterId());
        if (pxes.size() == 0) {
            throw new CloudRuntimeException("Please add PXE server in Pod before taking image");
        }

        if (pxes.size() > 1) {
            throw new CloudRuntimeException("Multiple PXE servers found in Pod " + host.getPodId() + " Zone " + host.getDataCenterId());
        }

        HostVO pxe = pxes.get(0);
        /*
         * prepare() will check if current account has right for creating
         * template
         */
        TemplateAdapter adapter = _adapters.get(TemplateAdapterType.BareMetal.getName());
        Long userId = UserContext.current().getCallerUserId();
        userId = (userId == null ? User.UID_SYSTEM : userId);
        AccountVO account = _accountDao.findById(vm.getAccountId());
      
        try {
            TemplateProfile tmplProfile;
            tmplProfile = adapter.prepare(false, userId, cmd.getTemplateName(), cmd.getDisplayText(), cmd.getBits(), false, false, cmd.getUrl(), cmd.isPublic(), cmd.isFeatured(), false,
                    "BareMetal", cmd.getOsTypeId(), pxe.getDataCenterId(), HypervisorType.BareMetal, account.getAccountName(), account.getDomainId(), "0", true, cmd.getDetails());
 
            if (!_pxeMgr.prepareCreateTemplate(_pxeMgr.getPxeServerType(pxe), pxe.getId(), vm, cmd.getUrl())) {
                throw new Exception("Prepare PXE boot file for host  " + hostId + " failed");
            }

            IpmISetBootDevCommand setBootDev = new IpmISetBootDevCommand(IpmISetBootDevCommand.BootDev.pxe);
            Answer ans = _agentMgr.send(hostId, setBootDev);
            if (!ans.getResult()) {
                throw new Exception("Set host " + hostId + " to PXE boot failed");
            }

            IpmiBootorResetCommand boot = new IpmiBootorResetCommand();
            ans = _agentMgr.send(hostId, boot);
            if (!ans.getResult()) {
                throw new Exception("Boot/Reboot host " + hostId + " failed");
            }

            VMTemplateVO tmpl = adapter.create(tmplProfile);
            s_logger.debug("Create baremetal template for host " + hostId + " successfully, template id:" + tmpl.getId());
            return tmpl;
        } catch (Exception e) {
            s_logger.debug("Create baremetal tempalte for host " + hostId + " failed", e);
            throw new CloudRuntimeException(e.getMessage());
        }
	}

	@Override
	public UserVm createVirtualMachine(DeployVMCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException, ConcurrentOperationException,
			StorageUnavailableException, ResourceAllocationException {
		Account caller = UserContext.current().getCaller();

		String accountName = cmd.getAccountName();
		Long domainId = cmd.getDomainId();
		List<Long> networkList = cmd.getNetworkIds();
		String group = cmd.getGroup();

		Account owner = _accountDao.findActiveAccount(accountName, domainId);
		if (owner == null) {
			throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain " + domainId);
		}

		_accountMgr.checkAccess(caller, null, owner);
		long accountId = owner.getId();

		DataCenterVO dc = _dcDao.findById(cmd.getZoneId());
		if (dc == null) {
			throw new InvalidParameterValueException("Unable to find zone: " + cmd.getZoneId());
		}
		
		if(Grouping.AllocationState.Disabled == dc.getAllocationState() && !_accountMgr.isRootAdmin(caller.getType())){
			throw new PermissionDeniedException("Cannot perform this operation, Zone is currently disabled: "+ cmd.getZoneId() );
		}

		if (dc.getDomainId() != null) {
			DomainVO domain = _domainDao.findById(dc.getDomainId());
			if (domain == null) {
				throw new CloudRuntimeException("Unable to find the domain " + dc.getDomainId() + " for the zone: " + dc);
			}
			_accountMgr.checkAccess(caller, domain);
			_accountMgr.checkAccess(owner, domain);
		}

		// check if account/domain is with in resource limits to create a new vm
		_resourceLimitMgr.checkResourceLimit(owner, ResourceType.user_vm);
		
		ServiceOfferingVO offering = _serviceOfferingDao.findById(cmd.getServiceOfferingId());
        if (offering == null || offering.getRemoved() != null) {
            throw new InvalidParameterValueException("Unable to find service offering: " + cmd.getServiceOfferingId());
        }
		
		VMTemplateVO template = _templateDao.findById(cmd.getTemplateId());
        // Make sure a valid template ID was specified
        if (template == null || template.getRemoved() != null) {
            throw new InvalidParameterValueException("Unable to use template " + cmd.getTemplateId());
        }
        
        if (template.getTemplateType().equals(TemplateType.SYSTEM)) {
        	throw new InvalidParameterValueException("Unable to use system template " + cmd.getTemplateId()+" to deploy a user vm");
        }

        if (template.getFormat() != Storage.ImageFormat.BAREMETAL) {
        	throw new InvalidParameterValueException("Unable to use non Bare Metal template" + cmd.getTemplateId() +" to deploy a bare metal vm");
        }
        
        String userData = cmd.getUserData();
        byte [] decodedUserData = null;
        if (userData != null) {
            if (userData.length() >= 2 * MAX_USER_DATA_LENGTH_BYTES) {
                throw new InvalidParameterValueException("User data is too long");
            }
            decodedUserData = org.apache.commons.codec.binary.Base64.decodeBase64(userData.getBytes());
            if (decodedUserData.length > MAX_USER_DATA_LENGTH_BYTES){
                throw new InvalidParameterValueException("User data is too long");
            }
            if (decodedUserData.length < 1) {
                throw new InvalidParameterValueException("User data is too short");
            }
        }
        
        // Find an SSH public key corresponding to the key pair name, if one is given
        String sshPublicKey = null;
        if (cmd.getSSHKeyPairName() != null && !cmd.getSSHKeyPairName().equals("")) {
            Account account = UserContext.current().getCaller();
        	SSHKeyPair pair = _sshKeyPairDao.findByName(account.getAccountId(), account.getDomainId(), cmd.getSSHKeyPairName());
    		if (pair == null) {
                throw new InvalidParameterValueException("A key pair with name '" + cmd.getSSHKeyPairName() + "' was not found.");
            }

			sshPublicKey = pair.getPublicKey();
		}

		_accountMgr.checkAccess(caller, null, template);

		DataCenterDeployment plan = new DataCenterDeployment(dc.getId());

		s_logger.debug("Allocating in the DB for bare metal vm");
		
		if (dc.getNetworkType() != NetworkType.Basic || networkList != null) {
			s_logger.warn("Bare Metal only supports basical network mode now, switch to baisc network automatically");
		}
		
		Network defaultNetwork = _networkMgr.getExclusiveGuestNetwork(dc.getId());
		if (defaultNetwork == null) {
			throw new InvalidParameterValueException("Unable to find a default network to start a vm");
		}
		
		
		networkList = new ArrayList<Long>();
		networkList.add(defaultNetwork.getId());
		
		List<Pair<NetworkVO, NicProfile>> networks = new ArrayList<Pair<NetworkVO, NicProfile>>();
        for (Long networkId : networkList) {
            NetworkVO network = _networkDao.findById(networkId);
            if (network == null) {
                throw new InvalidParameterValueException("Unable to find network by id " + networkId);
            } else {
                if (network.getGuestType() != Network.GuestType.Shared) {
                    //Check account permissions
                    List<NetworkVO> networkMap = _networkDao.listBy(accountId, networkId);
                    if (networkMap == null || networkMap.isEmpty()) {
                        throw new PermissionDeniedException("Unable to create a vm using network with id " + networkId + ", permission denied");
                    }
                } 
                networks.add(new Pair<NetworkVO, NicProfile>(network, null));
            }
        }
        
        long id = _vmDao.getNextInSequence(Long.class, "id");
        
        String hostName = cmd.getName();
        String instanceName = VirtualMachineName.getVmName(id, owner.getId(), _instance);
        if (hostName == null) {
            hostName = instanceName;
        } else {
            //verify hostName (hostname doesn't have to be unique)
            if (!NetUtils.verifyDomainNameLabel(hostName, true)) {
                throw new InvalidParameterValueException("Invalid name. Vm name can contain ASCII letters 'a' through 'z', the digits '0' through '9', " +
                		                                "and the hyphen ('-'), must be between 1 and 63 characters long, and can't start or end with \"-\" and can't start with digit");
            }
        }
        
        UserVmVO vm = new UserVmVO(id, instanceName, cmd.getDisplayName(), template.getId(), HypervisorType.BareMetal,
                template.getGuestOSId(), offering.getOfferHA(), false, domainId, owner.getId(), offering.getId(), userData, hostName);
        
        if (sshPublicKey != null) {
            vm.setDetail("SSH.PublicKey", sshPublicKey);
        }

		if (_itMgr.allocate(vm, template, offering, null, null, networks, null, plan, cmd.getHypervisor(), owner) == null) {
			return null;
		}
		

		if (s_logger.isDebugEnabled()) {
			s_logger.debug("Successfully allocated DB entry for " + vm);
		}

		if (s_logger.isDebugEnabled()) {
			s_logger.debug("Successfully allocated DB entry for " + vm);
		}
		UserContext.current().setEventDetails("Vm Id: " + vm.getId());
		 UsageEventVO usageEvent = new UsageEventVO(EventTypes.EVENT_VM_CREATE, accountId, cmd.getZoneId(), vm.getId(), vm.getHostName(), offering.getId(), template.getId(), HypervisorType.BareMetal.toString());
		_usageEventDao.persist(usageEvent);

		_resourceLimitMgr.incrementResourceCount(accountId, ResourceType.user_vm);

		// Assign instance to the group
		try {
			if (group != null) {
				boolean addToGroup = addInstanceToGroup(Long.valueOf(id), group);
				if (!addToGroup) {
					throw new CloudRuntimeException("Unable to assign Vm to the group " + group);
				}
			}
		} catch (Exception ex) {
			throw new CloudRuntimeException("Unable to assign Vm to the group " + group);
		}

		return vm;
	}
	
	public UserVm startVirtualMachine(DeployVMCmd cmd) throws ResourceUnavailableException, InsufficientCapacityException, ConcurrentOperationException {
	    long vmId = cmd.getEntityId();
	    UserVmVO vm = _vmDao.findById(vmId);
	    
		List<HostVO> servers = _resourceMgr.listAllUpAndEnabledHostsInOneZoneByType(Host.Type.PxeServer, vm.getDataCenterIdToDeployIn()); 
	    if (servers.size() == 0) {
	    	throw new CloudRuntimeException("Cannot find PXE server, please make sure there is one PXE server per zone");
	    }
	    HostVO pxeServer = servers.get(0);
	    
	    VMTemplateVO template = _templateDao.findById(vm.getTemplateId());
	    if (template == null || template.getFormat() != Storage.ImageFormat.BAREMETAL) {
	    	throw new InvalidParameterValueException("Invalid template with id = " + vm.getTemplateId());
	    }
	    
		Map<VirtualMachineProfile.Param, Object> params = new HashMap<VirtualMachineProfile.Param, Object>();
		params.put(Param.PxeSeverType, _pxeMgr.getPxeServerType(pxeServer));

		return startVirtualMachine(cmd, params);
	}

	@Override
	public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
		_name = name;

		ComponentLocator locator = ComponentLocator.getCurrentLocator();
		_configDao = locator.getDao(ConfigurationDao.class);
		if (_configDao == null) {
			throw new ConfigurationException("Unable to get the configuration dao.");
		}

		Map<String, String> configs = _configDao.getConfiguration("AgentManager", params);

		_instance = configs.get("instance.name");
		if (_instance == null) {
			_instance = "DEFAULT";
		}

		String workers = configs.get("expunge.workers");
		int wrks = NumbersUtil.parseInt(workers, 10);

		String time = configs.get("expunge.interval");
		_expungeInterval = NumbersUtil.parseInt(time, 86400);

		time = configs.get("expunge.delay");
		_expungeDelay = NumbersUtil.parseInt(time, _expungeInterval);

		_executor = Executors.newScheduledThreadPool(wrks, new NamedThreadFactory("UserVm-Scavenger"));

		_itMgr.registerGuru(Type.UserBareMetal, this);
		VirtualMachine.State.getStateMachine().registerListener(this);

		s_logger.info("User VM Manager is configured.");

		return true;
	}
	
	@Override
	public boolean finalizeVirtualMachineProfile(VirtualMachineProfile<UserVmVO> profile, DeployDestination dest, ReservationContext context) {
        UserVmVO vm = profile.getVirtualMachine();
	    Account owner = _accountDao.findById(vm.getAccountId());
	    
	    if (owner == null || owner.getState() == Account.State.disabled) {
	        throw new PermissionDeniedException("The owner of " + vm + " either does not exist or is disabled: " + vm.getAccountId());
	    }
	    
	    PxeServerType pxeType = (PxeServerType) profile.getParameter(Param.PxeSeverType);
	    if (pxeType == null) {
	    	s_logger.debug("This is a normal IPMI start, skip prepartion of PXE server");
	    	return true;
	    }
	    s_logger.debug("This is a PXE start, prepare PXE server first");
	    
	    List<HostVO> servers = _resourceMgr.listAllUpAndEnabledHosts(Host.Type.PxeServer, null, dest.getPod().getId(), dest.getDataCenter().getId()); 
	    if (servers.size() == 0) {
	    	throw new CloudRuntimeException("Cannot find PXE server, please make sure there is one PXE server per zone");
	    }
	    if (servers.size() > 1) {
	    	throw new CloudRuntimeException("Find more than one PXE server, please make sure there is only one PXE server per zone in pod " + dest.getPod().getId() + " zone " + dest.getDataCenter().getId());
	    }
	    HostVO pxeServer = servers.get(0);
	    
	    if (!_pxeMgr.prepare(pxeType, profile, dest, context, pxeServer.getId())) {
	    	throw new CloudRuntimeException("Pepare PXE server failed");
	    }
	    
	    profile.addBootArgs("PxeBoot");
	    
	    return true;
	}
	
	@Override
	public boolean finalizeDeployment(Commands cmds, VirtualMachineProfile<UserVmVO> profile, DeployDestination dest, ReservationContext context) {
		UserVmVO userVm = profile.getVirtualMachine();
		List<NicVO> nics = _nicDao.listByVmId(userVm.getId());
		for (NicVO nic : nics) {
			NetworkVO network = _networkDao.findById(nic.getNetworkId());
			if (network.getTrafficType() == TrafficType.Guest) {
				userVm.setPrivateIpAddress(nic.getIp4Address());
				userVm.setPrivateMacAddress(nic.getMacAddress());
			}
		}
		_vmDao.update(userVm.getId(), userVm);
		return true;
	}

	@Override
	public void finalizeStop(VirtualMachineProfile<UserVmVO> profile, StopAnswer answer) {
		super.finalizeStop(profile, answer);
	}

	@Override
	public UserVm destroyVm(long vmId) throws ResourceUnavailableException, ConcurrentOperationException {
		return super.destroyVm(vmId);
	}

	@Override
	public boolean preStateTransitionEvent(State oldState, Event event, State newState, VirtualMachine vo, boolean status, Object opaque) {
		return true;
	}

	@Override
	public boolean postStateTransitionEvent(State oldState, Event event, State newState, VirtualMachine vo, boolean status, Object opaque) {
		if (newState != State.Starting && newState != State.Error && newState != State.Expunging) {
			return true;
		}
		
		if (vo.getHypervisorType() != HypervisorType.BareMetal) {
		    return true;
		}
		
		HostVO host = _hostDao.findById(vo.getHostId());
		if (host == null) {
			s_logger.debug("Skip oldState " + oldState + " to " + "newState " + newState + " transimtion");
			return true;
		}
		_hostDao.loadDetails(host);
		
		if (newState == State.Starting) {
			host.setDetail("vmName", vo.getInstanceName());
			s_logger.debug("Add vmName " + host.getDetail("vmName") + " to host " + host.getId() + " details");
		} else {
			if (host.getDetail("vmName") != null && host.getDetail("vmName").equalsIgnoreCase(vo.getInstanceName())) {
				s_logger.debug("Remove vmName " + host.getDetail("vmName") + " from host " + host.getId() + " details");
				host.getDetails().remove("vmName");
			}
		}
		_hostDao.saveDetails(host);
		
		
		return true;
	}
}
