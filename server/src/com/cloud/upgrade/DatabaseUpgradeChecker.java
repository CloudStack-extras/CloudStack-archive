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
package com.cloud.upgrade;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.cluster.ClusterManagerImpl;
import com.cloud.maint.Version;
import com.cloud.upgrade.dao.DbUpgrade;
import com.cloud.upgrade.dao.Upgrade217to218;
import com.cloud.upgrade.dao.Upgrade218to22;
import com.cloud.upgrade.dao.Upgrade218to224DomainVlans;
import com.cloud.upgrade.dao.Upgrade2210to2211;
import com.cloud.upgrade.dao.Upgrade2211to2212;
import com.cloud.upgrade.dao.Upgrade2212to2213;
import com.cloud.upgrade.dao.Upgrade221to222;
import com.cloud.upgrade.dao.Upgrade222to224;
import com.cloud.upgrade.dao.Upgrade224to225;
import com.cloud.upgrade.dao.Upgrade225to226;
import com.cloud.upgrade.dao.Upgrade227to228;
import com.cloud.upgrade.dao.Upgrade228to229;
import com.cloud.upgrade.dao.Upgrade229to2210;
import com.cloud.upgrade.dao.UpgradeSnapshot217to224;
import com.cloud.upgrade.dao.UpgradeSnapshot223to224;
import com.cloud.upgrade.dao.VersionDao;
import com.cloud.upgrade.dao.VersionDaoImpl;
import com.cloud.upgrade.dao.VersionVO;
import com.cloud.upgrade.dao.VersionVO.Step;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.SystemIntegrityChecker;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.ScriptRunner;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;

@Local(value = {SystemIntegrityChecker.class})
public class DatabaseUpgradeChecker implements SystemIntegrityChecker {
    private final Logger s_logger = Logger.getLogger(DatabaseUpgradeChecker.class);

    protected HashMap<String, DbUpgrade[]> _upgradeMap = new HashMap<String, DbUpgrade[]>();

    VersionDao _dao;

    public DatabaseUpgradeChecker() {
        _dao = ComponentLocator.inject(VersionDaoImpl.class);

        _upgradeMap.put("2.1.7", new DbUpgrade[] { new Upgrade217to218(), new Upgrade218to22(), new Upgrade221to222(), new UpgradeSnapshot217to224(), new Upgrade222to224(), new Upgrade224to225(), new Upgrade225to226(), new Upgrade227to228(), new Upgrade228to229(), new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212(), new Upgrade2212to2213()});
        _upgradeMap.put("2.1.8", new DbUpgrade[] { new Upgrade218to22(), new Upgrade221to222(), new UpgradeSnapshot217to224(), new Upgrade222to224(), new Upgrade218to224DomainVlans(), new Upgrade224to225(), new Upgrade225to226(), new Upgrade227to228(), new Upgrade228to229(), new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212(), new Upgrade2212to2213()});
        _upgradeMap.put("2.1.9", new DbUpgrade[] { new Upgrade218to22(), new Upgrade221to222(), new UpgradeSnapshot217to224(), new Upgrade222to224(), new Upgrade218to224DomainVlans(), new Upgrade224to225(), new Upgrade225to226(), new Upgrade227to228(), new Upgrade228to229(), new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212(), new Upgrade2212to2213()});
        _upgradeMap.put("2.2.1", new DbUpgrade[] { new Upgrade221to222(), new UpgradeSnapshot223to224(), new Upgrade222to224(), new Upgrade224to225(), new Upgrade225to226(), new Upgrade227to228(), new Upgrade228to229(), new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212(), new Upgrade2212to2213()});
        _upgradeMap.put("2.2.2", new DbUpgrade[] { new Upgrade222to224(), new UpgradeSnapshot223to224(), new Upgrade224to225(), new Upgrade225to226(), new Upgrade227to228(), new Upgrade228to229(), new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212(), new Upgrade2212to2213()});
        _upgradeMap.put("2.2.3", new DbUpgrade[] { new Upgrade222to224(), new UpgradeSnapshot223to224(), new Upgrade224to225(), new Upgrade225to226(), new Upgrade227to228(), new Upgrade228to229(), new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212(), new Upgrade2212to2213()});
        _upgradeMap.put("2.2.4", new DbUpgrade[] { new Upgrade224to225(), new Upgrade225to226(), new Upgrade227to228(), new Upgrade228to229(), new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212(), new Upgrade2212to2213()});
        _upgradeMap.put("2.2.5", new DbUpgrade[] { new Upgrade225to226(), new Upgrade227to228(),new Upgrade228to229(), new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212(), new Upgrade2212to2213() });
        _upgradeMap.put("2.2.6", new DbUpgrade[] { new Upgrade227to228(), new Upgrade228to229(), new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212(), new Upgrade2212to2213()});
        _upgradeMap.put("2.2.7", new DbUpgrade[] { new Upgrade227to228(), new Upgrade228to229(), new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212(), new Upgrade2212to2213()});
        _upgradeMap.put("2.2.8", new DbUpgrade[] { new Upgrade228to229(), new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212(), new Upgrade2212to2213()});
        _upgradeMap.put("2.2.9", new DbUpgrade[] { new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212(), new Upgrade2212to2213()});
        _upgradeMap.put("2.2.10", new DbUpgrade[] { new Upgrade2210to2211(), new Upgrade2211to2212(), new Upgrade2212to2213()});
        _upgradeMap.put("2.2.11", new DbUpgrade[] { new Upgrade2211to2212(), new Upgrade2212to2213()});
        _upgradeMap.put("2.2.12", new DbUpgrade[] { new Upgrade2212to2213()});
    }

