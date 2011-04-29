package com.cloud.alert;

import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.alert.AlertAdapter;
import com.cloud.alert.AlertManager;
import com.cloud.consoleproxy.ConsoleProxyAlertEventArgs;
import com.cloud.consoleproxy.ConsoleProxyManager;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.events.SubscriptionMgr;
import com.cloud.vm.ConsoleProxyVO;
import com.cloud.vm.dao.ConsoleProxyDao;

@Local(value=AlertAdapter.class)
public class ConsoleProxyAlertAdapter implements AlertAdapter {
	
	private static final Logger s_logger = Logger.getLogger(ConsoleProxyAlertAdapter.class);
	
	private AlertManager _alertMgr;
    private String _name;
    
	private DataCenterDao _dcDao;
	private ConsoleProxyDao _consoleProxyDao;
    
    public void onProxyAlert(Object sender, ConsoleProxyAlertEventArgs args) {
    	if(s_logger.isDebugEnabled())
    		s_logger.debug("received console proxy alert");
    	
    	DataCenterVO dc = _dcDao.findById(args.getZoneId());
    	ConsoleProxyVO proxy = args.getProxy();
    	if(proxy == null)
    		proxy = _consoleProxyDao.findById(args.getProxyId());
    	
    	switch(args.getType()) {
    	case ConsoleProxyAlertEventArgs.PROXY_CREATED :
        	if(s_logger.isDebugEnabled())
        		s_logger.debug("New console proxy created, zone: " + dc.getName() + ", proxy: " + 
        			proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() + ", private IP: " + 
        			proxy.getPrivateIpAddress());
			break;
    		
    	case ConsoleProxyAlertEventArgs.PROXY_UP :
        	if(s_logger.isDebugEnabled())
        		s_logger.debug("Console proxy is up, zone: " + dc.getName() + ", proxy: " + 
        			proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() + ", private IP: " + 
        			proxy.getPrivateIpAddress());
    		
			_alertMgr.sendAlert(
				AlertManager.ALERT_TYPE_CONSOLE_PROXY,
				args.getZoneId(),
				proxy.getPodId(),
				"Console proxy up in zone: " + dc.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() 
				 	+ ", private IP: " + (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()),
			 	"Console proxy up (zone " + dc.getName() + ")" 	
			);
			break;
    		
    	case ConsoleProxyAlertEventArgs.PROXY_DOWN :
        	if(s_logger.isDebugEnabled())
        		s_logger.debug("Console proxy is down, zone: " + dc.getName() + ", proxy: " + 
        			proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() + ", private IP: " + 
        			(proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()));
    		
			_alertMgr.sendAlert(
				AlertManager.ALERT_TYPE_CONSOLE_PROXY,
				args.getZoneId(),
				proxy.getPodId(),
				"Console proxy down in zone: " + dc.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() 
				 	+ ", private IP: " + (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()),
			 	"Console proxy down (zone " + dc.getName() + ")" 	
			);
			break;
    		
    	case ConsoleProxyAlertEventArgs.PROXY_REBOOTED :
        	if(s_logger.isDebugEnabled())
        		s_logger.debug("Console proxy is rebooted, zone: " + dc.getName() + ", proxy: " + 
        			proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() + ", private IP: " + 
        			(proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()));
    		
			_alertMgr.sendAlert(
				AlertManager.ALERT_TYPE_CONSOLE_PROXY,
				args.getZoneId(),
				proxy.getPodId(),
				"Console proxy rebooted in zone: " + dc.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() 
				 	+ ", private IP: " + (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()),
			 	"Console proxy rebooted (zone " + dc.getName() + ")" 	
			);
			break;
			
    	case ConsoleProxyAlertEventArgs.PROXY_CREATE_FAILURE :
        	if(s_logger.isDebugEnabled())
        		s_logger.debug("Console proxy creation failure, zone: " + dc.getName() + ", proxy: " + 
        			proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() + ", private IP: " + 
        			(proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()));
    		
			_alertMgr.sendAlert(
				AlertManager.ALERT_TYPE_CONSOLE_PROXY,
				args.getZoneId(),
				proxy.getPodId(),
				"Console proxy creation failure. zone: " + dc.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() 
				 	+ ", private IP: " + (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()) 
				 	+ ", error details: " + args.getMessage(),
			 	"Console proxy creation failure (zone " + dc.getName() + ")"
			);
    		break;
    		
    	case ConsoleProxyAlertEventArgs.PROXY_START_FAILURE :
        	if(s_logger.isDebugEnabled())
        		s_logger.debug("Console proxy startup failure, zone: " + dc.getName() + ", proxy: " + 
        			proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() + ", private IP: " + 
        			(proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()));
    		
			_alertMgr.sendAlert(
				AlertManager.ALERT_TYPE_CONSOLE_PROXY,
				args.getZoneId(),
				proxy.getPodId(),
				"Console proxy startup failure. zone: " + dc.getName() + ", proxy: " + proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() 
				 	+ ", private IP: " + (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()) 
				 	+ ", error details: " + args.getMessage(),
			 	"Console proxy startup failure (zone " + dc.getName() + ")" 	
			);
    		break;
    	
    	case ConsoleProxyAlertEventArgs.PROXY_FIREWALL_ALERT :
        	if(s_logger.isDebugEnabled())
        		s_logger.debug("Console proxy firewall alert, zone: " + dc.getName() + ", proxy: " + 
        			proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() + ", private IP: " + 
        			(proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()));
    		
			_alertMgr.sendAlert(
				AlertManager.ALERT_TYPE_CONSOLE_PROXY,
				args.getZoneId(),
				proxy.getPodId(),
				"Failed to open console proxy firewall port. zone: " + dc.getName() + ", proxy: " + proxy.getHostName() 
					+ ", public IP: " + proxy.getPublicIpAddress() 
					+ ", private IP: " + (proxy.getPrivateIpAddress() == null ? "N/A" : proxy.getPrivateIpAddress()),
				"Console proxy alert (zone " + dc.getName() + ")"	
			);
    		break;
    	
    	case ConsoleProxyAlertEventArgs.PROXY_STORAGE_ALERT :
        	if(s_logger.isDebugEnabled())
        		s_logger.debug("Console proxy storage alert, zone: " + dc.getName() + ", proxy: " + 
        			proxy.getHostName() + ", public IP: " + proxy.getPublicIpAddress() + ", private IP: " + 
        			proxy.getPrivateIpAddress() + ", message: " + args.getMessage());
    		
			_alertMgr.sendAlert(
				AlertManager.ALERT_TYPE_STORAGE_MISC,
				args.getZoneId(),
				proxy.getPodId(),
				"Console proxy storage issue. zone: " + dc.getName() + ", message: " + args.getMessage(),
				"Console proxy alert (zone " + dc.getName() + ")"
			);
    		break;
    	}
    }
    
