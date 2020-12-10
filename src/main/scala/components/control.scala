// Control logic for the processor

package dinocpu.components

import chisel3._
import chisel3.util.{BitPat, ListLookup}

/**
 * Main control logic for our simple processor
 *
 * Input: opcode:     Opcode from instruction
 *
 * Output: branch        true if branch or jump and link (jal). update PC with immediate
 * Output: pcfromalu     Use the pc from the ALU, not pc+4 or pc+imm
 * Output: jump          True if we want to update the PC with pc+imm regardless of the ALU result
 * Output: memread       true if we should read from memory
 * Output: memwrite      true if writing to the data memory
 * Output: regwrite      true if writing to the register file
 * Output: toreg         0 for result from execute, 1 for data from memory
 * Output: resultselect  00 for result from alu, 01 for immediate, 10 for pc+4
 * Output: alusrc        source for the second ALU input (0 is readdata2 and 1 is immediate)
 * Output: pcadd         Use PC as the input to the ALU
 * Output: itype         True if we're working on an itype instruction
 * Output: aluop         00 for ld/st, 10 for R-type, 01 for branch
 * Output: validinst     True if the instruction we're decoding is valid
 *
 * For more information, see section 4.4 of Patterson and Hennessy.
 * This follows figure 4.22.
 */

class Control extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(7.W))

    val branch       = Output(Bool())
    val pcfromalu    = Output(Bool())
    val jump         = Output(Bool())
    val memread      = Output(Bool())
    val memwrite     = Output(Bool())
    val regwrite     = Output(Bool())
    val toreg        = Output(UInt(1.W))
    val resultselect = Output(UInt(2.W))
    val alusrc       = Output(Bool())
    val pcadd        = Output(Bool())
    val itype        = Output(Bool())
    val aluop        = Output(UInt(2.W))
    val validinst    = Output(Bool())
  })

  val signals =
    ListLookup(io.opcode,
      /*default*/           List(false.B, false.B,   false.B, false.B,   false.B,  false.B,  0.U,   false.B,      false.B, false.B, false.B, 0.U,   false.B),
      Array(              /*     branch,  pcfromalu, jump,    memread,   memwrite, regwrite, toreg, resultselect, alusrc,  pcadd,   itype,   aluop, validinst */
      // R-format
      BitPat("b0110011") -> List(false.B, false.B,   false.B, false.B,   false.B,  true.B,   0.U,   0.U,          false.B, false.B, false.B, 2.U,   true.B),
      // I-format
      BitPat("b0010011") -> List(false.B, false.B,   false.B, false.B,   false.B,  true.B,   0.U,   0.U,          true.B,  false.B, true.B,  2.U,   true.B),
      // load
      BitPat("b0000011") -> List(false.B, false.B,   false.B, true.B,    false.B,  true.B,   1.U,   0.U,          true.B,  false.B, false.B, 0.U,   true.B),
      // store
      BitPat("b0100011") -> List(false.B, false.B,   false.B, false.B,   true.B,   false.B,  0.U,   0.U,          true.B,  false.B, false.B, 0.U,   true.B),
      // branch
      BitPat("b1100011") -> List(true.B,  false.B,   false.B, false.B,   false.B,  false.B,  0.U,   0.U,          false.B, false.B, false.B, 1.U,   true.B),
      // lui
      BitPat("b0110111") -> List(false.B, false.B,   false.B, false.B,   false.B,  true.B,   0.U,   1.U,          false.B, false.B, false.B, 0.U,   true.B),
      // auipc
      BitPat("b0010111") -> List(false.B, false.B,   false.B, false.B,   false.B,  true.B,   0.U,   0.U,          true.B,  true.B,  false.B, 0.U,   true.B),
      // jal
      BitPat("b1101111") -> List(false.B, false.B,   true.B,  false.B,   false.B,  true.B,   0.U,   2.U,          false.B, false.B, false.B, 0.U,   true.B),
      // jalr
      BitPat("b1100111") -> List(false.B, true.B,    true.B,  false.B,   false.B,  true.B,   0.U,   2.U,          true.B,  false.B, false.B, 0.U,   true.B),
      ) // Array
    ) // ListLookup

  io.branch       := signals(0)
  io.pcfromalu    := signals(1)
  io.jump         := signals(2)
  io.memread      := signals(3)
  io.memwrite     := signals(4)
  io.regwrite     := signals(5)
  io.toreg        := signals(6)
  io.resultselect := signals(7)
  io.alusrc       := signals(8)
  io.pcadd        := signals(9)
  io.itype        := signals(10)
  io.aluop        := signals(11)
  io.validinst    := signals(12)
}