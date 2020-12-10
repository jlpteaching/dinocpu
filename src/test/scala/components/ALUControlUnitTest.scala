// Unit tests for the ALU control logic

package dinocpu.test.components

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import dinocpu.components.ALUControl

class ALUControlUnitTester(c: ALUControl) extends PeekPokeTester(c) {
  private val ctl = c

  // Copied from Patterson and Waterman Figure 2.3
  val tests = List(
    // aluop,   imm,      Funct7,     Func3,   Control Input
    (  0.U, false.B, "b0000000".U, "b000".U, "b0010".U, "load/store"),
    (  0.U, false.B, "b1111111".U, "b111".U, "b0010".U, "load/store"),
    (  0.U, false.B, "b0000000".U, "b000".U, "b0010".U, "load/store"),
    (  2.U, false.B, "b0000000".U, "b000".U, "b0010".U, "add"),
    (  2.U, false.B, "b0100000".U, "b000".U, "b0011".U, "sub"),
    (  2.U, false.B, "b0000000".U, "b001".U, "b1001".U, "sll"),
    (  2.U, false.B, "b0000000".U, "b010".U, "b1000".U, "slt"),
    (  2.U, false.B, "b0000000".U, "b011".U, "b0101".U, "sltu"),
    (  2.U, false.B, "b0000000".U, "b100".U, "b0110".U, "xor"),
    (  2.U, false.B, "b0000000".U, "b101".U, "b0111".U, "srl"),
    (  2.U, false.B, "b0100000".U, "b101".U, "b0100".U, "sra"),
    (  2.U, false.B, "b0000000".U, "b110".U, "b0001".U, "or"),
    (  2.U, false.B, "b0000000".U, "b111".U, "b0000".U, "and"),
    (  2.U, true.B,  "b0000000".U, "b000".U, "b0010".U, "addi"),
    (  2.U, true.B,  "b0000000".U, "b010".U, "b1000".U, "slti"),
    (  2.U, true.B,  "b0000000".U, "b011".U, "b0101".U, "sltiu"),
    (  2.U, true.B,  "b0000000".U, "b100".U, "b0110".U, "xori"),
    (  2.U, true.B,  "b0000000".U, "b110".U, "b0001".U, "ori"),
    (  2.U, true.B,  "b0000000".U, "b111".U, "b0000".U, "andi"),
    (  2.U, true.B,  "b0000000".U, "b001".U, "b1001".U, "slli"),
    (  2.U, true.B,  "b0000000".U, "b101".U, "b0111".U, "srli"),
    (  2.U, true.B,  "b0100000".U, "b101".U, "b0100".U, "srai"),
    (  1.U, false.B,  "b0000000".U, "b000".U, "b1101".U, "beq"),
    (  1.U, false.B,  "b0000000".U, "b001".U, "b1110".U, "bne"),
    (  1.U, false.B,  "b0000000".U, "b100".U, "b1000".U, "blt"),
    (  1.U, false.B,  "b0000000".U, "b101".U, "b1011".U, "bge"),
    (  1.U, false.B,  "b0000000".U, "b110".U, "b0101".U, "bltu"),
    (  1.U, false.B,  "b0000000".U, "b111".U, "b1100".U, "bgeu")
  )

  for (t <- tests) {
    poke(ctl.io.aluop, t._1)
    poke(ctl.io.itype, t._2)
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
