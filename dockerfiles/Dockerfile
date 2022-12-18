FROM ubuntu:22.04
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get -y update; apt-get -y upgrade
RUN apt-get -y install curl gnupg wget openjdk-11-jdk-headless

# From https://github.com/freechipsproject/chisel-template/commit/828ac6131210b1ff2a54eb09a348333982303cca
# To use Chisel 3.5.4, we might want to use Scala 2.13.8 and OpenJDK 11
#RUN wget https://downloads.lightbend.com/scala/2.13.8/scala-2.13.8.tgz && \
#    tar xf scala-2.13.8.tgz
RUN wget https://downloads.lightbend.com/scala/2.13.8/scala-2.13.8.deb && \
    dpkg -i scala-2.13.8.deb && \
    rm scala-2.13.8.deb

# From https://www.scala-sbt.org/download.html
# https://askubuntu.com/questions/1286545/what-commands-exactly-should-replace-the-deprecated-apt-key
# replacing 'deb https://' in the source lists by 'deb [signed-by=/etc/.../sbt-key.gpg] https://'
RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" > key.gpg; \
    gpg --no-default-keyring --keyring ./temp-keyring.gpg --import key.gpg && \
    gpg --no-default-keyring --keyring ./temp-keyring.gpg --export --output sbt-key.gpg && \
    rm ./temp-keyring.gpg && \
    rm ./key.gpg && \
    mv sbt-key.gpg /etc/apt/keyrings/ && \
    sed -i -e 's/deb\shttps:\/\//deb \[signed-by\=\/etc\/apt\/keyrings\/sbt-key.gpg\] https:\/\//g' /etc/apt/sources.list.d/sbt*.list && \
    apt-get update && \
    apt-get install -y sbt

CMD export HOME=/home/sbt-user && sbt -mem 3072
