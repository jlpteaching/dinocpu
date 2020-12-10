// This file is where all of the CPU components are assembled into the whole CPU

package dinocpu.pipelined

import chisel3._
import chisel3.util._
import dinocpu._
import dinocpu.components._

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.6 of Patterson and Hennessy
 * This follows figure 4.49
 */
class PipelinedNonCombinCPU(implicit val conf: CPUConfig) extends BaseCPU {
  // Bundles defining the pipeline registers and control structures

  // Everything in the register between IF and ID stages
  class IFIDBundle extends Bundle {
    val instruction = UInt(32.W)
    val pc          = UInt(32.W)
    val pcplusfour  = UInt(32.W)
  }

  // Control signals used in EX stage
  class EXControl extends Bundle {
    val itype        = Bool()
    val aluop        = UInt(2.W)
    val resultselect = UInt(2.W)
    val alusrc       = Bool()
    val pcadd        = Bool()
    val branch       = Bool()
    val jump         = Bool()
    val pcfromalu    = Bool()
  }

  // Control signals used in MEM stage
  class MControl extends Bundle {
    val memread  = Bool()
    val memwrite = Bool()
    val taken    = Bool()
    val maskmode = UInt(2.W)
    val sext     = Bool()
  }

  // Control signals used in WB stage
  class WBControl extends Bundle {
    val toreg     = UInt(1.W)
    val regwrite  = Bool()
  }

  // Data of the the register between ID and EX stages
  class IDEXBundle extends Bundle {
    val writereg   = UInt(5.W)
    val funct7     = UInt(7.W)
    val funct3     = UInt(3.W)
    val imm        = UInt(32.W)
    val readdata2  = UInt(32.W)
    val readdata1  = UInt(32.W)
    val pc         = UInt(32.W)
    val pcplusfour = UInt(32.W)
    val rs1        = UInt(5.W)    //pipelined only
    val rs2        = UInt(5.W)    //pipelined only
  }

  // Control block of the IDEX register
  class IDEXControl extends Bundle {
    val ex_ctrl  = new EXControl
    val mem_ctrl = new MControl
    val wb_ctrl  = new WBControl
  }

  // Everything in the register between EX and MEM stages
  class EXMEMBundle extends Bundle {
    val writereg   = UInt(5.W)
    val readdata2  = UInt(32.W)
    val result     = UInt(32.W)
    val nextpc     = UInt(32.W)
  }

  // Control block of the EXMEM register
  class EXMEMControl extends Bundle {
    val mem_ctrl  = new MControl
    val wb_ctrl   = new WBControl
  }

  // Everything in the register between MEM and WB stages
  class MEMWBBundle extends Bundle {
    val writereg   = UInt(5.W)
    val result     = UInt(32.W)
    val readdata   = UInt(32.W)
  }

  // Control block of the MEMWB register
  class MEMWBControl extends Bundle {
    val wb_ctrl = new WBControl
  }

  // All of the structures required
  val pc         = RegInit(0.U)
  val control    = Module(new Control())
  val registers  = Module(new RegisterFile())
  val aluControl = Module(new ALUControl())
  val alu        = Module(new ALU())
  val immGen     = Module(new ImmediateGenerator())
  val pcPlusFour = Module(new Adder())
  val branchAdd  = Module(new Adder())
  val forwarding = Module(new ForwardingUnit())           //pipelined only
  val hazard     = Module(new HazardUnitNonCombin())      //pipelined only
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  // The four pipeline registers
  val if_id       = Module(new StageReg(new IFIDBundle))

  val id_ex       = Module(new StageReg(new IDEXBundle))
  val id_ex_ctrl  = Module(new StageReg(new IDEXControl))

  val ex_mem      = Module(new StageReg(new EXMEMBundle))
  val ex_mem_ctrl = Module(new StageReg(new EXMEMControl))

  val mem_wb      = Module(new StageReg(new MEMWBBundle))
  // To make the interface of the mem_wb_ctrl register consistent with the other control
  // registers, we create an anonymous Bundle
  val mem_wb_ctrl = Module(new StageReg(new MEMWBControl))

  // Forward declaration of wires that connect different stages

  // From memory back to fetch. Since we don't decide whether to take a branch or not until the memory stage.
  val next_pc      = Wire(UInt())

