
package CODCPU

class ImmediateSimpleCPUTester extends CPUFlatSpec {
  val testCases = List[CPUTestCase](
    CPUTestCase("auipc0", 5,  Map(10 -> 1234),
                              Map(10 -> 0),
                              Map(), Map()),
    CPUTestCase("auipc1", 6,  Map(10 -> 1234),
                              Map(10 -> 4),
                              Map(), Map()),
    CPUTestCase("auipc2", 6,  Map(10 -> 1234),
                              Map(10 -> (17 << 12)),
                              Map(), Map()),
    CPUTestCase("auipc3", 6,  Map(10 -> 1234),
                              Map(10 -> ((17 << 12) + 4)),
                              Map(), Map()),
    CPUTestCase("lui0",   5,  Map(10 -> 1234),
                              Map(10 -> 0),
                              Map(), Map()),
    CPUTestCase("lui1",   5,  Map(10 -> 1234),
                              Map(10 -> 4096),
                              Map(), Map()),
    CPUTestCase("addi1",  5,  Map(),
                              Map(0 -> 0, 10 -> 17),
                              Map(), Map()),
    CPUTestCase("addi2",  6,  Map(),
                              Map(0 -> 0, 10 -> 17, 11 -> 93),
                              Map(), Map())
  )
  for (test <- testCases) {
    "Single cycle CPU" should s"run ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "single-cycle") should be(true)
    }
  }
  // for (test <- testCases) {
  //   "Five cycle CPU" should s"run ${test.binary}${test.extraName}" in {
  //     CPUTesterDriver(test, "five-cycle") should be(true)
  //   }
  // }
  // for (test <- testCases) {
  //   "Pipelined CPU" should s"run ${test.binary}${test.extraName}" in {
  //     CPUTesterDriver(test, "single-cycle") should be(true)
  //   }
  // }
}
