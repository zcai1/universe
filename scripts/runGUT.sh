#!/bin/bash

export MYDIR=`dirname $0`
. $MYDIR/setup.sh

$JAVAC -J-ea -processor GUT.GUTChecker $*

