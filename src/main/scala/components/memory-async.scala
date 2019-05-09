// Asynchronous memory module

package dinocpu

import chisel3._
import chisel3.util._

import chisel3.experimental.ChiselEnum

import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

// A Bundle used for representing a memory access by instruction memory or data memory.
class Request extends Bundle {  
  val address   = UInt(32.W)
  val writedata = UInt(32.W)

  // Encodes the operation requested by this access
  //  0 -> nop
  //  1 -> read
  //  2 -> write
  val operation = UInt(2.W)
}

// A Bundle used for temporarily storing the necessary information for a write in the data memory accessor.
class PartialWrite extends Bundle {
  val address   = UInt(32.W)
  val writedata = UInt(32.W)
  val maskmode  = UInt(2.W)

  // Used to determine whether the stored write is actually a valid write.
  val valid     = Bool() 
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

  // Per the ready/valid interface spec
  when (io.valid && io.ready) {
    val request = Wire(new Request())
    request := DontCare
    request.address   := io.address
    request.operation := 1.U

    io.request.bits  := request
    io.request.valid := true.B
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

  val storedWrite = RegInit(0.U.asTypeOf(new PartialWrite))

  // Per the ready/valid interface spec
  when (io.valid && io.ready && io.memread =/= io.memwrite) {
    val request = Wire (new Request)
    request.address   := io.address
    request.writedata := 0.U
    request.operation := 1.U

    io.request.bits  := request
    io.request.valid := true.B

    when (io.memwrite) {
      val partialWrite = Wire(new PartialWrite) 
      partialWrite.address   := io.address
      partialWrite.writedata := io.writedata
      partialWrite.maskmode  := io.maskmode
      partialWrite.valid     := true.B
      storedWrite := partialWrite
    }
  }

  when (io.response.valid) {
    when (storedWrite.valid) { 
      val writedata = Wire (UInt (32.W))

      // When not writing a whole word
      when (storedWrite.maskmode =/= 2.U) {
        // Read in the existing piece of data at the address, so we "overwrite" only part of it
        val offset = storedWrite.address (1, 0)
        val readdata = Wire (UInt (32.W))
        readdata := io.response.bits
        val data = Wire (UInt (32.W))
        // Mask the portion of the existing data so it can be or'd with the writedata
        when (storedWrite.maskmode === 0.U) {
          data := io.response.bits & ~(0xff.U << (offset * 8.U))
        } .otherwise {
          data := io.response.bits & ~(0xffff.U << (offset * 8.U))
        }
        writedata := data | (storedWrite.writedata << (offset * 8.U))
      } .otherwise {
        // Write the entire word
        writedata := storedWrite.writedata 
      }

      // Program the memory to issue a write
      val request = Wire (new Request)
      request.address   := storedWrite.address
      request.writedata := writedata
      request.operation := 2.U
      io.request.bits  := request
      io.request.valid := true.B

      // Wipe the stored value as it is no longer needed
      storedWrite := 0.U.asTypeOf(new PartialWrite)
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
  io := DontCare

  assert(latency > 0) // Check for attempt to make combinational memory

  val memory    = Mem(math.ceil(size.toDouble/4).toInt, UInt(32.W))
  loadMemoryFromFile(memory, memfile)

  // Instruction port

  val imemPipe = Module(new Pipe(new Request, latency))
  val imemBusy = Reg(new Bool)

  imemPipe.io := DontCare

  io.imem.request.ready := ~imemBusy
  when (io.imem.request.ready && io.imem.request.valid
        && io.imem.request.bits.operation =/= 0.U) {
    // Put the Request into the instruction pipe and signal that instruction memory is busy
    val inRequest = io.imem.request.asTypeOf(new Request)
    imemPipe.io.enq.bits <> inRequest
    imemPipe.io.enq.valid := true.B
    imemBusy := true.B
  }
 
  when (imemPipe.io.deq.valid && imemBusy) {
    // Dequeue the request out of memory and execute it
    val outRequest = imemPipe.io.deq.asTypeOf (new Request)
    when (outRequest.operation === 1.U) { 
      io.imem.response.valid := true.B
      io.imem.response.bits  := memory(outRequest.address >> 2)
    } // Ignore instruction writes as there is no way imem can issue a write access to memory
    // Signal that instruction memory is idling as we're done
    imemBusy := false.B
  }

  // Data port

  val dmemPipe = Module(new Pipe(new Request, latency))
  val dmemBusy = Reg(new Bool)

  dmemPipe.io := DontCare

  io.dmem.request.ready := ~dmemBusy
  when (io.dmem.request.ready && io.dmem.request.valid) {
    // Put the Request into the data pipe and signal that data memory is busy 
    val inRequest = io.dmem.request.asTypeOf (new Request)
    when (inRequest.operation =/= 0.U) {
      dmemPipe.io.enq.valid := true.B
      dmemPipe.io.enq.bits  := inRequest
      // Writes do not stall pipeline
      when (inRequest.operation =/= 1.U) { 
        dmemBusy := true.B
      }
    }
  }
 
  when (dmemPipe.io.deq.valid) {
    // Dequeue request and execute
    val outRequest = dmemPipe.io.deq.asTypeOf (new Request)
    val address = outRequest.address >> 2
    // Check that address is pointing to a valid location in memory
    assert (outRequest.address < size.U)
    
    when (outRequest.operation === 1.U) { 
      io.dmem.response.valid := true.B
      io.dmem.response.bits  := memory(address)
    } .elsewhen (outRequest.operation === 2.U) {  
      memory(address) := outRequest.writedata
    }
    // Signal that data memory is idling as we're done
    when (dmemBusy) {
      dmemBusy := false.B
    }
  }
}





class AsyncMemoryWrapper(size: Int, memFile: String, latency: Int) extends Module {
  val io = IO(new Bundle {
    val imem_address      = Input(UInt(32.W))
    val imem_valid        = Input(Bool())
    val imem_instruction  = Output(UInt(32.W)) 
    val imem_ready        = Output(Bool())
    
    val dmem_address      = Input(UInt(32.W))
    val dmem_valid        = Input(Bool())
    val dmem_writedata    = Input(UInt(32.W))
    val dmem_memread      = Input(Bool()) 
    val dmem_memwrite     = Input(Bool()) 
    val dmem_maskmode     = Input(UInt(2.W))
    val dmem_sext         = Input(Bool()) 
    val dmem_readdata     = Output(UInt(32.W)) 
    val dmem_ready        = Output(Bool())
  })
  io := DontCare

  val imem = Module(new IMemAccess)
  imem.io.address     := io.imem_address
  imem.io.valid       := io.imem_valid
  io.imem_instruction := imem.io.instruction
  io.imem_ready       := imem.io.ready

  val dmem = Module(new DMemAccess)
  dmem.io.address     := io.dmem_address
  dmem.io.valid       := io.dmem_valid
  dmem.io.writedata   := io.dmem_writedata
  dmem.io.memread     := io.dmem_memread
  dmem.io.memwrite    := io.dmem_memwrite
  dmem.io.maskmode    := io.dmem_maskmode
  dmem.io.sext        := io.dmem_sext
  io.dmem_readdata    := dmem.io.readdata
  io.dmem_ready       := dmem.io.ready
  val memory = Module(new DualPortedAsyncMemory (size, memFile, latency))
  memory.io := DontCare

  // Connect memory dmem IO to dmem accessor
  memory.io.imem.request  <> imem.io.request
  memory.io.imem.response <> imem.io.response  
  imem.io.ready           <> imem.io.request.ready

  // Connect memory dmem IO to dmem accessor
  memory.io.dmem.request  <> dmem.io.request
  memory.io.dmem.response <> dmem.io.response  
  dmem.io.ready           <> dmem.io.request.ready
}
