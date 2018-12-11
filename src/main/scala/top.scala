// Main entry point for single cycle CPU
package CODCPU

import chisel3._

class Top(val conf: CPUConfig) extends Module
{
  val io = IO(new Bundle{
    val success = Output(Bool())
  })

  io.success := DontCare

  val cpu   = Module(conf.getCPU())
  val dmem  = Module(conf.getDataMem())
  val imem  = Module(conf.getInstMem())

  cpu.io.imem <> imem.io
  cpu.io.dmem <> dmem.io
}
