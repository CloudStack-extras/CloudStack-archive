
  #
  # Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
  #
 

from xml.dom.minidom import *
import urllib2
import MySQLdb
from XenAPI  import *
import time
import datetime
import os
import sys
import paramiko
import traceback

### Logging functions

def getTimestamp():
    return datetime.datetime.now().strftime("%m/%d/%Y | %I:%M:%S %p")

def nonExitingLogDecorator(entryMessage):
    return genDecoratorFn(entryMessage, False, False)

def basicLogDecorator(entryMessage):
    return genDecoratorFn(entryMessage, True, True)

def verboseLogDecorator(entryMessage):
    return genDecoratorFn(entryMessage, False, True)

def genDecoratorFn(entryMessage, printToScreen, exitOnError):
    def wrap(f):
        def g(*args):
            writeToLog("", printToScreen)
            writeToLog(getTimestamp() + " | " + entryMessage, printToScreen)
            
            if (len(args) > 0):
                argString = ""
                for i in range(len(args)):
                    arg = args[i]
                    argString += str(arg)
                    if (i != len(args) - 1):
                        argString += ", "
                writeToLog("args: " + argString, False)
            
            returnValue = None
            try:
                returnValue = f(*args)
            except SystemExit:
                sys.exit(1)
            except Exception, e:                
                if (exitOnError):
                    handleError(str(e), True)
                    traceback.print_exc(file = GLOBALS["LOG_FILE"])
                    sys.exit(1)
                else:
                    return False

            if (returnValue in (None, False)):                
                if (exitOnError):
                    writeToLog(str(f) + " returned " + str(returnValue), False)
                    handleError(None, True)
                    sys.exit(1)
                else:
                    return False
            else:
                return returnValue
        return g
    return wrap

def handleError(msg, printToScreen):
    writeToLog(getTimestamp() + " | " + "Failed to complete this step.", printToScreen)
    if (msg != None):
        writeToLog("Details: " + msg, printToScreen)
    
def writeToLog(message, printToScreen):
    logFile = GLOBALS.get("LOG_FILE")
    if (logFile != None):
        logFile.write(message)
        logFile.write("\n")
    if (printToScreen):
        print message

### Util classes

class System:
    def __init__(self, managementServerIp, asyncApi, xenServerIp, xenServerPassword, xenServerPasswordMap, dbName, dbLogin, dbPassword, zoneId, templateId, isoId, defaultServiceOfferingId, defaultDiskOfferingId):
        self.zoneId = zoneId
        self.templateId = templateId
        self.isoId = isoId
        self.defaultServiceOfferingId = defaultServiceOfferingId
        self.defaultDiskOfferingId = defaultDiskOfferingId
        self.api = System.API(managementServerIp, asyncApi)
        if (dbPassword == None):
            dbPassword = ""
        self.db = System.DB(managementServerIp, dbName, dbLogin, dbPassword)

        self.xenServerIp = None
        self.xenapi = None
        if (xenServerIp != None or xenServerPasswordMap != None):
            self.findXenApi(xenServerIp, xenServerPassword, xenServerPasswordMap)
            self.controlDomainRef = self.findControlDomainRef()
            self.sshConn = paramiko.SSHClient()
            self.sshConn.set_missing_host_key_policy(paramiko.AutoAddPolicy())
            self.sshConn.connect(self.xenServerIp, username = "root", password = self.xenServerPassword)

    @verboseLogDecorator("Finding the XenAPI connection...")
    def findXenApi(self, xenServerIp, xenServerPassword, xenServerPasswordMap):
        if (xenServerPasswordMap != None):
            xenServerIp = xenServerPasswordMap.keys()[0]
            xenServerPassword = xenServerPasswordMap[xenServerIp]

        masterXenServerIp = xenServerIp
        masterXenServerPassword = xenServerPassword
        session = None
        try:
            session = Session("http://" + masterXenServerIp + "/var/run/xend/xen-api.sock")
            session.login_with_password("root", masterXenServerPassword)
        except Exception, e:
            if (e.details != None and len(e.details) == 2 and e.details[0] == "HOST_IS_SLAVE"):
                masterXenServerIp = e.details[1]
                if (xenServerPasswordMap != None):
                    masterXenServerPassword = xenServerPasswordMap[masterXenServerIp]
                else:
                    masterXenServerPassword = xenServerPassword
                session = Session("http://" + masterXenServerIp + "/var/run/xend/xen-api.sock")
                session.login_with_password("root", masterXenServerPassword)
            else:
                raise

        self.xenServerIp = masterXenServerIp
        self.xenServerPassword = masterXenServerPassword
        self.xenapi = session.xenapi
        return True
        
    @verboseLogDecorator("Finding the control domain for the dest system...")
    def findControlDomainRef(self):
        # Find the host ref for this system
        hostRefs = self.xenapi.host.get_all()
        systemHostRef = None
        for hostRef in hostRefs:
            address = self.xenapi.host.get_address(hostRef)
            if (address == self.xenServerIp):
                systemHostRef = hostRef
                break
            
        if (systemHostRef == None):
            raise Exception("Failed to find the XenServer host ref for " + str(self))

        # Find the control domain ref that corresponds to this host                
        vmRefs = self.xenapi.VM.get_all()
        for vmRef in vmRefs:
            if (self.xenapi.VM.get_is_control_domain(vmRef)):
                controlDomainHostRef = self.xenapi.VM.get_resident_on(vmRef)
                if (controlDomainHostRef == systemHostRef):
                    return vmRef
        return None

    @nonExitingLogDecorator("Running ssh command...")
    def runSshCommand(self, command):
        stdin, stdout, stderr = self.sshConn.exec_command(command)
        return (stdin.channel.recv_exit_status() == 0)

    def updateIsoPermissions(self):
        setParams = {"public":"1"}
        whereParams = {"id":self.isoId}
        return self.db.updateDbValues("vm_template", setParams, whereParams)

    def __str__(self):
        description = "Management Server: %s" % (self.api.ip)
        if (self.xenServerIp != None):
            description += " | XenServer: %s" %(self.xenServerIp)
        return description
    
    class API:
        # Vars: ip
        def __init__(self, ip, asyncApi):
            self.ip = ip
            self.asyncApi = asyncApi
                    
        # Runs a synchronous API command and returns the ID of the created object, or success/failure
        def runSyncApiCommand(self, command, params, objectName):
            requestURL = self.buildRequestUrl(command, params)
            xmlText = urllib2.urlopen(requestURL).read()
            if (objectName == None):
                responseName = (command + "response").lower()
                return (System.API.getTagValue(xmlText, responseName, "success") == "true")
            else:
                return System.API.getTagValue(xmlText, objectName, "id")
                
        # Runs a asynchronous API command and returns the ID of the created object, or success/failure
        def runAsyncApiCommand(self, command, params, objectName):
            requestURL = self.buildRequestUrl(command, params)
            xmlText = urllib2.urlopen(requestURL).read()
            responseName = (command + "response").lower()
            jobId = System.API.getTagValue(xmlText, responseName, "jobid")
            objectId = System.API.getTagValue(xmlText, responseName, objectName + "id")
            params = dict()
            params["jobId"] = jobId
            requestURL = self.buildRequestUrl("queryAsyncJobResult", params)                
            retries = int(GLOBALS["ASYNC_RETRIES"])
            jobResult = None
            while (retries > 0):
                time.sleep(float(GLOBALS["ASYNC_SLEEP_TIME"]))
                xmlText = urllib2.urlopen(requestURL).read()
                if (System.API.getTagValue(xmlText, "queryasyncjobresultresponse", "jobstatus") == "1"):
                    if (objectId != None):
                        return objectId
                    else:
                        return True
                jobResult = System.API.getTagValue(xmlText, "queryasyncjobresultresponse", "jobresult")
                retries -= 1
            raise Exception(jobResult)            

        def buildRequestUrl(self, command, params):
            requestURL = "http://" + self.ip + ":8096/client/api/?command=" + command
            for paramKey in params.keys():
                paramVal = params.get(paramKey)
                requestURL += "&" + str(paramKey) + "=" + str(paramVal)
            return requestURL
    
        @staticmethod
        def getTagValue(xmlText, objectName, tagName):
            xmlDoc = parseString(xmlText)
            response = xmlDoc.getElementsByTagName(objectName)[0]
            for x in response.childNodes:
                if (x.tagName == tagName):
                    return " ".join(z.wholeText for z in x.childNodes)
            return None        

        @staticmethod
        def printApiValues(listOfAPIObjects):
            for apiObject in listOfAPIObjects:
                for key in apiObject.keys():
                    print key + ":" + apiObject.get(key)
                print " "

    class DB:
        # Vars: conn
        def __init__(self, ip, dbName, dbLogin, dbPassword):
            self.conn = MySQLdb.connect(host = ip, user = dbLogin, passwd = dbPassword, db = dbName)
            self.conn.autocommit(True)

        def getTable(self, table):
            cursor = self.conn.cursor()
            sql  = "SELECT * from " + table
            cursor.execute(sql)
            for row in cursor.fetchall():
                print row
            cursor.close()
        
        # Returns a list of hashtables, each one representing a row and using column names as keys. 
        def getDbValues(self, table, columns, whereParams):
            values = []
            cursor = self.conn.cursor()
            columnsText = ",".join(columns)
            sql = "SELECT " + columnsText + " FROM " + table
            if (len(whereParams) > 0):
                sql += System.DB.buildSqlWhereClause(whereParams)
            cursor.execute(sql)
            rows = cursor.fetchall()
            cursor.close()
            for row in rows:
                value = dict()
                for i in range(len(columns)):
                    val = str(row[i])
                    value[columns[i]] = val
                values.append(value)
            return values

        def updateDbValues(self, table, setParams, whereParams):
            setClause = System.DB.buildSqlSetClause(setParams)
            sql = "UPDATE " + table + setClause
            if (len(whereParams) > 0):
                sql += System.DB.buildSqlWhereClause(whereParams)
        
            cursor = self.conn.cursor()
            cursor.execute(sql)
            self.conn.commit()
            cursor.close()
            return True

        def insertIntoDb(self, table, setParams):
            existingRecords = self.getDbValues(table, ["id"], setParams)
            if (len(existingRecords) > 0):
                return existingRecords[0]["id"]
            columns = setParams.keys()
            values = []
            for column in columns:
                values.append(setParams[column])
            sql = "INSERT INTO " + table + System.DB.buildSqlInsertClause(columns, values)
            cursor = self.conn.cursor()
            cursor.execute(sql)
            insertId = self.conn.insert_id()
            self.conn.commit()
            cursor.close()
            return insertId

        @staticmethod
        def buildSqlInsertClause(columns, values):
            columnsSql = " ("
            valuesSql = " VALUES ("
            for i in range(len(columns)):
                if (str(values[i]) == "null"):
                    continue

                columnsSql += columns[i]
                valuesSql += "'" + str(values[i]) + "'"
                if (i != (len(columns) - 1)):
                    columnsSql += ", "
                    valuesSql += ", "
                else:
                    columnsSql += ")"
                    valuesSql += ")"
            return columnsSql + valuesSql

        @staticmethod
        def buildSqlWhereClause(params):
            sql = " WHERE "
            keys = params.keys()
            for i in range(len(keys)):
                key = str(keys[i])
                val = str(params[key])
                if ("like" in val):
                    val = val.split(":")[1]
                    sql += key + " like '" + val + "'"
                elif ("neq" in val):
                    val  = val.split(":")[1]
                    sql += key + " != '" + val + "'"
                elif (val == "null" or val == "not null"):
                    sql += key + " IS " + val
                else:
                    sql += key + " = '" + val + "'"
                if (i != (len(keys) - 1)):
                    sql += " AND "
            return sql

        @staticmethod
        def buildSqlSetClause(params):
            sql = " SET "
            keys = params.keys()
            for i in range(len(keys)):
                key = keys[i]
                val = params[key]
                sql += key + " = "
                if (val == "null"):
                    sql += "null"
                else:
                    sql += "'" + val + "'"
                if (i != (len(keys) - 1)):
                    sql += ", "        
            return sql    

