// This file is where all of the CPU components are assembled into the whole CPU

package CODCPU

import chisel3._
import chisel3.util.Counter

import Common.MemPortIo

// I think I should define bundles for each of the pipeline registers
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

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.4 of Patterson and Hennessy
 * This follows figure 4.21
 */
class FiveCycleCPU extends Module {
  val io = IO(new Bundle {
    val imem = new MemPortIo(32)
    val dmem = new MemPortIo(32)
  })

  // All of the structures required
  val pc         = RegInit("h80000000".U)
  val instMem    = Module(new InstructionMemory())
  val control    = Module(new Control())
  val registers  = Module(new RegisterFile())
  val aluControl = Module(new ALUControl())
  val alu        = Module(new ALU())
  val immGen     = Module(new ImmediateGenerator())
  val dataMem    = Module(new DataMemory())
  val pcPlusFour = Module(new Adder())
  val branchAdd  = Module(new Adder())
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  val if_id      = Reg(new IFIDBundle)
  val id_ex      = Reg(new IDEXBundle)
  val ex_mem     = Reg(new EXMEMBundle)
  val mem_wb     = Reg(new MEMWBBundle)

  io.imem <> instMem.io.memport
  io.dmem <> dataMem.io.memport

  printf("Cycle=%d ", cycleCount)

  /////////////////////////////////////////////////////////////////////////////
  // FETCH STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Note: This comes from the memory stage!
  pc := Mux(ex_mem.mcontrol.branch & ex_mem.zero,
            branchAdd.io.result,
            ex_mem.nextpc)

  pc := RegNext(Mux(ex_mem.mcontrol.branch & ex_mem.zero,
                    branchAdd.io.result,
                    ex_mem.nextpc),
                (cycleCount % 5.U) === 0.U) // for now only enable PC every 5 cycles

  instMem.io.address := pc

  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  if_id.instruction := instMem.io.instruction
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

  id_ex.excontrol.aluop  := control.io.aluop
  id_ex.excontrol.alusrc := control.io.alusrc

  id_ex.mcontrol.memread  := control.io.memread
  id_ex.mcontrol.memwrite := control.io.memwrite
  id_ex.mcontrol.branch   := control.io.branch

  id_ex.wbcontrol.memtoreg := control.io.memtoreg
  id_ex.wbcontrol.regwrite := control.io.regwrite

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
  ex_mem.zero      := alu.io.zero

  ex_mem.mcontrol := id_ex.mcontrol
  ex_mem.wbcontrol := id_ex.wbcontrol

  printf(p"EX/MEM: $ex_mem\n")

  /////////////////////////////////////////////////////////////////////////////
  // MEM STAGE
  /////////////////////////////////////////////////////////////////////////////

  dataMem.io.address   := ex_mem.aluresult
  dataMem.io.writedata := ex_mem.readdata2
  dataMem.io.memread   := ex_mem.mcontrol.memread
  dataMem.io.memwrite  := ex_mem.mcontrol.memwrite

  mem_wb.writereg  := ex_mem.writereg
  mem_wb.aluresult := ex_mem.aluresult
  mem_wb.readdata  := dataMem.io.readdata
  mem_wb.wbcontrol := ex_mem.wbcontrol

  printf(p"MEM/WB: $mem_wb\n")

  /////////////////////////////////////////////////////////////////////////////
  // WB STAGE
  /////////////////////////////////////////////////////////////////////////////

  registers.io.writedata := Mux(mem_wb.wbcontrol.memtoreg, mem_wb.readdata, mem_wb.aluresult)
  registers.io.writereg  := mem_wb.writereg
  registers.io.wen       := mem_wb.wbcontrol.regwrite && (registers.io.writereg =/= 0.U)
}
