// Tests for Lab 1. Feel free to modify and add more tests here.
// If you name your test class something that ends with "TesterLab1" it will
// automatically be run when you use `Lab1 / test` at the sbt prompt.

package dinocpu

import chisel3._

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


class ALUControlUnitRTypeTester(c: ALUControl) extends PeekPokeTester(c) {
  private val ctl = c

  // Copied from Patterson and Waterman Figure 2.3
  val tests = List(
    // add,   imm,      Funct7,       Func3,    Control Input
    ( false.B, false.B, "b0000000".U, "b000".U, "b0010".U, "add"),
    ( false.B, false.B, "b0100000".U, "b000".U, "b0011".U, "sub"),
    ( false.B, false.B, "b0000000".U, "b001".U, "b0110".U, "sll"),
    ( false.B, false.B, "b0000000".U, "b010".U, "b0100".U, "slt"),
    ( false.B, false.B, "b0000000".U, "b011".U, "b0101".U, "sltu"),
    ( false.B, false.B, "b0000000".U, "b100".U, "b1001".U, "xor"),
    ( false.B, false.B, "b0000000".U, "b101".U, "b0111".U, "srl"),
    ( false.B, false.B, "b0100000".U, "b101".U, "b1000".U, "sra"),
    ( false.B, false.B, "b0000000".U, "b110".U, "b0001".U, "or"),
    ( false.B, false.B, "b0000000".U, "b111".U, "b0000".U, "and")
  )

  for (t <- tests) {
    poke(ctl.io.add, t._1)
    poke(ctl.io.immediate, t._2)
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
  * Lab1 / testOnly dinocpu.ALUControlTesterLab1
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.ALUControlTesterLab1'
  * }}}
  */
class ALUControlTesterLab1 extends ChiselFlatSpec {
  "ALUControl" should s"match expectations for each intruction type" in {
    Driver(() => new ALUControl) {
      c => new ALUControlUnitRTypeTester(c)
    } should be (true)
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleAddTesterLab1
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleAddTesterLab1'
  * }}}
  */
class SingleCycleAddTesterLab1 extends CPUFlatSpec {
  val test = CPUTestCase("add1",
                Map("single-cycle" -> 1),
                Map(5 -> 1234),
								Map(0 -> 0, 5 -> 1234, 6 -> 1234),
								Map(), Map())
  "Single Cycle CPU" should s"run add test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "single-cycle") should be(true)
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleRTypeTesterLab1
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleRTypeTesterLab1'
  * }}}
  *
  * To run a **single** test from this suite, you can use the -z option to sbt test.
  * The option after the `-z` is a string to search for in the test
  * {{{
  * sbt> testOnly dinocpu.SingleCycleRTypeTesterLab1 -- -z add1
  * }}}
  * Or, to run just the r-type instructions you can use `-z rtype`
  */
class SingleCycleRTypeTesterLab1 extends CPUFlatSpec {
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
  * testOnly dinocpu.SingleCycleMultiCycleTesterLab1
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleMultiCycleTesterLab1'
  * }}}
  *
  * To run a **single** test from this suite, you can use the -z option to sbt test.
  * The option after the `-z` is a string to search for in the test
  * {{{
  * sbt> testOnly dinocpu.SingleCycleMultiCycleTesterLab1 -- -z swapxor
  * }}}
  * Or, to run just the r-type instructions you can use `-z rtype`
  */
class SingleCycleMultiCycleTesterLab1 extends CPUFlatSpec {
  behavior of "Single Cycle CPU"
  for (test <- InstTests.rtypeMultiCycle) {
    it should s"run R-type multi-cycle program ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "single-cycle") should be(true)
    }
  }
}
