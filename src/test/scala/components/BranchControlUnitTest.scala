// Unit tests for the ALU control logic

package dinocpu

import chisel3._

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class BranchControlUnitTester(c: BranchControl, branch: Boolean = true) extends PeekPokeTester(c) {

  val tests = List(
    ("b000".U, (a: BigInt, b: BigInt) => a==b),
    ("b001".U, (a: BigInt, b: BigInt) => a!=b),
    ("b100".U, (a: BigInt, b: BigInt) => a.intValue<b.intValue),
    ("b101".U, (a: BigInt, b: BigInt) => a.intValue>=b.intValue),
    ("b110".U, (a: BigInt, b: BigInt) => a<b),
    ("b111".U, (a: BigInt, b: BigInt) => a>=b)
  )

  def test(opcode: UInt, result: (BigInt, BigInt) => Boolean) {
    for (i <- 0 until 10) {
      val x = rnd.nextInt(100000000)
      val y = rnd.nextInt(500000000)
      poke(c.io.branch, branch)
      poke(c.io.funct3, opcode)
      poke(c.io.inputx, x)
      poke(c.io.inputy, y)
      step(1)
      if (branch) {
        expect(c.io.taken, result(x, y))
      } else {
        expect(c.io.taken, false)
      }
    }
  }

  for (t <- tests) {
    test(t._1, t._2)
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.ControlTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.ControlTester'
  * }}}
  */
class BranchControlTester extends ChiselFlatSpec {
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
