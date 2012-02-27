/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.hypervisor.vmware.resource;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import com.cloud.agent.IAgentControl;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.AttachIsoCommand;
import com.cloud.agent.api.AttachVolumeAnswer;
import com.cloud.agent.api.AttachVolumeCommand;
import com.cloud.agent.api.BackupSnapshotAnswer;
import com.cloud.agent.api.BackupSnapshotCommand;
import com.cloud.agent.api.BumpUpPriorityCommand;
import com.cloud.agent.api.CheckHealthAnswer;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.agent.api.CheckNetworkAnswer;
import com.cloud.agent.api.CheckNetworkCommand;
import com.cloud.agent.api.CheckOnHostAnswer;
import com.cloud.agent.api.CheckOnHostCommand;
import com.cloud.agent.api.CheckRouterAnswer;
import com.cloud.agent.api.CheckRouterCommand;
import com.cloud.agent.api.CheckVirtualMachineAnswer;
import com.cloud.agent.api.CheckVirtualMachineCommand;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.CreatePrivateTemplateFromSnapshotCommand;
import com.cloud.agent.api.CreatePrivateTemplateFromVolumeCommand;
import com.cloud.agent.api.CreateStoragePoolCommand;
import com.cloud.agent.api.CreateVolumeFromSnapshotAnswer;
import com.cloud.agent.api.CreateVolumeFromSnapshotCommand;
import com.cloud.agent.api.DeleteStoragePoolCommand;
import com.cloud.agent.api.GetDomRVersionAnswer;
import com.cloud.agent.api.GetDomRVersionCmd;
import com.cloud.agent.api.GetHostStatsAnswer;
import com.cloud.agent.api.GetHostStatsCommand;
import com.cloud.agent.api.GetStorageStatsAnswer;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.agent.api.GetVmStatsAnswer;
import com.cloud.agent.api.GetVmStatsCommand;
import com.cloud.agent.api.GetVncPortAnswer;
import com.cloud.agent.api.GetVncPortCommand;
import com.cloud.agent.api.HostStatsEntry;
import com.cloud.agent.api.MaintainAnswer;
import com.cloud.agent.api.MaintainCommand;
import com.cloud.agent.api.ManageSnapshotAnswer;
import com.cloud.agent.api.ManageSnapshotCommand;
import com.cloud.agent.api.MigrateAnswer;
import com.cloud.agent.api.MigrateCommand;
import com.cloud.agent.api.ModifySshKeysCommand;
import com.cloud.agent.api.ModifyStoragePoolAnswer;
import com.cloud.agent.api.ModifyStoragePoolCommand;
import com.cloud.agent.api.NetworkUsageAnswer;
import com.cloud.agent.api.NetworkUsageCommand;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingRoutingCommand;
import com.cloud.agent.api.PingTestCommand;
import com.cloud.agent.api.PoolEjectCommand;
import com.cloud.agent.api.PrepareForMigrationAnswer;
import com.cloud.agent.api.PrepareForMigrationCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.RebootAnswer;
import com.cloud.agent.api.RebootCommand;
import com.cloud.agent.api.RebootRouterCommand;
import com.cloud.agent.api.SetupAnswer;
import com.cloud.agent.api.SetupCommand;
import com.cloud.agent.api.StartAnswer;
import com.cloud.agent.api.StartCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.agent.api.StartupStorageCommand;
import com.cloud.agent.api.StopAnswer;
import com.cloud.agent.api.StopCommand;
import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.agent.api.UpgradeSnapshotCommand;
import com.cloud.agent.api.ValidateSnapshotAnswer;
import com.cloud.agent.api.ValidateSnapshotCommand;
import com.cloud.agent.api.VmStatsEntry;
import com.cloud.agent.api.check.CheckSshAnswer;
import com.cloud.agent.api.check.CheckSshCommand;
import com.cloud.agent.api.routing.DhcpEntryCommand;
import com.cloud.agent.api.routing.IpAssocAnswer;
import com.cloud.agent.api.routing.IpAssocCommand;
import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.RemoteAccessVpnCfgCommand;
import com.cloud.agent.api.routing.SavePasswordCommand;
import com.cloud.agent.api.routing.SetFirewallRulesAnswer;
import com.cloud.agent.api.routing.SetFirewallRulesCommand;
import com.cloud.agent.api.routing.SetPortForwardingRulesAnswer;
import com.cloud.agent.api.routing.SetPortForwardingRulesCommand;
import com.cloud.agent.api.routing.SetStaticNatRulesAnswer;
import com.cloud.agent.api.routing.SetStaticNatRulesCommand;
import com.cloud.agent.api.routing.VmDataCommand;
import com.cloud.agent.api.routing.VpnUsersCfgCommand;
import com.cloud.agent.api.storage.CopyVolumeAnswer;
import com.cloud.agent.api.storage.CopyVolumeCommand;
import com.cloud.agent.api.storage.CreateAnswer;
import com.cloud.agent.api.storage.CreateCommand;
import com.cloud.agent.api.storage.CreatePrivateTemplateAnswer;
import com.cloud.agent.api.storage.DestroyCommand;
import com.cloud.agent.api.storage.PrimaryStorageDownloadAnswer;
import com.cloud.agent.api.storage.PrimaryStorageDownloadCommand;
import com.cloud.agent.api.to.IpAddressTO;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.PortForwardingRuleTO;
import com.cloud.agent.api.to.StaticNatRuleTO;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.Vlan;
import com.cloud.exception.InternalErrorException;
import com.cloud.host.Host.Type;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.vmware.manager.VmwareHostService;
import com.cloud.hypervisor.vmware.manager.VmwareManager;
import com.cloud.hypervisor.vmware.mo.ClusterMO;
import com.cloud.hypervisor.vmware.mo.CustomFieldConstants;
import com.cloud.hypervisor.vmware.mo.CustomFieldsManagerMO;
import com.cloud.hypervisor.vmware.mo.DatacenterMO;
import com.cloud.hypervisor.vmware.mo.DatastoreMO;
import com.cloud.hypervisor.vmware.mo.DiskControllerType;
import com.cloud.hypervisor.vmware.mo.HostFirewallSystemMO;
import com.cloud.hypervisor.vmware.mo.HostMO;
import com.cloud.hypervisor.vmware.mo.HostVirtualNicType;
import com.cloud.hypervisor.vmware.mo.HypervisorHostHelper;
import com.cloud.hypervisor.vmware.mo.NetworkDetails;
import com.cloud.hypervisor.vmware.mo.VirtualEthernetCardType;
import com.cloud.hypervisor.vmware.mo.VirtualMachineMO;
import com.cloud.hypervisor.vmware.mo.VmwareHypervisorHost;
import com.cloud.hypervisor.vmware.mo.VmwareHypervisorHostNetworkSummary;
import com.cloud.hypervisor.vmware.mo.VmwareHypervisorHostResourceSummary;
import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.cloud.hypervisor.vmware.util.VmwareGuestOsMapper;
import com.cloud.hypervisor.vmware.util.VmwareHelper;
import com.cloud.network.HAProxyConfigurator;
import com.cloud.network.LoadBalancerConfigurator;
import com.cloud.network.Networks;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.resource.ServerResource;
import com.cloud.serializer.GsonHelper;
import com.cloud.storage.Storage;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.Volume;
import com.cloud.storage.resource.StoragePoolResource;
import com.cloud.storage.template.TemplateInfo;
import com.cloud.utils.DateUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.StringUtils;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.db.DB;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.ExceptionUtil;
import com.cloud.utils.mgmt.JmxUtil;
import com.cloud.utils.mgmt.PropertyMapDynamicBean;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachineName;
import com.cloud.vm.VmDetailConstants;
import com.google.gson.Gson;
import com.vmware.vim25.AboutInfo;
import com.vmware.vim25.ClusterDasConfigInfo;
import com.vmware.vim25.ComputeResourceSummary;
import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.HostFirewallInfo;
import com.vmware.vim25.HostFirewallRuleset;
import com.vmware.vim25.HostNetworkTrafficShapingPolicy;
import com.vmware.vim25.HostPortGroupSpec;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.OptionValue;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PerfSampleInfo;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.ToolsUnavailable;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineFileInfo;
import com.vmware.vim25.VirtualMachineGuestOsIdentifier;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VirtualSCSISharing;

public class VmwareResource implements StoragePoolResource, ServerResource, VmwareHostService {
    private static final Logger s_logger = Logger.getLogger(VmwareResource.class);

    protected String _name;

    protected final long _ops_timeout = 900000; 		// 15 minutes time out to time
    protected final int _shutdown_waitMs = 300000;		// wait up to 5 minutes for shutdown 
    
    // out an operation
    protected final int _retry = 24;
    protected final int _sleep = 10000;
    protected final int DEFAULT_DOMR_SSHPORT = 3922;
    protected final int MAX_CMD_MBEAN = 100;

    protected String _url;
    protected String _dcId;
    protected String _pod;
    protected String _cluster;
    protected String _username;
    protected String _password;
    protected String _guid;
    protected String _vCenterAddress;

    protected String _privateNetworkVSwitchName;
    protected String _publicNetworkVSwitchName;
    protected String _guestNetworkVSwitchName;

    protected float _cpuOverprovisioningFactor = 1;
    protected boolean _reserveCpu = false;
    
    protected float _memOverprovisioningFactor = 1;
    protected boolean _reserveMem = false;
    protected boolean _recycleHungWorker = false;
    protected DiskControllerType _rootDiskController = DiskControllerType.ide;

    protected ManagedObjectReference _morHyperHost;
    protected VmwareContext _serviceContext;
    protected String _hostName;

    protected HashMap<String, State> _vms = new HashMap<String, State>(71);
    protected List<PropertyMapDynamicBean> _cmdMBeans = new ArrayList<PropertyMapDynamicBean>();

    protected Gson _gson;

    protected volatile long _cmdSequence = 1;

    protected static HashMap<VirtualMachinePowerState, State> s_statesTable;
    static {
        s_statesTable = new HashMap<VirtualMachinePowerState, State>();
        s_statesTable.put(VirtualMachinePowerState.poweredOn, State.Running);
        s_statesTable.put(VirtualMachinePowerState.poweredOff, State.Stopped);
        s_statesTable.put(VirtualMachinePowerState.suspended, State.Stopped);
    }

    public VmwareResource() {
        _gson = GsonHelper.getGsonLogger();
    }

    @Override
    public Answer executeRequest(Command cmd) {
    	if(s_logger.isTraceEnabled())
    		s_logger.trace("Begin executeRequest(), cmd: " + cmd.getClass().getSimpleName());
    	
        Answer answer = null;
        NDC.push(_hostName != null ? _hostName : _guid + "(" + ComponentLocator.class.getPackage().getImplementationVersion() + ")");
        try {
            long cmdSequence = _cmdSequence++;
            Date startTime = DateUtil.currentGMTTime();
            PropertyMapDynamicBean mbean = new PropertyMapDynamicBean();
            mbean.addProp("StartTime", DateUtil.getDateDisplayString(TimeZone.getDefault(), startTime));
            mbean.addProp("Command", _gson.toJson(cmd));
            mbean.addProp("Sequence", String.valueOf(cmdSequence));
            mbean.addProp("Name", cmd.getClass().getSimpleName());

            if (cmd instanceof CreateCommand) {
                answer = execute((CreateCommand) cmd);
            } else if (cmd instanceof SetPortForwardingRulesCommand) {
                answer = execute((SetPortForwardingRulesCommand) cmd);
            } else if (cmd instanceof SetStaticNatRulesCommand) {
                answer = execute((SetStaticNatRulesCommand) cmd);
            } else if (cmd instanceof LoadBalancerConfigCommand) {
                answer = execute((LoadBalancerConfigCommand) cmd);
            } else if (cmd instanceof IpAssocCommand) {
                answer = execute((IpAssocCommand) cmd);
            } else if (cmd instanceof SavePasswordCommand) {
                answer = execute((SavePasswordCommand) cmd);
            } else if (cmd instanceof DhcpEntryCommand) {
                answer = execute((DhcpEntryCommand) cmd);
            } else if (cmd instanceof VmDataCommand) {
                answer = execute((VmDataCommand) cmd);
            } else if (cmd instanceof ReadyCommand) {
                answer = execute((ReadyCommand) cmd);
            } else if (cmd instanceof GetHostStatsCommand) {
                answer = execute((GetHostStatsCommand) cmd);
            } else if (cmd instanceof GetVmStatsCommand) {
                answer = execute((GetVmStatsCommand) cmd);
            } else if (cmd instanceof CheckHealthCommand) {
                answer = execute((CheckHealthCommand) cmd);
            } else if (cmd instanceof StopCommand) {
                answer = execute((StopCommand) cmd);
            } else if (cmd instanceof RebootRouterCommand) {
                answer = execute((RebootRouterCommand) cmd);
            } else if (cmd instanceof RebootCommand) {
                answer = execute((RebootCommand) cmd);
            } else if (cmd instanceof CheckVirtualMachineCommand) {
                answer = execute((CheckVirtualMachineCommand) cmd);
            } else if (cmd instanceof PrepareForMigrationCommand) {
                answer = execute((PrepareForMigrationCommand) cmd);
            } else if (cmd instanceof MigrateCommand) {
                answer = execute((MigrateCommand) cmd);
            } else if (cmd instanceof DestroyCommand) {
                answer = execute((DestroyCommand) cmd);
            } else if (cmd instanceof CreateStoragePoolCommand) {
                return execute((CreateStoragePoolCommand) cmd);
            } else if (cmd instanceof ModifyStoragePoolCommand) {
                answer = execute((ModifyStoragePoolCommand) cmd);
            } else if (cmd instanceof DeleteStoragePoolCommand) {
                answer = execute((DeleteStoragePoolCommand) cmd);
            } else if (cmd instanceof CopyVolumeCommand) {
                answer = execute((CopyVolumeCommand) cmd);
            } else if (cmd instanceof AttachVolumeCommand) {
                answer = execute((AttachVolumeCommand) cmd);
            } else if (cmd instanceof AttachIsoCommand) {
                answer = execute((AttachIsoCommand) cmd);
            } else if (cmd instanceof ValidateSnapshotCommand) {
                answer = execute((ValidateSnapshotCommand) cmd);
            } else if (cmd instanceof ManageSnapshotCommand) {
                answer = execute((ManageSnapshotCommand) cmd);
            } else if (cmd instanceof BackupSnapshotCommand) {
                answer = execute((BackupSnapshotCommand) cmd);
            } else if (cmd instanceof CreateVolumeFromSnapshotCommand) {
                answer = execute((CreateVolumeFromSnapshotCommand) cmd);
            } else if (cmd instanceof CreatePrivateTemplateFromVolumeCommand) {
                answer = execute((CreatePrivateTemplateFromVolumeCommand) cmd);
            } else if (cmd instanceof CreatePrivateTemplateFromSnapshotCommand) {
                answer = execute((CreatePrivateTemplateFromSnapshotCommand) cmd);
            } else if (cmd instanceof UpgradeSnapshotCommand) {
                answer = execute((UpgradeSnapshotCommand) cmd);
            } else if (cmd instanceof GetStorageStatsCommand) {
                answer = execute((GetStorageStatsCommand) cmd);
            } else if (cmd instanceof PrimaryStorageDownloadCommand) {
                answer = execute((PrimaryStorageDownloadCommand) cmd);
            } else if (cmd instanceof GetVncPortCommand) {
                answer = execute((GetVncPortCommand) cmd);
            } else if (cmd instanceof SetupCommand) {
                answer = execute((SetupCommand) cmd);
            } else if (cmd instanceof MaintainCommand) {
                answer = execute((MaintainCommand) cmd);
            } else if (cmd instanceof PingTestCommand) {
                answer = execute((PingTestCommand) cmd);
            } else if (cmd instanceof CheckOnHostCommand) {
                answer = execute((CheckOnHostCommand) cmd);
            } else if (cmd instanceof ModifySshKeysCommand) {
                answer = execute((ModifySshKeysCommand) cmd);
            } else if (cmd instanceof PoolEjectCommand) {
                answer = execute((PoolEjectCommand) cmd);
            } else if (cmd instanceof NetworkUsageCommand) {
                answer = execute((NetworkUsageCommand) cmd);
            } else if (cmd instanceof StartCommand) {
                answer = execute((StartCommand) cmd);
            } else if (cmd instanceof RemoteAccessVpnCfgCommand) {
                answer = execute((RemoteAccessVpnCfgCommand) cmd);
            } else if (cmd instanceof VpnUsersCfgCommand) {
                answer = execute((VpnUsersCfgCommand) cmd);
            } else if (cmd instanceof CheckSshCommand) {
                answer = execute((CheckSshCommand) cmd);
            } else if (cmd instanceof CheckRouterCommand) {
                answer = execute((CheckRouterCommand) cmd);
            } else  if (cmd instanceof SetFirewallRulesCommand) {
            	answer = execute((SetFirewallRulesCommand)cmd);
            } else if (cmd instanceof BumpUpPriorityCommand) {
                answer = execute((BumpUpPriorityCommand)cmd);
            } else if (cmd instanceof GetDomRVersionCmd) {
                answer = execute((GetDomRVersionCmd)cmd);
            } else if (cmd instanceof CheckNetworkCommand) {
                answer = execute((CheckNetworkCommand) cmd);
            } else {
                answer = Answer.createUnsupportedCommandAnswer(cmd);
            }

            if(cmd.getContextParam("checkpoint") != null) {
                answer.setContextParam("checkpoint", cmd.getContextParam("checkpoint"));
            }

            Date doneTime = DateUtil.currentGMTTime();
            mbean.addProp("DoneTime", DateUtil.getDateDisplayString(TimeZone.getDefault(), doneTime));
            mbean.addProp("Answer", _gson.toJson(answer));

            synchronized (this) {
                try {
                    JmxUtil.registerMBean("VMware " + _morHyperHost.get_value(), "Command " + cmdSequence + "-" + cmd.getClass().getSimpleName(), mbean);
                    _cmdMBeans.add(mbean);

                    if (_cmdMBeans.size() >= MAX_CMD_MBEAN) {
                        PropertyMapDynamicBean mbeanToRemove = _cmdMBeans.get(0);
                        _cmdMBeans.remove(0);

                        JmxUtil.unregisterMBean("VMware " + _morHyperHost.get_value(), "Command " + mbeanToRemove.getProp("Sequence") + "-" + mbeanToRemove.getProp("Name"));
                    }
                } catch (Exception e) {
                	if(s_logger.isTraceEnabled())
                		s_logger.trace("Unable to register JMX monitoring due to exception " + ExceptionUtil.toString(e));
                }
            }

        } finally {
            NDC.pop();
        }

    	if(s_logger.isTraceEnabled())
    		s_logger.trace("End executeRequest(), cmd: " + cmd.getClass().getSimpleName());
        
        return answer;
    }
    
    protected Answer execute(CheckNetworkCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource CheckNetworkCommand " + _gson.toJson(cmd));
        }

