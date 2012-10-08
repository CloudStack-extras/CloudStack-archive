package com.cloud.ucs.manager;

import java.util.HashMap;
import java.util.Map;

import javax.naming.ConfigurationException;

import com.cloud.agent.IAgentControl;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingRoutingCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.api.ApiConstants;
import com.cloud.host.Host.Type;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.resource.ServerResource;
import com.cloud.vm.VirtualMachine.State;

public class UcsResourceBase implements ServerResource {

    private String zone;
    private String pod;
    private String cluster;
    private String ip;
    private String uuid;
    private String dn;
    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        zone = (String) params.get(ApiConstants.ZONE_ID);
        assert zone != null;
        pod = (String) params.get(ApiConstants.POD_ID);
        assert pod != null;
        cluster = (String) params.get(ApiConstants.CLUSTER_ID);
        assert cluster != null;
        ip = (String) params.get(ApiConstants.PRIVATE_IP);
        assert ip != null;
        uuid = (String) params.get(ApiConstants.UUID);
        assert uuid != null;
        dn = (String) params.get(ApiConstants.PASSWORD);
        assert dn != null;
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public String getName() {
        return "UcsResource";
    }

    @Override
    public Type getType() {
        return com.cloud.host.Host.Type.Routing;
    }

    @Override
    public StartupCommand[] initialize() {
        StartupRoutingCommand cmd = new StartupRoutingCommand(0, 0, 0, 0, null, Hypervisor.HypervisorType.ManagedHost, new HashMap<String, String>(), null);
        cmd.setDataCenter(zone);
        cmd.setPod(pod);
        cmd.setCluster(cluster);
        cmd.setGuid(uuid);
        cmd.setName(dn);
        cmd.setPrivateIpAddress(ip);
        cmd.setStorageIpAddress(ip);
        cmd.setVersion(UcsResourceBase.class.getPackage().getImplementationVersion());
        cmd.setCpus(2);
        cmd.setSpeed(2600);
        cmd.setMemory(40000);
        cmd.setPrivateMacAddress("00:1B:21:53:F1:F0");
        cmd.setPublicMacAddress("00:1B:21:53:F1:F0");
        return new StartupCommand[] { cmd };
    }

    @Override
    public PingCommand getCurrentStatus(long id) {
        return new PingRoutingCommand(getType(), id, new HashMap<String, State>());
    }

    protected ReadyAnswer execute(ReadyCommand cmd) {
        return new ReadyAnswer(cmd);
    }
    
    @Override
    public Answer executeRequest(Command cmd) {
        if (cmd instanceof ReadyCommand) {
            return execute((ReadyCommand) cmd);
        } else {
            return Answer.createUnsupportedCommandAnswer(cmd);
        }
    }

    @Override
    public void disconnected() {

    }

    @Override
    public IAgentControl getAgentControl() {
        return null;
    }

    @Override
    public void setAgentControl(IAgentControl agentControl) {
    }
}
