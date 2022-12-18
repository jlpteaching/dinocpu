// Logic to calculate the next pc

package dinocpu.components

import chisel3._

/**
 * Next PC unit. This takes various inputs and outputs the next address of the next instruction.
 *
 * Input: branch         True if executing a branch instruction, False otherwise
 * Input: jumptype       00 if not a jump inst, 10 if inst is a jal, 11 if inst is a jalr
 * Input: inputx         First input
 * Input: inputy         Second input
 * Input: funct3         The funct3 from the instruction
 * Input: pc             The *current* program counter for this instruction
 * Input: imm            The sign-extended immediate
 *
 * Output: nextpc        The address of the next instruction
 * Output: taken         True if the next pc is not pc+4
 *
 */
class NextPC extends Module {
  val io = IO(new Bundle {
    val branch   = Input(Bool())
    val jumptype = Input(UInt(2.W))
    val inputx   = Input(UInt(64.W))
    val inputy   = Input(UInt(64.W))
    val funct3   = Input(UInt(3.W))
    val pc       = Input(UInt(64.W))
    val imm      = Input(UInt(64.W))

    val nextpc   = Output(UInt(64.W))
    val taken    = Output(Bool())
  })

  when (io.branch) {
    when ( (io.funct3 === "b000".U & io.inputx === io.inputy)
         | (io.funct3 === "b001".U & io.inputx =/= io.inputy)
         | (io.funct3 === "b100".U & io.inputx.asSInt < io.inputy.asSInt)
         | (io.funct3 === "b101".U & io.inputx.asSInt >= io.inputy.asSInt)
         | (io.funct3 === "b110".U & io.inputx < io.inputy)
         | (io.funct3 === "b111".U & io.inputx >= io.inputy)) {
      io.nextpc := io.pc + io.imm
      io.taken := true.B
    }
    .otherwise {
      io.nextpc := io.pc + 4.U
      io.taken := false.B
    }
  } .elsewhen (io.jumptype =/= 0.U) {
    io.nextpc := Mux(io.jumptype(0), io.inputx + io.imm, // jalr
                                     io.pc + io.imm)     // jal
    io.taken := true.B
  } .otherwise {
    io.nextpc := io.pc + 4.U
    io.taken := false.B
  }

}
