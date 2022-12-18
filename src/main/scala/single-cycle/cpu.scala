// This file is where all of the CPU components are assembled into the whole CPU

package dinocpu

import chisel3._
import chisel3.util._
import dinocpu.components._

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.4 of Patterson and Hennessy
 * This follows figure 4.21
 */
class SingleCycleCPU(implicit val conf: CPUConfig) extends BaseCPU {
  // All of the structures required
  val pc         = dontTouch(RegInit(0.U))
  val control    = Module(new Control())
  val registers  = Module(new RegisterFile())
  val aluControl = Module(new ALUControl())
  val alu        = Module(new ALU())
  val immGen     = Module(new ImmediateGenerator())
  val nextpc     = Module(new NextPC())
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  //FETCH
  io.imem.address := pc
  io.imem.valid := true.B

  val instruction = io.imem.instruction
  val funct3 = instruction(14, 12)

  control.io.opcode := instruction(6, 0)

  registers.io.readreg1 := instruction(19, 15)
  registers.io.readreg2 := instruction(24, 20)
  registers.io.writereg := instruction(11, 7)
  registers.io.writedata := Mux(control.io.toreg, io.dmem.readdata, Mux(control.io.resultselect, immGen.io.sextImm, alu.io.result))
  when (registers.io.writereg =/= 0.U && control.io.regwrite) {
    registers.io.wen := true.B
  } .otherwise {
    registers.io.wen := false.B
  }

  immGen.io.instruction := instruction

  nextpc.io.branch := control.io.branch
  nextpc.io.jumptype := control.io.jumptype
  nextpc.io.inputx := registers.io.readdata1
  nextpc.io.inputy := alu.io.inputy
  nextpc.io.funct3 := funct3
  nextpc.io.pc := pc
  nextpc.io.imm := immGen.io.sextImm

  aluControl.io.aluop := control.io.aluop
  aluControl.io.itype := control.io.itype
  aluControl.io.funct7 := instruction(31, 25)
  aluControl.io.funct3 := instruction(14, 12)
  aluControl.io.wordinst := control.io.wordinst

  alu.io.operation := aluControl.io.operation
  alu.io.inputx := Mux(control.io.src1, pc, registers.io.readdata1)
  alu.io.inputy := MuxCase(0.U, Array((control.io.src2 === 0.U) -> registers.io.readdata2,
                                      (control.io.src2 === 1.U) -> immGen.io.sextImm,
                                      (control.io.src2 === 2.U) -> 4.U))

  io.dmem.address := alu.io.result
  io.dmem.memread := ~control.io.memop(0)
  io.dmem.memwrite := control.io.memop(0)
  io.dmem.valid := control.io.memop(1)
  io.dmem.maskmode := funct3(1, 0)
  io.dmem.sext := ~funct3(2)
  io.dmem.writedata := registers.io.readdata2

  pc := nextpc.io.nextpc
}

/*
 * Object to make it easier to print information about the CPU
 */
object SingleCycleCPUInfo {
  def getModules(): List[String] = {
    List(
      "dmem",
      "imem",
      "control",
      "registers",
      "csr",
      "aluControl",
      "alu",
      "immGen",
      "nextpc"
    )
  }
}
