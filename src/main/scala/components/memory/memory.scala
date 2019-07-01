// The instruction and data memory modules

package dinocpu

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import dinocpu.MemoryOperation._

/**
  * This is the actual memory. You should never directly use this in the CPU.
  * This module should only be instantiated in the Top file.
  *
  * The I/O for this module is defined in [[MemPortBusIO]].
  */

class DualPortedCombinMemory(size: Int, memfile: String) extends BaseDualPortedMemory (size, memfile) {
  // Instruction port

  wireMemPipe(io.imem)

  when (io.imem.request.valid) {
    // Put the Request into the instruction pipe and signal that instruction memory is busy
    val request = io.imem.request.asTypeOf(new Request)

    // We should only be expecting a read from instruction memory
    assert(request.operation === Read)
    // Check that address is pointing to a valid location in memory
    assert (request.address < size.U)

    io.imem.response.valid        := true.B
    io.imem.response.bits.data    := memory(request.address >> 2)
  } .otherwise {
    io.imem.response.valid := false.B
  }

  // Data port

  wireMemPipe(io.dmem)

  val memAddress = Wire(UInt(32.W))
  val memWriteData = Wire(UInt(32.W))

  // Read path
  memAddress := io.dmem.request.bits.address
  io.dmem.response.bits.data := memory.read(memAddress >> 2)
  io.dmem.response.valid := true.B

  // Write path
  memWriteData := io.dmem.request.bits.writedata
  memory(memAddress >> 2) := memWriteData

  when (io.dmem.request.valid) {
    val request = io.dmem.request.asTypeOf (new Request)

    // Check that non-combin write isn't being used
    assert (request.operation =/= Write)
    // Check that address is pointing to a valid location in memory
    assert (request.address < size.U)
  } .otherwise {
    memAddress := DontCare
    io.dmem.response.bits.data := DontCare
    memWriteData := DontCare

    io.dmem.response.valid := false.B
  }
}















/** DEPRECATED MEMORY **/


/**
 * This is the *interface* to memory from the instruction side of the pipeline
 *
 * Input:  address, where to read the data from. This *must be* aligned to 4 bytes
 * Output: instruction that we will decode. The data from memory.
 */
class IMemIO extends Bundle {
  val address     = Input(UInt(32.W))

  val instruction = Output(UInt(32.W))
}

/**
 * This is the *interface* to the memory from the data side of the pipeline
 *
 * Input:  address, where to get the data from (does not have to be aligned even
 *                  though DualPortedMemory *is* aligned to 4 bytes)
 * Input:  writedata, data to write to the address
 * Input:  memread, true if we are reading from memory
 * Input:  memwrite, true if we are writing to memory
 * Input:  maskmode, mode to mask the result. 0 means byte, 1 means halfword, 2 means word
 * Input:  sext, true if we should sign extend the result
 *
 * Output: the data read and sign extended
 */
class DMemIO extends Bundle {
  val address   = Input(UInt(32.W))
  val writedata = Input(UInt(32.W))
  val memread   = Input(Bool())
  val memwrite  = Input(Bool())
  val maskmode  = Input(UInt(2.W))
  val sext      = Input(Bool())

  val readdata  = Output(UInt(32.W))
}

/**
 * This is the actual memory. You should never directly use this in the CPU.
 * This module should only be instantiated in the Top file.
 *
 * Note: this module is **deprecated**. See [[DualPortedCombinMemory]] for a functionally equivalent backing memory
 * which utilizes the newer memory module system.
 *
 * The I/O for this module is defined in [[IMemIO]] and [[DMemIO]].
 */
class DualPortedMemory(size: Int, memfile: String) extends Module {
  val io = IO(new Bundle {
    val imem = new IMemIO
    val dmem = new DMemIO
  })
  io := DontCare

  val memory = Mem(math.ceil(size.toDouble / 4).toInt, UInt(32.W))
  loadMemoryFromFile(memory, memfile)

  when(io.imem.address >= size.U) {
    io.imem.instruction := 0.U
  }.otherwise {
    io.imem.instruction := memory(io.imem.address >> 2)
  }

  when(io.dmem.memread) {
    assert(io.dmem.address < size.U)
    val readdata = Wire(UInt(32.W))

    when(io.dmem.maskmode =/= 2.U) { // When not loading a whole word
      val offset = io.dmem.address(1, 0)
      readdata := memory(io.dmem.address >> 2) >> (offset * 8.U)
      when(io.dmem.maskmode === 0.U) { // Reading a byte
        readdata := memory(io.dmem.address >> 2) & 0xff.U
      }.otherwise {
        readdata := memory(io.dmem.address >> 2) & 0xffff.U
      }
    }.otherwise {
      readdata := memory(io.dmem.address >> 2)
    }

    when(io.dmem.sext) {
      when(io.dmem.maskmode === 0.U) {
        io.dmem.readdata := Cat(Fill(24, readdata(7)), readdata(7, 0))
      }.elsewhen(io.dmem.maskmode === 1.U) {
        io.dmem.readdata := Cat(Fill(16, readdata(15)), readdata(15, 0))
      }.otherwise {
        io.dmem.readdata := readdata
      }
    }.otherwise {
      io.dmem.readdata := readdata
    }
  }

  when(io.dmem.memwrite) {
    assert(io.dmem.address < size.U)
    when(io.dmem.maskmode =/= 2.U) { // When not storing a whole word
      val offset = io.dmem.address(1, 0)
      // first read the data since we are only overwriting part of it
      val readdata = Wire(UInt(32.W))
      readdata := memory(io.dmem.address >> 2)
      // mask off the part we're writing
      val data = Wire(UInt(32.W))
      when(io.dmem.maskmode === 0.U) { // Reading a byte
        data := readdata & ~(0xff.U << (offset * 8.U))
      }.otherwise {
        data := readdata & ~(0xffff.U << (offset * 8.U))
      }
      memory(io.dmem.address >> 2) := data | (io.dmem.writedata << (offset * 8.U))
    }.otherwise {
      memory(io.dmem.address >> 2) := io.dmem.writedata
    }
  }
}

