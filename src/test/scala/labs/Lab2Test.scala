// Tests for Lab 2. Feel free to modify and add more tests here.
// If you name your test class something that ends with "TesterLab2" it will
// automatically be run when you use `Lab2 / test` at the sbt prompt.

package dinocpu

import dinocpu._
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import dinocpu.components._
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

class ALUControlUnitBTypeTester(c: ALUControl) extends PeekPokeTester(c) {
  private val ctl = c

  // Copied from Patterson and Waterman Figure 2.3
  val tests = List(
    // alu,   itype,    Funct7,       Func3,    Control Input
    (  1.U, false.B,  "b0000000".U, "b000".U, "b1101".U, "beq"),
    (  1.U, false.B,  "b0000000".U, "b001".U, "b1110".U, "bne"),
    (  1.U, false.B,  "b0000000".U, "b100".U, "b1000".U, "blt"),
    (  1.U, false.B,  "b0000000".U, "b101".U, "b1011".U, "bge"),
    (  1.U, false.B,  "b0000000".U, "b110".U, "b0101".U, "bltu"),
    (  1.U, false.B,  "b0000000".U, "b111".U, "b1100".U, "bgeu")
  )

  for (t <- tests) {
    poke(ctl.io.aluop, t._1)
    poke(ctl.io.itype, t._2)
    poke(ctl.io.funct7, t._3)
    poke(ctl.io.funct3, t._4)
    step(1)
    expect(ctl.io.operation, t._5, s"${t._6} wrong")
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * Lab2 / testOnly dinocpu.ALUControlTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.ALUControlTesterLab2'
  * }}}
  */
class ALUControlTesterLab2 extends ChiselFlatSpec {
  "ALUControl" should s"match expectations for each intruction type" in {
    Driver(() => new ALUControl) {
      c => new ALUControlUnitBTypeTester(c)
    } should be (true)
  }
} 



