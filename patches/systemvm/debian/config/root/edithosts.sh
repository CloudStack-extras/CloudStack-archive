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
 
# edithosts.sh -- edit the dhcphosts file on the routing domain
# $mac : the mac address
# $ip : the associated ip address
# $host : the hostname
# $4 : default router
# $5 : comma separated static routes

mac=$1
ip=$2
host=$3
dflt=$4
routes=$5

DHCP_HOSTS=/etc/dhcphosts.txt
DHCP_OPTS=/etc/dhcpopts.txt
DHCP_LEASES=/var/lib/misc/dnsmasq.leases
HOSTS=/etc/hosts

source /root/func.sh

lock="biglock"
locked=$(getLockFile $lock)
if [ "$locked" != "1" ]
then
    exit 1
fi

lock_rr="rrouter"
locked_rr=$(getLockFile $lock_rr)
if [ "$locked_rr" != "1" ]
then
    exit 1
fi

grep "redundant_router=1" /var/cache/cloud/cmdline > /dev/null
no_redundant=$?

wait_for_dnsmasq () {
  local _pid=$(pidof dnsmasq)
  for i in 0 1 2 3 4 5 6 7 8 9 10
  do
    sleep 1
    _pid=$(pidof dnsmasq)
    [ "$_pid" != "" ] && break;
  done
  [ "$_pid" != "" ] && return 0;
  logger -t cloud "edithosts: timed out waiting for dnsmasq to start"
  return 1
}

logger -t cloud "edithosts: update $1 $2 $3 to hosts"

[ ! -f $DHCP_HOSTS ] && touch $DHCP_HOSTS
[ ! -f $DHCP_OPTS ] && touch $DHCP_OPTS
[ ! -f $DHCP_LEASES ] && touch $DHCP_LEASES

#delete any previous entries from the dhcp hosts file
sed -i  /$mac/d $DHCP_HOSTS 
sed -i  /$ip,/d $DHCP_HOSTS 
sed -i  /$host,/d $DHCP_HOSTS 


#put in the new entry
echo "$mac,$ip,$host,infinite" >>$DHCP_HOSTS

#delete leases to supplied mac and ip addresses
sed -i  /$mac/d $DHCP_LEASES 
sed -i  /"$ip "/d $DHCP_LEASES 
sed -i  /"$host "/d $DHCP_LEASES 

#put in the new entry
echo "0 $mac $ip $host *" >> $DHCP_LEASES

#edit hosts file as well
sed -i  /"$ip "/d $HOSTS
sed -i  /"$host "/d $HOSTS
echo "$ip $host" >> $HOSTS

if [ "$dflt" != "" ]
then
  #make sure dnsmasq looks into options file
  sed -i /dhcp-optsfile/d /etc/dnsmasq.conf
  echo "dhcp-optsfile=$DHCP_OPTS" >> /etc/dnsmasq.conf

  tag=$(echo $ip | tr '.' '_')
  sed -i /$tag/d $DHCP_OPTS
  echo "$tag,3,$dflt" >> $DHCP_OPTS
  [ "$routes" != "" ] && echo "$tag,121,$routes" >> $DHCP_OPTS
  #delete entry we just put in because we need a tag
  sed -i  /$mac/d $DHCP_HOSTS 
  #put it back with a tag
  echo "$mac,set:$tag,$ip,$host,infinite" >>$DHCP_HOSTS
fi

# make dnsmasq re-read files
pid=$(pidof dnsmasq)
if [ "$pid" != "" ]
then
  service dnsmasq restart
else
  if [ $no_redundant -eq 1 ]
  then
      wait_for_dnsmasq
  else
      logger -t cloud "edithosts: skip wait dnsmasq due to redundant virtual router"
  fi
fi

ret=$?
releaseLockFile $lock_rr $locked_rr
unlock_exit $ret $lock $locked
