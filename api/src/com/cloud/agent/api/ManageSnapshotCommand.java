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

package com.cloud.agent.api;

import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.storage.StoragePool;


public class ManageSnapshotCommand extends Command {
    // XXX: Should be an enum
    // XXX: Anyway there is something called inheritance in Java
    public static String CREATE_SNAPSHOT = "-c";
    public static String DESTROY_SNAPSHOT = "-d";
    
    private String _commandSwitch;
    
    // Information about the volume that the snapshot is based on
    private String _volumePath = null;
	StorageFilerTO _pool;

    // Information about the snapshot
    private String _snapshotPath = null;
    private String _snapshotName = null;
    private long _snapshotId;
    private String _vmName = null;

    public ManageSnapshotCommand() {}

    public ManageSnapshotCommand(long snapshotId, String volumePath, StoragePool pool, String preSnapshotPath ,String snapshotName, String vmName) {
        _commandSwitch = ManageSnapshotCommand.CREATE_SNAPSHOT;
        _volumePath = volumePath;
        _pool = new StorageFilerTO(pool);        
        _snapshotPath = preSnapshotPath;
        _snapshotName = snapshotName;
        _snapshotId = snapshotId;
        _vmName = vmName;
    }

    public ManageSnapshotCommand(long snapshotId, String snapshotPath) {
        _commandSwitch = ManageSnapshotCommand.DESTROY_SNAPSHOT;
        _snapshotPath = snapshotPath;
    }
    
    
    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getCommandSwitch() {
        return _commandSwitch;
    }
    
    public String getVolumePath() {
        return _volumePath;
    }
    
    public StorageFilerTO getPool() {
        return _pool;
    }
    
    public String getSnapshotPath() {
    	return _snapshotPath;
    }

    public String getSnapshotName() {
        return _snapshotName;
    }

    public long getSnapshotId() {
        return _snapshotId;
    }
    
    public String getVmName() {
    	return _vmName;
    }
    
}