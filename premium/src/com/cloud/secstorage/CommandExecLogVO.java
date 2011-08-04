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

package com.cloud.secstorage;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.cloud.utils.DateUtil;

@Entity
@Table(name="cmd_exec_log")
public class CommandExecLogVO {
	
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
	private long id;
    
    @Column(name="host_id")
	private long hostId;
    
    @Column(name="instance_id")
	private long instanceId;
	
    @Column(name="command_name")
	private String commandName;
    
    @Column(name="weight")
    private int weight;
	
    @Column(name="created")
    @Temporal(value=TemporalType.TIMESTAMP)
    private Date created;
    
    public CommandExecLogVO() {
    }
    
    public CommandExecLogVO(long hostId, long instanceId, String commandName, int weight) {
    	this.hostId = hostId;
    	this.instanceId = instanceId;
    	this.commandName = commandName;
    	this.weight = weight;
    	this.created = DateUtil.currentGMTTime();
    }
    
    public long getId() {
    	return this.id;
    }
    
    public long getHostId() {
    	return this.hostId;
    }
    
    public void setHostId(long hostId) {
    	this.hostId = hostId;
    }
    
    public long getInstanceId() {
    	return this.instanceId;
    }
    
    public void setInstanceId(long instanceId) {
    	this.instanceId = instanceId;
    }
    
    public String getCommandName() {
    	return this.commandName;
    }
    
    public void setCommandName(String commandName) {
    	this.commandName = commandName;
    }
    
    public int getWeight() {
    	return weight;
    }
    
    public void setWeight(int weight) {
    	this.weight = weight;
    }
    
    public Date getCreated() {
    	return this.created;
    }
    
    public void setCreated(Date created) {
    	this.created = created;
    }
}
