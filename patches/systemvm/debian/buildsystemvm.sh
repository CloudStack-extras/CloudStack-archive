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
 


set -x

IMAGENAME=systemvm
LOCATION=/var/lib/images/systemvm
PASSWORD=password
#APT_PROXY=192.168.1.115:3142/
APT_PROXY=
HOSTNAME=systemvm
SIZE=2000
DEBIAN_MIRROR=ftp.us.debian.org/debian
MINIMIZE=true
CLOUDSTACK_RELEASE=2.2

baseimage() {
  mkdir -p $LOCATION
  #dd if=/dev/zero of=$IMAGELOC bs=1M  count=$SIZE
  dd if=/dev/zero of=$IMAGELOC bs=1M seek=$((SIZE - 1)) count=1
  loopdev=$(losetup -f)
  losetup $loopdev $IMAGELOC
  parted $loopdev -s 'mklabel msdos'
  parted $loopdev -s 'mkpart primary ext3 512B -1'
  sleep 2 
  losetup -d $loopdev
  loopdev=$(losetup --show -o 512 -f $IMAGELOC )
  mkfs.ext3  -L ROOT $loopdev
  mkdir -p $MOUNTPOINT
  tune2fs -c 100 -i 0 $loopdev
  sleep 2 
  losetup -d $loopdev
  
  mount -o loop,offset=512 $IMAGELOC  $MOUNTPOINT
  
  #debootstrap --variant=minbase --keyring=/usr/share/keyrings/debian-archive-keyring.gpg squeeze $MOUNTPOINT http://${APT_PROXY}${DEBIAN_MIRROR}
  debootstrap --variant=minbase --arch=i386 squeeze $MOUNTPOINT http://${APT_PROXY}${DEBIAN_MIRROR}
}


fixapt() {
  if [ "$APT_PROXY" != "" ]; then
  cat >> etc/apt/apt.conf.d/01proxy << EOF
Acquire::http::Proxy "http://${APT_PROXY}";
EOF
  fi

  cat > etc/apt/sources.list << EOF
deb http://ftp.us.debian.org/debian/ squeeze main non-free
deb-src http://ftp.us.debian.org/debian/ squeeze main non-free

deb http://security.debian.org/ squeeze/updates main
deb-src http://security.debian.org/ squeeze/updates main

deb http://volatile.debian.org/debian-volatile squeeze/volatile main
deb-src http://volatile.debian.org/debian-volatile squeeze/volatile main

deb http://ftp.us.debian.org/debian testing main contrib non-free
EOF

  cat >> etc/apt/apt.conf << EOF
APT::Default-Release "stable"; 
EOF

  cat >> etc/apt/preferences << EOF
Package: *
Pin: release o=Debian,a=stable
Pin-Priority: 900

Package: *
Pin: release o=Debian,a=testing
Pin-Priority: 400
EOF

  #apt-key exportall | chroot . apt-key add - &&
  chroot . apt-get update &&
  echo "Apt::Install-Recommends 0;" > etc/apt/apt.conf.d/local-recommends

  cat >> usr/sbin/policy-rc.d  << EOF
#!/bin/sh
exit 101
EOF
  chmod a+x usr/sbin/policy-rc.d

  cat >> etc/default/locale  << EOF
LANG=en_US.UTF-8
LC_ALL=en_US.UTF-8
EOF

  cat >> etc/locale.gen  << EOF
en_US.UTF-8 UTF-8
EOF

  DEBIAN_FRONTEND=noninteractive
  DEBIAN_PRIORITY=critical
  export DEBIAN_FRONTEND DEBIAN_PRIORITY 
  chroot . dpkg-reconfigure debconf --frontend=noninteractive
  chroot . apt-get -q -y install locales
}

network() {

  echo "$HOSTNAME" > etc/hostname &&
  cat > etc/hosts << EOF 
127.0.0.1       localhost
# The following lines are desirable for IPv6 capable hosts
::1     localhost ip6-localhost ip6-loopback
fe00::0 ip6-localnet
ff00::0 ip6-mcastprefix
ff02::1 ip6-allnodes
ff02::2 ip6-allrouters
ff02::3 ip6-allhosts
EOF

  cat >> etc/network/interfaces << EOF
auto lo eth0
iface lo inet loopback

# The primary network interface
iface eth0 inet static

EOF
}

