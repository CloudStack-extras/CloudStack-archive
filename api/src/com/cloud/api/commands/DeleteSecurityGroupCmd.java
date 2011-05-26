package com.cloud.api.commands;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceInUseException;
import com.cloud.user.Account;
import com.cloud.user.UserContext;

@Implementation(description="Deletes security group", responseObject=SuccessResponse.class)
public class DeleteSecurityGroupCmd extends BaseCmd {
    public static final Logger s_logger = Logger.getLogger(DeleteSecurityGroupCmd.class.getName());
    private static final String s_name = "deletesecuritygroupresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.ACCOUNT, type=CommandType.STRING, description="the account of the security group. Must be specified with domain ID")
    private String accountName;

    @Parameter(name=ApiConstants.DOMAIN_ID, type=CommandType.LONG, description="the domain ID of account owning the security group")
    private Long domainId;

    @Parameter(name=ApiConstants.ID, type=CommandType.LONG, description="The ID of the security group. Mutually exclusive with name parameter")
    private Long id;
    
    @Parameter(name=ApiConstants.NAME, type=CommandType.STRING, description="The ID of the security group. Mutually exclusive with id parameter")
    private String name;


    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Long getId() {
        if (id != null && name != null) {
            throw new InvalidParameterValueException("name and id parameters are mutually exclusive");
        }
        
        if (name != null) {
            id = _responseGenerator.getSecurityGroupId(name, getEntityOwnerId());
            if (id == null) {
                throw new InvalidParameterValueException("Unable to find security group by name " + name + " for the account id=" + getEntityOwnerId());
            }
        }
        
        if (id == null) {
            throw new InvalidParameterValueException("Either id or name parameter is requred by deleteSecurityGroup command");
        }
        
        return id;
    }



    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
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
                    throw new InvalidParameterValueException("Unable to find account by name " + accountName + " in domain " + domainId);
                }
            }
        }
        
        return account.getId();
    }
	
    @Override
    public void execute(){
        try{
            boolean result = _securityGroupService.deleteSecurityGroup(this);
            if (result) {
                SuccessResponse response = new SuccessResponse(getCommandName());
                this.setResponseObject(response);
            } else {
                throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to delete security group");
            }
        } catch (ResourceInUseException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(BaseCmd.RESOURCE_IN_USE_ERROR, ex.getMessage());
        }
    }
}
