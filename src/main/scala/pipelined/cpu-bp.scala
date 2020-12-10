// This file is where all of the CPU components are assembled into the whole CPU
// This CPU has a branch predictor and a slightly different pipeline design from the base pipelined CPU

package dinocpu.pipelined

import chisel3._
import chisel3.util._
import dinocpu._
import dinocpu.components._

/**
 * The main CPU definition that hooks up all of the other components.
 *
 */
class PipelinedCPUBP(implicit val conf: CPUConfig) extends BaseCPU {

}
