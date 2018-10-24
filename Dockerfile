FROM openjdk:8
MAINTAINER Jason Lowe-Power <jason@lowepower.com>

ENV SBT_VERSION 0.13.16

RUN touch /usr/lib/jvm/java-8-openjdk-amd64/release

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb

# Install verilator
RUN \
  apt-get update -y && \
  apt-get install git make autoconf g++ flex bison pkg-config -y

RUN \
  git clone http://git.veripool.org/git/verilator && \
  cd verilator && \
  git checkout verilator_3_904 && \
  autoconf && \
  ./configure && \
  make -j8 && make install && \
  cd .. && rm -rf verilator

RUN \
  git clone https://github.com/codelec/riscv-fesvr && \
  cd riscv-fesvr && \
  git checkout f9754d8db7d8c1bd659f223017873fb6a25f1257 && \
  mkdir build && cd build && \
  ../configure --prefix=/usr && \
  make install && \
  cd ../../ && rm -rf riscv-fesvr

  
