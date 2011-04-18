#!/usr/bin/python
import cloud_utils
from cloud_utils import Command
import logging
import sys
import os
import xml.dom.minidom
from optparse import OptionParser, OptionGroup, OptParseError, BadOptionError, OptionError, OptionConflictError, OptionValueError
iptables = Command("iptables")
bash = Command("/bin/bash")
virsh = Command("virsh")
ebtablessave = Command("ebtables-save")
ebtables = Command("ebtables")
augtool = Command("augtool")
def execute(cmd):
    logging.debug(cmd)
    return bash("-c", cmd).stdout
def can_bridge_firewall(privnic):
    try:
        execute("which iptables")
    except:
        print "no iptables on your host machine"
        exit(1)

    try:
        execute("which ebtables")
    except:
        print "no ebtables on your host machine"
        exit(2)

    
    if not os.path.exists('/var/run/cloud'):
        os.makedirs('/var/run/cloud')
 
    cleanup_rules_for_dead_vms()
    cleanup_rules()
    
    return True
'''
def ipset(ipsetname, proto, start, end, ips):
    try:
        check_call(['ipset', '-N', ipsetname, 'iptreemap'])
    except:
        logging.debug("ipset chain already exists" + ipsetname)

    result = True
    ipsettmp = ''.join(''.join(ipsetname.split('-')).split('_')) + str(int(time.time()) % 1000)

    try: 
        check_call(['ipset', '-N', ipsettmp, 'iptreemap']) 
        for ip in ips:
            try:
                check_call(['ipset', '-A', ipsettmp, ip])
            except CommandException, cex:
                if cex.reason.rfind('already in set') == -1:
                   raise
        check_call(['ipset', '-W', ipsettmp, ipsetname]) 
        check_call(['ipset', '-X', ipsettmp]) 
    except:
        logging.debug("Failed to program ipset " + ipsetname)
        result = False

    return result
'''

def destroy_network_rules_for_vm(vm_name):
    vmchain = vm_name
    vmchain_default = None
    
    delete_rules_for_vm_in_bridge_firewall_chain(vm_name)
    if vm_name.startswith('i-') or vm_name.startswith('r-'):
        vmchain_default =  '-'.join(vm_name.split('-')[:-1]) + "-def"

    destroy_ebtables_rules(vmchain)
    
    try:
        if vmchain_default != None: 
            execute("iptables -F " + vmchain_default)
    except:
        logging.debug("Ignoring failure to delete  chain " + vmchain_default)
    
    try:
        if vmchain_default != None: 
            execute("iptables -X " + vmchain_default)
    except:
        logging.debug("Ignoring failure to delete  chain " + vmchain_default)

    try:
        execute("iptables -F " + vmchain)
    except:
        logging.debug("Ignoring failure to delete  chain " + vmchain)
    
    try:
        execute("iptables -X " + vmchain)
    except:
        logging.debug("Ignoring failure to delete  chain " + vmchain)
    
    remove_rule_log_for_vm(vm_name)
    
    if 1 in [ vm_name.startswith(c) for c in ['r-', 's-', 'v-'] ]:
        return 'true'
    
    return 'true'

def destroy_ebtables_rules(vm_name):
    delcmd = "ebtables-save | grep ROUTING | grep " +  vm_name + " | sed 's/-A/-D/'"
    delcmds = execute(delcmd).split('\n')
    delcmds.pop()
    for cmd in delcmds:
        try:
            execute("ebtables -t nat " +  cmd)
        except:
            logging.debug("Ignoring failure to delete ebtables rules for vm " + vm_name)
    chains = [vm_name+"-in", vm_name+"-out"]
    for chain in chains:
        try:
            execute("ebtables -t nat -F " +  chain)
            execute("ebtables -t nat -X " +  chain)
        except:
            logging.debug("Ignoring failure to delete ebtables chain for vm " + vm_name)   

