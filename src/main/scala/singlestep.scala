// A main to step through tests one cycle at a time.

package dinocpu

object singlestep {
  val helptext = "usage: singlestep <test name> <CPU type>"

  def main(args: Array[String]): Unit = {
    require(args.length >= 2, "Error: Expected at least two argument\n" + helptext)

    println(s"Running test ${args(0)} on CPU design ${args(1)}")

    val test = InstTests.nameMap(args(0))
    val cpuType = args(1)

    val driver = new CPUTesterDriver(cpuType, test.binary, test.extraName)
    driver.initRegs(test.initRegs)
    driver.initMemory(test.initMem)
    println("How many cycles to you want to run? \"Q\" to quit.")
    var done = false
    while (!done) {
      val command = readLine("Cycles > ")
      try {
        driver.step(command.toInt)
      } catch {
        case e: NumberFormatException => {
          if (command.toLowerCase() == "q") done = true
          else println("Must give a number")
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
