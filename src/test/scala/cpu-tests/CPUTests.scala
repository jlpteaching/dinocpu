// Unit tests for the CPU models running "full" RISC-V apps

package dinocpu

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
