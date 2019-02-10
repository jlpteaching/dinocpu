# Davis In-Order (DINO) CPU models

![Cute Dino](dino-128.png)

This repository contains chisel implementations of the CPU models from Patterson and Hennessy's Computer Organization and Design primarily for use in UC Davis's Computer Architecture course (ECS 154B).

The repository was originally cloned from https://github.com/ucb-bar/chisel-template.git.

Through the course this quarter, we will be building this CPU from the ground up.
You will be provided with some template code which contains a set of interfaces between CPU components and some pre-written components.
You will combine these components together into a working processor!

This is the first time we have used this set of assignments, so there may be mistakes.
To offset this, we will be offering a variety of ways to get extra credit.
See [the extra credit page](../extra-credit.md) for an up-to-date list of ways to get extra credit.
Two of the main ways to earn extra credit is by [submitting issues](issues/) (reporting bugs) and having [pull requests](pulls/) accepted (fixing bugs).

There will also likely be missing documentation.
We have tried our best to document everything clearly, but if there is something missing, create a post on Piazza and we'll answer as quickly as possible.
We will also be updating the [documentation](tree/master/documentation/) frequently.
We will make an announcement of any significant updates.

# Getting the code

To get the code, you can clone the repository that is in jlpteaching: `jlpteaching/dinocpu`.

```
git clone https://github.com/jlpteaching/dinocpu.git
```

## Overview of code

The `src/` directory:

