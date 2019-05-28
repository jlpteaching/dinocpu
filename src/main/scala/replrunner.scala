package dinocpu
import dinocpu.{CPUTesterDriver, InstTests}
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

    val options = new TreadleOptionsManager with HasReplConfig
    if (options.targetDirName == ".") {
      options.setTargetDirName(s"test_run_dir/$cpuType/$test.binary$test.extraName")
    }

    val repl = new treadle.TreadleRepl(options)
    repl.loadSource(driver.compiledFirrtl)
    initRegs(test.initRegs, repl)
    initMemory(test.initMem, repl)
    repl.run()

  }
}
