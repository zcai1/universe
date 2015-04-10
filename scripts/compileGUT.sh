#!/bin/bash

export MYDIR=`dirname $0`
. $MYDIR/setup.sh

$JAVAC -d $MYDIR/../bin/ `find $MYDIR/../src/ -name "*.java"`
$SCALAC -d $MYDIR/../bin/ `find $MYDIR/../src/ -name "*.scala"` `find $MYDIR/../src/ -name "*.java"` 

