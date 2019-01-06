// This file contains the forwarding unit

package dinocpu

import chisel3._

/**
 * The Forwarding unit
 *
 * Here we should describe the I/O
 *
 * For more information, see Section 4.7 of Patterson and Hennessy
 * This follows figure 4.53
 */

class ForwardingUnit extends Module {
  val io = IO(new Bundle {
    val rs1     = Input(UInt(5.W))
    val rs2     = Input(UInt(5.W))
    val exmemrd = Input(UInt(5.W))
    val exmemrw = Input(Bool())
    val memwbrd = Input(UInt(5.W))
    val memwbrw = Input(Bool())

    val forwardA = Output(UInt(2.W))
    val forwardB = Output(UInt(2.W))
  })

  when (io.rs1 === io.exmemrd && io.exmemrd =/= 0.U && io.exmemrw) {
    io.forwardA := 1.U
  } .elsewhen (io.rs1 === io.memwbrd && io.memwbrd =/= 0.U && io.memwbrw) {
    io.forwardA := 2.U
  } .otherwise {
    io.forwardA := 0.U
  }

  when (io.rs2 === io.exmemrd && io.exmemrd =/= 0.U && io.exmemrw) {
    io.forwardB := 1.U
  } .elsewhen (io.rs2 === io.memwbrd && io.memwbrd =/= 0.U && io.memwbrw) {
    io.forwardB := 2.U
  } .otherwise {
    io.forwardB := 0.U
  }
}
