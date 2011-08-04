/**
 * *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
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

package com.cloud.usage.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.usage.UsageVMInstanceVO;
import com.cloud.utils.DateUtil;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.Transaction;

@Local(value={UsageVMInstanceDao.class})
public class UsageVMInstanceDaoImpl extends GenericDaoBase<UsageVMInstanceVO, Long> implements UsageVMInstanceDao {
	public static final Logger s_logger = Logger.getLogger(UsageVMInstanceDaoImpl.class.getName());
	
	protected static final String UPDATE_USAGE_INSTANCE_SQL =
		"UPDATE usage_vm_instance SET end_date = ? "
	+   "WHERE account_id = ? and vm_instance_id = ? and usage_type = ? and end_date IS NULL";
    protected static final String DELETE_USAGE_INSTANCE_SQL =
        "DELETE FROM usage_vm_instance WHERE account_id = ? and vm_instance_id = ? and usage_type = ?";
    protected static final String GET_USAGE_RECORDS_BY_ACCOUNT = "SELECT usage_type, zone_id, account_id, vm_instance_id, vm_name, service_offering_id, template_id, hypervisor_type, start_date, end_date " +
                                                                  "FROM usage_vm_instance " +
                                                                  "WHERE account_id = ? AND ((end_date IS NULL) OR (start_date BETWEEN ? AND ?) OR " +
                                                                  "      (end_date BETWEEN ? AND ?) OR ((start_date <= ?) AND (end_date >= ?)))";

	public UsageVMInstanceDaoImpl() {}

    public void update(UsageVMInstanceVO instance) {
        Transaction txn = Transaction.open(Transaction.USAGE_DB);
        PreparedStatement pstmt = null;
        try {
            txn.start();
            String sql = UPDATE_USAGE_INSTANCE_SQL;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setString(1, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), instance.getEndDate()));
            pstmt.setLong(2, instance.getAccountId());
            pstmt.setLong(3, instance.getVmInstanceId());
            pstmt.setInt(4, instance.getUsageType());
            pstmt.executeUpdate();
            txn.commit();
        } catch (Exception e) {
            s_logger.warn(e);
        } finally {
            txn.close();
        }
    }

    public void delete(UsageVMInstanceVO instance) {
        Transaction txn = Transaction.open(Transaction.USAGE_DB);
        PreparedStatement pstmt = null;
        try {
            txn.start();
            String sql = DELETE_USAGE_INSTANCE_SQL;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, instance.getAccountId());
            pstmt.setLong(2, instance.getVmInstanceId());
            pstmt.setInt(3, instance.getUsageType());
            pstmt.executeUpdate();
            txn.commit();
        } catch (Exception ex) {
        	txn.rollback();
            s_logger.error("error deleting usage vm instance with vmId: " + instance.getVmInstanceId() + ", for account with id: " + instance.getAccountId());
        } finally {
            txn.close();
        }
    }

    public List<UsageVMInstanceVO> getUsageRecords(long accountId, Date startDate, Date endDate) {
        Transaction txn = Transaction.open(Transaction.USAGE_DB);
        PreparedStatement pstmt = null;
        List<UsageVMInstanceVO> usageInstances = new ArrayList<UsageVMInstanceVO>();
        try {
            String sql = GET_USAGE_RECORDS_BY_ACCOUNT;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, accountId);
            pstmt.setString(2, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(3, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            pstmt.setString(4, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(5, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            pstmt.setString(6, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(7, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int r_usageType = rs.getInt(1);
                long r_zoneId = rs.getLong(2);
                long r_accountId = rs.getLong(3);
                long r_vmId = rs.getLong(4);
                String r_vmName = rs.getString(5);
                long r_soId = rs.getLong(6);
                long r_tId = rs.getLong(7);
                String hypervisorType = rs.getString(8);
                String r_startDate = rs.getString(9);
                String r_endDate = rs.getString(10);
                Date instanceStartDate = null;
                Date instanceEndDate = null;
                if (r_startDate != null) {
                    instanceStartDate = DateUtil.parseDateString(s_gmtTimeZone, r_startDate);
                }
                if (r_endDate != null) {
                    instanceEndDate = DateUtil.parseDateString(s_gmtTimeZone, r_endDate);
                }
                UsageVMInstanceVO usageInstance = new UsageVMInstanceVO(r_usageType, r_zoneId, r_accountId, r_vmId, r_vmName, r_soId, r_tId, hypervisorType, instanceStartDate, instanceEndDate);
                usageInstances.add(usageInstance);
            }
        } catch (Exception ex) {
            s_logger.error("error retrieving usage vm instances for account id: " + accountId, ex);
        } finally {
            txn.close();
        }
        return usageInstances;
    }
}
