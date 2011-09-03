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
 

# $Id: managesnapshot.sh 11601 2010-08-11 17:26:15Z kris $ $HeadURL: svn://svn.lab.vmops.com/repos/branches/2.1.refactor/java/scripts/storage/qcow2/managesnapshot.sh $
# managesnapshot.sh -- manage snapshots for a single disk (create, destroy, rollback)
#

usage() {
  printf "Usage: %s: -c <path to disk> -n <snapshot name>\n" $(basename $0) >&2
  printf "Usage: %s: -d <path to disk> -n <snapshot name>\n" $(basename $0) >&2
  printf "Usage: %s: -r <path to disk> -n <snapshot name>\n" $(basename $0) >&2
  printf "Usage: %s: -b <path to disk> -n <snapshot name> -p <dest name>\n" $(basename $0) >&2
  exit 2
}

qemu_img="cloud-qemu-img"
which $qemu_img
if [ $? -gt 0 ]
then
   which qemu-img
   if [ $? -eq 0 ]
   then
       qemu_img="qemu-img"
   fi
fi

create_snapshot() {
  local disk=$1
  local snapshotname=$2
  local failed=0

  if [ -b "${disk}" ] && lvm lvs "${disk}" >/dev/null 2>&1; then
      local lv=$( lvm lvs --noheadings --unbuffered --separator=/ "${disk}" 2>/dev/null | sed 's|^[[:space:]]\+||' )
      local lvname=$( echo "${lv}" | awk -F/ '{ print $1 }' )
      local vgname=$( echo "${lv}" | awk -F/ '{ print $2 }' )
      local lvdmname=$( echo "${lvname}" | sed 's|-|--|g' )
      local vgdmname=$( echo "${vgname}" | sed 's|-|--|g' )
      local blockdevname="/dev/mapper/${vgdmname}-${lvdmname}"
      local blockdevsnap="/dev/mapper/${vgdmname}-${snapshotname}"
      local blockdevsize=$( blockdev --getsz "${blockdevname}" )
       
      lvm lvcreate --name "${snapshotname}-cow" --size "$(blockdev --getsize64 ${blockdevname})b" "${vgname}" >&2 || return 1
      dmsetup suspend "${vgdmname}-${lvdmname}" >&2
      [ $? -ne 0 ] && destroy_snapshot "${disk}" "${snapshotname}" && return 1
      if dmsetup table | awk -v e=1 -v tbl="${vgdmname}-${lvdmname}-real:" '$1 == tbl { e=0 }; END { exit e }'; then
          dmsetup create "${vgdmname}-${snapshotname}" --notable >&2
          [ $? -ne 0 ] && destroy_snapshot "${disk}" "${snapshotname}" && return 1

          echo "0 ${blockdevsize} snapshot ${blockdevname}-real ${blockdevsnap}--cow p 64" | \
              dmsetup load "${vgdmname}-${snapshotname}" >&2
          [ $? -ne 0 ] && destroy_snapshot "${disk}" "${snapshotname}" && return 1

          dmsetup resume "${vgdmname}-${snapshotname}" >&2
          [ $? -ne 0 ] && destroy_snapshot "${disk}" "${snapshotname}" && return 1

      else

          dmsetup create "${vgdmname}-${lvdmname}-real" --notable >&2
          [ $? -ne 0 ] && destroy_snapshot "${disk}" "${snapshotname}" && return 1

          dmsetup table "${vgdmname}-${lvdmname}" | dmsetup load "${vgdmname}-${lvdmname}-real" >&2
          [ $? -ne 0 ] && destroy_snapshot "${disk}" "${snapshotname}" && return 1

          dmsetup resume "${vgdmname}-${lvdmname}-real" >&2
          [ $? -ne 0 ] && destroy_snapshot "${disk}" "${snapshotname}" && return 1

          dmsetup create "${vgdmname}-${snapshotname}" --notable >&2
          [ $? -ne 0 ] && destroy_snapshot "${disk}" "${snapshotname}" && return 1

          echo "0 ${blockdevsize} snapshot ${blockdevname}-real ${blockdevsnap}--cow p 64" | \
              dmsetup load "${vgdmname}-${snapshotname}" >&2
          [ $? -ne 0 ] && destroy_snapshot "${disk}" "${snapshotname}" && return 1

          echo "0 ${blockdevsize} snapshot-origin ${blockdevname}-real" | \
              dmsetup load "${vgdmname}-${lvdmname}"
          [ $? -ne 0 ] && destroy_snapshot "${disk}" "${snapshotname}" && return 1

          dmsetup resume "${vgdmname}-${snapshotname}" >&2
          [ $? -ne 0 ] && destroy_snapshot "${disk}" "${snapshotname}" && return 1

      fi

      dmsetup resume "${vgdmname}-${lvdmname}" >&2
      [ $? -ne 0 ] && destroy_snapshot "${disk}" "${snapshotname}" && return 1

  elif [ -f "${disk}" ]; then
     $qemu_img snapshot -c $snapshotname $disk

  if [ $? -gt 0 ]
  then
    failed=2
    printf "***Failed to create snapshot $snapshotname for path $disk\n" >&2
    $qemu_img snapshot -d $snapshotname $disk
    
    if [ $? -gt 0 ]
    then
      printf "***Failed to delete snapshot $snapshotname for path $disk\n" >&2
    fi
  fi


    else
	failed=2
	printf "***Failed to create snapshot $snapshotname, undefined type $disk\n" >&2
    fi

  return $failed 
}

