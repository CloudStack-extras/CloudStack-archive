#!/bin/bash

a=$1 #CIDR - 16bytes
b=$2 #CIDR - 8 bytes
host=$3
zoneid=$4

zone_query="GET  http://$host/client/?command=createZone&networktype=Advanced&securitygroupenabled=false&name=SimulatorAdvanced&dns1=4.2.2.2&internaldns1=4.2.2.2&vlan=10-4000&guestcidraddress=10.1.1.0%2F24  HTTP/1.0\n\n"
echo -e $zone_query | nc -v -w 120 $host 8096

let x=a
let y=b
for name in `seq 1 1`
do
	pod_query="GET  http://$host/client/?command=createPod&zoneid=$zoneid&name=POD$name&netmask=255.255.0.0&startIp=172.$x.$y.2&endIp=172.$x.$y.252&gateway=172.$x.$y.1	HTTP/1.0\n\n"
	vlan_query="GET http://$host/client/?command=createVlanIpRange&forVirtualNetwork=true&vlan=untagged&zoneid=$zoneid&podId=$name&gateway=172.$y.$x.1&netmask=255.255.255.0&startip=172.$y.$x.2&endip=172.$y.$x.252        HTTP/1.0\n\n"
	echo -e $pod_query | nc -v -w 20 $host 8096
	echo -e $vlan_query | nc -v -w 20 $host 8096
done
let x+=1
let y+=1

name=1

clusterid=1
for cluster in `seq 1 1`
do
	cluster_query="GET  http://$host/client/?command=addCluster&hypervisor=Simulator&clustertype=CloudManaged&zoneid=$zoneid&podId=1&clustername=POD$name-CLUSTER$cluster HTTP/1.0\n\n"
	echo -e $cluster_query | nc -v -w 120 $host 8096

	host_query="GET	http://$host/client/?command=addHost&zoneid=$zoneid&podId=1&username=sim&password=sim&clusterid=$cluster&hypervisor=Simulator&clustertype=CloudManaged&url=http%3A%2F%2Fsim	HTTP/1.0\n\n"
	echo -e $host_query | nc -v -w 6000 $host 8096

	spool_query="GET	http://$host/client/?command=createStoragePool&zoneid=$zoneid&podId=1&clusterid=$cluster&name=SPOOL$cluster&url=nfs://172.1.25.$((cluster+1))/export/share/$cluster   HTTP/1.0\n\n"
	echo -e $spool_query | nc -v -w 6000 $host 8096
	let clusterid+=1
done

sstorquery="GET	http://$host/client/?command=addSecondaryStorage&zoneid=$zoneid&url=nfs://172.1.25.32/export/share/   HTTP/1.0\n\n"
echo -e $sstorquery | nc -v -w 6000 $host 8096
