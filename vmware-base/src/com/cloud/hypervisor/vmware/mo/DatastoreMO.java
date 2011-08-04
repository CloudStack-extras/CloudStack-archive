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

package com.cloud.hypervisor.vmware.mo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.cloud.utils.Pair;
import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.TraversalSpec;

public class DatastoreMO extends BaseMO {
	private static final Logger s_logger = Logger.getLogger(DatastoreMO.class);
	
	// cache copy to reduce calls to vCenter
	private String _name;							
	private Pair<DatacenterMO, String> _ownerDc;	
	
	public DatastoreMO(VmwareContext context, ManagedObjectReference morDatastore) {
		super(context, morDatastore);
	}
	
	public DatastoreMO(VmwareContext context, String morType, String morValue) {
		super(context, morType, morValue);
	}

	public String getName() throws Exception {
		if(_name == null) 
			_name = (String)_context.getServiceUtil().getDynamicProperty(_mor, "name");
		
		return _name;
	}
	
	public DatastoreSummary getSummary() throws Exception {
		return (DatastoreSummary)_context.getServiceUtil().getDynamicProperty(_mor, "summary");
	}
	
	public String getInventoryPath() throws Exception {
		Pair<DatacenterMO, String> dcInfo = getOwnerDatacenter();
		return dcInfo.second() + "/" + getName();
	}
	
	public Pair<DatacenterMO, String> getOwnerDatacenter() throws Exception {
		if(_ownerDc != null)
			return _ownerDc;
		
		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("Datacenter");
		pSpec.setPathSet(new String[] { "name" });
		
	    TraversalSpec folderParentTraversal = new TraversalSpec();
	    folderParentTraversal.setType("Folder");
	    folderParentTraversal.setPath("parent");
	    folderParentTraversal.setName("folderParentTraversal");
	    folderParentTraversal.setSelectSet(new SelectionSpec[] { new SelectionSpec(null, null, "folderParentTraversal") });
	    
	    TraversalSpec dsParentTraversal = new TraversalSpec();
	    dsParentTraversal.setType("Datastore");
	    dsParentTraversal.setPath("parent");
	    dsParentTraversal.setName("dsParentTraversal");
	    dsParentTraversal.setSelectSet(new SelectionSpec[] { folderParentTraversal });

	    ObjectSpec oSpec = new ObjectSpec();
	    oSpec.setObj(getMor());
	    oSpec.setSkip(Boolean.TRUE);
	    oSpec.setSelectSet(new SelectionSpec[] { dsParentTraversal });

	    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
	    pfSpec.setPropSet(new PropertySpec[] { pSpec });
	    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
	    
	    ObjectContent[] ocs = _context.getService().retrieveProperties(
	    	_context.getServiceContent().getPropertyCollector(), 
	    	new PropertyFilterSpec[] { pfSpec });
	    
	    assert(ocs != null);
	    assert(ocs[0].getObj() != null);
	    assert(ocs[0].getPropSet() != null);
	    String dcName = ocs[0].getPropSet()[0].getVal().toString(); 
	    _ownerDc = new Pair<DatacenterMO, String>(new DatacenterMO(_context, ocs[0].getObj()), dcName);
	    return _ownerDc;
	}
	
	public void makeDirectory(String path, ManagedObjectReference morDc) throws Exception {
		String datastoreName = getName();
		ManagedObjectReference morFileManager = _context.getServiceContent().getFileManager();
		
		String fullPath = path;
		if(!DatastoreFile.isFullDatastorePath(fullPath)) 
			fullPath = String.format("[%s] %s", datastoreName, path);
		
		_context.getService().makeDirectory(morFileManager, fullPath, morDc, true);
	}
	
	public boolean deleteFile(String path, ManagedObjectReference morDc, boolean testExistence) throws Exception {
		String datastoreName = getName();
		ManagedObjectReference morFileManager = _context.getServiceContent().getFileManager();
		
		String fullPath = path;
		if(!DatastoreFile.isFullDatastorePath(fullPath))
			fullPath = String.format("[%s] %s", datastoreName, path);

		try {
			if(testExistence && !fileExists(fullPath))
				return true;
		} catch(Exception e) {
			s_logger.info("Unable to test file existence due to exception " + e.getClass().getName() + ", skip deleting of it");
			return true;
		}
		
		ManagedObjectReference morTask = _context.getService().deleteDatastoreFile_Task(morFileManager, 
			fullPath, morDc);
		
		String result = _context.getServiceUtil().waitForTask(morTask);
		if(result.equals("sucess")) {
			_context.waitForTaskProgressDone(morTask);
			return true;
		} else {
        	s_logger.error("VMware deleteDatastoreFile_Task failed due to " + TaskMO.getTaskFailureInfo(_context, morTask));
		}
		return false;
	}
	
