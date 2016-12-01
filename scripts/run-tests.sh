#!/bin/bash

export MYDIR=`dirname $0`
. $MYDIR/setup.sh

java -cp $CLASSPATH GUT.GUTTests
