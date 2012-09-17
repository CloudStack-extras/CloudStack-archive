package com.cloud.host.updates;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.Inject;
import com.cloud.utils.concurrency.NamedThreadFactory;

@Local(value={HostUpdatesManager.class})
public class HostUpdatesManagerImpl implements HostUpdatesManager { 

    @Inject
    ResourceManager _resourceManager;
    @Inject
    ConfigurationDao _configDao;

    ScheduledExecutorService _executor;
    private String _name;
    private Map<String, String> _configs;
    private static final Logger s_logger = Logger.getLogger(HostUpdatesManagerImpl.class.getName());

    public final static String HYPERVISOR_TYPE = "XenServer";
    public final static int UPDATE_CHECK_INTERVAL = 604800;
    long _serverId;
    boolean _stopped;
    int _updateCheckInterval;
    int _initialDelay = 10;
    String _url;
    
    protected class checkHostUpdates implements Runnable {
        @Override
        public void run() {
            try {
                _resourceManager.hostUpdateChecker(_url, HYPERVISOR_TYPE);
            } catch (Throwable e){
                s_logger.error("Exception in Host Update Checker",e);
            };
        }
    }
    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;
        _configs = _configDao.getConfiguration("management-server", params);
        _updateCheckInterval = NumbersUtil.parseInt(_configs.get(Config.XenUpdateCheckInterval.key()),UPDATE_CHECK_INTERVAL);
        _url = _configs.get(Config.XenUpdateURL.key()); 
        _executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("HostUpdateCheck"));

        return true;
    }


    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean start() {
        _executor.scheduleAtFixedRate(new checkHostUpdates(), _initialDelay, _updateCheckInterval, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public boolean stop() {
        _executor.shutdown();
        return false;
    }
}
