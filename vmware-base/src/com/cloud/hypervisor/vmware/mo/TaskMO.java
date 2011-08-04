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

import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.vmware.vim25.LocalizableMessage;
import com.vmware.vim25.LocalizedMethodFault;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.TaskInfoState;

public class TaskMO extends BaseMO {
    public TaskMO(VmwareContext context, ManagedObjectReference morTask) {
        super(context, morTask);
    }
    
    public TaskMO(VmwareContext context, String morType, String morValue) {
        super(context, morType, morValue);
    }
    
    public TaskInfo getTaskInfo() throws Exception {
		return (TaskInfo)getContext().getServiceUtil().getDynamicProperty(_mor, "info");
    }
    
    public void setTaskDescription(LocalizableMessage description) throws Exception {
    	_context.getService().setTaskDescription(_mor, description);
    }
    
    public void setTaskState(TaskInfoState state, Object result, LocalizedMethodFault fault) throws Exception {
    	_context.getService().setTaskState(_mor, state, result, fault);
    }
    
    public void updateProgress(int percentDone) throws Exception {
    	_context.getService().updateProgress(_mor, percentDone);
    }
    
    public void cancelTask() throws Exception {
    	_context.getService().cancelTask(_mor);
    }
    
    public static String getTaskFailureInfo(VmwareContext context, ManagedObjectReference morTask) {
    	StringBuffer sb = new StringBuffer();
    	
    	try {
    		TaskInfo info = (TaskInfo)context.getServiceUtil().getDynamicProperty(morTask, "info");
    		if(info != null) {
    			LocalizedMethodFault fault = info.getError();
    			if(fault != null) {
    				sb.append(fault.getLocalizedMessage()).append(" ");
    				
    				if(fault.getFault() != null)
    					sb.append(fault.getFault().getClass().getName());
    			}
    		}
    	} catch(Exception e) {
    	}
    	
    	return sb.toString();
    }
}
