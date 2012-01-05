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
package com.cloud.storage;

public class Storage {
    public static enum ImageFormat {
        QCOW2(true, true, false),
        RAW(false, false, false),
        VHD(true, true, true),
        ISO(false, false, false),
        OVA(true, true, true, "ova"),
        BAREMETAL(false, false, false);
        
        private final boolean thinProvisioned;
        private final boolean supportSparse;
        private final boolean supportSnapshot;
        private final String fileExtension;
        
        private ImageFormat(boolean thinProvisioned, boolean supportSparse, boolean supportSnapshot) {
            this.thinProvisioned = thinProvisioned;
            this.supportSparse = supportSparse;
            this.supportSnapshot = supportSnapshot;
            fileExtension = null;
        }
        
        private ImageFormat(boolean thinProvisioned, boolean supportSparse, boolean supportSnapshot, String fileExtension) {
            this.thinProvisioned = thinProvisioned;
            this.supportSparse = supportSparse;
            this.supportSnapshot = supportSnapshot;
            this.fileExtension = fileExtension;
        }
        
        public boolean isThinProvisioned() {
            return thinProvisioned;
        }
        
        public boolean supportsSparse() {
            return supportSparse;
        }
        
        public boolean supportSnapshot() {
            return supportSnapshot;
        }
        
        public String getFileExtension() {
        	if(fileExtension == null)
        		return toString().toLowerCase();
        	
        	return fileExtension;
        }
    }
    
    public static enum FileSystem {
        Unknown,
        ext3,
        ntfs,
        fat,
        fat32,
        ext2,
        ext4,
        cdfs,
        hpfs,
        ufs,
        hfs,
        hfsp
    }
    
    public static enum TemplateType {
    	SYSTEM, /*routing, system vm template*/
    	BUILTIN, /*buildin template*/
    	PERHOST, /* every host has this template, don't need to install it in secondary storage */
    	USER /* User supplied template/iso */
    }
    
    public static enum StoragePoolType {
        Filesystem(false), //local directory
        NetworkFilesystem(true), //NFS or CIFS
        IscsiLUN(true), //shared LUN, with a clusterfs overlay
        Iscsi(true), //for e.g., ZFS Comstar
        ISO(false),    // for iso image
        LVM(false),    // XenServer local LVM SR
        SharedMountPoint(true),
        VMFS(true),		// VMware VMFS storage
        PreSetup(true),  // for XenServer, Storage Pool is set up by customers. 
        OCFS2(true),
        EXT(false);    // XenServer local EXT SR
        
        
        boolean shared;
        
        StoragePoolType(boolean shared) {
            this.shared = shared;
        }
        
        public boolean isShared() {
            return shared;
        }
    }

    public static enum StorageResourceType {STORAGE_POOL, STORAGE_HOST, SECONDARY_STORAGE, LOCAL_SECONDARY_STORAGE}
}
