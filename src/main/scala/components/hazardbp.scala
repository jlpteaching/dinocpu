// This file contains the hazard detection unit

package dinocpu.components

import chisel3._

/**
 * The hazard detection unit
 *
 * Input:  rs1, the first source register number
 * Input:  rs2, the first source register number
 * Input:  id_branch, if true, then the branch in ID is taken
 * Input:  idex_memread, true if the instruction in the ID/EX register is going to read from memory
 * Input:  idex_rd, the register number of the destination register for the instruction in the ID/EX register
 * Input:  exmem_taken, if true, then we are using the nextpc in the EX/MEM register, *not* pc+4.
 *
 * Output: pcwrite, the value to write to the pc. If 0, pc+4, if 1 the next_pc from the memory stage,
 *         if 2, then the last pc value (2 stalls the pipeline), if 3, then use next_pc from ID
 * Output: ifid_bubble, if true, we should instet a bubble in the IF/ID stage
 * Output: idex_bubble, if true, we should insert a bubble in the ID/EX stage
 * Output: exmem_bubble, if true, we should insert a bubble in the EX/MEM stage
 * Output: ifid_flush, if true, set the IF/ID register to 0
 *
 * For more information, see Section 4.7 and beginning of 4.8 of Patterson and Hennessy
 * This follows the "Data hazards and stalls" section and the "Assume branch not taken" section
 */
class HazardUnitBP extends Module {
  val io = IO(new Bundle {
    val rs1          = Input(UInt(5.W))
    val rs2          = Input(UInt(5.W))
    val id_branch    = Input(Bool())
    val idex_memread = Input(Bool())
    val idex_rd      = Input(UInt(5.W))
    val exmem_taken  = Input(Bool())

    val pcwrite      = Output(UInt(2.W))
    val ifid_bubble  = Output(Bool())
    val idex_bubble  = Output(Bool())
    val exmem_bubble = Output(Bool())
    val ifid_flush   = Output(Bool())
  })

  // default
  io.pcwrite      := 0.U
  io.ifid_bubble  := false.B
  io.idex_bubble  := false.B
  io.exmem_bubble := false.B
  io.ifid_flush   := false.B

  // Your code goes here
}
