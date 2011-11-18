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
package com.cloud.upgrade.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

public class Upgrade2212to2213 implements DbUpgrade {
    final static Logger s_logger = Logger.getLogger(Upgrade2212to2213.class);

    @Override
    public String[] getUpgradableVersionRange() {
        return new String[] { "2.2.12", "2.2.13"};
    }

    @Override
    public String getUpgradedVersion() {
        return "2.2.13";
    }

    @Override
    public boolean supportsRollingUpgrade() {
        return true;
    }

    @Override
    public File[] getPrepareScripts() {
        String script = Script.findScript("", "db/schema-2212to2213.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-2212to2213.sql");
        }
        
        return new File[] { new File(script) };
    }

    @Override
    public void performDataMigration(Connection conn) {
        fixForeignKeys(conn);
    }

    @Override
    public File[] getCleanupScripts() {
        return null;
    }
 

    private void fixForeignKeys(Connection conn) {
        HashMap<String, List<String>> foreignKeys = new HashMap<String, List<String>>();
        List<String> keys = new ArrayList<String>();
        keys.add("fk_networks__data_center_id");
        foreignKeys.put("networks", keys);

        // drop all foreign keys
        s_logger.debug("Dropping old key fk_networks__data_center_id...");
        for (String tableName : foreignKeys.keySet()) {
            DbUpgradeUtils.dropKeysIfExist(conn, tableName, foreignKeys.get(tableName), true);
        }

        try {
            PreparedStatement pstmt = conn.prepareStatement("ALTER TABLE `cloud`.`networks` ADD CONSTRAINT `fk_networks__data_center_id` FOREIGN KEY (`data_center_id`) REFERENCES `data_center`(`id`) ON DELETE CASCADE");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable to reinsert data center key for the network", e);
        }
        
        // drop primary keys
        DbUpgradeUtils.dropPrimaryKeyIfExists(conn, "cloud_usage.usage_load_balancer_policy");
        DbUpgradeUtils.dropPrimaryKeyIfExists(conn, "cloud_usage.usage_port_forwarding");
        
        //Drop usage_network_offering unique key
        try {
            PreparedStatement pstmt = conn.prepareStatement("drop index network_offering_id on cloud_usage.usage_network_offering");
            pstmt.executeUpdate();
            s_logger.debug("Dropped usage_network_offering unique key");
        } catch (Exception e) {
            // Ignore error if the usage_network_offering table or the unique key doesn't exist
        }
        
        //Drop i_usage_event__created key (if exists) and re-add it again
        keys = new ArrayList<String>();
        keys.add("i_usage_event__created");
        DbUpgradeUtils.dropKeysIfExist(conn, "usage_event", keys, false);
        
        try {
            PreparedStatement pstmt = conn.prepareStatement("ALTER TABLE `cloud`.`usage_event` ADD INDEX `i_usage_event__created`(`created`)");
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable to execute usage_event table update", e);
        }
    }
}
