---
Author: Jason Lowe-Power
Title: Single Stepping through DINO CPU tests
---

# Running a test in single step mode

To run a test in single stepping mode you can use the following command:

```
sbt:dinocpu> runMain dinocpu.singlestep ...
```

The `singlestep` main (found at `src/main/scala/singlestep.scala`) takes two parameters: the test to run and the CPU design to run.

For instance, if you wanted to single step through the `add1` test with the single cycle CPU, you would use the following:

```
sbt:dinocpu> runMain dinocpu.singlestep add1 single-cycle
```

# The single step interface

When you run the single step application, it will give you a command prompt.
This command prompt will take two different inputs:
1. A number of cycles to step through
2. "Q" or "q" to quit

When quitting, the end conditions of the test are checked and the single step application will tell you if the test passed or failed.

# Finding the test names

The file `src/main/scala/testing/InstTests.scala` has a set of lists which shows each of the tests.
The test name is the binary name + the "extra name" or the first and last parameters to each `InstTest`.

For example:

```
CPUTestCase("addi2",
    Map("single-cycle" -> 2, "five-cycle" -> 0, "pipelined" -> 6),
    Map(),
    Map(0 -> 0, 10 -> 17, 11 -> 93),
    Map(), Map())
)


CPUTestCase("beq",
    Map("single-cycle" -> 3, "five-cycle" -> 7, "pipelined" -> 9),
    Map(5 -> 1234, 6 -> 1, 7 -> 5678, 28 -> 9012),
    Map(5 -> 0, 6 -> 1, 7 -> 5678, 28 -> 9012),
    Map(), Map(), "-False"),
```

The first test, the name would be "addi2" and the second test the name would be "beq-False".

You can also get these names from failed tests.
For instance,

```
- should run branch bne-False *** FAILED ***
```

Means the test name "bne-False" failed.

# The CPU implementations

Right now, there are two public and one private implementation.
There may be more in the future.

- `single-cycle`: The single cycle DINO CPU
- `five-cycle`: The five stage pipelined DINO CPU without forwarding or hazards (private only)
- `pipelined`: The fully pipelined DINO CPU with forwarding and hazard detection

# Adding a new test

See [CPU Test Case](testing.md#cpu-test-case).
