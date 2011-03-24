from apisession import ApiSession
from physicalresource import ZoneCreator
from globalconfig import GlobalConfig
from db import Database
import random

def fix_default_db():
    database = Database()
    statement="""
     UPDATE vm_template SET url='%s'
     WHERE unique_name='%s' """
    database.update(statement % ('http://nfs1.lab.vmops.com/templates/dummy/systemvm.vhd.bz2', 'routing-1'))
    database.update(statement % ('http://nfs1.lab.vmops.com/templates/dummy/systemvm.qcow2.bz2', 'routing-3'))
    database.update(statement % ('http://nfs1.lab.vmops.com/templates/dummy/systemvm.ova', 'routing-8'))

    database.update(statement % ('http://nfs1.lab.vmops.com/templates/dummy/builtin.vhd.bz2', 'centos53-x86_64'))
    database.update(statement % ('http://nfs1.lab.vmops.com/templates/dummy/builtin.qcow2.bz2', 'centos55-x86_64'))
    database.update(statement % ('http://nfs1.lab.vmops.com/templates/dummy/builtin.ova', 'centos53-x64'))
    statement="""UPDATE vm_template SET checksum=NULL"""
    database.update(statement)
    
def config():
    config = GlobalConfig(api)
    config.update('use.local.storage', 'true')
    config.update('max.template.iso.size', '20')

def create_zone():
    zonecreator = ZoneCreator(api, random.randint(2,1000))
    zonecreator.create()

if __name__ == "__main__":
    fix_default_db()

    api = ApiSession('http://localhost:8080/client/api', 'admin', 'password')
    api.login()
    
    config()

    create_zone()


