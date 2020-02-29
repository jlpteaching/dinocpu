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
This command prompt will take a variety of different inputs:

```
 Printing registers
 ------------------
 print reg <num>  : print the value in register
 print regs       : print values in all registers
 print pc         : print the address in the pc
 print inst [addr]: print the disassembly for the instruction at addr.
                    If no addr provided then use the current pc.

 Printing module I/O (wires)
 ---------------------------
 dump all        : Show all modules and the values of their I/O
 dump list       : List the valid modules to dump
 dump [module]   : Show values of the I/O on a specific module


 Printing pipeline registers (pipelined CPU only)
 ------------------------------------------------
 print pipereg <name> : Print the values in the pipeline register with name <name>
 print piperegs       : Print the values in all of the pipeline registers


 Display command (display value after step)
 ---------------------------------------
 display reg [num]
 display regs
 display pc
 display inst
 display pipereg [name]
 display piperegs
 display modules
 display module [module]


 Stop Displaying command
 -----------------------
 undisplay <num>  : remove the ith display line


 Controlling the simulator
 -------------------------
 step [num]      : move forward this many cycles, default 1

 Other commands
 --------------
 ?               : print this help
 q               : quit
```

When quitting, the end conditions of the test are checked and the single step application will tell you if the test passed or failed.

# Finding the test names

The file `src/main/scala/testing/InstTests.scala` has a set of lists which shows each of the tests.
The test name is the binary name + the "extra name" or the first and last parameters to each `InstTest`.

For example:

```
CPUTestCase("addi2",
    Map("single-cycle" -> 2, "pipelined" -> 6),
    Map(),
    Map(0 -> 0, 10 -> 17, 11 -> 93),
    Map(), Map())
)


CPUTestCase("beq",
    Map("single-cycle" -> 3, "pipelined" -> 9),
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

# A Single Step Test Example

Now, let's go over a brief step-by-step example of running the `addfwd` test in single stepping mode, highlighting the values in the registers and wires. As a heads up, this test adds the values between two registers (5 & 10) and places the result into one of them (10) about ten times. Initially, register 5 will have a value of 1 and register 10 with 0.

When you first run the test, you should see something along these lines:

```
sbt:dinocpu> runMain dinocpu.singlestep addfwd single-cycle
[info] Updating ...
[info] Done updating.
[info] Compiling 35 Scala sources to /home/jtoya/Git Repositories/UC Davis/Winter2019/TA/dinocpu/target/scala-2.12/classes ...
[warn] there were 6 deprecation warnings (since 1.2)
[warn] there was one deprecation warning (since since )
[warn] there were 7 deprecation warnings in total; re-run with -deprecation for details
[warn] there were 564 feature warnings; re-run with -feature for details
[warn] four warnings found
[info] Done compiling.
[warn] Multiple main classes detected.  Run 'show discoveredMainClasses' to see the list
[info] Packaging /home/jtoya/Git Repositories/UC Davis/Winter2019/TA/dinocpu/target/scala-2.12/dinocpu_2.12-0.5.jar ...
[info] Done packaging.
[info] Running dinocpu.singlestep addfwd single-cycle
Running test addfwd on CPU design single-cycle
[info] [0.001] Elaborating design...
[info] [1.387] Done elaborating.
Total FIRRTL Compile Time: 1827.6 ms
file loaded in 0.273723526 seconds, 1019 symbols, 981 statements
Help for the single stepper:
 Note: Registers print the value *stored* in that register. The wires print
 the *current* value on the wire for that cycle.

 Printing registers
 ------------------
 print reg <num>  : print the value in register
 print regs       : print values in all registers
 print pc         : print the address in the pc
 print inst [addr]: print the disassembly for the instruction at addr.
                    If no addr provided then use the current pc.

 Printing module I/O (wires)
 ---------------------------
 dump all        : Show all modules and the values of their I/O
 dump list       : List the valid modules to dump
 dump [module]   : Show values of the I/O on a specific module

 Controlling the simulator
 -------------------------
 step [num]      : move forward this many cycles, default 1

 Other commands
 --------------
 ?               : print this help
 q               : quit

