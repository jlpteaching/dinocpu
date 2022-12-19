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
 * Output: the result of the computation
 */
class ALU extends Module {
  val io = IO(new Bundle {
    val operation = Input(UInt(5.W))
    val inputx    = Input(UInt(64.W))
    val inputy    = Input(UInt(64.W))

    val result    = Output(UInt(64.W))
  })

  val wordinst = Mux(io.operation(4) === 1.U, true.B, false.B)
  val aluop = io.operation(3, 0)

  // this function casts the input to 32-bit UInt, then sign extends it
  val signExtend32To64 = (input: UInt) => Cat(Fill(32, input(31)), input(31, 0))
  val operand1_32 = io.inputx(31, 0)
  val operand2_32 = io.inputy(31, 0)

  when (aluop === "b0110".U) { // and
    io.result := io.inputx & io.inputy
  }
  .elsewhen (aluop === "b0101".U) { // or
    io.result := io.inputx | io.inputy
  }
  .elsewhen (aluop === "b0111".U) { // add
    when (wordinst === true.B) {
      io.result := signExtend32To64(operand1_32 + operand2_32) // + results in width of max(width(op1), width(op2))
    } .otherwise {
      io.result := io.inputx + io.inputy
    }
  }
  .elsewhen (aluop === "b0100".U) { // sub
    when (wordinst === true.B) {
      io.result := signExtend32To64(operand1_32 - operand2_32)
    } .otherwise {
      io.result := io.inputx - io.inputy
    }
  }
  .elsewhen (aluop === "b0011".U) { // sra*
    when (wordinst === true.B) { // sraw
      io.result := signExtend32To64((operand1_32.asSInt >> operand2_32(4, 0)).asUInt) // arithmetic (signed)
                                                                                      // sraw takes 5 bits of op2
    } .otherwise { // sra
      io.result := (io.inputx.asSInt >> io.inputy(5, 0)).asUInt // sra takes 6 bits of op2
    }
  }
  .elsewhen (aluop === "b0001".U) { // sltu
    io.result := (io.inputx < io.inputy)
  }
  .elsewhen (aluop === "b0000".U) { // xor
    io.result := io.inputx ^ io.inputy
  }
  .elsewhen (aluop === "b0010".U) { // srl*
    when (wordinst === true.B) { // srlw
      io.result := signExtend32To64(operand1_32 >> operand2_32(4, 0)) // srlw takes 5 bits of op2
    } .otherwise {
      io.result := io.inputx >> io.inputy(5, 0) // srl takes 6 bits of op2
    }
  }
  .elsewhen (aluop === "b1001".U) { // slt
    io.result := (io.inputx.asSInt < io.inputy.asSInt).asUInt // signed
  }
  .elsewhen (aluop === "b1000".U) { // sll*
    when (wordinst === true.B) { // sllw
      io.result := signExtend32To64(operand1_32 << operand2_32(4, 0)) // sllw takes 5 bits of op2
    } .otherwise {
      io.result := io.inputx << io.inputy(5, 0) // sll takes 6 bits of op2
    }
  }
  .elsewhen (aluop === "b1010".U) { // nor
    io.result := ~(io.inputx | io.inputy)
  }
  .elsewhen (aluop === "b1011".U) { // sge (set greater than or equal)
    io.result := (io.inputx.asSInt >= io.inputy.asSInt).asUInt
  }
  .elsewhen (aluop === "b1100".U) { // sgeu (set greater than or equal unsigned)
    io.result := (io.inputx >= io.inputy)
  }
  .elsewhen (aluop === "b1101".U) { // seq (set equal)
    io.result := io.inputx === io.inputy
  }
  .elsewhen (aluop === "b1110".U) { // sne (set not equal)
    io.result := io.inputx =/= io.inputy
  }
  .otherwise {
    io.result := 0.U // should be invalid
  }
}
