// Asynchronous memory module

package dinocpu

import chisel3._
import chisel3.util._
import chisel3.internal.firrtl.Width

import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

import MemoryOperation._

// A Bundle used for temporarily storing the necessary information for a write in the data memory accessor.
class PartialWrite(val blockwidth: Int) extends Bundle {
  val address   = UInt(32.W)
  val writedata = UInt(blockwidth.W)
  val maskmode  = UInt(2.W)
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
 *   Output: ready, true when memory is either idling or outputting a value, and is ready for a new 
 *           request. Note that this is different from access_out.ready - this ready is for use by the general
 *           CPU (like to signal when to stall the CPU), while access_out.ready is used for signalling 
 *           between the memory and accessor only
 *
 * Port <=> Memory:
 *   Input:  response, the return route from memory to a memory port. This is primarily meant for connecting to 
 *           an AsyncMemIO's response output, and should not be connected to anything else in any circumstance 
 *           (or things will possibly break) 
 *   Output: request, a DecoupledIO that delivers a request from a memory port to memory. This is primarily
 *           meant for connecting to an AsynMemIO's request input, and should not be connected to anything else
 */
class MemPortIO(val blockwidth: Int) extends Bundle {
  // Pipeline <=> Port
  val address  = Input(UInt(32.W))
  val valid    = Input(Bool())
  val ready    = Output(Bool())

  // Port <=> Memory 
  val response = Flipped(Valid(UInt(blockwidth.W)))
  val request  = Decoupled(new Request(blockwidth))
}

/** 
 * The *interface* of the IMemPort module.
 *
 * Pipeline <=> Port:
 *   Input:  address, the address of an instruction in memory 
 *   Input:  valid, true when the address specified is valid
 *   Output: instruction, the requested instruction
 *   Output: ready, true when memory is idling and ready for a request
 */
class IMemPortIO(override val blockwidth: Int) extends MemPortIO(blockwidth) {
  val instruction = Output(UInt(blockwidth.W))
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
 *   Output: ready, true when memory is idling and ready for a request
 */
class DMemPortIO(override val blockwidth: Int) extends MemPortIO(blockwidth) {
  val writedata = Input(UInt(blockwidth.W))
  val memread   = Input(Bool())
  val memwrite  = Input(Bool())
  val maskmode  = Input(UInt(2.W))
  val sext      = Input(Bool())

  val readdata  = Output(UInt(blockwidth.W))
}

/**
 * The instruction memory port.
 *
 * The I/O for this module is defined in [[IMemPortIO]].
 */
class IMemPort(val blockwidth: Int) extends Module {
  val io = IO (new IMemPortIO(blockwidth))
  io := DontCare
  io.request.valid  := false.B
  io.ready          := io.request.ready

  // When the backing memory is ready and the pipeline is supplying a high valid signal
  when (io.valid && io.request.ready) {
    val request = Wire(new Request(blockwidth))
    request := DontCare
    request.address      := io.address
    request.operation    := Read

    io.request.bits  := request
    io.request.valid := true.B
  } .otherwise {
    io.request.valid := false.B
  }

  // When the memory is outputting a valid instruction
  when (io.response.valid) {
    io.instruction := io.response.bits
  }
}

/**
 * The data memory port.
 *
 * The I/O for this module is defined in [[DMemPortIO]].
 */
class DMemPort(val blockwidth: Int) extends Module {
  val io = IO (new DMemPortIO(blockwidth))
  io := DontCare
  io.request.valid  := false.B

  val storedWrite = RegInit(0.U.asTypeOf(Valid(new PartialWrite(blockwidth))))
  val memReallyReady = io.request.ready && !storedWrite.valid
  io.ready := memReallyReady

  // When the backing memory is ready and the pipeline is supplying a valid read OR write request, send out the request
  // ... on the condition that there isn't a stored write in the queue.
  // We need to process stored writes first to guarantee atomicity of the memory write operation

  when (io.valid && memReallyReady && io.memread =/= io.memwrite) {
    when (io.memwrite) {
      storedWrite.bits.address   := io.address
      storedWrite.bits.writedata := io.writedata
      storedWrite.bits.maskmode  := io.maskmode
      storedWrite.valid := true.B
    }
    
    io.request.bits.address   := io.address
    io.request.bits.writedata := 0.U(blockwidth.W)
    io.request.bits.operation := Read
    io.request.valid          := true.B
  } .otherwise {
    io.request.valid := false.B
  }

  // When memory is outputting data we need to determine whether it's to write back to memory or for simply
  // reading
  // This can be deduced from whether storedWrite is valid and the memory is signalling if it is ready.
  when (io.response.valid) {
    when (storedWrite.valid && io.request.ready) {
      val writedata = Wire (UInt (blockwidth.W))

      // When not writing a whole word
      when (storedWrite.bits.maskmode =/= 2.U) {
        // Read in the existing piece of data at the address, so we "overwrite" only part of it
        val offset = storedWrite.bits.address (1, 0)
        val readdata = Wire (UInt (blockwidth.W))
        readdata := io.response.bits
        val data = Wire (UInt (blockwidth.W))
        // Mask the portion of the existing data so it can be or'd with the writedata
        when (storedWrite.bits.maskmode === 0.U) {
          data := io.response.bits & ~(0xff.U << (offset * 8.U))
        } .otherwise {
          data := io.response.bits & ~(0xffff.U << (offset * 8.U))
        }
        writedata := data | (storedWrite.bits.writedata << (offset * 8.U))
      } .otherwise {
        // Write the entire word
        writedata := storedWrite.bits.writedata 
      }

      // Program the memory to issue a write
      val request = Wire (new Request (blockwidth))
      request.address   := storedWrite.bits.address
      request.writedata := writedata
      request.operation := Write
      io.request.bits  := request
      io.request.valid := true.B

      // Mark the stored write register as being invalid.
      storedWrite.valid := false.B
    } .otherwise {
      // Perform masking and sign extension on read data when memory is outputting it
      val readdata_mask      = Wire(UInt(blockwidth.W))
      val readdata_mask_sext = Wire(UInt(blockwidth.W))

      when (io.maskmode === 0.U) {
        // Byte
        readdata_mask := io.response.bits & 0xff.U
      } .elsewhen (io.maskmode === 1.U) {
        // Half-word
        // Generate the mask corresponding with a half word
        val halfword_mask = Cat(Fill(blockwidth / 2, 0.U), Fill(blockwidth / 2, 1.U))
        readdata_mask := io.response.bits & halfword_mask
      } .otherwise {
        readdata_mask := io.response.bits
      }

      when (io.sext) {
        when (io.maskmode === 0.U) {
          // Byte sign extension
          readdata_mask_sext := Cat(Fill(blockwidth - 8, readdata_mask(7)),  readdata_mask(7, 0))
        } .elsewhen (io.maskmode === 1.U) {
          // Half-word sign extension
          readdata_mask_sext := Cat(Fill(blockwidth / 2, 
            readdata_mask((blockwidth / 2) - 1)), readdata_mask((blockwidth / 2) - 1, 0))
        } .otherwise {
          // Word sign extension (does nothing)
          readdata_mask_sext := readdata_mask
        }
      } .otherwise {
        readdata_mask_sext := readdata_mask
      }

      io.readdata := readdata_mask_sext
    }
  } .otherwise {
    io.request.valid := false.B
  }
}
