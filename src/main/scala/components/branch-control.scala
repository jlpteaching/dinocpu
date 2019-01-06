// Control logic for whether branches are taken or not

package dinocpu

import chisel3._
import chisel3.util._

/**
 * Controls whether or not branches are taken.
 *
 * Input:  branch, true if we are looking at a branch
 * Input:  funct3, the middle three bits of the instruction (12-14). Specifies the
 *         type of branch
 * Input:  inputx, first value (e.g., reg1)
 * Input:  inputy, second value (e.g., reg2)
 * Output: taken, true if the branch is taken.
 */
class BranchControl extends Module {
  val io = IO(new Bundle {
    val branch = Input(Bool())
    val funct3 = Input(UInt(3.W))
    val inputx = Input(UInt(32.W))
    val inputy = Input(UInt(32.W))

    val taken  = Output(Bool())
  })
  io.taken := DontCare

  val check = Wire(Bool())
  check := DontCare

  switch(io.funct3) {
    is("b000".U) { check := (io.inputx === io.inputy) } // beq
    is("b001".U) { check := (io.inputx =/= io.inputy) } // bne
    is("b100".U) { check := (io.inputx.asSInt < io.inputy.asSInt) } // blt
    is("b101".U) { check := (io.inputx.asSInt >= io.inputy.asSInt) } // bge
    is("b110".U) { check := (io.inputx < io.inputy) } // bltu
    is("b111".U) { check := (io.inputx >= io.inputy) } // bgeu
  }

  io.taken := check & io.branch
}
