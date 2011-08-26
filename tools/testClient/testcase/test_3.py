from cloudstackTestCase import *
import uuid
class TestCase1(cloudstackTestCase):
    def setUp(self):
        '''get a small service offering id'''
        listsocmd = listServiceOfferings.listServiceOfferingsCmd()
        listsocmd.name = "Small Instance"
        listsocmd.issystem = "false"
        sos = self.testClient.getApiClient().listServiceOfferings(listsocmd)
        if sos is not None and len(sos) > 0:
            self.svid = sos[0].id
        listdiskovcmd = listDiskOfferings.listDiskOfferingsCmd()
        listdiskovcmd.name = "Small"
        disoov = self.testClient.getApiClient().listDiskOfferings(listdiskovcmd)
        if disoov is not None and len(disoov) > 0:
            self.diskov = disoov[0].id
            
        '''get user vm template id'''
        listtmplcmd = listTemplates.listTemplatesCmd()
        listtmplcmd.templatefilter = "featured"
        tmpls = self.testClient.getApiClient().listTemplates(listtmplcmd)
        if tmpls is not None:
            for tmpl in tmpls:
                if tmpl.isready:
                    self.templateId = tmpl.id
                    self.zoneId = tmpl.zoneid
                    break
        
    def test_cloudstackapi(self):
        apiClient = self.testClient.getApiClient()
        
        getzone = listZones.listZonesCmd()
        getzone.id = self.zoneId
        zone = apiClient.listZones(getzone)
        if zone[0].networktype == "Basic":
            '''create a security group for admin'''
            admincmd = listUsers.listUsersCmd()
            admincmd.account = "admin"
            admin = apiClient.listUsers(admincmd)
            domainId = admin[0].domainid
            
            securitygroup = authorizeSecurityGroupIngress.authorizeSecurityGroupIngressCmd()
            securitygroup.domainid = admin[0].domainid
            securitygroup.account = admin[0].account
            securitygroup.securitygroupid = 1
            securitygroup.protocol = "TCP"
            securitygroup.startport = "22"
            securitygroup.endport = "22"
            '''
            groups = [{"account":"a","group":"default"}, {"account":"b", "group":"default"}]
            securitygroup.usersecuritygrouplist = groups
            '''
            cidrlist = ["192.168.1.1/24", "10.1.1.1/24"]
            securitygroup.cidrlist = cidrlist
            try:
                apiClient.authorizeSecurityGroupIngress(securitygroup)
            except:
                pass
        '''
        createvm = deployVirtualMachine.deployVirtualMachineCmd()
        createvm.serviceofferingid = self.svid
        createvm.templateid = self.templateId
        createvm.zoneid = self.zoneId
        vm = apiClient.deployVirtualMachine(createvm)
        vmId = vm.id
        '''
        vmId = 1
        vmcmds = []
        for i in range(10):
            createvm = deployVirtualMachine.deployVirtualMachineCmd()
            createvm.serviceofferingid = self.svid
            createvm.templateid = self.templateId
            createvm.zoneid = self.zoneId
            vmcmds.append(createvm)
        
        result = self.testClient.submitCmdsAndWait(vmcmds)
        for jobstatus in result:
            if jobstatus.status == 1:
                self.debug(jobstatus.result.id)
                self.debug(jobstatus.result.displayname)
            else:
                self.debug(jobstatus.result)
        
        creatvolume = createVolume.createVolumeCmd()
        creatvolume.name = "tetst" + str(uuid.uuid4())
        creatvolume.diskofferingid = self.diskov
        creatvolume.zoneid = self.zoneId
        createvolumeresponse = apiClient.createVolume(creatvolume)
        volumeId = createvolumeresponse.id
        attach = attachVolume.attachVolumeCmd()
 
        attach.id = volumeId
        attach.virtualmachineid = vmId
        apiClient.attachVolume(attach)
        
        detach = detachVolume.detachVolumeCmd()
        detach.id = volumeId
        detach.virtualmachineid = vmId
        apiClient.detachVolume(detach)
        
        snapshotcmd = createSnapshot.createSnapshotCmd()
        snapshotcmd.volumeid = volumeId
        snapshotrespose = apiClient.createSnapshot(snapshotcmd)
        snapshotId = snapshotrespose.id
        
        createvolume = createVolume.createVolumeCmd()
        createvolume.snapshotid = snapshotId
        createvolume.name = "volumefrom_snapshot"+ str(snapshotId)
        apiClient.createVolume(createvolume)
        