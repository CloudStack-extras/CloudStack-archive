'''
Created on Jul 11, 2012

@author: frank
'''

import os.path
import xml.etree.ElementTree as xml
import cherrypy
import subprocess

XML_VM_NAME = 'vmName'
XML_VM_ID = 'vmId'
XML_VM_DOMAIN_ID = 'domainId'
XML_VM_IP = 'vmIp'
XML_VM_MAC = 'vmMac'
XML_VM_SIGNATURE = 'signature'
XML_VM_SEQUENCE_NUMBER = 'sequenceNumber'
XML_VM_INGRESS_RULES = 'ingressRules'
XML_VM_EGRESS_RULES = 'egressRules'
XML_HYPERVISOR_TYPE = 'hypervisorType'

VM_LOG_ROOT_TAG = 'SecurityGroupVmLog'
RULE_SET_ROOT_TAG = 'SecurityGroupVmRuleSet'

IPSET_TYPE = 'iptreemap'

class RuleSet(object):
    def __init__(self):
        self.vmName = None
        self.vmIp = None
        self.vmMac = None
        self.vmId = None
        self.hypervisorType = None
        self.domainId = None
        self.signature = None
        self.seqNumber = None
        self.ingressRules = None
        self.egressRules = None
        self.interfaces = []
        
class Rule(object):
    def __init__(self):
        self.protocol = None
        self.startPort = None
        self.endPort = None
        self.ips = None
        
class VmLog(object):
    rootPath = "/var/run/cloud/"
    
    def __init__(self):
        self.vmName = None
        self.vmId = None
        self.vmIp = None
        self.vmMac = "ff:ff:ff:ff:ff:ff"
        self.domainId = None
        self.signature = None
        self.sequenceNumber = None
        self.filePath = None
    
    def writeToFile(self):
        assert self.vmName != None
        assert self.vmId != None
        assert self.vmIp != None
        assert self.domainId != None
        assert self.signature != None
        assert self.sequenceNumber != None
        if self.filePath == None:
            self.filePath = os.path.join(VmLog.rootPath, "%.xml"%self.vmName)
            
        root = xml.Element(VM_LOG_ROOT_TAG)
        vmName = xml.SubElement(root, XML_VM_NAME)
        vmName.text = self.vmName
        vmId = xml.SubElement(root, XML_VM_ID)
        vmId.text = self.vmId
        domainId = xml.SubElement(root, XML_VM_DOMAIN_ID)
        domainId.text = self.domainId
        signature = xml.SubElement(root, XML_VM_SIGNATURE)
        signature.text = self.signature
        sequenceNumber = xml.SubElement(root, XML_VM_SEQUENCE_NUMBER)
        sequenceNumber.text = self.sequenceNumber
        vmIp = xml.SubElement(root, XML_VM_IP)
        vmIp.text = self.vmIp
        vmMac = xml.SubElement(root, XML_VM_MAC)
        vmMac.text = self.vmMac
        
        tree = xml.ElementTree(root)
        tree.write(self.filePath)
    
    def compare(self, log2):
        if self.domainId != log2.domainId or self.vmId != log2.vmId or self.vmIp != log2.vmIp:
            cherrypy.log('Default info of vm[%s] changed, reprogramm all'%self.vmName)
            return [True, True, True]
        
        reprogramDefault = False
        reprogramChain = False
        rewriteLog = True
        if int(log2.sequenceNumber) > int(self.sequenceNumber):
            if log2.signature != self.signature:
                cherrpy.log('sequence number increased from %s to %s, reprogramm rules for vm[%s]'%(self.sequenceNumber, log2.sequenceNumber, self.vmName))
                reprogramChain = True
        elif int(log2.sequenceNumber) < int(self.sequenceNumber):
            rewriteLog = False
        elif log2.signature != self.signature:
            cherrypy.log('signagure of vm[%s] changed, reprogramm rules'%self.vmName)
            rewriteLog = True
            reprogramChain = True
        else:
            cherrypy.log('nothing changed, no need to reprogramm anything of vm[%s]'%self.vmName)
            rewriteLog = False
        return [reprogramDefault, reprogramChain, rewriteLog]
    
    @staticmethod
    def s_fromRuleSet(ruleSet):
        log = VmLog()
        log.vmName = ruleSet.vmName
        log.vmId = ruleSet.vmId
        log.vmMac = ruleSet.vmMac
        log.vmIp = ruleSet.vmIp
        log.sequenceNumber = ruleSet.sequenceNumber
        log.signature =  ruleSet.signature
        log.domainId = ruleSet.domainId
        return log
    
    @staticmethod
    def s_fromXmlFile(vmName):
        log = VmLog()
        fp = os.path.join(VmLog.rootPath, "%s.xml"%vmName)
        log.filePath = fp
        if not os.path.exists(fp):
            return log
        
        tree = xml.parse(fp);
        root = tree.getroot()
        log.vmName = root.find(XML_VM_NAME).text
        log.vmId = root.find(XML_VM_ID).text
        log.vmIp = root.find(XML_VM_IP).text
        log.vmMac = root.find(XML_VM_MAC).text
        log.domainId = root.find(XML_VM_DOMAIN_ID).text
        log.signature = root.find(XML_VM_SIGNATURE).text
        log.sequenceNumber = root.find(XML_VM_SEQUENCE_NUMBER).text
        return log
    
class BashExceutedFailedException(Exception):
    stderr = ''
    errCode = -1000
    
    def __init__(self, err, code):
        Exception.__init__(self, "%s, return code:%s"%(err, code))
        self.stderr = err
        self.errCode = code
        
class Utils(object):
    @staticmethod
    def printObj(obj):
        contents = []
        for attr, val in obj.__dict__.iteritems():
            content.append('%s=%s'%s(attr, val))
        cherrypy.log('[%s]' % ','.join(contents))
        
    @staticmethod
    def runBash(cmdstr):
        process = subprocess.Popen(cmdstr, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        stdout, stderr = process.communicate()
        if process.returncode != 0:
            raise BashExceutedFailedException(stderr, process.returncode)
        return stdout
    
    @staticmethod
    def runBashSilent(cmdstr):
        process = subprocess.Popen(cmdstr, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        process.communicate()
    
    
    
class HypervisorStrategy(object):
    '''
    classdocs
    '''

    def __init__(self):
        '''
        Constructor
        '''
        pass
    
    def prepareHostEnvironment(self):
        raise NotImplementedError("This method must be implemented by derived class")
    
    def prepareRuleSet(self, ruleSet):
        raise NotImplementedError("This method must be implemented by derived class")
        