def default_ebtables_rules(vm_name, vm_ip, vm_mac, vif):
    vmchain_in = vm_name + "-in"
    vmchain_out = vm_name + "-out"
    
    for chain in [vmchain_in, vmchain_out]:
        try:
            execute("ebtables -t nat -N " + chain)
        except:
            execute("ebtables -t nat -F " + chain) 

    try:
        # -s ! 52:54:0:56:44:32 -j DROP 
        execute("ebtables -t nat -A PREROUTING -i " + vif + " -j " +  vmchain_in)
        execute("ebtables -t nat -A POSTROUTING -o " + vif + " -j " + vmchain_out)
    except:
        logging.debug("Failed to program default rules")
        return 'false'
    
    try:
        execute("ebtables -t nat -A " +  vmchain_in + " -s ! " +  vm_mac + " -j DROP")
        execute("ebtables -t nat -A " +  vmchain_in  + " -p ARP -s ! " + vm_mac + " -j DROP")
        execute("ebtables -t nat -A " +  vmchain_in  + " -p ARP --arp-mac-src ! " + vm_mac + " -j DROP")
        if vm_ip is not None:
            execute("ebtables -t nat -A " + vmchain_in  +  " -p ARP --arp-ip-src ! " + vm_ip + " -j DROP") 
        execute("ebtables -t nat -A " + vmchain_in  + " -p ARP --arp-op Request -j ACCEPT")   
        execute("ebtables -t nat -A " + vmchain_in  + " -p ARP --arp-op Reply -j ACCEPT")    
        execute("ebtables -t nat -A " + vmchain_in  + " -p ARP  -j DROP")    
    except:
        logging.exception("Failed to program default ebtables IN rules")
        return 'false'
   
    try:
        execute("ebtables -t nat -A " + vmchain_out + " -p ARP --arp-op Reply --arp-mac-dst ! " +  vm_mac + " -j DROP")
        if vm_ip is not None:
            execute("ebtables -t nat -A " + vmchain_out + " -p ARP --arp-ip-dst ! " + vm_ip + " -j DROP") 
        execute("ebtables -t nat -A " + vmchain_out + " -p ARP --arp-op Request -j ACCEPT")   
        execute("ebtables -t nat -A " + vmchain_out + " -p ARP --arp-op Reply -j ACCEPT")    
        execute("ebtables -t nat -A " + vmchain_out + " -p ARP -j DROP")    
    except:
        logging.debug("Failed to program default ebtables OUT rules")
        return 'false' 
    
            
def default_network_rules_systemvm(vm_name, brname):
    if not addFWFramework(brname):
        return False 

    vifs = getVifs(vm_name)
    domid = getvmId(vm_name)
    vmchain = vm_name
    brfw = "BF-" + brname
 
    delete_rules_for_vm_in_bridge_firewall_chain(vm_name)
  
    try:
        execute("iptables -N " + vmchain)
    except:
        execute("iptables -F " + vmchain)

    for vif in vifs:
        try:
            execute("iptables -A " + brfw + "-OUT" +  " -m physdev --physdev-is-bridged --physdev-out " + vif +  " -j " + vmchain)
            execute("iptables -A " + brfw + "-IN" + " -m physdev --physdev-is-bridged --physdev-in " + vif + " -j " +  vmchain)
        except:
            logging.debug("Failed to program default rules")
            return 'false'

    execute("iptables -A " + vmchain + " -j ACCEPT")
    
    if write_rule_log_for_vm(vm_name, '-1', '_ignore_', domid, '_initial_', '-1') == False:
        logging.debug("Failed to log default network rules for systemvm, ignoring")
    return 'true'