  // For wb back to other stages
  val write_data = Wire(UInt())

  // Checking if memory instruction in memory stage
  val memInst = Wire(Bool())

  /////////////////////////////////////////////////////////////////////////////
  // FETCH STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Note: This comes from the memory stage!
  // Only update the pc if pcstall is false
  pc := Mux(hazard.io.pcstall, pc,
           Mux(hazard.io.pcfromtaken, next_pc, pcPlusFour.io.result))

  // Send the PC to the instruction memory port to get the instruction
  io.imem.address := pc

  // Send a valid instruction request to instruction memory only if IFID isn't
  // being bubbled;
  io.imem.valid := hazard.io.imem_valid

  // Get the PC + 4
  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  // Data supplied to if_id register is bubbled when the hazard detector
  // indicates a IFID bubble
  if_id.io.valid := !hazard.io.if_id_stall

  // Connect outputs of IF stage into the stage register's in port
  if_id.io.in.instruction := io.imem.instruction
  if_id.io.in.pc          := pc
  if_id.io.in.pcplusfour  := pcPlusFour.io.result

  if_id.io.flush  := hazard.io.if_id_flush

  // Inform hazard unit if Instruction Memory got next instruction
  hazard.io.imem_ready := io.imem.ready
  hazard.io.imem_good  := io.imem.good

  /////////////////////////////////////////////////////////////////////////////
  // ID STAGE
  /////////////////////////////////////////////////////////////////////////////

  val rs1 = if_id.io.data.instruction(19,15)
  val rs2 = if_id.io.data.instruction(24,20)

  // Send input from this stage to hazard detection unit
  hazard.io.rs1       := rs1
  hazard.io.rs2       := rs2

  // Send opcode to control
  control.io.opcode := if_id.io.data.instruction(6,0)

  // Send register numbers to the register file
  registers.io.readreg1 := rs1
  registers.io.readreg2 := rs2

  // Send the instruction to the immediate generator
  immGen.io.instruction := if_id.io.data.instruction

  // Don't need to flush the data in this register
  id_ex.io.flush := hazard.io.id_ex_flush
  // FIll the id_ex register
  id_ex.io.in.writereg   := if_id.io.data.instruction(11,7)
  id_ex.io.in.rs1        := rs1
  id_ex.io.in.rs2        := rs2
  id_ex.io.in.funct7     := if_id.io.data.instruction(31,25)
  id_ex.io.in.funct3     := if_id.io.data.instruction(14,12)
  id_ex.io.in.imm        := immGen.io.sextImm
  id_ex.io.in.readdata2  := registers.io.readdata2
  id_ex.io.in.readdata1  := registers.io.readdata1
  id_ex.io.in.pc         := if_id.io.data.pc
  id_ex.io.in.pcplusfour := if_id.io.data.pcplusfour

  // Hold values in stage when stalling
  id_ex.io.valid      := !hazard.io.id_ex_stall
  id_ex_ctrl.io.valid := !hazard.io.id_ex_stall

  // Set the execution control signals
  id_ex_ctrl.io.in.ex_ctrl.aluop        := control.io.aluop
  id_ex_ctrl.io.in.ex_ctrl.itype        := control.io.itype
  id_ex_ctrl.io.in.ex_ctrl.alusrc       := control.io.alusrc
  id_ex_ctrl.io.in.ex_ctrl.resultselect := control.io.resultselect
  id_ex_ctrl.io.in.ex_ctrl.pcadd        := control.io.pcadd
  id_ex_ctrl.io.in.ex_ctrl.branch       := control.io.branch
  id_ex_ctrl.io.in.ex_ctrl.jump         := control.io.jump
  id_ex_ctrl.io.in.ex_ctrl.pcfromalu    := control.io.pcfromalu

  // Set the memory control signals
  id_ex_ctrl.io.in.mem_ctrl.memread  := control.io.memread
  id_ex_ctrl.io.in.mem_ctrl.memwrite := control.io.memwrite
  id_ex_ctrl.io.in.mem_ctrl.maskmode := if_id.io.data.instruction(13,12)
  id_ex_ctrl.io.in.mem_ctrl.sext     := ~if_id.io.data.instruction(14)
  id_ex_ctrl.io.in.mem_ctrl.taken    := false.B

