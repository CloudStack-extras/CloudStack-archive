#!/usr/bin/env python
try:
    import unittest2 as unittest
except ImportError:
    import unittest
import random
from cloudstackAPI import *
from cloudstackTestCase import *

class Provision(cloudstackTestCase):
    '''
    This should test basic provisioning of virtual machines
    1. Deploy a no-frills VM
    2. Deploy with tags - hosttags
    3. Deploy with/without security groups
    '''

    solist = {}
    sgid = []

    def setUp(self):
        pass


    def tearDown(self):
        pass


    def test_0_createServiceOfferings(self):
        for tag in ['TAG1', 'TAG2', 'TAG3']:
            csocmd = createServiceOffering.createServiceOfferingCmd()
            csocmd.cpunumber = 1
            csocmd.cpuspeed = 2048
            csocmd.displaytext =  tag
            csocmd.memory = 7168
            csocmd.name = tag
            csocmd.hosttags = tag
            csocmd.storagetype = 'local'
            csoresponse = self.testClient.getApiClient().createServiceOffering(csocmd)
            self.debug(csoresponse)
            self.solist[tag]=csoresponse.id


    def test_1_createSecurityGroupAndRules(self):
        apiClient = self.testClient.getApiClient()
        sgCmd=createSecurityGroup.createSecurityGroupCmd()
        sgCmd.description='default-ps'
        sgCmd.name='default-ps'
        sgCmd.account='admin'
        sgCmd.domainid='1'
        sgRes = apiClient.createSecurityGroup(sgCmd)
        self.sgid.append(sgRes.id)

        ruleCmd=authorizeSecurityGroupIngress.authorizeSecurityGroupIngressCmd()
        ruleCmd.cidrlist = '172.1.0.0/12'
        ruleCmd.startport = '1'
        ruleCmd.endport = '65535'
        ruleCmd.protocol = 'TCP'
        ruleCmd.securitygroupid = sgRes.id
        ruleCmd.account='admin'
        ruleCmd.domainid='1'
        sgIngressresponse=apiClient.authorizeSecurityGroupIngress(ruleCmd)

        ruleCmd=authorizeSecurityGroupIngress.authorizeSecurityGroupIngressCmd()
        ruleCmd.cidrlist = '172.1.0.0/12'
        ruleCmd.startport = '22'
        ruleCmd.endport = '22'
        ruleCmd.protocol = 'TCP'
        ruleCmd.securitygroupid = sgRes.id
        ruleCmd.account='admin'
        ruleCmd.domainid='1'
        sgIngressresponse=apiClient.authorizeSecurityGroupIngress(ruleCmd)

        ruleCmd=authorizeSecurityGroupIngress.authorizeSecurityGroupIngressCmd()
        ruleCmd.cidrlist = '10.0.0.0/8'
        ruleCmd.startport = '80'
        ruleCmd.endport = '80'
        ruleCmd.protocol = 'TCP'
        ruleCmd.securitygroupid = sgRes.id
        ruleCmd.account='admin'
        ruleCmd.domainid='1'
        sgIngressresponse=apiClient.authorizeSecurityGroupIngress(ruleCmd)
        self.debug("Security group created with id: %d"%sgRes.id)



    @unittest.skip("needs fixing")
    def test_2_DeployVMWithHostTags(self):
        '''
        Deploy 3 virtual machines one with each hosttag
        '''
        for k,v in self.solist:
            deployVmCmd = deployVirtualMachine.deployVirtualMachineCmd()
            deployVmCmd.zoneid = 1
            deployVmCmd.hypervisor='Simulator'
            deployVmCmd.serviceofferingid=v
            deployVmCmd.account='admin'
            deployVmCmd.domainid=1
            deployVmCmd.templateid=10
            deployVmResponse = self.testClient.getApiClient().deployVirtualMachine(deployVmCmd)
            self.debug("Deployed VM :%d in job: %d"%(deployVmResponse[0].id, deployVmResponse[0].jobid))


    def deployCmd(self, tag, sgid):
        deployVmCmd = deployVirtualMachine.deployVirtualMachineCmd()
        deployVmCmd.zoneid = 1
        deployVmCmd.hypervisor='Simulator'
        deployVmCmd.serviceofferingid=self.solist[tag]
        deployVmCmd.account='admin'
        deployVmCmd.domainid=1
        deployVmCmd.templateid=10
        deployVmCmd.securitygroupids=sgid
        return deployVmCmd


    def listVmsInAccountCmd(self, acct='admin'):
        api = self.testClient.getApiClient()
        listVmCmd = listVirtualMachines.listVirtualMachinesCmd()
        listVmCmd.account = acct
        listVmCmd.zoneid = 1
        listVmCmd.domainid = 1
        listVmResponse = api.listVirtualMachines(listVmCmd)
        return listVmResponse


    def destroyCmd(self, vmid):
        destroyVmCmd = destroyVirtualMachine.destroyVirtualMachineCmd()
        destroyVmCmd.zoneid=1
        destroyVmCmd.id=vmid
        self.testClient.getApiClient().destroyVirtualMachine(destroyVmCmd)


    def deployN(self,nargs=300,batchsize=0):
        '''
        Deploy Nargs number of VMs concurrently in batches of size {batchsize}.
        When batchsize is 0 all Vms are deployed in one batch
        VMs will be deployed in 5:2:6 ratio
        '''
        tag1=nargs*5/13
        tag2=nargs*2/13
        tag3=nargs-tag1-tag2

        cmds = []
        [cmds.append(self.deployCmd('TAG1', self.sgid[0])) for i in range(tag1)]
        [cmds.append(self.deployCmd('TAG2', self.sgid[0])) for i in range(tag2)]
        [cmds.append(self.deployCmd('TAG3', self.sgid[0])) for i in range(tag3)]
        random.shuffle(cmds) #with mix-and-match of Tags

        if batchsize == 0:
            self.testClient.submitCmdsAndWait(cmds)
        else:
            while len(cmds) > 0:
                try:
                    newbatch = [cmds.pop() for b in range(batchsize)] #pop batchsize items
                    self.testClient.submitCmdsAndWait(newbatch)
                except IndexError:
                    break


    def test_3_bulkDeploy(self):
        self.deployN(nargs=500,batchsize=100)


    def test_4_bulkDestroy(self):
        api = self.testClient.getApiClient()
        for vm in self.listVmsInAccountCmd('admin'):
            if vm is not None:
                self.destroyCmd(vm.id)