install_kernel() {
  DEBIAN_FRONTEND=noninteractive
  DEBIAN_PRIORITY=critical
  export DEBIAN_FRONTEND DEBIAN_PRIORITY

  chroot . apt-get -qq -y --force-yes install grub &&
  cp -av usr/lib/grub/i386-pc boot/grub
  #for some reason apt-get install grub does not install grub/stage1 etc
  loopd=$(losetup -f --show $1)
  grub-install $loopd --root-directory=$MOUNTPOINT
  losetup -d $loopd
  grub  << EOF &&
device (hd0) $1
root (hd0,0)
setup (hd0)
quit
EOF
   # install a kernel image
   cat > etc/kernel-img.conf << EOF &&
do_symlinks = yes
link_in_boot = yes
do_initrd = yes
EOF
  chroot . apt-get install -qq -y --force-yes linux-image-686-bigmem
  cat >> etc/kernel-img.conf << EOF
postinst_hook = /usr/sbin/update-grub
postrm_hook   = /usr/sbin/update-grub
EOF
}


fixgrub() {
  kern=$(basename $(ls  boot/vmlinuz-*))
  ver=${kern#vmlinuz-}
  cat > boot/grub/menu.lst << EOF
default 0
timeout 2
color cyan/blue white/blue

### BEGIN AUTOMAGIC KERNELS LIST
# kopt=root=LABEL=ROOT ro

## ## End Default Options ##
title		Debian GNU/Linux, kernel $ver
root		(hd0,0)
kernel		/boot/$kern root=LABEL=ROOT ro console=tty0 xencons=ttyS0,115200 console=hvc0 quiet
initrd		/boot/initrd.img-$ver

### END DEBIAN AUTOMAGIC KERNELS LIST
EOF
  (cd boot/grub; ln -s menu.lst grub.conf)
}

fixinittab() {
  cat >> etc/inittab << EOF

vc:2345:respawn:/sbin/getty 38400 hvc0
EOF
}

fixfstab() {
  cat > etc/fstab << EOF
# <file system> <mount point>   <type>  <options>       <dump>  <pass>
proc            /proc           proc    defaults        0       0
LABEL=ROOT      /               ext3    errors=remount-ro 0       1
EOF
}

fixacpid() {
  mkdir -p etc/acpi/events
  cat >> etc/acpi/events/power << EOF
event=button/power.*
action=/usr/local/sbin/power.sh "%e"
EOF
  cat >> usr/local/sbin/power.sh << EOF
#!/bin/bash
/sbin/poweroff
EOF
  chmod a+x usr/local/sbin/power.sh
}

fixiptables() {
cat >> etc/modules << EOF
nf_conntrack
nf_conntrack_ipv4
EOF
cat > etc/init.d/iptables-persistent << EOF
#!/bin/sh
### BEGIN INIT INFO
# Provides:          iptables
# Required-Start:    mountkernfs $local_fs
# Required-Stop:     $local_fs
# Should-Start:      cloud-early-config
# Default-Start:     S
# Default-Stop:     
# Short-Description: Set up iptables rules
### END INIT INFO

PATH="/sbin:/bin:/usr/sbin:/usr/bin"

# Include config file for iptables-persistent
. /etc/iptables/iptables.conf

case "\$1" in
start)
    if [ -e /var/run/iptables ]; then
        echo "iptables is already started!"
        exit 1
    else
        touch /var/run/iptables
    fi

    if [ \$ENABLE_ROUTING -ne 0 ]; then
        # Enable Routing
        echo 1 > /proc/sys/net/ipv4/ip_forward
    fi

    # Load Modules
    modprobe -a \$MODULES

    # Load saved rules
    if [ -f /etc/iptables/rules ]; then
        iptables-restore </etc/iptables/rules
    fi
    ;;
stop|force-stop)
    if [ ! -e /var/run/iptables ]; then
        echo "iptables is already stopped!"
        exit 1
    else
        rm /var/run/iptables
    fi

    if [ \$SAVE_NEW_RULES -ne 0 ]; then
        # Backup old rules
        cp /etc/iptables/rules /etc/iptables/rules.bak
        # Save new rules
        iptables-save >/etc/iptables/rules
    fi

    # Restore Default Policies
    iptables -P INPUT ACCEPT
    iptables -P FORWARD ACCEPT
    iptables -P OUTPUT ACCEPT

    # Flush rules on default tables
    iptables -F
    iptables -t nat -F
    iptables -t mangle -F

    # Unload previously loaded modules
    modprobe -r \$MODULES

    # Disable Routing if enabled
    if [ \$ENABLE_ROUTING -ne 0 ]; then
        # Disable Routing
        echo 0 > /proc/sys/net/ipv4/ip_forward
    fi

    ;;
