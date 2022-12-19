// A main to step through tests one cycle at a time.

package dinocpu

import dinocpu.test._
import org.jline.reader.{LineReaderBuilder,EndOfFileException,UserInterruptException}
import org.jline.terminal.TerminalBuilder
import org.jline.builtins.Completers.TreeCompleter
import org.jline.builtins.Completers.TreeCompleter.node
import scala.util.control.Breaks._
import scala.collection.mutable


object singlestep {
  val helptext = "usage: singlestep <test name> <CPU type>"

  val commands = """Help for the single stepper:
    | Note: Registers print the value *stored* in that register. The wires print
    | the *current* value on the wire for that cycle.
    |
    | Printing registers
    | ------------------
    | print reg <num>  : print the value in register
    | print regs       : print values in all registers
    | print pc         : print the address in the pc
    | print inst [addr]: print the disassembly for the instruction at addr.
    |                    If no addr provided then use the current pc.
    |
    | Printing module I/O (wires)
    | ---------------------------
    | dump all        : Show all modules and the values of their I/O
    | dump list       : List the valid modules to dump
    | dump [module]   : Show values of the I/O on a specific module
    |
    | Printing pipeline registers (pipelined CPU only)
    | ------------------------------------------------
    | print pipereg <name> : Print the values in the pipeline register with name <name>
    | print piperegs       : Print the values in all of the pipeline registers
    |
    | Controlling the simulator
    | -------------------------
    | step [num]      : move forward this many cycles, default 1
    |
    | Display command (display value after step)
    | ---------------------------------------
    | display reg [num]
    | display regs
    | display pc
    | display inst
    | display pipereg [name]
    | display piperegs
    | display modules
    | display module [module]
    |
    | Stop Displaying command
    | -----------------------
    | undisplay <num>  : remove the ith display line
    |
    | Other commands
    | --------------
    | ?               : print this help
    | q               : quit
    |
    | Command Alias
    | -------------
    | p: print
    | d: display
    | s: step
    | v: verbose step (print on each cycle)
    |
    |""".stripMargin

  val DisplayAcceptedCommands = Array[String](
    "pc",
    "reg",
    "regs",
    "inst",
    "pipereg",
    "piperegs",
    "module",
    "modules"
  )

  def doPrint(tokens: Array[String], driver: CPUTesterDriver): Boolean = {
    tokens(1) match {
      case "reg" => {
        if (tokens.length == 3) {
          try {
            driver.printReg(tokens(2).toInt)
            true
          } catch {
            case e: NumberFormatException => false
          }
        } else {
          false
        }
      }
      case "regs" => {
        driver.printRegs()
        true
      }
      case "pipereg" => {
        driver.printPipeReg(tokens(2))
        true
      }
      case "piperegs" => {
        driver.printAllPipeRegs()
        true
      }
      case "pc" => {
        driver.printPC()
        true
      }
      case "inst" => {
        if (tokens.length == 2) {
          driver.printInst()
          true
        } else if (tokens.length == 3) {
          try {
            driver.printInst(tokens(2).toInt)
            true
          } catch {
            case e: NumberFormatException => false
          }
        } else {
          false
        }
      }
      case _ => false
    }
  }

  def doDump(tokens: Array[String], driver: CPUTesterDriver): Boolean = {
    tokens(1) match {
      case "all" => {
        driver.dumpAllModules()
        true
      }
      case "list" => {
        driver.listModules()
        true
      }
      case _ => {
        driver.dumpModule(tokens(1))
        true
      }
    }
  }

  def doStep(tokens: Array[String], driver: CPUTesterDriver): Boolean = {
    val cycles = try {
      if (tokens.length == 2) tokens(1).toInt else 1
    } catch {
      case e: NumberFormatException => 0
    }
    if (cycles > 0) {
      driver.step(cycles)
      true
    } else {
      false
    }
  }

