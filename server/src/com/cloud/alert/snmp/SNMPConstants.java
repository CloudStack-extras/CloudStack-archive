package com.cloud.alert.snmp;

import org.snmp4j.smi.OID;

/**
 * @author Anshul Gangwar
 *
 *IMPORTANT      
 * 
 * These OIDs are based on MIB file. If there is any change in MIB file
 * then that should be reflected in this file also * 
 * <br/><br/>
 * used capitals due to conflict with other SnmpConstants class
 */
public class SNMPConstants {

    public static final short GENERAL_ALERT = 1;

    public static final short USAGE_ALERT   = 2;

    public static final int rootId = 45000;

    public static final OID cloudstack =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId});
    public static final OID alerts =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1});
    public static final OID traps =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 0});
    public static final OID objects =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 1});
    public static final OID conformance =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 2});
    public static final OID groups =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 2, 1});
    public static final OID compliances =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 2, 2});
    public static final OID alertsTrap =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 0, 1});
    public static final OID alertType =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 1, 1});
    public static final OID dataCenterId =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 1, 2});
    public static final OID podId =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 1, 3});
    public static final OID clusterId =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 1, 4});
    public static final OID subject =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 1, 5});
    public static final OID content =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 1, 6});
    public static final OID compliance =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 2, 2, 1});
    public static final OID alertsObjectsGroup =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 2, 1, 1});
    public static final OID alertsNotificationsGroup =
            new OID(new int[]{1, 3, 6, 1, 4, 1, rootId, 1, 2, 1, 2});

}

