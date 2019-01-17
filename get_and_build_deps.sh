#!/bin/bash
# This check out all of the master branches of the projects that this depends
# on and then installs them to /opt/ivy2/local
# I'm sure there's a better way to do this with sbt, but I need to get something
# working.

pushd /opt

git clone https://github.com/freechipsproject/firrtl.git
git checkout df3a34f01d227ff9ad0e63a41ff10001ac01c01d
git clone https://github.com/freechipsproject/firrtl-interpreter.git
git checkout df3a34f01d227ff9ad0e63a41ff10001ac01c01d
git clone https://github.com/freechipsproject/chisel3.git
git checkout 685790b2c6c7ff8ddfd34f2f84572a985d3416cc
git clone https://github.com/freechipsproject/chisel-testers.git
git checkout 8a737012f3c93dd41a3a9e120eebfc07ba541ebf
git clone https://github.com/freechipsproject/treadle.git
git checkout a10de08ff4dca3f5db44a665ca807d4457f0f10e

# Now, compile each and install to /opt/ivy2/local
cd firrtl && \
sbt -ivy /opt/ivy2 compile && sbt -ivy /opt/ivy2 publishLocal && \
cd ../firrtl-interpreter && \
sbt -ivy /opt/ivy2 compile && sbt -ivy /opt/ivy2 publishLocal && \
cd ../chisel3 && \
sbt -ivy /opt/ivy2 compile && sbt -ivy /opt/ivy2 publishLocal && \
cd ../chisel-testers && \
sbt -ivy /opt/ivy2 compile && sbt -ivy /opt/ivy2 publishLocal && \
cd ../treadle && \
sbt -ivy /opt/ivy2 compile && sbt -ivy /opt/ivy2 publishLocal && \
cd ..

rm -rf firrtl firrtl-interpreter chisel3 chisel-testers treadle
rm -rf /opt/ivy2/cache

popd
