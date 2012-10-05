/*Copyright 2012 Citrix Systems, Inc. Licensed under the
Apache License, Version 2.0 (the "License"); you may not use this
file except in compliance with the License.  Citrix Systems, Inc.
reserves all rights not expressly granted by the License.
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/


package com.cloud.upgrade.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

public class Upgrade305to306 extends Upgrade30xBase implements DbUpgrade {
    final static Logger s_logger = Logger.getLogger(Upgrade305to306.class);

    @Override
    public String[] getUpgradableVersionRange() {
        return new String[] { "3.0.5", "3.0.6" };
    }

    @Override
    public String getUpgradedVersion() {
        return "3.0.6";
    }

    @Override
    public boolean supportsRollingUpgrade() {
        return true;
    }

    @Override
    public File[] getPrepareScripts() {
        String script = Script.findScript("", "db/schema-305to306.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-305to306.sql");
        }

        return new File[] { new File(script) };
    }

    @Override
    public void performDataMigration(Connection conn) {
        upgradeEIPNetworkOfferings(conn);
    }

    private void upgradeEIPNetworkOfferings(Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
   
        try {
            pstmt = conn.prepareStatement("select id, elastic_ip_service from `cloud`.`network_offerings` where traffic_type='Guest'");
            rs = pstmt.executeQuery();
            while (rs.next()) {
            	long id = rs.getLong(1);
            	// check if elastic IP service is enabled for network offering
            	if (rs.getLong(2) != 0) {
                    //update network offering with eip_associate_public_ip set to true
                    pstmt = conn.prepareStatement("UPDATE `cloud`.`network_offerings` set eip_associate_public_ip=? where id=?");
                    pstmt.setBoolean(1, true);
                    pstmt.setLong(2, id);
                    pstmt.executeUpdate();
            	}
            }
        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable to set elastic_ip_service for network offerings with EIP service enabled.", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }

                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    @Override
    public File[] getCleanupScripts() {
        String script = Script.findScript("", "db/schema-305to306-cleanup.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-305to306-cleanup.sql");
        }

        return new File[] { new File(script) };
    }

}
