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
import java.util.Set;

import com.cloud.api.commands.CreateAccountCmd;
import com.cloud.api.commands.CreateDomainCmd;
import com.cloud.api.commands.CreateUserCmd;
import com.cloud.api.commands.DeleteAccountCmd;
import com.cloud.api.commands.DeleteUserCmd;
import com.cloud.api.commands.DisableAccountCmd;
import com.cloud.api.commands.DisableUserCmd;
import com.cloud.api.commands.EnableAccountCmd;
import com.cloud.api.commands.EnableUserCmd;
import com.cloud.api.commands.ListResourceLimitsCmd;
import com.cloud.api.commands.LockUserCmd;
import com.cloud.api.commands.UpdateAccountCmd;
import com.cloud.api.commands.UpdateResourceCountCmd;
import com.cloud.api.commands.UpdateResourceLimitCmd;
import com.cloud.api.commands.UpdateUserCmd;
import com.cloud.configuration.ResourceCount;
import com.cloud.configuration.ResourceLimit;
import com.cloud.domain.Domain;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.utils.Pair;

public interface AccountService {

    /**
     * Creates a new user, stores the password as is so encrypted passwords are recommended.
     * 
     * @param cmd
     *            the create command that has the username, email, password, account name, domain, timezone, etc. for creating
     *            the user.
     * @return the user if created successfully, null otherwise
     */
    UserAccount createAccount(CreateAccountCmd cmd);

    /**
     * Deletes a user by userId
     * 
     * @param cmd
     *            - the delete command defining the id of the user to be deleted.
     * @return true if delete was successful, false otherwise
     */
    boolean deleteUserAccount(DeleteAccountCmd cmd);

    /**
     * Disables a user by userId
     * 
     * @param cmd
     *            the command wrapping the userId parameter
     * @return UserAccount object
     */
    UserAccount disableUser(DisableUserCmd cmd);

    /**
     * Enables a user
     * 
     * @param cmd
     *            - the command containing userId
     * @return UserAccount object
     */
    UserAccount enableUser(EnableUserCmd cmd);

    /**
     * Locks a user by userId. A locked user cannot access the API, but will still have running VMs/IP addresses allocated/etc.
     * 
     * @param userId
     * @return UserAccount object
     */
    UserAccount lockUser(LockUserCmd cmd);

    /**
     * Update a user by userId
     * 
     * @param userId
     * @return UserAccount object
     */
    UserAccount updateUser(UpdateUserCmd cmd);

    /**
     * Disables an account by accountName and domainId
     * 
     * @param disabled
     *            account if success
     * @return true if disable was successful, false otherwise
     */
    Account disableAccount(DisableAccountCmd cmd) throws ConcurrentOperationException, ResourceUnavailableException;

    /**
     * Enables an account by accountId
     * 
     * @param cmd
     *            - the enableAccount command defining the accountId to be deleted.
     * @return account object
     */
    Account enableAccount(EnableAccountCmd cmd);

    /**
     * Locks an account by accountId. A locked account cannot access the API, but will still have running VMs/IP addresses
     * allocated/etc.
     * 
     * @param cmd
     *            - the LockAccount command defining the accountId to be locked.
     * @return account object
     */
    Account lockAccount(DisableAccountCmd cmd);

    /**
     * Updates an account name
     * 
     * @param cmd
     *            - the parameter containing accountId
     * @return updated account object
     */

    Account updateAccount(UpdateAccountCmd cmd);

    /**
     * Updates an existing resource limit with the specified details. If a limit doesn't exist, will create one.
     * 
     * @param cmd
     *            the command that wraps the domainId, accountId, type, and max parameters
     * @return the updated/created resource limit
     */
    ResourceLimit updateResourceLimit(UpdateResourceLimitCmd cmd);


    /**
     * Updates an existing resource count details for the account/domain
     * 
     * @param cmd
     *            the command that wraps the domainId, accountId, resource type  parameters
     * @return the updated/created resource counts
     */
    List<? extends ResourceCount> updateResourceCount(UpdateResourceCountCmd cmd);
    
    /**
     * Search for resource limits for the given id and/or account and/or type and/or domain.
     * 
     * @param cmd
     *            the command wrapping the id, type, account, and domain
     * @return a list of limits that match the criteria
     */
    List<? extends ResourceLimit> searchForLimits(ListResourceLimitsCmd cmd);

    Account getSystemAccount();

    User getSystemUser();

    User createUser(CreateUserCmd cmd);
	boolean deleteUser(DeleteUserCmd deleteUserCmd);
	
	boolean isAdmin(short accountType);
	
	Account finalizeOwner(Account caller, String accountName, Long domainId);
	
	Pair<String, Long> finalizeAccountDomainForList(Account caller, String accountName, Long domainId);
	
	Account getActiveAccount(String accountName, Long domainId);
	
	Account getActiveAccount(Long accountId);
	
	Account getAccount(Long accountId);
	
	User getActiveUser(long userId);
	
	User getUser(long userId);
	
	Domain getDomain(long id);
	
	boolean isRootAdmin(short accountType);
	
	User getActiveUserByRegistrationToken(String registrationToken);
	
	void markUserRegistered(long userId);
	
	Set<Long> getDomainParentIds(long domainId);
	
	Set<Long> getDomainChildrenIds(String parentDomainPath);

    Domain createDomain(CreateDomainCmd cmd);

}
