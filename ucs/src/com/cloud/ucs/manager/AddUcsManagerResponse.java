package com.cloud.ucs.manager;

import com.cloud.api.ApiConstants;
import com.cloud.api.response.BaseResponse;
import com.cloud.serializer.Param;
import com.cloud.utils.IdentityProxy;
import com.google.gson.annotations.SerializedName;

public class AddUcsManagerResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID) @Param(description="the ID of the ucs manager")
    private IdentityProxy id = new IdentityProxy("ucs_manager");
    
    @SerializedName(ApiConstants.NAME) @Param(description="the name of ucs manager")
    private String name;
    
    @SerializedName(ApiConstants.URL) @Param(description="the url of ucs manager")
    private String url;
    
    @SerializedName(ApiConstants.ZONE_ID) @Param(description="the zone ID of ucs manager")
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getZoneId() {
        return zoneId.getValue();
    }

    public void setZoneId(Long zoneId) {
        this.zoneId.setValue(zoneId);
    }
}
