package com.cloud.alert.snmp.dao;

import java.util.List;

import com.cloud.alert.snmp.SnmpManagersVO;
import com.cloud.utils.db.GenericDao;

/**
 * @author Anshul Gangwar
 *
 */
public interface SnmpManagersDao extends GenericDao<SnmpManagersVO, Long> {
    /**
     * returns SNMP Managers which are enabled and of type type
     * @param type
     * @param enabled
     * @return SNMP Managers which are enabled and of type type
     */
    List<SnmpManagersVO> getSnmpManagers(short type, boolean enabled);

    /**
     * returns SNMP Managers which are enabled
     * @param enabled
     * @return  SNMP Managers which are enabled
     */
    List<SnmpManagersVO> getSnmpManagers( boolean enabled);
}
