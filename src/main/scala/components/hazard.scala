// This file contains the hazard detection unit

package CODCPU

import chisel3._

/**
 * The hazard detection unit
 *
 * Here we should describe the I/O
 *
 * For more information, see Section 4.7 of Patterson and Hennessy
 * This follows the "Data hazards and stalls" section.
 */
class HazardUnit extends Module {
  val io = IO(new Bundle {
    val rs1          = Input(UInt(5.W))
    val rs2          = Input(UInt(5.W))
    val idex_memread = Input(Bool())
    val idex_rd      = Input(UInt(5.W))

    val pcwrite     = Output(Bool())
    val ifid_write  = Output(Bool())
    val idex_bubble = Output(Bool())
  })

  io.pcwrite     := true.B
  io.ifid_write  := true.B
  io.idex_bubble := false.B

  // Load to use hazard.
  when (io.idex_memread &&
        (io.idex_rd === io.rs1 || io.idex_rd === io.rs2)) {
    io.pcwrite     := false.B
    io.ifid_write  := false.B
    io.idex_bubble := true.B
  }

}
