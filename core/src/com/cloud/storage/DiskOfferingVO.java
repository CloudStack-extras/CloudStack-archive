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

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.cloud.offering.DiskOffering;
import com.cloud.utils.db.GenericDao;

@Entity
@Table(name="disk_offering")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="type", discriminatorType=DiscriminatorType.STRING, length=32)
public class DiskOfferingVO implements DiskOffering {
    public enum Type {
        Disk,
        Service
    };
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    long id;

    @Column(name="domain_id")
    Long domainId;

    @Column(name="unique_name")
    private String uniqueName;
    
    @Column(name="name")
    private String name = null;

    @Column(name="display_text", length=4096)
    private String displayText = null;

    @Column(name="disk_size")
    long diskSize;

    @Column(name="tags", length=4096)
    String tags;
    
    @Column(name="type")
    Type type;
    
    @Column(name=GenericDao.REMOVED)
    @Temporal(TemporalType.TIMESTAMP)
    private Date removed;

    @Column(name=GenericDao.CREATED_COLUMN)
    private Date created;
    
    @Column(name="recreatable")
    private boolean recreatable;
    
    @Column(name="use_local_storage")
    private boolean useLocalStorage;
    
    @Column(name="system_use")
    private boolean systemUse;
    
    @Column(name="customized")
    private boolean customized;
    
    public DiskOfferingVO() {
    }

    public DiskOfferingVO(Long domainId, String name, String displayText, long diskSize, String tags, boolean isCustomized) {
        this.domainId = domainId;
        this.name = name;
        this.displayText = displayText;
        this.diskSize = diskSize;
        this.tags = tags;
        this.recreatable = false;
        this.type = Type.Disk;
        this.useLocalStorage = false;
        this.customized = isCustomized;
    }
    
    public DiskOfferingVO(String name, String displayText, boolean mirrored, String tags, boolean recreatable, boolean useLocalStorage, boolean systemUse, boolean customized) {
        this.domainId = null;
        this.type = Type.Service;
        this.name = name;
        this.displayText = displayText;
        this.tags = tags;
        this.recreatable = recreatable;
        this.useLocalStorage = useLocalStorage;
        this.systemUse = systemUse;
        this.customized = customized;
    }

    //domain specific offerings constructor (null domainId implies public offering)
    public DiskOfferingVO(String name, String displayText, boolean mirrored, String tags, boolean recreatable, boolean useLocalStorage, boolean systemUse, boolean customized, Long domainId) {
        this.type = Type.Service;
        this.name = name;
        this.displayText = displayText;
        this.tags = tags;
        this.recreatable = recreatable;
        this.useLocalStorage = useLocalStorage;
        this.systemUse = systemUse;
        this.customized = customized;
        this.domainId = domainId;
    }

    @Override
    public long getId() {
        return id;
    }
    
    @Override
    public boolean isCustomized() {
		return customized;
	}

	public void setCustomized(boolean customized) {
		this.customized = customized;
	}

	@Override
    public String getUniqueName() {
        return uniqueName;
    }
    
    @Override
    public boolean getUseLocalStorage() {
        return useLocalStorage;
    }
    
    @Override
    public Long getDomainId() {
        return domainId;
    }
    
    public Type getType() {
        return type;
    }
    
    public boolean isRecreatable() {
        return recreatable;
    }
    
    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }

    @Override
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    

    @Override
    public boolean getSystemUse() {
        return systemUse;
    }
    
    public void setSystemUse(boolean systemUse) {
        this.systemUse = systemUse;
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }
    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    @Override
    public long getDiskSize(){
    	return diskSize;
    }
       
    @Override
    public void setDiskSize(long diskSize) {
        this.diskSize = diskSize;
    }

    public Date getRemoved() {
        return removed;
    }
    
	@Override
    public Date getCreated() {
		return created;
	}
	
    protected void setTags(String tags) {
        this.tags = tags;
    }
    
    @Override
    public String getTags() {
        return tags;
    }
    
    public void setUniqueName(String name) {
        this.uniqueName = name;
    }

    @Override
    @Transient
    public String[] getTagsArray() {
        String tags = getTags();
        if (tags == null || tags.isEmpty()) {
            return new String[0];
        }
        
        return tags.split(",");
    }

    @Transient
    public boolean containsTag(String... tags) {
        if (this.tags == null) {
            return false;
        }
        
        for (String tag : tags) {
            if (!this.tags.matches(tag)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Transient
    public void setTagsArray(List<String> newTags) {
        if (newTags.isEmpty()) {
            setTags(null);
            return;
        }
        
        StringBuilder buf = new StringBuilder();
        for (String tag : newTags) {
            buf.append(tag).append(",");
        }
        
        buf.delete(buf.length() - 1, buf.length());
        
        setTags(buf.toString());
    }

	public void setUseLocalStorage(boolean useLocalStorage) {
		this.useLocalStorage = useLocalStorage;
	}

    public void setRemoved(Date removed) {
        this.removed = removed;
    }
}
