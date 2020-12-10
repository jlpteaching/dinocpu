// Non-combinational/'asynchronous' memory module

package dinocpu.memory

import chisel3._
import chisel3.util._
import dinocpu.memory.MemoryOperation._

// A Bundle used for temporarily storing the necessary information for a  read/write in the data memory accessor.
class OutstandingReq extends Bundle {
  val address   = UInt(32.W)
  val writedata = UInt(32.W)
  val maskmode  = UInt(2.W)
  val operation = MemoryOperation()
  val sext      = Bool()
}

/**
 * The instruction memory port. Since both the combinational and noncombinational instruction ports just issue
 * read requests in the same way both ports have the same implementation
 *
 * The I/O for this module is defined in [[IMemPortIO]].
 */
class INonCombinMemPort extends ICombinMemPort {
  // Non-combinational memory can technically always accept requests since they are delayed through a pipe.
  // But we want to be able to signal that the memory is holding a request, so a register is used to store
  // whether a request passed through this memory port
  val imemBusy = RegInit(false.B)

  io.pipeline.good := io.bus.response.valid

  when (io.pipeline.valid) {
    imemBusy := true.B
  } .elsewhen (io.bus.response.valid) {
    imemBusy := false.B
  }

  io.pipeline.ready := !imemBusy 
}

/**
 * The data memory port.
 *
 * The I/O for this module is defined in [[DMemPortIO]].
 */
class DNonCombinMemPort extends BaseDMemPort {

  val dmem_busy = RegInit(false.B)

  // A register to hold intermediate data (e.g., write data, mask mode) while the request
  // is outstanding to memory.
  val outstandingReq = Reg (Valid (new OutstandingReq))
  outstandingReq.valid := false.B

  // Used to set the valid bit of the outstanding request
  val sending = Wire(Bool())

  // When the pipeline is supplying a valid read OR write request, send out the request
  // ... on the condition that there isn't an outstanding request in the queue.
  // We need to process outstanding request first to guarantee atomicity of the memory write operation
  // Ready if either we don't have an outstanding request or the outstanding request is a read and
  // it has been satisfied this cycle. Note: we cannot send a read until one cycle after the write has
  // been sent.
  val ready = !outstandingReq.valid || (io.bus.response.valid && (outstandingReq.valid && outstandingReq.bits.operation === MemoryOperation.Read))
  when (!dmem_busy && io.pipeline.valid && (io.pipeline.memread || io.pipeline.memwrite) && ready) {
    // Check if we aren't issuing both a read and write at the same time
    assert (! (io.pipeline.memread && io.pipeline.memwrite))

    // On either a read or write we must read a whole block from memory. Store the necessary
    // information to redirect the memory's response back into itself through a write
    // operation and get the right subset of the block on a read.
    outstandingReq.bits.address   := io.pipeline.address
    outstandingReq.bits.writedata := io.pipeline.writedata
    outstandingReq.bits.maskmode  := io.pipeline.maskmode
    outstandingReq.bits.sext      := io.pipeline.sext
    when (io.pipeline.memwrite) {
      outstandingReq.bits.operation := Write
    } .otherwise {
      outstandingReq.bits.operation := Read
    }
    sending := true.B
    dmem_busy := true.B

    // Program memory to perform a read. Always read since we must read before write.
    io.bus.request.bits.address   := io.pipeline.address
    io.bus.request.bits.writedata := 0.U
    io.bus.request.bits.operation := Read
    io.bus.request.valid          := true.B
  } .otherwise {
    // no request coming in so don't send a request out
    io.bus.request.valid := false.B
    sending := false.B
    dmem_busy := false.B
  }

  // Response path
  when (io.bus.response.valid) {
    assert(outstandingReq.valid)

    dmem_busy := false.B // No longer processing
    when (outstandingReq.bits.operation === MemoryOperation.Write) {
      val writedata = Wire (UInt (32.W))

      // When not writing a whole word
      when (outstandingReq.bits.maskmode =/= 2.U) {
        // Read in the existing piece of data at the address, so we "overwrite" only part of it
        val offset = outstandingReq.bits.address (1, 0)
        val readdata = Wire (UInt (32.W))
        readdata := io.bus.response.bits.data
        // Mask the portion of the existing data so it can be or'd with the writedata
        when (io.pipeline.maskmode === 0.U) {
          when(offset === 0.U) {
            writedata := Cat(readdata(31,8), io.pipeline.writedata(7,0))
          }.elsewhen(offset === 1.U) {
            writedata := Cat(readdata(31,16), Cat(io.pipeline.writedata(15,8), readdata(7,0)))
          }.elsewhen(offset === 2.U) {
            writedata := Cat(readdata(31,24), Cat(io.pipeline.writedata(23,16),readdata(15,0)))
          }.otherwise {
            writedata := Cat(io.pipeline.writedata(31,24), readdata(23,0))
          }
        } .otherwise {
          when (offset === 0.U) {
            writedata := Cat(readdata(31,16),io.pipeline.writedata(15,0))
          }.otherwise {
            writedata := Cat(io.pipeline.writedata(31,16), readdata(15,0))
          }
        }
      } .otherwise {
        // Write the entire word
        writedata := outstandingReq.bits.writedata
      }

      // Program the memory to issue a write.
      val request = Wire (new Request)
      request.address   := outstandingReq.bits.address
      request.writedata := writedata
      request.operation := Write
      io.bus.request.bits  := request
      io.bus.request.valid := true.B
    } .otherwise {
      // Response is valid and we don't have a stored write.
      // Perform masking and sign extension on read data when memory is outputting it
      val readdata_mask      = Wire(UInt(32.W))
      val readdata_mask_sext = Wire(UInt(32.W))

      val offset = outstandingReq.bits.address(1,0)
      when (outstandingReq.bits.maskmode === 0.U) {
        // Byte
        readdata_mask := (io.bus.response.bits.data >> (offset * 8.U)) & 0xff.U
      } .elsewhen (outstandingReq.bits.maskmode === 1.U) {
        // Half-word
        readdata_mask := (io.bus.response.bits.data >> (offset * 8.U)) & 0xffff.U
      } .otherwise {
        readdata_mask := io.bus.response.bits.data
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

      io.pipeline.readdata := readdata_mask_sext
    }
    // Mark the outstanding request register as being invalid, unless sending
    outstandingReq.valid := sending
  } .otherwise {
    // Keep the outstanding request valid or invalid unless sending
    outstandingReq.valid := outstandingReq.valid | sending
  }
}