destroy_snapshot() {
  local disk=$1
  local snapshotname=$2
  local failed=0

  if [ -b ${disk} ]; then
     local lvname=$( echo "${disk}" | awk -F/ '{ print $(NF) }' ) # '
     local vgname=$( echo "${disk}" | awk -F/ '{ print $(NF-1) }' ) # '
     local lvdmname=$( echo "${lvname}" | sed 's|-|--|g' )
     local vgdmname=$( echo "${vgname}" | sed 's|-|--|g' )

     if [ $( dmsetup --columns --noheadings --separator=: info "${vgdmname}-${lvdmname}-real" | awk -F: '{ print $5 }' ) -le 2 ]; then
         dmsetup suspend "${vgdmname}-${lvdmname}" >&2
         dmsetup table "${vgdmname}-${lvdmname}-real" | dmsetup load "${vgdmname}-${lvdmname}" >&2
         dmsetup resume "${vgdmname}-${lvdmname}" >&2
         dmsetup remove "${vgdmname}-${snapshotname}" >&2
         dmsetup remove "${vgdmname}-${lvdmname}-real" >&2
     else
         dmsetup remove "${vgdmname}-${snapshotname}" >&2
     fi
     lvm lvremove -f "${vgname}/${snapshotname}-cow" >&2
  elif [ -d $disk ]; then

     if [ -f $disk/$snapshotname ]
     then
	    rm -rf $disk/$snapshotname >& /dev/null
     fi

     return $failed
  elif [ -f $disk ];then
     $qemu_img snapshot -d $snapshotname $disk
  if [ $? -gt 0 ]
  then
     failed=2
     printf "Failed to delete snapshot $snapshotname for path $disk\n" >&2
  fi	
  else
     failed=2
     printf "***Failed to delete snapshot $snapshotname, undefined type $disk\n" >&2
  fi

  return $failed 
}

rollback_snapshot() {
  local disk=$1
  local snapshotname=$2
  local failed=0

  if [ -b ${disk} ]; then
     return 0
  else
     $qemu_img snapshot -a $snapshotname $disk
  
  if [ $? -gt 0 ]
  then
    printf "***Failed to apply snapshot $snapshotname for path $disk\n" >&2
    failed=1
  fi
  
  fi
  return $failed 
}
backup_snapshot() {
  local disk=$1
  local snapshotname=$2
  local destPath=$3
  local destName=$4

  if [ ! -d $destPath ]
  then
     mkdir -p $destPath >& /dev/null
     if [ $? -gt 0 ]
     then
        printf "Failed to create $destPath" >&2
        return 3
     fi
  fi

    if [ -b ${disk} ] && lvm lvs "${disk}" >/dev/null 2>&1; then
	local lv=$( lvm lvs --noheadings --unbuffered --separator=/ "${disk}" 2>/dev/null | sed 's|^[[:space:]]\+||' )
	local vgname=$( echo "${lv}" | awk -F/ '{ print $2 }' )
	local vgdmname=$( echo "${vgname}" | sed 's|-|--|g' )

	if [ -x "$( dirname $0 )/raw2qcow2.sh" ]; then
	    "$( dirname $0 )/raw2qcow2.sh" "/dev/mapper/${vgdmname}-${snapshotname}" "${destPath}/${destName}"
	else
	    cloud-qemu-img convert -f raw -O qcow2 "/dev/mapper/${vgdmname}-${snapshotname}" "${destPath}/${destName}"
	fi
	return 0

    elif [ -f ${disk} ]; then
  # Does the snapshot exist? 
  $qemu_img snapshot -l $disk|grep -w "$snapshotname" >& /dev/null
  if [ $? -gt 0 ]
  then
    printf "there is no $snapshotname on disk $disk" >&2
    return 1
  fi

  $qemu_img convert -f qcow2 -O qcow2 -s $snapshotname $disk $destPath/$destName >& /dev/null
  if [ $? -gt 0 ]
  then
    printf "Failed to backup $snapshotname for disk $disk to $destPath" >&2
    return 2
  fi

    else
	printf "***Failed to backup snapshot $snapshotname, undefined type $disk\n" >&2
	return 2
    fi

  return 0
}
#set -x

cflag=
dflag=
rflag=
bflag=
nflag=
pathval=
snapshot=
tmplName=
deleteDir=

while getopts 'c:d:r:n:b:p:t:f' OPTION
do
  case $OPTION in
  c)	cflag=1
	pathval="$OPTARG"
	;;
  d)    dflag=1
        pathval="$OPTARG"
        ;;
  r)    rflag=1
        pathval="$OPTARG"
        ;;
  b)    bflag=1
        pathval="$OPTARG"
        ;;
  n)	nflag=1
	snapshot="$OPTARG"
	;;
  p)    destPath="$OPTARG"
        ;;
  t)    tmplName="$OPTARG"
	;;
  f)    deleteDir=1
	;;
  ?)	usage
	;;
  esac
done

[ -b "$pathval" ] && snapshot=`echo "${snapshot}" | md5sum -t | awk '{ print $1 }'`

if [ "$cflag" == "1" ]
then
  create_snapshot $pathval $snapshot
  exit $?
elif [ "$dflag" == "1" ]
then
  destroy_snapshot $pathval $snapshot $deleteDir
  exit $?
elif [ "$bflag" == "1" ]
then
  backup_snapshot $pathval $snapshot $destPath $tmplName
  exit $?
elif [ "$rflag" == "1" ]
then
  rollback_snapshot $pathval $snapshot $destPath
  exit $?
fi


exit 0
