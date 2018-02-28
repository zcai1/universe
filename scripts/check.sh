#!/bin/bash

# Keep the environment settings from setup.sh script
export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

export CLASSPATH=./src/main/java:$CLASSPATH

javac -processor universe.GUTChecker $*

#-J-Xdebug -J-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5050
