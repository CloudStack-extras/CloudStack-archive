import xml.dom.minidom
from optparse import OptionParser
import os
import sys
class cmdParameterProperty(object):
    def __init__(self):
        self.name = None
        self.required = False
        self.desc = ""
        self.type = "planObject"
        self.subProperties = []
        
class cloudStackCmd:
    def __init__(self):
        self.name = ""
        self.desc = ""
        self.async = "false"
        self.request = []
        self.response = []

class codeGenerator:
    space = "    "

    cmdsName = []
    
    def __init__(self, outputFolder, apiSpecFile):
        self.cmd = None
        self.code = ""
        self.required = []
        self.subclass = []
        self.outputFolder = outputFolder
        self.apiSpecFile = apiSpecFile
        
    def addAttribute(self, attr, pro):
        value = pro.value
        if pro.required:
            self.required.append(attr)
        desc = pro.desc
        if desc is not None:
            self.code += self.space
            self.code += "''' " + pro.desc + " '''"
            self.code += "\n"
        
        self.code += self.space
        self.code += attr + " = " + str(value)
        self.code += "\n"
    
    def generateSubClass(self, name, properties):
        '''generate code for sub list'''
        subclass = 'class %s:\n'%name
        subclass += self.space + "def __init__(self):\n"
        for pro in properties:
            if pro.desc is not None:
                subclass += self.space + self.space + '""""%s"""\n'%pro.desc
            if len (pro.subProperties) > 0:
                subclass += self.space + self.space + 'self.%s = []\n'%pro.name
                self.generateSubClass(pro.name, pro.subProperties)
            else:
                subclass += self.space + self.space + 'self.%s = None\n'%pro.name
        
        self.subclass.append(subclass)
    def generate(self, cmd):
       
        self.cmd = cmd
        self.cmdsName.append(self.cmd.name)
        self.code += "\n"
        self.code += '"""%s"""\n'%self.cmd.desc  
        self.code += 'from baseCmd import *\n'
        self.code += 'from baseResponse import *\n'
        self.code += "class %sCmd (baseCmd):\n"%self.cmd.name
        self.code += self.space + "def __init__(self):\n"
        
        self.code += self.space + self.space + 'self.isAsync = "%s"\n' %self.cmd.async
        
        for req in self.cmd.request:
            if req.desc is not None:
                self.code += self.space + self.space + '"""%s"""\n'%req.desc
            if req.required == "true":
                self.code += self.space + self.space + '"""Required"""\n'
            
            value = "None"
            if req.type == "list" or req.type == "map":
                value = "[]"
            
            self.code += self.space + self.space  + 'self.%s = %s\n'%(req.name,value)
            if req.required == "true":
                self.required.append(req.name)
        
        self.code += self.space + self.space + "self.required = ["
        for require in self.required:
            self.code += '"' + require + '",'
        self.code += "]\n"
        self.required = []
        
        
        """generate response code"""
        subItems = {}
        self.code += "\n"
        self.code += 'class %sResponse (baseResponse):\n'%self.cmd.name
        self.code += self.space + "def __init__(self):\n"
        if len(self.cmd.response) == 0:
            self.code += self.space + self.space + "pass"
        else:
            for res in self.cmd.response:
                if res.desc is not None:
                    self.code += self.space + self.space + '"""%s"""\n'%res.desc
            
                if len(res.subProperties) > 0:
                    self.code += self.space + self.space + 'self.%s = []\n'%res.name
                    self.generateSubClass(res.name, res.subProperties)
                else:
                    self.code += self.space + self.space + 'self.%s = None\n'%res.name
        self.code += '\n'
        
        for subclass in self.subclass:
            self.code += subclass + "\n"
        
        fp = open(self.outputFolder + "/cloudstackAPI/%s.py"%self.cmd.name, "w")
        fp.write(self.code)
        fp.close()
        self.code = ""
        self.subclass = []
       
        
    def finalize(self):
        '''generate an api call'''
        
        header = '"""Test Client for CloudStack API"""\n'
        imports = "import copy\n"
        initCmdsList = '__all__ = ['
        body = ''
        body += "class CloudStackAPIClient:\n"
        body += self.space + 'def __init__(self, connection):\n'
        body += self.space + self.space + 'self.connection = connection\n'
        body += "\n"
        
        body += self.space + 'def __copy__(self):\n'
        body += self.space + self.space + 'return CloudStackAPIClient(copy.copy(self.connection))\n'
        body += "\n"
        
        for cmdName in self.cmdsName:
            body += self.space + 'def %s(self,command):\n'%cmdName
            body += self.space + self.space + 'response = %sResponse()\n'%cmdName
            body += self.space + self.space + 'response = self.connection.make_request(command, response)\n'
            body += self.space + self.space + 'return response\n'
            body += '\n'
        
            imports += 'from %s import %sResponse\n'%(cmdName, cmdName)
            initCmdsList += '"%s",'%cmdName
        
        fp = open(self.outputFolder + '/cloudstackAPI/cloudstackAPIClient.py', 'w')
        for item in [header, imports, body]:
            fp.write(item)
        fp.close()
        
        '''generate __init__.py'''
        initCmdsList += '"cloudstackAPIClient"]'
        fp = open(self.outputFolder + '/cloudstackAPI/__init__.py', 'w')
        fp.write(initCmdsList)
        fp.close()
        
        fp = open(self.outputFolder + '/cloudstackAPI/baseCmd.py', 'w')
        basecmd = '"""Base Command"""\n'
        basecmd += 'class baseCmd:\n'
        basecmd += self.space + 'pass\n'
        fp.write(basecmd)
        fp.close()
        
        fp = open(self.outputFolder + '/cloudstackAPI/baseResponse.py', 'w')
        basecmd = '"""Base class for response"""\n'
        basecmd += 'class baseResponse:\n'
        basecmd += self.space + 'pass\n'
        fp.write(basecmd)
        fp.close()
        
    
    def constructResponse(self, response):
        paramProperty = cmdParameterProperty()
        paramProperty.name = getText(response.getElementsByTagName('name'))
        paramProperty.desc = getText(response.getElementsByTagName('description'))
        if paramProperty.name.find('(*)') != -1:
            '''This is a list'''
            paramProperty.name = paramProperty.name.split('(*)')[0]
            for subresponse in response.getElementsByTagName('arguments')[0].getElementsByTagName('arg'):
                subProperty = self.constructResponse(subresponse)
                paramProperty.subProperties.append(subProperty)
        return paramProperty

    def loadCmdFromXML(self):
        dom = xml.dom.minidom.parse(self.apiSpecFile)
        cmds = []
        for cmd in dom.getElementsByTagName("command"):
            csCmd = cloudStackCmd()
            csCmd.name = getText(cmd.getElementsByTagName('name'))
            assert csCmd.name
        
            desc = getText(cmd.getElementsByTagName('description'))
            if desc: 
                csCmd.desc = desc
    
            async = getText(cmd.getElementsByTagName('isAsync'))
            if async:
                csCmd.async = async
        
            for param in cmd.getElementsByTagName("request")[0].getElementsByTagName("arg"):
                paramProperty = cmdParameterProperty()
            
                paramProperty.name = getText(param.getElementsByTagName('name'))
                assert paramProperty.name
            
                required = param.getElementsByTagName('required')
                if required:
                    paramProperty.required = getText(required)
            
                requestDescription = param.getElementsByTagName('description')
                if requestDescription:            
                    paramProperty.desc = getText(requestDescription)
            
                type = param.getElementsByTagName("type")
                if type:
                    paramProperty.type = getText(type)
                
                csCmd.request.append(paramProperty)
        
            responseEle = cmd.getElementsByTagName("response")[0]
            for response in responseEle.getElementsByTagName("arg"):
                if response.parentNode != responseEle:
                    continue
            
                paramProperty = self.constructResponse(response)
                csCmd.response.append(paramProperty)
            
            cmds.append(csCmd)
        return cmds
    
    def generateCode(self):
        cmds = self.loadCmdFromXML()
        for cmd in cmds:
            self.generate(cmd)
        self.finalize()

def getText(elements):
    return elements[0].childNodes[0].nodeValue.strip()

if __name__ == "__main__":
    parser = OptionParser()
  
    parser.add_option("-o", "--output", dest="output", help="the root path where code genereted, default is .")
    parser.add_option("-s", "--specfile", dest="spec", help="the path and name of the api spec xml file, default is /etc/cloud/cli/commands.xml")
    
    (options, args) = parser.parse_args()
    
    apiSpecFile = "/etc/cloud/cli/commands.xml"
    if options.spec is not None:
        apiSpecFile = options.spec
    
    if not os.path.exists(apiSpecFile):
        print "the spec file %s does not exists"%apiSpecFile
        print parser.print_help()
        exit(1)
    

    folder = "."
    if options.output is not None:
        folder = options.output
    apiModule=folder + "/cloudstackAPI"
    if not os.path.exists(apiModule):
        try:
            os.mkdir(apiModule)
        except:
            print "Failed to create folder %s, due to %s"%(apiModule,sys.exc_info())
            print parser.print_help()
            exit(2)
    
    cg = codeGenerator(folder, apiSpecFile)
    cg.generateCode()
    
