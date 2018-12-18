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
  */
class SingleCycleCPUTester extends CPUFlatSpec {
  for (test <- CPUTesterDriver.testCases) {
    "Single Cycle CPU" should s"run ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "single-cycle") should be(true)
    }
  }
}
