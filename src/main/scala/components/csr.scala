// This file contains the ALU logic and the ALU control logic.
// NOTE: This would be a good file to modify for different classes. With the ALU control different,
//       the students would have to think about how it's implemented.

package CODCPU

import chisel3._
import chisel3.util._

import Constants._

/**
 * CSR: control and status registers
 * This unit will handle all of the CSR functions. This might not include all CSRs required for
 * implementing RISC-V
 *
 * Describe I/O
 */
class CSR extends Module {
  val io = IO(new Bundle {
    val csr   = Input(UInt(12.W))
    val funct = Input(UInt(3.W))
    val rs1   = Input(UInt(5.W))

    val cycle = Output(UInt(64.W))
    val data  = Output(UInt(32.W))
  })

  val cycle = RegInit(0.U(32.W))

  // Default all output to 0
  io.data := 0.U

  io.cycle := cycle

  cycle := cycle + 1.U

}

