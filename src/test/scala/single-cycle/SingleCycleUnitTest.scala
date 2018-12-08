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

  val cpu   = Module(new SingleCycleCPU)
  val dmem  = Module(new DataMemory(1024, "/home/jlp/test1.txt"))
  val imem  = Module(new InstructionMemory(4096, "/home/jlp/test2.txt"))

  cpu.io.imem <> imem.io
  cpu.io.dmem <> dmem.io

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

  for (i <- 0 to 31) {
    expect (readReg(i, c) == i+100, s"Error on register $i")
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
  private val backendNames = if(firrtl.FileUtils.isCommandAvailable(Seq("verilator", "--version"))) {
    Array("treadle")
  }
  else {
    Array("treadle")
  }
  for ( backendName <- backendNames ) {
    "CPU" should s"save written values (with $backendName)" in {
      Driver(() => new CPUWrapper, backendName) {
        c => new CPUUnitTester(c)
      } should be (true)
    }
  }
}
