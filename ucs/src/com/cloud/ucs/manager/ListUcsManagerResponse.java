package com.cloud.ucs.manager;

import com.cloud.api.ApiConstants;
import com.cloud.api.response.BaseResponse;
import com.cloud.serializer.Param;
import com.cloud.utils.IdentityProxy;
import com.google.gson.annotations.SerializedName;

public class ListUcsManagerResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID) @Param(description="id of ucs manager")
    private IdentityProxy id = new IdentityProxy("ucs_manager");
    
    @SerializedName(ApiConstants.NAME) @Param(description="name of ucs manager")
    private String name;
    
    @SerializedName(ApiConstants.ZONE_ID) @Param(description="zone id the ucs manager belongs to")
    private IdentityProxy zoneId = new IdentityProxy("data_center");

    public Long getId() {
        return id.getValue();
    }

    public void setId(Long id) {
        this.id.setValue(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getZoneId() {
        return zoneId.getValue();
    }

    public void setZoneId(Long zoneId) {
        this.zoneId.setValue(zoneId);
    };
}
