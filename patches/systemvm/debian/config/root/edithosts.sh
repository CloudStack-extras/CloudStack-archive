#!/usr/bin/env bash
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

wait_for_dnsmasq () {
  local _pid=$(pidof dnsmasq)
  for i in 0 1 2 3 4 5 6 7 8 9 10
  do
    sleep 1
    _pid=$(pidof dnsmasq)
    [ "$_pid" != "" ] && break;
  done
  [ "$_pid" != "" ] && return 0;
  echo "edithosts: timed out waiting for dnsmasq to start"
  return 1
}

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
  wait_for_dnsmasq
fi

exit $?
