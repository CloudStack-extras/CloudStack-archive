#!/bin/bash
#
# @VERSION@

cert="/root/.ssh/id_rsa.cloud"
domr=$1
shift
ssh -p 3922 -o StrictHostKeyChecking=no -i $cert root@$domr "/opt/cloud/bin/elastic_ip.sh $*" >/dev/null

exit $?