  // Set the writeback control signals
  id_ex_ctrl.io.in.wb_ctrl.regwrite  := control.io.regwrite
  id_ex_ctrl.io.in.wb_ctrl.toreg     := control.io.toreg

  // Flush the IDEX control signals whenever the hazard detector signals an IDEX
  // bubble
  id_ex_ctrl.io.flush := hazard.io.id_ex_flush
  id_ex.io.flush := hazard.io.id_ex_flush

  /////////////////////////////////////////////////////////////////////////////
  // EX STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the inputs to the hazard detection unit from this stage
  hazard.io.idex_memread := id_ex_ctrl.io.data.mem_ctrl.memread
  hazard.io.idex_rd      := id_ex.io.data.writereg

  // Set the input to the forwarding unit from this stage
  forwarding.io.rs1 := id_ex.io.data.rs1
  forwarding.io.rs2 := id_ex.io.data.rs2

  // Connect the ALU control wires (line 45 of single-cycle/cpu.scala)
  aluControl.io.aluop     := id_ex_ctrl.io.data.ex_ctrl.aluop
  aluControl.io.itype     := id_ex_ctrl.io.data.ex_ctrl.itype
  aluControl.io.funct7    := id_ex.io.data.funct7
  aluControl.io.funct3    := id_ex.io.data.funct3

  // Insert the forward inputx mux here
  val forward_inputx = Wire(UInt(32.W))
  forward_inputx  := MuxCase(0.U, Array(
                           (forwarding.io.forwardA === 0.U) -> id_ex.io.data.readdata1,
                           (forwarding.io.forwardA === 1.U) -> ex_mem.io.data.result,
                           (forwarding.io.forwardA === 2.U) -> write_data))


  val alu_inputx = Wire(UInt(32.W))
  // Insert the ALU inputx mux here (line 59 of single-cycle/cpu.scala)
  alu_inputx := Mux(id_ex_ctrl.io.data.ex_ctrl.pcadd, id_ex.io.data.pc, forward_inputx)

  alu.io.inputx := alu_inputx

  // Insert forward inputy mux here
  val forward_inputy = MuxCase(0.U, Array(
                           (forwarding.io.forwardB === 0.U) -> id_ex.io.data.readdata2,
                           (forwarding.io.forwardB === 1.U) -> ex_mem.io.data.result,
                           (forwarding.io.forwardB === 2.U) -> write_data))

  val alu_inputy = Wire(UInt(32.W))
  alu_inputy := forward_inputy

  // Input y mux (line 66 of single-cycle/cpu.scala)
  alu.io.inputy := Mux(id_ex_ctrl.io.data.ex_ctrl.alusrc, id_ex.io.data.imm, alu_inputy)

  // Set the ALU operation
  alu.io.operation := aluControl.io.operation

  // Connect the branchAdd unit
  branchAdd.io.inputx := id_ex.io.data.pc
  branchAdd.io.inputy := id_ex.io.data.imm

  // Hold values in stage when stalling
  ex_mem.io.valid      := !hazard.io.ex_mem_stall
  ex_mem_ctrl.io.valid := !hazard.io.ex_mem_stall

  // Set the EX/MEM register values
  ex_mem.io.in.readdata2  := alu_inputy
  ex_mem.io.in.writereg   := id_ex.io.data.writereg

  // Pass control signals
  ex_mem_ctrl.io.in.mem_ctrl := id_ex_ctrl.io.data.mem_ctrl
  ex_mem_ctrl.io.in.wb_ctrl  := id_ex_ctrl.io.data.wb_ctrl

  // Calculate whether which PC we should use and set the taken flag (line 92 in single-cycle/cpu.scala)
  ex_mem.io.in.nextpc := Mux(id_ex_ctrl.io.data.ex_ctrl.pcfromalu, alu.io.result, branchAdd.io.result)

  // Determine which result to use
  ex_mem.io.in.result  := MuxCase(0.U, Array(
                           (id_ex_ctrl.io.data.ex_ctrl.resultselect === 0.U) -> alu.io.result,
                           (id_ex_ctrl.io.data.ex_ctrl.resultselect === 1.U) -> id_ex.io.data.imm,
                           (id_ex_ctrl.io.data.ex_ctrl.resultselect === 2.U) -> id_ex.io.data.pcplusfour))

