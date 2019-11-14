// Some other helpful hardware structures (e.g., adders, shifters, etc.)

package dinocpu.components

import chisel3._
import chisel3.util._

/**
 * A simple adder which takes two inputs and returns the sum
 *
 * Input:  inputx the first input operand
 * Input:  inputy the second input operand
 * Output: result first + second
 */
class Adder extends Module {
  val io = IO(new Bundle{
    val inputx    = Input(UInt(32.W))
    val inputy    = Input(UInt(32.W))

    val result    = Output(UInt(32.W))
  })

  io.result := io.inputx + io.inputy
}

/**
 * Takes a RISC-V instruction and returns the sign-exteneded immediate value
 * Note that different RISC-V instruction types have different bits used as the immediate.
 * Also,for the B type and j-type instruction the values are *already* shifted left on the
 * output.
 *
 * Input:  instruction the input full encoded RISC-V instruction
 * Output: sextImm the output sign-extended immediate value encoded in the instruction
 */
class ImmediateGenerator extends Module {
  val io = IO(new Bundle{
    val instruction = Input(UInt(32.W))

    val sextImm     = Output(UInt(32.W))
  })

  io.sextImm := 0.U

  val opcode = io.instruction(6,0)
  switch(opcode) {
    is("b0110111".U) { // U-type (lui)
      val imm = io.instruction(31, 12)
      io.sextImm := Cat(imm, Fill(12,0.U))
    }
    is("b0010111".U) { // U-type (auipc)
      val imm = io.instruction(31, 12)
      io.sextImm := Cat(imm, Fill(12,0.U))
    }
    is("b1101111".U) { // J-type (jal)
      val imm = Cat(io.instruction(31), io.instruction(19,12),
                    io.instruction(20), io.instruction(30,21))
      io.sextImm := Cat(Fill(11,imm(19)), imm, 0.U)
    }
    is("b1100111".U) { // I-type (jalr)
      val imm = io.instruction(31, 20)
      io.sextImm := Cat(Fill(20,imm(11)), imm)
    }
    is("b1100011".U) { // B-type
      val imm = Cat(io.instruction(31), io.instruction(7),
                    io.instruction(30,25), io.instruction(11,8))
      io.sextImm := Cat(Fill(19, imm(11)), imm, 0.U)
    }
    is("b0000011".U) { // I-type (ld)
      val imm = io.instruction(31, 20)
      io.sextImm := Cat(Fill(20,imm(11)), imm)
    }
    is("b0100011".U) { // S-type (st)
      val imm = Cat(io.instruction(31, 25), io.instruction(11,7))
      io.sextImm := Cat(Fill(20,imm(11)), imm)
    }
    is("b0010011".U) { // I-type (immediate arith.)
      val imm = io.instruction(31, 20)
      io.sextImm := Cat(Fill(20,imm(11)), imm)
    }
    is("b1110011".U) { // zimm for csri
      io.sextImm := Cat(Fill(27,0.U), io.instruction(19,15))
    }
  }
}