Single stepper> 
```

If ran correctly, you should notice an interface guide and be given a command prompt. Let's start running some of the commands and looking at our values.

**Cycle 0**

Printing the initial PC, instruction, and registers:
```
Single stepper> print pc
PC: 0
Single stepper> print inst
0       : add x10, x10, x5     (0x00550533)
Single stepper> print reg 5
reg5: 1
Single stepper> print reg 10
reg10: 0
```
We can see that the Pc is at 0 and we're going to add the values in register 5 and 10, which are 1 and 0, and store the result back into register 10. How about the values in the wires?

In order to see them, you'll need to *dump* each of the modules you are interested in. For our example, we only care about the ALU Control, ALU, register file, and the PC increment unit.

Dumping the module wires:
```
Single stepper> dump aluControl
aluControl.io.immediate        0 (0x0)
aluControl.io.funct3           0 (0x0)
aluControl.io.add              0 (0x0)
aluControl.io.funct7           0 (0x0)
aluControl.io.operation        2 (0x2)
Single stepper> dump alu
alu.io.inputx                  0 (0x0)
alu.io.result                  1 (0x1)
alu.io.inputy                  1 (0x1)
alu.io.operation               2 (0x2)
Single stepper> dump registers
registers.io.readdata1         0 (0x0)
registers.io.readdata2         1 (0x1)
registers.io.readreg1          10 (0xa)
registers.io.writereg          10 (0xa)
registers.io.readreg2          5 (0x5)
registers.io.writedata         1 (0x1)
registers.io.wen               1 (0x1)
Single stepper> dump pcPlusFour
pcPlusFour.io.result           4 (0x4)
pcPlusFour.io.inputx           0 (0x0)
pcPlusFour.io.inputy           4 (0x4)
```
As we expected from the instruction, the ALU Control determined the operation to be an add (from the opcode 0010) and the ALU added the values 0 and 1 (inputx and inputy) which produced the result 1. Peeking at the register file, we can see that that the registers being read are 10 and 5, their respective values are 0 and 1, and the register to be written to, along with its value, are 10 and 1. The PC will get incremented by 4 for the next cycle.

**Cycle 1**

Next, we step 1 cycle and print the following cycle's PC, instruction, and registers:
```
Single stepper> step 1
Current cycle: 1
Single stepper> print pc
PC: 4
Single stepper> print inst
4       : add x10, x10, x5     (0x00550533)
Single stepper> print reg 5
reg5: 1
Single stepper> print reg 10
reg10: 1
```
Above shows the instruction to be the same, but the PC incremented by 4 and the value in register 10 is now 1 (just like what we expected).

The following cycle's module wires:
```
Single stepper> dump aluControl
aluControl.io.immediate        0 (0x0)
aluControl.io.funct3           0 (0x0)
aluControl.io.add              0 (0x0)
aluControl.io.funct7           0 (0x0)
aluControl.io.operation        2 (0x2)
Single stepper> dump alu
alu.io.inputx                  1 (0x1)
alu.io.result                  2 (0x2)
alu.io.inputy                  1 (0x1)
alu.io.operation               2 (0x2)
Single stepper> dump registers
registers.io.readdata1         1 (0x1)
registers.io.readdata2         1 (0x1)
registers.io.readreg1          10 (0xa)
registers.io.writereg          10 (0xa)
registers.io.readreg2          5 (0x5)
registers.io.writedata         2 (0x2)
registers.io.wen               1 (0x1)
Single stepper> dump pcPlusFour   
pcPlusFour.io.result           8 (0x8)
pcPlusFour.io.inputx           4 (0x4)
pcPlusFour.io.inputy           4 (0x4)
```
Similar to above, the only changes we expect are the updated value from register 10 and the new result being 2. (As well as the PC incrementing by 4 again). To make things a bit more interesting, let's step by 9 cycles. 

**Cycle 10**

Last look at the PC, instruction, and registers:
```
Single stepper> step 9
Current cycle: 10
Single stepper> print pc
PC: 40
Single stepper> print inst
40      : addi x0, x0, 0       (0x00000013)
Single stepper> print reg 5
reg5: 1
Single stepper> print reg 10
reg10: 10
```
Having a look at the tenth cycle, we can see that we've executed the last of the ten add instructions by the *nop* instruction we've reached and the final value in register 10 being 10.

Last look at the module wires:
```
Single stepper> dump aluControl
aluControl.io.immediate        1 (0x1)
aluControl.io.funct3           0 (0x0)
aluControl.io.add              0 (0x0)
aluControl.io.funct7           0 (0x0)
aluControl.io.operation        2 (0x2)
Single stepper> dump alu
alu.io.inputx                  0 (0x0)
alu.io.result                  0 (0x0)
alu.io.inputy                  0 (0x0)
alu.io.operation               2 (0x2)
Single stepper> dump registers
registers.io.readdata1         0 (0x0)
registers.io.readdata2         0 (0x0)
registers.io.readreg1          0 (0x0)
registers.io.writereg          0 (0x0)
registers.io.readreg2          0 (0x0)
registers.io.writedata         0 (0x0)
registers.io.wen               0 (0x0)
Single stepper> dump pcPlusFour
pcPlusFour.io.result           44 (0x2c)
pcPlusFour.io.inputx           40 (0x28)
pcPlusFour.io.inputy           4 (0x4)
```
With a *nop*, you can see that a majority of the wire values have been changed to 0. Considering that the following instructions will be nothing but *nops*, we can effectively quit the mode. Which you should see:
```
Single stepper> q
Test passed!
[success] Total time: 462 s, completed Jan 6, 2020 11:56:35 PM
```
Of course you could keep stepping, but eventually the PC will stop incrementing, indicating the end of the test. (Which you still have to quit by typing q)

# The CPU implementations

Right now, there are two public and one private implementation.
There may be more in the future.

- `single-cycle`: The single cycle DINO CPU
- `pipelined`: The fully pipelined DINO CPU with forwarding and hazard detection

# Adding a new test

See [CPU Test Case](testing.md#cpu-test-case).

# Disassembly

Disassembly is supported by asserting the debug flag to be true in `src/main/scala/configurations.scala`.
You can find the disassembler in `src/main/scala/utils/disassembler.scala`.

