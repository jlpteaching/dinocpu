// Unit tests for the ALU

package dinocpu.test.components

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import dinocpu.components.ALU


class ALURandomUnitTester(c: ALU) extends PeekPokeTester(c) {
  private val alu = c

  val maxInt = BigInt("FFFFFFFF", 16)

  def test(op: UInt, f: (BigInt, BigInt) => BigInt) {
    for (i <- 0 until 10) {
      val x = rnd.nextInt(100000000)
      val y = rnd.nextInt(500000000)
      poke(alu.io.operation, op)
      poke(alu.io.inputx, x)
      poke(alu.io.inputy, y)
      step(1)
      val expectOut = f(x, y).toInt & maxInt
      expect(alu.io.result, expectOut, s"for operation ${op.toInt.toBinaryString}")
    }
  }

  def twoscomp(v: BigInt) : BigInt = {
    if (v < 0) {
      return maxInt + v + 1
    } else {
      return v
    }
  }

  test("b0000".U, (x: BigInt, y: BigInt) => (x & y))
  test("b0001".U, (x: BigInt, y: BigInt) => (x | y))
  test("b0010".U, (x: BigInt, y: BigInt) => (x + y))
  test("b0011".U, (x: BigInt, y: BigInt) => twoscomp(x - y))
  test("b0100".U, (x: BigInt, y: BigInt) => (x >> (y.toInt & 0x1f)))
  test("b0101".U, (x: BigInt, y: BigInt) => (if (x < y) 1 else 0))
  test("b0110".U, (x: BigInt, y: BigInt) => (x ^ y))
  test("b0111".U, (x: BigInt, y: BigInt) => (x >> (y.toInt & 0x1f)))
  test("b1000".U, (x: BigInt, y: BigInt) => (if (x < y) 1 else 0))
  test("b1001".U, (x: BigInt, y: BigInt) => (x << (y.toInt & 0x1f)))
  test("b1010".U, (x: BigInt, y: BigInt) => twoscomp(~(x | y)))
}

class ALUDirectedUnitTester(c: ALU) extends PeekPokeTester(c) {
  private val alu = c
  val maxInt = BigInt("FFFFFFFF", 16)

  def twoscomp(v: BigInt) : BigInt = {
    if (v < 0) {
      return maxInt + v + 1
    } else {
      return v
    }
  }

  // signed <
  poke(alu.io.operation, "b1000".U)
  poke(alu.io.inputx, twoscomp(-1))
  poke(alu.io.inputy, 1)
  step(1)
  expect(alu.io.result, 1)

  // unsigned <
  poke(alu.io.operation, "b0101".U)
  poke(alu.io.inputx, maxInt)
  poke(alu.io.inputy, 1)
  step(1)
  expect(alu.io.result, 0)

  // signed >>
  poke(alu.io.operation, "b0100".U)
  poke(alu.io.inputx, twoscomp(-1024))
  poke(alu.io.inputy, 5)
  step(1)
  expect(alu.io.result, twoscomp(-32))
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.ALUTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.ALUTester'
  * }}}
  */
class ALUTester extends ChiselFlatSpec {
  "ALU" should s"match expectations for random tests" in {
    Driver(() => new ALU) {
      c => new ALURandomUnitTester(c)
    } should be (true)
  }
  "ALU" should s"match expectations for directed edge tests" in {
    Driver(() => new ALU) {
      c => new ALUDirectedUnitTester(c)
    } should be (true)
  }
}
