/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

public class MockVmMetrics implements Runnable {
	private static final Logger s_logger = Logger.getLogger(MockVmMetrics.class);
	
	private String vmName;
	
	//the maximum number of network interfaces to a VM (should be 1)
	public final int MAX_INTERFACES=1;
	
	//the maximum number of disks to a VM 
	public final int MAX_DISKS=8;
	
	//the last calculated traffic speed (transmit) per interface
	private Map<String, Double> netTxKBps = new HashMap<String, Double>();
	
	//the last calculated traffic speed (receive) per interface
	private Map<String, Double> netRxKBps = new HashMap<String, Double>();
	
	//the last calculated disk write speed per disk (Bytes Per Second)
	private Map<String, Double> diskWriteKBytesPerSec = new HashMap<String, Double>();
	
	//the last calculated disk read speed per disk (Bytes Per Second)
	private Map<String, Double> diskReadKBytesPerSec = new HashMap<String, Double>();
	
	//Total Bytes Transmitted on network interfaces
	private Map<String, Long> netTxTotalBytes = new HashMap<String, Long>();
	
	//Total Bytes Received on network interfaces
	private Map<String, Long> netRxTotalBytes = new HashMap<String, Long>();
	
	//Total Bytes read per disk
	private Map<String, Long> diskReadTotalBytes = new HashMap<String, Long>();

	//Total Bytes written per disk
	private Map<String, Long> diskWriteTotalBytes = new HashMap<String, Long>();
	
	//CPU time in seconds
	private Double cpuSeconds = new Double(0.0);
	
	//CPU percentage
	private Float cpuPercent = new Float(0.0);
	
	private Map<String, String> diskMap = new HashMap<String, String>();

	private Map<String, String> vifMap = new HashMap<String, String>();
	
	private Map<String, Long> diskStatTimestamp = new HashMap<String, Long>();
	private Map<String, Long> netStatTimestamp = new HashMap<String, Long>();
	
	private long cpuStatTimestamp = 0L;
	
	private ScheduledFuture<?> future;
	private boolean stopped = false;
	private Random randSeed = new Random();

	public MockVmMetrics(String vmName) {
		this.vmName = vmName;
		vifMap.put("eth0", "eth0");
		vifMap.put("eth1", "eth1");
		vifMap.put("eth2", "eth2");
		
		Long networkStart = 0L;
		netTxTotalBytes.put("eth0", networkStart);
		netRxTotalBytes.put("eth0", networkStart);
		
		netTxTotalBytes.put("eth1", networkStart);
		netRxTotalBytes.put("eth1", networkStart);
		
		netTxTotalBytes.put("eth2", networkStart);
		netRxTotalBytes.put("eth2", networkStart);		
	}
	
	private int getIncrementor() {
		return randSeed.nextInt(100);
	}
	
	@Override
	public void run() {
		if(s_logger.isDebugEnabled()) {
			s_logger.debug("Generating MockVM metrics");
		}
		for (Map.Entry<String, Long> entry : netRxTotalBytes.entrySet()) {
			entry.setValue(entry.getValue() + getIncrementor());		
		}
		
		for (Map.Entry<String, Long> entry : netTxTotalBytes.entrySet()) {
			entry.setValue(entry.getValue() + getIncrementor());
		}
	}
	
	public String getVmName() {
		return vmName;
	}
	
	public Map<String, Double> getNetTxKBps() {
		return netTxKBps;
	}
	
	public Map<String, Double> getNetRxKBps() {
		return netRxKBps;
	}

	public Map<String, Double> getDiskWriteBytesPerSec() {
		return diskWriteKBytesPerSec;
	}
	
	public Map<String, Double> getDiskReadBytesPerSec() {
		return diskReadKBytesPerSec;
	}
	
	public  Map<String, Long> getNetTxTotalBytes() {
		return netTxTotalBytes;
	}

	public Map<String, Long> getNetRxTotalBytes() {
		return netRxTotalBytes;
	}
	
	public Map<String, Long> getDiskReadTotalBytes() {
		return diskReadTotalBytes;
	}

	public Map<String, Long> getDiskWriteTotalBytes() {
		return diskWriteTotalBytes;
	}
	
	public Double getNetTxKBps(String intf) {
		return netTxKBps.get(intf);
	}

	public Double getNetRxKBps(String intf) {
		return netRxKBps.get(intf);
	}
	
	public Double getDiskWriteKBytesPerSec(String disk) {
		return diskWriteKBytesPerSec.get(disk);
	}

	public Double getDiskReadKBytesPerSec(String disk) {
		return diskReadKBytesPerSec.get(disk);
	}
	
	public Long getNetTxTotalBytes(String intf) {
		return netTxTotalBytes.get(intf);
	}

	public Long getNetRxTotalBytes(String intf) {
		return netRxTotalBytes.get(intf);
	}
	
	public Long getDiskReadTotalBytes(String disk) {
		return diskReadTotalBytes.get(disk);
	}

	public Long getDiskWriteTotalBytes(String disk) {
		return diskWriteTotalBytes.get(disk);
	}
	
	public Double getCpuSeconds() {
		return cpuSeconds;
	}

	public Map<String, String> getDiskMap() {
		return diskMap;
	}

	public Float getCpuPercent() {
		return cpuPercent;
	}
	
	public void setFuture(ScheduledFuture<?> sf) {
		this.future = sf;
	}

	public ScheduledFuture<?> getFuture() {
		return future;
	}
	
	public void stop() {
		this.stopped = true;
	}
}

