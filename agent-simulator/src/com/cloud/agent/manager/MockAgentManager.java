package com.cloud.agent.manager;

import java.util.Map;

import javax.naming.ConfigurationException;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckHealthAnswer;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.agent.api.GetHostStatsAnswer;
import com.cloud.agent.api.GetHostStatsCommand;
import com.cloud.agent.api.HostStatsEntry;
import com.cloud.agent.api.MaintainCommand;
import com.cloud.agent.api.PingTestCommand;
import com.cloud.agent.api.PrepareForMigrationAnswer;
import com.cloud.agent.api.PrepareForMigrationCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.SecurityIngressRulesCmd;
import com.cloud.agent.api.ShutdownCommand;
import com.cloud.resource.AgentResourceBase;
import com.cloud.simulator.MockHost;
import com.cloud.utils.component.Manager;

public interface MockAgentManager extends Manager {
    public static final long DEFAULT_HOST_MEM_SIZE  = 8 * 1024 * 1024 * 1024L; // 8G, unit of
    // Mbytes
    public static final int DEFAULT_HOST_CPU_CORES = 4; // 2 dual core CPUs (2 x
    // 2)
    public static final int DEFAULT_HOST_SPEED_MHZ = 8000; // 1 GHz CPUs
    boolean configure(String name, Map<String, Object> params) throws ConfigurationException;

    Map<AgentResourceBase, Map<String, String>> createServerResources(Map<String, Object> params);

    boolean handleSystemVMStart(long vmId, String privateIpAddress, String privateMacAddress, String privateNetMask, long dcId, long podId, String name, String vmType, String url);

    boolean handleSystemVMStop(long vmId);

    GetHostStatsAnswer getHostStatistic(GetHostStatsCommand cmd);
    Answer checkHealth(CheckHealthCommand cmd);
    Answer pingTest(PingTestCommand cmd);
    
    Answer PrepareForMigration(PrepareForMigrationCommand cmd);
    
    MockHost getHost(String guid);

    Answer MaintainCommand(MaintainCommand cmd);
}
