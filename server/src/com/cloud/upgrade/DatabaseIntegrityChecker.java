package com.cloud.upgrade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.maint.Version;
import com.cloud.upgrade.dao.VersionDao;
import com.cloud.upgrade.dao.VersionDaoImpl;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.SystemIntegrityChecker;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;

@Local(value = {SystemIntegrityChecker.class})
public class DatabaseIntegrityChecker implements SystemIntegrityChecker {
	private final Logger s_logger = Logger.getLogger(DatabaseIntegrityChecker.class);
	
    VersionDao _dao;
    
    public DatabaseIntegrityChecker() {
        _dao = ComponentLocator.inject(VersionDaoImpl.class);
    }
	
	/*
	 * Check if there were multiple hosts connect to the same local storage. This is from a 2.1.x bug,
	 * we didn't prevent adding host with the same IP.
	 */
	private String formatDuplicateHostToReadText(Long poolId, ResultSet rs) throws SQLException {
		boolean has = false;
		StringBuffer buf = new StringBuffer();
		String fmt = "|%1$-8s|%2$-16s|%3$-16s|%4$-24s|%5$-8s|\n";
		String head = String.format(fmt, "id", "status", "removed", "private_ip_address", "pool_id");
		buf.append(head);
		while (rs.next()) {
			String h = String.format(fmt, rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), poolId);
			buf.append(h);
			has = true;
		}
		
