/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
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
package com.cloud.agent.resource.computing;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.cloud.agent.resource.computing.LibvirtVMDef.DiskDef;
import com.cloud.agent.resource.computing.LibvirtVMDef.InterfaceDef;
import com.cloud.agent.resource.computing.LibvirtVMDef.InterfaceDef.nicModel;

/**
 * @author chiradeep
 *
 */
public class LibvirtDomainXMLParser {
    private static final Logger s_logger = Logger.getLogger(LibvirtDomainXMLParser.class);
	private final List<InterfaceDef> interfaces = new ArrayList<InterfaceDef>();
	private final List<DiskDef> diskDefs = new ArrayList<DiskDef>();
	private Integer vncPort;
	private String desc;
	
	public boolean parseDomainXML(String domXML) {
	    DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(domXML));
            Document doc = builder.parse(is);
            
            Element rootElement = doc.getDocumentElement();
            
            desc = getTagValue("description", rootElement);
            
            Element devices = (Element)rootElement.getElementsByTagName("devices").item(0);
            NodeList disks = devices.getElementsByTagName("disk");
            for (int i = 0; i < disks.getLength(); i++) {
                Element disk = (Element)disks.item(i);
                String diskFmtType = getAttrValue("driver", "type", disk);
                String diskFile = getAttrValue("source", "file", disk);
                String diskDev = getAttrValue("source", "dev", disk);
                String diskLabel = getAttrValue("target", "dev", disk);
                String bus = getAttrValue("target", "bus", disk);
                String type = disk.getAttribute("type");
                String device = disk.getAttribute("device");
                
                DiskDef def = new DiskDef();
                if (type.equalsIgnoreCase("file")) {
                    if (device.equalsIgnoreCase("disk")) {
                        DiskDef.diskFmtType fmt = null;
                        if (diskFmtType != null) {
                            fmt = DiskDef.diskFmtType.valueOf(diskFmtType.toUpperCase());
                        }
                        def.defFileBasedDisk(diskFile, diskLabel, DiskDef.diskBus.valueOf(bus.toUpperCase()), fmt);
                    } else if (device.equalsIgnoreCase("cdrom")) {
                        def.defISODisk(diskFile);
                    }
                } else if (type.equalsIgnoreCase("block")) {
                    def.defBlockBasedDisk(diskDev, diskLabel, DiskDef.diskBus.valueOf(bus.toUpperCase()));
                }
                diskDefs.add(def);
            }
            
            NodeList nics = devices.getElementsByTagName("interface");
            for (int i = 0; i < nics.getLength(); i++ ) {
                Element nic = (Element)nics.item(i);
                
                String type = nic.getAttribute("type");
                String mac = getAttrValue("mac", "address", nic);
                String dev = getAttrValue("target", "dev", nic);
                String model = getAttrValue("model", "type", nic);
                InterfaceDef def = new InterfaceDef();

                if (type.equalsIgnoreCase("network")) {
                    String network = getAttrValue("source", "network", nic);
                    def.defPrivateNet(network, dev, mac, nicModel.valueOf(model.toUpperCase()));
                } else if (type.equalsIgnoreCase("bridge")) {
                    String bridge = getAttrValue("source", "bridge", nic);
                    def.defBridgeNet(bridge, dev, mac, nicModel.valueOf(model.toUpperCase()));
                }
                interfaces.add(def);
            }
            
            Element graphic = (Element)devices.getElementsByTagName("graphics").item(0);
            String port = graphic.getAttribute("port");
            if (port != null) {
                try {
                    vncPort = Integer.parseInt(port);
                    if (vncPort != -1) {
                        vncPort = vncPort - 5900;
                    } else {
                        vncPort = null;
                    }
                }catch (NumberFormatException nfe){
                    vncPort = null;
                }
            }

