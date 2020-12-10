// Unit tests for the CPU models running "full" RISC-V apps

package dinocpu.test


/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleCPUTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleCPUTester'
  * }}}
  *
  * To run a **single** test from this suite, you can use the -z option to sbt test.
  * The option after the `-z` is a string to search for in the test
  * {{{
  * sbt> testOnly dinocpu.SingleCycleCPUTester -- -z beqFalse
  * }}}
  * Or, to run just the r-type instructions you can use `-z rtype`
  */
class SingleCycleCPUTester extends CPUFlatSpec {
  behavior of "Single Cycle CPU"
  for ((group, tests) <- InstTests.tests) {
    for (test <- tests) {
      it should s"run $group ${test.binary}${test.extraName}" in {
        CPUTesterDriver(test, "single-cycle") should be(true)
      }
    }
  }
}

/** Just like [[SingleCycleCPUTester]], but for the pipelined CPU */
class PipelinedCPUTester extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for ((group, tests) <- InstTests.tests) {
    for (test <- tests) {
      it should s"run $group ${test.binary}${test.extraName}" in {
        CPUTesterDriver(test, "pipelined") should be(true)
      }
    }
  }
}

/** Just like [[SingleCycleCPUTester]], but for the pipelined CPU */
class PipelinedBPTakenCPUTester extends CPUFlatSpec {
  val mytests = Map(
    "branch" -> InstTests.branch,
    "jump" -> InstTests.jump,
		"applications" -> InstTests.smallApplications
  )
  behavior of "Pipelined CPU with always-taken branch predictor"
  for ((group, tests) <- mytests) {
    for (test <- tests) {
      it should s"run $group ${test.binary}${test.extraName}" in {
        CPUTesterDriver(test, "pipelined", "always-taken") should be(true)
      }
    }
  }
}

/** Just like [[SingleCycleCPUTester]], but for the pipelined CPU */
class PipelinedBPLocalCPUTester extends CPUFlatSpec {
  val mytests = Map(
    "branch" -> InstTests.branch,
    "jump" -> InstTests.jump,
		"applications" -> InstTests.smallApplications
  )
  behavior of "Pipelined CPU with local branch predictor"
  for ((group, tests) <- mytests) {
    for (test <- tests) {
      it should s"run $group ${test.binary}${test.extraName}" in {
        CPUTesterDriver(test, "pipelined", "local") should be(true)
      }
    }
  }
}

/** Just like [[SingleCycleCPUTester]], but for the pipelined CPU */
class PipelinedBPGlobalCPUTester extends CPUFlatSpec {
  val mytests = Map(
    "branch" -> InstTests.branch,
    "jump" -> InstTests.jump,
		"applications" -> InstTests.smallApplications
  )
  behavior of "Pipelined CPU with global branch predictor"
  for ((group, tests) <- mytests) {
    for (test <- tests) {
      it should s"run $group ${test.binary}${test.extraName}" in {
        CPUTesterDriver(test, "pipelined", "global") should be(true)
      }
    }
  }
}

class PipelinedFullApplicationTester extends CPUFlatSpec {
  behavior of "Pipelined CPU running full applications"
  for (test <- InstTests.fullApplications) {
    it should s"run ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

/** Just like [[SingleCycleCPUTester]], but for the pipelined CPU */
class PipelinedCombinCPUTester extends CPUFlatSpec {
  behavior of "Pipelined CPU with non-combinational memory"
  for ((group, tests) <- InstTests.tests) {
    for (test <- tests) {
      it should s"run $group ${test.binary}${test.extraName}" in {
        CPUTesterDriver(test, "pipelined-non-combin", "", "combinational", "combinational-port", 0) should be(true)
      }
    }
  }
}

class PipelinedCombinFullApplicationTester extends CPUFlatSpec {
  behavior of "Pipelined CPU with non-combinational memory running full applications"
  for (test <- InstTests.fullApplications) {
    it should s"run ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined-non-combin", "", "combinational", "combinational-port", 0) should be(true)
    }
  }
}

/** Just like [[SingleCycleCPUTester]], but for the pipelined CPU */
class PipelinedNonCombinCPUTester extends CPUFlatSpec {
  behavior of "Pipelined CPU with non-combinational memory"
  for ((group, tests) <- InstTests.tests) {
    for (test <- tests) {
      it should s"run $group ${test.binary}${test.extraName}" in {
        CPUTesterDriver(test, "pipelined-non-combin", "", "non-combinational", "non-combinational-port", 1) should be(true)
      }
    }
  }
}

class PipelinedNonCombinFullApplicationTester extends CPUFlatSpec {
  behavior of "Pipelined CPU with non-combinational memory running full applications"
  for (test <- InstTests.fullApplications) {
    it should s"run ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined-non-combin", "", "non-combinational", "non-combinational-port", 1) should be(true)
    }
  }
}