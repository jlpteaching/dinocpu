package dinocpu
import dinocpu.{CPUTesterDriver, InstTests}
import treadle.TreadleOptionsManager
import treadle.repl.HasReplConfig

object replrunner {
  val helptext = "usage: singlestep <test name> <CPU type>"
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

    val options = new TreadleOptionsManager with HasReplConfig
    if (options.targetDirName == ".") {
      options.setTargetDirName(s"test_run_dir/$cpuType/$test.binary$test.extraName")
    }

    val repl = new treadle.TreadleRepl(options)
    repl.loadSource(driver.compiledFirrtl)
    repl.run()
    if (driver.checkRegs(test.checkRegs) && driver.checkMemory(test.checkMem)) {
      println("Test passed!")
    } else {
      println("Test failed!")
    }

  }
}
