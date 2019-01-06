// This file contains the ALU logic and the ALU control logic.

package dinocpu

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

  // Default to 0 output
  io.result := 0.U

  switch (io.operation) {
    is ("b0000".U) {
      io.result := io.inputx & io.inputy
    }
    is ("b0001".U) {
      io.result := io.inputx | io.inputy
    }
    is ("b0010".U) {
      io.result := io.inputx + io.inputy
    }
    is ("b0011".U) {
      io.result := io.inputx - io.inputy
    }
    is ("b0100".U) {
      io.result := (io.inputx.asSInt < io.inputy.asSInt).asUInt // signed
    }
    is ("b0101".U) {
      io.result := (io.inputx < io.inputy)
    }
    is ("b0110".U) {
      io.result := io.inputx << io.inputy(4,0)
    }
    is ("b0111".U) {
      io.result := io.inputx >> io.inputy(4,0)
    }
    is ("b1000".U) {
      io.result := (io.inputx.asSInt >> io.inputy(4,0)).asUInt // arithmetic (signed)
    }
    is ("b1001".U) {
      io.result := io.inputx ^ io.inputy
    }
    is ("b1010".U) {
      io.result := ~(io.inputx | io.inputy)
    }
  }
}
