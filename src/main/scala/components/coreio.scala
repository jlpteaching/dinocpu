// The IO between the core and the rest of the system

package dinocpu

import chisel3._

class CoreIO extends Bundle {
  val imem = Flipped(new IMemPortIO)
  val dmem = Flipped(new DMemPortIO)
}
