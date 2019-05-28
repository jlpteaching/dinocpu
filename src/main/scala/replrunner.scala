package dinocpu
import treadle.TreadleOptionsManager
import treadle.repl.HasReplConfig

object replrunner {
  val helptext = "usage: singlestep <test name> <CPU type>"

  def initRegs(vals: Map[Int, BigInt],repl: treadle.TreadleRepl ) {
    for ((num, value) <- vals) {
      repl.currentTreadleTester.poke(s"cpu.registers.regs_$num", value)
    }
  }

  /**
    *
    * @param vals holds "addresses" to values. Where address is the nth *word*
    */
  def initMemory(vals: Map[Int, BigInt],repl: treadle.TreadleRepl): Unit = {
    for ((addr, value) <- vals) {
        repl.currentTreadleTester.pokeMemory(s"cpu.mem.memory", addr, value)
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

    val driver = new CPUTesterDriver(cpuType, predictor, test.binary, test.extraName, true)
    driver.initRegs(test.initRegs)
    driver.initMemory(test.initMem)

    val options = new TreadleOptionsManager with HasReplConfig
    options.treadleOptions = driver.optionsManager.treadleOptions.copy()
    options.firrtlOptions = driver.optionsManager.firrtlOptions.copy()

    val repl = new treadle.TreadleRepl(options)
    repl.currentTreadleTesterOpt = Some(driver.simulator)
    repl.run()
    if (driver.checkRegs(test.checkRegs) && driver.checkMemory(test.checkMem)) {
      println("Test passed!")
    } else {
      println("Test failed!")
    }
  }
}