  when (id_ex_ctrl.io.data.ex_ctrl.jump || (id_ex_ctrl.io.data.ex_ctrl.branch && alu.io.result(0))) {
    ex_mem_ctrl.io.in.mem_ctrl.taken  := true.B
  } .otherwise {
    ex_mem_ctrl.io.in.mem_ctrl.taken  := false.B
  }

  // Flush the EXMEM control signals if the hazard s a bubble
  ex_mem_ctrl.io.flush := hazard.io.ex_mem_flush
  ex_mem.io.flush := hazard.io.ex_mem_flush

  /////////////////////////////////////////////////////////////////////////////
  // MEM STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set data.mem_ctrlemory IO (line 71 of single-cycle/cpu.scala)
  io.dmem.address   := ex_mem.io.data.result
  io.dmem.writedata := ex_mem.io.data.readdata2
  io.dmem.memread   := ex_mem_ctrl.io.data.mem_ctrl.memread
  io.dmem.memwrite  := ex_mem_ctrl.io.data.mem_ctrl.memwrite
  io.dmem.maskmode  := ex_mem_ctrl.io.data.mem_ctrl.maskmode
  io.dmem.sext      := ex_mem_ctrl.io.data.mem_ctrl.sext

  // Set dmem request as valid when a write or read is being requested
  memInst := (io.dmem.memread || io.dmem.memwrite)
  io.dmem.valid := memInst

  // Send next_pc back to the fetch stage
  next_pc := ex_mem.io.data.nextpc

  // Send input signals to the hazard detection unit
  hazard.io.exmem_taken := ex_mem_ctrl.io.data.mem_ctrl.taken

  // Send input signals to the forwarding unit
  forwarding.io.exmemrd := ex_mem.io.data.writereg
  forwarding.io.exmemrw := ex_mem_ctrl.io.data.wb_ctrl.regwrite

  // Hold values in stage when stalling
  mem_wb.io.valid      := !hazard.io.mem_wb_stall
  mem_wb_ctrl.io.valid := !hazard.io.mem_wb_stall

  // Check that Data Memory is not busy
  hazard.io.dmem_good := !memInst || io.dmem.good

  // Wire the MEM/WB register
  mem_wb.io.in.writereg   := ex_mem.io.data.writereg
  mem_wb.io.in.result     := ex_mem.io.data.result
  mem_wb.io.in.readdata   := io.dmem.readdata

  // Wire the MEM/WB control signals
  mem_wb_ctrl.io.in.wb_ctrl  := ex_mem_ctrl.io.data.wb_ctrl

  // No need to flush the data of this register
  mem_wb.io.flush            := hazard.io.mem_wb_flush
  mem_wb_ctrl.io.flush       := hazard.io.mem_wb_flush

  /////////////////////////////////////////////////////////////////////////////
  // WB STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the writeback data.mem_ctrlux (line 78 single-cycle/cpu.scala)
  write_data := MuxCase(0.U, Array(
                           (mem_wb_ctrl.io.data.wb_ctrl.toreg === 0.U) -> mem_wb.io.data.result,
                           (mem_wb_ctrl.io.data.wb_ctrl.toreg === 1.U) -> mem_wb.io.data.readdata))


  // Write the data to the register file
  registers.io.writedata := write_data
  registers.io.writereg  := mem_wb.io.data.writereg
  registers.io.wen       := mem_wb_ctrl.io.data.wb_ctrl.regwrite && (mem_wb.io.data.writereg =/= 0.U)

  // Set the input signals for the forwarding unit
  forwarding.io.memwbrd := mem_wb.io.data.writereg
  forwarding.io.memwbrw := mem_wb_ctrl.io.data.wb_ctrl.regwrite
}

/*
 * Object to make it easier to print information about the CPU
 */
object PipelinedNonCombinCPUInfo {
  def getModules(): List[String] = {
    List(
      "imem",
      "dmem",
      "control",
      "branchCtrl",
      "registers",
      "aluControl",
      "alu",
      "immGen",
      "pcPlusFour",
      "branchAdd",
      "forwarding",
      "hazard",
    )
  }
  def getPipelineRegs(): List[String] = {
    List(
      "if_id",
      "id_ex",
      "id_ex_ctrl",
      "ex_mem",
      "ex_mem_ctrl",
      "mem_wb",
      "mem_wb_ctrl"
    )
  }
}
