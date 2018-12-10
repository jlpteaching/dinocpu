// Configurations for the CODCPU

package CODCPU


/**
 * This class configures all of the CODCPUs. It takes parameters for the type of CPU model
 * (e.g., single-cycle, five-cycle, etc.), and the memories to hook up.
 */
class CPUConfig
{
    /** If true, then the CPU should be elaborated in testing mode. */
    var testing = false

    /**
     * Set the CPU to testing mode.
     *
     * The CPU is elaborated in testing mode which adds some BoringUtils to expose
     * certain internal elementes for peek/poke.
     */
    def setTesting() = {
        testing = true
    }

    /** The type of CPU to elaborate */
    var cpuType = "single-cycle"

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
}
