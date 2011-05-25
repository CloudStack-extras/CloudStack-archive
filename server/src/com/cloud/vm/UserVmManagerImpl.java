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
package com.cloud.vm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.AttachIsoCommand;
import com.cloud.agent.api.AttachVolumeAnswer;
import com.cloud.agent.api.AttachVolumeCommand;
import com.cloud.agent.api.CreatePrivateTemplateFromSnapshotCommand;
import com.cloud.agent.api.CreatePrivateTemplateFromVolumeCommand;
import com.cloud.agent.api.GetVmStatsAnswer;
import com.cloud.agent.api.GetVmStatsCommand;
import com.cloud.agent.api.SnapshotCommand;
import com.cloud.agent.api.StopAnswer;
import com.cloud.agent.api.UpgradeSnapshotCommand;
import com.cloud.agent.api.VmStatsEntry;
import com.cloud.agent.api.storage.CreatePrivateTemplateAnswer;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.agent.manager.Commands;
import com.cloud.alert.AlertManager;
import com.cloud.api.ApiDBUtils;
import com.cloud.api.BaseCmd;
import com.cloud.api.commands.AttachVolumeCmd;
import com.cloud.api.commands.CreateTemplateCmd;
import com.cloud.api.commands.CreateVMGroupCmd;
import com.cloud.api.commands.DeleteVMGroupCmd;
import com.cloud.api.commands.DeployVMCmd;
import com.cloud.api.commands.DestroyVMCmd;
import com.cloud.api.commands.DetachVolumeCmd;
import com.cloud.api.commands.ListVMsCmd;
import com.cloud.api.commands.RebootVMCmd;
import com.cloud.api.commands.RecoverVMCmd;
import com.cloud.api.commands.ResetVMPasswordCmd;
import com.cloud.api.commands.StartVMCmd;
import com.cloud.api.commands.UpdateVMCmd;
import com.cloud.api.commands.UpgradeVMCmd;
import com.cloud.async.AsyncJobExecutor;
import com.cloud.async.AsyncJobManager;
import com.cloud.async.AsyncJobVO;
import com.cloud.async.BaseAsyncJobExecutor;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.configuration.ResourceCount.ResourceType;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.configuration.dao.ResourceLimitDao;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.AccountVlanMapDao;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeployDestination;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventVO;
import com.cloud.event.dao.EventDao;
import com.cloud.event.dao.UsageEventDao;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ManagementServerException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.exception.VirtualMachineMigrationException;
import com.cloud.ha.HighAvailabilityManager;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.DetailsDao;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.IPAddressVO;
import com.cloud.network.Network;
import com.cloud.network.Network.GuestIpType;
import com.cloud.network.NetworkManager;
import com.cloud.network.NetworkVO;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.LoadBalancerVMMapDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.lb.LoadBalancingRulesManager;
import com.cloud.network.router.VirtualNetworkApplianceManager;
import com.cloud.network.rules.RulesManager;
import com.cloud.network.security.SecurityGroup;
import com.cloud.network.security.SecurityGroupManager;
import com.cloud.network.security.dao.SecurityGroupDao;
import com.cloud.network.vpn.PasswordResetElement;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Availability;
import com.cloud.offering.ServiceOffering;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.org.Cluster;
import com.cloud.org.Grouping;
import com.cloud.server.Criteria;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.storage.GuestOSVO;
import com.cloud.storage.Snapshot;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.Storage;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.StorageManager;
import com.cloud.storage.StoragePoolStatus;
import com.cloud.storage.StoragePoolVO;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.VMTemplateZoneVO;
import com.cloud.storage.Volume;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.storage.dao.GuestOSCategoryDao;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.dao.StoragePoolDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateHostDao;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.snapshot.SnapshotManager;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.template.VirtualMachineTemplate.BootloaderType;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountService;
import com.cloud.user.AccountVO;
import com.cloud.user.SSHKeyPair;
import com.cloud.user.User;
import com.cloud.user.UserContext;
import com.cloud.user.UserVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.SSHKeyPairDao;
import com.cloud.user.dao.UserDao;
import com.cloud.user.dao.UserStatisticsDao;
import com.cloud.uservm.UserVm;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.PasswordGenerator;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.component.Manager;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.crypt.RSAHelper;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.ExecutionException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.InstanceGroupDao;
import com.cloud.vm.dao.InstanceGroupVMMapDao;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.UserVmDetailsDao;

@Local(value={UserVmManager.class, UserVmService.class})
public class UserVmManagerImpl implements UserVmManager, UserVmService, Manager {
    private static final Logger s_logger = Logger.getLogger(UserVmManagerImpl.class);

	private static final int ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_COOPERATION = 3; 	// 3 seconds

    @Inject protected HostDao _hostDao = null;
    @Inject protected DetailsDao _detailsDao = null;
    @Inject protected DomainRouterDao _routerDao = null;
    @Inject protected ServiceOfferingDao _offeringDao = null;
    @Inject protected DiskOfferingDao _diskOfferingDao = null;
    @Inject protected UserStatisticsDao _userStatsDao = null;
    @Inject protected VMTemplateDao _templateDao =  null;
    @Inject protected VMTemplateHostDao _templateHostDao = null;
    @Inject protected VMTemplateZoneDao _templateZoneDao = null;
    @Inject protected DomainDao _domainDao = null;
    @Inject protected ResourceLimitDao _limitDao = null;
    @Inject protected UserVmDao _vmDao = null;
    @Inject protected VolumeDao _volsDao = null;
    @Inject protected DataCenterDao _dcDao = null;
    @Inject protected FirewallRulesDao _rulesDao = null;
    @Inject protected LoadBalancerVMMapDao _loadBalancerVMMapDao = null;
    @Inject protected LoadBalancerDao _loadBalancerDao = null;
    @Inject protected IPAddressDao _ipAddressDao = null;
    @Inject protected HostPodDao _podDao = null;
    @Inject protected CapacityDao _capacityDao = null;
    @Inject protected NetworkManager _networkMgr = null;
    @Inject protected StorageManager _storageMgr = null;
    @Inject protected SnapshotManager _snapshotMgr = null;
    @Inject protected AgentManager _agentMgr = null;
    @Inject protected ConfigurationManager _configMgr = null;
    @Inject protected AccountDao _accountDao = null;
    @Inject protected UserDao _userDao = null;
    @Inject protected SnapshotDao _snapshotDao = null;
    @Inject protected GuestOSDao _guestOSDao = null;
    @Inject protected GuestOSCategoryDao _guestOSCategoryDao = null;
    @Inject protected HighAvailabilityManager _haMgr = null;
    @Inject protected AlertManager _alertMgr = null;
    @Inject protected AccountManager _accountMgr;
    @Inject protected AccountService _accountService;
    @Inject protected AsyncJobManager _asyncMgr;
    @Inject protected VlanDao _vlanDao;
    @Inject protected ClusterDao _clusterDao;
    @Inject protected AccountVlanMapDao _accountVlanMapDao;
    @Inject protected StoragePoolDao _storagePoolDao;
    @Inject protected VMTemplateHostDao _vmTemplateHostDao;
    @Inject protected SecurityGroupManager _securityGroupMgr;
    @Inject protected ServiceOfferingDao _serviceOfferingDao;
    @Inject protected NetworkOfferingDao _networkOfferingDao;
    @Inject protected EventDao _eventDao = null;
    @Inject protected InstanceGroupDao _vmGroupDao;
    @Inject protected InstanceGroupVMMapDao _groupVMMapDao;
    @Inject protected VirtualMachineManager _itMgr;
    @Inject protected NetworkDao _networkDao;
    @Inject protected VirtualNetworkApplianceManager _routerMgr;
    @Inject protected NicDao _nicDao;
    @Inject protected RulesManager _rulesMgr;
    @Inject protected LoadBalancingRulesManager _lbMgr;
    @Inject protected UsageEventDao _usageEventDao;
    @Inject protected SSHKeyPairDao _sshKeyPairDao;
    @Inject protected UserVmDetailsDao _vmDetailsDao;
    
    @Inject 
    protected SecurityGroupDao _securityGroupDao;

    protected ScheduledExecutorService _executor = null;
    protected int _expungeInterval;
    protected int _expungeDelay;

    protected String _name;
    protected String _instance;
    protected  String _zone;

    private ConfigurationDao _configDao;

    @Override
    public UserVmVO getVirtualMachine(long vmId) {
        return _vmDao.findById(vmId);
    }

    @Override
    public List<? extends UserVm> getVirtualMachines(long hostId) {
        return _vmDao.listByHostId(hostId);
    }

    @Override @ActionEvent (eventType=EventTypes.EVENT_VM_RESETPASSWORD, eventDescription="resetting Vm password", async=true)
    public UserVm resetVMPassword(ResetVMPasswordCmd cmd, String password) throws ResourceUnavailableException, InsufficientCapacityException{
        Account account = UserContext.current().getCaller();
    	Long userId = UserContext.current().getCallerUserId();
    	Long vmId = cmd.getId();
    	UserVmVO userVm = _vmDao.findById(cmd.getId());
    	
    	//Do parameters input validation
    	if (userVm == null) {
    	    throw new InvalidParameterValueException("unable to find a virtual machine with id " + cmd.getId());
    	}
    	
    	VMTemplateVO template = _templateDao.findByIdIncludingRemoved(userVm.getTemplateId());
    	if (template == null || !template.getEnablePassword()) {
    	    throw new InvalidParameterValueException("Fail to reset password for the virtual machine, the template is not password enabled");
    	}
    	
        if (userVm.getState() == State.Error || userVm.getState() == State.Expunging) {
            s_logger.error("vm is not in the right state: " + vmId);
            throw new InvalidParameterValueException("Vm with id " + vmId + " is not in the right state");
        }
    	
    	userId = accountAndUserValidation(vmId, account, userId, userVm);
        
    	boolean result = resetVMPasswordInternal(cmd, password);
       
        if (result) {
            userVm.setPassword(password);
        }
      
        return userVm;
    }

    private boolean resetVMPasswordInternal(ResetVMPasswordCmd cmd, String password) throws ResourceUnavailableException, InsufficientCapacityException{  
        Long vmId = cmd.getId();
        Long userId = UserContext.current().getCallerUserId();
        VMInstanceVO vmInstance = _vmDao.findById(vmId);

        if (password == null || password.equals("")) {
            return false;
        }

        VMTemplateVO template = _templateDao.findByIdIncludingRemoved(vmInstance.getTemplateId());
        if (template.getEnablePassword()) {
            Nic defaultNic = _networkMgr.getDefaultNic(vmId);
            if (defaultNic == null) {
                s_logger.error("Unable to reset password for vm " + vmInstance + " as the instance doesn't have default nic");
                return false;
            }
            
            Network defaultNetwork = _networkDao.findById(defaultNic.getNetworkId());
            NicProfile defaultNicProfile = new NicProfile(defaultNic, defaultNetwork, null, null, null);
            VirtualMachineProfile<VMInstanceVO> vmProfile = new VirtualMachineProfileImpl<VMInstanceVO>(vmInstance);
            vmProfile.setParameter(VirtualMachineProfile.Param.VmPassword, password);
            

            List<? extends PasswordResetElement> elements = _networkMgr.getPasswordResetElements();
            
            boolean result = true;
            for (PasswordResetElement element : elements) {
                if (!element.savePassword(defaultNetwork, defaultNicProfile, vmProfile)) {
                    result = false;
                }
            }
            
            // Need to reboot the virtual machine so that the password gets redownloaded from the DomR, and reset on the VM
            if (!result) {
                s_logger.debug("Failed to reset password for the virutal machine; no need to reboot the vm");
                return false;
            } else {
                if (rebootVirtualMachine(userId, vmId) == null) {
                    if (vmInstance.getState() == State.Stopped) {
                        s_logger.debug("Vm " + vmInstance + " is stopped, not rebooting it as a part of password reset");
                        return true;
                    }
                    s_logger.warn("Failed to reboot the vm " + vmInstance);
                    return false;
                } else {
                    s_logger.debug("Vm " + vmInstance + " is rebooted successfully as a part of password reset");
                    return true;
                }
            }
        } else {
        	if (s_logger.isDebugEnabled()) {
        		s_logger.debug("Reset password called for a vm that is not using a password enabled template");
        	}
        	return false;
        }
    }
    
