// This file contains ALU control logic.

package dinocpu

import chisel3._
import chisel3.util._

/**
 * The ALU control unit
 *
 * Input:  add, if true, add no matter what the other bits are
 * Input:  immediate, if true, ignore funct7 when computing the operation
 * Input:  funct7, the most significant bits of the instruction
 * Input:  funct3, the middle three bits of the instruction (12-14)
 * Output: operation, What we want the ALU to do.
 *
 * For more information, see Section 4.4 and A.5 of Patterson and Hennessy.
 * This is loosely based on figure 4.12
 */
class ALUControl extends Module {
  val io = IO(new Bundle {
    val add       = Input(Bool())
    val immediate = Input(Bool())
    val funct7    = Input(UInt(7.W))
    val funct3    = Input(UInt(3.W))

    val operation = Output(UInt(4.W))
  })

  io.operation := 15.U // invalid operation

  when (io.add) {
    io.operation := "b0010".U
  } .otherwise {
    switch (io.funct3) {
      is ("b000".U) {
        when (io.immediate || io.funct7 === "b0000000".U) {
          io.operation := "b0010".U // add
        } .otherwise {
          io.operation := "b0011".U // sub
        }
      }
      is ("b001".U) { io.operation := "b0110".U } // sll
      is ("b010".U) { io.operation := "b0100".U } // slt
      is ("b011".U) { io.operation := "b0101".U } // sltu
      is ("b100".U) { io.operation := "b1001".U } // xor
      is ("b101".U) {
        when (io.funct7 === "b0000000".U) {
          io.operation := "b0111".U // srl
        } .otherwise {
          io.operation := "b1000".U // sra
        }
      }
      is ("b110".U) { io.operation := "b0001".U } // or
      is ("b111".U) { io.operation := "b0000".U } // and
    }
  }
}
