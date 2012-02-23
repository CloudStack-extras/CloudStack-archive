/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
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

package com.cloud.hypervisor.xen.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckOnHostAnswer;
import com.cloud.agent.api.CheckOnHostCommand;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.FenceAnswer;
import com.cloud.agent.api.FenceCommand;
import com.cloud.agent.api.NetworkUsageAnswer;
import com.cloud.agent.api.NetworkUsageCommand;
import com.cloud.agent.api.PoolEjectCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.resource.ServerResource;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;
import com.xensource.xenapi.Bond;
import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Network;
import com.xensource.xenapi.PBD;
import com.xensource.xenapi.PIF;
import com.xensource.xenapi.SR;
import com.xensource.xenapi.Types;
import com.xensource.xenapi.Types.IpConfigurationMode;
import com.xensource.xenapi.Types.XenAPIException;
import com.xensource.xenapi.VLAN;
import com.xensource.xenapi.VM;

@Local(value = ServerResource.class)
public class XenServer56Resource extends CitrixResourceBase {
    private final static Logger s_logger = Logger.getLogger(XenServer56Resource.class);

    @Override
    public Answer executeRequest(Command cmd) {
        if (cmd instanceof FenceCommand) {
            return execute((FenceCommand) cmd);
        } else if (cmd instanceof PoolEjectCommand) {
            return execute((PoolEjectCommand) cmd);
        } else if (cmd instanceof NetworkUsageCommand) {
            return execute((NetworkUsageCommand) cmd);
        } else {
            return super.executeRequest(cmd);
        }
    }
    
    @Override
    protected void setMemory(Connection conn, VM vm, long memsize) throws XmlRpcException, XenAPIException {
        vm.setMemoryLimits(conn, memsize, memsize, memsize, memsize);
    }
    

    @Override
    protected String getGuestOsType(String stdType, boolean bootFromCD) {
        return CitrixHelper.getXenServerGuestOsType(stdType, bootFromCD);
    }


    @Override
    protected List<File> getPatchFiles() {
        List<File> files = new ArrayList<File>();
        String patch = "scripts/vm/hypervisor/xenserver/xenserver56/patch";
        String patchfilePath = Script.findScript("", patch);
        if (patchfilePath == null) {
            throw new CloudRuntimeException("Unable to find patch file " + patch);
        }
        File file = new File(patchfilePath);
        files.add(file);

        return files;
    }

    @Override
    protected void disableVlanNetwork(Connection conn, Network network) {
        try {
            Network.Record networkr = network.getRecord(conn);
            if (!networkr.nameLabel.startsWith("VLAN")) {
                return;
            }
            String bridge = networkr.bridge.trim();
            for (PIF pif : networkr.PIFs) {
                PIF.Record pifr = pif.getRecord(conn);
                if (!pifr.host.getUuid(conn).equalsIgnoreCase(_host.uuid)) {
                    continue;
                }
                
                VLAN vlan = pifr.VLANMasterOf;
                if (vlan != null) {
                    String vlannum = pifr.VLAN.toString();
                    String device = pifr.device.trim();
                    if (vlannum.equals("-1")) {
                        return;
                    }
                    try {
                        vlan.destroy(conn);
                        Host host = Host.getByUuid(conn, _host.uuid);
                        host.forgetDataSourceArchives(conn, "pif_" + bridge + "_tx");
                        host.forgetDataSourceArchives(conn, "pif_" + bridge + "_rx");
                        host.forgetDataSourceArchives(conn, "pif_" + device + "." + vlannum + "_tx");
                        host.forgetDataSourceArchives(conn, "pif_" + device + "." + vlannum + "_rx");
                    } catch (XenAPIException e) {
                        s_logger.info("Catch " + e.getClass().getName() + ": failed to destory VLAN " + device + " on host " + _host.uuid
                                + " due to "  + e.toString());
                    }
                }
                return;
            }
        } catch (XenAPIException e) {
            String msg = "Unable to disable VLAN network due to " + e.toString();
            s_logger.warn(msg, e);
        } catch (Exception e) {
            String msg = "Unable to disable VLAN network due to " + e.getMessage();
            s_logger.warn(msg, e);
        }
    }

    @Override
    protected String networkUsage(Connection conn, final String privateIpAddress, final String option, final String vif) {
        String args = null;
        if (option.equals("get")) {
            args = "-g";
        } else if (option.equals("create")) {
            args = "-c";
        } else if (option.equals("reset")) {
            args = "-r";
        } else if (option.equals("addVif")) {
            args = "-a";
            args += vif;
        } else if (option.equals("deleteVif")) {
            args = "-d";
            args += vif;
        }

        args += " -i ";
        args += privateIpAddress;
        return callHostPlugin(conn, "vmops", "networkUsage", "args", args);
    }

