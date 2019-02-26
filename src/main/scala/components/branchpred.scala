// This file contains the branch preditor logic

package dinocpu

import chisel3._
import chisel3.util._

/**
 * I/O for the branch predictors
 *
 * Output: prediction, true if the branch is predicted to be taken, false otherwise
 */
class BranchPredIO extends Bundle {
  val prediction = Output(Bool())
}

/**
 * An always not taken branch predictor
 *
 */
class AlwaysNotTakenPredictor extends Module {
  val io = IO(new BranchPredIO)

  io.prediction := false.B
}

/**
 * An always taken branch predictor
 *
 */
class AlwaysTakenPredictor extends Module {
  val io = IO(new BranchPredIO)

  io.prediction := true.B
}
