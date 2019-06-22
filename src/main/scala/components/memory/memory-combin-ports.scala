// Combinational/'synchronous' memory module

package components.memory

import chisel3._
import chisel3.util._
import components.memory.MemoryOperation._

/**
  * The instruction memory port.
  *
  * The I/O for this module is defined in [[IMemPortIO]].
  */
class ICombinMemPort extends Module {
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
class DCombinMemPort extends Module {
  val io = IO (new DMemPortIO)
  io      := DontCare
  io.good := io.response.valid

  when (io.valid && (io.memread || io.memwrite)) {
    // Check that we are not issuing a read and write at the same time
    assert(!(io.memread || io.memwrite))

    io.request.bits.address := io.address
    io.request.valid := true.B

    when (io.memwrite) {
      // We issue a ReadWrite to the backing memory.
      // Basic run-down of the ReadWrite operation:
      // - DCombinMemPort sends a ReadWrite at a specific address, **addr**.
      // - Backing memory outputs the data at **addr** in io.response
      // - DCombinMemPort receives high for io.response.valid, and notes that io.memwrite is high. io.response.bits.data
      //   is masked and sign extended, and sent down io.request.writedata
      // - Backing memory receives the modified writedata and feeds it into the memory at **addr**.
      // Since this is combinational logic, this should theoretically all resolve in one clock cycle with no issues
      io.request.bits.operation := ReadWrite
    } .otherwise {
      // Issue a normal read to the backing memory

      io.request.bits.operation := Read
    }
  } .otherwise {
    // no request coming in so don't send a request out
    io.request.valid := false.B
  }

  // Response path
  when (io.response.valid) {
    when (io.memwrite) {
      // Perform writedata modification and send it down io.request.writedata.

      val writedata = Wire (UInt (32.W))

      // When not writing a whole word
      when (io.maskmode =/= 2.U) {
        // Read in the existing piece of data at the address, so we "overwrite" only part of it
        val offset = io.address (1, 0)
        val readdata = Wire (UInt (32.W))
        readdata := io.response.bits.data
        val data = Wire (UInt (32.W))
        // Mask the portion of the existing data so it can be or'd with the writedata
        when (io.maskmode === 0.U) {
          data := readdata & ~(0xff.U << (offset * 8.U))
        } .otherwise {
          data := readdata & ~(0xffff.U << (offset * 8.U))
        }
        writedata := data | (io.writedata << (offset * 8.U))
      } .otherwise {
        // Write the entire word
        writedata := io.writedata
      }

      io.request.bits.writedata := writedata
    } .otherwise {
      // Perform normal masking and sign extension on the read data
      val readdata_mask      = Wire(UInt(32.W))
      val readdata_mask_sext = Wire(UInt(32.W))

      val offset = io.address(1,0)
      when (io.maskmode === 0.U) {
        // Byte
        readdata_mask := (io.response.bits.data >> (offset * 8.U)) & 0xff.U
      } .elsewhen (io.maskmode === 1.U) {
        // Half-word
        readdata_mask := (io.response.bits.data >> (offset * 8.U)) & 0xffff.U
      } .otherwise {
        readdata_mask := io.response.bits.data
      }

      when (io.sext) {
        when (io.maskmode === 0.U) {
          // Byte sign extension
          readdata_mask_sext := Cat(Fill(24, readdata_mask(7)),  readdata_mask(7, 0))
        } .elsewhen (io.maskmode === 1.U) {
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
  }
}
