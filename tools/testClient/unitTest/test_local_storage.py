import uuid
import time
import cloudstackAPIException
import cloudstackTestClient
from cloudstackAPI import *
if __name__ == "__main__":
    hypervisor = "Simulator"
    hostNum = 100
    clusterNum = hostNum/10
    templateId = 10
    vmNum = 10
   
    randomName = str(uuid.uuid4())
   
    testclient = cloudstackTestClient.cloudstackTestClient("localhost")
    api = testclient.getApiClient()
    
    
    updatecfg = updateConfiguration.updateConfigurationCmd()
    updatecfg.name = "expunge.delay"
    updatecfg.value = "100"
    ret = api.updateConfiguration(updatecfg)
    
    updatecfg.name = "expunge.interval"
    updatecfg.value = "100"
    ret = api.updateConfiguration(updatecfg)
    
    updatecfg.name = "ping.interval"
    updatecfg.value = "180"
    ret = api.updateConfiguration(updatecfg)
    
    updatecfg.name = "system.vm.use.local.storage"
    updatecfg.value = "true"
    ret = api.updateConfiguration(updatecfg)
    
    updatecfg.name = "use.local.storage"
    updatecfg.value = "true"
    ret = api.updateConfiguration(updatecfg)
    
    
    
    czcmd = createZone.createZoneCmd()
    czcmd.dns1 = "8.8.8.8"
    czcmd.internaldns1 = "192.168.110.254"
    czcmd.name = "test" + randomName
    czcmd.networktype = "Basic"
    
    czresponse = api.createZone(czcmd)
    zoneId = czresponse.id
    
    cpodcmd = createPod.createPodCmd()
    cpodcmd.zoneid = zoneId
    cpodcmd.gateway = "192.168.137.1"
    cpodcmd.name = "testpod"+ randomName
    cpodcmd.netmask = "255.255.255.0"
    cpodcmd.startip = "192.168.137.200"
    cpodcmd.endip = "192.168.137.230"
    cpodresponse = api.createPod(cpodcmd)
    podId = cpodresponse.id
    
    cvlancmd = createVlanIpRange.createVlanIpRangeCmd()
    cvlancmd.zoneid = zoneId
    cvlancmd.podid = podId
    cvlancmd.gateway = "192.168.137.1"
    cvlancmd.netmask = "255.255.255.0"
    cvlancmd.startip = "192.168.137.100"
    cvlancmd.endip = "192.168.137.190"
    cvlancmd.forvirtualnetwork = "false"
    cvlancmd.vlan = "untagged"
    
    api.createVlanIpRange(cvlancmd)
    
    aclustercmd = addCluster.addClusterCmd()
    aclustercmd.clustername = "testcluster"+ randomName
    aclustercmd.hypervisor = hypervisor
    aclustercmd.podid = podId
    aclustercmd.zoneid = zoneId
    aclustercmd.clustertype = "CloudManaged"
    clusterresponse = api.addCluster(aclustercmd)
    clusterId = clusterresponse[0].id
    
    for i in range(hostNum):
        addhostcmd = addHost.addHostCmd()
        addhostcmd.zoneid = zoneId
        addhostcmd.podid = podId
        addhostcmd.clusterid = clusterId
        addhostcmd.hypervisor = hypervisor
        addhostcmd.username = "root"
        addhostcmd.password = "password"
        if hypervisor == "Simulator":
            addhostcmd.url = "http://sim"
        else:
            addhostcmd.url = "http://192.168.137.4"
        addhostresponse = api.addHost(addhostcmd)
        print addhostresponse[0].id, addhostresponse[0].ipaddress
    
    
    createspcmd = createStoragePool.createStoragePoolCmd()
    createspcmd.zoneid = zoneId
    createspcmd.podid = podId
    createspcmd.clusterid = clusterId
    createspcmd.url = "nfs://nfs2.lab.vmops.com/export/home/edison/primary"
    createspcmd.name = "storage pool" + randomName
    createspresponse = api.createStoragePool(createspcmd)
    
    
    
    addsscmd = addSecondaryStorage.addSecondaryStorageCmd()
    addsscmd.url = "nfs://nfs2.lab.vmops.com/export/home/edison/xen/secondary"
    addsscmd.zoneid = zoneId
    api.addSecondaryStorage(addsscmd)
    
    listtmcmd = listTemplates.listTemplatesCmd()
    listtmcmd.id = templateId
    listtmcmd.zoneid = zoneId
    listtmcmd.templatefilter = "featured"
    listtmresponse = api.listTemplates(listtmcmd)
    while True:
        if listtmresponse is not None and listtmresponse[0].isready == "true":
            break
        time.sleep(30)
        listtmresponse = api.listTemplates(listtmcmd)
        
    vmId = []
    for i in range(vmNum):
        cmd = deployVirtualMachine.deployVirtualMachineCmd()
        cmd.zoneid = zoneId
        cmd.hypervisor = hypervisor
        cmd.serviceofferingid = "1"
        cmd.templateid = listtmresponse[0].id
        res = api.deployVirtualMachine(cmd)
        
        vmId.append(res.id)

    registerTempl = registerTemplate.registerTemplateCmd()
    registerTempl.displaytext = "test template4"
    registerTempl.format = "QCOW2"
    registerTempl.hypervisor = "Simulator"
    registerTempl.name = "test template4"
    registerTempl.ostypeid = "100"
    registerTempl.url = "http://www.google.com/template.qcow2"
    registerTempl.zoneid = 1
    registerTemlResponse = api.registerTemplate(registerTempl)
    newtemplateId = registerTemlResponse[0].id
    
    listtempl = listTemplates.listTemplatesCmd()
    listtempl.id = newtemplateId
    listtempl.templatefilter = "self"
    listemplResponse = api.listTemplates(listtempl)
    while True:
        if listemplResponse is not None:
            
            if listemplResponse[0].isready == "true":
                break
            else:
                print listemplResponse[0].status
        
        time.sleep(30)
        listemplResponse = api.listTemplates(listtempl)
    
    
    
    for i in range(10):
        cmd = deployVirtualMachine.deployVirtualMachineCmd()
        cmd.zoneid = 1
        cmd.hypervisor = hypervisor
        cmd.serviceofferingid = "1"
        #cmd.templateid = listemplResponse[0].id
        cmd.templateid = 200
        res = api.deployVirtualMachine(cmd)
    

    createvolume = createVolume.createVolumeCmd()
    createvolume.zoneid = 1
    createvolume.diskofferingid = 9
    createvolume.name = "test"
    
    createvolumeresponse = api.createVolume(createvolume)
    volumeId = createvolumeresponse.id
    
    attachvolume = attachVolume.attachVolumeCmd()
    attachvolume.id = volumeId
    attachvolume.virtualmachineid = 9
    attachvolumeresponse = api.attachVolume(attachvolume)
    
    deattachevolume = detachVolume.detachVolumeCmd()
    deattachevolume.id = volumeId
    deattachvolumeresponse = api.detachVolume(deattachevolume)
    
    createsnapshot = createSnapshot.createSnapshotCmd()
    createsnapshot.volumeid = volumeId
    createsnapshotresponse = api.createSnapshot(createsnapshot)
    snapshotId = createsnapshotresponse.id
    
    createtmpl = createTemplate.createTemplateCmd()
    createtmpl.snapshotid = snapshotId
    createtmpl.name =  randomName[:10]
    createtmpl.displaytext = randomName[:10]
    createtmpl.ostypeid = 100
    createtmpl.ispublic = "false"
    createtmpl.passwordenabled = "false"
    createtmpl.isfeatured = "false"
    createtmplresponse = api.createTemplate(createtmpl)
    templateId = createtmplresponse.id
    
    createvolume = createVolume.createVolumeCmd()
    createvolume.snapshotid = snapshotId
    createvolume.name = "test"
    createvolumeresponse = api.createVolume(createvolume)
    volumeId = createvolumeresponse.id
    
    cmd = deployVirtualMachine.deployVirtualMachineCmd()
    cmd.zoneid = 1
    cmd.hypervisor = hypervisor
    cmd.serviceofferingid = "1"
    cmd.templateid = templateId
    cmd.name = "fdf"
    res = api.deployVirtualMachine(cmd)
    
    
    
    attachvolume = attachVolume.attachVolumeCmd()
    attachvolume.id = volumeId
    attachvolume.virtualmachineid = 1
    attachvolumeresponse = api.attachVolume(attachvolume)
    
    deattachevolume = detachVolume.detachVolumeCmd()
    deattachevolume.id = volumeId
    deattachvolumeresponse = api.detachVolume(deattachevolume)
    
    deletetmpl = deleteTemplate.deleteTemplateCmd()
    deletetmpl.id = templateId
    deletetmpl.zoneid = 1
    api.deleteTemplate(deletetmpl)
    
    deletevolume = deleteVolume.deleteVolumeCmd()
    deletevolume.id = volumeId
    api.deleteVolume(deletevolume)
    
    deletesnapshot = deleteSnapshot.deleteSnapshotCmd()
    deletesnapshot.id = snapshotId

    