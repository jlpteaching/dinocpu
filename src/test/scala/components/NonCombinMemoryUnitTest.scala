// Unit tests for the async memory objects.

package dinocpu.test.memory

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import dinocpu.memory._

// To generate random latencies
import scala.util.Random

/** 
 * Testing harness for the non-combinational memory
 *
 * Bundles every I/O port of instruction and data memory into
 * a single interface so that the testers can poke all necessary inputs.
 *
 * Also serves as an example on how to set up the non-combinational memory in
 * an actual CPU by connecting the imem and dmem ports to the memory.
 
 * See: [[IMemPortIO]] and [[DMemPortIO]]
 */
class NonCombinMemoryTestHarness(size: Int, memFile: String, latency: Int) extends Module {
  val io = IO(new Bundle {
    val imem_address      = Input(UInt(64.W))
    val imem_valid        = Input(Bool())
    val imem_instruction  = Output(UInt(32.W))
    val imem_good         = Output(Bool())

    val dmem_address      = Input(UInt(64.W))
    val dmem_valid        = Input(Bool())
    val dmem_writedata    = Input(UInt(64.W))
    val dmem_memread      = Input(Bool())
    val dmem_memwrite     = Input(Bool())
    val dmem_maskmode     = Input(UInt(2.W))
    val dmem_sext         = Input(Bool())
    val dmem_readdata     = Output(UInt(64.W))
    val dmem_good         = Output(Bool())
  })
  io := DontCare

  val imem = Module(new INonCombinMemPort)

  val dmem = Module(new DNonCombinMemPort)
  val memory = Module(new DualPortedNonCombinMemory (size, memFile, latency))
  memory.io := DontCare


  imem.io.pipeline.address     := io.imem_address
  imem.io.pipeline.valid       := io.imem_valid
  io.imem_instruction := imem.io.pipeline.instruction
  io.imem_good        := imem.io.pipeline.good
  dmem.io.pipeline.address     := io.dmem_address
  dmem.io.pipeline.valid       := io.dmem_valid
  dmem.io.pipeline.writedata   := io.dmem_writedata
  dmem.io.pipeline.memread     := io.dmem_memread
  dmem.io.pipeline.memwrite    := io.dmem_memwrite
  dmem.io.pipeline.maskmode    := io.dmem_maskmode
  dmem.io.pipeline.sext        := io.dmem_sext
  io.dmem_readdata    := dmem.io.pipeline.readdata
  io.dmem_good        := dmem.io.pipeline.good

  memory.wireMemory (imem, dmem)
}

// Test instruction port reading a zeroed memory file
class AsyncMemoryUnitTester$IMemZero(m: NonCombinMemoryTestHarness, size: Int, latency: Int) extends PeekPokeTester(m) {
  expect(m.io.imem_good, 0)

  // Expect 0's on the instruction port
  for (i <- 0 until size/4) {
    poke(m.io.imem_address, i*4)
    poke(m.io.imem_valid, 1)

    step(1)
    poke(m.io.imem_valid, 0)
    if (latency > 1) {
      expect(m.io.imem_good, 0)
      step(latency - 1)
    }

    expect(m.io.imem_instruction, 0)
    expect(m.io.imem_good, 1)
  }
}

// Test instruction port reading an ascending memory file
class AsyncMemoryUnitTester$IMemRead(m: NonCombinMemoryTestHarness, size: Int, latency: Int) extends PeekPokeTester(m) {
  // Expect ascending bytes on instruction port
  expect(m.io.imem_good, 0)

  for (i <- 0 until size/4) {
    poke(m.io.imem_address, i*4)
    poke(m.io.imem_valid, 1)
    
    step(1)
    poke(m.io.imem_valid, 0)

    if (latency > 1) {
      expect(m.io.imem_good, 0)
      step(latency - 1)
    }

    expect(m.io.imem_instruction, i)
    expect(m.io.imem_good, 1)
  }
}
// Test data port writes and instruction port reads
class AsyncMemoryUnitTester$IMemWrite(m: NonCombinMemoryTestHarness, size: Int, latency: Int) extends PeekPokeTester(m) {
  expect(m.io.dmem_good, 0)
  // write ascending data to memory 
  for (i <- 0 until size/8) {
    poke(m.io.dmem_address, i*4)
    poke(m.io.dmem_valid, 1)
    poke(m.io.dmem_memwrite, 1)
    poke(m.io.dmem_maskmode, 2)
    poke(m.io.dmem_sext, 0)
    poke(m.io.dmem_writedata, i+100)

    step(1)
    poke(m.io.dmem_valid, 0)
    
    if (latency > 1) {
      expect(m.io.dmem_good, 0)
      step(latency - 1)
    }
    // We wait 1 extra cycle for the data memory to send the write back
    step(1)

    // Memory shouldn't be outputting high on good
    expect(m.io.dmem_good, 0)
  }

  poke (m.io.dmem_memwrite, 0)
  poke (m.io.dmem_valid, 0)
  
  // expect ascending bytes, the first size/8 of them being incremented by 100, on instruction port
  for (i <- 0 until size/4) {
    poke(m.io.imem_address, i*4)
    poke(m.io.imem_valid, 1)
    
    step(1)
    poke(m.io.imem_valid, 0)
    if (latency > 1) {
      expect(m.io.imem_good, 0)
      step(latency - 1)
    }
   
    if (i < size/8) {
      expect(m.io.imem_instruction, i+100)
    } else {
      expect(m.io.imem_instruction, i)
    }
    //expect(m.io.imem_instruction, -1)
    expect(m.io.imem_good, 1)
  }
}

