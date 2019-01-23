// Tests for Lab 2. Feel free to modify and add more tests here.
// If you name your test class something that ends with "TesterLab2" it will
// automatically be run when you use `Lab2 / test` at the sbt prompt.


package dinocpu

import chisel3._

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleRTypeTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleRTypeTesterLab2'
  * }}}
  */
class SingleCycleRTypeTesterLab2 extends CPUFlatSpec {
  behavior of "Single Cycle CPU"
  for (test <- InstTests.rtype) {
    it should s"run R-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "single-cycle") should be(true)
    }
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleITypeTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleITypeTesterLab2'
  * }}}
  *
*/
class SingleCycleITypeTesterLab2 extends CPUFlatSpec {

  val maxInt = BigInt("FFFFFFFF", 16)

  def twoscomp(v: BigInt) : BigInt = {
    if (v < 0) {
      return maxInt + v + 1
    } else {
      return v
    }
  }

  val tests = List[CPUTestCase](CPUTestCase("add1",
                Map("single-cycle" -> 1),
                Map(5 -> 1234),
								Map(0 -> 0, 5 -> 1234, 6 -> 1234),
								Map(), Map()),
		CPUTestCase("addi1",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(),
								Map(0 -> 0, 10 -> 17),
								Map(), Map()),
		CPUTestCase("addi2",
                Map("single-cycle" -> 2, "five-cycle" -> 0, "pipelined" -> 6),
                Map(),
								Map(0 -> 0, 10 -> 17, 11 -> 93),
								Map(), Map()),
		CPUTestCase("slli",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(5 -> 1),
								Map(0 -> 0, 5 -> 1, 6 -> 128),
								Map(), Map()),
		CPUTestCase("srai",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(5 -> 1024),
								Map(0 -> 0, 5 -> 1024, 6 -> 8),
								Map(), Map()),
		CPUTestCase("srai",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(5 -> twoscomp(-1024)),
								Map(0 -> 0, 5 -> twoscomp(-1024), 6 -> twoscomp(-8)),
								Map(), Map(), "-negative"),
		CPUTestCase("srli",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(5 -> 128),
								Map(0 -> 0, 5 -> 128, 6 -> 1),
								Map(), Map()),
		CPUTestCase("andi",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(5 -> 456),
								Map(0 -> 0, 5 -> 456, 6 -> 200),
								Map(), Map()),
		CPUTestCase("ori",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(5 -> 456),
								Map(0 -> 0, 5 -> 456, 6 -> 511),
								Map(), Map()),
		CPUTestCase("xori",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(5 -> 456),
								Map(0 -> 0, 5 -> 456, 6 -> 311),
								Map(), Map()),
		CPUTestCase("slti",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(5 -> twoscomp(-1)),
								Map(0 -> 0, 5 -> twoscomp(-1),6->1),
								Map(), Map()),
		CPUTestCase("sltiu",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(5 -> twoscomp(-1)),
								Map(0 -> 0, 5 -> twoscomp(-1), 6 -> 0),
								Map(), Map())
 )
  for (test <- tests) {
    "Single Cycle CPU" should s"run I-Type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "single-cycle") should be(true)
    }
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleLoadTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleLoadTesterLab2'
  * }}}
  *
*/
class SingleCycleLoadTesterLab2 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
		CPUTestCase("lw1",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(),
								Map(5 -> BigInt("ffffffff", 16)),
								Map(), Map()),
		CPUTestCase("lwfwd",
                Map("single-cycle" -> 2, "five-cycle" -> 0, "pipelined" -> 7),
                Map(5 -> BigInt("ffffffff", 16), 10 -> 5),
								Map(5 -> 1, 10 -> 6),
								Map(), Map())
 )
  for (test <- tests) {
    "Single Cycle CPU" should s"run load instruction test ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "single-cycle") should be(true)
    }
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleUTypeTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleUTypeTesterLab2'
  * }}}
  *
*/
class SingleCycleUTypeTesterLab2 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
		CPUTestCase("auipc0",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(10 -> 1234),
								Map(10 -> 0),
								Map(), Map()),
		CPUTestCase("auipc1",
                Map("single-cycle" -> 2, "five-cycle" -> 6, "pipelined" -> 6),
                Map(10 -> 1234),
								Map(10 -> 4),
								Map(), Map()),
		CPUTestCase("auipc2",
                Map("single-cycle" -> 2, "five-cycle" -> 6, "pipelined" -> 6),
                Map(10 -> 1234),
								Map(10 -> (17 << 12)),
								Map(), Map()),
		CPUTestCase("auipc3",
                Map("single-cycle" -> 2, "five-cycle" -> 6, "pipelined" -> 6),
                Map(10 -> 1234),
								Map(10 -> ((17 << 12) + 4)),
								Map(), Map()),
		CPUTestCase("lui0",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(10 -> 1234),
								Map(10 -> 0),
								Map(), Map()),
		CPUTestCase("lui1",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(10 -> 1234),
								Map(10 -> 4096),
								Map(), Map())
 )
  for (test <- tests) {
  "Single Cycle CPU" should s"run auipc/lui instruction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "single-cycle") should be(true)
	}
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleStoreTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleStoreTesterLab2'
  * }}}
  *
