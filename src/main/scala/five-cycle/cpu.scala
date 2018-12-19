// This file is where all of the CPU components are assembled into the whole CPU

package CODCPU

import chisel3._
import chisel3.util.Counter


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
  class IFIDBundle extends Bundle {
    val instruction = UInt(32.W)
    val pc = UInt(32.W)
  }

  class EXControl extends Bundle {
    val aluop  = UInt(2.W)
    val alusrc = Bool()
  }

  class MControl extends Bundle {
    val memread  = Bool()
    val memwrite = Bool()
    val branch   = Bool()
  }

  class WBControl extends Bundle {
    val memtoreg = Bool()
    val regwrite = Bool()
  }

  class IDEXBundle extends Bundle {
    val writereg  = UInt(5.W)
    val funct7    = UInt(7.W)
    val funct3    = UInt(3.W)
    val imm       = UInt(32.W)
    val readdata2 = UInt(32.W)
    val readdata1 = UInt(32.W)
    val pc        = UInt(32.W)
    val excontrol = new EXControl
    val mcontrol  = new MControl
    val wbcontrol = new WBControl
  }

  class EXMEMBundle extends Bundle {
    val writereg  = UInt(5.W)
    val readdata2 = UInt(32.W)
    val aluresult = UInt(32.W)
    val zero      = Bool()
    val nextpc    = UInt(32.W)
    val mcontrol  = new MControl
    val wbcontrol = new WBControl
  }

  class MEMWBBundle extends Bundle {
    val writereg  = UInt(5.W)
    val aluresult = UInt(32.W)
    val readdata  = UInt(32.W)
    val wbcontrol = new WBControl
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
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  val if_id      = RegInit(0.U.asTypeOf(new IFIDBundle))
  val id_ex      = RegInit(0.U.asTypeOf(new IDEXBundle))
  val ex_mem     = RegInit(0.U.asTypeOf(new EXMEMBundle))
  val mem_wb     = RegInit(0.U.asTypeOf(new MEMWBBundle))

  printf("Cycle=%d ", cycleCount)

  // Forward declaration of wires that connect different stages

  // From memory back to fetch
  val next_pc      = Wire(UInt())
  val branch_taken = Wire(Bool())

  // For flushing stages.
  val flush_idex  = Wire(Bool())
  val flush_exmem = Wire(Bool())

  /////////////////////////////////////////////////////////////////////////////
  // FETCH STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Note: This comes from the memory stage!
  pc := Mux(branch_taken,
            next_pc,
            pcPlusFour.io.result)

  io.imem.address := pc

  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  if_id.instruction := io.imem.instruction
  if_id.pc := pc

  printf("pc=0x%x\n", pc)

  printf(p"IF/ID: $if_id\n")

  /////////////////////////////////////////////////////////////////////////////
  // ID STAGE
  /////////////////////////////////////////////////////////////////////////////

  control.io.opcode := if_id.instruction(6,0)

  registers.io.readreg1 := if_id.instruction(19,15)
  registers.io.readreg2 := if_id.instruction(24,20)

  immGen.io.instruction := if_id.instruction

  id_ex.writereg  := if_id.instruction(11,7)
  id_ex.funct7    := if_id.instruction(31,25)
  id_ex.funct3    := if_id.instruction(14,12)
  id_ex.imm       := immGen.io.sextImm
  id_ex.readdata2 := registers.io.readdata2
  id_ex.readdata1 := registers.io.readdata1
  id_ex.pc        := if_id.pc

  when (flush_idex) {
    id_ex.excontrol := 0.U.asTypeOf(new EXControl)
    id_ex.mcontrol  := 0.U.asTypeOf(new MControl)
    id_ex.wbcontrol := 0.U.asTypeOf(new WBControl)
  } .otherwise {
    id_ex.excontrol.aluop  := control.io.aluop
    id_ex.excontrol.alusrc := control.io.alusrc2

    id_ex.mcontrol.memread  := control.io.memread
    id_ex.mcontrol.memwrite := control.io.memwrite
    id_ex.mcontrol.branch   := control.io.branch

    id_ex.wbcontrol.memtoreg := control.io.memtoreg
    id_ex.wbcontrol.regwrite := control.io.regwrite
  }

  printf("DASM(%x)\n", if_id.instruction)
  printf(p"ID/EX: $id_ex\n")

  /////////////////////////////////////////////////////////////////////////////
  // EX STAGE
  /////////////////////////////////////////////////////////////////////////////

  aluControl.io.aluop  := id_ex.excontrol.aluop
  aluControl.io.funct7 := id_ex.funct7
  aluControl.io.funct3 := id_ex.funct3

  alu.io.inputx := id_ex.readdata1
  alu.io.inputy := Mux(id_ex.excontrol.alusrc, id_ex.imm, id_ex.readdata2)
  alu.io.operation := aluControl.io.operation

  branchAdd.io.inputx := id_ex.pc
  branchAdd.io.inputy := id_ex.imm

  ex_mem.readdata2 := id_ex.readdata2
  ex_mem.aluresult := alu.io.result
  //ex_mem.zero      := alu.io.zero

  ex_mem.nextpc := branchAdd.io.result

  ex_mem.writereg := id_ex.writereg

  when (flush_exmem) {
    ex_mem.mcontrol  := 0.U.asTypeOf(new MControl)
    ex_mem.wbcontrol := 0.U.asTypeOf(new WBControl)
  } .otherwise {
    ex_mem.mcontrol := id_ex.mcontrol
    ex_mem.wbcontrol := id_ex.wbcontrol
  }

  printf(p"EX/MEM: $ex_mem\n")

  /////////////////////////////////////////////////////////////////////////////
  // MEM STAGE
  /////////////////////////////////////////////////////////////////////////////

  io.dmem.address   := ex_mem.aluresult
  io.dmem.writedata := ex_mem.readdata2
  io.dmem.memread   := ex_mem.mcontrol.memread
  io.dmem.memwrite  := ex_mem.mcontrol.memwrite

  // Send this back to the fetch stage
  next_pc      := ex_mem.nextpc
  branch_taken := ex_mem.mcontrol.branch & ex_mem.zero

  mem_wb.writereg  := ex_mem.writereg
  mem_wb.aluresult := ex_mem.aluresult
  mem_wb.readdata  := io.dmem.readdata
  mem_wb.wbcontrol := ex_mem.wbcontrol

  // Now we know the direction of the branch. If it's taken, clear the control
  // for the previous stages.
  when (branch_taken) {
    flush_exmem := true.B
    flush_idex  := true.B
  } .otherwise {
    flush_exmem := false.B
    flush_idex  := false.B
  }

  printf(p"MEM/WB: $mem_wb\n")

  /////////////////////////////////////////////////////////////////////////////
  // WB STAGE
  /////////////////////////////////////////////////////////////////////////////

  registers.io.writedata := Mux(mem_wb.wbcontrol.memtoreg, mem_wb.readdata, mem_wb.aluresult)
  registers.io.writereg  := mem_wb.writereg
  registers.io.wen       := mem_wb.wbcontrol.regwrite && (registers.io.writereg =/= 0.U)

  printf("---------------------------------------------\n")
}
