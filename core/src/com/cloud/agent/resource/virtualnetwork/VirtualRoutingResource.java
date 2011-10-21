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
package com.cloud.agent.resource.virtualnetwork;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.BumpUpPriorityCommand;
import com.cloud.agent.api.CheckRouterAnswer;
import com.cloud.agent.api.CheckRouterCommand;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.proxy.CheckConsoleProxyLoadCommand;
import com.cloud.agent.api.proxy.ConsoleProxyLoadAnswer;
import com.cloud.agent.api.proxy.WatchConsoleProxyLoadCommand;
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
import com.cloud.agent.api.to.IpAddressTO;
import com.cloud.agent.api.to.PortForwardingRuleTO;
import com.cloud.agent.api.to.StaticNatRuleTO;
import com.cloud.network.HAProxyConfigurator;
import com.cloud.network.LoadBalancerConfigurator;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.Manager;
import com.cloud.utils.net.NetUtils;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;

/**
 * VirtualNetworkResource controls and configures virtual networking
 *
 * @config
 * {@table
 *    || Param Name | Description | Values | Default ||
 *  }
 **/
@Local(value={VirtualRoutingResource.class})
public class VirtualRoutingResource implements Manager {
    private static final Logger s_logger = Logger.getLogger(VirtualRoutingResource.class);
    private String _savepasswordPath; 	// This script saves a random password to the DomR file system
    private String _ipassocPath;
    private String _publicIpAddress;
    private String _firewallPath;
    private String _loadbPath;
    private String _dhcpEntryPath;
    private String _vmDataPath;
    private String _publicEthIf;
    private String _privateEthIf;
    private String _getRouterStatusPath;
    private String _bumpUpPriorityPath;
    private String _l2tpVpnPath;


    private int _timeout;
    private int _startTimeout;
    private String _scriptsDir;
    private String _name;
    private int _sleep;
    private int _retry;
    private int _port;

    public Answer executeRequest(final Command cmd) {
        try {
            if (cmd instanceof SetPortForwardingRulesCommand ) {
                return execute((SetPortForwardingRulesCommand)cmd);
            } else if (cmd instanceof SetStaticNatRulesCommand){
                return execute((SetStaticNatRulesCommand)cmd);
            }else if (cmd instanceof LoadBalancerConfigCommand) {
                return execute((LoadBalancerConfigCommand)cmd);
            } else if (cmd instanceof IpAssocCommand) {
                return execute((IpAssocCommand)cmd);
            } else if (cmd instanceof CheckConsoleProxyLoadCommand) {
                return execute((CheckConsoleProxyLoadCommand)cmd);
            } else if(cmd instanceof WatchConsoleProxyLoadCommand) {
                return execute((WatchConsoleProxyLoadCommand)cmd);
            }  else if (cmd instanceof SavePasswordCommand) {
                return execute((SavePasswordCommand)cmd);
            }  else if (cmd instanceof DhcpEntryCommand) {
                return execute((DhcpEntryCommand)cmd);
            } else if (cmd instanceof VmDataCommand) {
                return execute ((VmDataCommand)cmd);
            } else if (cmd instanceof CheckRouterCommand) {
                return execute ((CheckRouterCommand)cmd);
            } else if (cmd instanceof SetFirewallRulesCommand) {
                return execute((SetFirewallRulesCommand)cmd);
            } else if (cmd instanceof BumpUpPriorityCommand) {
                return execute((BumpUpPriorityCommand)cmd);
            } else if (cmd instanceof RemoteAccessVpnCfgCommand) {
                return execute((RemoteAccessVpnCfgCommand)cmd);
            } else if (cmd instanceof VpnUsersCfgCommand) {
                return execute((VpnUsersCfgCommand)cmd);
            }
            else {
                return Answer.createUnsupportedCommandAnswer(cmd);
            }
        } catch (final IllegalArgumentException e) {
            return new Answer(cmd, false, e.getMessage());
        }
    }

