package com.cloud.api.commands;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.BaseCmd.CommandType;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.SSHKeyPairResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.async.AsyncJob;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import com.cloud.user.SSHKeyPair;
import com.cloud.user.UserContext;
import com.cloud.uservm.UserVm;
@Implementation(responseObject=UserVmResponse.class, description="Resets the SSHkey for virtual machine. ")

public class ResetSSHKeyCmd extends BaseAsyncCmd{

	public static final Logger s_logger = Logger.getLogger(ResetSSHKeyCmd.class.getName());

	private static final String s_name = "resetSSHforvirtualmachineresponse";
	
	/////////////////////////////////////////////////////
	//////////////// API parameters /////////////////////
	/////////////////////////////////////////////////////

    @IdentityMapper(entityTableName="vm_instance")
    @Parameter(name=ApiConstants.ID, type=CommandType.LONG, required=true, description="The ID of the virtual machine")
    private Long id;
    
    @Parameter(name=ApiConstants.NAME, type=CommandType.STRING, required=true, description="Name of the keypair") 
	private String name;
    
    
    //Owner information
	@Parameter(name=ApiConstants.ACCOUNT, type=CommandType.STRING, description="an optional account for the ssh key. Must be used with domainId.")
	private String accountName;

	@IdentityMapper(entityTableName="domain")
	@Parameter(name=ApiConstants.DOMAIN_ID, type=CommandType.LONG, description="an optional domainId for the ssh key. If the account parameter is used, domainId must also be used.")
	private Long domainId;

	@IdentityMapper(entityTableName="projects")
	@Parameter(name=ApiConstants.PROJECT_ID, type=CommandType.LONG, description="an optional project for the ssh key")
	private Long projectId;



	/////////////////////////////////////////////////////
	/////////////////// Accessors ///////////////////////
	/////////////////////////////////////////////////////
	
	public String getName() {
		return name;
	}

	
    public Long getId() {
        return id;
    }
    
	public String getAccountName() {
	    return accountName;
	}
	
	public Long getDomainId() {
	    return domainId;
	}
	
	public Long getProjectId() {
	    return projectId;
	}
	
    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

	
	
	@Override
	public String getEventType() {
		return EventTypes.EVENT_VM_RESETSSHKEY;
	}
	
	@Override
	public String getEventDescription() {
		 return  "resetting SSHKey for vm: " + getId();
	}
	

	
	@Override
	public String getCommandName() {
		return s_name;
	}
	
	@Override
	public long getEntityOwnerId() {
		UserVm vm = _responseGenerator.findUserVmById(getId());
        if (vm != null) {
            return vm.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
	}
	
	public Long getInstanceId() {
	   	return getId();
	}
	
	public AsyncJob.Type getInstanceType() {
	   	return AsyncJob.Type.VirtualMachine;
	}
	
	@Override
	public void execute() throws ResourceUnavailableException,
			InsufficientCapacityException, ServerApiException,
			ConcurrentOperationException, ResourceAllocationException,
			NetworkRuleConflictException {

		UserContext.current().setEventDetails("Vm Id: "+getId());
		UserVm result = _userVmService.resetSSHKey(this);

		if (result != null){
	            UserVmResponse response = _responseGenerator.createUserVmResponse("virtualmachine", result).get(0);
	            response.setResponseName(getCommandName());
	            this.setResponseObject(response);
	        } else {
	            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to reset vm SSHKey");
	        }
		}

}
