/**
 * Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
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

package com.cloud.usage.parser;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cloud.usage.UsageNetworkOfferingVO;
import com.cloud.usage.UsageServer;
import com.cloud.usage.UsageTypes;
import com.cloud.usage.UsageVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageNetworkOfferingDao;
import com.cloud.user.AccountVO;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ComponentLocator;

public class NetworkOfferingUsageParser {
	public static final Logger s_logger = Logger.getLogger(NetworkOfferingUsageParser.class.getName());
	
	private static ComponentLocator _locator = ComponentLocator.getLocator(UsageServer.Name, "usage-components.xml", "log4j-cloud_usage");
	private static UsageDao m_usageDao = _locator.getDao(UsageDao.class);
	private static UsageNetworkOfferingDao m_usageNetworkOfferingDao = _locator.getDao(UsageNetworkOfferingDao.class);
	
	public static boolean parse(AccountVO account, Date startDate, Date endDate) {
	    if (s_logger.isDebugEnabled()) {
	        s_logger.debug("Parsing all NetworkOffering usage events for account: " + account.getId());
	    }
		if ((endDate == null) || endDate.after(new Date())) {
			endDate = new Date();
		}

        // - query usage_volume table with the following criteria:
        //     - look for an entry for accountId with start date in the given range
        //     - look for an entry for accountId with end date in the given range
        //     - look for an entry for accountId with end date null (currently running vm or owned IP)
        //     - look for an entry for accountId with start date before given range *and* end date after given range
        List<UsageNetworkOfferingVO> usageNOs = m_usageNetworkOfferingDao.getUsageRecords(account.getId(), account.getDomainId(), startDate, endDate, false, 0);
        
        if(usageNOs.isEmpty()){
        	s_logger.debug("No NetworkOffering usage events for this period");
        	return true;
        }

        // This map has both the running time *and* the usage amount.
        Map<String, Pair<Long, Long>> usageMap = new HashMap<String, Pair<Long, Long>>();
        Map<String, NOInfo> noMap = new HashMap<String, NOInfo>();

		// loop through all the network offerings, create a usage record for each
        for (UsageNetworkOfferingVO usageNO : usageNOs) {
            long vmId = usageNO.getVmInstanceId();
            long noId = usageNO.getNetworkOfferingId();
            String key = ""+vmId+"NO"+noId;
            
            noMap.put(key, new NOInfo(vmId, usageNO.getZoneId(), noId, usageNO.isDefault()));
            
            Date noCreateDate = usageNO.getCreated();
            Date noDeleteDate = usageNO.getDeleted();

            if ((noDeleteDate == null) || noDeleteDate.after(endDate)) {
                noDeleteDate = endDate;
            }

            // clip the start date to the beginning of our aggregation range if the vm has been running for a while
            if (noCreateDate.before(startDate)) {
                noCreateDate = startDate;
            }

            long currentDuration = (noDeleteDate.getTime() - noCreateDate.getTime()) + 1; // make sure this is an inclusive check for milliseconds (i.e. use n - m + 1 to find total number of millis to charge)


            updateNOUsageData(usageMap, key, usageNO.getVmInstanceId(), currentDuration);
        }

        for (String noIdKey : usageMap.keySet()) {
            Pair<Long, Long> notimeInfo = usageMap.get(noIdKey);
            long useTime = notimeInfo.second().longValue();

            // Only create a usage record if we have a runningTime of bigger than zero.
            if (useTime > 0L) {
                NOInfo info = noMap.get(noIdKey);
                createUsageRecord(UsageTypes.NETWORK_OFFERING, useTime, startDate, endDate, account, info.getVmId(), info.getNOId(), info.getZoneId(), info.isDefault());
            }
        }

        return true;
	}

	private static void updateNOUsageData(Map<String, Pair<Long, Long>> usageDataMap, String key, long vmId, long duration) {
        Pair<Long, Long> noUsageInfo = usageDataMap.get(key);
        if (noUsageInfo == null) {
            noUsageInfo = new Pair<Long, Long>(new Long(vmId), new Long(duration));
        } else {
            Long runningTime = noUsageInfo.second();
            runningTime = new Long(runningTime.longValue() + duration);
            noUsageInfo = new Pair<Long, Long>(noUsageInfo.first(), runningTime);
        }
        usageDataMap.put(key, noUsageInfo);
	}

	private static void createUsageRecord(int type, long runningTime, Date startDate, Date endDate, AccountVO account, long vmId, long noId, long zoneId, boolean isDefault) {
        // Our smallest increment is hourly for now
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Total running time " + runningTime + "ms");
        }

        float usage = runningTime / 1000f / 60f / 60f;

        DecimalFormat dFormat = new DecimalFormat("#.######");
        String usageDisplay = dFormat.format(usage);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating network offering:" + noId + " usage record for Vm : " + vmId + ", usage: " + usageDisplay + ", startDate: " + startDate + ", endDate: " + endDate + ", for account: " + account.getId());
        }

        // Create the usage record
        String usageDesc = "Network offering:" + noId + " for Vm : " + vmId + " usage time";

        long defaultNic = (isDefault) ? 1 : 0;
        UsageVO usageRecord = new UsageVO(zoneId, account.getId(), account.getDomainId(), usageDesc, usageDisplay + " Hrs", type,
                new Double(usage), vmId, null, noId, null, defaultNic, null, startDate, endDate);
        m_usageDao.persist(usageRecord);
    }
	
	private static class NOInfo {
	    private long vmId;
	    private long zoneId;
	    private long noId;
	    private boolean isDefault;

	    public NOInfo(long vmId, long zoneId, long noId, boolean isDefault) {
	        this.vmId = vmId;
	        this.zoneId = zoneId;
	        this.noId = noId;
	        this.isDefault = isDefault;
	    }
	    public long getZoneId() {
	        return zoneId;
	    }
	    public long getVmId() {
	        return vmId;
	    }
	    public long getNOId() {
	        return noId;
	    }
	    
	    public boolean isDefault(){
	        return isDefault;
	    }
	}

}
