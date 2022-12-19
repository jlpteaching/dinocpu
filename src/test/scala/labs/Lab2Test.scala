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
  * Lab2 / testOnly dinocpu.SingleCycleRTypeTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.SingleCycleRTypeTesterLab2'
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
  * Lab2 / testOnly dinocpu.SingleCycleITypeTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.SingleCycleITypeTesterLab2'
  * }}}
  *
*/
class SingleCycleITypeTesterLab2 extends CPUFlatSpec {

  val maxInt = BigInt("FFFFFFFFFFFFFFFF", 16)

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
  * Lab2 / testOnly dinocpu.SingleCycleLoadTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.SingleCycleLoadTesterLab2'
  * }}}
  *
*/
class SingleCycleLoadTesterLab2 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
    InstTests.nameMap("ld1"), InstTests.nameMap("ld2"), InstTests.nameMap("ldfwd")
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
  * Lab2 / testOnly dinocpu.SingleCycleUTypeTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.SingleCycleUTypeTesterLab2'
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
  * Lab2 / testOnly dinocpu.SingleCycleStoreTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.SingleCycleStoreTesterLab2'
  * }}}
  *
*/
class SingleCycleStoreTesterLab2 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
    InstTests.nameMap("sd1"), InstTests.nameMap("sd2")
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
  * Lab2 / testOnly dinocpu.SingleCycleLoadStoreTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.SingleCycleLoadStoreTesterLab2'
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
  * Lab2 / testOnly dinocpu.SingleCycleBranchTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.SingleCycleBranchTesterLab2'
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
  * Lab2 / testOnly dinocpu.SingleCycleJALTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.SingleCycleJALTesterLab2'
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
  * Lab2 / testOnly dinocpu.SingleCycleJALRTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.SingleCycleJALRTesterLab2'
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
  * Lab2 / testOnly dinocpu.SingleCycleApplicationsTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.SingleCycleApplicationsTesterLab2'
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
  * Lab2 / testOnly dinocpu.ControlTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.ControlTesterLab2'
  * }}}
  */
class ControlTesterLab2 extends ChiselFlatSpec {
  "Control" should s"match expectations" in {
    Driver(() => new Control) {
      c => new ControlUnitTester(c)
    } should be (true)
  }
}

class NextPCBrTester(c: NextPC) extends PeekPokeTester(c) {
  private val ctl = c
  
  val tests = List(
    // branch, jumptype, inputx, inputy,   funct3,     pc,   imm, nextpc,   taken
    (  true.B,      0.U,   13.U,    9.U, "b000".U,   20.U,  16.U,   24.U, false.B, "beq0"),
    (  true.B,      0.U,  133.U,  133.U, "b000".U,   20.U,  16.U,   36.U,  true.B,  "beq1"),
    (  true.B,      0.U,   13.U,    9.U, "b001".U,   20.U,  16.U,   36.U,  true.B,  "bne"),
    (  true.B,      0.U,   13.U,    9.U, "b100".U,   20.U,  16.U,   24.U, false.B, "blt"),
    (  true.B,      0.U,   13.U,    9.U, "b101".U,   20.U,  16.U,   36.U,  true.B,  "bge"),
    (  true.B,      0.U,   13.U,    9.U, "b110".U,   20.U,  16.U,   24.U, false.B, "bltu"),
    (  true.B,      0.U,   13.U,    9.U, "b111".U,   20.U,  16.U,   36.U,  true.B,  "bgeu")
  )
  
  for (t <- tests) {
    poke(ctl.io.branch,   t._1)
    poke(ctl.io.jumptype, t._2)
    poke(ctl.io.inputx,   t._3)
    poke(ctl.io.inputy,   t._4)
    poke(ctl.io.funct3,   t._5)
    poke(ctl.io.pc,       t._6)
    poke(ctl.io.imm,      t._7)
    step(1)
    expect(ctl.io.nextpc, t._8, s"${t._10} wrong")
    expect(ctl.io.taken,  t._9, s"${t._10} wrong")
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * Lab2 / testOnly dinocpu.NextPCBranchTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.NextPCBranchTesterLab2'
  * }}}
  */

class NextPCBranchTesterLab2 extends ChiselFlatSpec {
  "NextPC" should s"match expectations for each intruction type" in {
    Driver(() => new NextPC) {
      c => new NextPCBrTester(c)
    } should be (true)
  }
}

class NextPCJalTester(c: NextPC) extends PeekPokeTester(c) {
  private val ctl = c
  val tests = List(
    //    branch, jumptype, inputx, inputy,   funct3,   pc,   imm, nextpc,   taken
      (  false.B,      2.U,   13.U,    9.U, "b000".U, 60.U,  16.U,   76.U,  true.B, "jal0"),
      (  false.B,      2.U,  133.U,  133.U, "b000".U, 40.U ,  8.U,   48.U,  true.B, "jal1")
  )

  for (t <- tests) {
    poke(ctl.io.branch,   t._1)
    poke(ctl.io.jumptype, t._2)
    poke(ctl.io.inputx,   t._3)
    poke(ctl.io.inputy,   t._4)
    poke(ctl.io.funct3,   t._5)
    poke(ctl.io.pc,       t._6)
    poke(ctl.io.imm,      t._7)
    step(1)
    expect(ctl.io.nextpc, t._8, s"${t._10} wrong")
    expect(ctl.io.taken,  t._9, s"${t._10} wrong")
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * Lab2 / testOnly dinocpu.NextPCJalTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.NextPCJalTesterLab2'
  * }}}
  */

