/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */


package com.cloud.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.Agent.ExitStatus;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupVMMAgentCommand;
import com.cloud.agent.dao.StorageComponent;
import com.cloud.agent.dao.impl.PropertiesStorage;
import com.cloud.agent.transport.Request;
import com.cloud.resource.ServerResource;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.ProcessUtil;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.backoff.BackoffAlgorithm;
import com.cloud.utils.backoff.impl.ConstantTimeBackoff;
import com.cloud.utils.component.Adapters;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.MacAddress;
import com.cloud.utils.nio.HandlerFactory;
import com.cloud.utils.nio.Link;
import com.cloud.utils.nio.NioServer;
import com.cloud.utils.nio.Task;
import com.cloud.utils.nio.Task.Type;

/**
 * Implementation of agent shell to run the agents on System Center Virtual Machine manager
 **/

public class VmmAgentShell implements IAgentShell, HandlerFactory {

	private static final Logger s_logger = Logger.getLogger(VmmAgentShell.class.getName());
    private final Properties _properties = new Properties();
    private final Map<String, Object> _cmdLineProperties = new HashMap<String, Object>();
    private StorageComponent _storage;
    private BackoffAlgorithm _backoff;
    private String _version;
    private String _zone;
    private String _pod;
    private String _cluster;
    private String _host;
    private String _privateIp;
    private int _port;
    private int _proxyPort;
    private int _workers;
    private String _guid;
	static private NioServer _connection;
	static private int _listenerPort=9000;    
    private int _nextAgentId = 1;
    private volatile boolean _exit = false;
    private int _pingRetries;
    private Thread _consoleProxyMain = null;
    private final List<Agent> _agents = new ArrayList<Agent>();

    public VmmAgentShell() {
    }
    
    @Override
    public Properties getProperties() {
    	return _properties;
    }
    
    @Override
    public BackoffAlgorithm getBackoffAlgorithm() {
    	return _backoff;
    }
    
    @Override
    public int getPingRetries() {
    	return _pingRetries;
    }
    
    @Override
    public String getZone() {
    	return _zone;
    }
    
    @Override
    public String getPod() {
    	return _pod;
    }
    
    @Override
    public String getHost() {
    	return _host;
    }
    
    @Override
    public String getPrivateIp() {
    	return _privateIp;
    }
    
    @Override
    public int getPort() {
    	return _port;
    }
    
    @Override
    public int getProxyPort() {
    	return _proxyPort;
    }
    
    @Override
    public int getWorkers() {
    	return _workers;
    }
    
    @Override
    public String getGuid() {
    	return _guid;
    }

	@Override
	public void upgradeAgent(String url) {
		// TODO Auto-generated method stub
		
	}

   @Override
    public String getVersion() {
    	return _version;
    }

	@Override
	public Map<String, Object> getCmdLineProperties() {
		// TODO Auto-generated method stub
		return _cmdLineProperties;
	}

	public String getProperty(String prefix, String name) {
    	if(prefix != null)
    		return _properties.getProperty(prefix + "." + name);
    	
    	return _properties.getProperty(name);
    }
	
	@Override
	public String getPersistentProperty(String prefix, String name) {
    	if(prefix != null)
    		return _storage.get(prefix + "." + name);
    	return _storage.get(name);
	}

	@Override
	public void setPersistentProperty(String prefix, String name, String value) {
    	if(prefix != null)
    		_storage.persist(prefix + "." + name, value);
    	else
    		_storage.persist(name, value);		
	}

   private void loadProperties() throws ConfigurationException {
        final File file = PropertiesUtil.findConfigFile("agent.properties");
        if (file == null) {
            throw new ConfigurationException("Unable to find agent.properties.");
        }

        s_logger.info("agent.properties found at " + file.getAbsolutePath());

        try {
            _properties.load(new FileInputStream(file));
        } catch (final FileNotFoundException ex) {
            throw new CloudRuntimeException("Cannot find the file: " + file.getAbsolutePath(), ex);
        } catch (final IOException ex) {
            throw new CloudRuntimeException("IOException in reading " + file.getAbsolutePath(), ex);
        }
    }
	
