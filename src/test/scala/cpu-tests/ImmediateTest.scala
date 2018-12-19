
package CODCPU

class ImmediateSimpleCPUTester extends CPUFlatSpec {
  val testCases = List[CPUTestCase](
    CPUTestCase("add1",   5,  Map(5 -> 1234),
                              Map(0 -> 0, 5 -> 1234, 6 -> 1234),
                              Map(), Map())
  )
  for (test <- testCases) {
    "Simple CPU" should s"run ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}