            return true;
        } catch (ParserConfigurationException e) {
          s_logger.debug(e.toString());
        } catch (SAXException e) {
            s_logger.debug(e.toString());
        } catch (IOException e) {
            s_logger.debug(e.toString());
        }
        return false;
	}
	
	private static String getTagValue(String tag, Element eElement){
	    NodeList tagNodeList = eElement.getElementsByTagName(tag);
	    if (tagNodeList == null || tagNodeList.getLength() == 0) {
	        return null;
	    }
	    
	    NodeList nlList= tagNodeList.item(0).getChildNodes();

	    Node nValue = (Node) nlList.item(0); 

	    return nValue.getNodeValue();    
	}

	private static String getAttrValue(String tag, String attr, Element eElement){
	    NodeList tagNode = eElement.getElementsByTagName(tag);
	    if (tagNode.getLength() == 0) {
	        return null;
	    }
	    Element node = (Element)tagNode.item(0);
	    return node.getAttribute(attr);
	}
	
	public Integer getVncPort() {
		return vncPort;
	}
	
	public List<InterfaceDef> getInterfaces() {
		return interfaces;
	}
	
	public List<DiskDef> getDisks() {
		return diskDefs;
	}
	
	public String getDescription() {
		return desc;
	}

	public static void main(String [] args){
		LibvirtDomainXMLParser parser = new LibvirtDomainXMLParser();
		parser.parseDomainXML("<domain type='kvm' id='12'>"+
				  "<name>r-6-CV-5002-1</name>"+
				  "<uuid>581b5a4b-b496-8d4d-e44e-a7dcbe9df0b5</uuid>"+
				  "<description>testVM</description>"+
				  "<memory>131072</memory>"+
				  "<currentMemory>131072</currentMemory>"+
				  "<vcpu>1</vcpu>"+
				  "<os>"+
				    "<type arch='i686' machine='pc-0.11'>hvm</type>"+
				    "<kernel>/var/lib/libvirt/qemu/vmlinuz-2.6.31.6-166.fc12.i686</kernel>"+
				    "<cmdline>ro root=/dev/sda1 acpi=force selinux=0 eth0ip=10.1.1.1 eth0mask=255.255.255.0 eth2ip=192.168.10.152 eth2mask=255.255.255.0 gateway=192.168.10.1 dns1=72.52.126.11 dns2=72.52.126.12 domain=v4.myvm.com</cmdline>"+
				    "<boot dev='hd'/>"+
				  "</os>"+
				  "<features>"+
				    "<acpi/>"+
				    "<pae/>"+
				  "</features>"+
				  "<clock offset='utc'/>"+
				  "<on_poweroff>destroy</on_poweroff>"+
				  "<on_reboot>restart</on_reboot>"+
				  "<on_crash>destroy</on_crash>"+
				  "<devices>"+
				    "<emulator>/usr/bin/qemu-kvm</emulator>"+
				    "<disk type='file' device='disk'>"+
				      "<driver name='qemu' type='raw'/>"+
				      "<source file='/mnt/tank//vmops/CV/vm/u000004/r000006/rootdisk'/>"+
				      "<target dev='hda' bus='ide'/>"+
				    "</disk>"+
				    "<interface type='bridge'>"+
				      "<mac address='02:00:50:02:00:01'/>"+
				      "<source bridge='vnbr5002'/>"+
				      "<target dev='vtap5002'/>"+
				      "<model type='e1000'/>"+
				    "</interface>"+
				    "<interface type='network'>"+
				      "<mac address='00:16:3e:77:e2:a1'/>"+
				      "<source network='vmops-private'/>"+
				      "<target dev='vnet3'/>"+
				      "<model type='e1000'/>"+
				    "</interface>"+
				    "<interface type='bridge'>"+
				      "<mac address='06:85:00:00:00:04'/>"+
				      "<source bridge='br0'/>"+
				      "<target dev='tap5002'/>"+
				      "<model type='e1000'/>"+
				    "</interface>"+
				    "<input type='mouse' bus='ps2'/>"+
				    "<graphics type='vnc' port='6031' autoport='no' listen=''/>"+
				    "<video>"+
				      "<model type='cirrus' vram='9216' heads='1'/>"+
				    "</video>"+
				  "</devices>"+
				"</domain>"

		);
		for (InterfaceDef intf: parser.getInterfaces()){
			System.out.println(intf);
		}
		for (DiskDef disk : parser.getDisks()) {
		    System.out.println(disk);
		}
		System.out.println(parser.getVncPort());
		System.out.println(parser.getDescription());
		
		List<String> test = new ArrayList<String>(1);
		test.add("1");
		test.add("2");
		if (test.contains("1")) {
		    System.out.print("fdf");
		}
	}

}
