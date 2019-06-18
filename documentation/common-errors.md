# Common errors

Note: We will populate this with questions from Piazza when it looks like many people are running into the same issue.

## VBoxManage: error: Details: code NS_ERROR_FAILURE...

```
There was an error while executing `VBoxManage`, a CLI used by Vagrant
for controlling VirtualBox. The command and stderr is shown below.

Command: ["startvm", "...", "--type", "headless"]

Stderr: VBoxManage: error: The virtual machine 'vm-singularity_default_...' has terminated unexpectedly during startup with exit code 1 (0x1)

VBoxManage: error: Details: code NS_ERROR_FAILURE (0x80004005), component MachineWrap, interface IMachine
```

This is a Vagrant and/or VirtualBox issue, not with Singularity or Chisel.
You'll most likely see this because you ran the following command in the Sylabs Singularity installation guide:

```
export VM=sylabs/singularity-ubuntu-bionic64 ...
```

This is the wrong image!
To fix this, go back to the folder you created during the tutorial and run `vagrant destroy`.
Then run the following:

```
cd dinocpu
vagrant up
vagrant ssh
```

The Vagrantfile in the `dinocpu` folder is correctly initialized, so doing this should just work.

## FATAL: container creation failed: mount error...

If you see the following error, it is likely because you ran out of disk space on the CSIF machine.

```
FATAL:   container creation failed: mount error: can't mount image /proc/self/fd/8: failed to mount squashfs filesystem: invalid argument
```

You can find out how much space you're using with the `fquota` command as shown below.
`fquota` is a script only on the CSIF machines to help you find the largest directories, so you can clean up your files.

```
jlp@ad3.ucdavis.edu@pc12:~$ fquota
QUOTA SUMMARY   -- Disk quotas for user jlp@ad3.ucdavis.edu (uid 832205):
Currently using 1717 MB of 2048 MB in your quota.
```

If you clear your Singularity cache (`.singularity/cache/`), you can free up some disk space, but the Singularity image will be re-downloaded the next time you run `singularity`.

## [warn] No sbt.version set in project/build.properties...

```
WARNING: Authentication token file not found : Only pulls of public images will succeed
[warn] No sbt.version set in project/build.properties, base directory: ...
[info] Set current project to ... (in build file: ...)
>
```

This occurs when you try to run Singularity outside of the `dinocpu` directory.
Run the `singularity run` command within the `dinocpu` directory.

## Cannot find cpu.registers.regs_5 in symbol table

```
sbt:dinocpu> testOnly dinocpu.SingleCycleAddTesterLab1
[info] Compiling 1 Scala source to /home/jlp/Code/dinocpu/target/scala-2.12/classes ...
[warn] there were 18 feature warnings; re-run with -feature for details
[warn] one warning found
[info] Done compiling.
[info] [0.001] Elaborating design...
[info] [0.148] Done elaborating.
[info] SingleCycleAddTesterLab1:
[info] Single Cycle CPU
[info] - should run add test add1 *** FAILED ***
[info]   firrtl.passes.CheckInitialization$RefNotInitializedException: @[cpu.scala 22:26] : [module SingleCycleCPU]  Reference registers is not fully initialized.
[info]    : registers.io.wen <= VOID
[info]   at firrtl.passes.CheckInitialization$.$anonfun$run$6(CheckInitialization.scala:79)
[info]   at firrtl.passes.CheckInitialization$.$anonfun$run$6$adapted(CheckInitialization.scala:74)
[info]   at scala.collection.TraversableLike$WithFilter.$anonfun$foreach$1(TraversableLike.scala:789)
[info]   at scala.collection.mutable.HashMap.$anonfun$foreach$1(HashMap.scala:138)
[info]   at scala.collection.mutable.HashTable.foreachEntry(HashTable.scala:236)
[info]   at scala.collection.mutable.HashTable.foreachEntry$(HashTable.scala:229)
[info]   at scala.collection.mutable.HashMap.foreachEntry(HashMap.scala:40)
[info]   at scala.collection.mutable.HashMap.foreach(HashMap.scala:138)
[info]   at scala.collection.TraversableLike$WithFilter.foreach(TraversableLike.scala:788)
[info]   at firrtl.passes.CheckInitialization$.checkInitM$1(CheckInitialization.scala:74)
[info]   ...
[info] ScalaTest
[info] Run completed in 776 milliseconds.
[info] Total number of tests run: 1
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 0, failed 1, canceled 0, ignored 0, pending 0
[info] *** 1 TEST FAILED ***
[error] Failed: Total 1, Failed 1, Errors 0, Passed 0
[error] Failed tests:
[error]         dinocpu.SingleCycleAddTesterLab1
[error] (test / testOnly) sbt.TestsFailedException: Tests unsuccessful
[error] Total time: 2 s, completed Jan 8, 2019 6:49:17 PM
```

