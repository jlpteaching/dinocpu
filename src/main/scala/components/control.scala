// Control logic for the processor

package dinocpu

import chisel3._
import chisel3.util.{BitPat, ListLookup}

/**
 * Main control logic for our simple processor
 *
 * Output: branch,  true if branch or jal and update PC with immediate
 * Output: memread, true if we should read memory
 * Output: toreg, 0 if writing ALU result, 1 if writing memory data, 2 if writing pc+4
 * Output: add, true if the ALU should add the results
 * Output: memwrite, write the memory
 * Output: regwrite, write the register file
 * Output: immediate, true if use the immediate value
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
    val toreg = Output(UInt(2.W))
    val add = Output(Bool())
    val memwrite = Output(Bool())
    val regwrite = Output(Bool())
    val immediate = Output(Bool())
    val alusrc1 = Output(UInt(2.W))
    val jump    = Output(UInt(2.W))
  })

  val signals =
    ListLookup(io.opcode,
      /*default*/           List(false.B, false.B, 3.U,   false.B, false.B,  false.B, false.B,    0.U,    0.U),
      Array(                 /*  branch,  memread, toreg, add,     memwrite, immediate, regwrite, alusrc1,  jump */
      // Invalid
      BitPat("b0000000") -> List(false.B, false.B, 0.U,   false.B, false.B,  false.B, false.B,     0.U,    0.U)
      ) // Array
    ) // ListLookup

  io.branch := signals(0)
  io.memread := signals(1)
  io.toreg := signals(2)
  io.add := signals(3)
  io.memwrite := signals(4)
  io.immediate := signals(5)
  io.regwrite := signals(6)
  io.alusrc1 := signals(7)
  io.jump := signals(8)
}
