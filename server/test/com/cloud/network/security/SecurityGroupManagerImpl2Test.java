package com.cloud.network.security;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import com.cloud.agent.MockAgentManagerImpl;
import com.cloud.configuration.DefaultInterceptorLibrary;
import com.cloud.configuration.dao.ConfigurationDaoImpl;
import com.cloud.domain.dao.DomainDaoImpl;
import com.cloud.network.MockNetworkManagerImpl;
import com.cloud.network.security.dao.IngressRuleDaoImpl;
import com.cloud.network.security.dao.SecurityGroupDaoImpl;
import com.cloud.network.security.dao.SecurityGroupRulesDaoImpl;
import com.cloud.network.security.dao.SecurityGroupVMMapDaoImpl;
import com.cloud.network.security.dao.SecurityGroupWorkDaoImpl;
import com.cloud.network.security.dao.VmRulesetLogDaoImpl;
import com.cloud.user.MockAccountManagerImpl;
import com.cloud.user.dao.AccountDaoImpl;
import com.cloud.utils.Profiler;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.MockComponentLocator;
import com.cloud.vm.MockUserVmManagerImpl;
import com.cloud.vm.MockVirtualMachineManagerImpl;
import com.cloud.vm.dao.UserVmDaoImpl;
import com.cloud.vm.dao.VMInstanceDaoImpl;

public class SecurityGroupManagerImpl2Test extends TestCase {
    //private final static Logger s_logger = Logger.getLogger(SecurityGroupManagerImpl2Test.class);
    
    @Override
    @Before
    public void setUp() {
        MockComponentLocator locator = new MockComponentLocator("management-server");
       
        locator.addDao("ConfigurationDao", ConfigurationDaoImpl.class);
        locator.addDao("SecurityGroupDao", SecurityGroupDaoImpl.class);
        
        locator.addDao("IngressRuleDao", IngressRuleDaoImpl.class);
        locator.addDao("SecurityGroupVMMapDao", SecurityGroupVMMapDaoImpl.class);
        locator.addDao("SecurityGroupRulesDao", SecurityGroupRulesDaoImpl.class);
        locator.addDao("UserVmDao", UserVmDaoImpl.class);
        locator.addDao("AccountDao", AccountDaoImpl.class);
        locator.addDao("ConfigurationDao", ConfigurationDaoImpl.class);
        locator.addDao("SecurityGroupWorkDao", SecurityGroupWorkDaoImpl.class);
        locator.addDao("VmRulesetLogDao", VmRulesetLogDaoImpl.class);
        locator.addDao("VMInstanceDao", VMInstanceDaoImpl.class);
        locator.addDao("DomainDao", DomainDaoImpl.class);
        locator.addManager("AgentManager", MockAgentManagerImpl.class);
        locator.addManager("VirtualMachineManager", MockVirtualMachineManagerImpl.class);
        locator.addManager("UserVmManager", MockUserVmManagerImpl.class);
        locator.addManager("NetworkManager", MockNetworkManagerImpl.class);
        locator.addManager("AccountManager", MockAccountManagerImpl.class); 
        locator.makeActive(new DefaultInterceptorLibrary());
    }
    
    @Override
    @After
    public void tearDown() throws Exception {
    }
    
    public void testSchedule() {
        final int numVms = 100000;
        System.out.println("Starting");
        ComponentLocator locator = ComponentLocator.getCurrentLocator();
        SecurityGroupManagerImpl2 sgMgr = ComponentLocator.inject(SecurityGroupManagerImpl2.class);
        List<Long> work = new ArrayList<Long>();
        for (long i=100; i <= numVms; i++) {
            work.add(i);
        }
        Profiler profiler = new Profiler();
        profiler.start();
        sgMgr.scheduleRulesetUpdateToHosts(work, false, null);
        profiler.stop();
        
        System.out.println("Done " + numVms + " in " + profiler.getDuration() + " ms");
    }
}
