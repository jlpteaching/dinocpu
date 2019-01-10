// Describes a 32 entry two read port one write port register file

package dinocpu

import chisel3._

import chisel3.util.experimental.BoringUtils

/**
 * A 32 entry two read port one write port register file
 *
 * Here we describe the I/O
 *
 * For more information, see section 4.3 of Patterson and Hennessy
 */
class RegisterFile(implicit val conf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val readreg1  = Input(UInt(5.W))
    val readreg2  = Input(UInt(5.W))
    val writereg  = Input(UInt(5.W))
    val writedata = Input(UInt(32.W))
    val wen       = Input(Bool())

    val readdata1 = Output(UInt(32.W))
    val readdata2 = Output(UInt(32.W))
  })

  val regs = Reg(Vec(32, UInt(32.W)))

  // When the write enable is high, write the data
  when (io.wen) {
    regs(io.writereg) := io.writedata
  }

  // *Always* read the data. This is required for the single cycle CPU since in a single cycle it
  // might both read and write the registers (e.g., an add)
  io.readdata1 := regs(io.readreg1)
  io.readdata2 := regs(io.readreg2)

var x=0
printf("\nRegister | Data\n")
printf("-------------------\n")
for (x <- 0 to 31) 
{
	printf("   x%d     |  %d\n", x.U, regs(x))
	printf("-------------------\n")
}

}
