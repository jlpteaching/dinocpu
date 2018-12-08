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

object elaborate {
  def main(args: Array[String]): Unit = {
    require(args.length == 1, "Error: Expected exactly one argument: CPU type.")

    val conf = new CPUConfig()
    conf.cpuType = args(0)
    chisel3.Driver.execute(args, () => new Top(conf))
  }
}
