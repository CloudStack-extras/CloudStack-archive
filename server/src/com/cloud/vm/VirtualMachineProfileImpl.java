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
package com.cloud.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cloud.agent.api.to.VolumeTO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.offering.ServiceOffering;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.template.VirtualMachineTemplate.BootloaderType;
import com.cloud.user.Account;
import com.cloud.user.dao.AccountDao;

/**
 * Implementation of VirtualMachineProfile.
 *
 */
public class VirtualMachineProfileImpl<T extends VMInstanceVO> implements VirtualMachineProfile<T> {
    
    T _vm;
    ServiceOfferingVO _offering;
    VMTemplateVO _template;
    Map<Param, Object> _params;
    List<NicProfile> _nics = new ArrayList<NicProfile>();
    List<VolumeTO> _disks = new ArrayList<VolumeTO>();
    StringBuilder _bootArgs = new StringBuilder();
    Account _owner;
    BootloaderType _bootloader;
    
    VirtualMachine.Type _type;
    
    public VirtualMachineProfileImpl(T vm, VMTemplateVO template, ServiceOfferingVO offering, Account owner, Map<Param, Object> params) {
        _vm = vm;
        _template = template;
        _offering = offering;
        _params = params;
        _owner = owner;
        if (_params == null) {
            _params = new HashMap<Param, Object>();
        }
        _type = vm.getType();
    }
    
    public VirtualMachineProfileImpl(T vm) {
        this(vm, null, null, null, null);
    }
    
    public VirtualMachineProfileImpl(VirtualMachine.Type type) {
        _type = type;
    }
    
    @Override
    public String toString() {
        return _vm.toString();
    }
    
    @Override
    public T getVirtualMachine() {
        return _vm;
    }
    
    @Override
    public ServiceOffering getServiceOffering() {
        if (_offering == null) {
            _offering = s_offeringDao.findByIdIncludingRemoved(_vm.getServiceOfferingId());
        }
        return _offering;
    }
    
    @Override
    public void setParameter(Param name, Object value) {
        _params.put(name, value);
    }
    
    @Override 
    public void setBootLoaderType(BootloaderType bootLoader) {
    	this._bootloader = bootLoader;
    }
    
    @Override
    public VirtualMachineTemplate getTemplate() {
        if (_template == null) {
            _template = s_templateDao.findByIdIncludingRemoved(_vm.getTemplateId());
        }
        return _template;
    }
    
    @Override
    public HypervisorType getHypervisorType() {
        return _vm.getHypervisorType();
    }

    @Override
    public long getTemplateId() {
        return _vm.getTemplateId();
    }

    @Override
    public long getServiceOfferingId() {
        return _vm.getServiceOfferingId();
    }

    @Override
    public long getId() {
        return _vm.getId();
    }
    
    public void setNics(List<NicProfile> nics) {
        _nics = nics;
    }
    
    public void setDisks(List<VolumeTO> disks) {
        _disks = disks;
    }
    
    @Override
    public List<NicProfile> getNics() {
        return _nics;
    }
    
    @Override
    public List<VolumeTO> getDisks() {
        return _disks;
    }
    
    @Override
    public void addNic(int index, NicProfile nic) {
        _nics.add(index, nic);
    }
    
    @Override
    public void addDisk(int index, VolumeTO disk) {
        _disks.add(index, disk);
    }
    
    @Override
    public StringBuilder getBootArgsBuilder() {
        return _bootArgs;
    }
    
    @Override
    public void addBootArgs(String... args) {
        for (String arg : args) {
            _bootArgs.append(arg).append(" ");
        }
    }
    
    @Override
    public VirtualMachine.Type getType() {
        return _type;
    }
    
    @Override
    public Account getOwner() {
        if (_owner == null) {
            _owner = s_accountDao.findByIdIncludingRemoved(_vm.getAccountId());
        }
        return _owner;
    }
    
    @Override
    public String getBootArgs() {
        return _bootArgs.toString();
    }
    
    static ServiceOfferingDao s_offeringDao;
    static VMTemplateDao s_templateDao;
    static AccountDao s_accountDao;
    static void setComponents(ServiceOfferingDao offeringDao, VMTemplateDao templateDao, AccountDao accountDao) {
        s_offeringDao = offeringDao;
        s_templateDao = templateDao;
        s_accountDao = accountDao;
    }

    @Override
    public void addNic(NicProfile nic) {
        _nics.add(nic);
    }

    @Override
    public void addDisk(VolumeTO disk) {
        _disks.add(disk);
    }

    @Override
    public Object getParameter(Param name) {
        return _params.get(name);
    }
    
    @Override
    public String getHostName() {
        return _vm.getHostName();
    }
    
    @Override
    public String getInstanceName() {
        return _vm.getInstanceName();
    }

	@Override
	public BootloaderType getBootLoaderType() {
		return this._bootloader;
	}
	
	@Override
	public Map<Param, Object> getParameters() {
	    return _params;
	}

	public void setServiceOffering(ServiceOfferingVO offering) {
		_offering = offering;
	}
}
