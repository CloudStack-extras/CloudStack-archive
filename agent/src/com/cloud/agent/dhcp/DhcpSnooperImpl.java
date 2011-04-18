/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
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
package com.cloud.agent.dhcp;


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.ejb.Local;
import javax.naming.ConfigurationException;


import org.apache.log4j.Logger;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapBpfProgram;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.tcpip.Udp;

import com.cloud.utils.Pair;
import com.cloud.utils.concurrency.NamedThreadFactory;

@Local(value = { DhcpSnooper.class})
public class DhcpSnooperImpl implements DhcpSnooper {
    private static final Logger s_logger = Logger.getLogger(DhcpSnooperImpl.class);
    public enum DHCPState {
        DHCPACKED,
        DHCPREQUESTED,
        DHCPRESET;
    }
    public class IPAddr {
        String _vmName;
        InetAddress _ip;
        DHCPState _state;
        public IPAddr(InetAddress ip, DHCPState state, String vmName) {
            _ip = ip;
            _state = state;
            _vmName = vmName;
        }
    }
    protected ExecutorService _executor;
    protected Map<String, IPAddr> _macIpMap;
    protected Map<InetAddress, String> _ipMacMap;
    private DhcpServer _server;
    protected long _timeout = 1200000;
    
    public DhcpSnooperImpl(String bridge) {
        _executor = new ThreadPoolExecutor(10, 10 * 10, 1, TimeUnit.DAYS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("DhcpListener"));
        _macIpMap = new ConcurrentHashMap<String, IPAddr>();
        _ipMacMap = new ConcurrentHashMap<InetAddress, String>();
        _server = new DhcpServer(this, bridge);
        _server.start();
    }
    

    @Override
    public InetAddress getIPAddr(String macAddr, String vmName) {
        String macAddrLowerCase = macAddr.toLowerCase();
        IPAddr addr =  _macIpMap.get(macAddrLowerCase);
        if (addr == null) {
            addr = new IPAddr(null, DHCPState.DHCPRESET, vmName);
            _macIpMap.put(macAddrLowerCase, addr);
        } else {
            addr._state = DHCPState.DHCPRESET;
        }
        
        synchronized(addr) {
            try {
                addr.wait(_timeout);
            } catch (InterruptedException e) {
            }
            if (addr._state == DHCPState.DHCPACKED) {
                addr._state = DHCPState.DHCPRESET;
                return addr._ip;
            }
        }
        
        return null;
    }
    

    @Override
    public void cleanup(String macAddr) {
        try {
            if (macAddr == null) {
                return;
            }
            _macIpMap.remove(macAddr);
            _ipMacMap.values().remove(macAddr);
        } catch (Exception e) {
            s_logger.debug("Failed to cleanup: " + e.toString());
        }
    }
    

    @Override
    public HashMap<String, InetAddress> syncIpAddr() {
        Collection<IPAddr> ips = _macIpMap.values();
        HashMap<String, InetAddress> vmIpMap = new HashMap<String, InetAddress>();
        for(IPAddr ip : ips) {
            if (ip._state == DHCPState.DHCPACKED) {
                vmIpMap.put(ip._vmName, ip._ip);
            }
        }
        return vmIpMap;
    }
    
    @Override
    public void initializeMacTable(List<Pair<String, String>> macVmNameList) {
        for (Pair<String, String> macVmname : macVmNameList) {
            IPAddr ipAdrr = new IPAddr(null, DHCPState.DHCPRESET, macVmname.second());
            _macIpMap.put(macVmname.first(), ipAdrr);
        }
    }
    
