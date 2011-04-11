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
package com.cloud.agent.api.storage;

import com.cloud.agent.api.to.VolumeTO;
import com.cloud.storage.StoragePool;
import com.cloud.storage.VMTemplateStorageResourceAssoc;
import com.cloud.storage.Volume;

public class DestroyCommand extends StorageCommand {
	// in VMware, things are designed around VM instead of volume, we need it the volume VM context if the volume is attached
	String vmName;
    VolumeTO volume;
    
    protected DestroyCommand() {
    }
    
    public DestroyCommand(StoragePool pool, Volume volume, String vmName) {
         this.volume = new VolumeTO(volume, pool);
         this.vmName = vmName;
    }
    
    public DestroyCommand(StoragePool pool, VMTemplateStorageResourceAssoc templatePoolRef) {
        volume = new VolumeTO(templatePoolRef.getId(), null, pool.getPoolType(), pool.getUuid(), 
        		null, pool.getPath(), templatePoolRef.getInstallPath(), 
        		templatePoolRef.getTemplateSize(), null);
    }
    
    public VolumeTO getVolume() {
    	return volume;
    }
    
    public String getVmName() {
    	return vmName;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