    protected NetworkUsageAnswer execute(NetworkUsageCommand cmd) {
        try {
            Connection conn = getConnection();
            if(cmd.getOption()!=null && cmd.getOption().equals("create") ){
                String result = networkUsage(conn, cmd.getPrivateIP(), "create", null);
                NetworkUsageAnswer answer = new NetworkUsageAnswer(cmd, result, 0L, 0L);
                return answer;
            }
            long[] stats = getNetworkStats(conn, cmd.getPrivateIP());
            NetworkUsageAnswer answer = new NetworkUsageAnswer(cmd, "", stats[0], stats[1]);
            return answer;
        } catch (Exception ex) {
            s_logger.warn("Failed to get network usage stats due to ", ex);
            return new NetworkUsageAnswer(cmd, ex); 
        }
    }

    @Override
    protected Answer execute(PoolEjectCommand cmd) {
        Connection conn = getConnection();
        String hostuuid = cmd.getHostuuid();
        try {
            Host host = Host.getByUuid(conn, hostuuid);
            // remove all tags cloud stack add before eject
            Host.Record hr = host.getRecord(conn);
            Iterator<String> it = hr.tags.iterator();
            while (it.hasNext()) {
                String tag = it.next();
                if (tag.contains("cloud-heartbeat-")) {
                    it.remove();
                }
            }
            return super.execute(cmd);

        } catch (XenAPIException e) {
            String msg = "Unable to eject host " + _host.uuid + " due to " + e.toString();
            s_logger.warn(msg, e);
            return new Answer(cmd, false, msg);
        } catch (Exception e) {
            s_logger.warn("Unable to eject host " + _host.uuid, e);
            String msg = "Unable to eject host " + _host.uuid + " due to " + e.getMessage();
            s_logger.warn(msg, e);
            return new Answer(cmd, false, msg);
        }

    }

    protected FenceAnswer execute(FenceCommand cmd) {
        Connection conn = getConnection();
        try {
            String result = callHostPluginPremium(conn, "check_heartbeat", "host", cmd.getHostGuid(), "interval",
                    Integer.toString(_heartbeatInterval * 2));
            if (!result.contains("> DEAD <")) {
                s_logger.debug("Heart beat is still going so unable to fence");
                return new FenceAnswer(cmd, false, "Heartbeat is still going on unable to fence");
            }

            Set<VM> vms = VM.getByNameLabel(conn, cmd.getVmName());
            for (VM vm : vms) {
                synchronized (_cluster.intern()) {
                    s_vms.remove(_cluster, _name, vm.getNameLabel(conn));
                }
                s_logger.info("Fence command for VM " + cmd.getVmName());
                vm.powerStateReset(conn);
                vm.destroy(conn);
            }
            return new FenceAnswer(cmd);
        } catch (XmlRpcException e) {
            s_logger.warn("Unable to fence", e);
            return new FenceAnswer(cmd, false, e.getMessage());
        } catch (XenAPIException e) {
            s_logger.warn("Unable to fence", e);
            return new FenceAnswer(cmd, false, e.getMessage());
        }
    }

    @Override
    protected boolean transferManagementNetwork(Connection conn, Host host, PIF src, PIF.Record spr, PIF dest)
            throws XmlRpcException, XenAPIException {
        dest.reconfigureIp(conn, spr.ipConfigurationMode, spr.IP, spr.netmask, spr.gateway, spr.DNS);
        Host.managementReconfigure(conn, dest);
        String hostUuid = null;
        int count = 0;
        while (count < 10) {
            try {
                Thread.sleep(10000);
                hostUuid = host.getUuid(conn);
                if (hostUuid != null) {
                    break;
                }
            } catch (XmlRpcException e) {
                s_logger.debug("Waiting for host to come back: " + e.getMessage());
            } catch (XenAPIException e) {
                s_logger.debug("Waiting for host to come back: " + e.getMessage());
            } catch (InterruptedException e) {
                s_logger.debug("Gotta run");
                return false;
            }
        }
        if (hostUuid == null) {
            s_logger.warn("Unable to transfer the management network from " + spr.uuid);
            return false;
        }

        src.reconfigureIp(conn, IpConfigurationMode.NONE, null, null, null, null);
        return true;
    }
    
    @Override
    public StartupCommand[] initialize() {
        pingXenServer();
        StartupCommand[] cmds = super.initialize();
        return cmds;
    }

    @Override
    protected CheckOnHostAnswer execute(CheckOnHostCommand cmd) {
        try {
            Connection conn = getConnection();
            String result = callHostPluginPremium(conn, "check_heartbeat", "host", cmd.getHost().getGuid(), "interval",
                    Integer.toString(_heartbeatInterval * 2));
            if (result == null) {
                return new CheckOnHostAnswer(cmd, "Unable to call plugin");
            }
            if (result.contains("> DEAD <")) {
                s_logger.debug("Heart beat is gone so dead.");
                return new CheckOnHostAnswer(cmd, false, "Heart Beat is done");
            } else if (result.contains("> ALIVE <")) {
                s_logger.debug("Heart beat is still going");
                return new CheckOnHostAnswer(cmd, true, "Heartbeat is still going");
            }
            return new CheckOnHostAnswer(cmd, null, "Unable to determine");
        } catch (Exception e) {
            s_logger.warn("Unable to fence", e);
            return new CheckOnHostAnswer(cmd, e.getMessage());
        }
    }

    public XenServer56Resource() {
        super();
    }

}
