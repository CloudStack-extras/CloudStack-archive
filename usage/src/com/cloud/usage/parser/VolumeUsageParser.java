/**
 *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
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
 */

package com.cloud.usage.parser;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cloud.usage.UsageServer;
import com.cloud.usage.UsageTypes;
import com.cloud.usage.UsageVO;
import com.cloud.usage.UsageVolumeVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageVolumeDao;
import com.cloud.user.AccountVO;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ComponentLocator;

public class VolumeUsageParser {
	public static final Logger s_logger = Logger.getLogger(VolumeUsageParser.class.getName());
	
	private static ComponentLocator _locator = ComponentLocator.getLocator(UsageServer.Name, "usage-components.xml", "log4j-cloud_usage");
	private static UsageDao m_usageDao = _locator.getDao(UsageDao.class);
	private static UsageVolumeDao m_usageVolumeDao = _locator.getDao(UsageVolumeDao.class);
	
	public static boolean parse(AccountVO account, Date startDate, Date endDate) {
	    if (s_logger.isDebugEnabled()) {
	        s_logger.debug("Parsing all Volume usage events for account: " + account.getId());
	    }
		if ((endDate == null) || endDate.after(new Date())) {
			endDate = new Date();
		}

        // - query usage_volume table with the following criteria:
        //     - look for an entry for accountId with start date in the given range
        //     - look for an entry for accountId with end date in the given range
        //     - look for an entry for accountId with end date null (currently running vm or owned IP)
        //     - look for an entry for accountId with start date before given range *and* end date after given range
        List<UsageVolumeVO> usageUsageVols = m_usageVolumeDao.getUsageRecords(account.getId(), account.getDomainId(), startDate, endDate, false, 0);
        
        if(usageUsageVols.isEmpty()){
        	s_logger.debug("No volume usage events for this period");
        	return true;
        }

        // This map has both the running time *and* the usage amount.
        Map<String, Pair<Long, Long>> usageMap = new HashMap<String, Pair<Long, Long>>();

        Map<String, VolInfo> diskOfferingMap = new HashMap<String, VolInfo>();

		// loop through all the usage volumes, create a usage record for each
        for (UsageVolumeVO usageVol : usageUsageVols) {
            long volId = usageVol.getId();
            Long doId = usageVol.getDiskOfferingId();
            long zoneId = usageVol.getZoneId();
            Long templateId = usageVol.getTemplateId();
            long size = usageVol.getSize();
            String key = ""+volId;

            diskOfferingMap.put(key, new VolInfo(volId, zoneId, doId, templateId, size));
            
            Date volCreateDate = usageVol.getCreated();
            Date volDeleteDate = usageVol.getDeleted();

            if ((volDeleteDate == null) || volDeleteDate.after(endDate)) {
            	volDeleteDate = endDate;
            }

            // clip the start date to the beginning of our aggregation range if the vm has been running for a while
            if (volCreateDate.before(startDate)) {
            	volCreateDate = startDate;
            }

            long currentDuration = (volDeleteDate.getTime() - volCreateDate.getTime()) + 1; // make sure this is an inclusive check for milliseconds (i.e. use n - m + 1 to find total number of millis to charge)


            updateVolUsageData(usageMap, key, usageVol.getId(), currentDuration);
        }

        for (String volIdKey : usageMap.keySet()) {
            Pair<Long, Long> voltimeInfo = usageMap.get(volIdKey);
            long useTime = voltimeInfo.second().longValue();

            // Only create a usage record if we have a runningTime of bigger than zero.
            if (useTime > 0L) {
                VolInfo info = diskOfferingMap.get(volIdKey);
                createUsageRecord(UsageTypes.VOLUME, useTime, startDate, endDate, account, info.getVolumeId(), info.getZoneId(), info.getDiskOfferingId(), info.getTemplateId(), info.getSize());
            }
        }

        return true;
	}

	private static void updateVolUsageData(Map<String, Pair<Long, Long>> usageDataMap, String key, long volId, long duration) {
        Pair<Long, Long> volUsageInfo = usageDataMap.get(key);
        if (volUsageInfo == null) {
        	volUsageInfo = new Pair<Long, Long>(new Long(volId), new Long(duration));
        } else {
            Long runningTime = volUsageInfo.second();
            runningTime = new Long(runningTime.longValue() + duration);
            volUsageInfo = new Pair<Long, Long>(volUsageInfo.first(), runningTime);
        }
        usageDataMap.put(key, volUsageInfo);
	}

	private static void createUsageRecord(int type, long runningTime, Date startDate, Date endDate, AccountVO account, long volId, long zoneId, Long doId, Long templateId, long size) {
        // Our smallest increment is hourly for now
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Total running time " + runningTime + "ms");
        }

        float usage = runningTime / 1000f / 60f / 60f;

        DecimalFormat dFormat = new DecimalFormat("#.######");
        String usageDisplay = dFormat.format(usage);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating Volume usage record for vol: " + volId + ", usage: " + usageDisplay + ", startDate: " + startDate + ", endDate: " + endDate + ", for account: " + account.getId());
        }

        // Create the usage record
        String usageDesc = "Volume Id: "+volId+" usage time";

        if(templateId != null){
            usageDesc += " (Template: " +templateId+ ")";
        } else if(doId != null){
            usageDesc += " (DiskOffering: " +doId+ ")";
        } 
        
        UsageVO usageRecord = new UsageVO(zoneId, account.getId(), account.getDomainId(), usageDesc, usageDisplay + " Hrs", type,
                new Double(usage), null, null, doId, templateId, volId, size, startDate, endDate);
        m_usageDao.persist(usageRecord);
    }

	private static class VolInfo {
	    private long volId;
	    private long zoneId;
        private Long diskOfferingId;
        private Long templateId;
        private long size;

	    public VolInfo(long volId, long zoneId, Long diskOfferingId, Long templateId, long size) {
	        this.volId = volId;
	        this.zoneId = zoneId;
	        this.diskOfferingId = diskOfferingId;
	        this.templateId = templateId;
	        this.size = size;
	    }
	    public long getZoneId() {
	        return zoneId;
	    }
	    public long getVolumeId() {
	        return volId;
	    }
	    public Long getDiskOfferingId() {
	        return diskOfferingId;
	    }
        public Long getTemplateId() {
            return templateId;
        }	    
        public long getSize() {
            return size;
        }
	}
}
