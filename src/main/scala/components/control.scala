// Control logic for the processor

package dinocpu.components

import chisel3._
import chisel3.util.{BitPat, ListLookup}

/**
 * Main control logic for our simple processor
 *
 * Input: opcode:        Opcode from instruction
 *
 * Output: itype         True if we're working on an itype instruction, False otherwise
 * Output: aluop         True if inst is of R-type or I-type, False otherwise
 * Output: src1          Source for the first ALU/nextpc input (0 if source is readdata1, 1 if source is pc)
 * Output: src2          Source for the second ALU/nextpc input (00 if source is readdata2, 01 if source is immediate, 10 if source is a hardwired value 4 (i.e., alu's inputy = 4))
 * Output: branch        True if branch, False otherwise
 * Output: jumptype      00 if not a jump inst, 10 if inst is jal, 11 is inst is jalr
 * Output: resultselect  0 for result from alu, 1 for immediate
 * Output: memop         00 if not using memory, 10 if reading, and 11 if writing
 * Output: toreg         0 for result from execute, 1 for data from memory
 * Output: regwrite      True if writing to the register file, False otherwise
 * Output: validinst     True if the instruction we're decoding is valid, False otherwise
 * Output: wordinst      True if the instruction *only* operates on 32-bit operands, False otherwise
 *
 * For more information, see section 4.4 of Patterson and Hennessy.
 * This follows figure 4.22.
 */

class Control extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(7.W))

    val itype        = Output(Bool())
    val aluop        = Output(Bool())
    val src1         = Output(Bool())
    val src2         = Output(UInt(2.W))
    val branch       = Output(Bool())
    val jumptype     = Output(UInt(2.W))
    val resultselect = Output(Bool())
    val memop        = Output(UInt(2.W))
    val toreg        = Output(Bool())
    val regwrite     = Output(Bool())
    val validinst    = Output(Bool())
    val wordinst     = Output(Bool())
  })

  val signals =
    ListLookup(io.opcode,
      /*default*/           List(false.B, false.B, false.B,  0.U,  false.B,       0.U,      false.B,   0.U, false.B,  false.B,   false.B,  false.B),
      Array(              /*       itype,   aluop,    src1, src2,   branch,  jumptype, resultselect, memop,   toreg, regwrite, validinst, wordinst */
      // R-format
      BitPat("b0110011") -> List(false.B,  true.B, false.B,  0.U,  false.B,       0.U,      false.B,   0.U, false.B,   true.B,    true.B,  false.B),
      // I-format
      BitPat("b0010011") -> List( true.B,  true.B, false.B,  1.U,  false.B,       0.U,      false.B,   0.U, false.B,   true.B,    true.B,  false.B),
      // load
      BitPat("b0000011") -> List(false.B, false.B, false.B,  1.U,  false.B,       0.U,      false.B,   2.U,  true.B,   true.B,    true.B,  false.B),
      // store
      BitPat("b0100011") -> List(false.B, false.B, false.B,  1.U,  false.B,       0.U,      false.B,   3.U, false.B,  false.B,    true.B,  false.B),
      // branch
      BitPat("b1100011") -> List(false.B, false.B, false.B,  0.U,   true.B,       0.U,      false.B,   0.U, false.B,  false.B,    true.B,  false.B),
      // lui
      BitPat("b0110111") -> List(false.B, false.B, false.B,  0.U,  false.B,       0.U,       true.B,   0.U, false.B,   true.B,    true.B,  false.B),
      // auipc
      BitPat("b0010111") -> List(false.B, false.B,  true.B,  1.U,  false.B,       0.U,      false.B,   0.U, false.B,   true.B,    true.B,  false.B),
      // jal
      BitPat("b1101111") -> List(false.B, false.B,  true.B,  2.U,  false.B,       2.U,      false.B,   0.U, false.B,   true.B,    true.B,  false.B),
      // jalr
      BitPat("b1100111") -> List(false.B, false.B,  true.B,  2.U,  false.B,       3.U,      false.B,   0.U, false.B,   true.B,    true.B,  false.B),
      // I-format 32-bit operands
      BitPat("b0011011") -> List( true.B,  true.B, false.B,  1.U,  false.B,       0.U,      false.B,   0.U, false.B,   true.B,    true.B,   true.B),
      // R-format 32-bit operands
      BitPat("b0111011") -> List(false.B,  true.B, false.B,  0.U,  false.B,       0.U,      false.B,   0.U, false.B,   true.B,    true.B,   true.B),
      ) // Array
    ) // ListLookup

  io.itype        := signals(0)
  io.aluop        := signals(1)
  io.src1         := signals(2)
  io.src2         := signals(3)
  io.branch       := signals(4)
  io.jumptype     := signals(5)
  io.resultselect := signals(6)
  io.memop        := signals(7)
  io.toreg        := signals(8)
  io.regwrite     := signals(9)
  io.validinst    := signals(10)
  io.wordinst     := signals(11)
}
