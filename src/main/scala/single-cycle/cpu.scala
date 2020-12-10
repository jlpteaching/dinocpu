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
  val pc         = RegInit(0.U)
  val control    = Module(new Control())
  val registers  = Module(new RegisterFile())
  val aluControl = Module(new ALUControl())
  val alu        = Module(new ALU())
  val immGen     = Module(new ImmediateGenerator())
  val pcPlusFour = Module(new Adder())
  val branchAdd  = Module(new Adder())
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  //FETCH
  io.imem.address := pc
  io.imem.valid := true.B

  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  val instruction = io.imem.instruction
  val opcode = instruction(6,0)

  //DECODE
  control.io.opcode := opcode

  registers.io.readreg1 := instruction(19,15)
  registers.io.readreg2 := instruction(24,20)

  registers.io.writereg := instruction(11,7)
  registers.io.wen      := (control.io.regwrite) && (registers.io.writereg =/= 0.U)

  immGen.io.instruction := instruction
  val imm = immGen.io.sextImm

  // EXECUTE
  aluControl.io.aluop  := control.io.aluop
  aluControl.io.itype  := control.io.itype
  aluControl.io.funct7 := instruction(31,25)
  aluControl.io.funct3 := instruction(14,12)

  alu.io.operation := aluControl.io.operation

  when (control.io.pcadd) {
    alu.io.inputx := pc
  } .otherwise {
    alu.io.inputx := registers.io.readdata1
  }

  when (control.io.alusrc) {
    alu.io.inputy := imm
  } .otherwise {
    alu.io.inputy := registers.io.readdata2
  }

  val result = MuxCase(0.U, Array(
                        (control.io.resultselect === 0.U) -> alu.io.result,
                        (control.io.resultselect === 1.U) -> imm,
                        (control.io.resultselect === 2.U) -> pcPlusFour.io.result))

  //MEMORY
  io.dmem.address   := alu.io.result
  io.dmem.writedata := registers.io.readdata2
  io.dmem.memread   := control.io.memread
  io.dmem.memwrite  := control.io.memwrite
  io.dmem.maskmode  := instruction(13,12)
  io.dmem.sext      := ~instruction(14)
  when(io.dmem.memread || io.dmem.memwrite) {
    io.dmem.valid := true.B
  } .otherwise {
    io.dmem.valid := false.B
  }

  //WRITEBACK
  registers.io.writedata := MuxCase(0.U, Array(
                        (control.io.toreg === 0.U) -> result,
                        (control.io.toreg === 1.U) -> io.dmem.readdata))


  branchAdd.io.inputx := pc
  branchAdd.io.inputy := imm

  when (control.io.branch && alu.io.result(0)) {
    pc := branchAdd.io.result
  } .elsewhen (control.io.jump) {
    when (control.io.pcfromalu) {
      pc := alu.io.result & Cat(Fill(31, 1.U), 0.U)
    } .otherwise {
      pc := branchAdd.io.result
    }
  } .otherwise {
    pc := pcPlusFour.io.result
  }
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
      "branchCtrl",
      "pcPlusFour",
      "branchAdd"
    )
  }
}
