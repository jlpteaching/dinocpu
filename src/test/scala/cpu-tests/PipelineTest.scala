
package CODCPU

class PipelineCPUTester extends CPUFlatSpec {
  val testCases = List[CPUTestCase](
    CPUTestCase("add1",   5,  Map(5 -> 1234),
                              Map(0 -> 0, 5 -> 1234, 6 -> 1234),
                              Map(), Map()),
    CPUTestCase("add2",   5,  Map(5 -> 1234, 20 -> 5678),
                              Map(0 -> 0, 10 -> 6912),
                              Map(), Map()),
    CPUTestCase("add0",   5,  Map(5 -> 1234, 6 -> 3456),
                              Map(0 -> 0, 5 -> 1234, 6 -> 3456),
                              Map(), Map()),
    CPUTestCase("addfwd", 14, Map(5 -> 1, 10 -> 0),
                              Map(5 -> 1, 10 -> 10),
                              Map(), Map()),
    CPUTestCase("and",    5,  Map(5 -> 1234, 6 -> 5678),
                              Map(7 -> 1026),
                              Map(), Map()),
    CPUTestCase("beq",    7,  Map(5 -> 1234, 6 -> 1, 7 -> 5678, 8 -> 9012),
                              Map(5 -> 0, 6 -> 1, 7 -> 5678, 8 -> 9012),
                              Map(), Map(), "False"),
    CPUTestCase("beq",    9,  Map(5 -> 1234, 6 -> 1, 7 -> 5678, 28 -> 5678),
                              Map(5 -> 1235, 6 -> 1, 7 -> 5678, 28 -> 5678),
                              Map(), Map(), "True"),
    CPUTestCase("lw1",    5,  Map(),
                              Map(5 -> BigInt("ffffffff", 16)),
                              Map(), Map()),
    CPUTestCase("lwfwd",  7,  Map(5 -> BigInt("ffffffff", 16), 10 -> 5),
                              Map(5 -> 1, 10 -> 6),
                              Map(), Map()),
    CPUTestCase("or",     5,  Map(5 -> 1234, 6 -> 5678),
                              Map(7 -> 5886),
                              Map(), Map()),
    CPUTestCase("sub",    5,  Map(5 -> 1234, 6 -> 5678),
                              Map(7 -> BigInt("FFFFEEA4", 16)),
                              Map(), Map()),
    CPUTestCase("sw",    10,  Map(5 -> 1234),
                              Map(6 -> 1234),
                              Map(), Map(0x100 -> 1234))
  )
  for (test <- testCases) {
    "Pipeline CPU" should s"run ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}
