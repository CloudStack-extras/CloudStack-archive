package com.cloud.ovm.hypervisor;

import java.util.HashMap;

public class OvmHelper {
	 private static final HashMap<String, String> _ovmMap = new HashMap<String, String>();
	 
	 public static final String ORACLE_LINUX = "Oracle Linux";
	 public static final String WINDOWS = "Windows";
	 
	 static {
	    _ovmMap.put("Oracle Enterprise Linux 6.0 (32-bit)", ORACLE_LINUX);
	    _ovmMap.put("Oracle Enterprise Linux 6.0 (64-bit)", ORACLE_LINUX);
		_ovmMap.put("Oracle Enterprise Linux 5.0 (32-bit)", ORACLE_LINUX);
		_ovmMap.put("Oracle Enterprise Linux 5.0 (64-bit)", ORACLE_LINUX);
		_ovmMap.put("Oracle Enterprise Linux 5.1 (32-bit)", ORACLE_LINUX);
		_ovmMap.put("Oracle Enterprise Linux 5.1 (64-bit)", ORACLE_LINUX);
		_ovmMap.put("Oracle Enterprise Linux 5.2 (32-bit)", ORACLE_LINUX);
		_ovmMap.put("Oracle Enterprise Linux 5.2 (64-bit)", ORACLE_LINUX);
		_ovmMap.put("Oracle Enterprise Linux 5.3 (32-bit)", ORACLE_LINUX);
		_ovmMap.put("Oracle Enterprise Linux 5.3 (64-bit)", ORACLE_LINUX);
		_ovmMap.put("Oracle Enterprise Linux 5.4 (32-bit)", ORACLE_LINUX);
		_ovmMap.put("Oracle Enterprise Linux 5.4 (64-bit)", ORACLE_LINUX);
		_ovmMap.put("Oracle Enterprise Linux 5.5 (32-bit)", ORACLE_LINUX);
		_ovmMap.put("Oracle Enterprise Linux 5.5 (64-bit)", ORACLE_LINUX);
		_ovmMap.put("Windows 7 (32-bit)", WINDOWS);
		_ovmMap.put("Windows 7 (64-bit)", WINDOWS);
		_ovmMap.put("Windows Server 2003 (32-bit)", WINDOWS);
		_ovmMap.put("Windows Server 2003 (64-bit)", WINDOWS);
		_ovmMap.put("Windows Server 2008 (32-bit)", WINDOWS);
		_ovmMap.put("Windows Server 2008 (64-bit)", WINDOWS);
		_ovmMap.put("Windows Server 2008 R2 (64-bit)", WINDOWS);
		_ovmMap.put("Windows 2000 SP4 (32-bit)", WINDOWS);
		_ovmMap.put("Windows Vista (32-bit)", WINDOWS);
		_ovmMap.put("Windows XP SP2 (32-bit)", WINDOWS);
		_ovmMap.put("Windows XP SP3 (32-bit)", WINDOWS);
	}
	 
	public static String getOvmGuestType(String stdType) {
		return _ovmMap.get(stdType);
	}
}