restart|force-reload)
    \$0 stop
    \$0 start
    ;;
status)
    echo "Filter Rules:"
    echo "--------------"
    iptables -L -v
    echo ""
    echo "NAT Rules:"
    echo "-------------"
    iptables -t nat -L -v
    echo ""
    echo "Mangle Rules:"
    echo "----------------"
    iptables -t mangle -L -v
    ;;
*)
    echo "Usage: \$0 {start|stop|force-stop|restart|force-reload|status}" >&2
    exit 1
    ;;
esac

exit 0
EOF
  chmod a+x etc/init.d/iptables-persistent


  touch etc/iptables/iptables.conf 
  cat > etc/iptables/iptables.conf << EOF
# A basic config file for the /etc/init.d/iptable-persistent script
#

# Should new manually added rules from command line be saved on reboot? Assign to a value different that 0 if you want this enabled.
SAVE_NEW_RULES=0

# Modules to load:
MODULES="nf_nat_ftp nf_conntrack_ftp"

# Enable Routing?
ENABLE_ROUTING=1
EOF
  chmod a+x etc/iptables/iptables.conf

}

vpn_config() {
  cp -r ${scriptdir}/vpn/* ./
}

packages() {
  DEBIAN_FRONTEND=noninteractive
  DEBIAN_PRIORITY=critical
  DEBCONF_DB_OVERRIDE=’File{/root/config.dat}’
  export DEBIAN_FRONTEND DEBIAN_PRIORITY DEBCONF_DB_OVERRIDE

  #basic stuff
  chroot .  apt-get --no-install-recommends -q -y --force-yes install rsyslog logrotate cron chkconfig insserv net-tools ifupdown vim-tiny netbase iptables openssh-server grub e2fsprogs dhcp3-client dnsmasq tcpdump socat wget  python bzip2 sed gawk diff grep gzip less tar telnet ftp rsync traceroute psmisc lsof procps monit inetutils-ping iputils-arping httping dnsutils zip unzip ethtool uuid file iproute acpid iptables-persistent virt-what sudo

  #sysstat
  chroot . echo 'sysstat sysstat/enable boolean true' | chroot . debconf-set-selections
  chroot .  apt-get --no-install-recommends -q -y --force-yes install sysstat
  #apache
  chroot .  apt-get --no-install-recommends -q -y --force-yes install apache2 ssl-cert 
  #haproxy
  chroot . apt-get --no-install-recommends -q -y --force-yes install haproxy 
  #dnsmasq
  chroot . apt-get --no-install-recommends -q -y --force-yes install dnsmasq 
  #nfs client
  chroot . apt-get --no-install-recommends -q -y --force-yes install nfs-common
  #vpn stuff
  chroot .  apt-get --no-install-recommends -q -y --force-yes install xl2tpd openswan bcrelay ppp ipsec-tools tdb-tools
  #vmware tools
  chroot . apt-get --no-install-recommends -q -y --force-yes install open-vm-tools
  #xenstore utils
  chroot . apt-get --no-install-recommends -q -y --force-yes install xenstore-utils libxenstore3.0
  #keepalived and conntrackd
  chroot . apt-get --no-install-recommends -q -y --force-yes install keepalived conntrackd ipvsadm libnetfilter-conntrack3 libnl1

  echo "***** getting sun jre 6*********"
  chroot . echo 'sun-java6-bin shared/accepted-sun-dlj-v1-1 boolean true
	sun-java6-jre shared/accepted-sun-dlj-v1-1 boolean true
	sun-java6-jre sun-java6-jre/stopthread boolean true
	sun-java6-jre sun-java6-jre/jcepolicy note
	sun-java6-bin shared/present-sun-dlj-v1-1 note
	sun-java6-jre shared/present-sun-dlj-v1-1 note ' | chroot . debconf-set-selections
  chroot .  apt-get --no-install-recommends -q -y install  sun-java6-jre 

}


password() {
  chroot . echo "root:$PASSWORD" | chroot . chpasswd
}

apache2() {
   chroot . a2enmod ssl rewrite auth_basic auth_digest
   chroot . a2ensite default-ssl
   cp etc/apache2/sites-available/default etc/apache2/sites-available/default.orig
   cp etc/apache2/sites-available/default-ssl etc/apache2/sites-available/default-ssl.orig
}

services() {
  mkdir -p ./var/www/html
  mkdir -p ./opt/cloud/bin
  mkdir -p ./var/cache/cloud
  mkdir -p ./usr/share/cloud
  mkdir -p ./usr/local/cloud
  mkdir -p ./root/.ssh
  
  /bin/cp -r ${scriptdir}/config/* ./
  chroot . chkconfig xl2tpd off
  chroot . chkconfig --add cloud-early-config
  chroot . chkconfig cloud-early-config on
  chroot . chkconfig --add cloud-passwd-srvr 
  chroot . chkconfig cloud-passwd-srvr off
  chroot . chkconfig --add cloud
  chroot . chkconfig cloud off
  chroot . chkconfig monit off
}

cleanup() {
  rm -f usr/sbin/policy-rc.d
  rm -f root/config.dat
  rm -f etc/apt/apt.conf.d/01proxy 

  if [ "$MINIMIZE" == "true" ]
  then
    rm -rf var/cache/apt/*
    rm -rf var/lib/apt/*
    rm -rf usr/share/locale/[a-d]*
    rm -rf usr/share/locale/[f-z]*
    rm -rf usr/share/doc/*
    size=$(df   $MOUNTPOINT | awk '{print $4}' | grep -v Available)
    dd if=/dev/zero of=$MOUNTPOINT/zeros.img bs=1M count=$((((size-150000)) / 1000))
    rm -f $MOUNTPOINT/zeros.img
  fi
}

signature() {
  (cd ${scriptdir}/config;  tar cvf ${MOUNTPOINT}/usr/share/cloud/cloud-scripts.tar *)
  (cd ${scriptdir}/vpn;  tar rvf ${MOUNTPOINT}/usr/share/cloud/cloud-scripts.tar *)
  gzip -c ${MOUNTPOINT}/usr/share/cloud/cloud-scripts.tar  > ${MOUNTPOINT}/usr/share/cloud/cloud-scripts.tgz
  md5sum ${MOUNTPOINT}/usr/share/cloud/cloud-scripts.tgz |awk '{print $1}'  > ${MOUNTPOINT}/var/cache/cloud/cloud-scripts-signature
  echo "Cloudstack Release $CLOUDSTACK_RELEASE $(date)" > ${MOUNTPOINT}/etc/cloudstack-release
}

mkdir -p $IMAGENAME
mkdir -p $LOCATION
MOUNTPOINT=/mnt/$IMAGENAME/
IMAGELOC=$LOCATION/$IMAGENAME.img
scriptdir=$(dirname $PWD/$0)

rm -f $IMAGELOC
begin=$(date +%s)
echo "*************INSTALLING BASEIMAGE********************"
baseimage

cp $scriptdir/config.dat $MOUNTPOINT/root/
cd $MOUNTPOINT

mount -o bind /proc $MOUNTPOINT/proc
mount -o bind /dev $MOUNTPOINT/dev

echo "*************CONFIGURING APT********************"
fixapt  
echo "*************DONE CONFIGURING APT********************"

echo "*************CONFIGURING NETWORK********************"
network
echo "*************DONE CONFIGURING NETWORK********************"

echo "*************INSTALLING KERNEL********************"
install_kernel $IMAGELOC
echo "*************DONE INSTALLING KERNEL********************"

echo "*************CONFIGURING GRUB********************"
fixgrub $IMAGELOC
echo "*************DONE CONFIGURING GRUB********************"


echo "*************CONFIGURING INITTAB********************"
fixinittab
echo "*************DONE CONFIGURING INITTAB********************"

echo "*************CONFIGURING FSTAB********************"
fixfstab
echo "*************DONE CONFIGURING FSTAB********************"

echo "*************CONFIGURING ACPID********************"
fixacpid
echo "*************DONE CONFIGURING ACPID********************"

echo "*************INSTALLING PACKAGES********************"
packages
echo "*************DONE INSTALLING PACKAGES********************"

echo "*************CONFIGURING IPTABLES********************"
fixiptables
echo "*************DONE CONFIGURING IPTABLES********************"

echo "*************CONFIGURING PASSWORD********************"
password

echo "*************CONFIGURING SERVICES********************"
services

echo "*************CONFIGURING APACHE********************"
apache2

echo "*************CONFIGURING VPN********************"
vpn_config

echo "*************CLEANING UP********************"
cleanup 

echo "*************GENERATING SIGNATURE********************"
signature

cd $scriptdir

umount $MOUNTPOINT/proc
umount $MOUNTPOINT/dev
umount $MOUNTPOINT
fin=$(date +%s)
t=$((fin-begin))
echo "Finished building image $IMAGELOC in $t seconds"

