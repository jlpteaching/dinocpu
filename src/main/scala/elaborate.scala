// Main entry point for elaboration
package dinocpu

/**
 * Simple object with only a main function to run the chisel elaboration.
 * When run, this will output Top.v, Top.fir, and other files.
 *
 * Running this takes a single argument, the CPU type to run. See [[dinocpu.CPUConfig]].
 */
object elaborate {
  def main(args: Array[String]): Unit = {
    require(args.length == 1, "Error: Expected exactly one argument: CPU type.")

    val conf = new CPUConfig()
    conf.cpuType = args(0)
    chisel3.Driver.execute(args, () => new Top(conf))
  }
}
