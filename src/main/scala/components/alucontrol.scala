// This file contains ALU control logic.

package dinocpu.components

import chisel3._
import chisel3.util._

/**
 * The ALU control unit
 *
 * Input:  aluop        0 for ld/st, 1 for R-type
 * Input:  itype        True if I-type
 * Input:  funct7       The most significant bits of the instruction
 * Input:  funct3       The middle three bits of the instruction (12-14)
 * Input:  wordinst     True if the instruction *only* operates on 32-bit operands, False otherwise
 * Output: operation    What we want the ALU to do.
 *
 * For more information, see Section 4.4 and A.5 of Patterson and Hennessy.
 * This is loosely based on figure 4.12
 */
class ALUControl extends Module {
  val io = IO(new Bundle {
    val aluop     = Input(Bool())
    val itype     = Input(Bool())
    val funct7    = Input(UInt(7.W))
    val funct3    = Input(UInt(3.W))
    val wordinst  = Input(Bool())

    val operation = Output(UInt(5.W))
  })

  when (io.aluop === 0.U) {
    io.operation := "b00111".U // add
  } .otherwise {
    when (io.funct3 === "b000".U) {
      when (io.itype | io.funct7 === "b0000000".U) {
        when (io.wordinst) {
          io.operation := "b10111".U // addw
        } .otherwise {
          io.operation := "b00111".U // add
        }
      } .elsewhen (io.funct7 === "b0100000".U) {
		when (io.wordinst) {
          io.operation := "b10100".U // subw
        } .otherwise {
          io.operation := "b00100".U // sub
        }
      } .otherwise {
        io.operation := "b11111".U // invalid operation
      }
    } .elsewhen (io.funct3 === "b001".U) {
      when (io.wordinst) {
        io.operation := "b11000".U // sllw
      } .otherwise {
        io.operation := "b01000".U // sll
      }
    } .elsewhen (io.funct3 === "b010".U) {
      io.operation := "b01001".U // slt
    } .elsewhen (io.funct3 === "b011".U) {
      io.operation := "b00001".U // sltu
    } .elsewhen (io.funct3 === "b100".U) {
      io.operation := "b00000".U // xor
    } .elsewhen (io.funct3 === "b101".U) {
      when (io.funct7(6,1) === "b000000".U) {
        when (io.wordinst) {
          io.operation := "b10010".U // srlw
        } .otherwise {
          io.operation := "b00010".U // srl
        }
      } .elsewhen (io.funct7(6,1) === "b010000".U) {
        when (io.wordinst) {
          io.operation := "b10011".U // sraw
        } .otherwise {
          io.operation := "b00011".U // sra
        }
      } .otherwise {
        io.operation := "b11111".U // invalid operation
      }
    } .elsewhen (io.funct3 === "b110".U) {
      io.operation := "b00101".U // or
    } .otherwise { // b111
      io.operation := "b00110".U // and
    }
  }
}
