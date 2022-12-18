---
Author: Jason Lowe-Power
Title: Creating your first Chisel hardware
---

# First chisel hardware

# `SimpleSystem`

Let's start by creating some *very* simple hardware!

For this, we will create a new file in the Chisel project.
We will create our file at `src/main/scala/` and call it `simple.scala`
If you are using the DINO CPU repository, you can create the file as follows:

```
touch src/main/scala/simple.scala
```

All Chisel files are actually just Scala files.
What makes it Chisel and not Scala is when you use the Chisel libraries.

Therefore, we will first import the Chisel libraries.
Add the following line to the `simple.scala` file.

```
package dinocpu

import chisel3._
import chisel3.util._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
```

These lines first say that the code you're writing are part of the `dinocpu` package (`package dinocpu`).
By declaring the package, all other objects in the namespace become visible, and this makes it possible to run the main function we will create later.
Next, we are going to import all of the objects from the Chisel library (`import chisel3._`) and the Chisel utilities (`import chisel3.util._`).
Finally, we import the tester objects that can be used to drive a simulation.

## Creating an adder

Now, we will create a simple adder.
For this, we will define a `Module`, which is simply inheriting from the `Module` class in Chisel.

```
class SimpleAdder extends Module {

}
```

A hardware module isn't very useful unless there is some wires going into and out of the module (I/O).
So, let's add some I/O to our adder.
In this case, let's add two inputs (`inputx` and `inputy`) and one output (`result`).
Chisel has a notion of `Bundle`s that group named hardware components together, and to create an I/O interface we must create a `Bundle` of `Input` and `Output` objects as follows.

```
val io = IO(new Bundle{
  val inputx = Input(UInt(64.W))
  val inputy = Input(UInt(64.W))

  val result = Output(UInt(64.W))
})
```

Here, we are using another Scala keyword, `val`.
You must use the keyword `val` before each variable you declare.
(Scala also has a `var` keyword, but I do no believe you'll ever need to use it when working with the DINO CPU.
`var` is somewhat like `auto` in C++.)
The type of the variable will be inferred by Scala's type system.
**IMPORTANT**: Whenever you instantiate a module or other Chisel type and create a new variable for it, use the `=` operator.

Note that these inputs and the output are unsigned integers that are 64 bits wide.
You always should declare the size of the input and output wires.

Finally, all we have to do it implement the hardware for the adder.
Our final adder looks like the following:

```
class SimpleAdder extends Module {
  val io = IO(new Bundle{
    val inputx = Input(UInt(64.W))
    val inputy = Input(UInt(64.W))

    val result = Output(UInt(64.W))
  })

  io.result := io.inputx + io.inputy
}
```

An important distinction is that the `+` operator above does not add `inputx` and `inputy`.
Instead it represents creating the transistors *in hardware* to calculate the addition and wiring those transistors to the `result` wire.

## Creating a system of components

Now, let's create a larger system that connects multiple components together.
In this section, we will learn about wiring modules together, stateful components, and multiplexers.

Let's start by declaring a new type of `Module`, the `SimpleSystem`.

```
class SimpleSystem extends Module {
  val io = IO(new Bundle {
    val success = Output(Bool())
  })
}
```

The `SimpleSystem` has one output which is whether the system succeeds.
This is a made up output, but without it Chisel would optimize away our circuit.
There's no reason to have a circuit if it doesn't have an I/O!

Now, let's start by simply connecting two adders together like the image below.

![Two adders](./simplesystem-1.svg)

To create this circuit, we need to instantiate two `Adder`s and connect the result from the first to the input of the second.
You can add the following in the `SimpleSystem` class from above.

```
val adder1 = Module(new SimpleAdder())
val adder2 = Module(new SimpleAdder())

adder2.io.inputx := adder1.io.result
```

Now, let's arbitrarily define success as when the result of the second adder is 128.
And, let's also make the second input to the second adder is 3.

Our circuit now looks like the following.

![Adding 3 and success](./simplesystem-2.svg)

Now, our code to represent this circuit will look like the following:

```
val adder1 = Module(new SimpleAdder())
val adder2 = Module(new SimpleAdder())

adder2.io.inputx := adder1.io.result
adder2.io.inputy := 3.U

io.success := Mux(adder2.io.result === 128.U, true.B, false.B)
```

We must use `3.U` and `128.U`.
The `.U` after 3 and 128 converts the Scala integer type to the Chisel type, which is the hardware that represents the number 3 and 128.

