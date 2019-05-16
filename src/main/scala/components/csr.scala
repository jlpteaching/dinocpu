/* The following code is based on https://github.com/freechipsproject/rocket-chip
 * implementation of the csr unit
 */

/* Describes register file that maintains machine state */

package dinocpu

import chisel3._
import collection.mutable.LinkedHashMap
import chisel3.util._

import scala.math._

object MCauses {
  //with interrupt
  val machine_soft_int = "h80000003".U
  val machine_timer_int = "h80000007".U
  val machine_ext_int = "h8000000b".U

  //non interrupt
  val misaligned_fetch = "h0".U
  val fetch_access = "h1".U
  val illegal_instruction = "h2".U
  val breakpoint = "h3".U
  val misaligned_load = "h4".U
  val load_access = "h5".U
  val misaligned_store = "h6".U
  val store_access = "h7".U
  val machine_ecall = "hb".U
}

object MCSRs {
  //machine information registers
  val mvendorid = 0xf11 //vendor id
  val marchid = 0xf12 //architecture id
  val mimpid = 0xf13 //implementation id
  val mhartid = 0xf14 //hardware thread id
  //machine trap setup
  val mstatus = 0x300 //machine status reg
  val misa = 0x301 //isa and extensions
  val medeleg = 0x302 //machine exception delegation reg
  val mideleg = 0x303 //machine interrupt delegation reg
  val mie = 0x304 //machine iterrupt-enable reg
  val mtvec = 0x305 //machine trap handler base address
  val mcounteren = 0x306 //machine counter enable

  //machine trap handling
  val mscratch = 0x340 //scratch reg for machine trap handlers
  val mepc = 0x341 //machine exception program counter
  val mcause = 0x342 //machine trap cause
  val mtval = 0x343 //machine bad address or instruction
  val mip = 0x344 //machine interrupt pending

  //machine memory protection
  //DONT NEED
  
  //machine counter/timers
  val mcycle = 0xb00 //machine cycle counter
  val minstret = 0xb02 //machine instructions retured counter
  val mcycleh = 0xb80
  val minstreth = 0xb82
  //performance counter setup
  val mcounterinhibit = 0x320
}

class MStatus extends Bundle{
  val sd = Bool() //dirty fs or xs
  val wpri1 = UInt(8.W) //reserved, zero
  val tsr = Bool() //trap on sret
  val tw = Bool() //timeout for supervisor wait for interrupt
  val tvm = Bool() //trap virtual memory
  val mxr = Bool() //make executable readable
  val sum = Bool() //supervisor user mem access
  val mprv = Bool() //modify priv, access memorr as mpp
  val xs = UInt(2.W) //user extension state
  val fs = UInt(2.W) //float state
  //previous privilege
  val mpp = UInt(2.W)
  val wpri2 = UInt(2.W)
  val spp = UInt(1.W)
  //previous interrupt enable
  val mpie = Bool()
  val wpri3 = Bool() //reserved, zero
  val spie = Bool()
  val upie = Bool() 
  //interrupt enable
  val mie = Bool() //machine interrupt enable
  val wpri4 = Bool() //reserved, zero
  val sie = Bool() //supervisor interrupt enable
  val uie = Bool() //user interrupt enable
}

class MISA extends Bundle{
  val mxl = UInt(2.W) //rv32, 64 , 128
  val wlrl = UInt(4.W) //reserved
  val extensions = UInt(26.W) //isa extensions
}

class MVendorID extends Bundle{
  val bank = UInt(25.W)
  val offset = UInt(7.W)
}

class MTVec extends Bundle{
  val base = UInt(30.W)
  val mode = UInt(2.W)
}

class MIx extends Bundle{
 val wpri1 = UInt(20.W)
 val meix = Bool()
 val wpri2 = UInt(1.W)
 val seix = Bool()
 val ueix = Bool()
 val mtix = Bool()
 val wpri3 = UInt(1.W)
 val stix = Bool()
 val utix = Bool()
 val msix = Bool()
 val wpri4 = UInt(1.W)
 val ssix = Bool()
 val usix = Bool()
}

