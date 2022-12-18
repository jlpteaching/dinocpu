// This file contains the forwarding unit

package dinocpu.components

import chisel3._

/**
 * The Forwarding unit
 *
 * Input:  rs1, the register number for which the data is used in the execute stage
 * Input:  rs2, the register number for which the data is used in the execute stage
 * Input:  exmemrd, the destination register for the instruction in the mem stage
 * Input:  exmemrw, true if the instruction in the memory stage needs to write the register file
 * Input:  memwbrd, the destination register for the instruction in the writeback stage
 * Input:  memwbrw, true if the instruction in the writeback stage needs to write the register file
 *
 * Output: forwardA, 0, don't forward. 1, forward from mem stage. 2, forward from wb stage.
 *                   This is used for the "readdata1" forwarding
 * Output: forwardB, 0, don't forward. 1, forward from mem stage. 2, forward from wb stage.
 *                   This is used for the "readdata2" forwarding
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

  when (io.exmemrw === true.B && io.exmemrd === io.rs1 && io.exmemrd =/= 0.U) {
    io.forwardA := 1.U
  }
  .elsewhen (io.memwbrw === true.B && io.memwbrd === io.rs1 && io.memwbrd =/= 0.U) {
    io.forwardA := 2.U
  }
  .otherwise {
    io.forwardA := 0.U
  }

  when (io.exmemrw === true.B && io.exmemrd === io.rs2 && io.exmemrd =/= 0.U) {
    io.forwardB := 1.U
  }
  .elsewhen (io.memwbrw === true.B && io.memwbrd === io.rs2 && io.memwbrd =/= 0.U) {
    io.forwardB := 2.U
  }
  .otherwise {
    io.forwardB := 0.U
  }
}
