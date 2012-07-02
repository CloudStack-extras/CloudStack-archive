'''
Created on Jul 2, 2012

@author: frank
'''
import sys
import os
import os.path
import base64

HTML_ROOT = "/var/www/html/"

def addUserData(vmIp, folder, fileName, contents):
        
    baseFolder = os.path.join(HTML_ROOT, folder, vmIp)
    if not os.path.exists(baseFolder):
        os.makedirs(baseFolder)
    datafileName = os.path.join(HTML_ROOT, folder, vmIp, fileName)
    metaManifest = os.path.join(HTML_ROOT, folder, vmIp, "meta-data")
    if folder == "userdata":
        if contents != "none":
            contents = base64.urlsafe_b64decode(contents)
        else:
            contents = ""
            
    f = open(datafileName, 'w')
    f.write(contents) 
    f.close()
    
    if folder == "metadata" or folder == "meta-data":
        fm = open(metaManifest, 'a+')
        metas = fm.readlines();
        try:
            metas.remove(fileName)
        except ValueError, e:
            pass
        metas.append(fileName)
        stuff = "\n".join(metas)
        fm.write(stuff)
        fm.close()

if __name__ == '__main__':
    string = sys.argv[1]
    allEntires = string.split(";")
    for entry in allEntires:
        (vmIp, folder, fileName, contents) = entry.split(',', 3)
        addUserData(vmIp, folder, fileName, contents)
    sys.exit(0)    
