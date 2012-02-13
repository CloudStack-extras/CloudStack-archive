#!/usr/bin/python

import os
import os.path
import sys
from xml.dom import minidom
from xml.parsers.expat import ExpatError


REGULAR_USER = 'u'
DOMAIN_ADMIN = 'd'
ROOT_ADMIN = 'r'

user_to_func = {
    REGULAR_USER: 'populateForUser',
    DOMAIN_ADMIN: 'populateForDomainAdmin',
    ROOT_ADMIN: 'populateForRootAdmin',
    }


user_to_cns = {
    REGULAR_USER: 'userCommandNames',
    DOMAIN_ADMIN: 'domainAdminCommandNames',
    ROOT_ADMIN: 'rootAdminCommandNames',
    }


dirname_to_user = {
    'regular_user': REGULAR_USER,
    'domain_admin': DOMAIN_ADMIN,
    'root_admin': ROOT_ADMIN,
    }


dirname_to_dirname = {
    'regular_user': 'user',
    'domain_admin': 'domain_admin',
    'root_admin': 'root_admin',
    }


known_categories = {
    'SystemVm': 'System VM',
    'VirtualMachine': 'Virtual Machine',
    'VM': 'Virtual Machine',
    'Domain': 'Domain',
    'Template': 'Template',
    'Iso': 'ISO',
    'Volume': 'Volume',
    'Vlan': 'VLAN',
    'IpAddress': 'Address',
    'PortForwarding': 'Firewall',
    'Firewall': 'Firewall',
    'StaticNat': 'NAT',
    'IpForwarding': 'NAT',
    'Host': 'Host',
    'Cluster': 'Cluster',
    'Account': 'Account',
    'Snapshot': 'Snapshot',
    'User': 'User',
    'Os': 'Guest OS',
    'ServiceOffering': 'Service Offering',
    'DiskOffering': 'Disk Offering',
    'LoadBalancer': 'Load Balancer',
    'Router': 'Router',
    'SystemVm': 'System VM',
    'Configuration': 'Configuration',
    'Capabilities': 'Configuration',
    'Pod': 'Pod',
    'Zone': 'Zone',
    'NetworkOffering': 'Network Offering',
    'Network': 'Network',
    'Vpn': 'VPN',
    'Limit': 'Limit',
    'ResourceCount': 'Limit',
    'CloudIdentifier': 'Cloud Identifier',
    'InstanceGroup': 'VM Group',
    'StorageMaintenance': 'Storage Pool',
    'StoragePool': 'Storage Pool',
    'SecurityGroup': 'Security Group',
    'SSH': 'SSH',
    'register': 'Registration',
    'AsyncJob': 'Async job',
    'Certificate': 'Certificate',
    'Hypervisor': 'Hypervisor',
    'Alert': 'Alert',
    'Event': 'Event',
    'login': 'Login',
    'logout': 'Logout',
    'Capacity': 'System Capacity',
    'NetworkDevice': 'Network Device',
    'ExternalLoadBalancer': 'Ext Load Balancer',
    'ExternalFirewall': 'Ext Firewall',
    'Usage': 'Usage',
    'TrafficMonitor': 'Usage',
    'TrafficType': 'Usage',
    'Product': 'Product',
    'LB': 'Load Balancer',
    'ldap': 'LDAP',
    'Swift': 'Swift',
    'SecondaryStorage': 'Host',
    'Project': 'Project',
    'Lun': 'Storage',
    'Pool': 'Pool',
    }


categories = {}


def choose_category(fn):
    for k, v in known_categories.iteritems():
        if k in fn:
            return v
    raise Exception(fn)


for f in sys.argv:
    dirname, fn = os.path.split(f)
    if not fn.endswith('.xml'):
        continue
    if fn.endswith('Summary.xml'):
        continue
    if fn.endswith('SummarySorted.xml'):
        continue
    if fn == 'alert_types.xml':
        continue
    if dirname.startswith('./'):
        dirname = dirname[2:]
    try:
        dom = minidom.parse(file(f))
        name = dom.getElementsByTagName('name')[0].firstChild.data
        isAsync = dom.getElementsByTagName('isAsync')[0].firstChild.data
        category = choose_category(fn)
        if category not in categories:
            categories[category] = []
        categories[category].append({
            'name': name,
            'dirname': dirname_to_dirname[dirname],
            'async': isAsync == 'true',
            'user': dirname_to_user[dirname],
            })
    except ExpatError, e:
        pass
    except IndexError, e:
        print fn
    

def xml_for(command):
    name = command['name']
    async = command['async'] and ' (A)' or ''
    dirname = command['dirname']
    return '''<xsl:if test="name=\'%(name)s\'">
<li><a href="%(dirname)s/%(name)s.html"><xsl:value-of select="name"/>%(async)s</a></li>
</xsl:if>
''' % locals()


def write_xml(out, user):
    with file(out, 'w') as f:
        cat_strings = []
        
        for category in categories.keys():
            strings = []
            for command in categories[category]:
                if command['user'] == user:
                    strings.append(xml_for(command))
            if strings:
                all_strings = ''.join(strings)
                cat_strings.append((len(strings), category, all_strings))

        cat_strings.sort(reverse=True)

        i = 0
        for _1, category, all_strings in cat_strings:
            if i == 0:
                print >>f, '<div class="apismallsections">'
            print >>f, '''<div class="apismallbullet_box">
<h5>%(category)s</h5>
<ul>
<xsl:for-each select="commands/command">
%(all_strings)s
</xsl:for-each>
</ul>      
</div>

''' % locals()
            if i == 3:
                print >>f, '</div>'
                i = 0
            else:
                i += 1
        if i != 0:
            print >>f, '</div>'


def java_for(command, user):
    name = command['name']
    cns = user_to_cns[user]
    return '''%(cns)s.add("%(name)s");
''' % locals()


def java_for_user(user):
    strings = []
    for category in categories.keys():
        for command in categories[category]:
            if command['user'] == user:
                strings.append(java_for(command, user))
    func = user_to_func[user]        
    all_strings = ''.join(strings)
    return '''
    public void %(func)s() {
        %(all_strings)s
    }
''' % locals()


def write_java(out):
    with file(out, 'w') as f:
        print >>f, '''/* Generated using gen_toc.py.  Do not edit. */

import java.util.HashSet;
import java.util.Set;

public class XmlToHtmlConverterData {

	Set<String> rootAdminCommandNames = new HashSet<String>();
	Set<String> domainAdminCommandNames = new HashSet<String>();
	Set<String> userCommandNames = new HashSet<String>();

'''
        print >>f, java_for_user(REGULAR_USER)
        print >>f, java_for_user(ROOT_ADMIN)
        print >>f, java_for_user(DOMAIN_ADMIN)

        print >>f, '''
}
'''


write_xml('generatetocforuser_include.xsl', REGULAR_USER)
write_xml('generatetocforadmin_include.xsl', ROOT_ADMIN)
write_xml('generatetocfordomainadmin_include.xsl', DOMAIN_ADMIN)
write_java('XmlToHtmlConverterData.java')
