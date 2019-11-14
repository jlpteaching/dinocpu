// The IO between the core and the rest of the system

package dinocpu.components

import chisel3._
import dinocpu.memory._

class CoreIO extends Bundle {
  val imem = Flipped(new IMemPortIO)
  val dmem = Flipped(new DMemPortIO)
}