### Data classes

class User:
    # Vars: system, id, username, password, accountId, firstname, lastname, email, accountType, accountName, domainId

    def __init__(self, system, userId, username, password, accountId, firstname, lastname, email, accountType, accountName, domainId):
        self.system = system
        self.id = userId
        self.username = username
        self.password = password
        self.accountId = accountId
        self.firstname = firstname
        self.lastname = lastname
        self.email = email
        self.accountType = accountType
        self.accountName = accountName
        self.domainId = domainId

    def __str__(self):
        return "(User: %s | %s)" % (self.username, self.system)

    def alreadyMigrated(self):
        f = open(GLOBALS["MIGRATED_ACCOUNTS_FILE"], "a+")
        migratedUsersCsv = f.read()
        f.close()
        migratedUsersEntries = migratedUsersCsv.split(",")
        for migratedUsersEntry in migratedUsersEntries:
            if (migratedUsersEntry.strip() == self.accountId):
                return True
        return False

    def tagAsMigrated(self):
        if (not self.alreadyMigrated()):
            f = open(GLOBALS["MIGRATED_ACCOUNTS_FILE"], "a")
            f.write(self.accountId + ",")
            f.close()
        return True        

    @staticmethod
    def getByName(system, username):
        columns = ["id"]
        users = system.db.getDbValues("user", columns, {"username":username, "removed":"null"})
        if (len(users) > 0):
            return User.get(system, users[0]["id"])
        else:
            return None

    @staticmethod
    def get(system, userId):
        columns = ["id", "username", "password", "account_id", "firstname", "lastname", "email"]
        users = system.db.getDbValues("user", columns, {"id":userId})
        if (len(users) == 0):
            return None
        user = users[0]
        columns = ["type", "account_name", "domain_id"]
        account = system.db.getDbValues("account", columns, {"id":user["account_id"]})[0]
        return User(system, userId, user["username"], user["password"], user["account_id"], user["firstname"], user["lastname"], user["email"], account["type"], account["account_name"], account["domain_id"])

    @staticmethod
    def getDomain(system, domainId):
        columns = ["id", "parent", "name", "owner"]
        return system.db.getDbValues("domain", columns, {"id":domainId})[0]

    @staticmethod
    def getDomainByName(system, domainName):
        columns = ["id"]
        domains = system.db.getDbValues("domain", columns, {"name":domainName})
        if (len(domains) > 0):
            return domains[0]
        else:
            return None

    @staticmethod
    def createDomain(srcSystem, destSystem, srcDomainId):
        # Get the source domain
        srcDomain = User.getDomain(srcSystem, srcDomainId)

        # If a domain with the same name exists in the dest system, return its ID
        destDomain = User.getDomainByName(destSystem, srcDomain["name"])
        if (destDomain != None):
            return destDomain["id"]
        else:
            # Otherwise, create a new domain in the dest system with the same name, and return its ID
            # If the src domain has parent domains, we need to create these first
            parentId = None
            if (srcDomain["parent"] != "null"):
                parentId = User.createDomain(srcSystem, destSystem, srcDomain["parent"])
            params = dict()
            params["name"] = srcDomain["name"]
            if (parentId != None):
                params["parent"] = parentId
            newDomainId = destSystem.api.runSyncApiCommand("createDomain", params, "domain")
            if (newDomainId == None):
                raise Exception("Failed to create domain " + srcDomain["name"])
            else:
                # Set the owner for the new domain
                srcOwner = User.get(srcSystem, srcDomain["owner"])
                destOwner = User.create(destSystem, srcOwner)
                if (not destSystem.db.updateDbValues("domain", {"owner":destOwner.id}, {"id":newDomainId})):
                    raise Exception("Failed to update the owner for domain " + srcDomain["name"])
                return newDomainId
                
    @staticmethod
    @basicLogDecorator("Creating new user...")
    def create(system, srcUser):
        user = User.getByName(system, srcUser.username)
        if (user != None):
            return user
        else:
            # If the user's domain doesn't exist in the system, create it
            domainId = User.createDomain(srcUser.system, system, srcUser.domainId)                                                    

            params = dict()
            params["username"] = srcUser.username
            params["password"] = "temp"
            params["firstname"] = srcUser.firstname
            params["lastname"] = srcUser.lastname
            params["email"] = srcUser.email
            accountType = srcUser.accountType
            if (accountType == "2"):
                accountType = "0"
            params["accounttype"] = accountType
            params["account"] = srcUser.accountName
            params["domainid"] = domainId
            newUserId = system.api.runSyncApiCommand("createUser", params, "user")
            if (newUserId != None):
                if (system.db.updateDbValues("user", {"password":srcUser.password}, {"id":newUserId})):
                    return User.get(system, newUserId)
                else:
                    return None
            else:
                return None

