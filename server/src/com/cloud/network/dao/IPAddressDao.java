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

package com.cloud.network.dao;

import java.util.List;

import com.cloud.network.IPAddressVO;
import com.cloud.utils.Pair;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.net.Ip;

public interface IPAddressDao extends GenericDao<IPAddressVO, Long> {
	
    IPAddressVO markAsUnavailable(long ipAddressId);
    
	void unassignIpAddress(long ipAddressId);	

	List<IPAddressVO> listByAccount(long accountId);
	
	List<IPAddressVO> listByVlanId(long vlanId);
	
	List<IPAddressVO> listByDcIdIpAddress(long dcId, String ipAddress);
	
	List<IPAddressVO> listByAssociatedNetwork(long networkId, Boolean isSourceNat);
	
	List<IPAddressVO> listStaticNatPublicIps(long networkId);
	
	int countIPs(long dcId, long vlanDbId, boolean onlyCountAllocated);
	
	int countIPs(long dcId, Long accountId, String vlanId, String vlanGateway, String vlanNetmask);
	
	boolean mark(long dcId, Ip ip);

	int countIPsForDashboard(long dcId, boolean onlyCountAllocated);
	
	IPAddressVO findByAssociatedVmId(long vmId);
	
	List<IPAddressVO> findAllByAssociatedVmId(long vmId);

	
	IPAddressVO findByAccountAndIp(long accountId, String ipAddress);

    List<Pair<String, String>> findAllElasticIpsForElasticIpVm(long elasticIpVmId);
	
}
