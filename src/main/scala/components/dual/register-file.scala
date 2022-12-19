// Describes a 32 entry two read port one write port register file

package dinocpu.components.dual

import chisel3._
import dinocpu._

/**
 * A 32 entry two read port one write port register file with support for dual-issue.
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
 *   pipeA_readdata1 = R[pipeA_readreg1]
 *   pipeA_readdata2 = R[pipeA_readreg2]
 *   pipeB_readdata1 = R[pipeB_readreg1]
 *   pipeB_readdata2 = R[pipeB_readreg2]
 *   if (pipeA_wen) R[pipeA_writereg] = pipeA_writedata
 *   if (pipeB_wen) R[pipeB_writereg] = pipeB_writedata
 *
 * Input:  pipeA_readreg1, source register 1 (rs1) of pipeA
 * Input:  pipeA_readreg2, source register 2 (rs2) of pipeA
 * Input:  pipeA_writereg, destination register (rd) of pipeA
 * Input:  pipeA_writedata, data to write to R[pipeA_writereg]
 * Input:  pipeA_wen, pipeA write enable. If true, write to the pipeA_writereg register.
 * Input:  pipeB_readreg1, source register 1 (rs1) of pipeB
 * Input:  pipeB_readreg2, source register 2 (rs2) of pipeB
 * Input:  pipeB_writereg, destination register (rd) of pipeB
 * Input:  pipeB_writedata, data to write to R[pipeB_writereg]
 * Input:  pipeB_wen, pipeB write enable. If true, write to the pipeB_writereg register.
 *
 * Output: pipeA_readdata1, the data in register number pipeA_readreg1 (R[pipeA_readreg1])
 * Output: pipeA_readdata2, the data in register number pipeA_readreg2 (R[pipeA_readreg2])
 * Output: pipeB_readdata1, the data in register number pipeB_readreg1 (R[pipeB_readreg1])
 * Output: pipeB_readdata2, the data in register number pipeB_readreg2 (R[pipeB_readreg2])
 */
class DualIssueRegisterFile(implicit val conf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val pipeA_readreg1  = Input(UInt(5.W))
    val pipeA_readreg2  = Input(UInt(5.W))
    val pipeA_writereg  = Input(UInt(5.W))
    val pipeA_writedata = Input(UInt(64.W))
    val pipeA_wen       = Input(Bool())
    val pipeB_readreg1  = Input(UInt(5.W))
    val pipeB_readreg2  = Input(UInt(5.W))
    val pipeB_writereg  = Input(UInt(5.W))
    val pipeB_writedata = Input(UInt(64.W))
    val pipeB_wen       = Input(Bool())

    val pipeA_readdata1 = Output(UInt(64.W))
    val pipeA_readdata2 = Output(UInt(64.W))
    val pipeB_readdata1 = Output(UInt(64.W))
    val pipeB_readdata2 = Output(UInt(64.W))
  })

  // Required so the compiler doesn't optimize things away when testing
  // incomplete designs.
  dontTouch(io)

  val regs = Reg(Vec(32, UInt(64.W)))

  // When the write enable is high, write the data
  // pipeA_writereg must be updated before pipeB_writereg as inst in pipeB comes after inst in pipeA
  when (io.pipeA_wen && io.pipeB_wen && io.pipeA_writereg === io.pipeB_writereg) {
    regs(io.pipeB_writereg) := io.pipeB_writedata
  } .otherwise {
    when (io.pipeA_wen) {
      regs(io.pipeA_writereg) := io.pipeA_writedata
    }
    when (io.pipeB_wen) {
      regs(io.pipeB_writereg) := io.pipeB_writedata
    }
  }

  // *Always* read the data. This is required for the single cycle CPU since in a single cycle it
  // might both read and write the registers (e.g., an add)
  io.pipeA_readdata1 := regs(io.pipeA_readreg1)
  io.pipeA_readdata2 := regs(io.pipeA_readreg2)
  io.pipeB_readdata1 := regs(io.pipeB_readreg1)
  io.pipeB_readdata2 := regs(io.pipeB_readreg2)

  if (conf.cpuType != "single-cycle") {
    // For the five-cycle and pipelined CPU forward the data through the register file
    when (io.pipeA_readreg1 === io.pipeB_writereg && io.pipeB_wen) {
      io.pipeA_readdata1 := io.pipeB_writedata
    } .elsewhen (io.pipeA_readreg1 === io.pipeA_writereg && io.pipeA_wen) {
      io.pipeA_readdata1 := io.pipeA_writedata
    }

    when (io.pipeA_readreg2 === io.pipeB_writereg && io.pipeB_wen) {
      io.pipeA_readdata2 := io.pipeB_writedata
    } .elsewhen (io.pipeA_readreg2 === io.pipeA_writereg && io.pipeA_wen) {
      io.pipeA_readdata2 := io.pipeA_writedata
    }

    when (io.pipeB_readreg1 === io.pipeB_writereg && io.pipeB_wen) {
      io.pipeB_readdata1 := io.pipeB_writedata
    } .elsewhen (io.pipeB_readreg1 === io.pipeA_writereg && io.pipeA_wen) {
      io.pipeB_readdata1 := io.pipeA_writedata
    }

    when (io.pipeB_readreg2 === io.pipeB_writereg && io.pipeB_wen) {
      io.pipeB_readdata2 := io.pipeB_writedata
    } .elsewhen (io.pipeB_readreg2 === io.pipeA_writereg && io.pipeA_wen) {
      io.pipeB_readdata2 := io.pipeA_writedata
    }
  }
}
