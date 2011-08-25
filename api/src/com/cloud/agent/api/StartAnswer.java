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

/**
 * 
 */
package com.cloud.agent.api;

import com.cloud.agent.api.to.VirtualMachineTO;

public class StartAnswer extends Answer {
    VirtualMachineTO vm;
    String host_guid;
    
    protected StartAnswer() {
    }
    
    public StartAnswer(StartCommand cmd, String msg) {
        super(cmd, false, msg);
        this.vm  = cmd.getVirtualMachine();
    }
    
    public StartAnswer(StartCommand cmd, Exception e) {
        super(cmd, false, e.getMessage());
        this.vm  = cmd.getVirtualMachine();
    }
    
    public StartAnswer(StartCommand cmd) {
        super(cmd, true, null);
        this.vm  = cmd.getVirtualMachine();
        this.host_guid = null;
    }

    public StartAnswer(StartCommand cmd, String msg, String guid) {
        super(cmd, true, msg);
        this.vm  = cmd.getVirtualMachine();
        this.host_guid = guid;
    }
    
    public VirtualMachineTO getVirtualMachine() {
        return vm;
    }

    public String getHost_guid() {
        return host_guid;
    }
}