	public boolean copyDatastoreFile(String srcFilePath, ManagedObjectReference morSrcDc,
		ManagedObjectReference morDestDs, String destFilePath, ManagedObjectReference morDestDc, 
		boolean forceOverwrite) throws Exception {
		
		String srcDsName = getName();
		DatastoreMO destDsMo = new DatastoreMO(_context, morDestDs);
		String destDsName = destDsMo.getName();
		
		ManagedObjectReference morFileManager = _context.getServiceContent().getFileManager();
		String srcFullPath = srcFilePath;
		if(!DatastoreFile.isFullDatastorePath(srcFullPath))
			srcFullPath = String.format("[%s] %s", srcDsName, srcFilePath);
		
		String destFullPath = destFilePath;
		if(!DatastoreFile.isFullDatastorePath(destFullPath))
			destFullPath = String.format("[%s] %s", destDsName, destFilePath);
		
		ManagedObjectReference morTask = _context.getService().copyDatastoreFile_Task(morFileManager, 
			srcFullPath, morSrcDc, destFullPath, morDestDc, forceOverwrite);
		
		String result = _context.getServiceUtil().waitForTask(morTask);
		if(result.equals("sucess")) {
			_context.waitForTaskProgressDone(morTask);
			return true;
		} else {
        	s_logger.error("VMware copyDatastoreFile_Task failed due to " + TaskMO.getTaskFailureInfo(_context, morTask));
		}
		return false;
	}
	
	public boolean moveDatastoreFile(String srcFilePath, ManagedObjectReference morSrcDc,
		ManagedObjectReference morDestDs, String destFilePath, ManagedObjectReference morDestDc, 
		boolean forceOverwrite) throws Exception {
		
		String srcDsName = getName();
		DatastoreMO destDsMo = new DatastoreMO(_context, morDestDs);
		String destDsName = destDsMo.getName();
		
		ManagedObjectReference morFileManager = _context.getServiceContent().getFileManager();
		String srcFullPath = srcFilePath;
		if(!DatastoreFile.isFullDatastorePath(srcFullPath))
			srcFullPath = String.format("[%s] %s", srcDsName, srcFilePath);
		
		String destFullPath = destFilePath;
		if(!DatastoreFile.isFullDatastorePath(destFullPath))
			destFullPath = String.format("[%s] %s", destDsName, destFilePath);
		
		ManagedObjectReference morTask = _context.getService().moveDatastoreFile_Task(morFileManager, 
			srcFullPath, morSrcDc, destFullPath, morDestDc, forceOverwrite);
		
		String result = _context.getServiceUtil().waitForTask(morTask);
		if(result.equals("sucess")) {
			_context.waitForTaskProgressDone(morTask);
			return true;
		} else {
        	s_logger.error("VMware moveDatgastoreFile_Task failed due to " + TaskMO.getTaskFailureInfo(_context, morTask));
		}
		return false;
	}
	
	public String[] getVmdkFileChain(String rootVmdkDatastoreFullPath) throws Exception {
		Pair<DatacenterMO, String> dcPair = getOwnerDatacenter();
		
		List<String> files = new ArrayList<String>();
		files.add(rootVmdkDatastoreFullPath);
		
		String currentVmdkFullPath = rootVmdkDatastoreFullPath;
		while(true) {
			String url = getContext().composeDatastoreBrowseUrl(dcPair.second(), currentVmdkFullPath);
			byte[] content = getContext().getResourceContent(url);
			if(content == null || content.length == 0)
				break;
			
			VmdkFileDescriptor descriptor = new VmdkFileDescriptor();
			descriptor.parse(content);
			
			String parentFileName = descriptor.getParentFileName();
			if(parentFileName == null)
				break;

			if(parentFileName.startsWith("/")) {
				// when parent file is not at the same directory as it is, assume it is at parent directory
				// this is only valid in cloud.com primary storage deployment
				DatastoreFile dsFile = new DatastoreFile(currentVmdkFullPath);
				String dir = dsFile.getDir();
				if(dir != null && dir.lastIndexOf('/') > 0)
					dir = dir.substring(0, dir.lastIndexOf('/'));
				else
					dir = "";
				
				currentVmdkFullPath = new DatastoreFile(dsFile.getDatastoreName(), dir, 
					parentFileName.substring(parentFileName.lastIndexOf('/') + 1)).getPath();
				files.add(currentVmdkFullPath);
			} else {
				currentVmdkFullPath = DatastoreFile.getCompanionDatastorePath(currentVmdkFullPath, parentFileName);
				files.add(currentVmdkFullPath);
			}
		}
		
		return files.toArray(new String[0]);
	}
	
	public String[] listDirContent(String path) throws Exception {
		String fullPath = path;
		if(!DatastoreFile.isFullDatastorePath(fullPath))
			fullPath = String.format("[%s] %s", getName(), fullPath);
		
		Pair<DatacenterMO, String> dcPair = getOwnerDatacenter();
		String url = getContext().composeDatastoreBrowseUrl(dcPair.second(), fullPath);
		
		// TODO, VMware currently does not have a formal API to list Datastore directory content,
		// folloing hacking may have performance hit if datastore has a large number of files
		return _context.listDatastoreDirContent(url);
	}
	
	public boolean fileExists(String fileFullPath) throws Exception {
		DatastoreFile file = new DatastoreFile(fileFullPath);
		DatastoreFile dirFile = new DatastoreFile(file.getDatastoreName(), file.getDir());
		
		String[] fileNames = listDirContent(dirFile.getPath());
		
		String fileName = file.getFileName();
		for(String name : fileNames) {
			if(name.equalsIgnoreCase(fileName))
				return true;
		} 
		
		return false;
	}
}
