// This file contains the branch preditor logic

package dinocpu.components

import chisel3._
import chisel3.util._
import dinocpu._

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
abstract class BaseBranchPredictor(val c: CPUConfig) extends Module {
  val io = IO(new BranchPredIO)

  // Default value is weakly taken for each branch
  val defaultSaturatingCounter = (1 << c.saturatingCounterBits - 1)
  // Create a register file with c.branchPredTableEntries
  // Each entry is c.saturatingCounterBits.W bits wide
  val predictionTable = RegInit(VecInit(Seq.fill(c.branchPredTableEntries)(defaultSaturatingCounter.U(c.saturatingCounterBits.W))))

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

  // Register to store the last branch predicted so we can update the tables.
  // This will also work for back to back branches since we resolve them in
  // execute (one cycle later)
  val lastBranch = Reg(UInt())

  when (io.update) {
    when (io.taken) {
      incrCounter(predictionTable(lastBranch))
    } .otherwise {
      decrCounter(predictionTable(lastBranch))
    }
  }

  // The first bit for the table access is based on the number of entries.
  // +2 since we ignore the bottom two bits
  val tableIndex = io.pc(log2Floor(conf.branchPredTableEntries) + 2, 2)

  // Return the high-order bit
  io.prediction := predictionTable(tableIndex)(conf.saturatingCounterBits - 1)

  // Remember the last pc to update the table later
  lastBranch := tableIndex
}

/**
 * A simple global history predictor
 */
class GlobalHistoryPredictor(implicit val conf: CPUConfig) extends BaseBranchPredictor(conf) {

  // The length is based on the size of the branch history table
  val historyBits = log2Floor(conf.branchPredTableEntries)
  // Need one extra bit for the "last" history
  val history = RegInit(0.U((historyBits+1).W))

  val curhist = history(historyBits,0)
  when(io.update) {
    // Update the prediction for this branch history
    // Use the last branch history.
    when (io.taken) {
      incrCounter(predictionTable(curhist))
    } .otherwise {
      decrCounter(predictionTable(curhist))
    }

    history := Cat(curhist, io.taken) // update the history register at the end of the cycle
  }

  io.prediction := predictionTable(curhist)(conf.saturatingCounterBits - 1)
}
