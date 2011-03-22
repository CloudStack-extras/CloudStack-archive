from apisession import ApiSession
from physicalresource import ZoneCreator
from globalconfig import GlobalConfig
import random
    
if __name__ == "__main__":
    api = ApiSession('http://localhost:8080/client/api', 'admin', 'password')
    api.login()
    config = GlobalConfig(api)
    config.update('use.local.storage', 'true')

    zonecreator = ZoneCreator(api, random.randint(2,1000))
    zonecreator.create()

