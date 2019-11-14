// Asynchronous memory module

package dinocpu.memory

import chisel3._
import chisel3.experimental.ChiselEnum

/**
 * Chisel enumerator to assign names to the UInt constants representing memory operations
 */
object MemoryOperation extends ChiselEnum {
  val Read, Write, ReadWrite = Value
}
