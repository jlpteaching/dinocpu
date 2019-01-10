// Unit tests for the memory objects.

package dinocpu

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class MemoryUnitZeroTester(m: DualPortedMemory, size: Int) extends PeekPokeTester(m) {

  // Expect 0's on the instruction port and data port
  for (i <- 0 to size/4 - 1) {
    poke(m.io.dmem.address, i*4)
    poke(m.io.dmem.memread, 1)
    poke(m.io.dmem.maskmode, 2)
    poke(m.io.dmem.sext, 0)
    poke(m.io.imem.address, i*4)
    step(1)
    expect(m.io.imem.instruction, 0)
    expect(m.io.dmem.readdata, 0)
  }
}

class MemoryUnitReadTester(m: DualPortedMemory, size: Int) extends PeekPokeTester(m) {

  // Expect ascending bytes on instruction port and data port
  for (i <- 0 to size/4 - 1) {
    poke(m.io.dmem.address, i*4)
    poke(m.io.dmem.memread, 1)
    poke(m.io.dmem.maskmode, 2)
    poke(m.io.dmem.sext, 0)
    poke(m.io.imem.address, i*4)
    step(1)
    expect(m.io.imem.instruction, i)
    expect(m.io.dmem.readdata, i)
  }
}

class MemoryUnitWriteTester(m: DualPortedMemory, size: Int) extends PeekPokeTester(m) {

  // Expect ascending bytes on instruction port
  for (i <- 0 to size/4/2 - 1) {
    poke(m.io.dmem.address, i*4)
    poke(m.io.dmem.memwrite, 1)
    poke(m.io.dmem.maskmode, 2)
    poke(m.io.dmem.sext, 0)
    poke(m.io.dmem.writedata, i+100)
    step(1)
  }

  // Expect ascending bytes on instruction port and data port
  for (i <- 0 to size/4 - 1) {
    poke(m.io.dmem.address, i*4)
    poke(m.io.dmem.memread, 1)
    poke(m.io.dmem.maskmode, 2)
    poke(m.io.dmem.sext, 0)
    poke(m.io.imem.address, i*4)
    step(1)
    if (i < size/2) {
      expect(m.io.imem.instruction, i+100)
      expect(m.io.dmem.readdata, i+100)
    } else {
      expect(m.io.imem.instruction, i)
      expect(m.io.dmem.readdata, i)
    }
  }
}

/**
  * Tests the memory system using treadle, and (optionally) verilator.
  * The firrtl interpreter won't work with loading memory.
  *
  * From within sbt use:
  * {{{
  * testOnly dinocpu.MemoryTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.MemoryTester'
  * }}}
  */
class MemoryTester extends ChiselFlatSpec {
  "DualPortedMemory" should s"have all zeros (with treadle)" in {
    Driver(() => new DualPortedMemory(2048, "src/test/resources/raw/zero.hex"), "treadle") {
      m => new MemoryUnitZeroTester(m, 2048)
    } should be (true)
  }

  "DualPortedMemory" should s"have increasing words (with treadle)" in {
    Driver(() => new DualPortedMemory(2048, "src/test/resources/raw/ascending.hex"), "treadle") {
      m => new MemoryUnitReadTester(m, 2048)
    } should be (true)
  }

  "DualPortedMemory" should s"store and load words (with treadle)" in {
    Driver(() => new DualPortedMemory(2048, "src/test/resources/raw/ascending.hex"), "treadle") {
      m => new MemoryUnitReadTester(m, 2048)
    } should be (true)
  }

}
