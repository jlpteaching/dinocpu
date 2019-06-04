// Asynchronous memory module

package dinocpu

import chisel3._
import chisel3.util._
import chisel3.internal.firrtl.Width

import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

import MemoryOperation._

// A Bundle used for temporarily storing the necessary information for a  read/write in the data memory accessor.
class OutstandingReq extends Bundle {
  val address   = UInt(32.W)
  val writedata = UInt(32.W)
  val maskmode  = UInt(2.W)
  val operation = UInt(1.W)
  val sext      = Bool()
}

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

/**
 * The instruction memory port.
 *
 * The I/O for this module is defined in [[IMemPortIO]].
 */
class IMemPort extends Module {
  val io = IO (new IMemPortIO)
  io := DontCare

  // When the pipeline is supplying a high valid signal
  when (io.valid) {
    val request = Wire(new Request)
    request := DontCare
    request.address   := io.address
    request.operation := Read

    io.request.bits  := request
    io.request.valid := true.B
  } .otherwise {
    io.request.valid := false.B
  }

  // When the memory is outputting a valid instruction
  io.good := io.response.valid
  when (io.response.valid) {
    io.instruction := io.response.bits.data
  }
}

/**
 * The data memory port.
 *
 * The I/O for this module is defined in [[DMemPortIO]].
 */
class DMemPort extends Module {
  val io = IO (new DMemPortIO)
  io      := DontCare
  io.good := io.response.valid

  // A register to hold intermediate data (e.g., write data, mask mode) while the request
  // is outstanding to memory.
  val outstandingReq = RegInit(0.U.asTypeOf(Valid(new OutstandingReq)))

  // Used to set the valid bit of the outstanding request
  val sending = Wire(Bool())

  // When the pipeline is supplying a valid read OR write request, send out the request
  // ... on the condition that there isn't an outstanding request in the queue.
  // We need to process outstanding request first to guarantee atomicity of the memory write operation
  // Ready if either we don't have an outstanding request or the outstanding request is a read and
  // it has been satisfied this cycle. Note: we cannot send a read until one cycle after the write has
  // been sent.
  val ready = !outstandingReq.valid || (io.response.valid && (outstandingReq.valid && outstandingReq.bits.operation === Read))
  when (io.valid && (io.memread || io.memwrite) && ready) {
    // Check if we aren't issuing both a read and write at the same time
    assert (! (io.memread && io.memwrite))

    // On either a read or write we must read a whole block from memory. Store the necessary
    // information to redirect the memory's response back into itself through a write
    // operation and get the right subset of the block on a read.
    outstandingReq.bits.address   := io.address
    outstandingReq.bits.writedata := io.writedata
    outstandingReq.bits.maskmode  := io.maskmode
    outstandingReq.bits.sext      := io.sext
    when (io.memwrite) {
      outstandingReq.bits.operation := Write
    } .otherwise {
      outstandingReq.bits.operation := Read
    }
    sending := true.B

    // Program memory to perform a read. Always read since we must read before write.
    io.request.bits.address   := io.address
    io.request.bits.writedata := 0.U
    io.request.bits.operation := Read
    io.request.valid          := true.B
  } .otherwise {
    // no request coming in so don't send a request out
    io.request.valid := false.B
    sending := false.B
  }

  // Response path
  when (io.response.valid) {
    assert(outstandingReq.valid)
    when (outstandingReq.bits.operation === Write) {
      val writedata = Wire (UInt (32.W))

      // When not writing a whole word
      when (outstandingReq.bits.maskmode =/= 2.U) {
        // Read in the existing piece of data at the address, so we "overwrite" only part of it
        val offset = outstandingReq.bits.address (1, 0)
        val readdata = Wire (UInt (32.W))
        readdata := io.response.bits.data
        val data = Wire (UInt (32.W))
        // Mask the portion of the existing data so it can be or'd with the writedata
        when (outstandingReq.bits.maskmode === 0.U) {
          data := io.response.bits.data & ~(0xff.U << (offset * 8.U))
        } .otherwise {
          data := io.response.bits.data & ~(0xffff.U << (offset * 8.U))
        }
        writedata := data | (outstandingReq.bits.writedata << (offset * 8.U))
      } .otherwise {
        // Write the entire word
        writedata := outstandingReq.bits.writedata
      }

      // Program the memory to issue a write.
      val request = Wire (new Request)
      request.address   := outstandingReq.bits.address
      request.writedata := writedata
      request.operation := Write
      io.request.bits  := request
      io.request.valid := true.B
    } .otherwise {
      // Response is valid and we don't have a stored write.
      // Perform masking and sign extension on read data when memory is outputting it
      val readdata_mask      = Wire(UInt(32.W))
      val readdata_mask_sext = Wire(UInt(32.W))

      val offset = outstandingReq.bits.address(1,0)
      when (outstandingReq.bits.maskmode === 0.U) {
        // Byte
        readdata_mask := (io.response.bits.data >> (offset * 8.U)) & 0xff.U
      } .elsewhen (outstandingReq.bits.maskmode === 1.U) {
        // Half-word
        readdata_mask := (io.response.bits.data >> (offset * 8.U)) & 0xffff.U
      } .otherwise {
        readdata_mask := io.response.bits.data
      }

      when (outstandingReq.bits.sext) {
        when (outstandingReq.bits.maskmode === 0.U) {
          // Byte sign extension
          readdata_mask_sext := Cat(Fill(24, readdata_mask(7)),  readdata_mask(7, 0))
        } .elsewhen (outstandingReq.bits.maskmode === 1.U) {
          // Half-word sign extension
          readdata_mask_sext := Cat(Fill(16, readdata_mask(15)), readdata_mask(15, 0))
        } .otherwise {
          // Word sign extension (does nothing)
          readdata_mask_sext := readdata_mask
        }
      } .otherwise {
        readdata_mask_sext := readdata_mask
      }

      io.readdata := readdata_mask_sext
    }
    // Mark the outstanding request register as being invalid, unless sending
    outstandingReq.valid := sending
  } .otherwise {
    // Keep the outstanding request valid or invalid unless sending
    outstandingReq.valid := outstandingReq.valid | sending
  }
}
