// Describes a 32 entry two read port one write port register file

package edu.darchr.codcpu

import chisel3._

/**
 * A 32 entry two read port one write port register file
 *
 * Here we describe the I/O
 *
 * For more information, see section 4.3 of Patterson and Hennessy
 */
class RegisterFile extends Module {
  val io = IO(new Bundle {
    val readreg1  = Input(UInt(5.W))
    val readreg2  = Input(UInt(5.W))
    val writereg1 = Input(UInt(5.W))
    val writedata = Input(UInt(32.W))
    val wen       = Input(UInt(32.W))

    val readdata1 = Output(UInt(32.W))
    val readdata2 = Output(UInt(32.W))
  })

  io.readdata1 := 0.U
  io.readdata2 := 0.U
}