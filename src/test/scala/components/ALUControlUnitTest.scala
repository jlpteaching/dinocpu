// Unit tests for the ALU control logic

package CODCPU

import chisel3._

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


class ALUControlUnitTester(c: ALUControl) extends PeekPokeTester(c) {
  private val ctl = c

  // Copied from Patterson and Waterman Figure 2.3
  val tests = List(
    // memop,  Funct7,       Func3,    Control Input
    (  true.B, "b0000000".U, "b000".U, "b0010".U, "load/store"),
    (  true.B, "b1111111".U, "b111".U, "b0010".U, "load/store"),
    (  true.B, "b0000000".U, "b000".U, "b0010".U, "load/store"),
    ( false.B, "b0000000".U, "b000".U, "b0010".U, "add"),
    ( false.B, "b0100000".U, "b000".U, "b0011".U, "sub"),
    ( false.B, "b0000000".U, "b001".U, "b0110".U, "sll"),
    ( false.B, "b0000000".U, "b010".U, "b0100".U, "slt"),
    ( false.B, "b0000000".U, "b011".U, "b0101".U, "sltu"),
    ( false.B, "b0000000".U, "b100".U, "b1001".U, "xor"),
    ( false.B, "b0000000".U, "b101".U, "b0111".U, "srl"),
    ( false.B, "b0100000".U, "b101".U, "b1000".U, "sra"),
    ( false.B, "b0000000".U, "b110".U, "b0001".U, "or"),
    ( false.B, "b0000000".U, "b111".U, "b0000".U, "and")
  )

  for (t <- tests) {
    poke(ctl.io.memop, t._1)
    poke(ctl.io.funct7, t._2)
    poke(ctl.io.funct3, t._3)
    step(1)
    expect(ctl.io.operation, t._4, s"${t._5} wrong")
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly CODCPU.ALUControlTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly CODCPU.ALUControlTester'
  * }}}
  */
class ALUControlTester extends ChiselFlatSpec {
  "ALUControl" should s"match expectations for each intruction type" in {
    Driver(() => new ALUControl) {
      c => new ALUControlUnitTester(c)
    } should be (true)
  }
}
