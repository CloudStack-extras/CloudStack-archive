/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.test.longrun;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import com.cloud.test.stress.TestClientWithAPI;



public class PerformanceWithAPI {
	
public static final Logger s_logger= Logger.getLogger(PerformanceWithAPI.class.getClass());
private static final int _retry=10;
private static final int _apiPort=8096;
private static int numVM=2;
private static final long _zoneId=-1L;
private static final long _templateId=3;
private static final long _serviceOfferingId=1;
private static final String _apiUrl = "/client/api";
private static final int _developerPort=8080;



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
					String singlePublicIp=null;
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
							myVM.deployVM(_zoneId, _serviceOfferingId, _templateId, myUser.getDeveloperServer(), myUser.getApiKey(), myUser.getSecretKey());
							myUser.getVirtualMachines().add(myVM);
							singlePrivateIp=myVM.getPrivateIp();
							
							if (singlePrivateIp!=null){
								s_logger.info("VM with private Ip "+singlePrivateIp+" was successfully created");
							}
							else{
								s_logger.info("Problems with VM creation for a user"+myUser.getUserName());
								break;
							}
							
							
						//get public IP address for the User				
							myUser.retrievePublicIp(_zoneId);
							singlePublicIp=myUser.getPublicIp().get(myUser.getPublicIp().size()-1);
							if (singlePublicIp!=null){
								s_logger.info("Successfully got public Ip "+singlePublicIp+" for user "+myUser.getUserName());
							}
							else{
								s_logger.info("Problems with getting public Ip address for user"+myUser.getUserName());
								break;
							}
							
							
						//create ForwardProxy rules for user's VMs
							int responseCode = CreateForwardingRule(myUser, singlePrivateIp, singlePublicIp, "22", "22");
							if (responseCode==500)
								break;
						}
						
						s_logger.info("Deployment successful..."+numVM+" VMs were created. Waiting for 5 min before performance test");
						Thread.sleep(300000L); // Wait 
						
						
						//Start performance test for the user
						s_logger.info("Starting performance test for Guest network that has "+myUser.getPublicIp().size()+" public IP addresses");		
						for (int j=0; j<myUser.getPublicIp().size(); j++){
							s_logger.info("Starting test for user which has "+myUser.getVirtualMachines().size()+" vms. Public IP for the user is "+myUser.getPublicIp().get(j)+" , number of retries is "+_retry+" , private IP address of the machine is"+myUser.getVirtualMachines().get(j).getPrivateIp());
							guestNetwork myNetwork =new guestNetwork(myUser.getPublicIp().get(j), _retry);
							myNetwork.setVirtualMachines(myUser.getVirtualMachines());
							new Thread(myNetwork).start();
						}
						
					}
					}catch (Exception e){
						s_logger.error(e);
					}
				}
			}).start();
	
		}
	}

	private static int CreateForwardingRule(User myUser, String privateIp, String publicIp, String publicPort, String privatePort) throws IOException{
		String encodedPrivateIp=URLEncoder.encode(""+privateIp, "UTF-8");
		String encodedPublicIp=URLEncoder.encode(""+publicIp, "UTF-8");
		String encodedPrivatePort=URLEncoder.encode(""+privatePort, "UTF-8");
		String encodedPublicPort=URLEncoder.encode(""+publicPort, "UTF-8");
		String encodedApiKey = URLEncoder.encode(myUser.getApiKey(), "UTF-8");
		int responseCode=500;
		
		
		String requestToSign = "apiKey=" + encodedApiKey
		+ "&command=createOrUpdateIpForwardingRule&privateIp="
		+ encodedPrivateIp + "&privatePort=" + encodedPrivatePort
		+ "&protocol=tcp&publicIp="
		+ encodedPublicIp + "&publicPort="+encodedPublicPort;
		
		requestToSign = requestToSign.toLowerCase();
		s_logger.info("Request to sign is "+requestToSign);
		
		String signature = TestClientWithAPI.signRequest(requestToSign, myUser.getSecretKey());
		String encodedSignature = URLEncoder.encode(signature, "UTF-8");

		String url = myUser.getDeveloperServer() + "?command=createOrUpdateIpForwardingRule"
		+ "&publicIp=" + encodedPublicIp
		+ "&publicPort="+encodedPublicPort+"&privateIp=" + encodedPrivateIp
		+ "&privatePort=" + encodedPrivatePort + "&protocol=tcp&apiKey=" + encodedApiKey
		+ "&signature=" + encodedSignature;
		
		s_logger.info("Trying to create IP forwarding rule: "+url);
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url);
		responseCode = client.executeMethod(method);
		s_logger.info("create ip forwarding rule response code: "
				+ responseCode);
		if (responseCode == 200) {
			s_logger.info("The rule is created successfully");
		} else if (responseCode == 500) {
			InputStream is = method.getResponseBodyAsStream();
			Map<String, String> errorInfo = TestClientWithAPI.getSingleValueFromXML(is,
					new String[] { "errorCode", "description" });
			s_logger
					.error("create ip forwarding rule (linux) test failed with errorCode: "
							+ errorInfo.get("errorCode")
							+ " and description: "
							+ errorInfo.get("description"));
		} else {
			s_logger.error("internal error processing request: "
					+ method.getStatusText());
		}
		return responseCode;
	}
	

}