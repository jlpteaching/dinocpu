// Unit tests for the register file.

package dinocpu.test.components

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import dinocpu.CPUConfig
import dinocpu.components.RegisterFile

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
  * testOnly dinocpu.RegisterFileTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.RegisterFileTester'
  * }}}
  */
class RegisterFileTester extends ChiselFlatSpec {
  "RegisterFile" should s"save written values (with firrtl)" in {
    implicit val conf = new CPUConfig()
    Driver(() => new RegisterFile, "firrtl") {
      c => new RegisterFileUnitTester(c)
    } should be (true)
  }
}