class XCounterEnInhibit extends Bundle{
  val hpm31 = Bool()
  val hpm30 = Bool()
  val hpm29 = Bool()
  val hpm28 = Bool()
  val hpm27 = Bool()
  val hpm26 = Bool()
  val hpm25 = Bool()
  val hpm24 = Bool()
  val hpm23 = Bool()
  val hpm22 = Bool()
  val hpm21 = Bool()
  val hpm20 = Bool()
  val hpm19 = Bool()
  val hpm18 = Bool()
  val hpm17 = Bool()
  val hpm16 = Bool()
  val hpm15 = Bool()
  val hpm14 = Bool()
  val hpm13 = Bool()
  val hpm12 = Bool()
  val hpm11 = Bool()
  val hpm10 = Bool()
  val hpm9 = Bool()
  val hpm8 = Bool()
  val hpm7 = Bool()
  val hpm6 = Bool()
  val hpm5 = Bool()
  val hpm4 = Bool()
  val hpm3 = Bool()
  val ir = Bool()
  val tmzero = Bool()
  val cy = Bool()
}

class MCause extends Bundle{
  val interrupt = Bool()
  val exceptioncode = UInt(31.W)
}

object MCSRCmd{
  // commands
  val size = 3.W
  val execute = 0.asUInt(size)
  val nop = 0.asUInt(size)
  val write = 1.asUInt(size)
  val set = 2.asUInt(size)
  val clear = 3.asUInt(size)
  val interrupt = 4.asUInt(size)
  val read = 5.asUInt(size)
  
  val SIZE = 3.W
  val MSB = 31
  val LSB = 20
  val TRAPADDR = "h80000000".U
  val MPRV = 3.U
}

class CSRRegFile extends Module{
  //INIT CSR
  val io = IO(new Bundle{
    val illegal_inst = Input(Bool())//an exception signal for a non existent instruction or bad fields 
    val retire_inst = Input(Bool())//asserted if a valid instruction has finished
    val pc = Input(UInt(32.W)) //current program counter value
    val read_data = Input(UInt(32.W))//data from reg file used in csr instructions
    val inst = Input(UInt(32.W)) //full instruction used for decoding csrs internally
    val immid = Input(UInt(32.W)) //sext immidiate for immidiate csr instructions
    
    val read_illegal = Output(Bool())//an exception raised interally by a bad csr inst, used to raise illegal inst signal
    val write_illegal = Output(Bool())//raised interally by a bad csr inst, used to raise illegal inst signal
    val system_illegal = Output(Bool())//bad syscall instruction raised interally, used to raise illegal inst signal
    val csr_stall = Output(Bool())//used in conjunction with wait for interrupt inst, not needed in single cycle
    val eret = Output(Bool())//return vector from a trap
    val evec = Output(UInt(32.W)) //trap address
    val write_data = Output(UInt(32.W)) //previous csr reg state sent to GP registers
    val reg_write = Output(Bool())//should we allow write_data to be written into GP registers?
    val status = Output(new MStatus())//not needed in this design but useful if more ISA extensions
    val time = Output(UInt(32.W))//time of operation in cpu cycles
  })
  io := DontCare

  val reset_mstatus = WireInit(0.U.asTypeOf(new MStatus()))
  reset_mstatus.mpp := MCSRCmd.MPRV//machine mode
  reset_mstatus.mie := true.B//machine mode

