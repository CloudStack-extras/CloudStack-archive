# -*- encoding: utf-8 -*-
# Copyright 2012 Citrix Systems, Inc. Licensed under the
# Apache License, Version 2.0 (the "License"); you may not use this
# file except in compliance with the License.  Citrix Systems, Inc.
# reserves all rights not expressly granted by the License.
# You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Automatically generated by addcopyright.py at 04/03/2012

""" Component tests for VPC functionality
"""
#Import Local Modules
import marvin
from nose.plugins.attrib import attr
from marvin.cloudstackTestCase import *
from marvin.cloudstackAPI import *
from integration.lib.utils import *
from integration.lib.base import *
from integration.lib.common import *
from marvin.remoteSSHClient import remoteSSHClient
import datetime


class Services:
    """Test VPC services
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
                                    "password": "password",
                                    },
                         "service_offering": {
                                    "name": "Tiny Instance",
                                    "displaytext": "Tiny Instance",
                                    "cpunumber": 1,
                                    "cpuspeed": 100,
                                    "memory": 64,
                                    },
                         "network_offering": {
                                    "name": 'VPC Network offering',
                                    "displaytext": 'VPC Network off',
                                    "guestiptype": 'Isolated',
                                    "supportedservices": 'Vpn,Dhcp,Dns,SourceNat,PortForwarding,Lb,UserData,StaticNat,NetworkACL',
                                    "traffictype": 'GUEST',
                                    "availability": 'Optional',
                                    "useVpc": 'on',
                                    "serviceProviderList": {
                                            "Vpn": 'VpcVirtualRouter',
                                            "Dhcp": 'VpcVirtualRouter',
                                            "Dns": 'VpcVirtualRouter',
                                            "SourceNat": 'VpcVirtualRouter',
                                            "PortForwarding": 'VpcVirtualRouter',
                                            "Lb": 'VpcVirtualRouter',
                                            "UserData": 'VpcVirtualRouter',
                                            "StaticNat": 'VpcVirtualRouter',
                                            "NetworkACL": 'VpcVirtualRouter'
                                        },
                                },
                         "network_offering_no_lb": {
                                    "name": 'VPC Network offering',
                                    "displaytext": 'VPC Network off',
                                    "guestiptype": 'Isolated',
                                    "supportedservices": 'Vpn,Dhcp,Dns,SourceNat,PortForwarding,UserData,StaticNat,NetworkACL',
                                    "traffictype": 'GUEST',
                                    "availability": 'Optional',
                                    "useVpc": 'on',
                                    "serviceProviderList": {
                                            "Vpn": 'VpcVirtualRouter',
                                            "Dhcp": 'VpcVirtualRouter',
                                            "Dns": 'VpcVirtualRouter',
                                            "SourceNat": 'VpcVirtualRouter',
                                            "PortForwarding": 'VpcVirtualRouter',
                                            "UserData": 'VpcVirtualRouter',
                                            "StaticNat": 'VpcVirtualRouter',
                                            "NetworkACL": 'VpcVirtualRouter'
                                        },
                                },
                         "vpc_offering": {
                                    "name": 'VPC off',
                                    "displaytext": 'VPC off',
                                    "supportedservices": 'Dhcp,Dns,SourceNat,PortForwarding,Vpn,Firewall,Lb,UserData,StaticNat',
                                },
                         "vpc": {
                                 "name": "TestVPC",
                                 "displaytext": "TestVPC",
                                 "cidr": '10.0.0.1/24'
                                 },
                         "network": {
                                  "name": "Test Network",
                                  "displaytext": "Test Network",
                                  "netmask": '255.255.255.0'
                                },
                         "lbrule": {
                                    "name": "SSH",
                                    "alg": "leastconn",
                                    # Algorithm used for load balancing
                                    "privateport": 22,
                                    "publicport": 2222,
                                    "openfirewall": False,
                                },
                         "natrule": {
                                    "privateport": 22,
                                    "publicport": 22,
                                    "protocol": "TCP"
                                },
                         "fw_rule": {
                                    "startport": 1,
                                    "endport": 6000,
                                    "cidr": '0.0.0.0/0',
                                    # Any network (For creating FW rule)
                                    "protocol": "TCP"
                                },
                         "virtual_machine": {
                                    "displayname": "Test VM",
                                    "username": "root",
                                    "password": "password",
                                    "ssh_port": 22,
                                    "hypervisor": 'XenServer',
                                    # Hypervisor type should be same as
                                    # hypervisor type of cluster
                                    "privateport": 22,
                                    "publicport": 22,
                                    "protocol": 'TCP',
                                },
                         "ostypeid": '415b9b50-8988-4e79-b582-59a199b779f8',
                         # Cent OS 5.3 (64 bit)
                         "sleep": 60,
                         "timeout": 10,
                         "mode": 'advanced'
                    }


class TestVPC(cloudstackTestCase):

    @classmethod
    def setUpClass(cls):
        cls.api_client = super(
                               TestVPC,
                               cls
                               ).getClsTestClient().getApiClient()
        cls.services = Services().services
        # Get Zone, Domain and templates
        cls.domain = get_domain(cls.api_client, cls.services)
        cls.zone = get_zone(cls.api_client, cls.services)
        cls.template = get_template(
                            cls.api_client,
                            cls.zone.id,
                            cls.services["ostypeid"]
                            )
        cls.services["virtual_machine"]["zoneid"] = cls.zone.id
        cls.services["virtual_machine"]["template"] = cls.template.id

        cls.service_offering = ServiceOffering.create(
                                            cls.api_client,
                                            cls.services["service_offering"]
                                            )
        cls.vpc_off = VpcOffering.create(
                                     cls.api_client,
                                     cls.services["vpc_offering"]
                                     )
        cls.vpc_off.update(cls.api_client, state='Enabled')
        cls._cleanup = [
                        cls.service_offering,
                        cls.vpc_off
                        ]
        return

    @classmethod
    def tearDownClass(cls):
        try:
            #Cleanup resources used
            cleanup_resources(cls.api_client, cls._cleanup)
        except Exception as e:
           raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    def setUp(self):
        self.apiclient = self.testClient.getApiClient()
        self.dbclient = self.testClient.getDbConnection()
        self.account = Account.create(
                                     self.apiclient,
                                     self.services["account"],
                                     admin=True,
                                     domainid=self.domain.id
                                     )
        self.cleanup = [self.account]
        return

    def tearDown(self):
        try:
            #Clean up, terminate the created network offerings
            cleanup_resources(self.apiclient, self.cleanup)
            interval = list_configurations(
                                    self.apiclient,
                                    name='network.gc.interval'
                                    )
            wait = list_configurations(
                                    self.apiclient,
                                    name='network.gc.wait'
                                   )
            # Sleep to ensure that all resources are deleted
            time.sleep(int(interval[0].value) + int(wait[0].value))
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    def validate_vpc_offering(self, vpc_offering):
        """Validates the VPC offering"""

        self.debug("Check if the VPC offering is created successfully?")
        vpc_offs = VpcOffering.list(
                                    self.apiclient,
                                    id=vpc_offering.id
                                    )
        self.assertEqual(
                         isinstance(vpc_offs, list),
                         True,
                         "List VPC offerings should return a valid list"
                         )
        self.assertEqual(
                 vpc_offering.name,
                 vpc_offs[0].name,
                "Name of the VPC offering should match with listVPCOff data"
                )
        self.debug(
                "VPC offering is created successfully - %s" %
                                                        vpc_offering.name)
        return

    def validate_vpc_network(self, network, state=None):
        """Validates the VPC network"""

        self.debug("Check if the VPC network is created successfully?")
        vpc_networks = VPC.list(
                                    self.apiclient,
                                    id=network.id
                          )
        self.assertEqual(
                         isinstance(vpc_networks, list),
                         True,
                         "List VPC network should return a valid list"
                         )
        self.assertEqual(
                 network.name,
                 vpc_networks[0].name,
                "Name of the VPC network should match with listVPC data"
                )
        if state:
            self.assertEqual(
                 vpc_networks[0].state,
                 state,
                "VPC state should be '%s'" % state
                )
        self.debug("VPC network validated - %s" % network.name)
        return

    @attr(tags=["advanced", "intervlan"])
    def test_01_restart_vpc_no_networks(self):
        """ Test restart VPC having no networks
        """

        # Validate the following
        # 1. Create a VPC with cidr - 10.1.1.1/16
        # 2. Restart VPC. Restart VPC should be successful

        self.services["vpc"]["cidr"] = "10.1.1.1/16"
        self.debug("creating a VPC network in the account: %s" %
                                                    self.account.account.name)
        vpc = VPC.create(
                         self.apiclient,
                         self.services["vpc"],
                         vpcofferingid=self.vpc_off.id,
                         zoneid=self.zone.id,
                         account=self.account.account.name,
                         domainid=self.account.account.domainid
                         )
        self.validate_vpc_network(vpc)

        self.debug("Restarting the VPC with no network")
        try:
            vpc.restart(self.apiclient)
        except Exception as e:
            self.fail("Failed to restart VPC network - %s" % e)

        self.validate_vpc_network(vpc, state='Enabled')
        return
    
    @attr(tags=["advanced", "intervlan"])
    def test_02_restart_vpc_with_networks(self):
        """ Test restart VPC having with networks
        """

        # Validate the following
        # 1. Create a VPC with cidr - 10.1.1.1/16
        # 2. Add couple of networks to VPC.
        # 3. Restart VPC. Restart network should be successful

        self.services["vpc"]["cidr"] = "10.1.1.1/16"
        self.debug("creating a VPC network in the account: %s" %
                                                    self.account.account.name)
        vpc = VPC.create(
                         self.apiclient,
                         self.services["vpc"],
                         vpcofferingid=self.vpc_off.id,
                         zoneid=self.zone.id,
                         account=self.account.account.name,
                         domainid=self.account.account.domainid
                         )
        self.validate_vpc_network(vpc)

        self.network_offering = NetworkOffering.create(
                                            self.apiclient,
                                            self.services["network_offering"],
                                            conservemode=False
                                            )
        # Enable Network offering
        self.network_offering.update(self.apiclient, state='Enabled')
        self._cleanup.append(self.network_offering)

        gateway = vpc.cidr.split('/')[0]
        # Split the cidr to retrieve gateway
        # for eg. cidr = 10.0.0.1/24
        # Gateway = 10.0.0.1

        # Creating network using the network offering created
        self.debug("Creating network with network offering: %s" %
                                                    self.network_offering.id)
        network_1 = Network.create(
                                self.apiclient,
                                self.services["network"],
                                accountid=self.account.account.name,
                                domainid=self.account.account.domainid,
                                networkofferingid=self.network_offering.id,
                                zoneid=self.zone.id,
                                gateway=gateway,
                                vpcid=vpc.id
                                )
        self.debug("Created network with ID: %s" % network_1.id)

        self.network_offering_no_lb = NetworkOffering.create(
                                            self.apiclient,
                                            self.services["network_offering_no_lb"],
                                            conservemode=False
                                            )
        # Enable Network offering
        self.network_offering_no_lb.update(self.apiclient, state='Enabled')
        self._cleanup.append(self.network_offering_no_lb)

        gateway = '10.1.2.1'    # New network -> different gateway
        self.debug("Creating network with network offering: %s" %
                                                    self.network_offering_no_lb.id)
        network_2 = Network.create(
                                self.apiclient,
                                self.services["network"],
                                accountid=self.account.account.name,
                                domainid=self.account.account.domainid,
                                networkofferingid=self.network_offering_no_lb.id,
                                zoneid=self.zone.id,
                                gateway=gateway,
                                vpcid=vpc.id
                                )
        self.debug("Created network with ID: %s" % network_2.id)

        self.debug("Restarting the VPC with no network")
        try:
            vpc.restart(self.apiclient)
        except Exception as e:
            self.fail("Failed to restart VPC network - %s" % e)

        self.validate_vpc_network(vpc, state='Enabled')
        return

    @attr(tags=["advanced", "intervlan"])
    def test_03_delete_vpc_no_networks(self):
        """ Test delete VPC having no networks
        """

        # Validate the following
        # 1. Create a VPC with cidr - 10.1.1.1/16
        # 2. Delete VPC. Delete VPC should be successful

        self.services["vpc"]["cidr"] = "10.1.1.1/16"
        self.debug("creating a VPC network in the account: %s" %
                                                    self.account.account.name)
        vpc = VPC.create(
                         self.apiclient,
                         self.services["vpc"],
                         vpcofferingid=self.vpc_off.id,
                         zoneid=self.zone.id,
                         account=self.account.account.name,
                         domainid=self.account.account.domainid
                         )
        self.validate_vpc_network(vpc)

        self.debug("Restarting the VPC with no network")
        try:
            vpc.delete(self.apiclient)
        except Exception as e:
            self.fail("Failed to delete VPC network - %s" % e)

        self.debug("Check if the VPC offering is deleted successfully?")
        vpcs = VPC.list(
                                    self.apiclient,
                                    id=vpc.id
                                    )
        self.assertEqual(
                         vpcs,
                         None,
                         "List VPC offerings should not return anything"
                         )
        return
    
    @attr(tags=["advanced", "intervlan"])
    def test_04_delete_vpc_with_networks(self):
        """ Test delete VPC having with networks
        """

        # Validate the following
        # 1. Create a VPC with cidr - 10.1.1.1/16
        # 2. Add couple of networks to VPC.
        # 3. Delete VPC. Delete network should be successful
        # 4. Virtual Router should be deleted
        # 5. Source NAT should be released back to pool

        self.services["vpc"]["cidr"] = "10.1.1.1/16"
        self.debug("creating a VPC network in the account: %s" %
                                                    self.account.account.name)
        vpc = VPC.create(
                         self.apiclient,
                         self.services["vpc"],
                         vpcofferingid=self.vpc_off.id,
                         zoneid=self.zone.id,
                         account=self.account.account.name,
                         domainid=self.account.account.domainid
                         )
        self.validate_vpc_network(vpc)

        self.network_offering = NetworkOffering.create(
                                            self.apiclient,
                                            self.services["network_offering"],
                                            conservemode=False
                                            )
        # Enable Network offering
        self.network_offering.update(self.apiclient, state='Enabled')
        self._cleanup.append(self.network_offering)

        gateway = vpc.cidr.split('/')[0]
        # Split the cidr to retrieve gateway
        # for eg. cidr = 10.0.0.1/24
        # Gateway = 10.0.0.1

        # Creating network using the network offering created
        self.debug("Creating network with network offering: %s" %
                                                    self.network_offering.id)
        network_1 = Network.create(
                                self.apiclient,
                                self.services["network"],
                                accountid=self.account.account.name,
                                domainid=self.account.account.domainid,
                                networkofferingid=self.network_offering.id,
                                zoneid=self.zone.id,
                                gateway=gateway,
                                vpcid=vpc.id
                                )
        self.debug("Created network with ID: %s" % network_1.id)

        self.network_offering_no_lb = NetworkOffering.create(
                                            self.apiclient,
                                            self.services["network_offering_no_lb"],
                                            conservemode=False
                                            )
        # Enable Network offering
        self.network_offering_no_lb.update(self.apiclient, state='Enabled')
        self._cleanup.append(self.network_offering_no_lb)

        gateway = '10.1.2.1'    # New network -> different gateway
        self.debug("Creating network with network offering: %s" %
                                                    self.network_offering_no_lb.id)
        network_2 = Network.create(
                                self.apiclient,
                                self.services["network"],
                                accountid=self.account.account.name,
                                domainid=self.account.account.domainid,
                                networkofferingid=self.network_offering_no_lb.id,
                                zoneid=self.zone.id,
                                gateway=gateway,
                                vpcid=vpc.id
                                )
        self.debug("Created network with ID: %s" % network_2.id)

        self.debug("Deleting the VPC with no network")
        with self.assertRaises(Exception):
            vpc.delete(self.apiclient)
        self.debug("Delete VPC failed as there are still networks in VPC")
        self.debug("Deleting the networks in the VPC")

        try:
            network_1.delete(self.apiclient)
            network_2.delete(self.apiclient)
        except Exception as e:
            self.fail("failed to delete the VPC networks: %s" % e)

        self.debug("Now trying to delete VPC")
        try:
            vpc.delete(self.apiclient)
        except Exception as e:
            self.fail("Delete to restart VPC network - %s" % e)

        self.debug("Check if the VPC offering is deleted successfully?")
        vpcs = VPC.list(
                        self.apiclient,
                        id=vpc.id
                        )
        self.assertEqual(
                         vpcs,
                         None,
                         "List VPC offerings should not return anything"
                         )
        self.debug("Waiting for network.gc.interval to cleanup network resources")
        interval = list_configurations(
                                    self.apiclient,
                                    name='network.gc.interval'
                                    )
        wait = list_configurations(
                                    self.apiclient,
                                    name='network.gc.wait'
                                   )
        # Sleep to ensure that all resources are deleted
        time.sleep(int(interval[0].value) + int(wait[0].value))
        self.debug("Check if VR is deleted or not?")
        routers = Router.list(
                            self.apiclient,
                            account=self.account.account.name,
                            domainid=self.account.account.domainid,
                            listall=True
                            )
        self.assertEqual(
                        routers,
                        None,
                        "List Routers for the account should not return any response"
                        )
        return

    @attr(tags=["advanced", "intervlan"])
    def test_05_list_vpc_apis(self):
        """ Test list VPC APIs
        """

        # Validate the following
        # 1. Create multiple VPCs
        # 2. listVPCs() by name. VPC with the provided name should be listed.
        # 3. listVPCs() by displayText. VPC with the provided displayText
        #    should be listed.
        # 4. listVPCs() by cidr. All the VPCs with the provided cidr should
        #    be listed.
        # 5. listVPCs() by vpcofferingId.All the VPCs with the vpcofferingId
        #    should be listed.
        # 6. listVPCs() by supported Services(). All the VPCs that provide the
        #    list of services should be listed.
        # 7. listVPCs() by restartRequired (set to true). All the VPCs that
        #    require restart should be listed.

        self.services["vpc"]["cidr"] = "10.1.1.1/16"
        self.debug("creating a VPC network in the account: %s" %
                                                    self.account.account.name)
        vpc_1 = VPC.create(
                         self.apiclient,
                         self.services["vpc"],
                         vpcofferingid=self.vpc_off.id,
                         zoneid=self.zone.id,
                         account=self.account.account.name,
                         domainid=self.account.account.domainid
                         )
        self.validate_vpc_network(vpc_1)

        self.services["vpc"]["cidr"] = "10.1.46.1/16"
        vpc_2 = VPC.create(
                         self.apiclient,
                         self.services["vpc"],
                         vpcofferingid=self.vpc_off.id,
                         zoneid=self.zone.id,
                         account=self.account.account.name,
                         domainid=self.account.account.domainid
                         )
        self.validate_vpc_network(vpc_2)

        self.debug("Check list VPC API by Name?")
        vpcs = VPC.list(
                        self.apiclient,
                        name=vpc_1.name,
                        listall=True
                        )
        self.assertEqual(
                        isinstance(vpcs, list),
                        True,
                        "List VPC shall return a valid resposne"
                        )
        vpc = vpcs[0]
        self.assertEqual(
                         vpc.name,
                         vpc_1.name,
                         "VPC name should match with the existing one"
                         )

        self.debug("Check list VPC API by displayText?")
        vpcs = VPC.list(
                        self.apiclient,
                        displaytext=vpc_1.displaytext,
                        listall=True
                        )
        self.assertEqual(
                        isinstance(vpcs, list),
                        True,
                        "List VPC shall return a valid resposne"
                        )
        vpc = vpcs[0]
        self.assertEqual(
                         vpc.displaytext,
                         vpc_1.displaytext,
                         "VPC displaytext should match with the existing one"
                         )

        self.debug("Check list VPC API by cidr?")
        vpcs = VPC.list(
                        self.apiclient,
                        cidr=vpc_2.cidr,
                        listall=True
                        )
        self.assertEqual(
                        isinstance(vpcs, list),
                        True,
                        "List VPC shall return a valid resposne"
                        )
        vpc = vpcs[0]
        self.assertEqual(
                         vpc.cidr,
                         vpc_2.cidr,
                         "VPC cidr should match with the existing one"
                         )
        self.debug("Validating list VPC by Id")
        self.validate_vpc_network(vpc_1)

        self.debug("Validating list VPC by vpcofferingId")
        vpcs = VPC.list(
                        self.apiclient,
                        vpcofferingid=self.vpc_off.id,
                        listall=True
                        )
        self.assertEqual(
                        isinstance(vpcs, list),
                        True,
                        "List VPC by vpcofferingId should return a valid response"
                    )
        self.debug("Length of list VPC response: %s" % len(vpcs))
        self.assertEqual(
                        len(vpcs),
                        2,
                        "List VPC should return 3 enabled VPCs"
                        )
        for vpc in vpcs:
            self.assertEqual(
                            vpc.vpcofferingid,
                            self.vpc_off.id,
                            "VPC offering ID should match with that of resposne"
                            )

        self.debug("Validating list VPC by supportedservices")
        vpcs = VPC.list(
                        self.apiclient,
                        supportedservices='Vpn,Dhcp,Dns,SourceNat,PortForwarding,Lb,UserData,StaticNat,NetworkACL',
                        listall=True,
                        account=self.account.account.name,
                        domainid=self.account.account.domainid
                        )
        self.assertEqual(
                        isinstance(vpcs, list),
                        True,
                        "List VPC by vpcofferingId should return a valid response"
                    )
        for vpc in vpcs:
            self.assertIn(
                            vpc.id,
                            [vpc_1.id, vpc_2.id],
                            "VPC offering ID should match with that of resposne"
                            )
        self.debug("Validating list VPC by restart required")
        vpcs = VPC.list(
                        self.apiclient,
                        restartrequired=True,
                        listall=True,
                        account=self.account.account.name,
                        domainid=self.account.account.domainid
                        )
	if vpcs is not None:
	    for vpc in vpcs:
            	self.assertEqual(
                            vpc.restartrequired,
                            True,
                            "RestartRequired should be set as True"
                            )
        self.debug("Validating list VPC by restart required")
        vpcs = VPC.list(
                        self.apiclient,
                        restartrequired=False,
                        listall=True,
                        account=self.account.account.name,
                        domainid=self.account.account.domainid
                        )
        self.assertEqual(
                        isinstance(vpcs, list),
                        True,
                        "List VPC by vpcofferingId should return a valid response"
                    )
	if vpcs is not None:
	    for vpc in vpcs:
            	self.assertEqual(
                            vpc.restartrequired,
                            False,
                            "RestartRequired should be set as False"
                            )
        return

    @attr(tags=["advanced", "intervlan"])
    def test_06_list_vpc_apis_admin(self):
        """ Test list VPC APIs for different user roles
        """

        # Validate the following
        # 1. list VPCS as admin User to view all the Vpcs owned by admin user
        # 2. list VPCS as regular User to view all the Vpcs owned by user
        # 3. list VPCS as domain admin User to view all the Vpcs owned by admin

        self.user = Account.create(
                                     self.apiclient,
                                     self.services["account"],
                                )
        self.cleanup.append(self.user)

        self.services["vpc"]["cidr"] = "10.1.1.1/16"
        self.debug("creating a VPC network in the account: %s" %
                                                    self.account.account.name)
        vpc_1 = VPC.create(
                         self.apiclient,
                         self.services["vpc"],
                         vpcofferingid=self.vpc_off.id,
                         zoneid=self.zone.id,
                         account=self.account.account.name,
                         domainid=self.account.account.domainid
                         )
        self.validate_vpc_network(vpc_1)

        self.services["vpc"]["cidr"] = "10.1.46.1/16"
        vpc_2 = VPC.create(
                         self.apiclient,
                         self.services["vpc"],
                         vpcofferingid=self.vpc_off.id,
                         zoneid=self.zone.id,
                         account=self.user.account.name,
                         domainid=self.user.account.domainid
                         )
        self.validate_vpc_network(vpc_2)

        self.debug("Validating list VPCs call by passing account and domain")
        vpcs = VPC.list(
                        self.apiclient,
                        account=self.user.account.name,
                        domainid=self.user.account.domainid,
                        listall=True
                    )
        self.assertEqual(
                    isinstance(vpcs, list),
                    True,
                    "List VPC should return a valid response"
                )
        vpc = vpcs[0]
        self.assertEqual(
                        vpc.id,
                        vpc_2.id,
                        "List VPC should return VPC belonging to that account"
                        )
        return
