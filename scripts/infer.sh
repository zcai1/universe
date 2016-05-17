#!/bin/bash

export MYDIR=`dirname $0`
. $MYDIR/setup.sh

java -ea -Xms512m -Xmx2048m -Xbootclasspath/p:$JSR308/checker-framework-inference/dist/json-simple-1.1.1.jar:$JSR308/checker-framework-inference/dist/mockito-all-1.8.4.jar:$CLASSPATH -ea -ea:checkers.inference... checkers.inference.InferenceLauncher --checker GUTI.GUTIChecker --solver GUTI.ConstraintGUTISolver  --log-level INFO --bootclasspath $JSR308/checker-framework-inference/dist/jdk8.jar $*
