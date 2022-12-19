// This file contains issue unit logic.

package dinocpu.components.dual

import chisel3._
import chisel3.util._

/**
 * The issue unit
 *
 * Input:  pc                        Current PC
 * Input:  inst                      64-bit data at PC loaded from imem
 * Input:  ignore_data_dependencies  If True, will issue both insts at pc and pc+4 even if they have data dependencies, False otherwise
 *
 * Output: nextpc         Where are the next instructions to fetch (either pc+4 or pc+8)
 * Output: pipeA_inst     The first instruction issued to the pipeline
 * Output: pipeA_valid    Whether pipeA_inst will be executed by the pipeline; if not, pipeA_inst must be a nop
 * Output: pipeB_inst     The first instruction issued to the pipeline
 * Output: pipeB_valid    Whether pipeB_inst will be executed by the pipeline; if not, pipeB_inst must be a nop
 */
class DualIssueIssueUnit extends Module {
  val io = IO(new Bundle {
    val pc           = Input(UInt(64.W))
    val inst         = Input(UInt(64.W))
    val ignore_data_dependencies = Input(Bool())

    val nextpc       = Output(UInt(64.W))
    val pipeA_inst   = Output(UInt(32.W))
    val pipeA_valid  = Output(Bool())
    val pipeB_inst   = Output(UInt(32.W))
    val pipeB_valid  = Output(Bool())
  })

  val inst1 = io.inst(31, 0)
  val inst2 = io.inst(63, 32)
  val inst1_opcode = inst1(6, 0)
  val inst2_opcode = inst2(6, 0)
  val oddPC = (io.pc % 8.U) === 4.U
  val will_issue_inst1 = Wire(Bool())
  val will_issue_inst2 = Wire(Bool())
  val nop = "h13".U(32.W)

  val inst1_rs1 = inst1(19, 15)
  val inst1_rs2 = inst1(24, 20)
  val inst1_rd  = inst1(11, 7)

  val inst2_rs1 = inst2(19, 15)
  val inst2_rs2 = inst2(24, 20)
  val inst2_rd  = inst2(11, 7)

  val opcode_to_regs = Array(/*   has_rs1, has_rs2,  has_rd */
      // R-format
      BitPat("b0110011") -> List( true.B,  true.B,  true.B),
      // I-format
      BitPat("b0010011") -> List( true.B, false.B,  true.B),
      // load
      BitPat("b0000011") -> List( true.B, false.B,  true.B),
      // store
      BitPat("b0100011") -> List( true.B,  true.B, false.B),
      // branch
      BitPat("b1100011") -> List( true.B,  true.B, false.B),
      // lui
      BitPat("b0110111") -> List(false.B, false.B,  true.B),
      // auipc
      BitPat("b0010111") -> List(false.B, false.B,  true.B),
      // jal
      BitPat("b1101111") -> List(false.B, false.B,  true.B),
      // jalr
      BitPat("b1100111") -> List( true.B, false.B,  true.B),
      // I-format 32-bit operands
      BitPat("b0011011") -> List( true.B, false.B,  true.B),
      // R-format 32-bit operands
      BitPat("b0111011") -> List( true.B,  true.B,  true.B)
  ) // Array

  val inst1_regs    = ListLookup(inst1_opcode, List(false.B, false.B, false.B), opcode_to_regs)
  val inst1_has_rd  = inst1_regs(2)

  val inst2_regs    = ListLookup(inst2_opcode, List(false.B, false.B, false.B), opcode_to_regs)
  val inst2_has_rs1 = inst2_regs(0)
  val inst2_has_rs2 = inst2_regs(1)

  // if PC is odd, inst1 has already been in the pipeline
  will_issue_inst1 := !oddPC

  // if inst1 is a branch/jump/load/store, inst2 won't be issued
  when (oddPC) {
    will_issue_inst2 := true.B
  } .elsewhen (   (inst1_opcode === "b0000011".U)
               || (inst1_opcode === "b0100011".U)
               || (inst1_opcode === "b1100011".U)
               || (inst1_opcode === "b1101111".U)
               || (inst1_opcode === "b1100111".U)) {
    will_issue_inst2 := false.B
  } .elsewhen (!io.ignore_data_dependencies && inst1_has_rd && inst1_rd =/= 0.U) {
    when ((inst2_has_rs1 && (inst1_rd === inst2_rs1)) || (inst2_has_rs2 && (inst1_rd === inst2_rs2))) {
      will_issue_inst2 := false.B
    } .otherwise {
      will_issue_inst2 := true.B
    }
  } .otherwise {
    will_issue_inst2 := true.B
  }


  assert(will_issue_inst1 || will_issue_inst2)
  when (!will_issue_inst1) { // only issue inst 2
    io.nextpc      := io.pc + 4.U
    io.pipeA_inst  := inst2
    io.pipeA_valid := true.B
    io.pipeB_inst  := nop
    io.pipeB_valid := false.B
  } .elsewhen (!will_issue_inst2) { // only issue inst 1
    io.nextpc      := io.pc + 4.U
    io.pipeA_inst  := inst1
    io.pipeA_valid := true.B
    io.pipeB_inst  := nop
    io.pipeB_valid := false.B
  } .otherwise { // issue both inst 1 and inst 2
    io.nextpc      := io.pc + 8.U
    io.pipeA_inst  := inst1
    io.pipeA_valid := true.B
    io.pipeB_inst  := inst2
    io.pipeB_valid := true.B
  }

}
