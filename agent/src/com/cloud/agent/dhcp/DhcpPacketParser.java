package com.cloud.agent.dhcp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.packet.JMemoryPacket;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.lan.IEEE802dot1q;
import org.jnetpcap.protocol.tcpip.Udp;

import com.cloud.agent.dhcp.DhcpSnooperImpl.DHCPState;
import com.cloud.agent.dhcp.DhcpSnooperImpl.IPAddr;
import com.sun.mail.iap.ByteArray;

import edu.emory.mathcs.backport.java.util.Arrays;

public class DhcpPacketParser implements Runnable{
    private enum DHCPPACKET {
        OP(0),
        HTYPE(1),
        HLEN(2),
        HOPS(3),
        XID(4),
        SECS(8),
        FLAGS(10),
        CIADDR(12),
        YIADDR(16),
        SIDADDR(20),
        GIADDR(24),
        CHADDR(28),
        SNAME(44),
        FILE(108),
        MAGIC(236),
        OPTIONS(240);
        int offset;
        DHCPPACKET(int i) {
            offset = i;
        }
        int getValue() {
            return offset;
        }
    }
    private enum DHCPOPTIONTYPE {
        PAD(0),
        MESSAGETYPE(53),
        REQUESTEDIP(50),
        END(255);
        int type;
        DHCPOPTIONTYPE(int i) {
            type = i;
        }
        int getValue() {
            return type;
        }
    }
    private enum DHCPMSGTYPE {
        DHCPDISCOVER(1),
        DHCPOFFER(2),
        DHCPREQUEST(3),
        DHCPDECLINE(4),
        DHCPACK(5),
        DHCPNAK(6),
        DHCPRELEASE(7),
        DHCPINFORM(8);
        int _type;
        DHCPMSGTYPE(int type) {
            _type = type; 
        }
        int getValue() {
            return _type;
        }
        public static DHCPMSGTYPE valueOf(int type) {
            for (DHCPMSGTYPE t : values()) {
                if (type == t.getValue()) {
                    return t;
                }
            }
            return null;
        }
    }
    
    private class DHCPMSG {
        DHCPMSGTYPE msgType;
        byte[] caddr;
        byte[] yaddr;
        byte[] chaddr;
        byte[] requestedIP;
        public DHCPMSG() {
            caddr = new byte[4];
            yaddr = new byte[4];
            chaddr = new byte[6];
        }
    }
    
    private PcapPacket _buffer;
    private int _offset;
    private int _len;
    private DhcpSnooperImpl _manager;
    
    
    public DhcpPacketParser(PcapPacket buffer, int offset, int len, DhcpSnooperImpl manager) {
        _buffer = buffer;
        _offset = offset;
        _len = len;
        _manager = manager;
    }
    private int getPos(int pos) {
        return _offset + pos;
    }
    private byte getByte(int offset) {
        return _buffer.getByte(getPos(offset));
    }
    private void getByteArray(int offset, byte[] array) {
        _buffer.getByteArray(getPos(offset), array);
    }
    private long getUInt(int offset) {
        return _buffer.getUInt(getPos(offset));
    }
    
    private DHCPMSG getDhcpMsg() {
        long magic = getUInt(DHCPPACKET.MAGIC.getValue());
        if (magic != 0x63538263) {
            return null;
        }
        
        DHCPMSG msg = new DHCPMSG();
        
        int pos = DHCPPACKET.OPTIONS.getValue();
        while (pos <= _len) {
            int type = (int)getByte(pos++) & 0xff;

            if (type == DHCPOPTIONTYPE.END.getValue()) {
                break;
            }
            if (type == DHCPOPTIONTYPE.PAD.getValue()) {
                continue;
            }
            int len = 0;
            if (pos <= _len) {
                len = ((int)getByte(pos++)) & 0xff;
            }
            
            if (type == DHCPOPTIONTYPE.MESSAGETYPE.getValue() || type == DHCPOPTIONTYPE.REQUESTEDIP.getValue()) {
                /*Read data only if needed */
                byte[] data = null;
                if ((len + pos) <= _len) {
                    data = new byte[len];
                    getByteArray(pos, data);
                }
                
                if (type == DHCPOPTIONTYPE.MESSAGETYPE.getValue()) {
                    msg.msgType = DHCPMSGTYPE.valueOf((int)data[0]);
                } else if (type == DHCPOPTIONTYPE.REQUESTEDIP.getValue()) {
                    msg.requestedIP = data;
                }
            }
            
            pos += len;
        }
        
        if (msg.msgType == DHCPMSGTYPE.DHCPREQUEST) {
            getByteArray(DHCPPACKET.CHADDR.getValue(), msg.chaddr);
            getByteArray(DHCPPACKET.CIADDR.getValue(), msg.caddr);
        } else if (msg.msgType == DHCPMSGTYPE.DHCPACK) {
            getByteArray(DHCPPACKET.YIADDR.getValue(), msg.yaddr);
        }
        return msg;
    }
    
    private String formatMacAddress(byte[] mac) {
        StringBuffer sb = new StringBuffer();
        Formatter formatter = new Formatter(sb);
        for (int i = 0; i < mac.length; i++) {
            formatter.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : "");
        }
        return sb.toString();
    }
    
    private String getDestMacAddress() {
        Ethernet ether = new Ethernet();
        if (_buffer.hasHeader(ether)) {
            byte[] destMac = ether.destination();
            return formatMacAddress(destMac);
        }
        return null;
    }
    
    @Override
    public void run() {
        DHCPMSG msg = getDhcpMsg();

        if (msg == null) {
            return;
        }
        
        if (msg.msgType == DHCPMSGTYPE.DHCPACK) {
            InetAddress ip = null;
            try {
                ip = InetAddress.getByAddress(msg.yaddr);
                String macAddr = getDestMacAddress();
                _manager.setIPAddr(macAddr, ip, DHCPState.DHCPACKED);
            } catch (UnknownHostException e) {

            }
        }  else if (msg.msgType == DHCPMSGTYPE.DHCPREQUEST) {
            InetAddress ip = null;
            if (msg.requestedIP != null) {
                try {
                    ip = InetAddress.getByAddress(msg.requestedIP);
                } catch (UnknownHostException e) {
                }
            }
            if (ip == null) {
                try {
                    ip = InetAddress.getByAddress(msg.caddr);
                } catch (UnknownHostException e) {
                }
            }

            if (ip != null) {
                String macAddr = formatMacAddress(msg.chaddr);
                _manager.setIPAddr(macAddr, ip, DHCPState.DHCPREQUESTED);
            }
        }

    }

    private void test() {
        JPacket packet = new JMemoryPacket(Ethernet.ID,  
        "      06fa 8800 00b3 0656 d200 0027 8100 001a 0800 4500 0156 64bf 0000 4011 f3f2 ac1a 6412 ac1a 649e 0043 0044 0001 0000 0001");  
        Ethernet eth = new Ethernet();
        if (packet.hasHeader(eth)) {
            System.out.print(" ether:" + eth);
        }
        IEEE802dot1q vlan = new IEEE802dot1q();
        if (packet.hasHeader(vlan)) {
            System.out.print(" vlan: " + vlan);
        }

        if (packet.hasHeader(Udp.ID)) {
            System.out.print("has udp");
        }
    }
}
