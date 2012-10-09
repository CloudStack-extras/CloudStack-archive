package com.cloud.ucs.manager;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;

import com.cloud.api.ServerApiException;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.server.ManagementService;
import com.cloud.user.Account;
import com.cloud.utils.component.ComponentLocator;

@Implementation(description="Adds a Ucs manager", responseObject=AddUcsManagerResponse.class)
public class AddUcsManagerCmd extends BaseCmd {
    public static final Logger s_logger = Logger.getLogger(AddUcsManagerCmd.class);
    
    @IdentityMapper(entityTableName="data_center")
    @Parameter(name=ApiConstants.ZONE_ID, type=CommandType.LONG, description="the Zone id for the ucs manager", required=true)
    private Long zoneId;
    
    @Parameter(name=ApiConstants.NAME, type=CommandType.STRING, description="the name of UCS manager")
    private String name;
    
    @Parameter(name=ApiConstants.URL, type=CommandType.STRING, description="the name of UCS url")
    private String url;
    
    @Parameter(name=ApiConstants.USERNAME, type=CommandType.STRING, description="the username of UCS")
    private String username;
    
    @Parameter(name=ApiConstants.PASSWORD, type=CommandType.STRING, description="the password of UCS")
    private String password;
    
    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException, NetworkRuleConflictException {
        ComponentLocator locator = ComponentLocator.getLocator(ManagementService.Name);
        UcsManager mgr = locator.getManager(UcsManager.class);
        try {
            AddUcsManagerResponse rsp = mgr.addUcsManager(this);
            rsp.setObjectName("ucsmanager");
            rsp.setResponseName(getCommandName());
            this.setResponseObject(rsp);
        } catch (Exception e) {
            s_logger.warn("Exception: ", e);
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, e.getMessage()); 
        }
    }

    @Override
    public String getCommandName() {
        return "addUcsManagerResponse";
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
}
