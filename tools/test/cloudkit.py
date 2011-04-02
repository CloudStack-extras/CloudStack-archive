from apisession import ApiSession
from physicalresource import ZoneCreator
from globalconfig import GlobalConfig
from db import Database
import random
import uuid

def fix_default_db():
    database = Database()
    statement="""
     UPDATE vm_template SET url='%s'
     WHERE unique_name='%s' """
    database.update(statement % ('http://nfs1.lab.vmops.com/templates/dummy/systemvm.vhd', 'routing-1'))
    database.update(statement % ('http://nfs1.lab.vmops.com/templates/dummy/systemvm.qcow2', 'routing-3'))
    database.update(statement % ('http://nfs1.lab.vmops.com/templates/dummy/systemvm.ova', 'routing-8'))

    database.update(statement % ('http://nfs1.lab.vmops.com/templates/dummy/builtin.vhd', 'centos53-x86_64'))
    database.update(statement % ('http://nfs1.lab.vmops.com/templates/dummy/builtin.qcow2', 'centos55-x86_64'))
    database.update(statement % ('http://nfs1.lab.vmops.com/templates/dummy/builtin.ova', 'centos53-x64'))
    statement="""UPDATE vm_template SET checksum=NULL"""
    database.update(statement)
    
    statement="""UPDATE disk_offering set use_local_storage=1"""
    database.update(statement)
    
def config():
    config = GlobalConfig(api)
    config.update('use.local.storage', 'true')
    config.update('max.template.iso.size', '20')

def create_zone():
    zonecreator = ZoneCreator(api, random.randint(2,1000))
    zoneid = zonecreator.create()
    database = Database()
    statement="""INSERT INTO data_center_details (dc_id, name, value) 
                 VALUES (%s, '%s', '0')"""
    database.update(statement % (zoneid, 'enable.secstorage.vm'))
    database.update(statement % (zoneid, 'enable.consoleproxy.vm'))
    statement="""INSERT INTO data_center_details (dc_id, name, value) 
                 VALUES (%s, '%s', '%s')"""
    database.update(statement % (zoneid, 'zone.dhcp.strategy', 'external'))

    statement="""UPDATE data_center set dhcp_provider='ExternalDhcpServer' where id=%s""" 
    database.update(statement % (zoneid))

if __name__ == "__main__":
    fix_default_db()

    api = ApiSession('http://localhost:8080/client/api', 'admin', 'password')
    api.login()
    
    config()

    create_zone()

    print "guid=%s" % str(uuid.uuid4())


