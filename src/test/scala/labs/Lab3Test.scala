// Tests for Lab 3. Feel free to modify and add more tests here.
// If you name your test class something that ends with "TesterLab3" it will
// automatically be run when you use `Lab3 / test` at the sbt prompt.

package dinocpu

import dinocpu.test._

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.RTypeTesterLab3
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.RTypeTesterLab3'
  * }}}
  */
class RTypeTesterLab3 extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.rtype) {
    it should s"run R-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class ITypeTesterLab3 extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.itype) {
    it should s"run I-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class UTypeTesterLab3 extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.utype) {
    it should s"run U-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class MemoryTesterLab3 extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.memory) {
    it should s"run memory-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class RTypeMultiCycleTesterLab3 extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.rtypeMultiCycle) {
    it should s"run multi cycle R-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class ITypeMultiCycleTesterLab3 extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.itypeMultiCycle) {
    it should s"run multi cycle I-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class BranchTesterLab3 extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.branch) {
    it should s"run branch instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class JumpTesterLab3 extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.jump) {
    it should s"run jump instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class MemoryMultiCycleTesterLab3 extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.memoryMultiCycle) {
    it should s"run multi cycle memory instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class ApplicationsTesterLab3 extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.smallApplications) {
    it should s"run application ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

