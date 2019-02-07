// This file is where all of the CPU components are assembled into the whole CPU

package dinocpu

import chisel3._
import chisel3.util._


/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.6 of Patterson and Hennessy
 * This follows figure 4.49
 *
 * This CPU has no hazard detection, so any data hazards require explicit
 * nops in the application. For control hazards, this CPU assumes the branch
 * is not taken and will squash any incorrectly fetched instructions after the
 * branch is resolved in the memory stage.
 */
class FiveCycleCPU(implicit val conf: CPUConfig) extends Module {
  val io = IO(new CoreIO)

  // Bundles defining the pipeline registers and control structures

  // Everything in the register between IF and ID stages
  class IFIDBundle extends Bundle {
    val instruction = UInt(32.W)
    val pc          = UInt(32.W)
    val pcplusfour  = UInt(32.W)
  }

  // Control signals used in EX stage
   class EXControl extends Bundle {
    val add       = Bool()
    val immediate = Bool()
    val alusrc1   = UInt(2.W)
    val branch    = Bool()
    val jump     = UInt(2.W)
  }

  // Control signals used in MEM stage
  class MControl extends Bundle {
    val memread  = Bool()
    val memwrite = Bool()
    val taken    = Bool()
    val maskmode = UInt(2.W)
    val sext     = Bool()
  }

  // Control signals used in EB stage
  class WBControl extends Bundle {
    val toreg    = UInt(2.W)
    val regwrite = Bool()
  }

  // Everything in the register between ID and EX stages
  class IDEXBundle extends Bundle {
    val writereg  = UInt(5.W)
    val funct7    = UInt(7.W)
    val funct3    = UInt(3.W)
    val imm       = UInt(32.W)
    val readdata2 = UInt(32.W)
    val readdata1 = UInt(32.W)
    val pc        = UInt(32.W)
    val pcplusfour= UInt(32.W)
    val excontrol = new EXControl
    val mcontrol  = new MControl
    val wbcontrol = new WBControl
  }

  // Everything in the register between ID and EX stages
  class EXMEMBundle extends Bundle {
    val writereg  = UInt(5.W)
    val readdata2 = UInt(32.W)
    val aluresult = UInt(32.W)
    val taken     = Bool()
    val nextpc    = UInt(32.W)
    val pcplusfour= UInt(32.W)
    val mcontrol  = new MControl
    val wbcontrol = new WBControl
  }

  // Everything in the register between ID and EX stages
  class MEMWBBundle extends Bundle {
    val writereg  = UInt(5.W)
    val aluresult = UInt(32.W)
    val readdata  = UInt(32.W)
    val pcplusfour= UInt(32.W)
    val wbcontrol = new WBControl
  }

  // All of the structures required
  val pc         = RegInit(0.U)
  val control    = Module(new Control())
  val branchCtrl = Module(new BranchControl())
  val registers  = Module(new RegisterFile())
  val aluControl = Module(new ALUControl())
  val alu        = Module(new ALU())
  val immGen     = Module(new ImmediateGenerator())
  val pcPlusFour = Module(new Adder())
  val branchAdd  = Module(new Adder())
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  val if_id      = RegInit(0.U.asTypeOf(new IFIDBundle))
  val id_ex      = RegInit(0.U.asTypeOf(new IDEXBundle))
  val ex_mem     = RegInit(0.U.asTypeOf(new EXMEMBundle))
  val mem_wb     = RegInit(0.U.asTypeOf(new MEMWBBundle))

  printf("Cycle=%d ", cycleCount)

  // Forward declaration of wires that connect different stages

  // From memory back to fetch. Since we don't decide whether to take a branch or not until the memory stage.
  val next_pc      = Wire(UInt())
  val branch_taken = Wire(Bool())

  // For flushing stages.
  val bubble_idex  = Wire(Bool())
  val bubble_exmem = Wire(Bool())

  /////////////////////////////////////////////////////////////////////////////
  // FETCH STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Note: This comes from the memory stage!
  pc := Mux(branch_taken,
            next_pc,
            pcPlusFour.io.result)

  // Send the PC to the instruction memory port to get the instruction
  io.imem.address := pc

  // Get the PC + 4
  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  // Fill the IF/ID register
  if_id.instruction := io.imem.instruction
  if_id.pc          := pc
  if_id.pcplusfour  := pcPlusFour.io.result

  printf("pc=0x%x\n", pc)

  printf(p"IF/ID: $if_id\n")

  /////////////////////////////////////////////////////////////////////////////
  // ID STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Send opcode to control
  control.io.opcode := if_id.instruction(6,0)

  // Send register numbers to the register file
  registers.io.readreg1 := if_id.instruction(19,15)
  registers.io.readreg2 := if_id.instruction(24,20)

  // Send the instruction to the immediate generator
  immGen.io.instruction := if_id.instruction

  // Fill the id_ex register
  id_ex.writereg   := if_id.instruction(11,7)
  id_ex.funct7     := if_id.instruction(31,25)
  id_ex.funct3     := if_id.instruction(14,12)
  id_ex.imm        := immGen.io.sextImm
  id_ex.readdata2  := registers.io.readdata2
  id_ex.readdata1  := registers.io.readdata1
  id_ex.pc         := if_id.pc
  id_ex.pcplusfour := if_id.pcplusfour

