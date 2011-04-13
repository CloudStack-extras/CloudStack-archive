package com.cloud.agent.dhcp;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;

import com.cloud.utils.Pair;

public class FakeDhcpSnooper implements DhcpSnooper {

    @Override
    public boolean configure(String name, Map<String, Object> params)
            throws ConfigurationException {
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cleanup(String macAddr) {

    }

    @Override
    public HashMap<String, InetAddress> syncIpAddr() {
        return null;
    }

    @Override
    public boolean stop() {
        return false;
    }

    @Override
    public void initializeMacTable(List<Pair<String, String>> macVmNameList) {
      

    }

}
