// This file is where all of the CPU components are assembled into the whole CPU

package dinocpu

import chisel3._
import chisel3.util.{Counter, MuxCase}

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.6 of Patterson and Hennessy
 * This follows figure 4.49
 */
class PipelinedCPU(implicit val conf: CPUConfig) extends Module {
  val io = IO(new CoreIO)

  // Bundles defining the pipeline registers and control structures
  class IFIDBundle extends Bundle {
    val instruction = UInt(32.W)
  }

  class EXControl extends Bundle {
  }

  class MControl extends Bundle {
  }

  class WBControl extends Bundle {
  }

  class IDEXBundle extends Bundle {
  }

  class EXMEMBundle extends Bundle {
  }

  class MEMWBBundle extends Bundle {
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
  val forwarding = Module(new ForwardingUnit())
  val hazard     = Module(new HazardUnit())
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

  // For wb back to other stages
  val write_data = Wire(UInt())

  /////////////////////////////////////////////////////////////////////////////
  // FETCH STAGE
  /////////////////////////////////////////////////////////////////////////////

  printf("pc=0x%x\n", pc)

  printf(p"IF/ID: $if_id\n")

  /////////////////////////////////////////////////////////////////////////////
  // ID STAGE
  /////////////////////////////////////////////////////////////////////////////

  printf("DASM(%x)\n", if_id.instruction)
  printf(p"ID/EX: $id_ex\n")

  /////////////////////////////////////////////////////////////////////////////
  // EX STAGE
  /////////////////////////////////////////////////////////////////////////////

  printf(p"EX/MEM: $ex_mem\n")

  /////////////////////////////////////////////////////////////////////////////
  // MEM STAGE
  /////////////////////////////////////////////////////////////////////////////

  printf(p"MEM/WB: $mem_wb\n")

  /////////////////////////////////////////////////////////////////////////////
  // WB STAGE
  /////////////////////////////////////////////////////////////////////////////

  printf("---------------------------------------------\n")
}
