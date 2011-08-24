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


package com.cloud.agent.resource.computing;


import java.net.InetAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;



import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartAnswer;
import com.cloud.agent.api.StartCommand;

import com.cloud.agent.api.StopAnswer;
import com.cloud.agent.api.StopCommand;

import com.cloud.agent.api.routing.SavePasswordCommand;
import com.cloud.agent.api.routing.VmDataCommand;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.dhcp.DhcpSnooper;
import com.cloud.agent.dhcp.DhcpSnooperImpl;


import com.cloud.agent.resource.computing.LibvirtComputingResource;
import com.cloud.agent.resource.computing.LibvirtConnection;
import com.cloud.agent.resource.computing.LibvirtVMDef;
import com.cloud.agent.resource.computing.LibvirtVMDef.DiskDef;
import com.cloud.agent.resource.computing.LibvirtVMDef.InterfaceDef;
import com.cloud.agent.vmdata.JettyVmDataServer;
import com.cloud.agent.vmdata.VmDataServer;


import com.cloud.network.Networks.TrafficType;
import com.cloud.utils.Pair;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.State;

/**
 * Logic specific to the Cloudzones feature
 * 
 *  }
 **/


public class CloudZonesComputingResource extends LibvirtComputingResource {
    private static final Logger s_logger = Logger.getLogger(CloudZonesComputingResource.class);
    protected DhcpSnooper _dhcpSnooper;
    String _parent;
    long _dhcpTimeout;
    protected String _hostIp;
    protected String _hostMacAddress;
    protected VmDataServer _vmDataServer = new JettyVmDataServer();

    
    private void setupDhcpManager(Connect conn, String bridgeName) {

        _dhcpSnooper = new DhcpSnooperImpl(bridgeName, _dhcpTimeout);

        List<Pair<String, String>> macs = new ArrayList<Pair<String, String>>();
        try {
            int[] domainIds = conn.listDomains();
            for (int i = 0; i < domainIds.length; i++) {
                Domain vm = conn.domainLookupByID(domainIds[i]);
                if (vm.getName().startsWith("i-")) {
                    List<InterfaceDef> nics = getInterfaces(conn, vm.getName());
                    InterfaceDef nic = nics.get(0);
                    macs.add(new Pair<String, String>(nic.getMacAddress(), vm.getName()));
                }
            }
        } catch (LibvirtException e) {
            s_logger.debug("Failed to get MACs: " + e.toString());
        }
        
        _dhcpSnooper.initializeMacTable(macs);
    }
    
    @Override
    public boolean configure(String name, Map<String, Object> params)
            throws ConfigurationException {
        boolean success = super.configure(name, params);
        if (! success) {
            return false;
        }

        _parent = (String)params.get("mount.path");
        
        try {
            _dhcpTimeout =  Long.parseLong((String)params.get("dhcp.timeout"));
        } catch (Exception e) {
            _dhcpTimeout = 1200000;
        }
        
        _hostIp = (String)params.get("host.ip");
        _hostMacAddress = (String)params.get("host.mac.address");
        
        try {
            Connect conn;
            conn = LibvirtConnection.getConnection();
            setupDhcpManager(conn, _guestBridgeName);
        } catch (LibvirtException e) {
            s_logger.debug("Failed to get libvirt connection:" + e.toString());
            return false;
        }
        
        _dhcpSnooper.configure(name, params);
        _vmDataServer.configure(name, params);
       
        return true;
    }
   
