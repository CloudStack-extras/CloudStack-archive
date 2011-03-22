from apisession import ApiSession
from physicalresource import ZoneCreator
import random
    
if __name__ == "__main__":
    api = ApiSession('http://localhost:8080/client/api', 'admin', 'password')
    api.login()
    zonecreator = ZoneCreator(api, random.randint(2,1000))
    zonecreator.create()
