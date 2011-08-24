/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.test.longrun;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


import org.apache.log4j.Logger;


public class BuildGuestNetwork {

	public static final Logger s_logger= Logger.getLogger(BuildGuestNetwork.class.getClass());
	private static final int _apiPort=8096;
	private static final int _developerPort=8080;
	private static final String _apiUrl = "/client/api";
	private static int numVM=1;
	private static long zoneId=-1L; 
	private static long templateId=3;
	private static long serviceOfferingId=1;
	


		public static void main (String[] args){
			
			List<String> argsList = Arrays.asList(args);
			Iterator<String> iter = argsList.iterator();
			String host = "http://localhost";
			int numThreads = 1;
			
			while (iter.hasNext()){
				String arg = iter.next();
				if (arg.equals("-h")){
					host="http://"+iter.next();
				}
				if (arg.equals("-t")){
					numThreads=Integer.parseInt(iter.next());
				}	
				if (arg.equals("-n")){
					numVM=Integer.parseInt(iter.next());
				}
				if (arg.equals("-z")){
					zoneId=Integer.parseInt(iter.next());
				}
				
				if (arg.equals("-e")){
					templateId=Integer.parseInt(iter.next());
				}
				
				if (arg.equals("-s")){
					serviceOfferingId=Integer.parseInt(iter.next());
				}
			}
			
			final String server = host + ":" + _apiPort + "/";
			final String developerServer = host + ":" + _developerPort + _apiUrl;
			s_logger.info("Starting test in "+numThreads+" thread(s). Each thread is launching "+numVM+" VMs");
			
			for (int i=0; i<numThreads; i++){
				new Thread(new Runnable() {
					public  void run() {
						try{

						String username = null;
						String singlePrivateIp=null;
						Random ran = new Random();
						username = Math.abs(ran.nextInt())+ "-user";
						
						//Create User
						User myUser = new User(username,username, server, developerServer);
						try{
							myUser.launchUser();
							myUser.registerUser();
						}catch (Exception e){
							s_logger.warn("Error code: ", e);
						}
						
						if (myUser.getUserId()!=null){
							s_logger.info("User "+myUser.getUserName()+" was created successfully, starting VM creation");
							//create VMs for the user
							for (int i=0; i<numVM; i++){
								//Create a new VM, add it to the list of user's VMs
								VirtualMachine myVM = new VirtualMachine(myUser.getUserId());
								myVM.deployVM(zoneId, serviceOfferingId, templateId, myUser.getDeveloperServer(), myUser.getApiKey(), myUser.getSecretKey());
								myUser.getVirtualMachines().add(myVM);
								singlePrivateIp=myVM.getPrivateIp();
								
								if (singlePrivateIp!=null){
									s_logger.info("VM with private Ip "+singlePrivateIp+" was successfully created");
								}
								else{
									s_logger.info("Problems with VM creation for a user"+myUser.getUserName());
									s_logger.info("Deployment failed");
									break;
								}			
							}
							
							s_logger.info("Deployment done..."+numVM+" VMs were created.");
						}

						}catch (Exception e){
							s_logger.error(e);
						}
					}
				}).start();
		
			}
		}
	
	
}
