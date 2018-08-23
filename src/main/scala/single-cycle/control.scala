// Control logic for the processor

package edu.darchr.codcpu

import chisel3._
import chisel3.util.{BitPat, ListLookup}

/**
 * Main control logic for our simple processor
 *
 * Describe the I/O here
 *
 * For more information, see section 4.4 of Patterson and Hennessy
 * This follows figure 4.22
 */
class Control extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(7.W))
    
    val branch = Output(Bool())
    val memread = Output(Bool())
    val memtoreg = Output(Bool())
    val aluop = Output(UInt(2.W))
    val memwrite = Output(Bool())
    val regwrite = Output(Bool())
    val alusrc = Output(Bool())
  })

  val signals =
    ListLookup(io.opcode,
      /*default*/           List(0.U,    0.U,     0.U,       0.U,   0.U,     0.U,     0.U),
      Array(                 /* branch, memread, memtoreg, aluop, memwrite, alusrc, regwrite */
      // R-format
      BitPat("b0110011") -> List(0.U,    0.U,     0.U,       2.U,   0.U,     0.U,     1.U),
      // load
      BitPat("b0000011") -> List(0.U,    1.U,     1.U,       0.U,   0.U,     1.U,     1.U),
      // store
      BitPat("b0100011") -> List(0.U,    0.U,     0.U,       0.U,   1.U,     1.U,     0.U),
      // beq
      BitPat("b1100011") -> List(1.U,    0.U,     0.U,       1.U,   0.U,     0.U,     0.U)  
      ) // Array
    ) // ListLookup

  io.branch := signals(0)
  io.memread := signals(1)
  io.memtoreg := signals(2)
  io.aluop := signals(3)
  io.memwrite := signals(4)
  io.alusrc := signals(5)
  io.regwrite := signals(6)

}