package dinocpu
class Disassembler {
  val R_TYPE_OPCODE = Integer.parseInt("0110011",2)
  val I_TYPE_OPCODE = Integer.parseInt("0010011",2)
  val STORE_OPCODE = Integer.parseInt("0100011",2)
  val LOAD_OPCODE = Integer.parseInt("0000011",2)
  val BRANCH_OPCODE = Integer.parseInt("1100011",2)
  val JUMP_OPCODE = Integer.parseInt("1101111",2)
  val AUIPC_OPCODE =  Integer.parseInt("0010111",2)
  val LUI_OPCODE =  Integer.parseInt("0110111",2)

  def sliceLong(num:Long, upper: Int,lower: Int): Long = {
   var mask: Long = scala.math.pow(2,upper - lower+1).toLong - 1
    mask = mask << lower
    //println(mask)
    (num & mask) >> lower
  }

  def parseRType(instruction:Long)={
    //println("R-type")
    val rd = sliceLong(instruction,11,7)
    val funct3 = sliceLong(instruction,14,12)
    val rs1 = sliceLong(instruction,19,15)
    val rs2 = sliceLong(instruction,24,20)
    val funct7 = sliceLong(instruction,31,25)

    val instName = funct3 match {
      case 0 => funct7 match {
        case 0 => "add"
        case 32 => "sub"
      }
      case 1 => "sll"
      case 2 => "slt"
      case 3 => "sltu"
      case 4 => "xor"
      case 5 => funct7 match {
        case 0 => "srl"
        case 32 => "sra"
      }
      case 6 => "or"
      case 7 => "and"
    }
    println(instName + " " + rd + ", " + rs1 + ", " + rs2)
  }
  def parseIType(instruction:Long)={
    val rd = sliceLong(instruction,11,7)
    val funct3 = sliceLong(instruction,14,12)
    val rs1 = sliceLong(instruction,19,15)
    val funct7 = sliceLong(instruction,31,25)
    val instName = funct3 match {
      case 0 => "addi"
      case 2 => "slti"
      case 3 => "sltiu"
      case 4 => "xori"
      case 6 => "ori"
      case 7 => "andi"
    }
    val imm = sliceLong(instruction,31,20)
    println(instName + " " + rd + ", " + rs1 + ", " + imm)
  }
  def parseStore(instruction:Long)={
    val offset = sliceLong(instruction,31,25) << 5 + sliceLong(instruction,11,7)
    val rs1 = sliceLong(instruction,19,15)
    val rs2 = sliceLong(instruction,24,20)
    val funct3 = sliceLong(instruction,14,12)
    val instName = funct3 match {
      case 0 => "sb"
      case 1 => "sh"
      case 2 => "sw"
    }
    println(instName + " " + rs2 + "," + rs1 +" (" + offset +")")
  }

  def parseLoad(instruction:Long)={
    val rd = sliceLong(instruction,11,7)
    val funct3 = sliceLong(instruction,14,12)
    val rs1 = sliceLong(instruction,19,15)
    val instName = funct3 match {
      case 0 => "lb"
      case 1 => "lh"
      case 2 => "lw"
      case 4 => "lbu"
      case 5 => "lhu"
    }
    val imm = sliceLong(instruction,31,20)
    println(instName + " " + rd + "," + rs1 +" (" + imm +")")
  }

  def parseBranch(instruction:Long)={

  }
  def parseJump(instruction:Long)={

  }
  def parseAuipc(instruction:Long)={

  }
  def parseLui(instruction:Long)={

  }

  def dissamble(instruction:Long)  = {
    println(instruction)
    val opCode = sliceLong(instruction,6,0)
    println(opCode)
    val _ = opCode match {
      case R_TYPE_OPCODE => parseRType(instruction)
      case I_TYPE_OPCODE => parseIType(instruction)
      case STORE_OPCODE => parseStore(instruction)
      case LOAD_OPCODE => parseLoad(instruction)
      case BRANCH_OPCODE => parseBranch(instruction)
      case JUMP_OPCODE => parseBranch(instruction)
      case AUIPC_OPCODE => parseBranch(instruction)
      case LUI_OPCODE => parseBranch(instruction)
      case _ => "Unknown"
    }
  }
  def main (args: Array[String] ): Unit = {
    dissamble(Integer.parseInt("00000000000000000000001010110011"))
  }
}

