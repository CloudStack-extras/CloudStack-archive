/**
 * *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
*
 *
 * This software is licensed under the GNU General Public License v3 or later.
 *
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.cloud.hypervisor.hyperv.resource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.IAgentControl;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.AttachIsoCommand;
import com.cloud.agent.api.AttachVolumeCommand;
import com.cloud.agent.api.BackupSnapshotCommand;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.agent.api.CheckNetworkCommand;
import com.cloud.agent.api.CheckOnHostCommand;
import com.cloud.agent.api.CheckVirtualMachineCommand;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.CreatePrivateTemplateFromSnapshotCommand;
import com.cloud.agent.api.CreatePrivateTemplateFromVolumeCommand;
import com.cloud.agent.api.CreateVolumeFromSnapshotCommand;
import com.cloud.agent.api.DeleteStoragePoolCommand;
import com.cloud.agent.api.GetHostStatsAnswer;
import com.cloud.agent.api.GetHostStatsCommand;
import com.cloud.agent.api.GetStorageStatsAnswer;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.agent.api.GetVmStatsCommand;
import com.cloud.agent.api.GetVncPortCommand;
import com.cloud.agent.api.HostStatsEntry;
import com.cloud.agent.api.MaintainCommand;
import com.cloud.agent.api.ManageSnapshotCommand;
import com.cloud.agent.api.MigrateCommand;
import com.cloud.agent.api.ModifySshKeysCommand;
import com.cloud.agent.api.ModifyStoragePoolAnswer;
import com.cloud.agent.api.ModifyStoragePoolCommand;
import com.cloud.agent.api.NetworkUsageCommand;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingRoutingCommand;
import com.cloud.agent.api.PingTestCommand;
import com.cloud.agent.api.PoolEjectCommand;
import com.cloud.agent.api.PrepareForMigrationCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.RebootCommand;
import com.cloud.agent.api.RebootRouterCommand;
import com.cloud.agent.api.SetupCommand;
import com.cloud.agent.api.StartAnswer;
import com.cloud.agent.api.StartCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.agent.api.StopCommand;
import com.cloud.agent.api.ValidateSnapshotCommand;
import com.cloud.agent.api.check.CheckSshAnswer;
import com.cloud.agent.api.check.CheckSshCommand;
import com.cloud.agent.api.routing.DhcpEntryCommand;
import com.cloud.agent.api.routing.IpAssocCommand;
import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.RemoteAccessVpnCfgCommand;
import com.cloud.agent.api.routing.SavePasswordCommand;
import com.cloud.agent.api.routing.SetPortForwardingRulesCommand;
import com.cloud.agent.api.routing.SetStaticNatRulesCommand;
import com.cloud.agent.api.routing.VmDataCommand;
import com.cloud.agent.api.routing.VpnUsersCfgCommand;
import com.cloud.agent.api.storage.CopyVolumeCommand;
import com.cloud.agent.api.storage.CreateAnswer;
import com.cloud.agent.api.storage.CreateCommand;
import com.cloud.agent.api.storage.DestroyCommand;
import com.cloud.agent.api.storage.PrimaryStorageDownloadAnswer;
import com.cloud.agent.api.storage.PrimaryStorageDownloadCommand;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.host.Host.Type;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.vmware.resource.SshHelper;
import com.cloud.resource.ServerResource;
import com.cloud.resource.ServerResourceBase;
import com.cloud.serializer.GsonHelper;
import com.cloud.storage.Volume;
import com.cloud.storage.template.TemplateInfo;
import com.cloud.utils.Pair;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachine.State;
import com.google.gson.Gson;

/**
 * Implementation of resource base class for Hyper-V hypervisor
 **/

@Local(value={ServerResource.class})
public class HypervResource extends ServerResourceBase implements ServerResource{
    private String _dcId;
    private String _podId;
    private String _clusterId;
    private String _guid;
    private String _name;
    private static final Logger s_logger = Logger.getLogger(HypervResource.class);
    private IAgentControl agentControl;
    private volatile Boolean _wakeUp = false;
    protected Gson _gson;
    protected HashMap<String, State> _vms = new HashMap<String, State>(512);
    protected final int DEFAULT_DOMR_SSHPORT = 3922;

    public HypervResource()
    {
        _gson = GsonHelper.getGsonLogger();
    }

