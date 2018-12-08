// Configurations for the CODCPU

package CODCPU

class CPUConfig
{
    var testing = false

    def setTesting() = {
        testing = true
    }

    var cpuType = "single-cycle"

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
