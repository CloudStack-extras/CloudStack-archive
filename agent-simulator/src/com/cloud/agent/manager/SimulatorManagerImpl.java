package com.cloud.agent.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.AttachIsoCommand;
import com.cloud.agent.api.AttachVolumeCommand;
import com.cloud.agent.api.BackupSnapshotCommand;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.agent.api.CleanupNetworkRulesCmd;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.ComputeChecksumCommand;
import com.cloud.agent.api.CreatePrivateTemplateFromSnapshotCommand;
import com.cloud.agent.api.CreatePrivateTemplateFromVolumeCommand;
import com.cloud.agent.api.CreateStoragePoolCommand;
import com.cloud.agent.api.CreateVolumeFromSnapshotCommand;
import com.cloud.agent.api.DeleteSnapshotBackupCommand;
import com.cloud.agent.api.DeleteStoragePoolCommand;
import com.cloud.agent.api.GetHostStatsCommand;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.agent.api.GetVmStatsCommand;
import com.cloud.agent.api.GetVncPortCommand;
import com.cloud.agent.api.MaintainCommand;
import com.cloud.agent.api.ManageSnapshotCommand;
import com.cloud.agent.api.MigrateCommand;
import com.cloud.agent.api.ModifyStoragePoolCommand;
import com.cloud.agent.api.NetworkUsageCommand;
import com.cloud.agent.api.PingTestCommand;
import com.cloud.agent.api.RebootCommand;
import com.cloud.agent.api.SecStorageSetupCommand;
import com.cloud.agent.api.SecStorageVMSetupCommand;
import com.cloud.agent.api.SecurityIngressRulesCmd;
import com.cloud.agent.api.StartCommand;
import com.cloud.agent.api.StopCommand;
import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.agent.api.check.CheckSshCommand;
import com.cloud.agent.api.proxy.CheckConsoleProxyLoadCommand;
import com.cloud.agent.api.proxy.WatchConsoleProxyLoadCommand;
import com.cloud.agent.api.routing.DhcpEntryCommand;
import com.cloud.agent.api.routing.IpAssocCommand;
import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.routing.SavePasswordCommand;
import com.cloud.agent.api.routing.SetPortForwardingRulesCommand;
import com.cloud.agent.api.routing.SetStaticNatRulesCommand;
import com.cloud.agent.api.routing.VmDataCommand;
import com.cloud.agent.api.storage.CreateCommand;
import com.cloud.agent.api.storage.DeleteTemplateCommand;
import com.cloud.agent.api.storage.DestroyCommand;
import com.cloud.agent.api.storage.DownloadCommand;
import com.cloud.agent.api.storage.DownloadProgressCommand;
import com.cloud.agent.api.storage.ListTemplateCommand;
import com.cloud.agent.api.storage.PrimaryStorageDownloadCommand;
import com.cloud.resource.AgentResourceBase;
import com.cloud.simulator.MockConfigurationVO;
import com.cloud.simulator.MockHost;
import com.cloud.simulator.MockHostVO;
import com.cloud.simulator.dao.MockConfigurationDao;
import com.cloud.simulator.dao.MockHostDao;
import com.cloud.utils.Pair;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.ConnectionConcierge;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;
@Local(value = { SimulatorManager.class })
public class SimulatorManagerImpl implements SimulatorManager {
    private static final Logger s_logger = Logger.getLogger(SimulatorManagerImpl.class);
    @Inject
    MockVmManager _mockVmMgr = null;
    @Inject
    MockStorageManager _mockStorageMgr = null;
    @Inject
    MockAgentManager _mockAgentMgr = null;
    @Inject
    MockConfigurationDao _mockConfigDao = null;
    @Inject
    MockHostDao _mockHost = null;
    private ConnectionConcierge _concierge;
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        try {
            Connection conn = Transaction.getStandaloneConnectionWithException();
            conn.setAutoCommit(true);
            _concierge = new ConnectionConcierge("SimulatorConnection", conn, true);
        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable to get a db connection", e);
        }
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
        return this.getClass().getSimpleName();
    }

    @Override
    public MockVmManager getVmMgr() {
        return _mockVmMgr;
    }

    @Override
    public MockStorageManager getStorageMgr() {
        return _mockStorageMgr;
    }

    @Override
    public MockAgentManager getAgentMgr() {
        return _mockAgentMgr;
    }

    @DB
    @Override
    public Answer simulate(Command cmd, String hostGuid) {
        MockHost host = _mockHost.findByGuid(hostGuid);
        MockConfigurationVO config = _mockConfigDao.findByCommand(host.getDataCenterId(), host.getPodId(), host.getClusterId(), host.getId(), cmd.toString());
        if (config == null) {
            config = _mockConfigDao.findByGlobal(cmd.toString());
        }
        
        if (config != null) {
            Map<String, String> configParameters = config.getParameters();
            if (configParameters.get("enabled").equalsIgnoreCase("false")) {
                return new Answer(cmd, false, "cmd is disabled");
            }
        }
        
        Transaction txn = Transaction.currentTxn();
        txn.transitToUserManagedConnection(_concierge.conn());
        
        try {
            if (cmd instanceof GetHostStatsCommand) {
                return _mockAgentMgr.getHostStatistic((GetHostStatsCommand)cmd);
            } else if (cmd instanceof CheckHealthCommand) {
                return _mockAgentMgr.checkHealth((CheckHealthCommand)cmd);
            } else if (cmd instanceof PingTestCommand) {
                return _mockAgentMgr.pingTest((PingTestCommand)cmd);
            } else if (cmd instanceof MigrateCommand) {
                return _mockVmMgr.Migrate((MigrateCommand)cmd, hostGuid);
            } else if (cmd instanceof StartCommand) {
                return _mockVmMgr.startVM((StartCommand)cmd, hostGuid);
            } else if (cmd instanceof CheckSshCommand) {
                return _mockVmMgr.checkSshCommand((CheckSshCommand)cmd);
            } else if (cmd instanceof SetStaticNatRulesCommand) {
                return _mockVmMgr.SetStaticNatRules((SetStaticNatRulesCommand)cmd);
            } else if (cmd instanceof SetPortForwardingRulesCommand) {
                return _mockVmMgr.SetPortForwardingRules((SetPortForwardingRulesCommand)cmd);
            } else if (cmd instanceof NetworkUsageCommand) {
                return _mockVmMgr.getNetworkUsage((NetworkUsageCommand)cmd);
            } else if (cmd instanceof IpAssocCommand) {
                return _mockVmMgr.IpAssoc((IpAssocCommand)cmd);
            } else if (cmd instanceof LoadBalancerConfigCommand) {
                return _mockVmMgr.LoadBalancerConfig((LoadBalancerConfigCommand)cmd);
            } else if (cmd instanceof DhcpEntryCommand) {
                return _mockVmMgr.AddDhcpEntry((DhcpEntryCommand)cmd);
            } else if (cmd instanceof VmDataCommand) {
                return _mockVmMgr.setVmData((VmDataCommand)cmd);
            } else if (cmd instanceof CleanupNetworkRulesCmd) {
                return _mockVmMgr.CleanupNetworkRules((CleanupNetworkRulesCmd)cmd, hostGuid);
            } else if (cmd instanceof StopCommand) {
                return _mockVmMgr.stopVM((StopCommand)cmd);
            } else if (cmd instanceof RebootCommand) {
                return _mockVmMgr.rebootVM((RebootCommand)cmd);
            } else if (cmd instanceof GetVncPortCommand) {
                return _mockVmMgr.getVncPort((GetVncPortCommand)cmd);
            } else if (cmd instanceof CheckConsoleProxyLoadCommand) {
                return _mockVmMgr.CheckConsoleProxyLoad((CheckConsoleProxyLoadCommand)cmd);
            } else if (cmd instanceof WatchConsoleProxyLoadCommand) {
                return _mockVmMgr.WatchConsoleProxyLoad((WatchConsoleProxyLoadCommand)cmd);
            } else if (cmd instanceof SecurityIngressRulesCmd) {
                return _mockVmMgr.AddSecurityIngressRules((SecurityIngressRulesCmd)cmd, hostGuid);
            } else if (cmd instanceof SavePasswordCommand) {
                return _mockVmMgr.SavePassword((SavePasswordCommand)cmd);
            } else if (cmd instanceof PrimaryStorageDownloadCommand) {
                return _mockStorageMgr.primaryStorageDownload((PrimaryStorageDownloadCommand)cmd);
            } else if (cmd instanceof CreateCommand) {
                return _mockStorageMgr.createVolume((CreateCommand)cmd);
            } else if (cmd instanceof AttachVolumeCommand) {
                return _mockStorageMgr.AttachVolume((AttachVolumeCommand)cmd);
            } else if (cmd instanceof AttachIsoCommand) {
                return _mockStorageMgr.AttachIso((AttachIsoCommand)cmd);
            } else if (cmd instanceof DeleteStoragePoolCommand) {
                return _mockStorageMgr.DeleteStoragePool((DeleteStoragePoolCommand)cmd);
            } else if (cmd instanceof ModifyStoragePoolCommand) {
                return _mockStorageMgr.ModifyStoragePool((ModifyStoragePoolCommand)cmd);
            } else if (cmd instanceof CreateStoragePoolCommand) {
                return _mockStorageMgr.CreateStoragePool((CreateStoragePoolCommand)cmd);
            } else if (cmd instanceof SecStorageSetupCommand) {
                return _mockStorageMgr.SecStorageSetup((SecStorageSetupCommand)cmd);
            } else if (cmd instanceof ListTemplateCommand) {
                return _mockStorageMgr.ListTemplates((ListTemplateCommand)cmd);
            } else if (cmd instanceof DestroyCommand) {
                return _mockStorageMgr.Destroy((DestroyCommand)cmd);
            } else if (cmd instanceof DownloadProgressCommand) {
                return _mockStorageMgr.DownloadProcess((DownloadProgressCommand)cmd);
            } else if (cmd instanceof DownloadCommand) {
                return _mockStorageMgr.Download((DownloadCommand)cmd);
            } else if (cmd instanceof GetStorageStatsCommand) {
                return _mockStorageMgr.GetStorageStats((GetStorageStatsCommand)cmd);
            } else if (cmd instanceof ManageSnapshotCommand) {
                return _mockStorageMgr.ManageSnapshot((ManageSnapshotCommand)cmd);
            } else if (cmd instanceof BackupSnapshotCommand) {
                return _mockStorageMgr.BackupSnapshot((BackupSnapshotCommand)cmd);
            } else if (cmd instanceof DeleteSnapshotBackupCommand) {
                return _mockStorageMgr.DeleteSnapshotBackup((DeleteSnapshotBackupCommand)cmd);
            } else if (cmd instanceof CreateVolumeFromSnapshotCommand) {
                return _mockStorageMgr.CreateVolumeFromSnapshot((CreateVolumeFromSnapshotCommand)cmd);
            } else if (cmd instanceof DeleteTemplateCommand) {
                return _mockStorageMgr.DeleteTemplate((DeleteTemplateCommand)cmd);
            } else if (cmd instanceof SecStorageVMSetupCommand) {
                return _mockStorageMgr.SecStorageVMSetup((SecStorageVMSetupCommand)cmd);
            } else if (cmd instanceof CreatePrivateTemplateFromSnapshotCommand) {
                return _mockStorageMgr.CreatePrivateTemplateFromSnapshot((CreatePrivateTemplateFromSnapshotCommand)cmd);
            } else if (cmd instanceof ComputeChecksumCommand) {
                return _mockStorageMgr.ComputeChecksum((ComputeChecksumCommand)cmd);
            } else if (cmd instanceof CreatePrivateTemplateFromVolumeCommand) {
                return _mockStorageMgr.CreatePrivateTemplateFromVolume((CreatePrivateTemplateFromVolumeCommand)cmd);
            } else if (cmd instanceof MaintainCommand) {
                return _mockAgentMgr.MaintainCommand((MaintainCommand)cmd);
            } else if (cmd instanceof GetVmStatsCommand) {
                return _mockVmMgr.getVmStats((GetVmStatsCommand)cmd);
            } else {
                return Answer.createUnsupportedCommandAnswer(cmd);
            }
        } catch(Exception e) {
            s_logger.debug("Failed execute cmd: " + e.toString());
            txn.rollback();
            return new Answer(cmd, false, e.toString());
        } finally {
            txn.transitToAutoManagedConnection(Transaction.CLOUD_DB);
        }
    }

    @Override
    public StoragePoolInfo getLocalStorage(String hostGuid) {
        return _mockStorageMgr.getLocalStorage(hostGuid);
    }

    @Override
    public boolean configureSimulator(Long zoneId, Long podId, Long clusterId, Long hostId, String command, String values) {
        MockConfigurationVO config = new MockConfigurationVO();
        config.setClusterId(clusterId);
        config.setDataCenterId(zoneId);
        config.setPodId(podId);
        config.setHostId(hostId);
        config.setName(command);
        config.setValues(values);
        _mockConfigDao.persist(config);
        return true;
    }

    @Override
    public HashMap<String, Pair<Long, Long>> syncNetworkGroups(String hostGuid) {
        return _mockVmMgr.syncNetworkGroups(hostGuid);
    }

}