class ServiceOffering:
    # Vars: system, id, numCpus, speed, memory, disk

    def __init__(self, system, offeringId, numCpus, speed, memory, disk):
        self.system = system
        self.id = offeringId
        self.numCpus = numCpus
        self.speed = speed
        self.memory = memory
        self.disk = disk

    def __str__(self):
        return "ServiceOffering: %s | id: %s | numCpus: %s | speed: %s | memory: %s | disk: %s" % (self.system, self.id, self.numCpus, self.speed, self.memory, self.disk)

    @staticmethod
    def getCorrespondingServiceOffering(srcServiceOfferingId):
        srcServiceOffering = ServiceOffering.getSrcSystemServiceOfferingById(srcServiceOfferingId)
        destServiceOffering = ServiceOffering.getDestSystemServiceOffering(srcServiceOffering.numCpus, srcServiceOffering.speed, srcServiceOffering.memory)
        return destServiceOffering

    @staticmethod
    def getDestSystemServiceOffering(numCpus, speed, memory):
        serviceOfferings = GLOBALS["DEST_SYSTEM"].db.getDbValues("service_offering", ["id"], {"cpu":numCpus, "speed":speed, "ram_size":memory, "guest_ip_type":"Virtualized"})
        if (len(serviceOfferings) > 0):
            return ServiceOffering(GLOBALS["DEST_SYSTEM"], serviceOfferings[0]["id"], numCpus, speed, memory, None)
        else:
            return None

    @staticmethod
    def getSrcSystemServiceOfferingByVmId(vmId):
        serviceOfferingId = GLOBALS["SRC_SYSTEM"].db.getDbValues("user_vm", ["service_offering_id"], {"id":vmId})[0]["service_offering_id"]
        return getSrcSystemServiceOfferingById(serviceOfferingId)

    @staticmethod
    def getSrcSystemServiceOfferingById(serviceOfferingId):
        columns = ["id", "cpu", "speed", "ram_size", "disk"]
        serviceOfferings = GLOBALS["SRC_SYSTEM"].db.getDbValues("service_offering", columns, {"id":serviceOfferingId})
        if (len(serviceOfferings) > 0):
            offering = serviceOfferings[0]
            return ServiceOffering(GLOBALS["SRC_SYSTEM"], offering["id"], offering["cpu"], offering["speed"], offering["ram_size"], offering["disk"])
        else:
            return None

    @staticmethod
    def getSrcSystemServiceOfferings():
        serviceOfferings = []
        columns = ["id", "cpu", "speed", "ram_size", "disk"]
        srcServiceOfferings = GLOBALS["SRC_SYSTEM"].db.getDbValues("service_offering", columns, {})
        for offering in srcServiceOfferings:
            serviceOfferings.append(ServiceOffering(GLOBALS["SRC_SYSTEM"], offering["id"], offering["cpu"], offering["speed"], offering["ram_size"], offering["disk"]))
        return serviceOfferings

class DiskOffering:
    # Vars: id, size

    def __init__(self, diskOfferingId, size):
        self.id = diskOfferingId
        self.size = size

    def __str__(self):
        return "Disk Offering: size = %s" % (self.size)
        
    @staticmethod
    def getDestDiskOffering(size):
        columns = ["id"]
        diskOfferingRows = GLOBALS["DEST_SYSTEM"].db.getDbValues("disk_offering", ["id"], {"disk_size":size, "type":"Disk"})
        if (len(diskOfferingRows) > 0):
            return DiskOffering(diskOfferingRows[0]["id"], size)
        else:
            size = ((int(size) / 1024) + 1) * 1024
            diskOfferingRows = GLOBALS["DEST_SYSTEM"].db.getDbValues("disk_offering", ["id"], {"disk_size":size, "type":"Disk"})
            if (len(diskOfferingRows) > 0):
                return DiskOffering(diskOfferingRows[0]["id"], size)
            else:
                return None

    @staticmethod
    def getCorrespondingDiskOffering(srcServiceOfferingId):
        srcServiceOffering = ServiceOffering.getSrcSystemServiceOfferingById(srcServiceOfferingId)
        diskOffering = DiskOffering.getDestDiskOffering(srcServiceOffering.disk)
        return diskOffering


