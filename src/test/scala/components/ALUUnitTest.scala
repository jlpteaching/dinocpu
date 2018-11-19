// Unit tests for the ALU

package CODCPU

import chisel3._

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

import Constants._


class ALUUnitTester(c: ALU) extends PeekPokeTester(c) {
  private val alu = c

  val maxInt = BigInt("FFFFFFFF", 16)

  def test(op: UInt, f: (BigInt, BigInt) => BigInt) {
    for (i <- 0 until 10) {
      val x = rnd.nextInt(100)
      val y = rnd.nextInt(500)
      poke(alu.io.operation, op)
      poke(alu.io.inputx, x)
      poke(alu.io.inputy, y)
      step(1)
      val expectOut = f(x, y)
      val expectZero = (expectOut == 0)
      expect(alu.io.result, expectOut)
      expect(alu.io.zero, expectZero)
    }
  }

  def twoscomp(v: BigInt) : BigInt = {
    if (v < 0) {
      return maxInt + v + 1
    } else {
      return v
    }
  }

  test(ADD_OP, (x: BigInt, y: BigInt) => (x + y))
  test(AND_OP, (x: BigInt, y: BigInt) => (x & y))
  test(OR_OP, (x: BigInt, y: BigInt) => (x | y))
  test(SUB_OP, (x: BigInt, y: BigInt) => twoscomp(x - y))
  test(SLT_OP, (x: BigInt, y: BigInt) => (if (x < y) 1 else 0))
  test(NOR_OP, (x: BigInt, y: BigInt) => twoscomp(~(x | y)))

  // Explicitly test 0
  val x = 0
  val y = 0
  poke(alu.io.operation, ADD_OP)
  poke(alu.io.inputx, x)
  poke(alu.io.inputy, y)
  step(1)
  expect(alu.io.result, x+y)
  expect(alu.io.zero, (x+y == 0))
  poke(alu.io.operation, AND_OP)
  poke(alu.io.inputx, x)
  poke(alu.io.inputy, y)
  step(1)
  expect(alu.io.result, 0)
  expect(alu.io.zero, 1)
  poke(alu.io.operation, OR_OP)
  poke(alu.io.inputx, x)
  poke(alu.io.inputy, y)
  step(1)
  expect(alu.io.result, 0)
  expect(alu.io.zero, 1)
  poke(alu.io.operation, SUB_OP)
  poke(alu.io.inputx, x)
  poke(alu.io.inputy, y)
  step(1)
  expect(alu.io.result, x-y)
  expect(alu.io.zero, (x-y == 0))
  poke(alu.io.operation, SLT_OP)
  poke(alu.io.inputx, x)
  poke(alu.io.inputy, y)
  step(1)
  expect(alu.io.result, 0)
  expect(alu.io.zero, (1))
  poke(alu.io.operation, NOR_OP)
  poke(alu.io.inputx, x)
  poke(alu.io.inputy, y)
  step(1)
  expect(alu.io.result, maxInt)
  expect(alu.io.zero, (0))
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly CODCPU.ALUTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly CODCPU.ALUTester'
  * }}}
  */
class ALUTester extends ChiselFlatSpec {
  private val backendNames = if(firrtl.FileUtils.isCommandAvailable(Seq("verilator", "--version"))) {
    Array("firrtl", "verilator")
  }
  else {
    Array("firrtl")
  }
  for ( backendName <- backendNames ) {
    "ALU" should s"save written values (with $backendName)" in {
      Driver(() => new ALU, backendName) {
        c => new ALUUnitTester(c)
      } should be (true)
    }
  }
}
