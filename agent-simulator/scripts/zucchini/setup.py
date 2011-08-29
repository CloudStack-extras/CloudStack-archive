#!/usr/bin/env python

'''
############################################################
# zucchini uses local storage, before setting up make sure
#     * xen.public.network.device is set
#     * use.local.storage and systemvm.use.local.storage are true
#     * optionally turn off stats collectors
#     * expunge.delay and expunge.interval are 60s
#     * ping.interval is around 3m
#     * turn off dns updates to entire zone, network.dns.basiczone.update=pod
#     * capacity.skipcounting.hours=0
#     * direct.agent.load.size=1000
#     * security groups are enabled
#
#   This script will only setup an approximate number of hosts. To achieve the ratio
#   of 5:2:6 hosts the total number of hosts is brought close to a number divisible
#   by 13. So if 4000 hosts are added, you might see only 3900 come up
#   
#   10 hosts per pod @ 1 host per cluster in a single zone
#   
#   Each pod has a /25, so 128 addresses. I think we reserved 5 IP addresses for system VMs in each pod. 
#   Then we had something like 60 addresses for hosts and 60 addresses for VMs.

#Environment
#1. Approximately 10 hosts per pod. 
#2. Only 3 host tags. 
#3. With in each pod, the host tags are the same. Homogenous Pods
#4. The ratio of hosts for the three tags should be 5/2/6
#5. In simulator.properties, workers=1
############################################################
'''

from optparse import OptionParser
from configGenerator import *
import random

def getGlobalSettings():
    global_settings = {'expunge.delay': '60',
                       'expunge.interval': '60',
                       'expunge.workers': '3',
                       'workers': '10',
                       'network.dns.basiczone.updates':'pod',
                       'use.user.concentrated.pod.allocation':'false',
                       'vm.allocation.algorithm':'firstfit',
                       'capacity.check.period':'0',
#                       'host.stats.interval':'-1',
#                       'vm.stats.interval':'-1',
#                       'storage.stats.interval':'-1',
#                       'router.stats.interval':'-1',
                       'vm.op.wait.interval':'5',
                       'xen.public.network.device':'10.10.10.10', #only a dummy for the simulator
                       'guest.domain.suffix':'zcloud.simulator',
                       'instance.name':'ZIM',
                       'direct.agent.load.size':'1000',
                       'default.page.size':'10000',
                       'linkLocalIp.nums':'4',
                       'system.vm.use.local.storage':'true',
                       'use.local.storage':'true',
                       'check.pod.cidrs':'false',
                      }
    for k,v in global_settings.iteritems():
        cfg=configuration()
        cfg.name=k
        cfg.value=v
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


def describeZyngaResources(numberOfAgents=1300, dbnode='localhost', mshost='localhost', randomize=False):
    zs=cloudstackConfiguration()
    numberofpods=numberOfAgents/10
    tagOneHosts = numberOfAgents*5/13
    tagTwoHosts = numberOfAgents*2/13
    tagThreeHosts = numberOfAgents-tagOneHosts-tagTwoHosts

    clustersPerPod=10
    hostsPerPod=10

    z = zone()
    z.dns1 = '4.2.2.2'
    z.dns2 = '192.168.110.254'
    z.internaldns1 = '10.91.28.6'
    z.internaldns2 = '192.168.110.254'
    z.name = 'Zynga'
    z.networktype = 'Basic'    

    hosttags=['TAG1' for x in range(tagOneHosts)] + ['TAG2' for x in range(tagTwoHosts)] + ['TAG3' for x in range(tagThreeHosts)]
    if randomize:
        random.shuffle(hosttags) #randomize the host distribution     
        curhost=0
        curpod=0

    for podRange,vlanRange in zip(podIpRangeGenerator(), vlanIpRangeGenerator()):
        p = pod()
        curpod=curpod+1
        p.name = 'POD'+str(curpod)
        p.gateway=podRange[0]
        p.startip=podRange[1]
        p.endip=podRange[2]
        p.netmask='255.255.255.128'

        v = iprange()
        v.gateway=vlanRange[0]        
        v.startip=vlanRange[1]
        v.endip=vlanRange[2]
        v.netmask="255.255.255.128"
        p.guestIpRanges.append(v)

        for i in range(1,clustersPerPod+1):
            c = cluster()
            c.clustername = 'POD'+str(curpod)+'-CLUSTER'+str(i)
            c.hypervisor = 'Simulator'
            c.clustertype = 'CloudManaged'

            try:
                h = host()
                h.username = 'root'
                h.password = 'password'
                h.url = "http://sim/test-%d"%(curhost)
                h.hosttags = hosttags.pop()
                c.hosts.append(h)
                curhost=curhost+1
                p.clusters.append(c)
            except IndexError:
                break            
        #end clusters
        z.pods.append(p)
        if curpod == numberofpods or curhost == numberOfAgents:
            break
    #end pods
    secondary = secondaryStorage()
    secondary.url = 'nfs://172.16.25.32/secondary/path'
    z.secondaryStorages.append(secondary)
    zs.zones.append(z)

    '''Add mgt server'''
    mgt = managementServer()
    mgt.mgtSvrIp = mshost #could be the LB balancing the cluster of management server as well
    zs.mgtSvr.append(mgt)

    '''Add a database'''
    db = dbServer()
    db.dbSvr = opts.dbnode
    zs.dbSvr = db

    '''Add some configuration'''
    [zs.globalConfig.append(cfg) for cfg in getGlobalSettings()]

    ''''add loggers'''
    testClientLogger = logger()
    testClientLogger.name = "TestClient"
    testClientLogger.file = "/var/log/testclient.log"

    testCaseLogger = logger()
    testCaseLogger.name = "TestCase"
    testCaseLogger.file = "/var/log/testcase.log"

    zs.logger.append(testClientLogger)
    zs.logger.append(testCaseLogger)
    return zs   

if __name__=="__main__":
    parser = OptionParser()
    #    parser.add_option('-h','--host',dest='host',help='location of management server(s) or load-balancer')
    parser.add_option('-n', '--number-of-agents', action='store', dest='agents', help='number of agents in the deployment')
    parser.add_option('-g', '--enable-security-groups', dest='sgenabled', help='specify if security groups are to be enabled', default=False, action='store_true')
    parser.add_option('-o', '--output', action='store', default='./zucchiniCfg', dest='output', help='the path where the json config file generated')
    parser.add_option('-d', '--dbnode', dest='dbnode', help='hostname/ip of the database node', action='store')
    parser.add_option('-m', '--mshost', dest='mshost', help='hostname/ip of management server', action='store')
    parser.add_option('-r', '--randomize', dest='randomize', help='randomize the distribution of tags (hetergenous clusters)', action='store_true', default=False)

    (opts, args) = parser.parse_args()
    mandatories = ['mshost', 'dbnode', 'agents']
    for m in mandatories:
        if not opts.__dict__[m]:
            print "mandatory option missing"

    cfg = describeZyngaResources(int(opts.agents), opts.dbnode, opts.mshost, opts.randomize)
    generate_setup_config(cfg, opts.output)
