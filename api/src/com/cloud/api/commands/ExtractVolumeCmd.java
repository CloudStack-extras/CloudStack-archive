/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.api.commands;

import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ExtractResponse;
import com.cloud.async.AsyncJob;
import com.cloud.dc.DataCenter;
import com.cloud.event.EventTypes;
import com.cloud.storage.Upload;
import com.cloud.storage.Volume;
import com.cloud.user.Account;
import com.cloud.user.UserContext;

@Implementation(description="Extracts volume", responseObject=ExtractResponse.class)
public class ExtractVolumeCmd extends BaseAsyncCmd {
	public static final Logger s_logger = Logger.getLogger(ExtractVolumeCmd.class.getName());

    private static final String s_name = "extractvolumeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    //FIXME - add description
    @Parameter(name=ApiConstants.ID, type=CommandType.LONG, required=true, description="the ID of the volume")
    private Long id;

    //FIXME - add description
    @Parameter(name=ApiConstants.URL, type=CommandType.STRING, required=false, description="the url to which the volume would be extracted")
    private String url;

    //FIXME - add description
    @Parameter(name=ApiConstants.ZONE_ID, type=CommandType.LONG, required=true, description="the ID of the zone where the volume is located")
    private Long zoneId;
    
    @Parameter(name=ApiConstants.MODE, type=CommandType.STRING, required=true, description="the mode of extraction - HTTP_DOWNLOAD or FTP_UPLOAD")
    private String mode;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public Long getZoneId() {
        return zoneId;
    }
    
    public String getMode() {
        return mode;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
	public String getCommandName() {
		return s_name;
	}
    
    public static String getStaticName() {
        return s_name;
    }
    
    public AsyncJob.Type getInstanceType() {
    	return AsyncJob.Type.Volume;
    }
    
    public Long getInstanceId() {
    	return getId();
    }

    @Override
    public long getEntityOwnerId() {
        Volume volume = _entityMgr.findById(Volume.class, getId());
        if (volume != null) {
            return volume.getAccountId();
        }

        // invalid id, parent this command to SYSTEM so ERROR events are tracked
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VOLUME_EXTRACT;
    }

    @Override
    public String getEventDescription() {
        return  "Extraction job";
    }
	
    @Override
    public void execute(){
        try {
            UserContext.current().setEventDetails("Volume Id: "+getId());
            Long uploadId = _mgr.extractVolume(this);
            if (uploadId != null){
                Upload uploadInfo = _entityMgr.findById(Upload.class, uploadId);
                ExtractResponse response = new ExtractResponse();
                response.setResponseName(getCommandName());
                response.setObjectName("volume");
                response.setId(id);
                response.setName(_entityMgr.findById(Volume.class, id).getName());        
                response.setZoneId(zoneId);
                response.setZoneName(_entityMgr.findById(DataCenter.class, zoneId).getName());
                response.setMode(mode);
                response.setUploadId(uploadId);
                response.setState(uploadInfo.getUploadState().toString());
                response.setAccountId(getEntityOwnerId());        
                response.setUrl(uploadInfo.getUploadUrl());
                this.setResponseObject(response);
            } else {
                throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to extract volume");
            }
        } catch (URISyntaxException ex) {
            s_logger.info(ex);
            throw new ServerApiException(BaseCmd.PARAM_ERROR, ex.getMessage());
        }
    }
}
