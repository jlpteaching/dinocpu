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
class PipelinedCPU(implicit val conf: CPUConfig) extends BaseCPU {
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
    val jump      = UInt(2.W)
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
    val toreg    = UInt(2.W)
    val regwrite = Bool()
  }

  // Data of the the register between ID and EX stages
  class IDEXBundle extends Bundle {
    val writereg  = UInt(5.W)
    val funct7    = UInt(7.W)
    val funct3    = UInt(3.W)
    val imm       = UInt(32.W)
    val readdata2 = UInt(32.W)
    val readdata1 = UInt(32.W)
    val pc        = UInt(32.W)
    val pcplusfour= UInt(32.W)
    val rs1       = UInt(5.W)    //pipelined only
    val rs2       = UInt(5.W)    //pipelined only
  }

  // Control block of the IDEX register
  class IDEXControl extends Bundle {
    val ex_ctrl = new EXControl
    val mem_ctrl  = new MControl
    val wb_ctrl = new WBControl
  }

  // Everything in the register between ID and EX stages
  class EXMEMBundle extends Bundle {
    val writereg  = UInt(5.W)
    val readdata2 = UInt(32.W)
    val aluresult = UInt(32.W)
    val nextpc    = UInt(32.W)
    val pcplusfour= UInt(32.W)
  }

  // Control block of the EXMEM register
  class EXMEMControl extends Bundle {
    val mem_ctrl  = new MControl
    val wb_ctrl = new WBControl
  }

  // Everything in the register between ID and EX stages
  class MEMWBBundle extends Bundle {
    val writereg  = UInt(5.W)
    val aluresult = UInt(32.W)
    val readdata  = UInt(32.W)
    val pcplusfour= UInt(32.W)
  }

  class MEMWBControl extends Bundle {
    val wb_ctrl = new WBControl
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
  val forwarding = Module(new ForwardingUnit())  //pipelined only
  val hazard     = Module(new HazardUnit())      //pipelined only
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

  if (conf.debug) { printf("Cycle=%d ", cycleCount) }

  // Forward declaration of wires that connect different stages

  // From memory back to fetch. Since we don't decide whether to take a branch or not until the memory stage.
  val next_pc      = Wire(UInt())

  // For wb back to other stages
  val write_data = Wire(UInt())

  /////////////////////////////////////////////////////////////////////////////
  // FETCH STAGE
  /////////////////////////////////////////////////////////////////////////////


  // Note: This comes from the memory stage!
  // Only update the pc if the pcwrite flag is enabled
  if (conf.debug) { printf(p"PC: $pc\n") }
  pc := MuxCase(0.U, Array(
                (hazard.io.pcwrite === 0.U) -> pcPlusFour.io.result,
                (hazard.io.pcwrite === 1.U) -> next_pc,
                (hazard.io.pcwrite === 2.U) -> pc))

  // Send the PC to the instruction memory port to get the instruction
  io.imem.address := pc

  // Send a valid instruction request to instruction memory only if IFID isn't
  // being bubbled;
  io.imem.valid := !hazard.io.ifid_bubble

  // Get the PC + 4
  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  // Data supplied to if_id register is bubbled when the hazard detector
  // indicates a IFID bubble
  if_id.io.valid := !hazard.io.ifid_bubble

  // Connect outputs of IF stage into the stage register's in port
  if_id.io.in.instruction := io.imem.instruction
  if_id.io.in.pc          := pc
  if_id.io.in.pcplusfour  := pcPlusFour.io.result

  if_id.io.flush  := hazard.io.ifid_flush

  if (conf.debug) { printf(p"IF/ID: $if_id\n") }

  /////////////////////////////////////////////////////////////////////////////
  // ID STAGE
  /////////////////////////////////////////////////////////////////////////////

  val rs1 = if_id.io.data.instruction(19,15)
  val rs2 = if_id.io.data.instruction(24,20)

  // Send input from this stage to hazard detection unit
  hazard.io.rs1 := rs1
  hazard.io.rs2 := rs2

  // Send opcode to control
  control.io.opcode := if_id.io.data.instruction(6,0)

  // Send register numbers to the register file
  registers.io.readreg1 := rs1
  registers.io.readreg2 := rs2

  // Send the instruction to the immediate generator
  immGen.io.instruction := if_id.io.data.instruction

  // Data supplied to id_ex register is always valid every cycle
  id_ex.io.valid := true.B
  // Don't need to flush the data in this register
  id_ex.io.flush := false.B
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

  // Data supplied to id_ex_ctrl register is always valid every cycle
  id_ex_ctrl.io.valid := true.B
  // Set the execution control signals
  id_ex_ctrl.io.in.ex_ctrl.add       := control.io.add
  id_ex_ctrl.io.in.ex_ctrl.immediate := control.io.immediate
  id_ex_ctrl.io.in.ex_ctrl.alusrc1   := control.io.alusrc1
  id_ex_ctrl.io.in.ex_ctrl.branch    := control.io.branch
  id_ex_ctrl.io.in.ex_ctrl.jump      := control.io.jump

  // Set the memory control signals
  id_ex_ctrl.io.in.mem_ctrl.memread  := control.io.memread
  id_ex_ctrl.io.in.mem_ctrl.memwrite := control.io.memwrite
  id_ex_ctrl.io.in.mem_ctrl.maskmode := if_id.io.data.instruction(13,12)
  id_ex_ctrl.io.in.mem_ctrl.sext     := ~if_id.io.data.instruction(14)
  id_ex_ctrl.io.in.mem_ctrl.taken    := false.B

  // Set the writeback control signals
  id_ex_ctrl.io.in.wb_ctrl.toreg    := control.io.toreg
  id_ex_ctrl.io.in.wb_ctrl.regwrite := control.io.regwrite

  // Flush the IDEX control signals whenever the hazard detector signals an IDEX
  // bubble
  id_ex_ctrl.io.flush := hazard.io.idex_bubble

  if (conf.debug) { printf("DASM(%x)\n", if_id.io.data.instruction) }
  if (conf.debug) { printf(p"ID/EX: $id_ex\n") }

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
  aluControl.io.add       := id_ex_ctrl.io.data.ex_ctrl.add
  aluControl.io.immediate := id_ex_ctrl.io.data.ex_ctrl.immediate
  aluControl.io.funct7    := id_ex.io.data.funct7
  aluControl.io.funct3    := id_ex.io.data.funct3

  // Insert the forward inputx mux here
  val forward_inputx = Wire(UInt(32.W))
  forward_inputx  := MuxCase(0.U, Array(
                           (forwarding.io.forwardA === 0.U) -> id_ex.io.data.readdata1,
                           (forwarding.io.forwardA === 1.U) -> ex_mem.io.data.aluresult,
                           (forwarding.io.forwardA === 2.U) -> write_data))


  val alu_inputx = Wire(UInt(32.W))
  // Insert the ALU inpux mux here (line 59 of single-cycle/cpu.scala)
  alu_inputx := MuxCase(0.U, Array(
    (id_ex_ctrl.io.data.ex_ctrl.alusrc1 === 0.U) -> forward_inputx,
    (id_ex_ctrl.io.data.ex_ctrl.alusrc1 === 1.U) -> 0.U,
    (id_ex_ctrl.io.data.ex_ctrl.alusrc1 === 2.U) -> id_ex.io.data.pc
  ))
  alu.io.inputx := alu_inputx

  // Insert forward inputy mux here
  val forward_inputy = MuxCase(0.U, Array(
                           (forwarding.io.forwardB === 0.U) -> id_ex.io.data.readdata2,
                           (forwarding.io.forwardB === 1.U) -> ex_mem.io.data.aluresult,
                           (forwarding.io.forwardB === 2.U) -> write_data))

  val alu_inputy = Wire(UInt(32.W))
  alu_inputy := forward_inputy

  // Input y mux (line 66 of single-cycle/cpu.scala)
  alu.io.inputy := Mux(id_ex_ctrl.io.data.ex_ctrl.immediate, id_ex.io.data.imm, alu_inputy)

  // Connect the branch control wire (line 54 of single-cycle/cpu.scala)
  branchCtrl.io.branch := id_ex_ctrl.io.data.ex_ctrl.branch
  branchCtrl.io.funct3 := id_ex.io.data.funct3
  branchCtrl.io.inputx := forward_inputx
  branchCtrl.io.inputy := forward_inputy

  // Set the ALU operation
  alu.io.operation := aluControl.io.operation

  // Connect the branchAdd unit
  branchAdd.io.inputx := id_ex.io.data.pc
  branchAdd.io.inputy := id_ex.io.data.imm

  // Data supplied to ex_mem register is always valid every cycle
  ex_mem.io.valid := true.B
  // Don't need to flush the data in this register
  ex_mem.io.flush := false.B
  // Set the EX/MEM register values
  ex_mem.io.in.readdata2  := alu_inputy
  ex_mem.io.in.aluresult  := alu.io.result
  ex_mem.io.in.writereg   := id_ex.io.data.writereg
  ex_mem.io.in.pcplusfour := id_ex.io.data.pcplusfour

  // Data supplied to ex_mem_ctrl register is always valid every cycle
  ex_mem_ctrl.io.valid       := true.B
  ex_mem_ctrl.io.in.mem_ctrl := id_ex_ctrl.io.data.mem_ctrl
  ex_mem_ctrl.io.in.wb_ctrl  := id_ex_ctrl.io.data.wb_ctrl

  // Calculate whether which PC we should use and set the taken flag (line 92 in single-cycle/cpu.scala)
  when (branchCtrl.io.taken || id_ex_ctrl.io.data.ex_ctrl.jump === 2.U) {
    ex_mem.io.in.nextpc := branchAdd.io.result
    ex_mem_ctrl.io.in.mem_ctrl.taken  := true.B
  } .elsewhen (id_ex_ctrl.io.data.ex_ctrl.jump === 3.U) {
    ex_mem.io.in.nextpc := alu.io.result & Cat(Fill(31, 1.U), 0.U)
    ex_mem_ctrl.io.in.mem_ctrl.taken  := true.B
  } .otherwise {
    ex_mem.io.in.nextpc := 0.U // No need to set the PC if not a branch
    ex_mem_ctrl.io.in.mem_ctrl.taken  := false.B
  }

  // Flush the EXMEM control signals if the hazard s a bubble
  ex_mem_ctrl.io.flush := hazard.io.exmem_bubble

  if (conf.debug) { printf(p"EX/MEM: $ex_mem\n") }

  /////////////////////////////////////////////////////////////////////////////
  // MEM STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set data.mem_ctrlemory IO (line 71 of single-cycle/cpu.scala)
  io.dmem.address   := ex_mem.io.data.aluresult
  io.dmem.writedata := ex_mem.io.data.readdata2
  io.dmem.memread   := ex_mem_ctrl.io.data.mem_ctrl.memread
  io.dmem.memwrite  := ex_mem_ctrl.io.data.mem_ctrl.memwrite
  io.dmem.maskmode  := ex_mem_ctrl.io.data.mem_ctrl.maskmode
  io.dmem.sext      := ex_mem_ctrl.io.data.mem_ctrl.sext

  // Set dmem request as valid when a write or read is being requested
  io.dmem.valid := (io.dmem.memread || io.dmem.memwrite)

  // Send next_pc back to the fetch stage
  next_pc := ex_mem.io.data.nextpc

  // Send input signals to the hazard detection unit
  hazard.io.exmem_taken := ex_mem_ctrl.io.data.mem_ctrl.taken

  // Send input signals to the forwarding unit
  forwarding.io.exmemrd := ex_mem.io.data.writereg
  forwarding.io.exmemrw := ex_mem_ctrl.io.data.wb_ctrl.regwrite

  // Data supplied to mem_wb register is always valid every cycle
  mem_wb.io.valid := true.B
  // No need to flush the data of this register
  mem_wb.io.flush := false.B
  // Wire the MEM/WB register
  mem_wb.io.in.writereg   := ex_mem.io.data.writereg
  mem_wb.io.in.aluresult  := ex_mem.io.data.aluresult
  mem_wb.io.in.pcplusfour := ex_mem.io.data.pcplusfour
  mem_wb.io.in.readdata   := io.dmem.readdata

  // Data supplied to mem_wb_ctrl register is always valid every cycle
  mem_wb_ctrl.io.valid       := true.B
  // No need to flush the data of this register
  mem_wb_ctrl.io.flush       := false.B
  mem_wb_ctrl.io.in.wb_ctrl  := ex_mem_ctrl.io.data.wb_ctrl

  if (conf.debug) { printf(p"MEM/WB: $mem_wb\n") }

  /////////////////////////////////////////////////////////////////////////////
  // WB STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the writeback data.mem_ctrlux (line 78 single-cycle/cpu.scala)
  write_data := MuxCase(mem_wb.io.data.aluresult, Array(
                       (mem_wb_ctrl.io.data.wb_ctrl.toreg === 0.U) -> mem_wb.io.data.aluresult,
                       (mem_wb_ctrl.io.data.wb_ctrl.toreg === 1.U) -> mem_wb.io.data.readdata,
                       (mem_wb_ctrl.io.data.wb_ctrl.toreg === 2.U) -> mem_wb.io.data.pcplusfour))

  // Write the data to the register file
  registers.io.writedata := write_data
  registers.io.writereg  := mem_wb.io.data.writereg
  registers.io.wen       := mem_wb_ctrl.io.data.wb_ctrl.regwrite && (mem_wb.io.data.writereg =/= 0.U)

  // Set the input signals for the forwarding unit
  forwarding.io.memwbrd := mem_wb.io.data.writereg
  forwarding.io.memwbrw := mem_wb_ctrl.io.data.wb_ctrl.regwrite

  if (conf.debug) { printf("---------------------------------------------\n") }
}
