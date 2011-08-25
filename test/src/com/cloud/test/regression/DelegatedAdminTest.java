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

package com.cloud.test.regression;


import java.util.HashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cloud.test.regression.ApiCommand.ResponseType;

public class DelegatedAdminTest extends TestCase{
	
public static final Logger s_logger = Logger.getLogger(DelegatedAdminTest.class.getName());

	public DelegatedAdminTest(){
		this.setClient();
		this.setParam(new HashMap<String, String>());
	}
	
	public boolean executeTest(){
		int error=0;
	
		for (Document eachElement: this.getInputFile()) {
			
			Element rootElement = eachElement.getDocumentElement();
			NodeList commandLst = rootElement.getElementsByTagName("command");
			
			//Analyze each command, send request and build the array list of api commands
			for (int i=0; i<commandLst.getLength(); i++) {
				boolean verify = false;
				Node fstNode = commandLst.item(i);
			    Element fstElmnt = (Element) fstNode;
			    
			    //new command
				ApiCommand api = new ApiCommand(fstElmnt, this.getParam(), this.getCommands());
				
				if ((eachElement.getElementsByTagName("delegated_admin_verify_part2").getLength() != 0) && !(api.getName().equals("registerUserKeys"))) {
					if (api.getName().startsWith("list")) {
					    
					    if (this.denyToExecute()) {
	                        api.setResponseType(ResponseType.EMPTY);
	                    }
						verify = true;
					}
					
					if (this.denyToExecute()) {
					    api.setResponseType(ResponseType.ERROR);
					}
				}
				
				//send a command
				api.sendCommand(this.getClient(), null);
				
				//verify the response of the command
				if ((verify == true) && !(api.getResponseType() == ResponseType.ERROR || api.getResponseType() == ResponseType.EMPTY)) {
					s_logger.error("Test case " + api.getTestCaseInfo() + " failed. Command that was supposed to fail, passed. The command was sent with the following url " + api.getUrl());
					error++;
				} 
				else if ((verify == true) && (api.getResponseType() == ResponseType.ERROR || api.getResponseType() == ResponseType.EMPTY)) {
					s_logger.info("Test case " + api.getTestCaseInfo() + " passed");
				}
				else if ((api.getResponseType() == ResponseType.ERROR) && (api.getResponseCode() == 200)) {
					s_logger.error("Test case " + api.getTestCaseInfo() + " failed. Command that was supposed to fail, passed. The command was sent with the following url " + api.getUrl());
					error++;
				}
				else if ((api.getResponseType() != ResponseType.ERROR) && (api.getResponseCode() == 200)) {
					//set parameters for the future use
					if (api.setParam(this.getParam()) == false) {
						s_logger.error("Exiting the test...Command " + api.getName() + " didn't return parameters needed for the future use. The command was sent with url " + api.getUrl());
						return false;
					}
					else if (api.getTestCaseInfo() != null){
						s_logger.info("Test case " + api.getTestCaseInfo() + " passed");
					}
				}
				else if ((api.getResponseType() != ResponseType.ERROR) && (api.getResponseCode() != 200)) {
					s_logger.error("Test case  " + api.getTestCaseInfo() + " failed with an error code " + api.getResponseCode() + " . Command was sent with url  " + api.getUrl());
					if (api.getRequired() == true) {
						s_logger.info("The command is required for the future use, so exiging");
						return false;
					}
					error++;
				}
				else if (api.getTestCaseInfo() != null){
						s_logger.info("Test case " + api.getTestCaseInfo() +  " passed");
				
				}	
			}
		}
		
		if (error != 0)
			return false;
		else
			return true;
	}
	
	public boolean denyToExecute () {
		boolean result = true;
		Integer level1 = Integer.valueOf(this.getParam().get("domainlevel1"));
		Integer level2 = Integer.valueOf(this.getParam().get("domainlevel2"));
		String domain1 = this.getParam().get("domainname1");
		String domain2 = this.getParam().get("domainname2");
		
		if (this.getParam().get("accounttype2").equals("1")) {
			result = false;
		}
		else if ((level2.compareTo(level1) < 0) && (domain1.startsWith(domain2)) && (this.getParam().get("accounttype2").equals("2"))) {
			result = false;
		}
		
		return result;
	}
}