def default_network_rules(vm_name, vm_id, vm_ip, vm_mac, vif, brname):
    if not addFWFramework(brname):
        return False 

    vmName = vm_name 
    brfw = "BF-" + brname
    domID = getvmId(vm_name)
    delete_rules_for_vm_in_bridge_firewall_chain(vmName)
    vmchain = vm_name
    vmchain_default = '-'.join(vmchain.split('-')[:-1]) + "-def"
    
    destroy_ebtables_rules(vmName)

    try:
        execute("iptables -N " + vmchain)
    except:
        execute("iptables -F " + vmchain)
        
    try:
        execute("iptables -N " + vmchain_default)
    except:
        execute("iptables -F " + vmchain_default)

    try:
        execute("iptables -A " + brfw + "-OUT" + " -m physdev --physdev-is-bridged --physdev-out " + vif + " -j " +  vmchain_default)
        execute("iptables -A " + brfw + "-IN" + " -m physdev --physdev-is-bridged --physdev-in " +  vif + " -j " + vmchain_default)
        execute("iptables -A  " + vmchain_default + " -m state --state RELATED,ESTABLISHED -j ACCEPT")
        #allow dhcp
        execute("iptables -A " + vmchain_default + " -m physdev --physdev-is-bridged --physdev-in " + vif + " -p udp --dport 67 --sport 68 -j ACCEPT")
        execute("iptables -A " + vmchain_default + " -m physdev --physdev-is-bridged --physdev-out " + vif + " -p udp --dport 68 --sport 67  -j ACCEPT")

        #don't let vm spoof its ip address
        if vm_ip is not None:
            execute("iptables -A " + vmchain_default + " -m physdev --physdev-is-bridged --physdev-in " + vif  + " --source " +  vm_ip +  " -j ACCEPT")
        execute("iptables -A " + vmchain_default + " -j " +  vmchain)
        execute("iptables -A " + vmchain + " -j DROP")
    except:
        logging.debug("Failed to program default rules for vm " + vm_name)
        return 'false'
    
    default_ebtables_rules(vmchain, vm_ip, vm_mac, vif)
    
    if vm_ip is not None:
        if write_rule_log_for_vm(vmName, vm_id, vm_ip, domID, '_initial_', '-1') == False:
            logging.debug("Failed to log default network rules, ignoring")
        
    logging.debug("Programmed default rules for vm " + vm_name)
    return 'true'
    
def post_default_network_rules(vm_name, vm_id, vm_ip, vm_mac, vif, brname):
    vmchain_default = '-'.join(vm_name.split('-')[:-1]) + "-def"
    vmchain_in = vm_name + "-in"
    vmchain_out = vm_name + "-out"
    domID = getvmId(vm_name)
    execute("iptables -I " + vmchain_default + " 4 -m physdev --physdev-is-bridged --physdev-in " + vif  + " --source " +  vm_ip +  " -j ACCEPT")
    execute("ebtables -t nat -I " + vmchain_in  +  " 4 -p ARP --arp-ip-src ! " + vm_ip + " -j DROP") 
    execute("ebtables -t nat -I " + vmchain_out + " 2 -p ARP --arp-ip-dst ! " + vm_ip + " -j DROP") 
    if write_rule_log_for_vm(vm_name, vm_id, vm_ip, domID, '_initial_', '-1') == False:
            logging.debug("Failed to log default network rules, ignoring")
def delete_rules_for_vm_in_bridge_firewall_chain(vmName):
    vm_name = vmName
    if vm_name.startswith('i-') or vm_name.startswith('r-'):
        vm_name =  '-'.join(vm_name.split('-')[:-1])
    
    vmchain = vm_name
    
    delcmd = "iptables -S | grep " +  vmchain + " | grep physdev-is-bridged | sed 's/-A/-D/'"
    delcmds = execute(delcmd).split('\n')
    delcmds.pop()
    for cmd in delcmds:
        try:
            execute("iptables " + cmd)
        except:
              logging.exception("Ignoring failure to delete rules for vm " + vmName)

def rewrite_rule_log_for_vm(vm_name, new_domid):
    logfilename = "/var/run/cloud/" + vm_name +".log"
    if not os.path.exists(logfilename):
        return 
    lines = (line.rstrip() for line in open(logfilename))
    
    [_vmName,_vmID,_vmIP,_domID,_signature,_seqno] = ['_', '-1', '_', '-1', '_', '-1']
    for line in lines:
        [_vmName,_vmID,_vmIP,_domID,_signature,_seqno] = line.split(',')
        break
    
    write_rule_log_for_vm(_vmName, _vmID, '0.0.0.0', new_domid, _signature, '-1')

