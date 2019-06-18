# Singularity implementation details

## Building a singularity image

To build the singularity image

```
sudo singularity build dinocpu.sif dinocpu.def
```

## To run chisel with singularity

The `dinocpu.def` file specifies `sbt` as the runscript (entrypoint in docker parlance).
Thus, you can simply `run` the image and you'll be dropped into the `sbt` shell.

Currently, the image is stored in the [singularity cloud](https://cloud.sylabs.io/library) under `jlowepower/default/dinocpu`.
This might change in the future.

To run this image directly from the cloud, you can use the following command.

```
singularity run library://jlowepower/default/dinocpu
```

This will drop you directly into `sbt` and you will be able to run the tests, simulator, compile, etc.

Note: This stores the image in `~/.singularity/cache/library/`.

The first time you run the container, it will take a while to start up.
When you execute `singularity run`, it automatically starts in `sbt`, the [scala build tool](https://www.scala-sbt.org/), which we will use for running Chisel for all of the labs.
The first time you run `sbt`, it downloads all of the dependencies to your local machine.
After the first time, it should start up much faster!

If, instead, you use `singularity pull library://jlowepower/default/dinocpu`, then the image is downloaded to the current working directory.

**Important:** We should discourage students from using `singularity pull` in case we need to update the image!

# How to use specific versions of chisel, firrtl, etc

Clone the repos:

```
git clone https://github.com/freechipsproject/firrtl.git
git clone https://github.com/freechipsproject/firrtl-interpreter.git
git clone https://github.com/freechipsproject/chisel3.git
git clone https://github.com/freechipsproject/chisel-testers.git
git clone https://github.com/freechipsproject/treadle.git
```

Compile each by running `sbt compile` in each directory and then publish it locally.

```
cd firrtl && \
sbt compile && sbt publishLocal && \
cd ../firrtl-interpreter && \
sbt compile && sbt publishLocal && \
cd ../chisel3 && \
sbt compile && sbt publishLocal && \
cd ../chisel-testers && \
sbt compile && sbt publishLocal && \
cd ../treadle && \
sbt compile && sbt publishLocal && \
cd ..
```

By default, this installs all of these to `~/.ivy2/local`.
You can change this path by specifying `-ivy` on the sbt command line.

```
`sbt -ivy /opt/ivy2`
```

However, you only want to do that while building installing.
Once installed, now you have an ivy repository at /opt/ivy2.
We want to use that as one of the resolvers in the `build.sbt` file.
It's important not to use `-ivy /opt/ivy2` in the singularity file as it writes that location when in use.
