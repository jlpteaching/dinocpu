// Main entry point for simulation
package CODCPU

import java.io.{RandomAccessFile,FileOutputStream}

import net.fornwall.jelf.ElfFile

/**
 * Simple object with only a main function to run the treadle simulation.
 * When run, this will begin execution and continue until something happens...
 *
 * {{{
 *  sbt> runMain CODCPU.simulate <riscv binary> [max cycles]
 * }}}
 */
object simulate {
  var helptext = "usage: simulate <riscv binary> [max cycles]"

  def elfToRaw(filename: String) = {
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
    val output = new FileOutputStream("output.bin")
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
    require(args.length >= 1, "Error: Expected at least one argument\n" + helptext)

    elfToRaw(args(0))

    val conf = new CPUConfig()
    conf.memFile = "output.bin"
    chisel3.Driver.execute(args, () => new Top(conf))
  }
}
