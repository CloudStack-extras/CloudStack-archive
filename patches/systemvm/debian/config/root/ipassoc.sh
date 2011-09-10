#!/usr/bin/env bash



  #
  # Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
  # 
  # This software is licensed under the GNU General Public License v3 or later.
  # 
  # It is free software: you can redistribute it and/or modify
  # it under the terms of the GNU General Public License as published by
  # the Free Software Foundation, either version 3 of the License, or any later version.
  # This program is distributed in the hope that it will be useful,
  # but WITHOUT ANY WARRANTY; without even the implied warranty of
  # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  # GNU General Public License for more details.
  # 
  # You should have received a copy of the GNU General Public License
  # along with this program.  If not, see <http://www.gnu.org/licenses/>.
  #
 

# $Id: ipassoc.sh 9804 2010-06-22 18:36:49Z alex $ $HeadURL: svn://svn.lab.vmops.com/repos/vmdev/java/scripts/network/domr/ipassoc.sh $
# ipassoc.sh -- associate/disassociate a public ip with an instance
#
#
# @VERSION@

lock="biglock"
locked=$(getLockFile $lock)
if [ "$locked" != "1" ]
then
    exit 1
fi

usage() {
  printf "Usage:\n %s -A    -l <public-ip-address>   -c <dev> [-f] \n" $(basename $0) >&2
  printf " %s -D  -l <public-ip-address>  -c <dev> [-f] \n" $(basename $0) >&2
}

add_fw_chain_for_ip () {
  local pubIp=$(echo $1 | awk -F'/' '{print $1}')
  if sudo iptables -t mangle -N FIREWALL_$pubIp &> /dev/null
  then
    logger -t cloud "$(basename $0): created firewall chain for $pubIp"
    #drop if no rules match (this will be the last rule in the chain)
    sudo iptables -t mangle -A FIREWALL_$pubIp -j DROP> /dev/null
    #ensure outgoing connections are maintained (first rule in chain)
    sudo iptables -t mangle -I FIREWALL_$pubIp -m state --state RELATED,ESTABLISHED -j ACCEPT> /dev/null
    #ensure that this table is after VPN chain
    sudo iptables -t mangle -I PREROUTING 2 -d $pubIp -j FIREWALL_$pubIp
    return $?
  fi
  logger -t cloud "$(basename $0): firewall chain for $pubIp already exists"
}

add_vpn_chain_for_ip () {
  local pubIp=$(echo $1 | awk -F'/' '{print $1}')
  if sudo iptables -t mangle -N VPN_$pubIp &> /dev/null
  then
    logger -t cloud "$(basename $0): created VPN chain for $pubIp"
    #ensure outgoing connections are maintained (first rule in chain)
    sudo iptables -t mangle -I VPN_$pubIp -m state --state RELATED,ESTABLISHED -j ACCEPT
    sudo iptables -t mangle -A VPN_$pubIp -j RETURN
    #ensure that this table is the first
    sudo iptables -t mangle -I PREROUTING 1 -d $pubIp -j VPN_$pubIp
    return $?
  fi
  logger -t cloud "$(basename $0): VPN chain for $pubIp already exists"
}

del_fw_chain_for_ip () {
  local pubIp=$(echo $1 | awk -F'/' '{print $1}')
  if ! sudo iptables -t mangle -N FIREWALL_$pubIp &> /dev/null
  then
    logger -t cloud "$(basename $0): destroying firewall chain for $pubIp"
    sudo iptables -t mangle -D PREROUTING  -d $pubIp -j FIREWALL_$pubIp
    sudo iptables -t mangle -F FIREWALL_$pubIp
    sudo iptables -t mangle -X FIREWALL_$pubIp 
    return $?
  fi
  # firewall chain got created as a result of testing for the chain, cleanup
  sudo iptables -t mangle -F FIREWALL_$pubIp
  sudo iptables -t mangle -X FIREWALL_$pubIp
  logger -t cloud "$(basename $0): firewall chain did not exist for $pubIp, cleaned up"
}

