import json
import os
from optparse import OptionParser
import jsonHelper

class managementServer():
    def __init__(self):
        self.mgtSvrIp = None
        self.port = 8096
        self.apiKey = None
        self.securityKey = None
        
class dbServer():
    def __init__(self):
        self.dbSvr = None
        self.port = 3306
        self.user = "cloud"
        self.passwd = "cloud"
        self.db = "cloud"

class configuration():
    def __init__(self):
        self.name = None
        self.value = None

class logger():
    def __init__(self):
        '''TestCase/TestClient'''
        self.name = None
        self.file = None

class cloudstackConfiguration():
    def __init__(self):
        self.zones = []
        self.mgtSvr = []
        self.dbSvr = None
        self.globalConfig = []
        self.logger = []

class zone():
    def __init__(self):
        self.dns1 = None
        self.internaldns1 = None
        self.name = None
        '''Basic or Advanced'''
        self.networktype = None
        self.dns2 = None
        self.guestcidraddress = None
        self.internaldns2 = None
        self.securitygroupenabled = None
        self.vlan = None
        '''default public network, in advanced mode'''
        self.ipranges = []
        '''tagged network, in advanced mode'''
        self.networks = []
        self.pods = []
        self.secondaryStorages = []
        
class pod():
    def __init__(self):
        self.gateway = None
        self.name = None
        self.netmask = None
        self.startip = None
        self.endip = None
        self.zoneid = None
        self.clusters = []
        '''Used in basic network mode'''
        self.guestIpRanges = []

class cluster():
    def __init__(self):
        self.clustername = None
        self.clustertype = None
        self.hypervisor = None
        self.zoneid = None
        self.podid = None
        self.password = None
        self.url = None
        self.username = None
        self.hosts = []
        self.primaryStorages = []
  
class host():
    def __init__(self):
        self.hypervisor = None
        self.password = None
        self.url = None
        self.username = None
        self.zoneid = None
        self.podid = None
        self.clusterid = None
        self.clustername = None
        self.cpunumber = None
        self.cpuspeed = None
        self.hostmac = None
        self.hosttags = None
        self.memory = None
    
class network():
    def __init__(self):
        self.displaytext = None
        self.name = None
        self.zoneid = None
        self.account = None
        self.domainid = None
        self.isdefault = None
        self.isshared = None
        self.networkdomain = None
        self.networkofferingid = None
        self.ipranges = []
      
class iprange():
    def __init__(self):
        '''tagged/untagged'''
        self.gateway = None
        self.netmask = None
        self.startip = None
        self.endip = None
        self.vlan = None
        '''for account specific '''
        self.account = None
        self.domain = None

class primaryStorage():
    def __init__(self):
        self.name = None
        self.url = None

class secondaryStorage():
    def __init__(self):
        self.url = None
  
'''sample code to generate setup configuration file'''
def describe_setup_in_basic_mode():
    zs = cloudstackConfiguration()
    
    for l in range(1):
        z = zone()
        z.dns1 = "8.8.8.8"
        z.dns2 = "4.4.4.4"
        z.internaldns1 = "192.168.110.254"
        z.internaldns2 = "192.168.110.253"
        z.name = "test"+str(l)
        z.networktype = 'Basic'
    
        '''create 10 pods'''
        for i in range(300):
            p = pod()
            p.name = "test" +str(l) + str(i)
            p.gateway = "192.%d.%d.1"%((i/255)+168,i%255)
            p.netmask = "255.255.255.0"
            
            p.startip = "192.%d.%d.150"%((i/255)+168,i%255)
            p.endip = "192.%d.%d.220"%((i/255)+168,i%255)
        
            '''add two pod guest ip ranges'''
            for j in range(1):
                ip = iprange()
                ip.gateway = p.gateway
                ip.netmask = p.netmask
                ip.startip = "192.%d.%d.%d"%(((i/255)+168), i%255,j*20)
                ip.endip = "192.%d.%d.%d"%((i/255)+168,i%255,j*20+10)
            
                p.guestIpRanges.append(ip)
        
            '''add 10 clusters'''
            for j in range(10):
                c = cluster()
                c.clustername = "test"+str(l)+str(i) + str(j)
                c.clustertype = "CloudManaged"
                c.hypervisor = "Simulator"
            
                '''add 10 hosts'''
                for k in range(1):
                    h = host()
                    h.username = "root"
                    h.password = "password"
                    memory = 8*1024*1024*1024
                    localstorage=1*1024*1024*1024*1024
                    #h.url = "http://Sim/%d%d%d%d/cpucore=1&cpuspeed=8000&memory=%d&localstorage=%d"%(l,i,j,k,memory,localstorage)
                    h.url = "http://Sim/%d%d%d%d"%(l,i,j,k)
                    c.hosts.append(h)
                
                '''add 2 primary storages'''
                '''
                for m in range(2):
                    primary = primaryStorage()
                    size=1*1024*1024*1024*1024
                    primary.name = "primary"+str(l) + str(i) + str(j) + str(m)
                    #primary.url = "nfs://localhost/path%s/size=%d"%(str(l) + str(i) + str(j) + str(m), size)
                    primary.url = "nfs://localhost/path%s"%(str(l) + str(i) + str(j) + str(m))
                    c.primaryStorages.append(primary)
                '''
        
                p.clusters.append(c)
            
            z.pods.append(p)
            
        '''add two secondary'''
        for i in range(5):
            secondary = secondaryStorage()
            secondary.url = "nfs://localhost/path"+str(l) + str(i)
            z.secondaryStorages.append(secondary)
        
        zs.zones.append(z)
    
    '''Add one mgt server'''
    mgt = managementServer()
    mgt.mgtSvrIp = "localhost"
    zs.mgtSvr.append(mgt)
    
    '''Add a database'''
    db = dbServer()
    db.dbSvr = "localhost"
    
    zs.dbSvr = db
    
    '''add global configuration'''
    global_settings = {'expunge.delay': '60',
                       'expunge.interval': '60',
                       'expunge.workers': '3',
                       }
    for k,v in global_settings.iteritems():
        cfg = configuration()
        cfg.name = k
        cfg.value = v
        zs.globalConfig.append(cfg)
    
    ''''add loggers'''
    testClientLogger = logger()
    testClientLogger.name = "TestClient"
    testClientLogger.file = "/tmp/testclient.log"
    
    testCaseLogger = logger()
    testCaseLogger.name = "TestCase"
    testCaseLogger.file = "/tmp/testcase.log"
    
    zs.logger.append(testClientLogger)
    zs.logger.append(testCaseLogger)
    
    return zs

