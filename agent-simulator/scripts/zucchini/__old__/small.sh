#!/bin/bash

  #
  # Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
  #

############################################################
#
# zucchini uses local storage, before setting up make sure
#     * xen.public.network.device is set
#     * use.local.storage and systemvm.use.local.storage are true
#     * optionally turn off stats collector
#     * expunge.delay and expunge.interval are 60s
#     * capacity.skipcounting.hours=0

#Environment
#1. Small setup for zucchini : 13, 26, 39, 52 hosts only
#2. Only 3 host tags. 
#3. With in each pod, the host tags are the same.
#4. The ratio of hosts for the three tags should be 5/2/6
#5. simulator.properties, workers=1
############################################################

usage() {
  printf "Setup Zucchini Like Environment\nUsage: %s: -h management-server -z zoneid [-d delay] -n numberofhosts\n" $(basename $0) >&2
}

a=1 #CIDR - 16bytes
b=2 #CIDR - 8 bytes

#options
hflag=1
zflag=1
dflag=1
nflag=1

host="127.0.0.1" #default localhost
zoneid=
delay=300 #default 5 minutes
numberofhosts=13 #default 13 hosts
tag1="TAG1"
tag2="TAG2"
tag3="TAG3"

while getopts 'h:z:d:n:' OPTION
do
 case $OPTION in
  h)	hflag=1
        host="$OPTARG"
        ;;
  z)    zflag=1
        zoneid="$OPTARG"
        ;;    
  d)    dflag=1
        delay="$OPTARG"
        ;;
  n)    nflag=1
        numberofhosts="$OPTARG"
        ;;
  ?)	usage
		exit 2
		;;
  esac
done

if [ $hflag$zflag$dflag$nflag != "1111" ]
then
 usage
 exit 2
fi

if [ $numberofhosts -gt 52 ]
then
	echo "Can't exceed 52 hosts"
	exit 130
fi

numberofpods=3 #130 hosts per pod. So need only 3. each pod is homogenous
tag_one_range=$(($numberofhosts*5/13))
tag_two_range=$(($numberofhosts*2/13))
tag_three_range=$(($numberofhosts-$tag_one_range-$tag_two_range))

#create the zone
zone_query="GET  http://$host/client/?command=createZone&networktype=Basic&securitygroupenabled=true&name=Zucchini&dns1=4.2.2.2&internaldns1=4.2.2.2  HTTP/1.0\n\n"
echo -e $zone_query | nc -v -w $delay $host 8096

sstor_query="GET  http://$host/client/?command=addSecondaryStorage&zoneid=$zoneid&url=nfs://172.16.15.32/export/share/secondary  HTTP/1.0\n\n"
echo -e $sstor_query | nc -v -w $delay $host 8096

let x=a
let y=b
for name in `seq 1 $numberofpods`
do
   	pod_query="GET  http://$host/client/?command=createPod&zoneId=$zoneid&name=POD$name&netmask=255.255.255.0&startIp=172.$x.$y.2&endIp=172.$x.$y.131&gateway=172.$x.$y.1	HTTP/1.0\n\n"
	vlan_query="GET http://$host/client/?command=createVlanIpRange&vlan=untagged&zoneid=$zoneid&podId=$name&forVirtualNetwork=false&gateway=172.$y.$x.1&netmask=255.255.255.0&startip=172.$y.$x.2&endip=172.$y.$x.131        HTTP/1.0\n\n"
	echo -e $pod_query | nc -v -w $delay $host 8096
	echo -e $vlan_query | nc -v -w $delay $host 8096

	let x+=1
	let y+=1
done

#add clusters - 
#for i in `seq 1 10`
#	cluster_query="GET  http://$host/client/?command=addCluster&hypervisor=Simulator&clustertype=CloudManaged&zoneId=$zoneid&podId=$podid&clustername=POD$podid-CLUSTER$cluster HTTP/1.0\n\n"
#       	echo -e $cluster_query | nc -v -w $delay $host 8096
#do

clusterid=1
#TAG1
for cluster in `seq 1 $tag_one_range`
do
		cluster_query="GET  http://$host/client/?command=addCluster&hypervisor=Simulator&clustertype=CloudManaged&zoneId=$zoneid&podId=1&clustername=POD1-CLUSTER$cluster HTTP/1.0\n\n"
       		echo -e $cluster_query | nc -v -w $delay $host 8096
	    host_query="GET	http://$host/client/?command=addHost&zoneId=$zoneid&podId=1&username=sim&password=sim&clusterid=$clusterid&url=http%3A%2F%2Fsim&hypervisor=Simulator&clustertype=CloudManaged&hosttags=$tag1	HTTP/1.0\n\n"
		echo -e $host_query | nc -v -w $delay $host 8096
		let clusterid+=1
done

#TAG2
for cluster in `seq $(($tag_one_range + 1)) $(($tag_one_range + $tag_two_range))`
do
		cluster_query="GET  http://$host/client/?command=addCluster&hypervisor=Simulator&clustertype=CloudManaged&zoneId=$zoneid&podId=2&clustername=POD2-CLUSTER$cluster HTTP/1.0\n\n"
       		echo -e $cluster_query | nc -v -w $delay $host 8096
	    host_query="GET	http://$host/client/?command=addHost&zoneId=$zoneid&podId=2&username=sim&password=sim&clusterid=$clusterid&url=http%3A%2F%2Fsim&hypervisor=Simulator&clustertype=CloudManaged&hosttags=$tag2	HTTP/1.0\n\n"
		echo -e $host_query | nc -v -w $delay $host 8096
		let clusterid+=1
done

#TAG3
for cluster in `seq $(($tag_two_range + $tag_one_range + 1)) $(($tag_three_range + $tag_two_range + $tag_one_range))`
do
		cluster_query="GET  http://$host/client/?command=addCluster&hypervisor=Simulator&clustertype=CloudManaged&zoneId=$zoneid&podId=3&clustername=POD3-CLUSTER$cluster HTTP/1.0\n\n"
       		echo -e $cluster_query | nc -v -w $delay $host 8096
	    host_query="GET	http://$host/client/?command=addHost&zoneId=$zoneid&podId=3&username=sim&password=sim&clusterid=$clusterid&url=http%3A%2F%2Fsim&hypervisor=Simulator&clustertype=CloudManaged&hosttags=$tag3	HTTP/1.0\n\n"
		echo -e $host_query | nc -v -w $delay $host 8096
		let clusterid+=1

done

for i in {1..60}
do
    sg_create_query="GET    http://$host/client/?command=createSecurityGroup&name=TestGroup1&description=Test   HTTP/1.0\n\n"
    echo -e $sg_create_query | nc -v -w $delay $host 8096

    sg_auth_query="GET   http://$host/client/?command=authorizeSecurityGroupIngress&domainid=1&account=admin&securitygroupid=$((i+2))&protocol=TCP&startport=2002&endport=2002&usersecuritygrouplist[0].account=admin&usersecuritygrouplist[0].group=TestGroup1 HTTP/1.0\n\n"
    echo -e $sg_create_query | nc -v -w $delay $host 8096
done

echo "Setup complete"
exit 0
