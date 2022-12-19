// Combinational/'synchronous' memory module

package dinocpu.memory

import chisel3._
import chisel3.util._
import dinocpu.memory.MemoryOperation._

/**
  * The instruction memory port.
  *
  * The I/O for this module is defined in [[IMemPortIO]].
  */
class ICombinMemPort extends BaseIMemPort {
  // When the pipeline is supplying a high valid signal
  when (io.pipeline.valid) {
    val request = Wire(new Request)
    request.address   := io.pipeline.address
    request.operation := Read
    request.writedata := 0.U

    io.bus.request.bits  := request
    io.bus.request.valid := true.B
  } .otherwise {
    io.bus.request.valid := false.B
  }

  // Combinational memory is always ready
  io.pipeline.ready := true.B

  // When the memory is outputting a valid instruction
  io.pipeline.good := true.B
  io.pipeline.instruction := io.bus.response.bits.data
}

/**
  * The data memory port.
  *
  * The I/O for this module is defined in [[DMemPortIO]].
  */
class DCombinMemPort extends BaseDMemPort {
  io.pipeline.good := true.B

  when (io.pipeline.valid && (io.pipeline.memread || io.pipeline.memwrite)) {
    // Check that we are not issuing a read and write at the same time
    assert(!(io.pipeline.memread && io.pipeline.memwrite))

    io.bus.request.bits.address := io.pipeline.address
    io.bus.request.valid := true.B

    when (io.pipeline.memwrite) {
      // We issue a ReadWrite to the backing memory.
      // Basic run-down of the ReadWrite operation:
      // - DCombinMemPort sends a ReadWrite at a specific address, **addr**.
      // - Backing memory outputs the data at **addr** in io.response
      // - DCombinMemPort notes that io.memwrite is high in the response path. io.response.bits.data
      //   is masked and sign extended, and sent down io.request.writedata
      // - Backing memory receives the modified writedata and feeds it into the memory at **addr**.
      // Since this is combinational logic, this should theoretically all resolve in one clock cycle with no issues
      io.bus.request.bits.operation := ReadWrite
    } .otherwise {
      // Issue a normal read to the backing memory
      io.bus.request.bits.operation := Read
    }
  } .otherwise {
    // no request coming in so don't send a request out
    io.bus.request.valid := false.B
  }

  // Response path
  when (io.bus.response.valid) {
    when (io.pipeline.memwrite) {
      // Perform writedata modification and send it down io.request.writedata.
      val writedata = Wire (UInt (64.W))

      // When not writing a whole doubleword
      when (io.pipeline.maskmode =/= 3.U) {
        // Read in the existing piece of data at the address, so we "overwrite" only part of it
        val offset = io.pipeline.address(1, 0)
        val readdata = Wire(UInt(64.W))
        val writedata_mask = Wire(UInt(64.W))
        val writedata_mask_shifted = Wire(UInt(64.W))
        val writedata_shifted = Wire(UInt(64.W))
        val readdata_mask = Wire(UInt(64.W)) // readdata doesn't need to be shifted

        readdata := io.bus.response.bits.data

        when (io.pipeline.maskmode === 0.U) { // Byte
          writedata_mask := Cat(Fill(56, 0.U(1.W)), Fill(8, 1.U(1.W)))
        } .elsewhen (io.pipeline.maskmode === 1.U) { // Half-word
          writedata_mask := Cat(Fill(48, 0.U(1.W)), Fill(16, 1.U(1.W)))
        } .elsewhen (io.pipeline.maskmode === 2.U) { // Word
          writedata_mask := Cat(Fill(32, 0.U(1.W)), Fill(32, 1.U(1.W)))
        } .otherwise { // Double-word
          writedata_mask := Fill(64, 1.U(1.W))
        }

        writedata_mask_shifted := writedata_mask << (offset * 8.U)
        writedata_shifted := io.pipeline.writedata << (offset * 8.U)

        // The read bits and the write bits locations are mutually exclusive
        readdata_mask := ~writedata_mask_shifted

        writedata := (readdata & readdata_mask) | (writedata_shifted & writedata_mask_shifted)
      } .otherwise {
        writedata := io.pipeline.writedata
      }

      io.bus.request.bits.writedata := writedata
    } .elsewhen (io.pipeline.memread) {
      // Perform normal masking and sign extension on the read data
      val readdata_mask      = Wire(UInt(64.W))
      val readdata_mask_sext = Wire(UInt(64.W))

      val offset = io.pipeline.address(1, 0)
      when (io.pipeline.maskmode === 0.U) {
        // Byte
        readdata_mask := (io.bus.response.bits.data >> (offset * 8.U)) & 0xff.U
      } .elsewhen (io.pipeline.maskmode === 1.U) {
        // Half-word
        readdata_mask := (io.bus.response.bits.data >> (offset * 8.U)) & 0xffff.U
      } .elsewhen (io.pipeline.maskmode === 2.U) {
        // Word
        readdata_mask := (io.bus.response.bits.data >> (offset * 8.U)) & 0xffffffffL.U
      } .otherwise {
        // Double-word
        readdata_mask := io.bus.response.bits.data
      }

      when (io.pipeline.sext) {
        when (io.pipeline.maskmode === 0.U) {
          // Byte sign extension
          readdata_mask_sext := Cat(Fill(56, readdata_mask(7)),  readdata_mask(7, 0))
        } .elsewhen (io.pipeline.maskmode === 1.U) {
          // Half-word sign extension
          readdata_mask_sext := Cat(Fill(48, readdata_mask(15)), readdata_mask(15, 0))
        } .elsewhen (io.pipeline.maskmode === 2.U) {
          // Word sign extension
          readdata_mask_sext := Cat(Fill(32, readdata_mask(31)), readdata_mask(31, 0))
        } .otherwise {
          // Double-word sign extension (does nothing)
          readdata_mask_sext := readdata_mask
        }
      } .otherwise {
        readdata_mask_sext := readdata_mask
      }

      io.pipeline.readdata := readdata_mask_sext
    }
  }
}
