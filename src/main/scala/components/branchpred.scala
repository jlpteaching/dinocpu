// This file contains the branch preditor logic

package dinocpu

import chisel3._
import chisel3.util._

/**
 * I/O for the branch predictors
 *
 * Input:  pc, the pc to use to predict whether the branch is taken or not. From decode
 * Input:  update, true if we should update the prediction we made last cycle
 * Input:  taken, true if the branch was actually taken, false otherwise
 *
 * Output: prediction, true if the branch is predicted to be taken, false otherwise
 */
class BranchPredIO extends Bundle {
  val pc         = Input(UInt(32.W))
  val update     = Input(Bool())
  val taken      = Input(Bool())

  val prediction = Output(Bool())
}

/**
 * Base class for all branch predictors. Simply declares the IO and has some
 * simple functions for updating saturating counters
 */
class BaseBranchPredictor(val c: CPUConfig) extends Module {
  val io = IO(new BranchPredIO)

  // Default value is weakly taken for each branch
  val defaultSaturatingCounter = (1 << c.saturatingCounterBits - 1)
  // Create a register file with c.branchPredTableEntries
  // Each entry is c.saturatingCounterBits.W bits wide
  val predictionTable = RegInit(VecInit(Seq.fill(c.branchPredTableEntries)(defaultSaturatingCounter.U(c.saturatingCounterBits.W))))

  // Convenvience for indexing the branch history table
  val tableIndexBits = log2Floor(c.branchPredTableEntries)

  // Function to increment a saturating counter
  def incrCounter(counter: UInt) {
    val max = (1 << c.saturatingCounterBits) - 1
    when (counter =/= max.U) {
      counter := counter + 1.U
    }
  }

  // Function to decrement a saturating counter
  def decrCounter(counter: UInt) {
    when (counter =/= 0.U) {
      counter := counter - 1.U
    }
  }
}

/**
 * An always not taken branch predictor
 *
 */
class AlwaysNotTakenPredictor(implicit val conf: CPUConfig) extends BaseBranchPredictor(conf) {
  io.prediction := false.B
}

/**
 * An always taken branch predictor
 *
 */
class AlwaysTakenPredictor(implicit val conf: CPUConfig) extends BaseBranchPredictor(conf) {
  io.prediction := true.B
}

/**
 * A simple local predictor
 */
class LocalPredictor(implicit val conf: CPUConfig) extends BaseBranchPredictor(conf) {
  // Implement a local predictor here
  io := DontCare
  io.prediction := false.B
}

/**
 * A simple global history predictor
 */
class GlobalHistoryPredictor(implicit val conf: CPUConfig) extends BaseBranchPredictor(conf) {
  // Implement a global predictor
  io := DontCare
  io.prediction := false.B
}
