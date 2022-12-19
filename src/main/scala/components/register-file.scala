// Describes a 32 entry two read port one write port register file

package dinocpu.components

import chisel3._
import dinocpu._

/**
 * A 32 entry two read port one write port register file.
 *
 * Note: this register file *has* an entry for register 0, and it's possible to
 * overwrite the default 0 value. Thus, you need to add extra logic to the
 * DINO CPU control or data path to make sure you always get 0 from register 0.
 *
 * Note: The chisel registers cannot be read and written on the same cycle.
 * Therefore, we have a bypass logic for when a register is read in the same
 * cycle it is written. However, for the single cycle CPU this causes a
 * combinational loop. Thus, we must have different logic when creating a
 * single cycle vs pipelined CPU.
 *
 * Basic operation:
 *   readdata1 = R[readreg1]
 *   readdata2 = R[readreg2]
 *   if (wen) R[writereg] = writedata
 *
 * Input:  readreg1, the number of the register to read
 * Input:  readreg2, the number of the register to read
 * Input:  writereg, the number of the register to write
 * Input:  writedata, the data to write into R[writereg]
 * Input:  wen, write enable. If true, write the writereg register
 *
 * Output: readdata1, the data in register number readreg1 (R[readreg1])
 * Output: readdata2, the data in register number readreg2 (R[readreg2])
 *
 * For more information, see section 4.3 of Patterson and Hennessy
 */
class RegisterFile(implicit val conf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val readreg1  = Input(UInt(5.W))
    val readreg2  = Input(UInt(5.W))
    val writereg  = Input(UInt(5.W))
    val writedata = Input(UInt(64.W))
    val wen       = Input(Bool())

    val readdata1 = Output(UInt(64.W))
    val readdata2 = Output(UInt(64.W))
  })

  // Required so the compiler doesn't optimize things away when testing
  // incomplete designs.
  dontTouch(io)

  val regs = Reg(Vec(32, UInt(64.W)))

  // When the write enable is high, write the data
  when (io.wen) {
    regs(io.writereg) := io.writedata
  }

  // *Always* read the data. This is required for the single cycle CPU since in a single cycle it
  // might both read and write the registers (e.g., an add)
  io.readdata1 := regs(io.readreg1)
  io.readdata2 := regs(io.readreg2)

  if (conf.cpuType != "single-cycle") {
    // For the five-cycle and pipelined CPU forward the data through the register file
    when (io.readreg1 === io.writereg && io.wen) {
      io.readdata1 := io.writedata
    }
    when (io.readreg2 === io.writereg && io.wen) {
      io.readdata2 := io.writedata
    }
  }
}
