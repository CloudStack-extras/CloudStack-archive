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
 

# $Id: createtmplt.sh 9132 2010-06-04 20:17:43Z manuel $ $HeadURL: svn://svn.lab.vmops.com/repos/vmdev/java/scripts/storage/secondary/createtmplt.sh $
# createtmplt.sh -- install a template

usage() {
  printf "Usage: %s: -t <template-fs> -n <templatename> -f <root disk file> -c <md5 cksum> -d <descr> -h  [-u] [-v]\n" $(basename $0) >&2
}


#set -x
ulimit -f 41943040 #40GiB in blocks
ulimit -c 0

rollback_if_needed() {
  if [ $2 -gt 0 ]
  then
    printf "$3\n"
    #back out all changes
    rm -rf $1
    exit 2
fi
}

verify_cksum() {
  echo  "$1  $2" | md5sum  -c --status
  #printf "$1\t$2" | md5sum  -c --status
  if [ $? -gt 0 ] 
  then
    printf "Checksum failed, not proceeding with install\n"
    exit 3
  fi
}

untar() {
  local ft=$(file $1| awk -F" " '{print $2}')
  case $ft in
  USTAR) 
     printf "tar archives not supported\n"  >&2
     return 1
          ;;
  *) printf "$1"
     return 0
	  ;;
  esac

}

is_raw() {
  file $1| grep "x86 boot sector"
  return $?
}

is_compressed() {
  local ft=$(file $1| awk -F" " '{print $2}')
  local tmpfile=${1}.tmp

  case $ft in
  gzip)  ctype="gzip"
         ;;
  bzip2)  ctype="bz2"
         ;;
  ZIP)  ctype="zip"
        ;;
    *) echo "File $1 does not appear to be compressed" >&2
        return 1
	;;
  esac
  echo "Uncompressing to $tmpfile (type $ctype)...could take a long time" >&2
  return 0
}

uncompress() {
  local ft=$(file $1| awk -F" " '{print $2}')
  local tmpfile=${1}.tmp

  case $ft in
  gzip)  gunzip -q -c $1 > $tmpfile
         ;;
  bzip2)  bunzip2 -q -c $1 > $tmpfile
         ;;
  ZIP)  unzip -q -p $1 | cat > $tmpfile
        ;;
    *) printf "$1"
       return 0
	;;
  esac

  if [ $? -gt 0 ] 
  then
    printf "Failed to uncompress file (filetype=$ft), exiting "
    return 1 
  fi
 
  rm -f $1
  printf $tmpfile

  return 0
}

create_from_file() {
  local tmpltfs=$1
  local tmpltimg=$2
  local tmpltname=$3

  [ -n "$verbose" ] && echo "Moving to /$tmpltfs/$tmpltname...could take a while" >&2
  mv $tmpltimg /$tmpltfs/$tmpltname

}

tflag=
nflag=
fflag=
sflag=
hflag=
hvm=false
cleanup=false
dflag=
cflag=

while getopts 'vuht:n:f:s:c:d:S:' OPTION
do
  case $OPTION in
  t)	tflag=1
		tmpltfs="$OPTARG"
		;;
  n)	nflag=1
		tmpltname="$OPTARG"
		;;
  f)	fflag=1
		tmpltimg="$OPTARG"
		;;
  s)	sflag=1
		;;
  c)	cflag=1
		cksum="$OPTARG"
		;;
  d)	dflag=1
		descr="$OPTARG"
		;;
  S)	Sflag=1
		size=$OPTARG
                let "size>>=10"
		ulimit -f $size
		;;
  h)	hflag=1
		hvm="true"
		;;
  u)	cleanup="true"
		;;
  v)	verbose="true"
		;;
  ?)	usage
		exit 2
		;;
  esac
done

if [ "$tflag$nflag$fflag$sflag" != "1111" ]
then
 usage
 exit 2
fi

mkdir -p $tmpltfs

if [ ! -f $tmpltimg ] 
then
  printf "root disk file $tmpltimg doesn't exist\n"
  exit 3
fi

if [ -n "$cksum" ]
then
  verify_cksum $cksum $tmpltimg
fi
[ -n "$verbose" ] && is_compressed $tmpltimg
tmpltimg2=$(uncompress $tmpltimg)
rollback_if_needed $tmpltfs $? "failed to uncompress $tmpltimg\n"

tmpltimg2=$(untar $tmpltimg2)
rollback_if_needed $tmpltfs $? "tar archives not supported\n"

if [ ${tmpltname%.vhd} != ${tmpltname} ]
then
  if  which  vhd-util &>/dev/null
  then 
    vhd-util check -n ${tmpltimg2} > /dev/null
    rollback_if_needed $tmpltfs $? "vhd check of $tmpltimg2 failed\n"
    vhd-util set -n ${tmpltimg2} -f "hidden" -v "0" > /dev/null
    rollback_if_needed $tmpltfs $? "vhd remove $tmpltimg2 hidden failed\n"
  fi
fi

#FIXME: check for errors. Also vhd-util convert leaves backup files
#Do not use in production as is
if is_raw $tmpltimg2
then
  #raw to fixed vhd
  vhd-util convert -s 0 -t 1 -i $tmpltimg2 -o ${tmpltimg2}.tmp &> /dev/null
  #fixed to dynamic vhd
  vhd-util convert -s 1 -t 2 -i ${tmpltimg2}.tmp -o ${tmpltimg2}.tmp2 &> /dev/null
  mv ${tmpltimg2}.tmp2 ${tmpltimg}
fi

imgsize=$(ls -l $tmpltimg2| awk -F" " '{print $5}')

create_from_file $tmpltfs $tmpltimg2 $tmpltname

touch /$tmpltfs/template.properties
rollback_if_needed $tmpltfs $? "Failed to create template.properties file"
echo -n "" > /$tmpltfs/template.properties

today=$(date '+%m_%d_%Y')
echo "filename=$tmpltname" > /$tmpltfs/template.properties
echo "description=$descr" >> /$tmpltfs/template.properties
echo "checksum=$cksum" >> /$tmpltfs/template.properties
echo "hvm=$hvm" >> /$tmpltfs/template.properties
echo "size=$imgsize" >> /$tmpltfs/template.properties

if [ "$cleanup" == "true" ]
then
  rm -f $tmpltimg
fi

exit 0
