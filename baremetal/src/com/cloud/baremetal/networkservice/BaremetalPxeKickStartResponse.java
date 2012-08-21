package com.cloud.baremetal.networkservice;

import com.cloud.api.ApiConstants;
import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

public class BaremetalPxeKickStartResponse extends BaremetalPxeResponse {
    @SerializedName(ApiConstants.TFTP_DIR) @Param(description="Tftp root directory of PXE server")
    private String tftpDir;

    public String getTftpDir() {
        return tftpDir;
    }

    public void setTftpDir(String tftpDir) {
        this.tftpDir = tftpDir;
    }
}
