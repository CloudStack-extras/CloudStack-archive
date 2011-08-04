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

import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.cloud.utils.Pair;
import com.vmware.apputils.vim25.ServiceUtil;
import com.vmware.vim25.CustomFieldStringValue;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.TraversalSpec;

public class DatacenterMO extends BaseMO {
	
	public DatacenterMO(VmwareContext context, ManagedObjectReference morDc) {
		super(context, morDc);
	}
	
	public DatacenterMO(VmwareContext context, String morType, String morValue) {
		super(context, morType, morValue);
	}
	
	public DatacenterMO(VmwareContext context, String dcName) throws Exception {
		super(context, null);
		
		_mor = _context.getServiceUtil().getDecendentMoRef(_context.getRootFolder(), "Datacenter", dcName);
		assert(_mor != null);
	}
	
	public String getName() throws Exception {
		return (String)_context.getServiceUtil().getDynamicProperty(_mor, "name");
	}
	
	public void registerTemplate(ManagedObjectReference morHost, String datastoreName, 
		String templateName, String templateFileName) throws Exception {
		
		ServiceUtil serviceUtil = _context.getServiceUtil();
		
		ManagedObjectReference morFolder = (ManagedObjectReference)serviceUtil.getDynamicProperty(
			_mor, "vmFolder");
		assert(morFolder != null);
		
		ManagedObjectReference morTask = _context.getService().registerVM_Task(
    		 morFolder, 
    		 String.format("[%s] %s/%s", datastoreName, templateName, templateFileName),
    		 templateName, true, 
    		 null, morHost);
		
		String result = serviceUtil.waitForTask(morTask);
		if (!result.equalsIgnoreCase("Sucess")) {
			throw new Exception("Unable to register template due to " + TaskMO.getTaskFailureInfo(_context, morTask));
		} else {
			_context.waitForTaskProgressDone(morTask);
		}
	}
	
	public VirtualMachineMO findVm(String vmName) throws Exception {
		ObjectContent[] ocs = getVmPropertiesOnDatacenterVmFolder(new String[] { "name" });
		if(ocs != null && ocs.length > 0) {
			for(ObjectContent oc : ocs) {
				DynamicProperty[] props = oc.getPropSet();
				if(props != null) {
					for(DynamicProperty prop : props) {
						if(prop.getVal().toString().equals(vmName))
							return new VirtualMachineMO(_context, oc.getObj());
					}
				}
			}
		}
		return null;
	}
	
	public List<VirtualMachineMO> findVmByNameAndLabel(String vmLabel) throws Exception {
		CustomFieldsManagerMO cfmMo = new CustomFieldsManagerMO(_context, 
				_context.getServiceContent().getCustomFieldsManager());
		int key = cfmMo.getCustomFieldKey("VirtualMachine", CustomFieldConstants.CLOUD_UUID);
		assert(key != 0);
		
		List<VirtualMachineMO> list = new ArrayList<VirtualMachineMO>();
		
		ObjectContent[] ocs = getVmPropertiesOnDatacenterVmFolder(new String[] { "name",  String.format("value[%d]", key)});
		if(ocs != null && ocs.length > 0) {
			for(ObjectContent oc : ocs) {
				DynamicProperty[] props = oc.getPropSet();
				if(props != null) {
					for(DynamicProperty prop : props) {
						if(prop.getVal() != null) {
							if(prop.getName().equalsIgnoreCase("name")) {
								if(prop.getVal().toString().equals(vmLabel)) {
									list.add(new VirtualMachineMO(_context, oc.getObj()));
									break;		// break out inner loop
								}
							} else if(prop.getVal() instanceof CustomFieldStringValue) {
								String val = ((CustomFieldStringValue)prop.getVal()).getValue();
								if(val.equals(vmLabel)) {
									list.add(new VirtualMachineMO(_context, oc.getObj()));
									break;		// break out inner loop
								}
							}
						}
					}
				}
			}
		}
		return list;
	}
	
