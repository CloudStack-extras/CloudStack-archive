package com.cloud.alert.snmp;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * @author Anshul Gangwar
 * 
 */
public class SnmpHelper {
    private static final Logger s_logger = Logger.getLogger(SnmpHelper.class.getName());

    private Snmp snmp;
    private CommunityTarget target;

    public SnmpHelper(String address, String community) {
        setCommunityTarget(address, community);
        try {
            snmp = new Snmp(new DefaultUdpTransportMapping());
        } catch (IOException e) {
            snmp = null;
            s_logger.error(" Some error occured in crearting snmp object ");
        }

    }

    public void sendSnmpTrap(short alertType, long dataCenterId, Long podId, Long clusterId, String subject, String content) {
        try {
            if (snmp != null) {
                snmp.send(createPDU(alertType, dataCenterId, podId, clusterId, subject, content), target, null, null);
            }
        } catch (IOException e) {
            s_logger.error(" Some error occured in sending SNMP Trap");
        }
    }

    private PDU createPDU(short alertType, long dataCenterId, Long podId, Long clusterId, String subject, String content) {
        PDU trap = new PDU();
        trap.setType(PDU.TRAP);

        trap.add(new VariableBinding(SnmpConstants.snmpTrapOID, SNMPConstants.alertsTrap));
        trap.add(new VariableBinding(SNMPConstants.alertType, new Integer32(alertType)));
        trap.add(new VariableBinding(SNMPConstants.dataCenterId, new UnsignedInteger32(dataCenterId)));
        trap.add(new VariableBinding(SNMPConstants.podId, new UnsignedInteger32(podId)));
        trap.add(new VariableBinding(SNMPConstants.clusterId, new UnsignedInteger32(clusterId)));
        trap.add(new VariableBinding(SNMPConstants.subject, new OctetString(subject)));
        trap.add(new VariableBinding(SNMPConstants.content, new OctetString(content)));

        return trap;

    }

    public CommunityTarget setCommunityTarget(String address, String community) {
        Address targetaddress = new UdpAddress(address);
        target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setVersion(SnmpConstants.version2c);
        target.setAddress(targetaddress);
        return target;
    }

    public static void main(String[] args) {
        SnmpHelper s = new SnmpHelper("127.0.0.1/162", "public");
        for (short i = 0; i < 3; i++) {
            s.sendSnmpTrap(i, 20L, 30L, 30L, " sample test ", " chalo kaam kar raha hai ");

        }
    }

}