    @Override
    public boolean stopVirtualMachine(long userId, long vmId) {
        boolean status = false;
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Stopping vm=" + vmId);
        }
        UserVmVO vm = _vmDao.findById(vmId); 
        if (vm == null || vm.getRemoved() != null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("VM is either removed or deleted.");
           }
            return true;
        }

        User user = _userDao.findById(userId);
        Account account = _accountDao.findById(user.getAccountId());
        
        try {
            status = _itMgr.stop(vm, user, account);
        } catch (ResourceUnavailableException e) {
            s_logger.debug("Unable to stop due to ", e);
            status = false;
        }
       
        if(status){
            return status;
            }
        else {
            return status;
        }
    }
    
    @Override @ActionEvent(eventType = EventTypes.EVENT_VOLUME_ATTACH, eventDescription = "attaching volume", async=true)
    public Volume attachVolumeToVM(AttachVolumeCmd command) {
    	Long vmId = command.getVirtualMachineId();
    	Long volumeId = command.getId();
    	Long deviceId = command.getDeviceId();
    	Account account = UserContext.current().getCaller();
    	
    	// Check that the volume ID is valid
    	VolumeVO volume = _volsDao.findById(volumeId);
        // Check that the volume is a data volume
        if (volume == null || volume.getVolumeType() != Volume.Type.DATADISK) {
            throw new InvalidParameterValueException("Please specify a valid data volume.");
        }

        // Check that the volume is stored on shared storage
        if (!Volume.State.Allocated.equals(volume.getState()) && !_storageMgr.volumeOnSharedStoragePool(volume)) {
            throw new InvalidParameterValueException("Please specify a volume that has been created on a shared storage pool.");
        }
        
        // Check that the volume is not currently attached to any VM
        if (volume.getInstanceId() != null) {
            throw new InvalidParameterValueException("Please specify a volume that is not attached to any VM.");
        }

        // Check that the volume is not destroyed
        if (volume.getState() == Volume.State.Destroy) {
            throw new InvalidParameterValueException("Please specify a volume that is not destroyed.");
        }

    	// Check that the virtual machine ID is valid and it's a user vm
    	UserVmVO vm = _vmDao.findById(vmId);
    	if (vm == null || vm.getType() != VirtualMachine.Type.User) {
            throw new InvalidParameterValueException("Please specify a valid User VM.");
        }
    	
        // Check that the VM is in the correct state
        if (vm.getState() != State.Running && vm.getState() != State.Stopped) {
        	throw new InvalidParameterValueException("Please specify a VM that is either running or stopped.");
        }

        // Check that the device ID is valid
        if( deviceId != null ) {
            if(deviceId.longValue() == 0) {
                throw new InvalidParameterValueException ("deviceId can't be 0, which is used by Root device");
            }
        }
        
        // Check that the VM has less than 6 data volumes attached
        List<VolumeVO> existingDataVolumes = _volsDao.findByInstanceAndType(vmId, Volume.Type.DATADISK);
        if (existingDataVolumes.size() >= 6) {
            throw new InvalidParameterValueException("The specified VM already has the maximum number of data disks (6). Please specify another VM.");
        }
        
        // Check that the VM and the volume are in the same zone
        if (vm.getDataCenterId() != volume.getDataCenterId()) {
        	throw new InvalidParameterValueException("Please specify a VM that is in the same zone as the volume.");
        }
        
        //Verify account information
        if (volume.getAccountId() != vm.getAccountId()) {
        	throw new PermissionDeniedException ("Virtual machine and volume belong to different accounts, can not attach. Permission denied.");
        }
    	
    	// If the account is not an admin, check that the volume and the virtual machine are owned by the account that was passed in
    	if (account != null) {
    	    if (!isAdmin(account.getType())) {
                if (account.getId() != volume.getAccountId()) {
                    throw new PermissionDeniedException("Unable to find volume with ID: " + volumeId + " for account: " + account.getAccountName() + ". Permission denied.");
                }

                if (account.getId() != vm.getAccountId()) {
                    throw new PermissionDeniedException("Unable to find VM with ID: " + vmId + " for account: " + account.getAccountName() + ". Permission denied");
                }
    	    } else {
    	        if (!_domainDao.isChildDomain(account.getDomainId(), volume.getDomainId()) ||
    	            !_domainDao.isChildDomain(account.getDomainId(), vm.getDomainId())) {
                    throw new PermissionDeniedException("Unable to attach volume " + volumeId + " to virtual machine instance " + vmId + ". Permission denied.");
    	        }
    	    }
    	}

        VolumeVO rootVolumeOfVm = null;
        List<VolumeVO> rootVolumesOfVm = _volsDao.findByInstanceAndType(vmId, Volume.Type.ROOT);
        if (rootVolumesOfVm.size() != 1) {
        	throw new CloudRuntimeException("The VM " + vm.getHostName() + " has more than one ROOT volume and is in an invalid state. Please contact Cloud Support.");
        } else {
        	rootVolumeOfVm = rootVolumesOfVm.get(0);
        }
        
        HypervisorType rootDiskHyperType = _volsDao.getHypervisorType(rootVolumeOfVm.getId());
        
        if (volume.getState().equals(Volume.State.Allocated)) {
    		/*Need to create the volume*/
        	VMTemplateVO rootDiskTmplt = _templateDao.findById(vm.getTemplateId());
        	DataCenterVO dcVO = _dcDao.findById(vm.getDataCenterId());
        	HostPodVO pod = _podDao.findById(vm.getPodId());
        	StoragePoolVO rootDiskPool = _storagePoolDao.findById(rootVolumeOfVm.getPoolId());
        	ServiceOfferingVO svo = _serviceOfferingDao.findById(vm.getServiceOfferingId());
        	DiskOfferingVO diskVO = _diskOfferingDao.findById(volume.getDiskOfferingId());
        	       
        	volume = _storageMgr.createVolume(volume, vm, rootDiskTmplt, dcVO, pod, rootDiskPool.getClusterId(), svo, diskVO, new ArrayList<StoragePoolVO>(), volume.getSize(), rootDiskHyperType);
        	
        	if (volume == null) {
        		throw new CloudRuntimeException("Failed to create volume when attaching it to VM: " + vm.getHostName());
        	}       	
    	}
        
        HypervisorType dataDiskHyperType = _volsDao.getHypervisorType(volume.getId());
        if (rootDiskHyperType != dataDiskHyperType) {
        	throw new InvalidParameterValueException("Can't attach a volume created by: " + dataDiskHyperType + " to a " + rootDiskHyperType + " vm");
        }
        
        List<VolumeVO> vols = _volsDao.findByInstance(vmId);
        if( deviceId != null ) {
            if( deviceId.longValue() > 15 || deviceId.longValue() == 0 || deviceId.longValue() == 3) {
                throw new RuntimeException("deviceId should be 1,2,4-15");
            }
            for (VolumeVO vol : vols) {
                if (vol.getDeviceId().equals(deviceId)) {
                    throw new RuntimeException("deviceId " + deviceId + " is used by VM " + vm.getHostName());
                }
            }           
        } else {
            // allocate deviceId here
            List<String> devIds = new ArrayList<String>();
            for( int i = 1; i < 15; i++ ) {
                devIds.add(String.valueOf(i));
            }
            devIds.remove("3");
            for (VolumeVO vol : vols) {
                devIds.remove(vol.getDeviceId().toString().trim());
            } 
            deviceId = Long.parseLong(devIds.iterator().next());        
        }
        
        StoragePoolVO vmRootVolumePool = _storagePoolDao.findById(rootVolumeOfVm.getPoolId());
        DiskOfferingVO volumeDiskOffering = _diskOfferingDao.findById(volume.getDiskOfferingId());
        String[] volumeTags = volumeDiskOffering.getTagsArray();
        
        StoragePoolVO sourcePool = _storagePoolDao.findById(volume.getPoolId());
        List<StoragePoolVO> sharedVMPools = _storagePoolDao.findPoolsByTags(vmRootVolumePool.getDataCenterId(), vmRootVolumePool.getPodId(), vmRootVolumePool.getClusterId(), volumeTags, true);
        boolean moveVolumeNeeded = true;
        if (sharedVMPools.size() == 0) {
        	String poolType;
        	if (vmRootVolumePool.getClusterId() != null) {
        		poolType = "cluster";
        	} else if (vmRootVolumePool.getPodId() != null) {
        		poolType = "pod";
        	} else {
        		poolType = "zone";
        	}
        	throw new CloudRuntimeException("There are no storage pools in the VM's " + poolType + " with all of the volume's tags (" + volumeDiskOffering.getTags() + ").");
        } else {
        	Long sourcePoolDcId = sourcePool.getDataCenterId();
        	Long sourcePoolPodId = sourcePool.getPodId();
        	Long sourcePoolClusterId = sourcePool.getClusterId();
        	for (StoragePoolVO vmPool : sharedVMPools) {
        		Long vmPoolDcId = vmPool.getDataCenterId();
        		Long vmPoolPodId = vmPool.getPodId();
        		Long vmPoolClusterId = vmPool.getClusterId();
        		
        		if (sourcePoolDcId == vmPoolDcId && sourcePoolPodId == vmPoolPodId && sourcePoolClusterId == vmPoolClusterId) {
        			moveVolumeNeeded = false;
        			break;
        		}
        	}
        }
       
    	if (moveVolumeNeeded) {
    		// Move the volume to a storage pool in the VM's zone, pod, or cluster
    		volume = _storageMgr.moveVolume(volume, vmRootVolumePool.getDataCenterId(), vmRootVolumePool.getPodId(), vmRootVolumePool.getClusterId(), dataDiskHyperType);
    	}
    	
        AsyncJobExecutor asyncExecutor = BaseAsyncJobExecutor.getCurrentExecutor();
        if(asyncExecutor != null) {
        	AsyncJobVO job = asyncExecutor.getJob();

        	if(s_logger.isInfoEnabled()) {
                s_logger.info("Trying to attaching volume " + volumeId +" to vm instance:"+vm.getId()+ ", update async job-" + job.getId() + " progress status");
            }
        	
        	_asyncMgr.updateAsyncJobAttachment(job.getId(), "volume", volumeId);
        	_asyncMgr.updateAsyncJobStatus(job.getId(), BaseCmd.PROGRESS_INSTANCE_CREATED, volumeId);
        }
    	
    	String errorMsg = "Failed to attach volume: " + volume.getName() + " to VM: " + vm.getHostName();
    	boolean sendCommand = (vm.getState() == State.Running);
    	AttachVolumeAnswer answer = null;
    	Long hostId = vm.getHostId();
    	if(hostId  == null) {
    		hostId = vm.getLastHostId();
    		HostVO host = _hostDao.findById(hostId);
    		if(host != null && host.getHypervisorType() == HypervisorType.VMware) {
                sendCommand = true;
            }
    	}
    	
    	if (sendCommand) {
    		StoragePoolVO volumePool = _storagePoolDao.findById(volume.getPoolId());
    		AttachVolumeCommand cmd = new AttachVolumeCommand(true, vm.getInstanceName(), volume.getPoolType(), volume.getFolder(), volume.getPath(), volume.getName(), deviceId, volume.getChainInfo());
			cmd.setPoolUuid(volumePool.getUuid());
    		
    		try {
    			answer = (AttachVolumeAnswer)_agentMgr.send(hostId, cmd);
    		} catch (Exception e) {
    			throw new CloudRuntimeException(errorMsg + " due to: " + e.getMessage());
    		}
    	}

        if (!sendCommand || (answer != null && answer.getResult())) {
    		// Mark the volume as attached
            if( sendCommand ) {
                _volsDao.attachVolume(volume.getId(), vmId, answer.getDeviceId());
            } else {
                _volsDao.attachVolume(volume.getId(), vmId, deviceId);
            }
            return _volsDao.findById(volumeId);
    	} else {
    		if (answer != null) {
    			String details = answer.getDetails();
    			if (details != null && !details.isEmpty()) {
                    errorMsg += "; " + details;
                }
    		}
    		throw new CloudRuntimeException(errorMsg);
    	}
    }
    
    @Override @ActionEvent(eventType = EventTypes.EVENT_VOLUME_DETACH, eventDescription = "detaching volume", async=true)
    public Volume detachVolumeFromVM(DetachVolumeCmd cmmd) {    	
    	Account account = UserContext.current().getCaller();
    	if ((cmmd.getId() == null && cmmd.getDeviceId() == null && cmmd.getVirtualMachineId() == null) ||
    	    (cmmd.getId() != null && (cmmd.getDeviceId() != null || cmmd.getVirtualMachineId() != null)) ||
    	    (cmmd.getId() == null && (cmmd.getDeviceId()==null || cmmd.getVirtualMachineId() == null))) {
    	    throw new InvalidParameterValueException("Please provide either a volume id, or a tuple(device id, instance id)");
    	}

    	Long volumeId = cmmd.getId();
    	VolumeVO volume = null;
    	
    	if(volumeId != null) {
    		volume = _volsDao.findById(volumeId);
    	} else {
    		volume = _volsDao.findByInstanceAndDeviceId(cmmd.getVirtualMachineId(), cmmd.getDeviceId()).get(0);
    	}

    	Long vmId = null;
    	
    	if (cmmd.getVirtualMachineId() == null) {
    		vmId = volume.getInstanceId();
    	} else {
    		vmId = cmmd.getVirtualMachineId();
    	}

    	boolean isAdmin;
    	if (account == null) {
    		// Admin API call
    		isAdmin = true;
    	} else {
    		// User API call
    		isAdmin = isAdmin(account.getType());
    	}

    	// Check that the volume ID is valid
    	if (volume == null) {
            throw new InvalidParameterValueException("Unable to find volume with ID: " + volumeId);
        }

    	// If the account is not an admin, check that the volume is owned by the account that was passed in
    	if (!isAdmin) {
    		if (account.getId() != volume.getAccountId()) {
                throw new InvalidParameterValueException("Unable to find volume with ID: " + volumeId + " for account: " + account.getAccountName());
            }
    	} else if (account != null) {
    	    if (!_domainDao.isChildDomain(account.getDomainId(), volume.getDomainId())) {
                throw new PermissionDeniedException("Unable to detach volume with ID: " + volumeId + ", permission denied.");
    	    }
    	}

        // Check that the volume is a data volume
        if (volume.getVolumeType() != Volume.Type.DATADISK) {
            throw new InvalidParameterValueException("Please specify a data volume.");
        }

        // Check that the volume is stored on shared storage
        if (!_storageMgr.volumeOnSharedStoragePool(volume)) {
            throw new InvalidParameterValueException("Please specify a volume that has been created on a shared storage pool.");
        }

        // Check that the volume is currently attached to a VM
        if (vmId == null) {
            throw new InvalidParameterValueException("The specified volume is not attached to a VM.");
        }

        // Check that the VM is in the correct state
        UserVmVO vm = _vmDao.findById(vmId);
        if (vm.getState() != State.Running && vm.getState() != State.Stopped && vm.getState() != State.Destroyed) {
        	throw new InvalidParameterValueException("Please specify a VM that is either running or stopped.");
        }
        
        AsyncJobExecutor asyncExecutor = BaseAsyncJobExecutor.getCurrentExecutor();
        if(asyncExecutor != null) {
        	AsyncJobVO job = asyncExecutor.getJob();

        	if(s_logger.isInfoEnabled()) {
                s_logger.info("Trying to attaching volume " + volumeId +"to vm instance:"+vm.getId()+ ", update async job-" + job.getId() + " progress status");
            }
        	
        	_asyncMgr.updateAsyncJobAttachment(job.getId(), "volume", volumeId);
        	_asyncMgr.updateAsyncJobStatus(job.getId(), BaseCmd.PROGRESS_INSTANCE_CREATED, volumeId);
        }
    	
    	String errorMsg = "Failed to detach volume: " + volume.getName() + " from VM: " + vm.getHostName();
    	boolean sendCommand = (vm.getState() == State.Running);
    	Answer answer = null;
    	
    	if (sendCommand) {
			AttachVolumeCommand cmd = new AttachVolumeCommand(false, vm.getInstanceName(), volume.getPoolType(), volume.getFolder(), volume.getPath(), volume.getName(), 
				cmmd.getDeviceId() != null ? cmmd.getDeviceId() : volume.getDeviceId(), volume.getChainInfo());

			StoragePoolVO volumePool = _storagePoolDao.findById(volume.getPoolId());
			cmd.setPoolUuid(volumePool.getUuid());

			try {
    			answer = _agentMgr.send(vm.getHostId(), cmd);
    		} catch (Exception e) {
    			throw new CloudRuntimeException(errorMsg + " due to: " + e.getMessage());
    		}
    	}
    	
		if (!sendCommand || (answer != null && answer.getResult())) {
			// Mark the volume as detached
    		_volsDao.detachVolume(volume.getId());
    		if(answer != null && answer instanceof AttachVolumeAnswer) {
    			volume.setChainInfo(((AttachVolumeAnswer)answer).getChainInfo());
    			_volsDao.update(volume.getId(), volume);
    		}
    		
            return _volsDao.findById(volumeId);
    	} else {
    		
    		if (answer != null) {
    			String details = answer.getDetails();
    			if (details != null && !details.isEmpty()) {
                    errorMsg += "; " + details;
                }
    		}
    		
    		throw new CloudRuntimeException(errorMsg);
    	}
    }
    
    @Override
    public boolean attachISOToVM(long vmId, long isoId, boolean attach) {
    	UserVmVO vm = _vmDao.findById(vmId);
    	
    	if (vm == null) {
            return false;
    	} else if (vm.getState() != State.Running) {
    		return true;
    	}
        String isoPath;
        VMTemplateVO tmplt = _templateDao.findById(isoId);   
        if ( tmplt == null ) {
            s_logger.warn("ISO: " + isoId +" does not exist");
            return false;
        }
        // Get the path of the ISO
        Pair<String, String> isoPathPair = null;
        if ( tmplt.getTemplateType() == TemplateType.PERHOST ) {
            isoPath = tmplt.getName();
        } else {
            isoPathPair = _storageMgr.getAbsoluteIsoPath(isoId, vm.getDataCenterId()); 	
            if (isoPathPair == null) {
	    	    s_logger.warn("Couldn't get absolute iso path");
	    	    return false;
	    	} else {
	    	    isoPath = isoPathPair.first();
	    	}
        }

    	String vmName = vm.getInstanceName();

    	HostVO host = _hostDao.findById(vm.getHostId());
    	if (host == null) {
            s_logger.warn("Host: " + vm.getHostId() +" does not exist");
            return false;
    	}
    	AttachIsoCommand cmd = new AttachIsoCommand(vmName, isoPath, attach);
    	if (isoPathPair != null) {
    		cmd.setStoreUrl(isoPathPair.second());
    	}
    	Answer a = _agentMgr.easySend(vm.getHostId(), cmd);
    	return (a != null);
    }

 
    private UserVm rebootVirtualMachine(long userId, long vmId) throws InsufficientCapacityException, ResourceUnavailableException{
        UserVmVO vm = _vmDao.findById(vmId);
        User caller = _accountMgr.getActiveUser(userId);
        Account owner = _accountMgr.getAccount(vm.getAccountId());

        if (vm == null || vm.getState() == State.Destroyed || vm.getState() == State.Expunging || vm.getRemoved() != null) {
            s_logger.warn("Vm id=" + vmId + " doesn't exist");
            return null;
        }

        if (vm.getState() == State.Running && vm.getHostId() != null) {
           return  _itMgr.reboot(vm, null, caller, owner);
        } else {
            s_logger.error("Vm id=" + vmId + " is not in Running state, failed to reboot");
            return null;
        }
    }
    
    @Override @ActionEvent (eventType=EventTypes.EVENT_VM_UPGRADE, eventDescription="upgrading Vm")
    /*
     * TODO: cleanup eventually - Refactored API call
     */
    public UserVm upgradeVirtualMachine(UpgradeVMCmd cmd){
        Long virtualMachineId = cmd.getId();
        Long serviceOfferingId = cmd.getServiceOfferingId();
        Account account = UserContext.current().getCaller();
        Long userId = UserContext.current().getCallerUserId();

        // Verify input parameters
        UserVmVO vmInstance = _vmDao.findById(virtualMachineId);
        if (vmInstance == null) {
        	throw new InvalidParameterValueException("unable to find a virtual machine with id " + virtualMachineId);
        }       

        userId = accountAndUserValidation(virtualMachineId, account, userId,vmInstance);                         
            
        // Check that the specified service offering ID is valid
        ServiceOfferingVO newServiceOffering = _offeringDao.findById(serviceOfferingId);
        if (newServiceOffering == null) {
        	throw new InvalidParameterValueException("Unable to find a service offering with id " + serviceOfferingId);
        }
            
        // Check that the VM is stopped
        if (!vmInstance.getState().equals(State.Stopped)) {
            s_logger.warn("Unable to upgrade virtual machine " + vmInstance.toString() + " in state " + vmInstance.getState());
            throw new InvalidParameterValueException("Unable to upgrade virtual machine " + vmInstance.toString() + " in state " + vmInstance.getState() + "; make sure the virtual machine is stopped and not in an error state before upgrading.");
        }
        
        // Check if the service offering being upgraded to is what the VM is already running with
        if (vmInstance.getServiceOfferingId() == newServiceOffering.getId()) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("Not upgrading vm " + vmInstance.toString() + " since it already has the requested service offering (" + newServiceOffering.getName() + ")");
            }
            
            throw new InvalidParameterValueException("Not upgrading vm " + vmInstance.toString() + " since it already has the requested service offering (" + newServiceOffering.getName() + ")");
        }
        
        ServiceOfferingVO currentServiceOffering = _offeringDao.findByIdIncludingRemoved(vmInstance.getServiceOfferingId());
        
        // Check that the service offering being upgraded to has the same Guest IP type as the VM's current service offering
        // NOTE: With the new network refactoring in 2.2, we shouldn't need the check for same guest IP type anymore.
        /*
        if (!currentServiceOffering.getGuestIpType().equals(newServiceOffering.getGuestIpType())) {
        	String errorMsg = "The service offering being upgraded to has a guest IP type: " + newServiceOffering.getGuestIpType();
        	errorMsg += ". Please select a service offering with the same guest IP type as the VM's current service offering (" + currentServiceOffering.getGuestIpType() + ").";
        	throw new InvalidParameterValueException(errorMsg);
        }
        */
        
        // Check that the service offering being upgraded to has the same storage pool preference as the VM's current service offering
        if (currentServiceOffering.getUseLocalStorage() != newServiceOffering.getUseLocalStorage()) {
            throw new InvalidParameterValueException("Unable to upgrade virtual machine " + vmInstance.toString() + ", cannot switch between local storage and shared storage service offerings.  Current offering useLocalStorage=" +
                   currentServiceOffering.getUseLocalStorage() + ", target offering useLocalStorage=" + newServiceOffering.getUseLocalStorage());
        }

        // Check that there are enough resources to upgrade the service offering
        if (!_agentMgr.isVirtualMachineUpgradable(vmInstance, newServiceOffering)) {
           throw new InvalidParameterValueException("Unable to upgrade virtual machine, not enough resources available for an offering of " +
                   newServiceOffering.getCpu() + " cpu(s) at " + newServiceOffering.getSpeed() + " Mhz, and " + newServiceOffering.getRamSize() + " MB of memory");
        }
        
        // Check that the service offering being upgraded to has all the tags of the current service offering
        List<String> currentTags = _configMgr.csvTagsToList(currentServiceOffering.getTags());
        List<String> newTags = _configMgr.csvTagsToList(newServiceOffering.getTags());
        if (!newTags.containsAll(currentTags)) {
        	throw new InvalidParameterValueException("Unable to upgrade virtual machine; the new service offering does not have all the tags of the " +
        											 "current service offering. Current service offering tags: " + currentTags + "; " +
        											 "new service offering tags: " + newTags);
        }

		UserVmVO vmForUpdate = _vmDao.createForUpdate();
		vmForUpdate.setServiceOfferingId(serviceOfferingId);
		vmForUpdate.setHaEnabled(_serviceOfferingDao.findById(serviceOfferingId).getOfferHA());
		_vmDao.update(vmInstance.getId(), vmForUpdate);
		
		return _vmDao.findById(vmInstance.getId());
    }

	private Long accountAndUserValidation(Long virtualMachineId,Account account, Long userId, UserVmVO vmInstance) {
		if (account != null) {
            if (!isAdmin(account.getType()) && (account.getId() != vmInstance.getAccountId())) {
                throw new InvalidParameterValueException("Unable to find a virtual machine with id " + virtualMachineId + " for this account");
            } else if (!_domainDao.isChildDomain(account.getDomainId(),vmInstance.getDomainId())) {
                throw new InvalidParameterValueException( "Invalid virtual machine id (" + virtualMachineId + ") given, unable to upgrade virtual machine.");
            }
        }

        // If command is executed via 8096 port, set userId to the id of System account (1)
        if (userId == null) {
            userId = Long.valueOf(User.UID_SYSTEM);
        }
		return userId;
	}

    @Override
    public HashMap<Long, VmStatsEntry> getVirtualMachineStatistics(long hostId, String hostName, List<Long> vmIds) throws CloudRuntimeException {
    	HashMap<Long, VmStatsEntry> vmStatsById = new HashMap<Long, VmStatsEntry>();
    	
    	if (vmIds.isEmpty()) {
    		return vmStatsById;
    	}
    	
    	List<String> vmNames = new ArrayList<String>();
    	
    	for (Long vmId : vmIds) {
    		UserVmVO vm = _vmDao.findById(vmId);
    		vmNames.add(vm.getInstanceName());
    	}
    	
    	Answer answer = _agentMgr.easySend(hostId, new GetVmStatsCommand(vmNames,_hostDao.findById(hostId).getGuid(), hostName));
    	if (answer == null || !answer.getResult()) {
    		s_logger.warn("Unable to obtain VM statistics.");
    		return null;
    	} else {
    		HashMap<String, VmStatsEntry> vmStatsByName = ((GetVmStatsAnswer) answer).getVmStatsMap();

    		if(vmStatsByName == null)
    		{
    			s_logger.warn("Unable to obtain VM statistics.");
        		return null;
    		}
    			
    		for (String vmName : vmStatsByName.keySet()) {
    			vmStatsById.put(vmIds.get(vmNames.indexOf(vmName)), vmStatsByName.get(vmName));
    		}
    	}
    	
    	return vmStatsById;
    }
    
    @Override @DB
    public UserVm recoverVirtualMachine(RecoverVMCmd cmd) throws ResourceAllocationException, CloudRuntimeException {
    	
        Long vmId = cmd.getId();
        Account accountHandle = UserContext.current().getCaller();
   
        //if account is removed, return error
        if(accountHandle!=null && accountHandle.getRemoved() != null) {
            throw new InvalidParameterValueException("The account " + accountHandle.getId()+" is removed");
        }

        // Verify input parameters
        UserVmVO vm = _vmDao.findById(vmId.longValue());
        
        if (vm == null) {
        	throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        }

        if ((accountHandle != null) && !_domainDao.isChildDomain(accountHandle.getDomainId(), vm.getDomainId())) {
            // the domain in which the VM lives is not in the admin's domain tree
            throw new InvalidParameterValueException("Unable to recover virtual machine with id " + vmId + ", invalid id given.");
        }

        if (vm.getRemoved() != null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Unable to find vm or vm is removed: " + vmId);
            }
            throw new InvalidParameterValueException("Unable to find vm by id " + vmId);
        }
        
        if (vm.getState() != State.Destroyed) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("vm is not in the right state: " + vmId);
            }
            throw new InvalidParameterValueException("Vm with id " + vmId + " is not in the right state");
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Recovering vm " + vmId);
        }

        Transaction txn = Transaction.currentTxn();
        AccountVO account = null;
    	txn.start();

        account = _accountDao.lockRow(vm.getAccountId(), true);
        
        //if the account is deleted, throw error
        if(account.getRemoved()!=null) {
            throw new CloudRuntimeException("Unable to recover VM as the account is deleted");
        }
        
    	// First check that the maximum number of UserVMs for the given accountId will not be exceeded
        if (_accountMgr.resourceLimitExceeded(account, ResourceType.user_vm)) {
        	ResourceAllocationException rae = new ResourceAllocationException("Maximum number of virtual machines for account: " + account.getAccountName() + " has been exceeded.");
        	rae.setResourceType("vm");
        	txn.commit();
        	throw rae;
        }
        
        _haMgr.cancelDestroy(vm, vm.getHostId());

        _accountMgr.incrementResourceCount(account.getId(), ResourceType.user_vm);

        if (!_itMgr.stateTransitTo(vm, VirtualMachine.Event.RecoveryRequested, null)) {
            s_logger.debug("Unable to recover the vm because it is not in the correct state: " + vmId);
            throw new InvalidParameterValueException("Unable to recover the vm because it is not in the correct state: " + vmId);
        }
        
        // Recover the VM's disks
        List<VolumeVO> volumes = _volsDao.findByInstance(vmId);
        for (VolumeVO volume : volumes) {
            if (volume.getVolumeType().equals(Volume.Type.ROOT)) {
                // Create an event
                Long templateId = volume.getTemplateId();
                Long diskOfferingId = volume.getDiskOfferingId();
                Long offeringId = null;
                if(diskOfferingId != null){
                    DiskOfferingVO offering = _diskOfferingDao.findById(diskOfferingId);
                    if(offering!=null && (offering.getType() == DiskOfferingVO.Type.Disk)){
                        offeringId = offering.getId();
                    }
                }
                UsageEventVO usageEvent = new UsageEventVO(EventTypes.EVENT_VOLUME_CREATE, volume.getAccountId(), volume.getDataCenterId(), volume.getId(), volume.getName(), offeringId, templateId , volume.getSize());
                _usageEventDao.persist(usageEvent);
            }
        }
        
        _accountMgr.incrementResourceCount(account.getId(), ResourceType.volume, new Long(volumes.size()));
        
        UsageEventVO usageEvent = new UsageEventVO(EventTypes.EVENT_VM_CREATE, vm.getAccountId(), vm.getDataCenterId(), vm.getId(), vm.getHostName(), vm.getServiceOfferingId(), vm.getTemplateId(), vm.getHypervisorType().toString());
        _usageEventDao.persist(usageEvent);
        txn.commit();
        
        return _vmDao.findById(vmId);
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
        
        _itMgr.registerGuru(VirtualMachine.Type.User, this);
        
        s_logger.info("User VM Manager is configured.");

        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean start() {
    	_executor.scheduleWithFixedDelay(new ExpungeTask(), _expungeInterval, _expungeInterval, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public boolean stop() {
    	_executor.shutdown();
        return true;
    }

    protected UserVmManagerImpl() {
    }

    public String getRandomPrivateTemplateName() {
    	return UUID.randomUUID().toString();
    }

    @Override
    public Long convertToId(String vmName) {
        if (!VirtualMachineName.isValidVmName(vmName, _instance)) {
            return null;
        }
        return VirtualMachineName.getVmId(vmName);
    }

    @Override
    public boolean expunge(UserVmVO vm, long callerUserId, Account caller) {
        UserContext ctx = UserContext.current();
        ctx.setAccountId(vm.getAccountId());
        
	    try { 
	        if (!_itMgr.advanceExpunge(vm, _accountMgr.getSystemUser(), caller)) {
                s_logger.info("Did not expunge " + vm);
                return false;
            }
	        
	        //Only if vm is not expunged already, cleanup it's resources
	        if (vm != null && vm.getRemoved() == null) {
	            //Cleanup vm resources - all the PF/LB/StaticNat rules associated with vm
	            s_logger.debug("Starting cleaning up vm " + vm + " resources...");
	            if (cleanupVmResources(vm.getId())) {
	                s_logger.debug("Successfully cleaned up vm " + vm + " resources as a part of expunge process");
	            } else {
	                s_logger.warn("Failed to cleanup resources as a part of vm " + vm + " expunge");
	                return false;
	            }
	 
	            _itMgr.remove(vm, _accountMgr.getSystemUser(), caller);
	        }
	       
	        return true;
	        
        } catch (ResourceUnavailableException e) {
            s_logger.warn("Unable to expunge  " + vm, e);
            return false;
        } catch (OperationTimedoutException e) {
            s_logger.warn("Operation time out on expunging " + vm, e);
            return false;
        } catch (ConcurrentOperationException e) {
            s_logger.warn("Concurrent operations on expunging " + vm, e);
            return false;
        }
    }
    
    private boolean cleanupVmResources(long vmId) {
        boolean success = true;
        
        //Remove vm from security groups
        _securityGroupMgr.removeInstanceFromGroups(vmId);
        
        //Remove vm from instance group
        removeInstanceFromInstanceGroup(vmId);
        
        //cleanup port forwarding rules
        if (_rulesMgr.revokePortForwardingRulesForVm(vmId)) {
            s_logger.debug("Port forwarding rules are removed successfully as a part of vm id=" + vmId + " expunge");
        } else {
            success = false;
            s_logger.warn("Fail to remove port forwarding rules as a part of vm id=" + vmId + " expunge");
        }
        
        //cleanup load balancer rules
        if (_lbMgr.removeVmFromLoadBalancers(vmId)) {
            s_logger.debug("Removed vm id=" + vmId + " from all load balancers as a part of expunge process");
        } else {
            success = false;
            s_logger.warn("Fail to remove vm id=" + vmId + " from load balancers as a part of expunge process");
        }
        
        //If vm is assigned to static nat, disable static nat for the ip address
        IPAddressVO ip = _ipAddressDao.findByAssociatedVmId(vmId);
        try {
            if (ip != null) {
                if (_rulesMgr.disableOneToOneNat(ip.getId())) {
                    s_logger.debug("Disabled 1-1 nat for ip address " + ip + " as a part of vm id=" + vmId + " expunge");
                } else {
                    s_logger.warn("Failed to disable static nat for ip address " + ip + " as a part of vm id=" + vmId + " expunge");
                    success = false;
                }
            }
        } catch (ResourceUnavailableException e) {
            success = false;
            s_logger.warn("Failed to disable static nat for ip address " + ip + " as a part of vm id=" + vmId + " expunge because resource is unavailable", e);
        }

        return success;
    }

    @Override
    public void deletePrivateTemplateRecord(Long templateId){
        if ( templateId != null) {
            _templateDao.remove(templateId);
        }
    }


    @Override
    public VMTemplateVO createPrivateTemplateRecord(CreateTemplateCmd cmd) throws InvalidParameterValueException, PermissionDeniedException, ResourceAllocationException {
        Long userId = UserContext.current().getCallerUserId();
        if (userId == null) {
            userId = User.UID_SYSTEM;
        }

        Account account = UserContext.current().getCaller();
        boolean isAdmin = ((account == null) || isAdmin(account.getType()));
        
    	VMTemplateVO privateTemplate = null;
    	
    	UserVO user = _userDao.findById(userId);
    	
    	if (user == null) {
    		throw new InvalidParameterValueException("User " + userId + " does not exist");
    	}    	  	

        String name = cmd.getTemplateName();
        if ((name == null) || (name.length() > 32)) {
            throw new InvalidParameterValueException("Template name cannot be null and should be less than 32 characters");
        }
        
        // do some parameter defaulting
        Integer bits = cmd.getBits();
        Boolean requiresHvm = cmd.getRequiresHvm();
        Boolean passwordEnabled = cmd.isPasswordEnabled();
        Boolean isPublic = cmd.isPublic();
        Boolean featured = cmd.isFeatured();
        int bitsValue = ((bits == null) ? 64 : bits.intValue());
        boolean requiresHvmValue = ((requiresHvm == null) ? true : requiresHvm.booleanValue());
        boolean passwordEnabledValue = ((passwordEnabled == null) ? false : passwordEnabled.booleanValue());
        if (isPublic == null) {
            isPublic = Boolean.FALSE;
        }
        boolean allowPublicUserTemplates = Boolean.parseBoolean(_configDao.getValue("allow.public.user.templates"));        
        if (!isAdmin && !allowPublicUserTemplates && isPublic) {
            throw new PermissionDeniedException("Failed to create template " + name + ", only private templates can be created.");
        }
        
    	Long volumeId = cmd.getVolumeId();
    	Long snapshotId = cmd.getSnapshotId();
    	if ( (volumeId == null) && (snapshotId == null) ) {
            throw new InvalidParameterValueException("Failed to create private template record, neither volume ID nor snapshot ID were specified.");
    	}
        if ( (volumeId != null) && (snapshotId != null) ) {
            throw new InvalidParameterValueException("Failed to create private template record, please specify only one of volume ID (" + volumeId + ") and snapshot ID (" + snapshotId + ")");
        }
        
        long domainId;
        long accountId;
        HypervisorType hyperType;
        VolumeVO volume = null;
    	if (volumeId != null) { // create template from volume
    	    volume = _volsDao.findById(volumeId);
    	    if (volume == null) {
    	        throw new InvalidParameterValueException("Failed to create private template record, unable to find volume " + volumeId);
    	    }
            // If private template is created from Volume, check that the volume will not be active when the private template is created
            if (!_storageMgr.volumeInactive(volume)) {
                String msg = "Unable to create private template for volume: " + volume.getName() + "; volume is attached to a non-stopped VM, please stop the VM first" ;
                if (s_logger.isInfoEnabled()) {
                    s_logger.info(msg);
                }
                throw new CloudRuntimeException(msg);
            }
    	    domainId = volume.getDomainId();
    	    accountId = volume.getAccountId();
    	    hyperType = _volsDao.getHypervisorType(volumeId);
    	} else {  // create template from snapshot
            SnapshotVO snapshot = _snapshotDao.findById(snapshotId);
            if (snapshot == null) {
                throw new InvalidParameterValueException("Failed to create private template record, unable to find snapshot " + snapshotId);
            }
            
            if (snapshot.getStatus() != Snapshot.Status.BackedUp) {
                throw new InvalidParameterValueException("Snapshot id=" + snapshotId + " is not in " + Snapshot.Status.BackedUp + " state yet and can't be used for template creation");
            }
            
            domainId = snapshot.getDomainId();
            accountId = snapshot.getAccountId();
            hyperType = snapshot.getHypervisorType();
            volume = _volsDao.findById(snapshot.getVolumeId());
    	}

        if (!isAdmin) {
            if (account.getId() != accountId) {
                throw new PermissionDeniedException("Unable to create a template permission denied.");
            }
        } else if ((account != null) && !_domainDao.isChildDomain(account.getDomainId(), domainId)) {
            throw new PermissionDeniedException("Unable to create a template permission denied.");
        }

        VMTemplateVO existingTemplate = _templateDao.findByTemplateNameAccountId(name, accountId);
        if (existingTemplate != null) {
            throw new InvalidParameterValueException("Failed to create private template " + name + ", a template with that name already exists.");
        }

        AccountVO ownerAccount = _accountDao.findById(accountId);
        if (_accountMgr.resourceLimitExceeded(ownerAccount, ResourceType.template)) {
        	ResourceAllocationException rae = new ResourceAllocationException("Maximum number of templates and ISOs for account: " + account.getAccountName() + " has been exceeded.");
        	rae.setResourceType("template");
        	throw rae;
        } 
        
        if (!isAdmin || featured == null) {
            featured = Boolean.FALSE;
        }
    	Long guestOSId = cmd.getOsTypeId();
    	GuestOSVO guestOS = _guestOSDao.findById(guestOSId);
    	if (guestOS == null) {
    		throw new InvalidParameterValueException("GuestOS with ID: " + guestOSId + " does not exist.");
    	}
    	
        String uniqueName = Long.valueOf((userId == null)?1:userId).toString() + UUID.nameUUIDFromBytes(name.getBytes()).toString();
    	Long nextTemplateId = _templateDao.getNextInSequence(Long.class, "id");
    	String description = cmd.getDisplayText();
    	boolean isExtractable = false;   	
    	if ( volume != null ) {
    	    VMTemplateVO template = ApiDBUtils.findTemplateById(volume.getTemplateId());            
    	    isExtractable = template != null && template.isExtractable() && template.getTemplateType() != Storage.TemplateType.SYSTEM ;
    	}
        privateTemplate = new VMTemplateVO(nextTemplateId,
                                           uniqueName,
                                           name,
                                           ImageFormat.RAW,
                                           isPublic,
                                           featured,
                                           isExtractable,
                                           TemplateType.USER,
                                           null,
                                           null,
                                           requiresHvmValue,
                                           bitsValue,
                                           accountId,
                                           null,
                                           description,
                                           passwordEnabledValue,
                                           guestOS.getId(),
                                           true,
                                           hyperType);        

        VMTemplateVO template = _templateDao.persist(privateTemplate);
        // Increment the number of templates
        if (template != null) {
            _accountMgr.incrementResourceCount(accountId, ResourceType.template);
        }

        return template;
    }

    @Override @DB
    public VMTemplateVO createPrivateTemplate(CreateTemplateCmd command) throws CloudRuntimeException {
        Long userId = UserContext.current().getCallerUserId();
        if (userId == null) {
            userId = User.UID_SYSTEM;
        }
        long templateId = command.getEntityId();
        Long volumeId = command.getVolumeId();
        Long snapshotId = command.getSnapshotId();
        SnapshotCommand cmd = null;        
        VMTemplateVO privateTemplate = null;
  
    	String uniqueName = getRandomPrivateTemplateName();

    	StoragePoolVO pool = null;
    	HostVO secondaryStorageHost = null;
    	long zoneId;
    	Long accountId = null;
    	
    	try {
    	    if (snapshotId != null) {  // create template from snapshot
                SnapshotVO snapshot = _snapshotDao.findById(snapshotId);
                if( snapshot == null ) {
                    throw new CloudRuntimeException("Unable to find Snapshot for Id " + snapshotId);
                }
                zoneId = snapshot.getDataCenterId();
                secondaryStorageHost = _storageMgr.getSecondaryStorageHost(zoneId);
                if ( secondaryStorageHost == null ) {
                    throw new CloudRuntimeException("Can not find the secondary storage for zoneId " + zoneId);
                }
                String secondaryStorageURL = secondaryStorageHost.getStorageUrl();

                String name = command.getTemplateName();
                String backupSnapshotUUID = snapshot.getBackupSnapshotId();
                if (backupSnapshotUUID == null) {
                    throw new CloudRuntimeException("Unable to create private template from snapshot " + snapshotId + " due to there is no backupSnapshotUUID for this snapshot");
                }
                
                Long dcId = snapshot.getDataCenterId();
                accountId = snapshot.getAccountId();
                volumeId = snapshot.getVolumeId();

                String origTemplateInstallPath = null;
                List<StoragePoolVO> storagePools = _storagePoolDao.listByDataCenterId(zoneId);
                if( storagePools == null || storagePools.size() == 0) {
                    throw new CloudRuntimeException("Unable to find storage pools in zone " + zoneId);
                }
                pool = storagePools.get(0);
                if (snapshot.getVersion() != null && snapshot.getVersion().equalsIgnoreCase("2.1")) {
                    VolumeVO volume = _volsDao.findByIdIncludingRemoved(volumeId);
                    if ( volume == null ) {
                        throw new CloudRuntimeException("failed to upgrade snapshot " + snapshotId + " due to unable to find orignal volume:" + volumeId + ", try it later ");
                    }
                    VMTemplateVO template = _templateDao.findByIdIncludingRemoved(volume.getTemplateId());
                    if ( template == null ) {
                        throw new CloudRuntimeException("failed to upgrade snapshot " + snapshotId + " due to unalbe to find orignal template :" + volume.getTemplateId() + ", try it later ");
                    }
                    Long origTemplateId = template.getId();
                    Long origTmpltAccountId = template.getAccountId();
                    if( ! _volsDao.lockInLockTable(volumeId.toString(), 10)) {       
                        throw new CloudRuntimeException("failed to upgrade snapshot " + snapshotId + " due to volume:" + volumeId + " is being used, try it later ");
                    }
                    cmd = new UpgradeSnapshotCommand(null, secondaryStorageURL, dcId, accountId,
                            volumeId, origTemplateId, origTmpltAccountId, null, snapshot.getBackupSnapshotId(), snapshot.getName(), "2.1" );
                    if( ! _volsDao.lockInLockTable(volumeId.toString(), 10)) {       
                        throw new CloudRuntimeException("Creating template failed due to volume:" + volumeId + " is being used, try it later ");
                    }
                    Answer answer = null;
                    try {
                        answer = _storageMgr.sendToPool(pool, cmd);
                        cmd = null;
                    } catch (StorageUnavailableException e) {
                    } finally {
                        _volsDao.unlockFromLockTable(volumeId.toString());
                    }
                    if ((answer != null) && answer.getResult()) {
                        _snapshotDao.updateSnapshotVersion(volumeId, "2.1", "2.2");
                    } else {
                        throw new CloudRuntimeException("Unable to upgrade snapshot");
                    }
                }
                cmd = new CreatePrivateTemplateFromSnapshotCommand(pool.getUuid(),
                        secondaryStorageURL, dcId, accountId, snapshot.getVolumeId(), backupSnapshotUUID, snapshot.getName(),
                        origTemplateInstallPath, templateId, name);
            } else if (volumeId != null) {           
                VolumeVO volume = _volsDao.findById(volumeId);
                if( volume == null ) {
                    throw new CloudRuntimeException("Unable to find volume for Id " + volumeId);
                }
                if( volume.getPoolId() == null ) {
                    _templateDao.remove(templateId);
                    throw new CloudRuntimeException("Volume " + volumeId + " is empty, can't create template on it");
                }
                String vmName = _storageMgr.getVmNameOnVolume(volume);
                zoneId = volume.getDataCenterId(); 
                secondaryStorageHost = _storageMgr.getSecondaryStorageHost(zoneId);
                if ( secondaryStorageHost == null ) {
                    throw new CloudRuntimeException("Can not find the secondary storage for zoneId " + zoneId);
                }
                String secondaryStorageURL = secondaryStorageHost.getStorageUrl();

                pool = _storagePoolDao.findById(volume.getPoolId());
                cmd = new CreatePrivateTemplateFromVolumeCommand(secondaryStorageURL, templateId, volume.getAccountId(),
                        command.getTemplateName(), uniqueName, volume.getPath(), vmName);

            } else {
                throw new CloudRuntimeException("Creating private Template need to specify snapshotId or volumeId");
            }
            // FIXME: before sending the command, check if there's enough capacity
            // on the storage server to create the template

            // This can be sent to a KVM host too.
            CreatePrivateTemplateAnswer answer = null;
            if( ! _volsDao.lockInLockTable(volumeId.toString(), 10)) {       
                throw new CloudRuntimeException("Creating template failed due to volume:" + volumeId + " is being used, try it later ");
            }
            try {
                answer = (CreatePrivateTemplateAnswer)_storageMgr.sendToPool(pool, cmd);
            } catch (StorageUnavailableException e) {
            } finally {
                _volsDao.unlockFromLockTable(volumeId.toString());
            }
            if ((answer != null) && answer.getResult()) {
                privateTemplate = _templateDao.findById(templateId);
                String answerUniqueName = answer.getUniqueName();
                if (answerUniqueName != null) {
                    privateTemplate.setUniqueName(answerUniqueName);
                } else {
                    privateTemplate.setUniqueName(uniqueName);
                }
                ImageFormat format = answer.getImageFormat();
                if (format != null) {
                    privateTemplate.setFormat(format);
                } else {
                    // This never occurs.
                    // Specify RAW format makes it unusable for snapshots.
                    privateTemplate.setFormat(ImageFormat.RAW);
                }
                            
                _templateDao.update(templateId, privateTemplate);

                // add template zone ref for this template
                _templateDao.addTemplateToZone(privateTemplate, zoneId);
                VMTemplateHostVO templateHostVO = new VMTemplateHostVO(secondaryStorageHost.getId(), templateId);
                templateHostVO.setDownloadPercent(100);
                templateHostVO.setDownloadState(Status.DOWNLOADED);
                templateHostVO.setInstallPath(answer.getPath());
                templateHostVO.setLastUpdated(new Date());
                templateHostVO.setSize(answer.getVirtualSize());
                templateHostVO.setPhysicalSize(answer.getphysicalSize());
                _templateHostDao.persist(templateHostVO);

                UsageEventVO usageEvent = new UsageEventVO(EventTypes.EVENT_TEMPLATE_CREATE, privateTemplate.getAccountId(), secondaryStorageHost.getDataCenterId(), privateTemplate.getId(), privateTemplate.getName(), null, null , templateHostVO.getSize());
                _usageEventDao.persist(usageEvent);
                
                

            } 
    	} finally {
    	    if (privateTemplate == null) {
    	        Transaction txn = Transaction.currentTxn();
                txn.start();
                // Remove the template record
                _templateDao.remove(templateId);
                
                //decrement resource count
                _accountMgr.decrementResourceCount(accountId, ResourceType.template);
                txn.commit();
    	    }
    	}
    	
        return privateTemplate;
    }

    //used for vm transitioning to error state
	private void updateVmStateForFailedVmCreation(Long vmId) {
		UserVmVO vm = _vmDao.findById(vmId);
		if(vm != null){
			if(vm.getState().equals(State.Stopped)){
				_itMgr.stateTransitTo(vm, VirtualMachine.Event.OperationFailed, null);
				//destroy associated volumes for vm in error state
				List<VolumeVO> volumesForThisVm = _volsDao.findByInstance(vm.getId());
				for(VolumeVO volume : volumesForThisVm) {
				    try {
				        if (volume.getState() != Volume.State.Destroy) {
				            _storageMgr.destroyVolume(volume);
				        } 
                        if (volume.getState() == Volume.State.Ready) {
                            UsageEventVO usageEvent = new UsageEventVO(EventTypes.EVENT_VOLUME_DELETE, volume.getAccountId(), volume.getDataCenterId(), volume.getId(),
                                    volume.getName());
                            _usageEventDao.persist(usageEvent);
                        }
                    } catch (ConcurrentOperationException e) {
                        s_logger.warn("Unable to delete volume:"+volume.getId()+" for vm:"+vmId+" whilst transitioning to error state");
                    }
				}
				UsageEventVO usageEvent = new UsageEventVO(EventTypes.EVENT_VM_DESTROY, vm.getAccountId(), vm.getDataCenterId(), vm.getId(), vm.getHostName());
	            _usageEventDao.persist(usageEvent);
			}
		}
	}
    
	protected class ExpungeTask implements Runnable {
    	public ExpungeTask() {
    	}

		@Override
        public void run() {
			GlobalLock scanLock = GlobalLock.getInternLock("UserVMExpunge");
			try {
				if (scanLock.lock(ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_COOPERATION)) {
					try {
		                List<UserVmVO> vms = _vmDao.findDestroyedVms(new Date(System.currentTimeMillis() - ((long)_expungeDelay << 10)));
		                if (s_logger.isInfoEnabled()) {
		                    if (vms.size() == 0) {
		                        s_logger.trace("Found " + vms.size() + " vms to expunge.");
		                    } else {
		                        s_logger.info("Found " + vms.size() + " vms to expunge.");
		                    }
		                }
		                for (UserVmVO vm : vms) {
		                    try {
		                        expunge(vm, _accountMgr.getSystemUser().getId(), _accountMgr.getSystemAccount());
		                    } catch(Exception e) {
		                        s_logger.warn("Unable to expunge " + vm, e);
		                    }
		                }
		            } catch (Exception e) {
		                s_logger.error("Caught the following Exception", e);
					} finally {
						scanLock.unlock();
					}
				}
			} finally {
				scanLock.releaseRef();
			}
		}
    }

	private static boolean isAdmin(short accountType) {
	    return ((accountType == Account.ACCOUNT_TYPE_ADMIN) ||
	            (accountType == Account.ACCOUNT_TYPE_DOMAIN_ADMIN) ||
	            (accountType == Account.ACCOUNT_TYPE_READ_ONLY_ADMIN));
	}
	
	@Override @ActionEvent (eventType=EventTypes.EVENT_VM_UPDATE, eventDescription="updating Vm")
    public UserVm updateVirtualMachine(UpdateVMCmd cmd) {
        String displayName = cmd.getDisplayName();
        String group = cmd.getGroup();
        Boolean ha = cmd.getHaEnable();
        Long id = cmd.getId();
        Long osTypeId = cmd.getOsTypeId();
        Account account = UserContext.current().getCaller();
        Long userId = UserContext.current().getCallerUserId();
    
        //Input validation
        UserVmVO vmInstance = null;

        // Verify input parameters
    	vmInstance = _vmDao.findById(id.longValue());
        
        if (vmInstance == null) {
            throw new InvalidParameterValueException("unable to find virtual machine with id " + id);
        }
        
        ServiceOffering offering = _configMgr.getServiceOffering(vmInstance.getServiceOfferingId());
        if (!offering.getOfferHA() && ha != null && ha) {
            throw new InvalidParameterValueException("Can't enable ha for the vm as it's created from the Service offering having HA disabled");
        }

        userId = accountAndUserValidation(id, account, userId,vmInstance);  
        
    	if (displayName == null) {
    		displayName = vmInstance.getDisplayName();
    	}

    	if (ha == null) {
    		ha = vmInstance.isHaEnabled();
    	}
    	
        UserVmVO vm = _vmDao.findById(id);
        if (vm == null) {
            throw new CloudRuntimeException("Unable to find virual machine with id " + id);
        }
        
        if (vm.getState() == State.Error || vm.getState() == State.Expunging) {
            s_logger.error("vm is not in the right state: " + id);
            throw new InvalidParameterValueException("Vm with id " + id + " is not in the right state");
        }

        String description = "";
        
        if(displayName != vmInstance.getDisplayName()){
            description += "New display name: "+displayName+". ";
        }
        
        if(ha != vmInstance.isHaEnabled()){
            if(ha){
                description += "Enabled HA. ";
            } else {
                description += "Disabled HA. ";
            }
        }
        if (osTypeId == null) {
            osTypeId = vmInstance.getGuestOSId();
        } else {
            description += "Changed Guest OS Type to " + osTypeId + ". ";
        }
        
        if (group != null) {
            if(addInstanceToGroup(id, group)){
                description += "Added to group: "+group+".";
            }
        }

        _vmDao.updateVM(id, displayName, ha, osTypeId);

        return _vmDao.findById(id);
    }

	@Override @ActionEvent (eventType=EventTypes.EVENT_VM_START, eventDescription="starting Vm", async=true)
	public UserVm startVirtualMachine(StartVMCmd cmd) throws ExecutionException, ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
	    return startVirtualMachine(cmd.getId());
	}

	@Override @ActionEvent (eventType=EventTypes.EVENT_VM_REBOOT, eventDescription="rebooting Vm", async=true)
	public UserVm rebootVirtualMachine(RebootVMCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException{
        Account account = UserContext.current().getCaller();
        Long userId = UserContext.current().getCallerUserId();
        Long vmId = cmd.getId();
        
        //Verify input parameters
        UserVmVO vmInstance = _vmDao.findById(vmId.longValue());
        if (vmInstance == null) {
        	throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        }

        userId = accountAndUserValidation(vmId, account, userId, vmInstance);
        
        return rebootVirtualMachine(userId, vmId);
	}

	@Override @ActionEvent (eventType=EventTypes.EVENT_VM_DESTROY, eventDescription="destroying Vm", async=true)
	public UserVm destroyVm(DestroyVMCmd cmd) throws ResourceUnavailableException, ConcurrentOperationException {
	    return destroyVm(cmd.getId());
	}

    @Override @DB
    public InstanceGroupVO createVmGroup(CreateVMGroupCmd cmd) throws InvalidParameterValueException, PermissionDeniedException {
        Account account = UserContext.current().getCaller();
        Long domainId = cmd.getDomainId();
        String accountName = cmd.getAccountName();
        Long accountId = null;
        String groupName = cmd.getGroupName();
        
        if (account == null) {
            account = _accountDao.findById(1L);
        }

        if (account != null) {
            if (isAdmin(account.getType())) {
                if ((domainId != null) && (accountName != null)) {
                    if (!_domainDao.isChildDomain(account.getDomainId(), domainId)) {
                        throw new PermissionDeniedException("Unable to create vm group in domain " + domainId + ", permission denied.");
                    }

                    Account userAccount = _accountDao.findActiveAccount(accountName, domainId);
                    if (userAccount != null) {
                        accountId = userAccount.getId();
                    } else {
                        throw new InvalidParameterValueException("Failed to create vm group " + groupName + ", unable to find account " + accountName + " in domain " + domainId);
                    }
                } else {
                    // the admin must be creating the vm group
                    accountId = account.getId();
                }
            } else {
                accountId = account.getId();
            }
        }

        if (accountId == null) {
            throw new InvalidParameterValueException("Failed to create vm group " + groupName + ", unable to find account for which to create a group.");
        }

        //Check if name is already in use by this account
        boolean isNameInUse = _vmGroupDao.isNameInUse(accountId, groupName);

        if (isNameInUse) {
            throw new InvalidParameterValueException("Unable to create vm group, a group with name " + groupName + " already exisits for account " + accountId);
        }

        return createVmGroup(groupName, accountId);
    }

    @DB
	private InstanceGroupVO createVmGroup(String groupName, long accountId) {
        Account account = null;
	    final Transaction txn = Transaction.currentTxn();
		txn.start();
		try {
			account = _accountDao.acquireInLockTable(accountId); //to ensure duplicate vm group names are not created.
			if (account == null) {
				s_logger.warn("Failed to acquire lock on account");
				return null;
			}
			InstanceGroupVO group = _vmGroupDao.findByAccountAndName(accountId, groupName);
			if (group == null){
				group = new InstanceGroupVO(groupName, accountId);
				group =  _vmGroupDao.persist(group);
			}
			return group;
		} finally {
			if (account != null) {
				_accountDao.releaseFromLockTable(accountId);
			}
			txn.commit();
		}
    }

    @Override
    public boolean deleteVmGroup(DeleteVMGroupCmd cmd) throws InvalidParameterValueException, PermissionDeniedException {
        Account account = UserContext.current().getCaller();
        Long groupId = cmd.getId();

        // Verify input parameters
        InstanceGroupVO group = _vmGroupDao.findById(groupId);
        if ((group == null) || (group.getRemoved() != null)) {
            throw new InvalidParameterValueException("unable to find a vm group with id " + groupId);
        }

        if (account != null) {
            Account tempAccount = _accountDao.findById(group.getAccountId());
            if (!isAdmin(account.getType()) && (account.getId() != group.getAccountId())) {
                throw new PermissionDeniedException("unable to find a group with id " + groupId);
            } else if (!_domainDao.isChildDomain(account.getDomainId(), tempAccount.getDomainId())) {
                throw new PermissionDeniedException("Invalid group id (" + groupId + ") given, unable to update the group.");
            }
        }

        return deleteVmGroup(groupId);
    }

    @Override
    public boolean deleteVmGroup(long groupId) {    	
    	//delete all the mappings from group_vm_map table
        List<InstanceGroupVMMapVO> groupVmMaps = _groupVMMapDao.listByGroupId(groupId);
        for (InstanceGroupVMMapVO groupMap : groupVmMaps) {
	        SearchCriteria<InstanceGroupVMMapVO> sc = _groupVMMapDao.createSearchCriteria();
	        sc.addAnd("instanceId", SearchCriteria.Op.EQ, groupMap.getInstanceId());
	        _groupVMMapDao.expunge(sc);
        }
    	
    	if (_vmGroupDao.remove(groupId)) {
    		return true;
    	} else {
    		return false;
    	}
    }

	@Override @DB
	public boolean addInstanceToGroup(long userVmId, String groupName) {		
		UserVmVO vm = _vmDao.findById(userVmId);

        InstanceGroupVO group = _vmGroupDao.findByAccountAndName(vm.getAccountId(), groupName);
    	//Create vm group if the group doesn't exist for this account
        if (group == null) {
        	group = createVmGroup(groupName, vm.getAccountId());
        }

		if (group != null) {
			final Transaction txn = Transaction.currentTxn();
			txn.start();
			UserVm userVm = _vmDao.acquireInLockTable(userVmId);
			if (userVm == null) {
				s_logger.warn("Failed to acquire lock on user vm id=" + userVmId);
			}
			try {
				//don't let the group be deleted when we are assigning vm to it.
				InstanceGroupVO ngrpLock = _vmGroupDao.lockRow(group.getId(), false);
				if (ngrpLock == null) {
					s_logger.warn("Failed to acquire lock on vm group id=" + group.getId() + " name=" + group.getName());
					txn.rollback();
					return false;
				}
				
				//Currently don't allow to assign a vm to more than one group
				if (_groupVMMapDao.listByInstanceId(userVmId) != null) {
					//Delete all mappings from group_vm_map table
			        List<InstanceGroupVMMapVO> groupVmMaps = _groupVMMapDao.listByInstanceId(userVmId);
			        for (InstanceGroupVMMapVO groupMap : groupVmMaps) {
				        SearchCriteria<InstanceGroupVMMapVO> sc = _groupVMMapDao.createSearchCriteria();
				        sc.addAnd("instanceId", SearchCriteria.Op.EQ, groupMap.getInstanceId());
				        _groupVMMapDao.expunge(sc);
			        }
				}
				InstanceGroupVMMapVO groupVmMapVO = new InstanceGroupVMMapVO(group.getId(), userVmId);
				_groupVMMapDao.persist(groupVmMapVO);
				
				txn.commit();
				return true;
			} finally {
				if (userVm != null) {
					_vmDao.releaseFromLockTable(userVmId);
				}
			}
	    }
		return false;
	}
	
	@Override
	public InstanceGroupVO getGroupForVm(long vmId) {
		//TODO - in future releases vm can be assigned to multiple groups; but currently return just one group per vm
		try {
			List<InstanceGroupVMMapVO> groupsToVmMap =  _groupVMMapDao.listByInstanceId(vmId);

            if(groupsToVmMap != null && groupsToVmMap.size() != 0){
            	InstanceGroupVO group = _vmGroupDao.findById(groupsToVmMap.get(0).getGroupId());
            	return group;
            } else {
            	return null;
            }
		}
		catch (Exception e){
			s_logger.warn("Error trying to get group for a vm: "+e);
			return null;
		}
	}
	
	@Override
	public void removeInstanceFromInstanceGroup(long vmId) {
		try {
			List<InstanceGroupVMMapVO> groupVmMaps = _groupVMMapDao.listByInstanceId(vmId);
	        for (InstanceGroupVMMapVO groupMap : groupVmMaps) {
		        SearchCriteria<InstanceGroupVMMapVO> sc = _groupVMMapDao.createSearchCriteria();
		        sc.addAnd("instanceId", SearchCriteria.Op.EQ, groupMap.getInstanceId());
		        _groupVMMapDao.expunge(sc);
	        }
		} catch (Exception e){
			s_logger.warn("Error trying to remove vm from group: "+e);
		}
	}
	
    protected boolean validPassword(String password) {
        if (password == null || password.length() == 0) {
            return false;
        }
        for (int i = 0; i < password.length(); i++) {
            if (password.charAt(i) == ' ') {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public UserVm createBasicSecurityGroupVirtualMachine(DataCenter zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, List<Long> securityGroupIdList, 
                                            Account owner, String hostName, String displayName, Long diskOfferingId, 
                                            Long diskSize, String group, HypervisorType hypervisor, String userData, String sshKeyPair, Host destinationHost) 
                                            throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException, StorageUnavailableException, ResourceAllocationException {
        
        Account caller = UserContext.current().getCaller();
        List<NetworkVO> networkList = new ArrayList<NetworkVO>();
        
        //Verify that caller can perform actions in behalf of vm owner
        _accountMgr.checkAccess(caller, owner);
        
        //Get default guest network in Basic zone
        Network defaultNetwork = _networkMgr.getSystemNetworkByZoneAndTrafficType(zone.getId(), TrafficType.Guest);
        
        if (defaultNetwork == null) {
            throw new InvalidParameterValueException("Unable to find a default network to start a vm");
        } else {
            networkList.add(_networkDao.findById(defaultNetwork.getId()));
        }
        
        boolean isVmWare = (template.getHypervisorType() == HypervisorType.VMware || (hypervisor != null && hypervisor == HypervisorType.VMware));
        
        if (securityGroupIdList != null && isVmWare) {
            throw new InvalidParameterValueException("Security group feature is not supported for vmWare hypervisor");
        } else if (securityGroupIdList == null && !isVmWare) {
            securityGroupIdList = new ArrayList<Long>();
            SecurityGroup defaultGroup = _securityGroupMgr.getDefaultSecurityGroup(owner.getId());
            if (defaultGroup != null) {
              //check if security group id list already contains Default security group, and if not - add it
                boolean defaultGroupPresent = false;
                for (Long securityGroupId : securityGroupIdList) {
                    if (securityGroupId.longValue() == defaultGroup.getId()) {
                        defaultGroupPresent = true;
                        break;
                    }
                }
                
                if (!defaultGroupPresent) {
                    securityGroupIdList.add(defaultGroup.getId());
                }
              
            } else {
                //create default security group for the account
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Couldn't find default security group for the account " + owner + " so creating a new one");
                }
                defaultGroup = _securityGroupMgr.createSecurityGroup(SecurityGroupManager.DEFAULT_GROUP_NAME, SecurityGroupManager.DEFAULT_GROUP_DESCRIPTION, owner.getDomainId(), owner.getId(), owner.getAccountName());
                securityGroupIdList.add(defaultGroup.getId());
            }
        }
        
        return createVirtualMachine(zone, serviceOffering, template, hostName, displayName, owner, diskOfferingId, 
                                    diskSize, networkList, securityGroupIdList, group, userData, sshKeyPair, hypervisor, caller, destinationHost);
    }
    
    
    @Override
    public UserVm createAdvancedSecurityGroupVirtualMachine(DataCenter zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, List<Long> networkIdList, List<Long> securityGroupIdList, 
            Account owner, String hostName, String displayName, Long diskOfferingId, Long diskSize, String group, 
            HypervisorType hypervisor, String userData, String sshKeyPair, Host destinationHost) 
            throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException, StorageUnavailableException, ResourceAllocationException {
        
        Account caller = UserContext.current().getCaller();
        List<NetworkVO> networkList = new ArrayList<NetworkVO>();
        boolean isSecurityGroupEnabledNetworkUsed = false;
        boolean isVmWare = (template.getHypervisorType() == HypervisorType.VMware || (hypervisor != null && hypervisor == HypervisorType.VMware));
        
        //Verify that caller can perform actions in behalf of vm owner
        _accountMgr.checkAccess(caller, owner);
        
        //If no network is specified, find system security group enabled network
        if (networkIdList == null || networkIdList.isEmpty()) {
            NetworkVO networkWithSecurityGroup = _networkMgr.getNetworkWithSecurityGroupEnabled(zone.getId());
            if (networkWithSecurityGroup == null) {
                throw new InvalidParameterValueException("No network with security enabled is found in zone id=" + zone.getId());
            }
            
            networkList.add(networkWithSecurityGroup);

        } else if (securityGroupIdList != null && !securityGroupIdList.isEmpty()) {
            if (isVmWare) {
                throw new InvalidParameterValueException("Security group feature is not supported for vmWare hypervisor");
            }
            // Only one network can be specified, and it should be security group enabled
            if (networkIdList.size() > 1) {
                throw new InvalidParameterValueException("Only support one network per VM if security group enabled");
            }
      
            NetworkVO network = _networkDao.findById(networkIdList.get(0).longValue());
            
            if (network == null) {
                throw new InvalidParameterValueException("Unable to find network by id " + networkIdList.get(0).longValue());
            }
            
            if (!network.isSecurityGroupEnabled()) {
                throw new InvalidParameterValueException("Network is not security group enabled: " + network.getId());  
            }
            
            networkList.add(network);
            isSecurityGroupEnabledNetworkUsed = true;
            
        } else {
            //Verify that all the networks are Direct/Guest/AccountSpecific; can't create combination of SG enabled network and regular networks
            for (Long networkId : networkIdList) {
                NetworkVO network = _networkDao.findById(networkId);
                
                if (network == null) {
                    throw new InvalidParameterValueException("Unable to find network by id " + networkIdList.get(0).longValue());
                }
                
                if (network.isSecurityGroupEnabled() && networkIdList.size() > 1) {
                    throw new InvalidParameterValueException("Can't create a vm with multiple networks one of which is Security Group enabled");
                }
                
                if (network.getTrafficType() != TrafficType.Guest || network.getGuestType() != GuestIpType.Direct || (network.getIsShared() && !network.isSecurityGroupEnabled())) {
                    throw new InvalidParameterValueException("Can specify only Direct Guest Account specific networks when deploy vm in Security Group enabled zone");
                }
                
                //Perform account permission check
                if (!network.getIsShared()) {
                    //Check account permissions
                    List<NetworkVO> networkMap = _networkDao.listBy(owner.getId(), network.getId());
                    if (networkMap == null || networkMap.isEmpty()) {
                        throw new PermissionDeniedException("Unable to create a vm using network with id " + network.getId() + ", permission denied");
                    }
                } 
                
                networkList.add(network);
            }
        }
        
        // if network is security group enabled, and default security group is not present in the list of groups specified, add it automatically
        if (isSecurityGroupEnabledNetworkUsed && !isVmWare) {
            if (securityGroupIdList == null) {
                securityGroupIdList = new ArrayList<Long>();
            }
            
            SecurityGroup defaultGroup = _securityGroupMgr.getDefaultSecurityGroup(owner.getId());
            if (defaultGroup != null) {
              //check if security group id list already contains Default security group, and if not - add it
                boolean defaultGroupPresent = false;
                for (Long securityGroupId : securityGroupIdList) {
                    if (securityGroupId.longValue() == defaultGroup.getId()) {
                        defaultGroupPresent = true;
                        break;
                    }
                }
                
                if (!defaultGroupPresent) {
                    securityGroupIdList.add(defaultGroup.getId());
                }
              
            } else {
                //create default security group for the account
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Couldn't find default security group for the account " + owner + " so creating a new one");
                }
                defaultGroup = _securityGroupMgr.createSecurityGroup(SecurityGroupManager.DEFAULT_GROUP_NAME, SecurityGroupManager.DEFAULT_GROUP_DESCRIPTION, owner.getDomainId(), owner.getId(), owner.getAccountName());
                securityGroupIdList.add(defaultGroup.getId());
            }
        }
        
        return createVirtualMachine(zone, serviceOffering, template, hostName, displayName, owner, diskOfferingId, 
                diskSize, networkList, securityGroupIdList, group, userData, sshKeyPair, hypervisor, caller, destinationHost);
    }
    
    
    @Override
    public UserVm createAdvancedVirtualMachine(DataCenter zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, List<Long> networkIdList, Account owner, String hostName, 
            String displayName, Long diskOfferingId, Long diskSize, String group, HypervisorType hypervisor, String userData, String sshKeyPair, Host destinationHost) 
            throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException, StorageUnavailableException, ResourceAllocationException {
        
        Account caller = UserContext.current().getCaller();
        List<NetworkVO> networkList = new ArrayList<NetworkVO>();
        
        //Verify that caller can perform actions in behalf of vm owner
        _accountMgr.checkAccess(caller, owner);
        
        if (networkIdList == null || networkIdList.isEmpty()) {
            NetworkVO defaultNetwork = null;
            
            //if no network is passed in
            //1) Check if default virtual network offering has Availability=Required. If it's true, search for corresponding network
            //   * if network is found, use it. If more than 1 virtual network is found, throw an error
            //   * if network is not found, create a new one and use it
            //2) If Availability=Optional, search for default networks for the account. If it's more than 1, throw an error. 
            //   If it's 0, and there are no default direct networks, create default Guest Virtual network
            
            List<NetworkOfferingVO> defaultVirtualOffering = _networkOfferingDao.listByTrafficTypeAndGuestType(false, TrafficType.Guest, GuestIpType.Virtual);
            
            if (defaultVirtualOffering.get(0).getAvailability() == Availability.Required) {
                //get Virtual netowrks
                List<NetworkVO> virtualNetworks = _networkMgr.listNetworksForAccount(owner.getId(), zone.getId(), GuestIpType.Virtual, true);
                
                if (virtualNetworks.isEmpty()) {
                    s_logger.debug("Creating default Virtual network for account " + owner + " as a part of deployVM process");
                    Network newNetwork = _networkMgr.createNetwork(defaultVirtualOffering.get(0).getId(), owner.getAccountName() + "-network", owner.getAccountName() + "-network", false, null, zone.getId(), null, null, null, null, owner, false, null);
                    defaultNetwork = _networkDao.findById(newNetwork.getId());
                } else if (virtualNetworks.size() > 1) {
                    throw new InvalidParameterValueException("More than 1 default Virtaul networks are found for account " + owner + "; please specify networkIds");
                } else {
                    defaultNetwork = virtualNetworks.get(0);
                }
            } else {
                List<NetworkVO> defaultNetworks = _networkMgr.listNetworksForAccount(owner.getId(), zone.getId(), null, true);
                if (defaultNetworks.isEmpty()) {
                    if (defaultVirtualOffering.get(0).getAvailability() == Availability.Optional) {
                        s_logger.debug("Creating default Virtual network for account " + owner + " as a part of deployVM process");
                        Network newNetwork = _networkMgr.createNetwork(defaultVirtualOffering.get(0).getId(), owner.getAccountName() + "-network", owner.getAccountName() + "-network", false, null, zone.getId(), null, null, null, null, owner, false, null);
                        defaultNetwork = _networkDao.findById(newNetwork.getId());
                    } else {
                        throw new InvalidParameterValueException("Unable to find default networks for account " + owner);
                    }
                    
                } else if (defaultNetworks.size() > 1) {
                    throw new InvalidParameterValueException("More than 1 default network is found for accoun " + owner);
                } else {
                    defaultNetwork = defaultNetworks.get(0);
                }
            }
            
            //Check that network offering doesn't have Availability=Unavailable
            NetworkOffering networkOffering = _configMgr.getNetworkOffering(defaultNetwork.getNetworkOfferingId());
            
            if (networkOffering.getAvailability() == Availability.Unavailable) {
                throw new InvalidParameterValueException("Unable to find default network; please specify networkOfferingIds");
            }
            
            networkList.add(defaultNetwork);
            
        } else {
            
            boolean requiredNetworkOfferingIsPresent = false;
            List<NetworkOfferingVO> requiredOfferings = _networkOfferingDao.listByAvailability(Availability.Required, false);
            Long requiredOfferingId = null;
            
            if (!requiredOfferings.isEmpty()) {
                //in 2.2.x there can be only one required offering - default Virtual
                requiredOfferingId = requiredOfferings.get(0).getId();
            }
            
            for (Long networkId : networkIdList) {         
                NetworkVO network = _networkDao.findById(networkId);
                if (network == null) {
                    throw new InvalidParameterValueException("Unable to find network by id " + networkIdList.get(0).longValue());
                }
                
                //Perform account permission check
                if (!network.getIsShared()) {
                    List<NetworkVO> networkMap = _networkDao.listBy(owner.getId(), network.getId());
                    if (networkMap == null || networkMap.isEmpty()) {
                        throw new PermissionDeniedException("Unable to create a vm using network with id " + network.getId() + ", permission denied");
                    }
                } else {
                    if (!_networkMgr.isNetworkAvailableInDomain(networkId, owner.getDomainId())) {
                        throw new PermissionDeniedException("Shared network id=" + networkId + " is not available in domain id=" + owner.getDomainId());
                    }
                }
                
                //check that corresponding offering is available
                NetworkOffering networkOffering = _configMgr.getNetworkOffering(network.getNetworkOfferingId());
                
                if (networkOffering.getAvailability() == Availability.Unavailable) {
                    throw new InvalidParameterValueException("Network id=" + network.getId() + " can't be used; corresponding network offering is " + Availability.Unavailable);
                }
                
                if (requiredOfferingId != null && network.getNetworkOfferingId() == requiredOfferingId.longValue()) {
                    requiredNetworkOfferingIsPresent = true;
                }
                
                networkList.add(network);
            }
            
            //If default Virtual network offering is Required, it has to be specified in the network list
            if (requiredOfferingId != null && !requiredNetworkOfferingIsPresent) {
                throw new InvalidParameterValueException("Network created from the network offering id=" + requiredOfferingId + " is required; change network offering availability to be Optional to relax this requirement");
            }
        }
        
        return createVirtualMachine(zone, serviceOffering, template, hostName, displayName, owner, diskOfferingId, 
                diskSize, networkList, null, group, userData, sshKeyPair, hypervisor, caller, destinationHost);
    }
    
    
    
	@DB @ActionEvent (eventType=EventTypes.EVENT_VM_CREATE, eventDescription="deploying Vm", create=true)
    protected UserVm createVirtualMachine(DataCenter zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, String hostName, String displayName, Account owner, Long diskOfferingId, 
                                        Long diskSize, List<NetworkVO> networkList, List<Long> securityGroupIdList, String group, String userData, String sshKeyPair, HypervisorType hypervisor, Account caller, Host destinationHost) 
	                                    throws InsufficientCapacityException, ResourceUnavailableException, ConcurrentOperationException, StorageUnavailableException, ResourceAllocationException {
        
        _accountMgr.checkAccess(caller, owner);
        long accountId = owner.getId();
        
    	if(destinationHost != null && !_accountMgr.isRootAdmin(caller.getType())){
    		throw new PermissionDeniedException("Destination Host can only be specified by a Root Admin, permission denied");
    	}
		
		if(Grouping.AllocationState.Disabled == zone.getAllocationState() && !_accountMgr.isRootAdmin(caller.getType())){
			throw new PermissionDeniedException("Cannot perform this operation, Zone is currently disabled: "+ zone.getId() );
		}
        
        if (zone.getDomainId() != null) {
            DomainVO domain = _domainDao.findById(zone.getDomainId());
            if (domain == null) {
                throw new CloudRuntimeException("Unable to find the domain " + zone.getDomainId() + " for the zone: " + zone);
            }
            //check that caller can operate with domain
            _accountMgr.checkAccess(caller, domain);
            //check that vm owner can create vm in the domain
            _accountMgr.checkAccess(owner, domain);
        }

        //check if account/domain is with in resource limits to create a new vm
        if (_accountMgr.resourceLimitExceeded(owner, ResourceType.user_vm))
        {
        	ResourceAllocationException rae = new ResourceAllocationException("Maximum number of virtual machines for account: " + owner.getAccountName() + " has been exceeded.");
        	rae.setResourceType("vm");
        	throw rae;	
        }
        
        //verify security group ids
        if (securityGroupIdList != null) {
            for (Long securityGroupId : securityGroupIdList) {
                if (_securityGroupDao.findById(securityGroupId) == null) {
                    throw new InvalidParameterValueException("Unable to find security group by id " + securityGroupId);
                }
            }
        }

        // check if we have available pools for vm deployment
        List<StoragePoolVO> availablePools = _storagePoolDao.listPoolsByStatus(StoragePoolStatus.Up);
        
        if( availablePools == null || availablePools.size() < 1) {
        	throw new StorageUnavailableException("There are no available pools in the UP state for vm deployment",-1);
        }
        
        ServiceOfferingVO offering = _serviceOfferingDao.findById(serviceOffering.getId());
        
        if (template.getTemplateType().equals(TemplateType.SYSTEM)) {
        	throw new InvalidParameterValueException("Unable to use system template " + template.getId() + " to deploy a user vm");
        }
        List<VMTemplateZoneVO> listZoneTemplate = _templateZoneDao.listByZoneTemplate(zone.getId(),template.getId());
        if (listZoneTemplate==null || listZoneTemplate.isEmpty()){
              	throw new InvalidParameterValueException("The template " +template.getId()+ " is not available for use");
        }   
        boolean isIso = Storage.ImageFormat.ISO == template.getFormat();
        if (isIso && !template.isBootable()) {
            throw new InvalidParameterValueException("Installing from ISO requires an ISO that is bootable: " + template.getId());
        }
        
        // Check templates permissions
        if (!template.isPublicTemplate()) {
            Account templateOwner = _accountMgr.getAccount(template.getAccountId());
            _accountMgr.checkAccess(owner, templateOwner);
        }
        
        // If the template represents an ISO, a disk offering must be passed in, and will be used to create the root disk
        // Else, a disk offering is optional, and if present will be used to create the data disk
        Pair<DiskOfferingVO, Long> rootDiskOffering = new Pair<DiskOfferingVO, Long>(null, null);
        List<Pair<DiskOfferingVO, Long>> dataDiskOfferings = new ArrayList<Pair<DiskOfferingVO, Long>>();
        
        if (isIso) {
            if (diskOfferingId == null) {
                throw new InvalidParameterValueException("Installing from ISO requires a disk offering to be specified for the root disk.");
            }
            DiskOfferingVO diskOffering = _diskOfferingDao.findById(diskOfferingId);
            if (diskOffering == null) {
                throw new InvalidParameterValueException("Unable to find disk offering " + diskOfferingId);
            }
            Long size = null;
            if (diskOffering.getDiskSize() == 0) {
                size = diskSize;
                if (size == null) {
                    throw new InvalidParameterValueException("Disk offering " + diskOffering + " requires size parameter.");
                }
            }
            rootDiskOffering.first(diskOffering);
            rootDiskOffering.second(size);
        } else {
            rootDiskOffering.first(offering);
            if (diskOfferingId != null) {
                DiskOfferingVO diskOffering = _diskOfferingDao.findById(diskOfferingId);
                if (diskOffering == null) {
                    throw new InvalidParameterValueException("Unable to find disk offering " + diskOfferingId);
                }
                Long size = null;
                if (diskOffering.getDiskSize() == 0) {
                    size = diskSize;
                    if (size == null) {
                        throw new InvalidParameterValueException("Disk offering " + diskOffering + " requires size parameter.");
                    }
                }
                dataDiskOfferings.add(new Pair<DiskOfferingVO, Long>(diskOffering, size));
            }
        }

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
        if (sshKeyPair != null && !sshKeyPair.equals("")) {
            Account account = UserContext.current().getCaller();
        	SSHKeyPair pair = _sshKeyPairDao.findByName(account.getAccountId(), account.getDomainId(), sshKeyPair);
    		if (pair == null) {
                throw new InvalidParameterValueException("A key pair with name '" + sshKeyPair + "' was not found.");
            }
    		
    		sshPublicKey = pair.getPublicKey();
        }
        
        _accountMgr.checkAccess(caller, template);
        
        DataCenterDeployment plan = null;
        if(destinationHost != null){
        	s_logger.debug("Destination Host to deploy the VM is specified, adding it to the deployment plan");
        	plan = new DataCenterDeployment(zone.getId(), destinationHost.getPodId(), destinationHost.getClusterId(), destinationHost.getId(), null);
        }else{
        	plan = new DataCenterDeployment(zone.getId());
        }
        
        s_logger.debug("Allocating in the DB for vm");
          
        List<Pair<NetworkVO, NicProfile>> networks = new ArrayList<Pair<NetworkVO, NicProfile>>();
        short defaultNetworkNumber = 0;
        for (NetworkVO network : networkList) {
            
            if (network.isDefault()) {
                defaultNetworkNumber++;
            }
            
            networks.add(new Pair<NetworkVO, NicProfile>(network, null));
        }
        
        //Verify network information - network default network has to be set; and vm can't have more than one default network
        //This is a part of business logic because default network is required by Agent Manager in order to configure default gateway for the vm
        if (defaultNetworkNumber == 0) {
            throw new InvalidParameterValueException("At least 1 default network has to be specified for the vm");
        } else if (defaultNetworkNumber >1) {
            throw new InvalidParameterValueException("Only 1 default network per vm is supported");
        }
        
        long id = _vmDao.getNextInSequence(Long.class, "id");
        
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
        
        HypervisorType hypervisorType = null;
        if (template == null || template.getHypervisorType() == null || template.getHypervisorType() == HypervisorType.None) {
            hypervisorType = hypervisor;
        } else {
            hypervisorType = template.getHypervisorType();
        }
        
        UserVmVO vm = new UserVmVO(id, instanceName, displayName, template.getId(), hypervisorType,
                                   template.getGuestOSId(), offering.getOfferHA(), owner.getDomainId(), owner.getId(), offering.getId(), userData, hostName);

        if (sshPublicKey != null) {
            vm.setDetail("SSH.PublicKey", sshPublicKey);
        }
        
        if (isIso) {
            vm.setIsoId(template.getId());
        }

        if (_itMgr.allocate(vm, _templateDao.findById(template.getId()), offering, rootDiskOffering, dataDiskOfferings, networks, null, plan, hypervisorType, owner) == null) {
            return null;
        }
        
        _vmDao.saveDetails(vm);
        
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Successfully allocated DB entry for " + vm);
        }
        UserContext.current().setEventDetails("Vm Id: "+vm.getId());
        UsageEventVO usageEvent = new UsageEventVO(EventTypes.EVENT_VM_CREATE, accountId, zone.getId(), vm.getId(), vm.getHostName(), offering.getId(), template.getId(), hypervisorType.toString());
        _usageEventDao.persist(usageEvent);
        
        _accountMgr.incrementResourceCount(accountId, ResourceType.user_vm);
        
        //Assign instance to the group
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
        
        _securityGroupMgr.addInstanceToGroups(vm.getId(), securityGroupIdList);
        
        return vm;
	}
	
	@Override @ActionEvent (eventType=EventTypes.EVENT_VM_CREATE, eventDescription="starting Vm", async=true)
	public UserVm startVirtualMachine(DeployVMCmd cmd) throws ResourceUnavailableException, InsufficientCapacityException, ConcurrentOperationException {
		return startVirtualMachine(cmd, null);
	}
	
	protected UserVm startVirtualMachine(DeployVMCmd cmd, Map<VirtualMachineProfile.Param, Object> additonalParams) throws ResourceUnavailableException, InsufficientCapacityException, ConcurrentOperationException {
	    long vmId = cmd.getEntityId();
	    UserVmVO vm = _vmDao.findById(vmId);
	    _vmDao.loadDetails(vm);
	    
        // Check that the password was passed in and is valid
        VMTemplateVO template = _templateDao.findByIdIncludingRemoved(vm.getTemplateId());
        
        String password = "saved_password";
        if (template.getEnablePassword()) {
            password = generateRandomPassword();
        }

        if (!validPassword(password)) {
            throw new InvalidParameterValueException("A valid password for this virtual machine was not provided.");
        }


        // Check if an SSH key pair was selected for the instance and if so use it to encrypt & save the vm password
        String sshPublicKey = vm.getDetail("SSH.PublicKey");
        if (sshPublicKey != null && !sshPublicKey.equals("") && password != null && !password.equals("saved_password") ) {       	
        	String encryptedPasswd = RSAHelper.encryptWithSSHPublicKey(sshPublicKey, password);
        	if (encryptedPasswd == null) {
                throw new CloudRuntimeException("Error encrypting password");
            }
        	
        	vm.setDetail("Encrypted.Password", encryptedPasswd);
        	_vmDao.saveDetails(vm);
        }
        
	    long userId = UserContext.current().getCallerUserId();
	    UserVO caller = _userDao.findById(userId);
	    
	    AccountVO owner = _accountDao.findById(vm.getAccountId());
	    
	    try {
	        Map<VirtualMachineProfile.Param, Object> params = new HashMap<VirtualMachineProfile.Param, Object>();
	        if (additonalParams != null) {
	        	params.putAll(additonalParams);
	        }
	        params.put(VirtualMachineProfile.Param.VmPassword, password);
			vm = _itMgr.start(vm, params, caller, owner);
		} finally {
			updateVmStateForFailedVmCreation(vm.getId());
		}
		
		if (template.getEnablePassword()) {
		    //this value is not being sent to the backend; need only for api dispaly purposes
		    vm.setPassword(password);
		}
        
	    return vm;
	}
	
	@Override
	public boolean finalizeVirtualMachineProfile(VirtualMachineProfile<UserVmVO> profile, DeployDestination dest, ReservationContext context) {
        UserVmVO vm = profile.getVirtualMachine();
	    Account owner = _accountDao.findById(vm.getAccountId());

	    if (owner == null) {
	        throw new PermissionDeniedException("The owner of " + vm + " does not exist: " + vm.getAccountId());
	    }
	    
	    if (owner.getState() == Account.State.disabled) {
	        throw new PermissionDeniedException("The owner of " + vm + " is disabled: " + vm.getAccountId());
	    }
	    
		VirtualMachineTemplate template = profile.getTemplate();
		if (vm.getIsoId() != null) {
			template = _templateDao.findById(vm.getIsoId());
		}
		if (template != null && template.getFormat() == ImageFormat.ISO && vm.getIsoId() != null) {
			String isoPath = null;
			Pair<String, String> isoPathPair = _storageMgr.getAbsoluteIsoPath(template.getId(), vm.getDataCenterId()); 	
			if (isoPathPair == null) {
				s_logger.warn("Couldn't get absolute iso path");
				return false;
			} else {
				isoPath = isoPathPair.first();
			}
			if (template.isBootable()) {
                profile.setBootLoaderType(BootloaderType.CD);
            }
			GuestOSVO guestOS = _guestOSDao.findById(template.getGuestOSId());
			String displayName = null;
			if (guestOS != null) {
				displayName = guestOS.getDisplayName();
			}
			VolumeTO iso = new VolumeTO(profile.getId(), Volume.Type.ISO, StoragePoolType.ISO, null, template.getName(), null, isoPath, 0,
										null, displayName);
			
			iso.setDeviceId(3);
			profile.addDisk(iso);
		} else {
			/*create a iso placeholder*/
			VolumeTO iso = new VolumeTO(profile.getId(), Volume.Type.ISO, StoragePoolType.ISO, null, template.getName(), null, null, 0,
					null);
			iso.setDeviceId(3);
			profile.addDisk(iso);
		}
		
		return true;
	}
	
	@Override
	public boolean finalizeDeployment(Commands cmds, VirtualMachineProfile<UserVmVO> profile, DeployDestination dest, ReservationContext context) {
		UserVmVO userVm = profile.getVirtualMachine();
		List<NicVO> nics = _nicDao.listByVmId(userVm.getId());
		for (NicVO nic : nics) {
			NetworkVO network = _networkDao.findById(nic.getNetworkId());
			if (network.getTrafficType() == TrafficType.Guest || network.getTrafficType() == TrafficType.Public) {
				userVm.setPrivateIpAddress(nic.getIp4Address());
				userVm.setPrivateMacAddress(nic.getMacAddress());
			}
		}
		_vmDao.update(userVm.getId(), userVm);
		return true;
	}
	
	 @Override
    public boolean finalizeCommandsOnStart(Commands cmds, VirtualMachineProfile<UserVmVO> profile) {
        return true;
    }

    @Override
    public boolean finalizeStart(VirtualMachineProfile<UserVmVO> profile, long hostId, Commands cmds, ReservationContext context) {
    	UserVmVO vm = profile.getVirtualMachine();
        UsageEventVO usageEvent = new UsageEventVO(EventTypes.EVENT_VM_START, vm.getAccountId(), vm.getDataCenterId(), vm.getId(), vm.getHostName(), vm.getServiceOfferingId(), vm.getTemplateId(), vm.getHypervisorType().toString());
        _usageEventDao.persist(usageEvent);
        
        List<NicVO> nics = _nicDao.listByVmId(vm.getId());
        for (NicVO nic : nics) {
            NetworkVO network = _networkDao.findById(nic.getNetworkId());
            long isDefault = (nic.isDefaultNic()) ? 1 : 0;
            usageEvent = new UsageEventVO(EventTypes.EVENT_NETWORK_OFFERING_ASSIGN, vm.getAccountId(), vm.getDataCenterId(), vm.getId(), vm.getHostName(), network.getNetworkOfferingId(), null, isDefault);
            _usageEventDao.persist(usageEvent);   
        }
        
    	return true;
    }
    
    @Override
    public void finalizeExpunge(UserVmVO vm) {
    }
    
    @Override
    public UserVmVO persist(UserVmVO vm) {
        return _vmDao.persist(vm);
    }
    
    @Override
    public UserVmVO findById(long id) {
        return _vmDao.findById(id);
    }
    
    @Override
    public UserVmVO findByName(String name) {
        if (!VirtualMachineName.isValidVmName(name)) {
            return null;
        }
        return findById(VirtualMachineName.getVmId(name));
    }

    @Override @ActionEvent (eventType=EventTypes.EVENT_VM_STOP, eventDescription="stopping Vm", async=true)
    public UserVm stopVirtualMachine(long vmId, boolean forced) throws ConcurrentOperationException {
        
        //Input validation
        Account caller = UserContext.current().getCaller();
        Long userId = UserContext.current().getCallerUserId();
        
        //if account is removed, return error
        if (caller != null && caller.getRemoved() != null) {
            throw new PermissionDeniedException("The account " + caller.getId()+" is removed");
        }
                
        UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        }
        
        userId = accountAndUserValidation(vmId, caller, userId, vm);
        UserVO user = _userDao.findById(userId);

        try {
            _itMgr.advanceStop(vm, forced, user, caller);
        } catch (ResourceUnavailableException e) {
            throw new CloudRuntimeException("Unable to contact the agent to stop the virtual machine " + vm, e);
        } catch (OperationTimedoutException e) {
            throw new CloudRuntimeException("Unable to contact the agent to stop the virtual machine " + vm, e);
        } 
        
        return _vmDao.findById(vmId);
    }
    
    @Override
    public void finalizeStop(VirtualMachineProfile<UserVmVO> profile, StopAnswer answer) {
		VMInstanceVO vm = profile.getVirtualMachine();
        UsageEventVO usageEvent = new UsageEventVO(EventTypes.EVENT_VM_STOP, vm.getAccountId(), vm.getDataCenterId(), vm.getId(), vm.getHostName());
        _usageEventDao.persist(usageEvent);
        
        List<NicVO> nics = _nicDao.listByVmId(vm.getId());
        for (NicVO nic : nics) {
            NetworkVO network = _networkDao.findById(nic.getNetworkId());
            usageEvent = new UsageEventVO(EventTypes.EVENT_NETWORK_OFFERING_REMOVE, vm.getAccountId(), vm.getDataCenterId(), vm.getId(), null, network.getNetworkOfferingId(), null, 0L);
            _usageEventDao.persist(usageEvent);   
        }
    }
    
    public String generateRandomPassword() {
        return PasswordGenerator.generateRandomPassword(6);
    }

    
    @Override
    public UserVm startVirtualMachine(long vmId) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        //Input validation
        Account account = UserContext.current().getCaller();
        Long userId = UserContext.current().getCallerUserId();
        
        //if account is removed, return error
        if(account!=null && account.getRemoved() != null) {
            throw new PermissionDeniedException("The account " + account.getId()+" is removed");
        }
                
        UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        }

        userId = accountAndUserValidation(vmId, account, userId, vm);
        UserVO user = _userDao.findById(userId);
        
        //check if vm is security group enabled
        if (_securityGroupMgr.isVmSecurityGroupEnabled(vmId) && !_securityGroupMgr.isVmMappedToDefaultSecurityGroup(vmId)) {
            //if vm is not mapped to security group, create a mapping
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Vm " + vm + " is security group enabled, but not mapped to default security group; creating the mapping automatically");
            }
            
            SecurityGroup defaultSecurityGroup = _securityGroupMgr.getDefaultSecurityGroup(vm.getAccountId());
            if (defaultSecurityGroup != null) {
                List<Long> groupList = new ArrayList<Long>();
                groupList.add(defaultSecurityGroup.getId());
                _securityGroupMgr.addInstanceToGroups(vmId, groupList);
            }
        }
  
        return _itMgr.start(vm, null, user, account);
    }
    
    @Override
    public UserVm destroyVm(long vmId) throws ResourceUnavailableException, ConcurrentOperationException {
        Account account = UserContext.current().getCaller();
        Long userId = UserContext.current().getCallerUserId();
        
        //Verify input parameters
        UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException("Unable to find a virtual machine with id " + vmId);
        }

        userId = accountAndUserValidation(vmId, account, userId, vm);
        User caller = _userDao.findById(userId);
        
        boolean status;
        State vmState = vm.getState();
        try {
            status = _itMgr.destroy(vm, caller, account);
        } catch (OperationTimedoutException e) {
            throw new CloudRuntimeException("Unable to destroy " + vm, e);
        }
        
        if (status) {
            // Mark the account's volumes as destroyed
            List<VolumeVO> volumes = _volsDao.findByInstance(vmId);
            for (VolumeVO volume : volumes) {
                if (volume.getVolumeType().equals(Volume.Type.ROOT)) {
                    UsageEventVO usageEvent = new UsageEventVO(EventTypes.EVENT_VOLUME_DELETE, volume.getAccountId(), volume.getDataCenterId(), volume.getId(),
                            volume.getName());
                    _usageEventDao.persist(usageEvent);
                }
            }
            UsageEventVO usageEvent = new UsageEventVO(EventTypes.EVENT_VM_DESTROY, vm.getAccountId(), vm.getDataCenterId(), vm.getId(), vm.getHostName());
            _usageEventDao.persist(usageEvent);
            if (vmState != State.Error) {
            	_accountMgr.decrementResourceCount(vm.getAccountId(), ResourceType.user_vm);
            }
            return _vmDao.findById(vmId);
        } else {
            throw new CloudRuntimeException("Failed to destroy vm with id " + vmId);
        }
    }

    @Override
    public List<UserVmVO> searchForUserVMs(ListVMsCmd cmd) throws InvalidParameterValueException, PermissionDeniedException {
        Account caller = UserContext.current().getCaller();
        Long domainId = cmd.getDomainId();
        String accountName = cmd.getAccountName();
        Boolean isRecursive = cmd.isRecursive();
        String hypervisor = cmd.getHypervisor();
        Long accountId = null;
        String path = null;
        
        if (isRecursive != null && isRecursive && domainId == null){
            throw new InvalidParameterValueException("Please enter a parent domain id for listing vms recursively");
        }
        
        if (domainId != null) {
            //Verify if user is authorized to see instances belonging to the domain
            DomainVO domain = _domainDao.findById(domainId);
            if (domain == null) {
                throw new InvalidParameterValueException("Domain id=" + domainId + " doesn't exist");
            }
            _accountMgr.checkAccess(caller, domain);
        }

        boolean isAdmin = false;

        if (_accountMgr.isAdmin(caller.getType())) {
            isAdmin = true;  
            if (accountName != null && domainId != null) {
                caller = _accountDao.findActiveAccount(accountName, domainId);
                if (caller == null) {
                    throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain " + domainId);
                }
                accountId = caller.getId();
            }
            
            if (caller.getType() == Account.ACCOUNT_TYPE_DOMAIN_ADMIN) {     
                if (isRecursive == null) {
                    DomainVO domain = _domainDao.findById(caller.getDomainId());
                    path = domain.getPath();
                } 
            }
        } else {
            accountId = caller.getId();
        }
        
        if (isRecursive != null && isRecursive && isAdmin) {
            if (isRecursive) {
                DomainVO domain = _domainDao.findById(domainId);
                path = domain.getPath();
                domainId = null;
            }
        } 
    
        Criteria c = new Criteria("id", Boolean.TRUE, cmd.getStartIndex(), cmd.getPageSizeVal());
        c.addCriteria(Criteria.KEYWORD, cmd.getKeyword());
        c.addCriteria(Criteria.ID, cmd.getId());
        c.addCriteria(Criteria.NAME, cmd.getInstanceName());
        c.addCriteria(Criteria.STATE, cmd.getState());
        c.addCriteria(Criteria.DATACENTERID, cmd.getZoneId());
        c.addCriteria(Criteria.GROUPID, cmd.getGroupId());
        c.addCriteria(Criteria.FOR_VIRTUAL_NETWORK, cmd.getForVirtualNetwork());
        c.addCriteria(Criteria.NETWORKID, cmd.getNetworkId());
        
        if (domainId != null) {
            c.addCriteria(Criteria.DOMAINID, domainId);
        }

        if (path != null) {
            c.addCriteria(Criteria.PATH, path);
        }
        
       	if (HypervisorType.getType(hypervisor) != HypervisorType.None){
       		c.addCriteria(Criteria.HYPERVISOR, hypervisor);
       	} else if (hypervisor != null){
       		throw new InvalidParameterValueException("Invalid HypervisorType " + hypervisor);
       	}

        // ignore these search requests if it's not an admin
        if (isAdmin) {
            c.addCriteria(Criteria.PODID, cmd.getPodId());
            c.addCriteria(Criteria.HOSTID, cmd.getHostId());
            c.addCriteria(Criteria.STORAGE_ID, cmd.getStorageId());
        }
        
        if (accountId != null) {
            c.addCriteria(Criteria.ACCOUNTID, new Object[] {accountId});
        }
        c.addCriteria(Criteria.ISADMIN, isAdmin); 

        return searchForUserVMs(c);
    }

    
    @Override
    public List<UserVmVO> searchForUserVMs(Criteria c) {
        Filter searchFilter = new Filter(UserVmVO.class, c.getOrderBy(), c.getAscending(), c.getOffset(), c.getLimit());
        
        SearchBuilder<UserVmVO> sb = _vmDao.createSearchBuilder();
        Object[] accountIds = (Object[]) c.getCriteria(Criteria.ACCOUNTID);
        Object domainId = c.getCriteria(Criteria.DOMAINID);
        Object id = c.getCriteria(Criteria.ID);
        Object name = c.getCriteria(Criteria.NAME);
        Object state = c.getCriteria(Criteria.STATE);
        Object notState = c.getCriteria(Criteria.NOTSTATE);
        Object zone = c.getCriteria(Criteria.DATACENTERID);
        Object pod = c.getCriteria(Criteria.PODID);
        Object hostId = c.getCriteria(Criteria.HOSTID);
        Object hostName = c.getCriteria(Criteria.HOSTNAME);
        Object keyword = c.getCriteria(Criteria.KEYWORD);
        Object isAdmin = c.getCriteria(Criteria.ISADMIN);
        assert c.getCriteria(Criteria.IPADDRESS) == null : "We don't support search by ip address on VM any more.  If you see this assert, it means we have to find a different way to search by the nic table.";
        Object groupId = c.getCriteria(Criteria.GROUPID);
        Object path = c.getCriteria(Criteria.PATH);
        Object networkId = c.getCriteria(Criteria.NETWORKID);
        Object hypervisor = c.getCriteria(Criteria.HYPERVISOR);
        Object storageId = c.getCriteria(Criteria.STORAGE_ID);
        
        sb.and("displayName", sb.entity().getDisplayName(), SearchCriteria.Op.LIKE);
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("accountIdEQ", sb.entity().getAccountId(), SearchCriteria.Op.EQ);
        sb.and("accountIdIN", sb.entity().getAccountId(), SearchCriteria.Op.IN);
        sb.and("name", sb.entity().getHostName(), SearchCriteria.Op.LIKE);
        sb.and("stateEQ", sb.entity().getState(), SearchCriteria.Op.EQ);
        sb.and("stateNEQ", sb.entity().getState(), SearchCriteria.Op.NEQ);
        sb.and("stateNIN", sb.entity().getState(), SearchCriteria.Op.NIN);
        sb.and("dataCenterId", sb.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        sb.and("podId", sb.entity().getPodId(), SearchCriteria.Op.EQ);
        sb.and("hypervisorType", sb.entity().getHypervisorType(), SearchCriteria.Op.EQ);
        sb.and("hostIdEQ", sb.entity().getHostId(), SearchCriteria.Op.EQ);
        sb.and("hostIdIN", sb.entity().getHostId(), SearchCriteria.Op.IN);
        sb.and("domainId", sb.entity().getDomainId(), SearchCriteria.Op.EQ);
        
        if (path != null) {
            SearchBuilder<DomainVO> domainSearch = _domainDao.createSearchBuilder();
            domainSearch.and("path", domainSearch.entity().getPath(), SearchCriteria.Op.LIKE);
            sb.join("domainSearch", domainSearch, sb.entity().getDomainId(), domainSearch.entity().getId(), JoinBuilder.JoinType.INNER);
        }
        
        if (groupId != null && (Long)groupId == -1) {
            SearchBuilder<InstanceGroupVMMapVO> vmSearch = _groupVMMapDao.createSearchBuilder();
            vmSearch.and("instanceId", vmSearch.entity().getInstanceId(), SearchCriteria.Op.EQ);
            sb.join("vmSearch", vmSearch, sb.entity().getId(), vmSearch.entity().getInstanceId(), JoinBuilder.JoinType.LEFTOUTER);
        } else if (groupId != null) {
            SearchBuilder<InstanceGroupVMMapVO> groupSearch = _groupVMMapDao.createSearchBuilder();
            groupSearch.and("groupId", groupSearch.entity().getGroupId(), SearchCriteria.Op.EQ);
            sb.join("groupSearch", groupSearch, sb.entity().getId(), groupSearch.entity().getInstanceId(), JoinBuilder.JoinType.INNER);
        }
        
        if (networkId != null) {
            SearchBuilder<NicVO> nicSearch = _nicDao.createSearchBuilder();
            nicSearch.and("networkId", nicSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
            
            SearchBuilder<NetworkVO> networkSearch = _networkDao.createSearchBuilder();
            networkSearch.and("networkId", networkSearch.entity().getId(), SearchCriteria.Op.EQ);
            nicSearch.join("networkSearch", networkSearch, nicSearch.entity().getNetworkId(), networkSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            
            sb.join("nicSearch", nicSearch, sb.entity().getId(), nicSearch.entity().getInstanceId(), JoinBuilder.JoinType.INNER);
        }
        
        if (storageId != null) {
            SearchBuilder<VolumeVO> volumeSearch = _volsDao.createSearchBuilder();
            volumeSearch.and("poolId", volumeSearch.entity().getPoolId(), SearchCriteria.Op.EQ);
            sb.join("volumeSearch", volumeSearch, sb.entity().getId(), volumeSearch.entity().getInstanceId(), JoinBuilder.JoinType.INNER);
        }
        
        // populate the search criteria with the values passed in
        SearchCriteria<UserVmVO> sc = sb.create();  
        if (groupId != null && (Long)groupId == -1){
            sc.setJoinParameters("vmSearch", "instanceId", (Object)null);
        } else if (groupId != null ) {
            sc.setJoinParameters("groupSearch", "groupId", groupId);
        }
        
        if (keyword != null) {
            SearchCriteria<UserVmVO> ssc = _vmDao.createSearchCriteria();
            ssc.addOr("displayName", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("hostName", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("instanceName", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("state", SearchCriteria.Op.EQ, keyword);
            
            sc.addAnd("displayName", SearchCriteria.Op.SC, ssc);
        }

        if (id != null) {
            sc.setParameters("id", id);
        }
        
        if (accountIds != null) {
            if (accountIds.length == 1) {
                if (accountIds[0] != null) {
                    sc.setParameters("accountIdEQ", accountIds[0]);
                }
            } else {
                sc.setParameters("accountIdIN", accountIds);
            }
        } 
        
        if (domainId != null) {
            sc.setParameters("domainId", domainId);
        }
        
        if (path != null) {
            sc.setJoinParameters("domainSearch", "path", path + "%");
        }
        
        if (networkId != null) {
            sc.setJoinParameters("nicSearch", "networkId", networkId);
        }

        if (name != null) {
            sc.setParameters("name", "%" + name + "%");
        }
        
        if (state != null) {
            if (notState != null && (Boolean) notState == true) {
                sc.setParameters("stateNEQ", state);
            } else {
                sc.setParameters("stateEQ", state);
            }
        }

        if (hypervisor != null){
        	sc.setParameters("hypervisorType", hypervisor);
        }
        
        //Don't show Destroyed and Expunging vms to the end user
        if ((isAdmin != null) && ((Boolean) isAdmin != true)) {
            sc.setParameters("stateNIN", "Destroyed", "Expunging");
        }

        if (zone != null) {
            sc.setParameters("dataCenterId", zone);
            
            if (state == null) {
                sc.setParameters("stateNEQ", "Destroyed");
            }
        }
        if (pod != null) {
            sc.setParameters("podId", pod);
            
            if(state == null) {
                sc.setParameters("stateNEQ", "Destroyed");
            }
        }

        if (hostId != null) {
            sc.setParameters("hostIdEQ", hostId);
        } else {
            if (hostName != null) {
                List<HostVO> hosts = _hostDao.findHostsLike((String) hostName);
                if (hosts != null & !hosts.isEmpty()) {
                    Long[] hostIds = new Long[hosts.size()];
                    for (int i = 0; i < hosts.size(); i++) {
                        HostVO host = hosts.get(i);
                        hostIds[i] = host.getId();
                    }
                    sc.setParameters("hostIdIN", (Object[]) hostIds);
                } else {
                    return new ArrayList<UserVmVO>();
                }
            }
        }
        
        if (storageId != null) {
            sc.setJoinParameters("volumeSearch", "poolId", storageId);
        }

        return _vmDao.search(sc, searchFilter);
    }

	@Override
	public HypervisorType getHypervisorTypeOfUserVM(long vmid) {
		UserVmVO userVm = _vmDao.findById(vmid);
		if (userVm == null) {
    	    throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmid);
    	}
		
		return userVm.getHypervisorType();
	}

	@Override
	public UserVm createVirtualMachine(DeployVMCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException, ConcurrentOperationException,
			StorageUnavailableException, ResourceAllocationException {
		// TODO Auto-generated method stub
		return null;
	}
    
    @Override
    public UserVm getUserVm(long vmId){
    	return _vmDao.findById(vmId);
    }
    
    @Override @ActionEvent (eventType=EventTypes.EVENT_VM_MIGRATE, eventDescription="migrating VM", async=true)
    public UserVm migrateVirtualMachine(UserVm vm, Host destinationHost) throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException, VirtualMachineMigrationException{
    	//access check - only root admin can migrate VM
    	Account caller = UserContext.current().getCaller();
    	if(caller.getType() != Account.ACCOUNT_TYPE_ADMIN){
    		if(s_logger.isDebugEnabled()){
    			s_logger.debug("Caller is not a root admin, permission denied to migrate the VM");
    		}    		
    		throw new PermissionDeniedException("No permission to migrate VM, Only Root Admin can migrate a VM!");
    	}
    	//business logic
        if(vm.getState() != State.Running){
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("VM is not Running, unable to migrate the vm " + vm);
            }
            throw new InvalidParameterValueException("VM is not Running, unable to migrate the vm " + vm);
        }
    	if(!vm.getHypervisorType().equals(HypervisorType.XenServer)){
    		if(s_logger.isDebugEnabled()){
    			s_logger.debug(vm + " is not XenServer, cannot migrate this VM.");
    		}
            throw new InvalidParameterValueException("Unsupported Hypervisor Type for VM migration, we support XenServer only");
    	}
    	
    	ServiceOfferingVO svcOffering = _serviceOfferingDao.findById(vm.getServiceOfferingId());
    	if(svcOffering.getUseLocalStorage()){
    		if(s_logger.isDebugEnabled()){
    			s_logger.debug(vm + " is using Local Storage, cannot migrate this VM.");
    		}
            throw new InvalidParameterValueException("Unsupported operation, VM uses Local storage, cannot migrate");
    	}

    	//call to core process
    	DataCenterVO dcVO = _dcDao.findById(destinationHost.getDataCenterId());
    	HostPodVO pod = _podDao.findById(destinationHost.getPodId());
    	long srcHostId = vm.getHostId();
    	Cluster cluster = _clusterDao.findById(destinationHost.getClusterId());
    	DeployDestination dest = new DeployDestination(dcVO, pod,cluster,destinationHost);
    	
		UserVmVO migratedVm = _itMgr.migrate((UserVmVO)vm, srcHostId, dest);
		return migratedVm;
    }
    
}
