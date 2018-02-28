#!/bin/bash

# Keep the environment settings from setup.sh script
export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

# dist directory of CheckerFramework Inference
distDir=$CFI/dist

# Start the inference: jar files are used when making inference
java -cp /home/mier/jsr308/jdepend/inferDebugBuild:/home/mier/Downloads/jodconverter/jodconverter-core/target/classes:"$distDir"/checker.jar:"$distDir"/plume.jar:"$distDir"/checker-framework-inference.jar:$CLASSPATH checkers.inference.InferenceLauncher --checker universe.GUTChecker --solver universe.solver.GUTSolverEngine --solverArgs=useGraph=false,collectStatistic=true --hacks=true --afuOutputDir=./annotated -m ROUNDTRIP $*
