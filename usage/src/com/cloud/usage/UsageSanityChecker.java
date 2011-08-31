/**
 * *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
*
 * 
 */

package com.cloud.usage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.cloud.utils.db.Transaction;

public class UsageSanityChecker {
    
    private StringBuffer errors;
    private String lastCheckId = "";
    private final String lastCheckFile = "/usr/local/libexec/sanity-check-last-id"; 
    
    private boolean checkMaxUsage(Connection conn) throws SQLException{
        
       PreparedStatement pstmt = conn.prepareStatement("SELECT value FROM `cloud`.`configuration` where name = 'usage.stats.job.aggregation.range'");
       ResultSet rs = pstmt.executeQuery();
       
       int aggregationRange = 1440; 
       if(rs.next()){
           aggregationRange = rs.getInt(1);
       } else {
           System.out.println("Failed to retrieve aggregation range. Using default : "+aggregationRange);
       }
       
       int aggregationHours = aggregationRange / 60;
       
       /*
        * Check for usage records with raw_usage > aggregationHours
        */
       pstmt = conn.prepareStatement("SELECT count(*) FROM `cloud_usage`.`cloud_usage` cu where usage_type not in (4,5) and raw_usage > "+aggregationHours+lastCheckId);
       rs = pstmt.executeQuery();
       if(rs.next() && (rs.getInt(1) > 0)){
           errors.append("Error: Found "+rs.getInt(1)+" usage records with raw_usage > "+aggregationHours);
           errors.append("\n");
           return false;
       }
       return true;
    }
    
    private boolean checkVmUsage(Connection conn) throws SQLException{
        boolean success = true;
        /*
         * Check for Vm usage records which are created after the vm is destroyed 
         */
        PreparedStatement pstmt = conn.prepareStatement("select count(*) from cloud_usage.cloud_usage cu inner join cloud.vm_instance vm where vm.type = 'User' " +
        		"and cu.usage_type in (1 , 2) and cu.usage_id = vm.id and cu.start_date > vm.removed"+lastCheckId);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next() && (rs.getInt(1) > 0)){
            errors.append("Error: Found "+rs.getInt(1)+" Vm usage records which are created after Vm is destroyed");
            errors.append("\n");
            success = false;
        }
        
        /*
         * Check for Vms which have multiple running vm records in helper table 
         */
        pstmt = conn.prepareStatement("select sum(cnt) from (select count(*) as cnt from cloud_usage.usage_vm_instance where usage_type =1 " +
        		"and end_date is null group by vm_instance_id having count(vm_instance_id) > 1) c ;");
        rs = pstmt.executeQuery();
        if(rs.next() && (rs.getInt(1) > 0)){
            errors.append("Error: Found "+rs.getInt(1)+" duplicate running Vm entries in vm usage helper table");
            errors.append("\n");
            success = false;
        }
        
        /*
         * Check for Vms which have multiple allocated vm records in helper table 
         */
        pstmt = conn.prepareStatement("select sum(cnt) from (select count(*) as cnt from cloud_usage.usage_vm_instance where usage_type =2 " +
        "and end_date is null group by vm_instance_id having count(vm_instance_id) > 1) c ;");
        rs = pstmt.executeQuery();
        if(rs.next() && (rs.getInt(1) > 0)){
            errors.append("Error: Found "+rs.getInt(1)+" duplicate allocated Vm entries in vm usage helper table");
            errors.append("\n");
            success = false;
        }
        
