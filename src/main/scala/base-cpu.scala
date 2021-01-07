// The base abstract CPU module which declares the CoreIO of a CPU
package dinocpu

import chisel3._
import dinocpu.components._

/**
  * Base CPU module which all CPU models implement
  */
abstract class BaseCPU extends Module {
  val io = IO(new CoreIO())

  // Required so the compiler doesn't optimize things away when testing
  // incomplete designs.
  dontTouch(io)
}