'''sample code to generate setup configuration file'''
def describe_setup_in_advanced_mode():
    zs = cloudstackConfiguration()
    
    for l in range(1):
        z = zone()
        z.dns1 = "8.8.8.8"
        z.dns2 = "4.4.4.4"
        z.internaldns1 = "192.168.110.254"
        z.internaldns2 = "192.168.110.253"
        z.name = "test"+str(l)
        z.networktype = 'Advanced'
        z.guestcidraddress = "10.1.1.0/24"
        z.vlan = "100-2000"
    
        '''create 10 pods'''
        for i in range(2):
            p = pod()
            p.name = "test" +str(l) + str(i)
            p.gateway = "192.168.%d.1"%i
            p.netmask = "255.255.255.0"
            p.startip = "192.168.%d.200"%i
            p.endip = "192.168.%d.220"%i
        
            '''add 10 clusters'''
            for j in range(2):
                c = cluster()
                c.clustername = "test"+str(l)+str(i) + str(j)
                c.clustertype = "CloudManaged"
                c.hypervisor = "Simulator"
            
                '''add 10 hosts'''
                for k in range(2):
                    h = host()
                    h.username = "root"
                    h.password = "password"
                    memory = 8*1024*1024*1024
                    localstorage=1*1024*1024*1024*1024
                    #h.url = "http://Sim/%d%d%d%d/cpucore=1&cpuspeed=8000&memory=%d&localstorage=%d"%(l,i,j,k,memory,localstorage)
                    h.url = "http://Sim/%d%d%d%d"%(l,i,j,k)
                    c.hosts.append(h)
                
                '''add 2 primary storages'''
                for m in range(2):
                    primary = primaryStorage()
                    size=1*1024*1024*1024*1024
                    primary.name = "primary"+str(l) + str(i) + str(j) + str(m)
                    #primary.url = "nfs://localhost/path%s/size=%d"%(str(l) + str(i) + str(j) + str(m), size)
                    primary.url = "nfs://localhost/path%s"%(str(l) + str(i) + str(j) + str(m))
                    c.primaryStorages.append(primary)
        
                p.clusters.append(c)
            
            z.pods.append(p)
            
        '''add two secondary'''
        for i in range(5):
            secondary = secondaryStorage()
            secondary.url = "nfs://localhost/path"+str(l) + str(i)
            z.secondaryStorages.append(secondary)
        
        '''add default public network'''
        ips = iprange()
        ips.vlan = "26"
        ips.startip = "172.16.26.2"
        ips.endip = "172.16.26.100"
        ips.gateway = "172.16.26.1"
        ips.netmask = "255.255.255.0"
        z.ipranges.append(ips)
        
        
        zs.zones.append(z)
    
    '''Add one mgt server'''
    mgt = managementServer()
    mgt.mgtSvrIp = "localhost"
    zs.mgtSvr.append(mgt)
    
    '''Add a database'''
    db = dbServer()
    db.dbSvr = "localhost"
    
    zs.dbSvr = db
    
    '''add global configuration'''
    global_settings = {'expunge.delay': '60',
                       'expunge.interval': '60',
                       'expunge.workers': '3',
                       }
    for k,v in global_settings.iteritems():
        cfg = configuration()
        cfg.name = k
        cfg.value = v
        zs.globalConfig.append(cfg)
    
    ''''add loggers'''
    testClientLogger = logger()
    testClientLogger.name = "TestClient"
    testClientLogger.file = "/tmp/testclient.log"
    
    testCaseLogger = logger()
    testCaseLogger.name = "TestCase"
    testCaseLogger.file = "/tmp/testcase.log"
    
    zs.logger.append(testClientLogger)
    zs.logger.append(testCaseLogger)
    
    return zs

def generate_setup_config(config, file=None):
    describe = config
    if file is None:
        return json.dumps(jsonHelper.jsonDump.dump(describe))
    else:
        fp = open(file, 'w')
        json.dump(jsonHelper.jsonDump.dump(describe), fp, indent=4)
        fp.close()
        
    
def get_setup_config(file):
    if not os.path.exists(file):
        return None
    config = cloudstackConfiguration()
    fp = open(file, 'r')
    config = json.load(fp)
    return jsonHelper.jsonLoader(config)

if __name__ == "__main__":
    parser = OptionParser()
  
    parser.add_option("-o", "--output", action="store", default="./datacenterCfg", dest="output", help="the path where the json config file generated, by default is ./datacenterCfg")
    
    (options, args) = parser.parse_args()
    config = describe_setup_in_basic_mode()
    generate_setup_config(config, options.output)
