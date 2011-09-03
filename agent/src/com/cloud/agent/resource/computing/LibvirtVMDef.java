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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LibvirtVMDef {
	private String _hvsType;
	private String _domName;
	private String _domUUID;
	private String _desc;
	private final Map<String, Object> components = new HashMap<String,Object>();
	
	public static class GuestDef {
		enum guestType {
			KVM,
			XEN,
			EXE
		}
		enum bootOrder {
			HARDISK("hd"),
			CDROM("cdrom"),
			FLOOPY("fd"),
			NETWORK("network");
			String _order;
			bootOrder(String order) {
				_order = order;
			}
			@Override
            public String toString() {
				return _order;
			}
		}
		private guestType _type;
		private String _arch;
		private String _loader;
		private String _kernel;
		private String _initrd;
		private String _root;
		private String _cmdline;
		private List<bootOrder> _bootdevs = new ArrayList<bootOrder>();
		private String _machine;
		public void setGuestType (guestType type) {
			_type = type;
		}
		public void setGuestArch (String arch) {
			_arch = arch;
		}
		public void setMachineType (String machine) {
			_machine = machine;
		}
		public void setLoader (String loader) {
			_loader = loader;
		}
		public void setBootKernel(String kernel, String initrd, String rootdev, String cmdline) {
			_kernel = kernel;
			_initrd = initrd;
			_root = rootdev;
			_cmdline = cmdline;
		}
		public void setBootOrder(bootOrder order) {
			_bootdevs.add(order);
		}
		@Override
        public String toString () {
			if (_type == guestType.KVM) {
				StringBuilder guestDef = new StringBuilder();
				guestDef.append("<os>\n");
				guestDef.append("<type ");
				if (_arch != null) {
					guestDef.append(" arch='" + _arch + "'");
				}
				if (_machine != null) {
					guestDef.append(" machine='" + _machine + "'");
				}
				guestDef.append(">hvm</type>\n");
				if (!_bootdevs.isEmpty()) {
					for (bootOrder bo : _bootdevs) {
						guestDef.append("<boot dev='" + bo + "'/>\n");
					}
				}
				guestDef.append("</os>\n");
				return guestDef.toString();
			} else
				return null;
		}
	}
	
	public static class GuestResourceDef {
		private long _mem;
		private int _currentMem = -1;
		private String _memBacking;
		private int _vcpu = -1;
		public void setMemorySize(long mem) {
			_mem = mem;
		}
		public void setCurrentMem(int currMem) {
			_currentMem = currMem;
		}
		public void setMemBacking(String memBacking) {
			_memBacking = memBacking;
		}
		public void setVcpuNum(int vcpu) {
			_vcpu = vcpu;
		}
		@Override
        public String toString(){
			StringBuilder resBuidler = new StringBuilder();
			resBuidler.append("<memory>" + _mem + "</memory>\n");
			if (_currentMem != -1) {
				resBuidler.append("<currentMemory>" + _currentMem + "</currentMemory>\n");
			}
			if (_memBacking != null) {
				resBuidler.append("<memoryBacking>" + "<" + _memBacking + "/>" + "</memoryBacking>\n");
			}
			if (_vcpu != -1) {
				resBuidler.append("<vcpu>" + _vcpu + "</vcpu>\n");
			}
			return resBuidler.toString();
		}
	}
	
	public static class FeaturesDef {
		private final List<String> _features = new ArrayList<String>();
		public void addFeatures(String feature) {
			_features.add(feature);
		}
		@Override
        public String toString() {
			StringBuilder feaBuilder = new StringBuilder();
			feaBuilder.append("<features>\n");
			for (String feature : _features) {
				feaBuilder.append("<" + feature + "/>\n");
			}
			feaBuilder.append("</features>\n");
			return feaBuilder.toString();
		}
	}
	public static class TermPolicy {
		private String _reboot;
		private String _powerOff;
		private String _crash;
		public TermPolicy() {
			_reboot = _powerOff = _crash = "destroy";
		}
		public void setRebootPolicy(String rbPolicy) {
			_reboot = rbPolicy;
		}
		public void setPowerOffPolicy(String poPolicy) {
			_powerOff = poPolicy;
		}
		public void setCrashPolicy(String crashPolicy) {
			_crash = crashPolicy;
		}
		@Override
        public String toString() {
			StringBuilder term = new StringBuilder();
			term.append("<on_reboot>" + _reboot + "</on_reboot>\n");
			term.append("<on_poweroff>" + _powerOff + "</on_poweroff>\n");
			term.append("<on_crash>" + _powerOff + "</on_crash>\n");
			return term.toString();
		}
	}
	
	public static class DevicesDef {
		private String _emulator;
		private final Map<String, List<?>> devices = new HashMap<String, List<?>>();
		public boolean addDevice(Object device) {
			Object dev = devices.get(device.getClass().toString());
			if (dev == null) { 
				List<Object> devs = new ArrayList<Object>();
				devs.add(device);
				devices.put(device.getClass().toString(), devs);
			} else {
				List<Object> devs = (List<Object>)dev;
				devs.add(device);
			}
			return true;
		}
		public void setEmulatorPath(String emulator) {
			_emulator = emulator;
		}
		@Override
        public String toString() {
			StringBuilder devicesBuilder = new StringBuilder();
			devicesBuilder.append("<devices>\n");
			if (_emulator != null) {
				devicesBuilder.append("<emulator>" + _emulator + "</emulator>\n");
			}
		
			for (List<?> devs : devices.values()) {
				for (Object dev : devs) {
					devicesBuilder.append(dev.toString());
				}
			}
			devicesBuilder.append("</devices>\n");
			return devicesBuilder.toString();
		}
		public List<DiskDef> getDisks() {
			return (List<DiskDef>)devices.get(DiskDef.class.toString());
		}
		public List<InterfaceDef> getInterfaces() {
			return (List<InterfaceDef>)devices.get(InterfaceDef.class.toString());
		}
		
	}
	public static class DiskDef {
		enum deviceType {
			FLOOPY("floopy"),
			DISK("disk"),
			CDROM("cdrom");
			String _type;
			deviceType(String type) {
				_type = type;
			}
			@Override
            public String toString() {
				return _type;
			}
		}
		enum diskType {
			FILE("file"),
			BLOCK("block"),
			DIRECTROY("dir");
			String _diskType;
			diskType(String type) {
				_diskType = type;
			}
			@Override
            public String toString() {
				return _diskType;
			}
		}
		enum diskBus {
			IDE("ide"),
			SCSI("scsi"),
			VIRTIO("virtio"),
			XEN("xen"),
			USB("usb"),
			UML("uml"),
			FDC("fdc");
			String _bus;
			diskBus(String bus) {
				_bus = bus;
			}
			@Override
            public String toString() {
				return _bus;
			}
		}
		enum diskFmtType {
			RAW("raw"),
			QCOW2("qcow2");
			String _fmtType;
			diskFmtType(String fmt) {
				_fmtType = fmt;
			}
			@Override
            public String toString() {
				return _fmtType;
			}
		}
		
		private deviceType _deviceType; /*floppy, disk, cdrom*/
		private diskType _diskType;
		private String _sourcePath;
		private String _diskLabel;
		private diskBus _bus;
		private diskFmtType _diskFmtType; /*qcow2, raw etc.*/
		private boolean _readonly = false;
		private boolean _shareable = false;
		private boolean _deferAttach = false;
		public void setDeviceType(deviceType deviceType) {
			_deviceType = deviceType;
		}
		public void defFileBasedDisk(String filePath, String diskLabel, diskBus bus, diskFmtType diskFmtType) {
			_diskType = diskType.FILE;
			_deviceType = deviceType.DISK;
			_sourcePath = filePath;
			_diskLabel = diskLabel;
			_diskFmtType = diskFmtType;
			_bus = bus;

		}
		/*skip iso label*/
		private String getDevLabel(int devId, diskBus bus) {
			if ( devId == 2 ) {
				devId++;
			}
			
			char suffix = (char)('a' + devId);
			if (bus == diskBus.SCSI) {
				return "sd" + suffix;
			} else if (bus == diskBus.VIRTIO) {
				return "vd" + suffix;
			}
			return "hd" + suffix;
			
		}
		
		public void defFileBasedDisk(String filePath, int devId, diskBus bus, diskFmtType diskFmtType) {
			
			_diskType = diskType.FILE;
			_deviceType = deviceType.DISK;
			_sourcePath = filePath;
			_diskLabel = getDevLabel(devId, bus);
			_diskFmtType = diskFmtType;
			_bus = bus;

		}
		public void defISODisk(String volPath) {
			_diskType = diskType.FILE;
			_deviceType = deviceType.CDROM;
			_sourcePath = volPath;
			_diskLabel = "hdc";
			_diskFmtType = diskFmtType.RAW;
			_bus = diskBus.IDE;
		}
		public void defBlockBasedDisk(String diskName, String diskLabel, diskBus bus) {
			_diskType = diskType.BLOCK;
			_deviceType = deviceType.DISK;
			_sourcePath = diskName;
			_diskLabel = diskLabel;
			_diskFmtType = diskFmtType.RAW;
			_bus = bus;
		}
		public void defBlockBasedDisk(String diskName, int devId, diskBus bus) {
			_diskType = diskType.BLOCK;
			_deviceType = deviceType.DISK;
			_sourcePath = diskName;
			_diskLabel = getDevLabel(devId, bus);
			_diskFmtType = diskFmtType.RAW;
			_bus = bus;
		}
		public void setReadonly() {
			_readonly = true;
		}
		public void setSharable() {
			_shareable = true;
		}
		public void setAttachDeferred(boolean deferAttach) {
			_deferAttach = deferAttach;
		}
		public boolean isAttachDeferred() {
			return _deferAttach;
		}
		public String getDiskPath() {
			return _sourcePath;
		}
		public String getDiskLabel() {
			return _diskLabel;
		}
		public deviceType getDeviceType() {
			return _deviceType;
		}
		public void setDiskPath(String volPath) {
			this._sourcePath = volPath;
		}
		public diskBus getBusType() {
			return _bus;
		}
		public int getDiskSeq() {
			char suffix = this._diskLabel.charAt(this._diskLabel.length() - 1);
			return suffix - 'a';
		}
		@Override
        public String toString() {
			StringBuilder diskBuilder = new StringBuilder();
			diskBuilder.append("<disk ");
			if (_deviceType != null) {
				diskBuilder.append(" device='" + _deviceType + "'");
			}
			diskBuilder.append(" type='" + _diskType + "'");
			diskBuilder.append(">\n");
			diskBuilder.append("<driver name='qemu'" + " type='" + _diskFmtType + "' cache='none' " +"/>\n");
			if (_diskType == diskType.FILE) {
				diskBuilder.append("<source ");
				if (_sourcePath != null) {
					diskBuilder.append("file='" + _sourcePath + "'");
				} else if (_deviceType == deviceType.CDROM) {
					diskBuilder.append("file=''");
				}
				diskBuilder.append("/>\n");
			} else if (_diskType == diskType.BLOCK) {
				diskBuilder.append("<source");
				if (_sourcePath != null) {
					diskBuilder.append(" dev='" + _sourcePath + "'");
				}
				diskBuilder.append("/>\n");
			}
			diskBuilder.append("<target dev='" + _diskLabel + "'");
			if (_bus != null) {
				diskBuilder.append(" bus='" + _bus + "'");
			}
			diskBuilder.append("/>\n");
			diskBuilder.append("</disk>\n");
			return diskBuilder.toString();
		}
	}
	
	public static class InterfaceDef {
		enum guestNetType {
			BRIDGE("bridge"),
			NETWORK("network"),
			USER("user"),
			ETHERNET("ethernet"),
			INTERNAL("internal");
			String _type;
			guestNetType(String type) {
				_type = type;
			}
			@Override
            public String toString() {
				return _type;
			}
		}
		enum nicModel {
			E1000("e1000"),
			VIRTIO("virtio"),
			RTL8139("rtl8139"),
			NE2KPCI("ne2k_pci");
			String _model;
			nicModel(String model) {
				_model = model;
			}
			@Override
            public String toString() {
				return _model;
			}
		}
		enum hostNicType {
			DIRECT_ATTACHED_WITHOUT_DHCP,
			DIRECT_ATTACHED_WITH_DHCP,
			VNET,
			VLAN;
		}
		private guestNetType _netType; /*bridge, ethernet, network, user, internal*/
		private hostNicType _hostNetType; /*Only used by agent java code*/
		private String _sourceName;
		private String _networkName;
		private String _macAddr;
		private String _ipAddr;
		private String _scriptPath;
		private nicModel _model;
		public void defBridgeNet(String brName, String targetBrName, String macAddr, nicModel model) {
			_netType = guestNetType.BRIDGE;
			_sourceName = brName;
			_networkName = targetBrName;
			_macAddr = macAddr;
			_model = model;
		}
		public void defPrivateNet(String networkName, String targetName, String macAddr, nicModel model) {
			_netType = guestNetType.NETWORK;
			_sourceName = networkName;
			_networkName = targetName;
			_macAddr = macAddr;
			_model = model;
		}
		
		public void setHostNetType(hostNicType hostNetType) {
			_hostNetType = hostNetType;
		}
		
		public hostNicType getHostNetType() {
			return _hostNetType;
		}
		
		public String getBrName() {
			return _sourceName;
		}
		public guestNetType getNetType() {
			return _netType;
		}
		public String getDevName() {
		    return _networkName;
		}
		public String getMacAddress() {
		    return _macAddr;
		}
		
		@Override
        public String toString() {
			StringBuilder netBuilder = new StringBuilder();
			netBuilder.append("<interface type='" + _netType +"'>\n");
			if (_netType == guestNetType.BRIDGE) {
				netBuilder.append("<source bridge='" + _sourceName +"'/>\n");
			} else if (_netType == guestNetType.NETWORK) {
				netBuilder.append("<source network='" + _sourceName +"'/>\n");
			}
			if (_networkName !=null) {
				netBuilder.append("<target dev='" + _networkName + "'/>\n");
			}
			if (_macAddr !=null) {
				netBuilder.append("<mac address='" + _macAddr + "'/>\n");
			}
			if (_model !=null) {
				netBuilder.append("<model type='" + _model + "'/>\n");
			}
			netBuilder.append("</interface>\n");
			return netBuilder.toString();
		}
	}
	public static class ConsoleDef {
		private final String _ttyPath;
		private final String _type;
		private final String _source;
		private short _port = -1;
		public ConsoleDef(String type, String path, String source, short port) {
			_type = type;
			_ttyPath = path;
			_source = source;
			_port = port;
		}
		@Override
        public String toString() {
			StringBuilder consoleBuilder = new StringBuilder();
			consoleBuilder.append("<console ");
			consoleBuilder.append("type='" + _type + "'");
			if (_ttyPath != null) {
				consoleBuilder.append("tty='" + _ttyPath + "'");
			}
			consoleBuilder.append(">\n");
			if (_source != null) {
				consoleBuilder.append("<source path='" + _source + "'/>\n");
			}
			if (_port != -1) {
				consoleBuilder.append("<target port='" + _port + "'/>\n");
			}
			consoleBuilder.append("</console>\n");
			return consoleBuilder.toString();
		}
	}
	public static class SerialDef {
		private final String _type;
		private final String _source;
		private short _port = -1;
		public SerialDef(String type, String source, short port) {
			_type = type;
			_source = source;
			_port = port;
		}
		@Override
        public String toString() {
			StringBuilder serialBuidler = new StringBuilder();
			serialBuidler.append("<serial type='" + _type + "'>\n");
			if (_source != null) {
				serialBuidler.append("<source path='" + _source + "'/>\n");
			}
			if (_port != -1) {
				serialBuidler.append("<target port='" + _port + "'/>\n");
			}
			serialBuidler.append("</serial>\n");
			return serialBuidler.toString();
		}
	}
	public  static class GraphicDef {
		private final String _type;
		private short _port = -2;
		private boolean _autoPort = false;
		private final String _listenAddr;
		private final String _passwd;
		private final String _keyMap;
		public GraphicDef(String type, short port, boolean auotPort, String listenAddr, String passwd, String keyMap) {
			_type = type;
			_port = port;
			_autoPort = auotPort;
			_listenAddr = listenAddr;
			_passwd = passwd;
			_keyMap = keyMap;
		}
		@Override
        public String toString() {
			StringBuilder graphicBuilder = new StringBuilder();
			graphicBuilder.append("<graphics type='" + _type + "'");
			if (_autoPort) {
				graphicBuilder.append(" autoport='yes'");
			} else if (_port != -2){
				graphicBuilder.append(" port='" + _port + "'");
			}
			if (_listenAddr != null) {
				graphicBuilder.append(" listen='" + _listenAddr + "'");
			} else {
				graphicBuilder.append(" listen='' ");
			}
			if (_passwd != null) {
				graphicBuilder.append(" passwd='" + _passwd + "'");
			} else if (_keyMap != null) {
				graphicBuilder.append(" _keymap='" + _keyMap + "'");
			}
			graphicBuilder.append("/>\n");
			return graphicBuilder.toString();
		}
	}
	public  static class InputDef {
		private final String _type; /*tablet, mouse*/
		private final String _bus; /*ps2, usb, xen*/
		public InputDef(String type, String bus) {
			_type = type;
			_bus = bus;
		}
		@Override
        public String toString() {
			StringBuilder inputBuilder = new StringBuilder();
			inputBuilder.append("<input type='" + _type + "'");
			if (_bus != null) {
				inputBuilder.append(" bus='" + _bus + "'");
			}
			inputBuilder.append("/>\n");
			return inputBuilder.toString();
		}
	}
	public void setHvsType(String hvs) {
		_hvsType = hvs;
	}
	public void setDomainName(String domainName) {
		_domName = domainName;
	}
	public void setDomUUID(String uuid) {
		_domUUID = uuid;
	}
	public void setDomDescription(String desc) {
		_desc = desc;
	}
	public String getGuestOSType() {
		return _desc;
	}
	public void addComp(Object comp) {
		components.put(comp.getClass().toString(), comp);
	}
	public DevicesDef getDevices() {
		Object o = components.get(DevicesDef.class.toString());
		if (o != null) {
			return (DevicesDef)o;
		}
		return null;
	}
	@Override
    public String toString() {
		StringBuilder vmBuilder = new StringBuilder();
		vmBuilder.append("<domain type='" + _hvsType + "'>\n");
		vmBuilder.append("<name>" + _domName + "</name>\n");
		if (_domUUID != null) {
			vmBuilder.append("<uuid>" + _domUUID + "</uuid>\n");
		}
		if (_desc != null ) {
			vmBuilder.append("<description>" + _desc + "</description>\n");
		}
		for (Object o : components.values()) {
			vmBuilder.append(o.toString());
		}
		vmBuilder.append("</domain>\n");
		return vmBuilder.toString();
	}
	
	public static void main(String [] args){
		System.out.println("testing");
		LibvirtVMDef vm = new LibvirtVMDef();
		vm.setHvsType("kvm");
		vm.setDomainName("testing");
		vm.setDomUUID(UUID.randomUUID().toString());
		
		GuestDef guest = new GuestDef();
		guest.setGuestType(GuestDef.guestType.KVM);
		guest.setGuestArch("x86_64");
		guest.setMachineType("pc-0.11");
		guest.setBootOrder(GuestDef.bootOrder.HARDISK);
		vm.addComp(guest);
		
		GuestResourceDef grd = new GuestResourceDef();
		grd.setMemorySize(512*1024);
		grd.setVcpuNum(1);
		vm.addComp(grd);
		
		FeaturesDef features = new FeaturesDef();
		features.addFeatures("pae");
		features.addFeatures("apic");
		features.addFeatures("acpi");
		vm.addComp(features);
		
		TermPolicy term = new TermPolicy();
		term.setCrashPolicy("destroy");
		term.setPowerOffPolicy("destroy");
		term.setRebootPolicy("destroy");
		vm.addComp(term);
		
		DevicesDef devices = new DevicesDef();
		devices.setEmulatorPath("/usr/bin/cloud-qemu-system-x86_64");
		
		DiskDef hda = new DiskDef();
		hda.defFileBasedDisk("/path/to/hda1", 0, DiskDef.diskBus.VIRTIO, DiskDef.diskFmtType.QCOW2);
		devices.addDevice(hda);
		
		DiskDef hdb = new DiskDef();
		hdb.defFileBasedDisk("/path/to/hda2", 1,  DiskDef.diskBus.VIRTIO, DiskDef.diskFmtType.QCOW2);
		devices.addDevice(hdb);
		
		InterfaceDef pubNic = new InterfaceDef();
		pubNic.defBridgeNet("cloudbr0", "vnet1", "00:16:3e:77:e2:a1", InterfaceDef.nicModel.VIRTIO);
		devices.addDevice(pubNic);
		
		InterfaceDef privNic = new InterfaceDef();
		privNic.defPrivateNet("cloud-private", null, "00:16:3e:77:e2:a2", InterfaceDef.nicModel.VIRTIO);
		devices.addDevice(privNic);
		
		InterfaceDef vlanNic = new InterfaceDef();
		vlanNic.defBridgeNet("vnbr1000", "tap1", "00:16:3e:77:e2:a2", InterfaceDef.nicModel.VIRTIO);
		devices.addDevice(vlanNic);
		
		SerialDef serial = new SerialDef("pty", null, (short)0);
		devices.addDevice(serial);
		
		ConsoleDef console = new ConsoleDef("pty", null, null, (short)0);
		devices.addDevice(console);
		
		GraphicDef grap = new GraphicDef("vnc", (short)0, true, null, null, null);
		devices.addDevice(grap);
		
		InputDef input = new InputDef("tablet", "usb");
		devices.addDevice(input);
		
		vm.addComp(devices);
		
		System.out.println(vm.toString());
	}

}
