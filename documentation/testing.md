---
Author: Jason Lowe-Power
Title: Testing DINO CPU
---

# Testing DINO CPU

This file explains how to run the tests, how to create unit tests, how to create instruction directed tests, and how to run the simulator.

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
dinocpu:sbt> Lab1 / test
```

## Running a single test

The tests are grouped together into "suites".
For the labs, each part of the lab is a single suite.
To run one suite, use `testOnly` instead of `test`.

For instance, to run just the ALU Control tests you can use the following.

```
sbt:dinocpu> testOnly dinocpu.test.components.ALUControlTester
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

You can search in "should match expectations for each instruction type" (ignore the typo).

So, let's say you only want to run the single cycle CPU test which executes the `add1` application, you can use the following.

```
sbt:dinocpu> testOnly dinocpu.test.SingleCycleCPUTester -- -z add1
```

## CPU Test Case

The `InstTests` object in`src/main/scala/testing/InstTests.scala` contains lists of different instruction test cases for use with different CPU models. Each list is a different set of instruction types and corresponds to a RISC-V program in `src/test/resources/risc-v`.

Each test case looks like:
 - binary to run in `src/test/resources/risc-v`
 - number of cycles to run for each CPU type
 - initial values for registers
 - final values to check for registers
 - initial values for memory
 - final values to check for memory
 - extra name information


  ```
 CPUTestCase(  "binary_name",
                Map("single-cycle" -> n_single, "pipelined" -> n_pipelined),
                Map(rs1 -> data1, rs2 -> data2),
                Map(0 -> 0, rd -> (data1 ? data2) , ...),
                Map(), Map())

  ```


add2.riscv :

```
  .text
  .align 2       # Make sure we're aligned to 4 bytes
  .globl _start
_start:
    add a0, s4, t0 # (reg[10] = reg[20] + reg[5])

    nop
    nop
    nop
    nop
    nop
_last:
```

In the CPUTestCase below, we run the binary `add2`, which is compiled from `add2.riscv` (shown above) both of which are in `src/test/resources/risc-v`. The `add2.riscv` adds contents of `t0` (`reg[5]`) and `s4` (`reg[20]`) and stores it in `a0` (`reg[10]`).

  ```
 CPUTestCase(  "add2",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(5 -> 1234, 20 -> 5678),
                Map(0 -> 0, 10 -> 6912),
                Map(), Map())

  ```

  - `Map("single-cycle" -> 1, "pipelined" -> 5)` runs the single-cyle implementation for one cycle and the pipelined implementations for five cycles.
 - `Map(5 -> 1234, 20 -> 5678)` initializes `t0` (`reg[5]`) to 1234 and `s4` (`reg[20]`) to 5678.
 - `Map(0 -> 0, 10 -> 6912)` checks if the contents of `reg[0]` and `a0` are 0 and 6912 respectively.

 # Adding your CPU Test Cases
 ## 1. Editing the given CPU Test Cases:
 Editing the already given CPU Test Cases with new values for initializing amd checking registers is very simple. Consider the test case for `add2.riscv`. If we edit the test case to the following:
   ```
 CPUTestCase(  "add2",
                Map("single-cycle" -> 1, "pipelined" -> 5),
                Map(5 -> 2048, 20 -> 512),
                Map(0 -> 0, 10 -> 2560),
                Map(), Map())

  ```

  The test case now initializes `t0`(`reg[5]`) to 2048, `s4`(`reg[20]`) to 512 and checks if `a0` (`reg[10]`) has the sum 2560 (= 2048 + 512).

  ## 2. Adding new tests:
  ```
  CPUTestCase("add2",
                Map("single-cycle" -> 1, "pipelined" -> 5),
                Map(5 -> 1234, 20 -> 5678),
								Map(0 -> 0, 10 -> 6912),
								Map(), Map()),

  CPUTestCase("add2",
                Map("single-cycle" -> 1, "pipelined" -> 5),
                Map(5 -> 2048, 20 -> 512),
								Map(0 -> 0, 10 -> 2560),
								Map(), Map())
  ```

  Simply copying the CPU Test case as is and adding in changes to register values will throw the error show below.
 ```
 sbt:dinocpu> testOnly dinocpu.test.SingleCycleCPUTester -- -z add
[info] SingleCycleCPUTester:
[info] dinocpu.test.SingleCycleCPUTester *** ABORTED ***
[info]   Duplicate test name: Single Cycle CPU should run rtype add2 (CPUTests.scala:27)
[info] ScalaTest
[info] Run completed in 411 milliseconds.
[info] Total number of tests run: 0
[info] Suites: completed 0, aborted 1
[info] Tests: succeeded 0, failed 0, canceled 0, ignored 0, pending 0
[info] *** 1 SUITE ABORTED ***
[error] Error: Total 1, Failed 0, Errors 1, Passed 0
[error] Error during tests:
[error] 	dinocpu.test.SingleCycleCPUTester
[error] (Test / testOnly) sbt.TestsFailedException: Tests unsuccessful
[error] Total time: 3 s, completed Jan 14, 2019 6:29:23 AM

 ```

 To avoid this add a name to the extra CPU Test Case which distinguishes it from any previous test cases that test for the same instruction in the extra name information field as shown below.

   ```
  CPUTestCase("add2",
                Map("single-cycle" -> 1, "pipelined" -> 5),
                Map(5 -> 1234, 20 -> 5678),
								Map(0 -> 0, 10 -> 6912),
								Map(), Map()),

  CPUTestCase("add2",
                Map("single-cycle" -> 1, "pipelined" -> 5),
                Map(5 -> 2048, 20 -> 512),
								Map(0 -> 0, 10 -> 2560),
								Map(), Map(),"-addnew")
  ```