    @Override
    public Answer executeRequest(Command cmd) {

        if (cmd instanceof CreateCommand) {
            return execute((CreateCommand) cmd);
        } else if (cmd instanceof SetPortForwardingRulesCommand) {
            s_logger.info("SCVMM agent recived command SetPortForwardingRulesCommand");
            //return execute((SetPortForwardingRulesCommand) cmd);
        } else if (cmd instanceof SetStaticNatRulesCommand) {
            s_logger.info("SCVMM agent recived command SetStaticNatRulesCommand");
            //return execute((SetStaticNatRulesCommand) cmd);
        }else if (cmd instanceof LoadBalancerConfigCommand) {
            s_logger.info("SCVMM agent recived command SetStaticNatRulesCommand");
            //return execute((LoadBalancerConfigCommand) cmd);
        } else if (cmd instanceof IpAssocCommand) {
            s_logger.info("SCVMM agent recived command IPAssocCommand");
            //return execute((IPAssocCommand) cmd);
        } else if (cmd instanceof SavePasswordCommand) {
            s_logger.info("SCVMM agent recived command SavePasswordCommand");
            //return execute((SavePasswordCommand) cmd);
        } else if (cmd instanceof DhcpEntryCommand) {
            return execute((DhcpEntryCommand) cmd);
        } else if (cmd instanceof VmDataCommand) {
            //return execute((VmDataCommand) cmd);
        } else if (cmd instanceof ReadyCommand) {
            s_logger.info("SCVMM agent recived ReadyCommand: " + _gson.toJson(cmd));
            return new ReadyAnswer((ReadyCommand) cmd);
        } else if (cmd instanceof GetHostStatsCommand) {
            return execute((GetHostStatsCommand) cmd);
        } else if (cmd instanceof GetVmStatsCommand) {
            s_logger.info("SCVMM agent recived command GetVmStatsCommand");
            //return execute((GetVmStatsCommand) cmd);
        } else if (cmd instanceof CheckHealthCommand) {
            //return execute((CheckHealthCommand) cmd);
        } else if (cmd instanceof StopCommand) {
            //return execute((StopCommand) cmd);
        } else if (cmd instanceof RebootRouterCommand) {
            //return execute((RebootRouterCommand) cmd);
        } else if (cmd instanceof RebootCommand) {
            //return execute((RebootCommand) cmd);
        } else if (cmd instanceof CheckVirtualMachineCommand) {
            s_logger.info("SCVMM agent recived command CheckVirtualMachineCommand");
            //return execute((CheckVirtualMachineCommand) cmd);
        } else if (cmd instanceof PrepareForMigrationCommand) {
            //return execute((PrepareForMigrationCommand) cmd);
        } else if (cmd instanceof MigrateCommand) {
            //return execute((MigrateCommand) cmd);
        } else if (cmd instanceof DestroyCommand) {
            //return execute((DestroyCommand) cmd);
        } else if (cmd instanceof ModifyStoragePoolCommand) {
            return execute((ModifyStoragePoolCommand) cmd);
        } else if (cmd instanceof DeleteStoragePoolCommand) {
            s_logger.info("SCVMM agent recived command DeleteStoragePoolCommand");
            Answer answer = new Answer(cmd, true, "success");
            return answer;
            //return execute((DeleteStoragePoolCommand) cmd);
        } else if (cmd instanceof CopyVolumeCommand) {
            s_logger.info("SCVMM agent recived command CopyVolumeCommand");
            //return execute((CopyVolumeCommand) cmd);
        } else if (cmd instanceof AttachVolumeCommand) {
            s_logger.info("SCVMM agent recived command AttachVolumeCommand");
            //return execute((AttachVolumeCommand) cmd);
        } else if (cmd instanceof AttachIsoCommand) {
            //return execute((AttachIsoCommand) cmd);
        } else if (cmd instanceof ValidateSnapshotCommand) {
            //return execute((ValidateSnapshotCommand) cmd);
        } else if (cmd instanceof ManageSnapshotCommand) {
            //return execute((ManageSnapshotCommand) cmd);
        } else if (cmd instanceof BackupSnapshotCommand) {
            //return execute((BackupSnapshotCommand) cmd);
        } else if (cmd instanceof CreateVolumeFromSnapshotCommand) {
            //return execute((CreateVolumeFromSnapshotCommand) cmd);
        } else if (cmd instanceof CreatePrivateTemplateFromVolumeCommand) {
            //return execute((CreatePrivateTemplateFromVolumeCommand) cmd);
        } else if (cmd instanceof CreatePrivateTemplateFromSnapshotCommand) {
            //return execute((CreatePrivateTemplateFromSnapshotCommand) cmd);
        } else if (cmd instanceof GetStorageStatsCommand) {
            return execute((GetStorageStatsCommand) cmd);
        } else if (cmd instanceof PrimaryStorageDownloadCommand) {
            s_logger.info("SCVMM agent recived command PrimaryStorageDownloadCommand");
            return execute((PrimaryStorageDownloadCommand) cmd);
        } else if (cmd instanceof GetVncPortCommand) {
            //return execute((GetVncPortCommand) cmd);
        } else if (cmd instanceof SetupCommand) {
            //return execute((SetupCommand) cmd);
        } else if (cmd instanceof MaintainCommand) {
            //return execute((MaintainCommand) cmd);
        } else if (cmd instanceof PingTestCommand) {
            s_logger.info("SCVMM agent recived command PingTestCommand");
            //return execute((PingTestCommand) cmd);
        } else if (cmd instanceof CheckOnHostCommand) {
            s_logger.info("SCVMM agent recived command CheckOnHostCommand");
            //return execute((CheckOnHostCommand) cmd);
        } else if (cmd instanceof ModifySshKeysCommand) {
            //return execute((ModifySshKeysCommand) cmd);
        } else if (cmd instanceof PoolEjectCommand) {
            //return execute((PoolEjectCommand) cmd);
        } else if (cmd instanceof NetworkUsageCommand) {
            //return execute((NetworkUsageCommand) cmd);
        } else if (cmd instanceof StartCommand) {
            return execute((StartCommand) cmd);
        } else if (cmd instanceof RemoteAccessVpnCfgCommand) {
            //return execute((RemoteAccessVpnCfgCommand) cmd);
        } else if (cmd instanceof VpnUsersCfgCommand) {
            //return execute((VpnUsersCfgCommand) cmd);
        } else if (cmd instanceof CheckSshCommand) {
            return execute((CheckSshCommand)cmd);
        } else if (cmd instanceof CheckNetworkCommand) {
            //return execute((CheckNetworkCommand) cmd);
        } else {
            s_logger.info("SCVMM agent recived unimplemented command: " + _gson.toJson(cmd));
            return Answer.createUnsupportedCommandAnswer(cmd);
        }

        return Answer.createUnsupportedCommandAnswer(cmd);
    }