def get_rule_log_for_vm(vmName):
    vm_name = vmName;
    logfilename = "/var/run/cloud/" + vm_name +".log"
    if not os.path.exists(logfilename):
        return ''
    
    lines = (line.rstrip() for line in open(logfilename))
    
    [_vmName,_vmID,_vmIP,_domID,_signature,_seqno] = ['_', '-1', '_', '-1', '_', '-1']
    for line in lines:
        [_vmName,_vmID,_vmIP,_domID,_signature,_seqno] = line.split(',')
        break
    
    return ','.join([_vmName, _vmID, _vmIP, _domID, _signature, _seqno])

def get_rule_logs_for_vms():
    cmd = "virsh list|grep running |awk '{print $2}'"
    vms = bash("-c", cmd).stdout.split("\n")
    
    result = []
    try:
        for name in vms:
            name = name.rstrip()
            if 1 not in [ name.startswith(c) for c in ['r-', 's-', 'v-', 'i-'] ]:
                continue
            #network_rules_for_rebooted_vm(session, name)
            if name.startswith('i-'):
                log = get_rule_log_for_vm(name)
                result.append(log)
    except:
        logging.debug("Failed to get rule logs, better luck next time!")
        
    print ";".join(result)

def cleanup_rules_for_dead_vms():
    return True 


def cleanup_rules():
  try:

    chainscmd = "iptables-save | grep '^:' | grep -v '.*-def' | awk '{print $1}' | cut -d':' -f2"
    chains = execute(chainscmd).split('\n')
    cleaned = 0
    cleanup = []
    for chain in chains:
        if 1 in [ chain.startswith(c) for c in ['r-', 'i-', 's-', 'v-'] ]:
            vm_name = chain
                
            cmd = "virsh list |grep " + vm_name 
            try:
                result = execute(cmd)
            except:
                result = None

            if result == None or len(result) == 0:
                logging.debug("chain " + chain + " does not correspond to a vm, cleaning up")
                cleanup.append(vm_name)
                continue
            if result.find("running") == -1:
                logging.debug("vm " + vm_name + " is not running, cleaning up")
                cleanup.append(vm_name)
                
    for vmname in cleanup:
        destroy_network_rules_for_vm(vmname)
                    
    logging.debug("Cleaned up rules for " + str(len(cleanup)) + " chains")                
  except:
    logging.debug("Failed to cleanup rules !")

def check_rule_log_for_vm(vmName, vmId, vmIP, domID, signature, seqno):
    vm_name = vmName;
    logfilename = "/var/run/cloud/" + vm_name +".log"
    if not os.path.exists(logfilename):
        return [True, True, True, True, True, True]
        
    try:
        lines = (line.rstrip() for line in open(logfilename))
    except:
        logging.debug("failed to open " + logfilename) 
        return [True, True, True, True, True, True]

    [_vmName,_vmID,_vmIP,_domID,_signature,_seqno] = ['_', '-1', '_', '-1', '_', '-1']
    try:
        for line in lines:
            [_vmName,_vmID,_vmIP,_domID,_signature,_seqno] = line.split(',')
            break
    except:
        logging.debug("Failed to parse log file for vm " + vm_name)
        remove_rule_log_for_vm(vm_name)
        return [True, True, True, True, True, True]
    
    return [(vm_name != _vmName), (vmId != _vmID), (vmIP != _vmIP), (domID != _domID), (signature != _signature),(seqno != _seqno)]

def write_rule_log_for_vm(vmName, vmID, vmIP, domID, signature, seqno):
    vm_name = vmName
    logfilename = "/var/run/cloud/" + vm_name +".log"
    logging.debug("Writing log to " + logfilename)
    logf = open(logfilename, 'w')
    output = ','.join([vmName, vmID, vmIP, domID, signature, seqno])
    result = True
    try:
        logf.write(output)
        logf.write('\n')
    except:
        logging.debug("Failed to write to rule log file " + logfilename)
        result = False
        
    logf.close()
    
    return result

