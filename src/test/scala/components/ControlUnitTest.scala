// Unit tests for the main control logic

package dinocpu.test.components

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import dinocpu.components.Control

class ControlUnitTester(c: Control) extends PeekPokeTester(c) {
  private val ctl = c

  val tests = List(
    // Inputs,      aluop, toreg,  pcadd, itype, branch, alusrc, pcfromalu, regwrite, memwrite, memread, jump, resultselect
    ( "b0110011".U,     2,     0,      0,     0,      0,      0,         0,        1,        0,       0,    0,           0), // R-type
    ( "b0010011".U,     2,     0,      0,     1,      0,      1,         0,        1,        0,       0,    0,           0), // I-type
    ( "b0000011".U,     0,     1,      0,     0,      0,      1,         0,        1,        0,       1,    0,           0), // Load
    ( "b0100011".U,     0,     0,      0,     0,      0,      1,         0,        0,        1,       0,    0,           0), // Store
    ( "b1100011".U,     1,     0,      0,     0,      1,      0,         0,        0,        0,       0,    0,           0), // beq
    ( "b0110111".U,     0,     0,      0,     0,      0,      0,         0,        1,        0,       0,    0,           1), // lui
    ( "b0010111".U,     0,     0,      1,     0,      0,      1,         0,        1,        0,       0,    0,           0), // auipc
    ( "b1101111".U,     0,     0,      0,     0,      0,      0,         0,        1,        0,       0,    1,           2), // jal
    ( "b1100111".U,     0,     0,      0,     0,      0,      1,         1,        1,        0,       0,    1,           2) // jalr

  )
                      
  for (t <- tests) {
    poke(ctl.io.opcode, t._1) 
    step(1)            
    expect(ctl.io.branch, t._6)
    expect(ctl.io.pcfromalu, t._8)
    expect(ctl.io.jump, t._12)
    expect(ctl.io.memread, t._11)
    expect(ctl.io.memwrite, t._10)
    expect(ctl.io.regwrite, t._9)
    expect(ctl.io.toreg, t._3)
    expect(ctl.io.resultselect, t._13)
    expect(ctl.io.alusrc, t._7)
    expect(ctl.io.pcadd, t._4)
    expect(ctl.io.itype, t._5)
    expect(ctl.io.aluop, t._2)
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
