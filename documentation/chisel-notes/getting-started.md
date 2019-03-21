---
Author: Jason Lowe-Power
Title: Getting started with Chisel
---

# Using the Singularity image/container

We have created a Singularity container image for you to use for the labs this quarter.
[Singularity](https://www.sylabs.io/singularity/) is a [container](https://linuxcontainers.org/) format similar to [Docker](https://www.docker.com/).
We cannot use Docker on the CSIF machines for security reasons.
We are using containers because the DINO CPU has a number of unique dependencies (e.g., [Chisel](https://chisel.eecs.berkeley.edu/), [FIRRTL](https://bar.eecs.berkeley.edu/projects/firrtl.html), [sbt](https://www.scala-sbt.org/), [Scala](https://www.scala-lang.org/), [Java](https://www.java.com/en/), and many others).
Of course, each of these dependencies requires a specific version to work correctly!
Containers allow us to give you a known-good starting point with the correct versions of all of the dependencies installed.
This also means less hassle on your end attempting to install the correct dependencies!

We may make updates to the Singularity image throughout the quarter.
We have done our best to make sure all of the labs will work with the current image, but there may be unforeseen issues.
Therefore, make sure to always use the "default" version of the image, and always use the image from the library.
Don't download the image locally, as the library version may change.
**We will announce when we push any changes to the image.**

To use the Singularity image, you can simply run the following command:

```
singularity run library://jlowepower/default/dinocpu
```

This will download the most up-to-date version of the image to your local machine (e.g., `~/.singularity/cache` on Linux machines).

The first time you run the container, it will take a while to start up.
When you execute `singularity run`, it automatically starts in `sbt`, the [scala build tool](https://www.scala-sbt.org/), which we will use for running Chisel for all of the labs.
The first time you run `sbt`, it downloads all of the dependencies to your local machine.
After the first time, it should start up much faster!

## Using the CSIF machines

Singularity is installed on the CSIF machines.
So, if you are using one of the CSIF machines either locally or remotely, things should *just work*.
However, if you run into any problems, post on Piazza or come to office hours.

The images are relatively large files.
As of the beginning of the quarter, the image is 380 MB.
We have tried to keep the size as small as possible.
Thus, especially if we update the image throughout the quarter, you may find that the disk space on your CSIF account is full.
If this happens, you can remove the Singularity cache to free up space.

To remove the Singularity cache, you can run the following command.

```
rm -r ~/.singularity/cache
```

To find out how much space the Singularity containers are using, you can use `du` (disk usage):

```
du -sh ~/.singularity/cache
```

You can also download the images to `/tmp`, if you do not have space in your user directory.
Let us know if you would like more details on this method via Piazza.

## Using your own machine

Details on how to install Singularity on your own machine can be found on the [Singularity website](https://www.sylabs.io/guides/3.0/user-guide/installation.html).
It's easiest to install it on Linux, but there are also directions for installing on Windows and MacOS.
On Windows and MacOS, you will have to run a Linux virtual machine to work with the Singularity containers.
**IMPORTANT: If you are using the installation directions for Windows or Mac, make sure to use the version 3.0 Vagrant box!**

For Linux, I suggest using the provided packages, not building from source.
Details are available [here](https://www.sylabs.io/guides/3.0/user-guide/installation.html#install-the-debian-ubuntu-package-using-apt).
**Be sure to use version 3 of Singularity, as it's the only version that supports the Singularity library!**

For Windows and Mac, you can follow [these instructions from Sylabs](https://www.sylabs.io/guides/3.0/user-guide/installation.html#install-on-windows-or-mac), **but read below first**.

We've made a couple of changes to make things easier.
There is a Vagrantfile included in the DINO CPU repository for you to use.
Thus, the steps are as follows:

On Windows:
1) Install [VirtualBox](https://www.virtualbox.org/wiki/Downloads).
2) Install [Vagrant](https://www.vagrantup.com/downloads.html).

On Mac:
1) Install Homebrew:
```
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
```
2) Install VirtualBox and Vagrant:
```
brew cask install virtualbox && \
    brew cask install vagrant && \
    brew cask install vagrant-manager
```
3) **Do not follow the directions to create the Singularity Vagrant Box on sylabs.io.** Instead, simply run the following *in the `dinocpu` directory*:
```
vagrant up
vagrant ssh
```

The first line, `vagrant up`, starts the virtual machine.
You may have to run this whenever you restart your computer or you kill your running virtual machine.
(See the [Vagrant documentation](https://www.vagrantup.com/docs/) for more information.)
The second line, `vagrant ssh`, starts an SSH session from your host to the running virtual machine.
In this SSH session, when you're running on the virtual machine, you will be able to use the `singularity` command.

Thus, after running `vagrant ssh`, you can then follow the directions below in the [Using Scala, sbt, etc. section](#using-scala-sbt-etc).
Note: If you use your own `Vagrantfile`, you will find the contents of the current working directory on the host in `/vagrant` on the guest.
In the `Vagrantfile` in the DINO CPU repository, we have mapped the the `dinocpu` directory to `/home/vagrant/dinocpu` to make things easier.

When using Vagrant, Singularity, and sbt, the first time you run everything, it will take some time.
All of these tools automatically download things from the Internet.
The total download is on the order of a gigabyte.
Be patient while everything is getting set up.
After the first time you run everything, it should be *much quicker* since the downloaded files should be cached.

**We will only support using the provided Singularity container!**
At your own risk, you can try to install the required dependencies.
However, we will not support this.
We will give priority to all other questions on Piazza and in office hours before we help you get set up without using the Singularity container.

## Common Vagrant problems

- On Windows, if you receive an error saying that SSH cannot establish a connection, you may need to enable virtualization in your BIOS.

# Using Scala, sbt, etc.

- How to use the sbt REPL interface
- Run tests, main, etc.

To start sbt in the REPL (Read-Eval-Print Loop), run the following code in the base DINO directory.

```
singularity run library://jlowepower/default/dinocpu
```

[Next: Creating your first Chisel hardware](first-hardware.md)
