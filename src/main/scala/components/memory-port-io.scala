// Contains the memory port IOs for use in port/cache implementations

package dinocpu

import chisel3._
import chisel3.util._

/** 
 * A generic ready/valid interface for MemPort modules, whose IOs extend this.
 * This interface is split into two parts: 
 *   - Pipeline <=> Port: the interface between the pipelined CPU and the memory port
 *   - Memory <=> Port:   the interface between the memory port and the backing memory
 *
 * Pipeline <=> Port:
 *   Input:  address, the address of a piece of data in memory. 
 *   Input:  valid, true when the address specified is valid
 *   Output: good, true when memory is responding with a piece of data (used to un-stall the pipeline)
 *
 *   
 * Port <=> Memory:
 *   Input:  response, the return route from memory to a memory port. This is primarily meant for connecting to 
 *           an AsyncMemIO's response output, and should not be connected to anything else in any circumstance 
 *           (or things will possibly break) 
 *   Output: request, a DecoupledIO that delivers a request from a memory port to memory. This is primarily
 *           meant for connecting to an AsynMemIO's request input, and should not be connected to anything else
 */
class MemPortIO extends Bundle {
  // Pipeline <=> Port
  val address  = Input(UInt(32.W))
  val valid    = Input(Bool())
  val good     = Output(Bool())
 
  // Port <=> Memory 
  val response = Flipped(Valid(new Response))
  val request  = Decoupled(new Request)
}

/** 
 * The *interface* of the IMemPort module.
 *
 * Pipeline <=> Port:
 *   Input:  address, the address of an instruction in memory 
 *   Input:  valid, true when the address specified is valid
 *   Output: instruction, the requested instruction
 *   Output: good, true when memory is responding with a piece of data
 */
class IMemPortIO extends MemPortIO {
  val instruction = Output(UInt(32.W))
}

/**
 * The *interface* of the DMemPort module.
 *
 * Pipeline <=> Port:
 *   Input:  address, the address of a piece of data in memory. 
 *   Input:  writedata, valid interface for the data to write to the address
 *   Input:  valid, true when the address (and writedata during a write) specified is valid
 *   Input:  memread, true if we are reading from memory
 *   Input:  memwrite, true if we are writing to memory
 *   Input:  maskmode, mode to mask the result. 0 means byte, 1 means halfword, 2 means word
 *   Input:  sext, true if we should sign extend the result
 *   Output: readdata, the data read and sign extended
 *   Output: good, true when memory is responding with a piece of data
 */
class DMemPortIO extends MemPortIO {
  val writedata = Input(UInt(32.W))
  val memread   = Input(Bool())
  val memwrite  = Input(Bool())
  val maskmode  = Input(UInt(2.W))
  val sext      = Input(Bool())

  val readdata  = Output(UInt(32.W))
}
