// The instruction and data memory modules

package edu.darchr.codcpu

import chisel3._

/**
 * Contains the instructions.
 * This might be automatically loaded, or we'll have to load it with special debugging statements
 * like how they do it Sodor
 *
 * Here we describe the I/O
 */
class InstructionMemory extends Module {
  val io = IO(new Bundle {
    val address     = Input(UInt(32.W))

    val instruction = Output(UInt(32.W))
  })

  io.instruction := 0.U
}

/**
 * Contains the data.
 * This might be automatically loaded, or we'll have to load it with special debugging statements
 * like how they do it Sodor. Initializing to zero might be good enough.
 *
 * Here we describe the I/O
 */
class DataMemory extends Module {
  val io = IO(new Bundle {
    val address   = Input(UInt(32.W))
    val writedata = Input(UInt(32.W))
    val memread   = Input(Bool())
    val memwrite  = Input(Bool())

    val readdata  = Output(UInt(32.W))
  })

  io.readdata := 0.U
}