class VM:
    # Vars: id, system, user, serviceOfferingId, name, templateId, guestOsId
    
    def __init__(self, vmId, user, serviceOfferingId, guestOsId, guestOsCategoryId):
        self.id = vmId
        self.system = user.system
        self.user = user
        self.serviceOfferingId = serviceOfferingId
        self.guestOsId = guestOsId
        self.guestOsCategoryId = guestOsCategoryId

    def __str__(self):
        return "UserVM: id = %s | username = %s | system = %s" % (self.id, self.user.username, self.system)

    def getName(self):
        columns = ["name"]
        return self.system.db.getDbValues("vm_instance", columns, {"id":self.id})[0]["name"]
    
    @basicLogDecorator("Deploying a temporary VM...")
    def deployTemp(self):
        params = {"account":self.user.accountName, "domainid":self.user.domainId, "zoneId":self.system.zoneId, "serviceofferingid":self.system.defaultServiceOfferingId, "templateid":self.system.templateId}
        vmId = self.system.api.runAsyncApiCommand("deployVirtualMachine", params, "virtualmachine")
        if (vmId in (None, False)):
            return False
        self.id = vmId
        self.name = self.getName() + "-temp-vm"
        success = self.system.db.updateDbValues("vm_instance", {"name":self.name}, {"id":self.id})
        return success

    @basicLogDecorator("Deploying a new VM for the user ...")    
    def deploy(self, srcVm):
        params = dict()
        params["account"] = self.user.accountName
        params["domainid"] = self.user.domainId
        params["zoneid"] = self.system.zoneId        
        params["serviceofferingid"] = self.serviceOfferingId
        params["templateid"] = self.system.isoId
        params["diskofferingid"] = self.system.defaultDiskOfferingId
        vmId = self.system.api.runAsyncApiCommand("deployVirtualMachine", params, "virtualmachine")
        if (vmId in (None, False)):
            return None
        self.id = vmId        
        self.name = self.getName() + "-" + str(srcVm.id) + " (" + VM.getGuestOsName(GLOBALS["DEST_SYSTEM"], self.guestOsId) + ")"
        success = self.system.db.updateDbValues("vm_instance", {"name":self.name}, {"id":self.id})
        return success
        
    @verboseLogDecorator("Updating the guest OS ID for the VM...")
    def updateGuestOsId(self):
        setParams = {"guest_os_id":self.guestOsId}
        whereParams = {"id":self.id}
        return self.system.db.updateDbValues("vm_instance", setParams, whereParams)
        
    @basicLogDecorator("Starting VM...")
    def start(self):
        params = {"id":self.id}
        return self.system.api.runAsyncApiCommand("startVirtualMachine", params, "virtualmachine")

    @basicLogDecorator("Stopping VM...")
    def stop(self):
        params = {"id":self.id}
        if (self.system.api.asyncApi):
            return self.system.api.runAsyncApiCommand("stopVirtualMachine", params, "virtualmachine")
        else:
            return self.system.api.runSyncApiCommand("stopVirtualMachine", params, None)

    @basicLogDecorator("Destroying temporary VM...")
    def destroy(self):
        params = {"id":self.id}
        return self.system.api.runAsyncApiCommand("destroyVirtualMachine", params, "virtualmachine")

    @verboseLogDecorator("Detaching ISO from VM...")
    def detachIso(self):
        isoId = self.system.db.getDbValues("vm_instance", ["iso_id"], {"id":self.id})[0]["iso_id"]
        if (isoId == "None"):
            return True
        
        params = {"virtualmachineid":self.id}
        return self.system.api.runAsyncApiCommand("detachIso", params, "virtualmachine")

    def isLinuxVm(self):
        return (self.guestOsCategoryId != str(GLOBALS["DEST_WINDOWS_GUEST_OS_CATEGORY_ID"]))

    @staticmethod
    def getGuestOsName(system, guestOsId):
        columns = ["id", "display_name"]
        guestOsList = system.db.getDbValues("guest_os", columns, {"id":guestOsId})
        if (len(guestOsList) > 0):
            return guestOsList[0]["display_name"]
        else:
            return None

    @staticmethod
    def getGuestOsCategoryId(system, guestOsId):
        columns = ["category_id"]
        return system.db.getDbValues("guest_os", columns, {"id":guestOsId})[0]["category_id"]
    
    @staticmethod
    def getVmId(system, accountId, guestIpAddress):
        userVms = system.db.getDbValues("user_vm", ["id"], {"account_id":accountId, "guest_ip_address":guestIpAddress})
        for userVm in userVms:
            vmInstances = system.db.getDbValues("vm_instance", ["id"], {"id":userVm["id"], "removed":"null", "state":"neq:Destroyed"})
            if (len(vmInstances) > 0):
                return vmInstances[0]["id"]
        return None

    @staticmethod
    def getVms(user):
        system = user.system
        vms = []
        columns = ["id", "service_offering_id"]
        userVmRows = system.db.getDbValues("user_vm", columns, {"account_id":user.accountId})
        for userVmRow in userVmRows:
            vmInstanceRow = system.db.getDbValues("vm_instance", ["vm_template_id", "removed"], {"id":userVmRow["id"]})[0]
            if (vmInstanceRow["removed"] != "None"):
                continue

            # Determine the service offering ID
            serviceOfferingId = userVmRow["service_offering_id"]
            
            # Determine the new guest OS id and category id
            templateId = vmInstanceRow["vm_template_id"]
            guestOsId = GLOBALS["GUEST_OS_MAP"][templateId]
            guestOsCategoryId = VM.getGuestOsCategoryId(GLOBALS["DEST_SYSTEM"], guestOsId)
            
            vms.append(VM(userVmRow["id"], user, serviceOfferingId, guestOsId, guestOsCategoryId))
        return vms

    @staticmethod
    def getCorrespondingVm(destUser, srcVm):
        system = destUser.system
        columns = ["id", "guest_os_id"]
        correspondingVms = system.db.getDbValues("vm_instance", columns, {"name":"like:%-" + srcVm.id + " (%"})
        if (len(correspondingVms) > 0):
            correspondingVm = correspondingVms[0]
            newServiceOffering = ServiceOffering.getCorrespondingServiceOffering(srcVm.serviceOfferingId)
            vmId = correspondingVm["id"]
            guestOsId = srcVm.guestOsId
            guestOsCategoryId = VM.getGuestOsCategoryId(GLOBALS["DEST_SYSTEM"], guestOsId)
            return VM(vmId, destUser, newServiceOffering.id, guestOsId, guestOsCategoryId)
        else:
            return None

    @staticmethod
    def getTempVm(user):
        system = user.system
        columns = ["id"]
        tempVms = system.db.getDbValues("vm_instance", columns, {"removed":"null", "state":"neq:Destroyed", "name":"like:%-temp-vm"})
        if (len(tempVms) > 0):
            tempVm = tempVms[0]
            return VM(tempVm["id"], user, None, None, None)
        else:
            return None

    @staticmethod
    def getTemplate(system, templateId):
        columns = ["id", "name", "format"]
        templates = system.db.getDbValues("vm_template", columns, {"id":templateId})
        if (len(templates) > 0):
            return templates[0]
        else:
            return None

    @staticmethod
    def getTemplateIds(system):
        templateIds = []
        columns = ["id", "unique_name"]
        templates = system.db.getDbValues("vm_template", columns, {})
        for template in templates:
            if (template["unique_name"] == "routing"):
                continue
            templateIds.append(template["id"])
        return templateIds

    @staticmethod
    @basicLogDecorator("Migrating the user's VMs...")    
    def migrateVirtualMachines(srcUser, destUser):
        # Maintain a map of src system VM IDs to dest system VM ids
        vmIdMap = dict()
        
        # Get a list of user VMs for the source user
        srcVms = VM.getVms(srcUser)
    
        for srcVm in srcVms:
            # Try to find an existing VM in the dest system that corresponds to the VM in the src system
            destVm = VM.getCorrespondingVm(destUser, srcVm)
            
            # If there is no corresponding VM, deploy a new VM in the dest system
            if (destVm == None):
                destVm = VM(None, destUser, srcVm.serviceOfferingId, srcVm.guestOsId, srcVm.guestOsCategoryId)
                destVm.deploy(srcVm)

            # Add a mapping between the src VM and the dest VM
            vmIdMap[srcVm.id] = destVm.id                    

            # Get a list of volumes for the source VM
            srcVolumes = Volume.getSrcVolumes(srcUser, srcVm)

            # If these volumes have already been copied to the dest system, skip migration for this VM
            vmAlreadyMigrated = True
            for srcVolume in srcVolumes:
                destVolume = Volume.getDestVolume(None, destVm, srcVolume.type)
                if (destVolume == None):
                    vmAlreadyMigrated = False
                    break
                elif (srcVolume.id != destVolume.name.split("-")[-1]):
                    vmAlreadyMigrated = False
                    break

            if (vmAlreadyMigrated):
                writeToLog("\n" + str(srcVm) + " has already been migrated.", True)
                continue
            else:
                writeToLog("\nMigrating volumes for source VM: " + str(srcVm), True)
            
            # Stop the dest VM
            destVm.stop()        

            # Stop the source VM
            srcVm.stop()

            for srcVolume in srcVolumes:
                destVolume = None
                if (srcVolume.type == "DATADISK"):
                    destVolume = Volume.getDestVolume(None, destVm, "DATADISK")
                    if (destVolume == None):
                        diskOffering = DiskOffering.getCorrespondingDiskOffering(srcVm.serviceOfferingId)
                        destVolume = Volume(GLOBALS["DEST_SYSTEM"], None, str(destVm.id) + "-DATADISK", None, None, None, "DATA", diskOffering.id)
                        destVolume.createAndAttach(destVm)
                else:
                    destVolume = Volume.getDestVolume(None, destVm, "ROOT")

                # If the dest volume is already tagged with the source volume's ID, we don't need to do a copy
                if (srcVolume.id == destVolume.name.split("-")[-1]):
                    writeToLog(str(srcVolume) + " has already been migrated.")
                    continue

                # If the srcVolume's iSCSI SR isn't created on the XenServer, create it
                srcHost = Host.getHost(GLOBALS["SRC_SYSTEM"], srcVolume.hostId)
                srcSR = SR.getExistingSrcSr(srcHost.ip, srcHost.iqn)
                if (srcSR == None):
                    srcSR = SR(GLOBALS["DEST_SYSTEM"], srcHost.ip, srcHost.iqn, None)
                    srcSR.create()
                else:
                    writeToLog("Found existing SR: " + str(srcSR), False)
                    
                # Find the VDI corresponding to the src volume
                srcVdi = VDI(srcSR, srcVolume, None)

                # Find the SR corresponding to the dest storage pool
                destStoragePool = StoragePool.getStoragePool(GLOBALS["DEST_SYSTEM"], destVolume.poolId)
                destSR = SR(GLOBALS["DEST_SYSTEM"], None, None, destStoragePool.uuid)
                destSR.find()
                
                # Copy the src VDI to the dest SR
                copiedVdiUuid = srcVdi.copy(destSR)

                # If this is the rootdisk of a Linux VM, change the disk name
                destVdi = VDI(destSR, destVolume, copiedVdiUuid)
                if (destVolume.type == "ROOT" and srcVm.isLinuxVm()):
                    destVdi.changeBootableDeviceName()                    
                                            
                # Destroy the VM's old VDI
                oldDestVdi = VDI(destSR, destVolume, destVolume.path)
                oldDestVdi.destroy()
    
                # Update the destVolume's database record to have the UUID of the copied VDI, the virtual size of the copied VDI, and the ID of the source volume
                destVolume.update(copiedVdiUuid, destVdi.getVirtualSize(), destVolume.name + "-" + srcVolume.id)

            # Detach the dest VM's ISO
            destVm.detachIso()         

            # Update the guest OS ID for the VM
            destVm.updateGuestOsId()
        
            # Start the dest VM
            destVm.start()                
            
        return vmIdMap

