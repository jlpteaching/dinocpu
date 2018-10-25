// Main entry point for single cycle CPU
package CODCPU

import chisel3._

object elaborate {
  def main(args: Array[String]): Unit = {
    chisel3.Driver.execute(args, () => new SingleCycleTop)
  }
}
