// Unit tests for the ALU control logic

package CODCPU

import chisel3._

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

import Constants._

class ControlUnitTester(c: Control) extends PeekPokeTester(c) {
  private val ctl = c

  // Copied from Patterson and Hennessy table 4.22
  val tests = List(
    // Inputs,       alusrc, memtoreg, regwrite, memread, memwrite, branch, aluop
    ( "b0110011".U,    0,        0,       1,        0,       0,        0,     2),
    ( "b0000011".U,    1,        1,       1,        1,       0,        0,     0),
    ( "b0100011".U,    1,        0,       0,        0,       1,        0,     0),
    ( "b1100011".U,    0,        0,       0,        0,       0,        1,     1)
  )

  for (t <- tests) {
    poke(ctl.io.opcode, t._1)
    step(1)
    expect(ctl.io.branch, t._7)
    expect(ctl.io.memread, t._5)
    expect(ctl.io.memtoreg, t._3)
    expect(ctl.io.aluop, t._8)
    expect(ctl.io.memwrite, t._6)
    expect(ctl.io.alusrc, t._2)
    expect(ctl.io.regwrite, t._4)
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly CODCPU.ControlTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly CODCPU.ControlTester'
  * }}}
  */
class ControlTester extends ChiselFlatSpec {
  private val backendNames = if(firrtl.FileUtils.isCommandAvailable(Seq("verilator", "--version"))) {
    Array("firrtl", "verilator")
  }
  else {
    Array("firrtl")
  }
  for ( backendName <- backendNames ) {
    "Control" should s"save written values (with $backendName)" in {
      Driver(() => new Control, backendName) {
        c => new ControlUnitTester(c)
      } should be (true)
    }
  }

  "Basic test using Driver.execute" should "be used as an alternative way to run specification" in {
    iotesters.Driver.execute(Array(), () => new Control) {
      c => new ControlUnitTester(c)
    } should be (true)
  }

  "using --backend-name verilator" should "be an alternative way to run using verilator" in {
    if(backendNames.contains("verilator")) {
      iotesters.Driver.execute(Array("--backend-name", "verilator"), () => new Control) {
        c => new ControlUnitTester(c)
      } should be(true)
    }
  }

  "running with --is-verbose" should "show more about what's going on in your tester" in {
    iotesters.Driver.execute(Array("--is-verbose"), () => new Control) {
      c => new ControlUnitTester(c)
    } should be(true)
  }

  "running with --fint-write-vcd" should "create a vcd file from your test" in {
    iotesters.Driver.execute(Array("--fint-write-vcd"), () => new Control) {
      c => new ControlUnitTester(c)
    } should be(true)
  }
}
