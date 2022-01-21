#!/bin/bash
set -e

WORKING_DIR=$(pwd)

export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

DLJC="$MYDIR"/../../do-like-javac
export AFU="$MYDIR"/../../annotation-tools/annotation-file-utilities
export PATH="$PATH":"$AFU"/scripts

#parsing build command of the target program
build_cmd="$2"
for i in ${@:3}
do
    build_cmd="$build_cmd "${i}""
done

CHECKER="universe.UniverseInferenceChecker"

#Typechecking or inference
if [[ "$1" = "-t" ]] ; then
    echo "Running typechecking"
    #checker tool doesn't support --cfArgs yet, so the arguments don't have effect right now
    running_cmd="python $DLJC/dljc -t checker --checker "${CHECKER}" -o logs2 -- $build_cmd"
elif [[ "$1" = "-i" ]] ; then
    echo "Running inference"
    echo "Cleaning logs and annotated directory from previous result"
    rm -rf logs annotated
    echo "Cleaning Done."
    SOLVER="universe.solver.UniverseSolverEngine"
    running_cmd="python $DLJC/dljc -t inference --checker "${CHECKER}" --solver "${SOLVER}" --solverArgs=\"collectStatistic=true,useGraph=false\" --mode ROUNDTRIP -afud $WORKING_DIR/annotated -o logs -- $build_cmd "
else
    echo "Unknown tool: should be either -t|-i but found: ${1}"
    exit 1
fi

echo "============ Important variables ============="
echo "JSR308: $JSR308"
echo "CLASSPATH: $CLASSPATH"
echo "build cmd: $build_cmd"
echo "running cmd: $running_cmd"
echo "=============================================="

echo "Start Running...."
eval "$running_cmd"
