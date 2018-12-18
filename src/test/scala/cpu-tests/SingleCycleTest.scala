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
  val testCases = List[CPUTestCase](
    CPUTestCase("add1",   1,  Map(5 -> 1234),
                              Map(0 -> 0, 5 -> 1234, 6 -> 1234),
                              Map(), Map()),
    CPUTestCase("add2",   1,  Map(5 -> 1234, 20 -> 5678),
                              Map(0 -> 0, 10 -> 6912),
                              Map(), Map()),
    CPUTestCase("add0",   1,  Map(5 -> 1234, 6 -> 3456),
                              Map(0 -> 0, 5 -> 1234, 6 -> 3456),
                              Map(), Map()),
    CPUTestCase("addfwd", 10, Map(5 -> 1, 10 -> 0),
                              Map(5 -> 1, 10 -> 10),
                              Map(), Map()),
    CPUTestCase("and",    1,  Map(5 -> 1234, 6 -> 5678),
                              Map(7 -> 1026),
                              Map(), Map()),
    CPUTestCase("beq",    3,  Map(5 -> 1234, 6 -> 1, 7 -> 5678, 8 -> 9012),
                              Map(5 -> 0, 6 -> 1, 7 -> 5678, 8 -> 9012),
                              Map(), Map(), "False"),
    CPUTestCase("beq",    3,  Map(5 -> 1234, 6 -> 1, 7 -> 5678, 28 -> 5678),
                              Map(5 -> 1235, 6 -> 1, 7 -> 5678, 28 -> 5678),
                              Map(), Map(), "True"),
    CPUTestCase("lw1",    1,  Map(),
                              Map(5 -> BigInt("ffffffff", 16)),
                              Map(), Map()),
    CPUTestCase("lwfwd",  2,  Map(5 -> BigInt("ffffffff", 16), 10 -> 5),
                              Map(5 -> 1, 10 -> 6),
                              Map(), Map()),
    CPUTestCase("or",     1,  Map(5 -> 1234, 6 -> 5678),
                              Map(7 -> 5886),
                              Map(), Map()),
    CPUTestCase("sub",    1,  Map(5 -> 1234, 6 -> 5678),
                              Map(7 -> BigInt("FFFFEEA4", 16)),
                              Map(), Map()),
    CPUTestCase("sw",     6,  Map(5 -> 1234),
                              Map(6 -> 1234),
                              Map(), Map(0x100 -> 1234))
  )
  for (test <- testCases) {
    "Single Cycle CPU" should s"run ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "single-cycle") should be(true)
    }
  }
}
