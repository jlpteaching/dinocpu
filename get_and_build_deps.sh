#!/bin/bash
# This check out all of the master branches of the projects that this depends
# on and then installs them to /opt/ivy2/local
# I'm sure there's a better way to do this with sbt, but I need to get something
# working.

pushd /opt

git clone https://github.com/freechipsproject/firrtl.git
git clone https://github.com/freechipsproject/firrtl-interpreter.git
git clone https://github.com/freechipsproject/chisel3.git
git clone https://github.com/freechipsproject/chisel-testers.git
git clone https://github.com/freechipsproject/treadle.git

# Now, compile each and install to /opt/ivy2/local
cd firrtl && \
git checkout df3a34f01d227ff9ad0e63a41ff10001ac01c01d && \
sbt -ivy /opt/ivy2 compile && sbt -ivy /opt/ivy2 publishLocal

cd ../firrtl-interpreter && \
git checkout e9d2939206573e398f04559b0ae314fc62bd38f7 && \
sbt -ivy /opt/ivy2 compile && sbt -ivy /opt/ivy2 publishLocal

cd ../chisel3 && \
git checkout 685790b2c6c7ff8ddfd34f2f84572a985d3416cc && \
sbt -ivy /opt/ivy2 compile && sbt -ivy /opt/ivy2 publishLocal

cd ../treadle && \
git checkout a10de08ff4dca3f5db44a665ca807d4457f0f10e && \
sbt -ivy /opt/ivy2 compile && sbt -ivy /opt/ivy2 publishLocal

cd ../chisel-testers && \
git checkout 8a737012f3c93dd41a3a9e120eebfc07ba541ebf && \
sbt -ivy /opt/ivy2 compile && sbt -ivy /opt/ivy2 publishLocal

cd ..

rm -rf firrtl firrtl-interpreter chisel3 chisel-testers treadle
rm -rf /opt/ivy2/cache

popd
