#!/usr/bin/env python
try:
    import unittest2 as unittest
except ImportError:
    import unittest

import random
import hashlib
from cloudstackTestCase import *
import string
import time

import pdb

class Provision(cloudstackTestCase):
    numberOfAccounts = 5
    accounts = []

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def setupServiceOffering(self):
        socreate = createServiceOffering.createServiceOfferingCmd()
        socreate.cpunumber = 1
        socreate.cpuspeed = 100
        socreate.displaytext = 'Sample SO'
        socreate.memory = 128
        socreate.name = 'Sample SO'
        api = self.testClient.getApiClient()
        soresponse = api.createServiceOffering(socreate)
        return soresponse.id


    def test_1_createAccounts(self):
        '''
        Create a bunch of user accounts
        '''
        mdf = hashlib.md5()
        mdf.update('password')
        mdf_pass = mdf.hexdigest()
        api = self.testClient.getApiClient()
        for i in range(1, self.numberOfAccounts + 1):
            acct = createAccount.createAccountCmd()
            acct.accounttype = 0
            name = ''.join(random.choice(string.ascii_uppercase + string.digits) for x in range(10))
            acct.firstname = name
            acct.lastname = name
            acct.password = mdf_pass
            acct.username = name
            acct.email = 'user@example.com'
            acct.account = name
            acct.domainid = 1
            acctResponse = api.createAccount(acct)
            self.accounts.append(acctResponse.account.name)
            self.debug("created account %s with id %d:"%(acctResponse.account.name, acctResponse.account.id))


    def deployCmd(self, acct):
        deployVmCmd = deployVirtualMachine.deployVirtualMachineCmd()
        deployVmCmd.zoneid = 1
        deployVmCmd.hypervisor='Simulator'
        deployVmCmd.account=acct
        deployVmCmd.domainid=1
        deployVmCmd.templateid=10
        deployVmCmd.serviceofferingid=self.setupServiceOffering()
        return deployVmCmd


    def destroyCmd(self, vmid):
        destroyVmCmd = destroyVirtualMachine.destroyVirtualMachineCmd()
        destroyVmCmd.zoneid = 1
        destroyVmCmd.id = vmid
        return destroyVmCmd

    def startCmd(self, vmid):
        startVmCmd = startVirtualMachine.startVirtualMachineCmd()
        startVmCmd.id = vmid
        return startVmCmd

    def listVmsInAccount(self, acct):
        listVm = listVirtualMachines.listVirtualMachinesCmd()
        listVm.account = acct
        listVm.zoneid = 1
        listVm.domainid = 1
        return self.testClient.getApiClient().listVirtualMachines(listVm)

    def listAccounts(self, acct):
        listacct = listAccounts.listAccountsCmd()
        listacct.name = acct
        listacct.accounttype = 0
        return self.testClient.getApiClient().listAccounts(listacct)


    def test_2_stressDeploy(self):
        api = self.testClient.getApiClient()
        for acct in self.accounts:
            [api.deployVirtualMachine(self.deployCmd(acct)) for x in range(0, 25)]


    def test_3_parallelDeployAndDestroy(self):
        p_accts = []
        #create 3 user accounts
        mdf = hashlib.md5()
        mdf.update('password')
        mdf_pass = mdf.hexdigest()
        api = self.testClient.getApiClient()
        for i in range(1, 3):
            acct = createAccount.createAccountCmd()
            acct.accounttype = 0
            name = ''.join(random.choice(string.ascii_uppercase + string.digits) for x in range(10))
            acct.firstname = name
            acct.lastname = name
            acct.password = mdf_pass
            acct.username = name
            acct.email = 'puser@example.com'
            acct.account = name
            acct.domainid = 1
            acctResponse = api.createAccount(acct)
            p_accts.append(acctResponse.account.name)
            self.debug("created account %s under root"%name)

        #deploy VMs each parallely in all three accounts
        deployCmds = []
        for acct in p_accts:
            for i in range(0, 50):
                deployCmds.append(self.deployCmd(acct))
        random.shuffle(deployCmds)
        self.execCmds(deployCmds)

        #destroy VMs each parallely in all three accounts
        destroyCmds = []
        for acct in p_accts:
            acctVms = self.listVmsInAccount(acct)
            self.debug("%d vms deployed in account: %s. Destroying them"%(len(acctVms),acct))
            for vm in acctVms[:45]:
                destroyCmds.append(self.destroyCmd(vm.id))
        random.shuffle(destroyCmds)
        self.execCmds(destroyCmds)

        for acct in p_accts:
            acctVms = self.listVmsInAccount(acct)
            self.assertEqual(len(acctVms), 5)
            listacct = self.listAccounts(acct)
            self.assertEqual(int(listacct[0].vmrunning) + int(listacct[0].vmstopped),\
                             int(listacct[0].vmtotal))
            self.debug("%d vms found left in account: %s. Finishing Test"%(len(acctVms),acct))


    def test_4_listVm(self):
        allVms = self.listVmsInAccount(None)
        self.debug("%d vms in all"%len(allVms))


    def test_5_multipleStartRequests(self):
        '''
            Start a few Vms, start them again before they reach running state,
            make sure the resource count remains consistent
        '''
        mdf = hashlib.md5()
        mdf.update('password')
        mdf_pass = mdf.hexdigest()
        api = self.testClient.getApiClient()
        acct = createAccount.createAccountCmd()
        acct.accounttype = 0
        name = ''.join(random.choice(string.ascii_uppercase + string.digits) for x in range(10))
        acct.firstname = name
        acct.lastname = name
        acct.password = mdf_pass
        acct.username = name
        acct.email = 'puser@example.com'
        acct.account = name
        acct.domainid = 1
        acctResponse = api.createAccount(acct)

        for i in range(0, 5):
           resp = api.deployVirtualMachine(self.deployCmd(name))
           api.startVirtualMachine(self.startCmd(resp.id))

        acctVms = self.listVmsInAccount(name)
        self.debug("%d vms found in account: %s."%(len(acctVms),name))
        self.assertEqual(5, len(acctVms))
        listacctresponse = self.listAccounts(name)
        self.assertEqual(int(listacctresponse[0].vmrunning) +\
                         int(listacctresponse[0].vmstopped),\
                         int(listacctresponse[0].vmtotal))
 

    def test_6_multipleDestroyRequests(self):
        '''
        Start a few Vms, destroy them, destroy them again before they reach
        expunging state, make sure the resource count remains consistent
        '''
        mdf = hashlib.md5()
        mdf.update('password')
        mdf_pass = mdf.hexdigest()
        api = self.testClient.getApiClient()
        acct = createAccount.createAccountCmd()
        acct.accounttype = 0
        name = ''.join(random.choice(string.ascii_uppercase + string.digits) for x in range(10))
        acct.firstname = name
        acct.lastname = name
        acct.password = mdf_pass
        acct.username = name
        acct.email = 'puser@example.com'
        acct.account = name
        acct.domainid = 1
        acctResponse = api.createAccount(acct)

        vmlist = []
        for i in range(0, 5):
            resp = api.deployVirtualMachine(self.deployCmd(name))
            vmlist.append(resp.id)

        for vm in vmlist:
            api.destroyVirtualMachine(self.destroyCmd(vm))
            time.sleep(1)
            api.destroyVirtualMachine(self.destroyCmd(vm))

        acctVms = self.listVmsInAccount(name)
        if acctVms is not None:
            self.debug("%d vms found in account: %s."%(len(acctVms),name))
            self.assertEqual(5, len(acctVms))
            listacctresponse = self.listAccounts(name)
            self.assertEqual(int(listacctresponse[0].vmrunning) +\
                             int(listacctresponse[0].vmstopped),\
                             int(listacctresponse[0].vmtotal))


    def execCmds(self,cmds=[],batchsize=0):
        '''
            When batchsize is 0 all Vms are deployed in one batch
        '''
        if batchsize == 0:
            self.testClient.submitCmdsAndWait(cmds)
        else:
            while len(cmds) > 0:
                try:
                    newbatch = [cmds.pop() for b in range(batchsize)] #pop batchsize items
                    self.testClient.submitCmdsAndWait(newbatch)
                except IndexError:
                    break
