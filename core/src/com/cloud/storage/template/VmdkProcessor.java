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

package com.cloud.storage.template;

import java.io.File;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.log4j.Logger;

import com.cloud.exception.InternalErrorException;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.StorageLayer;
import com.cloud.utils.script.Script;

@Local(value=Processor.class)
public class VmdkProcessor implements Processor {
    private static final Logger s_logger = Logger.getLogger(VmdkProcessor.class);

    String _name;
    StorageLayer _storage;
	
    @Override
    public FormatInfo process(String templatePath, ImageFormat format, String templateName) throws InternalErrorException {
        if (format != null) {
        	if(s_logger.isInfoEnabled())
        		s_logger.info("We currently don't handle conversion from " + format + " to VMDK.");
            return null;
        }
        
        s_logger.info("Template processing. templatePath: " + templatePath + ", templateName: " + templateName);
        String templateFilePath = templatePath + File.separator + templateName + "." + ImageFormat.OVA.getFileExtension();
        if (!_storage.exists(templateFilePath)) {
        	if(s_logger.isInfoEnabled())
        		s_logger.info("Unable to find the vmware template file: " + templateFilePath);
            return null;
        }
        
        s_logger.info("Template processing - untar OVA package. templatePath: " + templatePath + ", templateName: " + templateName);
        String templateFileFullPath = templatePath + templateName + "." + ImageFormat.OVA.getFileExtension();
        File templateFile = new File(templateFileFullPath);
        
        Script command = new Script("tar", 0, s_logger);
        command.add("--no-same-owner", templateFileFullPath);
        command.add("-xf", templateFileFullPath);
        command.setWorkDir(templateFile.getParent());
        String result = command.execute();
        if (result != null) {
            s_logger.info("failed to untar OVA package due to " + result + ". templatePath: " + templatePath + ", templateName: " + templateName);
        	return null;
        }
        
        FormatInfo info = new FormatInfo();
        info.format = ImageFormat.OVA;
        info.filename = templateName + "." + ImageFormat.OVA.getFileExtension();
        info.size = _storage.getSize(templateFilePath);
        info.virtualSize = getTemplateVirtualSize(templatePath, info.filename);

        // delete original OVA file
        // templateFile.delete();
        return info;
    }

    public long getTemplateVirtualSize(String templatePath, String templateName) throws InternalErrorException {
        // get the virtual size from the OVF file meta data
    	long virtualSize=0;
        String templateFileFullPath = templatePath.endsWith(File.separator) ? templatePath : templatePath + File.separator;
    	templateFileFullPath += templateName.endsWith(ImageFormat.OVA.getFileExtension()) ? templateName : templateName + "." + ImageFormat.OVA.getFileExtension();
        String ovfFileName = getOVFFilePath(templateFileFullPath);
        if(ovfFileName == null) {
            String msg = "Unable to locate OVF file in template package directory: " + templatePath; 
            s_logger.error(msg);
            throw new InternalErrorException(msg);
        }
        try {
            Document ovfDoc = null;
        	ovfDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(ovfFileName));
        	Element disk  = (Element) ovfDoc.getElementsByTagName("Disk").item(0);
        	virtualSize = Long.parseLong(disk.getAttribute("ovf:capacity"));
        	String allocationUnits = disk.getAttribute("ovf:capacityAllocationUnits");
        	if ((virtualSize != 0) && (allocationUnits != null)) {
        		long units = 1;
        		if (allocationUnits.equalsIgnoreCase("KB") || allocationUnits.equalsIgnoreCase("KiloBytes") || allocationUnits.equalsIgnoreCase("byte * 2^10")) {
        			units = 1024;
        		} else if (allocationUnits.equalsIgnoreCase("MB") || allocationUnits.equalsIgnoreCase("MegaBytes") || allocationUnits.equalsIgnoreCase("byte * 2^20")) {
        			units = 1024 * 1024;
        		} else if (allocationUnits.equalsIgnoreCase("GB") || allocationUnits.equalsIgnoreCase("GigaBytes") || allocationUnits.equalsIgnoreCase("byte * 2^30")) {
        			units = 1024 * 1024 * 1024;
        		}
        		virtualSize = virtualSize * units;
        	} else {
        		throw new InternalErrorException("Failed to read capacity and capacityAllocationUnits from the OVF file: " + ovfFileName);
        	}
        	return virtualSize;
        } catch (Exception e) {
        	String msg = "Unable to parse OVF XML document to get the virtual disk size due to"+e;
        	s_logger.error(msg);
        	throw new InternalErrorException(msg);
        }
    }
    
    private String getOVFFilePath(String srcOVAFileName) {
        File file = new File(srcOVAFileName);
        assert(_storage != null);
        String[] files = _storage.listFiles(file.getParent());
        if(files != null) {
            for(String fileName : files) {
                if(fileName.toLowerCase().endsWith(".ovf")) {
                    File ovfFile = new File(fileName);
                    return file.getParent() + File.separator + ovfFile.getName();
                }
            }
        }
        return null;
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _name = name;
        _storage = (StorageLayer)params.get(StorageLayer.InstanceConfigKey);
        if (_storage == null) {
            throw new ConfigurationException("Unable to get storage implementation");
        }
    	
    	return true;
    }
    
    @Override
    public String getName() {
        return _name;
    }
    
    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }
}
