#!/bin/bash

#set -x
usage() {
  printf "Usage:\n"
  printf "Associate ip   : %s -a -p <publicip> -g <guestip> \n" $(basename $0)
  printf "Disassociate ip: %s -d -p <public ip>\n"  $(basename $0)
}



elastic_ip_entry() {
  local publicIp=$1
  local instIp=$2  
  local op=$3
  logger -t cloud "$(basename $0): create elastic ip static nat: public ip=$publicIp \
  instance ip=$instIp  op=$op"

  #if adding, this might be a duplicate, so delete the old one first
  [ "$op" == "-A" ] && elastic_ip_entry $publicIp $instIp "-D" 
  # the delete operation may have errored out but the only possible reason is 
  # that the rules didn't exist in the first place

  local dev="eth2" #FIXME
  [ $? -ne 0 ] && logger -t cloud "Could not find device associated with $publicIp" && return 1

  # shortcircuit the process if error and it is an append operation
  # continue if it is delete
  # the last 2 rules for packets entering eth0 are to ensure hairpin NAT
  (sudo iptables -t nat $op  PREROUTING -i $dev -d $publicIp  -j DNAT \
           --to-destination $instIp &>>  $OUTFILE || [ "$op" == "-D" ]) &&
  (sudo iptables $op FORWARD -i $dev -o eth0 -d $instIp  \
           -m state --state NEW -j ACCEPT &>>  $OUTFILE )
  (sudo iptables -t nat $op  POSTROUTING -o $dev -s $instIp  -j SNAT \
           --to-source $publicIp &>>  $OUTFILE || [ "$op" == "-D" ]) &&
  (sudo iptables -t nat $op  PREROUTING -i eth0 -d $publicIp  -j DNAT \
           --to-destination $instIp &>>  $OUTFILE || [ "$op" == "-D" ]) &&
  (sudo iptables -t nat $op  POSTROUTING -o eth0 -s $instIp  -j SNAT \
           --to-source $publicIp &>>  $OUTFILE || [ "$op" == "-D" ]) 

  result=$?
  logger -t cloud "$(basename $0): done elastic ip entry public ip=$publicIp op=$op result=$result"
  return $result
}

add_an_ip () {
  local pubIp=$1
  local ethDev="eth2" #FIXME
  logger -t cloud "$(basename $0):Adding ip $pubIp on interface $ethDev"
  local ipNoMask=$(echo $1 | awk -F'/' '{print $1}')

  sudo ip link set $ethDev up
  sudo ip addr add dev $ethDev $pubIp ;
  sudo arping -c 3 -I $ethDev -A -U -s $ipNoMask $ipNoMask;
  return $?
   
}

remove_an_ip () {
  local pubIp=$1
  local ethDev="eth2" #FIXME
  logger -t cloud "$(basename $0):Removing ip $pubIp on interface $ethDev"
  local ipNoMask=$(echo $pubIp | awk -F'/' '{print $1}')
  local mask=$(echo $pubIp | awk -F'/' '{print $2}')
  local existingIpMask=$(sudo ip addr show dev $ethDev | grep inet | awk '{print $2}'  | grep -w $ipNoMask)
  [ "$existingIpMask" == "" ] && return 0
  local existingMask=$(echo $existingIpMask | awk -F'/' '{print $2}')
  sudo ip addr del dev $ethDev $existingIpMask
  result=$?
  if [ $result -gt 0  -a $result -ne 2 ]
  then
     return 1
  fi
  return 0
}

pflag=
gflag=
associate=
disassociate=

while getopts 'adp:g:' OPTION
do
  case $OPTION in
  a)	associate=1
		;;
  d)	disassociate=1
		;;
  g)	gflag=1
		instIp="$OPTARG"
		;;
  p)	pflag=1
		publicIp="$OPTARG"
		;;
  ?)	usage
		exit 2
		;;
  esac
done

[ "$associate$disassociate" == "11" ] && usage && exit 2
[ "$gflag$pflag" != "11" ] && usage && exit 2
OUTFILE=$(mktemp)



if [ "$associate" == "1" ]; then
    add_an_ip $publicIp
    sudo iptables -D FORWARD -i eth0 -o eth0 -j ACCEPT
    sudo iptables -I FORWARD -i eth0 -o eth0 -j ACCEPT
    elastic_ip_entry $publicIp $instIp "-A"
    [ "$result" -ne 0 ] && cat $OUTFILE >&2
    rm -f $OUTFILE
    exit $?
fi

if [ "$disassociate" == "1" ]; then
    elastic_ip_entry $publicIp $instIp "-D"
    remove_an_ip $publicIp
    [ "$result" -ne 0 ] && cat $OUTFILE >&2
    rm -f $OUTFILE
    exit $?
fi
