// This file contains the ALU logic and the ALU control logic.
// NOTE: This would be a good file to modify for different classes. With the ALU control different,
//       the students would have to think about how it's implemented.

package CODCPU

import chisel3._
import chisel3.util._

/**
 * The ALU control unit
 *
 * Input:  add, if true, add no matter what the other bits are
 * Input:  funct7, the most significant bits of the instruction
 * Input:  funct3, the middle three bits of the instruction (12-14)
 * Output: operation, What we want the ALU to do. See [[CODCPU.ALUConstants]]
 *
 * For more information, see Section 4.4 and A.5 of Patterson and Hennessy
 * This follows figure 4.12
 */
class ALUControl extends Module {
  val io = IO(new Bundle {
    val add       = Input(Bool())
    val funct7    = Input(UInt(7.W))
    val funct3    = Input(UInt(3.W))

    val operation = Output(UInt(4.W))
  })

  io.operation := 15.U // invalid operation

  when (io.add) {
    io.operation := "b0010".U
  } .otherwise {
    switch (io.funct7) {
      is ("b0100000".U) { // sub and sra
        switch (io.funct3) {
          is ("b000".U) { io.operation := "b0011".U } // sub
          is ("b101".U) { io.operation := "b1000".U } // sra
        }
      }
      is ("b0000000".U) {
        switch (io.funct3) {
          is ("b000".U) { io.operation := "b0010".U } // add
          is ("b001".U) { io.operation := "b0110".U } // sll
          is ("b010".U) { io.operation := "b0100".U } // slt
          is ("b011".U) { io.operation := "b0101".U } // sltu
          is ("b100".U) { io.operation := "b1001".U } // xor
          is ("b101".U) { io.operation := "b0111".U } // srl
          is ("b110".U) { io.operation := "b0001".U } // or
          is ("b111".U) { io.operation := "b0000".U } // and
        }
      }
    }
  }

}

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
      io.result := (io.inputx < io.inputy)
    }
    is ("b0101".U) {
      io.result := (io.inputx.asSInt < io.inputy.asSInt).asUInt // signed
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