class Volume:
    # vars: system, id, hostId, poolId, path, zoneId, iscsiName, type, diskOfferingId

    def __init__(self, system, volumeId, name, poolOrHostId, path, iscsiName, volumeType, diskOfferingId):
        self.system = system
        self.id = volumeId
        self.name = name
        if (iscsiName == None):
            self.poolId = poolOrHostId
            self.iscsiName = None
        else:
            self.hostId = poolOrHostId
            self.iscsiName = iscsiName
        self.path = path
        self.type = volumeType
        self.diskOfferingId = diskOfferingId

    def __str__(self):
        return "Volume: %s | type: %s | path: %s" % (self.system, self.type, self.path)

    @basicLogDecorator("Creating a new volume and attaching it to the user's VM...")
    def createAndAttach(self, destVm):
        params = dict()
        params["account"] = destVm.user.accountName
        params["domainid"] = destVm.user.domainId
        params["name"] = self.name
        params["zoneid"] = self.system.zoneId
        params["diskofferingid"] = self.diskOfferingId
        volumeId = self.system.api.runAsyncApiCommand("createVolume", params, "volume")        
        if (volumeId in (None, False)):
            return False        
        self.id = volumeId
        params = dict()
        params["id"] = volumeId
        params["virtualmachineid"] = destVm.id
        success = self.system.api.runAsyncApiCommand("attachVolume", params, "volume")        
        if (success in (None, False)):
            return False        
        newVolume = Volume.getDestVolume(volumeId, None, None)
        self.poolId = newVolume.poolId
        self.path = newVolume.path
        return True

    def update(self, volumeUuid, volumeSize, name):
        setParams = {"path":volumeUuid, "size":volumeSize, "name":name}
        whereParams = {"id":self.id}
        return self.system.db.updateDbValues("volumes", setParams, whereParams)

    @staticmethod
    def getSrcVolumes(user, vm):
        volumes = []
        columns = ["id", "name", "host_id", "path", "iscsi_name", "volume_type", "offering_id"]
        volumeRows = GLOBALS["SRC_SYSTEM"].db.getDbValues("volumes", columns, {"account_id":user.accountId, "instance_id":vm.id, "removed":"null"})
        for volumeRow in volumeRows:
            volumes.append(Volume(GLOBALS["SRC_SYSTEM"], volumeRow["id"], volumeRow["name"], volumeRow["host_id"], volumeRow["path"], volumeRow["iscsi_name"], volumeRow["volume_type"], volumeRow["offering_id"]))
        return volumes

    @staticmethod
    def getDestVolume(volumeId, vm, volumeType):
        columns = ["id", "name", "pool_id", "path", "disk_offering_id"]
        whereParams = None
        if (volumeId != None):
            whereParams = {"id":volumeId}
        else:
            whereParams = {"instance_id":vm.id, "volume_type":volumeType}
        volumeRows = GLOBALS["DEST_SYSTEM"].db.getDbValues("volumes", columns, whereParams)
        if (len(volumeRows) > 0):
            volumeRow = volumeRows[0]
            return Volume(GLOBALS["DEST_SYSTEM"], volumeRow["id"], volumeRow["name"], volumeRow["pool_id"], volumeRow["path"], None, volumeType, volumeRow["disk_offering_id"])
        else:
            return None
        
class DomainRouter:
    # Vars: id, system, user

    def __init__(self, user):
        self.system = user.system
        self.user = user
        self.id = self.getId()

    def __str__(self):
        return "DomainRouter: %s" % (self.user)

    @basicLogDecorator("Stopping user's router...")
    def stop(self):
        if (self.id == None):
            raise Exception("Could not find router for " + str(self.user))
        params  = {"id":self.id}
        if (self.system.api.asyncApi):
            return self.system.api.runAsyncApiCommand("stopRouter", params, "router")
        else:
            return self.system.api.runSyncApiCommand("stopRouter", params, None)

    @basicLogDecorator("Starting user's router...")
    def start(self):
        if (self.id == None):
            return False
        params = {"id":self.id}
        if (self.system.api.asyncApi):
            return self.system.api.runAsyncApiCommand("startRouter", params, "router")
        else:
            return self.system.api.runSyncApiCommand("startRouter", params, None)

    @basicLogDecorator("Rebooting user's router...")
    def reboot(self):
        if (self.id == None):
            return False
        params = {"id":self.id}
        return self.system.api.runAsyncApiCommand("rebootRouter", params, "router")

    def getId(self):
        routers = self.system.db.getDbValues("domain_router", ["id"], {"account_id":self.user.accountId})
        if (len(routers) > 0):
            return routers[0]["id"]
        else:
            return None

class PublicIp:
    # Vars: system, user, address, zoneId, sourceNat, allocated

    def __init__(self, system, user, address, zoneId, sourceNat, allocated):
        self.system = system
        self.user = user
        self.address = address
        self.zoneId = zoneId
        self.sourceNat = sourceNat
        self.allocated = allocated

    def __str__(self):
        return self.address

    def __repr__(self):
        return self.address

    def allocate(self):
        setParams = {"account_id":self.user.accountId,
                     "domain_id":self.user.domainId,
                     "source_nat":self.sourceNat,
                     "allocated":self.allocated}
        whereParams = {"public_ip_address":self.address,
                       "data_center_id":self.zoneId}
        return self.system.db.updateDbValues("user_ip_address", setParams, whereParams)

    @staticmethod
    @basicLogDecorator("Clearing existing public IPs...")    
    def clearPublicIps(user):
        system = user.system
        setParams = {"account_id":"null", "domain_id":"null", "source_nat":"0", "allocated":"null"}
        whereParams = {"account_id":user.accountId}
        return system.db.updateDbValues("user_ip_address", setParams, whereParams)

    @staticmethod
    @basicLogDecorator("Migrating allocated public IPs...")
    def migrateAllocatedPublicIps(srcUser, destUser):
        # Get a list of public IPs allocated to the source user
        ips = PublicIp.getAllocatedPublicIps(srcUser)
        
        # Allocate each one of these IPs in the dest system
        for ip in ips:
            ip.system = GLOBALS["DEST_SYSTEM"]
            ip.user = destUser
            if (not ip.allocate()):
                return None
            
        return ips

    @staticmethod
    def getAllocatedPublicIps(user):
        system = user.system
        ips = []
        columns = ["public_ip_address", "data_center_id", "source_nat", "allocated"]
        ipRows = system.db.getDbValues("user_ip_address", columns, {"account_id":user.accountId})
        for ipRow in ipRows:
            ips.append(PublicIp(system, user, ipRow["public_ip_address"], ipRow["data_center_id"], ipRow["source_nat"], ipRow["allocated"]))
        return ips

    @staticmethod
    def getGuestIpAddress(system, vmId):
        columns = ["guest_ip_address"]
        guestIp = system.db.getDbValues("user_vm", columns, {"id":vmId})[0]
        return guestIp["guest_ip_address"]

