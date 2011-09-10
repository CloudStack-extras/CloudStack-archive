#!/bin/bash



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
 

source /root/func.sh

lock="biglock"
locked=$(getLockFile $lock)
if [ "$locked" != "1" ]
then
    exit 1
fi

usage() {
  printf "Usage: %s: -v <vm ip> -F <vm data folder> -f <vm data file> -d <data to put in file> \n" $(basename $0) >&2
  unlock_exit 2 $lock $locked
}

set -x

PORT=3922

create_htaccess() {
  local vmIp=$1
  local folder=$2
  local file=$3
  
  local result=0
  
  entry="RewriteRule ^$file$ ../$folder/%{REMOTE_ADDR}/$file [L,NC,QSA]"
  htaccessFolder="/var/www/html/latest"
  htaccessFile=$htaccessFolder/.htaccess
  mkdir -p $htaccessFolder
  touch $htaccessFile
  
  #grep -w $file $htaccessFile
  grep -F `echo $entry` $htaccessFile
  
  if [ $? -gt 0 ]; then 
    echo -e $entry >> $htaccessFile; 
  fi
  result=$?
  
  if [ $result -eq 0 ]; then
    entry="Options -Indexes\\nOrder Deny,Allow\\nDeny from all\\nAllow from $vmIp"
    htaccessFolder="/var/www/html/$folder/$vmIp"
    htaccessFile=$htaccessFolder/.htaccess
    
    mkdir -p $htaccessFolder
    echo -e $entry > $htaccessFile
    result=$?
  fi

  return $result  
}

copy_vm_data_file() {
  local vmIp=$1
  local folder=$2
  local file=$3
  local dataFile=$4        
  
  dest=/var/www/html/$folder/$vmIp/$file
  metamanifest=/var/www/html/$folder/$vmIp/meta-data
  chmod +r $dataFile
  cp $dataFile $dest
  chmod 644 $dest
  touch $metamanifest
  chmod 644 $metamanifest
  if [ "$folder" == "metadata" ] || [ "$folder" == "meta-data" ]
  then
    sed -i '/$file/d' $metamanifest
    echo $file >> $metamanifest
  fi
  return $?
}

delete_vm_data_file() {
  local domrIp=$1
  local vmIp=$2
  local folder=$3
  local file=$4
  
  vmDataFilePath="/var/www/html/$folder/$vmIp/$file"
  if [ -f $vmDataFilePath ]; then 
    rm -rf $vmDataFilePath 
  fi
  return $?
}

vmIp=
folder=
file=
dataFile=

while getopts 'v:F:f:d:' OPTION
do
  case $OPTION in
  v)	vmIp="$OPTARG"
		;;
  F)	folder="$OPTARG"
  		;;
  f)	file="$OPTARG"
  		;;
  d)	dataFile="$OPTARG"
  		;;
  ?)    usage
                unlock_exit 1 $lock $locked
		;;
  esac
done

[ "$vmIp" == "" ]  || [ "$folder" == "" ] || [ "$file" == "" ] && usage 
[ "$folder" != "userdata" ] && [ "$folder" != "metadata" ] && usage

if [ "$dataFile" != "" ]
then
  create_htaccess $vmIp $folder $file
  
  if [ $? -gt 0 ]
  then
    unlock_exit 1 $lock $locked
  fi
  
  copy_vm_data_file $vmIp $folder $file $dataFile
else
  delete_vm_data_file $vmIp $folder $file
fi

unlock_exit $? $lock $locked
