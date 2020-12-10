package dinocpu

import firrtl.ir._
import dinocpu.test._
import scala.swing._
import scala.collection.mutable
import scala.collection.mutable.HashSet
import scala.collection.Set
import scala.swing.event.Event
import javax.swing.ToolTipManager
import java.awt.Color
import treadle.TreadleTester

// Port interface used by View to describe I/O ports on modules
trait Port extends Publisher {
  val module: CircuitModule
  val name: String
  var value: BigInt
}

// Model implementation of Port interface loaded from FIRRTL Circuit
class FIRRTLPort(n: String, bN: String, m: FIRRTLModule) extends Port {
  override val module:FIRRTLModule = m
  override val name = n
  override var value: BigInt = 0

  // bindingName is the fully qualified name in the simulator SymbolTable
  val bindingName = bN
}

// Emitted by Port to notify View the value has changed
object ValueChange extends Event {

}

// PortView is a Component that displays port information as text
class PortView(input: Port) extends Label {
  text = input.name
  tooltip = input.value.toString

  input.reactions += {
    case ValueChange => tooltip = input.value.toString 
  }
}

// DinoWire is an abstract type that describes connections between modules
trait DinoWire {
  val from: Port
  val to: Port  
}

// Static helper methods for constructing wires
object DinoWire {
  def wireFromPorts(t: Port, f: Port): DinoWire = {
    new DinoWire {
      val from = f
      val to = t
    }
  }
}

trait Dimension2D {
  val width: Int
  val height: Int
}

// CircuitModule is an interface for a module that contains a name and a list of I/O ports
trait CircuitModule {
  val label: String
  val input: Seq[Port]
  val output: Seq[Port]

  override def equals(that: Any): Boolean =
    that match {
        case that: CircuitModule => label == that.label
        case _ => super.equals(that)
    }
}

// FIRRTLModule is an extension of CircuitModule that uses FIRRTLPorts
// this is to expose the bindingName to the module
trait FIRRTLModule extends CircuitModule {
  val input: Seq[FIRRTLPort]
  val output: Seq[FIRRTLPort]
}

/* Module View is a component that displays a module
 * it contains a title row for the name and flexible layout for
 * input ports in the left column, and output ports in a right column */
class ModuleView(module: CircuitModule) extends BorderPanel {
  private val center = new BoxPanel(Orientation.Horizontal) {
    private val inputs = new BoxPanel(Orientation.Vertical) {
      for(input <- module.input) {
        val label = new PortView(input)
        contents += label
      }

      contents += Swing.VGlue
      background = Color.WHITE
    }

    private val outputs = new BoxPanel(Orientation.Vertical) {
      for(input <- module.output) {
        val label = new PortView(input)

        // align the output labels to the right of its BoxLayout
        label.xLayoutAlignment = 1.0
        contents += label
      }

      contents += Swing.VGlue
      background = Color.WHITE
    }

    contents += inputs
    contents += Swing.HGlue
    contents += Swing.HStrut(20)
    contents += outputs

    background = Color.WHITE
  }

  layout(center) = BorderPanel.Position.Center
  layout(new Label(module.label)) = BorderPanel.Position.North

  layoutManager.setHgap(15)

  background = Color.WHITE
}

object CircuitStep extends Event {
  val why = ""
}

// CircuitModel is a top-level interface that gets passed into the UI Component to build the view hierarchy
trait CircuitModel extends Publisher {
  val if_id: Seq[CircuitModule]
  val id_ex: Seq[CircuitModule]
  val ex_mem: Seq[CircuitModule]
  val mem_wb: Seq[CircuitModule]

  val fetchModules: Seq[CircuitModule]
  val decodeModules: Seq[CircuitModule]
  val executeModules: Seq[CircuitModule]
  val memoryModules: Seq[CircuitModule]
  val writeBackModules: Seq[CircuitModule]

  val connections: Seq[DinoWire]

  var cycle: BigInt
}

/* FIRRTLModel constructs a representation of a FIRRTL Circuit as a pipelined CPU,
 * Given a list of modules for each pipeline register, and a list of modules in the CPU,
 * the constructor assigns all the modules to the pipeline stages.  
 */
