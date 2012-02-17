
/**
 * *  Copyright (C) 2012 Citrix Systems, Inc.  All rights reserved
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

package com.cloud.usage.parser;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cloud.usage.UsageSecurityGroupVO;
import com.cloud.usage.UsageServer;
import com.cloud.usage.UsageTypes;
import com.cloud.usage.UsageVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageSecurityGroupDao;
import com.cloud.user.AccountVO;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ComponentLocator;

public class SecurityGroupUsageParser {
	public static final Logger s_logger = Logger.getLogger(SecurityGroupUsageParser.class.getName());
	
	private static ComponentLocator _locator = ComponentLocator.getLocator(UsageServer.Name, "usage-components.xml", "log4j-cloud_usage");
	private static UsageDao m_usageDao = _locator.getDao(UsageDao.class);
	private static UsageSecurityGroupDao m_usageSecurityGroupDao = _locator.getDao(UsageSecurityGroupDao.class);
	
	public static boolean parse(AccountVO account, Date startDate, Date endDate) {
	    if (s_logger.isDebugEnabled()) {
	        s_logger.debug("Parsing all SecurityGroup usage events for account: " + account.getId());
	    }
		if ((endDate == null) || endDate.after(new Date())) {
			endDate = new Date();
		}

        // - query usage_volume table with the following criteria:
        //     - look for an entry for accountId with start date in the given range
        //     - look for an entry for accountId with end date in the given range
        //     - look for an entry for accountId with end date null (currently running vm or owned IP)
        //     - look for an entry for accountId with start date before given range *and* end date after given range
        List<UsageSecurityGroupVO> usageSGs = m_usageSecurityGroupDao.getUsageRecords(account.getId(), account.getDomainId(), startDate, endDate, false, 0);
        
        if(usageSGs.isEmpty()){
        	s_logger.debug("No SecurityGroup usage events for this period");
        	return true;
        }

        // This map has both the running time *and* the usage amount.
        Map<String, Pair<Long, Long>> usageMap = new HashMap<String, Pair<Long, Long>>();
        Map<String, SGInfo> sgMap = new HashMap<String, SGInfo>();

		// loop through all the security groups, create a usage record for each
        for (UsageSecurityGroupVO usageSG : usageSGs) {
            long vmId = usageSG.getVmInstanceId();
            long sgId = usageSG.getSecurityGroupId();
            String key = ""+vmId+"SG"+sgId;
            
            sgMap.put(key, new SGInfo(vmId, usageSG.getZoneId(), sgId));
            
            Date sgCreateDate = usageSG.getCreated();
            Date sgDeleteDate = usageSG.getDeleted();

            if ((sgDeleteDate == null) || sgDeleteDate.after(endDate)) {
                sgDeleteDate = endDate;
            }

            // clip the start date to the beginning of our aggregation range if the vm has been running for a while
            if (sgCreateDate.before(startDate)) {
                sgCreateDate = startDate;
            }

            long currentDuration = (sgDeleteDate.getTime() - sgCreateDate.getTime()) + 1; // make sure this is an inclusive check for milliseconds (i.e. use n - m + 1 to find total number of millis to charge)


            updateSGUsageData(usageMap, key, usageSG.getVmInstanceId(), currentDuration);
        }

        for (String sgIdKey : usageMap.keySet()) {
            Pair<Long, Long> sgtimeInfo = usageMap.get(sgIdKey);
            long useTime = sgtimeInfo.second().longValue();

            // Only create a usage record if we have a runningTime of bigger than zero.
            if (useTime > 0L) {
                SGInfo info = sgMap.get(sgIdKey);
                createUsageRecord(UsageTypes.SECURITY_GROUP, useTime, startDate, endDate, account, info.getVmId(), info.getSGId(), info.getZoneId());
            }
        }

        return true;
	}

	private static void updateSGUsageData(Map<String, Pair<Long, Long>> usageDataMap, String key, long vmId, long duration) {
        Pair<Long, Long> sgUsageInfo = usageDataMap.get(key);
        if (sgUsageInfo == null) {
            sgUsageInfo = new Pair<Long, Long>(new Long(vmId), new Long(duration));
        } else {
            Long runningTime = sgUsageInfo.second();
            runningTime = new Long(runningTime.longValue() + duration);
            sgUsageInfo = new Pair<Long, Long>(sgUsageInfo.first(), runningTime);
        }
        usageDataMap.put(key, sgUsageInfo);
	}

	private static void createUsageRecord(int type, long runningTime, Date startDate, Date endDate, AccountVO account, long vmId, long sgId, long zoneId) {
        // Our smallest increment is hourly for now
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Total running time " + runningTime + "ms");
        }

        float usage = runningTime / 1000f / 60f / 60f;

        DecimalFormat dFormat = new DecimalFormat("#.######");
        String usageDisplay = dFormat.format(usage);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating security group:" + sgId + " usage record for Vm : " + vmId + ", usage: " + usageDisplay + ", startDate: " + startDate + ", endDate: " + endDate + ", for account: " + account.getId());
        }

        // Create the usage record
        String usageDesc = "Security Group: " + sgId + " for Vm : " + vmId + " usage time";

        UsageVO usageRecord = new UsageVO(zoneId, account.getId(), account.getDomainId(), usageDesc, usageDisplay + " Hrs", type,
                new Double(usage), vmId, null, null, null, sgId, null, startDate, endDate);
        m_usageDao.persist(usageRecord);
    }
	
	private static class SGInfo {
	    private long vmId;
	    private long zoneId;
	    private long sgId;

	    public SGInfo(long vmId, long zoneId, long sgId) {
	        this.vmId = vmId;
	        this.zoneId = zoneId;
	        this.sgId = sgId;
	    }
	    public long getZoneId() {
	        return zoneId;
	    }
	    public long getVmId() {
	        return vmId;
	    }
	    public long getSGId() {
	        return sgId;
	    }
	}

}
