// Configurations for the dinocpu

package dinocpu

// For file length
import java.io.File
import dinocpu.components._
import dinocpu.memory._
import dinocpu.pipelined.PipelinedCPU
import dinocpu.pipelined.PipelinedCPUBP
import dinocpu.pipelined.PipelinedNonCombinCPU

/**
 * This class configures all of the dinocpus. It takes parameters for the type of CPU model
 * (e.g., single-cycle, five-cycle, etc.), and the memories to hook up.
 */
class CPUConfig
{
  /** The type of CPU to elaborate */
  var cpuType = "single-cycle"

  /** The type of branch predictor to use */
  var branchPredictor = "always-not-taken"
  /** Number of bits for the saturating counters */
  var saturatingCounterBits = 2
  /** Number of entries in the branch predictor table */
  var branchPredTableEntries = 32

  /** The memory file location */
  var memFile = "test"
  /** The noncombinational memory latency */
  var memLatency = 5
  /** The port types **/
  var memPortType = "combinational-port"
  /** The backing memory type */
  var memType = "combinational"

  def printConfig(): Unit = {
    println(s"CPU Type: ${cpuType}")
    println(s"Branch predictor: ${branchPredictor}")
    println(s"Memory file: ${memFile}")
    println(s"Memory type: ${memType}")
    println(s"Memory port type: ${memPortType}")
    println(s"Memory latency (ignored if combinational): ${memLatency}")
  }

  /**
   * Returns the CPU that we will be elaborating
   *
   * @return A CPU to elaborate.
   */
  def getCPU(): BaseCPU = {
    implicit val conf = this
    cpuType match {
      case "single-cycle" => new SingleCycleCPU
      case "pipelined" => new PipelinedCPU
      case "pipelined-bp" => new PipelinedCPUBP
      case "pipelined-non-combin" => new PipelinedNonCombinCPU
      case _ => throw new IllegalArgumentException("Must specify known CPU model")
    }
  }

  def getBranchPredictor: BaseBranchPredictor = {
    implicit val conf = this
    branchPredictor match {
      case "always-taken"     => new AlwaysTakenPredictor
      case "always-not-taken" => new AlwaysNotTakenPredictor
      case "local"            => new LocalPredictor
      case "global"           => new GlobalHistoryPredictor
      case _ => throw new IllegalArgumentException("Must specify known branch predictor")
    }
  }

  /**
    * Create a memory with data from a file
    *
    * @param minSize is the minimum size for the memory. If the binary file is
    *        smaller than this, create a memory that is this size.
    * @return [[BaseDualPortedMemory]] object
    */
  def getNewMem(minSize: Int = 1 << 16): BaseDualPortedMemory = {
    val f = new File(memFile)
    if (f.length == 0) {
      println("WARNING: No file will be loaded for data memory")
    }

    memType match {
      case "combinational"     => new DualPortedCombinMemory (minSize, memFile)
      case "non-combinational" => new DualPortedNonCombinMemory (minSize, memFile, memLatency)
      case _ => throw new IllegalArgumentException("Must specify known backing memory type")
    }
  }

  /**
    * Create an instruction memory port
    *
    * @return [[BaseIMemPort]] object
    */
  def getIMemPort(): BaseIMemPort = {
    val f = new File(memFile)
    if (f.length == 0) {
      println("WARNING: No file will be loaded for data memory")
    }

    memPortType match {
      case "combinational-port"     => new ICombinMemPort
      case "non-combinational-port" => new INonCombinMemPort
      // case "non-combinational-cache" => new ICache
      case _ => throw new IllegalArgumentException("Must specify known instruction memory port type")
    }
  }

  /**
    * Create a data memory port
    *
    * @return [[BaseDMemPort]] object
    */
  def getDMemPort(): BaseDMemPort = {
    val f = new File(memFile)
    if (f.length == 0) {
      println("WARNING: No file will be loaded for data memory")
    }

    memPortType match {
      case "combinational-port"     => new DCombinMemPort
      case "non-combinational-port" => new DNonCombinMemPort
      // case "non-combinational-cache" => new DCache
      case _ => throw new IllegalArgumentException("Must specify known data memory port type")
    }
  }
}
