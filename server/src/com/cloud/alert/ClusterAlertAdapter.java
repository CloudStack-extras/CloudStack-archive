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

package com.cloud.alert;

import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.cluster.ClusterManager;
import com.cloud.cluster.ClusterNodeJoinEventArgs;
import com.cloud.cluster.ClusterNodeLeftEventArgs;
import com.cloud.cluster.ManagementServerHostVO;
import com.cloud.cluster.dao.ManagementServerHostDao;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.events.EventArgs;
import com.cloud.utils.events.SubscriptionMgr;

@Local(value = AlertAdapter.class)
public class ClusterAlertAdapter implements AlertAdapter {

    private static final Logger s_logger = Logger.getLogger(ClusterAlertAdapter.class);

    private AlertManager _alertMgr;
    private String _name;

    private ManagementServerHostDao _mshostDao;

    public void onClusterAlert(Object sender, EventArgs args) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Receive cluster alert, EventArgs: " + args.getClass().getName());
        }

        if (args instanceof ClusterNodeJoinEventArgs) {
            onClusterNodeJoined(sender, (ClusterNodeJoinEventArgs)args);
        } else if (args instanceof ClusterNodeLeftEventArgs) {
            onClusterNodeLeft(sender, (ClusterNodeLeftEventArgs)args);
        } else {
            s_logger.error("Unrecognized cluster alert event");
        }
    }

    private void onClusterNodeJoined(Object sender, ClusterNodeJoinEventArgs args) {
        if (s_logger.isDebugEnabled()) {
        	for(ManagementServerHostVO mshost: args.getJoinedNodes()) {
                s_logger.debug("Handle cluster node join alert, joined node: " + mshost.getServiceIP() + ", msidL: " + mshost.getMsid());
        	}
        }

        for (ManagementServerHostVO mshost : args.getJoinedNodes()) {
            if (mshost.getId() == args.getSelf().longValue()) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Management server node " + mshost.getServiceIP() + " is up, send alert");
                }

                _alertMgr.sendAlert(AlertManager.ALERT_TYPE_MANAGMENT_NODE, 0, new Long(0), "Management server node " + mshost.getServiceIP() + " is up", "");
                break;
            }
        }
    }

    private void onClusterNodeLeft(Object sender, ClusterNodeLeftEventArgs args) {

        if (s_logger.isDebugEnabled()) {
        	for(ManagementServerHostVO mshost: args.getLeftNodes()) {
        		s_logger.debug("Handle cluster node left alert, leaving node: " + mshost.getServiceIP() + ", msid: " + mshost.getMsid());
        	}
        }

        for (ManagementServerHostVO mshost : args.getLeftNodes()) {
            if (mshost.getId() != args.getSelf().longValue()) {
                GlobalLock lock = GlobalLock.getInternLock("ManagementAlert." + mshost.getId());
                try {
                    if (lock.lock(180)) {
                        try {
                            ManagementServerHostVO alertHost = _mshostDao.findById(mshost.getId());
                            if (alertHost.getAlertCount() == 0) {
                                _mshostDao.increaseAlertCount(mshost.getId());

                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Detected management server node " + mshost.getServiceIP() + " is down, send alert");
                                }

                                _alertMgr.sendAlert(AlertManager.ALERT_TYPE_MANAGMENT_NODE, 0, new Long(0), "Management server node " + mshost.getServiceIP() + " is down", "");
                            } else {
                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Detected management server node " + mshost.getServiceIP() + " is down, but alert has already been set");
                                }
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                } finally {
                    lock.releaseRef();
                }
            }
        }
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Start configuring cluster alert manager : " + name);
        }

        ComponentLocator locator = ComponentLocator.getCurrentLocator();

        _mshostDao = locator.getDao(ManagementServerHostDao.class);
        if (_mshostDao == null) {
            throw new ConfigurationException("Unable to get " + ManagementServerHostDao.class.getName());
        }

        _alertMgr = locator.getManager(AlertManager.class);
        if (_alertMgr == null) {
            throw new ConfigurationException("Unable to get " + AlertManager.class.getName());
        }

        try {
            SubscriptionMgr.getInstance().subscribe(ClusterManager.ALERT_SUBJECT, this, "onClusterAlert");
        } catch (SecurityException e) {
            throw new ConfigurationException("Unable to register cluster event subscription");
        } catch (NoSuchMethodException e) {
            throw new ConfigurationException("Unable to register cluster event subscription");
        }

        return true;
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
