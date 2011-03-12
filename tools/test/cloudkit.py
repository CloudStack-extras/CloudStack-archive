import cookielib
import hashlib
import json
import os
import random
import sys
import urllib2
import urllib


class ApiSession:
    """an ApiSession represents one api session, with cookies."""
    def __init__(self, url, username, password):
        self._username = username
        self._password = hashlib.md5(password).hexdigest()
        self._url = url
        self._cj = cookielib.CookieJar()
        self._opener = urllib2.build_opener(urllib2.HTTPCookieProcessor(self._cj))

    def _get(self, parameters):
        encoded = urllib.urlencode(parameters)
        url = "%s?%s"% (self._url, encoded)
        try:
           f = self._opener.open(url)
           return f.read()
        except urllib2.HTTPError as exn:
            print "Command %s failed" % parameters['command']
            print "Reason: %s" % json.loads(exn.read())

    def GET(self, parameters):
        parameters['sessionkey'] = self._sessionkey
        parameters['response'] = 'json'
        return self._get(parameters)
        
    def _post(self,  parameters):
        return self._opener.open(self._url, urllib.urlencode(parameters)).read()

    def POST(self, parameters):
        parameters['sessionkey'] = self._sessionkey
        parameters['response'] = 'json'
        return self._post(parameters)

    def login(self):
        params = {'command':'login', 'response': 'json'}
        params['username'] = self._username
        params['password'] = self._password
        result = self._get(params)
        jsonresult = json.loads(result)
        jsessionid = None
        self._sessionkey = jsonresult['loginresponse']['sessionkey']

class ZoneCreator:
    """Creates a zone (and a pod and cluster for now)"""
    def __init__(self, api, zonenum, dns1='192.168.10.254', 
                 dns2='192.168.10.253', internaldns='192.168.10.254'):
        self._api = api
        self._zonenum = zonenum
        self._zonename = "ZONE%04d"%zonenum
        self._dns1 = dns1
        self._dns2 = dns2
        self._internaldns = internaldns
    
    def create(self):
        jsonresult = self._api.GET({'command': 'createZone', 'networktype':'Basic', 
                      'name':self._zonename, 'dns1':self._dns1, 'dns2':self._dns2,
                      'internaldns1':self._internaldns})
        if  jsonresult is  None:
           print "Failed to create zone"
           return 1

        jsonobj = json.loads(jsonresult)
        self._zoneid = jsonobj['createzoneresponse']['zone']['id']
        print "Zone %s is created"%self._zonename
        print "zone=%s"%self._zoneid
        return self.createPod()


    def createPod(self):
        self._podname = "POD%04d"%self._zonenum
        self._clustername = "CLUSTER%04d"%self._zonenum
        jsonresult = api.GET({'command': 'createPod', 'zoneId':self._zoneid,
                          'name':self._podname, 'gateway':'192.168.1.1', 'netmask':'255.255.255.0',
                          'startIp':'192.168.1.100', 'endIp':'192.168.1.150'})
        if  jsonresult is  None:
           print "Failed to create pod"
           return 2
        jsonobj = json.loads(jsonresult)
        podid = jsonobj['createpodresponse']['pod']['id']
        jsonresult = api.GET({'command': 'addCluster', 'zoneId':self._zoneid,
                              'clustername':self._clustername, 'podId':podid, 'hypervisor':'KVM',
                              'clustertype':'CloudManaged'})
        if  jsonresult is  None:
           print "Failed to create cluster"
           return 3
        jsonobj = json.loads(jsonresult)
        clusterid = jsonobj['addclusterresponse']['cluster'][0]['id']
        print "pod=%s"%podid
        print "cluster=%s"%clusterid
        
    
if __name__ == "__main__":
    api = ApiSession('http://localhost:8080/client/api', 'admin', 'password')
    api.login()
    zonecreator = ZoneCreator(api, random.randint(2,1000))
    zonecreator.create()
