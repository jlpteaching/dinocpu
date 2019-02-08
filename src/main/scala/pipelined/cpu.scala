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
  }

  // Control signals used in MEM stage
  class MControl extends Bundle {

  }

  // Control signals used in EB stage
  class WBControl extends Bundle {

  }

  // Everything in the register between ID and EX stages
  class IDEXBundle extends Bundle {

    val excontrol = new EXControl
    val mcontrol  = new MControl
    val wbcontrol = new WBControl
  }

  // Everything in the register between ID and EX stages
  class EXMEMBundle extends Bundle {

    val mcontrol  = new MControl
    val wbcontrol = new WBControl
  }

  // Everything in the register between ID and EX stages
  class MEMWBBundle extends Bundle {

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
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  val if_id      = RegInit(0.U.asTypeOf(new IFIDBundle))
  val id_ex      = RegInit(0.U.asTypeOf(new IDEXBundle))
  val ex_mem     = RegInit(0.U.asTypeOf(new EXMEMBundle))
  val mem_wb     = RegInit(0.U.asTypeOf(new MEMWBBundle))

  // Remove these as you hook up each one
  control.io    := DontCare
  branchCtrl.io := DontCare
  registers.io := DontCare
  aluControl.io := DontCare
  alu.io := DontCare
  immGen.io := DontCare
  pcPlusFour.io := DontCare
  branchAdd.io := DontCare
  io.dmem := DontCare
  forwarding.io := DontCare
  hazard.io := DontCare

  printf("Cycle=%d ", cycleCount)

  // Forward declaration of wires that connect different stages

  // From memory back to fetch. Since we don't decide whether to take a branch or not until the memory stage.
  val next_pc = Wire(UInt(32.W))
  next_pc    := DontCare    // remove when connected

  // For wb back to other stages
  val write_data = Wire(UInt(32.W))
  write_data    := DontCare // remove when connected

  /////////////////////////////////////////////////////////////////////////////
  // FETCH STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Note: This comes from the memory stage!
  // Only update the pc if the pcwrite flag is enabled
  pc := pcPlusFour.io.result

  // Send the PC to the instruction memory port to get the instruction
  io.imem.address := pc

  // Get the PC + 4
  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  // Fill the IF/ID register if we are not bubbling IF/ID
  // otherwise, leave the IF/ID register *unchanged*
  if_id.instruction := io.imem.instruction
  if_id.pc          := pc
  if_id.pcplusfour  := pcPlusFour.io.result

  printf(p"IF/ID: $if_id\n")

  /////////////////////////////////////////////////////////////////////////////
  // ID STAGE
  /////////////////////////////////////////////////////////////////////////////

  val rs1 = if_id.instruction(19,15)
  val rs2 = if_id.instruction(24,20)

  // Send input from this stage to hazard detection unit

  // Send opcode to control
  control.io.opcode := if_id.instruction(6,0)

  // Send register numbers to the register file
  registers.io.readreg1 := rs1
  registers.io.readreg2 := rs2

  // Send the instruction to the immediate generator
  immGen.io.instruction := if_id.instruction

  // FIll the id_ex register

  // Set the execution control signals

  // Set the memory control signals

  // Set the writeback control signals

  printf("DASM(%x)\n", if_id.instruction)
  printf(p"ID/EX: $id_ex\n")

  /////////////////////////////////////////////////////////////////////////////
  // EX STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the inputs to the hazard detection unit from this stage (SKIP FOR PART I)

  // Set the input to the forwarding unit from this stage (SKIP FOR PART I)

  // Connect the ALU control wires (line 45 of single-cycle/cpu.scala)

  // Insert the forward inputx mux here (SKIP FOR PART I)

  // Insert the ALU inpux mux here (line 59 of single-cycle/cpu.scala)

  // Insert forward inputy mux here (SKIP FOR PART I)

  // Input y mux (line 66 of single-cycle/cpu.scala)

  // Connect the branch control wire (line 54 of single-cycle/cpu.scala)

  // Set the ALU operation
  alu.io.operation := aluControl.io.operation

  // Connect the branchAdd unit

  // Set the EX/MEM register values

  // Calculate whether which PC we should use and set the taken flag (line 92 in single-cycle/cpu.scala)


  printf(p"EX/MEM: $ex_mem\n")

  /////////////////////////////////////////////////////////////////////////////
  // MEM STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set data memory IO (line 71 of single-cycle/cpu.scala)

  // Send next_pc back to the fetch stage

  // Send input signals to the hazard detection unit (SKIP FOR PART I)

  // Send input signals to the forwarding unit (SKIP FOR PART I)

  // Wire the MEM/WB register

  printf(p"MEM/WB: $mem_wb\n")

  /////////////////////////////////////////////////////////////////////////////
  // WB STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the writeback data mux (line 78 single-cycle/cpu.scala)

  // Write the data to the register file

  // Set the input signals for the forwarding unit (SKIP FOR PART I)

  printf("---------------------------------------------\n")
}
