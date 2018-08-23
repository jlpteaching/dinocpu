// This file contains the ALU logic and the ALU control logic.
// NOTE: This would be a good file to modify for different classes. With the ALU control different,
//       the students would have to think about how it's implemented.

package edu.darchr.codcpu

import chisel3._
import chisel3.util._

import Constants._

/**
 * The ALU control unit
 * 
 * Here we should describe the I/O
 *
 * For more information, see Section 4.4 and A.5 of Patterson and Hennessy
 */
class ALUControl extends Module {
  val io = IO(new Bundle {
    val aluop     = Input(UInt(2.W))
    val funct7    = Input(UInt(7.W))
    val funct3    = Input(UInt(3.W))
    
    val operation = Output(UInt(4.W))
  })

  // The logic goes here.

  io.operation := 0.U

}

/** 
 * The ALU
 *
 * Here we should describe the I/O
 *
 * For more information, see Section 4.3 and A.5 of Patterson and Hennessy
 * Specifically, the control lines come from figure 1.5.13
 */
class ALU extends Module {
  val io = IO(new Bundle {
    val operation = Input(UInt(4.W))
    val inputx    = Input(UInt(32.W))
    val inputy    = Input(UInt(32.W))

    val result    = Output(UInt(32.W))
    val zero      = Output(Bool())
  })

  // Default to 0 output
  io.result := 0.U

  switch (io.operation) {
    is (AND_OP) {
      io.result := io.inputx & io.inputy
    }
    is (OR_OP) {
      io.result := io.inputx | io.inputy
    }
    is (ADD_OP) {
      io.result := io.inputx + io.inputy
    }
    is (SUB_OP) {
      io.result := io.inputx - io.inputy
    }
    is (SLT_OP) {
      io.result := (io.inputx < io.inputy)
    }
    is (NOR_OP) {
      io.result := ~(io.inputx | io.inputy)
    }
  }

  io.zero := (io.result === 0.U)
}