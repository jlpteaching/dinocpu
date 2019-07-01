// Asynchronous memory module

package dinocpu

import chisel3._

/**
 * Enumerator to assign names to the UInt constants representing memory operations
 */
object MemoryOperation {
  val Read = 0.U
  val Write = 1.U
  // Reserved exclusively for combinational memory - this is checked for in non-combin
  // memory with an assert
  val ReadWrite = 2.U
}