- `main/scala/`
  - `components/`: This contains a number of components that are needed to implement a CPU. You will be filling in some missing pieces to these components in this lab. You can also find all of the interfaces between components defined in this file.
  - `five-cycle/`: This is the code for the five cycle CPU. Right now, this is just an empty template. You will implement this in Lab 3.
  - `pipelined/`: This is the code for the pipelined CPU. Right now, this is just an empty template. You will implement this in Lab 4.
  - `single-cycle/`: This is the code for the single cycle CPU. Right now, this is just an empty template. You will implement this in Lab 2.
  - `configuration.scala`: Contains a simple class to configure the CPU. **Do not modify.**
  - `elaborate.scala`: Contains a main function to output FIRRTL- and Verilog-based versions of the CPU design. You can use this file by executing `runMain dinocpu.elaborate` in `sbt`. More details below. **Do not modify.**
  - `simulate.scala`: Contains a main function to simulate your CPU design. This simulator is written in Scala using the [Treadle executing engine](https://github.com/freechipsproject/treadle). You can execute the simulator by using `runMain dinocpu.simulate` from sbt. This will allow you to run *real RISC-V binaries* on your CPU design. More detail about this will be given in Lab 2. **Do not modify.**
  - `top.scala`: A simple Chisel file that hooks up the memory to the CPU. **Do not modify.**
- `test/`
  - `java/`: This contains some Gradescope libraries for automated grading. **Feel free to ignore.**
  - `resources/riscv`: Test RISC-V applications that we will use to test your CPU design and that you can use to test your CPU design.
  - `scala/`
    - `components/`: Tests for the CPU components/modules. **You may want to add additional tests here. Feel free to modify, but do not submit!**
    - `cpu-tests/`: Tests the full CPU design. **You may want to add additional tests here in future labs. Feel free to modify, but do not submit!**
    - `grading/`: The tests that will be run on Gradescope. Note: these won't work unless you are running inside the Gradescope docker container. They should match the tests in `components` and `cpu-tests`. **Do not modify.** (You *can* modify, but it will be ignored when uploading to Gradescope.)

# How to run

There are three primary ways to interact with the DINO CPU code.
1. Running the DINO CPU tests.
2. Running the DINO CPU simulator.
3. Compiling the Chisel hardware description code into Verilog.

## Running the DINO CPU tests

In this class, running the tests will be the primary way you will interact with DINO CPU.
We have set the repository up so that you can use the REPL sbt interface to run the tests.
In fact, for each part of each lab, there is a specific test that you will run.

To run a test, first start the sbt REPL interface.
If you are using singularity you can run the following command:

```
singularity run library://jlowepower/default/dinocpu
```

You should see the following:

```
[info] Loading settings for project global-plugins from idea.sbt ...
[info] Loading global plugins from /home/jlp/.sbt/1.0/plugins
[info] Loading settings for project dinocpu-build from plugins.sbt ...
[info] Loading project definition from /home/jlp/Code/chisel/dinocpu/project
[info] Loading settings for project root from build.sbt ...
[info] Set current project to dinocpu (in build file:/home/jlp/Code/chisel/dinocpu/)
[info] sbt server started at local:///home/jlp/.sbt/1.0/server/054be10b189c032c309d/sock
sbt:dinocpu>
```

Now, you can run the command `test` to run *all of the tests* on the DINO CPU.
When running in the template repository or in a specific lab branch, most (or all) of the tests will fail until you implement the requirements of that lab.

There are three environments to run the tests:

1. The default `Test` environment.
This is the default environment, and the environment active when you simply run `test`.
For each lab, this will be the LabX environment.
For the master branch, this is the `TestAll` environment that contains all of the tests.
2. The `LabX` environment.
There is a specific set of tests for each lab. For instance, when you use the `Lab1` environment you will just run the tests for Lab 1.
3. The `TestAll` environment contains all of the tests.

To run tests in a specific environment prepend the environment name and `/` before the `test` command.
As an example, to run the Lab 1 tests:

```
dinocpu:sbt> Lab1 / Test
```

### Running a single test

The tests are grouped together into "suites".
For the labs, each part of the lab is a single suite.
To run one suite, use `testOnly` instead of `test`.

For instance, to run just the ALU Control tests you can use the following.

```
sbt:dinocpu> testOnly dinocpu.ALUControlTester
```

Note: You can use tab completion in sbt to make searching for tests easier.

Each of these suites runs a number of different tests.
When trying to debug a test, you will often want to run just a single test case.
To do that, you can use full text search on the name of the test.
You can pass `-z <search>` to the tester.
**Important**: This must be after `--` to distinguish the parameters.

The name of the test is printed when you run the test.
For instance, for the ALU control tester you will see the following output.

```
[info] [0.001] Elaborating design...
[info] [0.081] Done elaborating.
Total FIRRTL Compile Time: 175.6 ms
Total FIRRTL Compile Time: 16.1 ms
End of dependency graph
Circuit state created
[info] [0.000] SEED 1547141675482
test ALUControl Success: 21 tests passed in 26 cycles taking 0.024443 seconds
[info] [0.015] RAN 21 CYCLES PASSED
[info] ALUControlTester:
[info] ALUControl
[info] - should match expectations for each intruction type
[info] ScalaTest
[info] Run completed in 649 milliseconds.
[info] Total number of tests run: 1
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[info] Passed: Total 1, Failed 0, Errors 0, Passed 1
[success] Total time: 1 s, completed Jan 10, 2019 5:34:36 PM
```

You can search in "should match expectations for each intruction type" (ignore the typo).

So, let's say you only want to run the single cycle CPU test which executes the `add1` application, you can use the following.

```
sbt:dinocpu> testOnly dinocpu.SingleCycleCPUTester -- -z add1
```

## Running the simulator

More info on this coming soon.

You can use pipe to automatically convert the `DASM` statements as the emulator is running.

```
./emulator +verbose 2>&1 | $RISV/bin/spike-dsm
```

## Compiling into Verilog

More info on this coming soon.


## Getting baremetal programs working

Here's an example baremetal program that loads -1.
We have to have the `_start` symbol declared or the linker gets mad at us.

```
  .text
  .align 2       # Make sure we're aligned to 4 bytes
  .globl _start
_start:
    lw t0, 0x100(zero)   # t0 (x5) <= mem[0x100]

  .data
.byte 0xFF,0xFF,0xFF,0xFF
```

### STEP 1: Assemble

Now, to compile, first we have to assemble it.

```
riscv32-unknown-elf-as -march=rv32i src/main/risc-v/test.riscv -o test.o
```

### STEP 2: Link

Then, we have to link it.
When linking, we have to specify that the text should be located at `0x00` and that the data should be located at `0x400`.
We chose `0x400` arbitrarily.
This can be anything within the size of memory (16 KB by default).

```
riscv32-unknown-elf-ld  -Ttext 0x0 -Tdata 0x400 test.o -o test
```

### STEP 3: Check output

Finally, we can disassemble it to make sure it looks right.

```
riscv32-unknown-elf-objdump -Dx test
```

We get the following:

```
lw1:     file format elf32-littleriscv
lw1
architecture: riscv:rv32, flags 0x00000112:
EXEC_P, HAS_SYMS, D_PAGED
start address 0x00000000

Program Header:
    LOAD off    0x00001000 vaddr 0x00000000 paddr 0x00000000 align 2**12
         filesz 0x00000404 memsz 0x00000404 flags rwx

Sections:
Idx Name          Size      VMA       LMA       File off  Algn
  0 .text         00000018  00000000  00000000  00001000  2**2
                  CONTENTS, ALLOC, LOAD, READONLY, CODE
  1 .data         00000004  00000400  00000400  00001400  2**0
                  CONTENTS, ALLOC, LOAD, DATA
SYMBOL TABLE:
00000000 l    d  .text  00000000 .text
00000400 l    d  .data  00000000 .data
00000000 l    df *ABS*  00000000 lw1.o
00000018 l       .text  00000000 _last
00000c04 g       .data  00000000 __global_pointer$
00000000 g       .text  00000000 _start
00000404 g       .data  00000000 __bss_start
00000404 g       .data  00000000 _edata
00000404 g       .data  00000000 _end



Disassembly of section .text:

00000000 <_start>:
   0:   40002283                lw      t0,1024(zero) # 400 <_last+0x3e8>
   4:   00000013                nop
   8:   00000013                nop
   c:   00000013                nop
  10:   00000013                nop
  14:   00000013                nop

Disassembly of section .data:

00000400 <__bss_start-0x4>:
 400:   ffff                    0xffff
 402:   ffff                    0xffff

```

## sbt hints


# Testing for grading

You can run the test and have it output a junit compatible xml file by appending the following after `sbt test` or `sbt testOnly <test>`

```
-- -u <directory>
```

However, we now have Gradescope support built in, so there's no need to do the above.

## How to do it

If you run the test under the "Grader" config, you can run just the grading scripts.
This assumes that you are running inside the gradescope docker container.

```
sbt "Grader / test"
```

### Updating the docker image

```
docker build -f Dockerfile.gradescope -t jlpteaching/dino-grading:labX .
```

Make sure that you have checked out the labX branch before building the docker file so you only include the template code and not the answer.

To test the grade image, run

```
docker run --rm -w $PWD -v $PWD:$PWD -it jlpteaching/dinocpu-grading:labX bash
```

In the dinocpu directory, check out the master branch (which has the correct solution).

Then, create the following directories in the container image.

```
mkdir /autograder/submission
mkdir /autograder/results
```

Copy the correct files into `/autograder/submission`.
Note, this varies for each lab.

Then, `cd` to `/autograder` and run `./run_autograder`.
This should produce a `results.json` filr in `/autograder/results` and print to the screen that all tests passed.

# Getting started

- I suggest install intellj with the scala plug in. On Ubuntu, you can install this as a snap so you don't have to fight java versions.
  - To set this up you have to point it to a jvm. Mine was /usr/lib/jvm/<jvm version>

# Using this with singularity

[Singularity](https://www.sylabs.io/singularity/) is yet another container (LXC) wrapper.
It's somewhat like docker, but it's built for scientific computing instead of microservices.
The biggest benefit is that you can "lock" an image instead of depending on docker's layers which can constantly change.
This allows you to have much more reproducible containers.

However, this reproducibility comes at a cost of disk space.
Each image is stored in full in the current working directory.
This is compared to docker which only stores unique layers and stores them in /var instead of the local directory.

The other benefit of singularity is that it's "safer" than docker.
Instead of having to run everything with root privileges like in docker, with singularity you still run with your user permissions.
Also, singularity does a better job at automatically binding local directories than docker.
[By default](https://singularity.lbl.gov/docs-mount#system-defined-bind-points) it binds `$HOME`, the current working directory, and a few others (e.g., `/tmp`, etc.)

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

# Example debugging

First, I tried to run the test:

```
testOnly dinocpu.ImmediateSimpleCPUTester
```

When this ran, I received the output:

```
[info] ImmediateSimpleCPUTester:
[info] Simple CPU
[info] - should run auipc0 *** FAILED ***
[info]   false was not true (ImmediateTest.scala:33)
[info] Simple CPU
[info] - should run auipc1 *** FAILED ***
[info]   false was not true (ImmediateTest.scala:33)
[info] Simple CPU
[info] - should run auipc2 *** FAILED ***
[info]   false was not true (ImmediateTest.scala:33)
[info] Simple CPU
[info] - should run auipc3 *** FAILED ***
[info]   false was not true (ImmediateTest.scala:33)
[info] Simple CPU
[info] - should run lui0
[info] Simple CPU
[info] - should run lui1 *** FAILED ***
[info]   false was not true (ImmediateTest.scala:33)
[info] Simple CPU
[info] - should run addi1 *** FAILED ***
[info]   false was not true (ImmediateTest.scala:33)
[info] Simple CPU
[info] - should run addi2 *** FAILED ***
[info]   false was not true (ImmediateTest.scala:33)
[info] ScalaTest
[info] Run completed in 5 seconds, 392 milliseconds.
[info] Total number of tests run: 8
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 7, canceled 0, ignored 0, pending 0
[info] *** 7 TESTS FAILED ***
[error] Failed: Total 8, Failed 7, Errors 0, Passed 1
[error] Failed tests:
[error]         dinocpu.ImmediateSimpleCPUTester
[error] (Test / testOnly) sbt.TestsFailedException: Tests unsuccessful
[error] Total time: 7 s, completed Jan 1, 2019 11:38:09 PM
```

Now, I am going to dive into the `auipc` instruction.

So, I need to run the simulator.
The simulator takes two parameters, the RISC binary and the CPU type.
So, to run with the `auipc` workload on the single cycle CPU I would use the following:

```
runMain dinocpu.simulate src/test/resources/risc-v/auipc0 single-cycle --max-cycles 5
```

Then, I get the following output, which I can step through to find the problem.

```
[info] [0.000] Elaborating design...
[info] [0.017] Done elaborating.
Total FIRRTL Compile Time: 78.6 ms
Total FIRRTL Compile Time: 119.4 ms
file loaded in 0.153465121 seconds, 458 symbols, 393 statements
DASM(537)
CYCLE=1
pc: 4
control: Bundle(opcode -> 55, branch -> 0, memread -> 0, memtoreg -> 0, memop -> 0, memwrite -> 0, regwrite -> 1, alusrc2 -> 1, alusrc1 -> 1, jump -> 0)
registers: Bundle(readreg1 -> 0, readreg2 -> 0, writereg -> 10, writedata -> 0, wen -> 1, readdata1 -> 0, readdata2 -> 0)
aluControl: Bundle(memop -> 0, funct7 -> 0, funct3 -> 0, operation -> 2)
alu: Bundle(operation -> 2, inputx -> 0, inputy -> 0, result -> 0)
immGen: Bundle(instruction -> 1335, sextImm -> 0)
branchCtrl: Bundle(branch -> 0, funct3 -> 0, inputx -> 0, inputy -> 0, taken -> 0)
pcPlusFour: Bundle(inputx -> 0, inputy -> 4, result -> 4)
branchAdd: Bundle(inputx -> 0, inputy -> 0, result -> 0)
```

Also, I could have just run one of the tests that were failing by using `-z` when running `testOnly` like the following:

```
testOnly dinocpu.ImmediateSimpleCPUTester -- -z auipc0
```

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

# To distribute template code for students

1. Check out the branch for the lab you want to distribute
2. Delete the history: `rm -rf .git`
3. Initialize the git repo: `git init`
4. Add all of the files: `git add .`
5. Make the commit: `git commit -m "Initial commit of template code"`
6. Add the template repo (e.g., `git remote add origin git@github.com:jlpteaching/dinocpu`)
7. Push the code: `git push -f origin master`

## To update the template code

1. Make changes in private repo to the labX branch. Note, you will probably want to make changes to the master branch then merge the labX branch.
2. Copy all changes over to the template repo: `cp -ru dinoprivate/* .`
3. Commit the changes to the template repo: `git commit`. You probably should comment on the git changeset hash is from the private repo in the commit message.
4. Push the code: `git push -f origin master`

**Note:** *If you know of a better way to do this, I'm all ears.*
