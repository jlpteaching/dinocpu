// Main entry point for single cycle CPU
package CODCPU

import chisel3._

import Common.SimDTM

class Top extends Module
{
   val io = IO(new Bundle{
      val success = Output(Bool())
    })

   val tile = Module(new Tile)
   val dtm = Module(new SimDTM).connect(clock, reset.toBool, tile.io.dmi, io.success)
}

object elaborate {
  def main(args: Array[String]): Unit = {
    chisel3.Driver.execute(args, () => new Top)
  }
}
