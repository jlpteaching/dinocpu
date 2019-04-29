package dinocpu

import chisel3._
import chisel3.core._

class Instruction(instruction: Long){
  def slice(upper: Int, lower: Int): Long = {
      var mask: Long = scala.math.pow(2, upper - lower + 1).toLong - 1
      mask = mask << lower
      (instruction & mask) >> lower
  }
  val opcode = slice(6,0)
  val rd = slice(11,7)
  val funct3 = slice(14,12)
  val rs1 = slice(19,15)
  val rs2 = slice(24,20)
  val funct7 = slice(31,25)
  val sign = slice(31,31)
  val iImm = slice(30,20) * (if (sign == 1)  -1 else 0)// i-type immediate value
}

class Disassembler {
  val R_TYPE_OPCODE = Integer.parseInt("0110011",2)
  val I_TYPE_OPCODE = Integer.parseInt("0010011",2)
  val STORE_OPCODE = Integer.parseInt("0100011",2)
  val LOAD_OPCODE = Integer.parseInt("0000011",2)
  val BRANCH_OPCODE = Integer.parseInt("1100011",2)
  val JAR_OPCODE = Integer.parseInt("1101111",2)
  val JALR_OPCODE = Integer.parseInt("1100111",2)
  val AUIPC_OPCODE =  Integer.parseInt("0010111",2)
  val LUI_OPCODE =  Integer.parseInt("0110111",2)

  def parseRType(instr:Instruction):String={
    val instName = instr.funct3 match {
      case 0 => instr.funct7 match {
        case 0 => "add"
        case 32 => "sub"
      }
      case 1 => "sll"
      case 2 => "slt"
      case 3 => "sltu"
      case 4 => "xor"
      case 5 => instr.funct7 match {
        case 0 => "srl"
        case 32 => "sra"
      }
      case 6 => "or"
      case 7 => "and"
    }
    instName + " x" + instr.rd + ", x" + instr.rs1 + ", x" + instr.rs2
  }
  def parseIType(instr:Instruction)={
    val shamt = instr.slice(24,20)
    instr.funct3 match {
      case 0 => "addi" + " x" + instr.rd + ", x" + instr.rs1 + ", " + instr.iImm
      case 2 => "slti" +  " x" + instr.rd + ", x" + instr.rs1 + ", " + instr.iImm
      case 3 => "sltiu" +  " x" + instr.rd + ", x" + instr.rs1 + ", " + instr.iImm
      case 4 => "xori" +  " x" + instr.rd + ", x" + instr.rs1 + ", " + instr.iImm
      case 6 => "ori" +  " x" + instr.rd + ", x" + instr.rs1 + ", " + instr.iImm
      case 7 => "andi" + " x" + instr.rd + ", x" + instr.rs1 + ", " + instr.iImm
      case 1 => "slli" + " x" + instr.rd + ", x" + instr.rs1 + ", " + shamt
      case 5 => instr.funct7 match {
        case 0 => "srli" +  " x" + instr.rd + ", x" + instr.rs1 + ", " + shamt
        case 8 => "srai" +  " x" + instr.rd + ", x" + instr.rs1 + ", " + shamt
      }
    }
  }
  def parseStore(instr:Instruction):String={
    val offset = (instr.slice(31,25) << 5) + instr.slice(11,7)
    val instName = instr.funct3 match {
      case 0 => "sb"
      case 1 => "sh"
      case 2 => "sw"
    }
    instName + " x" + instr.rs2 + ", " + offset +"(x" + instr.rs1 +")"
  }

  def parseLoad(instr:Instruction)={
    val instName = instr.funct3 match {
      case 0 => "lb"
      case 1 => "lh"
      case 2 => "lw"
      case 4 => "lbu"
      case 5 => "lhu"
    }
    instName + " x" + instr.rd + ", " + instr.iImm +"(x" + instr.rs1 +")"
  }

  def parseBranch(instr:Instruction):String={
    var offset = (instr.slice(7,7) << 11) + (instr.slice(30,25) << 5) + (instr.slice(11,8) << 1)
    if(instr.sign==1)
      offset = - offset
    val instName = instr.funct3 match {
      case 0 => "beq"
      case 1 => "bne"
      case 4 => "blt"
      case 5 => "bge"
      case 6 => "bltu"
      case 7 => "bgeu"
    }
    instName + " x" + instr.rs1 + ", x" + instr.rs2 + ", pc + " + offset
  }
  def parseJal(instr:Instruction):String={
    var imm = (instr.slice(19,12) << 12) +
      (instr.slice(20,20) << 10) +
      (instr.slice(30,21) << 1)
    if(instr.sign==1)
      imm = - imm
    "jal x" + instr.rd + "(" + imm + ")"
  }
  def parseJalr(instr:Instruction):String={
    "jalr x" + instr.rd +", x" + instr.rs1 + "(" + instr.iImm + ")"
   }
  def parseAuipc(instr:Instruction):String={
    "lui x" + instr.rd + " " + instr.iImm
  }
  def parseLui(instr:Instruction):String={
    "auipc x" + instr.rd + " " + instr.iImm
  }

  def disassemble(instruction:Long):String  = {
    val instr = new Instruction(instruction)
    instr.opcode match {
      case R_TYPE_OPCODE => parseRType(instr)
      case I_TYPE_OPCODE => parseIType(instr)
      case STORE_OPCODE => parseStore(instr)
      case LOAD_OPCODE => parseLoad(instr)
      case BRANCH_OPCODE => parseBranch(instr)
      case JAR_OPCODE => parseJal(instr)
      case JALR_OPCODE => parseJalr(instr)
      case AUIPC_OPCODE => parseBranch(instr)
      case LUI_OPCODE => parseBranch(instr)
      case _ => "Unknown"
    }
  }
  def disassemble(instruction:UInt):String  = {
    ""
  }

}

