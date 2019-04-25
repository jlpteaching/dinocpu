// Asynchronous memory module

package dinocpu

import chisel3._
import chisel3.util._

import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

//
/**
 * The *interface* for the asynchronous instruction memory. All inputs of the synchronous IMemIO bundle
 * are copied into this bundle, along with two extra flags:  
 *
 * Input: ready, true when an address is being supplied to the instruction memory, indicating that an instruction should be fetched
 *
 * Output: valid, true when the memory is outputting a valid instruction through imem.instruction 
 */
class AsyncIMemIO extends IMemIO {
  val ready = Input(Bool())

  val valid = Output(Bool())
}

/**
 * The *interface* for the asynchronous data memory. All inputs of the synchronous DMemIO bundle
 * are copied into this bundle along with an extra flag:
 * 
 * Output: valid, true when the number of cycles that the async memory is supposed to wait for 
 *         elapses, and readdata contains the data that was read and processed 
 *
 * The 'readiness' of the data address input is inferred from whether dmem.memread or dmem.memwrite
 * is true, as otherwise ready would be true when dmem.memread or dmem.memwrite is true.
 */
class AsyncDMemIO extends DMemIO {
  val valid = Output(Bool())
}

// A Bundle used for storing inputs while memory is being delayed.
class AsyncStoredIO extends Bundle {  
  val address   = UInt(32.W)
  val writedata = UInt(32.W)
  val maskmode  = UInt(2.W)
  val sext      = Bool()
  val delaying  = Bool()

  // operation is an encoding of the current memory operation, represented below:
  //             00 -> Instruction read
  //             01 -> Data read
  //             10 (-> Data write
  // Maybe use an enumerator for this, if possible?
  val operation = UInt(2.W)
}


/**
 * The modified asynchronous form of the dual ported memory module declared above. 
 * When io.imem.ready, io.dmem.memread, or io.dmem.memwrite is true, this memory module simulates the latency of 
 * real DRAM by waiting for a configurable number of cycles. After this amount of time elapses, 
 * it performs its normal operation and indicates that the requested output is now valid for reading
 * 
 * As with the synced memory module, this memory should only be instantiated in the Top file,
 * and never within the actual CPU.
 *
 * The I/O for this module is defined in [[AsyncIMemIO]] and [[AsyncDMemIO]].
 */
class DualPortedAsyncMemory(size: Int, memfile: String, latency: Int) extends Module {
  val io = IO(new Bundle {
    val imem = new AsyncIMemIO
    val dmem = new AsyncDMemIO
  })
  io.imem.valid := false.B
  io.dmem.valid := false.B
  io := DontCare

  val memory    = Mem(math.ceil(size.toDouble/4).toInt, UInt(32.W))
  loadMemoryFromFile(memory, memfile)
 
  val increment = Wire(Bool())
  increment := DontCare
  val storedIO = RegInit (0.U.asTypeOf(new AsyncStoredIO))
  val (delayCounter, delayWrap) = Counter (increment, latency) 

  when (~storedIO.delaying && (io.imem.ready || io.dmem.memread || io.dmem.memwrite)) {
    increment := true.B
    when (io.imem.ready) { 
      storedIO.address   := io.imem.address
      storedIO.operation := 0.U
    } .elsewhen (io.dmem.memread) { 
      storedIO.address   := io.dmem.address
      storedIO.maskmode  := io.dmem.maskmode
      storedIO.sext      := io.dmem.sext
      storedIO.operation := 1.U
    } .elsewhen (io.dmem.memwrite) { 
      storedIO.address   := io.dmem.address
      storedIO.writedata := io.dmem.writedata 
      storedIO.maskmode  := io.dmem.maskmode
      storedIO.sext      := io.dmem.sext
      storedIO.operation := 2.U     
    }
  } .elsewhen (storedIO.delaying && delayCounter === latency.U) {
    increment := false.B
    when (storedIO.operation === 0.U) {
      when (storedIO.address >= size.U) {
        io.imem.instruction := 0.U
      } .otherwise {
        io.imem.instruction := memory(storedIO.address >> 2)
      }

      io.imem.valid := true.B
    


    } .elsewhen (storedIO.operation === 1.U) {
      assert(storedIO.address < size.U)

      val readdata = Wire(UInt(32.W))

      when (storedIO.maskmode =/= 2.U) { // When not loading a whole word
        val offset = storedIO.address(1,0)
        readdata := memory(storedIO.address >> 2) >> (offset * 8.U)
        when (storedIO.maskmode === 0.U) { // Reading a byte
          readdata := memory(storedIO.address >> 2) & 0xff.U
        } .otherwise {
          readdata := memory(storedIO.address >> 2) & 0xffff.U
        }
      } .otherwise {
        readdata := memory(storedIO.address >> 2)
      }

      when (storedIO.sext) {
        when (storedIO.maskmode === 0.U) {
          io.dmem.readdata := Cat(Fill(24, readdata(7)), readdata(7,0))
        } .elsewhen (storedIO.maskmode === 1.U) {
          io.dmem.readdata := Cat(Fill(16, readdata(15)), readdata(15,0))
        } .otherwise {
          io.dmem.readdata := readdata
        }
      } .otherwise {
        io.dmem.readdata := readdata
      }

      io.dmem.valid := true.B



    } .elsewhen (storedIO.operation === 2.U) {
      assert(storedIO.address < size.U)
      when (storedIO.maskmode =/= 2.U) { // When not storing a whole word
        val offset = storedIO.address(1,0)
        // first read the data since we are only overwriting part of it
        val readdata = Wire(UInt(32.W))
        readdata := memory(storedIO.address >> 2)
        // mask off the part we're writing
        val data = Wire(UInt(32.W))
        when (storedIO.maskmode === 0.U) { // Reading a byte
          data := readdata & ~(0xff.U << (offset * 8.U))
        } .otherwise {
          data := readdata & ~(0xffff.U << (offset * 8.U))
        }
        memory(storedIO.address >> 2) := data | (storedIO.writedata << (offset * 8.U))
      } .otherwise {
        memory(storedIO.address >> 2) := storedIO.writedata
      }

      val writeData = storedIO.writedata
      printf (s"Writing $writeData to memory")
      
      io.dmem.valid := true.B
    }
  
    // Reset counter
    delayCounter := 0.U
  }
  
  storedIO.delaying := increment
  
  printf(p"$increment $delayCounter $latency\n")
  printf(p"$storedIO \n\n\n")
  
}
