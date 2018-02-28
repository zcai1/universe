#!/bin/bash

# Keep the environment settings from setup.sh script
export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

# dist directory of CheckerFramework Inference
distDir=$CFI/dist

# Start the inference: jar files are used when making inference
java -cp /home/mier/jsr308/jdepend/inferDebugBuild:"$distDir"/checker.jar:"$distDir"/plume.jar:"$distDir"/checker-framework-inference.jar:/home/mier/Downloads/jodconverter/jodconverter-core/target/classes:/home/mier/.m2/repository/commons-io/commons-io/2.4/commons-io-2.4.jar:/home/mier/.m2/repository/org/openoffice/juh/4.1.2/juh-4.1.2.jar:/home/mier/.m2/repository/org/openoffice/jurt/4.1.2/jurt-4.1.2.jar:/home/mier/.m2/repository/org/openoffice/ridl/4.1.2/ridl-4.1.2.jar:/home/mier/.m2/repository/org/openoffice/unoil/4.1.2/unoil-4.1.2.jar:/home/mier/.m2/repository/commons-cli/commons-cli/1.2/commons-cli-1.2.jar:/home/mier/.m2/repository/org/hyperic/sigar/1.6.4/sigar-1.6.4.jar:/home/mier/.m2/repository/org/json/json/20140107/json-20140107.jar:$CLASSPATH checkers.inference.InferenceLauncher --checker GUTI.GUTIChecker --solver checkers.inference.solver.DebugSolver --hacks=true --afuOutputDir=./annotated -m ROUNDTRIP $*
