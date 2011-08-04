package com.cloud.configuration;

import com.cloud.agent.manager.SimulatorManagerImpl;
import com.cloud.ha.HighAvailabilityManagerExtImpl;
import com.cloud.network.NetworkUsageManagerImpl;
import com.cloud.secstorage.PremiumSecondaryStorageManagerImpl;

public class SimulatorComponentLibrary extends PremiumComponentLibrary {
	  @Override
	    protected void populateManagers() {
	        addManager("secondary storage vm manager", PremiumSecondaryStorageManagerImpl.class);
	        addManager("HA Manager", HighAvailabilityManagerExtImpl.class);
//	        addManager("VMWareManager", VmwareManagerImpl.class);
//	        addManager("ExternalNetworkManager", ExternalNetworkManagerImpl.class);
//	        addManager("JuniperSrxManager", JuniperSrxManagerImpl.class);
//	        addManager("F5BigIpManager", F5BigIpManagerImpl.class);
//	        addManager("BareMetalVmManager", BareMetalVmManagerImpl.class);
//	        addManager("ExternalDhcpManager", ExternalDhcpManagerImpl.class);
	        addManager("SimulatorManager", SimulatorManagerImpl.class);
//	        addManager("PxeServerManager", PxeServerManagerImpl.class);
//	        addManager("NetworkDeviceManager", NetworkDeviceManagerImpl.class);
	        addManager("NetworkUsageManager", NetworkUsageManagerImpl.class);
//	        addManager("NetappManager", NetappManagerImpl.class);
	    }
}
