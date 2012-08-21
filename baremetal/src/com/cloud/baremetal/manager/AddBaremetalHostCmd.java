package com.cloud.baremetal.manager;

import com.cloud.api.ApiConstants;
import com.cloud.api.Parameter;
import com.cloud.api.BaseCmd.CommandType;
import com.cloud.api.commands.AddHostCmd;

public class AddBaremetalHostCmd extends AddHostCmd {
    
    @Parameter(name=ApiConstants.IP_ADDRESS, type=CommandType.STRING, description="ip address intentionally allocated to this host after provisioning")
    private String vmIpAddress;

    public String getVmIpAddress() {
        return vmIpAddress;
    }

    public void setVmIpAddress(String vmIpAddress) {
        this.vmIpAddress = vmIpAddress;
    }
}
