#!/bin/bash



  #
  # Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
  #
 

#setup for zynga hosts in the simulator

#1. Approximately 10 hosts per pod. 
#2. Only 3 host tags. 
#3. With in each pod, the host tags are the same.
#4. The ratio of hosts for the three tags should be 5/2/6

a=$1 #CIDR - 16bytes
b=$2 #CIDR - 8 bytes

zone_query="GET  http://10.91.30.219/client/?command=createZone&name=Zynga&dns1=4.2.2.2&internaldns1=4.2.2.2&vlan=10-4000&guestcidraddress=10.1.1.0%2F24  HTTP/1.0\n\n"
echo -e $zone_query | nc -v -q 120 10.91.30.219 8096

# Pod Ratio: 38:15:47 @ 10 hosts per pod
let x=a
let y=b
for name in `seq 1 152`
do
	pod_query="GET  http://10.91.30.219:8096/client/?command=createPod&zoneId=1&name=POD$name&cidr=172.$x.$y.0%2F24&startIp=172.$x.$y.2&endIp=172.$x.$y.252&gateway=172.$x.$y.1	HTTP/1.0\n\n"
	vlan_query="GET http://10.91.30.219:8096/client/?command=createVlanIpRange&vlan=untagged&zoneid=1&podId=$name&forVirtualNetwork=false&gateway=172.$y.$x.1&netmask=255.255.255.0&startip=172.$y.$x.2&endip=172.$y.$x.252        HTTP/1.0\n\n"
	echo -e $pod_query | nc -v -q 20 10.91.30.219 8096
	echo -e $vlan_query | nc -v -q 20 10.91.30.219 8096

	for cluster in `seq 1 10`
	do
		host_query="GET	http://10.91.30.219:8096/client/?command=addHost&zoneId=1&podId=$name&username=sim&password=sim&clustername=simulator-POD$name-CLUSTER$cluster&hosttags=TAG1&url=http%3A%2F%2Fsim	HTTP/1.0\n\n"
		echo -e $host_query | nc -v -q 6000 10.91.30.219 8096
	done

	let x+=1
	let y+=1
done

#reset for tag2
let x=a
let y=b
for name in `seq 153 212`
do
	pod_query="GET  http://10.91.30.219:8096/client/?command=createPod&zoneId=1&name=POD$name&cidr=182.$x.$y.0%2F24&startIp=182.$x.$y.2&endIp=182.$x.$y.252&gateway=182.$x.$y.1	HTTP/1.0\n\n"
	vlan_query="GET http://10.91.30.219:8096/client/?command=createVlanIpRange&vlan=untagged&zoneid=1&podId=$name&forVirtualNetwork=false&gateway=182.$y.$x.1&netmask=255.255.255.0&startip=182.$y.$x.2&endip=182.$y.$x.252        HTTP/1.0\n\n"
	echo -e $pod_query | nc -v -q 20 10.91.30.219 8096
	echo -e $vlan_query | nc -v -q 20 10.91.30.219 8096

	for cluster in `seq 1 10`
	do
		host_query="GET	http://10.91.30.219:8096/client/?command=addHost&zoneId=1&podId=$name&username=sim&password=sim&clustername=simulator-POD$name-CLUSTER$cluster&hosttags=TAG2&url=http%3A%2F%2Fsim	HTTP/1.0\n\n"
		echo -e $host_query | nc -v -q 6000 10.91.30.219 8096
	done

	let x+=1
	let y+=1
done

#reset for TAG3
let x=a
let y=b
for name in `seq 213 400`
do
	pod_query="GET  http://10.91.30.219:8096/client/?command=createPod&zoneId=1&name=POD$name&cidr=192.$x.$y.0%2F24&startIp=192.$x.$y.2&endIp=192.$x.$y.252&gateway=192.$x.$y.1	HTTP/1.0\n\n"
	vlan_query="GET http://10.91.30.219:8096/client/?command=createVlanIpRange&vlan=untagged&zoneid=1&podId=$name&forVirtualNetwork=false&gateway=192.$y.$x.1&netmask=255.255.255.0&startip=192.$y.$x.2&endip=192.$y.$x.252        HTTP/1.0\n\n"
	echo -e $pod_query | nc -v -q 20 10.91.30.219 8096
	echo -e $vlan_query | nc -v -q 20 10.91.30.219 8096

	for cluster in `seq 1 10`
	do
		host_query="GET	http://10.91.30.219:8096/client/?command=addHost&zoneId=1&podId=$name&username=sim&password=sim&clustername=simulator-POD$name-CLUSTER$cluster&hosttags=TAG3&url=http%3A%2F%2Fsim	HTTP/1.0\n\n"
		echo -e $host_query | nc -v -q 6000 10.91.30.219 8096
	done

	let x+=1
	let y+=1
done
