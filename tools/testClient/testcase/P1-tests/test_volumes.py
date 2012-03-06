# -*- encoding: utf-8 -*-
#
# Copyright (c) 2012 Citrix.  All rights reserved.
#
""" P1 tests for Volumes
"""
#Import Local Modules
from cloudstackTestCase import *
from cloudstackAPI import *
from testcase.libs.utils import *
from testcase.libs.base import *
from testcase.libs.common import *
import remoteSSHClient
#Import System modules
import os
import urllib
import time
import tempfile


class Services:
    """Test Volume Services
    """

    def __init__(self):
        self.services = {
                         "account": {
                                    "email": "test@test.com",
                                    "firstname": "Test",
                                    "lastname": "User",
                                    "username": "test",
                                    # Random characters are appended for unique
                                    # username
                                    "password": "fr3sca",
                         },
                         "service_offering": {
                                    "name": "Tiny Instance",
                                    "displaytext": "Tiny Instance",
                                    "cpunumber": 1,
                                    "cpuspeed": 100, # in MHz
                                    "memory": 64, # In MBs
                        },
                        "disk_offering": {
                                    "displaytext": "Small",
                                    "name": "Small",
                                    "disksize": 1
                        },
                        "volume": {
                                "diskname": "TestDiskServ",
                                "domainid": 1,
                                "max": 6,
                        },
                         "virtual_machine": {
                                    "displayname": "testVM",
                                    "hypervisor": 'XenServer',
                                    "domainid": 1,
                                    "protocol": 'TCP',
                                    "ssh_port": 22,
                                    "username": "root",
                                    "password": "password",
                                    "privateport": 22,
                                    "publicport": 22,
                         },
                         "iso":  # ISO settings for Attach/Detach ISO tests
                         {
                          "displaytext": "Test ISO",
                          "name": "testISO",
                          "url": "http://iso.linuxquestions.org/download/504/1819/http/gd4.tuwien.ac.at/dsl-4.4.10.iso",
                          # Source URL where ISO is located
                          "ostypeid": 76,
                          },
                        "sleep": 50,
                        "domainid": 1,
                        "ostypeid": 12,
                        "zoneid": 2,
                        # Optional, if specified the mentioned zone will be
                        # used for tests
                        "mode": 'basic',
                    }


