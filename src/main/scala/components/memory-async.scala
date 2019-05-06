// Asynchronous memory module

package dinocpu

import chisel3._
import chisel3.util._

import chisel3.experimental.ChiselEnum

import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

// Chisel enumerator for memory operations.
object MemoryOperation extends ChiselEnum {
  val nop, read, write = Value
}
import MemoryOperation._

// A Bundle used for representing a memory access by instruction memory or data memory.
class Request extends Bundle {  
  val address   = UInt(32.W)
  val writedata = UInt(32.W) 
  val maskmode  = UInt(2.W)
  val sext      = Bool()
  val operation = MemoryOperation()
}

/** 
 * The generic interface for communication between the IMem/DMemAccess modules and the backing memory.
 *
 * Input:  access_in, the ready/valid interface for a MemAccess module to issue Requests to. Memory
 *         will only accept a request when both access_in.valid (the MemAccess is supplying valid data)
 *         and access_in.ready (the memory is idling for a request) are high.
 *
 * Output: mem_out, the valid interface for the data outputted by memory if it was requested to read. 
 *         the bits in mem_out.bits should only be treated as valid data when mem_out.valid is high.
 */
class AsyncMemIO extends Bundle {
  val access_in = Input(Decoupled (new Request))
  val mem_out   = Output(Valid (UInt (32.W)))
}

/** 
 * A generic ready/valid interface for MemAccess modules, whose IOs extend this.
 * Input:  address, the address of a piece of data in memory. 
 * Input:  valid, true when the address specified is valid
 * Input:  mem_in, the return route from memory to a memory access. This is primarily meant for connecting to 
 *         an AsyncMemIO's mem_out output, and should not be connected to anything else in any circumstance 
 *         (or things will possibly break)
 * 
 * Output: ready, true when the MemAccess is either idling or outputting a value, and is ready for a new 
 *         request
 */
class MemAccessIO extends Bundle {
  val address = Input(UInt(32.W))
  val valid   = Input(Bool())
  val mem_in  = Input(Valid(UInt(32.W))) // can be Flipped()

  val ready      = Output(Bool())
  val access_out = Output(Decoupled(new Request)) // can be Flipped()
}

/** 
 * The *interface* of the IMemAccess module.
 *
 * Input:  address, ready/valid interface for the address of an instruction in memory 
 * Input:  valid, true when the address specified is valid
 *
 * Output: instruction, valid interface for the requested instruction
 * Output: ready, true when data memory is idling and ready for a request
 */
class IMemAccessIO extends MemAccessIO {
  val instruction = Output(UInt(32.W))
}

/**
 * The *interface* of the DMemAccess module.
 *
 * Input:  address, the address of a piece of data in memory. 
 * Input:  writedata, valid interface for the data to write to the address
 * Input:  valid, true when the address (and writedata during a write) specified is valid
 * Input:  memread,   true if we are reading from memory
 * Input:  memwrite,  true if we are writing to memory
 * Input:  maskmode,  mode to mask the result. 0 means byte, 1 means halfword, 2 means word
 * Input:  sext,      true if we should sign extend the result
 *
 * Output: readdata, valid interface for the data read and sign extended
 * Output: ready, true when instruction memory is idling and ready for a request
 */
class DMemAccessIO extends MemAccessIO {
  val writedata = Input(UInt(32.W))
  val memread   = Input(Bool())
  val memwrite  = Input(Bool())
  val maskmode  = Input(UInt(2.W))
  val sext      = Input(Bool())

  val readdata  = Output(UInt(32.W))
}

/**
 * The instruction memory accessor.
 *
 * The I/O for this module is defined in [[IMemAccessIO]].
 */
class IMemAccess(memoryIMemIO: AsyncMemIO) extends Module {
  val io = IO (new IMemAccessIO)
  io := DontCare

  // Connect supplied IO bundle to this accessor
  io.mem_in              := memoryIMemIO.mem_out
  io.ready               := memoryIMemIO.access_in.ready
  memoryIMemIO.access_in := io.access_out
 
  // Per the ready/valid interface spec
  when (io.valid && io.ready) {
    val request = Wire(new Request())
    request := DontCare
    request.address   := io.address
    request.operation := read
    request.maskmode  := 2.U
    request.sext      := false.B

    io.access_out.bits  := request
    io.access_out.valid := true.B
  }

  // When the memory is outputting a valid instruction
  when (io.mem_in.valid) {
    io.instruction := io.mem_in.bits
  }
}

/**
 * The data memory accessor.
 *
 * The I/O for this module is defined in [[DMemAccessIOio.access_out.bits  := request
    io.access_out.valid := true.B]].
 */
class DMemAccess(memoryDMemIO: AsyncMemIO) extends Module {
  val io = IO (new DMemAccessIO)
  io := DontCare

  // Connect supplied IO bundle to this accessor
  io.mem_in              := memoryDMemIO.mem_out
  io.ready               := memoryDMemIO.access_in.ready
  memoryDMemIO.access_in := io.access_out
  
  // Per the ready/valid interface spec
  when (io.valid && io.ready && io.memread != io.memwrite) {
    val request = WireInit (new Request)
    request.address   := io.address

    when (io.memread) {
      request.operation := read
    } .otherwise {
      request.operation := write
    }

    request.maskmode := io.maskmode
    request.sext     := io.sext

    io.access_out.bits  := request
    io.access_out.valid := true.B
  }

  // Perform masking and sign extension on read data when memory is outputting it
  when (io.mem_in.valid && io.memread) {
    val readdata_mask      = Wire(UInt(32.W))
    val readdata_mask_sext = Wire(UInt(32.W))

    when (io.maskmode === 0.U) {
      readdata_mask := io.mem_in.bits & 0xff.U
    } .elsewhen (io.maskmode === 1.U) {
      readdata_mask := io.mem_in.bits & 0xffff.U
    } .otherwise {
      readdata_mask := io.mem_in.bits
    }

    when (io.sext) {
      when (io.maskmode === 0.U) {
        readdata_mask_sext := Cat(Fill(24, readdata_mask(7)),  readdata_mask(7, 0))
      } .elsewhen (io.maskmode === 1.U) {
        readdata_mask_sext := Cat(Fill(26, readdata_mask(15)), readdata_mask(15,0))
      } .otherwise {
        readdata_mask_sext := readdata_mask
      }
    } .otherwise {
      readdata_mask_sext := readdata_mask
    }

    io.readdata := readdata_mask_sext
  }
}


/**
 * The modified asynchronous form of the dual ported memory module. 
 * When io.imem.ready, io.dmem.memread, or io.dmem.memwrite is true, this memory module simulates the latency of 
 * real DRAM by pushing memory accesses into pipes that delay the request for a configurable latency.
 *
 * As with the synced memory module, this memory should only be instantiated in the Top file,
 * and never within the actual CPU.
 *
 * The I/O for this module is defined in [[AsyncMemIO]].
 */
class DualPortedAsyncMemory(size: Int, memfile: String, latency: Int) extends Module {
  val io = IO(new Bundle {
    val imem = new AsyncMemIO
    val dmem = new AsyncMemIO
  })

  io := DontCare

  val memory    = Mem(math.ceil(size.toDouble/4).toInt, UInt(32.W))
  loadMemoryFromFile(memory, memfile)

  val imemPipe  = Pipe(Valid(new Request), latency)
  val dmemPipe  = Pipe(Valid(new Request), latency)

}
