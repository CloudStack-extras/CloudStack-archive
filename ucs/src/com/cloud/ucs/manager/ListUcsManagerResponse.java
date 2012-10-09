package com.cloud.ucs.manager;

import com.cloud.api.ApiConstants;
import com.cloud.api.response.BaseResponse;
import com.cloud.serializer.Param;
import com.cloud.utils.IdentityProxy;
import com.google.gson.annotations.SerializedName;

public class ListUcsManagerResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID) @Param(description="id of ucs manager")
    private IdentityProxy id = new IdentityProxy("ucs_manager");

    public Long getId() {
        return id.getValue();
    }

    public void setId(Long id) {
        this.id.setValue(id);
    };
}
