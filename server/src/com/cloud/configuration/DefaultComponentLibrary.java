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
package com.cloud.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cloud.agent.manager.ClusteredAgentManagerImpl;
import com.cloud.alert.AlertManagerImpl;
import com.cloud.alert.dao.AlertDaoImpl;
import com.cloud.async.AsyncJobExecutorContextImpl;
import com.cloud.async.AsyncJobManagerImpl;
import com.cloud.async.SyncQueueManagerImpl;
import com.cloud.async.dao.AsyncJobDaoImpl;
import com.cloud.async.dao.SyncQueueDaoImpl;
import com.cloud.async.dao.SyncQueueItemDaoImpl;
import com.cloud.capacity.CapacityManagerImpl;
import com.cloud.capacity.dao.CapacityDaoImpl;
import com.cloud.certificate.dao.CertificateDaoImpl;
import com.cloud.cluster.CheckPointManagerImpl;
import com.cloud.cluster.ClusterFenceManagerImpl;
import com.cloud.cluster.ClusterManagerImpl;
import com.cloud.cluster.ManagementServerNode;
import com.cloud.cluster.agentlb.dao.HostTransferMapDaoImpl;
import com.cloud.cluster.dao.ManagementServerHostDaoImpl;
import com.cloud.cluster.dao.StackMaidDaoImpl;
import com.cloud.configuration.dao.ConfigurationDaoImpl;
import com.cloud.configuration.dao.ResourceCountDaoImpl;
import com.cloud.configuration.dao.ResourceLimitDaoImpl;
import com.cloud.consoleproxy.ConsoleProxyManagerImpl;
import com.cloud.dao.EntityManager;
import com.cloud.dao.EntityManagerImpl;
import com.cloud.dc.ClusterDetailsDaoImpl;
import com.cloud.dc.dao.AccountVlanMapDaoImpl;
import com.cloud.dc.dao.ClusterDaoImpl;
import com.cloud.dc.dao.DataCenterDaoImpl;
import com.cloud.dc.dao.DataCenterIpAddressDaoImpl;
import com.cloud.dc.dao.DcDetailsDaoImpl;
import com.cloud.dc.dao.HostPodDaoImpl;
import com.cloud.dc.dao.PodVlanMapDaoImpl;
import com.cloud.dc.dao.VlanDaoImpl;
import com.cloud.domain.dao.DomainDaoImpl;
import com.cloud.event.dao.EventDaoImpl;
import com.cloud.event.dao.UsageEventDaoImpl;
import com.cloud.ha.HighAvailabilityManagerImpl;
import com.cloud.ha.dao.HighAvailabilityDaoImpl;
import com.cloud.host.dao.HostDaoImpl;
import com.cloud.host.dao.HostDetailsDaoImpl;
import com.cloud.host.dao.HostTagsDaoImpl;
import com.cloud.hypervisor.HypervisorGuruManagerImpl;
import com.cloud.keystore.KeystoreDaoImpl;
import com.cloud.keystore.KeystoreManagerImpl;
import com.cloud.maint.UpgradeManagerImpl;
import com.cloud.maint.dao.AgentUpgradeDaoImpl;
import com.cloud.network.NetworkManagerImpl;
import com.cloud.network.dao.FirewallRulesCidrsDaoImpl;
import com.cloud.network.dao.FirewallRulesDaoImpl;
import com.cloud.network.dao.IPAddressDaoImpl;
import com.cloud.network.dao.LoadBalancerDaoImpl;
import com.cloud.network.dao.LoadBalancerVMMapDaoImpl;
import com.cloud.network.dao.NetworkDaoImpl;
import com.cloud.network.dao.NetworkDomainDaoImpl;
import com.cloud.network.dao.NetworkRuleConfigDaoImpl;
import com.cloud.network.lb.LoadBalancingRulesManagerImpl;
import com.cloud.network.ovs.OvsNetworkManagerImpl;
import com.cloud.network.ovs.OvsTunnelManagerImpl;
import com.cloud.network.ovs.dao.GreTunnelDaoImpl;
import com.cloud.network.ovs.dao.OvsTunnelAccountDaoImpl;
import com.cloud.network.ovs.dao.OvsTunnelDaoImpl;
import com.cloud.network.ovs.dao.OvsWorkDaoImpl;
import com.cloud.network.ovs.dao.VlanMappingDaoImpl;
import com.cloud.network.ovs.dao.VlanMappingDirtyDaoImpl;
import com.cloud.network.ovs.dao.VmFlowLogDaoImpl;
import com.cloud.network.router.VirtualNetworkApplianceManagerImpl;
import com.cloud.network.rules.RulesManagerImpl;
import com.cloud.network.rules.dao.PortForwardingRulesDaoImpl;
import com.cloud.network.security.SecurityGroupManagerImpl;
import com.cloud.network.security.SecurityGroupManagerImpl2;
import com.cloud.network.security.dao.IngressRuleDaoImpl;
import com.cloud.network.security.dao.SecurityGroupDaoImpl;
import com.cloud.network.security.dao.SecurityGroupRulesDaoImpl;
import com.cloud.network.security.dao.SecurityGroupVMMapDaoImpl;
import com.cloud.network.security.dao.SecurityGroupWorkDaoImpl;
import com.cloud.network.security.dao.VmRulesetLogDaoImpl;
import com.cloud.offerings.dao.NetworkOfferingDaoImpl;
import com.cloud.resource.ResourceManagerImpl;
import com.cloud.service.dao.ServiceOfferingDaoImpl;
import com.cloud.storage.StorageManagerImpl;
import com.cloud.storage.dao.DiskOfferingDaoImpl;
import com.cloud.storage.dao.GuestOSCategoryDaoImpl;
import com.cloud.storage.dao.GuestOSDaoImpl;
import com.cloud.storage.dao.LaunchPermissionDaoImpl;
import com.cloud.storage.dao.SnapshotDaoImpl;
import com.cloud.storage.dao.SnapshotPolicyDaoImpl;
import com.cloud.storage.dao.SnapshotScheduleDaoImpl;
import com.cloud.storage.dao.StoragePoolDaoImpl;
import com.cloud.storage.dao.StoragePoolHostDaoImpl;
import com.cloud.storage.dao.StoragePoolWorkDaoImpl;
import com.cloud.storage.dao.SwiftDaoImpl;
import com.cloud.storage.dao.UploadDaoImpl;
import com.cloud.storage.dao.VMTemplateDaoImpl;
import com.cloud.storage.dao.VMTemplateHostDaoImpl;
import com.cloud.storage.dao.VMTemplatePoolDaoImpl;
import com.cloud.storage.dao.VMTemplateZoneDaoImpl;
import com.cloud.storage.dao.VolumeDaoImpl;
import com.cloud.storage.download.DownloadMonitorImpl;
import com.cloud.storage.secondary.SecondaryStorageManagerImpl;
import com.cloud.storage.snapshot.SnapshotManagerImpl;
import com.cloud.storage.snapshot.SnapshotSchedulerImpl;
import com.cloud.storage.upload.UploadMonitorImpl;
import com.cloud.template.HyervisorTemplateAdapter;
import com.cloud.template.TemplateAdapter;
import com.cloud.template.TemplateAdapter.TemplateAdapterType;
import com.cloud.template.TemplateManagerImpl;
import com.cloud.upgrade.DatabaseUpgradeChecker;
import com.cloud.user.AccountManagerImpl;
import com.cloud.user.dao.AccountDaoImpl;
import com.cloud.user.dao.SSHKeyPairDaoImpl;
import com.cloud.user.dao.UserAccountDaoImpl;
import com.cloud.user.dao.UserDaoImpl;
import com.cloud.user.dao.UserStatisticsDaoImpl;
import com.cloud.utils.component.Adapter;
import com.cloud.utils.component.ComponentLibrary;
import com.cloud.utils.component.ComponentLibraryBase;
import com.cloud.utils.component.ComponentLocator.ComponentInfo;
import com.cloud.utils.component.Manager;
import com.cloud.utils.component.SystemIntegrityChecker;
import com.cloud.utils.db.GenericDao;
import com.cloud.vm.ClusteredVirtualMachineManagerImpl;
import com.cloud.vm.ItWorkDaoImpl;
import com.cloud.vm.UserVmManagerImpl;
import com.cloud.vm.dao.ConsoleProxyDaoImpl;
import com.cloud.vm.dao.DomainRouterDaoImpl;
import com.cloud.vm.dao.InstanceGroupDaoImpl;
import com.cloud.vm.dao.InstanceGroupVMMapDaoImpl;
import com.cloud.vm.dao.NicDaoImpl;
import com.cloud.vm.dao.SecondaryStorageVmDaoImpl;
import com.cloud.vm.dao.UserVmDaoImpl;
import com.cloud.vm.dao.UserVmDetailsDaoImpl;
import com.cloud.vm.dao.VMInstanceDaoImpl;

