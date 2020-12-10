// This file contains ALU control logic.

package dinocpu.components

import chisel3._
import chisel3.util._

/**
 * The ALU control unit
 *
 * Input:  aluop, 00 for ld/st, 10 for R-type, 01 for branch
 * Input:  funct7, the most significant bits of the instruction
 * Input:  funct3, the middle three bits of the instruction (12-14)
 * Output: operation, What we want the ALU to do.
 *
 * For more information, see Section 4.4 and A.5 of Patterson and Hennessy.
 * This is loosely based on figure 4.12
 */
class ALUControl extends Module {
  val io = IO(new Bundle {
    val aluop     = Input(UInt(2.W))
    val itype     = Input(Bool())
    val funct7    = Input(UInt(7.W))
    val funct3    = Input(UInt(3.W))

    val operation = Output(UInt(4.W))
  })

  when (io.aluop === "b00".U) { // ld/st
    io.operation := "b0010".U // Do an add
  } .elsewhen (io.aluop === "b10".U ) { // R-type
    when (io.funct3 === "b000".U) {
      when (io.itype || io.funct7 === "b0000000".U) {
        io.operation := "b0010".U // add
      } .otherwise {
        io.operation := "b0011".U // sub
      }
    }
    .elsewhen (io.funct3 === "b001".U) { io.operation := "b1001".U } // sll
    .elsewhen (io.funct3 === "b010".U) { io.operation := "b1000".U } // slt
    .elsewhen (io.funct3 === "b011".U) { io.operation := "b0101".U } // sltu
    .elsewhen (io.funct3 === "b100".U) { io.operation := "b0110".U } // xor
    .elsewhen (io.funct3 === "b101".U) {
      when (io.funct7 === "b0000000".U) {
        io.operation := "b0111".U // srl
      } .otherwise {
        io.operation := "b0100".U // sra
      }
    }
    .elsewhen (io.funct3 === "b110".U) { io.operation := "b0001".U } // or
    .otherwise // (io.funct3 === "b111".U)
    { io.operation := "b0000".U } // and
  } .elsewhen (io.aluop === "b01".U) { // branches
    when (io.funct3 === "b000".U) { io.operation := "b1101".U } // beq
    .elsewhen (io.funct3 === "b001".U) { io.operation := "b1110".U } // bne
    .elsewhen (io.funct3 === "b100".U) { io.operation := "b1000".U } // blt
    .elsewhen (io.funct3 === "b101".U) { io.operation := "b1011".U } // bge
    .elsewhen (io.funct3 === "b110".U) { io.operation := "b0101".U } // bltu
    .elsewhen (io.funct3 === "b111".U) { io.operation := "b1100".U } // bgeu
    .otherwise { io.operation := "b1111".U } // invalid
  } .otherwise { io.operation := "b1111".U } // invalid operation
}
