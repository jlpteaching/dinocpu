// Unit tests for the main control logic

package dinocpu.test.components

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import dinocpu.components.Control

class ControlUnitTester(c: Control) extends PeekPokeTester(c) {
  private val ctl = c

  val tests = List(
    // Inputs,      itype, aluop, src1, src2, branch, jumptype, resultselect, memop, toreg, regwrite, validinst, wordinst      
    ( "b0110011".U,   0.U,   1.U,  0.U,  0.U,    0.U,      0.U,          0.U,   0.U,   0.U,      1.U,       1.U,      0.U), // R-type
    ( "b0111011".U,   0.U,   1.U,  0.U,  0.U,    0.U,      0.U,          0.U,   0.U,   0.U,      1.U,       1.U,      1.U), // R-type 32-bit
    ( "b0010011".U,   1.U,   1.U,  0.U,  1.U,    0.U,      0.U,          0.U,   0.U,   0.U,      1.U,       1.U,      0.U), // I-type
    ( "b0011011".U,   1.U,   1.U,  0.U,  1.U,    0.U,      0.U,          0.U,   0.U,   0.U,      1.U,       1.U,      1.U), // I-type 32-bit
    ( "b0000011".U,   0.U,   0.U,  0.U,  1.U,    0.U,      0.U,          0.U,   2.U,   1.U,      1.U,       1.U,      0.U), // Load
    ( "b0100011".U,   0.U,   0.U,  0.U,  1.U,    0.U,      0.U,          0.U,   3.U,   0.U,      0.U,       1.U,      0.U), // Store
    ( "b1100011".U,   0.U,   0.U,  0.U,  0.U,    1.U,      0.U,          0.U,   0.U,   0.U,      0.U,       1.U,      0.U), // Branch
    ( "b0110111".U,   0.U,   0.U,  0.U,  0.U,    0.U,      0.U,          1.U,   0.U,   0.U,      1.U,       1.U,      0.U), // lui
    ( "b0010111".U,   0.U,   0.U,  1.U,  1.U,    0.U,      0.U,          0.U,   0.U,   0.U,      1.U,       1.U,      0.U), // auipc
    ( "b1101111".U,   0.U,   0.U,  1.U,  2.U,    0.U,      2.U,          0.U,   0.U,   0.U,      1.U,       1.U,      0.U), // jal
    ( "b1100111".U,   0.U,   0.U,  1.U,  2.U,    0.U,      3.U,          0.U,   0.U,   0.U,      1.U,       1.U,      0.U)  // jalr
  )
                      
  for (t <- tests) {
    poke(ctl.io.opcode, t._1)
    step(1)
    expect(ctl.io.itype, t._2)
    expect(ctl.io.aluop, t._3)
    expect(ctl.io.src1, t._4)
    expect(ctl.io.src2, t._5)
    expect(ctl.io.branch, t._6)
    expect(ctl.io.jumptype, t._7)
    expect(ctl.io.resultselect, t._8)
    expect(ctl.io.memop, t._9)
    expect(ctl.io.toreg, t._10)
    expect(ctl.io.regwrite, t._11)
    expect(ctl.io.validinst, t._12)
    expect(ctl.io.wordinst, t._13)
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.test.ControlTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.test.ControlTester'
  * }}}
  */
class ControlTester extends ChiselFlatSpec {
  "Control" should s"match expectations" in {
    Driver(() => new Control) {
      c => new ControlUnitTester(c)
    } should be (true)
  }
}