*/
class SingleCycleStoreTesterLab2 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
		CPUTestCase("sw",
                Map("single-cycle" -> 6, "five-cycle" -> 10, "pipelined" -> 10),
                Map(5 -> 1234),
								Map(6 -> 1234),
								Map(), Map(0x100 -> 1234))
 )
  for (test <- tests) {
  "Single Cycle CPU" should s"run add Store instruction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "single-cycle") should be(true)
	}
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleLoadStoreTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleLoadStoreTesterLab2'
  * }}}
  *
*/
class SingleCycleLoadStoreTesterLab2 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
		CPUTestCase("lb",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(),
								Map(5 -> BigInt("04", 16)),
								Map(), Map()),
		CPUTestCase("lh",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(),
								Map(5 -> BigInt("0304", 16)),
								Map(), Map()),
		CPUTestCase("lbu",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(),
								Map(5 -> BigInt("f4", 16)),
								Map(), Map()),
		CPUTestCase("lhu",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(),
								Map(5 -> BigInt("f3f4", 16)),
								Map(), Map()),
		CPUTestCase("lb1",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(),
								Map(5 -> BigInt("fffffff4", 16)),
								Map(), Map()),
		CPUTestCase("lh1",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(),
								Map(5 -> BigInt("fffff3f4", 16)),
								Map(), Map()),
		CPUTestCase("sb",
                Map("single-cycle" -> 6, "five-cycle" -> 10, "pipelined" -> 10),
                Map(5 -> 1),
								Map(6 -> 1),
								Map(), Map(0x100 -> BigInt("ffffff01", 16))),
		CPUTestCase("sh",
                Map("single-cycle" -> 6, "five-cycle" -> 10, "pipelined" -> 10),
                Map(5 -> 1),
								Map(6 -> 1),
								Map(), Map(0x100 -> BigInt("ffff0001", 16)))
 )
  for (test <- tests) {
  "Single Cycle CPU" should s"run load/store insturction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "single-cycle") should be(true)
	}
  }
}

// Unit tests for the Branch control logic
/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.BranchControlTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.BranchControlTesterLab2'
  * }}}
  */

class BranchControlTesterLab2 extends ChiselFlatSpec {
  "BranchControl" should "correctly check when branching for each opcode" in {
    Driver(() => new BranchControl) {
      c => new BranchControlUnitTester(c)
    } should be (true)
  }
  "BranchControl" should "should never branch" in {
    Driver(() => new BranchControl) {
      c => new BranchControlUnitTester(c, false)
    } should be (true)
  }

}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleBranchTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleBranchTesterLab2'
  * }}}
  *
*/
class SingleCycleBranchTesterLab2 extends CPUFlatSpec {
  behavior of "Single Cycle CPU"
  for (test <- InstTests.branch) {
    it should s"run branch instruction test ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "single-cycle") should be(true)
    }
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleJALTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleJALTesterLab2'
  * }}}
  *
*/
class SingleCycleJALTesterLab2 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
    CPUTestCase("jal",
                Map("single-cycle" -> 2, "five-cycle" -> 6, "pipelined" -> 6),
                Map(5 -> 1234),
								Map(0 -> 0, 5 -> 1234, 6 -> 1234, 1 -> 4),
								Map(), Map())
 )
  for (test <- tests) {
  "Single Cycle CPU" should s"run JAL instruction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "single-cycle") should be(true)
	}
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleJALRTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleJALRTesterLab2'
  * }}}
  *
*/
class SingleCycleJALRTesterLab2 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
    CPUTestCase("jalr0",
                Map("single-cycle" -> 2, "five-cycle" -> 6, "pipelined" -> 6),
                Map(5 -> 1234, 10 -> 28),
								Map(0 -> 0, 5 -> 1234, 6 -> 1234, 1 -> 4),
								Map(), Map()),
    CPUTestCase("jalr1",
                Map("single-cycle" -> 2, "five-cycle" -> 6, "pipelined" -> 6),
                Map(5 -> 1234, 10 -> 20),
								Map(0 -> 0, 5 -> 1234, 6 -> 1234, 1 -> 4),
								Map(), Map())
 )
  for (test <- tests) {
  "Single Cycle CPU" should s"run JALR instruction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "single-cycle") should be(true)
	}
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleApplicationsTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleApplicationsTesterLab2'
  * }}}
  *
*/
class SingleCycleApplicationsTesterLab2 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
    CPUTestCase("fibonacci",
              	Map("single-cycle" -> 300, "five-cycle" -> 6, "pipelined" -> 6),
              	Map(6->11),
								Map(6->11,5->89),
								Map(), Map()),
    CPUTestCase("naturalsum",
               	Map("single-cycle" -> 200, "five-cycle" -> 6, "pipelined" -> 6),
                Map(),
								Map(5->55),
								Map(), Map()),
    CPUTestCase("multiplier",
          	Map("single-cycle" -> 1000, "five-cycle" -> 6, "pipelined" -> 6),
        	Map(5->23,6->20),
								Map(5->23*20),
								Map(), Map()),
    CPUTestCase("divider",
                Map("single-cycle" -> 1000, "five-cycle" -> 6, "pipelined" -> 6),
                Map(5->1260,6->30),
								Map(7->42),
								Map(), Map())

 )
  for (test <- tests) {
  "Single Cycle CPU" should s"run application test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "single-cycle") should be(true)
	}
  }
}

// Unit tests for the main control logic

/*
**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.ControlTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.ControlTesterLab2'
  * }}}
  */
class ControlTesterLab2 extends ChiselFlatSpec {
  "Control" should s"match expectations" in {
    Driver(() => new Control) {
      c => new ControlUnitTester(c)
    } should be (true)
  }
}



