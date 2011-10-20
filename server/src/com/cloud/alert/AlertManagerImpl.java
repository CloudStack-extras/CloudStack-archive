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

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.ejb.Local;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.alert.dao.AlertDao;
import com.cloud.api.ApiDBUtils;
import com.cloud.capacity.Capacity;
import com.cloud.capacity.CapacityManager;
import com.cloud.capacity.CapacityVO;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.capacity.dao.CapacityDaoImpl.SummedCapacity;
import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.Vlan.VlanType;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.DataCenterIpAddressDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.storage.StorageManager;
import com.cloud.storage.StoragePoolVO;
import com.cloud.storage.dao.StoragePoolDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.SearchCriteria;
import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.smtp.SMTPSSLTransport;
import com.sun.mail.smtp.SMTPTransport;

@Local(value={AlertManager.class})
public class AlertManagerImpl implements AlertManager {
    private static final Logger s_logger = Logger.getLogger(AlertManagerImpl.class.getName());

    private static final long INITIAL_CAPACITY_CHECK_DELAY = 30L * 1000L; // thirty seconds expressed in milliseconds

    private static final DecimalFormat _dfPct = new DecimalFormat("###.##");
    private static final DecimalFormat _dfWhole = new DecimalFormat("########");

    private String _name = null;
    private EmailAlert _emailAlert;
    @Inject private AlertDao _alertDao;
    @Inject private HostDao _hostDao;
    @Inject protected StorageManager _storageMgr;
    @Inject protected CapacityManager _capacityMgr;
    @Inject private CapacityDao _capacityDao;
    @Inject private DataCenterDao _dcDao;
    @Inject private HostPodDao _podDao;
    @Inject private ClusterDao _clusterDao;
    @Inject private VolumeDao _volumeDao;
    @Inject private IPAddressDao _publicIPAddressDao;
    @Inject private DataCenterIpAddressDao _privateIPAddressDao;
    @Inject private StoragePoolDao _storagePoolDao;
    
    private Timer _timer = null;
    private float _cpuOverProvisioningFactor = 1;
    private long _capacityCheckPeriod = 60L * 60L * 1000L; // one hour by default
    private double _memoryCapacityThreshold = 0.75;
    private double _cpuCapacityThreshold = 0.75;
    private double _storageCapacityThreshold = 0.75;
    private double _storageAllocCapacityThreshold = 0.75;
    private double _publicIPCapacityThreshold = 0.75;
    private double _privateIPCapacityThreshold = 0.75;
    private double _secondaryStorageCapacityThreshold = 0.75; 
	private double _vlanCapacityThreshold = 0.75;
	private double _directNetworkPublicIpCapacityThreshold = 0.75;
    Map<Short,Double> _capacityTypeThresholdMap = new HashMap<Short, Double>();

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _name = name;

        ComponentLocator locator = ComponentLocator.getCurrentLocator();
        ConfigurationDao configDao = locator.getDao(ConfigurationDao.class);
        if (configDao == null) {
            s_logger.error("Unable to get the configuration dao.");
            return false;
        }

        Map<String, String> configs = configDao.getConfiguration("management-server", params);

        // set up the email system for alerts
        String emailAddressList = configs.get("alert.email.addresses");
        String[] emailAddresses = null;
        if (emailAddressList != null) {
            emailAddresses = emailAddressList.split(",");
        }

        String smtpHost = configs.get("alert.smtp.host");
        int smtpPort = NumbersUtil.parseInt(configs.get("alert.smtp.port"), 25);
        String useAuthStr = configs.get("alert.smtp.useAuth");
        boolean useAuth = ((useAuthStr == null) ? false : Boolean.parseBoolean(useAuthStr));
        String smtpUsername = configs.get("alert.smtp.username");
        String smtpPassword = configs.get("alert.smtp.password");
        String emailSender = configs.get("alert.email.sender");
        String smtpDebugStr = configs.get("alert.smtp.debug");
        boolean smtpDebug = false;
        if (smtpDebugStr != null) {
            smtpDebug = Boolean.parseBoolean(smtpDebugStr);
        }

        _emailAlert = new EmailAlert(emailAddresses, smtpHost, smtpPort, useAuth, smtpUsername, smtpPassword, emailSender, smtpDebug);

