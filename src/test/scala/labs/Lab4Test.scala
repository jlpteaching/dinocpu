// Tests for Lab 4. Feel free to modify and add more tests here.
// If you name your test class something that ends with "TesterLab4" it will
// automatically be run when you use `Lab4 / test` at the sbt prompt.

package dinocpu

import dinocpu.test._

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * Lab4 / testOnly dinocpu.SmallTestsTesterLab4
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab4 / testOnly dinocpu.SmallTestsTesterLab4
  * }}}
  */
class SmallTestsTesterLab4 extends CPUFlatSpec {
  behavior of "Dual-issue Pipelined CPU"
  for ((group, tests) <- InstTests.tests) {
    for (test <- tests) {
      it should s"run $group ${test.binary}${test.extraName}" in {
        CPUTesterDriver(test, "pipelined-dual-issue") should be(true)
      }
    }
  }
}

class DualIssueForwardingTesterLab4 extends CPUFlatSpec {
  behavior of "Dual-issue Pipelined CPU"
  for (test <- InstTests.dualIssue) {
    it should s"run test ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined-dual-issue") should be(true)
    }
  }
}

class FullApplicationsTesterLab4 extends CPUFlatSpec {
  behavior of "Dual-issue Pipelined CPU"
  for (test <- InstTests.fullApplications) {
    it should s"run full application ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined-dual-issue") should be(true)
    }
  }
}

class LoopsUnrolledFullApplicationsTesterLab4 extends CPUFlatSpec {
  behavior of "Dual-issue Pipelined CPU"
  for (test <- InstTests.loopsUnrolledFullApplications) {
    it should s"run full application ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined-dual-issue") should be(true)
    }
  }
}
