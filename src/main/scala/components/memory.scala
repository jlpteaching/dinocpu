// The instruction and data memory modules

package CODCPU

import chisel3._

import chisel3.util.experimental.loadMemoryFromFile

/**
 * Describe this
 */
class IMemIO extends Bundle {
  val address     = Input(UInt(32.W))

  val instruction = Output(UInt(32.W))
}

/**
 * Describe this
 */
class DMemIO extends Bundle {
  val address   = Input(UInt(32.W))
  val writedata = Input(UInt(32.W))
  val memread   = Input(Bool())
  val memwrite  = Input(Bool())

  val readdata  = Output(UInt(32.W))
}

/**
 * Contains the instructions.
 * @param size the size of the memory in bytes.
 * @param memory file to load data from
 *
 * Here we describe the I/O
 */
class InstructionMemory(size: Int, memfile: String) extends Module {
  val io = IO(new IMemIO)

  // Make a memory that is the size given (round up to nearest word)
  val memory = Mem(math.ceil(size.toDouble/4).toInt, UInt(32.W))
  loadMemoryFromFile(memory, memfile)

  assert(io.address < size.U, "Cannot access outside of memory bounds")
  assert(!(io.address & 3.U), "Cannot do unaligned accesses to memory")

  io.instruction := memory(io.address)
}

/**
 * Contains the data.
 * @param size the size of the memory in bytes.
 *
 * Here we describe the I/O
 */
class DataMemory(size: Int, memfile: String) extends Module {
  val io = IO(new DMemIO)
  io := DontCare

   // Make a memory that is the size
  val memory = Mem(size/4, UInt(32.W))
  loadMemoryFromFile(memory, memfile)

  assert(io.address < size.U, "Cannot access outside of memory bounds")
  assert(!(io.address & 3.U), "Cannot do unaligned accesses to memory")

  when (io.memread) {
    io.readdata := memory(io.address)
  }

  when (io.memwrite) {
    memory(io.address) := io.writedata
  }
}