def remove_rule_log_for_vm(vmName):
    vm_name = vmName
    logfilename = "/var/run/cloud/" + vm_name +".log"

    result = True
    try:
        os.remove(logfilename)
    except:
        logging.debug("Failed to delete rule log file " + logfilename)
        result = False
    
    return result

def add_network_rules(vm_name, vm_id, vm_ip, signature, seqno, vmMac, rules, vif, brname):
  try:
    vmName = vm_name
    domId = getvmId(vmName)
    vmchain = vm_name
    
    changes = []
    changes = check_rule_log_for_vm(vmName, vm_id, vm_ip, domId, signature, seqno)
    
    if not 1 in changes:
        logging.debug("Rules already programmed for vm " + vm_name)
        return 'true'
    
    if changes[0] or changes[1] or changes[2] or changes[3]:
        default_network_rules(vmName, vm_id, vm_ip, vmMac, vif, brname)

    if rules == "" or rules == None:
        lines = []
    else:
        lines = rules.split(';')[:-1]

    logging.debug("    programming network rules for  IP: " + vm_ip + " vmname=" + vm_name)
    execute("iptables -F " + vmchain)
    
    for line in lines:
	
        tokens = line.split(':')
        if len(tokens) != 4:
          continue
        protocol = tokens[0]
        start = tokens[1]
        end = tokens[2]
        cidrs = tokens.pop();
        ips = cidrs.split(",")
        ips.pop()
        allow_any = False
        if  '0.0.0.0/0' in ips:
            i = ips.index('0.0.0.0/0')
            del ips[i]
            allow_any = True
        range = start + ":" + end
        if ips:    
            if protocol == 'all':
                for ip in ips:
                    execute("iptables -I " + vmchain + " -m state --state NEW -s " + ip + " -j ACCEPT")
            elif protocol != 'icmp':
                for ip in ips:
                    execute("iptables -I " + vmchain + " -p " + protocol + " -m " + protocol + " --dport " + range + " -m state --state NEW -s " + ip + " -j ACCEPT")
            else:
                range = start + "/" + end
                if start == "-1":
                    range = "any"
                    for ip in ips:
                        execute("iptables -I " + vmchain + " -p icmp --icmp-type " + range + " -s " + ip + " -j ACCEPT")
        
        if allow_any and protocol != 'all':
            if protocol != 'icmp':
                execute("iptables -I " + vmchain + " -p " + protocol + " -m " +  protocol + " --dport " + range + " -m state --state NEW -j ACCEPT")
            else:
                range = start + "/" + end
                if start == "-1":
                    range = "any"
                    execute("iptables -I " + vmchain + " -p icmp --icmp-type " + range + " -j ACCEPT")

    iptables =  "iptables -A " + vmchain + " -j DROP"       
    execute(iptables)
    if write_rule_log_for_vm(vmName, vm_id, vm_ip, domId, signature, seqno) == False:
        return 'false'
    
    return 'true'
  except:
    logging.debug("Failed to network rule !: " + sys.exc_type)

def getVifs(vmName):
    vifs = []
    try:
        xmlfile = virsh("dumpxml", vmName).stdout 
    except:
        return vifs    

    dom = xml.dom.minidom.parseString(xmlfile)
    vifs = []
    for network in dom.getElementsByTagName("interface"):
        target = network.getElementsByTagName('target')[0]
        nicdev = target.getAttribute("dev").strip()
        vifs.append(nicdev) 
    return vifs
def getvmId(vmName):
    cmd = "virsh list |grep " + vmName + " | awk '{print $1}'"
    return bash("-c", cmd).stdout.strip()
    