class FIRRTLModel(cpuModules: Seq[FIRRTLModule], connections_arg: Seq[DinoWire], 
  if_id_arg: Seq[FIRRTLModule], id_ex_arg: Seq[FIRRTLModule], ex_mem_arg: Seq[FIRRTLModule], mem_wb_arg: Seq[FIRRTLModule]) extends CircuitModel {
  override val connections = connections_arg
  override val if_id = if_id_arg
  override val id_ex = id_ex_arg
  override val ex_mem = ex_mem_arg
  override val mem_wb = mem_wb_arg
  
  /* DFS starting at right Port to find a target module
   * a module is reachable from a target port if a DFS can find a path between
   * that port and any port in the query. The path is traversed over all the connections
   * going backwards from "to" to "from". 
   */
  def isLeftOfPort(query: CircuitModule, right: Port, seen: Set[Port]): Boolean = seen.contains(right) match {
    case false => {
      val cons = connections.filter(c => right == c.to)
      cons.exists(c => c.from.module == query) || cons.exists(c => isLeftOfPort(query, c.from, seen + right))
    }
    case true => false
  }

  // A module is to the left of a target if any of the query ports are reachable from any of input ports of the target
  def isLeftOf(query: CircuitModule, right: CircuitModule): Boolean =
    right.input.exists(p => isLeftOfPort(query, p, HashSet[Port]()))
  
  // the two following functions are defined as the opposite of the above
  def isRightOfPort(query: CircuitModule, left: Port, seen: Set[Port]): Boolean = seen.contains(left) match {
    case false => {
      val cons = connections.filter(c => left == c.from)
      cons.exists(c => c.to.module == query) || cons.exists(c => isRightOfPort(query, c.to, seen + left))
    }
    case true => false
  }

  def isRightOf(query: CircuitModule, left: CircuitModule): Boolean =
    left.output.exists(p => isRightOfPort(query, p, HashSet[Port]()))
  

  override val fetchModules = new mutable.ArrayBuffer[FIRRTLModule]
  override val decodeModules = new mutable.ArrayBuffer[FIRRTLModule]
  override val executeModules = new mutable.ArrayBuffer[FIRRTLModule]
  override val memoryModules = new mutable.ArrayBuffer[FIRRTLModule]
  override val writeBackModules = new mutable.ArrayBuffer[FIRRTLModule]
 
  /* iteratively add each of the cpuModules to a pipeline stage
   * a module is in a stage if it is to the left of any of the next pipeline registers
   * or to the right of any of the previous pipeline register
   */
  cpuModules.foreach(mod => {
    if (if_id.exists(reg => isLeftOf(mod, reg)))
      fetchModules += mod
    else if (if_id.exists(reg => isRightOf(mod, reg)) || id_ex.exists(reg => isLeftOf(mod, reg)))
      decodeModules += mod
    else if (id_ex.exists(reg => isRightOf(mod, reg)) || ex_mem.exists(reg => isLeftOf(mod, reg)))
      executeModules += mod
    else if (ex_mem.exists(reg => isRightOf(mod, reg)) || mem_wb.exists(reg => isLeftOf(mod, reg)))
      memoryModules += mod
    else if (mem_wb.exists(reg => isRightOf(mod, reg)))
      writeBackModules += mod
  })

  override var cycle: BigInt = 0
  val toCheck = fetchModules ++ decodeModules ++ executeModules ++ memoryModules ++ writeBackModules ++ if_id ++ id_ex ++ ex_mem ++ mem_wb

  def loadFromDriver(driver: CPUTesterDriver) {
    val simulator = driver.simulator

    // peek all the ports within the circuit hierarchy and publish ValueChaged events if needed
    for(module <- toCheck) {
      for(port <- module.input ++ module.output) {
        try {
          val oldVal = port.value
          port.value = simulator.peek(port.bindingName)
          
          if(oldVal != port.value)
            port.publish(ValueChange)
        } catch {
          case e: AssertionError => {
            /* val symbol = table.get(port.bindingName)
            println(table.inputPortsNames.contains(port.bindingName))
            port.value = symbol.map(s => s.normalize(driver.simulator.engine.dataStore(s))).getOrElse(0) */
          }
        }
        
      }
    }

    cycle = driver.cycle
  }
}

