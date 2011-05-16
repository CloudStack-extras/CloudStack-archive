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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import com.cloud.upgrade.dao.VersionDaoImpl;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.db.DbTestUtils;

public class AdvanceZone223To224UpgradeTest extends TestCase {
    private static final Logger s_logger = Logger.getLogger(AdvanceZone217To224UpgradeTest.class);

    @Override
    @Before
    public void setUp() throws Exception {
        DbTestUtils.executeScript("PreviousDatabaseSchema/clean-db.sql", false, true);
    }

    @Override
    @After
    public void tearDown() throws Exception {
    }

    public void test217to22Upgrade() throws SQLException {
        s_logger.debug("Finding sample data from 2.2.3");
        DbTestUtils.executeScript("PreviousDatabaseSchema/2.2.3/dave-sample.sql", false, true);

        Connection conn;
        PreparedStatement pstmt;

        VersionDaoImpl dao = ComponentLocator.inject(VersionDaoImpl.class);
        DatabaseUpgradeChecker checker = ComponentLocator.inject(DatabaseUpgradeChecker.class);

        String version = dao.getCurrentVersion();
        assert version.equals("2.2.2") : "Version returned is not 2.2.2 but " + version;

        checker.upgrade("2.2.2", "2.2.4");
    }

}
