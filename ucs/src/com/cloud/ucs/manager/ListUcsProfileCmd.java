package com.cloud.ucs.manager;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ListResponse;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.server.ManagementService;
import com.cloud.user.Account;
import com.cloud.utils.component.ComponentLocator;
@Implementation(description="List profile in ucs manager", responseObject=ListUcsProfileResponse.class)
public class ListUcsProfileCmd extends BaseCmd {
    public static final Logger s_logger = Logger.getLogger(ListUcsProfileCmd.class);
    
    @IdentityMapper(entityTableName="ucs_manager")
    @Parameter(name=ApiConstants.ID, type=CommandType.LONG, description="the id for the ucs manager", required=true)
    private Long ucsManagerId;

    public Long getUcsManagerId() {
        return ucsManagerId;
    }

    public void setUcsManagerId(Long ucsManagerId) {
        this.ucsManagerId = ucsManagerId;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException, NetworkRuleConflictException {
        ComponentLocator locator = ComponentLocator.getLocator(ManagementService.Name);
        UcsManager mgr = locator.getManager(UcsManager.class);
        try {
            ListResponse<ListUcsProfileResponse> response = mgr.listUcsProfiles(this);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } catch (Exception e) {
            s_logger.warn("Exception: ", e);
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, e.getMessage());  
        }
    }

    @Override
    public String getCommandName() {
        return "listucsmanagerresponse";
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

}
