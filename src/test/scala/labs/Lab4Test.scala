// Tests for Lab 4. Feel free to modify and add more tests here.
// If you name your test class something that ends with "TesterLab4" it will
// automatically be run when you use `Lab3 / test` at the sbt prompt.

package dinocpu

import chisel3.iotesters.{ChiselFlatSpec, Driver}
import dinocpu.components._
import dinocpu.test._
import dinocpu.test.components._

class LocalPredictorUnitTesterLab4 extends CPUFlatSpec {
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
    conf.branchPredTableEntries = 8
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

class GlobalPredictorUnitTesterLab4 extends ChiselFlatSpec {

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

  "Global Branch predictor" should s"match expectations for random tests" in {
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

class SmallApplicationsNotTakenTesterLab4 extends CPUFlatSpec {
  behavior of "Pipelined CPU with an always not taken predictor"
  for (test <- InstTests.smallApplications) {
    it should s"run application ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined", "always-not-taken") should be(true)
    }
  }
}

class LargeApplicationsNotTakenTesterLab4 extends CPUFlatSpec {
  behavior of "Pipelined CPU with an always not taken predictor"
  for (test <- InstTests.fullApplications) {
    it should s"run application ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined", "always-not-taken") should be(true)
    }
  }
}

class SmallApplicationsTakenTesterLab4 extends CPUFlatSpec {
  behavior of "Pipelined CPU with an always taken predictor"
  for (test <- InstTests.smallApplications) {
    it should s"run application ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined", "always-taken") should be(true)
    }
  }
}

class LargeApplicationsTakenTesterLab4 extends CPUFlatSpec {
  behavior of "Pipelined CPU with an always taken predictor"
  for (test <- InstTests.fullApplications) {
    it should s"run application ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined", "always-taken") should be(true)
    }
  }
}

class SmallApplicationsLocalTesterLab4 extends CPUFlatSpec {
  behavior of "Pipelined CPU with a local history predictor"
  for (test <- InstTests.smallApplications) {
    it should s"run application ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined", "local") should be(true)
    }
  }
}

class LargeApplicationsLocalTesterLab4 extends CPUFlatSpec {
  behavior of "Pipelined CPU with a local history predictor"
  for (test <- InstTests.fullApplications) {
    it should s"run application ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined", "local") should be(true)
    }
  }
}

class SmallApplicationsGlobalTesterLab4 extends CPUFlatSpec {
  behavior of "Pipelined CPU with a global history predictor"
  for (test <- InstTests.smallApplications) {
    it should s"run application ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined", "global") should be(true)
    }
  }
}

class LargeApplicationsGlobalTesterLab4 extends CPUFlatSpec {
  behavior of "Pipelined CPU with a global history predictor"
  for (test <- InstTests.fullApplications) {
    it should s"run application ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined", "global") should be(true)
    }
  }
}
