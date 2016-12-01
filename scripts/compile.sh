#!/bin/bash

# Keep the environment settings from setup.sh script
export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

if [ ! -d $UNIVERSE/bin ]
then
mkdir $UNIVERSE/bin
fi

# What does -source 7 -target 7 mean?
javac -cp $CLASSPATH -source 7 -target 7 -d $UNIVERSE/bin `find $UNIVERSE/src -name "*.java"` $*
