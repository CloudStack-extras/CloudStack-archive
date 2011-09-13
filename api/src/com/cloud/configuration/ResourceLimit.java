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

package com.cloud.configuration;

public interface ResourceLimit {
    
    public static enum OwnerType {Account, Domain}
	
	public Long getId();
	
	public void setId(Long id);
	
	public ResourceCount.ResourceType getType();
	
	public void setType(ResourceCount.ResourceType type);
	
	public Long getDomainId();
	
	public void setDomainId(Long domainId);
	
	public Long getAccountId();
	
	public void setAccountId(Long accountId);
	
	public Long getMax();
	
	public void setMax(Long max);
	
}