	@Override
	public boolean configure(String name, Map<String, Object> params)
		throws ConfigurationException {
		
		if (s_logger.isInfoEnabled())
			s_logger.info("Start configuring console proxy alert manager : " + name);

		ComponentLocator locator = ComponentLocator.getCurrentLocator();

		_dcDao = locator.getDao(DataCenterDao.class);
		if (_dcDao == null) {
			throw new ConfigurationException("Unable to get " + DataCenterDao.class.getName());
		}
		
		_consoleProxyDao = locator.getDao(ConsoleProxyDao.class);
		if (_consoleProxyDao == null) {
			throw new ConfigurationException("Unable to get " + ConsoleProxyDao.class.getName());
		}
		
		_alertMgr = locator.getManager(AlertManager.class);
		if (_alertMgr == null) {
			throw new ConfigurationException("Unable to get " + AlertManager.class.getName());
		}
		
		try {
			SubscriptionMgr.getInstance().subscribe(ConsoleProxyManager.ALERT_SUBJECT, this, "onProxyAlert");
		} catch (SecurityException e) {
			throw new ConfigurationException("Unable to register console proxy event subscription, exception: " + e);
		} catch (NoSuchMethodException e) {
			throw new ConfigurationException("Unable to register console proxy event subscription, exception: " + e);
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
