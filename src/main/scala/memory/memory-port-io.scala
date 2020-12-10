// Contains the memory port IOs for use in port/cache implementations

package dinocpu.memory

import chisel3._

/**
 * A generic ready/valid interface for MemPort modules, whose IOs extend this.
 *
 * This interface corresponds with the pipeline <=> port interface between the
 * pipelined CPU and the memory port.
 *
 * Input:  address, the address of a piece of data in memory.
 * Input:  valid, true when the address specified is valid
 * Output: good, true when memory is responding with a piece of data (used to un-stall the pipeline)
 *
 */
class MemPortIO extends Bundle {
  // Pipeline <=> Port
  val address  = Input(UInt(32.W))
  val valid    = Input(Bool())
  val good     = Output(Bool())
}

/**
 * The *interface* of the IMemPort module.
 *
 * Pipeline <=> Port:
 *   Input:  address, the address of an instruction in memory
 *   Input:  valid, true when the address specified is valid
 *   Output: instruction, the requested instruction
 *   Output: good, true when memory is responding with a piece of data
 *   Output: ready, true when the memory is ready to accept another request (used to un-stall the pipeline)
 */
class IMemPortIO extends MemPortIO {
  val instruction = Output(UInt(32.W))
  val ready       = Output(Bool())
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
  // Pipeline <=> Port
  val writedata = Input(UInt(32.W))
  val memread   = Input(Bool())
  val memwrite  = Input(Bool())
  val maskmode  = Input(UInt(2.W))
  val sext      = Input(Bool())

  val readdata  = Output(UInt(32.W))
}
