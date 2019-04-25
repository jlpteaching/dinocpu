// Unit tests for the async memory objects.

package dinocpu

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

// To generate random latencies
import scala.util.Random


// Test instruction port reading a zeroed memory file
class AsyncMemoryUnitTester$IMemZero(m: DualPortedAsyncMemory, size: Int, latency: Int) extends PeekPokeTester(m) {
  // Expect 0's on the instruction port
  for (i <- 0 to size/4 - 1) {
    poke(m.io.imem.address, i*4)
    poke(m.io.imem.ready, 1)

    step(latency)
    
    expect(m.io.imem.valid, 1)
    expect(m.io.imem.instruction, 0)
  }
}

// Test instruction port reading an ascending memory file
class AsyncMemoryUnitTester$IMemRead(m: DualPortedAsyncMemory, size: Int, latency: Int) extends PeekPokeTester(m) {
  // Expect ascending bytes on instruction port
  for (i <- 0 to size/4 - 1) {
    poke(m.io.imem.address, i*4)
    poke(m.io.imem.ready, 1)
    
    step(latency)
    
    expect(m.io.imem.valid, 1)
    expect(m.io.imem.instruction, i)
  }
}

// Test data port writes and instruction port reads
class AsyncMemoryUnitTester$IMemWrite(m: DualPortedAsyncMemory, size: Int, latency: Int) extends PeekPokeTester(m) {
  // write ascending data to memory 
  for (i <- 0 to size/8 - 1) {
    poke(m.io.dmem.address, i*4)
    poke(m.io.dmem.memwrite, 1)
    poke(m.io.dmem.maskmode, 2)
    poke(m.io.dmem.sext, 0)
    poke(m.io.dmem.writedata, i+100)

    step(latency)

    expect(m.io.dmem.valid, 1)
  }

  poke (m.io.dmem.memwrite, 0)

  // expect ascending bytes on instruction port
  for (i <- 0 to size/4 - 1) {
    poke(m.io.imem.address, i*4)
    poke(m.io.imem.ready, 1)
    
    step(latency + 1)
   
    expect(m.io.imem.valid, 1)
    if (i < size/8) {
      expect(m.io.imem.instruction, i+100)
    } else {
      expect(m.io.imem.instruction, i)
    }
  }
}

// Test data port reading a zeroed memory file
class AsyncMemoryUnitTester$DMemZero(m: DualPortedAsyncMemory, size: Int, latency: Int) extends PeekPokeTester(m) {
  // Expect 0's on the data port
  for (i <- 0 to size/4 - 1) {
    poke(m.io.dmem.address, i*4)
    poke(m.io.dmem.memread, 1)
    poke(m.io.dmem.maskmode, 2)
    poke(m.io.dmem.sext, 0)

    step(latency)
    
    expect(m.io.dmem.valid, 1)
    expect(m.io.dmem.readdata, 0)
  }
}

// Test data port reading an ascending memory file
class AsyncMemoryUnitTester$DMemRead(m: DualPortedAsyncMemory, size: Int, latency: Int) extends PeekPokeTester(m) {
  // Expect ascending bytes on data port
  for (i <- 0 to size/4 - 1) {
    poke(m.io.dmem.address, i*4)
    poke(m.io.dmem.memread, 1)
    poke(m.io.dmem.maskmode, 2)
    poke(m.io.dmem.sext, 0)
    
    step(latency)
    
    expect(m.io.dmem.valid, 1)
    expect(m.io.dmem.readdata, i)
  }
}

// Test data port writes and data port reads
class AsyncMemoryUnitTester$DMemWrite(m: DualPortedAsyncMemory, size: Int, latency: Int) extends PeekPokeTester(m) {
  //  Write ascending data to memory through data port
  for (i <- 0 to size/8 - 1) {
    poke(m.io.dmem.address, i*4)
    poke(m.io.dmem.memwrite, 1)
    poke(m.io.dmem.maskmode, 2)
    poke(m.io.dmem.sext, 0)
    poke(m.io.dmem.writedata, i+100)
    
    step(latency)

    expect(m.io.dmem.valid, 1)
  }

  poke (m.io.dmem.memwrite, 0)

  // Expect ascending bytes on data port
  for (i <- 0 to size/4 - 1) {
    poke(m.io.dmem.address, i*4)
    poke(m.io.dmem.memread, 1)
    poke(m.io.dmem.maskmode, 2)
    poke(m.io.dmem.sext, 0)
    
    step(latency)
   
    expect(m.io.dmem.valid, 1)
    if (i < size/8) {
      expect(m.io.dmem.readdata, i+100)
    } else {
      expect(m.io.dmem.readdata, i)
    }
  }
}

/**
  * Tests the async memory system using treadle, and (optionally) verilator.
  * The firrtl interpreter won't work with loading memory.
  *
  * From within sbt use:
  * {{{
  * testOnly dinocpu.AsyncMemoryTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.AsyncMemoryTester'
  * }}}
  */
class AsyncMemoryTester extends ChiselFlatSpec {
  val latency = new Random().nextInt (49) + 1

  // imem side
  "DualPortedAsyncMemory" should s"have all zeros in instruction port (with treadle and $latency latency cycles)"  in {
    Driver(() => new DualPortedAsyncMemory(2048, "src/test/resources/raw/zero.hex", latency), "treadle") {
      m => new AsyncMemoryUnitTester$IMemZero(m, 2048, latency)
    } should be (true)
  }

  "DualPortedAsync" should s"have increasing words in instruction port (with treadle and $latency latency cycles)" in {
    Driver(() => new DualPortedAsyncMemory(2048, "src/test/resources/raw/ascending.hex", latency), "treadle") {
      m => new AsyncMemoryUnitTester$IMemRead(m, 2048, latency)
    } should be (true)
  }

  "DualPortedAsyncMemory" should s"store words with data port and load with instruction port (with treadle and $latency latency cycles)" in {
    Driver(() => new DualPortedAsyncMemory(2048, "src/test/resources/raw/ascending.hex", latency), "treadle") {
      m => new AsyncMemoryUnitTester$IMemWrite(m, 2048, latency)
    } should be (true)
  }


  // dmem side
  "DualPortedAsyncMemory" should s"have all zeros in data port (with treadle and $latency latency cycles)"  in {
    Driver(() => new DualPortedAsyncMemory(2048, "src/test/resources/raw/zero.hex", latency), "treadle") {
      m => new AsyncMemoryUnitTester$DMemZero(m, 2048, latency)
    } should be (true)
  }

  "DualPortedAsyncMemory" should s"have increasing words in data port (with treadle and $latency latency cycles)" in {
    Driver(() => new DualPortedAsyncMemory(2048, "src/test/resources/raw/ascending.hex", latency), "treadle") {
      m => new AsyncMemoryUnitTester$DMemRead(m, 2048, latency)
    } should be (true)
  }

  "DualPortedAsyncMemory" should s"store words with data port and load with data port (with treadle and $latency latency cycles)" in {
    Driver(() => new DualPortedAsyncMemory(2048, "src/test/resources/raw/ascending.hex", latency), "treadle") {
      m => new AsyncMemoryUnitTester$DMemWrite(m, 2048, latency)
    } should be (true)
  }
}
