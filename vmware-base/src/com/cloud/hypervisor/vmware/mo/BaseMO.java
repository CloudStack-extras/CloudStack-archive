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

import org.apache.log4j.Logger;

import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.vmware.vim25.CustomFieldDef;
import com.vmware.vim25.CustomFieldStringValue;
import com.vmware.vim25.ManagedObjectReference;

public class BaseMO {
    private static final Logger s_logger = Logger.getLogger(BaseMO.class);
	
	protected VmwareContext _context;
	protected ManagedObjectReference _mor;

	// cached name for the vCenter managed entity object
	private String _name;
	
	public BaseMO(VmwareContext context, ManagedObjectReference mor) {
		assert(context != null);
		
		_context = context;
		_mor = mor;
	}
	
	public BaseMO(VmwareContext context, String morType, String morValue) {
		assert(context != null);
		assert(morType != null);
		assert(morValue != null);
		
		_context = context;
		_mor = new ManagedObjectReference();
		_mor.setType(morType);
		_mor.set_value(morValue);
	}
	
	public VmwareContext getContext() {
		return _context;
	}
	
	public ManagedObjectReference getMor() {
		assert(_mor != null);
		return _mor;
	}
	
	public ManagedObjectReference getParentMor() throws Exception {
		return (ManagedObjectReference)_context.getServiceUtil().getDynamicProperty(_mor, "parent");
	}

	public String getName() throws Exception {
		if(_name == null)
			_name = (String)_context.getServiceUtil().getDynamicProperty(_mor, "name");
		
		return _name;
	}
	
	public boolean destroy() throws Exception {
        ManagedObjectReference morTask = _context.getService().destroy_Task(_mor);
        
        String result = _context.getServiceUtil().waitForTask(morTask);
        if(result.equals("sucess")) {
            _context.waitForTaskProgressDone(morTask);
            return true;
        } else {
        	s_logger.error("VMware destroy_Task failed due to " + TaskMO.getTaskFailureInfo(_context, morTask));
        }
        return false;
	}
	
	public void reload() throws Exception {
	    _context.getService().reload(_mor);
	}
	
	public boolean rename(String newName) throws Exception {
	    ManagedObjectReference morTask = _context.getService().rename_Task(_mor, newName);
	    
        String result = _context.getServiceUtil().waitForTask(morTask);
        if(result.equals("sucess")) {
            _context.waitForTaskProgressDone(morTask);
            return true;
        } else {
        	s_logger.error("VMware rename_Task failed due to " + TaskMO.getTaskFailureInfo(_context, morTask));
        }
        return false;
	}
	
	public void setCustomFieldValue(String fieldName, String value) throws Exception {
		CustomFieldsManagerMO cfmMo = new CustomFieldsManagerMO(_context, 
			_context.getServiceContent().getCustomFieldsManager());
		
		int key = getCustomFieldKey(fieldName);
		if(key == 0) {
			try {
				CustomFieldDef field = cfmMo.addCustomerFieldDef(fieldName, getMor().getType(), null, null);
				key = field.getKey();
			} catch (Exception e) {
				// assuming the exception is caused by concurrent operation from other places
				// so we retieve the key again
				key = getCustomFieldKey(fieldName);
			}
		}
		
		if(key == 0)
			throw new Exception("Unable to setup custom field facility");
		
		cfmMo.setField(getMor(), key, value);
	}
	
	public String getCustomFieldValue(String fieldName) throws Exception {
		int key = getCustomFieldKey(fieldName);
		if(key == 0)
			return null;
		
		CustomFieldStringValue cfValue = (CustomFieldStringValue)_context.getServiceUtil().getDynamicProperty(getMor(), 
			String.format("value[%d]", key));
		if(cfValue != null)
			return cfValue.getValue();
		
		return null;
	}
	
	public int getCustomFieldKey(String fieldName) throws Exception {
		return getCustomFieldKey(getMor().getType(), fieldName);
	}
	
	public int getCustomFieldKey(String morType, String fieldName) throws Exception {
		assert(morType != null);
		
		CustomFieldsManagerMO cfmMo = new CustomFieldsManagerMO(_context, 
				_context.getServiceContent().getCustomFieldsManager());
		
		return cfmMo.getCustomFieldKey(morType, fieldName);
	}
}
