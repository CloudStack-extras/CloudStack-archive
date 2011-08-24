/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.test.ui;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import com.cloud.test.ui.AbstractSeleniumTestCase;
import com.thoughtworks.selenium.SeleniumException;

public class UIScenarioTest extends AbstractSeleniumTestCase {

	@Test
	public void testLoginStartStopVMScenario() throws Exception {
		try {
			selenium.open("/client/");
			selenium.type("account_username", "admin");
			selenium.type("account_password", "password");
			selenium.click("loginbutton");
			Thread.sleep(3000);
			assertTrue(selenium.isTextPresent("admin"));
			selenium.click("//div[@id='leftmenu_instances']/div");
			selenium
					.click("//div[@id='leftmenu_instances_stopped_instances']/div/span");

			Thread.sleep(3000);
			selenium.click("//div[@id='midmenu_startvm_link']/div/div[2]");
			selenium.click("//div[39]/div[11]/button[1]");

			for (int second = 0;; second++) {
				if (second >= 60)
					fail("timeout");
				try {
					if (selenium.isVisible("//div/p[@id='after_action_info']"))
						break;
				} catch (Exception e) {
				}
				Thread.sleep(10000);
			}
			assertTrue(selenium
					.isTextPresent("Start Instance action succeeded"));
			selenium
					.click("//div[@id='leftmenu_instances_running_instances']/div/span");

			Thread.sleep(3000);
			selenium.click("//div[@id='midmenu_stopvm_link']/div/div[2]");
			selenium.click("//div[39]/div[11]/button[1]");
			for (int second = 0;; second++) {
				if (second >= 60)
					fail("timeout");
				try {
					if (selenium.isVisible("//div/p[@id='after_action_info']"))
						break;
				} catch (Exception e) {
				}
				Thread.sleep(10000);
			}

			assertTrue(selenium.isTextPresent("Stop Instance action succeeded"));
			selenium.click("main_logout");
			selenium.waitForPageToLoad("30000");
			assertTrue(selenium.isTextPresent("Welcome to Management Console"));

		} catch (SeleniumException ex) {
			fail(ex.getMessage());
			System.err.println(ex.getMessage());
			throw ex;
		}
	}
}
