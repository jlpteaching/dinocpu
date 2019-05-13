// Asynchronous memory module

package dinocpu

import chisel3._
import chisel3.util._

import chisel3.experimental.ChiselEnum

import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

object MemoryOperation {
  val Read = 0.U
  val Write = 1.U
}

import MemoryOperation._

// A Bundle used for representing a memory access by instruction memory or data memory.
class Request extends Bundle {  
  val address      = UInt(32.W)
  val writedata    = UInt(32.W)
  val operation    = UInt(1.W)
}

// A Bundle used for temporarily storing the necessary information for a write in the data memory accessor.
class PartialWrite extends Bundle {
  val address   = UInt(32.W)
  val writedata = UInt(32.W)
  val maskmode  = UInt(2.W)
}

/** 
 * The generic interface for communication between the IMem/DMemAccess modules and the backing memory.
 *
 * Input:  request, the ready/valid interface for a MemAccess module to issue Requests to. Memory
 *         will only accept a request when both request.valid (the MemAccess is supplying valid data)
 *         and request.ready (the memory is idling for a request) are high.
 *
 * Output: response, the valid interface for the data outputted by memory if it was requested to read. 
 *         the bits in response.bits should only be treated as valid data when response.valid is high.
 */
class AsyncMemIO extends Bundle {
  val request  = Flipped(Decoupled (new Request))
  val response = Valid (UInt (32.W))
}

/** 
 * A generic ready/valid interface for MemAccess modules, whose IOs extend this.
 * Input:  address, the address of a piece of data in memory. 
 * Input:  valid, true when the address specified is valid
 * Input:  response, the return route from memory to a memory access. This is primarily meant for connecting to 
 *         an AsyncMemIO's response output, and should not be connected to anything else in any circumstance 
 *         (or things will possibly break)
 * 
 * Output: ready, true when memory is either idling or outputting a value, and is ready for a new 
 *         request. Note that this is different from access_out.ready - this ready is for use by the general
 *         CPU (like to signal when to stall the CPU), while access_out.ready is used for signalling 
 *         between the memory and accessor only
 * Output: request, a DecoupledIO that delivers a request from a memory accessor to memory. This is primarily
 *         meant for connecting to an AsynMemIO's request input, and should not be connected to anything else
 */
class MemAccessIO extends Bundle {
  val address  = Input(UInt(32.W))
  val valid    = Input(Bool())
  val response = Flipped(Valid(UInt(32.W)))

  val ready    = Output(Bool())
  val request  = Decoupled(new Request)
}

/** 
 * The *interface* of the IMemAccess module.
 *
 * Input:  address, the address of an instruction in memory 
 * Input:  valid, true when the address specified is valid
 *
 * Output: instruction, the requested instruction
 * Output: ready, true when memory is idling and ready for a request
 */
class IMemAccessIO extends MemAccessIO {
  val instruction = Output(UInt(32.W))
}

/**
 * The *interface* of the DMemAccess module.
 *
 * Input:  address, the address of a piece of data in memory. 
 * Input:  writedata, valid interface for the data to write to the address
 * Input:  valid, true when the address (and writedata during a write) specified is valid
 * Input:  memread, true if we are reading from memory
 * Input:  memwrite, true if we are writing to memory
 * Input:  maskmode, mode to mask the result. 0 means byte, 1 means halfword, 2 means word
 * Input:  sext, true if we should sign extend the result
 *
 * Output: readdata, the data read and sign extended
 * Output: ready, true when memory is idling and ready for a request
 */
class DMemAccessIO extends MemAccessIO {
  val writedata = Input(UInt(32.W))
  val memread   = Input(Bool())
  val memwrite  = Input(Bool())
  val maskmode  = Input(UInt(2.W))
  val sext      = Input(Bool())

  val readdata  = Output(UInt(32.W))
}

/**
 * The instruction memory accessor.
 *
 * The I/O for this module is defined in [[IMemAccessIO]].
 */
class IMemAccess extends Module {
  val io = IO (new IMemAccessIO)
  io := DontCare
  io.request.valid  := false.B
  io.ready          := io.request.ready

