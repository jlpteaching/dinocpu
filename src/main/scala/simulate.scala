// Main entry point for simulation
package CODCPU

import firrtl.{ExecutionOptionsManager, HasFirrtlOptions}
import treadle.{HasTreadleOptions, TreadleOptionsManager, TreadleTester}

import java.io.{RandomAccessFile,FileOutputStream}
import net.fornwall.jelf.ElfFile

/**
 * Simple object with only a main function to run the treadle simulation.
 * When run, this will begin execution and continue until something happens...
 *
 * {{{
 *  sbt> runMain CODCPU.simulate <riscv binary> <CPU type> [max cycles]
 * }}}
 */
object simulate {
  var helptext = "usage: simulate <riscv binary> <CPU type> [max cycles]"

  def elfToRaw(filename: String, outfile: String) = {
    val elf = ElfFile.fromFile(new java.io.File(filename))
    val sections = Seq(".text", ".data") // These are the sections we want to pull out
    var info : Seq[(Long, Long, Long)] = Seq() // address to put the data, offset into the binary, size of section
    // Search for these section names
    for (i <- 1 to elf.num_sh - 1) {
      val section =  elf.getSection(i)
      if (sections.contains(section.getName)) {
        println("Found "+section.address + " " + section.section_offset + " " + section.size)
        info = info :+ (section.address, section.section_offset, section.size)
      }
    }
    require(info.length == sections.length, "Couldn't find all of the sections in the binary!")

    // Now, we want to create a new file to load into our memory
    val output = new FileOutputStream(outfile)
    val ch = output.getChannel()
    val f = new RandomAccessFile(filename, "r")
    println("Length: "+ f.length)
    for ((address, offset, size) <- info) {
      f.seek(offset)
      val data = new Array[Byte](size.toInt)
      f.read(data)
      ch.position(address)
      output.write(data)
    }
    output.close
  }

  def main(args: Array[String]): Unit = {
    require(args.length >= 2, "Error: Expected at least two argument\n" + helptext)

    val optionsManager = new SimulatorOptionsManager

    if (optionsManager.parser.parse(args)) {
        optionsManager.setTargetDirName("simulator_run_dir")
    } else {
        None
    }

    val rawName = optionsManager.targetDirName + "/executable.raw"
    println(s"Want to load $rawName")

    elfToRaw(args(0), rawName)

    val conf = new CPUConfig()
    conf.cpuType = args(1)
    // It would be nice to put this in the "simulator_run_dir", but fighting these options isn't worth my time, right now.
    conf.memFile = rawName

    val system = chisel3.Driver.emit(() => new Top(conf))

    val simulator = new TreadleTester(system, optionsManager)
    simulator.step(10)
  }
}

case class SimulatorOptions(
                                                        maxCycles           : Long              = 0
    )
    extends firrtl.ComposableOptions {
}

trait HasSimulatorOptions {
    self: ExecutionOptionsManager =>

    var simulatorOptions = SimulatorOptions()

    parser.note("simulator-options")

    parser.opt[Long]("max-cycles")
        .abbr("mx")
        .valueName("<long-value>")
        .foreach {x =>
            simulatorOptions.copy(maxCycles = x)
        }
        .text("Max number of cycles to simulate. Default is 0, to continue simulating")
}

class SimulatorOptionsManager extends TreadleOptionsManager with HasSimulatorSuite

trait HasSimulatorSuite extends TreadleOptionsManager with HasFirrtlOptions with HasTreadleOptions with HasSimulatorOptions {
    self : ExecutionOptionsManager =>
}

