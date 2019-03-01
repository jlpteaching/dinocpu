// This file is where all of the CPU components are assembled into the whole CPU

package dinocpu

import chisel3._
import chisel3.util._

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.4 of Patterson and Hennessy
 * This follows figure 4.21
 */
class SingleCycleCPU(implicit val conf: CPUConfig) extends Module {
  val io = IO(new CoreIO())
  io := DontCare

  // All of the structures required
  val pc         = RegInit(0.U)
  val control    = Module(new Control())
  val registers  = Module(new RegisterFile())
  val aluControl = Module(new ALUControl())
  val alu        = Module(new ALU())
  val immGen     = Module(new ImmediateGenerator())
  val branchCtrl = Module(new BranchControl())
  val pcPlusFour = Module(new Adder())
  val branchAdd  = Module(new Adder())
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  io.imem.address := pc

  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  val instruction = io.imem.instruction
  val opcode = instruction(6,0)

  control.io.opcode := opcode

  registers.io.readreg1 := instruction(19,15)
  registers.io.readreg2 := instruction(24,20)

  registers.io.writereg := instruction(11,7)
  registers.io.wen      := control.io.regwrite && (registers.io.writereg =/= 0.U)

  aluControl.io.add       := control.io.add
  aluControl.io.immediate := control.io.immediate
  aluControl.io.funct7    := instruction(31,25)
  aluControl.io.funct3    := instruction(14,12)

  immGen.io.instruction := instruction
  val imm = immGen.io.sextImm

  branchCtrl.io.branch := control.io.branch
  branchCtrl.io.funct3 := instruction(14,12)
  branchCtrl.io.inputx := registers.io.readdata1
  branchCtrl.io.inputy := registers.io.readdata2

  val alu_inputx = Wire(UInt())
  alu_inputx := DontCare
  switch(control.io.alusrc1) {
    is(0.U) { alu_inputx := registers.io.readdata1 }
    is(1.U) { alu_inputx := 0.U }
    is(2.U) { alu_inputx := pc }
  }
  val alu_inputy = Mux(control.io.immediate, imm, registers.io.readdata2)
  alu.io.inputx := alu_inputx
  alu.io.inputy := alu_inputy
  alu.io.operation := aluControl.io.operation

  io.dmem.address   := alu.io.result
  io.dmem.writedata := registers.io.readdata2
  io.dmem.memread   := control.io.memread
  io.dmem.memwrite  := control.io.memwrite
  io.dmem.maskmode  := instruction(13,12)
  io.dmem.sext      := ~instruction(14)

  val write_data = Wire(UInt())
  when (control.io.toreg === 1.U) {
    write_data := io.dmem.readdata
  } .elsewhen (control.io.toreg === 2.U) {
    write_data := pcPlusFour.io.result
  } .otherwise {
    write_data := alu.io.result
  }

  registers.io.writedata := write_data

  branchAdd.io.inputx := pc
  branchAdd.io.inputy := imm
  val next_pc = Wire(UInt())
  when (branchCtrl.io.taken || control.io.jump === 2.U) {
    next_pc := branchAdd.io.result
  } .elsewhen (control.io.jump === 3.U) {
    next_pc := alu.io.result & Cat(Fill(31, 1.U), 0.U)
  } .otherwise {
    next_pc := pcPlusFour.io.result
  }

  // Debug / pipeline viewer
  val structures = List(
    (control, "control"),
    (registers, "registers"),
    (aluControl, "aluControl"),
    (alu, "alu"),
    (immGen, "immGen"),
    (branchCtrl, "branchCtrl"),
    (pcPlusFour, "pcPlusFour"),
    (branchAdd, "branchAdd")
  )

  if (conf.debug) {
    printf("DASM(%x)\n", instruction)
    printf(p"CYCLE=$cycleCount\n")
    printf(p"pc: $pc\n")
    for (structure <- structures) {
      printf(p"${structure._2}: ${structure._1.io}\n")
    }
    printf("\n")
  }

  pc := next_pc

}
