// Control logic for the processor

package CODCPU

import chisel3._
import chisel3.util.{BitPat, ListLookup}

/**
 * Main control logic for our simple processor
 *
 * Output: branch,  true if branch or jal and update PC with immediate
 * Output: memread, true if we should read memory
 * Output: memtoreg, true if we are writing the memory value to the register
 * Output: memop, true if this is a memory operation
 * Output: memwrite, write the memory
 * Output: regwrite, write the register file
 * Output: alusrc2, true if use the immediate value
 * Output: alusrc1, 0 is Read data 1, 1 is zero, 2 is PC
 * Output: jump, 0 no jump, 2 jump, 3 jump and link register
 *
 * For more information, see section 4.4 of Patterson and Hennessy
 * This follows figure 4.22
 */
class Control extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(7.W))

    val branch = Output(Bool())
    val memread = Output(Bool())
    val memtoreg = Output(Bool())
    val memop = Output(Bool())
    val memwrite = Output(Bool())
    val regwrite = Output(Bool())
    val alusrc2 = Output(Bool())
    val alusrc1 = Output(UInt(2.W))
    val jump    = Output(UInt(2.W))
  })

  val signals =
    ListLookup(io.opcode,
      /*default*/           List(false.B, false.B, false.B,  false.B, false.B,  false.B, false.B,   0.U,     0.U),
      Array(                 /*  branch,  memread, memtoreg, memop,   memwrite, alusrc2, regwrite, alusrc1,  jump */
      // R-format
      BitPat("b0110011") -> List(false.B, false.B, false.B,  false.B, false.B,  false.B, true.B,     0.U,    0.U),
      // load
      BitPat("b0000011") -> List(false.B, true.B,  true.B,   true.B,  false.B,  true.B,  true.B,     0.U,    0.U),
      // store
      BitPat("b0100011") -> List(false.B, false.B, false.B,  true.B,  true.B,   true.B,  false.B,    0.U,    0.U),
      // beq
      BitPat("b1100011") -> List(true.B,  false.B, false.B,  false.B, false.B,  false.B, false.B,    0.U,    0.U),
      // lui
      BitPat("b0110111") -> List(false.B, false.B, false.B,  false.B, false.B,  true.B,  true.B,     1.U,    0.U),
      // auipc
      BitPat("b0010111") -> List(false.B, false.B, false.B,  false.B, false.B,  true.B,  true.B,     2.U,    0.U),
      // jal
      BitPat("b1101111") -> List(true.B,  false.B, false.B,  false.B, false.B,  false.B, true.B,     1.U,    2.U),
      // jalr
      BitPat("b1100111") -> List(false.B, false.B, false.B,  false.B, false.B,  true.B,  true.B,     0.U,    3.U)
      ) // Array
    ) // ListLookup

  io.branch := signals(0)
  io.memread := signals(1)
  io.memtoreg := signals(2)
  io.memop := signals(3)
  io.memwrite := signals(4)
  io.alusrc2 := signals(5)
  io.regwrite := signals(6)
  io.alusrc1 := signals(7)
  io.jump := signals(8)
}
