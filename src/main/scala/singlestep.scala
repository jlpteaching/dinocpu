// A main to step through tests one cycle at a time.

package dinocpu

import dinocpu.test._


object singlestep {
  val helptext = "usage: singlestep <test name> <CPU type>"

  val commands = """Help for the single stepper:
    | ?               : print this help
    | print reg <num> : print the value in register
    | print regs      : print values in all registers
    | print pc        : print the address in the pc
    | print inst      : print the disassembly for the current instruction
    | q               : quit
    | step [num]      : move forward this many cycles, default 1
    |""".stripMargin

  def doPrint(tokens: Array[String], driver: CPUTesterDriver): Boolean = {
    tokens(1) match {
      case "reg" => {
        if (tokens.length == 3) {
          try {
            driver.printReg(tokens(2).toInt)
            true
          } catch {
            case e: NumberFormatException => false
          }
        } else {
          false
        }
      }
      case "regs" => {
        driver.printRegs()
        true
      }
      case "pc" => {
        driver.printPC()
        true
      }
      case "inst" => {
        driver.printInst()
        true
      }
      case _ => false
    }
  }

  def doStep(tokens: Array[String], driver: CPUTesterDriver): Boolean = {
    val cycles = try {
      if (tokens.length == 2) tokens(1).toInt else 1
    } catch {
      case e: NumberFormatException => 0
    }
    if (cycles > 0) {
      driver.step(cycles)
      true
    } else {
      false
    }
  }

  def main(args: Array[String]): Unit = {
    require(args.length >= 2, "Error: Expected at least two argument\n" + helptext)

    println(s"Running test ${args(0)} on CPU design ${args(1)}")

    val test = InstTests.nameMap(args(0))
    val params = args(1).split(":")
    val cpuType = params(0)

    val predictor =
    if (params.length == 2) {
      params(1)
    } else {
      ""
    }

    val driver = new CPUTesterDriver(cpuType, predictor, test.binary, test.extraName)
    driver.initRegs(test.initRegs)
    driver.initMemory(test.initMem)
    println(commands)
    var done = false
    while (!done) {
      val tokens = scala.io.StdIn.readLine("Single stepper > ").split(" ")
      if (tokens.length > 0) {
        tokens(0) match {
          case "?" => println(commands)
          case "q" | "Q" => done = true
          case "step" => if (!doStep(tokens, driver)) println(commands)
          case "print" => {
            if (tokens.length > 1) {
              if (!doPrint(tokens, driver)) println(commands)
            }
          }
          case _ => println(commands)
        }
      }
    }
    if (driver.checkRegs(test.checkRegs) && driver.checkMemory(test.checkMem)) {
      println("Test passed!")
    } else {
      println("Test failed!")
    }
  }
}
