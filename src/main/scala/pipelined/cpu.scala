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
  // Everything in the register between IF and ID stages
  class IFIDBundle extends Bundle {
    val instruction = UInt(32.W)
    val pc          = UInt(64.W)
  }

  // Control signals used in EX stage
  class EXControl extends Bundle {
    val itype         = Bool()
    val aluop         = Bool()
    val src1          = Bool()
    val src2          = UInt(2.W)
    val branch        = Bool()
    val jumptype      = UInt(2.W)
    val resultselect  = Bool()
    val wordinst      = Bool()
  }

  // Control signals used in MEM stage
  class MControl extends Bundle {
    val memop = UInt(2.W)
  }

  // Control signals used in WB stage
  class WBControl extends Bundle {
    val toreg    = Bool()
    val regwrite = Bool()
  }

  // Data of the the register between ID and EX stages
  class IDEXBundle extends Bundle {
    val pc          = UInt(64.W) // NextPC's pc
    val instruction = UInt(64.W) // NextPC's funct3
    val sextImm     = UInt(64.W) // NextPC's imm
    val readdata1   = UInt(64.W) // NextPC's and ALU's inputx
    val readdata2   = UInt(64.W) // NextPC's and ALU's inputy
  }

  // Control block of the IDEX register
  class IDEXControl extends Bundle {
    val ex_ctrl  = new EXControl
    val mem_ctrl = new MControl
    val wb_ctrl  = new WBControl
  }

  // Everything in the register between EX and MEM stages
  class EXMEMBundle extends Bundle {
    val ex_result     = UInt(64.W) // either ALU's result or sextImm, for memAddress and for reg writeback
    val mem_writedata = UInt(64.W) // from reg2, for writedata of DMem
    val nextpc        = UInt(64.W)
    val taken         = Bool()
    val instruction   = UInt(64.W)
  }

  // Control block of the EXMEM register
  class EXMEMControl extends Bundle {
    val mem_ctrl  = new MControl
    val wb_ctrl   = new WBControl
  }

  // Everything in the register between MEM and WB stages
  class MEMWBBundle extends Bundle {
    val readdata    = UInt(64.W) // needed if it's a load inst
    val ex_result   = UInt(64.W)
    val instruction = UInt(64.W) // to figure out destination reg
  }

  // Control block of the MEMWB register
  class MEMWBControl extends Bundle {
    val wb_ctrl = new WBControl
  }

  // All of the structures required
  val pc              = RegInit(0.U(64.W))
  val control         = Module(new Control())
  val registers       = Module(new RegisterFile())
  val aluControl      = Module(new ALUControl())
  val alu             = Module(new ALU())
  val immGen          = Module(new ImmediateGenerator())
  val nextPCmod       = Module(new NextPC())
  val pcPlusFour      = Module(new Adder())
  val forwarding      = Module(new ForwardingUnit())  //pipelined only
  val hazard          = Module(new HazardUnit())      //pipelined only
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
  val wb_writedata = Wire(UInt(64.W)) // used for forwarding the writeback data from writeback stage to execute stage.

  // From memory back to fetch. Since we don't decide whether to take a branch or not until the memory stage.
  val next_pc = Wire(UInt(64.W))

  /////////////////////////////////////////////////////////////////////////////
  // FETCH STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Only update the pc if pcstall is false
  when (hazard.io.pcfromtaken) {
    pc := next_pc
  } .elsewhen (hazard.io.pcstall) {
    // not updating pc
    pc := pc
  } .otherwise {
    pc := pcPlusFour.io.result
  }

  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  // Send the PC to the instruction memory port to get the instruction
  io.imem.address := pc
  io.imem.valid   := true.B

  // Fill the IF/ID register
  if_id.io.in.instruction := io.imem.instruction
  if_id.io.in.pc          := pc

  // Update during Part III when implementing branches/jump
  if_id.io.valid := ~hazard.io.if_id_stall
  if_id.io.flush := hazard.io.if_id_flush


  /////////////////////////////////////////////////////////////////////////////
  // ID STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Send opcode to control (line 33 in single-cycle/cpu.scala)
  control.io.opcode := if_id.io.data.instruction(6, 0)

  // Grab rs1 and rs2 from the instruction (line 35 in single-cycle/cpu.scala)
  val id_rs1 = if_id.io.data.instruction(19, 15)
  val id_rs2 = if_id.io.data.instruction(24, 20)

  // Send input from this stage to hazard detection unit (Part III and/or Part IV)
  hazard.io.rs1 := id_rs1
  hazard.io.rs2 := id_rs2

  // Send register numbers to the register file
  registers.io.readreg1 := id_rs1
  registers.io.readreg2 := id_rs2

  // Send the instruction to the immediate generator (line 45 in single-cycle/cpu.scala)
  immGen.io.instruction := if_id.io.data.instruction

  // Control block of the IDEX register
  //  - Fill the id_ex register
  id_ex.io.in.pc          := if_id.io.data.pc
  id_ex.io.in.instruction := if_id.io.data.instruction
  id_ex.io.in.sextImm     := immGen.io.sextImm
  id_ex.io.in.readdata1   := registers.io.readdata1
  id_ex.io.in.readdata2   := registers.io.readdata2
  //  - Set the execution control signals
  id_ex_ctrl.io.in.ex_ctrl.itype        := control.io.itype
  id_ex_ctrl.io.in.ex_ctrl.aluop        := control.io.aluop
  id_ex_ctrl.io.in.ex_ctrl.src1         := control.io.src1
  id_ex_ctrl.io.in.ex_ctrl.src2         := control.io.src2
  id_ex_ctrl.io.in.ex_ctrl.branch       := control.io.branch
  id_ex_ctrl.io.in.ex_ctrl.jumptype     := control.io.jumptype
  id_ex_ctrl.io.in.ex_ctrl.resultselect := control.io.resultselect
  id_ex_ctrl.io.in.ex_ctrl.wordinst     := control.io.wordinst
  //  - Set the memory control signals
  id_ex_ctrl.io.in.mem_ctrl.memop := control.io.memop
  //  - Set the writeback control signals
  id_ex_ctrl.io.in.wb_ctrl.toreg    := control.io.toreg
  id_ex_ctrl.io.in.wb_ctrl.regwrite := control.io.regwrite

  // Set the control signals on the id_ex pipeline register (Part III and/or Part IV)
  id_ex.io.valid := true.B
  id_ex.io.flush := hazard.io.id_ex_flush

  id_ex_ctrl.io.valid := true.B
  id_ex_ctrl.io.flush := hazard.io.id_ex_flush


  /////////////////////////////////////////////////////////////////////////////
  // EX STAGE
  /////////////////////////////////////////////////////////////////////////////

  val ex_funct3 = id_ex.io.data.instruction(14, 12)
  val ex_funct7 = id_ex.io.data.instruction(31, 25)
  val ex_rs1 = id_ex.io.data.instruction(19, 15)
  val ex_rs2 = id_ex.io.data.instruction(24, 20)
  val ex_rd  = id_ex.io.data.instruction(11, 7)

  // Set the inputs to the hazard detection unit from this stage (SKIP FOR PART I)
  hazard.io.idex_memread := id_ex_ctrl.io.data.mem_ctrl.memop === 2.U
  hazard.io.idex_rd      := ex_rd

  // Set the input to the forwarding unit from this stage (SKIP FOR PART I)
  forwarding.io.rs1 := ex_rs1
  forwarding.io.rs2 := ex_rs2
  forwarding.io.exmemrd := ex_mem.io.data.instruction(11, 7)
  forwarding.io.exmemrw := ex_mem_ctrl.io.data.wb_ctrl.regwrite
  forwarding.io.memwbrd := mem_wb.io.data.instruction(11, 7)
  forwarding.io.memwbrw := mem_wb_ctrl.io.data.wb_ctrl.regwrite

  // Connect the ALU control wires (line 55 of single-cycle/cpu.scala)
  aluControl.io.itype    := id_ex_ctrl.io.data.ex_ctrl.itype
  aluControl.io.aluop    := id_ex_ctrl.io.data.ex_ctrl.aluop
  aluControl.io.wordinst := id_ex_ctrl.io.data.ex_ctrl.wordinst
  aluControl.io.funct3   := ex_funct3
  aluControl.io.funct7   := ex_funct7
  // Connect the NextPC control wires (line 47 of single-cycle/cpu.scala)
  nextPCmod.io.branch   := id_ex_ctrl.io.data.ex_ctrl.branch
  nextPCmod.io.jumptype := id_ex_ctrl.io.data.ex_ctrl.jumptype
  
  // Insert the forward inputx mux here (SKIP FOR PART I)
  val forwarded_inputx = MuxCase(0.U, Array((forwarding.io.forwardA === 0.U) -> id_ex.io.data.readdata1,
                                            (forwarding.io.forwardA === 1.U) -> ex_mem.io.data.ex_result,
                                            (forwarding.io.forwardA === 2.U) -> wb_writedata))
  // Insert the forward inputy mux here (SKIP FOR PART I)
  val forwarded_inputy = MuxCase(0.U, Array((forwarding.io.forwardB === 0.U) -> id_ex.io.data.readdata2,
                                            (forwarding.io.forwardB === 1.U) -> ex_mem.io.data.ex_result,
                                            (forwarding.io.forwardB === 2.U) -> wb_writedata))
  
  // Input x mux (line 62 of single-cycle/cpu.scala)
  val ex_inputx = Mux(id_ex_ctrl.io.data.ex_ctrl.src1, id_ex.io.data.pc, forwarded_inputx)
  // Input y mux (line 63 of single-cycle/cpu.scala)
  val ex_inputy = MuxCase(0.U, Array((id_ex_ctrl.io.data.ex_ctrl.src2 === 0.U) -> forwarded_inputy,
                                     (id_ex_ctrl.io.data.ex_ctrl.src2 === 1.U) -> id_ex.io.data.sextImm,
                                     (id_ex_ctrl.io.data.ex_ctrl.src2 === 2.U) -> 4.U))

  // Set the ALU operation  (line 61 of single-cycle/cpu.scala)
  alu.io.operation  := aluControl.io.operation
  // Connect the ALU data wires
  alu.io.inputx := ex_inputx
  alu.io.inputy := ex_inputy
  // Connect the NextPC data wires (line 49 of single-cycle/cpu.scala)
  nextPCmod.io.inputx := forwarded_inputx
  nextPCmod.io.inputy := forwarded_inputy
  nextPCmod.io.pc     := id_ex.io.data.pc
  nextPCmod.io.imm    := id_ex.io.data.sextImm
  nextPCmod.io.funct3 := ex_funct3

  // Set the EX/MEM register values
  ex_mem.io.in.instruction   := id_ex.io.data.instruction
  ex_mem.io.in.mem_writedata := forwarded_inputy

  // Determine which result to use (the resultselect mux from line 38 of single-cycle/cpu.scala)
  ex_mem_ctrl.io.in.mem_ctrl.memop   := id_ex_ctrl.io.data.mem_ctrl.memop
  ex_mem_ctrl.io.in.wb_ctrl.toreg    := id_ex_ctrl.io.data.wb_ctrl.toreg
  ex_mem_ctrl.io.in.wb_ctrl.regwrite := id_ex_ctrl.io.data.wb_ctrl.regwrite

  ex_mem.io.in.nextpc := nextPCmod.io.nextpc
  ex_mem.io.in.taken  := nextPCmod.io.taken

  // Determine which result to use (line 38 of single-cycle/cpu.scala)
  ex_mem.io.in.ex_result := Mux(id_ex_ctrl.io.data.ex_ctrl.resultselect, id_ex.io.data.sextImm, alu.io.result)

  // Set the control signals on the ex_mem pipeline register (Part III and/or Part IV)
  ex_mem.io.valid      := true.B
  ex_mem.io.flush      := hazard.io.ex_mem_flush

  ex_mem_ctrl.io.valid := true.B
  ex_mem_ctrl.io.flush := hazard.io.ex_mem_flush

  /////////////////////////////////////////////////////////////////////////////
  // MEM STAGE
  /////////////////////////////////////////////////////////////////////////////

  val mem_funct3 = ex_mem.io.data.instruction(14, 12)

  // Set data memory IO (line 67 of single-cycle/cpu.scala)
  io.dmem.address   := ex_mem.io.data.ex_result // this is fine because ex_result is alu's result when inst is LD/ST
  io.dmem.memread   := ~ex_mem_ctrl.io.data.mem_ctrl.memop(0)
  io.dmem.memwrite  := ex_mem_ctrl.io.data.mem_ctrl.memop(0)
  io.dmem.valid     := ex_mem_ctrl.io.data.mem_ctrl.memop(1)
  io.dmem.maskmode  := mem_funct3(1, 0)
  io.dmem.sext      := ~mem_funct3(2)
  io.dmem.writedata := ex_mem.io.data.mem_writedata

  // Send next_pc back to the fetch stage
  next_pc := ex_mem.io.data.nextpc

  // Send input signals to the hazard detection unit (SKIP FOR PART I)
  hazard.io.exmem_taken := ex_mem.io.data.taken
  // Send input signals to the forwarding unit (SKIP FOR PART I)

  // Wire the MEM/WB register
  mem_wb.io.in.readdata    := io.dmem.readdata
  mem_wb.io.in.ex_result   := ex_mem.io.data.ex_result
  mem_wb.io.in.instruction := ex_mem.io.data.instruction

  mem_wb_ctrl.io.in.wb_ctrl.toreg := ex_mem_ctrl.io.data.wb_ctrl.toreg
  mem_wb_ctrl.io.in.wb_ctrl.regwrite := ex_mem_ctrl.io.data.wb_ctrl.regwrite

  // Set the control signals on the mem_wb pipeline register
  mem_wb.io.valid      := true.B
  mem_wb.io.flush      := false.B

  mem_wb_ctrl.io.valid := true.B
  mem_wb_ctrl.io.flush := false.B


  /////////////////////////////////////////////////////////////////////////////
  // WB STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the register to be written to
  val wb_rd = mem_wb.io.data.instruction(11, 7)
  registers.io.writereg := wb_rd

  // Set the writeback data mux (line 39 single-cycle/cpu.scala)
  registers.io.wen := (wb_rd =/= 0.U) & (mem_wb_ctrl.io.data.wb_ctrl.regwrite === true.B)

  // Write the data to the register file
  wb_writedata           := Mux(mem_wb_ctrl.io.data.wb_ctrl.toreg, mem_wb.io.data.readdata, mem_wb.io.data.ex_result)
  registers.io.writedata := wb_writedata
  // Set the input signals for the forwarding unit (SKIP FOR PART I)

}

/*
 * Object to make it easier to print information about the CPU
 */
object PipelinedCPUInfo {
  def getModules(): List[String] = {
    List(
      "imem",
      "dmem",
      "control",
      //"branchCtrl",
      "registers",
      "aluControl",
      "alu",
      "immGen",
      "pcPlusFour",
      //"branchAdd",
      "nextPCmod",
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