  def doVerboseStep(tokens: Array[String], displayList: Seq[Array[String]], driver: CPUTesterDriver): Boolean = {
    val cycles = try {
      if (tokens.length == 2) tokens(1).toInt else 1
    } catch {
      case e: NumberFormatException => 0
    }
    if (cycles > 0) {
      var iter = 0
      for (iter <- 1 to cycles) {
        driver.verboseStep(1)
        doDisplay(displayList, driver)
      }
      true
    } else {
      false
    }
  }

  def doDisplay(displayList: Seq[Array[String]], driver: CPUTesterDriver) {
    var count = 0
    for (command <- displayList) {
      count += 1
      print(count + ": ")
      command(1) match {
        case "reg" | "regs" | "pipereg" | "piperegs" | "pc" | "inst" => doPrint(command, driver)
        case "modules" => doDump(Array("dump", "all"), driver)
        case "module" => doDump(command.slice(1, command.length), driver)
      }
    }
  }

  def displayCommandOkay(tokens: Seq[String]): Boolean = {
    if (tokens.length > 1 && DisplayAcceptedCommands.contains(tokens(1))) {
      tokens(1) match {
        case "pc" => return tokens.length == 2
        case "regs" | "piperegs" | "modules" => return tokens.length == 2
        case "module" | "pipereg" | "reg" => return tokens.length == 3
        case "inst" => {
          if (tokens.length == 3) {
            try {
              val i = tokens(2).toInt
            } catch {
              case e: Exception => return false
            }
          } else if(tokens.length > 3) {
            return false
          }
          return true
        }
        case _ => return false
      }
    }

    return false
  }

  def main(args: Array[String]): Unit = {
    require(args.length >= 2, "Error: Expected at least two argument\n" + helptext)

    println(s"Running test ${args(0)} on CPU design ${args(1)}")

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
    println(commands)
    var done = false

    val completionNodes: List[TreeCompleter.Node] = List(

    )

    val reader = LineReaderBuilder.builder
      .terminal(TerminalBuilder.builder.system(true).build)
      .completer(new TreeCompleter(
        node("print",
          node("reg"),
          node("regs"),
          node("pc"),
          node("inst"),
          node("pipereg"),
          node("piperegs")),
        node("display",
          node("reg"),
          node("regs"),
          node("pc"),
          node("inst"),
          node("pipereg"),
          node("piperegs"),
          node("modules"),
          node("module")),
        node("dump",
          node("all"),
          node("list")),
        node("step"),
        node("verbose"),
        node("?"),
        node("q")))
      .build

    var displayList: mutable.ListBuffer[Array[String]] = mutable.ListBuffer()

    while (!done) {
      var line: String =
        try {
          reader.readLine("Single stepper> ")
        } catch {
          case _: UserInterruptException =>
            println("Press Control-D to exit")
            ""

          case _: EndOfFileException =>
            break
            ""
        }
      val tokens = line.trim.split(" ")
      if (tokens.length > 0) {
        tokens(0) match {
          case "?" => println(commands)
          case "q" | "Q" => done = true
          case "s" | "step" if doStep(tokens, driver) => doDisplay(displayList, driver)
          case "v" | "verbose" if doVerboseStep(tokens, displayList, driver) =>
          case "p" | "print" => {
            if (tokens.length > 1) {
              if (!doPrint(tokens, driver)) println(commands)
            }
          }
          case "dump" => {
            if (tokens.length > 1) {
              if (!doDump(tokens, driver)) println(commands)
            }
          }
          case "d" | "display" if displayCommandOkay(tokens) => displayList += tokens
          case "undisplay" if tokens.length == 2 => {
            try {
              val index = tokens(1).toInt
              displayList.remove(index - 1)
            } catch {
              case e: java.lang.NumberFormatException => println(tokens(1) + " is not an integer")
              case e: java.lang.IndexOutOfBoundsException => println(tokens(1) + " is not in display")
            }
          }
          case "" =>
          case _ => println(commands)
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