// Test data port reading a zeroed memory file
class AsyncMemoryUnitTester$DMemZero(m: NonCombinMemoryTestHarness, size: Int, latency: Int) extends PeekPokeTester(m) {
  expect(m.io.dmem_good, 0)

  // Expect 0's on the data port
  for (i <- 0 until size/8) {
    poke(m.io.dmem_address, i*8)
    poke(m.io.dmem_valid, 1)
    poke(m.io.dmem_memread, 1)
    poke(m.io.dmem_maskmode, 3)
    poke(m.io.dmem_sext, 0)

    step(1)
    poke(m.io.dmem_valid, 0)
    if (latency > 1) {
      expect(m.io.dmem_good, 0)
      step(latency - 1)
    }  

    expect(m.io.dmem_readdata, 0)
    expect(m.io.dmem_good, 1)
  }
}

// Test data port reading an ascending memory file
class AsyncMemoryUnitTester$DMemRead(m: NonCombinMemoryTestHarness, size: Int, latency: Int) extends PeekPokeTester(m) {
  expect(m.io.dmem_good, 0)

  // Expect ascending bytes on data port
  for (i <- 0 until size/8) {
    poke(m.io.dmem_address, i*8)
    poke(m.io.dmem_valid, 1)
    poke(m.io.dmem_memread, 1)
    poke(m.io.dmem_maskmode, 3)
    poke(m.io.dmem_sext, 0)

    step(1)
    poke(m.io.dmem_valid, 0)
    if (latency > 1) {
      expect(m.io.dmem_good, 0)
      step(latency - 1)
    }
    
    expect(m.io.dmem_readdata, i)
    expect(m.io.dmem_good, 1)
  }
}

// Test data port writes and data port reads
class AsyncMemoryUnitTester$DMemWrite(m: NonCombinMemoryTestHarness, size: Int, latency: Int) extends PeekPokeTester(m) {
  expect(m.io.dmem_good, 0)
  // write ascending data to memory 
  for (i <- 0 until size/16) {
    poke(m.io.dmem_address, i*8)
    poke(m.io.dmem_valid, 1)
    poke(m.io.dmem_memwrite, 1)
    poke(m.io.dmem_maskmode, 3)
    poke(m.io.dmem_sext, 0)
    poke(m.io.dmem_writedata, i+100)

    step(1)
    poke(m.io.dmem_valid, 0)
    if (latency > 1) {
      expect(m.io.dmem_good, 0)
      step(latency - 1)
    }
    // We wait 1 extra cycle for the data memory to send the write back
    step(1)

    expect(m.io.dmem_good, 0)
  }

  poke (m.io.dmem_memwrite, 0)
  poke (m.io.dmem_valid, 0)

  // expect ascending bytes, the first size/16 of them being incremented by 100, on data port
  for (i <- 0 until size/8) {
    poke(m.io.dmem_address, i*8)
    poke(m.io.dmem_valid, 1)
    poke(m.io.dmem_memread, 1)
    poke(m.io.dmem_maskmode, 3)
    poke(m.io.dmem_sext, 0)

    step(1)
    poke(m.io.dmem_valid, 0)
    if (latency > 1) {
      expect(m.io.dmem_good, 0)
      step(latency - 1)
    }
  
    if (i < size/16) {
      expect(m.io.dmem_readdata, i+100)
    } else {
      expect(m.io.dmem_readdata, i)
    }
    expect(m.io.dmem_good, 1)
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
class NonCombinMemoryTester extends ChiselFlatSpec {
  val latency = new Random().nextInt (49) + 1

  // imem side
  "DualPortedNonCombinMemory" should s"have all zeros in instruction port (with treadle and $latency latency cycles)"  in {
    Driver(() => new NonCombinMemoryTestHarness(2048, "src/test/resources/raw/zero.hex", latency), "treadle") {
      m => new AsyncMemoryUnitTester$IMemZero(m, 2048, latency)
    } should be (true)
  }
  "DualPortedNonCombinMemory" should s"have increasing words in instruction port (with treadle and $latency latency cycles)" in {
    Driver(() => new NonCombinMemoryTestHarness(2048, "src/test/resources/raw/ascending-32.hex", latency), "treadle") {
      m => new AsyncMemoryUnitTester$IMemRead(m, 2048, latency)
    } should be (true)
  }
  "DualPortedNonCombinMemory" should s"store words with data port and load with instruction port (with treadle and $latency latency cycles)" in {
    Driver(() => new NonCombinMemoryTestHarness(2048, "src/test/resources/raw/ascending-32.hex", latency), "treadle") {
      m => new AsyncMemoryUnitTester$IMemWrite(m, 2048, latency)
    } should be (true)
  }
  
  // dmem side
  "DualPortedNonCombinMemory" should s"have all zeros in data port (with treadle and $latency latency cycles)"  in {
    Driver(() => new NonCombinMemoryTestHarness(4096, "src/test/resources/raw/zero.hex", latency), "treadle") {
      m => new AsyncMemoryUnitTester$DMemZero(m, 4096, latency)
    } should be (true)
  }

  "DualPortedNonCombinMemory" should s"have increasing double-words in data port (with treadle and $latency latency cycles)" in {
    Driver(() => new NonCombinMemoryTestHarness(4096, "src/test/resources/raw/ascending.hex", latency), "treadle") {
      m => new AsyncMemoryUnitTester$DMemRead(m, 4096, latency)
    } should be (true)
  }
  
  "DualPortedNonCombinMemory" should s"store words with data port and load with data port (with treadle and $latency latency cycles)" in {
    Driver(() => new NonCombinMemoryTestHarness(4096, "src/test/resources/raw/ascending.hex", latency), "treadle") {
      m => new AsyncMemoryUnitTester$DMemWrite(m, 4096, latency)
    } should be (true)
  }
 
}
