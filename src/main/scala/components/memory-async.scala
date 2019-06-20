// Asynchronous memory module

package dinocpu

import chisel3._
import chisel3.util._
import chisel3.internal.firrtl.Width

import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

import MemoryOperation._

// A Bundle used for representing a memory access by instruction memory or data memory.
class Request extends Bundle {
  val address      = UInt(32.W)
  val writedata    = UInt(32.W)
  val operation    = UInt(1.W)
}

// A bundle used for representing the memory's response to a memory port.
class Response extends Bundle {
  // The 4-byte-wide block of data being returned by memory
  val data         = UInt(32.W)
}

/**
 * The generic interface for communication between the IMem/DMemPort modules and the backing memory.
 *
 * Input:  request, the ready/valid interface for a MemPort module to issue Requests to. Memory
 *         will only accept a request when both request.valid (the MemPort is supplying valid data)
 *         and request.ready (the memory is idling for a request) are high.
 *
 * Output: response, the valid interface for the data outputted by memory if it was requested to read.
 *         the bits in response.bits should only be treated as valid data when response.valid is high.
 */
class AsyncMemIO extends Bundle {
  val request  = Flipped(Decoupled (new Request))
  val response = Valid (new Response)
}

/**
 * The modified asynchronous form of the dual ported memory module.
 * When io.imem.request.valid or io.imem.request.valid is true and the memory is ready for an operation,
 * this memory module simulates the latency of real DRAM by pushing memory accesses into pipes that delay
 * the request for a configurable latency.
 *
 * As with the synced memory module, this memory should only be instantiated in the Top file,
 * and never within the actual CPU.
 *
 * The I/O for this module is defined in [[AsyncMemIO]].
 */
class DualPortedAsyncMemory(size: Int, memfile: String, latency: Int) extends Module {
  def wireMemPipe(portio: AsyncMemIO, pipe: Pipe[Request]): Unit = {
    pipe.io.enq.bits      <> DontCare
    pipe.io.enq.valid     := false.B
    portio.response.valid := false.B

    // Memory is technically always ready, but we want to use the
    // ready/valid interface so that if needed we can restrict
    // executing memory operations
    portio.request.ready := true.B
  }

  val io = IO(new Bundle {
    val imem = new AsyncMemIO
    val dmem = new AsyncMemIO
  })
  io <> DontCare

  assert(latency > 0) // Check for attempt to make combinational memory

  val memory    = Mem(math.ceil(size.toDouble/4).toInt, UInt(32.W))
  loadMemoryFromFile(memory, memfile)

  // Instruction port
  val imemPipe = Module(new Pipe(new Request, latency))

  wireMemPipe(io.imem, imemPipe)

  when (io.imem.request.valid) {
    // Put the Request into the instruction pipe and signal that instruction memory is busy
    val inRequest = io.imem.request.asTypeOf(new Request)
    imemPipe.io.enq.bits  := inRequest
    imemPipe.io.enq.valid := true.B
  } .otherwise {
    imemPipe.io.enq.valid := false.B
  }

  when (imemPipe.io.deq.valid) {
    // We should only be expecting a read from instruction memory
    assert(imemPipe.io.deq.bits.operation === Read)
    val outRequest = imemPipe.io.deq.asTypeOf (new Request)
    // Check that address is pointing to a valid location in memory
    assert (outRequest.address < size.U)
    io.imem.response.valid        := true.B
    io.imem.response.bits.data    := memory(outRequest.address >> 2)
  } .otherwise {
    // The memory's response can't possibly be valid if the imem pipe isn't outputting a valid request
    io.imem.response.valid := false.B
  }

  // Data port

  val dmemPipe     = Module(new Pipe(new Request, latency))

  wireMemPipe(io.dmem, dmemPipe)

  when (io.dmem.request.valid) {
    // Put the Request into the data pipe and signal that data memory is busy
    val inRequest = io.dmem.request.asTypeOf (new Request)
    dmemPipe.io.enq.bits  := inRequest
    dmemPipe.io.enq.valid := true.B
  } .otherwise {
    dmemPipe.io.enq.valid := false.B
  }

  when (dmemPipe.io.deq.valid) {
    assert (dmemPipe.io.deq.bits.operation =/= ReadWrite)
    // Dequeue request and execute
    val outRequest = dmemPipe.io.deq.asTypeOf (new Request)
    val address = outRequest.address >> 2
    // Check that address is pointing to a valid location in memory
    assert (outRequest.address < size.U)

    when (outRequest.operation === Read) {
      io.dmem.response.valid        := true.B
      io.dmem.response.bits.data    := memory(address)
    } .elsewhen (outRequest.operation === Write) {
      io.dmem.response.valid        := false.B
      memory(address) := outRequest.writedata
    }
  } .otherwise {
    // The memory's response can't possibly be valid if the dmem pipe isn't outputting a valid request
    io.dmem.response.valid := false.B
  }
}
