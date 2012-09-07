package com.cloud.host.updates;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.xensource.xenapi.APIVersion;
import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.HostPatch;
import com.xensource.xenapi.Message;
import com.xensource.xenapi.PoolPatch;
import com.xensource.xenapi.Session;
import com.xensource.xenapi.VM;
import com.xensource.xenapi.Message.Record;

public class Xensdk {
    final static String masterIp = "10.102.125.13";
    final static String uname = "root";
    final static String pwd = "freebsd";
    /**
	 * @param args
	 */
	public static void serverUpdates() {
	    Connection masterConn = null;
        try {
            masterConn = new Connection(new URL("http://" + masterIp), 14);
            Session masterSession = Session.loginWithPassword(masterConn, uname, pwd, APIVersion.latest().toString());
            //String session_uuid = masterSession.getUuid(masterConn);
            Map<Message,Record> allMessages = Message.getAllRecords(masterConn);
            Map<Host, com.xensource.xenapi.Host.Record> hostRecords = Host.getAllRecords(masterConn);
            String XenserverVersion = null;
            com.xensource.xenapi.Host.Record hostDetails = null;
            for ( Entry<Host, com.xensource.xenapi.Host.Record> entryDetail : hostRecords.entrySet())
            	{
            		hostDetails = entryDetail.getValue();
					for(Entry<String, String> data : hostDetails.softwareVersion.entrySet())
            			if(data.getKey().equals("product_version"))
            			{
            				XenserverVersion = data.getValue();
							System.out.println("Xenserver Version = " + XenserverVersion);
							break;
            			}
            	}

            for(Map.Entry<Message, Record> entry : allMessages.entrySet())
            {
            	Record key = entry.getValue();
            //if (key.cls.toString() == "HOST")
            //System.out.println("System Alerts" + key.toString());
            }
            
            
            File updatesFile = new File("/home/sanjay/Desktop/updatesFile.xml");
            
            URL updates = new URL("http://updates.xensource.com/XenServer/updates.xml");
            ReadableByteChannel fis = Channels.newChannel(updates.openStream());
            FileOutputStream fos = new FileOutputStream(updatesFile);
            fos.getChannel().transferFrom(fis, 0, 1 << 24);

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(updatesFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("version");
            //System.out.println("Check = " + nList.item(0).getParentNode().getNodeName());
            NodeList patchTags = doc.getElementsByTagName("patch");
            String updatePatchUuid = null;
            for(int counter = 0; counter < nList.getLength(); counter++)
            {
            	Node nNode = nList.item(counter);
            	if(nNode.getParentNode().getNodeName() == "serverversions")
            	{
	            	Element value = (Element) nNode;
	            	if(value.getAttribute("value").equals(XenserverVersion))
	            	{
	            		
	            		System.out.println("Version : " + value.getAttribute("name"));
	            		NodeList patchList  = value.getElementsByTagName("patch");
	            		for(int temp = 0; temp < patchList.getLength(); temp++)
	            		{	
	            			int flag = 0;
	            			Node patchNode = patchList.item(temp);
	            			Element patchElement = (Element) patchNode;
	            			String patchUuid = patchElement.getAttribute("uuid");
	            			Map<PoolPatch, com.xensource.xenapi.PoolPatch.Record> appliedPatches = PoolPatch.getAllRecords(masterConn);
		            		//Map<HostPatch, com.xensource.xenapi.HostPatch.Record> appliedPatches = HostPatch.getAllRecords(masterConn);
		            		//System.out.println("Testing" + appliedPatches);
		            		for(Entry<PoolPatch, com.xensource.xenapi.PoolPatch.Record> appliedPatch : appliedPatches.entrySet())
		            		{
		            			updatePatchUuid = appliedPatch.getValue().uuid;
		            			if(updatePatchUuid.equals(patchUuid))
		            			{
		            				flag = 1;
		            				break;
		            			}
		            			
		            		}
		            		if(flag == 0)
		            		{
		            			for(int patchCounter = 0; patchCounter < patchTags.getLength(); patchCounter++)
		                        {
		                        	Node patchNode2 = patchTags.item(patchCounter);
		                        	if(patchNode2.getParentNode().getNodeName() == "patches")
		                        	{
		                        		Element patchValue = (Element) patchNode2;
		            	            	if(patchValue.getAttribute("uuid").equals(patchUuid))
		                        		System.out.println(patchValue.getAttribute("name-description"));
		                        	}
		                        }	
		            		}
	            		}
	                    //patchList.
	            		//System.out.println("Version : " + patchList.);
	            	}
            	}
            }
            
            
			//System.out.println("XML content = " + doc.getDocumentElement().getNodeName());
            //System.out.println("Session = " + masterSession);
            //System.out.println("Session_Uuid = " + session_uuid);
        } catch (Exception e) {
            System.out.println("Error message: " + e.getMessage());
        } finally {
            if (masterConn != null) {
                try {
                	//System.out.println("Logged in as master.");
                    Session.logout(masterConn);
                } catch (Exception e) {
                    System.out.println("Error in logout for master connection.");
                }

                masterConn.dispose();
                masterConn = null;
            }
        }
	}
	
    private static String len(Set<VM> vms) {
		// TODO Auto-generated method stub
		return null;
	}

	public static URL getURL(String ip) throws MalformedURLException {
            return new URL("http://" + ip);
    }
}