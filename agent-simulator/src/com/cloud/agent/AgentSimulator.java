/**
 * *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
*
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

package com.cloud.agent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import com.cloud.utils.NumbersUtil;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.backoff.BackoffAlgorithm;
import com.cloud.utils.backoff.impl.ConstantTimeBackoff;
import com.cloud.utils.exception.CloudRuntimeException;

@Deprecated
public class AgentSimulator implements MultiCasterListener, IAgentShell {
    private static final Logger s_logger = Logger.getLogger(AgentSimulator.class);
    
    public static final int DEFAULT_HOST_MEM_SIZE_MB = 4000;			// 4G, unit of Mbytes
    public static final int DEFAULT_HOST_CPU_CORES = 4;					// 2 dual core CPUs (2 x 2)
    public static final int DEFAULT_HOST_STORAGE_SIZE_MB = 500000;		// 500G, unit of Mbytes
    public static final int DEFAULT_HOST_SPEED_MHZ = 1000;				// 1 GHz CPUs
    
    public static final String SIMULATOR_CASTER_ADDRESS = "239.192.1.1"; //TODO: > .properties file
    public static final int SIMULATOR_CASTER_PORT = 5777;                //TODO: > .properties file  
    
    private static AgentSimulator s_instance;
    
    // c - computing agent
    // r - routing agent
    // s - storage agent
    
    // routing and computing host have been combined
    private String agentTypeSequence = "rs";
    
    private int agentCount = 2;
    private final List<AgentContainer> agentContainers = new ArrayList<AgentContainer>();
    private int nextAgentId = 1;
    
    private int runId = 0;
    private String testCase="DEFAULT";
    private final MultiCaster caster = new MultiCaster();
    
    private final Properties properties = new Properties();
    
    private final BackoffAlgorithm backoff;
    
    private volatile boolean exit = false;
    
    public enum AgentType {
    	Computing(0),
    	Routing(1),
    	Storage(2);
    	
    	int value;
    	AgentType(int value) {
    		this.value = value;
    	}
    	
    	public int value() {
    		return value;
    	}
    }

    public enum ExitStatus {
        Normal(0), // Normal status = 0.
        Upgrade(65), // Exiting for upgrade.
        Configuration(66), // Exiting due to configuration problems.
        Error(67); // Exiting because of error.
        
        int value;
        ExitStatus(int value) {
            this.value = value;
        }
        
        public int value() {
            return value;
        }
    }
    
    public AgentSimulator() {
        backoff = new ConstantTimeBackoff();
    }
    
    @Override
	public Map<String, Object> getCmdLineProperties() {
		return new HashMap<String, Object>();
	}

    @Override
    public Properties getProperties() {
    	return properties;
    }
    
    @Override
    public String getPersistentProperty(String prefix, String name) {
    	// don't need to support at simulator
    	return null;
    }
    
    @Override
    public void setPersistentProperty(String prefix, String name, String value) {
    }
    
    @Override
    public String getHost() {
    	return properties.getProperty("host");
    }
    
    @Override
    public String getPrivateIp() {
    	return null;
    }
    
    @Override
    public int getPort() {
    	return NumbersUtil.parseInt(properties.getProperty("port"), 8250);
    }
    
    @Override
    public int getWorkers() {
    	return NumbersUtil.parseInt(properties.getProperty("workers"), 3);
    }
    
    @Override
    public int getProxyPort() {
    	return 443;
    }
    
    @Override
    public String getGuid() {
    	// guid is implemented separately at instance level at simulator
    	return "";
    }
    
    @Override
    public String getZone() {
    	return properties.getProperty("zone");
    }
    
    @Override
    public String getPod() {
    	return properties.getProperty("pod");
    }
    
    @Override
    public BackoffAlgorithm getBackoffAlgorithm() {
    	return backoff;
    }
    
    @Override
    public int getPingRetries() {
    	return 5;
    }
    
    @Override
    public void upgradeAgent(final String url) {
    	// not supported in agent simulator
    	System.out.println("Management server requires agent to be upgraded, please change agent-update.properties in Tomcat to 0.0.0.0 for simulator run");
    	System.exit(1);
    }
    
    @Override
    public String getVersion() {
    	return "2.2.1.0";
    }
    
    public int getHostMemSizeInMB() {
    	return DEFAULT_HOST_MEM_SIZE_MB;
    }
    
    public int getHostCpuCores() {
    	return DEFAULT_HOST_CPU_CORES;
    }
    
    public int getHostCpuSpeedInMHz() {
    	return DEFAULT_HOST_SPEED_MHZ;
    }
    
    public int getHostStorageInMB() {
    	return DEFAULT_HOST_STORAGE_SIZE_MB;
    }
    
    public int getRunId() {
    	return runId;
    }
    
    public String getTestCase() {
    	return testCase;
    }
    
    public static AgentSimulator getInstance() {
    	if(s_instance == null)
    		s_instance = new AgentSimulator();
    		
    	return s_instance;
    }
    
    private static void configLog4j() {
        File file = PropertiesUtil.findConfigFile("log4j-cloud.xml");
        if (file != null) {
            System.out.println("Using log4j-cloud.xml configuration file : " + file.getAbsolutePath());
            DOMConfigurator.configureAndWatch(file.getAbsolutePath());
        } else {
            file = PropertiesUtil.findConfigFile("log4j-cloud.properties");
            if (file != null) {
                System.out.println("Using log4j-cloud.properties configuration file : " + file.getAbsolutePath());
                PropertyConfigurator.configureAndWatch(file.getAbsolutePath());
            }
        }
    }
    
	public void init(String[] args) throws ConfigurationException {
		s_logger.info("Simulator started");

		loadProperties();

		// parse after property loading, give it chance for command line
		// parameters to override settings in .properties file
		parseCommand(args);
		validateConfiguration();

		try {
			caster.addListener(this);
			caster.start(null, SIMULATOR_CASTER_ADDRESS, SIMULATOR_CASTER_PORT);
		} catch (SocketException e1) {
			s_logger.warn("Unable to start simulator multi caster");
		}

		synchronized (this) {
			for (int i = 0; i < agentCount; i++) {
				AgentContainer agent = new AgentContainer(this,
						getNextAgentId(), getAgentType(i));
				agentContainers.add(agent);
				agent.start();
			}
		}

		try {
			while (!exit)
				Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		s_logger.info("Simulator stopped");
	}
    
    public void stop() {
    	exit = true;
    	caster.stop();
    }
    
    public void castSimulatorCmd(SimulatorCmd cmd) {
    	
    	try {
	    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    	ObjectOutputStream os = new ObjectOutputStream(bos);
	    	os.writeObject(cmd);
	    	os.flush();
	    	os.close();
	    	bos.close();

	    	if(s_logger.isDebugEnabled())
	    		s_logger.debug("Cast simulator command, " + cmd.toString());
	    	
	    	byte[] buf = bos.toByteArray();
	    	caster.cast(buf, 0, buf.length, InetAddress.getByName(SIMULATOR_CASTER_ADDRESS),
    			SIMULATOR_CASTER_PORT);
    	} catch(IOException e) {
    		s_logger.warn("Unexpected exception ", e);
    	}
    }
    
    private void handleSimulatorCmd(SimulatorCmd cmd) {
    	// silently drop command from other test cases
    	if(!cmd.getTestCase().equals(testCase)) {
    		if(s_logger.isTraceEnabled())
    			s_logger.trace("Drop simulator command from other test case: " + cmd.getTestCase());
    		return;
    	}

    	if(cmd instanceof SimulatorMigrateVmCmd) {
	    	SimulatorMigrateVmCmd simulatorCmd = (SimulatorMigrateVmCmd)cmd;
	    	
	    	AgentContainer container =  getAgentContainerByHostIp(simulatorCmd.getDestIp());
	    	if(container != null) {
	    		VmMgr vmMgr = container.getVmMgr();
	    		
	    		// most fields are not used in mem-based VmMgr, I'll just pass the parameter name inside them
//	    		vmMgr.startVM(simulatorCmd.getVmName(), "vnetId", "gateway", "dns", "privateIP",
//	    			"privateMac", "privateMask",
//	    			"publicIP", "publicMac",
//	    			"publicMask",
//	    			simulatorCmd.getCpuCount(),
//	    			simulatorCmd.getUtilization(),
//	    			simulatorCmd.getRamSize(),
//	    			"localPath", "vncPassword");
//	    		
	    		if(s_logger.isDebugEnabled())
	    			s_logger.debug("VM " + simulatorCmd.getVmName() + " has been migrated to " + container.getHostPrivateIp());
	    	} else {
	    		if(s_logger.isDebugEnabled())
	    			s_logger.debug("Unable to find agent host with IP: " + simulatorCmd.getDestIp());
	    	}
    	}
    }
    
    private AgentContainer getAgentContainerByHostIp(String strHostIp) {
    	synchronized(this) {
    		for(AgentContainer container : agentContainers) {
    			if(container.getHostPrivateIp().equals(strHostIp))
    				return container;
    		}
    	}
    	return null;
    }
    
	public void onMultiCasting(byte[] data, int off, int len, InetAddress addrFrom) {
		
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data, off, len);
			ObjectInputStream is = new ObjectInputStream(bis);
			Object cmd = is.readObject();
			is.close();
			bis.close();
			if(cmd != null && cmd instanceof SimulatorCmd) {
				handleSimulatorCmd((SimulatorCmd)cmd);
			} else {
				s_logger.warn("Unrecogined command received");
			}
		} catch(IOException e) {
    		s_logger.warn("Unexpected exception ", e);
		} catch(ClassNotFoundException e) {
    		s_logger.warn("Unexpected exception ", e);
		} catch(Throwable e) {
    		s_logger.warn("Unexpected exception ", e);
		}
	}
    
    public synchronized int getNextAgentId() {
    	return nextAgentId++;
    }
    
    public AgentType getAgentType(int iteration) {
    	switch(agentTypeSequence.charAt(iteration % agentTypeSequence.length())) {
    	case 'c' :
        	return AgentType.Computing;
    		
    	case 'r' :
        	return AgentType.Routing;
        	
    	case 's' :
        	return AgentType.Storage;
        	
    	default :
    		s_logger.error("Invalid agent type charater, default to computing agent");
    		break;
    	}
    	return AgentType.Computing;
    }
    
	private void loadProperties() throws ConfigurationException {
		final File file = PropertiesUtil.findConfigFile("agent.properties");
		if (file == null) {
			throw new ConfigurationException("Unable to find agent.properties.");
		}

		s_logger.info("agent.properties found at " + file.getAbsolutePath());

		if (file != null) {
			try {
				properties.load(new FileInputStream(file));
			} catch (FileNotFoundException ex) {
				throw new CloudRuntimeException("Cannot find the file: "
						+ file.getAbsolutePath(), ex);

			} catch (IOException ex) {
				throw new CloudRuntimeException("IOException in reading "
						+ file.getAbsolutePath(), ex);
			}
		}
	}
    
    private void parseCommand(String[] args) {
    	if(args != null) {
    		for(int i = 0; i < args.length; i++) {
    			if(args[i].equals("-h")) {
    				if(i > args.length - 1) {
    					s_logger.error("Invalid command input for parameter -h");
    		            System.exit(ExitStatus.Error.value());
    				}
    				
    				properties.setProperty("host", args[i+1]);
    				i++;
    			} else if (args[i].equals("-c")) {
    				if(i > args.length - 1) {
    					s_logger.error("Invalid command input for parameter -c");
    		            System.exit(ExitStatus.Error.value());
    				}
    				
    				agentCount = Integer.parseInt(args[i+1]);
    				i++;
    			} else if (args[i].equals("-z")) {
    				if(i > args.length - 1) {
    					s_logger.error("Invalid command input for parameter -z");
    		            System.exit(ExitStatus.Error.value());
    				}
    				properties.setProperty("zone", args[i+1]);
    				i++;
    			} else if (args[i].equals("-p")) {
    				if(i > args.length - 1) {
    					s_logger.error("Invalid command input for parameter -p");
    		            System.exit(ExitStatus.Error.value());
    				}
    				properties.setProperty("pod", args[i+1]);
    				
    				i++;
    			} else if (args[i].equals("-t")) {
    				if(i > args.length - 1) {
    					s_logger.error("Invalid command input for parameter -t");
    		            System.exit(ExitStatus.Error.value());
    				}
    				agentTypeSequence = args[i+1];
    				
    				i++;
    			} else if (args[i].equals("-i")) {
    				if(i > args.length - 1) {
    					s_logger.error("Invalid command input for parameter -i");
    		            System.exit(ExitStatus.Error.value());
    				}
    				
    				runId = Integer.parseInt(args[i+1]);
    				i++;
    			} else if (args[i].equals("-s")) {
    				if(i > args.length - 1) {
    					s_logger.error("Invalid command input for parameter -i");
    		            System.exit(ExitStatus.Error.value());
    				}
    				
    				testCase = args[i+1];
    				i++;
    			}
    		}
    	}
    }
    
    private void validateConfiguration() throws ConfigurationException {
        String zone = properties.getProperty("zone");
        if (zone == null || (zone.startsWith("@") && zone.endsWith("@"))) {
            throw new ConfigurationException("Zone is not configured correctly: " + zone);
        }
        
        String pod = properties.getProperty("pod");
        if (pod == null || (pod.startsWith("@") && pod.endsWith("@"))) {
            throw new ConfigurationException("Pod is not configured correctly: " + pod);
        }
        
        String host = properties.getProperty("host");
        if (host != null && (host.startsWith("@") && host.endsWith("@"))) {
            throw new ConfigurationException("Host is not configured correctly: " + host);
        }
    }
    
    public static void main(String[] args) {
    	configLog4j();
    	
    	AgentSimulator simulator = AgentSimulator.getInstance();
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(simulator));
    	try {
    		simulator.init(args);
    	} catch (Throwable e) {
    		s_logger.error("Unexpected exception ", e);
            System.exit(ExitStatus.Error.value());
    	}
    }
    
    private static class ShutdownThread extends Thread {
        AgentSimulator agent;
        public ShutdownThread(AgentSimulator agent) {
            this.agent = agent;
        }
        
        @Override
        public void run() {
            agent.stop();
        }
    }
}