  //contains info about system interrupts and privlidge mode
  val reg_mstatus = RegInit(reset_mstatus)
  //exception program counter, set when exception raised
  val reg_mepc = Reg(UInt(32.W))
  //contains cause of exception
  val reg_mcause = RegInit(0.U.asTypeOf(new MCause()))
  //register that can hold data to assist with exceptions/traps
  val reg_mtval = Reg(UInt(32.W))
  //scratch register for trap handler, useful for switching between mode memory spaces
  val reg_mscratch = Reg(UInt(32.W))
  //register used to set time for when timer interrupt should be raised
  val reg_mtimecmp = Reg(UInt(64.W))
  //register to indicate if trap handler should go directly to a specifc modes' trap handler
  //rather than trap to machine mode then swap context to a less privileged mode. used to save
  //performance, our implementation does not implement hardware to do this.
  val reg_medeleg = Reg(UInt(32.W))

  //indicates if we have a pending iterrupt for different interrupt types and modes
  val reg_mip = RegInit(0.U.asTypeOf(new MIx()))
  //indicates which interrupts are enabled
  val reg_mie = RegInit(0.U.asTypeOf(new MIx()))
  //used to halt cpu if WFI inst is seen, or can also just do nothing. This cpu doesn't
  //implement WFI inst....yet
  val reg_wfi = RegInit(false.B)
  //trap vector/address
  val reg_mtvec = RegInit(0.U.asTypeOf(new MTVec()))
  //current cpu time given in cycles
  val reg_time = WideCounter(64)
  //number of instructions that have been completed
  val reg_instret = WideCounter(64, io.retire_inst)
  //performance counters, not implemented
  val reg_mcounterinhibit = RegInit(0.U.asTypeOf(new XCounterEnInhibit()))
  //performance counter control
  val reg_mcounteren = RegInit(0.U.asTypeOf(new XCounterEnInhibit()))
  //machine status, contains interrupt bits, and priviledge mode
  val read_mstatus = io.status.asUInt()
  val isa_string = "I"
  //takes user defined ISA string character by character and calculates ascii number for each,
  //aligns the result to be a multiple of 2 then ors the results together to fit into MISA
  //this tells the system what extensions are implemented
  //val reg_misa = RegInit((BigInt(0) | isa_string.map(x => 1 << (x - 'A')).reduce(_|_)).U.asTypeOf(new MISA()))
  //same as above but hardcoded for only I extension
  val reg_misa = RegInit(16.U.asTypeOf(new MISA()))
  //if we are a company we can hardcode our implementation info here
  val reg_mvendorid = RegInit(0.U.asTypeOf(new MVendorID()))

  //this hashmap associates CSR addresses to the actual register contents
  //this is done to make decoding and working with csr's easier (avoid manual specification)
  val read_mapping = collection.mutable.LinkedHashMap[Int,Bits](
    MCSRs.mcounterinhibit -> reg_mcounterinhibit.asUInt, 
    MCSRs.mcycle -> reg_time.value,
    MCSRs.minstret -> reg_instret.value,
    MCSRs.mimpid -> 0.U,
    MCSRs.marchid -> 0.U,
    MCSRs.mvendorid -> 0.U,
    MCSRs.misa -> reg_misa.asUInt,
    MCSRs.mstatus -> read_mstatus,
    MCSRs.mtvec -> MCSRCmd.TRAPADDR,
    MCSRs.mip -> reg_mip.asUInt(),
    MCSRs.mie -> reg_mie.asUInt(),
    MCSRs.mscratch -> reg_mscratch,
    MCSRs.mepc -> reg_mepc,
    MCSRs.mtval -> reg_mtval,
    MCSRs.mcause -> reg_mcause.asUInt(),
    MCSRs.mhartid -> 0.U,
    MCSRs.medeleg -> reg_medeleg,
    MCSRs.mcycleh -> 0.U, 
    MCSRs.minstreth -> 0.U
    )
    
  //CSR DECODE
  val cmd = WireInit(3.U(3.W))
  
