// This file contains the hazard detection unit

package dinocpu.components

import chisel3._

/**
 * The hazard detection unit
 *
 * Input:  rs1, the first source register number
 * Input:  rs2, the second source register number
 * Input:  idex_memread, true if the instruction in the ID/EX register is going to read from memory
 * Input:  idex_rd, the register number of the destination register for the instruction in the ID/EX register
 * Input:  exmem_taken, if true, then we are using the nextpc in the EX/MEM register, *not* pc+4.
 * Input:  imem_ready, if true, then the Instruction Memory is ready for another instruction
 * Input:  imem_good, if true, then an instruction was successfully retrieved and can unstall CPU
 * Input:  dmem_good, if true, then can unstall CPU for data memory
 *
 * Output: pcfromtaken, if true, use the pc from MEM
 * Output: pcstall, if true, stall the pipeline
 * Output: if_id_stall, if true, stall the if_id register.
 * Output: if_id_flush, if true, flush the if_id register.
 * Output: id_ex_stall, if true, stall the id_ex register.
 * Output: id_ex_flush, if true, flush the id_ex register.
 * Output: ex_mem_stall, if true, stall the ex_mem register.
 * Output: ex_mem_flush, if true, flush the ex_mem register.
 * Output: mem_wb_stall, if true, stall the mem_wb register.
 * Output: mem_wb_flush, if true, flush the mem_wb register.
 * Output: imem_valid, if true, imem can process new instruction
 *
 * For more information, see Section 4.7 and beginning of 4.8 of Patterson and Hennessy
 * This follows the "Data hazards and stalls" section and the "Assume branch not taken" section
 */
class HazardUnitNonCombin extends Module {
  val io = IO(new Bundle {
    val rs1          = Input(UInt(5.W))
    val rs2          = Input(UInt(5.W))
    val idex_memread = Input(Bool())
    val idex_rd      = Input(UInt(5.W))
    val exmem_taken  = Input(Bool())
    val imem_ready   = Input(Bool())
    val imem_good    = Input(Bool())
    val dmem_good    = Input(Bool())

    val pcfromtaken  = Output(Bool())
    val pcstall      = Output(Bool())
    val if_id_stall  = Output(Bool())
    val if_id_flush  = Output(Bool())
    val id_ex_stall  = Output(Bool())
    val id_ex_flush  = Output(Bool())
    val ex_mem_stall = Output(Bool())
    val ex_mem_flush = Output(Bool())
    val mem_wb_stall = Output(Bool())
    val mem_wb_flush = Output(Bool())
    val imem_valid   = Output(Bool())
  })

  // default
  io.pcfromtaken  := false.B
  io.pcstall      := false.B
  io.if_id_stall  := false.B
  io.if_id_flush  := false.B
  io.id_ex_stall  := false.B
  io.id_ex_flush  := false.B
  io.ex_mem_stall := false.B
  io.ex_mem_flush := false.B
  io.mem_wb_stall := false.B
  io.mem_wb_flush := false.B
  io.imem_valid   := true.B

  // Your code goes here
}
