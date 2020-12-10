// This file contains the ALU logic and the ALU control logic.

package dinocpu.components

import chisel3._
import chisel3.util._

/**
 * The ALU
 *
 * Input:  operation, specifies which operation the ALU should perform
 * Input:  inputx, the first input (e.g., reg1)
 * Input:  inputy, the second input (e.g., reg2)
 * Output: the result of the compuation
 */
class ALU extends Module {
  val io = IO(new Bundle {
    val operation = Input(UInt(4.W))
    val inputx    = Input(UInt(32.W))
    val inputy    = Input(UInt(32.W))

    val result    = Output(UInt(32.W))
  })

  when (io.operation === "b0000".U) { // and
    io.result := io.inputx & io.inputy
  }
  .elsewhen (io.operation === "b0001".U) { // or
    io.result := io.inputx | io.inputy
  }
  .elsewhen (io.operation === "b0010".U) { // add
    io.result := io.inputx + io.inputy
  }
  .elsewhen (io.operation === "b0011".U) { // sub
    io.result := io.inputx - io.inputy
  }
  .elsewhen (io.operation === "b0100".U) { // sra
    io.result := (io.inputx.asSInt >> io.inputy(4,0)).asUInt // arithmetic (signed)
  }
  .elsewhen (io.operation === "b0101".U) { // stlu
    io.result := (io.inputx < io.inputy)
  }
  .elsewhen (io.operation === "b0110".U) { // xor
    io.result := io.inputx ^ io.inputy
  }
  .elsewhen (io.operation === "b0111".U) { // srl
    io.result := io.inputx >> io.inputy(4,0)
  }
  .elsewhen (io.operation === "b1000".U) { // slt
    io.result := (io.inputx.asSInt < io.inputy.asSInt).asUInt // signed
  }
  .elsewhen (io.operation === "b1001".U) { // sll
    io.result := io.inputx << io.inputy(4,0)
  }
  .elsewhen (io.operation === "b1010".U) { // nor
    io.result := ~(io.inputx | io.inputy)
  }
  .elsewhen (io.operation === "b1011".U) { // sge (set greater than or equal)
    io.result := (io.inputx.asSInt >= io.inputy.asSInt).asUInt
  }
  .elsewhen (io.operation === "b1100".U) { // sgeu (set greater than or equal unsigned)
    io.result := (io.inputx >= io.inputy)
  }
  .elsewhen (io.operation === "b1101".U) { // seq (set equal)
    io.result := io.inputx === io.inputy
  }
  .elsewhen (io.operation === "b1110".U) { // sne (set not equal)
    io.result := io.inputx =/= io.inputy
  }
  .otherwise {
    io.result := 0.U // should be invalid
  }
}