  // Check for bubble
  when (bubble_idex) {
    // Set the id_ex control to 0 to indicate a bubble
    id_ex.excontrol := 0.U.asTypeOf(new EXControl)
    id_ex.mcontrol  := 0.U.asTypeOf(new MControl)
    id_ex.wbcontrol := 0.U.asTypeOf(new WBControl)
  } .otherwise {
    // Otherwise, set the id_ex control
    // Set the execution control signals
    id_ex.excontrol.add       := control.io.add
    id_ex.excontrol.immediate := control.io.immediate
    id_ex.excontrol.alusrc1   := control.io.alusrc1
    id_ex.excontrol.branch    := control.io.branch
    id_ex.excontrol.jump      := control.io.jump

    // Set the memory control signals
    id_ex.mcontrol.memread  := control.io.memread
    id_ex.mcontrol.memwrite := control.io.memwrite
    id_ex.mcontrol.maskmode := if_id.instruction(13,12)
    id_ex.mcontrol.sext := ~if_id.instruction(14)

    // Set the writeback control signals
    id_ex.wbcontrol.toreg    := control.io.toreg
    id_ex.wbcontrol.regwrite := control.io.regwrite
  }

  printf("DASM(%x)\n", if_id.instruction)
  printf(p"ID/EX: $id_ex\n")

  /////////////////////////////////////////////////////////////////////////////
  // EX STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Send data to the ALU control (line 45 of single-cycle/cpu.scala)
  aluControl.io.add  := id_ex.excontrol.add
  aluControl.io.immediate := id_ex.excontrol.immediate
  aluControl.io.funct7 := id_ex.funct7
  aluControl.io.funct3 := id_ex.funct3

  // Send data to the branch control (line 54 of single-cycle/cpu.scala)
  branchCtrl.io.branch := id_ex.excontrol.branch
  branchCtrl.io.funct3 := id_ex.funct3
  branchCtrl.io.inputx := id_ex.readdata1
  branchCtrl.io.inputy := id_ex.readdata2

  val alu_inputx = Wire(UInt(32.W))
  alu_inputx := DontCare
  // Insert the ALU inpux mux here
  switch(id_ex.excontrol.alusrc1) {
    is(0.U) { alu_inputx := id_ex.readdata1 }
    is(1.U) { alu_inputx := 0.U }
    is(2.U) { alu_inputx := id_ex.pc }
  }
  alu.io.inputx := alu_inputx

  // Input y mux (line 66 of single-cycle/cpu.scala)
  alu.io.inputy := Mux(id_ex.excontrol.immediate, id_ex.imm, id_ex.readdata2)

  // Set the ALU operation
  alu.io.operation := aluControl.io.operation

  // Connect the branchAdd unit
  branchAdd.io.inputx := id_ex.pc
  branchAdd.io.inputy := id_ex.imm

  // Set the ex_mem register values
  ex_mem.readdata2  := id_ex.readdata2
  ex_mem.aluresult  := alu.io.result
  ex_mem.writereg   := id_ex.writereg
  ex_mem.pcplusfour := id_ex.pcplusfour
  ex_mem.mcontrol   := id_ex.mcontrol
  ex_mem.wbcontrol  := id_ex.wbcontrol

  // Calculate whether which PC we should use and set the taken flag (line 92 in single-cycle/cpu.scala)
  when (branchCtrl.io.taken || id_ex.excontrol.jump === 2.U) {
    ex_mem.nextpc := branchAdd.io.result
    ex_mem.taken  := true.B
  } .elsewhen (id_ex.excontrol.jump === 3.U) {
    ex_mem.nextpc := alu.io.result & Cat(Fill(31, 1.U), 0.U)
    ex_mem.taken  := true.B
  } .otherwise {
    ex_mem.taken  := false.B
    ex_mem.nextpc := DontCare // No need to set the PC if not a branch
  }

  // Bubble memory if needed
  when (bubble_exmem) {
    ex_mem.mcontrol  := 0.U.asTypeOf(new MControl)
    ex_mem.wbcontrol := 0.U.asTypeOf(new WBControl)
  }

  printf(p"EX/MEM: $ex_mem\n")

  /////////////////////////////////////////////////////////////////////////////
  // MEM STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set data memory IO (line 71 of single-cycle/cpu.scala)
  io.dmem.address   := ex_mem.aluresult
  io.dmem.writedata := ex_mem.readdata2
  io.dmem.memread   := ex_mem.mcontrol.memread
  io.dmem.memwrite  := ex_mem.mcontrol.memwrite
  io.dmem.maskmode  := ex_mem.mcontrol.maskmode
  io.dmem.sext      := ex_mem.mcontrol.sext

  // Send this back to the fetch stage
  next_pc      := ex_mem.nextpc
  branch_taken := ex_mem.taken

  // Wire the MEM/WB register
  mem_wb.writereg   := ex_mem.writereg
  mem_wb.aluresult  := ex_mem.aluresult
  mem_wb.pcplusfour := ex_mem.pcplusfour
  mem_wb.readdata   := io.dmem.readdata
  mem_wb.wbcontrol  := ex_mem.wbcontrol

  // Now we know the direction of the branch. If it's taken, clear the control
  // for the previous stages.
  when (branch_taken) {
    bubble_exmem := true.B
    bubble_idex  := true.B
  } .otherwise {
    bubble_exmem := false.B
    bubble_idex  := false.B
  }

  printf(p"MEM/WB: $mem_wb\n")

  /////////////////////////////////////////////////////////////////////////////
  // WB STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the writeback data mux (line 78 single-cycle/cpu.scala)
  registers.io.writedata := MuxCase(mem_wb.aluresult, Array(
                            (mem_wb.wbcontrol.toreg === 0.U) -> mem_wb.aluresult,
                            (mem_wb.wbcontrol.toreg === 1.U) -> mem_wb.readdata,
                            (mem_wb.wbcontrol.toreg === 2.U) -> mem_wb.pcplusfour))

  // Write the data to the register file
  registers.io.writereg  := mem_wb.writereg
  registers.io.wen       := mem_wb.wbcontrol.regwrite && (registers.io.writereg =/= 0.U)

  printf("---------------------------------------------\n")
}
