// Unit tests for the ALU control logic

package dinocpu

import chisel3._

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class ControlUnitTester(c: Control) extends PeekPokeTester(c) {
  private val ctl = c

  val tests = List(
    // Inputs,       imm,      toreg, regwrite, memread, memwrite, branch, add
    ( "b0110011".U,    0,        0,       1,        0,       0,        0,     0), // R-type
    ( "b0000011".U,    1,        1,       1,        1,       0,        0,     1), // Load
    ( "b0100011".U,    1,        0,       0,        0,       1,        0,     1), // Store
    ( "b1100011".U,    0,        0,       0,        0,       0,        1,     0)  // beq
  )

  for (t <- tests) {
    poke(ctl.io.opcode, t._1)
    step(1)
    expect(ctl.io.branch, t._7)
    expect(ctl.io.memread, t._5)
    expect(ctl.io.toreg, t._3)
    expect(ctl.io.add, t._8)
    expect(ctl.io.memwrite, t._6)
    expect(ctl.io.immediate, t._2)
    expect(ctl.io.regwrite, t._4)
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.ControlTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.ControlTester'
  * }}}
  */
class ControlTester extends ChiselFlatSpec {
  "Control" should s"match expectations" in {
    Driver(() => new Control) {
      c => new ControlUnitTester(c)
    } should be (true)
  }
}
