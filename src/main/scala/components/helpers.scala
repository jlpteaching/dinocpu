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
    val inputx    = Input(UInt(64.W))
    val inputy    = Input(UInt(64.W))

    val result    = Output(UInt(64.W))
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
    val instruction = Input(UInt(64.W))

    val sextImm     = Output(UInt(64.W))
  })

  io.sextImm := 0.U

  val opcode = io.instruction(6,0)

  switch(opcode) {
    is("b0110111".U) { // U-type (lui)
      // RV64I lui
      // imm = cat(sign_extended_bits, imm[31:12], padding 0s)
      //           (32 bits)           (20 bits)   (12 bits)
      val imm = io.instruction(31, 12)
      io.sextImm := Cat(Fill(32, imm(19)), imm, Fill(12, 0.U))
    }
    is("b0010111".U) { // U-type (auipc)
      // RV64I auipc
      // imm = cat(sign_extended_bits, imm[31:12], padding 0s)
      //           (32 bits)           (20 bits)   (12 bits)
      val imm = io.instruction(31, 12)
      io.sextImm := Cat(Fill(32, imm(19)), imm, Fill(12, 0.U))
    }
    is("b1101111".U) { // J-type (jal)
      // riscv-spec: JAL encodes the offset as a multiple of 2 bytes
      // imm = sign_extends(2 * offset)
      val imm = Cat(io.instruction(31), io.instruction(19,12),
                    io.instruction(20), io.instruction(30,21))
      io.sextImm := Cat(Fill(43, imm(19)), imm, 0.U)
    }
    is("b1100111".U) { // I-type (jalr)
      val imm = io.instruction(31, 20)
      io.sextImm := Cat(Fill(52,imm(11)), imm)
    }
    is("b1100011".U) { // B-type
      val imm = Cat(io.instruction(31), io.instruction(7),
                    io.instruction(30,25), io.instruction(11,8))
      io.sextImm := Cat(Fill(51, imm(11)), imm, 0.U)
    }
    is("b0000011".U) { // I-type (ld)
      val imm = io.instruction(31, 20)
      io.sextImm := Cat(Fill(52, imm(11)), imm)
    }
    is("b0100011".U) { // S-type (st)
      val imm = Cat(io.instruction(31, 25), io.instruction(11,7))
      io.sextImm := Cat(Fill(52, imm(11)), imm)
    }
    is("b0010011".U) { // I-type (immediate arith.) 32-bit
      val imm = io.instruction(31, 20)
      io.sextImm := Cat(Fill(52,imm(11)), imm) // for instructions using shift amount, this imm is also valid as only the lower 5 bits (24, 20) are used
    }
    is("b0011011".U) { // I-type (immediate arith.)
      val imm = io.instruction(31, 20)
      io.sextImm := Cat(Fill(52,imm(11)), imm) // for instructions using shift amount, this imm is also valid as only the lower 6 bits (25, 20) are used
    }
    is("b1110011".U) { // zimm for csri
      io.sextImm := Cat(Fill(59,0.U), io.instruction(19,15))
    }
  }
}