		if (!has) {
			throw new CloudRuntimeException("Local storage with Id " + poolId + " shows there are multiple hosts connect to it, but 'select id, status, removed, private_ip_address from host where id in (select host_id from storage_pool_host_ref where pool_id=?)' returns nothing");
		} else {
			return buf.toString();
		}
	}
	
	private Boolean checkDuplicateHostWithTheSameLocalStorage() {
		Transaction txn = Transaction.open("Integrity");
		txn.start();
		try {
			Connection conn;
			try {
				conn = txn.getConnection();
				PreparedStatement pstmt = conn.prepareStatement("SELECT pool_id FROM host INNER JOIN storage_pool_host_ref INNER JOIN storage_pool WHERE storage_pool.id = storage_pool_host_ref.pool_id and storage_pool.pool_type='LVM' AND host.id=storage_pool_host_ref.host_id AND host.removed IS NULL group by pool_id having count(*) > 1");
				ResultSet rs = pstmt.executeQuery();
				
				boolean noDuplicate = true;
				StringBuffer helpInfo = new StringBuffer();
				String note = "DATABASE INTEGRITY ERROR\nManagement server detected there are some hosts connect to the same loacal storage, please contact Cloud.com support team for solution. Below are detialed info, please attach all of them to Cloud.com support. Thank you\n";
				helpInfo.append(note);
				while (rs.next()) {
					long poolId = rs.getLong(1);
					pstmt = conn.prepareStatement("select id, status, removed, private_ip_address from host where id in (select host_id from storage_pool_host_ref where pool_id=?)");
					pstmt.setLong(1, poolId);
					ResultSet dhrs = pstmt.executeQuery();
					String help = formatDuplicateHostToReadText(poolId, dhrs);
					helpInfo.append(help);
					helpInfo.append("\n");
					noDuplicate = false;
				}
				
				if (noDuplicate) {
					s_logger.debug("No duplicate hosts with the same local storage found in database");
				} else {
					s_logger.error(helpInfo.toString());
				}
				
				return noDuplicate;
			} catch (SQLException e) {
				s_logger.error("Unable to check duplicate hosts with the same local storage in database", e);
				throw new CloudRuntimeException("Unable to check duplicate hosts with the same local storage in database", e);
			}
		} finally {
			txn.commit();
			txn.close();
		}
	}
	
	private boolean check21to22PremiumUprage(Connection conn) throws SQLException {
	    PreparedStatement pstmt = conn.prepareStatement("show tables in cloud_usage");
	    ResultSet rs = pstmt.executeQuery();
	    int num = 0;
	    
	    while (rs.next()) {
	        String tableName = rs.getString(1);
	        if (tableName.equalsIgnoreCase("usage_event") || tableName.equalsIgnoreCase("usage_port_forwarding") || tableName.equalsIgnoreCase("usage_network_offering")) {
	            num ++;
	            s_logger.debug("Checking 21to22PremiumUprage table " + tableName + " found");
	        }
	        if (num == 3) {
	            return true;
	        }
	    }
	    
	    return false;
	}
	
	private boolean isColumnExisted(Connection conn, String dbName, String tableName, String column) throws SQLException {
	    PreparedStatement pstmt = conn.prepareStatement(String.format("describe %1$s.%2$s", dbName, tableName));
        ResultSet rs = pstmt.executeQuery();
        boolean found = false;
        while (rs.next()) {
            if (column.equalsIgnoreCase(rs.getString(1))) {
                s_logger.debug(String.format("Column %1$s.%2$s.%3$s found", dbName, tableName, column));
                found = true;
                break;
            }
        }
        return found;
	}
	
    private boolean check221to222PremiumUprage(Connection conn) throws SQLException {
        if (!isColumnExisted(conn, "cloud_usage", "cloud_usage", "network_id")) {
            return false;
        }

        if (!isColumnExisted(conn, "cloud_usage", "usage_network", "network_id")) {
            return false;
        }

        return isColumnExisted(conn, "cloud_usage", "user_statistics", "network_id");
    }
	
	private boolean check222to224PremiumUpgrade(Connection conn) throws SQLException {
	    if (!isColumnExisted(conn, "cloud_usage", "usage_vm_instance", "hypervisor_type")) {
            return false;
        }
	    
	    return isColumnExisted(conn, "cloud_usage", "usage_event", "resource_type");
	}
	
	private boolean checkMissedPremiumUpgradeFor228() {
		Transaction txn = Transaction.open("Integrity");
		txn.start();
		try {
		    String dbVersion = _dao.getCurrentVersion();
		    if (Version.compare(Version.trimToPatch(dbVersion), Version.trimToPatch("2.2.8")) != 0) {
		        return true;
		    }
		    
			Connection conn;
			try {
				conn = txn.getConnection();
				PreparedStatement pstmt = conn.prepareStatement("show databases");
				ResultSet rs = pstmt.executeQuery();
				boolean hasUsage = false;
				while (rs.next()) {
				    String dbName = rs.getString(1);
				    if (dbName.equalsIgnoreCase("cloud_usage")) {
				        hasUsage = true;
				        break;
				    }
				}
				
				if (!hasUsage) {
				    s_logger.debug("No cloud_usage found in database, no need to check missed premium upgrade");
				    return true;
				}
				
				if (!check21to22PremiumUprage(conn)) {
				    s_logger.error("21to22 premium upgrade missed");
				    return false;
				}
				
				if (!check221to222PremiumUprage(conn)) {
				    s_logger.error("221to222 premium upgrade missed");
				    return false;
				}
				
				if (!check222to224PremiumUpgrade(conn)) {
                    s_logger.error("222to224 premium upgrade missed");
                    return false;
				}
				
				return true;
			} catch (SQLException e) {
				s_logger.error("Unable to check missed premiumg upgrade");
				throw new CloudRuntimeException("Unable to check missed premiumg upgrade");
			}
		} finally {
			txn.commit();
			txn.close();
		}
	}
	
	@Override
	public void check() {
		GlobalLock lock = GlobalLock.getInternLock("DatabaseIntegrity");
        try {
            s_logger.info("Grabbing lock to check for database integrity.");
            if (!lock.lock(20 * 60)) {
                throw new CloudRuntimeException("Unable to acquire lock to check for database integrity.");
            }

            try {
                s_logger.info("Performing database integrity check");
                if (!checkDuplicateHostWithTheSameLocalStorage()) {
                    throw new CloudRuntimeException("checkDuplicateHostWithTheSameLocalStorage detected error");
                }
                
                if (!checkMissedPremiumUpgradeFor228()) {
                    s_logger.error("Your current database version is 2.2.8, management server detected some missed premium upgrade, please contact Cloud.com support and attach log file. Thank you!");
                    throw new CloudRuntimeException("Detected missed premium upgrade");
                }
            } finally {
                lock.unlock();
            }
        } finally {
            lock.releaseRef();
        }
	}
}
