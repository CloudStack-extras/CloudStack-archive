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

import java.util.Calendar;

import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PerfCompositeMetric;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfInterval;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;

public class PerfManagerMO extends BaseMO {
    public PerfManagerMO(VmwareContext context, ManagedObjectReference mor) {
        super(context, mor);
    }
    
    public PerfManagerMO(VmwareContext context, String morType, String morValue) {
        super(context, morType, morValue);
    }
    
    public void createPerfInterval(PerfInterval interval) throws Exception {
        _context.getService().createPerfInterval(_mor, interval);
    }
    
    public PerfMetricId[] queryAvailablePerfMetric(ManagedObjectReference morEntity, Calendar beginTime, 
        Calendar endTime, Integer intervalId) throws Exception {
        
        return _context.getService().queryAvailablePerfMetric(_mor, morEntity, beginTime, endTime, intervalId);
    }

    public PerfCompositeMetric queryPerfComposite(PerfQuerySpec spec) throws Exception {
        return _context.getService().queryPerfComposite(_mor, spec);
    }
    
    public PerfCounterInfo[] queryPerfCounter(int[] counterId) throws Exception {
        return _context.getService().queryPerfCounter(_mor, counterId);
    }
    
    public PerfCounterInfo[] queryPerfCounterByLevel(int level) throws Exception {
        return _context.getService().queryPerfCounterByLevel(_mor, level);
    }
    
    public PerfProviderSummary queryPerfProviderSummary(ManagedObjectReference morEntity) throws Exception {
        return _context.getService().queryPerfProviderSummary(_mor, morEntity); 
    }

    public PerfEntityMetricBase[] queryPerf(PerfQuerySpec[] specs) throws Exception {
        return _context.getService().queryPerf(_mor, specs);
    }
    
    public void removePerfInterval(int samplePeriod) throws Exception {
        _context.getService().removePerfInterval(_mor, samplePeriod);
    }
    
    public void updatePerfInterval(PerfInterval interval) throws Exception {
        _context.getService().updatePerfInterval(_mor, interval);
    }
    
    public PerfCounterInfo[] getCounterInfo() throws Exception {
        return (PerfCounterInfo[])_context.getServiceUtil().getDynamicProperty(_mor, "perfCounter");
    }
    
    public PerfInterval[] getIntervalInfo() throws Exception {
        return (PerfInterval[])_context.getServiceUtil().getDynamicProperty(_mor, "historicalInterval");
    }
}
