// Unit tests for the ALU

package dinocpu.test.components

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import dinocpu.components.ALU


class ALURandomUnitTester(c: ALU) extends PeekPokeTester(c) {
  private val alu = c

  val maxInt = BigInt("FFFFFFFFFFFFFFFF", 16)

  def test(op: UInt, f: (BigInt, BigInt) => BigInt) {
    for (i <- 0 until 10) {
      val x = rnd.nextInt(100000000)
      val y = rnd.nextInt(500000000)
      poke(alu.io.operation, op)
      poke(alu.io.inputx, x)
      poke(alu.io.inputy, y)
      step(1)
      val expectOut = f(x, y).toLong & maxInt
      expect(alu.io.result, expectOut, s"for operation ${op.toInt.toBinaryString}; inputx: ${x}; inputy: ${y}")
    }
  }

  def twoscomp(v: BigInt) : BigInt = {
    if (v < 0) {
      return maxInt + v + 1
    } else {
      return v
    }
  }

  def to32bit(v: BigInt) : BigInt = {
    return v & BigInt("FFFFFFFF", 16)
  }

  def signExtend32bitTo64bit(v: BigInt) : BigInt = {
    val signBit = (v >> 31) & 1
    val bitMask32 = BigInt("FFFFFFFF", 16)
    if (signBit == 0) {
      return v & bitMask32 // we only keep the lower half since the upper half must be all zeros
    } else {
      return v | (bitMask32 << 32) // the upper half must be all ones, the lower half must be preserved
    }
  }

  test("b00000".U, (x: BigInt, y: BigInt) => (x & y))
  test("b00001".U, (x: BigInt, y: BigInt) => (x | y))
  test("b00010".U, (x: BigInt, y: BigInt) => (x + y))
  test("b10010".U, (x: BigInt, y: BigInt) => (signExtend32bitTo64bit(to32bit(x) + to32bit(y)))) // 32-bit operands
  test("b00011".U, (x: BigInt, y: BigInt) => twoscomp(x - y))
  test("b10011".U, (x: BigInt, y: BigInt) => (signExtend32bitTo64bit(twoscomp(to32bit(x) - to32bit(y))))) // 32-bit operands
  test("b00100".U, (x: BigInt, y: BigInt) => (x >> (y.toInt & 0x3f)))
  test("b10100".U, (x: BigInt, y: BigInt) => (signExtend32bitTo64bit(to32bit(x) >> (y.toInt & 0x1f)))) // 32-bit operands
  test("b00101".U, (x: BigInt, y: BigInt) => (if (x < y) 1 else 0))
  test("b00110".U, (x: BigInt, y: BigInt) => (x ^ y))
  test("b00111".U, (x: BigInt, y: BigInt) => (x >> (y.toInt & 0x3f)))
  test("b10111".U, (x: BigInt, y: BigInt) => (signExtend32bitTo64bit(to32bit(x) >> (y.toInt & 0x1f)))) // 32-bit operands
  test("b01000".U, (x: BigInt, y: BigInt) => (if (x < y) 1 else 0))
  test("b01001".U, (x: BigInt, y: BigInt) => (x << (y.toInt & 0x3f)))
  test("b11001".U, (x: BigInt, y: BigInt) => (signExtend32bitTo64bit(to32bit(x) << (y.toInt & 0x1f)))) // 32-bit operands
  test("b01010".U, (x: BigInt, y: BigInt) => twoscomp(~(x | y)))
}

class ALUDirectedUnitTester(c: ALU) extends PeekPokeTester(c) {
  private val alu = c
  val maxInt = BigInt("FFFFFFFFFFFFFFFF", 16)

  def twoscomp(v: BigInt) : BigInt = {
    if (v < 0) {
      return maxInt + v + 1
    } else {
      return v
    }
  }

  // signed <
  poke(alu.io.operation, "b01000".U)
  poke(alu.io.inputx, twoscomp(-1))
  poke(alu.io.inputy, 1)
  step(1)
  expect(alu.io.result, 1)

  // unsigned <
  poke(alu.io.operation, "b00101".U)
  poke(alu.io.inputx, maxInt)
  poke(alu.io.inputy, 1)
  step(1)
  expect(alu.io.result, 0)

  // signed >>
  poke(alu.io.operation, "b00100".U)
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
