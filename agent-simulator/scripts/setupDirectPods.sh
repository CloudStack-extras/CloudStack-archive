
  #
  #  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
  #
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
  #
 

x=$1
y=$2
name=$3

pod_query="GET  http://10.91.30.226:8096/client/?command=createPod&zoneId=1&name=SSP$name&cidr=182.$x.$y.0%2F24&startIp=182.$x.$y.2&endIp=182.$x.$y.252&gateway=182.$x.$y.1
HTTP/1.0\n\n"

echo -e $pod_query | nc -v -w 20 10.91.30.226 8096

#vlan_query="GET http://10.91.30.226/client/?command=createVlanIpRange&vlan=untagged&zoneid=1&podId=$name&forVirtualNetwork=false&gateway=172.$y.$x.1&netmask=255.255.255.0&startip=172.$y.$x.2&endip=172.$y.$x.252        HTTP/1.0\n\n"

#echo -e $vlan_query | nc -v -w 20 10.91.30.226 8096