    @Override
    protected synchronized StartAnswer execute(StartCommand cmd) {
        VirtualMachineTO vmSpec = cmd.getVirtualMachine();
        String vmName = vmSpec.getName();
        LibvirtVMDef vm = null;
        
        State state = State.Stopped;
        Connect conn = null;
        try {
            conn = LibvirtConnection.getConnection();
            synchronized (_vms) {
                _vms.put(vmName, State.Starting);
            }

            vm = createVMFromSpec(vmSpec);

            createVbd(conn, vmSpec, vmName, vm);
            
            createVifs(conn, vmSpec, vm);

            s_logger.debug("starting " + vmName + ": " + vm.toString());
            startDomain(conn, vmName, vm.toString());
            

            NicTO[] nics = vmSpec.getNics();
            for (NicTO nic : nics) {
                if (nic.isSecurityGroupEnabled()) {
                    if (vmSpec.getType() != VirtualMachine.Type.User) {
                        default_network_rules_for_systemvm(conn, vmName);
                    } else {
                        nic.setIp(null);
                        default_network_rules(conn, vmName, nic, vmSpec.getId());
                    }
                }
            }

            // Attach each data volume to the VM, if there is a deferred attached disk
            for (DiskDef disk : vm.getDevices().getDisks()) {
                if (disk.isAttachDeferred()) {
                    attachOrDetachDisk(conn, true, vmName, disk.getDiskPath(), disk.getDiskSeq());
                }
            }
            
            if (vmSpec.getType() == VirtualMachine.Type.User) {
                for (NicTO nic: nics) {
                    if (nic.getType() == TrafficType.Guest) {
                        InetAddress ipAddr = _dhcpSnooper.getIPAddr(nic.getMac(), vmName);
                        if (ipAddr == null) {
                            s_logger.debug("Failed to get guest DHCP ip, stop it");
                            StopCommand stpCmd = new StopCommand(vmName);
                            execute(stpCmd);
                            return new StartAnswer(cmd, "Failed to get guest DHCP ip, stop it");
                        }
                        s_logger.debug(ipAddr);
                        nic.setIp(ipAddr.getHostAddress());
                        
                        post_default_network_rules(conn, vmName, nic, vmSpec.getId(), _dhcpSnooper.getDhcpServerIP(), _hostIp, _hostMacAddress);
                        _vmDataServer.handleVmStarted(cmd.getVirtualMachine());
                    }
                }
            }
            
            state = State.Running;
            return new StartAnswer(cmd);
        } catch (Exception e) {
            s_logger.warn("Exception ", e);
            if (conn != null) {
                handleVmStartFailure(conn, vmName, vm);
            }
            return new StartAnswer(cmd, e.getMessage());
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
    
    protected Answer execute(StopCommand cmd) {
        final String vmName = cmd.getVmName();
        
        Long bytesReceived = new Long(0);
        Long bytesSent = new Long(0);
        
        State state = null;
        synchronized(_vms) {
            state = _vms.get(vmName);
            _vms.put(vmName, State.Stopping);
        }
        try {
            Connect conn = LibvirtConnection.getConnection();
            
            try {
                Domain dm =  conn.domainLookupByUUID(UUID.nameUUIDFromBytes(vmName.getBytes()));
            } catch (LibvirtException e) {
                state = State.Stopped;
                return new StopAnswer(cmd, null, 0, bytesSent, bytesReceived);
            }
            
            String macAddress = null;
            if (vmName.startsWith("i-")) {
                List<InterfaceDef> nics = getInterfaces(conn, vmName);
                if (!nics.isEmpty()) {
                    macAddress = nics.get(0).getMacAddress(); 
                }
            }
            
            destroy_network_rules_for_vm(conn, vmName);
            String result = stopVM(conn, vmName, defineOps.UNDEFINE_VM);
            
            try {
                cleanupVnet(conn, cmd.getVnet());
                _dhcpSnooper.cleanup(macAddress, vmName);
                _vmDataServer.handleVmStopped(cmd.getVmName());
            } catch (Exception e) {

            }
            
            state = State.Stopped;
            return new StopAnswer(cmd, result, 0, bytesSent, bytesReceived);
        } catch (LibvirtException e) {
            return new StopAnswer(cmd, e.getMessage());
        } finally {
            synchronized(_vms) {
                if (state != null) {
                    _vms.put(vmName, state);
                } else {
                    _vms.remove(vmName);
                }
            }
        }
    }
    
    @Override
    public Answer executeRequest(Command cmd) {
        if (cmd instanceof VmDataCommand) {
            return execute((VmDataCommand)cmd);
        } else if (cmd instanceof SavePasswordCommand) {
            return execute ((SavePasswordCommand)cmd);
        }
        return super.executeRequest(cmd);
    }

    protected Answer execute(final VmDataCommand cmd) {
        return _vmDataServer.handleVmDataCommand(cmd);
    }
    
    protected Answer execute(final SavePasswordCommand cmd) {
        return new Answer(cmd);
    }

}