    private Answer execute(VpnUsersCfgCommand cmd) {
        for (VpnUsersCfgCommand.UsernamePassword userpwd: cmd.getUserpwds()) {
            Script command = new Script(_l2tpVpnPath, _timeout, s_logger);
            command.add(cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP));
            if (!userpwd.isAdd()) {
                command.add("-U ", userpwd.getUsername());
            } else {
                command.add("-u ", userpwd.getUsernamePassword());
            }
            String result = command.execute();
            if (result != null) {
                return new Answer(cmd, false, "Configure VPN user failed for user " + userpwd.getUsername());
            }
        }
        
        return new Answer(cmd);
    }

    private Answer execute(RemoteAccessVpnCfgCommand cmd) {
        Script command = new Script(_l2tpVpnPath, _timeout, s_logger);
        command.add(cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP));
        if (cmd.isCreate()) {
            command.add("-r ", cmd.getIpRange());
            command.add("-p ", cmd.getPresharedKey());
            command.add("-s ", cmd.getVpnServerIp());
            command.add("-l ", cmd.getLocalIp());
            command.add("-c ");
        } else {
            command.add("-d ");
            command.add("-s ", cmd.getVpnServerIp());
        }
        String result = command.execute();
        if (result != null) {
            return new Answer(cmd, false, "Configure VPN failed");
        }
        return new Answer(cmd);
    }

    private Answer execute(SetFirewallRulesCommand cmd) {
        String[] results = new String[cmd.getRules().length];
        for (int i =0; i < cmd.getRules().length; i++) {
            results[i] = "Failed";
        }
        String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);

        if (routerIp == null) {
            return new SetFirewallRulesAnswer(cmd, false, results);
        }

        String[][] rules = cmd.generateFwRules();
        final Script command = new Script(_firewallPath, _timeout, s_logger);
        command.add(routerIp);
        command.add("-F");
        
        StringBuilder sb = new StringBuilder();
        String[] fwRules = rules[0];
        if (fwRules.length > 0) {
            for (int i = 0; i < fwRules.length; i++) {
                sb.append(fwRules[i]).append(',');
            }
            command.add("-a", sb.toString());
        }
       
        String result = command.execute();
        if (result != null) {
            return new SetFirewallRulesAnswer(cmd, false, results);
        }
        return new SetFirewallRulesAnswer(cmd, true, null);
        
        
    }

    private Answer execute(SetPortForwardingRulesCommand cmd) {
        String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        String[] results = new String[cmd.getRules().length];
        int i = 0;
        
        boolean endResult = true;
        for (PortForwardingRuleTO rule : cmd.getRules()) {
            String result = null;
            final Script command = new Script(_firewallPath, _timeout, s_logger);
            
            command.add(routerIp);
            command.add(rule.revoked() ? "-D" : "-A");
            command.add("-P ", rule.getProtocol().toLowerCase());
            command.add("-l ", rule.getSrcIp());
            command.add("-p ", rule.getStringSrcPortRange());
            command.add("-r ", rule.getDstIp());
            command.add("-d ", rule.getStringDstPortRange());
            result = command.execute();
            if (result == null) {
                results[i++] = null;
            } else {
                results[i++] = "Failed";
                endResult = false;
            }
        }

        return new SetPortForwardingRulesAnswer(cmd, results, endResult);
    }
    
    private Answer execute(SetStaticNatRulesCommand cmd) {
        String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        String[] results = new String[cmd.getRules().length];
        int i = 0;
        boolean endResult = true;
        for (StaticNatRuleTO rule : cmd.getRules()) {
            String result = null;
            final Script command = new Script(_firewallPath, _timeout, s_logger);
            command.add(routerIp);
            command.add(rule.revoked() ? "-D" : "-A");
            
            //1:1 NAT needs instanceip;publicip;domrip;op
            command.add(" -l ", rule.getSrcIp());
            command.add(" -r ", rule.getDstIp());
            
            if (rule.getProtocol() != null) { 
                command.add(" -P ", rule.getProtocol().toLowerCase());
            }
            
            command.add(" -d ", rule.getStringSrcPortRange());
            command.add(" -G ") ;
            
            result = command.execute();
            if (result == null) {
                results[i++] = null;
            } else {
                results[i++] = "Failed";
                endResult = false;
            }
        }

        return new SetStaticNatRulesAnswer(cmd, results, endResult);
    }
    

    private Answer execute(LoadBalancerConfigCommand cmd) {
        String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        File tmpCfgFile = null;
        try {
            String cfgFilePath = "";
            LoadBalancerConfigurator cfgtr = new HAProxyConfigurator();
            String[] config;
            try {
                config = cfgtr.generateConfiguration(cmd);
            } catch ( Exception e) {
                return new Answer(cmd, false, e.getMessage());
            }
            String[][] rules = cfgtr.generateFwRules(cmd);
            if (routerIp != null) {
                tmpCfgFile = File.createTempFile(routerIp.replace('.', '_'), "cfg");
                final PrintWriter out
                = new PrintWriter(new BufferedWriter(new FileWriter(tmpCfgFile)));
                for (int i=0; i < config.length; i++) {
                    out.println(config[i]);
                }
                out.close();
                cfgFilePath = tmpCfgFile.getAbsolutePath();
            }

            final String result = setLoadBalancerConfig(cfgFilePath,
                    rules[LoadBalancerConfigurator.ADD],
                    rules[LoadBalancerConfigurator.REMOVE],
                    rules[LoadBalancerConfigurator.STATS],
                    routerIp);

            return new Answer(cmd, result == null, result);
        } catch (final IOException e) {
            return new Answer(cmd, false, e.getMessage());
        } finally {
            if (tmpCfgFile != null) {
                tmpCfgFile.delete();
            }
        }
    }

    protected Answer execute(VmDataCommand cmd) {
        List<String[]> vmData = cmd.getVmData();

        for (String[] vmDataEntry : vmData) {
            String folder = vmDataEntry[0];
            String file = vmDataEntry[1];
            String data = vmDataEntry[2];
            File tmpFile = null;

            byte[] dataBytes = null;
            if (data != null) {
                if (folder.equals("userdata")) {
                    dataBytes = Base64.decodeBase64(data);//userdata is supplied in url-safe unchunked mode
                } else {
                    dataBytes = data.getBytes();
                }
            }

            try {
                tmpFile = File.createTempFile("vmdata_", null);
                FileOutputStream outStream = new FileOutputStream(tmpFile);
                if (dataBytes != null)
                    outStream.write(dataBytes); 
                outStream.close();
            } catch (IOException e) {
                String tmpDir = System.getProperty("java.io.tmpdir");
                s_logger.warn("Failed to create temporary file: is " + tmpDir + " full?", e);
                return new Answer(cmd, false, "Failed to create or write to temporary file: is " + tmpDir + " full? " + e.getMessage() );
            }
       

            final Script command  = new Script(_vmDataPath, _timeout, s_logger);
            command.add("-r", cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP));
            command.add("-v", cmd.getVmIpAddress());
            command.add("-F", folder);
            command.add("-f", file);

            if (tmpFile != null) {
                command.add("-d", tmpFile.getAbsolutePath());
            }

            final String result = command.execute();

            if (tmpFile != null) {
                boolean deleted = tmpFile.delete();
                if (!deleted) {
                    s_logger.warn("Failed to clean up temp file after sending vmdata");
                    tmpFile.deleteOnExit();
                }
            }

            if (result != null) {
                return new Answer(cmd, false, result);        	
            }        	

        }

        return new Answer(cmd);
    }

    protected Answer execute(final IpAssocCommand cmd) {
        IpAddressTO[] ips = cmd.getIpAddresses();
        String[] results = new String[cmd.getIpAddresses().length];
        int i = 0;
        String result = null;
        String routerName = cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME);
        String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        for (IpAddressTO ip : ips) {
            result = assignPublicIpAddress(routerName, routerIp, ip.getPublicIp(), ip.isAdd(), 
                     ip.isFirstIP(), ip.isSourceNat(), ip.getVlanId(), ip.getVlanGateway(), ip.getVlanNetmask(),
                     ip.getVifMacAddress(), ip.getGuestIp(), 2);
            if (result != null) {
                results[i++] = IpAssocAnswer.errorResult;
            } else {
                results[i++] = ip.getPublicIp() + " - success";;
            }
        }
        return new IpAssocAnswer(cmd, results);
    }

    private String setLoadBalancerConfig(final String cfgFile,
            final String[] addRules, final String[] removeRules, final String[] statsRules,String routerIp) {

        if (routerIp == null) {
            routerIp = "none";
        }

        final Script command = new Script(_loadbPath, _timeout, s_logger);

        command.add("-i", routerIp);
        command.add("-f", cfgFile);

        StringBuilder sb = new StringBuilder();
        if (addRules.length > 0) {
            for (int i=0; i< addRules.length; i++) {
                sb.append(addRules[i]).append(',');
            }
            command.add("-a", sb.toString());
        }

        sb = new StringBuilder();
        if (removeRules.length > 0) {
            for (int i=0; i< removeRules.length; i++) {
                sb.append(removeRules[i]).append(',');
            }
            command.add("-d", sb.toString());
        }

        sb = new StringBuilder();
        if (statsRules.length > 0) {
            for (int i=0; i< statsRules.length; i++) {
                sb.append(statsRules[i]).append(',');
            }
            command.add("-s", sb.toString());
        }
        
        return command.execute();
    }

    protected synchronized Answer execute(final SavePasswordCommand cmd) {
        final String password = cmd.getPassword();
        final String routerPrivateIPAddress = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        final String vmName = cmd.getVmName();
        final String vmIpAddress = cmd.getVmIpAddress();
        final String local =  vmName;

        // Run save_password_to_domr.sh
        final String result = savePassword(routerPrivateIPAddress, vmIpAddress, password, local);
        if (result != null) {
            return new Answer(cmd, false, "Unable to save password to DomR.");
        } else {
            return new Answer(cmd);
        }
    }

    protected synchronized Answer execute (final DhcpEntryCommand cmd) {
        final Script command  = new Script(_dhcpEntryPath, _timeout, s_logger);
        command.add("-r", cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP));
        command.add("-v", cmd.getVmIpAddress());
        command.add("-m", cmd.getVmMac());
        command.add("-n", cmd.getVmName());

        final String result = command.execute();
        return new Answer(cmd, result==null, result);
    }

    public String getRouterStatus(String routerIP) {
        final Script command  = new Script(_getRouterStatusPath, _timeout, s_logger);
        final OutputInterpreter.OneLineParser parser = new OutputInterpreter.OneLineParser();
        command.add(routerIP);
        String result = command.execute(parser);
        if (result == null) {
            return parser.getLine();
        }
        return null;
    }

    protected Answer execute(CheckRouterCommand cmd) {
        final String routerPrivateIPAddress = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
    
        final String result = getRouterStatus(routerPrivateIPAddress);
        if (result == null || result.isEmpty()) {
            return new CheckRouterAnswer(cmd, "CheckRouterCommand failed");
        }
        return new CheckRouterAnswer(cmd, result, true);
    }

    protected Answer execute(BumpUpPriorityCommand cmd) {
        final String routerPrivateIPAddress = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        final Script command  = new Script(_bumpUpPriorityPath, _timeout, s_logger);
        final OutputInterpreter.OneLineParser parser = new OutputInterpreter.OneLineParser();
        command.add(routerPrivateIPAddress);
        String result = command.execute(parser);
        if (result != null) {
            return new Answer(cmd, false, "BumpUpPriorityCommand failed: " + result);
        }
        return new Answer(cmd, true, null);
    }

    protected Answer execute(final CheckConsoleProxyLoadCommand cmd) {
        return executeProxyLoadScan(cmd, cmd.getProxyVmId(), cmd.getProxyVmName(), cmd.getProxyManagementIp(), cmd.getProxyCmdPort());
    }

    protected Answer execute(final WatchConsoleProxyLoadCommand cmd) {
        return executeProxyLoadScan(cmd, cmd.getProxyVmId(), cmd.getProxyVmName(), cmd.getProxyManagementIp(), cmd.getProxyCmdPort());
    }

    private Answer executeProxyLoadScan(final Command cmd, final long proxyVmId, final String proxyVmName, final String proxyManagementIp, final int cmdPort) {
        String result = null;

        final StringBuffer sb = new StringBuffer();
        sb.append("http://").append(proxyManagementIp).append(":" + cmdPort).append("/cmd/getstatus");

        boolean success = true;
        try {
            final URL url = new URL(sb.toString());
            final URLConnection conn = url.openConnection();

            final InputStream is = conn.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            final StringBuilder sb2 = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb2.append(line + "\n");
                }
                result = sb2.toString();
            } catch (final IOException e) {
                success = false;
            } finally {
                try {
                    is.close();
                } catch (final IOException e) {
                    s_logger.warn("Exception when closing , console proxy address : " + proxyManagementIp);
                    success = false;
                }
            }
        } catch(final IOException e) {
            s_logger.warn("Unable to open console proxy command port url, console proxy address : " + proxyManagementIp);
            success = false;
        }

        return new ConsoleProxyLoadAnswer(cmd, proxyVmId, proxyVmName, success, result);
    }




    public synchronized String savePassword(final String privateIpAddress, final String vmIpAddress, final String password, final String localPath) {
        final Script command  = new Script(_savepasswordPath, _startTimeout, s_logger);
        command.add("-r", privateIpAddress);
        command.add("-v", vmIpAddress);
        command.add("-p", password);
        command.add(localPath);

        return command.execute();
    }


    public String assignPublicIpAddress(final String vmName, final long id, final String vnet, final String privateIpAddress, final String macAddress, final String publicIpAddress) {

        final Script command = new Script(_ipassocPath, _timeout, s_logger);
        command.add("-A");
        command.add("-f"); //first ip is source nat ip
        command.add("-r", vmName);
        command.add("-i", privateIpAddress);
        command.add("-a", macAddress);
        command.add("-l", publicIpAddress);

        return command.execute();
    }

    public String assignPublicIpAddress(final String vmName,
            final String privateIpAddress, final String publicIpAddress,
            final boolean add, final boolean firstIP, final boolean sourceNat,
            final String vlanId, final String vlanGateway,
            final String vlanNetmask, final String vifMacAddress, String guestIp, int nicNum){

        final Script command = new Script(_ipassocPath, _timeout, s_logger);
        command.add( privateIpAddress);
        if (add) {
            command.add("-A");
        } else {
            command.add("-D");
        }
        String cidrSize = Long.toString(NetUtils.getCidrSize(vlanNetmask));
        if (sourceNat) {
            command.add("-f");
            command.add("-l", publicIpAddress + "/" + cidrSize);
        } else if (firstIP) {
            command.add( "-f");
            command.add( "-l", publicIpAddress + "/" + cidrSize);
        } else {
            command.add("-l", publicIpAddress);
        }

        String publicNic = "eth" + nicNum;
        command.add("-c", publicNic);

        return command.execute();
    }
    
    private void deletExitingLinkLocalRoutTable(String linkLocalBr) {
        Script command = new Script("/bin/bash", _timeout);
        command.add("-c");
        command.add("ip route | grep " + NetUtils.getLinkLocalCIDR());
        OutputInterpreter.AllLinesParser parser = new OutputInterpreter.AllLinesParser();
        String result = command.execute(parser);
        boolean foundLinkLocalBr = false;
        if (result == null && parser.getLines() != null) {
            String[] lines = parser.getLines().split("\\n");
            for (String line : lines) {
                String[] tokens = line.split(" ");
                if (!tokens[2].equalsIgnoreCase(linkLocalBr)) {
                    Script.runSimpleBashScript("ip route del " + NetUtils.getLinkLocalCIDR());
                } else {
                    foundLinkLocalBr = true;
                }
            }
        }
        if (!foundLinkLocalBr) {
            Script.runSimpleBashScript("ifconfig " + linkLocalBr + " 169.254.0.1;" + "ip route add " + NetUtils.getLinkLocalCIDR() + " dev " + linkLocalBr + " src " + NetUtils.getLinkLocalGateway());
        }
    }

    public void createControlNetwork(String privBrName) {
        deletExitingLinkLocalRoutTable(privBrName);
        if (!isBridgeExists(privBrName)) {
            Script.runSimpleBashScript("brctl addbr " + privBrName + "; ifconfig " + privBrName + " up; ifconfig " + privBrName + " 169.254.0.1", _timeout);
        }
    }

    private boolean isBridgeExists(String bridgeName) {
        Script command = new Script("/bin/sh", _timeout);
        command.add("-c");
        command.add("brctl show|grep " + bridgeName);
        final OutputInterpreter.OneLineParser parser = new OutputInterpreter.OneLineParser();
        String result = command.execute(parser);
        if (result != null || parser.getLine() == null) {
            return false;
        } else {
            return true;
        }
    }

    private void deleteBridge(String brName) {
        Script cmd = new Script("/bin/sh", _timeout);
        cmd.add("-c");
        cmd.add("ifconfig " + brName + " down;brctl delbr " + brName);
        cmd.execute();
    }

    private boolean isDNSmasqRunning(String dnsmasqName) {
        Script cmd = new Script("/bin/sh", _timeout);
        cmd.add("-c");
        cmd.add("ls -l /var/run/libvirt/network/" + dnsmasqName + ".pid");
        String result = cmd.execute();
        if (result != null) {
            return false;
        } else {
            return true;
        }
    }

    private void stopDnsmasq(String dnsmasqName) {
        Script cmd = new Script("/bin/sh", _timeout);
        cmd.add("-c");
        cmd.add("kill -9 `cat /var/run/libvirt/network/"  + dnsmasqName +".pid`");
        cmd.execute();
    }

    public void cleanupPrivateNetwork(String privNwName, String privBrName){
        if (isBridgeExists(privBrName)) {
            deleteBridge(privBrName);
        }
    }

    //    protected Answer execute(final SetFirewallRuleCommand cmd) {
    //    	String args;
    //    	if(cmd.getProtocol().toLowerCase().equals(NetUtils.NAT_PROTO)){
    //    		//1:1 NAT needs instanceip;publicip;domrip;op
    //    		if(cmd.isCreate()) {
    //                args = "-A";
    //            } else {
    //                args = "-D";
    //            }
    //
    //    		args += " -l " + cmd.getPublicIpAddress();
    //    		args += " -i " + cmd.getRouterIpAddress();
    //    		args += " -r " + cmd.getPrivateIpAddress();
    //    		args += " -G " + cmd.getProtocol();
    //    	}else{
    //    		if (cmd.isEnable()) {
    //    			args = "-A";
    //    		} else {
    //    			args = "-D";
    //    		}
    //
    //    		args += " -P " + cmd.getProtocol().toLowerCase();
    //    		args += " -l " + cmd.getPublicIpAddress();
    //    		args += " -p " + cmd.getPublicPort();
    //    		args += " -n " + cmd.getRouterName();
    //    		args += " -i " + cmd.getRouterIpAddress();
    //    		args += " -r " + cmd.getPrivateIpAddress();
    //    		args += " -d " + cmd.getPrivatePort();
    //    		args += " -N " + cmd.getVlanNetmask();
    //
    //    		String oldPrivateIP = cmd.getOldPrivateIP();
    //    		String oldPrivatePort = cmd.getOldPrivatePort();
    //
    //    		if (oldPrivateIP != null) {
    //    			args += " -w " + oldPrivateIP;
    //    		}
    //
    //    		if (oldPrivatePort != null) {
    //    			args += " -x " + oldPrivatePort;
    //    		}
    //    	}
    //
    //    	final Script command = new Script(_firewallPath, _timeout, s_logger);
    //    	String [] argsArray = args.split(" ");
    //    	for (String param : argsArray) {
    //    		command.add(param);
    //    	}
    //    	String result = command.execute();
    //    	return new Answer(cmd, result == null, result);
    //    }

    protected String getDefaultScriptsDir() {
        return "scripts/network/domr/dom0";
    }

    protected String findScript(final String script) {
        return Script.findScript(_scriptsDir, script);
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;

        _scriptsDir = (String)params.get("domr.scripts.dir");
        if (_scriptsDir == null) {
            if(s_logger.isInfoEnabled()) {
                s_logger.info("VirtualRoutingResource _scriptDir can't be initialized from domr.scripts.dir param, use default" );
            }
            _scriptsDir = getDefaultScriptsDir();
        }

        if(s_logger.isInfoEnabled()) {
            s_logger.info("VirtualRoutingResource _scriptDir to use: " + _scriptsDir);
        }

        String value = (String)params.get("scripts.timeout");
        _timeout = NumbersUtil.parseInt(value, 120) * 1000;

        value = (String)params.get("start.script.timeout");
        _startTimeout = NumbersUtil.parseInt(value, 360) * 1000;

        value = (String)params.get("ssh.sleep");
        _sleep = NumbersUtil.parseInt(value, 10) * 1000;

        value = (String)params.get("ssh.retry");
        _retry = NumbersUtil.parseInt(value, 36);

        value = (String)params.get("ssh.port");
        _port = NumbersUtil.parseInt(value, 3922);

        _ipassocPath = findScript("ipassoc.sh");
        if (_ipassocPath == null) {
            throw new ConfigurationException("Unable to find the ipassoc.sh");
        }
        s_logger.info("ipassoc.sh found in " + _ipassocPath);

        _publicIpAddress = (String)params.get("public.ip.address");
        if (_publicIpAddress != null) {
            s_logger.warn("Incoming public ip address is overriden.  Will always be using the same ip address: " + _publicIpAddress);
        }

        _firewallPath = findScript("call_firewall.sh");
        if (_firewallPath == null) {
            throw new ConfigurationException("Unable to find the call_firewall.sh");
        }

        _loadbPath = findScript("call_loadbalancer.sh");
        if (_loadbPath == null) {
            throw new ConfigurationException("Unable to find the call_loadbalancer.sh");
        }

        _savepasswordPath = findScript("save_password_to_domr.sh");
        if(_savepasswordPath == null) {
            throw new ConfigurationException("Unable to find save_password_to_domr.sh");
        }

        _dhcpEntryPath = findScript("dhcp_entry.sh");
        if(_dhcpEntryPath == null) {
            throw new ConfigurationException("Unable to find dhcp_entry.sh");
        }

        _vmDataPath = findScript("vm_data.sh");
        if(_vmDataPath == null) {
            throw new ConfigurationException("Unable to find user_data.sh");
        }

        _getRouterStatusPath = findScript("getRouterStatus.sh");
        if(_getRouterStatusPath == null) {
            throw new ConfigurationException("Unable to find getRouterStatus.sh");
        }

        _publicEthIf = (String)params.get("public.network.device");
        if (_publicEthIf == null) {
            _publicEthIf = "xenbr1";
        }
        _publicEthIf = _publicEthIf.toLowerCase();

        _privateEthIf = (String)params.get("private.network.device");
        if (_privateEthIf == null) {
            _privateEthIf = "xenbr0";
        }
        _privateEthIf = _privateEthIf.toLowerCase();

        _bumpUpPriorityPath = findScript("bumpUpPriority.sh");
        if(_bumpUpPriorityPath == null) {
            throw new ConfigurationException("Unable to find bumpUpPriority.sh");
        }
        
        _l2tpVpnPath = findScript("l2tp_vpn.sh");
        if (_l2tpVpnPath == null) {
            throw new ConfigurationException("Unable to find l2tp_vpn.sh");
        }

        return true;
    }


    public String connect(final String ipAddress) {
        return connect(ipAddress, _port);
    }

    public String connect(final String ipAddress, final int port) {
        for (int i = 0; i <= _retry; i++) {
            SocketChannel sch = null;
            try {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Trying to connect to " + ipAddress);
                }
                sch = SocketChannel.open();
                sch.configureBlocking(true);

                final InetSocketAddress addr = new InetSocketAddress(ipAddress, port);
                sch.connect(addr);
                return null;
            } catch (final IOException e) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Could not connect to " + ipAddress);
                }
            } finally {
                if (sch != null) {
                    try {
                        sch.close();
                    } catch (final IOException e) {}
                }
            }
            try {
                Thread.sleep(_sleep);
            } catch (final InterruptedException e) {
            }
        }

        s_logger.debug("Unable to logon to " + ipAddress);

        return "Unable to connect";
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


}


