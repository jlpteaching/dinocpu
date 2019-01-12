package dinocpu

import chisel3._
import chisel3.util._

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class SimpleAdder extends Module {
  val io = IO(new Bundle{
    val inputx = Input(UInt(32.W))
    val inputy = Input(UInt(32.W))

    val result = Output(UInt(32.W))
  })

  io.result := io.inputx + io.inputy
}

class SimpleSystem extends Module {
  val io = IO(new Bundle {
    val success = Output(Bool())
  })

  val adder1 = Module(new SimpleAdder())
  val adder2 = Module(new SimpleAdder())

  val regx = RegInit(0.U)
  val regy = RegInit(1.U)

  adder1.io.inputx := regx
  adder1.io.inputy := regy

  adder2.io.inputx := adder1.io.result
  adder2.io.inputy := 3.U

  regx := adder1.io.result

  regy := adder2.io.result

  when(adder2.io.result === 128.U) {
    io.success := true.B
  } .otherwise {
    io.success := false.B
  }

  printf(p"regx: $regx, regy: $regy, success: ${io.success}\n")

}

class SimpleSystemUnitTester(c: SimpleSystem) extends PeekPokeTester(c) {
  step(10)
}

object simple {
  def main(args: Array[String]): Unit = {
    Driver( () => new SimpleSystem ) { c => new SimpleSystemUnitTester(c) }
  }
}
