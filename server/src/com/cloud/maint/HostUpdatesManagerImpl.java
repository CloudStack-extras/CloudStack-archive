package com.cloud.maint;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.api.commands.ListHostUpdatesCmd;
import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.host.updates.HostUpdatesRefVO;
import com.cloud.host.updates.HostUpdatesService;
import com.cloud.host.updates.dao.HostUpdatesRefDao;
import com.cloud.server.ManagementServer;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.Adapters;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.concurrency.NamedThreadFactory;

@Local(value={HostUpdatesService.class, HostUpdatesManager.class})
public class HostUpdatesManagerImpl implements HostUpdatesManager, HostUpdatesService {

    @Inject(adapter = HostUpdatesMonitor.class)
    Adapters<HostUpdatesMonitor>      _hostUpdatesMonitor;
    @Inject
    HostUpdatesRefDao                 _hostUpdatesRefDao;
    @Inject
    ConfigurationDao                  _configDao;

    ScheduledExecutorService _executor;
    Boolean _updateCheckEnable;
    private String _name;
    private Map<String, String> _configs;
    private static final Logger s_logger = Logger.getLogger(HostUpdatesManagerImpl.class.getName());
    public final static int UPDATE_CHECK_INTERVAL = 604800; // 1*7*24*60*60 (1 week in seconds)
    boolean _stopped;
    int _updateCheckInterval;
    int _initialDelay = 10;

    @Override
    public List<HostUpdatesRefVO> searchForHostUpdates(ListHostUpdatesCmd cmd){
        Long hostId = cmd.getHostId();
        Boolean isApplied = cmd.isApplied();
        return _hostUpdatesRefDao.searchByHostId(hostId, isApplied);
    }

    protected class checkHostUpdates implements Runnable {
        @Override
        public void run() {
            try {
                Enumeration<HostUpdatesMonitor> enhum = _hostUpdatesMonitor.enumeration();
                while (enhum.hasMoreElements()) {
                    HostUpdatesMonitor hum = enhum.nextElement();
                    hum.updateHosts();
                }
            } catch (Exception e) {
                s_logger.error("Exception in Host Update Checker", e);
            }
        }
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;
        _configs = _configDao.getConfiguration("management-server", params);
        _updateCheckEnable = Boolean.valueOf(_configs.get(Config.HostUpdatesEnable.key()));
 
        ComponentLocator locator = ComponentLocator.getLocator(ManagementServer.Name);
        _hostUpdatesMonitor = locator.getAdapters(HostUpdatesMonitor.class);
        _updateCheckInterval = NumbersUtil.parseInt(_configs.get(Config.UpdateCheckInterval.key()), UPDATE_CHECK_INTERVAL);
        _executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("HostUpdateCheck"));

        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean start() {
        if (_updateCheckEnable) {
            _executor.scheduleAtFixedRate(new checkHostUpdates(), _initialDelay, _updateCheckInterval, TimeUnit.SECONDS);
        }

        return true;
    }

    @Override
    public boolean stop() {
        _executor.shutdown();
        return false;
    }
}
