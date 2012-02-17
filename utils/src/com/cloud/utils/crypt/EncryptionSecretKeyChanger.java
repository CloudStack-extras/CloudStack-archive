/**
 * Copyright (C) 2012 Citrix Systems, Inc.  All rights reserved
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
package com.cloud.utils.crypt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.properties.EncryptableProperties;

import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/*
 * EncryptionSecretKeyChanger updates Management Secret Key / DB Secret Key or both.
 * DB secret key is validated against the key in db.properties
 * db.properties is updated with values encrypted using new MS secret key
 * DB data migrated using new DB secret key
 */
public class EncryptionSecretKeyChanger {

	private StandardPBEStringEncryptor oldEncryptor = new StandardPBEStringEncryptor();
	private StandardPBEStringEncryptor newEncryptor = new StandardPBEStringEncryptor();
	private static final String keyFile = "/etc/cloud/management/key";

	public static void main(String[] args){
		List<String> argsList = Arrays.asList(args);
		Iterator<String> iter = argsList.iterator();
		String oldMSKey = null;
		String oldDBKey = null;
		String newMSKey = null; 
		String newDBKey = null;

		//Parse command-line args
		while (iter.hasNext()) {
			String arg = iter.next();
			// Old MS Key
			if (arg.equals("-m")) {
				oldMSKey = iter.next();
			}
			// Old DB Key
			if (arg.equals("-d")) {
				oldDBKey = iter.next();
			}
			// New MS Key
			if (arg.equals("-n")) {
				newMSKey = iter.next();
			}
			// New DB Key
			if (arg.equals("-e")) {
				newDBKey = iter.next();
			}
		}

		if(oldMSKey == null || oldDBKey ==null){
			System.out.println("Existing MS secret key or DB secret key is not provided");
			usage();
			return;
		}

		if(newMSKey == null && newDBKey ==null){
			System.out.println("New MS secret key and DB secret are both not provided");
			usage();
			return;
		}

		final File dbPropsFile = PropertiesUtil.findConfigFile("db.properties");
		final Properties dbProps;
		EncryptionSecretKeyChanger keyChanger = new EncryptionSecretKeyChanger();
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		keyChanger.initEncryptor(encryptor, oldMSKey);
		dbProps = new EncryptableProperties(encryptor);
		PropertiesConfiguration backupDBProps = null;

		System.out.println("Parsing db.properties file");
		try {
			dbProps.load(new FileInputStream(dbPropsFile));
			backupDBProps = new PropertiesConfiguration(dbPropsFile);
		} catch (FileNotFoundException e) {
			System.out.println("db.properties file not found while reading DB secret key" +e.getMessage());
		} catch (IOException e) {
			System.out.println("Error while reading DB secret key from db.properties" +e.getMessage());
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		
		String dbSecretKey = null;
		try {
			dbSecretKey = dbProps.getProperty("db.cloud.encrypt.secret");
		} catch (EncryptionOperationNotPossibleException e) {
			System.out.println("Failed to decrypt existing DB secret key from db.properties. "+e.getMessage());
			return;
		}
		
		if(!oldDBKey.equals(dbSecretKey)){
			System.out.println("Incorrect MS Secret Key or DB Secret Key");
			return;
		}

		System.out.println("Secret key provided matched the key in db.properties");
		final String encryptionType = dbProps.getProperty("db.cloud.encryption.type");
		
		if(newMSKey == null){
			System.out.println("No change in MS Key. Skipping migrating db.properties");
		} else {
			if(!keyChanger.migrateProperties(dbPropsFile, dbProps, newMSKey, newDBKey)){
				System.out.println("Failed to update db.properties");
				return;
			} else {
				//db.properties updated successfully
				if(encryptionType.equals("file")){
					//update key file with new MS key
					try {
						FileWriter fwriter = new FileWriter(keyFile);
						BufferedWriter bwriter = new BufferedWriter(fwriter); 
						bwriter.write(newMSKey);
						bwriter.close();
					} catch (IOException e) {
						System.out.println("Failed to write new secret to file. Please update the file manually");
					} 
				}
			}
		}
		
		boolean success = false;
		if(newDBKey == null || newDBKey.equals(oldDBKey)){
			System.out.println("No change in DB Secret Key. Skipping Data Migration");
		} else {
			EncryptionSecretKeyChecker.initEncryptorForMigration(oldMSKey);
			try {
				success = keyChanger.migrateData(oldDBKey, newDBKey);
			} catch (Exception e) {
				System.out.println("Error during data migration");
				e.printStackTrace();
				success = false;
			}
		}
		
		if(success){
			System.out.println("Successfully updated secret key(s)");
		}
		else {
			System.out.println("Data Migration failed. Reverting db.properties");
			//revert db.properties
			try {
				backupDBProps.save();
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
			if(encryptionType.equals("file")){
				//revert secret key in file
				try {
					FileWriter fwriter = new FileWriter(keyFile);
					BufferedWriter bwriter = new BufferedWriter(fwriter); 
					bwriter.write(oldMSKey);
					bwriter.close();
				} catch (IOException e) {
					System.out.println("Failed to revert to old secret to file. Please update the file manually");
				} 
			}
		}
	}
	
	private boolean migrateProperties(File dbPropsFile, Properties dbProps, String newMSKey, String newDBKey){
		System.out.println("Migrating db.properties..");
		StandardPBEStringEncryptor msEncryptor = new StandardPBEStringEncryptor();;
		initEncryptor(msEncryptor, newMSKey);
		
		try {
			PropertiesConfiguration newDBProps = new PropertiesConfiguration(dbPropsFile);
			if(newDBKey!=null && !newDBKey.isEmpty()){
				newDBProps.setProperty("db.cloud.encrypt.secret", "ENC("+msEncryptor.encrypt(newDBKey)+")");
			}
			String prop = dbProps.getProperty("db.cloud.password");
			if(prop!=null && !prop.isEmpty()){
				newDBProps.setProperty("db.cloud.password", "ENC("+msEncryptor.encrypt(prop)+")");
			}
			prop = dbProps.getProperty("db.usage.password");
			if(prop!=null && !prop.isEmpty()){
				newDBProps.setProperty("db.usage.password", "ENC("+msEncryptor.encrypt(prop)+")");
			}
			newDBProps.save(dbPropsFile.getAbsolutePath());
		} catch (Exception e) { 		
			e.printStackTrace();
			return false;
		}
		System.out.println("Migrating db.properties Done.");
		return true;
	}

	private boolean migrateData(String oldDBKey, String newDBKey){
		System.out.println("Begin Data migration");
		initEncryptor(oldEncryptor, oldDBKey);
		initEncryptor(newEncryptor, newDBKey);
		System.out.println("Initialised Encryptors");
		
		Transaction txn = Transaction.open("Migrate");
		txn.start();
		try {
			Connection conn;
			try {
				conn = txn.getConnection();
			} catch (SQLException e) {
				throw new CloudRuntimeException("Unable to migrate encrypted data in the database", e);
			}

			migrateConfigValues(conn);
			migrateHostDetails(conn);
			migrateVNCPassword(conn);
			migrateUserCredentials(conn);

			txn.commit();
		} finally {
			txn.close();
		}
		System.out.println("End Data migration");
		return true;
	}

	private void initEncryptor(StandardPBEStringEncryptor encryptor, String secretKey){
		encryptor.setAlgorithm("PBEWithMD5AndDES");
		SimpleStringPBEConfig stringConfig = new SimpleStringPBEConfig();
		stringConfig.setPassword(secretKey);
		encryptor.setConfig(stringConfig);
	}

	private String migrateValue(String value){
		if(value ==null || value.isEmpty()){
			return value;
		}
		String decryptVal = oldEncryptor.decrypt(value);
		return newEncryptor.encrypt(decryptVal);
	}

	private void migrateConfigValues(Connection conn) {
		System.out.println("Begin migrate config values");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement("select name, value from configuration where category in ('Hidden', 'Secure')");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString(1);
				String value = rs.getString(2);
				if(value == null || value.isEmpty()){
					continue;
				}
				String encryptedValue = migrateValue(value);
				pstmt = conn.prepareStatement("update configuration set value=? where name=?");
				pstmt.setBytes(1, encryptedValue.getBytes("UTF-8"));
				pstmt.setString(2, name);
				pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new CloudRuntimeException("Unable to update configuration values ", e);
		} catch (UnsupportedEncodingException e) {
			throw new CloudRuntimeException("Unable to update configuration values ", e);
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
		System.out.println("End migrate config values");
	}

	private void migrateHostDetails(Connection conn) {
		System.out.println("Begin migrate host details");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement("select id, value from host_details where name = 'password'");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				long id = rs.getLong(1);
				String value = rs.getString(2);
				if(value == null || value.isEmpty()){
					continue;
				}
				String encryptedValue = migrateValue(value);
				pstmt = conn.prepareStatement("update host_details set value=? where id=?");
				pstmt.setBytes(1, encryptedValue.getBytes("UTF-8"));
				pstmt.setLong(2, id);
				pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new CloudRuntimeException("Unable update host_details values ", e);
		} catch (UnsupportedEncodingException e) {
			throw new CloudRuntimeException("Unable update host_details values ", e);
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
		System.out.println("End migrate host details");
	}

	private void migrateVNCPassword(Connection conn) {
		System.out.println("Begin migrate VNC password");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement("select id, vnc_password from vm_instance");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				long id = rs.getLong(1);
				String value = rs.getString(2);
				if(value == null || value.isEmpty()){
					continue;
				}
				String encryptedValue = migrateValue(value);
				pstmt = conn.prepareStatement("update vm_instance set vnc_password=? where id=?");
				pstmt.setBytes(1, encryptedValue.getBytes("UTF-8"));
				pstmt.setLong(2, id);
				pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new CloudRuntimeException("Unable update vm_instance vnc_password ", e);
		} catch (UnsupportedEncodingException e) {
			throw new CloudRuntimeException("Unable update vm_instance vnc_password ", e);
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
		System.out.println("End migrate VNC password");
	}

	private void migrateUserCredentials(Connection conn) {
		System.out.println("Begin migrate user credentials");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement("select id, secret_key from user");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				long id = rs.getLong(1);
				String secretKey = rs.getString(2);
				if(secretKey == null || secretKey.isEmpty()){
					continue;
				}
				String encryptedSecretKey = migrateValue(secretKey);
				pstmt = conn.prepareStatement("update user set secret_key=? where id=?");
				pstmt.setBytes(1, encryptedSecretKey.getBytes("UTF-8"));
				pstmt.setLong(2, id);
				pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new CloudRuntimeException("Unable update user secret key ", e);
		} catch (UnsupportedEncodingException e) {
			throw new CloudRuntimeException("Unable update user secret key ", e);
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
		System.out.println("End migrate user credentials");
	}

	private static void usage(){
		System.out.println("Usage: \tEncryptionSecretKeyChanger \n" +
				"\t\t-m <Mgmt Secret Key> \n" +
				"\t\t-d <DB Secret Key> \n" +
				"\t\t-n [New Mgmt Secret Key] \n" +
				"\t\t-e [New DB Secret Key]");
	}
}