public class DefaultComponentLibrary extends ComponentLibraryBase implements ComponentLibrary {
    protected void populateDaos() {
        addDao("StackMaidDao", StackMaidDaoImpl.class);
        addDao("VMTemplateZoneDao", VMTemplateZoneDaoImpl.class);
        addDao("DomainRouterDao", DomainRouterDaoImpl.class);
        addDao("HostDao", HostDaoImpl.class);
        addDao("VMInstanceDao", VMInstanceDaoImpl.class);
        addDao("UserVmDao", UserVmDaoImpl.class);
        ComponentInfo<? extends GenericDao<?, ? extends Serializable>> info = addDao("ServiceOfferingDao", ServiceOfferingDaoImpl.class);
        info.addParameter("cache.size", "50");
        info.addParameter("cache.time.to.live", "600");
        info = addDao("DiskOfferingDao", DiskOfferingDaoImpl.class);
        info.addParameter("cache.size", "50");
        info.addParameter("cache.time.to.live", "600");
        info = addDao("DataCenterDao", DataCenterDaoImpl.class);
        info.addParameter("cache.size", "50");
        info.addParameter("cache.time.to.live", "600");
        info = addDao("HostPodDao", HostPodDaoImpl.class);
        info.addParameter("cache.size", "50");
        info.addParameter("cache.time.to.live", "600");
        addDao("IPAddressDao", IPAddressDaoImpl.class);
        info = addDao("VlanDao", VlanDaoImpl.class);
        info.addParameter("cache.size", "30");
        info.addParameter("cache.time.to.live", "3600");
        addDao("PodVlanMapDao", PodVlanMapDaoImpl.class);
        addDao("AccountVlanMapDao", AccountVlanMapDaoImpl.class);
        addDao("VolumeDao", VolumeDaoImpl.class);
        addDao("EventDao", EventDaoImpl.class);
        info = addDao("UserDao", UserDaoImpl.class);
        info.addParameter("cache.size", "5000");
        info.addParameter("cache.time.to.live", "300");
        addDao("UserStatisticsDao", UserStatisticsDaoImpl.class);
        addDao("FirewallRulesDao", FirewallRulesDaoImpl.class);
        addDao("LoadBalancerDao", LoadBalancerDaoImpl.class);
        addDao("NetworkRuleConfigDao", NetworkRuleConfigDaoImpl.class);
        addDao("LoadBalancerVMMapDao", LoadBalancerVMMapDaoImpl.class);
        addDao("DataCenterIpAddressDao", DataCenterIpAddressDaoImpl.class);
        addDao("SecurityGroupDao", SecurityGroupDaoImpl.class);
        addDao("IngressRuleDao", IngressRuleDaoImpl.class);
        addDao("SecurityGroupVMMapDao", SecurityGroupVMMapDaoImpl.class);
        addDao("SecurityGroupRulesDao", SecurityGroupRulesDaoImpl.class);
        addDao("SecurityGroupWorkDao", SecurityGroupWorkDaoImpl.class);
        addDao("VmRulesetLogDao", VmRulesetLogDaoImpl.class);
        addDao("AlertDao", AlertDaoImpl.class);
        addDao("CapacityDao", CapacityDaoImpl.class);
        addDao("DomainDao", DomainDaoImpl.class);
        addDao("AccountDao", AccountDaoImpl.class);
        addDao("ResourceLimitDao", ResourceLimitDaoImpl.class);
        addDao("ResourceCountDao", ResourceCountDaoImpl.class);
        addDao("UserAccountDao", UserAccountDaoImpl.class);
        addDao("VMTemplateHostDao", VMTemplateHostDaoImpl.class);
        addDao("UploadDao", UploadDaoImpl.class);
        addDao("VMTemplatePoolDao", VMTemplatePoolDaoImpl.class);
        addDao("LaunchPermissionDao", LaunchPermissionDaoImpl.class);
        addDao("ConfigurationDao", ConfigurationDaoImpl.class);
        info = addDao("VMTemplateDao", VMTemplateDaoImpl.class);
        info.addParameter("cache.size", "100");
        info.addParameter("cache.time.to.live", "600");
        info.addParameter("routing.uniquename", "routing");
        addDao("HighAvailabilityDao", HighAvailabilityDaoImpl.class);
        addDao("ConsoleProxyDao", ConsoleProxyDaoImpl.class);
        addDao("SecondaryStorageVmDao", SecondaryStorageVmDaoImpl.class);
        addDao("ManagementServerHostDao", ManagementServerHostDaoImpl.class);
        addDao("AgentUpgradeDao", AgentUpgradeDaoImpl.class);
        addDao("SnapshotDao", SnapshotDaoImpl.class);
        addDao("AsyncJobDao", AsyncJobDaoImpl.class);
        addDao("SyncQueueDao", SyncQueueDaoImpl.class);
        addDao("SyncQueueItemDao", SyncQueueItemDaoImpl.class);
        addDao("GuestOSDao", GuestOSDaoImpl.class);
        addDao("GuestOSCategoryDao", GuestOSCategoryDaoImpl.class);
        addDao("StoragePoolDao", StoragePoolDaoImpl.class);
        addDao("StoragePoolHostDao", StoragePoolHostDaoImpl.class);
        addDao("DetailsDao", HostDetailsDaoImpl.class);
        addDao("SnapshotPolicyDao", SnapshotPolicyDaoImpl.class);
        addDao("SnapshotScheduleDao", SnapshotScheduleDaoImpl.class);
        addDao("ClusterDao", ClusterDaoImpl.class);
        addDao("CertificateDao", CertificateDaoImpl.class);
        addDao("NetworkConfigurationDao", NetworkDaoImpl.class);
        addDao("NetworkOfferingDao", NetworkOfferingDaoImpl.class);
        addDao("NicDao", NicDaoImpl.class);
        addDao("InstanceGroupDao", InstanceGroupDaoImpl.class);
        addDao("InstanceGroupVMMapDao", InstanceGroupVMMapDaoImpl.class);
        addDao("ItWorkDao", ItWorkDaoImpl.class);
        addDao("FirewallRulesDao", FirewallRulesDaoImpl.class);
        addDao("PortForwardingRulesDao", PortForwardingRulesDaoImpl.class);
        addDao("FirewallRulesCidrsDao", FirewallRulesCidrsDaoImpl.class);
        addDao("SSHKeyPairDao", SSHKeyPairDaoImpl.class);
        addDao("UsageEventDao", UsageEventDaoImpl.class);
        addDao("ClusterDetailsDao", ClusterDetailsDaoImpl.class);
        addDao("UserVmDetailsDao", UserVmDetailsDaoImpl.class);
        addDao("VlanMappingDao", VlanMappingDaoImpl.class);
        addDao("VlanMappingDirtyDao", VlanMappingDirtyDaoImpl.class);
        addDao("OvsWorkDao", OvsWorkDaoImpl.class);
        addDao("VmFlowLogDao", VmFlowLogDaoImpl.class);
        addDao("GreTunnelDao", GreTunnelDaoImpl.class);
        addDao("OvsTunnelDao", OvsTunnelDaoImpl.class);
        addDao("OvsTunnelAccountDao", OvsTunnelAccountDaoImpl.class);
        addDao("StoragePoolWorkDao", StoragePoolWorkDaoImpl.class);
        addDao("HostTagsDao", HostTagsDaoImpl.class);
        addDao("NetworkDomainDao", NetworkDomainDaoImpl.class);
        addDao("KeystoreDao", KeystoreDaoImpl.class);
        addDao("DcDetailsDao", DcDetailsDaoImpl.class);
        addDao("SwiftDao", SwiftDaoImpl.class);
        addDao("AgentTransferMapDao", HostTransferMapDaoImpl.class);
    }

