/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.hypervisor.vmware.resource;

import org.apache.log4j.Logger;

import com.cloud.hypervisor.vmware.manager.VmwareManager;
import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.cloud.utils.StringUtils;
import com.cloud.utils.component.ComponentLocator;
import com.vmware.apputils.version.ExtendedAppUtil;

public class VmwareContextFactory {
	
    private static final Logger s_logger = Logger.getLogger(VmwareContextFactory.class);
	
	private static volatile int s_seq = 1;
	private static VmwareManager s_vmwareMgr;
	
	static {
		// skip certificate check
		System.setProperty("axis.socketSecureFactory", "org.apache.axis.components.net.SunFakeTrustSocketFactory");
		
		ComponentLocator locator = ComponentLocator.getLocator("management-server");
		s_vmwareMgr = locator.getManager(VmwareManager.class);
	}

	public static VmwareContext create(String vCenterAddress, String vCenterUserName, String vCenterPassword) throws Exception {
		assert(vCenterAddress != null);
		assert(vCenterUserName != null);
		assert(vCenterPassword != null);

		String serviceUrl = "https://" + vCenterAddress + "/sdk/vimService";
		String[] params = new String[] {"--url", serviceUrl, "--username", vCenterUserName, "--password", vCenterPassword };

		if(s_logger.isDebugEnabled())
			s_logger.debug("initialize VmwareContext. url: " + serviceUrl + ", username: " + vCenterUserName + ", password: " + StringUtils.getMaskedPasswordForDisplay(vCenterPassword));
			
		ExtendedAppUtil appUtil = ExtendedAppUtil.initialize(vCenterAddress + "-" + s_seq++, params);
		
		appUtil.connect();
		VmwareContext context = new VmwareContext(appUtil, vCenterAddress);
		context.registerStockObject(VmwareManager.CONTEXT_STOCK_NAME, s_vmwareMgr);
		
		// this is ugly, it is relatively easier to let cleasses in vmware-base library to access
		context.registerStockObject("serviceconsole", s_vmwareMgr.getServiceConsolePortGroupName());
		context.registerStockObject("manageportgroup", s_vmwareMgr.getManagementPortGroupName());

		return context;
	}
}
