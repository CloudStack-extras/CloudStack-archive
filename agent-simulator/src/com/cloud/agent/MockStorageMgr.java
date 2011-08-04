/**
 * *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
*
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

package com.cloud.agent;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class MockStorageMgr implements StorageMgr {
    private static final Logger s_logger = Logger.getLogger(MockStorageMgr.class);
    
    private Map<String, MockVolume> storage = new HashMap<String, MockVolume>();
    public static final int DEFAULT_HOST_STORAGE_SIZE_GB = 500; //500G
	
    
    // used to populate template volumes
    public void addVolume(String path, MockVolume volume) {
    	synchronized(this) {
    		storage.put(path, volume);
    	}
    }
    
	@Override
    public long getTotalSize() {
		return getHostStorage()*1024*1024*1024L;
	}
	
	@Override
	public long getUsedSize() {
		long size = 0;
		synchronized(this) {
			for(MockVolume volume : storage.values())
				size += volume.getSize();
		}		
		return size;
	}
	
	@Override
    public String create(String templatePath, String imagePath, String userPath, String dataPath) {
		
		//
		// Parameter value examples
		//		templatePath: tank/volumes/demo/template/public/os/winxpsp3
		//		imagePath:	KY/vm/u000002/i000005
		//		userPath:	KY/vm/u000002
		//
		if(s_logger.isInfoEnabled())
			s_logger.info("Storage create. template: " + templatePath + ", imagePath: " + imagePath
				+ ", userPath: " + userPath + ", dataPath: " + dataPath);
		
		synchronized(this) {
			MockVolume templateVolume = storage.get(templatePath);
			if(templateVolume == null) {
				s_logger.warn("Unable to find template volume, templatePath: " + templatePath);
				return "Unable to find template, template volume path : " + templatePath;
			}
			
			String volumeName = getVolumeName(imagePath, null);
			addVolume(volumeName, new MockVolume(volumeName, templateVolume.getSize()));
		}
		return null;
	}
	
	@Override
    public String create(String templatePath, String imagePath, String userPath, long disksize) {
		if(s_logger.isInfoEnabled())
			s_logger.info("Storage create with disk size. template: " + templatePath + ", imagePath: " + imagePath
				+ ", userPath: " + userPath + ", disk size: " + disksize);

		//
		// Parameter value examples
		//		templatePath: tank/volumes/demo/template/public/os/winxpsp3
		//		imagePath:	KY/vm/u000002/i000005
		//		userPath:	KY/vm/u000002
		//
		synchronized(this) {
			if(templatePath != null) {
				MockVolume templateVolume = storage.get(templatePath);
				if(templateVolume == null) {
					s_logger.warn("Unable to find template volume, templatePath: " + templatePath);
					return "Unable to find template, template volume path : " + templatePath;
				}
			}
			
			String volumeName = getVolumeName(imagePath, 1L);
			addVolume(volumeName, new MockVolume(volumeName, disksize));
		}
    	return null;
    }
	
	@Override
    public String delete(String imagePath) {
		if(s_logger.isInfoEnabled())
			s_logger.info("Storage delete. imagePath: " + imagePath);
		
		synchronized(this) {
			String volume = getVolumeName(imagePath, null);
			storage.remove(volume);
			
			volume = getVolumeName(imagePath, 1L);
			storage.remove(volume);
		}
		
		return null;
	}
	
	@Override
    public String destroy(String imagePath) {
		if(s_logger.isInfoEnabled())
			s_logger.info("Storage destroy. imagePath: " + imagePath);
		
		return null;
	}
	
	@Override
    public long getSize(String imagePath) {
		long size = 0;
		synchronized(this) {
			String volume = getVolumeName(imagePath, null);
			size += getVolumeSize(volume); 
			
			volume = getVolumeName(imagePath, 1L);
			size += getVolumeSize(volume);
		}
		return size;
	}
	
	@Override
    public String getVolumeName(String imagePath, Long diskNum) {
		return imagePath + "-mockdisk" + diskNum;
	}
	
	@Override
    public long getVolumeSize(String volume) {
		synchronized(this) {
			MockVolume v = storage.get(volume);
			if(v != null)
				return v.getSize();
		}
		return 0L;
	}
	

	@Override
	public int getHostStorage() {
		return DEFAULT_HOST_STORAGE_SIZE_GB;
	}
}

