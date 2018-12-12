// Unit tests for the memory objects.

package CODCPU

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class MemoryUnitZeroTester(m: DualPortedMemory, size: Int) extends PeekPokeTester(m) {

    // Expect 0's on the instruction port
    for (i <- 0 to size/4 - 1) {
        poke(m.io.imem.address, i*4)
        step(1)
        expect(m.io.imem.instruction, 0)
    }
}

class MemoryUnitReadTester(m: DualPortedMemory, size: Int) extends PeekPokeTester(m) {

    // Expect ascending bytes on instruction port
    for (i <- 0 to size/4 - 1) {
        poke(m.io.imem.address, i*4)
        step(1)
        val d = peek(m.io.imem.instruction)
        println(s"got $d, expected $i")
        expect(m.io.imem.instruction, i)
    }
}

/**
  * Tests the memory system using treadle, and (optionally) verilator.
  * The firrtl interpreter won't work with loading memory.
  *
  * From within sbt use:
  * {{{
  * testOnly CODCPU.MemoryTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly CODCPU.MemoryTester'
  * }}}
  */
class MemoryTester extends ChiselFlatSpec {
  private val backendNames = if(firrtl.FileUtils.isCommandAvailable(Seq("verilator", "--version"))) {
    Array("treadle")
  }
  else {
    Array("treadle")
  }
  for ( backendName <- backendNames ) {
    "DualPortedMemory" should s"have all zeros (with $backendName)" in {
      Driver(() => new DualPortedMemory(2048, "src/test/resources/raw/zero.hex"), backendName) {
        m => new MemoryUnitZeroTester(m, 2048)
      } should be (true)
    }
  }
  for ( backendName <- backendNames ) {
    "DualPortedMemory" should s"have increasing bytes (with $backendName)" in {
      Driver(() => new DualPortedMemory(2048, "src/test/resources/raw/ascending.hex"), backendName) {
        m => new MemoryUnitReadTester(m, 2048)
      } should be (true)
    }
  }
}
