// Tests for Lab 3. Feel free to modify and add more tests here.
// If you name your test class something that ends with "TesterLab3" it will
// automatically be run when you use `Lab3 / test` at the sbt prompt.


package dinocpu

import chisel3._

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.FiveCycleRTypeTesterLab3
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.FiveCycleRTypeTesterLab3'
  * }}}
  */
class FiveCycleRTypeTesterLab3 extends CPUFlatSpec {
  behavior of "Five Cycle CPU"
  for (test <- InstTests.rtype) {
    it should s"run R-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "five-cycle") should be(true)
    }
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.FiveCycleITypeTesterLab3
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.FiveCycleITypeTesterLab3'
  * }}}
  *
*/
class FiveCycleITypeTesterLab3 extends CPUFlatSpec {

  val maxInt = BigInt("FFFFFFFF", 16)

  def twoscomp(v: BigInt) : BigInt = {
    if (v < 0) {
      return maxInt + v + 1
    } else {
      return v
    }
  }

  val tests = List[CPUTestCase](
		CPUTestCase("addi1",
                Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                Map(),
								Map(0 -> 0, 10 -> 17),
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
    "Five Cycle CPU" should s"run I-Type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "five-cycle") should be(true)
    }
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.FiveCycleLoadTesterLab3
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.FiveCycleLoadTesterLab3'
  * }}}
  *
*/
class FiveCycleLoadTesterLab3 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
		CPUTestCase("lw1",
                Map("single-cycle" -> 1, "five-cycle" -> 8, "pipelined" -> 5),
                Map(),
								Map(5 -> BigInt("ffffffff", 16)),
								Map(), Map()),
 )
  for (test <- tests) {
    "Five Cycle CPU" should s"run load instruction test ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "five-cycle") should be(true)
    }
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.FiveCycleUTypeTesterLab3
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.FiveCycleUTypeTesterLab3'
  * }}}
  *
*/
class FiveCycleUTypeTesterLab3 extends CPUFlatSpec {

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
  "Five Cycle CPU" should s"run auipc/lui instruction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "five-cycle") should be(true)
	}
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.FiveCycleStoreTesterLab3
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.FiveCycleStoreTesterLab3'
  * }}}
  *
*/
class FiveCycleStoreTesterLab3 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
		CPUTestCase("sw",
                Map("single-cycle" -> 6, "five-cycle" -> 10, "pipelined" -> 10),
                Map(5 -> 1234),
								Map(6 -> 1234),
								Map(), Map(0x100 -> 1234))
 )
  for (test <- tests) {
  "Five Cycle CPU" should s"run add Store instruction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "five-cycle") should be(true)
	}
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.FiveCycleLoadStoreTesterLab3
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.FiveCycleLoadStoreTesterLab3'
  * }}}
  *
*/
class FiveCycleLoadStoreTesterLab3 extends CPUFlatSpec {

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
  "Five Cycle CPU" should s"run load/store insturction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "five-cycle") should be(true)
	}
  }
}

// Unit tests for the Branch control logic
/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.BranchControlTesterLab3
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.BranchControlTesterLab3'
  * }}}
  */

class BranchControlTesterLab3 extends ChiselFlatSpec {
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
  * testOnly dinocpu.FiveCycleBranchTesterLab3
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.FiveCycleBranchTesterLab3'
  * }}}
  *
*/
class FiveCycleBranchTesterLab3 extends CPUFlatSpec {
  behavior of "Five Cycle CPU"
  for (test <- InstTests.branch) {
    it should s"run branch instruction test ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "five-cycle") should be(true)
    }
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.FiveCycleJALTesterLab3
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.FiveCycleJALTesterLab3'
  * }}}
  *
*/
class FiveCycleJALTesterLab3 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
    CPUTestCase("jal",
                Map("single-cycle" -> 2, "five-cycle" -> 20, "pipelined" -> 6),
                Map(5 -> 1234),
								Map(0 -> 0, 5 -> 1234, 6 -> 1234, 1 -> 4),
								Map(), Map())
 )
  for (test <- tests) {
  "Five Cycle CPU" should s"run JAL instruction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "five-cycle") should be(true)
	}
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.FiveCycleJALRTesterLab3
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.FiveCycleJALRTesterLab3'
  * }}}
  *
*/
class FiveCycleJALRTesterLab3 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
    CPUTestCase("jalr0",
                Map("single-cycle" -> 2, "five-cycle" -> 50, "pipelined" -> 6),
                Map(5 -> 1234, 10 -> 28),
								Map(0 -> 0, 5 -> 1234, 6 -> 1234, 1 -> 4),
								Map(), Map()),
    CPUTestCase("jalr1",
                Map("single-cycle" -> 2, "five-cycle" -> 20, "pipelined" -> 6),
                Map(5 -> 1234, 10 -> 20),
								Map(0 -> 0, 5 -> 1234, 6 -> 1234, 1 -> 4),
								Map(), Map())
 )
  for (test <- tests) {
  "Five Cycle CPU" should s"run JALR instruction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "five-cycle") should be(true)
	}
  }
}



