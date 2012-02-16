/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.hypervisor.vmware.util;

import java.util.HashMap;
import java.util.Map;

import com.vmware.vim25.VirtualMachineGuestOsIdentifier;

public class VmwareGuestOsMapper {
	private static Map<String, VirtualMachineGuestOsIdentifier> s_mapper = new HashMap<String, VirtualMachineGuestOsIdentifier>();
	static {
		s_mapper.put("DOS", VirtualMachineGuestOsIdentifier.dosGuest);
		s_mapper.put("OS/2", VirtualMachineGuestOsIdentifier.os2Guest);

		s_mapper.put("Windows 3.1", VirtualMachineGuestOsIdentifier.win31Guest);
		s_mapper.put("Windows 95", VirtualMachineGuestOsIdentifier.win95Guest);
		s_mapper.put("Windows 98", VirtualMachineGuestOsIdentifier.win98Guest);
		s_mapper.put("Windows NT 4", VirtualMachineGuestOsIdentifier.winNTGuest);
		s_mapper.put("Windows XP (32-bit)", VirtualMachineGuestOsIdentifier.winXPProGuest);
		s_mapper.put("Windows XP (64-bit)", VirtualMachineGuestOsIdentifier.winXPPro64Guest);
		s_mapper.put("Windows XP SP2 (32-bit)", VirtualMachineGuestOsIdentifier.winXPProGuest);
		s_mapper.put("Windows XP SP3 (32-bit)", VirtualMachineGuestOsIdentifier.winXPProGuest);
		s_mapper.put("Windows Vista (32-bit)", VirtualMachineGuestOsIdentifier.winVistaGuest);
		s_mapper.put("Windows Vista (64-bit)", VirtualMachineGuestOsIdentifier.winVista64Guest);
		s_mapper.put("Windows 7 (32-bit)", VirtualMachineGuestOsIdentifier.windows7Guest);
		s_mapper.put("Windows 7 (64-bit)", VirtualMachineGuestOsIdentifier.windows7_64Guest);

		s_mapper.put("Windows 2000 Professional", VirtualMachineGuestOsIdentifier.win2000ProGuest);
		s_mapper.put("Windows 2000 Server", VirtualMachineGuestOsIdentifier.win2000ServGuest);
		s_mapper.put("Windows 2000 Server SP4 (32-bit)", VirtualMachineGuestOsIdentifier.win2000ServGuest);
		s_mapper.put("Windows 2000 Advanced Server", VirtualMachineGuestOsIdentifier.win2000AdvServGuest);
		
		s_mapper.put("Windows Server 2003 Enterprise Edition(32-bit)", VirtualMachineGuestOsIdentifier.winNetEnterpriseGuest);
		s_mapper.put("Windows Server 2003 Enterprise Edition(64-bit)", VirtualMachineGuestOsIdentifier.winNetEnterprise64Guest);
		s_mapper.put("Windows Server 2008 R2 (64-bit)", VirtualMachineGuestOsIdentifier.winLonghorn64Guest);
		s_mapper.put("Windows Server 2003 DataCenter Edition(32-bit)", VirtualMachineGuestOsIdentifier.winNetDatacenterGuest);
		s_mapper.put("Windows Server 2003 DataCenter Edition(64-bit)", VirtualMachineGuestOsIdentifier.winNetDatacenter64Guest);
		s_mapper.put("Windows Server 2003 Standard Edition(32-bit)", VirtualMachineGuestOsIdentifier.winNetStandardGuest);
		s_mapper.put("Windows Server 2003 Standard Edition(64-bit)", VirtualMachineGuestOsIdentifier.winNetStandard64Guest);
		s_mapper.put("Windows Server 2003 Web Edition", VirtualMachineGuestOsIdentifier.winNetWebGuest);
		s_mapper.put("Microsoft Small Bussiness Server 2003", VirtualMachineGuestOsIdentifier.winNetBusinessGuest);
		
		s_mapper.put("Windows Server 2008 (32-bit)", VirtualMachineGuestOsIdentifier.winLonghornGuest);
		s_mapper.put("Windows Server 2008 (64-bit)", VirtualMachineGuestOsIdentifier.winLonghorn64Guest);
		
		s_mapper.put("Open Enterprise Server", VirtualMachineGuestOsIdentifier.oesGuest);
		
		s_mapper.put("Asianux 3(32-bit)", VirtualMachineGuestOsIdentifier.asianux3Guest);
		s_mapper.put("Asianux 3(64-bit)", VirtualMachineGuestOsIdentifier.asianux3_64Guest);
		
		s_mapper.put("Debian GNU/Linux 5(64-bit)", VirtualMachineGuestOsIdentifier.debian5_64Guest);
        s_mapper.put("Debian GNU/Linux 5.0 (32-bit)", VirtualMachineGuestOsIdentifier.debian5Guest);
		s_mapper.put("Debian GNU/Linux 4(32-bit)", VirtualMachineGuestOsIdentifier.debian4Guest);
		s_mapper.put("Debian GNU/Linux 4(64-bit)", VirtualMachineGuestOsIdentifier.debian4_64Guest);
		
		s_mapper.put("Novell Netware 6.x", VirtualMachineGuestOsIdentifier.netware6Guest);
		s_mapper.put("Novell Netware 5.1", VirtualMachineGuestOsIdentifier.netware5Guest);
		
		s_mapper.put("Sun Solaris 10(32-bit)", VirtualMachineGuestOsIdentifier.solaris10Guest);
		s_mapper.put("Sun Solaris 10(64-bit)", VirtualMachineGuestOsIdentifier.solaris10_64Guest);
		s_mapper.put("Sun Solaris 9(Experimental)", VirtualMachineGuestOsIdentifier.solaris9Guest);
		s_mapper.put("Sun Solaris 8(Experimental)", VirtualMachineGuestOsIdentifier.solaris8Guest);
		
		s_mapper.put("FreeBSD (32-bit)", VirtualMachineGuestOsIdentifier.freebsdGuest);
		s_mapper.put("FreeBSD (64-bit)", VirtualMachineGuestOsIdentifier.freebsd64Guest);
		
		s_mapper.put("SCO OpenServer 5", VirtualMachineGuestOsIdentifier.otherGuest);
		s_mapper.put("SCO UnixWare 7", VirtualMachineGuestOsIdentifier.unixWare7Guest);
		
		s_mapper.put("SUSE Linux Enterprise 8(32-bit)", VirtualMachineGuestOsIdentifier.suseGuest);
		s_mapper.put("SUSE Linux Enterprise 8(64-bit)", VirtualMachineGuestOsIdentifier.suse64Guest);
		s_mapper.put("SUSE Linux Enterprise 9(32-bit)", VirtualMachineGuestOsIdentifier.suseGuest);
		s_mapper.put("SUSE Linux Enterprise 9(64-bit)", VirtualMachineGuestOsIdentifier.suse64Guest);
		s_mapper.put("SUSE Linux Enterprise 10(32-bit)", VirtualMachineGuestOsIdentifier.suseGuest);
		s_mapper.put("SUSE Linux Enterprise 10(64-bit)", VirtualMachineGuestOsIdentifier.suse64Guest);
		s_mapper.put("SUSE Linux Enterprise 10(32-bit)", VirtualMachineGuestOsIdentifier.suseGuest);
		s_mapper.put("Other SUSE Linux(32-bit)", VirtualMachineGuestOsIdentifier.suseGuest);
		s_mapper.put("Other SUSE Linux(64-bit)", VirtualMachineGuestOsIdentifier.suse64Guest);
		
		s_mapper.put("CentOS 4.5 (32-bit)", VirtualMachineGuestOsIdentifier.centosGuest);
		s_mapper.put("CentOS 4.6 (32-bit)", VirtualMachineGuestOsIdentifier.centosGuest);
		s_mapper.put("CentOS 4.7 (32-bit)", VirtualMachineGuestOsIdentifier.centosGuest);
		s_mapper.put("CentOS 4.8 (32-bit)", VirtualMachineGuestOsIdentifier.centosGuest);
		s_mapper.put("CentOS 5.0 (32-bit)", VirtualMachineGuestOsIdentifier.centosGuest);
		s_mapper.put("CentOS 5.0 (64-bit)", VirtualMachineGuestOsIdentifier.centos64Guest);
		s_mapper.put("CentOS 5.1 (32-bit)", VirtualMachineGuestOsIdentifier.centosGuest);
		s_mapper.put("CentOS 5.1 (64-bit)", VirtualMachineGuestOsIdentifier.centos64Guest);
		s_mapper.put("CentOS 5.2 (32-bit)", VirtualMachineGuestOsIdentifier.centosGuest);
		s_mapper.put("CentOS 5.2 (64-bit)", VirtualMachineGuestOsIdentifier.centos64Guest);
		s_mapper.put("CentOS 5.3 (32-bit)", VirtualMachineGuestOsIdentifier.centosGuest);
		s_mapper.put("CentOS 5.3 (64-bit)", VirtualMachineGuestOsIdentifier.centos64Guest);
		s_mapper.put("CentOS 5.4 (32-bit)", VirtualMachineGuestOsIdentifier.centosGuest);
		s_mapper.put("CentOS 5.4 (64-bit)", VirtualMachineGuestOsIdentifier.centos64Guest);
		s_mapper.put("CentOS 5.5 (32-bit)", VirtualMachineGuestOsIdentifier.centosGuest);
		s_mapper.put("CentOS 5.5 (64-bit)", VirtualMachineGuestOsIdentifier.centos64Guest);
		s_mapper.put("CentOS 5.6 (32-bit)", VirtualMachineGuestOsIdentifier.centosGuest);
		s_mapper.put("CentOS 5.6 (64-bit)", VirtualMachineGuestOsIdentifier.centos64Guest);
		s_mapper.put("CentOS 6.0 (32-bit)", VirtualMachineGuestOsIdentifier.centosGuest);
		s_mapper.put("CentOS 6.0 (64-bit)", VirtualMachineGuestOsIdentifier.centos64Guest);
		s_mapper.put("Other CentOS (32-bit)", VirtualMachineGuestOsIdentifier.centosGuest);
		s_mapper.put("Other CentOS (64-bit)", VirtualMachineGuestOsIdentifier.centos64Guest);
		
		s_mapper.put("Red Hat Enterprise Linux 2", VirtualMachineGuestOsIdentifier.rhel2Guest);
		s_mapper.put("Red Hat Enterprise Linux 3(32-bit)", VirtualMachineGuestOsIdentifier.rhel3Guest);
		s_mapper.put("Red Hat Enterprise Linux 3(64-bit)", VirtualMachineGuestOsIdentifier.rhel3_64Guest);
		s_mapper.put("Red Hat Enterprise Linux 4(32-bit)", VirtualMachineGuestOsIdentifier.rhel4Guest);
		s_mapper.put("Red Hat Enterprise Linux 4(64-bit)", VirtualMachineGuestOsIdentifier.rhel4_64Guest);
		s_mapper.put("Red Hat Enterprise Linux 5(32-bit)", VirtualMachineGuestOsIdentifier.rhel5Guest);
		s_mapper.put("Red Hat Enterprise Linux 5(64-bit)", VirtualMachineGuestOsIdentifier.rhel5_64Guest);
		s_mapper.put("Red Hat Enterprise Linux 6(32-bit)", VirtualMachineGuestOsIdentifier.rhel6Guest);
		s_mapper.put("Red Hat Enterprise Linux 6(64-bit)", VirtualMachineGuestOsIdentifier.rhel6_64Guest);
		
		s_mapper.put("Red Hat Enterprise Linux 4.5 (32-bit)", VirtualMachineGuestOsIdentifier.rhel4Guest);
		s_mapper.put("Red Hat Enterprise Linux 4.6 (32-bit)", VirtualMachineGuestOsIdentifier.rhel4Guest);
		s_mapper.put("Red Hat Enterprise Linux 4.7 (32-bit)", VirtualMachineGuestOsIdentifier.rhel4Guest);
		s_mapper.put("Red Hat Enterprise Linux 4.8 (32-bit)", VirtualMachineGuestOsIdentifier.rhel4Guest);
		s_mapper.put("Red Hat Enterprise Linux 5.0(32-bit)", VirtualMachineGuestOsIdentifier.rhel5Guest);
		s_mapper.put("Red Hat Enterprise Linux 5.0(64-bit)", VirtualMachineGuestOsIdentifier.rhel5_64Guest);
		s_mapper.put("Red Hat Enterprise Linux 5.1(32-bit)", VirtualMachineGuestOsIdentifier.rhel5Guest);
		s_mapper.put("Red Hat Enterprise Linux 5.1(64-bit)", VirtualMachineGuestOsIdentifier.rhel5_64Guest);
		s_mapper.put("Red Hat Enterprise Linux 5.2(32-bit)", VirtualMachineGuestOsIdentifier.rhel5Guest);
		s_mapper.put("Red Hat Enterprise Linux 5.2(64-bit)", VirtualMachineGuestOsIdentifier.rhel5_64Guest);
		s_mapper.put("Red Hat Enterprise Linux 5.3(32-bit)", VirtualMachineGuestOsIdentifier.rhel5Guest);
		s_mapper.put("Red Hat Enterprise Linux 5.3(64-bit)", VirtualMachineGuestOsIdentifier.rhel5_64Guest);
		s_mapper.put("Red Hat Enterprise Linux 5.4(32-bit)", VirtualMachineGuestOsIdentifier.rhel5Guest);
		s_mapper.put("Red Hat Enterprise Linux 5.4(64-bit)", VirtualMachineGuestOsIdentifier.rhel5_64Guest);
		
		s_mapper.put("Ubuntu 8.04 (32-bit)", VirtualMachineGuestOsIdentifier.ubuntuGuest);
		s_mapper.put("Ubuntu 8.04 (64-bit)", VirtualMachineGuestOsIdentifier.ubuntu64Guest);
		s_mapper.put("Ubuntu 8.10 (32-bit)", VirtualMachineGuestOsIdentifier.ubuntuGuest);
		s_mapper.put("Ubuntu 8.10 (64-bit)", VirtualMachineGuestOsIdentifier.ubuntu64Guest);
		s_mapper.put("Ubuntu 9.04 (32-bit)", VirtualMachineGuestOsIdentifier.ubuntuGuest);
		s_mapper.put("Ubuntu 9.04 (64-bit)", VirtualMachineGuestOsIdentifier.ubuntu64Guest);
		s_mapper.put("Ubuntu 9.10 (32-bit)", VirtualMachineGuestOsIdentifier.ubuntuGuest);
		s_mapper.put("Ubuntu 9.10 (64-bit)", VirtualMachineGuestOsIdentifier.ubuntu64Guest);
		s_mapper.put("Ubuntu 10.04 (32-bit)", VirtualMachineGuestOsIdentifier.ubuntuGuest);
		s_mapper.put("Ubuntu 10.04 (64-bit)", VirtualMachineGuestOsIdentifier.ubuntu64Guest);
		s_mapper.put("Ubuntu 10.10 (32-bit)", VirtualMachineGuestOsIdentifier.ubuntuGuest);
		s_mapper.put("Ubuntu 10.10 (64-bit)", VirtualMachineGuestOsIdentifier.ubuntu64Guest);
		s_mapper.put("Other Ubuntu (32-bit)", VirtualMachineGuestOsIdentifier.ubuntuGuest);
		s_mapper.put("Other Ubuntu (64-bit)", VirtualMachineGuestOsIdentifier.ubuntu64Guest);

		s_mapper.put("Other 2.6x Linux (32-bit)", VirtualMachineGuestOsIdentifier.other26xLinuxGuest);
		s_mapper.put("Other 2.6x Linux (64-bit)", VirtualMachineGuestOsIdentifier.other26xLinux64Guest);
		s_mapper.put("Other Linux (32-bit)", VirtualMachineGuestOsIdentifier.otherLinuxGuest);
		s_mapper.put("Other Linux (64-bit)", VirtualMachineGuestOsIdentifier.otherLinux64Guest);
		
		s_mapper.put("Other (32-bit)", VirtualMachineGuestOsIdentifier.otherGuest);
		s_mapper.put("Other (64-bit)", VirtualMachineGuestOsIdentifier.otherGuest64);
	}
	
	public static VirtualMachineGuestOsIdentifier getGuestOsIdentifier(String guestOsName) {
		return s_mapper.get(guestOsName);
	}
}
