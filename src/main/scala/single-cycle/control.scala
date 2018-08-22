// Control logic for the processor

package edu.darchr.codcpu

import chisel3._

/**
 * Main control logic for our simple processor
 *
 * Describe the I/O here
 *
 * For more information, see section 4.4 of Patterson and Hennessy
 */
class Control extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(7.W))
    
    val branch = Output(Bool())
    val memread = Output(Bool())
    val memtoreg = Output(Bool())
    val aluop = Output(UInt(2.W))
    val memwrite = Output(Bool())
    val alusrc = Output(Bool())
    val regwrite = Output(Bool())
  })

  io.branch := Bool(false)
  io.memread := Bool(false)
  io.memtoreg := Bool(false)
  io.aluop := 0.U
  io.memwrite := Bool(false)
  io.alusrc := Bool(false)
  io.regwrite := Bool(false)
}