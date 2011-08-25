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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TestCaseEngine {

	public static final Logger s_logger = Logger.getLogger(TestCaseEngine.class
			.getName());
	public static String fileName = "../metadata/adapter.xml";
	public static HashMap<String, String> globalParameters = new HashMap<String, String>();
	protected static HashMap<String, String> _componentMap = new HashMap<String, String>();
	protected static HashMap<String, ArrayList<String>> _inputFile = new HashMap<String, ArrayList<String>>();
	protected static String testCaseName = new String();
	protected static ArrayList<String> _keys = new ArrayList<String>();
	private static ThreadLocal<Object> result = new ThreadLocal<Object>();
	public static int _numThreads = 1;
	public static boolean _repeat = false;
	public static boolean _printUrl = false;
	public static String type = "All";
	public static boolean isSanity = false;
	public static boolean isRegression = false;
	private static int failure = 0;

	public static void main(String args[]) {

		// Parameters
		List<String> argsList = Arrays.asList(args);
		Iterator<String> iter = argsList.iterator();
		while (iter.hasNext()) {
			String arg = iter.next();
			// is stress?
			if (arg.equals("-t")) {
				_numThreads = Integer.parseInt(iter.next());
			}
			// do you want to print url for all commands?
			if (arg.equals("-p")) {
				_printUrl = true;
			}
			
			//type of the test: sanity, regression, all (default)
			if (arg.equals("-type")) {
				type = iter.next();
			}
			
			if (arg.equals("-repeat")) {
			    _repeat = Boolean.valueOf(iter.next());
			}
			
			if (arg.equals("-filename")) {
			    fileName = iter.next();
			}
		}
		
		if (type.equalsIgnoreCase("sanity"))
			isSanity = true;
		else if (type.equalsIgnoreCase("regression"))
			isRegression = true;

		try {
			// parse adapter.xml file to get list of tests to execute
			File file = new File(fileName);
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			doc.getDocumentElement().normalize();
			Element root = doc.getDocumentElement();

			// set global parameters
			setGlobalParams(root);

			// populate _componentMap
			setComponent(root);

			// set error to 0 by default

			// execute test
			for (int i = 0; i < _numThreads; i++) {
			    if (_numThreads > 1) {
			        s_logger.info("STARTING STRESS TEST IN "
                            + _numThreads + " THREADS");
			    } else {
			        s_logger.info("STARTING FUNCTIONAL TEST");
			    }
				new Thread(new Runnable() {
					public void run() {
						do {
							if (_numThreads == 1) {
								try {
									for (String key : _keys) {
										Class<?> c = Class.forName(_componentMap.get(key));
										TestCase component = (TestCase) c.newInstance();
										executeTest(key, c, component);
									}
								} catch (Exception ex1) {
									s_logger.error(ex1);
								} finally {
									if (failure > 0) {
										System.exit(1);
									}
								}
							} else {
								Random ran = new Random();
								Integer randomNumber = (Integer) Math.abs(ran
										.nextInt(_keys.size()));
								try {
									String key = _keys.get(randomNumber);
									Class<?> c = Class.forName(_componentMap
											.get(key));
									TestCase component = (TestCase) c
											.newInstance();
									executeTest(key, c, component);
								} catch (Exception e) {
									s_logger.error("Error in thread ", e);
								}
							}
						} while (_repeat);
					}
				}).start();
			}

		} catch (Exception exc) {
			s_logger.error(exc);
		}
	}

	public static void setGlobalParams(Element rootElement) {
		NodeList globalParam = rootElement.getElementsByTagName("globalparam");
		Element parameter = (Element) globalParam.item(0);
		NodeList paramLst = parameter.getElementsByTagName("param");

		for (int i = 0; i < paramLst.getLength(); i++) {
			Element paramElement = (Element) paramLst.item(i);

			if (paramElement.getNodeType() == Node.ELEMENT_NODE) {
				Element itemElement = (Element) paramElement;
				NodeList itemName = itemElement.getElementsByTagName("name");
				Element itemNameElement = (Element) itemName.item(0);
				NodeList itemVariable = itemElement
						.getElementsByTagName("variable");
				Element itemVariableElement = (Element) itemVariable.item(0);
				globalParameters.put(itemVariableElement.getTextContent(),
						itemNameElement.getTextContent());
			}
		}
	}

	public static void setComponent(Element rootElement) {
		NodeList testLst = rootElement.getElementsByTagName("test");
		for (int j = 0; j < testLst.getLength(); j++) {
			Element testElement = (Element) testLst.item(j);

			if (testElement.getNodeType() == Node.ELEMENT_NODE) {
				Element itemElement = (Element) testElement;

				// get test case name
				NodeList testCaseNameList = itemElement
						.getElementsByTagName("testname");
				if (testCaseNameList != null) {
					testCaseName = ((Element) testCaseNameList.item(0))
							.getTextContent();
				}
				
				if (isSanity == true && !testCaseName.equals("SANITY TEST"))
					continue;
				else if (isRegression == true && !(testCaseName.equals("SANITY TEST") || testCaseName.equals("REGRESSION TEST")))
					continue;

				// set class name
				NodeList className = itemElement.getElementsByTagName("class");
				if ((className.getLength() == 0) || (className == null)) {
					_componentMap.put(testCaseName,
							"com.cloud.test.regression.VMApiTest");
				} else {
					String name = ((Element) className.item(0))
							.getTextContent();
					_componentMap.put(testCaseName, name);
				}

				// set input file name
				NodeList inputFileNameLst = itemElement
						.getElementsByTagName("filename");
				_inputFile.put(testCaseName, new ArrayList<String>());
				for (int k = 0; k < inputFileNameLst.getLength(); k++) {
					String inputFileName = ((Element) inputFileNameLst.item(k))
							.getTextContent();
					_inputFile.get(testCaseName).add(inputFileName);
				}
			}
		}
		
		//If sanity test required, make sure that SANITY TEST componennt got loaded
		if (isSanity == true && _componentMap.size() == 0) {
			s_logger.error("FAILURE!!! Failed to load SANITY TEST component. Verify that the test is uncommented in adapter.xml");
			System.exit(1);
		}
		
		if (isRegression == true && _componentMap.size() != 2) {
			s_logger.error("FAILURE!!! Failed to load SANITY TEST or REGRESSION TEST components. Verify that these tests are uncommented in adapter.xml");
			System.exit(1);
		}

		// put all keys from _componentMap to the ArrayList
		Set<?> set = _componentMap.entrySet();
		Iterator<?> it = set.iterator();
		while (it.hasNext()) {
			Map.Entry<?, ?> me = (Map.Entry<?, ?>) it.next();
			String key = (String) me.getKey();
			_keys.add(key);
		}

	}

	public static boolean executeTest(String key, Class<?> c, TestCase component) {
		boolean finalResult = false;
		try {
			s_logger.info("Starting \"" + key + "\" test...\n\n");

			// set global parameters
			HashMap<String, String> updateParam = new HashMap<String, String>();
			updateParam.putAll(globalParameters);
			component.setParam(updateParam);

			// set DB ip address
			component.setConn(globalParameters.get("dbPassword"));

			// set commands list
			component.setCommands();

			// set input file
			if (_inputFile.get(key) != null) {
				component.setInputFile(_inputFile.get(key));
			}

			// set test case name
			if (key != null) {
				component.setTestCaseName(testCaseName);
			}

			// execute method
			result.set(component.executeTest());
			if (result.get().toString().equals("false")) {
				s_logger.error("FAILURE!!! Test \"" + key + "\" failed\n\n\n");
				failure++;
			} else {
				finalResult = true;
				s_logger.info("SUCCESS!!! Test \"" + key + "\" passed\n\n\n");
			}

		} catch (Exception ex) {
			s_logger.error("error during test execution ", ex);
		}
		return finalResult;
	}
}