    protected void runScript(Connection conn, File file) {
        try {
            FileReader reader = new FileReader(file);
            ScriptRunner runner = new ScriptRunner(conn, false, true);
            runner.runScript(reader);
        } catch (FileNotFoundException e) {
            s_logger.error("Unable to find upgrade script: " + file.getAbsolutePath(), e);
            throw new CloudRuntimeException("Unable to find upgrade script: " + file.getAbsolutePath(), e);
        } catch (IOException e) {
            s_logger.error("Unable to read upgrade script: " + file.getAbsolutePath(), e);
            throw new CloudRuntimeException("Unable to read upgrade script: " + file.getAbsolutePath(), e);
        } catch (SQLException e) {
            s_logger.error("Unable to execute upgrade script: " + file.getAbsolutePath(), e);
            throw new CloudRuntimeException("Unable to execute upgrade script: " + file.getAbsolutePath(), e);
        }
    }

    protected void upgrade(String dbVersion, String currentVersion) {
        s_logger.info("Database upgrade must be performed from " + dbVersion + " to " + currentVersion);

        String trimmedDbVersion = Version.trimToPatch(dbVersion);
        String trimmedCurrentVersion = Version.trimToPatch(currentVersion);

        DbUpgrade[] upgrades = _upgradeMap.get(trimmedDbVersion);
        if (upgrades == null) {
            s_logger.error("There is no upgrade path from " + dbVersion + " to " + currentVersion);
            throw new CloudRuntimeException("There is no upgrade path from " + dbVersion + " to " + currentVersion);
        }
        
        if (Version.compare(trimmedCurrentVersion, upgrades[upgrades.length - 1].getUpgradedVersion()) != 0) {
            s_logger.error("The end upgrade version is actually at " + upgrades[upgrades.length - 1].getUpgradedVersion() + " but our management server code version is at " + currentVersion);
            throw new CloudRuntimeException("The end upgrade version is actually at " + upgrades[upgrades.length - 1].getUpgradedVersion() + " but our management server code version is at "
                    + currentVersion);
        }
        
        

        boolean supportsRollingUpgrade = true;
        for (DbUpgrade upgrade : upgrades) {
            if (!upgrade.supportsRollingUpgrade()) {
                supportsRollingUpgrade = false;
                break;
            }
        }

        if (!supportsRollingUpgrade && ClusterManagerImpl.arePeersRunning(null)) {
            s_logger.error("Unable to run upgrade because the upgrade sequence does not support rolling update and there are other management server nodes running");
            throw new CloudRuntimeException("Unable to run upgrade because the upgrade sequence does not support rolling update and there are other management server nodes running");
        }

        for (DbUpgrade upgrade : upgrades) {
            s_logger.debug("Running upgrade " + upgrade.getClass().getSimpleName() + " to upgrade from " + upgrade.getUpgradableVersionRange()[0] + "-" + upgrade.getUpgradableVersionRange()[1]
                                                                                                                                                                                             + " to " + upgrade.getUpgradedVersion());
            Transaction txn = Transaction.open("Upgrade");
            txn.start();
            try {
                Connection conn;
                try {
                    conn = txn.getConnection();
                } catch (SQLException e) {
                    s_logger.error("Unable to upgrade the database", e);
                    throw new CloudRuntimeException("Unable to upgrade the database", e);
                }
                File[] scripts = upgrade.getPrepareScripts();
                if (scripts != null) {
                    for (File script : scripts) {
                        runScript(conn, script);
                    }
                }

                upgrade.performDataMigration(conn);
                boolean upgradeVersion = true;

                if (upgrade.getUpgradedVersion().equals("2.1.8")) {
                    // we don't have VersionDao in 2.1.x
                    upgradeVersion = false;
                } else if (upgrade.getUpgradedVersion().equals("2.2.4")) {
                    try {
                        // specifically for domain vlan update from 2.1.8 to 2.2.4
                        PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM version WHERE version='2.2.4'");
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            upgradeVersion = false;
                        }
                    } catch (SQLException e) {
                        throw new CloudRuntimeException("Unable to update the version table", e);
                    }
                }

                if (upgradeVersion) {
                    VersionVO version = new VersionVO(upgrade.getUpgradedVersion());
                    _dao.persist(version);
                }

                txn.commit();
            } finally {
                txn.close();
            }
        }

