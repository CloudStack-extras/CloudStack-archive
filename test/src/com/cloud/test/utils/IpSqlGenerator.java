/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.test.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.StringTokenizer;

public class IpSqlGenerator {
	public static void main (String[] args) {
		try {
			if (args.length != 5) {
				System.out.println("Usage -- generate-ip.sh <public|private> <begin ip range> <end ip range> <data center id> <pod id>");
				System.out.println("Example -- generate-ip.sh public 192.168.1.1 192.168.1.255 1 1");
				System.out.println("  will generate ips ranging from public ips 192.168.1.1 to 192.168.1.255 for dc 1 and pod 1");
				return;
			}
			
			String type = args[0];
			
			StringTokenizer st = new StringTokenizer(args[1], ".");
			int ipS1 = Integer.parseInt(st.nextToken());
			int ipS2 = Integer.parseInt(st.nextToken());
			int ipS3 = Integer.parseInt(st.nextToken());
			int ipS4 = Integer.parseInt(st.nextToken());
			
			st = new StringTokenizer(args[2], ".");
			int ipE1 = Integer.parseInt(st.nextToken());
			int ipE2 = Integer.parseInt(st.nextToken());
			int ipE3 = Integer.parseInt(st.nextToken());
			int ipE4 = Integer.parseInt(st.nextToken());
			
			String dcId = args[3];
			String podId = args[4];
			
			if (type.equals("private")) {
				FileOutputStream fs = new FileOutputStream(new File("private-ips.sql"));
				DataOutputStream out = new DataOutputStream(fs);
				for (int i = ipS1; i <= ipE1; i++) {
					for (int j = ipS2; j <= ipE2; j++) {
						for (int k = ipS3; k <= ipE3; k++) {
							for (int l = ipS4; l <= ipE4; l++) {
								out.writeBytes("INSERT INTO `vmops`.`dc_ip_address_alloc` (ip_address, data_center_id, pod_id) VALUES ('" 
										+ i + "." + j + "." + k + "." + l + "'," + dcId + "," + podId + ");\r\n");
							}
						}
					}
				}
				out.writeBytes("\r\n");
				out.flush();
				out.close();
			} else {
				FileOutputStream fs = new FileOutputStream(new File("public-ips.sql"));
				DataOutputStream out = new DataOutputStream(fs);
				for (int i = ipS1; i <= ipE1; i++) {
					for (int j = ipS2; j <= ipE2; j++) {
						for (int k = ipS3; k <= ipE3; k++) {
							for (int l = ipS4; l <= ipE4; l++) {
								out.writeBytes("INSERT INTO `vmops`.`user_ip_address` (ip_address, data_center_id) VALUES ('" 
										+ i + "." + j + "." + k + "." + l + "'," + dcId + ");\r\n");
							}
						}
					}
				}
				out.writeBytes("\r\n");
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			
		}
	}
}