class ForwardingRule:
    @staticmethod
    @basicLogDecorator("Migrating port forwarding and load balancer rules...")    
    def migrateForwardingRules(srcUser, destUser, publicIps, vmIdMap):
        for publicIp in publicIps:
            forwardingRules = ForwardingRule.getSrcForwardingRules(srcUser, destUser, publicIp.address, vmIdMap)
            for forwardingRule in forwardingRules:
                newRuleId = forwardingRule.createInDestSystem()
                if (newRuleId == None):
                    return False
        return True
        
    @staticmethod
    def getSrcForwardingRules(srcUser, destUser, address, vmIdMap):
        # vmIdMap maps UserVM database IDs in the src system to UserVM database IDs in the dest system
        columns = ["id", "public_port", "private_ip_address", "private_port", "enabled", "protocol", "forwarding", "algorithm"]
        ruleRows = GLOBALS["SRC_SYSTEM"].db.getDbValues("ip_forwarding", columns, {"public_ip_address":address})
        activeRules = []
        for ruleRow in ruleRows:
            srcVmId = VM.getVmId(GLOBALS["SRC_SYSTEM"], srcUser.accountId, ruleRow["private_ip_address"])
            destVmId = vmIdMap.get(srcVmId)
            if (destVmId == None):
                continue

            if (ruleRow["forwarding"] == "1"):
                activeRules.append(ForwardingRule.PortForwardingRule(address, ruleRow["public_port"], PublicIp.getGuestIpAddress(GLOBALS["DEST_SYSTEM"], destVmId), ruleRow["private_port"], ruleRow["enabled"], ruleRow["protocol"]))
            else:
                activeRules.append(ForwardingRule.LoadBalancerRule(destUser.accountId, address, ruleRow["public_port"], ruleRow["private_port"], destVmId, ruleRow["algorithm"]))
                           
        return activeRules 
    
    class PortForwardingRule:
        def __init__(self, publicIp, publicPort, privateIp, privatePort, enabled, protocol):
            self.publicIp = publicIp
            self.publicPort = publicPort
            self.privateIp = privateIp
            self.privatePort = privatePort
            self.enabled = enabled
            self.protocol = protocol

        def createInDestSystem(self):
            setParams = dict()
            setParams["public_ip_address"] = self.publicIp
            setParams["public_port"] = self.publicPort
            setParams["private_ip_address"] = self.privateIp
            setParams["private_port"] = self.privatePort
            setParams["enabled"] = self.enabled
            setParams["protocol"] = self.protocol
            setParams["forwarding"] = "1"
            setParams["algorithm"] = "null"
            setParams["group_id"] = "null"
            return GLOBALS["DEST_SYSTEM"].db.insertIntoDb("ip_forwarding", setParams)
        
    class LoadBalancerRule:
        def __init__(self, accountId, ip, publicPort, privatePort, vmId, algorithm):
            self.accountId = accountId
            self.ip = ip
            self.publicPort = publicPort
            self.privatePort = privatePort
            self.vmId = vmId
            self.algorithm = algorithm

        def createInDestSystem(self):
            setParams = dict()
            setParams["name"] = str(self.publicPort) + "-" + str(self.privatePort)
            setParams["account_id"] = self.accountId
            setParams["ip_address"] = self.ip
            setParams["public_port"] = self.publicPort
            setParams["private_port"] = self.privatePort
            setParams["algorithm"] = self.algorithm
            newLoadBalancerRuleId = GLOBALS["DEST_SYSTEM"].db.insertIntoDb("load_balancer", setParams)
            if (newLoadBalancerRuleId == None or newLoadBalancerRuleId == "0"):
                return None
            setParams = dict()
            setParams["load_balancer_id"] = newLoadBalancerRuleId
            setParams["instance_id"] = self.vmId
            return GLOBALS["DEST_SYSTEM"].db.insertIntoDb("load_balancer_vm_map", setParams)               

class SR:
    def __init__(self, system, ip, iqn, uuid):
        self.system = system
        self.ip = ip
        self.iqn = iqn
        self.uuid = uuid

    def __str__(self):
        return "SR: %s | ip: %s | iqn: %s | uuid: %s" % (self.system, self.ip, self.iqn, self.uuid)
    
    @verboseLogDecorator("Finding SR...")
    def find(self):
        xenapi = self.system.xenapi
        self.ref = xenapi.SR.get_by_name_label(self.uuid)[0]
        return True
        
    @verboseLogDecorator("Finding source system's iSCSI SR...")        
    def create(self):
        xenapi = self.system.xenapi
        host = xenapi.host.get_all()[0]
        deviceConfig = {'targetIQN': self.iqn, 'target': self.ip}
        srRef = None
        name = "1.0 iSCSI pool: " + self.ip + "-" + self.iqn
        srRef = xenapi.SR.create(host, deviceConfig, "0", name, name, "iscsi", "user", True)
        if (srRef != None):
            self.ref = srRef
            return True
        else:
            return False            
        
    @staticmethod
    def getExistingSrcSr(ip, iqn):
        xenapi = GLOBALS["DEST_SYSTEM"].xenapi
        srRefs = xenapi.SR.get_all()
        for srRef in srRefs:
            srNameLabel = xenapi.SR.get_name_label(srRef)
            if (srNameLabel == "1.0 iSCSI pool: " + ip + "-" + iqn):
                sr = SR(GLOBALS["DEST_SYSTEM"], ip, iqn, xenapi.SR.get_uuid(srRef))
                sr.ref = srRef
                return sr
        return None

    @staticmethod
    @verboseLogDecorator("Forgetting all src iSCSI SRs...")    
    def forgetAllSrcSrs():
        xenapi = GLOBALS["DEST_SYSTEM"].xenapi
        srRefs = xenapi.SR.get_all()
        for srRef in srRefs:
            srNameLabel = xenapi.SR.get_name_label(srRef)
            if ("1.0 iSCSI pool" in srNameLabel):
                # Unplug and destroy the SR's PBDs
                pbdRefs = xenapi.SR.get_PBDs(srRef)
                for pbdRef in pbdRefs:
                    xenapi.PBD.unplug(pbdRef)
                    xenapi.PBD.destroy(pbdRef)

                # Forget the SR
                xenapi.SR.forget(srRef)                    

        return True

