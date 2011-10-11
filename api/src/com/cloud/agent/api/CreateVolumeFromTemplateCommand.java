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
package com.cloud.agent.api;

/**
 * This class encapsulates the information required for creating a new Volume from the backup of a snapshot. 
 * This currently assumes that both primary and secondary storage are mounted on the XenServer.  
 */
public class CreateVolumeFromTemplateCommand extends Command {
    private String primaryStoragePoolNameLabel;
    private String secondaryStoragePoolURL;
    private String absolutePath;
    private long dcId;
    
    protected CreateVolumeFromTemplateCommand() {
        
    }
    

    public CreateVolumeFromTemplateCommand(String primaryStoragePoolNameLabel,
                                           String secondaryStoragePoolURL,
                                           Long   dcId,
                                           String   absolutePath,
                                           int wait)
    {
        this.primaryStoragePoolNameLabel = primaryStoragePoolNameLabel;
        this.dcId = dcId;
        this.secondaryStoragePoolURL = secondaryStoragePoolURL;
        this.absolutePath = absolutePath;
        setWait(wait);
    }


    public String getPrimaryStoragePoolNameLabel() {
        return primaryStoragePoolNameLabel;
    }


    public String getSecondaryStoragePoolURL() {
        return secondaryStoragePoolURL;
    }


    public String getAbsolutePath() {
        return absolutePath;
    }


    public long getDcId() {
        return dcId;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}