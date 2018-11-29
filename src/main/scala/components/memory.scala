// The instruction and data memory modules

package CODCPU

import chisel3._

import Common.{MemPortIo}

import Constants._

/**
 * Contains the instructions.
 * This might be automatically loaded, or we'll have to load it with special debugging statements
 * like how they do it Sodor
 *
 * Here we describe the I/O
 */
class InstructionMemory extends Module {
  val io = IO(new Bundle {
    // For interfacing with Sodor/black box
    val memport = new MemPortIo(32)

    // For the pipeline
    val address     = Input(UInt(32.W))

    val instruction = Output(UInt(32.W))
  })
  io := DontCare

  io.memport.req.bits.addr := io.address
  io.memport.req.bits.fcn  := M_XRD
  io.memport.req.bits.typ  := MT_WU
  io.memport.req.valid := true.B
  io.memport.req.bits.data := 0.U

  io.instruction := io.memport.resp.bits.data
  assert(io.memport.resp.valid)
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
    // For interfacing with Sodor/black box
    val memport = new MemPortIo(32)

    // For the pipeline
    val address   = Input(UInt(32.W))
    val writedata = Input(UInt(32.W))
    val memread   = Input(Bool())
    val memwrite  = Input(Bool())

    val readdata  = Output(UInt(32.W))
  })
  io := DontCare

  io.memport.req.bits.addr := io.address
  io.memport.req.bits.data := io.writedata
  io.memport.req.bits.fcn  := M_X
  io.memport.req.bits.typ  := MT_X

  when (io.memread) {
    io.memport.req.bits.fcn  := M_XRD
    io.memport.req.bits.typ  := MT_W
  }
  when (io.memwrite) {
    io.memport.req.bits.fcn  := M_XWR
    io.memport.req.bits.typ  := MT_W
  }

  io.memport.req.valid := io.memread || io.memwrite

  io.readdata := io.memport.resp.bits.data

}