class VDI:
    def __init__(self, sr, volume, uuid):
        self.sr = sr
        self.volume = volume
        self.uuid = uuid
        self.find()

    def __str__(self):
        return "VDI: %s | uuid: %s" % (self.volume, self.uuid)

    @verboseLogDecorator("Getting virtual size for VDI...")
    def getVirtualSize(self):
        xenapi = self.sr.system.xenapi
        return xenapi.VDI.get_virtual_size(self.ref)
        
    @basicLogDecorator("Copying source system volume to dest system...")
    def copy(self, destSR):
        xenapi = self.sr.system.xenapi
        newVdiRef = xenapi.VDI.copy(self.ref, destSR.ref)
        return xenapi.VDI.get_uuid(newVdiRef)

    @basicLogDecorator("Destroying old volume...")
    def destroy(self):
        xenapi = self.sr.system.xenapi
        xenapi.VDI.destroy(self.ref)
        return True
    
    @verboseLogDecorator("Finding VDI in SR...")
    def find(self):
        xenapi = self.sr.system.xenapi        
        if (self.uuid == None):
            # Run an sr-scan
            xenapi.SR.scan(self.sr.ref)
            
            # Get a list of VDIs in the SR
            vdiRefs = xenapi.SR.get_VDIs(self.sr.ref)
                
            # Find the VDI that has the same SCSI ID as the specified volume
            volumeScsiId = self.volume.iscsiName.split(":")[-1].strip()
            for vdiRef in vdiRefs:
                smConfig = xenapi.VDI.get_sm_config(vdiRef)
                vdiScsiId = smConfig["SCSIid"].strip()[1:]
                if (vdiScsiId == volumeScsiId):
                    self.ref = vdiRef
                    self.uuid = xenapi.VDI.get_uuid(vdiRef)
                    return True
                
            return False
        else:
            self.ref = xenapi.VDI.get_by_uuid(self.uuid)
            return True
        
    @basicLogDecorator("Changing disk name for VDI...")
    def changeBootableDeviceName(self):
        system = self.volume.system
        xenapi = system.xenapi
        controlDomainRef = system.controlDomainRef        

        vbdRef = None
        try:
            # Create a VBD for the VDI
            vbd = {'bootable': True, 'userdevice': '0', 'VDI': self.ref,
                   'other_config': {}, 'VM': controlDomainRef,
                   'mode': 'rw', 'qos_algorithm_type': '', 'qos_algorithm_params': {},
                   'type': 'Disk', 'empty': False, 'unpluggable': True}
            vbdRef = xenapi.VBD.create(vbd)
            
            # Plug the VBD
            xenapi.VBD.plug(vbdRef)
    
            # Create a temporary directory
            if (not system.runSshCommand("mkdir -p /root/temp")):
                raise Exception ("Failed to create directory /root/temp")
    
            # Check if /dev/xvda1 exists
            xvda1Exists = system.runSshCommand("ls /dev/xvda1")
    
            # If /dev/xvda1 doesn't exist, work with /dev/xvda
            if (not xvda1Exists):
                # Mount /dev/xvda to /root/temp
                if (not system.runSshCommand("mount /dev/xvda /root/temp")):
                    raise Exception("Failed to mount /dev/xvda to /root/temp")
                writeToLog("Using /dev/xvda to change bootable device name.", False)
            else:
                # Mount /dev/xvda1 to /root/temp
                if (not system.runSshCommand("mount /dev/xvda1 /root/temp")):
                    raise Exception("Failed to mount /dev/xvda1 to /root/temp")

                # If the boot directory exists under /root/temp, we can work with xvda1 
                if (system.runSshCommand("ls /root/temp/boot")):
                    writeToLog("Using /dev/xvda1 to change bootable device name.", False)
                else:
                    # If the boot directory doesn't exist under /root/temp, we need to work with /dev/xvda2
                    
                    # Check that /dev/xvda2 exists
                    if (not system.runSshCommand("ls /dev/xvda2")):
                        raise Exception("/dev/xvda1 exists but /dev/xvda2 doesn't exist")

                    # Unmount /dev/xvda1
                    if (not system.runSshCommand("umount /root/temp")):
                        raise Exception("Failed to unmount /dev/xvda1")

                    # Mount /dev/xvda2
                    if (not system.runSshCommand("mount /dev/xvda2 /root/temp")):
                        raise Exception("Failed to mount /dev/xvda2")

                    writeToLog("Using /dev/xvda2 to change bootable device name.", False)
    
            # Modify fstab, grub.conf, and device.map, if they exist
            for fileToModify in ["/root/temp/etc/fstab", "/root/temp/boot/grub/grub.conf", "/root/temp/boot/grub/device.map"]:
                if (system.runSshCommand("ls " + fileToModify)):
                    if (not system.runSshCommand("sed -i 's_/dev/sda_/dev/xvda_' " + fileToModify)):
                        raise Exception("Failed to modify " + fileToModify)
        finally:
            # Unmount /root/temp if necessary
            if (system.runSshCommand("mount | grep '/root/temp'")):
                if (not system.runSshCommand("umount /root/temp")):
                    raise Exception("Failed to unmount /root/temp")

            # Delete /root/temp 
            system.runSshCommand("rm -rf /root/temp")
            
            if (vbdRef != None):
                # Unplug the VBD
                xenapi.VBD.unplug(vbdRef)
        
                # Destroy the VBD
                xenapi.VBD.destroy(vbdRef)

        return True
        
class StoragePool:
    # Vars: id, uuid

    def __init__(self, storagePoolId, uuid):
        self.id = storagePoolId
        self.uuid = uuid

    @staticmethod
    def getStoragePool(system, storagePoolId):
        columns = ["id", "uuid"]
        storagePoolRow = system.db.getDbValues("storage_pool", columns, {"id":storagePoolId})[0]
        return StoragePool(storagePoolRow["id"], storagePoolRow["uuid"])

class Host:
    # Vars: id, ip, iqn

    def __init__(self, hostId, ip, iqn):
        self.id = hostId
        self.ip = ip
        self.iqn = iqn

    def __str__(self):
        return "Host: id: %s | ip %s" % (self.id, self.ip)

    @staticmethod
    @basicLogDecorator("Sharing LUs...")
    def shareAllLus():
        return Host.shareOrUnshareAllLus(True)

    @staticmethod
    @basicLogDecorator("Unsharing LUs...")
    def unshareAllLus():
        return Host.shareOrUnshareAllLus(False)    

    @staticmethod
    def shareOrUnshareAllLus(share):
        # Get a map of XenServer IPs -> IQNs
        xenServerIqns = Host.getXenServerIqns()
        
        # Get a map of storage host IPs in the source system -> passwords
        storageHostPasswords = GLOBALS["STORAGE_HOST_PASSWORDS"]
        
        # Copy share_all_lus.sh to each storage host and run with each XenServer IQN
        for ip in storageHostPasswords.keys():
            password = storageHostPasswords[ip]
            sshConn = paramiko.SSHClient()
            sshConn.set_missing_host_key_policy(paramiko.AutoAddPolicy())
            sshConn.connect(ip, username = "root", password = password)            
            sftpConn = sshConn.open_sftp()
            sftpConn.put("share_all_lus.sh", "/root/share_all_lus.sh")
            for xenServerIp in xenServerIqns.keys():
                iqn = xenServerIqns[xenServerIp]
                command = "bash /root/share_all_lus.sh -i " + iqn
                if (share):
                    command += " -s"
                else:
                    command += " -u"
                stdin, stdout, stderr = sshConn.exec_command(command)
                if (stdin.channel.recv_exit_status() != 0):
                    return False
            if (not share):
                sftpConn.remove("/root/share_all_lus.sh")
            sshConn.close()
            sftpConn.close()

        return True
            
    @staticmethod
    def getHost(system, hostId):
        columns = ["id", "private_ip_address", "iqn"]
        hostRow =  system.db.getDbValues("host", columns, {"id":hostId})[0]
        return Host(hostRow["id"], hostRow["private_ip_address"], hostRow["iqn"])

    @staticmethod
    def getStorageHostIps():
        storageHostIps = []
        columns = ["private_ip_address"]
        hostRows = GLOBALS["SRC_SYSTEM"].db.getDbValues("host", columns, {"type":"Storage"})
        for hostRow in hostRows:
            storageHostIps.append(hostRow["private_ip_address"])
        return storageHostIps

    @staticmethod
    def getXenServerIqns():
        xenServerIqns = dict()
        columns = ["private_ip_address", "url"]
        hostRows = GLOBALS["DEST_SYSTEM"].db.getDbValues("host", columns, {"type":"Routing"})
        for hostRow in hostRows:
            xenServerIqns[hostRow["private_ip_address"]] = hostRow["url"]
        return xenServerIqns
    

### Runtime

GLOBALS = dict()

@basicLogDecorator("Reading upgrade.properties...")
def readUpgradeProperties():
    upgradePropertiesFile = open("upgrade.properties", "r")    
    upgradeProperties = upgradePropertiesFile.read().splitlines()
    for upgradeProperty in upgradeProperties:
        if (upgradeProperty.strip() == ""):
            continue
        elif (upgradeProperty.startswith("#")):
            continue
        else:
            propList = upgradeProperty.split("=")
            var = propList[0].strip()
            val = propList[1].strip()
            if (val == ""):
                continue
            GLOBALS[var] = val

    # Create the log file
    logFilePath = GLOBALS["LOG_FILE"]
    GLOBALS["LOG_FILE"] = open(logFilePath, "a")

    # Create the guest OS map
    GLOBALS["GUEST_OS_MAP"] = csvToMap(GLOBALS["GUEST_OS_MAP"])

    # Create the XenServer passwords map
    if (GLOBALS.get("DEST_XENSERVER_PASSWORDS") != None):
        GLOBALS["DEST_XENSERVER_PASSWORDS"] = csvToMap(GLOBALS.get("DEST_XENSERVER_PASSWORDS"))

    # Create the Storage Host passwords map
    if (GLOBALS.get("STORAGE_HOST_PASSWORDS") == None):
        raise Exception ("Please fill out the variable STORAGE_HOST_PASSWORDS in upgrade.properties.")
    else:
        GLOBALS["STORAGE_HOST_PASSWORDS"] = csvToMap(GLOBALS["STORAGE_HOST_PASSWORDS"])

    # Create the list of users to upgrade
    if GLOBALS.has_key("USERS"):
        GLOBALS["USERS"] = [userId.strip() for userId in GLOBALS["USERS"].split(",")]
    else:
        GLOBALS["USERS"] = None

    return True

