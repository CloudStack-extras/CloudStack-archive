#!/bin/bash

  #
  # Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
  #

############################################################
#
# zucchini uses local storage, before setting up make sure
#     * xen.public.network.device is set
#     * use.local.storage and systemvm.use.local.storage are true
#     * optionally turn off stats collectors
#     * expunge.delay and expunge.interval are 60s
#     * ping.interval is around 3m
#     * turn off dns updates to entire zone, network.dns.basiczone.update=pod
#     * capacity.skipcounting.hours=0
#     * direct.agent.load.size=1000

#
#   This script will only setup an approximate number of hosts. To achieve the ratio
#   of 5:2:6 hosts the total number of hosts is brought close to a number divisible
#   by 13. So if 4000 hosts are added, you might see only 3900 come up
#   
#   10 hosts per pod @ 1 host per cluster in a single zone
#   
#   Each pod has a /25, so 128 addresses. I think we reserved 5 IP addresses for system VMs in each pod. 
#   Then we had something like 60 addresses for hosts and 60 addresses for VMs.
   
#Environment
#1. Approximately 10 hosts per pod. 
#2. Only 3 host tags. 
#3. With in each pod, the host tags are the same. Homogenous Pods
#4. The ratio of hosts for the three tags should be 5/2/6
#5. In simulator.properties, workers=1
############################################################

usage() {
  printf "Setup Zucchini Like Environment\nUsage: %s: -h management-server -z zoneid [-d delay] -n numberofhosts\n" $(basename $0) >&2
}

a=1 #CIDR - 16bytes
b=2 #CIDR - 8 bytes

#options
hflag=1
zflag=
dflag=1
nflag=1

host="127.0.0.1" #default localhost
zoneid=
delay=300 #default 5 minutes
numberofhosts=1300 #default 1300 hosts
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

if [ $zflag$nflag != "11" ]
then
 usage
 exit 2
fi

numberofpods=$(($numberofhosts/10)) #10 hosts per pod
tag_one_range=$(($numberofpods*5/13))
tag_two_range=$(($numberofpods*2/13))
tag_three_range=$(($numberofpods-$tag_one_range-$tag_two_range))

clusters_per_pod=10 #each cluster has one host
hosts_per_pod=10


declare -a pod_array
declare -a cluster_array

echo "Split Ratio :: " $tag_one_range":"$tag_two_range":"$tag_three_range

#create the zone
zone_query="GET  http://$host/client/?command=createZone&networktype=Basic&securitygroupenabled=false&name=Zucchini&dns1=4.2.2.2&internaldns1=4.2.2.2  HTTP/1.0\n\n"
echo -e $zone_query | nc -v -w $delay $host 8096

#Add Secondary Storage
sstor_query="GET  http://$host/client/?command=addSecondaryStorage&zoneid=$zoneid&url=nfs://172.16.15.32/export/share/secondary  HTTP/1.0\n\n"
echo -e $sstor_query | nc -v -w $delay $host 8096

let x=a
let y=b

echo "[DEBUG] $(date) Starting Creation of $numberofpods Pods"
for ((name=1;name<=$numberofpods;name++))
do
    echo "[DEBUG] $(date) Creating pod[POD$name]"
   	pod_query="GET  http://$host/client/?command=createPod&zoneId=$zoneid&name=POD$name&netmask=255.255.255.128&startIp=172.$x.$y.130&endIp=172.$x.$y.189&gateway=172.$x.$y.129	HTTP/1.0\n\n"
    pod_out=$(echo -e $pod_query | nc -v -w $delay $host 8096)	
    pod_id=$(echo $pod_out | sed 's/\(.*<id>\)\([0-9]*\)\(.*\)/\2/g')
    if ! [[ "$pod_id" =~ ^[0-9]+$ ]] ; then
       exec >&2; echo "[ERROR] $(date) pod [POD$name] creation failed"; continue
    fi
    echo "[DEBUG] $(date) Created pod["$pod_id":POD"$name"]"
    pod_array[$name]=$pod_id
    
    echo "[DEBUG] $(date) Creating vlan for pod[POD$name]"
    vlan_query="GET http://$host/client/?command=createVlanIpRange&vlan=untagged&zoneid=$zoneid&podId=$pod_id&forVirtualNetwork=false&gateway=172.$x.$y.129&netmask=255.255.255.128&startip=172.$x.$y.190&endip=172.$x.$y.249        HTTP/1.0\n\n"
   	vlan_out=$(echo -e $vlan_query | nc -v -w $delay $host 8096)
    vlan_id=$(echo $vlan_out | sed 's/\(.*<id>\)\([0-9]*\)\(.*\)/\2/g')
    if ! [[ "$vlan_id" =~ ^[0-9]+$ ]] ; then           
       let y+=1
       if [ "$y" -eq 256 ]
       then
           let x+=1
           y=1
       fi
       exec >&2; echo "[ERROR] $(date) vlan creation for pod[POD$name] failed"; continue
    fi
    echo "[DEBUG] $(date) Created vlan for pod[POD$name]"    
        
	#add clusters
	echo "[DEBUG] $(date) Starting Creation of $clusters_per_pod clusters for pod[POD$name]"
	for ((cluster=1;cluster<=$clusters_per_pod;cluster++))
	do
	    echo "[DEBUG] $(date) Creating cluster[POD$name-CLUSTER$cluster] for pod[POD$name]"
		cluster_query="GET  http://$host/client/?command=addCluster&hypervisor=Simulator&clustertype=CloudManaged&zoneId=$zoneid&podId=$pod_id&clustername=POD$name-CLUSTER$cluster HTTP/1.0\n\n"
        cluster_out=$(echo -e $cluster_query | nc -v -w $delay $host 8096)
        cluster_id=$(echo $cluster_out | sed 's/\(.*<id>\)\([0-9]*\)\(.*\)/\2/g')
        if ! [[ "$cluster_id" =~ ^[0-9]+$ ]] ; then
           exec >&2; echo "[ERROR] $(date) cluster[POD$name-CLUSTER$cluster] creation for pod[POD$name] failed"; continue
        fi          
        echo "[DEBUG] $(date) Created cluster["$cluster_id":POD"$name"-CLUSTER"$cluster"]"
        cluster_array[$(($name*$clusters_per_pod + $cluster))]=$cluster_id
	done
	echo "[DEBUG] $(date) Finished Creating clusters for pod[POD$name]"
    let y+=1
    if [ "$y" -eq 256 ]
    then
        let x+=1
        y=1
    fi	
