// This file contains the hazard detection unit

package dinocpu

import chisel3._

/**
 * The hazard detection unit, modified for memory stalling
 *
 * Input:  rs1, the first source register number
 * Input:  rs2, the first source register number
 * Input:  idex_memread, true if the instruction in the ID/EX register is going to read from memory
 * Input:  idex_rd, the register number of the destination register for the instruction in the ID/EX register
 * Input:  exmem_taken, if true, then we are using the nextpc in the EX/MEM register, *not* pc+4.
 * Input:  imem_good, high if instruction memory is idle and ready.
 * Input:  dmem_good, high if data memory is idle and ready.
 *
 * Output: pcwrite, the value to write to the pc. If 0, pc+4, if 1 the next_pc from the memory stage,
 *         if 2, then the last pc value (2 stalls the pipeline)
 * Output: imem_disable, if true, prevent instruction memory from receiving requests
 * Output: ifid_bubble, if true, we should insert a bubble in the IF/ID stage
 * Output: ifid_disable, if true, explicitly prevent the IF/ID register from being written to
 * Output: ifid_flush, if true, set the IF/ID register to 0
 * Output: idex_bubble, if true, we should insert a bubble in the ID/EX stage
 * Output: idex_disable, if true, explicitly prevent the ID/EX register from being written to
 * Output: exmem_bubble, if true, we should insert a bubble in the EX/MEM stage
 * Output: exmem_disable, if true, explicitly prevent the EX/MEM register from being written to
 *
 * For more information, see Section 4.7 and beginning of 4.8 of Patterson and Hennessy
 * This follows the "Data hazards and stalls" section and the "Assume branch not taken" section
 */
class HazardUnitMemStall extends Module {
  val io = IO(new Bundle {
    val rs1           = Input(UInt(5.W))
    val rs2           = Input(UInt(5.W))
    val idex_memread  = Input(Bool())
    val idex_rd       = Input(UInt(5.W))
    val exmem_taken   = Input(Bool())
    val imem_good     = Input(Bool())
    val dmem_good     = Input(Bool())

    val pcwrite       = Output(UInt(2.W))
    val imem_disable  = Output(Bool())
    val ifid_bubble   = Output(Bool())
    val ifid_disable  = Output(Bool())
    val ifid_flush    = Output(Bool())
    val idex_bubble   = Output(Bool())
    val idex_disable  = Output(Bool())
    val exmem_bubble  = Output(Bool())
    val exmem_disable = Output(Bool())
  })

  // default
  io.pcwrite      := 0.U
  io.imem_disable  = false.B
  io.ifid_bubble   = false.B
  io.ifid_disable  = false.B
  io.ifid_flush    = false.B
  io.idex_bubble   = false.B
  io.idex_disable  = false.B
  io.exmem_bubble  = false.B
  io.exmem_disable = false.B

  // Load to use hazard.
  when (io.idex_memread &&
        (io.idex_rd === io.rs1 || io.idex_rd === io.rs2)) {
    io.pcwrite     := 2.U
    io.ifid_bubble := true.B
    io.idex_bubble := true.B
  }

  // branch flush
  when (io.exmem_taken && io.imem_good && io.dmem_good) {
    io.pcwrite := 1.U // use the PC from mem stage
    io.ifid_flush   := true.B
    io.idex_bubble  := true.B
    io.exmem_bubble := true.B
  }

  // imem stall:
  // Freeze the PC to preserve the current PC for the entire stall period,
  // so that on un-stall the next PC points to the next instruction
  when (! io.imem_good) {
    io.pcwrite := 2.U // Freeze the PC so whenever possible the CPU re-executes the same instruction
  }

  // dmem stall
  // Freeze the PC to preserve the current PC for imem
  // Disable any outbound instruction memory requests
  // Disable writes for IF/ID
  // Disable writes for ID/EX
  // Disable writes for EX/MEM
  when (! io.dmem_good) {
    io.pcwrite := 2.U
    io.imem_disable  := true.B
    io.ifid_disable  := true.B
    io.idex_disable  := true.B
    io.exmem_disable := true.B
  }
}