  // Per the ready/valid interface spec
  when (io.valid && io.request.ready) {
    val request = Wire(new Request())
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
 * The data memory accessor.
 *
 * The I/O for this module is defined in [[DMemAccessIO]].
 */
class DMemAccess extends Module {
  val io = IO (new DMemAccessIO)
  io := DontCare
  io.request.valid  := false.B

  val storedWrite = RegInit(0.U.asTypeOf(Valid(new PartialWrite)))
  val memReallyReady = io.request.ready && !storedWrite.valid
  io.ready := memReallyReady

  // When the backing memory is ready and the CPU is supplying a valid read OR write request, send out the request
  // on the condition that there isn't a stored write in the queue.
  // We need to process stored writes first to guarantee atomicity of the memory write operation

  when (io.valid && memReallyReady && io.memread =/= io.memwrite) {
    val request = Wire (new Request)
    request.address   := io.address
    request.writedata := 0.U
    request.operation := Read

    when (io.memwrite) {
      val partialWrite = Wire(new PartialWrite) 
      partialWrite.address   := io.address
      partialWrite.writedata := io.writedata
      partialWrite.maskmode  := io.maskmode
      storedWrite.valid := true.B
      storedWrite.bits  := partialWrite
    }
    
    io.request.bits  := request
    io.request.valid := true.B
  } .otherwise {
    io.request.valid := false.B
  }

  when (io.response.valid) {
    when (storedWrite.valid && io.request.ready) {
      val writedata = Wire (UInt (32.W))

      // When not writing a whole word
      when (storedWrite.bits.maskmode =/= 2.U) {
        // Read in the existing piece of data at the address, so we "overwrite" only part of it
        val offset = storedWrite.bits.address (1, 0)
        val readdata = Wire (UInt (32.W))
        readdata := io.response.bits
        val data = Wire (UInt (32.W))
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
      val request = Wire (new Request)
      request.address   := storedWrite.bits.address
      request.writedata := writedata
      request.operation := Write
      io.request.bits  := request
      io.request.valid := true.B

      // Mark the stored write register as being invalid.
      storedWrite.valid := false.B
    } .otherwise {
      // Perform masking and sign extension on read data when memory is outputting it
      val readdata_mask      = Wire(UInt(32.W))
      val readdata_mask_sext = Wire(UInt(32.W))

      when (io.maskmode === 0.U) {
        // Byte
        readdata_mask := io.response.bits & 0xff.U
      } .elsewhen (io.maskmode === 1.U) {
        // Half-word
        readdata_mask := io.response.bits & 0xffff.U
      } .otherwise {
        // Word
        readdata_mask := io.response.bits
      }

      when (io.sext) {
        when (io.maskmode === 0.U) {
          readdata_mask_sext := Cat(Fill(24, readdata_mask(7)),  readdata_mask(7, 0))
        } .elsewhen (io.maskmode === 1.U) {
          readdata_mask_sext := Cat(Fill(26, readdata_mask(15)), readdata_mask(15,0))
        } .otherwise {
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
class DualPortedAsyncMemory(size: Int, memfile: String, latency: Int) extends Module {
  val io = IO(new Bundle {
    val imem = new AsyncMemIO
    val dmem = new AsyncMemIO
  })
  io <> DontCare

  assert(latency > 1) // Check for attempt to make combinational memory

  val memory    = Mem(math.ceil(size.toDouble/4).toInt, UInt(32.W))
  loadMemoryFromFile(memory, memfile)

  // Instruction port

  val imemPipe = Module(new Pipe(new Request, latency - 1))
  val imemBusy = RegInit(false.B)

  imemPipe.io.enq.bits  <> DontCare
  imemPipe.io.enq.valid := false.B

  io.imem.request.ready := !imemBusy

  when (!imemBusy && io.imem.request.valid) {
    // Put the Request into the instruction pipe and signal that instruction memory is busy
    val inRequest = io.imem.request.asTypeOf(new Request)
    imemPipe.io.enq.bits  := inRequest
    imemPipe.io.enq.valid := true.B
    imemBusy := true.B
  }

  when (imemPipe.io.deq.valid) {
    // Dequeue the request out of memory and execute it
    val outRequest = imemPipe.io.deq.asTypeOf (new Request)
    when (outRequest.operation === Read) {
      io.imem.response.valid := true.B
      io.imem.response.bits  := memory(outRequest.address >> 2)
    } // Ignore instruction writes as there is no way imem can issue a write access to memory
    // Signal that instruction memory is idling as we're done
    
    imemBusy := false.B
  }

  // Data port

  val dmemPipe = Module(new Pipe(new Request, latency - 1))
  val dmemBusy = RegInit(false.B)

  dmemPipe.io.enq.bits  <> DontCare
  dmemPipe.io.enq.valid := false.B

  io.dmem.request.ready := !dmemBusy
  when (io.dmem.request.valid) {
    // Put the Request into the data pipe and signal that data memory is busy 
    val inRequest = io.dmem.request.asTypeOf (new Request)
    dmemPipe.io.enq.bits  := inRequest
    dmemPipe.io.enq.valid := true.B
      // Writes do not stall pipeline
    when (inRequest.operation =/= Write) { 
      dmemBusy := true.B
    }
  }

  when (dmemPipe.io.deq.valid) {
    // Dequeue request and execute
    val outRequest = dmemPipe.io.deq.asTypeOf (new Request)
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