object FIRRTLModel {
  def apply(schema: firrtl.ir.Circuit): FIRRTLModel = {
    val top = schema.modules.find(p => p.name == "Top").get
    val cpu = schema.modules.find(p => p.name.contains("CPU")).orElse({
      var ret: Option[DefModule] = None
      def recurse(s: Statement) {
        s match {
          case b: Block => b.stmts.foreach(recurse)
          case i: DefInstance if i.name == "cpu" && !ret.isDefined => ret = schema.modules.find(p => p.name == i.module)
          case _ =>
        }
      }
      top.foreachStmt(recurse)
      ret
    }).get

    val cpuModule = new FIRRTLModule {
      val label = "cpu"
      val input = cpu.ports.filter(p => p.direction == Input && p.name != "clock" && p.name != "reset").map(p => new FIRRTLPort(p.name, "cpu." + p.name, this))
      val output = cpu.ports.filter(p => p.direction == Output).map(p => new FIRRTLPort(p.name, "cpu." + p.name, this))
    }
    
    val cpuModules = mutable.ArrayBuffer[FIRRTLModule]()
    val cpuNodes = mutable.Queue[FIRRTLModule]()
    val connections = mutable.ArrayBuffer[DinoWire]()

    def processFromExpression(e: Expression): Iterable[FIRRTLPort] = e match {
      case s: SubField => s.expr match {
        case r: Reference => cpuModules.find(mod => mod.label == r.name)
                            .flatMap(mod => (mod.input ++ mod.output).find(p => p.name == s.name))
                            .orElse(cpuModule.input.find(p => p.name == s.name))
                            .orElse(cpuModule.output.find(p => p.name == s.name))
        case _ => None
      }
      case r: Reference => locateFromConnection(r)
      case p: DoPrim => p.args.map(exp => processFromExpression(exp)).flatten
      case m: Mux => {
        processFromExpression(m.cond) ++
        processFromExpression(m.fval) ++
        processFromExpression(m.tval)
      }
      case _ => None
    }

    def processToExpression(e: Expression): Option[FIRRTLPort] = e match {
      case s: SubField => s.expr match {
        case r: Reference => cpuModules.find(mod => mod.label == r.name)
                            .flatMap(mod => mod.input.find(p => p.name == s.name))
                            .orElse(cpuModule.input.find(p => p.name == s.name))
        case _ => None
      }
      case r: Reference => locateToConnection(r)
      case _ => None
    }

    def locateFromConnection(r: Reference): Option[FIRRTLPort] = {
      cpuNodes.find(mod => mod.label == r.name)
          .flatMap(m => m.output.find(p => p.name == r.name))
        .orElse(cpuModules.find(mod => mod.label == r.name)
          .flatMap(m => m.output.find(p => p.name == r.name)))
        .orElse(cpuModule.input.find(p => p.name == r.name))
        .orElse(cpuModule.output.find(p => p.name == r.name))
    }

    def locateToConnection(r: Reference): Option[FIRRTLPort] = 
      cpuModules.find(mod => mod.label == r.name)
        .flatMap(m => m.input.find(p => p.name == r.name))
        .orElse(cpuModule.input.find(p => p.name == r.name))

    def processDeclaration(r: IsDeclaration, inCPU: Boolean = false): FIRRTLModule = {
      def expand(name: String): String = inCPU match {
        case false => name
        case true => "cpu." + name
      }

      val node = new FIRRTLModule {
        val label = r.name
        val input = List(new FIRRTLPort(r.name, expand(r.name), this))
        val output = List(new FIRRTLPort(r.name, expand(r.name), this))
      }

      // add a connection through the node for DFS
      connections += DinoWire.wireFromPorts(node.output(0), node.input(0))
      node
    }

    def processRegister(r: DefRegister, inCPU: Boolean = false) {
      cpuModules += processDeclaration(r, inCPU)
    }

    /* processNode attempts to convert a node (mux, combinational logic) into a module
     * by definining an input and output port with the same name,
     * wiring all dependencies into the input side from the definition
     * wiring the input to the output (allows for DFS)
     * anywhere this node is used a wire will be created from the output side
     */
    def processNode(r: DefNode, inCPU: Boolean = false) {
      val module = processDeclaration(r, inCPU)

      def processExpression(e: Expression, e_module: FIRRTLModule): FIRRTLModule = {
        e match {
          case p: DoPrim => {
            p.args.foreach(e => processExpression(e, e_module))
            e_module
          }
          case m: Mux => {
            processExpression(m.cond, e_module)
            processExpression(m.fval, e_module)
            processExpression(m.tval, e_module)
            e_module
          }
          case _ => {
            val fromPort = processFromExpression(e)
            Option(module.input(0)).zip(fromPort).map(DinoWire.wireFromPorts _ tupled).map(connections.+=)
            e_module
          }
        }
      }
      
      cpuNodes += processExpression(r.value, module)
    }

    // processInstance adds a module definition to the list of CPUModules
    def processInstance(i: DefInstance, inCPU: Boolean = false) {
      def expand(moduleName: String, portName: String): String = inCPU match {
        case false => moduleName + "." + portName 
        case true => "cpu." + moduleName + "." + portName
      }
      
      val mod_def = schema.modules.find(p => p.name == i.module).get
      val new_module = new FIRRTLModule {
        val label = i.name
        val input = mod_def.ports.filter(port => port.direction == Input && port.name != "clock" && port.name != "reset")
                                .map(port => new FIRRTLPort(port.name, expand(i.name, port.name), this))
        val output = mod_def.ports.filter(port => port.direction == Output)
                                .map(port => new FIRRTLPort(port.name, expand(i.name, port.name), this))
      }
      cpuModules += new_module
    }

    // processConnect adds wires between dependencies on the right hand side to the port on the left hand side
    def processConnect(c: Connect) {
      val fromPort = processFromExpression(c.expr)
      val toPort = processToExpression(c.loc)
      for(to <- toPort.toIterable) {
        for(from <- fromPort) {
          connections += DinoWire.wireFromPorts(to, from)
        }
      }
    }

    cpu.foreachStmt(f => {
      f match {
        case Block(stmts) => {
          stmts.foreach(st => {
            st match {
              case r: DefRegister => processRegister(r, true)
              case n: DefNode => processNode(n, true)
              case i: DefInstance => processInstance(i, true)
              case c: Connect => processConnect(c)
              case _ => 
            }
          })
        }
        
      }
    })

    top.foreachStmt(stmt => stmt match {
      case Block(stmts) => {
        stmts.foreach(st => st match {
          case r: DefRegister => processRegister(r)
          case n: DefNode => processNode(n)
          case i: DefInstance if i.name != "cpu" => processInstance(i)
          case c: Connect => processConnect(c)
          case _ =>
        })
      }
    })
    
    val moduleNotFound = new FIRRTLModule {
      override val label = "not found"
      override val input = List[FIRRTLPort]()
      override val output = List[FIRRTLPort]()
    }

    val if_id = cpuModules.find(mod => mod.label == "if_id").getOrElse(moduleNotFound)
    val id_ex = cpuModules.find(mod => mod.label == "id_ex").getOrElse(moduleNotFound)
    val id_ex_ctrl = cpuModules.find(mod => mod.label == "id_ex_ctrl").getOrElse(moduleNotFound)
    val ex_mem = cpuModules.find(mod => mod.label == "ex_mem").getOrElse(moduleNotFound)
    val ex_mem_ctrl = cpuModules.find(mod => mod.label == "ex_mem_ctrl").getOrElse(moduleNotFound)
    val mem_wb = cpuModules.find(mod => mod.label == "mem_wb").getOrElse(moduleNotFound)
    val mem_wb_ctrl = cpuModules.find(mod => mod.label == "mem_wb_ctrl").getOrElse(moduleNotFound)

    // removeNode removes a module from the network graph adding connections from all modules
    // wired to inputs of the node to all modules wired to outputs of the node
    def removeNode(node: FIRRTLModule) {
      // filter out the node's circular from-to wire from dependencies
      val toNode = connections.filter(c => c.to.module == node && c.from.module != node)
      val fromNode = connections.filter(c => c.from.module == node && c.to.module != node)

      for(toCon <- toNode) {
        for(fromCon <- fromNode) {
          connections += DinoWire.wireFromPorts(fromCon.to, toCon.from)
        }
      }

      connections --= toNode
      connections --= fromNode

      // delete the node's from-to wire here
      connections --= connections.filter(c => c.to.module == node && c.from.module == node)
    }

    // reduce cpuNodes
    while(cpuNodes.size > 0) {
      removeNode(cpuNodes.dequeue)
    }

    cpuModules -= if_id
    cpuModules -= id_ex
    cpuModules -= id_ex_ctrl
    cpuModules -= ex_mem
    cpuModules -= ex_mem_ctrl
    cpuModules -= mem_wb
    cpuModules -= mem_wb_ctrl

    return new FIRRTLModel(cpuModules, connections, List(if_id), List(id_ex, id_ex_ctrl), List(ex_mem, ex_mem_ctrl), List(mem_wb, mem_wb_ctrl))
  }
}

