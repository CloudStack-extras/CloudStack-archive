package com.cloud.network.security;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

import com.cloud.agent.AgentManager;
import com.cloud.agent.MockAgentManagerImpl;
import com.cloud.configuration.DefaultInterceptorLibrary;
import com.cloud.configuration.dao.ConfigurationDaoImpl;
import com.cloud.domain.dao.DomainDaoImpl;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
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
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachineName;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.UserVmDaoImpl;
import com.cloud.vm.dao.VMInstanceDaoImpl;

public class SecurityGroupManagerImpl2Test extends TestCase {
    //private final static Logger s_logger = Logger.getLogger(SecurityGroupManagerImpl2Test.class);
    SecurityGroupManagerImpl2 _sgMgr = null;
    UserVmDaoImpl _vmDao = null;
    
    @Before
    @Override
    public  void setUp() {
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
        _sgMgr = ComponentLocator.inject(SecurityGroupManagerImpl2.class);

        _vmDao = spy((UserVmDaoImpl)locator.getDao(UserVmDao.class));
        when(_vmDao.findById(anyLong())).thenAnswer(new Answer<UserVmVO>() {

            @Override
            public UserVmVO answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Long vmId = (Long) args[0];
                String vmName = VirtualMachineName.getVmName(vmId,3, "VM");
                UserVmVO result = new UserVmVO(vmId, vmName, vmName, 1, HypervisorType.XenServer, 5, false, false, 1, 3, 1, null, vmName);
                result.setHostId(vmId);
                return result;
            }
            
        });
        AgentManager agentMgr = spy(locator.getManager(AgentManager.class));
    }
    
    @Override
    @After
    public void tearDown() throws Exception {
    }
    
    protected void _schedule(final int numVms) {
        System.out.println("Starting");
        List<Long> work = new ArrayList<Long>();
        for (long i=100; i <= 100+numVms; i++) {
            work.add(i);
        }
        Profiler profiler = new Profiler();
        profiler.start();
        _sgMgr.scheduleRulesetUpdateToHosts(work, false, null);
        profiler.stop();
        
        System.out.println("Done " + numVms + " in " + profiler.getDuration() + " ms");
    }
    
    @Ignore
    public void testSchedule() {
        _schedule(1000);
    }
    
    public void testWork() {
       _schedule(100);
       _sgMgr.work();
        
    }
}
