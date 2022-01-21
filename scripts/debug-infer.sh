#!/bin/bash
set -e

export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

CHECKER=universe.UniverseInferenceChecker

IS_HACK=true

DEBUG_SOLVER=checkers.inference.solver.DebugSolver
SOLVER="$DEBUG_SOLVER"

$CFI/scripts/inference-dev --checker "$CHECKER" --solver "$SOLVER" --solverArgs="collectStatistics=true" --hacks="$IS_HACK" -m ROUNDTRIP -afud ./annotated "$@"