class Circuit(model: CircuitModel) extends BoxPanel(Orientation.Horizontal) {

  class StagePanel(modules: Seq[CircuitModule]) extends FlowPanel(FlowPanel.Alignment.Leading)() {
    for(module <- modules) {
      val panel = new ModuleView(module)

      contents += panel
    }

    preferredSize = new Dimension(400, preferredSize.height)
    hGap = 30
    vGap = 20

    alignOnBaseline = true
  }

  contents += new StagePanel(model.fetchModules)
  contents += new BoxPanel(Orientation.Vertical) {
    model.if_id.foreach(reg => {
      contents += new ModuleView(reg)
      contents += Swing.VStrut(5)
    })
  }
  contents += new StagePanel(model.decodeModules)
  contents += new BoxPanel(Orientation.Vertical) {
    model.id_ex.foreach(reg => {
      contents += new ModuleView(reg)
      contents += Swing.VStrut(5)
    })
  }
  contents += new StagePanel(model.executeModules)
  contents += new BoxPanel(Orientation.Vertical) {
    model.ex_mem.foreach(reg => {
      contents += new ModuleView(reg)
      contents += Swing.VStrut(5)
    })
  }
  contents += new StagePanel(model.memoryModules)
  contents += new BoxPanel(Orientation.Vertical) {
    new BoxPanel(Orientation.Vertical) {
    model.mem_wb.foreach(reg => {
      contents += new ModuleView(reg)
      contents += Swing.VStrut(5)
    })
  }
  }
  contents += new StagePanel(model.writeBackModules)

