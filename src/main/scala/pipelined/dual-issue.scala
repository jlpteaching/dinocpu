// This file is where all of the CPU components are assembled into the whole CPU

package dinocpu.pipelined

import chisel3._
import chisel3.util._
import dinocpu._
import dinocpu.components._
import dinocpu.components.dual._

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.6 of Patterson and Hennessy
 * This follows figure 4.49
 */
class PipelinedDualIssueCPU(implicit val conf: CPUConfig) extends BaseCPU {
  // Everything in the register between IF and ID stages
  class IFIDBundle extends Bundle {
    val instruction = UInt(32.W)
    val pc          = UInt(64.W)
    val isValid     = Bool()
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
    val isValid     = Bool()
  }

  // Control block of the IDEX register
  class IDEXControl extends Bundle {
    val ex_ctrl  = new EXControl
    val mem_ctrl = new MControl
    val wb_ctrl  = new WBControl
  }

  // Everything in the register between EX and MEM stages
  class EXMEMBundle extends Bundle {
    val pc            = UInt(64.W)
    val ex_result     = UInt(64.W) // either ALU's result or sextImm, for memAddress and for reg writeback
    val mem_writedata = UInt(64.W) // from reg2, for writedata of DMem
    val nextpc        = UInt(64.W)
    val taken         = Bool()
    val instruction   = UInt(64.W)
    val isValid       = Bool()
  }

  // Control block of the EXMEM register
  class EXMEMControl extends Bundle {
    val mem_ctrl  = new MControl
    val wb_ctrl   = new WBControl
  }

  // Everything in the register between MEM and WB stages
  class MEMWBBundle extends Bundle {
    val pc          = UInt(64.W)
    val readdata    = UInt(64.W) // needed if it's a load inst
    val ex_result   = UInt(64.W)
    val instruction = UInt(64.W) // to figure out destination reg
    val isValid     = Bool()
  }

  // Control block of the MEMWB register
  class MEMWBControl extends Bundle {
    val wb_ctrl = new WBControl
  }

  // All of the structures required
  // Fetch
  val pc                    = RegInit(0.U)
  val issueUnit             = Module(new DualIssueIssueUnit())
  // Decode
  val registers             = Module(new DualIssueRegisterFile())
  val pipeA_control         = Module(new Control())
  val pipeB_control         = Module(new Control())
  val pipeA_immGen          = Module(new ImmediateGenerator())
  val pipeB_immGen          = Module(new ImmediateGenerator())
  // Execute
  val pipeA_aluControl      = Module(new ALUControl())
  val pipeB_aluControl      = Module(new ALUControl())
  val pipeA_alu             = Module(new ALU())
  val pipeB_alu             = Module(new ALU())
  // Mem
  val nextPCmod             = Module(new DualIssueNextPC())

  val forwarding            = Module(new DualIssueForwardingUnit())  //pipelined only
  val hazard                = Module(new DualIssueHazardUnit())      //pipelined only
  val (cycleCount, _)       = Counter(true.B, 1 << 30)

  // The four pipeline registers
  val pipeA_if_id       = Module(new StageReg(new IFIDBundle))
  val pipeB_if_id       = Module(new StageReg(new IFIDBundle))

  val pipeA_id_ex       = Module(new StageReg(new IDEXBundle))
  val pipeB_id_ex       = Module(new StageReg(new IDEXBundle))
  val pipeA_id_ex_ctrl  = Module(new StageReg(new IDEXControl))
  val pipeB_id_ex_ctrl  = Module(new StageReg(new IDEXControl))

  val pipeA_ex_mem      = Module(new StageReg(new EXMEMBundle))
  val pipeB_ex_mem      = Module(new StageReg(new EXMEMBundle))
  val pipeA_ex_mem_ctrl = Module(new StageReg(new EXMEMControl))
  val pipeB_ex_mem_ctrl = Module(new StageReg(new EXMEMControl))

  val pipeA_mem_wb      = Module(new StageReg(new MEMWBBundle))
  val pipeB_mem_wb      = Module(new StageReg(new MEMWBBundle))
  // To make the interface of the mem_wb_ctrl register consistent with the other control
  // registers, we create an anonymous Bundle
  val pipeA_mem_wb_ctrl = Module(new StageReg(new MEMWBControl))
  val pipeB_mem_wb_ctrl = Module(new StageReg(new MEMWBControl))

