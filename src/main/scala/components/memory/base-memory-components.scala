// Base memory classes

package dinocpu

import chisel3._
import chisel3.util.experimental.loadMemoryFromFile

/**
  * Base class for all modular backing memories. Simply declares the IO and the memory file.
  */
class BaseDualPortedMemory(size: Int, memfile: String) extends Module {
  def wireMemPipe(portio: MemPortBusIO): Unit = {
    portio.response.valid := false.B
    // Combinational memory is inherently always ready for port requests
    portio.request.ready := true.B
  }

  def wireMemory (imem: BaseIMemPort, dmem: BaseDMemPort): Unit = {
    // Connect memory imem IO to dmem accessor
    this.io.imem.request <> imem.io.request
    imem.io.response <> this.io.imem.response
    // Connect memory dmem IO to dmem accessor
    this.io.dmem.request <> dmem.io.request
    dmem.io.response <> this.io.dmem.response
  }

  val io = IO(new Bundle {
    val imem = new MemPortBusIO
    val dmem = new MemPortBusIO
  })
  io <> DontCare


  val memory   = Mem(math.ceil(size.toDouble/4).toInt, UInt(32.W))
  loadMemoryFromFile(memory, memfile)
}

/**
  * Base class for all instruction ports. Simply declares the IO.
  */
class BaseIMemPort extends Module {
  val io = IO (new IMemPortIO)
  io := DontCare
}

/**
  * Base class for all data ports. Simply declares the IO.
  */
class BaseDMemPort extends Module {
  val io = IO (new DMemPortIO)
  io      := DontCare
  io.good := io.response.valid
}