        String storageCapacityThreshold = configs.get("storage.capacity.threshold");
        String cpuCapacityThreshold = configs.get("cpu.capacity.threshold");
        String memoryCapacityThreshold = configs.get("memory.capacity.threshold");
        String storageAllocCapacityThreshold = configs.get("storage.allocated.capacity.threshold");
        String publicIPCapacityThreshold = configs.get("public.ip.capacity.threshold");
        String privateIPCapacityThreshold = configs.get("private.ip.capacity.threshold");
        String secondaryStorageCapacityThreshold = configs.get("secondarystorage.capacity.threshold");
        String vlanCapacityThreshold = configs.get("vlan.capacity.threshold");
        String directNetworkPublicIpCapacityThreshold = configs.get("directnetwork.public.ip.capacity.threshold");
        
        if (storageCapacityThreshold != null) {
            _storageCapacityThreshold = Double.parseDouble(storageCapacityThreshold);
        }
        if (storageAllocCapacityThreshold != null) {
            _storageAllocCapacityThreshold = Double.parseDouble(storageAllocCapacityThreshold);
        }
        if (cpuCapacityThreshold != null) {
            _cpuCapacityThreshold = Double.parseDouble(cpuCapacityThreshold);
        }
        if (memoryCapacityThreshold != null) {
            _memoryCapacityThreshold = Double.parseDouble(memoryCapacityThreshold);
        }
        if (publicIPCapacityThreshold != null) {
        	_publicIPCapacityThreshold = Double.parseDouble(publicIPCapacityThreshold);
        }
        if (privateIPCapacityThreshold != null) {
        	_privateIPCapacityThreshold = Double.parseDouble(privateIPCapacityThreshold);
        }
        if (secondaryStorageCapacityThreshold != null) {
            _secondaryStorageCapacityThreshold = Double.parseDouble(secondaryStorageCapacityThreshold);
        }
        if (vlanCapacityThreshold != null) {
            _vlanCapacityThreshold = Double.parseDouble(vlanCapacityThreshold);
        }
        if (directNetworkPublicIpCapacityThreshold != null) {
            _directNetworkPublicIpCapacityThreshold = Double.parseDouble(directNetworkPublicIpCapacityThreshold);
        }
        
        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_STORAGE, _storageCapacityThreshold);
        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED, _storageAllocCapacityThreshold);
        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_CPU, _cpuCapacityThreshold);
        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_MEMORY, _memoryCapacityThreshold);
        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_VIRTUAL_NETWORK_PUBLIC_IP, _publicIPCapacityThreshold);
        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_PRIVATE_IP, _privateIPCapacityThreshold);
        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_SECONDARY_STORAGE, _secondaryStorageCapacityThreshold);
        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_VLAN, _vlanCapacityThreshold);
        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_DIRECT_ATTACHED_PUBLIC_IP, _directNetworkPublicIpCapacityThreshold);
        
        String capacityCheckPeriodStr = configs.get("capacity.check.period");
        if (capacityCheckPeriodStr != null) {
            _capacityCheckPeriod = Long.parseLong(capacityCheckPeriodStr);
            if(_capacityCheckPeriod <= 0)
            	_capacityCheckPeriod = Long.parseLong(Config.CapacityCheckPeriod.getDefaultValue());
        }
        
        String cpuOverProvisioningFactorStr = configs.get("cpu.overprovisioning.factor");
        if (cpuOverProvisioningFactorStr != null) {
            _cpuOverProvisioningFactor = NumbersUtil.parseFloat(cpuOverProvisioningFactorStr,1);
            if(_cpuOverProvisioningFactor < 1){
            	_cpuOverProvisioningFactor = 1;
            }
        }

        _timer = new Timer("CapacityChecker");

        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean start() {
        _timer.schedule(new CapacityChecker(), INITIAL_CAPACITY_CHECK_DELAY, _capacityCheckPeriod);
        return true;
    }

    @Override
    public boolean stop() {
        _timer.cancel();
        return true;
    }

    @Override
    public void clearAlert(short alertType, long dataCenterId, long podId) {
        try {
            if (_emailAlert != null) {
                _emailAlert.clearAlert(alertType, dataCenterId, podId);
            }
        } catch (Exception ex) {
            s_logger.error("Problem clearing email alert", ex);
        }
    }

    @Override
    public void sendAlert(short alertType, long dataCenterId, Long podId, String subject, String body) {
        // TODO:  queue up these messages and send them as one set of issues once a certain number of issues is reached?  If that's the case,
        //         shouldn't we have a type/severity as part of the API so that severe errors get sent right away?
        try {
            if (_emailAlert != null) {
                _emailAlert.sendAlert(alertType, dataCenterId, podId, subject, body);
            }
        } catch (Exception ex) {
            s_logger.error("Problem sending email alert", ex);
        }
    }

    @Override @DB
    public void recalculateCapacity() {
        // FIXME: the right way to do this is to register a listener (see RouterStatsListener, VMSyncListener)
        //        for the vm sync state.  The listener model has connects/disconnects to keep things in sync much better
        //        than this model right now, so when a VM is started, we update the amount allocated, and when a VM
        //        is stopped we updated the amount allocated, and when VM sync reports a changed state, we update
        //        the amount allocated.  Hopefully it's limited to 3 entry points and will keep the amount allocated
        //        per host accurate.

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("recalculating system capacity");
        }
        
        // Calculate CPU and RAM capacities
        // 	get all hosts...even if they are not in 'UP' state
        List<HostVO> hosts = _hostDao.listByType(Host.Type.Routing);
        for (HostVO host : hosts) {
        	_capacityMgr.updateCapacityForHost(host);
        }
        
        // Calculate storage pool capacity
        List<StoragePoolVO> storagePools = _storagePoolDao.listAll();
        for (StoragePoolVO pool : storagePools) {
            long disk = 0l;
            Pair<Long, Long> sizes = _volumeDao.getCountAndTotalByPool(pool.getId());
            disk = sizes.second();
            if (pool.isShared()){
            	_storageMgr.createCapacityEntry(pool, Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED, disk);
            }else {
            	_storageMgr.createCapacityEntry(pool, Capacity.CAPACITY_TYPE_LOCAL_STORAGE, disk);
            }
        }       

        try {   

        	List<DataCenterVO> datacenters = _dcDao.listAll();
        	for (DataCenterVO datacenter : datacenters) {
        		long dcId = datacenter.getId();
		
		        //NOTE
		        //What happens if we have multiple vlans? Dashboard currently shows stats 
		        //with no filter based on a vlan
		        //ideal way would be to remove out the vlan param, and filter only on dcId
		        //implementing the same
        		
            	// Calculate new Public IP capacity for Virtual Network
            	s_logger.trace("Executing public ip capacity update for Virtual Network");
		        createOrUpdateIpCapacity(dcId, null, CapacityVO.CAPACITY_TYPE_VIRTUAL_NETWORK_PUBLIC_IP);
                s_logger.trace("Done with public ip capacity update for Virtual Network");
                
            	// Calculate new Public IP capacity for Direct Attached Network
            	s_logger.trace("Executing public ip capacity update for Direct Attached Network");
		        createOrUpdateIpCapacity(dcId, null, CapacityVO.CAPACITY_TYPE_DIRECT_ATTACHED_PUBLIC_IP);
                s_logger.trace("Done with public ip capacity update for Direct Attached Network");
                
                //Calculate VLAN's capacity
            	s_logger.trace("Executing VLAN capacity update");
            	createOrUpdateVlanCapacity(dcId);
            	s_logger.trace("Executing VLAN capacity update");
		    }

        

        	
	        // Calculate new Private IP capacity
	        List<HostPodVO> pods = _podDao.listAll();
	        for (HostPodVO pod : pods) {
	            long podId = pod.getId();
	            long dcId = pod.getDataCenterId();

            	s_logger.trace("Executing private ip capacity update");
	            createOrUpdateIpCapacity(dcId, podId, CapacityVO.CAPACITY_TYPE_PRIVATE_IP);
                s_logger.trace("Done with private ip capacity update");

	        }

        } catch (Exception ex) {        	
        	s_logger.error("Unable to start transaction for capacity update");
        }
    }
    
    private void createOrUpdateVlanCapacity(long dcId) {
    	
    	SearchCriteria<CapacityVO> capacitySC = _capacityDao.createSearchCriteria();

        List<CapacityVO> capacities = _capacityDao.search(capacitySC, null);
        capacitySC = _capacityDao.createSearchCriteria();
        capacitySC.addAnd("dataCenterId", SearchCriteria.Op.EQ, dcId);
        capacitySC.addAnd("capacityType", SearchCriteria.Op.EQ, Capacity.CAPACITY_TYPE_VLAN);
        capacities = _capacityDao.search(capacitySC, null);
		
       int totalVlans = _dcDao.countZoneVlans(dcId, false);
       int allocatedVlans = _dcDao.countZoneVlans(dcId, true);
        
        if (capacities.size() == 0){
        	CapacityVO newPublicIPCapacity = new CapacityVO(null, dcId, null, null, allocatedVlans, totalVlans, Capacity.CAPACITY_TYPE_VLAN);
            _capacityDao.persist(newPublicIPCapacity);
        }else if ( !(capacities.get(0).getUsedCapacity() == allocatedVlans 
        		&& capacities.get(0).getTotalCapacity() == totalVlans) ){
        	CapacityVO capacity = capacities.get(0);
        	capacity.setUsedCapacity(allocatedVlans);
        	capacity.setTotalCapacity(totalVlans);
            _capacityDao.update(capacity.getId(), capacity);        	
        }
        
        
	}

	public void createOrUpdateIpCapacity(Long dcId, Long podId, short capacityType){
        SearchCriteria<CapacityVO> capacitySC = _capacityDao.createSearchCriteria();

        List<CapacityVO> capacities = _capacityDao.search(capacitySC, null);
        capacitySC = _capacityDao.createSearchCriteria();
        capacitySC.addAnd("podId", SearchCriteria.Op.EQ, podId);
        capacitySC.addAnd("dataCenterId", SearchCriteria.Op.EQ, dcId);
        capacitySC.addAnd("capacityType", SearchCriteria.Op.EQ, capacityType);

        int totalIPs;
        int allocatedIPs;
        capacities = _capacityDao.search(capacitySC, null);
        if (capacityType == CapacityVO.CAPACITY_TYPE_PRIVATE_IP){
        	totalIPs = _privateIPAddressDao.countIPs(podId, dcId, false);
        	allocatedIPs = _privateIPAddressDao.countIPs(podId, dcId, true);
        }else if (capacityType == CapacityVO.CAPACITY_TYPE_VIRTUAL_NETWORK_PUBLIC_IP){
        	totalIPs = _publicIPAddressDao.countIPsForNetwork(dcId, false, VlanType.VirtualNetwork);
            allocatedIPs = _publicIPAddressDao.countIPsForNetwork(dcId, true, VlanType.VirtualNetwork);
        }else {
        	totalIPs = _publicIPAddressDao.countIPsForNetwork(dcId, false, VlanType.DirectAttached);
            allocatedIPs = _publicIPAddressDao.countIPsForNetwork(dcId, true, VlanType.DirectAttached);
        }
        
        if (capacities.size() == 0){
        	CapacityVO newPublicIPCapacity = new CapacityVO(null, dcId, podId, null, allocatedIPs, totalIPs, capacityType);
            _capacityDao.persist(newPublicIPCapacity);
        }else if ( !(capacities.get(0).getUsedCapacity() == allocatedIPs 
        		&& capacities.get(0).getTotalCapacity() == totalIPs) ){
        	CapacityVO capacity = capacities.get(0);
        	capacity.setUsedCapacity(allocatedIPs);
        	capacity.setTotalCapacity(totalIPs);
            _capacityDao.update(capacity.getId(), capacity);
        }
        
    }

    class CapacityChecker extends TimerTask {
        @Override
		public void run() {
            try {
            	checkForAlerts();
            } catch (Exception ex) {
                s_logger.error("Exception in CapacityChecker", ex);
            }
        }
    }
    
    
    public void checkForAlerts(){
    	
    	recalculateCapacity();

        // abort if we can't possibly send an alert...
        if (_emailAlert == null) {
            return;
        }
        
        //Get all datacenters, pods and clusters in the system.
        List<DataCenterVO> dataCenterList = _dcDao.listAll();
        List<ClusterVO> clusterList = _clusterDao.listAll();
        List<HostPodVO> podList = _podDao.listAll();
        //Get capacity types at different levels
        List<Short> dataCenterCapacityTypes = getCapacityTypesAtZoneLevel();         
        List<Short> podCapacityTypes = getCapacityTypesAtPodLevel();        
        List<Short> clusterCapacityTypes = getCapacityTypesAtClusterLevel();        
        
        // Generate Alerts for Zone Level capacities
        for(DataCenterVO dc : dataCenterList){
        	for (Short capacityType : dataCenterCapacityTypes){
        		List<SummedCapacity> capacity = _capacityDao.findCapacityBy(capacityType.intValue(), dc.getId(), null, null);
        		if (capacity == null || capacity.size() == 0){
        			continue;
        		}
        		double totalCapacity = capacity.get(0).getTotalCapacity(); 
                double usedCapacity =  capacity.get(0).getUsedCapacity();
                if (totalCapacity != 0 && usedCapacity/totalCapacity > _capacityTypeThresholdMap.get(capacityType)){
                	generateEmailAlert(dc, null, null, totalCapacity, usedCapacity, capacityType);
                }
        	}
        }
        
        // Generate Alerts for Pod Level capacities
        for( HostPodVO pod : podList){
        	for (Short capacityType : podCapacityTypes){
        		List<SummedCapacity> capacity = _capacityDao.findCapacityBy(capacityType.intValue(), pod.getDataCenterId(), pod.getId(), null);
        		if (capacity == null || capacity.size() == 0){
        			continue;
        		}
        		double totalCapacity = capacity.get(0).getTotalCapacity(); 
                double usedCapacity =  capacity.get(0).getUsedCapacity();
                if (totalCapacity != 0 && usedCapacity/totalCapacity > _capacityTypeThresholdMap.get(capacityType)){
                	generateEmailAlert(ApiDBUtils.findZoneById(pod.getDataCenterId()), pod, null, 
                			totalCapacity, usedCapacity, capacityType);
                }
        	}
        }
        
        // Generate Alerts for Cluster Level capacities
        for( ClusterVO cluster : clusterList){
        	for (Short capacityType : clusterCapacityTypes){
        		List<SummedCapacity> capacity = _capacityDao.findCapacityBy(capacityType.intValue(), cluster.getDataCenterId(), null, cluster.getId());
        		if (capacity == null || capacity.size() == 0){
        			continue;
        		}
        		double totalCapacity = capacity.get(0).getTotalCapacity(); 
                double usedCapacity =  capacity.get(0).getUsedCapacity();
                if (totalCapacity != 0 && usedCapacity/totalCapacity > _capacityTypeThresholdMap.get(capacityType)){
                	generateEmailAlert(ApiDBUtils.findZoneById(cluster.getDataCenterId()), ApiDBUtils.findPodById(cluster.getPodId()), cluster,
                			totalCapacity, usedCapacity, capacityType);
                }
        	}
        }
        
    }
    
    private void generateEmailAlert(DataCenterVO dc, HostPodVO pod, ClusterVO cluster, double totalCapacity, double usedCapacity, short capacityType){
    	
    	String msgSubject = null;
        String msgContent = null;
        String totalStr;
        String usedStr;
        String pctStr = formatPercent(usedCapacity/totalCapacity);
        
    	switch (capacityType) {
        case CapacityVO.CAPACITY_TYPE_MEMORY:
            msgSubject = "System Alert: Low Available Memory in pod " +pod.getName()+ " of availablity zone " + dc.getName();
            totalStr = formatBytesToMegabytes(totalCapacity);
            usedStr = formatBytesToMegabytes(usedCapacity);
            msgContent = "System memory is low, total: " + totalStr + " MB, used: " + usedStr + " MB (" + pctStr + "%)";
            break;
        case CapacityVO.CAPACITY_TYPE_CPU:
            msgSubject = "System Alert: Low Unallocated CPU in pod " +pod.getName()+ " of availablity zone " + dc.getName();
            totalStr = _dfWhole.format(totalCapacity);
            usedStr = _dfWhole.format(usedCapacity);
            msgContent = "Unallocated CPU is low, total: " + totalStr + " Mhz, used: " + usedStr + " Mhz (" + pctStr + "%)";
            break;
        case CapacityVO.CAPACITY_TYPE_STORAGE:
            msgSubject = "System Alert: Low Available Storage in pod " +pod.getName()+ " of availablity zone " + dc.getName();
            totalStr = formatBytesToMegabytes(totalCapacity);
            usedStr = formatBytesToMegabytes(usedCapacity);
            msgContent = "Available storage space is low, total: " + totalStr + " MB, used: " + usedStr + " MB (" + pctStr + "%)";
            break;
        case CapacityVO.CAPACITY_TYPE_STORAGE_ALLOCATED:
            msgSubject = "System Alert: Remaining unallocated Storage is low in pod " +pod.getName()+ " of availablity zone " + dc.getName();
            totalStr = formatBytesToMegabytes(totalCapacity);
            usedStr = formatBytesToMegabytes(usedCapacity);
            msgContent = "Unallocated storage space is low, total: " + totalStr + " MB, allocated: " + usedStr + " MB (" + pctStr + "%)";
            break;
        case CapacityVO.CAPACITY_TYPE_VIRTUAL_NETWORK_PUBLIC_IP:
            msgSubject = "System Alert: Number of unallocated public IPs is low in availablity zone " + dc.getName();
            totalStr = Double.toString(totalCapacity);
            usedStr = Double.toString(usedCapacity);
            msgContent = "Number of unallocated public IPs is low, total: " + totalStr + ", allocated: " + usedStr + " (" + pctStr + "%)";
            break;
        case CapacityVO.CAPACITY_TYPE_PRIVATE_IP:        	
        	msgSubject = "System Alert: Number of unallocated private IPs is low in pod " +pod.getName()+ " of availablity zone " + dc.getName();
        	totalStr = Double.toString(totalCapacity);
            usedStr = Double.toString(usedCapacity);
        	msgContent = "Number of unallocated private IPs is low, total: " + totalStr + ", allocated: " + usedStr + " (" + pctStr + "%)";
        	break;
        	
        case CapacityVO.CAPACITY_TYPE_SECONDARY_STORAGE:        	
        	msgSubject = "System Alert: Low Available Storage in availablity zone " + dc.getName();
        	totalStr = Double.toString(totalCapacity);
            usedStr = Double.toString(usedCapacity);
        	msgContent = "Available secondary storage space is low, total: " + totalStr + " MB, used: " + usedStr + " MB (" + pctStr + "%)";
        	break;        
        }
    	
    	try {
			_emailAlert.sendAlert(capacityType, dc.getId(), null, msgSubject, msgContent);
    	} catch (Exception ex) {
            s_logger.error("Exception in CapacityChecker", ex);        
		}
    }
    
    private List<Short> getCapacityTypesAtZoneLevel(){
    	
    	List<Short> dataCenterCapacityTypes = new ArrayList<Short>();
    	dataCenterCapacityTypes.add(Capacity.CAPACITY_TYPE_VIRTUAL_NETWORK_PUBLIC_IP);
    	dataCenterCapacityTypes.add(Capacity.CAPACITY_TYPE_DIRECT_ATTACHED_PUBLIC_IP);
    	dataCenterCapacityTypes.add(Capacity.CAPACITY_TYPE_SECONDARY_STORAGE);
    	dataCenterCapacityTypes.add(Capacity.CAPACITY_TYPE_VLAN);
		return dataCenterCapacityTypes;
    	
    }
    
    private List<Short> getCapacityTypesAtPodLevel(){
    	
    	List<Short> podCapacityTypes = new ArrayList<Short>();
    	podCapacityTypes.add(Capacity.CAPACITY_TYPE_PRIVATE_IP);    	
		return podCapacityTypes;
    	
    }
    
    private List<Short> getCapacityTypesAtClusterLevel(){
    	
    	List<Short> clusterCapacityTypes = new ArrayList<Short>();
    	clusterCapacityTypes.add(Capacity.CAPACITY_TYPE_CPU);
    	clusterCapacityTypes.add(Capacity.CAPACITY_TYPE_MEMORY);
    	clusterCapacityTypes.add(Capacity.CAPACITY_TYPE_STORAGE);
    	clusterCapacityTypes.add(Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED);
		return clusterCapacityTypes;
    	
    }

    class EmailAlert {
        private Session _smtpSession;
        private InternetAddress[] _recipientList;
        private final String _smtpHost;
        private int _smtpPort = -1;
        private boolean _smtpUseAuth = false;
        private final String _smtpUsername;
        private final String _smtpPassword;
        private final String _emailSender;

        public EmailAlert(String[] recipientList, String smtpHost, int smtpPort, boolean smtpUseAuth, final String smtpUsername, final String smtpPassword, String emailSender, boolean smtpDebug) {
            if (recipientList != null) {
                _recipientList = new InternetAddress[recipientList.length];
                for (int i = 0; i < recipientList.length; i++) {
                    try {
                        _recipientList[i] = new InternetAddress(recipientList[i], recipientList[i]);
                    } catch (Exception ex) {
                        s_logger.error("Exception creating address for: " + recipientList[i], ex);
                    }
                }
            }

            _smtpHost = smtpHost;
            _smtpPort = smtpPort;
            _smtpUseAuth = smtpUseAuth;
            _smtpUsername = smtpUsername;
            _smtpPassword = smtpPassword;
            _emailSender = emailSender;

            if (_smtpHost != null) {
                Properties smtpProps = new Properties();
                smtpProps.put("mail.smtp.host", smtpHost);
                smtpProps.put("mail.smtp.port", smtpPort);
                smtpProps.put("mail.smtp.auth", ""+smtpUseAuth);
                if (smtpUsername != null) {
                    smtpProps.put("mail.smtp.user", smtpUsername);
                }

                smtpProps.put("mail.smtps.host", smtpHost);
                smtpProps.put("mail.smtps.port", smtpPort);
                smtpProps.put("mail.smtps.auth", ""+smtpUseAuth);
                if (smtpUsername != null) {
                    smtpProps.put("mail.smtps.user", smtpUsername);
                }

                if ((smtpUsername != null) && (smtpPassword != null)) {
                    _smtpSession = Session.getInstance(smtpProps, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(smtpUsername, smtpPassword);
                        }
                    });
                } else {
                    _smtpSession = Session.getInstance(smtpProps);
                }
                _smtpSession.setDebug(smtpDebug);
            } else {
                _smtpSession = null;
            }
        }

        // TODO:  make sure this handles SSL transport (useAuth is true) and regular
        public void sendAlert(short alertType, long dataCenterId, Long podId, String subject, String content) throws MessagingException, UnsupportedEncodingException {
            AlertVO alert = null;
            if ((alertType != AlertManager.ALERT_TYPE_HOST) &&
                (alertType != AlertManager.ALERT_TYPE_USERVM) &&
                (alertType != AlertManager.ALERT_TYPE_DOMAIN_ROUTER) &&
                (alertType != AlertManager.ALERT_TYPE_CONSOLE_PROXY) &&
                (alertType != AlertManager.ALERT_TYPE_STORAGE_MISC) &&
                (alertType != AlertManager.ALERT_TYPE_MANAGMENT_NODE)) {
                alert = _alertDao.getLastAlert(alertType, dataCenterId, podId);
            }

            if (alert == null) {
                // set up a new alert
                AlertVO newAlert = new AlertVO();
                newAlert.setType(alertType);
                newAlert.setSubject(subject);
                newAlert.setPodId(podId);
                newAlert.setDataCenterId(dataCenterId);
                newAlert.setSentCount(1); // initialize sent count to 1 since we are now sending an alert
                newAlert.setLastSent(new Date());
                _alertDao.persist(newAlert);
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Have already sent: " + alert.getSentCount() + " emails for alert type '" + alertType + "' -- skipping send email");
                }
                return;
            }

            if (_smtpSession != null) {
                SMTPMessage msg = new SMTPMessage(_smtpSession);
                msg.setSender(new InternetAddress(_emailSender, _emailSender));
                msg.setFrom(new InternetAddress(_emailSender, _emailSender));
                for (InternetAddress address : _recipientList) {
                    msg.addRecipient(RecipientType.TO, address);
                }
                msg.setSubject(subject);
                msg.setSentDate(new Date());
                msg.setContent(content, "text/plain");
                msg.saveChanges();

                SMTPTransport smtpTrans = null;
                if (_smtpUseAuth) {
                    smtpTrans = new SMTPSSLTransport(_smtpSession, new URLName("smtp", _smtpHost, _smtpPort, null, _smtpUsername, _smtpPassword));
                } else {
                    smtpTrans = new SMTPTransport(_smtpSession, new URLName("smtp", _smtpHost, _smtpPort, null, _smtpUsername, _smtpPassword));
                }
                smtpTrans.connect();
                smtpTrans.sendMessage(msg, msg.getAllRecipients());
                smtpTrans.close();
            }
        }

        public void clearAlert(short alertType, long dataCenterId, Long podId) {
            if (alertType != -1) {
                AlertVO alert = _alertDao.getLastAlert(alertType, dataCenterId, podId);
                if (alert != null) {
                    AlertVO updatedAlert = _alertDao.createForUpdate();
                    updatedAlert.setResolved(new Date());
                    _alertDao.update(alert.getId(), updatedAlert);
                }
            }
        }
    }

    private static String formatPercent(double percentage) {
        return _dfPct.format(percentage*100);
    }

    private static String formatBytesToMegabytes(double bytes) {
        double megaBytes = (bytes / (1024 * 1024));
        return _dfWhole.format(megaBytes);
    }
}
