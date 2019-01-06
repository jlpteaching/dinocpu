// Main entry point for single cycle CPU
package dinocpu

import chisel3._

class Top(val conf: CPUConfig) extends Module
{
  val io = IO(new Bundle{
    val success = Output(Bool())
  })

  io.success := DontCare

  val cpu   = Module(conf.getCPU())
  val mem  = Module(conf.getMem())

  cpu.io.imem <> mem.io.imem
  cpu.io.dmem <> mem.io.dmem
}
