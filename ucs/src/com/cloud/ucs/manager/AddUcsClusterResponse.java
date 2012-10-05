package com.cloud.ucs.manager;

import com.cloud.api.ApiConstants;
import com.cloud.api.response.ClusterResponse;
import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

public class AddUcsClusterResponse extends ClusterResponse {
    @SerializedName(ApiConstants.UCS_PROFILE) @Param(description="the ucs profile name")
    private String ucsProfile;

    public String getUcsProfile() {
        return ucsProfile;
    }

    public void setUcsProfile(String ucsProfile) {
        this.ucsProfile = ucsProfile;
    }
}
