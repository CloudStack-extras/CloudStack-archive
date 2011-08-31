#!/usr/bin/env bash



  #
  # Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
  #
 

# run the usage server

PATHSEP=':'
if [[ $OSTYPE == "cygwin" ]]; then
  PATHSEP=';'
  export CATALINA_HOME=`cygpath -m $CATALINA_HOME`
fi

CP=./$PATHSEP$CATALINA_HOME/webapps/client/WEB-INF/lib/vmops-server.jar
CP=${CP}$PATHSEP$CATALINA_HOME/webapps/client/WEB-INF/lib/vmops-server-extras.jar
CP=${CP}$PATHSEP$CATALINA_HOME/webapps/client/WEB-INF/lib/vmops-utils.jar
CP=${CP}$PATHSEP$CATALINA_HOME/webapps/client/WEB-INF/lib/vmops-core.jar
CP=${CP}$PATHSEP$CATALINA_HOME/webapps/client/WEB-INF/lib/vmops-usage.jar
CP=${CP}$PATHSEP$CATALINA_HOME/conf

for file in $CATALINA_HOME/lib/*.jar; do
  CP=${CP}$PATHSEP$file
done

#echo CP is $CP
DEBUG_OPTS=
#DEBUG_OPTS=-Xrunjdwp:transport=dt_socket,address=$1,server=y,suspend=n

java -cp $CP $DEBUG_OPTS -Dcatalina.home=${CATALINA_HOME} -Dpid=$$ com.vmops.usage.UsageServer $*
