// Configurations for the CODCPU

package CODCPU

// For file length
import java.io.{File,FileNotFoundException}
import scala.math.max

/**
 * This class configures all of the CODCPUs. It takes parameters for the type of CPU model
 * (e.g., single-cycle, five-cycle, etc.), and the memories to hook up.
 */
class CPUConfig
{
    /** If true, then the CPU should be elaborated in testing mode. */
    var testing = false

    /** The type of CPU to elaborate */
    var cpuType = "single-cycle"

    /** The instruction memory file location */
    var instMemFile = "test"

    /** The data memory file location */
    var dataMemFile = "test"

    /**
     * Set the CPU to testing mode.
     *
     * The CPU is elaborated in testing mode which adds some BoringUtils to expose
     * certain internal elementes for peek/poke.
     */
    def setTesting() = {
        testing = true
    }

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
     * Create an instruction memory with data from a file.
     * Note: If the file doesn't exist, then no file will be loaded.
     *
     * @param filename to load the instruction memory with. Should be a binary file.
     * @return [[CODCPU.InstructionMemory]] object
     */
    def getInstMem() = {
        val f = new File(instMemFile)
        if (f.length == 0) {
            println("WARNING: No file will be loaded for instruction memory")
        }
        new InstructionMemory(max(f.length.toInt, 1024), instMemFile)
    }

    /**
     * Create an data memory with data from a file
     *
     * @param filename to load the data memory with. Should be a binary file.
     * @param minSize is the minimum size for the memory. If the binary file is
     *        smaller than this, create a memory that is this size.
     * @return [[CODCPU.DataMemory]] object
     */
    def getDataMem(minSize: Int = 4096) = {
        val f = new File(dataMemFile)
        if (f.length == 0) {
            println("WARNING: No file will be loaded for data memory")
        }
        new DataMemory(max(f.length.toInt, minSize), dataMemFile)
    }
}
