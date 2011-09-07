package com.cloud.vm;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.exception.ManagementServerException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.VirtualMachineMigrationException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.NetworkVO;
import com.cloud.offering.ServiceOffering;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.user.Account;
import com.cloud.user.User;
import com.cloud.uservm.UserVm;
import com.cloud.utils.Pair;
import com.cloud.utils.fsm.NoTransitionException;
import com.cloud.vm.VirtualMachine.Event;
import com.cloud.vm.VirtualMachine.Type;
import com.cloud.vm.VirtualMachineProfile.Param;

@Local(value = VirtualMachineManager.class)
public class MockVirtualMachineManagerImpl implements VirtualMachineManager {

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T allocate(T vm, VMTemplateVO template, ServiceOfferingVO serviceOffering, Pair<? extends DiskOfferingVO, Long> rootDiskOffering,
            List<Pair<DiskOfferingVO, Long>> dataDiskOfferings, List<Pair<NetworkVO, NicProfile>> networks, Map<Param, Object> params, DeploymentPlan plan, HypervisorType hyperType, Account owner)
            throws InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T allocate(T vm, VMTemplateVO template, ServiceOfferingVO serviceOffering, Long rootSize, Pair<DiskOfferingVO, Long> dataDiskOffering,
            List<Pair<NetworkVO, NicProfile>> networks, DeploymentPlan plan, HypervisorType hyperType, Account owner) throws InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T allocate(T vm, VMTemplateVO template, ServiceOfferingVO serviceOffering, List<Pair<NetworkVO, NicProfile>> networkProfiles, DeploymentPlan plan,
            HypervisorType hyperType, Account owner) throws InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T start(T vm, Map<Param, Object> params, User caller, Account account) throws InsufficientCapacityException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T start(T vm, Map<Param, Object> params, User caller, Account account, DeploymentPlan planToDeploy) throws InsufficientCapacityException,
            ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> boolean stop(T vm, User caller, Account account) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> boolean expunge(T vm, User caller, Account account) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> void registerGuru(Type type, VirtualMachineGuru<T> guru) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean stateTransitTo(VMInstanceVO vm, Event e, Long hostId) throws NoTransitionException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> T advanceStart(T vm, Map<Param, Object> params, User caller, Account account) throws InsufficientCapacityException, ResourceUnavailableException,
            ConcurrentOperationException, OperationTimedoutException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T advanceStart(T vm, Map<Param, Object> params, User caller, Account account, DeploymentPlan planToDeploy) throws InsufficientCapacityException,
            ResourceUnavailableException, ConcurrentOperationException, OperationTimedoutException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> boolean advanceStop(T vm, boolean forced, User caller, Account account) throws ResourceUnavailableException, OperationTimedoutException,
            ConcurrentOperationException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> boolean advanceExpunge(T vm, User caller, Account account) throws ResourceUnavailableException, OperationTimedoutException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> boolean remove(T vm, User caller, Account account) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> boolean destroy(T vm, User caller, Account account) throws AgentUnavailableException, OperationTimedoutException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean migrateAway(Type type, long vmid, long hostId) throws InsufficientServerCapacityException, VirtualMachineMigrationException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> T migrate(T vm, long srcHostId, DeployDestination dest) throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException,
            VirtualMachineMigrationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T reboot(T vm, Map<Param, Object> params, User caller, Account account) throws InsufficientCapacityException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T advanceReboot(T vm, Map<Param, Object> params, User caller, Account account) throws InsufficientCapacityException, ResourceUnavailableException,
            ConcurrentOperationException, OperationTimedoutException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VMInstanceVO findById(Type type, long vmId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isVirtualMachineUpgradable(UserVm vm, ServiceOffering offering) {
        // TODO Auto-generated method stub
        return false;
    }

}