    protected void setIPAddr(String macAddr, InetAddress ip, DHCPState state) {
        String macAddrLowerCase = macAddr.toLowerCase();
        if (state == DHCPState.DHCPREQUESTED) {
            IPAddr ipAddr = _macIpMap.get(macAddrLowerCase);
            if (ipAddr == null) {
                return;
            }
           
            _ipMacMap.put(ip, macAddr);
        } else if (state == DHCPState.DHCPACKED) {
            String destMac = macAddrLowerCase;
            if (macAddrLowerCase.equalsIgnoreCase("ff:ff:ff:ff:ff:ff")) {
                destMac = _ipMacMap.get(ip);
                if (destMac == null) {
                    return;
                }
            }
            
            IPAddr addr = _macIpMap.get(destMac);
            if (addr != null) {
                addr._ip = ip;
                addr._state = state;
            }
            synchronized (addr) {
                addr.notify();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.cloud.agent.dhcp.DhcpSnooper#stop()
     */
    @Override
    public boolean stop() {
        _executor.shutdown();
        _server.StopServer();
        return true;
    }
    
    private class DhcpServer extends Thread {
        private DhcpSnooperImpl _manager;
        private String _bridge;
        private Pcap _pcapedDev;
        private boolean _loop;
        public DhcpServer(DhcpSnooperImpl mgt, String bridge) {
            _manager = mgt;
            _bridge = bridge;
            _loop = true;
        }
        public void StopServer() {
            _loop = false;
            _pcapedDev.breakloop();
            _pcapedDev.close();
        }
        
        private Pcap initializePcap() {
            try {
                List<PcapIf> alldevs = new ArrayList<PcapIf>();
                StringBuilder errBuf = new StringBuilder();
                int r = Pcap.findAllDevs(alldevs, errBuf);
                if (r == Pcap.NOT_OK || alldevs.isEmpty()) {
                    return null;
                }

                PcapIf dev = null;
                for (PcapIf device : alldevs) {
                    if (device.getName().equalsIgnoreCase(_bridge)) {
                        dev = device;
                        break;
                    }
                }

                if (dev == null) {
                    s_logger.debug("Pcap: Can't find device: " + _bridge + " to listen on");
                    return null;
                }

                int snaplen = 64*1024;
                int flags = Pcap.MODE_PROMISCUOUS;
                int timeout = 10 * 1000;
                Pcap pcap = Pcap.openLive(dev.getName(), snaplen, flags, timeout, errBuf);
                if (pcap == null) {
                    s_logger.debug("Pcap: Can't open " + _bridge);
                    return null;
                }

                PcapBpfProgram program = new PcapBpfProgram();
                String expr = "dst port 68 or 67";
                int optimize = 0;
                int netmask = 0xFFFFFF00;
                if (pcap.compile(program, expr, optimize, netmask) != Pcap.OK) {
                    s_logger.debug("Pcap: can't compile BPF");
                    return null;
                }

                if(pcap.setFilter(program) != Pcap.OK) {
                    s_logger.debug("Pcap: Can't set filter");
                    return null;
                }
                return pcap;
            } catch (Exception e) {
                s_logger.debug("Failed to initialized: " + e.toString());
            }
            return null;
        }
        
        public void run() {
            while (_loop) {
                try {
                    _pcapedDev = initializePcap();
                    if (_pcapedDev == null) {
                        return;
                    }
                    
                    PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {
                        public void nextPacket(PcapPacket packet, String user) {
                            Udp u = new Udp();
                            if (packet.hasHeader(u)) {
                                int offset = u.getOffset() + u.getLength();
                                _executor.execute(new DhcpPacketParser(packet, offset, u.length() - u.getLength(), _manager));
                            }
                        }
                    };
                    s_logger.debug("Starting DHCP snooping on " + _bridge);
                    int retValue = _pcapedDev.loop(-1, jpacketHandler, "pcapPacketHandler");
                    if ( retValue == -1) {
                        s_logger.debug("Pcap: failed to set loop handler");
                    } else if ( retValue == -2 && !_loop) {
                        s_logger.debug("Pcap: terminated");
                        return;
                    }
                    _pcapedDev.close();
                } catch (Exception e) {
                    s_logger.debug("Pcap error:" + e.toString());
                }
            }
        }
    }
    
    static public void main(String args[]) {
        s_logger.addAppender(new org.apache.log4j.ConsoleAppender(new org.apache.log4j.PatternLayout(), "System.out"));
       final DhcpSnooperImpl manager = new DhcpSnooperImpl("cloudbr0");
       s_logger.debug(manager.getIPAddr("02:00:4c:66:00:03", "i-2-5-VM"));
       manager.stop();
       
    }

    @Override
    public boolean configure(String name, Map<String, Object> params)
            throws ConfigurationException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }
}
