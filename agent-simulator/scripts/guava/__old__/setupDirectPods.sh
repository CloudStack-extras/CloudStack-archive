
  #
  # Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
  #
 

x=$1
y=$2
name=$3

pod_query="GET  http://10.91.30.219:8096/client/?command=createPod&zoneId=1&name=SSP$name&cidr=182.$x.$y.0%2F24&startIp=182.$x.$y.2&endIp=182.$x.$y.252&gateway=182.$x.$y.1
HTTP/1.0\n\n"

echo -e $pod_query | nc -v -q 20 10.91.30.219 8096

#vlan_query="GET http://10.91.30.219/client/?command=createVlanIpRange&vlan=untagged&zoneid=1&podId=$name&forVirtualNetwork=false&gateway=172.$y.$x.1&netmask=255.255.255.0&startip=172.$y.$x.2&endip=172.$y.$x.252        HTTP/1.0\n\n"

#echo -e $vlan_query | nc -v -q 20 10.91.30.219 8096
