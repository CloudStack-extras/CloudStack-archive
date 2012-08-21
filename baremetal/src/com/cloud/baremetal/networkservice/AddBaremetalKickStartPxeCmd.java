package com.cloud.baremetal.networkservice;

import com.cloud.api.ApiConstants;
import com.cloud.api.Parameter;

public class AddBaremetalKickStartPxeCmd extends AddBaremetalPxeCmd {
    @Parameter(name=ApiConstants.TFTP_DIR, type=CommandType.STRING, required = true, description="Tftp root directory of PXE server")
    private String tftpDir;

    public String getTftpDir() {
        return tftpDir;
    }

    public void setTftpDir(String tftpDir) {
        this.tftpDir = tftpDir;
    }
}
