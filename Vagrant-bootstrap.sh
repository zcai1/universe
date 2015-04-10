sudo apt-get update
sudo apt-get -yu dist-upgrade
sudo apt-get install -y openjdk-7-jdk mercurial ant gradle hevea librsvg2-bin zip unzip

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}

export JSR308=/home/vagrant/jsr308
mkdir -p $JSR308
cd $JSR308

if [ ! -d $JSR308/jsr308-langtools ]; then
  hg clone https://code.google.com/p/jsr308-langtools/ jsr308-langtools;
fi

if [ ! -d $JSR308/plume-lib ]; then
  hg clone https://code.google.com/p/plume-lib/ plume-lib;
fi

if [ ! -d $JSR308/annotation-tools ]; then
  hg clone https://code.google.com/p/annotation-tools/ annotation-tools;
fi

if [ ! -d $JSR308/checker-framework ]; then
  hg clone https://code.google.com/p/checker-framework/ checker-framework;
fi

if [ ! -d $JSR308/checker-framework-inference ]; then
  hg clone https://code.google.com/p/checker-framework-inference/ checker-framework-inference;
fi

if [ ! -d $JSR308/universe ]; then
  hg clone https://bitbucket.org/wmdietl/universe/ universe;
fi

cd $JSR308/jsr308-langtools/make
ant

cd $JSR308/plume-lib
make

cd $JSR308/annotation-tools
ant

cd $JSR308/checker-framework
ant

cd $JSR308/checker-framework-inference
gradle dist
