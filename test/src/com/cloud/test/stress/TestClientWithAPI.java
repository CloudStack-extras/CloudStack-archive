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

package com.cloud.test.stress;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cloud.utils.encoding.Base64;
import com.cloud.utils.exception.CloudRuntimeException;
import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;

public class TestClientWithAPI {
    private static long sleepTime = 180000L; // default 0
    private static boolean cleanUp = true;
    public static final Logger s_logger = Logger.getLogger(TestClientWithAPI.class);
    private static boolean repeat = true;
    private static int numOfUsers = 0;
    private static String[] users = null;
    private static boolean internet = false;
    private static ThreadLocal<String> _linuxIP = new ThreadLocal<String>();
    private static ThreadLocal<String> _linuxIpId = new ThreadLocal<String>();
    private static ThreadLocal<String> _linuxVmId = new ThreadLocal<String>();
    private static ThreadLocal<String> _linuxPassword = new ThreadLocal<String>();
    private static ThreadLocal<String> _windowsIP = new ThreadLocal<String>();
    private static ThreadLocal<String> _windowsIpId = new ThreadLocal<String>();
    private static ThreadLocal<String> _windowsVmId = new ThreadLocal<String>();
    private static ThreadLocal<String> _secretKey = new ThreadLocal<String>();
    private static ThreadLocal<String> _apiKey = new ThreadLocal<String>();
    private static ThreadLocal<Long> _userId = new ThreadLocal<Long>();
    private static ThreadLocal<Long> _accountId = new ThreadLocal<Long>();
    private static ThreadLocal<String> _account = new ThreadLocal<String>();
    private static ThreadLocal<String> _domainRouterId = new ThreadLocal<String>();
    private static ThreadLocal<String> _pfGroupId = new ThreadLocal<String>();
    private static ThreadLocal<String> _windowsLb = new ThreadLocal<String>();
    private static ThreadLocal<String> _linuxLb = new ThreadLocal<String>();
    private static ThreadLocal<String> _dataVolume = new ThreadLocal<String>();
    private static ThreadLocal<String> _rootVolume = new ThreadLocal<String>();
    private static ThreadLocal<String> _newVolume = new ThreadLocal<String>();
    private static ThreadLocal<String> _snapshot = new ThreadLocal<String>();
    private static ThreadLocal<String> _volumeFromSnapshot = new ThreadLocal<String>();
    private static ThreadLocal<String> _networkId = new ThreadLocal<String>();
    private static ThreadLocal<String> _publicIpId = new ThreadLocal<String>();
    private static ThreadLocal<String> _winipfwdid = new ThreadLocal<String>();
    private static ThreadLocal<String> _linipfwdid = new ThreadLocal<String>();
    private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private static int usageIterator = 1;
    private static int numThreads = 1;
    private static int wait = 5000;
    private static String accountName = null;
    private static String zoneId = "1";
    private static String snapshot_test = "no";
    private static String serviceOfferingId = "1";
    private static String diskOfferingId = "4";
    private static String networkOfferingId = "6";
    private static String vmPassword = "rs-ccb35ea5";
    private static String downloadUrl = "192.168.1.250/dump.bin";

    private static final int MAX_RETRY_LINUX = 10;
    private static final int MAX_RETRY_WIN = 10;

    public static void main(String[] args) {
        String host = "http://localhost";
        String port = "8092";
        String devPort = "8080";
        String apiUrl = "/client/api";

        try {
            // Parameters
            List<String> argsList = Arrays.asList(args);
            Iterator<String> iter = argsList.iterator();
            while (iter.hasNext()) {
                String arg = iter.next();
                // host
                if (arg.equals("-h")) {
                    host = "http://" + iter.next();
                }

                if (arg.equals("-p")) {
                    port = iter.next();
                }
                if (arg.equals("-dp")) {
                    devPort = iter.next();
                }

                if (arg.equals("-t")) {
                    numThreads = Integer.parseInt(iter.next());
                }

                if (arg.equals("-s")) {
                    sleepTime = Long.parseLong(iter.next());
                }
                if (arg.equals("-a")) {
                    accountName = iter.next();
                }

                if (arg.equals("-c")) {
                    cleanUp = Boolean.parseBoolean(iter.next());
                    if (!cleanUp)
                        sleepTime = 0L; // no need to wait if we don't ever
                    // cleanup
                }

                if (arg.equals("-r")) {
                    repeat = Boolean.parseBoolean(iter.next());
                }

                if (arg.equals("-u")) {
                    numOfUsers = Integer.parseInt(iter.next());
                }

                if (arg.equals("-i")) {
                    internet = Boolean.parseBoolean(iter.next());
                }

                if (arg.equals("-w")) {
                    wait = Integer.parseInt(iter.next());
                }

                if (arg.equals("-z")) {
                    zoneId = iter.next();
                }

                if (arg.equals("-snapshot")) {
                    snapshot_test = "yes";
                }

                if (arg.equals("-so")) {
                    serviceOfferingId = iter.next();
                }

                if (arg.equals("-do")) {
                    diskOfferingId = iter.next();
                }

                if (arg.equals("-no")) {
                    networkOfferingId = iter.next();
                }
                
                if (arg.equals("-pass")) {
                    vmPassword = iter.next();
                }

                if (arg.equals("-url")) {
                    downloadUrl = iter.next();
                }

            }

            final String server = host + ":" + port + "/";
            final String developerServer = host + ":" + devPort + apiUrl;
            s_logger.info("Starting test against server: " + server + " with " + numThreads + " thread(s)");
            if (cleanUp)
                s_logger.info("Clean up is enabled, each test will wait " + sleepTime + " ms before cleaning up");

            if (numOfUsers > 0) {
                s_logger.info("Pre-generating users for test of size : " + numOfUsers);
                users = new String[numOfUsers];
                Random ran = new Random();
                for (int i = 0; i < numOfUsers; i++) {
                    users[i] = Math.abs(ran.nextInt()) + "-user";
                }
            }

            for (int i = 0; i < numThreads; i++) {
                new Thread(new Runnable() {
                    public void run() {
                        do {
                            String username = null;
                            try {
                                long now = System.currentTimeMillis();
                                Random ran = new Random();
                                if (users != null) {
                                    username = users[Math.abs(ran.nextInt()) % numOfUsers];
                                } else {
                                    username = Math.abs(ran.nextInt()) + "-user";
                                }
                                NDC.push(username);

                                s_logger.info("Starting test for the user " + username);
                                int response = executeDeployment(server, developerServer, username, snapshot_test);
                                boolean success = false;
                                String reason = null;

                                if (response == 200) {
                                    success = true;
                                    if (internet) {
                                        s_logger.info("Deploy successful...waiting 5 minute before SSH tests");
                                        Thread.sleep(300000L); // Wait 60
                                        // seconds so
                                        // the windows VM
                                        // can boot up and do a sys prep.

                                        if (accountName == null) {
                                            s_logger.info("Begin Linux SSH test for account " + _account.get());
                                            reason = sshTest(_linuxIP.get(), _linuxPassword.get(), snapshot_test);
                                        }

                                        if (reason == null) {
                                            s_logger.info("Linux SSH test successful for account " + _account.get());
                                            s_logger.info("Begin WindowsSSH test for account " + _account.get());

                                            reason = sshTest(_linuxIP.get(), _linuxPassword.get(), snapshot_test);
                                            // reason = sshWinTest(_windowsIP.get());
                                        }

                                        // release the linux IP now...
                                        _linuxIP.set(null);
                                        // release the Windows IP now
                                        _windowsIP.set(null);
                                    }

                                    // sleep for 3 min before getting the latest network stat
                                    // s_logger.info("Sleeping for 5 min before getting the lates network stat for the account");
                                    // Thread.sleep(300000);
                                    // verify that network stat is correct for the user; if it's not - stop all the resources
                                    // for the user
                                    // if ((reason == null) && (getNetworkStat(server) == false) ) {
                                    // s_logger.error("Stopping all the resources for the account " + _account.get() +
                                    // " as network stat is incorrect");
                                    // int stopResponseCode = executeStop(
                                    // server, developerServer,
                                    // username, false);
                                    // s_logger
                                    // .info("stop command finished with response code: "
                                    // + stopResponseCode);
                                    // success = false; // since the SSH test
                                    //
                                    // } else
                                    if (reason == null) {
                                        if (internet) {
                                            s_logger.info("Windows SSH test successful for account " + _account.get());
                                        } else {
                                            s_logger.info("deploy test successful....now cleaning up");
                                            if (cleanUp) {
                                                s_logger.info("Waiting " + sleepTime + " ms before cleaning up vms");
                                                Thread.sleep(sleepTime);
                                            } else {
                                                success = true;
                                            }
                                        }

                                        if (usageIterator >= numThreads) {
                                            int eventsAndBillingResponseCode = executeEventsAndBilling(server, developerServer);
                                            s_logger.info("events and usage records command finished with response code: " + eventsAndBillingResponseCode);
                                            usageIterator = 1;

                                        } else {
                                            s_logger.info("Skipping events and usage records for this user: usageIterator " + usageIterator + " and number of Threads " + numThreads);
                                            usageIterator++;
                                        }

                                        if ((users == null) && (accountName == null)) {
                                            s_logger.info("Sending cleanup command");
                                            int cleanupResponseCode = executeCleanup(server, developerServer, username);
                                            s_logger.info("cleanup command finished with response code: " + cleanupResponseCode);
                                            success = (cleanupResponseCode == 200);
                                        } else {
                                            s_logger.info("Sending stop DomR / destroy VM command");
                                            int stopResponseCode = executeStop(server, developerServer, username, true);
                                            s_logger.info("stop(destroy) command finished with response code: " + stopResponseCode);
                                            success = (stopResponseCode == 200);
                                        }

                                    } else {
                                        // Just stop but don't destroy the
                                        // VMs/Routers
                                        s_logger.info("SSH test failed for account " + _account.get() + "with reason '" + reason + "', stopping VMs");
                                        int stopResponseCode = executeStop(server, developerServer, username, false);
                                        s_logger.info("stop command finished with response code: " + stopResponseCode);
                                        success = false; // since the SSH test
                                        // failed, mark the
                                        // whole test as
                                        // failure
                                    }
                                } else {
                                    // Just stop but don't destroy the
                                    // VMs/Routers
                                    s_logger.info("Deploy test failed with reason '" + reason + "', stopping VMs");
                                    int stopResponseCode = executeStop(server, developerServer, username, true);
                                    s_logger.info("stop command finished with response code: " + stopResponseCode);
                                    success = false; // since the deploy test
                                    // failed, mark the
                                    // whole test as failure
                                }

                                if (success) {
                                    s_logger.info("***** Completed test for user : " + username + " in " + ((System.currentTimeMillis() - now) / 1000L) + " seconds");

                                } else {
                                    s_logger.info("##### FAILED test for user : " + username + " in " + ((System.currentTimeMillis() - now) / 1000L) + " seconds with reason : " + reason);
                                }
                                s_logger.info("Sleeping for " + wait + " seconds before starting next iteration");
                                Thread.sleep(wait);
                            } catch (Exception e) {
                                s_logger.warn("Error in thread", e);
                                try {
                                    int stopResponseCode = executeStop(server, developerServer, username, true);
                                    s_logger.info("stop response code: " + stopResponseCode);
                                } catch (Exception e1) {
                                }
                            } finally {
                                NDC.clear();
                            }
                        } while (repeat);
                    }
                }).start();
            }
        } catch (Exception e) {
            s_logger.error(e);
        }
    }

