#!bin/bash

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

usage() {
  printf "List Virtual Machines\nUsage: %s: -h management-server -z zoneid -d domainid\n" $(basename $0) >&2
}
 
zflag=
dflag=

zoneid=
domainid=
host="127.0.0.1" #defaults to localhost

while getopts 'h:z:d:' OPTION
do
 case $OPTION in
  h)	hflag=1
        host="$OPTARG"
        ;;
  z)    zflag=1
        zoneid="$OPTARG"
        ;;    
  d)    dflag=1
        domainid=$OPTARG
  ?)	usage
		exit 2
		;;
  esac
done

if [ $zflag$dflag != "1" ]
then
 usage
 exit 2
fi

query="GET	http://$host:8096/client/?command=listVirtualMachines&zoneId=$1&account=admin&domainid=$domainid	HTTP/1.0\n\n" 
echo -e $query | nc -v -w 20 $host 8096
