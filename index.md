---
---

# DINO CPU Assignments

See the [GitHub Repository]({{ site.github.repository_url }}) for high-level details about the DINO CPU project and how to contribute.

## [Assignment 1: Getting started with Chisel](assignments/assignment-1.md)

In this assignment, we will introduce you to the [Chisel](https://www.chisel-lang.org/) "hardware construction language" by asking you to implement a couple of simple circuits in Chisel.
First, you will implement the ALU control unit that you will use in the single cycle CPU design.
Then, you will begin implementing the single-cycle CPU datapath.
By the end of this assignment you will be able to correctly execute 10 RISC-V instructions (about 25%)!

## [Assignment 2: A single-cycle CPU design](assignments/assignment-2.md)

In this assignment, you will extend what you started in [assignment 1](assignments/assignment-1.md) and implement the rest of the RISC-V RV32I instruction set!
To do this, you will complete the datapath implementation and implement the control logic for the processor.
At the end, you will be able to run *real applications* compiled with GCC on your processor!

## [Assignment 3: A pipelined CPU design](assignments/assignment-3.md)

In this assignment, broken into two parts, you will be implementing a more realistic, pipelined-based core design.
In the first part, you will split your single cycle design into five different pipeline stages and ensure that all instructions except control have a correctly wired datapath.
Then, in part two you will finish wiring the rest of the datapath and the control path for the control instructions and implement the hazard detection and forwarding logic.

## [Assignment 4: Adding a branch predictor](assignments/assignment-4-bp.md)

In this assignment, you will extend the pipelined CPU with a branch predictor.
You are given a slightly modified pipelined design, and asked to develop a few different branch predictor designs.
Then, you will analyze the performance of these different designs on real applications.

## [Assignment 4: Adding non-combinational memory](assignments/assignment-4-nc.md)

This is an alternative extension to the branch predictor assignment.
In this assignment, you will connect the pipeline to a different memory system: one that doesn't return the result in the same cycle.
To work with this realistic memory system, you will need to update the stall conditions in the pipeline.
Then, you will analyze the performance of real appliations when connecting your processor to different memories.
