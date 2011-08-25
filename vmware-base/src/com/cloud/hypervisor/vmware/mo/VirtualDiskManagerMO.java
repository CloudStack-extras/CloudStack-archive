/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.hypervisor.vmware.mo;

import org.apache.log4j.Logger;

import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.vmware.vim25.HostDiskDimensionsChs;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualDiskSpec;

public class VirtualDiskManagerMO extends BaseMO {
    private static final Logger s_logger = Logger.getLogger(VirtualDiskManagerMO.class);
    
	public VirtualDiskManagerMO(VmwareContext context, ManagedObjectReference morDiskMgr) {
		super(context, morDiskMgr);
	}
	
	public VirtualDiskManagerMO(VmwareContext context, String morType, String morValue) {
		super(context, morType, morValue);
	}
	
    public void copyVirtualDisk(String srcName, ManagedObjectReference morSrcDc,
    	String destName, ManagedObjectReference morDestDc, VirtualDiskSpec diskSpec, 
    	boolean force) throws Exception {
    	
    	ManagedObjectReference morTask = _context.getService().copyVirtualDisk_Task(_mor, srcName, morSrcDc, destName, morDestDc, diskSpec, force);
    	
		String result = _context.getServiceUtil().waitForTask(morTask);
		if(!result.equals("sucess"))
			throw new Exception("Unable to copy virtual disk " + srcName + " to " + destName 
				+ " due to " + TaskMO.getTaskFailureInfo(_context, morTask));
		
		_context.waitForTaskProgressDone(morTask);
    }
    
    public void createVirtualDisk(String name, ManagedObjectReference morDc, VirtualDiskSpec diskSpec) throws Exception {
    	ManagedObjectReference morTask = _context.getService().createVirtualDisk_Task(_mor, name, morDc, diskSpec);
    	
		String result = _context.getServiceUtil().waitForTask(morTask);
		if(!result.equals("sucess"))
			throw new Exception("Unable to create virtual disk " + name 
				+ " due to " + TaskMO.getTaskFailureInfo(_context, morTask));

		_context.waitForTaskProgressDone(morTask);
    }
    
    public void defragmentVirtualDisk(String name, ManagedObjectReference morDc) throws Exception {
    	ManagedObjectReference morTask = _context.getService().defragmentVirtualDisk_Task(_mor, name, morDc);
    	
		String result = _context.getServiceUtil().waitForTask(morTask);
		if(!result.equals("sucess"))
			throw new Exception("Unable to defragment virtual disk " + name + " due to " + result);

		_context.waitForTaskProgressDone(morTask);
    }
    
    public void deleteVirtualDisk(String name, ManagedObjectReference morDc) throws Exception {
    	ManagedObjectReference morTask = _context.getService().deleteVirtualDisk_Task(_mor, name, morDc);
    	
		String result = _context.getServiceUtil().waitForTask(morTask);
		if(!result.equals("sucess"))
			throw new Exception("Unable to delete virtual disk " + name + " due to " + TaskMO.getTaskFailureInfo(_context, morTask));

		_context.waitForTaskProgressDone(morTask);
    }
    
    public void eagerZeroVirtualDisk(String name, ManagedObjectReference morDc) throws Exception {
    	ManagedObjectReference morTask = _context.getService().eagerZeroVirtualDisk_Task(_mor, name, morDc);
    	
		String result = _context.getServiceUtil().waitForTask(morTask);
		if(!result.equals("sucess"))
			throw new Exception("Unable to eager zero virtual disk " + name + " due to " + TaskMO.getTaskFailureInfo(_context, morTask));

		_context.waitForTaskProgressDone(morTask);
    }
    
    public void extendVirtualDisk(String name, ManagedObjectReference morDc, long newCapacityKb, boolean eagerZero) throws Exception {
    	ManagedObjectReference morTask = _context.getService().extendVirtualDisk_Task(_mor, name, morDc, newCapacityKb, eagerZero);
    	
		String result = _context.getServiceUtil().waitForTask(morTask);
		if(!result.equals("sucess"))
			throw new Exception("Unable to extend virtual disk " + name + " due to " + TaskMO.getTaskFailureInfo(_context, morTask));

		_context.waitForTaskProgressDone(morTask);
    }
    
    public void inflateVirtualDisk(String name, ManagedObjectReference morDc) throws Exception {
    	ManagedObjectReference morTask = _context.getService().inflateVirtualDisk_Task(_mor, name, morDc);
    	
		String result = _context.getServiceUtil().waitForTask(morTask);
		if(!result.equals("sucess"))
			throw new Exception("Unable to inflate virtual disk " + name + " due to " + TaskMO.getTaskFailureInfo(_context, morTask));
		_context.waitForTaskProgressDone(morTask);
    }
    
    public void shrinkVirtualDisk(String name, ManagedObjectReference morDc, boolean copy) throws Exception {
    	ManagedObjectReference morTask = _context.getService().shrinkVirtualDisk_Task(_mor, name, morDc, copy);
    	
		String result = _context.getServiceUtil().waitForTask(morTask);
		if(!result.equals("sucess"))
			throw new Exception("Unable to shrink virtual disk " + name + " due to " + TaskMO.getTaskFailureInfo(_context, morTask));
		_context.waitForTaskProgressDone(morTask);
    }
    
    public void zeroFillVirtualDisk(String name, ManagedObjectReference morDc) throws Exception {
    	ManagedObjectReference morTask = _context.getService().zeroFillVirtualDisk_Task(_mor, name, morDc);
    	
		String result = _context.getServiceUtil().waitForTask(morTask);
		if(!result.equals("sucess"))
			throw new Exception("Unable to zero fill virtual disk " + name + " due to " + TaskMO.getTaskFailureInfo(_context, morTask));
		_context.waitForTaskProgressDone(morTask);
    }
    
    public void moveVirtualDisk(String srcName, ManagedObjectReference morSrcDc,
    	String destName, ManagedObjectReference morDestDc, boolean force) throws Exception {
    	
    	ManagedObjectReference morTask = _context.getService().moveVirtualDisk_Task(_mor, srcName, morSrcDc,
    		destName, morDestDc, force);
    	
		String result = _context.getServiceUtil().waitForTask(morTask);
		if(!result.equals("sucess"))
			throw new Exception("Unable to move virtual disk " + srcName + " to " + destName 
				+ " due to " + TaskMO.getTaskFailureInfo(_context, morTask));
		_context.waitForTaskProgressDone(morTask);
    }
    
    public int queryVirtualDiskFragmentation(String name, ManagedObjectReference morDc) throws Exception {
    	return _context.getService().queryVirtualDiskFragmentation(_mor, name, morDc);
    }
    
    public HostDiskDimensionsChs queryVirtualDiskGeometry(String name, ManagedObjectReference morDc) throws Exception {
    	return _context.getService().queryVirtualDiskGeometry(_mor, name, morDc);
    }
    
    public String queryVirtualDiskUuid(String name, ManagedObjectReference morDc) throws Exception {
    	return _context.getService().queryVirtualDiskUuid(_mor, name, morDc);
    }
    
    public void setVirtualDiskUuid(String name, ManagedObjectReference morDc, String uuid) throws Exception {
    	_context.getService().setVirtualDiskUuid(_mor, name, morDc, uuid);
    }
}
