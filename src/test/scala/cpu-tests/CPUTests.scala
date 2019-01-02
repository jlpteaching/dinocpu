// Unit tests for the CPU models running "full" RISC-V apps

package CODCPU

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly CODCPU.SingleCycleCPUTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly CODCPU.SingleCycleCPUTester'
  * }}}
  *
  * To run a **single** test from this suite, you can use the -z option to sbt test.
  * The option after the `-z` is a string to search for in the test
  * {{{
  * sbt> testOnly CODCPU.SingleCycleCPUTester -- -z beqFalse
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

/** Just like [[SingleCycleCPUTester]], but for the five cycle CPU */
class FiveCycleCPUTester extends CPUFlatSpec {
  behavior of "Five Cycle CPU"
  for ((group, tests) <- InstTests.tests) {
    for (test <- tests if test.cycles("five-cycle") > 0) {
      it should s"run $group ${test.binary}${test.extraName}" in {
        CPUTesterDriver(test, "five-cycle") should be(true)
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
