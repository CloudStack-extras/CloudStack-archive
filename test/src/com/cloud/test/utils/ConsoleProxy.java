/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.test.utils;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;

public class ConsoleProxy implements Runnable {
	public static String proxyIp;
	private String command;
	private int connectionsMade;
	private long responseTime;
	public static final Logger s_logger = Logger.getLogger(ConsoleProxy.class
			.getClass());

	public ConsoleProxy(String port, String sid, String host) {
		this.command = "https://" + proxyIp
				+ ".realhostip.com:8000/getscreen?w=100&h=75&host=" + host
				+ "&port=" + port + "&sid=" + sid;
		s_logger.info("Command for a console proxy is " + this.command);
		this.connectionsMade=0;
		this.responseTime=0;
	}

	public int getConnectionsMade() {
		return this.connectionsMade;
	}
	
	public long getResponseTime() {
		return this.responseTime;
	}
	
	public void run() {
		while (true){
			
			Script myScript = new Script("wget");
			myScript.add(command);
			myScript.execute();
			long begin = System.currentTimeMillis();
			wgetInt process = new wgetInt();
			String response = myScript.execute(process);
			long end = process.getEnd();
			if (response!=null){
				s_logger.info("Content lenght is incorrect: "+response);
			}
			
			long duration = (end - begin);
			this.connectionsMade++;
			this.responseTime=this.responseTime+duration;
			try{
			Thread.sleep(1000);
			}catch (InterruptedException e){
				
			}
			
		}
	}

	public class wgetInt extends OutputInterpreter {
		private long end;
		
		public long getEnd() {
			return end;
		}

		public void setEnd(long end) {
			this.end = end;
		}

		@Override
		public String interpret(BufferedReader reader) throws IOException {
			// TODO Auto-generated method stub
			end = System.currentTimeMillis();
			String status = null;
			String line = null;
            while ((line = reader.readLine()) != null) {
                int index = line.indexOf("Length:");
                if (index == -1) {
                    continue;
                }
                else{
                	int index1 = line.indexOf("Length: 1827");
                	if (index1 == -1) {
                        return status;
                    }
                	else
                		status=line;
                }
                	          	
            }
			return status;
		}

	}
}
