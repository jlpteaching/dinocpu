// This file contains the ALU logic and the ALU control logic.
// NOTE: This would be a good file to modify for different classes. With the ALU control different,
//       the students would have to think about how it's implemented.

package CODCPU

import chisel3._
import chisel3.util._

import Constants._

/**
 * The ALU control unit
 *
 * Input:  aluop, defines how to decode the the other bits.
 *         0 means load/store, 1 means beq, 2 means R-type and use funct fields
 * Input:  funct7, the most significant bits of the instruction
 * Input:  funct3, the middle three bits of the instruction (12-14)
 * Output: operation, What we want the ALU to do. See [[CODCPU.ALUConstants]]
 *
 * For more information, see Section 4.4 and A.5 of Patterson and Hennessy
 * This follows figure 4.12
 */
class ALUControl extends Module {
  val io = IO(new Bundle {
    val aluop     = Input(UInt(2.W))
    val funct7    = Input(UInt(7.W))
    val funct3    = Input(UInt(3.W))

    val operation = Output(UInt(4.W))
  })

  io.operation := 15.U // invalid operation

  // Your code goes here

}

/**
 * The ALU
 *
 * Input:  operation, specifies which operation the ALU should perform
 * Input:  inputx, the first input (e.g., reg1)
 * Input:  inputy, the second input (e.g., reg2)
 * Output: the result of the compuation
 */
class ALU extends Module {
  val io = IO(new Bundle {
    val operation = Input(UInt(4.W))
    val inputx    = Input(UInt(32.W))
    val inputy    = Input(UInt(32.W))

    val result    = Output(UInt(32.W))
  })

  // Default to 0 output
  io.result := 0.U

  switch (io.operation) {
    is ("b0000".U) {
      io.result := io.inputx & io.inputy
    }
    is ("b0001".U) {
      io.result := io.inputx | io.inputy
    }
    is ("b0010".U) {
      io.result := io.inputx + io.inputy
    }
    is ("b0011".U) {
      io.result := io.inputx - io.inputy
    }
    is ("b0100".U) {
      io.result := (io.inputx < io.inputy)
    }
    is ("b0101".U) {
      io.result := (io.inputx.asSInt < io.inputy.asSInt) // signed
    }
    is ("b0110".U) {
      io.result := io.inputx << io.inputy
    }
    is ("b0111".U) {
      io.result := io.inputx >> io.inputy
    }
    is ("b1000".U) {
      io.result := io.inputx.asSInt >> io.inputy // arithmetic (signed)
    }
    is ("b1001".U) {
      io.result := io.inputx ^ io.inputy
    }
  }
}
