'''
Created on Jul 12, 2012

@author: frank
'''

import cherryp
import Header
from Header import *
import xml.etree.ElementTree as xml
import uuid

class SecurityGroupAgent(object):
    def __init__(self):
        self.hypervisorStrategies = {}
        self.ipsetKeyWord = self._getIpSetKeyWord()
        
    def _chainName(self, vmName):
        if vm_name.startswith('i-') or vm_name.startswith('r-'):
            if vm_name.endswith('untagged'):
                return '-'.join(vm_name.split('-')[:-1])
        return vm_name
    
    def _egressChainName(self, vmName):
        return "%s-eg" % self._chainName(vmName)

    def _parseRuleDoc(self, doc):
        def xmlDocToRule(ruleDocs):
            rules = []
            for doc in ruleDocs:
                rule = Rule()
                rule.protocol = doc.find('protocol').text
                rule.startPort = doc.find('startPort').text
                rule.endPort = doc.find('endPort').text
                ips = doc.findall('ip')
                rule.ips = []
                for ip in ips:
                    rule.ips.append(ip.text)
                rules.append(rule)
            return rules
                
        tree = xml.fromstring(doc)
        if tree.tag != "SecurityGroupVmRuleSet":
            raise ValueError("Invalid xml doc, the root element must be: 'SecurityGroupVmRuleSet'") 
        
        rs = RuleSet()
        rs.vmName = tree.find(Header.XML_VM_NAME).text
        rs.vmId = tree.find(Header.XML_VM_ID).text
        rs.vmIp = tree.find(Header.XML_VM_IP).text
        rs.vmMac = tree.find(Header.XML_VM_MAC).text
        rs.signature = tree.find(Header.XML_VM_SIGNATURE).text
        rs.sequenceNumber = tree.find(Header.XML_VM_SEQUENCE_NUMBER).text
        ingressRules = tree.findall(Header.XML_VM_INGRESS_RULES)
        rs.ingressRules = xmlDocToRule(ingressRules)
        egressRules = tree.findall(Header.XML_VM_EGRESS_RULES)
        rs.egressRules = xmlDocToRule(egressRules)
        return rs
    
    def _getIpSetKeyWord(self):
        tmpname= 'ipsetqzvxtmp'
        try:
            Utils.runBash('ipset -N %s iptreemap'%tmpname)
        except:
            Utils.runBash('ipset -F %s'%tmpname)
        
        try:
            Utils.runBash('iptables -A INPUT -m set --set %s src -j ACCEPT'%tmpname)
            Utils.runBash('iptables -D INPUT -m set --set %s src -j ACCEPT'%tmpname)
            keyword = '--set'
        except:
            keyword = '--match-set'
            
        try:
            Utils.runBash('ipset -X %s'%tmpname)
        except:
            pass
        
        return keyword
    
    def _createIpSet(self, name, ips):
        try:
            Utils.runBash('ipset -N %s %s' % (name, Header.IPSET_TYPE))
        except:
            cherrypy.log('ipset[%s] is already here'%name)
        
        tmpname = '%s-%s' % (name, str(uuid.uuid4()))
        try:
            Utils.runBash('ipset -N %s %s'%(tmpname, Header.IPSET_TYPE))
        except:
            cherrypy.log('Failed to create tmp ipset for %s'%name)
            Utils.runBashSilent('ipset -F %s'%tmpname)
            return False
            
        ret = False
        try:
            for ip in ips:
                try:
                    Utils.runBash('ipset -A %s %s'%(tmpname, ip))
                except BashExceutedFailedException, e:
                    if 'already in set' in e.stderr:
                        cherrypy.log('ip[%s] is already in set %s, raise error' % (ip, tmpname))
                        raise
                
                Utils.runBash('ipset -W %s %s'%(tmpname, name))
                cherrypy.log('successfully swap ipset %s to %s'%(tmpname, name))
                ret = True
        finally:
            Utils.runBashSilent('ipset -F %s'%tmpname)
            Utils.runBashSilent('ipset -X %s'%tmpname)
        
        return False
        
    def _setEgressRules(self, ruleSet):
        chainName = self._egressChainName(ruleSet.vmName)
        
        Utils.runBashSilent('iptables -F %s'%chainName)
        Utils.runBash('iptables -N %s'%chainName)
        
        if len(ruleSet.egressRules) > 0:
            for erule in ruleSet.egressRules:
                range = '%s:%s' % (erule.startPort, erule.endPort)
                if erule.startPort == '-1':
                    setname = "%s_%s_any" % (chainName, erule.protocol)
                else:
                    setname = "%s_%s_%s_%s" % (chainName, erule.protocol, erule.startPort, erule.endPort)
                
                if not self._createIpSet(setname, erule.ips):
                    raise Exception('Create ipset[%s] failed' % setname)
                
                allowAny = False
                if '0.0.0.0/0' in erule.ips:
                    del erule.ips[erule.ips.index('0.0.0.0/0')]
                    allowAny = True
                    
                if protocol == "all":
                    cmd = "iptables -I %s -m state --state NEW -m set %s %s dst -j RETURN" % (chainName, self.ipsetKeyWord, setname)
                elif protocol == "icmp":
                    if erule.startPort == "-1":
                        range = "any"
                    else:
                        range = "%s/%s" % (erule.startPort, erule.endPort)
                    cmd = "iptables -I %s -p icmp --icmp-type %s -m set %s %s dst -j RETURN" % (chainName, range, self.ipsetKeyWord, setname)
                else:
                    cmd = "iptables -I %s -p %s -m %s --dport %s -m state --state NEW -m set %s %s dst -j RETURN" % (chainName, erule.protocol, erule.protocol, range, self.ipsetKeyWord, setname)
                
                Utils.runBash(cmd)
                
                if allowAny and erule.protocol != "all":
                    if erule.protocol == "icmp":
                        cmd = "iptables -I %s -p %s -m %s --deport %s -m state --state NEW -j RETURN"
                    else:
                        if erule.startPort == "-1":
                            range = "any"
                        else:
                            range = "%s/%s" % (erule.startPort, erule.endPort)
                        cmd = "iptables -I %s -p icmp --icmp-type %s -j RETURN" % (chainName, range)
                    Utils.runBash(cmd)
            Utils.runBash("iptables -A %s -j DROP" % chainName)
        else:
            Utils.runBash("iptables -A %s -j RETURN" % chainName)
        
        
    def _setIngressRules(self, ruleSet):
        chainName = self._chainName(ruleSet.vmName)
        Utils.runBashSilent('iptables -F %s'%chainName)
        Utils.runBash('iptables -N %s'%chainName)
        
        if len(ruleSet.ingressRules) > 0:
            for irule in ruleSet.egressRules:
                range = '%s:%s' % (irule.startPort, irule.endPort)
                if irule.startPort == '-1':
                    setname = "%s_%s_any" % (chainName, irule.protocol)
                else:
                    setname = "%s_%s_%s_%s" % (chainName, irule.protocol, irule.startPort, irule.endPort)
                
                if not self._createIpSet(setname, irule.ips):
                    raise Exception('Create ipset[%s] failed' % setname)
                
                allowAny = False
                if '0.0.0.0/0' in irule.ips:
                    del irule.ips[irule.ips.index('0.0.0.0/0')]
                    allowAny = True
                    
                if protocol == "all":
                    cmd = "iptables -I %s -m state --state NEW -m set %s %s src -j ACCEPT" % (chainName, self.ipsetKeyWord, setname)
                elif protocol == "icmp":
                    if irule.startPort == "-1":
                        range = "any"
                    else:
                        range = "%s/%s" % (irule.startPort, irule.endPort)
                    cmd = "iptables -I %s -p icmp --icmp-type %s -m set %s %s src -j ACCEPT" % (chainName, range, self.ipsetKeyWord, setname)
                else:
                    cmd = "iptables -I %s -p %s -m %s --dport %s -m state --state NEW -m set %s %s src -j ACCEPT" % (chainName, irule.protocol, irule.protocol, range, self.ipsetKeyWord, setname)
                
                Utils.runBash(cmd)
                
                if allowAny and irule.protocol != "all":
                    if irule.protocol == "icmp":
                        cmd = "iptables -I %s -p %s -m %s --deport %s -m state --state NEW -j ACCEPT"
                    else:
                        if irule.startPort == "-1":
                            range = "any"
                        else:
                            range = "%s/%s" % (irule.startPort, irule.endPort)
                        cmd = "iptables -I %s -p icmp --icmp-type %s -j ACCEPT" % (chainName, range)
                    Utils.runBash(cmd)
        Utils.runBash("iptables -A %s -j DROP" % chainName)
            
    def setRules(self, doc=None):
        ruleSet = self._parseRuleDoc(doc)
        oldLog = VmLog.s_fromXmlFile(ruleSet.vmName)
        newLog = VmLog.s_fromRuleSet(ruleSet)
        [reprogramDefault, reprogramChain, rewriteLog] = oldLog.compare(newLog)
        if not reprogramChain and rewriteLog:
            newLog.writeToFile()
            cherrypy.log('Rewrite log for vm[%s] as %s' % (newLog.vmName, Utils.printObj(newLog)))
            return
        
        if not reprogramChain:
            cherrypy.log('No need to program any rules for vm[%s]'%ruleSet.vmName)
            return
        
        self._setEgressRules(ruleSet)
        self._setIngressRules(ruleSet)
    
    
    def index(self, command=None, args=None):
        if not hasattr(self, command):
            raise ValueError("SecurityGroupAgent doesn't have a method called '%s'"%command)
        
        method = getattr(self, command)
        return method(args)
    index = True
    
class WebServer(object):
    securitygroup = SecurityGroupAgent()
    
    def index(self):
        return "CloudStack web agent server"
    index.exposed = True
    
if __name__ == '__main__':
    cherrypy.quickstart(WebServer())