  when( io.inst(6, 0) === ("b1110011".U)){
    switch(io.inst(14, 12)){
      is("b011".U){
        cmd := MCSRCmd.clear
        io.reg_write := true.B
      }
      is("b111".U){
        cmd := MCSRCmd.clear
        io.reg_write := true.B
      } 
      is("b010".U){
        cmd := MCSRCmd.set
        io.reg_write := true.B
      }
      is("b110".U){
        cmd := MCSRCmd.set
        io.reg_write := true.B
      }
      is("b001".U){
        cmd := MCSRCmd.write
        io.reg_write := true.B
      }
      is("b101".U){
        cmd := MCSRCmd.write
        io.reg_write := true.B
      }
      is("b000".U){
        cmd := MCSRCmd.interrupt
        io.reg_write := false.B
      }
    }
  }.otherwise{
    cmd := MCSRCmd.nop
    io.reg_write := false.B
  }
  
  val csr = io.inst(MCSRCmd.MSB, MCSRCmd.LSB)
  val system_insn = cmd === MCSRCmd.interrupt
  val cpu_ren = cmd =/= MCSRCmd.nop && !system_insn

  //map is an infix operator on read_mapping. takes argument from decoded_addr() and applies it to
  //read_mapping which provides a set if it exists then checks if the csr in the set corresponds to
  //what the csr instruction specified. used for easier when statements below
  val decoded_addr = read_mapping map { case (k, v) => k -> (csr === k.U) }
  val priv_sufficient = MCSRCmd.MPRV >= csr(9,8)
  val read_only = csr(11,10).andR
  val cpu_wen = cpu_ren && cmd =/= MCSRCmd.read && priv_sufficient
  val wen = cpu_wen && !read_only
  val wdata = readModifyWriteCSR(cmd.asInstanceOf[UInt], io.write_data, Mux(io.inst(14),io.immid, io.read_data))

  //harware optimization? change this later?
  val opcode = 1.U << csr(2,0)
  val insn_call = system_insn && opcode(0)
  val insn_break = system_insn && opcode(1)
  val insn_ret = system_insn && opcode(2) && priv_sufficient
  //wait for interrupt inst not implemented
  val insn_wfi = system_insn && opcode(5) && priv_sufficient

  private def decodeAny(m: LinkedHashMap[Int,Bits]): Bool = m.map( { case(k: Int, _: Bits) => csr === k.U }).reduce(_ || _)
  io.read_illegal := 3.U < csr(9,8) || !decodeAny(read_mapping) 
  io.write_illegal := csr(11,10).andR
  io.system_illegal := 3.U < csr(9,8)

  io.status := reg_mstatus

  io.eret := insn_call || insn_break || insn_ret

  // ILLEGAL INSTR
  when (io.illegal_inst) {
    reg_mcause.interrupt := MCauses.illegal_instruction & "h80000000".U
    reg_mcause.exceptioncode := MCauses.illegal_instruction & "h7fffffff".U
    io.evec := "h80000000".U
    reg_mepc := io.pc // misaligned memory exceptions not supported...
  }
  
  //UNALIGNED MEM ACCESS
  /*
  when(io.???){
    reg_mcause.interrupt := MCauses.misaligned_fetch & "h80000000".U
    reg_mcause.exceptioncode := MCauses.misaligned_fetch & "h7fffffff".U
    io.evec := "h80000000".U
    reg_mepc := 
  }.elsewhen(io.???){
    reg_mcause.interrupt := MCauses.misaligned_load & "h80000000".U
    reg_mcause.exceptioncode := MCauses.misaligned_load & "h7fffffff".U
    io.evec := "h80000000".U
    reg_mepc :=
  }.elsewhen(io.???){
    reg_mcause.interrupt := MCauses.misaligned_store & "h80000000".U
    reg_mcause.exceptioncode := MCauses.misaligned_store & "h7fffffff".U
    io.evec := "h80000000".U
    reg_mepc :=

  }*/

  //assert(PopCount(insn_ret :: io.exception :: Nil) <= 1, "these conditions must be mutually exclusive")

