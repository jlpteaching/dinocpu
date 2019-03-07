// A main to step through tests one cycle at a time.

package dinocpu

object singlestep {
  val helptext = "usage: singlestep <test name> <CPU type>"

  val commands = """
    | ?       : print this help
    | q       : quit
    | number  : move forward this many cycles""".stripMargin
    //| p <str> : evaluate the <str> as a print statement in scala""".stripMargin

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

    val driver = new CPUTesterDriver(cpuType, predictor, test.binary, test.extraName, true)
    driver.initRegs(test.initRegs)
    driver.initMemory(test.initMem)
    println("How many cycles to you want to run? \"Q\" to quit.")
    var done = false
    while (!done) {
      readLine("Cycles > ") match {
        case "?" => println(commands)
        case "q" | "Q" => done = true
        case command => try {
            driver.step(command.toInt)
          } catch {
            case e: NumberFormatException => println("Must give a number or ?")
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
