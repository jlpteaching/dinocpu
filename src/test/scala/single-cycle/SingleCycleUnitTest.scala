// Unit tests for the register file.

package CODCPU

import chisel3._
import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

import chisel3.util.experimental.BoringUtils

import Common.{SimDTM, DMIIO, AsyncScratchPadMemory, DebugModule}

class CPUWrapper extends Module {
    // Expose the IO we want to use to test the CPU
    val io = IO(new Bundle {
      val dmi = Flipped(new DMIIO())

      val regnum = Input(UInt(5.W))
      val regdata = Output(UInt(32.W))

      val writedata = Input(UInt(32.W))
      val wen = Input(Bool())
    })
    io := DontCare

    implicit val conf = new CPUConfig()
    conf.setTesting()

    val debug = Module(new DebugModule())
    val cpu   = Module(new CPU)
    cpu.io := DontCare
    val memory = Module(new AsyncScratchPadMemory(num_core_ports = 2))
    cpu.io.dmem <> memory.io.core_ports(0)
    cpu.io.imem <> memory.io.core_ports(1)
    debug.io.debugmem <> memory.io.debug_port
    cpu.reset := debug.io.resetcore | reset.toBool
    debug.io.dmi <> io.dmi

    BoringUtils.addSource(io.regnum, "regNum")
    BoringUtils.addSink(io.regdata, "regData")

    BoringUtils.addSource(io.wen, "writeReg")
    BoringUtils.addSource(io.writedata, "writeRegData")
}

class CPUUnitTester(c: CPUWrapper) extends PeekPokeTester(c) {

  def readReg(num: Int, c: CPUWrapper) : BigInt = {
    poke(c.io.regnum, num)
    var value = peek(c.io.regdata)
    step(1)
    value
  }

  // Write some data to registers
  for (i <- 0 to 31) {
    poke(c.io.regnum, i)
    poke(c.io.writedata, i+100)
    poke(c.io.wen, true)
    step(1)
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
    Array("firrtl")
  }
  else {
    Array("firrtl")
  }
  for ( backendName <- backendNames ) {
    "CPU" should s"save written values (with $backendName)" in {
      Driver(() => new CPUWrapper, backendName) {
        c => new CPUUnitTester(c)
      } should be (true)
    }
  }
}
