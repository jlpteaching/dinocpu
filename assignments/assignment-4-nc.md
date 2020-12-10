---
Author: Jason Lowe-Power
Editor: Julian Angeles
Title: DINO CPU Assignment 3
---

# DINO CPU Assignment 4: Benchmarking memory

Originally from ECS 154B Lab 4, Spring 2020.

# Table of Contents

- [Introduction](#introduction)
  * [Pipeline design constraint](#pipeline-design-constraint)
  * [Updating the DINO CPU code](#updating-the-dino-cpu-code)
  * [Goals](#goals)
- [Non-combinational Pipelined CPU design](#non-combinational-pipelined-cpu-design)
  * [Modifications to the pipelined DINO CPU](#modifications-to-the-pipelined-dino-cpu)
    + [Non combinational hazards](#non-combinational-hazards)
  * [Running simulations](#running-simulations)
- [Part I: Implement hazard logic](#part-i-implement-hazard-logic)
- [Part II: Running experiments](#part-ii-running-experiments)
  * [CPI with perfect memory](#cpi-with-perfect-memory)
  * [Performance with realistic memory](#performance-with-realistic-memory)
  * [Power and energy analysis](#power-and-energy-analysis)
  * [Extra credit](#extra-credit)
- [Grading](#grading)
- [Submission](#submission)
  * [Code portion](#code-portion)
  * [Written portion](#written-portion)
  * [Academic misconduct reminder](#academic-misconduct-reminder)
- [Hints](#hints)

# Introduction

![Cute Dino](../dino-128.png)

In the last assignment, you implemented a pipelined RISC-V CPU.
You implemented forwarding to reduce the impact of data hazards, but control hazards still caused significant performance degradation.
At least, that was your assumption.

In this assignment, you will be evaluating the performance of your pipelined design and extending this design with a more realistic memory system.
You will implement the updated hazard detection logic when memory isn't "perfect".
Then, you will compare the performance for different memory latencies.

## Pipeline design constraint

For this assignment, you must use the template code as a baseline.
If you use your own pipeline as the basis instead of the template code, **you may get the wrong results.**

## Updating the DINO CPU code

The DINO CPU code must be updated before you can run each lab.
You should read up on [how to update your code](../documentation/updating-from-git.md) to get the assignment 4 template from GitHub.

You can check out the master branch to get the template code for this lab.

## Goals

- Learn how to handle the complexity of a real memory system.
- Evaluate different CPU designs.
- Evaluate trade-offs between different designs.

# Non-combinational Pipelined CPU design

For this assignment, we have to update the implementation of the pipelined DINO CPU to be able to handle "non-combinational" memory.
In the previous assignments, memory was a combinational circuit.
Whenever the address (and valid) lines were asserted, the data was produced on the output *in the same cycle*.
This is unrealistic.
Main memory (DRAM) usually takes 15-45ns, which would make the cycle time for the processor about 50 MHz!

Instead, memory is synchronous.
It takes some number of cycles from when you send the request to memory before memory can produce the data.
When we are waiting for memory, we can say memory is "busy".

So far, you have ignored the `good` and `valid` signals on the memory ports.
These signals are used in the new non-combinational pipeline design.

`good` is asserted true by memory when the output is "good".
When `good` is false, the memory is busy or does not have an outstanding request.

`valid` is an input and is asserted when the `address` is valid and you want the memory port to send a request.

You can find more information about the memory interface on the [Modular memory page](../documentation/memory.md).

**Special thanks to Jared Barocsi who took the class last year for developing this interface and implementing most of the non-combinational CPU design.**

## Modifications to the pipelined DINO CPU

To deal with this in your pipeline, there are now two new stall conditions.

1. You must stall the fetch stage and insert bubbles in the decode stage when the instruction memory is busy.
2. You must stall the fetch, decode, and execute stages when the data memory is busy.

For this, we have provided you with a new hazard detection unit that **you must fill in**.
We have modified the inputs somewhat to make them less ambiguous.

For each pipeline register you can "stall" the register, which means that it will not be updated in the next cycle.
When you "stall" the register, it will keep the *same values* as the previous cycle.
This is implemented by de-asserting the `valid` line for the register.

Alternatively, you can "flush" the register, which means all of the values in the register will be 0 at the end of the cycle.
This is implemented by asserting the `flush` line for the register.

This is a little different than the "bubble" and "flush" signals from the previous assignment.
These signals were ambiguous and differed whether they should flush or stall based on which register was modified.

We have made all of the necessary changes to the pipeline in the `pipelined/cpu-noncombin.scala` file.
You only have to fill in the `components/hazardnoncombin.scala` file.

Note: there is one other change from assignment 3.
We found a bug in the forwarding logic where the `lui` instruction was not forwarding correctly.
This didn't affect any of the code executed on your processor in lab 3.
However, the "full applications" that you will be running in this were affected by this bug.

### Non combinational hazards

The new hazard detection unit has a couple of new inputs.
See the description below (also repeated in the `hazardnoncombin.scala` file).

```
Input:  rs1, the first source register number
Input:  rs2, the second source register number
Input:  idex_memread, true if the instruction in the ID/EX register is going to read from memory
Input:  idex_rd, the register number of the destination register for the instruction in the ID/EX register
Input:  exmem_taken, if true, then we are using the nextpc in the EX/MEM register, *nopc+4.
Input:  imem_ready, if true, then the Instruction Memory is ready for another instruction
Input:  imem_good, if true, then an instruction was successfully retrieved and can unstall CPU
Input:  dmem_good, if true, then can unstall CPU for data memory
```

Now, you have to update the outputs that follow.

```
Output: if_id_stall, if true, stall the if_id register.
Output: if_id_flush, if true, flush the if_id register.
Output: id_ex_stall, if true, stall the id_ex register.
Output: id_ex_flush, if true, flush the id_ex register.
Output: ex_mem_stall, if true, stall the ex_mem register.
Output: ex_mem_flush, if true, flush the ex_mem register.
Output: mem_wb_stall, if true, stall the mem_wb register.
Output: mem_wb_flush, if true, flush the mem_wb register.
```

I suggest starting with testing the "0 cycle" latency, which is the combinational memory as used in the previous assignment.
With this you can replicate the logic from lab 3.

Then, I suggest going to the "1 cycle" latency memory.
This is the hardest case to get right.
If you can pass the tests with the "1 cycle" latency, you should pass with 5 and 10 cycles as well.

## Running simulations

In this assignment, you will be running a number of simulations to measure the performance of your CPU designs.
Some of these simulations may run for millions of cycles.
They may take a few minutes on the lab computers, and possibly longer on your laptops, especially if you are using Vagrant or virtualization.
All of the tests run in less than 30 seconds on my desktop.

To run experiments, you are going to use the `simulate` main function.
The code can be found in [`simulate.scala`](../src/main/scala/simulate.scala).
This main function takes two parameters: the binary to run, and the CPU design to create.

```
sbt:dinocpu> runMain dinocpu.simulate <test name> <latency>
```

For the `test name`, you will use the names below.

For the `latency`, if you specify `0`, this means it will use combinational or "perfect" memory.
If you use any other positive value, the latency of every memory access will be `latency` cycles.
You will be evaluating a latency of 1, 5, and 10 cycles. If you'd like to run the other implementations (single-cycle and pipelined), just replace <latency> with their name. (This also applies to the single-stepper)

Note: the simulator will time out after 3 million cycles.
Even with a 10 cycle latency, no workload will take more than 3 million cycles with this design.

Binaries:
- `median.riscv`: performs a 1D three element median filter
- `multiply.riscv`: tests the software multiply implementation
- `qsort.riscv`: quick sort
- `rsort.riscv`: radix sort
- `towers.riscv`: simulation of the solution to the [Towers of Hanoi](https://en.wikipedia.org/wiki/Tower_of_Hanoi)
- `vvadd.riscv`: vector-vector add

You can find binaries for the six benchmarks in the [`/src/test/resources/c`](../src/test/resources/c) directory.
The source is also included in the subdirectories.

In order to answer the questions below, you will need to study this code and understand what these algorithms are doing.

You can also use `sigularity exec` instead of the `sbt` REPL as shown below.

As an example, here's the output when running `median` with a latency of 2 cycles.

```
> singularity exec library://jlowepower/default/dinocpu sbt "runMain dinocpu.simulate median.riscv 2"
[info] Loading global plugins from /home/jlp/.sbt/1.0/plugins
[info] Loading settings for project codcpu-private-build from plugins.sbt ...
[info] Loading project definition from /home/jlp/Teaching/codcpu-private/project
[info] Loading settings for project root from build.sbt ...
[info] Set current project to dinocpu (in build file:/home/jlp/Teaching/codcpu-private/)
[warn] Multiple main classes detected.  Run 'show discoveredMainClasses' to see the list
[info] Running dinocpu.simulate median.riscv 2
Running test median.riscv with memory latency of 2 cycles
[info] [0.001] Elaborating design...
CPU Type: pipelined-non-combin
Branch predictor: always-not-taken
Memory file: test_run_dir/pipelined-non-combin/median.riscv/median.riscv.hex
Memory type: non-combinational
Memory port type: non-combinational-port
Memory latency (ignored if combinational): 2
[info] [1.193] Done elaborating.
Total FIRRTL Compile Time: 1134.5 ms
file loaded in 0.170263957 seconds, 1117 symbols, 1087 statements
Running for max of 3000000
0 10000 20000 30000 Finished after 32075 cycles
Test passed!
[success] Total time: 9 s, completed Feb 24, 2020 6:39:12 PM
```

At the end of the output, it prints the total number of cycles taken (32,075 in this case).
It also prints details about the system that it is simulating (elaborating) before the simulation begins.

Note: Some tests require millions of cycles.
This can take a significant amount of time, especially if you are using a virtualized environment (e.g., vagrant).
On my machine (Intel(R) Core(TM) i7-7700 CPU @ 3.60GHz) all of the tests took about 25 minutes to execute.
The lab machines (e.g., pc01, etc.) should take about the same amount of time.
However, if you use a virtualized environment, I would expect a 2x slowdown or more.


# Part I: Implement hazard logic

See [Non combinational hazards](#non-combinational-hazards).

You will upload your non-combinational hazard logic to gradescope as discussed in the [Submission](#submission) section.

## Testing

You can use the `dinocpu.test.PipelinedCombinCPUTester` and  `dinocpu.test.PipelinedCombinFullApplicationTester` to test your hazard logic with a latency of 0, while the `dinocpu.test.PipelinedNonCombinCPUTester` and `dinocpu.test.PipelinedNonCombinFullApplicationTester` can be used for non-perfect memory with a latency of 1.
You can also run single "tests" with the simulator as described below or use the `singlestep` main function.
On gradescope, we will run `dinocpu.tests.PipelinedNonCombinCPUTester` and `dinocpu.tests.PipelinedNonCombinFullApplicationTester` to test your design.

Note: you can change the latency of memory by modifying the `configuration.scala` file.

# Part II: Running experiments

The bulk of this assignment will be running experiments and answering questions.
Once you have the correct implementation of the hazard detection unit, you can start trying to decide how to design the best CPU!

The workloads are the six benchmark binaries [mentioned above](#running-simulations).

Feel free to answer questions in prose, as a table, or as a graph.
However, make sure your answers are **legible**!
These questions *will be graded*.
We know the correct answers since everyone is using the same pipeline design.

I strongly suggest using graphs and writing your answers using a word processor.
I suggest you *do not* write your answers by hand.

## CPI with perfect memory

First, let's calculate the best possible performance with a perfect memory system.
You can model perfect memory by using the combinational memory ports (use a latency of `0` in the simulator).

To calculate the CPI, you need to know the number of instructions executed.
There are two ways you can get this information.
You can modify the CPU design with a special register to count the number of instructions executed (make sure you count only instructions that write back!).
Or, you can run the applications with the single cycle CPU design from assignment 2.
Since you know the CPI of the single cycle design, you can calculate the number of instructions from the number of cycles each application takes to execute.

1. What changes did you make to your processor or how did you calculate the number of instructions executed for each application?
2. What is the CPI for each application with perfect memory?

## Performance with realistic memory

Now, we will investigate the performance with a variety of "realistic" memory systems.

3. Run each application with a memory latency of 1, 5, and 10 cycles.
    * a) Report the number of cycles they take to execute.
    * b) What is the CPI when using "realistic" memory?
    * c) Name two architectural changes that would get our processor closer to the CPI of the "perfect" memory.
4. Calculate the "slowdown" for the realistic memory compared to the perfect memory. "Slowdown" is the opposite of speedup. For instance, if an application suffers a 2x slowdown, that means it takes twice as long to execute.
5. Discuss the sensitivity to memory latency for the applications.
    * a) Which application is *most* sensitive to memory latency? What is it about the algorithm that makes this application the most sensitive?
    * b) Which application is *least* sensitive to memory latency? What is it about the algorithm that makes this application the least sensitive?

## Power and energy analysis

Finally, we will analyze tradeoffs between performance, power, and energy.
In this section, you will calculate the total energy required to execute each workload under a variety of different conditions and decide what is "best".

Remember, the frequency at which transistors can flip is dependent on the voltage.
Therefore, if you want to run at a higher frequency you often have to increase the volatage.
The converse is also true.
If you are able to reduce the frequency, you can reduce the voltage, which gives you significant power savings.

Make the following assumptions:

* The static power of the system is 30 W. This includes the memory power and all other parts of the system (e.g., power supply loss, hard disk, etc.).
* The "capacitive constant" (*c* in `c*v^2*f`) is 2*10^-8
* The following table shows the required voltage for each frequency.

| Frequency | Voltage |
|-----------|---------|
| 200 MHz   | 0.8V    |
| 1 GHz     | 1.0V    |
| 2 GHz     | 1.2V    |

Remember the following equations:

![Power equation](https://latex.codecogs.com/gif.latex?P%20%3D%20P_%7Bdynamic%7D%20+%20P_%7Bstatic%7D)

![Power equation](https://latex.codecogs.com/gif.latex?P_{dynamic}%20%3D%20c*v%5E2*f)

![Energy equation](https://latex.codecogs.com/gif.latex?E%20%3D%20P%20*%20t)

6. Assume memory takes exactly 5 ns for all requests. This means that when your processor is running at 200 MHz memory takes 1 cycle, at 1 GHz it takes 5 cycles, and at 2 GHz it takes 10 cycles.
    * a) What is the total time for each workload on each of the three systems (in milliseconds)?
    * b) What is the speedup of the processor at 1 GHz and at 2 GHz over 200 MHz. Why is this not 5x and 10x?
7. For each workload, how much energy does the *entire system* (static + dynamic) consume for each design?
    * a) First, for each of the three systems, what is the total power (dynamic + static) in Watts? (Note: this is in the "reasonable range" for each system between 1 W and 100 W, unless my math is wrong.)
    * b) Then, you can use the energy equation above to compute the energy for each workload on each system.
8. Which is the most energy efficient design? Is it the fastest, the slowest, or something in between? Depending on your answer, why is it this (e.g., why is it the fastest or why is it the slowest)? Does this depend on the workload? If so, *why*?

## Extra credit

11. If this system had a cache which increased the dynamic power significantly, increased the static power slightly, but had a high hit ratio (e.g., 95% of memory accesses complete with a latency of 0 like the combinational memory case), which design would be most energy efficient 200 MHz, 1 GHz, or 2 GHz? Explain your reasoning.


# Grading

Grading will be done automatically on Gradescope.
See [the Submission section](#Submission) for more information on how to submit to Gradescope.

| Name         | Percentage |
|--------------|------------|
| Part I       | 20%        |
| Part II      | 80%        |
| Extra credit | +10%       |

# Submission

**Warning**: read the submission instructions carefully.
Failure to adhere to the instructions will result in a loss of points.

## Code portion

You will upload the one file that you changed (`hazardnoncombin.scala`) to Gradescope on the [Assignment 4: Code]() assignment.

Once uploaded, Gradescope will automatically download and run your code.
This should take less than 5 minutes.
For each part of the assignment, you will receive a grade.
If all of your tests are passing locally, they should also pass on Gradescope unless you made changes to the I/O, **which you are not allowed to do**.

Note: There is no partial credit on Gradescope.
Each part is all or nothing.
Either the test passes or it fails.

## Written portion

You will upload your answers for the [Assignment 4: Written]() assignment to Gradescope.
**Please upload a separate page for each answer!**
Additionally, I believe Gradescope allows you to circle the area with your final answer.
Make sure to do this!

We will not grade any questions for which we cannot read.
Be sure to check your submission to make sure it's legible, right-side-up, etc.

## Academic misconduct reminder

You are to work on this project **individually**.
You may discuss *high level concepts* with one another (e.g., talking about the diagram), but all work must be completed on your own.

**Remember, DO NOT POST YOUR CODE PUBLICLY ON GITHUB!**
Any code found on GitHub that is not the base template you are given will be reported to SJA.
If you want to sidestep this problem entirely, don't create a public fork and instead create a private repository to store your work.
GitHub now allows everybody to create unlimited private repositories for up to three collaborators, and you shouldn't have *any* collaborators for your code in this class.

# Hints

- Start early! There is a steep learning curve for Chisel, so start early and ask questions on Campuswire and in discussion.
- If you need help, come to office hours for the TAs, or post your questions on Campuswire.
- See [common errors](../documentation/common-errors.md) for some common errors and their solutions.
