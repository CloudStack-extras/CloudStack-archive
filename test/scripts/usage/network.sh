#!/bin/bash



  #
  # Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
  #
 

#set -x
iteration=$(mysql -h $1 --user=root --skip-column-names -U cloud_usage -e "select count(*) from user_statistics;")
echo "Bytes received:"
for ((i=3; i<$iteration; i++))
do
cloud_usage=$(mysql -h $1 --user=root --skip-column-names -U cloud_usage -e "select SUM(raw_usage) from cloud_usage where account_id=$i and usage_type=5;")
if [ "$cloud_usage" = "NULL" ]
then
cloud_usage=0
fi

user_stat=$(mysql -h $1 --user=root --skip-column-names -U cloud_usage -e "select net_bytes_received+current_bytes_received from user_statistics where account_id=$i;")

if [ "$user_stat" = "" ]
then
user_stat=0
fi

temp=`expr $user_stat - $cloud_usage`
if [ $temp -ne 0 ]
then
echo "For account $i difference in bytes_received is $temp"
fi
done

echo "\n"
echo "Bytes sent:"
for ((i=3; i<$iteration; i++))
do
cloud_usage=$(mysql -h $1 --user=root --skip-column-names -U cloud_usage -e "select SUM(raw_usage) from cloud_usage where account_id=$i and usage_type=4;")

if [ "$cloud_usage" = "NULL" ]
then
cloud_usage=0
fi

user_stat=$(mysql -h $1 --user=root --skip-column-names -U cloud_usage -e "select net_bytes_sent+current_bytes_sent from user_statistics where account_id=$i;")

if [ "$user_stat" = "" ]
then
user_stat=0
fi

temp=`expr $user_stat - $cloud_usage`
if [ $temp -ne 0 ]
then
echo "For account $i difference in bytes_sent is $temp"
fi
done
