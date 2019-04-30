// This file is where all of the CPU components are assembled into the whole CPU

package dinocpu

import chisel3._
import chisel3.util._

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.6 of Patterson and Hennessy
 * This follows figure 4.49
 */
class PipelinedCPU(implicit val conf: CPUConfig) extends Module {
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
    val jump      = UInt(2.W)
    val prediction= Bool()
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
    val rs1       = UInt(5.W)    //pipelined only
    val rs2       = UInt(5.W)    //pipelined only
    val branchpc  = UInt(32.W)
  }

  // Everything in the register between ID and EX stages
  class EXMEMBundle extends Bundle {
    val writereg  = UInt(5.W)
    val readdata2 = UInt(32.W)
    val aluresult = UInt(32.W)
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
  val forwarding = Module(new ForwardingUnit())  //pipelined only
  val hazard     = Module(new HazardUnit())      //pipelined only
  val predictor  = Module(conf.getBranchPredictor)
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  // The four pipeline registers
  val if_id      = RegInit(0.U.asTypeOf(new IFIDBundle))
  val id_ex      = RegInit(0.U.asTypeOf(new IDEXBundle))
  val ex_mem     = RegInit(0.U.asTypeOf(new EXMEMBundle))
  val mem_wb     = RegInit(0.U.asTypeOf(new MEMWBBundle))

  if (conf.debug) { printf("Cycle=%d ", cycleCount) }

  // For tracking branch predictor stats
  val bpCorrect   = RegInit(0.U(32.W))
  val bpIncorrect = RegInit(0.U(32.W))
  when (bpCorrect > (1.U << 20)) {
    // Force these wires not to disappear
    printf(p"BP correct: $bpCorrect; incorrect: $bpIncorrect\n")
  }

  // Forward declaration of wires that connect different stages

  // From memory back to fetch. Since we don't decide whether to take a branch or not until the memory stage.
  val mem_next_pc = Wire(UInt())

  // From the decode stage back to fetch. Used when branch is predicted true
  val id_next_pc = Wire(UInt())

  // For wb back to other stages
  val write_data = Wire(UInt())

  /////////////////////////////////////////////////////////////////////////////
  // FETCH STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Note: This comes from the memory stage!
  // Only update the pc if the pcwrite flag is enabled
  pc := MuxCase(0.U, Array(
                (hazard.io.pcwrite === 0.U) -> pcPlusFour.io.result,
                (hazard.io.pcwrite === 1.U) -> mem_next_pc,
                (hazard.io.pcwrite === 2.U) -> pc,
                (hazard.io.pcwrite === 3.U) -> id_next_pc))

  if (conf.debug) { printf(p"PC: $pc\n") }

  // Send the PC to the instruction memory port to get the instruction
  io.imem.address := pc

  // Get the PC + 4
  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  // Fill the IF/ID register if we are not bubbling IF/ID
  // otherwise, leave the IF/ID register *unchanged*
  when (~hazard.io.ifid_bubble) {
    if_id.instruction := io.imem.instruction
    if_id.pc          := pc
    if_id.pcplusfour  := pcPlusFour.io.result
  }

  // Flush IF/ID when required
  when (hazard.io.ifid_flush) {
    if_id.instruction := 0.U
    if_id.pc          := 0.U
    if_id.pcplusfour  := 0.U
  }

  if (conf.debug) { printf(p"IF/ID: $if_id\n") }

  /////////////////////////////////////////////////////////////////////////////
  // ID STAGE
  /////////////////////////////////////////////////////////////////////////////

  val rs1 = if_id.instruction(19,15)
  val rs2 = if_id.instruction(24,20)

  // Send input from this stage to hazard detection unit
  hazard.io.rs1 := rs1
  hazard.io.rs2 := rs2

  // Set the predictor inputs
  predictor.io.pc := if_id.pc

  // Set the branch for this stage to the hazard
  // This is needed for when the branch is predicted taken
  hazard.io.id_branch := control.io.branch & predictor.io.prediction

  // Send opcode to control
  control.io.opcode := if_id.instruction(6,0)

  // Send register numbers to the register file
  registers.io.readreg1 := rs1
  registers.io.readreg2 := rs2

  // Send the instruction to the immediate generator
  immGen.io.instruction := if_id.instruction

  // Connect the branchAdd unit
  branchAdd.io.inputx := if_id.pc
  branchAdd.io.inputy := immGen.io.sextImm

  // Send the PC back to fetch
  id_next_pc := branchAdd.io.result

  // Fill the id_ex register
  id_ex.writereg   := if_id.instruction(11,7)
  id_ex.rs1        := rs1
  id_ex.rs2        := rs2
  id_ex.funct7     := if_id.instruction(31,25)
  id_ex.funct3     := if_id.instruction(14,12)
  id_ex.imm        := immGen.io.sextImm
  id_ex.readdata2  := registers.io.readdata2
  id_ex.readdata1  := registers.io.readdata1
  id_ex.pc         := if_id.pc
  id_ex.pcplusfour := if_id.pcplusfour
  id_ex.branchpc   := branchAdd.io.result

  // Set the execution control signals
  id_ex.excontrol.add       := control.io.add
  id_ex.excontrol.immediate := control.io.immediate
  id_ex.excontrol.alusrc1   := control.io.alusrc1
  id_ex.excontrol.branch    := control.io.branch
  id_ex.excontrol.jump      := control.io.jump
  id_ex.excontrol.prediction:= predictor.io.prediction

  // Set the memory control signals
  id_ex.mcontrol.memread  := control.io.memread
  id_ex.mcontrol.memwrite := control.io.memwrite
  id_ex.mcontrol.maskmode := if_id.instruction(13,12)
  id_ex.mcontrol.sext     := ~if_id.instruction(14)

  // Set the writeback control signals
  id_ex.wbcontrol.toreg    := control.io.toreg
  id_ex.wbcontrol.regwrite := control.io.regwrite

  when (hazard.io.idex_bubble) {
    // Set the id_ex control to 0 to indicate a bubble
    id_ex.excontrol := 0.U.asTypeOf(new EXControl)
    id_ex.mcontrol  := 0.U.asTypeOf(new MControl)
    id_ex.wbcontrol := 0.U.asTypeOf(new WBControl)
  }

  if (conf.debug) { printf("DASM(%x)\n", if_id.instruction) }
  if (conf.debug) { printf(p"ID/EX: $id_ex\n") }

  /////////////////////////////////////////////////////////////////////////////
  // EX STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the inputs to the hazard detection unit from this stage
  hazard.io.idex_memread := id_ex.mcontrol.memread
  hazard.io.idex_rd      := id_ex.writereg

  // Set the input to the forwarding unit from this stage
  forwarding.io.rs1 := id_ex.rs1
  forwarding.io.rs2 := id_ex.rs2

  // Connect the ALU control wires (line 45 of single-cycle/cpu.scala)
  aluControl.io.add       := id_ex.excontrol.add
  aluControl.io.immediate := id_ex.excontrol.immediate
  aluControl.io.funct7    := id_ex.funct7
  aluControl.io.funct3    := id_ex.funct3

  // Insert the forward inputx mux here
  val forward_inputx = Wire(UInt(32.W))
  forward_inputx  := MuxCase(0.U, Array(
                           (forwarding.io.forwardA === 0.U) -> id_ex.readdata1,
                           (forwarding.io.forwardA === 1.U) -> ex_mem.aluresult,
                           (forwarding.io.forwardA === 2.U) -> write_data))


  val alu_inputx = Wire(UInt(32.W))
  alu_inputx := DontCare
  // Insert the ALU inpux mux here (line 59 of single-cycle/cpu.scala)
  switch(id_ex.excontrol.alusrc1) {
    is(0.U) { alu_inputx := forward_inputx }
    is(1.U) { alu_inputx := 0.U }
    is(2.U) { alu_inputx := id_ex.pc }
  }
  alu.io.inputx := alu_inputx

  // Insert forward inputy mux here
  val forward_inputy = MuxCase(0.U, Array(
                           (forwarding.io.forwardB === 0.U) -> id_ex.readdata2,
                           (forwarding.io.forwardB === 1.U) -> ex_mem.aluresult,
                           (forwarding.io.forwardB === 2.U) -> write_data))

  val alu_inputy = Wire(UInt(32.W))
  alu_inputy := forward_inputy

  // Input y mux (line 66 of single-cycle/cpu.scala)
  alu.io.inputy := Mux(id_ex.excontrol.immediate, id_ex.imm, alu_inputy)

  // Connect the branch control wire (line 54 of single-cycle/cpu.scala)
  branchCtrl.io.branch := id_ex.excontrol.branch
  branchCtrl.io.funct3 := id_ex.funct3
  branchCtrl.io.inputx := forward_inputx
  branchCtrl.io.inputy := forward_inputy

  // Set the ALU operation
  alu.io.operation := aluControl.io.operation

  // Set the EX/MEM register values
  ex_mem.readdata2  := alu_inputy
  ex_mem.aluresult  := alu.io.result
  ex_mem.writereg   := id_ex.writereg
  ex_mem.pcplusfour := id_ex.pcplusfour
  ex_mem.mcontrol   := id_ex.mcontrol
  ex_mem.wbcontrol  := id_ex.wbcontrol

  // Update the branch predictor
  when (id_ex.excontrol.branch && ~hazard.io.exmem_bubble) {
    // when it's a branch, update the branch predictor
    predictor.io.update := true.B
    predictor.io.taken  := branchCtrl.io.taken

    // Update the branch predictor stats
    when (id_ex.excontrol.prediction === branchCtrl.io.taken) {
      bpCorrect   := bpCorrect + 1.U
    } .otherwise {
      bpIncorrect := bpIncorrect + 1.U
    }

  } .otherwise {
    // If not a branch, don't update
    predictor.io.update := false.B
    predictor.io.taken  := DontCare
  }

  // Calculate whether which PC we should use and set the taken flag (line 92 in single-cycle/cpu.scala)
  when (id_ex.excontrol.jump(1)) {
    // If the high bit in the jump control is set, then we jump no matter what.
    when (id_ex.excontrol.jump(0) === 0.U) {
      // jal, use the immediate computed result
      ex_mem.nextpc := id_ex.branchpc
      ex_mem.mcontrol.taken := true.B
    } .otherwise {
      // jalr, use the result from the ALU
      ex_mem.nextpc := alu.io.result & Cat(Fill(31, 1.U), 0.U)
      ex_mem.mcontrol.taken := true.B
    }
  } .otherwise {
    // No need to do anything unless the prediction was wrong
    when (branchCtrl.io.taken =/= id_ex.excontrol.prediction && id_ex.excontrol.branch) {
      when (branchCtrl.io.taken) { ex_mem.nextpc := id_ex.branchpc }
      .otherwise { ex_mem.nextpc := id_ex.pcplusfour }
      ex_mem.mcontrol.taken := true.B
    } .otherwise {
      ex_mem.mcontrol.taken  := false.B
      ex_mem.nextpc := DontCare
    }
  }

  // Check for bubble EX/MEM register
  when (hazard.io.exmem_bubble) {
    // Set the ex_mem control to 0 to indicate a bubble
    ex_mem.mcontrol  := 0.U.asTypeOf(new MControl)
    ex_mem.wbcontrol := 0.U.asTypeOf(new WBControl)
  }

  if (conf.debug) { printf(p"EX/MEM: $ex_mem\n") }

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

  // Send next_pc back to the fetch stage
  mem_next_pc := ex_mem.nextpc

  // Send input signals to the hazard detection unit
  hazard.io.exmem_taken := ex_mem.mcontrol.taken

  // Send input signals to the forwarding unit
  forwarding.io.exmemrd := ex_mem.writereg
  forwarding.io.exmemrw := ex_mem.wbcontrol.regwrite

  // Wire the MEM/WB register
  mem_wb.writereg   := ex_mem.writereg
  mem_wb.aluresult  := ex_mem.aluresult
  mem_wb.pcplusfour := ex_mem.pcplusfour
  mem_wb.readdata   := io.dmem.readdata
  mem_wb.wbcontrol  := ex_mem.wbcontrol

  if (conf.debug) { printf(p"MEM/WB: $mem_wb\n") }

  /////////////////////////////////////////////////////////////////////////////
  // WB STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the writeback data mux (line 78 single-cycle/cpu.scala)
  write_data := MuxCase(mem_wb.aluresult, Array(
                       (mem_wb.wbcontrol.toreg === 0.U) -> mem_wb.aluresult,
                       (mem_wb.wbcontrol.toreg === 1.U) -> mem_wb.readdata,
                       (mem_wb.wbcontrol.toreg === 2.U) -> mem_wb.pcplusfour))

  // Write the data to the register file
  registers.io.writedata := write_data
  registers.io.writereg  := mem_wb.writereg
  registers.io.wen       := mem_wb.wbcontrol.regwrite && (registers.io.writereg =/= 0.U)

  // Set the input signals for the forwarding unit
  forwarding.io.memwbrd := mem_wb.writereg
  forwarding.io.memwbrw := mem_wb.wbcontrol.regwrite

  if (conf.debug) { printf("---------------------------------------------\n") }
}
