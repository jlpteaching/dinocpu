// Tester driver definition

package dinocpu.test

import dinocpu._
import dinocpu.pipelined._
import dinocpu.simulate.{build, elfToHex}
import firrtl.stage.FirrtlSourceAnnotation
import org.scalatest.{FlatSpec, Matchers}
import treadle.TreadleTester
import treadle.executable.TreadleException


class CPUFlatSpec extends FlatSpec with Matchers

class CPUTesterDriver(cpuType: String,
                      branchPredictor: String,
                      binary: String,
                      extraName: String = "",
                      memType: String,
                      memPortType: String,
                      latency: Int = 0) {

  val optionsManager = new SimulatorOptionsManager()

  if (optionsManager.targetDirName == ".") {
    optionsManager.setTargetDirName(s"test_run_dir/$cpuType/$binary$extraName")
  }

  val hexName = s"${optionsManager.targetDirName}/${binary}.hex"

  val conf = new CPUConfig()
  conf.cpuType     = cpuType
  conf.memFile     = hexName
  conf.memType     = memType
  conf.memPortType = memPortType
  conf.memLatency  = latency

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

  // This compiles the chisel to firrtl
  val compiledFirrtl = build(optionsManager, conf)

  val endPC = elfToHex(path, hexName)

  // Instantiate the simulator
  val sourceAnnotation = FirrtlSourceAnnotation(compiledFirrtl)
  val simulator = TreadleTester(sourceAnnotation +: optionsManager.toAnnotationSeq)

  def reset(): Unit = {
    simulator.reset(5)
  }

  def initRegs(vals: Map[Int, BigInt]) {
    for ((num, value) <- vals) {
      simulator.poke(s"cpu.registers.regs_$num", value)
    }
  }

  def printReg(num: Int): Unit = {
    val v = simulator.peek(s"cpu.registers.regs_$num")
    println(s"reg${num}: ${v}")
  }

  def printRegs(): Unit = {
    // todo: Make this prettier
    for (reg <- 0 to 31) {
      printReg(reg)
    }
  }

  def printPC(): Unit = {
    val v = simulator.peek("cpu.pc")
    println(s"PC: ${v}")
  }

  def printInst(addr: Int = -1): Unit = {
    val pc = if (addr < 0) simulator.peek("cpu.pc").toInt else addr
    val v = simulator.peekMemory("mem.memory", pc/4)
    // Note: the memory is a 32-bit memory
    val inst = Disassembler.disassemble(v.longValue())
    val hex = v.toInt.toHexString.reverse.padTo(8, '0').reverse
    println(s"${pc.toString().padTo(8, ' ')}: ${inst.padTo(20, ' ')} (0x${hex})")
  }

  def dumpAllModules(): Unit = {
    val modules = conf.cpuType match {
      case "single-cycle" => SingleCycleCPUInfo.getModules()
      case "pipelined" => PipelinedCPUInfo.getModules()
      case "pipelined-dual-issue" => PipelinedDualIssueCPUInfo.getModules()
      case "pipelined-non-combin" => PipelinedNonCombinCPUInfo.getModules()
      case other => {
        println(s"Cannot dump info for CPU type ${other}")
        List()
      }
    }
    for (name <- modules) {
      for ((symbol, name) <- getIO(name)) {
        val v = simulator.peek(symbol)
        println(s"${name.padTo(30, ' ')} ${v} (0x${v.toInt.toHexString})")
      }
    }
  }

  def listModules(): Unit = {
    val modules = conf.cpuType match {
      case "single-cycle" => SingleCycleCPUInfo.getModules()
      case "pipelined" => PipelinedCPUInfo.getModules()
      case "pipelined-dual-issue" =>PipelinedDualIssueCPUInfo.getModules()
      case "pipelined-non-combin" => PipelinedNonCombinCPUInfo.getModules()
      case other => {
        println(s"Cannot dump info for CPU type ${other}")
        List()
      }
    }
    println("Available modules to dump I/O")
    println("-----------------------------")
    for (name <- modules) {
      println(s"${name}")
    }
  }

  def dumpModule(module: String): Unit = {
    for ((symbol, name) <- getIO(module)) {
      val v = simulator.peek(symbol)
      println(s"${name.padTo(30, ' ')} ${v} (0x${v.toInt.toHexString})")
    }
  }

  def printAllPipeRegs(): Unit = {
    for (reg <- PipelinedCPUInfo.getPipelineRegs()) {
      printPipeReg(reg)
    }
  }

  def printPipeReg(module: String): Unit = {
    val syms = simulator.engine.validNames.filter(
      name => (name startsWith s"cpu.${module}.reg_") && !(name endsWith "/in"))
    for (sym <- syms) {
      val name = s"${module}.${sym.split('.').last.drop(4)}"
      val v = simulator.peek(sym)
      println(s"${name.padTo(39, ' ')} ${v} (0x${v.toInt.toHexString})")
    }

   val inputs = simulator.engine.validNames.filter(
      name => (name startsWith s"cpu.${module}.reg_") && (name endsWith "/in"))
    for (sym <- inputs) {
      val name = s"${module}.${sym.split('.').last.drop(4)}".dropRight(3) + " (input)"
      val v = simulator.peek(sym)
      println(s"${name.padTo(40, ' ')}${v} (0x${v.toInt.toHexString})")
    }

  }

  def getIO(module: String): Map[String,String] = {
    module match {
      case "dmem" => {
        val syms = simulator.engine.validNames.filter(name => name startsWith s"cpu.io_dmem_")
        syms map {
          sym => sym -> sym.substring(sym.indexOf('_') + 1).replace('_', '.')
        } toMap
      }
      case "imem" => {
        val syms = simulator.engine.validNames.filter(name => name startsWith s"cpu.io_imem_")
        syms map {
          sym => sym -> sym.substring(sym.indexOf('_') + 1).replace('_', '.')
        } toMap
      }
      case other => {
        val syms = simulator.engine.validNames.filter(name => name startsWith s"cpu.${other}.io_")
        syms map {
          sym => sym -> s"${other}.io.${sym.substring(sym.indexOf('_') + 1)}"
        } toMap
      }
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
    simulator.step(1)
    cycle += 1
    while (simulator.peek("cpu.pc") < endPC && cycle < start + cycles) {
      simulator.step(1)
      cycle += 1
    }
    println(s"Current cycle: ${cycle}")
  }

  def verboseStep(cycles: Int = 0): Unit = {
    val start = cycle
    simulator.step(1)
    cycle += 1
    while (simulator.peek("cpu.pc") < endPC && cycle < start + cycles) {
      simulator.step(1)
      cycle += 1
    }
  }

  def run(cycles: Int): Unit = {
    while (cycle < cycles && simulator.peek("cpu.pc") < endPC) {
      if (cycle % 10000 == 0) println(s"${cycle} cycles simulated.")
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

/* Only used in tests/scala/cpu-tests */
object CPUTesterDriver {
  def apply(testCase: CPUTestCase, cpuType: String, branchPredictor: String = "",
                      memType: String = "combinational", memPortType: String = "combinational-port",
                      latency: Int = 0): Boolean = {
    val cpustr = if (branchPredictor != "") { cpuType+"-bp" } else { cpuType }
    val driver = new CPUTesterDriver(cpustr, branchPredictor, testCase.binary, testCase.extraName,
      memType, memPortType, latency)
    driver.initRegs(testCase.initRegs)
    driver.initMemory(testCase.initMem)
    driver.run(testCase.cycles(cpuType))
    val success = driver.checkRegs(testCase.checkRegs)
    success && driver.checkMemory(testCase.checkMem)
  }
}