The `===` operator creates hardware that checks for equality.
Therefore the multiplexer's selection input is 1 when the result is 128 and 0 otherwise.
Thus, we want to route 1 (or `true.B`) to the output of the mux in this case and 0 (or `false.B`) otherwise.

Finally, let's add some state to this.
We are going to add two registers and use those registers as the input to the first adder.
We will also feed the outputs of the adders back to the register.

Every time we use a register, there is an implicit clock added in Chisel.
You can think about this as at the "beginning" of the cycle, the registers are read and the output wires have the value in the register.
Then, at the end of the clock cycle, the registers will be updated with new values from their inputs.

So, let's create the full circuit shown below.

![Whole simplesystem](./simplesystem-3.svg)

```
class SimpleSystem extends Module {
  val io = IO(new Bundle {
    val success = Output(Bool())
  })

  val adder1 = Module(new SimpleAdder())
  val adder2 = Module(new SimpleAdder())

  val reg1 = RegInit(0.U)
  val reg2 = RegInit(1.U)

  adder1.io.inputx := reg1
  adder1.io.inputy := reg2

  adder2.io.inputx := adder1.io.result
  adder2.io.inputy := 3.U

  reg1 := adder1.io.result

  reg2 := adder2.io.result

  io.success := Mux(adder2.io.result === 128.U, true.B, false.B)
}
```

## Simulating and testing the circuit

Now, we need some way to simulate and test the circuit.
Luckily, Chisel has some built in testing capabilities.

The following creates a simple tester that runs the circuit for 10 cycles.
It also creates a `main` function that can be executed from the sbt prompt that creates the tester and drives it.

```
class SimpleSystemUnitTester(c: SimpleSystem) extends PeekPokeTester(c) {
  step(10)
}

object simple {
  def main(args: Array[String]): Unit = {
    Driver( () => new SimpleSystem ) { c => new SimpleSystemUnitTester(c) }
  }
}
```
Here, we create a new `SimpleSystem`, and pass that as a parameter to the `SimpleSystemUnitTester`.
The `SimpleSystemUnitTester` simply steps for 10 cycles.

You can run this by executing the following at the sbt command prompt:

```
sbt:dinocpu> runMain dinocpu.simple
```

When you run it, you will see the following, which isn't very interesting.

```
[info] Compiling 1 Scala source to /home/jlp/Code/chisel/darchr-codcpu/target/scala-2.12/classes ...
[warn] there were 13 feature warnings; re-run with -feature for details
[warn] one warning found
[info] Done compiling.
[warn] Multiple main classes detected.  Run 'show discoveredMainClasses' to see the list
[info] Packaging /home/jlp/Code/chisel/darchr-codcpu/target/scala-2.12/dinocpu_2.12-0.5.jar ...
[info] Done packaging.
[info] Running dinocpu.simple
[info] [0.001] Elaborating design...
[info] [0.800] Done elaborating.
Total FIRRTL Compile Time: 326.6 ms
Total FIRRTL Compile Time: 28.4 ms
End of dependency graph
Circuit state created
[info] [0.000] SEED 1547340019422
test SimpleSystem Success: 0 tests passed in 15 cycles taking 0.018804 seconds
[info] [0.008] RAN 10 CYCLES PASSED
[success] Total time: 3 s, completed Jan 13, 2019 12:40:21 AM
```

To make it more interesting, let's add a `printf` to the `SimpleSystem` that shows what's going on inside it.
You can add the following line as the last line in the `SimpleSystem` class.
It will print the values in the registers (at the beginning of the cycle) and the value of success.

```
printf(p"reg1: $reg1, reg2: $reg2, success: ${io.success}\n")
```

Now, you should see the following when you run the `simple` main function.

```
reg1: 1, reg2: 4, success: 0
reg1: 5, reg2: 8, success: 0
reg1: 13, reg2: 16, success: 0
reg1: 29, reg2: 32, success: 0
reg1: 61, reg2: 64, success: 1
reg1: 125, reg2: 128, success: 0
reg1: 253, reg2: 256, success: 0
reg1: 509, reg2: 512, success: 0
reg1: 1021, reg2: 1024, success: 0
reg1: 2045, reg2: 2048, success: 0
test SimpleSystem Success: 0 tests passed in 15 cycles taking 0.032475 seconds
```

Question: why did it print `success: 1` when `reg2 = 64`?

[Next: Testing with Chisel](testing.md)