del_vpn_chain_for_ip () {
  local pubIp=$(echo $1 | awk -F'/' '{print $1}')
  if ! sudo iptables -t mangle -N VPN_$pubIp &> /dev/null
  then
    logger -t cloud "$(basename $0): destroying vpn chain for $pubIp"
    sudo iptables -t mangle -D PREROUTING  -d $pubIp -j VPN_$pubIp
    sudo iptables -t mangle -F VPN_$pubIp
    sudo iptables -t mangle -X VPN_$pubIp 
    return $?
  fi
  # vpn chain got created as a result of testing for the chain, cleanup
  sudo iptables -t mangle -F VPN_$pubIp
  sudo iptables -t mangle -X VPN_$pubIp
  logger -t cloud "$(basename $0): vpn chain did not exist for $pubIp, cleaned up"
}

add_nat_entry() {
  local pubIp=$1
  logger -t cloud "$(basename $0):Adding nat entry for ip $pubIp on interface $ethDev"
  local ipNoMask=$(echo $1 | awk -F'/' '{print $1}')
  sudo ip link show $ethDev | grep "state DOWN" > /dev/null
  local old_state=$?
  sudo ip addr add dev $ethDev $pubIp
  sudo iptables -D FORWARD -i $ethDev -o eth0 -m state --state RELATED,ESTABLISHED -j ACCEPT
  sudo iptables -D FORWARD -i eth0 -o $ethDev  -j ACCEPT
  sudo iptables -t nat -D POSTROUTING   -j SNAT -o $ethDev --to-source $ipNoMask ;
  sudo iptables -A FORWARD -i $ethDev -o eth0 -m state --state RELATED,ESTABLISHED -j ACCEPT
  sudo iptables -A FORWARD -i eth0 -o $ethDev  -j ACCEPT
  sudo iptables -t nat -I POSTROUTING   -j SNAT -o $ethDev --to-source $ipNoMask ;
  if [ $? -gt 0  -a $? -ne 2 ]
  then
     logger -t cloud "$(basename $0):Failed adding nat entry for ip $pubIp on interface $ethDev"
     return 1
  fi
  logger -t cloud "$(basename $0):Added nat entry for ip $pubIp on interface $ethDev"
  if [ $if_keep_state -ne 1 -o $old_state -ne 0 ]
  then
      sudo ip link set $ethDev up
      sudo arping -c 3 -I $ethDev -A -U -s $ipNoMask $ipNoMask;
  fi

  return 0
}

del_nat_entry() {
  local pubIp=$1
  logger -t cloud "$(basename $0):Deleting nat entry for ip $pubIp on interface $ethDev"
  local ipNoMask=$(echo $1 | awk -F'/' '{print $1}')
  local mask=$(echo $1 | awk -F'/' '{print $2}')
  [ "$mask" == "" ] && mask="32"
  sudo iptables -D FORWARD -i $ethDev -o eth0 -m state --state RELATED,ESTABLISHED -j ACCEPT
  sudo iptables -D FORWARD -i eth0 -o $ethDev  -j ACCEPT
  sudo iptables -t nat -D POSTROUTING   -j SNAT -o $ethDev --to-source $ipNoMask;
  sudo ip addr del dev $ethDev "$ipNoMask/$mask"
 
  if [ $? -gt 0  -a $? -ne 2 ]
  then
     return 1
  fi

  return $?
}


add_an_ip () {
  local pubIp=$1
  logger -t cloud "$(basename $0):Adding ip $pubIp on interface $ethDev"
  local ipNoMask=$(echo $1 | awk -F'/' '{print $1}')
  sudo ip link show $ethDev | grep "state DOWN" > /dev/null
  local old_state=$?

  sudo ip addr add dev $ethDev $pubIp ;

  if [ $if_keep_state -ne 1 -o $old_state -ne 0 ]
  then
      sudo ip link set $ethDev up
      sudo arping -c 3 -I $ethDev -A -U -s $ipNoMask $ipNoMask;
  fi

  return $?
   
}