    protected boolean parseCommand(final String[] args) throws ConfigurationException {
        String host = null;
        String workers = null;
        String port = null;
        String zone = null;
        String pod = null;
        String guid = null;
        for (int i = 0; i < args.length; i++) {
            final String[] tokens = args[i].split("=");
            if (tokens.length != 2) {
                System.out.println("Invalid Parameter: " + args[i]);
                continue;
            }
            
            // save command line properties
            _cmdLineProperties.put(tokens[0], tokens[1]);

            if (tokens[0].equalsIgnoreCase("port")) {
                port = tokens[1];
            } else if (tokens[0].equalsIgnoreCase("threads")) {
                workers = tokens[1];
            } else if (tokens[0].equalsIgnoreCase("host")) {
                host = tokens[1];
            } else if(tokens[0].equalsIgnoreCase("zone")) {
            	zone = tokens[1];
            } else if(tokens[0].equalsIgnoreCase("pod")) {
            	pod = tokens[1];
            } else if(tokens[0].equalsIgnoreCase("guid")) {
            	guid = tokens[1];
        	} else if(tokens[0].equalsIgnoreCase("eth1ip")) {
        		_privateIp = tokens[1];
        	}
        }

        if (port == null) {
            port = getProperty(null, "port");
        }

        _port = NumbersUtil.parseInt(port, 8250);
        
        _proxyPort = NumbersUtil.parseInt(getProperty(null, "consoleproxy.httpListenPort"), 443);

        if (workers == null) {
            workers = getProperty(null, "workers");
        }

        _workers = NumbersUtil.parseInt(workers, 5);

        if (host == null) {
            host = getProperty(null, "host");
        }

        if (host == null) {
            host = "localhost";
        }
        _host = host;
        
        if(zone != null)
        	_zone = zone;
        else
        	_zone = getProperty(null, "zone");
        if (_zone == null || (_zone.startsWith("@") && _zone.endsWith("@"))) {
           _zone = "default";
        }

        if(pod != null)
        	_pod = pod;
        else
        	_pod = getProperty(null, "pod");
        if (_pod == null || (_pod.startsWith("@") && _pod.endsWith("@"))) {
           _pod = "default";
        }

        if (_host == null || (_host.startsWith("@") && _host.endsWith("@"))) {
            throw new ConfigurationException("Host is not configured correctly: " + _host);
        }
        
        final String retries = getProperty(null, "ping.retries");
        _pingRetries  = NumbersUtil.parseInt(retries, 5);

        String value = getProperty(null, "developer");
        boolean developer = Boolean.parseBoolean(value);
        
        if(guid != null)
        	_guid = guid;
        else
        	_guid = getProperty(null, "guid");
        if (_guid == null) {
        	if (!developer) {
        		throw new ConfigurationException("Unable to find the guid");
        	}
	        _guid = MacAddress.getMacAddress().toString(":");
        }

        return true;
    }	

    private void launchAgentFromTypeInfo() throws ConfigurationException {
        String typeInfo = getProperty(null, "type");
        if (typeInfo == null) {
            s_logger.error("Unable to retrieve the type");
            throw new ConfigurationException("Unable to retrieve the type of this agent.");
        }
        s_logger.trace("Launching agent based on type=" + typeInfo);
    }    
    
    private void launchAgent() throws ConfigurationException {
        String resourceClassNames = getProperty(null, "resource");
        s_logger.trace("resource=" + resourceClassNames);
        if(resourceClassNames != null) {
        	launchAgentFromClassInfo(resourceClassNames);
        	return;
        }
        
        launchAgentFromTypeInfo();
    }    
    
    private void init(String[] args) throws ConfigurationException{
    	
        final ComponentLocator locator = ComponentLocator.getLocator("agent");
        
        final Class<?> c = this.getClass();
        _version = c.getPackage().getImplementationVersion();
        if (_version == null) {
            throw new CloudRuntimeException("Unable to find the implementation version of this agent");
        }
        s_logger.info("Implementation Version is " + _version);
    	
        parseCommand(args);
        
        _storage = locator.getManager(StorageComponent.class);
        if (_storage == null) {
            s_logger.info("Defaulting to using properties file for storage");
            _storage = new PropertiesStorage();
            _storage.configure("Storage", new HashMap<String, Object>());
        }

        // merge with properties from command line to let resource access command line parameters
        for(Map.Entry<String, Object> cmdLineProp : getCmdLineProperties().entrySet()) {
        	_properties.put(cmdLineProp.getKey(), cmdLineProp.getValue());
        }
        
        final Adapters adapters = locator.getAdapters(BackoffAlgorithm.class);
        final Enumeration en = adapters.enumeration();
        while (en.hasMoreElements()) {
            _backoff = (BackoffAlgorithm)en.nextElement();
            break;
        }
        if (en.hasMoreElements()) {
            s_logger.info("More than one backoff algorithm specified.  Using the first one ");
        }

        if (_backoff == null) {
            s_logger.info("Defaulting to the constant time backoff algorithm");
            _backoff = new ConstantTimeBackoff();
            _backoff.configure("ConstantTimeBackoff", new HashMap<String, Object>());
        }
    }	

    private void launchAgentFromClassInfo(String resourceClassNames) throws ConfigurationException {
    	String[] names = resourceClassNames.split("\\|");
    	for(String name: names) {
            Class<?> impl;
            try {
                impl = Class.forName(name);
                final Constructor<?> constructor = impl.getDeclaredConstructor();
                constructor.setAccessible(true);
                ServerResource resource = (ServerResource)constructor.newInstance();
                launchAgent(getNextAgentId(), resource);
            } catch (final ClassNotFoundException e) {
            	throw new ConfigurationException("Resource class not found: " + name);
            } catch (final SecurityException e) {
            	throw new ConfigurationException("Security excetion when loading resource: " + name);
            } catch (final NoSuchMethodException e) {
            	throw new ConfigurationException("Method not found excetion when loading resource: " + name);
            } catch (final IllegalArgumentException e) {
            	throw new ConfigurationException("Illegal argument excetion when loading resource: " + name);
            } catch (final InstantiationException e) {
            	throw new ConfigurationException("Instantiation excetion when loading resource: " + name);
            } catch (final IllegalAccessException e) {
            	throw new ConfigurationException("Illegal access exception when loading resource: " + name);
            } catch (final InvocationTargetException e) {
            	throw new ConfigurationException("Invocation target exception when loading resource: " + name);
            }
    	}
    }

