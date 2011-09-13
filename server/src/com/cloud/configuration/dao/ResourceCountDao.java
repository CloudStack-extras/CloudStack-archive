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

package com.cloud.configuration.dao;

import java.util.Set;

import com.cloud.configuration.ResourceCount.ResourceType;
import com.cloud.configuration.ResourceCountVO;
import com.cloud.configuration.ResourceLimit.OwnerType;
import com.cloud.utils.db.GenericDao;

public interface ResourceCountDao extends GenericDao<ResourceCountVO, Long> {

	/**
	 * Get the count of in use resources for an account by type
	 * @param accountId the id of the account to get the resource count
	 * @param type the type of resource (e.g. user_vm, public_ip, volume)
	 * @return the count of resources in use for the given type and account
	 */
	public long getAccountCount(long accountId, ResourceType type);

	/**
	 * Get the count of in use resources for a domain by type
	 * @param domainId the id of the domain to get the resource count
	 * @param type the type of resource (e.g. user_vm, public_ip, volume)
	 * @return the count of resources in use for the given type and domain
	 */
	public long getDomainCount(long domainId, ResourceType type);

	/**
	 * Set the count of in use resources for an account by type
	 * @param accountId the id of the account to set the resource count
	 * @param type the type of resource (e.g. user_vm, public_ip, volume)
	 * @param the count of resources in use for the given type and account
	 */
	public void setAccountCount(long accountId, ResourceType type, long count);

	/**
	 * Get the count of in use resources for a domain by type
	 * @param domainId the id of the domain to set the resource count
	 * @param type the type of resource (e.g. user_vm, public_ip, volume)
	 * @param the count of resources in use for the given type and domain
	 */
	public void setDomainCount(long domainId, ResourceType type, long count);

	/**
	 * Update the count of resources in use for the given domain and given resource type
	 * @param domainId the id of the domain to update resource count
	 * @param type the type of resource (e.g. user_vm, public_ip, volume)
	 * @param increment whether the change is adding or subtracting from the current count
	 * @param delta the number of resources being added/released
	 */
	public void updateDomainCount(long domainId, ResourceType type, boolean increment, long delta);

    boolean updateById(long id, boolean increment, long delta);

    ResourceCountVO findByDomainIdAndType(long domainId, ResourceType type);

    ResourceCountVO findByAccountIdAndType(long accountId, ResourceType type);
    
    Set<Long> listAllRowsToUpdateForAccount(long accountId, long domainId, ResourceType type);
    
    Set<Long> listRowsToUpdateForDomain(long domainId, ResourceType type);

    void createResourceCounts(long ownerId, OwnerType ownerType);
    
}
