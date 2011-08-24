/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.test.longrun;
import java.util.ArrayList;
import java.util.Random;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;


public class guestNetwork implements Runnable{
	public static final Logger s_logger= Logger.getLogger(guestNetwork.class.getClass());
	
	private String publicIp;
	private ArrayList<VirtualMachine> virtualMachines;
	private int retryNum;
	
	public guestNetwork(String publicIp, int retryNum){
		this.publicIp=publicIp;
		this.retryNum=retryNum;	
	}
	
	public ArrayList<VirtualMachine> getVirtualMachines() {
		return virtualMachines;
	}

	public void setVirtualMachines(ArrayList<VirtualMachine> virtualMachines) {
		this.virtualMachines = virtualMachines;
	}

	public void run(){
		NDC.push("Following thread has started"+Thread.currentThread().getName());
		int retry = 0;
					
				//Start copying files between machines in the network
				s_logger.info("The size of the array is " + this.virtualMachines.size());
				while (true) {
					try {
					if (retry > 0) {
						s_logger.info("Retry attempt : " + retry
								+ " ...sleeping 120 seconds before next attempt");
						Thread.sleep(120000);
					}
					for (VirtualMachine vm: this.virtualMachines ){
						
							s_logger.info("Attempting to SSH into linux host " + this.publicIp
									+ " with retry attempt: " + retry);
							Connection conn = new Connection(this.publicIp);
							conn.connect(null, 600000, 600000);

							s_logger.info("SSHed successfully into linux host " + this.publicIp);

							boolean isAuthenticated = conn.authenticateWithPassword("root",
									"password");

							if (isAuthenticated == false) {
								s_logger.info("Authentication failed");
							}
							//execute copy command
							Session sess = conn.openSession();
							String fileName;
							Random ran = new Random();
							fileName=Math.abs(ran.nextInt())+"-file";
							String copyCommand = new String ("./scpScript "+vm.getPrivateIp()+" "+fileName);
							s_logger.info("Executing " + copyCommand);
							sess.execCommand(copyCommand);
							Thread.sleep(120000);
							sess.close();
							
							//execute wget command
							sess = conn.openSession();
							String downloadCommand = new String ("wget http://172.16.0.220/scripts/checkDiskSpace.sh; chmod +x *sh; ./checkDiskSpace.sh; rm -rf checkDiskSpace.sh");
							s_logger.info("Executing " + downloadCommand);
							sess.execCommand(downloadCommand);
							Thread.sleep(120000);
							sess.close();
							
							//close the connection
							conn.close();
						}
					}catch (Exception ex) {
							s_logger.error(ex);
							retry++;
							if (retry == retryNum) {
								s_logger.info("Performance Guest Network test failed with error "
										+ ex.getMessage()) ;
							}
					}		
					}

	}	
}