	public List<Pair<ManagedObjectReference, String>> getAllVmsOnDatacenter() throws Exception {
	    List<Pair<ManagedObjectReference, String>> vms = new ArrayList<Pair<ManagedObjectReference, String>>();
	    
	    ObjectContent[] ocs = getVmPropertiesOnDatacenterVmFolder(new String[] { "name" });
	    if(ocs != null) {
	        for(ObjectContent oc : ocs) {
	            String vmName = oc.getPropSet(0).getVal().toString();
	            vms.add(new Pair<ManagedObjectReference, String>(oc.getObj(), vmName));
	        }
	    }
	    
	    return vms;
	}	
	
	public ManagedObjectReference findDatastore(String name) throws Exception {
		assert(name != null);
		
		ObjectContent[] ocs = getDatastorePropertiesOnDatacenter(new String[] { "name" });
		if(ocs != null) {
			for(ObjectContent oc : ocs) {
				if(oc.getPropSet(0).getVal().toString().equals(name)) {
					return oc.getObj();
				}
			}
		}
		return null;
	}
	
	public ManagedObjectReference findHost(String name) throws Exception {
		ObjectContent[] ocs= getHostPropertiesOnDatacenterHostFolder(new String[] { "name" });
		
		if(ocs != null) {
			for(ObjectContent oc : ocs) {
				if(oc.getPropSet(0).getVal().toString().equals(name)) {
					return oc.getObj();
				}
			}
		}
		return null;
	}
	
	public ManagedObjectReference getVmFolder() throws Exception {
		return (ManagedObjectReference)_context.getServiceUtil().getDynamicProperty(_mor, "vmFolder");
	}
	
	public ObjectContent[] getHostPropertiesOnDatacenterHostFolder(String[] propertyPaths) throws Exception {
		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("HostSystem");
		pSpec.setPathSet(propertyPaths);
		
	    TraversalSpec computeResource2HostTraversal = new TraversalSpec();
	    computeResource2HostTraversal.setType("ComputeResource");
	    computeResource2HostTraversal.setPath("host");
	    computeResource2HostTraversal.setName("computeResource2HostTraversal");
		
	    SelectionSpec recurseFolders = new SelectionSpec();
	    recurseFolders.setName("folder2childEntity");
	      
	    TraversalSpec folder2childEntity = new TraversalSpec();
	    folder2childEntity.setType("Folder");
	    folder2childEntity.setPath("childEntity");
	    folder2childEntity.setName(recurseFolders.getName());
	    folder2childEntity.setSelectSet(new SelectionSpec[] { recurseFolders, computeResource2HostTraversal });
	    
	    TraversalSpec dc2HostFolderTraversal = new TraversalSpec();
	    dc2HostFolderTraversal.setType("Datacenter");
	    dc2HostFolderTraversal.setPath("hostFolder");
	    dc2HostFolderTraversal.setName("dc2HostFolderTraversal");
	    dc2HostFolderTraversal.setSelectSet(new SelectionSpec[] { folder2childEntity } );
	    
	    ObjectSpec oSpec = new ObjectSpec();
	    oSpec.setObj(_mor);
	    oSpec.setSkip(Boolean.TRUE);
	    oSpec.setSelectSet(new SelectionSpec[] { dc2HostFolderTraversal });

	    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
	    pfSpec.setPropSet(new PropertySpec[] { pSpec });
	    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
	    
	    return _context.getService().retrieveProperties(
	    	_context.getServiceContent().getPropertyCollector(), 
	    	new PropertyFilterSpec[] { pfSpec }); 
	}
	
