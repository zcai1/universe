#!/bin/bash
set -e

export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

echo $CLASSPATH

DEBUG=""
CHECKER="universe.UniverseChecker"

POSITIONAL=()
while [[ $# -gt 0 ]]; do
  key="$1"

  case $key in
    -d|--debug)
      ipaddr="$2"
      DEBUG="-J-Xdebug -J-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=${ipaddr}"
      shift # past argument
      shift # past value
      ;;
    *)    # unknown option
      POSITIONAL+=("$1") # save it in an array for later
      shift # past argument
      ;;
  esac
done

set -- "${POSITIONAL[@]}"

cmd=""

if [ "$DEBUG" == "" ]; then
	cmd="javac -cp "${CLASSPATH}" -processor "${CHECKER}" "$@""
else
	cmd="javac "$DEBUG" -cp "${CLASSPATH}" -processor "${CHECKER}" -AatfDoNotCache "$@""
fi

echo "$cmd"
eval "$cmd"
