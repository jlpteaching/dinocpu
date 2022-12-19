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
