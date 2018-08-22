// This file contains the ALU logic and the ALU control logic.

package edu.darchr.codcpu

import chisel3._

/**
 * The ALU control unit
 * 
 * Here we should describe the I/O
 *
 * For more information, see Section 4.4 of Patterson and Hennessy
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
 * For more information, see Section 4.3 of Patterson and Hennessy
 */
class ALU extends Module {
  val io = IO(new Bundle {
    val operation = Input(UInt(4.W))
    val inputx    = Input(UInt(32.W))
    val inputy    = Input(UInt(32.W))

    val result    = Output(UInt(32.W))
    val zero      = Output(Bool())
  })

  // The logic goes here. You'll likely want to use a switch statement.

  io.result := 0.U
  io.zero := Bool(false)
}