// Unit tests for the Branch predictors

package dinocpu.test.components

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import dinocpu._
import dinocpu.components._

import scala.util.Random


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

class LocalPredictorRandomUnitTester(p: LocalPredictor,
                                      entries: Int,
                                      bits: Int,
                                      tests: Int = 100)
          extends PeekPokeTester(p) {
  val table = Array.fill(entries)(1 << (bits-1))
  val max = (1 << bits) - 1

  val r = new Random()

  var last = 0

  for (i <- 1 to tests) {
    poke(p.io.update, false)
    val pc = r.nextInt(1000000) & 0xfffffff7
    poke(p.io.pc, pc)
    val entry = (pc >> 2) & (entries - 1)
    expect(p.io.prediction, table(entry) >= (1 << (bits-1)))
    step(1)
    val taken = r.nextInt(2)
    poke(p.io.pc, r.nextInt(1000000) & 0xfffffff7) // should ignore this value
    poke(p.io.update, true)
    poke(p.io.taken, taken)

    if (taken == 1) table(entry) += 1
    else table(entry) -= 1

    if (table(entry) > max) table(entry) = max
    if (table(entry) < 0) table(entry) = 0

    step(1)
  }
}

class GlobalPredictorUnitTester(p: GlobalHistoryPredictor, stream: List[(Int,Boolean,Boolean)]) extends PeekPokeTester(p) {

  var step = 0
  for ((addr, taken, pred) <- stream) {
    poke(p.io.update, false)
    poke(p.io.pc, addr)
    step(1)
    expect(p.io.prediction, pred)
    step(1)
    poke(p.io.update, true)
    poke(p.io.taken, taken)
    step(1)
    step += 1
  }
}

class GlobalPredictorRandomUnitTester(p: GlobalHistoryPredictor,
                                      entries: Int,
                                      bits: Int)
          extends PeekPokeTester(p) {
  val table = Array.fill(entries)(1 << (bits-1))
  val max = (1 << bits) - 1

  val r = new Random()

  var last = 0

  for (i <- 1 to 100) {
    poke(p.io.update, false)
    poke(p.io.pc, r.nextInt(1000000))
    expect(p.io.prediction, table(last) >= (1 << (bits-1)))
    step(1)
    val taken = r.nextInt(2)
    poke(p.io.update, true)
    poke(p.io.taken, taken)

    if (taken == 1) table(last) += 1
    else table(last) -= 1

    if (table(last) > max) table(last) = max
    if (table(last) < 0) table(last) = 0

    last = (last << 1 | taken) & (entries - 1)
    step(1)
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

class LocalPredictorRandomTester extends ChiselFlatSpec {
  "Local branch predictory" should "match expectations for random 2-bit 8 entry tests" in {
    implicit val conf = new CPUConfig()
    conf.branchPredictor = "local"
    conf.saturatingCounterBits = 2
    conf.branchPredTableEntries = 8
    Driver(() => new LocalPredictor) {
      p => new LocalPredictorRandomUnitTester(p, 8, 2)
    } should be (true)
  }
  "Local branch predictory" should "match expectations for random 1-bit 2 entry tests" in {
    implicit val conf = new CPUConfig()
    conf.branchPredictor = "local"
    conf.saturatingCounterBits = 1
    conf.branchPredTableEntries = 2
    Driver(() => new LocalPredictor) {
      p => new LocalPredictorRandomUnitTester(p, 2, 1)
    } should be (true)
  }
  "Local branch predictory" should "match expectations for random 3-bit 256 entry tests" in {
    implicit val conf = new CPUConfig()
    conf.branchPredictor = "local"
    conf.saturatingCounterBits = 3
    conf.branchPredTableEntries = 256
    Driver(() => new LocalPredictor) {
      p => new LocalPredictorRandomUnitTester(p, 256, 3, 1000)
    } should be (true)
  }
}

class GlobalPredictorTester extends ChiselFlatSpec {

  "Global Branch predictor" should s"match expectations for 2-bit saturating counter tests" in {
    val stream = List(
      /* pc, taken, prediction */
      (0x0,  true, true),  // 00: 11
      (0x0,  true, true), // 01: 11
      (0x0,  false, true), // 11: 01
      (0x0,  true, true), // 10: 11
      (0x0,  true, true), // 01: 11
      (0x0,  false, false), // 11: 00
      (0x0,  true, true), // 10: 11
      (0x0,  true, true), // 01: 11
      (0x0,  true, false),  // 11: 01
      (0x0,  true, false), // 11
      (0x0,  true, true), // 11
      (0x0,  true, true),  // 11: 11
      (0x0,  true, true),
      (0x0,  true, true),
      (0x0,  true, true),  // 11: 11
      (0x0,  true, true), // 11: 10
      (0x0,  true, true), // 11: 11
      (0x0,  false, true)  // 11: 10
    )
    implicit val conf = new CPUConfig()
    conf.branchPredictor = "global"
    conf.saturatingCounterBits = 2
    conf.branchPredTableEntries = 4
    Driver(() => new GlobalHistoryPredictor) {
      p => new GlobalPredictorUnitTester(p, stream)
    } should be (true)
  }
}

class GlobalPredictorRandomTester extends ChiselFlatSpec {

  "Global Branch predictor" should s"match expectations for 2-bit saturating counter tests" in {
    implicit val conf = new CPUConfig()
    conf.branchPredictor = "global"
    conf.saturatingCounterBits = 2
    conf.branchPredTableEntries = 8
    Driver(() => new GlobalHistoryPredictor) {
      p => new GlobalPredictorRandomUnitTester(p, 8, 2)
    } should be (true)
  }

  "Global Branch predictor" should s"match expectations for 1-bit saturating counter tests" in {
    implicit val conf = new CPUConfig()
    conf.branchPredictor = "global"
    conf.saturatingCounterBits = 1
    conf.branchPredTableEntries = 8
    Driver(() => new GlobalHistoryPredictor) {
      p => new GlobalPredictorRandomUnitTester(p, 8, 1)
    } should be (true)
  }

  "Global Branch predictor" should s"match expectations for 3-bit saturating counter tests 16 entries" in {
    implicit val conf = new CPUConfig()
    conf.branchPredictor = "global"
    conf.saturatingCounterBits = 3
    conf.branchPredTableEntries = 16
    Driver(() => new GlobalHistoryPredictor) {
      p => new GlobalPredictorRandomUnitTester(p, 16, 3)
    } should be (true)
  }
}
