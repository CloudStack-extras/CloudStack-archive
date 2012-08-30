package com.cloud.alert.snmp;

/**
 * @author Anshul Gangwar
 *
 */
public interface SnmpManagers {
    long getId();
    String getName();
    String getIpAddress();
    String getPort();
    String getCommunity();
    boolean isEnabled();
    short getType();
}