If you encounter an error saying that the simulator (Treadle) can't find some register in the symbol table, this is likely because the register file is being optimized away.
*You will see this error before you add any of your own code.*
Chisel is an optimizing compiler that checks to see if the hardware will ever be used.
If Chisel determines the hardware will never be used, it will remove the hardware.

**To fix this error**: Make sure that you have connected up the register file correctly.
More specifically, check the write enable input to the register file.

## possible cause: maybe a semicolon is missing before 'value is'?

If Chisel complains about a missing semicolon before an `is` statement it is likely that you have a nested `is`.

For instance, this is an error you may see.

```
[error] /home/jlp/dinocpu/src/main/scala/components/alucontrol.scala:40:13: value is is not a member of Unit
[error] possible cause: maybe a semicolon is missing before `value is'?
[error]           } is ("b0100000".U) {
```

Below is an example **incorrect** nested `is` statement.

```
switch (io.funct3) {
  is ("b000".U) {
    switch (io.funct7) {
      is (xxxxxxx) {
        io.operation := wwwwwwww
      } is (yyyyyyyy) {
        io.operation := zzzzzzzz
      }
    }
  }
  ...
```

You can fix this by changing the inner `switch` to a `when`.

```
switch (io.funct3) {
  is ("b000".U) {
    when (io.funct7 === xxxxxxx) { io.operation := wwwwwwww }
    otherwise { io.operation := zzzzzzzz }
  }
  ...
```

## java.lang.Exception: Problem with compilation

If you see this error, then your mistake is likely in your Chisel syntax or a type error in Chisel.

For instance, if I try to connect a signed number to an unsigned input I get the following output from Chisel.

```
class Adder extends Module {
  val io = IO(new Bundle{
    val inputx    = Input(UInt(32.W))
    val inputy    = Input(UInt(32.W))

    val result    = Output(UInt(32.W))
  })

  io.result := io.inputx + io.inputy
}
```

```
val pcPlusFour = Module(new Adder())
pcPlusFour.io.inputy := 4.S
```

The output when I try to run Chisel is:

```
[info] [0.001] Elaborating design...
[error] chisel3.internal.ChiselException: Connection between sink (chisel3.core.UInt@1b206824) and source (chisel3.core.SInt@511647f3)
failed @: Sink (chisel3.core.UInt@1b206824) and Source (chisel3.core.SInt@511647f3) have different types.
[error]         ...
[error]         at dinocpu.SingleCycleCPU.<init>(cpu.scala:33)
[error]         at dinocpu.CPUConfig.getCPU(configuration.scala:30)
[error]         at dinocpu.Top.$anonfun$cpu$1(top.scala:14)
[error]         at chisel3.core.Module$.do_apply(Module.scala:51)
[error]         at dinocpu.Top.<init>(top.scala:14)
[error]         at dinocpu.simulate$.$anonfun$build$1(simulate.scala:80)
[error]         ... (Stack trace trimmed to user code only, rerun with --full-stacktrace if you wish to see the full stack trace)
[info] SingleCycleAddTesterLab1:
[info] Single Cycle CPU
[info] - should run add test add1 *** FAILED ***
[info]   java.lang.Exception: Problem with compilation
[info]   at dinocpu.simulate$.build(simulate.scala:89)
[info]   at dinocpu.CPUTesterDriver.<init>(CPUTesterDriver.scala:24)
[info]   at dinocpu.CPUTesterDriver$.apply(CPUTesterDriver.scala:108)
[info]   at dinocpu.SingleCycleAddTesterLab1.$anonfun$new$6(Lab1Test.scala:77)
[info]   at org.scalatest.OutcomeOf.outcomeOf(OutcomeOf.scala:85)
[info]   at org.scalatest.OutcomeOf.outcomeOf$(OutcomeOf.scala:83)
[info]   at org.scalatest.OutcomeOf$.outcomeOf(OutcomeOf.scala:104)
[info]   at org.scalatest.Transformer.apply(Transformer.scala:22)
[info]   at org.scalatest.Transformer.apply(Transformer.scala:20)
[info]   at org.scalatest.FlatSpecLike$$anon$1.apply(FlatSpecLike.scala:1682)
```

The important part to notice is the following:

```
[error] chisel3.internal.ChiselException: Connection between sink (chisel3.core.UInt@1b206824) and source (chisel3.core.SInt@511647f3)
failed @: Sink (chisel3.core.UInt@1b206824) and Source (chisel3.core.SInt@511647f3) have different types.
```

This says the source and sink (input and output) types don't match.
In other words, one is signed and one is unsigned.

As a general hint, look at the *highest* error in the output, or the error that's generated *first*, not the error at the bottom.
