#!/bin/bash

export MYDIR=`dirname $0`
. $MYDIR/setup.sh

# echo $CLASSPATH
$JAVA -Xbootclasspath/p:$ROOT/jsr308-langtools/dist/lib/javac.jar -ea GUT.GUTTests