	public ObjectContent[] getDatastorePropertiesOnDatacenter(String[] propertyPaths) throws Exception {
		
		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("Datastore");
		pSpec.setPathSet(propertyPaths);
		
	    TraversalSpec dc2DatastoreTraversal = new TraversalSpec();
	    dc2DatastoreTraversal.setType("Datacenter");
	    dc2DatastoreTraversal.setPath("datastore");
	    dc2DatastoreTraversal.setName("dc2DatastoreTraversal");
	    
	    ObjectSpec oSpec = new ObjectSpec();
	    oSpec.setObj(_mor);
	    oSpec.setSkip(Boolean.TRUE);
	    oSpec.setSelectSet(new SelectionSpec[] { dc2DatastoreTraversal });

	    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
	    pfSpec.setPropSet(new PropertySpec[] { pSpec });
	    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
	    
	    return _context.getService().retrieveProperties(
	    	_context.getServiceContent().getPropertyCollector(), 
	    	new PropertyFilterSpec[] { pfSpec }); 
	}
	
	public ObjectContent[] getVmPropertiesOnDatacenterVmFolder(String[] propertyPaths) throws Exception {
		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("VirtualMachine");
		pSpec.setPathSet(propertyPaths);
		
	    TraversalSpec dc2VmFolderTraversal = new TraversalSpec();
	    dc2VmFolderTraversal.setType("Datacenter");
	    dc2VmFolderTraversal.setPath("vmFolder");
	    dc2VmFolderTraversal.setName("dc2VmFolderTraversal");
	    
	    SelectionSpec recurseFolders = new SelectionSpec();
	    recurseFolders.setName("folder2childEntity");
	      
	    TraversalSpec folder2childEntity = new TraversalSpec();
	    folder2childEntity.setType("Folder");
	    folder2childEntity.setPath("childEntity");
	    folder2childEntity.setName(recurseFolders.getName());
	    folder2childEntity.setSelectSet(new SelectionSpec[] { recurseFolders });
	    dc2VmFolderTraversal.setSelectSet(new SelectionSpec[] { folder2childEntity } );

	    ObjectSpec oSpec = new ObjectSpec();
	    oSpec.setObj(_mor);
	    oSpec.setSkip(Boolean.TRUE);
	    oSpec.setSelectSet(new SelectionSpec[] { dc2VmFolderTraversal });

	    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
	    pfSpec.setPropSet(new PropertySpec[] { pSpec });
	    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
	    
	    return _context.getService().retrieveProperties(
	    	_context.getServiceContent().getPropertyCollector(), 
	    	new PropertyFilterSpec[] { pfSpec }); 
	}
	
	public static Pair<DatacenterMO, String> getOwnerDatacenter(VmwareContext context, 
		ManagedObjectReference morEntity) throws Exception {
		
		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("Datacenter");
		pSpec.setPathSet(new String[] { "name" });
		
	    TraversalSpec entityParentTraversal = new TraversalSpec();
	    entityParentTraversal.setType("ManagedEntity");
	    entityParentTraversal.setPath("parent");
	    entityParentTraversal.setName("entityParentTraversal");
	    entityParentTraversal.setSelectSet(new SelectionSpec[] { new SelectionSpec(null, null, "entityParentTraversal") });

	    ObjectSpec oSpec = new ObjectSpec();
	    oSpec.setObj(morEntity);
	    oSpec.setSkip(Boolean.TRUE);
	    oSpec.setSelectSet(new SelectionSpec[] { entityParentTraversal });

	    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
	    pfSpec.setPropSet(new PropertySpec[] { pSpec });
	    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
	    
	    ObjectContent[] ocs = context.getService().retrieveProperties(
	    	context.getServiceContent().getPropertyCollector(), 
	    	new PropertyFilterSpec[] { pfSpec });
	    
	    assert(ocs != null);
	    assert(ocs[0].getObj() != null);
	    assert(ocs[0].getPropSet(0) != null);
	    assert(ocs[0].getPropSet(0).getVal() != null);
	    
	    String dcName = ocs[0].getPropSet(0).getVal().toString(); 
	    return new Pair<DatacenterMO, String>(new DatacenterMO(context, ocs[0].getObj()), dcName); 
	}
}
