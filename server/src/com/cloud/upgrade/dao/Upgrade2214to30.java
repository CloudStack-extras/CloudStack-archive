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
package com.cloud.upgrade.dao;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.cloud.offering.NetworkOffering;
import com.cloud.utils.crypt.DBEncryptionUtil;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

public class Upgrade2214to30 implements DbUpgrade {
    final static Logger s_logger = Logger.getLogger(Upgrade2214to30.class);

    @Override
    public String[] getUpgradableVersionRange() {
        return new String[] { "2.2.14", "3.0.0" };
    }

    @Override
    public String getUpgradedVersion() {
        return "3.0.0";
    }

    @Override
    public boolean supportsRollingUpgrade() {
        return true;
    }

    @Override
    public File[] getPrepareScripts() {
        String script = Script.findScript("", "db/schema-2214to30.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-2214to30.sql");
        }

        return new File[] { new File(script) };
    }

    @Override
    public void performDataMigration(Connection conn) {
        // encrypt data
        encryptData(conn);
        // drop keys
        dropKeysIfExist(conn);
        // physical network setup
        setupPhysicalNetworks(conn);
        // update domain network ref
        updateDomainNetworkRef(conn);
        // update networks that use redundant routers to the new network offering
        updateReduntantRouters(conn);
        // update networks that have to switch from Shared to Isolated network offerings
        switchAccountSpecificNetworksToIsolated(conn);
        // create service/provider map for network offerings
        createNetworkOfferingServices(conn);
        // create service/provider map for networks
        createNetworkServices(conn);
        //migrate user concentrated deployment planner choice to new global setting
        migrateUserConcentratedPlannerChoice(conn);
    }

    @Override
    public File[] getCleanupScripts() {
        String script = Script.findScript("", "db/schema-2214to30-cleanup.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-2214to30-cleanup.sql");
        }

        return new File[] { new File(script) };
    }

    private void setupPhysicalNetworks(Connection conn) {
        /**
         * for each zone:
         * add a p.network, use zone.vnet and zone.type
         * add default traffic types, pnsp and virtual router element in enabled state
         * set p.network.id in op_dc_vnet and vlan and user_ip_address
         * list guest networks for the zone, set p.network.id
         */
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        PreparedStatement pstmtUpdate = null;
        try {
            // Load all DataCenters
            String getNextNetworkSequenceSql = "SELECT value from sequence where name='physical_networks_seq'";
            String advanceNetworkSequenceSql = "UPDATE sequence set value=value+1 where name='physical_networks_seq'";

            String xenPublicLabel = getNetworkLabelFromConfig(conn, "xen.public.network.device");
            String xenPrivateLabel = getNetworkLabelFromConfig(conn, "xen.private.network.device");
            String xenStorageLabel = getNetworkLabelFromConfig(conn, "xen.storage.network.device1");
            String xenGuestLabel = getNetworkLabelFromConfig(conn, "xen.guest.network.device");

            String kvmPublicLabel = getNetworkLabelFromConfig(conn, "kvm.public.network.device");
            String kvmPrivateLabel = getNetworkLabelFromConfig(conn, "kvm.private.network.device");
            String kvmGuestLabel = getNetworkLabelFromConfig(conn, "kvm.guest.network.device");

            String vmwarePublicLabel = getNetworkLabelFromConfig(conn, "vmware.public.vswitch");
            String vmwarePrivateLabel = getNetworkLabelFromConfig(conn, "vmware.private.vswitch");
            String vmwareGuestLabel = getNetworkLabelFromConfig(conn, "vmware.guest.vswitch");

            pstmt = conn.prepareStatement("SELECT id, domain_id, networktype, vnet, name FROM data_center");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                long zoneId = rs.getLong(1);
                long domainId = rs.getLong(2);
                String networkType = rs.getString(3);
                String vnet = rs.getString(4);
                String zoneName = rs.getString(5);

                // add p.network
                PreparedStatement pstmt2 = conn.prepareStatement(getNextNetworkSequenceSql);
                ResultSet rsSeq = pstmt2.executeQuery();
                rsSeq.next();

                long physicalNetworkId = rsSeq.getLong(1);
                rsSeq.close();
                pstmt2.close();
                pstmt2 = conn.prepareStatement(advanceNetworkSequenceSql);
                pstmt2.executeUpdate();
                pstmt2.close();

                String uuid = UUID.randomUUID().toString();
                String broadcastDomainRange = "POD";
                if ("Advanced".equals(networkType)) {
                    broadcastDomainRange = "ZONE";
                }

                String values = null;
                values = "('" + physicalNetworkId + "'";
                values += ",'" + uuid + "'";
                values += ",'" + zoneId + "'";
                values += ",'" + vnet + "'";
                values += ",'" + domainId + "'";
                values += ",'" + broadcastDomainRange + "'";
                values += ",'Enabled'";
                values += ",'" + zoneName + "-pNtwk'";
                values += ")";

                s_logger.debug("Adding PhysicalNetwork " + physicalNetworkId + " for Zone id " + zoneId);

                String sql = "INSERT INTO `cloud`.`physical_network` (id, uuid, data_center_id, vnet, domain_id, broadcast_domain_range, state, name) VALUES " + values;
                pstmtUpdate = conn.prepareStatement(sql);
                pstmtUpdate.executeUpdate();
                pstmtUpdate.close();

                // add traffic types
                s_logger.debug("Adding PhysicalNetwork traffic types");
                String insertTraficType = "INSERT INTO `cloud`.`physical_network_traffic_types` (physical_network_id, traffic_type, xen_network_label, kvm_network_label, vmware_network_label, uuid) VALUES ( ?, ?, ?, ?, ?, ?)";
                pstmtUpdate = conn.prepareStatement(insertTraficType);
                pstmtUpdate.setLong(1, physicalNetworkId);
                pstmtUpdate.setString(2, "Public");
                pstmtUpdate.setString(3, xenPublicLabel);
                pstmtUpdate.setString(4, kvmPublicLabel);
                pstmtUpdate.setString(5, vmwarePublicLabel);
                pstmtUpdate.setString(6, UUID.randomUUID().toString());
                pstmtUpdate.executeUpdate();
                pstmtUpdate.close();

                pstmtUpdate = conn.prepareStatement(insertTraficType);
                pstmtUpdate.setLong(1, physicalNetworkId);
                pstmtUpdate.setString(2, "Management");
                pstmtUpdate.setString(3, xenPrivateLabel);
                pstmtUpdate.setString(4, kvmPrivateLabel);
                pstmtUpdate.setString(5, vmwarePrivateLabel);
                pstmtUpdate.setString(6, UUID.randomUUID().toString());
                pstmtUpdate.executeUpdate();
                pstmtUpdate.close();

                pstmtUpdate = conn.prepareStatement(insertTraficType);
                pstmtUpdate.setLong(1, physicalNetworkId);
                pstmtUpdate.setString(2, "Storage");
                pstmtUpdate.setString(3, xenStorageLabel);
                pstmtUpdate.setString(4, null);
                pstmtUpdate.setString(5, null);
                pstmtUpdate.setString(6, UUID.randomUUID().toString());
                pstmtUpdate.executeUpdate();
                pstmtUpdate.close();

                pstmtUpdate = conn.prepareStatement(insertTraficType);
                pstmtUpdate.setLong(1, physicalNetworkId);
                pstmtUpdate.setString(2, "Guest");
                pstmtUpdate.setString(3, xenGuestLabel);
                pstmtUpdate.setString(4, kvmGuestLabel);
                pstmtUpdate.setString(5, vmwareGuestLabel);
                pstmtUpdate.setString(6, UUID.randomUUID().toString());
                pstmtUpdate.executeUpdate();
                pstmtUpdate.close();

                // add physical network service provider - VirtualRouter
                s_logger.debug("Adding PhysicalNetworkServiceProvider VirtualRouter");
                String insertPNSP = "INSERT INTO `physical_network_service_providers` (`uuid`, `physical_network_id` , `provider_name`, `state` ," +
                        "`destination_physical_network_id`, `vpn_service_provided`, `dhcp_service_provided`, `dns_service_provided`, `gateway_service_provided`," +
                        "`firewall_service_provided`, `source_nat_service_provided`, `load_balance_service_provided`, `static_nat_service_provided`," +
                        "`port_forwarding_service_provided`, `user_data_service_provided`, `security_group_service_provided`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                pstmtUpdate = conn.prepareStatement(insertPNSP);
                pstmtUpdate.setString(1, UUID.randomUUID().toString());
                pstmtUpdate.setLong(2, physicalNetworkId);
                pstmtUpdate.setString(3, "VirtualRouter");
                pstmtUpdate.setString(4, "Enabled");
                pstmtUpdate.setLong(5, 0);
                pstmtUpdate.setInt(6, 1);
                pstmtUpdate.setInt(7, 1);
                pstmtUpdate.setInt(8, 1);
                pstmtUpdate.setInt(9, 1);
                pstmtUpdate.setInt(10, 1);
                pstmtUpdate.setInt(11, 1);
                pstmtUpdate.setInt(12, 1);
                pstmtUpdate.setInt(13, 1);
                pstmtUpdate.setInt(14, 1);
                pstmtUpdate.setInt(15, 1);
                pstmtUpdate.setInt(16, 0);
                pstmtUpdate.executeUpdate();
                pstmtUpdate.close();

                // add virtual_router_element
                String fetchNSPid = "SELECT id from physical_network_service_providers where physical_network_id=" + physicalNetworkId;
                pstmt2 = conn.prepareStatement(fetchNSPid);
                ResultSet rsNSPid = pstmt2.executeQuery();
                rsNSPid.next();
                long nspId = rsNSPid.getLong(1);
                rsSeq.close();
                pstmt2.close();

                String insertRouter = "INSERT INTO `virtual_router_providers` (`nsp_id`, `uuid` , `type` , `enabled`) " +
                        "VALUES (?,?,?,?)";
                pstmtUpdate = conn.prepareStatement(insertRouter);
                pstmtUpdate.setLong(1, nspId);
                pstmtUpdate.setString(2, UUID.randomUUID().toString());
                pstmtUpdate.setString(3, "VirtualRouter");
                pstmtUpdate.setInt(4, 1);
                pstmtUpdate.executeUpdate();
                pstmtUpdate.close();

                // add physicalNetworkId to op_dc_vnet_alloc for this zone
                s_logger.debug("Adding PhysicalNetwork to op_dc_vnet_alloc");
                String updateVnet = "UPDATE op_dc_vnet_alloc SET physical_network_id = " + physicalNetworkId + " WHERE data_center_id = " + zoneId;
                pstmtUpdate = conn.prepareStatement(updateVnet);
                pstmtUpdate.executeUpdate();
                pstmtUpdate.close();

                // add physicalNetworkId to vlan for this zone
                s_logger.debug("Adding PhysicalNetwork to VLAN");
                String updateVLAN = "UPDATE vlan SET physical_network_id = " + physicalNetworkId + " WHERE data_center_id = " + zoneId;
                pstmtUpdate = conn.prepareStatement(updateVLAN);
                pstmtUpdate.executeUpdate();
                pstmtUpdate.close();

                // add physicalNetworkId to user_ip_address for this zone
                s_logger.debug("Adding PhysicalNetwork to user_ip_address");
                String updateUsrIp = "UPDATE user_ip_address SET physical_network_id = " + physicalNetworkId + " WHERE data_center_id = " + zoneId;
                pstmtUpdate = conn.prepareStatement(updateUsrIp);
                pstmtUpdate.executeUpdate();
                pstmtUpdate.close();

                // add physicalNetworkId to guest networks for this zone
                s_logger.debug("Adding PhysicalNetwork to networks");
                String updateNet = "UPDATE networks SET physical_network_id = " + physicalNetworkId + " WHERE data_center_id = " + zoneId + " AND traffic_type = 'Guest'";
                pstmtUpdate = conn.prepareStatement(updateNet);
                pstmtUpdate.executeUpdate();
                pstmtUpdate.close();
            }
        } catch (SQLException e) {
            throw new CloudRuntimeException("Exception while adding PhysicalNetworks", e);
        } finally {
            if (pstmtUpdate != null) {
                try {
                    pstmtUpdate.close();
                } catch (SQLException e) {
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }

        }

    }
    
    private String getNetworkLabelFromConfig(Connection conn, String name){
        String sql = "SELECT value FROM configuration where name = '"+name+"'";
        String networkLabel = null;
        PreparedStatement pstmt = null; 
        ResultSet rs = null;
        try{
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                networkLabel = rs.getString(1);
            }
        }catch (SQLException e) {
            throw new CloudRuntimeException("Unable to fetch network label from configuration", e);
        }finally{
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
        }
        return networkLabel;
    }

    private void encryptData(Connection conn) {
        encryptConfigValues(conn);
        encryptHostDetails(conn);
        encryptVNCPassword(conn);
        encryptUserCredentials(conn);
    }

    private void encryptConfigValues(Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("select name, value from configuration where category in ('Hidden', 'Secure')");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString(1);
                String value = rs.getString(2);
                if (value == null) {
                    continue;
                }
                String encryptedValue = DBEncryptionUtil.encrypt(value);
                pstmt = conn.prepareStatement("update configuration set value=? where name=?");
                pstmt.setBytes(1, encryptedValue.getBytes("UTF-8"));
                pstmt.setString(2, name);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable encrypt configuration values ", e);
        } catch (UnsupportedEncodingException e) {
            throw new CloudRuntimeException("Unable encrypt configuration values ", e);
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

    private void encryptHostDetails(Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("select id, value from host_details where name = 'password'");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                long id = rs.getLong(1);
                String value = rs.getString(2);
                if (value == null) {
                    continue;
                }
                String encryptedValue = DBEncryptionUtil.encrypt(value);
                pstmt = conn.prepareStatement("update host_details set value=? where id=?");
                pstmt.setBytes(1, encryptedValue.getBytes("UTF-8"));
                pstmt.setLong(2, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable encrypt host_details values ", e);
        } catch (UnsupportedEncodingException e) {
            throw new CloudRuntimeException("Unable encrypt host_details values ", e);
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

    private void encryptVNCPassword(Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("select id, vnc_password from vm_instance");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                long id = rs.getLong(1);
                String value = rs.getString(2);
                if (value == null) {
                    continue;
                }
                String encryptedValue = DBEncryptionUtil.encrypt(value);
                pstmt = conn.prepareStatement("update vm_instance set vnc_password=? where id=?");
                pstmt.setBytes(1, encryptedValue.getBytes("UTF-8"));
                pstmt.setLong(2, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable encrypt vm_instance vnc_password ", e);
        } catch (UnsupportedEncodingException e) {
            throw new CloudRuntimeException("Unable encrypt vm_instance vnc_password ", e);
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

    private void encryptUserCredentials(Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("select id, secret_key from user");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                long id = rs.getLong(1);
                String secretKey = rs.getString(2);
                String encryptedSecretKey = DBEncryptionUtil.encrypt(secretKey);
                pstmt = conn.prepareStatement("update user set secret_key=? where id=?");
                if (encryptedSecretKey == null) {
                    pstmt.setNull(1, Types.VARCHAR);
                } else {
                    pstmt.setBytes(1, encryptedSecretKey.getBytes("UTF-8"));
                }
                pstmt.setLong(2, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable encrypt user secret key ", e);
        } catch (UnsupportedEncodingException e) {
            throw new CloudRuntimeException("Unable encrypt user secret key ", e);
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

    private void dropKeysIfExist(Connection conn) {
        HashMap<String, List<String>> uniqueKeys = new HashMap<String, List<String>>();
        List<String> keys = new ArrayList<String>();
        keys.add("public_ip_address");
        uniqueKeys.put("console_proxy", keys);
        uniqueKeys.put("secondary_storage_vm", keys);

        // drop keys
        s_logger.debug("Dropping public_ip_address keys from secondary_storage_vm and console_proxy tables...");
        for (String tableName : uniqueKeys.keySet()) {
            DbUpgradeUtils.dropKeysIfExist(conn, tableName, uniqueKeys.get(tableName), true);
        }
    }

    private void createNetworkOfferingServices(Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn
                    .prepareStatement("select id, dns_service, gateway_service, firewall_service, lb_service, userdata_service, vpn_service, dhcp_service, unique_name from network_offerings where traffic_type='Guest'");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                long id = rs.getLong(1);
                String uniqueName = rs.getString(9);

                ArrayList<String> services = new ArrayList<String>();
                if (rs.getLong(2) != 0) {
                    services.add("Dns");
                }

                if (rs.getLong(3) != 0) {
                    services.add("Gateway");
                }

                if (rs.getLong(4) != 0) {
                    services.add("Firewall");
                }

                if (rs.getLong(5) != 0) {
                    services.add("Lb");
                }

                if (rs.getLong(6) != 0) {
                    services.add("UserData");
                }

                if (rs.getLong(7) != 0) {
                    services.add("Vpn");
                }

                if (rs.getLong(8) != 0) {
                    services.add("Dhcp");
                }

                if (uniqueName.equalsIgnoreCase(NetworkOffering.DefaultSharedNetworkOfferingWithSGService.toString())) {
                    services.add("SecurityGroup");
                }

                if (uniqueName.equals(NetworkOffering.DefaultIsolatedNetworkOfferingWithSourceNatService.toString())) {
                    services.add("SourceNat");
                    services.add("PortForwarding");
                    services.add("StaticNat");
                }

                for (String service : services) {
                    pstmt = conn.prepareStatement("INSERT INTO ntwk_offering_service_map (`network_offering_id`, `service`, `provider`, `created`) values (?,?,?, now())");
                    pstmt.setLong(1, id);
                    pstmt.setString(2, service);
                    if (service.equalsIgnoreCase("SecurityGroup")) {
                        pstmt.setString(3, "SecurityGroupProvider");
                    } else {
                        pstmt.setString(3, "VirtualRouter");
                    }
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable to create service/provider map for network offerings", e);
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

    private void updateDomainNetworkRef(Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // update subdomain access field for existing domain specific networks
            pstmt = conn.prepareStatement("select value from configuration where name='allow.subdomain.network.access'");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                boolean subdomainAccess = Boolean.valueOf(rs.getString(1));
                pstmt = conn.prepareStatement("UPDATE domain_network_ref SET subdomain_access=?");
                pstmt.setBoolean(1, subdomainAccess);
                pstmt.executeUpdate();
                s_logger.debug("Successfully updated subdomain_access field in network_domain table with value " + subdomainAccess);
            }

            // convert zone level 2.2.x networks to ROOT domain 3.0 access networks
            pstmt = conn.prepareStatement("select id from networks where shared=true and is_domain_specific=false and traffic_type='Guest'");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                long networkId = rs.getLong(1);
                pstmt = conn.prepareStatement("INSERT INTO domain_network_ref (domain_id, network_id, subdomain_access) VALUES (1, ?, 1)");
                pstmt.setLong(1, networkId);
                pstmt.executeUpdate();
                s_logger.debug("Successfully converted zone specific network id=" + networkId + " to the ROOT domain level network with subdomain access set to true");
            }

        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable to update domain network ref", e);
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

    protected void createNetworkServices(Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ResultSet rs1 = null;
        try {
            pstmt = conn.prepareStatement("select id, network_offering_id from networks where traffic_type='Guest'");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                long networkId = rs.getLong(1);
                long networkOfferingId = rs.getLong(2);
                pstmt = conn.prepareStatement("select service, provider from ntwk_offering_service_map where network_offering_id=?");
                pstmt.setLong(1, networkOfferingId);
                rs1 = pstmt.executeQuery();
                while (rs1.next()) {
                    String service = rs1.getString(1);
                    String provider = rs1.getString(2);
                    pstmt = conn.prepareStatement("INSERT INTO ntwk_service_map (`network_id`, `service`, `provider`, `created`) values (?,?,?, now())");
                    pstmt.setLong(1, networkId);
                    pstmt.setString(2, service);
                    pstmt.setString(3, provider);
                    pstmt.executeUpdate();
                }
                s_logger.debug("Created service/provider map for network id=" + networkId);
            }
        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable to create service/provider map for networks", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }

                if (rs1 != null) {
                    rs1.close();
                }

                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    protected void updateReduntantRouters(Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ResultSet rs1 = null;
        try {
            // get all networks that need to be updated to the redundant network offerings
            pstmt = conn
                    .prepareStatement("select ni.network_id, n.network_offering_id from nics ni, networks n where ni.instance_id in (select id from domain_router where is_redundant_router=1) and n.id=ni.network_id and n.traffic_type='Guest'");
            rs = pstmt.executeQuery();
            pstmt = conn.prepareStatement("select count(*) from network_offerings");
            rs1 = pstmt.executeQuery();
            long ntwkOffCount = 0;
            while (rs1.next()) {
                ntwkOffCount = rs1.getLong(1);
            }

            s_logger.debug("Have " + ntwkOffCount + " networkOfferings");
            pstmt = conn.prepareStatement("CREATE TEMPORARY TABLE network_offerings2 ENGINE=MEMORY SELECT * FROM network_offerings WHERE id=1");
            pstmt.executeUpdate();

            HashMap<Long, Long> newNetworkOfferingMap = new HashMap<Long, Long>();

            while (rs.next()) {
                long networkId = rs.getLong(1);
                long networkOfferingId = rs.getLong(2);
                s_logger.debug("Updating network offering for the network id=" + networkId + " as it has redundant routers");
                Long newNetworkOfferingId = null;

                if (!newNetworkOfferingMap.containsKey(networkOfferingId)) {
                    // clone the record to
                    pstmt = conn.prepareStatement("INSERT INTO network_offerings2 SELECT * FROM network_offerings WHERE id=?");
                    pstmt.setLong(1, networkOfferingId);
                    pstmt.executeUpdate();

                    pstmt = conn.prepareStatement("SELECT unique_name FROM network_offerings WHERE id=?");
                    pstmt.setLong(1, networkOfferingId);
                    rs1 = pstmt.executeQuery();
                    String uniqueName = null;
                    while (rs1.next()) {
                        uniqueName = rs1.getString(1) + "-redundant";
                    }

                    pstmt = conn.prepareStatement("UPDATE network_offerings2 SET id=?, redundant_router_service=1, unique_name=?, name=? WHERE id=?");
                    ntwkOffCount = ntwkOffCount + 1;
                    newNetworkOfferingId = ntwkOffCount;
                    pstmt.setLong(1, newNetworkOfferingId);
                    pstmt.setString(2, uniqueName);
                    pstmt.setString(3, uniqueName);
                    pstmt.setLong(4, networkOfferingId);
                    pstmt.executeUpdate();

                    pstmt = conn.prepareStatement("INSERT INTO network_offerings SELECT * from network_offerings2 WHERE id=" + newNetworkOfferingId);
                    pstmt.executeUpdate();

                    pstmt = conn.prepareStatement("UPDATE networks SET network_offering_id=? where id=?");
                    pstmt.setLong(1, newNetworkOfferingId);
                    pstmt.setLong(2, networkId);
                    pstmt.executeUpdate();

                    newNetworkOfferingMap.put(networkOfferingId, ntwkOffCount);
                } else {
                    pstmt = conn.prepareStatement("UPDATE networks SET network_offering_id=? where id=?");
                    newNetworkOfferingId = newNetworkOfferingMap.get(networkOfferingId);
                    pstmt.setLong(1, newNetworkOfferingId);
                    pstmt.setLong(2, networkId);
                    pstmt.executeUpdate();
                }

                s_logger.debug("Successfully updated network offering id=" + networkId + " with new network offering id " + newNetworkOfferingId);
            }

        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable to update redundant router networks", e);
        } finally {
            try {
                pstmt = conn.prepareStatement("DROP TABLE network_offerings2");
                pstmt.executeUpdate();
                if (rs != null) {
                    rs.close();
                }

                if (rs1 != null) {
                    rs1.close();
                }

                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    protected void switchAccountSpecificNetworksToIsolated(Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ResultSet rs1 = null;
        try {
            // get all networks that need to be updated to the redundant network offerings
            pstmt = conn
                    .prepareStatement("select id, network_offering_id from networks where switch_to_isolated=1");
            rs = pstmt.executeQuery();
            pstmt = conn.prepareStatement("select count(*) from network_offerings");
            rs1 = pstmt.executeQuery();
            long ntwkOffCount = 0;
            while (rs1.next()) {
                ntwkOffCount = rs1.getLong(1);
            }

            s_logger.debug("Have " + ntwkOffCount + " networkOfferings");
            pstmt = conn.prepareStatement("CREATE TEMPORARY TABLE network_offerings2 ENGINE=MEMORY SELECT * FROM network_offerings WHERE id=1");
            pstmt.executeUpdate();

            HashMap<Long, Long> newNetworkOfferingMap = new HashMap<Long, Long>();

            while (rs.next()) {
                long networkId = rs.getLong(1);
                long networkOfferingId = rs.getLong(2);
                s_logger.debug("Updating network offering for the network id=" + networkId + " as it has switch_to_isolated=1");
                Long newNetworkOfferingId = null;

                if (!newNetworkOfferingMap.containsKey(networkOfferingId)) {
                    // clone the record to
                    pstmt = conn.prepareStatement("INSERT INTO network_offerings2 SELECT * FROM network_offerings WHERE id=?");
                    pstmt.setLong(1, networkOfferingId);
                    pstmt.executeUpdate();

                    pstmt = conn.prepareStatement("UPDATE network_offerings2 SET id=?, guest_type='Isolated', unique_name=?, name=? WHERE id=?");
                    ntwkOffCount = ntwkOffCount + 1;
                    newNetworkOfferingId = ntwkOffCount;
                    String uniqueName = "Isolated w/o source nat";
                    pstmt.setLong(1, newNetworkOfferingId);
                    pstmt.setString(2, uniqueName);
                    pstmt.setString(3, uniqueName);
                    pstmt.setLong(4, networkOfferingId);
                    pstmt.executeUpdate();

                    pstmt = conn.prepareStatement("INSERT INTO network_offerings SELECT * from network_offerings2 WHERE id=" + newNetworkOfferingId);
                    pstmt.executeUpdate();

                    pstmt = conn.prepareStatement("UPDATE networks SET network_offering_id=? where id=?");
                    pstmt.setLong(1, newNetworkOfferingId);
                    pstmt.setLong(2, networkId);
                    pstmt.executeUpdate();

                    newNetworkOfferingMap.put(networkOfferingId, ntwkOffCount);
                } else {
                    pstmt = conn.prepareStatement("UPDATE networks SET network_offering_id=? where id=?");
                    newNetworkOfferingId = newNetworkOfferingMap.get(networkOfferingId);
                    pstmt.setLong(1, newNetworkOfferingId);
                    pstmt.setLong(2, networkId);
                    pstmt.executeUpdate();
                }

                s_logger.debug("Successfully updated network offering id=" + networkId + " with new network offering id " + newNetworkOfferingId);
            }

            try {
                pstmt = conn.prepareStatement("ALTER TABLE `cloud`.`networks` DROP COLUMN `switch_to_isolated`");
                pstmt.executeUpdate();
            } catch (Exception ex) {
                // do nothing here
            }

        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable to switch networks to isolated", e);
        } finally {
            try {
                pstmt = conn.prepareStatement("DROP TABLE network_offerings2");
                pstmt.executeUpdate();
                pstmt = conn.prepareStatement("DROP TABLE network_offerings2");
                pstmt.executeUpdate();
                if (rs != null) {
                    rs.close();
                }

                if (rs1 != null) {
                    rs1.close();
                }

                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
            }
        }
    }
    
    private void migrateUserConcentratedPlannerChoice(Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("SELECT value FROM configuration where name = 'use.user.concentrated.pod.allocation'");
            rs = pstmt.executeQuery();
            Boolean isuserconcentrated = false;
            if(rs.next()) {
                String value = rs.getString(1); 
                isuserconcentrated = new Boolean(value);
            }
            rs.close();
            pstmt.close();
            
            if(isuserconcentrated){
                String currentAllocationAlgo = "random"; 
                pstmt = conn.prepareStatement("SELECT value FROM configuration where name = 'vm.allocation.algorithm'");
                rs = pstmt.executeQuery();
                if(rs.next()) {
                    currentAllocationAlgo = rs.getString(1);
                }
                rs.close();
                pstmt.close();
                
                String newAllocAlgo = "userconcentratedpod_random";
                if("random".equalsIgnoreCase(currentAllocationAlgo)){
                    newAllocAlgo = "userconcentratedpod_random";
                }else{
                    newAllocAlgo = "userconcentratedpod_firstfit";
                }
                
                pstmt = conn.prepareStatement("UPDATE configuration SET value = ? WHERE name = 'vm.allocation.algorithm'");
                pstmt.setString(1, newAllocAlgo);
                pstmt.executeUpdate();
                
            }

        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable to migrate the user_concentrated planner choice", e);
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
}