class TestAttachVolume(cloudstackTestCase):

    @classmethod
    def setUpClass(cls):
        cls.api_client = fetch_api_client()
        cls.services = Services().services

        # Get Zone, Domain and templates
        cls.zone = get_zone(cls.api_client, cls.services)
        cls.disk_offering = DiskOffering.create(
                                    cls.api_client,
                                    cls.services["disk_offering"]
                                    )
        template = get_template(
                            cls.api_client,
                            cls.zone.id,
                            cls.services["ostypeid"]
                            )
        cls.services["zoneid"] = cls.zone.id
        cls.services["virtual_machine"]["zoneid"] = cls.zone.id
        cls.services["virtual_machine"]["template"] = template.id

        # Create VMs, NAT Rules etc
        cls.account = Account.create(
                            cls.api_client,
                            cls.services["account"]
                            )

        cls.services["account"] = cls.account.account.name
        cls.service_offering = ServiceOffering.create(
                                            cls.api_client,
                                            cls.services["service_offering"]
                                            )
        cls.virtual_machine = VirtualMachine.create(
                                    cls.api_client,
                                    cls.services["virtual_machine"],
                                    accountid=cls.account.account.name,
                                    serviceofferingid=cls.service_offering.id,
                                )
        cls._cleanup = [
                        cls.service_offering,
                        cls.disk_offering,
                        cls.account
                        ]

    def setUp(self):

        self.apiclient = self.testClient.getApiClient()
        self.dbclient = self.testClient.getDbConnection()
        self.cleanup = []

    def test_01_volume_attach(self):
        """Test Attach volumes (max capacity)
        """

        # Validate the following
        # 1. Deploy a vm and create 5 data disk
        # 2. Attach all the created Volume to the vm.
        # 3. Reboot the VM. VM should be successfully rebooted
        # 4. Stop the VM. Stop VM should be successful
        # 5. Start The VM. Start VM should be successful

        # Create 5 volumes and attach to VM
        for i in range(self.services["volume"]["max"]):
            volume = Volume.create(
                                   self.apiclient,
                                   self.services["volume"],
                                   zoneid=self.zone.id,
                                   account=self.account.account.name,
                                   diskofferingid=self.disk_offering.id
                                   )
            self.debug("Created volume: %s for account: %s" % (
                                                volume.id,
                                                self.account.account.name               
                                                ))
            # Check List Volume response for newly created volume
            list_volume_response = list_volumes(
                                                self.apiclient,
                                                id=volume.id
                                                )
            self.assertNotEqual(
                                list_volume_response,
                                None,
                                "Check if volume exists in ListVolumes"
                                )
            # Attach volume to VM
            self.virtual_machine.attach_volume(
                                                self.apiclient,
                                                volume
                                                )
            self.debug("Attach volume: %s to VM: %s" % (
                                                volume.id,
                                                self.virtual_machine.id       
                                                ))
        # Check all volumes attached to same VM
        list_volume_response = list_volumes(
                                    self.apiclient,
                                    virtualmachineid=self.virtual_machine.id,
                                    type='DATADISK'
                                    )
        self.assertEqual(
                                isinstance(list_volume_response, list),
                                True,
                                "Check list volumes response for valid list"
                        )
        
        self.assertNotEqual(
                                list_volume_response,
                                None,
                                "Check if volume exists in ListVolumes"
                                )
        self.assertEqual(
                            len(list_volume_response),
                            self.services["volume"]["max"],
                            "Check number of data volumes attached to VM"
                        )
        self.debug("Rebooting the VM: %s" % self.virtual_machine.id)
        # Reboot VM
        self.virtual_machine.reboot(self.apiclient)

        vm_response = list_virtual_machines(
                                        self.apiclient,
                                        id=self.virtual_machine.id,
                                        )
        #Verify VM response to check whether VM deployment was successful
        self.assertNotEqual(
                            len(vm_response),
                            0,
                            "Check VMs available in List VMs response"
                        )
        self.assertEqual(
                                isinstance(vm_response, list),
                                True,
                                "Check list VM response for valid list"
                        )
        vm = vm_response[0]
        self.assertEqual(
                            vm.state,
                            'Running',
                            "Check the state of VM"
                        )

        self.debug("Stopping the VM: %s" % self.virtual_machine.id)
        # Stop VM
        self.virtual_machine.stop(self.apiclient)

        vm_response = list_virtual_machines(
                                        self.apiclient,
                                        id=self.virtual_machine.id,
                                        )
        self.assertEqual(
                                isinstance(vm_response, list),
                                True,
                                "Check list VM response for valid list"
                        )
        
        #Verify VM response to check whether VM deployment was successful
        self.assertNotEqual(
                            len(vm_response),
                            0,
                            "Check VMs available in List VMs response"
                        )

        vm = vm_response[0]
        self.assertEqual(
                            vm.state,
                            'Stopped',
                            "Check the state of VM"
                        )

        self.debug("Starting the VM: %s" % self.virtual_machine.id)
        # Start VM
        self.virtual_machine.start(self.apiclient)
        # Sleep to ensure that VM is in ready state
        time.sleep(self.services["sleep"])

        vm_response = list_virtual_machines(
                                        self.apiclient,
                                        id=self.virtual_machine.id,
                                        )
        self.assertEqual(
                                isinstance(vm_response, list),
                                True,
                                "Check list VM response for valid list"
                        )
        
        #Verify VM response to check whether VM deployment was successful
        self.assertNotEqual(
                            len(vm_response),
                            0,
                            "Check VMs available in List VMs response"
                        )

        vm = vm_response[0]
        self.assertEqual(
                            vm.state,
                            'Running',
                            "Check the state of VM"
                        )
        return

    def test_02_volume_attach_max(self):
        """Test attach volumes (more than max) to an instance
        """

        # Validate the following
        # 1. Attach one more data volume to VM (Already 5 attached)
        # 2. Attach volume should fail

        # Create a volume and attach to VM
        volume = Volume.create(
                                self.apiclient,
                                self.services["volume"],
                                zoneid=self.zone.id,
                                account=self.account.account.name,
                                diskofferingid=self.disk_offering.id
                               )
        self.debug("Created volume: %s for account: %s" % (
                                                volume.id,
                                                self.account.account.name               
                                                ))
        # Check List Volume response for newly created volume
        list_volume_response = list_volumes(
                                            self.apiclient,
                                            id=volume.id
                                            )
        self.assertEqual(
                                isinstance(list_volume_response, list),
                                True,
                                "Check list volumes response for valid list"
                        )
        
        self.assertNotEqual(
                            list_volume_response,
                            None,
                            "Check if volume exists in ListVolumes"
                            )
        # Attach volume to VM
        with self.assertRaises(Exception):
            self.debug("Trying to Attach volume: %s to VM: %s" % (
                                                volume.id,
                                                self.virtual_machine.id       
                                                ))
            self.virtual_machine.attach_volume(
                                                self.apiclient,
                                                volume
                                                )
        return

    def tearDown(self):
        #Clean up, terminate the created volumes
        cleanup_resources(self.apiclient, self.cleanup)
        return

    @classmethod
    def tearDownClass(cls):
        try:
            cls.api_client = fetch_api_client()
            cleanup_resources(cls.api_client, cls._cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)