class NextPCJalTesterLab2 extends ChiselFlatSpec {
  "NextPC" should s"match expectations for each intruction type" in {
    Driver(() => new NextPC) {
      c => new NextPCJalTester(c)
    } should be (true)
  }
}

class NextPCJalrTester(c: NextPC) extends PeekPokeTester(c) {
  private val ctl = c

  val tests = List(
    //  branch, jumptype, inputx, inputy, funct3,    pc,  imm, nextpc,    taken
    (  false.B,      3.U,   44.U,   99.U,    0.U, 100.U, 16.U,   60.U,   true.B, "jalr0"),
    (  false.B,      3.U,  112.U,   19.U,    0.U,  56.U, 12.U,  124.U,   true.B, "jalr1")
  )

  for (t <- tests) {
    poke(ctl.io.branch,   t._1)
    poke(ctl.io.jumptype, t._2)
    poke(ctl.io.inputx,   t._3)
    poke(ctl.io.inputy,   t._4)
    poke(ctl.io.funct3,   t._5)
    poke(ctl.io.pc,       t._6)
    poke(ctl.io.imm,      t._7)
    step(1)
    expect(ctl.io.nextpc, t._8, s"${t._10} wrong")
    expect(ctl.io.taken,  t._9, s"${t._10} wrong")
  }
}


/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * Lab2 / testOnly dinocpu.NextPCJalrTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.NextPCJalrTesterLab2'
  * }}}
  */

class NextPCJalrTesterLab2 extends ChiselFlatSpec {
  "NextPC" should s"match expectations for each intruction type" in {
    Driver(() => new NextPC) {
      c => new NextPCJalrTester(c)
    } should be (true)
  }
}


class NextPCTester(c: NextPC) extends PeekPokeTester(c) {
  private val ctl = c

  val tests = List(
    // branch, jumptype, inputx, inputy,   funct3,    pc,   imm, nextpc,   taken
    ( false.B,      0.U,  143.U,   92.U, "b000".U, 200.U, 164.U,  204.U, false.B, "none0"),
    (  true.B,      0.U,   13.U,    9.U, "b000".U,  20.U,  16.U,   24.U, false.B, "beqF"),
    (  true.B,      0.U,  133.U,  133.U, "b000".U,  28.U,  40.U,   68.U,  true.B,  "beqT"),
    (  true.B,      0.U,   11.U,    7.U, "b001".U,  36.U,  12.U,   48.U,  true.B,  "bneT"),
    (  true.B,      0.U,   14.U,   14.U, "b001".U,  52.U,   8.U,   56.U, false.B, "bneF"),
    (  true.B,      0.U,   13.U,    9.U, "b100".U,  24.U,  20.U,   28.U, false.B, "bltF"),
    (  true.B,      0.U,    5.U,    7.U, "b100".U,  12.U,   8.U,   20.U,  true.B,  "bltT"),
    (  true.B,      0.U,  130.U,  130.U, "b101".U,  24.U,  16.U,   40.U,  true.B,  "bgeT"),
    (  true.B,      0.U,   13.U,   94.U, "b101".U,  28.U,  16.U,   32.U, false.B, "bgeF"),
    (  true.B,      0.U,   13.U,    9.U, "b110".U,  20.U,  16.U,   24.U, false.B, "bltuF"),
    (  true.B,      0.U,    4.U,    8.U, "b110".U,   4.U,  24.U,   28.U,  true.B,  "bltuT"),
    ( false.B,      0.U,  151.U,   55.U, "b000".U, 164.U,  12.U,  168.U, false.B, "none1"),
    (  true.B,      0.U,   13.U,    9.U, "b111".U,  20.U,  16.U,   36.U,  true.B,  "bgeuT"),
    (  true.B,      0.U,   11.U,  117.U, "b111".U,  68.U,  16.U,   72.U, false.B, "bgeuF"),
    ( false.B,      2.U,   13.U,    9.U, "b000".U, 204.U,  16.U,  220.U,  true.B,  "jal0"),
    ( false.B,      2.U,  133.U,  133.U, "b000".U, 208.U,   8.U,  216.U,  true.B,  "jal1"),
    ( false.B,      3.U,  100.U,  919.U, "b000".U, 100.U,  16.U,  116.U,  true.B,  "jalr0"),
    ( false.B,      3.U,  116.U,  119.U, "b000".U,  56.U,  12.U,  128.U,  true.B,  "jalr1")
  )

  for (t <- tests) {
    poke(ctl.io.branch,   t._1)
    poke(ctl.io.jumptype, t._2)
    poke(ctl.io.inputx,   t._3)
    poke(ctl.io.inputy,   t._4)
    poke(ctl.io.funct3,   t._5)
    poke(ctl.io.pc,       t._6)
    poke(ctl.io.imm,      t._7)
    step(1)
    expect(ctl.io.nextpc, t._8, s"${t._10} wrong")
    expect(ctl.io.taken,  t._9, s"${t._10} wrong")
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * Lab2 / testOnly dinocpu.NextPCTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.NextPCTesterLab2'
  * }}}
  */

class NextPCTesterLab2 extends ChiselFlatSpec {
  "NextPC" should s"match expectations for each intruction type" in {
    Driver(() => new NextPC) {
      c => new NextPCTester(c)
    } should be (true)
  }
}