    private void launchAgent(int localAgentId, ServerResource resource) throws ConfigurationException {
    	// we don't track agent after it is launched for now
    	Agent agent = new Agent(this, localAgentId, resource);
    	_agents.add(agent);
    	agent.start();
    }

    public synchronized int getNextAgentId() {
    	return _nextAgentId++;
    }
    
	private void run(String[] args) {
		
		try {
            System.setProperty("java.net.preferIPv4Stack","true");			
	        loadProperties();
	        init(args);
	
            String instance = getProperty(null, "instance");
            if (instance == null) {
                instance = "";
            } else {
                instance += ".";
            }

            // TODO need to do this check. For Agentshell running on windows needs different approach
            //final String run = "agent." + instance + "pid";
            //s_logger.debug("Checking to see if " + run + "exists.");
        	//ProcessUtil.pidCheck(run);	        
	        
            
            // TODO: For Hyper-V agent.properties need to be revamped to support multiple agents 
            // corresponding to multiple clusters but running on a SCVMM host
			
            // read the persistent storage and launch the agents
        	//launchAgent();

            // FIXME get rid of this approach of agent listening for boot strap commands from the management server

			// now listen for bootstrap request from the management server and launch agents 
			_connection = new NioServer("VmmAgentShell", _listenerPort, 1, this);
			_connection.start();
			s_logger.info("SCVMM agent is listening on port " +_listenerPort + " for bootstrap command from management server");
			while(_connection.isRunning());
        } catch(final ConfigurationException e) {
            s_logger.error("Unable to start agent: " + e.getMessage());
            System.out.println("Unable to start agent: " + e.getMessage());
            System.exit(ExitStatus.Configuration.value());
        } catch (final Exception e) {
            s_logger.error("Unable to start agent: ", e);
            System.out.println("Unable to start agent: " + e.getMessage());
            System.exit(ExitStatus.Error.value());
        }
	 }

	@Override
	public Task create(com.cloud.utils.nio.Task.Type type, Link link,
			byte[] data) {
		return new AgentBootStrapHandler(type, link, data);
	} 

   public void stop() {
   	_exit = true;
   	if(_consoleProxyMain != null) {
   		_consoleProxyMain.interrupt();
   	}
   }	
	
	public static void main(String[] args) {
		
		VmmAgentShell shell = new VmmAgentShell();
		Runtime.getRuntime().addShutdownHook(new ShutdownThread(shell));		
		shell.run(args);
	}   

	// class to handle the bootstrap command from the management server
	private class AgentBootStrapHandler extends Task {

		public AgentBootStrapHandler(Task.Type type, Link link, byte[] data) {
			super(type, link, data);
		}

		@Override
		protected void doTask(Task task) throws Exception {
			final Type type = task.getType();
			s_logger.info("recieved task of type "+ type.toString() +" to handle in BootStrapTakHandler");
			if (type == Task.Type.DATA)
			{
				final byte[] data = task.getData();
				final Request request = Request.parse(data);
				final Command cmd = request.getCommand();
				
				if (cmd instanceof StartupVMMAgentCommand) {

					StartupVMMAgentCommand vmmCmd = (StartupVMMAgentCommand) cmd;

					_zone = Long.toString(vmmCmd.getDataCenter());
					_cmdLineProperties.put("zone", _zone);

					_pod = Long.toString(vmmCmd.getPod());
					_cmdLineProperties.put("pod", _pod);

					_cluster = vmmCmd.getClusterName();
					_cmdLineProperties.put("cluster", _cluster);

					_guid = vmmCmd.getGuid();
					_cmdLineProperties.put("guid", _guid);

					_host = vmmCmd.getManagementServerIP();
					_port =  NumbersUtil.parseInt(vmmCmd.getport(), 8250);

					s_logger.info("Recieved boot strap command from management server with parameters " +
							" Zone:"+  _zone + " "+
							" Cluster:"+ _cluster + " "+
							" pod:"+_pod + " "+
							" host:"+ _host +" "+
							" port:"+_port);

					launchAgentFromClassInfo("com.cloud.hypervisor.hyperv.resource.HypervResource");
					
					// TODO: persist the info in agent.properties for agent restarts
				}
			}
		}
	}

    private static class ShutdownThread extends Thread {
    	VmmAgentShell _shell;
        public ShutdownThread(VmmAgentShell shell) {
            this._shell = shell;
        }
        
        @Override
        public void run() {
            _shell.stop();
        }
    }	
	
}