class TestAttachDetachVolume(cloudstackTestCase):

    @classmethod
    def setUpClass(cls):
        cls.api_client = fetch_api_client()
        cls.services = Services().services

        # Get Zone, Domain and templates
        cls.zone = get_zone(cls.api_client, cls.services)
        cls.disk_offering = DiskOffering.create(
                                    cls.api_client,
                                    cls.services["disk_offering"]
                                    )
        template = get_template(
                            cls.api_client,
                            cls.zone.id,
                            cls.services["ostypeid"]
                            )
        cls.services["zoneid"] = cls.zone.id
        cls.services["virtual_machine"]["zoneid"] = cls.zone.id
        cls.services["virtual_machine"]["template"] = template.id

        # Create VMs, NAT Rules etc
        cls.account = Account.create(
                            cls.api_client,
                            cls.services["account"]
                            )

        cls.services["account"] = cls.account.account.name
        cls.service_offering = ServiceOffering.create(
                                            cls.api_client,
                                            cls.services["service_offering"]
                                            )
        cls.virtual_machine = VirtualMachine.create(
                                    cls.api_client,
                                    cls.services["virtual_machine"],
                                    accountid=cls.account.account.name,
                                    serviceofferingid=cls.service_offering.id,
                                )
        cls._cleanup = [
                        cls.service_offering,
                        cls.disk_offering,
                        cls.account
                        ]

    def setUp(self):

        self.apiclient = self.testClient.getApiClient()
        self.dbclient = self.testClient.getDbConnection()
        self.cleanup = []

    def tearDown(self):
        #Clean up, terminate the created volumes
        cleanup_resources(self.apiclient, self.cleanup)
        return

    @classmethod
    def tearDownClass(cls):
        try:
            cls.api_client = fetch_api_client()
            cleanup_resources(cls.api_client, cls._cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)

    def test_01_volume_attach_detach(self):
        """Test Volume attach/detach to VM (5 data volumes)
        """

        # Validate the following
        # 1. Deploy a vm and create 5 data disk
        # 2. Attach all the created Volume to the vm.
        # 3. Detach all the volumes attached.
        # 4. Reboot the VM. VM should be successfully rebooted
        # 5. Stop the VM. Stop VM should be successful
        # 6. Start The VM. Start VM should be successful

        volumes = []
        # Create 5 volumes and attach to VM
        for i in range(self.services["volume"]["max"]):
            volume = Volume.create(
                                   self.apiclient,
                                   self.services["volume"],
                                   zoneid=self.zone.id,
                                   account=self.account.account.name,
                                   diskofferingid=self.disk_offering.id
                                   )
            self.debug("Created volume: %s for account: %s" % (
                                                volume.id,
                                                self.account.account.name               
                                                ))
            self.cleanup.append(volume)
            volumes.append(volume)

            # Check List Volume response for newly created volume
            list_volume_response = list_volumes(
                                                self.apiclient,
                                                id=volume.id
                                                )
            self.assertEqual(
                                isinstance(list_volume_response, list),
                                True,
                                "Check list volumes response for valid list"
                        )
        
            self.assertNotEqual(
                                list_volume_response,
                                None,
                                "Check if volume exists in ListVolumes"
                                )
            self.debug("Attach volume: %s to VM: %s" % (
                                                volume.id,
                                                self.virtual_machine.id       
                                                ))
            # Attach volume to VM
            self.virtual_machine.attach_volume(
                                                self.apiclient,
                                                volume
                                                )

        # Check all volumes attached to same VM
        list_volume_response = list_volumes(
                                    self.apiclient,
                                    virtualmachineid=self.virtual_machine.id,
                                    type='DATADISK'
                                    )
        self.assertEqual(
                                isinstance(list_volume_response, list),
                                True,
                                "Check list volumes response for valid list"
                        )
        
        self.assertNotEqual(
                                list_volume_response,
                                None,
                                "Check if volume exists in ListVolumes"
                                )
        self.assertEqual(
                            len(list_volume_response),
                            self.services["volume"]["max"],
                            "Check number of data volumes attached to VM"
                        )

        # Detach all volumes from VM
        for volume in volumes:
            self.debug("Detach volume: %s to VM: %s" % (
                                                volume.id,
                                                self.virtual_machine.id       
                                                ))
            self.virtual_machine.detach_volume(
                                                self.apiclient,
                                                volume
                                            )
        # Reboot VM
        self.debug("Rebooting the VM: %s" % self.virtual_machine.id)
        self.virtual_machine.reboot(self.apiclient)
        # Sleep to ensure that VM is in ready state
        time.sleep(self.services["sleep"])

        vm_response = list_virtual_machines(
                                        self.apiclient,
                                        id=self.virtual_machine.id,
                                        )
        #Verify VM response to check whether VM deployment was successful
        self.assertEqual(
                                isinstance(vm_response, list),
                                True,
                                "Check list VM response for valid list"
                        )
        
        self.assertNotEqual(
                            len(vm_response),
                            0,
                            "Check VMs available in List VMs response"
                        )
        vm = vm_response[0]
        self.assertEqual(
                            vm.state,
                            'Running',
                            "Check the state of VM"
                        )
        
        # Stop VM
        self.debug("Stopping the VM: %s" % self.virtual_machine.id)
        self.virtual_machine.stop(self.apiclient)
        # Sleep to ensure that VM is in ready state
        time.sleep(self.services["sleep"])

        vm_response = list_virtual_machines(
                                        self.apiclient,
                                        id=self.virtual_machine.id,
                                        )
        #Verify VM response to check whether VM deployment was successful
        self.assertEqual(
                                isinstance(vm_response, list),
                                True,
                                "Check list VM response for valid list"
                        )
        self.assertNotEqual(
                            len(vm_response),
                            0,
                            "Check VMs available in List VMs response"
                        )
        vm = vm_response[0]
        self.assertEqual(
                            vm.state,
                            'Stopped',
                            "Check the state of VM"
                        )

        # Start VM
        self.debug("Starting the VM: %s" % self.virtual_machine.id)
        self.virtual_machine.start(self.apiclient)
        # Sleep to ensure that VM is in ready state
        time.sleep(self.services["sleep"])

        vm_response = list_virtual_machines(
                                        self.apiclient,
                                        id=self.virtual_machine.id,
                                        )
        #Verify VM response to check whether VM deployment was successful
        self.assertEqual(
                                isinstance(vm_response, list),
                                True,
                                "Check list VM response for valid list"
                        )
        self.assertNotEqual(
                            len(vm_response),
                            0,
                            "Check VMs available in List VMs response"
                        )
        vm = vm_response[0]
        self.assertEqual(
                            vm.state,
                            'Running',
                            "Check the state of VM"
                        )
        return


