#!/bin/bash

export MYDIR=`dirname $0`
. $MYDIR/setup.sh

$JAVAC -processor org.checkerframework.checker.compilermsgs.CompilerMessagesChecker -Apropfiles=$MYDIR/../src/GUT/messages.properties $MYDIR/../src/GUT/*.java