def csvToMap(csv):
    entries = csv.split(",")
    entryMap = dict()
    for entry in entries:
        entryList = entry.strip().split(":")
        key = entryList[0].strip()
        val = entryList[1].strip()
        entryMap[key] = val
    return entryMap
    
@basicLogDecorator("Running diagnostic...")
def runDiagnostic():    
    # Either one XenServer IP and password should be specified, or a mapping between XenServer IPs and passwords should be specified
    if ((GLOBALS.get("DEST_XENSERVER_IP") == None and (GLOBALS.get("DEST_XENSERVER_PASSWORD") != None or GLOBALS.get("DEST_XENSERVER_PASSWORDS") == None))
        or (GLOBALS.get("DEST_XENSERVER_IP") != None and (GLOBALS.get("DEST_XENSERVER_PASSWORD") == None or GLOBALS.get("DEST_XENSERVER_PASSWORDS") != None))):
        raise Exception("Please specify the IP and root password for one XenServer (if all XenServers have the same root password), or the IPs and root passwords of all XenServers.")

    GLOBALS["SRC_SYSTEM"] = System(GLOBALS["SRC_MANAGEMENT_SERVER_IP"], False, None, None, None, "vmops", GLOBALS["SRC_DB_LOGIN"],
                                   GLOBALS.get("SRC_DB_PASSWORD"), GLOBALS["SRC_ZONE_ID"], None, None, None, None)
    
    GLOBALS["DEST_SYSTEM"] = System(GLOBALS["DEST_MANAGEMENT_SERVER_IP"], True, GLOBALS.get("DEST_XENSERVER_IP"),
                                    GLOBALS.get("DEST_XENSERVER_PASSWORD"), GLOBALS.get("DEST_XENSERVER_PASSWORDS"),
                                    "cloud", GLOBALS["DEST_DB_LOGIN"], GLOBALS.get("DEST_DB_PASSWORD"), GLOBALS["DEST_ZONE_ID"],
                                    GLOBALS["DEST_TEMPLATE_ID"], GLOBALS["DEST_ISO_ID"], GLOBALS["DEST_SERVICE_OFFERING_ID"],
                                    GLOBALS["DEST_DISK_OFFERING_ID"])

    srcSystemServiceOfferings = ServiceOffering.getSrcSystemServiceOfferings()
    for srcSystemServiceOffering in srcSystemServiceOfferings:
        # Every service offering in the src system must have a corresponding service offering in the dest system
        destSystemServiceOffering = ServiceOffering.getCorrespondingServiceOffering(srcSystemServiceOffering.id)
        if (destSystemServiceOffering == None):
            raise Exception("No corresponding service offering found for: " + str(srcSystemServiceOffering))

        # Every service offering in the src system has a corresponding disk offering in the dest system
        destSystemDiskOffering = DiskOffering.getCorrespondingDiskOffering(srcSystemServiceOffering.id)
        if (destSystemDiskOffering == None):
            raise Exception("No corresponding disk offering found for: " + str(srcSystemServiceOffering))

    # Every template ID in the src system has a valid entry in GUEST_OS_MAP
    srcSystemTemplateIds = VM.getTemplateIds(GLOBALS["SRC_SYSTEM"])
    for templateId in srcSystemTemplateIds:
        if (not GLOBALS["GUEST_OS_MAP"].has_key(templateId)):
            raise Exception("No corresponding guest OS ID for templateId: " + templateId)
        else:
            guestOsId = GLOBALS["GUEST_OS_MAP"][templateId]
            guestOsName = VM.getGuestOsName(GLOBALS["DEST_SYSTEM"], guestOsId)
            if (guestOsName == None):
                raise Exception("The guest OS ID that corresponds to template ID: " + templateId + " is not valid.")

    # The dest system's ISO id must be valid
    template = VM.getTemplate(GLOBALS["DEST_SYSTEM"], GLOBALS["DEST_ISO_ID"])
    if (template == None or template["format"] != "ISO"):
        raise Exception("The dest system ISO ID is not valid.")

    # Verify that all source system storage hosts have a password
    storageHostIps = Host.getStorageHostIps()
    for ip in storageHostIps:
        if (ip not in GLOBALS["STORAGE_HOST_PASSWORDS"].keys()):
            raise Exception("The storage host IP: " + str(ip) + " has no entry in STORAGE_HOST_PASSWORDS.")

    return True        

@basicLogDecorator("Starting CloudStack Migration (1.0 -> 2.1)...")
def upgradeUsers(userIds, onlyMigratePublicIps):
    # Read variables from upgrade.properties
    readUpgradeProperties()

    # Run the diagnostic
    runDiagnostic()

    if (userIds == None):
        if (GLOBALS["USERS"] == None):
            raise Exception("Please specify one or more users to upgrade.")
        else:
            userIds = GLOBALS["USERS"]

    # Make sure all users are valid
    for userId in userIds:
        if (User.get(GLOBALS["SRC_SYSTEM"], userId) == None):
            raise Exception("The user ID: " + str(userId) + " is not valid.")

    if (not onlyMigratePublicIps):
        # Share all LUs
        Host.shareAllLus()
    
    try:
        for userId in userIds:
            doUpgrade(userId, onlyMigratePublicIps)
        return True
    finally:
        if (not onlyMigratePublicIps):
            # Forget all iSCSI SRs
            SR.forgetAllSrcSrs()
            # Unshare all LUs
            Host.unshareAllLus()

def doUpgrade(userId, onlyMigratePublicIps):            
    # Get the specified user from the source system
    srcUser = User.get(GLOBALS["SRC_SYSTEM"], userId)

    writeToLog("\nStarting migration for " + str(srcUser), True)
    
    # Create a new user in the destination system with the same attributes as the original user
    destUser = User.create(GLOBALS["DEST_SYSTEM"], srcUser)

    if (not srcUser.alreadyMigrated()):
        # Allocate the src user's public IPs in the dest system
        allocatedPublicIps = PublicIp.migrateAllocatedPublicIps(srcUser, destUser)

        if (onlyMigratePublicIps):
            writeToLog("\nMigrated public IPs for " + str(srcUser), True)
            return 

        # Stop the source user's DomR
        srcUserDomR = DomainRouter(srcUser)

        # Only migrate the user's VMs if there is a DomR
        if (srcUserDomR.id != None):
            srcUserDomR.stop()
            
            # If the dest user doesn't have a DomR, deploy a temporary VM
            destUserDomR = DomainRouter(destUser)
            tempVm = None
            if (destUserDomR.id == None):
                tempVm = VM(None, destUser, None, None, None)
                tempVm.deployTemp()
                destUserDomR.id = destUserDomR.getId()
            else:
                tempVm = VM.getTempVm(destUser)
                            
            # Migrate the source user's VM's to the dest system
            vmIdMap = VM.migrateVirtualMachines(srcUser, destUser)    
                
            # Migrate the source user's port forwarding and load balancer rules
            ForwardingRule.migrateForwardingRules(srcUser, destUser, allocatedPublicIps, vmIdMap)
        
            # Reboot the dest user's router    
            destUserDomR.reboot()
        
            # Destroy the temporary VM
            if (tempVm != None):
                tempVm.destroy()

        srcUser.tagAsMigrated() 
    else:
        writeToLog("\n" + str(srcUser) + " has already been migrated.", True)

    writeToLog("\nMigration was successful for " + str(srcUser), True)

if (len(sys.argv) > 1):
    if (sys.argv[1].lower() == "publicips"):
        if (len(sys.argv) > 2):
            upgradeUsers(sys.argv[2:], True)
        else:
            upgradeUsers(None, True)
    else:
        upgradeUsers(sys.argv[1:], False)
else:
    upgradeUsers(None, False)
    