        if (!ClusterManagerImpl.arePeersRunning(trimmedCurrentVersion)) {
            s_logger.info("Cleaning upgrades because all management server are now at the same version");
            TreeMap<String, List<DbUpgrade>> upgradedVersions = new TreeMap<String, List<DbUpgrade>>();

            for (DbUpgrade upgrade : upgrades) {
                String upgradedVerson = upgrade.getUpgradedVersion();
                List<DbUpgrade> upgradeList = upgradedVersions.get(upgradedVerson);
                if (upgradeList == null) {
                    upgradeList = new ArrayList<DbUpgrade>();
                }
                upgradeList.add(upgrade);
                upgradedVersions.put(upgradedVerson, upgradeList);
            }

            for (String upgradedVersion : upgradedVersions.keySet()) {
                List<DbUpgrade> versionUpgrades = upgradedVersions.get(upgradedVersion);
                VersionVO version = _dao.findByVersion(upgradedVersion, Step.Upgrade);
                s_logger.debug("Upgrading to version " + upgradedVersion + "...");

                Transaction txn = Transaction.open("Cleanup");
                try {
                    if (version != null) {
                        for (DbUpgrade upgrade : versionUpgrades) {
                            s_logger.info("Cleanup upgrade " + upgrade.getClass().getSimpleName() + " to upgrade from " + upgrade.getUpgradableVersionRange()[0] + "-"
                                    + upgrade.getUpgradableVersionRange()[1] + " to " + upgrade.getUpgradedVersion());

                            txn.start();
                            
                            Connection conn;
                            try {
                                conn = txn.getConnection();
                            } catch (SQLException e) {
                                s_logger.error("Unable to cleanup the database", e);
                                throw new CloudRuntimeException("Unable to cleanup the database", e);
                            }

                            File[] scripts = upgrade.getCleanupScripts();
                            if (scripts != null) {
                                for (File script : scripts) {
                                    runScript(conn, script);
                                    s_logger.debug("Cleanup script " + script.getAbsolutePath() + " is executed successfully");
                                }
                            }
                            txn.commit();
                        }

                        txn.start();
                        version.setStep(Step.Complete);
                        s_logger.debug("Upgrade completed for version " + upgradedVersion);
                        version.setUpdated(new Date());
                        _dao.update(version.getId(), version);
                        txn.commit();
                    }
                } finally {
                    txn.close();
                }
            }
        }

    }

    @Override
    public void check() {
        GlobalLock lock = GlobalLock.getInternLock("DatabaseUpgrade");
        try {
            s_logger.info("Grabbing lock to check for database upgrade.");
            if (!lock.lock(20 * 60)) {
                throw new CloudRuntimeException("Unable to acquire lock to check for database integrity.");
            }

            try {
                String dbVersion = _dao.getCurrentVersion();
                String currentVersion = this.getClass().getPackage().getImplementationVersion();
                if (currentVersion == null) {
                    currentVersion = this.getClass().getSuperclass().getPackage().getImplementationVersion();
                }

                s_logger.info("DB version = " + dbVersion + " Code Version = " + currentVersion);

                if (Version.compare(Version.trimToPatch(dbVersion), Version.trimToPatch(currentVersion)) > 0) {
                    throw new CloudRuntimeException("Database version " + dbVersion + " is higher than management software version " + currentVersion);
                }

                if (Version.compare(Version.trimToPatch(dbVersion), Version.trimToPatch(currentVersion)) == 0) {
                    s_logger.info("DB version and code version matches so no upgrade needed.");
                    return;
                }

                upgrade(dbVersion, currentVersion);
            } finally {
                lock.unlock();
            }
        } finally {
            lock.releaseRef();
        }
    }
}
