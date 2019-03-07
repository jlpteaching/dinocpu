package dinocpu

import dinocpu.simulate.{build, elfToHex}
import org.scalatest.{FlatSpec, Matchers}
import treadle.TreadleTester
import treadle.executable.TreadleException

class CPUFlatSpec extends FlatSpec with Matchers

class CPUTesterDriver(cpuType: String,
                      branchPredictor: String,
                      binary: String,
                      extraName: String = "",
                      forceDebug: Boolean = false) {

  val optionsManager = new SimulatorOptionsManager()

  if (optionsManager.targetDirName == ".") {
    optionsManager.setTargetDirName(s"test_run_dir/$cpuType/$binary$extraName")
  }

  val hexName = s"${optionsManager.targetDirName}/${binary}.hex"

  val conf = new CPUConfig()
  conf.cpuType = cpuType
  conf.memFile = hexName
  conf.debug = true // always run with debug print statements

  if (!branchPredictor.isEmpty) {
    conf.branchPredictor = branchPredictor
  }

  // Convert the binary to a hex file that can be loaded by treadle
  // (Do this after compiling the firrtl so the directory is created)
  val path = if (binary.endsWith(".riscv")) {
    s"src/test/resources/c/${binary}"
  } else {
    s"src/test/resources/risc-v/${binary}"
  }

  if (path.endsWith(".riscv")) {
    // This is a long test, suppress the debugging output
    println("WARNING: Suppressing debug output for long test.")
    println("Modify CPUTesterDriver or use singlestep for debugging ouput.")
    conf.debug = false
  }

  if (forceDebug) conf.debug = true

  // This compiles the chisel to firrtl
  val compiledFirrtl = build(optionsManager, conf)

  val endPC = elfToHex(path, hexName)

  // Instantiate the simulator
  val simulator = TreadleTester(compiledFirrtl, optionsManager)

  def reset(): Unit = {
    simulator.reset(5)
  }

  def initRegs(vals: Map[Int, BigInt]) {
    for ((num, value) <- vals) {
      simulator.poke(s"cpu.registers.regs_$num", value)
    }
  }

  /**
    *
    * @param vals holds "addresses" to values. Where address is the nth *word*
    */
  def initMemory(vals: Map[Int, BigInt]): Unit = {
    for ((addr, value) <- vals) {
      simulator.pokeMemory(s"cpu.mem.memory", addr, value)
    }
  }

  def checkRegs(vals: Map[Int, BigInt]): Boolean = {
    var success = true
    for ((num, value) <- vals) {
      try {
        simulator.expect(s"cpu.registers.regs_$num", value)
      } catch {
        case _: TreadleException => {
          success = false
          val real = simulator.peek(s"cpu.registers.regs_$num")
          println(s"Register $num failed to match. Was $real. Should be $value")
        }
      }
    }
    success
  }

  def checkMemory(vals: Map[Int, BigInt]): Boolean = {
    var success = true
    for ((addr, value) <- vals) {
      try {
        simulator.expectMemory("mem.memory", addr, value)
      } catch {
        case e: TreadleException => {
          success = false
          val real = simulator.peekMemory("mem.memory", addr)
          println(s"Memory at address 0x${addr.toHexString} failed to match. Was $real. Should be $value")
        }
      }
    }
    success
  }

  var cycle = 0

  def step(cycles: Int = 0): Unit = {
    val start = cycle
    while (simulator.peek("cpu.pc") != endPC && cycle < start + cycles) {
      simulator.step(1)
      cycle += 1
    }
  }

  def run(cycles: Int): Unit = {
    while (cycle < cycles) {
      simulator.step(1)
      cycle += 1
    }
  }
}

case class CPUTestCase(
  binary:  String,
  cycles: Map[String, Int],
  initRegs: Map[Int, BigInt],
  checkRegs: Map[Int, BigInt],
  initMem: Map[Int, BigInt],
  checkMem: Map[Int, BigInt],
  extraName: String = ""
  )
{
  def name() : String = {
    binary + extraName
  }
}

object CPUTesterDriver {
  def apply(testCase: CPUTestCase, cpuType: String, branchPredictor: String = ""): Boolean = {
    val driver = new CPUTesterDriver(cpuType, branchPredictor, testCase.binary, testCase.extraName)
    driver.initRegs(testCase.initRegs)
    driver.initMemory(testCase.initMem)
    driver.run(testCase.cycles(cpuType))
    val success = driver.checkRegs(testCase.checkRegs)
    success && driver.checkMemory(testCase.checkMem)
  }
}
