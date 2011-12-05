package com.cloud.api.commands;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.UserVmResponse;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import com.cloud.user.UserContext;
import com.cloud.uservm.UserVm;

@Implementation(description="Restore a VM to original template or specific snapshot", responseObject=UserVmResponse.class)
public class RestoreVMCmd extends BaseAsyncCmd {
	public static final Logger s_logger = Logger.getLogger(RestoreVMCmd.class);
	private static final String s_name = "restorevmresponse";
	
    @IdentityMapper(entityTableName="vm_instance")
    @Parameter(name=ApiConstants.VIRTUAL_MACHINE_ID, type=CommandType.LONG, required=true, description="Virtual Machine ID")
    private Long vmId;
    
	@Override
	public String getEventType() {
		return EventTypes.EVENT_VM_RESTORE;
	}

	@Override
	public String getEventDescription() {
		return "Restore a VM to orignal template or specific snapshot";
	}

	@Override
	public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
	        ResourceAllocationException {
		UserVm result;
		UserContext.current().setEventDetails("Vm Id: " + getVmId());
		result = _userVmService.restoreVM(this);
		if (result != null) {
            UserVmResponse response = _responseGenerator.createUserVmResponse("virtualmachine", result).get(0);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
		} else {
			throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to restore vm " + getVmId());
		}
	}

	@Override
	public String getCommandName() {
		return s_name;
	}

	@Override
	public long getEntityOwnerId() {
		UserVm vm = _responseGenerator.findUserVmById(getVmId());
		if (vm == null) {
			 return Account.ACCOUNT_ID_SYSTEM; // bad id given, parent this command to SYSTEM so ERROR events are tracked
		}
		return vm.getAccountId();
	}
	
	public long getVmId() {
		return vmId;
	}

}
