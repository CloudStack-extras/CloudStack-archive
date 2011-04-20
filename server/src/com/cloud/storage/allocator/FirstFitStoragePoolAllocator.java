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
package com.cloud.storage.allocator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.server.StatsCollector;
import com.cloud.storage.StoragePool;
import com.cloud.storage.StoragePoolVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

@Local(value=StoragePoolAllocator.class)
public class FirstFitStoragePoolAllocator extends AbstractStoragePoolAllocator {
    private static final Logger s_logger = Logger.getLogger(FirstFitStoragePoolAllocator.class);

    @Override
    public boolean allocatorIsCorrectType(DiskProfile dskCh) {
    	return !localStorageAllocationNeeded(dskCh);
    }

    @Override
	public List<StoragePool> allocateToPool(DiskProfile dskCh, VirtualMachineTemplate VMtemplate, DeploymentPlan plan, ExcludeList avoid, int returnUpTo) {

	    
	    VMTemplateVO template = (VMTemplateVO)VMtemplate;
	    
    	List<StoragePool> suitablePools = new ArrayList<StoragePool>();

    	// Check that the allocator type is correct
        if (!allocatorIsCorrectType(dskCh)) {
        	return suitablePools;
        }
		long dcId = plan.getDataCenterId();
		Long podId = plan.getPodId();
		Long clusterId = plan.getClusterId();

        if(dskCh.getTags() != null && dskCh.getTags().length != 0){
        	s_logger.debug("Looking for pools in dc: " + dcId + "  pod:" + podId + "  cluster:" + clusterId + " having tags:" + Arrays.toString(dskCh.getTags()));
        }else{
        	s_logger.debug("Looking for pools in dc: " + dcId + "  pod:" + podId + "  cluster:" + clusterId);
        }

		List<StoragePoolVO> pools = _storagePoolDao.findPoolsByTags(dcId, podId, clusterId, dskCh.getTags(), null);
        if (pools.size() == 0) {
    		if (s_logger.isDebugEnabled()) {
    			s_logger.debug("No storage pools available for allocation, returning");
    		}
            return suitablePools;
        }
        
        StatsCollector sc = StatsCollector.getInstance();

        Collections.shuffle(pools);
        
    	if (s_logger.isDebugEnabled()) {
            s_logger.debug("FirstFitStoragePoolAllocator has " + pools.size() + " pools to check for allocation");
        }
    	
        for (StoragePoolVO pool: pools) {
        	if(suitablePools.size() == returnUpTo){
        		break;
        	}
        	if (checkPool(avoid, pool, dskCh, template, null, sc, plan)) {
        		suitablePools.add(pool);
        	}
        }
        
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("FirstFitStoragePoolAllocator returning "+suitablePools.size() +" suitable storage pools");
        }
        
        return suitablePools;
	}
}
