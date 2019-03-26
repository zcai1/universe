#!/bin/bash
# Fail the whole script if any command fails
set -e

# Environment variables setup
export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}
export JSR308=$(cd $(dirname "$0")/.. && pwd)
export AFU=$JSR308/annotation-tools/annotation-file-utilities
export CHECKERFRAMEWORK=$JSR308/checker-framework
export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

#default value is opprop. REPO_SITE may be set to other value for travis test purpose.
export REPO_SITE=baoruiz

echo "------ Downloading everthing from REPO_SITE: $REPO_SITE ------"

if [ ! -d $JSR308/annotation-tools ] ; then
    (cd $JSR308 && git clone https://github.com/typetools/annotation-tools.git)
    (cd $JSR308/annotation-tools && git checkout 643077d70129192e52926b3df8838ae15a0fcd4f)
fi

# Clone stubparser
if [ -d $JSR308/stubparser ] ; then
    (cd $JSR308/stubparser && git pull)
else
    (cd $JSR308 && git clone --depth 1 https://github.com/topnessman/stubparser.git)
fi
# Clone checker-framework
if [ -d $JSR308/checker-framework ] ; then
    (cd $JSR308/checker-framework && git checkout pull-pico-changes && git pull)
else
    (cd $JSR308 && git clone -b pull-pico-changes --depth 1 https://github.com/"$REPO_SITE"/checker-framework.git)
fi

# Clone checker-framework-inference
if [ -d $JSR308/checker-framework-inference ] ; then
    (cd $JSR308/checker-framework-inference && git checkout pull-pico-changes && git pull)
else
    (cd $JSR308 && git clone -b pull-pico-changes --depth 1 https://github.com/"$REPO_SITE"/checker-framework-inference.git)
fi

# Download jsr308-langtools replacement for javac.jar that fixes some bugs
if [ ! -d $JSR308/jsr308-langtools ] ; then
  (cd .. && wget -q https://checkerframework.org/jsr308/jsr308-langtools-2.4.0.zip)
  (cd .. && unzip -q jsr308-langtools-2.4.0.zip)
  (cd .. && mv jsr308-langtools-2.4.0 jsr308-langtools)
fi

# Build annotation-tools
(cd $JSR308/annotation-tools && ./.travis-build-without-test.sh)
# Build stubparser
(cd $JSR308/stubparser/ && mvn package -Dmaven.test.skip=true)
# Build checker-framework, with downloaded jdk
(cd $JSR308/checker-framework && ./gradlew assemble)
# Build checker-framework-inference
(cd $JSR308/checker-framework-inference && ./gradlew dist) # This step needs to be manually in $CFI executed due to path problems

# Build GUT
(cd $JSR308/universe && ./gradlew build)
