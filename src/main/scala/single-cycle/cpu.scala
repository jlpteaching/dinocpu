
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

  // To make the FIRRTL compiler happy. Remove this as you connect up the I/O's
  //control.io    := DontCare
  //registers.io  := DontCare
  //aluControl.io := DontCare
  //alu.io        := DontCare
  immGen.io     := DontCare
  branchCtrl.io := DontCare
  //pcPlusFour.io := DontCare
  branchAdd.io  := DontCare

  io.imem.address := pc

//Program Counter
  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  val instruction = io.imem.instruction

  val opcode = instruction(6,0)
  control.io.opcode := opcode

//Reg address
  registers.io.readreg1 := instruction(19,15)
  registers.io.readreg2 := instruction(24,20)
  registers.io.writereg := instruction(11,7)
  registers.io.wen      := control.io.regwrite && (registers.io.writereg =/= 0.U)

//ALU Control
  aluControl.io.add       := control.io.add
  aluControl.io.immediate := control.io.immediate
  aluControl.io.funct7    := instruction(31,25)
  aluControl.io.funct3    := instruction(14,12)

//Reg Values
  val alu_inputx = registers.io.readdata1 
  val alu_inputy = registers.io.readdata2

//ALU operation
  alu.io.inputx := alu_inputx
  alu.io.inputy := alu_inputy
  alu.io.operation := aluControl.io.operation

  val write_data = alu.io.result
  registers.io.writedata := write_data

//Update PC
  val  next_pc = pcPlusFour.io.result

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

  printf("DASM(%x)\n", instruction)
  printf(p"CYCLE=$cycleCount\n")
  printf(p"pc: $pc\n")
  for (structure <- structures) {
    printf(p"${structure._2}: ${structure._1.io}\n")
    
  }
  printf("\n")

  pc := next_pc

}