done
echo "[DEBUG] $(date) Finished Creating $numberofpods Pods"

#echo "DEBUG:Pods and Clusters"
#echo "PODS:("${#pod_array[@]}")" ${pod_array[@]}
#echo "CLUSTERS:("${#cluster_array[@]}")" ${cluster_array[@]}
echo
echo

#Add hosts
#TAG1
for ((i=1;i<=$tag_one_range;i++))
do
    podid=${pod_array[$i]}
    for ((j=1;j<=$hosts_per_pod;j++))
	do
	    clusterid=${cluster_array[$(($i*$clusters_per_pod + $j))]}
	    host_query="GET	http://$host/client/?command=addHost&zoneId=$zoneid&podId=$podid&username=sim&password=sim&clusterid=$clusterid&url=http%3A%2F%2Fsim&hypervisor=Simulator&clustertype=CloudManaged&hosttags=$tag1	HTTP/1.0\n\n"
		host_out=$(echo -e $host_query | nc -v -w $delay $host 8096)
		host_id=$(echo $host_out | sed 's/\(.*<id>\)\([0-9]*\)\(.*\)/\2/g')
		if ! [[ "$host_id" =~ ^[0-9]+$ ]] ; then
           exec >&2; echo "[ERROR] $(date) host addition failed in [pod:$podid,cluster:$clusterid]"; continue
        fi 
		host_name=$(echo $host_out | sed 's/\(.*<name>\)\(SimulatedAgent.[-0-9a-zA-Z]*\)\(.*\)/\2/g')
		echo "[DEBUG] $date added host [$host_id:$host_name] to [pod:$podid,cluster:$clusterid] for TAG1"
	done	
done

#TAG2
for ((i=$(($tag_one_range + 1));i<=$(($tag_one_range + $tag_two_range));i++))
do
    podid=${pod_array[$i]}
    for ((j=1;j<=$hosts_per_pod;j++))
	do
	    clusterid=${cluster_array[$(($i*$clusters_per_pod + $j))]}
	    host_query="GET	http://$host/client/?command=addHost&zoneId=$zoneid&podId=$podid&username=sim&password=sim&clusterid=$clusterid&url=http%3A%2F%2Fsim&hypervisor=Simulator&clustertype=CloudManaged&hosttags=$tag2	HTTP/1.0\n\n"
		host_out=$(echo -e $host_query | nc -v -w $delay $host 8096)
		host_id=$(echo $host_out | sed 's/\(.*<id>\)\([0-9]*\)\(.*\)/\2/g')
		if ! [[ "$host_id" =~ ^[0-9]+$ ]] ; then
           exec >&2; echo "[ERROR] $(date) host addition failed in [pod:$podid,cluster:$clusterid]"; continue
        fi 
		host_name=$(echo $host_out | sed 's/\(.*<name>\)\(SimulatedAgent.[-0-9a-zA-Z]*\)\(.*\)/\2/g')
		echo "[DEBUG] $date added host [$host_id:$host_name] to [pod:$podid,cluster:$clusterid] for TAG2"		
	done
done

#TAG3
for ((i=$(($tag_two_range + $tag_one_range + 1));i<=$(($tag_three_range + $tag_two_range + $tag_one_range));i++))
do
    podid=${pod_array[$i]}
    for ((j=1;j<=$hosts_per_pod;j++))
	do
	    clusterid=${cluster_array[$(($i*$clusters_per_pod + $j))]}
	    host_query="GET	http://$host/client/?command=addHost&zoneId=$zoneid&podId=$podid&username=sim&password=sim&clusterid=$clusterid&url=http%3A%2F%2Fsim&hypervisor=Simulator&clustertype=CloudManaged&hosttags=$tag3	HTTP/1.0\n\n"
		host_out=$(echo -e $host_query | nc -v -w $delay $host 8096)
		host_id=$(echo $host_out | sed 's/\(.*<id>\)\([0-9]*\)\(.*\)/\2/g')
		if ! [[ "$host_id" =~ ^[0-9]+$ ]] ; then
           exec >&2; echo "[ERROR] $(date) host addition failed in [pod:$podid,cluster:$clusterid]"; continue
        fi 
		host_name=$(echo $host_out | sed 's/\(.*<name>\)\(SimulatedAgent.[-0-9a-zA-Z]*\)\(.*\)/\2/g')
		echo "[DEBUG] $date added host [$host_id:$host_name] to [pod:$podid,cluster:$clusterid] for TAG3"		
	done	
done

echo "Setup complete"
exit 0
