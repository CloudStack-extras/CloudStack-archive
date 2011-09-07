#!/usr/bin/env python

'''
############################################################
# Kumquat uses nfs storage, before setting up make sure
#     * optionally turn off stats collectors
#     * expunge.delay and expunge.interval are 60s
############################################################
'''

from optparse import OptionParser
from configGenerator import *
import random


def getGlobalSettings():
    global_settings = {'expunge.delay': '60',
                       'expunge.interval': '60',
                       'capacity.skipcounting.hours': '2',
                       'cpu.overprovisioning.factor': '1.5',
                       'expunge.workers': '3',
                       'workers': '10',
                       'use.user.concentrated.pod.allocation': 'true',
                       'vm.allocation.algorithm': 'random',
                       'vm.op.wait.interval': '5',
                       'guest.domain.suffix': 'kumquat.simulator',
                       'instance.name': 'KIM',
                       'direct.agent.load.size': '16',
                       'default.page.size': '500',
                       'linkLocalIp.nums': '10',
                       'check.pod.cidrs': 'false',
                       'max.account.public.ips': '10000',
                       'max.account.snapshots': '10000',
                       'max.account.templates': '10000',
                       'max.account.user.vms': '10000',
                       'max.account.volumes': '10000',
                      }
    for k, v in global_settings.iteritems():
        cfg = configuration()
        cfg.name = k
        cfg.value = v
        yield cfg


def podIpRangeGenerator():
    x=1
    y=2
    while 1:
        if y == 255:
            x=x+1
            if x == 255:
                x=1
                break

            y=1

        y=y+1            
        #pod mangement network
        yield ('172.'+str(x)+'.'+str(y)+'.129', '172.'+str(x)+'.'+str(y)+'.130', '172.'+str(x)+'.'+str(y)+'.189')


def vlanIpRangeGenerator():
    x=1
    y=2
    while 1:
        if y == 255:
            x=x+1
            if x==255:
                x=1
                break

            y=1

        y=y+1            
        #vlan ip range
        yield ('172.'+str(x)+'.'+str(y)+'.129', '172.'+str(x)+'.'+str(y)+'.190', '172.'+str(x)+'.'+str(y)+'.249')


def describeKumquatResources(dbnode='localhost', mshost='localhost'):
    zs = cloudstackConfiguration()
    numberofpods = 15

    clustersPerPod = 2
    hostsPerCluster = 8

    curpod = 0
    curhost = 0

    z = zone()
    z.dns1 = '4.2.2.2'
    z.dns2 = '192.168.110.254'
    z.internaldns1 = '10.91.28.6'
    z.internaldns2 = '192.168.110.254'
    z.name = 'Kumquat'
    z.networktype = 'Advanced'
    z.guestcidraddress = '10.1.1.0/24'
    z.vlan='100-3000'

    for podRange,vlanRange in zip(podIpRangeGenerator(), vlanIpRangeGenerator()):
        p = pod()
        curpod=curpod+1
        p.name = 'POD'+str(curpod)
        p.gateway=podRange[0]
        p.startip=podRange[1]
        p.endip=podRange[2]
        p.netmask='255.255.255.128'

        for i in range(1,clustersPerPod+1):
            c = cluster()
            c.clustername = 'POD'+str(curpod)+'-CLUSTER'+str(i)
            c.hypervisor = 'Simulator'
            c.clustertype = 'CloudManaged'

            ps = primaryStorage()
            ps.name = 'spool'+str(i)
            ps.url = 'nfs://172.16.24.32/export/path/'+str(curpod)+'/'+str(i)
            c.primaryStorages.append(ps)

            for i in range(1, hostsPerCluster + 1):
                h = host()
                h.username = 'root'
                h.password = 'password'
                h.url = "http://sim/test-%d"%(curhost)
                c.hosts.append(h)
                curhost=curhost+1

            p.clusters.append(c)

        z.pods.append(p)
        if curpod == numberofpods:
            break

    v = iprange()
    v.vlan = 'untagged'
    v.gateway='172.2.1.1'
    v.startip='172.2.1.2'
    v.endip='172.2.255.252'
    v.netmask="255.255.0.0"
    z.ipranges.append(v)

    secondary = secondaryStorage()
    secondary.url = 'nfs://172.16.25.32/secondary/path'
    z.secondaryStorages.append(secondary)
    zs.zones.append(z)

    '''Add mgt server'''
    mgt = managementServer()
    mgt.mgtSvrIp = mshost
    zs.mgtSvr.append(mgt)

    '''Add a database'''
    db = dbServer()
    db.dbSvr = opts.dbnode
    zs.dbSvr = db

    '''Add some configuration'''
    [zs.globalConfig.append(cfg) for cfg in getGlobalSettings()]

    ''''add loggers'''
    testClientLogger = logger()
    testClientLogger.name = 'TestClient'
    testClientLogger.file = '/var/log/testclient.log'

    testCaseLogger = logger()
    testCaseLogger.name = 'TestCase'
    testCaseLogger.file = '/var/log/testcase.log'

    zs.logger.append(testClientLogger)
    zs.logger.append(testCaseLogger)
    return zs


if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option('-o', '--output', action='store', default='./KumquatCfg', dest='output', help='the path where the json config file generated')
    parser.add_option('-d', '--dbnode', dest='dbnode', help='hostname/ip of the database node', action='store')
    parser.add_option('-m', '--mshost', dest='mshost', help='hostname/ip of management server', action='store')

    (opts, args) = parser.parse_args()
    cfg = describeKumquatResources(opts.dbnode, opts.mshost)
    generate_setup_config(cfg, opts.output)
