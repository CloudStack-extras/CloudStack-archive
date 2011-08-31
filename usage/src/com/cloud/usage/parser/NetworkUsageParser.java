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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cloud.usage.UsageNetworkVO;
import com.cloud.usage.UsageServer;
import com.cloud.usage.UsageTypes;
import com.cloud.usage.UsageVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageNetworkDao;
import com.cloud.user.AccountVO;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.db.SearchCriteria;

public class NetworkUsageParser {
public static final Logger s_logger = Logger.getLogger(NetworkUsageParser.class.getName());

	private static ComponentLocator _locator = ComponentLocator.getLocator(UsageServer.Name, "usage-components.xml", "log4j-cloud_usage");
	private static UsageDao m_usageDao = _locator.getDao(UsageDao.class);
	private static UsageNetworkDao m_usageNetworkDao = _locator.getDao(UsageNetworkDao.class);

	public static boolean parse(AccountVO account, Date startDate, Date endDate) {
	    if (s_logger.isDebugEnabled()) {
	        s_logger.debug("Parsing all Network usage events for account: " + account.getId());
	    }

		if ((endDate == null) || endDate.after(new Date())) {
		    endDate = new Date();
		}

        // - query usage_network table for all entries for userId with
        // event_date in the given range
        SearchCriteria<UsageNetworkVO> sc = m_usageNetworkDao.createSearchCriteria();
        sc.addAnd("accountId", SearchCriteria.Op.EQ, account.getId());
        sc.addAnd("eventTimeMillis", SearchCriteria.Op.BETWEEN, startDate.getTime(), endDate.getTime());
        List<UsageNetworkVO> usageNetworkVOs = m_usageNetworkDao.search(sc, null);

        Map<String, NetworkInfo> networkUsageByZone = new HashMap<String, NetworkInfo>();

        // Calculate the total bytes since last parsing
        for (UsageNetworkVO usageNetwork : usageNetworkVOs) {
            long zoneId = usageNetwork.getZoneId();
            String key = ""+zoneId;
            if(usageNetwork.getHostId() != 0){
                key += "-Host"+usageNetwork.getHostId();
            }
            NetworkInfo networkInfo = networkUsageByZone.get(key);

            long bytesSent = usageNetwork.getBytesSent();
            long bytesReceived = usageNetwork.getBytesReceived();
            if (networkInfo != null) {
                bytesSent += networkInfo.getBytesSent();
                bytesReceived += networkInfo.getBytesRcvd();
            }

            networkUsageByZone.put(key, new NetworkInfo(zoneId, usageNetwork.getHostId(), usageNetwork.getHostType(), usageNetwork.getNetworkId(), bytesSent, bytesReceived));
        }

        for (String key : networkUsageByZone.keySet()) {
            NetworkInfo networkInfo = networkUsageByZone.get(key);
            long totalBytesSent = networkInfo.getBytesSent();
            long totalBytesReceived = networkInfo.getBytesRcvd();

            if ((totalBytesSent > 0L) || (totalBytesReceived > 0L)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Creating usage record, total bytes sent:" + totalBytesSent + ", total bytes received: " + totalBytesReceived + " for account: "
                            + account.getId() + " in availability zone " + networkInfo.getZoneId() + ", start: " + startDate + ", end: " + endDate);
                }

                Long hostId = null;
                
                // Create the usage record for bytes sent
                String usageDesc = "network bytes sent";
                if(networkInfo.getHostId() != 0){
                    hostId = networkInfo.getHostId();
                    usageDesc += " for Host: "+networkInfo.getHostId(); 
                }
                UsageVO usageRecord = new UsageVO(networkInfo.getZoneId(), account.getId(), account.getDomainId(), usageDesc, totalBytesSent + " bytes sent",
                        UsageTypes.NETWORK_BYTES_SENT, new Double(totalBytesSent), hostId, networkInfo.getHostType(), networkInfo.getNetworkId(), startDate, endDate);
                m_usageDao.persist(usageRecord);

                // Create the usage record for bytes received
                usageDesc = "network bytes received";
                if(networkInfo.getHostId() != 0){
                    usageDesc += " for Host: "+networkInfo.getHostId(); 
                }
                usageRecord = new UsageVO(networkInfo.getZoneId(), account.getId(), account.getDomainId(), usageDesc, totalBytesReceived + " bytes received",
                        UsageTypes.NETWORK_BYTES_RECEIVED, new Double(totalBytesReceived), hostId, networkInfo.getHostType(), networkInfo.getNetworkId(), startDate, endDate);
                m_usageDao.persist(usageRecord);
            } else {
                // Don't charge anything if there were zero bytes processed
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("No usage record (0 bytes used) generated for account: " + account.getId());
                }
            }
        }

		return true;
	}
	
	private static class NetworkInfo {
        private long zoneId;
        private long hostId;
        private String hostType;
        private Long networkId;
        private long bytesSent;
        private long bytesRcvd;

        public NetworkInfo(long zoneId, long hostId, String hostType, Long networkId, long bytesSent, long bytesRcvd) {
            this.zoneId = zoneId;
            this.hostId = hostId;
            this.hostType = hostType;
            this.networkId = networkId;
            this.bytesSent = bytesSent;
            this.bytesRcvd = bytesRcvd;
        }

        public long getZoneId() {
            return zoneId;
        }

        public long getHostId() {
            return hostId;
        }
        
        public Long getNetworkId() {
            return networkId;
        }

        public long getBytesSent() {
            return bytesSent;
        }

        public long getBytesRcvd() {
            return bytesRcvd;
        }
        
        public String getHostType(){
            return hostType;
        }
    
    }
}
