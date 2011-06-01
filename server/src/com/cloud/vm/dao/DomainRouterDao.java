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
package com.cloud.vm.dao;

import java.util.List;

import com.cloud.network.router.VirtualRouter.Role;
import com.cloud.utils.db.GenericDao;
import com.cloud.vm.DomainRouterVO;

/**
 *
 *  DomainRouterDao implements
 */
public interface DomainRouterDao extends GenericDao<DomainRouterVO, Long> {
    /**
     * gets the DomainRouterVO by user id and data center
     * @Param dcId data center Id.
     * @return list of DomainRouterVO
     */
    public List<DomainRouterVO> listByDataCenter(long dcId);
    
    /**
     * gets the DomainRouterVO by account id and data center
     * @param account id of the user.
     * @Param dcId data center Id.
     * @return DomainRouterVO
     */
    public List<DomainRouterVO> findBy(long accountId, long dcId);
    
    /**
     * gets the DomainRouterVO by user id.
     * @param userId id of the user.
     * @Param dcId data center Id.
     * @return list of DomainRouterVO
     */
    public List<DomainRouterVO> listBy(long userId);
    
    /**
     * list virtual machine routers by host id.  pass in null to get all
     * virtual machine routers.
     * @param hostId id of the host.  null if to get all.
     * @return list of DomainRouterVO
     */
    public List<DomainRouterVO> listByHostId(Long hostId);
    public List<DomainRouterVO> listByLastHostId(Long hostId);
    
    /**
     * list virtual machine routers by host id.  exclude destroyed, stopped, expunging VM, 
     * pass in null to get all
     * virtual machine routers.
     * @param hostId id of the host.  null if to get all.
     * @return list of DomainRouterVO
     */
    public List<DomainRouterVO> listVirtualUpByHostId(Long hostId);
    
	/**
	 * Find the list of domain routers for a domain
	 * @param id
	 * @return
	 */
	public List<DomainRouterVO> listByDomain(Long id);

	List<DomainRouterVO> findBy(long accountId, long dcId, Role role);
	
	List<DomainRouterVO> findByNetwork(long networkId);

	List<DomainRouterVO> findByNetworkAndPod(long networkId, long podId);
}
