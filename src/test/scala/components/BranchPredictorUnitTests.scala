// Unit tests for the Branch predictors

package dinocpu

import chisel3._

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


class LocalPredictorUnitTester(p: LocalPredictor, stream: List[(Int,Boolean,Boolean)]) extends PeekPokeTester(p) {
  var step = 0
  for ((addr, taken, pred) <- stream) {
    poke(p.io.update, false)
    poke(p.io.pc, addr)
    expect(p.io.prediction, pred)
    step(1)
    poke(p.io.update, true)
    poke(p.io.taken, taken)
    step(1)
    step += 1
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.LocalPredictorTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.LocalPredictorTester'
  * }}}
  */
class LocalPredictorTester extends ChiselFlatSpec {

  "Local Branch predictor" should s"match expectations for 2-bit saturating counter tests" in {
    val stream = List(
      /* pc, taken, prediction */
      (0x0,  true, true),
      (0x0,  true, true),
      (0x0,  false, true),
      (0x0,  false, true),
      (0x0,  false, false),
      (0x0,  false, false),
      (0x0,  false, false),
      (0x0,  false, false),
      (0x0,  true, false),
      (0x0,  true, false),
      (0x0,  true, true)
    )
    implicit val conf = new CPUConfig()
    conf.branchPredictor = "local"
    conf.saturatingCounterBits = 2
    Driver(() => new LocalPredictor) {
      p => new LocalPredictorUnitTester(p, stream)
    } should be (true)
  }

  "Local Branch predictor" should s"match expectations for 3-bit saturating counter tests" in {
    val stream = List(
      /* pc, taken, prediction */
      (0x0,  true, true), // 101
      (0x0,  true, true), // 111
      (0x0,  true, true), // 111
      (0x0,  false, true), // 110
      (0x0,  false, true), // 101
      (0x0,  false, true), // 100
      (0x0,  false, true), // 011 (this is the value after this cycle)
      (0x0,  false, false), // 010
      (0x0,  false, false), // 001
      (0x0,  true, false),  // 010
      (0x0,  false, false), // 001
      (0x0,  false, false), // 000
      (0x0,  true, false),  // 001
      (0x0,  true, false), // 010
      (0x0,  true, false), // 011
      (0x0,  true, false), // 100
      (0x0,  false, true)
    )
    implicit val conf = new CPUConfig()
    conf.branchPredictor = "local"
    conf.saturatingCounterBits = 3
    Driver(() => new LocalPredictor) {
      p => new LocalPredictorUnitTester(p, stream)
    } should be (true)
  }

  "Local branch predictor" should "match expectations for multiple addresses" in {
    val stream = List(
      (0x0,  true, true), (0x0,  false, true), (0x0,  false, true), (0x0,  false, false), (0x0,  false, false), (0x0,  false, false), (0x0,  false, false), (0x0,  true, false), (0x0,  true, false), (0x0,  true, true),
      (0x4,  true, true), (0x4,  false, true), (0x4,  false, true), (0x4,  false, false), (0x4,  false, false), (0x4,  false, false), (0x4,  false, false), (0x4,  true, false), (0x4,  true, false), (0x4,  true, true),
      (0x8,  true, true), (0x8,  false, true), (0x8,  false, true), (0x8,  false, false), (0x8,  false, false), (0x8,  false, false), (0x8,  false, false), (0x8,  true, false), (0x8,  true, false), (0x8,  true, true),
      (0xc,  true, true), (0xc,  false, true), (0xc,  false, true), (0xc,  false, false), (0xc,  false, false), (0xc,  false, false), (0xc,  false, false), (0xc,  true, false), (0xc,  true, false), (0xc,  true, true),
      (0x10,  true, true), (0x10,  false, true), (0x10,  false, true), (0x10,  false, false), (0x10,  false, false), (0x10,  false, false), (0x10,  false, false), (0x10,  true, false), (0x10,  true, false), (0x10,  true, true)
    )
    implicit val conf = new CPUConfig()
    conf.branchPredictor = "local"
    conf.saturatingCounterBits = 2
    conf.branchPredTableEntries = 5
    Driver(() => new LocalPredictor) {
      p => new LocalPredictorUnitTester(p, stream)
    } should be (true)
  }

  "Local branch predictor" should "match expectations for aliased addresses" in {
    val stream = List(
      (0x0,  true, true), (0x8,  false, true), (0x0,  false, true), (0x8,  false, false), (0x0,  false, false), (0x8,  false, false), (0x0,  false, false), (0x8,  true, false), (0x0,  true, false), (0x8,  true, true),
      (0x4,  true, true), (0xc,  false, true), (0x4,  false, true), (0xc,  false, false), (0x4,  false, false), (0xc,  false, false), (0x4,  false, false), (0xc,  true, false), (0x4,  true, false), (0xc,  true, true),
    )
    implicit val conf = new CPUConfig()
    conf.branchPredictor = "local"
    conf.saturatingCounterBits = 2
    conf.branchPredTableEntries = 2
    Driver(() => new LocalPredictor) {
      p => new LocalPredictorUnitTester(p, stream)
    } should be (true)
  }
}