  // Forward declaration of wires that connect different stages
  val pipeA_wb_writedata = Wire(UInt(64.W)) // used for forwarding the writeback data from writeback stage to execute stage.
  val pipeB_wb_writedata = Wire(UInt(64.W))

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
    pc := issueUnit.io.nextpc
  }

  // Send the PC to the instruction memory port to get the instruction
  io.imem.address := pc
  io.imem.valid   := true.B

  issueUnit.io.pc   := pc
  issueUnit.io.inst := io.imem.instruction
  issueUnit.io.ignore_data_dependencies := false.B

  // Fill the IF/ID register
  pipeA_if_id.io.in.instruction := issueUnit.io.pipeA_inst
  pipeA_if_id.io.in.pc          := pc
  pipeA_if_id.io.in.isValid     := issueUnit.io.pipeA_valid

  pipeB_if_id.io.in.instruction := issueUnit.io.pipeB_inst
  pipeB_if_id.io.in.pc          := pc + 4.U
  pipeB_if_id.io.in.isValid     := issueUnit.io.pipeB_valid

  // Update during Part III when implementing branches/jump
  pipeA_if_id.io.valid := ~hazard.io.if_id_stall
  pipeA_if_id.io.flush := hazard.io.if_id_flush
  pipeB_if_id.io.valid := ~hazard.io.if_id_stall
  pipeB_if_id.io.flush := hazard.io.if_id_flush

  /////////////////////////////////////////////////////////////////////////////
  // ID STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Send opcode to control (line 33 in single-cycle/cpu.scala)
  pipeA_control.io.opcode := pipeA_if_id.io.data.instruction(6, 0)
  pipeB_control.io.opcode := pipeB_if_id.io.data.instruction(6, 0)

  // Grab rs1 and rs2 from the instruction (line 35 in single-cycle/cpu.scala)
  val pipeA_id_rs1 = pipeA_if_id.io.data.instruction(19, 15)
  val pipeA_id_rs2 = pipeA_if_id.io.data.instruction(24, 20)
  val pipeB_id_rs1 = pipeB_if_id.io.data.instruction(19, 15)
  val pipeB_id_rs2 = pipeB_if_id.io.data.instruction(24, 20)

  // Send input from this stage to hazard detection unit (Part III and/or Part IV)
  hazard.io.id_pipeA_rs1 := pipeA_id_rs1
  hazard.io.id_pipeA_rs2 := pipeA_id_rs2
  hazard.io.id_pipeB_rs1 := pipeB_id_rs1
  hazard.io.id_pipeB_rs2 := pipeB_id_rs2

  // Send register numbers to the register file
  registers.io.pipeA_readreg1 := pipeA_id_rs1
  registers.io.pipeA_readreg2 := pipeA_id_rs2
  registers.io.pipeB_readreg1 := pipeB_id_rs1
  registers.io.pipeB_readreg2 := pipeB_id_rs2

  // Send the instruction to the immediate generator (line 45 in single-cycle/cpu.scala)
  pipeA_immGen.io.instruction := pipeA_if_id.io.data.instruction
  pipeB_immGen.io.instruction := pipeB_if_id.io.data.instruction

  // Control block of the IDEX register
  // Fill the id_ex register

  pipeA_id_ex.io.in.pc          := pipeA_if_id.io.data.pc
  pipeA_id_ex.io.in.instruction := pipeA_if_id.io.data.instruction
  pipeA_id_ex.io.in.sextImm     := pipeA_immGen.io.sextImm
  pipeA_id_ex.io.in.readdata1   := registers.io.pipeA_readdata1
  pipeA_id_ex.io.in.readdata2   := registers.io.pipeA_readdata2
  pipeA_id_ex.io.in.isValid     := pipeA_if_id.io.data.isValid

  pipeB_id_ex.io.in.pc          := pipeB_if_id.io.data.pc
  pipeB_id_ex.io.in.instruction := pipeB_if_id.io.data.instruction
  pipeB_id_ex.io.in.sextImm     := pipeB_immGen.io.sextImm
  pipeB_id_ex.io.in.readdata1   := registers.io.pipeB_readdata1
  pipeB_id_ex.io.in.readdata2   := registers.io.pipeB_readdata2
  pipeB_id_ex.io.in.isValid     := pipeB_if_id.io.data.isValid

  // Set the execution control signals

  pipeA_id_ex_ctrl.io.in.ex_ctrl.itype        := pipeA_control.io.itype
  pipeA_id_ex_ctrl.io.in.ex_ctrl.aluop        := pipeA_control.io.aluop
  pipeA_id_ex_ctrl.io.in.ex_ctrl.src1         := pipeA_control.io.src1
  pipeA_id_ex_ctrl.io.in.ex_ctrl.src2         := pipeA_control.io.src2
  pipeA_id_ex_ctrl.io.in.ex_ctrl.branch       := pipeA_control.io.branch
  pipeA_id_ex_ctrl.io.in.ex_ctrl.jumptype     := pipeA_control.io.jumptype
  pipeA_id_ex_ctrl.io.in.ex_ctrl.resultselect := pipeA_control.io.resultselect
  pipeA_id_ex_ctrl.io.in.ex_ctrl.wordinst     := pipeA_control.io.wordinst

  pipeB_id_ex_ctrl.io.in.ex_ctrl.itype        := pipeB_control.io.itype
  pipeB_id_ex_ctrl.io.in.ex_ctrl.aluop        := pipeB_control.io.aluop
  pipeB_id_ex_ctrl.io.in.ex_ctrl.src1         := pipeB_control.io.src1
  pipeB_id_ex_ctrl.io.in.ex_ctrl.src2         := pipeB_control.io.src2
  pipeB_id_ex_ctrl.io.in.ex_ctrl.branch       := pipeB_control.io.branch
  pipeB_id_ex_ctrl.io.in.ex_ctrl.jumptype     := pipeB_control.io.jumptype
  pipeB_id_ex_ctrl.io.in.ex_ctrl.resultselect := pipeB_control.io.resultselect
  pipeB_id_ex_ctrl.io.in.ex_ctrl.wordinst     := pipeB_control.io.wordinst

  // Set the memory control signals
  pipeA_id_ex_ctrl.io.in.mem_ctrl.memop := pipeA_control.io.memop
  pipeB_id_ex_ctrl.io.in.mem_ctrl.memop := pipeB_control.io.memop

  // Set the writeback control signals
  pipeA_id_ex_ctrl.io.in.wb_ctrl.toreg    := pipeA_control.io.toreg
  pipeA_id_ex_ctrl.io.in.wb_ctrl.regwrite := pipeA_control.io.regwrite

  pipeB_id_ex_ctrl.io.in.wb_ctrl.toreg    := pipeB_control.io.toreg
  pipeB_id_ex_ctrl.io.in.wb_ctrl.regwrite := pipeB_control.io.regwrite

  // Set the control signals on the id_ex pipeline register (Part III and/or Part IV)

  pipeA_id_ex.io.valid := true.B
  pipeA_id_ex.io.flush := hazard.io.id_ex_flush
  pipeA_id_ex_ctrl.io.valid := true.B
  pipeA_id_ex_ctrl.io.flush := hazard.io.id_ex_flush

  pipeB_id_ex.io.valid := true.B
  pipeB_id_ex.io.flush := hazard.io.id_ex_flush
  pipeB_id_ex_ctrl.io.valid := true.B
  pipeB_id_ex_ctrl.io.flush := hazard.io.id_ex_flush


  /////////////////////////////////////////////////////////////////////////////
  // EX STAGE
  /////////////////////////////////////////////////////////////////////////////

  val pipeA_ex_funct3 = pipeA_id_ex.io.data.instruction(14, 12)
  val pipeA_ex_funct7 = pipeA_id_ex.io.data.instruction(31, 25)
  val pipeA_ex_rs1    = pipeA_id_ex.io.data.instruction(19, 15)
  val pipeA_ex_rs2    = pipeA_id_ex.io.data.instruction(24, 20)
  val pipeA_ex_rd     = pipeA_id_ex.io.data.instruction(11, 7)

  val pipeB_ex_funct3 = pipeB_id_ex.io.data.instruction(14, 12)
  val pipeB_ex_funct7 = pipeB_id_ex.io.data.instruction(31, 25)
  val pipeB_ex_rs1    = pipeB_id_ex.io.data.instruction(19, 15)
  val pipeB_ex_rs2    = pipeB_id_ex.io.data.instruction(24, 20)
  val pipeB_ex_rd     = pipeB_id_ex.io.data.instruction(11, 7)

  // Set the inputs to the hazard detection unit from this stage (SKIP FOR PART I)
  hazard.io.pipeA_idex_memread := pipeA_id_ex_ctrl.io.data.mem_ctrl.memop === 2.U
  hazard.io.pipeA_idex_rd      := pipeA_ex_rd
  hazard.io.pipeB_idex_memread := pipeB_id_ex_ctrl.io.data.mem_ctrl.memop === 2.U
  hazard.io.pipeB_idex_rd      := pipeB_ex_rd

  // Set the input to the forwarding unit from this stage (SKIP FOR PART I)
  forwarding.io.ex_pipeA_rs1 := pipeA_ex_rs1
  forwarding.io.ex_pipeA_rs2 := pipeA_ex_rs2
  forwarding.io.ex_pipeB_rs1 := pipeB_ex_rs1
  forwarding.io.ex_pipeB_rs2 := pipeB_ex_rs2
  forwarding.io.ex_pipeA_rd  := pipeA_id_ex.io.data.instruction(11, 7)
  forwarding.io.ex_pipeA_rw  := pipeA_id_ex_ctrl.io.data.wb_ctrl.regwrite
  forwarding.io.mem_pipeA_rd := pipeA_ex_mem.io.data.instruction(11, 7)
  forwarding.io.mem_pipeA_rw := pipeA_ex_mem_ctrl.io.data.wb_ctrl.regwrite
  forwarding.io.mem_pipeB_rd := pipeB_ex_mem.io.data.instruction(11, 7)
  forwarding.io.mem_pipeB_rw := pipeB_ex_mem_ctrl.io.data.wb_ctrl.regwrite
  forwarding.io.wb_pipeA_rd  := pipeA_mem_wb.io.data.instruction(11, 7)
  forwarding.io.wb_pipeA_rw  := pipeA_mem_wb_ctrl.io.data.wb_ctrl.regwrite
  forwarding.io.wb_pipeB_rd  := pipeB_mem_wb.io.data.instruction(11, 7)
  forwarding.io.wb_pipeB_rw  := pipeB_mem_wb_ctrl.io.data.wb_ctrl.regwrite

  // Connect the ALU control wires (line 55 of single-cycle/cpu.scala)

  pipeA_aluControl.io.itype    := pipeA_id_ex_ctrl.io.data.ex_ctrl.itype
  pipeA_aluControl.io.aluop    := pipeA_id_ex_ctrl.io.data.ex_ctrl.aluop
  pipeA_aluControl.io.wordinst := pipeA_id_ex_ctrl.io.data.ex_ctrl.wordinst
  pipeA_aluControl.io.funct3   := pipeA_ex_funct3
  pipeA_aluControl.io.funct7   := pipeA_ex_funct7

  pipeB_aluControl.io.itype    := pipeB_id_ex_ctrl.io.data.ex_ctrl.itype
  pipeB_aluControl.io.aluop    := pipeB_id_ex_ctrl.io.data.ex_ctrl.aluop
  pipeB_aluControl.io.wordinst := pipeB_id_ex_ctrl.io.data.ex_ctrl.wordinst
  pipeB_aluControl.io.funct3   := pipeB_ex_funct3
  pipeB_aluControl.io.funct7   := pipeB_ex_funct7

  // Connect the NextPC control wires (line 47 of single-cycle/cpu.scala)
  // if inst1 is a branch or a jump; inst2 is not a valid inst
  // if inst1 is not a branch/jump/load/store; inst2 can be branch/jump/load/store
  when (!pipeB_id_ex.io.data.isValid) {
    nextPCmod.io.branch   := pipeA_id_ex_ctrl.io.data.ex_ctrl.branch
    nextPCmod.io.jumptype := pipeA_id_ex_ctrl.io.data.ex_ctrl.jumptype
  } .otherwise {
    nextPCmod.io.branch   := pipeB_id_ex_ctrl.io.data.ex_ctrl.branch
    nextPCmod.io.jumptype := pipeB_id_ex_ctrl.io.data.ex_ctrl.jumptype
  }
  nextPCmod.io.pipeB_valid := pipeB_id_ex.io.data.isValid


  // Insert the forward inputx mux here (SKIP FOR PART I)
  val pipeA_forwarded_inputx = MuxCase(0.U, Array((forwarding.io.pipeA_forward1 === 0.U) -> pipeA_id_ex.io.data.readdata1,
                                                  (forwarding.io.pipeA_forward1 === 1.U) -> pipeA_ex_mem.io.data.ex_result,
                                                  (forwarding.io.pipeA_forward1 === 2.U) -> pipeB_ex_mem.io.data.ex_result,
                                                  (forwarding.io.pipeA_forward1 === 3.U) -> pipeA_wb_writedata,
                                                  (forwarding.io.pipeA_forward1 === 4.U) -> pipeB_wb_writedata))
  val pipeA_forwarded_inputy = MuxCase(0.U, Array((forwarding.io.pipeA_forward2 === 0.U) -> pipeA_id_ex.io.data.readdata2,
                                                  (forwarding.io.pipeA_forward2 === 1.U) -> pipeA_ex_mem.io.data.ex_result,
                                                  (forwarding.io.pipeA_forward2 === 2.U) -> pipeB_ex_mem.io.data.ex_result,
                                                  (forwarding.io.pipeA_forward2 === 3.U) -> pipeA_wb_writedata,
                                                  (forwarding.io.pipeA_forward2 === 4.U) -> pipeB_wb_writedata))
  val pipeB_forwarded_inputx = MuxCase(0.U, Array((forwarding.io.pipeB_forward1 === 0.U) -> pipeB_id_ex.io.data.readdata1,
                                                  (forwarding.io.pipeB_forward1 === 1.U) -> 0.U,
                                                  (forwarding.io.pipeB_forward1 === 2.U) -> pipeA_ex_mem.io.data.ex_result,
                                                  (forwarding.io.pipeB_forward1 === 3.U) -> pipeB_ex_mem.io.data.ex_result,
                                                  (forwarding.io.pipeB_forward1 === 4.U) -> pipeA_wb_writedata,
                                                  (forwarding.io.pipeB_forward1 === 5.U) -> pipeB_wb_writedata))

  val pipeB_forwarded_inputy = MuxCase(0.U, Array((forwarding.io.pipeB_forward2 === 0.U) -> pipeB_id_ex.io.data.readdata2,
                                                  (forwarding.io.pipeB_forward2 === 1.U) -> 0.U,
                                                  (forwarding.io.pipeB_forward2 === 2.U) -> pipeA_ex_mem.io.data.ex_result,
                                                  (forwarding.io.pipeB_forward2 === 3.U) -> pipeB_ex_mem.io.data.ex_result,
                                                  (forwarding.io.pipeB_forward2 === 4.U) -> pipeA_wb_writedata,
                                                  (forwarding.io.pipeB_forward2 === 5.U) -> pipeB_wb_writedata))

  // Input x mux (line 62 of single-cycle/cpu.scala)
  val pipeA_ex_inputx = Mux(pipeA_id_ex_ctrl.io.data.ex_ctrl.src1, pipeA_id_ex.io.data.pc, pipeA_forwarded_inputx)
  val pipeB_ex_inputx = Mux(pipeB_id_ex_ctrl.io.data.ex_ctrl.src1, pipeB_id_ex.io.data.pc, pipeB_forwarded_inputx)
  // Input y mux (line 63 of single-cycle/cpu.scala)
  val pipeA_ex_inputy = MuxCase(0.U, Array((pipeA_id_ex_ctrl.io.data.ex_ctrl.src2 === 0.U) -> pipeA_forwarded_inputy,
                                           (pipeA_id_ex_ctrl.io.data.ex_ctrl.src2 === 1.U) -> pipeA_id_ex.io.data.sextImm,
                                           (pipeA_id_ex_ctrl.io.data.ex_ctrl.src2 === 2.U) -> 4.U))
  val pipeB_ex_inputy = MuxCase(0.U, Array((pipeB_id_ex_ctrl.io.data.ex_ctrl.src2 === 0.U) -> pipeB_forwarded_inputy,
                                           (pipeB_id_ex_ctrl.io.data.ex_ctrl.src2 === 1.U) -> pipeB_id_ex.io.data.sextImm,
                                           (pipeB_id_ex_ctrl.io.data.ex_ctrl.src2 === 2.U) -> 4.U))

  // Set the ALU operation  (line 61 of single-cycle/cpu.scala)
  pipeA_alu.io.operation  := pipeA_aluControl.io.operation
  pipeB_alu.io.operation  := pipeB_aluControl.io.operation

  // Connect the ALU data wires
  pipeA_alu.io.inputx := pipeA_ex_inputx
  pipeA_alu.io.inputy := pipeA_ex_inputy
  pipeB_alu.io.inputx := pipeB_ex_inputx
  pipeB_alu.io.inputy := pipeB_ex_inputy

  // Connect the NextPC data wires (line 49 of single-cycle/cpu.scala)
  when (!pipeB_id_ex.io.data.isValid) {
    nextPCmod.io.inputx := pipeA_forwarded_inputx
    nextPCmod.io.inputy := pipeA_forwarded_inputy
    nextPCmod.io.pc     := pipeA_id_ex.io.data.pc
    nextPCmod.io.imm    := pipeA_id_ex.io.data.sextImm
    nextPCmod.io.funct3 := pipeA_ex_funct3
  } .otherwise {
    nextPCmod.io.inputx := pipeB_forwarded_inputx
    nextPCmod.io.inputy := pipeB_forwarded_inputy
    nextPCmod.io.pc     := pipeB_id_ex.io.data.pc
    nextPCmod.io.imm    := pipeB_id_ex.io.data.sextImm
    nextPCmod.io.funct3 := pipeB_ex_funct3
  }
  // Set the EX/MEM register values
  pipeA_ex_mem.io.in.instruction   := pipeA_id_ex.io.data.instruction
  pipeA_ex_mem.io.in.mem_writedata := pipeA_forwarded_inputy
  pipeB_ex_mem.io.in.instruction   := pipeB_id_ex.io.data.instruction
  pipeB_ex_mem.io.in.mem_writedata := pipeB_forwarded_inputy

  pipeA_ex_mem_ctrl.io.in.mem_ctrl.memop   := pipeA_id_ex_ctrl.io.data.mem_ctrl.memop
  pipeA_ex_mem_ctrl.io.in.wb_ctrl.toreg    := pipeA_id_ex_ctrl.io.data.wb_ctrl.toreg
  pipeA_ex_mem_ctrl.io.in.wb_ctrl.regwrite := pipeA_id_ex_ctrl.io.data.wb_ctrl.regwrite

  pipeB_ex_mem_ctrl.io.in.mem_ctrl.memop   := pipeB_id_ex_ctrl.io.data.mem_ctrl.memop
  pipeB_ex_mem_ctrl.io.in.wb_ctrl.toreg    := pipeB_id_ex_ctrl.io.data.wb_ctrl.toreg
  pipeB_ex_mem_ctrl.io.in.wb_ctrl.regwrite := pipeB_id_ex_ctrl.io.data.wb_ctrl.regwrite

  when (!pipeB_id_ex.io.data.isValid) {
    pipeA_ex_mem.io.in.nextpc := nextPCmod.io.nextpc
    pipeA_ex_mem.io.in.taken  := nextPCmod.io.taken
    pipeB_ex_mem.io.in.nextpc := 0.U
    pipeB_ex_mem.io.in.taken  := false.B
  } .otherwise {
    pipeA_ex_mem.io.in.nextpc := 0.U
    pipeA_ex_mem.io.in.taken  := false.B
    pipeB_ex_mem.io.in.nextpc := nextPCmod.io.nextpc
    pipeB_ex_mem.io.in.taken  := nextPCmod.io.taken
  }

  pipeA_ex_mem.io.in.isValid := pipeA_id_ex.io.data.isValid
  pipeB_ex_mem.io.in.isValid := pipeB_id_ex.io.data.isValid

  // Determine which result to use (line 38 of single-cycle/cpu.scala)
  pipeA_ex_mem.io.in.ex_result := Mux(pipeA_id_ex_ctrl.io.data.ex_ctrl.resultselect, pipeA_id_ex.io.data.sextImm, pipeA_alu.io.result)
  pipeB_ex_mem.io.in.ex_result := Mux(pipeB_id_ex_ctrl.io.data.ex_ctrl.resultselect, pipeB_id_ex.io.data.sextImm, pipeB_alu.io.result)

  pipeA_ex_mem.io.in.pc := pipeA_id_ex.io.data.pc
  pipeB_ex_mem.io.in.pc := pipeB_id_ex.io.data.pc

  // Set the control signals on the ex_mem pipeline register (Part III and/or Part IV)
  pipeA_ex_mem.io.valid := true.B
  pipeA_ex_mem.io.flush := hazard.io.ex_mem_flush
  pipeB_ex_mem.io.valid := true.B
  pipeB_ex_mem.io.flush := hazard.io.ex_mem_flush

  pipeA_ex_mem_ctrl.io.valid := true.B
  pipeA_ex_mem_ctrl.io.flush := hazard.io.ex_mem_flush
  pipeB_ex_mem_ctrl.io.valid := true.B
  pipeB_ex_mem_ctrl.io.flush := hazard.io.ex_mem_flush

  /////////////////////////////////////////////////////////////////////////////
  // MEM STAGE
  /////////////////////////////////////////////////////////////////////////////

  val pipeA_mem_funct3 = pipeA_ex_mem.io.data.instruction(14, 12)
  val pipeB_mem_funct3 = pipeB_ex_mem.io.data.instruction(14, 12)

  // Set data memory IO (line 67 of single-cycle/cpu.scala)
  // if inst1 is a load/store, the inst2 is a nop
  // if inst1 is not a branch/jump/load/store, the inst2 could be a load/store
  when (!pipeB_ex_mem.io.data.isValid) {
    io.dmem.address   := pipeA_ex_mem.io.data.ex_result // this is fine because ex_result is alu's result when inst is LD/ST
    io.dmem.memread   := ~pipeA_ex_mem_ctrl.io.data.mem_ctrl.memop(0)
    io.dmem.memwrite  := pipeA_ex_mem_ctrl.io.data.mem_ctrl.memop(0)
    io.dmem.valid     := pipeA_ex_mem_ctrl.io.data.mem_ctrl.memop(1)
    io.dmem.maskmode  := pipeA_mem_funct3(1, 0)
    io.dmem.sext      := ~pipeA_mem_funct3(2)
    io.dmem.writedata := pipeA_ex_mem.io.data.mem_writedata
  } .otherwise {
    io.dmem.address   := pipeB_ex_mem.io.data.ex_result // this is fine because ex_result is alu's result when inst is LD/ST
    io.dmem.memread   := ~pipeB_ex_mem_ctrl.io.data.mem_ctrl.memop(0)
    io.dmem.memwrite  := pipeB_ex_mem_ctrl.io.data.mem_ctrl.memop(0)
    io.dmem.valid     := pipeB_ex_mem_ctrl.io.data.mem_ctrl.memop(1)
    io.dmem.maskmode  := pipeB_mem_funct3(1, 0)
    io.dmem.sext      := ~pipeB_mem_funct3(2)
    io.dmem.writedata := pipeB_ex_mem.io.data.mem_writedata
  }

  // Send next_pc back to the fetch stage
  when (!pipeB_ex_mem.io.data.isValid) {
    next_pc := pipeA_ex_mem.io.data.nextpc
  } .otherwise {
    next_pc := pipeB_ex_mem.io.data.nextpc
  }

  // Send input signals to the hazard detection unit (SKIP FOR PART I)
  hazard.io.pipeA_exmem_taken := pipeA_ex_mem.io.data.taken
  hazard.io.pipeB_exmem_taken := pipeB_ex_mem.io.data.taken
  // Send input signals to the forwarding unit (SKIP FOR PART I)

  // Wire the MEM/WB register
  // if inst1 is a load then inst2 is a nop
  // if inst2 is not branch/jump/load/store then inst2 could be a load
  pipeA_mem_wb.io.in.readdata    := io.dmem.readdata
  pipeA_mem_wb.io.in.ex_result   := pipeA_ex_mem.io.data.ex_result
  pipeA_mem_wb.io.in.instruction := pipeA_ex_mem.io.data.instruction
  pipeB_mem_wb.io.in.readdata    := io.dmem.readdata
  pipeB_mem_wb.io.in.ex_result   := pipeB_ex_mem.io.data.ex_result
  pipeB_mem_wb.io.in.instruction := pipeB_ex_mem.io.data.instruction

  pipeA_mem_wb_ctrl.io.in.wb_ctrl.toreg    := pipeA_ex_mem_ctrl.io.data.wb_ctrl.toreg
  pipeA_mem_wb_ctrl.io.in.wb_ctrl.regwrite := pipeA_ex_mem_ctrl.io.data.wb_ctrl.regwrite
  pipeB_mem_wb_ctrl.io.in.wb_ctrl.toreg    := pipeB_ex_mem_ctrl.io.data.wb_ctrl.toreg
  pipeB_mem_wb_ctrl.io.in.wb_ctrl.regwrite := pipeB_ex_mem_ctrl.io.data.wb_ctrl.regwrite

  pipeA_mem_wb.io.in.pc      := pipeA_ex_mem.io.data.pc
  pipeB_mem_wb.io.in.pc      := pipeB_ex_mem.io.data.pc
  pipeA_mem_wb.io.in.isValid := pipeA_ex_mem.io.data.isValid
  pipeB_mem_wb.io.in.isValid := pipeB_ex_mem.io.data.isValid

  // Set the control signals on the mem_wb pipeline register
  pipeA_mem_wb.io.valid      := true.B
  pipeA_mem_wb.io.flush      := false.B
  pipeB_mem_wb.io.valid      := true.B
  pipeB_mem_wb.io.flush      := false.B

  pipeA_mem_wb_ctrl.io.valid := true.B
  pipeA_mem_wb_ctrl.io.flush := false.B
  pipeB_mem_wb_ctrl.io.valid := true.B
  pipeB_mem_wb_ctrl.io.flush := false.B


  /////////////////////////////////////////////////////////////////////////////
  // WB STAGE
  /////////////////////////////////////////////////////////////////////////////
 
  val pipeA_wb_rd = pipeA_mem_wb.io.data.instruction(11, 7)
  val pipeB_wb_rd = pipeB_mem_wb.io.data.instruction(11, 7)
  registers.io.pipeA_writereg := pipeA_wb_rd
  registers.io.pipeB_writereg := pipeB_wb_rd

  // Set the writeback data mux (line 38 single-cycle/cpu.scala)
  registers.io.pipeA_wen := (pipeA_wb_rd =/= 0.U) && (pipeA_mem_wb_ctrl.io.data.wb_ctrl.regwrite === true.B)
  registers.io.pipeB_wen := (pipeB_wb_rd =/= 0.U) && (pipeB_mem_wb_ctrl.io.data.wb_ctrl.regwrite === true.B)

  // Write the data to the register file
  pipeA_wb_writedata           := Mux(pipeA_mem_wb_ctrl.io.data.wb_ctrl.toreg, pipeA_mem_wb.io.data.readdata, pipeA_mem_wb.io.data.ex_result)
  pipeB_wb_writedata           := Mux(pipeB_mem_wb_ctrl.io.data.wb_ctrl.toreg, pipeB_mem_wb.io.data.readdata, pipeB_mem_wb.io.data.ex_result)
  registers.io.pipeA_writedata := pipeA_wb_writedata
  registers.io.pipeB_writedata := pipeB_wb_writedata