def addFWFramework(brname):
    try:
        alreadySetup = augtool.match("/files/etc/sysctl.conf/net.bridge.bridge-nf-call-arptables", "1").stdout.strip()
        if len(alreadySetup) == 0:
            script = """
                        set /files/etc/sysctl.conf/net.bridge.bridge-nf-call-arptables 1
                        save"""
            augtool < script

        alreadySetup = augtool.match("/files/etc/sysctl.conf/net.bridge.bridge-nf-call-iptables", "1").stdout.strip()
        if len(alreadySetup) == 0:
            script = """
                        set /files/etc/sysctl.conf/net.bridge.bridge-nf-call-iptables 1
                        save"""
            augtool < script

        alreadySetup = augtool.match("/files/etc/sysctl.conf/net.bridge.bridge-nf-call-ip6tables", "1").stdout.strip()
        if len(alreadySetup) == 0:
            script = """
                        set /files/etc/sysctl.conf/net.bridge.bridge-nf-call-ip6tables 1
                        save"""
            augtool < script
        execute("sysctl -p /etc/sysctl.conf")
    except:
        logging.debug("failed to turn on bridge netfilter")
        return False

    brfw = "BF-" + brname
    try:
        execute("iptables -L " + brfw)
    except:
        execute("iptables -N " + brfw)

    brfwout = brfw + "-OUT"
    try:
        execute("iptables -L " + brfwout)
    except:
        execute("iptables -N " + brfwout)

    brfwin = brfw + "-IN"
    try:
        execute("iptables -L " + brfwin)
    except:
        execute("iptables -N " + brfwin)

    try:
        refs = execute("iptables -n -L  " + brfw + " |grep " + brfw + " | cut -d \( -f2 | awk '{print $1}'").strip()
        if refs == "0":
            execute("iptables -A FORWARD -i " + brname + " -m physdev --physdev-is-bridged -j " + brfw)
            execute("iptables -A FORWARD -o " + brname + " -m physdev --physdev-is-bridged -j " + brfw)
            phydev = execute("brctl show |grep " + brname + " | awk '{print $4}'").strip()
            execute("iptables -A " + brfw + " -m physdev --physdev-is-bridged --physdev-out " + phydev + " -j ACCEPT")
            execute("iptables -A " + brfw + " -m state --state RELATED,ESTABLISHED -j ACCEPT")
            execute("iptables -A " + brfw + " -m physdev --physdev-is-bridged --physdev-is-out -j " + brfwout)
            execute("iptables -A " + brfw + " -m physdev --physdev-is-bridged --physdev-is-in -j " + brfwin)
            execute("iptables -A FORWARD -i " + brname + " -j DROP")
            execute("iptables -A FORWARD -o " + brname + " -j DROP")
    
        return True
    except:
        try:
            execute("iptables -F " + brfw)
        except:
            return False
        return False
            
if __name__ == '__main__':
    logging.basicConfig(filename="/var/log/cloud/security_group.log", format="%(asctime)s - %(message)s", level=logging.DEBUG)
    parser = OptionParser()
    parser.add_option("--vmname", dest="vmName")
    parser.add_option("--vmip", dest="vmIP")
    parser.add_option("--vmid", dest="vmID")
    parser.add_option("--vmmac", dest="vmMAC")
    parser.add_option("--vif", dest="vif")
    parser.add_option("--sig", dest="sig")
    parser.add_option("--seq", dest="seq")
    parser.add_option("--rules", dest="rules")
    parser.add_option("--brname", dest="brname")
    (option, args) = parser.parse_args()
    cmd = args[0]
    if cmd == "can_bridge_firewall":
        can_bridge_firewall(args[1])
    elif cmd == "default_network_rules":
        default_network_rules(option.vmName, option.vmID, option.vmIP, option.vmMAC, option.vif, option.brname)
    elif cmd == "destroy_network_rules_for_vm":
        destroy_network_rules_for_vm(option.vmName) 
    elif cmd == "default_network_rules_systemvm":
        default_network_rules_systemvm(option.vmName, option.brname)
    elif cmd == "get_rule_logs_for_vms":
        get_rule_logs_for_vms()
    elif cmd == "add_network_rules":
        add_network_rules(option.vmName, option.vmID, option.vmIP, option.sig, option.seq, option.vmMAC, option.rules, option.vif, option.brname)
    elif cmd == "cleanup_rules":
        cleanup_rules()
    elif cmd == "post_default_network_rules":
        post_default_network_rules(option.vmName, option.vmID, option.vmIP, option.vmMAC, option.vif, option.brname)