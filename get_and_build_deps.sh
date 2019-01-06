# This checks out all of the master branches of the projects that this depends
# on and then installs them to /opt/ivy2/local
# I'm sure there's a better way to do this with sbt, but I need to get something
# working.

git clone https://github.com/freechipsproject/firrtl.git
git clone https://github.com/freechipsproject/firrtl-interpreter.git
git clone https://github.com/freechipsproject/chisel3.git
git clone https://github.com/freechipsproject/chisel-testers.git
git clone https://github.com/freechipsproject/treadle.git

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