  model.reactions += {
    case CircuitStep => repaint()
  }
}

// UI is the top-level view component that constructs the view hierarchy from a CircuitModel
class UI(model: CircuitModel) extends MainFrame {
  val stepButton = new Button("Step")
  val cycleCount = new Label("Cycle: " + model.cycle.toString)

  // mainPane contains the rendered circuit
  val mainPane = new SplitPane(Orientation.Vertical)

  // controlPane displays the step button and cycle count
  val controlPane = new BoxPanel(Orientation.Vertical)
  val circuitPane = new ScrollPane

  controlPane.contents += cycleCount
  controlPane.contents += Swing.VStrut(5)
  controlPane.contents += stepButton
  controlPane.minimumSize = controlPane.preferredSize

  mainPane.leftComponent = circuitPane

  mainPane.rightComponent = controlPane
  mainPane.leftComponent.minimumSize = new Dimension(600, 640)

  title = "GUI Program #1"
  preferredSize = new Dimension(800, 640)
  contents = mainPane

  menuBar = new MenuBar {
    contents += new Menu("About") {
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += Swing.HStrut(10)
        contents += new BoxPanel(Orientation.Vertical) {
          contents += Swing.VStrut(10)
          contents += new Label("Author: Kaelan Mikowicz")
          contents += Swing.VStrut(10)
          contents += new Label("Email: kaelan.miko@gmail.com")
          contents += Swing.VStrut(10)
        }
        contents += Swing.HStrut(10)
      }
      
    }
  }

  listenTo(stepButton)

  def setStepButtonAction(action: Action) {
    stepButton.action = action
  }

  circuitPane.contents = new Circuit(model)
  
  model.reactions += {
    case CircuitStep => cycleCount.text = "Cycle: " + model.cycle.toString
  }
}

// SimulationController maps the ui step button to trigger the Model to update
class SimulationController(driver: CPUTesterDriver, model: FIRRTLModel, ui: UI) {
  def step() {
    driver.step(1)
    model.loadFromDriver(driver)
    model.publish(CircuitStep)
  }

  ui.setStepButtonAction(Action("Step")(step)) 
  model.loadFromDriver(driver)
}

object visualize {
  val helptext = "usage: visualize <test name>"

  def main(args: Array[String]): Unit = {
    require(args.length >= 1, "Error: Expected at least one argument\n" + helptext)

    println(s"Running test ${args(0)} on CPU design Pipelined")

    val test = InstTests.nameMap(args(0))
    val latency = args(1).toInt

    val (cpuType, memType, memPortType) =
    if (latency == 0) {
      ("pipelined-non-combin", "combinational", "combinational-port")
    } else {
      ("pipelined-non-combin", "non-combinational", "non-combinational-port")
    }

    val driver = new CPUTesterDriver(cpuType, "", test.binary, test.extraName, memType,
      memPortType, latency)
    driver.initRegs(test.initRegs)
    driver.initMemory(test.initMem)

    val model = FIRRTLModel(firrtl.Parser.parse(driver.compiledFirrtl))

    val ui = new UI(model)
    ToolTipManager.sharedInstance().setInitialDelay(5)

    val controller = new SimulationController(driver, model, ui)
    ui.pack()
    ui.visible = true
  }
}