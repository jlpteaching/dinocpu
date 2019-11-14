// Tests for Lab 2. Feel free to modify and add more tests here.
// If you name your test class something that ends with "TesterLab2" it will
// automatically be run when you use `Lab2 / test` at the sbt prompt.

package dinocpu

import chisel3.iotesters.{ChiselFlatSpec, Driver}
import dinocpu.test._
import dinocpu.test.components._


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

  val tests = InstTests.tests("itype")
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
    InstTests.nameMap("lw1"), InstTests.nameMap("lwfwd")
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

  val tests = InstTests.tests("utype")
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
    InstTests.nameMap("sw")
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

  val tests = InstTests.tests("memory")
  for (test <- tests) {
  "Single Cycle CPU" should s"run load/store instruction test ${test.binary}${test.extraName}" in {
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
    InstTests.nameMap("jal")
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
    InstTests.nameMap("jalr0"), InstTests.nameMap("jalr1")
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

  val tests = InstTests.tests("smallApplications")
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



