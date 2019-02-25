// Unit tests for the saturating counter

package dinocpu

import chisel3._

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class Wrapper(width: Int) extends Module {
  val io = IO(new Bundle {
    val incr = Input(Bool())
    val decr = Input(Bool())

    val value = Output(UInt(width.W))
    val up = Output(Bool())
    val down = Output(Bool())
  })
  io := DontCare

  val counter = RegInit(SaturatingCounter.initup(width))

  when (io.incr) {
    counter := SaturatingCounter.incr(counter)
  }

  when (io.decr) {
    //counter.decr()
  }

  io.value := counter
  //io.up := counter.up()
  //io.down := counter.down()
}

class SaturatingCounterUnitTester(c: Wrapper, width: Int) extends PeekPokeTester(c) {
  val max = 1 << width
  val d = 1 << (width - 1)
  expect(c.io.value, d)

  step(1)
  val v = d + 1
  expect(c.io.value, if (v < max) v else max)

}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SaturatingCounterTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SaturatingCounterTester'
  * }}}
  */
class SaturatingCounterTester extends ChiselFlatSpec {
  "SaturatingCounter" should s"match expectations for width 2" in {
    Driver(() => new Wrapper(2)) {
      c => new SaturatingCounterUnitTester(c, 2)
    } should be (true)
  }
  "SaturatingCounter" should s"match expectations for width 4" in {
    Driver(() => new Wrapper(4)) {
      c => new SaturatingCounterUnitTester(c, 4)
    } should be (true)
  }
}
