// Unit tests for the register file.

package CODCPU

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class RegisterFileUnitTester(c: RegisterFile) extends PeekPokeTester(c) {
  private val rf = c

  // Write some data to registers
  for (i <- 0 to 31) {
    poke(rf.io.writereg, i)
    poke(rf.io.writedata, i+100)
    poke(rf.io.wen, true)
    step(1)
  }

  for (i <- 0 to 31 by 2) {
    println("Checking")
    poke(rf.io.readreg1, i)
    poke(rf.io.readreg2, i+1)
    poke(rf.io.wen, false)
    expect(rf.io.readdata1, i+100)
    expect(rf.io.readdata2, i+101)
    step(1)
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly CODCPU.RegisterFileTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly CODCPU.RegisterFileTester'
  * }}}
  */
class RegisterFileTester extends ChiselFlatSpec {
  private val backendNames = if(firrtl.FileUtils.isCommandAvailable(Seq("verilator", "--version"))) {
    Array("firrtl", "verilator")
  }
  else {
    Array("firrtl")
  }
  for ( backendName <- backendNames ) {
    "RegisterFile" should s"save written values (with $backendName)" in {
      Driver(() => new RegisterFile, backendName) {
        c => new RegisterFileUnitTester(c)
      } should be (true)
    }
  }

  "Basic test using Driver.execute" should "be used as an alternative way to run specification" in {
    iotesters.Driver.execute(Array(), () => new RegisterFile) {
      c => new RegisterFileUnitTester(c)
    } should be (true)
  }

  "using --backend-name verilator" should "be an alternative way to run using verilator" in {
    if(backendNames.contains("verilator")) {
      iotesters.Driver.execute(Array("--backend-name", "verilator"), () => new RegisterFile) {
        c => new RegisterFileUnitTester(c)
      } should be(true)
    }
  }

  "running with --is-verbose" should "show more about what's going on in your tester" in {
    iotesters.Driver.execute(Array("--is-verbose"), () => new RegisterFile) {
      c => new RegisterFileUnitTester(c)
    } should be(true)
  }

  "running with --fint-write-vcd" should "create a vcd file from your test" in {
    iotesters.Driver.execute(Array("--fint-write-vcd"), () => new RegisterFile) {
      c => new RegisterFileUnitTester(c)
    } should be(true)
  }
}
