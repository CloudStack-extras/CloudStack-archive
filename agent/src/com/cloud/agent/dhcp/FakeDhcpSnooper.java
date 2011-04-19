package com.cloud.agent.dhcp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.utils.Pair;
import com.cloud.utils.net.NetUtils;

@Local(value = {DhcpSnooper.class})
public class FakeDhcpSnooper implements DhcpSnooper {
    private static final Logger s_logger = Logger.getLogger(FakeDhcpSnooper.class);
    private Queue<String> _ipAddresses = new ConcurrentLinkedQueue<String>();
    private Map<String, String> _macIpMap = new ConcurrentHashMap<String, String>();
    private Map<String, InetAddress> _vmIpMap = new ConcurrentHashMap<String, InetAddress>();

    @Override
    public boolean configure(String name, Map<String, Object> params)
            throws ConfigurationException {
        String guestIpRange = (String)params.get("guest.ip.range");
        if (guestIpRange != null) {
            String [] guestIps = guestIpRange.split("-");
            if (guestIps.length == 2) {
                long start = NetUtils.ip2Long(guestIps[0]);
                long end = NetUtils.ip2Long(guestIps[1]);
                while (start <= end) {
                    _ipAddresses.offer(NetUtils.long2Ip(start++));
                }
            }
        }
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public String getName() {
        return "FakeDhcpSnooper";
    }

    @Override
    public InetAddress getIPAddr(String macAddr, String vmName) {
        String ipAddr = _ipAddresses.poll();
        if (ipAddr == null) {
            s_logger.warn("No ip addresses left in queue");
            return null;
        }
        try {
            InetAddress inetAddr = InetAddress.getByName(ipAddr);
            _macIpMap.put(macAddr.toLowerCase(), ipAddr);
            _vmIpMap.put(vmName, inetAddr);
            s_logger.info("Got ip address " + ipAddr + " for vm " + vmName + " mac=" + macAddr.toLowerCase());
            return inetAddr;
        } catch (UnknownHostException e) {
            s_logger.warn("Failed to get InetAddress for " + ipAddr);
            return null;
        }
    }

    @Override
    public void cleanup(String macAddr, String vmName) {
        try {
            if (macAddr == null) {
                return;
            }
            InetAddress inetAddr = _vmIpMap.remove(vmName);
            String ipAddr = inetAddr.getHostName();
            for (Map.Entry<String, String> entry: _macIpMap.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(ipAddr)){
                    macAddr = entry.getKey();
                    break;
                }
            }
            ipAddr =  _macIpMap.remove(macAddr);

            s_logger.info("Cleaning up for mac address: " + macAddr + " ip=" + ipAddr + " inetAddr=" + inetAddr);
            if (ipAddr != null) {
                _ipAddresses.offer(ipAddr);
            }
        } catch (Exception e) {
            s_logger.debug("Failed to cleanup: " + e.toString());
        }
    }

    @Override
    public Map<String, InetAddress> syncIpAddr() {
        return _vmIpMap;
    }

    @Override
    public boolean stop() {
        return false;
    }

    @Override
    public void initializeMacTable(List<Pair<String, String>> macVmNameList) {
      

    }

}
