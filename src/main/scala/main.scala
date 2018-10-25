// Main entry point for single cycle CPU
package CODCPU

import chisel3._

object elaborate {
  def main(args: Array[String]): Unit = {
    if (args(0) == "single-cycle") {
      chisel3.Driver.execute(args, () => new SingleCycleTop)
    } else if (args(0) == "multi-cycle") {
      chisel3.Driver.execute(args, () => new FiveCycleTop)
    } else {
      println("Error: Expected first argument to be CPU type.")
    }
  }
}