        /*
         * Check for Vms which have running vm entry without allocated vm  entry in helper table 
         */
        pstmt = conn.prepareStatement("select count(vm_instance_id) from cloud_usage.usage_vm_instance o where o.end_date is null and o.usage_type=1 and not exists " +
        		"(select 1 from cloud_usage.usage_vm_instance i where i.vm_instance_id=o.vm_instance_id and usage_type=2 and i.end_date is null)");
        rs = pstmt.executeQuery();
        if(rs.next() && (rs.getInt(1) > 0)){
            errors.append("Error: Found "+rs.getInt(1)+" running Vm entries without corresponding allocated entries in vm usage helper table");
            errors.append("\n");
            success = false;
        }
        return success;
    }
    
    private boolean checkVolumeUsage(Connection conn) throws SQLException{
        boolean success = true;
        /*
         * Check for Volume usage records which are created after the volume is removed 
         */
        PreparedStatement pstmt = conn.prepareStatement("select count(*) from cloud_usage.cloud_usage cu inner join cloud.volumes v " +
        		"where cu.usage_type = 6 and cu.usage_id = v.id and cu.start_date > v.removed"+lastCheckId);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next() && (rs.getInt(1) > 0)){
            errors.append("Error: Found "+rs.getInt(1)+" volume usage records which are created after volume is removed");
            errors.append("\n");
            success = false;
        }
        
        /*
         * Check for duplicate records in volume usage helper table
         */
        pstmt = conn.prepareStatement("select sum(cnt) from (select count(*) as cnt from cloud_usage.usage_volume " +
        		"where deleted is null group by id having count(id) > 1) c;");
        rs = pstmt.executeQuery();
        if(rs.next() && (rs.getInt(1) > 0)){
            errors.append("Error: Found "+rs.getInt(1)+" duplicate records is volume usage helper table");
            errors.append("\n");
            success = false;
        }
        return success;
    }
    
    private boolean checkTemplateISOUsage(Connection conn) throws SQLException{
        /*
         * Check for Template/ISO usage records which are created after it is removed 
         */
        PreparedStatement pstmt = conn.prepareStatement("select count(*) from cloud_usage.cloud_usage cu inner join cloud.template_zone_ref tzr " +
        		"where cu.usage_id = tzr.template_id and cu.zone_id = tzr.zone_id and cu.usage_type in (7,8) and cu.start_date > tzr.removed"+lastCheckId);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next() && (rs.getInt(1) > 0)){
            errors.append("Error: Found "+rs.getInt(1)+" template/ISO usage records which are created after it is removed");
            errors.append("\n");
            return false;
        }
        return true;
    }
    
    private boolean checkSnapshotUsage(Connection conn) throws SQLException{
        /*
         * Check for snapshot usage records which are created after snapshot is removed 
         */
        PreparedStatement pstmt = conn.prepareStatement("select count(*) from cloud_usage.cloud_usage cu inner join cloud.snapshots s " +
        		"where cu.usage_id = s.id and cu.usage_type = 9 and cu.start_date > s.removed"+lastCheckId);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next() && (rs.getInt(1) > 0)){
            errors.append("Error: Found "+rs.getInt(1)+" snapshot usage records which are created after snapshot is removed");
            errors.append("\n");
            return false;
        }
        return true;
    }
    
    public String runSanityCheck() throws SQLException{
        try {
            BufferedReader reader = new BufferedReader( new FileReader (lastCheckFile));
            String last_id  = null;
            if( (reader != null) && ( last_id = reader.readLine() ) != null ) {
                int lastId = Integer.parseInt(last_id);
                if(lastId > 0){
                    lastCheckId = " and cu.id > "+last_id;
                }
            }
            reader.close();
        } catch (Exception e) {
            // Error while reading last check id  
        }

        Connection conn = Transaction.getStandaloneConnection();
        int maxId = 0;
        PreparedStatement pstmt = conn.prepareStatement("select max(id) from cloud_usage.cloud_usage");
        ResultSet rs = pstmt.executeQuery();
        if(rs.next() && (rs.getInt(1) > 0)){
            maxId = rs.getInt(1);
            lastCheckId += " and cu.id <= "+maxId;
        }
        errors = new StringBuffer();
        checkMaxUsage(conn);
        checkVmUsage(conn);
        checkVolumeUsage(conn);
        checkTemplateISOUsage(conn);
        checkSnapshotUsage(conn);
        FileWriter fstream;
        try {
            fstream = new FileWriter(lastCheckFile);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(""+maxId);
            out.close();
        } catch (IOException e) {
         // Error while writing last check id
        } 
        return errors.toString();
    }
    
    public static void main(String args[]){
        UsageSanityChecker usc = new UsageSanityChecker();
        String sanityErrors;
        try {
            sanityErrors = usc.runSanityCheck();
            if(sanityErrors.length() > 0){
                System.out.println(sanityErrors.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
