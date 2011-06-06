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

package com.cloud.api.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.UserVmResponse;
import com.cloud.async.AsyncJob;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.Host;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.offering.ServiceOffering;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.user.UserContext;
import com.cloud.uservm.UserVm;

@Implementation(description="Creates and automatically starts a virtual machine based on a service offering, disk offering, and template.", responseObject=UserVmResponse.class)
public class DeployVMCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = Logger.getLogger(DeployVMCmd.class.getName());
    
    private static final String s_name = "deployvirtualmachineresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.ZONE_ID, type=CommandType.LONG, required=true, description="availability zone for the virtual machine")
    private Long zoneId;
    
    @Parameter(name=ApiConstants.SERVICE_OFFERING_ID, type=CommandType.LONG, required=true, description="the ID of the service offering for the virtual machine")
    private Long serviceOfferingId;
    
    @Parameter(name=ApiConstants.TEMPLATE_ID, type=CommandType.LONG, required=true, description="the ID of the template for the virtual machine")
    private Long templateId;
    
    @Parameter(name=ApiConstants.NAME, type=CommandType.STRING, description="host name for the virtual machine")
    private String name;
    
    @Parameter(name=ApiConstants.DISPLAY_NAME, type=CommandType.STRING, description="an optional user generated name for the virtual machine")
    private String displayName;
    
    //Owner information
    @Parameter(name=ApiConstants.ACCOUNT, type=CommandType.STRING, description="an optional account for the virtual machine. Must be used with domainId.")
    private String accountName;
    
    @Parameter(name=ApiConstants.DOMAIN_ID, type=CommandType.LONG, description="an optional domainId for the virtual machine. If the account parameter is used, domainId must also be used.")
    private Long domainId;
    
    //Network information
    @Parameter(name=ApiConstants.NETWORK_IDS, type=CommandType.LIST, collectionType=CommandType.LONG, description="list of network ids used by virtual machine")
    private List<Long> networkIds;

    //DataDisk information
    @Parameter(name=ApiConstants.DISK_OFFERING_ID, type=CommandType.LONG, description="the ID of the disk offering for the virtual machine. If the template is of ISO format, the diskOfferingId is for the root disk volume. Otherwise this parameter is used to indicate the offering for the data disk volume. If the templateId parameter passed is from a Template object, the diskOfferingId refers to a DATA Disk Volume created. If the templateId parameter passed is from an ISO object, the diskOfferingId refers to a ROOT Disk Volume created.")
    private Long diskOfferingId;
    
    @Parameter(name=ApiConstants.SIZE, type=CommandType.LONG, description="the arbitrary size for the DATADISK volume. Mutually exclusive with diskOfferingId")
    private Long size;

    @Parameter(name=ApiConstants.GROUP, type=CommandType.STRING, description="an optional group for the virtual machine")
    private String group;

    @Parameter(name=ApiConstants.HYPERVISOR, type=CommandType.STRING, description="the hypervisor on which to deploy the virtual machine")
    private String hypervisor;
   
    @Parameter(name=ApiConstants.USER_DATA, type=CommandType.STRING, description="an optional binary data that can be sent to the virtual machine upon a successful deployment. This binary data must be base64 encoded before adding it to the request. Currently only HTTP GET is supported. Using HTTP GET (via querystring), you can send up to 2KB of data after base64 encoding.")
    private String userData;

    @Parameter(name=ApiConstants.SSH_KEYPAIR, type=CommandType.STRING, description="name of the ssh key pair used to login to the virtual machine", includeInApiDoc=false)
    private String sshKeyPairName;
    
    @Parameter(name=ApiConstants.HOST_ID, type=CommandType.LONG, description="destination Host ID to deploy the VM to - parameter available for root admin only")
    private Long hostId;
    
    @Parameter(name=ApiConstants.SECURITY_GROUP_IDS, type=CommandType.LIST, collectionType=CommandType.LONG, description="comma separated list of security groups id that going to be applied to the virtual machine. Should be passed only when vm is created from a zone with Basic Network support. Mutually exclusive with securitygroupnames parameter")
    private List<Long> securityGroupIdList;
    
    @Parameter(name=ApiConstants.SECURITY_GROUP_NAMES, type=CommandType.LIST, collectionType=CommandType.STRING, description="comma separated list of security groups names that going to be applied to the virtual machine. Should be passed only when vm is created from a zone with Basic Network support. Mutually exclusive with securitygroupids parameter")
    private List<String> securityGroupNameList;
    
    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getAccountName() {
        if (accountName == null) {
            return UserContext.current().getCaller().getAccountName();
        }
        return accountName;
    }

    public Long getDiskOfferingId() {
        return diskOfferingId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Long getDomainId() {
        if (domainId == null) {
            return UserContext.current().getCaller().getDomainId();
        }
        return domainId;
    }

    public String getGroup() {
        return group;
    }

    public HypervisorType getHypervisor() {  	
        return HypervisorType.getType(hypervisor);
    }

    public List<Long> getSecurityGroupIdList() {
        if (securityGroupNameList != null && securityGroupIdList != null) {
            throw new InvalidParameterValueException("securitygroupids parameter is mutually exclusive with securitygroupnames parameter");
        }
        
       //transform group names to ids here
       if (securityGroupNameList != null) {
            List<Long> securityGroupIds = new ArrayList<Long>();
            for (String groupName : securityGroupNameList) {
                Long groupId = _responseGenerator.getSecurityGroupId(groupName, getEntityOwnerId());
                if (groupId == null) {
                    throw new InvalidParameterValueException("Unable to find group by name " + groupName + " for account " + getEntityOwnerId());
                } else {
                    securityGroupIds.add(groupId);
                }
            }    
            return securityGroupIds;
        } else {
            return securityGroupIdList;
        }
    }

    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }

    public Long getSize() {
        return size;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public String getUserData() {
        return userData;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public List<Long> getNetworkIds() {
        return networkIds;
    }

    public String getName() {
        return name;
    }
    
    public String getSSHKeyPairName() {
    	return sshKeyPairName;
    }

    public Long getHostId() {
        return hostId;
    }
    
    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }
    
    public static String getResultObjectName() {
        return "virtualmachine";
    }
    
    @Override
    public long getEntityOwnerId() {
        Account account = UserContext.current().getCaller();
        if ((account == null) || isAdmin(account.getType())) {
            if ((domainId != null) && (accountName != null)) {
                Account userAccount = _responseGenerator.findAccountByNameDomain(accountName, domainId);
                if (userAccount != null) {
                    return userAccount.getId();
                } else {
                    throw new InvalidParameterValueException("Unable to find account by name " + getAccountName() + " in domain " + getDomainId());
                }
            }
        }
        
        return account.getId();
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VM_CREATE;
    }

    @Override
    public String getCreateEventType() {
        return EventTypes.EVENT_VM_CREATE;
    }
    
    @Override
    public String getCreateEventDescription() {
        return "creating Vm";
    }
    
    @Override
    public String getEventDescription() {
        return  "starting Vm. Vm Id: "+getEntityId();
    }
    
    @Override
    public AsyncJob.Type getInstanceType() {
    	return AsyncJob.Type.VirtualMachine;
    }
    
    @Override
    public void execute(){
        UserVm result;
        try {
            UserContext.current().setEventDetails("Vm Id: "+getEntityId());
            if (getHypervisor() == HypervisorType.BareMetal) {
            	result = _bareMetalVmService.startVirtualMachine(this);
            } else {
            	result = _userVmService.startVirtualMachine(this);
            }
            
            if (result != null) {
                UserVmResponse response = _responseGenerator.createUserVmResponse("virtualmachine", result).get(0);
                response.setResponseName(getCommandName());
                this.setResponseObject(response);
            } else {
                throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to deploy vm");
            }
        } catch (ResourceUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(BaseCmd.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        } catch (ConcurrentOperationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, ex.getMessage()); 
        } catch (InsufficientCapacityException ex) {
            s_logger.info(ex);
            s_logger.trace(ex);
            throw new ServerApiException(BaseCmd.INSUFFICIENT_CAPACITY_ERROR, ex.getMessage());
		}       
    }

    @Override
    public void create() throws ResourceAllocationException{
        try {
            //Verify that all objects exist before passing them to the service
            Account owner = _accountService.getActiveAccount(getAccountName(), getDomainId());
            if (owner == null) {
                throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain " + domainId);
            }
            
            DataCenter zone = _configService.getZone(zoneId);
            if (zone == null) {
                throw new InvalidParameterValueException("Unable to find zone by id=" + zoneId);
            }
            
            ServiceOffering serviceOffering = _configService.getServiceOffering(serviceOfferingId);
            if (serviceOffering == null) {
                throw new InvalidParameterValueException("Unable to find service offering: " + serviceOfferingId);
            }
            
            VirtualMachineTemplate template = _templateService.getTemplate(templateId);
            // Make sure a valid template ID was specified
            if (template == null) {
                throw new InvalidParameterValueException("Unable to use template " + templateId);
            }
            
            Host destinationHost = null;
            if(getHostId() != null){
            	Account account = UserContext.current().getCaller();
            	if(!_accountService.isRootAdmin(account.getType())){
            		throw new PermissionDeniedException("Parameter hostid can only be specified by a Root Admin, permission denied");
            	}
	            destinationHost = _resourceService.getHost(getHostId());
	            if (destinationHost == null) {
	                throw new InvalidParameterValueException("Unable to find the host to deploy the VM, host id=" + getHostId());
	            }
            }
            
			UserVm vm = null;
			if (getHypervisor() == HypervisorType.BareMetal) {
				vm = _bareMetalVmService.createVirtualMachine(this);
			} else {
				if (zone.getNetworkType() == NetworkType.Basic) {
					if (getNetworkIds() != null) {
						throw new InvalidParameterValueException("Can't specify network Ids in Basic zone");
					} else {
						vm = _userVmService.createBasicSecurityGroupVirtualMachine(zone, serviceOffering, template, getSecurityGroupIdList(), owner, name,
								displayName, diskOfferingId, size, group, getHypervisor(), userData, sshKeyPairName, destinationHost);
					}
				} else {
					if (zone.isSecurityGroupEnabled()) {
						vm = _userVmService.createAdvancedSecurityGroupVirtualMachine(zone, serviceOffering, template, getNetworkIds(), getSecurityGroupIdList(),
								owner, name, displayName, diskOfferingId, size, group, getHypervisor(), userData, sshKeyPairName, destinationHost);
					} else {
						if (getSecurityGroupIdList() != null && !getSecurityGroupIdList().isEmpty()) {
							throw new InvalidParameterValueException("Can't create vm with security groups; security group feature is not enabled per zone");
						}
						vm = _userVmService.createAdvancedVirtualMachine(zone, serviceOffering, template, getNetworkIds(), owner, name, displayName,
								diskOfferingId, size, group, getHypervisor(), userData, sshKeyPairName, destinationHost);
					}
				}
			}
            
			if (vm != null) {
				setEntityId(vm.getId());
            } else {
                throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to deploy vm");
            }
        } catch (InsufficientCapacityException ex) {
            s_logger.info(ex);
            s_logger.trace(ex);
            throw new ServerApiException(BaseCmd.INSUFFICIENT_CAPACITY_ERROR, ex.getMessage());
        } catch (ResourceUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(BaseCmd.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        }  catch (ConcurrentOperationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, ex.getMessage());
        }  
    }

}
