// Main entry point for simulation
package dinocpu

import dinocpu.test._

import firrtl.{ExecutionOptionsManager, HasFirrtlOptions}
import treadle.{HasTreadleOptions, TreadleOptionsManager, TreadleTester}
import java.io.{File, PrintWriter, RandomAccessFile}

import chisel3.{ChiselExecutionFailure,ChiselExecutionSuccess,HasChiselExecutionOptions}
import net.fornwall.jelf.ElfFile

import scala.collection.SortedMap
import scala.util.control.NonFatal

/**
 * Simple object with only a main function to run the treadle simulation.
 * When run, this will begin execution and continue until the PC reaches the
 * "_last" symbol in the elf file or the max cycle parameter is reached
 *
 * {{{
 *  sbt> runMain dinocpu.simulate [options] <riscv binary> <CPU type>
 * }}}
 */
object simulate {
  val helptext = "usage: simulate <riscv binary> <CPU type>"

  def elfToHex(filename: String, outfile: String) = {
    val elf = ElfFile.fromFile(new java.io.File(filename))
    val sections = Seq(".text", ".data") // These are the sections we want to pull out
    // address to put the data -> offset into the binary, size of section
    var info = SortedMap[Long, (Long, Long)]()
    // Search for these section names
    for (i <- 1 until elf.num_sh) {
      val section =  elf.getSection(i)
      if (sections.contains(section.getName)) {
        //println("Found "+section.address + " " + section.section_offset + " " + section.size)
        info += section.address -> (section.section_offset, section.size)
      }
    }

    // Now, we want to create a new file to load into our memory
    val output = new PrintWriter(new File(outfile))
    val f = new RandomAccessFile(filename, "r")
    // println("Length: "+ f.length)
    var location = 0
    for ((address, (offset, size)) <- info) {
      //println(s"Skipping until $address")
      while (location < address) {
        require(location + 3 < address, "Assuming addresses aligned to 4 bytes")
        output.write("00000000\n")
        location += 4
      }
      //println(s"Writing $size bytes")
      val data = new Array[Byte](size.toInt)
      f.seek(offset)
      f.read(data)
      var s = List[String]()
      for (byte <- data) {
        s = s :+ ("%02X" format byte)
        location += 1
        if (location % 4 == 0) {
          // Once we've read 4 bytes, swap endianness
          output.write(s(3)+s(2)+s(1)+s(0)+"\n")
          s = List[String]()
        }
      }
      //println(s"Wrote until $location")
    }
    output.close()
    // Return the final PC value we're looking for
    val symbol = elf.getELFSymbol("_last")

    if (symbol != null) symbol.value
    else 0x400L
  }

  def build(optionsManager: SimulatorOptionsManager, conf: CPUConfig): String = {
    optionsManager.firrtlOptions = optionsManager.firrtlOptions.copy(compilerName = "low")

    chisel3.Driver.execute(optionsManager, () => new Top(conf)) match {
    case ChiselExecutionSuccess(Some(_), _, Some(firrtlExecutionResult)) =>
      firrtlExecutionResult match {
      case firrtl.FirrtlExecutionSuccess(_, compiledFirrtl) =>
        compiledFirrtl
      case firrtl.FirrtlExecutionFailure(message) =>
        throw new Exception(s"FirrtlBackend: Compile failed. Message: $message")
      }
      case _ =>
        throw new Exception("Problem with compilation")
    }
  }

  def main(args: Array[String]): Unit = {
    require(args.length >= 2, "Error: Expected at least two argument\n" + helptext)

    val optionsManager = new SimulatorOptionsManager

    if (optionsManager.parser.parse(args)) {
      optionsManager.setTargetDirName("simulator_run_dir")
    } else {
      None
    }

    // Get the name for the hex file
    val hexName = optionsManager.targetDirName + "/executable.hex"

    // Create the CPU config. This sets the type of CPU and the binary to load
    val conf = new CPUConfig()

    println(s"Running test ${args(0)} with memory latency of ${args(1)} cycles")

    val test = InstTests.nameMap(args(0))
    val (cpuType, memType, memPortType, latency) =
    // Check for latency
    if (args(1) forall Character.isDigit) {
      if (args(1).toInt == 0) {
        ("pipelined-non-combin", "combinational", "combinational-port", 0)
      } else {
        ("pipelined-non-combin", "non-combinational", "non-combinational-port", args(1).toInt)
      }
    } else { // Original single-step format
      (args(1), "combinational", "combinational-port", 0)
    }

    val driver = new CPUTesterDriver(cpuType, "", test.binary, test.extraName, memType,
      memPortType, latency)
    driver.initRegs(test.initRegs)
    driver.initMemory(test.initMem)

    val cycles = 3000000
    println(s"Running for max of ${cycles}")
    driver.run(cycles)
    println(s"Finished after ${driver.cycle} cycles")

    if (driver.checkRegs(test.checkRegs) && driver.checkMemory(test.checkMem)) {
      println("Test passed!")
    } else {
      println("Test failed!")
    }

  }
}

case class SimulatorOptions(
              maxCycles           : Int              = 0
  )
  extends firrtl.ComposableOptions {
}

trait HasSimulatorOptions {
  self: ExecutionOptionsManager =>

  val simulatorOptions = SimulatorOptions()

  parser.note("simulator-options")

  parser.opt[Int]("max-cycles")
    .abbr("mx")
    .valueName("<long-value>")
    .foreach {x =>
      simulatorOptions.copy(maxCycles = x)
    }
    .text("Max number of cycles to simulate. Default is 0, to continue simulating")
}

class SimulatorOptionsManager extends TreadleOptionsManager with HasSimulatorSuite

trait HasSimulatorSuite extends TreadleOptionsManager with HasChiselExecutionOptions with HasFirrtlOptions with HasTreadleOptions with HasSimulatorOptions {
  self : ExecutionOptionsManager =>
}

