// This file contains the hazard detection unit

package dinocpu.components.dual

import chisel3._

/**
 * The hazard detection unit with support for dual-issue pipeline
 *
 * Input:  rs1, the first source register number
 * Input:  rs2, the second source register number
 * Input:  idex_memread, true if the instruction in the ID/EX register is going to read from memory
 * Input:  idex_rd, the register number of the destination register for the instruction in the ID/EX register
 * Input:  exmem_taken, if true, then we are using the nextpc in the EX/MEM register, *not* pc+4.
 * Input:  id_pipeA_rs1, the first source register number from pipeA
 * Input:  id_pipeA_rs2, the second source register number from pipeA
 * Input:  id_pipeB_rs1, the first source register number from pipeB
 * Input:  id_pipeB_rs2, the second source register number from pipeB
 *
 * Input:  pipeA_idex_memread, true if the instruction in pipeA's ID/EX register is going to read from memory
 * Input:  pipeA_idex_rd, the register number of the destination register for the instruction in the pipeA's ID/EX register
 * Input:  pipeA_exmem_taken, if true, then we are using the nextpc in the pipeA's EX/MEM register, *not* pc+4
 * Input:  pipeB_idex_memread, true if the instruction in pipeB's ID/EX register is going to read from memory
 * Input:  pipeB_idex_rd, the register number of the destination register for the instruction in the pipeB's ID/EX register
 * Input:  pipeB_exmem_taken, if true, then we are using the nextpc in the pipeB's EX/MEM register, *not* pc+4
 *
 * Output: pcfromtaken, if true, use the pc from MEM
 * Output: pcstall, if true, stall the pipeline
 * Output: if_id_stall, if true, we should insert a bubble in the IF/ID stage
 * Output: id_ex_flush, if true, we should insert a bubble in the ID/EX stage
 * Output: ex_mem_flush, if true, we should insert a bubble in the EX/MEM stage
 * Output: if_id_flush, if true, set the IF/ID register to 0
 *
 * For more information, see Section 4.7 and beginning of 4.8 of Patterson and Hennessy
 * This follows the "Data hazards and stalls" section and the "Assume branch not taken" section
 */
class DualIssueHazardUnit extends Module {
  val io = IO(new Bundle {
    val id_pipeA_rs1 = Input(UInt(5.W))
    val id_pipeA_rs2 = Input(UInt(5.W))
    val id_pipeB_rs1 = Input(UInt(5.W))
    val id_pipeB_rs2 = Input(UInt(5.W))

    val pipeA_idex_memread = Input(Bool())
    val pipeA_idex_rd      = Input(UInt(5.W))
    val pipeA_exmem_taken  = Input(Bool())
    val pipeB_idex_memread = Input(Bool())
    val pipeB_idex_rd      = Input(UInt(5.W))
    val pipeB_exmem_taken  = Input(Bool())

    val pcfromtaken  = Output(Bool())
    val pcstall      = Output(Bool())
    val if_id_stall  = Output(Bool())
    val id_ex_flush  = Output(Bool())
    val ex_mem_flush = Output(Bool())
    val if_id_flush  = Output(Bool())
  })

  // default
  io.pcfromtaken  := false.B
  io.pcstall      := false.B
  io.if_id_stall  := false.B
  io.id_ex_flush  := false.B
  io.ex_mem_flush := false.B
  io.if_id_flush  := false.B

  val stalling_due_to_pipeA = Wire(Bool())
  val stalling_due_to_pipeB = Wire(Bool())

  stalling_due_to_pipeA := io.pipeA_idex_memread && (io.pipeA_idex_rd === io.id_pipeA_rs1 || io.pipeA_idex_rd === io.id_pipeA_rs2 || io.pipeA_idex_rd === io.id_pipeB_rs1 || io.pipeA_idex_rd === io.id_pipeB_rs2)
  stalling_due_to_pipeB := io.pipeB_idex_memread && (io.pipeB_idex_rd === io.id_pipeA_rs1 || io.pipeB_idex_rd === io.id_pipeA_rs2 || io.pipeB_idex_rd === io.id_pipeB_rs1 || io.pipeB_idex_rd === io.id_pipeB_rs2)

  when (io.pipeA_exmem_taken || io.pipeB_exmem_taken) {
    io.pcfromtaken  := true.B
    io.pcstall      := false.B
    io.if_id_stall  := false.B
    io.if_id_flush  := true.B
    io.id_ex_flush  := true.B
    io.ex_mem_flush := true.B
  } .elsewhen (stalling_due_to_pipeB || stalling_due_to_pipeA) {
    io.pcfromtaken  := false.B
    io.pcstall      := true.B
    io.if_id_stall  := true.B
    io.id_ex_flush  := true.B
    io.ex_mem_flush := false.B
    io.if_id_flush  := false.B
  }

}
