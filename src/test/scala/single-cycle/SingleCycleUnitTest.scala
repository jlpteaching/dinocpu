// Unit tests for the register file.

package CODCPU

import chisel3._
import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

import chisel3.util.experimental.BoringUtils

class CPUWrapper extends Module {
  // Expose the IO we want to use to test the CPU
  val io = IO(new Bundle {
    val regnum = Input(UInt(5.W))
    val regdata = Output(UInt(32.W))

    val writedata = Input(UInt(32.W))
    val wen = Input(Bool())
  })
  io := DontCare

  implicit val conf = new CPUConfig()
  conf.setTesting() // Required for BoringUtils in reg file
  conf.memFile = "src/test/resources/risc-v/lw1.raw"

  val cpu   = Module(new SingleCycleCPU)
  val mem   = Module(conf.getMem())

  cpu.io.imem <> mem.io.imem
  cpu.io.dmem <> mem.io.dmem

  BoringUtils.addSource(io.regnum, "regNum")
  BoringUtils.addSink(io.regdata, "regData")

  BoringUtils.addSource(io.wen, "writeReg")
  BoringUtils.addSource(io.writedata, "writeRegData")
}

class CPUUnitTester(c: CPUWrapper) extends PeekPokeTester(c) {

  def readReg(num: Int, c: CPUWrapper) : BigInt = {
    poke(c.io.regnum, num)
    var value = peek(c.io.regdata)
    value
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly CODCPU.CPUTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly CODCPU.CPUTester'
  * }}}
  */
class CPUTester extends ChiselFlatSpec {
  "CPU" should "save written values" in {
    Driver(() => new CPUWrapper, "firrtl") {
      c => new CPUUnitTester(c)
    } should be (true)
  }
}
