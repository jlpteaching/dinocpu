// Configurations for the dinocpu

package dinocpu

// For file length
import java.io.{File, FileNotFoundException}

import scala.math.max

/**
 * This class configures all of the dinocpus. It takes parameters for the type of CPU model
 * (e.g., single-cycle, five-cycle, etc.), and the memories to hook up.
 */
class CPUConfig
{
  /** The type of CPU to elaborate */
  var cpuType = "single-cycle"

  /** The memory file location */
  var memFile = "test"

  /**
   * Returns the CPU that we will be elaborating
   *
   * @return A CPU to elaborate.
   */
  def getCPU() = {
    implicit val conf = this
    cpuType match {
      case "single-cycle" => new SingleCycleCPU
      case "five-cycle" => new FiveCycleCPU
      case "pipelined" => new PipelinedCPU
      case _ => throw new IllegalArgumentException("Must specify known CPU model")
    }
  }

  /**
   * Create a memory with data from a file
   *
   * @param minSize is the minimum size for the memory. If the binary file is
   *        smaller than this, create a memory that is this size.
   * @return [[dinocpu.DualPortedMemory]] object
   */
  def getMem(minSize: Int = 4096) = {
    val f = new File(memFile)
    if (f.length == 0) {
      println("WARNING: No file will be loaded for data memory")
    }
    new DualPortedMemory(max(f.length.toInt, minSize), memFile)
  }
}
