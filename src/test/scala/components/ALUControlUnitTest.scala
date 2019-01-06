// Unit tests for the ALU control logic

package dinocpu

import chisel3._

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


class ALUControlUnitTester(c: ALUControl) extends PeekPokeTester(c) {
  private val ctl = c

  // Copied from Patterson and Waterman Figure 2.3
  val tests = List(
    // add,   imm,      Funct7,       Func3,    Control Input
    (  true.B, false.B, "b0000000".U, "b000".U, "b0010".U, "load/store"),
    (  true.B, false.B, "b1111111".U, "b111".U, "b0010".U, "load/store"),
    (  true.B, false.B, "b0000000".U, "b000".U, "b0010".U, "load/store"),
    ( false.B, false.B, "b0000000".U, "b000".U, "b0010".U, "add"),
    ( false.B, false.B, "b0100000".U, "b000".U, "b0011".U, "sub"),
    ( false.B, false.B, "b0000000".U, "b001".U, "b0110".U, "sll"),
    ( false.B, false.B, "b0000000".U, "b010".U, "b0100".U, "slt"),
    ( false.B, false.B, "b0000000".U, "b011".U, "b0101".U, "sltu"),
    ( false.B, false.B, "b0000000".U, "b100".U, "b1001".U, "xor"),
    ( false.B, false.B, "b0000000".U, "b101".U, "b0111".U, "srl"),
    ( false.B, false.B, "b0100000".U, "b101".U, "b1000".U, "sra"),
    ( false.B, false.B, "b0000000".U, "b110".U, "b0001".U, "or"),
    ( false.B, false.B, "b0000000".U, "b111".U, "b0000".U, "and"),
    ( false.B, true.B,  "b0000000".U, "b000".U, "b0010".U, "addi"),
    ( false.B, true.B,  "b0000000".U, "b010".U, "b0100".U, "sltiu"),
    ( false.B, true.B,  "b0000000".U, "b100".U, "b1001".U, "xori"),
    ( false.B, true.B,  "b0000000".U, "b110".U, "b0001".U, "ori"),
    ( false.B, true.B,  "b0000000".U, "b111".U, "b0000".U, "andi"),
    ( false.B, true.B,  "b0000000".U, "b001".U, "b0110".U, "slli"),
    ( false.B, true.B,  "b0000000".U, "b101".U, "b0111".U, "srli"),
    ( false.B, true.B,  "b0100000".U, "b101".U, "b1000".U, "srai")
  )

  for (t <- tests) {
    poke(ctl.io.add, t._1)
    poke(ctl.io.immediate, t._2)
    poke(ctl.io.funct7, t._3)
    poke(ctl.io.funct3, t._4)
    step(1)
    expect(ctl.io.operation, t._5, s"${t._6} wrong")
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.ALUControlTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.ALUControlTester'
  * }}}
  */
class ALUControlTester extends ChiselFlatSpec {
  "ALUControl" should s"match expectations for each intruction type" in {
    Driver(() => new ALUControl) {
      c => new ALUControlUnitTester(c)
    } should be (true)
  }
}