    @Override
    public synchronized Map<String, ComponentInfo<GenericDao<?, ?>>> getDaos() {
        if (_daos.size() == 0) {
            populateDaos();
        }
        return _daos;
    }

    protected void populateManagers() {
        addManager("StackMaidManager", CheckPointManagerImpl.class);
        addManager("account manager", AccountManagerImpl.class);
        addManager("configuration manager", ConfigurationManagerImpl.class);
        addManager("network manager", NetworkManagerImpl.class);
        addManager("download manager", DownloadMonitorImpl.class);
        addManager("upload manager", UploadMonitorImpl.class);
        addManager("keystore manager", KeystoreManagerImpl.class);
        addManager("secondary storage vm manager", SecondaryStorageManagerImpl.class);
        addManager("vm manager", UserVmManagerImpl.class);
        addManager("upgrade manager", UpgradeManagerImpl.class);
        addManager("StorageManager", StorageManagerImpl.class);
        addManager("SyncQueueManager", SyncQueueManagerImpl.class);
        addManager("AsyncJobManager", AsyncJobManagerImpl.class);
        addManager("AsyncJobExecutorContext", AsyncJobExecutorContextImpl.class);
        addManager("HA Manager", HighAvailabilityManagerImpl.class);
        addManager("Alert Manager", AlertManagerImpl.class);
        addManager("Template Manager", TemplateManagerImpl.class);
        addManager("Snapshot Manager", SnapshotManagerImpl.class);
        addManager("SnapshotScheduler", SnapshotSchedulerImpl.class);
        addManager("SecurityGroupManager", SecurityGroupManagerImpl2.class);
        addManager("DomainRouterManager", VirtualNetworkApplianceManagerImpl.class);
        addManager("EntityManager", EntityManagerImpl.class);
        addManager("LoadBalancingRulesManager", LoadBalancingRulesManagerImpl.class);
        addManager("RulesManager", RulesManagerImpl.class);
        addManager("OvsNetworkManager", OvsNetworkManagerImpl.class);
        addManager("OvsTunnelManager", OvsTunnelManagerImpl.class);
        addManager("Capacity Manager", CapacityManagerImpl.class);
        addManager("Cluster Manager", ClusterManagerImpl.class);
        addManager("VirtualMachineManager", ClusteredVirtualMachineManagerImpl.class);
        addManager("HypervisorGuruManager", HypervisorGuruManagerImpl.class);
        addManager("ClusterFenceManager", ClusterFenceManagerImpl.class);
        addManager("ResourceManager", ResourceManagerImpl.class);
        ComponentInfo<? extends Manager> info = addManager("ConsoleProxyManager", ConsoleProxyManagerImpl.class);
        info.addParameter("consoleproxy.sslEnabled", "true");
        addManager("ClusteredAgentManager", ClusteredAgentManagerImpl.class);
    }

    @Override
    public synchronized Map<String, ComponentInfo<Manager>> getManagers() {
        if (_managers.size() == 0) {
            populateManagers();
        }
        return _managers;
    }

    protected void populateAdapters() {
        addAdapter(TemplateAdapter.class, TemplateAdapterType.Hypervisor.getName(), HyervisorTemplateAdapter.class);
    }

    @Override
    public synchronized Map<String, List<ComponentInfo<Adapter>>> getAdapters() {
        if (_adapters.size() == 0) {
            populateAdapters();
        }
        return _adapters;
    }

    @Override
    public synchronized Map<Class<?>, Class<?>> getFactories() {
        HashMap<Class<?>, Class<?>> factories = new HashMap<Class<?>, Class<?>>();
        factories.put(EntityManager.class, EntityManagerImpl.class);
        return factories;
    }
}