/*
  val pipeA_IF_pc  = Mux(issueUnit.io.pipeA_valid, pc(15, 0), "hFFFF".U)
  val pipeA_ID_pc  = Mux(pipeA_if_id.io.data.isValid, pipeA_if_id.io.data.pc(15,0), "hFFFF".U)
  val pipeA_EX_pc  = Mux(pipeA_id_ex.io.data.isValid, pipeA_id_ex.io.data.pc(15,0), "hFFFF".U)
  val pipeA_MEM_pc = Mux(pipeA_ex_mem.io.data.isValid, pipeA_ex_mem.io.data.pc(15,0), "hFFFF".U)
  val pipeA_WB_pc  = Mux(pipeA_mem_wb.io.data.isValid, pipeA_mem_wb.io.data.pc(15,0), "hFFFF".U)

  val pipeB_IF_pc  = Mux(issueUnit.io.pipeB_valid, pc(15, 0) + 4.U, "hFFFF".U)
  val pipeB_ID_pc  = Mux(pipeB_if_id.io.data.isValid, pipeB_if_id.io.data.pc(15,0), "hFFFF".U)
  val pipeB_EX_pc  = Mux(pipeB_id_ex.io.data.isValid, pipeB_id_ex.io.data.pc(15,0), "hFFFF".U)
  val pipeB_MEM_pc = Mux(pipeB_ex_mem.io.data.isValid, pipeB_ex_mem.io.data.pc(15,0), "hFFFF".U)
  val pipeB_WB_pc  = Mux(pipeB_mem_wb.io.data.isValid, pipeB_mem_wb.io.data.pc(15,0), "hFFFF".U)

  printf(p"PipeA: IF->${Hexadecimal(pipeA_IF_pc)} ID->${Hexadecimal(pipeA_ID_pc)} EX->${Hexadecimal(pipeA_EX_pc)} MEM->${Hexadecimal(pipeA_MEM_pc)} WB->${Hexadecimal(pipeA_WB_pc)}\n")
  printf(p"PipeB: IF->${Hexadecimal(pipeB_IF_pc)} ID->${Hexadecimal(pipeB_ID_pc)} EX->${Hexadecimal(pipeB_EX_pc)} MEM->${Hexadecimal(pipeB_MEM_pc)} WB->${Hexadecimal(pipeB_WB_pc)}\n")
*/

