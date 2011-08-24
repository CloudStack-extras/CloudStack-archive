# !/usr/bin/env bash



  #
  # Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
  #
 

# lu_share.sh -- makes all logical units (LUs) available over iSCSI, for a specified initiator IQN
# OpenSolaris

usage() {
  printf "Usage:  %s -i <initiator-iqn> [ -s | -u ]\n" $(basename $0) >&2
}

valid_target_name() {  # <target-name>
  echo $1 | grep ':lu:' >/dev/null
  return $?
}

target_iqn_from_target_name() {  # <target-name>
  echo $1 | cut -d':' -f1,2,3
}

hg_from_initiator_iqn() {  # <initiator-iqn>
  echo $1
  return 0
}

lu_name_from_target_name() {  # <target-name>
  echo $1 | cut -d':' -f5
}

view_entry_from_hg_and_lu_name() {  # <host-group-name> <lu-name>
  local hg=$1
  local lu_name=$2
  local view=
  local last_view=
  local last_hg=
  for w in $(stmfadm list-view -l $lu_name)
  do
    case $w in
    [0-9]*) last_view=$w
            ;;
    esac
    
    if [ "$w" == "$hg" ]
    then
      echo $last_view
      return 0
    fi
  done
  return 1
}

create_host_group() {  # <initiator-iqn>
  local hg=$1
  local i_iqn=$2
  local host_group=
  
  local result=
  result=$(stmfadm create-hg $hg 2>&1)
  if [ $? -ne 0 ]
  then
     echo $result | grep "already exists" > /dev/null
     if [ $? -ne 0 ]
     then  
       printf "%s: create-hg %s failed due to %s\n" $(basename $0) $i_iqn $result >&2
       return 11
     fi
   fi
   
   result=$(stmfadm add-hg-member -g $hg $i_iqn 2>&1)
   if [ $? -ne 0 ]
   then
     echo $result | grep "already exists" > /dev/null
     if [ $? -ne 0 ]
     then
       printf "%s: unable to add %s due to %s\n" $(basename $0) $i_iqn $result >&2
       return 12
     fi
   fi
  return 0
}

add_view() { # <hg> <lu_name>
  local i=1
  local hg=$1
  local lu=$2
  
  while [ $i -lt 500 ]
  do
    local lun=$[ ( $RANDOM % 512 ) ]
    local result=
    result=$(stmfadm add-view -h $hg -n $lun $lu 2>&1)
    if [ $? -eq 0 ]
    then
      printf "lun %s for luname %s\n" $lun $lu  >&2
      #stmfadm list-view -l $lu
      #sbdadm list-lu 
      return 0
    fi
    echo $result | grep "view entry exists" > /dev/null
    if [ $? -eq 0 ]
    then
      return 0
    fi
    echo $result | grep "LUN already in use" > /dev/null
    if [ $? -ne 0 ]
    then
      echo $result
      return 1
    fi
    let i=i+1
  done
  printf "Unable to add view after lots of tries\n" >&2
  return 1
}

add_view_and_hg() {  # <initiator_iqn> <lu_name>
  local i_iqn=$1
  local lu_name=$2
  local hg="Migration"
  local result=

  if ! create_host_group $hg $i_iqn
  then
    printf "%s: create_host_group failed: %s %s\n" $(basename $0) $i_iqn $lu_name >&2
    return 22
  fi

  if ! add_view $hg $lu_name 
  then
    return 1
  fi

  return 0
}

remove_view() {  # <initiator-iqn> <lu-name>
  local lu_name=$1
  local hg="Migration"
  local view=$(view_entry_from_hg_and_lu_name $hg $lu_name)
  if [ -n "$view" ]
  then
    local result=
    result=$(stmfadm remove-view -l $lu_name $view 2>&1)
    if [ $? -ne 0 ]
    then
      echo $result | grep "not found"
      if [ $? -eq 0 ]
      then
        return 0
      fi
      echo $result | grep "no views found"
      if [ $? -eq 0 ]
      then
       return 0
      fi
      printf "Unable to remove view due to: $result\n" >&2
      return 5
    fi
  fi
  return 0
}

# set -x

iflag=
sflag=
uflag=

while getopts 'sui:' OPTION
do
  case $OPTION in
  i)	iflag=1
  	init_iqn="$OPTARG"
  	;;
  s)    sflag=1
       	;;
  u)	uflag=1
  	;;
  *)	usage
  	exit 2
  	;;
  esac
done

if [ "$sflag$iflag" != "11" -a "$uflag" != "1" ]
then
  usage
  exit 3
fi

lu_names="$(stmfadm list-lu | cut -d":" -f2)"

for lu_name in $lu_names
do
  if [ "$uflag" == "1" ]
  then
    remove_view $lu_name
    if [ $? -gt 0 ]
    then
      printf "%s: remove_view failed: %s\n" $(basename $0) $lu_name >&2 
      exit 1
    fi
  else
    if [ "$sflag" == "1" ]
    then
      add_view_and_hg $init_iqn $lu_name
      if [ $? -gt 0 ]
      then
        printf "%s: add_view failed: %s\n" $(basename $0) $lu_name >&2
        exit 1
      fi
    fi
  fi
done

if [ "$uflag" == "1" ]
then
  stmfadm delete-hg "Migration"
fi

exit 0
