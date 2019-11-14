// IO Bundles for the bus that connect the memory ports to the backing memory.

package dinocpu.memory

import chisel3._
import chisel3.util._

// A Bundle used for representing a memory access by instruction memory or data memory.
class Request extends Bundle {
  val address      = UInt(32.W)
  val writedata    = UInt(32.W)
  val operation    = MemoryOperation()
}

// A bundle used for representing the memory's response to a memory read operation, which
// is sent back to the issuing memory port.
class Response extends Bundle {
  // The 4-byte-wide block of data being returned by memory
  val data         = UInt(32.W)
}

/**
 * The generic interface for communication between the IMem/DMemPort modules and the backing memory.
 * This interface corresponds with the port <=> memory interface between the
 * memory port and the backing memory.
 *
 * Input:  request, the ready/valid interface for a MemPort module to issue Requests to. Memory
 *         will only accept a request when both request.valid (the MemPort is supplying valid data)
 *         and request.ready (the memory is idling for a request) are high.
 *
 * Output: response, the valid interface for the data outputted by memory if it was requested to read.
 *         the bits in response.bits should only be treated as valid data when response.valid is high.
 */
class MemPortBusIO extends Bundle {
  val request  = Flipped(Decoupled (new Request))
  val response = Valid (new Response)
}