// Main entry point for single cycle CPU
package CODCPU

import chisel3._

import Common.{SimDTM, DMIIO, AsyncScratchPadMemory, DebugModule}


class Tile extends Module
{
   val io = IO(new Bundle {
      val dmi = Flipped(new DMIIO())
   })

   // notice that while the core is put into reset, the scratchpad needs to be
   // alive so that the Debug Module can load in the program.
   val debug = Module(new DebugModule())
   val cpu   = Module(new CPU)
   cpu.io := DontCare
   val memory = Module(new AsyncScratchPadMemory(num_core_ports = 2))
   cpu.io.dmem <> memory.io.core_ports(0)
   cpu.io.imem <> memory.io.core_ports(1)
   debug.io.debugmem <> memory.io.debug_port
   cpu.reset := debug.io.resetcore | reset.toBool
   debug.io.dmi <> io.dmi
}

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
    if (args(0) == "single-cycle") {
      chisel3.Driver.execute(args, () => new Top)
    } else if (args(0) == "multi-cycle") {
      chisel3.Driver.execute(args, () => new Top)
    } else {
      println("Error: Expected first argument to be CPU type.")
    }
  }
}