class TestAttachVolumeISO(cloudstackTestCase):

    @classmethod
    def setUpClass(cls):
        cls.api_client = fetch_api_client()
        cls.services = Services().services

        # Get Zone, Domain and templates
        cls.zone = get_zone(cls.api_client, cls.services)
        cls.disk_offering = DiskOffering.create(
                                    cls.api_client,
                                    cls.services["disk_offering"]
                                    )
        template = get_template(
                            cls.api_client,
                            cls.zone.id,
                            cls.services["ostypeid"]
                            )
        cls.services["zoneid"] = cls.zone.id
        cls.services["virtual_machine"]["zoneid"] = cls.zone.id
        cls.services["iso"]["zoneid"] = cls.zone.id
        cls.services["virtual_machine"]["template"] = template.id

        # Create VMs, NAT Rules etc
        cls.account = Account.create(
                            cls.api_client,
                            cls.services["account"]
                            )

        cls.services["account"] = cls.account.account.name
        cls.service_offering = ServiceOffering.create(
                                            cls.api_client,
                                            cls.services["service_offering"]
                                            )
        cls.virtual_machine = VirtualMachine.create(
                                    cls.api_client,
                                    cls.services["virtual_machine"],
                                    accountid=cls.account.account.name,
                                    serviceofferingid=cls.service_offering.id,
                                )
        cls._cleanup = [
                        cls.service_offering,
                        cls.disk_offering,
                        cls.account
                        ]

    @classmethod
    def tearDownClass(cls):
        try:
            cleanup_resources(cls.api_client, cls._cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)

    def setUp(self):

        self.apiclient = self.testClient.getApiClient()
        self.dbclient = self.testClient.getDbConnection()
        self.cleanup = []

    def tearDown(self):
        try:
            #Clean up, terminate the created instance, volumes and snapshots
            cleanup_resources(self.apiclient, self.cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    def test_01_volume_iso_attach(self):
        """Test Volumes and ISO attach
        """

        # Validate the following
        # 1. Create and attach 5 data volumes to VM
        # 2. Create an ISO. Attach it to VM instance
        # 3. Verify that attach ISO is successful

        # Create 5 volumes and attach to VM
        for i in range(self.services["volume"]["max"]):
            volume = Volume.create(
                                   self.apiclient,
                                   self.services["volume"],
                                   zoneid=self.zone.id,
                                   account=self.account.account.name,
                                   diskofferingid=self.disk_offering.id
                                   )
            self.debug("Created volume: %s for account: %s" % (
                                                volume.id,
                                                self.account.account.name               
                                                ))
            # Check List Volume response for newly created volume
            list_volume_response = list_volumes(
                                                self.apiclient,
                                                id=volume.id
                                                )
            self.assertEqual(
                                isinstance(list_volume_response, list),
                                True,
                                "Check list volumes response for valid list"
                        )
            self.assertNotEqual(
                                list_volume_response,
                                None,
                                "Check if volume exists in ListVolumes"
                                )
            # Attach volume to VM
            self.virtual_machine.attach_volume(
                                                self.apiclient,
                                                volume
                                                )

        # Check all volumes attached to same VM
        list_volume_response = list_volumes(
                                    self.apiclient,
                                    virtualmachineid=self.virtual_machine.id,
                                    type='DATADISK'
                                    )
        self.assertEqual(
                                isinstance(list_volume_response, list),
                                True,
                                "Check list volumes response for valid list"
                        )
        self.assertNotEqual(
                                list_volume_response,
                                None,
                                "Check if volume exists in ListVolumes"
                                )
        self.assertEqual(
                            len(list_volume_response),
                            self.services["volume"]["max"],
                            "Check number of data volumes attached to VM"
                        )
        # Create an ISO and attach it to VM
        iso = Iso.create(
                         self.apiclient,
                         self.services["iso"],
                         account=self.account.account.name,
                         domainid=self.account.account.domainid,
                         )
        self.debug("Created ISO with ID: %s for account: %s" % (
                                                    iso.id, 
                                                    self.account.account.name
                                                    ))
        self.cleanup.append(iso)
        try:
            self.debug("Downloading ISO with ID: %s" % iso.id)
            iso.download(self.apiclient)
        except Exception as e:
            self.fail("Exception while downloading ISO %s: %s"\
                      % (iso.id, e))

        #Attach ISO to virtual machine
        self.debug("Attach ISO ID: %s to VM: %s" % (
                                                    iso.id,
                                                    self.virtual_machine.id
                                                    ))
        cmd = attachIso.attachIsoCmd()
        cmd.id = iso.id
        cmd.virtualmachineid = self.virtual_machine.id
        self.apiclient.attachIso(cmd)

        # Verify ISO is attached to VM
        vm_response = list_virtual_machines(
                                        self.apiclient,
                                        id=self.virtual_machine.id,
                                        )
        #Verify VM response to check whether VM deployment was successful
        self.assertEqual(
                                isinstance(vm_response, list),
                                True,
                                "Check list VM response for valid list"
                        )
        
        self.assertNotEqual(
                            len(vm_response),
                            0,
                            "Check VMs available in List VMs response"
                        )
        vm = vm_response[0]
        self.assertEqual(
                            vm.isoid,
                            iso.id,
                            "Check ISO is attached to VM or not"
                        )
        return


class TestVolumes(cloudstackTestCase):

    @classmethod
    def setUpClass(cls):
        cls.api_client = fetch_api_client()
        cls.services = Services().services
        # Get Zone, Domain and templates
        cls.zone = get_zone(cls.api_client, cls.services)
        cls.disk_offering = DiskOffering.create(
                                    cls.api_client,
                                    cls.services["disk_offering"]
                                    )
        template = get_template(
                            cls.api_client,
                            cls.zone.id,
                            cls.services["ostypeid"]
                            )
        cls.services["zoneid"] = cls.zone.id
        cls.services["virtual_machine"]["zoneid"] = cls.zone.id
        cls.services["virtual_machine"]["template"] = template.id
        cls.services["virtual_machine"]["diskofferingid"] = cls.disk_offering.id

        # Create VMs, VMs etc
        cls.account = Account.create(
                            cls.api_client,
                            cls.services["account"]
                            )

        cls.services["account"] = cls.account.account.name
        cls.service_offering = ServiceOffering.create(
                                            cls.api_client,
                                            cls.services["service_offering"]
                                        )
        cls.virtual_machine = VirtualMachine.create(
                                    cls.api_client,
                                    cls.services["virtual_machine"],
                                    accountid=cls.account.account.name,
                                    serviceofferingid=cls.service_offering.id,
                                )

        cls.volume = Volume.create(
                                   cls.api_client,
                                   cls.services["volume"],
                                   zoneid=cls.zone.id,
                                   account=cls.account.account.name,
                                   diskofferingid=cls.disk_offering.id
                                   )
        cls._cleanup = [
                        cls.service_offering,
                        cls.disk_offering,
                        cls.account
                        ]

    @classmethod
    def tearDownClass(cls):
        try:
            cleanup_resources(cls.api_client, cls._cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)

    def setUp(self):
        self.apiclient = self.testClient.getApiClient()
        self.dbclient = self.testClient.getDbConnection()
        self.cleanup = []

    def tearDown(self):
        #Clean up, terminate the created volumes
        cleanup_resources(self.apiclient, self.cleanup)
        return

    def test_01_attach_volume(self):
        """Attach a created Volume to a Running VM
        """
        # Validate the following
        # 1. Create a data volume.
        # 2. List Volumes should not have vmname and virtualmachineid fields in
        #    response before volume attach (to VM)
        # 3. Attch volume to VM. Attach volume should be successful.
        # 4. List Volumes should have vmname and virtualmachineid fields in
        #    response before volume attach (to VM)

        # Check the list volumes response for vmname and virtualmachineid
        list_volume_response = list_volumes(
                                                self.apiclient,
                                                id=self.volume.id
                                                )
        self.assertEqual(
                         isinstance(list_volume_response, list),
                         True,
                         "Check list volumes response for valid list"
                        )
        self.assertNotEqual(
                            list_volume_response,
                            None,
                            "Check if volume exists in ListVolumes"
                            )

        volume = list_volume_response[0]

        self.assertEqual(
                            volume.type,
                            'DATADISK',
                            "Check volume type from list volume response"
                        )

        self.assertEqual(
                            hasattr(volume, 'vmname'),
                            True,
                            "Check whether volume has vmname field"
                            )
        self.assertEqual(
                            hasattr(volume, 'virtualmachineid'),
                            True,
                            "Check whether volume has virtualmachineid field"
                            )

        # Attach volume to VM
        self.debug("Attach volume: %s to VM: %s" % (
                                                    self.volume.id,
                                                    self.virtual_machine.id
                                                    ))
        self.virtual_machine.attach_volume(self.apiclient, self.volume)

        # Check all volumes attached to same VM
        list_volume_response = list_volumes(
                                    self.apiclient,
                                    virtualmachineid=self.virtual_machine.id,
                                    type='DATADISK'
                                    )
        self.assertEqual(
                         isinstance(list_volume_response, list),
                         True,
                         "Check list volumes response for valid list"
                        )
        self.assertNotEqual(
                                list_volume_response,
                                None,
                                "Check if volume exists in ListVolumes"
                                )
        volume = list_volume_response[0]
        self.assertEqual(
                        volume.vmname,
                        self.virtual_machine.name,
                        "Check virtual machine name in list volumes response"
                        )
        self.assertEqual(
                        volume.virtualmachineid,
                        self.virtual_machine.id,
                        "Check VM ID in list Volume response"
                        )
        return

    def test_02_detach_volume(self):
        """Detach a Volume attached to a VM
        """

        # Validate the following
        # 1. Data disk should be detached from instance
        # 2. Listvolumes should not have vmname and virtualmachineid fields for
        #    that volume.

        self.debug("Detach volume: %s to VM: %s" % (
                                                    self.volume.id,
                                                    self.virtual_machine.id
                                                    ))
        self.virtual_machine.detach_volume(self.apiclient, self.volume)

        #Sleep to ensure the current state will reflected in other calls
        time.sleep(self.services["sleep"])

        list_volume_response = list_volumes(
                                            self.apiclient,
                                            id=self.volume.id
                                            )
        self.assertEqual(
                         isinstance(list_volume_response, list),
                         True,
                         "Check list volumes response for valid list"
                        )
        
        self.assertNotEqual(
                            list_volume_response,
                            None,
                            "Check if volume exists in ListVolumes"
                            )
        volume = list_volume_response[0]
        self.assertEqual(
                         volume.virtualmachineid,
                         None,
                         "Check if volume state (detached) is reflected"
                         )

        self.assertEqual(
                         volume.vmname,
                         None,
                         "Check if volume state (detached) is reflected"
                         )
        return

    def test_03_delete_detached_volume(self):
        """Delete a Volume unattached to an VM
        """
        # Validate the following
        # 1. volume should be deleted successfully and listVolume should not
        #    contain the deleted volume details.

        self.debug("Deleting volume: %s" % self.volume.id)
        cmd = deleteVolume.deleteVolumeCmd()
        cmd.id = self.volume.id
        self.apiclient.deleteVolume(cmd)

        #Sleep to ensure the current state will reflected in other calls
        time.sleep(self.services["sleep"])

        list_volume_response = list_volumes(
                                            self.apiclient,
                                            id=self.volume.id,
                                            type='DATADISK'
                                            )
        self.assertEqual(
                        list_volume_response,
                        None,
                        "Check if volume exists in ListVolumes"
                    )
        return
