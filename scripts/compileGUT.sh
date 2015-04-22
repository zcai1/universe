#!/bin/bash

export MYDIR=`dirname $0`
. $MYDIR/setup.sh

$JAVAC -source 7 -target 7 -d $MYDIR/../bin/ `find $MYDIR/../src/ -name "*.java"`
$SCALAC -d $MYDIR/../bin/ `find $MYDIR/../src/ -name "*.scala"` `find $MYDIR/../src/ -name "*.java"` 