   when (reg_time.value >= reg_mtimecmp) {
      reg_mip.mtix := true.B
   }

  //MRET
  when (insn_ret && !csr(10)) {
    reg_mstatus.mie := reg_mstatus.mpie
    reg_mstatus.mpie := true.B
    io.evec := reg_mepc
  }

  //ECALL
  when(insn_call){
    io.evec := "h80000004".U
    reg_mcause.interrupt := MCauses.machine_ecall & "h80000000".U
    reg_mcause.exceptioncode := MCauses.machine_ecall & "h7fffffff".U
  }

  //EBREAK
  when(insn_break){
    io.evec := "h80000008".U
    reg_mcause.interrupt := MCauses.breakpoint & "h80000000".U
    reg_mcause.exceptioncode := MCauses.breakpoint & "h7fffffff".U
  }

  io.time := reg_time.value
  io.csr_stall := reg_wfi


  io.write_data := Mux1H(for ((k, v) <- read_mapping) yield decoded_addr(k) -> v)

  when (wen) {
    //MISA IS FIXED IN THIS IMPLEMENATION
    
    //MVENDORID IS FIXED IN THIS IMPLEMENTATION

    //MARCHID IS FIXED IN THIS IMPLEMENTATION

    //MIMPID IS FIXED IN THIS IMPLEMENTATION
    
    //MHARTID IS FIXED IN THIS IMPLEMENTATION
    
    //MSTATUS
    /* Only need to worry about m mode interrupts so no need to worry about setting
     * mpie, mpp, and mie correctly with respect to other modes.
     * non implemented modes wired to 0
     */
    when (decoded_addr(MCSRs.mstatus)) {
      val new_mstatus = wdata.asTypeOf(new MStatus())
      reg_mstatus.mie := new_mstatus.mie
      reg_mstatus.mpie := new_mstatus.mpie
      //unused bits in mstatus m-mode only specified by spec
      reg_mstatus.spp := 0.U
      reg_mstatus.uie := 0.U
      reg_mstatus.upie := 0.U
      reg_mstatus.mprv := 0.U
      reg_mstatus.mxr := 0.U
      reg_mstatus.sum := 0.U
      reg_mstatus.tvm := 0.U
      reg_mstatus.tw := 0.U
      reg_mstatus.tsr := 0.U
      reg_mstatus.fs := 0.U
      reg_mstatus.xs := 0.U
      reg_mstatus.sd := 0.U
    }
    
    //MTVEC IS FIXED IN THIS IMPLEMENTATION

    //MDELEG DOES NOT EXIST IN M-MODE IMPLEMENTATION
    
    //MIDELEG DOES NOT EXIST IN M-MODE IMPLEMENTATION

    
    //MIP
    /* mtip read only, cleared on timercmp write
     * meip read only, set by external interrupt controller
     */
    when (decoded_addr(MCSRs.mip)) {
      val new_mip = wdata.asTypeOf(new MIx())
      reg_mip.msix := new_mip.msix
      reg_mip.seix := 0.U
      reg_mip.ueix := 0.U
      reg_mip.stix := 0.U
      reg_mip.utix := 0.U
      reg_mip.ssix := 0.U
      reg_mip.usix := 0.U

    }
    //MIE
    /* deals with external interrupts similar to  mip but
     * m mode bits are r and w
     */
    when (decoded_addr(MCSRs.mie)) {
      val new_mie = wdata.asTypeOf(new MIx())
      reg_mie.meix := new_mie.meix
      reg_mie.msix := new_mie.msix
      reg_mie.mtix := new_mie.mtix
      reg_mip.seix := 0.U
      reg_mip.ueix := 0.U
      reg_mip.stix := 0.U
      reg_mip.utix := 0.U
      reg_mip.ssix := 0.U
      reg_mip.usix := 0.U

    }
    //MCOUNTEREB IS FIXED IN THIS IMPLEMENTATION BECAUSE NO S | U MODE
    
    //MCOUNTINHIBIT
    /* stops counting cycles and retired instructions if need be
     *
     */
    when (decoded_addr(MCSRs.mcounterinhibit)) {
      val new_mcounterinhibit = wdata.asTypeOf(new XCounterEnInhibit())
      reg_mcounterinhibit := new_mcounterinhibit
      when( reg_mcounterinhibit.cy === false.B) {
        writeCounter(MCSRs.mcycle, reg_time, wdata)
      }
      when( reg_mcounterinhibit.ir === false.B){
        writeCounter(MCSRs.minstret, reg_instret, wdata)
      }
    }
    
    //MSCRATCH
    when (decoded_addr(MCSRs.mscratch)) { reg_mscratch := wdata }
    
    //MEPC
    /* hardcoded to be 32 bit aligned because no compressed isa last 2 bits 0
     */
    when (decoded_addr(MCSRs.mepc))     { reg_mepc := (wdata(32-1,0) >> 2.U) << 2.U }
    //MCAUSE
    /* Only write to on interrupt for hardware. software can write whenever
     * masks msb and 5 lsb from wdata
     */
    when (decoded_addr(MCSRs.mcause))   { 
      reg_mcause.interrupt := (wdata & ((BigInt(1) << (32-1)) + 31).U) & "h80000000".U /* only implement 5 LSBs and MSB */
      reg_mcause.exceptioncode := (wdata & ((BigInt(1) << (32-1)) + 31).U) & "h7fffffff".U /* only implement 5 LSBs and MSB */

    }

    when (decoded_addr(MCSRs.mtval))    { reg_mtval := wdata(32-1,0) }
    when (decoded_addr(MCSRs.medeleg))    { reg_medeleg := wdata(32-1,0) }
  }

