// Main entry point for single cycle CPU
package CODCPU

import chisel3._

import Common.{SimDTM, DMIIO, AsyncScratchPadMemory, DebugModule}


class Tile(implicit val conf: CPUConfig) extends Module
{
   val io = IO(new Bundle {
      val dmi = Flipped(new DMIIO())
   })

   // notice that while the core is put into reset, the scratchpad needs to be
   // alive so that the Debug Module can load in the program.

   val debug = Module(new DebugModule())
   val cpu   = Module(conf.getCPU())
   cpu.io := DontCare
   val memory = Module(new AsyncScratchPadMemory(num_core_ports = 2))
   cpu.io.dmem <> memory.io.core_ports(0)
   cpu.io.imem <> memory.io.core_ports(1)
   debug.io.debugmem <> memory.io.debug_port
   cpu.reset := debug.io.resetcore | reset.toBool
   debug.io.dmi <> io.dmi
}

class Top(implicit val conf: CPUConfig) extends Module
{
  val io = IO(new Bundle{
      val success = Output(Bool())
    })

  val tile = Module(new Tile)
  val dtm = Module(new SimDTM).connect(clock, reset.toBool, tile.io.dmi, io.success)
}

object elaborate {
  def main(args: Array[String]): Unit = {
    require(args.length == 1, "Error: Expected exactly one argument: CPU type.")

    implicit val conf = new CPUConfig()
    conf.cpuType = args(0)
    chisel3.Driver.execute(args, () => new Top)
  }
}
