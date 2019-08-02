// Combinational/'synchronous' memory module

package dinocpu

import chisel3._
import chisel3.util._
import dinocpu.MemoryOperation._

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

  // When the memory is outputting a valid instruction
  io.pipeline.good := io.bus.response.valid
  io.pipeline.instruction := io.bus.response.bits.data
}

/**
  * The data memory port.
  *
  * The I/O for this module is defined in [[DMemPortIO]].
  */
class DCombinMemPort extends BaseDMemPort {
  io.pipeline.good := io.bus.response.valid && io.pipeline.memread && !io.pipeline.memwrite

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
      val writedata = Wire (UInt (32.W))

      // When not writing a whole word
      when (io.pipeline.maskmode =/= 2.U) {
        // Read in the existing piece of data at the address, so we "overwrite" only part of it
        val offset = io.pipeline.address (1, 0)
        val readdata = Wire (UInt (32.W))

        readdata := io.bus.response.bits.data

        val data = Wire (UInt (32.W))
        // Mask the portion of the existing data so it can be or'd with the writedata
        when (io.pipeline.maskmode === 0.U) {
          data := readdata & ~(0xff.U << (offset * 8.U))
        } .otherwise {
          data := readdata & ~(0xffff.U << (offset * 8.U))
        }
        writedata := data | (io.pipeline.writedata << (offset * 8.U))
      } .otherwise {
        // Write the entire word
        writedata := io.pipeline.writedata
      }

      io.bus.request.bits.writedata := writedata
    } .elsewhen (io.pipeline.memread) {
      // Perform normal masking and sign extension on the read data
      val readdata_mask      = Wire(UInt(32.W))
      val readdata_mask_sext = Wire(UInt(32.W))

      val offset = io.pipeline.address(1,0)
      when (io.pipeline.maskmode === 0.U) {
        // Byte
        readdata_mask := (io.bus.response.bits.data >> (offset * 8.U)) & 0xff.U
      } .elsewhen (io.pipeline.maskmode === 1.U) {
        // Half-word
        readdata_mask := (io.bus.response.bits.data >> (offset * 8.U)) & 0xffff.U
      } .otherwise {
        readdata_mask := io.bus.response.bits.data
      }

      when (io.pipeline.sext) {
        when (io.pipeline.maskmode === 0.U) {
          // Byte sign extension
          readdata_mask_sext := Cat(Fill(24, readdata_mask(7)),  readdata_mask(7, 0))
        } .elsewhen (io.pipeline.maskmode === 1.U) {
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
  }
}
