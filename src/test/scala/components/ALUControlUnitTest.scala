// Unit tests for the ALU control logic

package CODCPU

import chisel3._

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

import Constants._

class ALUControlUnitTester(c: ALUControl) extends PeekPokeTester(c) {
  private val ctl = c

  // Copied from Patterson and Hennessy table 4.12
  val tests = List(
    // ALUop,  Funct7,       Func3,    Control Input
    ( "b00".U, "b0000000".U, "b000".U, "b0010".U),
    ( "b00".U, "b0000000".U, "b000".U, "b0010".U),
    ( "b01".U, "b0000000".U, "b000".U, "b0110".U),
    ( "b10".U, "b0000000".U, "b000".U, "b0010".U),
    ( "b10".U, "b0100000".U, "b000".U, "b0110".U),
    ( "b10".U, "b0000000".U, "b111".U, "b0000".U),
    ( "b10".U, "b0000000".U, "b110".U, "b0001".U)
  )

  for (t <- tests) {
    poke(ctl.io.aluop, t._1)
    poke(ctl.io.funct7, t._2)
    poke(ctl.io.funct3, t._3)
    step(1)
    expect(ctl.io.operation, t._4)
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly CODCPU.ALUControlTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly CODCPU.ALUControlTester'
  * }}}
  */
class ALUControlTester extends ChiselFlatSpec {
  private val backendNames = if(firrtl.FileUtils.isCommandAvailable(Seq("verilator", "--version"))) {
    Array("firrtl", "verilator")
  }
  else {
    Array("firrtl")
  }
  for ( backendName <- backendNames ) {
    "ALUControl" should s"save written values (with $backendName)" in {
      Driver(() => new ALUControl, backendName) {
        c => new ALUControlUnitTester(c)
      } should be(true)
    }
  }
}