remove_an_ip () {
  local pubIp=$1
  logger -t cloud "$(basename $0):Removing ip $pubIp on interface $ethDev"
  local ipNoMask=$(echo $1 | awk -F'/' '{print $1}')
  local mask=$(echo $1 | awk -F'/' '{print $2}')
  local existingIpMask=$(sudo ip addr show dev $ethDev | grep inet | awk '{print $2}'  | grep -w $ipNoMask)
  [ "$existingIpMask" == "" ] && return 0
  local existingMask=$(echo $existingIpMask | awk -F'/' '{print $2}')
  if [ "$existingMask" == "32" ] 
  then
    sudo ip addr del dev $ethDev $existingIpMask
    result=$?
  fi
  if [ "$existingMask" != "32" ] 
  then
        replaceIpMask=`sudo ip addr show dev $ethDev | grep inet | grep -v $existingIpMask | awk '{print $2}' | sort -t/ -k2 -n|tail -1`
        sudo ip addr del dev $ethDev $existingIpMask;
        if [ -n "$replaceIpMask" ]; then
          sudo ip addr del dev $ethDev $replaceIpMask;
          replaceIp=`echo $replaceIpMask | awk -F/ '{print $1}'`;
          sudo ip addr add dev $ethDev $replaceIp/$existingMask;
          sudo iptables -t nat -D POSTROUTING   -j SNAT -o $ethDev --to-source $ipNoMask ;
          sudo iptables -t nat -A POSTROUTING   -j SNAT -o $ethDev --to-source $replaceIp ;
        fi
    result=$?
  fi
  if [ $result -gt 0  -a $result -ne 2 ]
  then
     return 1
  fi
  return 0
}

#set -x

lflag=
fflag=
cflag=
op=""

is_master=0
is_redundant=0
if_keep_state=0
sudo ls /root/keepalived.log > /dev/null 2>&1
if [ $? -eq 0 ]
then
    is_redundant=1
    sudo /root/checkrouter.sh|grep "Status: MASTER" > /dev/null 2>&1 
    if [ $? -eq 0 ]
    then
        is_master=1
    fi
fi
if [ $is_redundant -eq 1 -a $is_master -ne 1 ]
then
    if_keep_state=1
fi

while getopts 'fADa:l:c:' OPTION
do
  case $OPTION in
  A)	Aflag=1
		op="-A"
		;;
  D)	Dflag=1
		op="-D"
		;;
  f)	fflag=1
		;;
  l)	lflag=1
		publicIp="$OPTARG"
		;;
  c)	cflag=1
  		ethDev="$OPTARG"
  		;;
  ?)	usage
                unlock_exit 2 $lock $locked
		;;
  esac
done


#Either the A flag or the D flag but not both
if [ "$Aflag$Dflag" != "1" ]
then
    usage
    unlock_exit 2 $lock $locked
fi

if [ "$lflag$cflag" != "11" ] 
then
    usage
    unlock_exit 2 $lock $locked
fi


if [ "$fflag" == "1" ] && [ "$Aflag" == "1" ]
then
  add_nat_entry  $publicIp  &&
  add_vpn_chain_for_ip $publicIp &&
  add_fw_chain_for_ip $publicIp 
  unlock_exit $? $lock $locked
fi

if [ "$Aflag" == "1" ]
then  
  add_an_ip  $publicIp  &&
  add_fw_chain_for_ip $publicIp 
  unlock_exit $? $lock $locked
fi

if [ "$fflag" == "1" ] && [ "$Dflag" == "1" ]
then
  del_nat_entry  $publicIp &&
  del_fw_chain_for_ip $publicIp &&
  del_vpn_chain_for_ip $publicIp
  unlock_exit $? $lock $locked
fi

if [ "$Dflag" == "1" ]
then
  remove_an_ip  $publicIp &&
  del_fw_chain_for_ip $publicIp 
  unlock_exit $? $lock $locked
fi

unlock_exit 0 $lock $locked

