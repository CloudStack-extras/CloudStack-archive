#!/usr/bin/env python
try:
    import unittest2 as unittest
except ImportError:
    import unittest

import random
import hashlib
from cloudstackTestCase import *
import remoteSSHClient

class SampleScenarios(cloudstackTestCase):
    '''
    '''
    def setUp(self):
        pass


    def tearDown(self):
        pass


    def test_1_createAccounts(self, numberOfAccounts=2):
        '''
        Create a bunch of user accounts
        '''
        mdf = hashlib.md5()
        mdf.update('password')
        mdf_pass = mdf.hexdigest()
        api = self.testClient.getApiClient()
        for i in range(1, numberOfAccounts + 1):
            acct = createAccount.createAccountCmd()
            acct.accounttype = 0
            acct.firstname = 'user' + str(i)
            acct.lastname = 'user' + str(i)
            acct.password = mdf_pass
            acct.username = 'user' + str(i)
            acct.email = 'user@example.com'
            acct.account = 'user' + str(i)
            acct.domainid = 1
            acctResponse = api.createAccount(acct)
            self.debug("successfully created account: %s, user: %s, id: %s"%(acctResponse.account, acctResponse.username, acctResponse.id))


    def test_2_createServiceOffering(self):
        apiClient = self.testClient.getApiClient()
        createSOcmd=createServiceOffering.createServiceOfferingCmd()
        createSOcmd.name='Sample SO'
        createSOcmd.displaytext='Sample SO'
        createSOcmd.storagetype='shared'
        createSOcmd.cpunumber=1
        createSOcmd.cpuspeed=100
        createSOcmd.memory=128
        createSOcmd.offerha='false'
        createSOresponse = apiClient.createServiceOffering(createSOcmd)
        return createSOresponse.id 

    def deployCmd(self, account, service):
        deployVmCmd = deployVirtualMachine.deployVirtualMachineCmd()
        deployVmCmd.zoneid = 1
        deployVmCmd.account=account
        deployVmCmd.domainid=1
        deployVmCmd.templateid=2
        deployVmCmd.serviceofferingid=service
        return deployVmCmd

    def listVmsInAccountCmd(self, acct):
        api = self.testClient.getApiClient()
        listVmCmd = listVirtualMachines.listVirtualMachinesCmd()
        listVmCmd.account = acct
        listVmCmd.zoneid = 1
        listVmCmd.domainid = 1
        listVmResponse = api.listVirtualMachines(listVmCmd)
        return listVmResponse


    def destroyVmCmd(self, key):
        api = self.testClient.getApiClient()
        destroyVmCmd = destroyVirtualMachine.destroyVirtualMachineCmd()
        destroyVmCmd.id = key
        api.destroyVirtualMachine(destroyVmCmd)


    def test_3_stressDeploy(self):
        '''
            Deploy 5 Vms in each account
        '''
        service_id = self.test_2_createServiceOffering()
        api = self.testClient.getApiClient()
        for acct in range(1, 5):
            [api.deployVirtualMachine(self.deployCmd('user'+str(acct), service_id)) for x in range(0,5)]

    @unittest.skip("skipping destroys")
    def test_4_stressDestroy(self):
        '''
            Cleanup all Vms in every account
        '''
        api = self.testClient.getApiClient()
        for acct in range(1, 6):
            for vm in self.listVmsInAccountCmd('user'+str(acct)):
                if vm is not None:
                    self.destroyVmCmd(vm.id)

    @unittest.skip("skipping destroys")
    def test_5_combineStress(self):
        for i in range(0, 5):
            self.test_3_stressDeploy()
            self.test_4_stressDestroy()

    def deployN(self,nargs=300,batchsize=0):
        '''
        Deploy Nargs number of VMs concurrently in batches of size {batchsize}.
        When batchsize is 0 all Vms are deployed in one batch
        VMs will be deployed in 5:2:6 ratio
        '''
        cmds = []

        if batchsize == 0:
            self.testClient.submitCmdsAndWait(cmds)
        else:
            while len(z) > 0:
                try:
                    newbatch = [cmds.pop() for b in range(batchsize)] #pop batchsize items
                    self.testClient.submitCmdsAndWait(newbatch)
                except IndexError:
                    break
