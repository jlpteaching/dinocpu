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
  val dmem  = Module(new DataMemory(1024, "/home/jlp/test1.txt"))
  val imem  = Module(new InstructionMemory(4096, "/home/jlp/test2.txt"))

  cpu.io.imem <> imem.io
  cpu.io.dmem <> dmem.io
}
