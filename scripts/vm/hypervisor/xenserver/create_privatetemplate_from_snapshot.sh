#!/bin/bash
# Version @VERSION@

#set -x
 
usage() {
  printf "Usage: %s [vhd file in secondary storage] [template directory in secondary storage] \n" $(basename $0) 
}

cleanup()
{
  if [ ! -z $snapshotdir ]; then 
    umount $snapshotdir
    if [ $? -eq 0 ];  then
      rm $snapshotdir -rf
    fi
  fi
  if [ ! -z $templatedir ]; then 
    umount $templatedir
    if [ $? -eq 0 ];  then
      rm $templatedir -rf
    fi
  fi
}

if [ -z $1 ]; then
  usage
  echo "2#no vhd file path"
  exit 0
else
  snapshoturl=${1%/*}
  vhdfilename=${1##*/}
fi

if [ -z $2 ]; then
  usage
  echo "3#no template path"
  exit 0
else
  templateurl=$2
fi

snapshotdir=/var/run/cloud_mount/$(uuidgen -r)
mkdir -p $snapshotdir
if [ $? -ne 0 ]; then
  echo "4#cann't make dir $snapshotdir"
  exit 0
fi

mount $snapshoturl $snapshotdir
if [ $? -ne 0 ]; then
  rm -rf $snapshotdir
  echo "5#can not mount $snapshoturl to $snapshotdir"
  exit 0
fi

templatedir=/var/run/cloud_mount/$(uuidgen -r)
mkdir -p $templatedir
if [ $? -ne 0 ]; then
  templatedir=""
  cleanup
  echo "6#cann't make dir $templatedir"
  exit 0
fi

mount $templateurl $templatedir
if [ $? -ne 0 ]; then
  rm -rf $templatedir
  templatedir=""
  cleanup
  echo "7#can not mount $templateurl to $templatedir"
  exit 0
fi

VHDUTIL="/opt/xensource/bin/vhd-util"

copyvhd()
{
  local desvhd=$1
  local srcvhd=$2
  local parent=
  parent=`$VHDUTIL query -p -n $srcvhd`
  if [ $? -ne 0 ]; then
    echo "30#failed to query $srcvhd"
    cleanup
    exit 0
  fi
  if [[ "${parent}"  =~ " no parent" ]]; then
    dd if=$srcvhd of=$desvhd bs=2M     
    if [ $? -ne 0 ]; then
      echo "31#failed to dd $srcvhd to $desvhd"
      cleanup
      exit 0
    fi
  else
    copyvhd $desvhd $parent
    $VHDUTIL coalesce -p $desvhd -n $srcvhd
    if [ $? -ne 0 ]; then
      echo "32#failed to coalesce  $desvhd to $srcvhd"
      cleanup
      exit 0
    fi
  fi
}

templateuuid=$(uuidgen -r)
desvhd=$templatedir/$templateuuid.vhd
srcvhd=$snapshotdir/$vhdfilename
copyvhd $desvhd $srcvhd
virtualSize=`$VHDUTIL query -v -n $desvhd`
physicalSize=`ls -l $desvhd | awk '{print $5}'`
cleanup
echo "0#$templateuuid#$physicalSize#$virtualSize"
exit 0