  //takes counter data and data to write and modifies it 32 bits at a time
  def writeCounter(lo: Int, ctr: WideCounter, wdata: UInt) = {
    val hi = lo + MCSRs.mcycleh - MCSRs.mcycle
    when (decoded_addr(hi)) { ctr := Cat(wdata(ctr.value.getWidth-33, 0), ctr.value(31, 0)) }
    when (decoded_addr(lo)) { ctr := Cat(ctr.value(ctr.value.getWidth-1, 32), wdata) }
  }

  //takes in csr command and sees if it maps to any int the defined sequence and determines
  //resulting csr data with bitwise operations
  def readModifyWriteCSR(cmd: UInt, rdata: UInt, wdata: UInt) =
(Mux(Seq(MCSRCmd.set, MCSRCmd.clear).map(cmd === _).reduce(_||_), rdata, 0.U) | wdata) & ~Mux(cmd === MCSRCmd.clear, wdata, 0.U)
}

//used for timers and performance counters.  case class lets us use comparison
//operators on the content of the object rather than the reference to the object
case class WideCounter(width: Int, inc: UInt = 1.U, reset: Boolean = true)
{
  private val isWide = width > 2*inc.getWidth
  private val smallWidth = if (isWide) inc.getWidth max log2Ceil(width) else width
  private val small = if (reset) RegInit(0.asUInt(smallWidth.W)) else Reg(UInt(smallWidth.W))
  private val nextSmall = small +& inc
  small := nextSmall

  private val large = if (isWide) {
    val r = if (reset) RegInit(0.asUInt((width - smallWidth).W)) else Reg(UInt((width - smallWidth).W))
    when (nextSmall(smallWidth)) { r := r + 1.U }
    r
  } else null

  val value = if (isWide) Cat(large, small) else small
  lazy val carryOut = {
    val lo = (small ^ nextSmall) >> 1
    if (!isWide) lo else {
      val hi = Mux(nextSmall(smallWidth), large ^ (large +& 1.U), 0.U) >> 1
      Cat(hi, lo)
    }
  }

  def := (x: UInt) = {
    small := x
    if (isWide) large := x >> smallWidth
  }
}

