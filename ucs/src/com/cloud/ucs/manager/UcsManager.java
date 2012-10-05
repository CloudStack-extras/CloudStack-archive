package com.cloud.ucs.manager;

import com.cloud.api.ResponseGenerator;
import com.cloud.api.response.ClusterResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.utils.component.Manager;

public interface UcsManager extends Manager {
    AddUcsManagerResponse addUcsManager(AddUcsManagerCmd cmd);
    
    ListResponse<ClusterResponse> addUcsCluster(AddUcsClusterCmd cmd, ResponseGenerator responseGenerator);
    
    ListResponse<ListUcsProfileResponse> listUcsProfiles(ListUcsProfileCmd cmd);
    
    void associateProfileToBladesInCluster(AssociateUcsProfileToBladesInClusterCmd cmd) throws InterruptedException;
}
