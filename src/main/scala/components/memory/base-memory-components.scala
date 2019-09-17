// Base memory classes

package dinocpu

import chisel3._
import chisel3.util.experimental.loadMemoryFromFile

/**
  * Base class for all modular backing memories. Simply declares the IO and the memory file.
  */
abstract class BaseDualPortedMemory(size: Int, memfile: String) extends Module {
  def wireMemory (imem: BaseIMemPort, dmem: BaseDMemPort): Unit = {
    // Connect memory imem IO to dmem accessor
    this.io.imem.request <> imem.io.bus.request
    imem.io.bus.response <> this.io.imem.response
    // Connect memory dmem IO to dmem accessor
    this.io.dmem.request <> dmem.io.bus.request
    dmem.io.bus.response <> this.io.dmem.response
  }

  val io = IO(new Bundle {
    val imem = new MemPortBusIO
    val dmem = new MemPortBusIO
  })
  io.imem <> 0.U.asTypeOf (new MemPortBusIO)
  io.dmem <> 0.U.asTypeOf (new MemPortBusIO)

  val memory = Mem(math.ceil(size.toDouble/4).toInt, UInt(32.W))
  loadMemoryFromFile(memory, memfile)
}

/**
  * Base class for all instruction ports. Simply declares the IO.
  */
abstract class BaseIMemPort extends Module {
  val io = IO (new Bundle {
    val pipeline = new IMemPortIO
    val bus  = Flipped (new MemPortBusIO)
  })

  io.pipeline <> 0.U.asTypeOf (new IMemPortIO)
  io.bus      <> 0.U.asTypeOf (new MemPortBusIO)
}

/**
  * Base class for all data ports. Simply declares the IO.
  */
abstract class BaseDMemPort extends Module {
  val io = IO (new Bundle {
    val pipeline = new DMemPortIO
    val bus = Flipped (new MemPortBusIO)
  })

  io.pipeline <> 0.U.asTypeOf (new DMemPortIO)
  io.bus      <> 0.U.asTypeOf (new MemPortBusIO)

  io.pipeline.good := io.bus.response.valid
}
