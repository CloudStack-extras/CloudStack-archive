package com.cloud.ucs.manager;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.commands.AddClusterCmd;
import com.cloud.api.response.ClusterResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.server.ManagementService;
import com.cloud.utils.component.ComponentLocator;

public class AddUcsClusterCmd extends AddClusterCmd {
    @Parameter(name=ApiConstants.UCS_PROFILE, type=CommandType.STRING, required=true, description="the ucs profile name")
    private String ucsProfile;

    public String getUcsProfile() {
        return ucsProfile;
    }

    public void setUcsProfile(String ucsProfile) {
        this.ucsProfile = ucsProfile;
    }

    @Override
    public String getCommandName() {
        return "addclusterresponse";
    }
    
    @Override
    public void execute(){
        ComponentLocator locator = ComponentLocator.getLocator(ManagementService.Name);
        UcsManager mgr = locator.getManager(UcsManager.class);
        try {
            ListResponse<ClusterResponse> rsp = mgr.addUcsCluster(this, _responseGenerator);
            rsp.setResponseName(getCommandName());
            this.setResponseObject(rsp);
        } catch (Exception e) {
            s_logger.warn("Exception: ", e);
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, e.getMessage()); 
        }
    }
}