/*
  val pipeA_pc = pipeA_mem_wb.io.data.pc
  val pipeB_pc = pipeB_mem_wb.io.data.pc
  when (pipeA_mem_wb.io.data.isValid) {
    printf(p"Committed pc: ${Hexadecimal(pipeA_pc)}\n")
  }
  when (pipeB_mem_wb.io.data.isValid) {
    printf(p"Committed pc: ${Hexadecimal(pipeB_pc)}\n")
  }
  printf(p"-\n")
*/

}

/*
 * Object to make it easier to print information about the CPU
 */
object PipelinedDualIssueCPUInfo {
  def getModules(): List[String] = {
    List(
      "imem",
      "dmem",
      "pipeA_control",
      "pipeB_control",
      "registers",
      "pipeA_aluControl",
      "pipeB_aluControl",
      "pipeA_alu",
      "pipeB_alu",
      "pipeA_immGen",
      "pipeB_immGen",
      "nextPCmod",
      "forwarding",
      "hazard",
    )
  }
  def getPipelineRegs(): List[String] = {
    List(
      "pipeA_if_id",
      "pipeA_id_ex",
      "pipeA_id_ex_ctrl",
      "pipeA_ex_mem",
      "pipeA_ex_mem_ctrl",
      "pipeA_mem_wb",
      "pipeA_mem_wb_ctrl",
      "pipeB_if_id",
      "pipeB_id_ex",
      "pipeB_id_ex_ctrl",
      "pipeB_ex_mem",
      "pipeB_ex_mem_ctrl",
      "pipeB_mem_wb",
      "pipeB_mem_wb_ctrl"
    )
  }
}
