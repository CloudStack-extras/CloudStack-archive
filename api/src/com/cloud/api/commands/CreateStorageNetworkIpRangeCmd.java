package com.cloud.api.commands;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.StorageNetworkIpRangeResponse;
import com.cloud.dc.StorageNetworkIpRange;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;

@Implementation(description="Creates a Storage network IP range.", responseObject=StorageNetworkIpRangeResponse.class, since="3.0.0")
public class CreateStorageNetworkIpRangeCmd extends BaseAsyncCmd {
	public static final Logger s_logger = Logger.getLogger(CreateStorageNetworkIpRangeCmd.class);
	
	private static final String s_name = "createstoragenetworkiprangeresponse";
	
    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////    
    @IdentityMapper(entityTableName="host_pod_ref")
    @Parameter(name=ApiConstants.POD_ID, type=CommandType.LONG, required=true, description="UUID of pod where the ip range belongs to")
    private Long podId;
    
    @Parameter(name=ApiConstants.START_IP, type=CommandType.STRING, required=true, description="the beginning IP address")
    private String startIp;

    @Parameter(name=ApiConstants.END_IP, type=CommandType.STRING, description="the ending IP address")
    private String endIp;
    
	@Parameter(name = ApiConstants.VLAN, type = CommandType.INTEGER, description = "Optional. The vlan the ip range sits on, default to Null when it is not specificed which means you network is not on any Vlan. This is mainly for Vmware as other hypervisors can directly reterive bridge from pyhsical network traffic type table")
    private Integer vlan;
        
    @Parameter(name=ApiConstants.NETMASK, type=CommandType.STRING, required=true, description="the netmask for storage network")
    private String netmask;
    
    @Parameter(name=ApiConstants.GATEWAY, type=CommandType.STRING, required=true, description="the gateway for storage network")
    private String gateway;
    
    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getEndIp() {
        return endIp;
    }

    public Long getPodId() {
        return podId;
    }

    public String getStartIp() {
        return startIp;
    }

    public Integer getVlan() {
        return vlan;
    }
            
    public String getNetmask() {
    	return netmask;
    }
    
    public String getGateWay() {
    	return gateway;
    }

	@Override
	public String getEventType() {
		return EventTypes.EVENT_STORAGE_IP_RANGE_CREATE;
	}

	@Override
	public String getEventDescription() {
		return "Creating storage ip range from " + getStartIp() + " to " + getEndIp() + " with vlan " + getVlan();
	}

	@Override
	public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
	        ResourceAllocationException {
		try {
			StorageNetworkIpRange result = _storageNetworkService.createIpRange(this);
			StorageNetworkIpRangeResponse response = _responseGenerator.createStorageNetworkIpRangeResponse(result);
			response.setResponseName(getCommandName());
			this.setResponseObject(response);
		} catch (Exception e) {
			s_logger.warn("Create storage network IP range failed", e);
			throw new ServerApiException(BaseCmd.INTERNAL_ERROR, e.getMessage());
		}
	}

	@Override
	public String getCommandName() {
		return s_name;
	}

	@Override
	public long getEntityOwnerId() {
		return Account.ACCOUNT_ID_SYSTEM;
	}

}
