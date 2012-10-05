package com.cloud.ucs.manager;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.BaseCmd.CommandType;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.server.ManagementService;
import com.cloud.user.Account;
import com.cloud.utils.component.ComponentLocator;
@Implementation(description="associate a profile to blades in cluster", responseObject=AssociateUcsProfileToBladesInClusterResponse.class)
public class AssociateUcsProfileToBladesInClusterCmd extends BaseCmd {
    public static final Logger s_logger = Logger.getLogger(AssociateUcsProfileToBladesInClusterCmd.class);
    
    @IdentityMapper(entityTableName="cluster")
    @Parameter(name=ApiConstants.CLUSTER_ID, type=CommandType.LONG, description="the ucs cluster id", required=true)
    private Long clusterId;
    
    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException, NetworkRuleConflictException {
        try {
            ComponentLocator locator = ComponentLocator.getLocator(ManagementService.Name);
            UcsManager mgr = locator.getManager(UcsManager.class);
            mgr.associateProfileToBladesInCluster(this);
            AssociateUcsProfileToBladesInClusterResponse rsp = new AssociateUcsProfileToBladesInClusterResponse();
            rsp.setResponseName(getCommandName());
            this.setResponseObject(rsp);
        } catch (Exception e) {
            s_logger.warn("Exception: ", e);
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, e.getMessage());   
        }
    }

    @Override
    public String getCommandName() {
        return "associateucsprofiletobladesinclusterresponse";
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }
}
