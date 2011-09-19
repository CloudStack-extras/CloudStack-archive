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

package com.cloud.user;

import java.util.List;

import com.cloud.acl.ControlledEntity;
import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.commands.CreateUserCmd;
import com.cloud.configuration.ResourceCount;
import com.cloud.configuration.ResourceCount.ResourceType;
import com.cloud.configuration.ResourceLimitVO;
import com.cloud.domain.Domain;
import com.cloud.domain.DomainVO;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.server.Criteria;

/**
 * AccountManager includes logic that deals with accounts, domains, and users.
 *
 */
public interface AccountManager extends AccountService {

	/**
	 * Finds all ISOs that are usable for a user. This includes ISOs usable for the user's account and for all of the account's parent domains.
	 * @param userId
	 * @return List of IsoVOs
	 */
	// public List<VMTemplateVO> findAllIsosForUser(long userId);
	
	/**
	 * Finds the resource limit for a specified account and type. If the account has an infinite limit, will check
	 * the account's parent domain, and if that limit is also infinite, will return the ROOT domain's limit.
	 * @param accountId
	 * @param type
	 * @return resource limit
	 */
    public long findCorrectResourceLimit(long accountId, ResourceType type);

    /**
     * Finds the resource limit for a specified domain and type. If the domain has an infinite limit, will check
     * up the domain hierarchy
     * @param account
     * @param type
     * @return resource limit
     */
    public long findCorrectResourceLimit(DomainVO domain, ResourceType type);

    /**
	 * Updates the resource count of an account to reflect current usage by account
	 * @param accountId
	 * @param type
	 */
	public long updateAccountResourceCount(long accountId, ResourceType type);

    /**
	 * Updates the resource count of the domain to reflect current usage in the domain
	 * @param domainId
	 * @param type
	 */
	public long updateDomainResourceCount(long domainId, ResourceType type);
    /**
	 * Increments the resource count
	 * @param accountId
	 * @param type
	 * @param delta
	 */
	public void incrementResourceCount(long accountId, ResourceType type, Long...delta);
    
	/**
	 * Decrements the resource count
	 * @param accountId
	 * @param type
	 * @param delta
	 */
    public void decrementResourceCount(long accountId, ResourceType type, Long...delta);
	
	/**
	 * Checks if a limit has been exceeded for an account
	 * @param account
	 * @param type
	 * @param count the number of resources being allocated, count will be added to current allocation and compared against maximum allowed allocation
	 * @return true if the limit has been exceeded
	 */
	public boolean resourceLimitExceeded(Account account, ResourceCount.ResourceType type, long...count);
	
	/**
	 * Gets the count of resources for a resource type and account
	 * @param account
	 * @param type
	 * @return count of resources
	 */
	public long getResourceCount(AccountVO account, ResourceType type);

    /**
     * Disables an account by accountId
     * @param accountId
     * @return true if disable was successful, false otherwise
     */
    boolean disableAccount(long accountId) throws ConcurrentOperationException, ResourceUnavailableException;
    
    boolean deleteAccount(AccountVO account, long callerUserId, Account caller);
    
    void checkAccess(Account account, Domain domain) throws PermissionDeniedException;
    
    void checkAccess(Account account, AccessType accessType, ControlledEntity... entities) throws PermissionDeniedException;

	boolean cleanupAccount(AccountVO account, long callerUserId, Account caller);

	@Override
    UserVO createUser(CreateUserCmd cmd);

	Long checkAccessAndSpecifyAuthority(Account caller, Long zoneId);

}