    public PrimaryStorageDownloadAnswer execute(PrimaryStorageDownloadCommand cmd) {

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource PrimaryStorageDownloadCommand: " + _gson.toJson(cmd));
        }

        try {
            String secondaryStorageUrl = cmd.getSecondaryStorageUrl();
            String primaryStroageUrl = cmd.getPrimaryStorageUrl();
            String templateUuidName =null;
            assert ((primaryStroageUrl != null) && (secondaryStorageUrl != null));
            // FIXME: paths and system vm name are hard coded
            String templateUrl = cmd.getUrl();
            String templatePath = templateUrl.replace('/', '\\');
            templatePath = templatePath.substring(4);
            if (!templatePath.endsWith(".vhd")) {
                String templateName = cmd.getName();
                templateUuidName = UUID.nameUUIDFromBytes((templateName + "@" + cmd.getPoolUuid()).getBytes()).toString();
                if (!templatePath.endsWith("\\")) {
                    templatePath = templatePath + "\\";
                }
                //templatePath = templatePath + templateUuidName + ".vhd";
                templatePath = templatePath + "systemvm.vhd";
            }

            s_logger.info("template URL: "+ templateUrl + "template name: "+ cmd.getName() + "  sec storage " + secondaryStorageUrl+ " pri storage" + primaryStroageUrl);

            StringBuilder cmdStr = new StringBuilder("cmd /c powershell.exe ");
            cmdStr.append("copy-item '");
            cmdStr.append(templatePath.toCharArray());
            cmdStr.append("'  'C:\\programdata\\Virtual Machine Manager Library Files\\VHDs\\';");

            s_logger.info("Running command: " + cmdStr);
            Process p = Runtime.getRuntime().exec(cmdStr.toString());
            p.getOutputStream().close();

            InputStreamReader temperrReader = new InputStreamReader(new BufferedInputStream(p.getErrorStream()));
            BufferedReader errreader = new BufferedReader(temperrReader);
            if (errreader.ready()) {
                String errorOutput = new String("");
                s_logger.info("errors found while running cmdlet: " + cmdStr.toString());
                while (true){
                    String errline = errreader.readLine();
                    if (errline == null) {
                        break;
                    }
                    errorOutput = errorOutput + errline;
                }
                s_logger.info(errorOutput);
            }

            p.getErrorStream().close();
            InputStreamReader tempReader = new InputStreamReader(new BufferedInputStream(p.getInputStream()));
            BufferedReader reader = new BufferedReader(tempReader);

            String output = new String("");

            while (true){
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                output = output + line;
            }
            p.getInputStream().close();

            s_logger.info("Command output: "+ output);

            if (output.contains("FullyQualifiedErrorId") || output.contains("Error") || output.contains("Exception")) {
                return new PrimaryStorageDownloadAnswer("Failed to copy template to SCVMM library share from secondary storage.");
            }

            return new PrimaryStorageDownloadAnswer(templateUuidName, 0);

        } catch (Exception e)
        {
            s_logger.info("Exception caught: "+e.getMessage());
            return new PrimaryStorageDownloadAnswer("Failed to copy template to SCVMM library share from secondary storage.");
        }
    }

    protected Answer execute(CreateCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource CreateCommand: " + _gson.toJson(cmd));
        }

        try {
            long volId = cmd.getVolumeId();
            String templateUrl = cmd.getTemplateUrl();;
            StorageFilerTO pool = cmd.getPool();
            DiskProfile diskchar = cmd.getDiskCharacteristics();

            if (diskchar.getType() == Volume.Type.ROOT) {
                if (cmd.getTemplateUrl() == null) {
                    //create root volume
                    VolumeTO vol = new VolumeTO(cmd.getVolumeId(),
                            diskchar.getType(),
                            pool.getType(), pool.getUuid(), cmd.getDiskCharacteristics().getName(),
                            pool.getPath(), cmd.getDiskCharacteristics().getName(), cmd.getDiskCharacteristics().getSize(),
                            null);
                    return new CreateAnswer(cmd, vol);
                } else {
                    VolumeTO vol = new VolumeTO(cmd.getVolumeId(),
                            diskchar.getType(),
                            pool.getType(), pool.getUuid(), cmd.getDiskCharacteristics().getName(),
                            pool.getPath(), cmd.getDiskCharacteristics().getName(), cmd.getDiskCharacteristics().getSize(), null);
                    return new CreateAnswer(cmd, vol);
                }

            } else {
                //create data volume
                String volumeUuid = "cloud.worker." + UUID.randomUUID().toString();
                VolumeTO vol = new VolumeTO(cmd.getVolumeId(),
                        diskchar.getType(),
                        pool.getType(), pool.getUuid(), cmd.getDiskCharacteristics().getName(),
                        pool.getPath(), volumeUuid, cmd.getDiskCharacteristics().getSize(), null);
                return new CreateAnswer(cmd, vol);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    protected StartAnswer execute(StartCommand cmd) {

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource StartCommand: " + _gson.toJson(cmd));
        }

        VirtualMachineTO vmSpec = cmd.getVirtualMachine();
        String vmName = vmSpec.getName();
        State state = State.Stopped;
        String scriptFileName = vmName+ ".ps1";
        String newLine = System.getProperty("line.separator");
        String bootArgsDiskName  = vmName+"-bootparams.vhd";
        String bootArgsDiskPath  = "C:\\ProgramData\\Virtual Machine Manager Library Files\\VHDs\\"+bootArgsDiskName;

        try {
            // mark VM as starting state so that sync() can know not to report stopped too early
            synchronized (_vms) {
                _vms.put(vmName, State.Starting);
                {
                    // create and attach boot parameter disk
                    String diskpartScriptName = vmName+"-Diskpart.txt";

                    StringBuilder cmdDiskpart = new StringBuilder("create vdisk file=\"");cmdDiskpart.append(bootArgsDiskPath.toCharArray());cmdDiskpart.append("\" maximum=10 type=expandable" + newLine);
                    cmdDiskpart.append("select vdisk file=\"");cmdDiskpart.append(bootArgsDiskPath.toCharArray());cmdDiskpart.append("\"" + newLine);
                    cmdDiskpart.append("attach vdisk" + newLine);
                    cmdDiskpart.append("create partition primary" + newLine);
                    cmdDiskpart.append("format fs=ntfs label=\"test vhd\" quick" + newLine);
                    cmdDiskpart.append("assign letter="+vmName.toCharArray()[0]+ newLine);
                    cmdDiskpart.append("attach vdisk" + newLine);

                    File f=new File(diskpartScriptName);
                    FileOutputStream fop=new FileOutputStream(f);
                    fop.write(cmdDiskpart.toString().getBytes());
                    fop.flush();
                    fop.close();

                    s_logger.info("Running diskpart attach command");
                    Process p = Runtime.getRuntime().exec("cmd.exe /c diskpart.exe /s "+diskpartScriptName);
                    p.getOutputStream().close();

                    InputStreamReader temperrReader = new InputStreamReader(new BufferedInputStream(p.getErrorStream()));
                    BufferedReader errreader = new BufferedReader(temperrReader);

                    if (errreader.ready()) {
                        String errorOutput = new String("");
                        while (true){
                            String errline = errreader.readLine();
                            if (errline == null) {
                                break;
                            }
                            errorOutput = errorOutput + errline;
                        }
                        s_logger.info("errors found while running diskpart command: " + errorOutput);
                    }

                    p.getErrorStream().close();
                    InputStreamReader tempReader = new InputStreamReader(new BufferedInputStream(p.getInputStream()));
                    BufferedReader reader = new BufferedReader(tempReader);

                    String output = new String("");

                    while (true){
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        output = output + line;
                    }
                    p.getInputStream().close();
                    s_logger.info("diskpart detahc command output: "+ output);
                }
                // wait for a while so that disk formatting is done
                Thread.sleep(60000);

                //create boot args on the disk
                String bootArgs = vmSpec.getBootArgs();
                String Drive = vmName.substring(0,1);
                File fBootargs =new File(Drive+":\\cmdline");
                FileOutputStream fopBoot=new FileOutputStream(fBootargs);
                fopBoot.write(bootArgs.toString().getBytes());
                fopBoot.flush();
                fopBoot.close();

                //detach the boot parameter disk
                {
                    String diskpartDetachScriptName = vmName+"-Detach-Diskpart.txt";

                    StringBuilder cmdDetachDiskpart = new StringBuilder("select vdisk file=\"");
                    cmdDetachDiskpart.append(bootArgsDiskPath.toCharArray());cmdDetachDiskpart.append("\"" + newLine);
                    cmdDetachDiskpart.append("detach vdisk" + newLine);

                    File fd=new File(diskpartDetachScriptName);
                    FileOutputStream fdop=new FileOutputStream(fd);
                    fdop.write(cmdDetachDiskpart.toString().getBytes());
                    fdop.flush();
                    fdop.close();

                    s_logger.info("Running diskpart detach command");
                    Process pd = Runtime.getRuntime().exec("cmd.exe /c diskpart.exe /s "+diskpartDetachScriptName);
                    pd.getOutputStream().close();
                    InputStreamReader temperrReader1 = new InputStreamReader(new BufferedInputStream(pd.getErrorStream()));
                    BufferedReader errreader1 = new BufferedReader(temperrReader1);

                    if (errreader1.ready()) {
                        String errorOutput = new String("");
                        while (true){
                            String errline = errreader1.readLine();
                            if (errline == null) {
                                break;
                            }
                            errorOutput = errorOutput + errline;
                        }
                        s_logger.info("errors found while running diskpart detach command: " + errorOutput);
                    }

                    pd.getErrorStream().close();
                    InputStreamReader tempReader1 = new InputStreamReader(new BufferedInputStream(pd.getInputStream()));
                    BufferedReader reader1 = new BufferedReader(tempReader1);

                    String output1 = new String("");

                    while (true){
                        String line = reader1.readLine();
                        if (line == null) {
                            break;
                        }
                        output1 = output1 + line;
                    }

                    pd.getInputStream().close();
                    s_logger.info("diskpart detach command output: "+ output1);
                }
            }

            UUID id = UUID.randomUUID();
            String hwProfileId = id.toString();

            StringBuilder cmdStr = new StringBuilder("Add-PSSnapin Microsoft.SystemCenter.VirtualMachineManager;" + newLine);
            cmdStr.append("Get-VMMServer -ComputerName localhost;" + newLine);
            cmdStr.append("$JobGroupId = [Guid]::NewGuid().ToString();" + newLine);
            cmdStr.append("$hwProfileId = [Guid]::NewGuid().ToString(); " + newLine);
            cmdStr.append("$CPUType = Get-CPUType -VMMServer localhost | where {$_.Name -eq " +  "'1.20 GHz Athlon MP'}; " + newLine);
            cmdStr.append("$ISO =  Get-ISO -VMMServer localhost | where { $_.Name -match  \"systemvm\" }; " + newLine);
            cmdStr.append("$VMHost = Get-VMHost -VMMServer localhost | where {$_.Name -eq \"HYPERVHOST.hypervdc.intranet.lab.vmops.com\"}; " + newLine);
            cmdStr.append("$VNetwork = Get-VirtualNetwork  -VMHost $VMHost  -Name \"public\"; " + newLine);
            cmdStr.append("New-VirtualNetworkAdapter -VMMServer localhost -JobGroup $JobGroupID -PhysicalAddressType Dynamic -VirtualNetwork $vnetwork; " + newLine);
            cmdStr.append("New-VirtualNetworkAdapter -VMMServer localhost -JobGroup $JobGroupID -PhysicalAddressType Dynamic -VirtualNetwork $vnetwork; " + newLine);
            cmdStr.append("New-VirtualNetworkAdapter -VMMServer localhost -JobGroup $JobGroupID -PhysicalAddressType Dynamic -VirtualNetwork $vnetwork; " + newLine);
            cmdStr.append("New-VirtualDVDDrive -VMMServer localhost -JobGroup $JobGroupID -Bus 1 -LUN 0 -ISO $ISO ; " + newLine);
            cmdStr.append("New-HardwareProfile -VMMServer localhost -JobGroup $JobGroupID -Owner \"HYPERVDC\\Administrator\" -CPUType $CPUType  -Name $hwProfileId");
            cmdStr.append(" -Description \"Profile used to create a VM/Template\"" +
                    " -CPUCount 1 -MemoryMB 512 -RelativeWeight 100 -HighlyAvailable $true -NumLock $false -BootOrder \"CD\", " +
                    "\"IdeHardDrive\", \"PxeBoot\", \"Floppy\" -LimitCPUFunctionality $false -LimitCPUForMigration $false; " + newLine);

            cmdStr.append("$JobGroupId = [Guid]::NewGuid().ToString(); " + newLine);

            //refresh library share
            cmdStr.append("$share = Get-LibraryShare;"+newLine);
            cmdStr.append("Refresh-LibraryShare -LibraryShare $share;"+newLine);

            // create root disk
            cmdStr.append("$VirtualHardDisk1 = Get-VirtualHardDisk -VMMServer localhost | where {$_.Location -eq \"\\\\scvmm.hypervdc.intranet.lab.vmops.com\\MSSCVMMLibrary\\VHDs\\systemvm.vhd\"} | where {$_.HostName -eq \"scvmm.hypervdc.intranet.lab.vmops.com\"}" + newLine);
            cmdStr.append("New-VirtualDiskDrive -VMMServer localhost -JobGroup $JobGroupID -IDE -Bus 0 -LUN 0 -VirtualHardDisk $VirtualHardDisk1 -Filename \"");
            cmdStr.append(vmName.toCharArray());
            cmdStr.append("-systemvm.vhd\"; " + newLine);

            // create boot param data disk
            cmdStr.append("$VirtualHardDisk2 = Get-VirtualHardDisk -VMMServer localhost " +
            " | where {$_.Location -eq \"\\\\scvmm.hypervdc.intranet.lab.vmops.com\\MSSCVMMLibrary\\VHDs\\");
            cmdStr.append(bootArgsDiskName.toCharArray());
            cmdStr.append("\" } | where {$_.HostName -eq \"scvmm.hypervdc.intranet.lab.vmops.com\"}" + newLine);
            cmdStr.append("New-VirtualDiskDrive -VMMServer localhost -JobGroup $JobGroupID -IDE -Bus 0 -LUN 1 -VirtualHardDisk $VirtualHardDisk2 -Filename \"");
            cmdStr.append(bootArgsDiskName.toCharArray());
            cmdStr.append("\";"+newLine);

            cmdStr.append("$HardwareProfile = Get-HardwareProfile -VMMServer localhost | where {$_.Name -eq  $hwProfileId};" + newLine);
            cmdStr.append("$OperatingSystem = Get-OperatingSystem -VMMServer localhost | where {$_.Name -eq 'Other Linux (32 bit)'};" + newLine);

            cmdStr.append("New-VM -VMMServer localhost -Name \"");
            cmdStr.append(vmName.toCharArray());

            cmdStr.append("\" -Description \"\" -Owner \"HYPERVDC\\Administrator\" -VMHost $VMHost -Path \"C:\\ClusterStorage\\Volume1\" -HardwareProfile $HardwareProfile " +
                    " -JobGroup $JobGroupID" +
                    " -OperatingSystem $OperatingSystem -RunAsSystem -StartVM -StartAction NeverAutoTurnOnVM -StopAction SaveVM;" + newLine);

            File f=new File(scriptFileName);
            FileOutputStream fop=new FileOutputStream(f);
            fop.write(cmdStr.toString().getBytes());
            fop.flush();
            fop.close();
            s_logger.info("Running command: " + cmdStr);
            Process p = Runtime.getRuntime().exec("cmd.exe /c Powershell -Command \" & '.\\" + scriptFileName +"'\"");
            p.getOutputStream().close();

            InputStreamReader temperrReader = new InputStreamReader(new BufferedInputStream(p.getErrorStream()));
            BufferedReader errreader = new BufferedReader(temperrReader);

            if (errreader.ready()) {
                String errorOutput = new String("");
                while (true){
                    String errline = errreader.readLine();
                    if (errline == null) {
                        break;
                    }
                    errorOutput = errorOutput + errline;
                }
                s_logger.info("errors found while running cmdlet to create VM: " + errorOutput);
            }

            p.getErrorStream().close();
            InputStreamReader tempReader = new InputStreamReader(new BufferedInputStream(p.getInputStream()));
            BufferedReader reader = new BufferedReader(tempReader);

            String output = new String("");

            while (true){
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                output = output + line;
            }
            p.getInputStream().close();

            s_logger.info("vm create cmmdlet output: "+ output);

            if (output.contains("FullyQualifiedErrorId") || output.contains("Error") || output.contains("Exception")) {
                s_logger.info("No errors found in running cmdlet "+ cmdStr.toString());
                return new StartAnswer(cmd, "Failed to start VM");
            }

            state = State.Running;
            return new StartAnswer(cmd);
        } catch (Exception e){
            return new StartAnswer(cmd, "Failed to start VM");
        } finally {
            //delete the PS script file
            //File f=new File(".\\"+scriptFileName);
            //f.delete();
            synchronized (_vms) {
                if (state != State.Stopped) {
                    _vms.put(vmName, state);
                } else {
                    _vms.remove(vmName);
                }
            }
        }
    }

    protected Answer execute(DhcpEntryCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource DhcpEntryCommand: " + _gson.toJson(cmd));
        }

        String args = " " + cmd.getVmMac();
        args += " " + cmd.getVmIpAddress();
        args += " " + cmd.getVmName();
        if (cmd.getDefaultRouter() != null) {
            args += " " + cmd.getDefaultRouter();
            args += " " + cmd.getDefaultDns();
            args += " " + cmd.getStaticRoutes();
        }


        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Run command on domR " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP) + ", /root/edithosts.sh " + args);
        }

        try {
            Pair<Boolean, String> result = SshHelper.sshExecute(cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP), DEFAULT_DOMR_SSHPORT, "root",
                    new File("id_rsa.cloud"), null, "/root/edithosts.sh " + args);

            if (!result.first()) {
                s_logger.error("dhcp_entry command on domR " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP)
                        + " failed, message: " + result.second());

                return new Answer(cmd, false, "DhcpEntry failed due to " + result.second());
            }

            if (s_logger.isInfoEnabled()) {
                s_logger.info("dhcp_entry command on domain router " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP) + " completed");
            }

        } catch (Throwable e) {
            s_logger.error("Unexpected exception ", e);
            return new Answer(cmd, false, "DhcpEntry failed due to exception");
        }

        return new Answer(cmd);
    }

    protected CheckSshAnswer execute(CheckSshCommand cmd) {
        String vmName = cmd.getName();
        String privateIp = cmd.getIp();
        int cmdPort = cmd.getPort();

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Ping VM:" + cmd.getName() + " IP:" + privateIp + " port:" + cmdPort);
        }
        return new CheckSshAnswer(cmd);
    }

    @Override
    public void setAgentControl(IAgentControl agentControl) {
        this.agentControl = agentControl;
    }

    @Override
    public Type getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StartupCommand[] initialize() {

        s_logger.info("recieved initialize request for cluster:" + _clusterId);
        List<String> vmHostList = getHostsInCluster(_clusterId);

        if (vmHostList.size() == 0) {
            s_logger.info("cluster is not recognized or zero instances in the cluster");
        }

        StartupCommand[] answerCmds = new StartupCommand[vmHostList.size()];

        int index =0;
        for (String hostName: vmHostList) {
            s_logger.info("Node :" + hostName);
            StartupRoutingCommand cmd = new StartupRoutingCommand();
            answerCmds[index] = cmd;
            fillHostInfo(cmd,hostName);
            index++;
        }

        s_logger.info("response sent to initialize request for cluster:" + _clusterId);
        return answerCmds;
    }

    protected void fillHostInfo(StartupRoutingCommand cmd, String hostName) {

        Map<String, String> details = cmd.getHostDetails();
        if (details == null) {
            details = new HashMap<String, String>();
        }

        try {
            fillHostHardwareInfo(cmd);
            fillHostNetworkInfo(cmd);
            fillHostDetailsInfo(details);
        } catch (Exception e) {
            s_logger.error("Exception while retrieving host info ", e);
            throw new CloudRuntimeException("Exception while retrieving host info");
        }

        cmd.setName(hostName);
        cmd.setHostDetails(details);
        cmd.setGuid(_guid);
        cmd.setDataCenter(_dcId);
        cmd.setPod(_podId);
        cmd.setCluster(_clusterId);
        cmd.setHypervisorType(HypervisorType.Hyperv);
    }

    private void fillHostDetailsInfo(Map<String, String> details) throws Exception {

    }

    private Answer execute(GetHostStatsCommand cmd) {

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource GetHostStatsCommand: " + _gson.toJson(cmd));
        }

        try {
            // FIXME: get the actual host stats by running powershell cmdlet. This is just for prototype.
            HostStatsEntry hostStats = new HostStatsEntry(cmd.getHostId(), 0, 10000, 10000,
                    "host", 2*1024*1024, 1*1024*1024, 1*1024*1024, 0);
            s_logger.info("returning stats :" + 2*1024*1024 + " " + 1*1024*1024 + " " + 1*1024*1024);
            return new GetHostStatsAnswer(cmd, hostStats);
        } catch (Exception e) {
            HostStatsEntry hostStats = new HostStatsEntry(cmd.getHostId(), 0, 0, 0,
                    "host", 0, 0, 0, 0);
            String msg = "Unable to execute GetHostStatsCommand due to exception " + e.getMessage();
            s_logger.error(msg, e);
            return new GetHostStatsAnswer(cmd, hostStats);
        }
    }

    protected Answer execute(ModifyStoragePoolCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing resource ModifyStoragePoolCommand: " + _gson.toJson(cmd));
        }

        try {
            StorageFilerTO pool = cmd.getPool();
            s_logger.info("Primary storage pool  details: " + pool.getHost() + " " + pool.getPath());
            Map<String, TemplateInfo> tInfo = new HashMap<String, TemplateInfo>();
            // FIXME: get the actual storage capacity and storage stats of CSV volume
            // by running powershell cmdlet. This hardcoding just for prototype.
            ModifyStoragePoolAnswer answer = new ModifyStoragePoolAnswer(cmd,
                    1024*1024*1024*1024L, 512*1024*1024*1024L, tInfo);

            return answer;
        } catch (Throwable e) {
            return new Answer(cmd, false, "Unable to execute ModifyStoragePoolCommand due to exception " + e.getMessage());
        }
    }

    protected Answer execute(GetStorageStatsCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing GetStorageStatsCommand command: " + _gson.toJson(cmd));
        }
        // FIXME: get the actual storage capacity and storage stats of CSV volume
        return new GetStorageStatsAnswer(cmd, 1024*1024*1024*1024L, 512*1024*1024*1024L);
    }

    private void fillHostHardwareInfo(StartupRoutingCommand cmd) throws RemoteException {
        try {
            // FIXME: get the actual host capacity by running cmdlet.This hardcoding just for prototype
            cmd.setCaps("hvm");
            cmd.setDom0MinMemory(0);
            cmd.setSpeed(100000);
            cmd.setCpus(6);
            long ram = new Long("211642163904");
            cmd.setMemory(ram);
        } catch (Throwable e) {
            s_logger.error("Unable to query host network info due to exception ", e);
            throw new CloudRuntimeException("Unable to query host network info due to exception");
        }
    }

    private void fillHostNetworkInfo(StartupRoutingCommand cmd) throws RemoteException {
        try {
            // FIXME: get the actual host and storage IP by running cmdlet.This hardcoding just for prototype
            cmd.setPrivateIpAddress("192.168.154.236");
            cmd.setPrivateNetmask("255.255.255.0");
            cmd.setPrivateMacAddress("00:16:3e:77:e2:a0");

            cmd.setStorageIpAddress("192.168.154.36");
            cmd.setStorageNetmask("255.255.255.0");
            cmd.setStorageMacAddress("00:16:3e:77:e2:a0");
        } catch (Throwable e) {
            s_logger.error("Unable to query host network info due to exception ", e);
            throw new CloudRuntimeException("Unable to query host network info due to exception");
        }
    }

    private List <String> getHostsInCluster(String clusterName)
    {
        List<String> hypervHosts = new ArrayList<String>();

        try {
            //StringBuilder cmd = new StringBuilder("cmd /c powershell.exe -OutputFormat XML ");
            StringBuilder cmd = new StringBuilder("cmd /c powershell.exe ");
            cmd.append("-Command Add-PSSnapin Microsoft.SystemCenter.VirtualMachineManager; ");
            cmd.append("Get-VMMServer -ComputerName localhost; ");
            cmd.append("Get-VMHostCluster ");
            cmd.append(clusterName.toCharArray());

            Process p = Runtime.getRuntime().exec(cmd.toString());
            p.getOutputStream().close();

            InputStreamReader temperrReader = new InputStreamReader(new BufferedInputStream(p.getErrorStream()));
            BufferedReader errreader = new BufferedReader(temperrReader);
            if (errreader.ready()) {
                String errorOutput = new String("");
                s_logger.info("errors found while running cmdlet Get-VMHostCluster");
                while (true){
                    String errline = errreader.readLine();
                    if (errline == null) {
                        break;
                    }
                    errorOutput = errorOutput + errline;
                }
                s_logger.info(errorOutput);
            } else {
                s_logger.info("No errors found in running cmdlet:" + cmd);
            }

            p.getErrorStream().close();
            /*
            InputStream in = (InputStream) p.getInputStream();
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader parser = factory.createXMLStreamReader(in);

            while(parser.hasNext()) {

                  int eventType = parser.next();
                  switch (eventType) {

                       case START_ELEMENT:
                       //  Do something
                       break;
                       case END_ELEMENT:
                       //  Do something
                       break;
                       //  And so on ...
                  }
            }
            parser.close();
             */

            InputStreamReader tempReader = new InputStreamReader(new BufferedInputStream(p.getInputStream()));
            BufferedReader reader = new BufferedReader(tempReader);

            String output = new String("");

            while (true){
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                output = output + line;
            }

            String nodesListStr = output.substring(output.indexOf("Nodes"));
            nodesListStr = nodesListStr.substring(nodesListStr.indexOf('{', 0)+1, nodesListStr.indexOf('}', 0));
            String[] nodesList =  nodesListStr.split(",");

            for (String node : nodesList) {
                hypervHosts.add(node);
            }

            p.getInputStream().close();
        } catch (Exception e)
        {
            s_logger.info("Exception caught: "+e.getMessage());
        }
        return hypervHosts;
    }

    protected HashMap<String, State> sync() {
        HashMap<String, State> changes = new HashMap<String, State>();

        try {
            synchronized (_vms) {
            }

        } catch (Throwable e) {
            s_logger.error("Unable to perform sync information collection process at this point due to exception ", e);
            return null;
        }
        return changes;
    }

    @Override
    public PingCommand getCurrentStatus(long id) {
        HashMap<String, State> newStates = sync();
        if (newStates == null) {
            newStates = new HashMap<String, State>();
        }
        PingRoutingCommand cmd = new PingRoutingCommand(com.cloud.host.Host.Type.Routing, id, newStates);
        return cmd;
    }

    @Override
    public boolean configure(String name, Map<String, Object> params)
    throws ConfigurationException {

        _dcId = params.get("zone").toString();
        _podId= params.get("pod").toString();
        _clusterId = params.get("cluster").toString();
        _guid = params.get("guid").toString();

        boolean success = super.configure(name, params);
        if (! success) {
            return false;
        }
        return true;
    }

    @Override
    protected String getDefaultScriptsDir() {
        // TODO Auto-generated method stub
        return null;
    }
}
