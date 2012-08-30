package com.cloud.alert.snmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.alert.snmp.dao.SnmpManagersDao;
import com.cloud.utils.component.Inject;

/**
 * @author Anshul Gangwar
 * 
 */
@Local(value = { SnmpManager.class })
public class SnmpManagerImpl implements SnmpManager {
    private static final Logger s_logger = Logger.getLogger(SnmpManagerImpl.class.getName());

    private String _name = null;
    @Inject
    private SnmpManagersDao _snmpManagersDao;

    List<SnmpHelper> generalSnmpHelpers = null;
    List<SnmpHelper> usageSnmpHelpers = null;

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _name = name;

        initializeSnmpHelpers();

        return true;
    }

    private void initializeSnmpHelpers() {

        generalSnmpHelpers = new ArrayList<SnmpHelper>();
        usageSnmpHelpers = new ArrayList<SnmpHelper>();

        List<SnmpManagersVO> sms = _snmpManagersDao.getSnmpManagers(true);

        String address;
        String community;

        for (SnmpManagersVO sm : sms) {
            address = sm.getIpAddress() + "/" + sm.getPort();
            community = sm.getCommunity();

            if (sm.getType() == SNMPConstants.GENERAL_ALERT) {
                generalSnmpHelpers.add(new SnmpHelper(address, community));
            }

            if (sm.getType() == SNMPConstants.USAGE_ALERT) {
                usageSnmpHelpers.add(new SnmpHelper(address, community));
            }
        }
    }

    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public SnmpManagersResponse registerSnmpManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SnmpManagersResponse enableSnmpManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean deleteSnmpManager() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public SnmpManagersResponse updateSnmpManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SnmpManagersResponse> listSnmpManagers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendSnmpTrap(short alertType, long dataCenterId, Long podId, Long clusterId, String subject, String content, short type) {
        s_logger.error(" sending SNMP trap to SNMP Managers");

        if (type == SNMPConstants.GENERAL_ALERT) {
            for (SnmpHelper h : generalSnmpHelpers) {
                h.sendSnmpTrap(alertType, dataCenterId, podId, clusterId, subject, content);
            }
        }

        if (type == SNMPConstants.USAGE_ALERT) {
            for (SnmpHelper h : usageSnmpHelpers) {
                h.sendSnmpTrap(alertType, dataCenterId, podId, clusterId, subject, content);
            }
        }
        /*
         * SnmpManagersVO m = new SnmpManagersVO();
         * m.setName("test");
         * m.setIpAddress("127.0.0.1");
         * m.setPort("162");
         * m.setCommunity("public");
         * m.setEnabled(true);
         * m.setType(type);
         * _snmpManagersDao.persist(m);
         */
    }

    @Override
    public boolean isEnabled(short type) {

        if (type == SNMPConstants.GENERAL_ALERT) {
            if (generalSnmpHelpers.isEmpty()) {
                return false;
            }
            else {
                return true;
            }
        }

        if (type == SNMPConstants.USAGE_ALERT) {
            if (usageSnmpHelpers.isEmpty()) {
                return false;
            }
            else {
                return true;
            }
        }

        return false;
    }

}