        // TODO setup portgroup for private network needs to be done here now
        return new CheckNetworkAnswer(cmd, true , "Network Setup check by names is done");
    }
    
    protected Answer execute(NetworkUsageCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource NetworkUsageCommand " + _gson.toJson(cmd));
        }

        long[] stats = getNetworkStats(cmd.getPrivateIP());

        NetworkUsageAnswer answer = new NetworkUsageAnswer(cmd, "", stats[0], stats[1]);
        return answer;
    }

    protected Answer execute(SetPortForwardingRulesCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource SetPortForwardingRulesCommand: " + _gson.toJson(cmd));
        }

        String controlIp = getRouterSshControlIp(cmd);
        String args = "";
        String[] results = new String[cmd.getRules().length];
        int i = 0;
        
        boolean endResult = true;
        for (PortForwardingRuleTO rule : cmd.getRules()) {
            args += rule.revoked() ? " -D " : " -A ";
            args += " -P " + rule.getProtocol().toLowerCase();
            args += " -l " + rule.getSrcIp();
            args += " -p " + rule.getStringSrcPortRange();
            args += " -r " + rule.getDstIp();
            args += " -d " + rule.getStringDstPortRange();

            try {
                VmwareManager mgr = getServiceContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
                Pair<Boolean, String> result = SshHelper.sshExecute(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null, "/root/firewall.sh " + args);

                if (s_logger.isDebugEnabled())
                    s_logger.debug("Executing script on domain router " + controlIp + ": /root/firewall.sh " + args);

                if (!result.first()) {
                    s_logger.error("SetPortForwardingRulesCommand failure on setting one rule. args: " + args);
                    results[i++] = "Failed";
                    endResult = false;
                } else {
                    results[i++] = null;
                }
            } catch (Throwable e) {
                s_logger.error("SetPortForwardingRulesCommand(args: " + args + ") failed on setting one rule due to " + VmwareHelper.getExceptionMessage(e), e);
                results[i++] = "Failed";
                endResult = false;
            }
        }

        return new SetPortForwardingRulesAnswer(cmd, results, endResult);
    }
    
    protected SetFirewallRulesAnswer execute(SetFirewallRulesCommand cmd) {
		String controlIp = getRouterSshControlIp(cmd);
		String[] results = new String[cmd.getRules().length];

		String[][] rules = cmd.generateFwRules();
		String args = "";
		args += " -F ";
		StringBuilder sb = new StringBuilder();
		String[] fwRules = rules[0];
		if (fwRules.length > 0) {
			for (int i = 0; i < fwRules.length; i++) {
				sb.append(fwRules[i]).append(',');
			}
			args += " -a " + sb.toString();
		}

		try {
			VmwareManager mgr = getServiceContext().getStockObject(
					VmwareManager.CONTEXT_STOCK_NAME);
			Pair<Boolean, String> result = SshHelper.sshExecute(controlIp,
					DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(),
					null, "/root/firewall_rule.sh " + args);

			if (s_logger.isDebugEnabled())
				s_logger.debug("Executing script on domain router " + controlIp
						+ ": /root/firewall_rule.sh " + args);

			if (!result.first()) {
				s_logger.error("SetFirewallRulesCommand failure on setting one rule. args: "
						+ args);
				//FIXME - in the future we have to process each rule separately; now we temporarily set every rule to be false if single rule fails
	            for (int i=0; i < results.length; i++) {
	                results[i] = "Failed";
	            }
	            
	            return new SetFirewallRulesAnswer(cmd, false, results);
			} 
		} catch (Throwable e) {
			s_logger.error("SetFirewallRulesCommand(args: " + args
					+ ") failed on setting one rule due to "
					+ VmwareHelper.getExceptionMessage(e), e);
			//FIXME - in the future we have to process each rule separately; now we temporarily set every rule to be false if single rule fails
            for (int i=0; i < results.length; i++) {
                results[i] = "Failed";
            }
			return new SetFirewallRulesAnswer(cmd, false, results);
		} 

		return new SetFirewallRulesAnswer(cmd, true, results);
    }	
    
    protected Answer execute(SetStaticNatRulesCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource SetFirewallRuleCommand: " + _gson.toJson(cmd));
        }

        String args = null;
        String[] results = new String[cmd.getRules().length];
        int i = 0;
        boolean endResult = true;
        for (StaticNatRuleTO rule : cmd.getRules()) {
            // 1:1 NAT needs instanceip;publicip;domrip;op
            args = rule.revoked() ? " -D " : " -A ";

            args += " -l " + rule.getSrcIp();
            args += " -r " + rule.getDstIp();
            
            if (rule.getProtocol() != null) {
                args += " -P " + rule.getProtocol().toLowerCase();
            }
            
            args += " -d " + rule.getStringSrcPortRange();
            args += " -G ";

            try {
                VmwareManager mgr = getServiceContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
                String controlIp = getRouterSshControlIp(cmd);
                Pair<Boolean, String> result = SshHelper.sshExecute(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null, "/root/firewall.sh " + args);

                if (s_logger.isDebugEnabled())
                    s_logger.debug("Executing script on domain router " + controlIp + ": /root/firewall.sh " + args);

                if (!result.first()) {
                    s_logger.error("SetStaticNatRulesCommand failure on setting one rule. args: " + args);
                    results[i++] = "Failed";
                    endResult = false;
                } else {
                    results[i++] = null;
                }
            } catch (Throwable e) {
                s_logger.error("SetStaticNatRulesCommand (args: " + args + ") failed on setting one rule due to " + VmwareHelper.getExceptionMessage(e), e);
                results[i++] = "Failed";
                endResult = false;
            }
        }
        return new SetStaticNatRulesAnswer(cmd, results, endResult);
    }

    protected Answer execute(final LoadBalancerConfigCommand cmd) {
        VmwareManager mgr = getServiceContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
        File keyFile = mgr.getSystemVMKeyFile();

        String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        String controlIp = getRouterSshControlIp(cmd);
        
        assert(controlIp != null);

        LoadBalancerConfigurator cfgtr = new HAProxyConfigurator();
        String[] config = cfgtr.generateConfiguration(cmd);

        String[][] rules = cfgtr.generateFwRules(cmd);
        String tmpCfgFilePath = "/tmp/" + routerIp.replace('.', '_') + ".cfg";
        String tmpCfgFileContents = "";
        for (int i = 0; i < config.length; i++) {
            tmpCfgFileContents += config[i];
            tmpCfgFileContents += "\n";
        }

        try {
            SshHelper.scpTo(controlIp, DEFAULT_DOMR_SSHPORT, "root", keyFile, null, "/tmp/", tmpCfgFileContents.getBytes(), routerIp.replace('.', '_') + ".cfg", null);

            try {
                String[] addRules = rules[LoadBalancerConfigurator.ADD];
                String[] removeRules = rules[LoadBalancerConfigurator.REMOVE];
                String[] statRules = rules[LoadBalancerConfigurator.STATS];

                String args = "";
                args += "-i " + routerIp;
                args += " -f " + tmpCfgFilePath;

                StringBuilder sb = new StringBuilder();
                if (addRules.length > 0) {
                    for (int i = 0; i < addRules.length; i++) {
                        sb.append(addRules[i]).append(',');
                    }

                    args += " -a " + sb.toString();
                }

                sb = new StringBuilder();
                if (removeRules.length > 0) {
                    for (int i = 0; i < removeRules.length; i++) {
                        sb.append(removeRules[i]).append(',');
                    }

                    args += " -d " + sb.toString();
                }

                sb = new StringBuilder();
                if (statRules.length > 0) {
                    for (int i = 0; i < statRules.length; i++) {
                        sb.append(statRules[i]).append(',');
                    }

                    args += " -s " + sb.toString();
                }
                
                Pair<Boolean, String> result = SshHelper.sshExecute(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null, "scp " + tmpCfgFilePath + " /etc/haproxy/haproxy.cfg.new");

                if (!result.first()) {
                    s_logger.error("Unable to copy haproxy configuration file");
                    return new Answer(cmd, false, "LoadBalancerConfigCommand failed due to uanble to copy haproxy configuration file");
                }

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Run command on domain router " + routerIp + ",  /root/loadbalancer.sh " + args);
                }

                result = SshHelper.sshExecute(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null, "/root/loadbalancer.sh " + args);

                if (!result.first()) {
                    String msg = "LoadBalancerConfigCommand on domain router " + routerIp + " failed. message: " + result.second();
                    s_logger.error(msg);

                    return new Answer(cmd, false, msg);
                }

                if (s_logger.isInfoEnabled()) {
                    s_logger.info("LoadBalancerConfigCommand on domain router " + routerIp + " completed");
                }
            } finally {
                SshHelper.sshExecute(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null, "rm " + tmpCfgFilePath);
            }

            return new Answer(cmd);
        } catch (Throwable e) {
            s_logger.error("Unexpected exception: " + e.toString(), e);
            return new Answer(cmd, false, "LoadBalancerConfigCommand failed due to " + VmwareHelper.getExceptionMessage(e));
        }
    }

    protected void assignPublicIpAddress(VirtualMachineMO vmMo, final String vmName, final String privateIpAddress, final String publicIpAddress, final boolean add, final boolean firstIP,
            final boolean sourceNat, final String vlanId, final String vlanGateway, final String vlanNetmask, final String vifMacAddress, String guestIp) throws Exception {

        String publicNeworkName = HypervisorHostHelper.getPublicNetworkNamePrefix(vlanId);
        Pair<Integer, VirtualDevice> publicNicInfo = vmMo.getNicDeviceIndex(publicNeworkName);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Find public NIC index, public network name: " + publicNeworkName + ", index: " + publicNicInfo.first());
        }

        boolean addVif = false;
        boolean removeVif = false;
        if (add && publicNicInfo.first().intValue() == -1) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Plug new NIC to associate" + privateIpAddress + " to " + publicIpAddress);
            }

            addVif = true;
        } else if (!add && firstIP) {
            removeVif = true;

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Unplug NIC " + publicNicInfo.first());
            }
        }

        if (addVif) {
            plugPublicNic(vmMo, vlanId, vifMacAddress);
            publicNicInfo = vmMo.getNicDeviceIndex(publicNeworkName);
            if (publicNicInfo.first().intValue() >= 0) {
                networkUsage(privateIpAddress, "addVif", "eth" + publicNicInfo.first());
            }
        }

        if (publicNicInfo.first().intValue() < 0) {
            String msg = "Failed to find DomR VIF to associate/disassociate IP with.";
            s_logger.error(msg);
            throw new InternalErrorException(msg);
        }

        String args = null;

        if (add) {
            args = " -A ";
        } else {
            args = " -D ";
        }
        String cidrSize = Long.toString(NetUtils.getCidrSize(vlanNetmask));
        if (sourceNat) {
            args += " -s ";
        }
        if (firstIP) {
            args += " -f ";
            args += " -l ";
            args += publicIpAddress + "/" + cidrSize;
        } else {
            args += " -l ";
            args += publicIpAddress;
        }

        args += " -c ";
        args += "eth" + publicNicInfo.first();
        
        args += " -g ";
        args += vlanGateway;

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Run command on domain router " + privateIpAddress + ", /root/ipassoc.sh " + args);
        }

        VmwareManager mgr = getServiceContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
        Pair<Boolean, String> result = SshHelper.sshExecute(privateIpAddress, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null, "/root/ipassoc.sh " + args);

        if (!result.first()) {
            s_logger.error("ipassoc command on domain router " + privateIpAddress + " failed. message: " + result.second());
            throw new Exception("ipassoc failed due to " + result.second());
        }

        if (removeVif) {
        	
        	String nicMasksStr = vmMo.getCustomFieldValue(CustomFieldConstants.CLOUD_NIC_MASK);
        	int nicMasks = Integer.parseInt(nicMasksStr);
        	nicMasks &= ~(1 << publicNicInfo.first().intValue());
        	vmMo.setCustomFieldValue(CustomFieldConstants.CLOUD_NIC_MASK, String.valueOf(nicMasks));
        	
            HostMO hostMo = vmMo.getRunningHost();
            List<NetworkDetails> networks = vmMo.getNetworksWithDetails();
            for (NetworkDetails netDetails : networks) {
                if (netDetails.getGCTag() != null && netDetails.getGCTag().equalsIgnoreCase("true")) {
                    if (netDetails.getVMMorsOnNetwork() == null || netDetails.getVMMorsOnNetwork().length == 1) {
                        cleanupNetwork(hostMo, netDetails);
                    }
                }
            }
        }

        if (s_logger.isInfoEnabled()) {
            s_logger.info("ipassoc command on domain router " + privateIpAddress + " completed");
        }
    }

    private void plugPublicNic(VirtualMachineMO vmMo, final String vlanId, final String vifMacAddress) throws Exception {
        // TODO : probably need to set traffic shaping
        Pair<ManagedObjectReference, String> networkInfo = HypervisorHostHelper.prepareNetwork(this._publicNetworkVSwitchName, "cloud.public",
        	vmMo.getRunningHost(), vlanId, null, null, this._ops_timeout, true);
        
        int nicIndex = allocPublicNicIndex(vmMo);
        
        try {
        	VirtualDevice[] nicDevices = vmMo.getNicDevices();
        	
        	VirtualEthernetCard device = (VirtualEthernetCard)nicDevices[nicIndex];
        	
    		VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
    		nicBacking.setDeviceName(networkInfo.second());
    		nicBacking.setNetwork(networkInfo.first());
    		device.setBacking(nicBacking);
        	
            VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
            VirtualDeviceConfigSpec[] deviceConfigSpecArray = new VirtualDeviceConfigSpec[1];
            deviceConfigSpecArray[0] = new VirtualDeviceConfigSpec();
            deviceConfigSpecArray[0].setDevice(device);
            deviceConfigSpecArray[0].setOperation(VirtualDeviceConfigSpecOperation.edit);
            
            vmConfigSpec.setDeviceChange(deviceConfigSpecArray);
            if(!vmMo.configureVm(vmConfigSpec)) {
                throw new Exception("Failed to configure devices when plugPublicNic");
            }
        } catch(Exception e) {
        
        	// restore allocation mask in case of exceptions
        	String nicMasksStr = vmMo.getCustomFieldValue(CustomFieldConstants.CLOUD_NIC_MASK);
        	int nicMasks = Integer.parseInt(nicMasksStr);
        	nicMasks &= ~(1 << nicIndex);
        	vmMo.setCustomFieldValue(CustomFieldConstants.CLOUD_NIC_MASK, String.valueOf(nicMasks));
        	
        	throw e;
        }
    }
    
    private int allocPublicNicIndex(VirtualMachineMO vmMo) throws Exception {
    	String nicMasksStr = vmMo.getCustomFieldValue(CustomFieldConstants.CLOUD_NIC_MASK);
    	if(nicMasksStr == null || nicMasksStr.isEmpty()) {
    		throw new Exception("Could not find NIC allocation info");
    	}
    	
    	int nicMasks = Integer.parseInt(nicMasksStr);
    	VirtualDevice[] nicDevices = vmMo.getNicDevices();
    	for(int i = 3; i < nicDevices.length; i++) {
    		if((nicMasks & (1 << i)) == 0) {
    			nicMasks |= (1 << i);
    			vmMo.setCustomFieldValue(CustomFieldConstants.CLOUD_NIC_MASK, String.valueOf(nicMasks));
    			return i;
    		}
    	}
    	
    	throw new Exception("Could not allocate a free public NIC");
    }

    protected Answer execute(IpAssocCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource IPAssocCommand: " + _gson.toJson(cmd));
        }

        int i = 0;
        String[] results = new String[cmd.getIpAddresses().length];

        VmwareContext context = getServiceContext();
        try {
            VmwareHypervisorHost hyperHost = getHyperHost(context);

            IpAddressTO[] ips = cmd.getIpAddresses();
            String routerName = cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME);
            String controlIp = VmwareResource.getRouterSshControlIp(cmd);

            VirtualMachineMO vmMo = hyperHost.findVmOnHyperHost(routerName);

            // command may sometimes be redirect to a wrong host, we relax
            // the check and will try to find it within cluster
            if(vmMo == null) {
                if(hyperHost instanceof HostMO) {
                    ClusterMO clusterMo = new ClusterMO(hyperHost.getContext(),
                        ((HostMO)hyperHost).getParentMor());
                    vmMo = clusterMo.findVmOnHyperHost(routerName);
                }
            }

            if (vmMo == null) {
                String msg = "Router " + routerName + " no longer exists to execute IPAssoc command";
                s_logger.error(msg);
                throw new Exception(msg);
            }

            for (IpAddressTO ip : ips) {
                assignPublicIpAddress(vmMo, routerName, controlIp, ip.getPublicIp(), ip.isAdd(), ip.isFirstIP(), ip.isSourceNat(), ip.getVlanId(), ip.getVlanGateway(), ip.getVlanNetmask(),
                        ip.getVifMacAddress(), ip.getGuestIp());
                results[i++] = ip.getPublicIp() + " - success";
            }
        } catch (Throwable e) {
            s_logger.error("Unexpected exception: " + e.toString() + " will shortcut rest of IPAssoc commands", e);

            for (; i < cmd.getIpAddresses().length; i++) {
                results[i++] = IpAssocAnswer.errorResult;
            }
        }

        return new IpAssocAnswer(cmd, results);
    }

    protected Answer execute(SavePasswordCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource SavePasswordCommand. vmName: " + cmd.getVmName() + ", vmIp: " + cmd.getVmIpAddress() + ", password: " 
            	+ StringUtils.getMaskedPasswordForDisplay(cmd.getPassword()));
        }

        String controlIp = getRouterSshControlIp(cmd);
        final String password = cmd.getPassword();
        final String vmIpAddress = cmd.getVmIpAddress();

        // Run save_password_to_domr.sh
        String args = " -v " + vmIpAddress;

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Run command on domain router " + controlIp + ", /root/savepassword.sh " + args + " -p " + StringUtils.getMaskedPasswordForDisplay(cmd.getPassword()));
        }
        
        args += " -p " + password;


        try {
            VmwareManager mgr = getServiceContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
            Pair<Boolean, String> result = SshHelper.sshExecute(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null, "/root/savepassword.sh " + args);

            if (!result.first()) {
                s_logger.error("savepassword command on domain router " + controlIp + " failed, message: " + result.second());

                return new Answer(cmd, false, "SavePassword failed due to " + result.second());
            }

            if (s_logger.isInfoEnabled()) {
                s_logger.info("savepassword command on domain router " + controlIp + " completed");
            }

        } catch (Throwable e) {
            String msg = "SavePasswordCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new Answer(cmd, false, msg);
        }
        return new Answer(cmd);
    }

    protected Answer execute(DhcpEntryCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource DhcpEntryCommand: " + _gson.toJson(cmd));
        }

        // ssh -p 3922 -o StrictHostKeyChecking=no -i $cert root@$domr "/root/edithosts.sh $mac $ip $vm $dfltrt $ns $staticrt" >/dev/null
        String args = " " + cmd.getVmMac();
        args += " " + cmd.getVmIpAddress();
        args += " " + cmd.getVmName();
        
        if (cmd.getDefaultRouter() != null) {
            args += " " + cmd.getDefaultRouter();
        }
        
        if (cmd.getDefaultDns() != null) {
            args += " " + cmd.getDefaultDns();
        }

        if (cmd.getStaticRoutes() != null) {
            args +=  " " + cmd.getStaticRoutes();
        }
        
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Run command on domR " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP) + ", /root/edithosts.sh " + args);
        }

        try {
            VmwareManager mgr = getServiceContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
            String controlIp = getRouterSshControlIp(cmd);
            Pair<Boolean, String> result = SshHelper.sshExecute(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null,
                    "/root/edithosts.sh " + args);

            if (!result.first()) {
                s_logger.error("dhcp_entry command on domR " + controlIp + " failed, message: " + result.second());

                return new Answer(cmd, false, "DhcpEntry failed due to " + result.second());
            }

            if (s_logger.isInfoEnabled()) {
                s_logger.info("dhcp_entry command on domain router " + controlIp + " completed");
            }

        } catch (Throwable e) {
            String msg = "DhcpEntryCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new Answer(cmd, false, msg);
        }

        return new Answer(cmd);
    }
    
    protected Answer execute(CheckRouterCommand cmd) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Executing resource CheckRouterCommand: " + _gson.toJson(cmd));
            s_logger.debug("Run command on domR " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP) + ", /root/checkrouter.sh ");
        }

        Pair<Boolean, String> result;
        try {
            VmwareManager mgr = getServiceContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
            String controlIp = getRouterSshControlIp(cmd);
            result = SshHelper.sshExecute(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null,
                    "/root/checkrouter.sh ");

            if (!result.first()) {
                s_logger.error("check router command on domR " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP) + " failed, message: " + result.second());

                return new CheckRouterAnswer(cmd, "CheckRouter failed due to " + result.second());
            }

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("check router command on domain router " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP) + " completed");
            }
        } catch (Throwable e) {
            String msg = "CheckRouterCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new CheckRouterAnswer(cmd, msg);
        }
        return new CheckRouterAnswer(cmd, result.second(), true);
    }
    
    protected Answer execute(GetDomRVersionCmd cmd) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Executing resource GetDomRVersionCmd: " + _gson.toJson(cmd));
            s_logger.debug("Run command on domR " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP) + ", /opt/cloud/bin/get_template_version.sh ");
        }

        Pair<Boolean, String> result;
        try {
            VmwareManager mgr = getServiceContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
            String controlIp = getRouterSshControlIp(cmd);
            result = SshHelper.sshExecute(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null,
                    "/opt/cloud/bin/get_template_version.sh ");

            if (!result.first()) {
                s_logger.error("GetDomRVersionCmd on domR " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP) + " failed, message: " + result.second());

                return new GetDomRVersionAnswer(cmd, "GetDomRVersionCmd failed due to " + result.second());
            }

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("GetDomRVersionCmd on domain router " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP) + " completed");
            }
        } catch (Throwable e) {
            String msg = "GetDomRVersionCmd failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new GetDomRVersionAnswer(cmd, msg);
        }
        String[] lines = result.second().split("&");
        if (lines.length != 2) {
            return new GetDomRVersionAnswer(cmd, result.second());
        }
        return new GetDomRVersionAnswer(cmd, result.second(), lines[0], lines[1]);
    }
    
    protected Answer execute(BumpUpPriorityCommand cmd) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Executing resource BumpUpPriorityCommand: " + _gson.toJson(cmd));
            s_logger.debug("Run command on domR " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP) + ", /root/bumpup_priority.sh ");
        }

        Pair<Boolean, String> result;
        try {
            VmwareManager mgr = getServiceContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
            String controlIp = getRouterSshControlIp(cmd);
            result = SshHelper.sshExecute(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null,
                    "/root/bumpup_priority.sh ");

            if (!result.first()) {
                s_logger.error("BumpUpPriority command on domR " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP) + " failed, message: " + result.second());

                return new Answer(cmd, false, "BumpUpPriorityCommand failed due to " + result.second());
            }

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("BumpUpPriorityCommand on domain router " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP) + " completed");
            }
        } catch (Throwable e) {
            String msg = "BumpUpPriorityCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new Answer(cmd, false, msg);
        }
        if (result.second() == null || result.second().isEmpty()) {
            return new Answer(cmd, true, result.second());
        }
        return new Answer(cmd, false, result.second());
    }

    protected Answer execute(VmDataCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource VmDataCommand: " + _gson.toJson(cmd));
        }

        String routerPrivateIpAddress = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        String controlIp = getRouterSshControlIp(cmd);
        
        String vmIpAddress = cmd.getVmIpAddress();
        List<String[]> vmData = cmd.getVmData();
        String[] vmDataArgs = new String[vmData.size() * 2 + 4];
        vmDataArgs[0] = "routerIP";
        vmDataArgs[1] = routerPrivateIpAddress;
        vmDataArgs[2] = "vmIP";
        vmDataArgs[3] = vmIpAddress;
        int i = 4;
        for (String[] vmDataEntry : vmData) {
            String folder = vmDataEntry[0];
            String file = vmDataEntry[1];
            String contents = (vmDataEntry[2] != null) ? vmDataEntry[2] : "none";

            vmDataArgs[i] = folder + "," + file;
            vmDataArgs[i + 1] = contents;
            i += 2;
        }

        String content = encodeDataArgs(vmDataArgs);
        String tmpFileName = UUID.randomUUID().toString();

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Run vm_data command on domain router " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP) + ", data: " + content);
        }

        try {
            VmwareManager mgr = getServiceContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
            SshHelper.scpTo(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null, "/tmp", content.getBytes(), tmpFileName, null);

            try {
                Pair<Boolean, String> result = SshHelper.sshExecute(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null,
                        "/root/userdata.py " + tmpFileName);

                if (!result.first()) {
                    s_logger.error("vm_data command on domain router " + controlIp + " failed. messge: " + result.second());
                    return new Answer(cmd, false, "VmDataCommand failed due to " + result.second());
                }
            } finally {

                SshHelper.sshExecute(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null, "rm /tmp/" + tmpFileName);
            }

            if (s_logger.isInfoEnabled()) {
                s_logger.info("vm_data command on domain router " + controlIp + " completed");
            }

        } catch (Throwable e) {
            String msg = "VmDataCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new Answer(cmd, false, msg);
        }
        return new Answer(cmd);
    }

    private String encodeDataArgs(String[] dataArgs) {
        StringBuilder sb = new StringBuilder();

        for (String arg : dataArgs) {
            sb.append(arg);
            sb.append("\n");
        }

        return sb.toString();
    }

    protected CheckSshAnswer execute(CheckSshCommand cmd) {
        String vmName = cmd.getName();
        String privateIp = cmd.getIp();
        int cmdPort = cmd.getPort();

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Ping command port, " + privateIp + ":" + cmdPort);
        }

        try {
            String result = connect(cmd.getName(), privateIp, cmdPort);
            if (result != null) {
                s_logger.error("Can not ping System vm " + vmName + "due to:" + result);
                return new CheckSshAnswer(cmd, "Can not ping System vm " + vmName + "due to:" + result);
            }
        } catch (Exception e) {
            s_logger.error("Can not ping System vm " + vmName + "due to exception");
            return new CheckSshAnswer(cmd, e);
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Ping command port succeeded for vm " + vmName);
        }

        if (VirtualMachineName.isValidRouterName(vmName)) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Execute network usage setup command on " + vmName);
            }
            networkUsage(privateIp, "create", null);
        }

        return new CheckSshAnswer(cmd);
    }

    private VolumeTO[] validateDisks(VolumeTO[] disks) {
        List<VolumeTO> validatedDisks = new ArrayList<VolumeTO>();

        for (VolumeTO vol : disks) {
            if (vol.getPoolUuid() != null && !vol.getPoolUuid().isEmpty()) {
                validatedDisks.add(vol);
            } else if (vol.getPoolType() == StoragePoolType.ISO && (vol.getPath() != null && !vol.getPath().isEmpty())) {
                validatedDisks.add(vol);
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Drop invalid disk option, volumeTO: " + _gson.toJson(vol));
                }
            }
        }

        return validatedDisks.toArray(new VolumeTO[0]);
    }

    protected StartAnswer execute(StartCommand cmd) {

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource StartCommand: " + _gson.toJson(cmd));
        }

        VirtualMachineTO vmSpec = cmd.getVirtualMachine();
        String vmName = vmSpec.getName();
        
        State state = State.Stopped;
        VmwareContext context = getServiceContext();
        try {
            VmwareManager mgr = context.getStockObject(VmwareManager.CONTEXT_STOCK_NAME);

            // mark VM as starting state so that sync() can know not to report stopped too early
            synchronized (_vms) {
                _vms.put(vmName, State.Starting);
            }

            VirtualEthernetCardType nicDeviceType = VirtualEthernetCardType.valueOf(vmSpec.getDetails().get(VmDetailConstants.NIC_ADAPTER));
            if(s_logger.isDebugEnabled())
            	s_logger.debug("VM " + vmName + " will be started with NIC device type: " + nicDeviceType);
            
            VmwareHypervisorHost hyperHost = getHyperHost(context);
            VolumeTO[] disks = validateDisks(vmSpec.getDisks());
            assert (disks.length > 0);
            NicTO[] nics = vmSpec.getNics();

            HashMap<String, Pair<ManagedObjectReference, DatastoreMO>> dataStoresDetails = inferDatastoreDetailsFromDiskInfo(hyperHost, context, disks);
            if ((dataStoresDetails == null) || (dataStoresDetails.isEmpty()) ){
                String msg = "Unable to locate datastore details of the volumes to be attached";
                s_logger.error(msg);
                throw new Exception(msg);
            }

            VirtualMachineMO vmMo = hyperHost.findVmOnHyperHost(vmName);
            if (vmMo != null) {
                s_logger.info("VM " + vmName + " already exists, tear down devices for reconfiguration");
                if (getVmState(vmMo) != State.Stopped)
                    vmMo.safePowerOff(_shutdown_waitMs);
                vmMo.tearDownDevices(new Class<?>[] { VirtualDisk.class, VirtualEthernetCard.class });
                vmMo.ensureScsiDeviceController();
            } else {
                ManagedObjectReference morDc = hyperHost.getHyperHostDatacenter();
                assert (morDc != null);

                vmMo = hyperHost.findVmOnPeerHyperHost(vmName);
                if (vmMo != null) {
                    if (s_logger.isInfoEnabled()) {
                        s_logger.info("Found vm " + vmName + " at other host, relocate to " + hyperHost.getHyperHostName());
                    }

                    takeVmFromOtherHyperHost(hyperHost, vmName);

                    if (getVmState(vmMo) != State.Stopped)
                        vmMo.safePowerOff(_shutdown_waitMs);
                    vmMo.tearDownDevices(new Class<?>[] { VirtualDisk.class, VirtualEthernetCard.class });
                    vmMo.ensureScsiDeviceController();
                } else {
                    int ramMb = (int) (vmSpec.getMinRam() / (1024 * 1024));
                    Pair<ManagedObjectReference, DatastoreMO> rootDiskDataStoreDetails = null;
                    for (VolumeTO vol : disks) {
                        if (vol.getType() == Volume.Type.ROOT) {
                        	rootDiskDataStoreDetails = dataStoresDetails.get(vol.getPoolUuid());
                        }
                    }

                    assert (vmSpec.getSpeed() != null) && (rootDiskDataStoreDetails != null);
                    if (!hyperHost.createBlankVm(vmName, vmSpec.getCpus(), vmSpec.getSpeed().intValue(), 
                    		getReserveCpuMHz(vmSpec.getSpeed().intValue()), vmSpec.getLimitCpuUse(), ramMb, getReserveMemMB(ramMb),
                    	translateGuestOsIdentifier(vmSpec.getArch(), vmSpec.getOs()).toString(), rootDiskDataStoreDetails.first(), false)) {
                        throw new Exception("Failed to create VM. vmName: " + vmName);
                    }
                }

                vmMo = hyperHost.findVmOnHyperHost(vmName);
                if (vmMo == null) {
                    throw new Exception("Failed to find the newly create or relocated VM. vmName: " + vmName);
                }
            }

            int totalChangeDevices = disks.length + nics.length;
            VolumeTO volIso = null;
            if (vmSpec.getType() != VirtualMachine.Type.User) {
                // system VM needs a patch ISO
                totalChangeDevices++;
            } else {
                for (VolumeTO vol : disks) {
                    if (vol.getType() == Volume.Type.ISO) {
                        volIso = vol;
                        break;
                    }
                }

                if (volIso == null)
                    totalChangeDevices++;
            }

            VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
            int ramMb = (int) (vmSpec.getMinRam() / (1024 * 1024));
            VmwareHelper.setBasicVmConfig(vmConfigSpec, vmSpec.getCpus(), vmSpec.getSpeed().intValue(), 
            	getReserveCpuMHz(vmSpec.getSpeed().intValue()), ramMb, getReserveMemMB(ramMb),
        		translateGuestOsIdentifier(vmSpec.getArch(), vmSpec.getOs()).toString(), vmSpec.getLimitCpuUse());
            
            VirtualDeviceConfigSpec[] deviceConfigSpecArray = new VirtualDeviceConfigSpec[totalChangeDevices];
            int i = 0;
            int ideControllerKey = vmMo.getIDEDeviceControllerKey();
            int scsiControllerKey = vmMo.getScsiDeviceControllerKey();
            int controllerKey;
            String datastoreDiskPath;

            // prepare systemvm patch ISO
            if (vmSpec.getType() != VirtualMachine.Type.User) {
                // attach ISO (for patching of system VM)
                String secStoreUrl = mgr.getSecondaryStorageStoreUrl(Long.parseLong(_dcId));
                if(secStoreUrl == null) {
                    String msg = "secondary storage for dc " + _dcId + " is not ready yet?";
                    throw new Exception(msg);
                }
                mgr.prepareSecondaryStorageStore(secStoreUrl);
                
                ManagedObjectReference morSecDs = prepareSecondaryDatastoreOnHost(secStoreUrl);
                if (morSecDs == null) {
                    String msg = "Failed to prepare secondary storage on host, secondary store url: " + secStoreUrl;
                    throw new Exception(msg);
                }
                DatastoreMO secDsMo = new DatastoreMO(hyperHost.getContext(), morSecDs);

                deviceConfigSpecArray[i] = new VirtualDeviceConfigSpec();
                Pair<VirtualDevice, Boolean> isoInfo = VmwareHelper.prepareIsoDevice(vmMo, String.format("[%s] systemvm/%s", secDsMo.getName(), mgr.getSystemVMIsoFileNameOnDatastore()), 
                	secDsMo.getMor(), true, true, i, i + 1);
                deviceConfigSpecArray[i].setDevice(isoInfo.first());
                if (isoInfo.second()) {
                	if(s_logger.isDebugEnabled())
                		s_logger.debug("Prepare ISO volume at new device " + _gson.toJson(isoInfo.first()));
                    deviceConfigSpecArray[i].setOperation(VirtualDeviceConfigSpecOperation.add);
                } else {
                	if(s_logger.isDebugEnabled())
                		s_logger.debug("Prepare ISO volume at existing device " + _gson.toJson(isoInfo.first()));
                    deviceConfigSpecArray[i].setOperation(VirtualDeviceConfigSpecOperation.edit);
                }
                i++;
            } else {
                // we will always plugin a CDROM device
                if (volIso != null && volIso.getPath() != null && !volIso.getPath().isEmpty()) {
                    Pair<String, ManagedObjectReference> isoDatastoreInfo = getIsoDatastoreInfo(hyperHost, volIso.getPath());
                    assert (isoDatastoreInfo != null);
                    assert (isoDatastoreInfo.second() != null);

                    deviceConfigSpecArray[i] = new VirtualDeviceConfigSpec();
                    Pair<VirtualDevice, Boolean> isoInfo = VmwareHelper.prepareIsoDevice(vmMo, isoDatastoreInfo.first(), isoDatastoreInfo.second(), true, true, i, i + 1);
                    deviceConfigSpecArray[i].setDevice(isoInfo.first());
                    if (isoInfo.second()) {
                    	if(s_logger.isDebugEnabled())
                    		s_logger.debug("Prepare ISO volume at new device " + _gson.toJson(isoInfo.first()));
                    	deviceConfigSpecArray[i].setOperation(VirtualDeviceConfigSpecOperation.add);
                    } else {
                    	if(s_logger.isDebugEnabled())
                    		s_logger.debug("Prepare ISO volume at existing device " + _gson.toJson(isoInfo.first()));
                        deviceConfigSpecArray[i].setOperation(VirtualDeviceConfigSpecOperation.edit);
                    }
                } else {
                    deviceConfigSpecArray[i] = new VirtualDeviceConfigSpec();
                    Pair<VirtualDevice, Boolean> isoInfo = VmwareHelper.prepareIsoDevice(vmMo, null, null, true, true, i, i + 1);
                    deviceConfigSpecArray[i].setDevice(isoInfo.first());
                    if (isoInfo.second()) {
                    	if(s_logger.isDebugEnabled())
                    		s_logger.debug("Prepare ISO volume at existing device " + _gson.toJson(isoInfo.first()));
                    	
                        deviceConfigSpecArray[i].setOperation(VirtualDeviceConfigSpecOperation.add);
                    } else {
                    	if(s_logger.isDebugEnabled())
                    		s_logger.debug("Prepare ISO volume at existing device " + _gson.toJson(isoInfo.first()));
                    	
                        deviceConfigSpecArray[i].setOperation(VirtualDeviceConfigSpecOperation.edit);
                    }
                }
                i++;
            }

            for (VolumeTO vol : sortVolumesByDeviceId(disks)) {
                deviceConfigSpecArray[i] = new VirtualDeviceConfigSpec();

                if (vol.getType() == Volume.Type.ISO) {
                    controllerKey = ideControllerKey;
                } else {
                    if(vol.getType() == Volume.Type.ROOT) {
                    	if(vmSpec.getDetails() != null && vmSpec.getDetails().get(VmDetailConstants.ROOK_DISK_CONTROLLER) != null)
                    	{
                    		if(vmSpec.getDetails().get(VmDetailConstants.ROOK_DISK_CONTROLLER).equalsIgnoreCase("scsi"))
    	                		controllerKey = scsiControllerKey;
    	                	else
    	                		controllerKey = ideControllerKey;
                    	} else {
                    		controllerKey = scsiControllerKey;
                    	}
                    } else {
                        // DATA volume always use SCSI device
                        controllerKey = scsiControllerKey;
                    }
                }

                if (vol.getType() != Volume.Type.ISO) {
                    Pair<ManagedObjectReference, DatastoreMO> volumeDsDetails = dataStoresDetails.get(vol.getPoolUuid());
                    assert (volumeDsDetails != null);
                	VirtualDevice device;
                    datastoreDiskPath = String.format("[%s] %s.vmdk", volumeDsDetails.second().getName(), vol.getPath());
                    String chainInfo = vol.getChainInfo();

                    if (chainInfo != null && !chainInfo.isEmpty()) {
                        String[] diskChain = _gson.fromJson(chainInfo, String[].class);
                        if (diskChain == null || diskChain.length < 1) {
                            s_logger.warn("Empty previously-saved chain info, fall back to the original");
                            device = VmwareHelper.prepareDiskDevice(vmMo, controllerKey, new String[] { datastoreDiskPath }, volumeDsDetails.first(), i, i + 1);
                        } else {
                            s_logger.info("Attach the disk with stored chain info: " + chainInfo);
                            for (int j = 0; j < diskChain.length; j++) {
                                diskChain[j] = String.format("[%s] %s", volumeDsDetails.second().getName(), diskChain[j]);
                            }

                            device = VmwareHelper.prepareDiskDevice(vmMo, controllerKey, diskChain, volumeDsDetails.first(), i, i + 1);
                        }
                    } else {
                        device = VmwareHelper.prepareDiskDevice(vmMo, controllerKey, new String[] { datastoreDiskPath }, volumeDsDetails.first(), i, i + 1);
                    }
                    deviceConfigSpecArray[i].setDevice(device);
                    deviceConfigSpecArray[i].setOperation(VirtualDeviceConfigSpecOperation.add);

                	if(s_logger.isDebugEnabled())
                		s_logger.debug("Prepare volume at new device " + _gson.toJson(device));
                    
                    i++;
                }
            }

            VirtualDevice nic;
            int nicMask = 0;
            int nicCount = 0;
            for (NicTO nicTo : sortNicsByDeviceId(nics)) {
                s_logger.info("Prepare NIC device based on NicTO: " + _gson.toJson(nicTo));

                Pair<ManagedObjectReference, String> networkInfo = prepareNetworkFromNicInfo(vmMo.getRunningHost(), nicTo);
                
                nic = VmwareHelper.prepareNicDevice(vmMo, networkInfo.first(), nicDeviceType, networkInfo.second(), nicTo.getMac(), i, i + 1, true, true);
                deviceConfigSpecArray[i] = new VirtualDeviceConfigSpec();
                deviceConfigSpecArray[i].setDevice(nic);
                deviceConfigSpecArray[i].setOperation(VirtualDeviceConfigSpecOperation.add);
                
            	if(s_logger.isDebugEnabled())
            		s_logger.debug("Prepare NIC at new device " + _gson.toJson(deviceConfigSpecArray[i]));
 
            	// this is really a hacking for DomR, upon DomR startup, we will reset all the NIC allocation after eth3
                if(nicCount < 3)
                	nicMask |= (1 << nicCount);
                
                i++;
                nicCount++;
            }

            vmConfigSpec.setDeviceChange(deviceConfigSpecArray);

            // pass boot arguments through machine.id & perform customized options to VMX
    
            Map<String, String> vmDetailOptions = validateVmDetails(vmSpec.getDetails());
            OptionValue[] extraOptions = new OptionValue[2 + vmDetailOptions.size()];
            extraOptions[0] = new OptionValue();
            extraOptions[0].setKey("machine.id");
            extraOptions[0].setValue(vmSpec.getBootArgs());
            
            extraOptions[1] = new OptionValue();
            extraOptions[1].setKey("devices.hotplug");
            extraOptions[1].setValue("true");

            int j = 2;
            for(Map.Entry<String, String> entry : vmDetailOptions.entrySet()) {
            	extraOptions[j] = new OptionValue();
            	extraOptions[j].setKey(entry.getKey());
            	extraOptions[j].setValue(entry.getValue());
            	j++;
            }
            
            String keyboardLayout = null;
            if(vmSpec.getDetails() != null)
            	keyboardLayout = vmSpec.getDetails().get(VmDetailConstants.KEYBOARD);
            vmConfigSpec.setExtraConfig(configureVnc(extraOptions, hyperHost, vmName, vmSpec.getVncPassword(), keyboardLayout));

            if (!vmMo.configureVm(vmConfigSpec)) {
                throw new Exception("Failed to configure VM before start. vmName: " + vmName);
            }
            
            vmMo.setCustomFieldValue(CustomFieldConstants.CLOUD_NIC_MASK, String.valueOf(nicMask));

            if (!vmMo.powerOn()) {
                throw new Exception("Failed to start VM. vmName: " + vmName);
            }

            state = State.Running;
            return new StartAnswer(cmd);
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "StartCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.warn(msg, e);
            return new StartAnswer(cmd, msg);
        } finally {
            synchronized (_vms) {
                if (state != State.Stopped) {
                    _vms.put(vmName, state);
                } else {
                    _vms.remove(vmName);
                }
            }
        }
    }
    
    private Map<String, String> validateVmDetails(Map<String, String> vmDetails) {
    	Map<String, String> validatedDetails = new HashMap<String, String>();
    	
    	if(vmDetails != null && vmDetails.size() > 0) {
    		for(Map.Entry<String, String> entry : vmDetails.entrySet()) {
    			if("machine.id".equalsIgnoreCase(entry.getKey()))
    				continue;
    			else if("devices.hotplug".equalsIgnoreCase(entry.getKey()))
    				continue;
    			else if("RemoteDisplay.vnc.enabled".equalsIgnoreCase(entry.getKey()))
    				continue;
    			else if("RemoteDisplay.vnc.password".equalsIgnoreCase(entry.getKey()))
    				continue;
    			else if("RemoteDisplay.vnc.port".equalsIgnoreCase(entry.getKey()))
    				continue;
    			else if("RemoteDisplay.vnc.keymap".equalsIgnoreCase(entry.getKey()))
    				continue;
    			else
    				validatedDetails.put(entry.getKey(), entry.getValue());
    		}
    	}
    	return validatedDetails;
    }

    private int getReserveCpuMHz(int cpuMHz) {
    	if(this._reserveCpu) {
    		return (int)(cpuMHz / this._cpuOverprovisioningFactor);
    	}
    	
    	return 0;
    }
    
    private int getReserveMemMB(int memMB) {
    	if(this._reserveMem) {
    		return (int)(memMB / this._memOverprovisioningFactor);
    	}
    	
    	return 0;
    }
    
    private NicTO[] sortNicsByDeviceId(NicTO[] nics) {

        List<NicTO> listForSort = new ArrayList<NicTO>();
        for (NicTO nic : nics) {
            listForSort.add(nic);
        }
        Collections.sort(listForSort, new Comparator<NicTO>() {

            @Override
            public int compare(NicTO arg0, NicTO arg1) {
                if (arg0.getDeviceId() < arg1.getDeviceId()) {
                    return -1;
                } else if (arg0.getDeviceId() == arg1.getDeviceId()) {
                    return 0;
                }

                return 1;
            }
        });

        return listForSort.toArray(new NicTO[0]);
    }
    
    private VolumeTO[] sortVolumesByDeviceId(VolumeTO[] volumes) {

        List<VolumeTO> listForSort = new ArrayList<VolumeTO>();
        for (VolumeTO vol : volumes) {
            listForSort.add(vol);
        }
        Collections.sort(listForSort, new Comparator<VolumeTO>() {

            @Override
            public int compare(VolumeTO arg0, VolumeTO arg1) {
                if (arg0.getDeviceId() < arg1.getDeviceId()) {
                    return -1;
                } else if (arg0.getDeviceId() == arg1.getDeviceId()) {
                    return 0;
                }

                return 1;
            }
        });

        return listForSort.toArray(new VolumeTO[0]);
    }

    private HashMap<String, Pair<ManagedObjectReference, DatastoreMO>> inferDatastoreDetailsFromDiskInfo(VmwareHypervisorHost hyperHost, VmwareContext context, VolumeTO[] disks) throws Exception {
        HashMap<String ,Pair<ManagedObjectReference, DatastoreMO>> poolMors = new HashMap<String, Pair<ManagedObjectReference, DatastoreMO>>();

        assert (hyperHost != null) && (context != null);
        for (VolumeTO vol : disks) {
            if (vol.getType() != Volume.Type.ISO) {
                String poolUuid = vol.getPoolUuid();
                if(poolMors.get(poolUuid) == null) {
                    ManagedObjectReference morDataStore = hyperHost.findDatastore(poolUuid);
                    if (morDataStore == null) {
                        String msg = "Failed to get the mounted datastore for the volume's pool " + poolUuid;
                        s_logger.error(msg);
                        throw new Exception(msg);
                    }
                    poolMors.put(vol.getPoolUuid(), new Pair<ManagedObjectReference, DatastoreMO> (morDataStore, new DatastoreMO(context, morDataStore)));
                }
            }
        }
        return poolMors;
    }

    private String getVlanInfo(NicTO nicTo) {
        if (nicTo.getBroadcastType() == BroadcastDomainType.Native) {
            return Vlan.UNTAGGED;
        }

        if (nicTo.getBroadcastType() == BroadcastDomainType.Vlan) {
            if (nicTo.getBroadcastUri() != null) {
                return nicTo.getBroadcastUri().getHost();
            } else {
                s_logger.warn("BroadcastType is not claimed as VLAN, but without vlan info in broadcast URI");
                return Vlan.UNTAGGED;
            }
        }

        s_logger.warn("Unrecognized broadcast type in VmwareResource, type: " + nicTo.getBroadcastType().toString());
        return Vlan.UNTAGGED;
    }

    private Pair<ManagedObjectReference, String> prepareNetworkFromNicInfo(HostMO hostMo, NicTO nicTo) throws Exception {
        
        String switchName =  getTargetSwitch(nicTo);
        String namePrefix = getNetworkNamePrefix(nicTo);
        
        s_logger.info("Prepare network on vSwitch: " + switchName + " with name prefix: " + namePrefix);
        return HypervisorHostHelper.prepareNetwork(switchName, namePrefix, hostMo, getVlanInfo(nicTo), 
                nicTo.getNetworkRateMbps(), nicTo.getNetworkRateMulticastMbps(), _ops_timeout, 
                !namePrefix.startsWith("cloud.private"));
    }
    
    private String getTargetSwitch(NicTO nicTo) throws Exception {
        if(nicTo.getName() != null && !nicTo.getName().isEmpty())
            return nicTo.getName();
        
        if (nicTo.getType() == Networks.TrafficType.Guest) {
            return this._guestNetworkVSwitchName;
        } else if (nicTo.getType() == Networks.TrafficType.Control || nicTo.getType() == Networks.TrafficType.Management) {
            return this._privateNetworkVSwitchName;
        } else if (nicTo.getType() == Networks.TrafficType.Public) {
            return this._publicNetworkVSwitchName;
        } else if (nicTo.getType() == Networks.TrafficType.Storage) {
            return this._privateNetworkVSwitchName;
        } else if (nicTo.getType() == Networks.TrafficType.Vpn) {
            throw new Exception("Unsupported traffic type: " + nicTo.getType().toString());
        } else {
            throw new Exception("Unsupported traffic type: " + nicTo.getType().toString());
        }
    }
    
    private String getNetworkNamePrefix(NicTO nicTo) throws Exception {
        if (nicTo.getType() == Networks.TrafficType.Guest) {
            return "cloud.guest";
        } else if (nicTo.getType() == Networks.TrafficType.Control || nicTo.getType() == Networks.TrafficType.Management) {
            return "cloud.private";
        } else if (nicTo.getType() == Networks.TrafficType.Public) {
            return "cloud.public";
        } else if (nicTo.getType() == Networks.TrafficType.Storage) {
            return "cloud.storage";
        } else if (nicTo.getType() == Networks.TrafficType.Vpn) {
            throw new Exception("Unsupported traffic type: " + nicTo.getType().toString());
        } else {
            throw new Exception("Unsupported traffic type: " + nicTo.getType().toString());
        }
    }

    protected synchronized Answer execute(final RemoteAccessVpnCfgCommand cmd) {
        String controlIp = getRouterSshControlIp(cmd);
        StringBuffer argsBuf = new StringBuffer();
        if (cmd.isCreate()) {
            argsBuf.append(" -r ").append(cmd.getIpRange()).append(" -p ").append(cmd.getPresharedKey()).append(" -s ").append(cmd.getVpnServerIp()).append(" -l ").append(cmd.getLocalIp())
            .append(" -c ");

        } else {
            argsBuf.append(" -d ").append(" -s ").append(cmd.getVpnServerIp());
        }

        try {
            VmwareManager mgr = getServiceContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Executing /opt/cloud/bin/vpn_lt2p.sh ");
            }

            Pair<Boolean, String> result = SshHelper.sshExecute(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null, "/opt/cloud/bin/vpn_l2tp.sh " + argsBuf.toString());

            if (!result.first()) {
                s_logger.error("RemoteAccessVpnCfg command on domR failed, message: " + result.second());

                return new Answer(cmd, false, "RemoteAccessVpnCfg command failed due to " + result.second());
            }

            if (s_logger.isInfoEnabled()) {
                s_logger.info("RemoteAccessVpnCfg command on domain router " + argsBuf.toString() + " completed");
            }

        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "RemoteAccessVpnCfg command failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new Answer(cmd, false, msg);
        }

        return new Answer(cmd);
    }

    protected synchronized Answer execute(final VpnUsersCfgCommand cmd) {
        VmwareManager mgr = getServiceContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);

        String controlIp = getRouterSshControlIp(cmd);
        for (VpnUsersCfgCommand.UsernamePassword userpwd : cmd.getUserpwds()) {
            StringBuffer argsBuf = new StringBuffer();
            if (!userpwd.isAdd()) {
                argsBuf.append(" -U ").append(userpwd.getUsername());
            } else {
                argsBuf.append(" -u ").append(userpwd.getUsernamePassword());
            }

            try {

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Executing /opt/cloud/bin/vpn_lt2p.sh ");
                }

                Pair<Boolean, String> result = SshHelper.sshExecute(controlIp, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null, "/opt/cloud/bin/vpn_l2tp.sh " + argsBuf.toString());

                if (!result.first()) {
                    s_logger.error("VpnUserCfg command on domR failed, message: " + result.second());

                    return new Answer(cmd, false, "VpnUserCfg command failed due to " + result.second());
                }
            } catch (Throwable e) {
                if (e instanceof RemoteException) {
                    s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                    invalidateServiceContext();
                }

                String msg = "VpnUserCfg command failed due to " + VmwareHelper.getExceptionMessage(e);
                s_logger.error(msg, e);
                return new Answer(cmd, false, msg);
            }
        }

        return new Answer(cmd);
    }

    private VirtualMachineMO takeVmFromOtherHyperHost(VmwareHypervisorHost hyperHost, String vmName) throws Exception {

        VirtualMachineMO vmMo = hyperHost.findVmOnPeerHyperHost(vmName);
        if (vmMo != null) {
            ManagedObjectReference morTargetPhysicalHost = hyperHost.findMigrationTarget(vmMo);
            if (morTargetPhysicalHost == null) {
                String msg = "VM " + vmName + " is on other host and we have no resource available to migrate and start it here";
                s_logger.error(msg);
                throw new Exception(msg);
            }

            if (!vmMo.relocate(morTargetPhysicalHost)) {
                String msg = "VM " + vmName + " is on other host and we failed to relocate it here";
                s_logger.error(msg);
                throw new Exception(msg);
            }

            return vmMo;
        }
        return null;
    }

    // isoUrl sample content :
    // nfs://192.168.10.231/export/home/kelven/vmware-test/secondary/template/tmpl/2/200//200-2-80f7ee58-6eff-3a2d-bcb0-59663edf6d26.iso
    private Pair<String, ManagedObjectReference> getIsoDatastoreInfo(VmwareHypervisorHost hyperHost, String isoUrl) throws Exception {

        assert (isoUrl != null);
        int isoFileNameStartPos = isoUrl.lastIndexOf("/");
        if (isoFileNameStartPos < 0) {
            throw new Exception("Invalid ISO path info");
        }

        String isoFileName = isoUrl.substring(isoFileNameStartPos);

        int templateRootPos = isoUrl.indexOf("template/tmpl");
        if (templateRootPos < 0) {
            throw new Exception("Invalid ISO path info");
        }

        String storeUrl = isoUrl.substring(0, templateRootPos - 1);
        String isoPath = isoUrl.substring(templateRootPos, isoFileNameStartPos);

        ManagedObjectReference morDs = prepareSecondaryDatastoreOnHost(storeUrl);
        DatastoreMO dsMo = new DatastoreMO(getServiceContext(), morDs);

        return new Pair<String, ManagedObjectReference>(String.format("[%s] %s%s", dsMo.getName(), isoPath, isoFileName), morDs);
    }

    protected Answer execute(ReadyCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource ReadyCommand: " + _gson.toJson(cmd));
        }
        
        try {
        	VmwareContext context = getServiceContext();
        	VmwareHypervisorHost hyperHost = getHyperHost(context);
        	if(hyperHost.isHyperHostConnected()) {
                return new ReadyAnswer(cmd);
        	} else {
        		return new ReadyAnswer(cmd, "Host is not in connect state");
        	}
        } catch(Exception e) {
        	s_logger.error("Unexpected exception: ", e);
    		return new ReadyAnswer(cmd, VmwareHelper.getExceptionMessage(e));
        }
    }

    protected Answer execute(GetHostStatsCommand cmd) {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Executing resource GetHostStatsCommand: " + _gson.toJson(cmd));
        }

        VmwareContext context = getServiceContext();
        VmwareHypervisorHost hyperHost = getHyperHost(context);
        
        HostStatsEntry hostStats = new HostStatsEntry(cmd.getHostId(), 0, 0, 0, "host", 0, 0, 0, 0);
        Answer answer = new GetHostStatsAnswer(cmd, hostStats);
        try {
            HostStatsEntry entry = getHyperHostStats(hyperHost);
            if(entry != null) {
	            entry.setHostId(cmd.getHostId());
	            answer = new GetHostStatsAnswer(cmd, entry);
            }
        } catch (Exception e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "Unable to execute GetHostStatsCommand due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
        }

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("GetHostStats Answer: " + _gson.toJson(answer));
        }

        return answer;
    }

    protected Answer execute(GetVmStatsCommand cmd) {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Executing resource GetVmStatsCommand: " + _gson.toJson(cmd));
        }

        HashMap<String, VmStatsEntry> vmStatsMap = null;

        try {
            HashMap<String, State> newStates = getVmStates();

            List<String> requestedVmNames = cmd.getVmNames();
            List<String> vmNames = new ArrayList();

            if (requestedVmNames != null) {
                for (String vmName : requestedVmNames) {
                     if (newStates.get(vmName) != null) {
                         vmNames.add(vmName);
        	         }
                }
            }

            if (vmNames != null) {
                vmStatsMap = getVmStats(vmNames);
            }
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            s_logger.error("Unable to execute GetVmStatsCommand due to : " + VmwareHelper.getExceptionMessage(e), e);
        }

        Answer answer = new GetVmStatsAnswer(cmd, vmStatsMap);

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Report GetVmStatsAnswer: " + _gson.toJson(answer));
        }
        return answer;
    }

    protected Answer execute(CheckHealthCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource CheckHealthCommand: " + _gson.toJson(cmd));
        }

        try {
            VmwareHypervisorHost hyperHost = getHyperHost(getServiceContext());
            if (hyperHost.isHyperHostConnected()) {
                return new CheckHealthAnswer(cmd, true);
            }
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            s_logger.error("Unable to execute CheckHealthCommand due to " + VmwareHelper.getExceptionMessage(e), e);
        }
        return new CheckHealthAnswer(cmd, false);
    }

    protected Answer execute(StopCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource StopCommand: " + _gson.toJson(cmd));
        }

        VmwareContext context = getServiceContext();
        VmwareHypervisorHost hyperHost = getHyperHost(context);
        try {
            VirtualMachineMO vmMo = hyperHost.findVmOnHyperHost(cmd.getVmName());
            if (vmMo != null) {

                State state = null;
                synchronized (_vms) {
                    state = _vms.get(cmd.getVmName());
                    _vms.put(cmd.getVmName(), State.Stopping);
                }

                try {
                    vmMo.setCustomFieldValue(CustomFieldConstants.CLOUD_NIC_MASK, "0");
                	
                    if (getVmState(vmMo) != State.Stopped) {
                        Long bytesSent = 0L;
                        Long bytesRcvd = 0L;

                        if (VirtualMachineName.isValidRouterName(cmd.getVmName())) {
                            if (cmd.getPrivateRouterIpAddress() != null) {
                                long[] stats = getNetworkStats(cmd.getPrivateRouterIpAddress());
                                bytesSent = stats[0];
                                bytesRcvd = stats[1];
                            }
                        }
                        
                        // before we stop VM, remove all possible snapshots on the VM to let
                        // disk chain be collapsed
                        s_logger.info("Remove all snapshot before stopping VM " + cmd.getVmName());
                        vmMo.removeAllSnapshots();
                        if (vmMo.safePowerOff(_shutdown_waitMs)) {
                            state = State.Stopped;
                            return new StopAnswer(cmd, "Stop VM " + cmd.getVmName() + " Succeed", 0, bytesSent, bytesRcvd);
                        } else {
                        	String msg = "Have problem in powering off VM " + cmd.getVmName() + ", let the process continue";
                        	s_logger.warn(msg);
                            return new StopAnswer(cmd, msg, 0, 0L, 0L);
                        }
                    } else {
                        state = State.Stopped;
                    }

                    String msg = "VM " + cmd.getVmName() + " is already in stopped state";
                    s_logger.info(msg);
                    return new StopAnswer(cmd, msg, 0, 0L, 0L);
                } finally {
                    synchronized (_vms) {
                        _vms.put(cmd.getVmName(), state);
                    }
                }
            } else {
                synchronized (_vms) {
                    _vms.remove(cmd.getVmName());
                }

                String msg = "VM " + cmd.getVmName() + " is no longer in vSphere";
                s_logger.info(msg);
                return new StopAnswer(cmd, msg, 0, 0L, 0L);
            }
        } catch (Exception e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "StopCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg);
            return new StopAnswer(cmd, msg);
        }
    }

    protected Answer execute(RebootRouterCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource RebootRouterCommand: " + _gson.toJson(cmd));
        }

        Long bytesSent = 0L;
        Long bytesRcvd = 0L;
        if (VirtualMachineName.isValidRouterName(cmd.getVmName())) {
            long[] stats = getNetworkStats(cmd.getPrivateIpAddress());
            bytesSent = stats[0];
            bytesRcvd = stats[1];
        }

        RebootAnswer answer = (RebootAnswer) execute((RebootCommand) cmd);
        answer.setBytesSent(bytesSent);
        answer.setBytesReceived(bytesRcvd);

        if (answer.getResult()) {
            String connectResult = connect(cmd.getVmName(), cmd.getPrivateIpAddress());
            networkUsage(cmd.getPrivateIpAddress(), "create", null);
            if (connectResult == null) {
                return answer;
            } else {
                return new Answer(cmd, false, connectResult);
            }
        }
        return answer;
    }

    protected Answer execute(RebootCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource RebootCommand: " + _gson.toJson(cmd));
        }

        VmwareContext context = getServiceContext();
        VmwareHypervisorHost hyperHost = getHyperHost(context);
        try {
            VirtualMachineMO vmMo = hyperHost.findVmOnHyperHost(cmd.getVmName());
            if (vmMo != null) {
                try {
                    vmMo.rebootGuest();
                    return new RebootAnswer(cmd, "reboot succeeded", null, null);
                } catch(ToolsUnavailable e) {
                    s_logger.warn("VMware tools is not installed at guest OS, we will perform hard reset for reboot");
                } catch(Exception e) {
                    s_logger.warn("We are not able to perform gracefull guest reboot due to " + VmwareHelper.getExceptionMessage(e));
                }

                // continue to try with hard-reset
                if (vmMo.reset()) {
                    return new RebootAnswer(cmd, "reboot succeeded", null, null);
                }

                String msg = "Reboot failed in vSphere. vm: " + cmd.getVmName();
                s_logger.warn(msg);
                return new RebootAnswer(cmd, msg);
            } else {
                String msg = "Unable to find the VM in vSphere to reboot. vm: " + cmd.getVmName();
                s_logger.warn(msg);
                return new RebootAnswer(cmd, msg);
            }
        } catch (Exception e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "RebootCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg);
            return new RebootAnswer(cmd, msg);
        }
    }

    protected Answer execute(CheckVirtualMachineCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource CheckVirtualMachineCommand: " + _gson.toJson(cmd));
        }

        final String vmName = cmd.getVmName();
        State state = State.Unknown;
        Integer vncPort = null;

        VmwareContext context = getServiceContext();
        VmwareHypervisorHost hyperHost = getHyperHost(context);

        try {
            VirtualMachineMO vmMo = hyperHost.findVmOnHyperHost(vmName);
            if (vmMo != null) {
                state = getVmState(vmMo);
                if (state == State.Running) {
                    synchronized (_vms) {
                        _vms.put(vmName, State.Running);
                    }
                }
                return new CheckVirtualMachineAnswer(cmd, state, vncPort);
            } else {
                s_logger.warn("Can not find vm " + vmName + " to execute CheckVirtualMachineCommand");
                return new CheckVirtualMachineAnswer(cmd, state, vncPort);
            }

        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }
            s_logger.error("Unexpected exception: " + VmwareHelper.getExceptionMessage(e), e);

            return new CheckVirtualMachineAnswer(cmd, state, vncPort);
        }
    }

    protected Answer execute(PrepareForMigrationCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource PrepareForMigrationCommand: " + _gson.toJson(cmd));
        }

        VirtualMachineTO vm = cmd.getVirtualMachine();
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Preparing host for migrating " + vm);
        }

        final String vmName = vm.getName();
        try {
            VmwareHypervisorHost hyperHost = getHyperHost(getServiceContext());
            VmwareManager mgr = hyperHost.getContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);

            // find VM through datacenter (VM is not at the target host yet)
            VirtualMachineMO vmMo = hyperHost.findVmOnPeerHyperHost(vmName);
            if (vmMo == null) {
                String msg = "VM " + vmName + " does not exist in VMware datacenter";
                s_logger.error(msg);
                throw new Exception(msg);
            }

            NicTO[] nics = vm.getNics();
            for (NicTO nic : nics) {
                // prepare network on the host
                prepareNetworkFromNicInfo(new HostMO(getServiceContext(), _morHyperHost), nic);
            }

            String secStoreUrl = mgr.getSecondaryStorageStoreUrl(Long.parseLong(_dcId));
            if(secStoreUrl == null) {
                String msg = "secondary storage for dc " + _dcId + " is not ready yet?";
                throw new Exception(msg);
            }
            mgr.prepareSecondaryStorageStore(secStoreUrl);
            
            ManagedObjectReference morSecDs = prepareSecondaryDatastoreOnHost(secStoreUrl);
            if (morSecDs == null) {
                String msg = "Failed to prepare secondary storage on host, secondary store url: " + secStoreUrl;
                throw new Exception(msg);
            }

            synchronized (_vms) {
                _vms.put(vm.getName(), State.Migrating);
            }
            return new PrepareForMigrationAnswer(cmd);
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "Unexcpeted exception " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new PrepareForMigrationAnswer(cmd, msg);
        }
    }

    protected Answer execute(MigrateCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource MigrateCommand: " + _gson.toJson(cmd));
        }

        final String vmName = cmd.getVmName();

        State state = null;
        synchronized (_vms) {
            state = _vms.get(vmName);
            _vms.put(vmName, State.Stopping);
        }

        try {
            VmwareHypervisorHost hyperHost = getHyperHost(getServiceContext());
            ManagedObjectReference morDc = hyperHost.getHyperHostDatacenter();

            // find VM through datacenter (VM is not at the target host yet)
            VirtualMachineMO vmMo = hyperHost.findVmOnPeerHyperHost(vmName);
            if (vmMo == null) {
                String msg = "VM " + vmName + " does not exist in VMware datacenter";
                s_logger.error(msg);
                throw new Exception(msg);
            }

            VmwareHypervisorHost destHyperHost = getTargetHyperHost(new DatacenterMO(hyperHost.getContext(), morDc), cmd.getDestinationIp());

            ManagedObjectReference morTargetPhysicalHost = destHyperHost.findMigrationTarget(vmMo);
            if (morTargetPhysicalHost == null) {
                throw new Exception("Unable to find a target capable physical host");
            }

            if (!vmMo.migrate(destHyperHost.getHyperHostOwnerResourcePool(), morTargetPhysicalHost)) {
                throw new Exception("Migration failed");
            }

            state = State.Stopping;
            return new MigrateAnswer(cmd, true, "migration succeeded", null);
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "MigrationCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.warn(msg, e);
            return new MigrateAnswer(cmd, false, msg, null);
        } finally {
            synchronized (_vms) {
                _vms.put(vmName, state);
            }
        }
    }

    private VmwareHypervisorHost getTargetHyperHost(DatacenterMO dcMo, String destIp) throws Exception {
    	
        VmwareManager mgr = dcMo.getContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
    	
        ObjectContent[] ocs = dcMo.getHostPropertiesOnDatacenterHostFolder(new String[] { "name", "parent" });
        if (ocs != null && ocs.length > 0) {
            for (ObjectContent oc : ocs) {
                HostMO hostMo = new HostMO(dcMo.getContext(), oc.getObj());
                VmwareHypervisorHostNetworkSummary netSummary = hostMo.getHyperHostNetworkSummary(mgr.getManagementPortGroupByHost(hostMo));
                if (destIp.equalsIgnoreCase(netSummary.getHostIp())) {
                    return new HostMO(dcMo.getContext(), oc.getObj());
                }
            }
        }

        throw new Exception("Unable to locate dest host by " + destIp);
    }

    protected Answer execute(CreateStoragePoolCommand cmd) {
        return new Answer(cmd, true, "success");
    }

    protected Answer execute(ModifyStoragePoolCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource ModifyStoragePoolCommand: " + _gson.toJson(cmd));
        }

        try {
            VmwareHypervisorHost hyperHost = getHyperHost(getServiceContext());
            StorageFilerTO pool = cmd.getPool();

            if (pool.getType() != StoragePoolType.NetworkFilesystem && pool.getType() != StoragePoolType.VMFS) {
                throw new Exception("Unsupported storage pool type " + pool.getType());
            }

            ManagedObjectReference morDatastore = hyperHost.mountDatastore(pool.getType() == StoragePoolType.VMFS, pool.getHost(), pool.getPort(), pool.getPath(), pool.getUuid());

            assert (morDatastore != null);
            DatastoreSummary summary = new DatastoreMO(getServiceContext(), morDatastore).getSummary();
            long capacity = summary.getCapacity();
            long available = summary.getFreeSpace();
            Map<String, TemplateInfo> tInfo = new HashMap<String, TemplateInfo>();
            ModifyStoragePoolAnswer answer = new ModifyStoragePoolAnswer(cmd, capacity, available, tInfo);
            return answer;
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "ModifyStoragePoolCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new Answer(cmd, false, msg);
        }
    }

    protected Answer execute(DeleteStoragePoolCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource DeleteStoragePoolCommand: " + _gson.toJson(cmd));
        }

        StorageFilerTO pool = cmd.getPool();
        try {
        	// We will leave datastore cleanup management to vCenter. Since for cluster VMFS datastore, it will always
        	// be mounted by vCenter.
        	
            // VmwareHypervisorHost hyperHost = this.getHyperHost(getServiceContext());
            // hyperHost.unmountDatastore(pool.getUuid());
            Answer answer = new Answer(cmd, true, "success");
            return answer;
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "DeleteStoragePoolCommand (pool: " + pool.getHost() + ", path: " + pool.getPath() + ") failed due to " + VmwareHelper.getExceptionMessage(e);
            return new Answer(cmd, false, msg);
        }
    }

    protected Answer execute(AttachVolumeCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource AttachVolumeCommand: " + _gson.toJson(cmd));
        }

        /*
         * AttachVolumeCommand { "attach":true,"vmName":"i-2-1-KY","pooltype":"NetworkFilesystem",
         * "volumeFolder":"/export/home/kelven/vmware-test/primary", "volumePath":"uuid",
         * "volumeName":"volume name","deviceId":1 }
         */
        try {
            VmwareHypervisorHost hyperHost = getHyperHost(getServiceContext());
            VirtualMachineMO vmMo = hyperHost.findVmOnHyperHost(cmd.getVmName());
            if (vmMo == null) {
                String msg = "Unable to find the VM to execute AttachVolumeCommand, vmName: " + cmd.getVmName();
                s_logger.error(msg);
                throw new Exception(msg);
            }

            ManagedObjectReference morDs = hyperHost.findDatastore(cmd.getPoolUuid());
            if (morDs == null) {
                String msg = "Unable to find the mounted datastore to execute AttachVolumeCommand, vmName: " + cmd.getVmName();
                s_logger.error(msg);
                throw new Exception(msg);
            }

            DatastoreMO dsMo = new DatastoreMO(getServiceContext(), morDs);
            String datastoreVolumePath = String.format("[%s] %s.vmdk", dsMo.getName(), cmd.getVolumePath());

            AttachVolumeAnswer answer = new AttachVolumeAnswer(cmd, cmd.getDeviceId());
            if (cmd.getAttach()) {
                vmMo.attachDisk(new String[] { datastoreVolumePath }, morDs);
            } else {
            	vmMo.removeAllSnapshots();
                vmMo.detachDisk(datastoreVolumePath, false);
            }

            return answer;
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "AttachVolumeCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new AttachVolumeAnswer(cmd, msg);
        }
    }

    protected Answer execute(AttachIsoCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource AttachIsoCommand: " + _gson.toJson(cmd));
        }

        try {
            VmwareHypervisorHost hyperHost = getHyperHost(getServiceContext());
            VirtualMachineMO vmMo = hyperHost.findVmOnHyperHost(cmd.getVmName());
            if (vmMo == null) {
                String msg = "Unable to find VM in vSphere to execute AttachIsoCommand, vmName: " + cmd.getVmName();
                s_logger.error(msg);
                throw new Exception(msg);
            }

            String storeUrl = cmd.getStoreUrl();
            if (storeUrl == null) {
                if (!cmd.getIsoPath().equalsIgnoreCase("vmware-tools.iso")) {
                    String msg = "ISO store root url is not found in AttachIsoCommand";
                    s_logger.error(msg);
                    throw new Exception(msg);
                } else {
                    if (cmd.isAttach()) {
                        vmMo.mountToolsInstaller();
                    } else {
                        vmMo.unmountToolsInstaller();
                    }

                    return new Answer(cmd);
                }
            }

            ManagedObjectReference morSecondaryDs = prepareSecondaryDatastoreOnHost(storeUrl);
            String isoPath = cmd.getIsoPath();
            if (!isoPath.startsWith(storeUrl)) {
                assert (false);
                String msg = "ISO path does not start with the secondary storage root";
                s_logger.error(msg);
                throw new Exception(msg);
            }

            int isoNameStartPos = isoPath.lastIndexOf('/');
            String isoFileName = isoPath.substring(isoNameStartPos + 1);
            String isoStorePathFromRoot = isoPath.substring(storeUrl.length(), isoNameStartPos);

            // TODO, check if iso is already attached, or if there is a previous
            // attachment
            String isoDatastorePath = String.format("[%s] %s%s", getSecondaryDatastoreUUID(storeUrl), isoStorePathFromRoot, isoFileName);

            if (cmd.isAttach()) {
                vmMo.attachIso(isoDatastorePath, morSecondaryDs, true, false);
            } else {
                vmMo.detachIso(isoDatastorePath);
            }

            return new Answer(cmd);
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            if(cmd.isAttach()) {
	            String msg = "AttachIsoCommand(attach) failed due to " + VmwareHelper.getExceptionMessage(e);
	            s_logger.error(msg, e);
	            return new Answer(cmd, false, msg);
            } else {
	            String msg = "AttachIsoCommand(detach) failed due to " + VmwareHelper.getExceptionMessage(e);
	            s_logger.warn(msg, e);
	            return new Answer(cmd, false, msg);
            }
        }
    }

    private synchronized ManagedObjectReference prepareSecondaryDatastoreOnHost(String storeUrl) throws Exception {
        String storeName = getSecondaryDatastoreUUID(storeUrl);
        URI uri = new URI(storeUrl);

        VmwareHypervisorHost hyperHost = getHyperHost(getServiceContext());
        ManagedObjectReference morDatastore = hyperHost.mountDatastore(false, uri.getHost(), 0, uri.getPath(), storeName);

        if (morDatastore == null) {
            throw new Exception("Unable to mount secondary storage on host. storeUrl: " + storeUrl);
        }

        return morDatastore;
    }

    private static String getSecondaryDatastoreUUID(String storeUrl) {
        return UUID.nameUUIDFromBytes(storeUrl.getBytes()).toString();
    }

    protected Answer execute(ValidateSnapshotCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource ValidateSnapshotCommand: " + _gson.toJson(cmd));
        }

        // the command is no longer available
        String expectedSnapshotBackupUuid = null;
        String actualSnapshotBackupUuid = null;
        String actualSnapshotUuid = null;
        return new ValidateSnapshotAnswer(cmd, false, "ValidateSnapshotCommand is not supported for vmware yet", expectedSnapshotBackupUuid, actualSnapshotBackupUuid, actualSnapshotUuid);
    }

    protected Answer execute(ManageSnapshotCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource ManageSnapshotCommand: " + _gson.toJson(cmd));
        }

        long snapshotId = cmd.getSnapshotId();

        /*
         * "ManageSnapshotCommand",
         * "{\"_commandSwitch\":\"-c\",\"_volumePath\":\"i-2-3-KY-ROOT\",\"_snapshotName\":\"i-2-3-KY_i-2-3-KY-ROOT_20101102203827\",\"_snapshotId\":1,\"_vmName\":\"i-2-3-KY\"}"
         */
        boolean success = false;
        String cmdSwitch = cmd.getCommandSwitch();
        String snapshotOp = "Unsupported snapshot command." + cmdSwitch;
        if (cmdSwitch.equals(ManageSnapshotCommand.CREATE_SNAPSHOT)) {
            snapshotOp = "create";
        } else if (cmdSwitch.equals(ManageSnapshotCommand.DESTROY_SNAPSHOT)) {
            snapshotOp = "destroy";
        }

        String details = "ManageSnapshotCommand operation: " + snapshotOp + " Failed for snapshotId: " + snapshotId;
        String snapshotUUID = null;

        // snapshot operation (create or destroy) is handled inside BackupSnapshotCommand(), we just fake
        // a success return here
        snapshotUUID = UUID.randomUUID().toString();
        success = true;
        details = null;

        return new ManageSnapshotAnswer(cmd, snapshotId, snapshotUUID, success, details);
    }

    protected Answer execute(BackupSnapshotCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource BackupSnapshotCommand: " + _gson.toJson(cmd));
        }

        try {
            VmwareContext context = getServiceContext();
            VmwareManager mgr = context.getStockObject(VmwareManager.CONTEXT_STOCK_NAME);

            return mgr.getStorageManager().execute(this, cmd);
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String details = "BackupSnapshotCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(details, e);
            return new BackupSnapshotAnswer(cmd, false, details, null, true);
        }
    }


    protected Answer execute(CreateVolumeFromSnapshotCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource CreateVolumeFromSnapshotCommand: " + _gson.toJson(cmd));
        }

        String details = null;
        boolean success = false;
        String newVolumeName = UUID.randomUUID().toString();

        try {
            VmwareContext context = getServiceContext();
            VmwareManager mgr = context.getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
            return mgr.getStorageManager().execute(this, cmd);
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            details = "CreateVolumeFromSnapshotCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(details, e);
        }

        return new CreateVolumeFromSnapshotAnswer(cmd, success, details, newVolumeName);
    }

    protected Answer execute(CreatePrivateTemplateFromVolumeCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource CreatePrivateTemplateFromVolumeCommand: " + _gson.toJson(cmd));
        }

        try {
            VmwareContext context = getServiceContext();
            VmwareManager mgr = context.getStockObject(VmwareManager.CONTEXT_STOCK_NAME);

            return mgr.getStorageManager().execute(this, cmd);

        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String details = "CreatePrivateTemplateFromVolumeCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(details, e);
            return new CreatePrivateTemplateAnswer(cmd, false, details);
        }
    }

    protected Answer execute(final UpgradeSnapshotCommand cmd) {
        return new Answer(cmd, true, "success");
    }

    protected Answer execute(CreatePrivateTemplateFromSnapshotCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource CreatePrivateTemplateFromSnapshotCommand: " + _gson.toJson(cmd));
        }

        try {
            VmwareManager mgr = getServiceContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
            return mgr.getStorageManager().execute(this, cmd);

        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String details = "CreatePrivateTemplateFromSnapshotCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(details, e);
            return new CreatePrivateTemplateAnswer(cmd, false, details);
        }
    }

    protected Answer execute(GetStorageStatsCommand cmd) {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Executing resource GetStorageStatsCommand: " + _gson.toJson(cmd));
        }

        try {
            VmwareContext context = getServiceContext();
            VmwareHypervisorHost hyperHost = getHyperHost(context);
            ManagedObjectReference morDs = hyperHost.findDatastore(cmd.getStorageId());

            if (morDs != null) {
                DatastoreMO datastoreMo = new DatastoreMO(context, morDs);
                DatastoreSummary summary = datastoreMo.getSummary();
                assert (summary != null);

                long capacity = summary.getCapacity();
                long free = summary.getFreeSpace();
                long used = capacity - free;

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Datastore summary info, storageId: " + cmd.getStorageId() + ", localPath: " + cmd.getLocalPath() + ", poolType: " + cmd.getPooltype() + ", capacity: " + capacity
                            + ", free: " + free + ", used: " + used);
                }

                if (summary.getCapacity() <= 0) {
                    s_logger.warn("Something is wrong with vSphere NFS datastore, rebooting ESX(ESXi) host should help");
                }

                return new GetStorageStatsAnswer(cmd, capacity, used);
            } else {
                String msg = "Could not find datastore for GetStorageStatsCommand storageId : " + cmd.getStorageId() + ", localPath: " + cmd.getLocalPath() + ", poolType: " + cmd.getPooltype();

                s_logger.error(msg);
                return new GetStorageStatsAnswer(cmd, msg);
            }
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "Unable to execute GetStorageStatsCommand(storageId : " + cmd.getStorageId() + ", localPath: " + cmd.getLocalPath() + ", poolType: " + cmd.getPooltype() + ") due to "
            + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new GetStorageStatsAnswer(cmd, msg);
        }
    }

    protected Answer execute(GetVncPortCommand cmd) {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Executing resource GetVncPortCommand: " + _gson.toJson(cmd));
        }

        try {
            VmwareContext context = getServiceContext();
            VmwareHypervisorHost hyperHost = getHyperHost(context);
            assert(hyperHost instanceof HostMO);
            VmwareManager mgr = context.getStockObject(VmwareManager.CONTEXT_STOCK_NAME);

            VirtualMachineMO vmMo = hyperHost.findVmOnHyperHost(cmd.getName());
            if (vmMo == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Unable to find the owner VM for GetVncPortCommand on host " + hyperHost.getHyperHostName() + ", try within datacenter");
                }

                vmMo = hyperHost.findVmOnPeerHyperHost(cmd.getName());

                if (vmMo == null) {
                    throw new Exception("Unable to find VM in vSphere, vm: " + cmd.getName());
                }
            }
            
            Pair<String, Integer> portInfo = vmMo.getVncPort(mgr.getManagementPortGroupByHost((HostMO)hyperHost));

            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Found vnc port info. vm: " + cmd.getName() + " host: " + portInfo.first() + ", vnc port: " + portInfo.second());
            }
            return new GetVncPortAnswer(cmd, portInfo.first(), portInfo.second());
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "GetVncPortCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new GetVncPortAnswer(cmd, msg);
        }
    }

    protected Answer execute(SetupCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource SetupCommand: " + _gson.toJson(cmd));
        }

        return new SetupAnswer(cmd, false);
    }

    protected Answer execute(MaintainCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource MaintainCommand: " + _gson.toJson(cmd));
        }

        return new MaintainAnswer(cmd, "Put host in maintaince");
    }

    protected Answer execute(PingTestCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource PingTestCommand: " + _gson.toJson(cmd));
        }

        return new Answer(cmd);
    }

    protected Answer execute(CheckOnHostCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource CheckOnHostCommand: " + _gson.toJson(cmd));
        }

        return new CheckOnHostAnswer(cmd, null, "Not Implmeneted");
    }

    protected Answer execute(ModifySshKeysCommand cmd) {
        //do not log the command contents for this command. do NOT log the ssh keys
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource ModifySshKeysCommand.");
        }

        return new Answer(cmd);
    }

    protected Answer execute(PoolEjectCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource PoolEjectCommand: " + _gson.toJson(cmd));
        }

        return new Answer(cmd, false, "PoolEjectCommand is not available for vmware");
    }

    @Override
    public PrimaryStorageDownloadAnswer execute(PrimaryStorageDownloadCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource PrimaryStorageDownloadCommand: " + _gson.toJson(cmd));
        }

        try {
            VmwareContext context = getServiceContext();
            VmwareManager mgr = context.getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
            return (PrimaryStorageDownloadAnswer) mgr.getStorageManager().execute(this, cmd);
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "PrimaryStorageDownloadCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new PrimaryStorageDownloadAnswer(msg);
        }
    }

    @Override
    public Answer execute(DestroyCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource DestroyCommand: " + _gson.toJson(cmd));
        }

        /*
         * DestroyCommand content example
         * 
         * {"volume": {"id":5,"name":"Volume1", "mountPoint":"/export/home/kelven/vmware-test/primary",
         * "path":"6bb8762f-c34c-453c-8e03-26cc246ceec4", "size":0,"type":"DATADISK","resourceType":
         * "STORAGE_POOL","storagePoolType":"NetworkFilesystem", "poolId":0,"deviceId":0 } }
         * 
         * {"volume": {"id":1, "name":"i-2-1-KY-ROOT", "mountPoint":"/export/home/kelven/vmware-test/primary",
         * "path":"i-2-1-KY-ROOT","size":0,"type":"ROOT", "resourceType":"STORAGE_POOL", "storagePoolType":"NetworkFilesystem",
         * "poolId":0,"deviceId":0 } }
         */

        try {
            VmwareContext context = getServiceContext();
            VmwareHypervisorHost hyperHost = getHyperHost(context);
            VolumeTO vol = cmd.getVolume();

            ManagedObjectReference morDs = hyperHost.findDatastore(vol.getPoolUuid());
            if (morDs == null) {
                String msg = "Unable to find datastore based on volume mount point " + cmd.getVolume().getMountPoint();
                s_logger.error(msg);
                throw new Exception(msg);
            }

            DatastoreMO dsMo = new DatastoreMO(context, morDs);

            ManagedObjectReference morDc = hyperHost.getHyperHostDatacenter();
            ManagedObjectReference morCluster = hyperHost.getHyperHostCluster();
            ClusterMO clusterMo = new ClusterMO(context, morCluster);
            
            if (cmd.getVolume().getType() == Volume.Type.ROOT) {
                String vmName = cmd.getVmName();
                if (vmName != null) {
                    VirtualMachineMO vmMo = clusterMo.findVmOnHyperHost(vmName);
                    if (vmMo != null) {
                        if (s_logger.isInfoEnabled()) {
                            s_logger.info("Destroy root volume and VM itself. vmName " + vmName);
                        }

                        HostMO hostMo = vmMo.getRunningHost();
                        List<NetworkDetails> networks = vmMo.getNetworksWithDetails();
                        
                        // tear down all devices first before we destroy the VM to avoid accidently delete disk backing files
                        if (getVmState(vmMo) != State.Stopped)
                        	vmMo.safePowerOff(_shutdown_waitMs);
                        vmMo.tearDownDevices(new Class<?>[] { VirtualDisk.class, VirtualEthernetCard.class });
                        vmMo.destroy();
                        
                        for (NetworkDetails netDetails : networks) {
                            if (netDetails.getGCTag() != null && netDetails.getGCTag().equalsIgnoreCase("true")) {
                                if (netDetails.getVMMorsOnNetwork() == null || netDetails.getVMMorsOnNetwork().length == 1) {
                                    cleanupNetwork(hostMo, netDetails);
                                }
                            }
                        }
                    } 
                    
                    if (s_logger.isInfoEnabled())
                        s_logger.info("Destroy volume by original name: " + cmd.getVolume().getPath() + ".vmdk");
                    dsMo.deleteFile(cmd.getVolume().getPath() + ".vmdk", morDc, true);
                    
                    // root volume may be created via linked-clone, delete the delta disk as well
                    if (s_logger.isInfoEnabled())
                    	s_logger.info("Destroy volume by derived name: " + cmd.getVolume().getPath() + "-delta.vmdk");
                    dsMo.deleteFile(cmd.getVolume().getPath() + "-delta.vmdk", morDc, true);
                    return new Answer(cmd, true, "Success");
                }

                if (s_logger.isInfoEnabled()) {
                    s_logger.info("Destroy root volume directly from datastore");
                }
            } else {
            	// evitTemplate will be converted into DestroyCommand, test if we are running in this case
                VirtualMachineMO vmMo = clusterMo.findVmOnHyperHost(cmd.getVolume().getPath());
                if (vmMo != null) {
                    if (s_logger.isInfoEnabled())
                        s_logger.info("Destroy template volume " + cmd.getVolume().getPath());
                	
                	vmMo.destroy();
                    return new Answer(cmd, true, "Success");
                }
            }

            String chainInfo = cmd.getVolume().getChainInfo();
            if (chainInfo != null && !chainInfo.isEmpty()) {
                s_logger.info("Destroy volume by chain info: " + chainInfo);
                String[] diskChain = _gson.fromJson(chainInfo, String[].class);

                if (diskChain != null && diskChain.length > 0) {
                    for (String backingName : diskChain) {
                        if (s_logger.isInfoEnabled()) {
                            s_logger.info("Delete volume backing file: " + backingName);
                        }
                        dsMo.deleteFile(backingName, morDc, true);
                    }
                } else {
                    if (s_logger.isInfoEnabled()) {
                        s_logger.info("Empty disk chain info, fall back to try to delete by original backing file name");
                    }
                    dsMo.deleteFile(cmd.getVolume().getPath() + ".vmdk", morDc, true);
                    
                    if (s_logger.isInfoEnabled()) {
                    	s_logger.info("Destroy volume by derived name: " + cmd.getVolume().getPath() + "-flat.vmdk");
                    }
                    dsMo.deleteFile(cmd.getVolume().getPath() + "-flat.vmdk", morDc, true);
                }
            } else {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("Destroy volume by original name: " + cmd.getVolume().getPath() + ".vmdk");
                }
                dsMo.deleteFile(cmd.getVolume().getPath() + ".vmdk", morDc, true);
                
                if (s_logger.isInfoEnabled()) {
                	s_logger.info("Destroy volume by derived name: " + cmd.getVolume().getPath() + "-flat.vmdk");
                }
                dsMo.deleteFile(cmd.getVolume().getPath() + "-flat.vmdk", morDc, true);
            }

            return new Answer(cmd, true, "Success");
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "DestroyCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new Answer(cmd, false, msg);
        }
    }

    private void cleanupNetwork(HostMO hostMo, NetworkDetails netDetails) {
        // we will no longer cleanup VLAN networks in order to support native VMware HA
        /*
         * assert(netDetails.getName() != null); try { synchronized(this) { NetworkMO networkMo = new
         * NetworkMO(hostMo.getContext(), netDetails.getNetworkMor()); ManagedObjectReference[] vms =
         * networkMo.getVMsOnNetwork(); if(vms == null || vms.length == 0) { if(s_logger.isInfoEnabled()) {
         * s_logger.info("Cleanup network as it is currently not in use: " + netDetails.getName()); }
         * 
         * hostMo.deletePortGroup(netDetails.getName()); } } } catch(Throwable e) {
         * s_logger.warn("Unable to cleanup network due to exception, skip for next time"); }
         */
    }

    @Override
    public CopyVolumeAnswer execute(CopyVolumeCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource CopyVolumeCommand: " + _gson.toJson(cmd));
        }

        try {
            VmwareContext context = getServiceContext();
            VmwareManager mgr = context.getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
            return (CopyVolumeAnswer) mgr.getStorageManager().execute(this, cmd);
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "CopyVolumeCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new CopyVolumeAnswer(cmd, false, msg, null, null);
        }
    }

    @Override
    public synchronized CreateAnswer execute(CreateCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource CreateCommand: " + _gson.toJson(cmd));
        }

        StorageFilerTO pool = cmd.getPool();
        DiskProfile dskch = cmd.getDiskCharacteristics();

        try {
            VmwareContext context = getServiceContext();
            VmwareHypervisorHost hyperHost = getHyperHost(context);
            DatacenterMO dcMo = new DatacenterMO(context, hyperHost.getHyperHostDatacenter());

            VmwareManager vmwareMgr = context.getStockObject(VmwareManager.CONTEXT_STOCK_NAME);

            ManagedObjectReference morDatastore = hyperHost.findDatastore(pool.getUuid());
            if (morDatastore == null) {
                throw new Exception("Unable to find datastore in vSphere");
            }
            DatastoreMO dsMo = new DatastoreMO(context, morDatastore);

            if (cmd.getDiskCharacteristics().getType() == Volume.Type.ROOT) {
                if (cmd.getTemplateUrl() == null) {
                    // create a root volume for blank VM
                    String dummyVmName = getWorkerName(context, cmd, 0);
                    VirtualMachineMO vmMo = null;

                    try {
                        vmMo = prepareVolumeHostDummyVm(hyperHost, dsMo, dummyVmName);
                        if (vmMo == null) {
                            throw new Exception("Unable to create a dummy VM for volume creation");
                        }

                        String volumeDatastorePath = String.format("[%s] %s.vmdk", dsMo.getName(), cmd.getDiskCharacteristics().getName());
                        synchronized (this) {
                            s_logger.info("Delete file if exists in datastore to clear the way for creating the volume. file: " + volumeDatastorePath);
                            VmwareHelper.deleteVolumeVmdkFiles(dsMo, cmd.getDiskCharacteristics().getName(), dcMo);
                            vmMo.createDisk(volumeDatastorePath, (int) (cmd.getDiskCharacteristics().getSize() / (1024L * 1024L)), morDatastore, -1);
                            vmMo.detachDisk(volumeDatastorePath, false);
                        }

                        VolumeTO vol = new VolumeTO(cmd.getVolumeId(), dskch.getType(), pool.getType(), pool.getUuid(), cmd.getDiskCharacteristics().getName(), pool.getPath(), cmd
                                .getDiskCharacteristics().getName(), cmd.getDiskCharacteristics().getSize(), null);
                        return new CreateAnswer(cmd, vol);
                    } finally {
                        vmMo.detachAllDisks();

                        s_logger.info("Destroy dummy VM after volume creation");
                        vmMo.destroy();
                    }
                } else {
                    VirtualMachineMO vmTemplate = VmwareHelper.pickOneVmOnRunningHost(dcMo.findVmByNameAndLabel(cmd.getTemplateUrl()), true);
                    if (vmTemplate == null) {
                        s_logger.warn("Template host in vSphere is not in connected state, request template reload");
                        return new CreateAnswer(cmd, "Template host in vSphere is not in connected state, request template reload", true);
                    }
                    
                    ManagedObjectReference morPool = hyperHost.getHyperHostOwnerResourcePool();
                    ManagedObjectReference morCluster = hyperHost.getHyperHostCluster();
                    ManagedObjectReference morBaseSnapshot = vmTemplate.getSnapshotMor("cloud.template.base");
                    if (morBaseSnapshot == null) {
                        String msg = "Unable to find template base snapshot, invalid template";
                        s_logger.error(msg);
                        throw new Exception(msg);
                    }

                    String name = cmd.getDiskCharacteristics().getName();
                    if(dsMo.folderExists(String.format("[%s]", dsMo.getName()), name))
                    	dsMo.deleteFile(String.format("[%s] %s/", dsMo.getName(), name), dcMo.getMor(), false);

                    s_logger.info("create linked clone from template");
                    if (!vmTemplate.createLinkedClone(name, morBaseSnapshot, dcMo.getVmFolder(), morPool, morDatastore)) {
                        String msg = "Unable to clone from the template";
                        s_logger.error(msg);
                        throw new Exception(msg);
                    }

                    VirtualMachineMO vmMo = new ClusterMO(context, morCluster).findVmOnHyperHost(name);
                    assert (vmMo != null);

                    // we can't rely on un-offical API (VirtualMachineMO.moveAllVmDiskFiles() any more, use hard-coded disk names that we know
                    // to move files
                    s_logger.info("Move volume out of volume-wrapper VM ");
                    dsMo.moveDatastoreFile(String.format("[%s] %s/%s.vmdk", dsMo.getName(), name, name), 
                    	dcMo.getMor(), dsMo.getMor(), 
                    	String.format("[%s] %s.vmdk", dsMo.getName(), name), dcMo.getMor(), true);
                    
                    dsMo.moveDatastoreFile(String.format("[%s] %s/%s-delta.vmdk", dsMo.getName(), name, name), 
                        	dcMo.getMor(), dsMo.getMor(), 
                        	String.format("[%s] %s-delta.vmdk", dsMo.getName(), name), dcMo.getMor(), true);

                    s_logger.info("detach disks from volume-wrapper VM " + name);
                    vmMo.detachAllDisks();

                    s_logger.info("destroy volume-wrapper VM " + name);
                    vmMo.destroy();

                    String srcFile = String.format("[%s] %s/", dsMo.getName(), name);
                    dsMo.deleteFile(srcFile, dcMo.getMor(), true);

                    VolumeTO vol = new VolumeTO(cmd.getVolumeId(), dskch.getType(), pool.getType(), pool.getUuid(), name, pool.getPath(), name, cmd.getDiskCharacteristics().getSize(), null);
                    return new CreateAnswer(cmd, vol);
                }
            } else {
                // create data volume
                VirtualMachineMO vmMo = null;
                String volumeUuid = UUID.randomUUID().toString().replace("-", "");
                String volumeDatastorePath = String.format("[%s] %s.vmdk", dsMo.getName(), volumeUuid);
                String dummyVmName = getWorkerName(context, cmd, 0);
                try {
                    vmMo = prepareVolumeHostDummyVm(hyperHost, dsMo, dummyVmName);
                    if (vmMo == null) {
                        throw new Exception("Unable to create a dummy VM for volume creation");
                    }

                    synchronized (this) {
                        // s_logger.info("Delete file if exists in datastore to clear the way for creating the volume. file: " + volumeDatastorePath);
                        VmwareHelper.deleteVolumeVmdkFiles(dsMo, volumeUuid.toString(), dcMo);

                        vmMo.createDisk(volumeDatastorePath, (int) (cmd.getDiskCharacteristics().getSize() / (1024L * 1024L)), morDatastore, vmMo.getScsiDeviceControllerKey());
                        vmMo.detachDisk(volumeDatastorePath, false);
                    }

                    VolumeTO vol = new VolumeTO(cmd.getVolumeId(), dskch.getType(), pool.getType(), pool.getUuid(), cmd.getDiskCharacteristics().getName(), pool.getPath(), volumeUuid, cmd
                            .getDiskCharacteristics().getSize(), null);
                    return new CreateAnswer(cmd, vol);
                } finally {
                    s_logger.info("Destroy dummy VM after volume creation");
                    vmMo.detachAllDisks();
                    vmMo.destroy();
                }
            }
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            String msg = "CreateCommand failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            return new CreateAnswer(cmd, new Exception(e));
        }
    }

    protected VirtualMachineMO prepareVolumeHostDummyVm(VmwareHypervisorHost hyperHost, DatastoreMO dsMo, String vmName) throws Exception {
        assert (hyperHost != null);

        VirtualMachineMO vmMo = null;
        VirtualMachineConfigSpec vmConfig = new VirtualMachineConfigSpec();
        vmConfig.setName(vmName);
        vmConfig.setMemoryMB((long) 4); // vmware request minimum of 4 MB
        vmConfig.setNumCPUs(1);
        vmConfig.setGuestId(VirtualMachineGuestOsIdentifier._otherGuest.toString());
        VirtualMachineFileInfo fileInfo = new VirtualMachineFileInfo();
        fileInfo.setVmPathName(String.format("[%s]", dsMo.getName()));
        vmConfig.setFiles(fileInfo);

        // Scsi controller
        VirtualLsiLogicController scsiController = new VirtualLsiLogicController();
        scsiController.setSharedBus(VirtualSCSISharing.noSharing);
        scsiController.setBusNumber(0);
        scsiController.setKey(1);
        VirtualDeviceConfigSpec scsiControllerSpec = new VirtualDeviceConfigSpec();
        scsiControllerSpec.setDevice(scsiController);
        scsiControllerSpec.setOperation(VirtualDeviceConfigSpecOperation.add);

        vmConfig.setDeviceChange(new VirtualDeviceConfigSpec[] { scsiControllerSpec });
        hyperHost.createVm(vmConfig);
        vmMo = hyperHost.findVmOnHyperHost(vmName);
        return vmMo;
    }

    @Override
    public void disconnected() {
    }

    @Override
    public IAgentControl getAgentControl() {
        return null;
    }

    @Override
    public PingCommand getCurrentStatus(long id) {
        HashMap<String, State> newStates = sync();
        if (newStates == null) {
            return null;
        }

        try {
            // take the chance to do left-over dummy VM cleanup from previous run
            VmwareContext context = getServiceContext();
            VmwareHypervisorHost hyperHost = getHyperHost(context);
            VmwareManager mgr = hyperHost.getContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
            
            if(hyperHost.isHyperHostConnected()) {
                mgr.gcLeftOverVMs(context);
                
                if(_recycleHungWorker) {
                    s_logger.info("Scan hung worker VM to recycle");
                    
                    // GC worker that has been running for too long
                    ObjectContent[] ocs = hyperHost.getVmPropertiesOnHyperHost(
                            new String[] {"name", "config.template", "runtime.powerState", "runtime.bootTime"});
                    if(ocs != null) {
                        for(ObjectContent oc : ocs) {
                            DynamicProperty[] props = oc.getPropSet();
                            if(props != null) {
                                String name = null;
                                boolean template = false;
                                VirtualMachinePowerState powerState = VirtualMachinePowerState.poweredOff;
                                GregorianCalendar bootTime = null;
                                
                                for(DynamicProperty prop : props) {
                                    if(prop.getName().equals("name"))
                                        name = prop.getVal().toString();
                                    else if(prop.getName().equals("config.template"))
                                        template = (Boolean)prop.getVal();
                                    else if(prop.getName().equals("runtime.powerState"))
                                        powerState = (VirtualMachinePowerState)prop.getVal();
                                    else if(prop.getName().equals("runtime.bootTime")) 
                                        bootTime = (GregorianCalendar)prop.getVal();
                                }
                                
                                if(!template && name.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                                    boolean recycle = false;
    
                                    // recycle stopped worker VM and VM that has been running for too long (hard-coded 10 hours for now)
                                    if(powerState == VirtualMachinePowerState.poweredOff)
                                        recycle = true;
                                    else if(bootTime != null && (new Date().getTime() - bootTime.getTimeInMillis() > 10*3600*1000))
                                        recycle = true;
                                    
                                    if(recycle) {
                                        s_logger.info("Recycle pending worker VM: " + name);
                                        
                                        VirtualMachineMO vmMo = new VirtualMachineMO(hyperHost.getContext(), oc.getObj());
                                        vmMo.powerOff();
                                        vmMo.destroy();
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                s_logger.error("Host is no longer connected.");
            	return null;
            }
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
                return null;
            }
        }

        return new PingRoutingCommand(getType(), id, newStates);
    }

    @Override
    public Type getType() {
        return com.cloud.host.Host.Type.Routing;
    }

    @Override
    public StartupCommand[] initialize() {
        String hostApiVersion = "4.1";
    	VmwareContext context = getServiceContext();
        try {
            VmwareHypervisorHost hyperHost = getHyperHost(context);
            assert(hyperHost instanceof HostMO);
            if(!((HostMO)hyperHost).isHyperHostConnected()) {
            	s_logger.info("Host " + hyperHost.getHyperHostName() + " is not in connected state");
            	return null;
            }
            
            AboutInfo aboutInfo = ((HostMO)hyperHost).getHostAboutInfo();
            hostApiVersion = aboutInfo.getApiVersion();
            
        } catch (Exception e) {
            String msg = "VmwareResource intialize() failed due to : " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg);
            invalidateServiceContext();
            return null;
        }
    	
        StartupRoutingCommand cmd = new StartupRoutingCommand();
        fillHostInfo(cmd);

        Map<String, State> changes = null;
        synchronized (_vms) {
            _vms.clear();
            changes = sync();
        }

        cmd.setHypervisorType(HypervisorType.VMware);
        cmd.setStateChanges(changes);
        cmd.setCluster(_cluster);
        cmd.setVersion(hostApiVersion);

        List<StartupStorageCommand> storageCmds = initializeLocalStorage();
        StartupCommand[] answerCmds = new StartupCommand[1 + storageCmds.size()];
        answerCmds[0] = cmd;
        for (int i = 0; i < storageCmds.size(); i++) {
            answerCmds[i + 1] = storageCmds.get(i);
        }

        return answerCmds;
    }

    private List<StartupStorageCommand> initializeLocalStorage() {
        List<StartupStorageCommand> storageCmds = new ArrayList<StartupStorageCommand>();
        VmwareContext context = getServiceContext();

        try {
            VmwareHypervisorHost hyperHost = getHyperHost(context);
            if (hyperHost instanceof HostMO) {
                HostMO hostMo = (HostMO) hyperHost;

                List<Pair<ManagedObjectReference, String>> dsList = hostMo.getLocalDatastoreOnHost();
                for (Pair<ManagedObjectReference, String> dsPair : dsList) {
                    DatastoreMO dsMo = new DatastoreMO(context, dsPair.first());

                    String poolUuid = dsMo.getCustomFieldValue(CustomFieldConstants.CLOUD_UUID);
                    if (poolUuid == null || poolUuid.isEmpty()) {
                        poolUuid = UUID.randomUUID().toString();
                        dsMo.setCustomFieldValue(CustomFieldConstants.CLOUD_UUID, poolUuid);
                    }

                    DatastoreSummary dsSummary = dsMo.getSummary();
                    String address = hostMo.getHostName();
                    StoragePoolInfo pInfo = new StoragePoolInfo(poolUuid, address, dsMo.getMor().get_value(), "", StoragePoolType.LVM, dsSummary.getCapacity(), dsSummary.getFreeSpace());
                    StartupStorageCommand cmd = new StartupStorageCommand();
                    cmd.setName(poolUuid);
                    cmd.setPoolInfo(pInfo);
                    cmd.setGuid(poolUuid); // give storage host the same UUID as the local storage pool itself
                    cmd.setResourceType(Storage.StorageResourceType.STORAGE_POOL);
                    cmd.setDataCenter(_dcId);
                    cmd.setPod(_pod);
                    cmd.setCluster(_cluster);

                    s_logger.info("Add local storage startup command: " + _gson.toJson(cmd));
                    storageCmds.add(cmd);
                }

            } else {
                s_logger.info("Cluster host does not support local storage, skip it");
            }
        } catch (Exception e) {
            String msg = "initializing local storage failed due to : " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg);
            invalidateServiceContext();
            throw new CloudRuntimeException(msg);
        }

        return storageCmds;
    }

    protected void fillHostInfo(StartupRoutingCommand cmd) {
        VmwareContext serviceContext = getServiceContext();
        Map<String, String> details = cmd.getHostDetails();
        if (details == null) {
            details = new HashMap<String, String>();
        }

        try {
            fillHostHardwareInfo(serviceContext, cmd);
            fillHostNetworkInfo(serviceContext, cmd);
            fillHostDetailsInfo(serviceContext, details);
        } catch (RuntimeFault e) {
            s_logger.error("RuntimeFault while retrieving host info: " + e.toString(), e);
            throw new CloudRuntimeException("RuntimeFault while retrieving host info");
        } catch (RemoteException e) {
            s_logger.error("RemoteException while retrieving host info: " + e.toString(), e);
            invalidateServiceContext();
            throw new CloudRuntimeException("RemoteException while retrieving host info");
        } catch (Exception e) {
            s_logger.error("Exception while retrieving host info: " + e.toString(), e);
            invalidateServiceContext();
            throw new CloudRuntimeException("Exception while retrieving host info: " + e.toString());
        }

        cmd.setHostDetails(details);
        cmd.setName(_url);
        cmd.setGuid(_guid);
        cmd.setDataCenter(_dcId);
        cmd.setPod(_pod);
        cmd.setCluster(_cluster);
        cmd.setVersion(VmwareResource.class.getPackage().getImplementationVersion());
    }

    private void fillHostHardwareInfo(VmwareContext serviceContext, StartupRoutingCommand cmd) throws RuntimeFault, RemoteException, Exception {

        VmwareHypervisorHost hyperHost = getHyperHost(getServiceContext());
        VmwareHypervisorHostResourceSummary summary = hyperHost.getHyperHostResourceSummary();

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Startup report on host hardware info. " + _gson.toJson(summary));
        }

        cmd.setCaps("hvm");
        cmd.setDom0MinMemory(0);
        cmd.setSpeed(summary.getCpuSpeed());
        cmd.setCpus((int) summary.getCpuCount());
        cmd.setMemory(summary.getMemoryBytes());
    }

    private void fillHostNetworkInfo(VmwareContext serviceContext, StartupRoutingCommand cmd) throws RuntimeFault, RemoteException {

        try {
            VmwareHypervisorHost hyperHost = getHyperHost(getServiceContext());
            
            assert(hyperHost instanceof HostMO);
            VmwareManager mgr = hyperHost.getContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
                        
            VmwareHypervisorHostNetworkSummary summary = hyperHost.getHyperHostNetworkSummary(mgr.getManagementPortGroupByHost((HostMO)hyperHost));
            if (summary == null) {
                throw new Exception("No ESX(i) host found");
            }

            if (s_logger.isInfoEnabled()) {
                s_logger.info("Startup report on host network info. " + _gson.toJson(summary));
            }

            cmd.setPrivateIpAddress(summary.getHostIp());
            cmd.setPrivateNetmask(summary.getHostNetmask());
            cmd.setPrivateMacAddress(summary.getHostMacAddress());

            cmd.setStorageIpAddress(summary.getHostIp());
            cmd.setStorageNetmask(summary.getHostNetmask());
            cmd.setStorageMacAddress(summary.getHostMacAddress());

        } catch (Throwable e) {
            String msg = "querying host network info failed due to " + VmwareHelper.getExceptionMessage(e);
            s_logger.error(msg, e);
            throw new CloudRuntimeException(msg);
        }
    }

    private void fillHostDetailsInfo(VmwareContext serviceContext, Map<String, String> details) throws Exception {
        VmwareHypervisorHost hyperHost = getHyperHost(getServiceContext());

        ClusterDasConfigInfo dasConfig = hyperHost.getDasConfig();
        if (dasConfig != null && dasConfig.getEnabled() != null && dasConfig.getEnabled().booleanValue()) {
            details.put("NativeHA", "true");
        }
    }

    protected HashMap<String, State> sync() {
        HashMap<String, State> changes = new HashMap<String, State>();
        HashMap<String, State> oldStates = null;

        try {
            synchronized (_vms) {
                HashMap<String, State> newStates = getVmStates();
                oldStates = new HashMap<String, State>(_vms.size());
                oldStates.putAll(_vms);

                for (final Map.Entry<String, State> entry : newStates.entrySet()) {
                    final String vm = entry.getKey();

                    State newState = entry.getValue();
                    final State oldState = oldStates.remove(vm);

                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("VM " + vm + ": vSphere has state " + newState + " and we have state " + (oldState != null ? oldState.toString() : "null"));
                    }

                    if (vm.startsWith("migrating")) {
                        s_logger.debug("Migrating detected.  Skipping");
                        continue;
                    }

                    if (oldState == null) {
                        _vms.put(vm, newState);
                        s_logger.debug("Detecting a new state but couldn't find a old state so adding it to the changes: " + vm);
                        changes.put(vm, newState);
                    } else if (oldState == State.Starting) {
                        if (newState == State.Running) {
                            _vms.put(vm, newState);
                        } else if (newState == State.Stopped) {
                            s_logger.debug("Ignoring vm " + vm + " because of a lag in starting the vm.");
                        }
                    } else if (oldState == State.Migrating) {
                        if (newState == State.Running) {
                            s_logger.debug("Detected that an migrating VM is now running: " + vm);
                            _vms.put(vm, newState);
                        }
                    } else if (oldState == State.Stopping) {
                        if (newState == State.Stopped) {
                            _vms.put(vm, newState);
                        } else if (newState == State.Running) {
                            s_logger.debug("Ignoring vm " + vm + " because of a lag in stopping the vm. ");
                        }
                    } else if (oldState != newState) {
                        _vms.put(vm, newState);
                        if (newState == State.Stopped) {
                            /*
                             * if (_vmsKilled.remove(vm)) { s_logger.debug("VM " + vm + " has been killed for storage. ");
                             * newState = State.Error; }
                             */
                        }
                        changes.put(vm, newState);
                    }
                }

                for (final Map.Entry<String, State> entry : oldStates.entrySet()) {
                    final String vm = entry.getKey();
                    final State oldState = entry.getValue();

                    if (isVmInCluster(vm)) {
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("VM " + vm + " is now missing from host report but we detected that it might be migrated to other host by vCenter");
                        }
                        
                        if(oldState != State.Starting && oldState != State.Migrating) {
                            s_logger.debug("VM " + vm + " is now missing from host report and VM is not at starting/migrating state, remove it from host VM-sync map, oldState: " + oldState);
                        	_vms.remove(vm);
                        } else {
                        	s_logger.debug("VM " + vm + " is missing from host report, but we will ignore VM " + vm + " in transition state " + oldState);
                        }
                        continue;
                    }

                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("VM " + vm + " is now missing from host report");
                    }

                    if (oldState == State.Stopping) {
                        s_logger.debug("Ignoring VM " + vm + " in transition state stopping.");
                        _vms.remove(vm);
                    } else if (oldState == State.Starting) {
                        s_logger.debug("Ignoring VM " + vm + " in transition state starting.");
                    } else if (oldState == State.Stopped) {
                        _vms.remove(vm);
                    } else if (oldState == State.Migrating) {
                        s_logger.debug("Ignoring VM " + vm + " in migrating state.");
                    } else {
                        State state = State.Stopped;
                        changes.put(entry.getKey(), state);
                    }
                }
            }
        } catch (Throwable e) {
            if (e instanceof RemoteException) {
                s_logger.warn("Encounter remote exception to vCenter, invalidate VMware session context");
                invalidateServiceContext();
            }

            s_logger.error("Unable to perform sync information collection process at this point due to " + VmwareHelper.getExceptionMessage(e), e);
            return null;
        }
        return changes;
    }

    private boolean isVmInCluster(String vmName) throws Exception {
        VmwareHypervisorHost hyperHost = getHyperHost(getServiceContext());

        return hyperHost.findVmOnPeerHyperHost(vmName) != null;
    }

    protected OptionValue[] configureVnc(OptionValue[] optionsToMerge, VmwareHypervisorHost hyperHost, String vmName, 
    	String vncPassword, String keyboardLayout) throws Exception {
    	
        VirtualMachineMO vmMo = hyperHost.findVmOnHyperHost(vmName);

        VmwareManager mgr = hyperHost.getContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
        if(!mgr.beginExclusiveOperation(600))
        	throw new Exception("Unable to begin exclusive operation, lock time out");
        
        try {
	        int maxVncPorts = 64;
	        int vncPort = 0;
	        Random random = new Random();
	
	        HostMO vmOwnerHost = vmMo.getRunningHost();
	
	        ManagedObjectReference morParent = vmOwnerHost.getParentMor();
	        HashMap<String, Integer> portInfo;
	        if(morParent.getType().equalsIgnoreCase("ClusterComputeResource")) {
	        	ClusterMO clusterMo = new ClusterMO(vmOwnerHost.getContext(), morParent);
	        	portInfo = clusterMo.getVmVncPortsOnCluster();
	        } else {
	        	portInfo = vmOwnerHost.getVmVncPortsOnHost();
	        }
	        
	        // allocate first at 5900 - 5964 range
	        Collection<Integer> existingPorts = portInfo.values();
	        int val = random.nextInt(maxVncPorts);
	        int startVal = val;
	        do {
	            if (!existingPorts.contains(5900 + val)) {
	                vncPort = 5900 + val;
	                break;
	            }
	
	            val = (++val) % maxVncPorts;
	        } while (val != startVal);
	        
	        if(vncPort == 0) {
	            s_logger.info("we've run out of range for ports between 5900-5964 for the cluster, we will try port range at 59000-60000");

	            Pair<Integer, Integer> additionalRange = mgr.getAddiionalVncPortRange();
	            maxVncPorts = additionalRange.second();
	            val = random.nextInt(maxVncPorts);
	            startVal = val;
	            do {
	                if (!existingPorts.contains(additionalRange.first() + val)) {
	                    vncPort = additionalRange.first() + val;
	                    break;
	                }
	
	                val = (++val) % maxVncPorts;
	            } while (val != startVal);
	        }
	
	        if (vncPort == 0) {
	            throw new Exception("Unable to find an available VNC port on host");
	        }
	
	        if (s_logger.isInfoEnabled()) {
	            s_logger.info("Configure VNC port for VM " + vmName + ", port: " + vncPort + ", host: " + vmOwnerHost.getHyperHostName());
	        }
	
	        return VmwareHelper.composeVncOptions(optionsToMerge, true, vncPassword, vncPort, keyboardLayout);
        } finally {
        	try {
        		mgr.endExclusiveOperation();
        	} catch(Throwable e) {
        		assert(false);
        		s_logger.error("Unexpected exception ", e);
        	}
        }
    }

    private VirtualMachineGuestOsIdentifier translateGuestOsIdentifier(String cpuArchitecture, String cloudGuestOs) {
        if (cpuArchitecture == null) {
            s_logger.warn("CPU arch is not set, default to i386. guest os: " + cloudGuestOs);
            cpuArchitecture = "i386";
        }

        VirtualMachineGuestOsIdentifier identifier = VmwareGuestOsMapper.getGuestOsIdentifier(cloudGuestOs);
        if (identifier != null) {
            return identifier;
        }

        if (cpuArchitecture.equalsIgnoreCase("x86_64")) {
            return VirtualMachineGuestOsIdentifier.otherGuest64;
        }
        return VirtualMachineGuestOsIdentifier.otherGuest;
    }

    private void prepareNetworkForVmTargetHost(HostMO hostMo, VirtualMachineMO vmMo) throws Exception {
        assert (vmMo != null);
        assert (hostMo != null);

        String[] networks = vmMo.getNetworks();
        for (String networkName : networks) {
            HostPortGroupSpec portGroupSpec = hostMo.getHostPortGroupSpec(networkName);
            HostNetworkTrafficShapingPolicy shapingPolicy = null;
            if (portGroupSpec != null) {
                shapingPolicy = portGroupSpec.getPolicy().getShapingPolicy();
            }

            if (networkName.startsWith("cloud.private")) {
                String[] tokens = networkName.split("\\.");
                if (tokens.length == 3) {
                    Integer networkRateMbps = null;
                    if (shapingPolicy != null && shapingPolicy.getEnabled() != null && shapingPolicy.getEnabled().booleanValue()) {
                        networkRateMbps = (int) (shapingPolicy.getPeakBandwidth().longValue() / (1024 * 1024));
                    }
                    String vlanId = null;
                    if(!"untagged".equalsIgnoreCase(tokens[2]))
                        vlanId = tokens[2];

                    HypervisorHostHelper.prepareNetwork(this._privateNetworkVSwitchName, "cloud.private",
                        hostMo, vlanId, networkRateMbps, null, this._ops_timeout, false);
                } else {
                    s_logger.info("Skip suspecious cloud network " + networkName);
                }
            } else if (networkName.startsWith("cloud.public")) {
                String[] tokens = networkName.split("\\.");
                if (tokens.length == 3) {
                    Integer networkRateMbps = null;
                    if (shapingPolicy != null && shapingPolicy.getEnabled() != null && shapingPolicy.getEnabled().booleanValue()) {
                        networkRateMbps = (int) (shapingPolicy.getPeakBandwidth().longValue() / (1024 * 1024));
                    }
                    String vlanId = null;
                    if(!"untagged".equalsIgnoreCase(tokens[2]))
                        vlanId = tokens[2];

                    HypervisorHostHelper.prepareNetwork(this._publicNetworkVSwitchName, "cloud.public",
                        hostMo, vlanId, networkRateMbps, null, this._ops_timeout, false);
                } else {
                    s_logger.info("Skip suspecious cloud network " + networkName);
                }
            } else if (networkName.startsWith("cloud.guest")) {
                String[] tokens = networkName.split("\\.");
                if (tokens.length >= 3) {
                    Integer networkRateMbps = null;
                    if (shapingPolicy != null && shapingPolicy.getEnabled() != null && shapingPolicy.getEnabled().booleanValue()) {
                        networkRateMbps = (int) (shapingPolicy.getPeakBandwidth().longValue() / (1024 * 1024));
                    }

                    String vlanId = null;
                    if(!"untagged".equalsIgnoreCase(tokens[2]))
                        vlanId = tokens[2];

                    HypervisorHostHelper.prepareNetwork(this._guestNetworkVSwitchName, "cloud.guest",
                        hostMo, vlanId, networkRateMbps, null, this._ops_timeout, false);
                } else {
                    s_logger.info("Skip suspecious cloud network " + networkName);
                }
            } else {
                s_logger.info("Skip non-cloud network " + networkName + " when preparing target host");
            }
        }
    }
    
    private HashMap<String, State> getVmStates() throws Exception {
        VmwareHypervisorHost hyperHost = getHyperHost(getServiceContext());
        ObjectContent[] ocs = hyperHost.getVmPropertiesOnHyperHost(new String[] { "name", "runtime.powerState", "config.template" });

        HashMap<String, State> newStates = new HashMap<String, State>();
        if (ocs != null && ocs.length > 0) {
            for (ObjectContent oc : ocs) {
                DynamicProperty[] objProps = oc.getPropSet();
                if (objProps != null) {

                    boolean isTemplate = false;
                    String name = null;
                    VirtualMachinePowerState powerState = VirtualMachinePowerState.poweredOff;
                    for (DynamicProperty objProp : objProps) {
                        if (objProp.getName().equals("config.template")) {
                            if (objProp.getVal().toString().equalsIgnoreCase("true")) {
                                isTemplate = true;
                            }
                        } else if (objProp.getName().equals("name")) {
                            name = (String) objProp.getVal();
                        } else if (objProp.getName().equals("runtime.powerState")) {
                            powerState = (VirtualMachinePowerState) objProp.getVal();
                        } else {
                            assert (false);
                        }
                    }

                    if (!isTemplate) {
                        newStates.put(name, convertState(powerState));
                    }
                }
            }
        }
        return newStates;
    }

    private HashMap<String, VmStatsEntry> getVmStats(List<String> vmNames) throws Exception {
        VmwareHypervisorHost hyperHost = getHyperHost(getServiceContext());
        HashMap<String, VmStatsEntry> vmResponseMap = new HashMap<String, VmStatsEntry>();
        ManagedObjectReference perfMgr = getServiceContext().getServiceConnection().getServiceContent().getPerfManager();
        VimPortType service = getServiceContext().getServiceConnection().getService();
        PerfCounterInfo rxPerfCounterInfo = null;
        PerfCounterInfo txPerfCounterInfo = null;

        PerfCounterInfo[] cInfo = (PerfCounterInfo[]) getServiceContext().getServiceUtil().getDynamicProperty(perfMgr, "perfCounter");
        for(int i=0; i<cInfo.length; ++i) {
            if ("net".equalsIgnoreCase(cInfo[i].getGroupInfo().getKey())) { 
                if ("transmitted".equalsIgnoreCase(cInfo[i].getNameInfo().getKey())) {
                    txPerfCounterInfo = cInfo[i];
                }
                if ("received".equalsIgnoreCase(cInfo[i].getNameInfo().getKey())) {
                    rxPerfCounterInfo = cInfo[i];
                }
            }
        }

        ObjectContent[] ocs = hyperHost.getVmPropertiesOnHyperHost(new String[] {"name", "summary.config.numCpu", "summary.quickStats.overallCpuUsage"});
        if (ocs != null && ocs.length > 0) {
            for (ObjectContent oc : ocs) {
                DynamicProperty[] objProps = oc.getPropSet();
                if (objProps != null) {
                    String name = null;
                    String numberCPUs = null;
                    String maxCpuUsage = null;

                    for (DynamicProperty objProp : objProps) {
                        if (objProp.getName().equals("name")) {
                        	name = objProp.getVal().toString();
                        } else if (objProp.getName().equals("summary.config.numCpu")) {
                        	numberCPUs = objProp.getVal().toString();
                        } else if (objProp.getName().equals("summary.quickStats.overallCpuUsage")) {
                        	maxCpuUsage =  objProp.getVal().toString();
                        }
                    }

                    if (!vmNames.contains(name)) {
                        continue;
                    }

                    ManagedObjectReference vmMor = hyperHost.findVmOnHyperHost(name).getMor();
                    assert(vmMor!=null);

                    ArrayList vmNetworkMetrics = new ArrayList();
                    // get all the metrics from the available sample period 
                    PerfMetricId[] perfMetrics = service.queryAvailablePerfMetric(perfMgr, vmMor, null, null, null);
                    if(perfMetrics != null) {
                       for(int index=0; index < perfMetrics.length; ++index) {
                           if ( ((rxPerfCounterInfo != null) && (perfMetrics[index].getCounterId() == rxPerfCounterInfo.getKey())) || 
                                   ((txPerfCounterInfo != null) && (perfMetrics[index].getCounterId() == txPerfCounterInfo.getKey())) ) {
                               vmNetworkMetrics.add(perfMetrics[index]);
                          }
                       }
                    }

                    double networkReadKBs=0;
                    double networkWriteKBs=0;
                    long sampleDuration=0;

                    if (vmNetworkMetrics.size() != 0) {
                        PerfQuerySpec qSpec = new PerfQuerySpec();
                        qSpec.setEntity(vmMor);
                        PerfMetricId[] availableMetricIds = (PerfMetricId[]) vmNetworkMetrics.toArray(new PerfMetricId[0]);
                        qSpec.setMetricId(availableMetricIds);
                        PerfQuerySpec[] qSpecs = new PerfQuerySpec[] {qSpec};
                        PerfEntityMetricBase[] values = service.queryPerf(perfMgr, qSpecs);

                        for(int i=0; i<values.length; ++i) {
                            PerfSampleInfo[]  infos = ((PerfEntityMetric)values[i]).getSampleInfo();
                            sampleDuration = (infos[infos.length-1].getTimestamp().getTimeInMillis() - infos[0].getTimestamp().getTimeInMillis()) /1000;
                            PerfMetricSeries[] vals = ((PerfEntityMetric)values[i]).getValue();
                            for(int vi = 0; ((vals!= null) && (vi < vals.length)); ++vi){
                                if(vals[vi] instanceof PerfMetricIntSeries) {
                                    PerfMetricIntSeries val = (PerfMetricIntSeries)vals[vi];
                                    long[] perfValues = val.getValue();
                                    if (vals[vi].getId().getCounterId() == rxPerfCounterInfo.getKey()) {
                                        networkReadKBs = sampleDuration * perfValues[3]; //get the average RX rate multiplied by sampled duration
                                    }
                                    if (vals[vi].getId().getCounterId() == txPerfCounterInfo.getKey()) {
                                        networkWriteKBs = sampleDuration * perfValues[3];//get the average TX rate multiplied by sampled duration
                                    }
                               }
                            }
                         }
                    }
                    vmResponseMap.put(name, new VmStatsEntry(Integer.parseInt(maxCpuUsage), networkReadKBs, networkWriteKBs, Integer.parseInt(numberCPUs), "vm"));
                }
            }
        }
        return vmResponseMap;    	
    }    

    protected String networkUsage(final String privateIpAddress, final String option, final String ethName) {
        String args = null;
        if (option.equals("get")) {
            args = "-g";
        } else if (option.equals("create")) {
            args = "-c";
        } else if (option.equals("reset")) {
            args = "-r";
        } else if (option.equals("addVif")) {
            args = "-a";
            args += ethName;
        } else if (option.equals("deleteVif")) {
            args = "-d";
            args += ethName;
        }

        try {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Executing /root/netusage.sh " + args + " on DomR " + privateIpAddress);
            }

            VmwareManager mgr = getServiceContext().getStockObject(VmwareManager.CONTEXT_STOCK_NAME);

            Pair<Boolean, String> result = SshHelper.sshExecute(privateIpAddress, DEFAULT_DOMR_SSHPORT, "root", mgr.getSystemVMKeyFile(), null, "/root/netusage.sh " + args);

            if (!result.first()) {
                return null;
            }

            return result.second();
        } catch (Throwable e) {
            s_logger.error("Unable to execute NetworkUsage command on DomR (" + privateIpAddress + "), domR may not be ready yet. failure due to " 
                + VmwareHelper.getExceptionMessage(e), e);
        }

        return null;
    }

    private long[] getNetworkStats(String privateIP) {
        String result = networkUsage(privateIP, "get", null);
        long[] stats = new long[2];
        if (result != null) {
            try {
                String[] splitResult = result.split(":");
                int i = 0;
                while (i < splitResult.length - 1) {
                    stats[0] += (new Long(splitResult[i++])).longValue();
                    stats[1] += (new Long(splitResult[i++])).longValue();
                }
            } catch (Throwable e) {
                s_logger.warn("Unable to parse return from script return of network usage command: " + e.toString(), e);
            }
        }
        return stats;
    }

    protected String connect(final String vmName, final String ipAddress, final int port) {
        long startTick = System.currentTimeMillis();

        // wait until we have at least been waiting for _ops_timeout time or
        // at least have tried _retry times, this is to coordinate with system
        // VM patching/rebooting time that may need
        int retry = _retry;
        while (System.currentTimeMillis() - startTick <= _ops_timeout || --retry > 0) {
            SocketChannel sch = null;
            try {
                s_logger.info("Trying to connect to " + ipAddress);
                sch = SocketChannel.open();
                sch.configureBlocking(true);
                sch.socket().setSoTimeout(5000);

                InetSocketAddress addr = new InetSocketAddress(ipAddress, port);
                sch.connect(addr);
                return null;
            } catch (IOException e) {
                s_logger.info("Could not connect to " + ipAddress + " due to " + e.toString());
                if (e instanceof ConnectException) {
                    // if connection is refused because of VM is being started,
                    // we give it more sleep time
                    // to avoid running out of retry quota too quickly
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                    }
                }
            } finally {
                if (sch != null) {
                    try {
                        sch.close();
                    } catch (IOException e) {
                    }
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }

        s_logger.info("Unable to logon to " + ipAddress);

        return "Unable to connect";
    }

    protected String connect(final String vmname, final String ipAddress) {
        return connect(vmname, ipAddress, 3922);
    }

    private static State convertState(VirtualMachinePowerState powerState) {
        return s_statesTable.get(powerState);
    }

    private static State getVmState(VirtualMachineMO vmMo) throws Exception {
        VirtualMachineRuntimeInfo runtimeInfo = vmMo.getRuntimeInfo();
        return convertState(runtimeInfo.getPowerState());
    }

    private static HostStatsEntry getHyperHostStats(VmwareHypervisorHost hyperHost) throws Exception {
        ComputeResourceSummary hardwareSummary = hyperHost.getHyperHostHardwareSummary();
        if(hardwareSummary == null)
        	return null;
        
        HostStatsEntry entry = new HostStatsEntry();

        entry.setEntityType("host");
        double cpuUtilization = ((double) (hardwareSummary.getTotalCpu() - hardwareSummary.getEffectiveCpu()) / (double) hardwareSummary.getTotalCpu() * 100);
        entry.setCpuUtilization(cpuUtilization);
        entry.setTotalMemoryKBs(hardwareSummary.getTotalMemory() / 1024);
        entry.setFreeMemoryKBs(hardwareSummary.getEffectiveMemory() * 1024);

        return entry;
    }
    
    private static String getRouterSshControlIp(NetworkElementCommand cmd) {
    	String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
    	String routerGuestIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP);
    	String zoneNetworkType = cmd.getAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE);
    	
    	if(routerGuestIp != null && zoneNetworkType != null && NetworkType.valueOf(zoneNetworkType) == NetworkType.Basic) {
    		if(s_logger.isDebugEnabled())
    			s_logger.debug("In Basic zone mode, use router's guest IP for SSH control. guest IP : " + routerGuestIp);
    		
    		return routerGuestIp;
    	}
    	
		if(s_logger.isDebugEnabled())
			s_logger.debug("Use router's private IP for SSH control. IP : " + routerIp);
    	return routerIp;
    }

    @Override
    public void setAgentControl(IAgentControl agentControl) {
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _name = name;

        _url = (String) params.get("url");
        _username = (String) params.get("username");
        _password = (String) params.get("password");
        _dcId = (String) params.get("zone");
        _pod = (String) params.get("pod");
        _cluster = (String) params.get("cluster");
        
        _guid = (String) params.get("guid");
        String[] tokens = _guid.split("@");
        _vCenterAddress = tokens[1];
        _morHyperHost = new ManagedObjectReference();
        String[] hostTokens = tokens[0].split(":");
        _morHyperHost.setType(hostTokens[0]);
        _morHyperHost.set_value(hostTokens[1]);
        
        VmwareContext context = getServiceContext();
        try {
            VmwareManager mgr = context.getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
            mgr.setupResourceStartupParams(params);

            CustomFieldsManagerMO cfmMo = new CustomFieldsManagerMO(context, context.getServiceContent().getCustomFieldsManager());
            cfmMo.ensureCustomFieldDef("Datastore", CustomFieldConstants.CLOUD_UUID);
            cfmMo.ensureCustomFieldDef("Network", CustomFieldConstants.CLOUD_GC);
            cfmMo.ensureCustomFieldDef("VirtualMachine", CustomFieldConstants.CLOUD_UUID);
            cfmMo.ensureCustomFieldDef("VirtualMachine", CustomFieldConstants.CLOUD_NIC_MASK);

            VmwareHypervisorHost hostMo = this.getHyperHost(context);
            _hostName = hostMo.getHyperHostName();
        } catch (Exception e) {
            s_logger.error("Unexpected Exception ", e);
        }

        _privateNetworkVSwitchName = (String) params.get("private.network.vswitch.name");
        _publicNetworkVSwitchName = (String) params.get("public.network.vswitch.name");
        _guestNetworkVSwitchName = (String) params.get("guest.network.vswitch.name");

        String value = (String) params.get("cpu.overprovisioning.factor");
        if(value != null)
            _cpuOverprovisioningFactor = Float.parseFloat(value);

        value = (String) params.get("vmware.reserve.cpu");
        if(value != null && value.equalsIgnoreCase("true"))
            _reserveCpu = true;
        
        value = (String) params.get("vmware.recycle.hung.wokervm");
        if(value != null && value.equalsIgnoreCase("true"))
            _recycleHungWorker = true;

        value = (String) params.get("mem.overprovisioning.factor");
        if(value != null)
            _memOverprovisioningFactor = Float.parseFloat(value);

        value = (String) params.get("vmware.reserve.mem");
        if(value != null && value.equalsIgnoreCase("true"))
            _reserveMem = true;
        
        value = (String)params.get("vmware.root.disk.controller");
        if(value != null && value.equalsIgnoreCase("scsi"))
        	_rootDiskController = DiskControllerType.scsi;
        else
        	_rootDiskController = DiskControllerType.ide;

        s_logger.info("VmwareResource network configuration info. private vSwitch: " + _privateNetworkVSwitchName + ", public vSwitch: " + _publicNetworkVSwitchName + ", guest network: "
                + _guestNetworkVSwitchName);

        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    private VmwareContext getServiceContext() {
        return getServiceContext(null);
    }

    private void invalidateServiceContext() {
        invalidateServiceContext(null);
    }

    private VmwareHypervisorHost getHyperHost(VmwareContext context) {
        return getHyperHost(context, null);
    }

    @Override
    public synchronized VmwareContext getServiceContext(Command cmd) {
        if (_serviceContext == null) {
            try {
                _serviceContext = VmwareContextFactory.create(_vCenterAddress, _username, _password);
                VmwareHypervisorHost hyperHost = getHyperHost(_serviceContext, cmd);
                assert(hyperHost instanceof HostMO);
                
                HostFirewallSystemMO firewallMo = ((HostMO)hyperHost).getHostFirewallSystemMO();
                boolean bRefresh = false;
                if(firewallMo != null) {
                	HostFirewallInfo firewallInfo = firewallMo.getFirewallInfo();
                	if(firewallInfo != null) {
                		for(HostFirewallRuleset rule : firewallInfo.getRuleset()) {
                			if("vncServer".equalsIgnoreCase(rule.getKey())) {
                				bRefresh = true;
                				firewallMo.enableRuleset("vncServer");
                			} else if("gdbserver".equalsIgnoreCase(rule.getKey())) {
                				bRefresh = true;
                				firewallMo.enableRuleset("gdbserver");
                			}
                		}
                	}
                	
                	if(bRefresh)
                		firewallMo.refreshFirewall();
                }
            } catch (Exception e) {
                s_logger.error("Unable to connect to vSphere server: " + _vCenterAddress, e);
                throw new CloudRuntimeException("Unable to connect to vSphere server: " + _vCenterAddress);
            }
        }

        return _serviceContext;
    }

    @Override
    public synchronized void invalidateServiceContext(VmwareContext context) {
        if (_serviceContext == null) {
            _serviceContext.close();
        }
        _serviceContext = null;
    }

    @Override
    public VmwareHypervisorHost getHyperHost(VmwareContext context, Command cmd) {
        if (_morHyperHost.getType().equalsIgnoreCase("HostSystem")) {
        	return new HostMO(context, _morHyperHost);
        }
        return new ClusterMO(context, _morHyperHost);
    }

    @Override
    @DB
    public String getWorkerName(VmwareContext context, Command cmd, int workerSequence) {
        VmwareManager mgr = context.getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
        String vmName = mgr.composeWorkerName();

        assert(cmd != null);
        VmwareManager vmwareMgr = context.getStockObject(VmwareManager.CONTEXT_STOCK_NAME);
        long checkPointId = vmwareMgr.pushCleanupCheckpoint(this._guid, vmName);
        cmd.setContextParam("checkpoint", String.valueOf(checkPointId));
        return vmName;
    }
}
