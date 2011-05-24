/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
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
package com.cloud.network.eip;

import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import com.cloud.vm.DomainRouterVO;


@Local(value={ElasticIpVmAllocator.class})
public class ElasticIpVmDefaultAllocator implements ElasticIpVmAllocator {
	
    private String _name = "ElasticIpVmDefaultAllocator";
    private Random _rand = new Random(System.currentTimeMillis());

    @Override
	public DomainRouterVO allocElasticIpVm(List<DomainRouterVO> candidates, Map<Long, Integer> loadInfo, long dataCenterId) {
    	if(candidates.size() > 0)
			return candidates.get(_rand.nextInt(candidates.size()));
    	return null;
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
     
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
