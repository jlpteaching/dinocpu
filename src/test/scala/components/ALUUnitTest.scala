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
      val x = rnd.nextInt(100000000)
      val y = rnd.nextInt(500000000)
      poke(alu.io.operation, op)
      poke(alu.io.inputx, x)
      poke(alu.io.inputy, y)
      step(1)
      val expectOut = f(x, y)
      expect(alu.io.result, expectOut)
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