    public static Map<String, List<String>> getMultipleValuesFromXML(InputStream is, String[] tagNames) {
        Map<String, List<String>> returnValues = new HashMap<String, List<String>>();
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse(is);
            Element rootElement = doc.getDocumentElement();
            for (int i = 0; i < tagNames.length; i++) {
                NodeList targetNodes = rootElement.getElementsByTagName(tagNames[i]);
                if (targetNodes.getLength() <= 0) {
                    s_logger.error("no " + tagNames[i] + " tag in XML response...returning null");
                } else {
                    List<String> valueList = new ArrayList<String>();
                    for (int j = 0; j < targetNodes.getLength(); j++) {
                        Node node = targetNodes.item(j);
                        valueList.add(node.getTextContent());
                    }
                    returnValues.put(tagNames[i], valueList);
                }
            }
        } catch (Exception ex) {
            s_logger.error(ex);
        }
        return returnValues;
    }

    public static Map<String, String> getSingleValueFromXML(InputStream is, String[] tagNames) {
        Map<String, String> returnValues = new HashMap<String, String>();
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse(is);
            Element rootElement = doc.getDocumentElement();

            for (int i = 0; i < tagNames.length; i++) {
                NodeList targetNodes = rootElement.getElementsByTagName(tagNames[i]);
                if (targetNodes.getLength() <= 0) {
                    s_logger.error("no " + tagNames[i] + " tag in XML response...returning null");
                } else {
                    returnValues.put(tagNames[i], targetNodes.item(0).getTextContent());
                }
            }
        } catch (Exception ex) {
            s_logger.error("error processing XML", ex);
        }
        return returnValues;
    }

    public static Map<String, String> getSingleValueFromXML(Element rootElement, String[] tagNames) {
        Map<String, String> returnValues = new HashMap<String, String>();
        if (rootElement == null) {
            s_logger.error("Root element is null, can't get single value from xml");
            return null;
        }
        try {
            for (int i = 0; i < tagNames.length; i++) {
                NodeList targetNodes = rootElement.getElementsByTagName(tagNames[i]);
                if (targetNodes.getLength() <= 0) {
                    s_logger.error("no " + tagNames[i] + " tag in XML response...returning null");
                } else {
                    returnValues.put(tagNames[i], targetNodes.item(0).getTextContent());
                }
            }
        } catch (Exception ex) {
            s_logger.error("error processing XML", ex);
        }
        return returnValues;
    }

    private static List<String> getNonSourceNatIPs(InputStream is) {
        List<String> returnValues = new ArrayList<String>();
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse(is);
            Element rootElement = doc.getDocumentElement();
            NodeList allocatedIpAddrNodes = rootElement.getElementsByTagName("publicipaddress");
            for (int i = 0; i < allocatedIpAddrNodes.getLength(); i++) {
                Node allocatedIpAddrNode = allocatedIpAddrNodes.item(i);
                NodeList childNodes = allocatedIpAddrNode.getChildNodes();
                String ipAddress = null;
                boolean isSourceNat = true; // assume it's source nat until we
                // find otherwise
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node n = childNodes.item(j);
                    if ("id".equals(n.getNodeName())) {
                //    if ("ipaddress".equals(n.getNodeName())) {
                        ipAddress = n.getTextContent();
                    } else if ("issourcenat".equals(n.getNodeName())) {
                        isSourceNat = Boolean.parseBoolean(n.getTextContent());
                    }
                }
                if ((ipAddress != null) && !isSourceNat) {
                    returnValues.add(ipAddress);
                }
            }
        } catch (Exception ex) {
            s_logger.error(ex);
        }
        return returnValues;
    }

    private static List<String> getIPs(InputStream is, boolean sourceNat) {
        List<String> returnValues = new ArrayList<String>();
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse(is);
            Element rootElement = doc.getDocumentElement();
            NodeList allocatedIpAddrNodes = rootElement.getElementsByTagName("publicipaddress");
            for (int i = 0; i < allocatedIpAddrNodes.getLength(); i++) {
                Node allocatedIpAddrNode = allocatedIpAddrNodes.item(i);
                NodeList childNodes = allocatedIpAddrNode.getChildNodes();
                String ipAddress = null;
                String ipAddressId = null;
                boolean isSourceNat = false; // assume it's *not* source nat until we find otherwise
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node n = childNodes.item(j);
                    //Id is being used instead of ipaddress. Changes need to done later to ipaddress variable
                    if ("id".equals(n.getNodeName())) 
                    {
                        ipAddressId = n.getTextContent();
                    }
                    else if("ipaddress".equals(n.getNodeName()))
                    {
                    	ipAddress = n.getTextContent();
                    }
                    else if ("issourcenat".equals(n.getNodeName())) {
                        isSourceNat = Boolean.parseBoolean(n.getTextContent());
                    }
                }
                if ((ipAddress != null) && isSourceNat == sourceNat) {
                    returnValues.add(ipAddressId);
                    returnValues.add(ipAddress);
                }
            }
        } catch (Exception ex) {
            s_logger.error(ex);
        }
        return returnValues;
    }

    private static String executeRegistration(String server, String username, String password) throws HttpException, IOException {
        String url = server + "?command=registerUserKeys&id=" + _userId.get().toString();
        s_logger.info("registering: " + username);
        String returnValue = null;
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(url);
        int responseCode = client.executeMethod(method);
        if (responseCode == 200) {
            InputStream is = method.getResponseBodyAsStream();
            Map<String, String> requestKeyValues = getSingleValueFromXML(is, new String[] { "apikey", "secretkey" });
            _apiKey.set(requestKeyValues.get("apikey"));
            returnValue = requestKeyValues.get("secretkey");
        } else {
            s_logger.error("registration failed with error code: " + responseCode);
        }
        return returnValue;
    }

    private static Integer executeDeployment(String server, String developerServer, String username, String snapshot_test) throws HttpException, IOException {
        // test steps:
        // - create user
        // - deploy Windows VM
        // - deploy Linux VM
        // - associate IP address
        // - create two IP forwarding rules
        // - create load balancer rule
        // - list IP forwarding rules
        // - list load balancer rules

        // -----------------------------
        // CREATE ACCOUNT
        // -----------------------------
        String encodedUsername = URLEncoder.encode(username, "UTF-8");
        String encryptedPassword = createMD5Password(username);
        String encodedPassword = URLEncoder.encode(encryptedPassword, "UTF-8");

        String url = server + "?command=createAccount&username=" + encodedUsername + "&account=" + encodedUsername + "&password=" + encodedPassword + "&firstname=Test&lastname=Test&email=test@vmops.com&domainId=1&accounttype=0";

        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(url);
        int responseCode = client.executeMethod(method);
        long accountId = -1;
        if (responseCode == 200) {
            InputStream is = method.getResponseBodyAsStream();
            Map<String, String> accountValues = getSingleValueFromXML(is, new String[] { "id", "name" });
            String accountIdStr = accountValues.get("id");
            s_logger.info("created account " + username + " with id " + accountIdStr);
            if (accountIdStr != null) {
                accountId = Long.parseLong(accountIdStr);
                _accountId.set(accountId);
                _account.set(accountValues.get("name"));
                if (accountId == -1) {
                    s_logger.error("create account (" + username + ") failed to retrieve a valid user id, aborting depolyment test");
                    return -1;
                }
            }
        } else {
            s_logger.error("create account test failed for account " + username + " with error code :" + responseCode + ", aborting deployment test. The command was sent with url " + url);
            return -1;
        }

        // LIST JUST CREATED USER TO GET THE USER ID
        url = server + "?command=listUsers&username=" + encodedUsername + "&account=" + encodedUsername + "&domainId=1";
        client = new HttpClient();
        method = new GetMethod(url);
        responseCode = client.executeMethod(method);
        long userId = -1;
        if (responseCode == 200) {
            InputStream is = method.getResponseBodyAsStream();
            Map<String, String> userIdValues = getSingleValueFromXML(is, new String[] { "id" });
            String userIdStr = userIdValues.get("id");
            s_logger.info("listed user " + username + " with id " + userIdStr);
            if (userIdStr != null) {
                userId = Long.parseLong(userIdStr);
                _userId.set(userId);
                if (userId == -1) {
                    s_logger.error("list user by username " + username + ") failed to retrieve a valid user id, aborting depolyment test");
                    return -1;
                }
            }
        } else {
            s_logger.error("list user test failed for account " + username + " with error code :" + responseCode + ", aborting deployment test. The command was sent with url " + url);
            return -1;
        }

        _secretKey.set(executeRegistration(server, username, username));

        if (_secretKey.get() == null) {
            s_logger.error("FAILED to retrieve secret key during registration, skipping user: " + username);
            return -1;
        } else {
            s_logger.info("got secret key: " + _secretKey.get());
            s_logger.info("got api key: " + _apiKey.get());
        }

        // ---------------------------------
        // CREATE VIRTUAL NETWORK
        // ---------------------------------
        url = server + "?command=createNetwork&networkofferingid=" + networkOfferingId + "&account=" + encodedUsername + "&domainId=1" + "&zoneId=" + zoneId + "&name=virtualnetwork-" + encodedUsername + "&displaytext=virtualnetwork-" + encodedUsername;
        client = new HttpClient();
        method = new GetMethod(url);
        responseCode = client.executeMethod(method);
        if (responseCode == 200) {
            InputStream is = method.getResponseBodyAsStream();
            Map<String, String> networkValues = getSingleValueFromXML(is, new String[] { "id" });
            String networkIdStr = networkValues.get("id");
            s_logger.info("Created virtual network with name virtualnetwork-" + encodedUsername + " and id " + networkIdStr);
            if (networkIdStr != null) {
                _networkId.set(networkIdStr);
            }
        } else {
            s_logger.error("Create virtual network failed for account " + username + " with error code :" + responseCode + ", aborting deployment test. The command was sent with url " + url);
            return -1;
        }
/*
        // ---------------------------------
        // CREATE DIRECT NETWORK
        // ---------------------------------
        url = server + "?command=createNetwork&networkofferingid=" + networkOfferingId_dir + "&account=" + encodedUsername + "&domainId=1" + "&zoneId=" + zoneId + "&name=directnetwork-" + encodedUsername + "&displaytext=directnetwork-" + encodedUsername;
        client = new HttpClient();
        method = new GetMethod(url);
        responseCode = client.executeMethod(method);
        if (responseCode == 200) {
            InputStream is = method.getResponseBodyAsStream();
            Map<String, String> networkValues = getSingleValueFromXML(is, new String[] { "id" });
            String networkIdStr = networkValues.get("id");
            s_logger.info("Created direct network with name directnetwork-" + encodedUsername + " and id " + networkIdStr);
            if (networkIdStr != null) {
                _networkId_dir.set(networkIdStr);
            }
        } else {
            s_logger.error("Create direct network failed for account " + username + " with error code :" + responseCode + ", aborting deployment test. The command was sent with url " + url);
            return -1;
        }
*/
        
        
        // ---------------------------------
        // DEPLOY LINUX VM
        // ---------------------------------
        String linuxVMPrivateIP = null;
        {
            // long templateId = 3;
            long templateId = 4;
            String encodedZoneId = URLEncoder.encode("" + zoneId, "UTF-8");
            String encodedServiceOfferingId = URLEncoder.encode("" + serviceOfferingId, "UTF-8");
            String encodedTemplateId = URLEncoder.encode("" + templateId, "UTF-8");
            String encodedApiKey = URLEncoder.encode(_apiKey.get(), "UTF-8");
            String encodedNetworkIds = URLEncoder.encode(_networkId.get()+",206","UTF-8");
            String requestToSign = "apikey=" + encodedApiKey + "&command=deployVirtualMachine&diskofferingid=" + diskOfferingId + "&networkids=" + encodedNetworkIds + "&serviceofferingid=" + encodedServiceOfferingId + "&templateid=" + encodedTemplateId
                    + "&zoneid=" + encodedZoneId;
            requestToSign = requestToSign.toLowerCase();
            String signature = signRequest(requestToSign, _secretKey.get());
            String encodedSignature = URLEncoder.encode(signature, "UTF-8");
            url = developerServer + "?command=deployVirtualMachine" + "&zoneid=" + encodedZoneId + "&serviceofferingid=" + encodedServiceOfferingId + "&diskofferingid=" + diskOfferingId + "&networkids=" + encodedNetworkIds + "&templateid=" + encodedTemplateId
                    + "&apikey=" + encodedApiKey + "&signature=" + encodedSignature;

            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            if (responseCode == 200) {
                InputStream input = method.getResponseBodyAsStream();
                Element el = queryAsyncJobResult(server, input);
                Map<String, String> values = getSingleValueFromXML(el, new String[] { "id", "ipaddress" });

                if ((values.get("ipaddress") == null) || (values.get("id") == null)) {
                    s_logger.info("deploy linux vm response code: 401, the command was sent with url " + url);
                    return 401;
                } else {
                    s_logger.info("deploy linux vm response code: " + responseCode);
                    long linuxVMId = Long.parseLong(values.get("id"));
                    s_logger.info("got linux virtual machine id: " + linuxVMId);
                    _linuxVmId.set(values.get("id"));
                    linuxVMPrivateIP = values.get("ipaddress");
                    // _linuxPassword.set(values.get("password"));
                    _linuxPassword.set(vmPassword);
                    s_logger.info("got linux virtual machine password: " + _linuxPassword.get());
                }
            } else {
                s_logger.error("deploy linux vm failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
        }

        {
            // ---------------------------------
            // ASSOCIATE IP for windows
            // ---------------------------------
            String ipAddr = null;

            String encodedApiKey = URLEncoder.encode(_apiKey.get(), "UTF-8");
            String requestToSign = "apikey=" + encodedApiKey + "&command=associateIpAddress" + "&zoneid=" + zoneId;
            requestToSign = requestToSign.toLowerCase();
            String signature = signRequest(requestToSign, _secretKey.get());
            String encodedSignature = URLEncoder.encode(signature, "UTF-8");

            url = developerServer + "?command=associateIpAddress" + "&apikey=" + encodedApiKey + "&zoneid=" + zoneId + "&signature=" + encodedSignature;

            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                /*Asynchronous Job - Corresponding Changes Made*/
                Element associpel = queryAsyncJobResult(server, is);
                Map<String, String> values = getSingleValueFromXML(associpel, new String[] {"id", "ipaddress" });

                if ((values.get("ipaddress") == null)|| (values.get("id") == null)) {
                    s_logger.info("associate ip for Windows response code: 401, the command was sent with url " + url);
                    return 401;
                } 
                else
                {
                	s_logger.info("Associate IP Address response code: " + responseCode);
                    long publicIpId = Long.parseLong(values.get("id"));
                    s_logger.info("Associate IP's Id: " + publicIpId);
                    _publicIpId.set(values.get("id"));         
                }	
            } else {
                s_logger.error("associate ip address for windows vm failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
            
            String encodedPublicIpId = URLEncoder.encode(_publicIpId.get(), "UTF-8");
            requestToSign = "apikey=" + encodedApiKey + "&command=listPublicIpAddresses"+"&id="+ encodedPublicIpId;
            requestToSign = requestToSign.toLowerCase();
            signature = signRequest(requestToSign, _secretKey.get());
            encodedSignature = URLEncoder.encode(signature, "UTF-8");
            
            url = developerServer + "?command=listPublicIpAddresses&apikey=" + encodedApiKey + "&id=" + encodedPublicIpId + "&signature=" + encodedSignature;
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("url is " + url);
            s_logger.info("list ip addresses for user " + userId + " response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
         //       InputStream ips = method.getResponseBodyAsStream();
                List<String> ipAddressValues = getIPs(is, false);
         //       List<String> ipAddressVals = getIPs(is, false, true);
                if ((ipAddressValues != null) && !ipAddressValues.isEmpty()) {
                    _windowsIpId.set(ipAddressValues.get(0));
                    _windowsIP.set(ipAddressValues.get(1));
                    s_logger.info("For Windows, using non-sourceNat IP address ID: " + ipAddressValues.get(0));
                    s_logger.info("For Windows, using non-sourceNat IP address: " + ipAddressValues.get(1));
                }
            } else {
                s_logger.error("list ip addresses failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
            
            // ---------------------------------
            // Use the SourceNat IP for linux
            // ---------------------------------
            {
                requestToSign = "apikey=" + encodedApiKey + "&command=listPublicIpAddresses";
                requestToSign = requestToSign.toLowerCase();
                signature = signRequest(requestToSign, _secretKey.get());
                encodedSignature = URLEncoder.encode(signature, "UTF-8");

                url = developerServer + "?command=listPublicIpAddresses&apikey=" + encodedApiKey + "&signature=" + encodedSignature;
                client = new HttpClient();
                method = new GetMethod(url);
                responseCode = client.executeMethod(method);
                s_logger.info("url is " + url);
                s_logger.info("list ip addresses for user " + userId + " response code: " + responseCode);
                if (responseCode == 200) {
                    InputStream is = method.getResponseBodyAsStream();
//                  InputStream ips = method.getResponseBodyAsStream();
                    List<String> ipAddressValues = getIPs(is, true);
//                    is = method.getResponseBodyAsStream();
//                    List<String> ipAddressVals = getIPs(is, true, true);
                    if ((ipAddressValues != null) && !ipAddressValues.isEmpty()) {
                        _linuxIpId.set(ipAddressValues.get(0));
                        _linuxIP.set(ipAddressValues.get(1));
                        s_logger.info("For linux, using sourceNat IP address ID: " + ipAddressValues.get(0));
                        s_logger.info("For linux, using sourceNat IP address: " + ipAddressValues.get(1));
                    }
                } else {
                    s_logger.error("list ip addresses failed with error code: " + responseCode + ". Following URL was sent: " + url);
                    return responseCode;
                }
            }
            
            //--------------------------------------------
            // Enable Static NAT for the Source NAT Ip
            //--------------------------------------------
            String encodedSourceNatPublicIpId = URLEncoder.encode(_linuxIpId.get(), "UTF-8");
                   
  /*          requestToSign = "apikey=" + encodedApiKey + "&command=enableStaticNat"+"&id=" + encodedSourceNatPublicIpId + "&virtualMachineId=" + encodedVmId;;
            requestToSign = requestToSign.toLowerCase();
            signature = signRequest(requestToSign, _secretKey.get());
            encodedSignature = URLEncoder.encode(signature, "UTF-8");
            
            url = developerServer + "?command=enableStaticNat&apikey=" + encodedApiKey + "&signature=" + encodedSignature + "&id=" + encodedSourceNatPublicIpId + "&virtualMachineId=" + encodedVmId;
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("url is " + url);
            s_logger.info("list ip addresses for user " + userId + " response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                Map<String, String> success = getSingleValueFromXML(is, new String[] { "success" });
                s_logger.info("Enable Static NAT..success? " + success.get("success"));
            } else {
                s_logger.error("Enable Static NAT failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
  */          
            // -------------------------------------------------------------
            // CREATE IP FORWARDING RULE -- Linux VM
            // -------------------------------------------------------------
            String encodedVmId = URLEncoder.encode(_linuxVmId.get(), "UTF-8");
            String encodedIpAddress = URLEncoder.encode(_linuxIpId.get(), "UTF-8");
            requestToSign = "apikey=" + encodedApiKey + "&command=createPortForwardingRule&ipaddressid=" + encodedIpAddress + "&privateport=22&protocol=TCP&publicport=22" + "&virtualmachineid=" + encodedVmId ;
            requestToSign = requestToSign.toLowerCase();
            signature = signRequest(requestToSign, _secretKey.get());
            encodedSignature = URLEncoder.encode(signature, "UTF-8");

            url = developerServer + "?command=createPortForwardingRule&apikey=" + encodedApiKey + "&ipaddressid=" + encodedIpAddress + "&privateport=22&protocol=TCP&publicport=22&virtualmachineid=" + encodedVmId + "&signature=" + encodedSignature;

            s_logger.info("Created port forwarding rule with " + url);
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            if (responseCode == 200) {
                InputStream input = method.getResponseBodyAsStream();
                Element el = queryAsyncJobResult(server, input);
                Map<String, String> values = getSingleValueFromXML(el, new String[] {"id"});
                s_logger.info("Port forwarding rule was assigned successfully to Linux VM");
                long ipfwdid = Long.parseLong(values.get("id"));
                s_logger.info("got Port Forwarding Rule's Id:" + ipfwdid);
                _linipfwdid.set(values.get("id"));
                
            } else {
                s_logger.error("Port forwarding rule creation failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }

            // Create snapshot recurring policy if needed; otherwise create windows vm
            if (snapshot_test.equals("yes")) {

                // list volumes for linux vm
                {
                    url = server + "?command=listVolumes&virtualMachineId=" + _linuxVmId.get() + "&type=root";
                    s_logger.info("Getting rootDisk id of Centos vm");
                    client = new HttpClient();
                    method = new GetMethod(url);
                    responseCode = client.executeMethod(method);
                    s_logger.info("List volumes response code: " + responseCode);
                    if (responseCode == 200) {
                        InputStream is = method.getResponseBodyAsStream();
                        Map<String, String> success = getSingleValueFromXML(is, new String[] { "id" });
                        if (success.get("id") == null) {
                            s_logger.error("Unable to get root volume for linux vm. Followin url was sent: " + url);
                        }
                        s_logger.info("Got rootVolume for linux vm with id " + success.get("id"));
                        _rootVolume.set(success.get("id"));
                    } else {
                        s_logger.error("List volumes for linux vm failed with error code: " + responseCode + ". Following URL was sent: " + url);
                        return responseCode;
                    }
                }
                // Create recurring snapshot policy for linux vm
                {
                    String encodedTimeZone = URLEncoder.encode("America/Los Angeles", "UTF-8");
                    url = server + "?command=createSnapshotPolicy&intervaltype=hourly&schedule=10&maxsnaps=4&volumeid=" + _rootVolume.get() + "&timezone=" + encodedTimeZone;
                    s_logger.info("Creating recurring snapshot policy for linux vm ROOT disk");
                    client = new HttpClient();
                    method = new GetMethod(url);
                    responseCode = client.executeMethod(method);
                    s_logger.info("Create recurring snapshot policy for linux vm ROOT disk: " + responseCode);
                    if (responseCode != 200) {
                        s_logger.error("Create recurring snapshot policy for linux vm ROOT disk failed with error code: " + responseCode + ". Following URL was sent: " + url);
                        return responseCode;
                    }
                }
            } else {
                // ---------------------------------
                // DEPLOY WINDOWS VM
                // ---------------------------------
                String windowsVMPrivateIP = null;
                {
                    // long templateId = 6;
                    long templateId = 4;
                    String encodedZoneId = URLEncoder.encode("" + zoneId, "UTF-8");
                    String encodedServiceOfferingId = URLEncoder.encode("" + serviceOfferingId, "UTF-8");
                    String encodedTemplateId = URLEncoder.encode("" + templateId, "UTF-8");
                    encodedApiKey = URLEncoder.encode(_apiKey.get(), "UTF-8");
                    String encodedNetworkIds = URLEncoder.encode(_networkId.get()+",206","UTF-8");
                    
                    requestToSign = "apikey=" + encodedApiKey + "&command=deployVirtualMachine&diskofferingid=" + diskOfferingId + "&networkids=" + encodedNetworkIds + "&serviceofferingid=" + encodedServiceOfferingId + "&templateid=" + encodedTemplateId
                            + "&zoneid=" + encodedZoneId;
                    requestToSign = requestToSign.toLowerCase();
                    signature = signRequest(requestToSign, _secretKey.get());
                    encodedSignature = URLEncoder.encode(signature, "UTF-8");

                    url = developerServer + "?command=deployVirtualMachine" + "&zoneid=" + encodedZoneId + "&serviceofferingid=" + encodedServiceOfferingId + "&diskofferingid=" + diskOfferingId + "&networkids=" + encodedNetworkIds + "&templateid="
                            + encodedTemplateId + "&apikey=" + encodedApiKey + "&signature=" + encodedSignature;

                    method = new GetMethod(url);
                    responseCode = client.executeMethod(method);
                    if (responseCode == 200) {
                        InputStream input = method.getResponseBodyAsStream();
                        Element el = queryAsyncJobResult(server, input);
                        Map<String, String> values = getSingleValueFromXML(el, new String[] { "id", "ipaddress" });

                        if ((values.get("ipaddress") == null) || (values.get("id") == null)) {
                            s_logger.info("deploy windows vm response code: 401, the command was sent with url " + url);
                            return 401;
                        } else {
                            s_logger.info("deploy windows vm response code: " + responseCode);
                            windowsVMPrivateIP = values.get("ipaddress");
                            long windowsVMId = Long.parseLong(values.get("id"));
                            s_logger.info("got windows virtual machine id: " + windowsVMId);
                            _windowsVmId.set(values.get("id"));
                        }
                    } else {
                        s_logger.error("deploy windows vm failes with error code: " + responseCode + ". Following URL was sent: " + url);
                        return responseCode;
                    }
                }

                //--------------------------------------------
                // Enable Static NAT for the Non Source NAT Ip
                //--------------------------------------------
                
                encodedVmId = URLEncoder.encode(_windowsVmId.get(), "UTF-8");
                encodedPublicIpId = URLEncoder.encode(_publicIpId.get(), "UTF-8");
                requestToSign = "apikey=" + encodedApiKey + "&command=enableStaticNat"+"&ipaddressid="+ encodedPublicIpId + "&virtualMachineId=" + encodedVmId;
                requestToSign = requestToSign.toLowerCase();
                signature = signRequest(requestToSign, _secretKey.get());
                encodedSignature = URLEncoder.encode(signature, "UTF-8");
                
                url = developerServer + "?command=enableStaticNat&apikey=" + encodedApiKey + "&ipaddressid=" + encodedPublicIpId + "&signature=" + encodedSignature + "&virtualMachineId=" + encodedVmId;
                client = new HttpClient();
                method = new GetMethod(url);
                responseCode = client.executeMethod(method);
                s_logger.info("url is " + url);
                s_logger.info("list ip addresses for user " + userId + " response code: " + responseCode);
                if (responseCode == 200) {
                    InputStream is = method.getResponseBodyAsStream();
                    Map<String, String> success = getSingleValueFromXML(is, new String[] { "success" });
                    s_logger.info("Enable Static NAT..success? " + success.get("success"));
                } else {
                    s_logger.error("Enable Static NAT failed with error code: " + responseCode + ". Following URL was sent: " + url);
                    return responseCode;
                }                                            

                
                // -------------------------------------------------------------
                // CREATE IP FORWARDING RULE -- Windows VM
                // -------------------------------------------------------------

                // create port forwarding rule for window vm
                encodedIpAddress = URLEncoder.encode(_windowsIpId.get(), "UTF-8");
                //encodedVmId = URLEncoder.encode(_windowsVmId.get(), "UTF-8");

                requestToSign = "apikey=" + encodedApiKey + "&command=createIpForwardingRule&endPort=22&ipaddressid=" + encodedIpAddress + "&protocol=TCP&startPort=22";
                requestToSign = requestToSign.toLowerCase();
                signature = signRequest(requestToSign, _secretKey.get());
                encodedSignature = URLEncoder.encode(signature, "UTF-8");

                url = developerServer + "?command=createIpForwardingRule&apikey=" + encodedApiKey + "&endPort=22&ipaddressid=" + encodedIpAddress + "&protocol=TCP&signature=" + encodedSignature + "&startPort=22";

                s_logger.info("Created Ip forwarding rule with " + url);
                method = new GetMethod(url);
                responseCode = client.executeMethod(method);
                if (responseCode == 200) {
                    InputStream input = method.getResponseBodyAsStream();
                    Element el = queryAsyncJobResult(server, input);
                    Map<String, String> values = getSingleValueFromXML(el, new String[] {"id"});
                    s_logger.info("Port forwarding rule was assigned successfully to Windows VM");
                    long ipfwdid = Long.parseLong(values.get("id"));
                    s_logger.info("got Ip Forwarding Rule's Id:" + ipfwdid);
                    _winipfwdid.set(values.get("id"));
                } else {
                    s_logger.error("Port forwarding rule creation failed with error code: " + responseCode + ". Following URL was sent: " + url);
                    return responseCode;
                }
           }
        }
        return responseCode;
    }

    private static int executeCleanup(String server, String developerServer, String username) throws HttpException, IOException {
        // test steps:
        // - get user
        // - delete user

        // -----------------------------
        // GET USER
        // -----------------------------
        String userId = _userId.get().toString();
        String encodedUserId = URLEncoder.encode(userId, "UTF-8");
        String url = server + "?command=listUsers&id=" + encodedUserId;
        s_logger.info("Cleaning up resources for user: " + userId + " with url " + url);
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(url);
        int responseCode = client.executeMethod(method);
        s_logger.info("get user response code: " + responseCode);
        if (responseCode == 200) {
            InputStream is = method.getResponseBodyAsStream();
            Map<String, String> userInfo = getSingleValueFromXML(is, new String[] { "username", "id", "account" });
            if (!username.equals(userInfo.get("username"))) {
                s_logger.error("get user failed to retrieve requested user, aborting cleanup test" + ". Following URL was sent: " + url);
                return -1;
            }

        } else {
            s_logger.error("get user failed with error code: " + responseCode + ". Following URL was sent: " + url);
            return responseCode;
        }

        // -----------------------------
        // UPDATE USER
        // -----------------------------
        {
            url = server + "?command=updateUser&id=" + userId + "&firstname=delete&lastname=me";
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("update user response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                Map<String, String> success = getSingleValueFromXML(is, new String[] { "success" });
                s_logger.info("update user..success? " + success.get("success"));
            } else {
                s_logger.error("update user failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
        }

        // -----------------------------
        // Detach existin dataVolume, create a new volume, attach it to the vm
        // -----------------------------
        {
            url = server + "?command=listVolumes&virtualMachineId=" + _linuxVmId.get() + "&type=dataDisk";
            s_logger.info("Getting dataDisk id of Centos vm");
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("List volumes response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                Map<String, String> success = getSingleValueFromXML(is, new String[] { "id" });
                s_logger.info("Got dataDiskVolume with id " + success.get("id"));
                _dataVolume.set(success.get("id"));
            } else {
                s_logger.error("List volumes failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
        }

        // Detach volume
        {
            url = server + "?command=detachVolume&id=" + _dataVolume.get();
            s_logger.info("Detaching volume with id " + _dataVolume.get());
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("Detach data volume response code: " + responseCode);
            if (responseCode == 200) {
                InputStream input = method.getResponseBodyAsStream();
                Element el = queryAsyncJobResult(server, input);
                s_logger.info("The volume was detached successfully");
            } else {
                s_logger.error("Detach data disk failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
        }

        // Delete a volume
        {
            url = server + "?command=deleteVolume&id=" + _dataVolume.get();
            s_logger.info("Deleting volume with id " + _dataVolume.get());
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("Delete data volume response code: " + responseCode);
            if (responseCode == 200) {
                s_logger.info("The volume was deleted successfully");
            } else {
                s_logger.error("Delete volume failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
        }

        // Create a new volume
        {
            url = server + "?command=createVolume&diskofferingid=" + diskOfferingId + "&zoneid=" + zoneId + "&name=newvolume&account=" + _account.get() + "&domainid=1";
            s_logger.info("Creating volume....");
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            if (responseCode == 200) {
                InputStream input = method.getResponseBodyAsStream();
                Element el = queryAsyncJobResult(server, input);
                Map<String, String> values = getSingleValueFromXML(el, new String[] { "id" });

                if (values.get("id") == null) {
                    s_logger.info("create volume response code: 401");
                    return 401;
                } else {
                    s_logger.info("create volume response code: " + responseCode);
                    long volumeId = Long.parseLong(values.get("id"));
                    s_logger.info("got volume id: " + volumeId);
                    _newVolume.set(values.get("id"));
                }
            } else {
                s_logger.error("create volume failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
        }

        // attach a new volume to the vm
        {
            url = server + "?command=attachVolume&id=" + _newVolume.get() + "&virtualmachineid=" + _linuxVmId.get();
            s_logger.info("Attaching volume with id " + _newVolume.get() + " to the vm " + _linuxVmId.get());
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("Attach data volume response code: " + responseCode);
            if (responseCode == 200) {
                InputStream input = method.getResponseBodyAsStream();
                Element el = queryAsyncJobResult(server, input);
                s_logger.info("The volume was attached successfully");
            } else {
                s_logger.error("Attach volume failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
        }

        // Create a snapshot
        // list volumes
        {
            url = server + "?command=listVolumes&virtualMachineId=" + _linuxVmId.get() + "&type=root";
            s_logger.info("Getting rootDisk id of Centos vm");
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("List volumes response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                Map<String, String> success = getSingleValueFromXML(is, new String[] { "id" });
                if (success.get("id") == null) {
                    s_logger.error("Unable to get root volume. Followin url was sent: " + url);
                }
                s_logger.info("Got rootVolume with id " + success.get("id"));
                _rootVolume.set(success.get("id"));
            } else {
                s_logger.error("List volumes failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
        }

        // //Create snapshot from root disk volume
        String encodedApiKey = URLEncoder.encode(_apiKey.get(), "UTF-8");
        String requestToSign = "apikey=" + encodedApiKey + "&command=createSnapshot&volumeid=" + _rootVolume.get();
        requestToSign = requestToSign.toLowerCase();
        String signature = signRequest(requestToSign, _secretKey.get());
        String encodedSignature = URLEncoder.encode(signature, "UTF-8");

        url = developerServer + "?command=createSnapshot&volumeid=" + _rootVolume.get() + "&apikey=" + encodedApiKey + "&signature=" + encodedSignature;
        client = new HttpClient();
        method = new GetMethod(url);
        responseCode = client.executeMethod(method);
        s_logger.info("Create snapshot response code: " + responseCode);
        if (responseCode == 200) {
            InputStream input = method.getResponseBodyAsStream();
            Element el = queryAsyncJobResult(server, input);
            Map<String, String> values = getSingleValueFromXML(el, new String[] { "id" });

            if (values.get("id") == null) {
                s_logger.info("create snapshot response code: 401");
                return 401;
            } else {
                s_logger.info("create snapshot response code: " + responseCode + ". Got snapshot with id " + values.get("id"));
                _snapshot.set(values.get("id"));
            }
        } else {
            s_logger.error("create snapshot failed with error code: " + responseCode + ". Following URL was sent: " + url);
            return responseCode;
        }

        // Create volume from the snapshot created on the previous step and attach it to the running vm
  /*      encodedApiKey = URLEncoder.encode(_apiKey.get(), "UTF-8");
        requestToSign = "apikey=" + encodedApiKey + "&command=createVolume&name=" + _account.get() + "&snapshotid=" + _snapshot.get();
        requestToSign = requestToSign.toLowerCase();
        signature = signRequest(requestToSign, _secretKey.get());
        encodedSignature = URLEncoder.encode(signature, "UTF-8");

        url = developerServer + "?command=createVolume&name=" + _account.get() + "&snapshotid=" + _snapshot.get() + "&apikey=" + encodedApiKey + "&signature=" + encodedSignature;
        client = new HttpClient();
        method = new GetMethod(url);
        responseCode = client.executeMethod(method);
        s_logger.info("Create volume from snapshot response code: " + responseCode);
        if (responseCode == 200) {
            InputStream input = method.getResponseBodyAsStream();
            Element el = queryAsyncJobResult(server, input);
            Map<String, String> values = getSingleValueFromXML(el, new String[] { "id" });

            if (values.get("id") == null) {
                s_logger.info("create volume from snapshot response code: 401");
                return 401;
            } else {
                s_logger.info("create volume from snapshot response code: " + responseCode + ". Got volume with id " + values.get("id") + ". The command was sent with url " + url);
                _volumeFromSnapshot.set(values.get("id"));
            }
        } else {
            s_logger.error("create volume from snapshot failed with error code: " + responseCode + ". Following URL was sent: " + url);
            return responseCode;
        }

        {
            url = server + "?command=attachVolume&id=" + _volumeFromSnapshot.get() + "&virtualmachineid=" + _linuxVmId.get();
            s_logger.info("Attaching volume with id " + _volumeFromSnapshot.get() + " to the vm " + _linuxVmId.get());
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("Attach volume from snapshot to linux vm response code: " + responseCode);
            if (responseCode == 200) {
                InputStream input = method.getResponseBodyAsStream();
                Element el = queryAsyncJobResult(server, input);
                s_logger.info("The volume created from snapshot was attached successfully to linux vm");
            } else {
                s_logger.error("Attach volume created from snapshot failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
        }
*/
        // -----------------------------
        // Execute reboot/stop/start commands for the VMs before deleting the account - made to exercise xen
        // -----------------------------

        // Reboot windows VM
        requestToSign = "apikey=" + encodedApiKey + "&command=rebootVirtualMachine&id=" + _windowsVmId.get();
        requestToSign = requestToSign.toLowerCase();
        signature = signRequest(requestToSign, _secretKey.get());
        encodedSignature = URLEncoder.encode(signature, "UTF-8");

        url = developerServer + "?command=rebootVirtualMachine&id=" + _windowsVmId.get() + "&apikey=" + encodedApiKey + "&signature=" + encodedSignature;
        client = new HttpClient();
        method = new GetMethod(url);
        responseCode = client.executeMethod(method);
        s_logger.info("Reboot windows Vm response code: " + responseCode);
        if (responseCode == 200) {
            InputStream input = method.getResponseBodyAsStream();
            Element el = queryAsyncJobResult(server, input);
            Map<String, String> success = getSingleValueFromXML(el, new String[] { "success" });
            s_logger.info("Windows VM was rebooted with the status: " + success.get("success"));
        } else {
            s_logger.error("Reboot windows VM test failed with error code: " + responseCode + ". Following URL was sent: " + url);
            return responseCode;
        }

        // Stop centos VM
        requestToSign = "apikey=" + encodedApiKey + "&command=stopVirtualMachine&id=" + _linuxVmId.get();
        requestToSign = requestToSign.toLowerCase();
        signature = signRequest(requestToSign, _secretKey.get());
        encodedSignature = URLEncoder.encode(signature, "UTF-8");

        url = developerServer + "?command=stopVirtualMachine&id=" + _linuxVmId.get() + "&apikey=" + encodedApiKey + "&signature=" + encodedSignature;
        client = new HttpClient();
        method = new GetMethod(url);
        responseCode = client.executeMethod(method);
        s_logger.info("Stop linux Vm response code: " + responseCode);
        if (responseCode == 200) {
            InputStream input = method.getResponseBodyAsStream();
            Element el = queryAsyncJobResult(server, input);
            Map<String, String> success = getSingleValueFromXML(el, new String[] { "success" });
            s_logger.info("Linux VM was stopped with the status: " + success.get("success"));
        } else {
            s_logger.error("Stop linux VM test failed with error code: " + responseCode + ". Following URL was sent: " + url);
            return responseCode;
        }

        // Create private template from root disk volume
        requestToSign = "apikey=" + encodedApiKey + "&command=createTemplate" + "&displaytext=" + _account.get() + "&name=" + _account.get() + "&ostypeid=11" + "&snapshotid=" + _snapshot.get();
        requestToSign = requestToSign.toLowerCase();
        signature = signRequest(requestToSign, _secretKey.get());
        encodedSignature = URLEncoder.encode(signature, "UTF-8");

        url = developerServer + "?command=createTemplate" + "&displaytext=" + _account.get() + "&name=" + _account.get() + "&ostypeid=11" + "&snapshotid=" + _snapshot.get() + "&apikey=" + encodedApiKey + "&signature=" + encodedSignature;
        client = new HttpClient();
        method = new GetMethod(url);
        responseCode = client.executeMethod(method);
        s_logger.info("Create private template response code: " + responseCode);
        if (responseCode == 200) {
            InputStream input = method.getResponseBodyAsStream();
            Element el = queryAsyncJobResult(server, input);
            Map<String, String> values = getSingleValueFromXML(el, new String[] { "id" });

            if (values.get("id") == null) {
                s_logger.info("create private template response code: 401");
                return 401;
            } else {
                s_logger.info("create private template response code: " + responseCode);
            }
        } else {
            s_logger.error("create private template failed with error code: " + responseCode + ". Following URL was sent: " + url);
            return responseCode;
        }

        // Start centos VM
        requestToSign = "apikey=" + encodedApiKey + "&command=startVirtualMachine&id=" + _windowsVmId.get();
        requestToSign = requestToSign.toLowerCase();
        signature = signRequest(requestToSign, _secretKey.get());
        encodedSignature = URLEncoder.encode(signature, "UTF-8");

        url = developerServer + "?command=startVirtualMachine&id=" + _windowsVmId.get() + "&apikey=" + encodedApiKey + "&signature=" + encodedSignature;
        client = new HttpClient();
        method = new GetMethod(url);
        responseCode = client.executeMethod(method);
        s_logger.info("Start linux Vm response code: " + responseCode);
        if (responseCode != 200) {
            s_logger.error("Start linux VM test failed with error code: " + responseCode + ". Following URL was sent: " + url);
            return responseCode;
        }

        // get domainRouter id
        {
            url = server + "?command=listRouters&zoneid=" + zoneId + "&account=" + _account.get() + "&domainid=1";
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("List domain routers response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                Map<String, String> success = getSingleValueFromXML(is, new String[] { "id" });
                s_logger.info("Got the domR with id " + success.get("id"));
                _domainRouterId.set(success.get("id"));
            } else {
                s_logger.error("List domain routers failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
        }

        // reboot the domain router
        {
            url = server + "?command=rebootRouter&id=" + _domainRouterId.get();
            s_logger.info("Rebooting domR with id " + _domainRouterId.get());
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("Reboot domain router response code: " + responseCode);
            if (responseCode == 200) {
                InputStream input = method.getResponseBodyAsStream();
                Element el = queryAsyncJobResult(server, input);
                s_logger.info("Domain router was rebooted successfully");
            } else {
                s_logger.error("Reboot domain routers failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
        }

        // -----------------------------
        // DELETE ACCOUNT
        // -----------------------------
        {
            url = server + "?command=deleteAccount&id=" + _accountId.get();
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("delete account response code: " + responseCode);
            if (responseCode == 200) {
                InputStream input = method.getResponseBodyAsStream();
                Element el = queryAsyncJobResult(server, input);
                s_logger.info("Deleted account successfully");
            } else {
                s_logger.error("delete account failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
        }
        return responseCode;
    }

    private static int executeEventsAndBilling(String server, String developerServer) throws HttpException, IOException {
        // test steps:
        // - get all the events in the system for all users in the system
        // - generate all the usage records in the system
        // - get all the usage records in the system

        // -----------------------------
        // GET EVENTS
        // -----------------------------
        String url = server + "?command=listEvents&page=1&pagesize=100&&account=" + _account.get();

        s_logger.info("Getting events for the account " + _account.get());
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(url);
        int responseCode = client.executeMethod(method);
        s_logger.info("get events response code: " + responseCode);
        if (responseCode == 200) {
            InputStream is = method.getResponseBodyAsStream();
            Map<String, List<String>> eventDescriptions = getMultipleValuesFromXML(is, new String[] { "description" });
            List<String> descriptionText = eventDescriptions.get("description");
            if (descriptionText == null) {
                s_logger.info("no events retrieved...");
            } else {
                for (String text : descriptionText) {
                    s_logger.info("event: " + text);
                }
            }
        } else {
            s_logger.error("list events failed with error code: " + responseCode + ". Following URL was sent: " + url);

            return responseCode;
        }

        // -------------------------------------------------------------------------------------
        // GENERATE USAGE RECORDS (note: typically this is done infrequently)
        // -------------------------------------------------------------------------------------
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();
        String endDate = dateFormat.format(currentDate);
        s_logger.info("Generating usage records from September 1st till " + endDate);
        url = server + "?command=generateUsageRecords&startdate=2009-09-01&enddate=" + endDate; // generate
        // all usage record till today
        client = new HttpClient();
        method = new GetMethod(url);
        responseCode = client.executeMethod(method);
        s_logger.info("generate usage records response code: " + responseCode);
        if (responseCode == 200) {
            InputStream is = method.getResponseBodyAsStream();
            Map<String, String> successStr = getSingleValueFromXML(is, new String[] { "success" });
            s_logger.info("successfully generated usage records? " + successStr.get("success"));
        } else {
            s_logger.error("generate usage records failed with error code: " + responseCode + ". Following URL was sent: " + url);
            return responseCode;
        }

        // Sleeping for a 2 minutes before getting a usage records from the database
        try {
            Thread.sleep(120000);
        } catch (Exception ex) {
            s_logger.error(ex);
        }

        // --------------------------------
        // GET USAGE RECORDS
        // --------------------------------
        url = server + "?command=listUsageRecords&startdate=2009-09-01&enddate=" + endDate + "&account=" + _account.get() + "&domaindid=1";
        s_logger.info("Getting all usage records with request: " + url);
        client = new HttpClient();
        method = new GetMethod(url);
        responseCode = client.executeMethod(method);
        s_logger.info("get usage records response code: " + responseCode);
        if (responseCode == 200) {
            InputStream is = method.getResponseBodyAsStream();
            Map<String, List<String>> usageRecValues = getMultipleValuesFromXML(is, new String[] { "description", "usage" });
            if ((usageRecValues.containsKey("description") == true) && (usageRecValues.containsKey("usage") == true)) {
                List<String> descriptions = usageRecValues.get("description");
                List<String> usages = usageRecValues.get("usage");
                for (int i = 0; i < descriptions.size(); i++) {
                    String desc = descriptions.get(i);
                    String usage = "";
                    if (usages != null) {
                        if (i < usages.size()) {
                            usage = ", usage: " + usages.get(i);
                        }
                    }
                    s_logger.info("desc: " + desc + usage);
                }
            }

        } else {
            s_logger.error("list usage records failed with error code: " + responseCode + ". Following URL was sent: " + url);
            return responseCode;
        }

        return responseCode;
    }

    private static boolean getNetworkStat(String server) {
        try {
            String url = server + "?command=listAccountStatistics&account=" + _account.get();
            HttpClient client = new HttpClient();
            HttpMethod method = new GetMethod(url);
            int responseCode = client.executeMethod(method);
            s_logger.info("listAccountStatistics response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                Map<String, String> requestKeyValues = getSingleValueFromXML(is, new String[] { "receivedbytes", "sentbytes" });
                int bytesReceived = Integer.parseInt(requestKeyValues.get("receivedbytes"));
                int bytesSent = Integer.parseInt(requestKeyValues.get("sentbytes"));
                if ((bytesReceived > 100000000) && (bytesSent > 0)) {
                    s_logger.info("Network stat is correct for account" + _account.get() + "; bytest received is " + bytesReceived + " and bytes sent is " + bytesSent);
                    return true;
                } else {
                    s_logger.error("Incorrect value for bytes received/sent for the account " + _account.get() + ". We got " + bytesReceived + " bytes received; " + " and " + bytesSent + " bytes sent");
                    return false;
                }

            } else {
                s_logger.error("listAccountStatistics failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return false;
            }
        } catch (Exception ex) {
            s_logger.error("Exception while sending command listAccountStatistics");
            return false;
        }
    }

    private static int executeStop(String server, String developerServer, String username, boolean destroy) throws HttpException, IOException {
        // test steps:
        // - get userId for the given username
        // - list virtual machines for the user
        // - stop all virtual machines
        // - get ip addresses for the user
        // - release ip addresses

        // -----------------------------
        // GET USER
        // -----------------------------
        String userId = _userId.get().toString();
        String encodedUserId = URLEncoder.encode(userId, "UTF-8");

        String url = server + "?command=listUsers&id=" + encodedUserId;
        s_logger.info("Stopping resources for user: " + username);
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(url);
        int responseCode = client.executeMethod(method);
        s_logger.info("get user response code: " + responseCode);
        if (responseCode == 200) {
            InputStream is = method.getResponseBodyAsStream();
            Map<String, String> userIdValues = getSingleValueFromXML(is, new String[] { "id" });
            String userIdStr = userIdValues.get("id");
            if (userIdStr != null) {
                userId = userIdStr;

            } else {
                s_logger.error("get user failed to retrieve a valid user id, aborting depolyment test" + ". Following URL was sent: " + url);
                return -1;
            }
        } else {
            s_logger.error("get user failed with error code: " + responseCode + ". Following URL was sent: " + url);
            return responseCode;
        }

        {
            // ----------------------------------
            // LIST VIRTUAL MACHINES
            // ----------------------------------
            String encodedApiKey = URLEncoder.encode(_apiKey.get(), "UTF-8");
            String requestToSign = "apikey=" + encodedApiKey + "&command=listVirtualMachines";
            requestToSign = requestToSign.toLowerCase();
            String signature = signRequest(requestToSign, _secretKey.get());
            String encodedSignature = URLEncoder.encode(signature, "UTF-8");

            url = developerServer + "?command=listVirtualMachines&apikey=" + encodedApiKey + "&signature=" + encodedSignature;

            s_logger.info("Listing all virtual machines for the user with url " + url);
            String[] vmIds = null;
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("list virtual machines response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                Map<String, List<String>> vmIdValues = getMultipleValuesFromXML(is, new String[] { "id" });
                if (vmIdValues.containsKey("id")) {
                    List<String> vmIdList = vmIdValues.get("id");
                    if (vmIdList != null) {
                        vmIds = new String[vmIdList.size()];
                        vmIdList.toArray(vmIds);
                        String vmIdLogStr = "";
                        if ((vmIds != null) && (vmIds.length > 0)) {
                            vmIdLogStr = vmIds[0];
                            for (int i = 1; i < vmIds.length; i++) {
                                vmIdLogStr = vmIdLogStr + "," + vmIds[i];
                            }
                        }
                        s_logger.info("got virtual machine ids: " + vmIdLogStr);
                    }
                }

            } else {
                s_logger.error("list virtual machines test failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }

            // ----------------------------------
            // LIST USER IP ADDRESSES
            // ----------------------------------

            requestToSign = "apikey=" + encodedApiKey + "&command=listPublicIpAddresses";
            requestToSign = requestToSign.toLowerCase();
            signature = signRequest(requestToSign, _secretKey.get());
            encodedSignature = URLEncoder.encode(signature, "UTF-8");

            url = developerServer + "?command=listPublicIpAddresses&apikey=" + encodedApiKey + "&signature=" + encodedSignature;
            String[] ipAddresses = null;
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("list ip addresses for user " + userId + " response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                Map<String, List<String>> ipAddressValues = getMultipleValuesFromXML(is, new String[] { "ipaddress" });
                if (ipAddressValues.containsKey("ipaddress")) {
                    List<String> ipAddressList = ipAddressValues.get("ipaddress");
                    if (ipAddressList != null) {
                        ipAddresses = new String[ipAddressList.size()];
                        ipAddressList.toArray(ipAddresses);
                        String ipAddressLogStr = "";
                        if ((ipAddresses != null) && (ipAddresses.length > 0)) {
                            ipAddressLogStr = ipAddresses[0];
                            for (int i = 1; i < ipAddresses.length; i++) {
                                ipAddressLogStr = ipAddressLogStr + "," + ipAddresses[i];
                            }
                        }
                        s_logger.info("got IP addresses: " + ipAddressLogStr);
                    }
                }

            } else {
                s_logger.error("list user ip addresses failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }

            // ----------------------------------
            // LIST ZONES
            // ----------------------------------

            requestToSign = "apikey=" + encodedApiKey + "&command=listZones";
            requestToSign = requestToSign.toLowerCase();
            signature = signRequest(requestToSign, _secretKey.get());
            encodedSignature = URLEncoder.encode(signature, "UTF-8");

            url = developerServer + "?command=listZones&apikey=" + encodedApiKey + "&signature=" + encodedSignature;
            String[] zoneNames = null;
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("list zones response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                Map<String, List<String>> zoneNameValues = getMultipleValuesFromXML(is, new String[] { "name" });
                if (zoneNameValues.containsKey("name")) {
                    List<String> zoneNameList = zoneNameValues.get("name");
                    if (zoneNameList != null) {
                        zoneNames = new String[zoneNameList.size()];
                        zoneNameList.toArray(zoneNames);
                        String zoneNameLogStr = "\n\n";
                        if ((zoneNames != null) && (zoneNames.length > 0)) {
                            zoneNameLogStr += zoneNames[0];
                            for (int i = 1; i < zoneNames.length; i++) {
                                zoneNameLogStr = zoneNameLogStr + "\n" + zoneNames[i];
                            }

                        }
                        zoneNameLogStr += "\n\n";
                        s_logger.info("got zones names: " + zoneNameLogStr);
                    }
                }

            } else {
                s_logger.error("list zones failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }

            // ----------------------------------
            // LIST ACCOUNT STATISTICS
            // ----------------------------------

            requestToSign = "apikey=" + encodedApiKey + "&command=listAccounts";
            requestToSign = requestToSign.toLowerCase();
            signature = signRequest(requestToSign, _secretKey.get());
            encodedSignature = URLEncoder.encode(signature, "UTF-8");

            url = developerServer + "?command=listAccounts&apikey=" + encodedApiKey + "&signature=" + encodedSignature;
            String[] statNames = null;
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("listAccountStatistics response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                Map<String, List<String>> statValues = getMultipleValuesFromXML(is, new String[] { "receivedbytes" });
                if (statValues.containsKey("receivedbytes")) {
                    List<String> statList = statValues.get("receivedbytes");
                    if (statList != null) {
                        statNames = new String[statList.size()];
                        statList.toArray(statNames);
                        String statLogStr = "\n\n";
                        if ((statNames != null) && (zoneNames.length > 0)) {
                            statLogStr += statNames[0];
                            for (int i = 1; i < statNames.length; i++) {
                                statLogStr = statLogStr + "\n" + zoneNames[i];
                            }

                        }
                        statLogStr += "\n\n";
                        s_logger.info("got accountstatistics: " + statLogStr);
                    }
                }

            } else {
                s_logger.error("listAccountStatistics failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }

            // ----------------------------------
            // LIST TEMPLATES
            // ----------------------------------

            requestToSign = "apikey=" + encodedApiKey + "&command=listTemplates@templatefilter=self";
            requestToSign = requestToSign.toLowerCase();
            signature = signRequest(requestToSign, _secretKey.get());
            encodedSignature = URLEncoder.encode(signature, "UTF-8");

            url = developerServer + "?command=listTemplates&apikey=" + encodedApiKey + "&templatefilter=self&signature=" + encodedSignature;
            String[] templateNames = null;
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("list templates response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                Map<String, List<String>> templateNameValues = getMultipleValuesFromXML(is, new String[] { "name" });

                if (templateNameValues.containsKey("name")) {
                    List<String> templateNameList = templateNameValues.get("name");
                    if (templateNameList != null) {
                        templateNames = new String[templateNameList.size()];
                        templateNameList.toArray(templateNames);
                        String templateNameLogStr = "\n\n";
                        if ((templateNames != null) && (templateNames.length > 0)) {
                            templateNameLogStr += templateNames[0];
                            for (int i = 1; i < templateNames.length; i++) {
                                templateNameLogStr = templateNameLogStr + "\n" + templateNames[i];
                            }

                        }
                        templateNameLogStr += "\n\n";
                        s_logger.info("got template names: " + templateNameLogStr);
                    }
                }

            } else {
                s_logger.error("list templates failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }

            // ----------------------------------
            // LIST SERVICE OFFERINGS
            // ----------------------------------

            requestToSign = "apikey=" + encodedApiKey + "&command=listServiceOfferings";
            requestToSign = requestToSign.toLowerCase();
            signature = signRequest(requestToSign, _secretKey.get());
            encodedSignature = URLEncoder.encode(signature, "UTF-8");

            url = developerServer + "?command=listServiceOfferings&apikey=" + encodedApiKey + "&signature=" + encodedSignature;
            String[] serviceOfferingNames = null;
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("list service offerings response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                Map<String, List<String>> serviceOfferingNameValues = getMultipleValuesFromXML(is, new String[] { "name" });

                if (serviceOfferingNameValues.containsKey("name")) {
                    List<String> serviceOfferingNameList = serviceOfferingNameValues.get("name");
                    if (serviceOfferingNameList != null) {
                        serviceOfferingNames = new String[serviceOfferingNameList.size()];
                        serviceOfferingNameList.toArray(serviceOfferingNames);
                        String serviceOfferingNameLogStr = "";
                        if ((serviceOfferingNames != null) && (serviceOfferingNames.length > 0)) {
                            serviceOfferingNameLogStr = serviceOfferingNames[0];
                            for (int i = 1; i < serviceOfferingNames.length; i++) {
                                serviceOfferingNameLogStr = serviceOfferingNameLogStr + ", " + serviceOfferingNames[i];
                            }
                        }
                        s_logger.info("got service offering names: " + serviceOfferingNameLogStr);
                    }
                }

            } else {
                s_logger.error("list service offerings failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }

            // ----------------------------------
            // LIST EVENTS
            // ---------------------------------

            url = server + "?command=listEvents&page=1&pagesize=100&&account=" + _account.get();
            String[] eventDescriptions = null;
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("list events response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                Map<String, List<String>> eventNameValues = getMultipleValuesFromXML(is, new String[] { "description" });

                if (eventNameValues.containsKey("description")) {
                    List<String> eventNameList = eventNameValues.get("description");
                    if (eventNameList != null) {
                        eventDescriptions = new String[eventNameList.size()];
                        eventNameList.toArray(eventDescriptions);
                        String eventNameLogStr = "\n\n";
                        if ((eventDescriptions != null) && (eventDescriptions.length > 0)) {
                            eventNameLogStr += eventDescriptions[0];
                            for (int i = 1; i < eventDescriptions.length; i++) {
                                eventNameLogStr = eventNameLogStr + "\n" + eventDescriptions[i];
                            }
                        }
                        eventNameLogStr += "\n\n";
                        s_logger.info("got event descriptions: " + eventNameLogStr);
                    }
                }
            } else {
                s_logger.error("list events failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }

            // ----------------------------------
            // STOP/DESTROY VIRTUAL MACHINES
            // ----------------------------------
            if (vmIds != null) {
                String cmdName = (destroy ? "destroyVirtualMachine" : "stopVirtualMachine");
                for (String vmId : vmIds) {
                    requestToSign = "apikey=" + encodedApiKey + "&command=" + cmdName + "&id=" + vmId;
                    requestToSign = requestToSign.toLowerCase();
                    signature = signRequest(requestToSign, _secretKey.get());
                    encodedSignature = URLEncoder.encode(signature, "UTF-8");

                    url = developerServer + "?command=" + cmdName + "&id=" + vmId + "&apikey=" + encodedApiKey + "&signature=" + encodedSignature;
                    client = new HttpClient();
                    method = new GetMethod(url);
                    responseCode = client.executeMethod(method);
                    s_logger.info(cmdName + " [" + vmId + "] response code: " + responseCode);
                    if (responseCode == 200) {
                        InputStream input = method.getResponseBodyAsStream();
                        Element el = queryAsyncJobResult(server, input);
                        Map<String, String> success = getSingleValueFromXML(el, new String[] { "success" });
                        s_logger.info(cmdName + "..success? " + success.get("success"));
                    } else {
                        s_logger.error(cmdName + "test failed with error code: " + responseCode + ". Following URL was sent: " + url);
                        return responseCode;
                    }
                }
            }
        }

        {
            String[] ipAddresses = null;
            // -----------------------------------------
            // LIST NAT IP ADDRESSES
            // -----------------------------------------
            String encodedApiKey = URLEncoder.encode(_apiKey.get(), "UTF-8");
            String requestToSign = "apikey=" + encodedApiKey + "&command=listPublicIpAddresses";
            requestToSign = requestToSign.toLowerCase();
            String signature = signRequest(requestToSign, _secretKey.get());
            String encodedSignature = URLEncoder.encode(signature, "UTF-8");

            url = developerServer + "?command=listPublicIpAddresses&apikey=" + encodedApiKey + "&signature=" + encodedSignature;
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("list ip addresses for user " + userId + " response code: " + responseCode);
            if (responseCode == 200) {

                InputStream is = method.getResponseBodyAsStream();
                List<String> ipAddressList = getNonSourceNatIPs(is);
                ipAddresses = new String[ipAddressList.size()];
                ipAddressList.toArray(ipAddresses);
                String ipAddrLogStr = "";
                if ((ipAddresses != null) && (ipAddresses.length > 0)) {
                    ipAddrLogStr = ipAddresses[0];
                    for (int i = 1; i < ipAddresses.length; i++) {
                        ipAddrLogStr = ipAddrLogStr + "," + ipAddresses[i];
                    }
                }
                s_logger.info("got ip addresses: " + ipAddrLogStr);

            } else {
                s_logger.error("list nat ip addresses failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }

            // -------------------------------------------------------------
            // Delete IP FORWARDING RULE -- Windows VM
            // -------------------------------------------------------------
            String encodedIpFwdId = URLEncoder.encode(_winipfwdid.get(), "UTF-8");

            requestToSign = "apikey=" + encodedApiKey + "&command=deleteIpForwardingRule&id=" + encodedIpFwdId;
            requestToSign = requestToSign.toLowerCase();
            signature = signRequest(requestToSign, _secretKey.get());
            encodedSignature = URLEncoder.encode(signature, "UTF-8");

            url = developerServer + "?command=deleteIpForwardingRule&apikey=" + encodedApiKey + "&id=" + encodedIpFwdId + "&signature=" + encodedSignature;

            s_logger.info("Delete Ip forwarding rule with " + url);
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            if (responseCode == 200) {
                InputStream input = method.getResponseBodyAsStream();
                Element el = queryAsyncJobResult(server, input);
                s_logger.info("IP forwarding rule was successfully deleted");        
                
            } else {
                s_logger.error("IP forwarding rule creation failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }

            //--------------------------------------------
            // Disable Static NAT for the Source NAT Ip
            //--------------------------------------------
            encodedApiKey = URLEncoder.encode(_apiKey.get(), "UTF-8");
            String encodedPublicIpId = URLEncoder.encode(_publicIpId.get(), "UTF-8");       
            requestToSign = "apikey=" + encodedApiKey + "&command=disableStaticNat"+"&id=" + encodedPublicIpId;
            requestToSign = requestToSign.toLowerCase();
            signature = signRequest(requestToSign, _secretKey.get());
            encodedSignature = URLEncoder.encode(signature, "UTF-8");
            
            url = developerServer + "?command=disableStaticNat&apikey=" + encodedApiKey + "&id=" + encodedPublicIpId + "&signature=" + encodedSignature ;
            client = new HttpClient();
            method = new GetMethod(url);
            responseCode = client.executeMethod(method);
            s_logger.info("url is " + url);
            s_logger.info("list ip addresses for user " + userId + " response code: " + responseCode);
            if (responseCode == 200) {
                InputStream is = method.getResponseBodyAsStream();
                Map<String, String> success = getSingleValueFromXML(is, new String[] { "success" });
                s_logger.info("Disable Static NAT..success? " + success.get("success"));
            } else {
                s_logger.error("Disable Static NAT failed with error code: " + responseCode + ". Following URL was sent: " + url);
                return responseCode;
            }
            
            // -----------------------------------------
            // DISASSOCIATE IP ADDRESSES
            // -----------------------------------------
            if (ipAddresses != null) {
                for (String ipAddress : ipAddresses) {
                    requestToSign = "apikey=" + encodedApiKey + "&command=disassociateIpAddress&id=" + ipAddress;
                    requestToSign = requestToSign.toLowerCase();
                    signature = signRequest(requestToSign, _secretKey.get());
                    encodedSignature = URLEncoder.encode(signature, "UTF-8");

                    url = developerServer + "?command=disassociateIpAddress&apikey=" + encodedApiKey + "&id=" + ipAddress + "&signature=" + encodedSignature;
                    client = new HttpClient();
                    method = new GetMethod(url);
                    responseCode = client.executeMethod(method);
                    s_logger.info("disassociate ip address [" + userId + "/" + ipAddress + "] response code: " + responseCode);
                    if (responseCode == 200) {
                        InputStream input = method.getResponseBodyAsStream();
                        Element disassocipel = queryAsyncJobResult(server, input);
                        Map<String, String> success = getSingleValueFromXML(disassocipel, new String[] {"success"});
               //       Map<String, String> success = getSingleValueFromXML(input, new String[] { "success" });
                        s_logger.info("disassociate ip address..success? " + success.get("success"));
                    } else {
                        s_logger.error("disassociate ip address failed with error code: " + responseCode + ". Following URL was sent: " + url);
                        return responseCode;
                    }
                }
            }
        }
        _linuxIP.set("");
        _linuxIpId.set("");
        _linuxVmId.set("");
        _linuxPassword.set("");
        _windowsIP.set("");
        _windowsIpId.set("");
        _windowsVmId.set("");
        _secretKey.set("");
        _apiKey.set("");
        _userId.set(Long.parseLong("0"));
        _account.set("");
        _domainRouterId.set("");
        return responseCode;
    }

    public static String signRequest(String request, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            mac.init(keySpec);
            mac.update(request.getBytes());
            byte[] encryptedBytes = mac.doFinal();
            return Base64.encodeBytes(encryptedBytes);
        } catch (Exception ex) {
            s_logger.error("unable to sign request", ex);
        }
        return null;
    }

    private static String sshWinTest(String host) {
        if (host == null) {
            s_logger.info("Did not receive a host back from test, ignoring win ssh test");
            return null;
        }

        // We will retry 5 times before quitting
        int retry = 1;

        while (true) {
            try {
                if (retry > 0) {
                    s_logger.info("Retry attempt : " + retry + " ...sleeping 300 seconds before next attempt. Account is " + _account.get());
                    Thread.sleep(300000);
                }

                s_logger.info("Attempting to SSH into windows host " + host + " with retry attempt: " + retry + " for account " + _account.get());

                Connection conn = new Connection(host);
                conn.connect(null, 60000, 60000);

                s_logger.info("User " + _account.get() + " ssHed successfully into windows host " + host);
                boolean success = false;
                boolean isAuthenticated = conn.authenticateWithPassword("Administrator", "password");
                if (isAuthenticated == false) {
                    return "Authentication failed";
                } else {
                    s_logger.info("Authentication is successfull");
                }

                try {
                    SCPClient scp = new SCPClient(conn);
                    scp.put("wget.exe", "wget.exe", "C:\\Users\\Administrator", "0777");
                    s_logger.info("Successfully put wget.exe file");
                } catch (Exception ex) {
                    s_logger.error("Unable to put wget.exe " + ex);
                }

                if (conn == null) {
                    s_logger.error("Connection is null");
                }
                Session sess = conn.openSession();

                s_logger.info("User + " + _account.get() + " executing : wget http://" + downloadUrl);
                String downloadCommand = "wget http://" + downloadUrl + " && dir dump.bin";
                sess.execCommand(downloadCommand);

                InputStream stdout = sess.getStdout();
                InputStream stderr = sess.getStderr();

                byte[] buffer = new byte[8192];
                while (true) {
                    if ((stdout.available() == 0) && (stderr.available() == 0)) {
                        int conditions = sess.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA | ChannelCondition.EOF, 120000);

                        if ((conditions & ChannelCondition.TIMEOUT) != 0) {
                            s_logger.info("Timeout while waiting for data from peer.");
                            return null;
                        }

                        if ((conditions & ChannelCondition.EOF) != 0) {
                            if ((conditions & (ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA)) == 0) {
                                break;
                            }
                        }
                    }

                    while (stdout.available() > 0) {
                        success = true;
                        int len = stdout.read(buffer);
                        if (len > 0) // this check is somewhat paranoid
                            s_logger.info(new String(buffer, 0, len));
                    }

                    while (stderr.available() > 0) {
                        /* int len = */stderr.read(buffer);
                    }
                }
                sess.close();
                conn.close();

                if (success) {
                    return null;
                } else {
                    retry++;
                    if (retry == MAX_RETRY_WIN) {
                        return "SSH Windows Network test fail for account " + _account.get();
                    }
                }
            } catch (Exception e) {
                s_logger.error(e);
                retry++;
                if (retry == MAX_RETRY_WIN) {
                    return "SSH Windows Network test fail with error " + e.getMessage();
                }
            }
        }
    }

    private static String sshTest(String host, String password, String snapshot_test) {
        int i = 0;
        if (host == null) {
            s_logger.info("Did not receive a host back from test, ignoring ssh test");
            return null;
        }

        if (password == null) {
            s_logger.info("Did not receive a password back from test, ignoring ssh test");
            return null;
        }

        // We will retry 5 times before quitting
        String result = null;
        int retry = 0;

        while (true) {
            try {
                if (retry > 0) {
                    s_logger.info("Retry attempt : " + retry + " ...sleeping 120 seconds before next attempt. Account is " + _account.get());
                    Thread.sleep(120000);
                }

                s_logger.info("Attempting to SSH into linux host " + host + " with retry attempt: " + retry + ". Account is " + _account.get());

                Connection conn = new Connection(host);
                conn.connect(null, 60000, 60000);

                s_logger.info("User + " + _account.get() + " ssHed successfully into linux host " + host);

                boolean isAuthenticated = conn.authenticateWithPassword("root", password);

                if (isAuthenticated == false) {
                    s_logger.info("Authentication failed for root with password" + password);
                    return "Authentication failed";

                }

                boolean success = false;
                String linuxCommand = null;

                if (i % 10 == 0)
                    linuxCommand = "rm -rf *; wget http://" + downloadUrl + " && ls -al dump.bin";
                else
                    linuxCommand = "wget http://" + downloadUrl + " && ls -al dump.bin";

                Session sess = conn.openSession();
                s_logger.info("User " + _account.get() + " executing : " + linuxCommand);
                sess.execCommand(linuxCommand);

                InputStream stdout = sess.getStdout();
                InputStream stderr = sess.getStderr();

                byte[] buffer = new byte[8192];
                while (true) {
                    if ((stdout.available() == 0) && (stderr.available() == 0)) {
                        int conditions = sess.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA | ChannelCondition.EOF, 120000);

                        if ((conditions & ChannelCondition.TIMEOUT) != 0) {
                            s_logger.info("Timeout while waiting for data from peer.");
                            return null;
                        }

                        if ((conditions & ChannelCondition.EOF) != 0) {
                            if ((conditions & (ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA)) == 0) {
                                break;
                            }
                        }
                    }

                    while (stdout.available() > 0) {
                        success = true;
                        int len = stdout.read(buffer);
                        if (len > 0) // this check is somewhat paranoid
                            s_logger.info(new String(buffer, 0, len));
                    }

                    while (stderr.available() > 0) {
                        /* int len = */stderr.read(buffer);
                    }
                }

                sess.close();
                conn.close();

                if (!success) {
                    retry++;
                    if (retry == MAX_RETRY_LINUX) {
                        result = "SSH Linux Network test fail";
                    }
                }

                if (snapshot_test.equals("no"))
                    return result;
                else {
                    Long sleep = 300000L;
                    s_logger.info("Sleeping for " + sleep / 1000 / 60 + "minutes before executing next ssh test");
                    Thread.sleep(sleep);
                }
            } catch (Exception e) {
                retry++;
                s_logger.error("SSH Linux Network test fail with error");
                if ((retry == MAX_RETRY_LINUX) && (snapshot_test.equals("no"))) {
                    return "SSH Linux Network test fail with error " + e.getMessage();
                }
            }
            i++;
        }
    }

    public static String createMD5Password(String password) {
        MessageDigest md5;

        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new CloudRuntimeException("Error", e);
        }

        md5.reset();
        BigInteger pwInt = new BigInteger(1, md5.digest(password.getBytes()));

        // make sure our MD5 hash value is 32 digits long...
        StringBuffer sb = new StringBuffer();
        String pwStr = pwInt.toString(16);
        int padding = 32 - pwStr.length();
        for (int i = 0; i < padding; i++) {
            sb.append('0');
        }
        sb.append(pwStr);
        return sb.toString();
    }

    public static Element queryAsyncJobResult(String host, InputStream inputStream) {
        Element returnBody = null;

        Map<String, String> values = getSingleValueFromXML(inputStream, new String[] { "jobid" });
        String jobId = values.get("jobid");

        if (jobId == null) {
            s_logger.error("Unable to get a jobId");
            return null;
        }

        // s_logger.info("Job id is " + jobId);
        String resultUrl = host + "?command=queryAsyncJobResult&jobid=" + jobId;
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(resultUrl);
        while (true) {
            try {
                client.executeMethod(method);
                // s_logger.info("Method is executed successfully. Following url was sent " + resultUrl);
                InputStream is = method.getResponseBodyAsStream();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(is);
                returnBody = doc.getDocumentElement();
                doc.getDocumentElement().normalize();
                Element jobStatusTag = (Element) returnBody.getElementsByTagName("jobstatus").item(0);
                String jobStatus = jobStatusTag.getTextContent();
                if (jobStatus.equals("0")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    break;
                }

            } catch (Exception ex) {
                s_logger.error(ex);
            }
        }
        return returnBody;
    }

}
