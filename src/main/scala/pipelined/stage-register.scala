// Definition for a pipeline stage register module and its interface

// TODO: Change this to 'pipelined' package in a future patch
package dinocpu


import chisel3._
import chisel3.internal._
import chisel3.core.CompileOptions

/** A [[Bundle]] which adds a `flush`, `bubble`, and `valid` bit to a data bundle.
  * These values are observed by a StageReg during modification of its contents to
  * either flush its contents to 0 when flush is high, ignore all writes when
  * bubble is high, and write if `valid` is high.
  *
  */


class StageRegIO[+T <: Data](gen: T) extends Bundle {
  /** Inputted data to the stage regster */
  val in = Input(gen)

  /** A bit that flushes the contents of a [[StageReg]] with 0 when high. */
  val flush = Input(Bool())

  /** A bit that ignores any write request regardless of the valid bit. */
  val bubble = Input(Bool())

  /** A bit that writes valid data to a [[StageReg]]. */
  val valid = Input(Bool())

  /** The outputted data from the internal register */
  val data = Output(gen)
}

/**
  * Factory to wrap a data bundle in a stage register IO interface.
  */
object StageRegIO {
  def apply[T <: Data](gen: T): StageRegIO[T] = new StageRegIO(gen)
}


/** A specialized register module that supports freezing, flushing,
  * and writing to its contents when valid.
  */

class StageReg[+T <: Data](val gen: T)(implicit compileOptions: CompileOptions) extends Module {
  val io = IO(new StageRegIO[T] (gen))
  io := DontCare

  val reg = RegInit (0.U.asTypeOf (gen))

  io.data := reg

  when (io.valid && ! io.bubble) {
    reg := io.in
  }

  when (io.flush) {
    reg := 0.U.asTypeOf (gen)
  }
}

