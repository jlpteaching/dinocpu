// Asynchronous memory module

package dinocpu

import chisel3._
import chisel3.util._
import chisel3.internal.firrtl.Width

import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

/**
 * Enumerator to assign names to the UInt constants representing memory operations
 */
object MemoryOperation {
  val Read = 0.U
  val Write = 1.U
}

import MemoryOperation._

// A Bundle used for representing a memory access by instruction memory or data memory.
class Request(val blockwidth: Int) extends Bundle {  
  val address      = UInt(32.W)
  val writedata    = UInt(blockwidth.W)
  val operation    = UInt(1.W)
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
class AsyncMemIO(val blockwidth: Int) extends Bundle {
  val request  = Flipped(Decoupled (new Request(blockwidth)))
  val response = Valid (UInt (blockwidth.W))
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
class DualPortedAsyncMemory(size: Int, memfile: String, latency: Int, val blockwidth: Int) extends Module {
  def wireMemPipe(portio: AsyncMemIO, pipe: Pipe[Request], busy: Bool): Unit = {      
    pipe.io.enq.bits  <> DontCare
    pipe.io.enq.valid := false.B

    portio.request.ready := !busy
  } 

  val io = IO(new Bundle {
    val imem = new AsyncMemIO(blockwidth)
    val dmem = new AsyncMemIO(blockwidth)
  })
  io <> DontCare

  assert(latency > 0) // Check for attempt to make combinational memory

  val memory    = Mem(math.ceil(size.toDouble/4).toInt, UInt(blockwidth.W))
  loadMemoryFromFile(memory, memfile)

  // Instruction port
  val imemPipe = Module(new Pipe(new Request(blockwidth), latency))
  val imemBusy = RegInit(false.B)

  wireMemPipe(io.imem, imemPipe, imemBusy)

  when (!imemBusy && io.imem.request.valid) {
    // Put the Request into the instruction pipe and signal that instruction memory is busy
    val inRequest = io.imem.request.asTypeOf(new Request(blockwidth))
    imemPipe.io.enq.bits  := inRequest
    imemPipe.io.enq.valid := true.B
    imemBusy := true.B
  }

  when (imemPipe.io.deq.valid) {
    assert(imemBusy)
    assert(imemPipe.io.deq.bits.operation === Read) 
    val outRequest = imemPipe.io.deq.asTypeOf (new Request(blockwidth))
    io.imem.response.valid := true.B
    io.imem.response.bits  := memory(outRequest.address >> 2)
    // Ignore instruction writes as there is no way imem can issue a write access to memory
    // Signal that instruction memory is idling as we're done
    
    imemBusy := false.B
  }

  // Data port

  val dmemPipe = Module(new Pipe(new Request(blockwidth), latency))
  val dmemBusy = RegInit(false.B)

  wireMemPipe(io.dmem, dmemPipe, dmemBusy)

  when (io.dmem.request.valid) {
    // Put the Request into the data pipe and signal that data memory is busy 
    val inRequest = io.dmem.request.asTypeOf (new Request(blockwidth))
    dmemPipe.io.enq.bits  := inRequest
    dmemPipe.io.enq.valid := true.B
      // Writes do not stall pipeline
    when (inRequest.operation =/= Write) { 
      dmemBusy := true.B
    }
  }

  when (dmemPipe.io.deq.valid) {
    // Dequeue request and execute
    val outRequest = dmemPipe.io.deq.asTypeOf (new Request(blockwidth))
    val address = outRequest.address >> 2
    // Check that address is pointing to a valid location in memory
    assert (outRequest.address < size.U)
    
    when (outRequest.operation === Read) {
      io.dmem.response.valid := true.B
      io.dmem.response.bits  := memory(address)
      dmemBusy := false.B
    } .elsewhen (outRequest.operation === Write) {  
      memory(address) := outRequest.writedata
    }
  }
}
