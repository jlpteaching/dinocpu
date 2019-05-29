package dinocpu
import treadle.TreadleOptionsManager
import treadle.repl.HasReplConfig

object replrunner {
  val helptext = "usage: replrunner <test name> <CPU type>"

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

    //setup test
    val driver = new CPUTesterDriver(cpuType, predictor, test.binary, test.extraName, true)
    driver.initRegs(test.initRegs)
    driver.initMemory(test.initMem)

    //create a copy of the options from the testerdriver, just as slightly different type
    val options = new TreadleOptionsManager with HasReplConfig
    options.treadleOptions = driver.optionsManager.treadleOptions.copy()
    options.firrtlOptions = driver.optionsManager.firrtlOptions.copy()

    //copy the configured simulator in the REPL
    val repl = new treadle.TreadleRepl(options)
    repl.currentTreadleTesterOpt = Some(driver.simulator)
    repl.run()

    //check test result
    if (driver.checkRegs(test.checkRegs) && driver.checkMemory(test.checkMem)) {
      println("Test passed!")
    } else {
      println("Test failed!")
    }
  }
}
